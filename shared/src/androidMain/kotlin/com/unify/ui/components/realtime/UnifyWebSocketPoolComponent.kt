package com.unify.ui.components.realtime

import androidx.compose.foundation.layout.*
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
 * WebSocket连接池管理组件 - 基于Compose的统一UI
 */
@Composable
fun UnifyWebSocketPoolComponent(
    webSocketPool: UnifyWebSocketPool,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(PoolTab.OVERVIEW) }
    var newConnectionId by remember { mutableStateOf("") }
    var newConnectionUrl by remember { mutableStateOf("") }
    var broadcastMessage by remember { mutableStateOf("") }
    
    val poolState by webSocketPool.poolState.collectAsState()
    val activeConnections by webSocketPool.activeConnections.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 标题和状态
        PoolHeader(
            poolState = poolState,
            activeConnections = activeConnections,
            webSocketPool = webSocketPool
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 标签选择
        TabSelector(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 内容区域
        when (selectedTab) {
            PoolTab.OVERVIEW -> {
                OverviewTab(
                    webSocketPool = webSocketPool,
                    modifier = Modifier.weight(1f)
                )
            }
            PoolTab.CONNECTIONS -> {
                ConnectionsTab(
                    webSocketPool = webSocketPool,
                    modifier = Modifier.weight(1f)
                )
            }
            PoolTab.MANAGEMENT -> {
                ManagementTab(
                    webSocketPool = webSocketPool,
                    newConnectionId = newConnectionId,
                    onConnectionIdChange = { newConnectionId = it },
                    newConnectionUrl = newConnectionUrl,
                    onConnectionUrlChange = { newConnectionUrl = it },
                    broadcastMessage = broadcastMessage,
                    onBroadcastMessageChange = { broadcastMessage = it },
                    modifier = Modifier.weight(1f)
                )
            }
            PoolTab.MONITORING -> {
                MonitoringTab(
                    webSocketPool = webSocketPool,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * 连接池头部
 */
@Composable
private fun PoolHeader(
    poolState: WebSocketPoolState,
    activeConnections: Int,
    webSocketPool: UnifyWebSocketPool,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "WebSocket连接池",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "活跃连接: $activeConnections",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // 池状态指示器
        PoolStateChip(poolState = poolState)
    }
}

/**
 * 池状态芯片
 */
@Composable
private fun PoolStateChip(
    poolState: WebSocketPoolState,
    modifier: Modifier = Modifier
) {
    val (statusText, statusColor) = when (poolState) {
        WebSocketPoolState.IDLE -> "空闲" to Color.Gray
        WebSocketPoolState.DISCONNECTED -> "断开" to Color.Red
        WebSocketPoolState.PARTIALLY_CONNECTED -> "部分连接" to Color(0xFFFF9800)
        WebSocketPoolState.FULLY_CONNECTED -> "全部连接" to Color.Green
        WebSocketPoolState.ERROR -> "错误" to Color.Red
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
 * 标签选择器
 */
@Composable
private fun TabSelector(
    selectedTab: PoolTab,
    onTabSelected: (PoolTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PoolTab.values().forEach { tab ->
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
 * 概览标签
 */
@Composable
private fun OverviewTab(
    webSocketPool: UnifyWebSocketPool,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // 统计信息卡片
            val stats = webSocketPool.getPoolStats()
            StatsCard(stats = stats)
        }
        
        item {
            // 连接状态概览
            ConnectionOverviewCard(webSocketPool = webSocketPool)
        }
        
        item {
            // 快速操作
            QuickActionsCard(webSocketPool = webSocketPool)
        }
    }
}

/**
 * 连接标签
 */
@Composable
private fun ConnectionsTab(
    webSocketPool: UnifyWebSocketPool,
    modifier: Modifier = Modifier
) {
    val connectionStates = webSocketPool.getAllConnectionStates()
    
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (connectionStates.isEmpty()) {
            item {
                EmptyConnectionsCard()
            }
        } else {
            items(connectionStates.toList()) { (connectionId, state) ->
                ConnectionCard(
                    connectionId = connectionId,
                    state = state,
                    webSocketPool = webSocketPool
                )
            }
        }
    }
}

/**
 * 管理标签
 */
@Composable
private fun ManagementTab(
    webSocketPool: UnifyWebSocketPool,
    newConnectionId: String,
    onConnectionIdChange: (String) -> Unit,
    newConnectionUrl: String,
    onConnectionUrlChange: (String) -> Unit,
    broadcastMessage: String,
    onBroadcastMessageChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // 添加连接
            AddConnectionCard(
                connectionId = newConnectionId,
                onConnectionIdChange = onConnectionIdChange,
                connectionUrl = newConnectionUrl,
                onConnectionUrlChange = onConnectionUrlChange,
                onAddConnection = {
                    if (newConnectionId.isNotBlank() && newConnectionUrl.isNotBlank()) {
                        coroutineScope.launch {
                            webSocketPool.addConnection(newConnectionId, newConnectionUrl)
                            onConnectionIdChange("")
                            onConnectionUrlChange("")
                        }
                    }
                }
            )
        }
        
        item {
            // 广播消息
            BroadcastCard(
                message = broadcastMessage,
                onMessageChange = onBroadcastMessageChange,
                onBroadcast = {
                    if (broadcastMessage.isNotBlank()) {
                        coroutineScope.launch {
                            webSocketPool.broadcast(broadcastMessage)
                            onBroadcastMessageChange("")
                        }
                    }
                }
            )
        }
        
        item {
            // 批量操作
            BatchOperationsCard(webSocketPool = webSocketPool)
        }
    }
}

/**
 * 监控标签
 */
@Composable
private fun MonitoringTab(
    webSocketPool: UnifyWebSocketPool,
    modifier: Modifier = Modifier
) {
    var healthReport by remember { mutableStateOf<WebSocketHealthReport?>(null) }
    var isChecking by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // 健康检查
            HealthCheckCard(
                healthReport = healthReport,
                isChecking = isChecking,
                onHealthCheck = {
                    isChecking = true
                    coroutineScope.launch {
                        healthReport = webSocketPool.healthCheck()
                        isChecking = false
                    }
                }
            )
        }
        
        item {
            // 性能监控
            PerformanceMonitorCard(webSocketPool = webSocketPool)
        }
    }
}

/**
 * 统计信息卡片
 */
@Composable
private fun StatsCard(
    stats: WebSocketPoolStats,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "连接池统计",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem("总连接", stats.totalConnections.toString())
                StatItem("活跃连接", stats.activeConnections.toString())
                StatItem("连接中", stats.connectingConnections.toString())
                StatItem("错误连接", stats.errorConnections.toString())
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "负载均衡: ${stats.loadBalanceStrategy.name}",
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
 * 连接概览卡片
 */
@Composable
private fun ConnectionOverviewCard(
    webSocketPool: UnifyWebSocketPool,
    modifier: Modifier = Modifier
) {
    val connectionStates = webSocketPool.getAllConnectionStates()
    
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "连接状态概览",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (connectionStates.isEmpty()) {
                Text(
                    text = "暂无连接",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                connectionStates.forEach { (id, state) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = id,
                            fontSize = 14.sp
                        )
                        
                        ConnectionStateChip(state = state)
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

/**
 * 连接状态芯片
 */
@Composable
private fun ConnectionStateChip(
    state: WebSocketState,
    modifier: Modifier = Modifier
) {
    val (statusText, statusColor) = when (state) {
        WebSocketState.DISCONNECTED -> "断开" to Color.Gray
        WebSocketState.CONNECTING -> "连接中" to Color.Blue
        WebSocketState.CONNECTED -> "已连接" to Color.Green
        WebSocketState.DISCONNECTING -> "断开中" to Color(0xFFFF9800)
        WebSocketState.ERROR -> "错误" to Color.Red
    }
    
    Text(
        text = statusText,
        fontSize = 10.sp,
        color = statusColor,
        modifier = modifier
    )
}

/**
 * 快速操作卡片
 */
@Composable
private fun QuickActionsCard(
    webSocketPool: UnifyWebSocketPool,
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
                            webSocketPool.connectAll()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("全部连接", fontSize = 12.sp)
                }
                
                Button(
                    onClick = {
                        coroutineScope.launch {
                            webSocketPool.disconnectAll()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("全部断开", fontSize = 12.sp)
                }
            }
        }
    }
}

/**
 * 空连接卡片
 */
@Composable
private fun EmptyConnectionsCard(
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🔌",
                    fontSize = 48.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "暂无WebSocket连接",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "请在管理页面添加连接",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 连接卡片
 */
@Composable
private fun ConnectionCard(
    connectionId: String,
    state: WebSocketState,
    webSocketPool: UnifyWebSocketPool,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = connectionId,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                
                ConnectionStateChip(state = state)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            if (state == WebSocketState.CONNECTED) {
                                webSocketPool.disconnect(connectionId)
                            } else {
                                webSocketPool.connect(connectionId)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (state == WebSocketState.CONNECTED) "断开" else "连接",
                        fontSize = 12.sp
                    )
                }
                
                Button(
                    onClick = {
                        coroutineScope.launch {
                            webSocketPool.removeConnection(connectionId)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("移除", fontSize = 12.sp)
                }
            }
        }
    }
}

/**
 * 添加连接卡片
 */
@Composable
private fun AddConnectionCard(
    connectionId: String,
    onConnectionIdChange: (String) -> Unit,
    connectionUrl: String,
    onConnectionUrlChange: (String) -> Unit,
    onAddConnection: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "添加新连接",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = connectionId,
                onValueChange = onConnectionIdChange,
                label = { Text("连接ID") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("例如: server1") }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = connectionUrl,
                onValueChange = onConnectionUrlChange,
                label = { Text("WebSocket URL") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("wss://example.com/websocket") }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onAddConnection,
                enabled = connectionId.isNotBlank() && connectionUrl.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("添加连接")
            }
        }
    }
}

/**
 * 广播卡片
 */
@Composable
private fun BroadcastCard(
    message: String,
    onMessageChange: (String) -> Unit,
    onBroadcast: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "广播消息",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = message,
                onValueChange = onMessageChange,
                label = { Text("广播消息") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("输入要广播的消息...") },
                minLines = 2
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onBroadcast,
                enabled = message.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("广播到所有连接")
            }
        }
    }
}

/**
 * 批量操作卡片
 */
@Composable
private fun BatchOperationsCard(
    webSocketPool: UnifyWebSocketPool,
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
                        coroutineScope.launch {
                            webSocketPool.connectAll()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("连接全部", fontSize = 12.sp)
                }
                
                Button(
                    onClick = {
                        coroutineScope.launch {
                            webSocketPool.disconnectAll()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("断开全部", fontSize = 12.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = {
                    coroutineScope.launch {
                        webSocketPool.cleanup()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("清空连接池")
            }
        }
    }
}

/**
 * 健康检查卡片
 */
@Composable
private fun HealthCheckCard(
    healthReport: WebSocketHealthReport?,
    isChecking: Boolean,
    onHealthCheck: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "健康检查",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Button(
                    onClick = onHealthCheck,
                    enabled = !isChecking
                ) {
                    if (isChecking) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Text("检查", fontSize = 12.sp)
                    }
                }
            }
            
            healthReport?.let { report ->
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "健康连接: ${report.healthyConnections.size}",
                    fontSize = 14.sp,
                    color = Color.Green
                )
                
                Text(
                    text = "异常连接: ${report.unhealthyConnections.size}",
                    fontSize = 14.sp,
                    color = Color.Red
                )
                
                Text(
                    text = "整体状态: ${report.overallHealth}",
                    fontSize = 14.sp,
                    color = when (report.overallHealth) {
                        HealthStatus.HEALTHY -> Color.Green
                        HealthStatus.DEGRADED -> Color(0xFFFF9800)
                        HealthStatus.CRITICAL -> Color.Red
                    }
                )
            }
        }
    }
}

/**
 * 性能监控卡片
 */
@Composable
private fun PerformanceMonitorCard(
    webSocketPool: UnifyWebSocketPool,
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
                text = "实时性能指标监控功能开发中...",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 池标签枚举
 */
enum class PoolTab(val displayName: String, val icon: String) {
    OVERVIEW("概览", "📊"),
    CONNECTIONS("连接", "🔗"),
    MANAGEMENT("管理", "⚙️"),
    MONITORING("监控", "📈")
}
