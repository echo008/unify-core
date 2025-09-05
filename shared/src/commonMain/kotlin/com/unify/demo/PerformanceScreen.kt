package com.unify.demo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.unify.ui.components.feedback.UnifyProgress
import com.unify.core.components.UnifySection
import kotlinx.coroutines.delay

/**
 * Unify性能监控演示界面
 * 展示跨平台性能监控功能
 */

data class PerformanceMetric(
    val name: String,
    val value: Float,
    val unit: String,
    val status: PerformanceStatus,
    val description: String
)

enum class PerformanceStatus {
    EXCELLENT, GOOD, FAIR, POOR
}

@Composable
fun PerformanceScreen(
    modifier: Modifier = Modifier
) {
    var metrics by remember { mutableStateOf(emptyList<PerformanceMetric>()) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        delay(1000) // 模拟加载时间
        metrics = generatePerformanceMetrics()
        isLoading = false
    }
    
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            UnifySection(
                title = "性能监控"
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    PerformanceOverview(metrics = metrics)
                }
            }
        }
        
        if (!isLoading) {
            items(metrics) { metric ->
                PerformanceMetricCard(metric = metric)
            }
            
            item {
                PerformanceActions()
            }
        }
    }
}

@Composable
private fun PerformanceOverview(
    metrics: List<PerformanceMetric>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "系统概览",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val excellentCount = metrics.count { it.status == PerformanceStatus.EXCELLENT }
                val goodCount = metrics.count { it.status == PerformanceStatus.GOOD }
                val fairCount = metrics.count { it.status == PerformanceStatus.FAIR }
                val poorCount = metrics.count { it.status == PerformanceStatus.POOR }
                
                PerformanceStatusItem("优秀", excellentCount, Color(0xFF4CAF50))
                PerformanceStatusItem("良好", goodCount, Color(0xFF8BC34A))
                PerformanceStatusItem("一般", fairCount, Color(0xFFFF9800))
                PerformanceStatusItem("较差", poorCount, Color(0xFFF44336))
            }
        }
    }
}

@Composable
private fun PerformanceStatusItem(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun PerformanceMetricCard(
    metric: PerformanceMetric,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = metric.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = metric.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = getStatusColor(metric.status)
                ) {
                    Text(
                        text = getStatusText(metric.status),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${metric.value} ${metric.unit}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = getStatusColor(metric.status)
                )
                
                UnifyProgress(
                    progress = metric.value / 100f,
                    color = getStatusColor(metric.status),
                    modifier = Modifier
                        .width(100.dp)
                        .height(8.dp)
                )
            }
        }
    }
}

@Composable
private fun PerformanceActions(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "性能优化",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { /* 刷新性能数据 */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("刷新数据")
                }
                
                OutlinedButton(
                    onClick = { /* 导出报告 */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("导出报告")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedButton(
                onClick = { /* 性能优化建议 */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("获取优化建议")
            }
        }
    }
}

private fun getStatusColor(status: PerformanceStatus): Color {
    return when (status) {
        PerformanceStatus.EXCELLENT -> Color(0xFF4CAF50)
        PerformanceStatus.GOOD -> Color(0xFF8BC34A)
        PerformanceStatus.FAIR -> Color(0xFFFF9800)
        PerformanceStatus.POOR -> Color(0xFFF44336)
    }
}

private fun getStatusText(status: PerformanceStatus): String {
    return when (status) {
        PerformanceStatus.EXCELLENT -> "优秀"
        PerformanceStatus.GOOD -> "良好"
        PerformanceStatus.FAIR -> "一般"
        PerformanceStatus.POOR -> "较差"
    }
}

private fun generatePerformanceMetrics(): List<PerformanceMetric> {
    return listOf(
        PerformanceMetric(
            name = "CPU使用率",
            value = 45f,
            unit = "%",
            status = PerformanceStatus.GOOD,
            description = "当前CPU使用情况"
        ),
        PerformanceMetric(
            name = "内存使用率",
            value = 68f,
            unit = "%",
            status = PerformanceStatus.FAIR,
            description = "系统内存占用情况"
        ),
        PerformanceMetric(
            name = "帧率",
            value = 58f,
            unit = "FPS",
            status = PerformanceStatus.GOOD,
            description = "UI渲染帧率"
        ),
        PerformanceMetric(
            name = "网络延迟",
            value = 25f,
            unit = "ms",
            status = PerformanceStatus.EXCELLENT,
            description = "网络请求延迟"
        ),
        PerformanceMetric(
            name = "存储I/O",
            value = 78f,
            unit = "MB/s",
            status = PerformanceStatus.GOOD,
            description = "磁盘读写速度"
        ),
        PerformanceMetric(
            name = "电池使用",
            value = 85f,
            unit = "%",
            status = PerformanceStatus.FAIR,
            description = "剩余电量"
        )
    )
}
