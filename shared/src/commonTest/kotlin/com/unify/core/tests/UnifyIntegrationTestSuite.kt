package com.unify.core.tests

import kotlin.test.*
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.delay
import com.unify.core.platform.PlatformManager
import com.unify.core.dynamic.UnifyDynamicEngine
import com.unify.core.performance.UnifyPerformanceMonitor
import com.unify.core.security.UnifySecurityManager
import com.unify.core.quality.UnifyCodeQualityManager

/**
 * Unify集成测试套件
 * 测试各模块间的集成和端到端功能
 */
class UnifyIntegrationTestSuite {

    private lateinit var dynamicEngine: UnifyDynamicEngine
    private lateinit var performanceMonitor: UnifyPerformanceMonitor
    private lateinit var securityManager: UnifySecurityManager
    private lateinit var qualityManager: UnifyCodeQualityManager

    @BeforeTest
    fun setup() = runTest {
        // 初始化所有核心模块
        PlatformManager.initialize()
        dynamicEngine = UnifyDynamicEngine()
        performanceMonitor = UnifyPerformanceMonitor()
        securityManager = UnifySecurityManager()
        qualityManager = UnifyCodeQualityManager()
        
        // 启动性能监控
        performanceMonitor.startMonitoring()
    }

    @AfterTest
    fun tearDown() = runTest {
        performanceMonitor.stopMonitoring()
        dynamicEngine.cleanup()
        securityManager.cleanup()
        qualityManager.cleanup()
    }

    // 平台与动态引擎集成测试
    @Test
    fun testPlatformDynamicEngineIntegration() = runTest {
        // 获取平台信息
        val platformType = PlatformManager.getPlatformType()
        val deviceInfo = PlatformManager.getDeviceInfo()
        
        // 基于平台信息配置动态引擎
        dynamicEngine.configurePlatform(platformType, deviceInfo)
        
        // 验证动态引擎已正确配置
        assertTrue(dynamicEngine.isInitialized())
        assertEquals(platformType, dynamicEngine.getCurrentPlatform())
        
        // 测试平台特定组件加载
        val platformComponent = dynamicEngine.loadPlatformSpecificComponent(platformType)
        assertNotNull(platformComponent)
        assertTrue(platformComponent.isCompatible(platformType))
    }

    @Test
    fun testDynamicComponentWithPerformanceMonitoring() = runTest {
        // 注册性能监控回调
        var performanceMetrics: ComponentPerformanceMetrics? = null
        dynamicEngine.setPerformanceCallback { metrics ->
            performanceMetrics = metrics
        }
        
        // 加载动态组件
        val componentCode = """
            @Composable
            fun TestComponent() {
                Text("Dynamic Test Component")
            }
        """.trimIndent()
        
        val loadResult = dynamicEngine.loadComponent("TestComponent", componentCode)
        assertTrue(loadResult.isSuccess)
        
        // 等待性能数据收集
        delay(1000)
        
        // 验证性能监控数据
        assertNotNull(performanceMetrics)
        assertTrue(performanceMetrics!!.loadTime > 0)
        assertTrue(performanceMetrics!!.renderTime >= 0)
    }

    // 安全与动态引擎集成测试
    @Test
    fun testSecurityDynamicEngineIntegration() = runTest {
        // 配置安全策略
        securityManager.setSecurityLevel(SecurityLevel.HIGH)
        
        // 将安全管理器集成到动态引擎
        dynamicEngine.setSecurityValidator(securityManager)
        
        // 测试安全组件加载
        val secureComponentCode = """
            @Composable
            fun SecureComponent() {
                Text("Secure Dynamic Component")
            }
        """.trimIndent()
        
        val secureResult = dynamicEngine.loadSecureComponent("SecureComponent", secureComponentCode)
        assertTrue(secureResult.isSuccess)
        assertTrue(secureResult.securityValidated)
        
        // 测试恶意代码拦截
        val maliciousCode = """
            @Composable
            fun MaliciousComponent() {
                Runtime.getRuntime().exec("rm -rf /")
                Text("Malicious Component")
            }
        """.trimIndent()
        
        val maliciousResult = dynamicEngine.loadSecureComponent("MaliciousComponent", maliciousCode)
        assertFalse(maliciousResult.isSuccess)
        assertTrue(maliciousResult.securityIssues.isNotEmpty())
    }

    @Test
    fun testEncryptedComponentStorage() = runTest {
        val componentCode = """
            @Composable
            fun EncryptedComponent() {
                Text("Encrypted Dynamic Component")
            }
        """.trimIndent()
        
        // 加密存储组件
        val encryptionKey = securityManager.generateEncryptionKey(EncryptionType.AES_256)
        val storeResult = dynamicEngine.storeEncryptedComponent(
            "EncryptedComponent", 
            componentCode, 
            encryptionKey
        )
        assertTrue(storeResult.isSuccess)
        
        // 解密加载组件
        val loadResult = dynamicEngine.loadEncryptedComponent(
            "EncryptedComponent", 
            encryptionKey
        )
        assertTrue(loadResult.isSuccess)
        assertEquals(componentCode, loadResult.componentCode)
    }

    // 质量管理与代码分析集成测试
    @Test
    fun testQualityAnalysisIntegration() = runTest {
        val componentCode = """
            @Composable
            fun QualityTestComponent(param1: String, param2: Int) {
                if (param2 > 0) {
                    when (param1) {
                        "option1" -> Text("Option 1: $param2")
                        "option2" -> Text("Option 2: $param2")
                        else -> {
                            for (i in 1..param2) {
                                if (i % 2 == 0) {
                                    // Process even numbers
                                }
                            }
                            Text("Default: $param2")
                        }
                    }
                } else {
                    Text("Invalid parameter")
                }
            }
        """.trimIndent()
        
        // 集成质量分析到动态引擎
        dynamicEngine.setQualityAnalyzer(qualityManager)
        
        // 加载组件并进行质量分析
        val result = dynamicEngine.loadComponentWithQualityCheck("QualityTestComponent", componentCode)
        assertTrue(result.isSuccess)
        
        // 验证质量分析结果
        assertNotNull(result.qualityMetrics)
        assertTrue(result.qualityMetrics!!.cyclomaticComplexity > 1)
        assertTrue(result.qualityMetrics!!.linesOfCode > 10)
        
        // 检查质量建议
        if (result.qualityMetrics!!.cyclomaticComplexity > 10) {
            assertTrue(result.qualityRecommendations.any { it.type == "reduce_complexity" })
        }
    }

    // 性能监控全链路集成测试
    @Test
    fun testFullPerformanceMonitoringIntegration() = runTest {
        // 启动全链路性能监控
        performanceMonitor.enableFullStackMonitoring()
        
        // 执行一系列操作
        val operations = listOf(
            "platform_detection",
            "component_loading", 
            "security_validation",
            "quality_analysis",
            "rendering"
        )
        
        operations.forEach { operation ->
            performanceMonitor.startOperation(operation, PerformanceCategory.INTEGRATION)
            
            when (operation) {
                "platform_detection" -> PlatformManager.getDeviceInfo()
                "component_loading" -> dynamicEngine.loadComponent("TestOp", "Text(\"Test\")")
                "security_validation" -> securityManager.validateToken("test_token")
                "quality_analysis" -> qualityManager.analyzeCode("fun test() {}")
                "rendering" -> delay(50) // 模拟渲染时间
            }
            
            performanceMonitor.endOperation(operation)
        }
        
        // 生成性能报告
        val report = performanceMonitor.generatePerformanceReport()
        assertNotNull(report)
        
        // 验证所有操作都被记录
        operations.forEach { operation ->
            assertTrue(report.metrics.operationMetrics.containsKey(operation))
            val metrics = report.metrics.operationMetrics[operation]!!
            assertTrue(metrics.totalDuration.inWholeMilliseconds > 0)
        }
    }

    // 跨平台一致性集成测试
    @Test
    fun testCrossPlatformConsistencyIntegration() = runTest {
        val testComponent = """
            @Composable
            fun CrossPlatformComponent() {
                Column {
                    Text("Cross-platform component")
                    Button(onClick = {}) {
                        Text("Cross-platform button")
                    }
                }
            }
        """.trimIndent()
        
        // 在当前平台加载组件
        val currentPlatform = PlatformManager.getPlatformType()
        val loadResult = dynamicEngine.loadComponent("CrossPlatformComponent", testComponent)
        assertTrue(loadResult.isSuccess)
        
        // 验证组件在所有支持的平台上都能正确解析
        val supportedPlatforms = listOf(
            PlatformType.ANDROID,
            PlatformType.IOS, 
            PlatformType.WEB,
            PlatformType.DESKTOP
        )
        
        supportedPlatforms.forEach { platform ->
            val compatibility = dynamicEngine.checkPlatformCompatibility(testComponent, platform)
            assertTrue(compatibility.isCompatible, "Component not compatible with $platform")
            assertTrue(compatibility.requiredAdaptations.isEmpty() || 
                      compatibility.requiredAdaptations.all { it.isSupported })
        }
    }

    // 热更新端到端测试
    @Test
    fun testHotUpdateEndToEnd() = runTest {
        // 初始组件版本
        val initialComponent = """
            @Composable
            fun UpdatableComponent() {
                Text("Version 1.0")
            }
        """.trimIndent()
        
        // 加载初始版本
        val initialResult = dynamicEngine.loadComponent("UpdatableComponent", initialComponent)
        assertTrue(initialResult.isSuccess)
        
        // 创建更新包
        val updatedComponent = """
            @Composable
            fun UpdatableComponent() {
                Column {
                    Text("Version 2.0")
                    Text("New feature added")
                }
            }
        """.trimIndent()
        
        val updatePackage = dynamicEngine.createUpdatePackage(
            componentName = "UpdatableComponent",
            newCode = updatedComponent,
            version = "2.0"
        )
        
        // 安全验证更新包
        val validationResult = securityManager.validateUpdatePackage(updatePackage)
        assertTrue(validationResult.isValid)
        
        // 执行热更新
        val updateResult = dynamicEngine.performHotUpdate(updatePackage)
        assertTrue(updateResult.isSuccess)
        
        // 验证更新后的组件
        val updatedComponentInstance = dynamicEngine.getComponent("UpdatableComponent")
        assertNotNull(updatedComponentInstance)
        assertEquals("2.0", updatedComponentInstance.version)
        
        // 测试回滚功能
        val rollbackResult = dynamicEngine.rollbackToVersion("UpdatableComponent", "1.0")
        assertTrue(rollbackResult.isSuccess)
        
        val rolledBackComponent = dynamicEngine.getComponent("UpdatableComponent")
        assertEquals("1.0", rolledBackComponent!!.version)
    }

    // AI功能集成测试
    @Test
    fun testAIIntegrationEndToEnd() = runTest {
        // 启用AI功能
        dynamicEngine.enableAIFeatures()
        
        // 测试AI组件推荐
        val userContext = UserContext(
            platform = PlatformManager.getPlatformType(),
            screenSize = PlatformManager.getScreenInfo(),
            preferences = mapOf("theme" to "dark", "language" to "zh-CN")
        )
        
        val recommendations = dynamicEngine.getAIRecommendations(userContext)
        assertNotNull(recommendations)
        assertTrue(recommendations.isNotEmpty())
        
        // 验证推荐的相关性
        recommendations.forEach { recommendation ->
            assertTrue(recommendation.relevanceScore > 0.5)
            assertTrue(recommendation.platformCompatible)
        }
        
        // 测试AI代码生成
        val generationRequest = CodeGenerationRequest(
            description = "Create a login form with username and password fields",
            platform = PlatformManager.getPlatformType(),
            style = "material_design"
        )
        
        val generatedCode = dynamicEngine.generateComponentCode(generationRequest)
        assertNotNull(generatedCode)
        assertTrue(generatedCode.code.contains("TextField"))
        assertTrue(generatedCode.code.contains("Button"))
        assertTrue(generatedCode.qualityScore > 0.7)
        
        // 验证生成的代码可以正常加载
        val loadResult = dynamicEngine.loadComponent("AIGeneratedLogin", generatedCode.code)
        assertTrue(loadResult.isSuccess)
    }

    // 错误恢复和容错集成测试
    @Test
    fun testErrorRecoveryIntegration() = runTest {
        // 模拟组件加载失败
        val invalidComponent = """
            @Composable
            fun InvalidComponent() {
                ThisFunctionDoesNotExist()
            }
        """.trimIndent()
        
        val failedResult = dynamicEngine.loadComponent("InvalidComponent", invalidComponent)
        assertFalse(failedResult.isSuccess)
        assertTrue(failedResult.errors.isNotEmpty())
        
        // 验证错误恢复机制
        assertTrue(dynamicEngine.isHealthy())
        assertTrue(dynamicEngine.canLoadNewComponents())
        
        // 测试降级策略
        val fallbackComponent = dynamicEngine.getFallbackComponent("InvalidComponent")
        assertNotNull(fallbackComponent)
        assertTrue(fallbackComponent.isValid)
        
        // 验证系统仍可正常工作
        val validComponent = """
            @Composable
            fun ValidComponent() {
                Text("Recovery test successful")
            }
        """.trimIndent()
        
        val recoveryResult = dynamicEngine.loadComponent("ValidComponent", validComponent)
        assertTrue(recoveryResult.isSuccess)
    }

    // 大规模并发集成测试
    @Test
    fun testLargeScaleConcurrentIntegration() = runTest {
        val componentCount = 50
        val concurrentUsers = 20
        
        // 创建多个组件
        val components = (1..componentCount).map { index ->
            "Component$index" to """
                @Composable
                fun Component$index() {
                    Text("Component $index")
                }
            """.trimIndent()
        }
        
        // 并发加载组件
        val loadJobs = components.map { (name, code) ->
            kotlinx.coroutines.async {
                dynamicEngine.loadComponent(name, code)
            }
        }
        
        val loadResults = loadJobs.map { it.await() }
        
        // 验证所有组件都成功加载
        assertTrue(loadResults.all { it.isSuccess })
        
        // 并发访问组件
        val accessJobs = (1..concurrentUsers).map { userIndex ->
            kotlinx.coroutines.async {
                val accessResults = mutableListOf<Boolean>()
                repeat(10) {
                    val randomComponent = "Component${(1..componentCount).random()}"
                    val component = dynamicEngine.getComponent(randomComponent)
                    accessResults.add(component != null)
                }
                accessResults
            }
        }
        
        val accessResults = accessJobs.map { it.await() }.flatten()
        
        // 验证并发访问成功率
        val successRate = accessResults.count { it } / accessResults.size.toDouble()
        assertTrue(successRate > 0.95, "Concurrent access success rate too low: $successRate")
        
        // 验证性能指标
        val performanceReport = performanceMonitor.generatePerformanceReport()
        assertTrue(performanceReport.metrics.averageResponseTime.inWholeMilliseconds < 100)
        assertTrue(performanceReport.metrics.errorRate < 0.05)
    }

    // 内存管理集成测试
    @Test
    fun testMemoryManagementIntegration() = runTest {
        val initialMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        
        // 加载大量组件
        repeat(100) { index ->
            val componentCode = """
                @Composable
                fun MemoryTestComponent$index() {
                    LazyColumn {
                        items(1000) { item ->
                            Text("Item $item in component $index")
                        }
                    }
                }
            """.trimIndent()
            
            dynamicEngine.loadComponent("MemoryTestComponent$index", componentCode)
        }
        
        // 触发内存清理
        dynamicEngine.performMemoryCleanup()
        System.gc()
        delay(1000)
        
        val afterCleanupMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        val memoryIncrease = afterCleanupMemory - initialMemory
        
        // 验证内存使用在合理范围内
        assertTrue(memoryIncrease < 100 * 1024 * 1024, "Memory usage too high: ${memoryIncrease / 1024 / 1024}MB")
        
        // 验证内存监控数据
        val memoryMetrics = performanceMonitor.getMemoryMetrics()
        assertTrue(memoryMetrics.peakUsage > initialMemory)
        assertTrue(memoryMetrics.currentUsage < memoryMetrics.peakUsage)
    }
}

// 辅助数据类
data class ComponentPerformanceMetrics(
    val loadTime: Long,
    val renderTime: Long,
    val memoryUsage: Long
)

data class UserContext(
    val platform: PlatformType,
    val screenSize: ScreenInfo,
    val preferences: Map<String, String>
)

data class CodeGenerationRequest(
    val description: String,
    val platform: PlatformType,
    val style: String
)

data class GeneratedCode(
    val code: String,
    val qualityScore: Double,
    val dependencies: List<String>
)
