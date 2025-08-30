# Unify KMP 架构设计文档

## 概述

Unify KMP 是一个基于 Kotlin Multiplatform 和 Jetpack Compose 的跨平台开发框架，采用纯 Compose 语法实现"一套代码，多端复用"的开发理念。

## 核心原则

### 1. 纯 Compose 语法
- **禁止 DSL 转换**：完全摒弃自研 DSL，使用标准 Jetpack Compose 语法
- **声明式 UI**：所有 UI 组件使用 `@Composable` 函数实现
- **类型安全**：利用 Kotlin 类型系统确保编译时安全

### 2. 一套代码多端复用
- **目标复用率**：>85% 代码复用
- **平台抽象**：使用 expect/actual 机制处理平台差异
- **统一 API**：提供一致的跨平台接口

### 3. 原生性能
- **零运行时开销**：编译时优化，无额外抽象层
- **平台原生**：生成各平台原生二进制文件
- **性能监控**：内置性能基准测试和监控

## 架构分层

```
┌─────────────────────────────────────────────────────────────┐
│                        应用层 (App Layer)                    │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────┐ │
│  │ AndroidApp  │ │   iOSApp    │ │   WebApp    │ │ Desktop │ │
│  └─────────────┘ └─────────────┘ └─────────────┘ └─────────┘ │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                        UI层 (UI Layer)                      │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │              Unify UI Components                        │ │
│  │  UnifyButton │ UnifyCard │ UnifyTextField │ ...         │ │
│  └─────────────────────────────────────────────────────────┘ │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │                   Compose Theme                         │ │
│  │  Colors │ Typography │ Shapes │ Material3              │ │
│  └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                   业务逻辑层 (Business Layer)                │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │                    MVI Architecture                     │ │
│  │  StateManager │ Intent │ State │ Effect                │ │
│  └─────────────────────────────────────────────────────────┘ │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │                  Core Services                          │ │
│  │  Network │ Storage │ Performance │ Platform             │ │
│  └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                  平台抽象层 (Platform Layer)                 │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │                  expect declarations                    │ │
│  │  PlatformManager │ NetworkService │ Storage             │ │
│  └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                  平台实现层 (Platform Impl)                  │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────┐ │
│  │   Android   │ │     iOS     │ │     Web     │ │ Desktop │ │
│  │   actual    │ │   actual    │ │   actual    │ │ actual  │ │
│  └─────────────┘ └─────────────┘ └─────────────┘ └─────────┘ │
└─────────────────────────────────────────────────────────────┘
```

## 核心模块

### 1. UnifyCore 核心框架
```kotlin
// 统一初始化入口
UnifyCore.initialize()

// 应用根组件
@Composable
fun UnifyApp(content: @Composable () -> Unit) {
    // 提供统一主题和平台上下文
}
```

### 2. PlatformManager 平台管理
```kotlin
expect object PlatformManager {
    fun getPlatformName(): String
    fun getScreenInfo(): ScreenInfo
    fun getNetworkStatus(): NetworkStatus
    // ... 更多平台功能
}
```

### 3. UI 组件库
```kotlin
// 统一按钮组件
@Composable
fun UnifyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)

// 统一卡片组件
@Composable
fun UnifyCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
)
```

### 4. MVI 状态管理
```kotlin
// 状态管理器基类
abstract class UnifyStateManager<I, S, E> {
    abstract fun handleIntent(intent: I)
    protected fun updateState(newState: S)
    protected suspend fun sendEffect(effect: E)
}

// Compose 集成
@Composable
fun UnifyMVIContainer(
    stateManager: UnifyStateManager<I, S, E>,
    content: @Composable (state: S, onIntent: (I) -> Unit) -> Unit
)
```

### 5. 网络服务
```kotlin
interface UnifyNetworkService {
    suspend fun <T> get(url: String, deserializer: (String) -> T): NetworkResult<T>
    suspend fun <T> post(url: String, body: String, deserializer: (String) -> T): NetworkResult<T>
    // ... 更多网络功能
}
```

### 6. 存储服务
```kotlin
interface UnifyStorage {
    suspend fun putString(key: String, value: String)
    suspend fun getString(key: String, defaultValue: String = ""): String
    // ... 更多存储功能
}
```

### 7. 性能监控
```kotlin
object UnifyPerformanceMonitor {
    fun startMeasurement(name: String): PerformanceMeasurement
    fun recordMetric(name: String, value: Double, unit: String)
    fun getPerformanceReport(): PerformanceReport
}
```

## 平台支持

### 支持平台矩阵

| 平台 | 支持度 | 技术栈 | 状态 |
|------|--------|--------|------|
| Android | 100% | Jetpack Compose | ✅ 生产就绪 |
| iOS | 95% | Compose Multiplatform | ✅ 生产就绪 |
| Web | 90% | Compose for Web | ✅ 生产就绪 |
| Desktop | 95% | Compose Desktop | ✅ 生产就绪 |
| HarmonyOS | 85% | ArkTS + Bridge | 🚧 开发中 |
| 小程序 | 75% | JS Bridge | 🚧 开发中 |

### 平台特性对比

| 特性 | Android | iOS | Web | Desktop | HarmonyOS | 小程序 |
|------|---------|-----|-----|---------|-----------|---------|
| UI 渲染 | Native | Native | Canvas/DOM | Native | ArkTS | 小程序组件 |
| 网络请求 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| 本地存储 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| 文件系统 | ✅ | ✅ | ❌ | ✅ | ✅ | ❌ |
| 相机/相册 | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ |
| 推送通知 | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ |
| 生物识别 | ✅ | ✅ | ✅ | ❌ | ✅ | ❌ |

## 开发流程

### 1. 环境配置
```bash
# 安装 JDK 17
# 配置 Android SDK
# 安装 Xcode (macOS)
# 配置 Node.js (Web)
```

### 2. 项目初始化
```bash
git clone https://github.com/echo008/unify-core.git
cd unify-core
./gradlew build
```

### 3. 开发新功能
```kotlin
// 1. 在 commonMain 中定义 expect 接口
expect class MyPlatformService {
    fun doSomething(): String
}

// 2. 在各平台实现 actual
// androidMain
actual class MyPlatformService {
    actual fun doSomething(): String = "Android Implementation"
}

// 3. 创建 Compose UI
@Composable
fun MyFeatureScreen() {
    val service = remember { MyPlatformService() }
    UnifyCard {
        Text(service.doSomething())
    }
}
```

### 4. 测试验证
```bash
# 运行所有平台测试
./gradlew test

# 性能基准测试
./scripts/benchmark.sh

# 构建所有平台
./gradlew build
```

## 性能优化

### 1. 编译时优化
- **R8/ProGuard**：Android 代码混淆和优化
- **Kotlin/Native**：iOS 原生编译
- **Webpack**：Web 资源优化
- **GraalVM**：Desktop 原生镜像

### 2. 运行时优化
- **Compose 优化**：避免不必要的重组
- **内存管理**：及时释放资源
- **网络缓存**：智能缓存策略
- **懒加载**：按需加载组件

### 3. 包大小优化
- **代码分割**：按功能模块分割
- **资源优化**：压缩图片和资源
- **依赖精简**：移除未使用的依赖
- **Tree Shaking**：移除死代码

## 最佳实践

### 1. 代码组织
```
shared/src/
├── commonMain/kotlin/com/unify/
│   ├── core/           # 核心框架
│   ├── components/     # UI 组件
│   ├── features/       # 业务功能
│   └── utils/          # 工具类
├── androidMain/kotlin/
├── iosMain/kotlin/
└── jsMain/kotlin/
```

### 2. 命名规范
- **类名**：PascalCase (UnifyButton)
- **函数名**：camelCase (getPlatformName)
- **常量**：UPPER_SNAKE_CASE (MAX_RETRY_COUNT)
- **包名**：小写 + 点分隔 (com.unify.core)

### 3. 错误处理
```kotlin
sealed class UnifyResult<out T> {
    data class Success<T>(val data: T) : UnifyResult<T>()
    data class Error(val exception: Exception) : UnifyResult<Nothing>()
    object Loading : UnifyResult<Nothing>()
}
```

### 4. 测试策略
- **单元测试**：业务逻辑测试
- **UI 测试**：Compose 测试
- **集成测试**：端到端测试
- **性能测试**：基准测试

## 扩展指南

### 1. 添加新平台
1. 在 `shared/build.gradle.kts` 中添加新目标
2. 创建平台特定源集
3. 实现 actual 声明
4. 创建平台应用模块
5. 配置构建脚本

### 2. 添加新组件
1. 在 `commonMain` 中创建 Compose 组件
2. 遵循 Material3 设计规范
3. 添加文档和示例
4. 编写单元测试
5. 更新组件库文档

### 3. 集成第三方库
1. 检查 KMP 兼容性
2. 添加到 `libs.versions.toml`
3. 在相应源集中添加依赖
4. 创建统一抽象接口
5. 实现平台特定适配

## 故障排除

### 常见问题

1. **编译错误**
   - 检查 JDK 版本 (需要 17+)
   - 清理构建缓存：`./gradlew clean`
   - 检查依赖版本兼容性

2. **iOS 构建失败**
   - 确保 Xcode 版本兼容
   - 检查 CocoaPods 配置
   - 验证签名证书

3. **Web 构建问题**
   - 检查 Node.js 版本
   - 清理 npm 缓存
   - 验证 Webpack 配置

4. **性能问题**
   - 使用性能监控工具
   - 检查内存泄漏
   - 优化 Compose 重组

### 调试技巧

1. **日志调试**
```kotlin
// 使用统一日志接口
UnifyLogger.d("Tag", "Debug message")
```

2. **性能分析**
```kotlin
val measurement = UnifyPerformanceMonitor.startMeasurement("feature_load")
// ... 执行代码
measurement.end()
```

3. **网络调试**
```kotlin
// 启用网络日志
NetworkServiceFactory.create().enableLogging(true)
```

## 贡献指南

请参考 [CONTRIBUTING.md](CONTRIBUTING.md) 了解如何为项目做贡献。

## 许可证

本项目采用 MIT 许可证，详见 [LICENSE](LICENSE) 文件。
