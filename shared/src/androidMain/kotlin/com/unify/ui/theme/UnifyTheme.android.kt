package com.unify.ui.theme

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Android平台主题实现
 */
@Composable
actual fun supportsDynamicColor(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
}

@Composable
actual fun dynamicLightColorScheme(): ColorScheme {
    val context = LocalContext.current
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        dynamicLightColorScheme(context)
    } else {
        androidx.compose.material3.lightColorScheme()
    }
}

@Composable
actual fun dynamicDarkColorScheme(): ColorScheme {
    val context = LocalContext.current
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        dynamicDarkColorScheme(context)
    } else {
        androidx.compose.material3.darkColorScheme()
    }
}
