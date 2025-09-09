package com.unify.ui.responsive

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Android平台响应式设计实现
 */
@Composable
actual fun isTablet(): Boolean {
    val context = LocalContext.current
    val configuration = context.resources.configuration
    return (configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE
}

@Composable
actual fun isDesktop(): Boolean {
    return false // Android设备不是桌面设备
}

@Composable
actual fun isTV(): Boolean {
    val context = LocalContext.current
    return context.packageManager.hasSystemFeature("android.software.leanback")
}

@Composable
actual fun isWatch(): Boolean {
    val context = LocalContext.current
    return context.packageManager.hasSystemFeature("android.hardware.type.watch")
}

@Composable
actual fun getScreenDensity(): Float {
    val context = LocalContext.current
    return context.resources.displayMetrics.density
}

@Composable
actual fun getScreenOrientation(): Int {
    val context = LocalContext.current
    val configuration = context.resources.configuration
    return configuration.orientation
}
