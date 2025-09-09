package com.unify.ui.responsive

import androidx.compose.runtime.Composable

/**
 * Native平台响应式设计实现
 */

@Composable
actual fun isTablet(): Boolean {
    // Native平台平板检测
    return false
}

@Composable
actual fun isDesktop(): Boolean {
    // Native平台桌面检测
    return true
}

@Composable
actual fun isTV(): Boolean {
    // Native平台TV检测
    return false
}

@Composable
actual fun isWatch(): Boolean {
    // Native平台手表检测
    return false
}

@Composable
actual fun getScreenDensity(): Float {
    // Native平台屏幕密度
    return 1.0f
}

@Composable
actual fun getScreenOrientation(): Int {
    // Native平台屏幕方向 (0=portrait, 1=landscape)
    return 1
}
