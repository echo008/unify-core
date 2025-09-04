package com.unify.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Android平台网络管理器实现
 * 基于OkHttp和Android ConnectivityManager实现
 */
class AndroidUnifyNetworkManager(private val context: Context) : UnifyNetworkManager {
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val _connectionState = MutableStateFlow(NetworkConnectionState.UNKNOWN)
    private val _networkType = MutableStateFlow(NetworkType.UNKNOWN)
    
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(LoggingInterceptor())
        .addInterceptor(RetryInterceptor())
        .build()
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    init {
        registerNetworkCallback()
        updateConnectionState()
    }
    
    override suspend fun <T> get(
        url: String,
        headers: Map<String, String>,
        serializer: kotlinx.serialization.KSerializer<T>
    ): NetworkResult<T> {
        return executeRequest(
            request = Request.Builder()
                .url(url)
                .apply { headers.forEach { (key, value) -> addHeader(key, value) } }
                .get()
                .build(),
            serializer = serializer
        )
    }
    
    override suspend fun <T> post(
        url: String,
        body: Any?,
        headers: Map<String, String>,
        serializer: kotlinx.serialization.KSerializer<T>
    ): NetworkResult<T> {
        val requestBody = body?.let { bodyData ->
            val jsonString = when (bodyData) {
                is String -> bodyData
                else -> json.encodeToString(kotlinx.serialization.serializers.serializer(), bodyData)
            }
            jsonString.toRequestBody("application/json".toMediaType())
        } ?: "".toRequestBody()
        
        return executeRequest(
            request = Request.Builder()
                .url(url)
                .apply { headers.forEach { (key, value) -> addHeader(key, value) } }
                .post(requestBody)
                .build(),
            serializer = serializer
        )
    }
    
    override suspend fun <T> put(
        url: String,
        body: Any?,
        headers: Map<String, String>,
        serializer: kotlinx.serialization.KSerializer<T>
    ): NetworkResult<T> {
        val requestBody = body?.let { bodyData ->
            val jsonString = when (bodyData) {
                is String -> bodyData
                else -> json.encodeToString(kotlinx.serialization.serializers.serializer(), bodyData)
            }
            jsonString.toRequestBody("application/json".toMediaType())
        } ?: "".toRequestBody()
        
        return executeRequest(
            request = Request.Builder()
                .url(url)
                .apply { headers.forEach { (key, value) -> addHeader(key, value) } }
                .put(requestBody)
                .build(),
            serializer = serializer
        )
    }
    
    override suspend fun <T> delete(
        url: String,
        headers: Map<String, String>,
        serializer: kotlinx.serialization.KSerializer<T>
    ): NetworkResult<T> {
        return executeRequest(
            request = Request.Builder()
                .url(url)
                .apply { headers.forEach { (key, value) -> addHeader(key, value) } }
                .delete()
                .build(),
            serializer = serializer
        )
    }
    
    override suspend fun downloadFile(
        url: String,
        destinationPath: String,
        headers: Map<String, String>,
        onProgress: ((Long, Long) -> Unit)?
    ): NetworkResult<String> {
        return try {
            val request = Request.Builder()
                .url(url)
                .apply { headers.forEach { (key, value) -> addHeader(key, value) } }
                .build()
            
            val response = okHttpClient.newCall(request).execute()
            
            if (response.isSuccessful) {
                val body = response.body
                if (body != null) {
                    val file = java.io.File(destinationPath)
                    file.parentFile?.mkdirs()
                    
                    val contentLength = body.contentLength()
                    var downloadedBytes = 0L
                    
                    body.byteStream().use { inputStream ->
                        file.outputStream().use { outputStream ->
                            val buffer = ByteArray(8192)
                            var bytesRead: Int
                            
                            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                outputStream.write(buffer, 0, bytesRead)
                                downloadedBytes += bytesRead
                                onProgress?.invoke(downloadedBytes, contentLength)
                            }
                        }
                    }
                    
                    NetworkResult.Success(destinationPath)
                } else {
                    NetworkResult.Error(NetworkException("Empty response body", null))
                }
            } else {
                NetworkResult.Error(NetworkException("HTTP ${response.code}: ${response.message}", null))
            }
        } catch (e: Exception) {
            NetworkResult.Error(NetworkException("Download failed: ${e.message}", e))
        }
    }
    
    override suspend fun uploadFile(
        url: String,
        filePath: String,
        fieldName: String,
        headers: Map<String, String>,
        onProgress: ((Long, Long) -> Unit)?
    ): NetworkResult<String> {
        return try {
            val file = java.io.File(filePath)
            if (!file.exists()) {
                return NetworkResult.Error(NetworkException("File not found: $filePath", null))
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
                .apply { headers.forEach { (key, value) -> addHeader(key, value) } }
                .post(requestBody)
                .build()
            
            val response = okHttpClient.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                NetworkResult.Success(responseBody)
            } else {
                NetworkResult.Error(NetworkException("HTTP ${response.code}: ${response.message}", null))
            }
        } catch (e: Exception) {
            NetworkResult.Error(NetworkException("Upload failed: ${e.message}", e))
        }
    }
    
    override fun observeConnectionState(): Flow<NetworkConnectionState> {
        return _connectionState.asStateFlow()
    }
    
    override fun observeNetworkType(): Flow<NetworkType> {
        return _networkType.asStateFlow()
    }
    
    override suspend fun isConnected(): Boolean {
        return try {
            val activeNetwork = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun getConnectionInfo(): NetworkConnectionInfo {
        return try {
            val activeNetwork = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            
            val type = when {
                networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> NetworkType.WIFI
                networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> NetworkType.CELLULAR
                networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> NetworkType.ETHERNET
                else -> NetworkType.UNKNOWN
            }
            
            val isConnected = networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            val isMetered = connectivityManager.isActiveNetworkMetered
            
            NetworkConnectionInfo(
                isConnected = isConnected,
                networkType = type,
                isMetered = isMetered,
                signalStrength = getSignalStrength(),
                ipAddress = getIpAddress()
            )
        } catch (e: Exception) {
            NetworkConnectionInfo(
                isConnected = false,
                networkType = NetworkType.UNKNOWN,
                isMetered = false,
                signalStrength = 0,
                ipAddress = ""
            )
        }
    }
    
    override suspend fun ping(host: String, timeout: Long): NetworkResult<Long> {
        return try {
            val startTime = System.currentTimeMillis()
            
            val request = Request.Builder()
                .url("http://$host")
                .head()
                .build()
            
            val response = okHttpClient.newCall(request).execute()
            val endTime = System.currentTimeMillis()
            
            if (response.isSuccessful) {
                NetworkResult.Success(endTime - startTime)
            } else {
                NetworkResult.Error(NetworkException("Ping failed: ${response.code}", null))
            }
        } catch (e: Exception) {
            NetworkResult.Error(NetworkException("Ping failed: ${e.message}", e))
        }
    }
    
    // 私有辅助方法
    
    private suspend fun <T> executeRequest(
        request: Request,
        serializer: kotlinx.serialization.KSerializer<T>
    ): NetworkResult<T> = suspendCancellableCoroutine { continuation ->
        val call = okHttpClient.newCall(request)
        
        continuation.invokeOnCancellation {
            call.cancel()
        }
        
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                continuation.resume(NetworkResult.Error(NetworkException("Request failed: ${e.message}", e)))
            }
            
            override fun onResponse(call: Call, response: Response) {
                try {
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        if (responseBody != null) {
                            val result = json.decodeFromString(serializer, responseBody)
                            continuation.resume(NetworkResult.Success(result))
                        } else {
                            continuation.resume(NetworkResult.Error(NetworkException("Empty response body", null)))
                        }
                    } else {
                        continuation.resume(NetworkResult.Error(NetworkException("HTTP ${response.code}: ${response.message}", null)))
                    }
                } catch (e: Exception) {
                    continuation.resume(NetworkResult.Error(NetworkException("Response parsing failed: ${e.message}", e)))
                }
            }
        })
    }
    
    private fun registerNetworkCallback() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        connectivityManager.registerNetworkCallback(networkRequest, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                updateConnectionState()
            }
            
            override fun onLost(network: Network) {
                updateConnectionState()
            }
            
            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                updateConnectionState()
            }
        })
    }
    
    private fun updateConnectionState() {
        try {
            val activeNetwork = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            
            val isConnected = networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            
            _connectionState.value = if (isConnected) {
                NetworkConnectionState.CONNECTED
            } else {
                NetworkConnectionState.DISCONNECTED
            }
            
            _networkType.value = when {
                networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> NetworkType.WIFI
                networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> NetworkType.CELLULAR
                networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> NetworkType.ETHERNET
                else -> NetworkType.UNKNOWN
            }
        } catch (e: Exception) {
            _connectionState.value = NetworkConnectionState.DISCONNECTED
            _networkType.value = NetworkType.UNKNOWN
        }
    }
    
    private fun getSignalStrength(): Int {
        // 获取信号强度的实现
        return try {
            // 这里需要使用TelephonyManager获取信号强度
            // 简化实现，返回默认值
            75
        } catch (e: Exception) {
            0
        }
    }
    
    private fun getIpAddress(): String {
        return try {
            val activeNetwork = connectivityManager.activeNetwork
            val linkProperties = connectivityManager.getLinkProperties(activeNetwork)
            linkProperties?.linkAddresses?.firstOrNull()?.address?.hostAddress ?: ""
        } catch (e: Exception) {
            ""
        }
    }
}

/**
 * 日志拦截器
 */
private class LoggingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startTime = System.nanoTime()
        
        val response = chain.proceed(request)
        
        val endTime = System.nanoTime()
        val duration = (endTime - startTime) / 1_000_000 // 转换为毫秒
        
        println("Network Request: ${request.method} ${request.url} - ${response.code} (${duration}ms)")
        
        return response
    }
}

/**
 * 重试拦截器
 */
private class RetryInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response = chain.proceed(request)
        var tryCount = 0
        
        while (!response.isSuccessful && tryCount < 3) {
            tryCount++
            response.close()
            response = chain.proceed(request)
        }
        
        return response
    }
}
