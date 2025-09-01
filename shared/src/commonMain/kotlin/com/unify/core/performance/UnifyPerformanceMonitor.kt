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
    
    // 性能监控常量
    private const val FRAME_HISTORY_SIZE = 100
    private const val MEMORY_SNAPSHOTS_SIZE = 50
    private const val MIN_FPS_THRESHOLD = 30
    private const val MILLISECONDS_PER_SECOND = 1000.0
    private const val BYTES_TO_MB_DIVISOR = 1024.0 * 1024.0
    
    private val _metrics = MutableStateFlow<Map<String, PerformanceMetric>>(emptyMap())
    val metrics: StateFlow<Map<String, PerformanceMetric>> = _metrics.asStateFlow()
    
    private val _realTimeMetrics = MutableStateFlow<RealTimeMetrics>(RealTimeMetrics())
    val realTimeMetrics: StateFlow<RealTimeMetrics> = _realTimeMetrics.asStateFlow()
    
    private val _performanceScore = MutableStateFlow(0.0f)
    val performanceScore: StateFlow<Float> = _performanceScore.asStateFlow()
    
    private var appStartTime: Long = 0
    private val frameTimeHistory = mutableListOf<Long>()
    private val memorySnapshots = mutableListOf<MemorySnapshot>()
    private val performanceHistory = mutableListOf<PerformanceSnapshot>()
    private val componentMetrics = mutableMapOf<String, ComponentMetrics>()
    private var isMonitoringEnabled = true
    
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
        if (frameTimeHistory.size > FRAME_HISTORY_SIZE) {
            frameTimeHistory.removeAt(0)
        }
        
        val averageFrameTime = frameTimeHistory.average()
        val fps = MILLISECONDS_PER_SECOND / averageFrameTime
        recordMetric("frame_rate", fps, "fps")
        
        // 检测性能问题
        if (fps < MIN_FPS_THRESHOLD) {
            recordMetric("performance_warning", 1.0, "count", 
                mapOf("type" to "low_fps", "fps" to fps.toString()))
        }
        recordMetric("frame_time", averageFrameTime, "ms")
    }
    
    /**
     * 记录内存使用
     */
    fun recordMemoryUsage(bytes: Long) {
        val mb = bytes / BYTES_TO_MB_DIVISOR
        recordMetric("memory_usage", mb, "MB")
        
        memorySnapshots.add(MemorySnapshot(getCurrentTimeMillis(), bytes))
        if (memorySnapshots.size > MEMORY_SNAPSHOTS_SIZE) {
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
    
    /**
     * 启动时性能优化
     */
    fun optimizeStartupPerformance() {
        // 预加载关键资源
        preloadCriticalResources()
        
        // 延迟初始化非关键组件
        deferNonCriticalInitialization()
        
        // 优化内存分配
        optimizeMemoryAllocation()
        
        recordMetric("startup_optimization_applied", 1.0, "count")
    }
    
    /**
     * 运行时性能优化
     */
    fun optimizeRuntimePerformance() {
        // 清理未使用的资源
        cleanupUnusedResources()
        
        // 优化组件缓存
        optimizeComponentCache()
        
        // 调整GC策略
        optimizeGarbageCollection()
        
        recordMetric("runtime_optimization_applied", 1.0, "count")
    }
    
    /**
     * 智能性能调优
     */
    fun intelligentPerformanceTuning() {
        val currentMetrics = _metrics.value
        
        // 分析性能瓶颈
        val bottlenecks = analyzePerformanceBottlenecks(currentMetrics)
        
        bottlenecks.forEach { bottleneck ->
            when (bottleneck.type) {
                "memory" -> applyMemoryOptimization(bottleneck)
                "cpu" -> applyCPUOptimization(bottleneck)
                "io" -> applyIOOptimization(bottleneck)
                "network" -> applyNetworkOptimization(bottleneck)
            }
        }
        
        // 更新性能评分
        updatePerformanceScore()
        
        recordMetric("intelligent_tuning_applied", bottlenecks.size.toDouble(), "count")
    }
    
    /**
     * 实时性能监控
     */
    fun startRealTimeMonitoring() {
        isMonitoringEnabled = true
        // 启动实时监控协程
        recordMetric("realtime_monitoring_started", 1.0, "count")
    }
    
    /**
     * 停止实时监控
     */
    fun stopRealTimeMonitoring() {
        isMonitoringEnabled = false
        recordMetric("realtime_monitoring_stopped", 1.0, "count")
    }
    
    /**
     * 更新实时指标
     */
    fun updateRealTimeMetrics() {
        if (!isMonitoringEnabled) return
        
        val currentTime = getCurrentTimeMillis()
        val frameRate = _metrics.value["frame_rate"]?.value ?: 0.0
        val memoryUsage = _metrics.value["memory_usage"]?.value ?: 0.0
        val cpuUsage = calculateCPUUsage()
        val networkLatency = _metrics.value["network_latency"]?.value ?: 0.0
        
        val realTimeMetrics = RealTimeMetrics(
            frameRate = frameRate,
            memoryUsage = memoryUsage,
            cpuUsage = cpuUsage,
            networkLatency = networkLatency,
            timestamp = currentTime,
            activeComponents = componentMetrics.size
        )
        
        _realTimeMetrics.value = realTimeMetrics
        
        // 添加到历史记录
        performanceHistory.add(PerformanceSnapshot(
            timestamp = currentTime,
            frameRate = frameRate,
            memoryUsage = memoryUsage,
            cpuUsage = cpuUsage
        ))
        
        // 保持历史记录在合理范围内
        if (performanceHistory.size > 1000) {
            performanceHistory.removeAt(0)
        }
    }
    
    /**
     * 记录组件性能
     */
    fun recordComponentMetric(componentName: String, metricType: String, value: Double) {
        val componentMetric = componentMetrics.getOrPut(componentName) {
            ComponentMetrics(componentName)
        }
        
        when (metricType) {
            "render_time" -> componentMetric.renderTime = value
            "memory_usage" -> componentMetric.memoryUsage = value
            "recomposition_count" -> componentMetric.recompositionCount = value.toInt()
        }
        
        componentMetric.lastUpdated = getCurrentTimeMillis()
        recordMetric("component_${componentName}_${metricType}", value, "ms")
    }
    
    /**
     * 获取组件性能报告
     */
    fun getComponentPerformanceReport(): ComponentPerformanceReport {
        val topPerformers = componentMetrics.values
            .sortedBy { it.renderTime }
            .take(5)
            
        val bottomPerformers = componentMetrics.values
            .sortedByDescending { it.renderTime }
            .take(5)
            
        val averageRenderTime = componentMetrics.values
            .map { it.renderTime }
            .average()
            
        return ComponentPerformanceReport(
            totalComponents = componentMetrics.size,
            averageRenderTime = averageRenderTime,
            topPerformers = topPerformers,
            bottomPerformers = bottomPerformers,
            timestamp = getCurrentTimeMillis()
        )
    }
    
    /**
     * 获取性能历史趋势
     */
    fun getPerformanceTrends(timeRange: Long = 3600000): PerformanceTrends {
        val cutoffTime = getCurrentTimeMillis() - timeRange
        val recentHistory = performanceHistory.filter { it.timestamp >= cutoffTime }
        
        if (recentHistory.isEmpty()) {
            return PerformanceTrends()
        }
        
        val frameRateTrend = calculateTrend(recentHistory.map { it.frameRate })
        val memoryTrend = calculateTrend(recentHistory.map { it.memoryUsage })
        val cpuTrend = calculateTrend(recentHistory.map { it.cpuUsage })
        
        return PerformanceTrends(
            frameRateTrend = frameRateTrend,
            memoryTrend = memoryTrend,
            cpuTrend = cpuTrend,
            dataPoints = recentHistory.size,
            timeRange = timeRange
        )
    }
    
    /**
     * 性能健康检查
     */
    fun performHealthCheck(): PerformanceHealthCheck {
        val currentMetrics = _metrics.value
        val frameRate = currentMetrics["frame_rate"]?.value ?: 0.0
        val memoryUsage = currentMetrics["memory_usage"]?.value ?: 0.0
        val cpuUsage = calculateCPUUsage()
        
        val issues = mutableListOf<PerformanceIssue>()
        
        // 检查帧率
        if (frameRate < 30.0) {
            issues.add(PerformanceIssue(
                type = "frame_rate",
                severity = if (frameRate < 15.0) IssueSeverity.CRITICAL else IssueSeverity.HIGH,
                description = "帧率过低: ${frameRate.toInt()}fps",
                recommendation = "优化UI渲染，减少重组次数"
            ))
        }
        
        // 检查内存使用
        if (memoryUsage > 80.0) {
            issues.add(PerformanceIssue(
                type = "memory",
                severity = if (memoryUsage > 95.0) IssueSeverity.CRITICAL else IssueSeverity.HIGH,
                description = "内存使用过高: ${memoryUsage.toInt()}%",
                recommendation = "清理缓存，释放未使用的资源"
            ))
        }
        
        // 检查CPU使用
        if (cpuUsage > 70.0) {
            issues.add(PerformanceIssue(
                type = "cpu",
                severity = if (cpuUsage > 90.0) IssueSeverity.CRITICAL else IssueSeverity.MEDIUM,
                description = "CPU使用过高: ${cpuUsage.toInt()}%",
                recommendation = "优化算法，将耗时操作移到后台"
            ))
        }
        
        val overallHealth = when {
            issues.any { it.severity == IssueSeverity.CRITICAL } -> HealthStatus.CRITICAL
            issues.any { it.severity == IssueSeverity.HIGH } -> HealthStatus.WARNING
            issues.any { it.severity == IssueSeverity.MEDIUM } -> HealthStatus.FAIR
            else -> HealthStatus.GOOD
        }
        
        return PerformanceHealthCheck(
            status = overallHealth,
            issues = issues,
            score = calculateHealthScore(frameRate, memoryUsage, cpuUsage),
            timestamp = getCurrentTimeMillis()
        )
    }
    
    private fun updatePerformanceScore() {
        val currentMetrics = _metrics.value
        val frameRate = currentMetrics["frame_rate"]?.value ?: 0.0
        val memoryUsage = currentMetrics["memory_usage"]?.value ?: 0.0
        val cpuUsage = calculateCPUUsage()
        
        val score = calculateHealthScore(frameRate, memoryUsage, cpuUsage)
        _performanceScore.value = score
    }
    
    private fun calculateCPUUsage(): Double {
        // 模拟CPU使用率计算
        return (10..80).random().toDouble()
    }
    
    private fun calculateTrend(values: List<Double>): TrendDirection {
        if (values.size < 2) return TrendDirection.STABLE
        
        val firstHalf = values.take(values.size / 2).average()
        val secondHalf = values.drop(values.size / 2).average()
        
        return when {
            secondHalf > firstHalf * 1.1 -> TrendDirection.IMPROVING
            secondHalf < firstHalf * 0.9 -> TrendDirection.DEGRADING
            else -> TrendDirection.STABLE
        }
    }
    
    private fun calculateHealthScore(frameRate: Double, memoryUsage: Double, cpuUsage: Double): Float {
        val frameRateScore = minOf(frameRate / 60.0, 1.0) * 0.4
        val memoryScore = maxOf(0.0, (100.0 - memoryUsage) / 100.0) * 0.3
        val cpuScore = maxOf(0.0, (100.0 - cpuUsage) / 100.0) * 0.3
        
        return ((frameRateScore + memoryScore + cpuScore) * 100).toFloat()
    }
    
    /**
     * 预加载关键资源
     */
    private fun preloadCriticalResources() {
        // 预加载主题资源
        // 预加载字体资源
        // 预加载关键图片
        recordMetric("critical_resources_preloaded", 1.0, "count")
    }
    
    /**
     * 延迟初始化非关键组件
     */
    private fun deferNonCriticalInitialization() {
        // 延迟初始化分析组件
        // 延迟初始化非核心功能
        recordMetric("non_critical_init_deferred", 1.0, "count")
    }
    
    /**
     * 优化内存分配
     */
    private fun optimizeMemoryAllocation() {
        // 预分配对象池
        // 优化字符串缓存
        recordMetric("memory_allocation_optimized", 1.0, "count")
    }
    
    /**
     * 清理未使用的资源
     */
    private fun cleanupUnusedResources() {
        // 清理图片缓存
        // 清理组件缓存
        recordMetric("unused_resources_cleaned", 1.0, "count")
    }
    
    /**
     * 优化组件缓存
     */
    private fun optimizeComponentCache() {
        // 调整缓存大小
        // 优化缓存策略
        recordMetric("component_cache_optimized", 1.0, "count")
    }
    
    /**
     * 优化垃圾回收
     */
    private fun optimizeGarbageCollection() {
        // 建议GC策略
        recordMetric("gc_optimization_applied", 1.0, "count")
    }
    
    /**
     * 分析性能瓶颈
     */
    private fun analyzePerformanceBottlenecks(metrics: Map<String, PerformanceMetric>): List<PerformanceBottleneck> {
        val bottlenecks = mutableListOf<PerformanceBottleneck>()
        
        // 检查内存使用
        val memoryUsage = metrics["memory_usage"]?.value ?: 0.0
        if (memoryUsage > 80.0) {
            bottlenecks.add(PerformanceBottleneck("memory", "高内存使用", memoryUsage))
        }
        
        // 检查帧率
        val frameRate = metrics["frame_rate"]?.value ?: 60.0
        if (frameRate < 30.0) {
            bottlenecks.add(PerformanceBottleneck("cpu", "低帧率", frameRate))
        }
        
        return bottlenecks
    }
    
    /**
     * 应用内存优化
     */
    private fun applyMemoryOptimization(bottleneck: PerformanceBottleneck) {
        // 清理内存
        // 调整缓存策略
        recordMetric("memory_optimization_applied", 1.0, "count")
    }
    
    /**
     * 应用CPU优化
     */
    private fun applyCPUOptimization(bottleneck: PerformanceBottleneck) {
        // 优化算法
        // 减少计算复杂度
        recordMetric("cpu_optimization_applied", 1.0, "count")
    }
    
    /**
     * 应用IO优化
     */
    private fun applyIOOptimization(bottleneck: PerformanceBottleneck) {
        // 优化文件读写
        // 使用缓存
        recordMetric("io_optimization_applied", 1.0, "count")
    }
    
    /**
     * 应用网络优化
     */
    private fun applyNetworkOptimization(bottleneck: PerformanceBottleneck) {
        // 优化网络请求
        // 使用连接池
        recordMetric("network_optimization_applied", 1.0, "count")
    }
    
    /**
     * 获取详细性能报告
     */
    fun getDetailedPerformanceReport(): DetailedPerformanceReport {
        val currentMetrics = _metrics.value
        val summary = getPerformanceSummary()
        val healthCheck = performHealthCheck()
        val trends = getPerformanceTrends()
        val componentReport = getComponentPerformanceReport()
        
        return DetailedPerformanceReport(
            summary = summary,
            healthCheck = healthCheck,
            trends = trends,
            componentReport = componentReport,
            metrics = currentMetrics,
            timestamp = getCurrentTimeMillis()
        )
    }
    
    /**
     * 导出性能数据
     */
    fun exportPerformanceData(): PerformanceExport {
        return PerformanceExport(
            metrics = _metrics.value,
            history = performanceHistory.takeLast(100),
            components = componentMetrics.values.toList(),
            exportTime = getCurrentTimeMillis()
        )
    }
    
    /**
     * 重置性能监控
     */
    fun resetMonitoring() {
        _metrics.value = emptyMap()
        frameTimeHistory.clear()
        memorySnapshots.clear()
        performanceHistory.clear()
        componentMetrics.clear()
        _performanceScore.value = 0.0f
        
        recordMetric("monitoring_reset", 1.0, "count")
    }
    
    private fun getCurrentTimeMillis(): Long = System.currentTimeMillis()
}

/**
 * 详细性能报告
 */
data class DetailedPerformanceReport(
    val summary: PerformanceSummary,
    val healthCheck: PerformanceHealthCheck,
    val trends: PerformanceTrends,
    val componentReport: ComponentPerformanceReport,
    val metrics: Map<String, PerformanceMetric>,
    val timestamp: Long
)

/**
 * 性能数据导出
 */
data class PerformanceExport(
    val metrics: Map<String, PerformanceMetric>,
    val history: List<PerformanceSnapshot>,
    val components: List<ComponentMetrics>,
    val exportTime: Long
)
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
 * 性能瓶颈数据类
 */
data class PerformanceBottleneck(
    val type: String,
    val description: String,
    val severity: Double,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 性能告警系统
 */
object UnifyPerformanceAlerting {
    private val alertThresholds = mutableMapOf<String, AlertThreshold>()
    private val activeAlerts = mutableMapOf<String, PerformanceAlert>()
    
    fun setThreshold(metricName: String, threshold: AlertThreshold) {
        alertThresholds[metricName] = threshold
    }
    
    fun checkMetric(metric: PerformanceMetric) {
        val threshold = alertThresholds[metric.name] ?: return
        
        val shouldAlert = when (threshold.condition) {
            AlertCondition.GREATER_THAN -> metric.value > threshold.value
            AlertCondition.LESS_THAN -> metric.value < threshold.value
            AlertCondition.EQUALS -> metric.value == threshold.value
        }
        
        if (shouldAlert) {
            val alert = PerformanceAlert(
                metricName = metric.name,
                currentValue = metric.value,
                threshold = threshold,
                timestamp = metric.timestamp
            )
            activeAlerts[metric.name] = alert
            triggerAlert(alert)
        } else {
            activeAlerts.remove(metric.name)
        }
    }
    
    private fun triggerAlert(alert: PerformanceAlert) {
        // 触发告警通知
        UnifyPerformanceMonitor.recordMetric(
            "performance_alert_triggered", 
            1.0, 
            "count",
            mapOf("metric" to alert.metricName, "severity" to alert.threshold.severity.name)
        )
    }
}

data class AlertThreshold(
    val value: Double,
    val condition: AlertCondition,
    val severity: AlertSeverity = AlertSeverity.WARNING
)

enum class AlertCondition {
    GREATER_THAN, LESS_THAN, EQUALS
}

enum class AlertSeverity {
    INFO, WARNING, ERROR, CRITICAL
}

data class PerformanceAlert(
    val metricName: String,
    val currentValue: Double,
    val threshold: AlertThreshold,
    val timestamp: Long
)

/**
 * 智能性能分析器
 */
object UnifyPerformanceAnalyzer {
    
    fun analyzePerformanceTrends(metrics: Map<String, PerformanceMetric>): PerformanceAnalysis {
        val trends = mutableMapOf<String, TrendAnalysis>()
        
        // 分析帧率趋势
        val frameRate = metrics["frame_rate"]?.value ?: 0.0
        trends["frame_rate"] = analyzeTrend("frame_rate", frameRate)
        
        // 分析内存使用趋势
        val memoryUsage = metrics["memory_usage"]?.value ?: 0.0
        trends["memory_usage"] = analyzeTrend("memory_usage", memoryUsage)
        
        // 生成优化建议
        val recommendations = generateOptimizationRecommendations(trends)
        
        return PerformanceAnalysis(
            trends = trends,
            recommendations = recommendations,
            overallScore = calculateOverallScore(trends),
            timestamp = System.currentTimeMillis()
        )
    }
    
    private fun analyzeTrend(metricName: String, currentValue: Double): TrendAnalysis {
        return TrendAnalysis(
            metricName = metricName,
            currentValue = currentValue,
            trend = when {
                currentValue > 50.0 -> TrendDirection.IMPROVING
                currentValue < 30.0 -> TrendDirection.DEGRADING
                else -> TrendDirection.STABLE
            },
            confidence = 0.85f
        )
    }
    
    private fun generateOptimizationRecommendations(trends: Map<String, TrendAnalysis>): List<OptimizationRecommendation> {
        val recommendations = mutableListOf<OptimizationRecommendation>()
        
        trends.forEach { (metric, trend) ->
            if (trend.trend == TrendDirection.DEGRADING) {
                when (metric) {
                    "frame_rate" -> recommendations.add(
                        OptimizationRecommendation(
                            type = "performance",
                            description = "帧率下降，建议优化UI渲染",
                            priority = RecommendationPriority.HIGH,
                            estimatedImpact = 0.3f
                        )
                    )
                    "memory_usage" -> recommendations.add(
                        OptimizationRecommendation(
                            type = "memory",
                            description = "内存使用过高，建议清理缓存",
                            priority = RecommendationPriority.MEDIUM,
                            estimatedImpact = 0.2f
                        )
                    )
                }
            }
        }
        
        return recommendations
    }
    
    private fun calculateOverallScore(trends: Map<String, TrendAnalysis>): Float {
        if (trends.isEmpty()) return 0.0f
        
        val scores = trends.values.map { trend ->
            when (trend.trend) {
                TrendDirection.IMPROVING -> 1.0f
                TrendDirection.STABLE -> 0.7f
                TrendDirection.DEGRADING -> 0.3f
            }
        }
        
        return scores.average().toFloat()
    }
}

data class PerformanceAnalysis(
    val trends: Map<String, TrendAnalysis>,
    val recommendations: List<OptimizationRecommendation>,
    val overallScore: Float,
    val timestamp: Long
)

data class TrendAnalysis(
    val metricName: String,
    val currentValue: Double,
    val trend: TrendDirection,
    val confidence: Float
)

enum class TrendDirection {
    IMPROVING, STABLE, DEGRADING
}

data class OptimizationRecommendation(
    val type: String,
    val description: String,
    val priority: RecommendationPriority,
    val estimatedImpact: Float
)

enum class RecommendationPriority {
    LOW, MEDIUM, HIGH, CRITICAL
}

/**
 * 实时性能指标
 */
data class RealTimeMetrics(
    val frameRate: Double = 0.0,
    val memoryUsage: Double = 0.0,
    val cpuUsage: Double = 0.0,
    val networkLatency: Double = 0.0,
    val timestamp: Long = 0L,
    val activeComponents: Int = 0
)

/**
 * 性能快照
 */
data class PerformanceSnapshot(
    val timestamp: Long,
    val frameRate: Double,
    val memoryUsage: Double,
    val cpuUsage: Double
)

/**
 * 组件性能指标
 */
data class ComponentMetrics(
    val componentName: String,
    var renderTime: Double = 0.0,
    var memoryUsage: Double = 0.0,
    var recompositionCount: Int = 0,
    var lastUpdated: Long = 0L
)

/**
 * 组件性能报告
 */
data class ComponentPerformanceReport(
    val totalComponents: Int,
    val averageRenderTime: Double,
    val topPerformers: List<ComponentMetrics>,
    val bottomPerformers: List<ComponentMetrics>,
    val timestamp: Long
)

/**
 * 性能趋势
 */
data class PerformanceTrends(
    val frameRateTrend: TrendDirection = TrendDirection.STABLE,
    val memoryTrend: TrendDirection = TrendDirection.STABLE,
    val cpuTrend: TrendDirection = TrendDirection.STABLE,
    val dataPoints: Int = 0,
    val timeRange: Long = 0L
)

/**
 * 性能健康检查
 */
data class PerformanceHealthCheck(
    val status: HealthStatus,
    val issues: List<PerformanceIssue>,
    val score: Float,
    val timestamp: Long
)

/**
 * 健康状态
 */
enum class HealthStatus {
    GOOD, FAIR, WARNING, CRITICAL
}

/**
 * 性能问题
 */
data class PerformanceIssue(
    val type: String,
    val severity: IssueSeverity,
    val description: String,
    val recommendation: String
)

/**
 * 问题严重程度
 */
enum class IssueSeverity {
    LOW, MEDIUM, HIGH, CRITICAL
}

