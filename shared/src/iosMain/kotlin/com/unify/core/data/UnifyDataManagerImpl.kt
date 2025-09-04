package com.unify.core.data

import com.unify.core.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * iOS平台UnifyDataManager实现
 * 基于iOS NSUserDefaults、Core Data和iCloud同步
 */
class UnifyDataManagerImpl : UnifyDataManager {
    
    // 数据变化监听
    private val dataChangeFlows = mutableMapOf<String, MutableStateFlow<Any?>>()
    
    // 缓存管理
    private val cache = mutableMapOf<String, CacheEntry>()
    
    // iOS存储管理器
    private val iosStorageManager = IOSStorageManager()
    
    override suspend fun putString(key: String, value: String) {
        iosStorageManager.putString(key, value)
        notifyDataChange(key, value)
    }
    
    override suspend fun getString(key: String, defaultValue: String): String {
        return iosStorageManager.getString(key, defaultValue)
    }
    
    override suspend fun putInt(key: String, value: Int) {
        iosStorageManager.putInt(key, value)
        notifyDataChange(key, value)
    }
    
    override suspend fun getInt(key: String, defaultValue: Int): Int {
        return iosStorageManager.getInt(key, defaultValue)
    }
    
    override suspend fun putLong(key: String, value: Long) {
        iosStorageManager.putLong(key, value)
        notifyDataChange(key, value)
    }
    
    override suspend fun getLong(key: String, defaultValue: Long): Long {
        return iosStorageManager.getLong(key, defaultValue)
    }
    
    override suspend fun putFloat(key: String, value: Float) {
        iosStorageManager.putFloat(key, value)
        notifyDataChange(key, value)
    }
    
    override suspend fun getFloat(key: String, defaultValue: Float): Float {
        return iosStorageManager.getFloat(key, defaultValue)
    }
    
    override suspend fun putBoolean(key: String, value: Boolean) {
        iosStorageManager.putBoolean(key, value)
        notifyDataChange(key, value)
    }
    
    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return iosStorageManager.getBoolean(key, defaultValue)
    }
    
    override suspend fun <T> putObject(key: String, value: T) {
        try {
            val jsonString = Json.encodeToString(value)
            iosStorageManager.putString(key, jsonString)
            notifyDataChange(key, value)
        } catch (e: Exception) {
            throw DataException("Failed to serialize object: ${e.message}")
        }
    }
    
    override suspend fun <T> getObject(key: String, defaultValue: T): T {
        return try {
            val jsonString = iosStorageManager.getString(key, "")
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
        iosStorageManager.remove(key)
        notifyDataChange(key, null)
    }
    
    override suspend fun clear() {
        iosStorageManager.clear()
        dataChangeFlows.values.forEach { flow ->
            flow.value = null
        }
    }
    
    override suspend fun contains(key: String): Boolean {
        return iosStorageManager.contains(key)
    }
    
    override suspend fun getAllKeys(): Set<String> {
        return iosStorageManager.getAllKeys()
    }
    
    override fun <T> observeData(key: String): Flow<T?> {
        val flow = dataChangeFlows.getOrPut(key) {
            MutableStateFlow(iosStorageManager.getString(key, ""))
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
            iosStorageManager.syncToiCloud()
        } catch (e: Exception) {
            throw DataException("iCloud sync failed: ${e.message}")
        }
    }
    
    override suspend fun syncFromCloud() {
        try {
            iosStorageManager.syncFromiCloud()
        } catch (e: Exception) {
            throw DataException("iCloud sync failed: ${e.message}")
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

// iOS存储管理器模拟实现
private class IOSStorageManager {
    private val userDefaults = mutableMapOf<String, Any>()
    
    fun putString(key: String, value: String) {
        userDefaults[key] = value
        // 实际实现中会使用NSUserDefaults
    }
    
    fun getString(key: String, defaultValue: String): String {
        return userDefaults[key] as? String ?: defaultValue
    }
    
    fun putInt(key: String, value: Int) {
        userDefaults[key] = value
    }
    
    fun getInt(key: String, defaultValue: Int): Int {
        return userDefaults[key] as? Int ?: defaultValue
    }
    
    fun putLong(key: String, value: Long) {
        userDefaults[key] = value
    }
    
    fun getLong(key: String, defaultValue: Long): Long {
        return userDefaults[key] as? Long ?: defaultValue
    }
    
    fun putFloat(key: String, value: Float) {
        userDefaults[key] = value
    }
    
    fun getFloat(key: String, defaultValue: Float): Float {
        return userDefaults[key] as? Float ?: defaultValue
    }
    
    fun putBoolean(key: String, value: Boolean) {
        userDefaults[key] = value
    }
    
    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return userDefaults[key] as? Boolean ?: defaultValue
    }
    
    fun remove(key: String) {
        userDefaults.remove(key)
    }
    
    fun clear() {
        userDefaults.clear()
    }
    
    fun contains(key: String): Boolean {
        return userDefaults.containsKey(key)
    }
    
    fun getAllKeys(): Set<String> {
        return userDefaults.keys.toSet()
    }
    
    suspend fun syncToiCloud() {
        // 实际实现中会使用NSUbiquitousKeyValueStore
        println("Syncing to iCloud")
    }
    
    suspend fun syncFromiCloud() {
        // 实际实现中会从iCloud同步数据
        println("Syncing from iCloud")
    }
}

actual object UnifyDataManagerFactory {
    actual fun create(): UnifyDataManager {
        return UnifyDataManagerImpl()
    }
}
