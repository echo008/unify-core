package com.unify.network

import kotlinx.coroutines.flow.Flow

/**
 * 网络管理器接口
 */
interface UnifyNetworkManager {
    suspend fun get(url: String, headers: Map<String, String> = emptyMap()): NetworkResponse
    suspend fun post(url: String, body: String, headers: Map<String, String> = emptyMap()): NetworkResponse
    suspend fun put(url: String, body: String, headers: Map<String, String> = emptyMap()): NetworkResponse
    suspend fun delete(url: String, headers: Map<String, String> = emptyMap()): NetworkResponse
    fun observeNetworkState(): Flow<NetworkState>
    suspend fun downloadFile(url: String, destinationPath: String): Boolean
    suspend fun uploadFile(url: String, filePath: String, headers: Map<String, String> = emptyMap()): NetworkResponse
}

/**
 * 网络响应数据类
 */
data class NetworkResponse(
    val statusCode: Int,
    val body: String,
    val headers: Map<String, String> = emptyMap(),
    val isSuccess: Boolean = statusCode in 200..299
)

/**
 * 网络状态枚举
 */
enum class NetworkState {
    CONNECTED,
    DISCONNECTED,
    CONNECTING,
    UNKNOWN
}

/**
 * 网络管理器工厂expect声明
 */
expect object UnifyNetworkManagerFactory {
    fun create(): UnifyNetworkManager
}
