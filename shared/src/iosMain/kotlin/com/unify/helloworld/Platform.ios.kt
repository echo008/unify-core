package com.unify.helloworld

import platform.UIKit.UIDevice

actual fun getPlatformName(): String = "iOS"
actual fun getPlatformNameImpl(): String = "iOS"
actual fun getDeviceInfoImpl(): String = "${UIDevice.currentDevice.model} iOS ${UIDevice.currentDevice.systemVersion}"
