package com.unify.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 线性进度条
 */
@Composable
fun UnifyLinearProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    height: Dp = 8.dp,
    cornerRadius: Dp = 4.dp,
    showPercentage: Boolean = true,
    showLabel: Boolean = false,
    label: String = "",
) {
    Column(modifier = modifier) {
        LinearProgressIndicator(
            progress = progress.coerceIn(0f, 1f),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(height)
                    .clip(RoundedCornerShape(cornerRadius)),
            color = color,
            trackColor = backgroundColor,
        )

        if (showPercentage || showLabel) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                if (showLabel && label.isNotEmpty()) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                if (showPercentage) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

/**
 * 圆形进度条
 */
@Composable
fun UnifyCircularProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    strokeWidth: Dp = 4.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    showPercentage: Boolean = true,
    showLabel: Boolean = false,
    label: String = "",
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            progress = progress.coerceIn(0f, 1f),
            modifier = Modifier.size(size),
            color = color,
            strokeWidth = strokeWidth,
            trackColor = backgroundColor,
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (showPercentage) {
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            if (showLabel && label.isNotEmpty()) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                )
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
    LinearProgressIndicator(
        modifier =
            modifier
                .fillMaxWidth()
                .height(height),
        color = color,
        trackColor = backgroundColor,
    )
}

/**
 * 不确定圆形进度条
 */
@Composable
fun UnifyIndeterminateCircularProgress(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    strokeWidth: Dp = 4.dp,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    CircularProgressIndicator(
        modifier = modifier.size(size),
        color = color,
        strokeWidth = strokeWidth,
    )
}

/**
 * 步骤进度条
 */
@Composable
fun UnifyStepProgress(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    completedColor: Color = MaterialTheme.colorScheme.primary,
    showLabels: Boolean = true,
    labels: List<String> = emptyList(),
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            for (step in 1..totalSteps) {
                val isCompleted = step < currentStep
                val isActive = step == currentStep
                val stepColor =
                    when {
                        isCompleted -> completedColor
                        isActive -> activeColor
                        else -> inactiveColor
                    }

                Box(
                    modifier =
                        Modifier
                            .size(24.dp)
                            .background(stepColor, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isCompleted) {
                        Text(
                            text = "✓",
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    } else {
                        Text(
                            text = step.toString(),
                            color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }

                if (step < totalSteps) {
                    HorizontalDivider(
                        modifier =
                            Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                        color = if (step < currentStep) completedColor else inactiveColor,
                    )
                }
            }
        }

        if (showLabels && labels.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                labels.take(totalSteps).forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

/**
 * 环形进度条
 */
@Composable
fun UnifyRingProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    strokeWidth: Dp = 12.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    content: @Composable BoxScope.() -> Unit = {},
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            progress = progress.coerceIn(0f, 1f),
            modifier = Modifier.size(size),
            color = color,
            strokeWidth = strokeWidth,
            trackColor = backgroundColor,
        )

        content()
    }
}

/**
 * 多段进度条
 */
@Composable
fun UnifyMultiProgress(
    segments: List<ProgressSegment>,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
    cornerRadius: Dp = 4.dp,
    showLabels: Boolean = false,
) {
    Column(modifier = modifier) {
        if (showLabels) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                segments.forEach { segment ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier =
                                Modifier
                                    .size(12.dp)
                                    .background(segment.color, CircleShape),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = segment.label,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(height)
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            var currentOffset = 0f
            segments.forEach { segment ->
                LinearProgressIndicator(
                    progress = segment.progress.coerceIn(0f, 1f),
                    modifier =
                        Modifier
                            .fillMaxWidth(currentOffset + segment.progress.coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(cornerRadius)),
                    color = segment.color,
                    trackColor = Color.Transparent,
                )
                currentOffset += segment.progress
            }
        }
    }
}

/**
 * 波浪进度 - 简化实现
 */
@Composable
fun UnifyWaveProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    waveColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    Box(
        modifier =
            modifier
                .size(size)
                .clip(CircleShape)
                .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            progress = progress.coerceIn(0f, 1f),
            modifier = Modifier.size(size),
            color = waveColor,
            strokeWidth = 8.dp,
            trackColor = Color.Transparent,
        )

        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = waveColor,
        )
    }
}

/**
 * 仪表盘进度 - 简化实现
 */
@Composable
fun UnifyGaugeProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    strokeWidth: Dp = 12.dp,
    startAngle: Float = 135f,
    sweepAngle: Float = 270f,
    color: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            progress = progress.coerceIn(0f, 1f),
            modifier = Modifier.size(size),
            color = color,
            strokeWidth = strokeWidth,
            trackColor = backgroundColor,
        )

        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

/**
 * 进度段数据类
 */
data class ProgressSegment(
    val progress: Float,
    val color: Color,
    val label: String = "",
)
