package com.unify.data.demo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.unify.data.UnifyDataManager
import com.unify.data.DataSyncStatus
import com.unify.data.CacheStrategy
import com.unify.data.StorageType
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

/**
 * 统一数据管理系统演示应用
 * 展示跨平台数据存储、状态管理、缓存和同步功能
 */
@Composable
fun UnifyDataDemo() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("存储管理", "状态管理", "缓存管理", "数据同步", "性能监控")
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 标题
        Text(
            text = "Unify 数据管理系统演示",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // 选项卡
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 内容区域
        when (selectedTab) {
            0 -> StorageManagementDemo()
            1 -> StateManagementDemo()
            2 -> CacheManagementDemo()
            3 -> DataSyncDemo()
            4 -> PerformanceMonitoringDemo()
        }
    }
}

/**
 * 存储管理演示
 */
@Composable
fun StorageManagementDemo() {
    val scope = rememberCoroutineScope()
    val dataManager = remember { 
        UnifyDataManagerFactory.create(
            UnifyDataManagerConfig(
                enableStorage = true,
                storageEncryption = true,
                maxStorageSize = 50 * 1024 * 1024 // 50MB
            )
        )
    }
    
    var key by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var storageKeys by remember { mutableStateOf<Set<String>>(emptySet()) }
    var storageSize by remember { mutableStateOf(0L) }
    
    LaunchedEffect(Unit) {
        dataManager.initialize()
        refreshStorageInfo()
    }
    
    suspend fun refreshStorageInfo() {
        storageKeys = dataManager.storage.keys()
        storageSize = dataManager.storage.size()
    }
    
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "存储操作",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = key,
                        onValueChange = { key = it },
                        label = { Text("键 (Key)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = value,
                        onValueChange = { value = it },
                        label = { Text("值 (Value)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                scope.launch {
                                    try {
                                        val userData = UserData(
                                            name = value,
                                            timestamp = System.currentTimeMillis()
                                        )
                                        dataManager.storage.put(key, userData)
                                        result = "存储成功: $key"
                                        refreshStorageInfo()
                                    } catch (e: Exception) {
                                        result = "存储失败: ${e.message}"
                                    }
                                }
                            },
                            enabled = key.isNotBlank() && value.isNotBlank()
                        ) {
                            Text("存储")
                        }
                        
                        Button(
                            onClick = {
                                scope.launch {
                                    try {
                                        val userData = dataManager.storage.get<UserData>(key)
                                        result = if (userData != null) {
                                            "读取成功: ${userData.name} (${userData.timestamp})"
                                        } else {
                                            "未找到数据: $key"
                                        }
                                    } catch (e: Exception) {
                                        result = "读取失败: ${e.message}"
                                    }
                                }
                            },
                            enabled = key.isNotBlank()
                        ) {
                            Text("读取")
                        }
                        
                        Button(
                            onClick = {
                                scope.launch {
                                    try {
                                        val removed = dataManager.storage.remove(key)
                                        result = if (removed) {
                                            "删除成功: $key"
                                        } else {
                                            "删除失败: $key"
                                        }
                                        refreshStorageInfo()
                                    } catch (e: Exception) {
                                        result = "删除失败: ${e.message}"
                                    }
                                }
                            },
                            enabled = key.isNotBlank()
                        ) {
                            Text("删除")
                        }
                    }
                    
                    if (result.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = result,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (result.contains("成功")) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )
                    }
                }
            }
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "存储信息",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text("存储大小: ${formatBytes(storageSize)}")
                    Text("键数量: ${storageKeys.size}")
                    
                    if (storageKeys.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("存储的键:")
                        storageKeys.forEach { storageKey ->
                            Text("• $storageKey", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

/**
 * 状态管理演示
 */
@Composable
fun StateManagementDemo() {
    val dataManager = remember { 
        UnifyDataManagerFactory.create(UnifyDataManagerConfig(enableState = true))
    }
    
    var counter by remember { mutableStateOf(0) }
    var userName by remember { mutableStateOf("") }
    var theme by remember { mutableStateOf("Light") }
    
    // 观察状态变化
    val observedCounter by dataManager.state.observeState<Int>("counter").collectAsState(initial = 0)
    val observedUserName by dataManager.state.observeState<String>("userName").collectAsState(initial = "")
    val observedTheme by dataManager.state.observeState<String>("theme").collectAsState(initial = "Light")
    
    LaunchedEffect(Unit) {
        dataManager.initialize()
    }
    
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "状态管理",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 计数器状态
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("计数器: $observedCounter")
                        Row {
                            Button(
                                onClick = {
                                    counter = observedCounter - 1
                                    dataManager.state.setState("counter", counter)
                                }
                            ) {
                                Text("-")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    counter = observedCounter + 1
                                    dataManager.state.setState("counter", counter)
                                }
                            ) {
                                Text("+")
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 用户名状态
                    OutlinedTextField(
                        value = userName,
                        onValueChange = { 
                            userName = it
                            dataManager.state.setState("userName", it)
                        },
                        label = { Text("用户名") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Text(
                        text = "当前用户名: $observedUserName",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 主题状态
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("主题: ")
                        Spacer(modifier = Modifier.width(8.dp))
                        listOf("Light", "Dark", "Auto").forEach { themeOption ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = observedTheme == themeOption,
                                    onClick = {
                                        theme = themeOption
                                        dataManager.state.setState("theme", themeOption)
                                    }
                                )
                                Text(themeOption)
                            }
                        }
                    }
                }
            }
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "状态信息",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val stateKeys = dataManager.state.getStateKeys()
                    Text("状态数量: ${stateKeys.size}")
                    
                    if (stateKeys.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("状态键:")
                        stateKeys.forEach { stateKey ->
                            Text("• $stateKey", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

/**
 * 缓存管理演示
 */
@Composable
fun CacheManagementDemo() {
    val scope = rememberCoroutineScope()
    val dataManager = remember { 
        UnifyDataManagerFactory.create(
            UnifyDataManagerConfig(
                enableCache = true,
                cachePolicy = UnifyCachePolicy(
                    maxSize = 10 * 1024 * 1024, // 10MB
                    defaultTtl = 300000, // 5分钟
                    evictionPolicy = UnifyCacheEvictionPolicy.LRU
                )
            )
        )
    }
    
    var cacheKey by remember { mutableStateOf("") }
    var cacheValue by remember { mutableStateOf("") }
    var cacheTtl by remember { mutableStateOf("300") }
    var cacheResult by remember { mutableStateOf("") }
    var cacheStats by remember { mutableStateOf<UnifyCacheStats?>(null) }
    
    LaunchedEffect(Unit) {
        dataManager.initialize()
        refreshCacheStats()
    }
    
    suspend fun refreshCacheStats() {
        cacheStats = dataManager.cache.getCacheStats()
    }
    
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "缓存操作",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = cacheKey,
                        onValueChange = { cacheKey = it },
                        label = { Text("缓存键") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = cacheValue,
                        onValueChange = { cacheValue = it },
                        label = { Text("缓存值") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = cacheTtl,
                        onValueChange = { cacheTtl = it },
                        label = { Text("过期时间(秒)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                scope.launch {
                                    try {
                                        val ttlMs = (cacheTtl.toLongOrNull() ?: 300) * 1000
                                        dataManager.cache.cache(cacheKey, cacheValue, ttlMs)
                                        cacheResult = "缓存成功: $cacheKey"
                                        refreshCacheStats()
                                    } catch (e: Exception) {
                                        cacheResult = "缓存失败: ${e.message}"
                                    }
                                }
                            },
                            enabled = cacheKey.isNotBlank() && cacheValue.isNotBlank()
                        ) {
                            Text("缓存")
                        }
                        
                        Button(
                            onClick = {
                                scope.launch {
                                    try {
                                        val cached = dataManager.cache.getCache<String>(cacheKey)
                                        cacheResult = if (cached != null) {
                                            "缓存命中: $cached"
                                        } else {
                                            "缓存未命中: $cacheKey"
                                        }
                                        refreshCacheStats()
                                    } catch (e: Exception) {
                                        cacheResult = "获取失败: ${e.message}"
                                    }
                                }
                            },
                            enabled = cacheKey.isNotBlank()
                        ) {
                            Text("获取")
                        }
                        
                        Button(
                            onClick = {
                                scope.launch {
                                    try {
                                        val removed = dataManager.cache.removeCache(cacheKey)
                                        cacheResult = if (removed) {
                                            "删除成功: $cacheKey"
                                        } else {
                                            "删除失败: $cacheKey"
                                        }
                                        refreshCacheStats()
                                    } catch (e: Exception) {
                                        cacheResult = "删除失败: ${e.message}"
                                    }
                                }
                            },
                            enabled = cacheKey.isNotBlank()
                        ) {
                            Text("删除")
                        }
                    }
                    
                    if (cacheResult.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = cacheResult,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (cacheResult.contains("成功") || cacheResult.contains("命中")) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )
                    }
                }
            }
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "缓存统计",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    cacheStats?.let { stats ->
                        Text("命中次数: ${stats.hitCount}")
                        Text("未命中次数: ${stats.missCount}")
                        Text("淘汰次数: ${stats.evictionCount}")
                        Text("命中率: ${"%.2f".format(stats.hitRate * 100)}%")
                        Text("当前大小: ${formatBytes(stats.totalSize)}")
                        Text("最大大小: ${formatBytes(stats.maxSize)}")
                    } ?: Text("加载中...")
                }
            }
        }
    }
}

/**
 * 数据同步演示
 */
@Composable
fun DataSyncDemo() {
    val scope = rememberCoroutineScope()
    val dataManager = remember { 
        UnifyDataManagerFactory.create(
            UnifyDataManagerConfig(
                enableSync = true,
                syncPolicy = UnifySyncPolicy(
                    autoSync = true,
                    syncInterval = 60000, // 1分钟
                    conflictResolution = UnifyConflictResolution.REMOTE_WINS
                )
            )
        )
    }
    
    var syncKey by remember { mutableStateOf("") }
    var syncResult by remember { mutableStateOf("") }
    val syncStatus by dataManager.sync.observeSyncStatus().collectAsState(
        initial = UnifySyncStatus(
            isOnline = false,
            isSyncing = false,
            lastSyncTime = 0L,
            pendingSyncCount = 0,
            failedSyncCount = 0
        )
    )
    
    LaunchedEffect(Unit) {
        dataManager.initialize()
    }
    
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "数据同步",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = syncKey,
                        onValueChange = { syncKey = it },
                        label = { Text("同步键") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                scope.launch {
                                    try {
                                        val result = dataManager.sync.syncToRemote(syncKey)
                                        syncResult = if (result.success) {
                                            "上传成功: ${result.key}"
                                        } else {
                                            "上传失败: ${result.error}"
                                        }
                                    } catch (e: Exception) {
                                        syncResult = "上传异常: ${e.message}"
                                    }
                                }
                            },
                            enabled = syncKey.isNotBlank() && syncStatus.isOnline
                        ) {
                            Text("上传")
                        }
                        
                        Button(
                            onClick = {
                                scope.launch {
                                    try {
                                        val result = dataManager.sync.syncFromRemote(syncKey)
                                        syncResult = if (result.success) {
                                            "下载成功: ${result.key}"
                                        } else {
                                            "下载失败: ${result.error}"
                                        }
                                    } catch (e: Exception) {
                                        syncResult = "下载异常: ${e.message}"
                                    }
                                }
                            },
                            enabled = syncKey.isNotBlank() && syncStatus.isOnline
                        ) {
                            Text("下载")
                        }
                        
                        Button(
                            onClick = {
                                scope.launch {
                                    try {
                                        val result = dataManager.sync.bidirectionalSync(syncKey)
                                        syncResult = if (result.success) {
                                            "双向同步成功: ${result.key}"
                                        } else {
                                            "双向同步失败: ${result.error}"
                                        }
                                    } catch (e: Exception) {
                                        syncResult = "双向同步异常: ${e.message}"
                                    }
                                }
                            },
                            enabled = syncKey.isNotBlank() && syncStatus.isOnline
                        ) {
                            Text("双向同步")
                        }
                    }
                    
                    if (syncResult.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = syncResult,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (syncResult.contains("成功")) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )
                    }
                }
            }
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "同步状态",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("网络状态: ")
                        Text(
                            text = if (syncStatus.isOnline) "在线" else "离线",
                            color = if (syncStatus.isOnline) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("同步状态: ")
                        Text(
                            text = if (syncStatus.isSyncing) "同步中" else "空闲",
                            color = if (syncStatus.isSyncing) {
                                MaterialTheme.colorScheme.secondary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                    
                    Text("上次同步: ${formatTimestamp(syncStatus.lastSyncTime)}")
                    Text("待同步数量: ${syncStatus.pendingSyncCount}")
                    Text("失败数量: ${syncStatus.failedSyncCount}")
                }
            }
        }
    }
}

/**
 * 性能监控演示
 */
@Composable
fun PerformanceMonitoringDemo() {
    val scope = rememberCoroutineScope()
    val dataManager = remember { 
        UnifyDataManagerFactory.create(UnifyDataManagerConfig())
    }
    
    var performanceData by remember { mutableStateOf<PerformanceData?>(null) }
    
    LaunchedEffect(Unit) {
        dataManager.initialize()
        // 模拟性能监控数据收集
        scope.launch {
            while (true) {
                performanceData = collectPerformanceData(dataManager)
                kotlinx.coroutines.delay(1000) // 每秒更新
            }
        }
    }
    
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "性能监控",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    performanceData?.let { data ->
                        Text("存储使用率: ${"%.1f".format(data.storageUsagePercent)}%")
                        Text("缓存命中率: ${"%.1f".format(data.cacheHitRate)}%")
                        Text("同步成功率: ${"%.1f".format(data.syncSuccessRate)}%")
                        Text("平均响应时间: ${data.averageResponseTime}ms")
                        Text("内存使用: ${formatBytes(data.memoryUsage)}")
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        LinearProgressIndicator(
                            progress = data.storageUsagePercent / 100f,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text("存储使用率", style = MaterialTheme.typography.bodySmall)
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        LinearProgressIndicator(
                            progress = data.cacheHitRate / 100f,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text("缓存命中率", style = MaterialTheme.typography.bodySmall)
                        
                    } ?: Text("收集性能数据中...")
                }
            }
        }
    }
}

/**
 * 用户数据模型
 */
@Serializable
data class UserData(
    val name: String,
    val timestamp: Long
)

/**
 * 性能数据模型
 */
data class PerformanceData(
    val storageUsagePercent: Float,
    val cacheHitRate: Float,
    val syncSuccessRate: Float,
    val averageResponseTime: Long,
    val memoryUsage: Long
)

/**
 * 收集性能数据
 */
suspend fun collectPerformanceData(dataManager: UnifyDataManager): PerformanceData {
    val cacheStats = dataManager.cache.getCacheStats()
    val storageSize = dataManager.storage.size()
    val maxStorageSize = 50 * 1024 * 1024L // 50MB
    
    return PerformanceData(
        storageUsagePercent = (storageSize.toFloat() / maxStorageSize * 100).coerceAtMost(100f),
        cacheHitRate = (cacheStats.hitRate * 100).toFloat(),
        syncSuccessRate = 95.5f, // 模拟数据
        averageResponseTime = (50..200).random().toLong(), // 模拟数据
        memoryUsage = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
    )
}

/**
 * 格式化字节数
 */
fun formatBytes(bytes: Long): String {
    val units = arrayOf("B", "KB", "MB", "GB")
    var size = bytes.toDouble()
    var unitIndex = 0
    
    while (size >= 1024 && unitIndex < units.size - 1) {
        size /= 1024
        unitIndex++
    }
    
    return "${"%.1f".format(size)} ${units[unitIndex]}"
}

/**
 * 格式化时间戳
 */
fun formatTimestamp(timestamp: Long): String {
    if (timestamp == 0L) return "从未"
    
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60000 -> "刚刚"
        diff < 3600000 -> "${diff / 60000}分钟前"
        diff < 86400000 -> "${diff / 3600000}小时前"
        else -> "${diff / 86400000}天前"
    }
}
