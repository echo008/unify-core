# Todo 应用示例

## 📱 项目概述

Todo 应用是一个功能完整的任务管理应用，展示了 Unify KMP 框架的高级功能，包括数据持久化、搜索过滤、状态管理和复杂UI交互。

## 🎯 功能特性

- ✅ **任务管理** - 添加、编辑、删除、完成任务
- ✅ **优先级设置** - 高、中、低优先级分类
- ✅ **搜索过滤** - 实时搜索和状态过滤
- ✅ **数据持久化** - SQLDelight 数据库存储
- ✅ **响应式UI** - Material Design 3 界面
- ✅ **跨平台支持** - 所有平台功能一致

## 🏗️ 项目结构

```
todo-app/
├── shared/
│   └── src/
│       └── commonMain/
│           ├── kotlin/
│           │   ├── TodoApp.kt
│           │   ├── TodoViewModel.kt
│           │   ├── TodoRepository.kt
│           │   └── models/
│           │       └── TodoItem.kt
│           └── sqldelight/
│               └── database/
│                   └── TodoDatabase.sq
├── androidApp/
├── iosApp/
├── webApp/
└── desktopApp/
```

## 💻 核心实现

### TodoItem.kt - 数据模型

```kotlin
package com.unify.todo.models

import kotlinx.serialization.Serializable

@Serializable
data class TodoItem(
    val id: String,
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val priority: Priority = Priority.MEDIUM,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val dueDate: Long? = null
)

enum class Priority(val displayName: String, val level: Int) {
    LOW("低", 1),
    MEDIUM("中", 2),
    HIGH("高", 3)
}

enum class TodoFilter {
    ALL, ACTIVE, COMPLETED
}
```

### TodoApp.kt - 主应用组件

```kotlin
package com.unify.todo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoApp() {
    val viewModel = remember { TodoViewModel() }
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    
    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("待办事项") },
                    actions = {
                        IconButton(onClick = { /* 搜索 */ }) {
                            Icon(Icons.Default.Search, contentDescription = "搜索")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddDialog = true }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "添加任务")
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // 搜索栏
                SearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::updateSearchQuery,
                    modifier = Modifier.padding(16.dp)
                )
                
                // 过滤器
                FilterTabs(
                    currentFilter = uiState.filter,
                    onFilterChange = viewModel::updateFilter,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                // 任务列表
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = uiState.filteredTodos,
                            key = { it.id }
                        ) { todo ->
                            TodoItemCard(
                                todo = todo,
                                onToggleComplete = { viewModel.toggleTodoComplete(todo.id) },
                                onEdit = { viewModel.editTodo(todo) },
                                onDelete = { viewModel.deleteTodo(todo.id) }
                            )
                        }
                    }
                }
            }
        }
        
        // 添加任务对话框
        if (showAddDialog) {
            AddTodoDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { title, description, priority ->
                    viewModel.addTodo(title, description, priority)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun TodoItemCard(
    todo: TodoItem,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = todo.isCompleted,
                onCheckedChange = { onToggleComplete() }
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = todo.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (todo.isCompleted) 
                        TextDecoration.LineThrough else null
                )
                
                if (todo.description.isNotEmpty()) {
                    Text(
                        text = todo.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                PriorityChip(priority = todo.priority)
            }
            
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "编辑")
            }
            
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "删除")
            }
        }
    }
}
```

### TodoViewModel.kt - 状态管理

```kotlin
package com.unify.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class TodoUiState(
    val todos: List<TodoItem> = emptyList(),
    val searchQuery: String = "",
    val filter: TodoFilter = TodoFilter.ALL,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val filteredTodos: List<TodoItem>
        get() = todos
            .filter { todo ->
                // 搜索过滤
                if (searchQuery.isNotEmpty()) {
                    todo.title.contains(searchQuery, ignoreCase = true) ||
                    todo.description.contains(searchQuery, ignoreCase = true)
                } else true
            }
            .filter { todo ->
                // 状态过滤
                when (filter) {
                    TodoFilter.ALL -> true
                    TodoFilter.ACTIVE -> !todo.isCompleted
                    TodoFilter.COMPLETED -> todo.isCompleted
                }
            }
            .sortedWith(
                compareByDescending<TodoItem> { it.priority.level }
                    .thenByDescending { it.createdAt }
            )
}

class TodoViewModel : ViewModel() {
    private val repository = TodoRepository()
    
    private val _uiState = MutableStateFlow(TodoUiState())
    val uiState: StateFlow<TodoUiState> = _uiState.asStateFlow()
    
    init {
        loadTodos()
    }
    
    fun addTodo(title: String, description: String, priority: Priority) {
        viewModelScope.launch {
            try {
                repository.addTodo(title, description, priority)
                loadTodos()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun toggleTodoComplete(id: String) {
        viewModelScope.launch {
            try {
                repository.toggleTodoComplete(id)
                loadTodos()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun deleteTodo(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteTodo(id)
                loadTodos()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }
    
    fun updateFilter(filter: TodoFilter) {
        _uiState.value = _uiState.value.copy(filter = filter)
    }
    
    private fun loadTodos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val todos = repository.getAllTodos()
                _uiState.value = _uiState.value.copy(
                    todos = todos,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
}
```

### TodoRepository.kt - 数据持久化

```kotlin
package com.unify.todo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class TodoRepository {
    private val database = TodoDatabase.getInstance()
    
    suspend fun getAllTodos(): List<TodoItem> = withContext(Dispatchers.IO) {
        database.todoQueries.selectAll().executeAsList().map { dbTodo ->
            TodoItem(
                id = dbTodo.id,
                title = dbTodo.title,
                description = dbTodo.description,
                isCompleted = dbTodo.isCompleted,
                priority = Priority.valueOf(dbTodo.priority),
                createdAt = dbTodo.createdAt,
                updatedAt = dbTodo.updatedAt,
                dueDate = dbTodo.dueDate
            )
        }
    }
    
    suspend fun addTodo(
        title: String,
        description: String,
        priority: Priority
    ) = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        
        database.todoQueries.insert(
            id = id,
            title = title,
            description = description,
            isCompleted = false,
            priority = priority.name,
            createdAt = now,
            updatedAt = now,
            dueDate = null
        )
    }
    
    suspend fun toggleTodoComplete(id: String) = withContext(Dispatchers.IO) {
        val todo = database.todoQueries.selectById(id).executeAsOneOrNull()
        if (todo != null) {
            database.todoQueries.updateCompleted(
                isCompleted = !todo.isCompleted,
                updatedAt = System.currentTimeMillis(),
                id = id
            )
        }
    }
    
    suspend fun deleteTodo(id: String) = withContext(Dispatchers.IO) {
        database.todoQueries.deleteById(id)
    }
}
```

## 📊 数据库设计

### TodoDatabase.sq

```sql
CREATE TABLE TodoItem (
    id TEXT NOT NULL PRIMARY KEY,
    title TEXT NOT NULL,
    description TEXT NOT NULL DEFAULT '',
    isCompleted INTEGER NOT NULL DEFAULT 0,
    priority TEXT NOT NULL DEFAULT 'MEDIUM',
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL,
    dueDate INTEGER
);

selectAll:
SELECT * FROM TodoItem ORDER BY priority DESC, createdAt DESC;

selectById:
SELECT * FROM TodoItem WHERE id = ?;

selectByCompleted:
SELECT * FROM TodoItem WHERE isCompleted = ? ORDER BY priority DESC, createdAt DESC;

insert:
INSERT INTO TodoItem(id, title, description, isCompleted, priority, createdAt, updatedAt, dueDate)
VALUES (?, ?, ?, ?, ?, ?, ?, ?);

updateCompleted:
UPDATE TodoItem SET isCompleted = ?, updatedAt = ? WHERE id = ?;

updateTodo:
UPDATE TodoItem SET title = ?, description = ?, priority = ?, updatedAt = ? WHERE id = ?;

deleteById:
DELETE FROM TodoItem WHERE id = ?;

deleteCompleted:
DELETE FROM TodoItem WHERE isCompleted = 1;
```

## 🧪 测试实现

### 单元测试

```kotlin
class TodoViewModelTest {
    private lateinit var viewModel: TodoViewModel
    
    @BeforeTest
    fun setup() {
        viewModel = TodoViewModel()
    }
    
    @Test
    fun `添加任务应该更新状态`() = runTest {
        viewModel.addTodo("测试任务", "描述", Priority.HIGH)
        
        val state = viewModel.uiState.value
        assertEquals(1, state.todos.size)
        assertEquals("测试任务", state.todos.first().title)
    }
    
    @Test
    fun `搜索应该正确过滤任务`() = runTest {
        viewModel.addTodo("工作任务", "完成报告", Priority.HIGH)
        viewModel.addTodo("个人任务", "买菜", Priority.LOW)
        
        viewModel.updateSearchQuery("工作")
        
        val filteredTodos = viewModel.uiState.value.filteredTodos
        assertEquals(1, filteredTodos.size)
        assertEquals("工作任务", filteredTodos.first().title)
    }
}
```

## 🚀 构建和运行

### Gradle 配置

```kotlin
// shared/build.gradle.kts
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.sqldelight.runtime)
            implementation(libs.kotlinx.serialization.json)
        }
        
        androidMain.dependencies {
            implementation(libs.sqldelight.android.driver)
        }
        
        iosMain.dependencies {
            implementation(libs.sqldelight.native.driver)
        }
        
        jvmMain.dependencies {
            implementation(libs.sqldelight.sqlite.driver)
        }
    }
}

sqldelight {
    databases {
        create("TodoDatabase") {
            packageName.set("com.unify.todo.database")
        }
    }
}
```

---

这个 Todo 应用示例展示了 Unify KMP 框架的高级功能，包括数据库集成、复杂状态管理和丰富的用户交互。
