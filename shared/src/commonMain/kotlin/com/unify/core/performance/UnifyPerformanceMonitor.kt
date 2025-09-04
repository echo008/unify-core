package com.unify.core.performance

import com.unify.core.types.UnifyResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Unify统一性能监控系统
 * 100% Kotlin Multiplatform实现
 */
data class UnifyPerformanceMetrics(
    val cpuUsage: Float = 0f,
    val memoryUsage: Long = 0L,
    val memoryTotal: Long = 0L,
    val frameRate: Float = 0f,
    val renderTime: Long = 0L,
    val networkLatency: Long = 0L,
    val batteryLevel: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
) {
    val memoryUsagePercent: Float
        get() = if (memoryTotal > 0) (memoryUsage.toFloat() / memoryTotal) * 100f else 0f
}

data class UnifyPerformanceThresholds(
    val maxCpuUsage: Float = 80f,
    val maxMemoryUsage: Float = 85f,
    val minFrameRate: Float = 30f,
    val maxRenderTime: Long = 16L, // 16ms for 60fps
    val maxNetworkLatency: Long = 1000L,
    val minBatteryLevel: Float = 20f
)

enum class UnifyPerformanceLevel {
    EXCELLENT,
    GOOD,
    FAIR,
    POOR,
    CRITICAL
}

data class UnifyPerformanceAlert(
    val level: UnifyPerformanceLevel,
    val message: String,
    val metric: String,
    val value: Float,
    val threshold: Float,
    val timestamp: Long = System.currentTimeMillis()
)

interface UnifyPerformanceMonitor {
    val metrics: StateFlow<UnifyPerformanceMetrics>
    val alerts: StateFlow<List<UnifyPerformanceAlert>>
    val isMonitoring: StateFlow<Boolean>
    
    suspend fun startMonitoring(): UnifyResult<Unit>
    suspend fun stopMonitoring(): UnifyResult<Unit>
    suspend fun getCurrentMetrics(): UnifyResult<UnifyPerformanceMetrics>
    suspend fun getPerformanceLevel(): UnifyResult<UnifyPerformanceLevel>
    suspend fun setThresholds(thresholds: UnifyPerformanceThresholds): UnifyResult<Unit>
    suspend fun clearAlerts(): UnifyResult<Unit>
    suspend fun exportMetrics(): UnifyResult<String>
}

class UnifyPerformanceMonitorImpl : UnifyPerformanceMonitor {
    private val _metrics = MutableStateFlow(UnifyPerformanceMetrics())
    override val metrics: StateFlow<UnifyPerformanceMetrics> = _metrics.asStateFlow()
    
    private val _alerts = MutableStateFlow<List<UnifyPerformanceAlert>>(emptyList())
    override val alerts: StateFlow<List<UnifyPerformanceAlert>> = _alerts.asStateFlow()
    
    private val _isMonitoring = MutableStateFlow(false)
    override val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()
    
    private var thresholds = UnifyPerformanceThresholds()
    private val frameHistory = mutableListOf<Long>()
    private var lastFrameTime = 0L
    
    companion object {
        private const val FRAME_HISTORY_SIZE = 60
        private const val MONITORING_INTERVAL = 1000L
        private const val MILLISECONDS_PER_SECOND = 1000L
        private const val BYTES_TO_MB_DIVISOR = 1024 * 1024
    }
    
    override suspend fun startMonitoring(): UnifyResult<Unit> {
        return try {
            if (_isMonitoring.value) {
                return UnifyResult.Success(Unit)
            }
            
            _isMonitoring.value = true
            startMetricsCollection()
            UnifyResult.Success(Unit)
        } catch (e: Exception) {
            UnifyResult.Error("启动性能监控失败: ${e.message}")
        }
    }
    
    override suspend fun stopMonitoring(): UnifyResult<Unit> {
        return try {
            _isMonitoring.value = false
            frameHistory.clear()
            UnifyResult.Success(Unit)
        } catch (e: Exception) {
            UnifyResult.Error("停止性能监控失败: ${e.message}")
        }
    }
    
    override suspend fun getCurrentMetrics(): UnifyResult<UnifyPerformanceMetrics> {
        return try {
            val currentMetrics = collectCurrentMetrics()
            _metrics.value = currentMetrics
            checkThresholds(currentMetrics)
            UnifyResult.Success(currentMetrics)
        } catch (e: Exception) {
            UnifyResult.Error("获取性能指标失败: ${e.message}")
        }
    }
    
    override suspend fun getPerformanceLevel(): UnifyResult<UnifyPerformanceLevel> {
        return try {
            val currentMetrics = _metrics.value
            val level = calculatePerformanceLevel(currentMetrics)
            UnifyResult.Success(level)
        } catch (e: Exception) {
            UnifyResult.Error("计算性能等级失败: ${e.message}")
        }
    }
    
    override suspend fun setThresholds(thresholds: UnifyPerformanceThresholds): UnifyResult<Unit> {
        return try {
            this.thresholds = thresholds
            UnifyResult.Success(Unit)
        } catch (e: Exception) {
            UnifyResult.Error("设置性能阈值失败: ${e.message}")
        }
    }
    
    override suspend fun clearAlerts(): UnifyResult<Unit> {
        return try {
            _alerts.value = emptyList()
            UnifyResult.Success(Unit)
        } catch (e: Exception) {
            UnifyResult.Error("清除性能警告失败: ${e.message}")
        }
    }
    
    override suspend fun exportMetrics(): UnifyResult<String> {
        return try {
            val currentMetrics = _metrics.value
            val currentAlerts = _alerts.value
            
            val report = buildString {
                appendLine("=== Unify 性能监控报告 ===")
                appendLine("时间: ${currentMetrics.timestamp}")
                appendLine()
                appendLine("性能指标:")
                appendLine("- CPU使用率: ${String.format("%.1f", currentMetrics.cpuUsage)}%")
                appendLine("- 内存使用: ${currentMetrics.memoryUsage / BYTES_TO_MB_DIVISOR}MB / ${currentMetrics.memoryTotal / BYTES_TO_MB_DIVISOR}MB (${String.format("%.1f", currentMetrics.memoryUsagePercent)}%)")
                appendLine("- 帧率: ${String.format("%.1f", currentMetrics.frameRate)} FPS")
                appendLine("- 渲染时间: ${currentMetrics.renderTime}ms")
                appendLine("- 网络延迟: ${currentMetrics.networkLatency}ms")
                appendLine("- 电池电量: ${String.format("%.1f", currentMetrics.batteryLevel)}%")
                appendLine()
                appendLine("性能等级: ${calculatePerformanceLevel(currentMetrics)}")
                
                if (currentAlerts.isNotEmpty()) {
                    appendLine()
                    appendLine("性能警告:")
                    currentAlerts.forEach { alert ->
                        appendLine("- [${alert.level}] ${alert.message}")
                    }
                }
            }
            
            UnifyResult.Success(report)
        } catch (e: Exception) {
            UnifyResult.Error("导出性能报告失败: ${e.message}")
        }
    }
    
    private suspend fun startMetricsCollection() {
        // 在实际实现中，这里会启动一个协程定期收集指标
        // 目前提供基础框架
    }
    
    private fun collectCurrentMetrics(): UnifyPerformanceMetrics {
        // 平台特定的指标收集将在actual实现中完成
        return UnifyPerformanceMetrics(
            cpuUsage = getCpuUsage(),
            memoryUsage = getMemoryUsage(),
            memoryTotal = getTotalMemory(),
            frameRate = calculateFrameRate(),
            renderTime = getLastRenderTime(),
            networkLatency = getNetworkLatency(),
            batteryLevel = getBatteryLevel(),
            timestamp = System.currentTimeMillis()
        )
    }
    
    private fun getCpuUsage(): Float {
        // 平台特定实现
        return 0f
    }
    
    private fun getMemoryUsage(): Long {
        // 平台特定实现
        return 0L
    }
    
    private fun getTotalMemory(): Long {
        // 平台特定实现
        return 0L
    }
    
    private fun calculateFrameRate(): Float {
        if (frameHistory.isEmpty()) return 0f
        
        val totalTime = frameHistory.sum()
        val avgFrameTime = totalTime.toFloat() / frameHistory.size
        return if (avgFrameTime > 0) MILLISECONDS_PER_SECOND / avgFrameTime else 0f
    }
    
    private fun getLastRenderTime(): Long {
        return frameHistory.lastOrNull() ?: 0L
    }
    
    private fun getNetworkLatency(): Long {
        // 平台特定实现
        return 0L
    }
    
    private fun getBatteryLevel(): Float {
        // 平台特定实现
        return 100f
    }
    
    fun recordFrameTime(frameTime: Long) {
        frameHistory.add(frameTime)
        if (frameHistory.size > FRAME_HISTORY_SIZE) {
            frameHistory.removeAt(0)
        }
    }
    
    private fun checkThresholds(metrics: UnifyPerformanceMetrics) {
        val newAlerts = mutableListOf<UnifyPerformanceAlert>()
        
        // CPU使用率检查
        if (metrics.cpuUsage > thresholds.maxCpuUsage) {
            newAlerts.add(
                UnifyPerformanceAlert(
                    level = if (metrics.cpuUsage > 95f) UnifyPerformanceLevel.CRITICAL else UnifyPerformanceLevel.POOR,
                    message = "CPU使用率过高: ${String.format("%.1f", metrics.cpuUsage)}%",
                    metric = "cpu_usage",
                    value = metrics.cpuUsage,
                    threshold = thresholds.maxCpuUsage
                )
            )
        }
        
        // 内存使用率检查
        if (metrics.memoryUsagePercent > thresholds.maxMemoryUsage) {
            newAlerts.add(
                UnifyPerformanceAlert(
                    level = if (metrics.memoryUsagePercent > 95f) UnifyPerformanceLevel.CRITICAL else UnifyPerformanceLevel.POOR,
                    message = "内存使用率过高: ${String.format("%.1f", metrics.memoryUsagePercent)}%",
                    metric = "memory_usage",
                    value = metrics.memoryUsagePercent,
                    threshold = thresholds.maxMemoryUsage
                )
            )
        }
        
        // 帧率检查
        if (metrics.frameRate < thresholds.minFrameRate && metrics.frameRate > 0) {
            newAlerts.add(
                UnifyPerformanceAlert(
                    level = if (metrics.frameRate < 15f) UnifyPerformanceLevel.CRITICAL else UnifyPerformanceLevel.POOR,
                    message = "帧率过低: ${String.format("%.1f", metrics.frameRate)} FPS",
                    metric = "frame_rate",
                    value = metrics.frameRate,
                    threshold = thresholds.minFrameRate
                )
            )
        }
        
        // 渲染时间检查
        if (metrics.renderTime > thresholds.maxRenderTime) {
            newAlerts.add(
                UnifyPerformanceAlert(
                    level = if (metrics.renderTime > 33L) UnifyPerformanceLevel.CRITICAL else UnifyPerformanceLevel.POOR,
                    message = "渲染时间过长: ${metrics.renderTime}ms",
                    metric = "render_time",
                    value = metrics.renderTime.toFloat(),
                    threshold = thresholds.maxRenderTime.toFloat()
                )
            )
        }
        
        // 网络延迟检查
        if (metrics.networkLatency > thresholds.maxNetworkLatency) {
            newAlerts.add(
                UnifyPerformanceAlert(
                    level = if (metrics.networkLatency > 3000L) UnifyPerformanceLevel.CRITICAL else UnifyPerformanceLevel.POOR,
                    message = "网络延迟过高: ${metrics.networkLatency}ms",
                    metric = "network_latency",
                    value = metrics.networkLatency.toFloat(),
                    threshold = thresholds.maxNetworkLatency.toFloat()
                )
            )
        }
        
        // 电池电量检查
        if (metrics.batteryLevel < thresholds.minBatteryLevel) {
            newAlerts.add(
                UnifyPerformanceAlert(
                    level = if (metrics.batteryLevel < 10f) UnifyPerformanceLevel.CRITICAL else UnifyPerformanceLevel.POOR,
                    message = "电池电量过低: ${String.format("%.1f", metrics.batteryLevel)}%",
                    metric = "battery_level",
                    value = metrics.batteryLevel,
                    threshold = thresholds.minBatteryLevel
                )
            )
        }
        
        _alerts.value = newAlerts
    }
    
    private fun calculatePerformanceLevel(metrics: UnifyPerformanceMetrics): UnifyPerformanceLevel {
        var score = 100
        
        // CPU评分
        when {
            metrics.cpuUsage > 90f -> score -= 30
            metrics.cpuUsage > 70f -> score -= 20
            metrics.cpuUsage > 50f -> score -= 10
        }
        
        // 内存评分
        when {
            metrics.memoryUsagePercent > 90f -> score -= 25
            metrics.memoryUsagePercent > 70f -> score -= 15
            metrics.memoryUsagePercent > 50f -> score -= 8
        }
        
        // 帧率评分
        when {
            metrics.frameRate < 15f && metrics.frameRate > 0 -> score -= 35
            metrics.frameRate < 30f && metrics.frameRate > 0 -> score -= 20
            metrics.frameRate < 45f && metrics.frameRate > 0 -> score -= 10
        }
        
        // 渲染时间评分
        when {
            metrics.renderTime > 33L -> score -= 20
            metrics.renderTime > 20L -> score -= 10
        }
        
        return when {
            score >= 90 -> UnifyPerformanceLevel.EXCELLENT
            score >= 75 -> UnifyPerformanceLevel.GOOD
            score >= 60 -> UnifyPerformanceLevel.FAIR
            score >= 40 -> UnifyPerformanceLevel.POOR
            else -> UnifyPerformanceLevel.CRITICAL
        }
    }
}

// 全局性能监控实例
expect fun createPlatformPerformanceMonitor(): UnifyPerformanceMonitor

object UnifyPerformance {
    private var _monitor: UnifyPerformanceMonitor? = null
    
    val monitor: UnifyPerformanceMonitor
        get() = _monitor ?: createPlatformPerformanceMonitor().also { _monitor = it }
    
    suspend fun startMonitoring(): UnifyResult<Unit> = monitor.startMonitoring()
    suspend fun stopMonitoring(): UnifyResult<Unit> = monitor.stopMonitoring()
    suspend fun getCurrentMetrics(): UnifyResult<UnifyPerformanceMetrics> = monitor.getCurrentMetrics()
    suspend fun getPerformanceLevel(): UnifyResult<UnifyPerformanceLevel> = monitor.getPerformanceLevel()
}
