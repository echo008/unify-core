package com.unify.core.realtime

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Desktop平台WebSocket适配器实现
 * 基于Ktor WebSocket客户端
 */
actual class WebSocketPlatformAdapter {
    private var httpClient: HttpClient? = null
    private var webSocketSession: DefaultClientWebSocketSession? = null
    private var onMessageCallback: ((String) -> Unit)? = null
    private var onStateChangeCallback: ((WebSocketState) -> Unit)? = null
    private val isConnected = AtomicBoolean(false)
    private var messageReceiveJob: Job? = null
    
    actual suspend fun connect(
        url: String,
        headers: Map<String, String>,
        onMessage: (String) -> Unit,
        onStateChange: (WebSocketState) -> Unit
    ): WebSocketResult = withContext(Dispatchers.IO) {
        try {
            onMessageCallback = onMessage
            onStateChangeCallback = onStateChange
            
            // 创建Ktor HTTP客户端
            httpClient = HttpClient(CIO) {
                install(WebSockets) {
                    pingInterval = 30_000 // 30秒心跳
                    maxFrameSize = Long.MAX_VALUE
                }
                
                engine {
                    requestTimeout = 10_000 // 10秒超时
                }
            }
            
            onStateChange(WebSocketState.CONNECTING)
            
            // 建立WebSocket连接
            webSocketSession = httpClient!!.webSocketSession(url) {
                headers.forEach { (key, value) ->
                    headers.append(key, value)
                }
            }
            
            isConnected.set(true)
            onStateChange(WebSocketState.CONNECTED)
            
            // 启动消息接收协程
            startMessageReceiving()
            
            WebSocketResult.Success("Desktop WebSocket连接已建立")
        } catch (e: Exception) {
            isConnected.set(false)
            onStateChange(WebSocketState.ERROR)
            WebSocketResult.Error("Desktop WebSocket连接失败: ${e.message}")
        }
    }
    
    actual suspend fun disconnect(): WebSocketResult = withContext(Dispatchers.IO) {
        try {
            isConnected.set(false)
            messageReceiveJob?.cancel()
            
            webSocketSession?.close(CloseReason(CloseReason.Codes.NORMAL, "正常关闭"))
            webSocketSession = null
            
            httpClient?.close()
            httpClient = null
            
            onStateChangeCallback?.invoke(WebSocketState.DISCONNECTED)
            WebSocketResult.Success("Desktop WebSocket已断开")
        } catch (e: Exception) {
            WebSocketResult.Error("Desktop WebSocket断开失败: ${e.message}")
        }
    }
    
    actual suspend fun sendMessage(message: String): WebSocketResult = withContext(Dispatchers.IO) {
        try {
            if (!isConnected.get()) {
                return@withContext WebSocketResult.Error("WebSocket未连接")
            }
            
            webSocketSession?.send(Frame.Text(message))
            WebSocketResult.Success("消息发送成功")
        } catch (e: Exception) {
            WebSocketResult.Error("发送消息异常: ${e.message}")
        }
    }
    
    actual suspend fun sendBinaryMessage(data: ByteArray): WebSocketResult = withContext(Dispatchers.IO) {
        try {
            if (!isConnected.get()) {
                return@withContext WebSocketResult.Error("WebSocket未连接")
            }
            
            webSocketSession?.send(Frame.Binary(true, data))
            WebSocketResult.Success("二进制消息发送成功")
        } catch (e: Exception) {
            WebSocketResult.Error("发送二进制消息异常: ${e.message}")
        }
    }
    
    actual suspend fun cleanup() {
        withContext(Dispatchers.IO) {
            try {
                isConnected.set(false)
                messageReceiveJob?.cancel()
                webSocketSession?.close()
                webSocketSession = null
                httpClient?.close()
                httpClient = null
                onMessageCallback = null
                onStateChangeCallback = null
            } catch (e: Exception) {
                println("Desktop WebSocket清理异常: ${e.message}")
            }
        }
    }
    
    /**
     * 启动消息接收
     */
    private fun startMessageReceiving() {
        messageReceiveJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                webSocketSession?.let { session ->
                    for (frame in session.incoming) {
                        when (frame) {
                            is Frame.Text -> {
                                val text = frame.readText()
                                onMessageCallback?.invoke(text)
                            }
                            is Frame.Binary -> {
                                val data = frame.readBytes()
                                val base64String = java.util.Base64.getEncoder().encodeToString(data)
                                onMessageCallback?.invoke("BINARY:$base64String")
                            }
                            is Frame.Close -> {
                                isConnected.set(false)
                                onStateChangeCallback?.invoke(WebSocketState.DISCONNECTED)
                                break
                            }
                            is Frame.Ping -> {
                                // Ktor自动处理Ping/Pong
                            }
                            is Frame.Pong -> {
                                // Ktor自动处理Ping/Pong
                            }
                        }
                    }
                }
            } catch (e: ClosedReceiveChannelException) {
                // 连接正常关闭
                isConnected.set(false)
                onStateChangeCallback?.invoke(WebSocketState.DISCONNECTED)
            } catch (e: Exception) {
                // 连接异常
                isConnected.set(false)
                onStateChangeCallback?.invoke(WebSocketState.ERROR)
                println("Desktop WebSocket接收消息异常: ${e.message}")
            }
        }
    }
    
    /**
     * Desktop特有的网络检查
     */
    fun isNetworkAvailable(): Boolean {
        return try {
            val address = java.net.InetAddress.getByName("8.8.8.8")
            address.isReachable(3000) // 3秒超时
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 获取连接信息
     */
    fun getConnectionInfo(): Map<String, Any> {
        return mapOf(
            "isConnected" to isConnected.get(),
            "sessionActive" to (webSocketSession != null),
            "clientActive" to (httpClient != null)
        )
    }
    
    /**
     * 发送Ping帧
     */
    suspend fun sendPing(): WebSocketResult = withContext(Dispatchers.IO) {
        try {
            if (!isConnected.get()) {
                return@withContext WebSocketResult.Error("WebSocket未连接")
            }
            
            webSocketSession?.send(Frame.Ping("ping".toByteArray()))
            WebSocketResult.Success("Ping发送成功")
        } catch (e: Exception) {
            WebSocketResult.Error("发送Ping异常: ${e.message}")
        }
    }
}
