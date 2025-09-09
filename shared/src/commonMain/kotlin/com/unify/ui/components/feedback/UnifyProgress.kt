package com.unify.ui.components.feedback

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
 * Unify跨平台进度指示器组件
 * 支持所有8大平台的统一进度显示体验
 */

@Composable
fun UnifyProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    strokeCap: StrokeCap = StrokeCap.Round,
) {
    LinearProgressIndicator(
        progress = { progress.coerceIn(0f, 1f) },
        modifier = modifier,
        color = color,
        trackColor = trackColor,
        strokeCap = strokeCap,
    )
}

@Composable
fun UnifyProgressWithLabel(
    progress: Float,
    modifier: Modifier = Modifier,
    label: String? = null,
    showPercentage: Boolean = true,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    val clampedProgress = progress.coerceIn(0f, 1f)

    Column(
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (label != null) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
            }
            if (showPercentage) {
                Text(
                    text = "${(clampedProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = color,
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        UnifyProgress(
            progress = clampedProgress,
            color = color,
            trackColor = trackColor,
        )
    }
}

@Composable
fun UnifyCircularProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    strokeWidth: Dp = 8.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    showPercentage: Boolean = true,
) {
    val clampedProgress = progress.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = clampedProgress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "circularProgress",
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize(),
        ) {
            val strokeWidthPx = strokeWidth.toPx()
            val radius = (size.toPx() - strokeWidthPx) / 2
            val center = Offset(size.toPx() / 2, size.toPx() / 2)

            // 绘制背景圆环
            drawCircle(
                color = trackColor,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
            )

            // 绘制进度圆弧
            val sweepAngle = animatedProgress * 360f
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round),
                topLeft = Offset(strokeWidthPx / 2, strokeWidthPx / 2),
                size = Size(radius * 2, radius * 2),
            )
        }

        if (showPercentage) {
            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun UnifyStepProgress(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier,
    stepLabels: List<String> = emptyList(),
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    completedColor: Color = MaterialTheme.colorScheme.primary,
) {
    Column(
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(totalSteps) { index ->
                val stepNumber = index + 1
                val isCompleted = stepNumber < currentStep
                val isActive = stepNumber == currentStep
                val isInactive = stepNumber > currentStep

                val stepColor =
                    when {
                        isCompleted -> completedColor
                        isActive -> activeColor
                        else -> inactiveColor
                    }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // 步骤圆圈
                    Box(
                        modifier =
                            Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                color = stepColor,
                                radius = size.minDimension / 2,
                            )
                        }
                        Text(
                            text = if (isCompleted) "✓" else stepNumber.toString(),
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    // 连接线（除了最后一步）
                    if (index < totalSteps - 1) {
                        Canvas(
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .height(2.dp),
                        ) {
                            drawLine(
                                color = if (isCompleted) completedColor else inactiveColor,
                                start = Offset(0f, size.height / 2),
                                end = Offset(size.width, size.height / 2),
                                strokeWidth = size.height,
                            )
                        }
                    }
                }
            }
        }

        // 步骤标签
        if (stepLabels.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                stepLabels.take(totalSteps).forEachIndexed { index, label ->
                    val stepNumber = index + 1
                    val isActive = stepNumber <= currentStep

                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isActive) activeColor else inactiveColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
fun UnifySegmentedProgress(
    segments: List<Float>,
    modifier: Modifier = Modifier,
    colors: List<Color> =
        listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.tertiary,
        ),
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    height: Dp = 8.dp,
) {
    Canvas(
        modifier =
            modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(height / 2)),
    ) {
        val totalWidth = size.width
        val segmentHeight = size.height

        // 绘制背景
        drawRect(
            color = trackColor,
            size = Size(totalWidth, segmentHeight),
        )

        // 绘制各个段落
        var currentX = 0f
        segments.forEachIndexed { index, segment ->
            val segmentWidth = totalWidth * segment.coerceIn(0f, 1f)
            val color = colors.getOrElse(index % colors.size) { colors.first() }

            if (segmentWidth > 0f) {
                drawRect(
                    color = color,
                    topLeft = Offset(currentX, 0f),
                    size = Size(segmentWidth, segmentHeight),
                )
            }
            currentX += segmentWidth
        }
    }
}

@Composable
fun UnifyAnimatedProgress(
    targetProgress: Float,
    modifier: Modifier = Modifier,
    animationDuration: Int = 1000,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress.coerceIn(0f, 1f),
        animationSpec =
            tween(
                durationMillis = animationDuration,
                easing = FastOutSlowInEasing,
            ),
        label = "animatedProgress",
    )

    UnifyProgress(
        progress = animatedProgress,
        modifier = modifier,
        color = color,
        trackColor = trackColor,
    )
}
