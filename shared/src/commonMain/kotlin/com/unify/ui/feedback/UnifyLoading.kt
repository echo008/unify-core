package com.unify.ui.feedback

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlin.math.cos
import kotlin.math.sin

/**
 * Unify加载组件
 * 提供多种加载动画和样式
 */
@Composable
fun UnifyLoading(
    visible: Boolean,
    modifier: Modifier = Modifier,
    type: LoadingType = LoadingType.CIRCULAR,
    size: Dp = 40.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Dp = 4.dp
) {
    if (visible) {
        when (type) {
            LoadingType.CIRCULAR -> CircularLoading(
                modifier = modifier,
                size = size,
                color = color,
                strokeWidth = strokeWidth
            )
            LoadingType.DOTS -> DotsLoading(
                modifier = modifier,
                size = size,
                color = color
            )
            LoadingType.PULSE -> PulseLoading(
                modifier = modifier,
                size = size,
                color = color
            )
            LoadingType.WAVE -> WaveLoading(
                modifier = modifier,
                size = size,
                color = color
            )
            LoadingType.SPINNER -> SpinnerLoading(
                modifier = modifier,
                size = size,
                color = color
            )
        }
    }
}

/**
 * 带文本的加载组件
 */
@Composable
fun UnifyLoadingWithText(
    visible: Boolean,
    text: String,
    modifier: Modifier = Modifier,
    type: LoadingType = LoadingType.CIRCULAR,
    size: Dp = 32.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    arrangement: LoadingTextArrangement = LoadingTextArrangement.VERTICAL
) {
    if (visible) {
        when (arrangement) {
            LoadingTextArrangement.VERTICAL -> {
                Column(
                    modifier = modifier,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    UnifyLoading(
                        visible = true,
                        type = type,
                        size = size,
                        color = color
                    )
                    Text(
                        text = text,
                        color = textColor,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
            LoadingTextArrangement.HORIZONTAL -> {
                Row(
                    modifier = modifier,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    UnifyLoading(
                        visible = true,
                        type = type,
                        size = size,
                        color = color
                    )
                    Text(
                        text = text,
                        color = textColor,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

/**
 * 全屏加载遮罩
 */
@Composable
fun UnifyLoadingOverlay(
    visible: Boolean,
    text: String? = null,
    type: LoadingType = LoadingType.CIRCULAR,
    backgroundColor: Color = Color.Black.copy(alpha = 0.5f),
    contentColor: Color = Color.White
) {
    if (visible) {
        Dialog(
            onDismissRequest = { },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.wrapContentSize(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.9f),
                    tonalElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        UnifyLoading(
                            visible = true,
                            type = type,
                            size = 48.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        text?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 内联加载组件
 */
@Composable
fun UnifyInlineLoading(
    visible: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 16.dp,
    color: Color = MaterialTheme.colorScheme.primary
) {
    if (visible) {
        CircularProgressIndicator(
            modifier = modifier.size(size),
            color = color,
            strokeWidth = 2.dp
        )
    }
}

// 具体加载动画实现

@Composable
private fun CircularLoading(
    modifier: Modifier,
    size: Dp,
    color: Color,
    strokeWidth: Dp
) {
    CircularProgressIndicator(
        modifier = modifier.size(size),
        color = color,
        strokeWidth = strokeWidth
    )
}

@Composable
private fun DotsLoading(
    modifier: Modifier,
    size: Dp,
    color: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val animationDelay = index * 200
            val scale by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = animationDelay),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot_scale_$index"
            )
            
            Box(
                modifier = Modifier
                    .size(size / 4)
                    .clip(CircleShape)
                    .background(color.copy(alpha = scale))
            )
        }
    }
}

@Composable
private fun PulseLoading(
    modifier: Modifier,
    size: Dp,
    color: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )
    
    Box(
        modifier = modifier
            .size(size * scale)
            .clip(CircleShape)
            .background(color.copy(alpha = alpha))
    )
}

@Composable
private fun WaveLoading(
    modifier: Modifier,
    size: Dp,
    color: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        repeat(5) { index ->
            val animationDelay = index * 100
            val height by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, delayMillis = animationDelay),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "wave_height_$index"
            )
            
            Box(
                modifier = Modifier
                    .width(size / 8)
                    .height(size * height)
                    .background(color, RoundedCornerShape(2.dp))
            )
        }
    }
}

@Composable
private fun SpinnerLoading(
    modifier: Modifier,
    size: Dp,
    color: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "spinner")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing)
        ),
        label = "spinner_rotation"
    )
    
    Canvas(
        modifier = modifier
            .size(size)
            .rotate(rotation)
    ) {
        val radius = size.toPx() / 2
        val strokeWidth = 4.dp.toPx()
        
        repeat(8) { index ->
            val angle = index * 45f
            val alpha = (index + 1) / 8f
            
            drawLine(
                color = color.copy(alpha = alpha),
                start = Offset(
                    x = center.x + cos(Math.toRadians(angle.toDouble())).toFloat() * (radius - strokeWidth * 2),
                    y = center.y + sin(Math.toRadians(angle.toDouble())).toFloat() * (radius - strokeWidth * 2)
                ),
                end = Offset(
                    x = center.x + cos(Math.toRadians(angle.toDouble())).toFloat() * radius,
                    y = center.y + sin(Math.toRadians(angle.toDouble())).toFloat() * radius
                ),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * 骨架屏加载
 */
@Composable
fun UnifySkeletonLoading(
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    shimmerColor: Color = Color.Gray.copy(alpha = 0.3f),
    highlightColor: Color = Color.White.copy(alpha = 0.5f)
) {
    if (visible) {
        val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
        val shimmerTranslateAnim by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = LinearEasing)
            ),
            label = "shimmer_translate"
        )
        
        Box(
            modifier = modifier
                .background(shimmerColor)
                .shimmerEffect(shimmerTranslateAnim, highlightColor)
        )
    }
}

// 扩展函数
private fun Modifier.shimmerEffect(
    translateAnim: Float,
    highlightColor: Color
): Modifier = this.then(
    Modifier.background(
        brush = androidx.compose.ui.graphics.Brush.linearGradient(
            colors = listOf(
                Color.Transparent,
                highlightColor,
                Color.Transparent
            ),
            start = Offset(translateAnim - 300f, 0f),
            end = Offset(translateAnim, 0f)
        )
    )
)

// 枚举定义

enum class LoadingType {
    CIRCULAR,
    DOTS,
    PULSE,
    WAVE,
    SPINNER
}

enum class LoadingTextArrangement {
    VERTICAL,
    HORIZONTAL
}
