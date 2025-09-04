package com.unify.helloworld

import kotlinx.browser.window

actual fun getPlatformName(): String = "Web"

actual fun getDeviceInfo(): String {
    return buildString {
        append("Browser: ${window.navigator.userAgent}\n")
        append("Platform: ${window.navigator.platform}\n")
        append("Language: ${window.navigator.language}\n")
        append("Online: ${window.navigator.onLine}")
    }
}
