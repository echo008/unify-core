package com.unify.core.data

import com.unify.core.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * TV平台UnifyDataManager实现
 * 基于Android TV SharedPreferences和文件系统
 */
class UnifyDataManagerImpl : UnifyDataManager {
    
    // 数据变化监听
    private val dataChangeFlows = mutableMapOf<String, MutableStateFlow<Any?>>()
    
    // 缓存管理
    private val cache = mutableMapOf<String, CacheEntry>()
    
    // TV存储管理器
    private val tvStorageManager = TVStorageManager()
    
    override suspend fun putString(key: String, value: String) {
        tvStorageManager.putString(key, value)
        notifyDataChange(key, value)
    }
    
    override suspend fun getString(key: String, defaultValue: String): String {
        return tvStorageManager.getString(key, defaultValue)
    }
    
    override suspend fun putInt(key: String, value: Int) {
        tvStorageManager.putInt(key, value)
        notifyDataChange(key, value)
    }
    
    override suspend fun getInt(key: String, defaultValue: Int): Int {
        return tvStorageManager.getInt(key, defaultValue)
    }
    
    override suspend fun putLong(key: String, value: Long) {
        tvStorageManager.putLong(key, value)
        notifyDataChange(key, value)
    }
    
    override suspend fun getLong(key: String, defaultValue: Long): Long {
        return tvStorageManager.getLong(key, defaultValue)
    }
    
    override suspend fun putFloat(key: String, value: Float) {
        tvStorageManager.putFloat(key, value)
        notifyDataChange(key, value)
    }
    
    override suspend fun getFloat(key: String, defaultValue: Float): Float {
        return tvStorageManager.getFloat(key, defaultValue)
    }
    
    override suspend fun putBoolean(key: String, value: Boolean) {
        tvStorageManager.putBoolean(key, value)
        notifyDataChange(key, value)
    }
    
    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return tvStorageManager.getBoolean(key, defaultValue)
    }
    
    override suspend fun <T> putObject(key: String, value: T) {
        try {
            val jsonString = Json.encodeToString(value)
            tvStorageManager.putString(key, jsonString)
            notifyDataChange(key, value)
        } catch (e: Exception) {
            throw DataException("Failed to serialize object: ${e.message}")
        }
    }
    
    override suspend fun <T> getObject(key: String, defaultValue: T): T {
        return try {
            val jsonString = tvStorageManager.getString(key, "")
            if (jsonString.isNotEmpty()) {
                Json.decodeFromString<T>(jsonString)
            } else {
                defaultValue
            }
        } catch (e: Exception) {
            defaultValue
        }
    }
    
    override suspend fun remove(key: String) {
        tvStorageManager.remove(key)
        notifyDataChange(key, null)
    }
    
    override suspend fun clear() {
        tvStorageManager.clear()
        dataChangeFlows.values.forEach { flow ->
            flow.value = null
        }
    }
    
    override suspend fun contains(key: String): Boolean {
        return tvStorageManager.contains(key)
    }
    
    override suspend fun getAllKeys(): Set<String> {
        return tvStorageManager.getAllKeys()
    }
    
    override fun <T> observeData(key: String): Flow<T?> {
        val flow = dataChangeFlows.getOrPut(key) {
            MutableStateFlow(tvStorageManager.getString(key, ""))
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
            // TV平台云同步实现
            val syncData = tvStorageManager.getAllData()
            tvStorageManager.syncToCloud(syncData)
        } catch (e: Exception) {
            throw DataException("Cloud sync failed: ${e.message}")
        }
    }
    
    override suspend fun syncFromCloud() {
        try {
            // TV平台云同步实现
            val cloudData = tvStorageManager.syncFromCloud()
            cloudData.forEach { (key, value) ->
                tvStorageManager.putString(key, value.toString())
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

// TV存储管理器模拟实现
private class TVStorageManager {
    private val storage = mutableMapOf<String, Any>()
    
    fun putString(key: String, value: String) {
        storage[key] = value
        // 实际实现中会使用Android TV的SharedPreferences
    }
    
    fun getString(key: String, defaultValue: String): String {
        return storage[key] as? String ?: defaultValue
    }
    
    fun putInt(key: String, value: Int) {
        storage[key] = value
    }
    
    fun getInt(key: String, defaultValue: Int): Int {
        return storage[key] as? Int ?: defaultValue
    }
    
    fun putLong(key: String, value: Long) {
        storage[key] = value
    }
    
    fun getLong(key: String, defaultValue: Long): Long {
        return storage[key] as? Long ?: defaultValue
    }
    
    fun putFloat(key: String, value: Float) {
        storage[key] = value
    }
    
    fun getFloat(key: String, defaultValue: Float): Float {
        return storage[key] as? Float ?: defaultValue
    }
    
    fun putBoolean(key: String, value: Boolean) {
        storage[key] = value
    }
    
    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return storage[key] as? Boolean ?: defaultValue
    }
    
    fun remove(key: String) {
        storage.remove(key)
    }
    
    fun clear() {
        storage.clear()
    }
    
    fun contains(key: String): Boolean {
        return storage.containsKey(key)
    }
    
    fun getAllKeys(): Set<String> {
        return storage.keys.toSet()
    }
    
    fun getAllData(): Map<String, Any> {
        return storage.toMap()
    }
    
    suspend fun syncToCloud(data: Map<String, Any>) {
        // TV平台云同步实现
        println("Syncing ${data.size} items to TV cloud storage")
    }
    
    suspend fun syncFromCloud(): Map<String, Any> {
        // TV平台云同步实现
        return emptyMap()
    }
}

actual object UnifyDataManagerFactory {
    actual fun create(): UnifyDataManager {
        return UnifyDataManagerImpl()
    }
}
