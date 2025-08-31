package com.unify.demo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.unify.core.architecture.UnifyApp
import com.unify.core.ui.components.*
import com.unify.core.mvi.*
import com.unify.core.data.UnifyResult
import com.unify.core.performance.UnifyPerformanceMonitor
import com.unify.core.performance.UnifyComposeOptimizer.PerformanceTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Unify-Core 完整示例应用
 * 展示所有核心功能和最佳实践
 */

/**
 * 1. 示例应用主入口
 */
@Composable
fun UnifyDemoApp() {
    // 初始化性能监控
    LaunchedEffect(Unit) {
        UnifyPerformanceMonitor.initialize()
    }
    
    UnifyApp {
        DemoNavigationHost()
    }
}

/**
 * 2. 导航主机
 */
@Composable
fun DemoNavigationHost() {
    var currentScreen by remember { mutableStateOf(DemoScreen.HOME) }
    
    when (currentScreen) {
        DemoScreen.HOME -> HomeScreen(
            onNavigate = { screen -> currentScreen = screen }
        )
        DemoScreen.TASK_LIST -> TaskListScreen(
            onNavigateBack = { currentScreen = DemoScreen.HOME }
        )
        DemoScreen.PROFILE -> ProfileScreen(
            onNavigateBack = { currentScreen = DemoScreen.HOME }
        )
        DemoScreen.SETTINGS -> SettingsScreen(
            onNavigateBack = { currentScreen = DemoScreen.HOME }
        )
        DemoScreen.PERFORMANCE -> PerformanceScreen(
            onNavigateBack = { currentScreen = DemoScreen.HOME }
        )
    }
}

/**
 * 3. 屏幕枚举
 */
enum class DemoScreen {
    HOME, TASK_LIST, PROFILE, SETTINGS, PERFORMANCE
}

/**
 * 4. 首页屏幕
 */
@Composable
fun HomeScreen(
    onNavigate: (DemoScreen) -> Unit
) {
    PerformanceTracker("HomeScreen") {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 标题卡片
            UnifyCard {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🚀 Unify-Core Demo",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "生产级 Kotlin Multiplatform Compose 框架",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // 功能导航
            UnifyCard {
                Column {
                    Text(
                        text = "📱 功能演示",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    DemoNavigationItem(
                        title = "待办事项",
                        description = "MVI 架构 + 数据持久化",
                        icon = "✅",
                        onClick = { onNavigate(DemoScreen.TASK_LIST) }
                    )
                    
                    DemoNavigationItem(
                        title = "用户资料",
                        description = "表单处理 + 数据验证",
                        icon = "👤",
                        onClick = { onNavigate(DemoScreen.PROFILE) }
                    )
                    
                    DemoNavigationItem(
                        title = "应用设置",
                        description = "主题切换 + 偏好设置",
                        icon = "⚙️",
                        onClick = { onNavigate(DemoScreen.SETTINGS) }
                    )
                    
                    DemoNavigationItem(
                        title = "性能监控",
                        description = "实时性能指标展示",
                        icon = "📊",
                        onClick = { onNavigate(DemoScreen.PERFORMANCE) }
                    )
                }
            }
            
            // 架构特性
            UnifyCard {
                Column {
                    Text(
                        text = "🏗️ 架构特性",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    FeatureItem("🎯", "100% 纯 Compose 语法")
                    FeatureItem("🔄", "85%+ 代码复用率")
                    FeatureItem("📱", "多平台支持")
                    FeatureItem("⚡", "原生性能")
                    FeatureItem("🎨", "统一 UI 组件库")
                    FeatureItem("🔧", "模块化架构")
                    FeatureItem("📈", "性能监控")
                    FeatureItem("🧪", "全面测试覆盖")
                }
            }
        }
    }
}

/**
 * 5. 待办事项屏幕
 */
@Composable
fun TaskListScreen(
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val viewModel = remember { TaskViewModel(scope) }
    
    PerformanceTracker("TaskListScreen") {
        UnifyMVIContainer(
            stateManager = viewModel,
            onEffect = { effect ->
                when (effect) {
                    is TaskEffect.ShowMessage -> {
                        // 处理消息显示
                        UnifyPerformanceMonitor.recordMetric("demo_message_shown", 1.0, "count",
                            mapOf("message_type" to "task_effect"))
                    }
                }
            }
        ) { state, isLoading, onIntent ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // 顶部栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UnifyButton(
                        onClick = onNavigateBack,
                        text = "← 返回"
                    )
                    Text(
                        text = "待办事项",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.width(80.dp))
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 添加待办
                var newTaskText by remember { mutableStateOf("") }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UnifyTextField(
                        value = newTaskText,
                        onValueChange = { newTaskText = it },
                        placeholder = "输入新的任务项",
                        modifier = Modifier.weight(1f)
                    )
                    UnifyButton(
                        onClick = {
                            if (newTaskText.isNotBlank()) {
                                onIntent(TaskIntent.AddTask(newTaskText))
                                newTaskText = ""
                            }
                        },
                        text = "添加"
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 待办列表
                if (isLoading) {
                    UnifyLoadingIndicator()
                } else if (state.tasks.isEmpty()) {
                    UnifyEmptyState(
                        message = "还没有任务项，添加一个吧！"
                    )
                } else {
                    UnifyLazyList(
                        items = state.tasks,
                        key = { it.id }
                    ) { task ->
                        TaskItem(
                            task = task,
                            onToggle = { onIntent(TaskIntent.ToggleTask(task.id)) },
                            onDelete = { onIntent(TaskIntent.DeleteTask(task.id)) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 6. 待办事项 MVI 实现
 */
data class TaskState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class TaskIntent {
    data class AddTask(val text: String) : TaskIntent()
    data class ToggleTask(val id: String) : TaskIntent()
    data class DeleteTask(val id: String) : TaskIntent()
    object LoadTasks : TaskIntent()
}

sealed class TaskEffect {
    data class ShowMessage(val message: String) : TaskEffect()
}

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

class TaskViewModel(private val scope: CoroutineScope) : UnifyStateManager<TaskState, TaskIntent, TaskEffect> {
    override val state = MutableStateFlow(TaskState())
    override val effects = MutableSharedFlow<TaskEffect>()
    
    init {
        handleIntent(TaskIntent.LoadTasks)
    }
    
    override fun handleIntent(intent: TaskIntent) {
        when (intent) {
            is TaskIntent.AddTask -> {
                val newTask = Task(text = intent.text)
                updateState { 
                    copy(tasks = tasks + newTask) 
                }
            }
            is TaskIntent.ToggleTask -> {
                updateState {
                    copy(
                        tasks = tasks.map { task ->
                            if (task.id == intent.id) {
                                task.copy(isCompleted = !task.isCompleted)
                            } else {
                                task
                            }
                        }
                    )
                }
            }
            is TaskIntent.DeleteTask -> {
                updateState {
                    copy(tasks = tasks.filter { it.id != intent.id })
                }
            }
            TaskIntent.LoadTasks -> {
                updateState { copy(isLoading = true) }
                scope.launch {
                    delay(500) // 模拟加载
                    updateState { copy(isLoading = false) }
                }
            }
        }
    }
    
    private fun updateState(update: TaskState.() -> TaskState) {
        state.value = state.value.update()
    }
}

@Composable
fun TaskItem(
    task: Task,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    UnifyCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                UnifyCheckbox(
                    checked = task.isCompleted,
                    onCheckedChange = { onToggle() }
                )
                UnifyText(
                    text = task.text,
                    style = if (task.isCompleted) {
                        UnifyTheme.typography.body1.copy(
                            textDecoration = TextDecoration.LineThrough,
                            color = UnifyTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    } else {
                        UnifyTheme.typography.body1
                    }
                )
            }
            UnifyIconButton(
                onClick = onDelete,
                icon = "🗑️"
            )
        }
    }
}

/**
 * 7. 待办事项组件
 * 8. 通用组件
 */
@Composable
fun DemoNavigationItem(
    title: String,
    description: String,
    icon: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "→",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun FeatureItem(
    icon: String,
    text: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
