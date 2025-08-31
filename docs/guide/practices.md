# Unify-Core 最佳实践指南

## 架构设计原则

### 1. 跨平台一致性原则

**核心理念**: "一套代码，多端复用"

```kotlin
// ✅ 推荐：使用expect/actual机制
// commonMain
expect class NetworkClient {
    suspend fun request(url: String): Response
}

// androidMain
actual class NetworkClient {
    actual suspend fun request(url: String): Response {
        return OkHttpClient().newCall(Request.Builder().url(url).build()).execute()
    }
}

// iosMain  
actual class NetworkClient {
    actual suspend fun request(url: String): Response {
        return NSURLSession.shared.dataTask(with: URL(string: url)!!)
    }
}
```

**避免的做法**:
```kotlin
// ❌ 避免：平台特定代码混合
class NetworkClient {
    fun request(url: String): Response {
        return if (Platform.isAndroid()) {
            // Android特定代码
        } else if (Platform.isIOS()) {
            // iOS特定代码
        } else {
            // 其他平台代码
        }
    }
}
```

### 2. 模块化设计原则

**单一职责原则**:
```kotlin
// ✅ 推荐：职责清晰的模块设计
interface AuthenticationManager {
    suspend fun authenticate(credentials: Credentials): AuthResult
    suspend fun refreshToken(token: String): String
    suspend fun logout()
}

interface UserProfileManager {
    suspend fun getUserProfile(userId: String): UserProfile
    suspend fun updateProfile(profile: UserProfile): Boolean
}

interface SecurityManager {
    fun encrypt(data: String): String
    fun decrypt(encryptedData: String): String
    fun validateInput(input: String): ValidationResult
}
```

### 3. 依赖注入原则

```kotlin
// ✅ 推荐：使用依赖注入
class UserRepository(
    private val networkClient: NetworkClient,
    private val localDatabase: LocalDatabase,
    private val securityManager: SecurityManager
) {
    suspend fun getUser(id: String): User {
        return try {
            val encryptedUser = networkClient.getUser(id)
            val user = securityManager.decrypt(encryptedUser)
            localDatabase.cacheUser(user)
            user
        } catch (e: NetworkException) {
            localDatabase.getUser(id) ?: throw UserNotFoundException(id)
        }
    }
}

// 依赖注入配置
val appModule = module {
    single<NetworkClient> { NetworkClientImpl() }
    single<LocalDatabase> { LocalDatabaseImpl() }
    single<SecurityManager> { SecurityManagerImpl() }
    single { UserRepository(get(), get(), get()) }
}
```

## 性能优化最佳实践

### 1. 内存管理

**对象池模式**:
```kotlin
class ComponentPool<T>(
    private val factory: () -> T,
    private val reset: (T) -> Unit,
    private val maxSize: Int = 10
) {
    private val pool = ArrayDeque<T>(maxSize)
    
    fun acquire(): T {
        return pool.removeFirstOrNull() ?: factory()
    }
    
    fun release(item: T) {
        if (pool.size < maxSize) {
            reset(item)
            pool.addLast(item)
        }
    }
}

// 使用示例
val componentPool = ComponentPool(
    factory = { DynamicComponent() },
    reset = { it.reset() }
)
```

**内存泄漏预防**:
```kotlin
class MemoryAwareComponent : DisposableHandle {
    private val resources = mutableListOf<Closeable>()
    private var isDisposed = false
    
    fun addResource(resource: Closeable) {
        if (!isDisposed) {
            resources.add(resource)
        }
    }
    
    override fun dispose() {
        if (!isDisposed) {
            resources.forEach { it.close() }
            resources.clear()
            isDisposed = true
        }
    }
}
```

### 2. 渲染性能优化

**Compose优化**:
```kotlin
// ✅ 推荐：使用稳定的参数和remember
@Composable
fun OptimizedList(
    items: List<String>, // 稳定参数
    onItemClick: (String) -> Unit // 稳定回调
) {
    val processedItems = remember(items) {
        items.map { it.uppercase() }
    }
    
    LazyColumn {
        items(
            items = processedItems,
            key = { it } // 提供稳定的key
        ) { item ->
            ItemCard(
                text = item,
                onClick = { onItemClick(item) }
            )
        }
    }
}

// ✅ 推荐：使用derivedStateOf避免不必要的重组
@Composable
fun SearchScreen(query: String) {
    val filteredItems by remember {
        derivedStateOf {
            allItems.filter { it.contains(query, ignoreCase = true) }
        }
    }
    
    LazyColumn {
        items(filteredItems) { item ->
            Text(item)
        }
    }
}
```

### 3. 网络性能优化

**请求优化策略**:
```kotlin
class OptimizedNetworkManager {
    private val requestCache = LRUCache<String, CachedResponse>(100)
    private val requestDeduplicator = mutableMapOf<String, Deferred<Response>>()
    
    suspend fun request(url: String, cachePolicy: CachePolicy = CachePolicy.CACHE_FIRST): Response {
        // 缓存策略
        when (cachePolicy) {
            CachePolicy.CACHE_FIRST -> {
                requestCache[url]?.let { cached ->
                    if (!cached.isExpired()) return cached.response
                }
            }
            CachePolicy.NETWORK_FIRST -> {
                // 优先网络请求
            }
        }
        
        // 请求去重
        requestDeduplicator[url]?.let { return it.await() }
        
        val deferred = async {
            try {
                val response = httpClient.get(url)
                requestCache[url] = CachedResponse(response, System.currentTimeMillis())
                response
            } finally {
                requestDeduplicator.remove(url)
            }
        }
        
        requestDeduplicator[url] = deferred
        return deferred.await()
    }
}
```

## 安全开发最佳实践

### 1. 输入验证和清理

```kotlin
class SecureInputProcessor {
    private val sqlInjectionPatterns = listOf(
        Regex("(?i)(union|select|insert|update|delete|drop|create|alter)\\s"),
        Regex("(?i)(or|and)\\s+\\d+\\s*=\\s*\\d+"),
        Regex("(?i)('|(\\-\\-)|(;)|(\\||\\|)|(\\*|\\*))")
    )
    
    private val xssPatterns = listOf(
        Regex("(?i)<script[^>]*>.*?</script>"),
        Regex("(?i)javascript:"),
        Regex("(?i)on\\w+\\s*=")
    )
    
    fun validateAndSanitize(input: String): ValidationResult {
        // 1. 长度检查
        if (input.length > MAX_INPUT_LENGTH) {
            return ValidationResult.Error("输入长度超过限制")
        }
        
        // 2. SQL注入检查
        if (sqlInjectionPatterns.any { it.containsMatchIn(input) }) {
            return ValidationResult.Error("检测到SQL注入尝试")
        }
        
        // 3. XSS检查
        if (xssPatterns.any { it.containsMatchIn(input) }) {
            return ValidationResult.Error("检测到XSS攻击尝试")
        }
        
        // 4. 清理输入
        val sanitized = input
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
        
        return ValidationResult.Success(sanitized)
    }
}
```

### 2. 数据加密最佳实践

```kotlin
class SecureDataStorage {
    private val keyAlias = "UnifySecureKey"
    
    fun storeSecureData(key: String, data: String) {
        val encryptedData = encryptData(data)
        val hmac = generateHMAC(encryptedData)
        
        preferences.edit()
            .putString(key, encryptedData)
            .putString("${key}_hmac", hmac)
            .apply()
    }
    
    fun retrieveSecureData(key: String): String? {
        val encryptedData = preferences.getString(key, null) ?: return null
        val storedHmac = preferences.getString("${key}_hmac", null) ?: return null
        
        // 验证数据完整性
        val calculatedHmac = generateHMAC(encryptedData)
        if (storedHmac != calculatedHmac) {
            throw SecurityException("数据完整性验证失败")
        }
        
        return decryptData(encryptedData)
    }
    
    private fun encryptData(data: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val secretKey = getOrCreateSecretKey()
        
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val encryptedData = cipher.doFinal(data.toByteArray())
        
        // 组合IV和加密数据
        val combined = iv + encryptedData
        return Base64.encodeToString(combined, Base64.DEFAULT)
    }
}
```

### 3. 网络安全

```kotlin
class SecureNetworkClient {
    private val certificatePinner = CertificatePinner.Builder()
        .add("api.unify.com", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
        .build()
    
    private val httpClient = OkHttpClient.Builder()
        .certificatePinner(certificatePinner)
        .addInterceptor(AuthenticationInterceptor())
        .addInterceptor(SecurityHeadersInterceptor())
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    suspend fun secureRequest(request: SecureRequest): SecureResponse {
        // 请求签名
        val signedRequest = signRequest(request)
        
        // 执行请求
        val response = httpClient.newCall(signedRequest.toOkHttpRequest()).execute()
        
        // 验证响应
        return validateAndParseResponse(response)
    }
}
```

## 测试最佳实践

### 1. 单元测试策略

```kotlin
class UserRepositoryTest {
    
    @Mock
    private lateinit var networkClient: NetworkClient
    
    @Mock
    private lateinit var localDatabase: LocalDatabase
    
    @Mock
    private lateinit var securityManager: SecurityManager
    
    private lateinit var userRepository: UserRepository
    
    @BeforeEach
    fun setup() {
        MockitoAnnotations.openMocks(this)
        userRepository = UserRepository(networkClient, localDatabase, securityManager)
    }
    
    @Test
    fun `获取用户成功时应该缓存到本地数据库`() = runTest {
        // Given
        val userId = "user123"
        val expectedUser = User(userId, "John Doe")
        whenever(networkClient.getUser(userId)).thenReturn(expectedUser)
        whenever(securityManager.decrypt(any())).thenReturn(expectedUser)
        
        // When
        val result = userRepository.getUser(userId)
        
        // Then
        assertEquals(expectedUser, result)
        verify(localDatabase).cacheUser(expectedUser)
    }
    
    @Test
    fun `网络请求失败时应该从本地数据库获取`() = runTest {
        // Given
        val userId = "user123"
        val cachedUser = User(userId, "John Doe")
        whenever(networkClient.getUser(userId)).thenThrow(NetworkException())
        whenever(localDatabase.getUser(userId)).thenReturn(cachedUser)
        
        // When
        val result = userRepository.getUser(userId)
        
        // Then
        assertEquals(cachedUser, result)
    }
}
```

### 2. 集成测试模式

```kotlin
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DynamicEngineIntegrationTest {
    
    private lateinit var testEngine: UnifyDynamicEngine
    private lateinit var testSecurityManager: UnifySecurityManager
    
    @BeforeAll
    fun setupIntegrationTest() {
        testEngine = UnifyDynamicEngine()
        testSecurityManager = UnifySecurityManager()
        
        // 配置测试环境
        testEngine.setSecurityValidator(testSecurityManager)
        testEngine.initialize()
    }
    
    @Test
    fun `完整的安全组件加载流程`() = runTest {
        // 1. 准备测试数据
        val componentCode = loadTestComponentCode()
        val componentName = "SecureTestComponent"
        
        // 2. 执行安全加载
        val loadResult = testEngine.loadSecureComponent(componentName, componentCode)
        
        // 3. 验证结果
        assertTrue(loadResult.isSuccess)
        assertTrue(loadResult.securityValidated)
        assertNotNull(testEngine.getComponent(componentName))
        
        // 4. 验证安全审计日志
        val auditLogs = testSecurityManager.getAuditLogs()
        assertTrue(auditLogs.any { it.action == "component_loaded" })
    }
}
```

### 3. UI测试最佳实践

```kotlin
class UnifyComponentUITest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun `复杂交互流程测试`() {
        var state by mutableStateOf(ComponentState.INITIAL)
        
        composeTestRule.setContent {
            UnifyComplexComponent(
                state = state,
                onStateChange = { state = it }
            )
        }
        
        // 验证初始状态
        composeTestRule
            .onNodeWithTag("loading_indicator")
            .assertDoesNotExist()
        
        // 触发加载
        composeTestRule
            .onNodeWithTag("load_button")
            .performClick()
        
        // 验证加载状态
        composeTestRule
            .onNodeWithTag("loading_indicator")
            .assertExists()
        
        // 模拟加载完成
        state = ComponentState.LOADED
        
        // 验证加载完成状态
        composeTestRule
            .onNodeWithTag("content_list")
            .assertExists()
            .assertIsDisplayed()
    }
}
```

## 错误处理和恢复

### 1. 异常处理策略

```kotlin
sealed class UnifyResult<out T> {
    data class Success<T>(val data: T) : UnifyResult<T>()
    data class Error(val exception: UnifyException) : UnifyResult<Nothing>()
    data class Loading(val progress: Float = 0f) : UnifyResult<Nothing>()
}

sealed class UnifyException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class NetworkException(message: String, cause: Throwable? = null) : UnifyException(message, cause)
    class SecurityException(message: String, cause: Throwable? = null) : UnifyException(message, cause)
    class ValidationException(message: String, cause: Throwable? = null) : UnifyException(message, cause)
    class ComponentException(message: String, cause: Throwable? = null) : UnifyException(message, cause)
}

class ErrorHandler {
    suspend fun <T> safeExecute(
        operation: suspend () -> T,
        fallback: (suspend (UnifyException) -> T)? = null
    ): UnifyResult<T> {
        return try {
            UnifyResult.Success(operation())
        } catch (e: UnifyException) {
            if (fallback != null) {
                try {
                    UnifyResult.Success(fallback(e))
                } catch (fallbackError: Exception) {
                    UnifyResult.Error(UnifyException.ComponentException("Fallback failed", fallbackError))
                }
            } else {
                UnifyResult.Error(e)
            }
        } catch (e: Exception) {
            UnifyResult.Error(UnifyException.ComponentException("Unexpected error", e))
        }
    }
}
```

### 2. 重试机制

```kotlin
class RetryPolicy(
    val maxAttempts: Int = 3,
    val baseDelay: Long = 1000L,
    val maxDelay: Long = 30000L,
    val backoffMultiplier: Double = 2.0
) {
    suspend fun <T> execute(operation: suspend () -> T): T {
        var lastException: Exception? = null
        
        repeat(maxAttempts) { attempt ->
            try {
                return operation()
            } catch (e: Exception) {
                lastException = e
                
                if (attempt < maxAttempts - 1) {
                    val delay = calculateDelay(attempt)
                    kotlinx.coroutines.delay(delay)
                }
            }
        }
        
        throw lastException ?: Exception("重试失败")
    }
    
    private fun calculateDelay(attempt: Int): Long {
        val delay = (baseDelay * Math.pow(backoffMultiplier, attempt.toDouble())).toLong()
        return minOf(delay, maxDelay)
    }
}
```

## 国际化和本地化

### 1. 多语言支持

```kotlin
object UnifyStrings {
    private val strings = mapOf(
        "zh-CN" to mapOf(
            "welcome" to "欢迎使用Unify",
            "login" to "登录",
            "logout" to "退出登录"
        ),
        "en-US" to mapOf(
            "welcome" to "Welcome to Unify",
            "login" to "Login", 
            "logout" to "Logout"
        ),
        "ja-JP" to mapOf(
            "welcome" to "Unifyへようこそ",
            "login" to "ログイン",
            "logout" to "ログアウト"
        )
    )
    
    fun getString(key: String, locale: String = getCurrentLocale()): String {
        return strings[locale]?.get(key) 
            ?: strings["en-US"]?.get(key) 
            ?: key
    }
}

@Composable
fun LocalizedText(key: String) {
    val locale by LocalLocale.current
    Text(UnifyStrings.getString(key, locale))
}
```

### 2. 格式化和排版

```kotlin
class LocalizedFormatter {
    fun formatNumber(number: Double, locale: String): String {
        return when (locale) {
            "zh-CN" -> DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.CHINA)).format(number)
            "en-US" -> DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.US)).format(number)
            "ja-JP" -> DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.JAPAN)).format(number)
            else -> number.toString()
        }
    }
    
    fun formatDate(timestamp: Long, locale: String): String {
        val date = Date(timestamp)
        return when (locale) {
            "zh-CN" -> SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA).format(date)
            "en-US" -> SimpleDateFormat("MMM dd, yyyy", Locale.US).format(date)
            "ja-JP" -> SimpleDateFormat("yyyy年MM月dd日", Locale.JAPAN).format(date)
            else -> date.toString()
        }
    }
}
```

## 监控和分析

### 1. 性能监控集成

```kotlin
@Composable
fun MonitoredScreen(screenName: String) {
    LaunchedEffect(screenName) {
        UnifyPerformanceMonitor.trackScreenView(screenName)
    }
    
    DisposableEffect(screenName) {
        val startTime = System.currentTimeMillis()
        
        onDispose {
            val duration = System.currentTimeMillis() - startTime
            UnifyPerformanceMonitor.trackScreenDuration(screenName, duration)
        }
    }
    
    // 屏幕内容
}

class MonitoredRepository {
    suspend fun fetchData(): List<Data> {
        return UnifyPerformanceMonitor.measureOperation("fetch_data") {
            apiService.getData()
        }
    }
}
```

### 2. 用户行为分析

```kotlin
object UnifyAnalytics {
    fun trackEvent(eventName: String, parameters: Map<String, Any> = emptyMap()) {
        val event = AnalyticsEvent(
            name = eventName,
            parameters = parameters,
            timestamp = System.currentTimeMillis(),
            sessionId = getCurrentSessionId(),
            userId = getCurrentUserId()
        )
        
        analyticsQueue.offer(event)
    }
    
    fun trackUserAction(action: UserAction) {
        trackEvent("user_action", mapOf(
            "action_type" to action.type,
            "screen" to action.screen,
            "component" to action.component
        ))
    }
}
```

## 部署和运维

### 1. 环境配置

```kotlin
object UnifyConfig {
    private val config = when (BuildConfig.ENVIRONMENT) {
        "development" -> DevelopmentConfig()
        "staging" -> StagingConfig()
        "production" -> ProductionConfig()
        else -> throw IllegalStateException("未知环境: ${BuildConfig.ENVIRONMENT}")
    }
    
    val apiBaseUrl: String get() = config.apiBaseUrl
    val enableLogging: Boolean get() = config.enableLogging
    val enableAnalytics: Boolean get() = config.enableAnalytics
}

interface EnvironmentConfig {
    val apiBaseUrl: String
    val enableLogging: Boolean
    val enableAnalytics: Boolean
}

class ProductionConfig : EnvironmentConfig {
    override val apiBaseUrl = "https://api.unify.com"
    override val enableLogging = false
    override val enableAnalytics = true
}
```

### 2. 健康检查

```kotlin
class HealthCheckManager {
    suspend fun performHealthCheck(): HealthCheckResult {
        val checks = listOf(
            checkDatabaseConnection(),
            checkNetworkConnectivity(),
            checkMemoryUsage(),
            checkDiskSpace()
        )
        
        val failedChecks = checks.filter { !it.isHealthy }
        
        return HealthCheckResult(
            isHealthy = failedChecks.isEmpty(),
            checks = checks,
            timestamp = System.currentTimeMillis()
        )
    }
    
    private suspend fun checkDatabaseConnection(): HealthCheck {
        return try {
            database.ping()
            HealthCheck("database", true, "数据库连接正常")
        } catch (e: Exception) {
            HealthCheck("database", false, "数据库连接失败: ${e.message}")
        }
    }
}
```

## 总结

遵循这些最佳实践可以确保：

1. **代码质量**: 可维护、可测试、可扩展的代码
2. **性能优化**: 高效的内存使用和渲染性能
3. **安全保障**: 全面的安全防护和数据保护
4. **用户体验**: 一致的跨平台体验和本地化支持
5. **运维效率**: 完善的监控、日志和错误处理机制

这些实践经过生产环境验证，是构建企业级跨平台应用的重要指导原则。

---

**文档版本**: v1.0.0  
**最后更新**: 2025年1月27日  
**维护团队**: Unify-Core开发团队
