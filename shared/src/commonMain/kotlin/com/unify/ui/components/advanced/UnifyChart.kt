package com.unify.ui.components.advanced

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Unify跨平台图表组件
 * 支持线图、柱状图、饼图等多种图表类型
 */

data class ChartData(
    val label: String,
    val value: Float,
    val color: Color = Color.Blue
)

enum class ChartType {
    LINE, BAR, PIE, AREA
}

@Composable
expect fun UnifyChart(
    data: List<ChartData>,
    chartType: ChartType = ChartType.LINE,
    modifier: Modifier = Modifier,
    title: String = "",
    showLegend: Boolean = true,
    animationEnabled: Boolean = true
)

@Composable
expect fun UnifyLineChart(
    data: List<ChartData>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Float = 3f,
    showPoints: Boolean = true,
    showGrid: Boolean = true
)

@Composable
expect fun UnifyBarChart(
    data: List<ChartData>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    showValues: Boolean = true,
    horizontal: Boolean = false
)

@Composable
expect fun UnifyPieChart(
    data: List<ChartData>,
    modifier: Modifier = Modifier,
    showLabels: Boolean = true,
    showPercentages: Boolean = true,
    centerHoleRadius: Float = 0f
)

@Composable
expect fun UnifyAreaChart(
    data: List<ChartData>,
    modifier: Modifier = Modifier,
    fillColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
    strokeColor: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Float = 2f
)

/**
 * 图表工具函数
 */
object ChartUtils {
    fun normalizeData(data: List<ChartData>): List<ChartData> {
        val maxValue = data.maxOfOrNull { it.value } ?: 1f
        return data.map { it.copy(value = it.value / maxValue) }
    }
    
    fun calculateChartBounds(data: List<ChartData>): Pair<Float, Float> {
        val minValue = data.minOfOrNull { it.value } ?: 0f
        val maxValue = data.maxOfOrNull { it.value } ?: 1f
        return Pair(minValue, maxValue)
    }
}
