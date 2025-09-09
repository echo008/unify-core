package com.unify.ui.responsive

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity

/**
 * Desktop平台响应式设计actual实现
 */

@Composable
actual fun isTablet(): Boolean {
    // Desktop平台不是平板设备
    return false
}

@Composable
actual fun isDesktop(): Boolean {
    // Desktop平台返回true
    return true
}

@Composable
actual fun isTV(): Boolean {
    // Desktop平台不是TV设备
    return false
}

@Composable
actual fun isWatch(): Boolean {
    // Desktop平台不是手表设备
    return false
}

@Composable
actual fun getScreenDensity(): Float {
    val density = LocalDensity.current.density
    return density
}

@Composable
actual fun getScreenOrientation(): Int {
    // Desktop平台通常是横屏，返回横屏方向
    // 0 = ORIENTATION_UNDEFINED
    // 1 = ORIENTATION_PORTRAIT
    // 2 = ORIENTATION_LANDSCAPE
    return 2 // ORIENTATION_LANDSCAPE
}
