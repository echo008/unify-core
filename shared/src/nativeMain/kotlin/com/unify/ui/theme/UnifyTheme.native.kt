package com.unify.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Native平台主题实现
 */

@Composable
actual fun supportsDynamicColor(): Boolean {
    // Native平台动态颜色支持
    return false
}

@Composable
actual fun dynamicLightColorScheme(): ColorScheme {
    // Native平台动态浅色主题
    return lightColorScheme()
}

@Composable
actual fun dynamicDarkColorScheme(): ColorScheme {
    // Native平台动态深色主题
    return darkColorScheme()
}
