package com.unify.data

import com.unify.core.data.UnifyDataManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import platform.Foundation.*
import platform.darwin.NSObject
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

/**
 * iOS平台UnifyDataManager实现
 * 基于NSUserDefaults和文件系统
 */
class UnifyDataManagerImpl : UnifyDataManager {
    private val userDefaults = NSUserDefaults.standardUserDefaults
    private val documentsPath = NSSearchPathForDirectoriesInDomains(
        NSDocumentDirectory, NSUserDomainMask, true
    ).firstOrNull() as? String ?: ""
    
    private val mutex = Mutex()
    private val json = Json { ignoreUnknownKeys = true }
    
    // 响应式数据流管理
    private val dataFlows = mutableMapOf<String, MutableStateFlow<Any?>>()
    
    // 缓存过期时间管理
    private val cacheExpiryMap = mutableMapOf<String, Long>()
    
    // 同步设置
    private var syncEnabled = false
    
    override suspend fun getString(key: String, defaultValue: String?): String? = mutex.withLock {
        if (isCacheExpired(key)) {
            remove(key)
            return defaultValue
        }
        userDefaults.stringForKey(key) ?: defaultValue
    }
    
    override suspend fun setString(key: String, value: String) = mutex.withLock {
        userDefaults.setObject(value, key)
        userDefaults.synchronize()
        updateDataFlow(key, value)
    }
    
    override suspend fun getInt(key: String, defaultValue: Int): Int = mutex.withLock {
        if (isCacheExpired(key)) {
            remove(key)
            return defaultValue
        }
        val value = userDefaults.integerForKey(key)
        if (userDefaults.objectForKey(key) == null) defaultValue else value.toInt()
    }
    
    override suspend fun setInt(key: String, value: Int) = mutex.withLock {
        userDefaults.setInteger(value.toLong(), key)
        userDefaults.synchronize()
        updateDataFlow(key, value)
    }
    
    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean = mutex.withLock {
        if (isCacheExpired(key)) {
            remove(key)
            return defaultValue
        }
        val value = userDefaults.boolForKey(key)
        if (userDefaults.objectForKey(key) == null) defaultValue else value
    }
    
    override suspend fun setBoolean(key: String, value: Boolean) = mutex.withLock {
        userDefaults.setBool(value, key)
        userDefaults.synchronize()
        updateDataFlow(key, value)
    }
    
    override suspend fun getLong(key: String, defaultValue: Long): Long = mutex.withLock {
        if (isCacheExpired(key)) {
            remove(key)
            return defaultValue
        }
        val value = userDefaults.integerForKey(key)
        if (userDefaults.objectForKey(key) == null) defaultValue else value
    }
    
    override suspend fun setLong(key: String, value: Long) = mutex.withLock {
        userDefaults.setInteger(value, key)
        userDefaults.synchronize()
        updateDataFlow(key, value)
    }
    
    override suspend fun getFloat(key: String, defaultValue: Float): Float = mutex.withLock {
        if (isCacheExpired(key)) {
            remove(key)
            return defaultValue
        }
        val value = userDefaults.floatForKey(key)
        if (userDefaults.objectForKey(key) == null) defaultValue else value
    }
    
    override suspend fun setFloat(key: String, value: Float) = mutex.withLock {
        userDefaults.setFloat(value, key)
        userDefaults.synchronize()
        updateDataFlow(key, value)
    }
    
    override suspend fun <T> getObject(key: String, clazz: Class<T>): T? = mutex.withLock {
        if (isCacheExpired(key)) {
            remove(key)
            return null
        }
        
        val jsonString = userDefaults.stringForKey("${key}_object") ?: return null
        try {
            json.decodeFromString<T>(jsonString)
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun <T> setObject(key: String, value: T) = mutex.withLock {
        try {
            val jsonString = json.encodeToString(value)
            userDefaults.setObject(jsonString, "${key}_object")
            userDefaults.synchronize()
            updateDataFlow(key, value)
        } catch (e: Exception) {
            throw RuntimeException("Failed to serialize object for key: $key", e)
        }
    }
    
    override suspend fun clear() = mutex.withLock {
        val domain = NSBundle.mainBundle.bundleIdentifier
        if (domain != null) {
            userDefaults.removePersistentDomainForName(domain)
            userDefaults.synchronize()
        }
        cacheExpiryMap.clear()
        dataFlows.values.forEach { it.value = null }
    }
    
    override suspend fun remove(key: String) = mutex.withLock {
        userDefaults.removeObjectForKey(key)
        userDefaults.removeObjectForKey("${key}_object")
        userDefaults.synchronize()
        cacheExpiryMap.remove(key)
        updateDataFlow(key, null)
    }
    
    override suspend fun contains(key: String): Boolean = mutex.withLock {
        if (isCacheExpired(key)) {
            remove(key)
            return false
        }
        userDefaults.objectForKey(key) != null || userDefaults.objectForKey("${key}_object") != null
    }
    
    override suspend fun getAllKeys(): Set<String> = mutex.withLock {
        val allKeys = mutableSetOf<String>()
        val dictionary = userDefaults.dictionaryRepresentation()
        
        dictionary.keys.forEach { key ->
            val keyString = key.toString()
            if (!keyString.endsWith("_object")) {
                allKeys.add(keyString)
            } else {
                allKeys.add(keyString.removeSuffix("_object"))
            }
        }
        
        // 过滤掉过期的键
        allKeys.filter { !isCacheExpired(it) }.toSet()
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
        val expiryTime = NSDate().timeIntervalSince1970 * 1000 + expiryMillis
        cacheExpiryMap[key] = expiryTime.toLong()
    }
    
    override suspend fun isCacheExpired(key: String): Boolean {
        val expiryTime = cacheExpiryMap[key] ?: return false
        val currentTime = (NSDate().timeIntervalSince1970 * 1000).toLong()
        return currentTime > expiryTime
    }
    
    override suspend fun clearExpiredCache() = mutex.withLock {
        val currentTime = (NSDate().timeIntervalSince1970 * 1000).toLong()
        val expiredKeys = cacheExpiryMap.filter { (_, expiryTime) ->
            currentTime > expiryTime
        }.keys
        
        expiredKeys.forEach { key ->
            userDefaults.removeObjectForKey(key)
            userDefaults.removeObjectForKey("${key}_object")
            cacheExpiryMap.remove(key)
            updateDataFlow(key, null)
        }
        
        if (expiredKeys.isNotEmpty()) {
            userDefaults.synchronize()
        }
    }
    
    override suspend fun syncToCloud() {
        if (!syncEnabled) return
        
        try {
            val ubiquitousStore = NSUbiquitousKeyValueStore.defaultStore
            val allKeys = getAllKeys()
            
            allKeys.forEach { key ->
                val value = userDefaults.objectForKey(key)
                val objectValue = userDefaults.objectForKey("${key}_object")
                
                when {
                    value != null -> ubiquitousStore.setObject(value, key)
                    objectValue != null -> ubiquitousStore.setObject(objectValue, "${key}_object")
                }
            }
            
            ubiquitousStore.synchronize()
        } catch (e: Exception) {
            // 同步失败时静默处理，避免影响应用正常运行
        }
    }
    
    override suspend fun syncFromCloud() {
        if (!syncEnabled) return
        
        try {
            val ubiquitousStore = NSUbiquitousKeyValueStore.defaultStore
            val cloudData = ubiquitousStore.dictionaryRepresentation
            
            cloudData.keys.forEach { key ->
                val keyString = key.toString()
                val value = cloudData[key]
                
                if (value != null) {
                    userDefaults.setObject(value, keyString)
                    
                    // 更新数据流
                    if (keyString.endsWith("_object")) {
                        val originalKey = keyString.removeSuffix("_object")
                        updateDataFlow(originalKey, value)
                    } else {
                        updateDataFlow(keyString, value)
                    }
                }
            }
            
            userDefaults.synchronize()
        } catch (e: Exception) {
            // 同步失败时静默处理
        }
    }
    
    override fun isSyncEnabled(): Boolean = syncEnabled
    
    override fun setSyncEnabled(enabled: Boolean) {
        syncEnabled = enabled
    }
    
    private fun getOrCreateDataFlow(key: String): Flow<Any?> {
        return dataFlows.getOrPut(key) {
            val initialValue = userDefaults.objectForKey(key) ?: userDefaults.objectForKey("${key}_object")
            MutableStateFlow(initialValue)
        }.asStateFlow()
    }
    
    private fun updateDataFlow(key: String, value: Any?) {
        dataFlows.getOrPut(key) { MutableStateFlow(null) }.value = value
    }
}
