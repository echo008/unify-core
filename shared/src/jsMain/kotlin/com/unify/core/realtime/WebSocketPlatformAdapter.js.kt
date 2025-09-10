package com.unify.core.realtime

import kotlinx.browser.window
import kotlinx.coroutines.await
import org.w3c.dom.WebSocket
import org.w3c.dom.events.Event
import org.w3c.dom.MessageEvent
import org.w3c.dom.CloseEvent
import kotlin.js.Promise

/**
 * JavaScript/Web平台WebSocket适配器实现
 * 基于浏览器原生WebSocket API
 */
actual class WebSocketPlatformAdapter {
    private var webSocket: WebSocket? = null
    private var onMessageCallback: ((String) -> Unit)? = null
    private var onStateChangeCallback: ((WebSocketState) -> Unit)? = null
    
    actual suspend fun connect(
        url: String,
        headers: Map<String, String>,
        onMessage: (String) -> Unit,
        onStateChange: (WebSocketState) -> Unit
    ): WebSocketResult {
        return try {
            onMessageCallback = onMessage
            onStateChangeCallback = onStateChange
            
            onStateChange(WebSocketState.CONNECTING)
            
            // 创建WebSocket连接
            webSocket = WebSocket(url).apply {
                // 设置事件监听器
                onopen = { _: Event ->
                    onStateChange(WebSocketState.CONNECTED)
                }
                
                onmessage = { event: Event ->
                    val messageEvent = event as MessageEvent
                    val data = messageEvent.data
                    
                    when {
                        data is String -> {
                            onMessage(data)
                        }
                        // 处理二进制数据
                        js("data instanceof ArrayBuffer") as Boolean -> {
                            val arrayBuffer = data
                            val uint8Array = js("new Uint8Array(arrayBuffer)")
                            val base64String = js("btoa(String.fromCharCode.apply(null, uint8Array))") as String
                            onMessage("BINARY:$base64String")
                        }
                        js("data instanceof Blob") as Boolean -> {
                            // 处理Blob数据
                            val blob = data
                            val reader = js("new FileReader()")
                            reader.asDynamic().onload = { readerEvent ->
                                val result = readerEvent.target.result as String
                                val base64 = result.substringAfter("base64,")
                                onMessage("BINARY:$base64")
                            }
                            reader.asDynamic().readAsDataURL(blob)
                        }
                        else -> {
                            onMessage(data.toString())
                        }
                    }
                }
                
                onclose = { event: Event ->
                    val closeEvent = event as CloseEvent
                    console.log("WebSocket关闭: ${closeEvent.code} - ${closeEvent.reason}")
                    onStateChange(WebSocketState.DISCONNECTED)
                }
                
                onerror = { _: Event ->
                    console.log("WebSocket错误")
                    onStateChange(WebSocketState.ERROR)
                }
            }
            
            // 等待连接建立
            waitForConnection()
            
            WebSocketResult.Success("Web WebSocket连接已建立")
        } catch (e: Exception) {
            WebSocketResult.Error("Web WebSocket连接失败: ${e.message}")
        }
    }
    
    actual suspend fun disconnect(): WebSocketResult {
        return try {
            webSocket?.close(1000, "正常关闭")
            webSocket = null
            onStateChangeCallback?.invoke(WebSocketState.DISCONNECTED)
            WebSocketResult.Success("Web WebSocket已断开")
        } catch (e: Exception) {
            WebSocketResult.Error("Web WebSocket断开失败: ${e.message}")
        }
    }
    
    actual suspend fun sendMessage(message: String): WebSocketResult {
        return try {
            val ws = webSocket
            if (ws == null || ws.readyState != WebSocket.OPEN) {
                return WebSocketResult.Error("WebSocket未连接")
            }
            
            ws.send(message)
            WebSocketResult.Success("消息发送成功")
        } catch (e: Exception) {
            WebSocketResult.Error("发送消息异常: ${e.message}")
        }
    }
    
    actual suspend fun sendBinaryMessage(data: ByteArray): WebSocketResult {
        return try {
            val ws = webSocket
            if (ws == null || ws.readyState != WebSocket.OPEN) {
                return WebSocketResult.Error("WebSocket未连接")
            }
            
            // 将ByteArray转换为Uint8Array
            val uint8Array = js("new Uint8Array(arguments[0].length)") as dynamic
            data.forEachIndexed { index, byte ->
                uint8Array[index] = byte
            }
            
            ws.send(uint8Array.buffer)
            WebSocketResult.Success("二进制消息发送成功")
        } catch (e: Exception) {
            WebSocketResult.Error("发送二进制消息异常: ${e.message}")
        }
    }
    
    actual suspend fun cleanup() {
        try {
            webSocket?.close()
            webSocket = null
            onMessageCallback = null
            onStateChangeCallback = null
        } catch (e: Exception) {
            console.log("Web WebSocket清理异常: ${e.message}")
        }
    }
    
    /**
     * 等待WebSocket连接建立
     */
    private suspend fun waitForConnection() {
        val promise = Promise<Unit> { resolve, reject ->
            val ws = webSocket
            if (ws == null) {
                reject(Exception("WebSocket未创建"))
                return@Promise
            }
            
            when (ws.readyState) {
                WebSocket.OPEN -> resolve(Unit)
                WebSocket.CONNECTING -> {
                    val originalOnOpen = ws.onopen
                    val originalOnError = ws.onerror
                    
                    ws.onopen = { event ->
                        originalOnOpen?.invoke(event)
                        resolve(Unit)
                    }
                    
                    ws.onerror = { event ->
                        originalOnError?.invoke(event)
                        reject(Exception("WebSocket连接失败"))
                    }
                }
                else -> reject(Exception("WebSocket状态异常: ${ws.readyState}"))
            }
        }
        
        promise.await()
    }
    
    /**
     * Web特有的网络状态检查
     */
    fun isOnline(): Boolean {
        return window.navigator.onLine
    }
    
    /**
     * 获取WebSocket状态
     */
    fun getReadyState(): String {
        return when (webSocket?.readyState) {
            WebSocket.CONNECTING -> "连接中"
            WebSocket.OPEN -> "已连接"
            WebSocket.CLOSING -> "关闭中"
            WebSocket.CLOSED -> "已关闭"
            else -> "未知"
        }
    }
    
    /**
     * 获取WebSocket URL
     */
    fun getUrl(): String? {
        return webSocket?.url
    }
    
    /**
     * 获取WebSocket协议
     */
    fun getProtocol(): String? {
        return webSocket?.protocol
    }
    
    /**
     * 获取WebSocket扩展
     */
    fun getExtensions(): String? {
        return webSocket?.extensions
    }
}
