package com.unify.network.demo

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 网络管理演示应用
 * 展示网络请求、缓存管理、离线支持和性能监控功能
 */
@Composable
fun UnifyNetworkDemo() {
    var networkState by remember { mutableStateOf(NetworkDemoState()) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        scope.launch {
            // 模拟网络状态监控
            while (true) {
                networkState = networkState.copy(
                    connectionStatus = generateConnectionStatus(),
                    networkSpeed = generateNetworkSpeed(),
                    latency = (50..200).random(),
                    dataUsage = networkState.dataUsage + (100..1000).random()
                )
                delay(2000)
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "网络管理演示",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                NetworkStatusCard(
                    status = networkState.connectionStatus,
                    speed = networkState.networkSpeed,
                    latency = networkState.latency,
                    dataUsage = networkState.dataUsage
                )
            }
            
            item {
                NetworkRequestCard(
                    onSendRequest = { url, method ->
                        scope.launch {
                            networkState = networkState.copy(isLoading = true)
                            delay(1000)
                            val response = simulateNetworkRequest(url, method)
                            networkState = networkState.copy(
                                isLoading = false,
                                requestHistory = networkState.requestHistory + response
                            )
                        }
                    },
                    isLoading = networkState.isLoading
                )
            }
            
            item {
                CacheManagementCard(
                    cacheStats = networkState.cacheStats,
                    onClearCache = {
                        networkState = networkState.copy(
                            cacheStats = networkState.cacheStats.copy(
                                hitCount = 0,
                                missCount = 0,
                                totalSize = 0
                            )
                        )
                    },
                    onOptimizeCache = {
                        scope.launch {
                            delay(500)
                            networkState = networkState.copy(
                                cacheStats = networkState.cacheStats.copy(
                                    totalSize = (networkState.cacheStats.totalSize * 0.7).toInt()
                                )
                            )
                        }
                    }
                )
            }
            
            item {
                OfflineManagementCard(
                    offlineRequests = networkState.offlineRequests,
                    onProcessOfflineRequests = {
                        scope.launch {
                            delay(1000)
                            networkState = networkState.copy(
                                offlineRequests = emptyList()
                            )
                        }
                    }
                )
            }
            
            item {
                Text(
                    text = "请求历史",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(networkState.requestHistory.takeLast(5)) { request ->
                RequestHistoryItem(request = request)
            }
        }
    }
}

@Composable
private fun NetworkStatusCard(
    status: ConnectionStatus,
    speed: NetworkSpeed,
    latency: Int,
    dataUsage: Long
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "网络状态",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                NetworkStatusItem(
                    label = "连接状态",
                    value = status.displayName,
                    color = when (status) {
                        ConnectionStatus.CONNECTED -> Color.Green
                        ConnectionStatus.DISCONNECTED -> Color.Red
                        ConnectionStatus.LIMITED -> Color.Orange
                    }
                )
                
                NetworkStatusItem(
                    label = "网络速度",
                    value = "${speed.downloadMbps}/${speed.uploadMbps} Mbps",
                    color = if (speed.downloadMbps > 10) Color.Green else Color.Orange
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                NetworkStatusItem(
                    label = "延迟",
                    value = "${latency}ms",
                    color = if (latency < 100) Color.Green else if (latency < 200) Color.Orange else Color.Red
                )
                
                NetworkStatusItem(
                    label = "数据使用",
                    value = "${dataUsage / 1024}KB",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun NetworkStatusItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun NetworkRequestCard(
    onSendRequest: (String, String) -> Unit,
    isLoading: Boolean
) {
    var url by remember { mutableStateOf("https://api.example.com/data") }
    var selectedMethod by remember { mutableStateOf("GET") }
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "网络请求测试",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("请求URL") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedMethod,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("请求方法") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    listOf("GET", "POST", "PUT", "DELETE").forEach { method ->
                        DropdownMenuItem(
                            text = { Text(method) },
                            onClick = {
                                selectedMethod = method
                                expanded = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = { onSendRequest(url, selectedMethod) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("发送中...")
                } else {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "发送",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("发送请求")
                }
            }
        }
    }
}

@Composable
private fun CacheManagementCard(
    cacheStats: CacheStats,
    onClearCache: () -> Unit,
    onOptimizeCache: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "缓存管理",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CacheStatItem(
                    label = "命中率",
                    value = "${(cacheStats.hitRate * 100).toInt()}%"
                )
                CacheStatItem(
                    label = "缓存大小",
                    value = "${cacheStats.totalSize}KB"
                )
                CacheStatItem(
                    label = "条目数",
                    value = "${cacheStats.entryCount}"
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onClearCache,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "清空",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("清空缓存")
                }
                
                Button(
                    onClick = onOptimizeCache,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "优化",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("优化缓存")
                }
            }
        }
    }
}

@Composable
private fun CacheStatItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun OfflineManagementCard(
    offlineRequests: List<OfflineRequest>,
    onProcessOfflineRequests: () -> Unit
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
                    text = "离线管理",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Surface(
                    color = if (offlineRequests.isNotEmpty()) 
                        MaterialTheme.colorScheme.errorContainer 
                    else 
                        MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "${offlineRequests.size} 待处理",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (offlineRequests.isNotEmpty()) 
                            MaterialTheme.colorScheme.onErrorContainer 
                        else 
                            MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            if (offlineRequests.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "离线请求队列:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                offlineRequests.take(3).forEach { request ->
                    Text(
                        text = "• ${request.method} ${request.url}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                    )
                }
                
                if (offlineRequests.size > 3) {
                    Text(
                        text = "... 还有 ${offlineRequests.size - 3} 个请求",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = onProcessOfflineRequests,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = "处理",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("处理离线请求")
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "暂无离线请求",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RequestHistoryItem(request: NetworkRequestResult) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = MaterialTheme.shapes.small,
                color = when (request.status) {
                    RequestStatus.SUCCESS -> Color.Green
                    RequestStatus.ERROR -> Color.Red
                    RequestStatus.TIMEOUT -> Color.Orange
                    RequestStatus.CACHED -> Color.Blue
                }
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (request.status) {
                            RequestStatus.SUCCESS -> Icons.Default.CheckCircle
                            RequestStatus.ERROR -> Icons.Default.Error
                            RequestStatus.TIMEOUT -> Icons.Default.Schedule
                            RequestStatus.CACHED -> Icons.Default.Storage
                        },
                        contentDescription = request.status.name,
                        modifier = Modifier.size(20.dp),
                        tint = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${request.method} ${request.url}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${request.status.displayName} • ${request.duration}ms",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = formatTime(request.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// 数据生成和辅助函数

private fun generateConnectionStatus(): ConnectionStatus {
    return when ((1..10).random()) {
        in 1..7 -> ConnectionStatus.CONNECTED
        in 8..9 -> ConnectionStatus.LIMITED
        else -> ConnectionStatus.DISCONNECTED
    }
}

private fun generateNetworkSpeed(): NetworkSpeed {
    return NetworkSpeed(
        downloadMbps = (5..50).random(),
        uploadMbps = (1..20).random()
    )
}

private fun simulateNetworkRequest(url: String, method: String): NetworkRequestResult {
    val isSuccess = (1..10).random() > 2 // 80% 成功率
    val duration = (100..2000).random()
    
    return NetworkRequestResult(
        url = url,
        method = method,
        status = if (isSuccess) {
            if ((1..10).random() > 7) RequestStatus.CACHED else RequestStatus.SUCCESS
        } else {
            if (duration > 1500) RequestStatus.TIMEOUT else RequestStatus.ERROR
        },
        duration = duration,
        timestamp = System.currentTimeMillis(),
        responseSize = if (isSuccess) (1000..50000).random() else 0
    )
}

private fun formatTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 60000 -> "刚刚"
        diff < 3600000 -> "${diff / 60000}分钟前"
        else -> "${diff / 3600000}小时前"
    }
}

// 数据类定义

data class NetworkDemoState(
    val connectionStatus: ConnectionStatus = ConnectionStatus.CONNECTED,
    val networkSpeed: NetworkSpeed = NetworkSpeed(25, 10),
    val latency: Int = 80,
    val dataUsage: Long = 0,
    val isLoading: Boolean = false,
    val requestHistory: List<NetworkRequestResult> = emptyList(),
    val cacheStats: CacheStats = CacheStats(),
    val offlineRequests: List<OfflineRequest> = generateOfflineRequests()
)

data class NetworkSpeed(
    val downloadMbps: Int,
    val uploadMbps: Int
)

data class CacheStats(
    val hitCount: Int = 45,
    val missCount: Int = 12,
    val totalSize: Int = 2048,
    val entryCount: Int = 156
) {
    val hitRate: Double get() = if (hitCount + missCount > 0) hitCount.toDouble() / (hitCount + missCount) else 0.0
}

data class OfflineRequest(
    val url: String,
    val method: String,
    val timestamp: Long
)

data class NetworkRequestResult(
    val url: String,
    val method: String,
    val status: RequestStatus,
    val duration: Int,
    val timestamp: Long,
    val responseSize: Int
)

enum class ConnectionStatus(val displayName: String) {
    CONNECTED("已连接"),
    DISCONNECTED("未连接"),
    LIMITED("受限连接")
}

enum class RequestStatus(val displayName: String) {
    SUCCESS("成功"),
    ERROR("失败"),
    TIMEOUT("超时"),
    CACHED("缓存")
}

private fun generateOfflineRequests(): List<OfflineRequest> {
    return if ((1..10).random() > 7) {
        listOf(
            OfflineRequest(
                url = "https://api.example.com/sync",
                method = "POST",
                timestamp = System.currentTimeMillis() - 300000
            ),
            OfflineRequest(
                url = "https://api.example.com/upload",
                method = "PUT",
                timestamp = System.currentTimeMillis() - 180000
            )
        )
    } else emptyList()
}
