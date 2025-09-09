package com.unify.core.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Unify统一卡片组件
 * 100% Kotlin Compose语法实现
 */
enum class UnifyCardType {
    FILLED,
    ELEVATED,
    OUTLINED,
    TONAL,
}

@Composable
fun UnifyCard(
    modifier: Modifier = Modifier,
    type: UnifyCardType = UnifyCardType.FILLED,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(12.dp),
    colors: androidx.compose.material3.CardColors? = null,
    elevation: androidx.compose.material3.CardElevation? = null,
    border: BorderStroke? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    when (type) {
        UnifyCardType.FILLED -> {
            if (onClick != null) {
                Card(
                    onClick = onClick,
                    modifier = modifier,
                    enabled = enabled,
                    shape = shape,
                    colors = colors ?: CardDefaults.cardColors(),
                    elevation = elevation ?: CardDefaults.cardElevation(),
                    border = border,
                    content = content,
                )
            } else {
                Card(
                    modifier = modifier,
                    shape = shape,
                    colors = colors ?: CardDefaults.cardColors(),
                    elevation = elevation ?: CardDefaults.cardElevation(),
                    border = border,
                    content = content,
                )
            }
        }
        UnifyCardType.ELEVATED -> {
            if (onClick != null) {
                ElevatedCard(
                    onClick = onClick,
                    modifier = modifier,
                    enabled = enabled,
                    shape = shape,
                    colors = colors ?: CardDefaults.elevatedCardColors(),
                    elevation = elevation ?: CardDefaults.elevatedCardElevation(),
                    content = content,
                )
            } else {
                ElevatedCard(
                    modifier = modifier,
                    shape = shape,
                    colors = colors ?: CardDefaults.elevatedCardColors(),
                    elevation = elevation ?: CardDefaults.elevatedCardElevation(),
                    content = content,
                )
            }
        }
        UnifyCardType.OUTLINED -> {
            if (onClick != null) {
                OutlinedCard(
                    onClick = onClick,
                    modifier = modifier,
                    enabled = enabled,
                    shape = shape,
                    colors = colors ?: CardDefaults.outlinedCardColors(),
                    elevation = elevation ?: CardDefaults.outlinedCardElevation(),
                    border = border ?: CardDefaults.outlinedCardBorder(),
                    content = content,
                )
            } else {
                OutlinedCard(
                    modifier = modifier,
                    shape = shape,
                    colors = colors ?: CardDefaults.outlinedCardColors(),
                    elevation = elevation ?: CardDefaults.outlinedCardElevation(),
                    border = border ?: CardDefaults.outlinedCardBorder(),
                    content = content,
                )
            }
        }
        UnifyCardType.TONAL -> {
            if (onClick != null) {
                Card(
                    onClick = onClick,
                    modifier = modifier,
                    enabled = enabled,
                    shape = shape,
                    colors =
                        colors ?: CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        ),
                    elevation = elevation ?: CardDefaults.cardElevation(),
                    border = border,
                    content = content,
                )
            } else {
                Card(
                    modifier = modifier,
                    shape = shape,
                    colors =
                        colors ?: CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        ),
                    elevation = elevation ?: CardDefaults.cardElevation(),
                    border = border,
                    content = content,
                )
            }
        }
    }
}

/**
 * Unify统一表面组件
 */
@Composable
fun UnifySurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(0.dp),
    color: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    tonalElevation: Dp = 0.dp,
    shadowElevation: Dp = 0.dp,
    border: BorderStroke? = null,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = color,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        border = border,
        content = content,
    )
}

/**
 * Unify可点击表面组件
 */
@Composable
fun UnifyClickableSurface(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(0.dp),
    color: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    tonalElevation: Dp = 0.dp,
    shadowElevation: Dp = 0.dp,
    border: BorderStroke? = null,
    interactionSource: androidx.compose.foundation.interaction.MutableInteractionSource? = null,
    content: @Composable () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        color = color,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        border = border,
        interactionSource = interactionSource ?: androidx.compose.foundation.interaction.MutableInteractionSource(),
        content = content,
    )
}
