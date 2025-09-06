package com.unify.core.network

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * 统一网络管理器 - 跨平台网络通信核心接口
 * 提供HTTP请求、响应式数据流、错误处理、缓存管理等功能
 */
expect class UnifyNetworkManager {
    companion object {
        fun create(): UnifyNetworkManager
    }
    
    /**
     * 初始化网络管理器
     */
    fun initialize(config: NetworkConfig)
    
    /**
     * GET请求
     */
    suspend fun get(
        url: String,
        headers: Map<String, String> = emptyMap(),
        useCache: Boolean = true
    ): NetworkResponse<String>
    
    /**
     * POST请求
     */
    suspend fun post(
        url: String,
        body: String,
        headers: Map<String, String> = emptyMap(),
        contentType: String = "application/json"
    ): NetworkResponse<String>
    
    /**
     * PUT请求
     */
    suspend fun put(
        url: String,
        body: String,
        headers: Map<String, String> = emptyMap(),
        contentType: String = "application/json"
    ): NetworkResponse<String>
    
    /**
     * DELETE请求
     */
    suspend fun delete(
        url: String,
        headers: Map<String, String> = emptyMap()
    ): NetworkResponse<String>
    
    /**
     * 下载文件
     */
    suspend fun downloadFile(
        url: String,
        destinationPath: String,
        onProgress: ((Float) -> Unit)? = null
    ): NetworkResponse<String>
    
    /**
     * 上传文件
     */
    suspend fun uploadFile(
        url: String,
        filePath: String,
        fieldName: String = "file",
        additionalData: Map<String, String> = emptyMap(),
        onProgress: ((Float) -> Unit)? = null
    ): NetworkResponse<UploadResult>
    
    /**
     * 获取网络状态流
     */
    fun getNetworkStatusFlow(): Flow<NetworkStatus>
    
    /**
     * 清除缓存
     */
    suspend fun clearCache()
    
    /**
     * 取消所有请求
     */
    fun cancelAllRequests()
}

/**
 * 网络响应封装
 */
@Serializable
data class NetworkResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: NetworkError? = null,
    val statusCode: Int = 0,
    val headers: Map<String, String> = emptyMap(),
    val responseTime: Long = 0L,
    val fromCache: Boolean = false
)

/**
 * 网络错误
 */
@Serializable
data class NetworkError(
    val code: NetworkErrorCode,
    val message: String,
    val details: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 网络错误代码
 */
enum class NetworkErrorCode {
    TIMEOUT,
    NO_INTERNET,
    SERVER_ERROR,
    CLIENT_ERROR,
    PARSE_ERROR,
    UNKNOWN_HOST,
    SSL_ERROR,
    CANCELLED,
    UNKNOWN
}

/**
 * 请求优先级
 */
enum class RequestPriority {
    LOW,
    NORMAL,
    HIGH,
    CRITICAL
}

/**
 * 缓存策略
 */
enum class CacheStrategy {
    NO_CACHE,
    CACHE_FIRST,
    NETWORK_FIRST,
    CACHE_ONLY,
    NETWORK_ONLY
}
