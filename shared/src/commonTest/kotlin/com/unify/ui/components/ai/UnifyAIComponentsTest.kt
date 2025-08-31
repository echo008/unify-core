package com.unify.ui.components.ai

import kotlin.test.*
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.delay

/**
 * AI组件全面测试套件
 */
class UnifyAIComponentsTest {
    
    @Test
    fun testAIModelTypes() {
        // 测试所有AI模型类型
        val allTypes = AIModelType.values()
        assertTrue(allTypes.contains(AIModelType.TEXT_GENERATION))
        assertTrue(allTypes.contains(AIModelType.IMAGE_GENERATION))
        assertTrue(allTypes.contains(AIModelType.CODE_GENERATION))
        assertTrue(allTypes.contains(AIModelType.SENTIMENT_ANALYSIS))
        assertTrue(allTypes.contains(AIModelType.CONTENT_MODERATION))
        assertTrue(allTypes.contains(AIModelType.QUESTION_ANSWERING))
        assertTrue(allTypes.contains(AIModelType.TEXT_SUMMARIZATION))
        assertTrue(allTypes.contains(AIModelType.TRANSLATION))
        assertTrue(allTypes.contains(AIModelType.SPEECH_TO_TEXT))
        assertTrue(allTypes.contains(AIModelType.TEXT_TO_SPEECH))
    }
    
    @Test
    fun testAIChatConfiguration() {
        val config = AIChatConfig(
            modelType = AIModelType.TEXT_GENERATION,
            maxTokens = 1000,
            temperature = 0.7f,
            topP = 0.9f,
            frequencyPenalty = 0.1f,
            presencePenalty = 0.1f,
            systemPrompt = "You are a helpful assistant",
            contextWindow = 4096,
            enableMemory = true,
            memorySize = 10,
            responseFormat = "json"
        )
        
        assertEquals(AIModelType.TEXT_GENERATION, config.modelType)
        assertEquals(1000, config.maxTokens)
        assertEquals(0.7f, config.temperature)
        assertEquals(0.9f, config.topP)
        assertEquals(0.1f, config.frequencyPenalty)
        assertEquals(0.1f, config.presencePenalty)
        assertEquals("You are a helpful assistant", config.systemPrompt)
        assertEquals(4096, config.contextWindow)
        assertTrue(config.enableMemory)
        assertEquals(10, config.memorySize)
        assertEquals("json", config.responseFormat)
    }
    
    @Test
    fun testAIChatConfigurationDefaults() {
        val defaultConfig = AIChatConfig()
        
        assertEquals(AIModelType.TEXT_GENERATION, defaultConfig.modelType)
        assertEquals(2048, defaultConfig.maxTokens)
        assertEquals(0.7f, defaultConfig.temperature)
        assertEquals(1.0f, defaultConfig.topP)
        assertEquals(0.0f, defaultConfig.frequencyPenalty)
        assertEquals(0.0f, defaultConfig.presencePenalty)
        assertEquals("", defaultConfig.systemPrompt)
        assertEquals(4096, defaultConfig.contextWindow)
        assertFalse(defaultConfig.enableMemory)
        assertEquals(0, defaultConfig.memorySize)
        assertEquals("text", defaultConfig.responseFormat)
    }
    
    @Test
    fun testAIChatConfigurationValidation() {
        // 测试温度范围验证
        assertFailsWith<IllegalArgumentException> {
            AIChatConfig(temperature = -0.1f)
        }
        
        assertFailsWith<IllegalArgumentException> {
            AIChatConfig(temperature = 2.1f)
        }
        
        // 测试topP范围验证
        assertFailsWith<IllegalArgumentException> {
            AIChatConfig(topP = -0.1f)
        }
        
        assertFailsWith<IllegalArgumentException> {
            AIChatConfig(topP = 1.1f)
        }
        
        // 测试penalty范围验证
        assertFailsWith<IllegalArgumentException> {
            AIChatConfig(frequencyPenalty = -2.1f)
        }
        
        assertFailsWith<IllegalArgumentException> {
            AIChatConfig(presencePenalty = 2.1f)
        }
        
        // 测试maxTokens范围验证
        assertFailsWith<IllegalArgumentException> {
            AIChatConfig(maxTokens = 0)
        }
        
        assertFailsWith<IllegalArgumentException> {
            AIChatConfig(maxTokens = 100001)
        }
    }
    
    @Test
    fun testAICodeGenerationConfig() {
        val codeConfig = AIChatConfig(
            modelType = AIModelType.CODE_GENERATION,
            maxTokens = 2000,
            temperature = 0.2f, // 代码生成通常使用较低温度
            systemPrompt = "You are an expert programmer. Generate clean, efficient code.",
            responseFormat = "code"
        )
        
        assertEquals(AIModelType.CODE_GENERATION, codeConfig.modelType)
        assertEquals(0.2f, codeConfig.temperature)
        assertEquals("code", codeConfig.responseFormat)
        assertTrue(codeConfig.systemPrompt.contains("programmer"))
    }
    
    @Test
    fun testAIImageGenerationConfig() {
        val imageConfig = AIChatConfig(
            modelType = AIModelType.IMAGE_GENERATION,
            maxTokens = 500, // 图像生成通常需要较少token
            temperature = 0.8f, // 创意任务使用较高温度
            systemPrompt = "Generate creative and detailed image descriptions.",
            responseFormat = "image_url"
        )
        
        assertEquals(AIModelType.IMAGE_GENERATION, imageConfig.modelType)
        assertEquals(0.8f, imageConfig.temperature)
        assertEquals("image_url", imageConfig.responseFormat)
    }
    
    @Test
    fun testAISentimentAnalysisConfig() {
        val sentimentConfig = AIChatConfig(
            modelType = AIModelType.SENTIMENT_ANALYSIS,
            maxTokens = 100, // 情感分析通常输出简短
            temperature = 0.1f, // 分析任务需要一致性
            systemPrompt = "Analyze the sentiment of the given text. Return positive, negative, or neutral.",
            responseFormat = "json"
        )
        
        assertEquals(AIModelType.SENTIMENT_ANALYSIS, sentimentConfig.modelType)
        assertEquals(0.1f, sentimentConfig.temperature)
        assertEquals("json", sentimentConfig.responseFormat)
    }
    
    @Test
    fun testAIContentModerationConfig() {
        val moderationConfig = AIChatConfig(
            modelType = AIModelType.CONTENT_MODERATION,
            maxTokens = 200,
            temperature = 0.0f, // 内容审核需要完全一致
            systemPrompt = "Review content for safety violations. Flag inappropriate content.",
            responseFormat = "json"
        )
        
        assertEquals(AIModelType.CONTENT_MODERATION, moderationConfig.modelType)
        assertEquals(0.0f, moderationConfig.temperature)
        assertEquals("json", moderationConfig.responseFormat)
    }
    
    @Test
    fun testAIQuestionAnsweringConfig() {
        val qaConfig = AIChatConfig(
            modelType = AIModelType.QUESTION_ANSWERING,
            maxTokens = 1500,
            temperature = 0.3f,
            systemPrompt = "Answer questions accurately and concisely based on the provided context.",
            enableMemory = true,
            memorySize = 5 // 记住最近5个问答
        )
        
        assertEquals(AIModelType.QUESTION_ANSWERING, qaConfig.modelType)
        assertTrue(qaConfig.enableMemory)
        assertEquals(5, qaConfig.memorySize)
    }
    
    @Test
    fun testAITextSummarizationConfig() {
        val summaryConfig = AIChatConfig(
            modelType = AIModelType.TEXT_SUMMARIZATION,
            maxTokens = 800,
            temperature = 0.4f,
            systemPrompt = "Summarize the given text concisely while preserving key information."
        )
        
        assertEquals(AIModelType.TEXT_SUMMARIZATION, summaryConfig.modelType)
        assertEquals(0.4f, summaryConfig.temperature)
    }
    
    @Test
    fun testAITranslationConfig() {
        val translationConfig = AIChatConfig(
            modelType = AIModelType.TRANSLATION,
            maxTokens = 2000,
            temperature = 0.2f, // 翻译需要准确性
            systemPrompt = "Translate the given text accurately while preserving meaning and context."
        )
        
        assertEquals(AIModelType.TRANSLATION, translationConfig.modelType)
        assertEquals(0.2f, translationConfig.temperature)
    }
    
    @Test
    fun testAISpeechToTextConfig() {
        val sttConfig = AIChatConfig(
            modelType = AIModelType.SPEECH_TO_TEXT,
            maxTokens = 3000,
            temperature = 0.1f, // 语音识别需要准确性
            systemPrompt = "Transcribe speech to text accurately with proper punctuation."
        )
        
        assertEquals(AIModelType.SPEECH_TO_TEXT, sttConfig.modelType)
        assertEquals(0.1f, sttConfig.temperature)
    }
    
    @Test
    fun testAITextToSpeechConfig() {
        val ttsConfig = AIChatConfig(
            modelType = AIModelType.TEXT_TO_SPEECH,
            maxTokens = 1000,
            temperature = 0.5f,
            systemPrompt = "Convert text to natural-sounding speech with appropriate intonation.",
            responseFormat = "audio"
        )
        
        assertEquals(AIModelType.TEXT_TO_SPEECH, ttsConfig.modelType)
        assertEquals("audio", ttsConfig.responseFormat)
    }
    
    @Test
    fun testConfigurationSerialization() {
        val originalConfig = AIChatConfig(
            modelType = AIModelType.TEXT_GENERATION,
            maxTokens = 1500,
            temperature = 0.8f,
            topP = 0.95f,
            frequencyPenalty = 0.2f,
            presencePenalty = 0.1f,
            systemPrompt = "Test prompt",
            contextWindow = 8192,
            enableMemory = true,
            memorySize = 15,
            responseFormat = "markdown"
        )
        
        // 测试配置可以被序列化和反序列化（模拟）
        val serialized = originalConfig.toString()
        assertTrue(serialized.contains("TEXT_GENERATION"))
        assertTrue(serialized.contains("1500"))
        assertTrue(serialized.contains("0.8"))
        assertTrue(serialized.contains("Test prompt"))
    }
    
    @Test
    fun testMemoryManagement() {
        val config = AIChatConfig(
            enableMemory = true,
            memorySize = 3
        )
        
        assertTrue(config.enableMemory)
        assertEquals(3, config.memorySize)
        
        // 测试内存大小限制
        assertFailsWith<IllegalArgumentException> {
            AIChatConfig(enableMemory = true, memorySize = -1)
        }
        
        assertFailsWith<IllegalArgumentException> {
            AIChatConfig(enableMemory = true, memorySize = 101) // 假设最大100
        }
    }
    
    @Test
    fun testContextWindowValidation() {
        // 测试有效的上下文窗口大小
        val validSizes = listOf(1024, 2048, 4096, 8192, 16384, 32768)
        
        validSizes.forEach { size ->
            assertDoesNotThrow {
                AIChatConfig(contextWindow = size)
            }
        }
        
        // 测试无效的上下文窗口大小
        assertFailsWith<IllegalArgumentException> {
            AIChatConfig(contextWindow = 512) // 太小
        }
        
        assertFailsWith<IllegalArgumentException> {
            AIChatConfig(contextWindow = 65537) // 太大
        }
    }
    
    @Test
    fun testResponseFormatValidation() {
        val validFormats = listOf("text", "json", "markdown", "html", "code", "image_url", "audio")
        
        validFormats.forEach { format ->
            assertDoesNotThrow {
                AIChatConfig(responseFormat = format)
            }
        }
        
        // 测试无效格式
        assertFailsWith<IllegalArgumentException> {
            AIChatConfig(responseFormat = "invalid_format")
        }
    }
    
    @Test
    fun testSystemPromptValidation() {
        // 测试空提示词
        assertDoesNotThrow {
            AIChatConfig(systemPrompt = "")
        }
        
        // 测试长提示词
        val longPrompt = "A".repeat(10000)
        assertDoesNotThrow {
            AIChatConfig(systemPrompt = longPrompt)
        }
        
        // 测试超长提示词
        val tooLongPrompt = "A".repeat(50001)
        assertFailsWith<IllegalArgumentException> {
            AIChatConfig(systemPrompt = tooLongPrompt)
        }
    }
    
    @Test
    fun testConfigurationCopy() {
        val originalConfig = AIChatConfig(
            modelType = AIModelType.TEXT_GENERATION,
            maxTokens = 1000,
            temperature = 0.7f
        )
        
        // 测试配置复制和修改
        val modifiedConfig = originalConfig.copy(
            maxTokens = 2000,
            temperature = 0.9f
        )
        
        assertEquals(AIModelType.TEXT_GENERATION, modifiedConfig.modelType) // 保持不变
        assertEquals(2000, modifiedConfig.maxTokens) // 已修改
        assertEquals(0.9f, modifiedConfig.temperature) // 已修改
    }
    
    @Test
    fun testConfigurationEquality() {
        val config1 = AIChatConfig(
            modelType = AIModelType.TEXT_GENERATION,
            maxTokens = 1000,
            temperature = 0.7f
        )
        
        val config2 = AIChatConfig(
            modelType = AIModelType.TEXT_GENERATION,
            maxTokens = 1000,
            temperature = 0.7f
        )
        
        val config3 = AIChatConfig(
            modelType = AIModelType.TEXT_GENERATION,
            maxTokens = 2000, // 不同
            temperature = 0.7f
        )
        
        assertEquals(config1, config2)
        assertNotEquals(config1, config3)
    }
    
    @Test
    fun testConfigurationHashCode() {
        val config1 = AIChatConfig(maxTokens = 1000)
        val config2 = AIChatConfig(maxTokens = 1000)
        val config3 = AIChatConfig(maxTokens = 2000)
        
        assertEquals(config1.hashCode(), config2.hashCode())
        assertNotEquals(config1.hashCode(), config3.hashCode())
    }
}

/**
 * AI组件集成测试
 */
class AIComponentsIntegrationTest {
    
    @Test
    fun testMultiModelWorkflow() = runTest {
        // 模拟多模型协作工作流
        
        // 1. 文本生成
        val textConfig = AIChatConfig(
            modelType = AIModelType.TEXT_GENERATION,
            systemPrompt = "Generate a product description"
        )
        
        // 2. 情感分析
        val sentimentConfig = AIChatConfig(
            modelType = AIModelType.SENTIMENT_ANALYSIS,
            systemPrompt = "Analyze sentiment of the generated text"
        )
        
        // 3. 内容审核
        val moderationConfig = AIChatConfig(
            modelType = AIModelType.CONTENT_MODERATION,
            systemPrompt = "Check content for safety"
        )
        
        // 验证配置链
        assertEquals(AIModelType.TEXT_GENERATION, textConfig.modelType)
        assertEquals(AIModelType.SENTIMENT_ANALYSIS, sentimentConfig.modelType)
        assertEquals(AIModelType.CONTENT_MODERATION, moderationConfig.modelType)
    }
    
    @Test
    fun testConfigurationPipeline() {
        // 测试配置管道
        val baseConfig = AIChatConfig()
        
        val textGenConfig = baseConfig.copy(
            modelType = AIModelType.TEXT_GENERATION,
            temperature = 0.8f
        )
        
        val codeGenConfig = textGenConfig.copy(
            modelType = AIModelType.CODE_GENERATION,
            temperature = 0.2f,
            responseFormat = "code"
        )
        
        val summaryConfig = codeGenConfig.copy(
            modelType = AIModelType.TEXT_SUMMARIZATION,
            maxTokens = 500
        )
        
        // 验证配置演化
        assertEquals(AIModelType.TEXT_GENERATION, textGenConfig.modelType)
        assertEquals(0.8f, textGenConfig.temperature)
        
        assertEquals(AIModelType.CODE_GENERATION, codeGenConfig.modelType)
        assertEquals(0.2f, codeGenConfig.temperature)
        assertEquals("code", codeGenConfig.responseFormat)
        
        assertEquals(AIModelType.TEXT_SUMMARIZATION, summaryConfig.modelType)
        assertEquals(500, summaryConfig.maxTokens)
    }
    
    @Test
    fun testPerformanceOptimization() {
        // 测试性能优化配置
        val highPerformanceConfig = AIChatConfig(
            maxTokens = 500, // 限制输出长度
            temperature = 0.3f, // 降低随机性
            contextWindow = 2048, // 较小上下文窗口
            enableMemory = false, // 禁用内存以提高速度
            responseFormat = "text" // 简单格式
        )
        
        assertEquals(500, highPerformanceConfig.maxTokens)
        assertEquals(0.3f, highPerformanceConfig.temperature)
        assertEquals(2048, highPerformanceConfig.contextWindow)
        assertFalse(highPerformanceConfig.enableMemory)
        assertEquals("text", highPerformanceConfig.responseFormat)
    }
    
    @Test
    fun testQualityOptimization() {
        // 测试质量优化配置
        val highQualityConfig = AIChatConfig(
            maxTokens = 4000, // 允许更长输出
            temperature = 0.1f, // 提高一致性
            contextWindow = 16384, // 更大上下文窗口
            enableMemory = true, // 启用内存保持上下文
            memorySize = 20, // 大内存容量
            responseFormat = "markdown" // 结构化格式
        )
        
        assertEquals(4000, highQualityConfig.maxTokens)
        assertEquals(0.1f, highQualityConfig.temperature)
        assertEquals(16384, highQualityConfig.contextWindow)
        assertTrue(highQualityConfig.enableMemory)
        assertEquals(20, highQualityConfig.memorySize)
        assertEquals("markdown", highQualityConfig.responseFormat)
    }
}
