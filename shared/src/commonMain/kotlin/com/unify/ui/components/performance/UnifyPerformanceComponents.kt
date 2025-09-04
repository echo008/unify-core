package com.unify.ui.components.performance

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

/**
 * 跨平台统一性能监控组件
 */

/**
 * 性能数据类型
 */
data class PerformanceMetric(
    val name: String,
    val value: Double,
    val unit: String,
    val status: PerformanceStatus,
    val trend: List<Double> = emptyList()
)

enum class PerformanceStatus {
    EXCELLENT, GOOD, WARNING, CRITICAL
}

data class MemoryInfo(
    val used: Long,
    val total: Long,
    val available: Long,
    val gcCount: Int = 0
)

data class CPUInfo(
    val usage: Double,
    val cores: Int,
    val frequency: Double = 0.0
)

data class NetworkInfo(
    val downloadSpeed: Double,
    val uploadSpeed: Double,
    val latency: Double,
    val packetsLost: Int = 0
)

/**
 * 性能仪表板组件
 */
@Composable
fun UnifyPerformanceDashboard(
    modifier: Modifier = Modifier,
    refreshInterval: Long = 1000L
) {
    var performanceMetrics by remember { mutableStateOf(generateMockMetrics()) }
    var isMonitoring by remember { mutableStateOf(true) }
    
    LaunchedEffect(isMonitoring) {
        while (isMonitoring) {
            delay(refreshInterval)
            performanceMetrics = generateMockMetrics()
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 标题和控制
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "性能监控",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Switch(
                checked = isMonitoring,
                onCheckedChange = { isMonitoring = it }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 性能指标网格
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(performanceMetrics.chunked(2)) { rowMetrics ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowMetrics.forEach { metric ->
                        PerformanceMetricCard(
                            metric = metric,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // 如果行中只有一个项目，添加空白占位
                    if (rowMetrics.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/**
 * 性能指标卡片
 */
@Composable
fun PerformanceMetricCard(
    metric: PerformanceMetric,
    modifier: Modifier = Modifier
) {
    val statusColor = when (metric.status) {
        PerformanceStatus.EXCELLENT -> Color(0xFF4CAF50)
        PerformanceStatus.GOOD -> Color(0xFF8BC34A)
        PerformanceStatus.WARNING -> Color(0xFFFF9800)
        PerformanceStatus.CRITICAL -> Color(0xFFF44336)
    }
    
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = statusColor.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = metric.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                Icon(
                    imageVector = getStatusIcon(metric.status),
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${metric.value} ${metric.unit}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )
            
            if (metric.trend.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                TrendChart(
                    data = metric.trend,
                    color = statusColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                )
            }
        }
    }
}

/**
 * 趋势图表组件
 */
@Composable
fun TrendChart(
    data: List<Double>,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (data.size < 2) return@Canvas
        
        val maxValue = data.maxOrNull() ?: 1.0
        val minValue = data.minOrNull() ?: 0.0
        val range = maxValue - minValue
        
        val path = Path()
        val stepX = size.width / (data.size - 1)
        
        data.forEachIndexed { index, value ->
            val x = index * stepX
            val y = if (range > 0) {
                size.height - ((value - minValue) / range * size.height).toFloat()
            } else {
                size.height / 2
            }
            
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        
        drawPath(
            path = path,
            color = color,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
        )
    }
}

/**
 * 内存使用监控组件
 */
@Composable
fun UnifyMemoryMonitor(
    modifier: Modifier = Modifier
) {
    var memoryInfo by remember { mutableStateOf(generateMockMemoryInfo()) }
    var isMonitoring by remember { mutableStateOf(true) }
    
    LaunchedEffect(isMonitoring) {
        while (isMonitoring) {
            delay(2000)
            memoryInfo = generateMockMemoryInfo()
        }
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "内存使用情况",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Switch(
                    checked = isMonitoring,
                    onCheckedChange = { isMonitoring = it }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 内存使用进度条
            val usagePercentage = (memoryInfo.used.toFloat() / memoryInfo.total.toFloat())
            
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "已使用: ${formatBytes(memoryInfo.used)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "总计: ${formatBytes(memoryInfo.total)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LinearProgressIndicator(
                    progress = { usagePercentage },
                    modifier = Modifier.fillMaxWidth(),
                    color = when {
                        usagePercentage > 0.9f -> Color(0xFFF44336)
                        usagePercentage > 0.7f -> Color(0xFFFF9800)
                        else -> Color(0xFF4CAF50)
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "可用: ${formatBytes(memoryInfo.available)} | GC次数: ${memoryInfo.gcCount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * CPU使用监控组件
 */
@Composable
fun UnifyCPUMonitor(
    modifier: Modifier = Modifier
) {
    var cpuInfo by remember { mutableStateOf(generateMockCPUInfo()) }
    var cpuHistory by remember { mutableStateOf(List(20) { 0.0 }) }
    var isMonitoring by remember { mutableStateOf(true) }
    
    LaunchedEffect(isMonitoring) {
        while (isMonitoring) {
            delay(1000)
            cpuInfo = generateMockCPUInfo()
            cpuHistory = cpuHistory.drop(1) + cpuInfo.usage
        }
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CPU使用情况",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Switch(
                    checked = isMonitoring,
                    onCheckedChange = { isMonitoring = it }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "${String.format("%.1f", cpuInfo.usage)}%",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            cpuInfo.usage > 80 -> Color(0xFFF44336)
                            cpuInfo.usage > 60 -> Color(0xFFFF9800)
                            else -> Color(0xFF4CAF50)
                        }
                    )
                    Text(
                        text = "CPU使用率",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Column {
                    Text(
                        text = "${cpuInfo.cores}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "核心数",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                if (cpuInfo.frequency > 0) {
                    Column {
                        Text(
                            text = "${String.format("%.1f", cpuInfo.frequency)}GHz",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "频率",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TrendChart(
                data = cpuHistory,
                color = Color(0xFF2196F3),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            )
        }
    }
}

/**
 * 网络性能监控组件
 */
@Composable
fun UnifyNetworkMonitor(
    modifier: Modifier = Modifier
) {
    var networkInfo by remember { mutableStateOf(generateMockNetworkInfo()) }
    var isMonitoring by remember { mutableStateOf(true) }
    
    LaunchedEffect(isMonitoring) {
        while (isMonitoring) {
            delay(2000)
            networkInfo = generateMockNetworkInfo()
        }
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "网络性能",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Switch(
                    checked = isMonitoring,
                    onCheckedChange = { isMonitoring = it }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                NetworkMetricItem(
                    icon = Icons.Default.Download,
                    label = "下载",
                    value = "${String.format("%.1f", networkInfo.downloadSpeed)} Mbps",
                    color = Color(0xFF4CAF50)
                )
                
                NetworkMetricItem(
                    icon = Icons.Default.Upload,
                    label = "上传",
                    value = "${String.format("%.1f", networkInfo.uploadSpeed)} Mbps",
                    color = Color(0xFF2196F3)
                )
                
                NetworkMetricItem(
                    icon = Icons.Default.Speed,
                    label = "延迟",
                    value = "${String.format("%.0f", networkInfo.latency)} ms",
                    color = when {
                        networkInfo.latency > 100 -> Color(0xFFF44336)
                        networkInfo.latency > 50 -> Color(0xFFFF9800)
                        else -> Color(0xFF4CAF50)
                    }
                )
            }
            
            if (networkInfo.packetsLost > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "丢包: ${networkInfo.packetsLost} 个",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFF44336)
                )
            }
        }
    }
}

@Composable
private fun NetworkMetricItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

// 辅助函数
private fun getStatusIcon(status: PerformanceStatus) = when (status) {
    PerformanceStatus.EXCELLENT -> Icons.Default.CheckCircle
    PerformanceStatus.GOOD -> Icons.Default.Check
    PerformanceStatus.WARNING -> Icons.Default.Warning
    PerformanceStatus.CRITICAL -> Icons.Default.Error
}

private fun formatBytes(bytes: Long): String {
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var size = bytes.toDouble()
    var unitIndex = 0
    
    while (size >= 1024 && unitIndex < units.size - 1) {
        size /= 1024
        unitIndex++
    }
    
    return "${String.format("%.1f", size)} ${units[unitIndex]}"
}

// 模拟数据生成函数
private fun generateMockMetrics(): List<PerformanceMetric> {
    return listOf(
        PerformanceMetric(
            name = "FPS",
            value = Random.nextDouble(55.0, 60.0),
            unit = "fps",
            status = PerformanceStatus.EXCELLENT,
            trend = List(10) { Random.nextDouble(55.0, 60.0) }
        ),
        PerformanceMetric(
            name = "内存",
            value = Random.nextDouble(60.0, 85.0),
            unit = "%",
            status = PerformanceStatus.GOOD,
            trend = List(10) { Random.nextDouble(60.0, 85.0) }
        ),
        PerformanceMetric(
            name = "CPU",
            value = Random.nextDouble(20.0, 40.0),
            unit = "%",
            status = PerformanceStatus.GOOD,
            trend = List(10) { Random.nextDouble(20.0, 40.0) }
        ),
        PerformanceMetric(
            name = "网络",
            value = Random.nextDouble(10.0, 50.0),
            unit = "ms",
            status = PerformanceStatus.EXCELLENT,
            trend = List(10) { Random.nextDouble(10.0, 50.0) }
        ),
        PerformanceMetric(
            name = "电池",
            value = Random.nextDouble(70.0, 95.0),
            unit = "%",
            status = PerformanceStatus.GOOD
        ),
        PerformanceMetric(
            name = "温度",
            value = Random.nextDouble(35.0, 45.0),
            unit = "°C",
            status = PerformanceStatus.WARNING
        )
    )
}

private fun generateMockMemoryInfo(): MemoryInfo {
    val total = 8L * 1024 * 1024 * 1024 // 8GB
    val used = Random.nextLong(total / 2, (total * 0.8).toLong())
    return MemoryInfo(
        used = used,
        total = total,
        available = total - used,
        gcCount = Random.nextInt(50, 200)
    )
}

private fun generateMockCPUInfo(): CPUInfo {
    return CPUInfo(
        usage = Random.nextDouble(10.0, 80.0),
        cores = listOf(4, 6, 8, 12).random(),
        frequency = Random.nextDouble(2.0, 4.0)
    )
}

private fun generateMockNetworkInfo(): NetworkInfo {
    return NetworkInfo(
        downloadSpeed = Random.nextDouble(10.0, 100.0),
        uploadSpeed = Random.nextDouble(5.0, 50.0),
        latency = Random.nextDouble(10.0, 100.0),
        packetsLost = if (Random.nextBoolean()) 0 else Random.nextInt(1, 5)
    )
}
