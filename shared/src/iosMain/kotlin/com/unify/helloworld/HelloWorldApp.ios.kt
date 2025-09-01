package com.unify.helloworld

import platform.UIKit.UIDevice

/**
 * iOS平台Hello World实现
 */
actual fun getPlatformName(): String {
    val device = UIDevice.currentDevice
    return "iOS ${device.systemVersion} (${device.model})"
}
