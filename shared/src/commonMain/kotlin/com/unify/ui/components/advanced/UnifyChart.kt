package com.unify.ui.components.advanced

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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.unify.ui.LocalUnifyTheme
import com.unify.ui.components.foundation.*
import kotlin.math.*

/**
 * Unify Chart 组件
 * 支持多平台适配的统一图表组件，参考 KuiklyUI 设计规范
 */

/**
 * 图表类型枚举
 */
enum class UnifyChartType {
    LINE,           // 折线图
    BAR,            // 柱状图
    PIE,            // 饼图
    AREA,           // 面积图
    SCATTER,        // 散点图
    DONUT           // 环形图
}

/**
 * 图表数据点
 */
data class UnifyChartDataPoint(
    val x: Float,
    val y: Float,
    val label: String? = null,
    val color: Color? = null
)

/**
 * 图表数据系列
 */
data class UnifyChartDataSeries(
    val name: String,
    val data: List<UnifyChartDataPoint>,
    val color: Color? = null,
    val strokeWidth: Dp = 2.dp,
    val fillAlpha: Float = 0.3f
)

/**
 * 主要 Unify Chart 组件
 */
@Composable
fun UnifyChart(
    data: List<UnifyChartDataSeries>,
    type: UnifyChartType,
    modifier: Modifier = Modifier,
    title: String? = null,
    showLegend: Boolean = true,
    showGrid: Boolean = true,
    showLabels: Boolean = true,
    animated: Boolean = true,
    colors: List<Color>? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    
    // 默认颜色方案
    val defaultColors = listOf(
        theme.colors.primary,
        theme.colors.secondary,
        theme.colors.tertiary,
        theme.colors.error,
        theme.colors.success,
        theme.colors.warning,
        theme.colors.info
    )
    val actualColors = colors ?: defaultColors
    
    // 动画进度
    val animatedProgress by animateFloatAsState(
        targetValue = if (animated) 1f else 1f,
        animationSpec = if (animated) {
            tween(durationMillis = 1000, easing = EaseOutCubic)
        } else {
            snap()
        },
        label = "chart_animation"
    )
    
    Column(
        modifier = modifier.semantics {
            contentDescription?.let { 
                this.contentDescription = it 
            }
        }
    ) {
        // 标题
        title?.let { titleText ->
            UnifyText(
                text = titleText,
                variant = UnifyTextVariant.TITLE_MEDIUM,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        // 图表内容
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            when (type) {
                UnifyChartType.LINE -> {
                    UnifyLineChart(
                        data = data,
                        colors = actualColors,
                        showGrid = showGrid,
                        showLabels = showLabels,
                        animatedProgress = animatedProgress,
                        textMeasurer = textMeasurer
                    )
                }
                UnifyChartType.BAR -> {
                    UnifyBarChart(
                        data = data,
                        colors = actualColors,
                        showGrid = showGrid,
                        showLabels = showLabels,
                        animatedProgress = animatedProgress,
                        textMeasurer = textMeasurer
                    )
                }
                UnifyChartType.PIE -> {
                    UnifyPieChart(
                        data = data.firstOrNull()?.data ?: emptyList(),
                        colors = actualColors,
                        showLabels = showLabels,
                        animatedProgress = animatedProgress,
                        textMeasurer = textMeasurer
                    )
                }
                UnifyChartType.AREA -> {
                    UnifyAreaChart(
                        data = data,
                        colors = actualColors,
                        showGrid = showGrid,
                        showLabels = showLabels,
                        animatedProgress = animatedProgress,
                        textMeasurer = textMeasurer
                    )
                }
                UnifyChartType.SCATTER -> {
                    UnifyScatterChart(
                        data = data,
                        colors = actualColors,
                        showGrid = showGrid,
                        showLabels = showLabels,
                        animatedProgress = animatedProgress,
                        textMeasurer = textMeasurer
                    )
                }
                UnifyChartType.DONUT -> {
                    UnifyDonutChart(
                        data = data.firstOrNull()?.data ?: emptyList(),
                        colors = actualColors,
                        showLabels = showLabels,
                        animatedProgress = animatedProgress,
                        textMeasurer = textMeasurer
                    )
                }
            }
        }
        
        // 图例
        if (showLegend && data.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            UnifyChartLegend(
                series = data,
                colors = actualColors
            )
        }
    }
}

/**
 * 折线图组件
 */
@Composable
private fun UnifyLineChart(
    data: List<UnifyChartDataSeries>,
    colors: List<Color>,
    showGrid: Boolean,
    showLabels: Boolean,
    animatedProgress: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
) {
    val theme = LocalUnifyTheme.current
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        if (data.isEmpty()) return@Canvas
        
        val padding = 40.dp.toPx()
        val chartWidth = size.width - padding * 2
        val chartHeight = size.height - padding * 2
        
        // 计算数据范围
        val allPoints = data.flatMap { it.data }
        val minX = allPoints.minOfOrNull { it.x } ?: 0f
        val maxX = allPoints.maxOfOrNull { it.x } ?: 1f
        val minY = allPoints.minOfOrNull { it.y } ?: 0f
        val maxY = allPoints.maxOfOrNull { it.y } ?: 1f
        
        // 绘制网格
        if (showGrid) {
            drawGrid(
                chartWidth = chartWidth,
                chartHeight = chartHeight,
                padding = padding,
                gridColor = theme.colors.outline.copy(alpha = 0.2f)
            )
        }
        
        // 绘制折线
        data.forEachIndexed { seriesIndex, series ->
            val seriesColor = series.color ?: colors.getOrNull(seriesIndex % colors.size) ?: colors.first()
            val points = series.data
            
            if (points.size >= 2) {
                val path = Path()
                var isFirst = true
                
                points.forEach { point ->
                    val x = padding + (point.x - minX) / (maxX - minX) * chartWidth
                    val y = padding + chartHeight - (point.y - minY) / (maxY - minY) * chartHeight
                    
                    if (isFirst) {
                        path.moveTo(x, y)
                        isFirst = false
                    } else {
                        path.lineTo(x, y)
                    }
                }
                
                // 应用动画进度
                val animatedPath = Path()
                val pathMeasure = PathMeasure()
                pathMeasure.setPath(path, false)
                pathMeasure.getSegment(0f, pathMeasure.length * animatedProgress, animatedPath, true)
                
                drawPath(
                    path = animatedPath,
                    color = seriesColor,
                    style = Stroke(width = series.strokeWidth.toPx(), cap = StrokeCap.Round)
                )
                
                // 绘制数据点
                points.forEach { point ->
                    val x = padding + (point.x - minX) / (maxX - minX) * chartWidth
                    val y = padding + chartHeight - (point.y - minY) / (maxY - minY) * chartHeight
                    
                    drawCircle(
                        color = seriesColor,
                        radius = 4.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }
        }
    }
}

/**
 * 柱状图组件
 */
@Composable
private fun UnifyBarChart(
    data: List<UnifyChartDataSeries>,
    colors: List<Color>,
    showGrid: Boolean,
    showLabels: Boolean,
    animatedProgress: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
) {
    val theme = LocalUnifyTheme.current
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        if (data.isEmpty()) return@Canvas
        
        val padding = 40.dp.toPx()
        val chartWidth = size.width - padding * 2
        val chartHeight = size.height - padding * 2
        
        val allPoints = data.flatMap { it.data }
        val maxY = allPoints.maxOfOrNull { it.y } ?: 1f
        val minY = 0f
        
        // 绘制网格
        if (showGrid) {
            drawGrid(
                chartWidth = chartWidth,
                chartHeight = chartHeight,
                padding = padding,
                gridColor = theme.colors.outline.copy(alpha = 0.2f)
            )
        }
        
        // 计算柱子宽度
        val totalBars = data.sumOf { it.data.size }
        val barWidth = if (totalBars > 0) chartWidth / totalBars * 0.8f else 0f
        
        var currentX = padding
        
        data.forEachIndexed { seriesIndex, series ->
            val seriesColor = series.color ?: colors.getOrNull(seriesIndex % colors.size) ?: colors.first()
            
            series.data.forEach { point ->
                val barHeight = (point.y - minY) / (maxY - minY) * chartHeight * animatedProgress
                val barTop = padding + chartHeight - barHeight
                
                drawRect(
                    color = seriesColor,
                    topLeft = Offset(currentX, barTop),
                    size = Size(barWidth, barHeight)
                )
                
                currentX += barWidth + 4.dp.toPx()
            }
        }
    }
}

/**
 * 饼图组件
 */
@Composable
private fun UnifyPieChart(
    data: List<UnifyChartDataPoint>,
    colors: List<Color>,
    showLabels: Boolean,
    animatedProgress: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        if (data.isEmpty()) return@Canvas
        
        val center = Offset(size.width / 2, size.height / 2)
        val radius = minOf(size.width, size.height) / 2 * 0.8f
        
        val total = data.sumOf { it.y.toDouble() }.toFloat()
        var currentAngle = -90f
        
        data.forEachIndexed { index, point ->
            val sweepAngle = (point.y / total) * 360f * animatedProgress
            val color = point.color ?: colors.getOrNull(index % colors.size) ?: colors.first()
            
            drawArc(
                color = color,
                startAngle = currentAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2)
            )
            
            currentAngle += sweepAngle
        }
    }
}

/**
 * 面积图组件
 */
@Composable
private fun UnifyAreaChart(
    data: List<UnifyChartDataSeries>,
    colors: List<Color>,
    showGrid: Boolean,
    showLabels: Boolean,
    animatedProgress: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
) {
    val theme = LocalUnifyTheme.current
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        if (data.isEmpty()) return@Canvas
        
        val padding = 40.dp.toPx()
        val chartWidth = size.width - padding * 2
        val chartHeight = size.height - padding * 2
        
        val allPoints = data.flatMap { it.data }
        val minX = allPoints.minOfOrNull { it.x } ?: 0f
        val maxX = allPoints.maxOfOrNull { it.x } ?: 1f
        val minY = allPoints.minOfOrNull { it.y } ?: 0f
        val maxY = allPoints.maxOfOrNull { it.y } ?: 1f
        
        // 绘制网格
        if (showGrid) {
            drawGrid(
                chartWidth = chartWidth,
                chartHeight = chartHeight,
                padding = padding,
                gridColor = theme.colors.outline.copy(alpha = 0.2f)
            )
        }
        
        // 绘制面积图
        data.forEachIndexed { seriesIndex, series ->
            val seriesColor = series.color ?: colors.getOrNull(seriesIndex % colors.size) ?: colors.first()
            val points = series.data
            
            if (points.size >= 2) {
                val path = Path()
                val baseline = padding + chartHeight
                
                // 移动到起始点
                val firstX = padding + (points.first().x - minX) / (maxX - minX) * chartWidth
                val firstY = padding + chartHeight - (points.first().y - minY) / (maxY - minY) * chartHeight
                path.moveTo(firstX, baseline)
                path.lineTo(firstX, firstY)
                
                // 连接所有点
                points.forEach { point ->
                    val x = padding + (point.x - minX) / (maxX - minX) * chartWidth
                    val y = padding + chartHeight - (point.y - minY) / (maxY - minY) * chartHeight
                    path.lineTo(x, y)
                }
                
                // 闭合路径
                val lastX = padding + (points.last().x - minX) / (maxX - minX) * chartWidth
                path.lineTo(lastX, baseline)
                path.close()
                
                // 绘制填充区域
                drawPath(
                    path = path,
                    color = seriesColor.copy(alpha = series.fillAlpha * animatedProgress)
                )
                
                // 绘制边界线
                val linePath = Path()
                var isFirst = true
                points.forEach { point ->
                    val x = padding + (point.x - minX) / (maxX - minX) * chartWidth
                    val y = padding + chartHeight - (point.y - minY) / (maxY - minY) * chartHeight
                    
                    if (isFirst) {
                        linePath.moveTo(x, y)
                        isFirst = false
                    } else {
                        linePath.lineTo(x, y)
                    }
                }
                
                drawPath(
                    path = linePath,
                    color = seriesColor,
                    style = Stroke(width = series.strokeWidth.toPx())
                )
            }
        }
    }
}

/**
 * 散点图组件
 */
@Composable
private fun UnifyScatterChart(
    data: List<UnifyChartDataSeries>,
    colors: List<Color>,
    showGrid: Boolean,
    showLabels: Boolean,
    animatedProgress: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
) {
    val theme = LocalUnifyTheme.current
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        if (data.isEmpty()) return@Canvas
        
        val padding = 40.dp.toPx()
        val chartWidth = size.width - padding * 2
        val chartHeight = size.height - padding * 2
        
        val allPoints = data.flatMap { it.data }
        val minX = allPoints.minOfOrNull { it.x } ?: 0f
        val maxX = allPoints.maxOfOrNull { it.x } ?: 1f
        val minY = allPoints.minOfOrNull { it.y } ?: 0f
        val maxY = allPoints.maxOfOrNull { it.y } ?: 1f
        
        // 绘制网格
        if (showGrid) {
            drawGrid(
                chartWidth = chartWidth,
                chartHeight = chartHeight,
                padding = padding,
                gridColor = theme.colors.outline.copy(alpha = 0.2f)
            )
        }
        
        // 绘制散点
        data.forEachIndexed { seriesIndex, series ->
            val seriesColor = series.color ?: colors.getOrNull(seriesIndex % colors.size) ?: colors.first()
            
            series.data.forEach { point ->
                val x = padding + (point.x - minX) / (maxX - minX) * chartWidth
                val y = padding + chartHeight - (point.y - minY) / (maxY - minY) * chartHeight
                
                drawCircle(
                    color = seriesColor,
                    radius = 6.dp.toPx() * animatedProgress,
                    center = Offset(x, y)
                )
            }
        }
    }
}

/**
 * 环形图组件
 */
@Composable
private fun UnifyDonutChart(
    data: List<UnifyChartDataPoint>,
    colors: List<Color>,
    showLabels: Boolean,
    animatedProgress: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        if (data.isEmpty()) return@Canvas
        
        val center = Offset(size.width / 2, size.height / 2)
        val outerRadius = minOf(size.width, size.height) / 2 * 0.8f
        val innerRadius = outerRadius * 0.5f
        
        val total = data.sumOf { it.y.toDouble() }.toFloat()
        var currentAngle = -90f
        
        data.forEachIndexed { index, point ->
            val sweepAngle = (point.y / total) * 360f * animatedProgress
            val color = point.color ?: colors.getOrNull(index % colors.size) ?: colors.first()
            
            // 绘制外圆弧
            drawArc(
                color = color,
                startAngle = currentAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                size = Size(outerRadius * 2, outerRadius * 2)
            )
            
            currentAngle += sweepAngle
        }
        
        // 绘制内圆（创建环形效果）
        drawCircle(
            color = Color.White,
            radius = innerRadius,
            center = center
        )
    }
}

/**
 * 图例组件
 */
@Composable
private fun UnifyChartLegend(
    series: List<UnifyChartDataSeries>,
    colors: List<Color>
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(series.size) { index ->
            val seriesData = series[index]
            val color = seriesData.color ?: colors.getOrNull(index % colors.size) ?: colors.first()
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(color, RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                UnifyText(
                    text = seriesData.name,
                    variant = UnifyTextVariant.CAPTION
                )
            }
        }
    }
}

/**
 * 绘制网格辅助函数
 */
private fun DrawScope.drawGrid(
    chartWidth: Float,
    chartHeight: Float,
    padding: Float,
    gridColor: Color,
    gridLines: Int = 5
) {
    // 垂直网格线
    for (i in 0..gridLines) {
        val x = padding + (chartWidth / gridLines) * i
        drawLine(
            color = gridColor,
            start = Offset(x, padding),
            end = Offset(x, padding + chartHeight),
            strokeWidth = 1.dp.toPx()
        )
    }
    
    // 水平网格线
    for (i in 0..gridLines) {
        val y = padding + (chartHeight / gridLines) * i
        drawLine(
            color = gridColor,
            start = Offset(padding, y),
            end = Offset(padding + chartWidth, y),
            strokeWidth = 1.dp.toPx()
        )
    }
}
