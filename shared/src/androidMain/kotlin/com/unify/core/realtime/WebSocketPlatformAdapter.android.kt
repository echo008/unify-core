package com.unify.core.realtime

import okhttp3.*
import okio.ByteString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Android平台WebSocket适配器实现
 * 基于OkHttp WebSocket
 */
actual class WebSocketPlatformAdapter {
    private var webSocket: WebSocket? = null
    private var okHttpClient: OkHttpClient? = null
    private var onMessageCallback: ((String) -> Unit)? = null
    private var onStateChangeCallback: ((WebSocketState) -> Unit)? = null
    
    actual suspend fun connect(
        url: String,
        headers: Map<String, String>,
        onMessage: (String) -> Unit,
        onStateChange: (WebSocketState) -> Unit
    ): WebSocketResult = withContext(Dispatchers.IO) {
        try {
            onMessageCallback = onMessage
            onStateChangeCallback = onStateChange
            
            // 创建OkHttp客户端
            okHttpClient = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build()
            
            // 构建请求
            val requestBuilder = Request.Builder().url(url)
            headers.forEach { (key, value) ->
                requestBuilder.addHeader(key, value)
            }
            val request = requestBuilder.build()
            
            // 创建WebSocket监听器
            val listener = object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    super.onOpen(webSocket, response)
                    onStateChange(WebSocketState.CONNECTED)
                }
                
                override fun onMessage(webSocket: WebSocket, text: String) {
                    super.onMessage(webSocket, text)
                    onMessage(text)
                }
                
                override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                    super.onMessage(webSocket, bytes)
                    // 将二进制消息转换为Base64字符串
                    val base64String = bytes.base64()
                    onMessage("BINARY:$base64String")
                }
                
                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    super.onClosing(webSocket, code, reason)
                    onStateChange(WebSocketState.DISCONNECTING)
                }
                
                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    super.onClosed(webSocket, code, reason)
                    onStateChange(WebSocketState.DISCONNECTED)
                }
                
                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    super.onFailure(webSocket, t, response)
                    onStateChange(WebSocketState.ERROR)
                }
            }
            
            // 建立WebSocket连接
            webSocket = okHttpClient!!.newWebSocket(request, listener)
            
            WebSocketResult.Success("Android WebSocket连接已建立")
        } catch (e: Exception) {
            WebSocketResult.Error("Android WebSocket连接失败: ${e.message}")
        }
    }
    
    actual suspend fun disconnect(): WebSocketResult = withContext(Dispatchers.IO) {
        try {
            webSocket?.close(1000, "正常关闭")
            webSocket = null
            WebSocketResult.Success("Android WebSocket已断开")
        } catch (e: Exception) {
            WebSocketResult.Error("Android WebSocket断开失败: ${e.message}")
        }
    }
    
    actual suspend fun sendMessage(message: String): WebSocketResult = withContext(Dispatchers.IO) {
        try {
            val success = webSocket?.send(message) ?: false
            if (success) {
                WebSocketResult.Success("消息发送成功")
            } else {
                WebSocketResult.Error("消息发送失败")
            }
        } catch (e: Exception) {
            WebSocketResult.Error("发送消息异常: ${e.message}")
        }
    }
    
    actual suspend fun sendBinaryMessage(data: ByteArray): WebSocketResult = withContext(Dispatchers.IO) {
        try {
            val byteString = ByteString.of(*data)
            val success = webSocket?.send(byteString) ?: false
            if (success) {
                WebSocketResult.Success("二进制消息发送成功")
            } else {
                WebSocketResult.Error("二进制消息发送失败")
            }
        } catch (e: Exception) {
            WebSocketResult.Error("发送二进制消息异常: ${e.message}")
        }
    }
    
    actual suspend fun cleanup() {
        withContext(Dispatchers.IO) {
            try {
                webSocket?.close(1000, "清理资源")
                webSocket = null
                okHttpClient?.dispatcher?.executorService?.shutdown()
                okHttpClient = null
                onMessageCallback = null
                onStateChangeCallback = null
            } catch (e: Exception) {
                println("Android WebSocket清理异常: ${e.message}")
            }
        }
    }
    
    /**
     * Android特有的网络状态检查
     */
    fun isNetworkAvailable(): Boolean {
        // 实际应用中应该检查网络连接状态
        return true
    }
    
    /**
     * 获取连接信息
     */
    fun getConnectionInfo(): String {
        return webSocket?.request()?.url?.toString() ?: "未连接"
    }
}
