package com.unify.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

/**
 * 小程序平台数据管理器实现
 */
class MiniAppUnifyDataManager(
    private val config: UnifyDataManagerConfig
) : UnifyDataManager {
    
    override val storage: UnifyStorage = MiniAppUnifyStorage(config)
    override val state: UnifyStateManager = MiniAppUnifyStateManager()
    override val cache: UnifyCacheManager = MiniAppUnifyCacheManager(config.cachePolicy)
    override val sync: UnifyDataSync = MiniAppUnifyDataSync(config.syncPolicy)
    
    override suspend fun initialize() {
        (storage as MiniAppUnifyStorage).initialize()
        (cache as MiniAppUnifyCacheManager).initialize()
        (sync as MiniAppUnifyDataSync).initialize()
    }
    
    override suspend fun cleanup() {
        (cache as MiniAppUnifyCacheManager).cleanup()
        (sync as MiniAppUnifyDataSync).cleanup()
    }
}

/**
 * 小程序存储实现 - 基于小程序存储API
 */
class MiniAppUnifyStorage(
    private val config: UnifyDataManagerConfig
) : UnifyStorage {
    
    private val json = Json { 
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    // 小程序存储适配器，支持多平台
    private val storageAdapter = MiniAppStorageAdapter()
    
    suspend fun initialize() {
        // 初始化小程序存储适配器
        storageAdapter.initialize()
        
        // 检查存储配额
        checkStorageQuota()
    }
    
    override suspend fun <T> put(key: String, value: T) {
        try {
            val jsonString = json.encodeToString(value)
            val finalData = if (config.storageEncryption) {
                encryptDataForMiniApp(jsonString)
            } else {
                jsonString
            }
            
            // 使用小程序存储API
            storageAdapter.setStorage(key, finalData)
            
            // 存储元数据
            val metadata = MiniAppStorageMetadata(
                timestamp = System.currentTimeMillis(),
                size = jsonString.length.toLong(),
                encrypted = config.storageEncryption,
                platform = detectMiniAppPlatform()
            )
            storageAdapter.setStorage("${key}_meta", json.encodeToString(metadata))
            
        } catch (e: Exception) {
            throw RuntimeException("Failed to store data for key: $key", e)
        }
    }
    
    override suspend fun <T> get(key: String, type: KClass<T>): T? {
        return try {
            val storedData = storageAdapter.getStorage(key) ?: return null
            
            val jsonString = if (config.storageEncryption) {
                decryptDataForMiniApp(storedData)
            } else {
                storedData
            }
            
            json.decodeFromString(type.java, jsonString) as T
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun remove(key: String): Boolean {
        return try {
            storageAdapter.removeStorage(key)
            storageAdapter.removeStorage("${key}_meta")
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun clear() {
        try {
            storageAdapter.clearStorage()
        } catch (e: Exception) {
            // 忽略清理错误
        }
    }
    
    override suspend fun contains(key: String): Boolean {
        return storageAdapter.getStorage(key) != null
    }
    
    override suspend fun keys(): Set<String> {
        return try {
            storageAdapter.getStorageKeys()
                .filter { !it.endsWith("_meta") }
                .toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }
    
    override suspend fun size(): Long {
        return try {
            storageAdapter.getStorageSize()
        } catch (e: Exception) {
            0L
        }
    }
    
    private fun checkStorageQuota() {
        // 检查小程序存储配额
        val currentSize = storageAdapter.getStorageSize()
        val maxSize = when (detectMiniAppPlatform()) {
            MiniAppPlatform.WECHAT -> 10 * 1024 * 1024 // 微信小程序10MB
            MiniAppPlatform.ALIPAY -> 10 * 1024 * 1024 // 支付宝小程序10MB
            MiniAppPlatform.BAIDU -> 10 * 1024 * 1024 // 百度小程序10MB
            MiniAppPlatform.BYTEDANCE -> 50 * 1024 * 1024 // 字节跳动小程序50MB
            MiniAppPlatform.UNKNOWN -> 5 * 1024 * 1024 // 默认5MB
        }
        
        if (currentSize > maxSize * 0.8) {
            // 接近配额限制，清理旧数据
            cleanOldData()
        }
    }
    
    private fun cleanOldData() {
        // 清理最旧的数据以释放空间
        val dataWithTimestamp = mutableListOf<Pair<String, Long>>()
        
        storageAdapter.getStorageKeys()
            .filter { it.endsWith("_meta") }
            .forEach { metaKey ->
                try {
                    val metaJson = storageAdapter.getStorage(metaKey)
                    if (metaJson != null) {
                        val metadata = json.decodeFromString<MiniAppStorageMetadata>(metaJson)
                        val dataKey = metaKey.removeSuffix("_meta")
                        dataWithTimestamp.add(dataKey to metadata.timestamp)
                    }
                } catch (e: Exception) {
                    // 忽略解析错误
                }
            }
        
        // 删除最旧的30%数据
        val sortedData = dataWithTimestamp.sortedBy { it.second }
        val deleteCount = (sortedData.size * 0.3).toInt()
        
        sortedData.take(deleteCount).forEach { (key, _) ->
            runCatching { 
                storageAdapter.removeStorage(key)
                storageAdapter.removeStorage("${key}_meta")
            }
        }
    }
    
    private fun detectMiniAppPlatform(): MiniAppPlatform {
        // 检测小程序平台类型
        // 占位符实现
        return MiniAppPlatform.WECHAT
    }
    
    private fun encryptDataForMiniApp(data: String): String {
        // 小程序平台加密实现
        // 占位符实现
        return data
    }
    
    private fun decryptDataForMiniApp(data: String): String {
        // 小程序平台解密实现
        // 占位符实现
        return data
    }
}

/**
 * 小程序状态管理实现
 */
class MiniAppUnifyStateManager : UnifyStateManager {
    
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
 * 小程序缓存管理实现 - 优化内存使用
 */
class MiniAppUnifyCacheManager(
    private var policy: UnifyCachePolicy
) : UnifyCacheManager {
    
    private val cache = ConcurrentHashMap<String, MiniAppCacheEntry>()
    private val stats = MiniAppCacheStats()
    
    // 小程序内存限制较严格，调整默认策略
    init {
        policy = policy.copy(
            maxSize = minOf(policy.maxSize, 5 * 1024 * 1024), // 最多5MB
            defaultTtl = minOf(policy.defaultTtl, 1800000) // 最多30分钟
        )
    }
    
    suspend fun initialize() {
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
        
        val entry = MiniAppCacheEntry(
            value = value,
            expireTime = expireTime,
            accessTime = System.currentTimeMillis(),
            accessCount = 1
        )
        
        cache[key] = entry
        
        // 小程序内存限制严格，积极清理缓存
        if (cache.size * 512 > policy.maxSize) { // 估算每个条目512字节
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
            totalSize = cache.size.toLong() * 512, // 估算
            maxSize = policy.maxSize,
            hitRate = if (stats.hitCount + stats.missCount > 0) {
                stats.hitCount.toDouble() / (stats.hitCount + stats.missCount)
            } else 0.0
        )
    }
    
    override fun setCachePolicy(policy: UnifyCachePolicy) {
        this.policy = policy.copy(
            maxSize = minOf(policy.maxSize, 5 * 1024 * 1024),
            defaultTtl = minOf(policy.defaultTtl, 1800000)
        )
    }
    
    private suspend fun cleanExpiredCache() {
        val currentTime = System.currentTimeMillis()
        val expiredKeys = cache.entries
            .filter { it.value.expireTime > 0 && currentTime > it.value.expireTime }
            .map { it.key }
        
        expiredKeys.forEach { cache.remove(it) }
    }
    
    private fun evictCache() {
        // 小程序优先使用LRU策略以优化内存
        val lruEntry = cache.entries.minByOrNull { it.value.accessTime }
        lruEntry?.let { 
            cache.remove(it.key)
            stats.evictionCount++
        }
    }
}

/**
 * 小程序数据同步实现
 */
class MiniAppUnifyDataSync(
    private var policy: UnifySyncPolicy
) : UnifyDataSync {
    
    private val syncStatus = MutableStateFlow(
        UnifySyncStatus(
            isOnline = checkMiniAppNetworkStatus(),
            isSyncing = false,
            lastSyncTime = 0L,
            pendingSyncCount = 0,
            failedSyncCount = 0
        )
    )
    
    suspend fun initialize() {
        // 初始化小程序网络状态监听
        setupMiniAppNetworkMonitoring()
    }
    
    suspend fun cleanup() {
        // 清理资源
    }
    
    override suspend fun syncToRemote(key: String): UnifySyncResult {
        return try {
            // 使用小程序网络API进行同步
            syncToMiniAppCloud(key)
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
        return try {
            syncFromMiniAppCloud(key)
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
    
    override suspend fun bidirectionalSync(key: String): UnifySyncResult {
        return try {
            bidirectionalSyncWithMiniAppCloud(key)
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
    
    override suspend fun batchSync(keys: List<String>): List<UnifySyncResult> {
        // 小程序批量同步需要控制并发数
        return keys.chunked(5).flatMap { chunk ->
            chunk.map { bidirectionalSync(it) }
        }
    }
    
    override fun setSyncPolicy(policy: UnifySyncPolicy) {
        this.policy = policy
    }
    
    override fun observeSyncStatus(): Flow<UnifySyncStatus> {
        return syncStatus
    }
    
    private fun checkMiniAppNetworkStatus(): Boolean {
        // 检查小程序网络状态
        // 占位符实现
        return true
    }
    
    private fun setupMiniAppNetworkMonitoring() {
        // 设置小程序网络状态监听
        // 占位符实现
    }
    
    private suspend fun syncToMiniAppCloud(key: String) {
        // 同步到小程序云服务
        // 占位符实现
    }
    
    private suspend fun syncFromMiniAppCloud(key: String) {
        // 从小程序云服务同步
        // 占位符实现
    }
    
    private suspend fun bidirectionalSyncWithMiniAppCloud(key: String) {
        // 与小程序云服务双向同步
        // 占位符实现
    }
}

/**
 * 小程序存储适配器 - 适配不同小程序平台
 */
class MiniAppStorageAdapter {
    
    private val localStorage = mutableMapOf<String, String>()
    
    fun initialize() {
        // 初始化小程序存储适配器
        // 根据不同平台使用不同的存储API
    }
    
    fun setStorage(key: String, value: String) {
        // 适配不同小程序平台的存储API
        // 微信: wx.setStorageSync
        // 支付宝: my.setStorageSync
        // 百度: swan.setStorageSync
        // 字节跳动: tt.setStorageSync
        localStorage[key] = value
    }
    
    fun getStorage(key: String): String? {
        // 适配不同小程序平台的读取API
        return localStorage[key]
    }
    
    fun removeStorage(key: String) {
        // 适配不同小程序平台的删除API
        localStorage.remove(key)
    }
    
    fun clearStorage() {
        // 适配不同小程序平台的清空API
        localStorage.clear()
    }
    
    fun getStorageKeys(): List<String> {
        // 获取所有存储键
        return localStorage.keys.toList()
    }
    
    fun getStorageSize(): Long {
        // 计算存储大小
        return localStorage.values.sumOf { it.length.toLong() * 2 } // UTF-16编码
    }
}

/**
 * 小程序平台类型
 */
enum class MiniAppPlatform {
    WECHAT,     // 微信小程序
    ALIPAY,     // 支付宝小程序
    BAIDU,      // 百度小程序
    BYTEDANCE,  // 字节跳动小程序
    UNKNOWN     // 未知平台
}

/**
 * 小程序存储元数据
 */
@kotlinx.serialization.Serializable
private data class MiniAppStorageMetadata(
    val timestamp: Long,
    val size: Long,
    val encrypted: Boolean,
    val platform: MiniAppPlatform
)

/**
 * 小程序缓存条目
 */
private data class MiniAppCacheEntry(
    val value: Any?,
    var expireTime: Long,
    var accessTime: Long,
    var accessCount: Long
)

/**
 * 小程序缓存统计
 */
private class MiniAppCacheStats {
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
 * 小程序数据管理器工厂实现
 */
actual object UnifyDataManagerFactory {
    actual fun create(config: UnifyDataManagerConfig): UnifyDataManager {
        return MiniAppUnifyDataManager(config)
    }
}
