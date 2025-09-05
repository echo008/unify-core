package com.unify.ui.components

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Unify Toast组件
 * 提供多种样式的提示消息和通知
 */

/**
 * Toast类型
 */
enum class ToastType {
    Success, Error, Warning, Info
}

/**
 * Toast位置
 */
enum class ToastPosition {
    Top, Bottom, Center
}

/**
 * Toast数据类
 */
data class ToastData(
    val message: String,
    val type: ToastType = ToastType.Info,
    val duration: Long = 3000L,
    val action: ToastAction? = null
)

data class ToastAction(
    val text: String,
    val onClick: () -> Unit
)

/**
 * Toast状态管理
 */
class ToastState {
    private val _toasts = mutableStateListOf<ToastData>()
    val toasts: List<ToastData> = _toasts
    
    fun showToast(
        message: String,
        type: ToastType = ToastType.Info,
        duration: Long = 3000L,
        action: ToastAction? = null
    ) {
        val toast = ToastData(message, type, duration, action)
        _toasts.add(toast)
        
        // 自动移除Toast
        if (duration > 0) {
            // 需要在Composable中使用LaunchedEffect来处理协程
            // 这里暂时注释掉，在UI组件中处理自动移除
        }
    }
    
    fun dismissToast(toast: ToastData) {
        _toasts.remove(toast)
    }
    
    fun clearAll() {
        _toasts.clear()
    }
}

/**
 * 记住Toast状态
 */
@Composable
fun rememberToastState(): ToastState {
    return remember { ToastState() }
}

/**
 * Toast容器
 */
@Composable
fun UnifyToastContainer(
    toastState: ToastState,
    modifier: Modifier = Modifier,
    position: ToastPosition = ToastPosition.Bottom
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = when (position) {
            ToastPosition.Top -> Alignment.TopCenter
            ToastPosition.Bottom -> Alignment.BottomCenter
            ToastPosition.Center -> Alignment.Center
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            toastState.toasts.forEach { toast ->
                key(toast) {
                    UnifyToast(
                        toast = toast,
                        onDismiss = { toastState.dismissToast(toast) }
                    )
                }
            }
        }
    }
}

/**
 * 基础Toast组件
 */
@Composable
fun UnifyToast(
    toast: ToastData,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(toast) {
        visible = true
        if (toast.duration > 0) {
            delay(toast.duration)
            visible = false
            delay(300) // 等待动画完成
            onDismiss()
        }
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(300)
        ) + fadeIn(animationSpec = tween(300)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(300)
        ) + fadeOut(animationSpec = tween(300)),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = getToastBackgroundColor(toast.type)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 图标
                Text(
                    text = getToastIcon(toast.type),
                    fontSize = 20.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )
                
                // 消息
                Text(
                    text = toast.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = getToastTextColor(toast.type),
                    modifier = Modifier.weight(1f)
                )
                
                // 操作按钮
                toast.action?.let { action ->
                    TextButton(
                        onClick = {
                            action.onClick()
                            onDismiss()
                        }
                    ) {
                        Text(
                            text = action.text,
                            color = getToastTextColor(toast.type)
                        )
                    }
                }
                
                // 关闭按钮
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Text(
                        text = "×",
                        fontSize = 16.sp,
                        color = getToastTextColor(toast.type)
                    )
                }
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
    type: ToastType = ToastType.Info,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = modifier
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = getToastBackgroundColor(type)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = getToastIcon(type),
                    fontSize = 16.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = getToastTextColor(type)
                )
            }
        }
    }
}

/**
 * 浮动Toast
 */
@Composable
fun UnifyFloatingToast(
    message: String,
    type: ToastType = ToastType.Info,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "floating")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetY"
    )
    
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier.offset(y = offsetY.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = getToastBackgroundColor(type)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = getToastIcon(type),
                    fontSize = 18.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = getToastTextColor(type)
                )
            }
        }
    }
}

/**
 * 进度Toast
 */
@Composable
fun UnifyProgressToast(
    message: String,
    progress: Float,
    isVisible: Boolean,
    onCancel: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    
                    onCancel?.let {
                        IconButton(
                            onClick = it,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Text("×", fontSize = 16.sp)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 自定义Toast
 */
@Composable
fun UnifyCustomToast(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable ColumnScope.() -> Unit
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                content = content
            )
        }
    }
}

/**
 * 批量Toast
 */
@Composable
fun UnifyBatchToast(
    messages: List<String>,
    type: ToastType = ToastType.Info,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = getToastBackgroundColor(type)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = getToastIcon(type),
                            fontSize = 18.sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "${messages.size} 条消息",
                            style = MaterialTheme.typography.titleSmall,
                            color = getToastTextColor(type),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Text(
                            text = "×",
                            fontSize = 16.sp,
                            color = getToastTextColor(type)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                messages.take(3).forEach { message ->
                    Text(
                        text = "• $message",
                        style = MaterialTheme.typography.bodySmall,
                        color = getToastTextColor(type),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
                
                if (messages.size > 3) {
                    Text(
                        text = "还有 ${messages.size - 3} 条消息...",
                        style = MaterialTheme.typography.bodySmall,
                        color = getToastTextColor(type).copy(alpha = 0.7f),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

// 辅助函数
@Composable
private fun getToastBackgroundColor(type: ToastType): Color {
    return when (type) {
        ToastType.Success -> Color(0xFF4CAF50)
        ToastType.Error -> Color(0xFFF44336)
        ToastType.Warning -> Color(0xFFFF9800)
        ToastType.Info -> MaterialTheme.colorScheme.primary
    }
}

@Composable
private fun getToastTextColor(type: ToastType): Color {
    return Color.White
}

private fun getToastIcon(type: ToastType): String {
    return when (type) {
        ToastType.Success -> "✅"
        ToastType.Error -> "❌"
        ToastType.Warning -> "⚠️"
        ToastType.Info -> "ℹ️"
    }
}

// 扩展函数
fun ToastState.showSuccess(message: String, duration: Long = 3000L) {
    showToast(message, ToastType.Success, duration)
}

fun ToastState.showError(message: String, duration: Long = 5000L) {
    showToast(message, ToastType.Error, duration)
}

fun ToastState.showWarning(message: String, duration: Long = 4000L) {
    showToast(message, ToastType.Warning, duration)
}

fun ToastState.showInfo(message: String, duration: Long = 3000L) {
    showToast(message, ToastType.Info, duration)
}
