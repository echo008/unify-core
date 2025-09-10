package com.unify.ui.components.performance

import androidx.compose.foundation.Canvas
import kotlinx.datetime.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unify.core.performance.*
import kotlinx.coroutines.delay

/**
 * 统一性能监控面板组件
 * 基于Compose实现的跨平台性能监控UI
 */
@Composable
fun UnifyPerformanceMonitorComponent(
    collector: UnifyPerformanceCollector,
    modifier: Modifier = Modifier
) {
    val collectorState by collector.collectorState.collectAsState()
    val performanceMetrics by collector.performanceMetrics.collectAsState()
    val performanceStats by collector.performanceStats.collectAsState()
    
    var selectedTab by remember { mutableStateOf(0) }
    var isMonitoring by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 标题和控制区域
        PerformanceHeader(
            collectorState = collectorState,
            isMonitoring = isMonitoring,
            onStartStop = { 
                isMonitoring = !isMonitoring
                if (isMonitoring) {
                    // 启动监控
                } else {
                    // 停止监控
                }
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 标签页导航
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("实时监控") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("性能图表") }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("告警管理") }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("统计信息") }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 内容区域
        when (selectedTab) {
            0 -> RealTimeMonitorTab(performanceMetrics)
            1 -> PerformanceChartsTab(collector)
            2 -> AlertManagementTab(collector)
            3 -> StatisticsTab(performanceStats)
        }
    }
}

/**
 * 性能监控头部组件
 */
@Composable
private fun PerformanceHeader(
    collectorState: CollectorState,
    isMonitoring: Boolean,
    onStartStop: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "性能监控系统",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "状态: ${getStateText(collectorState)}",
                fontSize = 14.sp,
                color = getStateColor(collectorState)
            )
        }
        
        Button(
            onClick = onStartStop,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isMonitoring) Color.Red else Color.Green
            )
        ) {
            Text(if (isMonitoring) "停止监控" else "开始监控")
        }
    }
}

/**
 * 实时监控标签页
 */
@Composable
private fun RealTimeMonitorTab(
    performanceMetrics: PerformanceSnapshot
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            // CPU监控卡片
            PerformanceCard(
                title = "CPU使用率",
                value = "${com.unify.core.utils.UnifyStringUtils.format("%.1f", performanceMetrics.cpuMetrics.usage)}%",
                progress = (performanceMetrics.cpuMetrics.usage / 100.0).toFloat(),
                color = getCpuColor(performanceMetrics.cpuMetrics.usage),
                details = listOf(
                    "核心数: ${performanceMetrics.cpuMetrics.cores}",
                    "频率: ${performanceMetrics.cpuMetrics.frequency / 1000000}MHz",
                    "负载: ${com.unify.core.utils.UnifyStringUtils.format("%.2f", performanceMetrics.cpuMetrics.loadAverage)}"
                )
            )
        }
        
        item {
            // 内存监控卡片
            val memoryUsage = if (performanceMetrics.memoryMetrics.total > 0) {
                (performanceMetrics.memoryMetrics.used.toDouble() / performanceMetrics.memoryMetrics.total) * 100
            } else 0.0
            
            PerformanceCard(
                title = "内存使用率",
                value = "${com.unify.core.utils.UnifyStringUtils.format("%.1f", (performanceMetrics.memoryMetrics.used.toDouble() / performanceMetrics.memoryMetrics.total * 100))}%",
                progress = (performanceMetrics.memoryMetrics.used.toDouble() / performanceMetrics.memoryMetrics.total).toFloat(),
                color = getMemoryColor(performanceMetrics.memoryMetrics.used.toDouble() / performanceMetrics.memoryMetrics.total * 100),
                details = listOf(
                    "总内存: ${formatBytes(performanceMetrics.memoryMetrics.total)}",
                    "已用: ${formatBytes(performanceMetrics.memoryMetrics.used)}",
                    "可用: ${formatBytes(performanceMetrics.memoryMetrics.available)}"
                )
            )
        }
        
        item {
            // 网络监控卡片
            PerformanceCard(
                title = "网络活动",
                value = "${formatBytes(performanceMetrics.networkMetrics.bytesReceived + performanceMetrics.networkMetrics.bytesSent)}/s",
                progress = 0.5f, // 网络活动没有固定的进度条概念
                color = Color.Blue,
                details = listOf(
                    "接收: ${formatBytes(performanceMetrics.networkMetrics.bytesReceived)}",
                    "发送: ${formatBytes(performanceMetrics.networkMetrics.bytesSent)}",
                    "错误: ${performanceMetrics.networkMetrics.errors}"
                )
            )
        }
        
        item {
            // 磁盘监控卡片
            val diskUsage = performanceMetrics.diskMetrics.usage
            val diskUsed = performanceMetrics.diskMetrics.usedSpace
            val diskUsagePercent = if (performanceMetrics.diskMetrics.totalSpace > 0) {
                (diskUsed.toDouble() / performanceMetrics.diskMetrics.totalSpace) * 100
            } else 0.0
            
            PerformanceCard(
                title = "磁盘使用率",
                value = "${com.unify.core.utils.UnifyStringUtils.format("%.1f", diskUsagePercent)}%",
                progress = (diskUsagePercent / 100.0).toFloat(),
                color = getDiskColor(diskUsagePercent),
                details = listOf(
                    "总空间: ${formatBytes(performanceMetrics.diskMetrics.totalSpace)}",
                    "已用: ${formatBytes(performanceMetrics.diskMetrics.usedSpace)}",
                    "可用: ${formatBytes(performanceMetrics.diskMetrics.freeSpace)}"
                )
            )
        }
        
        performanceMetrics.batteryMetrics?.let { battery ->
            item {
                // 电池监控卡片
                PerformanceCard(
                    title = "电池状态",
                    value = "${com.unify.core.utils.UnifyStringUtils.format("%.1f", battery.level)}%",
                    progress = (battery.level / 100.0).toFloat(),
                    color = getBatteryColor(battery.level),
                    details = listOf(
                        "充电状态: ${if (battery.isCharging) "充电中" else "未充电"}",
                        "电压: ${com.unify.core.utils.UnifyStringUtils.format("%.2f", battery.voltage)}V",
                        "温度: ${com.unify.core.utils.UnifyStringUtils.format("%.1f", battery.temperature)}°C"
                    )
                )
            }
        }
    }
}

/**
 * 性能图表标签页
 */
@Composable
private fun PerformanceChartsTab(
    collector: UnifyPerformanceCollector
) {
    var chartData by remember { mutableStateOf<List<PerformanceSnapshot>>(emptyList()) }
    
    LaunchedEffect(Unit) {
        while (true) {
            // 模拟获取历史数据
            delay(5000)
        }
    }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "性能趋势图表",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        
        // CPU趋势图
        PerformanceChart(
            title = "CPU使用率趋势",
            data = chartData.map { it.cpuMetrics.usage },
            color = Color.Red,
            unit = "%"
        )
        
        // 内存趋势图
        PerformanceChart(
            title = "内存使用率趋势",
            data = chartData.map { 
                if (it.memoryMetrics.total > 0) {
                    (it.memoryMetrics.used.toDouble() / it.memoryMetrics.total) * 100
                } else 0.0
            },
            color = Color.Blue,
            unit = "%"
        )
    }
}

/**
 * 告警管理标签页
 */
@Composable
private fun AlertManagementTab(
    collector: UnifyPerformanceCollector
) {
    var alerts by remember { mutableStateOf<List<PerformanceAlert>>(emptyList()) }
    
    LaunchedEffect(Unit) {
        alerts = collector.getPerformanceAlerts()
    }
    
    Column {
        Text(
            text = "性能告警",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (alerts.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("暂无告警信息")
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(alerts) { alert ->
                    AlertCard(alert = alert)
                }
            }
        }
    }
}

/**
 * 统计信息标签页
 */
@Composable
private fun StatisticsTab(
    performanceStats: PerformanceStats
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            StatisticCard(
                title = "监控会话",
                value = performanceStats.monitoringSessions.toString(),
                description = "总监控会话数"
            )
        }
        
        item {
            StatisticCard(
                title = "快照数量",
                value = performanceStats.snapshotsTaken.toString(),
                description = "已采集的性能快照"
            )
        }
        
        item {
            StatisticCard(
                title = "异常检测",
                value = performanceStats.anomaliesDetected.toString(),
                description = "检测到的性能异常"
            )
        }
        
        item {
            StatisticCard(
                title = "报告生成",
                value = performanceStats.reportsGenerated.toString(),
                description = "生成的性能报告"
            )
        }
        
        item {
            StatisticCard(
                title = "平均收集时间",
                value = "${com.unify.core.utils.UnifyStringUtils.format("%.2f", performanceStats.averageCollectionTime)}ms",
                description = "性能数据收集平均耗时"
            )
        }
    }
}

/**
 * 性能卡片组件
 */
@Composable
private fun PerformanceCard(
    title: String,
    value: String,
    progress: Float,
    color: Color,
    details: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = progress.coerceIn(0f, 1f),
                modifier = Modifier.fillMaxWidth(),
                color = color
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            details.forEach { detail ->
                Text(
                    text = detail,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 性能图表组件
 */
@Composable
private fun PerformanceChart(
    title: String,
    data: List<Double>,
    color: Color,
    unit: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                if (data.isNotEmpty()) {
                    drawPerformanceChart(data, color)
                }
            }
        }
    }
}

/**
 * 告警卡片组件
 */
@Composable
private fun AlertCard(
    alert: PerformanceAlert
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (alert.severity) {
                AlertSeverity.CRITICAL -> Color.Red.copy(alpha = 0.1f)
                AlertSeverity.HIGH -> Color(0xFFFF9800).copy(alpha = 0.1f)
                AlertSeverity.MEDIUM -> Color.Yellow.copy(alpha = 0.1f)
                AlertSeverity.LOW -> Color.Blue.copy(alpha = 0.1f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = alert.type.name,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = alert.severity.name,
                    color = when (alert.severity) {
                        AlertSeverity.CRITICAL -> Color.Red
                        AlertSeverity.HIGH -> Color(0xFFFF9800)
                        AlertSeverity.MEDIUM -> Color(0xFFFFA500)
                        AlertSeverity.LOW -> Color.Blue
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = alert.message,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "时间: ${formatTimestamp(alert.timestamp)}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 统计卡片组件
 */
@Composable
private fun StatisticCard(
    title: String,
    value: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// 辅助函数

private fun DrawScope.drawPerformanceChart(data: List<Double>, color: Color) {
    if (data.size < 2) return
    
    val maxValue = data.maxOrNull() ?: 100.0
    val minValue = data.minOrNull() ?: 0.0
    val range = maxValue - minValue
    
    val path = Path()
    val stepX = size.width / (data.size - 1)
    
    data.forEachIndexed { index, value ->
        val x = index * stepX
        val y = size.height - ((value - minValue) / range * size.height).toFloat()
        
        if (index == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    
    drawPath(path, color, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx()))
}

private fun getStateText(state: CollectorState): String = when (state) {
    CollectorState.INITIALIZING -> "初始化中"
    CollectorState.IDLE -> "空闲"
    CollectorState.STARTING -> "启动中"
    CollectorState.RUNNING -> "运行中"
    CollectorState.STOPPING -> "停止中"
    CollectorState.ERROR -> "错误"
}

private fun getStateColor(state: CollectorState): Color = when (state) {
    CollectorState.RUNNING -> Color.Green
    CollectorState.ERROR -> Color.Red
    CollectorState.STARTING, CollectorState.STOPPING -> Color(0xFFFF9800)
    else -> Color.Gray
}

private fun getCpuColor(usage: Double): Color = when {
    usage > 80 -> Color.Red
    usage > 60 -> Color(0xFFFF9800)
    else -> Color.Green
}

private fun getMemoryColor(usage: Double): Color = when {
    usage > 85 -> Color.Red
    usage > 70 -> Color(0xFFFF9800)
    else -> Color.Blue
}

private fun getDiskColor(usage: Double): Color = when {
    usage > 90 -> Color.Red
    usage > 75 -> Color(0xFFFF9800)
    else -> Color.Green
}

private fun getBatteryColor(level: Double): Color = when {
    level < 20 -> Color.Red
    level < 50 -> Color(0xFFFF9800)
    else -> Color.Green
}

private fun formatBytes(bytes: Long): String {
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var size = bytes.toDouble()
    var unitIndex = 0
    
    while (size >= 1024 && unitIndex < units.size - 1) {
        size /= 1024
        unitIndex++
    }
    
    return "${com.unify.core.utils.UnifyStringUtils.format("%.1f", size)} ${units[unitIndex]}"
}

private fun formatTimestamp(timestamp: Long): String {
    // 简化的时间格式化 - 使用跨平台实现
    val instant = Instant.fromEpochMilliseconds(timestamp)
    return instant.toString().substring(0, 19).replace('T', ' ')
}
