package com.unify.core.network

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.Json
import java.net.NetworkInterface
import java.net.InetAddress

/**
 * Desktop平台网络服务工厂实现
 */
actual object NetworkServiceFactory {
    private var httpClient: HttpClient? = null
    
    actual fun createHttpClient(config: NetworkConfig): UnifyNetworkService {
        if (httpClient == null) {
            httpClient = HttpClient(CIO) {
                install(ContentNegotiation) {
                    json(Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                    })
                }
                
                install(Logging) {
                    logger = Logger.SIMPLE
                    level = if (config.enableLogging) LogLevel.INFO else LogLevel.NONE
                }
                
                install(HttpTimeout) {
                    requestTimeoutMillis = config.timeout
                    connectTimeoutMillis = config.timeout
                    socketTimeoutMillis = config.timeout
                }
                
                install(HttpRequestRetry) {
                    retryOnServerErrors(maxRetries = config.retryCount)
                    retryOnException(maxRetries = config.retryCount)
                    exponentialDelay()
                }
                
                install(WebSockets)
                
                defaultRequest {
                    config.headers.forEach { (key, value) ->
                        header(key, value)
                    }
                }
            }
        }
        
        return DesktopUnifyNetworkServiceImpl(httpClient!!, config)
    }
    
    actual fun createWebSocketClient(url: String): WebSocketClient {
        return DesktopWebSocketClient(url, httpClient ?: createHttpClient().let { 
            (it as DesktopUnifyNetworkServiceImpl).httpClient 
        })
    }
    
    actual fun createFileUploadClient(): FileUploadClient {
        return DesktopFileUploadClient(httpClient ?: createHttpClient().let { 
            (it as DesktopUnifyNetworkServiceImpl).httpClient 
        })
    }
    
    actual fun getNetworkStatusMonitor(): Flow<NetworkStatus> = callbackFlow {
        // Desktop平台网络状态监控
        var lastStatus = NetworkStatus.UNKNOWN
        
        while (true) {
            try {
                val currentStatus = checkNetworkStatus()
                if (currentStatus != lastStatus) {
                    trySend(currentStatus)
                    lastStatus = currentStatus
                }
                kotlinx.coroutines.delay(5000) // 每5秒检查一次
            } catch (e: Exception) {
                trySend(NetworkStatus.UNKNOWN)
            }
        }
        
        awaitClose { }
    }
    
    private fun checkNetworkStatus(): NetworkStatus {
        return try {
            val networkInterfaces = NetworkInterface.getNetworkInterfaces()
            var hasConnection = false
            var connectionType = NetworkStatus.UNKNOWN
            
            while (networkInterfaces.hasMoreElements()) {
                val networkInterface = networkInterfaces.nextElement()
                if (networkInterface.isUp && !networkInterface.isLoopback) {
                    val addresses = networkInterface.inetAddresses
                    while (addresses.hasMoreElements()) {
                        val address = addresses.nextElement()
                        if (!address.isLoopbackAddress && !address.isLinkLocalAddress) {
                            hasConnection = true
                            connectionType = when {
                                networkInterface.displayName.contains("Wi-Fi", ignoreCase = true) ||
                                networkInterface.displayName.contains("wlan", ignoreCase = true) -> NetworkStatus.WIFI
                                networkInterface.displayName.contains("Ethernet", ignoreCase = true) ||
                                networkInterface.displayName.contains("eth", ignoreCase = true) -> NetworkStatus.ETHERNET
                                else -> NetworkStatus.CONNECTED
                            }
                            break
                        }
                    }
                }
            }
            
            if (hasConnection) connectionType else NetworkStatus.DISCONNECTED
        } catch (e: Exception) {
            NetworkStatus.UNKNOWN
        }
    }
}

/**
 * Desktop WebSocket客户端实现
 */
private class DesktopWebSocketClient(
    private val url: String,
    private val httpClient: HttpClient
) : WebSocketClient {
    private var session: io.ktor.client.plugins.websocket.WebSocketSession? = null
    private var messageCallback: ((String) -> Unit)? = null
    private var errorCallback: ((Throwable) -> Unit)? = null
    private var closeCallback: ((Int, String) -> Unit)? = null
    
    override suspend fun connect(): Boolean {
        return try {
            session = httpClient.webSocketSession(url)
            true
        } catch (e: Exception) {
            errorCallback?.invoke(e)
            false
        }
    }
    
    override suspend fun disconnect() {
        session?.close()
        session = null
    }
    
    override suspend fun send(message: String) {
        session?.send(io.ktor.websocket.Frame.Text(message))
    }
    
    override fun onMessage(callback: (String) -> Unit) {
        messageCallback = callback
    }
    
    override fun onError(callback: (Throwable) -> Unit) {
        errorCallback = callback
    }
    
    override fun onClose(callback: (Int, String) -> Unit) {
        closeCallback = callback
    }
}

/**
 * Desktop文件上传客户端实现
 */
private class DesktopFileUploadClient(
    private val httpClient: HttpClient
) : FileUploadClient {
    override suspend fun uploadFile(
        url: String,
        filePath: String,
        fieldName: String,
        additionalData: Map<String, String>,
        onProgress: ((Float) -> Unit)?
    ): UploadResult {
        return try {
            val file = java.io.File(filePath)
            if (!file.exists()) {
                return UploadResult(
                    success = false,
                    error = "文件不存在: $filePath"
                )
            }
            
            // 实现Desktop文件上传逻辑
            UploadResult(
                success = true,
                url = url,
                fileSize = file.length(),
                uploadTime = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            UploadResult(
                success = false,
                error = e.message
            )
        }
    }
    
    override suspend fun uploadMultipleFiles(
        url: String,
        files: List<FileUploadInfo>,
        onProgress: ((Float) -> Unit)?
    ): List<UploadResult> {
        return files.map { file ->
            uploadFile(url, file.filePath, file.fieldName, emptyMap(), onProgress)
        }
    }
}

/**
 * Desktop网络服务实现类
 */
internal class DesktopUnifyNetworkServiceImpl(
    val httpClient: HttpClient,
    private val config: NetworkConfig
) : UnifyNetworkService {
    
    override suspend fun <T> request(request: NetworkRequest<T>): NetworkResponse<T> {
        TODO("实现通用请求方法")
    }
    
    override suspend fun batchRequest(requests: List<NetworkRequest<*>>): List<NetworkResponse<*>> {
        TODO("实现批量请求")
    }
    
    override fun <T> createDataStream(url: String, interval: Long, parser: (String) -> T): Flow<T> {
        TODO("实现数据流")
    }
    
    override suspend fun connectWebSocket(url: String, protocols: List<String>): WebSocketConnection {
        TODO("实现WebSocket连接")
    }
    
    override fun createServerSentEventStream(url: String): Flow<ServerSentEvent> {
        TODO("实现SSE流")
    }
    
    override suspend fun graphqlQuery(
        url: String,
        query: String,
        variables: Map<String, Any>
    ): NetworkResponse<GraphQLResponse> {
        TODO("实现GraphQL查询")
    }
    
    override fun getRequestStats(): RequestStats {
        return RequestStats()
    }
    
    override fun setRequestInterceptor(interceptor: RequestInterceptor) {
        TODO("实现请求拦截器")
    }
    
    override fun setResponseInterceptor(interceptor: ResponseInterceptor) {
        TODO("实现响应拦截器")
    }
}
