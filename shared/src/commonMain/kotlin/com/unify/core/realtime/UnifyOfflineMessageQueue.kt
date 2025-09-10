package com.unify.core.realtime

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Duration.Companion.minutes

/**
 * 离线消息队列管理器
 * 提供消息持久化、优先级管理、自动重发和批量处理能力
 */
class UnifyOfflineMessageQueue(
    private val webSocketManager: UnifyWebSocketManager,
    private val config: OfflineQueueConfig = OfflineQueueConfig()
) {
    private val _queueState = MutableStateFlow(QueueState.IDLE)
    val queueState: StateFlow<QueueState> = _queueState.asStateFlow()
    
    private val _queueSize = MutableStateFlow(0)
    val queueSize: StateFlow<Int> = _queueSize.asStateFlow()
    
    private val _processingStats = MutableStateFlow(ProcessingStats())
    val processingStats: StateFlow<ProcessingStats> = _processingStats.asStateFlow()
    
    // 消息队列存储
    private val messageQueue = mutableListOf<QueuedMessage>()
    private val failedMessages = mutableListOf<QueuedMessage>()
    private val sentMessages = mutableListOf<QueuedMessage>()
    
    // 处理作业
    private var processingJob: Job? = null
    private var retryJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    init {
        // 监听WebSocket连接状态
        coroutineScope.launch {
            webSocketManager.connectionState.collect { state ->
                when (state) {
                    WebSocketState.CONNECTED -> {
                        _queueState.value = QueueState.PROCESSING
                        startProcessing()
                    }
                    WebSocketState.DISCONNECTED -> {
                        _queueState.value = QueueState.OFFLINE
                        stopProcessing()
                    }
                    WebSocketState.ERROR -> {
                        _queueState.value = QueueState.ERROR
                        handleProcessingError()
                    }
                    else -> { /* 其他状态暂不处理 */ }
                }
            }
        }
        
        // 启动重试机制
        startRetryMechanism()
    }
    
    /**
     * 入队消息
     */
    suspend fun enqueue(
        content: String,
        priority: MessagePriority = MessagePriority.NORMAL,
        metadata: Map<String, String> = emptyMap(),
        ttl: Duration? = null
    ): QueueResult {
        return try {
            val message = QueuedMessage(
                id = generateMessageId(),
                content = content,
                priority = priority,
                metadata = metadata,
                timestamp = com.unify.core.platform.getCurrentTimeMillis(),
                ttl = ttl?.inWholeMilliseconds,
                status = MessageStatus.QUEUED,
                retryCount = 0,
                maxRetries = config.maxRetries
            )
            
            // 检查队列容量
            if (messageQueue.size >= config.maxQueueSize) {
                return handleQueueFull(message)
            }
            
            // 按优先级插入
            insertByPriority(message)
            updateQueueSize()
            
            // 如果在线，立即尝试发送
            if (_queueState.value == QueueState.PROCESSING) {
                processNextMessage()
            }
            
            QueueResult.Success("消息已入队: ${message.id}")
        } catch (e: Exception) {
            QueueResult.Error("入队失败: ${e.message}")
        }
    }
    
    /**
     * 批量入队消息
     */
    suspend fun enqueueBatch(
        messages: List<MessageData>,
        priority: MessagePriority = MessagePriority.NORMAL
    ): QueueResult {
        return try {
            val queuedMessages = messages.map { data ->
                QueuedMessage(
                    id = generateMessageId(),
                    content = data.content,
                    priority = priority,
                    metadata = data.metadata,
                    timestamp = com.unify.core.platform.getCurrentTimeMillis(),
                    ttl = data.ttl?.inWholeMilliseconds,
                    status = MessageStatus.QUEUED,
                    retryCount = 0,
                    maxRetries = config.maxRetries
                )
            }
            
            // 检查容量
            if (messageQueue.size + queuedMessages.size > config.maxQueueSize) {
                return QueueResult.Error("队列容量不足，无法批量入队")
            }
            
            // 批量插入
            queuedMessages.forEach { message ->
                insertByPriority(message)
            }
            updateQueueSize()
            
            // 触发处理
            if (_queueState.value == QueueState.PROCESSING) {
                processMessages()
            }
            
            QueueResult.Success("批量入队成功: ${queuedMessages.size}条消息")
        } catch (e: Exception) {
            QueueResult.Error("批量入队失败: ${e.message}")
        }
    }
    
    /**
     * 获取队列状态
     */
    fun getQueueStatus(): QueueStatus {
        return QueueStatus(
            totalMessages = messageQueue.size,
            highPriorityMessages = messageQueue.count { it.priority == MessagePriority.HIGH },
            normalPriorityMessages = messageQueue.count { it.priority == MessagePriority.NORMAL },
            lowPriorityMessages = messageQueue.count { it.priority == MessagePriority.LOW },
            failedMessages = failedMessages.size,
            sentMessages = sentMessages.size,
            queueState = _queueState.value,
            processingStats = _processingStats.value
        )
    }
    
    /**
     * 清理过期消息
     */
    suspend fun cleanupExpiredMessages(): Int {
        val currentTime = com.unify.core.platform.getCurrentTimeMillis()
        val expiredMessages = mutableListOf<QueuedMessage>()
        
        messageQueue.removeAll { message ->
            val isExpired = message.ttl?.let { ttl ->
                currentTime - message.timestamp > ttl
            } ?: false
            
            if (isExpired) {
                expiredMessages.add(message.copy(status = MessageStatus.EXPIRED))
            }
            isExpired
        }
        
        // 同时清理失败消息中的过期消息
        failedMessages.removeAll { message ->
            message.ttl?.let { ttl ->
                currentTime - message.timestamp > ttl
            } ?: false
        }
        
        updateQueueSize()
        updateProcessingStats { stats ->
            stats.copy(expiredMessages = stats.expiredMessages + expiredMessages.size)
        }
        
        return expiredMessages.size
    }
    
    /**
     * 重新入队失败的消息
     */
    suspend fun requeueFailedMessages(): QueueResult {
        return try {
            val requeueCount = failedMessages.size
            
            failedMessages.forEach { message ->
                val requeuedMessage = message.copy(
                    status = MessageStatus.QUEUED,
                    retryCount = 0,
                    timestamp = com.unify.core.platform.getCurrentTimeMillis()
                )
                insertByPriority(requeuedMessage)
            }
            
            failedMessages.clear()
            updateQueueSize()
            
            // 触发处理
            if (_queueState.value == QueueState.PROCESSING) {
                processMessages()
            }
            
            QueueResult.Success("重新入队 $requeueCount 条失败消息")
        } catch (e: Exception) {
            QueueResult.Error("重新入队失败: ${e.message}")
        }
    }
    
    /**
     * 获取消息详情
     */
    fun getMessageById(messageId: String): QueuedMessage? {
        return messageQueue.find { it.id == messageId }
            ?: failedMessages.find { it.id == messageId }
            ?: sentMessages.find { it.id == messageId }
    }
    
    /**
     * 删除消息
     */
    suspend fun removeMessage(messageId: String): QueueResult {
        val removed = messageQueue.removeAll { it.id == messageId } ||
                     failedMessages.removeAll { it.id == messageId }
        
        return if (removed) {
            updateQueueSize()
            QueueResult.Success("消息已删除: $messageId")
        } else {
            QueueResult.Error("消息不存在: $messageId")
        }
    }
    
    /**
     * 清空队列
     */
    suspend fun clearQueue(): QueueResult {
        return try {
            val totalCleared = messageQueue.size + failedMessages.size
            messageQueue.clear()
            failedMessages.clear()
            updateQueueSize()
            
            QueueResult.Success("队列已清空，共清除 $totalCleared 条消息")
        } catch (e: Exception) {
            QueueResult.Error("清空队列失败: ${e.message}")
        }
    }
    
    /**
     * 暂停队列处理
     */
    fun pauseProcessing() {
        if (_queueState.value == QueueState.PROCESSING) {
            _queueState.value = QueueState.PAUSED
            stopProcessing()
        }
    }
    
    /**
     * 恢复队列处理
     */
    fun resumeProcessing() {
        if (_queueState.value == QueueState.PAUSED) {
            _queueState.value = QueueState.PROCESSING
            startProcessing()
        }
    }
    
    /**
     * 获取队列统计信息
     */
    fun getStatistics(): QueueStatistics {
        val currentStats = _processingStats.value
        return QueueStatistics(
            totalEnqueued = currentStats.totalEnqueued,
            totalSent = currentStats.totalSent,
            totalFailed = currentStats.totalFailed,
            totalExpired = currentStats.expiredMessages,
            averageProcessingTime = currentStats.averageProcessingTime,
            successRate = if (currentStats.totalEnqueued > 0) {
                (currentStats.totalSent.toDouble() / currentStats.totalEnqueued * 100)
            } else 0.0,
            queueThroughput = currentStats.queueThroughput,
            currentQueueSize = messageQueue.size,
            failedQueueSize = failedMessages.size
        )
    }
    
    /**
     * 按优先级插入消息
     */
    private fun insertByPriority(message: QueuedMessage) {
        val insertIndex = messageQueue.indexOfFirst { it.priority.ordinal > message.priority.ordinal }
        if (insertIndex == -1) {
            messageQueue.add(message)
        } else {
            messageQueue.add(insertIndex, message)
        }
    }
    
    /**
     * 处理队列满的情况
     */
    private fun handleQueueFull(message: QueuedMessage): QueueResult {
        return when (config.queueFullStrategy) {
            QueueFullStrategy.DROP_NEW -> {
                QueueResult.Error("队列已满，新消息被丢弃")
            }
            QueueFullStrategy.DROP_OLD -> {
                // 删除最旧的低优先级消息
                val removedMessage = messageQueue.removeFirstOrNull()
                if (removedMessage != null) {
                    insertByPriority(message)
                    updateQueueSize()
                    QueueResult.Success("队列已满，删除旧消息后入队成功")
                } else {
                    QueueResult.Error("队列已满且无可删除的低优先级消息")
                }
            }
            QueueFullStrategy.EXPAND -> {
                // 临时扩展队列容量
                insertByPriority(message)
                updateQueueSize()
                QueueResult.Success("队列已满，临时扩展容量后入队成功")
            }
        }
    }
    
    /**
     * 启动消息处理
     */
    private fun startProcessing() {
        processingJob?.cancel()
        processingJob = coroutineScope.launch {
            while (isActive && _queueState.value == QueueState.PROCESSING) {
                try {
                    processMessages()
                    delay(config.processingInterval.inWholeMilliseconds)
                } catch (e: Exception) {
                    println("消息处理错误: ${e.message}")
                }
            }
        }
    }
    
    /**
     * 停止消息处理
     */
    private fun stopProcessing() {
        processingJob?.cancel()
        processingJob = null
    }
    
    /**
     * 处理消息
     */
    private suspend fun processMessages() {
        val batchSize = config.batchSize
        val messagesToProcess = messageQueue.take(batchSize).toList()
        
        if (messagesToProcess.isEmpty()) return
        
        messagesToProcess.forEach { message ->
            processMessage(message)
        }
    }
    
    /**
     * 处理单个消息
     */
    private suspend fun processMessage(message: QueuedMessage) {
        val startTime = com.unify.core.platform.getCurrentTimeMillis()
        
        try {
            // 更新消息状态
            val processingMessage = message.copy(status = MessageStatus.PROCESSING)
            
            // 发送消息
            val result = webSocketManager.sendMessage(message.content)
            
            when (result) {
                is WebSocketResult.Success -> {
                    // 发送成功
                    messageQueue.remove(message)
                    val sentMessage = processingMessage.copy(
                        status = MessageStatus.SENT,
                        sentTimestamp = com.unify.core.platform.getCurrentTimeMillis()
                    )
                    sentMessages.add(sentMessage)
                    
                    // 限制已发送消息列表大小
                    if (sentMessages.size > config.maxSentHistorySize) {
                        sentMessages.removeFirst()
                    }
                    
                    updateProcessingStats { stats ->
                        val processingTime = com.unify.core.platform.getCurrentTimeMillis() - startTime
                        stats.copy(
                            totalSent = stats.totalSent + 1,
                            averageProcessingTime = (stats.averageProcessingTime + processingTime) / 2,
                            queueThroughput = stats.queueThroughput + 1
                        )
                    }
                }
                is WebSocketResult.Error -> {
                    // 发送失败，处理重试
                    handleMessageFailure(message, result.message)
                }
            }
        } catch (e: Exception) {
            handleMessageFailure(message, e.message ?: "未知错误")
        } finally {
            updateQueueSize()
        }
    }
    
    /**
     * 处理消息发送失败
     */
    private fun handleMessageFailure(message: QueuedMessage, error: String) {
        val failedMessage = message.copy(
            status = MessageStatus.FAILED,
            retryCount = message.retryCount + 1,
            lastError = error,
            lastRetryTimestamp = com.unify.core.platform.getCurrentTimeMillis()
        )
        
        if (failedMessage.retryCount < failedMessage.maxRetries) {
            // 还可以重试，重新入队
            messageQueue.remove(message)
            insertByPriority(failedMessage.copy(status = MessageStatus.QUEUED))
        } else {
            // 超过最大重试次数，移到失败队列
            messageQueue.remove(message)
            failedMessages.add(failedMessage)
            
            updateProcessingStats { stats ->
                stats.copy(totalFailed = stats.totalFailed + 1)
            }
        }
    }
    
    /**
     * 启动重试机制
     */
    private fun startRetryMechanism() {
        retryJob = coroutineScope.launch {
            while (isActive) {
                try {
                    // 清理过期消息
                    if (config.autoCleanupExpired) {
                        cleanupExpiredMessages()
                    }
                    
                    // 处理需要重试的消息
                    processRetryMessages()
                    
                    delay(config.retryInterval.inWholeMilliseconds)
                } catch (e: Exception) {
                    println("重试机制错误: ${e.message}")
                }
            }
        }
    }
    
    /**
     * 处理重试消息
     */
    private suspend fun processRetryMessages() {
        val currentTime = com.unify.core.platform.getCurrentTimeMillis()
        val retryMessages = messageQueue.filter { message ->
            message.status == MessageStatus.QUEUED &&
            message.retryCount > 0 &&
            currentTime - message.lastRetryTimestamp >= config.retryDelay.inWholeMilliseconds
        }
        
        retryMessages.forEach { message ->
            if (_queueState.value == QueueState.PROCESSING) {
                processMessage(message)
            }
        }
    }
    
    /**
     * 处理处理错误
     */
    private fun handleProcessingError() {
        // 暂停处理，等待连接恢复
        stopProcessing()
    }
    
    /**
     * 处理下一条消息
     */
    private suspend fun processNextMessage() {
        if (messageQueue.isNotEmpty() && _queueState.value == QueueState.PROCESSING) {
            val nextMessage = messageQueue.first()
            processMessage(nextMessage)
        }
    }
    
    /**
     * 更新队列大小
     */
    private fun updateQueueSize() {
        _queueSize.value = messageQueue.size
    }
    
    /**
     * 更新处理统计
     */
    private fun updateProcessingStats(update: (ProcessingStats) -> ProcessingStats) {
        _processingStats.value = update(_processingStats.value)
    }
    
    /**
     * 清理资源
     */
    fun cleanup() {
        processingJob?.cancel()
        retryJob?.cancel()
        coroutineScope.cancel()
        messageQueue.clear()
        failedMessages.clear()
        sentMessages.clear()
    }
}

/**
 * 离线队列配置
 */
@Serializable
data class OfflineQueueConfig(
    val maxQueueSize: Int = 1000,
    val maxRetries: Int = 3,
    val processingInterval: Duration = 1.seconds,
    val retryInterval: Duration = 30.seconds,
    val retryDelay: Duration = 5.seconds,
    val batchSize: Int = 10,
    val queueFullStrategy: QueueFullStrategy = QueueFullStrategy.DROP_OLD,
    val autoCleanupExpired: Boolean = true,
    val maxSentHistorySize: Int = 100
)

/**
 * 队列消息
 */
@Serializable
data class QueuedMessage(
    val id: String,
    val content: String,
    val priority: MessagePriority,
    val metadata: Map<String, String>,
    val timestamp: Long,
    val ttl: Long? = null,
    val status: MessageStatus,
    val retryCount: Int,
    val maxRetries: Int,
    val lastError: String? = null,
    val lastRetryTimestamp: Long = 0L,
    val sentTimestamp: Long? = null
)

/**
 * 消息数据
 */
data class MessageData(
    val content: String,
    val metadata: Map<String, String> = emptyMap(),
    val ttl: Duration? = null
)

/**
 * 队列状态
 */
data class QueueStatus(
    val totalMessages: Int,
    val highPriorityMessages: Int,
    val normalPriorityMessages: Int,
    val lowPriorityMessages: Int,
    val failedMessages: Int,
    val sentMessages: Int,
    val queueState: QueueState,
    val processingStats: ProcessingStats
)

/**
 * 处理统计
 */
@Serializable
data class ProcessingStats(
    val totalEnqueued: Int = 0,
    val totalSent: Int = 0,
    val totalFailed: Int = 0,
    val expiredMessages: Int = 0,
    val averageProcessingTime: Long = 0L,
    val queueThroughput: Int = 0
)

/**
 * 队列统计信息
 */
data class QueueStatistics(
    val totalEnqueued: Int,
    val totalSent: Int,
    val totalFailed: Int,
    val totalExpired: Int,
    val averageProcessingTime: Long,
    val successRate: Double,
    val queueThroughput: Int,
    val currentQueueSize: Int,
    val failedQueueSize: Int
)

/**
 * 队列状态枚举
 */
enum class QueueState {
    IDLE,           // 空闲
    PROCESSING,     // 处理中
    PAUSED,         // 暂停
    OFFLINE,        // 离线
    ERROR           // 错误
}

/**
 * 消息优先级
 */
enum class MessagePriority {
    HIGH,           // 高优先级
    NORMAL,         // 普通优先级
    LOW             // 低优先级
}

/**
 * 消息状态
 */
enum class MessageStatus {
    QUEUED,         // 已入队
    PROCESSING,     // 处理中
    SENT,           // 已发送
    FAILED,         // 发送失败
    EXPIRED         // 已过期
}

/**
 * 队列满策略
 */
enum class QueueFullStrategy {
    DROP_NEW,       // 丢弃新消息
    DROP_OLD,       // 丢弃旧消息
    EXPAND          // 扩展队列
}

/**
 * 队列结果
 */
sealed class QueueResult {
    data class Success(val message: String) : QueueResult()
    data class Error(val message: String) : QueueResult()
}

/**
 * 生成消息ID
 */
private fun generateMessageId(): String {
    return "msg_${com.unify.core.platform.getCurrentTimeMillis()}_${(0..9999).random()}"
}
