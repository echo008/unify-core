package com.unify.core.types

/**
 * 性能监控指标枚举
 * 定义了系统性能监控的各种指标类型
 */
enum class PerformanceMetric {
    /**
     * CPU使用率 (百分比)
     */
    CPU_USAGE,

    /**
     * 内存使用率 (百分比)
     */
    MEMORY_USAGE,

    /**
     * 电池电量 (百分比)
     */
    BATTERY_LEVEL,

    /**
     * 网络速度 (Mbps)
     */
    NETWORK_SPEED,

    /**
     * 帧率 (FPS)
     */
    FPS,

    /**
     * 设备温度 (摄氏度)
     */
    TEMPERATURE,
}
