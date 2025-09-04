package com.unify.helloworld

import platform.UIKit.UIDevice

actual fun getPlatformName(): String {
    return "iOS ${UIDevice.currentDevice.systemVersion}"
}
