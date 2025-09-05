package com.unify.core.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Unify跨平台网络服务
 * 支持8大平台的统一网络通信
 */
interface UnifyNetworkService {
    suspend fun get(url: String, headers: Map<String, String> = emptyMap()): NetworkResult<String>
    suspend fun post(url: String, body: String, headers: Map<String, String> = emptyMap()): NetworkResult<String>
    suspend fun put(url: String, body: String, headers: Map<String, String> = emptyMap()): NetworkResult<String>
    suspend fun delete(url: String, headers: Map<String, String> = emptyMap()): NetworkResult<String>
    suspend fun download(url: String, onProgress: (Float) -> Unit = {}): NetworkResult<ByteArray>
    suspend fun upload(url: String, data: ByteArray, onProgress: (Float) -> Unit = {}): NetworkResult<String>
    fun getNetworkStatus(): Flow<NetworkStatus>
}

/**
 * 网络请求结果封装
 */
@Serializable
sealed class NetworkResult<out T> {
    @Serializable
    data class Success<T>(val data: T) : NetworkResult<T>()
    
    @Serializable
    data class Error(val exception: NetworkException) : NetworkResult<Nothing>()
    
    @Serializable
    data class Loading(val progress: Float = 0f) : NetworkResult<Nothing>()
}

/**
 * 网络异常定义
 */
@Serializable
data class NetworkException(
    val code: Int,
    override val message: String,
    val causeMessage: String? = null
) : Exception(message)

/**
 * 网络状态枚举
 */
@Serializable
enum class NetworkStatus {
    CONNECTED,
    DISCONNECTED,
    CONNECTING,
    WIFI,
    MOBILE,
    ETHERNET,
    UNKNOWN
}

/**
 * 网络配置
 */
@Serializable
data class NetworkConfig(
    val timeout: Long = 30000L,
    val retryCount: Int = 3,
    val retryDelay: Long = 1000L,
    val enableLogging: Boolean = false,
    val enableCache: Boolean = true,
    val cacheSize: Long = 10 * 1024 * 1024L, // 10MB
    val userAgent: String = "UnifyApp/1.0",
    val baseUrl: String = "",
    val defaultHeaders: Map<String, String> = emptyMap()
)

/**
 * Unify网络服务实现
 */
class UnifyNetworkServiceImpl(
    private val client: HttpClient,
    private val config: NetworkConfig = NetworkConfig()
) : UnifyNetworkService {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = false
    }
    
    override suspend fun get(url: String, headers: Map<String, String>): NetworkResult<String> {
        return executeRequest {
            client.get(buildUrl(url)) {
                applyHeaders(headers)
            }
        }
    }
    
    override suspend fun post(url: String, body: String, headers: Map<String, String>): NetworkResult<String> {
        return executeRequest {
            client.post(buildUrl(url)) {
                applyHeaders(headers)
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        }
    }
    
    override suspend fun put(url: String, body: String, headers: Map<String, String>): NetworkResult<String> {
        return executeRequest {
            client.put(buildUrl(url)) {
                applyHeaders(headers)
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        }
    }
    
    override suspend fun delete(url: String, headers: Map<String, String>): NetworkResult<String> {
        return executeRequest {
            client.delete(buildUrl(url)) {
                applyHeaders(headers)
            }
        }
    }
    
    override suspend fun download(url: String, onProgress: (Float) -> Unit): NetworkResult<ByteArray> {
        return try {
            val response = client.get(buildUrl(url))
            val data = response.body<ByteArray>()
            onProgress(1.0f)
            NetworkResult.Success(data)
        } catch (e: Exception) {
            NetworkResult.Error(NetworkException(0, e.message ?: "下载失败", e.toString()))
        }
    }
    
    override suspend fun upload(url: String, data: ByteArray, onProgress: (Float) -> Unit): NetworkResult<String> {
        return try {
            val response = client.post(buildUrl(url)) {
                contentType(ContentType.Application.OctetStream)
                setBody(data)
            }
            onProgress(1.0f)
            NetworkResult.Success(response.bodyAsText())
        } catch (e: Exception) {
            NetworkResult.Error(NetworkException(0, e.message ?: "上传失败", e.toString()))
        }
    }
    
    override fun getNetworkStatus(): Flow<NetworkStatus> = flow {
        emit(getCurrentNetworkStatus())
    }
    
    private suspend fun executeRequest(request: suspend () -> HttpResponse): NetworkResult<String> {
        return try {
            val response = request()
            when (response.status.value) {
                in 200..299 -> NetworkResult.Success(response.bodyAsText())
                else -> NetworkResult.Error(
                    NetworkException(
                        response.status.value,
                        response.status.description
                    )
                )
            }
        } catch (e: Exception) {
            NetworkResult.Error(
                NetworkException(
                    0,
                    e.message ?: "网络请求失败",
                    e.toString()
                )
            )
        }
    }
    
    private fun HttpRequestBuilder.applyHeaders(headers: Map<String, String>) {
        config.defaultHeaders.forEach { (key, value) ->
            header(key, value)
        }
        headers.forEach { (key, value) ->
            header(key, value)
        }
        header("User-Agent", config.userAgent)
    }
    
    private fun buildUrl(url: String): String {
        return if (url.startsWith("http")) {
            url
        } else {
            "${config.baseUrl.trimEnd('/')}/${url.trimStart('/')}"
        }
    }
    
}

// 平台特定的网络状态获取函数
internal expect fun getCurrentNetworkStatus(): NetworkStatus

/**
 * 网络工具类
 */
object UnifyNetworkUtils {
    /**
     * 检查URL是否有效
     */
    fun isValidUrl(url: String): Boolean {
        return try {
            Url(url)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 构建查询参数
     */
    fun buildQueryParams(params: Map<String, String>): String {
        return params.entries.joinToString("&") { (key, value) ->
            "$key=${value.encodeURLParameter()}"
        }
    }
    
    /**
     * 解析Content-Type
     */
    fun parseContentType(contentType: String): ContentType {
        return try {
            ContentType.parse(contentType)
        } catch (e: Exception) {
            ContentType.Application.Json
        }
    }
}

// UnifyNetworkCache 接口已在 NetworkServiceFactory.kt 中定义，此处移除重复声明

/**
 * 网络拦截器接口
 */
interface UnifyNetworkInterceptor {
    suspend fun intercept(request: HttpRequestBuilder): HttpRequestBuilder
    suspend fun interceptResponse(response: HttpResponse): HttpResponse
}

// NetworkEvent 密封类已在 NetworkServiceFactory.kt 中定义，此处移除重复声明

// NetworkConnectionInfo 数据类已在 NetworkServiceFactory.kt 中定义，此处移除重复声明
// 扩展版本保留额外字段
@Serializable
data class ExtendedNetworkConnectionInfo(
    val isConnected: Boolean,
    val networkType: NetworkType,
    val signalStrength: Int = 0,
    val bandwidth: Long = 0L,
    val ipAddress: String? = null
)

/**
 * 网络监控接口
 */
interface UnifyNetworkMonitor {
    fun observeNetworkEvents(): Flow<NetworkEvent>
    fun onRequestStart(url: String, method: String)
    fun onRequestComplete(url: String, statusCode: Int, duration: Long)
    fun onRequestError(url: String, error: String?)
    fun stopMonitoring()
    fun getCurrentNetworkInfo(): NetworkConnectionInfo
}
