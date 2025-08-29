package com.unify.performance

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.*
import kotlinx.serialization.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Unify 性能监控和分析系统
 * 提供全面的性能指标收集、分析和报告功能
 */

/**
 * 性能指标类型
 */
enum class PerformanceMetricType {
    RENDER_TIME,           // 渲染时间
    NETWORK_LATENCY,       // 网络延迟
    DATABASE_QUERY_TIME,   // 数据库查询时间
    MEMORY_USAGE,          // 内存使用
    CPU_USAGE,             // CPU使用率
    BATTERY_USAGE,         // 电池使用
    FRAME_RATE,            // 帧率
    STARTUP_TIME,          // 启动时间
    NAVIGATION_TIME,       // 页面导航时间
    API_RESPONSE_TIME,     // API响应时间
    CACHE_HIT_RATE,        // 缓存命中率
    ERROR_RATE,            // 错误率
    USER_INTERACTION_TIME, // 用户交互响应时间
    BUNDLE_SIZE,           // 包大小
    LOAD_TIME              // 加载时间
}

/**
 * 性能指标数据
 */
@Serializable
data class PerformanceMetric(
    val id: String,
    val type: PerformanceMetricType,
    val name: String,
    val value: Double,
    val unit: String,
    val timestamp: Long,
    val tags: Map<String, String> = emptyMap(),
    val metadata: Map<String, String> = emptyMap()
)

/**
 * 性能报告
 */
@Serializable
data class PerformanceReport(
    val id: String,
    val startTime: Long,
    val endTime: Long,
    val metrics: List<PerformanceMetric>,
    val summary: PerformanceSummary,
    val recommendations: List<PerformanceRecommendation>
)

/**
 * 性能摘要
 */
@Serializable
data class PerformanceSummary(
    val totalMetrics: Int,
    val averageRenderTime: Double,
    val averageNetworkLatency: Double,
    val memoryUsagePeak: Double,
    val cpuUsageAverage: Double,
    val errorCount: Int,
    val performanceScore: Double // 0-100分
)

/**
 * 性能建议
 */
@Serializable
data class PerformanceRecommendation(
    val type: RecommendationType,
    val title: String,
    val description: String,
    val priority: RecommendationPriority,
    val impact: String,
    val actionItems: List<String>
)

enum class RecommendationType {
    RENDER_OPTIMIZATION,
    NETWORK_OPTIMIZATION,
    MEMORY_OPTIMIZATION,
    CPU_OPTIMIZATION,
    BATTERY_OPTIMIZATION,
    CODE_SPLITTING,
    CACHING_STRATEGY,
    DATABASE_OPTIMIZATION
}

enum class RecommendationPriority {
    LOW, MEDIUM, HIGH, CRITICAL
}

/**
 * 性能监控器主类
 */
class UnifyPerformanceMonitor {
    private val _metrics = MutableSharedFlow<PerformanceMetric>()
    val metrics: SharedFlow<PerformanceMetric> = _metrics.asSharedFlow()
    
    private val metricsHistory = mutableListOf<PerformanceMetric>()
    private val activeTimers = mutableMapOf<String, Long>()
    private val performanceThresholds = mutableMapOf<PerformanceMetricType, Double>()
    
    private var isMonitoring = false
    private var monitoringJob: Job? = null
    
    init {
        setupDefaultThresholds()
    }
    
    /**
     * 开始性能监控
     */
    fun startMonitoring() {
        if (isMonitoring) return
        
        isMonitoring = true
        monitoringJob = CoroutineScope(Dispatchers.Default).launch {
            startPeriodicCollection()
        }
    }
    
    /**
     * 停止性能监控
     */
    fun stopMonitoring() {
        isMonitoring = false
        monitoringJob?.cancel()
        monitoringJob = null
    }
    
    /**
     * 记录性能指标
     */
    suspend fun recordMetric(
        type: PerformanceMetricType,
        name: String,
        value: Double,
        unit: String,
        tags: Map<String, String> = emptyMap(),
        metadata: Map<String, String> = emptyMap()
    ) {
        val metric = PerformanceMetric(
            id = generateMetricId(),
            type = type,
            name = name,
            value = value,
            unit = unit,
            timestamp = Clock.System.now().toEpochMilliseconds(),
            tags = tags,
            metadata = metadata
        )
        
        metricsHistory.add(metric)
        _metrics.emit(metric)
        
        // 检查阈值告警
        checkThresholdAlert(metric)
    }
    
    /**
     * 开始计时器
     */
    fun startTimer(name: String) {
        activeTimers[name] = Clock.System.now().toEpochMilliseconds()
    }
    
    /**
     * 结束计时器并记录指标
     */
    suspend fun endTimer(
        name: String,
        type: PerformanceMetricType,
        tags: Map<String, String> = emptyMap()
    ) {
        val startTime = activeTimers.remove(name)
        if (startTime != null) {
            val duration = Clock.System.now().toEpochMilliseconds() - startTime
            recordMetric(
                type = type,
                name = name,
                value = duration.toDouble(),
                unit = "ms",
                tags = tags
            )
        }
    }
    
    /**
     * 测量代码块执行时间
     */
    suspend inline fun <T> measure(
        name: String,
        type: PerformanceMetricType,
        tags: Map<String, String> = emptyMap(),
        block: () -> T
    ): T {
        val startTime = Clock.System.now().toEpochMilliseconds()
        val result = block()
        val endTime = Clock.System.now().toEpochMilliseconds()
        
        recordMetric(
            type = type,
            name = name,
            value = (endTime - startTime).toDouble(),
            unit = "ms",
            tags = tags
        )
        
        return result
    }
    
    /**
     * 测量异步代码块执行时间
     */
    suspend inline fun <T> measureSuspend(
        name: String,
        type: PerformanceMetricType,
        tags: Map<String, String> = emptyMap(),
        crossinline block: suspend () -> T
    ): T {
        val startTime = Clock.System.now().toEpochMilliseconds()
        val result = block()
        val endTime = Clock.System.now().toEpochMilliseconds()
        
        recordMetric(
            type = type,
            name = name,
            value = (endTime - startTime).toDouble(),
            unit = "ms",
            tags = tags
        )
        
        return result
    }
    
    /**
     * 生成性能报告
     */
    fun generateReport(
        startTime: Long = 0,
        endTime: Long = Clock.System.now().toEpochMilliseconds()
    ): PerformanceReport {
        val filteredMetrics = metricsHistory.filter { metric ->
            metric.timestamp >= startTime && metric.timestamp <= endTime
        }
        
        val summary = generateSummary(filteredMetrics)
        val recommendations = generateRecommendations(filteredMetrics, summary)
        
        return PerformanceReport(
            id = generateReportId(),
            startTime = startTime,
            endTime = endTime,
            metrics = filteredMetrics,
            summary = summary,
            recommendations = recommendations
        )
    }
    
    /**
     * 获取指标统计信息
     */
    fun getMetricStats(
        type: PerformanceMetricType,
        timeRange: Duration = 1.hours
    ): MetricStats {
        val cutoffTime = Clock.System.now().toEpochMilliseconds() - timeRange.inWholeMilliseconds
        val relevantMetrics = metricsHistory.filter { 
            it.type == type && it.timestamp >= cutoffTime 
        }
        
        if (relevantMetrics.isEmpty()) {
            return MetricStats(0, 0.0, 0.0, 0.0, 0.0, 0.0)
        }
        
        val values = relevantMetrics.map { it.value }
        return MetricStats(
            count = values.size,
            min = values.minOrNull() ?: 0.0,
            max = values.maxOrNull() ?: 0.0,
            average = values.average(),
            median = values.sorted().let { sorted ->
                val middle = sorted.size / 2
                if (sorted.size % 2 == 0) {
                    (sorted[middle - 1] + sorted[middle]) / 2.0
                } else {
                    sorted[middle]
                }
            },
            percentile95 = values.sorted().let { sorted ->
                val index = (sorted.size * 0.95).toInt().coerceAtMost(sorted.size - 1)
                sorted[index]
            }
        )
    }
    
    /**
     * 设置性能阈值
     */
    fun setThreshold(type: PerformanceMetricType, threshold: Double) {
        performanceThresholds[type] = threshold
    }
    
    /**
     * 清除历史指标
     */
    fun clearHistory() {
        metricsHistory.clear()
    }
    
    /**
     * 导出指标数据
     */
    fun exportMetrics(format: ExportFormat = ExportFormat.JSON): String {
        return when (format) {
            ExportFormat.JSON -> kotlinx.serialization.json.Json.encodeToString(metricsHistory)
            ExportFormat.CSV -> exportToCsv(metricsHistory)
        }
    }
    
    private fun setupDefaultThresholds() {
        performanceThresholds[PerformanceMetricType.RENDER_TIME] = 16.0 // 16ms for 60fps
        performanceThresholds[PerformanceMetricType.NETWORK_LATENCY] = 1000.0 // 1s
        performanceThresholds[PerformanceMetricType.DATABASE_QUERY_TIME] = 100.0 // 100ms
        performanceThresholds[PerformanceMetricType.API_RESPONSE_TIME] = 2000.0 // 2s
        performanceThresholds[PerformanceMetricType.STARTUP_TIME] = 3000.0 // 3s
        performanceThresholds[PerformanceMetricType.NAVIGATION_TIME] = 500.0 // 500ms
        performanceThresholds[PerformanceMetricType.USER_INTERACTION_TIME] = 100.0 // 100ms
    }
    
    private suspend fun startPeriodicCollection() {
        while (isMonitoring) {
            try {
                collectSystemMetrics()
                delay(5.seconds) // 每5秒收集一次系统指标
            } catch (e: Exception) {
                // 记录错误但继续监控
                println("Error collecting system metrics: ${e.message}")
            }
        }
    }
    
    private suspend fun collectSystemMetrics() {
        // 收集内存使用情况
        val memoryUsage = getMemoryUsage()
        recordMetric(
            type = PerformanceMetricType.MEMORY_USAGE,
            name = "system_memory",
            value = memoryUsage,
            unit = "MB"
        )
        
        // 收集CPU使用情况
        val cpuUsage = getCpuUsage()
        recordMetric(
            type = PerformanceMetricType.CPU_USAGE,
            name = "system_cpu",
            value = cpuUsage,
            unit = "%"
        )
    }
    
    private fun checkThresholdAlert(metric: PerformanceMetric) {
        val threshold = performanceThresholds[metric.type]
        if (threshold != null && metric.value > threshold) {
            // 发送性能告警
            CoroutineScope(Dispatchers.Default).launch {
                sendPerformanceAlert(metric, threshold)
            }
        }
    }
    
    private suspend fun sendPerformanceAlert(metric: PerformanceMetric, threshold: Double) {
        println("Performance Alert: ${metric.name} (${metric.value}${metric.unit}) exceeded threshold (${threshold}${metric.unit})")
        // 这里可以集成告警系统
    }
    
    private fun generateSummary(metrics: List<PerformanceMetric>): PerformanceSummary {
        val renderMetrics = metrics.filter { it.type == PerformanceMetricType.RENDER_TIME }
        val networkMetrics = metrics.filter { it.type == PerformanceMetricType.NETWORK_LATENCY }
        val memoryMetrics = metrics.filter { it.type == PerformanceMetricType.MEMORY_USAGE }
        val cpuMetrics = metrics.filter { it.type == PerformanceMetricType.CPU_USAGE }
        val errorMetrics = metrics.filter { it.type == PerformanceMetricType.ERROR_RATE }
        
        val performanceScore = calculatePerformanceScore(metrics)
        
        return PerformanceSummary(
            totalMetrics = metrics.size,
            averageRenderTime = renderMetrics.map { it.value }.average().takeIf { !it.isNaN() } ?: 0.0,
            averageNetworkLatency = networkMetrics.map { it.value }.average().takeIf { !it.isNaN() } ?: 0.0,
            memoryUsagePeak = memoryMetrics.maxOfOrNull { it.value } ?: 0.0,
            cpuUsageAverage = cpuMetrics.map { it.value }.average().takeIf { !it.isNaN() } ?: 0.0,
            errorCount = errorMetrics.size,
            performanceScore = performanceScore
        )
    }
    
    private fun calculatePerformanceScore(metrics: List<PerformanceMetric>): Double {
        // 简化的性能评分算法
        var score = 100.0
        
        // 根据各种指标扣分
        val renderTime = metrics.filter { it.type == PerformanceMetricType.RENDER_TIME }
            .map { it.value }.average().takeIf { !it.isNaN() } ?: 0.0
        if (renderTime > 16.0) score -= (renderTime - 16.0) / 16.0 * 20
        
        val networkLatency = metrics.filter { it.type == PerformanceMetricType.NETWORK_LATENCY }
            .map { it.value }.average().takeIf { !it.isNaN() } ?: 0.0
        if (networkLatency > 1000.0) score -= (networkLatency - 1000.0) / 1000.0 * 15
        
        val errorCount = metrics.count { it.type == PerformanceMetricType.ERROR_RATE }
        score -= errorCount * 5.0
        
        return score.coerceIn(0.0, 100.0)
    }
    
    private fun generateRecommendations(
        metrics: List<PerformanceMetric>,
        summary: PerformanceSummary
    ): List<PerformanceRecommendation> {
        val recommendations = mutableListOf<PerformanceRecommendation>()
        
        // 渲染性能建议
        if (summary.averageRenderTime > 16.0) {
            recommendations.add(
                PerformanceRecommendation(
                    type = RecommendationType.RENDER_OPTIMIZATION,
                    title = "优化渲染性能",
                    description = "平均渲染时间超过16ms，可能影响用户体验",
                    priority = RecommendationPriority.HIGH,
                    impact = "提升UI流畅度，改善用户体验",
                    actionItems = listOf(
                        "减少不必要的重组",
                        "优化复杂布局",
                        "使用LazyColumn/LazyRow处理长列表",
                        "避免在Composable中进行耗时操作"
                    )
                )
            )
        }
        
        // 网络性能建议
        if (summary.averageNetworkLatency > 1000.0) {
            recommendations.add(
                PerformanceRecommendation(
                    type = RecommendationType.NETWORK_OPTIMIZATION,
                    title = "优化网络请求",
                    description = "网络延迟较高，影响数据加载速度",
                    priority = RecommendationPriority.MEDIUM,
                    impact = "提升数据加载速度，减少用户等待时间",
                    actionItems = listOf(
                        "启用请求缓存",
                        "使用CDN加速",
                        "优化API响应大小",
                        "实现请求重试机制"
                    )
                )
            )
        }
        
        // 内存使用建议
        if (summary.memoryUsagePeak > 512.0) { // 假设512MB为阈值
            recommendations.add(
                PerformanceRecommendation(
                    type = RecommendationType.MEMORY_OPTIMIZATION,
                    title = "优化内存使用",
                    description = "内存使用峰值较高，可能导致应用被系统杀死",
                    priority = RecommendationPriority.HIGH,
                    impact = "降低应用被杀死的风险，提升稳定性",
                    actionItems = listOf(
                        "及时释放不用的资源",
                        "优化图片加载和缓存策略",
                        "避免内存泄漏",
                        "使用对象池复用对象"
                    )
                )
            )
        }
        
        return recommendations
    }
    
    private fun generateMetricId(): String {
        return "metric_${Clock.System.now().toEpochMilliseconds()}_${(0..999).random()}"
    }
    
    private fun generateReportId(): String {
        return "report_${Clock.System.now().toEpochMilliseconds()}"
    }
    
    private fun exportToCsv(metrics: List<PerformanceMetric>): String {
        val header = "ID,Type,Name,Value,Unit,Timestamp,Tags,Metadata"
        val rows = metrics.map { metric ->
            "${metric.id},${metric.type},${metric.name},${metric.value},${metric.unit},${metric.timestamp},${metric.tags},${metric.metadata}"
        }
        return (listOf(header) + rows).joinToString("\n")
    }
    
    // 平台特定实现
    private expect fun getMemoryUsage(): Double
    private expect fun getCpuUsage(): Double
}

/**
 * 指标统计信息
 */
data class MetricStats(
    val count: Int,
    val min: Double,
    val max: Double,
    val average: Double,
    val median: Double,
    val percentile95: Double
)

/**
 * 导出格式
 */
enum class ExportFormat {
    JSON, CSV
}

/**
 * 性能监控扩展函数
 */
suspend inline fun <T> UnifyPerformanceMonitor.measureRender(
    name: String,
    crossinline block: suspend () -> T
): T = measureSuspend(name, PerformanceMetricType.RENDER_TIME, block = block)

suspend inline fun <T> UnifyPerformanceMonitor.measureNetwork(
    name: String,
    crossinline block: suspend () -> T
): T = measureSuspend(name, PerformanceMetricType.NETWORK_LATENCY, block = block)

suspend inline fun <T> UnifyPerformanceMonitor.measureDatabase(
    name: String,
    crossinline block: suspend () -> T
): T = measureSuspend(name, PerformanceMetricType.DATABASE_QUERY_TIME, block = block)

/**
 * 全局性能监控实例
 */
object GlobalPerformanceMonitor {
    private val monitor = UnifyPerformanceMonitor()
    
    fun getInstance(): UnifyPerformanceMonitor = monitor
    
    fun startGlobalMonitoring() {
        monitor.startMonitoring()
    }
    
    fun stopGlobalMonitoring() {
        monitor.stopMonitoring()
    }
}
