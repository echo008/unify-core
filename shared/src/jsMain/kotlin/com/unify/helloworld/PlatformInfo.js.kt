package com.unify.helloworld

import kotlinx.browser.window

actual class PlatformInfo {
    actual companion object {
        actual fun getPlatformName(): String = "Web"
        
        actual fun getDeviceInfo(): String = window.navigator.userAgent
    }
}
