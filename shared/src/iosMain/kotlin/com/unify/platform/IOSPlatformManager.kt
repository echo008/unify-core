package com.unify.platform

import platform.Foundation.*
import platform.UIKit.*

actual fun getPlatformType(): PlatformType = PlatformType.IOS

actual class IOSPlatformInfoImpl : PlatformInfo {
    override val platformType: PlatformType = PlatformType.IOS
    override val platformVersion: String = UIDevice.currentDevice().systemVersion()
    override val deviceModel: String = UIDevice.currentDevice().model()
    override val isDebug: Boolean = Platform.isDebugBinary
    
    override fun getScreenSize(): Pair<Int, Int> {
        val bounds = UIScreen.mainScreen().bounds()
        return Pair(bounds.size.width.toInt(), bounds.size.height.toInt())
    }
    
    override fun getDeviceId(): String? = UIDevice.currentDevice().identifierForVendor()?.UUIDString()
    
    override fun isMobile(): Boolean = true
    
    override fun isTablet(): Boolean = UIDevice.currentDevice().userInterfaceIdiom() == UIUserInterfaceIdiomPad
}

actual class IOSPlatformCapabilitiesImpl : PlatformCapabilities {
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
    
    override fun hasPermission(permission: String): Boolean {
        return when (permission) {
            "camera" -> {
                val status = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)
                status == AVAuthorizationStatusAuthorized
            }
            "location" -> {
                val status = CLLocationManager.authorizationStatus()
                status == kCLAuthorizationStatusAuthorizedWhenInUse || 
                status == kCLAuthorizationStatusAuthorizedAlways
            }
            "notifications" -> {
                // This would require checking UNUserNotificationCenter authorization
                true // Simplified for now
            }
            else -> false
        }
    }
    
    override suspend fun requestPermission(permission: String): Boolean {
        // In a real implementation, this would use proper iOS permission request APIs
        return hasPermission(permission)
    }
    
    override fun getAvailableFeatures(): List<String> {
        val features = mutableListOf<String>()
        
        // Check camera availability
        if (UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceTypeCamera)) {
            features.add("camera")
        }
        
        // Check location services
        if (CLLocationManager.locationServicesEnabled()) {
            features.add("location")
        }
        
        // Add other iOS-specific features
        features.addAll(listOf(
            "notifications",
            "biometrics",
            "vibration",
            "clipboard",
            "share",
            "deep_links",
            "webview"
        ))
        
        return features
    }
}
