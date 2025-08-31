# Unify-Core 生产级开发指南

## 🎯 项目概述

Unify-Core 是一个严格遵循 **100% Compose 语法** 的生产级 Kotlin Multiplatform 跨平台框架，实现 **85%+ 代码复用率**，支持 Android、iOS、Desktop、Web、HarmonyOS、小程序等多个平台。

### 核心设计原则

- ✅ **100% 纯 Compose 语法** - 禁止任何自定义 DSL
- ✅ **85%+ 代码复用率** - 平台特定代码 < 15%
- ✅ **渐进式适配** - 优先核心功能，逐步扩展
- ✅ **生产级质量** - 完整测试、性能监控、文档

## 🏗️ 技术架构

### 技术栈配置

```kotlin
object UnifyTechStack {
    // 核心框架 - 使用稳定版本
    const val KOTLIN_VERSION = "1.9.22"
    const val COMPOSE_MULTIPLATFORM_VERSION = "1.5.12"
    
    // 基础库
    const val COROUTINES_VERSION = "1.8.1"
    const val KTOR_VERSION = "2.3.12"
    const val KOIN_VERSION = "3.5.6"
}
```

### 模块化架构

```
unify-core/
├── shared/                    # 共享业务逻辑 (85%+)
│   ├── commonMain/           # 跨平台共享代码
│   │   ├── architecture/     # 核心架构
│   │   ├── ui/components/    # UI 组件库
│   │   ├── mvi/             # MVI 状态管理
│   │   ├── data/            # 数据层
│   │   ├── performance/     # 性能监控
│   │   └── platform/        # 平台抽象
│   ├── androidMain/         # Android 特定实现
│   ├── iosMain/            # iOS 特定实现
│   └── jsMain/             # Web 特定实现
├── androidApp/             # Android 宿主应用
├── iosApp/                # iOS 宿主应用
├── desktopApp/            # 桌面端宿主应用
├── webApp/                # Web 宿主应用
├── harmonyosApp/          # HarmonyOS 适配层
└── miniProgramAdapter/    # 小程序适配层
```

## 🎨 UI 组件库使用

### 基础组件

```kotlin
@Composable
fun MyScreen() {
    UnifyApp {
        Column {
            // 自适应卡片
            UnifyCard {
                Text("内容")
            }
            
            // 自适应按钮
            UnifyButton(
                onClick = { /* 处理点击 */ },
                text = "点击我"
            )
            
            // 自适应输入框
            UnifyTextField(
                value = text,
                onValueChange = { text = it },
                label = "输入内容"
            )
        }
    }
}
```

### 状态管理组件

```kotlin
@Composable
fun StatefulContent() {
    val state = UnifyComponentState(
        isLoading = false,
        error = null,
        isEmpty = false
    )
    
    UnifyStatefulContent(
        state = state,
        data = myData,
        onRetry = { /* 重试逻辑 */ }
    ) { data ->
        // 渲染数据
        Text("数据: $data")
    }
}
```

## 🔄 MVI 架构实现

### 定义状态、意图和效果

```kotlin
// 状态
data class MyState(
    val items: List<Item> = emptyList(),
    val isLoading: Boolean = false
) : UnifyState

// 意图
sealed class MyIntent : UnifyIntent {
    object LoadItems : MyIntent()
    data class AddItem(val item: Item) : MyIntent()
}

// 效果
sealed class MyEffect : UnifyEffect {
    data class ShowMessage(val message: String) : MyEffect()
}
```

### 实现 ViewModel

```kotlin
class MyViewModel(scope: CoroutineScope) : 
    UnifyViewModel<MyIntent, MyState, MyEffect>(
        initialState = MyState(),
        scope = scope
    ) {
    
    override fun handleIntent(intent: MyIntent) {
        when (intent) {
            MyIntent.LoadItems -> {
                handleAsyncIntent(intent) {
                    val items = repository.getItems()
                    updateState { state -> 
                        state.copy(items = items) 
                    }
                }
            }
            is MyIntent.AddItem -> {
                updateState { state ->
                    state.copy(items = state.items + intent.item)
                }
                sendEffect(MyEffect.ShowMessage("已添加"))
            }
        }
    }
}
```

### UI 集成

```kotlin
@Composable
fun MyScreen() {
    val scope = rememberCoroutineScope()
    val viewModel = remember { MyViewModel(scope) }
    
    UnifyMVIContainer(
        stateManager = viewModel,
        onEffect = { effect ->
            when (effect) {
                is MyEffect.ShowMessage -> {
                    // 显示消息
                }
            }
        }
    ) { state, isLoading, onIntent ->
        // UI 渲染
        if (isLoading) {
            UnifyLoadingIndicator()
        } else {
            LazyColumn {
                items(state.items) { item ->
                    ItemCard(item)
                }
            }
        }
    }
}
```

## 💾 数据层实现

### Repository 模式

```kotlin
class MyRepository(
    private val networkDataSource: MyNetworkDataSource,
    private val localDataSource: MyLocalDataSource
) : UnifyBaseRepository<Item, String>(
    networkDataSource = networkDataSource,
    localDataSource = localDataSource,
    cacheStrategy = UnifyCacheStrategy.CACHE_FIRST
) {
    
    suspend fun getItemsByCategory(category: String): UnifyResult<List<Item>> {
        return try {
            val items = networkDataSource.fetchByCategory(category)
            UnifyResult.Success(items)
        } catch (e: Exception) {
            UnifyResult.Error(e)
        }
    }
}
```

### 网络数据源

```kotlin
class MyNetworkDataSource : UnifyNetworkDataSource<Item, String> {
    private val httpClient = HttpClient()
    
    override suspend fun fetchAll(): List<Item> {
        return httpClient.get("https://api.example.com/items")
            .body<List<Item>>()
    }
    
    override suspend fun fetchById(id: String): Item? {
        return httpClient.get("https://api.example.com/items/$id")
            .body<Item>()
    }
}
```

## ⚡ 性能优化

### Compose 性能优化

```kotlin
@Composable
fun OptimizedScreen() {
    // 使用性能监控装饰器
    PerformanceTracker("MyScreen") {
        // 稳定性标记
        val stableData = remember { 
            UnifyComposeOptimizer.StableData(myData) 
        }
        
        // 记忆化计算
        val expensiveValue = UnifyComposeOptimizer.rememberCalculation(
            stableData
        ) {
            performExpensiveCalculation(stableData.value)
        }
        
        // UI 内容
        MyContent(expensiveValue)
    }
}
```

### 内存优化

```kotlin
// 图片内存优化
val optimizedSize = UnifyMemoryOptimizer.optimizeImageMemory(imageSize)

// 列表内存优化
val optimizedList = UnifyMemoryOptimizer.optimizeListMemory(largeList, 1000)

// 缓存清理
UnifyMemoryOptimizer.clearCache()
```

### 网络优化

```kotlin
// 请求批处理
val batcher = UnifyNetworkOptimizer.RequestBatcher<MyRequest>()
batcher.addRequest(request)
if (batcher.shouldFlush()) {
    val batch = batcher.flush()
    processBatch(batch)
}

// 响应缓存
val cache = UnifyNetworkOptimizer.ResponseCache<String, MyResponse>()
val cachedResponse = cache.get("key", maxAge = 5 * 60 * 1000) // 5分钟
```

## 🌐 平台特定实现

### Android 平台

```kotlin
// AndroidManifest.xml
<application
    android:name=".UnifyApplication"
    android:theme="@style/Theme.Unify">
    
    <activity
        android:name=".MainActivity"
        android:exported="true"
        android:theme="@style/Theme.Unify">
        <intent-filter>
            <action android:name="android.intent.action.MAIN" />
            <category android:name="android.intent.category.LAUNCHER" />
        </intent-filter>
    </activity>
</application>

// MainActivity.kt
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UnifyDemoApp()
        }
    }
}
```

### iOS 平台

```swift
// ContentView.swift
import SwiftUI
import shared

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.keyboard)
    }
}

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }
    
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
```

### HarmonyOS 适配

```typescript
// HarmonyOS ArkTS
@Entry
@Component
struct UnifyHarmonyPage {
  private unifyBridge: UnifyBridge = new UnifyBridge()
  
  aboutToAppear() {
    this.unifyBridge.initialize()
  }
  
  build() {
    Column() {
      // 通过 Bridge 渲染 Unify 内容
      this.unifyBridge.renderContent()
    }
    .width('100%')
    .height('100%')
  }
}
```

### 小程序适配

```javascript
// 微信小程序 app.js
const UnifyMiniAdapter = require('./unify-adapter/UnifyAdapter')

App({
  onLaunch() {
    UnifyMiniAdapter.initialize({
      apiVersion: '2.0',
      platform: 'wechat'
    })
  }
})

// 页面 index.wxml
<view class="container">
  <view class="unify-content">
    <!-- 转换后的小程序结构 -->
  </view>
</view>
```

## 🧪 测试策略

### 单元测试

```kotlin
class MyViewModelTest {
    private val testRepository = TestRepository()
    private lateinit var viewModel: MyViewModel
    
    @BeforeTest
    fun setup() {
        viewModel = MyViewModel(TestScope())
    }
    
    @Test
    fun `when load items, should update state`() = runTest {
        // Given
        val testItems = listOf(Item("1", "Test"))
        testRepository.setTestData(testItems)
        
        // When
        viewModel.handleIntent(MyIntent.LoadItems)
        
        // Then
        val state = viewModel.state.value
        assertEquals(testItems, state.items)
    }
}
```

### UI 测试

```kotlin
@OptIn(ExperimentalTestApi::class)
class UnifyUITest {
    @Test
    fun `UnifyButton should handle click events`() = runComposeUiTest {
        var clicked = false
        
        setContent {
            UnifyTheme {
                UnifyButton(
                    onClick = { clicked = true },
                    text = "Click Me"
                )
            }
        }
        
        onNodeWithText("Click Me").performClick()
        assertTrue(clicked)
    }
}
```

## 📊 性能监控

### 初始化监控

```kotlin
// 应用启动时
UnifyPerformanceMonitor.initialize()

// 记录性能指标
UnifyPerformanceMonitor.recordFrameTime(16) // 60 FPS
UnifyPerformanceMonitor.recordMemoryUsage(50 * 1024 * 1024) // 50MB
UnifyPerformanceMonitor.recordNetworkRequest("api/items", 200, true)
```

### 性能分析

```kotlin
// 获取性能摘要
val summary = UnifyPerformanceMonitor.getPerformanceSummary()
println("平均帧率: ${summary.averageFrameRate}")
println("内存使用: ${summary.memoryUsage} MB")
println("网络请求: ${summary.networkRequestCount}")
```

## 🚀 最佳实践

### 1. 代码组织

- 按功能模块组织代码，不按平台
- 共享代码放在 `commonMain`
- 平台特定代码使用 `expect/actual`
- 保持 85%+ 代码复用率

### 2. 性能优化

- 使用 `@Stable` 和 `@Immutable` 注解
- 合理使用 `remember` 和 `LaunchedEffect`
- 避免在 Compose 中进行重复计算
- 监控内存使用和帧率

### 3. 状态管理

- 使用 MVI 架构模式
- 状态应该是不可变的
- 副作用通过 Effect 处理
- 异步操作使用协程

### 4. 错误处理

- 使用 `UnifyResult` 封装操作结果
- 提供友好的错误提示
- 实现重试机制
- 记录错误日志

## 📚 示例代码

完整的示例应用位于 `shared/src/commonMain/kotlin/com/unify/demo/`：

- `UnifyDemoApp.kt` - 主应用入口
- `TodoListScreen.kt` - 待办事项（MVI 架构演示）
- `ProfileScreen.kt` - 用户资料（表单验证演示）
- `SettingsScreen.kt` - 应用设置（偏好管理演示）
- `PerformanceScreen.kt` - 性能监控（实时指标展示）

## 🔧 开发工具

### 构建脚本

```bash
# 清理项目
./gradlew clean

# 构建所有平台
./gradlew build

# 运行 Android
./gradlew :androidApp:installDebug

# 运行桌面端
./gradlew :desktopApp:run

# 性能基准测试
./scripts/benchmark.sh
```

### 代码质量

```bash
# 代码格式化
./gradlew ktlintFormat

# 静态分析
./gradlew detekt

# 测试覆盖率
./gradlew koverHtmlReport
```

## 📈 性能指标

### 目标指标

- **启动时间**: < 2秒（冷启动）
- **帧率**: 60 FPS
- **内存使用**: < 100MB
- **代码复用率**: > 85%
- **包体积**: < 10MB

### 监控工具

- 实时性能监控
- 内存使用分析
- 网络请求监控
- Compose 重组分析

## 🤝 贡献指南

1. Fork 项目
2. 创建功能分支
3. 提交更改
4. 推送到分支
5. 创建 Pull Request

### 代码规范

- 遵循 Kotlin 编码规范
- 使用有意义的变量名
- 添加必要的注释
- 编写单元测试

## 📄 许可证

本项目采用 MIT 许可证。

---

**Unify-Core** - 让跨平台开发更简单、更高效！

🌟 **如果这个项目对你有帮助，请给我们一个星标！**
