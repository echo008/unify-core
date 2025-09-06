package com.unify.helloworld

import android.os.Build

/**
 * Android平台信息实现
 */
actual fun getPlatformName(): String {
    return "Android ${Build.VERSION.RELEASE}"
}

actual fun getDeviceInfo(): String {
    return buildString {
        append("设备型号: ${Build.MODEL}\n")
        append("制造商: ${Build.MANUFACTURER}\n")
        append("Android版本: ${Build.VERSION.RELEASE}\n")
        append("API级别: ${Build.VERSION.SDK_INT}\n")
        append("处理器架构: ${Build.SUPPORTED_ABIS.firstOrNull() ?: "未知"}")
    }
}
