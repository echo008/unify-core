@file:OptIn(ExperimentalMaterial3Api::class)

package com.unify.ui.components.advanced

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.*

@Composable
actual fun UnifyChart(
    data: List<ChartData>,
    chartType: ChartType,
    modifier: Modifier,
    title: String,
    showLegend: Boolean,
    animationEnabled: Boolean
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (title.isNotEmpty()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            when (chartType) {
                ChartType.LINE -> UnifyLineChart(
                    data = data,
                    modifier = Modifier.height(200.dp)
                )
                ChartType.BAR -> UnifyBarChart(
                    data = data,
                    modifier = Modifier.height(200.dp)
                )
                ChartType.PIE -> UnifyPieChart(
                    data = data,
                    modifier = Modifier.size(200.dp)
                )
                ChartType.AREA -> UnifyAreaChart(
                    data = data,
                    modifier = Modifier.height(200.dp)
                )
            }
            
            if (showLegend) {
                Spacer(modifier = Modifier.height(16.dp))
                ChartLegend(data = data)
            }
        }
    }
}

@Composable
actual fun UnifyLineChart(
    data: List<ChartData>,
    modifier: Modifier,
    lineColor: Color,
    strokeWidth: Float,
    showPoints: Boolean,
    showGrid: Boolean
) {
    if (data.isEmpty()) return
    
    val maxValue = data.maxOfOrNull { it.value } ?: 1f
    val minValue = data.minOfOrNull { it.value } ?: 0f
    val valueRange = maxValue - minValue
    
    Canvas(modifier = modifier.fillMaxWidth()) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val padding = 40f
        
        val chartWidth = canvasWidth - padding * 2
        val chartHeight = canvasHeight - padding * 2
        
        // Draw grid
        if (showGrid) {
            drawGrid(canvasWidth, canvasHeight, padding)
        }
        
        // Draw line
        if (data.size > 1) {
            val path = Path()
            data.forEachIndexed { index, point ->
                val x = padding + (index.toFloat() / (data.size - 1)) * chartWidth
                val y = padding + chartHeight - ((point.value - minValue) / valueRange) * chartHeight
                
                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }
            
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = strokeWidth)
            )
        }
        
        // Draw points
        if (showPoints) {
            data.forEachIndexed { index, point ->
                val x = padding + (index.toFloat() / (data.size - 1)) * chartWidth
                val y = padding + chartHeight - ((point.value - minValue) / valueRange) * chartHeight
                
                drawCircle(
                    color = lineColor,
                    radius = strokeWidth * 2,
                    center = Offset(x, y)
                )
            }
        }
    }
}

@Composable
actual fun UnifyBarChart(
    data: List<ChartData>,
    modifier: Modifier,
    barColor: Color,
    showValues: Boolean,
    horizontal: Boolean
) {
    if (data.isEmpty()) return
    
    val maxValue = data.maxOfOrNull { it.value } ?: 1f
    
    Canvas(modifier = modifier.fillMaxWidth()) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val padding = 40f
        
        val chartWidth = canvasWidth - padding * 2
        val chartHeight = canvasHeight - padding * 2
        
        if (horizontal) {
            // Horizontal bars
            val barHeight = chartHeight / data.size
            data.forEachIndexed { index, point ->
                val barWidth = (point.value / maxValue) * chartWidth
                val y = padding + index * barHeight
                
                drawRect(
                    color = point.color,
                    topLeft = Offset(padding, y),
                    size = Size(barWidth, barHeight * 0.8f)
                )
            }
        } else {
            // Vertical bars
            val barWidth = chartWidth / data.size
            data.forEachIndexed { index, point ->
                val barHeight = (point.value / maxValue) * chartHeight
                val x = padding + index * barWidth
                val y = padding + chartHeight - barHeight
                
                drawRect(
                    color = point.color,
                    topLeft = Offset(x, y),
                    size = Size(barWidth * 0.8f, barHeight)
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
    centerHoleRadius: Float
) {
    if (data.isEmpty()) return
    
    val total = data.sumOf { it.value.toDouble() }.toFloat()
    
    Canvas(modifier = modifier.size(200.dp)) {
        val canvasSize = size.minDimension
        val radius = canvasSize / 2 - 20f
        val center = Offset(size.width / 2, size.height / 2)
        
        var startAngle = -90f
        
        data.forEach { point ->
            val sweepAngle = (point.value / total) * 360f
            
            drawArc(
                color = point.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = centerHoleRadius == 0f,
                topLeft = Offset(
                    center.x - radius,
                    center.y - radius
                ),
                size = Size(radius * 2, radius * 2)
            )
            
            startAngle += sweepAngle
        }
        
        // Draw center hole if specified
        if (centerHoleRadius > 0f) {
            drawCircle(
                color = androidx.compose.ui.graphics.Color.White,
                radius = centerHoleRadius,
                center = center
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
    strokeWidth: Float
) {
    if (data.isEmpty()) return
    
    val maxValue = data.maxOfOrNull { it.value } ?: 1f
    val minValue = data.minOfOrNull { it.value } ?: 0f
    val valueRange = maxValue - minValue
    
    Canvas(modifier = modifier.fillMaxWidth()) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val padding = 40f
        
        val chartWidth = canvasWidth - padding * 2
        val chartHeight = canvasHeight - padding * 2
        
        if (data.size > 1) {
            // Create area path
            val areaPath = Path()
            val linePath = Path()
            
            data.forEachIndexed { index, point ->
                val x = padding + (index.toFloat() / (data.size - 1)) * chartWidth
                val y = padding + chartHeight - ((point.value - minValue) / valueRange) * chartHeight
                
                if (index == 0) {
                    areaPath.moveTo(x, padding + chartHeight) // Start from bottom
                    areaPath.lineTo(x, y)
                    linePath.moveTo(x, y)
                } else {
                    areaPath.lineTo(x, y)
                    linePath.lineTo(x, y)
                }
                
                if (index == data.size - 1) {
                    areaPath.lineTo(x, padding + chartHeight) // Close to bottom
                    areaPath.close()
                }
            }
            
            // Draw filled area
            drawPath(
                path = areaPath,
                color = fillColor
            )
            
            // Draw stroke line
            drawPath(
                path = linePath,
                color = strokeColor,
                style = Stroke(width = strokeWidth)
            )
        }
    }
}

@Composable
private fun ChartLegend(data: List<ChartData>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(data) { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(item.color)
                )
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

private fun DrawScope.drawGrid(
    canvasWidth: Float,
    canvasHeight: Float,
    padding: Float,
    gridColor: Color = Color.Gray.copy(alpha = 0.3f)
) {
    val chartWidth = canvasWidth - padding * 2
    val chartHeight = canvasHeight - padding * 2
    
    // Vertical grid lines
    for (i in 0..4) {
        val x = padding + (i / 4f) * chartWidth
        drawLine(
            color = gridColor,
            start = Offset(x, padding),
            end = Offset(x, padding + chartHeight),
            strokeWidth = 1f
        )
    }
    
    // Horizontal grid lines
    for (i in 0..4) {
        val y = padding + (i / 4f) * chartHeight
        drawLine(
            color = gridColor,
            start = Offset(padding, y),
            end = Offset(padding + chartWidth, y),
            strokeWidth = 1f
        )
    }
}
