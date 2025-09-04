package com.unify.demo

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
import com.unify.ui.components.container.UnifySection
import com.unify.ui.components.feedback.UnifyProgress
import kotlinx.coroutines.delay

/**
 * Unify动态组件演示界面
 * 展示跨平台动态加载和热更新功能
 */

data class DynamicComponent(
    val id: String,
    val name: String,
    val version: String,
    val status: ComponentStatus,
    val description: String,
    val size: String,
    val lastUpdated: String
)

enum class ComponentStatus {
    INSTALLED, AVAILABLE, UPDATING, ERROR
}

data class UpdateInfo(
    val componentId: String,
    val currentVersion: String,
    val newVersion: String,
    val updateSize: String,
    val changelog: List<String>
)

@Composable
fun UnifyDynamicDemo(
    modifier: Modifier = Modifier
) {
    var components by remember { mutableStateOf(getDefaultComponents()) }
    var availableUpdates by remember { mutableStateOf(getAvailableUpdates()) }
    var isRefreshing by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            DynamicSystemHeader(
                onRefresh = {
                    isRefreshing = true
                    // 模拟刷新
                },
                isRefreshing = isRefreshing
            )
        }
        
        item {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("已安装") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("可用更新") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("组件商店") }
                )
            }
        }
        
        when (selectedTab) {
            0 -> {
                items(components.filter { it.status == ComponentStatus.INSTALLED }) { component ->
                    InstalledComponentCard(
                        component = component,
                        onUninstall = { componentId ->
                            components = components.map { comp ->
                                if (comp.id == componentId) {
                                    comp.copy(status = ComponentStatus.AVAILABLE)
                                } else comp
                            }
                        }
                    )
                }
            }
            1 -> {
                items(availableUpdates) { update ->
                    UpdateCard(
                        update = update,
                        onUpdate = { updateId ->
                            // 处理更新
                        }
                    )
                }
            }
            2 -> {
                items(components.filter { it.status == ComponentStatus.AVAILABLE }) { component ->
                    AvailableComponentCard(
                        component = component,
                        onInstall = { componentId ->
                            components = components.map { comp ->
                                if (comp.id == componentId) {
                                    comp.copy(status = ComponentStatus.UPDATING)
                                } else comp
                            }
                        }
                    )
                }
            }
        }
    }
    
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            delay(2000)
            isRefreshing = false
        }
    }
}

@Composable
private fun DynamicSystemHeader(
    onRefresh: () -> Unit,
    isRefreshing: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🔄 动态组件系统",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "实时组件管理和热更新",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
                
                IconButton(
                    onClick = onRefresh,
                    enabled = !isRefreshing
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("🔄", style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SystemStatusItem("已安装", "12", Color(0xFF4CAF50))
                SystemStatusItem("可更新", "3", Color(0xFFFF9800))
                SystemStatusItem("可用", "8", Color(0xFF2196F3))
            }
        }
    }
}

@Composable
private fun SystemStatusItem(
    label: String,
    count: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun InstalledComponentCard(
    component: DynamicComponent,
    onUninstall: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = component.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "版本 ${component.version}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = component.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = Color(0xFF4CAF50)
                ) {
                    Text(
                        text = "已安装",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "大小: ${component.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "更新: ${component.lastUpdated}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                OutlinedButton(
                    onClick = { onUninstall(component.id) },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("卸载")
                }
            }
        }
    }
}

@Composable
private fun UpdateCard(
    update: UpdateInfo,
    onUpdate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "组件更新可用",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${update.currentVersion} → ${update.newVersion}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = Color(0xFFFF9800)
                ) {
                    Text(
                        text = "可更新",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "更新内容:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            update.changelog.forEach { change ->
                Text(
                    text = "• $change",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "大小: ${update.updateSize}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Button(
                    onClick = { onUpdate(update.componentId) }
                ) {
                    Text("更新")
                }
            }
        }
    }
}

@Composable
private fun AvailableComponentCard(
    component: DynamicComponent,
    onInstall: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = component.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "版本 ${component.version}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = component.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = Color(0xFF2196F3)
                ) {
                    Text(
                        text = "可安装",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "大小: ${component.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Button(
                    onClick = { onInstall(component.id) }
                ) {
                    Text("安装")
                }
            }
        }
    }
}

private fun getDefaultComponents(): List<DynamicComponent> {
    return listOf(
        DynamicComponent(
            id = "ui_enhanced",
            name = "增强UI组件包",
            version = "2.1.0",
            status = ComponentStatus.INSTALLED,
            description = "提供高级UI组件和动画效果",
            size = "2.3 MB",
            lastUpdated = "2天前"
        ),
        DynamicComponent(
            id = "analytics_pro",
            name = "专业分析工具",
            version = "1.5.2",
            status = ComponentStatus.INSTALLED,
            description = "高级数据分析和可视化功能",
            size = "1.8 MB",
            lastUpdated = "1周前"
        ),
        DynamicComponent(
            id = "camera_filters",
            name = "相机滤镜包",
            version = "3.0.1",
            status = ComponentStatus.AVAILABLE,
            description = "丰富的相机滤镜和特效",
            size = "4.2 MB",
            lastUpdated = "3天前"
        ),
        DynamicComponent(
            id = "ai_assistant",
            name = "AI智能助手",
            version = "1.2.0",
            status = ComponentStatus.AVAILABLE,
            description = "智能对话和任务辅助功能",
            size = "5.1 MB",
            lastUpdated = "1天前"
        )
    )
}

private fun getAvailableUpdates(): List<UpdateInfo> {
    return listOf(
        UpdateInfo(
            componentId = "ui_enhanced",
            currentVersion = "2.1.0",
            newVersion = "2.2.0",
            updateSize = "1.2 MB",
            changelog = listOf(
                "新增暗黑模式支持",
                "优化动画性能",
                "修复已知问题"
            )
        ),
        UpdateInfo(
            componentId = "analytics_pro",
            currentVersion = "1.5.2",
            newVersion = "1.6.0",
            updateSize = "0.8 MB",
            changelog = listOf(
                "新增实时数据监控",
                "增强图表功能",
                "提升数据处理速度"
            )
        )
    )
}
