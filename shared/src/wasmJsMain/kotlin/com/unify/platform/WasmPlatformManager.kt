package com.unify.platform

import kotlinx.browser.window

actual fun getPlatformType(): PlatformType = PlatformType.WEB

actual class WebPlatformInfoImpl : PlatformInfo {
    override val platformType: PlatformType = PlatformType.WEB
    override val platformVersion: String = "WASM ${window.navigator.userAgent}"
    override val deviceModel: String = "WebAssembly Runtime"
    override val isDebug: Boolean = js("typeof console !== 'undefined'") as Boolean
    
    override fun getScreenSize(): Pair<Int, Int> {
        return Pair(window.screen.width, window.screen.height)
    }
    
    override fun getDeviceId(): String? = null
    
    override fun isMobile(): Boolean {
        val userAgent = window.navigator.userAgent.lowercase()
        return userAgent.contains("mobile")
    }
    
    override fun isTablet(): Boolean {
        val userAgent = window.navigator.userAgent.lowercase()
        return userAgent.contains("tablet")
    }
}

actual class WebPlatformCapabilitiesImpl : PlatformCapabilities {
    override val supportsFileSystem: Boolean = false // Limited in WASM
    override val supportsCamera: Boolean = js("'mediaDevices' in navigator") as Boolean
    override val supportsLocation: Boolean = js("'geolocation' in navigator") as Boolean
    override val supportsNotifications: Boolean = js("'Notification' in window") as Boolean
    override val supportsBiometrics: Boolean = false
    override val supportsNFC: Boolean = false
    override val supportsVibration: Boolean = js("'vibrate' in navigator") as Boolean
    override val supportsBluetooth: Boolean = false
    override val supportsWifi: Boolean = js("'connection' in navigator") as Boolean
    override val supportsCellular: Boolean = false
    override val supportsClipboard: Boolean = js("'clipboard' in navigator") as Boolean
    override val supportsShare: Boolean = js("'share' in navigator") as Boolean
    override val supportsDeepLinks: Boolean = true
    override val supportsWebView: Boolean = true
    
    override fun hasPermission(permission: String): Boolean = false
    override suspend fun requestPermission(permission: String): Boolean = false
    override fun getAvailableFeatures(): List<String> = listOf("webview", "deep_links")
}

// Placeholder implementations for other platforms
actual class DesktopPlatformInfoImpl : PlatformInfo {
    override val platformType: PlatformType = PlatformType.DESKTOP
    override val platformVersion: String = "WASM Desktop"
    override val deviceModel: String = "WebAssembly Desktop"
    override val isDebug: Boolean = true
    
    override fun getScreenSize(): Pair<Int, Int> = Pair(1920, 1080)
    override fun getDeviceId(): String? = null
    override fun isMobile(): Boolean = false
    override fun isTablet(): Boolean = false
}

actual class DesktopPlatformCapabilitiesImpl : PlatformCapabilities {
    override val supportsFileSystem: Boolean = false
    override val supportsCamera: Boolean = false
    override val supportsLocation: Boolean = false
    override val supportsNotifications: Boolean = false
    override val supportsBiometrics: Boolean = false
    override val supportsNFC: Boolean = false
    override val supportsVibration: Boolean = false
    override val supportsBluetooth: Boolean = false
    override val supportsWifi: Boolean = false
    override val supportsCellular: Boolean = false
    override val supportsClipboard: Boolean = false
    override val supportsShare: Boolean = false
    override val supportsDeepLinks: Boolean = true
    override val supportsWebView: Boolean = true
    
    override fun hasPermission(permission: String): Boolean = false
    override suspend fun requestPermission(permission: String): Boolean = false
    override fun getAvailableFeatures(): List<String> = emptyList()
}

actual class AndroidPlatformInfoImpl : PlatformInfo {
    override val platformType: PlatformType = PlatformType.ANDROID
    override val platformVersion: String = "Android (WASM)"
    override val deviceModel: String = "WASM Android"
    override val isDebug: Boolean = true
    
    override fun getScreenSize(): Pair<Int, Int> = Pair(1080, 1920)
    override fun getDeviceId(): String? = null
    override fun isMobile(): Boolean = true
    override fun isTablet(): Boolean = false
}

actual class AndroidPlatformCapabilitiesImpl : PlatformCapabilities {
    override val supportsFileSystem: Boolean = false
    override val supportsCamera: Boolean = false
    override val supportsLocation: Boolean = false
    override val supportsNotifications: Boolean = false
    override val supportsBiometrics: Boolean = false
    override val supportsNFC: Boolean = false
    override val supportsVibration: Boolean = false
    override val supportsBluetooth: Boolean = false
    override val supportsWifi: Boolean = false
    override val supportsCellular: Boolean = false
    override val supportsClipboard: Boolean = false
    override val supportsShare: Boolean = false
    override val supportsDeepLinks: Boolean = false
    override val supportsWebView: Boolean = false
    
    override fun hasPermission(permission: String): Boolean = false
    override suspend fun requestPermission(permission: String): Boolean = false
    override fun getAvailableFeatures(): List<String> = emptyList()
}

actual class IOSPlatformInfoImpl : PlatformInfo {
    override val platformType: PlatformType = PlatformType.IOS
    override val platformVersion: String = "iOS (WASM)"
    override val deviceModel: String = "WASM iOS"
    override val isDebug: Boolean = true
    
    override fun getScreenSize(): Pair<Int, Int> = Pair(375, 812)
    override fun getDeviceId(): String? = null
    override fun isMobile(): Boolean = true
    override fun isTablet(): Boolean = false
}

actual class IOSPlatformCapabilitiesImpl : PlatformCapabilities {
    override val supportsFileSystem: Boolean = false
    override val supportsCamera: Boolean = false
    override val supportsLocation: Boolean = false
    override val supportsNotifications: Boolean = false
    override val supportsBiometrics: Boolean = false
    override val supportsNFC: Boolean = false
    override val supportsVibration: Boolean = false
    override val supportsBluetooth: Boolean = false
    override val supportsWifi: Boolean = false
    override val supportsCellular: Boolean = false
    override val supportsClipboard: Boolean = false
    override val supportsShare: Boolean = false
    override val supportsDeepLinks: Boolean = false
    override val supportsWebView: Boolean = false
    
    override fun hasPermission(permission: String): Boolean = false
    override suspend fun requestPermission(permission: String): Boolean = false
    override fun getAvailableFeatures(): List<String> = emptyList()
}

actual class HarmonyOSPlatformInfoImpl : PlatformInfo {
    override val platformType: PlatformType = PlatformType.HARMONYOS
    override val platformVersion: String = "HarmonyOS (WASM)"
    override val deviceModel: String = "WASM HarmonyOS"
    override val isDebug: Boolean = true
    
    override fun getScreenSize(): Pair<Int, Int> = Pair(1080, 2340)
    override fun getDeviceId(): String? = null
    override fun isMobile(): Boolean = true
    override fun isTablet(): Boolean = false
}

actual class HarmonyOSPlatformCapabilitiesImpl : PlatformCapabilities {
    override val supportsFileSystem: Boolean = false
    override val supportsCamera: Boolean = false
    override val supportsLocation: Boolean = false
    override val supportsNotifications: Boolean = false
    override val supportsBiometrics: Boolean = false
    override val supportsNFC: Boolean = false
    override val supportsVibration: Boolean = false
    override val supportsBluetooth: Boolean = false
    override val supportsWifi: Boolean = false
    override val supportsCellular: Boolean = false
    override val supportsClipboard: Boolean = false
    override val supportsShare: Boolean = false
    override val supportsDeepLinks: Boolean = false
    override val supportsWebView: Boolean = false
    
    override fun hasPermission(permission: String): Boolean = false
    override suspend fun requestPermission(permission: String): Boolean = false
    override fun getAvailableFeatures(): List<String> = emptyList()
}
