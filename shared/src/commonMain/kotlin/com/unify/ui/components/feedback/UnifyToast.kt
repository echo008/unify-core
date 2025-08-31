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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.unify.ui.LocalUnifyTheme
import com.unify.ui.LocalUnifyPlatformTheme
import com.unify.ui.components.foundation.*
import kotlinx.coroutines.delay

/**
 * Unify Toast 组件
 * 支持多平台适配的统一提示消息组件，参考 KuiklyUI 设计规范
 */

/**
 * Toast 变体枚举
 */
enum class UnifyToastVariant {
    DEFAULT,        // 默认提示
    SUCCESS,        // 成功提示
    WARNING,        // 警告提示
    ERROR,          // 错误提示
    INFO            // 信息提示
}

/**
 * Toast 位置枚举
 */
enum class UnifyToastPosition {
    TOP,            // 顶部
    CENTER,         // 中心
    BOTTOM          // 底部
}

/**
 * Toast 尺寸枚举
 */
enum class UnifyToastSize {
    COMPACT,        // 紧凑尺寸
    STANDARD,       // 标准尺寸
    LARGE           // 大尺寸
}

/**
 * Toast 数据类
 */
data class UnifyToastData(
    val message: String,
    val variant: UnifyToastVariant = UnifyToastVariant.DEFAULT,
    val duration: Long = 3000L,
    val icon: ImageVector? = null,
    val action: UnifyToastAction? = null,
    val dismissible: Boolean = true,
    val id: String = System.currentTimeMillis().toString()
)

/**
 * Toast 操作数据类
 */
data class UnifyToastAction(
    val text: String,
    val onClick: () -> Unit
)

/**
 * Toast 状态管理
 */
class UnifyToastState {
    private val _toasts = mutableStateListOf<UnifyToastData>()
    val toasts: List<UnifyToastData> = _toasts
    
    fun showToast(toast: UnifyToastData) {
        _toasts.add(toast)
    }
    
    fun showToast(
        message: String,
        variant: UnifyToastVariant = UnifyToastVariant.DEFAULT,
        duration: Long = 3000L,
        icon: ImageVector? = null,
        action: UnifyToastAction? = null,
        dismissible: Boolean = true
    ) {
        showToast(
            UnifyToastData(
                message = message,
                variant = variant,
                duration = duration,
                icon = icon,
                action = action,
                dismissible = dismissible
            )
        )
    }
    
    fun dismissToast(id: String) {
        _toasts.removeAll { it.id == id }
    }
    
    fun dismissAll() {
        _toasts.clear()
    }
}

/**
 * 记住 Toast 状态
 */
@Composable
fun rememberUnifyToastState(): UnifyToastState {
    return remember { UnifyToastState() }
}

/**
 * Toast 容器组件
 */
@Composable
fun UnifyToastHost(
    toastState: UnifyToastState,
    modifier: Modifier = Modifier,
    position: UnifyToastPosition = UnifyToastPosition.BOTTOM,
    size: UnifyToastSize = UnifyToastSize.STANDARD,
    maxToasts: Int = 3,
    spacing: Dp = 8.dp
) {
    val density = LocalDensity.current
    
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = when (position) {
            UnifyToastPosition.TOP -> Alignment.TopCenter
            UnifyToastPosition.CENTER -> Alignment.Center
            UnifyToastPosition.BOTTOM -> Alignment.BottomCenter
        }
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .zIndex(1000f),
            verticalArrangement = Arrangement.spacedBy(spacing),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            toastState.toasts.takeLast(maxToasts).forEach { toast ->
                key(toast.id) {
                    UnifyToastItem(
                        toast = toast,
                        size = size,
                        onDismiss = { toastState.dismissToast(toast.id) }
                    )
                }
            }
        }
    }
}

/**
 * 单个 Toast 项组件
 */
@Composable
private fun UnifyToastItem(
    toast: UnifyToastData,
    size: UnifyToastSize,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalUnifyTheme.current
    var visible by remember { mutableStateOf(false) }
    
    // 自动显示和隐藏
    LaunchedEffect(toast.id) {
        visible = true
        delay(toast.duration)
        visible = false
        delay(300) // 等待退出动画
        onDismiss()
    }
    
    // 获取 Toast 配置
    val toastConfig = getToastConfig(toast.variant, size, theme)
    
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(300, easing = EaseOutCubic)
        ) + fadeIn(animationSpec = tween(300)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(300, easing = EaseInCubic)
        ) + fadeOut(animationSpec = tween(300))
    ) {
        Surface(
            modifier = modifier
                .widthIn(min = 280.dp, max = 560.dp)
                .semantics {
                    liveRegion = LiveRegionMode.Polite
                    contentDescription = toast.message
                },
            shape = toastConfig.shape,
            color = toastConfig.backgroundColor,
            contentColor = toastConfig.contentColor,
            tonalElevation = toastConfig.elevation,
            shadowElevation = toastConfig.elevation
        ) {
            Row(
                modifier = Modifier.padding(toastConfig.padding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 图标
                val iconVector = toast.icon ?: getDefaultIcon(toast.variant)
                iconVector?.let { icon ->
                    UnifyIcon(
                        imageVector = icon,
                        contentDescription = null,
                        size = toastConfig.iconSize,
                        tint = toastConfig.iconColor
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                
                // 消息文本
                UnifyText(
                    text = toast.message,
                    variant = toastConfig.textVariant,
                    color = toastConfig.contentColor,
                    modifier = Modifier.weight(1f)
                )
                
                // 操作按钮
                toast.action?.let { action ->
                    Spacer(modifier = Modifier.width(12.dp))
                    UnifyButton(
                        text = action.text,
                        onClick = action.onClick,
                        variant = UnifyButtonVariant.TEXT,
                        size = UnifyButtonSize.SMALL,
                        contentColor = toastConfig.actionColor
                    )
                }
                
                // 关闭按钮
                if (toast.dismissible) {
                    Spacer(modifier = Modifier.width(8.dp))
                    UnifyIconButton(
                        imageVector = androidx.compose.material.icons.Icons.Default.Close,
                        contentDescription = "关闭",
                        onClick = onDismiss,
                        size = UnifyIconSize.EXTRA_SMALL,
                        tint = toastConfig.contentColor.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * 简化的 Toast 显示函数
 */
@Composable
fun UnifyToast(
    message: String,
    variant: UnifyToastVariant = UnifyToastVariant.DEFAULT,
    duration: Long = 3000L,
    position: UnifyToastPosition = UnifyToastPosition.BOTTOM,
    size: UnifyToastSize = UnifyToastSize.STANDARD,
    icon: ImageVector? = null,
    action: UnifyToastAction? = null,
    dismissible: Boolean = true,
    onDismiss: (() -> Unit)? = null
) {
    var visible by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        delay(duration)
        visible = false
        delay(300)
        onDismiss?.invoke()
    }
    
    if (visible) {
        val theme = LocalUnifyTheme.current
        val toastConfig = getToastConfig(variant, size, theme)
        
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = when (position) {
                UnifyToastPosition.TOP -> Alignment.TopCenter
                UnifyToastPosition.CENTER -> Alignment.Center
                UnifyToastPosition.BOTTOM -> Alignment.BottomCenter
            }
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(
                    initialOffsetY = { if (position == UnifyToastPosition.TOP) -it else it },
                    animationSpec = tween(300)
                ) + fadeIn(),
                exit = slideOutVertically(
                    targetOffsetY = { if (position == UnifyToastPosition.TOP) -it else it },
                    animationSpec = tween(300)
                ) + fadeOut()
            ) {
                Surface(
                    modifier = Modifier
                        .padding(16.dp)
                        .widthIn(min = 280.dp, max = 560.dp)
                        .zIndex(1000f),
                    shape = toastConfig.shape,
                    color = toastConfig.backgroundColor,
                    contentColor = toastConfig.contentColor,
                    tonalElevation = toastConfig.elevation
                ) {
                    Row(
                        modifier = Modifier.padding(toastConfig.padding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val iconVector = icon ?: getDefaultIcon(variant)
                        iconVector?.let { iconVec ->
                            UnifyIcon(
                                imageVector = iconVec,
                                contentDescription = null,
                                size = toastConfig.iconSize,
                                tint = toastConfig.iconColor
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                        
                        UnifyText(
                            text = message,
                            variant = toastConfig.textVariant,
                            color = toastConfig.contentColor,
                            modifier = Modifier.weight(1f)
                        )
                        
                        action?.let { actionData ->
                            Spacer(modifier = Modifier.width(12.dp))
                            UnifyButton(
                                text = actionData.text,
                                onClick = actionData.onClick,
                                variant = UnifyButtonVariant.TEXT,
                                size = UnifyButtonSize.SMALL,
                                contentColor = toastConfig.actionColor
                            )
                        }
                        
                        if (dismissible) {
                            Spacer(modifier = Modifier.width(8.dp))
                            UnifyIconButton(
                                imageVector = androidx.compose.material.icons.Icons.Default.Close,
                                contentDescription = "关闭",
                                onClick = { visible = false },
                                size = UnifyIconSize.EXTRA_SMALL,
                                tint = toastConfig.contentColor.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 获取默认图标
 */
private fun getDefaultIcon(variant: UnifyToastVariant): ImageVector? {
    return when (variant) {
        UnifyToastVariant.SUCCESS -> androidx.compose.material.icons.Icons.Default.CheckCircle
        UnifyToastVariant.WARNING -> androidx.compose.material.icons.Icons.Default.Warning
        UnifyToastVariant.ERROR -> androidx.compose.material.icons.Icons.Default.Error
        UnifyToastVariant.INFO -> androidx.compose.material.icons.Icons.Default.Info
        UnifyToastVariant.DEFAULT -> null
    }
}

/**
 * 获取 Toast 配置
 */
@Composable
private fun getToastConfig(
    variant: UnifyToastVariant,
    size: UnifyToastSize,
    theme: com.unify.ui.theme.UnifyTheme
): ToastConfig {
    val (backgroundColor, contentColor, iconColor, actionColor) = when (variant) {
        UnifyToastVariant.SUCCESS -> ToastColors(
            backgroundColor = theme.colors.success,
            contentColor = theme.colors.onSuccess,
            iconColor = theme.colors.onSuccess,
            actionColor = theme.colors.onSuccess
        )
        UnifyToastVariant.WARNING -> ToastColors(
            backgroundColor = theme.colors.warning,
            contentColor = theme.colors.onWarning,
            iconColor = theme.colors.onWarning,
            actionColor = theme.colors.onWarning
        )
        UnifyToastVariant.ERROR -> ToastColors(
            backgroundColor = theme.colors.error,
            contentColor = theme.colors.onError,
            iconColor = theme.colors.onError,
            actionColor = theme.colors.onError
        )
        UnifyToastVariant.INFO -> ToastColors(
            backgroundColor = theme.colors.info,
            contentColor = theme.colors.onInfo,
            iconColor = theme.colors.onInfo,
            actionColor = theme.colors.onInfo
        )
        UnifyToastVariant.DEFAULT -> ToastColors(
            backgroundColor = theme.colors.inverseSurface,
            contentColor = theme.colors.inverseOnSurface,
            iconColor = theme.colors.inverseOnSurface,
            actionColor = theme.colors.inversePrimary
        )
    }
    
    val (padding, iconSize, textVariant, elevation) = when (size) {
        UnifyToastSize.COMPACT -> ToastSizeConfig(
            padding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            iconSize = UnifyIconSize.EXTRA_SMALL,
            textVariant = UnifyTextVariant.BODY_SMALL,
            elevation = 3.dp
        )
        UnifyToastSize.STANDARD -> ToastSizeConfig(
            padding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            iconSize = UnifyIconSize.SMALL,
            textVariant = UnifyTextVariant.BODY_MEDIUM,
            elevation = 6.dp
        )
        UnifyToastSize.LARGE -> ToastSizeConfig(
            padding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            iconSize = UnifyIconSize.MEDIUM,
            textVariant = UnifyTextVariant.BODY_LARGE,
            elevation = 8.dp
        )
    }
    
    return ToastConfig(
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        iconColor = iconColor,
        actionColor = actionColor,
        padding = padding,
        iconSize = iconSize,
        textVariant = textVariant,
        elevation = elevation,
        shape = theme.shapes.small
    )
}

/**
 * Toast 颜色配置
 */
private data class ToastColors(
    val backgroundColor: Color,
    val contentColor: Color,
    val iconColor: Color,
    val actionColor: Color
)

/**
 * Toast 尺寸配置
 */
private data class ToastSizeConfig(
    val padding: PaddingValues,
    val iconSize: UnifyIconSize,
    val textVariant: UnifyTextVariant,
    val elevation: Dp
)

/**
 * Toast 配置数据类
 */
private data class ToastConfig(
    val backgroundColor: Color,
    val contentColor: Color,
    val iconColor: Color,
    val actionColor: Color,
    val padding: PaddingValues,
    val iconSize: UnifyIconSize,
    val textVariant: UnifyTextVariant,
    val elevation: Dp,
    val shape: Shape
)
