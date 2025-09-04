package com.unify.core.data

import com.unify.core.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Watch平台UnifyDataManager实现
 * 基于WatchOS/WearOS存储系统和健康数据同步
 */
class UnifyDataManagerImpl : UnifyDataManager {
    
    // 数据变化监听
    private val dataChangeFlows = mutableMapOf<String, MutableStateFlow<Any?>>()
    
    // 缓存管理
    private val cache = mutableMapOf<String, CacheEntry>()
    
    // Watch存储管理器
    private val watchStorageManager = WatchStorageManager()
    
    override suspend fun putString(key: String, value: String) {
        watchStorageManager.putString(key, value)
        notifyDataChange(key, value)
        
        // Watch数据同步到手机
        syncToCompanionDevice(key, value)
    }
    
    override suspend fun getString(key: String, defaultValue: String): String {
        return watchStorageManager.getString(key, defaultValue)
    }
    
    override suspend fun putInt(key: String, value: Int) {
        watchStorageManager.putInt(key, value)
        notifyDataChange(key, value)
        syncToCompanionDevice(key, value)
    }
    
    override suspend fun getInt(key: String, defaultValue: Int): Int {
        return watchStorageManager.getInt(key, defaultValue)
    }
    
    override suspend fun putLong(key: String, value: Long) {
        watchStorageManager.putLong(key, value)
        notifyDataChange(key, value)
        syncToCompanionDevice(key, value)
    }
    
    override suspend fun getLong(key: String, defaultValue: Long): Long {
        return watchStorageManager.getLong(key, defaultValue)
    }
    
    override suspend fun putFloat(key: String, value: Float) {
        watchStorageManager.putFloat(key, value)
        notifyDataChange(key, value)
        syncToCompanionDevice(key, value)
    }
    
    override suspend fun getFloat(key: String, defaultValue: Float): Float {
        return watchStorageManager.getFloat(key, defaultValue)
    }
    
    override suspend fun putBoolean(key: String, value: Boolean) {
        watchStorageManager.putBoolean(key, value)
        notifyDataChange(key, value)
        syncToCompanionDevice(key, value)
    }
    
    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return watchStorageManager.getBoolean(key, defaultValue)
    }
    
    override suspend fun <T> putObject(key: String, value: T) {
        try {
            val jsonString = Json.encodeToString(value)
            watchStorageManager.putString(key, jsonString)
            notifyDataChange(key, value)
            syncToCompanionDevice(key, jsonString)
        } catch (e: Exception) {
            throw DataException("Failed to serialize object: ${e.message}")
        }
    }
    
    override suspend fun <T> getObject(key: String, defaultValue: T): T {
        return try {
            val jsonString = watchStorageManager.getString(key, "")
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
        watchStorageManager.remove(key)
        notifyDataChange(key, null)
        syncRemovalToCompanionDevice(key)
    }
    
    override suspend fun clear() {
        watchStorageManager.clear()
        dataChangeFlows.values.forEach { flow ->
            flow.value = null
        }
        syncClearToCompanionDevice()
    }
    
    override suspend fun contains(key: String): Boolean {
        return watchStorageManager.contains(key)
    }
    
    override suspend fun getAllKeys(): Set<String> {
        return watchStorageManager.getAllKeys()
    }
    
    override fun <T> observeData(key: String): Flow<T?> {
        val flow = dataChangeFlows.getOrPut(key) {
            MutableStateFlow(watchStorageManager.getString(key, ""))
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
            // Watch云同步实现
            val syncData = watchStorageManager.getAllData()
            watchStorageManager.syncToHealthCloud(syncData)
        } catch (e: Exception) {
            throw DataException("Cloud sync failed: ${e.message}")
        }
    }
    
    override suspend fun syncFromCloud() {
        try {
            // Watch云同步实现
            val cloudData = watchStorageManager.syncFromHealthCloud()
            cloudData.forEach { (key, value) ->
                watchStorageManager.putString(key, value.toString())
                notifyDataChange(key, value)
            }
        } catch (e: Exception) {
            throw DataException("Cloud sync failed: ${e.message}")
        }
    }
    
    // Watch特有功能
    suspend fun syncHealthData(healthData: Map<String, Any>) {
        try {
            watchStorageManager.syncHealthData(healthData)
        } catch (e: Exception) {
            throw DataException("Health data sync failed: ${e.message}")
        }
    }
    
    suspend fun getHealthData(): Map<String, Any> {
        return try {
            watchStorageManager.getHealthData()
        } catch (e: Exception) {
            emptyMap()
        }
    }
    
    private fun notifyDataChange(key: String, value: Any?) {
        val flow = dataChangeFlows.getOrPut(key) {
            MutableStateFlow(value)
        }
        flow.value = value
    }
    
    private suspend fun syncToCompanionDevice(key: String, value: Any) {
        try {
            watchStorageManager.syncToCompanion(key, value)
        } catch (e: Exception) {
            // 同步失败不影响本地存储
            println("Companion sync failed for $key: ${e.message}")
        }
    }
    
    private suspend fun syncRemovalToCompanionDevice(key: String) {
        try {
            watchStorageManager.syncRemovalToCompanion(key)
        } catch (e: Exception) {
            println("Companion removal sync failed for $key: ${e.message}")
        }
    }
    
    private suspend fun syncClearToCompanionDevice() {
        try {
            watchStorageManager.syncClearToCompanion()
        } catch (e: Exception) {
            println("Companion clear sync failed: ${e.message}")
        }
    }
    
    private data class CacheEntry(
        val value: Any,
        val expiryTime: Long
    ) {
        fun isExpired(): Boolean = System.currentTimeMillis() > expiryTime
    }
}

// Watch存储管理器模拟实现
private class WatchStorageManager {
    private val storage = mutableMapOf<String, Any>()
    private val healthData = mutableMapOf<String, Any>()
    
    fun putString(key: String, value: String) {
        storage[key] = value
        // 实际实现中会使用WatchOS NSUserDefaults或WearOS SharedPreferences
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
    
    suspend fun syncToHealthCloud(data: Map<String, Any>) {
        // Watch健康云同步实现
        println("Syncing ${data.size} items to health cloud")
    }
    
    suspend fun syncFromHealthCloud(): Map<String, Any> {
        // Watch健康云同步实现
        return emptyMap()
    }
    
    suspend fun syncHealthData(data: Map<String, Any>) {
        healthData.putAll(data)
        // 实际实现中会使用HealthKit或Google Fit API
    }
    
    suspend fun getHealthData(): Map<String, Any> {
        return healthData.toMap()
    }
    
    suspend fun syncToCompanion(key: String, value: Any) {
        // 实际实现中会使用WatchConnectivity或Wear OS Data Layer API
        println("Syncing $key to companion device")
    }
    
    suspend fun syncRemovalToCompanion(key: String) {
        println("Syncing removal of $key to companion device")
    }
    
    suspend fun syncClearToCompanion() {
        println("Syncing clear to companion device")
    }
}

actual object UnifyDataManagerFactory {
    actual fun create(): UnifyDataManager {
        return UnifyDataManagerImpl()
    }
}
