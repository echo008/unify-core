package com.unify.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.*
import java.net.*
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

/**
 * Desktop平台网络管理器实现
 */
class DesktopUnifyNetworkManager(
    private val config: UnifyNetworkConfig
) : UnifyNetworkManager {
    
    override val httpClient: UnifyHttpClient = DesktopUnifyHttpClient(config)
    override val networkState: UnifyNetworkState = DesktopUnifyNetworkState()
    override val offlineSupport: UnifyOfflineSupport = DesktopUnifyOfflineSupport(config.cachePolicy)
    override val interceptors: UnifyInterceptorManager = DesktopUnifyInterceptorManager()
    
    override suspend fun initialize() {
        (networkState as DesktopUnifyNetworkState).initialize()
        (offlineSupport as DesktopUnifyOfflineSupport).initialize()
    }
    
    override suspend fun cleanup() {
        (networkState as DesktopUnifyNetworkState).cleanup()
        (offlineSupport as DesktopUnifyOfflineSupport).cleanup()
    }
}

/**
 * Desktop HTTP客户端实现
 */
class DesktopUnifyHttpClient(
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
        return executeRequest(urlWithParams, "GET", headers, null, responseType)
    }
    
    override suspend fun <T> post(
        url: String,
        body: Any?,
        headers: Map<String, String>,
        responseType: KClass<T>
    ): UnifyHttpResponse<T> {
        return executeRequest(url, "POST", headers, body, responseType)
    }
    
    override suspend fun <T> put(
        url: String,
        body: Any?,
        headers: Map<String, String>,
        responseType: KClass<T>
    ): UnifyHttpResponse<T> {
        return executeRequest(url, "PUT", headers, body, responseType)
    }
    
    override suspend fun <T> delete(
        url: String,
        headers: Map<String, String>,
        responseType: KClass<T>
    ): UnifyHttpResponse<T> {
        return executeRequest(url, "DELETE", headers, null, responseType)
    }
    
    override suspend fun <T> patch(
        url: String,
        body: Any?,
        headers: Map<String, String>,
        responseType: KClass<T>
    ): UnifyHttpResponse<T> {
        return executeRequest(url, "PATCH", headers, body, responseType)
    }
    
    override suspend fun uploadFile(
        url: String,
        filePath: String,
        fieldName: String,
        headers: Map<String, String>,
        progressCallback: ((Long, Long) -> Unit)?
    ): UnifyHttpResponse<String> {
        return try {
            val file = File(filePath)
            if (!file.exists()) {
                return createErrorResponse("File not found: $filePath")
            }
            
            val boundary = "----UnifyBoundary${System.currentTimeMillis()}"
            val connection = createConnection(url, "POST", headers + ("Content-Type" to "multipart/form-data; boundary=$boundary"))
            
            connection.doOutput = true
            connection.setChunkedStreamingMode(8192)
            
            val startTime = System.currentTimeMillis()
            
            connection.outputStream.use { output ->
                val writer = PrintWriter(OutputStreamWriter(output, "UTF-8"))
                
                // 写入文件部分
                writer.append("--$boundary\r\n")
                writer.append("Content-Disposition: form-data; name=\"$fieldName\"; filename=\"${file.name}\"\r\n")
                writer.append("Content-Type: application/octet-stream\r\n\r\n")
                writer.flush()
                
                // 写入文件数据
                val fileSize = file.length()
                var uploadedBytes = 0L
                
                file.inputStream().use { input ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        uploadedBytes += bytesRead
                        progressCallback?.invoke(uploadedBytes, fileSize)
                    }
                }
                
                writer.append("\r\n--$boundary--\r\n")
                writer.flush()
            }
            
            val responseTime = System.currentTimeMillis() - startTime
            processResponse(connection, String::class, responseTime)
            
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
            val connection = createConnection(url, "GET", headers)
            val startTime = System.currentTimeMillis()
            
            val responseCode = connection.responseCode
            val responseTime = System.currentTimeMillis() - startTime
            
            if (responseCode in 200..299) {
                val contentLength = connection.contentLengthLong
                var downloadedBytes = 0L
                
                val destinationFile = File(destinationPath)
                destinationFile.parentFile?.mkdirs()
                
                connection.inputStream.use { input ->
                    destinationFile.outputStream().use { output ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            downloadedBytes += bytesRead
                            progressCallback?.invoke(downloadedBytes, contentLength)
                        }
                    }
                }
                
                UnifyHttpResponse(
                    statusCode = responseCode,
                    statusMessage = connection.responseMessage ?: "OK",
                    headers = extractHeaders(connection),
                    body = destinationPath,
                    isSuccessful = true,
                    responseTime = responseTime
                )
            } else {
                createErrorResponse("Download failed: ${connection.responseMessage}")
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
            try {
                val connection = createConnection(url, method.name, headers)
                val contentLength = connection.contentLengthLong
                var receivedBytes = 0L
                
                connection.inputStream.use { input ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        receivedBytes += bytesRead
                        
                        emit(UnifyStreamResponse(
                            data = buffer.copyOf(bytesRead),
                            isComplete = false,
                            totalBytes = contentLength,
                            receivedBytes = receivedBytes
                        ))
                    }
                    
                    emit(UnifyStreamResponse(
                        data = byteArrayOf(),
                        isComplete = true,
                        totalBytes = contentLength,
                        receivedBytes = receivedBytes
                    ))
                }
            } catch (e: Exception) {
                // 处理流式请求错误
            }
        }
    }
    
    private suspend fun <T> executeRequest(
        url: String,
        method: String,
        headers: Map<String, String>,
        body: Any?,
        responseType: KClass<T>
    ): UnifyHttpResponse<T> {
        return try {
            val connection = createConnection(url, method, headers)
            val startTime = System.currentTimeMillis()
            
            // 写入请求体
            if (body != null && method in listOf("POST", "PUT", "PATCH")) {
                connection.doOutput = true
                connection.outputStream.use { output ->
                    val bodyBytes = when (body) {
                        is String -> body.toByteArray()
                        is ByteArray -> body
                        else -> json.encodeToString(body).toByteArray()
                    }
                    output.write(bodyBytes)
                }
            }
            
            val responseTime = System.currentTimeMillis() - startTime
            processResponse(connection, responseType, responseTime)
            
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
    
    private fun createConnection(
        url: String,
        method: String,
        headers: Map<String, String>
    ): HttpURLConnection {
        val connection = URL(url).openConnection() as HttpURLConnection
        
        connection.requestMethod = method
        connection.connectTimeout = config.defaultTimeout.toInt()
        connection.readTimeout = config.defaultTimeout.toInt()
        connection.instanceFollowRedirects = config.followRedirects
        
        // 设置默认User-Agent
        connection.setRequestProperty("User-Agent", config.userAgent)
        
        // 设置请求头
        (config.defaultHeaders + headers).forEach { (key, value) ->
            connection.setRequestProperty(key, value)
        }
        
        // 启用Gzip压缩
        if (config.enableGzip) {
            connection.setRequestProperty("Accept-Encoding", "gzip, deflate")
        }
        
        return connection
    }
    
    private fun <T> processResponse(
        connection: HttpURLConnection,
        responseType: KClass<T>,
        responseTime: Long
    ): UnifyHttpResponse<T> {
        val responseCode = connection.responseCode
        val responseMessage = connection.responseMessage ?: ""
        val responseHeaders = extractHeaders(connection)
        
        val responseBody = if (responseCode in 200..299) {
            parseResponseBody(connection, responseType)
        } else {
            null
        }
        
        return UnifyHttpResponse(
            statusCode = responseCode,
            statusMessage = responseMessage,
            headers = responseHeaders,
            body = responseBody,
            isSuccessful = responseCode in 200..299,
            responseTime = responseTime,
            error = if (responseCode !in 200..299) {
                UnifyNetworkError(
                    code = responseCode,
                    message = responseMessage,
                    type = UnifyNetworkErrorType.HTTP_ERROR
                )
            } else null
        )
    }
    
    private fun <T> parseResponseBody(connection: HttpURLConnection, responseType: KClass<T>): T? {
        return try {
            val inputStream = if (connection.responseCode >= 400) {
                connection.errorStream
            } else {
                connection.inputStream
            }
            
            // 处理Gzip压缩
            val stream = if (connection.contentEncoding == "gzip") {
                java.util.zip.GZIPInputStream(inputStream)
            } else {
                inputStream
            }
            
            when (responseType) {
                String::class -> stream.bufferedReader().readText() as T
                ByteArray::class -> stream.readBytes() as T
                else -> {
                    val text = stream.bufferedReader().readText()
                    json.decodeFromString(responseType.java, text) as T
                }
            }
        } catch (e: Exception) {
            null
        }
    }
    
    private fun extractHeaders(connection: HttpURLConnection): Map<String, String> {
        val headers = mutableMapOf<String, String>()
        connection.headerFields.forEach { (key, values) ->
            if (key != null && values.isNotEmpty()) {
                headers[key] = values.joinToString(", ")
            }
        }
        return headers
    }
    
    private fun buildUrlWithParams(url: String, params: Map<String, String>): String {
        if (params.isEmpty()) return url
        
        val separator = if (url.contains("?")) "&" else "?"
        val queryString = params.map { "${URLEncoder.encode(it.key, "UTF-8")}=${URLEncoder.encode(it.value, "UTF-8")}" }
            .joinToString("&")
        return "$url$separator$queryString"
    }
    
    private fun mapExceptionToNetworkError(exception: Exception): UnifyNetworkError {
        return when (exception) {
            is SocketTimeoutException -> UnifyNetworkError(
                code = 0,
                message = "Request timeout",
                type = UnifyNetworkErrorType.TIMEOUT,
                retryable = true
            )
            is UnknownHostException -> UnifyNetworkError(
                code = 0,
                message = "DNS resolution failed",
                type = UnifyNetworkErrorType.DNS_ERROR,
                retryable = true
            )
            is ConnectException -> UnifyNetworkError(
                code = 0,
                message = "Connection failed",
                type = UnifyNetworkErrorType.CONNECTION_ERROR,
                retryable = true
            )
            is IOException -> UnifyNetworkError(
                code = 0,
                message = exception.message ?: "Network error",
                type = UnifyNetworkErrorType.CONNECTION_ERROR,
                retryable = true
            )
            else -> UnifyNetworkError(
                code = 0,
                message = exception.message ?: "Unknown error",
                type = UnifyNetworkErrorType.UNKNOWN_ERROR,
                retryable = false
            )
        }
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
 * Desktop网络状态管理实现
 */
class DesktopUnifyNetworkState : UnifyNetworkState {
    
    private val networkStatusFlow = MutableStateFlow(getCurrentNetworkStatus())
    private val networkStateListeners = mutableSetOf<UnifyNetworkStateListener>()
    
    fun initialize() {
        // Desktop平台网络监控初始化
        startNetworkMonitoring()
    }
    
    fun cleanup() {
        // 清理网络监控
    }
    
    override suspend fun isNetworkAvailable(): Boolean {
        return try {
            val socket = Socket()
            socket.connect(InetSocketAddress("8.8.8.8", 53), 3000)
            socket.close()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun getNetworkType(): UnifyNetworkType {
        return try {
            val networkInterfaces = NetworkInterface.getNetworkInterfaces()
            
            for (networkInterface in networkInterfaces) {
                if (networkInterface.isUp && !networkInterface.isLoopback) {
                    val name = networkInterface.name.lowercase()
                    return when {
                        name.contains("wifi") || name.contains("wlan") -> UnifyNetworkType.WIFI
                        name.contains("eth") || name.contains("en") -> UnifyNetworkType.ETHERNET
                        name.contains("ppp") || name.contains("wwan") -> UnifyNetworkType.CELLULAR_4G
                        name.contains("vpn") || name.contains("tun") -> UnifyNetworkType.VPN
                        else -> UnifyNetworkType.UNKNOWN
                    }
                }
            }
            
            UnifyNetworkType.UNKNOWN
        } catch (e: Exception) {
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
        
        val latency = measureLatency()
        return when {
            latency < 50 -> UnifyNetworkQuality.EXCELLENT
            latency < 100 -> UnifyNetworkQuality.GOOD
            latency < 200 -> UnifyNetworkQuality.FAIR
            latency < 500 -> UnifyNetworkQuality.POOR
            else -> UnifyNetworkQuality.UNAVAILABLE
        }
    }
    
    override fun setNetworkStateListener(listener: UnifyNetworkStateListener) {
        networkStateListeners.add(listener)
    }
    
    override fun removeNetworkStateListener(listener: UnifyNetworkStateListener) {
        networkStateListeners.remove(listener)
    }
    
    private fun startNetworkMonitoring() {
        // 启动网络状态监控线程
        Thread {
            while (true) {
                try {
                    val newStatus = getCurrentNetworkStatus()
                    if (newStatus != networkStatusFlow.value) {
                        networkStatusFlow.value = newStatus
                        
                        networkStateListeners.forEach { listener ->
                            listener.onNetworkStateChanged(newStatus)
                        }
                    }
                    
                    Thread.sleep(5000) // 每5秒检查一次
                } catch (e: Exception) {
                    // 忽略监控错误
                }
            }
        }.apply {
            isDaemon = true
            start()
        }
    }
    
    private fun getCurrentNetworkStatus(): UnifyNetworkStatus {
        return try {
            val isConnected = runCatching { isNetworkAvailable() }.getOrDefault(false)
            val networkType = if (isConnected) {
                runCatching { getNetworkType() }.getOrDefault(UnifyNetworkType.UNKNOWN)
            } else {
                UnifyNetworkType.UNKNOWN
            }
            val quality = if (isConnected) {
                runCatching { getNetworkQuality() }.getOrDefault(UnifyNetworkQuality.UNAVAILABLE)
            } else {
                UnifyNetworkQuality.UNAVAILABLE
            }
            
            UnifyNetworkStatus(
                isConnected = isConnected,
                networkType = networkType,
                quality = quality,
                signalStrength = if (isConnected) 85 else 0,
                bandwidth = estimateBandwidth(networkType),
                latency = if (isConnected) measureLatency() else 0
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
    
    private fun measureLatency(): Long {
        return try {
            val startTime = System.currentTimeMillis()
            val socket = Socket()
            socket.connect(InetSocketAddress("8.8.8.8", 53), 3000)
            socket.close()
            System.currentTimeMillis() - startTime
        } catch (e: Exception) {
            1000 // 默认延迟
        }
    }
    
    private fun estimateBandwidth(networkType: UnifyNetworkType): Long {
        return when (networkType) {
            UnifyNetworkType.ETHERNET -> 1000 * 1024 * 1024 // 1 Gbps
            UnifyNetworkType.WIFI -> 100 * 1024 * 1024 // 100 Mbps
            UnifyNetworkType.CELLULAR_5G -> 100 * 1024 * 1024
            UnifyNetworkType.CELLULAR_4G -> 50 * 1024 * 1024
            UnifyNetworkType.VPN -> 50 * 1024 * 1024
            else -> 10 * 1024 * 1024
        }
    }
}

/**
 * Desktop离线支持实现
 */
class DesktopUnifyOfflineSupport(
    private var cachePolicy: UnifyCachePolicy
) : UnifyOfflineSupport {
    
    private val json = Json { ignoreUnknownKeys = true }
    private val cacheDirectory = File(System.getProperty("user.home"), ".unify/network_cache")
    private val offlineQueueFile = File(cacheDirectory, "offline_queue.json")
    
    fun initialize() {
        cacheDirectory.mkdirs()
    }
    
    fun cleanup() {
        // 清理过期缓存
        runCatching { cleanExpiredCache() }
    }
    
    override suspend fun cacheResponse(request: UnifyHttpRequest, response: UnifyHttpResponse<*>) {
        try {
            val cacheKey = generateCacheKey(request)
            val cacheFile = File(cacheDirectory, "$cacheKey.cache")
            val metaFile = File(cacheDirectory, "$cacheKey.meta")
            
            // 存储响应数据
            cacheFile.writeText(json.encodeToString(response))
            
            // 存储元数据
            val metadata = mapOf(
                "timestamp" to System.currentTimeMillis(),
                "ttl" to cachePolicy.defaultCacheTtl,
                "url" to request.url
            )
            metaFile.writeText(json.encodeToString(metadata))
            
        } catch (e: Exception) {
            // 忽略缓存错误
        }
    }
    
    override suspend fun getCachedResponse(request: UnifyHttpRequest): UnifyHttpResponse<*>? {
        return try {
            val cacheKey = generateCacheKey(request)
            val cacheFile = File(cacheDirectory, "$cacheKey.cache")
            val metaFile = File(cacheDirectory, "$cacheKey.meta")
            
            if (!cacheFile.exists() || !metaFile.exists()) {
                return null
            }
            
            // 检查缓存是否过期
            val metadata = json.decodeFromString<Map<String, Long>>(metaFile.readText())
            val timestamp = metadata["timestamp"] ?: 0
            val ttl = metadata["ttl"] ?: cachePolicy.defaultCacheTtl
            
            if (System.currentTimeMillis() - timestamp > ttl) {
                cacheFile.delete()
                metaFile.delete()
                return null
            }
            
            // 读取缓存响应
            val cachedResponse = json.decodeFromString<UnifyHttpResponse<String>>(cacheFile.readText())
            cachedResponse.copy(isFromCache = true)
            
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun queueOfflineRequest(request: UnifyHttpRequest) {
        try {
            val existingQueue = getOfflineRequestQueue().toMutableList()
            existingQueue.add(request)
            
            offlineQueueFile.writeText(json.encodeToString(existingQueue))
        } catch (e: Exception) {
            // 忽略队列错误
        }
    }
    
    override suspend fun getOfflineRequestQueue(): List<UnifyHttpRequest> {
        return try {
            if (offlineQueueFile.exists()) {
                json.decodeFromString(offlineQueueFile.readText())
            } else {
                emptyList()
            }
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
            val currentTime = System.currentTimeMillis()
            
            cacheDirectory.listFiles { file ->
                file.name.endsWith(".meta")
            }?.forEach { metaFile ->
                try {
                    val metadata = json.decodeFromString<Map<String, Long>>(metaFile.readText())
                    val timestamp = metadata["timestamp"] ?: 0
                    
                    if (currentTime - timestamp > cachePolicy.maxCacheAge) {
                        val cacheKey = metaFile.nameWithoutExtension
                        val cacheFile = File(cacheDirectory, "$cacheKey.cache")
                        
                        metaFile.delete()
                        cacheFile.delete()
                    }
                } catch (e: Exception) {
                    // 删除损坏的缓存文件
                    metaFile.delete()
                }
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
            val cacheFiles = cacheDirectory.listFiles { file ->
                file.name.endsWith(".cache")
            } ?: emptyArray()
            
            val cacheSize = cacheFiles.sumOf { it.length() }
            val offlineQueue = getOfflineRequestQueue()
            
            UnifyOfflineCacheStats(
                cacheSize = cacheSize,
                cacheCount = cacheFiles.size,
                hitCount = 0, // 需要实际统计
                missCount = 0, // 需要实际统计
                hitRate = 0.0, // 需要实际计算
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
}

/**
 * Desktop拦截器管理实现
 */
class DesktopUnifyInterceptorManager : UnifyInterceptorManager {
    
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

/**
 * Desktop网络管理器工厂实现
 */
actual object UnifyNetworkManagerFactory {
    actual fun create(config: UnifyNetworkConfig): UnifyNetworkManager {
        return DesktopUnifyNetworkManager(config)
    }
}
