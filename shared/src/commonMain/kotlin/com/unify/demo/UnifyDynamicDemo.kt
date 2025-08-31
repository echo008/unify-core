package com.unify.demo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.unify.core.dynamic.*
import kotlinx.coroutines.*

/**
 * Unify动态化演示应用
 * 展示热更新、动态组件加载、配置管理等核心功能
 */
@Composable
fun UnifyDynamicDemo() {
    val dynamicEngine = remember { UnifyDynamicEngine() }
    val testFramework = remember { DynamicTestFramework(dynamicEngine) }
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("动态组件", "热更新", "配置管理", "测试验证", "管理控制台")
    
    // 初始化动态引擎
    LaunchedEffect(Unit) {
        dynamicEngine.initialize()
        
        // 预加载一些示例组件
        loadSampleComponents(dynamicEngine)
        loadSampleConfigurations(dynamicEngine)
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // 顶部标题栏
        TopAppBar(
            title = { 
                Text(
                    "Unify动态化演示",
                    fontWeight = FontWeight.Bold
                ) 
            },
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
            0 -> DynamicComponentsDemo(dynamicEngine)
            1 -> HotUpdateDemo(dynamicEngine)
            2 -> ConfigurationDemo(dynamicEngine)
            3 -> TestValidationDemo(testFramework)
            4 -> DynamicManagementConsole(dynamicEngine)
        }
    }
}

/**
 * 动态组件演示
 */
@Composable
private fun DynamicComponentsDemo(dynamicEngine: UnifyDynamicEngine) {
    val registeredComponents by dynamicEngine.registeredComponents.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedComponent by remember { mutableStateOf<String?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "动态组件演示",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Button(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("添加组件")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxSize()) {
            // 左侧组件列表
            Card(modifier = Modifier.weight(0.4f)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "已注册组件",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(registeredComponents.keys.toList()) { componentId ->
                            Card(
                                onClick = { selectedComponent = componentId },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedComponent == componentId) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    }
                                )
                            ) {
                                Text(
                                    text = componentId,
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 右侧组件预览
            Card(modifier = Modifier.weight(0.6f)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "组件预览",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (selectedComponent != null) {
                        val factory = registeredComponents[selectedComponent]
                        if (factory != null) {
                            ComponentPreview(
                                componentId = selectedComponent!!,
                                factory = factory
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "选择组件查看预览",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
    
    if (showAddDialog) {
        AddDynamicComponentDialog(
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
 * 热更新演示
 */
@Composable
private fun HotUpdateDemo(dynamicEngine: UnifyDynamicEngine) {
    val updateHistory by dynamicEngine.updateHistory.collectAsState()
    var isCheckingUpdate by remember { mutableStateOf(false) }
    var updateStatus by remember { mutableStateOf("就绪") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "热更新演示",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 更新控制面板
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "更新控制",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            isCheckingUpdate = true
                            updateStatus = "检查中..."
                            GlobalScope.launch {
                                try {
                                    delay(2000) // 模拟网络请求
                                    val hasUpdate = dynamicEngine.checkForUpdates()
                                    updateStatus = if (hasUpdate) "发现更新" else "已是最新"
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
                    
                    OutlinedButton(
                        onClick = {
                            GlobalScope.launch {
                                // 模拟应用更新
                                val mockUpdate = UpdatePackage(
                                    version = "1.0.${System.currentTimeMillis() % 100}",
                                    components = listOf(
                                        ComponentData(
                                            metadata = ComponentMetadata(
                                                name = "MockComponent",
                                                version = "1.0.0",
                                                description = "模拟更新组件"
                                            ),
                                            code = "@Composable fun MockComponent() { Text(\"更新后的组件\") }"
                                        )
                                    ),
                                    configurations = mapOf("mock_config" to "updated_value"),
                                    signature = "mock_signature",
                                    checksum = "mock_checksum"
                                )
                                
                                dynamicEngine.applyUpdate(mockUpdate)
                                updateStatus = "更新完成"
                            }
                        }
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("模拟更新")
                    }
                    
                    OutlinedButton(
                        onClick = {
                            GlobalScope.launch {
                                if (updateHistory.isNotEmpty()) {
                                    val lastUpdate = updateHistory.first()
                                    dynamicEngine.rollbackToVersion(lastUpdate.version)
                                    updateStatus = "回滚完成"
                                }
                            }
                        },
                        enabled = updateHistory.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Undo, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("回滚")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "状态: $updateStatus",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 更新历史
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "更新历史",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (updateHistory.isEmpty()) {
                    Text(
                        text = "暂无更新历史",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(200.dp)
                    ) {
                        items(updateHistory) { update ->
                            UpdateHistoryItem(update)
                        }
                    }
                }
            }
        }
    }
}

/**
 * 配置管理演示
 */
@Composable
private fun ConfigurationDemo(dynamicEngine: UnifyDynamicEngine) {
    val configurations by dynamicEngine.configurations.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "配置管理演示",
                style = MaterialTheme.typography.headlineSmall,
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
                
                OutlinedButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("添加配置")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(configurations.entries.toList()) { (key, value) ->
                ConfigurationItem(
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
 * 测试验证演示
 */
@Composable
private fun TestValidationDemo(testFramework: DynamicTestFramework) {
    DynamicTestRunner(
        testFramework = testFramework,
        modifier = Modifier.fillMaxSize()
    )
}

/**
 * 组件预览
 */
@Composable
private fun ComponentPreview(
    componentId: String,
    factory: ComponentFactory
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "组件信息",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val metadata = factory.getMetadata()
            Text("名称: ${metadata.name}")
            Text("版本: ${metadata.version}")
            Text("描述: ${metadata.description}")
            Text("类型: ${metadata.type}")
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "组件渲染",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Card {
                Box(modifier = Modifier.padding(16.dp)) {
                    try {
                        factory.CreateComponent(
                            props = mapOf(
                                "text" to "示例文本",
                                "fontSize" to 16f,
                                "color" to "#FF0000"
                            )
                        ) {
                            // 子组件内容
                        }
                    } catch (e: Exception) {
                        Text(
                            text = "组件渲染错误: ${e.message}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

/**
 * 更新历史项
 */
@Composable
private fun UpdateHistoryItem(update: UpdateHistory) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "版本 ${update.version}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = update.description,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Chip(
                onClick = { },
                label = { Text(update.status) }
            )
        }
    }
}

/**
 * 配置项
 */
@Composable
private fun ConfigurationItem(
    key: String,
    value: String,
    onUpdate: (String) -> Unit,
    onDelete: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var editValue by remember { mutableStateOf(value) }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = key,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                
                Row {
                    if (isEditing) {
                        IconButton(
                            onClick = {
                                onUpdate(editValue)
                                isEditing = false
                            }
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "保存")
                        }
                        IconButton(
                            onClick = {
                                editValue = value
                                isEditing = false
                            }
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "取消")
                        }
                    } else {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "编辑")
                        }
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "删除")
                        }
                    }
                }
            }
            
            if (isEditing) {
                OutlinedTextField(
                    value = editValue,
                    onValueChange = { editValue = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false
                )
            } else {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 添加动态组件对话框
 */
@Composable
private fun AddDynamicComponentDialog(
    onDismiss: () -> Unit,
    onAdd: (ComponentData) -> Unit
) {
    var componentName by remember { mutableStateOf("") }
    var componentVersion by remember { mutableStateOf("1.0.0") }
    var componentDescription by remember { mutableStateOf("") }
    var componentCode by remember { mutableStateOf("""@Composable
fun MyComponent() {
    Text("Hello Dynamic Component!")
}""") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加动态组件") },
        text = {
            Column {
                OutlinedTextField(
                    value = componentName,
                    onValueChange = { componentName = it },
                    label = { Text("组件名称") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = componentVersion,
                    onValueChange = { componentVersion = it },
                    label = { Text("版本") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = componentDescription,
                    onValueChange = { componentDescription = it },
                    label = { Text("描述") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = componentCode,
                    onValueChange = { componentCode = it },
                    label = { Text("组件代码") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val componentData = ComponentData(
                        metadata = ComponentMetadata(
                            name = componentName,
                            version = componentVersion,
                            description = componentDescription
                        ),
                        code = componentCode
                    )
                    onAdd(componentData)
                },
                enabled = componentName.isNotEmpty() && componentCode.isNotEmpty()
            ) {
                Text("添加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 加载示例组件
 */
private suspend fun loadSampleComponents(dynamicEngine: UnifyDynamicEngine) {
    // 注册内置组件工厂
    dynamicEngine.registerComponentFactory("DynamicText", DynamicTextComponentFactory())
    dynamicEngine.registerComponentFactory("DynamicButton", DynamicButtonComponentFactory())
    dynamicEngine.registerComponentFactory("DynamicImage", DynamicImageComponentFactory())
    dynamicEngine.registerComponentFactory("DynamicList", DynamicListComponentFactory())
    dynamicEngine.registerComponentFactory("DynamicForm", DynamicFormComponentFactory())
    
    // 加载示例动态组件
    val sampleComponent = ComponentData(
        metadata = ComponentMetadata(
            name = "WelcomeCard",
            version = "1.0.0",
            description = "欢迎卡片组件"
        ),
        code = """
            @Composable
            fun WelcomeCard() {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "欢迎使用Unify动态化",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("这是一个动态加载的组件示例")
                    }
                }
            }
        """.trimIndent()
    )
    
    dynamicEngine.loadComponent(sampleComponent)
}

/**
 * 加载示例配置
 */
private suspend fun loadSampleConfigurations(dynamicEngine: UnifyDynamicEngine) {
    val sampleConfigs = mapOf(
        "app_name" to "Unify动态化演示",
        "theme_color" to "#6200EE",
        "enable_debug" to "true",
        "api_endpoint" to "https://api.unify.com",
        "cache_size" to "50MB",
        "update_interval" to "3600"
    )
    
    sampleConfigs.forEach { (key, value) ->
        dynamicEngine.updateConfiguration(key, value)
    }
}
