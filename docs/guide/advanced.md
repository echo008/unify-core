# 高级教程和最佳实践

## 🏗️ 架构设计最佳实践

### MVI 架构模式
```kotlin
// 状态定义
data class AppState(
    val isLoading: Boolean = false,
    val count: Int = 0,
    val error: String? = null,
    val userInfo: UserInfo? = null
)

// 意图定义
sealed class AppIntent {
    object LoadData : AppIntent()
    object Increment : AppIntent()
    object Reset : AppIntent()
    data class UpdateUser(val user: UserInfo) : AppIntent()
}

// ViewModel 实现
class AppViewModel : ViewModel() {
    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()
    
    fun handleIntent(intent: AppIntent) {
        when (intent) {
            is AppIntent.LoadData -> loadData()
            is AppIntent.Increment -> increment()
            is AppIntent.Reset -> reset()
            is AppIntent.UpdateUser -> updateUser(intent.user)
        }
    }
    
    private fun loadData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val data = repository.loadData()
                _state.value = _state.value.copy(
                    isLoading = false,
                    userInfo = data
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
}
```

### 依赖注入设计
```kotlin
// 模块定义
val networkModule = module {
    single<HttpClient> {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            install(Logging) {
                level = LogLevel.INFO
            }
        }
    }
    
    single<ApiService> { ApiServiceImpl(get()) }
}

val repositoryModule = module {
    single<UserRepository> { UserRepositoryImpl(get(), get()) }
    single<DataRepository> { DataRepositoryImpl(get()) }
}

val viewModelModule = module {
    factory { AppViewModel(get()) }
    factory { UserViewModel(get()) }
}

// 应用初始化
fun initKoin() {
    startKoin {
        modules(networkModule, repositoryModule, viewModelModule)
    }
}
```

## 🌐 网络编程进阶

### 网络请求封装
```kotlin
class NetworkClient {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        
        install(HttpTimeout) {
            requestTimeoutMillis = 30000
            connectTimeoutMillis = 10000
            socketTimeoutMillis = 30000
        }
        
        install(DefaultRequest) {
            header("Content-Type", "application/json")
            header("User-Agent", "Unify-KMP/1.0")
        }
    }
    
    suspend inline fun <reified T> get(
        url: String,
        parameters: Map<String, String> = emptyMap()
    ): Result<T> = try {
        val response = client.get(url) {
            parameters.forEach { (key, value) ->
                parameter(key, value)
            }
        }
        Result.success(response.body<T>())
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    suspend inline fun <reified T, reified R> post(
        url: String,
        body: T
    ): Result<R> = try {
        val response = client.post(url) {
            setBody(body)
        }
        Result.success(response.body<R>())
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

### 错误处理策略
```kotlin
sealed class NetworkError : Exception() {
    object NetworkUnavailable : NetworkError()
    object Timeout : NetworkError()
    data class ServerError(val code: Int, val message: String) : NetworkError()
    data class ClientError(val code: Int, val message: String) : NetworkError()
    data class UnknownError(val throwable: Throwable) : NetworkError()
}

class ErrorHandler {
    fun handleError(throwable: Throwable): NetworkError {
        return when (throwable) {
            is ConnectTimeoutException -> NetworkError.Timeout
            is SocketTimeoutException -> NetworkError.Timeout
            is UnknownHostException -> NetworkError.NetworkUnavailable
            is ClientRequestException -> {
                when (throwable.response.status.value) {
                    in 400..499 -> NetworkError.ClientError(
                        throwable.response.status.value,
                        throwable.message ?: "Client error"
                    )
                    else -> NetworkError.UnknownError(throwable)
                }
            }
            is ServerResponseException -> NetworkError.ServerError(
                throwable.response.status.value,
                throwable.message ?: "Server error"
            )
            else -> NetworkError.UnknownError(throwable)
        }
    }
}
```

## 💾 数据持久化进阶

### SQLDelight 高级用法
```sql
-- database/User.sq
CREATE TABLE User (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    avatar_url TEXT,
    created_at INTEGER NOT NULL DEFAULT (strftime('%s', 'now')),
    updated_at INTEGER NOT NULL DEFAULT (strftime('%s', 'now'))
);

CREATE INDEX user_email_idx ON User(email);
CREATE INDEX user_created_at_idx ON User(created_at);

-- 查询操作
selectAll:
SELECT * FROM User ORDER BY created_at DESC;

selectById:
SELECT * FROM User WHERE id = ?;

selectByEmail:
SELECT * FROM User WHERE email = ?;

searchByName:
SELECT * FROM User WHERE name LIKE '%' || ? || '%';

-- 插入操作
insert:
INSERT INTO User(name, email, avatar_url)
VALUES (?, ?, ?);

-- 更新操作
updateUser:
UPDATE User SET 
    name = ?,
    email = ?,
    avatar_url = ?,
    updated_at = strftime('%s', 'now')
WHERE id = ?;

-- 删除操作
deleteById:
DELETE FROM User WHERE id = ?;

-- 统计操作
countUsers:
SELECT COUNT(*) FROM User;
```

```kotlin
class DatabaseManager(private val database: AppDatabase) {
    private val userQueries = database.userQueries
    
    suspend fun getAllUsers(): List<User> = withContext(Dispatchers.IO) {
        userQueries.selectAll().executeAsList()
    }
    
    suspend fun getUserById(id: Long): User? = withContext(Dispatchers.IO) {
        userQueries.selectById(id).executeAsOneOrNull()
    }
    
    suspend fun insertUser(user: UserInsert): Long = withContext(Dispatchers.IO) {
        userQueries.transactionWithResult {
            userQueries.insert(user.name, user.email, user.avatarUrl)
            userQueries.lastInsertRowId().executeAsOne()
        }
    }
    
    suspend fun updateUser(id: Long, user: UserUpdate): Boolean = withContext(Dispatchers.IO) {
        userQueries.updateUser(user.name, user.email, user.avatarUrl, id)
        userQueries.changes().executeAsOne() > 0
    }
    
    suspend fun deleteUser(id: Long): Boolean = withContext(Dispatchers.IO) {
        userQueries.deleteById(id)
        userQueries.changes().executeAsOne() > 0
    }
}
```

### 缓存策略实现
```kotlin
class CacheManager {
    private val memoryCache = mutableMapOf<String, CacheEntry>()
    private val maxCacheSize = 100
    private val defaultTtl = 5 * 60 * 1000L // 5分钟
    
    data class CacheEntry(
        val data: Any,
        val timestamp: Long,
        val ttl: Long
    )
    
    fun <T> put(key: String, data: T, ttl: Long = defaultTtl) {
        if (memoryCache.size >= maxCacheSize) {
            evictOldest()
        }
        
        memoryCache[key] = CacheEntry(
            data = data as Any,
            timestamp = System.currentTimeMillis(),
            ttl = ttl
        )
    }
    
    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String): T? {
        val entry = memoryCache[key] ?: return null
        
        if (System.currentTimeMillis() - entry.timestamp > entry.ttl) {
            memoryCache.remove(key)
            return null
        }
        
        return entry.data as? T
    }
    
    fun remove(key: String) {
        memoryCache.remove(key)
    }
    
    fun clear() {
        memoryCache.clear()
    }
    
    private fun evictOldest() {
        val oldestKey = memoryCache.minByOrNull { it.value.timestamp }?.key
        oldestKey?.let { memoryCache.remove(it) }
    }
}
```

## 🎨 UI 开发进阶

### 自定义 Compose 组件
```kotlin
@Composable
fun LoadingButton(
    text: String,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(text)
        }
    }
}

@Composable
fun SwipeRefreshList<T>(
    items: List<T>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    itemContent: @Composable (T) -> Unit,
    modifier: Modifier = Modifier
) {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = onRefresh
    )
    
    Box(
        modifier = modifier.pullRefresh(pullRefreshState)
    ) {
        LazyColumn {
            items(items) { item ->
                itemContent(item)
            }
            
            item {
                LaunchedEffect(Unit) {
                    onLoadMore()
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}
```

### 主题系统设计
```kotlin
@Composable
fun UnifyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1976D2),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBBDEFB),
    onPrimaryContainer = Color(0xFF0D47A1),
    secondary = Color(0xFF03DAC6),
    onSecondary = Color.Black,
    tertiary = Color(0xFF018786),
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF0D47A1),
    primaryContainer = Color(0xFF1565C0),
    onPrimaryContainer = Color(0xFFE3F2FD),
    secondary = Color(0xFF03DAC6),
    onSecondary = Color.Black,
    tertiary = Color(0xFF018786),
    background = Color(0xFF10131C),
    surface = Color(0xFF10131C),
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5),
)
```

## 🚀 性能优化策略

### 内存优化
```kotlin
class MemoryOptimizer {
    companion object {
        // 图片缓存优化
        fun optimizeImageLoading() {
            // 使用适当的图片格式和尺寸
            // 实施图片懒加载
            // 使用内存缓存和磁盘缓存
        }
        
        // 列表优化
        @Composable
        fun OptimizedLazyColumn(
            items: List<Any>,
            itemContent: @Composable (Any) -> Unit
        ) {
            LazyColumn {
                items(
                    items = items,
                    key = { it.hashCode() } // 提供稳定的 key
                ) { item ->
                    itemContent(item)
                }
            }
        }
        
        // 状态优化
        @Composable
        fun rememberExpensiveCalculation(
            input: String
        ): String {
            return remember(input) {
                // 昂贵的计算操作
                input.uppercase().reversed()
            }
        }
    }
}
```

### 启动优化
```kotlin
class AppInitializer {
    fun initialize(context: Context) {
        // 异步初始化非关键组件
        CoroutineScope(Dispatchers.IO).launch {
            initializeNetworking()
            initializeDatabase()
            initializeAnalytics()
        }
        
        // 同步初始化关键组件
        initializeCrashReporting()
        initializeLogging()
    }
    
    private suspend fun initializeNetworking() {
        // 预热网络连接
        delay(100)
    }
    
    private suspend fun initializeDatabase() {
        // 数据库预热
        delay(200)
    }
    
    private suspend fun initializeAnalytics() {
        // 分析工具初始化
        delay(50)
    }
    
    private fun initializeCrashReporting() {
        // 崩溃报告工具初始化
    }
    
    private fun initializeLogging() {
        // 日志系统初始化
    }
}
```

## 🧪 测试策略

### 单元测试最佳实践
```kotlin
class UserRepositoryTest {
    private lateinit var repository: UserRepository
    private lateinit var mockApiService: ApiService
    private lateinit var mockDatabase: UserDatabase
    
    @BeforeEach
    fun setup() {
        mockApiService = mockk()
        mockDatabase = mockk()
        repository = UserRepositoryImpl(mockApiService, mockDatabase)
    }
    
    @Test
    fun `getUserById should return user when found`() = runTest {
        // Given
        val userId = 1L
        val expectedUser = User(id = userId, name = "Test User", email = "test@example.com")
        coEvery { mockDatabase.getUserById(userId) } returns expectedUser
        
        // When
        val result = repository.getUserById(userId)
        
        // Then
        assertEquals(expectedUser, result)
        coVerify { mockDatabase.getUserById(userId) }
    }
    
    @Test
    fun `createUser should handle network error gracefully`() = runTest {
        // Given
        val newUser = CreateUserRequest(name = "New User", email = "new@example.com")
        coEvery { mockApiService.createUser(newUser) } throws NetworkException("Network error")
        
        // When & Then
        assertThrows<NetworkException> {
            repository.createUser(newUser)
        }
    }
}
```

### UI 测试实现
```kotlin
@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    
    @Test
    fun incrementButtonIncreasesCounter() {
        // 验证初始状态
        composeTestRule.onNodeWithText("Count: 0").assertExists()
        
        // 点击增加按钮
        composeTestRule.onNodeWithText("Increment").performClick()
        
        // 验证计数器增加
        composeTestRule.onNodeWithText("Count: 1").assertExists()
    }
    
    @Test
    fun resetButtonResetsCounter() {
        // 先增加计数器
        repeat(3) {
            composeTestRule.onNodeWithText("Increment").performClick()
        }
        
        // 点击重置按钮
        composeTestRule.onNodeWithText("Reset").performClick()
        
        // 验证计数器重置
        composeTestRule.onNodeWithText("Count: 0").assertExists()
    }
}
```

## 🔧 工具链优化

### 自定义 Gradle 任务
```kotlin
// buildSrc/src/main/kotlin/CodeQualityPlugin.kt
class CodeQualityPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register("codeQualityCheck") {
            group = "verification"
            description = "Run all code quality checks"
            
            dependsOn("detekt", "ktlintCheck", "test")
        }
        
        project.tasks.register("generateApiDocs") {
            group = "documentation"
            description = "Generate API documentation"
            
            doLast {
                // 生成 API 文档的逻辑
            }
        }
    }
}
```

### CI/CD 优化
```yaml
# .github/workflows/ci.yml
name: CI/CD Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v4
    
    - name: Setup JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Cache Gradle dependencies
      uses: actions/cache@v3
      with:
        path: |
          ~/.gradle/caches
          ~/.gradle/wrapper
        key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
    
    - name: Run tests
      run: ./gradlew test
    
    - name: Run code quality checks
      run: ./gradlew codeQualityCheck
    
    - name: Build all platforms
      run: ./gradlew build
    
    - name: Upload test results
      uses: actions/upload-artifact@v3
      if: always()
      with:
        name: test-results
        path: '**/build/test-results/**/*.xml'
```

## 📈 监控和分析

### 性能监控
```kotlin
class PerformanceMonitor {
    private val metrics = mutableMapOf<String, Long>()
    
    inline fun <T> measureTime(operation: String, block: () -> T): T {
        val startTime = System.currentTimeMillis()
        val result = block()
        val endTime = System.currentTimeMillis()
        
        metrics[operation] = endTime - startTime
        logPerformance(operation, endTime - startTime)
        
        return result
    }
    
    private fun logPerformance(operation: String, duration: Long) {
        when {
            duration > 1000 -> Logger.warn("Slow operation: $operation took ${duration}ms")
            duration > 500 -> Logger.info("Operation: $operation took ${duration}ms")
        }
    }
    
    fun getMetrics(): Map<String, Long> = metrics.toMap()
    
    fun resetMetrics() {
        metrics.clear()
    }
}
```

### 错误追踪
```kotlin
class ErrorTracker {
    fun trackError(
        error: Throwable,
        context: Map<String, Any> = emptyMap(),
        severity: ErrorSeverity = ErrorSeverity.ERROR
    ) {
        val errorReport = ErrorReport(
            message = error.message ?: "Unknown error",
            stackTrace = error.stackTraceToString(),
            context = context,
            severity = severity,
            timestamp = System.currentTimeMillis(),
            platform = getPlatformInfo()
        )
        
        // 发送到错误追踪服务
        sendErrorReport(errorReport)
    }
    
    private fun sendErrorReport(report: ErrorReport) {
        // 实现错误报告发送逻辑
    }
}

enum class ErrorSeverity {
    INFO, WARNING, ERROR, CRITICAL
}

data class ErrorReport(
    val message: String,
    val stackTrace: String,
    val context: Map<String, Any>,
    val severity: ErrorSeverity,
    val timestamp: Long,
    val platform: String
)
```

---

通过这些高级技术和最佳实践，您可以构建出高质量、高性能的 Unify KMP 应用，为用户提供卓越的跨平台体验。
