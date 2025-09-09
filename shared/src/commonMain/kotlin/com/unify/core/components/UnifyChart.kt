package com.unify.core.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Unify统一图表组件
 * 100% Kotlin Compose语法实现
 */
data class UnifyChartData(
    val label: String,
    val value: Float,
    val color: Color,
)

enum class UnifyChartType {
    LINE,
    BAR,
    PIE,
    AREA,
}

@Composable
fun UnifyChart(
    data: List<UnifyChartData>,
    type: UnifyChartType,
    modifier: Modifier = Modifier,
    title: String? = null,
    showLegend: Boolean = true,
    showValues: Boolean = false,
    animationEnabled: Boolean = true,
) {
    UnifyCard(
        modifier = modifier,
        type = UnifyCardType.OUTLINED,
    ) {
        UnifyColumn(
            modifier = Modifier.padding(16.dp),
            spacing = UnifySpacing.MEDIUM,
        ) {
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            when (type) {
                UnifyChartType.LINE -> {
                    UnifyLineChart(
                        data = data,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                    )
                }
                UnifyChartType.BAR -> {
                    UnifyBarChart(
                        data = data,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                    )
                }
                UnifyChartType.PIE -> {
                    UnifyPieChart(
                        data = data,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                    )
                }
                UnifyChartType.AREA -> {
                    UnifyAreaChart(
                        data = data,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                    )
                }
            }

            if (showLegend) {
                UnifyChartLegend(data = data)
            }
        }
    }
}

@Composable
private fun UnifyLineChart(
    data: List<UnifyChartData>,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas

        val maxValue = data.maxOfOrNull { it.value } ?: 1f
        val stepX = size.width / (data.size - 1).coerceAtLeast(1)
        val points = mutableListOf<Offset>()

        data.forEachIndexed { index, item ->
            val x = index * stepX
            val y = size.height - (item.value / maxValue) * size.height
            points.add(Offset(x, y))
        }

        // 绘制线条
        for (i in 0 until points.size - 1) {
            drawLine(
                color = data[i].color,
                start = points[i],
                end = points[i + 1],
                strokeWidth = 3.dp.toPx(),
            )
        }

        // 绘制点
        points.forEachIndexed { index, point ->
            drawCircle(
                color = data[index].color,
                radius = 4.dp.toPx(),
                center = point,
            )
        }
    }
}

@Composable
private fun UnifyBarChart(
    data: List<UnifyChartData>,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas

        val maxValue = data.maxOfOrNull { it.value } ?: 1f
        val barWidth = size.width / data.size * 0.8f
        val spacing = size.width / data.size * 0.2f

        data.forEachIndexed { index, item ->
            val barHeight = (item.value / maxValue) * size.height
            val x = index * (barWidth + spacing) + spacing / 2
            val y = size.height - barHeight

            drawRect(
                color = item.color,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
            )
        }
    }
}

@Composable
private fun UnifyPieChart(
    data: List<UnifyChartData>,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
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
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
            )

            startAngle += sweepAngle
        }
    }
}

@Composable
private fun UnifyAreaChart(
    data: List<UnifyChartData>,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas

        val maxValue = data.maxOfOrNull { it.value } ?: 1f
        val stepX = size.width / (data.size - 1).coerceAtLeast(1)
        val path = Path()

        // 创建路径
        data.forEachIndexed { index, item ->
            val x = index * stepX
            val y = size.height - (item.value / maxValue) * size.height

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        // 闭合路径到底部
        path.lineTo(size.width, size.height)
        path.lineTo(0f, size.height)
        path.close()

        // 绘制填充区域
        drawPath(
            path = path,
            color = data.firstOrNull()?.color?.copy(alpha = 0.3f) ?: Color.Gray,
        )

        // 绘制边界线
        val linePath = Path()
        data.forEachIndexed { index, item ->
            val x = index * stepX
            val y = size.height - (item.value / maxValue) * size.height

            if (index == 0) {
                linePath.moveTo(x, y)
            } else {
                linePath.lineTo(x, y)
            }
        }

        drawPath(
            path = linePath,
            color = data.firstOrNull()?.color ?: Color.Gray,
            style = Stroke(width = 2.dp.toPx()),
        )
    }
}

@Composable
private fun UnifyChartLegend(
    data: List<UnifyChartData>,
    modifier: Modifier = Modifier,
) {
    UnifyColumn(
        modifier = modifier,
        spacing = UnifySpacing.SMALL,
    ) {
        data.chunked(2).forEach { rowItems ->
            UnifyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                rowItems.forEach { item ->
                    UnifyRow(
                        verticalAlignment = Alignment.CenterVertically,
                        spacing = UnifySpacing.SMALL,
                        modifier = Modifier.weight(1f),
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(item.color),
                        )
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // 如果行中只有一个项目，添加空白占位
                if (rowItems.size == 1) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
