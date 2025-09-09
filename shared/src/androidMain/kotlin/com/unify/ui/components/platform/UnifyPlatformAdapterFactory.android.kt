package com.unify.ui.components.platform

actual object UnifyPlatformAdapterFactory {
    actual fun createAdapter(): UnifyPlatformAdapter {
        return AndroidPlatformAdapter()
    }
}

class AndroidPlatformAdapter : UnifyPlatformAdapter {
    override fun getPlatformName(): String = "Android"

    override fun getPlatformVersion(): String = android.os.Build.VERSION.RELEASE

    override fun isFeatureSupported(feature: PlatformFeature): Boolean {
        return when (feature) {
            PlatformFeature.CAMERA -> true
            PlatformFeature.MICROPHONE -> true
            PlatformFeature.GPS -> true
            PlatformFeature.BLUETOOTH -> true
            PlatformFeature.NFC -> android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD_MR1
            PlatformFeature.BIOMETRIC -> android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M
            else -> false
        }
    }

    override fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            deviceId = "android_${System.currentTimeMillis()}",
            deviceName = android.os.Build.MODEL,
            manufacturer = android.os.Build.MANUFACTURER,
            model = android.os.Build.MODEL,
            brand = android.os.Build.BRAND,
            osVersion = android.os.Build.VERSION.RELEASE,
            apiLevel = android.os.Build.VERSION.SDK_INT,
            screenWidth = 1080,
            screenHeight = 1920,
            density = 3.0f,
            isTablet = false,
            isEmulator = android.os.Build.FINGERPRINT.contains("generic"),
            totalMemory = Runtime.getRuntime().totalMemory(),
            availableMemory = Runtime.getRuntime().freeMemory(),
            totalStorage = 8L * 1024L * 1024L * 1024L,
            availableStorage = 1024L * 1024L * 1024L,
        )
    }

    override fun getSystemInfo(): SystemInfo {
        return SystemInfo(
            platformType = PlatformType.ANDROID,
            architecture = android.os.Build.CPU_ABI,
            locale = java.util.Locale.getDefault().toString(),
            timezone = java.util.TimeZone.getDefault().id,
            batteryLevel = 0.75f,
            isCharging = false,
            networkType = NetworkType.WIFI,
            isOnline = true,
            isDarkMode = false,
            systemFeatures =
                listOf(
                    PlatformFeature.CAMERA,
                    PlatformFeature.MICROPHONE,
                    PlatformFeature.GPS,
                    PlatformFeature.BLUETOOTH,
                ),
        )
    }
}
