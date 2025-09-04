package com.unify.core.network

import kotlinx.coroutines.flow.Flow

/**
 * Unify跨平台网络管理器接口
 * 统一管理HTTP请求、WebSocket连接和网络状态
 */
interface UnifyNetworkManager {
    // HTTP请求
    suspend fun get(url: String, headers: Map<String, String> = emptyMap()): NetworkResponse
    suspend fun post(url: String, body: String, headers: Map<String, String> = emptyMap()): NetworkResponse
    suspend fun put(url: String, body: String, headers: Map<String, String> = emptyMap()): NetworkResponse
    suspend fun delete(url: String, headers: Map<String, String> = emptyMap()): NetworkResponse
    suspend fun patch(url: String, body: String, headers: Map<String, String> = emptyMap()): NetworkResponse
    
    // 文件上传下载
    suspend fun uploadFile(url: String, filePath: String, headers: Map<String, String> = emptyMap()): NetworkResponse
    suspend fun downloadFile(url: String, savePath: String, headers: Map<String, String> = emptyMap()): NetworkResponse
    
    // WebSocket连接
    fun connectWebSocket(url: String, listener: WebSocketListener): WebSocketConnection
    
    // 网络状态监控
    fun observeNetworkStatus(): Flow<NetworkStatus>
    fun isNetworkAvailable(): Boolean
    fun getNetworkType(): NetworkType
    
    // 请求配置
    fun setBaseUrl(baseUrl: String)
    fun getBaseUrl(): String
    fun setDefaultHeaders(headers: Map<String, String>)
    fun getDefaultHeaders(): Map<String, String>
    fun setTimeout(timeoutMillis: Long)
    fun getTimeout(): Long
    
    // 缓存管理
    fun setCacheEnabled(enabled: Boolean)
    fun isCacheEnabled(): Boolean
    fun clearCache()
    
    // 重试机制
    fun setRetryPolicy(policy: RetryPolicy)
    fun getRetryPolicy(): RetryPolicy
}

/**
 * 网络响应数据类
 */
data class NetworkResponse(
    val statusCode: Int,
    val body: String,
    val headers: Map<String, String>,
    val isSuccess: Boolean,
    val error: String? = null
)

/**
 * WebSocket监听器接口
 */
interface WebSocketListener {
    fun onOpen()
    fun onMessage(message: String)
    fun onError(error: String)
    fun onClose(code: Int, reason: String)
}

/**
 * WebSocket连接接口
 */
interface WebSocketConnection {
    fun send(message: String)
    fun close()
    fun isConnected(): Boolean
}

/**
 * 网络状态枚举
 */
enum class NetworkStatus {
    CONNECTED,
    DISCONNECTED,
    CONNECTING
}

/**
 * 网络类型枚举
 */
enum class NetworkType {
    WIFI,
    CELLULAR,
    ETHERNET,
    UNKNOWN
}

/**
 * 重试策略数据类
 */
data class RetryPolicy(
    val maxRetries: Int = 3,
    val retryDelayMillis: Long = 1000,
    val backoffMultiplier: Float = 2.0f
)

/**
 * 网络管理器工厂
 */
expect object UnifyNetworkManagerFactory {
    fun create(): UnifyNetworkManager
}
