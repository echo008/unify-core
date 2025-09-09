package com.unify.platform.canvas

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.*

/**
 * Canvas平台特定组件
 * 提供画布绘制和图形处理功能
 */

/**
 * 绘图画布组件
 */
@Composable
fun UnifyDrawingCanvas(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    onPathDrawn: ((Path) -> Unit)? = null,
    brushColor: Color = Color.Black,
    brushSize: Float = 5f,
    enabled: Boolean = true,
) {
    var currentPath by remember { mutableStateOf(Path()) }
    var paths by remember { mutableStateOf(listOf<DrawingPath>()) }
    var isDrawing by remember { mutableStateOf(false) }

    Canvas(
        modifier =
            modifier
                .fillMaxSize()
                .background(backgroundColor)
                .pointerInput(enabled) {
                    if (enabled) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                isDrawing = true
                                currentPath =
                                    Path().apply {
                                        moveTo(offset.x, offset.y)
                                    }
                            },
                            onDragEnd = {
                                isDrawing = false
                                if (currentPath.isEmpty.not()) {
                                    paths = paths +
                                        DrawingPath(
                                            path = Path().apply { addPath(currentPath) },
                                            color = brushColor,
                                            strokeWidth = brushSize,
                                        )
                                    onPathDrawn?.invoke(currentPath)
                                    currentPath = Path()
                                }
                            },
                            onDrag = { _, dragAmount ->
                                if (isDrawing) {
                                    currentPath.relativeLineTo(dragAmount.x, dragAmount.y)
                                }
                            },
                        )
                    }
                },
    ) {
        // 绘制已完成的路径
        paths.forEach { drawingPath ->
            drawPath(
                path = drawingPath.path,
                color = drawingPath.color,
                style =
                    Stroke(
                        width = drawingPath.strokeWidth,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                    ),
            )
        }

        // 绘制当前路径
        if (isDrawing && currentPath.isEmpty.not()) {
            drawPath(
                path = currentPath,
                color = brushColor,
                style =
                    Stroke(
                        width = brushSize,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                    ),
            )
        }
    }
}

/**
 * 图表画布组件
 */
@Composable
fun UnifyChartCanvas(
    data: List<ChartDataPoint>,
    chartType: ChartType = ChartType.LINE,
    modifier: Modifier = Modifier,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    showGrid: Boolean = true,
    showLabels: Boolean = true,
    animated: Boolean = true,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (animated) 1f else 1f,
        animationSpec = androidx.compose.animation.core.tween(1000),
        label = "chart_animation",
    )

    Canvas(
        modifier =
            modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(16.dp),
    ) {
        if (data.isNotEmpty()) {
            when (chartType) {
                ChartType.LINE -> drawLineChart(data, primaryColor, showGrid, animatedProgress)
                ChartType.BAR -> drawBarChart(data, primaryColor, showGrid, animatedProgress)
                ChartType.PIE -> drawPieChart(data, primaryColor, animatedProgress)
                ChartType.AREA -> drawAreaChart(data, primaryColor, showGrid, animatedProgress)
            }
        }
    }
}

/**
 * 形状绘制画布
 */
@Composable
fun UnifyShapeCanvas(
    shapes: List<CanvasShape>,
    onShapeSelected: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    selectionColor: Color = MaterialTheme.colorScheme.primary,
) {
    var selectedShapeId by remember { mutableStateOf<String?>(null) }

    Canvas(
        modifier =
            modifier
                .fillMaxSize()
                .background(backgroundColor)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            // 检测点击的形状
                            val clickedShape =
                                shapes.find { shape ->
                                    isPointInShape(offset, shape)
                                }
                            selectedShapeId = clickedShape?.id
                            onShapeSelected?.invoke(clickedShape?.id ?: "")
                        },
                    ) { _, _ -> }
                },
    ) {
        shapes.forEach { shape ->
            val isSelected = shape.id == selectedShapeId
            drawShape(shape, isSelected, selectionColor)
        }
    }
}

/**
 * 动画画布组件
 */
@Composable
fun UnifyAnimationCanvas(
    animations: List<CanvasAnimation>,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Black,
    onAnimationComplete: (() -> Unit)? = null,
) {
    var animationTime by remember { mutableStateOf(0f) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isPlaying) {
                animationTime += 16f // 约60fps
                kotlinx.coroutines.delay(16)

                // 检查动画是否完成
                val maxDuration = animations.maxOfOrNull { it.duration } ?: 0f
                if (animationTime >= maxDuration) {
                    onAnimationComplete?.invoke()
                    break
                }
            }
        }
    }

    Canvas(
        modifier =
            modifier
                .fillMaxSize()
                .background(backgroundColor),
    ) {
        animations.forEach { animation ->
            if (animationTime >= animation.startTime && animationTime <= animation.startTime + animation.duration) {
                val progress = (animationTime - animation.startTime) / animation.duration
                drawAnimation(animation, progress)
            }
        }
    }
}

/**
 * 画布工具栏
 */
@Composable
fun UnifyCanvasToolbar(
    selectedTool: CanvasTool,
    onToolSelected: (CanvasTool) -> Unit,
    brushColor: Color,
    onColorSelected: (Color) -> Unit,
    brushSize: Float,
    onBrushSizeChanged: (Float) -> Unit,
    onClear: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 工具选择
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CanvasTool.values().forEach { tool ->
                    FilterChip(
                        selected = selectedTool == tool,
                        onClick = { onToolSelected(tool) },
                        label = { Text(tool.displayName) },
                        leadingIcon = {
                            Icon(
                                imageVector = getToolIcon(tool),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                        },
                    )
                }
            }

            // 颜色选择
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "颜色:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )

                val colors =
                    listOf(
                        Color.Black,
                        Color.Red,
                        Color.Green,
                        Color.Blue,
                        Color.Yellow,
                        Color.Magenta,
                        Color.Cyan,
                        Color.Gray,
                    )

                colors.forEach { color ->
                    Box(
                        modifier =
                            Modifier
                                .size(32.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(color)
                                .then(
                                    if (brushColor == color) {
                                        Modifier.border(
                                            2.dp,
                                            MaterialTheme.colorScheme.primary,
                                            androidx.compose.foundation.shape.CircleShape,
                                        )
                                    } else {
                                        Modifier
                                    },
                                )
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = { onColorSelected(color) },
                                    ) { _, _ -> }
                                },
                    )
                }
            }

            // 画笔大小
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "画笔大小: ${brushSize.toInt()}px",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )

                Slider(
                    value = brushSize,
                    onValueChange = onBrushSizeChanged,
                    valueRange = 1f..50f,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onUndo,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.Undo, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("撤销")
                }

                OutlinedButton(
                    onClick = onRedo,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.Redo, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("重做")
                }

                Button(
                    onClick = onClear,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                        ),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.Clear, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("清空")
                }
            }
        }
    }
}

// 绘制函数

private fun DrawScope.drawLineChart(
    data: List<ChartDataPoint>,
    color: Color,
    showGrid: Boolean,
    progress: Float,
) {
    val maxValue = data.maxOfOrNull { it.value } ?: 1f
    val minValue = data.minOfOrNull { it.value } ?: 0f
    val valueRange = maxValue - minValue

    val stepX = size.width / (data.size - 1)
    val stepY = size.height

    // 绘制网格
    if (showGrid) {
        val gridColor = Color.Gray.copy(alpha = 0.3f)
        for (i in 0..10) {
            val y = size.height * i / 10
            drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
        }
        for (i in 0 until data.size) {
            val x = stepX * i
            drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1.dp.toPx())
        }
    }

    // 绘制线条
    val path = Path()
    data.forEachIndexed { index, point ->
        val x = stepX * index
        val y = stepY - (point.value - minValue) / valueRange * stepY

        if (index == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }

    // 应用动画进度
    val animatedPath = Path()
    val pathMeasure = PathMeasure()
    pathMeasure.setPath(path, false)
    pathMeasure.getSegment(0f, pathMeasure.length * progress, animatedPath, true)

    drawPath(
        path = animatedPath,
        color = color,
        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
    )
}

private fun DrawScope.drawBarChart(
    data: List<ChartDataPoint>,
    color: Color,
    showGrid: Boolean,
    progress: Float,
) {
    val maxValue = data.maxOfOrNull { it.value } ?: 1f
    val barWidth = size.width / data.size * 0.8f
    val barSpacing = size.width / data.size * 0.2f

    data.forEachIndexed { index, point ->
        val barHeight = (point.value / maxValue) * size.height * progress
        val x = index * (barWidth + barSpacing) + barSpacing / 2
        val y = size.height - barHeight

        drawRect(
            color = color,
            topLeft = Offset(x, y),
            size = Size(barWidth, barHeight),
        )
    }
}

private fun DrawScope.drawPieChart(
    data: List<ChartDataPoint>,
    color: Color,
    progress: Float,
) {
    val total = data.sumOf { it.value.toDouble() }.toFloat()
    val center = Offset(size.width / 2, size.height / 2)
    val radius = minOf(size.width, size.height) / 2 * 0.8f

    var startAngle = -90f
    val colors = generateColors(data.size, color)

    data.forEachIndexed { index, point ->
        val sweepAngle = (point.value / total) * 360f * progress

        drawArc(
            color = colors[index],
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = true,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
        )

        startAngle += sweepAngle
    }
}

private fun DrawScope.drawAreaChart(
    data: List<ChartDataPoint>,
    color: Color,
    showGrid: Boolean,
    progress: Float,
) {
    val maxValue = data.maxOfOrNull { it.value } ?: 1f
    val minValue = data.minOfOrNull { it.value } ?: 0f
    val valueRange = maxValue - minValue

    val stepX = size.width / (data.size - 1)
    val stepY = size.height

    val path = Path()
    path.moveTo(0f, size.height)

    data.forEachIndexed { index, point ->
        val x = stepX * index
        val y = stepY - (point.value - minValue) / valueRange * stepY
        path.lineTo(x, y)
    }

    path.lineTo(size.width, size.height)
    path.close()

    drawPath(
        path = path,
        color = color.copy(alpha = 0.3f),
    )
}

private fun DrawScope.drawShape(
    shape: CanvasShape,
    isSelected: Boolean,
    selectionColor: Color,
) {
    when (shape) {
        is CanvasShape.Rectangle -> {
            drawRect(
                color = shape.color,
                topLeft = shape.position,
                size = shape.size,
            )
            if (isSelected) {
                drawRect(
                    color = selectionColor,
                    topLeft = shape.position,
                    size = shape.size,
                    style = Stroke(width = 2.dp.toPx()),
                )
            }
        }
        is CanvasShape.Circle -> {
            drawCircle(
                color = shape.color,
                radius = shape.radius,
                center = shape.center,
            )
            if (isSelected) {
                drawCircle(
                    color = selectionColor,
                    radius = shape.radius,
                    center = shape.center,
                    style = Stroke(width = 2.dp.toPx()),
                )
            }
        }
        is CanvasShape.Line -> {
            drawLine(
                color = shape.color,
                start = shape.start,
                end = shape.end,
                strokeWidth = shape.strokeWidth,
            )
        }
    }
}

private fun DrawScope.drawAnimation(
    animation: CanvasAnimation,
    progress: Float,
) {
    when (animation) {
        is CanvasAnimation.FadeIn -> {
            val alpha = progress
            drawRect(
                color = animation.color.copy(alpha = alpha),
                topLeft = animation.position,
                size = animation.size,
            )
        }
        is CanvasAnimation.Scale -> {
            val scale = progress
            val scaledSize =
                Size(
                    animation.size.width * scale,
                    animation.size.height * scale,
                )
            val scaledPosition =
                Offset(
                    animation.position.x + (animation.size.width - scaledSize.width) / 2,
                    animation.position.y + (animation.size.height - scaledSize.height) / 2,
                )
            drawRect(
                color = animation.color,
                topLeft = scaledPosition,
                size = scaledSize,
            )
        }
        is CanvasAnimation.Move -> {
            val currentPosition =
                Offset(
                    animation.startPosition.x + (animation.endPosition.x - animation.startPosition.x) * progress,
                    animation.startPosition.y + (animation.endPosition.y - animation.startPosition.y) * progress,
                )
            drawCircle(
                color = animation.color,
                radius = animation.radius,
                center = currentPosition,
            )
        }
    }
}

// 辅助函数

private fun isPointInShape(
    point: Offset,
    shape: CanvasShape,
): Boolean {
    return when (shape) {
        is CanvasShape.Rectangle -> {
            point.x >= shape.position.x &&
                point.x <= shape.position.x + shape.size.width &&
                point.y >= shape.position.y &&
                point.y <= shape.position.y + shape.size.height
        }
        is CanvasShape.Circle -> {
            val distance =
                sqrt(
                    (point.x - shape.center.x).pow(2) +
                        (point.y - shape.center.y).pow(2),
                )
            distance <= shape.radius
        }
        is CanvasShape.Line -> {
            // 简化的线条点击检测
            val distance = distanceFromPointToLine(point, shape.start, shape.end)
            distance <= shape.strokeWidth / 2
        }
    }
}

private fun distanceFromPointToLine(
    point: Offset,
    lineStart: Offset,
    lineEnd: Offset,
): Float {
    val A = point.x - lineStart.x
    val B = point.y - lineStart.y
    val C = lineEnd.x - lineStart.x
    val D = lineEnd.y - lineStart.y

    val dot = A * C + B * D
    val lenSq = C * C + D * D

    if (lenSq == 0f) return sqrt(A * A + B * B)

    val param = dot / lenSq
    val xx: Float
    val yy: Float

    if (param < 0) {
        xx = lineStart.x
        yy = lineStart.y
    } else if (param > 1) {
        xx = lineEnd.x
        yy = lineEnd.y
    } else {
        xx = lineStart.x + param * C
        yy = lineStart.y + param * D
    }

    val dx = point.x - xx
    val dy = point.y - yy
    return sqrt(dx * dx + dy * dy)
}

private fun generateColors(
    count: Int,
    baseColor: Color,
): List<Color> {
    return (0 until count).map { index ->
        val hue = (index * 360f / count) % 360f
        Color.hsv(hue, 0.7f, 0.9f)
    }
}

private fun getToolIcon(tool: CanvasTool): androidx.compose.ui.graphics.vector.ImageVector {
    return when (tool) {
        CanvasTool.BRUSH -> Icons.Default.Brush
        CanvasTool.ERASER -> Icons.Default.Clear
        CanvasTool.LINE -> Icons.Default.Timeline
        CanvasTool.RECTANGLE -> Icons.Default.CropSquare
        CanvasTool.CIRCLE -> Icons.Default.Circle
        CanvasTool.TEXT -> Icons.Default.TextFields
    }
}

// 数据类定义

data class DrawingPath(
    val path: Path,
    val color: Color,
    val strokeWidth: Float,
)

data class ChartDataPoint(
    val label: String,
    val value: Float,
)

enum class ChartType {
    LINE,
    BAR,
    PIE,
    AREA,
}

sealed class CanvasShape {
    abstract val id: String
    abstract val color: Color

    data class Rectangle(
        override val id: String,
        override val color: Color,
        val position: Offset,
        val size: Size,
    ) : CanvasShape()

    data class Circle(
        override val id: String,
        override val color: Color,
        val center: Offset,
        val radius: Float,
    ) : CanvasShape()

    data class Line(
        override val id: String,
        override val color: Color,
        val start: Offset,
        val end: Offset,
        val strokeWidth: Float,
    ) : CanvasShape()
}

sealed class CanvasAnimation {
    abstract val startTime: Float
    abstract val duration: Float
    abstract val color: Color

    data class FadeIn(
        override val startTime: Float,
        override val duration: Float,
        override val color: Color,
        val position: Offset,
        val size: Size,
    ) : CanvasAnimation()

    data class Scale(
        override val startTime: Float,
        override val duration: Float,
        override val color: Color,
        val position: Offset,
        val size: Size,
    ) : CanvasAnimation()

    data class Move(
        override val startTime: Float,
        override val duration: Float,
        override val color: Color,
        val startPosition: Offset,
        val endPosition: Offset,
        val radius: Float,
    ) : CanvasAnimation()
}

enum class CanvasTool(val displayName: String) {
    BRUSH("画笔"),
    ERASER("橡皮擦"),
    LINE("直线"),
    RECTANGLE("矩形"),
    CIRCLE("圆形"),
    TEXT("文本"),
}
