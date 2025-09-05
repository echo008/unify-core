package com.unify.core.ai

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable

/**
 * AI模型类型枚举
 */
enum class AIModelType {
    GPT_3_5_TURBO,
    GPT_4,
    CLAUDE_3_HAIKU,
    CLAUDE_3_SONNET,
    GEMINI_PRO,
    LLAMA_2,
    MISTRAL_7B,
    QWEN_PLUS,
    BAICHUAN_2,
    CHATGLM_3
}

/**
 * AI配置数据类
 */
@Serializable
data class AIConfiguration(
    val modelType: AIModelType = AIModelType.GPT_3_5_TURBO,
    val accuracy: Float = 0.7f,
    val maxTokens: Int = 2048,
    val temperature: Float = 0.7f,
    val topP: Float = 0.9f,
    val frequencyPenalty: Float = 0.0f,
    val presencePenalty: Float = 0.0f,
    val contextLength: Int = 8192,
    val timeoutMs: Long = 30000L,
    val maxRetryAttempts: Int = 3,
    val enableCaching: Boolean = true,
    val cacheExpiryMs: Long = 3600000L,
    val enableLogging: Boolean = true,
    val enableMetrics: Boolean = true
)

/**
 * 模型配置数据类
 */
@Serializable
data class ModelConfiguration(
    val modelId: String,
    val maxTokens: Int = 2048,
    val temperature: Float = 0.7f,
    val topP: Float = 0.9f,
    val frequencyPenalty: Float = 0.0f,
    val presencePenalty: Float = 0.0f,
    val customParameters: Map<String, String> = emptyMap()
)

/**
 * AI配置管理器 - 管理AI模型配置和参数
 */
class AIConfigurationManager {
    
    companion object {
        // 配置常量
        const val BASE_ACCURACY = 0.7f
        const val MAX_ACCURACY = 0.95f
        const val MIN_ACCURACY = 0.5f
        const val DEFAULT_MAX_TOKENS = 2048
        const val DEFAULT_TEMPERATURE = 0.7f
        const val DEFAULT_TOP_P = 0.9f
        const val DEFAULT_STREAM = false
        const val DEFAULT_FREQUENCY_PENALTY = 0.0f
        const val DEFAULT_PRESENCE_PENALTY = 0.0f
        const val MAX_CONTEXT_LENGTH = 8192
        const val MIN_CONTEXT_LENGTH = 512
        const val DEFAULT_TIMEOUT_MS = 30000L
        const val MAX_RETRY_ATTEMPTS = 3
        const val CACHE_EXPIRY_MS = 3600000L // 1小时
    }
    
    private val _configuration = MutableStateFlow(AIConfiguration())
    val configuration: StateFlow<AIConfiguration> = _configuration.asStateFlow()
    
    private val _modelConfigurations = MutableStateFlow<Map<AICapabilityType, ModelConfiguration>>(
        mapOf(
            AICapabilityType.TEXT_GENERATION to ModelConfiguration(
                modelId = "gpt-3.5-turbo",
                maxTokens = DEFAULT_MAX_TOKENS,
                temperature = DEFAULT_TEMPERATURE,
                topP = DEFAULT_TOP_P,
                frequencyPenalty = DEFAULT_FREQUENCY_PENALTY,
                presencePenalty = DEFAULT_PRESENCE_PENALTY
            ),
            AICapabilityType.IMAGE_GENERATION to ModelConfiguration(
                modelId = "dall-e-3",
                maxTokens = 1024,
                temperature = 0.8f,
                topP = 0.95f
            ),
            AICapabilityType.SPEECH_TO_TEXT to ModelConfiguration(
                modelId = "whisper-1",
                maxTokens = 512,
                temperature = 0.0f
            ),
            AICapabilityType.TEXT_TO_SPEECH to ModelConfiguration(
                modelId = "tts-1",
                maxTokens = 256,
                temperature = 0.0f
            ),
            AICapabilityType.EMBEDDING to ModelConfiguration(
                modelId = "text-embedding-ada-002",
                maxTokens = 8191
            ),
            AICapabilityType.MODERATION to ModelConfiguration(
                modelId = "text-moderation-latest",
                maxTokens = 32768
            ),
            AICapabilityType.CODE_GENERATION to ModelConfiguration(
                modelId = "code-davinci-002",
                maxTokens = 4096,
                temperature = 0.2f
            ),
            AICapabilityType.TRANSLATION to ModelConfiguration(
                modelId = "gpt-3.5-turbo",
                maxTokens = 2048,
                temperature = 0.3f
            ),
            AICapabilityType.SUMMARIZATION to ModelConfiguration(
                modelId = "gpt-3.5-turbo",
                maxTokens = 1024,
                temperature = 0.5f
            ),
            AICapabilityType.QUESTION_ANSWERING to ModelConfiguration(
                modelId = "gpt-3.5-turbo",
                maxTokens = 1024,
                temperature = 0.1f
            )
        )
    )
    val modelConfigurations: StateFlow<Map<AICapabilityType, ModelConfiguration>> = _modelConfigurations.asStateFlow()
    
    /**
     * 更新AI配置
     */
    fun updateConfiguration(config: AIConfiguration) {
        _configuration.value = config.copy(
            accuracy = config.accuracy.coerceIn(MIN_ACCURACY, MAX_ACCURACY),
            maxTokens = config.maxTokens.coerceIn(1, DEFAULT_MAX_TOKENS),
            temperature = config.temperature.coerceIn(0.0f, 2.0f),
            topP = config.topP.coerceIn(0.0f, 1.0f),
            contextLength = config.contextLength.coerceIn(MIN_CONTEXT_LENGTH, MAX_CONTEXT_LENGTH),
            timeoutMs = config.timeoutMs.coerceAtLeast(1000L),
            maxRetryAttempts = config.maxRetryAttempts.coerceIn(0, 10)
        )
    }
    
    /**
     * 更新特定模型配置
     */
    fun updateModelConfiguration(modelType: AICapabilityType, config: ModelConfiguration) {
        val currentConfigs = _modelConfigurations.value.toMutableMap()
        currentConfigs[modelType] = config
        _modelConfigurations.value = currentConfigs
    }
    
    /**
     * 获取特定模型配置
     */
    fun getModelConfiguration(modelType: AICapabilityType): ModelConfiguration? {
        return _modelConfigurations.value[modelType]
    }
    
    /**
     * 重置为默认配置
     */
    fun resetToDefaults() {
        _configuration.value = AIConfiguration()
    }
    
    /**
     * 验证配置有效性
     */
    fun validateConfiguration(config: AIConfiguration): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (config.accuracy < MIN_ACCURACY || config.accuracy > MAX_ACCURACY) {
            errors.add("准确度必须在 $MIN_ACCURACY 到 $MAX_ACCURACY 之间")
        }
        
        if (config.maxTokens <= 0 || config.maxTokens > DEFAULT_MAX_TOKENS) {
            errors.add("最大令牌数必须在 1 到 $DEFAULT_MAX_TOKENS 之间")
        }
        
        if (config.temperature < 0.0f || config.temperature > 2.0f) {
            errors.add("温度参数必须在 0.0 到 2.0 之间")
        }
        
        if (config.topP < 0.0f || config.topP > 1.0f) {
            errors.add("Top-P 参数必须在 0.0 到 1.0 之间")
        }
        
        if (config.contextLength < MIN_CONTEXT_LENGTH || config.contextLength > MAX_CONTEXT_LENGTH) {
            errors.add("上下文长度必须在 $MIN_CONTEXT_LENGTH 到 $MAX_CONTEXT_LENGTH 之间")
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Error(errors)
        }
    }
}

/**
 * 验证结果密封类
 */
sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val errors: List<String>) : ValidationResult()
}
