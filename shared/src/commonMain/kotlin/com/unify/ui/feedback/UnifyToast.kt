package com.unify.ui.feedback

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.delay

/**
 * Unify Toast组件
 * 提供多种样式的消息提示
 */
@Composable
fun UnifyToast(
    message: String,
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    type: ToastType = ToastType.INFO,
    duration: ToastDuration = ToastDuration.SHORT,
    position: ToastPosition = ToastPosition.BOTTOM,
    icon: ImageVector? = null,
    action: @Composable (() -> Unit)? = null
) {
    LaunchedEffect(visible) {
        if (visible) {
            delay(duration.milliseconds)
            onDismiss()
        }
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { if (position == ToastPosition.TOP) -it else it },
            animationSpec = tween(300)
        ) + fadeIn(animationSpec = tween(300)),
        exit = slideOutVertically(
            targetOffsetY = { if (position == ToastPosition.TOP) -it else it },
            animationSpec = tween(300)
        ) + fadeOut(animationSpec = tween(300))
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            color = getToastBackgroundColor(type),
            tonalElevation = 6.dp
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 图标
                val toastIcon = icon ?: getToastIcon(type)
                Icon(
                    imageVector = toastIcon,
                    contentDescription = null,
                    tint = getToastContentColor(type),
                    modifier = Modifier.size(20.dp)
                )
                
                // 消息文本
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = getToastContentColor(type),
                    modifier = Modifier.weight(1f)
                )
                
                // 操作按钮
                action?.invoke()
            }
        }
    }
}

/**
 * 简单Toast
 */
@Composable
fun UnifySimpleToast(
    message: String,
    visible: Boolean,
    onDismiss: () -> Unit,
    type: ToastType = ToastType.INFO,
    duration: ToastDuration = ToastDuration.SHORT
) {
    UnifyToast(
        message = message,
        visible = visible,
        onDismiss = onDismiss,
        type = type,
        duration = duration
    )
}

/**
 * 带操作的Toast
 */
@Composable
fun UnifyActionToast(
    message: String,
    visible: Boolean,
    onDismiss: () -> Unit,
    actionText: String,
    onAction: () -> Unit,
    type: ToastType = ToastType.INFO,
    duration: ToastDuration = ToastDuration.LONG
) {
    UnifyToast(
        message = message,
        visible = visible,
        onDismiss = onDismiss,
        type = type,
        duration = duration,
        action = {
            TextButton(
                onClick = {
                    onAction()
                    onDismiss()
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = getToastContentColor(type)
                )
            ) {
                Text(
                    text = actionText,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    )
}

/**
 * 成功Toast
 */
@Composable
fun UnifySuccessToast(
    message: String,
    visible: Boolean,
    onDismiss: () -> Unit,
    duration: ToastDuration = ToastDuration.SHORT
) {
    UnifyToast(
        message = message,
        visible = visible,
        onDismiss = onDismiss,
        type = ToastType.SUCCESS,
        duration = duration
    )
}

/**
 * 错误Toast
 */
@Composable
fun UnifyErrorToast(
    message: String,
    visible: Boolean,
    onDismiss: () -> Unit,
    duration: ToastDuration = ToastDuration.LONG
) {
    UnifyToast(
        message = message,
        visible = visible,
        onDismiss = onDismiss,
        type = ToastType.ERROR,
        duration = duration
    )
}

/**
 * 警告Toast
 */
@Composable
fun UnifyWarningToast(
    message: String,
    visible: Boolean,
    onDismiss: () -> Unit,
    duration: ToastDuration = ToastDuration.MEDIUM
) {
    UnifyToast(
        message = message,
        visible = visible,
        onDismiss = onDismiss,
        type = ToastType.WARNING,
        duration = duration
    )
}

/**
 * 信息Toast
 */
@Composable
fun UnifyInfoToast(
    message: String,
    visible: Boolean,
    onDismiss: () -> Unit,
    duration: ToastDuration = ToastDuration.SHORT
) {
    UnifyToast(
        message = message,
        visible = visible,
        onDismiss = onDismiss,
        type = ToastType.INFO,
        duration = duration
    )
}

/**
 * 自定义Toast容器
 */
@Composable
fun UnifyToastContainer(
    modifier: Modifier = Modifier,
    position: ToastPosition = ToastPosition.BOTTOM,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = when (position) {
            ToastPosition.TOP -> Alignment.TopCenter
            ToastPosition.BOTTOM -> Alignment.BottomCenter
            ToastPosition.CENTER -> Alignment.Center
        }
    ) {
        content()
    }
}

/**
 * Toast管理器状态
 */
@Stable
class UnifyToastState {
    private var _currentToast by mutableStateOf<ToastData?>(null)
    val currentToast: ToastData? get() = _currentToast
    
    fun showToast(
        message: String,
        type: ToastType = ToastType.INFO,
        duration: ToastDuration = ToastDuration.SHORT,
        icon: ImageVector? = null,
        actionText: String? = null,
        onAction: (() -> Unit)? = null
    ) {
        _currentToast = ToastData(
            message = message,
            type = type,
            duration = duration,
            icon = icon,
            actionText = actionText,
            onAction = onAction,
            id = System.currentTimeMillis()
        )
    }
    
    fun showSuccess(message: String, duration: ToastDuration = ToastDuration.SHORT) {
        showToast(message, ToastType.SUCCESS, duration)
    }
    
    fun showError(message: String, duration: ToastDuration = ToastDuration.LONG) {
        showToast(message, ToastType.ERROR, duration)
    }
    
    fun showWarning(message: String, duration: ToastDuration = ToastDuration.MEDIUM) {
        showToast(message, ToastType.WARNING, duration)
    }
    
    fun showInfo(message: String, duration: ToastDuration = ToastDuration.SHORT) {
        showToast(message, ToastType.INFO, duration)
    }
    
    fun dismiss() {
        _currentToast = null
    }
}

/**
 * Toast管理器
 */
@Composable
fun UnifyToastManager(
    toastState: UnifyToastState,
    modifier: Modifier = Modifier,
    position: ToastPosition = ToastPosition.BOTTOM
) {
    val currentToast = toastState.currentToast
    
    UnifyToastContainer(
        modifier = modifier,
        position = position
    ) {
        currentToast?.let { toast ->
            if (toast.actionText != null && toast.onAction != null) {
                UnifyActionToast(
                    message = toast.message,
                    visible = true,
                    onDismiss = { toastState.dismiss() },
                    actionText = toast.actionText,
                    onAction = toast.onAction,
                    type = toast.type,
                    duration = toast.duration
                )
            } else {
                UnifyToast(
                    message = toast.message,
                    visible = true,
                    onDismiss = { toastState.dismiss() },
                    type = toast.type,
                    duration = toast.duration,
                    icon = toast.icon,
                    position = position
                )
            }
        }
    }
}

/**
 * 记住Toast状态
 */
@Composable
fun rememberUnifyToastState(): UnifyToastState {
    return remember { UnifyToastState() }
}

// 辅助函数

@Composable
private fun getToastBackgroundColor(type: ToastType): Color {
    return when (type) {
        ToastType.SUCCESS -> Color(0xFF4CAF50)
        ToastType.ERROR -> Color(0xFFF44336)
        ToastType.WARNING -> Color(0xFFFF9800)
        ToastType.INFO -> Color(0xFF2196F3)
    }
}

@Composable
private fun getToastContentColor(type: ToastType): Color {
    return Color.White
}

private fun getToastIcon(type: ToastType): ImageVector {
    return when (type) {
        ToastType.SUCCESS -> Icons.Default.CheckCircle
        ToastType.ERROR -> Icons.Default.Error
        ToastType.WARNING -> Icons.Default.Warning
        ToastType.INFO -> Icons.Default.Info
    }
}

// 数据类和枚举

data class ToastData(
    val message: String,
    val type: ToastType,
    val duration: ToastDuration,
    val icon: ImageVector?,
    val actionText: String?,
    val onAction: (() -> Unit)?,
    val id: Long
)

enum class ToastType {
    SUCCESS,
    ERROR,
    WARNING,
    INFO
}

enum class ToastDuration(val milliseconds: Long) {
    SHORT(2000L),
    MEDIUM(3500L),
    LONG(5000L)
}

enum class ToastPosition {
    TOP,
    BOTTOM,
    CENTER
}
