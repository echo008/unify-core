package com.unify.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

@Composable
actual fun supportsDynamicColor(): Boolean = false

@Composable
actual fun dynamicLightColorScheme(): ColorScheme = lightColorScheme()

@Composable
actual fun dynamicDarkColorScheme(): ColorScheme = darkColorScheme()
