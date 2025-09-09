package com.unify.core.types

import kotlinx.serialization.Serializable

/**
 * 系统信息数据类
 * 用于表示设备的系统信息
 */
@Serializable
data class SystemInfo(
    val operatingSystem: String,
    val version: String,
    val deviceName: String,
    val deviceModel: String,
    val architecture: String,
    val totalMemory: Long,
    val availableMemory: Long,
    val batteryLevel: Float? = null,
    val isCharging: Boolean? = null,
)
