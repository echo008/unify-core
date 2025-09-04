package com.unify.core.performance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.unify.core.components.UnifyButton
import com.unify.core.components.UnifyButtonType
import com.unify.core.components.UnifyCard
import com.unify.core.components.UnifyCardType
import com.unify.core.components.UnifyColumn
import com.unify.core.components.UnifyFeedbackBanner
import com.unify.core.components.UnifyFeedbackType
import com.unify.core.components.UnifyProgressIndicator
import com.unify.core.components.UnifyRow
import com.unify.core.components.UnifySection
import com.unify.core.components.UnifySpacing
import com.unify.core.error.UnifyErrorHandling
import com.unify.core.logging.UnifyLog
import kotlinx.coroutines.delay

/**
 * Unify性能监控演示应用
 * 展示性能监控、错误处理和日志系统
 */
@Composable
fun UnifyPerformanceDemo() {
    val performanceMonitor = remember { UnifyPerformance.monitor }
    val metrics by performanceMonitor.metrics.collectAsState()
    val alerts by performanceMonitor.alerts.collectAsState()
    val isMonitoring by performanceMonitor.isMonitoring.collectAsState()
    
    var performanceLevel by remember { mutableStateOf<UnifyPerformanceLevel?>(null) }
    var performanceReport by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(isMonitoring) {
        if (isMonitoring) {
            while (isMonitoring) {
                val levelResult = performanceMonitor.getPerformanceLevel()
                if (levelResult is com.unify.core.types.UnifyResult.Success) {
                    performanceLevel = levelResult.data
                }
                delay(2000)
            }
        }
    }
    
    UnifyColumn(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        spacing = UnifySpacing.LARGE
    ) {
        // 标题
        Text(
            text = "⚡ Unify 性能监控系统",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        // 监控控制
        UnifySection(title = "监控控制") {
            UnifyRow(
                spacing = UnifySpacing.MEDIUM,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                UnifyButton(
                    onClick = {
                        if (isMonitoring) {
                            kotlinx.coroutines.GlobalScope.launch {
                                UnifyPerformance.stopMonitoring()
                            }
                        } else {
                            kotlinx.coroutines.GlobalScope.launch {
                                UnifyPerformance.startMonitoring()
                            }
                        }
                    },
                    text = if (isMonitoring) "停止监控" else "开始监控",
                    type = if (isMonitoring) UnifyButtonType.OUTLINED else UnifyButtonType.FILLED,
                    icon = Icons.Default.Speed
                )
                
                UnifyButton(
                    onClick = {
                        kotlinx.coroutines.GlobalScope.launch {
                            val result = performanceMonitor.exportMetrics()
                            if (result is com.unify.core.types.UnifyResult.Success) {
                                performanceReport = result.data
                            }
                        }
                    },
                    text = "导出报告",
                    type = UnifyButtonType.OUTLINED
                )
            }
        }
        
        // 性能指标
        UnifySection(title = "实时性能指标") {
            UnifyCard(type = UnifyCardType.FILLED) {
                UnifyColumn(
                    modifier = Modifier.padding(16.dp),
                    spacing = UnifySpacing.MEDIUM
                ) {
                    // 性能等级
                    performanceLevel?.let { level ->
                        val levelColor = when (level) {
                            UnifyPerformanceLevel.EXCELLENT -> Color.Green
                            UnifyPerformanceLevel.GOOD -> Color.Blue
                            UnifyPerformanceLevel.FAIR -> Color.Yellow
                            UnifyPerformanceLevel.POOR -> Color.Orange
                            UnifyPerformanceLevel.CRITICAL -> Color.Red
                        }
                        
                        UnifyFeedbackBanner(
                            message = "性能等级: ${level.name}",
                            type = when (level) {
                                UnifyPerformanceLevel.EXCELLENT, UnifyPerformanceLevel.GOOD -> UnifyFeedbackType.SUCCESS
                                UnifyPerformanceLevel.FAIR -> UnifyFeedbackType.INFO
                                UnifyPerformanceLevel.POOR -> UnifyFeedbackType.WARNING
                                UnifyPerformanceLevel.CRITICAL -> UnifyFeedbackType.ERROR
                            }
                        )
                    }
                    
                    // CPU使用率
                    MetricItem(
                        label = "CPU使用率",
                        value = "${String.format("%.1f", metrics.cpuUsage)}%",
                        progress = metrics.cpuUsage / 100f,
                        icon = Icons.Default.Speed
                    )
                    
                    // 内存使用
                    val memoryPercent = if (metrics.memoryTotal > 0) {
                        (metrics.memoryUsage.toFloat() / metrics.memoryTotal) * 100f
                    } else 0f
                    
                    MetricItem(
                        label = "内存使用",
                        value = "${metrics.memoryUsage / (1024 * 1024)}MB / ${metrics.memoryTotal / (1024 * 1024)}MB",
                        progress = memoryPercent / 100f,
                        icon = Icons.Default.Memory
                    )
                    
                    // 帧率
                    MetricItem(
                        label = "帧率",
                        value = "${String.format("%.1f", metrics.frameRate)} FPS",
                        progress = (metrics.frameRate / 60f).coerceIn(0f, 1f),
                        icon = Icons.Default.Speed
                    )
                    
                    // 电池电量
                    MetricItem(
                        label = "电池电量",
                        value = "${String.format("%.1f", metrics.batteryLevel)}%",
                        progress = metrics.batteryLevel / 100f,
                        icon = Icons.Default.BatteryAlert
                    )
                }
            }
        }
        
        // 性能警告
        if (alerts.isNotEmpty()) {
            UnifySection(title = "性能警告") {
                alerts.forEach { alert ->
                    UnifyFeedbackBanner(
                        message = alert.message,
                        type = when (alert.level) {
                            UnifyPerformanceLevel.CRITICAL -> UnifyFeedbackType.ERROR
                            UnifyPerformanceLevel.POOR -> UnifyFeedbackType.WARNING
                            else -> UnifyFeedbackType.INFO
                        },
                        icon = Icons.Default.Warning
                    )
                }
            }
        }
        
        // 错误处理演示
        UnifySection(title = "错误处理演示") {
            UnifyRow(
                spacing = UnifySpacing.MEDIUM,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                UnifyButton(
                    onClick = {
                        kotlinx.coroutines.GlobalScope.launch {
                            try {
                                throw RuntimeException("这是一个测试异常")
                            } catch (e: Exception) {
                                UnifyErrorHandling.handleError(e, "演示错误")
                            }
                        }
                    },
                    text = "触发错误",
                    type = UnifyButtonType.OUTLINED
                )
                
                UnifyButton(
                    onClick = {
                        kotlinx.coroutines.GlobalScope.launch {
                            UnifyLog.i("Demo", "这是一条信息日志")
                            UnifyLog.w("Demo", "这是一条警告日志")
                            UnifyLog.e("Demo", "这是一条错误日志")
                        }
                    },
                    text = "记录日志",
                    type = UnifyButtonType.OUTLINED
                )
            }
        }
        
        // 性能报告
        performanceReport?.let { report ->
            UnifySection(title = "性能报告") {
                UnifyCard(type = UnifyCardType.OUTLINED) {
                    UnifyColumn(
                        modifier = Modifier.padding(16.dp),
                        spacing = UnifySpacing.SMALL
                    ) {
                        Text(
                            text = report,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        UnifyButton(
                            onClick = { performanceReport = null },
                            text = "关闭报告",
                            type = UnifyButtonType.TEXT
                        )
                    }
                }
            }
        }
        
        // 系统信息
        UnifySection(title = "系统信息") {
            UnifyCard(type = UnifyCardType.OUTLINED) {
                UnifyColumn(
                    modifier = Modifier.padding(16.dp),
                    spacing = UnifySpacing.SMALL
                ) {
                    InfoItem("时间戳", metrics.timestamp.toString())
                    InfoItem("渲染时间", "${metrics.renderTime}ms")
                    InfoItem("网络延迟", "${metrics.networkLatency}ms")
                    InfoItem("监控状态", if (isMonitoring) "运行中" else "已停止")
                }
            }
        }
    }
}

@Composable
private fun MetricItem(
    label: String,
    value: String,
    progress: Float,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    UnifyColumn(spacing = UnifySpacing.SMALL) {
        UnifyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            UnifyRow(spacing = UnifySpacing.SMALL) {
                androidx.compose.material3.Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        UnifyProgressIndicator(
            progress = progress.coerceIn(0f, 1f),
            isLinear = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun InfoItem(
    label: String,
    value: String
) {
    UnifyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
