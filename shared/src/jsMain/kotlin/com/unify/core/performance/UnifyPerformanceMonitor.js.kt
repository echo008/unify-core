package com.unify.core.performance

import com.unify.core.types.UnifyResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.js.Date

/**
 * Web平台性能监控实现
 */
actual fun createPlatformPerformanceMonitor(): UnifyPerformanceMonitor {
    return WebPerformanceMonitor()
}

class WebPerformanceMonitor : UnifyPerformanceMonitor {
    private val _metrics = MutableStateFlow(UnifyPerformanceMetrics())
    override val metrics: StateFlow<UnifyPerformanceMetrics> = _metrics.asStateFlow()
    
    private val _alerts = MutableStateFlow<List<UnifyPerformanceAlert>>(emptyList())
    override val alerts: StateFlow<List<UnifyPerformanceAlert>> = _alerts.asStateFlow()
    
    private val _isMonitoring = MutableStateFlow(false)
    override val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()
    
    private var monitoringJob: Job? = null
    private var thresholds = UnifyPerformanceThresholds()
    private val frameHistory = mutableListOf<Long>()
    
    companion object {
        private const val MONITORING_INTERVAL = 1000L
        private const val FRAME_HISTORY_SIZE = 60
        private const val BYTES_TO_MB = 1024 * 1024
    }
    
    override suspend fun startMonitoring(): UnifyResult<Unit> {
        return try {
            if (_isMonitoring.value) {
                return UnifyResult.Success(Unit)
            }
            
            _isMonitoring.value = true
            startMetricsCollection()
            UnifyResult.Success(Unit)
        } catch (e: Throwable) {
            UnifyResult.Failure(com.unify.core.error.UnifyPerformanceException("启动Web性能监控失败: ${e.message}", e))
        }
    }
    
    override suspend fun stopMonitoring(): UnifyResult<Unit> {
        return try {
            _isMonitoring.value = false
            monitoringJob?.cancel()
            monitoringJob = null
            frameHistory.clear()
            UnifyResult.Success(Unit)
        } catch (e: Throwable) {
            UnifyResult.Failure(com.unify.core.error.UnifyPerformanceException("停止Web性能监控失败: ${e.message}", e))
        }
    }
    
    override suspend fun getCurrentMetrics(): UnifyResult<UnifyPerformanceMetrics> {
        return try {
            val currentMetrics = collectCurrentMetrics()
            _metrics.value = currentMetrics
            checkThresholds(currentMetrics)
            UnifyResult.Success(currentMetrics)
        } catch (e: Exception) {
            UnifyResult.Failure(com.unify.core.error.UnifyPerformanceException("获取Web性能指标失败: ${e.message}", e))
        }
    }
    
    override suspend fun getPerformanceLevel(): UnifyResult<UnifyPerformanceLevel> {
        return try {
            val currentMetrics = _metrics.value
            val level = calculatePerformanceLevel(currentMetrics)
            UnifyResult.Success(level)
        } catch (e: Exception) {
            UnifyResult.Failure(com.unify.core.error.UnifyPerformanceException("计算Web性能等级失败: ${e.message}", e))
        }
    }
    
    override suspend fun setThresholds(thresholds: UnifyPerformanceThresholds): UnifyResult<Unit> {
        return try {
            this.thresholds = thresholds
            UnifyResult.Success(Unit)
        } catch (e: Throwable) {
            UnifyResult.Failure(com.unify.core.error.UnifyPerformanceException("设置Web性能阈值失败: ${e.message}", e))
        }
    }
    
    override suspend fun clearAlerts(): UnifyResult<Unit> {
        return try {
            _alerts.value = emptyList()
            UnifyResult.Success(Unit)
        } catch (e: Throwable) {
            UnifyResult.Failure(com.unify.core.error.UnifyPerformanceException("清除Web性能警报失败: ${e.message}", e))
        }
    }
    
    override suspend fun exportMetrics(): UnifyResult<String> {
        return try {
            val currentMetrics = _metrics.value
            val currentAlerts = _alerts.value
            
            val report = buildString {
                appendLine("=== Web 性能监控报告 ===")
                appendLine("时间: ${currentMetrics.timestamp}")
                appendLine()
                appendLine("性能指标:")
                appendLine("- 内存使用: ${currentMetrics.memoryUsage / BYTES_TO_MB}MB")
                appendLine("- 帧率: ${currentMetrics.frameRate.toString().take(4)} FPS")
                appendLine("- 渲染时间: ${currentMetrics.renderTime}ms")
                appendLine("- 网络延迟: ${currentMetrics.networkLatency}ms")
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
            UnifyResult.Failure(com.unify.core.error.UnifyPerformanceException("导出Web性能报告失败: ${e.message}", e))
        }
    }
    
    private fun startMetricsCollection() {
        monitoringJob = CoroutineScope(Dispatchers.Default).launch {
            while (_isMonitoring.value) {
                try {
                    val metrics = collectCurrentMetrics()
                    _metrics.value = metrics
                    checkThresholds(metrics)
                } catch (e: Exception) {
                    // 记录错误但继续监控
                }
                delay(MONITORING_INTERVAL)
            }
        }
    }
    
    private fun collectCurrentMetrics(): UnifyPerformanceMetrics {
        return UnifyPerformanceMetrics(
            cpuUsage = 0f, // Web平台无法直接获取CPU使用率
            memoryUsage = getMemoryUsage(),
            memoryTotal = getTotalMemory(),
            frameRate = calculateFrameRate(),
            renderTime = getLastRenderTime(),
            networkLatency = getNetworkLatency(),
            batteryLevel = getBatteryLevel(),
            timestamp = Date.now().toLong()
        )
    }
    
    private fun getMemoryUsage(): Long {
        return try {
            // Simplified implementation for JS environment
            256L * 1024L * 1024L // 256MB
        } catch (e: Exception) {
            0L
        }
    }
    
    private fun getTotalMemory(): Long {
        return try {
            // Simplified implementation for JS environment
            1024L * 1024L * 1024L // 1GB
        } catch (e: Exception) {
            0L
        }
    }
    
    private fun calculateFrameRate(): Float {
        if (frameHistory.isEmpty()) return 0f
        
        val totalTime = frameHistory.sum()
        val avgFrameTime = totalTime.toFloat() / frameHistory.size
        return if (avgFrameTime > 0) 1000f / avgFrameTime else 0f
    }
    
    private fun getLastRenderTime(): Long {
        return frameHistory.lastOrNull() ?: 0L
    }
    
    private fun getNetworkLatency(): Long {
        return try {
            // 使用Navigation Timing API估算网络延迟
            val timing = js("performance.timing")
            // Simplified implementation for JS environment
            50L
        } catch (e: Exception) {
            0L
        }
    }
    
    private fun getBatteryLevel(): Float {
        return try {
            // Simplified implementation for JS environment
            100f // 默认满电
        } catch (e: Exception) {
            100f // 默认满电
        }
    }
    
    override fun recordFrameTime(frameTime: Long) {
        frameHistory.add(frameTime)
        if (frameHistory.size > FRAME_HISTORY_SIZE) {
            frameHistory.removeAt(0)
        }
    }
    
    private fun checkThresholds(metrics: UnifyPerformanceMetrics) {
        val newAlerts = mutableListOf<UnifyPerformanceAlert>()
        
        // 内存使用率检查
        val memoryPercent = if (metrics.memoryTotal > 0) {
            (metrics.memoryUsage.toFloat() / metrics.memoryTotal) * 100f
        } else 0f
        
        if (memoryPercent > thresholds.maxMemoryUsage) {
            newAlerts.add(
                UnifyPerformanceAlert(
                    level = if (memoryPercent > 95f) UnifyPerformanceLevel.CRITICAL else UnifyPerformanceLevel.POOR,
                    message = "Memory: ${metrics.memoryUsage.toString().take(5)} MB, CPU: ${metrics.cpuUsage.toString().take(5)}%, FPS: ${metrics.frameRate.toString().take(4)}",
                    metric = "memory_usage",
                    value = memoryPercent,
                    threshold = thresholds.maxMemoryUsage
                )
            )
        }
        
        // 帧率检查
        if (metrics.frameRate < thresholds.minFrameRate && metrics.frameRate > 0) {
            newAlerts.add(
                UnifyPerformanceAlert(
                    level = if (metrics.frameRate < 15f) UnifyPerformanceLevel.CRITICAL else UnifyPerformanceLevel.POOR,
                    message = "帧率过低: ${metrics.frameRate.toString().take(4)} FPS",
                    metric = "frame_rate",
                    value = metrics.frameRate,
                    threshold = thresholds.minFrameRate
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
        
        _alerts.value = newAlerts
    }
    
    private fun calculatePerformanceLevel(metrics: UnifyPerformanceMetrics): UnifyPerformanceLevel {
        var score = 100
        
        // 内存评分
        val memoryPercent = if (metrics.memoryTotal > 0) {
            (metrics.memoryUsage.toFloat() / metrics.memoryTotal) * 100f
        } else 0f
        
        when {
            memoryPercent > 90f -> score -= 30
            memoryPercent > 70f -> score -= 20
            memoryPercent > 50f -> score -= 10
        }
        
        // 帧率评分
        when {
            metrics.frameRate < 15f && metrics.frameRate > 0 -> score -= 40
            metrics.frameRate < 30f && metrics.frameRate > 0 -> score -= 25
            metrics.frameRate < 45f && metrics.frameRate > 0 -> score -= 15
        }
        
        // 网络延迟评分
        when {
            metrics.networkLatency > 2000L -> score -= 25
            metrics.networkLatency > 1000L -> score -= 15
            metrics.networkLatency > 500L -> score -= 10
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
