package com.unify.helloworld

import platform.UIKit.UIDevice

/**
 * iOS 平台信息实现
 */
actual fun getPlatformName(): String = "iOS ${UIDevice.currentDevice.systemVersion}"
