package com.unify.core.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * iOS 平台特定修饰符实现
 */
actual fun Modifier.platformSpecific(): Modifier = this
    .fillMaxWidth()
    .padding(horizontal = 20.dp) // iOS 设计规范边距

actual fun Modifier.platformButtonStyle(): Modifier = this
    .fillMaxWidth()
    .height(44.dp) // iOS Human Interface Guidelines 推荐高度

actual fun Modifier.platformTextFieldStyle(): Modifier = this
    .fillMaxWidth()
    .padding(horizontal = 8.dp)
