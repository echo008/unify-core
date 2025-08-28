package com.unify.monitoring

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import com.unify.database.*
import com.unify.platform.*

/**
 * 统一性能监控系统
 * 基于文档要求的性能监控和错误处理机制
 */

/**
 * 性能指标类型
 */
enum class PerformanceMetricType {
    RENDER_TIME,        // 渲染时间
    MEMORY_USAGE,       // 内存使用
    CPU_USAGE,          // CPU使用率
    NETWORK_LATENCY,    // 网络延迟
    DATABASE_QUERY,     // 数据库查询时间
    COMPONENT_LIFECYCLE, // 组件生命周期
    USER_INTERACTION,   // 用户交互响应时间
    FRAME_RATE,         // 帧率
    STARTUP_TIME,       // 启动时间
    BUNDLE_SIZE         // 包体大小
}

/**
 * 性能指标数据
 */
@Serializable
data class PerformanceMetric(
    val id: String = generateId(),
    val componentId: String,
    val metricType: PerformanceMetricType,
    val value: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val platform: String,
    val additionalData: Map<String, String> = emptyMap()
)

/**
 * 性能阈值配置
 */
data class PerformanceThreshold(
    val metricType: PerformanceMetricType,
    val warningThreshold: Double,
    val errorThreshold: Double,
    val enabled: Boolean = true
)

/**
 * 性能监控配置
 */
data class PerformanceMonitorConfig(
    val enableRealTimeMonitoring: Boolean = true,
    val enablePerformanceLogging: Boolean = true,
    val enableAutomaticReporting: Boolean = true,
    val samplingRate: Double = 1.0, // 采样率 0.0-1.0
    val maxMetricsInMemory: Int = 1000,
    val reportingIntervalMs: Long = 60000L, // 1分钟
    val thresholds: List<PerformanceThreshold> = defaultThresholds()
)

/**
 * 组件性能监控器
 */
class ComponentPerformanceMonitor(
    private val config: PerformanceMonitorConfig = PerformanceMonitorConfig(),
    private val database: UnifyDatabase? = null,
    private val platformManager: UnifyPlatformManager? = null
) {
    private val metricsFlow = MutableSharedFlow<PerformanceMetric>()
    private val metricsBuffer = mutableListOf<PerformanceMetric>()
    private val activeTimers = mutableMapOf<String, Long>()
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    init {
        if (config.enableAutomaticReporting) {
            startAutomaticReporting()
        }
        
        if (config.enableRealTimeMonitoring) {
            startRealTimeMonitoring()
        }
    }
    
    /**
     * 开始性能计时
     */
    fun startTimer(componentId: String, metricType: PerformanceMetricType): String {
        val timerId = "${componentId}_${metricType}_${System.currentTimeMillis()}"
        activeTimers[timerId] = System.currentTimeMillis()
        return timerId
    }
    
    /**
     * 结束性能计时并记录指标
     */
    fun endTimer(timerId: String, componentId: String, metricType: PerformanceMetricType) {
        val startTime = activeTimers.remove(timerId) ?: return
        val duration = System.currentTimeMillis() - startTime
        
        recordMetric(
            componentId = componentId,
            metricType = metricType,
            value = duration.toDouble()
        )
    }
    
    /**
     * 记录性能指标
     */
    fun recordMetric(
        componentId: String,
        metricType: PerformanceMetricType,
        value: Double,
        additionalData: Map<String, String> = emptyMap()
    ) {
        if (!shouldSample()) return
        
        val platform = platformManager?.getCurrentPlatform()?.name ?: "UNKNOWN"
        val metric = PerformanceMetric(
            componentId = componentId,
            metricType = metricType,
            value = value,
            platform = platform,
            additionalData = additionalData
        )
        
        // 添加到内存缓冲区
        synchronized(metricsBuffer) {
            metricsBuffer.add(metric)
            if (metricsBuffer.size > config.maxMetricsInMemory) {
                metricsBuffer.removeAt(0)
            }
        }
        
        // 发送到流
        scope.launch {
            metricsFlow.emit(metric)
        }
        
        // 检查阈值
        checkThresholds(metric)
        
        // 持久化到数据库
        if (database != null) {
            scope.launch {
                try {
                    database.insertPerformanceMetricEntity(
                        PerformanceMetricEntity(
                            id = 0, // 自动生成
                            componentId = metric.componentId,
                            metricType = metric.metricType.name,
                            value = metric.value,
                            timestamp = metric.timestamp,
                            platform = metric.platform
                        )
                    )
                } catch (e: Exception) {
                    // 记录错误但不影响主流程
                    UnifyErrorHandler.handleError(
                        UnifyError.DatabaseError("Failed to persist performance metric", e)
                    )
                }
            }
        }
    }
    
    /**
     * 获取性能指标流
     */
    fun getMetricsFlow(): SharedFlow<PerformanceMetric> = metricsFlow.asSharedFlow()
    
    /**
     * 获取缓存的指标
     */
    fun getCachedMetrics(): List<PerformanceMetric> {
        return synchronized(metricsBuffer) {
            metricsBuffer.toList()
        }
    }
    
    /**
     * 获取指定组件的性能统计
     */
    fun getComponentStats(componentId: String): ComponentPerformanceStats {
        val componentMetrics = synchronized(metricsBuffer) {
            metricsBuffer.filter { it.componentId == componentId }
        }
        
        return ComponentPerformanceStats(
            componentId = componentId,
            totalMetrics = componentMetrics.size,
            averageRenderTime = componentMetrics
                .filter { it.metricType == PerformanceMetricType.RENDER_TIME }
                .map { it.value }
                .average(),
            maxMemoryUsage = componentMetrics
                .filter { it.metricType == PerformanceMetricType.MEMORY_USAGE }
                .maxOfOrNull { it.value } ?: 0.0,
            averageFrameRate = componentMetrics
                .filter { it.metricType == PerformanceMetricType.FRAME_RATE }
                .map { it.value }
                .average()
        )
    }
    
    private fun shouldSample(): Boolean {
        return Math.random() < config.samplingRate
    }
    
    private fun checkThresholds(metric: PerformanceMetric) {
        val threshold = config.thresholds.find { 
            it.metricType == metric.metricType && it.enabled 
        } ?: return
        
        when {
            metric.value >= threshold.errorThreshold -> {
                scope.launch {
                    UnifyErrorHandler.handleError(
                        UnifyError.PerformanceError(
                            "Performance metric ${metric.metricType} exceeded error threshold: ${metric.value} >= ${threshold.errorThreshold}",
                            metric
                        )
                    )
                }
            }
            metric.value >= threshold.warningThreshold -> {
                if (config.enablePerformanceLogging) {
                    println("Performance Warning: ${metric.metricType} = ${metric.value} (threshold: ${threshold.warningThreshold})")
                }
            }
        }
    }
    
    private fun startAutomaticReporting() {
        scope.launch {
            while (true) {
                delay(config.reportingIntervalMs)
                generatePerformanceReport()
            }
        }
    }
    
    private fun startRealTimeMonitoring() {
        scope.launch {
            metricsFlow.collect { metric ->
                if (config.enablePerformanceLogging) {
                    println("Performance Metric: ${metric.componentId} - ${metric.metricType} = ${metric.value}")
                }
            }
        }
    }
    
    private suspend fun generatePerformanceReport() {
        val metrics = getCachedMetrics()
        if (metrics.isEmpty()) return
        
        val report = PerformanceReport(
            timestamp = System.currentTimeMillis(),
            platform = platformManager?.getCurrentPlatform()?.name ?: "UNKNOWN",
            totalMetrics = metrics.size,
            metricsByType = metrics.groupBy { it.metricType }
                .mapValues { (_, values) ->
                    MetricSummary(
                        count = values.size,
                        average = values.map { it.value }.average(),
                        min = values.minOf { it.value },
                        max = values.maxOf { it.value }
                    )
                },
            topSlowComponents = metrics
                .filter { it.metricType == PerformanceMetricType.RENDER_TIME }
                .groupBy { it.componentId }
                .mapValues { (_, values) -> values.map { it.value }.average() }
                .toList()
                .sortedByDescending { it.second }
                .take(10)
        )
        
        // 可以将报告发送到分析服务或保存到文件
        if (config.enablePerformanceLogging) {
            println("Performance Report Generated: ${report.totalMetrics} metrics")
        }
    }
    
    fun cleanup() {
        scope.cancel()
        activeTimers.clear()
        synchronized(metricsBuffer) {
            metricsBuffer.clear()
        }
    }
}

/**
 * 组件性能统计
 */
data class ComponentPerformanceStats(
    val componentId: String,
    val totalMetrics: Int,
    val averageRenderTime: Double,
    val maxMemoryUsage: Double,
    val averageFrameRate: Double
)

/**
 * 指标摘要
 */
data class MetricSummary(
    val count: Int,
    val average: Double,
    val min: Double,
    val max: Double
)

/**
 * 性能报告
 */
data class PerformanceReport(
    val timestamp: Long,
    val platform: String,
    val totalMetrics: Int,
    val metricsByType: Map<PerformanceMetricType, MetricSummary>,
    val topSlowComponents: List<Pair<String, Double>>
)

/**
 * 默认性能阈值配置
 */
private fun defaultThresholds(): List<PerformanceThreshold> = listOf(
    PerformanceThreshold(PerformanceMetricType.RENDER_TIME, 16.0, 32.0), // 16ms警告，32ms错误
    PerformanceThreshold(PerformanceMetricType.MEMORY_USAGE, 50.0, 100.0), // 50MB警告，100MB错误
    PerformanceThreshold(PerformanceMetricType.CPU_USAGE, 70.0, 90.0), // 70%警告，90%错误
    PerformanceThreshold(PerformanceMetricType.NETWORK_LATENCY, 1000.0, 3000.0), // 1s警告，3s错误
    PerformanceThreshold(PerformanceMetricType.DATABASE_QUERY, 100.0, 500.0), // 100ms警告，500ms错误
    PerformanceThreshold(PerformanceMetricType.USER_INTERACTION, 100.0, 300.0), // 100ms警告，300ms错误
    PerformanceThreshold(PerformanceMetricType.FRAME_RATE, 45.0, 30.0), // 45fps警告，30fps错误（反向）
    PerformanceThreshold(PerformanceMetricType.STARTUP_TIME, 2000.0, 5000.0) // 2s警告，5s错误
)

/**
 * 生成唯一ID
 */
private fun generateId(): String {
    return "${System.currentTimeMillis()}-${(Math.random() * 1000).toInt()}"
}
