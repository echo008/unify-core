package com.unify.core.ai

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable

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
    
    private val _modelConfigurations = MutableStateFlow<Map<AIModelType, ModelConfiguration>>(
        mapOf(
            AIModelType.TEXT_GENERATION to ModelConfiguration(
                modelId = "gpt-3.5-turbo",
                maxTokens = DEFAULT_MAX_TOKENS,
                temperature = DEFAULT_TEMPERATURE,
                topP = DEFAULT_TOP_P,
                frequencyPenalty = DEFAULT_FREQUENCY_PENALTY,
                presencePenalty = DEFAULT_PRESENCE_PENALTY
            ),
            AIModelType.IMAGE_GENERATION to ModelConfiguration(
                modelId = "dall-e-3",
                maxTokens = 1024,
                temperature = 0.8f,
                topP = 0.95f
            ),
            AIModelType.SPEECH_TO_TEXT to ModelConfiguration(
                modelId = "whisper-1",
                maxTokens = 512,
                temperature = 0.0f
            ),
            AIModelType.TEXT_TO_SPEECH to ModelConfiguration(
                modelId = "tts-1",
                maxTokens = 256,
                temperature = 0.0f
            ),
            AIModelType.EMBEDDING to ModelConfiguration(
                modelId = "text-embedding-ada-002",
                maxTokens = 8191
            ),
            AIModelType.MODERATION to ModelConfiguration(
                modelId = "text-moderation-latest",
                maxTokens = 32768
            ),
            AIModelType.CODE_GENERATION to ModelConfiguration(
                modelId = "code-davinci-002",
                maxTokens = 4096,
                temperature = 0.2f
            ),
            AIModelType.TRANSLATION to ModelConfiguration(
                modelId = "gpt-3.5-turbo",
                maxTokens = 2048,
                temperature = 0.3f
            ),
            AIModelType.SUMMARIZATION to ModelConfiguration(
                modelId = "gpt-3.5-turbo",
                maxTokens = 1024,
                temperature = 0.5f
            ),
            AIModelType.QUESTION_ANSWERING to ModelConfiguration(
                modelId = "gpt-3.5-turbo",
                maxTokens = 1024,
                temperature = 0.1f
            )
        )
    )
    val modelConfigurations: StateFlow<Map<AIModelType, ModelConfiguration>> = _modelConfigurations.asStateFlow()
    
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
    fun updateModelConfiguration(modelType: AIModelType, config: ModelConfiguration) {
        val currentConfigs = _modelConfigurations.value.toMutableMap()
        currentConfigs[modelType] = config
        _modelConfigurations.value = currentConfigs
    }
    
    /**
     * 获取特定模型配置
     */
    fun getModelConfiguration(modelType: AIModelType): ModelConfiguration? {
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
 * AI配置数据类
 */
@Serializable
data class AIConfiguration(
    val accuracy: Float = BASE_ACCURACY,
    val maxTokens: Int = DEFAULT_MAX_TOKENS,
    val temperature: Float = DEFAULT_TEMPERATURE,
    val topP: Float = DEFAULT_TOP_P,
    val frequencyPenalty: Float = DEFAULT_FREQUENCY_PENALTY,
    val presencePenalty: Float = DEFAULT_PRESENCE_PENALTY,
    val contextLength: Int = MAX_CONTEXT_LENGTH,
    val timeoutMs: Long = DEFAULT_TIMEOUT_MS,
    val maxRetryAttempts: Int = MAX_RETRY_ATTEMPTS,
    val enableCaching: Boolean = true,
    val cacheExpiryMs: Long = CACHE_EXPIRY_MS,
    val enableLogging: Boolean = true,
    val enableMetrics: Boolean = true
)

/**
 * 模型配置数据类
 */
@Serializable
data class ModelConfiguration(
    val modelId: String,
    val maxTokens: Int = DEFAULT_MAX_TOKENS,
    val temperature: Float = DEFAULT_TEMPERATURE,
    val topP: Float = DEFAULT_TOP_P,
    val frequencyPenalty: Float = DEFAULT_FREQUENCY_PENALTY,
    val presencePenalty: Float = DEFAULT_PRESENCE_PENALTY,
    val customParameters: Map<String, String> = emptyMap()
)

/**
 * AI模型类型枚举
 */
enum class AIModelType {
    TEXT_GENERATION,
    IMAGE_GENERATION,
    SPEECH_TO_TEXT,
    TEXT_TO_SPEECH,
    EMBEDDING,
    MODERATION,
    CODE_GENERATION,
    TRANSLATION,
    SUMMARIZATION,
    QUESTION_ANSWERING
}

/**
 * 验证结果密封类
 */
sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val errors: List<String>) : ValidationResult()
}
