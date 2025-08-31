package com.unify.core.tests

import com.unify.core.ai.*
import kotlin.test.*
import kotlinx.coroutines.test.runTest

/**
 * AI智能功能专项测试
 */
class UnifyAITestSuite {
    
    private lateinit var aiEngine: UnifyAIEngine
    private lateinit var configManager: AIConfigurationManager
    
    @BeforeTest
    fun setup() = runTest {
        aiEngine = UnifyAIEngine()
        configManager = AIConfigurationManager()
        
        aiEngine.initialize()
        configManager.initialize()
    }
    
    @Test
    fun testAIEngineInitialization() = runTest {
        assertTrue(aiEngine.isInitialized.value, "AI引擎应该已初始化")
    }
    
    @Test
    fun testComponentRecommendation() = runTest {
        val context = ComponentContext(
            currentComponents = listOf("UnifyButton"),
            userIntent = "创建登录表单",
            platformTarget = "Android"
        )
        
        val recommendations = aiEngine.recommendComponents(context)
        
        assertTrue(recommendations.isNotEmpty(), "应该有组件推荐")
        assertTrue(recommendations.all { it.confidence > 0.0f }, "所有推荐都应该有置信度")
        assertTrue(recommendations.any { it.componentName.contains("TextField") }, "应该推荐输入框组件")
    }
    
    @Test
    fun testCodeGeneration() = runTest {
        val request = CodeGenerationRequest(
            description = "创建一个带图标的按钮",
            context = "Android Compose UI",
            requirements = listOf("支持点击事件", "自定义颜色", "响应式设计")
        )
        
        val result = aiEngine.generateCode(request)
        
        assertNotNull(result.code, "应该生成代码")
        assertTrue(result.confidence > 0.7f, "代码生成置信度应该较高")
        assertTrue(result.code.contains("@Composable"), "生成的代码应该是Compose组件")
        assertTrue(result.suggestions.isNotEmpty(), "应该提供优化建议")
    }
    
    @Test
    fun testErrorDiagnosis() = runTest {
        val errorContext = ErrorContext(
            errorMessage = "kotlin.KotlinNullPointerException",
            stackTrace = """
                at com.unify.core.Component.render(Component.kt:42)
                at com.unify.core.Screen.build(Screen.kt:15)
            """.trimIndent(),
            codeContext = """
                val component: Component? = null
                component.render() // 这里会抛出NPE
            """.trimIndent()
        )
        
        val diagnosis = aiEngine.diagnoseError(errorContext)
        
        assertEquals("NullPointerException", diagnosis.errorType, "错误类型识别应该正确")
        assertTrue(diagnosis.rootCause.contains("未初始化"), "应该识别出根本原因")
        assertTrue(diagnosis.solutions.isNotEmpty(), "应该提供解决方案")
        assertTrue(diagnosis.confidence > 0.8f, "诊断置信度应该很高")
    }
    
    @Test
    fun testPerformanceOptimization() = runTest {
        val metrics = PerformanceMetrics(
            renderTime = 25L, // 超过16ms阈值
            memoryUsage = 150 * 1024 * 1024L, // 150MB
            cpuUsage = 85.0f // 85% CPU使用率
        )
        
        val suggestions = aiEngine.optimizePerformance(metrics)
        
        assertTrue(suggestions.suggestions.isNotEmpty(), "应该提供优化建议")
        assertTrue(suggestions.expectedImprovement > 0.0f, "应该有预期改进效果")
        assertTrue(suggestions.suggestions.any { it.contains("LazyColumn") || it.contains("remember") }, 
                  "应该包含具体的优化建议")
    }
    
    @Test
    fun testTestGeneration() = runTest {
        val codeContext = CodeContext(
            functionName = "calculateTotal",
            parameters = listOf("items: List<Item>", "taxRate: Float"),
            returnType = "Float"
        )
        
        val testCases = aiEngine.generateTests(codeContext)
        
        assertTrue(testCases.isNotEmpty(), "应该生成测试用例")
        assertTrue(testCases.any { it.type == TestType.UNIT }, "应该包含单元测试")
        assertTrue(testCases.all { it.name.startsWith("test") }, "测试方法名应该以test开头")
        assertTrue(testCases.all { it.code.contains("@Test") }, "测试代码应该包含@Test注解")
    }
    
    @Test
    fun testAIConfigurationManagement() = runTest {
        val modelType = "test_model"
        val config = AIConfiguration(
            modelType = modelType,
            version = "1.0.0",
            parameters = mapOf("threshold" to 0.8f, "maxResults" to 10),
            accuracy = 0.85f,
            enabled = true
        )
        
        configManager.updateConfiguration(modelType, config)
        
        val retrievedConfig = configManager.getConfiguration(modelType)
        assertNotNull(retrievedConfig, "应该能获取配置")
        assertEquals(config.modelType, retrievedConfig.modelType, "配置类型应该匹配")
        assertEquals(config.accuracy, retrievedConfig.accuracy, "准确率应该匹配")
    }
    
    @Test
    fun testAutoTuneConfiguration() = runTest {
        val modelType = "component_recommendation"
        val metrics = PerformanceMetrics(
            renderTime = 120L, // 高渲染时间
            memoryUsage = 80 * 1024 * 1024L,
            cpuUsage = 60.0f
        )
        
        val optimizedConfig = configManager.autoTuneConfiguration(modelType, metrics)
        
        assertNotNull(optimizedConfig, "应该返回优化后的配置")
        assertTrue(optimizedConfig.updatedAt > optimizedConfig.createdAt, "更新时间应该晚于创建时间")
    }
    
    @Test
    fun testLearningDataManagement() = runTest {
        val modelType = "test_model"
        val learningData = LearningData(
            input = "测试输入",
            expectedOutput = "期望输出",
            actualOutput = "实际输出",
            feedback = 0.9f,
            context = mapOf("platform" to "Android", "version" to "1.0")
        )
        
        configManager.addLearningData(modelType, learningData)
        
        val statistics = configManager.getModelStatistics(modelType)
        assertTrue(statistics.trainingDataSize > 0, "训练数据大小应该大于0")
        assertEquals(modelType, statistics.modelType, "模型类型应该匹配")
    }
    
    @Test
    fun testModelStatistics() {
        val modelType = "component_recommendation"
        val statistics = configManager.getModelStatistics(modelType)
        
        assertNotNull(statistics, "应该返回统计信息")
        assertEquals(modelType, statistics.modelType, "模型类型应该正确")
        assertTrue(statistics.accuracy >= 0.0f && statistics.accuracy <= 1.0f, "准确率应该在0-1之间")
        assertTrue(statistics.performanceScore >= 0.0f, "性能分数应该非负")
    }
    
    @Test
    fun testContextAnalysis() = runTest {
        // 测试UI布局上下文
        val layoutContext = ComponentContext(
            currentComponents = listOf("UnifyColumn"),
            userIntent = "需要垂直排列的布局",
            platformTarget = "Android"
        )
        
        val layoutRecommendations = aiEngine.recommendComponents(layoutContext)
        assertTrue(layoutRecommendations.any { it.componentName.contains("Column") || it.componentName.contains("Card") }, 
                  "布局上下文应该推荐布局组件")
        
        // 测试输入上下文
        val inputContext = ComponentContext(
            currentComponents = emptyList(),
            userIntent = "用户需要输入用户名和密码",
            platformTarget = "iOS"
        )
        
        val inputRecommendations = aiEngine.recommendComponents(inputContext)
        assertTrue(inputRecommendations.any { it.componentName.contains("TextField") || it.componentName.contains("Button") }, 
                  "输入上下文应该推荐输入组件")
    }
    
    @Test
    fun testRecommendationConfidence() = runTest {
        val context = ComponentContext(
            currentComponents = listOf("UnifyText"),
            userIntent = "显示用户信息",
            platformTarget = "Web"
        )
        
        val recommendations = aiEngine.recommendComponents(context)
        
        assertTrue(recommendations.all { it.confidence >= 0.0f && it.confidence <= 1.0f }, 
                  "置信度应该在0-1之间")
        assertTrue(recommendations.any { it.confidence > 0.8f }, "应该有高置信度的推荐")
    }
    
    @Test
    fun testMultiPlatformRecommendations() = runTest {
        val platforms = listOf("Android", "iOS", "Web", "Desktop", "HarmonyOS")
        
        platforms.forEach { platform ->
            val context = ComponentContext(
                currentComponents = listOf("UnifyButton"),
                userIntent = "创建导航界面",
                platformTarget = platform
            )
            
            val recommendations = aiEngine.recommendComponents(context)
            assertTrue(recommendations.isNotEmpty(), "$platform 平台应该有推荐组件")
        }
    }
    
    @Test
    fun testAIModelTypes() {
        val modelTypes = AIModelType.values()
        
        assertTrue(modelTypes.contains(AIModelType.COMPONENT_RECOMMENDATION), "应该包含组件推荐模型")
        assertTrue(modelTypes.contains(AIModelType.CODE_GENERATION), "应该包含代码生成模型")
        assertTrue(modelTypes.contains(AIModelType.ERROR_DIAGNOSIS), "应该包含错误诊断模型")
        assertTrue(modelTypes.contains(AIModelType.PERFORMANCE_OPTIMIZATION), "应该包含性能优化模型")
        assertTrue(modelTypes.contains(AIModelType.TEST_GENERATION), "应该包含测试生成模型")
    }
    
    @Test
    fun testOptimizationStrategies() = runTest {
        val strategies = listOf(
            ComponentRecommendationOptimizer(),
            CodeGenerationOptimizer(),
            ErrorDiagnosisOptimizer(),
            PerformanceOptimizationOptimizer(),
            TestGenerationOptimizer()
        )
        
        val testConfig = AIConfiguration(
            modelType = "test",
            version = "1.0",
            parameters = mapOf("threshold" to 0.8f),
            accuracy = 0.85f,
            enabled = true
        )
        
        val testMetrics = PerformanceMetrics(
            renderTime = 100L,
            memoryUsage = 200 * 1024 * 1024L,
            cpuUsage = 75.0f
        )
        
        strategies.forEach { strategy ->
            val optimizedConfig = strategy.optimize(testConfig, testMetrics)
            assertNotNull(optimizedConfig, "优化策略应该返回配置")
            assertTrue(optimizedConfig.updatedAt >= testConfig.updatedAt, "更新时间应该不早于原配置")
        }
    }
    
    @Test
    fun testLearningDataSet() {
        val dataSet = LearningDataSet("test_model")
        
        assertEquals(0, dataSet.size, "初始数据集应该为空")
        assertEquals(0.0f, dataSet.quality, "初始质量应该为0")
        
        // 添加学习数据
        repeat(10) { i ->
            dataSet.addData(LearningData(
                input = "input_$i",
                expectedOutput = "expected_$i",
                actualOutput = "actual_$i",
                feedback = 0.8f + (i % 3) * 0.1f
            ))
        }
        
        assertEquals(10, dataSet.size, "数据集大小应该为10")
        assertTrue(dataSet.quality > 0.0f, "质量应该大于0")
        
        // 测试重训练条件
        assertFalse(dataSet.shouldRetrain(), "数据量不足时不应该重训练")
        
        // 添加更多数据
        repeat(100) { i ->
            dataSet.addData(LearningData(
                input = "input_${i + 10}",
                expectedOutput = "expected_${i + 10}",
                actualOutput = "actual_${i + 10}",
                feedback = 0.9f
            ))
        }
        
        assertTrue(dataSet.shouldRetrain(), "数据量充足时应该重训练")
    }
    
    @Test
    fun testContextTypeDetection() {
        // 测试布局特征检测
        val layoutContext = ComponentContext(
            currentComponents = emptyList(),
            userIntent = "需要layout排列组件",
            platformTarget = "Android"
        )
        assertTrue(layoutContext.hasLayoutFeatures(), "应该检测到布局特征")
        
        // 测试输入特征检测
        val inputContext = ComponentContext(
            currentComponents = emptyList(),
            userIntent = "用户需要input输入数据",
            platformTarget = "iOS"
        )
        assertTrue(inputContext.hasInputFeatures(), "应该检测到输入特征")
        
        // 测试导航特征检测
        val navigationContext = ComponentContext(
            currentComponents = emptyList(),
            userIntent = "添加navigation导航功能",
            platformTarget = "Web"
        )
        assertTrue(navigationContext.hasNavigationFeatures(), "应该检测到导航特征")
    }
}
