package com.unify.core.realtime

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Unify跨平台WebSocket统一管理器
 * 提供统一的实时通信接口，屏蔽平台差异
 */
class UnifyWebSocketManager(
    private val platformAdapter: WebSocketPlatformAdapter,
    private val config: WebSocketConfig = WebSocketConfig(),
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {
    
    private val _connectionState = MutableStateFlow(WebSocketState.DISCONNECTED)
    val connectionState: StateFlow<WebSocketState> = _connectionState.asStateFlow()
    
    private val _messageFlow = MutableStateFlow<WebSocketMessage?>(null)
    val messageFlow: Flow<WebSocketMessage?> = _messageFlow.asStateFlow()
    
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    private var currentUrl: String? = null
    private var reconnectAttempts: Int = 0
    private var isReconnecting = false
    
    /**
     * 连接到WebSocket服务器
     */
    suspend fun connect(url: String, headers: Map<String, String> = emptyMap()): WebSocketResult {
        if (_connectionState.value == WebSocketState.CONNECTED) {
            return WebSocketResult.Success("已连接")
        }
        
        currentUrl = url
        _connectionState.value = WebSocketState.CONNECTING
        
        return try {
            val result = platformAdapter.connect(
                url = url,
                headers = headers,
                onMessage = { message ->
                    handleIncomingMessage(message)
                },
                onStateChange = { state ->
                    _connectionState.value = state
                    handleStateChange(state)
                }
            )
            
            when (result) {
                is WebSocketResult.Success -> {
                    _connectionState.value = WebSocketState.CONNECTED
                    reconnectAttempts = 0
                    WebSocketResult.Success("连接成功")
                }
                is WebSocketResult.Error -> {
                    _connectionState.value = WebSocketState.DISCONNECTED
                    WebSocketResult.Error("连接失败: ${result.message}")
                }
            }
        } catch (e: Exception) {
            _connectionState.value = WebSocketState.DISCONNECTED
            WebSocketResult.Error("连接异常: ${e.message}")
        }
    }
    
    /**
     * 断开WebSocket连接
     */
    suspend fun disconnect(): WebSocketResult {
        return try {
            val result = platformAdapter.disconnect()
            _connectionState.value = WebSocketState.DISCONNECTED
            currentUrl = null
            reconnectAttempts = 0
            isReconnecting = false
            result
        } catch (e: Exception) {
            WebSocketResult.Error("断开连接异常: ${e.message}")
        }
    }
    
    /**
     * 发送文本消息
     */
    suspend fun sendMessage(message: String): WebSocketResult {
        if (_connectionState.value != WebSocketState.CONNECTED) {
            return WebSocketResult.Error("WebSocket未连接")
        }
        
        return try {
            platformAdapter.sendMessage(message)
        } catch (e: Exception) {
            WebSocketResult.Error("发送消息失败: ${e.message}")
        }
    }
    
    /**
     * 发送JSON消息
     */
    suspend fun sendJsonMessage(message: Any): WebSocketResult {
        return try {
            val jsonString = when (message) {
                is WebSocketMessage -> json.encodeToString(WebSocketMessage.serializer(), message)
                else -> message.toString()
            }
            sendMessage(jsonString)
        } catch (e: Exception) {
            WebSocketResult.Error("序列化消息失败: ${e.message}")
        }
    }
    
    /**
     * 发送二进制消息
     */
    suspend fun sendBinaryMessage(data: ByteArray): WebSocketResult {
        if (_connectionState.value != WebSocketState.CONNECTED) {
            return WebSocketResult.Error("WebSocket未连接")
        }
        
        return try {
            platformAdapter.sendBinaryMessage(data)
        } catch (e: Exception) {
            WebSocketResult.Error("发送二进制消息失败: ${e.message}")
        }
    }
    
    /**
     * 发送心跳包
     */
    suspend fun sendHeartbeat(): WebSocketResult {
        val heartbeatMessage = WebSocketMessage(
            type = MessageType.HEARTBEAT,
            data = "ping",
            timestamp = com.unify.core.platform.getCurrentTimeMillis()
        )
        return sendJsonMessage(heartbeatMessage)
    }
    
    /**
     * 处理接收到的消息
     */
    private fun handleIncomingMessage(message: String) {
        try {
            val webSocketMessage = json.decodeFromString<WebSocketMessage>(message)
            _messageFlow.value = webSocketMessage
            
            // 处理特殊消息类型
            when (webSocketMessage.type) {
                MessageType.HEARTBEAT -> {
                    // 响应心跳
                    if (webSocketMessage.data == "ping") {
                        coroutineScope.launch {
                            val pongMessage = WebSocketMessage(
                                type = MessageType.HEARTBEAT,
                                data = "pong",
                                timestamp = com.unify.core.platform.getCurrentTimeMillis()
                            )
                            sendJsonMessage(pongMessage)
                        }
                    }
                }
                MessageType.ERROR -> {
                    // 处理服务器错误
                    println("WebSocket服务器错误: ${webSocketMessage.data}")
                }
                else -> {
                    // 普通消息，由上层处理
                }
            }
        } catch (e: Exception) {
            // 如果不是JSON格式，创建原始文本消息
            val rawMessage = WebSocketMessage(
                type = MessageType.TEXT,
                data = message,
                timestamp = com.unify.core.platform.getCurrentTimeMillis()
            )
            _messageFlow.value = rawMessage
        }
    }
    
    /**
     * 处理连接状态变化
     */
    private fun handleStateChange(state: WebSocketState) {
        when (state) {
            WebSocketState.DISCONNECTED -> {
                if (config.autoReconnect && !isReconnecting && currentUrl != null) {
                    attemptReconnect()
                }
            }
            WebSocketState.ERROR -> {
                if (config.autoReconnect && !isReconnecting && currentUrl != null) {
                    attemptReconnect()
                }
            }
            else -> {
                // 其他状态不需要特殊处理
            }
        }
    }
    
    /**
     * 尝试重连
     */
    private fun attemptReconnect() {
        if (reconnectAttempts >= config.maxReconnectAttempts) {
            println("达到最大重连次数，停止重连")
            return
        }
        
        isReconnecting = true
        reconnectAttempts++
        
        coroutineScope.launch {
            try {
                delay(config.reconnectInterval)
                currentUrl?.let { url ->
                    println("尝试重连 ($reconnectAttempts/${config.maxReconnectAttempts}): $url")
                    val result = connect(url)
                    if (result is WebSocketResult.Success) {
                        isReconnecting = false
                    } else {
                        isReconnecting = false
                        // 继续下一次重连尝试
                        if (reconnectAttempts < config.maxReconnectAttempts) {
                            attemptReconnect()
                        }
                    }
                }
            } catch (e: Exception) {
                isReconnecting = false
                println("重连异常: ${e.message}")
            }
        }
    }
    
    /**
     * 获取连接统计信息
     */
    fun getConnectionStats(): WebSocketStats {
        return WebSocketStats(
            currentState = _connectionState.value,
            reconnectAttempts = reconnectAttempts,
            isAutoReconnectEnabled = config.autoReconnect,
            currentUrl = currentUrl
        )
    }
    
    /**
     * 清理资源
     */
    suspend fun cleanup() {
        disconnect()
        platformAdapter.cleanup()
    }
}

/**
 * WebSocket平台适配器接口
 */
expect class WebSocketPlatformAdapter() {
    suspend fun connect(
        url: String,
        headers: Map<String, String>,
        onMessage: (String) -> Unit,
        onStateChange: (WebSocketState) -> Unit
    ): WebSocketResult
    
    suspend fun disconnect(): WebSocketResult
    suspend fun sendMessage(message: String): WebSocketResult
    suspend fun sendBinaryMessage(data: ByteArray): WebSocketResult
    suspend fun cleanup()
}

/**
 * WebSocket配置
 */
@Serializable
data class WebSocketConfig(
    val autoReconnect: Boolean = true,
    val maxReconnectAttempts: Int = 5,
    val reconnectInterval: Long = 3000L, // 3秒
    val heartbeatInterval: Long = 30000L, // 30秒
    val connectionTimeout: Long = 10000L, // 10秒
    val enableCompression: Boolean = true,
    val maxMessageSize: Int = 1024 * 1024 // 1MB
)

/**
 * WebSocket状态枚举
 */
enum class WebSocketState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    DISCONNECTING,
    ERROR
}

/**
 * WebSocket消息
 */
@Serializable
data class WebSocketMessage(
    val type: MessageType,
    val data: String,
    val timestamp: Long,
    val id: String = generateMessageId(),
    val metadata: Map<String, String> = emptyMap()
)


/**
 * WebSocket操作结果
 */
sealed class WebSocketResult {
    data class Success(val message: String) : WebSocketResult()
    data class Error(val message: String) : WebSocketResult()
}

/**
 * WebSocket连接统计
 */
data class WebSocketStats(
    val currentState: WebSocketState,
    val reconnectAttempts: Int,
    val isAutoReconnectEnabled: Boolean,
    val currentUrl: String?
)

/**
 * 生成消息ID
 */
private fun generateMessageId(): String {
    return "${com.unify.core.platform.getCurrentTimeMillis()}-${kotlin.random.Random.nextInt(1000, 9999)}"
}
