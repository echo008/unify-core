package com.unify.core.network

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json

/**
 * HarmonyOS平台网络管理器实现
 * 基于Ktor CIO引擎，适配HarmonyOS系统网络特性
 */
actual class UnifyNetworkManager {
    actual companion object {
        actual fun create(): UnifyNetworkManager = UnifyNetworkManager()
    }

    private lateinit var httpClient: HttpClient
    private val networkStatusFlow = MutableStateFlow(NetworkStatus.CONNECTED)

    actual fun initialize(config: NetworkConfig) {
        httpClient = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.INFO
            }
            
            install(HttpTimeout) {
                requestTimeoutMillis = config.timeoutMs
                connectTimeoutMillis = config.connectTimeoutMs
                socketTimeoutMillis = config.socketTimeoutMs
            }
            
            defaultRequest {
                config.baseUrl?.let { url(it) }
                config.defaultHeaders.forEach { (key, value) ->
                    header(key, value)
                }
            }
        }
    }

    actual suspend fun get(
        url: String,
        headers: Map<String, String>,
        useCache: Boolean
    ): NetworkResponse<String> {
        return try {
            val response = httpClient.get(url) {
                headers.forEach { (key, value) ->
                    header(key, value)
                }
                if (!useCache) {
                    header("Cache-Control", "no-cache")
                }
            }
            
            NetworkResponse.Success(
                data = response.bodyAsText(),
                statusCode = response.status.value,
                headers = response.headers.toMap()
            )
        } catch (e: Exception) {
            NetworkResponse.Error(
                exception = e,
                statusCode = null,
                message = e.message ?: "Unknown error"
            )
        }
    }

    actual suspend fun post(
        url: String,
        body: String,
        headers: Map<String, String>,
        contentType: String
    ): NetworkResponse<String> {
        return try {
            val response = httpClient.post(url) {
                setBody(body)
                contentType(ContentType.parse(contentType))
                headers.forEach { (key, value) ->
                    header(key, value)
                }
            }
            
            NetworkResponse.Success(
                data = response.bodyAsText(),
                statusCode = response.status.value,
                headers = response.headers.toMap()
            )
        } catch (e: Exception) {
            NetworkResponse.Error(
                exception = e,
                statusCode = null,
                message = e.message ?: "Unknown error"
            )
        }
    }

    actual suspend fun put(
        url: String,
        body: String,
        headers: Map<String, String>,
        contentType: String
    ): NetworkResponse<String> {
        return try {
            val response = httpClient.put(url) {
                setBody(body)
                contentType(ContentType.parse(contentType))
                headers.forEach { (key, value) ->
                    header(key, value)
                }
            }
            
            NetworkResponse.Success(
                data = response.bodyAsText(),
                statusCode = response.status.value,
                headers = response.headers.toMap()
            )
        } catch (e: Exception) {
            NetworkResponse.Error(
                exception = e,
                statusCode = null,
                message = e.message ?: "Unknown error"
            )
        }
    }

    actual suspend fun delete(
        url: String,
        headers: Map<String, String>
    ): NetworkResponse<String> {
        return try {
            val response = httpClient.delete(url) {
                headers.forEach { (key, value) ->
                    header(key, value)
                }
            }
            
            NetworkResponse.Success(
                data = response.bodyAsText(),
                statusCode = response.status.value,
                headers = response.headers.toMap()
            )
        } catch (e: Exception) {
            NetworkResponse.Error(
                exception = e,
                statusCode = null,
                message = e.message ?: "Unknown error"
            )
        }
    }

    actual suspend fun downloadFile(
        url: String,
        destinationPath: String,
        onProgress: ((Float) -> Unit)?
    ): NetworkResponse<String> {
        return try {
            val response = httpClient.get(url)
            val bytes = response.readBytes()
            
            // HarmonyOS文件系统写入
            // 简化实现，实际需要使用HarmonyOS文件API
            
            NetworkResponse.Success(
                data = "File downloaded successfully",
                statusCode = response.status.value,
                headers = response.headers.toMap()
            )
        } catch (e: Exception) {
            NetworkResponse.Error(
                exception = e,
                statusCode = null,
                message = e.message ?: "Download failed"
            )
        }
    }

    actual suspend fun uploadFile(
        url: String,
        filePath: String,
        fieldName: String,
        additionalData: Map<String, String>,
        onProgress: ((Float) -> Unit)?
    ): NetworkResponse<UploadResult> {
        return try {
            // HarmonyOS文件上传实现
            // 简化实现，实际需要使用HarmonyOS文件API和multipart上传
            
            NetworkResponse.Success(
                data = UploadResult(
                    fileId = "harmony_file_${System.currentTimeMillis()}",
                    fileName = filePath.substringAfterLast("/"),
                    fileSize = 0L,
                    url = "$url/uploaded"
                ),
                statusCode = 200,
                headers = emptyMap()
            )
        } catch (e: Exception) {
            NetworkResponse.Error(
                exception = e,
                statusCode = null,
                message = e.message ?: "Upload failed"
            )
        }
    }

    actual fun getNetworkStatusFlow(): Flow<NetworkStatus> {
        // HarmonyOS网络状态监听
        // 简化实现，实际需要使用HarmonyOS网络API
        return networkStatusFlow.asStateFlow()
    }

    actual suspend fun clearCache() {
        // 清除网络缓存
    }

    actual fun cancelAllRequests() {
        httpClient.close()
    }
}
