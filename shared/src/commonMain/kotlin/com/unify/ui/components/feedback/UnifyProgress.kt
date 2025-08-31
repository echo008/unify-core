package com.unify.ui.components.feedback

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.unify.ui.LocalUnifyTheme
import com.unify.ui.LocalUnifyPlatformTheme
import com.unify.ui.components.foundation.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * Unify Progress 组件
 * 支持多平台适配的统一进度指示器组件，参考 KuiklyUI 设计规范
 */

/**
 * 进度条变体枚举
 */
enum class UnifyProgressVariant {
    LINEAR,         // 线性进度条
    CIRCULAR,       // 圆形进度条
    RING,           // 环形进度条
    STEPPED,        // 步骤进度条
    RADIAL          // 径向进度条
}

/**
 * 进度条尺寸枚举
 */
enum class UnifyProgressSize {
    SMALL,          // 小尺寸
    MEDIUM,         // 中等尺寸
    LARGE,          // 大尺寸
    EXTRA_LARGE     // 超大尺寸
}

/**
 * 进度条状态枚举
 */
enum class UnifyProgressState {
    NORMAL,         // 正常
    SUCCESS,        // 成功
    WARNING,        // 警告
    ERROR           // 错误
}

/**
 * 主要 Unify Progress 组件
 */
@Composable
fun UnifyProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    variant: UnifyProgressVariant = UnifyProgressVariant.LINEAR,
    size: UnifyProgressSize = UnifyProgressSize.MEDIUM,
    state: UnifyProgressState = UnifyProgressState.NORMAL,
    showPercentage: Boolean = false,
    showLabel: Boolean = false,
    label: String? = null,
    color: Color? = null,
    backgroundColor: Color? = null,
    strokeWidth: Dp? = null,
    animated: Boolean = true,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    val progressConfig = getProgressConfig(variant, size, state, theme)
    val actualColor = color ?: progressConfig.color
    val actualBackgroundColor = backgroundColor ?: progressConfig.backgroundColor
    val actualStrokeWidth = strokeWidth ?: progressConfig.strokeWidth
    
    // 动画进度值
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = if (animated) {
            tween(durationMillis = 300, easing = EaseOutCubic)
        } else {
            snap()
        },
        label = "progress_animation"
    )
    
    Box(
        modifier = modifier.semantics {
            progressBarRangeInfo = ProgressBarRangeInfo(
                current = progress,
                range = 0f..1f
            )
            contentDescription?.let { 
                this.contentDescription = it 
            }
        }
    ) {
        when (variant) {
            UnifyProgressVariant.LINEAR -> {
                UnifyLinearProgress(
                    progress = animatedProgress,
                    size = progressConfig.size,
                    color = actualColor,
                    backgroundColor = actualBackgroundColor,
                    showPercentage = showPercentage,
                    showLabel = showLabel,
                    label = label
                )
            }
            
            UnifyProgressVariant.CIRCULAR -> {
                UnifyCircularProgress(
                    progress = animatedProgress,
                    size = progressConfig.size,
                    strokeWidth = actualStrokeWidth,
                    color = actualColor,
                    backgroundColor = actualBackgroundColor,
                    showPercentage = showPercentage,
                    showLabel = showLabel,
                    label = label
                )
            }
            
            UnifyProgressVariant.RING -> {
                UnifyRingProgress(
                    progress = animatedProgress,
                    size = progressConfig.size,
                    strokeWidth = actualStrokeWidth,
                    color = actualColor,
                    backgroundColor = actualBackgroundColor,
                    showPercentage = showPercentage
                )
            }
            
            UnifyProgressVariant.STEPPED -> {
                UnifySteppedProgress(
                    progress = animatedProgress,
                    size = progressConfig.size,
                    color = actualColor,
                    backgroundColor = actualBackgroundColor,
                    showPercentage = showPercentage
                )
            }
            
            UnifyProgressVariant.RADIAL -> {
                UnifyRadialProgress(
                    progress = animatedProgress,
                    size = progressConfig.size,
                    strokeWidth = actualStrokeWidth,
                    color = actualColor,
                    backgroundColor = actualBackgroundColor,
                    showPercentage = showPercentage
                )
            }
        }
    }
}

/**
 * 线性进度条组件
 */
@Composable
private fun UnifyLinearProgress(
    progress: Float,
    size: Dp,
    color: Color,
    backgroundColor: Color,
    showPercentage: Boolean,
    showLabel: Boolean,
    label: String?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // 标签
        if (showLabel && label != null) {
            UnifyText(
                text = label,
                variant = UnifyTextVariant.BODY_SMALL,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .weight(1f)
                    .height(size),
                color = color,
                trackColor = backgroundColor,
                strokeCap = StrokeCap.Round
            )
            
            // 百分比显示
            if (showPercentage) {
                Spacer(modifier = Modifier.width(8.dp))
                UnifyText(
                    text = "${(progress * 100).toInt()}%",
                    variant = UnifyTextVariant.CAPTION,
                    modifier = Modifier.widthIn(min = 32.dp),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

/**
 * 圆形进度条组件
 */
@Composable
private fun UnifyCircularProgress(
    progress: Float,
    size: Dp,
    strokeWidth: Dp,
    color: Color,
    backgroundColor: Color,
    showPercentage: Boolean,
    showLabel: Boolean,
    label: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                color = color,
                strokeWidth = strokeWidth,
                trackColor = backgroundColor,
                strokeCap = StrokeCap.Round
            )
            
            // 中心内容
            if (showPercentage) {
                UnifyText(
                    text = "${(progress * 100).toInt()}%",
                    variant = when {
                        size <= 48.dp -> UnifyTextVariant.CAPTION
                        size <= 72.dp -> UnifyTextVariant.BODY_SMALL
                        else -> UnifyTextVariant.BODY_MEDIUM
                    },
                    textAlign = TextAlign.Center
                )
            }
        }
        
        // 标签
        if (showLabel && label != null) {
            Spacer(modifier = Modifier.height(8.dp))
            UnifyText(
                text = label,
                variant = UnifyTextVariant.BODY_SMALL,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 环形进度条组件
 */
@Composable
private fun UnifyRingProgress(
    progress: Float,
    size: Dp,
    strokeWidth: Dp,
    color: Color,
    backgroundColor: Color,
    showPercentage: Boolean,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = this.size
            val radius = (canvasSize.minDimension - strokeWidth.toPx()) / 2
            val center = Offset(canvasSize.width / 2, canvasSize.height / 2)
            val strokeWidthPx = strokeWidth.toPx()
            
            // 背景圆环
            drawCircle(
                color = backgroundColor,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
            
            // 进度圆环
            val sweepAngle = 360f * progress
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(
                    center.x - radius,
                    center.y - radius
                ),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
        }
        
        // 中心百分比
        if (showPercentage) {
            UnifyText(
                text = "${(progress * 100).toInt()}%",
                variant = when {
                    size <= 48.dp -> UnifyTextVariant.CAPTION
                    size <= 72.dp -> UnifyTextVariant.BODY_SMALL
                    else -> UnifyTextVariant.BODY_MEDIUM
                },
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 步骤进度条组件
 */
@Composable
private fun UnifySteppedProgress(
    progress: Float,
    size: Dp,
    color: Color,
    backgroundColor: Color,
    showPercentage: Boolean,
    steps: Int = 5,
    modifier: Modifier = Modifier
) {
    val completedSteps = (progress * steps).toInt()
    
    Column(modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(steps) { index ->
                val isCompleted = index < completedSteps
                val isActive = index == completedSteps && progress > 0f
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(size)
                        .clip(RoundedCornerShape(size / 2))
                        .background(
                            when {
                                isCompleted -> color
                                isActive -> color.copy(alpha = 0.5f)
                                else -> backgroundColor
                            }
                        )
                )
            }
        }
        
        if (showPercentage) {
            Spacer(modifier = Modifier.height(4.dp))
            UnifyText(
                text = "${(progress * 100).toInt()}%",
                variant = UnifyTextVariant.CAPTION,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * 径向进度条组件
 */
@Composable
private fun UnifyRadialProgress(
    progress: Float,
    size: Dp,
    strokeWidth: Dp,
    color: Color,
    backgroundColor: Color,
    showPercentage: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = this.size
            val radius = canvasSize.minDimension / 2
            val center = Offset(canvasSize.width / 2, canvasSize.height / 2)
            val strokeWidthPx = strokeWidth.toPx()
            
            // 背景
            drawCircle(
                color = backgroundColor,
                radius = radius,
                center = center
            )
            
            // 进度扇形
            val sweepAngle = 360f * progress
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset.Zero,
                size = canvasSize
            )
            
            // 中心圆（遮罩效果）
            val innerRadius = radius - strokeWidthPx
            if (innerRadius > 0) {
                drawCircle(
                    color = backgroundColor,
                    radius = innerRadius,
                    center = center
                )
            }
        }
        
        if (showPercentage) {
            UnifyText(
                text = "${(progress * 100).toInt()}%",
                variant = when {
                    size <= 48.dp -> UnifyTextVariant.CAPTION
                    size <= 72.dp -> UnifyTextVariant.BODY_SMALL
                    else -> UnifyTextVariant.BODY_MEDIUM
                },
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 多步骤进度条组件
 */
@Composable
fun UnifyStepProgress(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier,
    size: UnifyProgressSize = UnifyProgressSize.MEDIUM,
    state: UnifyProgressState = UnifyProgressState.NORMAL,
    stepLabels: List<String>? = null,
    showStepNumbers: Boolean = true,
    color: Color? = null,
    backgroundColor: Color? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    val progressConfig = getProgressConfig(UnifyProgressVariant.STEPPED, size, state, theme)
    val actualColor = color ?: progressConfig.color
    val actualBackgroundColor = backgroundColor ?: progressConfig.backgroundColor
    val stepSize = progressConfig.size
    
    Column(
        modifier = modifier.semantics {
            progressBarRangeInfo = ProgressBarRangeInfo(
                current = currentStep.toFloat(),
                range = 0f..totalSteps.toFloat()
            )
            contentDescription?.let { 
                this.contentDescription = it 
            }
        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(totalSteps) { index ->
                val stepNumber = index + 1
                val isCompleted = stepNumber < currentStep
                val isActive = stepNumber == currentStep
                val isFuture = stepNumber > currentStep
                
                // 步骤圆圈
                Box(
                    modifier = Modifier
                        .size(stepSize)
                        .background(
                            when {
                                isCompleted -> actualColor
                                isActive -> actualColor
                                else -> actualBackgroundColor
                            },
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (showStepNumbers) {
                        UnifyText(
                            text = if (isCompleted) "✓" else stepNumber.toString(),
                            variant = UnifyTextVariant.CAPTION,
                            color = when {
                                isCompleted -> theme.colors.onPrimary
                                isActive -> theme.colors.onPrimary
                                else -> theme.colors.onSurface
                            }
                        )
                    }
                }
                
                // 连接线
                if (index < totalSteps - 1) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(
                                if (isCompleted) actualColor else actualBackgroundColor
                            )
                    )
                }
            }
        }
        
        // 步骤标签
        if (stepLabels != null && stepLabels.size >= totalSteps) {
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                repeat(totalSteps) { index ->
                    UnifyText(
                        text = stepLabels[index],
                        variant = UnifyTextVariant.CAPTION,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                        color = if (index + 1 <= currentStep) {
                            actualColor
                        } else {
                            theme.colors.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

/**
 * 获取进度条配置
 */
@Composable
private fun getProgressConfig(
    variant: UnifyProgressVariant,
    size: UnifyProgressSize,
    state: UnifyProgressState,
    theme: com.unify.ui.theme.UnifyTheme
): ProgressConfig {
    val (progressSize, strokeWidth) = when (size) {
        UnifyProgressSize.SMALL -> Pair(
            when (variant) {
                UnifyProgressVariant.LINEAR -> 4.dp
                else -> 32.dp
            },
            2.dp
        )
        UnifyProgressSize.MEDIUM -> Pair(
            when (variant) {
                UnifyProgressVariant.LINEAR -> 6.dp
                else -> 48.dp
            },
            3.dp
        )
        UnifyProgressSize.LARGE -> Pair(
            when (variant) {
                UnifyProgressVariant.LINEAR -> 8.dp
                else -> 64.dp
            },
            4.dp
        )
        UnifyProgressSize.EXTRA_LARGE -> Pair(
            when (variant) {
                UnifyProgressVariant.LINEAR -> 12.dp
                else -> 96.dp
            },
            6.dp
        )
    }
    
    val (color, backgroundColor) = when (state) {
        UnifyProgressState.SUCCESS -> Pair(
            theme.colors.success,
            theme.colors.success.copy(alpha = 0.1f)
        )
        UnifyProgressState.WARNING -> Pair(
            theme.colors.warning,
            theme.colors.warning.copy(alpha = 0.1f)
        )
        UnifyProgressState.ERROR -> Pair(
            theme.colors.error,
            theme.colors.error.copy(alpha = 0.1f)
        )
        UnifyProgressState.NORMAL -> Pair(
            theme.colors.primary,
            theme.colors.primary.copy(alpha = 0.1f)
        )
    }
    
    return ProgressConfig(
        size = progressSize,
        strokeWidth = strokeWidth,
        color = color,
        backgroundColor = backgroundColor
    )
}

/**
 * 进度条配置数据类
 */
private data class ProgressConfig(
    val size: Dp,
    val strokeWidth: Dp,
    val color: Color,
    val backgroundColor: Color
)
