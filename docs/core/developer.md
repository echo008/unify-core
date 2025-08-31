# Unify-Core 开发者指南

## 快速开始

### 环境要求

- **Kotlin**: 2.1.0+
- **Gradle**: 8.0+
- **JDK**: 17+
- **Android Studio**: 2024.1.1+
- **Xcode**: 15.0+ (iOS开发)
- **Node.js**: 18.0+ (Web开发)

### 项目设置

#### 1. 克隆项目

```bash
git clone https://github.com/your-org/unify-core.git
cd unify-core
```

#### 2. 配置开发环境

```bash
# 安装依赖
./gradlew build

# 运行测试
./gradlew test

# 生成文档
./gradlew dokkaHtml
```

#### 3. 平台特定设置

**Android开发**
```bash
# 构建Android应用
./gradlew :androidApp:assembleDebug

# 运行Android测试
./gradlew :androidApp:connectedAndroidTest
```

**iOS开发**
```bash
# 构建iOS应用 (需要macOS)
./gradlew :iosApp:iosSimulatorArm64Test

# 打开Xcode项目
open iosApp/iosApp.xcodeproj
```

**Web开发**
```bash
# 构建Web应用
./gradlew :webApp:jsBrowserDevelopmentRun

# 生产构建
./gradlew :webApp:jsBrowserDistribution
```

**Desktop开发**
```bash
# 运行Desktop应用
./gradlew :desktopApp:run

# 打包Desktop应用
./gradlew :desktopApp:createDistributable
```

## 核心概念

### 1. 跨平台架构

Unify-Core采用expect/actual机制实现跨平台：

```kotlin
// commonMain - 通用接口定义
expect class PlatformSpecific {
    fun getPlatformName(): String
}

// androidMain - Android实现
actual class PlatformSpecific {
    actual fun getPlatformName(): String = "Android"
}

// iosMain - iOS实现
actual class PlatformSpecific {
    actual fun getPlatformName(): String = "iOS"
}
```

### 2. 动态化系统

动态组件开发模式：

```kotlin
// 1. 定义组件接口
interface DynamicComponent {
    val name: String
    val version: String
    fun render(): @Composable () -> Unit
}

// 2. 实现动态组件
class MyDynamicComponent : DynamicComponent {
    override val name = "MyComponent"
    override val version = "1.0.0"
    
    override fun render(): @Composable () -> Unit = {
        Column {
            Text("动态组件内容")
            Button(onClick = { /* 处理点击 */ }) {
                Text("动态按钮")
            }
        }
    }
}

// 3. 注册和使用
dynamicEngine.registerComponent(MyDynamicComponent())
val component = dynamicEngine.getComponent("MyComponent")
```

### 3. 性能监控集成

在代码中集成性能监控：

```kotlin
class MyRepository {
    suspend fun fetchData(): List<Data> {
        return UnifyPerformanceMonitor.measureOperation("fetch_data") {
            // 实际的数据获取逻辑
            apiService.getData()
        }
    }
}

@Composable
fun MyScreen() {
    LaunchedEffect(Unit) {
        UnifyPerformanceMonitor.trackScreenView("MyScreen")
    }
    
    // UI内容
}
```

## 开发工作流

### 1. 功能开发流程

```
需求分析 → 设计API → 编写测试 → 实现功能 → 代码审查 → 集成测试 → 性能测试 → 文档更新
```

### 2. 代码规范

#### Kotlin代码风格

```kotlin
// ✅ 正确的命名和格式
class UnifyComponentManager {
    private val components = mutableMapOf<String, DynamicComponent>()
    
    fun registerComponent(component: DynamicComponent): Boolean {
        return if (components.containsKey(component.name)) {
            false
        } else {
            components[component.name] = component
            true
        }
    }
    
    fun getComponent(name: String): DynamicComponent? {
        return components[name]
    }
}

// ❌ 避免的写法
class componentmanager{
private var comp=HashMap<String,Any>()
fun reg(c:Any):Boolean{
if(comp.get(c.toString())!=null)return false
comp.put(c.toString(),c)
return true}}
```

#### 文档注释规范

```kotlin
/**
 * Unify动态组件管理器
 * 
 * 负责动态组件的注册、获取和生命周期管理
 * 
 * @author Unify团队
 * @since 1.0.0
 */
class UnifyComponentManager {
    
    /**
     * 注册动态组件
     * 
     * @param component 要注册的动态组件
     * @return 注册成功返回true，组件已存在返回false
     * @throws IllegalArgumentException 当组件名称为空时
     */
    fun registerComponent(component: DynamicComponent): Boolean {
        // 实现
    }
}
```

### 3. 测试策略

#### 单元测试

```kotlin
class UnifyComponentManagerTest {
    
    private lateinit var manager: UnifyComponentManager
    
    @BeforeTest
    fun setup() {
        manager = UnifyComponentManager()
    }
    
    @Test
    fun `注册组件应该成功`() {
        val component = createTestComponent("TestComponent")
        
        val result = manager.registerComponent(component)
        
        assertTrue(result)
        assertEquals(component, manager.getComponent("TestComponent"))
    }
    
    @Test
    fun `重复注册组件应该失败`() {
        val component = createTestComponent("TestComponent")
        manager.registerComponent(component)
        
        val result = manager.registerComponent(component)
        
        assertFalse(result)
    }
}
```

#### 集成测试

```kotlin
class DynamicSystemIntegrationTest {
    
    @Test
    fun `完整的组件加载流程`() = runTest {
        // 1. 初始化系统
        val engine = UnifyDynamicEngine()
        engine.initialize()
        
        // 2. 加载组件
        val componentCode = """
            @Composable
            fun TestComponent() {
                Text("集成测试组件")
            }
        """.trimIndent()
        
        val result = engine.loadComponent("TestComponent", componentCode)
        
        // 3. 验证结果
        assertTrue(result.isSuccess)
        assertNotNull(engine.getComponent("TestComponent"))
    }
}
```

#### UI测试

```kotlin
class UnifyButtonTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun `按钮点击应该触发回调`() {
        var clicked = false
        
        composeTestRule.setContent {
            UnifyButton(
                text = "测试按钮",
                onClick = { clicked = true }
            )
        }
        
        composeTestRule.onNodeWithText("测试按钮").performClick()
        
        assertTrue(clicked)
    }
}
```

## 平台特定开发

### Android平台

#### 权限处理

```kotlin
// androidMain/kotlin/com/unify/platform/AndroidPlatformManager.kt
actual class PlatformManager {
    actual fun requestPermission(permission: String): Boolean {
        return when (permission) {
            "camera" -> requestCameraPermission()
            "location" -> requestLocationPermission()
            else -> false
        }
    }
    
    private fun requestCameraPermission(): Boolean {
        val context = getCurrentActivity()
        return ContextCompat.checkSelfPermission(
            context, 
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
}
```

#### 生命周期集成

```kotlin
class AndroidLifecycleObserver : DefaultLifecycleObserver {
    override fun onStart(owner: LifecycleOwner) {
        UnifyPerformanceMonitor.onAppStart()
    }
    
    override fun onStop(owner: LifecycleOwner) {
        UnifyPerformanceMonitor.onAppStop()
    }
}
```

### iOS平台

#### 原生功能集成

```kotlin
// iosMain/kotlin/com/unify/platform/IOSPlatformManager.kt
actual class PlatformManager {
    actual fun getBiometricInfo(): BiometricInfo {
        return BiometricInfo(
            isAvailable = LAContext().canEvaluatePolicy(
                LAPolicy.LAPolicyDeviceOwnerAuthenticationWithBiometrics, 
                error = null
            ),
            type = getBiometricType()
        )
    }
}
```

### Web平台

#### PWA支持

```kotlin
// jsMain/kotlin/com/unify/platform/WebPlatformManager.kt
actual class PlatformManager {
    actual fun installPWA(): Boolean {
        return js("""
            if ('serviceWorker' in navigator) {
                navigator.serviceWorker.register('/sw.js');
                return true;
            }
            return false;
        """) as Boolean
    }
}
```

### Desktop平台

#### 系统集成

```kotlin
// desktopMain/kotlin/com/unify/platform/DesktopPlatformManager.kt
actual class PlatformManager {
    actual fun showSystemNotification(title: String, message: String) {
        if (SystemTray.isSupported()) {
            val tray = SystemTray.getSystemTray()
            val image = Toolkit.getDefaultToolkit().getImage("icon.png")
            val trayIcon = TrayIcon(image, "Unify App")
            
            trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO)
        }
    }
}
```

## 性能优化指南

### 1. 内存管理

```kotlin
// 使用对象池减少GC压力
class ComponentPool {
    private val pool = mutableListOf<DynamicComponent>()
    
    fun acquire(): DynamicComponent {
        return if (pool.isNotEmpty()) {
            pool.removeAt(pool.size - 1)
        } else {
            createNewComponent()
        }
    }
    
    fun release(component: DynamicComponent) {
        component.reset()
        pool.add(component)
    }
}
```

### 2. 渲染优化

```kotlin
// 使用remember避免不必要的重组
@Composable
fun OptimizedComponent(data: List<String>) {
    val processedData = remember(data) {
        data.map { it.uppercase() }
    }
    
    LazyColumn {
        items(processedData) { item ->
            Text(item)
        }
    }
}
```

### 3. 网络优化

```kotlin
class OptimizedNetworkClient {
    private val cache = LRUCache<String, Response>(100)
    
    suspend fun request(url: String): Response {
        // 检查缓存
        cache[url]?.let { return it }
        
        // 发起请求
        val response = httpClient.get(url)
        
        // 缓存结果
        cache[url] = response
        return response
    }
}
```

## 安全开发指南

### 1. 输入验证

```kotlin
class SecureInputValidator {
    fun validateUserInput(input: String): ValidationResult {
        return when {
            input.isEmpty() -> ValidationResult.Error("输入不能为空")
            input.length > 1000 -> ValidationResult.Error("输入过长")
            containsSqlInjection(input) -> ValidationResult.Error("包含非法字符")
            else -> ValidationResult.Success(input)
        }
    }
    
    private fun containsSqlInjection(input: String): Boolean {
        val sqlKeywords = listOf("DROP", "DELETE", "INSERT", "UPDATE", "SELECT")
        return sqlKeywords.any { input.uppercase().contains(it) }
    }
}
```

### 2. 数据加密

```kotlin
class SecureDataManager {
    private val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    
    fun encryptSensitiveData(data: String, key: SecretKey): String {
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encryptedData = cipher.doFinal(data.toByteArray())
        return Base64.encodeToString(encryptedData, Base64.DEFAULT)
    }
    
    fun decryptSensitiveData(encryptedData: String, key: SecretKey): String {
        cipher.init(Cipher.DECRYPT_MODE, key)
        val decodedData = Base64.decode(encryptedData, Base64.DEFAULT)
        val decryptedData = cipher.doFinal(decodedData)
        return String(decryptedData)
    }
}
```

## 调试和故障排除

### 1. 日志系统

```kotlin
object UnifyLogger {
    private const val TAG = "Unify"
    
    fun debug(message: String, tag: String = TAG) {
        if (BuildConfig.DEBUG) {
            println("[$tag] DEBUG: $message")
        }
    }
    
    fun error(message: String, throwable: Throwable? = null, tag: String = TAG) {
        println("[$tag] ERROR: $message")
        throwable?.printStackTrace()
    }
    
    fun performance(operation: String, duration: Long, tag: String = TAG) {
        println("[$tag] PERF: $operation took ${duration}ms")
    }
}
```

### 2. 常见问题解决

#### 组件加载失败

```kotlin
// 问题诊断
fun diagnoseComponentLoadFailure(componentName: String, error: Throwable) {
    UnifyLogger.error("组件加载失败: $componentName", error)
    
    when (error) {
        is ClassNotFoundException -> {
            UnifyLogger.debug("检查组件依赖是否正确导入")
        }
        is SecurityException -> {
            UnifyLogger.debug("检查组件安全权限配置")
        }
        is OutOfMemoryError -> {
            UnifyLogger.debug("检查内存使用情况，考虑释放不必要的资源")
        }
    }
}
```

#### 性能问题排查

```kotlin
class PerformanceDiagnostic {
    fun analyzePerformanceIssue() {
        val metrics = UnifyPerformanceMonitor.getCurrentMetrics()
        
        when {
            metrics.memoryUsage > 80 -> {
                UnifyLogger.debug("内存使用过高，建议进行内存清理")
                suggestMemoryOptimization()
            }
            metrics.frameRate < 30 -> {
                UnifyLogger.debug("帧率过低，检查UI渲染逻辑")
                suggestRenderingOptimization()
            }
            metrics.networkLatency > 1000 -> {
                UnifyLogger.debug("网络延迟过高，检查网络配置")
                suggestNetworkOptimization()
            }
        }
    }
}
```

## 部署和发布

### 1. 构建配置

```kotlin
// build.gradle.kts
kotlin {
    android {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "UnifyCore"
            isStatic = true
        }
    }
    
    js(IR) {
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
        }
    }
    
    jvm("desktop")
}
```

### 2. 发布流程

```bash
# 1. 运行所有测试
./gradlew test

# 2. 代码质量检查
./gradlew detekt

# 3. 生成文档
./gradlew dokkaHtml

# 4. 构建所有平台
./gradlew build

# 5. 发布到仓库
./gradlew publish
```

### 3. 版本管理

```kotlin
// version.gradle.kts
object Versions {
    const val unifyCore = "1.0.0"
    const val kotlin = "2.1.0"
    const val compose = "1.7.5"
    
    fun isStableVersion(): Boolean {
        return !unifyCore.contains("alpha") && 
               !unifyCore.contains("beta") && 
               !unifyCore.contains("rc")
    }
}
```

## 贡献指南

### 1. 提交规范

```
feat: 添加新功能
fix: 修复bug
docs: 文档更新
style: 代码格式调整
refactor: 代码重构
test: 测试相关
chore: 构建过程或辅助工具的变动

示例:
feat(dynamic): 添加AI组件推荐功能
fix(security): 修复加密算法安全漏洞
docs(api): 更新API文档示例
```

### 2. Pull Request流程

1. Fork项目仓库
2. 创建功能分支: `git checkout -b feature/new-feature`
3. 提交更改: `git commit -m 'feat: 添加新功能'`
4. 推送分支: `git push origin feature/new-feature`
5. 创建Pull Request

### 3. 代码审查清单

- [ ] 代码符合项目规范
- [ ] 包含充分的测试用例
- [ ] 文档已更新
- [ ] 性能影响已评估
- [ ] 安全性已考虑
- [ ] 跨平台兼容性已验证

## 社区和支持

### 获取帮助

- **GitHub Issues**: 报告bug和功能请求
- **讨论区**: 技术讨论和经验分享
- **文档**: 查阅完整的API文档和指南
- **示例项目**: 参考实际应用案例

### 参与贡献

- 提交bug报告
- 建议新功能
- 改进文档
- 分享使用经验
- 参与代码审查

---

**文档版本**: v1.0.0  
**最后更新**: 2025年1月27日  
**维护团队**: Unify-Core开发团队
