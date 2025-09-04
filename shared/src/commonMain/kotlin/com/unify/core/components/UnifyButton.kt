package com.unify.core.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Unify统一按钮组件
 * 100% Kotlin Compose语法实现
 */
enum class UnifyButtonType {
    FILLED,
    OUTLINED,
    TEXT,
    ELEVATED,
    TONAL
}

enum class UnifyButtonSize {
    SMALL,
    MEDIUM,
    LARGE
}

@Composable
fun UnifyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String? = null,
    icon: ImageVector? = null,
    iconPosition: UnifyIconPosition = UnifyIconPosition.START,
    type: UnifyButtonType = UnifyButtonType.FILLED,
    size: UnifyButtonSize = UnifyButtonSize.MEDIUM,
    enabled: Boolean = true,
    loading: Boolean = false,
    shape: Shape = RoundedCornerShape(8.dp),
    colors: androidx.compose.material3.ButtonColors? = null,
    border: BorderStroke? = null,
    elevation: androidx.compose.material3.ButtonElevation? = null,
    contentPadding: PaddingValues? = null,
    content: (@Composable RowScope.() -> Unit)? = null
) {
    val actualContentPadding = contentPadding ?: when (size) {
        UnifyButtonSize.SMALL -> PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        UnifyButtonSize.MEDIUM -> PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        UnifyButtonSize.LARGE -> PaddingValues(horizontal = 20.dp, vertical = 12.dp)
    }
    
    val iconSize = when (size) {
        UnifyButtonSize.SMALL -> 16.dp
        UnifyButtonSize.MEDIUM -> 20.dp
        UnifyButtonSize.LARGE -> 24.dp
    }
    
    val buttonContent: @Composable RowScope.() -> Unit = content ?: {
        if (loading) {
            UnifyLoadingIndicator(size = iconSize)
            if (text != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(text)
            }
        } else {
            when (iconPosition) {
                UnifyIconPosition.START -> {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(iconSize)
                        )
                        if (text != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                    if (text != null) {
                        Text(text)
                    }
                }
                UnifyIconPosition.END -> {
                    if (text != null) {
                        Text(text)
                        if (icon != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(iconSize)
                        )
                    }
                }
                UnifyIconPosition.ONLY -> {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(iconSize)
                        )
                    }
                }
            }
        }
    }
    
    when (type) {
        UnifyButtonType.FILLED -> {
            Button(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled && !loading,
                shape = shape,
                colors = colors ?: ButtonDefaults.buttonColors(),
                elevation = elevation,
                border = border,
                contentPadding = actualContentPadding,
                content = buttonContent
            )
        }
        UnifyButtonType.OUTLINED -> {
            OutlinedButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled && !loading,
                shape = shape,
                colors = colors ?: ButtonDefaults.outlinedButtonColors(),
                elevation = elevation,
                border = border ?: ButtonDefaults.outlinedButtonBorder(enabled && !loading),
                contentPadding = actualContentPadding,
                content = buttonContent
            )
        }
        UnifyButtonType.TEXT -> {
            TextButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled && !loading,
                shape = shape,
                colors = colors ?: ButtonDefaults.textButtonColors(),
                elevation = elevation,
                border = border,
                contentPadding = actualContentPadding,
                content = buttonContent
            )
        }
        UnifyButtonType.ELEVATED -> {
            ElevatedButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled && !loading,
                shape = shape,
                colors = colors ?: ButtonDefaults.elevatedButtonColors(),
                elevation = elevation ?: ButtonDefaults.elevatedButtonElevation(),
                border = border,
                contentPadding = actualContentPadding,
                content = buttonContent
            )
        }
        UnifyButtonType.TONAL -> {
            FilledTonalButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled && !loading,
                shape = shape,
                colors = colors ?: ButtonDefaults.filledTonalButtonColors(),
                elevation = elevation,
                border = border,
                contentPadding = actualContentPadding,
                content = buttonContent
            )
        }
    }
}

enum class UnifyIconPosition {
    START,
    END,
    ONLY
}

@Composable
fun UnifyLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 20.dp,
    color: Color = MaterialTheme.colorScheme.onPrimary
) {
    androidx.compose.material3.CircularProgressIndicator(
        modifier = modifier.size(size),
        color = color,
        strokeWidth = 2.dp
    )
}
