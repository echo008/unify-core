package com.unify.helloworld

actual fun getPlatformName(): String = "Desktop"
actual fun getPlatformNameImpl(): String = "Desktop"
actual fun getDeviceInfoImpl(): String = "${System.getProperty("os.name")} ${System.getProperty("os.version")} (JVM ${System.getProperty("java.version")})"
