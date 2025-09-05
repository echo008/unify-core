package com.unify.core.dynamic

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.unify.core.platform.getCurrentTimeMillis

/**
 * 动态组件演示应用
 */
@Composable
fun UnifyDynamicDemo() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("组件加载", "热更新", "配置管理", "安全验证")
    
    Column(modifier = Modifier.fillMaxSize()) {
        // 顶部标题
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🔄 动态组件系统演示",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "实时组件加载、热更新和配置管理",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
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
        
        // 内容区域
        when (selectedTab) {
            0 -> ComponentLoadingDemo()
            1 -> HotUpdateDemo()
            2 -> ConfigurationDemo()
            3 -> SecurityDemo()
        }
    }
}

@Composable
private fun ComponentLoadingDemo() {
    var loadedComponents by remember { mutableStateOf<List<ComponentInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "组件加载演示",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                scope.launch {
                                    isLoading = true
                                    // 模拟加载组件
                                    kotlinx.coroutines.delay(1000)
                                    loadedComponents = getSampleComponents()
                                    isLoading = false
                                }
                            },
                            enabled = !isLoading
                        ) {
                            Text("加载示例组件")
                        }
                        
                        Button(
                            onClick = { loadedComponents = emptyList() },
                            enabled = loadedComponents.isNotEmpty()
                        ) {
                            Text("清空组件")
                        }
                    }
                    
                    if (isLoading) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
        
        items(loadedComponents) { component ->
            ComponentInfoCard(component)
        }
    }
}

@Composable
private fun HotUpdateDemo() {
    var updateAvailable by remember { mutableStateOf(false) }
    var updateProgress by remember { mutableFloatStateOf(0f) }
    var isUpdating by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "热更新演示",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = {
                        scope.launch {
                            updateAvailable = true
                        }
                    }
                ) {
                    Text("检查更新")
                }
                
                if (updateAvailable) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("发现新版本可用！")
                    
                    Button(
                        onClick = {
                            scope.launch {
                                isUpdating = true
                                for (i in 1..100) {
                                    updateProgress = i / 100f
                                    kotlinx.coroutines.delay(50)
                                }
                                isUpdating = false
                                updateAvailable = false
                                updateProgress = 0f
                            }
                        },
                        enabled = !isUpdating
                    ) {
                        Text("应用更新")
                    }
                }
                
                if (isUpdating) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { updateProgress },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("更新进度: ${(updateProgress * 100).toInt()}%")
                }
            }
        }
    }
}

@Composable
private fun ConfigurationDemo() {
    var configs by remember { mutableStateOf(getSampleConfigs()) }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "配置管理演示",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "动态配置加载和实时更新",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        
        items(configs) { config ->
            ConfigCard(config) { updatedConfig ->
                configs = configs.map { 
                    if (it.id == updatedConfig.id) updatedConfig else it 
                }
            }
        }
    }
}

@Composable
private fun SecurityDemo() {
    var securityStatus by remember { mutableStateOf("安全") }
    var violations by remember { mutableStateOf<List<SecurityViolation>>(emptyList()) }
    
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "安全验证演示",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("安全状态:")
                    Text(
                        text = securityStatus,
                        color = when (securityStatus) {
                            "安全" -> MaterialTheme.colorScheme.primary
                            "警告" -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = {
                        violations = getSampleViolations()
                        securityStatus = if (violations.isNotEmpty()) "警告" else "安全"
                    }
                ) {
                    Text("执行安全扫描")
                }
            }
        }
        
        if (violations.isNotEmpty()) {
            violations.forEach { violation ->
                SecurityViolationCard(violation)
            }
        }
    }
}

@Composable
private fun ComponentInfoCard(component: ComponentInfo) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = component.component.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = component.state.name,
                    color = when (component.state) {
                        ComponentState.LOADED -> MaterialTheme.colorScheme.primary
                        ComponentState.ERROR -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            }
            
            Text(
                text = "版本: ${component.component.version}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "类型: ${component.component.type}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun ConfigCard(
    config: DynamicConfiguration,
    onUpdate: (DynamicConfiguration) -> Unit
) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = config.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "类别: ${config.category}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "优先级: ${config.priority}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun SecurityViolationCard(violation: SecurityViolation) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = violation.type.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = violation.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            if (violation.suggestion.isNotEmpty()) {
                Text(
                    text = "建议: ${violation.suggestion}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

// 示例数据
private fun getSampleComponents(): List<ComponentInfo> {
    return listOf(
        ComponentInfo(
            component = DynamicComponent(
                id = "button_1",
                name = "动态按钮",
                version = "1.0.0",
                type = DynamicComponentType.COMPOSE_UI,
                metadata = mapOf("description" to "可交互的动态按钮组件"),
                dependencies = emptyList(),
                config = mapOf("theme" to "material3"),
                content = "ButtonComponent",
                checksum = "abc123",
                signature = "sig123"
            ),
            state = ComponentState.LOADED,
            loadTime = getCurrentTimeMillis(),
            lastUpdate = getCurrentTimeMillis(),
            errorMessage = null,
            loaded = null,
            metrics = ComponentMetrics(
                componentId = "button_1",
                loadCount = 1,
                errorCount = 0,
                lastAccess = getCurrentTimeMillis(),
                totalExecutionTime = 150L,
                averageResponseTime = 16L
            ),
            dependencies = emptyList(),
            dependents = emptyList()
        ),
        ComponentInfo(
            component = DynamicComponent(
                id = "chart_1",
                name = "图表组件",
                version = "2.1.0",
                type = DynamicComponentType.COMPOSE_UI,
                metadata = mapOf("description" to "数据可视化图表组件"),
                dependencies = listOf("data_processor"),
                config = mapOf("chartType" to "line"),
                content = "ChartComponent",
                checksum = "def456",
                signature = "sig456"
            ),
            state = ComponentState.ACTIVE,
            loadTime = getCurrentTimeMillis(),
            lastUpdate = getCurrentTimeMillis(),
            errorMessage = null,
            loaded = null,
            metrics = ComponentMetrics(
                componentId = "chart_1",
                loadCount = 2,
                errorCount = 0,
                lastAccess = getCurrentTimeMillis(),
                totalExecutionTime = 200L,
                averageResponseTime = 24L
            ),
            dependencies = listOf("data_processor"),
            dependents = emptyList()
        )
    )
}

private fun getSampleConfigs(): List<DynamicConfiguration> {
    return listOf(
        DynamicConfiguration(
            id = "theme_config",
            name = "主题配置",
            version = "1.0",
            category = ConfigCategory.UI_THEME,
            priority = ConfigPriority.HIGH,
            scope = ConfigScope.GLOBAL,
            values = mapOf(
                "primaryColor" to ConfigValue.StringValue("#2196F3"),
                "darkMode" to ConfigValue.BooleanValue(false)
            )
        )
    )
}

private fun getSampleViolations(): List<SecurityViolation> {
    return listOf(
        SecurityViolation(
            type = ViolationType.PERMISSION_ABUSE,
            severity = ViolationSeverity.WARNING,
            description = "组件请求了相机权限",
            suggestion = "确认是否真的需要相机权限"
        )
    )
}
