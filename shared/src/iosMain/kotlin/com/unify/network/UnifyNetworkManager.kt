package com.unify.network

import com.unify.core.network.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.*
import platform.Network.*
import kotlin.coroutines.resume

/**
 * iOS平台UnifyNetworkManager实现
 * 基于NSURLSession和Network framework
 */
class UnifyNetworkManagerImpl : UnifyNetworkManager {
    private val urlSession = NSURLSession.sharedSession
    private val networkMonitor = nw_path_monitor_create()
    
    private var baseUrl: String = ""
    private var defaultHeaders: Map<String, String> = emptyMap()
    private var timeoutMillis: Long = 30000L
    private var cacheEnabled: Boolean = false
    private var retryPolicy: RetryPolicy = RetryPolicy()
    
    // 网络状态监控
    private val _networkStatus = MutableStateFlow(getCurrentNetworkStatus())
    
    // WebSocket连接管理
    private val webSocketConnections = mutableMapOf<String, WebSocketConnection>()
    
    init {
        setupNetworkMonitoring()
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
            val fileUrl = NSURL.fileURLWithPath(filePath)
            val request = createNSURLRequest("POST", url, headers)
            
            val uploadTask = urlSession.uploadTaskWithRequest(request, fileUrl) { data, response, error ->
                val networkResponse = processResponse(data, response, error)
                continuation.resume(networkResponse)
            }
            
            uploadTask.resume()
        }
    }
    
    override suspend fun downloadFile(url: String, savePath: String, headers: Map<String, String>): NetworkResponse {
        return suspendCancellableCoroutine { continuation ->
            val request = createNSURLRequest("GET", url, headers)
            
            val downloadTask = urlSession.downloadTaskWithRequest(request) { tempUrl, response, error ->
                val networkResponse = processResponse(null, response, error)
                
                if (networkResponse.isSuccess && tempUrl != null) {
                    // 移动文件到目标位置
                    val fileManager = NSFileManager.defaultManager
                    val destinationUrl = NSURL.fileURLWithPath(savePath)
                    
                    try {
                        fileManager.moveItemAtURL(tempUrl, destinationUrl, null)
                    } catch (e: Exception) {
                        continuation.resume(
                            networkResponse.copy(
                                isSuccess = false,
                                error = "Failed to save file: ${e.message}"
                            )
                        )
                        return@downloadTaskWithRequest
                    }
                }
                
                continuation.resume(networkResponse)
            }
            
            downloadTask.resume()
        }
    }
    
    override fun connectWebSocket(url: String, listener: WebSocketListener): WebSocketConnection {
        val webSocketUrl = NSURL.URLWithString(url)!!
        val request = NSURLRequest.requestWithURL(webSocketUrl)
        val webSocketTask = urlSession.webSocketTaskWithRequest(request)
        
        val connection = object : WebSocketConnection {
            private var connected = true
            
            override fun send(message: String) {
                if (connected) {
                    val webSocketMessage = NSURLSessionWebSocketMessage.messageWithString(message)
                    webSocketTask.sendMessage(webSocketMessage) { error ->
                        error?.let {
                            listener.onError(it.localizedDescription)
                        }
                    }
                }
            }
            
            override fun close() {
                connected = false
                webSocketTask.cancelWithCloseCode(1000, null)
                webSocketConnections.remove(url)
                listener.onClose(1000, "Normal closure")
            }
            
            override fun isConnected(): Boolean = connected
        }
        
        webSocketConnections[url] = connection
        
        // 开始接收消息
        receiveWebSocketMessage(webSocketTask, listener)
        
        webSocketTask.resume()
        listener.onOpen()
        
        return connection
    }
    
    override fun observeNetworkStatus(): Flow<NetworkStatus> = _networkStatus.asStateFlow()
    
    override fun isNetworkAvailable(): Boolean {
        // 使用Network framework检查网络状态
        return true // 简化实现
    }
    
    override fun getNetworkType(): NetworkType {
        // 使用Network framework获取网络类型
        return NetworkType.WIFI // 简化实现
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
        NSURLCache.sharedURLCache.removeAllCachedResponses()
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
                val request = createNSURLRequest(method, url, headers, body)
                
                val dataTask = urlSession.dataTaskWithRequest(request) { data, response, error ->
                    val networkResponse = processResponse(data, response, error)
                    
                    if (!networkResponse.isSuccess && attempt < retryPolicy.maxRetries) {
                        attempt++
                        // 延迟重试
                        val delay = retryPolicy.retryDelayMillis * attempt
                        dispatch_after(
                            dispatch_time(DISPATCH_TIME_NOW, (delay * 1_000_000).toLong()),
                            dispatch_get_main_queue()
                        ) {
                            attemptRequest()
                        }
                    } else {
                        continuation.resume(networkResponse)
                    }
                }
                
                dataTask.resume()
            }
            
            attemptRequest()
        }
    }
    
    private fun createNSURLRequest(
        method: String,
        url: String,
        headers: Map<String, String>,
        body: String? = null
    ): NSMutableURLRequest {
        val fullUrl = if (url.startsWith("http")) url else "$baseUrl$url"
        val nsUrl = NSURL.URLWithString(fullUrl)!!
        val request = NSMutableURLRequest.requestWithURL(nsUrl)
        
        request.HTTPMethod = method
        request.timeoutInterval = (timeoutMillis / 1000.0)
        
        // 设置默认头部
        defaultHeaders.forEach { (key, value) ->
            request.setValue(value, key)
        }
        
        // 设置请求特定头部
        headers.forEach { (key, value) ->
            request.setValue(value, key)
        }
        
        // 设置请求体
        if (body != null && (method == "POST" || method == "PUT" || method == "PATCH")) {
            request.HTTPBody = body.toNSData()
            if (!headers.containsKey("Content-Type")) {
                request.setValue("application/json", "Content-Type")
            }
        }
        
        return request
    }
    
    private fun processResponse(data: NSData?, response: NSURLResponse?, error: NSError?): NetworkResponse {
        if (error != null) {
            return NetworkResponse(
                statusCode = -1,
                body = "",
                headers = emptyMap(),
                isSuccess = false,
                error = error.localizedDescription
            )
        }
        
        val httpResponse = response as? NSHTTPURLResponse
        val statusCode = httpResponse?.statusCode?.toInt() ?: -1
        val isSuccess = statusCode in 200..299
        
        val responseHeaders = httpResponse?.allHeaderFields?.mapNotNull { (key, value) ->
            (key as? String)?.let { it to (value as? String ?: "") }
        }?.toMap() ?: emptyMap()
        
        val body = data?.let { NSString.create(it, NSUTF8StringEncoding) as? String } ?: ""
        
        return NetworkResponse(
            statusCode = statusCode,
            body = body,
            headers = responseHeaders,
            isSuccess = isSuccess,
            error = if (!isSuccess) "HTTP $statusCode" else null
        )
    }
    
    private fun receiveWebSocketMessage(webSocketTask: NSURLSessionWebSocketTask, listener: WebSocketListener) {
        webSocketTask.receiveMessageWithCompletionHandler { message, error ->
            if (error != null) {
                listener.onError(error.localizedDescription)
                return@receiveMessageWithCompletionHandler
            }
            
            message?.let { msg ->
                when (msg.type) {
                    NSURLSessionWebSocketMessageTypeString -> {
                        msg.string?.let { listener.onMessage(it) }
                    }
                    NSURLSessionWebSocketMessageTypeData -> {
                        // 处理二进制数据
                        msg.data?.let { data ->
                            val string = NSString.create(data, NSUTF8StringEncoding) as? String
                            string?.let { listener.onMessage(it) }
                        }
                    }
                }
                
                // 继续接收下一条消息
                receiveWebSocketMessage(webSocketTask, listener)
            }
        }
    }
    
    private fun setupNetworkMonitoring() {
        // 使用Network framework监控网络状态
        // 简化实现
    }
    
    private fun getCurrentNetworkStatus(): NetworkStatus {
        return if (isNetworkAvailable()) NetworkStatus.CONNECTED else NetworkStatus.DISCONNECTED
    }
    
    private fun String.toNSData(): NSData {
        return (this as NSString).dataUsingEncoding(NSUTF8StringEncoding)!!
    }
}

actual object UnifyNetworkManagerFactory {
    actual fun create(): UnifyNetworkManager {
        return UnifyNetworkManagerImpl()
    }
}
