package com.unify.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay

/**
 * 统一网络服务实现
 * 基于Ktor实现的跨平台网络服务
 */
class UnifyNetworkServiceImpl(
    private val httpClient: HttpClient,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }
) : UnifyNetworkService {

    override suspend fun <T> get(
        url: String,
        headers: Map<String, String>,
        queryParams: Map<String, String>
    ): NetworkResult<T> {
        return executeRequest {
            httpClient.get(url) {
                headers.forEach { (key, value) ->
                    header(key, value)
                }
                queryParams.forEach { (key, value) ->
                    parameter(key, value)
                }
            }
        }
    }

    override suspend fun <T> post(
        url: String,
        body: Any?,
        headers: Map<String, String>
    ): NetworkResult<T> {
        return executeRequest {
            httpClient.post(url) {
                headers.forEach { (key, value) ->
                    header(key, value)
                }
                contentType(ContentType.Application.Json)
                body?.let { 
                    setBody(json.encodeToString(it))
                }
            }
        }
    }

    override suspend fun <T> put(
        url: String,
        body: Any?,
        headers: Map<String, String>
    ): NetworkResult<T> {
        return executeRequest {
            httpClient.put(url) {
                headers.forEach { (key, value) ->
                    header(key, value)
                }
                contentType(ContentType.Application.Json)
                body?.let { 
                    setBody(json.encodeToString(it))
                }
            }
        }
    }

    override suspend fun <T> delete(
        url: String,
        headers: Map<String, String>
    ): NetworkResult<T> {
        return executeRequest {
            httpClient.delete(url) {
                headers.forEach { (key, value) ->
                    header(key, value)
                }
            }
        }
    }

    override fun <T> getStream(
        url: String,
        headers: Map<String, String>
    ): Flow<NetworkResult<T>> = flow {
        try {
            // 模拟流式数据获取
            var retryCount = 0
            while (retryCount < MAX_RETRY_COUNT) {
                val result = get<T>(url, headers)
                emit(result)
                
                if (result is NetworkResult.Success) {
                    delay(STREAM_INTERVAL_MS)
                } else {
                    retryCount++
                    delay(RETRY_DELAY_MS * retryCount)
                }
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error(e.message ?: "Stream error", e))
        }
    }

    override suspend fun uploadFile(
        url: String,
        filePath: String,
        headers: Map<String, String>,
        onProgress: ((Float) -> Unit)?
    ): NetworkResult<String> {
        return try {
            val response = httpClient.post(url) {
                headers.forEach { (key, value) ->
                    header(key, value)
                }
                // 文件上传实现需要平台特定代码
                // 这里提供基本框架
            }
            
            if (response.status.isSuccess()) {
                NetworkResult.Success(response.bodyAsText())
            } else {
                NetworkResult.Error("Upload failed: ${response.status}", null)
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Upload error", e)
        }
    }

    override suspend fun downloadFile(
        url: String,
        destinationPath: String,
        headers: Map<String, String>,
        onProgress: ((Float) -> Unit)?
    ): NetworkResult<String> {
        return try {
            val response = httpClient.get(url) {
                headers.forEach { (key, value) ->
                    header(key, value)
                }
            }
            
            if (response.status.isSuccess()) {
                // 文件下载实现需要平台特定代码
                // 这里提供基本框架
                NetworkResult.Success(destinationPath)
            } else {
                NetworkResult.Error("Download failed: ${response.status}", null)
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Download error", e)
        }
    }

    private suspend inline fun <T> executeRequest(
        crossinline request: suspend () -> HttpResponse
    ): NetworkResult<T> {
        return try {
            val response = request()
            val responseBody = response.bodyAsText()
            
            when {
                response.status.isSuccess() -> {
                    try {
                        @Suppress("UNCHECKED_CAST")
                        val data = if (responseBody.isBlank()) {
                            Unit as T
                        } else {
                            json.decodeFromString<T>(responseBody)
                        }
                        NetworkResult.Success(data)
                    } catch (e: Exception) {
                        NetworkResult.Error("JSON parsing error: ${e.message}", e)
                    }
                }
                response.status.value in 400..499 -> {
                    NetworkResult.Error("Client error: ${response.status}", null)
                }
                response.status.value in 500..599 -> {
                    NetworkResult.Error("Server error: ${response.status}", null)
                }
                else -> {
                    NetworkResult.Error("Unknown error: ${response.status}", null)
                }
            }
        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "Network error", e)
        }
    }

    companion object {
        private const val MAX_RETRY_COUNT = 3
        private const val STREAM_INTERVAL_MS = 5000L
        private const val RETRY_DELAY_MS = 1000L
    }
}

/**
 * 网络客户端工厂
 */
object NetworkClientFactory {
    fun createHttpClient(): HttpClient {
        return HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    encodeDefaults = true
                })
            }
            
            install(Logging) {
                level = LogLevel.INFO
            }
            
            // 请求超时配置
            engine {
                // 平台特定配置将在各平台实现中添加
            }
        }
    }
}

/**
 * 网络拦截器接口
 */
interface NetworkInterceptor {
    suspend fun intercept(request: HttpRequestBuilder): HttpRequestBuilder
    suspend fun interceptResponse(response: HttpResponse): HttpResponse
}

/**
 * 认证拦截器
 */
class AuthInterceptor(
    private val tokenProvider: () -> String?
) : NetworkInterceptor {
    
    override suspend fun intercept(request: HttpRequestBuilder): HttpRequestBuilder {
        tokenProvider()?.let { token ->
            request.header("Authorization", "Bearer $token")
        }
        return request
    }
    
    override suspend fun interceptResponse(response: HttpResponse): HttpResponse {
        return response
    }
}

/**
 * 缓存拦截器
 */
class CacheInterceptor(
    private val cacheProvider: NetworkCacheProvider
) : NetworkInterceptor {
    
    override suspend fun intercept(request: HttpRequestBuilder): HttpRequestBuilder {
        // 检查缓存
        return request
    }
    
    override suspend fun interceptResponse(response: HttpResponse): HttpResponse {
        // 存储响应到缓存
        return response
    }
}

/**
 * 网络缓存提供者接口
 */
interface NetworkCacheProvider {
    suspend fun get(key: String): String?
    suspend fun put(key: String, value: String, ttl: Long? = null)
    suspend fun remove(key: String)
    suspend fun clear()
}
