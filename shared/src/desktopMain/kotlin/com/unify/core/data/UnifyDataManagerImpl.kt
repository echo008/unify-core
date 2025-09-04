package com.unify.core.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.io.File
import java.util.Properties
import java.util.concurrent.ConcurrentHashMap

/**
 * Desktop平台UnifyDataManager实现
 * 基于文件系统和Properties文件
 */
class UnifyDataManagerImpl : UnifyDataManager {
    private val dataDir = File(System.getProperty("user.home"), ".unify-core")
    private val propertiesFile = File(dataDir, "data.properties")
    private val objectsDir = File(dataDir, "objects")
    
    private val properties = Properties()
    private val mutex = Mutex()
    private val json = Json { ignoreUnknownKeys = true }
    
    // 响应式数据流管理
    private val dataFlows = ConcurrentHashMap<String, MutableStateFlow<Any?>>()
    
    // 缓存过期时间管理
    private val cacheExpiryMap = ConcurrentHashMap<String, Long>()
    
    // 同步设置
    private var syncEnabled = false
    
    init {
        initializeDataDirectory()
        loadProperties()
    }
    
    override suspend fun getString(key: String, defaultValue: String?): String? = mutex.withLock {
        if (isCacheExpired(key)) {
            remove(key)
            return defaultValue
        }
        properties.getProperty(key, defaultValue)
    }
    
    override suspend fun setString(key: String, value: String) = mutex.withLock {
        properties.setProperty(key, value)
        saveProperties()
        updateDataFlow(key, value)
    }
    
    override suspend fun getInt(key: String, defaultValue: Int): Int = mutex.withLock {
        if (isCacheExpired(key)) {
            remove(key)
            return defaultValue
        }
        properties.getProperty(key)?.toIntOrNull() ?: defaultValue
    }
    
    override suspend fun setInt(key: String, value: Int) = mutex.withLock {
        properties.setProperty(key, value.toString())
        saveProperties()
        updateDataFlow(key, value)
    }
    
    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean = mutex.withLock {
        if (isCacheExpired(key)) {
            remove(key)
            return defaultValue
        }
        properties.getProperty(key)?.toBooleanStrictOrNull() ?: defaultValue
    }
    
    override suspend fun setBoolean(key: String, value: Boolean) = mutex.withLock {
        properties.setProperty(key, value.toString())
        saveProperties()
        updateDataFlow(key, value)
    }
    
    override suspend fun getLong(key: String, defaultValue: Long): Long = mutex.withLock {
        if (isCacheExpired(key)) {
            remove(key)
            return defaultValue
        }
        properties.getProperty(key)?.toLongOrNull() ?: defaultValue
    }
    
    override suspend fun setLong(key: String, value: Long) = mutex.withLock {
        properties.setProperty(key, value.toString())
        saveProperties()
        updateDataFlow(key, value)
    }
    
    override suspend fun getFloat(key: String, defaultValue: Float): Float = mutex.withLock {
        if (isCacheExpired(key)) {
            remove(key)
            return defaultValue
        }
        properties.getProperty(key)?.toFloatOrNull() ?: defaultValue
    }
    
    override suspend fun setFloat(key: String, value: Float) = mutex.withLock {
        properties.setProperty(key, value.toString())
        saveProperties()
        updateDataFlow(key, value)
    }
    
    override suspend fun <T> getObject(key: String, clazz: Class<T>): T? = mutex.withLock {
        if (isCacheExpired(key)) {
            remove(key)
            return null
        }
        
        val objectFile = File(objectsDir, "$key.json")
        if (!objectFile.exists()) return null
        
        try {
            val jsonString = objectFile.readText()
            // 使用反射或序列化库来反序列化对象
            // 这里简化实现，实际应用中需要更复杂的序列化处理
            json.decodeFromString<T>(jsonString)
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun <T> setObject(key: String, value: T) = mutex.withLock {
        val objectFile = File(objectsDir, "$key.json")
        try {
            val jsonString = json.encodeToString(value)
            objectFile.writeText(jsonString)
            updateDataFlow(key, value)
        } catch (e: Exception) {
            throw RuntimeException("Failed to serialize object for key: $key", e)
        }
    }
    
    override suspend fun clear() = mutex.withLock {
        properties.clear()
        saveProperties()
        
        // 清理对象文件
        if (objectsDir.exists()) {
            objectsDir.listFiles()?.forEach { it.delete() }
        }
        
        cacheExpiryMap.clear()
        dataFlows.values.forEach { it.value = null }
    }
    
    override suspend fun remove(key: String) = mutex.withLock {
        properties.remove(key)
        saveProperties()
        
        // 删除对象文件
        val objectFile = File(objectsDir, "$key.json")
        if (objectFile.exists()) {
            objectFile.delete()
        }
        
        cacheExpiryMap.remove(key)
        updateDataFlow(key, null)
    }
    
    override suspend fun contains(key: String): Boolean = mutex.withLock {
        if (isCacheExpired(key)) {
            remove(key)
            return false
        }
        properties.containsKey(key) || File(objectsDir, "$key.json").exists()
    }
    
    override suspend fun getAllKeys(): Set<String> = mutex.withLock {
        val propertyKeys = properties.keys.map { it.toString() }.toSet()
        val objectKeys = if (objectsDir.exists()) {
            objectsDir.listFiles()?.map { it.nameWithoutExtension }?.toSet() ?: emptySet()
        } else {
            emptySet()
        }
        
        // 过滤掉过期的键
        (propertyKeys + objectKeys).filter { !isCacheExpired(it) }.toSet()
    }
    
    override fun <T> observeKey(key: String, clazz: Class<T>): Flow<T?> {
        return getOrCreateDataFlow(key).map { value ->
            when {
                value == null -> null
                clazz.isInstance(value) -> clazz.cast(value)
                else -> null
            }
        }
    }
    
    override fun observeStringKey(key: String): Flow<String?> {
        return getOrCreateDataFlow(key).map { it as? String }
    }
    
    override fun observeIntKey(key: String): Flow<Int> {
        return getOrCreateDataFlow(key).map { (it as? Int) ?: 0 }
    }
    
    override fun observeBooleanKey(key: String): Flow<Boolean> {
        return getOrCreateDataFlow(key).map { (it as? Boolean) ?: false }
    }
    
    override suspend fun setCacheExpiry(key: String, expiryMillis: Long) = mutex.withLock {
        val expiryTime = System.currentTimeMillis() + expiryMillis
        cacheExpiryMap[key] = expiryTime
    }
    
    override suspend fun isCacheExpired(key: String): Boolean {
        val expiryTime = cacheExpiryMap[key] ?: return false
        return System.currentTimeMillis() > expiryTime
    }
    
    override suspend fun clearExpiredCache() = mutex.withLock {
        val currentTime = System.currentTimeMillis()
        val expiredKeys = cacheExpiryMap.filter { (_, expiryTime) ->
            currentTime > expiryTime
        }.keys
        
        expiredKeys.forEach { key ->
            properties.remove(key)
            File(objectsDir, "$key.json").delete()
            cacheExpiryMap.remove(key)
            updateDataFlow(key, null)
        }
        
        if (expiredKeys.isNotEmpty()) {
            saveProperties()
        }
    }
    
    override suspend fun syncToCloud() {
        if (!syncEnabled) return
        
        try {
            // 创建同步备份文件
            val syncFile = File(dataDir, "cloud_sync_backup.json")
            val syncData = mutableMapOf<String, Any>()
            
            // 收集所有数据
            val allKeys = getAllKeys()
            allKeys.forEach { key ->
                val propertyValue = properties.getProperty(key)
                if (propertyValue != null) {
                    syncData[key] = propertyValue
                }
                
                // 检查对象文件
                val objectFile = File(objectsDir, "$key.json")
                if (objectFile.exists()) {
                    syncData["${key}_object"] = objectFile.readText()
                }
            }
            
            // 保存到本地同步文件（模拟云端存储）
            val syncJson = json.encodeToString(syncData)
            syncFile.writeText(syncJson)
            
        } catch (e: Exception) {
            println("Cloud sync failed: ${e.message}")
        }
    }
    
    override suspend fun syncFromCloud() {
        if (!syncEnabled) return
        
        try {
            // 从本地同步文件读取（模拟从云端获取）
            val syncFile = File(dataDir, "cloud_sync_backup.json")
            if (!syncFile.exists()) return
            
            val syncJson = syncFile.readText()
            val syncData: Map<String, String> = json.decodeFromString(syncJson)
            
            syncData.forEach { (key, value) ->
                if (key.endsWith("_object")) {
                    // 恢复对象文件
                    val originalKey = key.removeSuffix("_object")
                    val objectFile = File(objectsDir, "$originalKey.json")
                    objectFile.writeText(value)
                    updateDataFlow(originalKey, value)
                } else {
                    // 恢复属性值
                    properties.setProperty(key, value)
                    updateDataFlow(key, value)
                }
            }
            
            saveProperties()
            
        } catch (e: Exception) {
            println("Cloud sync from failed: ${e.message}")
        }
    }
    
    override fun isSyncEnabled(): Boolean = syncEnabled
    
    override fun setSyncEnabled(enabled: Boolean) {
        syncEnabled = enabled
    }
    
    private fun initializeDataDirectory() {
        if (!dataDir.exists()) {
            dataDir.mkdirs()
        }
        if (!objectsDir.exists()) {
            objectsDir.mkdirs()
        }
    }
    
    private fun loadProperties() {
        if (propertiesFile.exists()) {
            try {
                propertiesFile.inputStream().use { input ->
                    properties.load(input)
                }
            } catch (e: Exception) {
                println("Failed to load properties: ${e.message}")
            }
        }
    }
    
    private fun saveProperties() {
        try {
            propertiesFile.outputStream().use { output ->
                properties.store(output, "UnifyCore Desktop Data")
            }
        } catch (e: Exception) {
            throw RuntimeException("Failed to save properties", e)
        }
    }
    
    private fun getOrCreateDataFlow(key: String): Flow<Any?> {
        return dataFlows.getOrPut(key) {
            val initialValue = properties.getProperty(key) ?: run {
                val objectFile = File(objectsDir, "$key.json")
                if (objectFile.exists()) objectFile.readText() else null
            }
            MutableStateFlow(initialValue)
        }.asStateFlow()
    }
    
    private fun updateDataFlow(key: String, value: Any?) {
        dataFlows.getOrPut(key) { MutableStateFlow(null) }.value = value
    }
}

actual object UnifyDataManagerFactory {
    actual fun create(): UnifyDataManager {
        return UnifyDataManagerImpl()
    }
}
