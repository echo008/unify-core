package com.unify.ui.screens
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
import com.unify.core.platform.getCurrentTimeMillis
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 性能监控界面
 * 提供实时性能指标展示和分析功能
 */
@Composable
fun PerformanceScreen() {
    var performanceData by remember { mutableStateOf(PerformanceData()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            while (true) {
                performanceData = generatePerformanceData()
                delay(1000) // 每秒更新
            }
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
    ) {
        Text(
            text = "性能监控",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                PerformanceMetricCard(
                    title = "CPU使用率",
                    value = "${performanceData.cpuUsage}%",
                    color = getColorForPercentage(performanceData.cpuUsage),
                )
            }

            item {
                PerformanceMetricCard(
                    title = "内存使用",
                    value = "${performanceData.memoryUsage}MB / ${performanceData.totalMemory}MB",
                    color = getColorForPercentage((performanceData.memoryUsage * 100 / performanceData.totalMemory).toInt()),
                )
            }

            item {
                PerformanceMetricCard(
                    title = "帧率",
                    value = "${performanceData.fps} FPS",
                    color =
                        if (performanceData.fps >= 60) {
                            Color.Green
                        } else if (performanceData.fps >= 30) {
                            Color.Yellow
                        } else {
                            Color.Red
                        },
                )
            }

            item {
                PerformanceMetricCard(
                    title = "网络延迟",
                    value = "${performanceData.networkLatency}ms",
                    color =
                        if (performanceData.networkLatency < 100) {
                            Color.Green
                        } else if (performanceData.networkLatency < 300) {
                            Color.Yellow
                        } else {
                            Color.Red
                        },
                )
            }

            item {
                PerformanceMetricCard(
                    title = "电池使用",
                    value = "${performanceData.batteryLevel}%",
                    color = getColorForBattery(performanceData.batteryLevel),
                )
            }

            items(performanceData.recentEvents) { event ->
                PerformanceEventCard(event = event)
            }
        }
    }
}

@Composable
private fun PerformanceMetricCard(
    title: String,
    value: String,
    color: Color,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = color,
            )
        }
    }
}

@Composable
private fun PerformanceEventCard(event: PerformanceEvent) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    when (event.severity) {
                        EventSeverity.HIGH -> MaterialTheme.colorScheme.errorContainer
                        EventSeverity.MEDIUM -> MaterialTheme.colorScheme.secondaryContainer
                        EventSeverity.LOW -> MaterialTheme.colorScheme.surfaceVariant
                    },
            ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = formatTimestamp(event.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (event.description.isNotEmpty()) {
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

private fun getColorForPercentage(percentage: Int): Color {
    return when {
        percentage < 50 -> Color.Green
        percentage < 80 -> Color.Yellow
        else -> Color.Red
    }
}

private fun getColorForBattery(level: Int): Color {
    return when {
        level > 50 -> Color.Green
        level > 20 -> Color.Yellow
        else -> Color.Red
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = getCurrentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 60000 -> "刚刚"
        diff < 3600000 -> "${diff / 60000}分钟前"
        else -> "${diff / 3600000}小时前"
    }
}

private fun generatePerformanceData(): PerformanceData {
    return PerformanceData(
        cpuUsage = (20..80).random(),
        memoryUsage = (200..800).random(),
        totalMemory = 1024,
        fps = (30..60).random(),
        networkLatency = (50..200).random(),
        batteryLevel = (20..100).random(),
        recentEvents = generateRecentEvents(),
    )
}

private fun generateRecentEvents(): List<PerformanceEvent> {
    val events =
        listOf(
            PerformanceEvent("内存使用过高", "应用内存使用超过阈值", EventSeverity.HIGH, getCurrentTimeMillis() - 30000),
            PerformanceEvent("网络请求缓慢", "API响应时间超过预期", EventSeverity.MEDIUM, getCurrentTimeMillis() - 120000),
            PerformanceEvent("帧率下降", "UI渲染性能降低", EventSeverity.LOW, getCurrentTimeMillis() - 300000),
        )
    return events.take((1..3).random())
}

data class PerformanceData(
    val cpuUsage: Int = 0,
    val memoryUsage: Int = 0,
    val totalMemory: Int = 1024,
    val fps: Int = 60,
    val networkLatency: Int = 50,
    val batteryLevel: Int = 100,
    val recentEvents: List<PerformanceEvent> = emptyList(),
)

data class PerformanceEvent(
    val title: String,
    val description: String,
    val severity: EventSeverity,
    val timestamp: Long,
)

enum class EventSeverity {
    LOW,
    MEDIUM,
    HIGH,
}
