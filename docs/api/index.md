# API 参考

## 📚 API 文档概览

Unify KMP 提供了完整的跨平台 API 接口，支持在所有目标平台上实现一致的功能体验。本节包含详细的 API 参考文档，帮助开发者快速上手和深入使用框架功能。

## 🔧 核心 API

### [核心 API 参考](./core.md)
包含 Unify KMP 框架的核心组件和接口：
- **HelloWorldApp** - 主应用组件
- **PlatformInfo** - 平台信息抽象接口
- **状态管理** - 响应式状态管理系统
- **主题系统** - Material3 主题配置
- **错误处理** - 统一错误处理机制

## 🎨 UI 组件 API

### 基础组件
```kotlin
// 按钮组件
@Composable
fun UnifyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
)

// 文本输入组件
@Composable
fun UnifyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "",
    placeholder: String = "",
    modifier: Modifier = Modifier
)

// 卡片组件
@Composable
fun UnifyCard(
    modifier: Modifier = Modifier,
    elevation: Dp = 4.dp,
    content: @Composable () -> Unit
)
```

### 布局组件
```kotlin
// 响应式网格
@Composable
fun ResponsiveGrid(
    columns: Int,
    spacing: Dp = 16.dp,
    modifier: Modifier = Modifier,
    content: @Composable LazyGridScope.() -> Unit
)

// 自适应容器
@Composable
fun AdaptiveContainer(
    modifier: Modifier = Modifier,
    maxWidth: Dp = 1200.dp,
    content: @Composable () -> Unit
)
```

## 🌐 平台接口 API

### 平台抽象接口
```kotlin
// 平台信息接口
expect class PlatformInfo {
    companion object {
        fun getPlatformName(): String
        fun getDeviceInfo(): String
        fun getSystemVersion(): String
    }
}

// 文件系统接口
expect class FileSystem {
    suspend fun readFile(path: String): String?
    suspend fun writeFile(path: String, content: String): Boolean
    suspend fun deleteFile(path: String): Boolean
    suspend fun listFiles(directory: String): List<String>
}

// 网络接口
expect class NetworkManager {
    suspend fun isNetworkAvailable(): Boolean
    suspend fun getNetworkType(): NetworkType
}
```

### 设备能力接口
```kotlin
// 相机接口
expect class CameraManager {
    suspend fun takePicture(): ByteArray?
    suspend fun selectFromGallery(): ByteArray?
}

// 位置服务接口
expect class LocationManager {
    suspend fun getCurrentLocation(): Location?
    suspend fun requestLocationPermission(): Boolean
}

// 通知接口
expect class NotificationManager {
    suspend fun showNotification(title: String, message: String)
    suspend fun requestNotificationPermission(): Boolean
}
```

## 🔧 工具类 API

### 日志工具
```kotlin
object Logger {
    fun d(tag: String, message: String)
    fun i(tag: String, message: String)
    fun w(tag: String, message: String)
    fun e(tag: String, message: String, throwable: Throwable? = null)
}
```

### 缓存工具
```kotlin
class CacheManager {
    suspend fun put(key: String, value: Any, ttl: Long = 300000L)
    suspend fun get(key: String): Any?
    suspend fun remove(key: String)
    suspend fun clear()
}
```

### 加密工具
```kotlin
object CryptoUtils {
    fun encryptAES(data: String, key: String): String
    fun decryptAES(encryptedData: String, key: String): String
    fun generateHash(input: String, algorithm: HashAlgorithm = HashAlgorithm.SHA256): String
}
```

## 📡 网络 API

### HTTP 客户端
```kotlin
class HttpClient {
    suspend fun get(url: String, headers: Map<String, String> = emptyMap()): HttpResponse
    suspend fun post(url: String, body: Any, headers: Map<String, String> = emptyMap()): HttpResponse
    suspend fun put(url: String, body: Any, headers: Map<String, String> = emptyMap()): HttpResponse
    suspend fun delete(url: String, headers: Map<String, String> = emptyMap()): HttpResponse
}

data class HttpResponse(
    val status: Int,
    val headers: Map<String, String>,
    val body: String
)
```

### WebSocket 客户端
```kotlin
class WebSocketClient {
    suspend fun connect(url: String): WebSocketConnection
    suspend fun disconnect()
    fun onMessage(callback: (String) -> Unit)
    fun onError(callback: (Throwable) -> Unit)
}
```

## 💾 数据存储 API

### 键值存储
```kotlin
expect class KeyValueStorage {
    suspend fun putString(key: String, value: String)
    suspend fun getString(key: String): String?
    suspend fun putInt(key: String, value: Int)
    suspend fun getInt(key: String, defaultValue: Int = 0): Int
    suspend fun putBoolean(key: String, value: Boolean)
    suspend fun getBoolean(key: String, defaultValue: Boolean = false): Boolean
    suspend fun remove(key: String)
    suspend fun clear()
}
```

### 数据库 API
```kotlin
// SQLDelight 数据库接口
interface DatabaseQueries {
    fun selectAll(): Flow<List<Entity>>
    fun selectById(id: Long): Entity?
    fun insert(entity: Entity): Long
    fun update(entity: Entity)
    fun delete(id: Long)
}
```

## 🎯 状态管理 API

### ViewModel 基类
```kotlin
abstract class BaseViewModel : ViewModel() {
    protected val _uiState = MutableStateFlow(getInitialState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    abstract fun getInitialState(): UiState
    abstract fun handleIntent(intent: Intent)
}
```

### 状态容器
```kotlin
class StateContainer<T> {
    private val _state = MutableStateFlow<T?>(null)
    val state: StateFlow<T?> = _state.asStateFlow()
    
    fun updateState(newState: T) {
        _state.value = newState
    }
    
    fun clearState() {
        _state.value = null
    }
}
```

## 🔍 搜索和过滤

### 搜索 API
```kotlin
class SearchManager<T> {
    fun search(
        items: List<T>,
        query: String,
        searchFields: List<(T) -> String>
    ): List<T>
    
    fun fuzzySearch(
        items: List<T>,
        query: String,
        searchFields: List<(T) -> String>,
        threshold: Double = 0.6
    ): List<T>
}
```

## 📱 平台特定 API

### Android 特定
```kotlin
// Android 上下文访问
expect fun getApplicationContext(): Any

// Activity 生命周期
expect class ActivityLifecycleObserver {
    fun onCreate(callback: () -> Unit)
    fun onResume(callback: () -> Unit)
    fun onPause(callback: () -> Unit)
    fun onDestroy(callback: () -> Unit)
}
```

### iOS 特定
```kotlin
// iOS 视图控制器访问
expect fun getRootViewController(): Any

// iOS 生命周期
expect class ViewControllerLifecycleObserver {
    fun viewDidLoad(callback: () -> Unit)
    fun viewWillAppear(callback: () -> Unit)
    fun viewDidDisappear(callback: () -> Unit)
}
```

## 🚀 性能监控 API

### 性能指标
```kotlin
class PerformanceMonitor {
    fun startTrace(name: String): TraceId
    fun stopTrace(traceId: TraceId)
    fun recordMetric(name: String, value: Double)
    fun getMetrics(): Map<String, Double>
}
```

## 📖 使用示例

### 基本用法
```kotlin
@Composable
fun MyApp() {
    val platformInfo = remember { PlatformInfo.getPlatformName() }
    
    UnifyCard {
        Column {
            Text("Running on: $platformInfo")
            UnifyButton(
                text = "Click Me",
                onClick = { /* 处理点击 */ }
            )
        }
    }
}
```

### 网络请求示例
```kotlin
class ApiService {
    private val httpClient = HttpClient()
    
    suspend fun fetchUserData(userId: String): Result<User> {
        return try {
            val response = httpClient.get("/api/users/$userId")
            if (response.status == 200) {
                val user = Json.decodeFromString<User>(response.body)
                Result.success(user)
            } else {
                Result.failure(Exception("HTTP ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

## 📚 更多资源

- [核心 API 详细文档](./core.md)
- [Hello World 示例](../examples/hello-world.md)
- [平台开发指南](../platforms/)
- [故障排除指南](../guide/troubleshooting.md)

---

通过这些 API，您可以构建功能丰富的跨平台应用，享受 Kotlin Multiplatform 带来的开发效率提升。
