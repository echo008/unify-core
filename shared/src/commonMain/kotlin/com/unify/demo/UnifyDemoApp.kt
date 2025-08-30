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
        DemoScreen.TODO_LIST -> TodoListScreen(
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
    HOME, TODO_LIST, PROFILE, SETTINGS, PERFORMANCE
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
                        onClick = { onNavigate(DemoScreen.TODO_LIST) }
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
fun TodoListScreen(
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val viewModel = remember { TodoViewModel(scope) }
    
    PerformanceTracker("TodoListScreen") {
        UnifyMVIContainer(
            stateManager = viewModel,
            onEffect = { effect ->
                when (effect) {
                    is TodoEffect.ShowMessage -> {
                        // 显示消息
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
                var newTodoText by remember { mutableStateOf("") }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UnifyTextField(
                        value = newTodoText,
                        onValueChange = { newTodoText = it },
                        placeholder = "输入新的待办事项",
                        modifier = Modifier.weight(1f)
                    )
                    UnifyButton(
                        onClick = {
                            if (newTodoText.isNotBlank()) {
                                onIntent(TodoIntent.AddTodo(newTodoText))
                                newTodoText = ""
                            }
                        },
                        text = "添加"
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 待办列表
                if (isLoading) {
                    UnifyLoadingIndicator()
                } else if (state.todos.isEmpty()) {
                    UnifyEmptyState(
                        message = "还没有待办事项，添加一个吧！"
                    )
                } else {
                    UnifyLazyList(
                        items = state.todos,
                        key = { it.id }
                    ) { todo ->
                        TodoItem(
                            todo = todo,
                            onToggle = { onIntent(TodoIntent.ToggleTodo(todo.id)) },
                            onDelete = { onIntent(TodoIntent.DeleteTodo(todo.id)) }
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
data class TodoState(
    val todos: List<Todo> = emptyList(),
    val filter: TodoFilter = TodoFilter.ALL
) : UnifyState

sealed class TodoIntent : UnifyIntent {
    data class AddTodo(val text: String) : TodoIntent()
    data class ToggleTodo(val id: String) : TodoIntent()
    data class DeleteTodo(val id: String) : TodoIntent()
    data class SetFilter(val filter: TodoFilter) : TodoIntent()
    object LoadTodos : TodoIntent()
}

sealed class TodoEffect : UnifyEffect {
    data class ShowMessage(val message: String) : TodoEffect()
}

data class Todo(
    val id: String,
    val text: String,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

enum class TodoFilter {
    ALL, ACTIVE, COMPLETED
}

class TodoViewModel(scope: CoroutineScope) : UnifyViewModel<TodoIntent, TodoState, TodoEffect>(
    initialState = TodoState(),
    scope = scope
) {
    
    init {
        handleIntent(TodoIntent.LoadTodos)
    }
    
    override fun handleIntent(intent: TodoIntent) {
        when (intent) {
            is TodoIntent.AddTodo -> {
                val newTodo = Todo(
                    id = generateId(),
                    text = intent.text
                )
                updateState { state ->
                    state.copy(todos = state.todos + newTodo)
                }
                sendEffect(TodoEffect.ShowMessage("已添加待办事项"))
            }
            
            is TodoIntent.ToggleTodo -> {
                updateState { state ->
                    state.copy(
                        todos = state.todos.map { todo ->
                            if (todo.id == intent.id) {
                                todo.copy(isCompleted = !todo.isCompleted)
                            } else {
                                todo
                            }
                        }
                    )
                }
            }
            
            is TodoIntent.DeleteTodo -> {
                updateState { state ->
                    state.copy(
                        todos = state.todos.filter { it.id != intent.id }
                    )
                }
                sendEffect(TodoEffect.ShowMessage("已删除待办事项"))
            }
            
            is TodoIntent.SetFilter -> {
                updateState { state ->
                    state.copy(filter = intent.filter)
                }
            }
            
            TodoIntent.LoadTodos -> {
                handleAsyncIntent(intent) {
                    // 模拟加载数据
                    delay(1000)
                    val sampleTodos = listOf(
                        Todo("1", "学习 Kotlin Multiplatform"),
                        Todo("2", "实现 Compose UI", true),
                        Todo("3", "编写单元测试")
                    )
                    updateState { state ->
                        state.copy(todos = sampleTodos)
                    }
                }
            }
        }
    }
    
    private fun generateId(): String = System.currentTimeMillis().toString()
}

/**
 * 7. 待办事项组件
 */
@Composable
fun TodoItem(
    todo: Todo,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    UnifyCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Checkbox(
                    checked = todo.isCompleted,
                    onCheckedChange = { onToggle() }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = todo.text,
                    style = if (todo.isCompleted) {
                        MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        MaterialTheme.typography.bodyMedium
                    }
                )
            }
            
            TextButton(onClick = onDelete) {
                Text("删除", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

/**
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
