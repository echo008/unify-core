package com.unify.network

import com.unify.core.network.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.*
import java.net.*
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume

/**
 * Desktop平台UnifyNetworkManager实现
 * 基于Java标准库的HTTP客户端
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
    private val webSocketConnections = ConcurrentHashMap<String, WebSocketConnection>()
    
    // 缓存管理
    private val responseCache = ConcurrentHashMap<String, CachedResponse>()
    
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
                val file = File(filePath)
                if (!file.exists()) {
                    continuation.resume(
                        NetworkResponse(
                            statusCode = -1,
                            body = "",
                            headers = emptyMap(),
                            isSuccess = false,
                            error = "File not found: $filePath"
                        )
                    )
                    return@suspendCancellableCoroutine
                }
                
                val connection = createConnection("POST", url, headers)
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/octet-stream")
                connection.setRequestProperty("Content-Length", file.length().toString())
                
                // 上传文件
                connection.outputStream.use { output ->
                    file.inputStream().use { input ->
                        input.copyTo(output)
                    }
                }
                
                val response = readResponse(connection)
                continuation.resume(response)
                
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
                val connection = createConnection("GET", url, headers)
                val response = readResponse(connection)
                
                if (response.isSuccess) {
                    // 保存文件
                    val saveFile = File(savePath)
                    saveFile.parentFile?.mkdirs()
                    
                    connection.inputStream.use { input ->
                        saveFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
                
                continuation.resume(response)
                
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
        // 简化的WebSocket实现，实际应用中需要使用专门的WebSocket库
        val connection = object : WebSocketConnection {
            private var connected = false
            
            override fun send(message: String) {
                if (connected) {
                    // 发送消息的实现
                }
            }
            
            override fun close() {
                connected = false
                webSocketConnections.remove(url)
                listener.onClose(1000, "Normal closure")
            }
            
            override fun isConnected(): Boolean = connected
        }
        
        webSocketConnections[url] = connection
        
        // 模拟连接成功
        listener.onOpen()
        
        return connection
    }
    
    override fun observeNetworkStatus(): Flow<NetworkStatus> = _networkStatus.asStateFlow()
    
    override fun isNetworkAvailable(): Boolean {
        return try {
            val url = URL("https://www.google.com")
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.requestMethod = "HEAD"
            connection.responseCode == HttpURLConnection.HTTP_OK
        } catch (e: Exception) {
            false
        }
    }
    
    override fun getNetworkType(): NetworkType {
        return if (isNetworkAvailable()) {
            // 在Desktop环境中，通常是以太网或WiFi
            NetworkType.ETHERNET
        } else {
            NetworkType.UNKNOWN
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
            var lastException: Exception? = null
            
            while (attempt <= retryPolicy.maxRetries) {
                try {
                    val cacheKey = "$method:$url:${body?.hashCode()}"
                    
                    // 检查缓存
                    if (cacheEnabled && method == "GET") {
                        responseCache[cacheKey]?.let { cachedResponse ->
                            if (!cachedResponse.isExpired()) {
                                continuation.resume(cachedResponse.response)
                                return@suspendCancellableCoroutine
                            }
                        }
                    }
                    
                    val connection = createConnection(method, url, headers)
                    
                    // 发送请求体
                    if (body != null && (method == "POST" || method == "PUT" || method == "PATCH")) {
                        connection.doOutput = true
                        connection.setRequestProperty("Content-Type", "application/json")
                        connection.outputStream.use { output ->
                            output.write(body.toByteArray())
                        }
                    }
                    
                    val response = readResponse(connection)
                    
                    // 缓存响应
                    if (cacheEnabled && method == "GET" && response.isSuccess) {
                        responseCache[cacheKey] = CachedResponse(response, System.currentTimeMillis() + 300000) // 5分钟缓存
                    }
                    
                    continuation.resume(response)
                    return@suspendCancellableCoroutine
                    
                } catch (e: Exception) {
                    lastException = e
                    attempt++
                    
                    if (attempt <= retryPolicy.maxRetries) {
                        // 等待重试
                        Thread.sleep(retryPolicy.retryDelayMillis * attempt)
                    }
                }
            }
            
            // 所有重试都失败了
            continuation.resume(
                NetworkResponse(
                    statusCode = -1,
                    body = "",
                    headers = emptyMap(),
                    isSuccess = false,
                    error = lastException?.message ?: "Request failed after ${retryPolicy.maxRetries} retries"
                )
            )
        }
    }
    
    private fun createConnection(method: String, url: String, headers: Map<String, String>): HttpURLConnection {
        val fullUrl = if (url.startsWith("http")) url else "$baseUrl$url"
        val connection = URL(fullUrl).openConnection() as HttpURLConnection
        
        connection.requestMethod = method
        connection.connectTimeout = timeoutMillis.toInt()
        connection.readTimeout = timeoutMillis.toInt()
        
        // 设置默认头部
        defaultHeaders.forEach { (key, value) ->
            connection.setRequestProperty(key, value)
        }
        
        // 设置请求特定头部
        headers.forEach { (key, value) ->
            connection.setRequestProperty(key, value)
        }
        
        return connection
    }
    
    private fun readResponse(connection: HttpURLConnection): NetworkResponse {
        val statusCode = connection.responseCode
        val isSuccess = statusCode in 200..299
        
        val responseHeaders = connection.headerFields.mapNotNull { (key, values) ->
            key?.let { it to (values.firstOrNull() ?: "") }
        }.toMap()
        
        val body = try {
            if (isSuccess) {
                connection.inputStream.bufferedReader().readText()
            } else {
                connection.errorStream?.bufferedReader()?.readText() ?: ""
            }
        } catch (e: Exception) {
            ""
        }
        
        return NetworkResponse(
            statusCode = statusCode,
            body = body,
            headers = responseHeaders,
            isSuccess = isSuccess,
            error = if (!isSuccess) connection.responseMessage else null
        )
    }
    
    private fun getCurrentNetworkStatus(): NetworkStatus {
        return if (isNetworkAvailable()) NetworkStatus.CONNECTED else NetworkStatus.DISCONNECTED
    }
    
    private data class CachedResponse(
        val response: NetworkResponse,
        val expiryTime: Long
    ) {
        fun isExpired(): Boolean = System.currentTimeMillis() > expiryTime
    }
}

actual object UnifyNetworkManagerFactory {
    actual fun create(): UnifyNetworkManager {
        return UnifyNetworkManagerImpl()
    }
}
