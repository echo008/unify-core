package com.unify.ui.components.feedback

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.unify.ui.LocalUnifyTheme
import com.unify.ui.LocalUnifyPlatformTheme
import com.unify.ui.components.foundation.*

/**
 * Unify Loading 组件
 * 支持多平台适配的统一加载指示器组件，参考 KuiklyUI 设计规范
 */

/**
 * 加载指示器变体枚举
 */
enum class UnifyLoadingVariant {
    CIRCULAR,       // 圆形加载器
    LINEAR,         // 线性加载器
    DOTS,           // 点状加载器
    SPINNER,        // 旋转器
    PULSE,          // 脉冲加载器
    SKELETON        // 骨架屏
}

/**
 * 加载指示器尺寸枚举
 */
enum class UnifyLoadingSize {
    SMALL,          // 小尺寸 - 16dp
    MEDIUM,         // 中等尺寸 - 24dp
    LARGE,          // 大尺寸 - 32dp
    EXTRA_LARGE     // 超大尺寸 - 48dp
}

/**
 * 主要 Unify Loading 组件
 */
@Composable
fun UnifyLoading(
    modifier: Modifier = Modifier,
    variant: UnifyLoadingVariant = UnifyLoadingVariant.CIRCULAR,
    size: UnifyLoadingSize = UnifyLoadingSize.MEDIUM,
    color: Color? = null,
    backgroundColor: Color? = null,
    strokeWidth: Dp? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    val actualColor = color ?: theme.colors.primary
    val actualSize = getLoadingSize(size)
    val actualStrokeWidth = strokeWidth ?: (actualSize / 8)
    
    Box(
        modifier = modifier.semantics {
            contentDescription?.let { 
                this.contentDescription = it 
            }
        },
        contentAlignment = Alignment.Center
    ) {
        when (variant) {
            UnifyLoadingVariant.CIRCULAR -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(actualSize),
                    color = actualColor,
                    strokeWidth = actualStrokeWidth,
                    trackColor = backgroundColor ?: actualColor.copy(alpha = 0.1f)
                )
            }
            
            UnifyLoadingVariant.LINEAR -> {
                LinearProgressIndicator(
                    modifier = Modifier
                        .width(actualSize * 3)
                        .height(actualStrokeWidth),
                    color = actualColor,
                    trackColor = backgroundColor ?: actualColor.copy(alpha = 0.1f)
                )
            }
            
            UnifyLoadingVariant.DOTS -> {
                UnifyDotsLoading(
                    size = actualSize,
                    color = actualColor
                )
            }
            
            UnifyLoadingVariant.SPINNER -> {
                UnifySpinnerLoading(
                    size = actualSize,
                    color = actualColor,
                    strokeWidth = actualStrokeWidth
                )
            }
            
            UnifyLoadingVariant.PULSE -> {
                UnifyPulseLoading(
                    size = actualSize,
                    color = actualColor
                )
            }
            
            UnifyLoadingVariant.SKELETON -> {
                UnifySkeletonLoading(
                    modifier = Modifier.size(actualSize),
                    color = backgroundColor ?: theme.colors.surfaceVariant
                )
            }
        }
    }
}

/**
 * 带文本的加载组件
 */
@Composable
fun UnifyLoadingWithText(
    text: String,
    modifier: Modifier = Modifier,
    variant: UnifyLoadingVariant = UnifyLoadingVariant.CIRCULAR,
    size: UnifyLoadingSize = UnifyLoadingSize.MEDIUM,
    color: Color? = null,
    textVariant: UnifyTextVariant = UnifyTextVariant.BODY_MEDIUM,
    arrangement: Arrangement.Vertical = Arrangement.spacedBy(12.dp),
    contentDescription: String? = null
) {
    Column(
        modifier = modifier.semantics {
            contentDescription?.let { 
                this.contentDescription = it 
            }
        },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = arrangement
    ) {
        UnifyLoading(
            variant = variant,
            size = size,
            color = color
        )
        
        UnifyText(
            text = text,
            variant = textVariant,
            color = color ?: LocalUnifyTheme.current.colors.onSurface
        )
    }
}

/**
 * 全屏加载遮罩组件
 */
@Composable
fun UnifyLoadingOverlay(
    visible: Boolean,
    modifier: Modifier = Modifier,
    variant: UnifyLoadingVariant = UnifyLoadingVariant.CIRCULAR,
    size: UnifyLoadingSize = UnifyLoadingSize.LARGE,
    text: String? = null,
    color: Color? = null,
    backgroundColor: Color? = null,
    scrimColor: Color? = null,
    dismissible: Boolean = false,
    onDismiss: (() -> Unit)? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    val actualScrimColor = scrimColor ?: Color.Black.copy(alpha = 0.5f)
    val actualBackgroundColor = backgroundColor ?: theme.colors.surface
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300))
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(actualScrimColor)
                .zIndex(999f)
                .let { mod ->
                    if (dismissible && onDismiss != null) {
                        mod.clickable { onDismiss() }
                    } else {
                        mod
                    }
                }
                .semantics {
                    contentDescription?.let { 
                        this.contentDescription = it 
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.padding(32.dp),
                shape = theme.shapes.medium,
                color = actualBackgroundColor,
                tonalElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier.padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (text != null) {
                        UnifyLoadingWithText(
                            text = text,
                            variant = variant,
                            size = size,
                            color = color
                        )
                    } else {
                        UnifyLoading(
                            variant = variant,
                            size = size,
                            color = color
                        )
                    }
                }
            }
        }
    }
}

/**
 * 点状加载组件
 */
@Composable
private fun UnifyDotsLoading(
    size: Dp,
    color: Color,
    modifier: Modifier = Modifier
) {
    val dotSize = size / 6
    val animationDelay = 150
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(dotSize / 2),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val infiniteTransition = rememberInfiniteTransition(label = "dot_$index")
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 600,
                        delayMillis = index * animationDelay,
                        easing = EaseInOutCubic
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot_scale_$index"
            )
            
            Box(
                modifier = Modifier
                    .size(dotSize * scale)
                    .background(color, CircleShape)
            )
        }
    }
}

/**
 * 旋转器加载组件
 */
@Composable
private fun UnifySpinnerLoading(
    size: Dp,
    color: Color,
    strokeWidth: Dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "spinner")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spinner_rotation"
    )
    
    Box(
        modifier = modifier
            .size(size)
            .rotate(rotation)
    ) {
        CircularProgressIndicator(
            progress = { 0.25f },
            modifier = Modifier.fillMaxSize(),
            color = color,
            strokeWidth = strokeWidth,
            trackColor = Color.Transparent,
            strokeCap = StrokeCap.Round
        )
    }
}

/**
 * 脉冲加载组件
 */
@Composable
private fun UnifyPulseLoading(
    size: Dp,
    color: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    Box(
        modifier = modifier
            .size(size * scale)
            .background(color.copy(alpha = alpha), CircleShape)
    )
}

/**
 * 骨架屏加载组件
 */
@Composable
private fun UnifySkeletonLoading(
    modifier: Modifier = Modifier,
    color: Color,
    shape: Shape = RoundedCornerShape(4.dp)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeleton_alpha"
    )
    
    Box(
        modifier = modifier
            .clip(shape)
            .background(color.copy(alpha = alpha))
    )
}

/**
 * 骨架屏文本组件
 */
@Composable
fun UnifySkeletonText(
    lines: Int = 1,
    modifier: Modifier = Modifier,
    lineHeight: Dp = 16.dp,
    spacing: Dp = 4.dp,
    color: Color? = null,
    lastLineWidthRatio: Float = 0.7f
) {
    val theme = LocalUnifyTheme.current
    val actualColor = color ?: theme.colors.surfaceVariant
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        repeat(lines) { index ->
            val width = if (index == lines - 1 && lines > 1) {
                Modifier.fillMaxWidth(lastLineWidthRatio)
            } else {
                Modifier.fillMaxWidth()
            }
            
            UnifySkeletonLoading(
                modifier = width.height(lineHeight),
                color = actualColor
            )
        }
    }
}

/**
 * 骨架屏卡片组件
 */
@Composable
fun UnifySkeletonCard(
    modifier: Modifier = Modifier,
    showAvatar: Boolean = true,
    showTitle: Boolean = true,
    showSubtitle: Boolean = true,
    showContent: Boolean = true,
    contentLines: Int = 3,
    color: Color? = null
) {
    val theme = LocalUnifyTheme.current
    val actualColor = color ?: theme.colors.surfaceVariant
    
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 头部（头像 + 标题）
        if (showAvatar || showTitle || showSubtitle) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showAvatar) {
                    UnifySkeletonLoading(
                        modifier = Modifier.size(40.dp),
                        color = actualColor,
                        shape = CircleShape
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (showTitle) {
                        UnifySkeletonLoading(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(16.dp),
                            color = actualColor
                        )
                    }
                    
                    if (showSubtitle) {
                        UnifySkeletonLoading(
                            modifier = Modifier
                                .fillMaxWidth(0.4f)
                                .height(12.dp),
                            color = actualColor
                        )
                    }
                }
            }
        }
        
        // 内容
        if (showContent) {
            UnifySkeletonText(
                lines = contentLines,
                color = actualColor
            )
        }
    }
}

/**
 * 获取加载指示器尺寸
 */
private fun getLoadingSize(size: UnifyLoadingSize): Dp {
    return when (size) {
        UnifyLoadingSize.SMALL -> 16.dp
        UnifyLoadingSize.MEDIUM -> 24.dp
        UnifyLoadingSize.LARGE -> 32.dp
        UnifyLoadingSize.EXTRA_LARGE -> 48.dp
    }
}
