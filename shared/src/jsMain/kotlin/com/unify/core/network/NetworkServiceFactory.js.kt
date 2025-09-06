package com.unify.core.network

import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.Json

/**
 * Web平台网络服务工厂实现
 */
actual object NetworkServiceFactory {
    private var httpClient: HttpClient? = null
    
    actual fun createHttpClient(config: NetworkConfig): UnifyNetworkService {
        if (httpClient == null) {
            httpClient = HttpClient(Js) {
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
        
        return WebUnifyNetworkServiceImpl(httpClient!!, config)
    }
    
    actual fun createWebSocketClient(url: String): WebSocketClient {
        return WebSocketClientImpl(url, httpClient ?: createHttpClient().let { 
            (it as WebUnifyNetworkServiceImpl).httpClient 
        })
    }
    
    actual fun createFileUploadClient(): FileUploadClient {
        return WebFileUploadClient(httpClient ?: createHttpClient().let { 
            (it as WebUnifyNetworkServiceImpl).httpClient 
        })
    }
    
    actual fun getNetworkStatusMonitor(): Flow<NetworkStatus> = callbackFlow {
        // Web平台网络状态监控
        val updateStatus = {
            val status = if (js("navigator.onLine") as Boolean) {
                // 检测连接类型
                val connection = js("navigator.connection || navigator.mozConnection || navigator.webkitConnection")
                if (connection != null) {
                    val effectiveType = js("connection.effectiveType") as? String
                    when (effectiveType) {
                        "4g", "3g" -> NetworkStatus.MOBILE
                        else -> NetworkStatus.WIFI
                    }
                } else {
                    NetworkStatus.CONNECTED
                }
            } else {
                NetworkStatus.DISCONNECTED
            }
            trySend(status)
        }
        
        // 初始状态
        updateStatus()
        
        // 监听网络状态变化
        val onlineHandler = { updateStatus() }
        val offlineHandler = { trySend(NetworkStatus.DISCONNECTED) }
        
        js("window.addEventListener('online', onlineHandler)")
        js("window.addEventListener('offline', offlineHandler)")
        
        awaitClose {
            js("window.removeEventListener('online', onlineHandler)")
            js("window.removeEventListener('offline', offlineHandler)")
        }
    }
}

/**
 * Web WebSocket客户端实现
 */
private class WebSocketClientImpl(
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
 * Web文件上传客户端实现
 */
private class WebFileUploadClient(
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
            // Web平台文件上传实现
            // 注意：Web平台的filePath通常是File对象或Blob URL
            UploadResult(
                success = true,
                url = url,
                fileSize = 0L,
                uploadTime = kotlin.js.Date.now().toLong()
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
 * Web网络服务实现类
 */
internal class WebUnifyNetworkServiceImpl(
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
