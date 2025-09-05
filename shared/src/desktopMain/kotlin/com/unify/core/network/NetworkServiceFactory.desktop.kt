package com.unify.core.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Desktop平台网络服务工厂实现
 */
actual class NetworkServiceFactory {
    actual companion object {
        actual fun createNetworkManager(): UnifyNetworkManager {
            return DesktopUnifyNetworkManager()
        }
        
        actual fun createNetworkCache(): UnifyNetworkCache {
            return DesktopUnifyNetworkCache()
        }
        
        actual fun createNetworkMonitor(): UnifyNetworkMonitor {
            return DesktopUnifyNetworkMonitor()
        }
    }
}

/**
 * Desktop网络管理器实现
 */
class DesktopUnifyNetworkManager : UnifyNetworkManager {
    override suspend fun get(url: String, headers: Map<String, String>): NetworkResponse {
        return NetworkResponse(200, "", emptyMap(), true)
    }
    
    override suspend fun post(url: String, body: String, headers: Map<String, String>): NetworkResponse {
        return NetworkResponse(200, "", emptyMap(), true)
    }
    
    override suspend fun put(url: String, body: String, headers: Map<String, String>): NetworkResponse {
        return NetworkResponse(200, "", emptyMap(), true)
    }
    
    override suspend fun delete(url: String, headers: Map<String, String>): NetworkResponse {
        return NetworkResponse(200, "", emptyMap(), true)
    }
    
    override suspend fun patch(url: String, body: String, headers: Map<String, String>): NetworkResponse {
        return NetworkResponse(200, "", emptyMap(), true)
    }
    
    override suspend fun uploadFile(url: String, filePath: String, headers: Map<String, String>): NetworkResponse {
        return NetworkResponse(200, "", emptyMap(), true)
    }
    
    override suspend fun downloadFile(url: String, savePath: String, headers: Map<String, String>): NetworkResponse {
        return NetworkResponse(200, "", emptyMap(), true)
    }
    
    override fun connectWebSocket(url: String, listener: WebSocketListener): WebSocketConnection {
        return DesktopWebSocketConnection()
    }
    
    override fun observeNetworkStatus(): Flow<NetworkStatus> {
        return MutableSharedFlow<NetworkStatus>().asSharedFlow()
    }
    
    override fun isNetworkAvailable(): Boolean = true
    override fun getNetworkType(): NetworkType = NetworkType.ETHERNET
    override fun setBaseUrl(baseUrl: String) {}
    override fun getBaseUrl(): String = ""
    override fun setDefaultHeaders(headers: Map<String, String>) {}
    override fun getDefaultHeaders(): Map<String, String> = emptyMap()
    override fun setTimeout(timeoutMillis: Long) {}
    override fun getTimeout(): Long = 30000L
    override fun setCacheEnabled(enabled: Boolean) {}
    override fun isCacheEnabled(): Boolean = true
    override fun clearCache() {}
    override fun setRetryPolicy(policy: RetryPolicy) {}
    override fun getRetryPolicy(): RetryPolicy = RetryPolicy()
}

/**
 * Desktop网络缓存实现
 */
class DesktopUnifyNetworkCache : UnifyNetworkCache {
    override suspend fun get(key: String): String? = null
    override suspend fun put(key: String, value: String, ttl: Long) {}
    override suspend fun remove(key: String) {}
    override suspend fun clear() {}
    override suspend fun size(): Long = 0L
}

/**
 * Desktop网络监控实现
 */
class DesktopUnifyNetworkMonitor : UnifyNetworkMonitor {
    private val _networkEvents = MutableSharedFlow<NetworkEvent>()
    
    override fun observeNetworkEvents(): Flow<NetworkEvent> = _networkEvents.asSharedFlow()
    override fun onRequestStart(url: String, method: String) {}
    override fun onRequestComplete(url: String, statusCode: Int, duration: Long) {}
    override fun onRequestError(url: String, error: Throwable) {}
    override fun stopMonitoring() {}
    override fun getCurrentNetworkInfo(): NetworkConnectionInfo {
        return NetworkConnectionInfo(true, NetworkType.ETHERNET, 100)
    }
}

/**
 * Desktop WebSocket连接实现
 */
class DesktopWebSocketConnection : WebSocketConnection {
    override fun send(message: String) {}
    override fun close() {}
    override fun isConnected(): Boolean = false
}

/**
 * Desktop平台网络状态获取实现
 */
actual fun getCurrentNetworkStatus(): NetworkStatus {
    return NetworkStatus.CONNECTED
}
