package com.unify.ui.components.realtime

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
import androidx.compose.ui.unit.sp
import com.unify.core.realtime.*
import kotlinx.coroutines.launch

/**
 * 离线消息队列管理组件 - 基于Compose的统一UI
 */
@Composable
fun UnifyOfflineMessageQueueComponent(
    messageQueue: UnifyOfflineMessageQueue,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(QueueTab.OVERVIEW) }
    var newMessageContent by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(MessagePriority.NORMAL) }
    
    val queueState by messageQueue.queueState.collectAsState()
    val queueSize by messageQueue.queueSize.collectAsState()
    val processingStats by messageQueue.processingStats.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 标题和状态
        QueueHeader(
            queueState = queueState,
            queueSize = queueSize,
            processingStats = processingStats
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 标签选择
        QueueTabSelector(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 内容区域
        when (selectedTab) {
            QueueTab.OVERVIEW -> {
                QueueOverviewTab(
                    messageQueue = messageQueue,
                    modifier = Modifier.weight(1f)
                )
            }
            QueueTab.MESSAGES -> {
                MessagesTab(
                    messageQueue = messageQueue,
                    newMessageContent = newMessageContent,
                    onMessageContentChange = { newMessageContent = it },
                    selectedPriority = selectedPriority,
                    onPriorityChange = { selectedPriority = it },
                    modifier = Modifier.weight(1f)
                )
            }
            QueueTab.STATISTICS -> {
                StatisticsTab(
                    messageQueue = messageQueue,
                    modifier = Modifier.weight(1f)
                )
            }
            QueueTab.SETTINGS -> {
                SettingsTab(
                    messageQueue = messageQueue,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * 队列头部
 */
@Composable
private fun QueueHeader(
    queueState: QueueState,
    queueSize: Int,
    processingStats: ProcessingStats,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "离线消息队列",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = com.unify.core.utils.UnifyStringUtils.format("离线消息队列: %d条待发送", queueSize),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        QueueStateChip(queueState = queueState)
    }
}

/**
 * 队列状态芯片
 */
@Composable
private fun QueueStateChip(
    queueState: QueueState,
    modifier: Modifier = Modifier
) {
    val (statusText, statusColor) = when (queueState) {
        QueueState.IDLE -> "空闲" to Color.Gray
        QueueState.PROCESSING -> "处理中" to Color.Green
        QueueState.PAUSED -> "暂停" to Color(0xFFFF9800)
        QueueState.OFFLINE -> "离线" to Color.Red
        QueueState.ERROR -> "错误" to Color.Red
    }
    
    AssistChip(
        onClick = { },
        label = { Text(statusText, fontSize = 12.sp) },
        modifier = modifier,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = statusColor.copy(alpha = 0.1f),
            labelColor = statusColor
        )
    )
}

/**
 * 队列标签选择器
 */
@Composable
private fun QueueTabSelector(
    selectedTab: QueueTab,
    onTabSelected: (QueueTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QueueTab.values().forEach { tab ->
            FilterChip(
                onClick = { onTabSelected(tab) },
                label = { 
                    Text(
                        text = "${tab.icon} ${tab.displayName}",
                        fontSize = 12.sp
                    )
                },
                selected = selectedTab == tab
            )
        }
    }
}

/**
 * 队列概览标签
 */
@Composable
private fun QueueOverviewTab(
    messageQueue: UnifyOfflineMessageQueue,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            val queueStatus = messageQueue.getQueueStatus()
            QueueStatusCard(queueStatus = queueStatus)
        }
        
        item {
            QuickQueueActionsCard(messageQueue = messageQueue)
        }
        
        item {
            val statistics = messageQueue.getStatistics()
            QueueStatisticsCard(statistics = statistics)
        }
    }
}

/**
 * 消息标签
 */
@Composable
private fun MessagesTab(
    messageQueue: UnifyOfflineMessageQueue,
    newMessageContent: String,
    onMessageContentChange: (String) -> Unit,
    selectedPriority: MessagePriority,
    onPriorityChange: (MessagePriority) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AddMessageCard(
                messageContent = newMessageContent,
                onMessageContentChange = onMessageContentChange,
                selectedPriority = selectedPriority,
                onPriorityChange = onPriorityChange,
                onAddMessage = {
                    if (newMessageContent.isNotBlank()) {
                        coroutineScope.launch {
                            messageQueue.enqueue(newMessageContent, selectedPriority)
                            onMessageContentChange("")
                            println(com.unify.core.utils.UnifyStringUtils.format("消息发送失败: %s...", newMessageContent.take(50)))
                        }
                    }
                }
            )
        }
        
        item {
            MessageManagementCard(messageQueue = messageQueue)
        }
    }
}

/**
 * 统计标签
 */
@Composable
private fun StatisticsTab(
    messageQueue: UnifyOfflineMessageQueue,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            val statistics = messageQueue.getStatistics()
            DetailedStatisticsCard(statistics = statistics)
        }
        
        item {
            PerformanceMetricsCard(statistics = messageQueue.getStatistics())
        }
    }
}

/**
 * 设置标签
 */
@Composable
private fun SettingsTab(
    messageQueue: UnifyOfflineMessageQueue,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            QueueControlCard(messageQueue = messageQueue)
        }
        
        item {
            MaintenanceCard(messageQueue = messageQueue)
        }
    }
}

/**
 * 队列状态卡片
 */
@Composable
private fun QueueStatusCard(
    queueStatus: QueueStatus,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "队列状态",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem("总消息", queueStatus.totalMessages.toString())
                StatItem("高优先级", queueStatus.highPriorityMessages.toString())
                StatItem("失败消息", queueStatus.failedMessages.toString())
                StatItem("已发送", queueStatus.sentMessages.toString())
            }
        }
    }
}

/**
 * 统计项
 */
@Composable
private fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 快速队列操作卡片
 */
@Composable
private fun QuickQueueActionsCard(
    messageQueue: UnifyOfflineMessageQueue,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "快速操作",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { messageQueue.resumeProcessing() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("恢复处理", fontSize = 12.sp)
                }
                
                Button(
                    onClick = { messageQueue.pauseProcessing() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("暂停处理", fontSize = 12.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            messageQueue.requeueFailedMessages()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("重试失败", fontSize = 12.sp)
                }
                
                Button(
                    onClick = {
                        coroutineScope.launch {
                            messageQueue.cleanupExpiredMessages()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("清理过期", fontSize = 12.sp)
                }
            }
        }
    }
}

/**
 * 队列统计卡片
 */
@Composable
private fun QueueStatisticsCard(
    statistics: QueueStatistics,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "性能统计",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = com.unify.core.utils.UnifyStringUtils.format("成功率: %.1f%%", statistics.successRate),
                fontSize = 14.sp
            )
            
            Text(
                text = com.unify.core.utils.UnifyStringUtils.format("数据大小: %.2fKB", (statistics.totalEnqueued * 1024) / 1024.0),
                fontSize = 14.sp
            )
            
            Text(
                text = "平均处理时间: ${statistics.averageProcessingTime}ms",
                fontSize = 14.sp
            )
            
            Text(
                text = "吞吐量: ${statistics.queueThroughput} 消息/秒",
                fontSize = 14.sp
            )
        }
    }
}

/**
 * 添加消息卡片
 */
@Composable
private fun AddMessageCard(
    messageContent: String,
    onMessageContentChange: (String) -> Unit,
    selectedPriority: MessagePriority,
    onPriorityChange: (MessagePriority) -> Unit,
    onAddMessage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "添加消息",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = messageContent,
                onValueChange = onMessageContentChange,
                label = { Text("消息内容") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("输入要发送的消息...") },
                minLines = 2
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("优先级:", fontSize = 12.sp)
                
                MessagePriority.values().forEach { priority ->
                    FilterChip(
                        onClick = { onPriorityChange(priority) },
                        label = { 
                            Text(
                                text = when (priority) {
                                    MessagePriority.HIGH -> "高"
                                    MessagePriority.NORMAL -> "中"
                                    MessagePriority.LOW -> "低"
                                },
                                fontSize = 10.sp
                            )
                        },
                        selected = selectedPriority == priority
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onAddMessage,
                enabled = messageContent.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("添加到队列")
            }
        }
    }
}

/**
 * 消息管理卡片
 */
@Composable
private fun MessageManagementCard(
    messageQueue: UnifyOfflineMessageQueue,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "消息管理",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "批量操作和消息管理功能",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = {
                    coroutineScope.launch {
                        messageQueue.clearQueue()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("清空队列")
            }
        }
    }
}

/**
 * 详细统计卡片
 */
@Composable
private fun DetailedStatisticsCard(
    statistics: QueueStatistics,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "详细统计",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            StatRow("总入队消息", statistics.totalEnqueued.toString())
            StatRow("成功发送", statistics.totalSent.toString())
            StatRow("发送失败", statistics.totalFailed.toString())
            StatRow("过期消息", statistics.totalExpired.toString())
            StatRow("当前队列", statistics.currentQueueSize.toString())
            StatRow("失败队列", statistics.failedQueueSize.toString())
        }
    }
}

/**
 * 统计行
 */
@Composable
private fun StatRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
    Spacer(modifier = Modifier.height(4.dp))
}

/**
 * 性能指标卡片
 */
@Composable
private fun PerformanceMetricsCard(
    statistics: QueueStatistics,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "性能指标",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            StatRow("成功率", com.unify.core.utils.UnifyStringUtils.format("%.2f%%", statistics.successRate))
            StatRow("平均处理时间", "${statistics.averageProcessingTime}ms")
            StatRow("队列吞吐量", "${statistics.queueThroughput} 消息/秒")
        }
    }
}

/**
 * 队列控制卡片
 */
@Composable
private fun QueueControlCard(
    messageQueue: UnifyOfflineMessageQueue,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "队列控制",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { messageQueue.pauseProcessing() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("暂停", fontSize = 12.sp)
                }
                
                Button(
                    onClick = { messageQueue.resumeProcessing() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("恢复", fontSize = 12.sp)
                }
            }
        }
    }
}

/**
 * 维护卡片
 */
@Composable
private fun MaintenanceCard(
    messageQueue: UnifyOfflineMessageQueue,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "维护操作",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = {
                    coroutineScope.launch {
                        messageQueue.cleanupExpiredMessages()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("清理过期消息")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = {
                    coroutineScope.launch {
                        messageQueue.clearQueue()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("清空所有队列")
            }
        }
    }
}

/**
 * 队列标签枚举
 */
enum class QueueTab(val displayName: String, val icon: String) {
    OVERVIEW("概览", "📊"),
    MESSAGES("消息", "📝"),
    STATISTICS("统计", "📈"),
    SETTINGS("设置", "⚙️")
}
