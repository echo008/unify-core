package com.unify.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.unify.core.network.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

/**
 * Android平台UnifyNetworkManager实现
 * 基于OkHttp和Android ConnectivityManager
 */
class UnifyNetworkManagerImpl(private val context: Context) : UnifyNetworkManager {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    private var baseUrl: String = ""
    private var defaultHeaders: Map<String, String> = emptyMap()
    private var timeoutMillis: Long = 30000L
    private var cacheEnabled: Boolean = false
    private var retryPolicy: RetryPolicy = RetryPolicy()
    
    // 网络状态监控
    private val _networkStatus = MutableStateFlow(getCurrentNetworkStatus())
    
    // OkHttp客户端
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(timeoutMillis, TimeUnit.MILLISECONDS)
            .readTimeout(timeoutMillis, TimeUnit.MILLISECONDS)
            .writeTimeout(timeoutMillis, TimeUnit.MILLISECONDS)
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val requestBuilder = originalRequest.newBuilder()
                
                // 添加默认头部
                defaultHeaders.forEach { (key, value) ->
                    requestBuilder.addHeader(key, value)
                }
                
                chain.proceed(requestBuilder.build())
            }
            .build()
    }
    
    init {
        setupNetworkMonitoring()
    }
    
    override suspend fun get(url: String, headers: Map<String, String>): NetworkResponse {
        return executeRequest {
            Request.Builder()
                .url(resolveUrl(url))
                .apply { headers.forEach { (key, value) -> addHeader(key, value) } }
                .get()
                .build()
        }
    }
    
    override suspend fun post(url: String, body: String, headers: Map<String, String>): NetworkResponse {
        return executeRequest {
            val requestBody = body.toRequestBody("application/json".toMediaType())
            Request.Builder()
                .url(resolveUrl(url))
                .apply { headers.forEach { (key, value) -> addHeader(key, value) } }
                .post(requestBody)
                .build()
        }
    }
    
    override suspend fun put(url: String, body: String, headers: Map<String, String>): NetworkResponse {
        return executeRequest {
            val requestBody = body.toRequestBody("application/json".toMediaType())
            Request.Builder()
                .url(resolveUrl(url))
                .apply { headers.forEach { (key, value) -> addHeader(key, value) } }
                .put(requestBody)
                .build()
        }
    }
    
    override suspend fun delete(url: String, headers: Map<String, String>): NetworkResponse {
        return executeRequest {
            Request.Builder()
                .url(resolveUrl(url))
                .apply { headers.forEach { (key, value) -> addHeader(key, value) } }
                .delete()
                .build()
        }
    }
    
    override suspend fun patch(url: String, body: String, headers: Map<String, String>): NetworkResponse {
        return executeRequest {
            val requestBody = body.toRequestBody("application/json".toMediaType())
            Request.Builder()
                .url(resolveUrl(url))
                .apply { headers.forEach { (key, value) -> addHeader(key, value) } }
                .patch(requestBody)
                .build()
        }
    }
    
    override suspend fun uploadFile(url: String, filePath: String, headers: Map<String, String>): NetworkResponse {
        return executeRequest {
            val file = File(filePath)
            val requestBody = file.asRequestBody("application/octet-stream".toMediaType())
            Request.Builder()
                .url(resolveUrl(url))
                .apply { headers.forEach { (key, value) -> addHeader(key, value) } }
                .post(requestBody)
                .build()
        }
    }
    
    override suspend fun downloadFile(url: String, savePath: String, headers: Map<String, String>): NetworkResponse {
        return executeRequest { 
            Request.Builder()
                .url(resolveUrl(url))
                .apply { headers.forEach { (key, value) -> addHeader(key, value) } }
                .get()
                .build()
        }.also { response ->
            if (response.isSuccess) {
                // 保存文件到指定路径
                File(savePath).writeText(response.body)
            }
        }
    }
    
    override fun connectWebSocket(url: String, listener: WebSocketListener): WebSocketConnection {
        val request = Request.Builder().url(url).build()
        val webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                listener.onOpen()
            }
            
            override fun onMessage(webSocket: WebSocket, text: String) {
                listener.onMessage(text)
            }
            
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                listener.onError(t.message ?: "WebSocket error")
            }
            
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                listener.onClose(code, reason)
            }
        })
        
        return object : WebSocketConnection {
            override fun send(message: String) {
                webSocket.send(message)
            }
            
            override fun close() {
                webSocket.close(1000, "Normal closure")
            }
            
            override fun isConnected(): Boolean {
                // OkHttp WebSocket doesn't provide direct connection status
                return true // 简化实现
            }
        }
    }
    
    override fun observeNetworkStatus(): Flow<NetworkStatus> = _networkStatus.asStateFlow()
    
    override fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
    
    override fun getNetworkType(): NetworkType {
        val network = connectivityManager.activeNetwork ?: return NetworkType.UNKNOWN
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return NetworkType.UNKNOWN
        
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.CELLULAR
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.ETHERNET
            else -> NetworkType.UNKNOWN
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
        // OkHttp缓存清理实现
    }
    
    override fun setRetryPolicy(policy: RetryPolicy) {
        this.retryPolicy = policy
    }
    
    override fun getRetryPolicy(): RetryPolicy = retryPolicy
    
    private suspend fun executeRequest(requestBuilder: () -> Request): NetworkResponse {
        return suspendCancellableCoroutine { continuation ->
            val request = requestBuilder()
            
            okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
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
                
                override fun onResponse(call: Call, response: Response) {
                    val responseHeaders = response.headers.toMultimap()
                        .mapValues { it.value.firstOrNull() ?: "" }
                    
                    continuation.resume(
                        NetworkResponse(
                            statusCode = response.code,
                            body = response.body?.string() ?: "",
                            headers = responseHeaders,
                            isSuccess = response.isSuccessful,
                            error = if (!response.isSuccessful) response.message else null
                        )
                    )
                }
            })
        }
    }
    
    private fun resolveUrl(url: String): String {
        return if (url.startsWith("http")) url else "$baseUrl$url"
    }
    
    private fun getCurrentNetworkStatus(): NetworkStatus {
        return if (isNetworkAvailable()) NetworkStatus.CONNECTED else NetworkStatus.DISCONNECTED
    }
    
    private fun setupNetworkMonitoring() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        connectivityManager.registerNetworkCallback(networkRequest, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _networkStatus.value = NetworkStatus.CONNECTED
            }
            
            override fun onLost(network: Network) {
                _networkStatus.value = NetworkStatus.DISCONNECTED
            }
        })
    }
}

actual object UnifyNetworkManagerFactory {
    private var context: Context? = null
    
    fun initialize(context: Context) {
        this.context = context.applicationContext
    }
    
    actual fun create(): UnifyNetworkManager {
        return UnifyNetworkManagerImpl(
            context ?: throw IllegalStateException("UnifyNetworkManagerFactory not initialized. Call initialize(context) first.")
        )
    }
}
