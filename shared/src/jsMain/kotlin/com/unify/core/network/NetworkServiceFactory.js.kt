package com.unify.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

/**
 * Web平台网络服务实现
 */
actual object NetworkServiceFactory {
    actual fun create(): UnifyNetworkService = WebNetworkService()
}

private class WebNetworkService : UnifyNetworkService {
    
    private val httpClient = HttpClient(Js) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        install(Logging) {
            level = LogLevel.INFO
        }
    }
    
    override suspend fun <T> get(
        url: String,
        headers: Map<String, String>,
        deserializer: (String) -> T
    ): NetworkResult<T> {
        return try {
            val response = httpClient.get(url) {
                headers {
                    headers.forEach { (key, value) ->
                        append(key, value)
                    }
                }
            }
            val body = response.bodyAsText()
            NetworkResult.Success(deserializer(body))
        } catch (e: Exception) {
            NetworkResult.Error(NetworkException.UnknownError(e.message ?: "Unknown error", e))
        }
    }
    
    override suspend fun <T> post(
        url: String,
        body: String,
        headers: Map<String, String>,
        deserializer: (String) -> T
    ): NetworkResult<T> {
        return try {
            val response = httpClient.post(url) {
                contentType(ContentType.Application.Json)
                setBody(body)
                headers {
                    headers.forEach { (key, value) ->
                        append(key, value)
                    }
                }
            }
            val responseBody = response.bodyAsText()
            NetworkResult.Success(deserializer(responseBody))
        } catch (e: Exception) {
            NetworkResult.Error(NetworkException.UnknownError(e.message ?: "Unknown error", e))
        }
    }
    
    override suspend fun <T> put(
        url: String,
        body: String,
        headers: Map<String, String>,
        deserializer: (String) -> T
    ): NetworkResult<T> {
        return try {
            val response = httpClient.put(url) {
                contentType(ContentType.Application.Json)
                setBody(body)
                headers {
                    headers.forEach { (key, value) ->
                        append(key, value)
                    }
                }
            }
            val responseBody = response.bodyAsText()
            NetworkResult.Success(deserializer(responseBody))
        } catch (e: Exception) {
            NetworkResult.Error(NetworkException.UnknownError(e.message ?: "Unknown error", e))
        }
    }
    
    override suspend fun <T> delete(
        url: String,
        headers: Map<String, String>,
        deserializer: (String) -> T
    ): NetworkResult<T> {
        return try {
            val response = httpClient.delete(url) {
                headers {
                    headers.forEach { (key, value) ->
                        append(key, value)
                    }
                }
            }
            val responseBody = response.bodyAsText()
            NetworkResult.Success(deserializer(responseBody))
        } catch (e: Exception) {
            NetworkResult.Error(NetworkException.UnknownError(e.message ?: "Unknown error", e))
        }
    }
    
    override suspend fun uploadFile(
        url: String,
        filePath: String,
        headers: Map<String, String>
    ): NetworkResult<String> {
        return NetworkResult.Error(NetworkException.UnknownError("File upload not implemented yet"))
    }
    
    override suspend fun downloadFile(
        url: String,
        savePath: String,
        headers: Map<String, String>
    ): Flow<DownloadProgress> = flow {
        emit(DownloadProgress(0, 0, false, NetworkException.UnknownError("File download not implemented yet")))
    }
}
