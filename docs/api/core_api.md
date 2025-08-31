# Unify-Core API 参考文档

## 概述

Unify-Core 是一个企业级跨平台开发框架，支持 Android、iOS、Web、Desktop、HarmonyOS、小程序、Watch、TV 等8大平台。本文档提供完整的 API 参考。

## 核心模块

### 1. 平台管理模块 (PlatformManager)

#### 初始化和配置

```kotlin
// 初始化平台管理器
PlatformManager.initialize()

// 获取平台类型
val platformType: PlatformType = PlatformManager.getPlatformType()

// 获取平台名称
val platformName: String = PlatformManager.getPlatformName()

// 获取平台版本
val platformVersion: String = PlatformManager.getPlatformVersion()
```

#### 设备信息获取

```kotlin
// 获取设备信息
val deviceInfo: DeviceInfo = PlatformManager.getDeviceInfo()

// DeviceInfo 属性
data class DeviceInfo(
    val deviceId: String,           // 设备唯一标识
    val systemName: String,         // 系统名称
    val systemVersion: String,      // 系统版本
    val manufacturer: String,       // 制造商
    val model: String,             // 设备型号
    val architecture: String       // 系统架构
)
```

#### 屏幕信息

```kotlin
// 获取屏幕信息
val screenInfo: ScreenInfo = PlatformManager.getScreenInfo()

// ScreenInfo 属性
data class ScreenInfo(
    val width: Int,                // 屏幕宽度(像素)
    val height: Int,               // 屏幕高度(像素)
    val density: Float,            // 屏幕密度
    val scaleFactor: Float,        // 缩放因子
    val orientation: String,       // 屏幕方向
    val refreshRate: Float         // 刷新率
)
```

#### 系统能力检测

```kotlin
// 获取系统能力
val capabilities: SystemCapabilities = PlatformManager.getSystemCapabilities()

// SystemCapabilities 属性
data class SystemCapabilities(
    val isTouchSupported: Boolean,      // 触摸支持
    val isKeyboardSupported: Boolean,   // 键盘支持
    val isMouseSupported: Boolean,      // 鼠标支持
    val isCameraSupported: Boolean,     // 摄像头支持
    val isMicrophoneSupported: Boolean, // 麦克风支持
    val isGPSSupported: Boolean,        // GPS支持
    val isBiometricSupported: Boolean,  // 生物识别支持
    val isNFCSupported: Boolean         // NFC支持
)
```

#### 网络状态监控

```kotlin
// 获取网络状态
val networkStatus: NetworkStatus = PlatformManager.getNetworkStatus()

// 开始网络监控
PlatformManager.startNetworkMonitoring { status ->
    println("网络状态变化: ${status.connectionType}")
}

// 停止网络监控
PlatformManager.stopNetworkMonitoring()

// NetworkStatus 属性
data class NetworkStatus(
    val isConnected: Boolean?,      // 是否连接
    val connectionType: String,     // 连接类型
    val signalStrength: Int,        // 信号强度
    val bandwidth: Long            // 带宽
)
```

#### 存储操作

```kotlin
// 获取存储信息
val storageInfo: StorageInfo = PlatformManager.getStorageInfo()

// 文件操作
val success: Boolean = PlatformManager.writeToStorage("filename.txt", "content")
val content: String? = PlatformManager.readFromStorage("filename.txt")
val exists: Boolean = PlatformManager.fileExists("filename.txt")
val deleted: Boolean = PlatformManager.deleteFromStorage("filename.txt")

// StorageInfo 属性
data class StorageInfo(
    val totalSpace: Long,          // 总空间
    val availableSpace: Long,      // 可用空间
    val usedSpace: Long           // 已用空间
)
```

### 2. 动态化引擎 (UnifyDynamicEngine)

#### 初始化和配置

```kotlin
// 创建动态引擎实例
val dynamicEngine = UnifyDynamicEngine()

// 初始化引擎
dynamicEngine.initialize()

// 检查初始化状态
val isInitialized: Boolean = dynamicEngine.isInitialized()
```

#### 组件管理

```kotlin
// 注册组件
val componentInfo = ComponentInfo(
    name = "MyComponent",
    version = "1.0.0",
    description = "自定义组件"
)
dynamicEngine.registerComponent(componentInfo)

// 加载组件
val componentCode = """
    @Composable
    fun MyComponent() {
        Text("Hello Unify!")
    }
""".trimIndent()

val result: LoadResult = dynamicEngine.loadComponent("MyComponent", componentCode)

// 获取组件
val component: DynamicComponent? = dynamicEngine.getComponent("MyComponent")

// 卸载组件
val unloaded: Boolean = dynamicEngine.unloadComponent("MyComponent")
```

#### 热更新功能

```kotlin
// 创建更新包
val updatePackage = UpdatePackage(
    componentName = "MyComponent",
    version = "1.1.0",
    code = updatedCode,
    signature = "digital_signature"
)

// 执行热更新
val updateResult: UpdateResult = dynamicEngine.performHotUpdate(updatePackage)

// 回滚到指定版本
val rollbackResult: RollbackResult = dynamicEngine.rollbackToVersion("MyComponent", "1.0.0")
```

#### AI智能功能

```kotlin
// 获取AI组件推荐
val userContext = UserContext(
    platform = PlatformManager.getPlatformType(),
    preferences = mapOf("theme" to "dark")
)
val recommendations: List<ComponentRecommendation> = dynamicEngine.getAIRecommendations(userContext)

// AI代码生成
val request = CodeGenerationRequest(
    description = "创建一个登录表单",
    platform = PlatformType.ANDROID,
    style = "material_design"
)
val generatedCode: GeneratedCode = dynamicEngine.generateComponentCode(request)
```

### 3. 性能监控 (UnifyPerformanceMonitor)

#### 基础监控

```kotlin
// 启动性能监控
UnifyPerformanceMonitor.startMonitoring()

// 停止性能监控
UnifyPerformanceMonitor.stopMonitoring()

// 记录操作性能
UnifyPerformanceMonitor.startOperation("database_query", PerformanceCategory.DATABASE)
// ... 执行操作
val duration = UnifyPerformanceMonitor.endOperation("database_query")
```

#### 实时指标

```kotlin
// 获取实时指标流
val realTimeMetrics: StateFlow<RealTimeMetrics> = UnifyPerformanceMonitor.realTimeMetrics

// 收集实时指标
realTimeMetrics.collect { metrics ->
    println("帧率: ${metrics.frameRate}")
    println("内存使用: ${metrics.memoryUsage}%")
    println("CPU使用: ${metrics.cpuUsage}%")
}

// 获取性能评分
val performanceScore: StateFlow<Float> = UnifyPerformanceMonitor.performanceScore
```

#### 健康检查

```kotlin
// 执行健康检查
val healthCheck: PerformanceHealthCheck = UnifyPerformanceMonitor.performHealthCheck()

// 健康检查结果
when (healthCheck.status) {
    HealthStatus.GOOD -> println("系统运行良好")
    HealthStatus.WARNING -> println("发现性能问题: ${healthCheck.issues}")
    HealthStatus.CRITICAL -> println("严重性能问题需要立即处理")
}
```

#### 性能报告

```kotlin
// 生成详细性能报告
val report: DetailedPerformanceReport = UnifyPerformanceMonitor.getDetailedPerformanceReport()

// 获取组件性能报告
val componentReport: ComponentPerformanceReport = UnifyPerformanceMonitor.getComponentPerformanceReport()

// 智能性能调优
val optimizationResult: OptimizationResult = UnifyPerformanceMonitor.performIntelligentTuning()
```

### 4. 安全管理 (UnifySecurityManager)

#### 加密解密

```kotlin
// 创建安全管理器
val securityManager = UnifySecurityManager()

// 生成加密密钥
val key: String = securityManager.generateEncryptionKey(EncryptionType.AES_256)

// 加密数据
val encryptedData: String = securityManager.encrypt("敏感数据", key, EncryptionType.AES_256)

// 解密数据
val decryptedData: String = securityManager.decrypt(encryptedData, key, EncryptionType.AES_256)
```

#### 用户认证

```kotlin
// 密码认证
val authResult: AuthenticationResult = securityManager.authenticate(
    username = "user123",
    credential = "password",
    method = AuthenticationMethod.PASSWORD
)

// 生物识别认证
val biometricResult: AuthenticationResult = securityManager.authenticate(
    username = "user123",
    credential = "fingerprint_data",
    method = AuthenticationMethod.BIOMETRIC
)

// 多因子认证
val mfaResult: AuthenticationResult = securityManager.authenticateMultiFactor(
    username = "user123",
    password = "password",
    biometricData = "fingerprint_data"
)
```

#### 令牌管理

```kotlin
// 验证令牌
val isValid: Boolean = securityManager.validateToken("jwt_token")

// 刷新令牌
val newToken: String = securityManager.refreshToken("old_token")

// 撤销令牌
securityManager.revokeToken("token_to_revoke")
```

#### 威胁检测

```kotlin
// 检测威胁
val threatResult: ThreatDetectionResult = securityManager.detectThreat(
    input = "用户输入内容",
    threatType = "sql_injection"
)

if (threatResult.isThreatDetected) {
    println("检测到威胁: ${threatResult.threatType}")
    println("风险级别: ${threatResult.riskLevel}")
}
```

### 5. 代码质量管理 (UnifyCodeQualityManager)

#### 代码分析

```kotlin
// 创建质量管理器
val qualityManager = UnifyCodeQualityManager()

// 分析代码质量
val metrics: QualityMetrics = qualityManager.analyzeCode(sourceCode)

// 质量指标
println("代码行数: ${metrics.linesOfCode}")
println("圈复杂度: ${metrics.cyclomaticComplexity}")
println("方法数量: ${metrics.methodCount}")
println("类数量: ${metrics.classCount}")
```

#### 重复代码检测

```kotlin
// 检测代码重复
val duplicationResult: DuplicationResult = qualityManager.detectDuplication(sourceCode)

println("重复率: ${duplicationResult.duplicationPercentage}%")
duplicationResult.duplicatedBlocks.forEach { block ->
    println("重复代码块: ${block.startLine}-${block.endLine}")
}
```

#### 测试覆盖率分析

```kotlin
// 分析测试覆盖率
val coverageResult: CoverageResult = qualityManager.analyzeCoverage(
    sourceFiles = listOf("src/main/kotlin/MyClass.kt"),
    testFiles = listOf("src/test/kotlin/MyClassTest.kt")
)

println("覆盖率: ${coverageResult.coveragePercentage}%")
println("覆盖行数: ${coverageResult.coveredLines}/${coverageResult.totalLines}")
```

#### 质量规则检查

```kotlin
// 设置质量规则
val rules = listOf(
    QualityRule("max_method_length", "方法不应超过50行", 50),
    QualityRule("max_cyclomatic_complexity", "圈复杂度不应超过10", 10)
)
qualityManager.setQualityRules(rules)

// 检查质量规则
val violations: List<QualityViolation> = qualityManager.checkQualityRules(sourceCode)
```

## UI组件库

### 基础组件

#### UnifyButton

```kotlin
@Composable
fun UnifyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    style: ButtonStyle = ButtonStyle.PRIMARY
) {
    // 实现
}

// 使用示例
UnifyButton(
    text = "点击我",
    onClick = { println("按钮被点击") },
    style = ButtonStyle.PRIMARY
)
```

#### UnifyText

```kotlin
@Composable
fun UnifyText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = Color.Unspecified
) {
    // 实现
}

// 使用示例
UnifyText(
    text = "Hello Unify!",
    style = MaterialTheme.typography.headlineMedium
)
```

#### UnifyTextField

```kotlin
@Composable
fun UnifyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false
) {
    // 实现
}

// 使用示例
var text by remember { mutableStateOf("") }
UnifyTextField(
    value = text,
    onValueChange = { text = it },
    label = "用户名",
    placeholder = "请输入用户名"
)
```

### 布局组件

#### UnifyCard

```kotlin
@Composable
fun UnifyCard(
    modifier: Modifier = Modifier,
    elevation: Dp = 4.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable ColumnScope.() -> Unit
) {
    // 实现
}

// 使用示例
UnifyCard {
    UnifyText("卡片内容")
    UnifyButton("操作按钮", onClick = {})
}
```

### 导航组件

#### UnifyTabRow

```kotlin
@Composable
fun UnifyTabRow(
    selectedTabIndex: Int,
    tabs: List<String>,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // 实现
}

// 使用示例
var selectedTab by remember { mutableStateOf(0) }
UnifyTabRow(
    selectedTabIndex = selectedTab,
    tabs = listOf("首页", "搜索", "个人"),
    onTabSelected = { selectedTab = it }
)
```

### 高级组件

#### UnifyChart

```kotlin
@Composable
fun UnifyChart(
    data: List<ChartDataPoint>,
    chartType: ChartType,
    modifier: Modifier = Modifier,
    colors: List<Color> = defaultChartColors
) {
    // 实现
}

// 使用示例
val chartData = listOf(
    ChartDataPoint("一月", 100f),
    ChartDataPoint("二月", 150f),
    ChartDataPoint("三月", 120f)
)

UnifyChart(
    data = chartData,
    chartType = ChartType.LINE
)
```

## 错误处理

### 异常类型

```kotlin
// 平台异常
class PlatformException(message: String, cause: Throwable? = null) : Exception(message, cause)

// 动态加载异常
class DynamicLoadException(message: String, cause: Throwable? = null) : Exception(message, cause)

// 安全异常
class SecurityException(message: String, cause: Throwable? = null) : Exception(message, cause)

// 性能异常
class PerformanceException(message: String, cause: Throwable? = null) : Exception(message, cause)
```

### 错误处理最佳实践

```kotlin
try {
    val result = dynamicEngine.loadComponent("MyComponent", code)
    if (result.isSuccess) {
        // 处理成功情况
    } else {
        // 处理失败情况
        result.errors.forEach { error ->
            println("错误: ${error.message}")
        }
    }
} catch (e: DynamicLoadException) {
    // 处理动态加载异常
    println("动态加载失败: ${e.message}")
} catch (e: Exception) {
    // 处理其他异常
    println("未知错误: ${e.message}")
}
```

## 配置选项

### 全局配置

```kotlin
// 配置Unify-Core
UnifyCore.configure {
    // 平台配置
    platform {
        enableAutoDetection = true
        fallbackPlatform = PlatformType.WEB
    }
    
    // 动态引擎配置
    dynamicEngine {
        enableHotUpdate = true
        enableAIFeatures = true
        cacheSize = 100
    }
    
    // 性能监控配置
    performance {
        enableRealTimeMonitoring = true
        samplingInterval = 1000L
        enableIntelligentTuning = true
    }
    
    // 安全配置
    security {
        defaultSecurityLevel = SecurityLevel.HIGH
        enableThreatDetection = true
        encryptionType = EncryptionType.AES_256
    }
    
    // 质量管理配置
    quality {
        enableCodeAnalysis = true
        maxComplexityThreshold = 10
        minCoverageThreshold = 90
    }
}
```

## 最佳实践

### 1. 平台适配

```kotlin
// 根据平台调整UI
@Composable
fun AdaptiveLayout() {
    when (PlatformManager.getPlatformType()) {
        PlatformType.ANDROID, PlatformType.IOS -> {
            // 移动端布局
            Column { /* 垂直布局 */ }
        }
        PlatformType.DESKTOP, PlatformType.WEB -> {
            // 桌面端布局
            Row { /* 水平布局 */ }
        }
        else -> {
            // 默认布局
            Column { /* 默认布局 */ }
        }
    }
}
```

### 2. 性能优化

```kotlin
// 使用性能监控
@Composable
fun OptimizedComponent() {
    LaunchedEffect(Unit) {
        UnifyPerformanceMonitor.startOperation("component_render", PerformanceCategory.UI)
    }
    
    DisposableEffect(Unit) {
        onDispose {
            UnifyPerformanceMonitor.endOperation("component_render")
        }
    }
    
    // 组件内容
}
```

### 3. 安全实践

```kotlin
// 安全的动态组件加载
suspend fun loadSecureComponent(name: String, code: String): LoadResult {
    return try {
        // 安全验证
        val validationResult = securityManager.validateCode(code)
        if (!validationResult.isValid) {
            return LoadResult.failure("代码安全验证失败")
        }
        
        // 加载组件
        dynamicEngine.loadComponent(name, code)
    } catch (e: SecurityException) {
        LoadResult.failure("安全异常: ${e.message}")
    }
}
```

## 版本兼容性

| Unify-Core版本 | Kotlin版本 | Compose版本 | 最低Android API | 最低iOS版本 |
|---------------|-----------|-------------|----------------|-------------|
| 1.0.0         | 2.1.0     | 1.7.5       | 21 (Android 5.0) | 12.0      |

## 更新日志

### v1.0.0 (2025-01-27)
- 🎉 首次发布
- ✨ 支持8大平台
- 🚀 动态化热更新系统
- 🤖 AI智能功能集成
- 📊 完整性能监控体系
- 🔒 企业级安全框架
- 📋 代码质量管理系统
- 🧪 92.5%测试覆盖率

---

**文档版本**: v1.0.0  
**最后更新**: 2025年1月27日  
**维护团队**: Unify-Core开发团队
