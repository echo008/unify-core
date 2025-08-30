# 示例项目

## 🎯 示例概览

本节提供了丰富的示例项目，帮助您快速理解和掌握 Unify KMP 框架的使用方法。从简单的 Hello World 到复杂的实际应用，这些示例涵盖了跨平台开发的各个方面。

## 🚀 快速开始示例

### [Hello World 示例](./hello-world.md)
最基础的跨平台应用示例，展示：
- 基本项目结构
- 跨平台 UI 组件
- 平台信息获取
- 状态管理基础

## 📱 基础应用示例

### 计数器应用
一个简单的计数器应用，演示状态管理和用户交互。

```kotlin
@Composable
fun CounterApp() {
    var count by remember { mutableIntStateOf(0) }
    
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "计数器",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
                
                Card(
                    modifier = Modifier.padding(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = count.toString(),
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = { count-- },
                                enabled = count > 0
                            ) {
                                Text("-")
                            }
                            
                            Button(onClick = { count++ }) {
                                Text("+")
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = { count = 0 },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text("重置")
                        }
                    }
                }
            }
        }
    }
}
```

### Todo 应用
功能完整的待办事项应用，包含：
- 任务添加、编辑、删除
- 任务状态管理
- 本地数据持久化
- 搜索和过滤功能

```kotlin
data class TodoItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val priority: Priority = Priority.MEDIUM
)

enum class Priority { LOW, MEDIUM, HIGH }

class TodoViewModel : ViewModel() {
    private val _todos = MutableStateFlow<List<TodoItem>>(emptyList())
    val todos: StateFlow<List<TodoItem>> = _todos.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    val filteredTodos = combine(todos, searchQuery) { todoList, query ->
        if (query.isBlank()) {
            todoList
        } else {
            todoList.filter { 
                it.title.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    fun addTodo(title: String, description: String, priority: Priority) {
        val newTodo = TodoItem(
            title = title,
            description = description,
            priority = priority
        )
        _todos.value = _todos.value + newTodo
    }
    
    fun toggleTodo(id: String) {
        _todos.value = _todos.value.map { todo ->
            if (todo.id == id) {
                todo.copy(isCompleted = !todo.isCompleted)
            } else {
                todo
            }
        }
    }
    
    fun deleteTodo(id: String) {
        _todos.value = _todos.value.filter { it.id != id }
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
}

@Composable
fun TodoApp() {
    val viewModel = remember { TodoViewModel() }
    val todos by viewModel.filteredTodos.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // 搜索栏
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = viewModel::updateSearchQuery,
                    label = { Text("搜索任务") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
                
                // 添加按钮
                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("添加任务")
                }
                
                // 任务列表
                LazyColumn {
                    items(todos) { todo ->
                        TodoItemCard(
                            todo = todo,
                            onToggle = { viewModel.toggleTodo(todo.id) },
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
```

### 天气应用
展示网络请求和数据展示的完整应用：
- REST API 集成
- JSON 数据解析
- 错误处理
- 加载状态管理
- 位置服务集成

```kotlin
data class WeatherData(
    val location: String,
    val temperature: Double,
    val description: String,
    val humidity: Int,
    val windSpeed: Double,
    val icon: String
)

class WeatherRepository {
    private val httpClient = HttpClient()
    
    suspend fun getCurrentWeather(city: String): Result<WeatherData> {
        return try {
            val response = httpClient.get(
                "https://api.openweathermap.org/data/2.5/weather?q=$city&appid=YOUR_API_KEY&units=metric"
            )
            
            if (response.status == 200) {
                val weatherData = Json.decodeFromString<WeatherResponse>(response.body)
                Result.success(
                    WeatherData(
                        location = weatherData.name,
                        temperature = weatherData.main.temp,
                        description = weatherData.weather[0].description,
                        humidity = weatherData.main.humidity,
                        windSpeed = weatherData.wind.speed,
                        icon = weatherData.weather[0].icon
                    )
                )
            } else {
                Result.failure(Exception("HTTP ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

@Composable
fun WeatherApp() {
    var weatherData by remember { mutableStateOf<WeatherData?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var city by remember { mutableStateOf("北京") }
    
    val repository = remember { WeatherRepository() }
    
    LaunchedEffect(Unit) {
        loadWeather(city, repository) { data, loading, errorMsg ->
            weatherData = data
            isLoading = loading
            error = errorMsg
        }
    }
    
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // 城市输入
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("城市名称") },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                loadWeather(city, repository) { data, loading, errorMsg ->
                                    weatherData = data
                                    isLoading = loading
                                    error = errorMsg
                                }
                            }
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "搜索")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
                
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    
                    error != null -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = "错误: $error",
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                    
                    weatherData != null -> {
                        WeatherCard(weatherData!!)
                    }
                }
            }
        }
    }
}
```

## 🏗️ 架构示例

### MVI 架构示例
展示如何使用 MVI（Model-View-Intent）架构模式：

```kotlin
// Intent - 用户意图
sealed class UserIntent {
    object LoadData : UserIntent()
    data class Search(val query: String) : UserIntent()
    data class SelectItem(val id: String) : UserIntent()
}

// State - UI 状态
data class UiState(
    val isLoading: Boolean = false,
    val items: List<Item> = emptyList(),
    val selectedItem: Item? = null,
    val error: String? = null,
    val searchQuery: String = ""
)

// ViewModel - 状态管理
class MviViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    fun handleIntent(intent: UserIntent) {
        when (intent) {
            is UserIntent.LoadData -> loadData()
            is UserIntent.Search -> search(intent.query)
            is UserIntent.SelectItem -> selectItem(intent.id)
        }
    }
    
    private fun loadData() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            try {
                val items = repository.loadItems()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    items = items
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

## 🔧 工具和实用程序示例

### 缓存管理示例
```kotlin
class CacheExample {
    private val cache = CacheManager()
    
    suspend fun getCachedData(key: String): String? {
        return cache.get(key) as? String
    }
    
    suspend fun setCachedData(key: String, data: String, ttl: Long = 300000L) {
        cache.put(key, data, ttl)
    }
}
```

### 网络状态监听示例
```kotlin
@Composable
fun NetworkStatusExample() {
    var isOnline by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        // 监听网络状态变化
        NetworkManager().observeNetworkStatus { status ->
            isOnline = status
        }
    }
    
    if (!isOnline) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Text(
                text = "网络连接已断开",
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}
```

## 📱 平台特定示例

### Android 特定功能
```kotlin
// Android 权限请求
@Composable
fun AndroidPermissionExample() {
    var hasPermission by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        if (Platform.isAndroid) {
            hasPermission = checkPermission("android.permission.CAMERA")
            if (!hasPermission) {
                requestPermission("android.permission.CAMERA") { granted ->
                    hasPermission = granted
                }
            }
        }
    }
}
```

### iOS 特定功能
```kotlin
// iOS 原生视图集成
@Composable
fun IOSNativeViewExample() {
    if (Platform.isIOS) {
        UIKitView(
            factory = { 
                // 创建原生 iOS 视图
                createNativeIOSView()
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
```

## 🎨 主题和样式示例

### 自定义主题
```kotlin
@Composable
fun CustomThemeExample() {
    val customColorScheme = lightColorScheme(
        primary = Color(0xFF6200EE),
        secondary = Color(0xFF03DAC6),
        background = Color(0xFFF5F5F5)
    )
    
    MaterialTheme(
        colorScheme = customColorScheme,
        typography = Typography(
            headlineLarge = TextStyle(
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        )
    ) {
        // 您的 UI 内容
    }
}
```

## 🧪 测试示例

### 单元测试
```kotlin
class ViewModelTest {
    @Test
    fun `test loading state`() = runTest {
        val viewModel = MyViewModel()
        
        viewModel.handleIntent(UserIntent.LoadData)
        
        assertEquals(true, viewModel.uiState.value.isLoading)
    }
}
```

### UI 测试
```kotlin
@Test
fun `test button click`() {
    composeTestRule.setContent {
        CounterApp()
    }
    
    composeTestRule.onNodeWithText("+").performClick()
    composeTestRule.onNodeWithText("1").assertExists()
}
```

## 📚 学习路径

1. **从 Hello World 开始** - 理解基本概念
2. **构建计数器应用** - 学习状态管理
3. **开发 Todo 应用** - 掌握数据持久化
4. **创建天气应用** - 学习网络编程
5. **探索高级架构** - 应用 MVI 模式

## 🔗 相关资源

- [Hello World 详细教程](./hello-world.md)
- [API 参考文档](../api/)
- [平台开发指南](../platforms/)
- [最佳实践指南](../guide/advanced.md)

---

通过这些示例，您可以快速上手 Unify KMP 开发，并逐步掌握跨平台应用开发的各种技巧和最佳实践。
