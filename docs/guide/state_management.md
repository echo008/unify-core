# 状态管理

本文档介绍 Unify KMP 框架中的状态管理模式和最佳实践。

## 🎯 状态管理概述

Unify KMP 采用现代化的状态管理方案，结合 Compose 的响应式特性和 MVI 架构模式，提供可预测、可测试的状态管理解决方案。

## 🏗️ MVI 架构模式

### Model-View-Intent 架构
```
┌─────────────┐    Intent    ┌─────────────┐
│    View     │─────────────▶│   Intent    │
│             │              │             │
└─────────────┘              └─────────────┘
       ▲                            │
       │                            ▼
       │                     ┌─────────────┐
       │        State        │    Model    │
       └─────────────────────│             │
                             └─────────────┘
```

### 核心组件

#### 1. State (状态)
```kotlin
@Immutable
data class AppState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null,
    val counter: Int = 0
)
```

#### 2. Intent (意图)
```kotlin
sealed interface AppIntent {
    object LoadUser : AppIntent
    object Increment : AppIntent
    object Decrement : AppIntent
    data class UpdateUser(val user: User) : AppIntent
}
```

#### 3. ViewModel (视图模型)
```kotlin
class AppViewModel : ViewModel() {
    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()
    
    fun handleIntent(intent: AppIntent) {
        when (intent) {
            is AppIntent.LoadUser -> loadUser()
            is AppIntent.Increment -> increment()
            is AppIntent.Decrement -> decrement()
            is AppIntent.UpdateUser -> updateUser(intent.user)
        }
    }
    
    private fun loadUser() {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val user = userRepository.getUser()
                _state.value = _state.value.copy(
                    isLoading = false,
                    user = user,
                    error = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    private fun increment() {
        _state.value = _state.value.copy(
            counter = _state.value.counter + 1
        )
    }
    
    private fun decrement() {
        _state.value = _state.value.copy(
            counter = _state.value.counter - 1
        )
    }
}
```

## 🎨 Compose 状态管理

### 本地状态管理

#### remember
用于简单的本地状态：
```kotlin
@Composable
fun Counter() {
    var count by remember { mutableIntStateOf(0) }
    
    Column {
        Text("Count: $count")
        Button(onClick = { count++ }) {
            Text("Increment")
        }
    }
}
```

#### rememberSaveable
用于需要保存的状态：
```kotlin
@Composable
fun UserInput() {
    var text by rememberSaveable { mutableStateOf("") }
    
    TextField(
        value = text,
        onValueChange = { text = it },
        label = { Text("Enter text") }
    )
}
```

### 状态提升

#### 单向数据流
```kotlin
@Composable
fun ParentComponent() {
    var sharedState by remember { mutableStateOf("") }
    
    Column {
        ChildInput(
            value = sharedState,
            onValueChange = { sharedState = it }
        )
        ChildDisplay(value = sharedState)
    }
}

@Composable
fun ChildInput(
    value: String,
    onValueChange: (String) -> Unit
) {
    TextField(
        value = value,
        onValueChange = onValueChange
    )
}

@Composable
fun ChildDisplay(value: String) {
    Text("Current value: $value")
}
```

## 🔄 响应式状态管理

### StateFlow 集成
```kotlin
@Composable
fun UserProfile(viewModel: UserViewModel) {
    val state by viewModel.state.collectAsState()
    
    when {
        state.isLoading -> {
            CircularProgressIndicator()
        }
        state.error != null -> {
            ErrorMessage(state.error)
        }
        state.user != null -> {
            UserCard(
                user = state.user,
                onEdit = { viewModel.handleIntent(UserIntent.Edit) }
            )
        }
    }
}
```

### 副作用处理

#### LaunchedEffect
```kotlin
@Composable
fun DataLoader(userId: String, viewModel: UserViewModel) {
    LaunchedEffect(userId) {
        viewModel.handleIntent(UserIntent.LoadUser(userId))
    }
    
    // UI 内容
}
```

#### DisposableEffect
```kotlin
@Composable
fun LocationTracker() {
    DisposableEffect(Unit) {
        val locationListener = LocationListener { location ->
            // 处理位置更新
        }
        
        locationManager.requestLocationUpdates(locationListener)
        
        onDispose {
            locationManager.removeLocationUpdates(locationListener)
        }
    }
}
```

## 🏪 全局状态管理

### 应用级状态
```kotlin
// 全局状态定义
@Immutable
data class GlobalAppState(
    val theme: AppTheme = AppTheme.Light,
    val language: Language = Language.Chinese,
    val user: User? = null,
    val networkStatus: NetworkStatus = NetworkStatus.Connected
)

// 全局状态管理器
class GlobalStateManager {
    private val _state = MutableStateFlow(GlobalAppState())
    val state: StateFlow<GlobalAppState> = _state.asStateFlow()
    
    fun updateTheme(theme: AppTheme) {
        _state.value = _state.value.copy(theme = theme)
    }
    
    fun updateLanguage(language: Language) {
        _state.value = _state.value.copy(language = language)
    }
    
    fun updateUser(user: User?) {
        _state.value = _state.value.copy(user = user)
    }
}
```

### 依赖注入集成
```kotlin
// Koin 模块定义
val stateModule = module {
    single { GlobalStateManager() }
    factory { UserViewModel(get()) }
    factory { SettingsViewModel(get()) }
}

// 在 Compose 中使用
@Composable
fun App() {
    val globalStateManager: GlobalStateManager = koinInject()
    val globalState by globalStateManager.state.collectAsState()
    
    AppTheme(theme = globalState.theme) {
        // 应用内容
    }
}
```

## 🧪 状态管理测试

### ViewModel 测试
```kotlin
class UserViewModelTest {
    private lateinit var viewModel: UserViewModel
    private lateinit var mockRepository: UserRepository
    
    @BeforeEach
    fun setup() {
        mockRepository = mockk()
        viewModel = UserViewModel(mockRepository)
    }
    
    @Test
    fun `should load user successfully`() = runTest {
        // Given
        val expectedUser = User("1", "John", "john@example.com")
        coEvery { mockRepository.getUser() } returns expectedUser
        
        // When
        viewModel.handleIntent(UserIntent.LoadUser)
        
        // Then
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(expectedUser, state.user)
        assertNull(state.error)
    }
    
    @Test
    fun `should handle loading error`() = runTest {
        // Given
        val errorMessage = "Network error"
        coEvery { mockRepository.getUser() } throws Exception(errorMessage)
        
        // When
        viewModel.handleIntent(UserIntent.LoadUser)
        
        // Then
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.user)
        assertEquals(errorMessage, state.error)
    }
}
```

### Compose 状态测试
```kotlin
@Test
fun testCounterIncrement() {
    composeTestRule.setContent {
        Counter()
    }
    
    // 验证初始状态
    composeTestRule.onNodeWithText("Count: 0").assertExists()
    
    // 执行操作
    composeTestRule.onNodeWithText("Increment").performClick()
    
    // 验证状态变化
    composeTestRule.onNodeWithText("Count: 1").assertExists()
}
```

## 🔧 状态持久化

### 本地存储
```kotlin
class StateRepository {
    private val preferences = getSharedPreferences()
    
    suspend fun saveAppState(state: AppState) {
        preferences.edit {
            putString("user", Json.encodeToString(state.user))
            putInt("counter", state.counter)
            putString("theme", state.theme.name)
        }
    }
    
    suspend fun loadAppState(): AppState? {
        return try {
            val userJson = preferences.getString("user", null)
            val counter = preferences.getInt("counter", 0)
            val themeName = preferences.getString("theme", "Light")
            
            AppState(
                user = userJson?.let { Json.decodeFromString(it) },
                counter = counter,
                theme = AppTheme.valueOf(themeName)
            )
        } catch (e: Exception) {
            null
        }
    }
}
```

### 状态恢复
```kotlin
class AppViewModel(
    private val stateRepository: StateRepository
) : ViewModel() {
    
    init {
        viewModelScope.launch {
            val savedState = stateRepository.loadAppState()
            if (savedState != null) {
                _state.value = savedState
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            stateRepository.saveAppState(_state.value)
        }
    }
}
```

## 🚀 性能优化

### 状态分割
```kotlin
// 避免单一大状态
data class AppState(
    val ui: UIState,
    val data: DataState,
    val network: NetworkState
)

// 使用独立的状态管理器
class UIStateManager {
    private val _uiState = MutableStateFlow(UIState())
    val uiState: StateFlow<UIState> = _uiState.asStateFlow()
}

class DataStateManager {
    private val _dataState = MutableStateFlow(DataState())
    val dataState: StateFlow<DataState> = _dataState.asStateFlow()
}
```

### 记忆化优化
```kotlin
@Composable
fun ExpensiveComponent(
    data: List<Item>,
    onItemClick: (Item) -> Unit
) {
    // 记忆化计算结果
    val processedData = remember(data) {
        data.map { processItem(it) }
    }
    
    // 记忆化回调函数
    val memoizedCallback = remember {
        { item: Item -> onItemClick(item) }
    }
    
    LazyColumn {
        items(processedData) { item ->
            ItemCard(
                item = item,
                onClick = memoizedCallback
            )
        }
    }
}
```

## 📋 最佳实践

### 1. 状态设计原则
- **不可变性**: 使用不可变数据结构
- **单一数据源**: 每个状态只有一个权威来源
- **最小化状态**: 只保存必要的状态信息
- **状态规范化**: 避免重复和冗余数据

### 2. 状态更新规则
- **纯函数**: 状态更新应该是纯函数
- **原子操作**: 状态更新应该是原子的
- **可预测性**: 相同输入产生相同输出
- **可追踪性**: 状态变化应该可以追踪

### 3. 错误处理
```kotlin
sealed class AsyncState<out T> {
    object Loading : AsyncState<Nothing>()
    data class Success<T>(val data: T) : AsyncState<T>()
    data class Error(val exception: Throwable) : AsyncState<Nothing>()
}

@Composable
fun <T> AsyncStateHandler(
    state: AsyncState<T>,
    onRetry: () -> Unit = {},
    content: @Composable (T) -> Unit
) {
    when (state) {
        is AsyncState.Loading -> {
            CircularProgressIndicator()
        }
        is AsyncState.Success -> {
            content(state.data)
        }
        is AsyncState.Error -> {
            ErrorCard(
                error = state.exception,
                onRetry = onRetry
            )
        }
    }
}
```

### 4. 状态调试
```kotlin
// 开发环境下的状态日志
class DebuggableStateManager<T>(
    private val delegate: StateManager<T>,
    private val logger: Logger
) : StateManager<T> by delegate {
    
    override fun updateState(newState: T) {
        logger.d("State Update", "Old: ${delegate.currentState}")
        logger.d("State Update", "New: $newState")
        delegate.updateState(newState)
    }
}
```

---

通过合理的状态管理，您可以构建响应式、可维护的跨平台应用。记住始终保持状态的简单性和可预测性。
