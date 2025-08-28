package com.unify.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * 统一网络服务
 * 基于文档第8章要求实现的网络与存储系统
 */

/**
 * 网络服务接口
 */
interface UnifyNetworkService {
    suspend fun <T> get(
        url: String,
        headers: Map<String, String> = emptyMap(),
        queryParams: Map<String, String> = emptyMap()
    ): NetworkResult<T>
    
    suspend fun <T> post(
        url: String,
        body: Any? = null,
        headers: Map<String, String> = emptyMap()
    ): NetworkResult<T>
    
    suspend fun <T> put(
        url: String,
        body: Any? = null,
        headers: Map<String, String> = emptyMap()
    ): NetworkResult<T>
    
    suspend fun <T> delete(
        url: String,
        headers: Map<String, String> = emptyMap()
    ): NetworkResult<T>
    
    fun <T> getStream(
        url: String,
        headers: Map<String, String> = emptyMap()
    ): Flow<NetworkResult<T>>
    
    suspend fun uploadFile(
        url: String,
        filePath: String,
        headers: Map<String, String> = emptyMap(),
        onProgress: ((Float) -> Unit)? = null
    ): NetworkResult<String>
    
    suspend fun downloadFile(
        url: String,
        destinationPath: String,
        headers: Map<String, String> = emptyMap(),
        onProgress: ((Float) -> Unit)? = null
    ): NetworkResult<String>
}

/**
 * 网络结果封装
 */
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val exception: NetworkException) : NetworkResult<Nothing>()
    object Loading : NetworkResult<Nothing>()
}

/**
 * 网络异常
 */
sealed class NetworkException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class ConnectionError(message: String, cause: Throwable? = null) : NetworkException(message, cause)
    class TimeoutError(message: String, cause: Throwable? = null) : NetworkException(message, cause)
    class ServerError(val code: Int, message: String) : NetworkException("Server error $code: $message")
    class ClientError(val code: Int, message: String) : NetworkException("Client error $code: $message")
    class ParseError(message: String, cause: Throwable? = null) : NetworkException(message, cause)
    class UnknownError(message: String, cause: Throwable? = null) : NetworkException(message, cause)
}

/**
 * HTTP方法枚举
 */
enum class HttpMethod {
    GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS
}

/**
 * 网络请求配置
 */
@Serializable
data class NetworkConfig(
    val baseUrl: String,
    val timeout: Long = 30000L,
    val retryCount: Int = 3,
    val retryDelay: Long = 1000L,
    val enableLogging: Boolean = true,
    val enableCache: Boolean = true,
    val cacheMaxAge: Long = 300000L, // 5分钟
    val defaultHeaders: Map<String, String> = emptyMap()
)

/**
 * 网络请求
 */
data class NetworkRequest(
    val method: HttpMethod,
    val url: String,
    val headers: Map<String, String> = emptyMap(),
    val queryParams: Map<String, String> = emptyMap(),
    val body: Any? = null,
    val timeout: Long? = null,
    val retryCount: Int? = null
)

/**
 * 网络响应
 */
data class NetworkResponse(
    val statusCode: Int,
    val headers: Map<String, String>,
    val body: String,
    val isSuccessful: Boolean = statusCode in 200..299
)

/**
 * 网络拦截器接口
 */
interface NetworkInterceptor {
    suspend fun intercept(request: NetworkRequest): NetworkRequest
    suspend fun interceptResponse(response: NetworkResponse): NetworkResponse
}

/**
 * 认证拦截器
 */
class AuthInterceptor(
    private val tokenProvider: () -> String?
) : NetworkInterceptor {
    
    override suspend fun intercept(request: NetworkRequest): NetworkRequest {
        val token = tokenProvider()
        return if (token != null) {
            request.copy(
                headers = request.headers + ("Authorization" to "Bearer $token")
            )
        } else {
            request
        }
    }
    
    override suspend fun interceptResponse(response: NetworkResponse): NetworkResponse {
        return response
    }
}

/**
 * 日志拦截器
 */
class LoggingInterceptor : NetworkInterceptor {
    override suspend fun intercept(request: NetworkRequest): NetworkRequest {
        println("Network Request: ${request.method} ${request.url}")
        if (request.body != null) {
            println("Request Body: ${request.body}")
        }
        return request
    }
    
    override suspend fun interceptResponse(response: NetworkResponse): NetworkResponse {
        println("Network Response: ${response.statusCode} - ${response.body.take(200)}")
        return response
    }
}

/**
 * 缓存拦截器
 */
class CacheInterceptor(
    private val cache: NetworkCache
) : NetworkInterceptor {
    
    override suspend fun intercept(request: NetworkRequest): NetworkRequest {
        // GET请求检查缓存
        if (request.method == HttpMethod.GET) {
            val cachedResponse = cache.get(request.url)
            if (cachedResponse != null && !cache.isExpired(request.url)) {
                // 返回缓存的响应（这里需要特殊处理）
            }
        }
        return request
    }
    
    override suspend fun interceptResponse(response: NetworkResponse): NetworkResponse {
        // 缓存成功的GET响应
        if (response.isSuccessful && response.statusCode == 200) {
            cache.put(response.headers["request-url"] ?: "", response)
        }
        return response
    }
}

/**
 * 网络缓存接口
 */
interface NetworkCache {
    suspend fun get(key: String): NetworkResponse?
    suspend fun put(key: String, response: NetworkResponse)
    suspend fun remove(key: String)
    suspend fun clear()
    suspend fun isExpired(key: String): Boolean
}

/**
 * 内存网络缓存实现
 */
class MemoryNetworkCache(
    private val maxAge: Long = 300000L // 5分钟
) : NetworkCache {
    
    private data class CacheEntry(
        val response: NetworkResponse,
        val timestamp: Long
    )
    
    private val cache = mutableMapOf<String, CacheEntry>()
    
    override suspend fun get(key: String): NetworkResponse? {
        val entry = cache[key]
        return if (entry != null && !isExpired(key)) {
            entry.response
        } else {
            cache.remove(key)
            null
        }
    }
    
    override suspend fun put(key: String, response: NetworkResponse) {
        cache[key] = CacheEntry(response, System.currentTimeMillis())
    }
    
    override suspend fun remove(key: String) {
        cache.remove(key)
    }
    
    override suspend fun clear() {
        cache.clear()
    }
    
    override suspend fun isExpired(key: String): Boolean {
        val entry = cache[key] ?: return true
        return System.currentTimeMillis() - entry.timestamp > maxAge
    }
}

/**
 * 网络监控器
 */
class NetworkMonitor {
    private val metrics = mutableMapOf<String, NetworkMetrics>()
    
    fun recordRequest(url: String, method: HttpMethod, duration: Long, success: Boolean) {
        val existing = metrics[url] ?: NetworkMetrics()
        metrics[url] = existing.copy(
            totalRequests = existing.totalRequests + 1,
            successfulRequests = if (success) existing.successfulRequests + 1 else existing.successfulRequests,
            totalDuration = existing.totalDuration + duration,
            averageDuration = (existing.totalDuration + duration) / (existing.totalRequests + 1)
        )
    }
    
    fun getMetrics(url: String): NetworkMetrics? = metrics[url]
    
    fun getAllMetrics(): Map<String, NetworkMetrics> = metrics.toMap()
}

/**
 * 网络指标
 */
data class NetworkMetrics(
    val totalRequests: Int = 0,
    val successfulRequests: Int = 0,
    val totalDuration: Long = 0,
    val averageDuration: Long = 0
) {
    val successRate: Float get() = if (totalRequests > 0) successfulRequests.toFloat() / totalRequests else 0f
}

/**
 * 网络工厂
 */
object NetworkFactory {
    fun createNetworkService(
        config: NetworkConfig,
        interceptors: List<NetworkInterceptor> = emptyList(),
        cache: NetworkCache? = null
    ): UnifyNetworkService {
        return UnifyNetworkServiceImpl(config, interceptors, cache)
    }
}

/**
 * 网络服务实现（平台特定实现将在各平台模块中提供）
 */
expect class UnifyNetworkServiceImpl(
    config: NetworkConfig,
    interceptors: List<NetworkInterceptor>,
    cache: NetworkCache?
) : UnifyNetworkService

/**
 * JSON序列化配置
 */
val networkJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = false
    prettyPrint = false
}

/**
 * 网络工具类
 */
object NetworkUtils {
    fun buildUrl(baseUrl: String, path: String, queryParams: Map<String, String> = emptyMap()): String {
        val url = if (baseUrl.endsWith("/") && path.startsWith("/")) {
            baseUrl + path.substring(1)
        } else if (!baseUrl.endsWith("/") && !path.startsWith("/")) {
            "$baseUrl/$path"
        } else {
            baseUrl + path
        }
        
        return if (queryParams.isNotEmpty()) {
            val queryString = queryParams.entries.joinToString("&") { "${it.key}=${it.value}" }
            "$url?$queryString"
        } else {
            url
        }
    }
    
    fun parseErrorResponse(response: NetworkResponse): String {
        return try {
            val errorJson = networkJson.parseToJsonElement(response.body)
            errorJson.jsonObject["message"]?.toString()?.removeSurrounding("\"") 
                ?: "Unknown error"
        } catch (e: Exception) {
            response.body.ifEmpty { "Unknown error" }
        }
    }
}
