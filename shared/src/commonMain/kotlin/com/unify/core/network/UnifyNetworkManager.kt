package com.unify.core.network

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

/**
 * 统一网络管理器接口
 * 提供跨平台一致的网络通信功能
 */
interface UnifyNetworkManager {
    
    /**
     * 获取HTTP客户端
     */
    val httpClient: UnifyHttpClient
    
    /**
     * 获取网络监控器
     */
    val networkMonitor: UnifyNetworkMonitor
    
    /**
     * 获取WebSocket客户端
     */
    val webSocketClient: UnifyWebSocketClient
    
    /**
     * 初始化网络管理器
     */
    suspend fun initialize()
    
    /**
     * 设置全局请求头
     */
    fun setGlobalHeaders(headers: Map<String, String>)
    
    /**
     * 设置全局超时配置
     */
    fun setGlobalTimeout(config: TimeoutConfig)
    
    /**
     * 设置全局重试配置
     */
    fun setGlobalRetryPolicy(policy: RetryPolicy)
    
    /**
     * 设置网络拦截器
     */
    fun addInterceptor(interceptor: NetworkInterceptor)
    
    /**
     * 获取网络统计信息
     */
    suspend fun getNetworkStats(): NetworkStats
    
    /**
     * 清理网络缓存
     */
    suspend fun clearNetworkCache()
}

/**
 * HTTP客户端接口
 */
interface UnifyHttpClient {
    
    /**
     * GET请求
     */
    suspend fun get(
        url: String,
        headers: Map<String, String> = emptyMap(),
        params: Map<String, String> = emptyMap()
    ): HttpResponse
    
    /**
     * POST请求
     */
    suspend fun post(
        url: String,
        body: String? = null,
        headers: Map<String, String> = emptyMap()
    ): HttpResponse
    
    /**
     * PUT请求
     */
    suspend fun put(
        url: String,
        body: String? = null,
        headers: Map<String, String> = emptyMap()
    ): HttpResponse
    
    /**
     * DELETE请求
     */
    suspend fun delete(
        url: String,
        headers: Map<String, String> = emptyMap()
    ): HttpResponse
    
    /**
     * PATCH请求
     */
    suspend fun patch(
        url: String,
        body: String? = null,
        headers: Map<String, String> = emptyMap()
    ): HttpResponse
    
    /**
     * 上传文件
     */
    suspend fun upload(
        url: String,
        files: List<FileUpload>,
        headers: Map<String, String> = emptyMap(),
        onProgress: ((Float) -> Unit)? = null
    ): HttpResponse
    
    /**
     * 下载文件
     */
    suspend fun download(
        url: String,
        destination: String,
        headers: Map<String, String> = emptyMap(),
        onProgress: ((Float) -> Unit)? = null
    ): DownloadResult
}

/**
 * 网络监控器接口
 */
interface UnifyNetworkMonitor {
    
    /**
     * 当前网络状态
     */
    val networkStatus: Flow<NetworkStatus>
    
    /**
     * 当前连接类型
     */
    val connectionType: Flow<ConnectionType>
    
    /**
     * 网络速度
     */
    val networkSpeed: Flow<NetworkSpeed>
    
    /**
     * 检查网络连接
     */
    suspend fun checkConnectivity(): Boolean
    
    /**
     * Ping测试
     */
    suspend fun ping(host: String, timeout: Long = 5000): PingResult
    
    /**
     * 网络质量测试
     */
    suspend fun testNetworkQuality(): NetworkQuality
}

/**
 * WebSocket客户端接口
 */
interface UnifyWebSocketClient {
    
    /**
     * 连接WebSocket
     */
    suspend fun connect(url: String, protocols: List<String> = emptyList()): WebSocketConnection
    
    /**
     * 断开连接
     */
    suspend fun disconnect(connection: WebSocketConnection)
    
    /**
     * 发送消息
     */
    suspend fun sendMessage(connection: WebSocketConnection, message: String)
    
    /**
     * 发送二进制数据
     */
    suspend fun sendBinary(connection: WebSocketConnection, data: ByteArray)
    
    /**
     * 监听消息
     */
    fun observeMessages(connection: WebSocketConnection): Flow<WebSocketMessage>
    
    /**
     * 监听连接状态
     */
    fun observeConnectionState(connection: WebSocketConnection): Flow<WebSocketState>
}

/**
 * HTTP响应
 */
@Serializable
data class HttpResponse(
    val statusCode: Int,
    val headers: Map<String, String>,
    val body: String,
    val isSuccessful: Boolean,
    val error: String? = null,
    val responseTime: Long = 0
)

/**
 * 文件上传
 */
data class FileUpload(
    val fieldName: String,
    val fileName: String,
    val mimeType: String,
    val data: ByteArray
)

/**
 * 下载结果
 */
@Serializable
data class DownloadResult(
    val success: Boolean,
    val filePath: String? = null,
    val fileSize: Long = 0,
    val error: String? = null
)

/**
 * 网络状态
 */
enum class NetworkStatus {
    CONNECTED,
    DISCONNECTED,
    CONNECTING,
    UNKNOWN
}

/**
 * 连接类型
 */
enum class ConnectionType {
    WIFI,
    CELLULAR,
    ETHERNET,
    BLUETOOTH,
    VPN,
    UNKNOWN
}

/**
 * 网络速度
 */
@Serializable
data class NetworkSpeed(
    val downloadSpeed: Float, // Mbps
    val uploadSpeed: Float,   // Mbps
    val latency: Long         // ms
)

/**
 * Ping结果
 */
@Serializable
data class PingResult(
    val success: Boolean,
    val latency: Long = 0,
    val packetLoss: Float = 0f,
    val error: String? = null
)

/**
 * 网络质量
 */
enum class NetworkQuality {
    EXCELLENT,
    GOOD,
    FAIR,
    POOR,
    UNKNOWN
}

/**
 * WebSocket连接
 */
data class WebSocketConnection(
    val id: String,
    val url: String,
    val state: WebSocketState
)

/**
 * WebSocket消息
 */
sealed class WebSocketMessage {
    data class Text(val content: String) : WebSocketMessage()
    data class Binary(val data: ByteArray) : WebSocketMessage()
    object Ping : WebSocketMessage()
    object Pong : WebSocketMessage()
}

/**
 * WebSocket状态
 */
enum class WebSocketState {
    CONNECTING,
    CONNECTED,
    DISCONNECTING,
    DISCONNECTED,
    ERROR
}

/**
 * 超时配置
 */
@Serializable
data class TimeoutConfig(
    val connectTimeout: Long = 30000,  // ms
    val readTimeout: Long = 30000,     // ms
    val writeTimeout: Long = 30000     // ms
)

/**
 * 重试策略
 */
@Serializable
data class RetryPolicy(
    val maxRetries: Int = 3,
    val retryDelay: Long = 1000,       // ms
    val backoffMultiplier: Float = 2.0f,
    val maxRetryDelay: Long = 10000    // ms
)

/**
 * 网络拦截器
 */
interface NetworkInterceptor {
    suspend fun intercept(request: NetworkRequest): NetworkRequest
    suspend fun onResponse(response: HttpResponse): HttpResponse
    suspend fun onError(error: NetworkError): NetworkError
}

/**
 * 网络请求
 */
data class NetworkRequest(
    val url: String,
    val method: String,
    val headers: Map<String, String>,
    val body: String? = null
)

/**
 * 网络错误
 */
@Serializable
data class NetworkError(
    val code: Int,
    val message: String,
    val type: ErrorType,
    val retryable: Boolean = false
)

/**
 * 错误类型
 */
enum class ErrorType {
    TIMEOUT,
    CONNECTION_ERROR,
    SSL_ERROR,
    DNS_ERROR,
    HTTP_ERROR,
    UNKNOWN
}

/**
 * 网络统计
 */
@Serializable
data class NetworkStats(
    val totalRequests: Long,
    val successfulRequests: Long,
    val failedRequests: Long,
    val averageResponseTime: Long,
    val totalDataTransferred: Long,
    val cacheHitRate: Float
)

/**
 * 网络管理器实现类
 * 使用expect/actual机制实现跨平台功能
 */
expect class UnifyNetworkManagerImpl : UnifyNetworkManager
