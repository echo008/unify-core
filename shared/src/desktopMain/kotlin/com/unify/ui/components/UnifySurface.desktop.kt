package com.unify.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp

/**
 * Desktop平台Surface组件actual实现
 */

@Composable
actual fun UnifySurface(
    modifier: Modifier,
    shape: Shape?,
    color: Color,
    contentColor: Color,
    elevation: Dp,
    border: UnifyBorder?,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = shape ?: RectangleShape,
        color = if (color != Color.Unspecified) color else MaterialTheme.colorScheme.surface,
        contentColor = if (contentColor != Color.Unspecified) contentColor else MaterialTheme.colorScheme.onSurface,
        shadowElevation = elevation,
    ) {
        content()
    }
}

@Composable
actual fun UnifyPlatformCard(
    modifier: Modifier,
    elevation: Dp,
    backgroundColor: Color,
    contentColor: Color,
    border: UnifyBorder?,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        color = if (backgroundColor != Color.Unspecified) backgroundColor else MaterialTheme.colorScheme.surface,
        contentColor = if (contentColor != Color.Unspecified) contentColor else MaterialTheme.colorScheme.onSurface,
        shadowElevation = elevation,
    ) {
        content()
    }
}

@Composable
actual fun UnifyContainer(
    modifier: Modifier,
    backgroundColor: Color,
    padding: androidx.compose.foundation.layout.PaddingValues,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        color = if (backgroundColor != Color.Unspecified) backgroundColor else Color.Transparent,
    ) {
        Box(
            modifier = Modifier.padding(padding),
        ) {
            content()
        }
    }
}
