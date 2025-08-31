package com.unify.ui.components.content

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unify.ui.LocalUnifyTheme
import com.unify.ui.components.foundation.*
import kotlinx.coroutines.delay
import kotlin.math.*

/**
 * Unify 基础内容组件
 * 对应微信小程序的 rich-text、progress、animation-view 等内容组件
 */

/**
 * 富文本节点类型
 */
sealed class UnifyRichTextNode {
    data class TextNode(
        val text: String,
        val style: UnifyRichTextStyle? = null
    ) : UnifyRichTextNode()
    
    data class ElementNode(
        val name: String,
        val attrs: Map<String, String> = emptyMap(),
        val children: List<UnifyRichTextNode> = emptyList(),
        val style: UnifyRichTextStyle? = null
    ) : UnifyRichTextNode()
}

/**
 * 富文本样式
 */
data class UnifyRichTextStyle(
    val color: Color? = null,
    val backgroundColor: Color? = null,
    val fontSize: androidx.compose.ui.unit.TextUnit? = null,
    val fontWeight: FontWeight? = null,
    val fontStyle: FontStyle? = null,
    val textDecoration: TextDecoration? = null,
    val textAlign: TextAlign? = null,
    val lineHeight: androidx.compose.ui.unit.TextUnit? = null,
    val padding: PaddingValues? = null,
    val margin: PaddingValues? = null
)

/**
 * 富文本组件
 */
@Composable
fun UnifyRichText(
    nodes: List<UnifyRichTextNode>,
    modifier: Modifier = Modifier,
    selectable: Boolean = false,
    userSelect: Boolean = true,
    space: String = "ensp", // ensp, emsp, nbsp
    onLinkClick: ((url: String) -> Unit)? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    
    val annotatedString = remember(nodes) {
        buildAnnotatedString {
            nodes.forEach { node ->
                appendRichTextNode(node, theme, onLinkClick)
            }
        }
    }
    
    Box(
        modifier = modifier.semantics {
            contentDescription?.let { 
                this.contentDescription = it 
            }
        }
    ) {
        if (selectable) {
            SelectionContainer {
                if (onLinkClick != null) {
                    ClickableText(
                        text = annotatedString,
                        onClick = { offset ->
                            annotatedString.getStringAnnotations(
                                tag = "URL",
                                start = offset,
                                end = offset
                            ).firstOrNull()?.let { annotation ->
                                onLinkClick(annotation.item)
                            }
                        }
                    )
                } else {
                    Text(text = annotatedString)
                }
            }
        } else {
            if (onLinkClick != null) {
                ClickableText(
                    text = annotatedString,
                    onClick = { offset ->
                        annotatedString.getStringAnnotations(
                            tag = "URL",
                            start = offset,
                            end = offset
                        ).firstOrNull()?.let { annotation ->
                            onLinkClick(annotation.item)
                        }
                    }
                )
            } else {
                Text(text = annotatedString)
            }
        }
    }
}

/**
 * 构建富文本节点
 */
private fun AnnotatedString.Builder.appendRichTextNode(
    node: UnifyRichTextNode,
    theme: UnifyTheme,
    onLinkClick: ((String) -> Unit)?
) {
    when (node) {
        is UnifyRichTextNode.TextNode -> {
            val style = node.style?.let { styleToSpanStyle(it, theme) }
            if (style != null) {
                withStyle(style) {
                    append(node.text)
                }
            } else {
                append(node.text)
            }
        }
        is UnifyRichTextNode.ElementNode -> {
            when (node.name.lowercase()) {
                "br" -> append("\n")
                "p" -> {
                    append("\n")
                    node.children.forEach { child ->
                        appendRichTextNode(child, theme, onLinkClick)
                    }
                    append("\n")
                }
                "strong", "b" -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        node.children.forEach { child ->
                            appendRichTextNode(child, theme, onLinkClick)
                        }
                    }
                }
                "em", "i" -> {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        node.children.forEach { child ->
                            appendRichTextNode(child, theme, onLinkClick)
                        }
                    }
                }
                "u" -> {
                    withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                        node.children.forEach { child ->
                            appendRichTextNode(child, theme, onLinkClick)
                        }
                    }
                }
                "s", "del" -> {
                    withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                        node.children.forEach { child ->
                            appendRichTextNode(child, theme, onLinkClick)
                        }
                    }
                }
                "a" -> {
                    val href = node.attrs["href"]
                    if (href != null && onLinkClick != null) {
                        val start = length
                        withStyle(
                            SpanStyle(
                                color = theme.colors.primary,
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            node.children.forEach { child ->
                                appendRichTextNode(child, theme, onLinkClick)
                            }
                        }
                        addStringAnnotation(
                            tag = "URL",
                            annotation = href,
                            start = start,
                            end = length
                        )
                    } else {
                        node.children.forEach { child ->
                            appendRichTextNode(child, theme, onLinkClick)
                        }
                    }
                }
                "h1", "h2", "h3", "h4", "h5", "h6" -> {
                    val fontSize = when (node.name) {
                        "h1" -> 24.sp
                        "h2" -> 20.sp
                        "h3" -> 18.sp
                        "h4" -> 16.sp
                        "h5" -> 14.sp
                        "h6" -> 12.sp
                        else -> 16.sp
                    }
                    withStyle(
                        SpanStyle(
                            fontSize = fontSize,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        node.children.forEach { child ->
                            appendRichTextNode(child, theme, onLinkClick)
                        }
                    }
                }
                else -> {
                    val style = node.style?.let { styleToSpanStyle(it, theme) }
                    if (style != null) {
                        withStyle(style) {
                            node.children.forEach { child ->
                                appendRichTextNode(child, theme, onLinkClick)
                            }
                        }
                    } else {
                        node.children.forEach { child ->
                            appendRichTextNode(child, theme, onLinkClick)
                        }
                    }
                }
            }
        }
    }
}

/**
 * 样式转换
 */
private fun styleToSpanStyle(style: UnifyRichTextStyle, theme: UnifyTheme): SpanStyle {
    return SpanStyle(
        color = style.color ?: Color.Unspecified,
        fontSize = style.fontSize ?: androidx.compose.ui.unit.TextUnit.Unspecified,
        fontWeight = style.fontWeight,
        fontStyle = style.fontStyle,
        textDecoration = style.textDecoration,
        background = style.backgroundColor ?: Color.Unspecified
    )
}

/**
 * 进度条变体
 */
enum class UnifyProgressVariant {
    LINEAR,         // 线性进度条
    CIRCULAR,       // 圆形进度条
    RING,           // 环形进度条
    STEP,           // 步骤进度条
    RADIAL,         // 径向进度条
    CUSTOM          // 自定义进度条
}

/**
 * 进度条配置
 */
data class UnifyProgressConfig(
    val variant: UnifyProgressVariant = UnifyProgressVariant.LINEAR,
    val showInfo: Boolean = false,
    val borderRadius: Dp = 0.dp,
    val fontSize: androidx.compose.ui.unit.TextUnit = 16.sp,
    val strokeWidth: Dp = 6.dp,
    val activeColor: Color? = null,
    val backgroundColor: Color? = null,
    val active: Boolean = false,
    val activeMode: String = "backwards", // backwards, forwards
    val duration: Long = 30000L,
    val bindActiveEnd: (() -> Unit)? = null
)

/**
 * 增强进度条组件
 */
@Composable
fun UnifyProgressBar(
    percent: Float,
    modifier: Modifier = Modifier,
    config: UnifyProgressConfig = UnifyProgressConfig(),
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    val progress = percent.coerceIn(0f, 100f) / 100f
    
    val activeColor = config.activeColor ?: theme.colors.primary
    val backgroundColor = config.backgroundColor ?: theme.colors.surfaceVariant
    
    // 动画进度
    val animatedProgress by animateFloatAsState(
        targetValue = if (config.active) progress else progress,
        animationSpec = if (config.active) {
            tween(
                durationMillis = config.duration.toInt(),
                easing = LinearEasing
            )
        } else {
            snap()
        },
        finishedListener = {
            config.bindActiveEnd?.invoke()
        },
        label = "progress_animation"
    )
    
    when (config.variant) {
        UnifyProgressVariant.LINEAR -> {
            UnifyLinearProgress(
                progress = animatedProgress,
                modifier = modifier,
                config = config,
                activeColor = activeColor,
                backgroundColor = backgroundColor,
                contentDescription = contentDescription
            )
        }
        UnifyProgressVariant.CIRCULAR -> {
            UnifyCircularProgress(
                progress = animatedProgress,
                modifier = modifier,
                config = config,
                activeColor = activeColor,
                backgroundColor = backgroundColor,
                contentDescription = contentDescription
            )
        }
        UnifyProgressVariant.RING -> {
            UnifyRingProgress(
                progress = animatedProgress,
                modifier = modifier,
                config = config,
                activeColor = activeColor,
                backgroundColor = backgroundColor,
                contentDescription = contentDescription
            )
        }
        UnifyProgressVariant.STEP -> {
            UnifyStepProgress(
                progress = animatedProgress,
                modifier = modifier,
                config = config,
                activeColor = activeColor,
                backgroundColor = backgroundColor,
                contentDescription = contentDescription
            )
        }
        UnifyProgressVariant.RADIAL -> {
            UnifyRadialProgress(
                progress = animatedProgress,
                modifier = modifier,
                config = config,
                activeColor = activeColor,
                backgroundColor = backgroundColor,
                contentDescription = contentDescription
            )
        }
        UnifyProgressVariant.CUSTOM -> {
            UnifyCustomProgress(
                progress = animatedProgress,
                modifier = modifier,
                config = config,
                activeColor = activeColor,
                backgroundColor = backgroundColor,
                contentDescription = contentDescription
            )
        }
    }
}

/**
 * 线性进度条
 */
@Composable
private fun UnifyLinearProgress(
    progress: Float,
    modifier: Modifier,
    config: UnifyProgressConfig,
    activeColor: Color,
    backgroundColor: Color,
    contentDescription: String?
) {
    Column(
        modifier = modifier.semantics {
            contentDescription?.let { 
                this.contentDescription = it 
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(config.strokeWidth)
                .clip(RoundedCornerShape(config.borderRadius))
                .background(backgroundColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(activeColor)
            )
        }
        
        if (config.showInfo) {
            Spacer(modifier = Modifier.height(4.dp))
            UnifyText(
                text = "${(progress * 100).toInt()}%",
                variant = UnifyTextVariant.CAPTION,
                fontSize = config.fontSize,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * 圆形进度条
 */
@Composable
private fun UnifyCircularProgress(
    progress: Float,
    modifier: Modifier,
    config: UnifyProgressConfig,
    activeColor: Color,
    backgroundColor: Color,
    contentDescription: String?
) {
    Box(
        modifier = modifier
            .size(80.dp)
            .semantics {
                contentDescription?.let { 
                    this.contentDescription = it 
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = config.strokeWidth.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val center = Offset(size.width / 2, size.height / 2)
            
            // 背景圆
            drawCircle(
                color = backgroundColor,
                radius = radius,
                center = center,
                style = Stroke(strokeWidth)
            )
            
            // 进度弧
            drawArc(
                color = activeColor,
                startAngle = -90f,
                sweepAngle = progress * 360f,
                useCenter = false,
                topLeft = Offset(
                    center.x - radius,
                    center.y - radius
                ),
                size = Size(radius * 2, radius * 2),
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
        }
        
        if (config.showInfo) {
            UnifyText(
                text = "${(progress * 100).toInt()}%",
                variant = UnifyTextVariant.CAPTION,
                fontSize = config.fontSize
            )
        }
    }
}

/**
 * 环形进度条
 */
@Composable
private fun UnifyRingProgress(
    progress: Float,
    modifier: Modifier,
    config: UnifyProgressConfig,
    activeColor: Color,
    backgroundColor: Color,
    contentDescription: String?
) {
    Box(
        modifier = modifier
            .size(100.dp)
            .semantics {
                contentDescription?.let { 
                    this.contentDescription = it 
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = config.strokeWidth.toPx()
            val outerRadius = size.minDimension / 2
            val innerRadius = outerRadius - strokeWidth
            val center = Offset(size.width / 2, size.height / 2)
            
            // 背景环
            drawCircle(
                color = backgroundColor,
                radius = outerRadius,
                center = center
            )
            
            drawCircle(
                color = Color.White,
                radius = innerRadius,
                center = center
            )
            
            // 进度环
            if (progress > 0) {
                drawArc(
                    color = activeColor,
                    startAngle = -90f,
                    sweepAngle = progress * 360f,
                    useCenter = true,
                    topLeft = Offset(
                        center.x - outerRadius,
                        center.y - outerRadius
                    ),
                    size = Size(outerRadius * 2, outerRadius * 2)
                )
                
                drawCircle(
                    color = Color.White,
                    radius = innerRadius,
                    center = center
                )
            }
        }
        
        if (config.showInfo) {
            UnifyText(
                text = "${(progress * 100).toInt()}%",
                variant = UnifyTextVariant.BODY_MEDIUM,
                fontSize = config.fontSize
            )
        }
    }
}

/**
 * 步骤进度条
 */
@Composable
private fun UnifyStepProgress(
    progress: Float,
    modifier: Modifier,
    config: UnifyProgressConfig,
    activeColor: Color,
    backgroundColor: Color,
    contentDescription: String?
) {
    val steps = 5 // 默认5步
    val currentStep = (progress * steps).toInt()
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription?.let { 
                    this.contentDescription = it 
                }
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(steps) { index ->
            val isActive = index < currentStep
            val isCompleted = index < currentStep
            
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = if (isCompleted) activeColor else backgroundColor,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    UnifyIcon(
                        icon = Icons.Default.Check,
                        size = UnifyIconSize.SMALL,
                        tint = Color.White
                    )
                } else {
                    UnifyText(
                        text = "${index + 1}",
                        variant = UnifyTextVariant.CAPTION,
                        color = if (isActive) Color.White else Color.Gray
                    )
                }
            }
            
            if (index < steps - 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(
                            if (isCompleted) activeColor else backgroundColor
                        )
                )
            }
        }
    }
}

/**
 * 径向进度条
 */
@Composable
private fun UnifyRadialProgress(
    progress: Float,
    modifier: Modifier,
    config: UnifyProgressConfig,
    activeColor: Color,
    backgroundColor: Color,
    contentDescription: String?
) {
    Box(
        modifier = modifier
            .size(120.dp)
            .semantics {
                contentDescription?.let { 
                    this.contentDescription = it 
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val maxRadius = size.minDimension / 2
            val currentRadius = maxRadius * progress
            
            // 背景圆
            drawCircle(
                color = backgroundColor,
                radius = maxRadius,
                center = center
            )
            
            // 进度圆
            if (progress > 0) {
                drawCircle(
                    color = activeColor,
                    radius = currentRadius,
                    center = center
                )
            }
        }
        
        if (config.showInfo) {
            UnifyText(
                text = "${(progress * 100).toInt()}%",
                variant = UnifyTextVariant.BODY_MEDIUM,
                fontSize = config.fontSize,
                color = if (progress > 0.5f) Color.White else Color.Black
            )
        }
    }
}

/**
 * 自定义进度条
 */
@Composable
private fun UnifyCustomProgress(
    progress: Float,
    modifier: Modifier,
    config: UnifyProgressConfig,
    activeColor: Color,
    backgroundColor: Color,
    contentDescription: String?
) {
    // 自定义样式的进度条，可以根据需要实现各种创意效果
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(20.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .semantics {
                contentDescription?.let { 
                    this.contentDescription = it 
                }
            }
    ) {
        // 渐变进度条
        Canvas(modifier = Modifier.fillMaxSize()) {
            val progressWidth = size.width * progress
            
            if (progress > 0) {
                val gradient = Brush.horizontalGradient(
                    colors = listOf(
                        activeColor.copy(alpha = 0.6f),
                        activeColor,
                        activeColor.copy(alpha = 0.8f)
                    ),
                    startX = 0f,
                    endX = progressWidth
                )
                
                drawRect(
                    brush = gradient,
                    size = Size(progressWidth, size.height)
                )
            }
        }
        
        if (config.showInfo) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                UnifyText(
                    text = "${(progress * 100).toInt()}%",
                    variant = UnifyTextVariant.CAPTION,
                    fontSize = config.fontSize,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * 动画视图组件
 */
@Composable
fun UnifyAnimationView(
    path: String,
    modifier: Modifier = Modifier,
    autoplay: Boolean = true,
    loop: Boolean = false,
    action: String = "play", // play, pause, stop
    hidden: Boolean = false,
    onEnded: (() -> Unit)? = null,
    onFrame: ((progress: Float) -> Unit)? = null,
    contentDescription: String? = null
) {
    var isPlaying by remember { mutableStateOf(autoplay) }
    var currentFrame by remember { mutableStateOf(0f) }
    
    // 动画控制
    val infiniteTransition = rememberInfiniteTransition(label = "animation_view")
    val animatedProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = if (loop) RepeatMode.Restart else RepeatMode.Reverse
        ),
        label = "animation_progress"
    )
    
    LaunchedEffect(action) {
        when (action) {
            "play" -> isPlaying = true
            "pause" -> isPlaying = false
            "stop" -> {
                isPlaying = false
                currentFrame = 0f
            }
        }
    }
    
    LaunchedEffect(animatedProgress) {
        if (isPlaying) {
            currentFrame = animatedProgress
            onFrame?.invoke(currentFrame)
            
            if (currentFrame >= 1f && !loop) {
                isPlaying = false
                onEnded?.invoke()
            }
        }
    }
    
    if (!hidden) {
        Box(
            modifier = modifier
                .size(100.dp)
                .semantics {
                    contentDescription?.let { 
                        this.contentDescription = it 
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // 这里应该加载和显示实际的动画文件（如 Lottie 动画）
            // 简化实现，显示一个旋转的图标来模拟动画
            UnifyIcon(
                icon = Icons.Default.Animation,
                size = UnifyIconSize.LARGE,
                modifier = Modifier.graphicsLayer {
                    rotationZ = if (isPlaying) currentFrame * 360f else 0f
                }
            )
        }
    }
}
