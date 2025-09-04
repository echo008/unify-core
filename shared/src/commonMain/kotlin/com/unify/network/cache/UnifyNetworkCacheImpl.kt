package com.unify.network.cache

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * 网络缓存实现
 * 提供智能网络缓存、离线支持和数据同步功能
 */
class UnifyNetworkCacheImpl {
    private val _cacheState = MutableStateFlow(CacheState())
    val cacheState: StateFlow<CacheState> = _cacheState
    
    private val cacheStorage = mutableMapOf<String, CacheEntry>()
    private val cacheMetadata = mutableMapOf<String, CacheMetadata>()
    private val requestQueue = mutableListOf<QueuedRequest>()
    private val cachePolicy = CachePolicy()
    
    // 缓存常量
    companion object {
        private const val DEFAULT_CACHE_SIZE_MB = 100
        private const val DEFAULT_TTL_SECONDS = 3600
        private const val MAX_CACHE_ENTRIES = 10000
        private const val CACHE_CLEANUP_INTERVAL = 300000L // 5分钟
        private const val OFFLINE_REQUEST_TIMEOUT = 30000L
        private const val CACHE_HIT_THRESHOLD = 0.8
        private const val MEMORY_WARNING_THRESHOLD = 0.9
        private const val COMPRESSION_MIN_SIZE = 1024
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val RETRY_DELAY_MS = 1000L
        private const val PREFETCH_THRESHOLD = 0.1
        private const val CACHE_VERSION = 1
    }
    
    /**
     * 初始化网络缓存
     */
    suspend fun initialize(config: CacheConfig = CacheConfig()): CacheInitResult {
        return try {
            _cacheState.value = _cacheState.value.copy(
                isInitializing = true,
                config = config
            )
            
            // 加载持久化缓存
            loadPersistedCache()
            
            // 启动缓存清理任务
            startCacheCleanup()
            
            // 初始化缓存策略
            cachePolicy.initialize(config.policyConfig)
            
            _cacheState.value = _cacheState.value.copy(
                isInitializing = false,
                isInitialized = true,
                initTime = System.currentTimeMillis()
            )
            
            CacheInitResult.Success("网络缓存初始化成功")
            
        } catch (e: Exception) {
            _cacheState.value = _cacheState.value.copy(
                isInitializing = false,
                initError = "初始化失败: ${e.message}"
            )
            CacheInitResult.Error("初始化失败: ${e.message}")
        }
    }
    
    /**
     * 获取缓存数据
     */
    suspend fun get(key: String): CacheResult<String> {
        return try {
            val entry = cacheStorage[key]
            
            if (entry == null) {
                updateCacheStats(miss = true)
                return CacheResult.Miss("缓存未命中")
            }
            
            // 检查缓存是否过期
            if (isCacheExpired(entry)) {
                cacheStorage.remove(key)
                cacheMetadata.remove(key)
                updateCacheStats(miss = true, expired = true)
                return CacheResult.Expired("缓存已过期")
            }
            
            // 更新访问时间
            val updatedEntry = entry.copy(
                lastAccessTime = System.currentTimeMillis(),
                accessCount = entry.accessCount + 1
            )
            cacheStorage[key] = updatedEntry
            
            updateCacheStats(hit = true)
            CacheResult.Hit(updatedEntry.data)
            
        } catch (e: Exception) {
            CacheResult.Error("获取缓存失败: ${e.message}")
        }
    }
    
    /**
     * 设置缓存数据
     */
    suspend fun put(
        key: String, 
        data: String, 
        ttl: Long = DEFAULT_TTL_SECONDS * 1000,
        priority: CachePriority = CachePriority.NORMAL
    ): CacheResult<Unit> {
        return try {
            // 检查缓存容量
            if (shouldEvictCache()) {
                evictLeastRecentlyUsed()
            }
            
            val now = System.currentTimeMillis()
            val entry = CacheEntry(
                key = key,
                data = data,
                createdTime = now,
                lastAccessTime = now,
                expiryTime = now + ttl,
                size = data.length,
                priority = priority,
                accessCount = 0,
                compressed = shouldCompress(data)
            )
            
            val finalData = if (entry.compressed) compressData(data) else data
            val finalEntry = entry.copy(
                data = finalData,
                size = finalData.length
            )
            
            cacheStorage[key] = finalEntry
            cacheMetadata[key] = CacheMetadata(
                key = key,
                size = finalEntry.size,
                createdTime = now,
                priority = priority
            )
            
            updateCacheSize()
            CacheResult.Success(Unit)
            
        } catch (e: Exception) {
            CacheResult.Error("设置缓存失败: ${e.message}")
        }
    }
    
    /**
     * 删除缓存数据
     */
    suspend fun remove(key: String): CacheResult<Unit> {
        return try {
            val removed = cacheStorage.remove(key) != null
            cacheMetadata.remove(key)
            
            if (removed) {
                updateCacheSize()
                CacheResult.Success(Unit)
            } else {
                CacheResult.Miss("缓存项不存在")
            }
            
        } catch (e: Exception) {
            CacheResult.Error("删除缓存失败: ${e.message}")
        }
    }
    
    /**
     * 清空所有缓存
     */
    suspend fun clear(): CacheResult<Unit> {
        return try {
            val clearedCount = cacheStorage.size
            cacheStorage.clear()
            cacheMetadata.clear()
            
            _cacheState.value = _cacheState.value.copy(
                totalSize = 0,
                entryCount = 0,
                lastClearTime = System.currentTimeMillis()
            )
            
            CacheResult.Success(Unit)
            
        } catch (e: Exception) {
            CacheResult.Error("清空缓存失败: ${e.message}")
        }
    }
    
    /**
     * 批量获取缓存
     */
    suspend fun getBatch(keys: List<String>): Map<String, CacheResult<String>> {
        return keys.associateWith { key ->
            get(key)
        }
    }
    
    /**
     * 批量设置缓存
     */
    suspend fun putBatch(
        entries: Map<String, String>,
        ttl: Long = DEFAULT_TTL_SECONDS * 1000,
        priority: CachePriority = CachePriority.NORMAL
    ): Map<String, CacheResult<Unit>> {
        return entries.mapValues { (key, data) ->
            put(key, data, ttl, priority)
        }
    }
    
    /**
     * 预取数据
     */
    suspend fun prefetch(
        keys: List<String>,
        dataProvider: suspend (String) -> String?
    ): PrefetchResult {
        return try {
            val prefetchedCount = keys.count { key ->
                if (!cacheStorage.containsKey(key)) {
                    val data = dataProvider(key)
                    if (data != null) {
                        put(key, data, priority = CachePriority.LOW)
                        true
                    } else false
                } else false
            }
            
            PrefetchResult.Success(prefetchedCount)
            
        } catch (e: Exception) {
            PrefetchResult.Error("预取失败: ${e.message}")
        }
    }
    
    /**
     * 离线请求队列
     */
    suspend fun queueOfflineRequest(request: NetworkRequest): QueueResult {
        return try {
            if (requestQueue.size >= _cacheState.value.config.maxOfflineRequests) {
                return QueueResult.Error("离线请求队列已满")
            }
            
            val queuedRequest = QueuedRequest(
                id = generateRequestId(),
                request = request,
                queueTime = System.currentTimeMillis(),
                retryCount = 0
            )
            
            requestQueue.add(queuedRequest)
            
            _cacheState.value = _cacheState.value.copy(
                offlineRequestCount = requestQueue.size
            )
            
            QueueResult.Success(queuedRequest.id)
            
        } catch (e: Exception) {
            QueueResult.Error("添加离线请求失败: ${e.message}")
        }
    }
    
    /**
     * 处理离线请求队列
     */
    suspend fun processOfflineRequests(): ProcessResult {
        return try {
            val processedRequests = mutableListOf<QueuedRequest>()
            val failedRequests = mutableListOf<QueuedRequest>()
            
            requestQueue.forEach { queuedRequest ->
                try {
                    val result = executeRequest(queuedRequest.request)
                    if (result.isSuccess) {
                        processedRequests.add(queuedRequest)
                        // 缓存成功的响应
                        result.data?.let { data ->
                            put(queuedRequest.request.url, data)
                        }
                    } else {
                        if (queuedRequest.retryCount < MAX_RETRY_ATTEMPTS) {
                            queuedRequest.retryCount++
                        } else {
                            failedRequests.add(queuedRequest)
                        }
                    }
                } catch (e: Exception) {
                    failedRequests.add(queuedRequest)
                }
            }
            
            // 移除已处理和失败的请求
            requestQueue.removeAll(processedRequests + failedRequests)
            
            _cacheState.value = _cacheState.value.copy(
                offlineRequestCount = requestQueue.size
            )
            
            ProcessResult.Success(
                processed = processedRequests.size,
                failed = failedRequests.size,
                remaining = requestQueue.size
            )
            
        } catch (e: Exception) {
            ProcessResult.Error("处理离线请求失败: ${e.message}")
        }
    }
    
    /**
     * 获取缓存统计信息
     */
    fun getCacheStatistics(): CacheStatistics {
        val state = _cacheState.value
        return CacheStatistics(
            totalEntries = cacheStorage.size,
            totalSize = state.totalSize,
            hitCount = state.hitCount,
            missCount = state.missCount,
            hitRate = if (state.hitCount + state.missCount > 0) {
                state.hitCount.toDouble() / (state.hitCount + state.missCount)
            } else 0.0,
            expiredCount = state.expiredCount,
            evictedCount = state.evictedCount,
            offlineRequestCount = requestQueue.size,
            memoryUsage = calculateMemoryUsage(),
            oldestEntry = findOldestEntry(),
            newestEntry = findNewestEntry()
        )
    }
    
    /**
     * 优化缓存
     */
    suspend fun optimizeCache(): OptimizationResult {
        return try {
            var optimizedCount = 0
            
            // 1. 清理过期缓存
            val expiredKeys = cacheStorage.filter { (_, entry) ->
                isCacheExpired(entry)
            }.keys
            
            expiredKeys.forEach { key ->
                cacheStorage.remove(key)
                cacheMetadata.remove(key)
                optimizedCount++
            }
            
            // 2. 压缩大数据
            val uncompressedEntries = cacheStorage.filter { (_, entry) ->
                !entry.compressed && entry.data.length > COMPRESSION_MIN_SIZE
            }
            
            uncompressedEntries.forEach { (key, entry) ->
                val compressedData = compressData(entry.data)
                if (compressedData.length < entry.data.length) {
                    cacheStorage[key] = entry.copy(
                        data = compressedData,
                        size = compressedData.length,
                        compressed = true
                    )
                    optimizedCount++
                }
            }
            
            // 3. 整理内存
            if (shouldEvictCache()) {
                val evictedCount = evictLeastRecentlyUsed()
                optimizedCount += evictedCount
            }
            
            updateCacheSize()
            
            OptimizationResult.Success(
                optimizedEntries = optimizedCount,
                freedSpace = calculateFreedSpace(),
                newHitRate = getCacheStatistics().hitRate
            )
            
        } catch (e: Exception) {
            OptimizationResult.Error("缓存优化失败: ${e.message}")
        }
    }
    
    /**
     * 导出缓存数据
     */
    suspend fun exportCache(): ExportResult {
        return try {
            val exportData = CacheExportData(
                version = CACHE_VERSION,
                timestamp = System.currentTimeMillis(),
                entries = cacheStorage.values.toList(),
                metadata = cacheMetadata.values.toList(),
                statistics = getCacheStatistics()
            )
            
            val jsonData = Json.encodeToString(CacheExportData.serializer(), exportData)
            ExportResult.Success(jsonData)
            
        } catch (e: Exception) {
            ExportResult.Error("导出缓存失败: ${e.message}")
        }
    }
    
    /**
     * 导入缓存数据
     */
    suspend fun importCache(data: String): ImportResult {
        return try {
            val importData = Json.decodeFromString(CacheExportData.serializer(), data)
            
            if (importData.version != CACHE_VERSION) {
                return ImportResult.Error("缓存版本不兼容")
            }
            
            var importedCount = 0
            importData.entries.forEach { entry ->
                if (!isCacheExpired(entry)) {
                    cacheStorage[entry.key] = entry
                    importedCount++
                }
            }
            
            importData.metadata.forEach { metadata ->
                if (cacheStorage.containsKey(metadata.key)) {
                    cacheMetadata[metadata.key] = metadata
                }
            }
            
            updateCacheSize()
            
            ImportResult.Success(importedCount)
            
        } catch (e: Exception) {
            ImportResult.Error("导入缓存失败: ${e.message}")
        }
    }
    
    // 私有辅助方法
    
    private suspend fun loadPersistedCache() {
        // 加载持久化缓存数据
    }
    
    private suspend fun startCacheCleanup() {
        // 启动定期缓存清理任务
    }
    
    private fun isCacheExpired(entry: CacheEntry): Boolean {
        return System.currentTimeMillis() > entry.expiryTime
    }
    
    private fun shouldEvictCache(): Boolean {
        val state = _cacheState.value
        return cacheStorage.size >= MAX_CACHE_ENTRIES ||
                state.totalSize >= state.config.maxSizeMB * 1024 * 1024
    }
    
    private fun evictLeastRecentlyUsed(): Int {
        val sortedEntries = cacheStorage.entries.sortedBy { it.value.lastAccessTime }
        val evictCount = (cacheStorage.size * 0.1).toInt().coerceAtLeast(1)
        
        var evicted = 0
        sortedEntries.take(evictCount).forEach { (key, _) ->
            cacheStorage.remove(key)
            cacheMetadata.remove(key)
            evicted++
        }
        
        _cacheState.value = _cacheState.value.copy(
            evictedCount = _cacheState.value.evictedCount + evicted
        )
        
        return evicted
    }
    
    private fun shouldCompress(data: String): Boolean {
        return data.length > COMPRESSION_MIN_SIZE
    }
    
    private fun compressData(data: String): String {
        // 模拟数据压缩
        return if (data.length > COMPRESSION_MIN_SIZE) {
            "compressed:${data.take(data.length / 2)}"
        } else data
    }
    
    private fun decompressData(data: String): String {
        return if (data.startsWith("compressed:")) {
            data.removePrefix("compressed:")
        } else data
    }
    
    private fun updateCacheStats(hit: Boolean = false, miss: Boolean = false, expired: Boolean = false) {
        _cacheState.value = _cacheState.value.copy(
            hitCount = if (hit) _cacheState.value.hitCount + 1 else _cacheState.value.hitCount,
            missCount = if (miss) _cacheState.value.missCount + 1 else _cacheState.value.missCount,
            expiredCount = if (expired) _cacheState.value.expiredCount + 1 else _cacheState.value.expiredCount,
            lastAccessTime = System.currentTimeMillis()
        )
    }
    
    private fun updateCacheSize() {
        val totalSize = cacheStorage.values.sumOf { it.size }
        _cacheState.value = _cacheState.value.copy(
            totalSize = totalSize,
            entryCount = cacheStorage.size
        )
    }
    
    private fun calculateMemoryUsage(): Long {
        return cacheStorage.values.sumOf { it.size.toLong() }
    }
    
    private fun findOldestEntry(): Long? {
        return cacheStorage.values.minOfOrNull { it.createdTime }
    }
    
    private fun findNewestEntry(): Long? {
        return cacheStorage.values.maxOfOrNull { it.createdTime }
    }
    
    private fun calculateFreedSpace(): Long {
        // 计算优化后释放的空间
        return 0L
    }
    
    private fun generateRequestId(): String {
        return "req_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
    
    private suspend fun executeRequest(request: NetworkRequest): RequestResult {
        // 模拟网络请求执行
        return RequestResult(
            isSuccess = (1..10).random() > 3, // 70% 成功率
            data = if ((1..10).random() > 3) "response_data_${System.currentTimeMillis()}" else null,
            error = if ((1..10).random() <= 3) "网络请求失败" else null
        )
    }
}

// 缓存策略类
class CachePolicy {
    private var config = PolicyConfig()
    
    fun initialize(config: PolicyConfig) {
        this.config = config
    }
    
    fun shouldCache(request: NetworkRequest): Boolean {
        return when (request.method) {
            "GET" -> true
            "POST" -> config.cachePostRequests
            else -> false
        }
    }
    
    fun getTTL(request: NetworkRequest): Long {
        return when {
            request.url.contains("static") -> config.staticResourceTTL
            request.url.contains("api") -> config.apiResponseTTL
            else -> config.defaultTTL
        }
    }
    
    fun getPriority(request: NetworkRequest): CachePriority {
        return when {
            request.url.contains("critical") -> CachePriority.HIGH
            request.url.contains("background") -> CachePriority.LOW
            else -> CachePriority.NORMAL
        }
    }
}

// 数据类定义

@Serializable
data class CacheState(
    val isInitializing: Boolean = false,
    val isInitialized: Boolean = false,
    val config: CacheConfig = CacheConfig(),
    val totalSize: Int = 0,
    val entryCount: Int = 0,
    val hitCount: Long = 0,
    val missCount: Long = 0,
    val expiredCount: Long = 0,
    val evictedCount: Long = 0,
    val offlineRequestCount: Int = 0,
    val initTime: Long = 0,
    val lastAccessTime: Long = 0,
    val lastClearTime: Long = 0,
    val initError: String? = null
)

@Serializable
data class CacheConfig(
    val maxSizeMB: Int = DEFAULT_CACHE_SIZE_MB,
    val defaultTTL: Long = DEFAULT_TTL_SECONDS * 1000,
    val maxEntries: Int = MAX_CACHE_ENTRIES,
    val enableCompression: Boolean = true,
    val enablePersistence: Boolean = true,
    val maxOfflineRequests: Int = 1000,
    val policyConfig: PolicyConfig = PolicyConfig()
)

@Serializable
data class PolicyConfig(
    val cachePostRequests: Boolean = false,
    val staticResourceTTL: Long = 86400000L, // 24小时
    val apiResponseTTL: Long = 3600000L, // 1小时
    val defaultTTL: Long = DEFAULT_TTL_SECONDS * 1000
)

@Serializable
data class CacheEntry(
    val key: String,
    val data: String,
    val createdTime: Long,
    val lastAccessTime: Long,
    val expiryTime: Long,
    val size: Int,
    val priority: CachePriority,
    val accessCount: Int,
    val compressed: Boolean
)

@Serializable
data class CacheMetadata(
    val key: String,
    val size: Int,
    val createdTime: Long,
    val priority: CachePriority
)

@Serializable
data class QueuedRequest(
    val id: String,
    val request: NetworkRequest,
    val queueTime: Long,
    var retryCount: Int
)

@Serializable
data class NetworkRequest(
    val url: String,
    val method: String,
    val headers: Map<String, String> = emptyMap(),
    val body: String? = null
)

@Serializable
data class CacheStatistics(
    val totalEntries: Int,
    val totalSize: Int,
    val hitCount: Long,
    val missCount: Long,
    val hitRate: Double,
    val expiredCount: Long,
    val evictedCount: Long,
    val offlineRequestCount: Int,
    val memoryUsage: Long,
    val oldestEntry: Long?,
    val newestEntry: Long?
)

@Serializable
data class CacheExportData(
    val version: Int,
    val timestamp: Long,
    val entries: List<CacheEntry>,
    val metadata: List<CacheMetadata>,
    val statistics: CacheStatistics
)

data class RequestResult(
    val isSuccess: Boolean,
    val data: String?,
    val error: String?
)

enum class CachePriority {
    LOW, NORMAL, HIGH
}

// 结果类定义

sealed class CacheResult<T> {
    data class Hit<T>(val data: T) : CacheResult<T>()
    data class Miss<T>(val message: String) : CacheResult<T>()
    data class Expired<T>(val message: String) : CacheResult<T>()
    data class Success<T>(val data: T) : CacheResult<T>()
    data class Error<T>(val message: String) : CacheResult<T>()
}

sealed class CacheInitResult {
    data class Success(val message: String) : CacheInitResult()
    data class Error(val message: String) : CacheInitResult()
}

sealed class PrefetchResult {
    data class Success(val prefetchedCount: Int) : PrefetchResult()
    data class Error(val message: String) : PrefetchResult()
}

sealed class QueueResult {
    data class Success(val requestId: String) : QueueResult()
    data class Error(val message: String) : QueueResult()
}

sealed class ProcessResult {
    data class Success(val processed: Int, val failed: Int, val remaining: Int) : ProcessResult()
    data class Error(val message: String) : ProcessResult()
}

sealed class OptimizationResult {
    data class Success(val optimizedEntries: Int, val freedSpace: Long, val newHitRate: Double) : OptimizationResult()
    data class Error(val message: String) : OptimizationResult()
}

sealed class ExportResult {
    data class Success(val data: String) : ExportResult()
    data class Error(val message: String) : ExportResult()
}

sealed class ImportResult {
    data class Success(val importedCount: Int) : ImportResult()
    data class Error(val message: String) : ImportResult()
}
