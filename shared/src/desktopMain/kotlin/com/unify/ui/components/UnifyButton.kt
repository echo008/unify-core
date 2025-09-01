package com.unify.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.unify.ui.LocalUnifyTheme

/**
 * Desktop平台特定的按钮实现
 */
actual class UnifyPlatformButton

/**
 * Desktop平台按钮适配器
 */
@Composable
actual fun UnifyNativeButton(
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    variant: UnifyButtonVariant,
    size: UnifyButtonSize,
    content: @Composable RowScope.() -> Unit
) {
    val theme = LocalUnifyTheme.current
    val buttonColors = UnifyButtonDefaults.colors(variant, theme.colors)
    val buttonShape = UnifyButtonDefaults.shape(size)
    val buttonPadding = UnifyButtonDefaults.contentPadding(size)
    
    // Desktop平台音频反馈
    val handleClick = {
        // 使用Java Sound API播放系统音效
        // Toolkit.getDefaultToolkit().beep()
        onClick()
    }
    
    when (variant) {
        UnifyButtonVariant.Primary -> {
            Button(
                onClick = handleClick,
                modifier = modifier,
                enabled = enabled,
                shape = buttonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColors.containerColor,
                    contentColor = buttonColors.contentColor,
                    disabledContainerColor = buttonColors.disabledContainerColor,
                    disabledContentColor = buttonColors.disabledContentColor
                ),
                contentPadding = buttonPadding,
                content = content
            )
        }
        UnifyButtonVariant.Secondary -> {
            OutlinedButton(
                onClick = handleClick,
                modifier = modifier,
                enabled = enabled,
                shape = buttonShape,
                colors = OutlinedButtonDefaults.buttonColors(
                    containerColor = buttonColors.containerColor,
                    contentColor = buttonColors.contentColor,
                    disabledContainerColor = buttonColors.disabledContainerColor,
                    disabledContentColor = buttonColors.disabledContentColor
                ),
                contentPadding = buttonPadding,
                content = content
            )
        }
        UnifyButtonVariant.Text -> {
            TextButton(
                onClick = handleClick,
                modifier = modifier,
                enabled = enabled,
                shape = buttonShape,
                colors = TextButtonDefaults.textButtonColors(
                    containerColor = buttonColors.containerColor,
                    contentColor = buttonColors.contentColor,
                    disabledContainerColor = buttonColors.disabledContainerColor,
                    disabledContentColor = buttonColors.disabledContentColor
                ),
                contentPadding = buttonPadding,
                content = content
            )
        }
        UnifyButtonVariant.Elevated -> {
            ElevatedButton(
                onClick = handleClick,
                modifier = modifier,
                enabled = enabled,
                shape = buttonShape,
                colors = ElevatedButtonDefaults.elevatedButtonColors(
                    containerColor = buttonColors.containerColor,
                    contentColor = buttonColors.contentColor,
                    disabledContainerColor = buttonColors.disabledContainerColor,
                    disabledContentColor = buttonColors.disabledContentColor
                ),
                contentPadding = buttonPadding,
                content = content
            )
        }
        UnifyButtonVariant.Tonal -> {
            FilledTonalButton(
                onClick = handleClick,
                modifier = modifier,
                enabled = enabled,
                shape = buttonShape,
                colors = FilledTonalButtonDefaults.filledTonalButtonColors(
                    containerColor = buttonColors.containerColor,
                    contentColor = buttonColors.contentColor,
                    disabledContainerColor = buttonColors.disabledContainerColor,
                    disabledContentColor = buttonColors.disabledContentColor
                ),
                contentPadding = buttonPadding,
                content = content
            )
        }
    }
}
