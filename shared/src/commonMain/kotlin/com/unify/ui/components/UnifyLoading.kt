package com.unify.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Unify加载组件
 * 提供多种样式的加载指示器和状态显示
 */

/**
 * 基础加载指示器
 */
@Composable
fun UnifyLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    size: Dp = 40.dp,
) {
    CircularProgressIndicator(
        modifier = modifier.size(size),
        color = color,
        strokeWidth = 4.dp,
    )
}

/**
 * 带文本的加载指示器
 */
@Composable
fun UnifyLoadingWithText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        UnifyLoadingIndicator(color = color)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/**
 * 脉冲加载指示器
 */
@Composable
fun UnifyPulseLoading(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    size: Dp = 40.dp,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "scale",
    )

    Box(
        modifier =
            modifier
                .size(size)
                .background(color.copy(alpha = 0.3f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(size.times(scale))
                    .background(color, CircleShape),
        )
    }
}

/**
 * 点状加载指示器
 */
@Composable
fun UnifyDotsLoading(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    dotCount: Int = 3,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(dotCount) { index ->
            val delay = index * 200
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1.0f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(600, delayMillis = delay),
                        repeatMode = RepeatMode.Reverse,
                    ),
                label = "dot_$index",
            )

            Box(
                modifier =
                    Modifier
                        .size(12.dp * scale)
                        .background(color, CircleShape),
            )
        }
    }
}

/**
 * 波浪加载指示器
 */
@Composable
fun UnifyWaveLoading(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    barCount: Int = 5,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        repeat(barCount) { index ->
            val delay = index * 100
            val height by infiniteTransition.animateFloat(
                initialValue = 10f,
                targetValue = 40f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(800, delayMillis = delay),
                        repeatMode = RepeatMode.Reverse,
                    ),
                label = "bar_$index",
            )

            Box(
                modifier =
                    Modifier
                        .width(6.dp)
                        .height(height.dp)
                        .background(color, RoundedCornerShape(3.dp)),
            )
        }
    }
}

/**
 * 旋转加载指示器
 */
@Composable
fun UnifySpinnerLoading(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    size: Dp = 40.dp,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "spinner")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
            ),
        label = "rotation",
    )

    Canvas(
        modifier =
            modifier
                .size(size)
                .rotate(rotation),
    ) {
        val strokeWidth = 4.dp.toPx()
        drawArc(
            color = color,
            startAngle = 0f,
            sweepAngle = 270f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )
    }
}

/**
 * 骨架屏加载
 */
@Composable
fun UnifySkeletonLoading(
    modifier: Modifier = Modifier,
    lines: Int = 3,
    showAvatar: Boolean = false,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "alpha",
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        if (showAvatar) {
            Box(
                modifier =
                    Modifier
                        .size(48.dp)
                        .background(
                            MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                            CircleShape,
                        ),
            )
            Spacer(modifier = Modifier.width(12.dp))
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            repeat(lines) { index ->
                val width =
                    when (index) {
                        lines - 1 -> 0.6f // 最后一行较短
                        else -> 1f
                    }

                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth(width)
                            .height(16.dp)
                            .background(
                                MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                                RoundedCornerShape(8.dp),
                            ),
                )
            }
        }
    }
}

/**
 * 进度条加载
 */
@Composable
fun UnifyProgressLoading(
    progress: Float,
    modifier: Modifier = Modifier,
    showPercentage: Boolean = true,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Column(modifier = modifier) {
        if (showPercentage) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "加载中...",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth(),
            color = color,
        )
    }
}

/**
 * 圆形进度加载
 */
@Composable
fun UnifyCircularProgressLoading(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    strokeWidth: Dp = 8.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    showPercentage: Boolean = true,
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            progress = progress,
            modifier = Modifier.size(size),
            strokeWidth = strokeWidth,
            color = color,
        )

        if (showPercentage) {
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

/**
 * 全屏加载遮罩
 */
@Composable
fun UnifyFullScreenLoading(
    isVisible: Boolean,
    message: String = "加载中...",
    modifier: Modifier = Modifier,
) {
    if (isVisible) {
        Box(
            modifier =
                modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center,
        ) {
            Card(
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            ) {
                UnifyLoadingWithText(
                    text = message,
                    modifier = Modifier.padding(24.dp),
                )
            }
        }
    }
}

/**
 * 自定义形状加载
 */
@Composable
fun UnifyCustomShapeLoading(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    size: Dp = 40.dp,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "custom")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
            ),
        label = "rotation",
    )

    Canvas(
        modifier =
            modifier
                .size(size)
                .rotate(rotation),
    ) {
        val radius = size.toPx().div(2f)
        val centerX = size.toPx().div(2f)
        val centerY = size.toPx().div(2f)

        // 绘制自定义形状（三角形旋转）
        repeat(3) { i ->
            val angle = i.times(120f).plus(rotation).times(kotlin.math.PI.div(180f))
            val x = centerX.plus(cos(angle).toFloat().times(radius).times(0.6f))
            val y = centerY.plus(sin(angle).toFloat().times(radius).times(0.6f))

            drawCircle(
                color = color,
                radius = 6.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(x, y),
            )
        }
    }
}

/**
 * 加载状态管理器
 */
@Composable
fun UnifyLoadingStateManager(
    loadingState: LoadingState,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier) {
        when (loadingState) {
            is LoadingState.Loading -> {
                UnifyLoadingWithText(
                    text = loadingState.message,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
            is LoadingState.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "❌",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = loadingState.message,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    if (onRetry != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onRetry) {
                            Text("重试")
                        }
                    }
                }
            }
            is LoadingState.Success -> {
                content()
            }
            is LoadingState.Empty -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "📭",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = loadingState.message,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

// 数据类
sealed class LoadingState {
    data class Loading(val message: String = "加载中...") : LoadingState()

    data class Error(val message: String) : LoadingState()

    object Success : LoadingState()

    data class Empty(val message: String = "暂无数据") : LoadingState()
}
