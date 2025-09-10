package com.unify.core.realtime

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable

/**
 * WebSocket连接池管理器
 * 支持多连接管理、负载均衡和故障转移
 */
class UnifyWebSocketPool(
    private val config: WebSocketPoolConfig = WebSocketPoolConfig(),
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {
    private val connections = mutableMapOf<String, UnifyWebSocketManager>()
    private val connectionStates = mutableMapOf<String, WebSocketState>()
    private val mutex = Mutex()
    
    private val _poolState = MutableStateFlow(WebSocketPoolState.IDLE)
    val poolState: StateFlow<WebSocketPoolState> = _poolState.asStateFlow()
    
    private val _activeConnections = MutableStateFlow(0)
    val activeConnections: StateFlow<Int> = _activeConnections.asStateFlow()
    
    private var roundRobinIndex = 0
    
    /**
     * 添加WebSocket连接到池中
     */
    suspend fun addConnection(
        connectionId: String,
        url: String,
        headers: Map<String, String> = emptyMap(),
        priority: Int = 0
    ): WebSocketResult = mutex.withLock {
        if (connections.containsKey(connectionId)) {
            return WebSocketResult.Error("连接ID已存在: $connectionId")
        }
        
        if (connections.size >= config.maxConnections) {
            return WebSocketResult.Error("连接池已满，最大连接数: ${config.maxConnections}")
        }
        
        try {
            val platformAdapter = WebSocketPlatformAdapter()
            val manager = UnifyWebSocketManager(
                platformAdapter = platformAdapter,
                config = WebSocketConfig(
                    autoReconnect = config.autoReconnect,
                    maxReconnectAttempts = config.maxReconnectAttempts,
                    reconnectInterval = config.reconnectInterval
                )
            )
            
            // 监听连接状态变化
            coroutineScope.launch {
                manager.connectionState.collect { state ->
                    connectionStates[connectionId] = state
                    updatePoolState()
                }
            }
            
            connections[connectionId] = manager
            connectionStates[connectionId] = WebSocketState.DISCONNECTED
            
            // 如果启用自动连接，立即连接
            if (config.autoConnect) {
                val result = manager.connect(url, headers)
                if (result is WebSocketResult.Success) {
                    updateActiveConnectionCount()
                }
                return result
            }
            
            WebSocketResult.Success("连接已添加到池中: $connectionId")
        } catch (e: Exception) {
            WebSocketResult.Error("添加连接失败: ${e.message}")
        }
    }
    
    /**
     * 从池中移除连接
     */
    suspend fun removeConnection(connectionId: String): WebSocketResult = mutex.withLock {
        val manager = connections[connectionId]
            ?: return WebSocketResult.Error("连接不存在: $connectionId")
        
        try {
            manager.disconnect()
            manager.cleanup()
            connections.remove(connectionId)
            connectionStates.remove(connectionId)
            updateActiveConnectionCount()
            updatePoolState()
            
            WebSocketResult.Success("连接已从池中移除: $connectionId")
        } catch (e: Exception) {
            WebSocketResult.Error("移除连接失败: ${e.message}")
        }
    }
    
    /**
     * 连接指定的WebSocket
     */
    suspend fun connect(connectionId: String): WebSocketResult {
        val manager = connections[connectionId]
            ?: return WebSocketResult.Error("连接不存在: $connectionId")
        
        val result = manager.connect("", emptyMap()) // URL应该在添加时已设置
        if (result is WebSocketResult.Success) {
            updateActiveConnectionCount()
        }
        return result
    }
    
    /**
     * 连接所有WebSocket
     */
    suspend fun connectAll(): List<Pair<String, WebSocketResult>> {
        val results = mutableListOf<Pair<String, WebSocketResult>>()
        
        connections.forEach { (id, manager) ->
            try {
                val result = manager.connect("", emptyMap())
                results.add(id to result)
            } catch (e: Exception) {
                results.add(id to WebSocketResult.Error("连接失败: ${e.message}"))
            }
        }
        
        updateActiveConnectionCount()
        return results
    }
    
    /**
     * 断开指定连接
     */
    suspend fun disconnect(connectionId: String): WebSocketResult {
        val manager = connections[connectionId]
            ?: return WebSocketResult.Error("连接不存在: $connectionId")
        
        val result = manager.disconnect()
        updateActiveConnectionCount()
        return result
    }
    
    /**
     * 断开所有连接
     */
    suspend fun disconnectAll(): List<Pair<String, WebSocketResult>> {
        val results = mutableListOf<Pair<String, WebSocketResult>>()
        
        connections.forEach { (id, manager) ->
            try {
                val result = manager.disconnect()
                results.add(id to result)
            } catch (e: Exception) {
                results.add(id to WebSocketResult.Error("断开失败: ${e.message}"))
            }
        }
        
        updateActiveConnectionCount()
        return results
    }
    
    /**
     * 发送消息到指定连接
     */
    suspend fun sendMessage(connectionId: String, message: String): WebSocketResult {
        val manager = connections[connectionId]
            ?: return WebSocketResult.Error("连接不存在: $connectionId")
        
        return manager.sendMessage(message)
    }
    
    /**
     * 广播消息到所有活跃连接
     */
    suspend fun broadcast(message: String): List<Pair<String, WebSocketResult>> {
        val results = mutableListOf<Pair<String, WebSocketResult>>()
        
        connections.forEach { (id, manager) ->
            if (connectionStates[id] == WebSocketState.CONNECTED) {
                try {
                    val result = manager.sendMessage(message)
                    results.add(id to result)
                } catch (e: Exception) {
                    results.add(id to WebSocketResult.Error("广播失败: ${e.message}"))
                }
            }
        }
        
        return results
    }
    
    /**
     * 使用负载均衡策略发送消息
     */
    suspend fun sendMessageBalanced(message: String): WebSocketResult {
        val activeConnections = getActiveConnections()
        if (activeConnections.isEmpty()) {
            return WebSocketResult.Error("没有活跃的连接")
        }
        
        val selectedConnection = when (config.loadBalanceStrategy) {
            LoadBalanceStrategy.ROUND_ROBIN -> {
                val connection = activeConnections[roundRobinIndex % activeConnections.size]
                roundRobinIndex++
                connection
            }
            LoadBalanceStrategy.RANDOM -> {
                activeConnections.random()
            }
            LoadBalanceStrategy.FIRST_AVAILABLE -> {
                activeConnections.first()
            }
        }
        
        return connections[selectedConnection]?.sendMessage(message)
            ?: WebSocketResult.Error("选中的连接不可用")
    }
    
    /**
     * 获取活跃连接列表
     */
    fun getActiveConnections(): List<String> {
        return connectionStates.filter { it.value == WebSocketState.CONNECTED }.keys.toList()
    }
    
    /**
     * 获取所有连接状态
     */
    fun getAllConnectionStates(): Map<String, WebSocketState> {
        return connectionStates.toMap()
    }
    
    /**
     * 获取连接池统计信息
     */
    fun getPoolStats(): WebSocketPoolStats {
        val totalConnections = connections.size
        val activeConnections = getActiveConnections().size
        val connectingConnections = connectionStates.values.count { it == WebSocketState.CONNECTING }
        val errorConnections = connectionStates.values.count { it == WebSocketState.ERROR }
        
        return WebSocketPoolStats(
            totalConnections = totalConnections,
            activeConnections = activeConnections,
            connectingConnections = connectingConnections,
            errorConnections = errorConnections,
            poolState = _poolState.value,
            loadBalanceStrategy = config.loadBalanceStrategy
        )
    }
    
    /**
     * 健康检查
     */
    suspend fun healthCheck(): WebSocketHealthReport {
        val healthyConnections = mutableListOf<String>()
        val unhealthyConnections = mutableListOf<Pair<String, String>>()
        
        connections.forEach { (id, manager) ->
            try {
                val stats = manager.getConnectionStats()
                if (stats.currentState == WebSocketState.CONNECTED) {
                    // 发送心跳检查
                    val heartbeatResult = manager.sendHeartbeat()
                    if (heartbeatResult is WebSocketResult.Success) {
                        healthyConnections.add(id)
                    } else {
                        unhealthyConnections.add(id to "心跳失败")
                    }
                } else {
                    unhealthyConnections.add(id to "连接状态异常: ${stats.currentState}")
                }
            } catch (e: Exception) {
                unhealthyConnections.add(id to "健康检查异常: ${e.message}")
            }
        }
        
        return WebSocketHealthReport(
            timestamp = com.unify.core.platform.getCurrentTimeMillis(),
            healthyConnections = healthyConnections,
            unhealthyConnections = unhealthyConnections,
            overallHealth = if (unhealthyConnections.isEmpty()) HealthStatus.HEALTHY else HealthStatus.DEGRADED
        )
    }
    
    /**
     * 清理连接池
     */
    suspend fun cleanup() {
        mutex.withLock {
            connections.values.forEach { manager ->
                try {
                    manager.cleanup()
                } catch (e: Exception) {
                    println("清理连接异常: ${e.message}")
                }
            }
            connections.clear()
            connectionStates.clear()
            _activeConnections.value = 0
            _poolState.value = WebSocketPoolState.IDLE
        }
    }
    
    /**
     * 更新活跃连接数
     */
    private fun updateActiveConnectionCount() {
        _activeConnections.value = getActiveConnections().size
    }
    
    /**
     * 更新池状态
     */
    private fun updatePoolState() {
        val activeCount = getActiveConnections().size
        val totalCount = connections.size
        
        _poolState.value = when {
            totalCount == 0 -> WebSocketPoolState.IDLE
            activeCount == 0 -> WebSocketPoolState.DISCONNECTED
            activeCount == totalCount -> WebSocketPoolState.FULLY_CONNECTED
            activeCount > 0 -> WebSocketPoolState.PARTIALLY_CONNECTED
            else -> WebSocketPoolState.ERROR
        }
    }
}

/**
 * WebSocket连接池配置
 */
@Serializable
data class WebSocketPoolConfig(
    val maxConnections: Int = 10,
    val autoConnect: Boolean = true,
    val autoReconnect: Boolean = true,
    val maxReconnectAttempts: Int = 3,
    val reconnectInterval: Long = 5000L,
    val loadBalanceStrategy: LoadBalanceStrategy = LoadBalanceStrategy.ROUND_ROBIN,
    val healthCheckInterval: Long = 60000L // 1分钟
)

/**
 * 负载均衡策略
 */
@Serializable
enum class LoadBalanceStrategy {
    ROUND_ROBIN,    // 轮询
    RANDOM,         // 随机
    FIRST_AVAILABLE // 第一个可用
}

/**
 * 连接池状态
 */
enum class WebSocketPoolState {
    IDLE,                   // 空闲
    DISCONNECTED,          // 全部断开
    PARTIALLY_CONNECTED,   // 部分连接
    FULLY_CONNECTED,       // 全部连接
    ERROR                  // 错误状态
}

/**
 * 连接池统计信息
 */
data class WebSocketPoolStats(
    val totalConnections: Int,
    val activeConnections: Int,
    val connectingConnections: Int,
    val errorConnections: Int,
    val poolState: WebSocketPoolState,
    val loadBalanceStrategy: LoadBalanceStrategy
)

/**
 * 健康检查报告
 */
data class WebSocketHealthReport(
    val timestamp: Long,
    val healthyConnections: List<String>,
    val unhealthyConnections: List<Pair<String, String>>,
    val overallHealth: HealthStatus
)

/**
 * 健康状态
 */
enum class HealthStatus {
    HEALTHY,    // 健康
    DEGRADED,   // 降级
    CRITICAL    // 严重
}
