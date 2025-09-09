package com.unify.core.performance

import com.unify.core.types.UnifyResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Native平台性能监控实现
 */
actual fun createPlatformPerformanceMonitor(): UnifyPerformanceMonitor {
    return NativePerformanceMonitor()
}

/**
 * Native平台性能监控器实现
 */
private class NativePerformanceMonitor : UnifyPerformanceMonitor {
    private val _metrics = MutableStateFlow(UnifyPerformanceMetrics())
    override val metrics: StateFlow<UnifyPerformanceMetrics> = _metrics

    private val _alerts = MutableStateFlow<List<UnifyPerformanceAlert>>(emptyList())
    override val alerts: StateFlow<List<UnifyPerformanceAlert>> = _alerts

    private val _isMonitoring = MutableStateFlow(false)
    override val isMonitoring: StateFlow<Boolean> = _isMonitoring

    override suspend fun startMonitoring(): UnifyResult<Unit> {
        _isMonitoring.value = true
        return UnifyResult.Success(Unit)
    }

    override suspend fun stopMonitoring(): UnifyResult<Unit> {
        _isMonitoring.value = false
        return UnifyResult.Success(Unit)
    }

    override suspend fun getCurrentMetrics(): UnifyResult<UnifyPerformanceMetrics> {
        val metrics =
            UnifyPerformanceMetrics(
                cpuUsage = 0.0f,
                memoryUsage = 0L,
                memoryTotal = 1024L * 1024 * 1024, // 1GB
                frameRate = 60.0f,
                renderTime = 16L,
                networkLatency = 50L,
                batteryLevel = 100.0f,
            )
        _metrics.value = metrics
        return UnifyResult.Success(metrics)
    }

    override suspend fun getPerformanceLevel(): UnifyResult<UnifyPerformanceLevel> {
        return UnifyResult.Success(UnifyPerformanceLevel.GOOD)
    }

    override suspend fun setThresholds(thresholds: UnifyPerformanceThresholds): UnifyResult<Unit> {
        return UnifyResult.Success(Unit)
    }

    override suspend fun clearAlerts(): UnifyResult<Unit> {
        _alerts.value = emptyList()
        return UnifyResult.Success(Unit)
    }

    override suspend fun exportMetrics(): UnifyResult<String> {
        return UnifyResult.Success("{}")
    }

    override fun recordFrameTime(frameTime: Long) {
        // 记录帧时间
    }
}
