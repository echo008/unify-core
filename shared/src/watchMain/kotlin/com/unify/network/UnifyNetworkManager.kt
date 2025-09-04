package com.unify.network

import com.unify.core.network.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Watch平台UnifyNetworkManager实现
 * 基于WatchOS/WearOS网络API和低功耗网络管理
 */
class UnifyNetworkManager : com.unify.core.network.UnifyNetworkManager {
    
    // 网络状态管理
    private val _networkStatus = MutableStateFlow(NetworkStatus.CONNECTED)
    override val networkStatus: Flow<NetworkStatus> = _networkStatus.asStateFlow()
    
    private val _isOnline = MutableStateFlow(true)
    override val isOnline: Flow<Boolean> = _isOnline.asStateFlow()
    
    // 请求配置
    private var defaultConfig = NetworkRequestConfig()
    
    // 缓存管理
    private val responseCache = mutableMapOf<String, CachedResponse>()
    
    // Watch网络管理器
    private val watchNetworkManager = WatchNetworkManager()
    
    override suspend fun <T> get(
        url: String,
        headers: Map<String, String>,
        config: NetworkRequestConfig?
    ): NetworkResponse<T> {
        return executeRequest("GET", url, null, headers, config)
    }
    
    override suspend fun <T> post(
        url: String,
        body: Any?,
        headers: Map<String, String>,
        config: NetworkRequestConfig?
    ): NetworkResponse<T> {
        return executeRequest("POST", url, body, headers, config)
    }
    
    override suspend fun <T> put(
        url: String,
        body: Any?,
        headers: Map<String, String>,
        config: NetworkRequestConfig?
    ): NetworkResponse<T> {
        return executeRequest("PUT", url, body, headers, config)
    }
    
    override suspend fun <T> delete(
        url: String,
        headers: Map<String, String>,
        config: NetworkRequestConfig?
    ): NetworkResponse<T> {
        return executeRequest("DELETE", url, null, headers, config)
    }
    
    override suspend fun <T> patch(
        url: String,
        body: Any?,
        headers: Map<String, String>,
        config: NetworkRequestConfig?
    ): NetworkResponse<T> {
        return executeRequest("PATCH", url, body, headers, config)
    }
    
    override suspend fun uploadFile(
        url: String,
        filePath: String,
        fieldName: String,
        headers: Map<String, String>,
        config: NetworkRequestConfig?
    ): NetworkResponse<String> {
        val requestConfig = config ?: defaultConfig
        
        return try {
            // Watch平台文件上传（通常通过伴侣设备）
            val result = watchNetworkManager.uploadFile(url, filePath, fieldName, headers, requestConfig)
            NetworkResponse.Success(result, 200, emptyMap())
        } catch (e: Exception) {
            NetworkResponse.Error(
                NetworkError.REQUEST_FAILED,
                "Upload failed: ${e.message}",
                e
            )
        }
    }
    
    override suspend fun downloadFile(
        url: String,
        destinationPath: String,
        headers: Map<String, String>,
        config: NetworkRequestConfig?
    ): NetworkResponse<String> {
        val requestConfig = config ?: defaultConfig
        
        return try {
            // Watch平台文件下载（通常通过伴侣设备）
            val result = watchNetworkManager.downloadFile(url, destinationPath, headers, requestConfig)
            NetworkResponse.Success(result, 200, emptyMap())
        } catch (e: Exception) {
            NetworkResponse.Error(
                NetworkError.REQUEST_FAILED,
                "Download failed: ${e.message}",
                e
            )
        }
    }
    
    override suspend fun createWebSocket(
        url: String,
        protocols: List<String>
    ): WebSocketConnection {
        return watchNetworkManager.createWebSocket(url, protocols)
    }
    
    override suspend fun setDefaultConfig(config: NetworkRequestConfig) {
        defaultConfig = config
    }
    
    override suspend fun getDefaultConfig(): NetworkRequestConfig {
        return defaultConfig
    }
    
    override suspend fun clearCache() {
        responseCache.clear()
    }
    
    override suspend fun getCacheSize(): Long {
        return responseCache.values.sumOf { it.data.length.toLong() }
    }
    
    // Watch特有功能
    suspend fun enableLowPowerMode(enabled: Boolean) {
        watchNetworkManager.enableLowPowerMode(enabled)
    }
    
    suspend fun syncWithCompanionDevice(data: Map<String, Any>) {
        watchNetworkManager.syncWithCompanionDevice(data)
    }
    
    suspend fun requestDataFromCompanion(requestType: String): Map<String, Any> {
        return watchNetworkManager.requestDataFromCompanion(requestType)
    }
    
    private suspend fun <T> executeRequest(
        method: String,
        url: String,
        body: Any?,
        headers: Map<String, String>,
        config: NetworkRequestConfig?
    ): NetworkResponse<T> {
        val requestConfig = config ?: defaultConfig
        
        // 检查缓存
        if (method == "GET" && requestConfig.enableCache) {
            val cachedResponse = getCachedResponse<T>(url)
            if (cachedResponse != null) {
                return cachedResponse
            }
        }
        
        // 检查网络状态
        if (!_isOnline.value) {
            return NetworkResponse.Error(
                NetworkError.NO_INTERNET,
                "No internet connection",
                null
            )
        }
        
        return try {
            val response = executeWithRetry(method, url, body, headers, requestConfig)
            
            // 缓存GET请求响应
            if (method == "GET" && requestConfig.enableCache && response is NetworkResponse.Success) {
                cacheResponse(url, response, requestConfig.cacheMaxAge)
            }
            
            response
        } catch (e: Exception) {
            NetworkResponse.Error(
                NetworkError.REQUEST_FAILED,
                "Request failed: ${e.message}",
                e
            )
        }
    }
    
    private suspend fun <T> executeWithRetry(
        method: String,
        url: String,
        body: Any?,
        headers: Map<String, String>,
        config: NetworkRequestConfig
    ): NetworkResponse<T> {
        var lastException: Exception? = null
        
        repeat(config.maxRetries + 1) { attempt ->
            try {
                if (attempt > 0) {
                    delay(config.retryDelay * attempt)
                }
                
                return watchNetworkManager.executeRequest(method, url, body, headers, config)
            } catch (e: Exception) {
                lastException = e
                if (attempt == config.maxRetries) {
                    throw e
                }
            }
        }
        
        throw lastException ?: Exception("Unknown error")
    }
    
    private fun <T> getCachedResponse(url: String): NetworkResponse<T>? {
        val cached = responseCache[url]
        return if (cached != null && !cached.isExpired()) {
            try {
                val data = Json.decodeFromString<T>(cached.data)
                NetworkResponse.Success(data, cached.statusCode, cached.headers)
            } catch (e: Exception) {
                null
            }
        } else {
            responseCache.remove(url)
            null
        }
    }
    
    private fun <T> cacheResponse(
        url: String,
        response: NetworkResponse.Success<T>,
        maxAge: Long
    ) {
        try {
            val jsonData = Json.encodeToString(response.data)
            val expiryTime = System.currentTimeMillis() + maxAge
            responseCache[url] = CachedResponse(
                data = jsonData,
                statusCode = response.statusCode,
                headers = response.headers,
                expiryTime = expiryTime
            )
        } catch (e: Exception) {
            // 序列化失败，不缓存
        }
    }
    
    private data class CachedResponse(
        val data: String,
        val statusCode: Int,
        val headers: Map<String, String>,
        val expiryTime: Long
    ) {
        fun isExpired(): Boolean = System.currentTimeMillis() > expiryTime
    }
    
    init {
        // 监听网络状态变化
        watchNetworkManager.observeNetworkStatus { status ->
            _networkStatus.value = status
            _isOnline.value = status == NetworkStatus.CONNECTED
        }
    }
}

// Watch网络管理器模拟实现
private class WatchNetworkManager {
    private var lowPowerModeEnabled = false
    
    suspend fun <T> executeRequest(
        method: String,
        url: String,
        body: Any?,
        headers: Map<String, String>,
        config: NetworkRequestConfig
    ): NetworkResponse<T> {
        // 模拟网络请求延迟
        delay(if (lowPowerModeEnabled) 2000 else 500)
        
        // 实际实现中会使用WatchOS NSURLSession或WearOS OkHttp
        return try {
            // 模拟成功响应
            val mockData = """{"status": "success", "message": "Watch request completed"}"""
            val data = Json.decodeFromString<T>(mockData)
            NetworkResponse.Success(data, 200, emptyMap())
        } catch (e: Exception) {
            NetworkResponse.Error(
                NetworkError.PARSE_ERROR,
                "Failed to parse response",
                e
            )
        }
    }
    
    suspend fun uploadFile(
        url: String,
        filePath: String,
        fieldName: String,
        headers: Map<String, String>,
        config: NetworkRequestConfig
    ): String {
        // 实际实现中会通过伴侣设备上传文件
        delay(if (lowPowerModeEnabled) 5000 else 2000)
        return "Upload completed via companion device"
    }
    
    suspend fun downloadFile(
        url: String,
        destinationPath: String,
        headers: Map<String, String>,
        config: NetworkRequestConfig
    ): String {
        // 实际实现中会通过伴侣设备下载文件
        delay(if (lowPowerModeEnabled) 3000 else 1000)
        return "Download completed via companion device"
    }
    
    suspend fun createWebSocket(
        url: String,
        protocols: List<String>
    ): WebSocketConnection {
        return WatchWebSocketConnection(url, protocols)
    }
    
    fun enableLowPowerMode(enabled: Boolean) {
        lowPowerModeEnabled = enabled
        println("Watch low power mode: $enabled")
    }
    
    suspend fun syncWithCompanionDevice(data: Map<String, Any>) {
        // 实际实现中会使用WatchConnectivity或Wear OS Data Layer API
        delay(1000)
        println("Syncing ${data.size} items with companion device")
    }
    
    suspend fun requestDataFromCompanion(requestType: String): Map<String, Any> {
        // 实际实现中会从伴侣设备请求数据
        delay(1500)
        return mapOf(
            "requestType" to requestType,
            "data" to "Companion device response",
            "timestamp" to System.currentTimeMillis()
        )
    }
    
    fun observeNetworkStatus(callback: (NetworkStatus) -> Unit) {
        // 实际实现中会监听Watch网络状态变化
        println("Observing Watch network status")
        // 模拟网络状态
        callback(NetworkStatus.CONNECTED)
    }
}

// Watch WebSocket连接实现
private class WatchWebSocketConnection(
    private val url: String,
    private val protocols: List<String>
) : WebSocketConnection {
    
    private val _connectionState = MutableStateFlow(WebSocketState.CONNECTING)
    override val connectionState: Flow<WebSocketState> = _connectionState.asStateFlow()
    
    private val _messages = MutableStateFlow<String?>(null)
    override val messages: Flow<String?> = _messages.asStateFlow()
    
    override suspend fun connect() {
        // 实际实现中会建立Watch WebSocket连接
        delay(1000)
        _connectionState.value = WebSocketState.CONNECTED
        println("Watch WebSocket connected to: $url")
    }
    
    override suspend fun disconnect() {
        _connectionState.value = WebSocketState.DISCONNECTED
        println("Watch WebSocket disconnected")
    }
    
    override suspend fun send(message: String) {
        if (_connectionState.value == WebSocketState.CONNECTED) {
            // 实际实现中会发送消息
            println("Watch WebSocket sending: $message")
        }
    }
    
    override suspend fun send(data: ByteArray) {
        if (_connectionState.value == WebSocketState.CONNECTED) {
            // 实际实现中会发送二进制数据
            println("Watch WebSocket sending binary data: ${data.size} bytes")
        }
    }
}

actual object UnifyNetworkManagerFactory {
    actual fun create(): com.unify.core.network.UnifyNetworkManager {
        return UnifyNetworkManager()
    }
}
