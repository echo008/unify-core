package com.unify.core.dynamic

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
import kotlinx.coroutines.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*

/**
 * 动态化管理控制台
 * 提供热更新管理、监控和配置的可视化界面
 */
@Composable
fun DynamicManagementConsole(
    dynamicEngine: UnifyDynamicEngine,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("概览", "组件管理", "配置管理", "更新管理", "监控", "日志")
    
    Column(modifier = modifier.fillMaxSize()) {
        // 顶部标题栏
        TopAppBar(
            title = { Text("Unify动态化管理控制台") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
        
        // 标签页导航
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        // 内容区域
        when (selectedTab) {
            0 -> OverviewPanel(dynamicEngine)
            1 -> ComponentManagementPanel(dynamicEngine)
            2 -> ConfigurationManagementPanel(dynamicEngine)
            3 -> UpdateManagementPanel(dynamicEngine)
            4 -> MonitoringPanel(dynamicEngine)
            5 -> LogPanel(dynamicEngine)
        }
    }
}

/**
 * 概览面板
 */
@Composable
private fun OverviewPanel(dynamicEngine: UnifyDynamicEngine) {
    val systemStatus by dynamicEngine.systemStatus.collectAsState()
    val metrics by dynamicEngine.performanceMetrics.collectAsState()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "系统概览",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            SystemStatusCard(systemStatus)
        }
        
        item {
            PerformanceMetricsCard(metrics)
        }
        
        item {
            QuickActionsCard(dynamicEngine)
        }
    }
}

/**
 * 组件管理面板
 */
@Composable
private fun ComponentManagementPanel(dynamicEngine: UnifyDynamicEngine) {
    val registeredComponents by dynamicEngine.registeredComponents.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "组件管理",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Button(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("添加组件")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(registeredComponents.entries.toList()) { (id, factory) ->
                ComponentCard(
                    componentId = id,
                    factory = factory,
                    onUnregister = { dynamicEngine.unregisterComponent(id) },
                    onReload = { 
                        // 重新加载组件
                        GlobalScope.launch {
                            dynamicEngine.reloadComponent(id)
                        }
                    }
                )
            }
        }
    }
    
    if (showAddDialog) {
        AddComponentDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { componentData ->
                GlobalScope.launch {
                    dynamicEngine.loadComponent(componentData)
                }
                showAddDialog = false
            }
        )
    }
}

/**
 * 配置管理面板
 */
@Composable
private fun ConfigurationManagementPanel(dynamicEngine: UnifyDynamicEngine) {
    val configurations by dynamicEngine.configurations.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "配置管理",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row {
                Button(
                    onClick = { 
                        GlobalScope.launch {
                            dynamicEngine.syncConfigurations()
                        }
                    }
                ) {
                    Icon(Icons.Default.Sync, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("同步配置")
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Button(
                    onClick = { showAddDialog = true }
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("添加配置")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(configurations.entries.toList()) { (key, value) ->
                ConfigurationCard(
                    key = key,
                    value = value,
                    onUpdate = { newValue ->
                        GlobalScope.launch {
                            dynamicEngine.updateConfiguration(key, newValue)
                        }
                    },
                    onDelete = {
                        GlobalScope.launch {
                            dynamicEngine.deleteConfiguration(key)
                        }
                    }
                )
            }
        }
    }
    
    if (showAddDialog) {
        AddConfigurationDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { key, value ->
                GlobalScope.launch {
                    dynamicEngine.updateConfiguration(key, value)
                }
                showAddDialog = false
            }
        )
    }
}

/**
 * 更新管理面板
 */
@Composable
private fun UpdateManagementPanel(dynamicEngine: UnifyDynamicEngine) {
    val updateHistory by dynamicEngine.updateHistory.collectAsState()
    var isCheckingUpdate by remember { mutableStateOf(false) }
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "更新管理",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Button(
                onClick = { 
                    isCheckingUpdate = true
                    GlobalScope.launch {
                        try {
                            dynamicEngine.checkForUpdates()
                        } finally {
                            isCheckingUpdate = false
                        }
                    }
                },
                enabled = !isCheckingUpdate
            ) {
                if (isCheckingUpdate) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("检查更新")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(updateHistory) { update ->
                UpdateHistoryCard(
                    update = update,
                    onRollback = {
                        GlobalScope.launch {
                            dynamicEngine.rollbackToVersion(update.version)
                        }
                    }
                )
            }
        }
    }
}

/**
 * 监控面板
 */
@Composable
private fun MonitoringPanel(dynamicEngine: UnifyDynamicEngine) {
    val metrics by dynamicEngine.performanceMetrics.collectAsState()
    val systemStatus by dynamicEngine.systemStatus.collectAsState()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "实时监控",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            RealTimeMetricsCard(metrics)
        }
        
        item {
            SystemHealthCard(systemStatus)
        }
        
        item {
            AlertsCard(dynamicEngine)
        }
    }
}

/**
 * 日志面板
 */
@Composable
private fun LogPanel(dynamicEngine: UnifyDynamicEngine) {
    val logs by dynamicEngine.logs.collectAsState()
    var filterLevel by remember { mutableStateOf("ALL") }
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "系统日志",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row {
                FilterChip(
                    selected = filterLevel == "ALL",
                    onClick = { filterLevel = "ALL" },
                    label = { Text("全部") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = filterLevel == "ERROR",
                    onClick = { filterLevel = "ERROR" },
                    label = { Text("错误") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = filterLevel == "WARN",
                    onClick = { filterLevel = "WARN" },
                    label = { Text("警告") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = filterLevel == "INFO",
                    onClick = { filterLevel = "INFO" },
                    label = { Text("信息") }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val filteredLogs = if (filterLevel == "ALL") {
                logs
            } else {
                logs.filter { it.level == filterLevel }
            }
            
            items(filteredLogs) { log ->
                LogEntryCard(log)
            }
        }
    }
}

/**
 * 系统状态卡片
 */
@Composable
private fun SystemStatusCard(status: SystemStatus) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "系统状态",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatusItem("引擎状态", status.engineStatus, getStatusColor(status.engineStatus))
                StatusItem("网络状态", status.networkStatus, getStatusColor(status.networkStatus))
                StatusItem("存储状态", status.storageStatus, getStatusColor(status.storageStatus))
            }
        }
    }
}

/**
 * 状态项
 */
@Composable
private fun StatusItem(label: String, status: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = status,
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 获取状态颜色
 */
private fun getStatusColor(status: String): Color {
    return when (status.uppercase()) {
        "RUNNING", "ONLINE", "HEALTHY" -> Color.Green
        "STOPPED", "OFFLINE", "ERROR" -> Color.Red
        "LOADING", "SYNCING", "WARNING" -> Color.Orange
        else -> Color.Gray
    }
}

/**
 * 系统状态数据类
 */
@Serializable
data class SystemStatus(
    val engineStatus: String = "RUNNING",
    val networkStatus: String = "ONLINE", 
    val storageStatus: String = "HEALTHY",
    val lastUpdate: Long = System.currentTimeMillis()
)

/**
 * 更新历史数据类
 */
@Serializable
data class UpdateHistory(
    val version: String,
    val timestamp: Long,
    val status: String,
    val description: String
)

/**
 * 日志条目数据类
 */
@Serializable
data class LogEntry(
    val timestamp: Long,
    val level: String,
    val message: String,
    val source: String = ""
)
