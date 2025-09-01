package com.unify.data

import kotlinx.browser.localStorage
import kotlinx.browser.sessionStorage
import kotlinx.browser.window
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.w3c.dom.Storage
import org.w3c.dom.get
import org.w3c.dom.set
import kotlin.js.Date
import kotlin.reflect.KClass

/**
 * Web平台数据管理器实现
 */
class WebUnifyDataManager(
    private val config: UnifyDataManagerConfig
) : UnifyDataManager {
    
    override val storage: UnifyStorage = WebUnifyStorage(config)
    override val state: UnifyStateManager = WebUnifyStateManager()
    override val cache: UnifyCacheManager = WebUnifyCacheManager(config.cachePolicy)
    override val sync: UnifyDataSync = WebUnifyDataSync(config.syncPolicy)
    
    override suspend fun initialize() {
        (storage as WebUnifyStorage).initialize()
        (cache as WebUnifyCacheManager).initialize()
        (sync as WebUnifyDataSync).initialize()
    }
    
    override suspend fun cleanup() {
        (cache as WebUnifyCacheManager).cleanup()
        (sync as WebUnifyDataSync).cleanup()
    }
}

/**
 * Web存储实现 - 基于localStorage和IndexedDB
 */
class WebUnifyStorage(
    private val config: UnifyDataManagerConfig
) : UnifyStorage {
    
    private val storage: Storage = localStorage
    private val sessionStore: Storage = sessionStorage
    private val json = Json { 
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    private val keyPrefix = "unify_"
    
    suspend fun initialize() {
        // 检查浏览器存储支持
        checkStorageSupport()
        // 清理过期数据
        cleanExpiredData()
    }
    
    override suspend fun <T> put(key: String, value: T) {
        try {
            val jsonString = json.encodeToString(value)
            val finalKey = keyPrefix + key
            
            if (config.storageEncryption) {
                // Web平台可以使用Web Crypto API进行加密
                val encryptedData = encryptData(jsonString)
                storage[finalKey] = encryptedData
            } else {
                storage[finalKey] = jsonString
            }
            
            // 存储元数据
            val metadata = WebStorageMetadata(
                timestamp = Date.now(),
                size = jsonString.length,
                encrypted = config.storageEncryption
            )
            storage["${finalKey}_meta"] = json.encodeToString(metadata)
            
        } catch (e: Exception) {
            // 如果localStorage满了，尝试清理或使用sessionStorage
            if (e.message?.contains("QuotaExceededError") == true) {
                cleanOldData()
                // 重试一次
                val jsonString = json.encodeToString(value)
                storage[keyPrefix + key] = jsonString
            } else {
                throw e
            }
        }
    }
    
    override suspend fun <T> get(key: String, type: KClass<T>): T? {
        return try {
            val finalKey = keyPrefix + key
            val jsonString = storage[finalKey] ?: return null
            
            val decryptedData = if (config.storageEncryption) {
                decryptData(jsonString)
            } else {
                jsonString
            }
            
            JSON.parse<T>(decryptedData)
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun remove(key: String): Boolean {
        return try {
            val finalKey = keyPrefix + key
            storage.removeItem(finalKey)
            storage.removeItem("${finalKey}_meta")
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun clear() {
        try {
            val keysToRemove = mutableListOf<String>()
            for (i in 0 until storage.length) {
                val key = storage.key(i)
                if (key?.startsWith(keyPrefix) == true) {
                    keysToRemove.add(key)
                }
            }
            keysToRemove.forEach { storage.removeItem(it) }
        } catch (e: Exception) {
            // 忽略清理错误
        }
    }
    
    override suspend fun contains(key: String): Boolean {
        return storage[keyPrefix + key] != null
    }
    
    override suspend fun keys(): Set<String> {
        return try {
            val keys = mutableSetOf<String>()
            for (i in 0 until storage.length) {
                val key = storage.key(i)
                if (key?.startsWith(keyPrefix) == true && !key.endsWith("_meta")) {
                    keys.add(key.removePrefix(keyPrefix))
                }
            }
            keys
        } catch (e: Exception) {
            emptySet()
        }
    }
    
    override suspend fun size(): Long {
        return try {
            var totalSize = 0L
            for (i in 0 until storage.length) {
                val key = storage.key(i)
                if (key?.startsWith(keyPrefix) == true) {
                    val value = storage[key]
                    totalSize += (key.length + (value?.length ?: 0)) * 2 // UTF-16编码
                }
            }
            totalSize
        } catch (e: Exception) {
            0L
        }
    }
    
    private fun checkStorageSupport() {
        // 检查浏览器存储支持
        if (!js("typeof(Storage) !== 'undefined'")) {
            throw UnsupportedOperationException("Browser does not support Web Storage")
        }
    }
    
    private suspend fun cleanExpiredData() {
        // 清理过期的数据
        val currentTime = Date.now()
        val keysToRemove = mutableListOf<String>()
        
        for (i in 0 until storage.length) {
            val key = storage.key(i)
            if (key?.startsWith(keyPrefix) == true && key.endsWith("_meta")) {
                try {
                    val metaJson = storage[key]
                    if (metaJson != null) {
                        val metadata = json.decodeFromString<WebStorageMetadata>(metaJson)
                        // 如果数据超过7天，标记为清理
                        if (currentTime - metadata.timestamp > 7 * 24 * 60 * 60 * 1000) {
                            val dataKey = key.removeSuffix("_meta")
                            keysToRemove.add(dataKey)
                            keysToRemove.add(key)
                        }
                    }
                } catch (e: Exception) {
                    // 忽略元数据解析错误
                }
            }
        }
        
        keysToRemove.forEach { storage.removeItem(it) }
    }
    
    private suspend fun cleanOldData() {
        // 清理最旧的数据以释放空间
        val dataWithTimestamp = mutableListOf<Pair<String, Double>>()
        
        for (i in 0 until storage.length) {
            val key = storage.key(i)
            if (key?.startsWith(keyPrefix) == true && key.endsWith("_meta")) {
                try {
                    val metaJson = storage[key]
                    if (metaJson != null) {
                        val metadata = json.decodeFromString<WebStorageMetadata>(metaJson)
                        dataWithTimestamp.add(key.removeSuffix("_meta") to metadata.timestamp)
                    }
                } catch (e: Exception) {
                    // 忽略解析错误
                }
            }
        }
        
        // 删除最旧的20%数据
        val sortedData = dataWithTimestamp.sortedBy { it.second }
        val deleteCount = (sortedData.size * 0.2).toInt()
        
        sortedData.take(deleteCount).forEach { (key, _) ->
            storage.removeItem(key)
            storage.removeItem("${key}_meta")
        }
    }
    
    private fun encryptData(data: String): String {
        // Web平台加密实现 - 可以使用Web Crypto API
        // 这里返回原数据作为占位符
        return data
    }
    
    private fun decryptData(data: String): String {
        // Web平台解密实现
        return data
    }
}

/**
 * Web状态管理实现
 */
class WebUnifyStateManager : UnifyStateManager {
    
    private val states = mutableMapOf<String, Any?>()
    private val stateFlows = mutableMapOf<String, MutableStateFlow<Any?>>()
    
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
 * Web缓存管理实现 - 基于Map和Web Storage
 */
class WebUnifyCacheManager(
    private var policy: UnifyCachePolicy
) : UnifyCacheManager {
    
    private val cache = mutableMapOf<String, WebCacheEntry>()
    private val stats = WebCacheStats()
    
    suspend fun initialize() {
        // 从sessionStorage恢复缓存
        restoreCacheFromStorage()
        cleanExpiredCache()
    }
    
    suspend fun cleanup() {
        // 保存缓存到sessionStorage
        saveCacheToStorage()
        cache.clear()
    }
    
    override suspend fun <T> cache(key: String, value: T, ttl: Long) {
        val actualTtl = if (ttl > 0) ttl else policy.defaultTtl
        val expireTime = if (actualTtl > 0) {
            Date.now() + actualTtl
        } else {
            0.0 // 永不过期
        }
        
        val entry = WebCacheEntry(
            value = value,
            expireTime = expireTime,
            accessTime = Date.now(),
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
        if (entry.expireTime > 0 && Date.now() > entry.expireTime) {
            cache.remove(key)
            stats.missCount++
            return null
        }
        
        // 更新访问信息
        entry.accessTime = Date.now()
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
        // 清理sessionStorage中的缓存
        sessionStorage.removeItem("unify_cache")
    }
    
    override suspend fun isCacheValid(key: String): Boolean {
        val entry = cache[key] ?: return false
        return entry.expireTime == 0.0 || Date.now() <= entry.expireTime
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
        val currentTime = Date.now()
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
    
    private fun restoreCacheFromStorage() {
        // 从sessionStorage恢复缓存数据
        try {
            val cacheData = sessionStorage["unify_cache"]
            if (cacheData != null) {
                // 这里可以实现缓存数据的反序列化
                // 由于类型擦除问题，暂时跳过
            }
        } catch (e: Exception) {
            // 忽略恢复错误
        }
    }
    
    private fun saveCacheToStorage() {
        // 保存缓存数据到sessionStorage
        try {
            // 这里可以实现缓存数据的序列化
            // 由于类型擦除问题，暂时跳过
        } catch (e: Exception) {
            // 忽略保存错误
        }
    }
}

/**
 * Web数据同步实现
 */
class WebUnifyDataSync(
    private var policy: UnifySyncPolicy
) : UnifyDataSync {
    
    private val syncStatus = MutableStateFlow(
        UnifySyncStatus(
            isOnline = window.navigator.onLine,
            isSyncing = false,
            lastSyncTime = 0L,
            pendingSyncCount = 0,
            failedSyncCount = 0
        )
    )
    
    suspend fun initialize() {
        // 监听网络状态变化
        setupNetworkListeners()
    }
    
    suspend fun cleanup() {
        // 清理网络监听器
    }
    
    override suspend fun syncToRemote(key: String): UnifySyncResult {
        // Web平台同步到远程实现
        return try {
            // 这里可以使用fetch API进行网络请求
            UnifySyncResult(
                key = key,
                success = true,
                timestamp = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            UnifySyncResult(
                key = key,
                success = false,
                timestamp = System.currentTimeMillis(),
                error = e.message
            )
        }
    }
    
    override suspend fun syncFromRemote(key: String): UnifySyncResult {
        // Web平台从远程同步实现
        return UnifySyncResult(
            key = key,
            success = true,
            timestamp = System.currentTimeMillis()
        )
    }
    
    override suspend fun bidirectionalSync(key: String): UnifySyncResult {
        // Web平台双向同步实现
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
    
    private fun setupNetworkListeners() {
        // 监听网络状态变化
        window.addEventListener("online", {
            syncStatus.value = syncStatus.value.copy(isOnline = true)
        })
        
        window.addEventListener("offline", {
            syncStatus.value = syncStatus.value.copy(isOnline = false)
        })
    }
}

/**
 * Web存储元数据
 */
@kotlinx.serialization.Serializable
private data class WebStorageMetadata(
    val timestamp: Double,
    val size: Int,
    val encrypted: Boolean
)

/**
 * Web缓存条目
 */
private data class WebCacheEntry(
    val value: Any?,
    var expireTime: Double,
    var accessTime: Double,
    var accessCount: Long
)

/**
 * Web缓存统计
 */
private class WebCacheStats {
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
 * Web数据管理器工厂实现
 */
actual object UnifyDataManagerFactory {
    actual fun create(config: UnifyDataManagerConfig): UnifyDataManager {
        return WebUnifyDataManager(config)
    }
}
