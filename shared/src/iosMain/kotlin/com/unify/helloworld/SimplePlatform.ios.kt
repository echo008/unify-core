package com.unify.helloworld

import platform.UIKit.UIDevice

actual class SimplePlatformInfo {
    actual fun getPlatformName(): String = "iOS"
    
    actual fun getDeviceInfo(): String {
        val device = UIDevice.currentDevice
        return buildString {
            append("Device: ${device.model}\n")
            append("OS: iOS ${device.systemVersion}\n")
            append("Name: ${device.name}\n")
            append("Identifier: ${device.identifierForVendor?.UUIDString ?: "Unknown"}")
        }
    }
}
