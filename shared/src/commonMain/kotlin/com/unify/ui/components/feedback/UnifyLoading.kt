package com.unify.ui.components.feedback

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Unify跨平台加载指示器组件
 * 支持所有8大平台的统一加载体验
 */

@Composable
fun UnifyLoading(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Dp = 4.dp,
) {
    CircularProgressIndicator(
        modifier = modifier.size(size),
        color = color,
        strokeWidth = strokeWidth,
    )
}

@Composable
fun UnifyLoadingWithText(
    text: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        UnifyLoading(
            size = size,
            color = color,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun UnifyLinearLoading(
    progress: Float? = null,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    if (progress != null) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = modifier,
            color = color,
            trackColor = trackColor,
        )
    } else {
        LinearProgressIndicator(
            modifier = modifier,
            color = color,
            trackColor = trackColor,
        )
    }
}

@Composable
fun UnifyDotLoading(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    dotSize: Dp = 8.dp,
    animationDuration: Int = 1200,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dotLoading")

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(3) { index ->
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1f,
                animationSpec =
                    infiniteRepeatable(
                        animation =
                            tween(
                                durationMillis = animationDuration / 3,
                                delayMillis = index * (animationDuration / 6),
                                easing = FastOutSlowInEasing,
                            ),
                        repeatMode = RepeatMode.Reverse,
                    ),
                label = "dotScale$index",
            )

            Canvas(
                modifier = Modifier.size(dotSize),
            ) {
                drawCircle(
                    color = color,
                    radius = size.minDimension / 2 * scale,
                    center = center,
                )
            }
        }
    }
}

@Composable
fun UnifyPulseLoading(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    size: Dp = 40.dp,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulseLoading")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "pulseScale",
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "pulseAlpha",
    )

    Canvas(
        modifier = modifier.size(size),
    ) {
        drawCircle(
            color = color.copy(alpha = alpha),
            radius = size.toPx() / 2 * scale,
            center = center,
        )
    }
}

@Composable
fun UnifySpinnerLoading(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    size: Dp = 40.dp,
    strokeWidth: Dp = 4.dp,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "spinnerLoading")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
            ),
        label = "spinnerRotation",
    )

    Canvas(
        modifier =
            modifier
                .size(size)
                .rotate(rotation),
    ) {
        val strokeWidthPx = strokeWidth.toPx()
        val radius = (size.toPx() - strokeWidthPx) / 2

        drawArc(
            color = color,
            startAngle = 0f,
            sweepAngle = 270f,
            useCenter = false,
            style =
                androidx.compose.ui.graphics.drawscope.Stroke(
                    width = strokeWidthPx,
                    cap = StrokeCap.Round,
                ),
            topLeft = Offset(strokeWidthPx / 2, strokeWidthPx / 2),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
        )
    }
}

@Composable
fun UnifyLoadingOverlay(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    loadingText: String = "加载中...",
    backgroundColor: Color = Color.Black.copy(alpha = 0.5f),
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier) {
        content()

        if (isLoading) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center),
                contentAlignment = Alignment.Center,
            ) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                ) {
                    UnifyLoadingWithText(
                        text = loadingText,
                        modifier = Modifier.padding(32.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun UnifySkeletonLoading(
    modifier: Modifier = Modifier,
    isLoading: Boolean = true,
    content: @Composable () -> Unit,
) {
    if (isLoading) {
        val infiniteTransition = rememberInfiniteTransition(label = "skeletonLoading")

        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 0.7f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(1000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "skeletonAlpha",
        )

        Box(
            modifier = modifier,
        ) {
            content()
            Box(
                modifier =
                    Modifier
                        .fillMaxSize(),
            ) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha),
                        ),
                ) {}
            }
        }
    } else {
        content()
    }
}
