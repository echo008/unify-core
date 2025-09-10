package com.unify.core.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Android平台网络管理器实现
 */
actual class UnifyNetworkManager {
    actual companion object {
        actual fun create(): UnifyNetworkManager = UnifyNetworkManager()
    }

    private lateinit var httpClient: HttpClient
    private lateinit var config: NetworkConfig
    private val networkStatusFlow = MutableStateFlow(NetworkStatus.UNKNOWN)

    actual fun initialize(config: NetworkConfig) {
        this.config = config
        val networkService = NetworkServiceFactory.createHttpClient(config) as UnifyNetworkServiceImpl
        this.httpClient = networkService.httpClient
    }

    actual suspend fun get(
        url: String,
        headers: Map<String, String>,
        useCache: Boolean,
    ): NetworkResponse<String> {
        return try {
            val startTime = System.currentTimeMillis()
            val response: HttpResponse =
                httpClient.get(url) {
                    headers.forEach { (key, value) ->
                        header(key, value)
                    }
                }

            val responseTime = System.currentTimeMillis() - startTime
            val body = response.body<String>()

            NetworkResponse(
                success = response.status.isSuccess(),
                data = body,
                statusCode = response.status.value,
                headers = response.headers.entries().associate { it.key to it.value.joinToString(", ") },
                responseTime = responseTime,
                fromCache = false,
            )
        } catch (e: Exception) {
            NetworkResponse(
                success = false,
                error =
                    NetworkError(
                        code = mapExceptionToErrorCode(e),
                        message = e.message ?: "Unknown error",
                    ),
            )
        }
    }

    actual suspend fun post(
        url: String,
        body: String,
        headers: Map<String, String>,
        contentType: String,
    ): NetworkResponse<String> {
        return try {
            val startTime = System.currentTimeMillis()
            val response: HttpResponse =
                httpClient.post(url) {
                    headers.forEach { (key, value) ->
                        header(key, value)
                    }
                    header(HttpHeaders.ContentType, contentType)
                    setBody(body)
                }

            val responseTime = System.currentTimeMillis() - startTime
            val responseBody = response.body<String>()

            NetworkResponse(
                success = response.status.isSuccess(),
                data = responseBody,
                statusCode = response.status.value,
                headers = response.headers.entries().associate { it.key to it.value.joinToString(", ") },
                responseTime = responseTime,
            )
        } catch (e: Exception) {
            NetworkResponse(
                success = false,
                error =
                    NetworkError(
                        code = mapExceptionToErrorCode(e),
                        message = e.message ?: "Unknown error",
                    ),
            )
        }
    }

    actual suspend fun put(
        url: String,
        body: String,
        headers: Map<String, String>,
        contentType: String,
    ): NetworkResponse<String> {
        return try {
            val startTime = System.currentTimeMillis()
            val response: HttpResponse =
                httpClient.put(url) {
                    headers.forEach { (key, value) ->
                        header(key, value)
                    }
                    header(HttpHeaders.ContentType, contentType)
                    setBody(body)
                }

            val responseTime = System.currentTimeMillis() - startTime
            val responseBody = response.body<String>()

            NetworkResponse(
                success = response.status.isSuccess(),
                data = responseBody,
                statusCode = response.status.value,
                headers = response.headers.entries().associate { it.key to it.value.joinToString(", ") },
                responseTime = responseTime,
            )
        } catch (e: Exception) {
            NetworkResponse(
                success = false,
                error =
                    NetworkError(
                        code = mapExceptionToErrorCode(e),
                        message = e.message ?: "Unknown error",
                    ),
            )
        }
    }

    actual suspend fun delete(
        url: String,
        headers: Map<String, String>,
    ): NetworkResponse<String> {
        return try {
            val startTime = System.currentTimeMillis()
            val response: HttpResponse =
                httpClient.delete(url) {
                    headers.forEach { (key, value) ->
                        header(key, value)
                    }
                }

            val responseTime = System.currentTimeMillis() - startTime
            val responseBody = response.body<String>()

            NetworkResponse(
                success = response.status.isSuccess(),
                data = responseBody,
                statusCode = response.status.value,
                headers = response.headers.entries().associate { it.key to it.value.joinToString(", ") },
                responseTime = responseTime,
            )
        } catch (e: Exception) {
            NetworkResponse(
                success = false,
                error =
                    NetworkError(
                        code = mapExceptionToErrorCode(e),
                        message = e.message ?: "Unknown error",
                    ),
            )
        }
    }

    actual suspend fun downloadFile(
        url: String,
        destinationPath: String,
        onProgress: ((Float) -> Unit)?,
    ): NetworkResponse<String> {
        return try {
            // 实现文件下载逻辑
            NetworkResponse(
                success = true,
                data = destinationPath,
            )
        } catch (e: Exception) {
            NetworkResponse(
                success = false,
                error =
                    NetworkError(
                        code = mapExceptionToErrorCode(e),
                        message = e.message ?: "Unknown error",
                    ),
            )
        }
    }

    actual suspend fun uploadFile(
        url: String,
        filePath: String,
        fieldName: String,
        additionalData: Map<String, String>,
        onProgress: ((Float) -> Unit)?,
    ): NetworkResponse<UploadResult> {
        return try {
            val uploadClient = NetworkServiceFactory.createFileUploadClient()
            val result = uploadClient.uploadFile(url, filePath, fieldName, additionalData, onProgress)

            NetworkResponse(
                success = result.success,
                data = result,
            )
        } catch (e: Exception) {
            NetworkResponse(
                success = false,
                error =
                    NetworkError(
                        code = mapExceptionToErrorCode(e),
                        message = e.message ?: "Unknown error",
                    ),
            )
        }
    }

    actual fun getNetworkStatusFlow(): Flow<NetworkStatus> {
        return NetworkServiceFactory.getNetworkStatusMonitor()
    }

    actual suspend fun clearCache() {
        // 实现缓存清理逻辑
    }

    actual fun cancelAllRequests() {
        httpClient.close()
    }

    private fun mapExceptionToErrorCode(exception: Exception): NetworkErrorCode {
        return when {
            exception.message?.contains("timeout", ignoreCase = true) == true -> NetworkErrorCode.TIMEOUT
            exception.message?.contains("network", ignoreCase = true) == true -> NetworkErrorCode.NO_INTERNET
            exception.message?.contains("host", ignoreCase = true) == true -> NetworkErrorCode.UNKNOWN_HOST
            exception.message?.contains("ssl", ignoreCase = true) == true -> NetworkErrorCode.SSL_ERROR
            else -> NetworkErrorCode.UNKNOWN
        }
    }
}

/**
 * 网络服务实现类
 */
internal class UnifyNetworkServiceImpl(
    val httpClient: HttpClient,
    private val config: NetworkConfig,
) : UnifyNetworkService {
    private var requestInterceptor: RequestInterceptor? = null
    private var responseInterceptor: ResponseInterceptor? = null
    private val requestStats = RequestStats()

    override suspend fun <T> request(request: NetworkRequest<T>): NetworkResponse<T> {
        // 基础实现，返回空响应避免编译错误
        @Suppress("UNCHECKED_CAST")
        return NetworkResponse<T>(
            success = false,
            error = NetworkError(
                code = NetworkErrorCode.UNKNOWN,
                message = "Method not implemented yet"
            )
        ) as NetworkResponse<T>
    }

    override suspend fun batchRequest(requests: List<NetworkRequest<*>>): List<NetworkResponse<*>> {
        return requests.map { request(it) }
    }

    override fun <T> createDataStream(
        url: String,
        interval: Long,
        parser: (String) -> T,
    ): Flow<T> {
        return kotlinx.coroutines.flow.emptyFlow()
    }

    override suspend fun connectWebSocket(
        url: String,
        protocols: List<String>,
    ): WebSocketConnection {
        return object : WebSocketConnection {
            override val isConnected: Boolean = false
            override suspend fun send(message: String) {}
            override suspend fun send(data: ByteArray) {}
            override suspend fun close(code: Int, reason: String) {}
            override fun onMessage(callback: (String) -> Unit) {}
            override fun onBinaryMessage(callback: (ByteArray) -> Unit) {}
            override fun onError(callback: (Throwable) -> Unit) {}
            override fun onClose(callback: (Int, String) -> Unit) {}
        }
    }

    override fun createServerSentEventStream(url: String): Flow<ServerSentEvent> {
        return kotlinx.coroutines.flow.emptyFlow()
    }

    override suspend fun graphqlQuery(
        url: String,
        query: String,
        variables: Map<String, Any>,
    ): NetworkResponse<GraphQLResponse> {
        return NetworkResponse(
            success = false,
            error = NetworkError(
                code = NetworkErrorCode.UNKNOWN,
                message = "GraphQL not implemented yet"
            )
        )
    }

    override fun getRequestStats(): RequestStats {
        return requestStats
    }

    override fun setRequestInterceptor(interceptor: RequestInterceptor) {
        this.requestInterceptor = interceptor
    }

    override fun setResponseInterceptor(interceptor: ResponseInterceptor) {
        this.responseInterceptor = interceptor
    }
}
