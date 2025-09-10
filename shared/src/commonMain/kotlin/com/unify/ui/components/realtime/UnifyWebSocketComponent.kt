package com.unify.ui.components.realtime

import androidx.compose.foundation.layout.*
import kotlinx.datetime.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unify.core.realtime.*
import kotlinx.coroutines.launch

/**
 * WebSocket实时通信组件 - 基于Compose的统一UI
 */
@Composable
fun UnifyWebSocketComponent(
    webSocketManager: UnifyWebSocketManager,
    modifier: Modifier = Modifier,
    title: String = "实时通信"
) {
    var serverUrl by remember { mutableStateOf("wss://echo.websocket.org") }
    var messageInput by remember { mutableStateOf("") }
    var messageHistory by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var isConnecting by remember { mutableStateOf(false) }
    
    val connectionState by webSocketManager.connectionState.collectAsState(initial = com.unify.core.realtime.WebSocketState.DISCONNECTED)
    val incomingMessage by webSocketManager.messageFlow.collectAsState(initial = null)
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // 监听接收到的消息
    LaunchedEffect(incomingMessage) {
        incomingMessage?.let { message ->
            val chatMessage = ChatMessage(
                id = message.id,
                content = message.data,
                isUser = false,
                timestamp = message.timestamp,
                messageType = when (message.type) {
                    MessageType.HEARTBEAT -> ChatMessageType.SYSTEM
                    MessageType.ERROR -> ChatMessageType.ERROR
                    else -> ChatMessageType.TEXT
                }
            )
            messageHistory = messageHistory + chatMessage
            
            // 自动滚动到底部
            if (messageHistory.isNotEmpty()) {
                listState.animateScrollToItem(messageHistory.size - 1)
            }
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 标题和连接状态
        WebSocketHeader(
            title = title,
            connectionState = connectionState,
            webSocketManager = webSocketManager
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 连接控制区域
        ConnectionControlPanel(
            serverUrl = serverUrl,
            onServerUrlChange = { serverUrl = it },
            connectionState = connectionState,
            isConnecting = isConnecting,
            onConnect = {
                isConnecting = true
                coroutineScope.launch {
                    val result = webSocketManager.connect(serverUrl)
                    isConnecting = false
                    
                    val currentTime = Clock.System.now().toEpochMilliseconds()
                    val statusMessage = ChatMessage(
                        id = currentTime.toString(),
                        content = when (result) {
                            is WebSocketResult.Success -> "✅ ${result.message}"
                            is WebSocketResult.Error -> "❌ ${result.message}"
                        },
                        isUser = false,
                        timestamp = currentTime,
                        messageType = ChatMessageType.SYSTEM
                    )
                    messageHistory = messageHistory + statusMessage
                }
            },
            onDisconnect = {
                coroutineScope.launch {
                    val result = webSocketManager.disconnect()
                    val currentTime = Clock.System.now().toEpochMilliseconds()
                    val statusMessage = ChatMessage(
                        id = currentTime.toString(),
                        content = when (result) {
                            is WebSocketResult.Success -> "🔌 ${result.message}"
                            is WebSocketResult.Error -> "❌ ${result.message}"
                        },
                        isUser = false,
                        timestamp = currentTime,
                        messageType = ChatMessageType.SYSTEM
                    )
                    messageHistory = messageHistory + statusMessage
                }
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 消息历史
        Card(
            modifier = Modifier.weight(1f),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            if (messageHistory.isEmpty()) {
                // 空状态
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🔌",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "连接WebSocket服务器开始通信",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messageHistory) { message ->
                        MessageBubble(message = message)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 消息输入区域
        MessageInputArea(
            messageInput = messageInput,
            onMessageInputChange = { messageInput = it },
            connectionState = connectionState,
            onSendMessage = {
                if (messageInput.isNotBlank()) {
                    // 添加用户消息到历史
                    val currentTime = Clock.System.now().toEpochMilliseconds()
                    val userMessage = ChatMessage(
                        id = currentTime.toString(),
                        content = messageInput,
                        isUser = true,
                        timestamp = currentTime,
                        messageType = ChatMessageType.TEXT
                    )
                    messageHistory = messageHistory + userMessage
                    
                    // 发送消息
                    coroutineScope.launch {
                        val result = webSocketManager.sendMessage(messageInput)
                        if (result is WebSocketResult.Error) {
                            val currentTime = Clock.System.now().toEpochMilliseconds()
                            val errorMessage = ChatMessage(
                                id = currentTime.toString(),
                                content = "发送失败: ${result.message}",
                                isUser = false,
                                timestamp = currentTime,
                                messageType = ChatMessageType.ERROR
                            )
                            messageHistory = messageHistory + errorMessage
                        }
                    }
                    
                    messageInput = ""
                }
            }
        )
    }
}

/**
 * WebSocket头部组件
 */
@Composable
private fun WebSocketHeader(
    title: String,
    connectionState: WebSocketState,
    webSocketManager: UnifyWebSocketManager,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        
        // 连接状态指示器
        ConnectionStatusChip(connectionState = connectionState)
    }
    
    // 连接统计信息
    val stats = webSocketManager.getConnectionStats()
    if (stats.reconnectAttempts > 0) {
        Text(
            text = "重连次数: ${stats.reconnectAttempts}",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 连接状态芯片
 */
@Composable
private fun ConnectionStatusChip(
    connectionState: WebSocketState,
    modifier: Modifier = Modifier
) {
    val (statusText, statusColor) = when (connectionState) {
        WebSocketState.DISCONNECTED -> "未连接" to Color.Gray
        WebSocketState.CONNECTING -> "连接中" to Color.Blue
        WebSocketState.CONNECTED -> "已连接" to Color.Green
        WebSocketState.DISCONNECTING -> "断开中" to Color(0xFFFF9800)
        WebSocketState.ERROR -> "错误" to Color.Red
    }
    
    AssistChip(
        onClick = { },
        label = { Text(statusText, fontSize = 12.sp) },
        modifier = modifier,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = statusColor.copy(alpha = 0.1f),
            labelColor = statusColor
        )
    )
}

/**
 * 连接控制面板
 */
@Composable
private fun ConnectionControlPanel(
    serverUrl: String,
    onServerUrlChange: (String) -> Unit,
    connectionState: WebSocketState,
    isConnecting: Boolean,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "服务器连接",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // URL输入
            OutlinedTextField(
                value = serverUrl,
                onValueChange = onServerUrlChange,
                label = { Text("WebSocket服务器地址") },
                modifier = Modifier.fillMaxWidth(),
                enabled = connectionState == WebSocketState.DISCONNECTED,
                placeholder = { Text("wss://example.com/websocket") }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 连接按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onConnect,
                    enabled = connectionState == WebSocketState.DISCONNECTED && !isConnecting && serverUrl.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    if (isConnecting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("连接中...")
                    } else {
                        Text("连接")
                    }
                }
                
                Button(
                    onClick = onDisconnect,
                    enabled = connectionState == WebSocketState.CONNECTED,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("断开")
                }
            }
        }
    }
}

/**
 * 消息气泡
 */
@Composable
private fun MessageBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isUser) 16.dp else 4.dp,
                bottomEnd = if (message.isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    message.messageType == ChatMessageType.ERROR -> MaterialTheme.colorScheme.errorContainer
                    message.messageType == ChatMessageType.SYSTEM -> MaterialTheme.colorScheme.secondaryContainer
                    message.isUser -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = message.content,
                    color = when {
                        message.messageType == ChatMessageType.ERROR -> MaterialTheme.colorScheme.onErrorContainer
                        message.messageType == ChatMessageType.SYSTEM -> MaterialTheme.colorScheme.onSecondaryContainer
                        message.isUser -> MaterialTheme.colorScheme.onPrimary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontSize = 14.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = formatTimestamp(message.timestamp),
                    fontSize = 10.sp,
                    color = when {
                        message.messageType == ChatMessageType.ERROR -> MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                        message.messageType == ChatMessageType.SYSTEM -> MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        message.isUser -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    }
                )
            }
        }
    }
}

/**
 * 消息输入区域
 */
@Composable
private fun MessageInputArea(
    messageInput: String,
    onMessageInputChange: (String) -> Unit,
    connectionState: WebSocketState,
    onSendMessage: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = messageInput,
        onValueChange = onMessageInputChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("输入消息...") },
        enabled = connectionState == WebSocketState.CONNECTED,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
        keyboardActions = KeyboardActions(onSend = { onSendMessage() }),
        trailingIcon = {
            IconButton(
                onClick = onSendMessage,
                enabled = connectionState == WebSocketState.CONNECTED && messageInput.isNotBlank()
            ) {
                Text("发送", fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

/**
 * 聊天消息数据类
 */
data class ChatMessage(
    val id: String,
    val content: String,
    val isUser: Boolean,
    val timestamp: Long,
    val messageType: ChatMessageType = ChatMessageType.TEXT
)

/**
 * 聊天消息类型
 */
enum class ChatMessageType {
    TEXT,
    SYSTEM,
    ERROR
}

/**
 * 格式化时间戳
 */
private fun formatTimestamp(timestamp: Long): String {
    val now = Clock.System.now().toEpochMilliseconds()
    val diff = now - timestamp
    
    return when {
        diff < 60000 -> "刚刚"
        diff < 3600000 -> "${diff / 60000}分钟前"
        diff < 86400000 -> "${diff / 3600000}小时前"
        else -> "${diff / 86400000}天前"
    }
}
