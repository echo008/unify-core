# Unify-Core API 使用指南

## 概述

Unify-Core 是一个基于 Kotlin Multiplatform 和 Compose Multiplatform 的跨平台统一开发框架，提供企业级功能和原生性能保证。

## 核心模块

### 1. AI集成模块

#### UnifyAIEngine

统一AI引擎，支持多厂商大语言模型API集成。

```kotlin
// 初始化AI引擎
val aiEngine = UnifyAIEngine()
aiEngine.initialize()

// 配置API密钥
val keyManager = UnifyAIKeyManager()
keyManager.setAPIKey(AIProvider.OPENAI, "your-api-key")
keyManager.setActiveProvider(AIProvider.OPENAI)

// 文本生成
val request = AIRequest(
    type = AIRequestType.TEXT_GENERATION,
    content = "请生成一段关于AI的介绍",
    parameters = mapOf(
        "max_tokens" to "100",
        "temperature" to "0.7"
    )
)

val result = aiEngine.processTextGeneration(request)
when (result) {
    is AIResult.Success -> println("生成结果: ${result.content}")
    is AIResult.Error -> println("错误: ${result.message}")
}
```

#### 支持的AI提供商

- OpenAI (GPT-3.5, GPT-4)
- Anthropic (Claude)
- Google (Gemini)
- 阿里云 (通义千问)
- 百度 (文心一言)
- 腾讯 (混元)

### 2. 实时通信框架

#### UnifyWebSocketManager

统一WebSocket连接管理器，支持连接池和自动重连。

```kotlin
// 初始化WebSocket管理器
val webSocketManager = UnifyWebSocketManager()
webSocketManager.initialize()

// 连接到服务器
webSocketManager.connect("wss://your-server.com/ws")

// 监听连接状态
webSocketManager.connectionState.collect { state ->
    when (state) {
        WebSocketState.CONNECTED -> println("已连接")
        WebSocketState.DISCONNECTED -> println("已断开")
        WebSocketState.ERROR -> println("连接错误")
    }
}

// 发送消息
webSocketManager.sendMessage("Hello, Server!")

// 接收消息
webSocketManager.messages.collect { message ->
    println("收到消息: ${message.data}")
}
```

#### UnifyRealtimeDataSync

实时数据同步引擎。

```kotlin
val dataSyncEngine = UnifyRealtimeDataSync()
dataSyncEngine.initialize()

// 同步数据
val syncRequest = DataSyncRequest(
    dataType = "user_profile",
    operation = SyncOperation.UPDATE,
    data = mapOf("name" to "张三", "age" to 25)
)

dataSyncEngine.syncData(syncRequest)
```

### 3. 企业安全模块

#### UnifyEncryptionManager

统一加密管理器，支持多种加密算法。

```kotlin
val encryptionManager = UnifyEncryptionManager()
encryptionManager.initialize()

// 数据加密
val plainData = "敏感数据".toByteArray()
val encryptResult = encryptionManager.encryptData(plainData)

when (encryptResult) {
    is EncryptionResult.Success -> {
        println("加密成功")
        
        // 数据解密
        val decryptResult = encryptionManager.decryptData(encryptResult.encryptedData)
        when (decryptResult) {
            is DecryptionResult.Success -> {
                val decryptedText = String(decryptResult.decryptedData)
                println("解密结果: $decryptedText")
            }
            is DecryptionResult.Error -> println("解密失败: ${decryptResult.message}")
        }
    }
    is EncryptionResult.Error -> println("加密失败: ${encryptResult.message}")
}
```

#### UnifyPermissionManager

统一权限管理器，支持RBAC权限控制。

```kotlin
val permissionManager = UnifyPermissionManager()
permissionManager.initialize()

// 创建用户会话
val sessionRequest = SessionRequest(
    userId = "user123",
    credentials = mapOf("token" to "jwt-token"),
    deviceInfo = mapOf("platform" to "android")
)

val sessionResult = permissionManager.createSession(sessionRequest)

// 检查权限
val permissionRequest = PermissionRequest(
    userId = "user123",
    permission = "data.read",
    resource = "user_profile",
    action = "read"
)

val permissionResult = permissionManager.checkPermission(permissionRequest)
when (permissionResult) {
    is PermissionResult.Granted -> println("权限已授予")
    is PermissionResult.Denied -> println("权限被拒绝: ${permissionResult.reason}")
}
```

### 4. 性能监控系统

#### UnifyPerformanceCollector

跨平台性能指标收集器。

```kotlin
val performanceCollector = UnifyPerformanceCollector()
performanceCollector.initialize()

// 收集性能快照
val snapshot = performanceCollector.collectSnapshot()
println("CPU使用率: ${snapshot.cpuMetrics.usage}%")
println("内存使用: ${snapshot.memoryMetrics.used / 1024 / 1024}MB")

// 监听性能指标
performanceCollector.performanceMetrics.collect { metrics ->
    if (metrics.cpuMetrics.usage > 80.0) {
        println("CPU使用率过高!")
    }
}
```

#### UnifyPerformanceOptimizer

自动化性能优化器。

```kotlin
val performanceOptimizer = UnifyPerformanceOptimizer()
performanceOptimizer.initialize()

// 执行性能优化
val optimizationRequest = OptimizationRequest(
    targetMetrics = listOf("cpu_usage", "memory_usage"),
    priority = OptimizationPriority.HIGH
)

val result = performanceOptimizer.optimizePerformance(optimizationRequest)
when (result) {
    is OptimizationResult.Success -> println("优化完成: ${result.message}")
    is OptimizationResult.Error -> println("优化失败: ${result.message}")
}
```

## UI组件

### Compose组件使用

所有UI组件基于Compose Multiplatform实现，保证跨平台一致性和原生性能。

#### AI智能组件

```kotlin
@Composable
fun MyScreen() {
    val aiEngine = remember { UnifyAIEngine() }
    
    UnifySmartChatComponent(
        aiEngine = aiEngine,
        modifier = Modifier.fillMaxSize()
    )
}
```

#### 实时通信组件

```kotlin
@Composable
fun ChatScreen() {
    val webSocketManager = remember { UnifyWebSocketManager() }
    
    UnifyWebSocketComponent(
        webSocketManager = webSocketManager,
        title = "实时聊天"
    )
}
```

#### 性能监控组件

```kotlin
@Composable
fun PerformanceScreen() {
    val performanceCollector = remember { UnifyPerformanceCollector() }
    
    UnifyPerformanceMonitorComponent(
        performanceCollector = performanceCollector
    )
}
```

## 平台特定功能

### Android平台

```kotlin
// Android特定的权限请求
class AndroidUnifyCore : UnifyCore {
    override suspend fun requestPermissions(permissions: List<String>): Boolean {
        // Android权限请求实现
        return true
    }
}
```

### iOS平台

```kotlin
// iOS特定的系统集成
class IOSUnifyCore : UnifyCore {
    override fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            platform = PlatformType.IOS,
            deviceType = UIDevice.current.model,
            osVersion = UIDevice.current.systemVersion
        )
    }
}
```

### Desktop平台

```kotlin
// Desktop特定的文件系统访问
class DesktopUnifyCore : UnifyCore {
    override fun getStorageInfo(): StorageInfo {
        val file = File(System.getProperty("user.home"))
        return StorageInfo(
            totalSpace = file.totalSpace,
            availableSpace = file.freeSpace,
            usedSpace = file.totalSpace - file.freeSpace
        )
    }
}
```

## 测试和基准测试

### 集成测试

```kotlin
val testSuite = UnifyIntegrationTestSuite()
val report = testSuite.runFullTestSuite()

println("测试通过率: ${report.passedTests}/${report.totalTests}")
```

### 性能基准测试

```kotlin
val benchmark = UnifyPerformanceBenchmark()
val report = benchmark.runBenchmarks()

println("整体性能评分: ${report.overallScore}")
println("性能等级: ${report.performanceGrade}")
```

### 安全审计

```kotlin
val securityAuditor = UnifySecurityAuditor()
val auditReport = securityAuditor.performSecurityAudit()

println("安全评分: ${auditReport.overallScore}")
println("风险等级: ${auditReport.riskLevel}")
```

## 配置和初始化

### 应用程序初始化

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 初始化Unify-Core
        runBlocking {
            UnifyCore.initialize()
        }
    }
}
```

### 配置文件示例

```kotlin
// unify-config.kt
object UnifyConfig {
    const val AI_API_TIMEOUT = 30000L
    const val WEBSOCKET_RECONNECT_INTERVAL = 5000L
    const val PERFORMANCE_COLLECTION_INTERVAL = 1000L
    const val ENCRYPTION_ALGORITHM = "AES-256-GCM"
}
```

## 错误处理

### 统一错误处理

```kotlin
sealed class UnifyError {
    data class NetworkError(val message: String) : UnifyError()
    data class SecurityError(val message: String) : UnifyError()
    data class PerformanceError(val message: String) : UnifyError()
}

// 错误处理示例
try {
    val result = aiEngine.processTextGeneration(request)
} catch (e: UnifyError.NetworkError) {
    println("网络错误: ${e.message}")
} catch (e: UnifyError.SecurityError) {
    println("安全错误: ${e.message}")
}
```

## 最佳实践

### 1. 性能优化

- 使用协程进行异步操作
- 合理使用缓存机制
- 避免在主线程执行耗时操作
- 定期进行性能监控和优化

### 2. 安全最佳实践

- 始终使用加密传输敏感数据
- 实施最小权限原则
- 定期进行安全审计
- 保持API密钥安全

### 3. 跨平台开发

- 使用expect/actual机制处理平台差异
- 保持UI组件的平台一致性
- 测试所有目标平台
- 遵循各平台的设计规范

## 故障排除

### 常见问题

1. **编译错误**
   - 检查Kotlin版本兼容性
   - 确保所有依赖项正确配置
   - 验证expect/actual声明匹配

2. **运行时错误**
   - 检查网络连接
   - 验证API密钥配置
   - 查看日志输出

3. **性能问题**
   - 使用性能监控工具
   - 检查内存泄漏
   - 优化算法复杂度

## 版本兼容性

| Unify-Core版本 | Kotlin版本 | Compose版本 | 支持平台 |
|---------------|------------|-------------|----------|
| 1.0.0         | 2.1.0      | 1.6.11      | Android, iOS, Desktop, Web |
| 1.1.0         | 2.1.0      | 1.6.11      | + HarmonyOS, TV, Watch |

## 许可证

本项目采用 MIT 许可证。详见 LICENSE 文件。

## 支持和社区

- GitHub Issues: [项目地址]
- 文档网站: [文档地址]
- 社区论坛: [论坛地址]

---

更新时间: 2025-09-10
版本: 1.0.0
