package com.unify.core.platform

import kotlinx.browser.window
import org.w3c.dom.Navigator

/**
 * Web平台管理器实现
 */
actual object PlatformManager {
    
    actual fun initialize() {
        // Web初始化逻辑
    }
    
    actual fun getPlatformName(): String = "Web"
    
    actual fun getPlatformVersion(): String = window.navigator.appVersion
    
    actual fun getDeviceInfo(): String = window.navigator.userAgent
    
    actual fun getScreenInfo(): ScreenInfo {
        val screen = window.screen
        val orientation = if (screen.width > screen.height) {
            Orientation.LANDSCAPE
        } else {
            Orientation.PORTRAIT
        }
        
        return ScreenInfo(
            width = screen.width,
            height = screen.height,
            density = window.devicePixelRatio.toFloat(),
            orientation = orientation
        )
    }
    
    actual fun isTouchSupported(): Boolean = 
        js("'ontouchstart' in window || navigator.maxTouchPoints > 0") as Boolean
    
    actual fun isKeyboardSupported(): Boolean = true
    
    actual fun getNetworkStatus(): NetworkStatus {
        return if (js("navigator.onLine") as Boolean) {
            NetworkStatus.CONNECTED
        } else {
            NetworkStatus.DISCONNECTED
        }
    }
}
