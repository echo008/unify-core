package com.unify.core.ai

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * AI API提供商枚举
 */
enum class AIProvider(val displayName: String) {
    OPENAI("OpenAI"),
    ANTHROPIC("Anthropic"),
    GOOGLE("Google AI"),
    ALIBABA_CLOUD("阿里云"),
    BAIDU("百度"),
    TENCENT("腾讯")
}

/**
 * API配置数据类
 */
@Serializable
data class APIConfiguration(
    val provider: AIProvider,
    val apiKey: String,
    val baseUrl: String,
    val timeout: Long = 30000L,
    val maxRetries: Int = 3
)

/**
 * OpenAI API请求数据类
 */
@Serializable
data class OpenAIRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Float = 0.7f,
    val max_tokens: Int = 2048,
    val top_p: Float = 0.9f,
    val frequency_penalty: Float = 0.0f,
    val presence_penalty: Float = 0.0f,
    val stream: Boolean = false
)

/**
 * 聊天消息数据类
 */
@Serializable
data class ChatMessage(
    val role: String, // "system", "user", "assistant"
    val content: String
)

/**
 * OpenAI API响应数据类
 */
@Serializable
data class OpenAIResponse(
    val id: String,
    val `object`: String,
    val created: Long,
    val model: String,
    val choices: List<Choice>,
    val usage: Usage
)

@Serializable
data class Choice(
    val index: Int,
    val message: ChatMessage,
    val finish_reason: String?
)

@Serializable
data class Usage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int
)

/**
 * Unify AI API客户端 - 统一的AI服务API接口
 */
class UnifyAIApiClient(
    private val httpClient: HttpClient,
    private val json: Json = Json { 
        ignoreUnknownKeys = true
        isLenient = true
    }
) {
    private var currentConfig: APIConfiguration? = null
    
    /**
     * 配置API客户端
     */
    fun configure(config: APIConfiguration) {
        currentConfig = config
    }
    
    /**
     * 发送聊天完成请求
     */
    suspend fun chatCompletion(
        messages: List<ChatMessage>,
        model: String = "gpt-3.5-turbo",
        temperature: Float = 0.7f,
        maxTokens: Int = 2048
    ): AIApiResult<String> {
        val config = currentConfig ?: return AIApiResult.Error("API未配置")
        
        return try {
            withTimeout(config.timeout) {
                when (config.provider) {
                    AIProvider.OPENAI -> openAIChatCompletion(config, messages, model, temperature, maxTokens)
                    AIProvider.ANTHROPIC -> anthropicChatCompletion(config, messages, model, temperature, maxTokens)
                    AIProvider.GOOGLE -> googleChatCompletion(config, messages, model, temperature, maxTokens)
                    AIProvider.ALIBABA_CLOUD -> alibabaCloudChatCompletion(config, messages, model, temperature, maxTokens)
                    AIProvider.BAIDU -> baiduChatCompletion(config, messages, model, temperature, maxTokens)
                    AIProvider.TENCENT -> tencentChatCompletion(config, messages, model, temperature, maxTokens)
                }
            }
        } catch (e: Exception) {
            AIApiResult.Error("API请求失败: ${e.message}")
        }
    }
    
    /**
     * OpenAI聊天完成实现
     */
    private suspend fun openAIChatCompletion(
        config: APIConfiguration,
        messages: List<ChatMessage>,
        model: String,
        temperature: Float,
        maxTokens: Int
    ): AIApiResult<String> {
        return try {
            val request = OpenAIRequest(
                model = model,
                messages = messages,
                temperature = temperature,
                max_tokens = maxTokens
            )
            
            val response: HttpResponse = httpClient.post("${config.baseUrl}/v1/chat/completions") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer ${config.apiKey}")
                    append(HttpHeaders.ContentType, "application/json")
                }
                setBody(json.encodeToString(OpenAIRequest.serializer(), request))
            }
            
            if (response.status.isSuccess()) {
                val responseBody = response.body<String>()
                val openAIResponse = json.decodeFromString(OpenAIResponse.serializer(), responseBody)
                val content = openAIResponse.choices.firstOrNull()?.message?.content
                    ?: return AIApiResult.Error("响应内容为空")
                
                AIApiResult.Success(
                    data = content,
                    tokensUsed = openAIResponse.usage.total_tokens,
                    model = openAIResponse.model
                )
            } else {
                AIApiResult.Error("API请求失败: ${response.status}")
            }
        } catch (e: Exception) {
            AIApiResult.Error("OpenAI API调用失败: ${e.message}")
        }
    }
    
    /**
     * Anthropic Claude API实现
     */
    private suspend fun anthropicChatCompletion(
        config: APIConfiguration,
        messages: List<ChatMessage>,
        model: String,
        temperature: Float,
        maxTokens: Int
    ): AIApiResult<String> {
        return try {
            // 构建Claude API请求格式
            val prompt = messages.joinToString("\n") { "${it.role}: ${it.content}" }
            
            val requestBody = mapOf(
                "model" to model,
                "prompt" to prompt,
                "max_tokens_to_sample" to maxTokens,
                "temperature" to temperature
            )
            
            val response: HttpResponse = httpClient.post("${config.baseUrl}/v1/complete") {
                headers {
                    append("x-api-key", config.apiKey)
                    append(HttpHeaders.ContentType, "application/json")
                    append("anthropic-version", "2023-06-01")
                }
                setBody(json.encodeToString(
                    kotlinx.serialization.json.JsonObject.serializer(),
                    kotlinx.serialization.json.JsonObject(requestBody.mapValues {
                        kotlinx.serialization.json.JsonPrimitive(it.value.toString())
                    })
                ))
            }
            
            if (response.status.isSuccess()) {
                val responseBody = response.body<String>()
                // 简化处理，实际需要解析Claude响应格式
                AIApiResult.Success(
                    data = "Claude响应: $responseBody",
                    tokensUsed = maxTokens,
                    model = model
                )
            } else {
                AIApiResult.Error("Claude API请求失败: ${response.status}")
            }
        } catch (e: Exception) {
            AIApiResult.Error("Claude API调用失败: ${e.message}")
        }
    }
    
    /**
     * Google Gemini API实现
     */
    private suspend fun googleChatCompletion(
        config: APIConfiguration,
        messages: List<ChatMessage>,
        model: String,
        temperature: Float,
        maxTokens: Int
    ): AIApiResult<String> {
        return try {
            val requestBody = mapOf(
                "contents" to listOf(
                    mapOf(
                        "parts" to listOf(
                            mapOf("text" to messages.last().content)
                        )
                    )
                ),
                "generationConfig" to mapOf(
                    "temperature" to temperature,
                    "maxOutputTokens" to maxTokens
                )
            )
            
            val response: HttpResponse = httpClient.post("${config.baseUrl}/v1beta/models/$model:generateContent") {
                parameter("key", config.apiKey)
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                }
                setBody(json.encodeToString(
                    kotlinx.serialization.json.JsonObject.serializer(),
                    kotlinx.serialization.json.JsonObject(requestBody.mapValues { entry ->
                        when (val value = entry.value) {
                            is String -> kotlinx.serialization.json.JsonPrimitive(value)
                            is Number -> kotlinx.serialization.json.JsonPrimitive(value)
                            else -> kotlinx.serialization.json.JsonPrimitive(value.toString())
                        }
                    })
                ))
            }
            
            if (response.status.isSuccess()) {
                val responseBody = response.body<String>()
                AIApiResult.Success(
                    data = "Gemini响应: $responseBody",
                    tokensUsed = maxTokens,
                    model = model
                )
            } else {
                AIApiResult.Error("Gemini API请求失败: ${response.status}")
            }
        } catch (e: Exception) {
            AIApiResult.Error("Gemini API调用失败: ${e.message}")
        }
    }
    
    /**
     * 阿里云通义千问API实现
     */
    private suspend fun alibabaCloudChatCompletion(
        config: APIConfiguration,
        messages: List<ChatMessage>,
        model: String,
        temperature: Float,
        maxTokens: Int
    ): AIApiResult<String> {
        return try {
            val requestBody = mapOf(
                "model" to model,
                "input" to mapOf(
                    "messages" to messages.map { 
                        mapOf("role" to it.role, "content" to it.content) 
                    }
                ),
                "parameters" to mapOf(
                    "temperature" to temperature,
                    "max_tokens" to maxTokens
                )
            )
            
            val response: HttpResponse = httpClient.post("${config.baseUrl}/api/v1/services/aigc/text-generation/generation") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer ${config.apiKey}")
                    append(HttpHeaders.ContentType, "application/json")
                }
                setBody(json.encodeToString(
                    kotlinx.serialization.json.JsonObject.serializer(),
                    kotlinx.serialization.json.JsonObject(requestBody.mapValues { entry ->
                        when (val value = entry.value) {
                            is String -> kotlinx.serialization.json.JsonPrimitive(value)
                            is Number -> kotlinx.serialization.json.JsonPrimitive(value)
                            else -> kotlinx.serialization.json.JsonPrimitive(value.toString())
                        }
                    })
                ))
            }
            
            if (response.status.isSuccess()) {
                val responseBody = response.body<String>()
                AIApiResult.Success(
                    data = "通义千问响应: $responseBody",
                    tokensUsed = maxTokens,
                    model = model
                )
            } else {
                AIApiResult.Error("通义千问API请求失败: ${response.status}")
            }
        } catch (e: Exception) {
            AIApiResult.Error("通义千问API调用失败: ${e.message}")
        }
    }
    
    /**
     * 百度文心一言API实现
     */
    private suspend fun baiduChatCompletion(
        config: APIConfiguration,
        messages: List<ChatMessage>,
        model: String,
        temperature: Float,
        maxTokens: Int
    ): AIApiResult<String> {
        return try {
            val requestBody = mapOf(
                "messages" to messages.map { 
                    mapOf("role" to it.role, "content" to it.content) 
                },
                "temperature" to temperature,
                "max_output_tokens" to maxTokens
            )
            
            val response: HttpResponse = httpClient.post("${config.baseUrl}/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/$model") {
                parameter("access_token", config.apiKey)
                headers {
                    append(HttpHeaders.ContentType, "application/json")
                }
                setBody(json.encodeToString(
                    kotlinx.serialization.json.JsonObject.serializer(),
                    kotlinx.serialization.json.JsonObject(requestBody.mapValues { entry ->
                        when (val value = entry.value) {
                            is String -> kotlinx.serialization.json.JsonPrimitive(value)
                            is Number -> kotlinx.serialization.json.JsonPrimitive(value)
                            else -> kotlinx.serialization.json.JsonPrimitive(value.toString())
                        }
                    })
                ))
            }
            
            if (response.status.isSuccess()) {
                val responseBody = response.body<String>()
                AIApiResult.Success(
                    data = "文心一言响应: $responseBody",
                    tokensUsed = maxTokens,
                    model = model
                )
            } else {
                AIApiResult.Error("文心一言API请求失败: ${response.status}")
            }
        } catch (e: Exception) {
            AIApiResult.Error("文心一言API调用失败: ${e.message}")
        }
    }
    
    /**
     * 腾讯混元API实现
     */
    private suspend fun tencentChatCompletion(
        config: APIConfiguration,
        messages: List<ChatMessage>,
        model: String,
        temperature: Float,
        maxTokens: Int
    ): AIApiResult<String> {
        return try {
            val requestBody = mapOf(
                "Model" to model,
                "Messages" to messages.map { 
                    mapOf("Role" to it.role, "Content" to it.content) 
                },
                "Temperature" to temperature,
                "MaxTokens" to maxTokens
            )
            
            val response: HttpResponse = httpClient.post(config.baseUrl) {
                headers {
                    append(HttpHeaders.Authorization, config.apiKey)
                    append(HttpHeaders.ContentType, "application/json")
                }
                setBody(json.encodeToString(
                    kotlinx.serialization.json.JsonObject.serializer(),
                    kotlinx.serialization.json.JsonObject(requestBody.mapValues { entry ->
                        when (val value = entry.value) {
                            is String -> kotlinx.serialization.json.JsonPrimitive(value)
                            is Number -> kotlinx.serialization.json.JsonPrimitive(value)
                            else -> kotlinx.serialization.json.JsonPrimitive(value.toString())
                        }
                    })
                ))
            }
            
            if (response.status.isSuccess()) {
                val responseBody = response.body<String>()
                AIApiResult.Success(
                    data = "腾讯混元响应: $responseBody",
                    tokensUsed = maxTokens,
                    model = model
                )
            } else {
                AIApiResult.Error("腾讯混元API请求失败: ${response.status}")
            }
        } catch (e: Exception) {
            AIApiResult.Error("腾讯混元API调用失败: ${e.message}")
        }
    }
}

/**
 * AI API结果密封类
 */
sealed class AIApiResult<T> {
    data class Success<T>(
        val data: T,
        val tokensUsed: Int,
        val model: String
    ) : AIApiResult<T>()
    
    data class Error<T>(
        val message: String,
        val code: String? = null
    ) : AIApiResult<T>()
}
