package com.unify.core.performance

import com.unify.core.platform.getCurrentTimeMillis
import kotlinx.serialization.Serializable

// 使用PerformanceModels.kt中的统一定义

/**
 * 综合性能指标数据类 - 使用PerformanceModels.kt中的PerformanceSnapshot
 */
@Serializable
data class LegacyPerformanceMetrics(
    val cpuUsage: Double = 0.0,
    val memoryUsage: com.unify.core.performance.MemoryMetrics = com.unify.core.performance.MemoryMetrics(),
    val frameRate: Double = 0.0,
    val networkMetrics: com.unify.core.performance.NetworkMetrics = com.unify.core.performance.NetworkMetrics(),
    val customMetrics: Map<String, Double> = emptyMap(),
    val timers: Map<String, Long> = emptyMap(),
    val timestamp: Long = getCurrentTimeMillis(),
)

/**
 * 性能监控接口
 */
interface PerformanceMonitor {
    suspend fun getMetrics(): kotlinx.coroutines.flow.Flow<PerformanceSnapshot>

    suspend fun startMonitoring()

    suspend fun stopMonitoring()

    suspend fun clearMetrics()

    suspend fun recordCustomMetric(
        name: String,
        value: Double,
    )

    suspend fun startTimer(name: String)

    suspend fun stopTimer(name: String): Long?
}
