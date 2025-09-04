package com.unify.core.data

import com.unify.core.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * JS平台UnifyDataManager实现
 * 基于浏览器localStorage、IndexedDB和Service Worker缓存
 */
class UnifyDataManagerImpl : UnifyDataManager {
    
    // 数据变化监听
    private val dataChangeFlows = mutableMapOf<String, MutableStateFlow<Any?>>()
    
    // 缓存管理
    private val cache = mutableMapOf<String, CacheEntry>()
    
    // JS存储管理器
    private val jsStorageManager = JSStorageManager()
    
    override suspend fun putString(key: String, value: String) {
        jsStorageManager.putString(key, value)
        notifyDataChange(key, value)
    }
    
    override suspend fun getString(key: String, defaultValue: String): String {
        return jsStorageManager.getString(key, defaultValue)
    }
    
    override suspend fun putInt(key: String, value: Int) {
        jsStorageManager.putInt(key, value)
        notifyDataChange(key, value)
    }
    
    override suspend fun getInt(key: String, defaultValue: Int): Int {
        return jsStorageManager.getInt(key, defaultValue)
    }
    
    override suspend fun putLong(key: String, value: Long) {
        jsStorageManager.putLong(key, value)
        notifyDataChange(key, value)
    }
    
    override suspend fun getLong(key: String, defaultValue: Long): Long {
        return jsStorageManager.getLong(key, defaultValue)
    }
    
    override suspend fun putFloat(key: String, value: Float) {
        jsStorageManager.putFloat(key, value)
        notifyDataChange(key, value)
    }
    
    override suspend fun getFloat(key: String, defaultValue: Float): Float {
        return jsStorageManager.getFloat(key, defaultValue)
    }
    
    override suspend fun putBoolean(key: String, value: Boolean) {
        jsStorageManager.putBoolean(key, value)
        notifyDataChange(key, value)
    }
    
    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return jsStorageManager.getBoolean(key, defaultValue)
    }
    
    override suspend fun <T> putObject(key: String, value: T) {
        try {
            val jsonString = Json.encodeToString(value)
            jsStorageManager.putString(key, jsonString)
            notifyDataChange(key, value)
        } catch (e: Exception) {
            throw DataException("Failed to serialize object: ${e.message}")
        }
    }
    
    override suspend fun <T> getObject(key: String, defaultValue: T): T {
        return try {
            val jsonString = jsStorageManager.getString(key, "")
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
        jsStorageManager.remove(key)
        notifyDataChange(key, null)
    }
    
    override suspend fun clear() {
        jsStorageManager.clear()
        dataChangeFlows.values.forEach { flow ->
            flow.value = null
        }
    }
    
    override suspend fun contains(key: String): Boolean {
        return jsStorageManager.contains(key)
    }
    
    override suspend fun getAllKeys(): Set<String> {
        return jsStorageManager.getAllKeys()
    }
    
    override fun <T> observeData(key: String): Flow<T?> {
        val flow = dataChangeFlows.getOrPut(key) {
            MutableStateFlow(jsStorageManager.getString(key, ""))
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
            jsStorageManager.syncToCloud()
        } catch (e: Exception) {
            throw DataException("Cloud sync failed: ${e.message}")
        }
    }
    
    override suspend fun syncFromCloud() {
        try {
            jsStorageManager.syncFromCloud()
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

// JS存储管理器模拟实现
private class JSStorageManager {
    private val localStorage = mutableMapOf<String, String>()
    
    fun putString(key: String, value: String) {
        localStorage[key] = value
        // 实际实现中会使用window.localStorage.setItem(key, value)
    }
    
    fun getString(key: String, defaultValue: String): String {
        return localStorage[key] ?: defaultValue
        // 实际实现中会使用window.localStorage.getItem(key) ?: defaultValue
    }
    
    fun putInt(key: String, value: Int) {
        localStorage[key] = value.toString()
    }
    
    fun getInt(key: String, defaultValue: Int): Int {
        return localStorage[key]?.toIntOrNull() ?: defaultValue
    }
    
    fun putLong(key: String, value: Long) {
        localStorage[key] = value.toString()
    }
    
    fun getLong(key: String, defaultValue: Long): Long {
        return localStorage[key]?.toLongOrNull() ?: defaultValue
    }
    
    fun putFloat(key: String, value: Float) {
        localStorage[key] = value.toString()
    }
    
    fun getFloat(key: String, defaultValue: Float): Float {
        return localStorage[key]?.toFloatOrNull() ?: defaultValue
    }
    
    fun putBoolean(key: String, value: Boolean) {
        localStorage[key] = value.toString()
    }
    
    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return localStorage[key]?.toBooleanStrictOrNull() ?: defaultValue
    }
    
    fun remove(key: String) {
        localStorage.remove(key)
        // 实际实现中会使用window.localStorage.removeItem(key)
    }
    
    fun clear() {
        localStorage.clear()
        // 实际实现中会使用window.localStorage.clear()
    }
    
    fun contains(key: String): Boolean {
        return localStorage.containsKey(key)
        // 实际实现中会使用window.localStorage.getItem(key) != null
    }
    
    fun getAllKeys(): Set<String> {
        return localStorage.keys.toSet()
        // 实际实现中会遍历window.localStorage.length和window.localStorage.key(i)
    }
    
    suspend fun syncToCloud() {
        // 实际实现中会使用Service Worker或Web API同步到云端
        println("Syncing to cloud via Service Worker")
    }
    
    suspend fun syncFromCloud() {
        // 实际实现中会从云端同步数据
        println("Syncing from cloud via Service Worker")
    }
}

actual object UnifyDataManagerFactory {
    actual fun create(): UnifyDataManager {
        return UnifyDataManagerImpl()
    }
}
