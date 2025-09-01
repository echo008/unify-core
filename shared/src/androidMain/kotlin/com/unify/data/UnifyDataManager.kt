package com.unify.data

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

/**
 * Android平台数据管理器实现
 */
class AndroidUnifyDataManager(
    private val context: Context,
    private val config: UnifyDataManagerConfig
) : UnifyDataManager {
    
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "unify_data")
    
    override val storage: UnifyStorage = AndroidUnifyStorage(context, config)
    override val state: UnifyStateManager = AndroidUnifyStateManager()
    override val cache: UnifyCacheManager = AndroidUnifyCacheManager(context, config.cachePolicy)
    override val sync: UnifyDataSync = AndroidUnifyDataSync(config.syncPolicy)
    
    override suspend fun initialize() {
        // 初始化各个组件
        (storage as AndroidUnifyStorage).initialize()
        (cache as AndroidUnifyCacheManager).initialize()
        (sync as AndroidUnifyDataSync).initialize()
    }
    
    override suspend fun cleanup() {
        (cache as AndroidUnifyCacheManager).cleanup()
        (sync as AndroidUnifyDataSync).cleanup()
    }
}

/**
 * Android存储实现
 */
class AndroidUnifyStorage(
    private val context: Context,
    private val config: UnifyDataManagerConfig
) : UnifyStorage {
    
    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences("unify_storage", Context.MODE_PRIVATE)
    }
    
    private val dataStore: DataStore<Preferences> by lazy {
        context.dataStore
    }
    
    private val json = Json { 
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    suspend fun initialize() {
        // 检查存储大小限制
        checkStorageSize()
    }
    
    override suspend fun <T> put(key: String, value: T) {
        try {
            val jsonString = json.encodeToString(value)
            val prefKey = stringPreferencesKey(key)
            
            dataStore.edit { preferences ->
                preferences[prefKey] = if (config.storageEncryption) {
                    encryptData(jsonString)
                } else {
                    jsonString
                }
            }
        } catch (e: Exception) {
            // 降级到SharedPreferences
            val jsonString = json.encodeToString(value)
            sharedPreferences.edit()
                .putString(key, if (config.storageEncryption) encryptData(jsonString) else jsonString)
                .apply()
        }
    }
    
    override suspend fun <T> get(key: String, type: KClass<T>): T? {
        return try {
            val prefKey = stringPreferencesKey(key)
            val jsonString = dataStore.data.first()[prefKey]
            
            jsonString?.let { data ->
                val decryptedData = if (config.storageEncryption) {
                    decryptData(data)
                } else {
                    data
                }
                json.decodeFromString(type.java, decryptedData) as T
            }
        } catch (e: Exception) {
            // 降级到SharedPreferences
            try {
                val jsonString = sharedPreferences.getString(key, null)
                jsonString?.let { data ->
                    val decryptedData = if (config.storageEncryption) {
                        decryptData(data)
                    } else {
                        data
                    }
                    json.decodeFromString(type.java, decryptedData) as T
                }
            } catch (e: Exception) {
                null
            }
        }
    }
    
    override suspend fun remove(key: String): Boolean {
        return try {
            val prefKey = stringPreferencesKey(key)
            dataStore.edit { preferences ->
                preferences.remove(prefKey)
            }
            true
        } catch (e: Exception) {
            sharedPreferences.edit().remove(key).commit()
        }
    }
    
    override suspend fun clear() {
        try {
            dataStore.edit { preferences ->
                preferences.clear()
            }
        } catch (e: Exception) {
            sharedPreferences.edit().clear().apply()
        }
    }
    
    override suspend fun contains(key: String): Boolean {
        return try {
            val prefKey = stringPreferencesKey(key)
            dataStore.data.first().contains(prefKey)
        } catch (e: Exception) {
            sharedPreferences.contains(key)
        }
    }
    
    override suspend fun keys(): Set<String> {
        return try {
            dataStore.data.first().asMap().keys.map { it.name }.toSet()
        } catch (e: Exception) {
            sharedPreferences.all.keys
        }
    }
    
    override suspend fun size(): Long {
        return try {
            val dataDir = File(context.applicationInfo.dataDir, "datastore")
            if (dataDir.exists()) {
                dataDir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
            } else {
                0L
            }
        } catch (e: Exception) {
            0L
        }
    }
    
    private suspend fun checkStorageSize() {
        val currentSize = size()
        if (currentSize > config.maxStorageSize) {
            // 清理旧数据或压缩
            if (config.compressionEnabled) {
                compressOldData()
            }
        }
    }
    
    private suspend fun compressOldData() {
        // 实现数据压缩逻辑
        // 这里可以实现LRU清理或数据压缩
    }
    
    private fun encryptData(data: String): String {
        // 实现加密逻辑，可以使用Android Keystore
        // 这里返回原数据作为占位符
        return data
    }
    
    private fun decryptData(data: String): String {
        // 实现解密逻辑
        // 这里返回原数据作为占位符
        return data
    }
}

/**
 * Android状态管理实现
 */
class AndroidUnifyStateManager : UnifyStateManager {
    
    private val states = ConcurrentHashMap<String, Any?>()
    private val stateFlows = ConcurrentHashMap<String, MutableStateFlow<Any?>>()
    
    override fun <T> setState(key: String, value: T) {
        states[key] = value
        
        val flow = stateFlows.getOrPut(key) { MutableStateFlow(null) }
        flow.value = value
    }
    
    override fun <T> getState(key: String, type: KClass<T>): T? {
        return states[key] as? T
    }
    
    override fun <T> observeState(key: String, type: KClass<T>): Flow<T?> {
        val flow = stateFlows.getOrPut(key) { MutableStateFlow(states[key]) }
        return flow.map { it as? T }
    }
    
    override fun removeState(key: String) {
        states.remove(key)
        stateFlows[key]?.value = null
    }
    
    override fun clearStates() {
        states.clear()
        stateFlows.values.forEach { it.value = null }
        stateFlows.clear()
    }
    
    override fun getStateKeys(): Set<String> {
        return states.keys.toSet()
    }
}

/**
 * Android缓存管理实现
 */
class AndroidUnifyCacheManager(
    private val context: Context,
    private var policy: UnifyCachePolicy
) : UnifyCacheManager {
    
    private val cache = ConcurrentHashMap<String, AndroidCacheEntry>()
    private val stats = AndroidCacheStats()
    
    suspend fun initialize() {
        // 清理过期缓存
        cleanExpiredCache()
    }
    
    suspend fun cleanup() {
        cache.clear()
    }
    
    override suspend fun <T> cache(key: String, value: T, ttl: Long) {
        val actualTtl = if (ttl > 0) ttl else policy.defaultTtl
        val expireTime = if (actualTtl > 0) {
            System.currentTimeMillis() + actualTtl
        } else {
            0L // 永不过期
        }
        
        val entry = AndroidCacheEntry(
            value = value,
            expireTime = expireTime,
            accessTime = System.currentTimeMillis(),
            accessCount = 1
        )
        
        cache[key] = entry
        
        // 检查缓存大小限制
        if (cache.size * 1024 > policy.maxSize) {
            evictCache()
        }
    }
    
    override suspend fun <T> getCache(key: String, type: KClass<T>): T? {
        val entry = cache[key] ?: run {
            stats.missCount++
            return null
        }
        
        // 检查是否过期
        if (entry.expireTime > 0 && System.currentTimeMillis() > entry.expireTime) {
            cache.remove(key)
            stats.missCount++
            return null
        }
        
        // 更新访问信息
        entry.accessTime = System.currentTimeMillis()
        entry.accessCount++
        
        stats.hitCount++
        return entry.value as? T
    }
    
    override suspend fun removeCache(key: String): Boolean {
        return cache.remove(key) != null
    }
    
    override suspend fun clearCache() {
        cache.clear()
        stats.reset()
    }
    
    override suspend fun isCacheValid(key: String): Boolean {
        val entry = cache[key] ?: return false
        return entry.expireTime == 0L || System.currentTimeMillis() <= entry.expireTime
    }
    
    override suspend fun getCacheStats(): UnifyCacheStats {
        return UnifyCacheStats(
            hitCount = stats.hitCount,
            missCount = stats.missCount,
            evictionCount = stats.evictionCount,
            totalSize = cache.size.toLong() * 1024, // 估算
            maxSize = policy.maxSize,
            hitRate = if (stats.hitCount + stats.missCount > 0) {
                stats.hitCount.toDouble() / (stats.hitCount + stats.missCount)
            } else 0.0
        )
    }
    
    override fun setCachePolicy(policy: UnifyCachePolicy) {
        this.policy = policy
    }
    
    private suspend fun cleanExpiredCache() {
        val currentTime = System.currentTimeMillis()
        val expiredKeys = cache.entries
            .filter { it.value.expireTime > 0 && currentTime > it.value.expireTime }
            .map { it.key }
        
        expiredKeys.forEach { cache.remove(it) }
    }
    
    private fun evictCache() {
        when (policy.evictionPolicy) {
            UnifyCacheEvictionPolicy.LRU -> evictLRU()
            UnifyCacheEvictionPolicy.LFU -> evictLFU()
            UnifyCacheEvictionPolicy.FIFO -> evictFIFO()
            UnifyCacheEvictionPolicy.RANDOM -> evictRandom()
        }
    }
    
    private fun evictLRU() {
        val lruEntry = cache.entries.minByOrNull { it.value.accessTime }
        lruEntry?.let { 
            cache.remove(it.key)
            stats.evictionCount++
        }
    }
    
    private fun evictLFU() {
        val lfuEntry = cache.entries.minByOrNull { it.value.accessCount }
        lfuEntry?.let { 
            cache.remove(it.key)
            stats.evictionCount++
        }
    }
    
    private fun evictFIFO() {
        val firstEntry = cache.entries.firstOrNull()
        firstEntry?.let { 
            cache.remove(it.key)
            stats.evictionCount++
        }
    }
    
    private fun evictRandom() {
        val randomEntry = cache.entries.randomOrNull()
        randomEntry?.let { 
            cache.remove(it.key)
            stats.evictionCount++
        }
    }
}

/**
 * Android数据同步实现
 */
class AndroidUnifyDataSync(
    private var policy: UnifySyncPolicy
) : UnifyDataSync {
    
    private val syncStatus = MutableStateFlow(
        UnifySyncStatus(
            isOnline = true,
            isSyncing = false,
            lastSyncTime = 0L,
            pendingSyncCount = 0,
            failedSyncCount = 0
        )
    )
    
    suspend fun initialize() {
        // 初始化网络状态监听
        // 这里可以注册网络状态变化监听器
    }
    
    suspend fun cleanup() {
        // 清理资源
    }
    
    override suspend fun syncToRemote(key: String): UnifySyncResult {
        // 实现同步到远程的逻辑
        return UnifySyncResult(
            key = key,
            success = true,
            timestamp = System.currentTimeMillis()
        )
    }
    
    override suspend fun syncFromRemote(key: String): UnifySyncResult {
        // 实现从远程同步的逻辑
        return UnifySyncResult(
            key = key,
            success = true,
            timestamp = System.currentTimeMillis()
        )
    }
    
    override suspend fun bidirectionalSync(key: String): UnifySyncResult {
        // 实现双向同步逻辑
        return UnifySyncResult(
            key = key,
            success = true,
            timestamp = System.currentTimeMillis()
        )
    }
    
    override suspend fun batchSync(keys: List<String>): List<UnifySyncResult> {
        return keys.map { bidirectionalSync(it) }
    }
    
    override fun setSyncPolicy(policy: UnifySyncPolicy) {
        this.policy = policy
    }
    
    override fun observeSyncStatus(): Flow<UnifySyncStatus> {
        return syncStatus
    }
}

/**
 * Android缓存条目
 */
private data class AndroidCacheEntry(
    val value: Any?,
    var expireTime: Long,
    var accessTime: Long,
    var accessCount: Long
)

/**
 * Android缓存统计
 */
private class AndroidCacheStats {
    var hitCount: Long = 0
    var missCount: Long = 0
    var evictionCount: Long = 0
    
    fun reset() {
        hitCount = 0
        missCount = 0
        evictionCount = 0
    }
}

/**
 * Android数据管理器工厂实现
 */
actual object UnifyDataManagerFactory {
    actual fun create(config: UnifyDataManagerConfig): UnifyDataManager {
        // 使用Application Context进行初始化
        val context = getApplicationContext()
        return AndroidUnifyDataManager(context, config)
    }
    
    private fun getApplicationContext(): Context {
        // 通过反射获取Application Context
        return try {
            val activityThread = Class.forName("android.app.ActivityThread")
            val currentApplication = activityThread.getMethod("currentApplication")
            currentApplication.invoke(null) as Context
        } catch (e: Exception) {
            throw IllegalStateException("无法获取Application Context，请确保在Android环境中运行")
        }
    }
    
    fun create(context: Context, config: UnifyDataManagerConfig = UnifyDataManagerConfig()): UnifyDataManager {
        return AndroidUnifyDataManager(context, config)
    }
}
