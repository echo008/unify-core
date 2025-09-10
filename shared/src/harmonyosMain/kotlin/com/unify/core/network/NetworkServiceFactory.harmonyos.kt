package com.unify.core.network

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json

/**
 * HarmonyOS平台网络服务工厂实现
 */
actual class NetworkServiceFactory {
    actual companion object {
        actual fun create(): NetworkServiceFactory = NetworkServiceFactory()
    }

    private val networkStatusFlow = MutableStateFlow(NetworkStatus.CONNECTED)

    actual fun createHttpClient(config: NetworkConfig): HttpClient {
        return HttpClient(CIO) {
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

    actual fun createWebSocketClient(config: NetworkConfig): HttpClient {
        return HttpClient(CIO) {
            install(WebSockets) {
                pingInterval = 20_000
                maxFrameSize = Long.MAX_VALUE
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
        }
    }

    actual fun createFileUploadClient(config: NetworkConfig): HttpClient {
        return HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
            
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.INFO
            }
            
            install(HttpTimeout) {
                requestTimeoutMillis = config.timeoutMs * 3 // 文件上传需要更长时间
                connectTimeoutMillis = config.connectTimeoutMs
                socketTimeoutMillis = config.socketTimeoutMs * 3
            }
        }
    }

    actual fun getNetworkStatusMonitor(): Flow<NetworkStatus> {
        // HarmonyOS网络状态监控
        // 实际实现需要使用HarmonyOS的网络API
        return networkStatusFlow.asStateFlow()
    }

    actual fun isNetworkAvailable(): Boolean {
        // HarmonyOS网络可用性检查
        // 简化实现，实际需要使用HarmonyOS网络API
        return true
    }

    actual fun getNetworkType(): NetworkType {
        // HarmonyOS网络类型检测
        // 简化实现，实际需要使用HarmonyOS网络API
        return NetworkType.WIFI
    }

    actual fun startNetworkMonitoring() {
        // 启动HarmonyOS网络监控
        // 实际实现需要注册HarmonyOS网络状态变化监听器
        networkStatusFlow.value = NetworkStatus.CONNECTED
    }

    actual fun stopNetworkMonitoring() {
        // 停止HarmonyOS网络监控
        // 实际实现需要注销HarmonyOS网络状态变化监听器
    }
}
