package com.unify.core.platform

import platform.UIKit.UIDevice
import platform.UIKit.UIScreen
import platform.Foundation.NSBundle
import platform.SystemConfiguration.SCNetworkReachabilityCreateWithName
import platform.SystemConfiguration.SCNetworkReachabilityGetFlags
import platform.SystemConfiguration.kSCNetworkReachabilityFlagsReachable
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr

/**
 * iOS平台管理器实现
 */
actual object PlatformManager {
    
    actual fun initialize() {
        // iOS初始化逻辑
    }
    
    actual fun getPlatformName(): String = "iOS"
    
    actual fun getPlatformVersion(): String {
        val device = UIDevice.currentDevice
        return device.systemVersion
    }
    
    actual fun getDeviceInfo(): String {
        val device = UIDevice.currentDevice
        return "${device.model} ${device.systemName} ${device.systemVersion}"
    }
    
    actual fun getScreenInfo(): ScreenInfo {
        val screen = UIScreen.mainScreen
        val bounds = screen.bounds
        val scale = screen.scale
        
        // iOS中通过bounds判断方向
        val orientation = if (bounds.useContents { width > height }) {
            Orientation.LANDSCAPE
        } else {
            Orientation.PORTRAIT
        }
        
        return ScreenInfo(
            width = (bounds.useContents { width } * scale).toInt(),
            height = (bounds.useContents { height } * scale).toInt(),
            density = scale.toFloat(),
            orientation = orientation
        )
    }
    
    actual fun isTouchSupported(): Boolean = true
    
    actual fun isKeyboardSupported(): Boolean = false // iOS设备通常没有物理键盘
    
    actual fun getNetworkStatus(): NetworkStatus {
        return try {
            memScoped {
                val reachability = SCNetworkReachabilityCreateWithName(null, "www.google.com")
                if (reachability != null) {
                    val flags = alloc<platform.SystemConfiguration.SCNetworkReachabilityFlagsVar>()
                    val success = SCNetworkReachabilityGetFlags(reachability, flags.ptr)
                    
                    if (success && (flags.value and kSCNetworkReachabilityFlagsReachable) != 0u) {
                        NetworkStatus.CONNECTED
                    } else {
                        NetworkStatus.DISCONNECTED
                    }
                } else {
                    NetworkStatus.UNKNOWN
                }
            }
        } catch (e: Exception) {
            NetworkStatus.UNKNOWN
        }
    }
}
