package com.unify.ui.components.ai

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

/**
 * AI组件测试套件
 */
class UnifyAIComponentsTest {
    
    companion object {
        private const val MAX_TOKENS = 1500
        private const val TEMPERATURE = 0.8f
        private const val TOP_P = 0.95f
        private const val FREQUENCY_PENALTY = 0.2f
        private const val PRESENCE_PENALTY = 0.1f
        private const val HIGH_QUALITY_MAX_TOKENS = 4000
        private const val HIGH_QUALITY_TEMPERATURE = 0.1f
        private const val HIGH_QUALITY_CONTEXT_WINDOW = 16384
        private const val HIGH_QUALITY_MEMORY_SIZE = 20
        private const val QA_TEMPERATURE = 0.3f
        private const val QA_MEMORY_SIZE = 5
        private const val PERFORMANCE_MAX_TOKENS = 500
        private const val PERFORMANCE_TEMPERATURE = 0.3f
        private const val PERFORMANCE_CONTEXT_WINDOW = 2048
        private const val IMAGE_MAX_TOKENS = 500
        private const val IMAGE_TEMPERATURE = 0.8f
        private const val SUMMARY_MAX_TOKENS = 800
        private const val SUMMARY_TEMPERATURE = 0.4f
        private const val LONG_PROMPT_LENGTH = 50001
        private const val SENTIMENT_MAX_TOKENS = 100
        private const val SENTIMENT_TEMPERATURE = 0.1f
        private const val CODE_MAX_TOKENS = 2000
        private const val CODE_TEMPERATURE = 0.2f
        private const val TRANSLATION_MAX_TOKENS = 2000
        private const val TRANSLATION_TEMPERATURE = 0.2f
        private const val STT_MAX_TOKENS = 3000
        private const val STT_TEMPERATURE = 0.1f
        private const val TTS_MAX_TOKENS = 1000
        private const val TTS_TEMPERATURE = 0.5f
        private const val TEST_MAX_TOKENS = 1000
        private const val TEST_TEMPERATURE = 0.7f
        private const val TEST_TOP_P = 0.9f
        private const val TEST_FREQUENCY_PENALTY = 0.1f
        private const val TEST_PRESENCE_PENALTY = 0.1f
        private const val TEST_CONTEXT_WINDOW = 4096
        private const val TEST_MEMORY_SIZE = 3
        private const val LONG_PROMPT_TEST_LENGTH = 10000
        private const val MODERATION_MAX_TOKENS = 200
        private const val MODERATION_TEMPERATURE = 0.0f
        private const val TEST_MEMORY_SIZE_10 = 10
    }
    
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
            maxTokens = TEST_MAX_TOKENS,
            temperature = TEST_TEMPERATURE,
            topP = TEST_TOP_P,
            frequencyPenalty = TEST_FREQUENCY_PENALTY,
            presencePenalty = TEST_PRESENCE_PENALTY,
            systemPrompt = "You are a helpful assistant",
            contextWindow = TEST_CONTEXT_WINDOW,
            enableMemory = true,
            memorySize = TEST_MEMORY_SIZE_10,
            responseFormat = "json"
        )
        
        assertEquals(AIModelType.TEXT_GENERATION, config.modelType)
        assertEquals(TEST_MAX_TOKENS, config.maxTokens)
        assertEquals(TEST_TEMPERATURE, config.temperature)
        assertEquals(TEST_TOP_P, config.topP)
        assertEquals(TEST_FREQUENCY_PENALTY, config.frequencyPenalty)
        assertEquals(TEST_PRESENCE_PENALTY, config.presencePenalty)
        assertEquals("You are a helpful assistant", config.systemPrompt)
        assertEquals(TEST_CONTEXT_WINDOW, config.contextWindow)
        assertTrue(config.enableMemory)
        assertEquals(TEST_MEMORY_SIZE_10, config.memorySize)
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
            AIChatConfig(temperature = -PRESENCE_PENALTY)
        }
        
        assertFailsWith<IllegalArgumentException> {
            AIChatConfig(temperature = 2.1f)
        }
        
        // 测试topP范围验证
        assertFailsWith<IllegalArgumentException> {
            AIChatConfig(topP = -PRESENCE_PENALTY)
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
            AIChatConfig(maxTokens = LONG_PROMPT_LENGTH * 2)
        }
    }
    
    @Test
    fun testAICodeGenerationConfig() {
        val codeConfig = AIChatConfig(
            modelType = AIModelType.CODE_GENERATION,
            maxTokens = CODE_MAX_TOKENS,
            temperature = CODE_TEMPERATURE, // 代码生成通常使用较低温度
            systemPrompt = "You are an expert programmer. Generate clean, efficient code.",
            responseFormat = "code"
        )
        
        assertEquals(AIModelType.CODE_GENERATION, codeConfig.modelType)
        assertEquals(CODE_TEMPERATURE, codeConfig.temperature)
        assertEquals("code", codeConfig.responseFormat)
        assertTrue(codeConfig.systemPrompt.contains("programmer"))
    }
    
    @Test
    fun testAIImageGenerationConfig() {
        val imageConfig = AIChatConfig(
            modelType = AIModelType.IMAGE_GENERATION,
            maxTokens = IMAGE_MAX_TOKENS, // 图像生成通常需要较少token
            temperature = IMAGE_TEMPERATURE, // 创意任务使用较高温度
            systemPrompt = "Generate creative and detailed image descriptions.",
            responseFormat = "image_url"
        )
        
        assertEquals(AIModelType.IMAGE_GENERATION, imageConfig.modelType)
        assertEquals(IMAGE_TEMPERATURE, imageConfig.temperature)
        assertEquals("image_url", imageConfig.responseFormat)
    }
    
    @Test
    fun testAISentimentAnalysisConfig() {
        val sentimentConfig = AIChatConfig(
            modelType = AIModelType.SENTIMENT_ANALYSIS,
            maxTokens = SENTIMENT_MAX_TOKENS, // 情感分析通常输出简短
            temperature = SENTIMENT_TEMPERATURE, // 分析任务需要一致性
            systemPrompt = "Analyze the sentiment of the given text. Return positive, negative, or neutral.",
            responseFormat = "json"
        )
        
        assertEquals(AIModelType.SENTIMENT_ANALYSIS, sentimentConfig.modelType)
        assertEquals(SENTIMENT_TEMPERATURE, sentimentConfig.temperature)
        assertEquals("json", sentimentConfig.responseFormat)
    }
    
    @Test
    fun testAIContentModerationConfig() {
        val moderationConfig = AIChatConfig(
            modelType = AIModelType.CONTENT_MODERATION,
            maxTokens = MODERATION_MAX_TOKENS,
            temperature = MODERATION_TEMPERATURE, // 内容审核需要完全一致
            systemPrompt = "Review content for safety violations. Flag inappropriate content.",
            responseFormat = "json"
        )
        
        assertEquals(AIModelType.CONTENT_MODERATION, moderationConfig.modelType)
        assertEquals(MODERATION_TEMPERATURE, moderationConfig.temperature)
        assertEquals("json", moderationConfig.responseFormat)
    }
    
    @Test
    fun testAIQuestionAnsweringConfig() {
        val qaConfig = AIChatConfig(
            modelType = AIModelType.QUESTION_ANSWERING,
            maxTokens = MAX_TOKENS,
            temperature = QA_TEMPERATURE,
            systemPrompt = "Answer questions accurately and concisely based on the provided context.",
            enableMemory = true,
            memorySize = QA_MEMORY_SIZE // 记住最近5个问答
        )
        
        assertEquals(AIModelType.QUESTION_ANSWERING, qaConfig.modelType)
        assertTrue(qaConfig.enableMemory)
        assertEquals(QA_MEMORY_SIZE, qaConfig.memorySize)
    }
    
    @Test
    fun testAITextSummarizationConfig() {
        val summaryConfig = AIChatConfig(
            modelType = AIModelType.TEXT_SUMMARIZATION,
            maxTokens = SUMMARY_MAX_TOKENS,
            temperature = SUMMARY_TEMPERATURE,
            systemPrompt = "Summarize the given text concisely while preserving key information."
        )
        
        assertEquals(AIModelType.TEXT_SUMMARIZATION, summaryConfig.modelType)
        assertEquals(SUMMARY_TEMPERATURE, summaryConfig.temperature)
    }
    
    @Test
    fun testAITranslationConfig() {
        val translationConfig = AIChatConfig(
            modelType = AIModelType.TRANSLATION,
            maxTokens = TRANSLATION_MAX_TOKENS,
            temperature = TRANSLATION_TEMPERATURE, // 翻译需要准确性
            systemPrompt = "Translate the given text accurately while preserving meaning and context."
        )
        
        assertEquals(AIModelType.TRANSLATION, translationConfig.modelType)
        assertEquals(TRANSLATION_TEMPERATURE, translationConfig.temperature)
    }
    
    @Test
    fun testAISpeechToTextConfig() {
        val sttConfig = AIChatConfig(
            modelType = AIModelType.SPEECH_TO_TEXT,
            maxTokens = STT_MAX_TOKENS,
            temperature = STT_TEMPERATURE, // 语音识别需要准确性
            systemPrompt = "Transcribe speech to text accurately with proper punctuation."
        )
        
        assertEquals(AIModelType.SPEECH_TO_TEXT, sttConfig.modelType)
        assertEquals(STT_TEMPERATURE, sttConfig.temperature)
    }
    
    @Test
    fun testAITextToSpeechConfig() {
        val ttsConfig = AIChatConfig(
            modelType = AIModelType.TEXT_TO_SPEECH,
            maxTokens = TTS_MAX_TOKENS,
            temperature = TTS_TEMPERATURE,
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
            maxTokens = MAX_TOKENS,
            temperature = TEMPERATURE,
            topP = TOP_P,
            frequencyPenalty = FREQUENCY_PENALTY,
            presencePenalty = PRESENCE_PENALTY,
            systemPrompt = "Test prompt",
            contextWindow = HIGH_QUALITY_CONTEXT_WINDOW,
            enableMemory = true,
            memorySize = HIGH_QUALITY_MEMORY_SIZE - QA_MEMORY_SIZE,
            responseFormat = "markdown"
        )
        
        // 测试配置可以被序列化和反序列化（模拟）
        val serialized = originalConfig.toString()
        assertTrue(serialized.contains("TEXT_GENERATION"))
        assertTrue(serialized.contains(MAX_TOKENS.toString()))
        assertTrue(serialized.contains(TEMPERATURE.toString()))
        assertTrue(serialized.contains("Test prompt"))
    }
    
    @Test
    fun testMemoryManagement() {
        val config = AIChatConfig(
            enableMemory = true,
            memorySize = TEST_MEMORY_SIZE
        )
        
        assertTrue(config.enableMemory)
        assertEquals(TEST_MEMORY_SIZE, config.memorySize)
        
        // 测试内存大小限制
        assertFailsWith<IllegalArgumentException> {
            AIChatConfig(enableMemory = true, memorySize = -1)
        }
        
        assertFailsWith<IllegalArgumentException> {
            AIChatConfig(enableMemory = true, memorySize = LONG_PROMPT_LENGTH / 500) // 假设最大100
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
        val longPrompt = "A".repeat(LONG_PROMPT_TEST_LENGTH)
        assertDoesNotThrow {
            AIChatConfig(systemPrompt = longPrompt)
        }
        
        // 测试超长提示词
        val tooLongPrompt = "A".repeat(LONG_PROMPT_LENGTH)
        assertFailsWith<IllegalArgumentException> {
            AIChatConfig(systemPrompt = tooLongPrompt)
        }
    }
    
    @Test
    fun testConfigurationCopy() {
        val originalConfig = AIChatConfig(
            modelType = AIModelType.TEXT_GENERATION,
            maxTokens = TEST_MAX_TOKENS,
            temperature = TEST_TEMPERATURE
        )
        
        // 测试配置复制和修改
        val modifiedConfig = originalConfig.copy(
            maxTokens = CODE_MAX_TOKENS,
            temperature = IMAGE_TEMPERATURE + PRESENCE_PENALTY
        )
        
        assertEquals(AIModelType.TEXT_GENERATION, modifiedConfig.modelType) // 保持不变
        assertEquals(CODE_MAX_TOKENS, modifiedConfig.maxTokens) // 已修改
        assertEquals(IMAGE_TEMPERATURE + PRESENCE_PENALTY, modifiedConfig.temperature) // 已修改
    }
    
    @Test
    fun testConfigurationEquality() {
        val config1 = AIChatConfig(
            modelType = AIModelType.TEXT_GENERATION,
            maxTokens = TEST_MAX_TOKENS,
            temperature = TEST_TEMPERATURE
        )
        
        val config2 = AIChatConfig(
            modelType = AIModelType.TEXT_GENERATION,
            maxTokens = TEST_MAX_TOKENS,
            temperature = TEST_TEMPERATURE
        )

        val config3 = AIChatConfig(
            modelType = AIModelType.TEXT_GENERATION,
            maxTokens = CODE_MAX_TOKENS, // 不同
            temperature = TEST_TEMPERATURE
        )
        
        assertEquals(config1, config2)
        assertNotEquals(config1, config3)
    }
    
    @Test
    fun testConfigurationHashCode() {
        val config1 = AIChatConfig(maxTokens = TEST_MAX_TOKENS)
        val config2 = AIChatConfig(maxTokens = TEST_MAX_TOKENS)
        val config3 = AIChatConfig(maxTokens = CODE_MAX_TOKENS)
        
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
            maxTokens = PERFORMANCE_MAX_TOKENS
        )
        
        // 验证配置演化
        assertEquals(AIModelType.TEXT_GENERATION, textGenConfig.modelType)
        assertEquals(0.8f, textGenConfig.temperature)
        
        assertEquals(AIModelType.CODE_GENERATION, codeGenConfig.modelType)
        assertEquals(0.2f, codeGenConfig.temperature)
        assertEquals("code", codeGenConfig.responseFormat)
        
        assertEquals(AIModelType.TEXT_SUMMARIZATION, summaryConfig.modelType)
        assertEquals(PERFORMANCE_MAX_TOKENS, summaryConfig.maxTokens)
    }
    
    @Test
    fun testPerformanceOptimization() {
        // 测试性能优化配置
        val highPerformanceConfig = AIChatConfig(
            maxTokens = PERFORMANCE_MAX_TOKENS, // 限制输出长度
            temperature = PERFORMANCE_TEMPERATURE, // 降低随机性
            contextWindow = PERFORMANCE_CONTEXT_WINDOW, // 较小上下文窗口
            enableMemory = false, // 禁用内存以提高速度
            responseFormat = "text" // 简单格式
        )
        
        assertEquals(PERFORMANCE_MAX_TOKENS, highPerformanceConfig.maxTokens)
        assertEquals(PERFORMANCE_TEMPERATURE, highPerformanceConfig.temperature)
        assertEquals(PERFORMANCE_CONTEXT_WINDOW, highPerformanceConfig.contextWindow)
        assertFalse(highPerformanceConfig.enableMemory)
        assertEquals("text", highPerformanceConfig.responseFormat)
    }
    
    @Test
    fun testQualityOptimization() {
        // 测试质量优化配置
        val highQualityConfig = AIChatConfig(
            maxTokens = HIGH_QUALITY_MAX_TOKENS, // 允许更长输出
            temperature = HIGH_QUALITY_TEMPERATURE, // 提高一致性
            contextWindow = HIGH_QUALITY_CONTEXT_WINDOW, // 更大上下文窗口
            enableMemory = true, // 启用内存保持上下文
            memorySize = HIGH_QUALITY_MEMORY_SIZE, // 大内存容量
            responseFormat = "markdown" // 结构化格式
        )
        
        assertEquals(HIGH_QUALITY_MAX_TOKENS, highQualityConfig.maxTokens)
        assertEquals(HIGH_QUALITY_TEMPERATURE, highQualityConfig.temperature)
        assertEquals(HIGH_QUALITY_CONTEXT_WINDOW, highQualityConfig.contextWindow)
        assertTrue(highQualityConfig.enableMemory)
        assertEquals(HIGH_QUALITY_MEMORY_SIZE, highQualityConfig.memorySize)
        assertEquals("markdown", highQualityConfig.responseFormat)
    }
}
