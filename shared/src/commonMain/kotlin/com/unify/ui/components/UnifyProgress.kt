package com.unify.ui.components

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Unify进度组件
 * 提供多种样式的进度指示器和进度条
 */

/**
 * 线性进度条
 */
@Composable
fun UnifyLinearProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    height: dp = 8.dp,
    cornerRadius: dp = 4.dp,
    showLabel: Boolean = false,
    label: String = "${(progress * 100).toInt()}%"
) {
    Column(modifier = modifier) {
        if (showLabel) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(cornerRadius))
                .background(backgroundColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(color, RoundedCornerShape(cornerRadius))
            )
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
    size: dp = 80.dp,
    strokeWidth: dp = 8.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    showPercentage: Boolean = true,
    showLabel: Boolean = false,
    label: String = ""
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val radius = (size.toPx() - strokeWidth.toPx()) / 2
            val center = androidx.compose.ui.geometry.Offset(size.toPx() / 2, size.toPx() / 2)
            
            // 背景圆环
            drawCircle(
                color = backgroundColor,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
            
            // 进度圆弧
            val sweepAngle = 360f * progress.coerceIn(0f, 1f)
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round),
                topLeft = androidx.compose.ui.geometry.Offset(
                    strokeWidth.toPx() / 2,
                    strokeWidth.toPx() / 2
                ),
                size = androidx.compose.ui.geometry.Size(
                    size.toPx() - strokeWidth.toPx(),
                    size.toPx() - strokeWidth.toPx()
                )
            )
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (showPercentage) {
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            if (showLabel && label.isNotEmpty()) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
    completedColor: Color = MaterialTheme.colorScheme.tertiary
) {
    Column(modifier = modifier) {
        // 步骤指示器
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(totalSteps) { index ->
                val stepNumber = index + 1
                val isCompleted = stepNumber < currentStep
                val isActive = stepNumber == currentStep
                val isFuture = stepNumber > currentStep
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 步骤圆圈
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                when {
                                    isCompleted -> completedColor
                                    isActive -> activeColor
                                    else -> inactiveColor
                                },
                                androidx.compose.foundation.shape.CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isCompleted) "✓" else stepNumber.toString(),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // 连接线
                    if (index < totalSteps - 1) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(2.dp)
                                .background(
                                    if (isCompleted) completedColor else inactiveColor
                                )
                        )
                    }
                }
            }
        }
        
        // 步骤标签
        if (stepLabels.isNotEmpty() && stepLabels.size >= totalSteps) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(totalSteps) { index ->
                    Text(
                        text = stepLabels[index],
                        style = MaterialTheme.typography.bodySmall,
                        color = if (index + 1 <= currentStep) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.weight(1f)
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
    size: dp = 120.dp,
    strokeWidth: dp = 12.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    content: @Composable BoxScope.() -> Unit = {}
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val radius = (size.toPx() - strokeWidth.toPx()) / 2
            val center = androidx.compose.ui.geometry.Offset(size.toPx() / 2, size.toPx() / 2)
            
            // 背景圆环
            drawCircle(
                color = backgroundColor,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth.toPx())
            )
            
            // 进度圆弧
            val sweepAngle = 360f * progress.coerceIn(0f, 1f)
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round),
                topLeft = androidx.compose.ui.geometry.Offset(
                    strokeWidth.toPx() / 2,
                    strokeWidth.toPx() / 2
                ),
                size = androidx.compose.ui.geometry.Size(
                    size.toPx() - strokeWidth.toPx(),
                    size.toPx() - strokeWidth.toPx()
                )
            )
        }
        
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
    height: dp = 8.dp,
    cornerRadius: dp = 4.dp,
    showLabels: Boolean = false
) {
    Column(modifier = modifier) {
        if (showLabels) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                segments.forEach { segment ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(segment.color, androidx.compose.foundation.shape.CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = segment.label,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(cornerRadius))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            var currentOffset = 0f
            segments.forEach { segment ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth(segment.progress.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .offset(x = (currentOffset * size.width).dp)
                        .background(segment.color, RoundedCornerShape(cornerRadius))
                )
                currentOffset += segment.progress
            }
        }
    }
}

/**
 * 波浪进度条
 */
@Composable
fun UnifyWaveProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    size: dp = 100.dp,
    waveColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        ),
        label = "waveOffset"
    )
    
    Box(
        modifier = modifier
            .size(size)
            .clip(androidx.compose.foundation.shape.CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val radius = size.toPx() / 2
            val centerX = size.toPx() / 2
            val centerY = size.toPx() / 2
            val waveHeight = radius * 2 * (1 - progress.coerceIn(0f, 1f))
            
            // 绘制波浪
            val path = androidx.compose.ui.graphics.Path()
            val waveWidth = size.toPx()
            val waveAmplitude = 20f
            
            path.moveTo(0f, centerY + waveHeight)
            
            for (x in 0..waveWidth.toInt()) {
                val y = centerY + waveHeight + 
                    waveAmplitude * sin((x / waveWidth * 4 * PI + waveOffset).toDouble()).toFloat()
                path.lineTo(x.toFloat(), y)
            }
            
            path.lineTo(waveWidth, size.toPx())
            path.lineTo(0f, size.toPx())
            path.close()
            
            clipPath(path) {
                drawRect(waveColor)
            }
        }
        
        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

/**
 * 仪表盘进度
 */
@Composable
fun UnifyGaugeProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    size: dp = 120.dp,
    strokeWidth: dp = 12.dp,
    startAngle: Float = 135f,
    sweepAngle: Float = 270f,
    color: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val radius = (size.toPx() - strokeWidth.toPx()) / 2
            val center = androidx.compose.ui.geometry.Offset(size.toPx() / 2, size.toPx() / 2)
            
            // 背景弧
            drawArc(
                color = backgroundColor,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round),
                topLeft = androidx.compose.ui.geometry.Offset(
                    strokeWidth.toPx() / 2,
                    strokeWidth.toPx() / 2
                ),
                size = androidx.compose.ui.geometry.Size(
                    size.toPx() - strokeWidth.toPx(),
                    size.toPx() - strokeWidth.toPx()
                )
            )
            
            // 进度弧
            val progressSweep = sweepAngle * progress.coerceIn(0f, 1f)
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = progressSweep,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round),
                topLeft = androidx.compose.ui.geometry.Offset(
                    strokeWidth.toPx() / 2,
                    strokeWidth.toPx() / 2
                ),
                size = androidx.compose.ui.geometry.Size(
                    size.toPx() - strokeWidth.toPx(),
                    size.toPx() - strokeWidth.toPx()
                )
            )
            
            // 指针
            val angle = (startAngle + progressSweep) * PI / 180
            val pointerLength = radius * 0.8f
            val pointerX = center.x + cos(angle).toFloat() * pointerLength
            val pointerY = center.y + sin(angle).toFloat() * pointerLength
            
            drawLine(
                color = color,
                start = center,
                end = androidx.compose.ui.geometry.Offset(pointerX, pointerY),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )
            
            // 中心点
            drawCircle(
                color = color,
                radius = 8.dp.toPx(),
                center = center
            )
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(y = 20.dp)
        ) {
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// 数据类
data class ProgressSegment(
    val progress: Float,
    val color: Color,
    val label: String = ""
)
