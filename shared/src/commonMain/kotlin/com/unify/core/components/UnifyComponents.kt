package com.unify.core.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Unify反馈类型枚举
 */
enum class UnifyFeedbackType {
    SUCCESS,
    ERROR,
    WARNING,
    INFO,
    LOADING,
}

/**
 * Unify间距枚举
 */
enum class UnifySpacing(val value: Dp) {
    SMALL(4.dp),
    MEDIUM(12.dp),
    LARGE(20.dp),
    EXTRA_LARGE(28.dp),
}

// UnifyCard moved to UnifySurface.kt to use expect/actual mechanism and avoid duplicate declarations

// Layout components moved to UnifyLayout.kt to avoid duplicate declarations

/**
 * Unify统一反馈横幅组件
 */
@Composable
fun UnifyFeedbackBanner(
    message: String,
    type: UnifyFeedbackType,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    val backgroundColor =
        when (type) {
            UnifyFeedbackType.SUCCESS -> Color(0xFF4CAF50)
            UnifyFeedbackType.ERROR -> Color(0xFFF44336)
            UnifyFeedbackType.WARNING -> Color(0xFFFF9800)
            UnifyFeedbackType.INFO -> Color(0xFF2196F3)
            UnifyFeedbackType.LOADING -> Color(0xFF9C27B0)
        }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = backgroundColor,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(
                text = message,
                color = backgroundColor,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

/**
 * Unify统一进度指示器组件
 */
@Composable
fun UnifyProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    isLinear: Boolean = true,
) {
    if (isLinear) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = modifier,
            color = MaterialTheme.colorScheme.primary,
        )
    } else {
        CircularProgressIndicator(
            progress = { progress },
            modifier = modifier,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

// UnifySection moved to UnifyLayout.kt to avoid duplicate declarations
