package com.unify.core.platform

/**
 * HarmonyOS平台管理器接口
 * 提供HarmonyOS特有的平台功能
 */
expect class HarmonyOSPlatformManager {
    
    /**
     * 初始化HarmonyOS平台管理器
     */
    fun initialize()
    
    /**
     * 获取HarmonyOS版本
     */
    fun getHarmonyOSVersion(): String
    
    /**
     * 获取设备能力信息
     */
    fun getDeviceCapabilities(): HarmonyOSDeviceCapabilities
    
    /**
     * 获取分布式设备列表
     */
    suspend fun getDistributedDevices(): List<HarmonyOSDevice>
    
    /**
     * 启动跨设备协同
     */
    suspend fun startCrossDeviceCollaboration(deviceId: String): Boolean
    
    /**
     * 获取应用上下文
     */
    fun getApplicationContext(): Any?
}

/**
 * HarmonyOS设备能力信息
 */
data class HarmonyOSDeviceCapabilities(
    val supportDistributedComputing: Boolean,
    val supportCrossDeviceUI: Boolean,
    val supportHiAI: Boolean,
    val supportAREngine: Boolean,
    val deviceType: HarmonyOSDeviceType
)

/**
 * HarmonyOS设备类型
 */
enum class HarmonyOSDeviceType {
    PHONE, TABLET, TV, WATCH, CAR, SPEAKER, UNKNOWN
}

/**
 * HarmonyOS分布式设备信息
 */
data class HarmonyOSDevice(
    val deviceId: String,
    val deviceName: String,
    val deviceType: HarmonyOSDeviceType,
    val isOnline: Boolean,
    val capabilities: HarmonyOSDeviceCapabilities
)
