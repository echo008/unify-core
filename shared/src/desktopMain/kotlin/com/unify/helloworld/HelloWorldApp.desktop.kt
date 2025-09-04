package com.unify.helloworld

actual fun getPlatformName(): String {
    return "Desktop (${System.getProperty("os.name")})"
}
