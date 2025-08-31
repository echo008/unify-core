package com.unify.helloworld

actual fun getPlatformName(): String = "Web"
actual fun getPlatformNameImpl(): String = "Web"
actual fun getDeviceInfoImpl(): String = "${js("navigator.userAgent")} (Kotlin/JS)"
