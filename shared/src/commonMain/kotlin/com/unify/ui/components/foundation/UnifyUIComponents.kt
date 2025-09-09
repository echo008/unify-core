package com.unify.ui.components.foundation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

// UnifyCard moved to UnifyCard.kt to avoid duplicate declarations

/**
 * Unify统一文本组件
 */
@Composable
fun UnifyText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        style = style,
    )
}

/**
 * Unify统一图标组件 - 基础版本
 */
@Composable
fun UnifyBasicIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
) {
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint,
    )
}

/**
 * Unify统一按钮组件
 */
@Composable
fun UnifyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        shape = RoundedCornerShape(8.dp),
        content = content,
    )
}

/**
 * Unify统一轮廓按钮组件
 */
@Composable
fun UnifyOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        content = content,
    )
}

/**
 * Unify统一表面组件
 */
@Composable
fun UnifySurface(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surface,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        color = color,
        shape = RoundedCornerShape(8.dp),
        content = content,
    )
}

/**
 * Unify统一图片组件占位符
 */
@Composable
fun UnifyImage(
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    // 这是一个占位符实现
    Box(
        modifier =
            modifier
                .size(48.dp)
                .padding(8.dp),
        contentAlignment = Alignment.Center,
    ) {
        UnifyBasicIcon(
            imageVector = Icons.Default.Check,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}
