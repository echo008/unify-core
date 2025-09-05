package com.unify.core.network

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Android平台网络服务工厂
 * 创建Android特定的网络管理器实例
 */
actual class NetworkServiceFactory {
    
    actual companion object {
        private var context: Context? = null
        
        /**
         * 初始化Android上下文
         * 必须在应用启动时调用
         */
        fun initialize(applicationContext: Context) {
            context = applicationContext.applicationContext
        }
        
        actual fun createNetworkManager(): UnifyNetworkManager {
            val appContext = context ?: throw IllegalStateException(
                "NetworkServiceFactory not initialized. Call NetworkServiceFactory.initialize(context) first."
            )
            return AndroidUnifyNetworkManager(appContext)
        }
        
        actual fun createNetworkCache(): UnifyNetworkCache {
            val appContext = context ?: throw IllegalStateException(
                "NetworkServiceFactory not initialized. Call NetworkServiceFactory.initialize(context) first."
            )
            return AndroidUnifyNetworkCache(appContext)
        }
        
        actual fun createNetworkMonitor(): UnifyNetworkMonitor {
            val appContext = context ?: throw IllegalStateException(
                "NetworkServiceFactory not initialized. Call NetworkServiceFactory.initialize(context) first."
            )
            return AndroidUnifyNetworkMonitor(appContext)
        }
    }
}

/**
 * Android网络缓存实现
 */
class AndroidUnifyNetworkCache(private val context: Context) : UnifyNetworkCache {
    
    private val cacheDir = context.cacheDir.resolve("network_cache")
    private val maxCacheSize = 50 * 1024 * 1024L // 50MB
    
    init {
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
    }
    
    override suspend fun get(key: String): String? {
        return try {
            val cacheFile = cacheDir.resolve(key.hashCode().toString())
            if (cacheFile.exists() && !isExpired(cacheFile)) {
                cacheFile.readText()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun put(key: String, value: String, ttl: Long) {
        try {
            val cacheFile = cacheDir.resolve(key.hashCode().toString())
            val cacheData = CacheEntry(value, System.currentTimeMillis() + ttl)
            cacheFile.writeText(kotlinx.serialization.json.Json.encodeToString(CacheEntry.serializer(), cacheData))
            
            // 清理过期缓存
            cleanupExpiredCache()
        } catch (e: Exception) {
            // 忽略缓存写入错误
        }
    }
    
    override suspend fun remove(key: String) {
        try {
            val cacheFile = cacheDir.resolve(key.hashCode().toString())
            if (cacheFile.exists()) {
                cacheFile.delete()
            }
        } catch (e: Exception) {
            // 忽略删除错误
        }
    }
    
    override suspend fun clear() {
        try {
            cacheDir.listFiles()?.forEach { file ->
                file.delete()
            }
        } catch (e: Exception) {
            // 忽略清理错误
        }
    }
    
    override suspend fun size(): Long {
        return try {
            cacheDir.listFiles()?.sumOf { it.length() } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
    
    private fun isExpired(cacheFile: java.io.File): Boolean {
        return try {
            val content = cacheFile.readText()
            val cacheEntry = kotlinx.serialization.json.Json.decodeFromString(CacheEntry.serializer(), content)
            System.currentTimeMillis() > cacheEntry.expiryTime
        } catch (e: Exception) {
            true // 如果无法解析，认为已过期
        }
    }
    
    private suspend fun cleanupExpiredCache() {
        try {
            val currentTime = System.currentTimeMillis()
            var totalSize = 0L
            
            val files = cacheDir.listFiles()?.sortedByDescending { it.lastModified() } ?: return
            
            for (file in files) {
                if (isExpired(file)) {
                    file.delete()
                } else {
                    totalSize += file.length()
                    if (totalSize > maxCacheSize) {
                        file.delete()
                    }
                }
            }
        } catch (e: Exception) {
            // 忽略清理错误
        }
    }
}

/**
 * Android网络监控实现
 */
class AndroidUnifyNetworkMonitor(private val context: Context) : UnifyNetworkMonitor {
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
    private val _networkEvents = MutableSharedFlow<NetworkEvent>()
    
    override fun observeNetworkEvents(): Flow<NetworkEvent> {
        return _networkEvents.asSharedFlow()
    }
    
    override fun onRequestStart(url: String, method: String) {
        _networkEvents.tryEmit(NetworkEvent.RequestStart(url, method))
    }
    
    override fun onRequestComplete(url: String, statusCode: Int, duration: Long) {
        _networkEvents.tryEmit(NetworkEvent.RequestComplete(url, statusCode, duration))
    }
    
    override fun onRequestError(url: String, error: Throwable) {
        _networkEvents.tryEmit(NetworkEvent.RequestError(url, error))
    }
    
    private suspend fun startMonitoring() {
        val networkRequest = android.net.NetworkRequest.Builder()
            .addCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        connectivityManager.registerNetworkCallback(networkRequest, object : android.net.ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: android.net.Network) {
                _networkEvents.tryEmit(NetworkEvent.Connected)
            }
            
            override fun onLost(network: android.net.Network) {
                _networkEvents.tryEmit(NetworkEvent.Disconnected)
            }
            
            override fun onCapabilitiesChanged(network: android.net.Network, networkCapabilities: android.net.NetworkCapabilities) {
                val networkType = when {
                    networkCapabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
                    networkCapabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.CELLULAR
                    networkCapabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.ETHERNET
                    else -> NetworkType.UNKNOWN
                }
                _networkEvents.tryEmit(NetworkEvent.TypeChanged(networkType))
            }
        })
    }
    
    override fun stopMonitoring() {
        // Android会在应用销毁时自动取消注册
    }
    
    override fun getCurrentNetworkInfo(): NetworkConnectionInfo {
        return try {
            val activeNetwork = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            
            val type = when {
                networkCapabilities?.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) == true -> NetworkType.WIFI
                networkCapabilities?.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) == true -> NetworkType.CELLULAR
                networkCapabilities?.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET) == true -> NetworkType.ETHERNET
                else -> NetworkType.UNKNOWN
            }
            
            val isConnected = networkCapabilities?.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            
            NetworkConnectionInfo(
                isConnected = isConnected,
                networkType = type,
                signalStrength = 75, // 简化实现
                bandwidth = 0L,
                ipAddress = getIpAddress()
            )
        } catch (e: Exception) {
            NetworkConnectionInfo(
                isConnected = false,
                networkType = NetworkType.UNKNOWN,
                signalStrength = 0,
                bandwidth = 0L,
                ipAddress = ""
            )
        }
    }
    
    private fun getIpAddress(): String {
        return try {
            val activeNetwork = connectivityManager.activeNetwork
            val linkProperties = connectivityManager.getLinkProperties(activeNetwork)
            linkProperties?.linkAddresses?.firstOrNull()?.address?.hostAddress ?: ""
        } catch (e: Exception) {
            ""
        }
    }
}

/**
 * Android平台网络状态获取实现
 */
actual fun getCurrentNetworkStatus(): NetworkStatus {
    return try {
        // 简化实现，实际项目中应该获取真实网络状态
        NetworkStatus.CONNECTED
    } catch (e: Exception) {
        NetworkStatus.UNKNOWN
    }
}

/**
 * 缓存条目数据类
 */
@kotlinx.serialization.Serializable
private data class CacheEntry(
    val data: String,
    val expiryTime: Long
)
