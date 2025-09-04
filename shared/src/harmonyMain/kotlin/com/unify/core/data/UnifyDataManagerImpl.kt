package com.unify.core.data

import com.unify.core.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * HarmonyOS平台UnifyDataManager实现
 * 基于HarmonyOS分布式数据管理和Preferences
 */
class UnifyDataManagerImpl : UnifyDataManager {
    
    // 数据变化监听
    private val dataChangeFlows = mutableMapOf<String, MutableStateFlow<Any?>>()
    
    // 缓存管理
    private val cache = mutableMapOf<String, CacheEntry>()
    
    // HarmonyOS Preferences模拟实现
    private val preferences = mutableMapOf<String, Any>()
    
    // 分布式数据存储模拟
    private val distributedStorage = mutableMapOf<String, Any>()
    
    override suspend fun putString(key: String, value: String) {
        preferences[key] = value
        notifyDataChange(key, value)
        
        // HarmonyOS分布式同步
        syncToDistributedStorage(key, value)
    }
    
    override suspend fun getString(key: String, defaultValue: String): String {
        return preferences[key] as? String ?: defaultValue
    }
    
    override suspend fun putInt(key: String, value: Int) {
        preferences[key] = value
        notifyDataChange(key, value)
        syncToDistributedStorage(key, value)
    }
    
    override suspend fun getInt(key: String, defaultValue: Int): Int {
        return preferences[key] as? Int ?: defaultValue
    }
    
    override suspend fun putLong(key: String, value: Long) {
        preferences[key] = value
        notifyDataChange(key, value)
        syncToDistributedStorage(key, value)
    }
    
    override suspend fun getLong(key: String, defaultValue: Long): Long {
        return preferences[key] as? Long ?: defaultValue
    }
    
    override suspend fun putFloat(key: String, value: Float) {
        preferences[key] = value
        notifyDataChange(key, value)
        syncToDistributedStorage(key, value)
    }
    
    override suspend fun getFloat(key: String, defaultValue: Float): Float {
        return preferences[key] as? Float ?: defaultValue
    }
    
    override suspend fun putBoolean(key: String, value: Boolean) {
        preferences[key] = value
        notifyDataChange(key, value)
        syncToDistributedStorage(key, value)
    }
    
    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return preferences[key] as? Boolean ?: defaultValue
    }
    
    override suspend fun <T> putObject(key: String, value: T) {
        try {
            val jsonString = Json.encodeToString(value)
            preferences[key] = jsonString
            notifyDataChange(key, value)
            syncToDistributedStorage(key, jsonString)
        } catch (e: Exception) {
            throw DataException("Failed to serialize object: ${e.message}")
        }
    }
    
    override suspend fun <T> getObject(key: String, defaultValue: T): T {
        return try {
            val jsonString = preferences[key] as? String
            if (jsonString != null) {
                Json.decodeFromString<T>(jsonString)
            } else {
                defaultValue
            }
        } catch (e: Exception) {
            defaultValue
        }
    }
    
    override suspend fun remove(key: String) {
        preferences.remove(key)
        distributedStorage.remove(key)
        notifyDataChange(key, null)
    }
    
    override suspend fun clear() {
        preferences.clear()
        distributedStorage.clear()
        dataChangeFlows.values.forEach { flow ->
            flow.value = null
        }
    }
    
    override suspend fun contains(key: String): Boolean {
        return preferences.containsKey(key)
    }
    
    override suspend fun getAllKeys(): Set<String> {
        return preferences.keys.toSet()
    }
    
    override fun <T> observeData(key: String): Flow<T?> {
        val flow = dataChangeFlows.getOrPut(key) {
            MutableStateFlow(preferences[key])
        }
        return flow.asStateFlow() as Flow<T?>
    }
    
    override suspend fun putCache(key: String, value: Any, ttlMillis: Long) {
        val expiryTime = System.currentTimeMillis() + ttlMillis
        cache[key] = CacheEntry(value, expiryTime)
    }
    
    override suspend fun <T> getCache(key: String): T? {
        val entry = cache[key]
        return if (entry != null && !entry.isExpired()) {
            entry.value as? T
        } else {
            cache.remove(key)
            null
        }
    }
    
    override suspend fun removeCache(key: String) {
        cache.remove(key)
    }
    
    override suspend fun clearCache() {
        cache.clear()
    }
    
    override suspend fun clearExpiredCache() {
        val expiredKeys = cache.filter { it.value.isExpired() }.keys
        expiredKeys.forEach { cache.remove(it) }
    }
    
    override suspend fun syncToCloud() {
        // HarmonyOS云同步实现
        try {
            // 模拟华为云同步
            val syncData = preferences.toMap()
            // 实际实现中会调用HarmonyOS云服务API
            println("Syncing ${syncData.size} items to Huawei Cloud")
        } catch (e: Exception) {
            throw DataException("Cloud sync failed: ${e.message}")
        }
    }
    
    override suspend fun syncFromCloud() {
        // HarmonyOS云同步实现
        try {
            // 模拟从华为云同步
            // 实际实现中会调用HarmonyOS云服务API
            println("Syncing data from Huawei Cloud")
        } catch (e: Exception) {
            throw DataException("Cloud sync failed: ${e.message}")
        }
    }
    
    private fun notifyDataChange(key: String, value: Any?) {
        val flow = dataChangeFlows.getOrPut(key) {
            MutableStateFlow(value)
        }
        flow.value = value
    }
    
    private suspend fun syncToDistributedStorage(key: String, value: Any) {
        try {
            // HarmonyOS分布式数据同步
            distributedStorage[key] = value
            
            // 实际实现中会使用HarmonyOS分布式数据管理API
            // 如DistributedKVStore等
            println("Syncing $key to distributed storage")
        } catch (e: Exception) {
            // 分布式同步失败不影响本地存储
            println("Distributed sync failed for $key: ${e.message}")
        }
    }
    
    private data class CacheEntry(
        val value: Any,
        val expiryTime: Long
    ) {
        fun isExpired(): Boolean = System.currentTimeMillis() > expiryTime
    }
}

actual object UnifyDataManagerFactory {
    actual fun create(): UnifyDataManager {
        return UnifyDataManagerImpl()
    }
}
