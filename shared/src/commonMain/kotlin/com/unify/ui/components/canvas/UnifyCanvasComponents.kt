package com.unify.ui.components.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.*

/**
 * Unify画布组件库
 * 提供跨平台的2D绘图和可视化组件
 */

/**
 * 统一绘图画布组件
 */
@Composable
fun UnifyDrawingCanvas(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    onDraw: DrawScope.() -> Unit,
) {
    Canvas(
        modifier =
            modifier
                .fillMaxSize()
                .background(backgroundColor),
    ) {
        onDraw()
    }
}

/**
 * 统一图表画布组件
 */
@Composable
fun UnifyChartCanvas(
    data: List<Float>,
    modifier: Modifier = Modifier,
    chartType: ChartType = ChartType.LINE,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    showGrid: Boolean = true,
    showLabels: Boolean = true,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            if (showLabels) {
                Text(
                    text = "数据图表",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Canvas(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(200.dp),
            ) {
                when (chartType) {
                    ChartType.LINE -> drawLineChart(data, primaryColor, showGrid)
                    ChartType.BAR -> drawBarChart(data, primaryColor, showGrid)
                    ChartType.PIE -> drawPieChart(data, primaryColor)
                    ChartType.AREA -> drawAreaChart(data, primaryColor, showGrid)
                }
            }
        }
    }
}

/**
 * 统一进度环形图组件
 */
@Composable
fun UnifyCircularProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    strokeWidth: Float = 12f,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
    showPercentage: Boolean = true,
) {
    Box(
        modifier = modifier.size(120.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize(),
        ) {
            val canvasSize = size.minDimension
            val radius = (canvasSize - strokeWidth) / 2
            val center = Offset(size.width / 2, size.height / 2)

            // 绘制背景圆环
            drawCircle(
                color = backgroundColor,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth),
            )

            // 绘制进度圆环
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = progress * 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft =
                    Offset(
                        center.x - radius,
                        center.y - radius,
                    ),
                size = Size(radius * 2, radius * 2),
            )
        }

        if (showPercentage) {
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = progressColor,
            )
        }
    }
}

/**
 * 统一波形图组件
 */
@Composable
fun UnifyWaveform(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
    waveColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    animated: Boolean = true,
) {
    var animationProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(animated) {
        if (animated) {
            // 简单的动画效果
            animationProgress = 1f
        }
    }

    Canvas(
        modifier =
            modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(backgroundColor),
    ) {
        drawWaveform(amplitudes, waveColor, animationProgress)
    }
}

/**
 * 统一签名画板组件
 */
@Composable
fun UnifySignaturePad(
    modifier: Modifier = Modifier,
    strokeColor: Color = Color.Black,
    strokeWidth: Float = 4f,
    backgroundColor: Color = Color.White,
    onSignatureChange: (List<Offset>) -> Unit = {},
) {
    var paths by remember { mutableStateOf(listOf<Offset>()) }

    Canvas(
        modifier =
            modifier
                .fillMaxSize()
                .background(backgroundColor)
                .clip(RoundedCornerShape(8.dp)),
    ) {
        // 绘制签名路径
        if (paths.isNotEmpty()) {
            val path = Path()
            paths.forEachIndexed { index, point ->
                if (index == 0) {
                    path.moveTo(point.x, point.y)
                } else {
                    path.lineTo(point.x, point.y)
                }
            }

            drawPath(
                path = path,
                color = strokeColor,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
        }
    }
}

/**
 * 统一几何图形组件
 */
@Composable
fun UnifyGeometryShapes(
    modifier: Modifier = Modifier,
    shapes: List<GeometryShape> = getDefaultShapes(),
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
) {
    Canvas(
        modifier =
            modifier
                .fillMaxSize()
                .background(backgroundColor),
    ) {
        shapes.forEach { shape ->
            drawGeometryShape(shape)
        }
    }
}

/**
 * 统一数据可视化仪表盘
 */
@Composable
fun UnifyDataVisualizationDashboard(
    data: DashboardData,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp),
    ) {
        item {
            Text(
                text = "数据可视化仪表盘",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                UnifyCircularProgress(
                    progress = data.performanceScore,
                    modifier = Modifier.weight(1f),
                )
                UnifyCircularProgress(
                    progress = data.qualityScore,
                    modifier = Modifier.weight(1f),
                    progressColor = Color.Green,
                )
            }
        }

        item {
            UnifyChartCanvas(
                data = data.trendData,
                chartType = ChartType.LINE,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        item {
            UnifyChartCanvas(
                data = data.categoryData,
                chartType = ChartType.BAR,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        item {
            UnifyWaveform(
                amplitudes = data.waveData,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// 绘图辅助函数
private fun DrawScope.drawLineChart(
    data: List<Float>,
    color: Color,
    showGrid: Boolean,
) {
    if (data.isEmpty()) return

    val maxValue = data.maxOrNull() ?: 1f
    val minValue = data.minOrNull() ?: 0f
    val range = maxValue - minValue

    if (showGrid) {
        drawGrid()
    }

    val path = Path()
    data.forEachIndexed { index, value ->
        val x = (index.toFloat() / (data.size - 1)) * size.width
        val y = size.height - ((value - minValue) / range) * size.height

        if (index == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(width = 3f, cap = StrokeCap.Round),
    )
}

private fun DrawScope.drawBarChart(
    data: List<Float>,
    color: Color,
    showGrid: Boolean,
) {
    if (data.isEmpty()) return

    val maxValue = data.maxOrNull() ?: 1f
    val barWidth = size.width / data.size * 0.8f
    val barSpacing = size.width / data.size * 0.2f

    if (showGrid) {
        drawGrid()
    }

    data.forEachIndexed { index, value ->
        val x = index * (barWidth + barSpacing) + barSpacing / 2
        val height = (value / maxValue) * size.height
        val y = size.height - height

        drawRect(
            color = color,
            topLeft = Offset(x, y),
            size = Size(barWidth, height),
        )
    }
}

private fun DrawScope.drawPieChart(
    data: List<Float>,
    baseColor: Color,
) {
    if (data.isEmpty()) return

    val total = data.sum()
    val center = Offset(size.width / 2, size.height / 2)
    val radius = minOf(size.width, size.height) / 2 * 0.8f

    var startAngle = 0f

    data.forEachIndexed { index, value ->
        val sweepAngle = (value / total) * 360f
        val color =
            baseColor.copy(
                red = (baseColor.red + index * 0.1f).coerceIn(0f, 1f),
                green = (baseColor.green + index * 0.05f).coerceIn(0f, 1f),
                blue = (baseColor.blue + index * 0.15f).coerceIn(0f, 1f),
            )

        drawArc(
            color = color,
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
    data: List<Float>,
    color: Color,
    showGrid: Boolean,
) {
    if (data.isEmpty()) return

    val maxValue = data.maxOrNull() ?: 1f
    val minValue = data.minOrNull() ?: 0f
    val range = maxValue - minValue

    if (showGrid) {
        drawGrid()
    }

    val path = Path()
    path.moveTo(0f, size.height)

    data.forEachIndexed { index, value ->
        val x = (index.toFloat() / (data.size - 1)) * size.width
        val y = size.height - ((value - minValue) / range) * size.height
        path.lineTo(x, y)
    }

    path.lineTo(size.width, size.height)
    path.close()

    drawPath(
        path = path,
        color = color.copy(alpha = 0.3f),
    )
}

private fun DrawScope.drawGrid() {
    val gridColor = Color.Gray.copy(alpha = 0.3f)
    val gridLines = 5

    // 垂直网格线
    for (i in 0..gridLines) {
        val x = (i.toFloat() / gridLines) * size.width
        drawLine(
            color = gridColor,
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = 1f,
        )
    }

    // 水平网格线
    for (i in 0..gridLines) {
        val y = (i.toFloat() / gridLines) * size.height
        drawLine(
            color = gridColor,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1f,
        )
    }
}

private fun DrawScope.drawWaveform(
    amplitudes: List<Float>,
    color: Color,
    progress: Float,
) {
    if (amplitudes.isEmpty()) return

    val path = Path()
    val centerY = size.height / 2

    amplitudes.forEachIndexed { index, amplitude ->
        val x = (index.toFloat() / amplitudes.size) * size.width * progress
        val y = centerY + amplitude * centerY * 0.8f

        if (index == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(width = 2f, cap = StrokeCap.Round),
    )
}

private fun DrawScope.drawGeometryShape(shape: GeometryShape) {
    when (shape.type) {
        ShapeType.CIRCLE -> {
            drawCircle(
                color = shape.color,
                radius = shape.size,
                center = shape.position,
            )
        }
        ShapeType.RECTANGLE -> {
            drawRect(
                color = shape.color,
                topLeft =
                    Offset(
                        shape.position.x - shape.size,
                        shape.position.y - shape.size,
                    ),
                size = Size(shape.size * 2, shape.size * 2),
            )
        }
        ShapeType.TRIANGLE -> {
            val path = Path()
            path.moveTo(shape.position.x, shape.position.y - shape.size)
            path.lineTo(shape.position.x - shape.size, shape.position.y + shape.size)
            path.lineTo(shape.position.x + shape.size, shape.position.y + shape.size)
            path.close()
            drawPath(path, shape.color)
        }
    }
}

// 数据类和枚举
enum class ChartType {
    LINE,
    BAR,
    PIE,
    AREA,
}

enum class ShapeType {
    CIRCLE,
    RECTANGLE,
    TRIANGLE,
}

data class GeometryShape(
    val type: ShapeType,
    val position: Offset,
    val size: Float,
    val color: Color,
)

data class DashboardData(
    val performanceScore: Float,
    val qualityScore: Float,
    val trendData: List<Float>,
    val categoryData: List<Float>,
    val waveData: List<Float>,
)

// 默认数据
private fun getDefaultShapes(): List<GeometryShape> {
    return listOf(
        GeometryShape(ShapeType.CIRCLE, Offset(100f, 100f), 30f, Color.Red),
        GeometryShape(ShapeType.RECTANGLE, Offset(200f, 150f), 25f, Color.Blue),
        GeometryShape(ShapeType.TRIANGLE, Offset(300f, 120f), 35f, Color.Green),
    )
}
