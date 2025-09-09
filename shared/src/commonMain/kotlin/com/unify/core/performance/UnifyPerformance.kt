package com.unify.core.performance

import com.unify.core.types.UnifyResult

/**
 * Unify性能监控统一入口
 */
object UnifyPerformance {
    /**
     * 默认性能监控器实例
     */
    val monitor: UnifyPerformanceMonitor by lazy {
        UnifyPerformanceMonitorImpl()
    }

    /**
     * 开始性能监控
     */
    suspend fun startMonitoring(): UnifyResult<Unit> {
        return monitor.startMonitoring()
    }

    /**
     * 停止性能监控
     */
    suspend fun stopMonitoring(): UnifyResult<Unit> {
        return monitor.stopMonitoring()
    }

    /**
     * 获取当前性能指标
     */
    suspend fun getCurrentMetrics(): UnifyResult<UnifyPerformanceMetrics> {
        return monitor.getCurrentMetrics()
    }

    /**
     * 获取性能等级
     */
    suspend fun getPerformanceLevel(): UnifyResult<UnifyPerformanceLevel> {
        return monitor.getPerformanceLevel()
    }

    /**
     * 导出性能报告
     */
    suspend fun exportMetrics(): UnifyResult<String> {
        return monitor.exportMetrics()
    }

    /**
     * 清除告警
     */
    suspend fun clearAlerts(): UnifyResult<Unit> {
        return monitor.clearAlerts()
    }
}
