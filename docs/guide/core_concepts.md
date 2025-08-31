# 核心概念

本文档介绍 Unify KMP 框架的核心概念和设计理念。

## 🎯 框架理念

### 一套代码，多端复用
Unify KMP 基于 Kotlin Multiplatform 技术，实现真正的跨平台代码复用：

- **共享业务逻辑**: 核心业务代码在所有平台间共享
- **平台特定优化**: 通过 expect/actual 机制处理平台差异
- **统一开发体验**: 使用相同的语言、工具和框架

### 原生性能保证
- **编译时优化**: 生成各平台的原生代码
- **零运行时开销**: 无需额外的运行时环境
- **直接API调用**: 直接访问平台原生API

## 🏗️ 架构层次

### 1. 共享代码层 (Shared Layer)
```
commonMain/
├── business/          # 业务逻辑
├── data/             # 数据模型
├── network/          # 网络服务
├── ui/               # UI组件
└── utils/            # 工具类
```

### 2. 平台抽象层 (Platform Layer)
```kotlin
// 定义平台接口
expect class PlatformManager {
    fun getPlatformInfo(): String
    fun showNotification(message: String)
}
```

### 3. 平台实现层 (Implementation Layer)
```kotlin
// Android 实现
actual class PlatformManager {
    actual fun getPlatformInfo(): String = "Android"
    actual fun showNotification(message: String) {
        // Android 特定实现
    }
}
```

## 🔧 核心机制

### expect/actual 机制
用于处理平台特定功能：

```kotlin
// 共享代码中声明期望
expect fun getCurrentTime(): Long

// 各平台提供实际实现
actual fun getCurrentTime(): Long = System.currentTimeMillis()
```

### Compose Multiplatform
统一的UI开发框架：

```kotlin
@Composable
fun UniversalButton(
    text: String,
    onClick: () -> Unit
) {
    Button(onClick = onClick) {
        Text(text)
    }
}
```

### 依赖注入
使用 Koin 进行依赖管理：

```kotlin
val appModule = module {
    single<NetworkService> { NetworkServiceImpl() }
    single<DataRepository> { DataRepositoryImpl(get()) }
}
```

## 📱 平台支持

### 移动平台
- **Android**: API 24+ (Android 7.0+)
- **iOS**: iOS 13.0+
- **HarmonyOS**: HarmonyOS 3.0+

### 桌面平台
- **Desktop**: Windows、macOS、Linux
- **Web**: 现代浏览器支持

### 其他平台
- **小程序**: 微信、支付宝等8大平台
- **Watch**: 可穿戴设备
- **TV**: 智能电视

## 🔄 开发流程

### 1. 设计阶段
- 确定共享功能范围
- 识别平台特定需求
- 设计统一的API接口

### 2. 开发阶段
- 实现共享业务逻辑
- 开发平台特定功能
- 创建统一UI组件

### 3. 测试阶段
- 单元测试共享代码
- 集成测试各平台
- UI测试和性能测试

### 4. 部署阶段
- 构建各平台应用
- 发布到应用商店
- 监控和维护

## 🎨 UI 开发模式

### 声明式UI
使用 Compose 的声明式语法：

```kotlin
@Composable
fun UserProfile(user: User) {
    Column {
        Text(user.name)
        Text(user.email)
        Button(
            onClick = { /* 处理点击 */ }
        ) {
            Text("编辑")
        }
    }
}
```

### 状态管理
使用 Compose 的状态管理：

```kotlin
@Composable
fun Counter() {
    var count by remember { mutableIntStateOf(0) }
    
    Button(onClick = { count++ }) {
        Text("Count: $count")
    }
}
```

## 🌐 网络编程

### Ktor 客户端
跨平台网络请求：

```kotlin
val client = HttpClient {
    install(ContentNegotiation) {
        json()
    }
}

suspend fun fetchUser(id: String): User {
    return client.get("/users/$id").body()
}
```

### 序列化
使用 kotlinx.serialization：

```kotlin
@Serializable
data class User(
    val id: String,
    val name: String,
    val email: String
)
```

## 💾 数据持久化

### SQLDelight
跨平台数据库解决方案：

```sql
-- user.sq
CREATE TABLE User (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    email TEXT NOT NULL
);

selectAll:
SELECT * FROM User;

insertUser:
INSERT INTO User(id, name, email)
VALUES (?, ?, ?);
```

### 平台存储
各平台的存储机制：

```kotlin
expect class Storage {
    fun save(key: String, value: String)
    fun load(key: String): String?
}
```

## 🔧 工具链

### 构建工具
- **Gradle**: 项目构建和依赖管理
- **Kotlin Multiplatform Plugin**: KMP支持
- **Compose Plugin**: UI框架支持

### 开发工具
- **IntelliJ IDEA**: 主要IDE
- **Android Studio**: Android开发
- **Xcode**: iOS开发

### 质量工具
- **Detekt**: 代码质量检查
- **KtLint**: 代码格式化
- **Kover**: 测试覆盖率

## 📈 性能优化

### 编译优化
- **代码混淆**: 减小包体积
- **死代码消除**: 移除未使用代码
- **内联优化**: 提升运行性能

### 运行时优化
- **懒加载**: 按需加载资源
- **内存管理**: 避免内存泄漏
- **异步处理**: 使用协程处理耗时操作

## 🧪 测试策略

### 单元测试
测试共享业务逻辑：

```kotlin
@Test
fun testUserValidation() {
    val user = User("1", "John", "john@example.com")
    assertTrue(user.isValid())
}
```

### 集成测试
测试平台特定功能：

```kotlin
@Test
fun testPlatformStorage() {
    val storage = Storage()
    storage.save("key", "value")
    assertEquals("value", storage.load("key"))
}
```

### UI测试
测试用户界面：

```kotlin
@Test
fun testButtonClick() {
    composeTestRule.setContent {
        Counter()
    }
    
    composeTestRule.onNodeWithText("Count: 0").assertExists()
    composeTestRule.onNodeWithText("增加").performClick()
    composeTestRule.onNodeWithText("Count: 1").assertExists()
}
```

## 🚀 最佳实践

### 代码组织
- 按功能模块组织代码
- 保持清晰的依赖关系
- 使用统一的命名规范

### 平台适配
- 最小化平台特定代码
- 提供合理的默认实现
- 优雅处理平台差异

### 性能考虑
- 避免过度抽象
- 合理使用缓存
- 监控关键性能指标

---

通过理解这些核心概念，您可以更好地使用 Unify KMP 框架开发高质量的跨平台应用。
