package com.unify.ui.responsive

import androidx.compose.runtime.Composable

@Composable
actual fun isTablet(): Boolean = false

@Composable
actual fun isDesktop(): Boolean = true

@Composable
actual fun isTV(): Boolean = false

@Composable
actual fun isWatch(): Boolean = false

@Composable
actual fun getScreenDensity(): Float = 1.0f

@Composable
actual fun getScreenOrientation(): Int = 0
