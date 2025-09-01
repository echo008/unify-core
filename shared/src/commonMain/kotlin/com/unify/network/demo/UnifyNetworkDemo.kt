package com.unify.network.demo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unify.network.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

/**
 * 统一网络通信系统演示应用
 * 展示HTTP客户端、网络状态监控、离线支持和拦截器功能
 */
@Composable
fun UnifyNetworkDemo() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("HTTP客户端", "网络状态", "离线支持", "拦截器管理", "性能监控")
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 标题
        Text(
            text = "Unify Network 演示",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // 标签页
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 标签页内容
        when (selectedTab) {
            0 -> HttpClientDemo()
            1 -> NetworkStateDemo()
            2 -> OfflineSupportDemo()
            3 -> InterceptorDemo()
            4 -> PerformanceMonitorDemo()
        }
    }
}

/**
 * HTTP客户端演示
 */
@Composable
fun HttpClientDemo() {
    val scope = rememberCoroutineScope()
    var url by remember { mutableStateOf("https://jsonplaceholder.typicode.com/posts/1") }
    var method by remember { mutableStateOf("GET") }
    var requestBody by remember { mutableStateOf("") }
    var headers by remember { mutableStateOf("Content-Type: application/json") }
    var response by remember { mutableStateOf<UnifyHttpResponse<String>?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    
    // 初始化网络管理器
    val networkManager = remember {
        UnifyNetworkManagerFactory.create(
            UnifyNetworkConfig(
                defaultTimeout = 30000,
                enableLogging = true,
                enableCache = true
            )
        )
    }
    
    LaunchedEffect(Unit) {
        networkManager.initialize()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // 请求配置
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "HTTP 请求配置",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // URL输入
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("请求URL") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 方法选择
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("GET", "POST", "PUT", "DELETE").forEach { methodOption ->
                        FilterChip(
                            selected = method == methodOption,
                            onClick = { method = methodOption },
                            label = { Text(methodOption) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 请求头
                OutlinedTextField(
                    value = headers,
                    onValueChange = { headers = it },
                    label = { Text("请求头 (key: value)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 请求体
                if (method in listOf("POST", "PUT")) {
                    OutlinedTextField(
                        value = requestBody,
                        onValueChange = { requestBody = it },
                        label = { Text("请求体 (JSON)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 发送按钮
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            try {
                                val headerMap = parseHeaders(headers)
                                response = when (method) {
                                    "GET" -> networkManager.httpClient.get<String>(url, headerMap)
                                    "POST" -> networkManager.httpClient.post<String>(url, requestBody.takeIf { it.isNotBlank() }, headerMap)
                                    "PUT" -> networkManager.httpClient.put<String>(url, requestBody.takeIf { it.isNotBlank() }, headerMap)
                                    "DELETE" -> networkManager.httpClient.delete<String>(url, headerMap)
                                    else -> networkManager.httpClient.get<String>(url, headerMap)
                                }
                            } catch (e: Exception) {
                                response = UnifyHttpResponse(
                                    statusCode = 0,
                                    statusMessage = "Error",
                                    headers = emptyMap(),
                                    body = null,
                                    isSuccessful = false,
                                    responseTime = 0,
                                    error = UnifyNetworkError(
                                        code = 0,
                                        message = e.message ?: "Unknown error",
                                        type = UnifyNetworkErrorType.UNKNOWN_ERROR
                                    )
                                )
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("发送请求")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 响应显示
        response?.let { resp ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "响应结果",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    // 状态码
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("状态码:")
                        Text(
                            text = "${resp.statusCode} ${resp.statusMessage}",
                            color = if (resp.isSuccessful) Color.Green else Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // 响应时间
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("响应时间:")
                        Text("${resp.responseTime}ms")
                    }
                    
                    // 是否来自缓存
                    if (resp.isFromCache) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("缓存:")
                            Text("来自缓存", color = Color.Blue)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 响应头
                    if (resp.headers.isNotEmpty()) {
                        Text(
                            text = "响应头:",
                            fontWeight = FontWeight.Bold
                        )
                        resp.headers.forEach { (key, value) ->
                            Text(
                                text = "$key: $value",
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    // 响应体
                    Text(
                        text = "响应体:",
                        fontWeight = FontWeight.Bold
                    )
                    
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = resp.body ?: resp.error?.message ?: "无响应体",
                            modifier = Modifier.padding(8.dp),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * 网络状态演示
 */
@Composable
fun NetworkStateDemo() {
    val scope = rememberCoroutineScope()
    var networkStatus by remember { mutableStateOf<UnifyNetworkStatus?>(null) }
    var isMonitoring by remember { mutableStateOf(false) }
    
    val networkManager = remember {
        UnifyNetworkManagerFactory.create()
    }
    
    LaunchedEffect(Unit) {
        networkManager.initialize()
    }
    
    // 监控网络状态
    LaunchedEffect(isMonitoring) {
        if (isMonitoring) {
            networkManager.networkState.observeNetworkState().collect { status ->
                networkStatus = status
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "网络状态监控",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // 控制按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                val available = networkManager.networkState.isNetworkAvailable()
                                val type = networkManager.networkState.getNetworkType()
                                val quality = networkManager.networkState.getNetworkQuality()
                                
                                networkStatus = UnifyNetworkStatus(
                                    isConnected = available,
                                    networkType = type,
                                    quality = quality,
                                    signalStrength = 80,
                                    bandwidth = 50 * 1024 * 1024,
                                    latency = 20
                                )
                            }
                        }
                    ) {
                        Text("检查网络")
                    }
                    
                    Button(
                        onClick = { isMonitoring = !isMonitoring }
                    ) {
                        Text(if (isMonitoring) "停止监控" else "开始监控")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 网络状态显示
                networkStatus?.let { status ->
                    NetworkStatusCard(status)
                }
            }
        }
    }
}

@Composable
fun NetworkStatusCard(status: UnifyNetworkStatus) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "当前网络状态",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // 连接状态
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("连接状态:")
                Text(
                    text = if (status.isConnected) "已连接" else "未连接",
                    color = if (status.isConnected) Color.Green else Color.Red,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // 网络类型
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("网络类型:")
                Text(getNetworkTypeText(status.networkType))
            }
            
            // 网络质量
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("网络质量:")
                Text(
                    text = getNetworkQualityText(status.quality),
                    color = getNetworkQualityColor(status.quality)
                )
            }
            
            // 信号强度
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("信号强度:")
                Text("${status.signalStrength}%")
            }
            
            // 带宽
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("估算带宽:")
                Text(formatBandwidth(status.bandwidth))
            }
            
            // 延迟
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("网络延迟:")
                Text("${status.latency}ms")
            }
        }
    }
}

/**
 * 离线支持演示
 */
@Composable
fun OfflineSupportDemo() {
    val scope = rememberCoroutineScope()
    var cacheStats by remember { mutableStateOf<UnifyOfflineCacheStats?>(null) }
    var offlineQueue by remember { mutableStateOf<List<UnifyHttpRequest>>(emptyList()) }
    
    val networkManager = remember {
        UnifyNetworkManagerFactory.create(
            UnifyNetworkConfig(enableOfflineSupport = true)
        )
    }
    
    LaunchedEffect(Unit) {
        networkManager.initialize()
        scope.launch {
            cacheStats = networkManager.offlineSupport.getCacheStats()
            offlineQueue = networkManager.offlineSupport.getOfflineRequestQueue()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // 缓存统计
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "缓存统计",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                cacheStats?.let { stats ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("缓存大小:")
                        Text(formatBytes(stats.cacheSize))
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("缓存条目:")
                        Text("${stats.cacheCount}")
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("命中率:")
                        Text("${(stats.hitRate * 100).toInt()}%")
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("离线队列:")
                        Text("${stats.offlineQueueSize}")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 操作按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                cacheStats = networkManager.offlineSupport.getCacheStats()
                            }
                        }
                    ) {
                        Text("刷新统计")
                    }
                    
                    Button(
                        onClick = {
                            scope.launch {
                                networkManager.offlineSupport.cleanExpiredCache()
                                cacheStats = networkManager.offlineSupport.getCacheStats()
                            }
                        }
                    ) {
                        Text("清理缓存")
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 离线队列
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "离线请求队列",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                if (offlineQueue.isEmpty()) {
                    Text(
                        text = "队列为空",
                        color = Color.Gray,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.height(200.dp)
                    ) {
                        items(offlineQueue) { request ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    Text(
                                        text = "${request.method} ${request.url}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    if (request.body != null) {
                                        Text(
                                            text = "Body: ${request.body}",
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = {
                        scope.launch {
                            offlineQueue = networkManager.offlineSupport.getOfflineRequestQueue()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("刷新队列")
                }
            }
        }
    }
}

/**
 * 拦截器演示
 */
@Composable
fun InterceptorDemo() {
    val scope = rememberCoroutineScope()
    var interceptorLogs by remember { mutableStateOf<List<String>>(emptyList()) }
    
    val networkManager = remember {
        UnifyNetworkManagerFactory.create().apply {
            // 添加日志拦截器
            interceptors.addRequestInterceptor(UnifyLoggingInterceptor())
            interceptors.addResponseInterceptor(UnifyLoggingInterceptor())
            
            // 添加认证拦截器
            interceptors.addRequestInterceptor(UnifyAuthInterceptor { "demo-token-123" })
        }
    }
    
    LaunchedEffect(Unit) {
        networkManager.initialize()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "拦截器管理",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // 测试按钮
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                val response = networkManager.httpClient.get<String>(
                                    "https://jsonplaceholder.typicode.com/posts/1"
                                )
                                interceptorLogs = interceptorLogs + "请求完成: ${response.statusCode}"
                            } catch (e: Exception) {
                                interceptorLogs = interceptorLogs + "请求失败: ${e.message}"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("测试拦截器")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 拦截器日志
                Text(
                    text = "拦截器日志:",
                    fontWeight = FontWeight.Bold
                )
                
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        if (interceptorLogs.isEmpty()) {
                            Text(
                                text = "暂无日志",
                                color = Color.Gray
                            )
                        } else {
                            interceptorLogs.forEach { log ->
                                Text(
                                    text = log,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = { interceptorLogs = emptyList() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("清空日志")
                }
            }
        }
    }
}

/**
 * 性能监控演示
 */
@Composable
fun PerformanceMonitorDemo() {
    var performanceMetrics by remember { mutableStateOf<List<PerformanceMetric>>(emptyList()) }
    
    LaunchedEffect(Unit) {
        // 模拟性能数据
        performanceMetrics = listOf(
            PerformanceMetric("请求总数", "1,234"),
            PerformanceMetric("成功率", "98.5%"),
            PerformanceMetric("平均响应时间", "245ms"),
            PerformanceMetric("缓存命中率", "76.3%"),
            PerformanceMetric("网络错误率", "1.2%"),
            PerformanceMetric("超时率", "0.3%")
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "性能监控",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                performanceMetrics.forEach { metric ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(metric.name)
                        Text(
                            text = metric.value,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// 数据类和辅助函数
@Serializable
data class PerformanceMetric(
    val name: String,
    val value: String
)

private fun parseHeaders(headersString: String): Map<String, String> {
    return headersString.split("\n")
        .mapNotNull { line ->
            val parts = line.split(":", limit = 2)
            if (parts.size == 2) {
                parts[0].trim() to parts[1].trim()
            } else null
        }
        .toMap()
}

private fun getNetworkTypeText(type: UnifyNetworkType): String {
    return when (type) {
        UnifyNetworkType.WIFI -> "WiFi"
        UnifyNetworkType.CELLULAR_5G -> "5G"
        UnifyNetworkType.CELLULAR_4G -> "4G"
        UnifyNetworkType.CELLULAR_3G -> "3G"
        UnifyNetworkType.CELLULAR_2G -> "2G"
        UnifyNetworkType.ETHERNET -> "以太网"
        UnifyNetworkType.BLUETOOTH -> "蓝牙"
        UnifyNetworkType.VPN -> "VPN"
        UnifyNetworkType.UNKNOWN -> "未知"
    }
}

private fun getNetworkQualityText(quality: UnifyNetworkQuality): String {
    return when (quality) {
        UnifyNetworkQuality.EXCELLENT -> "优秀"
        UnifyNetworkQuality.GOOD -> "良好"
        UnifyNetworkQuality.FAIR -> "一般"
        UnifyNetworkQuality.POOR -> "较差"
        UnifyNetworkQuality.UNAVAILABLE -> "不可用"
    }
}

private fun getNetworkQualityColor(quality: UnifyNetworkQuality): Color {
    return when (quality) {
        UnifyNetworkQuality.EXCELLENT -> Color.Green
        UnifyNetworkQuality.GOOD -> Color.Blue
        UnifyNetworkQuality.FAIR -> Color(0xFFFFA500) // Orange
        UnifyNetworkQuality.POOR -> Color.Red
        UnifyNetworkQuality.UNAVAILABLE -> Color.Gray
    }
}

private fun formatBandwidth(bandwidth: Long): String {
    return when {
        bandwidth >= 1024 * 1024 * 1024 -> "${bandwidth / (1024 * 1024 * 1024)} Gbps"
        bandwidth >= 1024 * 1024 -> "${bandwidth / (1024 * 1024)} Mbps"
        bandwidth >= 1024 -> "${bandwidth / 1024} Kbps"
        else -> "${bandwidth} bps"
    }
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes >= 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024 * 1024)} GB"
        bytes >= 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        bytes >= 1024 -> "${bytes / 1024} KB"
        else -> "${bytes} B"
    }
}
