package com.unify.core.platform

import android.content.Context
import android.os.Build
import android.provider.Settings
import com.unify.core.types.PlatformType
import com.unify.core.types.DeviceInfo

/**
 * Android平台管理器实现
 */
class AndroidPlatformManager(private val context: Context) : BasePlatformManager() {
    
    override fun getPlatformType(): PlatformType = PlatformType.ANDROID
    
    override fun getPlatformName(): String = "Android"
    
    override fun getPlatformVersion(): String = Build.VERSION.RELEASE
    
    override suspend fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            systemName = "Android",
            systemVersion = Build.VERSION.RELEASE,
            deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID),
            isEmulator = isEmulator()
        )
    }
    
    override fun hasCapability(capability: String): Boolean {
        return when (capability) {
            "camera" -> context.packageManager.hasSystemFeature("android.hardware.camera")
            "gps" -> context.packageManager.hasSystemFeature("android.hardware.location.gps")
            "bluetooth" -> context.packageManager.hasSystemFeature("android.hardware.bluetooth")
            "wifi" -> context.packageManager.hasSystemFeature("android.hardware.wifi")
            "nfc" -> context.packageManager.hasSystemFeature("android.hardware.nfc")
            "fingerprint" -> context.packageManager.hasSystemFeature("android.hardware.fingerprint")
            "accelerometer" -> context.packageManager.hasSystemFeature("android.hardware.sensor.accelerometer")
            "gyroscope" -> context.packageManager.hasSystemFeature("android.hardware.sensor.gyroscope")
            "magnetometer" -> context.packageManager.hasSystemFeature("android.hardware.sensor.compass")
            "microphone" -> context.packageManager.hasSystemFeature("android.hardware.microphone")
            "telephony" -> context.packageManager.hasSystemFeature("android.hardware.telephony")
            "vibration" -> context.packageManager.hasSystemFeature("android.hardware.vibrator")
            else -> false
        }
    }
    
    override fun getSupportedCapabilities(): List<String> {
        val capabilities = mutableListOf<String>()
        
        if (hasCapability("camera")) capabilities.add("camera")
        if (hasCapability("gps")) capabilities.add("gps")
        if (hasCapability("bluetooth")) capabilities.add("bluetooth")
        if (hasCapability("wifi")) capabilities.add("wifi")
        if (hasCapability("nfc")) capabilities.add("nfc")
        if (hasCapability("fingerprint")) capabilities.add("fingerprint")
        if (hasCapability("accelerometer")) capabilities.add("accelerometer")
        if (hasCapability("gyroscope")) capabilities.add("gyroscope")
        if (hasCapability("magnetometer")) capabilities.add("magnetometer")
        if (hasCapability("microphone")) capabilities.add("microphone")
        if (hasCapability("telephony")) capabilities.add("telephony")
        if (hasCapability("vibration")) capabilities.add("vibration")
        
        return capabilities
    }
    
    override suspend fun performPlatformInitialization() {
        // Android特定初始化
        config["api_level"] = Build.VERSION.SDK_INT.toString()
        config["manufacturer"] = Build.MANUFACTURER
        config["model"] = Build.MODEL
        config["brand"] = Build.BRAND
        config["device"] = Build.DEVICE
        config["product"] = Build.PRODUCT
        config["hardware"] = Build.HARDWARE
        config["board"] = Build.BOARD
        config["bootloader"] = Build.BOOTLOADER
        config["fingerprint"] = Build.FINGERPRINT
        config["host"] = Build.HOST
        config["id"] = Build.ID
        config["tags"] = Build.TAGS
        config["type"] = Build.TYPE
        config["user"] = Build.USER
    }
    
    override suspend fun performPlatformCleanup() {
        // Android特定清理
        config.clear()
    }
    
    private fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk" == Build.PRODUCT)
    }
}

/**
 * 平台管理器创建函数实现
 */
actual fun getCurrentPlatformManager(): PlatformManager {
    // 在Android中需要Context，这里返回一个默认实现
    // 实际使用时应该通过依赖注入提供Context
    throw UnsupportedOperationException("Android平台管理器需要Context参数，请使用AndroidPlatformManager(context)")
}

actual fun createAndroidPlatformManager(): PlatformManager {
    throw UnsupportedOperationException("Android平台管理器需要Context参数，请使用AndroidPlatformManager(context)")
}

actual fun createIOSPlatformManager(): PlatformManager {
    throw UnsupportedOperationException("iOS平台管理器在Android平台不可用")
}

actual fun createWebPlatformManager(): PlatformManager {
    throw UnsupportedOperationException("Web平台管理器在Android平台不可用")
}

actual fun createDesktopPlatformManager(): PlatformManager {
    throw UnsupportedOperationException("Desktop平台管理器在Android平台不可用")
}

actual fun createHarmonyOSPlatformManager(): PlatformManager {
    throw UnsupportedOperationException("HarmonyOS平台管理器在Android平台不可用")
}

actual fun createMiniProgramPlatformManager(): PlatformManager {
    throw UnsupportedOperationException("MiniProgram平台管理器在Android平台不可用")
}

actual fun createWatchPlatformManager(): PlatformManager {
    throw UnsupportedOperationException("Watch平台管理器在Android平台不可用")
}

actual fun createTVPlatformManager(): PlatformManager {
    throw UnsupportedOperationException("TV平台管理器在Android平台不可用")
}
