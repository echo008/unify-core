package com.unify.core.network

import com.unify.core.UnifyCore
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

/**
 * 网络服务工厂 - 跨平台网络服务创建和管理
 * 支持HTTP客户端、WebSocket、文件上传下载等功能
 */
expect object NetworkServiceFactory {
    /**
     * 创建HTTP客户端实例
     */
    fun createHttpClient(config: NetworkConfig = NetworkConfig()): UnifyNetworkService
    
    /**
     * 创建WebSocket客户端
     */
    fun createWebSocketClient(url: String): WebSocketClient
    
    /**
     * 创建文件上传客户端
     */
    fun createFileUploadClient(): FileUploadClient
    
    /**
     * 获取网络状态监听器
     */
    fun getNetworkStatusMonitor(): Flow<NetworkStatus>
}

/**
 * 网络配置
 */
@Serializable
data class NetworkConfig(
    val baseUrl: String = "",
    val timeout: Long = 30000L,
    val retryCount: Int = 3,
    val enableLogging: Boolean = true,
    val enableCache: Boolean = true,
    val cacheSize: Long = 10 * 1024 * 1024L, // 10MB
    val headers: Map<String, String> = emptyMap()
)

/**
 * 网络状态
 */
enum class NetworkStatus {
    CONNECTED,
    DISCONNECTED,
    CONNECTING,
    WIFI,
    MOBILE,
    ETHERNET,
    UNKNOWN
}

/**
 * WebSocket客户端接口
 */
interface WebSocketClient {
    suspend fun connect(): Boolean
    suspend fun disconnect()
    suspend fun send(message: String)
    fun onMessage(callback: (String) -> Unit)
    fun onError(callback: (Throwable) -> Unit)
    fun onClose(callback: (Int, String) -> Unit)
}

/**
 * 文件上传客户端接口
 */
interface FileUploadClient {
    suspend fun uploadFile(
        url: String,
        filePath: String,
        fieldName: String = "file",
        additionalData: Map<String, String> = emptyMap(),
        onProgress: ((Float) -> Unit)? = null
    ): UploadResult
    
    suspend fun uploadMultipleFiles(
        url: String,
        files: List<FileUploadInfo>,
        onProgress: ((Float) -> Unit)? = null
    ): List<UploadResult>
}

/**
 * 文件上传信息
 */
@Serializable
data class FileUploadInfo(
    val filePath: String,
    val fieldName: String,
    val fileName: String? = null,
    val mimeType: String? = null
)

/**
 * 上传结果
 */
@Serializable
data class UploadResult(
    val success: Boolean,
    val url: String? = null,
    val error: String? = null,
    val fileSize: Long = 0L,
    val uploadTime: Long = 0L
)
