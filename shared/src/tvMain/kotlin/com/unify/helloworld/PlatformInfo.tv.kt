package com.unify.helloworld

/**
 * TV平台实现
 */
actual fun getPlatformName(): String = "TV"

/**
 * 获取TV设备详细信息
 */
actual fun getDeviceInfo(): String {
    return buildString {
        append("Platform: Smart TV\n")
        append("Display: 4K HDR\n")
        append("Audio: Dolby Atmos\n")
        append("Remote Control: IR/Bluetooth\n")
        append("HDMI: 2.1\n")
        append("Network: WiFi 6/Ethernet\n")
        append("Storage: 32GB Internal")
    }
}
