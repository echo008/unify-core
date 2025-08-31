package com.unify.ui.components.ai

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.unify.ui.LocalUnifyTheme
import com.unify.ui.components.foundation.*

/**
 * AI模型类型
 */
enum class UnifyAIModelType {
    TEXT_GENERATION,        // 文本生成
    IMAGE_GENERATION,       // 图像生成
    SPEECH_TO_TEXT,         // 语音转文字
    TEXT_TO_SPEECH,         // 文字转语音
    TRANSLATION,            // 翻译
    CODE_GENERATION,        // 代码生成
    SENTIMENT_ANALYSIS,     // 情感分析
    CONTENT_MODERATION,     // 内容审核
    QUESTION_ANSWERING,     // 问答系统
    SUMMARIZATION          // 文本摘要
}

/**
 * AI对话消息
 */
data class UnifyAIMessage(
    val id: String = System.currentTimeMillis().toString(),
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * AI聊天配置
 */
data class UnifyAIChatConfig(
    val modelType: UnifyAIModelType = UnifyAIModelType.TEXT_GENERATION,
    val maxTokens: Int = 2048,
    val temperature: Float = 0.7f,
    val topP: Float = 0.9f,
    val frequencyPenalty: Float = 0.0f,
    val presencePenalty: Float = 0.0f,
    val systemPrompt: String = "你是一个有用的AI助手",
    val enableStreaming: Boolean = true,
    val enableContextMemory: Boolean = true,
    val maxContextLength: Int = 4096
)

/**
 * AI聊天组件
 */
@Composable
fun UnifyAIChat(
    config: UnifyAIChatConfig,
    modifier: Modifier = Modifier,
    onMessageSent: ((UnifyAIMessage) -> Unit)? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    var messages by remember { mutableStateOf<List<UnifyAIMessage>>(emptyList()) }
    var inputText by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(400.dp)
            .semantics {
                contentDescription?.let { this.contentDescription = it }
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 标题栏
            Card(
                colors = CardDefaults.cardColors(containerColor = theme.colors.primaryContainer),
                shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = theme.colors.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    UnifyText(
                        text = "AI助手",
                        variant = UnifyTextVariant.H6,
                        fontWeight = FontWeight.Bold,
                        color = theme.colors.onPrimaryContainer
                    )
                }
            }
            
            // 消息列表
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages.size) { index ->
                    AIMessageBubble(message = messages[index])
                }
                
                if (isGenerating) {
                    item { AITypingIndicator() }
                }
            }
            
            // 输入区域
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { UnifyText(text = "输入消息...") },
                    modifier = Modifier.weight(1f),
                    maxLines = 3
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                FloatingActionButton(
                    onClick = {
                        if (inputText.isNotBlank() && !isGenerating) {
                            val userMessage = UnifyAIMessage(content = inputText, isUser = true)
                            messages = messages + userMessage
                            onMessageSent?.invoke(userMessage)
                            inputText = ""
                            isGenerating = true
                        }
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "发送"
                    )
                }
            }
        }
    }
    
    LaunchedEffect(isGenerating) {
        if (isGenerating) {
            kotlinx.coroutines.delay(2000)
            val aiResponse = UnifyAIMessage(
                content = generateAIResponse(messages.last().content),
                isUser = false
            )
            messages = messages + aiResponse
            isGenerating = false
        }
    }
}

@Composable
private fun AIMessageBubble(message: UnifyAIMessage) {
    val theme = LocalUnifyTheme.current
    
    Row(
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser) theme.colors.primary else theme.colors.surfaceVariant
            )
        ) {
            UnifyText(
                text = message.content,
                modifier = Modifier.padding(12.dp),
                color = if (message.isUser) theme.colors.onPrimary else theme.colors.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AITypingIndicator() {
    val theme = LocalUnifyTheme.current
    
    Card(colors = CardDefaults.cardColors(containerColor = theme.colors.surfaceVariant)) {
        Row(modifier = Modifier.padding(16.dp)) {
            repeat(3) { index ->
                val infiniteTransition = rememberInfiniteTransition(label = "typing")
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600, delayMillis = index * 200),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "alpha$index"
                )
                
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            theme.colors.onSurfaceVariant.copy(alpha = alpha),
                            androidx.compose.foundation.shape.CircleShape
                        )
                )
                if (index < 2) Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
}

private fun generateAIResponse(userInput: String): String {
    val input = userInput.lowercase()

    // 简单的AI回复生成逻辑
    return when {
        input.contains("你好") || input.contains("hello") -> "您好！我是您的AI助手，有什么可以帮助您的吗？"
        input.contains("天气") -> "我可以帮您查询天气信息。请告诉我您想要查询哪个城市的天气？"
        input.contains("时间") || input.contains("几点") -> "现在是 ${java.util.Date().toString()}，有什么其他需要帮助的吗？"
        input.contains("帮助") || input.contains("help") -> "我可以帮您：\n• 查询天气信息\n• 获取当前时间\n• 解答常见问题\n• 提供建议和指导\n请告诉我您需要什么帮助！"
        input.contains("谢谢") || input.contains("thank") -> "不客气！很高兴能帮助到您。如果还有其他问题，随时可以问我。"
        input.contains("再见") || input.contains("bye") -> "再见！希望很快能再次为您服务。祝您一切顺利！"
        else -> "我理解您的问题。基于我的知识，我会尽力为您提供有用的信息和建议。如果您需要更具体的帮助，请提供更多详细信息。"
    }
}

data class UnifyRecommendationItem(
    val id: String,
    val title: String,
    val description: String,
    val score: Float,
    val category: String
)
