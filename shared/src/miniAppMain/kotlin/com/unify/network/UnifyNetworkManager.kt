package com.unify.network

import com.unify.core.network.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * 小程序平台UnifyNetworkManager实现
 * 基于小程序网络API (wx.request, wx.uploadFile等)
 */
class UnifyNetworkManagerImpl : UnifyNetworkManager {
    private var baseUrl: String = ""
    private var defaultHeaders: Map<String, String> = emptyMap()
    private var timeoutMillis: Long = 60000L // 小程序默认60秒超时
    private var cacheEnabled: Boolean = false
    private var retryPolicy: RetryPolicy = RetryPolicy()
    
    // 网络状态监控
    private val _networkStatus = MutableStateFlow(getCurrentNetworkStatus())
    
    // WebSocket连接管理
    private val webSocketConnections = mutableMapOf<String, WebSocketConnection>()
    
    // 缓存管理
    private val responseCache = mutableMapOf<String, CachedResponse>()
    
    // 小程序网络管理器
    private val miniAppNetworkManager = MiniAppNetworkManager()
    
    init {
        // 监听小程序网络状态变化
        miniAppNetworkManager.onNetworkStatusChange { status ->
            _networkStatus.value = status
        }
    }
    
    override suspend fun get(url: String, headers: Map<String, String>): NetworkResponse {
        return executeRequest("GET", url, null, headers)
    }
    
    override suspend fun post(url: String, body: String, headers: Map<String, String>): NetworkResponse {
        return executeRequest("POST", url, body, headers)
    }
    
    override suspend fun put(url: String, body: String, headers: Map<String, String>): NetworkResponse {
        return executeRequest("PUT", url, body, headers)
    }
    
    override suspend fun delete(url: String, headers: Map<String, String>): NetworkResponse {
        return executeRequest("DELETE", url, null, headers)
    }
    
    override suspend fun patch(url: String, body: String, headers: Map<String, String>): NetworkResponse {
        return executeRequest("PATCH", url, body, headers)
    }
    
    override suspend fun uploadFile(url: String, filePath: String, headers: Map<String, String>): NetworkResponse {
        return suspendCancellableCoroutine { continuation ->
            try {
                miniAppNetworkManager.uploadFile(
                    url = resolveUrl(url),
                    filePath = filePath,
                    headers = mergeHeaders(headers),
                    timeout = timeoutMillis
                ) { response ->
                    continuation.resume(response)
                }
            } catch (e: Exception) {
                continuation.resume(
                    NetworkResponse(
                        statusCode = -1,
                        body = "",
                        headers = emptyMap(),
                        isSuccess = false,
                        error = e.message
                    )
                )
            }
        }
    }
    
    override suspend fun downloadFile(url: String, savePath: String, headers: Map<String, String>): NetworkResponse {
        return suspendCancellableCoroutine { continuation ->
            try {
                miniAppNetworkManager.downloadFile(
                    url = resolveUrl(url),
                    savePath = savePath,
                    headers = mergeHeaders(headers),
                    timeout = timeoutMillis
                ) { response ->
                    continuation.resume(response)
                }
            } catch (e: Exception) {
                continuation.resume(
                    NetworkResponse(
                        statusCode = -1,
                        body = "",
                        headers = emptyMap(),
                        isSuccess = false,
                        error = e.message
                    )
                )
            }
        }
    }
    
    override fun connectWebSocket(url: String, listener: WebSocketListener): WebSocketConnection {
        val connection = miniAppNetworkManager.connectWebSocket(url, listener)
        webSocketConnections[url] = connection
        return connection
    }
    
    override fun observeNetworkStatus(): Flow<NetworkStatus> = _networkStatus.asStateFlow()
    
    override fun isNetworkAvailable(): Boolean = miniAppNetworkManager.isNetworkAvailable()
    
    override fun getNetworkType(): NetworkType = miniAppNetworkManager.getNetworkType()
    
    override fun setBaseUrl(baseUrl: String) {
        this.baseUrl = baseUrl
    }
    
    override fun getBaseUrl(): String = baseUrl
    
    override fun setDefaultHeaders(headers: Map<String, String>) {
        this.defaultHeaders = headers
    }
    
    override fun getDefaultHeaders(): Map<String, String> = defaultHeaders
    
    override fun setTimeout(timeoutMillis: Long) {
        this.timeoutMillis = timeoutMillis
    }
    
    override fun getTimeout(): Long = timeoutMillis
    
    override fun setCacheEnabled(enabled: Boolean) {
        this.cacheEnabled = enabled
    }
    
    override fun isCacheEnabled(): Boolean = cacheEnabled
    
    override fun clearCache() {
        responseCache.clear()
    }
    
    override fun setRetryPolicy(policy: RetryPolicy) {
        this.retryPolicy = policy
    }
    
    override fun getRetryPolicy(): RetryPolicy = retryPolicy
    
    private suspend fun executeRequest(
        method: String,
        url: String,
        body: String?,
        headers: Map<String, String>
    ): NetworkResponse {
        return suspendCancellableCoroutine { continuation ->
            var attempt = 0
            
            fun attemptRequest() {
                val cacheKey = "$method:$url:${body?.hashCode()}"
                
                // 检查缓存
                if (cacheEnabled && method == "GET") {
                    responseCache[cacheKey]?.let { cachedResponse ->
                        if (!cachedResponse.isExpired()) {
                            continuation.resume(cachedResponse.response)
                            return
                        }
                    }
                }
                
                try {
                    miniAppNetworkManager.request(
                        method = method,
                        url = resolveUrl(url),
                        data = body,
                        headers = mergeHeaders(headers),
                        timeout = timeoutMillis
                    ) { response ->
                        if (!response.isSuccess && attempt < retryPolicy.maxRetries) {
                            attempt++
                            // 延迟重试
                            miniAppNetworkManager.setTimeout(retryPolicy.retryDelayMillis * attempt) {
                                attemptRequest()
                            }
                        } else {
                            // 缓存响应
                            if (cacheEnabled && method == "GET" && response.isSuccess) {
                                responseCache[cacheKey] = CachedResponse(
                                    response, 
                                    System.currentTimeMillis() + 300000 // 5分钟缓存
                                )
                            }
                            continuation.resume(response)
                        }
                    }
                } catch (e: Exception) {
                    if (attempt < retryPolicy.maxRetries) {
                        attempt++
                        miniAppNetworkManager.setTimeout(retryPolicy.retryDelayMillis * attempt) {
                            attemptRequest()
                        }
                    } else {
                        continuation.resume(
                            NetworkResponse(
                                statusCode = -1,
                                body = "",
                                headers = emptyMap(),
                                isSuccess = false,
                                error = e.message
                            )
                        )
                    }
                }
            }
            
            attemptRequest()
        }
    }
    
    private fun mergeHeaders(additionalHeaders: Map<String, String>): Map<String, String> {
        return defaultHeaders + additionalHeaders
    }
    
    private fun resolveUrl(url: String): String {
        return if (url.startsWith("http")) url else "$baseUrl$url"
    }
    
    private fun getCurrentNetworkStatus(): NetworkStatus {
        return if (miniAppNetworkManager.isNetworkAvailable()) {
            NetworkStatus.CONNECTED
        } else {
            NetworkStatus.DISCONNECTED
        }
    }
    
    private data class CachedResponse(
        val response: NetworkResponse,
        val expiryTime: Long
    ) {
        fun isExpired(): Boolean = System.currentTimeMillis() > expiryTime
    }
}

// 小程序网络管理器模拟实现
private class MiniAppNetworkManager {
    private var networkStatusCallback: ((NetworkStatus) -> Unit)? = null
    
    fun onNetworkStatusChange(callback: (NetworkStatus) -> Unit) {
        this.networkStatusCallback = callback
        // 实际实现中会调用: wx.onNetworkStatusChange(callback)
    }
    
    fun request(
        method: String,
        url: String,
        data: String?,
        headers: Map<String, String>,
        timeout: Long,
        callback: (NetworkResponse) -> Unit
    ) {
        // 实际实现中会调用小程序API: wx.request()
        try {
            val response = NetworkResponse(
                statusCode = 200,
                body = "Mini-app network response",
                headers = mapOf("Content-Type" to "application/json"),
                isSuccess = true
            )
            callback(response)
        } catch (e: Exception) {
            callback(
                NetworkResponse(
                    statusCode = -1,
                    body = "",
                    headers = emptyMap(),
                    isSuccess = false,
                    error = e.message
                )
            )
        }
    }
    
    fun uploadFile(
        url: String,
        filePath: String,
        headers: Map<String, String>,
        timeout: Long,
        callback: (NetworkResponse) -> Unit
    ) {
        // 实际实现中会调用小程序API: wx.uploadFile()
        try {
            val response = NetworkResponse(
                statusCode = 200,
                body = "File uploaded successfully",
                headers = emptyMap(),
                isSuccess = true
            )
            callback(response)
        } catch (e: Exception) {
            callback(
                NetworkResponse(
                    statusCode = -1,
                    body = "",
                    headers = emptyMap(),
                    isSuccess = false,
                    error = e.message
                )
            )
        }
    }
    
    fun downloadFile(
        url: String,
        savePath: String,
        headers: Map<String, String>,
        timeout: Long,
        callback: (NetworkResponse) -> Unit
    ) {
        // 实际实现中会调用小程序API: wx.downloadFile()
        try {
            val response = NetworkResponse(
                statusCode = 200,
                body = savePath,
                headers = emptyMap(),
                isSuccess = true
            )
            callback(response)
        } catch (e: Exception) {
            callback(
                NetworkResponse(
                    statusCode = -1,
                    body = "",
                    headers = emptyMap(),
                    isSuccess = false,
                    error = e.message
                )
            )
        }
    }
    
    fun connectWebSocket(url: String, listener: WebSocketListener): WebSocketConnection {
        // 实际实现中会调用小程序API: wx.connectSocket()
        return object : WebSocketConnection {
            private var connected = false
            
            override fun send(message: String) {
                if (connected) {
                    // 实际实现中会调用: wx.sendSocketMessage()
                }
            }
            
            override fun close() {
                connected = false
                // 实际实现中会调用: wx.closeSocket()
                listener.onClose(1000, "Connection closed")
            }
            
            override fun isConnected(): Boolean = connected
            
            init {
                // 模拟连接建立
                connected = true
                listener.onOpen()
            }
        }
    }
    
    fun isNetworkAvailable(): Boolean {
        // 实际实现中会调用小程序API: wx.getNetworkType()
        return true
    }
    
    fun getNetworkType(): NetworkType {
        // 实际实现中会调用小程序API: wx.getNetworkType()
        return NetworkType.WIFI
    }
    
    fun setTimeout(delayMillis: Long, action: () -> Unit) {
        // 实际实现中会使用小程序定时器: setTimeout()
        action()
    }
}

actual object UnifyNetworkManagerFactory {
    actual fun create(): UnifyNetworkManager {
        return UnifyNetworkManagerImpl()
    }
}
