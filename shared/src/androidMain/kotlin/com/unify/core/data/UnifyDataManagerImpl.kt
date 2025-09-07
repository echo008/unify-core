package com.unify.core.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.lang.reflect.Type

/**
 * Android平台UnifyDataManager实现
 * 基于SharedPreferences和内存缓存
 */
actual class UnifyDataManagerImpl : UnifyDataManager {
    private var context: Context? = null
    private var sharedPreferences: SharedPreferences? = null
    private val gson = Gson()
    private val mutex = Mutex()
    
    actual constructor() {
        // Default constructor for expect/actual
    }
    
    fun initialize(context: Context) {
        this.context = context
        this.sharedPreferences = context.getSharedPreferences(
            "unify_data_manager", Context.MODE_PRIVATE
        )
    }
    
    private fun ensureInitialized() {
        if (context == null || sharedPreferences == null) {
            throw IllegalStateException("UnifyDataManagerImpl not initialized. Call initialize(context) first.")
        }
    }
    
    // 响应式数据流管理
    private val dataFlows = mutableMapOf<String, MutableStateFlow<Any?>>()
    
    // 缓存过期时间管理
    private val cacheExpiryMap = mutableMapOf<String, Long>()
    
    // 同步设置
    private var syncEnabled = false
    
    override suspend fun getString(key: String, defaultValue: String?): String? = mutex.withLock {
        ensureInitialized()
        if (isCacheExpired(key)) {
            remove(key)
            return defaultValue
        }
        sharedPreferences!!.getString(key, defaultValue)
    }
    
    override suspend fun setString(key: String, value: String) = mutex.withLock {
        ensureInitialized()
        sharedPreferences!!.edit().putString(key, value).apply()
        updateDataFlow(key, value)
    }
    
    override suspend fun getInt(key: String, defaultValue: Int): Int = mutex.withLock {
        ensureInitialized()
        if (isCacheExpired(key)) {
            remove(key)
            return defaultValue
        }
        sharedPreferences!!.getInt(key, defaultValue)
    }
    
    override suspend fun setInt(key: String, value: Int) = mutex.withLock {
        ensureInitialized()
        sharedPreferences!!.edit().putInt(key, value).apply()
        updateDataFlow(key, value)
    }
    
    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean = mutex.withLock {
        ensureInitialized()
        if (isCacheExpired(key)) {
            remove(key)
            return defaultValue
        }
        sharedPreferences!!.getBoolean(key, defaultValue)
    }
    
    override suspend fun setBoolean(key: String, value: Boolean) = mutex.withLock {
        ensureInitialized()
        sharedPreferences!!.edit().putBoolean(key, value).apply()
        updateDataFlow(key, value)
    }
    
    override suspend fun getLong(key: String, defaultValue: Long): Long = mutex.withLock {
        ensureInitialized()
        if (isCacheExpired(key)) {
            remove(key)
            return defaultValue
        }
        sharedPreferences!!.getLong(key, defaultValue)
    }
    
    override suspend fun setLong(key: String, value: Long) = mutex.withLock {
        ensureInitialized()
        sharedPreferences!!.edit().putLong(key, value).apply()
        updateDataFlow(key, value)
    }
    
    override suspend fun getFloat(key: String, defaultValue: Float): Float = mutex.withLock {
        ensureInitialized()
        if (isCacheExpired(key)) {
            remove(key)
            return defaultValue
        }
        sharedPreferences!!.getFloat(key, defaultValue)
    }
    
    override suspend fun setFloat(key: String, value: Float) = mutex.withLock {
        ensureInitialized()
        sharedPreferences!!.edit().putFloat(key, value).apply()
        updateDataFlow(key, value)
    }
    
    override suspend fun <T : Any> getObject(key: String, clazz: kotlin.reflect.KClass<T>): T? = mutex.withLock {
        ensureInitialized()
        if (isCacheExpired(key)) {
            remove(key)
            return null
        }
        val json = sharedPreferences!!.getString(key, null) ?: return null
        try {
            gson.fromJson(json, clazz.java)
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun <T> setObject(key: String, value: T) = mutex.withLock {
        ensureInitialized()
        val json = gson.toJson(value)
        sharedPreferences!!.edit().putString(key, json).apply()
        updateDataFlow(key, value)
    }
    
    override suspend fun clear() = mutex.withLock {
        ensureInitialized()
        sharedPreferences!!.edit().clear().apply()
        cacheExpiryMap.clear()
        dataFlows.values.forEach { it.value = null }
    }
    
    override suspend fun remove(key: String) = mutex.withLock {
        ensureInitialized()
        sharedPreferences!!.edit().remove(key).apply()
        cacheExpiryMap.remove(key)
        updateDataFlow(key, null)
    }
    
    override suspend fun contains(key: String): Boolean = mutex.withLock {
        ensureInitialized()
        if (isCacheExpired(key)) {
            remove(key)
            return false
        }
        sharedPreferences!!.contains(key)
    }
    
    override suspend fun getAllKeys(): Set<String> = mutex.withLock {
        ensureInitialized()
        val allKeys = sharedPreferences!!.all.keys
        // 过滤掉过期的键
        allKeys.filter { !isCacheExpired(it) }.toSet()
    }
    
    override fun <T : Any> observeKey(key: String, clazz: kotlin.reflect.KClass<T>): Flow<T?> {
        return getOrCreateDataFlow(key).map { value ->
            when {
                value == null -> null
                clazz.java.isInstance(value) -> clazz.java.cast(value)
                value is String -> {
                    try {
                        gson.fromJson(value, clazz.java)
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
        ensureInitialized()
        val currentTime = System.currentTimeMillis()
        val expiredKeys = cacheExpiryMap.filter { (_, expiryTime) ->
            currentTime > expiryTime
        }.keys
        
        expiredKeys.forEach { key ->
            sharedPreferences!!.edit().remove(key).apply()
            cacheExpiryMap.remove(key)
            updateDataFlow(key, null)
        }
    }
    
    override suspend fun syncToCloud() {
        if (!syncEnabled) return
        
        try {
            // 收集所有数据进行云端同步
            ensureInitialized()
            val allData = sharedPreferences!!.all
            val syncData = mutableMapOf<String, Any?>()
            
            allData.forEach { (key, value) ->
                if (!isCacheExpired(key)) {
                    syncData[key] = value
                }
            }
            
            // 将同步数据保存到应用私有存储（模拟云端存储）
            val syncJson = gson.toJson(syncData)
            val syncFile = context!!.getFileStreamPath("cloud_sync_backup.json")
            syncFile.writeText(syncJson)
            
        } catch (e: Exception) {
            android.util.Log.e("UnifyDataManager", "Cloud sync failed: ${e.message}")
        }
    }
    
    override suspend fun syncFromCloud() {
        if (!syncEnabled) return
        
        try {
            // 从应用私有存储读取同步数据（模拟从云端获取）
            ensureInitialized()
            val syncFile = context!!.getFileStreamPath("cloud_sync_backup.json")
            if (!syncFile.exists()) return
            
            val syncJson = syncFile.readText()
            val type: Type = object : TypeToken<Map<String, Any>>() {}.type
            val syncData: Map<String, Any> = gson.fromJson(syncJson, type)
            
            val editor = sharedPreferences!!.edit()
            syncData.forEach { (key, value) ->
                when (value) {
                    is String -> {
                        editor.putString(key, value)
                        updateDataFlow(key, value)
                    }
                    is Int -> {
                        editor.putInt(key, value)
                        updateDataFlow(key, value)
                    }
                    is Boolean -> {
                        editor.putBoolean(key, value)
                        updateDataFlow(key, value)
                    }
                    is Long -> {
                        editor.putLong(key, value)
                        updateDataFlow(key, value)
                    }
                    is Float -> {
                        editor.putFloat(key, value)
                        updateDataFlow(key, value)
                    }
                    else -> {
                        // 处理其他类型，转换为JSON字符串
                        val jsonValue = gson.toJson(value)
                        editor.putString(key, jsonValue)
                        updateDataFlow(key, value)
                    }
                }
            }
            editor.apply()
            
        } catch (e: Exception) {
            android.util.Log.e("UnifyDataManager", "Cloud sync from failed: ${e.message}")
        }
    }
    
    override fun isSyncEnabled(): Boolean = syncEnabled
    
    override fun setSyncEnabled(enabled: Boolean) {
        syncEnabled = enabled
    }
    
    private fun getOrCreateDataFlow(key: String): Flow<Any?> {
        return dataFlows.getOrPut(key) {
            MutableStateFlow(sharedPreferences?.all?.get(key))
        }.asStateFlow()
    }
    
    private fun updateDataFlow(key: String, value: Any?) {
        dataFlows.getOrPut(key) { MutableStateFlow(null) }.value = value
    }
}

actual object UnifyDataManagerFactory {
    private var context: Context? = null
    
    fun initialize(context: Context) {
        this.context = context.applicationContext
    }
    
    actual fun create(): UnifyDataManager {
        val manager = UnifyDataManagerImpl()
        val ctx = context ?: throw IllegalStateException("UnifyDataManagerFactory not initialized. Call initialize(context) first.")
        manager.initialize(ctx)
        return manager
    }
}
