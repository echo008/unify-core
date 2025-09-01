package com.unify.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.Foundation.NSBundle
import platform.Foundation.NSURLSession
import platform.Foundation.NSURLRequest
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_get_status
import platform.SystemConfiguration.SCNetworkReachability
import kotlin.reflect.KClass

/**
 * iOS平台网络管理器实现
 */
class IOSUnifyNetworkManager(
    private val config: UnifyNetworkConfig
) : UnifyNetworkManager {
    
    override val httpClient: UnifyHttpClient = IOSUnifyHttpClient(config)
    override val networkState: UnifyNetworkState = IOSUnifyNetworkState()
    override val offlineSupport: UnifyOfflineSupport = IOSUnifyOfflineSupport(config.cachePolicy)
    override val interceptors: UnifyInterceptorManager = IOSUnifyInterceptorManager()
    
    override suspend fun initialize() {
        (networkState as IOSUnifyNetworkState).initialize()
        (offlineSupport as IOSUnifyOfflineSupport).initialize()
    }
    
    override suspend fun cleanup() {
        (httpClient as IOSUnifyHttpClient).cleanup()
        (networkState as IOSUnifyNetworkState).cleanup()
        (offlineSupport as IOSUnifyOfflineSupport).cleanup()
    }
}

/**
 * iOS HTTP客户端实现
 */
class IOSUnifyHttpClient(
    private val config: UnifyNetworkConfig
) : UnifyHttpClient {
    
    private val json = Json { 
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    private val urlSession: NSURLSession by lazy {
        val configuration = NSURLSessionConfiguration.defaultSessionConfiguration()
        configuration.timeoutIntervalForRequest = config.defaultTimeout / 1000.0
        configuration.timeoutIntervalForResource = config.defaultTimeout / 1000.0
        configuration.allowsCellularAccess = true
        configuration.waitsForConnectivity = true
        
        NSURLSession.sessionWithConfiguration(configuration)
    }
    
    override suspend fun <T> get(
        url: String,
        headers: Map<String, String>,
        queryParams: Map<String, String>,
        responseType: KClass<T>
    ): UnifyHttpResponse<T> {
        val urlWithParams = buildUrlWithParams(url, queryParams)
        val request = createNSURLRequest(urlWithParams, "GET", headers)
        return executeRequest(request, responseType)
    }
    
    override suspend fun <T> post(
        url: String,
        body: Any?,
        headers: Map<String, String>,
        responseType: KClass<T>
    ): UnifyHttpResponse<T> {
        val request = createNSURLRequest(url, "POST", headers, body)
        return executeRequest(request, responseType)
    }
    
    override suspend fun <T> put(
        url: String,
        body: Any?,
        headers: Map<String, String>,
        responseType: KClass<T>
    ): UnifyHttpResponse<T> {
        val request = createNSURLRequest(url, "PUT", headers, body)
        return executeRequest(request, responseType)
    }
    
    override suspend fun <T> delete(
        url: String,
        headers: Map<String, String>,
        responseType: KClass<T>
    ): UnifyHttpResponse<T> {
        val request = createNSURLRequest(url, "DELETE", headers)
        return executeRequest(request, responseType)
    }
    
    override suspend fun <T> patch(
        url: String,
        body: Any?,
        headers: Map<String, String>,
        responseType: KClass<T>
    ): UnifyHttpResponse<T> {
        val request = createNSURLRequest(url, "PATCH", headers, body)
        return executeRequest(request, responseType)
    }
    
    override suspend fun uploadFile(
        url: String,
        filePath: String,
        fieldName: String,
        headers: Map<String, String>,
        progressCallback: ((Long, Long) -> Unit)?
    ): UnifyHttpResponse<String> {
        return try {
            val fileUrl = NSURL.fileURLWithPath(filePath)
            val fileData = NSData.dataWithContentsOfURL(fileUrl)
                ?: return createErrorResponse("File not found: $filePath")
            
            val boundary = "Boundary-${NSProcessInfo.processInfo.globallyUniqueString}"
            val multipartData = createMultipartData(fileData, fieldName, filePath, boundary)
            
            val request = NSMutableURLRequest.requestWithURL(NSURL.URLWithString(url)!!)
            request.HTTPMethod = "POST"
            request.setValue("multipart/form-data; boundary=$boundary", forHTTPHeaderField = "Content-Type")
            
            (config.defaultHeaders + headers).forEach { (key, value) ->
                request.setValue(value, forHTTPHeaderField = key)
            }
            
            request.HTTPBody = multipartData
            
            executeRequest(request, String::class)
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
            val request = createNSURLRequest(url, "GET", headers)
            val response = executeRequest(request, NSData::class)
            
            if (response.isSuccessful && response.body != null) {
                val data = response.body as NSData
                val destinationUrl = NSURL.fileURLWithPath(destinationPath)
                
                val success = data.writeToURL(destinationUrl, atomically = true)
                if (success) {
                    UnifyHttpResponse(
                        statusCode = response.statusCode,
                        statusMessage = response.statusMessage,
                        headers = response.headers,
                        body = destinationPath,
                        isSuccessful = true,
                        responseTime = response.responseTime
                    )
                } else {
                    createErrorResponse("Failed to write file to $destinationPath")
                }
            } else {
                createErrorResponse("Download failed")
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
            // 流式请求实现占位符
        }
    }
    
    private suspend fun <T> executeRequest(
        request: NSURLRequest,
        responseType: KClass<T>
    ): UnifyHttpResponse<T> {
        return kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
            val startTime = NSDate().timeIntervalSince1970 * 1000
            
            val task = urlSession.dataTaskWithRequest(request) { data, response, error ->
                val responseTime = (NSDate().timeIntervalSince1970 * 1000 - startTime).toLong()
                
                if (error != null) {
                    val networkError = UnifyNetworkError(
                        code = error.code.toInt(),
                        message = error.localizedDescription ?: "Unknown error",
                        type = UnifyNetworkErrorType.CONNECTION_ERROR,
                        retryable = true
                    )
                    
                    continuation.resumeWith(Result.success(
                        UnifyHttpResponse(
                            statusCode = 0,
                            statusMessage = "Request failed",
                            headers = emptyMap(),
                            body = null,
                            isSuccessful = false,
                            responseTime = responseTime,
                            error = networkError
                        )
                    ))
                    return@dataTaskWithRequest
                }
                
                val httpResponse = response as? NSHTTPURLResponse
                val statusCode = httpResponse?.statusCode?.toInt() ?: 0
                val statusMessage = NSHTTPURLResponse.localizedStringForStatusCode(statusCode.toLong())
                val headers = httpResponse?.allHeaderFields?.mapKeys { it.key.toString() }
                    ?.mapValues { it.value.toString() } ?: emptyMap()
                
                val parsedBody = if (data != null && statusCode in 200..299) {
                    parseResponseData(data, responseType)
                } else {
                    null
                }
                
                continuation.resumeWith(Result.success(
                    UnifyHttpResponse(
                        statusCode = statusCode,
                        statusMessage = statusMessage,
                        headers = headers,
                        body = parsedBody,
                        isSuccessful = statusCode in 200..299,
                        responseTime = responseTime,
                        error = if (statusCode !in 200..299) {
                            UnifyNetworkError(
                                code = statusCode,
                                message = statusMessage,
                                type = UnifyNetworkErrorType.HTTP_ERROR
                            )
                        } else null
                    )
                ))
            }
            
            task.resume()
            
            continuation.invokeOnCancellation {
                task.cancel()
            }
        }
    }
    
    private fun createNSURLRequest(
        url: String,
        method: String,
        headers: Map<String, String>,
        body: Any? = null
    ): NSMutableURLRequest {
        val request = NSMutableURLRequest.requestWithURL(NSURL.URLWithString(url)!!)
        request.HTTPMethod = method
        
        (config.defaultHeaders + headers).forEach { (key, value) ->
            request.setValue(value, forHTTPHeaderField = key)
        }
        
        if (body != null) {
            val bodyData = when (body) {
                is String -> body.toNSData()
                is ByteArray -> body.toNSData()
                else -> json.encodeToString(body).toNSData()
            }
            request.HTTPBody = bodyData
            
            if (!headers.containsKey("Content-Type")) {
                request.setValue("application/json", forHTTPHeaderField = "Content-Type")
            }
        }
        
        return request
    }
    
    private fun buildUrlWithParams(url: String, params: Map<String, String>): String {
        if (params.isEmpty()) return url
        
        val separator = if (url.contains("?")) "&" else "?"
        val queryString = params.map { "${it.key}=${it.value}" }.joinToString("&")
        return "$url$separator$queryString"
    }
    
    private fun <T> parseResponseData(data: NSData, responseType: KClass<T>): T? {
        return try {
            when (responseType) {
                String::class -> data.toKString() as T
                NSData::class -> data as T
                ByteArray::class -> data.toByteArray() as T
                else -> {
                    val jsonString = data.toKString()
                    json.decodeFromString(responseType.java, jsonString) as T
                }
            }
        } catch (e: Exception) {
            null
        }
    }
    
    private fun createMultipartData(
        fileData: NSData,
        fieldName: String,
        fileName: String,
        boundary: String
    ): NSData {
        val mutableData = NSMutableData()
        
        val boundaryData = "--$boundary\r\n".toNSData()
        val dispositionData = "Content-Disposition: form-data; name=\"$fieldName\"; filename=\"$fileName\"\r\n".toNSData()
        val typeData = "Content-Type: application/octet-stream\r\n\r\n".toNSData()
        val endData = "\r\n--$boundary--\r\n".toNSData()
        
        mutableData.appendData(boundaryData)
        mutableData.appendData(dispositionData)
        mutableData.appendData(typeData)
        mutableData.appendData(fileData)
        mutableData.appendData(endData)
        
        return mutableData
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
    
    fun cleanup() {
        urlSession.invalidateAndCancel()
    }
}

/**
 * iOS网络状态管理实现
 */
class IOSUnifyNetworkState : UnifyNetworkState {
    
    private val networkStatusFlow = MutableStateFlow(getCurrentNetworkStatus())
    private val networkStateListeners = mutableSetOf<UnifyNetworkStateListener>()
    private var reachability: SCNetworkReachabilityRef? = null
    
    fun initialize() {
        setupReachabilityMonitoring()
    }
    
    fun cleanup() {
        reachability?.let { SCNetworkReachabilityUnscheduleFromRunLoop(it, CFRunLoopGetCurrent(), kCFRunLoopDefaultMode) }
    }
    
    override suspend fun isNetworkAvailable(): Boolean {
        return checkNetworkReachability()
    }
    
    override suspend fun getNetworkType(): UnifyNetworkType {
        // iOS网络类型检测实现
        return UnifyNetworkType.WIFI // 简化实现
    }
    
    override fun observeNetworkState(): Flow<UnifyNetworkStatus> {
        return networkStatusFlow.asStateFlow()
    }
    
    override suspend fun getNetworkQuality(): UnifyNetworkQuality {
        if (!isNetworkAvailable()) {
            return UnifyNetworkQuality.UNAVAILABLE
        }
        
        val networkType = getNetworkType()
        return when (networkType) {
            UnifyNetworkType.WIFI, UnifyNetworkType.ETHERNET -> UnifyNetworkQuality.EXCELLENT
            UnifyNetworkType.CELLULAR_5G -> UnifyNetworkQuality.EXCELLENT
            UnifyNetworkType.CELLULAR_4G -> UnifyNetworkQuality.GOOD
            UnifyNetworkType.CELLULAR_3G -> UnifyNetworkQuality.FAIR
            UnifyNetworkType.CELLULAR_2G -> UnifyNetworkQuality.POOR
            else -> UnifyNetworkQuality.FAIR
        }
    }
    
    override fun setNetworkStateListener(listener: UnifyNetworkStateListener) {
        networkStateListeners.add(listener)
    }
    
    override fun removeNetworkStateListener(listener: UnifyNetworkStateListener) {
        networkStateListeners.remove(listener)
    }
    
    private fun setupReachabilityMonitoring() {
        // iOS Reachability监控设置
        // 这里是简化实现
    }
    
    private fun checkNetworkReachability(): Boolean {
        // iOS网络可达性检查
        return true // 简化实现
    }
    
    private fun getCurrentNetworkStatus(): UnifyNetworkStatus {
        return UnifyNetworkStatus(
            isConnected = checkNetworkReachability(),
            networkType = UnifyNetworkType.WIFI,
            quality = UnifyNetworkQuality.EXCELLENT,
            signalStrength = 80,
            bandwidth = 50 * 1024 * 1024,
            latency = 20
        )
    }
}

/**
 * iOS离线支持实现
 */
class IOSUnifyOfflineSupport(
    private var cachePolicy: UnifyCachePolicy
) : UnifyOfflineSupport {
    
    private val json = Json { ignoreUnknownKeys = true }
    private val cacheDirectory: String by lazy {
        val paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, true)
        val cachesDir = paths.firstOrNull() as? String ?: ""
        "$cachesDir/UnifyNetworkCache"
    }
    
    fun initialize() {
        createCacheDirectory()
    }
    
    fun cleanup() {
        // 清理过期缓存
    }
    
    override suspend fun cacheResponse(request: UnifyHttpRequest, response: UnifyHttpResponse<*>) {
        // iOS缓存实现
    }
    
    override suspend fun getCachedResponse(request: UnifyHttpRequest): UnifyHttpResponse<*>? {
        // iOS缓存读取实现
        return null
    }
    
    override suspend fun queueOfflineRequest(request: UnifyHttpRequest) {
        // iOS离线队列实现
    }
    
    override suspend fun getOfflineRequestQueue(): List<UnifyHttpRequest> {
        return emptyList()
    }
    
    override suspend fun processOfflineQueue(): List<UnifyHttpResponse<*>> {
        return emptyList()
    }
    
    override suspend fun cleanExpiredCache() {
        // iOS缓存清理实现
    }
    
    override fun setCachePolicy(policy: UnifyCachePolicy) {
        this.cachePolicy = policy
    }
    
    override suspend fun getCacheStats(): UnifyOfflineCacheStats {
        return UnifyOfflineCacheStats(0, 0, 0, 0, 0.0, 0)
    }
    
    private fun createCacheDirectory() {
        val fileManager = NSFileManager.defaultManager
        if (!fileManager.fileExistsAtPath(cacheDirectory)) {
            fileManager.createDirectoryAtPath(cacheDirectory, true, null, null)
        }
    }
}

/**
 * iOS拦截器管理实现
 */
class IOSUnifyInterceptorManager : UnifyInterceptorManager {
    
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

// iOS扩展函数
private fun String.toNSData(): NSData {
    return this.encodeToByteArray().toNSData()
}

private fun ByteArray.toNSData(): NSData {
    return NSData.create(bytes = this.refTo(0), length = this.size.toULong())
}

private fun NSData.toKString(): String {
    val bytes = ByteArray(this.length.toInt())
    this.getBytes(bytes.refTo(0), this.length)
    return bytes.decodeToString()
}

private fun NSData.toByteArray(): ByteArray {
    val bytes = ByteArray(this.length.toInt())
    this.getBytes(bytes.refTo(0), this.length)
    return bytes
}

/**
 * iOS网络管理器工厂实现
 */
actual object UnifyNetworkManagerFactory {
    actual fun create(config: UnifyNetworkConfig): UnifyNetworkManager {
        return IOSUnifyNetworkManager(config)
    }
}
