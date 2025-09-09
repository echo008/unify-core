package com.unify.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Desktop平台统一按钮组件
 * 针对桌面端优化的按钮实现
 */
@Composable
actual fun UnifyButton(
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    text: String,
    backgroundColor: Color,
    contentColor: Color,
    contentPadding: PaddingValues,
) {
    val buttonColors =
        ButtonDefaults.buttonColors(
            containerColor = if (backgroundColor != Color.Unspecified) backgroundColor else Color(0xFF2196F3),
            contentColor = if (contentColor != Color.Unspecified) contentColor else Color.White,
            disabledContainerColor = Color(0xFFBDBDBD),
            disabledContentColor = Color(0xFF757575),
        )

    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = buttonColors,
        shape = RoundedCornerShape(6.dp),
        contentPadding = contentPadding,
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
actual fun UnifyIconButton(
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    content: @Composable () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
    ) {
        content()
    }
}

@Composable
actual fun UnifyFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier,
    backgroundColor: Color,
    contentColor: Color,
    content: @Composable () -> Unit,
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = if (backgroundColor != Color.Unspecified) backgroundColor else MaterialTheme.colorScheme.primary,
        contentColor = if (contentColor != Color.Unspecified) contentColor else MaterialTheme.colorScheme.onPrimary,
    ) {
        content()
    }
}
