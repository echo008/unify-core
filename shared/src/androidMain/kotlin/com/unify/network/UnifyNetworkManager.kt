package com.unify.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass

/**
 * Android平台网络管理器实现
 */
class AndroidUnifyNetworkManager(
    private val context: Context,
    private val config: UnifyNetworkConfig
) : UnifyNetworkManager {
    
    override val httpClient: UnifyHttpClient = AndroidUnifyHttpClient(config)
    override val networkState: UnifyNetworkState = AndroidUnifyNetworkState(context)
    override val offlineSupport: UnifyOfflineSupport = AndroidUnifyOfflineSupport(context, config.cachePolicy)
    override val interceptors: UnifyInterceptorManager = AndroidUnifyInterceptorManager()
    
    override suspend fun initialize() {
        (networkState as AndroidUnifyNetworkState).initialize()
        (offlineSupport as AndroidUnifyOfflineSupport).initialize()
    }
    
    override suspend fun cleanup() {
        (httpClient as AndroidUnifyHttpClient).cleanup()
        (networkState as AndroidUnifyNetworkState).cleanup()
        (offlineSupport as AndroidUnifyOfflineSupport).cleanup()
    }
}

/**
 * Android HTTP客户端实现
 */
class AndroidUnifyHttpClient(
    private val config: UnifyNetworkConfig
) : UnifyHttpClient {
    
    private val json = Json { 
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(config.defaultTimeout, TimeUnit.MILLISECONDS)
        .readTimeout(config.defaultTimeout, TimeUnit.MILLISECONDS)
        .writeTimeout(config.defaultTimeout, TimeUnit.MILLISECONDS)
        .followRedirects(config.followRedirects)
        .retryOnConnectionFailure(true)
        .apply {
            if (config.enableGzip) {
                addInterceptor(GzipRequestInterceptor())
            }
            if (config.enableLogging) {
                addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
            }
        }
        .build()
    
    override suspend fun <T> get(
        url: String,
        headers: Map<String, String>,
        queryParams: Map<String, String>,
        responseType: KClass<T>
    ): UnifyHttpResponse<T> {
        val urlBuilder = HttpUrl.parse(url)?.newBuilder()
            ?: throw IllegalArgumentException("Invalid URL: $url")
        
        queryParams.forEach { (key, value) ->
            urlBuilder.addQueryParameter(key, value)
        }
        
        val request = Request.Builder()
            .url(urlBuilder.build())
            .apply {
                (config.defaultHeaders + headers).forEach { (key, value) ->
                    addHeader(key, value)
                }
            }
            .get()
            .build()
        
        return executeRequest(request, responseType)
    }
    
    override suspend fun <T> post(
        url: String,
        body: Any?,
        headers: Map<String, String>,
        responseType: KClass<T>
    ): UnifyHttpResponse<T> {
        val requestBody = createRequestBody(body)
        
        val request = Request.Builder()
            .url(url)
            .apply {
                (config.defaultHeaders + headers).forEach { (key, value) ->
                    addHeader(key, value)
                }
            }
            .post(requestBody)
            .build()
        
        return executeRequest(request, responseType)
    }
    
    override suspend fun <T> put(
        url: String,
        body: Any?,
        headers: Map<String, String>,
        responseType: KClass<T>
    ): UnifyHttpResponse<T> {
        val requestBody = createRequestBody(body)
        
        val request = Request.Builder()
            .url(url)
            .apply {
                (config.defaultHeaders + headers).forEach { (key, value) ->
                    addHeader(key, value)
                }
            }
            .put(requestBody)
            .build()
        
        return executeRequest(request, responseType)
    }
    
    override suspend fun <T> delete(
        url: String,
        headers: Map<String, String>,
        responseType: KClass<T>
    ): UnifyHttpResponse<T> {
        val request = Request.Builder()
            .url(url)
            .apply {
                (config.defaultHeaders + headers).forEach { (key, value) ->
                    addHeader(key, value)
                }
            }
            .delete()
            .build()
        
        return executeRequest(request, responseType)
    }
    
    override suspend fun <T> patch(
        url: String,
        body: Any?,
        headers: Map<String, String>,
        responseType: KClass<T>
    ): UnifyHttpResponse<T> {
        val requestBody = createRequestBody(body)
        
        val request = Request.Builder()
            .url(url)
            .apply {
                (config.defaultHeaders + headers).forEach { (key, value) ->
                    addHeader(key, value)
                }
            }
            .patch(requestBody)
            .build()
        
        return executeRequest(request, responseType)
    }
    
    override suspend fun uploadFile(
        url: String,
        filePath: String,
        fieldName: String,
        headers: Map<String, String>,
        progressCallback: ((Long, Long) -> Unit)?
    ): UnifyHttpResponse<String> {
        val file = File(filePath)
        if (!file.exists()) {
            return UnifyHttpResponse(
                statusCode = 400,
                statusMessage = "File not found",
                headers = emptyMap(),
                body = null,
                isSuccessful = false,
                responseTime = 0,
                error = UnifyNetworkError(
                    code = 400,
                    message = "File not found: $filePath",
                    type = UnifyNetworkErrorType.UNKNOWN_ERROR
                )
            )
        }
        
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                fieldName,
                file.name,
                file.asRequestBody("application/octet-stream".toMediaType())
            )
            .build()
        
        val request = Request.Builder()
            .url(url)
            .apply {
                (config.defaultHeaders + headers).forEach { (key, value) ->
                    addHeader(key, value)
                }
            }
            .post(requestBody)
            .build()
        
        return executeRequest(request, String::class)
    }
    
    override suspend fun downloadFile(
        url: String,
        destinationPath: String,
        headers: Map<String, String>,
        progressCallback: ((Long, Long) -> Unit)?
    ): UnifyHttpResponse<String> {
        val request = Request.Builder()
            .url(url)
            .apply {
                (config.defaultHeaders + headers).forEach { (key, value) ->
                    addHeader(key, value)
                }
            }
            .get()
            .build()
        
        return try {
            val startTime = System.currentTimeMillis()
            val response = okHttpClient.newCall(request).execute()
            val responseTime = System.currentTimeMillis() - startTime
            
            if (response.isSuccessful) {
                val file = File(destinationPath)
                file.parentFile?.mkdirs()
                
                response.body?.let { responseBody ->
                    val totalBytes = responseBody.contentLength()
                    var downloadedBytes = 0L
                    
                    file.outputStream().use { output ->
                        responseBody.byteStream().use { input ->
                            val buffer = ByteArray(8192)
                            var bytesRead: Int
                            
                            while (input.read(buffer).also { bytesRead = it } != -1) {
                                output.write(buffer, 0, bytesRead)
                                downloadedBytes += bytesRead
                                progressCallback?.invoke(downloadedBytes, totalBytes)
                            }
                        }
                    }
                }
                
                UnifyHttpResponse(
                    statusCode = response.code,
                    statusMessage = response.message,
                    headers = response.headers.toMap(),
                    body = destinationPath,
                    isSuccessful = true,
                    responseTime = responseTime
                )
            } else {
                UnifyHttpResponse(
                    statusCode = response.code,
                    statusMessage = response.message,
                    headers = response.headers.toMap(),
                    body = null,
                    isSuccessful = false,
                    responseTime = responseTime,
                    error = UnifyNetworkError(
                        code = response.code,
                        message = response.message,
                        type = UnifyNetworkErrorType.HTTP_ERROR
                    )
                )
            }
        } catch (e: Exception) {
            UnifyHttpResponse(
                statusCode = 0,
                statusMessage = "Download failed",
                headers = emptyMap(),
                body = null,
                isSuccessful = false,
                responseTime = 0,
                error = mapExceptionToNetworkError(e)
            )
        }
    }
    
    override suspend fun streamRequest(
        url: String,
        method: UnifyHttpMethod,
        headers: Map<String, String>
    ): Flow<UnifyStreamResponse> {
        // 流式请求实现
        // 这里返回一个占位符Flow
        return kotlinx.coroutines.flow.flow {
            // 实际实现需要处理流式数据
        }
    }
    
    private suspend fun <T> executeRequest(
        request: Request,
        responseType: KClass<T>
    ): UnifyHttpResponse<T> {
        return try {
            val startTime = System.currentTimeMillis()
            val response = okHttpClient.newCall(request).execute()
            val responseTime = System.currentTimeMillis() - startTime
            
            val responseHeaders = response.headers.toMap()
            val responseBody = response.body?.string()
            
            val parsedBody = if (response.isSuccessful && responseBody != null) {
                parseResponseBody(responseBody, responseType)
            } else {
                null
            }
            
            UnifyHttpResponse(
                statusCode = response.code,
                statusMessage = response.message,
                headers = responseHeaders,
                body = parsedBody,
                isSuccessful = response.isSuccessful,
                responseTime = responseTime,
                error = if (!response.isSuccessful) {
                    UnifyNetworkError(
                        code = response.code,
                        message = response.message,
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
    
    private fun createRequestBody(body: Any?): RequestBody {
        return when (body) {
            null -> "".toRequestBody("text/plain".toMediaType())
            is String -> body.toRequestBody("text/plain".toMediaType())
            is ByteArray -> body.toRequestBody("application/octet-stream".toMediaType())
            else -> {
                val jsonString = json.encodeToString(body)
                jsonString.toRequestBody("application/json".toMediaType())
            }
        }
    }
    
    private fun <T> parseResponseBody(body: String, responseType: KClass<T>): T? {
        return try {
            when (responseType) {
                String::class -> body as T
                ByteArray::class -> body.toByteArray() as T
                else -> json.decodeFromString(responseType.java, body) as T
            }
        } catch (e: Exception) {
            null
        }
    }
    
    private fun mapExceptionToNetworkError(exception: Exception): UnifyNetworkError {
        return when (exception) {
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
    
    private fun Headers.toMap(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        for (i in 0 until size) {
            map[name(i)] = value(i)
        }
        return map
    }
    
    fun cleanup() {
        okHttpClient.dispatcher.executorService.shutdown()
        okHttpClient.connectionPool.evictAll()
    }
}

/**
 * Android网络状态管理实现
 */
class AndroidUnifyNetworkState(
    private val context: Context
) : UnifyNetworkState {
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val networkStatusFlow = MutableStateFlow(getCurrentNetworkStatus())
    private val networkStateListeners = mutableSetOf<UnifyNetworkStateListener>()
    
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            updateNetworkStatus()
        }
        
        override fun onLost(network: Network) {
            updateNetworkStatus()
        }
        
        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            updateNetworkStatus()
        }
    }
    
    fun initialize() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }
    
    fun cleanup() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
    
    override suspend fun isNetworkAvailable(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    
    override suspend fun getNetworkType(): UnifyNetworkType {
        val activeNetwork = connectivityManager.activeNetwork ?: return UnifyNetworkType.UNKNOWN
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return UnifyNetworkType.UNKNOWN
        
        return when {
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> UnifyNetworkType.WIFI
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                // 检测蜂窝网络类型需要更详细的实现
                UnifyNetworkType.CELLULAR_4G // 简化实现
            }
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> UnifyNetworkType.ETHERNET
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> UnifyNetworkType.BLUETOOTH
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> UnifyNetworkType.VPN
            else -> UnifyNetworkType.UNKNOWN
        }
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
    
    private fun updateNetworkStatus() {
        val newStatus = getCurrentNetworkStatus()
        networkStatusFlow.value = newStatus
        
        networkStateListeners.forEach { listener ->
            listener.onNetworkStateChanged(newStatus)
        }
    }
    
    private fun getCurrentNetworkStatus(): UnifyNetworkStatus {
        return try {
            val isConnected = runCatching { isNetworkAvailable() }.getOrDefault(false)
            val networkType = runCatching { getNetworkType() }.getOrDefault(UnifyNetworkType.UNKNOWN)
            val quality = runCatching { getNetworkQuality() }.getOrDefault(UnifyNetworkQuality.UNAVAILABLE)
            
            UnifyNetworkStatus(
                isConnected = isConnected,
                networkType = networkType,
                quality = quality,
                signalStrength = getSignalStrength(),
                bandwidth = estimateBandwidth(networkType),
                latency = estimateLatency(networkType)
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
    
    private fun getSignalStrength(): Int {
        // 获取信号强度的实现
        // 这里返回估算值
        return 75
    }
    
    private fun estimateBandwidth(networkType: UnifyNetworkType): Long {
        return when (networkType) {
            UnifyNetworkType.WIFI -> 50 * 1024 * 1024 // 50 Mbps
            UnifyNetworkType.ETHERNET -> 100 * 1024 * 1024 // 100 Mbps
            UnifyNetworkType.CELLULAR_5G -> 100 * 1024 * 1024 // 100 Mbps
            UnifyNetworkType.CELLULAR_4G -> 20 * 1024 * 1024 // 20 Mbps
            UnifyNetworkType.CELLULAR_3G -> 2 * 1024 * 1024 // 2 Mbps
            UnifyNetworkType.CELLULAR_2G -> 256 * 1024 // 256 Kbps
            else -> 0
        }
    }
    
    private fun estimateLatency(networkType: UnifyNetworkType): Long {
        return when (networkType) {
            UnifyNetworkType.WIFI -> 20
            UnifyNetworkType.ETHERNET -> 10
            UnifyNetworkType.CELLULAR_5G -> 30
            UnifyNetworkType.CELLULAR_4G -> 50
            UnifyNetworkType.CELLULAR_3G -> 100
            UnifyNetworkType.CELLULAR_2G -> 300
            else -> 1000
        }
    }
}

/**
 * Android离线支持实现
 */
class AndroidUnifyOfflineSupport(
    private val context: Context,
    private var cachePolicy: UnifyCachePolicy
) : UnifyOfflineSupport {
    
    private val cacheDir = File(context.cacheDir, "unify_network_cache")
    private val offlineQueueFile = File(context.cacheDir, "unify_offline_queue.json")
    private val json = Json { ignoreUnknownKeys = true }
    
    fun initialize() {
        cacheDir.mkdirs()
    }
    
    fun cleanup() {
        // 清理过期缓存
        runCatching { cleanExpiredCache() }
    }
    
    override suspend fun cacheResponse(request: UnifyHttpRequest, response: UnifyHttpResponse<*>) {
        if (!cachePolicy.maxCacheSize > 0) return
        
        try {
            val cacheKey = generateCacheKey(request)
            val cacheFile = File(cacheDir, "$cacheKey.cache")
            val metaFile = File(cacheDir, "$cacheKey.meta")
            
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
            val cacheFile = File(cacheDir, "$cacheKey.cache")
            val metaFile = File(cacheDir, "$cacheKey.meta")
            
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
        // 处理离线队列的实现
        // 这里返回空列表作为占位符
        return emptyList()
    }
    
    override suspend fun cleanExpiredCache() {
        try {
            val currentTime = System.currentTimeMillis()
            
            cacheDir.listFiles { file ->
                file.name.endsWith(".meta")
            }?.forEach { metaFile ->
                try {
                    val metadata = json.decodeFromString<Map<String, Long>>(metaFile.readText())
                    val timestamp = metadata["timestamp"] ?: 0
                    val maxAge = cachePolicy.maxCacheAge
                    
                    if (currentTime - timestamp > maxAge) {
                        val cacheKey = metaFile.nameWithoutExtension
                        val cacheFile = File(cacheDir, "$cacheKey.cache")
                        
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
            val cacheFiles = cacheDir.listFiles { file ->
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
 * Android拦截器管理实现
 */
class AndroidUnifyInterceptorManager : UnifyInterceptorManager {
    
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
 * Gzip请求拦截器
 */
class GzipRequestInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        if (originalRequest.body == null || originalRequest.header("Content-Encoding") != null) {
            return chain.proceed(originalRequest)
        }
        
        val compressedRequest = originalRequest.newBuilder()
            .header("Content-Encoding", "gzip")
            .method(originalRequest.method, gzip(originalRequest.body!!))
            .build()
        
        return chain.proceed(compressedRequest)
    }
    
    private fun gzip(body: RequestBody): RequestBody {
        return object : RequestBody() {
            override fun contentType() = body.contentType()
            
            override fun contentLength() = -1L
            
            override fun writeTo(sink: okio.BufferedSink) {
                val gzipSink = okio.GzipSink(sink).buffer()
                body.writeTo(gzipSink)
                gzipSink.close()
            }
        }
    }
}

/**
 * Android网络管理器工厂实现
 */
actual object UnifyNetworkManagerFactory {
    actual fun create(config: UnifyNetworkConfig): UnifyNetworkManager {
        // 使用Application Context进行初始化
        val context = getApplicationContext()
        return AndroidUnifyNetworkManager(context, config)
    }
    
    private fun getApplicationContext(): Context {
        // 通过反射获取Application Context
        return try {
            val activityThread = Class.forName("android.app.ActivityThread")
            val currentApplication = activityThread.getMethod("currentApplication")
            currentApplication.invoke(null) as Context
        } catch (e: Exception) {
            throw IllegalStateException("无法获取Application Context，请确保在Android环境中运行")
        }
    }
    
    fun create(context: Context, config: UnifyNetworkConfig = UnifyNetworkConfig()): UnifyNetworkManager {
        return AndroidUnifyNetworkManager(context, config)
    }
}
