package com.unify.core.performance

import com.unify.core.platform.getCurrentTimeMillis
import kotlinx.serialization.Serializable

/**
 * 内存指标数据类
 */
@Serializable
data class MemoryMetrics(
    val usedMemory: Long = 0L,
    val totalMemory: Long = 0L,
    val maxMemory: Long = 0L,
    val gcCount: Int = 0,
    val gcTime: Long = 0L,
) {
    val usagePercent: Float
        get() = if (totalMemory > 0) (usedMemory.toFloat() / totalMemory) * 100f else 0f
}

/**
 * 网络指标数据类
 */
@Serializable
data class NetworkMetrics(
    val latency: Double = 0.0,
    val requestCount: Int = 0,
    val errorCount: Int = 0,
    val bytesReceived: Long = 0L,
    val bytesSent: Long = 0L,
    val connectionCount: Int = 0,
) {
    val errorRate: Float
        get() = if (requestCount > 0) (errorCount.toFloat() / requestCount) * 100f else 0f
}

/**
 * CPU指标数据类
 */
@Serializable
data class CPUMetrics(
    val usage: Double = 0.0,
    val coreCount: Int = 1,
    val frequency: Long = 0L,
    val temperature: Float = 0f,
)

/**
 * 综合性能指标数据类
 */
@Serializable
data class PerformanceMetrics(
    val cpuUsage: Double = 0.0,
    val memoryUsage: MemoryMetrics = MemoryMetrics(),
    val frameRate: Double = 0.0,
    val networkMetrics: NetworkMetrics = NetworkMetrics(),
    val customMetrics: Map<String, Double> = emptyMap(),
    val timers: Map<String, Long> = emptyMap(),
    val timestamp: Long = getCurrentTimeMillis(),
)

/**
 * 性能监控接口
 */
interface PerformanceMonitor {
    suspend fun getMetrics(): kotlinx.coroutines.flow.Flow<PerformanceMetrics>

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
