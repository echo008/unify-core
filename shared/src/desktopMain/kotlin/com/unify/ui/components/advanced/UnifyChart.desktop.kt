package com.unify.ui.components.advanced

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Desktop平台Chart组件actual实现
 */

@Composable
actual fun UnifyChart(
    data: List<ChartData>,
    chartType: ChartType,
    modifier: Modifier,
    title: String,
    showLegend: Boolean,
    animationEnabled: Boolean,
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (title.isNotEmpty()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            when (chartType) {
                ChartType.LINE ->
                    UnifyLineChart(
                        data = data,
                        modifier = Modifier.height(200.dp),
                    )
                ChartType.BAR ->
                    UnifyBarChart(
                        data = data,
                        modifier = Modifier.height(200.dp),
                    )
                ChartType.PIE ->
                    UnifyPieChart(
                        data = data,
                        modifier = Modifier.height(200.dp),
                    )
                ChartType.AREA ->
                    UnifyAreaChart(
                        data = data,
                        modifier = Modifier.height(200.dp),
                    )
            }

            if (showLegend && data.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(data) { item ->
                        ChartLegendItem(item = item)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartLegendItem(item: ChartData) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Surface(
            modifier = Modifier.size(16.dp),
            color = item.color,
        ) {}
        Text(
            text = item.label,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
actual fun UnifyLineChart(
    data: List<ChartData>,
    modifier: Modifier,
    lineColor: Color,
    strokeWidth: Float,
    showPoints: Boolean,
    showGrid: Boolean,
) {
    Canvas(modifier = modifier.fillMaxWidth()) {
        if (data.isEmpty()) return@Canvas

        val maxValue = data.maxOfOrNull { it.value } ?: 1f
        val minValue = data.minOfOrNull { it.value } ?: 0f
        val valueRange = maxValue - minValue

        val stepX = size.width / (data.size - 1).coerceAtLeast(1)
        val stepY = size.height

        // 绘制网格
        if (showGrid) {
            drawGrid()
        }

        // 绘制线条
        val path = Path()
        data.forEachIndexed { index, point ->
            val x = index * stepX
            val y = size.height - ((point.value - minValue) / valueRange) * stepY

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }

            // 绘制点
            if (showPoints) {
                drawCircle(
                    color = lineColor,
                    radius = 4.dp.toPx(),
                    center = Offset(x, y),
                )
            }
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = strokeWidth.dp.toPx()),
        )
    }
}

@Composable
actual fun UnifyBarChart(
    data: List<ChartData>,
    modifier: Modifier,
    barColor: Color,
    showValues: Boolean,
    horizontal: Boolean,
) {
    Canvas(modifier = modifier.fillMaxWidth()) {
        if (data.isEmpty()) return@Canvas

        val maxValue = data.maxOfOrNull { it.value } ?: 1f

        if (horizontal) {
            // 水平柱状图
            val barHeight = size.height / data.size
            data.forEachIndexed { index, item ->
                val barWidth = (item.value / maxValue) * size.width
                val y = index * barHeight

                drawRect(
                    color = item.color,
                    topLeft = Offset(0f, y),
                    size = androidx.compose.ui.geometry.Size(barWidth, barHeight * 0.8f),
                )
            }
        } else {
            // 垂直柱状图
            val barWidth = size.width / data.size
            data.forEachIndexed { index, item ->
                val barHeight = (item.value / maxValue) * size.height
                val x = index * barWidth

                drawRect(
                    color = item.color,
                    topLeft = Offset(x, size.height - barHeight),
                    size = androidx.compose.ui.geometry.Size(barWidth * 0.8f, barHeight),
                )
            }
        }
    }
}

@Composable
actual fun UnifyPieChart(
    data: List<ChartData>,
    modifier: Modifier,
    showLabels: Boolean,
    showPercentages: Boolean,
    centerHoleRadius: Float,
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        if (data.isEmpty()) return@Canvas

        val total = data.sumOf { it.value.toDouble() }.toFloat()
        val center = Offset(size.width / 2, size.height / 2)
        val radius = minOf(size.width, size.height) / 2 * 0.8f

        var startAngle = -90f

        data.forEach { item ->
            val sweepAngle = (item.value / total) * 360f

            drawArc(
                color = item.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = centerHoleRadius == 0f,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
            )

            startAngle += sweepAngle
        }

        // 绘制中心孔
        if (centerHoleRadius > 0f) {
            drawCircle(
                color = Color.White,
                radius = centerHoleRadius,
                center = center,
            )
        }
    }
}

@Composable
actual fun UnifyAreaChart(
    data: List<ChartData>,
    modifier: Modifier,
    fillColor: Color,
    strokeColor: Color,
    strokeWidth: Float,
) {
    Canvas(modifier = modifier.fillMaxWidth()) {
        if (data.isEmpty()) return@Canvas

        val maxValue = data.maxOfOrNull { it.value } ?: 1f
        val minValue = data.minOfOrNull { it.value } ?: 0f
        val valueRange = maxValue - minValue

        val stepX = size.width / (data.size - 1).coerceAtLeast(1)

        // 创建填充路径
        val fillPath = Path()
        val strokePath = Path()

        data.forEachIndexed { index, point ->
            val x = index * stepX
            val y = size.height - ((point.value - minValue) / valueRange) * size.height

            if (index == 0) {
                fillPath.moveTo(x, size.height)
                fillPath.lineTo(x, y)
                strokePath.moveTo(x, y)
            } else {
                fillPath.lineTo(x, y)
                strokePath.lineTo(x, y)
            }

            if (index == data.size - 1) {
                fillPath.lineTo(x, size.height)
                fillPath.close()
            }
        }

        // 绘制填充区域
        drawPath(
            path = fillPath,
            color = fillColor,
        )

        // 绘制边框线
        drawPath(
            path = strokePath,
            color = strokeColor,
            style = Stroke(width = strokeWidth.dp.toPx()),
        )
    }
}

private fun DrawScope.drawGrid() {
    val gridColor = Color.Gray.copy(alpha = 0.3f)
    val gridLines = 5

    // 垂直网格线
    for (i in 0..gridLines) {
        val x = (size.width / gridLines) * i
        drawLine(
            color = gridColor,
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = 1.dp.toPx(),
        )
    }

    // 水平网格线
    for (i in 0..gridLines) {
        val y = (size.height / gridLines) * i
        drawLine(
            color = gridColor,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1.dp.toPx(),
        )
    }
}
