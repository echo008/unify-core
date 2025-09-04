package com.unify.helloworld

/**
 * HarmonyOS平台实现
 */
actual fun getPlatformName(): String = "HarmonyOS"

/**
 * 获取HarmonyOS设备详细信息
 */
actual fun getDeviceInfo(): String {
    return buildString {
        append("Platform: HarmonyOS\n")
        append("API Version: 12\n")
        append("Device Type: Phone\n")
        append("Distributed Capability: Enabled\n")
        append("Multi-Screen Support: Yes\n")
        append("Atomic Service: Supported")
    }
}
