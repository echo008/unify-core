package com.unify.core.memory

import kotlinx.coroutines.flow.Flow
import com.unify.core.platform.getCurrentTimeMillis
import com.unify.core.platform.getNanoTime
import kotlinx.coroutines.flow.MutableStateFlow
import com.unify.core.platform.getCurrentTimeMillis
import com.unify.core.platform.getNanoTime
import kotlinx.coroutines.flow.StateFlow
import com.unify.core.platform.getCurrentTimeMillis
import com.unify.core.platform.getNanoTime
import kotlinx.coroutines.flow.asStateFlow
import com.unify.core.platform.getCurrentTimeMillis
import com.unify.core.platform.getNanoTime
import kotlinx.serialization.Serializable
import com.unify.core.platform.getCurrentTimeMillis
import com.unify.core.platform.getNanoTime
import kotlinx.serialization.json.Json
import com.unify.core.platform.getCurrentTimeMillis
import com.unify.core.platform.getNanoTime

/**
 * Unify跨平台内存管理器
 * 支持8大平台的统一内存管理
 */
interface UnifyMemoryManager {
    suspend fun getMemoryInfo(): MemoryInfo
    suspend fun getMemoryUsage(): MemoryUsage
    suspend fun clearCache()
    suspend fun optimizeMemory()
    suspend fun setMemoryThreshold(threshold: Long)
    fun getMemoryStatus(): Flow<MemoryStatus>
    suspend fun enableMemoryMonitoring(enabled: Boolean)
    suspend fun getGarbageCollectionInfo(): GCInfo
}

/**
 * 内存信息数据类
 */
@Serializable
data class MemoryInfo(
    val totalMemory: Long,
    val availableMemory: Long,
    val usedMemory: Long,
    val freeMemory: Long,
    val maxMemory: Long,
    val memoryClass: Int,
    val largeMemoryClass: Int,
    val isLowMemory: Boolean,
    val threshold: Long,
    val timestamp: Long = getCurrentTimeMillis()
)

/**
 * 内存使用情况
 */
@Serializable
data class MemoryUsage(
    val heapUsed: Long,
    val heapTotal: Long,
    val heapMax: Long,
    val nonHeapUsed: Long,
    val nonHeapTotal: Long,
    val directMemoryUsed: Long,
    val directMemoryMax: Long,
    val gcCount: Long,
    val gcTime: Long,
    val usagePercentage: Float,
    val timestamp: Long = getCurrentTimeMillis()
)

/**
 * 内存状态枚举
 */
@Serializable
enum class MemoryStatus {
    NORMAL,
    WARNING,
    CRITICAL,
    LOW_MEMORY,
    OUT_OF_MEMORY,
    OPTIMIZING,
    MONITORING_DISABLED
}

/**
 * 垃圾回收信息
 */
@Serializable
data class GCInfo(
    val youngGenCollections: Long,
    val youngGenTime: Long,
    val oldGenCollections: Long,
    val oldGenTime: Long,
    val totalCollections: Long,
    val totalTime: Long,
    val lastGCTime: Long,
    val averageGCTime: Double
)

/**
 * 内存配置
 */
@Serializable
data class MemoryConfig(
    val warningThreshold: Float = 0.8f,
    val criticalThreshold: Float = 0.9f,
    val autoOptimize: Boolean = true,
    val monitoringInterval: Long = 5000L,
    val enableGCLogging: Boolean = false,
    val maxCacheSize: Long = 50 * 1024 * 1024L, // 50MB
    val enableMemoryProfiling: Boolean = false
)

/**
 * Unify内存管理器实现
 */
// 平台特定的内存管理器函数声明
internal expect suspend fun getPlatformMemoryInfo(): MemoryInfo
internal expect suspend fun getPlatformMemoryUsage(): MemoryUsage
internal expect suspend fun getPlatformGCInfo(): GCInfo
internal expect suspend fun performCacheClear()
internal expect suspend fun performMemoryOptimization()
internal expect suspend fun startMemoryMonitoring()
internal expect suspend fun stopMemoryMonitoring()

class UnifyMemoryManagerImpl(
    private val config: MemoryConfig = MemoryConfig()
) : UnifyMemoryManager {
    
    private val _memoryStatus = MutableStateFlow(MemoryStatus.NORMAL)
    private val memoryStatus: StateFlow<MemoryStatus> = _memoryStatus.asStateFlow()
    
    private var isMonitoringEnabled = false
    private var memoryThreshold = config.maxCacheSize
    
    override suspend fun getMemoryInfo(): MemoryInfo {
        return getPlatformMemoryInfo()
    }
    
    override suspend fun getMemoryUsage(): MemoryUsage {
        return getPlatformMemoryUsage()
    }
    
    override suspend fun clearCache() {
        performCacheClear()
        updateMemoryStatus()
    }
    
    override suspend fun optimizeMemory() {
        performMemoryOptimization()
        updateMemoryStatus()
    }
    
    override suspend fun setMemoryThreshold(threshold: Long) {
        memoryThreshold = threshold
        updateMemoryStatus()
    }
    
    override fun getMemoryStatus(): Flow<MemoryStatus> {
        return memoryStatus
    }
    
    override suspend fun enableMemoryMonitoring(enabled: Boolean) {
        isMonitoringEnabled = enabled
        if (enabled) {
            startMemoryMonitoring()
        } else {
            stopMemoryMonitoring()
            _memoryStatus.value = MemoryStatus.MONITORING_DISABLED
        }
    }
    
    override suspend fun getGarbageCollectionInfo(): GCInfo {
        return getPlatformGCInfo()
    }
    
    private suspend fun updateMemoryStatus() {
        if (!isMonitoringEnabled) return
        
        val usage = getMemoryUsage()
        val newStatus = when {
            usage.usagePercentage >= config.criticalThreshold -> MemoryStatus.CRITICAL
            usage.usagePercentage >= config.warningThreshold -> MemoryStatus.WARNING
            else -> MemoryStatus.NORMAL
        }
        
        _memoryStatus.value = newStatus
        
        if (config.autoOptimize && newStatus == MemoryStatus.CRITICAL) {
            optimizeMemory()
        }
    }
}

/**
 * 内存缓存管理器
 */
interface UnifyMemoryCache {
    suspend fun put(key: String, value: Any, size: Long)
    suspend fun get(key: String): Any?
    suspend fun remove(key: String)
    suspend fun clear()
    suspend fun size(): Long
    suspend fun maxSize(): Long
    suspend fun hitRate(): Float
    suspend fun evictAll()
}

/**
 * LRU内存缓存实现
 */
class UnifyLRUMemoryCache(
    private val maxSize: Long
) : UnifyMemoryCache {
    
    private val cache = mutableMapOf<String, CacheEntry>()
    private val accessOrder = mutableListOf<String>()
    private var currentSize = 0L
    private var hits = 0L
    private var misses = 0L
    
    @Serializable
    private data class CacheEntry(
        val value: String, // 序列化后的值
        val size: Long,
        val timestamp: Long
    )
    
    override suspend fun put(key: String, value: Any, size: Long) {
        val serializedValue = value.toString() // 简化序列化处理
        val entry = CacheEntry(serializedValue, size, getCurrentTimeMillis())
        
        // 如果key已存在，先移除旧值
        remove(key)
        
        // 确保有足够空间
        while (currentSize + size > maxSize && cache.isNotEmpty()) {
            evictLRU()
        }
        
        if (size <= maxSize) {
            cache[key] = entry
            accessOrder.add(key)
            currentSize += size
        }
    }
    
    override suspend fun get(key: String): Any? {
        val entry = cache[key]
        return if (entry != null) {
            hits++
            // 更新访问顺序
            accessOrder.remove(key)
            accessOrder.add(key)
            entry.value // 返回字符串值
        } else {
            misses++
            null
        }
    }
    
    override suspend fun remove(key: String) {
        cache[key]?.let { entry ->
            cache.remove(key)
            accessOrder.remove(key)
            currentSize -= entry.size
        }
    }
    
    override suspend fun clear() {
        cache.clear()
        accessOrder.clear()
        currentSize = 0L
        hits = 0L
        misses = 0L
    }
    
    override suspend fun size(): Long = currentSize
    
    override suspend fun maxSize(): Long = maxSize
    
    override suspend fun hitRate(): Float {
        val total = hits + misses
        return if (total > 0) hits.toFloat() / total else 0f
    }
    
    override suspend fun evictAll() {
        clear()
    }
    
    private fun evictLRU() {
        if (accessOrder.isNotEmpty()) {
            val oldestKey = accessOrder.removeFirst()
            cache[oldestKey]?.let { entry ->
                cache.remove(oldestKey)
                currentSize -= entry.size
            }
        }
    }
}

/**
 * 内存监控器
 */
interface UnifyMemoryMonitor {
    fun onMemoryStatusChanged(status: MemoryStatus)
    fun onMemoryWarning(usage: MemoryUsage)
    fun onOutOfMemory()
    fun onGarbageCollection(gcInfo: GCInfo)
}

/**
 * 内存工具类
 */
object UnifyMemoryUtils {
    /**
     * 格式化内存大小
     */
    fun formatMemorySize(bytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var size = bytes.toDouble()
        var unitIndex = 0
        
        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }
        
        return "%.2f %s".format(size, units[unitIndex])
    }
    
    /**
     * 计算内存使用百分比
     */
    fun calculateUsagePercentage(used: Long, total: Long): Float {
        return if (total > 0) (used.toFloat() / total) * 100f else 0f
    }
    
    /**
     * 检查是否为低内存状态
     */
    fun isLowMemory(available: Long, total: Long, threshold: Float = 0.1f): Boolean {
        return (available.toFloat() / total) < threshold
    }
}
