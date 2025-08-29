# Unify KMP - 快速开始指南

## 项目概述

Unify KMP 是一个基于 Kotlin Multiplatform 的跨平台开发框架，支持 Android、iOS、Web、Desktop 和 HarmonyOS 平台。

## 环境要求

### 基础环境
- **JDK**: OpenJDK 17 或更高版本
- **Gradle**: 8.14.2 (已包含在项目中)
- **Kotlin**: 2.0.21
- **Compose Multiplatform**: 1.7.0

### Android 开发
- **Android SDK**: API 34
- **Android Build Tools**: 34.0.0
- **ANDROID_HOME** 环境变量配置

### iOS 开发
- **Xcode**: 15.0 或更高版本
- **iOS SDK**: 17.0 或更高版本
- **macOS**: 仅在 macOS 上支持 iOS 开发

## 快速开始

### 1. 克隆项目
```bash
git clone <repository-url>
cd unify-core
```

### 2. 环境配置
创建 `local.properties` 文件：
```properties
sdk.dir=/path/to/android-sdk
```

### 3. 构建项目
```bash
# 构建共享模块
./gradlew shared:build

# 构建 Android 应用
./gradlew androidApp:build

# 构建 Web 应用
./gradlew webApp:build
```

### 4. 运行应用

#### Android
```bash
./gradlew androidApp:installDebug
```

#### Web
```bash
./gradlew webApp:jsBrowserDevelopmentRun
```

#### iOS
在 Xcode 中打开 `iosApp/iosApp.xcodeproj` 并运行

## 项目结构

```
unify-core/
├── shared/                 # 共享模块
│   ├── src/
│   │   ├── commonMain/     # 通用代码
│   │   ├── androidMain/    # Android 特定代码
│   │   ├── iosMain/        # iOS 特定代码
│   │   ├── jsMain/         # Web 特定代码
│   │   └── jvmMain/        # JVM 特定代码
│   └── build.gradle.kts
├── androidApp/             # Android 应用
├── iosApp/                 # iOS 应用
├── webApp/                 # Web 应用
├── electronApp/            # Electron 应用
└── miniAppBridge/          # 小程序桥接
```

## 核心功能

### 1. 统一组件系统
```kotlin
// 使用统一组件
@Composable
fun MyScreen() {
    UnifyTheme {
        UnifyButton(
            text = "点击我",
            onClick = { /* 处理点击 */ }
        )
    }
}
```

### 2. 状态管理
```kotlin
// 使用统一状态管理
class MyViewModel : UnifyViewModel<MyState, MyIntent, MyEffect>() {
    override fun createInitialState() = MyState()
    
    override fun createReducer() = MyReducer()
    
    override fun createMiddleware() = listOf(LoggingMiddleware())
}
```

### 3. 网络服务
```kotlin
// 使用网络服务
class MyRepository(private val networkService: UnifyNetworkService) {
    suspend fun fetchData(): NetworkResult<MyData> {
        return networkService.get("/api/data")
    }
}
```

### 4. 数据库操作
```kotlin
// 使用数据库
class MyRepository(private val databaseRepository: UnifyDatabaseRepository) {
    suspend fun getUsers(): List<User> {
        return databaseRepository.getAllUsers()
    }
}
```

## 平台特定配置

### Android
在 `androidApp/src/main/kotlin/com/unify/android/MainActivity.kt` 中：
```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidDI.initialize(this)
        setContent {
            UnifyAndroidApp()
        }
    }
}
```

### iOS
在 `iosApp/iosApp/ContentView.swift` 中：
```swift
struct ContentView: View {
    @StateObject private var viewModel = IOSMainViewModel()
    
    var body: some View {
        // iOS UI 实现
    }
}
```

### Web
在 `webApp/src/jsMain/kotlin/Main.kt` 中：
```kotlin
fun main() {
    renderComposable(rootElementId = "root") {
        WebApp()
    }
}
```

## 开发指南

### 添加新功能
1. 在 `shared/src/commonMain` 中定义通用接口
2. 在各平台特定目录中实现平台相关代码
3. 在应用模块中集成新功能

### 测试
```bash
# 运行测试
./gradlew shared:test

# 运行特定平台测试
./gradlew shared:testDebugUnitTest  # Android
./gradlew shared:jsTest             # Web
```

### 调试
- 使用 `LoggingMiddleware` 查看状态变化
- 使用 `ComposePerformanceMonitor` 监控性能
- 查看数据库内容使用 SQLDelight 工具

## 常见问题

### Q: 编译失败怎么办？
A: 检查环境配置，确保 JDK、Android SDK 版本正确

### Q: iOS 构建失败？
A: 确保在 macOS 上运行，Xcode 版本符合要求

### Q: Web 应用无法启动？
A: 检查浏览器控制台错误，确保 JavaScript 模块正确加载

## 更多资源

- [项目设计文档](./开发设计指导.md)
- [实现概况](./PROJECT_IMPLEMENTATION_SUMMARY.md)
- [API 文档](./docs/api/)
- [示例代码](./examples/)

## 贡献指南

1. Fork 项目
2. 创建功能分支
3. 提交更改
4. 创建 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。
