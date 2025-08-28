package com.unify.ui.performance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Compose性能监控工具
 */
@Stable
class ComposePerformanceMonitor {
    private val performanceListeners = mutableSetOf<PerformanceListener>()
    private val compositionMetrics = mutableMapOf<String, CompositionMetrics>()
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    /**
     * 记录组合度量
     */
    fun recordComposition(componentName: String, duration: Duration) {
        val metrics = compositionMetrics.getOrPut(componentName) { CompositionMetrics() }
        metrics.totalCompositions++
        metrics.totalCompositionTime += duration
        metrics.averageCompositionTime = metrics.totalCompositionTime / metrics.totalCompositions
        
        if (duration > metrics.maxCompositionTime) {
            metrics.maxCompositionTime = duration
        }
        
        if (metrics.minCompositionTime == Duration.ZERO || duration < metrics.minCompositionTime) {
            metrics.minCompositionTime = duration
        }

        // 通知监听器
        performanceListeners.forEach { listener ->
            coroutineScope.launch {
                listener.onCompositionMeasured(componentName, duration, metrics)
            }
        }
    }

    /**
     * 记录重组度量
     */
    fun recordRecomposition(componentName: String, duration: Duration) {
        val metrics = compositionMetrics.getOrPut(componentName) { CompositionMetrics() }
        metrics.totalRecompositions++
        metrics.totalRecompositionTime += duration
        metrics.averageRecompositionTime = metrics.totalRecompositionTime / metrics.totalRecompositions

        if (duration > metrics.maxRecompositionTime) {
            metrics.maxRecompositionTime = duration
        }
        
        if (metrics.minRecompositionTime == Duration.ZERO || duration < metrics.minRecompositionTime) {
            metrics.minRecompositionTime = duration
        }

        // 通知监听器
        performanceListeners.forEach { listener ->
            coroutineScope.launch {
                listener.onRecompositionMeasured(componentName, duration, metrics)
            }
        }
    }

    /**
     * 获取组件性能报告
     */
    fun getPerformanceReport(): PerformanceReport {
        val totalCompositions = compositionMetrics.values.sumOf { it.totalCompositions }
        val totalRecompositions = compositionMetrics.values.sumOf { it.totalRecompositions }
        val totalCompositionTime = compositionMetrics.values.sumOf { it.totalCompositionTime.inWholeMilliseconds }
        val totalRecompositionTime = compositionMetrics.values.sumOf { it.totalRecompositionTime.inWholeMilliseconds }

        return PerformanceReport(
            totalComponents = compositionMetrics.size,
            totalCompositions = totalCompositions,
            totalRecompositions = totalRecompositions,
            averageCompositionTime = totalCompositionTime.milliseconds / totalCompositions.coerceAtLeast(1),
            averageRecompositionTime = totalRecompositionTime.milliseconds / totalRecompositions.coerceAtLeast(1),
            componentMetrics = compositionMetrics,
            timestamp = Clock.System.now()
        )
    }

    /**
     * 添加性能监听器
     */
    fun addPerformanceListener(listener: PerformanceListener) {
        performanceListeners.add(listener)
    }

    /**
     * 移除性能监听器
     */
    fun removePerformanceListener(listener: PerformanceListener) {
        performanceListeners.remove(listener)
    }

    /**
     * 清除所有度量数据
     */
    fun clearMetrics() {
        compositionMetrics.clear()
    }
}

/**
 * 性能监听器接口
 */
interface PerformanceListener {
    suspend fun onCompositionMeasured(componentName: String, duration: Duration, metrics: CompositionMetrics)
    suspend fun onRecompositionMeasured(componentName: String, duration: Duration, metrics: CompositionMetrics)
}

/**
 * 组合度量数据类
 */
data class CompositionMetrics(
    var totalCompositions: Long = 0,
    var totalCompositionTime: Duration = Duration.ZERO,
    var averageCompositionTime: Duration = Duration.ZERO,
    var maxCompositionTime: Duration = Duration.ZERO,
    var minCompositionTime: Duration = Duration.ZERO,
    
    var totalRecompositions: Long = 0,
    var totalRecompositionTime: Duration = Duration.ZERO,
    var averageRecompositionTime: Duration = Duration.ZERO,
    var maxRecompositionTime: Duration = Duration.ZERO,
    var minRecompositionTime: Duration = Duration.ZERO
)

/**
 * 性能报告数据类
 */
data class PerformanceReport(
    val totalComponents: Int,
    val totalCompositions: Long,
    val totalRecompositions: Long,
    val averageCompositionTime: Duration,
    val averageRecompositionTime: Duration,
    val componentMetrics: Map<String, CompositionMetrics>,
    val timestamp: kotlinx.datetime.Instant
)

/**
 * 性能监控Composable函数
 */
@Composable
fun PerformanceMonitor(
    componentName: String,
    monitor: ComposePerformanceMonitor = remember { ComposePerformanceMonitor() },
    content: @Composable () -> Unit
) {
    var compositionStart by remember { mutableStateOf(Clock.System.now()) }
    var recompositionStart by remember { mutableStateOf(Clock.System.now()) }

    // 监听组合变化
    LaunchedEffect(Unit) {
        snapshotFlow { compositionStart }
            .debounce(50.milliseconds)
            .distinctUntilChanged()
            .collect { startTime ->
                val duration = Clock.System.now() - startTime
                monitor.recordComposition(componentName, duration)
            }
    }

    // 监听重组变化
    LaunchedEffect(Unit) {
        snapshotFlow { recompositionStart }
            .debounce(50.milliseconds)
            .distinctUntilChanged()
            .collect { startTime ->
                val duration = Clock.System.now() - startTime
                monitor.recordRecomposition(componentName, duration)
            }
    }

    // 记录组合开始时间
    LaunchedEffect(Unit) {
        compositionStart = Clock.System.now()
    }

    // 记录重组开始时间
    LaunchedEffect(Unit) {
        recompositionStart = Clock.System.now()
    }

    content()
}

/**
 * 性能优化建议生成器
 */
class PerformanceOptimizationAdvisor {
    
    fun generateAdvice(report: PerformanceReport): List<PerformanceAdvice> {
        val advice = mutableListOf<PerformanceAdvice>()
        
        report.componentMetrics.forEach { (componentName, metrics) ->
            // 检查组合时间过长
            if (metrics.averageCompositionTime > 16.milliseconds) {
                advice.add(
                    PerformanceAdvice(
                        componentName = componentName,
                        issue = "组合时间过长",
                        severity = if (metrics.averageCompositionTime > 32.milliseconds) 
                            PerformanceSeverity.HIGH else PerformanceSeverity.MEDIUM,
                        suggestion = "考虑使用remember减少重复计算，或拆分复杂组件",
                        metricType = MetricType.COMPOSITION,
                        currentValue = metrics.averageCompositionTime,
                        threshold = 16.milliseconds
                    )
                )
            }
            
            // 检查重组频率过高
            if (metrics.totalRecompositions > 1000) {
                advice.add(
                    PerformanceAdvice(
                        componentName = componentName,
                        issue = "重组频率过高",
                        severity = PerformanceSeverity.MEDIUM,
                        suggestion = "使用derivedStateOf或snapshotFlow优化状态更新",
                        metricType = MetricType.RECOMPOSITION,
                        currentValue = metrics.totalRecompositions.toDouble(),
                        threshold = 1000.0
                    )
                )
            }
            
            // 检查最大重组时间
            if (metrics.maxRecompositionTime > 50.milliseconds) {
                advice.add(
                    PerformanceAdvice(
                        componentName = componentName,
                        issue = "单次重组时间过长",
                        severity = PerformanceSeverity.HIGH,
                        suggestion = "检查组件内部复杂计算，考虑使用LaunchedEffect或rememberCoroutineScope",
                        metricType = MetricType.RECOMPOSITION,
                        currentValue = metrics.maxRecompositionTime,
                        threshold = 50.milliseconds
                    )
                )
            }
        }
        
        return advice
    }
}

/**
 * 性能建议数据类
 */
data class PerformanceAdvice(
    val componentName: String,
    val issue: String,
    val severity: PerformanceSeverity,
    val suggestion: String,
    val metricType: MetricType,
    val currentValue: Any,
    val threshold: Any
)

/**
 * 性能严重程度枚举
 */
enum class PerformanceSeverity {
    LOW, MEDIUM, HIGH, CRITICAL
}

/**
 * 度量类型枚举
 */
enum class MetricType {
    COMPOSITION, RECOMPOSITION
}