# Unify-Core API 完整文档

## 📚 目录

- [核心动态引擎 API](#核心动态引擎-api)
- [AI智能引擎 API](#ai智能引擎-api)
- [性能监控系统 API](#性能监控系统-api)
- [代码质量管理 API](#代码质量管理-api)
- [安全管理系统 API](#安全管理系统-api)
- [UI组件 API](#ui组件-api)
- [平台适配 API](#平台适配-api)
- [配置管理 API](#配置管理-api)
- [测试框架 API](#测试框架-api)

## 核心动态引擎 API

### UnifyDynamicEngine

动态化引擎核心类，负责组件的动态加载、热更新和生命周期管理。

#### 初始化

```kotlin
suspend fun initialize(config: DynamicEngineConfig)
```

**参数:**
- `config: DynamicEngineConfig` - 引擎配置

**示例:**
```kotlin
val engine = UnifyDynamicEngine()
engine.initialize(DynamicEngineConfig(
    configUrl = "https://api.example.com/config",
    apiKey = "your_api_key",
    publicKey = "your_public_key"
))
```

#### 组件注册

```kotlin
fun registerComponent(name: String, factory: ComponentFactory)
```

**参数:**
- `name: String` - 组件名称
- `factory: ComponentFactory` - 组件工厂

#### 组件加载

```kotlin
suspend fun loadComponent(componentData: ComponentData): Boolean
```

**参数:**
- `componentData: ComponentData` - 组件数据

**返回值:**
- `Boolean` - 加载是否成功

#### 热更新

```kotlin
suspend fun applyHotUpdate(updatePackage: UpdatePackage): Boolean
```

**参数:**
- `updatePackage: UpdatePackage` - 更新包

**返回值:**
- `Boolean` - 更新是否成功

## AI智能引擎 API

### UnifyAIEngine

AI智能引擎，提供组件推荐、代码生成、错误诊断等AI能力。

#### 初始化

```kotlin
suspend fun initialize(config: AIEngineConfig = AIEngineConfig())
```

#### 智能组件推荐

```kotlin
suspend fun recommendComponents(context: ComponentContext): List<ComponentRecommendation>
```

**参数:**
- `context: ComponentContext` - 组件上下文

**返回值:**
- `List<ComponentRecommendation>` - 推荐组件列表

**示例:**
```kotlin
val aiEngine = UnifyAIEngine()
aiEngine.initialize()

val context = ComponentContext(
    currentComponents = listOf("UnifyButton"),
    userIntent = "创建登录表单",
    platformTarget = "Android"
)

val recommendations = aiEngine.recommendComponents(context)
recommendations.forEach { recommendation ->
    println("推荐组件: ${recommendation.componentName}, 置信度: ${recommendation.confidence}")
}
```

#### 智能代码生成

```kotlin
suspend fun generateCode(request: CodeGenerationRequest): CodeGenerationResult
```

**参数:**
- `request: CodeGenerationRequest` - 代码生成请求

**返回值:**
- `CodeGenerationResult` - 生成结果

#### 错误诊断

```kotlin
suspend fun diagnoseError(error: ErrorContext): ErrorDiagnosis
```

### AIConfigurationManager

AI配置管理器，管理AI模型配置和学习数据。

#### 获取配置

```kotlin
fun getConfiguration(modelType: String): AIConfiguration?
```

#### 更新配置

```kotlin
suspend fun updateConfiguration(modelType: String, config: AIConfiguration)
```

#### 自动调优

```kotlin
suspend fun autoTuneConfiguration(modelType: String, metrics: PerformanceMetrics): AIConfiguration
```

## 性能监控系统 API

### UnifyPerformanceMonitor

性能监控核心类，提供实时性能监控和优化建议。

#### 初始化

```kotlin
fun initialize()
```

#### 记录性能指标

```kotlin
fun recordMetric(name: String, value: Double, unit: String = "", tags: Map<String, String> = emptyMap())
```

**参数:**
- `name: String` - 指标名称
- `value: Double` - 指标值
- `unit: String` - 单位
- `tags: Map<String, String>` - 标签

**示例:**
```kotlin
UnifyPerformanceMonitor.initialize()
UnifyPerformanceMonitor.recordMetric("api_response_time", 150.0, "ms", mapOf("endpoint" to "/api/users"))
```

#### 开始性能测量

```kotlin
fun startMeasurement(name: String): PerformanceMeasurement
```

#### 记录帧率

```kotlin
fun recordFrameTime(frameTime: Long)
```

#### 记录内存使用

```kotlin
fun recordMemoryUsage(bytes: Long)
```

#### 获取性能摘要

```kotlin
fun getPerformanceSummary(): PerformanceSummary
```

### UnifyPerformanceAlerting

性能告警系统。

#### 设置阈值

```kotlin
fun setThreshold(metricName: String, threshold: AlertThreshold)
```

#### 检查指标

```kotlin
fun checkMetric(metric: PerformanceMetric)
```

### UnifyPerformanceAnalyzer

智能性能分析器。

#### 分析性能趋势

```kotlin
fun analyzePerformanceTrends(metrics: Map<String, PerformanceMetric>): PerformanceAnalysis
```

## 代码质量管理 API

### UnifyCodeQualityManager

代码质量管理器，提供代码质量检查和改进建议。

#### 初始化

```kotlin
fun initialize()
```

#### 分析代码质量

```kotlin
fun analyzeCodeQuality(codeFiles: List<CodeFile>): QualityReport
```

**参数:**
- `codeFiles: List<CodeFile>` - 代码文件列表

**返回值:**
- `QualityReport` - 质量报告

**示例:**
```kotlin
val qualityManager = UnifyCodeQualityManager()
qualityManager.initialize()

val codeFiles = listOf(
    CodeFile("MainActivity.kt", "/src/main/MainActivity.kt", kotlinCode, "kotlin")
)

val report = qualityManager.analyzeCodeQuality(codeFiles)
println("整体评分: ${report.overallScore}")
report.suggestions.forEach { suggestion ->
    println("建议: ${suggestion.description}")
}
```

## 安全管理系统 API

### UnifySecurityManager

安全管理器，提供身份验证、授权、加密和威胁检测。

#### 初始化

```kotlin
suspend fun initialize(config: SecurityConfig = SecurityConfig())
```

#### 用户认证

```kotlin
suspend fun authenticateUser(credentials: UserCredentials): AuthenticationResult
```

**参数:**
- `credentials: UserCredentials` - 用户凭据

**返回值:**
- `AuthenticationResult` - 认证结果

**示例:**
```kotlin
val securityManager = UnifySecurityManager()
securityManager.initialize()

val credentials = UserCredentials("username", "password")
val result = securityManager.authenticateUser(credentials)

if (result.success) {
    println("认证成功，令牌: ${result.token}")
} else {
    println("认证失败: ${result.message}")
}
```

#### 授权检查

```kotlin
fun authorize(token: String, resource: String, action: String): AuthorizationResult
```

#### 数据加密

```kotlin
fun encryptData(data: String, keyId: String = "default"): EncryptionResult
```

#### 数据解密

```kotlin
fun decryptData(encryptedData: String, keyId: String = "default"): DecryptionResult
```

#### 输入验证

```kotlin
fun validateAndSanitizeInput(input: String, type: InputType): InputValidationResult
```

#### 威胁扫描

```kotlin
suspend fun scanForThreats(): ThreatScanResult
```

#### 生成安全报告

```kotlin
fun generateSecurityReport(): SecurityReport
```

## 测试框架 API

### UnifyComprehensiveTestSuite

综合测试套件，提供全面的测试覆盖。

#### 核心组件测试

```kotlin
@Test
fun testUnifyButtonBasicFunctionality()
```

#### 动态化系统测试

```kotlin
@Test
fun testDynamicComponentLoading()
```

#### 性能测试

```kotlin
@Test
fun testPerformanceMonitoring()
```

#### AI功能测试

```kotlin
@Test
fun testAIComponentRecommendation()
```

#### 安全性测试

```kotlin
@Test
fun testSecurityValidation()
```

### 测试工具类

#### MockTextField

模拟文本输入框。

```kotlin
class MockTextField {
    fun setValue(text: String)
    fun setMaxLength(length: Int)
    fun isValid(): Boolean
}
```

#### MockDynamicEngine

模拟动态引擎。

```kotlin
class MockDynamicEngine {
    suspend fun loadComponent(data: MockComponentData): Boolean
    suspend fun applyHotUpdate(update: MockUpdatePackage): Boolean
    suspend fun rollback(): Boolean
}
```

## 最佳实践

### 1. 性能优化

```kotlin
// 使用性能监控
UnifyPerformanceMonitor.initialize()
val measurement = UnifyPerformanceMonitor.startMeasurement("api_call")
// 执行操作
measurement.end()

// 使用Compose优化
@Composable
fun OptimizedComponent() {
    UnifyComposeOptimizer.PerformanceTracker("MyComponent") {
        // 组件内容
    }
}
```

### 2. 安全最佳实践

```kotlin
// 输入验证
val validationResult = securityManager.validateAndSanitizeInput(userInput, InputType.HTML)
if (validationResult.valid) {
    // 使用清理后的输入
    processInput(validationResult.sanitizedInput!!)
}

// 数据加密
val encryptionResult = securityManager.encryptData(sensitiveData)
if (encryptionResult.success) {
    // 存储加密数据
    store(encryptionResult.encryptedData!!)
}
```

### 3. AI功能集成

```kotlin
// 智能组件推荐
val context = ComponentContext(
    currentComponents = getCurrentComponents(),
    userIntent = getUserIntent(),
    platformTarget = getPlatform()
)

val recommendations = aiEngine.recommendComponents(context)
val bestRecommendation = recommendations.maxByOrNull { it.confidence }
```

### 4. 代码质量保证

```kotlin
// 代码质量检查
val codeFiles = getProjectFiles()
val qualityReport = qualityManager.analyzeCodeQuality(codeFiles)

if (qualityReport.overallScore < 8.0f) {
    qualityReport.suggestions.forEach { suggestion ->
        when (suggestion.priority) {
            SuggestionPriority.CRITICAL, SuggestionPriority.HIGH -> {
                // 立即处理高优先级建议
                handleSuggestion(suggestion)
            }
            else -> {
                // 记录其他建议
                logSuggestion(suggestion)
            }
        }
    }
}
```

## 错误处理

### 常见错误码

- `DYNAMIC_ENGINE_001`: 组件加载失败
- `AI_ENGINE_002`: AI模型未初始化
- `SECURITY_003`: 认证失败
- `PERFORMANCE_004`: 性能阈值超限
- `QUALITY_005`: 代码质量不达标

### 错误处理示例

```kotlin
try {
    val result = dynamicEngine.loadComponent(componentData)
    if (!result) {
        throw DynamicEngineException("DYNAMIC_ENGINE_001", "组件加载失败")
    }
} catch (e: DynamicEngineException) {
    logger.error("动态引擎错误: ${e.code} - ${e.message}")
    // 错误恢复逻辑
}
```

## 版本兼容性

| API版本 | Unify-Core版本 | 兼容性 |
|---------|---------------|--------|
| 1.0.x   | 1.0.0+        | ✅ 完全兼容 |
| 1.1.x   | 1.1.0+        | ✅ 向后兼容 |
| 2.0.x   | 2.0.0+        | ⚠️ 部分兼容 |

## 支持和反馈

- 📧 邮箱: support@unify-core.dev
- 🐛 问题反馈: https://github.com/unify-core/issues
- 📖 文档: https://docs.unify-core.dev
- 💬 社区: https://community.unify-core.dev

```kotlin
suspend fun loadComponent(componentData: ComponentData): Boolean
suspend fun loadComponents(componentsData: List<ComponentData>): BatchLoadResult
```

**返回值:**
- `Boolean` - 单个组件加载是否成功
- `BatchLoadResult` - 批量加载结果

#### 预加载组件

```kotlin
suspend fun preloadComponents(componentIds: List<String>)
```

#### 智能推荐

```kotlin
suspend fun getRecommendedComponents(context: Map<String, Any>): List<ComponentRecommendation>
```

**参数:**
- `context: Map<String, Any>` - 推荐上下文

**返回值:**
- `List<ComponentRecommendation>` - 推荐组件列表

#### 热更新

```kotlin
suspend fun applyHotUpdate(updateInfo: HotUpdateInfo): Boolean
suspend fun applyHotUpdate(updatePackage: UpdatePackage): Boolean
```

### 数据类

#### DynamicEngineConfig
```kotlin
data class DynamicEngineConfig(
    val configUrl: String = "",
    val apiKey: String = "",
    val publicKey: String = "",
    val enableSecurity: Boolean = true,
    val enableMetrics: Boolean = true
)
```

#### ComponentData
```kotlin
data class ComponentData(
    val metadata: ComponentMetadata,
    val code: String,
    val dependencies: List<String> = emptyList(),
    val resources: Map<String, ByteArray> = emptyMap()
)
```

#### BatchLoadResult
```kotlin
data class BatchLoadResult(
    val totalComponents: Int,
    val successfulComponents: Int,
    val failedComponents: Int,
    val results: Map<String, Boolean>,
    val duration: Long
)
```

## AI组件 API

### AIModelType

支持的AI模型类型枚举。

```kotlin
enum class AIModelType {
    TEXT_GENERATION,
    IMAGE_GENERATION,
    CODE_GENERATION,
    SENTIMENT_ANALYSIS,
    CONTENT_MODERATION,
    QUESTION_ANSWERING,
    TEXT_SUMMARIZATION,
    TRANSLATION,
    SPEECH_TO_TEXT,
    TEXT_TO_SPEECH
}
```

### AIChatConfig

AI聊天配置类。

```kotlin
data class AIChatConfig(
    val modelType: AIModelType = AIModelType.TEXT_GENERATION,
    val maxTokens: Int = 2048,
    val temperature: Float = 0.7f,
    val topP: Float = 1.0f,
    val frequencyPenalty: Float = 0.0f,
    val presencePenalty: Float = 0.0f,
    val systemPrompt: String = "",
    val contextWindow: Int = 4096,
    val enableMemory: Boolean = false,
    val memorySize: Int = 0,
    val responseFormat: String = "text"
)
```

#### 参数验证规则

- `temperature`: 0.0 - 2.0
- `topP`: 0.0 - 1.0  
- `frequencyPenalty`: -2.0 - 2.0
- `presencePenalty`: -2.0 - 2.0
- `maxTokens`: 1 - 100000
- `contextWindow`: 1024, 2048, 4096, 8192, 16384, 32768
- `responseFormat`: "text", "json", "markdown", "html", "code", "image_url", "audio"

#### 使用示例

```kotlin
// 文本生成配置
val textConfig = AIChatConfig(
    modelType = AIModelType.TEXT_GENERATION,
    maxTokens = 1000,
    temperature = 0.8f,
    systemPrompt = "You are a helpful assistant"
)

// 代码生成配置
val codeConfig = AIChatConfig(
    modelType = AIModelType.CODE_GENERATION,
    temperature = 0.2f,
    responseFormat = "code",
    systemPrompt = "Generate clean, efficient code"
)
```

## UI组件 API

### 基础组件状态

#### UnifyButtonState
```kotlin
data class UnifyButtonState(
    val text: String = "",
    val isLoading: Boolean = false,
    val isEnabled: Boolean = true,
    val isPressed: Boolean = false,
    val leadingIcon: String? = null,
    val trailingIcon: String? = null,
    val contentDescription: String? = null
)
```

#### UnifyTextFieldState
```kotlin
data class UnifyTextFieldState(
    val value: String = "",
    val label: String = "",
    val placeholder: String = "",
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val validator: ((String) -> ValidationResult)? = null
)
```

### 响应式设计

#### Breakpoint
```kotlin
enum class Breakpoint {
    Compact,    // < 600dp
    Medium,     // 600dp - 840dp
    Expanded    // > 840dp
}
```

#### 响应式间距
```kotlin
fun getResponsiveSpacing(breakpoint: Breakpoint): ResponsiveSpacing
```

#### 响应式字体
```kotlin
fun getResponsiveTypography(breakpoint: Breakpoint): ResponsiveTypography
```

### 无障碍支持

#### SemanticRole
```kotlin
enum class SemanticRole {
    Button,
    TextField,
    Image,
    List
}
```

## 性能监控 API

### UnifyPerformanceMonitor

性能监控核心类。

#### 初始化
```kotlin
fun initialize()
```

#### 指标记录
```kotlin
fun recordMetric(name: String, value: Double, unit: String, tags: Map<String, String> = emptyMap())
```

#### 性能测量
```kotlin
fun startMeasurement(operationName: String): PerformanceMeasurement
```

#### 性能优化
```kotlin
fun optimizeStartupPerformance()
fun optimizeRuntimePerformance()
fun intelligentPerformanceTuning()
```

#### 获取指标
```kotlin
val metrics: StateFlow<Map<String, PerformanceMetric>>
```

### PerformanceMeasurement

性能测量类。

```kotlin
class PerformanceMeasurement(private val operationName: String) {
    fun end(): Long // 返回持续时间(ms)
}
```

## 内存管理 API

### UnifyMemoryManager

内存管理核心类。

#### 初始化
```kotlin
fun initialize()
```

#### 内存优化
```kotlin
fun optimizeMemory()
```

#### 对象池管理
```kotlin
fun <T> getObjectPool(name: String): ObjectPool<T>
fun <T> createObjectPool(name: String, factory: () -> T, maxSize: Int = 50): ObjectPool<T>
```

#### 缓存管理
```kotlin
fun <K, V> createCache(name: String, maxSize: Int, ttl: Long): Cache<K, V>
```

#### 内存泄漏检测
```kotlin
fun detectMemoryLeaks(): List<MemoryLeak>
```

#### 内存压力测试
```kotlin
fun performMemoryStressTest(): MemoryStressTestResult
```

### ObjectPool

对象池接口。

```kotlin
interface ObjectPool<T> {
    fun borrow(): T
    fun return(obj: T)
    fun getStats(): ObjectPoolStats
}
```

### Cache

缓存接口。

```kotlin
interface Cache<K, V> {
    fun put(key: K, value: V)
    fun get(key: K): V?
    fun remove(key: K): V?
    fun clear()
    fun getStats(): CacheStats
}
```

## 安全验证 API

### HotUpdateSecurityValidator

安全验证器。

#### 输入清理
```kotlin
fun sanitizeInput(input: String): String
```

#### HTML转义
```kotlin
fun escapeHtml(html: String): String
```

#### CSRF令牌
```kotlin
fun generateCSRFToken(): String
fun validateCSRFToken(token: String): Boolean
```

#### 权限验证
```kotlin
fun validatePrivilegeEscalation(currentUser: Map<String, Any>, targetUser: Map<String, Any>): Boolean
```

#### 数字签名验证
```kotlin
fun validateSignature(data: ByteArray, signature: String, publicKey: String): Boolean
```

## 平台适配 API

### PlatformManager

平台管理器接口。

```kotlin
expect object PlatformManager {
    fun getPlatformName(): String
    fun getDeviceInfo(): DeviceInfo
    fun isFeatureSupported(feature: String): Boolean
    suspend fun requestPermission(permission: String): Boolean
}
```

### 平台特定实现

#### Android
```kotlin
actual object PlatformManager {
    actual fun getPlatformName(): String = "Android"
    actual fun getDeviceInfo(): DeviceInfo = AndroidDeviceInfo()
    // ... 其他实现
}
```

#### iOS
```kotlin
actual object PlatformManager {
    actual fun getPlatformName(): String = "iOS"
    actual fun getDeviceInfo(): DeviceInfo = IOSDeviceInfo()
    // ... 其他实现
}
```

## 配置管理 API

### DynamicConfigurationManager

动态配置管理器。

#### 配置更新
```kotlin
suspend fun updateConfiguration(key: String, value: Any)
```

#### 配置获取
```kotlin
suspend fun getConfiguration(key: String): Any?
```

#### 配置监听
```kotlin
fun observeConfiguration(key: String): Flow<Any?>
```

#### 批量配置
```kotlin
suspend fun updateConfigurations(configurations: Map<String, Any>)
```

## 错误处理

### 异常类型

#### DynamicEngineException
```kotlin
class DynamicEngineException(message: String, cause: Throwable? = null) : Exception(message, cause)
```

#### ComponentLoadException
```kotlin
class ComponentLoadException(componentName: String, cause: Throwable? = null) : 
    DynamicEngineException("Failed to load component: $componentName", cause)
```

#### SecurityValidationException
```kotlin
class SecurityValidationException(message: String) : Exception(message)
```

### 错误码

| 错误码 | 描述 | 处理建议 |
|--------|------|----------|
| 1001 | 组件加载失败 | 检查组件数据完整性 |
| 1002 | 安全验证失败 | 验证签名和权限 |
| 1003 | 网络连接失败 | 检查网络状态 |
| 1004 | 存储空间不足 | 清理缓存或扩容 |
| 1005 | 权限不足 | 申请必要权限 |

## 最佳实践

### 1. 组件开发
```kotlin
// 推荐的组件工厂实现
class MyComponentFactory : ComponentFactory {
    override fun createComponent(properties: Map<String, Any>): Any {
        return MyComponent(properties).apply {
            // 初始化逻辑
        }
    }
    
    override fun getComponentType(): String = "MyComponent"
    
    override fun getSupportedProperties(): List<String> = listOf("title", "content")
}
```

### 2. 性能监控
```kotlin
// 推荐的性能监控使用
val measurement = UnifyPerformanceMonitor.startMeasurement("component_load")
try {
    // 执行操作
    loadComponent(componentData)
} finally {
    val duration = measurement.end()
    UnifyPerformanceMonitor.recordMetric("component_load_success", 1.0, "count")
}
```

### 3. 内存管理
```kotlin
// 推荐的对象池使用
val stringBuilderPool = UnifyMemoryManager.getObjectPool<StringBuilder>("StringBuilder")
val sb = stringBuilderPool.borrow()
try {
    // 使用StringBuilder
    sb.append("Hello World")
    return sb.toString()
} finally {
    sb.clear()
    stringBuilderPool.return(sb)
}
```

### 4. 安全验证
```kotlin
// 推荐的安全验证流程
fun processUserInput(input: String): String {
    // 1. 输入清理
    val sanitized = securityValidator.sanitizeInput(input)
    
    // 2. HTML转义
    val escaped = securityValidator.escapeHtml(sanitized)
    
    // 3. 长度验证
    if (escaped.length > MAX_INPUT_LENGTH) {
        throw SecurityValidationException("Input too long")
    }
    
    return escaped
}
```

## 版本兼容性

| API版本 | Unify-Core版本 | 兼容性 |
|---------|----------------|--------|
| v1.0 | 1.0.x | ✅ 完全兼容 |
| v1.1 | 1.1.x | ✅ 向后兼容 |
| v1.2 | 1.2.x | ✅ 向后兼容 |
| v1.3 | 1.3.x | ✅ 当前版本 |

## 迁移指南

### 从v1.2升级到v1.3

1. **API变更**
   - `UnifyPerformanceMonitor.recordMetric()` 新增可选参数 `tags`
   - `AIChatConfig` 新增 `responseFormat` 参数

2. **新增功能**
   - 智能组件推荐
   - 批量组件加载
   - 内存压力测试

3. **废弃API**
   - 无废弃API，完全向后兼容

## 支持与反馈

- **文档更新**: 本文档随代码同步更新
- **API变更**: 遵循语义化版本控制
- **技术支持**: 详见项目README.md
