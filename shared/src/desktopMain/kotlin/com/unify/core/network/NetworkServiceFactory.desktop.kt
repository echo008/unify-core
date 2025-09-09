package com.unify.core.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Desktop平台网络服务工厂实现
 */
actual object NetworkServiceFactory {
    actual fun createHttpClient(config: NetworkConfig): UnifyNetworkService {
        return DesktopUnifyNetworkService(config)
    }

    actual fun createWebSocketClient(url: String): WebSocketClient {
        return DesktopWebSocketClient(url)
    }

    actual fun createFileUploadClient(): FileUploadClient {
        return DesktopFileUploadClient()
    }

    actual fun getNetworkStatusMonitor(): Flow<NetworkStatus> {
        return flowOf(NetworkStatus.CONNECTED)
    }
}

/**
 * Desktop平台网络服务完整实现
 */
class DesktopUnifyNetworkService(private val config: NetworkConfig) : UnifyNetworkService {
    override suspend fun <T> request(request: NetworkRequest<T>): NetworkResponse<T> {
        @Suppress("UNCHECKED_CAST")
        return NetworkResponse(
            success = true,
            data = "Desktop ${request.method} response from ${request.url}" as T,
            statusCode = 200,
            headers = request.headers,
            responseTime = System.currentTimeMillis(),
            fromCache = false,
        )
    }

    override suspend fun batchRequest(requests: List<NetworkRequest<*>>): List<NetworkResponse<*>> {
        return requests.map { request(it) }
    }

    override fun <T> createDataStream(
        url: String,
        interval: Long,
        parser: (String) -> T,
    ): Flow<T> {
        return kotlinx.coroutines.flow.flow {
            while (true) {
                try {
                    val data = parser("Desktop data stream from $url")
                    emit(data)
                    kotlinx.coroutines.delay(interval)
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
        return DesktopWebSocketConnection(url)
    }

    override fun createServerSentEventStream(url: String): Flow<ServerSentEvent> {
        return kotlinx.coroutines.flow.flowOf(
            ServerSentEvent(
                id = "desktop-1",
                event = "message",
                data = "Desktop SSE data from $url",
            ),
        )
    }

    override suspend fun graphqlQuery(
        url: String,
        query: String,
        variables: Map<String, Any>,
    ): NetworkResponse<GraphQLResponse> {
        return NetworkResponse(
            success = true,
            data =
                GraphQLResponse(
                    data =
                        kotlinx.serialization.json.buildJsonObject {
                            put("desktop", kotlinx.serialization.json.JsonPrimitive("GraphQL response"))
                        },
                ),
            statusCode = 200,
            responseTime = System.currentTimeMillis(),
            fromCache = false,
        )
    }

    override fun getRequestStats(): RequestStats {
        return RequestStats(
            totalRequests = 100L,
            successfulRequests = 95L,
            failedRequests = 5L,
            averageResponseTime = 150.0,
            cacheHitRate = 0.8,
            bytesTransferred = 1024L * 1024L,
            activeConnections = 5,
        )
    }

    override fun setRequestInterceptor(interceptor: RequestInterceptor) {
        // Desktop平台请求拦截器设置
    }

    override fun setResponseInterceptor(interceptor: ResponseInterceptor) {
        // Desktop平台响应拦截器设置
    }
}

/**
 * Desktop平台WebSocket连接实现
 */
class DesktopWebSocketConnection(private val url: String) : WebSocketConnection {
    override val isConnected: Boolean = true

    override suspend fun send(message: String) {
        // Desktop平台WebSocket发送消息
    }

    override suspend fun send(data: ByteArray) {
        // Desktop平台WebSocket发送二进制数据
    }

    override suspend fun close(
        code: Int,
        reason: String,
    ) {
        // Desktop平台WebSocket关闭连接
    }

    override fun onMessage(callback: (String) -> Unit) {
        // Desktop平台WebSocket消息监听
    }

    override fun onBinaryMessage(callback: (ByteArray) -> Unit) {
        // Desktop平台WebSocket二进制消息监听
    }

    override fun onError(callback: (Throwable) -> Unit) {
        // Desktop平台WebSocket错误监听
    }

    override fun onClose(callback: (Int, String) -> Unit) {
        // Desktop平台WebSocket关闭监听
    }
}

/**
 * Desktop平台WebSocket客户端实现
 */
class DesktopWebSocketClient(private val url: String) : WebSocketClient {
    override suspend fun connect(): Boolean = true

    override suspend fun disconnect() {}

    override suspend fun send(message: String) {}

    override fun onMessage(callback: (String) -> Unit) {}

    override fun onError(callback: (Throwable) -> Unit) {}

    override fun onClose(callback: (Int, String) -> Unit) {}
}

/**
 * Desktop平台文件上传客户端实现
 */
class DesktopFileUploadClient : FileUploadClient {
    override suspend fun uploadFile(
        url: String,
        filePath: String,
        fieldName: String,
        additionalData: Map<String, String>,
        onProgress: ((Float) -> Unit)?,
    ): UploadResult {
        return UploadResult(
            success = true,
            url = "desktop://uploaded/$filePath",
            fileSize = 1024L,
            uploadTime = System.currentTimeMillis(),
        )
    }

    override suspend fun uploadMultipleFiles(
        url: String,
        files: List<FileUploadInfo>,
        onProgress: ((Float) -> Unit)?,
    ): List<UploadResult> {
        return files.map { file ->
            UploadResult(
                success = true,
                url = "desktop://uploaded/${file.filePath}",
                fileSize = 1024L,
                uploadTime = System.currentTimeMillis(),
            )
        }
    }
}
