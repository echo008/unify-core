package com.unify.helloworld

actual object PlatformInfo {
    actual fun getPlatformName(): String = "Web"
    
    actual fun getDeviceInfo(): String {
        return js("navigator.userAgent").toString()
    }
}
