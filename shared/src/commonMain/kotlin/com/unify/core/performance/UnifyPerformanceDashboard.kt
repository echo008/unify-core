package com.unify.core.performance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.unify.ui.components.foundation.*
import kotlinx.coroutines.launch

/**
 * Unify性能监控仪表板
 * 提供实时性能数据可视化
 */
@Composable
fun UnifyPerformanceDashboard(
    monitor: UnifyPerformanceMonitor,
    modifier: Modifier = Modifier
) {
    var metrics by remember { mutableStateOf(PerformanceMetrics()) }
    var isMonitoring by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(monitor) {
        monitor.getMetrics().collect { newMetrics ->
            metrics = newMetrics
        }
    }
    
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        // 控制面板
        PerformanceControlPanel(
            isMonitoring = isMonitoring,
            onStartMonitoring = {
                coroutineScope.launch {
                    monitor.startMonitoring()
                    isMonitoring = true
                }
            },
            onStopMonitoring = {
                coroutineScope.launch {
                    monitor.stopMonitoring()
                    isMonitoring = false
                }
            },
            onClearMetrics = {
                coroutineScope.launch {
                    monitor.clearMetrics()
                }
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 性能指标卡片
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                CPUUsageCard(cpuUsage = metrics.cpuUsage)
            }
            
            item {
                MemoryUsageCard(memoryMetrics = metrics.memoryUsage)
            }
            
            item {
                FrameRateCard(frameRate = metrics.frameRate)
            }
            
            item {
                NetworkMetricsCard(networkMetrics = metrics.networkMetrics)
            }
            
            if (metrics.customMetrics.isNotEmpty()) {
                item {
                    CustomMetricsCard(customMetrics = metrics.customMetrics)
                }
            }
            
            if (metrics.timers.isNotEmpty()) {
                item {
                    TimersCard(timers = metrics.timers)
                }
            }
        }
    }
}

/**
 * 性能控制面板
 */
@Composable
private fun PerformanceControlPanel(
    isMonitoring: Boolean,
    onStartMonitoring: () -> Unit,
    onStopMonitoring: () -> Unit,
    onClearMetrics: () -> Unit
) {
    UnifyCard {
        Column(modifier = Modifier.padding(16.dp)) {
            UnifyText(
                text = "性能监控控制",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UnifyButton(
                    onClick = if (isMonitoring) onStopMonitoring else onStartMonitoring,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isMonitoring) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                ) {
                    UnifyIcon(
                        imageVector = if (isMonitoring) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    UnifyText(if (isMonitoring) "停止监控" else "开始监控")
                }
                
                UnifyOutlinedButton(onClick = onClearMetrics) {
                    UnifyIcon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    UnifyText("清除数据")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            if (isMonitoring) Color.Green else Color.Gray,
                            androidx.compose.foundation.shape.CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                UnifyText(
                    text = if (isMonitoring) "监控中" else "已停止",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/**
 * CPU使用率卡片
 */
@Composable
private fun CPUUsageCard(cpuUsage: Double) {
    UnifyCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                UnifyIcon(
                    imageVector = Icons.Default.Memory,
                    contentDescription = "CPU",
                    tint = getCPUColor(cpuUsage)
                )
                Spacer(modifier = Modifier.width(8.dp))
                UnifyText(
                    text = "CPU使用率",
                    style = MaterialTheme.typography.titleSmall
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            UnifyText(
                text = "${cpuUsage.toInt()}%",
                style = MaterialTheme.typography.headlineMedium,
                color = getCPUColor(cpuUsage)
            )
            
            LinearProgressIndicator(
                progress = (cpuUsage / 100).toFloat(),
                modifier = Modifier.fillMaxWidth(),
                color = getCPUColor(cpuUsage)
            )
        }
    }
}

/**
 * 内存使用卡片
 */
@Composable
private fun MemoryUsageCard(memoryMetrics: MemoryMetrics) {
    val usagePercent = if (memoryMetrics.totalMemory > 0) {
        (memoryMetrics.usedMemory.toDouble() / memoryMetrics.totalMemory) * 100
    } else 0.0
    
    UnifyCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                UnifyIcon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = "内存",
                    tint = getMemoryColor(usagePercent)
                )
                Spacer(modifier = Modifier.width(8.dp))
                UnifyText(
                    text = "内存使用",
                    style = MaterialTheme.typography.titleSmall
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            UnifyText(
                text = "${PerformanceUtils.formatMemorySize(memoryMetrics.usedMemory)} / ${PerformanceUtils.formatMemorySize(memoryMetrics.totalMemory)}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            UnifyText(
                text = "${usagePercent.toInt()}%",
                style = MaterialTheme.typography.headlineMedium,
                color = getMemoryColor(usagePercent)
            )
            
            LinearProgressIndicator(
                progress = (usagePercent / 100).toFloat(),
                modifier = Modifier.fillMaxWidth(),
                color = getMemoryColor(usagePercent)
            )
        }
    }
}

/**
 * 帧率卡片
 */
@Composable
private fun FrameRateCard(frameRate: Double) {
    UnifyCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                UnifyIcon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = "帧率",
                    tint = getFrameRateColor(frameRate)
                )
                Spacer(modifier = Modifier.width(8.dp))
                UnifyText(
                    text = "帧率",
                    style = MaterialTheme.typography.titleSmall
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            UnifyText(
                text = "${frameRate.toInt()} FPS",
                style = MaterialTheme.typography.headlineMedium,
                color = getFrameRateColor(frameRate)
            )
            
            LinearProgressIndicator(
                progress = (frameRate / 60).toFloat().coerceIn(0f, 1f),
                modifier = Modifier.fillMaxWidth(),
                color = getFrameRateColor(frameRate)
            )
        }
    }
}

/**
 * 网络指标卡片
 */
@Composable
private fun NetworkMetricsCard(networkMetrics: NetworkMetrics) {
    UnifyCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                UnifyIcon(
                    imageVector = Icons.Default.NetworkCheck,
                    contentDescription = "网络",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                UnifyText(
                    text = "网络指标",
                    style = MaterialTheme.typography.titleSmall
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    UnifyText(
                        text = "延迟",
                        style = MaterialTheme.typography.bodySmall
                    )
                    UnifyText(
                        text = "${networkMetrics.latency.toInt()}ms",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Column {
                    UnifyText(
                        text = "请求数",
                        style = MaterialTheme.typography.bodySmall
                    )
                    UnifyText(
                        text = "${networkMetrics.requestCount}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Column {
                    UnifyText(
                        text = "错误数",
                        style = MaterialTheme.typography.bodySmall
                    )
                    UnifyText(
                        text = "${networkMetrics.errorCount}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (networkMetrics.errorCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/**
 * 自定义指标卡片
 */
@Composable
private fun CustomMetricsCard(customMetrics: Map<String, Double>) {
    UnifyCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                UnifyIcon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = "自定义指标",
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                UnifyText(
                    text = "自定义指标",
                    style = MaterialTheme.typography.titleSmall
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            customMetrics.forEach { (name, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    UnifyText(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    UnifyText(
                        text = value.toString(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

/**
 * 计时器卡片
 */
@Composable
private fun TimersCard(timers: Map<String, Long>) {
    UnifyCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                UnifyIcon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "计时器",
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.width(8.dp))
                UnifyText(
                    text = "活动计时器",
                    style = MaterialTheme.typography.titleSmall
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            timers.forEach { (name, duration) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    UnifyText(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    UnifyText(
                        text = "${duration}ms",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

/**
 * 获取CPU颜色
 */
private fun getCPUColor(usage: Double): Color {
    return when {
        usage < 50 -> Color.Green
        usage < 80 -> Color.Yellow
        else -> Color.Red
    }
}

/**
 * 获取内存颜色
 */
private fun getMemoryColor(usage: Double): Color {
    return when {
        usage < 60 -> Color.Green
        usage < 85 -> Color.Yellow
        else -> Color.Red
    }
}

/**
 * 获取帧率颜色
 */
private fun getFrameRateColor(frameRate: Double): Color {
    return when {
        frameRate >= 50 -> Color.Green
        frameRate >= 30 -> Color.Yellow
        else -> Color.Red
    }
}
