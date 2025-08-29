# Unify KMP API 参考文档

## 核心 API

### UnifyViewModel

基于 MVI 模式的 ViewModel 基类，提供状态管理和副作用处理。

```kotlin
abstract class UnifyViewModel<S : State, I : Intent, E : Effect> {
    val stateFlow: StateFlow<S>
    val effectFlow: SharedFlow<E>
    
    fun sendIntent(intent: I)
    protected fun sendEffect(effect: E)
    
    abstract fun createInitialState(): S
    abstract fun createReducer(): StateReducer<S, I>
    abstract fun createMiddleware(): List<StateMiddleware<S, I>>
}
```

**使用示例:**
```kotlin
class UserListViewModel : UnifyViewModel<UserListState, UserListIntent, UserListEffect>() {
    override fun createInitialState() = UserListState()
    
    override fun createReducer() = UserListReducer()
    
    override fun createMiddleware() = listOf(
        LoggingMiddleware(),
        NetworkMiddleware(networkService)
    )
}

// 在 UI 中使用
@Composable
fun UserListScreen(viewModel: UserListViewModel) {
    val state by viewModel.stateFlow.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.sendIntent(UserListIntent.LoadUsers)
    }
    
    when (state) {
        is UserListState.Loading -> LoadingIndicator()
        is UserListState.Success -> UserList(state.users)
        is UserListState.Error -> ErrorMessage(state.message)
    }
}
```

### UnifyNetworkService

统一的网络服务接口，支持 RESTful API 调用。

```kotlin
interface UnifyNetworkService {
    suspend fun <T> get(
        url: String,
        headers: Map<String, String> = emptyMap(),
        queryParams: Map<String, String> = emptyMap()
    ): NetworkResult<T>
    
    suspend fun <T> post(
        url: String,
        body: Any? = null,
        headers: Map<String, String> = emptyMap()
    ): NetworkResult<T>
    
    suspend fun <T> put(url: String, body: Any?, headers: Map<String, String>): NetworkResult<T>
    suspend fun <T> delete(url: String, headers: Map<String, String>): NetworkResult<T>
    
    fun <T> getStream(url: String, headers: Map<String, String>): Flow<NetworkResult<T>>
    
    suspend fun uploadFile(
        url: String,
        filePath: String,
        headers: Map<String, String> = emptyMap(),
        onProgress: ((Float) -> Unit)? = null
    ): NetworkResult<String>
}
```

**使用示例:**
```kotlin
class UserRepository(private val networkService: UnifyNetworkService) {
    suspend fun getUsers(): NetworkResult<List<User>> {
        return networkService.get("/api/users")
    }
    
    suspend fun createUser(user: CreateUserRequest): NetworkResult<User> {
        return networkService.post("/api/users", user)
    }
    
    suspend fun updateUser(id: String, user: UpdateUserRequest): NetworkResult<User> {
        return networkService.put("/api/users/$id", user)
    }
}
```

### UnifyStorage

统一的存储接口，支持键值对存储和对象序列化。

```kotlin
interface UnifyStorage {
    suspend fun putString(key: String, value: String)
    suspend fun getString(key: String): String?
    
    suspend fun <T> putObject(key: String, value: T, serializer: KSerializer<T>)
    suspend fun <T> getObject(key: String, serializer: KSerializer<T>): T?
    
    suspend fun remove(key: String)
    suspend fun clear()
    
    fun <T> observeObject(key: String, serializer: KSerializer<T>): Flow<T?>
}
```

**使用示例:**
```kotlin
class UserPreferences(private val storage: UnifyStorage) {
    suspend fun saveUser(user: User) {
        storage.putObject("current_user", user, User.serializer())
    }
    
    suspend fun getUser(): User? {
        return storage.getObject("current_user", User.serializer())
    }
    
    fun observeUser(): Flow<User?> {
        return storage.observeObject("current_user", User.serializer())
    }
}
```

### UnifyDatabaseRepository

数据库访问层，基于 SQLDelight 实现。

```kotlin
class UnifyDatabaseRepository(private val database: UnifyDatabase) {
    // 用户操作
    suspend fun getAllUsers(): List<User>
    suspend fun getUserById(id: Long): User?
    suspend fun createUser(username: String, email: String, displayName: String): Long
    suspend fun updateUser(id: Long, email: String, displayName: String)
    suspend fun deleteUser(id: Long)
    
    // 配置操作
    suspend fun getConfigByKey(key: String): AppConfig?
    suspend fun setConfig(key: String, value: String, type: String = "STRING")
    
    // 缓存操作
    suspend fun getCacheByKey(key: String): CacheData?
    suspend fun setCache(key: String, value: String, expiresAt: Long? = null)
    suspend fun clearAllCache()
    
    // 性能监控
    suspend fun insertPerformanceMetric(componentId: String, metricType: String, value: Double, platform: String)
    suspend fun getPerformanceMetrics(componentId: String, since: Long): List<PerformanceMetrics>
}
```

**使用示例:**
```kotlin
class UserService(private val repository: UnifyDatabaseRepository) {
    suspend fun loadUsers(): List<User> {
        return repository.getAllUsers()
    }
    
    suspend fun createUser(request: CreateUserRequest): User {
        val userId = repository.createUser(
            username = request.username,
            email = request.email,
            displayName = request.displayName
        )
        return repository.getUserById(userId)!!
    }
}
```

## UI 组件 API

### UnifyTheme

统一主题系统，提供跨平台的设计令牌。

```kotlin
@Composable
fun UnifyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        UnifyThemeAccessor.darkColorScheme
    } else {
        UnifyThemeAccessor.lightColorScheme
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = UnifyThemeAccessor.typography,
        shapes = UnifyThemeAccessor.shapes,
        content = content
    )
}
```

### UnifyButton

跨平台按钮组件。

```kotlin
@Composable
fun UnifyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: UnifyButtonStyle = UnifyButtonStyle.Primary
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = when (style) {
            UnifyButtonStyle.Primary -> ButtonDefaults.buttonColors()
            UnifyButtonStyle.Secondary -> ButtonDefaults.outlinedButtonColors()
            UnifyButtonStyle.Text -> ButtonDefaults.textButtonColors()
        }
    ) {
        Text(text)
    }
}
```

### UnifyTextField

跨平台文本输入组件。

```kotlin
@Composable
fun UnifyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = label?.let { { Text(it) } },
            placeholder = placeholder?.let { { Text(it) } },
            isError = isError,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth()
        )
        
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}
```

## 数据流 API

### UnifyDataFlow

响应式数据流管理器。

```kotlin
class UnifyDataFlow<T>(
    private val scope: CoroutineScope,
    private val cacheConfig: CacheConfig = CacheConfig()
) {
    val data: StateFlow<DataState<T>>
    
    fun fetch(fetcher: suspend () -> T, forceRefresh: Boolean = false)
    fun refresh(fetcher: suspend () -> T)
    fun clear()
}
```

**使用示例:**
```kotlin
class UserListViewModel {
    private val userDataFlow = UnifyDataFlow<List<User>>(viewModelScope)
    val users: StateFlow<DataState<List<User>>> = userDataFlow.data
    
    fun loadUsers() {
        userDataFlow.fetch {
            userRepository.getUsers()
        }
    }
    
    fun refreshUsers() {
        userDataFlow.refresh {
            userRepository.getUsers()
        }
    }
}
```

### PaginatedDataFlow

分页数据流管理器。

```kotlin
class PaginatedDataFlow<T>(
    private val scope: CoroutineScope,
    private val pageSize: Int = 20
) {
    val data: StateFlow<PaginatedState<T>>
    
    fun loadFirstPage(fetcher: suspend (page: Int, size: Int) -> List<T>)
    fun loadNextPage(fetcher: suspend (page: Int, size: Int) -> List<T>)
    fun refresh(fetcher: suspend (page: Int, size: Int) -> List<T>)
}
```

## 平台特定 API

### Android

```kotlin
// 依赖注入
object AndroidDI {
    fun initialize(context: Context)
    val module: AndroidModule
}

// 数据库驱动
class AndroidDatabaseDriverFactory(context: Context) : DatabaseDriverFactory {
    override fun createDriver(): SqlDriver
}
```

### iOS

```swift
// ViewModel
class IOSMainViewModel: ObservableObject {
    @Published var users: [UserModel] = []
    @Published var isLoading: Bool = false
    @Published var errorMessage: String? = nil
    
    func loadUsers()
}

// 数据库驱动
class IOSDatabaseDriverFactory: DatabaseDriverFactory {
    func createDriver() -> SqlDriver
}
```

### Web

```kotlin
// 主应用组件
@Composable
fun WebApp() {
    // Web 特定的 Compose for Web 实现
}

// 依赖注入
object WebDI {
    val databaseRepository: UnifyDatabaseRepository
    val networkService: UnifyNetworkService
}
```

## 错误处理

### NetworkResult

网络请求结果封装。

```kotlin
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val message: String, val exception: Throwable? = null) : NetworkResult<Nothing>()
    
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }
    
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception ?: RuntimeException(message)
    }
}
```

### DataState

数据状态封装。

```kotlin
sealed class DataState<out T> {
    object Loading : DataState<Nothing>()
    data class Success<T>(val data: T) : DataState<T>()
    data class Error(val message: String, val exception: Throwable? = null) : DataState<Nothing>()
    
    val isLoading: Boolean get() = this is Loading
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
}
```

## 配置选项

### CacheConfig

缓存配置。

```kotlin
data class CacheConfig(
    val ttlSeconds: Long = 300, // 5分钟默认TTL
    val maxSize: Int = 100
)
```

### PerformanceConfig

性能配置。

```kotlin
data class KuiklyPerformanceConfig(
    val enableRenderCaching: Boolean = true,
    val enableLazyLoading: Boolean = true,
    val enableVirtualization: Boolean = false,
    val cacheSize: Int = 50,
    val preloadThreshold: Float = 0.8f
)
```

## 扩展函数

### StateFlow 扩展

```kotlin
fun <T> StateFlow<DataState<T>>.onSuccess(action: (T) -> Unit): StateFlow<DataState<T>>
fun <T> StateFlow<DataState<T>>.onError(action: (String, Throwable?) -> Unit): StateFlow<DataState<T>>
fun <T, R> StateFlow<DataState<T>>.mapData(mapper: (T) -> R): Flow<DataState<R>>
```

### Flow 扩展

```kotlin
fun <T> Flow<T>.throttleLatest(periodMillis: Long): Flow<T>
fun <T> Flow<T>.retryWithBackoff(maxRetries: Int, initialDelay: Long): Flow<T>
```
