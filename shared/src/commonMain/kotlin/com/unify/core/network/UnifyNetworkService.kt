package com.unify.core.network

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * 统一网络服务接口 - 跨平台网络通信核心服务
 * 提供RESTful API调用、实时数据流、批量请求等高级功能
 */
interface UnifyNetworkService {
    /**
     * 执行HTTP请求
     */
    suspend fun <T> request(request: NetworkRequest<T>): NetworkResponse<T>

    /**
     * 批量请求
     */
    suspend fun batchRequest(requests: List<NetworkRequest<*>>): List<NetworkResponse<*>>

    /**
     * 创建实时数据流
     */
    fun <T> createDataStream(
        url: String,
        interval: Long = 5000L,
        parser: (String) -> T,
    ): Flow<T>

    /**
     * WebSocket连接
     */
    suspend fun connectWebSocket(
        url: String,
        protocols: List<String> = emptyList(),
    ): WebSocketConnection

    /**
     * 服务器推送事件 (SSE)
     */
    fun createServerSentEventStream(url: String): Flow<ServerSentEvent>

    /**
     * GraphQL查询
     */
    suspend fun graphqlQuery(
        url: String,
        query: String,
        variables: Map<String, Any> = emptyMap(),
    ): NetworkResponse<GraphQLResponse>

    /**
     * 获取请求统计信息
     */
    fun getRequestStats(): RequestStats

    /**
     * 设置全局请求拦截器
     */
    fun setRequestInterceptor(interceptor: RequestInterceptor)

    /**
     * 设置全局响应拦截器
     */
    fun setResponseInterceptor(interceptor: ResponseInterceptor)
}

/**
 * 网络请求封装
 */
@Serializable
data class NetworkRequest<T>(
    val method: HttpMethod,
    val url: String,
    val headers: Map<String, String> = emptyMap(),
    val body: String? = null,
    val timeout: Long = 30000L,
    val priority: RequestPriority = RequestPriority.NORMAL,
    val cacheStrategy: CacheStrategy = CacheStrategy.NETWORK_FIRST,
    val retryPolicy: RetryPolicy = RetryPolicy(),
    val tag: String? = null,
)

/**
 * HTTP方法
 */
enum class HttpMethod {
    GET,
    POST,
    PUT,
    DELETE,
    PATCH,
    HEAD,
    OPTIONS,
}

/**
 * 重试策略
 */
@Serializable
data class RetryPolicy(
    val maxRetries: Int = 3,
    val baseDelay: Long = 1000L,
    val maxDelay: Long = 10000L,
    val backoffMultiplier: Double = 2.0,
    val retryOnConnectionFailure: Boolean = true,
    val retryOnTimeout: Boolean = true,
)

/**
 * WebSocket连接接口
 */
interface WebSocketConnection {
    val isConnected: Boolean

    suspend fun send(message: String)

    suspend fun send(data: ByteArray)

    suspend fun close(
        code: Int = 1000,
        reason: String = "",
    )

    fun onMessage(callback: (String) -> Unit)

    fun onBinaryMessage(callback: (ByteArray) -> Unit)

    fun onError(callback: (Throwable) -> Unit)

    fun onClose(callback: (Int, String) -> Unit)
}

/**
 * 服务器推送事件
 */
@Serializable
data class ServerSentEvent(
    val id: String? = null,
    val event: String? = null,
    val data: String,
    val retry: Long? = null,
)

/**
 * GraphQL响应
 */
@Serializable
data class GraphQLResponse(
    val data: JsonObject? = null,
    val errors: List<GraphQLError>? = null,
    val extensions: JsonObject? = null,
)

/**
 * GraphQL错误
 */
@Serializable
data class GraphQLError(
    val message: String,
    val locations: List<GraphQLLocation>? = null,
    val path: List<String>? = null,
    val extensions: JsonObject? = null,
)

/**
 * GraphQL位置
 */
@Serializable
data class GraphQLLocation(
    val line: Int,
    val column: Int,
)

/**
 * 请求统计信息
 */
@Serializable
data class RequestStats(
    val totalRequests: Long = 0L,
    val successfulRequests: Long = 0L,
    val failedRequests: Long = 0L,
    val averageResponseTime: Double = 0.0,
    val cacheHitRate: Double = 0.0,
    val bytesTransferred: Long = 0L,
    val activeConnections: Int = 0,
)

/**
 * 请求拦截器
 */
interface RequestInterceptor {
    suspend fun intercept(request: NetworkRequest<*>): NetworkRequest<*>
}

/**
 * 响应拦截器
 */
interface ResponseInterceptor {
    suspend fun <T> intercept(response: NetworkResponse<T>): NetworkResponse<T>
}
