package com.unify.helloworld

import android.content.Context
import android.os.Build
import android.provider.Settings

/**
 * Android平台的SimplePlatform实现
 */
class AndroidSimplePlatform(private val context: Context) {
    fun getPlatformSpecificInfo(): Map<String, String> {
        return mapOf(
            "platform" to "Android",
            "version" to Build.VERSION.RELEASE,
            "apiLevel" to Build.VERSION.SDK_INT.toString(),
            "manufacturer" to Build.MANUFACTURER,
            "model" to Build.MODEL,
            "brand" to Build.BRAND,
            "product" to Build.PRODUCT,
            "hardware" to Build.HARDWARE,
            "board" to Build.BOARD,
            "bootloader" to Build.BOOTLOADER,
            "fingerprint" to Build.FINGERPRINT,
            "host" to Build.HOST,
            "id" to Build.ID,
            "tags" to Build.TAGS,
            "type" to Build.TYPE,
            "user" to Build.USER,
            "deviceId" to getDeviceId(),
            "supportedAbis" to Build.SUPPORTED_ABIS.joinToString(","),
        )
    }

    private fun getDeviceId(): String {
        return try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        } catch (e: Exception) {
            "unknown"
        }
    }

    fun isTablet(): Boolean {
        val configuration = context.resources.configuration
        return (configuration.screenLayout and android.content.res.Configuration.SCREENLAYOUT_SIZE_MASK) >=
            android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE
    }

    fun getScreenDensity(): String {
        val density = context.resources.displayMetrics.density
        return when {
            density <= 0.75f -> "ldpi"
            density <= 1.0f -> "mdpi"
            density <= 1.5f -> "hdpi"
            density <= 2.0f -> "xhdpi"
            density <= 3.0f -> "xxhdpi"
            else -> "xxxhdpi"
        }
    }
}
