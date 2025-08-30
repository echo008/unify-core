package com.unify.helloworld

import platform.UIKit.UIDevice

actual class PlatformInfo {
    actual companion object {
        actual fun getPlatformName(): String = "iOS"
        
        actual fun getDeviceInfo(): String {
            val device = UIDevice.currentDevice
            return "${device.model} ${device.systemName} ${device.systemVersion}"
        }
    }
}
