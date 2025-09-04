package com.unify.network

import com.unify.core.network.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.browser.window
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.w3c.dom.WebSocket
import org.w3c.dom.events.Event
import org.w3c.fetch.*
import kotlin.coroutines.resume
import kotlin.js.Promise

/**
 * Web/JS平台UnifyNetworkManager实现
 * 基于Fetch API和WebSocket
 */
class UnifyNetworkManagerImpl : UnifyNetworkManager {
    private var baseUrl: String = ""
    private var defaultHeaders: Map<String, String> = emptyMap()
    private var timeoutMillis: Long = 30000L
    private var cacheEnabled: Boolean = false
    private var retryPolicy: RetryPolicy = RetryPolicy()
    
    // 网络状态监控
    private val _networkStatus = MutableStateFlow(getCurrentNetworkStatus())
    
    // WebSocket连接管理
    private val webSocketConnections = mutableMapOf<String, WebSocketConnection>()
    
    // 缓存管理
    private val responseCache = mutableMapOf<String, CachedResponse>()
    
    init {
        // 监听网络状态变化
        window.addEventListener("online") { _networkStatus.value = NetworkStatus.CONNECTED }
        window.addEventListener("offline") { _networkStatus.value = NetworkStatus.DISCONNECTED }
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
                // 在Web环境中，filePath通常是File对象的引用或blob URL
                val fileInput = js("document.querySelector('input[type=\"file\"]')") as? org.w3c.dom.HTMLInputElement
                val file = fileInput?.files?.get(0)
                
                if (file != null) {
                    val formData = js("new FormData()")
                    formData.asDynamic().append("file", file)
                    
                    val requestInit = RequestInit(
                        method = "POST",
                        body = formData.asDynamic(),
                        headers = createHeaders(headers)
                    )
                    
                    window.fetch(resolveUrl(url), requestInit).then({ response ->
                        processResponse(response).then({ networkResponse ->
                            continuation.resume(networkResponse)
                        })
                    }).catch({ error ->
                        continuation.resume(
                            NetworkResponse(
                                statusCode = -1,
                                body = "",
                                headers = emptyMap(),
                                isSuccess = false,
                                error = error.toString()
                            )
                        )
                    })
                } else {
                    continuation.resume(
                        NetworkResponse(
                            statusCode = -1,
                            body = "",
                            headers = emptyMap(),
                            isSuccess = false,
                            error = "No file selected"
                        )
                    )
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
            val requestInit = RequestInit(
                method = "GET",
                headers = createHeaders(headers)
            )
            
            window.fetch(resolveUrl(url), requestInit).then({ response ->
                if (response.ok) {
                    response.blob().then({ blob ->
                        // 创建下载链接
                        val downloadUrl = js("URL.createObjectURL(blob)") as String
                        val link = js("document.createElement('a')") as org.w3c.dom.HTMLAnchorElement
                        link.href = downloadUrl
                        link.download = savePath.substringAfterLast('/')
                        js("document.body.appendChild(link)")
                        link.click()
                        js("document.body.removeChild(link)")
                        js("URL.revokeObjectURL(downloadUrl)")
                        
                        continuation.resume(
                            NetworkResponse(
                                statusCode = response.status.toInt(),
                                body = "File downloaded successfully",
                                headers = extractHeaders(response),
                                isSuccess = true
                            )
                        )
                    })
                } else {
                    continuation.resume(
                        NetworkResponse(
                            statusCode = response.status.toInt(),
                            body = "",
                            headers = extractHeaders(response),
                            isSuccess = false,
                            error = response.statusText
                        )
                    )
                }
            }).catch({ error ->
                continuation.resume(
                    NetworkResponse(
                        statusCode = -1,
                        body = "",
                        headers = emptyMap(),
                        isSuccess = false,
                        error = error.toString()
                    )
                )
            })
        }
    }
    
    override fun connectWebSocket(url: String, listener: WebSocketListener): WebSocketConnection {
        val webSocket = WebSocket(url)
        
        val connection = object : WebSocketConnection {
            private var connected = false
            
            override fun send(message: String) {
                if (connected && webSocket.readyState == WebSocket.OPEN) {
                    webSocket.send(message)
                }
            }
            
            override fun close() {
                connected = false
                webSocket.close()
                webSocketConnections.remove(url)
            }
            
            override fun isConnected(): Boolean = connected && webSocket.readyState == WebSocket.OPEN
        }
        
        webSocket.onopen = { 
            connection.asDynamic().connected = true
            listener.onOpen()
        }
        
        webSocket.onmessage = { event ->
            val data = event.asDynamic().data
            listener.onMessage(data.toString())
        }
        
        webSocket.onerror = { event ->
            listener.onError("WebSocket error occurred")
        }
        
        webSocket.onclose = { event ->
            connection.asDynamic().connected = false
            val closeEvent = event.asDynamic()
            listener.onClose(closeEvent.code as Int, closeEvent.reason as String)
            webSocketConnections.remove(url)
        }
        
        webSocketConnections[url] = connection
        return connection
    }
    
    override fun observeNetworkStatus(): Flow<NetworkStatus> = _networkStatus.asStateFlow()
    
    override fun isNetworkAvailable(): Boolean = window.navigator.onLine
    
    override fun getNetworkType(): NetworkType {
        val connection = js("navigator.connection") 
        return if (connection != null) {
            when (connection.asDynamic().effectiveType as? String) {
                "4g" -> NetworkType.CELLULAR
                "3g" -> NetworkType.CELLULAR
                "2g" -> NetworkType.CELLULAR
                "slow-2g" -> NetworkType.CELLULAR
                else -> NetworkType.WIFI
            }
        } else {
            if (window.navigator.onLine) NetworkType.WIFI else NetworkType.UNKNOWN
        }
    }
    
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
        // 清除浏览器缓存（如果可能）
        if (js("'caches' in window") as Boolean) {
            js("caches.keys().then(names => names.forEach(name => caches.delete(name)))")
        }
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
                
                val requestInit = RequestInit(
                    method = method,
                    headers = createHeaders(headers),
                    body = body
                )
                
                // 设置超时
                val timeoutId = window.setTimeout({
                    continuation.resume(
                        NetworkResponse(
                            statusCode = -1,
                            body = "",
                            headers = emptyMap(),
                            isSuccess = false,
                            error = "Request timeout"
                        )
                    )
                }, timeoutMillis.toInt())
                
                window.fetch(resolveUrl(url), requestInit).then({ response ->
                    window.clearTimeout(timeoutId)
                    processResponse(response).then({ networkResponse ->
                        if (!networkResponse.isSuccess && attempt < retryPolicy.maxRetries) {
                            attempt++
                            window.setTimeout({
                                attemptRequest()
                            }, (retryPolicy.retryDelayMillis * attempt).toInt())
                        } else {
                            // 缓存响应
                            if (cacheEnabled && method == "GET" && networkResponse.isSuccess) {
                                responseCache[cacheKey] = CachedResponse(
                                    networkResponse, 
                                    js("Date.now()") as Long + 300000 // 5分钟缓存
                                )
                            }
                            continuation.resume(networkResponse)
                        }
                    })
                }).catch({ error ->
                    window.clearTimeout(timeoutId)
                    if (attempt < retryPolicy.maxRetries) {
                        attempt++
                        window.setTimeout({
                            attemptRequest()
                        }, (retryPolicy.retryDelayMillis * attempt).toInt())
                    } else {
                        continuation.resume(
                            NetworkResponse(
                                statusCode = -1,
                                body = "",
                                headers = emptyMap(),
                                isSuccess = false,
                                error = error.toString()
                            )
                        )
                    }
                })
            }
            
            attemptRequest()
        }
    }
    
    private fun createHeaders(additionalHeaders: Map<String, String>): dynamic {
        val headers = js("new Headers()")
        
        // 添加默认头部
        defaultHeaders.forEach { (key, value) ->
            headers.append(key, value)
        }
        
        // 添加请求特定头部
        additionalHeaders.forEach { (key, value) ->
            headers.append(key, value)
        }
        
        return headers
    }
    
    private fun processResponse(response: Response): Promise<NetworkResponse> {
        return response.text().then({ body ->
            NetworkResponse(
                statusCode = response.status.toInt(),
                body = body,
                headers = extractHeaders(response),
                isSuccess = response.ok,
                error = if (!response.ok) response.statusText else null
            )
        })
    }
    
    private fun extractHeaders(response: Response): Map<String, String> {
        val headers = mutableMapOf<String, String>()
        val responseHeaders = response.headers
        
        // 使用forEach遍历headers
        responseHeaders.asDynamic().forEach { value: String, key: String ->
            headers[key] = value
        }
        
        return headers
    }
    
    private fun resolveUrl(url: String): String {
        return if (url.startsWith("http")) url else "$baseUrl$url"
    }
    
    private fun getCurrentNetworkStatus(): NetworkStatus {
        return if (window.navigator.onLine) NetworkStatus.CONNECTED else NetworkStatus.DISCONNECTED
    }
    
    private data class CachedResponse(
        val response: NetworkResponse,
        val expiryTime: Long
    ) {
        fun isExpired(): Boolean = js("Date.now()") as Long > expiryTime
    }
}

actual object UnifyNetworkManagerFactory {
    actual fun create(): UnifyNetworkManager {
        return UnifyNetworkManagerImpl()
    }
}
