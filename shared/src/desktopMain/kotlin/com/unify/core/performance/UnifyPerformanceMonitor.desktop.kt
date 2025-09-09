package com.unify.core.performance

import com.unify.core.types.UnifyResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Desktop平台性能监控器完整实现
 */
class DesktopPerformanceMonitor : UnifyPerformanceMonitor {
    private val _metrics = MutableStateFlow(UnifyPerformanceMetrics())
    override val metrics: StateFlow<UnifyPerformanceMetrics> = _metrics.asStateFlow()

    private val _alerts = MutableStateFlow<List<UnifyPerformanceAlert>>(emptyList())
    override val alerts: StateFlow<List<UnifyPerformanceAlert>> = _alerts.asStateFlow()

    private val _isMonitoring = MutableStateFlow(false)
    override val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()

    private var thresholds = UnifyPerformanceThresholds()

    override suspend fun startMonitoring(): UnifyResult<Unit> {
        return try {
            _isMonitoring.value = true
            // 启动Desktop平台性能监控
            updateMetrics()
            UnifyResult.Success(Unit)
        } catch (e: Exception) {
            UnifyResult.Failure(com.unify.core.error.UnifyPerformanceException("Desktop平台启动性能监控失败: ${e.message}", e))
        }
    }

    override suspend fun stopMonitoring(): UnifyResult<Unit> {
        return try {
            _isMonitoring.value = false
            UnifyResult.Success(Unit)
        } catch (e: Exception) {
            UnifyResult.Failure(com.unify.core.error.UnifyPerformanceException("Desktop平台停止性能监控失败: ${e.message}", e))
        }
    }

    override suspend fun getCurrentMetrics(): UnifyResult<UnifyPerformanceMetrics> {
        return try {
            updateMetrics()
            UnifyResult.Success(_metrics.value)
        } catch (e: Exception) {
            UnifyResult.Failure(com.unify.core.error.UnifyPerformanceException("Desktop平台获取性能指标失败: ${e.message}", e))
        }
    }

    override suspend fun getPerformanceLevel(): UnifyResult<UnifyPerformanceLevel> {
        return try {
            val currentMetrics = _metrics.value
            val level = calculatePerformanceLevel(currentMetrics)
            UnifyResult.Success(level)
        } catch (e: Exception) {
            UnifyResult.Failure(com.unify.core.error.UnifyPerformanceException("Desktop平台计算性能等级失败: ${e.message}", e))
        }
    }

    override suspend fun setThresholds(thresholds: UnifyPerformanceThresholds): UnifyResult<Unit> {
        return try {
            this.thresholds = thresholds
            UnifyResult.Success(Unit)
        } catch (e: Exception) {
            UnifyResult.Failure(com.unify.core.error.UnifyPerformanceException("Desktop平台设置性能阈值失败: ${e.message}", e))
        }
    }

    override suspend fun clearAlerts(): UnifyResult<Unit> {
        return try {
            _alerts.value = emptyList()
            UnifyResult.Success(Unit)
        } catch (e: Exception) {
            UnifyResult.Failure(com.unify.core.error.UnifyPerformanceException("Desktop平台清除告警失败: ${e.message}", e))
        }
    }

    override suspend fun exportMetrics(): UnifyResult<String> {
        return try {
            val currentMetrics = _metrics.value
            val report =
                buildString {
                    appendLine("=== Desktop平台性能监控报告 ===")
                    appendLine("时间: ${currentMetrics.timestamp}")
                    appendLine("性能指标:")
                    appendLine("- CPU使用率: ${currentMetrics.cpuUsage}%")
                    appendLine("- 内存使用: ${currentMetrics.memoryUsage / (1024 * 1024)}MB")
                    appendLine("- 内存总量: ${currentMetrics.memoryTotal / (1024 * 1024)}MB")
                    appendLine("- 帧率: ${currentMetrics.frameRate} FPS")
                    appendLine("- 渲染时间: ${currentMetrics.renderTime}ms")
                    appendLine("- 网络延迟: ${currentMetrics.networkLatency}ms")
                    appendLine("- 电池电量: ${currentMetrics.batteryLevel}%")
                }
            UnifyResult.Success(report)
        } catch (e: Exception) {
            UnifyResult.Failure(com.unify.core.error.UnifyPerformanceException("Desktop平台导出性能指标失败: ${e.message}", e))
        }
    }

    override fun recordFrameTime(frameTime: Long) {
        // Desktop平台记录帧时间
        val currentMetrics = _metrics.value
        _metrics.value =
            currentMetrics.copy(
                renderTime = frameTime,
                frameRate = if (frameTime > 0) 1000f / frameTime else 0f,
                timestamp = System.currentTimeMillis(),
            )
    }

    private fun updateMetrics() {
        val runtime = Runtime.getRuntime()
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()
        val usedMemory = totalMemory - freeMemory
        val maxMemory = runtime.maxMemory()

        // 获取系统CPU使用率（简化实现）
        val cpuUsage = getCpuUsage()

        val newMetrics =
            UnifyPerformanceMetrics(
                cpuUsage = cpuUsage,
                memoryUsage = usedMemory,
                memoryTotal = maxMemory,
                frameRate = 60.0f, // Desktop平台默认60FPS
                renderTime = 16L, // 约16ms渲染时间
                networkLatency = 50L, // 默认网络延迟
                batteryLevel = 100f, // Desktop平台电池电量（AC供电）
                timestamp = System.currentTimeMillis(),
            )

        _metrics.value = newMetrics
        checkThresholds(newMetrics)
    }

    private fun getCpuUsage(): Float {
        // Desktop平台CPU使用率获取（简化实现）
        return try {
            val osBean = java.lang.management.ManagementFactory.getOperatingSystemMXBean()
            if (osBean is com.sun.management.OperatingSystemMXBean) {
                (osBean.processCpuLoad * 100).toFloat()
            } else {
                45.0f // 默认值
            }
        } catch (e: Exception) {
            45.0f // 默认值
        }
    }

    private fun calculatePerformanceLevel(metrics: UnifyPerformanceMetrics): UnifyPerformanceLevel {
        var score = 100

        // CPU使用率评分
        when {
            metrics.cpuUsage > 90f -> score -= 30
            metrics.cpuUsage > 70f -> score -= 20
            metrics.cpuUsage > 50f -> score -= 10
        }

        // 内存使用率评分
        val memoryUsagePercent = metrics.memoryUsagePercent
        when {
            memoryUsagePercent > 90f -> score -= 25
            memoryUsagePercent > 70f -> score -= 15
            memoryUsagePercent > 50f -> score -= 8
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

    private fun checkThresholds(metrics: UnifyPerformanceMetrics) {
        val alerts = mutableListOf<UnifyPerformanceAlert>()

        if (metrics.cpuUsage > thresholds.maxCpuUsage) {
            alerts.add(
                UnifyPerformanceAlert(
                    level = UnifyPerformanceLevel.POOR,
                    message = "CPU使用率过高",
                    metric = "cpuUsage",
                    value = metrics.cpuUsage,
                    threshold = thresholds.maxCpuUsage,
                ),
            )
        }

        if (metrics.memoryUsagePercent > thresholds.maxMemoryUsage) {
            alerts.add(
                UnifyPerformanceAlert(
                    level = UnifyPerformanceLevel.POOR,
                    message = "内存使用率过高",
                    metric = "memoryUsage",
                    value = metrics.memoryUsagePercent,
                    threshold = thresholds.maxMemoryUsage,
                ),
            )
        }

        if (metrics.frameRate < thresholds.minFrameRate && metrics.frameRate > 0) {
            alerts.add(
                UnifyPerformanceAlert(
                    level = UnifyPerformanceLevel.POOR,
                    message = "帧率过低",
                    metric = "frameRate",
                    value = metrics.frameRate,
                    threshold = thresholds.minFrameRate,
                ),
            )
        }

        _alerts.value = alerts
    }
}

/**
 * Desktop平台性能监控器工厂函数
 */
actual fun createPlatformPerformanceMonitor(): UnifyPerformanceMonitor {
    return DesktopPerformanceMonitor()
}
