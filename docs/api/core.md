# 核心 API 参考

本文档详细介绍 Unify KMP 框架的核心 API 接口和使用方法。

## HelloWorldApp

跨平台 Hello World 应用的主要 Composable 函数。

### 函数签名

```kotlin
@Composable
fun HelloWorldApp(platformName: String = "Unknown")
```

### 参数

| 参数名 | 类型 | 默认值 | 描述 |
|--------|------|--------|------|
| `platformName` | `String` | `"Unknown"` | 显示的平台名称 |

### 功能特性

- 响应式计数器交互
- 平台信息自动显示
- Material3 设计风格
- 多语言支持准备

### 使用示例

```kotlin
// 基础使用
HelloWorldApp()

// 指定平台名称
HelloWorldApp(platformName = "Android")

// 在 Android Activity 中使用
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HelloWorldApp("Android")
        }
    }
}

// 在 iOS 中使用
fun MainViewController() = ComposeUIViewController {
    HelloWorldApp("iOS")
}
```

### 状态管理

组件内部使用 `remember` 和 `mutableIntStateOf` 管理计数器状态：

```kotlin
var count by remember { mutableIntStateOf(0) }
```

### UI 结构

```
MaterialTheme
└── Surface (全屏背景)
    └── Column (垂直布局)
        ├── Text (欢迎标题)
        ├── Text (欢迎描述)
        ├── Text (平台信息)
        ├── Text (设备信息)
        ├── Button (计数按钮)
        └── Button (重置按钮，条件显示)
```

## PlatformInfo

平台信息抽象接口，使用 `expect/actual` 机制实现跨平台功能。

### 接口定义

```kotlin
expect class PlatformInfo {
    companion object {
        fun getPlatformName(): String
        fun getDeviceInfo(): String
    }
}
```

### 方法说明

#### getPlatformName()

获取当前运行平台的名称。

**返回值**: `String` - 平台名称

**平台实现**:
- Android: `"Android"`
- iOS: `"iOS"`
- Web: `"Web"`
- Desktop: `"Desktop"`

#### getDeviceInfo()

获取当前设备的详细信息。

**返回值**: `String` - 设备信息字符串

**平台实现**:

##### Android
```kotlin
actual fun getDeviceInfo(): String = 
    "${Build.MANUFACTURER} ${Build.MODEL} (API ${Build.VERSION.SDK_INT})"
```

示例输出: `"Samsung SM-G991B (API 34)"`

##### iOS
```kotlin
actual fun getDeviceInfo(): String {
    val device = UIDevice.currentDevice
    return "${device.model} ${device.systemName} ${device.systemVersion}"
}
```

示例输出: `"iPhone iOS 17.0"`

##### Web
```kotlin
actual fun getDeviceInfo(): String = window.navigator.userAgent
```

示例输出: `"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"`

### 使用示例

```kotlin
// 获取平台名称
val platformName = PlatformInfo.getPlatformName()
println("当前平台: $platformName")

// 获取设备信息
val deviceInfo = PlatformInfo.getDeviceInfo()
println("设备信息: $deviceInfo")

// 在 Composable 中使用
@Composable
fun PlatformInfoDisplay() {
    Column {
        Text("平台: ${PlatformInfo.getPlatformName()}")
        Text("设备: ${PlatformInfo.getDeviceInfo()}")
    }
}
```

## 状态管理

### 本地状态

使用 Compose 的 `remember` 和 `mutableStateOf` 管理组件本地状态：

```kotlin
@Composable
fun CounterComponent() {
    var count by remember { mutableIntStateOf(0) }
    
    Button(onClick = { count++ }) {
        Text("Count: $count")
    }
}
```

### 共享状态

对于跨组件的状态管理，推荐使用以下模式：

```kotlin
// 状态持有者
class CounterState {
    var count by mutableIntStateOf(0)
        private set
    
    fun increment() {
        count++
    }
    
    fun reset() {
        count = 0
    }
}

// 在 Composable 中使用
@Composable
fun CounterApp() {
    val counterState = remember { CounterState() }
    
    Column {
        Text("Count: ${counterState.count}")
        Button(onClick = { counterState.increment() }) {
            Text("Increment")
        }
        Button(onClick = { counterState.reset() }) {
            Text("Reset")
        }
    }
}
```

## 主题和样式

### Material3 主题

框架默认使用 Material3 主题系统：

```kotlin
MaterialTheme {
    // 应用内容
    HelloWorldApp()
}
```

### 自定义主题

```kotlin
@Composable
fun CustomTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
```

### 常用样式

```kotlin
// 标题样式
Text(
    text = "标题",
    style = MaterialTheme.typography.headlineMedium
)

// 正文样式
Text(
    text = "正文内容",
    style = MaterialTheme.typography.bodyLarge
)

// 小号文字
Text(
    text = "辅助信息",
    style = MaterialTheme.typography.bodySmall
)
```

## 错误处理

### 异常类型

框架定义了以下异常类型：

```kotlin
// 平台不支持异常
class PlatformNotSupportedException(
    message: String
) : Exception(message)

// 初始化失败异常
class InitializationException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
```

### 错误处理模式

```kotlin
@Composable
fun SafeComponent() {
    var error by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        try {
            // 可能出错的操作
            val result = riskyOperation()
        } catch (e: Exception) {
            error = e.message
        }
    }
    
    error?.let { errorMessage ->
        Text(
            text = "错误: $errorMessage",
            color = MaterialTheme.colorScheme.error
        )
    }
}
```

## 性能优化

### Compose 优化

```kotlin
// 使用 key 优化列表性能
@Composable
fun ItemList(items: List<Item>) {
    LazyColumn {
        items(items, key = { it.id }) { item ->
            ItemRow(item)
        }
    }
}

// 避免不必要的重组
@Composable
fun OptimizedComponent(
    data: String,
    onAction: () -> Unit
) {
    val stableOnAction = remember { onAction }
    
    // 组件内容
}
```

### 内存管理

```kotlin
// 正确使用 remember
@Composable
fun ExpensiveComponent() {
    val expensiveValue = remember {
        computeExpensiveValue()
    }
    
    // 使用 expensiveValue
}

// 清理资源
@Composable
fun ResourceComponent() {
    DisposableEffect(Unit) {
        val resource = acquireResource()
        
        onDispose {
            resource.release()
        }
    }
}
```

## 调试工具

### 日志输出

```kotlin
// 平台无关的日志
expect fun logDebug(message: String)
expect fun logError(message: String, throwable: Throwable?)

// 使用示例
logDebug("应用启动")
logError("网络请求失败", exception)
```

### 性能监控

```kotlin
@Composable
fun PerformanceMonitor(content: @Composable () -> Unit) {
    val startTime = remember { System.currentTimeMillis() }
    
    LaunchedEffect(Unit) {
        val endTime = System.currentTimeMillis()
        logDebug("组件渲染耗时: ${endTime - startTime}ms")
    }
    
    content()
}
```

## 版本兼容性

| API | 最低版本 | 说明 |
|-----|----------|------|
| `HelloWorldApp` | 1.0.0 | 核心组件，稳定 API |
| `PlatformInfo` | 1.0.0 | 平台抽象，稳定 API |
| Material3 主题 | 1.0.0 | 基于 Compose 1.7.0 |

## 迁移指南

### 从 1.0.0-alpha 升级

```kotlin
// 旧版本
HelloWorld(platform = "Android")

// 新版本
HelloWorldApp(platformName = "Android")
```

### 破坏性变更

- v1.0.0: 无破坏性变更
- 未来版本的变更将在此处记录
