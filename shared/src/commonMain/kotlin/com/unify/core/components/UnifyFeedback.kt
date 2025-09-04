package com.unify.core.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Unify统一反馈组件
 * 100% Kotlin Compose语法实现
 */
enum class UnifyFeedbackType {
    SUCCESS,
    ERROR,
    WARNING,
    INFO,
    LOADING
}

@Composable
fun UnifyProgressIndicator(
    modifier: Modifier = Modifier,
    progress: Float? = null,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    strokeWidth: androidx.compose.ui.unit.Dp = 4.dp,
    isLinear: Boolean = false
) {
    if (isLinear) {
        if (progress != null) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = modifier,
                color = color,
                trackColor = trackColor,
                strokeCap = androidx.compose.foundation.StrokeCap.Round
            )
        } else {
            LinearProgressIndicator(
                modifier = modifier,
                color = color,
                trackColor = trackColor,
                strokeCap = androidx.compose.foundation.StrokeCap.Round
            )
        }
    } else {
        if (progress != null) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = modifier,
                color = color,
                trackColor = trackColor,
                strokeWidth = strokeWidth,
                strokeCap = androidx.compose.foundation.StrokeCap.Round
            )
        } else {
            CircularProgressIndicator(
                modifier = modifier,
                color = color,
                strokeWidth = strokeWidth,
                strokeCap = androidx.compose.foundation.StrokeCap.Round
            )
        }
    }
}

@Composable
fun UnifyLoadingState(
    message: String = "加载中...",
    modifier: Modifier = Modifier,
    showProgress: Boolean = true,
    progress: Float? = null
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (showProgress) {
                UnifyProgressIndicator(
                    progress = progress,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.size(16.dp))
            }
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun UnifyErrorState(
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    actionButton: @Composable (() -> Unit)? = null
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.size(16.dp))
            }
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            
            if (actionButton != null) {
                Spacer(modifier = Modifier.size(16.dp))
                actionButton()
            }
        }
    }
}

@Composable
fun UnifyFeedbackBanner(
    message: String,
    type: UnifyFeedbackType,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    onDismiss: (() -> Unit)? = null,
    action: @Composable (() -> Unit)? = null
) {
    val backgroundColor = when (type) {
        UnifyFeedbackType.SUCCESS -> MaterialTheme.colorScheme.primaryContainer
        UnifyFeedbackType.ERROR -> MaterialTheme.colorScheme.errorContainer
        UnifyFeedbackType.WARNING -> MaterialTheme.colorScheme.tertiaryContainer
        UnifyFeedbackType.INFO -> MaterialTheme.colorScheme.secondaryContainer
        UnifyFeedbackType.LOADING -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val contentColor = when (type) {
        UnifyFeedbackType.SUCCESS -> MaterialTheme.colorScheme.onPrimaryContainer
        UnifyFeedbackType.ERROR -> MaterialTheme.colorScheme.onErrorContainer
        UnifyFeedbackType.WARNING -> MaterialTheme.colorScheme.onTertiaryContainer
        UnifyFeedbackType.INFO -> MaterialTheme.colorScheme.onSecondaryContainer
        UnifyFeedbackType.LOADING -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    UnifySurface(
        modifier = modifier.fillMaxWidth(),
        color = backgroundColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = contentColor
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
                modifier = Modifier.weight(1f)
            )
            
            if (action != null) {
                Spacer(modifier = Modifier.width(12.dp))
                action()
            }
            
            if (onDismiss != null) {
                Spacer(modifier = Modifier.width(8.dp))
                UnifyButton(
                    onClick = onDismiss,
                    text = "×",
                    type = UnifyButtonType.TEXT,
                    size = UnifyButtonSize.SMALL
                )
            }
        }
    }
}

@Composable
fun UnifySnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier
    )
}

@Composable
fun UnifyStatusIndicator(
    status: UnifyFeedbackType,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 12.dp
) {
    val color = when (status) {
        UnifyFeedbackType.SUCCESS -> MaterialTheme.colorScheme.primary
        UnifyFeedbackType.ERROR -> MaterialTheme.colorScheme.error
        UnifyFeedbackType.WARNING -> MaterialTheme.colorScheme.tertiary
        UnifyFeedbackType.INFO -> MaterialTheme.colorScheme.secondary
        UnifyFeedbackType.LOADING -> MaterialTheme.colorScheme.outline
    }
    
    UnifySurface(
        modifier = modifier.size(size),
        color = color,
        shape = androidx.compose.foundation.shape.CircleShape
    ) {}
}
