package com.unify.data

import com.unify.core.data.UnifyDataManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import org.w3c.dom.Storage
import org.w3c.dom.get
import org.w3c.dom.set

/**
 * Web/JS平台UnifyDataManager实现
 * 基于localStorage和IndexedDB
 */
class UnifyDataManagerImpl : UnifyDataManager {
    private val storage: Storage = localStorage
    private val mutex = Mutex()
    private val json = Json { ignoreUnknownKeys = true }
    
    // 响应式数据流管理
    private val dataFlows = mutableMapOf<String, MutableStateFlow<Any?>>()
    
    // 缓存过期时间管理
    private val cacheExpiryMap = mutableMapOf<String, Long>()
    
    // 同步设置
    private var syncEnabled = false
    
    // 键前缀
    private val keyPrefix = "unify_"
    private val objectPrefix = "${keyPrefix}obj_"
    private val expiryPrefix = "${keyPrefix}exp_"
    
    init {
        // 监听storage事件以支持跨标签页同步
        window.addEventListener("storage") { event ->
            val storageEvent = event as org.w3c.dom.events.StorageEvent
            val key = storageEvent.key
            if (key?.startsWith(keyPrefix) == true) {
                val cleanKey = key.removePrefix(keyPrefix).removePrefix("obj_")
                val newValue = storageEvent.newValue
                updateDataFlow(cleanKey, newValue)
            }
        }
    }
    
    override suspend fun getString(key: String, defaultValue: String?): String? = mutex.withLock {
        if (isCacheExpired(key)) {
            remove(key)
            return defaultValue
        }
        storage["$keyPrefix$key"] ?: defaultValue
    }
    
    override suspend fun setString(key: String, value: String) = mutex.withLock {
        storage["$keyPrefix$key"] = value
        updateDataFlow(key, value)
    }
    
    override suspend fun getInt(key: String, defaultValue: Int): Int = mutex.withLock {
        if (isCacheExpired(key)) {
            remove(key)
            return defaultValue
        }
        storage["$keyPrefix$key"]?.toIntOrNull() ?: defaultValue
    }
    
    override suspend fun setInt(key: String, value: Int) = mutex.withLock {
        storage["$keyPrefix$key"] = value.toString()
        updateDataFlow(key, value)
    }
    
    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean = mutex.withLock {
        if (isCacheExpired(key)) {
            remove(key)
            return defaultValue
        }
        storage["$keyPrefix$key"]?.toBooleanStrictOrNull() ?: defaultValue
    }
    
    override suspend fun setBoolean(key: String, value: Boolean) = mutex.withLock {
        storage["$keyPrefix$key"] = value.toString()
        updateDataFlow(key, value)
    }
    
    override suspend fun getLong(key: String, defaultValue: Long): Long = mutex.withLock {
        if (isCacheExpired(key)) {
            remove(key)
            return defaultValue
        }
        storage["$keyPrefix$key"]?.toLongOrNull() ?: defaultValue
    }
    
    override suspend fun setLong(key: String, value: Long) = mutex.withLock {
        storage["$keyPrefix$key"] = value.toString()
        updateDataFlow(key, value)
    }
    
    override suspend fun getFloat(key: String, defaultValue: Float): Float = mutex.withLock {
        if (isCacheExpired(key)) {
            remove(key)
            return defaultValue
        }
        storage["$keyPrefix$key"]?.toFloatOrNull() ?: defaultValue
    }
    
    override suspend fun setFloat(key: String, value: Float) = mutex.withLock {
        storage["$keyPrefix$key"] = value.toString()
        updateDataFlow(key, value)
    }
    
    override suspend fun <T> getObject(key: String, clazz: Class<T>): T? = mutex.withLock {
        if (isCacheExpired(key)) {
            remove(key)
            return null
        }
        
        val jsonString = storage["$objectPrefix$key"] ?: return null
        try {
            json.decodeFromString<T>(jsonString)
        } catch (e: Exception) {
            console.error("Failed to deserialize object for key: $key", e)
            null
        }
    }
    
    override suspend fun <T> setObject(key: String, value: T) = mutex.withLock {
        try {
            val jsonString = json.encodeToString(value)
            storage["$objectPrefix$key"] = jsonString
            updateDataFlow(key, value)
        } catch (e: Exception) {
            console.error("Failed to serialize object for key: $key", e)
            throw RuntimeException("Failed to serialize object for key: $key", e)
        }
    }
    
    override suspend fun clear() = mutex.withLock {
        val keysToRemove = mutableListOf<String>()
        
        // 收集所有以我们的前缀开头的键
        for (i in 0 until storage.length) {
            val key = storage.key(i)
            if (key?.startsWith(keyPrefix) == true) {
                keysToRemove.add(key)
            }
        }
        
        // 删除所有键
        keysToRemove.forEach { key ->
            storage.removeItem(key)
        }
        
        cacheExpiryMap.clear()
        dataFlows.values.forEach { it.value = null }
    }
    
    override suspend fun remove(key: String) = mutex.withLock {
        storage.removeItem("$keyPrefix$key")
        storage.removeItem("$objectPrefix$key")
        storage.removeItem("$expiryPrefix$key")
        cacheExpiryMap.remove(key)
        updateDataFlow(key, null)
    }
    
    override suspend fun contains(key: String): Boolean = mutex.withLock {
        if (isCacheExpired(key)) {
            remove(key)
            return false
        }
        storage["$keyPrefix$key"] != null || storage["$objectPrefix$key"] != null
    }
    
    override suspend fun getAllKeys(): Set<String> = mutex.withLock {
        val keys = mutableSetOf<String>()
        
        for (i in 0 until storage.length) {
            val key = storage.key(i)
            when {
                key?.startsWith(keyPrefix) == true && !key.startsWith(objectPrefix) && !key.startsWith(expiryPrefix) -> {
                    val cleanKey = key.removePrefix(keyPrefix)
                    if (!isCacheExpired(cleanKey)) {
                        keys.add(cleanKey)
                    }
                }
                key?.startsWith(objectPrefix) == true -> {
                    val cleanKey = key.removePrefix(objectPrefix)
                    if (!isCacheExpired(cleanKey)) {
                        keys.add(cleanKey)
                    }
                }
            }
        }
        
        keys
    }
    
    override fun <T> observeKey(key: String, clazz: Class<T>): Flow<T?> {
        return getOrCreateDataFlow(key).map { value ->
            when {
                value == null -> null
                clazz.isInstance(value) -> clazz.cast(value)
                value is String -> {
                    try {
                        json.decodeFromString<T>(value)
                    } catch (e: Exception) {
                        null
                    }
                }
                else -> null
            }
        }
    }
    
    override fun observeStringKey(key: String): Flow<String?> {
        return getOrCreateDataFlow(key).map { it as? String }
    }
    
    override fun observeIntKey(key: String): Flow<Int> {
        return getOrCreateDataFlow(key).map { 
            when (it) {
                is Int -> it
                is String -> it.toIntOrNull() ?: 0
                else -> 0
            }
        }
    }
    
    override fun observeBooleanKey(key: String): Flow<Boolean> {
        return getOrCreateDataFlow(key).map { 
            when (it) {
                is Boolean -> it
                is String -> it.toBooleanStrictOrNull() ?: false
                else -> false
            }
        }
    }
    
    override suspend fun setCacheExpiry(key: String, expiryMillis: Long) = mutex.withLock {
        val expiryTime = kotlinx.datetime.Clock.System.now().toEpochMilliseconds() + expiryMillis
        cacheExpiryMap[key] = expiryTime
        storage["$expiryPrefix$key"] = expiryTime.toString()
    }
    
    override suspend fun isCacheExpired(key: String): Boolean {
        val expiryTime = cacheExpiryMap[key] ?: storage["$expiryPrefix$key"]?.toLongOrNull()
        if (expiryTime == null) return false
        
        val currentTime = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        return currentTime > expiryTime
    }
    
    override suspend fun clearExpiredCache() = mutex.withLock {
        val currentTime = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        val expiredKeys = mutableListOf<String>()
        
        // 检查内存中的过期时间
        cacheExpiryMap.forEach { (key, expiryTime) ->
            if (currentTime > expiryTime) {
                expiredKeys.add(key)
            }
        }
        
        // 检查存储中的过期时间
        for (i in 0 until storage.length) {
            val key = storage.key(i)
            if (key?.startsWith(expiryPrefix) == true) {
                val cleanKey = key.removePrefix(expiryPrefix)
                val expiryTime = storage[key]?.toLongOrNull()
                if (expiryTime != null && currentTime > expiryTime) {
                    expiredKeys.add(cleanKey)
                }
            }
        }
        
        // 删除过期的键
        expiredKeys.forEach { key ->
            storage.removeItem("$keyPrefix$key")
            storage.removeItem("$objectPrefix$key")
            storage.removeItem("$expiryPrefix$key")
            cacheExpiryMap.remove(key)
            updateDataFlow(key, null)
        }
    }
    
    override suspend fun syncToCloud() {
        if (!syncEnabled) return
        
        try {
            // 使用Web API进行云端同步
            val allKeys = getAllKeys()
            val syncData = mutableMapOf<String, String>()
            
            allKeys.forEach { key ->
                val value = storage["$keyPrefix$key"]
                val objectValue = storage["$objectPrefix$key"]
                val expiryValue = storage["$expiryPrefix$key"]
                
                when {
                    value != null -> syncData["$keyPrefix$key"] = value
                    objectValue != null -> syncData["$objectPrefix$key"] = objectValue
                }
                
                if (expiryValue != null) {
                    syncData["$expiryPrefix$key"] = expiryValue
                }
            }
            
            // 将数据存储到sessionStorage作为临时云端存储
            // 实际项目中应该使用真实的云存储服务
            val syncJson = json.encodeToString(syncData)
            window.sessionStorage.setItem("unify_cloud_sync", syncJson)
            
        } catch (e: Exception) {
            console.error("Cloud sync failed:", e.message)
        }
    }
    
    override suspend fun syncFromCloud() {
        if (!syncEnabled) return
        
        try {
            // 从sessionStorage获取云端数据
            val syncJson = window.sessionStorage.getItem("unify_cloud_sync")
            if (syncJson != null) {
                val syncData: Map<String, String> = json.decodeFromString(syncJson)
                
                syncData.forEach { (key, value) ->
                    storage[key] = value
                    
                    // 更新数据流
                    when {
                        key.startsWith(keyPrefix) && !key.startsWith(objectPrefix) && !key.startsWith(expiryPrefix) -> {
                            val cleanKey = key.removePrefix(keyPrefix)
                            updateDataFlow(cleanKey, value)
                        }
                        key.startsWith(objectPrefix) -> {
                            val cleanKey = key.removePrefix(objectPrefix)
                            updateDataFlow(cleanKey, value)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            console.error("Cloud sync from failed:", e.message)
        }
    }
    
    override fun isSyncEnabled(): Boolean = syncEnabled
    
    override fun setSyncEnabled(enabled: Boolean) {
        syncEnabled = enabled
    }
    
    private fun getOrCreateDataFlow(key: String): Flow<Any?> {
        return dataFlows.getOrPut(key) {
            val initialValue = storage["$keyPrefix$key"] ?: storage["$objectPrefix$key"]
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
