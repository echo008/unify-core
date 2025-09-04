package com.unify.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Desktop平台统一按钮组件
 * 针对桌面端优化的按钮实现
 */
@Composable
actual fun UnifyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    variant: ButtonVariant,
    size: ButtonSize
) {
    val buttonColors = when (variant) {
        ButtonVariant.PRIMARY -> ButtonDefaults.buttonColors(
            containerColor = Color(0xFF2196F3),
            contentColor = Color.White,
            disabledContainerColor = Color(0xFFBDBDBD),
            disabledContentColor = Color(0xFF757575)
        )
        ButtonVariant.SECONDARY -> ButtonDefaults.outlinedButtonColors(
            contentColor = Color(0xFF2196F3),
            disabledContentColor = Color(0xFF757575)
        )
        ButtonVariant.TERTIARY -> ButtonDefaults.textButtonColors(
            contentColor = Color(0xFF2196F3),
            disabledContentColor = Color(0xFF757575)
        )
        ButtonVariant.DANGER -> ButtonDefaults.buttonColors(
            containerColor = Color(0xFFE53E3E),
            contentColor = Color.White,
            disabledContainerColor = Color(0xFFBDBDBD),
            disabledContentColor = Color(0xFF757575)
        )
    }
    
    val buttonHeight = when (size) {
        ButtonSize.SMALL -> 32.dp
        ButtonSize.MEDIUM -> 40.dp
        ButtonSize.LARGE -> 48.dp
    }
    
    val fontSize = when (size) {
        ButtonSize.SMALL -> 12.sp
        ButtonSize.MEDIUM -> 14.sp
        ButtonSize.LARGE -> 16.sp
    }
    
    val horizontalPadding = when (size) {
        ButtonSize.SMALL -> 12.dp
        ButtonSize.MEDIUM -> 16.dp
        ButtonSize.LARGE -> 20.dp
    }
    
    when (variant) {
        ButtonVariant.PRIMARY, ButtonVariant.DANGER -> {
            Button(
                onClick = onClick,
                modifier = modifier.height(buttonHeight),
                enabled = enabled,
                colors = buttonColors,
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 0.dp)
            ) {
                Text(
                    text = text,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        ButtonVariant.SECONDARY -> {
            OutlinedButton(
                onClick = onClick,
                modifier = modifier.height(buttonHeight),
                enabled = enabled,
                colors = buttonColors,
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 0.dp)
            ) {
                Text(
                    text = text,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        ButtonVariant.TERTIARY -> {
            TextButton(
                onClick = onClick,
                modifier = modifier.height(buttonHeight),
                enabled = enabled,
                colors = buttonColors,
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 0.dp)
            ) {
                Text(
                    text = text,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
