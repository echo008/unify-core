package com.unify.helloworld

import platform.UIKit.UIDevice

actual fun getPlatformName(): String = "iOS"

actual fun getDeviceInfo(): String {
    val device = UIDevice.currentDevice
    return "iOS ${device.systemVersion} on ${device.model}"
}
