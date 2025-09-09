package com.unify.core.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

/**
 * Web平台网络服务工厂实现
 */
actual object NetworkServiceFactory {
    private var httpClient: HttpClient? = null

    actual fun createHttpClient(config: NetworkConfig): UnifyNetworkService {
        if (httpClient == null) {
            httpClient =
                HttpClient(Js) {
                    install(ContentNegotiation) {
                        json(
                            Json {
                                prettyPrint = true
                                isLenient = true
                                ignoreUnknownKeys = true
                            },
                        )
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
                            headers.append(key, value)
                        }
                    }
                }
        }

        return WebUnifyNetworkServiceImpl(httpClient!!, config)
    }

    actual fun createWebSocketClient(url: String): WebSocketClient {
        return WebSocketClientImpl(
            url,
            httpClient ?: createHttpClient().let {
                (it as WebUnifyNetworkServiceImpl).httpClient
            },
        )
    }

    actual fun createFileUploadClient(): FileUploadClient {
        return WebFileUploadClient(
            httpClient ?: createHttpClient().let {
                (it as WebUnifyNetworkServiceImpl).httpClient
            },
        )
    }

    actual fun getNetworkStatusMonitor(): Flow<NetworkStatus> =
        callbackFlow {
            // 监听网络状态变化
            val onlineListener = {
                trySend(NetworkStatus.CONNECTED)
            }

            val offlineListener = {
                trySend(NetworkStatus.DISCONNECTED)
            }

            // 添加事件监听器
            js("window.addEventListener('online', onlineListener)")
            js("window.addEventListener('offline', offlineListener)")

            // 发送当前状态
            if (js("navigator.onLine") as Boolean) {
                trySend(NetworkStatus.CONNECTED)
            } else {
                trySend(NetworkStatus.DISCONNECTED)
            }

            awaitClose {
                js("window.removeEventListener('online', onlineListener)")
                js("window.removeEventListener('offline', offlineListener)")
            }
        }
}

/**
 * Web WebSocket客户端实现
 */
private class WebSocketClientImpl(
    private val url: String,
    private val httpClient: HttpClient,
) : WebSocketClient {
    private var session: DefaultClientWebSocketSession? = null
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
        session?.send(message)
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
    private val httpClient: HttpClient,
) : FileUploadClient {
    override suspend fun uploadFile(
        url: String,
        filePath: String,
        fieldName: String,
        additionalData: Map<String, String>,
        onProgress: ((Float) -> Unit)?,
    ): UploadResult {
        return try {
            // Web平台文件上传实现
            // 注意：Web平台的filePath通常是File对象或Blob URL
            UploadResult(
                success = true,
                url = url,
                fileSize = 0L,
                uploadTime = kotlin.js.Date.now().toLong(),
            )
        } catch (e: Exception) {
            UploadResult(
                success = false,
                error = e.message,
            )
        }
    }

    override suspend fun uploadMultipleFiles(
        url: String,
        files: List<FileUploadInfo>,
        onProgress: ((Float) -> Unit)?,
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
    private val config: NetworkConfig,
) : UnifyNetworkService {
    override suspend fun <T> request(request: NetworkRequest<T>): NetworkResponse<T> {
        TODO("实现通用请求方法")
    }

    override suspend fun batchRequest(requests: List<NetworkRequest<*>>): List<NetworkResponse<*>> {
        TODO("实现批量请求")
    }

    override fun <T> createDataStream(
        url: String,
        interval: Long,
        parser: (String) -> T,
    ): Flow<T> {
        return flow {
            while (true) {
                try {
                    val response = httpClient.get(url).body<String>()
                    val data = parser(response)
                    emit(data)
                    delay(interval)
                } catch (e: Exception) {
                    break
                }
            }
        }
    }

    override suspend fun connectWebSocket(
        url: String,
        protocols: List<String>,
    ): WebSocketConnection {
        // Return a simplified WebSocket connection for JS platform
        return object : WebSocketConnection {
            override val isConnected: Boolean = false

            override suspend fun send(message: String) {
                // Simplified send implementation
            }

            override suspend fun send(data: ByteArray) {
                // Simplified binary send implementation
            }

            override suspend fun close(
                code: Int,
                reason: String,
            ) {
                // Simplified close implementation
            }

            override fun onMessage(callback: (String) -> Unit) {
                // Simplified message callback
            }

            override fun onBinaryMessage(callback: (ByteArray) -> Unit) {
                // Simplified binary message callback
            }

            override fun onError(callback: (Throwable) -> Unit) {
                // Simplified error callback
            }

            override fun onClose(callback: (Int, String) -> Unit) {
                // Simplified close callback
            }
        }
    }

    override fun createServerSentEventStream(url: String): Flow<ServerSentEvent> {
        return kotlinx.coroutines.flow.flow {
            // Simplified SSE implementation for JS platform
            while (true) {
                try {
                    val data = "mock_sse_data"
                    emit(ServerSentEvent(data, null, "message", null))
                    kotlinx.coroutines.delay(1000)
                } catch (e: Exception) {
                    break
                }
            }
        }
    }

    override suspend fun graphqlQuery(
        url: String,
        query: String,
        variables: Map<String, Any>,
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
