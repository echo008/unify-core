package com.unify.ui.components.performance

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.unify.ui.LocalUnifyTheme
import com.unify.ui.components.foundation.*
import kotlinx.coroutines.delay
import kotlin.math.*

/**
 * 性能监控数据类型
 */
enum class UnifyPerformanceMetric {
    CPU_USAGE,          // CPU使用率
    MEMORY_USAGE,       // 内存使用率
    GPU_USAGE,          // GPU使用率
    BATTERY_USAGE,      // 电池使用率
    NETWORK_SPEED,      // 网络速度
    FRAME_RATE,         // 帧率
    RENDER_TIME,        // 渲染时间
    LOAD_TIME,          // 加载时间
    CRASH_RATE,         // 崩溃率
    ANR_RATE           // ANR率
}

/**
 * 性能数据
 */
data class UnifyPerformanceData(
    val metric: UnifyPerformanceMetric,
    val value: Float,
    val unit: String,
    val timestamp: Long = System.currentTimeMillis(),
    val threshold: Float = 100f,
    val isHealthy: Boolean = true
)

/**
 * 性能监控配置
 */
data class UnifyPerformanceConfig(
    val enableRealTimeMonitoring: Boolean = true,
    val samplingInterval: Long = 1000L,        // 采样间隔(ms)
    val historySize: Int = 100,                // 历史数据大小
    val enableAlerts: Boolean = true,          // 启用性能告警
    val cpuThreshold: Float = 80f,            // CPU告警阈值
    val memoryThreshold: Float = 85f,         // 内存告警阈值
    val batteryThreshold: Float = 15f,        // 电池告警阈值
    val frameRateThreshold: Float = 30f,      // 帧率告警阈值
    val enableAutoOptimization: Boolean = false // 自动优化
)

/**
 * 性能监控组件
 */
@Composable
fun UnifyPerformanceMonitor(
    modifier: Modifier = Modifier,
    config: UnifyPerformanceConfig = UnifyPerformanceConfig(),
    onPerformanceData: ((UnifyPerformanceData) -> Unit)? = null,
    onAlert: ((String) -> Unit)? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    var isMonitoring by remember { mutableStateOf(false) }
    var performanceData by remember { mutableStateOf<Map<UnifyPerformanceMetric, UnifyPerformanceData>>(emptyMap()) }
    var alerts by remember { mutableStateOf<List<String>>(emptyList()) }
    
    LaunchedEffect(isMonitoring) {
        if (isMonitoring) {
            while (isMonitoring) {
                // 生成模拟性能数据
                val newData = generateMockPerformanceData()
                performanceData = newData.associateBy { it.metric }
                
                newData.forEach { data ->
                    onPerformanceData?.invoke(data)
                    
                    // 检查告警
                    if (config.enableAlerts && !data.isHealthy) {
                        val alertMessage = "性能告警: ${getMetricName(data.metric)} 达到 ${data.value}${data.unit}"
                        alerts = (alerts + alertMessage).takeLast(5)
                        onAlert?.invoke(alertMessage)
                    }
                }
                
                delay(config.samplingInterval)
            }
        }
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription?.let { this.contentDescription = it }
            }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                UnifyText(
                    text = "性能监控",
                    variant = UnifyTextVariant.H6,
                    fontWeight = FontWeight.Bold
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isMonitoring) {
                        val infiniteTransition = rememberInfiniteTransition(label = "monitoring")
                        val alpha by infiniteTransition.animateFloat(
                            initialValue = 0.3f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = EaseInOut),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "alpha"
                        )
                        
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    Color.Green.copy(alpha = alpha),
                                    androidx.compose.foundation.shape.CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    
                    Switch(
                        checked = isMonitoring,
                        onCheckedChange = { isMonitoring = it }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 性能指标网格
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(300.dp)
            ) {
                performanceData.forEach { (metric, data) ->
                    item {
                        PerformanceMetricCard(
                            data = data,
                            isMonitoring = isMonitoring
                        )
                    }
                }
            }
            
            // 告警信息
            if (alerts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Red.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color.Red,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            UnifyText(
                                text = "性能告警",
                                variant = UnifyTextVariant.BODY_MEDIUM,
                                fontWeight = FontWeight.Bold,
                                color = Color.Red
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        alerts.takeLast(3).forEach { alert ->
                            UnifyText(
                                text = "• $alert",
                                variant = UnifyTextVariant.BODY_SMALL,
                                color = Color.Red.copy(alpha = 0.8f)
                            )
                        }
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
private fun PerformanceMetricCard(
    data: UnifyPerformanceData,
    isMonitoring: Boolean,
    modifier: Modifier = Modifier
) {
    val theme = LocalUnifyTheme.current
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                !data.isHealthy -> Color.Red.copy(alpha = 0.1f)
                data.value > data.threshold * 0.8f -> Color.Orange.copy(alpha = 0.1f)
                else -> theme.colors.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = getMetricIcon(data.metric),
                    contentDescription = null,
                    tint = getMetricColor(data.metric, data.isHealthy),
                    modifier = Modifier.size(20.dp)
                )
                
                if (isMonitoring) {
                    val infiniteTransition = rememberInfiniteTransition(label = "metric")
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1500, easing = EaseInOut),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "alpha"
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                Color.Green.copy(alpha = alpha),
                                androidx.compose.foundation.shape.CircleShape
                            )
                    )
                }
            }
            
            Column {
                UnifyText(
                    text = "${data.value.toInt()}${data.unit}",
                    variant = UnifyTextVariant.H6,
                    fontWeight = FontWeight.Bold,
                    color = getMetricColor(data.metric, data.isHealthy)
                )
                UnifyText(
                    text = getMetricName(data.metric),
                    variant = UnifyTextVariant.CAPTION,
                    color = theme.colors.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 性能优化建议组件
 */
@Composable
fun UnifyPerformanceOptimizer(
    performanceData: Map<UnifyPerformanceMetric, UnifyPerformanceData>,
    modifier: Modifier = Modifier,
    onOptimize: ((String) -> Unit)? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    val suggestions = generateOptimizationSuggestions(performanceData)
    
    if (suggestions.isNotEmpty()) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription?.let { this.contentDescription = it }
                }
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.TipsAndUpdates,
                        contentDescription = null,
                        tint = theme.colors.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    UnifyText(
                        text = "性能优化建议",
                        variant = UnifyTextVariant.H6,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                suggestions.forEach { suggestion ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.LightbulbOutline,
                            contentDescription = null,
                            tint = Color.Orange,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        UnifyText(
                            text = suggestion,
                            variant = UnifyTextVariant.BODY_SMALL,
                            modifier = Modifier.weight(1f)
                        )
                        
                        TextButton(
                            onClick = { onOptimize?.invoke(suggestion) }
                        ) {
                            UnifyText(
                                text = "优化",
                                variant = UnifyTextVariant.CAPTION,
                                color = theme.colors.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

// 辅助函数
private fun generateMockPerformanceData(): List<UnifyPerformanceData> {
    return listOf(
        UnifyPerformanceData(
            metric = UnifyPerformanceMetric.CPU_USAGE,
            value = (20..90).random().toFloat(),
            unit = "%",
            threshold = 80f,
            isHealthy = (20..90).random() < 80
        ),
        UnifyPerformanceData(
            metric = UnifyPerformanceMetric.MEMORY_USAGE,
            value = (30..95).random().toFloat(),
            unit = "%",
            threshold = 85f,
            isHealthy = (30..95).random() < 85
        ),
        UnifyPerformanceData(
            metric = UnifyPerformanceMetric.BATTERY_USAGE,
            value = (10..100).random().toFloat(),
            unit = "%",
            threshold = 15f,
            isHealthy = (10..100).random() > 15
        ),
        UnifyPerformanceData(
            metric = UnifyPerformanceMetric.FRAME_RATE,
            value = (15..60).random().toFloat(),
            unit = "fps",
            threshold = 30f,
            isHealthy = (15..60).random() > 30
        )
    )
}

private fun getMetricIcon(metric: UnifyPerformanceMetric): ImageVector {
    return when (metric) {
        UnifyPerformanceMetric.CPU_USAGE -> Icons.Default.Memory
        UnifyPerformanceMetric.MEMORY_USAGE -> Icons.Default.Storage
        UnifyPerformanceMetric.GPU_USAGE -> Icons.Default.GraphicEq
        UnifyPerformanceMetric.BATTERY_USAGE -> Icons.Default.Battery6Bar
        UnifyPerformanceMetric.NETWORK_SPEED -> Icons.Default.NetworkCheck
        UnifyPerformanceMetric.FRAME_RATE -> Icons.Default.Speed
        UnifyPerformanceMetric.RENDER_TIME -> Icons.Default.Timer
        UnifyPerformanceMetric.LOAD_TIME -> Icons.Default.Hourglass
        UnifyPerformanceMetric.CRASH_RATE -> Icons.Default.BugReport
        UnifyPerformanceMetric.ANR_RATE -> Icons.Default.Error
    }
}

private fun getMetricColor(metric: UnifyPerformanceMetric, isHealthy: Boolean): Color {
    return if (!isHealthy) {
        Color.Red
    } else {
        when (metric) {
            UnifyPerformanceMetric.CPU_USAGE -> Color.Blue
            UnifyPerformanceMetric.MEMORY_USAGE -> Color.Green
            UnifyPerformanceMetric.GPU_USAGE -> Color.Purple
            UnifyPerformanceMetric.BATTERY_USAGE -> Color.Orange
            UnifyPerformanceMetric.NETWORK_SPEED -> Color.Cyan
            UnifyPerformanceMetric.FRAME_RATE -> Color.Magenta
            UnifyPerformanceMetric.RENDER_TIME -> Color.Brown
            UnifyPerformanceMetric.LOAD_TIME -> Color.Gray
            UnifyPerformanceMetric.CRASH_RATE -> Color.Red
            UnifyPerformanceMetric.ANR_RATE -> Color.Red
        }
    }
}

private fun getMetricName(metric: UnifyPerformanceMetric): String {
    return when (metric) {
        UnifyPerformanceMetric.CPU_USAGE -> "CPU使用率"
        UnifyPerformanceMetric.MEMORY_USAGE -> "内存使用率"
        UnifyPerformanceMetric.GPU_USAGE -> "GPU使用率"
        UnifyPerformanceMetric.BATTERY_USAGE -> "电池电量"
        UnifyPerformanceMetric.NETWORK_SPEED -> "网络速度"
        UnifyPerformanceMetric.FRAME_RATE -> "帧率"
        UnifyPerformanceMetric.RENDER_TIME -> "渲染时间"
        UnifyPerformanceMetric.LOAD_TIME -> "加载时间"
        UnifyPerformanceMetric.CRASH_RATE -> "崩溃率"
        UnifyPerformanceMetric.ANR_RATE -> "ANR率"
    }
}

private fun generateOptimizationSuggestions(
    performanceData: Map<UnifyPerformanceMetric, UnifyPerformanceData>
): List<String> {
    val suggestions = mutableListOf<String>()
    
    performanceData.forEach { (metric, data) ->
        if (!data.isHealthy) {
            when (metric) {
                UnifyPerformanceMetric.CPU_USAGE -> {
                    suggestions.add("CPU使用率过高，建议关闭后台应用或降低动画复杂度")
                }
                UnifyPerformanceMetric.MEMORY_USAGE -> {
                    suggestions.add("内存使用率过高，建议清理缓存或减少同时加载的资源")
                }
                UnifyPerformanceMetric.BATTERY_USAGE -> {
                    suggestions.add("电池电量不足，建议启用省电模式或降低屏幕亮度")
                }
                UnifyPerformanceMetric.FRAME_RATE -> {
                    suggestions.add("帧率过低，建议降低渲染质量或关闭特效")
                }
                else -> {}
            }
        }
    }
    
    return suggestions
}
