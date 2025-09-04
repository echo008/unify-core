package com.unify.ui.components.feedback

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.delay

/**
 * Unify跨平台Toast组件
 * 支持所有8大平台的统一消息提示体验
 */

enum class UnifyToastType {
    SUCCESS, ERROR, WARNING, INFO, DEFAULT
}

enum class UnifyToastPosition {
    TOP, CENTER, BOTTOM
}

@Composable
fun UnifyToast(
    message: String,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    type: UnifyToastType = UnifyToastType.DEFAULT,
    position: UnifyToastPosition = UnifyToastPosition.BOTTOM,
    duration: Long = 3000L,
    action: (@Composable () -> Unit)? = null
) {
    LaunchedEffect(isVisible) {
        if (isVisible && duration > 0) {
            delay(duration)
            onDismiss()
        }
    }

    if (isVisible) {
        Popup(
            alignment = when (position) {
                UnifyToastPosition.TOP -> Alignment.TopCenter
                UnifyToastPosition.CENTER -> Alignment.Center
                UnifyToastPosition.BOTTOM -> Alignment.BottomCenter
            },
            properties = PopupProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { if (position == UnifyToastPosition.TOP) -it else it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300)),
                exit = slideOutVertically(
                    targetOffsetY = { if (position == UnifyToastPosition.TOP) -it else it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            ) {
                UnifyToastContent(
                    message = message,
                    type = type,
                    action = action,
                    modifier = modifier
                )
            }
        }
    }
}

@Composable
private fun UnifyToastContent(
    message: String,
    type: UnifyToastType,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    val (backgroundColor, textColor, icon) = when (type) {
        UnifyToastType.SUCCESS -> Triple(
            Color(0xFF4CAF50),
            Color.White,
            "✓"
        )
        UnifyToastType.ERROR -> Triple(
            Color(0xFFF44336),
            Color.White,
            "✗"
        )
        UnifyToastType.WARNING -> Triple(
            Color(0xFFFF9800),
            Color.White,
            "⚠"
        )
        UnifyToastType.INFO -> Triple(
            Color(0xFF2196F3),
            Color.White,
            "ℹ"
        )
        UnifyToastType.DEFAULT -> Triple(
            MaterialTheme.colorScheme.inverseSurface,
            MaterialTheme.colorScheme.inverseOnSurface,
            null
        )
    }

    Card(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .widthIn(max = 400.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Text(
                    text = icon,
                    color = textColor,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            
            Text(
                text = message,
                color = textColor,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            
            if (action != null) {
                Spacer(modifier = Modifier.width(8.dp))
                action()
            }
        }
    }
}

@Composable
fun UnifySnackbar(
    message: String,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    duration: Long = 4000L,
    withDismissAction: Boolean = false
) {
    LaunchedEffect(isVisible) {
        if (isVisible && duration > 0) {
            delay(duration)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(300)
        ) + fadeIn(animationSpec = tween(300)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(300)
        ) + fadeOut(animationSpec = tween(300))
    ) {
        Snackbar(
            modifier = modifier,
            action = if (actionLabel != null && onActionClick != null) {
                {
                    TextButton(onClick = onActionClick) {
                        Text(actionLabel)
                    }
                }
            } else null,
            dismissAction = if (withDismissAction) {
                {
                    TextButton(onClick = onDismiss) {
                        Text("✗")
                    }
                }
            } else null
        ) {
            Text(message)
        }
    }
}

@Composable
fun UnifyFloatingToast(
    message: String,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    type: UnifyToastType = UnifyToastType.DEFAULT,
    duration: Long = 2000L
) {
    LaunchedEffect(isVisible) {
        if (isVisible && duration > 0) {
            delay(duration)
            onDismiss()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = scaleIn(
                initialScale = 0.8f,
                animationSpec = tween(200)
            ) + fadeIn(animationSpec = tween(200)),
            exit = scaleOut(
                targetScale = 0.8f,
                animationSpec = tween(200)
            ) + fadeOut(animationSpec = tween(200))
        ) {
            UnifyToastContent(
                message = message,
                type = type,
                modifier = modifier
            )
        }
    }
}

class UnifyToastState {
    private var _isVisible by mutableStateOf(false)
    private var _message by mutableStateOf("")
    private var _type by mutableStateOf(UnifyToastType.DEFAULT)
    
    val isVisible: Boolean get() = _isVisible
    val message: String get() = _message
    val type: UnifyToastType get() = _type
    
    fun show(message: String, type: UnifyToastType = UnifyToastType.DEFAULT) {
        _message = message
        _type = type
        _isVisible = true
    }
    
    fun hide() {
        _isVisible = false
    }
    
    fun showSuccess(message: String) = show(message, UnifyToastType.SUCCESS)
    fun showError(message: String) = show(message, UnifyToastType.ERROR)
    fun showWarning(message: String) = show(message, UnifyToastType.WARNING)
    fun showInfo(message: String) = show(message, UnifyToastType.INFO)
}

@Composable
fun rememberUnifyToastState(): UnifyToastState {
    return remember { UnifyToastState() }
}

@Composable
fun UnifyToastHost(
    toastState: UnifyToastState,
    modifier: Modifier = Modifier,
    position: UnifyToastPosition = UnifyToastPosition.BOTTOM,
    duration: Long = 3000L
) {
    UnifyToast(
        message = toastState.message,
        isVisible = toastState.isVisible,
        onDismiss = { toastState.hide() },
        type = toastState.type,
        position = position,
        duration = duration,
        modifier = modifier
    )
}

@Composable
fun UnifyBannerToast(
    message: String,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    type: UnifyToastType = UnifyToastType.INFO,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    duration: Long = 5000L
) {
    LaunchedEffect(isVisible) {
        if (isVisible && duration > 0) {
            delay(duration)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(300)
        ),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(300)
        )
    ) {
        val backgroundColor = when (type) {
            UnifyToastType.SUCCESS -> Color(0xFF4CAF50)
            UnifyToastType.ERROR -> Color(0xFFF44336)
            UnifyToastType.WARNING -> Color(0xFFFF9800)
            UnifyToastType.INFO -> Color(0xFF2196F3)
            UnifyToastType.DEFAULT -> MaterialTheme.colorScheme.primaryContainer
        }

        Surface(
            modifier = modifier.fillMaxWidth(),
            color = backgroundColor
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = message,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                
                if (actionText != null && onActionClick != null) {
                    TextButton(
                        onClick = onActionClick,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Text(actionText, fontWeight = FontWeight.Bold)
                    }
                }
                
                IconButton(onClick = onDismiss) {
                    Text("✗", color = Color.White)
                }
            }
        }
    }
}
