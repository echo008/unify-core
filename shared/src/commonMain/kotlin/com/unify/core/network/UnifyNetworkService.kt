package com.unify.core.network

import kotlinx.coroutines.flow.Flow

/**
 * Unify网络服务接口
 * 提供统一的网络请求抽象
 */
interface UnifyNetworkService {
    
    /**
     * GET请求
     */
    suspend fun <T> get(
        url: String,
        headers: Map<String, String> = emptyMap(),
        deserializer: (String) -> T
    ): NetworkResult<T>
    
    /**
     * POST请求
     */
    suspend fun <T> post(
        url: String,
        body: String,
        headers: Map<String, String> = emptyMap(),
        deserializer: (String) -> T
    ): NetworkResult<T>
    
    /**
     * PUT请求
     */
    suspend fun <T> put(
        url: String,
        body: String,
        headers: Map<String, String> = emptyMap(),
        deserializer: (String) -> T
    ): NetworkResult<T>
    
    /**
     * DELETE请求
     */
    suspend fun <T> delete(
        url: String,
        headers: Map<String, String> = emptyMap(),
        deserializer: (String) -> T
    ): NetworkResult<T>
    
    /**
     * 文件上传
     */
    suspend fun uploadFile(
        url: String,
        filePath: String,
        headers: Map<String, String> = emptyMap()
    ): NetworkResult<String>
    
    /**
     * 文件下载
     */
    suspend fun downloadFile(
        url: String,
        savePath: String,
        headers: Map<String, String> = emptyMap()
    ): Flow<DownloadProgress>
}

/**
 * 网络请求结果封装
 */
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val exception: NetworkException) : NetworkResult<Nothing>()
    data class Loading(val progress: Float = 0f) : NetworkResult<Nothing>()
}

/**
 * 网络异常类
 */
sealed class NetworkException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class ConnectionError(message: String, cause: Throwable? = null) : NetworkException(message, cause)
    class TimeoutError(message: String, cause: Throwable? = null) : NetworkException(message, cause)
    class HttpError(val code: Int, message: String) : NetworkException("HTTP $code: $message")
    class ParseError(message: String, cause: Throwable? = null) : NetworkException(message, cause)
    class UnknownError(message: String, cause: Throwable? = null) : NetworkException(message, cause)
}

/**
 * 下载进度数据类
 */
data class DownloadProgress(
    val bytesDownloaded: Long,
    val totalBytes: Long,
    val isCompleted: Boolean = false,
    val error: NetworkException? = null
) {
    val progress: Float get() = if (totalBytes > 0) bytesDownloaded.toFloat() / totalBytes else 0f
}

/**
 * 网络服务工厂
 */
expect object NetworkServiceFactory {
    fun create(): UnifyNetworkService
}
