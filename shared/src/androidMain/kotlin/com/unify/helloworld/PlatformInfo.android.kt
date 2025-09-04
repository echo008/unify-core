package com.unify.helloworld

import android.os.Build

/**
 * Android平台实现
 */
actual fun getPlatformName(): String = "Android"

/**
 * 获取Android设备详细信息
 */
actual fun getDeviceInfo(): String {
    return buildString {
        append("Device: ${Build.MANUFACTURER} ${Build.MODEL}\n")
        append("Android Version: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})\n")
        append("Board: ${Build.BOARD}\n")
        append("Brand: ${Build.BRAND}\n")
        append("Hardware: ${Build.HARDWARE}\n")
        append("Product: ${Build.PRODUCT}\n")
        append("Supported ABIs: ${Build.SUPPORTED_ABIS.joinToString(", ")}")
    }
}
