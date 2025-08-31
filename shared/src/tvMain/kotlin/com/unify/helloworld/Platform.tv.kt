package com.unify.helloworld

actual fun getPlatformName(): String = "TV"
actual fun getPlatformNameImpl(): String = "TV"
actual fun getDeviceInfoImpl(): String = "Smart TV Platform"
