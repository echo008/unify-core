package com.unify.core.data

import com.unify.core.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * 小程序平台UnifyDataManager实现
 * 基于小程序Storage API和云开发数据库
 */
class UnifyDataManagerImpl : UnifyDataManager {
    
    // 数据变化监听
    private val dataChangeFlows = mutableMapOf<String, MutableStateFlow<Any?>>()
    
    // 缓存管理
    private val cache = mutableMapOf<String, CacheEntry>()
    
    // 小程序存储管理器
    private val miniAppStorage = MiniAppStorageManager()
    
    override suspend fun putString(key: String, value: String) {
        miniAppStorage.setStorageSync(key, value)
        notifyDataChange(key, value)
    }
    
    override suspend fun getString(key: String, defaultValue: String): String {
        return miniAppStorage.getStorageSync(key) as? String ?: defaultValue
    }
    
    override suspend fun putInt(key: String, value: Int) {
        miniAppStorage.setStorageSync(key, value)
        notifyDataChange(key, value)
    }
    
    override suspend fun getInt(key: String, defaultValue: Int): Int {
        return miniAppStorage.getStorageSync(key) as? Int ?: defaultValue
    }
    
    override suspend fun putLong(key: String, value: Long) {
        miniAppStorage.setStorageSync(key, value)
        notifyDataChange(key, value)
    }
    
    override suspend fun getLong(key: String, defaultValue: Long): Long {
        return miniAppStorage.getStorageSync(key) as? Long ?: defaultValue
    }
    
    override suspend fun putFloat(key: String, value: Float) {
        miniAppStorage.setStorageSync(key, value)
        notifyDataChange(key, value)
    }
    
    override suspend fun getFloat(key: String, defaultValue: Float): Float {
        return miniAppStorage.getStorageSync(key) as? Float ?: defaultValue
    }
    
    override suspend fun putBoolean(key: String, value: Boolean) {
        miniAppStorage.setStorageSync(key, value)
        notifyDataChange(key, value)
    }
    
    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return miniAppStorage.getStorageSync(key) as? Boolean ?: defaultValue
    }
    
    override suspend fun <T> putObject(key: String, value: T) {
        try {
            val jsonString = Json.encodeToString(value)
            miniAppStorage.setStorageSync(key, jsonString)
            notifyDataChange(key, value)
        } catch (e: Exception) {
            throw DataException("Failed to serialize object: ${e.message}")
        }
    }
    
    override suspend fun <T> getObject(key: String, defaultValue: T): T {
        return try {
            val jsonString = miniAppStorage.getStorageSync(key) as? String
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
        miniAppStorage.removeStorageSync(key)
        notifyDataChange(key, null)
    }
    
    override suspend fun clear() {
        miniAppStorage.clearStorageSync()
        dataChangeFlows.values.forEach { flow ->
            flow.value = null
        }
    }
    
    override suspend fun contains(key: String): Boolean {
        return miniAppStorage.getStorageInfoSync().keys.contains(key)
    }
    
    override suspend fun getAllKeys(): Set<String> {
        return miniAppStorage.getStorageInfoSync().keys.toSet()
    }
    
    override fun <T> observeData(key: String): Flow<T?> {
        val flow = dataChangeFlows.getOrPut(key) {
            MutableStateFlow(miniAppStorage.getStorageSync(key))
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
        try {
            // 使用小程序云开发数据库同步
            val syncData = miniAppStorage.getStorageInfoSync()
            miniAppStorage.syncToCloudDatabase(syncData)
        } catch (e: Exception) {
            throw DataException("Cloud sync failed: ${e.message}")
        }
    }
    
    override suspend fun syncFromCloud() {
        try {
            // 从小程序云开发数据库同步
            val cloudData = miniAppStorage.syncFromCloudDatabase()
            cloudData.forEach { (key, value) ->
                miniAppStorage.setStorageSync(key, value)
                notifyDataChange(key, value)
            }
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
    
    private data class CacheEntry(
        val value: Any,
        val expiryTime: Long
    ) {
        fun isExpired(): Boolean = System.currentTimeMillis() > expiryTime
    }
}

// 小程序存储管理器模拟实现
private class MiniAppStorageManager {
    private val storage = mutableMapOf<String, Any>()
    
    fun setStorageSync(key: String, value: Any) {
        storage[key] = value
        // 实际实现中会调用小程序API: wx.setStorageSync(key, value)
    }
    
    fun getStorageSync(key: String): Any? {
        // 实际实现中会调用小程序API: wx.getStorageSync(key)
        return storage[key]
    }
    
    fun removeStorageSync(key: String) {
        storage.remove(key)
        // 实际实现中会调用小程序API: wx.removeStorageSync(key)
    }
    
    fun clearStorageSync() {
        storage.clear()
        // 实际实现中会调用小程序API: wx.clearStorageSync()
    }
    
    fun getStorageInfoSync(): StorageInfo {
        // 实际实现中会调用小程序API: wx.getStorageInfoSync()
        return StorageInfo(
            keys = storage.keys.toList(),
            currentSize = storage.size * 100, // 模拟大小
            limitSize = 10240 // 10MB限制
        )
    }
    
    suspend fun syncToCloudDatabase(data: StorageInfo) {
        // 实际实现中会调用小程序云开发API
        // 如: wx.cloud.database().collection('user_data').add({data: data})
        println("Syncing ${data.keys.size} items to cloud database")
    }
    
    suspend fun syncFromCloudDatabase(): Map<String, Any> {
        // 实际实现中会调用小程序云开发API
        // 如: wx.cloud.database().collection('user_data').get()
        return emptyMap()
    }
    
    data class StorageInfo(
        val keys: List<String>,
        val currentSize: Int,
        val limitSize: Int
    )
}

actual object UnifyDataManagerFactory {
    actual fun create(): UnifyDataManager {
        return UnifyDataManagerImpl()
    }
}
