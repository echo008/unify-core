package com.unify.network

import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.w3c.dom.events.Event
import org.w3c.fetch.RequestInit
import org.w3c.fetch.Response
import org.w3c.fetch.Headers
import kotlin.js.Promise
import kotlin.reflect.KClass

/**
 * Web平台网络管理器实现
 */
class WebUnifyNetworkManager(
    private val config: UnifyNetworkConfig
) : UnifyNetworkManager {
    
    override val httpClient: UnifyHttpClient = WebUnifyHttpClient(config)
    override val networkState: UnifyNetworkState = WebUnifyNetworkState()
    override val offlineSupport: UnifyOfflineSupport = WebUnifyOfflineSupport(config.cachePolicy)
    override val interceptors: UnifyInterceptorManager = WebUnifyInterceptorManager()
    
    override suspend fun initialize() {
        (networkState as WebUnifyNetworkState).initialize()
        (offlineSupport as WebUnifyOfflineSupport).initialize()
    }
    
    override suspend fun cleanup() {
        (networkState as WebUnifyNetworkState).cleanup()
        (offlineSupport as WebUnifyOfflineSupport).cleanup()
    }
}

/**
 * Web HTTP客户端实现
 */
class WebUnifyHttpClient(
    private val config: UnifyNetworkConfig
) : UnifyHttpClient {
    
    private val json = Json { 
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    override suspend fun <T> get(
        url: String,
        headers: Map<String, String>,
        queryParams: Map<String, String>,
        responseType: KClass<T>
    ): UnifyHttpResponse<T> {
        val urlWithParams = buildUrlWithParams(url, queryParams)
        val requestInit = RequestInit(
            method = "GET",
            headers = createHeaders(headers)
        )
        return executeRequest(urlWithParams, requestInit, responseType)
    }
    
    override suspend fun <T> post(
        url: String,
        body: Any?,
        headers: Map<String, String>,
        responseType: KClass<T>
    ): UnifyHttpResponse<T> {
        val requestInit = RequestInit(
            method = "POST",
            headers = createHeaders(headers),
            body = createRequestBody(body)
        )
        return executeRequest(url, requestInit, responseType)
    }
    
    override suspend fun <T> put(
        url: String,
        body: Any?,
        headers: Map<String, String>,
        responseType: KClass<T>
    ): UnifyHttpResponse<T> {
        val requestInit = RequestInit(
            method = "PUT",
            headers = createHeaders(headers),
            body = createRequestBody(body)
        )
        return executeRequest(url, requestInit, responseType)
    }
    
    override suspend fun <T> delete(
        url: String,
        headers: Map<String, String>,
        responseType: KClass<T>
    ): UnifyHttpResponse<T> {
        val requestInit = RequestInit(
            method = "DELETE",
            headers = createHeaders(headers)
        )
        return executeRequest(url, requestInit, responseType)
    }
    
    override suspend fun <T> patch(
        url: String,
        body: Any?,
        headers: Map<String, String>,
        responseType: KClass<T>
    ): UnifyHttpResponse<T> {
        val requestInit = RequestInit(
            method = "PATCH",
            headers = createHeaders(headers),
            body = createRequestBody(body)
        )
        return executeRequest(url, requestInit, responseType)
    }
    
    override suspend fun uploadFile(
        url: String,
        filePath: String,
        fieldName: String,
        headers: Map<String, String>,
        progressCallback: ((Long, Long) -> Unit)?
    ): UnifyHttpResponse<String> {
        return try {
            // Web平台文件上传需要通过File API
            val formData = org.w3c.dom.FormData()
            
            // 这里需要实际的File对象，filePath在Web中通常是File对象的引用
            // 简化实现，实际使用时需要传入File对象
            val requestInit = RequestInit(
                method = "POST",
                headers = createHeaders(headers, excludeContentType = true),
                body = formData
            )
            
            executeRequest(url, requestInit, String::class)
        } catch (e: Exception) {
            createErrorResponse("Upload failed: ${e.message}")
        }
    }
    
    override suspend fun downloadFile(
        url: String,
        destinationPath: String,
        headers: Map<String, String>,
        progressCallback: ((Long, Long) -> Unit)?
    ): UnifyHttpResponse<String> {
        return try {
            val requestInit = RequestInit(
                method = "GET",
                headers = createHeaders(headers)
            )
            
            val startTime = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            val response = window.fetch(url, requestInit).await()
            val responseTime = kotlinx.datetime.Clock.System.now().toEpochMilliseconds() - startTime
            
            if (response.ok) {
                val arrayBuffer = response.arrayBuffer().await()
                val blob = org.w3c.files.Blob(arrayOf(arrayBuffer))
                
                // 在Web中，我们创建下载链接
                val downloadUrl = org.w3c.dom.url.URL.createObjectURL(blob)
                val link = kotlinx.browser.document.createElement("a") as org.w3c.dom.HTMLAnchorElement
                link.href = downloadUrl
                link.download = destinationPath.substringAfterLast("/")
                link.click()
                
                org.w3c.dom.url.URL.revokeObjectURL(downloadUrl)
                
                UnifyHttpResponse(
                    statusCode = response.status.toInt(),
                    statusMessage = response.statusText,
                    headers = extractHeaders(response),
                    body = destinationPath,
                    isSuccessful = true,
                    responseTime = responseTime
                )
            } else {
                createErrorResponse("Download failed: ${response.statusText}")
            }
        } catch (e: Exception) {
            createErrorResponse("Download failed: ${e.message}")
        }
    }
    
    override suspend fun streamRequest(
        url: String,
        method: UnifyHttpMethod,
        headers: Map<String, String>
    ): Flow<UnifyStreamResponse> {
        return kotlinx.coroutines.flow.flow {
            // Web流式请求实现
            try {
                val requestInit = RequestInit(
                    method = method.name,
                    headers = createHeaders(headers)
                )
                
                val response = window.fetch(url, requestInit).await()
                if (response.body != null) {
                    val reader = response.body!!.getReader()
                    var totalBytes = 0L
                    val contentLength = response.headers.get("content-length")?.toLongOrNull() ?: 0L
                    
                    while (true) {
                        val result = reader.read().await()
                        if (result.done) break
                        
                        val chunk = result.value as Uint8Array
                        totalBytes += chunk.length
                        
                        emit(UnifyStreamResponse(
                            data = chunk.asByteArray(),
                            isComplete = false,
                            totalBytes = contentLength,
                            receivedBytes = totalBytes
                        ))
                    }
                    
                    emit(UnifyStreamResponse(
                        data = byteArrayOf(),
                        isComplete = true,
                        totalBytes = contentLength,
                        receivedBytes = totalBytes
                    ))
                }
            } catch (e: Exception) {
                // 处理流式请求错误
            }
        }
    }
    
    private suspend fun <T> executeRequest(
        url: String,
        requestInit: RequestInit,
        responseType: KClass<T>
    ): UnifyHttpResponse<T> {
        return try {
            val startTime = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            val response = window.fetch(url, requestInit).await()
            val responseTime = kotlinx.datetime.Clock.System.now().toEpochMilliseconds() - startTime
            
            val responseHeaders = extractHeaders(response)
            val responseBody = if (response.ok) {
                parseResponseBody(response, responseType)
            } else {
                null
            }
            
            UnifyHttpResponse(
                statusCode = response.status.toInt(),
                statusMessage = response.statusText,
                headers = responseHeaders,
                body = responseBody,
                isSuccessful = response.ok,
                responseTime = responseTime,
                error = if (!response.ok) {
                    UnifyNetworkError(
                        code = response.status.toInt(),
                        message = response.statusText,
                        type = UnifyNetworkErrorType.HTTP_ERROR
                    )
                } else null
            )
        } catch (e: Exception) {
            UnifyHttpResponse(
                statusCode = 0,
                statusMessage = "Request failed",
                headers = emptyMap(),
                body = null,
                isSuccessful = false,
                responseTime = 0,
                error = mapExceptionToNetworkError(e)
            )
        }
    }
    
    private fun createHeaders(
        headers: Map<String, String>,
        excludeContentType: Boolean = false
    ): dynamic {
        val allHeaders = config.defaultHeaders + headers
        val headersObj = js("{}")
        
        allHeaders.forEach { (key, value) ->
            if (!excludeContentType || key.lowercase() != "content-type") {
                headersObj[key] = value
            }
        }
        
        return headersObj
    }
    
    private fun createRequestBody(body: Any?): dynamic {
        return when (body) {
            null -> undefined
            is String -> body
            is ByteArray -> Uint8Array(body.toTypedArray())
            else -> json.encodeToString(body)
        }
    }
    
    private fun buildUrlWithParams(url: String, params: Map<String, String>): String {
        if (params.isEmpty()) return url
        
        val separator = if (url.contains("?")) "&" else "?"
        val queryString = params.map { "${it.key}=${encodeURIComponent(it.value)}" }.joinToString("&")
        return "$url$separator$queryString"
    }
    
    private suspend fun <T> parseResponseBody(response: Response, responseType: KClass<T>): T? {
        return try {
            when (responseType) {
                String::class -> response.text().await() as T
                ByteArray::class -> {
                    val arrayBuffer = response.arrayBuffer().await()
                    Uint8Array(arrayBuffer).asByteArray() as T
                }
                else -> {
                    val text = response.text().await()
                    json.decodeFromString(responseType.java, text) as T
                }
            }
        } catch (e: Exception) {
            null
        }
    }
    
    private fun extractHeaders(response: Response): Map<String, String> {
        val headers = mutableMapOf<String, String>()
        response.headers.forEach { value, key ->
            headers[key] = value
        }
        return headers
    }
    
    private fun mapExceptionToNetworkError(exception: Exception): UnifyNetworkError {
        return UnifyNetworkError(
            code = 0,
            message = exception.message ?: "Unknown error",
            type = UnifyNetworkErrorType.CONNECTION_ERROR,
            retryable = true
        )
    }
    
    private fun <T> createErrorResponse(message: String): UnifyHttpResponse<T> {
        return UnifyHttpResponse(
            statusCode = 0,
            statusMessage = "Error",
            headers = emptyMap(),
            body = null,
            isSuccessful = false,
            responseTime = 0,
            error = UnifyNetworkError(
                code = 0,
                message = message,
                type = UnifyNetworkErrorType.UNKNOWN_ERROR
            )
        )
    }
}

/**
 * Web网络状态管理实现
 */
class WebUnifyNetworkState : UnifyNetworkState {
    
    private val networkStatusFlow = MutableStateFlow(getCurrentNetworkStatus())
    private val networkStateListeners = mutableSetOf<UnifyNetworkStateListener>()
    
    fun initialize() {
        setupNetworkListeners()
    }
    
    fun cleanup() {
        removeNetworkListeners()
    }
    
    override suspend fun isNetworkAvailable(): Boolean {
        return window.navigator.onLine
    }
    
    override suspend fun getNetworkType(): UnifyNetworkType {
        // Web平台网络类型检测
        val connection = window.navigator.asDynamic().connection
        return if (connection != undefined) {
            when (connection.effectiveType) {
                "4g" -> UnifyNetworkType.CELLULAR_4G
                "3g" -> UnifyNetworkType.CELLULAR_3G
                "2g" -> UnifyNetworkType.CELLULAR_2G
                "slow-2g" -> UnifyNetworkType.CELLULAR_2G
                else -> UnifyNetworkType.WIFI
            }
        } else {
            UnifyNetworkType.UNKNOWN
        }
    }
    
    override fun observeNetworkState(): Flow<UnifyNetworkStatus> {
        return networkStatusFlow.asStateFlow()
    }
    
    override suspend fun getNetworkQuality(): UnifyNetworkQuality {
        if (!isNetworkAvailable()) {
            return UnifyNetworkQuality.UNAVAILABLE
        }
        
        val connection = window.navigator.asDynamic().connection
        return if (connection != undefined) {
            when (connection.effectiveType) {
                "4g" -> UnifyNetworkQuality.EXCELLENT
                "3g" -> UnifyNetworkQuality.GOOD
                "2g" -> UnifyNetworkQuality.FAIR
                "slow-2g" -> UnifyNetworkQuality.POOR
                else -> UnifyNetworkQuality.GOOD
            }
        } else {
            UnifyNetworkQuality.GOOD
        }
    }
    
    override fun setNetworkStateListener(listener: UnifyNetworkStateListener) {
        networkStateListeners.add(listener)
    }
    
    override fun removeNetworkStateListener(listener: UnifyNetworkStateListener) {
        networkStateListeners.remove(listener)
    }
    
    private fun setupNetworkListeners() {
        window.addEventListener("online", ::onNetworkChange)
        window.addEventListener("offline", ::onNetworkChange)
        
        // 监听连接变化
        val connection = window.navigator.asDynamic().connection
        if (connection != undefined) {
            connection.addEventListener("change", ::onNetworkChange)
        }
    }
    
    private fun removeNetworkListeners() {
        window.removeEventListener("online", ::onNetworkChange)
        window.removeEventListener("offline", ::onNetworkChange)
        
        val connection = window.navigator.asDynamic().connection
        if (connection != undefined) {
            connection.removeEventListener("change", ::onNetworkChange)
        }
    }
    
    private fun onNetworkChange(event: Event) {
        val newStatus = getCurrentNetworkStatus()
        networkStatusFlow.value = newStatus
        
        networkStateListeners.forEach { listener ->
            listener.onNetworkStateChanged(newStatus)
        }
    }
    
    private fun getCurrentNetworkStatus(): UnifyNetworkStatus {
        return try {
            val isConnected = window.navigator.onLine
            val networkType = if (isConnected) getNetworkType() else UnifyNetworkType.UNKNOWN
            val quality = if (isConnected) getNetworkQuality() else UnifyNetworkQuality.UNAVAILABLE
            
            val connection = window.navigator.asDynamic().connection
            val bandwidth = if (connection != undefined && connection.downlink != undefined) {
                (connection.downlink * 1024 * 1024).toLong() // Mbps to bps
            } else {
                estimateBandwidth(networkType)
            }
            
            val rtt = if (connection != undefined && connection.rtt != undefined) {
                connection.rtt.toLong()
            } else {
                estimateLatency(networkType)
            }
            
            UnifyNetworkStatus(
                isConnected = isConnected,
                networkType = networkType,
                quality = quality,
                signalStrength = if (isConnected) 80 else 0,
                bandwidth = bandwidth,
                latency = rtt
            )
        } catch (e: Exception) {
            UnifyNetworkStatus(
                isConnected = false,
                networkType = UnifyNetworkType.UNKNOWN,
                quality = UnifyNetworkQuality.UNAVAILABLE,
                signalStrength = 0,
                bandwidth = 0,
                latency = 0
            )
        }
    }
    
    private fun estimateBandwidth(networkType: UnifyNetworkType): Long {
        return when (networkType) {
            UnifyNetworkType.WIFI -> 50 * 1024 * 1024
            UnifyNetworkType.CELLULAR_4G -> 20 * 1024 * 1024
            UnifyNetworkType.CELLULAR_3G -> 2 * 1024 * 1024
            UnifyNetworkType.CELLULAR_2G -> 256 * 1024
            else -> 10 * 1024 * 1024
        }
    }
    
    private fun estimateLatency(networkType: UnifyNetworkType): Long {
        return when (networkType) {
            UnifyNetworkType.WIFI -> 20
            UnifyNetworkType.CELLULAR_4G -> 50
            UnifyNetworkType.CELLULAR_3G -> 100
            UnifyNetworkType.CELLULAR_2G -> 300
            else -> 100
        }
    }
}

/**
 * Web离线支持实现
 */
class WebUnifyOfflineSupport(
    private var cachePolicy: UnifyCachePolicy
) : UnifyOfflineSupport {
    
    private val json = Json { ignoreUnknownKeys = true }
    private val cachePrefix = "unify_network_cache_"
    private val queueKey = "unify_offline_queue"
    
    fun initialize() {
        // Web平台初始化
        setupServiceWorker()
    }
    
    fun cleanup() {
        // Web平台清理
    }
    
    override suspend fun cacheResponse(request: UnifyHttpRequest, response: UnifyHttpResponse<*>) {
        try {
            val cacheKey = generateCacheKey(request)
            val cacheData = mapOf(
                "response" to json.encodeToString(response),
                "timestamp" to kotlinx.datetime.Clock.System.now().toEpochMilliseconds(),
                "ttl" to cachePolicy.defaultCacheTtl
            )
            
            kotlinx.browser.localStorage.setItem(
                "$cachePrefix$cacheKey",
                json.encodeToString(cacheData)
            )
        } catch (e: Exception) {
            // 忽略缓存错误
        }
    }
    
    override suspend fun getCachedResponse(request: UnifyHttpRequest): UnifyHttpResponse<*>? {
        return try {
            val cacheKey = generateCacheKey(request)
            val cacheDataStr = kotlinx.browser.localStorage.getItem("$cachePrefix$cacheKey")
                ?: return null
            
            val cacheData = json.decodeFromString<Map<String, String>>(cacheDataStr)
            val timestamp = cacheData["timestamp"]?.toLongOrNull() ?: 0
            val ttl = cacheData["ttl"]?.toLongOrNull() ?: cachePolicy.defaultCacheTtl
            
            // 检查缓存是否过期
            if (kotlinx.datetime.Clock.System.now().toEpochMilliseconds() - timestamp > ttl) {
                kotlinx.browser.localStorage.removeItem("$cachePrefix$cacheKey")
                return null
            }
            
            val responseStr = cacheData["response"] ?: return null
            val cachedResponse = json.decodeFromString<UnifyHttpResponse<String>>(responseStr)
            cachedResponse.copy(isFromCache = true)
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun queueOfflineRequest(request: UnifyHttpRequest) {
        try {
            val existingQueue = getOfflineRequestQueue().toMutableList()
            existingQueue.add(request)
            
            kotlinx.browser.localStorage.setItem(
                queueKey,
                json.encodeToString(existingQueue)
            )
        } catch (e: Exception) {
            // 忽略队列错误
        }
    }
    
    override suspend fun getOfflineRequestQueue(): List<UnifyHttpRequest> {
        return try {
            val queueStr = kotlinx.browser.localStorage.getItem(queueKey) ?: return emptyList()
            json.decodeFromString(queueStr)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun processOfflineQueue(): List<UnifyHttpResponse<*>> {
        // 处理离线队列
        return emptyList()
    }
    
    override suspend fun cleanExpiredCache() {
        try {
            val currentTime = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            val keysToRemove = mutableListOf<String>()
            
            for (i in 0 until kotlinx.browser.localStorage.length) {
                val key = kotlinx.browser.localStorage.key(i) ?: continue
                if (key.startsWith(cachePrefix)) {
                    try {
                        val cacheDataStr = kotlinx.browser.localStorage.getItem(key) ?: continue
                        val cacheData = json.decodeFromString<Map<String, String>>(cacheDataStr)
                        val timestamp = cacheData["timestamp"]?.toLongOrNull() ?: 0
                        
                        if (currentTime - timestamp > cachePolicy.maxCacheAge) {
                            keysToRemove.add(key)
                        }
                    } catch (e: Exception) {
                        keysToRemove.add(key)
                    }
                }
            }
            
            keysToRemove.forEach { key ->
                kotlinx.browser.localStorage.removeItem(key)
            }
        } catch (e: Exception) {
            // 忽略清理错误
        }
    }
    
    override fun setCachePolicy(policy: UnifyCachePolicy) {
        this.cachePolicy = policy
    }
    
    override suspend fun getCacheStats(): UnifyOfflineCacheStats {
        return try {
            var cacheSize = 0L
            var cacheCount = 0
            
            for (i in 0 until kotlinx.browser.localStorage.length) {
                val key = kotlinx.browser.localStorage.key(i) ?: continue
                if (key.startsWith(cachePrefix)) {
                    val value = kotlinx.browser.localStorage.getItem(key) ?: continue
                    cacheSize += value.length * 2 // 估算字符串大小
                    cacheCount++
                }
            }
            
            val offlineQueue = getOfflineRequestQueue()
            
            UnifyOfflineCacheStats(
                cacheSize = cacheSize,
                cacheCount = cacheCount,
                hitCount = 0,
                missCount = 0,
                hitRate = 0.0,
                offlineQueueSize = offlineQueue.size
            )
        } catch (e: Exception) {
            UnifyOfflineCacheStats(0, 0, 0, 0, 0.0, 0)
        }
    }
    
    private fun generateCacheKey(request: UnifyHttpRequest): String {
        val keyString = "${request.method}_${request.url}_${request.queryParams}_${request.body}"
        return keyString.hashCode().toString()
    }
    
    private fun setupServiceWorker() {
        // Service Worker设置用于离线支持
        if (js("'serviceWorker' in navigator")) {
            // 注册Service Worker的代码
        }
    }
}

/**
 * Web拦截器管理实现
 */
class WebUnifyInterceptorManager : UnifyInterceptorManager {
    
    private val requestInterceptors = mutableListOf<UnifyRequestInterceptor>()
    private val responseInterceptors = mutableListOf<UnifyResponseInterceptor>()
    
    override fun addRequestInterceptor(interceptor: UnifyRequestInterceptor) {
        requestInterceptors.add(interceptor)
    }
    
    override fun addResponseInterceptor(interceptor: UnifyResponseInterceptor) {
        responseInterceptors.add(interceptor)
    }
    
    override fun removeRequestInterceptor(interceptor: UnifyRequestInterceptor) {
        requestInterceptors.remove(interceptor)
    }
    
    override fun removeResponseInterceptor(interceptor: UnifyResponseInterceptor) {
        responseInterceptors.remove(interceptor)
    }
    
    override fun clearInterceptors() {
        requestInterceptors.clear()
        responseInterceptors.clear()
    }
    
    override suspend fun interceptRequest(request: UnifyHttpRequest): UnifyHttpRequest {
        var modifiedRequest = request
        for (interceptor in requestInterceptors) {
            modifiedRequest = interceptor.intercept(modifiedRequest)
        }
        return modifiedRequest
    }
    
    override suspend fun interceptResponse(response: UnifyHttpResponse<*>): UnifyHttpResponse<*> {
        var modifiedResponse = response
        for (interceptor in responseInterceptors) {
            modifiedResponse = interceptor.intercept(modifiedResponse)
        }
        return modifiedResponse
    }
}

// Web平台扩展函数
private external fun encodeURIComponent(str: String): String

private fun Uint8Array.asByteArray(): ByteArray {
    return ByteArray(this.length) { this[it] }
}

/**
 * Web网络管理器工厂实现
 */
actual object UnifyNetworkManagerFactory {
    actual fun create(config: UnifyNetworkConfig): UnifyNetworkManager {
        return WebUnifyNetworkManager(config)
    }
}
