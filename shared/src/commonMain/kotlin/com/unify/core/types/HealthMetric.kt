package com.unify.core.types

import kotlinx.serialization.Serializable

/**
 * 健康状态枚举
 */
enum class HealthStatus {
    EXCELLENT,
    GOOD,
    FAIR,
    POOR,
    CRITICAL,
}

/**
 * 健康指标数据类
 * 用于表示各种健康监控数据
 */
@Serializable
data class HealthMetric(
    val score: Int,
    val status: HealthStatus,
    val details: String,
)
