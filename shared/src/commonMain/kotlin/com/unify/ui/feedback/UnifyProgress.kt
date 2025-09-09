package com.unify.ui.feedback

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Unify进度条组件
 * 支持线性和圆形进度条，以及多种样式
 */
@Composable
fun UnifyLinearProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    strokeCap: StrokeCap = StrokeCap.Round,
    height: Dp = 8.dp,
    animated: Boolean = true,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (animated) progress.coerceIn(0f, 1f) else progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 300),
        label = "linear_progress",
    )

    Canvas(
        modifier =
            modifier
                .fillMaxWidth()
                .height(height),
    ) {
        val strokeWidth = size.height
        val progressWidth = size.width * animatedProgress

        // 背景
        drawRoundRect(
            color = backgroundColor,
            size = size,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(strokeWidth / 2),
        )

        // 进度
        if (progressWidth > 0) {
            drawRoundRect(
                color = color,
                size = Size(progressWidth, size.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(strokeWidth / 2),
            )
        }
    }
}

/**
 * 带标签的线性进度条
 */
@Composable
fun UnifyLinearProgressWithLabel(
    progress: Float,
    modifier: Modifier = Modifier,
    label: String? = null,
    showPercentage: Boolean = true,
    color: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    height: Dp = 8.dp,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // 标签和百分比
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            label?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            if (showPercentage) {
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        // 进度条
        UnifyLinearProgress(
            progress = progress,
            color = color,
            backgroundColor = backgroundColor,
            height = height,
        )
    }
}

/**
 * 圆形进度条
 */
@Composable
fun UnifyCircularProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    strokeWidth: Dp = 8.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    animated: Boolean = true,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (animated) progress.coerceIn(0f, 1f) else progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 500),
        label = "circular_progress",
    )

    Canvas(
        modifier = modifier.size(size),
    ) {
        val strokeWidthPx = strokeWidth.toPx()
        val radius = (size.toPx() - strokeWidthPx) / 2
        val center = Offset(size.toPx() / 2, size.toPx() / 2)

        // 背景圆环
        drawCircle(
            color = backgroundColor,
            radius = radius,
            center = center,
            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
        )

        // 进度圆弧
        if (animatedProgress > 0) {
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
                size = Size(radius * 2, radius * 2),
                topLeft = Offset(center.x - radius, center.y - radius),
            )
        }
    }
}

/**
 * 带文本的圆形进度条
 */
@Composable
fun UnifyCircularProgressWithText(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    strokeWidth: Dp = 12.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    text: String? = null,
    showPercentage: Boolean = true,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        UnifyCircularProgress(
            progress = progress,
            size = size,
            strokeWidth = strokeWidth,
            color = color,
            backgroundColor = backgroundColor,
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (showPercentage) {
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                )
            }

            text?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

/**
 * 步骤进度条
 */
@Composable
fun UnifyStepProgress(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier,
    stepLabels: List<String> = emptyList(),
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    completedColor: Color = MaterialTheme.colorScheme.primary,
    stepSize: Dp = 32.dp,
    lineHeight: Dp = 4.dp,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        // 步骤指示器
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(totalSteps) { index ->
                val stepNumber = index + 1
                val isCompleted = stepNumber < currentStep
                val isActive = stepNumber == currentStep

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                ) {
                    // 步骤圆圈
                    Box(
                        modifier =
                            Modifier
                                .size(stepSize)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(
                                    when {
                                        isCompleted -> completedColor
                                        isActive -> activeColor
                                        else -> inactiveColor
                                    },
                                ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (isCompleted) "✓" else stepNumber.toString(),
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    // 连接线（除了最后一步）
                    if (index < totalSteps - 1) {
                        Box(
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .height(lineHeight)
                                    .background(
                                        if (isCompleted) completedColor else inactiveColor,
                                        RoundedCornerShape(lineHeight / 2),
                                    ),
                        )
                    }
                }
            }
        }

        // 步骤标签
        if (stepLabels.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                stepLabels.take(totalSteps).forEachIndexed { index, label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        color =
                            if (index + 1 <= currentStep) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            },
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

/**
 * 多段进度条
 */
@Composable
fun UnifyMultiSegmentProgress(
    segments: List<ProgressSegment>,
    modifier: Modifier = Modifier,
    height: Dp = 12.dp,
    spacing: Dp = 2.dp,
    showLabels: Boolean = true,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // 进度条
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(height),
            horizontalArrangement = Arrangement.spacedBy(spacing),
        ) {
            segments.forEach { segment ->
                Box(
                    modifier =
                        Modifier
                            .weight(segment.weight)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(height / 2))
                            .background(segment.color),
                )
            }
        }

        // 标签
        if (showLabels) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                segments.forEach { segment ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .size(12.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(segment.color),
                        )
                        Text(
                            text = segment.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}

/**
 * 不确定进度条
 */
@Composable
fun UnifyIndeterminateProgress(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    height: Dp = 4.dp,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "indeterminate")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(1500, easing = LinearEasing),
            ),
        label = "indeterminate_offset",
    )

    Canvas(
        modifier =
            modifier
                .fillMaxWidth()
                .height(height),
    ) {
        val width = size.width
        val progressWidth = width * 0.3f
        val startX = (width - progressWidth) * offset

        // 背景
        drawRoundRect(
            color = backgroundColor,
            size = size,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2),
        )

        // 移动的进度条
        drawRoundRect(
            color = color,
            topLeft = Offset(startX, 0f),
            size = Size(progressWidth, size.height),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2),
        )
    }
}

// 数据类

data class ProgressSegment(
    val weight: Float,
    val color: Color,
    val label: String,
)
