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
import platform.Foundation.NSProcessInfo
import platform.UIKit.UIDevice

/**
 * iOS平台性能监控实现
 */
actual fun createPlatformPerformanceMonitor(): UnifyPerformanceMonitor {
    return IOSPerformanceMonitor()
}

class IOSPerformanceMonitor : UnifyPerformanceMonitor {
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
        } catch (e: Exception) {
            UnifyResult.Error("启动iOS性能监控失败: ${e.message}")
        }
    }
    
    override suspend fun stopMonitoring(): UnifyResult<Unit> {
        return try {
            _isMonitoring.value = false
            monitoringJob?.cancel()
            monitoringJob = null
            frameHistory.clear()
            UnifyResult.Success(Unit)
        } catch (e: Exception) {
            UnifyResult.Error("停止iOS性能监控失败: ${e.message}")
        }
    }
    
    override suspend fun getCurrentMetrics(): UnifyResult<UnifyPerformanceMetrics> {
        return try {
            val currentMetrics = collectCurrentMetrics()
            _metrics.value = currentMetrics
            checkThresholds(currentMetrics)
            UnifyResult.Success(currentMetrics)
        } catch (e: Exception) {
            UnifyResult.Error("获取iOS性能指标失败: ${e.message}")
        }
    }
    
    override suspend fun getPerformanceLevel(): UnifyResult<UnifyPerformanceLevel> {
        return try {
            val currentMetrics = _metrics.value
            val level = calculatePerformanceLevel(currentMetrics)
            UnifyResult.Success(level)
        } catch (e: Exception) {
            UnifyResult.Error("计算iOS性能等级失败: ${e.message}")
        }
    }
    
    override suspend fun setThresholds(thresholds: UnifyPerformanceThresholds): UnifyResult<Unit> {
        return try {
            this.thresholds = thresholds
            UnifyResult.Success(Unit)
        } catch (e: Exception) {
            UnifyResult.Error("设置iOS性能阈值失败: ${e.message}")
        }
    }
    
    override suspend fun clearAlerts(): UnifyResult<Unit> {
        return try {
            _alerts.value = emptyList()
            UnifyResult.Success(Unit)
        } catch (e: Exception) {
            UnifyResult.Error("清除iOS性能警告失败: ${e.message}")
        }
    }
    
    override suspend fun exportMetrics(): UnifyResult<String> {
        return try {
            val currentMetrics = _metrics.value
            val currentAlerts = _alerts.value
            val device = UIDevice.currentDevice
            
            val report = buildString {
                appendLine("=== iOS 性能监控报告 ===")
                appendLine("时间: ${currentMetrics.timestamp}")
                appendLine("设备: ${device.model}")
                appendLine("系统版本: ${device.systemName} ${device.systemVersion}")
                appendLine("处理器: ${NSProcessInfo.processInfo.processorCount} 核心")
                appendLine()
                appendLine("性能指标:")
                appendLine("- 内存使用: ${currentMetrics.memoryUsage / BYTES_TO_MB}MB")
                appendLine("- 帧率: ${String.format("%.1f", currentMetrics.frameRate)} FPS")
                appendLine("- 渲染时间: ${currentMetrics.renderTime}ms")
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
            UnifyResult.Error("导出iOS性能报告失败: ${e.message}")
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
            cpuUsage = 0f, // iOS平台CPU使用率需要特殊权限
            memoryUsage = getMemoryUsage(),
            memoryTotal = getTotalMemory(),
            frameRate = calculateFrameRate(),
            renderTime = getLastRenderTime(),
            networkLatency = 0L, // 需要网络测试实现
            batteryLevel = getBatteryLevel(),
            timestamp = platform.Foundation.NSDate().timeIntervalSince1970.toLong() * 1000
        )
    }
    
    private fun getMemoryUsage(): Long {
        return try {
            // 使用mach API获取内存使用情况
            // 这里简化实现，实际需要调用mach_task_basic_info
            0L
        } catch (e: Exception) {
            0L
        }
    }
    
    private fun getTotalMemory(): Long {
        return try {
            // 使用NSProcessInfo获取物理内存
            NSProcessInfo.processInfo.physicalMemory.toLong()
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
    
    private fun getBatteryLevel(): Float {
        return try {
            val device = UIDevice.currentDevice
            device.batteryMonitoringEnabled = true
            val level = device.batteryLevel
            device.batteryMonitoringEnabled = false
            
            if (level >= 0) {
                level * 100f
            } else {
                100f // 无法获取时默认满电
            }
        } catch (e: Exception) {
            100f
        }
    }
    
    fun recordFrameTime(frameTime: Long) {
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
                    message = "内存使用率过高: ${String.format("%.1f", memoryPercent)}%",
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
                    message = "帧率过低: ${String.format("%.1f", metrics.frameRate)} FPS",
                    metric = "frame_rate",
                    value = metrics.frameRate,
                    threshold = thresholds.minFrameRate
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
        
        // 电池评分
        when {
            metrics.batteryLevel < 10f -> score -= 25
            metrics.batteryLevel < 20f -> score -= 15
            metrics.batteryLevel < 30f -> score -= 10
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
