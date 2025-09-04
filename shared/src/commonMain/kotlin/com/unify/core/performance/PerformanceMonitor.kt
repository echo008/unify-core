package com.unify.core.performance

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlin.system.measureTimeMillis

/**
 * Unify跨平台性能监控器
 * 支持8大平台的统一性能监控
 */
interface UnifyPerformanceMonitor {
    suspend fun startMonitoring()
    suspend fun stopMonitoring()
    suspend fun recordMetric(name: String, value: Double, unit: String = "")
    suspend fun startTimer(name: String): String
    suspend fun stopTimer(timerId: String): Long
    suspend fun recordMemoryUsage()
    suspend fun recordCPUUsage()
    suspend fun recordNetworkLatency(url: String)
    suspend fun recordFrameRate()
    fun getMetrics(): Flow<PerformanceMetrics>
    suspend fun exportMetrics(): String
    suspend fun clearMetrics()
}

/**
 * 性能指标数据类
 */
@Serializable
data class PerformanceMetrics(
    val timestamp: Long = System.currentTimeMillis(),
    val cpuUsage: Double = 0.0,
    val memoryUsage: MemoryMetrics = MemoryMetrics(),
    val networkMetrics: NetworkMetrics = NetworkMetrics(),
    val frameRate: Double = 0.0,
    val customMetrics: Map<String, Double> = emptyMap(),
    val timers: Map<String, Long> = emptyMap()
)

/**
 * 内存指标
 */
@Serializable
data class MemoryMetrics(
    val usedMemory: Long = 0L,
    val totalMemory: Long = 0L,
    val maxMemory: Long = 0L,
    val gcCount: Long = 0L,
    val gcTime: Long = 0L
)

/**
 * 网络指标
 */
@Serializable
data class NetworkMetrics(
    val latency: Double = 0.0,
    val downloadSpeed: Double = 0.0,
    val uploadSpeed: Double = 0.0,
    val requestCount: Long = 0L,
    val errorCount: Long = 0L
)

/**
 * 性能警告级别
 */
@Serializable
enum class PerformanceWarningLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

/**
 * 性能警告
 */
@Serializable
data class PerformanceWarning(
    val level: PerformanceWarningLevel,
    val message: String,
    val metric: String,
    val value: Double,
    val threshold: Double,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 性能监控配置
 */
@Serializable
data class PerformanceConfig(
    val enableCPUMonitoring: Boolean = true,
    val enableMemoryMonitoring: Boolean = true,
    val enableNetworkMonitoring: Boolean = true,
    val enableFrameRateMonitoring: Boolean = true,
    val monitoringInterval: Long = 1000L,
    val maxMetricsHistory: Int = 1000,
    val cpuWarningThreshold: Double = 80.0,
    val memoryWarningThreshold: Double = 90.0,
    val frameRateWarningThreshold: Double = 30.0
)

/**
 * Unify性能监控器实现
 */
class UnifyPerformanceMonitorImpl(
    private val config: PerformanceConfig = PerformanceConfig()
) : UnifyPerformanceMonitor {
    
    private val _metrics = MutableStateFlow(PerformanceMetrics())
    private val metrics: StateFlow<PerformanceMetrics> = _metrics.asStateFlow()
    
    private val metricsHistory = mutableListOf<PerformanceMetrics>()
    private val activeTimers = mutableMapOf<String, Long>()
    private val customMetrics = mutableMapOf<String, Double>()
    private var isMonitoring = false
    
    companion object {
        private const val MILLISECONDS_PER_SECOND = 1000L
        private const val BYTES_TO_MB_DIVISOR = 1024 * 1024
        private const val MIN_FPS_THRESHOLD = 30.0
        private const val MAX_CPU_THRESHOLD = 80.0
        private const val MAX_MEMORY_THRESHOLD = 90.0
    }
    
    override suspend fun startMonitoring() {
        if (isMonitoring) return
        isMonitoring = true
        
        // 启动监控循环
        startMonitoringLoop()
    }
    
    override suspend fun stopMonitoring() {
        isMonitoring = false
    }
    
    override suspend fun recordMetric(name: String, value: Double, unit: String) {
        customMetrics[name] = value
        updateMetrics()
    }
    
    override suspend fun startTimer(name: String): String {
        val timerId = "${name}_${System.currentTimeMillis()}"
        activeTimers[timerId] = System.currentTimeMillis()
        return timerId
    }
    
    override suspend fun stopTimer(timerId: String): Long {
        val startTime = activeTimers.remove(timerId) ?: return 0L
        return System.currentTimeMillis() - startTime
    }
    
    override suspend fun recordMemoryUsage() {
        val memoryMetrics = getPlatformMemoryMetrics()
        updateMetrics(memoryMetrics = memoryMetrics)
    }
    
    override suspend fun recordCPUUsage() {
        val cpuUsage = getPlatformCPUUsage()
        updateMetrics(cpuUsage = cpuUsage)
    }
    
    override suspend fun recordNetworkLatency(url: String) {
        val latency = measureNetworkLatency(url)
        val networkMetrics = _metrics.value.networkMetrics.copy(latency = latency)
        updateMetrics(networkMetrics = networkMetrics)
    }
    
    override suspend fun recordFrameRate() {
        val frameRate = getPlatformFrameRate()
        updateMetrics(frameRate = frameRate)
    }
    
    override fun getMetrics(): Flow<PerformanceMetrics> = metrics
    
    override suspend fun exportMetrics(): String {
        return kotlinx.serialization.json.Json.encodeToString(metricsHistory)
    }
    
    override suspend fun clearMetrics() {
        metricsHistory.clear()
        customMetrics.clear()
        activeTimers.clear()
        _metrics.value = PerformanceMetrics()
    }
    
    private fun updateMetrics(
        cpuUsage: Double? = null,
        memoryMetrics: MemoryMetrics? = null,
        networkMetrics: NetworkMetrics? = null,
        frameRate: Double? = null
    ) {
        val currentMetrics = _metrics.value
        val newMetrics = currentMetrics.copy(
            timestamp = System.currentTimeMillis(),
            cpuUsage = cpuUsage ?: currentMetrics.cpuUsage,
            memoryUsage = memoryMetrics ?: currentMetrics.memoryUsage,
            networkMetrics = networkMetrics ?: currentMetrics.networkMetrics,
            frameRate = frameRate ?: currentMetrics.frameRate,
            customMetrics = customMetrics.toMap(),
            timers = activeTimers.mapValues { System.currentTimeMillis() - it.value }
        )
        
        _metrics.value = newMetrics
        
        // 添加到历史记录
        metricsHistory.add(newMetrics)
        if (metricsHistory.size > config.maxMetricsHistory) {
            metricsHistory.removeAt(0)
        }
        
        // 检查警告
        checkWarnings(newMetrics)
    }
    
    private fun checkWarnings(metrics: PerformanceMetrics) {
        // CPU使用率警告
        if (config.enableCPUMonitoring && metrics.cpuUsage > config.cpuWarningThreshold) {
            emitWarning(
                PerformanceWarningLevel.HIGH,
                "CPU使用率过高: ${metrics.cpuUsage}%",
                "cpu_usage",
                metrics.cpuUsage,
                config.cpuWarningThreshold
            )
        }
        
        // 内存使用警告
        if (config.enableMemoryMonitoring) {
            val memoryUsagePercent = if (metrics.memoryUsage.totalMemory > 0) {
                (metrics.memoryUsage.usedMemory.toDouble() / metrics.memoryUsage.totalMemory) * 100
            } else 0.0
            
            if (memoryUsagePercent > config.memoryWarningThreshold) {
                emitWarning(
                    PerformanceWarningLevel.HIGH,
                    "内存使用率过高: ${memoryUsagePercent}%",
                    "memory_usage",
                    memoryUsagePercent,
                    config.memoryWarningThreshold
                )
            }
        }
        
        // 帧率警告
        if (config.enableFrameRateMonitoring && metrics.frameRate < config.frameRateWarningThreshold) {
            emitWarning(
                PerformanceWarningLevel.MEDIUM,
                "帧率过低: ${metrics.frameRate} FPS",
                "frame_rate",
                metrics.frameRate,
                config.frameRateWarningThreshold
            )
        }
    }
    
    private fun emitWarning(
        level: PerformanceWarningLevel,
        message: String,
        metric: String,
        value: Double,
        threshold: Double
    ) {
        val warning = PerformanceWarning(level, message, metric, value, threshold)
        // 这里可以通过事件系统发送警告
        println("性能警告: ${warning.message}")
    }
    
    private suspend fun startMonitoringLoop() {
        // 实际实现中应该使用协程定时器
        // 这里简化为立即执行一次
        if (config.enableCPUMonitoring) recordCPUUsage()
        if (config.enableMemoryMonitoring) recordMemoryUsage()
        if (config.enableFrameRateMonitoring) recordFrameRate()
    }
    
    private suspend fun measureNetworkLatency(url: String): Double {
        return measureTimeMillis {
            // 实际实现中应该发送网络请求
            // 这里模拟网络延迟
        }.toDouble()
    }
    
    // 平台特定的实现
    private expect suspend fun getPlatformMemoryMetrics(): MemoryMetrics
    private expect suspend fun getPlatformCPUUsage(): Double
    private expect suspend fun getPlatformFrameRate(): Double
}

/**
 * 性能分析器
 */
class PerformanceAnalyzer(private val monitor: UnifyPerformanceMonitor) {
    
    suspend fun analyzePerformance(): PerformanceAnalysisResult {
        val metricsJson = monitor.exportMetrics()
        val metrics: List<PerformanceMetrics> = kotlinx.serialization.json.Json.decodeFromString(metricsJson)
        
        return PerformanceAnalysisResult(
            averageCPU = metrics.map { it.cpuUsage }.average(),
            peakMemory = metrics.maxOfOrNull { it.memoryUsage.usedMemory } ?: 0L,
            averageFrameRate = metrics.map { it.frameRate }.average(),
            totalSamples = metrics.size,
            analysisTime = System.currentTimeMillis()
        )
    }
    
    suspend fun generateReport(): String {
        val analysis = analyzePerformance()
        return buildString {
            appendLine("=== 性能分析报告 ===")
            appendLine("平均CPU使用率: ${analysis.averageCPU}%")
            appendLine("峰值内存使用: ${analysis.peakMemory / (1024 * 1024)} MB")
            appendLine("平均帧率: ${analysis.averageFrameRate} FPS")
            appendLine("样本数量: ${analysis.totalSamples}")
            appendLine("分析时间: ${analysis.analysisTime}")
        }
    }
}

/**
 * 性能分析结果
 */
@Serializable
data class PerformanceAnalysisResult(
    val averageCPU: Double,
    val peakMemory: Long,
    val averageFrameRate: Double,
    val totalSamples: Int,
    val analysisTime: Long
)

/**
 * 性能工具类
 */
object PerformanceUtils {
    /**
     * 测量代码块执行时间
     */
    inline fun <T> measurePerformance(block: () -> T): Pair<T, Long> {
        val startTime = System.currentTimeMillis()
        val result = block()
        val duration = System.currentTimeMillis() - startTime
        return Pair(result, duration)
    }
    
    /**
     * 格式化内存大小
     */
    fun formatMemorySize(bytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB")
        var size = bytes.toDouble()
        var unitIndex = 0
        
        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }
        
        return "%.2f %s".format(size, units[unitIndex])
    }
    
    /**
     * 计算百分比
     */
    fun calculatePercentage(value: Double, total: Double): Double {
        return if (total > 0) (value / total) * 100 else 0.0
    }
}
