package com.unify.helloworld

/**
 * Watch平台实现
 */
actual fun getPlatformName(): String = "Watch"

/**
 * 获取Watch设备详细信息
 */
actual fun getDeviceInfo(): String {
    return buildString {
        append("Platform: Smart Watch\n")
        append("Display: AMOLED 1.4\"\n")
        append("Health Sensors: Heart Rate, SpO2, GPS\n")
        append("Battery: 300mAh\n")
        append("Connectivity: Bluetooth 5.0, WiFi\n")
        append("Water Resistance: 5ATM\n")
        append("Storage: 4GB Internal")
    }
}
