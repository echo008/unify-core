package com.unify.core.performance

import androidx.compose.runtime.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Unify-Core 生产级性能监控和优化系统
 * 支持编译时和运行时性能优化
 */

/**
 * 1. 核心性能监控器
 */
object UnifyPerformanceMonitor {
    
    private val _metrics = MutableStateFlow<Map<String, PerformanceMetric>>(emptyMap())
    val metrics: StateFlow<Map<String, PerformanceMetric>> = _metrics.asStateFlow()
    
    private var appStartTime: Long = 0
    private val frameTimeHistory = mutableListOf<Long>()
    private val memorySnapshots = mutableListOf<MemorySnapshot>()
    
    /**
     * 初始化性能监控
     */
    fun initialize() {
        appStartTime = getCurrentTimeMillis()
        recordMetric("app_start_time", appStartTime.toDouble(), "ms")
        startMemoryMonitoring()
    }
    
    /**
     * 记录性能指标
     */
    fun recordMetric(name: String, value: Double, unit: String = "", tags: Map<String, String> = emptyMap()) {
        val currentMetrics = _metrics.value.toMutableMap()
        currentMetrics[name] = PerformanceMetric(
            name = name,
            value = value,
            unit = unit,
            timestamp = getCurrentTimeMillis(),
            tags = tags
        )
        _metrics.value = currentMetrics
    }
    
    /**
     * 开始性能测量
     */
    fun startMeasurement(name: String): PerformanceMeasurement {
        val startTime = getCurrentTimeMillis()
        return PerformanceMeasurement(name, startTime)
    }
    
    /**
     * 记录 Compose 重组性能
     */
    fun recordRecomposition(composableName: String, duration: Long) {
        recordMetric(
            "recomposition_$composableName", 
            duration.toDouble(), 
            "ms",
            mapOf("type" to "recomposition", "composable" to composableName)
        )
    }
    
    /**
     * 记录帧率
     */
    fun recordFrameTime(frameTime: Long) {
        frameTimeHistory.add(frameTime)
        if (frameTimeHistory.size > 100) {
            frameTimeHistory.removeAt(0)
        }
        
        val averageFrameTime = frameTimeHistory.average()
        val fps = 1000.0 / averageFrameTime
        recordMetric("frame_rate", fps, "fps")
        recordMetric("frame_time", averageFrameTime, "ms")
    }
    
    /**
     * 记录内存使用
     */
    fun recordMemoryUsage(bytes: Long) {
        val mb = bytes / (1024.0 * 1024.0)
        recordMetric("memory_usage", mb, "MB")
        
        memorySnapshots.add(MemorySnapshot(getCurrentTimeMillis(), bytes))
        if (memorySnapshots.size > 50) {
            memorySnapshots.removeAt(0)
        }
    }
    
    /**
     * 记录网络请求性能
     */
    fun recordNetworkRequest(url: String, duration: Long, success: Boolean, responseSize: Long = 0) {
        recordMetric(
            "network_request_duration", 
            duration.toDouble(), 
            "ms",
            mapOf("url" to url, "success" to success.toString())
        )
        
        if (responseSize > 0) {
            recordMetric(
                "network_response_size",
                responseSize.toDouble(),
                "bytes",
                mapOf("url" to url)
            )
        }
    }
    
    /**
     * 获取性能摘要
     */
    fun getPerformanceSummary(): PerformanceSummary {
        val currentMetrics = _metrics.value
        val frameRate = currentMetrics["frame_rate"]?.value ?: 0.0
        val memoryUsage = currentMetrics["memory_usage"]?.value ?: 0.0
        val networkRequests = currentMetrics.keys.count { it.startsWith("network_request_") }
        
        return PerformanceSummary(
            appStartTime = appStartTime,
            averageFrameRate = frameRate,
            memoryUsage = memoryUsage,
            networkRequestCount = networkRequests,
            recompositionCount = currentMetrics.keys.count { it.startsWith("recomposition_") }
        )
    }
    
    private fun startMemoryMonitoring() {
        // 平台特定的内存监控实现
    }
    
    private fun getCurrentTimeMillis(): Long = System.currentTimeMillis()
}

/**
 * 2. Compose 性能优化工具
 */
object UnifyComposeOptimizer {
    
    /**
     * 稳定性标记 - 帮助 Compose 编译器优化
     */
    @Stable
    data class StableData<T>(val value: T)
    
    /**
     * 不可变集合包装器
     */
    @Immutable
    data class ImmutableList<T>(val items: List<T>)
    
    /**
     * 记忆化计算
     */
    @Composable
    fun <T> rememberCalculation(
        vararg keys: Any?,
        calculation: () -> T
    ): T {
        return remember(*keys) { calculation() }
    }
    
    /**
     * 延迟计算
     */
    @Composable
    fun <T> rememberLazyCalculation(
        vararg keys: Any?,
        calculation: () -> T
    ): Lazy<T> {
        return remember(*keys) { lazy { calculation() } }
    }
    
    /**
     * 性能监控装饰器
     */
    @Composable
    fun PerformanceTracker(
        name: String,
        content: @Composable () -> Unit
    ) {
        val measurement = remember { UnifyPerformanceMonitor.startMeasurement("compose_$name") }
        
        DisposableEffect(name) {
            onDispose {
                measurement.end()
            }
        }
        
        content()
    }
}

/**
 * 3. 内存优化工具
 */
object UnifyMemoryOptimizer {
    
    /**
     * 图片内存优化
     */
    fun optimizeImageMemory(imageSize: Long): Long {
        // 根据设备性能调整图片质量
        return when {
            imageSize > 5 * 1024 * 1024 -> imageSize / 2 // 大于5MB压缩50%
            imageSize > 2 * 1024 * 1024 -> imageSize * 3 / 4 // 大于2MB压缩25%
            else -> imageSize
        }
    }
    
    /**
     * 列表内存优化
     */
    fun <T> optimizeListMemory(list: List<T>, maxSize: Int = 1000): List<T> {
        return if (list.size > maxSize) {
            list.takeLast(maxSize)
        } else {
            list
        }
    }
    
    /**
     * 缓存清理
     */
    fun clearCache() {
        // 实现缓存清理逻辑
        System.gc() // 建议垃圾回收
    }
}

/**
 * 4. 网络性能优化
 */
object UnifyNetworkOptimizer {
    
    /**
     * 请求批处理
     */
    class RequestBatcher<T> {
        private val pendingRequests = mutableListOf<T>()
        private var batchTimer: Long = 0
        
        fun addRequest(request: T) {
            pendingRequests.add(request)
            if (batchTimer == 0L) {
                batchTimer = System.currentTimeMillis()
            }
        }
        
        fun shouldFlush(maxBatchSize: Int = 10, maxWaitTime: Long = 100): Boolean {
            return pendingRequests.size >= maxBatchSize || 
                   (System.currentTimeMillis() - batchTimer) >= maxWaitTime
        }
        
        fun flush(): List<T> {
            val batch = pendingRequests.toList()
            pendingRequests.clear()
            batchTimer = 0
            return batch
        }
    }
    
    /**
     * 响应缓存
     */
    class ResponseCache<K, V>(private val maxSize: Int = 100) {
        private val cache = mutableMapOf<K, CacheEntry<V>>()
        
        fun get(key: K, maxAge: Long = 5 * 60 * 1000): V? { // 默认5分钟过期
            val entry = cache[key] ?: return null
            return if (System.currentTimeMillis() - entry.timestamp <= maxAge) {
                entry.value
            } else {
                cache.remove(key)
                null
            }
        }
        
        fun put(key: K, value: V) {
            if (cache.size >= maxSize) {
                val oldestKey = cache.keys.first()
                cache.remove(oldestKey)
            }
            cache[key] = CacheEntry(value, System.currentTimeMillis())
        }
        
        private data class CacheEntry<V>(val value: V, val timestamp: Long)
    }
}

/**
 * 5. 启动优化
 */
object UnifyStartupOptimizer {
    
    private val initTasks = mutableListOf<suspend () -> Unit>()
    private val criticalTasks = mutableListOf<suspend () -> Unit>()
    
    /**
     * 添加初始化任务
     */
    fun addInitTask(task: suspend () -> Unit) {
        initTasks.add(task)
    }
    
    /**
     * 添加关键任务
     */
    fun addCriticalTask(task: suspend () -> Unit) {
        criticalTasks.add(task)
    }
    
    /**
     * 执行启动优化
     */
    suspend fun executeStartupTasks() {
        val startTime = System.currentTimeMillis()
        
        // 先执行关键任务
        criticalTasks.forEach { task ->
            val taskStart = System.currentTimeMillis()
            task()
            val taskDuration = System.currentTimeMillis() - taskStart
            UnifyPerformanceMonitor.recordMetric("startup_critical_task", taskDuration.toDouble(), "ms")
        }
        
        // 后台执行非关键任务
        initTasks.forEach { task ->
            val taskStart = System.currentTimeMillis()
            task()
            val taskDuration = System.currentTimeMillis() - taskStart
            UnifyPerformanceMonitor.recordMetric("startup_init_task", taskDuration.toDouble(), "ms")
        }
        
        val totalDuration = System.currentTimeMillis() - startTime
        UnifyPerformanceMonitor.recordMetric("startup_total_time", totalDuration.toDouble(), "ms")
    }
}

/**
 * 6. 数据类定义
 */
class PerformanceMeasurement(
    private val name: String,
    private val startTime: Long
) {
    fun end(): Long {
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        UnifyPerformanceMonitor.recordMetric(name, duration.toDouble(), "ms")
        return duration
    }
}

data class PerformanceMetric(
    val name: String,
    val value: Double,
    val unit: String,
    val timestamp: Long,
    val tags: Map<String, String> = emptyMap()
)

data class PerformanceSummary(
    val appStartTime: Long,
    val averageFrameRate: Double,
    val memoryUsage: Double,
    val networkRequestCount: Int,
    val recompositionCount: Int = 0
)

data class MemorySnapshot(
    val timestamp: Long,
    val bytes: Long
)

/**
 * 性能事件密封类
 */
sealed class PerformanceEvent {
    data class MetricRecorded(val metric: PerformanceMetric) : PerformanceEvent()
    object MetricsCleared : PerformanceEvent()
}

/**
 * 性能报告数据类
 */
data class PerformanceReport(
    val timestamp: Long,
    val metrics: Map<String, PerformanceMetric>,
    val summary: PerformanceSummary
)

/**
 * 性能摘要数据类
 */
data class PerformanceSummary(
    val appStartTime: Double,
    val averageFrameRate: Double,
    val memoryUsage: Double,
    val networkRequestCount: Int
)
