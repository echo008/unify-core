package com.unify.helloworld

/**
 * 跨平台信息接口
 * 定义获取平台名称和设备信息的统一接口
 */
expect fun getPlatformName(): String

/**
 * 获取设备详细信息
 */
expect fun getDeviceInfo(): String

/**
 * 平台信息数据类
 */
data class PlatformInfo(
    val name: String,
    val version: String,
    val deviceInfo: String,
    val architecture: String,
    val isDebug: Boolean = false
)

/**
 * 简单平台信息类
 * 提供基础平台信息获取功能
 */
class SimplePlatformInfo {
    fun getCurrentPlatform(): PlatformInfo {
        return PlatformInfo(
            name = getPlatformName(),
            version = getPlatformVersion(),
            deviceInfo = getDeviceInfo(),
            architecture = getArchitecture(),
            isDebug = isDebugBuild()
        )
    }
    
    private fun getPlatformVersion(): String {
        return "1.0.0"
    }
    
    private fun getArchitecture(): String {
        return System.getProperty("os.arch") ?: "unknown"
    }
    
    private fun isDebugBuild(): Boolean {
        return true // 在实际项目中应该根据构建配置确定
    }
}
