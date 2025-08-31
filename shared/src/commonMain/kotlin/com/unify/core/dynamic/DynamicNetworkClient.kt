package com.unify.core.dynamic

import kotlinx.coroutines.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

/**
 * 动态网络客户端
 * 负责与远程服务器通信，获取配置、组件和更新信息
 */
class DynamicNetworkClient {
    private val httpClient = createHttpClient()
    private val requestTimeout = 30000L // 30秒超时
    private val maxRetryCount = 3
    
    /**
     * 检查更新
     */
    suspend fun checkUpdate(
        configUrl: String,
        apiKey: String,
        request: UpdateCheckRequest
    ): UpdateCheckResponse {
        return withTimeout(requestTimeout) {
            retry(maxRetryCount) {
                val response = httpClient.post("$configUrl/api/v1/check-update") {
                    headers {
                        append("Authorization", "Bearer $apiKey")
                        append("Content-Type", "application/json")
                    }
                    setBody(Json.encodeToString(request))
                }
                
                if (response.status.isSuccess()) {
                    val responseBody = response.bodyAsText()
                    Json.decodeFromString<UpdateCheckResponse>(responseBody)
                } else {
                    throw NetworkException("检查更新失败: ${response.status}")
                }
            }
        }
    }
    
    /**
     * 下载更新包
     */
    suspend fun downloadPackage(downloadUrl: String, apiKey: String): ByteArray {
        return withTimeout(requestTimeout * 3) { // 下载允许更长时间
            retry(maxRetryCount) {
                val response = httpClient.get(downloadUrl) {
                    headers {
                        append("Authorization", "Bearer $apiKey")
                    }
                }
                
                if (response.status.isSuccess()) {
                    response.readBytes()
                } else {
                    throw NetworkException("下载更新包失败: ${response.status}")
                }
            }
        }
    }
    
    /**
     * 获取配置列表
     */
    suspend fun fetchConfigurations(configUrl: String, apiKey: String): Map<String, String> {
        return withTimeout(requestTimeout) {
            retry(maxRetryCount) {
                val response = httpClient.get("$configUrl/api/v1/configurations") {
                    headers {
                        append("Authorization", "Bearer $apiKey")
                    }
                }
                
                if (response.status.isSuccess()) {
                    val responseBody = response.bodyAsText()
                    Json.decodeFromString<Map<String, String>>(responseBody)
                } else {
                    throw NetworkException("获取配置失败: ${response.status}")
                }
            }
        }
    }
    
    /**
     * 获取单个配置
     */
    suspend fun fetchConfig(configUrl: String, apiKey: String, key: String): String? {
        return withTimeout(requestTimeout) {
            try {
                retry(maxRetryCount) {
                    val response = httpClient.get("$configUrl/api/v1/configuration/$key") {
                        headers {
                            append("Authorization", "Bearer $apiKey")
                        }
                    }
                    
                    if (response.status.isSuccess()) {
                        val responseBody = response.bodyAsText()
                        val configResponse = Json.decodeFromString<ConfigResponse>(responseBody)
                        configResponse.value
                    } else if (response.status == HttpStatusCode.NotFound) {
                        null
                    } else {
                        throw NetworkException("获取配置失败: ${response.status}")
                    }
                }
            } catch (e: Exception) {
                UnifyPerformanceMonitor.recordMetric("network_config_fetch_error", 1.0, "count",
                    mapOf("key" to key, "error" to e.message.orEmpty()))
                null
            }
        }
    }
    
    /**
     * 获取组件
     */
    suspend fun fetchComponent(configUrl: String, apiKey: String, componentId: String): ComponentData {
        return withTimeout(requestTimeout) {
            retry(maxRetryCount) {
                val response = httpClient.get("$configUrl/api/v1/component/$componentId") {
                    headers {
                        append("Authorization", "Bearer $apiKey")
                    }
                }
                
                if (response.status.isSuccess()) {
                    val responseBody = response.bodyAsText()
                    Json.decodeFromString<ComponentData>(responseBody)
                } else {
                    throw NetworkException("获取组件失败: ${response.status}")
                }
            }
        }
    }
    
    /**
     * 上传使用统计
     */
    suspend fun uploadUsageStats(configUrl: String, apiKey: String, stats: UsageStats): Boolean {
        return try {
            withTimeout(requestTimeout) {
                val response = httpClient.post("$configUrl/api/v1/usage-stats") {
                    headers {
                        append("Authorization", "Bearer $apiKey")
                        append("Content-Type", "application/json")
                    }
                    setBody(Json.encodeToString(stats))
                }
                
                response.status.isSuccess()
            }
        } catch (e: Exception) {
            UnifyPerformanceMonitor.recordMetric("network_stats_upload_error", 1.0, "count",
                mapOf("error" to e.message.orEmpty()))
            false
        }
    }
    
    /**
     * 上传错误报告
     */
    suspend fun uploadErrorReport(configUrl: String, apiKey: String, errorReport: ErrorReport): Boolean {
        return try {
            withTimeout(requestTimeout) {
                val response = httpClient.post("$configUrl/api/v1/error-report") {
                    headers {
                        append("Authorization", "Bearer $apiKey")
                        append("Content-Type", "application/json")
                    }
                    setBody(Json.encodeToString(errorReport))
                }
                
                response.status.isSuccess()
            }
        } catch (e: Exception) {
            UnifyPerformanceMonitor.recordMetric("network_error_report_upload_error", 1.0, "count",
                mapOf("error" to e.message.orEmpty()))
            false
        }
    }
    
    /**
     * 验证API密钥
     */
    suspend fun validateApiKey(configUrl: String, apiKey: String): Boolean {
        return try {
            withTimeout(requestTimeout) {
                val response = httpClient.get("$configUrl/api/v1/validate") {
                    headers {
                        append("Authorization", "Bearer $apiKey")
                    }
                }
                
                response.status.isSuccess()
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 重试机制
     */
    private suspend fun <T> retry(maxRetries: Int, block: suspend () -> T): T {
        var lastException: Exception? = null
        
        repeat(maxRetries) { attempt ->
            try {
                return block()
            } catch (e: Exception) {
                lastException = e
                if (attempt < maxRetries - 1) {
                    val delay = (1000L * (attempt + 1)) // 递增延迟
                    delay(delay)
                    
                    UnifyPerformanceMonitor.recordMetric("network_retry_attempt", 1.0, "count",
                        mapOf("attempt" to (attempt + 1).toString()))
                }
            }
        }
        
        throw lastException ?: NetworkException("重试失败")
    }
    
    /**
     * 创建HTTP客户端
     */
    private fun createHttpClient(): HttpClient {
        return HttpClient {
            // 配置HTTP客户端
            expectSuccess = false
            
            // 添加请求拦截器
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    coerceInputValues = true
                })
            }
            
            // 添加日志插件
            install(io.ktor.client.plugins.logging.Logging) {
                level = io.ktor.client.plugins.logging.LogLevel.INFO
            }
            
            // 添加超时配置
            install(io.ktor.client.plugins.HttpTimeout) {
                requestTimeoutMillis = requestTimeout
                connectTimeoutMillis = 10000L
                socketTimeoutMillis = requestTimeout
            }
        }
    }
}

/**
 * 配置响应
 */
@Serializable
data class ConfigResponse(
    val key: String,
    val value: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 使用统计
 */
@Serializable
data class UsageStats(
    val deviceId: String,
    val platform: String,
    val version: String,
    val componentUsage: Map<String, Int> = emptyMap(),
    val featureUsage: Map<String, Int> = emptyMap(),
    val performanceMetrics: Map<String, Double> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 错误报告
 */
@Serializable
data class ErrorReport(
    val deviceId: String,
    val platform: String,
    val version: String,
    val errorType: String,
    val errorMessage: String,
    val stackTrace: String,
    val context: Map<String, String> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 网络异常
 */
class NetworkException(message: String, cause: Throwable? = null) : Exception(message, cause)
