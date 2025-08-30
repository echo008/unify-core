package com.unify.core.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Android 平台特定修饰符实现
 */
actual fun Modifier.platformSpecific(): Modifier = this
    .fillMaxWidth()
    .padding(horizontal = 16.dp)

actual fun Modifier.platformButtonStyle(): Modifier = this
    .fillMaxWidth()
    .height(48.dp) // Material Design 推荐高度

actual fun Modifier.platformTextFieldStyle(): Modifier = this
    .fillMaxWidth()
    .padding(horizontal = 4.dp)
