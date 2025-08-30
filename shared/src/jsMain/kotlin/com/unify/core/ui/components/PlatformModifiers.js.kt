package com.unify.core.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Web 平台特定修饰符实现
 */
actual fun Modifier.platformSpecific(): Modifier = this
    .fillMaxWidth()
    .padding(horizontal = 12.dp) // Web 适中边距

actual fun Modifier.platformButtonStyle(): Modifier = this
    .fillMaxWidth()
    .height(40.dp) // Web 标准按钮高度

actual fun Modifier.platformTextFieldStyle(): Modifier = this
    .fillMaxWidth()
    .padding(horizontal = 2.dp)
