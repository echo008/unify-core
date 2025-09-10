package com.unify.ui.components.realtime

import androidx.compose.foundation.layout.*
import kotlinx.datetime.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
 * 实时数据同步管理组件 - 基于Compose的统一UI
 */
@Composable
fun UnifyRealtimeDataSyncComponent(
    dataSyncManager: UnifyRealtimeDataSync,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(SyncTab.OVERVIEW) }
    var newDataKey by remember { mutableStateOf("") }
    var newDataValue by remember { mutableStateOf("") }
    var subscriptionKey by remember { mutableStateOf("") }
    
    val syncState by dataSyncManager.syncState.collectAsState()
    val connectedClients by dataSyncManager.connectedClients.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    // 监听数据更新
    val dataUpdates by dataSyncManager.dataUpdates.collectAsState(initial = null)
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 标题和状态
        SyncHeader(
            syncState = syncState,
            connectedClients = connectedClients,
            dataSyncManager = dataSyncManager
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 标签选择
        SyncTabSelector(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 内容区域
        when (selectedTab) {
            SyncTab.OVERVIEW -> {
                SyncOverviewTab(
                    dataSyncManager = dataSyncManager,
                    modifier = Modifier.weight(1f)
                )
            }
            SyncTab.DATA_MANAGEMENT -> {
                DataManagementTab(
                    dataSyncManager = dataSyncManager,
                    newDataKey = newDataKey,
                    onDataKeyChange = { newDataKey = it },
                    newDataValue = newDataValue,
                    onDataValueChange = { newDataValue = it },
                    modifier = Modifier.weight(1f)
                )
            }
            SyncTab.SUBSCRIPTION -> {
                SubscriptionTab(
                    dataSyncManager = dataSyncManager,
                    subscriptionKey = subscriptionKey,
                    onSubscriptionKeyChange = { subscriptionKey = it },
                    modifier = Modifier.weight(1f)
                )
            }
            SyncTab.MONITORING -> {
                MonitoringTab(
                    dataSyncManager = dataSyncManager,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * 同步头部
 */
@Composable
private fun SyncHeader(
    syncState: SyncState,
    connectedClients: Int,
    dataSyncManager: UnifyRealtimeDataSync,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "实时数据同步",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "连接客户端: $connectedClients",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // 同步状态指示器
        SyncStateChip(syncState = syncState)
    }
}

/**
 * 同步状态芯片
 */
@Composable
private fun SyncStateChip(
    syncState: SyncState,
    modifier: Modifier = Modifier
) {
    val (statusText, statusColor) = when (syncState) {
        SyncState.IDLE -> "空闲" to Color.Gray
        SyncState.SYNCING -> "同步中" to Color.Green
        SyncState.OFFLINE -> "离线" to Color.Red
        SyncState.ERROR -> "错误" to Color.Red
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
 * 同步标签选择器
 */
@Composable
private fun SyncTabSelector(
    selectedTab: SyncTab,
    onTabSelected: (SyncTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SyncTab.values().forEach { tab ->
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
 * 同步概览标签
 */
@Composable
private fun SyncOverviewTab(
    dataSyncManager: UnifyRealtimeDataSync,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // 同步统计卡片
            val stats = dataSyncManager.getSyncStats()
            SyncStatsCard(stats = stats)
        }
        
        item {
            // 快速操作卡片
            QuickSyncActionsCard(dataSyncManager = dataSyncManager)
        }
        
        item {
            // 数据概览卡片
            DataOverviewCard(dataSyncManager = dataSyncManager)
        }
    }
}

/**
 * 数据管理标签
 */
@Composable
private fun DataManagementTab(
    dataSyncManager: UnifyRealtimeDataSync,
    newDataKey: String,
    onDataKeyChange: (String) -> Unit,
    newDataValue: String,
    onDataValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // 添加数据卡片
            AddDataCard(
                dataKey = newDataKey,
                onDataKeyChange = onDataKeyChange,
                dataValue = newDataValue,
                onDataValueChange = onDataValueChange,
                onAddData = {
                    if (newDataKey.isNotBlank() && newDataValue.isNotBlank()) {
                        coroutineScope.launch {
                            dataSyncManager.setData(newDataKey, newDataValue)
                            onDataKeyChange("")
                            onDataValueChange("")
                        }
                    }
                }
            )
        }
        
        item {
            // 批量操作卡片
            BatchDataOperationsCard(dataSyncManager = dataSyncManager)
        }
        
        item {
            // 数据清理卡片
            DataCleanupCard(dataSyncManager = dataSyncManager)
        }
    }
}

/**
 * 订阅标签
 */
@Composable
private fun SubscriptionTab(
    dataSyncManager: UnifyRealtimeDataSync,
    subscriptionKey: String,
    onSubscriptionKeyChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var subscribedKeys by remember { mutableStateOf(setOf<String>()) }
    var subscriptionValues by remember { mutableStateOf(mapOf<String, String?>()) }
    
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // 订阅管理卡片
            SubscriptionManagementCard(
                subscriptionKey = subscriptionKey,
                onSubscriptionKeyChange = onSubscriptionKeyChange,
                onSubscribe = { key ->
                    if (key.isNotBlank() && !subscribedKeys.contains(key)) {
                        subscribedKeys = subscribedKeys + key
                        // 这里应该启动订阅流的收集
                        onSubscriptionKeyChange("")
                    }
                },
                subscribedKeys = subscribedKeys,
                onUnsubscribe = { key ->
                    subscribedKeys = subscribedKeys - key
                    subscriptionValues = subscriptionValues - key
                }
            )
        }
        
        item {
            // 订阅数据显示卡片
            SubscriptionDataCard(
                subscribedKeys = subscribedKeys,
                subscriptionValues = subscriptionValues
            )
        }
    }
}

/**
 * 监控标签
 */
@Composable
private fun MonitoringTab(
    dataSyncManager: UnifyRealtimeDataSync,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // 性能监控卡片
            PerformanceMonitorCard(dataSyncManager = dataSyncManager)
        }
        
        item {
            // 冲突监控卡片
            ConflictMonitorCard(dataSyncManager = dataSyncManager)
        }
        
        item {
            // 网络状态监控卡片
            NetworkStatusCard(dataSyncManager = dataSyncManager)
        }
    }
}

/**
 * 同步统计卡片
 */
@Composable
private fun SyncStatsCard(
    stats: SyncStats,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "同步统计",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem("总条目", stats.totalEntries.toString())
                StatItem("待同步", stats.pendingChanges.toString())
                StatItem("冲突数", stats.conflictCount.toString())
                StatItem("客户端", stats.connectedClients.toString())
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "最后同步: ${formatTimestamp(stats.lastSyncTime)}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
 * 快速同步操作卡片
 */
@Composable
private fun QuickSyncActionsCard(
    dataSyncManager: UnifyRealtimeDataSync,
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
                    onClick = {
                        coroutineScope.launch {
                            dataSyncManager.forceSync()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("强制同步", fontSize = 12.sp)
                }
                
                Button(
                    onClick = {
                        coroutineScope.launch {
                            dataSyncManager.cleanupExpiredData()
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
 * 数据概览卡片
 */
@Composable
private fun DataOverviewCard(
    dataSyncManager: UnifyRealtimeDataSync,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "数据概览",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "实时数据同步功能正在运行中...",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "支持多客户端实时数据同步、冲突解决和版本控制",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 添加数据卡片
 */
@Composable
private fun AddDataCard(
    dataKey: String,
    onDataKeyChange: (String) -> Unit,
    dataValue: String,
    onDataValueChange: (String) -> Unit,
    onAddData: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "添加数据",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = dataKey,
                onValueChange = onDataKeyChange,
                label = { Text("数据键") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("例如: user.name") }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = dataValue,
                onValueChange = onDataValueChange,
                label = { Text("数据值") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("输入JSON或字符串数据...") },
                minLines = 2
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onAddData,
                enabled = dataKey.isNotBlank() && dataValue.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("添加数据")
            }
        }
    }
}

/**
 * 批量数据操作卡片
 */
@Composable
private fun BatchDataOperationsCard(
    dataSyncManager: UnifyRealtimeDataSync,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "批量操作",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        // 批量导入数据功能
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("批量导入", fontSize = 12.sp)
                }
                
                Button(
                    onClick = {
                        // 批量导出数据功能
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("批量导出", fontSize = 12.sp)
                }
            }
        }
    }
}

/**
 * 数据清理卡片
 */
@Composable
private fun DataCleanupCard(
    dataSyncManager: UnifyRealtimeDataSync,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "数据清理",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "清理过期数据和无效缓存",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = {
                    coroutineScope.launch {
                        dataSyncManager.cleanupExpiredData()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("执行清理")
            }
        }
    }
}

/**
 * 订阅管理卡片
 */
@Composable
private fun SubscriptionManagementCard(
    subscriptionKey: String,
    onSubscriptionKeyChange: (String) -> Unit,
    onSubscribe: (String) -> Unit,
    subscribedKeys: Set<String>,
    onUnsubscribe: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "订阅管理",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = subscriptionKey,
                    onValueChange = onSubscriptionKeyChange,
                    label = { Text("订阅键") },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("输入要订阅的数据键") }
                )
                
                Button(
                    onClick = { onSubscribe(subscriptionKey) },
                    enabled = subscriptionKey.isNotBlank()
                ) {
                    Text("订阅")
                }
            }
            
            if (subscribedKeys.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "已订阅的键:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                
                subscribedKeys.forEach { key ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = key,
                            fontSize = 12.sp
                        )
                        
                        TextButton(
                            onClick = { onUnsubscribe(key) }
                        ) {
                            Text("取消", fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

/**
 * 订阅数据卡片
 */
@Composable
private fun SubscriptionDataCard(
    subscribedKeys: Set<String>,
    subscriptionValues: Map<String, String?>,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "订阅数据",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (subscribedKeys.isEmpty()) {
                Text(
                    text = "暂无订阅数据",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                subscribedKeys.forEach { key ->
                    val value = subscriptionValues[key]
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = key,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Text(
                            text = value ?: "等待数据...",
                            fontSize = 12.sp,
                            color = if (value != null) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

/**
 * 性能监控卡片
 */
@Composable
private fun PerformanceMonitorCard(
    dataSyncManager: UnifyRealtimeDataSync,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "性能监控",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "同步延迟、吞吐量和资源使用情况监控",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "性能监控功能开发中...",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 冲突监控卡片
 */
@Composable
private fun ConflictMonitorCard(
    dataSyncManager: UnifyRealtimeDataSync,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "冲突监控",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val stats = dataSyncManager.getSyncStats()
            
            Text(
                text = "检测到的冲突: ${stats.conflictCount}",
                fontSize = 14.sp
            )
            
            Text(
                text = "冲突解决策略: 使用远程版本",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 网络状态卡片
 */
@Composable
private fun NetworkStatusCard(
    dataSyncManager: UnifyRealtimeDataSync,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "网络状态",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val syncState = dataSyncManager.syncState.collectAsState()
            
            Text(
                text = "连接状态: ${
                    when (syncState.value) {
                        SyncState.SYNCING -> "已连接"
                        SyncState.OFFLINE -> "离线"
                        SyncState.ERROR -> "连接错误"
                        SyncState.IDLE -> "空闲"
                    }
                }",
                fontSize = 14.sp
            )
            
            Text(
                text = "网络质量监控和连接优化功能开发中...",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 同步标签枚举
 */
enum class SyncTab(val displayName: String, val icon: String) {
    OVERVIEW("概览", "📊"),
    DATA_MANAGEMENT("数据管理", "📝"),
    SUBSCRIPTION("订阅", "🔔"),
    MONITORING("监控", "📈")
}

/**
 * 格式化时间戳
 */
private fun formatTimestamp(timestamp: Long): String {
    if (timestamp == 0L) return "从未同步"
    
    val now = Clock.System.now().toEpochMilliseconds()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "刚刚"
        diff < 3600_000 -> "${diff / 60_000}分钟前"
        diff < 86400_000 -> "${diff / 3600_000}小时前"
        else -> "${diff / 86400_000}天前"
    }
}
