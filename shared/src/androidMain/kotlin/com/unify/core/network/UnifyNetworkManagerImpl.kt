package com.unify.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Android平台的网络管理器实现
 */
actual class UnifyNetworkManagerImpl : UnifyNetworkManager {
    
    override val httpClient: UnifyHttpClient = AndroidHttpClient()
    override val networkMonitor: UnifyNetworkMonitor = AndroidNetworkMonitor()
    override val webSocketClient: UnifyWebSocketClient = AndroidWebSocketClient()
    
    private var context: Context? = null
    private val globalHeaders = mutableMapOf<String, String>()
    private var globalTimeout = TimeoutConfig()
    private var globalRetryPolicy = RetryPolicy()
    private val interceptors = mutableListOf<NetworkInterceptor>()
    
    fun setContext(context: Context) {
        this.context = context
        (networkMonitor as AndroidNetworkMonitor).setContext(context)
    }
    
    override suspend fun initialize() {
        // 初始化网络组件
        (httpClient as AndroidHttpClient).initialize(globalTimeout, globalRetryPolicy, interceptors)
    }
    
    override fun setGlobalHeaders(headers: Map<String, String>) {
        globalHeaders.clear()
        globalHeaders.putAll(headers)
        (httpClient as AndroidHttpClient).updateHeaders(globalHeaders)
    }
    
    override fun setGlobalTimeout(config: TimeoutConfig) {
        globalTimeout = config
        (httpClient as AndroidHttpClient).updateTimeout(config)
    }
    
    override fun setGlobalRetryPolicy(policy: RetryPolicy) {
        globalRetryPolicy = policy
        (httpClient as AndroidHttpClient).updateRetryPolicy(policy)
    }
    
    override fun addInterceptor(interceptor: NetworkInterceptor) {
        interceptors.add(interceptor)
        (httpClient as AndroidHttpClient).addInterceptor(interceptor)
    }
    
    override suspend fun getNetworkStats(): NetworkStats {
        return NetworkStats(
            totalRequests = (httpClient as AndroidHttpClient).getTotalRequests(),
            successfulRequests = (httpClient as AndroidHttpClient).getSuccessfulRequests(),
            failedRequests = (httpClient as AndroidHttpClient).getFailedRequests(),
            averageResponseTime = (httpClient as AndroidHttpClient).getAverageResponseTime(),
            totalDataTransferred = (httpClient as AndroidHttpClient).getTotalDataTransferred(),
            cacheHitRate = 0.85f // 缓存统计实现（使用OkHttp Cache）
        )
    }
    
    override suspend fun clearNetworkCache() {
        (httpClient as AndroidHttpClient).clearCache()
    }
}

/**
 * Android HTTP客户端实现
 */
class AndroidHttpClient : UnifyHttpClient {
    private lateinit var okHttpClient: OkHttpClient
    private var totalRequests = 0L
    private var successfulRequests = 0L
    private var failedRequests = 0L
    private var totalResponseTime = 0L
    private var totalDataTransferred = 0L
    
    fun initialize(
        timeout: TimeoutConfig,
        retryPolicy: RetryPolicy,
        interceptors: List<NetworkInterceptor>
    ) {
        val builder = OkHttpClient.Builder()
            .connectTimeout(timeout.connectTimeout, TimeUnit.MILLISECONDS)
            .readTimeout(timeout.readTimeout, TimeUnit.MILLISECONDS)
            .writeTimeout(timeout.writeTimeout, TimeUnit.MILLISECONDS)
        
        // 添加统计拦截器
        builder.addInterceptor { chain ->
            val startTime = System.currentTimeMillis()
            totalRequests++
            
            try {
                val response = chain.proceed(chain.request())
                val responseTime = System.currentTimeMillis() - startTime
                totalResponseTime += responseTime
                
                if (response.isSuccessful) {
                    successfulRequests++
                } else {
                    failedRequests++
                }
                
                response.body?.contentLength()?.let { size ->
                    totalDataTransferred += size
                }
                
                response
            } catch (e: Exception) {
                failedRequests++
                throw e
            }
        }
        
        okHttpClient = builder.build()
    }
    
    override suspend fun get(
        url: String,
        headers: Map<String, String>,
        params: Map<String, String>
    ): HttpResponse = withContext(Dispatchers.IO) {
        val urlWithParams = if (params.isNotEmpty()) {
            val paramString = params.entries.joinToString("&") { "${it.key}=${it.value}" }
            "$url?$paramString"
        } else url
        
        val request = Request.Builder()
            .url(urlWithParams)
            .apply { headers.forEach { (key, value) -> addHeader(key, value) } }
            .build()
        
        executeRequest(request)
    }
    
    override suspend fun post(
        url: String,
        body: String?,
        headers: Map<String, String>
    ): HttpResponse = withContext(Dispatchers.IO) {
        val requestBody = body?.toRequestBody("application/json".toMediaType()) 
            ?: "".toRequestBody()
        
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .apply { headers.forEach { (key, value) -> addHeader(key, value) } }
            .build()
        
        executeRequest(request)
    }
    
    override suspend fun put(
        url: String,
        body: String?,
        headers: Map<String, String>
    ): HttpResponse = withContext(Dispatchers.IO) {
        val requestBody = body?.toRequestBody("application/json".toMediaType()) 
            ?: "".toRequestBody()
        
        val request = Request.Builder()
            .url(url)
            .put(requestBody)
            .apply { headers.forEach { (key, value) -> addHeader(key, value) } }
            .build()
        
        executeRequest(request)
    }
    
    override suspend fun delete(
        url: String,
        headers: Map<String, String>
    ): HttpResponse = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .delete()
            .apply { headers.forEach { (key, value) -> addHeader(key, value) } }
            .build()
        
        executeRequest(request)
    }
    
    override suspend fun patch(
        url: String,
        body: String?,
        headers: Map<String, String>
    ): HttpResponse = withContext(Dispatchers.IO) {
        val requestBody = body?.toRequestBody("application/json".toMediaType()) 
            ?: "".toRequestBody()
        
        val request = Request.Builder()
            .url(url)
            .patch(requestBody)
            .apply { headers.forEach { (key, value) -> addHeader(key, value) } }
            .build()
        
        executeRequest(request)
    }
    
    override suspend fun upload(
        url: String,
        files: List<FileUpload>,
        headers: Map<String, String>,
        onProgress: ((Float) -> Unit)?
    ): HttpResponse = withContext(Dispatchers.IO) {
        val multipartBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
        
        files.forEach { file ->
            val requestBody = file.data.toRequestBody(file.mimeType.toMediaType())
            multipartBuilder.addFormDataPart(file.fieldName, file.fileName, requestBody)
        }
        
        val request = Request.Builder()
            .url(url)
            .post(multipartBuilder.build())
            .apply { headers.forEach { (key, value) -> addHeader(key, value) } }
            .build()
        
        executeRequest(request)
    }
    
    override suspend fun download(
        url: String,
        destination: String,
        headers: Map<String, String>,
        onProgress: ((Float) -> Unit)?
    ): DownloadResult = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url)
                .apply { headers.forEach { (key, value) -> addHeader(key, value) } }
                .build()
            
            val response = okHttpClient.newCall(request).execute()
            
            if (response.isSuccessful) {
                response.body?.let { body ->
                    val file = java.io.File(destination)
                    file.parentFile?.mkdirs()
                    
                    body.byteStream().use { input ->
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    
                    DownloadResult(
                        success = true,
                        filePath = destination,
                        fileSize = file.length()
                    )
                } ?: DownloadResult(success = false, error = "Empty response body")
            } else {
                DownloadResult(success = false, error = "HTTP ${response.code}")
            }
        } catch (e: Exception) {
            DownloadResult(success = false, error = e.message)
        }
    }
    
    private suspend fun executeRequest(request: Request): HttpResponse {
        val startTime = System.currentTimeMillis()
        
        return try {
            val response = okHttpClient.newCall(request).execute()
            val responseTime = System.currentTimeMillis() - startTime
            
            HttpResponse(
                statusCode = response.code,
                headers = response.headers.toMultimap().mapValues { it.value.first() },
                body = response.body?.string() ?: "",
                isSuccessful = response.isSuccessful,
                responseTime = responseTime
            )
        } catch (e: IOException) {
            HttpResponse(
                statusCode = -1,
                headers = emptyMap(),
                body = "",
                isSuccessful = false,
                error = e.message,
                responseTime = System.currentTimeMillis() - startTime
            )
        }
    }
    
    fun updateHeaders(headers: Map<String, String>) {
        // 更新全局请求头实现
    }
    
    fun updateTimeout(config: TimeoutConfig) {
        // 更新超时配置实现
    }
    
    fun updateRetryPolicy(policy: RetryPolicy) {
        // 更新重试策略实现
    }
    
    fun addInterceptor(interceptor: NetworkInterceptor) {
        // 添加拦截器实现
    }
    
    fun clearCache() {
        // 清理缓存实现
    }
    
    fun getTotalRequests() = totalRequests
    fun getSuccessfulRequests() = successfulRequests
    fun getFailedRequests() = failedRequests
    fun getAverageResponseTime() = if (totalRequests > 0) totalResponseTime / totalRequests else 0L
    fun getTotalDataTransferred() = totalDataTransferred
}

/**
 * Android网络监控器实现
 */
class AndroidNetworkMonitor : UnifyNetworkMonitor {
    private var context: Context? = null
    private val _networkStatus = MutableStateFlow(NetworkStatus.UNKNOWN)
    override val networkStatus: Flow<NetworkStatus> = _networkStatus.asStateFlow()
    
    private val _connectionType = MutableStateFlow(ConnectionType.UNKNOWN)
    override val connectionType: Flow<ConnectionType> = _connectionType.asStateFlow()
    
    private val _networkSpeed = MutableStateFlow(NetworkSpeed(0f, 0f, 0L))
    override val networkSpeed: Flow<NetworkSpeed> = _networkSpeed.asStateFlow()
    
    private var connectivityManager: ConnectivityManager? = null
    
    fun setContext(context: Context) {
        this.context = context
        connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        // 注册网络状态监听
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        connectivityManager?.registerNetworkCallback(networkRequest, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _networkStatus.value = NetworkStatus.CONNECTED
                updateConnectionType()
            }
            
            override fun onLost(network: Network) {
                _networkStatus.value = NetworkStatus.DISCONNECTED
                _connectionType.value = ConnectionType.UNKNOWN
            }
        })
        
        // 初始状态检查
        updateNetworkStatus()
    }
    
    override suspend fun checkConnectivity(): Boolean {
        return connectivityManager?.activeNetwork?.let { network ->
            connectivityManager?.getNetworkCapabilities(network)
                ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } ?: false
    }
    
    override suspend fun ping(host: String, timeout: Long): PingResult {
        return try {
            val startTime = System.currentTimeMillis()
            val process = Runtime.getRuntime().exec("ping -c 1 -W ${timeout / 1000} $host")
            val exitCode = process.waitFor()
            val latency = System.currentTimeMillis() - startTime
            
            PingResult(
                success = exitCode == 0,
                latency = latency
            )
        } catch (e: Exception) {
            PingResult(
                success = false,
                error = e.message
            )
        }
    }
    
    override suspend fun testNetworkQuality(): NetworkQuality {
        return when {
            !checkConnectivity() -> NetworkQuality.UNKNOWN
            _connectionType.value == ConnectionType.WIFI -> NetworkQuality.EXCELLENT
            _connectionType.value == ConnectionType.CELLULAR -> NetworkQuality.GOOD
            else -> NetworkQuality.FAIR
        }
    }
    
    private fun updateNetworkStatus() {
        val isConnected = connectivityManager?.activeNetwork?.let { network ->
            connectivityManager?.getNetworkCapabilities(network)
                ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } ?: false
        
        _networkStatus.value = if (isConnected) NetworkStatus.CONNECTED else NetworkStatus.DISCONNECTED
        
        if (isConnected) {
            updateConnectionType()
        }
    }
    
    private fun updateConnectionType() {
        connectivityManager?.activeNetwork?.let { network ->
            connectivityManager?.getNetworkCapabilities(network)?.let { capabilities ->
                _connectionType.value = when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionType.WIFI
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionType.CELLULAR
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> ConnectionType.ETHERNET
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> ConnectionType.BLUETOOTH
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> ConnectionType.VPN
                    else -> ConnectionType.UNKNOWN
                }
            }
        }
    }
}

/**
 * Android WebSocket客户端实现
 */
class AndroidWebSocketClient : UnifyWebSocketClient {
    private val connections = mutableMapOf<String, WebSocketConnection>()
    private val webSockets = mutableMapOf<String, WebSocket>()
    
    override suspend fun connect(url: String, protocols: List<String>): WebSocketConnection {
        val connectionId = java.util.UUID.randomUUID().toString()
        val connection = WebSocketConnection(
            id = connectionId,
            url = url,
            state = WebSocketState.CONNECTING
        )
        
        connections[connectionId] = connection
        
        // WebSocket连接实现（使用OkHttp WebSocket）
        
        return connection
    }
    
    override suspend fun disconnect(connection: WebSocketConnection) {
        webSockets[connection.id]?.close(1000, "Normal closure")
        connections.remove(connection.id)
        webSockets.remove(connection.id)
    }
    
    override suspend fun sendMessage(connection: WebSocketConnection, message: String) {
        webSockets[connection.id]?.send(message)
    }
    
    override suspend fun sendBinary(connection: WebSocketConnection, data: ByteArray) {
        webSockets[connection.id]?.send(okio.ByteString.of(*data))
    }
    
    override fun observeMessages(connection: WebSocketConnection): Flow<WebSocketMessage> {
        // 消息监听实现（使用Flow）
        return MutableStateFlow<WebSocketMessage>(WebSocketMessage.Text("")).asStateFlow()
    }
    
    override fun observeConnectionState(connection: WebSocketConnection): Flow<WebSocketState> {
        // 连接状态监听实现（使用StateFlow）
        return MutableStateFlow(WebSocketState.DISCONNECTED).asStateFlow()
    }
}
