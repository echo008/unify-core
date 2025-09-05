package com.unify.core.network

import kotlinx.coroutines.flow.Flow

/**
 * 网络服务工厂 - 跨平台接口
 */
expect class NetworkServiceFactory {
    companion object {
        fun createNetworkManager(): UnifyNetworkManager
        fun createNetworkCache(): UnifyNetworkCache
        fun createNetworkMonitor(): UnifyNetworkMonitor
    }
}

/**
 * 统一网络缓存接口
 */
interface UnifyNetworkCache {
    suspend fun get(key: String): String?
    suspend fun put(key: String, value: String, ttl: Long = 0L)
    suspend fun remove(key: String)
    suspend fun clear()
    suspend fun size(): Long
}

/**
 * 网络连接信息
 */
data class NetworkConnectionInfo(
    val isConnected: Boolean,
    val networkType: NetworkType,
    val signalStrength: Int = 0
)


/**
 * 网络类型枚举
 */
enum class NetworkType {
    WIFI,
    CELLULAR,
    ETHERNET,
    UNKNOWN
}

/**
 * 重试策略
 */
data class RetryPolicy(
    val maxRetries: Int = 3,
    val initialDelay: Long = 1000L,
    val maxDelay: Long = 30000L,
    val backoffMultiplier: Double = 2.0,
    val retryOnConnectionFailure: Boolean = true,
    val retryOnTimeout: Boolean = true
)

/**
 * 网络事件
 */
sealed class NetworkEvent {
    data class RequestStart(val url: String, val method: String) : NetworkEvent()
    data class RequestComplete(val url: String, val statusCode: Int, val duration: Long) : NetworkEvent()
    data class RequestError(val url: String, val error: Throwable) : NetworkEvent()
    object Connected : NetworkEvent()
    object Disconnected : NetworkEvent()
    data class TypeChanged(val networkType: NetworkType) : NetworkEvent()
}
