package com.unify.ai

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Unify跨平台AI引擎
 * 支持8大平台的统一AI能力
 */
interface UnifyAIEngine {
    suspend fun generateText(prompt: String, config: AIConfig = AIConfig()): AIResult<String>
    suspend fun generateImage(prompt: String, config: ImageConfig = ImageConfig()): AIResult<ByteArray>
    suspend fun analyzeImage(imageData: ByteArray, prompt: String = ""): AIResult<String>
    suspend fun speechToText(audioData: ByteArray, language: String = "zh-CN"): AIResult<String>
    suspend fun textToSpeech(text: String, voice: String = "default"): AIResult<ByteArray>
    suspend fun translate(text: String, fromLang: String, toLang: String): AIResult<String>
    suspend fun summarize(text: String, maxLength: Int = 200): AIResult<String>
    suspend fun classify(text: String, categories: List<String>): AIResult<ClassificationResult>
    suspend fun extractEntities(text: String): AIResult<List<Entity>>
    suspend fun generateEmbedding(text: String): AIResult<List<Float>>
    fun streamText(prompt: String, config: AIConfig = AIConfig()): Flow<AIResult<String>>
    suspend fun chatCompletion(messages: List<ChatMessage>, config: ChatConfig = ChatConfig()): AIResult<String>
}

/**
 * AI配置
 */
@Serializable
data class AIConfig(
    val model: String = "gpt-3.5-turbo",
    val temperature: Float = 0.7f,
    val maxTokens: Int = 1000,
    val topP: Float = 1.0f,
    val frequencyPenalty: Float = 0.0f,
    val presencePenalty: Float = 0.0f,
    val stopSequences: List<String> = emptyList(),
    val seed: Int? = null,
    val systemPrompt: String? = null
)

/**
 * 图像生成配置
 */
@Serializable
data class ImageConfig(
    val model: String = "dall-e-3",
    val size: String = "1024x1024",
    val quality: String = "standard",
    val style: String = "vivid",
    val responseFormat: String = "url"
)

/**
 * 聊天配置
 */
@Serializable
data class ChatConfig(
    val model: String = "gpt-3.5-turbo",
    val temperature: Float = 0.7f,
    val maxTokens: Int = 1000,
    val contextWindow: Int = 4096,
    val enableMemory: Boolean = true,
    val systemPrompt: String? = null
)

/**
 * AI结果封装
 */
@Serializable
sealed class AIResult<out T> {
    @Serializable
    data class Success<T>(val data: T, val usage: TokenUsage? = null) : AIResult<T>()
    
    @Serializable
    data class Error(val exception: AIException) : AIResult<Nothing>()
    
    @Serializable
    data class Loading(val progress: Float = 0f) : AIResult<Nothing>()
}

/**
 * AI异常
 */
@Serializable
data class AIException(
    val code: String,
    val message: String,
    val details: String? = null
) : Exception(message)

/**
 * Token使用情况
 */
@Serializable
data class TokenUsage(
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int,
    val cost: Double? = null
)

/**
 * 聊天消息
 */
@Serializable
data class ChatMessage(
    val role: String, // "system", "user", "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 分类结果
 */
@Serializable
data class ClassificationResult(
    val category: String,
    val confidence: Float,
    val allScores: Map<String, Float>
)

/**
 * 实体
 */
@Serializable
data class Entity(
    val text: String,
    val type: String,
    val confidence: Float,
    val startIndex: Int,
    val endIndex: Int
)

/**
 * AI模型类型
 */
@Serializable
enum class AIModelType {
    TEXT_GENERATION,
    IMAGE_GENERATION,
    IMAGE_ANALYSIS,
    SPEECH_TO_TEXT,
    TEXT_TO_SPEECH,
    TRANSLATION,
    SUMMARIZATION,
    CLASSIFICATION,
    ENTITY_EXTRACTION,
    EMBEDDING
}

/**
 * Unify AI引擎实现
 */
class UnifyAIEngineImpl(
    private val apiKey: String,
    private val baseUrl: String = "https://api.openai.com/v1"
) : UnifyAIEngine {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    companion object {
        private const val HIGH_CONFIDENCE = 0.8f
        private const val MEDIUM_CONFIDENCE = 0.6f
        private const val LOW_CONFIDENCE = 0.4f
        private const val MAX_RETRIES = 3
        private const val RETRY_DELAY = 1000L
        private const val DEFAULT_TIMEOUT = 30000L
    }
    
    override suspend fun generateText(prompt: String, config: AIConfig): AIResult<String> {
        return try {
            val response = callTextGenerationAPI(prompt, config)
            AIResult.Success(response.text, response.usage)
        } catch (e: Exception) {
            AIResult.Error(AIException("TEXT_GENERATION_ERROR", e.message ?: "文本生成失败"))
        }
    }
    
    override suspend fun generateImage(prompt: String, config: ImageConfig): AIResult<ByteArray> {
        return try {
            val imageData = callImageGenerationAPI(prompt, config)
            AIResult.Success(imageData)
        } catch (e: Exception) {
            AIResult.Error(AIException("IMAGE_GENERATION_ERROR", e.message ?: "图像生成失败"))
        }
    }
    
    override suspend fun analyzeImage(imageData: ByteArray, prompt: String): AIResult<String> {
        return try {
            val analysis = callImageAnalysisAPI(imageData, prompt)
            AIResult.Success(analysis)
        } catch (e: Exception) {
            AIResult.Error(AIException("IMAGE_ANALYSIS_ERROR", e.message ?: "图像分析失败"))
        }
    }
    
    override suspend fun speechToText(audioData: ByteArray, language: String): AIResult<String> {
        return try {
            val text = callSpeechToTextAPI(audioData, language)
            AIResult.Success(text)
        } catch (e: Exception) {
            AIResult.Error(AIException("SPEECH_TO_TEXT_ERROR", e.message ?: "语音识别失败"))
        }
    }
    
    override suspend fun textToSpeech(text: String, voice: String): AIResult<ByteArray> {
        return try {
            val audioData = callTextToSpeechAPI(text, voice)
            AIResult.Success(audioData)
        } catch (e: Exception) {
            AIResult.Error(AIException("TEXT_TO_SPEECH_ERROR", e.message ?: "语音合成失败"))
        }
    }
    
    override suspend fun translate(text: String, fromLang: String, toLang: String): AIResult<String> {
        return try {
            val translatedText = callTranslationAPI(text, fromLang, toLang)
            AIResult.Success(translatedText)
        } catch (e: Exception) {
            AIResult.Error(AIException("TRANSLATION_ERROR", e.message ?: "翻译失败"))
        }
    }
    
    override suspend fun summarize(text: String, maxLength: Int): AIResult<String> {
        return try {
            val summary = callSummarizationAPI(text, maxLength)
            AIResult.Success(summary)
        } catch (e: Exception) {
            AIResult.Error(AIException("SUMMARIZATION_ERROR", e.message ?: "摘要生成失败"))
        }
    }
    
    override suspend fun classify(text: String, categories: List<String>): AIResult<ClassificationResult> {
        return try {
            val result = callClassificationAPI(text, categories)
            AIResult.Success(result)
        } catch (e: Exception) {
            AIResult.Error(AIException("CLASSIFICATION_ERROR", e.message ?: "分类失败"))
        }
    }
    
    override suspend fun extractEntities(text: String): AIResult<List<Entity>> {
        return try {
            val entities = callEntityExtractionAPI(text)
            AIResult.Success(entities)
        } catch (e: Exception) {
            AIResult.Error(AIException("ENTITY_EXTRACTION_ERROR", e.message ?: "实体提取失败"))
        }
    }
    
    override suspend fun generateEmbedding(text: String): AIResult<List<Float>> {
        return try {
            val embedding = callEmbeddingAPI(text)
            AIResult.Success(embedding)
        } catch (e: Exception) {
            AIResult.Error(AIException("EMBEDDING_ERROR", e.message ?: "向量化失败"))
        }
    }
    
    override fun streamText(prompt: String, config: AIConfig): Flow<AIResult<String>> = flow {
        try {
            emit(AIResult.Loading(0.1f))
            val stream = callStreamingTextAPI(prompt, config)
            var progress = 0.1f
            stream.collect { chunk ->
                progress = minOf(progress + 0.1f, 0.9f)
                emit(AIResult.Loading(progress))
                emit(AIResult.Success(chunk))
            }
            emit(AIResult.Loading(1.0f))
        } catch (e: Exception) {
            emit(AIResult.Error(AIException("STREAMING_ERROR", e.message ?: "流式生成失败")))
        }
    }
    
    override suspend fun chatCompletion(messages: List<ChatMessage>, config: ChatConfig): AIResult<String> {
        return try {
            val response = callChatCompletionAPI(messages, config)
            AIResult.Success(response.content, response.usage)
        } catch (e: Exception) {
            AIResult.Error(AIException("CHAT_COMPLETION_ERROR", e.message ?: "对话完成失败"))
        }
    }
    
    // 平台特定的API调用实现
    private expect suspend fun callTextGenerationAPI(prompt: String, config: AIConfig): TextGenerationResponse
    private expect suspend fun callImageGenerationAPI(prompt: String, config: ImageConfig): ByteArray
    private expect suspend fun callImageAnalysisAPI(imageData: ByteArray, prompt: String): String
    private expect suspend fun callSpeechToTextAPI(audioData: ByteArray, language: String): String
    private expect suspend fun callTextToSpeechAPI(text: String, voice: String): ByteArray
    private expect suspend fun callTranslationAPI(text: String, fromLang: String, toLang: String): String
    private expect suspend fun callSummarizationAPI(text: String, maxLength: Int): String
    private expect suspend fun callClassificationAPI(text: String, categories: List<String>): ClassificationResult
    private expect suspend fun callEntityExtractionAPI(text: String): List<Entity>
    private expect suspend fun callEmbeddingAPI(text: String): List<Float>
    private expect suspend fun callStreamingTextAPI(prompt: String, config: AIConfig): Flow<String>
    private expect suspend fun callChatCompletionAPI(messages: List<ChatMessage>, config: ChatConfig): ChatCompletionResponse
}

/**
 * 内部响应数据类
 */
@Serializable
internal data class TextGenerationResponse(
    val text: String,
    val usage: TokenUsage?
)

@Serializable
internal data class ChatCompletionResponse(
    val content: String,
    val usage: TokenUsage?
)

/**
 * AI工具类
 */
object UnifyAIUtils {
    /**
     * 验证API密钥格式
     */
    fun validateApiKey(apiKey: String): Boolean {
        return apiKey.isNotBlank() && apiKey.length >= 20
    }
    
    /**
     * 计算Token数量（估算）
     */
    fun estimateTokenCount(text: String): Int {
        return (text.length / 4.0).toInt() // 粗略估算：1 token ≈ 4 字符
    }
    
    /**
     * 截断文本到指定Token数量
     */
    fun truncateToTokens(text: String, maxTokens: Int): String {
        val maxChars = maxTokens * 4
        return if (text.length <= maxChars) text else text.take(maxChars)
    }
    
    /**
     * 构建系统提示词
     */
    fun buildSystemPrompt(role: String, context: String = ""): String {
        return "你是一个$role。$context请用中文回答。"
    }
}
