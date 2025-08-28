package com.unify.network

import com.unify.platform.NetworkService
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

data class NetworkResponse(
    val statusCode: Int,
    val body: String,
    val headers: Map<String, String>,
    val isSuccess: Boolean
)

actual class UnifyNetworkServiceImpl actual constructor(
    config: NetworkConfig,
    interceptors: List<NetworkInterceptor>,
    cache: NetworkCache?
) : UnifyNetworkService {
    
    private val httpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
        
        install(Logging) {
            logger = Logger.ANDROID
            level = LogLevel.INFO
        }
        
        engine {
            config {
                connectTimeout(config.connectTimeout.toLong(), java.util.concurrent.TimeUnit.MILLISECONDS)
                readTimeout(config.readTimeout.toLong(), java.util.concurrent.TimeUnit.MILLISECONDS)
                writeTimeout(config.writeTimeout.toLong(), java.util.concurrent.TimeUnit.MILLISECONDS)
            }
        }
    }
    
    override suspend fun <T> get(
        url: String,
        headers: Map<String, String>,
        responseType: kotlin.reflect.KClass<T>
    ): NetworkResult<T> {
        return try {
            val response = httpClient.get(url) {
                headers.forEach { (key, value) ->
                    header(key, value)
                }
            }
            
            val body = response.bodyAsText()
            val networkResponse = NetworkResponse(
                statusCode = response.status.value,
                body = body,
                headers = response.headers.toMap().mapValues { it.value.joinToString(", ") },
                isSuccess = response.status.isSuccess()
            )
            
            @Suppress("UNCHECKED_CAST")
            NetworkResult.Success(networkResponse as T)
        } catch (e: Exception) {
            NetworkResult.Error(NetworkError.Unknown(e.message ?: "Unknown error"))
        }
    }
    
    override suspend fun <T> post(
        url: String,
        body: Any?,
        headers: Map<String, String>,
        responseType: kotlin.reflect.KClass<T>
    ): NetworkResult<T> {
        return try {
            val response = httpClient.post(url) {
                headers.forEach { (key, value) ->
                    header(key, value)
                }
                if (body != null) {
                    setBody(body)
                }
                contentType(ContentType.Application.Json)
            }
            
            val responseBody = response.bodyAsText()
            val networkResponse = NetworkResponse(
                statusCode = response.status.value,
                body = responseBody,
                headers = response.headers.toMap().mapValues { it.value.joinToString(", ") },
                isSuccess = response.status.isSuccess()
            )
            
            @Suppress("UNCHECKED_CAST")
            NetworkResult.Success(networkResponse as T)
        } catch (e: Exception) {
            NetworkResult.Error(NetworkError.Unknown(e.message ?: "Unknown error"))
        }
    }
    
    override suspend fun <T> put(
        url: String,
        body: Any?,
        headers: Map<String, String>,
        responseType: kotlin.reflect.KClass<T>
    ): NetworkResult<T> {
        return try {
            val response = httpClient.put(url) {
                headers.forEach { (key, value) ->
                    header(key, value)
                }
                if (body != null) {
                    setBody(body)
                }
                contentType(ContentType.Application.Json)
            }
            
            val responseBody = response.bodyAsText()
            val networkResponse = NetworkResponse(
                statusCode = response.status.value,
                body = responseBody,
                headers = response.headers.toMap().mapValues { it.value.joinToString(", ") },
                isSuccess = response.status.isSuccess()
            )
            
            @Suppress("UNCHECKED_CAST")
            NetworkResult.Success(networkResponse as T)
        } catch (e: Exception) {
            NetworkResult.Error(NetworkError.Unknown(e.message ?: "Unknown error"))
        }
    }
    
    override suspend fun <T> delete(
        url: String,
        headers: Map<String, String>,
        responseType: kotlin.reflect.KClass<T>
    ): NetworkResult<T> {
        return try {
            val response = httpClient.delete(url) {
                headers.forEach { (key, value) ->
                    header(key, value)
                }
            }
            
            val responseBody = response.bodyAsText()
            val networkResponse = NetworkResponse(
                statusCode = response.status.value,
                body = responseBody,
                headers = response.headers.toMap().mapValues { it.value.joinToString(", ") },
                isSuccess = response.status.isSuccess()
            )
            
            @Suppress("UNCHECKED_CAST")
            NetworkResult.Success(networkResponse as T)
        } catch (e: Exception) {
            NetworkResult.Error(NetworkError.Unknown(e.message ?: "Unknown error"))
        }
    }
    
    override fun close() {
        httpClient.close()
    }
}
