package com.unify.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unify.ui.theme.UnifyTheme

/**
 * 统一按钮组件 - 支持多种样式
 */
@Composable
fun UnifyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    variant: ButtonVariant = ButtonVariant.Primary,
    size: ButtonSize = ButtonSize.Medium,
    icon: ImageVector? = null,
    iconPosition: IconPosition = IconPosition.Start
) {
    val buttonColors = when (variant) {
        ButtonVariant.Primary -> ButtonColors(
            backgroundColor = UnifyTheme.colors.primary,
            contentColor = UnifyTheme.colors.onPrimary,
            disabledBackgroundColor = UnifyTheme.colors.primary.copy(alpha = 0.38f),
            disabledContentColor = UnifyTheme.colors.onPrimary.copy(alpha = 0.38f)
        )
        ButtonVariant.Secondary -> ButtonColors(
            backgroundColor = UnifyTheme.colors.secondary,
            contentColor = UnifyTheme.colors.onSecondary,
            disabledBackgroundColor = UnifyTheme.colors.secondary.copy(alpha = 0.38f),
            disabledContentColor = UnifyTheme.colors.onSecondary.copy(alpha = 0.38f)
        )
        ButtonVariant.Outlined -> ButtonColors(
            backgroundColor = Color.Transparent,
            contentColor = UnifyTheme.colors.primary,
            disabledBackgroundColor = Color.Transparent,
            disabledContentColor = UnifyTheme.colors.primary.copy(alpha = 0.38f),
            borderColor = UnifyTheme.colors.primary
        )
    }

    val buttonPadding = when (size) {
        ButtonSize.Small -> 8.dp
        ButtonSize.Medium -> 12.dp
        ButtonSize.Large -> 16.dp
    }

    val buttonHeight = when (size) {
        ButtonSize.Small -> 32.dp
        ButtonSize.Medium -> 48.dp
        ButtonSize.Large -> 56.dp
    }

    Surface(
        modifier = modifier
            .defaultMinSize(minHeight = buttonHeight)
            .clip(RoundedCornerShape(UnifyTheme.shapes.medium.dp)),
        color = if (enabled) buttonColors.backgroundColor else buttonColors.disabledBackgroundColor,
        shape = RoundedCornerShape(UnifyTheme.shapes.medium.dp),
        border = if (variant == ButtonVariant.Outlined) {
            BorderStroke(1.dp, buttonColors.borderColor ?: Color.Transparent)
        } else {
            null
        }
    ) {
        Box(
            modifier = Modifier
                .clickable(enabled = enabled && !isLoading, onClick = onClick)
                .padding(horizontal = buttonPadding, vertical = buttonPadding / 2),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = buttonColors.contentColor,
                    strokeWidth = 2.dp
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (icon != null && iconPosition == IconPosition.Start) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = if (enabled) buttonColors.contentColor else buttonColors.disabledContentColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Text(
                        text = text,
                        color = if (enabled) buttonColors.contentColor else buttonColors.disabledContentColor,
                        style = UnifyTheme.typography.button.copy(fontSize = when (size) {
                            ButtonSize.Small -> 12.sp
                            ButtonSize.Medium -> 14.sp
                            ButtonSize.Large -> 16.sp
                        }),
                        textAlign = TextAlign.Center
                    )

                    if (icon != null && iconPosition == IconPosition.End) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = if (enabled) buttonColors.contentColor else buttonColors.disabledContentColor
                        )
                    }
                }
            }
        }
    }
}

/**
 * 统一输入框组件
 */
@Composable
fun UnifyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "",
    enabled: Boolean = true,
    isError: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null
) {
    Column(modifier = modifier) {
        label?.let {
            Text(
                text = it,
                style = UnifyTheme.typography.body2.copy(color = UnifyTheme.colors.onSurface),
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        
        Box(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = when {
                        isError -> UnifyTheme.colors.error
                        !enabled -> UnifyTheme.colors.onSurface.copy(alpha = 0.12f)
                        else -> UnifyTheme.colors.onSurface.copy(alpha = 0.24f)
                    },
                    shape = RoundedCornerShape(UnifyTheme.shapes.small.dp)
                )
                .background(
                    color = if (enabled) UnifyTheme.colors.surface 
                           else UnifyTheme.colors.onSurface.copy(alpha = 0.04f),
                    shape = RoundedCornerShape(UnifyTheme.shapes.small.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                singleLine = singleLine,
                maxLines = maxLines,
                textStyle = UnifyTheme.typography.body1.copy(color = UnifyTheme.colors.onSurface),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                decorationBox = { innerTextField ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        leadingIcon?.let {
                            Icon(
                                imageVector = it,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = UnifyTheme.colors.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (value.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    style = UnifyTheme.typography.body1.copy(
                                        color = UnifyTheme.colors.onSurface.copy(alpha = 0.6f)
                                    )
                                )
                            }
                            innerTextField()
                        }

                        trailingIcon?.let {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = it,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = UnifyTheme.colors.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            )
        }
    }
}

/**
 * 统一卡片组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifyCard(
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    elevation: Dp = 4.dp,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(UnifyTheme.shapes.medium.dp),
        colors = CardDefaults.cardColors(
            containerColor = UnifyTheme.colors.surface,
            contentColor = UnifyTheme.colors.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        onClick = onClick ?: {}
    ) {
        content()
    }
}

/**
 * 统一加载指示器
 */
@Composable
fun UnifyLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    color: Color = UnifyTheme.colors.primary
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(size),
            color = color,
            strokeWidth = 4.dp
        )
    }
}

/**
 * 统一分隔线
 */
@Composable
fun UnifyDivider(
    modifier: Modifier = Modifier,
    color: Color = UnifyTheme.colors.onSurface.copy(alpha = 0.12f),
    thickness: Dp = 1.dp
) {
    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
            .background(color)
    )
}

/**
 * 按钮变体枚举
 */
enum class ButtonVariant {
    Primary, Secondary, Outlined
}

/**
 * 按钮尺寸枚举
 */
enum class ButtonSize {
    Small, Medium, Large
}

/**
 * 图标位置枚举
 */
enum class IconPosition {
    Start, End
}

/**
 * 按钮颜色数据类
 */
data class ButtonColors(
    val backgroundColor: Color,
    val contentColor: Color,
    val disabledBackgroundColor: Color,
    val disabledContentColor: Color,
    val borderColor: Color? = null
)