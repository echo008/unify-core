package com.unify.dynamic.demo

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
 * 动态系统演示应用
 * 展示动态组件加载、热更新和配置管理功能
 */
@Composable
fun UnifyDynamicDemo() {
    var dynamicState by remember { mutableStateOf(DynamicDemoState()) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        scope.launch {
            // 模拟动态组件加载
            dynamicState = dynamicState.copy(isLoading = true)
            delay(2000)
            dynamicState = dynamicState.copy(
                isLoading = false,
                loadedComponents = generateDynamicComponents(),
                lastUpdateTime = System.currentTimeMillis()
            )
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "动态系统演示",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // 系统状态卡片
        DynamicSystemStatusCard(
            state = dynamicState,
            onRefresh = {
                scope.launch {
                    dynamicState = dynamicState.copy(isLoading = true)
                    delay(1000)
                    dynamicState = dynamicState.copy(
                        isLoading = false,
                        loadedComponents = generateDynamicComponents(),
                        lastUpdateTime = System.currentTimeMillis()
                    )
                }
            },
            onHotUpdate = {
                scope.launch {
                    dynamicState = dynamicState.copy(isUpdating = true)
                    delay(1500)
                    dynamicState = dynamicState.copy(
                        isUpdating = false,
                        hotUpdateCount = dynamicState.hotUpdateCount + 1,
                        lastUpdateTime = System.currentTimeMillis()
                    )
                }
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 动态组件列表
        if (dynamicState.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("正在加载动态组件...")
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(dynamicState.loadedComponents) { component ->
                    DynamicComponentCard(
                        component = component,
                        onToggle = { enabled ->
                            val updatedComponents = dynamicState.loadedComponents.map {
                                if (it.id == component.id) it.copy(isEnabled = enabled) else it
                            }
                            dynamicState = dynamicState.copy(loadedComponents = updatedComponents)
                        },
                        onConfigure = {
                            // 打开配置对话框
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DynamicSystemStatusCard(
    state: DynamicDemoState,
    onRefresh: () -> Unit,
    onHotUpdate: () -> Unit
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
                    text = "系统状态",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Row {
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "刷新"
                        )
                    }
                    IconButton(onClick = onHotUpdate) {
                        Icon(
                            imageVector = Icons.Default.Update,
                            contentDescription = "热更新"
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatusItem(
                    label = "已加载组件",
                    value = "${state.loadedComponents.size}",
                    color = MaterialTheme.colorScheme.primary
                )
                StatusItem(
                    label = "启用组件",
                    value = "${state.loadedComponents.count { it.isEnabled }}",
                    color = Color.Green
                )
                StatusItem(
                    label = "热更新次数",
                    value = "${state.hotUpdateCount}",
                    color = Color.Orange
                )
            }
            
            if (state.isUpdating) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "正在执行热更新...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            if (state.lastUpdateTime > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "最后更新: ${formatUpdateTime(state.lastUpdateTime)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatusItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
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
private fun DynamicComponentCard(
    component: DynamicComponent,
    onToggle: (Boolean) -> Unit,
    onConfigure: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = component.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = component.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "版本: ${component.version}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Switch(
                    checked = component.isEnabled,
                    onCheckedChange = onToggle
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    color = when (component.status) {
                        ComponentStatus.ACTIVE -> Color.Green
                        ComponentStatus.INACTIVE -> Color.Gray
                        ComponentStatus.ERROR -> Color.Red
                        ComponentStatus.UPDATING -> Color.Orange
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = component.status.displayName,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
                
                Row {
                    TextButton(onClick = onConfigure) {
                        Text("配置")
                    }
                    TextButton(
                        onClick = { /* 查看详情 */ }
                    ) {
                        Text("详情")
                    }
                }
            }
            
            // 组件配置预览
            if (component.configurations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "配置项:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
                component.configurations.take(3).forEach { config ->
                    Text(
                        text = "• ${config.key}: ${config.value}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                if (component.configurations.size > 3) {
                    Text(
                        text = "... 还有 ${component.configurations.size - 3} 项配置",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

private fun generateDynamicComponents(): List<DynamicComponent> {
    return listOf(
        DynamicComponent(
            id = "ui_theme",
            name = "主题管理器",
            description = "动态主题切换和自定义",
            version = "1.2.0",
            status = ComponentStatus.ACTIVE,
            isEnabled = true,
            configurations = listOf(
                ComponentConfig("primary_color", "#6200EE"),
                ComponentConfig("dark_mode", "auto"),
                ComponentConfig("animation_duration", "300ms")
            )
        ),
        DynamicComponent(
            id = "data_sync",
            name = "数据同步器",
            description = "实时数据同步和缓存管理",
            version = "2.1.3",
            status = ComponentStatus.ACTIVE,
            isEnabled = true,
            configurations = listOf(
                ComponentConfig("sync_interval", "30s"),
                ComponentConfig("cache_size", "100MB"),
                ComponentConfig("offline_mode", "enabled")
            )
        ),
        DynamicComponent(
            id = "analytics",
            name = "分析统计",
            description = "用户行为分析和统计",
            version = "1.0.5",
            status = ComponentStatus.INACTIVE,
            isEnabled = false,
            configurations = listOf(
                ComponentConfig("tracking_enabled", "false"),
                ComponentConfig("report_interval", "1h")
            )
        ),
        DynamicComponent(
            id = "notification",
            name = "通知中心",
            description = "推送通知和消息管理",
            version = "1.3.1",
            status = ComponentStatus.UPDATING,
            isEnabled = true,
            configurations = listOf(
                ComponentConfig("push_enabled", "true"),
                ComponentConfig("sound_enabled", "true"),
                ComponentConfig("vibration_enabled", "false")
            )
        ),
        DynamicComponent(
            id = "security",
            name = "安全模块",
            description = "加密和安全验证",
            version = "2.0.0",
            status = ComponentStatus.ERROR,
            isEnabled = false,
            configurations = listOf(
                ComponentConfig("encryption_level", "AES256"),
                ComponentConfig("biometric_auth", "enabled"),
                ComponentConfig("session_timeout", "30m")
            )
        )
    )
}

private fun formatUpdateTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 60000 -> "刚刚"
        diff < 3600000 -> "${diff / 60000}分钟前"
        diff < 86400000 -> "${diff / 3600000}小时前"
        else -> "${diff / 86400000}天前"
    }
}

data class DynamicDemoState(
    val isLoading: Boolean = false,
    val isUpdating: Boolean = false,
    val loadedComponents: List<DynamicComponent> = emptyList(),
    val hotUpdateCount: Int = 0,
    val lastUpdateTime: Long = 0
)

data class DynamicComponent(
    val id: String,
    val name: String,
    val description: String,
    val version: String,
    val status: ComponentStatus,
    val isEnabled: Boolean,
    val configurations: List<ComponentConfig>
)

data class ComponentConfig(
    val key: String,
    val value: String
)

enum class ComponentStatus(val displayName: String) {
    ACTIVE("运行中"),
    INACTIVE("未激活"),
    ERROR("错误"),
    UPDATING("更新中")
}
