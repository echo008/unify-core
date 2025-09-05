package com.unify.core.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.foundation.shape.RoundedCornerShape

/**
 * 跨平台Modifier扩展函数expect声明
 */
expect fun Modifier.platformClickable(enabled: Boolean = true, onClick: () -> Unit): Modifier

expect fun Modifier.platformLongClickable(enabled: Boolean = true, onLongClick: () -> Unit): Modifier

expect fun Modifier.platformShadow(elevation: Dp, shape: Shape = RoundedCornerShape(0.dp), clip: Boolean = false): Modifier

expect fun Modifier.platformRoundedCorners(radius: Dp): Modifier

expect fun Modifier.platformBorder(width: Dp, color: Color, shape: Shape = RoundedCornerShape(0.dp)): Modifier

expect fun Modifier.platformPadding(horizontal: Dp = 0.dp, vertical: Dp = 0.dp): Modifier

expect fun Modifier.platformSize(width: Dp, height: Dp): Modifier

expect fun Modifier.platformTouchFeedback(enabled: Boolean = true): Modifier

expect fun Modifier.platformAccessibility(contentDescription: String? = null, role: String? = null): Modifier

expect fun Modifier.platformScrollable(enabled: Boolean = true): Modifier

expect fun Modifier.platformAnimated(enabled: Boolean = true): Modifier
