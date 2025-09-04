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
import java.lang.management.ManagementFactory
import java.lang.management.MemoryMXBean
import java.lang.management.OperatingSystemMXBean
import javax.management.MBeanServer
import javax.management.ObjectName

/**
 * Desktop平台性能监控实现
 */
actual fun createPlatformPerformanceMonitor(): UnifyPerformanceMonitor {
    return DesktopPerformanceMonitor()
}

class DesktopPerformanceMonitor : UnifyPerformanceMonitor {
    private val _metrics = MutableStateFlow(UnifyPerformanceMetrics())
    override val metrics: StateFlow<UnifyPerformanceMetrics> = _metrics.asStateFlow()
    
    private val _alerts = MutableStateFlow<List<UnifyPerformanceAlert>>(emptyList())
    override val alerts: StateFlow<List<UnifyPerformanceAlert>> = _alerts.asStateFlow()
    
    private val _isMonitoring = MutableStateFlow(false)
    override val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()
    
    private var monitoringJob: Job? = null
    private var thresholds = UnifyPerformanceThresholds()
    private val frameHistory = mutableListOf<Long>()
    
    private val osBean: OperatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean()
    private val memoryBean: MemoryMXBean = ManagementFactory.getMemoryMXBean()
    private val mBeanServer: MBeanServer = ManagementFactory.getPlatformMBeanServer()
    
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
            UnifyResult.Error("启动Desktop性能监控失败: ${e.message}")
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
            UnifyResult.Error("停止Desktop性能监控失败: ${e.message}")
        }
    }
    
    override suspend fun getCurrentMetrics(): UnifyResult<UnifyPerformanceMetrics> {
        return try {
            val currentMetrics = collectCurrentMetrics()
            _metrics.value = currentMetrics
            checkThresholds(currentMetrics)
            UnifyResult.Success(currentMetrics)
        } catch (e: Exception) {
            UnifyResult.Error("获取Desktop性能指标失败: ${e.message}")
        }
    }
    
    override suspend fun getPerformanceLevel(): UnifyResult<UnifyPerformanceLevel> {
        return try {
            val currentMetrics = _metrics.value
            val level = calculatePerformanceLevel(currentMetrics)
            UnifyResult.Success(level)
        } catch (e: Exception) {
            UnifyResult.Error("计算Desktop性能等级失败: ${e.message}")
        }
    }
    
    override suspend fun setThresholds(thresholds: UnifyPerformanceThresholds): UnifyResult<Unit> {
        return try {
            this.thresholds = thresholds
            UnifyResult.Success(Unit)
        } catch (e: Exception) {
            UnifyResult.Error("设置Desktop性能阈值失败: ${e.message}")
        }
    }
    
    override suspend fun clearAlerts(): UnifyResult<Unit> {
        return try {
            _alerts.value = emptyList()
            UnifyResult.Success(Unit)
        } catch (e: Exception) {
            UnifyResult.Error("清除Desktop性能警告失败: ${e.message}")
        }
    }
    
    override suspend fun exportMetrics(): UnifyResult<String> {
        return try {
            val currentMetrics = _metrics.value
            val currentAlerts = _alerts.value
            
            val report = buildString {
                appendLine("=== Desktop 性能监控报告 ===")
                appendLine("时间: ${currentMetrics.timestamp}")
                appendLine("操作系统: ${System.getProperty("os.name")} ${System.getProperty("os.version")}")
                appendLine("Java版本: ${System.getProperty("java.version")}")
                appendLine("处理器: ${osBean.arch} (${osBean.availableProcessors} 核心)")
                appendLine()
                appendLine("性能指标:")
                appendLine("- CPU使用率: ${String.format("%.1f", currentMetrics.cpuUsage)}%")
                appendLine("- 内存使用: ${currentMetrics.memoryUsage / BYTES_TO_MB}MB / ${currentMetrics.memoryTotal / BYTES_TO_MB}MB")
                appendLine("- 帧率: ${String.format("%.1f", currentMetrics.frameRate)} FPS")
                appendLine("- 渲染时间: ${currentMetrics.renderTime}ms")
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
            UnifyResult.Error("导出Desktop性能报告失败: ${e.message}")
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
            cpuUsage = getCpuUsage(),
            memoryUsage = getMemoryUsage(),
            memoryTotal = getTotalMemory(),
            frameRate = calculateFrameRate(),
            renderTime = getLastRenderTime(),
            networkLatency = 0L, // Desktop平台网络延迟需要单独测试
            batteryLevel = getBatteryLevel(),
            timestamp = System.currentTimeMillis()
        )
    }
    
    private fun getCpuUsage(): Float {
        return try {
            // 使用OperatingSystemMXBean获取CPU使用率
            when (val cpuUsage = osBean.processCpuLoad) {
                -1.0 -> {
                    // 如果不支持，尝试使用系统负载
                    val systemLoad = osBean.systemLoadAverage
                    if (systemLoad >= 0) {
                        (systemLoad / osBean.availableProcessors * 100).toFloat().coerceIn(0f, 100f)
                    } else {
                        0f
                    }
                }
                else -> (cpuUsage * 100).toFloat().coerceIn(0f, 100f)
            }
        } catch (e: Exception) {
            0f
        }
    }
    
    private fun getMemoryUsage(): Long {
        return try {
            val heapMemory = memoryBean.heapMemoryUsage
            val nonHeapMemory = memoryBean.nonHeapMemoryUsage
            heapMemory.used + nonHeapMemory.used
        } catch (e: Exception) {
            Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        }
    }
    
    private fun getTotalMemory(): Long {
        return try {
            val heapMemory = memoryBean.heapMemoryUsage
            val nonHeapMemory = memoryBean.nonHeapMemoryUsage
            heapMemory.max + nonHeapMemory.max
        } catch (e: Exception) {
            Runtime.getRuntime().maxMemory()
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
            // Desktop平台通常连接电源，返回100%
            // 在笔记本电脑上可以通过系统API获取真实电池信息
            when (System.getProperty("os.name").lowercase()) {
                "windows" -> getWindowsBatteryLevel()
                "mac os x", "darwin" -> getMacBatteryLevel()
                "linux" -> getLinuxBatteryLevel()
                else -> 100f
            }
        } catch (e: Exception) {
            100f
        }
    }
    
    private fun getWindowsBatteryLevel(): Float {
        return try {
            // 在Windows上可以通过WMI查询电池状态
            // 这里简化实现
            100f
        } catch (e: Exception) {
            100f
        }
    }
    
    private fun getMacBatteryLevel(): Float {
        return try {
            // 在macOS上可以通过IOKit查询电池状态
            // 这里简化实现
            100f
        } catch (e: Exception) {
            100f
        }
    }
    
    private fun getLinuxBatteryLevel(): Float {
        return try {
            // 在Linux上可以通过/sys/class/power_supply/查询电池状态
            val batteryPath = java.io.File("/sys/class/power_supply/BAT0/capacity")
            if (batteryPath.exists()) {
                batteryPath.readText().trim().toFloat()
            } else {
                100f
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
        val memoryPercent = if (metrics.memoryTotal > 0) {
            (metrics.memoryUsage.toFloat() / metrics.memoryTotal) * 100f
        } else 0f
        
        when {
            memoryPercent > 90f -> score -= 25
            memoryPercent > 70f -> score -= 15
            memoryPercent > 50f -> score -= 8
        }
        
        // 帧率评分
        when {
            metrics.frameRate < 15f && metrics.frameRate > 0 -> score -= 35
            metrics.frameRate < 30f && metrics.frameRate > 0 -> score -= 20
            metrics.frameRate < 45f && metrics.frameRate > 0 -> score -= 10
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
