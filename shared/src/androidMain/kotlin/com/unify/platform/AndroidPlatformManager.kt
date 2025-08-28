package com.unify.platform

import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager

private lateinit var applicationContext: Context

fun initializeAndroidPlatform(context: Context) {
    applicationContext = context.applicationContext
}

actual fun getPlatformType(): PlatformType = PlatformType.ANDROID

actual class AndroidPlatformInfoImpl : PlatformInfo {
    override val platformType: PlatformType = PlatformType.ANDROID
    override val platformVersion: String = Build.VERSION.RELEASE
    override val deviceModel: String = "${Build.MANUFACTURER} ${Build.MODEL}"
    override val isDebug: Boolean = BuildConfig.DEBUG
    
    override fun getScreenSize(): Pair<Int, Int> {
        val windowManager = applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return Pair(displayMetrics.widthPixels, displayMetrics.heightPixels)
    }
    
    override fun getDeviceId(): String? {
        return try {
            android.provider.Settings.Secure.getString(
                applicationContext.contentResolver,
                android.provider.Settings.Secure.ANDROID_ID
            )
        } catch (e: Exception) {
            null
        }
    }
    
    override fun isMobile(): Boolean = true
    
    override fun isTablet(): Boolean {
        val configuration = applicationContext.resources.configuration
        return configuration.smallestScreenWidthDp >= 600
    }
}

actual class AndroidPlatformCapabilitiesImpl : PlatformCapabilities {
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
        return try {
            val result = androidx.core.content.ContextCompat.checkSelfPermission(
                applicationContext,
                permission
            )
            result == android.content.pm.PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun requestPermission(permission: String): Boolean {
        // This would typically require an Activity context
        // In a real implementation, you'd use ActivityResultLauncher
        return hasPermission(permission)
    }
    
    override fun getAvailableFeatures(): List<String> {
        val packageManager = applicationContext.packageManager
        return listOf(
            "android.hardware.camera",
            "android.hardware.location",
            "android.hardware.nfc",
            "android.hardware.bluetooth",
            "android.hardware.wifi",
            "android.hardware.telephony"
        ).filter { feature ->
            packageManager.hasSystemFeature(feature)
        }
    }
}
