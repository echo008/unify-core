package com.unify.core.performance

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.TimeSource

/**
 * 跨平台性能监控系统
 * 提供统一的性能指标收集、分析和报告功能
 */
object PerformanceMonitor {
    
    private val _performanceMetrics = MutableStateFlow(PerformanceMetrics())
    val performanceMetrics: StateFlow<PerformanceMetrics> = _performanceMetrics.asStateFlow()
    
    private val _performanceEvents = MutableStateFlow<List<PerformanceEvent>>(emptyList())
    val performanceEvents: StateFlow<List<PerformanceEvent>> = _performanceEvents.asStateFlow()
    
    private val timeSource = TimeSource.Monotonic
    private val activeOperations = mutableMapOf<String, TimeSource.Monotonic.ValueTimeMark>()
    
    /**
     * 开始性能监控
     */
    fun startMonitoring() {
        // 初始化性能监控
    }
    
    /**
     * 停止性能监控
     */
    fun stopMonitoring() {
        activeOperations.clear()
    }
    
    /**
     * 开始计时操作
     */
    fun startOperation(operationId: String, category: PerformanceCategory = PerformanceCategory.GENERAL) {
        activeOperations[operationId] = timeSource.markNow()
        recordEvent(PerformanceEvent.OperationStarted(operationId, category))
    }
    
    /**
     * 结束计时操作
     */
    fun endOperation(operationId: String): Duration? {
        val startTime = activeOperations.remove(operationId) ?: return null
        val duration = startTime.elapsedNow()
        
        recordEvent(PerformanceEvent.OperationCompleted(operationId, duration))
        updateMetrics(operationId, duration)
        
        return duration
    }
    
    /**
     * 记录内存使用情况
     */
    fun recordMemoryUsage(memoryUsage: MemoryUsage) {
        val currentMetrics = _performanceMetrics.value
        _performanceMetrics.value = currentMetrics.copy(
            memoryUsage = memoryUsage,
            lastUpdated = System.currentTimeMillis()
        )
        recordEvent(PerformanceEvent.MemoryUpdate(memoryUsage))
    }
    
    /**
     * 记录CPU使用率
     */
    fun recordCpuUsage(cpuUsage: Float) {
        val currentMetrics = _performanceMetrics.value
        _performanceMetrics.value = currentMetrics.copy(
            cpuUsage = cpuUsage,
            lastUpdated = System.currentTimeMillis()
        )
        recordEvent(PerformanceEvent.CpuUpdate(cpuUsage))
    }
    
    /**
     * 记录网络请求性能
     */
    fun recordNetworkRequest(request: NetworkRequestMetrics) {
        val currentMetrics = _performanceMetrics.value
        val updatedRequests = currentMetrics.networkRequests + request
        
        _performanceMetrics.value = currentMetrics.copy(
            networkRequests = updatedRequests,
            lastUpdated = System.currentTimeMillis()
        )
        recordEvent(PerformanceEvent.NetworkRequest(request))
    }
    
    /**
     * 记录UI渲染性能
     */
    fun recordRenderingMetrics(rendering: RenderingMetrics) {
        val currentMetrics = _performanceMetrics.value
        _performanceMetrics.value = currentMetrics.copy(
            renderingMetrics = rendering,
            lastUpdated = System.currentTimeMillis()
        )
        recordEvent(PerformanceEvent.RenderingUpdate(rendering))
    }
    
    /**
     * 记录启动时间
     */
    fun recordStartupTime(startupTime: Duration) {
        val currentMetrics = _performanceMetrics.value
        _performanceMetrics.value = currentMetrics.copy(
            startupTime = startupTime,
            lastUpdated = System.currentTimeMillis()
        )
        recordEvent(PerformanceEvent.StartupCompleted(startupTime))
    }
    
    /**
     * 获取性能报告
     */
    fun generatePerformanceReport(): PerformanceReport {
        val metrics = _performanceMetrics.value
        val events = _performanceEvents.value
        
        return PerformanceReport(
            timestamp = System.currentTimeMillis(),
            metrics = metrics,
            recentEvents = events.takeLast(100),
            summary = generateSummary(metrics, events)
        )
    }
    
    /**
     * 清除历史数据
     */
    fun clearHistory() {
        _performanceEvents.value = emptyList()
    }
    
    private fun recordEvent(event: PerformanceEvent) {
        val currentEvents = _performanceEvents.value
        _performanceEvents.value = (currentEvents + event).takeLast(1000) // 保留最近1000个事件
    }
    
    private fun updateMetrics(operationId: String, duration: Duration) {
        val currentMetrics = _performanceMetrics.value
        val operations = currentMetrics.operationMetrics.toMutableMap()
        
        val existing = operations[operationId]
        if (existing != null) {
            operations[operationId] = existing.copy(
                totalCalls = existing.totalCalls + 1,
                totalDuration = existing.totalDuration + duration,
                averageDuration = (existing.totalDuration + duration) / (existing.totalCalls + 1),
                maxDuration = maxOf(existing.maxDuration, duration),
                minDuration = minOf(existing.minDuration, duration)
            )
        } else {
            operations[operationId] = OperationMetrics(
                operationId = operationId,
                totalCalls = 1,
                totalDuration = duration,
                averageDuration = duration,
                maxDuration = duration,
                minDuration = duration
            )
        }
        
        _performanceMetrics.value = currentMetrics.copy(
            operationMetrics = operations,
            lastUpdated = System.currentTimeMillis()
        )
    }
    
    private fun generateSummary(metrics: PerformanceMetrics, events: List<PerformanceEvent>): PerformanceSummary {
        val slowOperations = metrics.operationMetrics.values
            .filter { it.averageDuration.inWholeMilliseconds > 100 }
            .sortedByDescending { it.averageDuration }
            .take(5)
        
        val memoryPressure = when {
            metrics.memoryUsage.usedMemory.toFloat() / metrics.memoryUsage.totalMemory > 0.9 -> MemoryPressure.HIGH
            metrics.memoryUsage.usedMemory.toFloat() / metrics.memoryUsage.totalMemory > 0.7 -> MemoryPressure.MEDIUM
            else -> MemoryPressure.LOW
        }
        
        val networkIssues = metrics.networkRequests.count { it.duration.inWholeMilliseconds > 5000 }
        
        return PerformanceSummary(
            overallHealth = calculateOverallHealth(metrics),
            slowOperations = slowOperations,
            memoryPressure = memoryPressure,
            networkIssues = networkIssues,
            recommendations = generateRecommendations(metrics)
        )
    }
    
    private fun calculateOverallHealth(metrics: PerformanceMetrics): PerformanceHealth {
        val memoryScore = 1.0f - (metrics.memoryUsage.usedMemory.toFloat() / metrics.memoryUsage.totalMemory)
        val cpuScore = 1.0f - (metrics.cpuUsage / 100f)
        val renderingScore = if (metrics.renderingMetrics.fps > 55) 1.0f else metrics.renderingMetrics.fps / 60f
        
        val overallScore = (memoryScore + cpuScore + renderingScore) / 3f
        
        return when {
            overallScore > 0.8f -> PerformanceHealth.EXCELLENT
            overallScore > 0.6f -> PerformanceHealth.GOOD
            overallScore > 0.4f -> PerformanceHealth.FAIR
            else -> PerformanceHealth.POOR
        }
    }
    
    private fun generateRecommendations(metrics: PerformanceMetrics): List<String> {
        val recommendations = mutableListOf<String>()
        
        if (metrics.memoryUsage.usedMemory.toFloat() / metrics.memoryUsage.totalMemory > 0.8) {
            recommendations.add("内存使用率过高，建议优化内存管理")
        }
        
        if (metrics.cpuUsage > 80) {
            recommendations.add("CPU使用率过高，建议优化计算密集型操作")
        }
        
        if (metrics.renderingMetrics.fps < 50) {
            recommendations.add("渲染帧率较低，建议优化UI渲染性能")
        }
        
        val slowOperations = metrics.operationMetrics.values.filter { it.averageDuration.inWholeMilliseconds > 200 }
        if (slowOperations.isNotEmpty()) {
            recommendations.add("发现${slowOperations.size}个慢操作，建议进行性能优化")
        }
        
        return recommendations
    }
}

/**
 * 性能指标数据类
 */
@Serializable
data class PerformanceMetrics(
    val memoryUsage: MemoryUsage = MemoryUsage(0, 0, 0, 0),
    val cpuUsage: Float = 0f,
    val networkRequests: List<NetworkRequestMetrics> = emptyList(),
    val renderingMetrics: RenderingMetrics = RenderingMetrics(),
    val operationMetrics: Map<String, OperationMetrics> = emptyMap(),
    val startupTime: Duration = Duration.ZERO,
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * 内存使用情况
 */
@Serializable
data class MemoryUsage(
    val totalMemory: Long,
    val availableMemory: Long,
    val usedMemory: Long,
    val appMemoryUsage: Long
)

/**
 * 网络请求指标
 */
@Serializable
data class NetworkRequestMetrics(
    val url: String,
    val method: String,
    val statusCode: Int,
    val duration: Duration,
    val requestSize: Long,
    val responseSize: Long,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 渲染性能指标
 */
@Serializable
data class RenderingMetrics(
    val fps: Float = 60f,
    val frameDrops: Int = 0,
    val averageFrameTime: Duration = Duration.ZERO,
    val maxFrameTime: Duration = Duration.ZERO
)

/**
 * 操作性能指标
 */
@Serializable
data class OperationMetrics(
    val operationId: String,
    val totalCalls: Int,
    val totalDuration: Duration,
    val averageDuration: Duration,
    val maxDuration: Duration,
    val minDuration: Duration
)

/**
 * 性能事件
 */
sealed class PerformanceEvent {
    abstract val timestamp: Long
    
    data class OperationStarted(
        val operationId: String,
        val category: PerformanceCategory,
        override val timestamp: Long = System.currentTimeMillis()
    ) : PerformanceEvent()
    
    data class OperationCompleted(
        val operationId: String,
        val duration: Duration,
        override val timestamp: Long = System.currentTimeMillis()
    ) : PerformanceEvent()
    
    data class MemoryUpdate(
        val memoryUsage: MemoryUsage,
        override val timestamp: Long = System.currentTimeMillis()
    ) : PerformanceEvent()
    
    data class CpuUpdate(
        val cpuUsage: Float,
        override val timestamp: Long = System.currentTimeMillis()
    ) : PerformanceEvent()
    
    data class NetworkRequest(
        val request: NetworkRequestMetrics,
        override val timestamp: Long = System.currentTimeMillis()
    ) : PerformanceEvent()
    
    data class RenderingUpdate(
        val rendering: RenderingMetrics,
        override val timestamp: Long = System.currentTimeMillis()
    ) : PerformanceEvent()
    
    data class StartupCompleted(
        val startupTime: Duration,
        override val timestamp: Long = System.currentTimeMillis()
    ) : PerformanceEvent()
}

/**
 * 性能类别
 */
enum class PerformanceCategory {
    GENERAL,
    NETWORK,
    DATABASE,
    UI_RENDERING,
    COMPUTATION,
    FILE_IO,
    STARTUP
}

/**
 * 性能报告
 */
@Serializable
data class PerformanceReport(
    val timestamp: Long,
    val metrics: PerformanceMetrics,
    val recentEvents: List<PerformanceEvent>,
    val summary: PerformanceSummary
)

/**
 * 性能摘要
 */
@Serializable
data class PerformanceSummary(
    val overallHealth: PerformanceHealth,
    val slowOperations: List<OperationMetrics>,
    val memoryPressure: MemoryPressure,
    val networkIssues: Int,
    val recommendations: List<String>
)

/**
 * 性能健康状态
 */
enum class PerformanceHealth {
    EXCELLENT,
    GOOD,
    FAIR,
    POOR
}

/**
 * 内存压力等级
 */
enum class MemoryPressure {
    LOW,
    MEDIUM,
    HIGH
}
