package com.unify.core.dynamic

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*

/**
 * 性能指标卡片
 */
@Composable
fun PerformanceMetricsCard(metrics: Map<String, Double>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "性能指标",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricItem(
                    label = "CPU使用率",
                    value = "${(metrics["cpu_usage"] ?: 0.0).toInt()}%",
                    color = getMetricColor(metrics["cpu_usage"] ?: 0.0, 80.0)
                )
                MetricItem(
                    label = "内存使用",
                    value = "${(metrics["memory_usage"] ?: 0.0).toInt()}MB",
                    color = getMetricColor(metrics["memory_usage"] ?: 0.0, 512.0)
                )
                MetricItem(
                    label = "网络延迟",
                    value = "${(metrics["network_latency"] ?: 0.0).toInt()}ms",
                    color = getMetricColor(metrics["network_latency"] ?: 0.0, 200.0)
                )
                MetricItem(
                    label = "组件数量",
                    value = "${(metrics["component_count"] ?: 0.0).toInt()}",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * 指标项
 */
@Composable
private fun MetricItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 获取指标颜色
 */
private fun getMetricColor(value: Double, threshold: Double): Color {
    return when {
        value < threshold * 0.7 -> Color.Green
        value < threshold -> Color.Orange
        else -> Color.Red
    }
}

/**
 * 快速操作卡片
 */
@Composable
fun QuickActionsCard(dynamicEngine: UnifyDynamicEngine) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "快速操作",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        GlobalScope.launch {
                            dynamicEngine.checkForUpdates()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("检查更新")
                }
                
                OutlinedButton(
                    onClick = {
                        GlobalScope.launch {
                            dynamicEngine.syncConfigurations()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Sync, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("同步配置")
                }
                
                OutlinedButton(
                    onClick = {
                        GlobalScope.launch {
                            dynamicEngine.clearCache()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Clear, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("清理缓存")
                }
            }
        }
    }
}

/**
 * 组件卡片
 */
@Composable
fun ComponentCard(
    componentId: String,
    factory: ComponentFactory,
    onUnregister: () -> Unit,
    onReload: () -> Unit
) {
    var showDetails by remember { mutableStateOf(false) }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = componentId,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = factory.getMetadata().description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "版本: ${factory.getMetadata().version}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row {
                    IconButton(onClick = { showDetails = !showDetails }) {
                        Icon(
                            if (showDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "详情"
                        )
                    }
                    IconButton(onClick = onReload) {
                        Icon(Icons.Default.Refresh, contentDescription = "重新加载")
                    }
                    IconButton(onClick = onUnregister) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "卸载",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            if (showDetails) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))
                
                val metadata = factory.getMetadata()
                Text(
                    text = "组件详情:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text("名称: ${metadata.name}")
                Text("版本: ${metadata.version}")
                Text("描述: ${metadata.description}")
                Text("类型: ${metadata.type}")
                Text("依赖: ${metadata.dependencies.joinToString(", ")}")
            }
        }
    }
}

/**
 * 配置卡片
 */
@Composable
fun ConfigurationCard(
    key: String,
    value: String,
    onUpdate: (String) -> Unit,
    onDelete: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var editValue by remember { mutableStateOf(value) }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = key,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (isEditing) {
                        OutlinedTextField(
                            value = editValue,
                            onValueChange = { editValue = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = false,
                            maxLines = 3
                        )
                    } else {
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Row {
                    if (isEditing) {
                        IconButton(
                            onClick = {
                                onUpdate(editValue)
                                isEditing = false
                            }
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "保存")
                        }
                        IconButton(
                            onClick = {
                                editValue = value
                                isEditing = false
                            }
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "取消")
                        }
                    } else {
                        IconButton(
                            onClick = { isEditing = true }
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "编辑")
                        }
                        IconButton(
                            onClick = onDelete
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "删除",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 更新历史卡片
 */
@Composable
fun UpdateHistoryCard(
    update: UpdateHistory,
    onRollback: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "版本 ${update.version}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = update.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = formatTimestamp(update.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row {
                    Chip(
                        onClick = { },
                        label = { Text(update.status) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = getStatusColor(update.status)
                        )
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    if (update.status == "SUCCESS") {
                        OutlinedButton(
                            onClick = onRollback
                        ) {
                            Icon(Icons.Default.Undo, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("回滚")
                        }
                    }
                }
            }
        }
    }
}

/**
 * 实时指标卡片
 */
@Composable
fun RealTimeMetricsCard(metrics: Map<String, Double>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "实时指标",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                metrics.forEach { (key, value) ->
                    MetricProgressBar(
                        label = key,
                        value = value,
                        maxValue = getMaxValueForMetric(key)
                    )
                }
            }
        }
    }
}

/**
 * 指标进度条
 */
@Composable
private fun MetricProgressBar(label: String, value: Double, maxValue: Double) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${value.toInt()}/${maxValue.toInt()}",
                style = MaterialTheme.typography.bodySmall
            )
        }
        
        LinearProgressIndicator(
            progress = (value / maxValue).coerceIn(0.0, 1.0).toFloat(),
            modifier = Modifier.fillMaxWidth(),
            color = getMetricColor(value, maxValue * 0.8)
        )
    }
}

/**
 * 获取指标最大值
 */
private fun getMaxValueForMetric(key: String): Double {
    return when (key) {
        "cpu_usage" -> 100.0
        "memory_usage" -> 1024.0
        "network_latency" -> 500.0
        "component_count" -> 100.0
        else -> 100.0
    }
}

/**
 * 系统健康卡片
 */
@Composable
fun SystemHealthCard(status: SystemStatus) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "系统健康",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                HealthIndicator("引擎", status.engineStatus)
                HealthIndicator("网络", status.networkStatus)
                HealthIndicator("存储", status.storageStatus)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "最后更新: ${formatTimestamp(status.lastUpdate)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 健康指示器
 */
@Composable
private fun HealthIndicator(label: String, status: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (status.uppercase()) {
                    "RUNNING", "ONLINE", "HEALTHY" -> Icons.Default.CheckCircle
                    "STOPPED", "OFFLINE", "ERROR" -> Icons.Default.Error
                    else -> Icons.Default.Warning
                },
                contentDescription = null,
                tint = getStatusColor(status),
                modifier = Modifier.size(32.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = status,
            style = MaterialTheme.typography.bodySmall,
            color = getStatusColor(status)
        )
    }
}

/**
 * 告警卡片
 */
@Composable
fun AlertsCard(dynamicEngine: UnifyDynamicEngine) {
    val alerts by dynamicEngine.alerts.collectAsState()
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "系统告警",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (alerts.isEmpty()) {
                Text(
                    text = "无告警信息",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    alerts.take(5).forEach { alert ->
                        AlertItem(alert)
                    }
                }
            }
        }
    }
}

/**
 * 告警项
 */
@Composable
private fun AlertItem(alert: Alert) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = when (alert.level) {
                "ERROR" -> Icons.Default.Error
                "WARNING" -> Icons.Default.Warning
                else -> Icons.Default.Info
            },
            contentDescription = null,
            tint = when (alert.level) {
                "ERROR" -> Color.Red
                "WARNING" -> Color.Orange
                else -> Color.Blue
            },
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = alert.message,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = formatTimestamp(alert.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 日志条目卡片
 */
@Composable
fun LogEntryCard(log: LogEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (log.level) {
                "ERROR" -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                "WARN" -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = when (log.level) {
                    "ERROR" -> Icons.Default.Error
                    "WARN" -> Icons.Default.Warning
                    "INFO" -> Icons.Default.Info
                    else -> Icons.Default.Circle
                },
                contentDescription = null,
                tint = when (log.level) {
                    "ERROR" -> Color.Red
                    "WARN" -> Color.Orange
                    "INFO" -> Color.Blue
                    else -> Color.Gray
                },
                modifier = Modifier.size(16.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = log.level,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatTimestamp(log.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Text(
                    text = log.message,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                if (log.source.isNotEmpty()) {
                    Text(
                        text = "来源: ${log.source}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * 添加组件对话框
 */
@Composable
fun AddComponentDialog(
    onDismiss: () -> Unit,
    onAdd: (ComponentData) -> Unit
) {
    var componentName by remember { mutableStateOf("") }
    var componentVersion by remember { mutableStateOf("1.0.0") }
    var componentDescription by remember { mutableStateOf("") }
    var componentCode by remember { mutableStateOf("") }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.width(400.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "添加组件",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = componentName,
                    onValueChange = { componentName = it },
                    label = { Text("组件名称") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = componentVersion,
                    onValueChange = { componentVersion = it },
                    label = { Text("版本") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = componentDescription,
                    onValueChange = { componentDescription = it },
                    label = { Text("描述") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = componentCode,
                    onValueChange = { componentCode = it },
                    label = { Text("组件代码") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            val componentData = ComponentData(
                                metadata = ComponentMetadata(
                                    name = componentName,
                                    version = componentVersion,
                                    description = componentDescription
                                ),
                                code = componentCode
                            )
                            onAdd(componentData)
                        },
                        enabled = componentName.isNotEmpty() && componentCode.isNotEmpty()
                    ) {
                        Text("添加")
                    }
                }
            }
        }
    }
}

/**
 * 添加配置对话框
 */
@Composable
fun AddConfigurationDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {
    var key by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.width(400.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "添加配置",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = key,
                    onValueChange = { key = it },
                    label = { Text("配置键") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text("配置值") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = { onAdd(key, value) },
                        enabled = key.isNotEmpty() && value.isNotEmpty()
                    ) {
                        Text("添加")
                    }
                }
            }
        }
    }
}

/**
 * 告警数据类
 */
@Serializable
data class Alert(
    val level: String,
    val message: String,
    val timestamp: Long,
    val source: String = ""
)

/**
 * 工具函数
 */
private fun formatTimestamp(timestamp: Long): String {
    // 简化的时间格式化，实际应用中可使用更完善的时间库
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60000 -> "刚刚"
        diff < 3600000 -> "${diff / 60000}分钟前"
        diff < 86400000 -> "${diff / 3600000}小时前"
        else -> "${diff / 86400000}天前"
    }
}
