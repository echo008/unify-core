package com.unify.ui.components.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.unify.core.ai.*
import kotlinx.coroutines.launch

/**
 * 智能聊天组件 - 基于真实AI引擎的聊天界面
 */
@Composable
fun UnifySmartChatComponent(
    aiEngine: UnifyAIEngine,
    modifier: Modifier = Modifier,
    placeholder: String = "输入消息...",
    maxMessages: Int = 100
) {
    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // 监听AI引擎状态
    val engineState by aiEngine.engineState.collectAsState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 状态指示器
        AIStatusIndicator(
            engineState = engineState,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 消息列表
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
                MessageBubble(message = message)
            }
            
            if (isLoading) {
                item {
                    TypingIndicator()
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 输入区域
        ChatInputField(
            text = inputText,
            onTextChange = { inputText = it },
            onSend = {
                if (inputText.isNotBlank() && !isLoading) {
                    val userMessage = ChatMessage(
                        id = com.unify.core.platform.getCurrentTimeMillis().toString(),
                        content = inputText,
                        isUser = true,
                        timestamp = com.unify.core.platform.getCurrentTimeMillis()
                    )
                    
                    messages = messages + userMessage
                    val currentInput = inputText
                    inputText = ""
                    isLoading = true
                    
                    // 滚动到底部
                    coroutineScope.launch {
                        listState.animateScrollToItem(messages.size)
                    }
                    
                    // 发送AI请求
                    coroutineScope.launch {
                        try {
                            val aiRequest = AIRequest(
                                type = AICapabilityType.TEXT_GENERATION,
                                input = currentInput
                            )
                            
                            val result = aiEngine.processRequest(aiRequest)
                            
                            when (result) {
                                is AIResult.Success -> {
                                    val aiMessage = ChatMessage(
                                        id = com.unify.core.platform.getCurrentTimeMillis().toString(),
                                        content = result.content,
                                        isUser = false,
                                        timestamp = com.unify.core.platform.getCurrentTimeMillis()
                                    )
                                    messages = messages + aiMessage
                                }
                                is AIResult.Error -> {
                                    val errorMessage = ChatMessage(
                                        id = com.unify.core.platform.getCurrentTimeMillis().toString(),
                                        content = "抱歉，处理您的请求时出现错误：${result.message}",
                                        isUser = false,
                                        timestamp = com.unify.core.platform.getCurrentTimeMillis()
                                    )
                                    messages = messages + errorMessage
                                }
                            }
                        } catch (e: Exception) {
                            val errorMessage = ChatMessage(
                                id = com.unify.core.platform.getCurrentTimeMillis().toString(),
                                content = "发生未知错误：${e.message}",
                                isUser = false,
                                timestamp = com.unify.core.platform.getCurrentTimeMillis()
                            )
                            messages = messages + errorMessage
                        } finally {
                            isLoading = false
                            // 滚动到底部
                            coroutineScope.launch {
                                listState.animateScrollToItem(messages.size)
                            }
                        }
                    }
                }
            },
            placeholder = placeholder,
            enabled = !isLoading && engineState == AIEngineState.READY
        )
    }
}

/**
 * AI状态指示器
 */
@Composable
private fun AIStatusIndicator(
    engineState: AIEngineState,
    modifier: Modifier = Modifier
) {
    val (statusText, statusColor) = when (engineState) {
        AIEngineState.IDLE -> "AI引擎空闲" to Color.Gray
        AIEngineState.INITIALIZING -> "AI引擎初始化中..." to Color.Blue
        AIEngineState.READY -> "AI引擎就绪" to Color.Green
        AIEngineState.PROCESSING -> "处理中" to Color(0xFFFFA500)
        AIEngineState.ERROR -> "AI引擎错误" to Color.Red
    }
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(statusColor, RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = statusText,
                fontSize = 12.sp,
                color = statusColor
            )
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
                containerColor = if (message.isUser) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = message.content,
                    color = if (message.isUser) 
                        MaterialTheme.colorScheme.onPrimary 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = formatChatTimestamp(message.timestamp),
                    fontSize = 10.sp,
                    color = if (message.isUser) 
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * 输入中指示器
 */
@Composable
private fun TypingIndicator(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 100.dp),
            shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    var alpha by remember { mutableStateOf(0.3f) }
                    
                    LaunchedEffect(Unit) {
                        while (true) {
                            kotlinx.coroutines.delay(300L * index)
                            alpha = 1f
                            kotlinx.coroutines.delay(300L)
                            alpha = 0.3f
                            kotlinx.coroutines.delay(600L)
                        }
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
                                RoundedCornerShape(3.dp)
                            )
                    )
                }
            }
        }
    }
}

/**
 * 聊天输入框
 */
@Composable
internal fun ChatInputField(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    placeholder: String,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = text,
        onValueChange = onTextChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        enabled = enabled,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
        keyboardActions = KeyboardActions(onSend = { onSend() }),
        trailingIcon = {
            IconButton(
                onClick = onSend,
                enabled = enabled && text.isNotBlank()
            ) {
                Text("发送", fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

/**
 * 格式化时间戳
 */
internal fun formatChatTimestamp(timestamp: Long): String {
    val now = com.unify.core.platform.getCurrentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60000 -> "刚刚"
        diff < 3600000 -> "${diff.toDouble() / 60000}分钟前"
        diff < 86400000 -> "${diff.toDouble() / 3600000}小时前"
        else -> "${diff.toDouble() / 86400000}天前"
    }
}
