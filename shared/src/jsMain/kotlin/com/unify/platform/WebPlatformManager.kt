package com.unify.platform

import kotlinx.browser.window
import kotlinx.browser.document

actual fun getPlatformType(): PlatformType = PlatformType.WEB

actual class WebPlatformInfoImpl : PlatformInfo {
    override val platformType: PlatformType = PlatformType.WEB
    override val platformVersion: String = window.navigator.userAgent
    override val deviceModel: String = "${window.navigator.platform} - ${window.navigator.userAgent}"
    override val isDebug: Boolean = js("typeof console !== 'undefined' && console.assert") as Boolean
    
    override fun getScreenSize(): Pair<Int, Int> {
        return Pair(window.screen.width, window.screen.height)
    }
    
    override fun getDeviceId(): String? {
        // Web doesn't provide a stable device ID for privacy reasons
        // Could use fingerprinting techniques but that's not recommended
        return null
    }
    
    override fun isMobile(): Boolean {
        val userAgent = window.navigator.userAgent.lowercase()
        return userAgent.contains("mobile") || 
               userAgent.contains("android") || 
               userAgent.contains("iphone") || 
               userAgent.contains("ipad")
    }
    
    override fun isTablet(): Boolean {
        val userAgent = window.navigator.userAgent.lowercase()
        return userAgent.contains("ipad") || 
               (userAgent.contains("android") && !userAgent.contains("mobile"))
    }
}

actual class WebPlatformCapabilitiesImpl : PlatformCapabilities {
    override val supportsFileSystem: Boolean = js("'showOpenFilePicker' in window") as Boolean
    override val supportsCamera: Boolean = js("'mediaDevices' in navigator && 'getUserMedia' in navigator.mediaDevices") as Boolean
    override val supportsLocation: Boolean = js("'geolocation' in navigator") as Boolean
    override val supportsNotifications: Boolean = js("'Notification' in window") as Boolean
    override val supportsBiometrics: Boolean = js("'credentials' in navigator && 'create' in navigator.credentials") as Boolean
    override val supportsNFC: Boolean = js("'NDEFReader' in window") as Boolean
    override val supportsVibration: Boolean = js("'vibrate' in navigator") as Boolean
    override val supportsBluetooth: Boolean = js("'bluetooth' in navigator") as Boolean
    override val supportsWifi: Boolean = js("'connection' in navigator") as Boolean
    override val supportsCellular: Boolean = js("'connection' in navigator") as Boolean
    override val supportsClipboard: Boolean = js("'clipboard' in navigator") as Boolean
    override val supportsShare: Boolean = js("'share' in navigator") as Boolean
    override val supportsDeepLinks: Boolean = true // Web supports URLs
    override val supportsWebView: Boolean = true // Web is essentially a webview
    
    override fun hasPermission(permission: String): Boolean {
        return when (permission) {
            "camera", "microphone" -> {
                // Check if permission was previously granted
                js("navigator.permissions && navigator.permissions.query({name: permission}).then(result => result.state === 'granted')") as? Boolean ?: false
            }
            "location" -> {
                js("navigator.permissions && navigator.permissions.query({name: 'geolocation'}).then(result => result.state === 'granted')") as? Boolean ?: false
            }
            "notifications" -> {
                js("Notification.permission === 'granted'") as Boolean
            }
            else -> false
        }
    }
    
    override suspend fun requestPermission(permission: String): Boolean {
        return when (permission) {
            "camera" -> {
                try {
                    js("navigator.mediaDevices.getUserMedia({video: true})")
                    true
                } catch (e: Exception) {
                    false
                }
            }
            "microphone" -> {
                try {
                    js("navigator.mediaDevices.getUserMedia({audio: true})")
                    true
                } catch (e: Exception) {
                    false
                }
            }
            "location" -> {
                try {
                    js("navigator.geolocation.getCurrentPosition(() => {}, () => {})")
                    true
                } catch (e: Exception) {
                    false
                }
            }
            "notifications" -> {
                try {
                    val result = js("Notification.requestPermission()") as String
                    result == "granted"
                } catch (e: Exception) {
                    false
                }
            }
            else -> false
        }
    }
    
    override fun getAvailableFeatures(): List<String> {
        val features = mutableListOf<String>()
        
        if (supportsCamera) features.add("camera")
        if (supportsLocation) features.add("location")
        if (supportsNotifications) features.add("notifications")
        if (supportsBiometrics) features.add("biometrics")
        if (supportsNFC) features.add("nfc")
        if (supportsVibration) features.add("vibration")
        if (supportsBluetooth) features.add("bluetooth")
        if (supportsClipboard) features.add("clipboard")
        if (supportsShare) features.add("share")
        if (supportsFileSystem) features.add("file_system")
        
        return features
    }
}

actual class DesktopPlatformInfoImpl : PlatformInfo {
    override val platformType: PlatformType = PlatformType.DESKTOP
    override val platformVersion: String = window.navigator.userAgent
    override val deviceModel: String = window.navigator.platform
    override val isDebug: Boolean = js("typeof console !== 'undefined' && console.assert") as Boolean
    
    override fun getScreenSize(): Pair<Int, Int> {
        return Pair(window.screen.width, window.screen.height)
    }
    
    override fun getDeviceId(): String? = null
    
    override fun isMobile(): Boolean = false
    
    override fun isTablet(): Boolean = false
}

actual class DesktopPlatformCapabilitiesImpl : PlatformCapabilities {
    override val supportsFileSystem: Boolean = js("'showOpenFilePicker' in window") as Boolean
    override val supportsCamera: Boolean = js("'mediaDevices' in navigator && 'getUserMedia' in navigator.mediaDevices") as Boolean
    override val supportsLocation: Boolean = js("'geolocation' in navigator") as Boolean
    override val supportsNotifications: Boolean = js("'Notification' in window") as Boolean
    override val supportsBiometrics: Boolean = false // Desktop web typically doesn't support biometrics
    override val supportsNFC: Boolean = false // Desktop web typically doesn't support NFC
    override val supportsVibration: Boolean = false // Desktop doesn't support vibration
    override val supportsBluetooth: Boolean = js("'bluetooth' in navigator") as Boolean
    override val supportsWifi: Boolean = js("'connection' in navigator") as Boolean
    override val supportsCellular: Boolean = false // Desktop typically doesn't have cellular
    override val supportsClipboard: Boolean = js("'clipboard' in navigator") as Boolean
    override val supportsShare: Boolean = js("'share' in navigator") as Boolean
    override val supportsDeepLinks: Boolean = true
    override val supportsWebView: Boolean = true
    
    override fun hasPermission(permission: String): Boolean {
        return when (permission) {
            "camera", "microphone" -> {
                js("navigator.permissions && navigator.permissions.query({name: permission}).then(result => result.state === 'granted')") as? Boolean ?: false
            }
            "notifications" -> {
                js("Notification.permission === 'granted'") as Boolean
            }
            else -> false
        }
    }
    
    override suspend fun requestPermission(permission: String): Boolean {
        return when (permission) {
            "camera" -> {
                try {
                    js("navigator.mediaDevices.getUserMedia({video: true})")
                    true
                } catch (e: Exception) {
                    false
                }
            }
            "notifications" -> {
                try {
                    val result = js("Notification.requestPermission()") as String
                    result == "granted"
                } catch (e: Exception) {
                    false
                }
            }
            else -> false
        }
    }
    
    override fun getAvailableFeatures(): List<String> {
        val features = mutableListOf<String>()
        
        if (supportsCamera) features.add("camera")
        if (supportsNotifications) features.add("notifications")
        if (supportsBluetooth) features.add("bluetooth")
        if (supportsClipboard) features.add("clipboard")
        if (supportsShare) features.add("share")
        if (supportsFileSystem) features.add("file_system")
        
        return features
    }
}

// HarmonyOS and other platform implementations would be similar but platform-specific
actual class HarmonyOSPlatformInfoImpl : PlatformInfo {
    override val platformType: PlatformType = PlatformType.HARMONYOS
    override val platformVersion: String = "HarmonyOS (Web Runtime)"
    override val deviceModel: String = window.navigator.platform
    override val isDebug: Boolean = js("typeof console !== 'undefined' && console.assert") as Boolean
    
    override fun getScreenSize(): Pair<Int, Int> {
        return Pair(window.screen.width, window.screen.height)
    }
    
    override fun getDeviceId(): String? = null
    
    override fun isMobile(): Boolean {
        val userAgent = window.navigator.userAgent.lowercase()
        return userAgent.contains("harmonyos") || userAgent.contains("mobile")
    }
    
    override fun isTablet(): Boolean {
        val userAgent = window.navigator.userAgent.lowercase()
        return userAgent.contains("harmonyos") && userAgent.contains("tablet")
    }
}

actual class HarmonyOSPlatformCapabilitiesImpl : PlatformCapabilities {
    override val supportsFileSystem: Boolean = true
    override val supportsCamera: Boolean = true
    override val supportsLocation: Boolean = true
    override val supportsNotifications: Boolean = true
    override val supportsBiometrics: Boolean = true
    override val supportsNFC: Boolean = true
    override val supportsVibration: Boolean = true
    override val supportsBluetooth: Boolean = true
    override val supportsWifi: Boolean = true
    override val supportsCellular: Boolean = true
    override val supportsClipboard: Boolean = true
    override val supportsShare: Boolean = true
    override val supportsDeepLinks: Boolean = true
    override val supportsWebView: Boolean = true
    
    override fun hasPermission(permission: String): Boolean = false
    override suspend fun requestPermission(permission: String): Boolean = false
    override fun getAvailableFeatures(): List<String> = emptyList()
}
