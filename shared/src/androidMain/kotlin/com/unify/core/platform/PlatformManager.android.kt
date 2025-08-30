package com.unify.core.platform

import android.content.Context
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager

/**
 * Android平台管理器实现
 */
actual object PlatformManager {
    
    private lateinit var context: Context
    
    actual fun initialize() {
        // Android初始化逻辑
        // 注意：Context需要在Application中设置
    }
    
    fun setContext(context: Context) {
        this.context = context
    }
    
    actual fun getPlatformName(): String = "Android"
    
    actual fun getPlatformVersion(): String = Build.VERSION.RELEASE
    
    actual fun getDeviceInfo(): String = 
        "${Build.MANUFACTURER} ${Build.MODEL} (API ${Build.VERSION.SDK_INT})"
    
    actual fun getScreenInfo(): ScreenInfo {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        
        val orientation = when (context.resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> Orientation.PORTRAIT
            Configuration.ORIENTATION_LANDSCAPE -> Orientation.LANDSCAPE
            else -> Orientation.UNKNOWN
        }
        
        return ScreenInfo(
            width = displayMetrics.widthPixels,
            height = displayMetrics.heightPixels,
            density = displayMetrics.density,
            orientation = orientation
        )
    }
    
    actual fun isTouchSupported(): Boolean = true
    
    actual fun isKeyboardSupported(): Boolean = 
        context.resources.configuration.keyboard != Configuration.KEYBOARD_NOKEYS
    
    actual fun getNetworkStatus(): NetworkStatus {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            if (capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true) {
                NetworkStatus.CONNECTED
            } else {
                NetworkStatus.DISCONNECTED
            }
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            if (networkInfo?.isConnected == true) {
                NetworkStatus.CONNECTED
            } else {
                NetworkStatus.DISCONNECTED
            }
        }
    }
}
