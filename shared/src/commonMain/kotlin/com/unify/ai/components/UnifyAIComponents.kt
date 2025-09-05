package com.unify.ai.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.unify.core.ai.UnifyAIEngine
import com.unify.core.ai.AIResult
import com.unify.core.platform.getCurrentTimeMillis

/**
 * 聊天消息数据类
 */
data class ChatMessage(
    val role: String,
    val content: String,
    val timestamp: Long = getCurrentTimeMillis()
)

/**
 * 聊天配置
 */
data class ChatConfig(
    val maxMessages: Int = 100,
    val enableHistory: Boolean = true,
    val placeholder: String = "输入消息..."
)

/**
 * AI配置
 */
data class AIConfig(
    val temperature: Float = 0.7f,
    val maxTokens: Int = 1000,
    val model: String = "default"
)

/**
 * Unify AI聊天组件
 */
@Composable
fun UnifyAIChatComponent(
    aiEngine: UnifyAIEngine,
    modifier: Modifier = Modifier,
    config: ChatConfig = ChatConfig(),
    onError: (String) -> Unit = {}
) {
    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    Column(modifier = modifier.fillMaxSize()) {
        // 消息列表
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
                ChatMessageItem(message = message)
            }
            
            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI正在思考...")
                    }
                }
            }
        }
        
        // 输入区域
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text(config.placeholder) },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (inputText.isNotBlank()) {
                            messages = messages + ChatMessage("user", inputText)
                            inputText = ""
                        }
                    }
                )
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            IconButton(
                onClick = {
                    if (inputText.isNotBlank() && !isLoading) {
                        sendMessage(
                            inputText,
                            messages,
                            aiEngine,
                            config,
                            coroutineScope,
                            onMessagesUpdate = { messages = it },
                            onLoadingUpdate = { isLoading = it },
                            onError = onError
                        )
                        inputText = ""
                    }
                },
                enabled = inputText.isNotBlank() && !isLoading
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "发送"
                )
            }
        }
    }
}

/**
 * 聊天消息项组件
 */
@Composable
private fun ChatMessageItem(message: ChatMessage) {
    val isUser = message.role == "user"
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Icon(
                imageVector = Icons.Default.SmartToy,
                contentDescription = "AI",
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(12.dp),
                color = if (isUser) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
        
        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "用户",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

/**
 * 发送消息逻辑
 */
private fun sendMessage(
    text: String,
    currentMessages: List<ChatMessage>,
    aiEngine: UnifyAIEngine,
    config: ChatConfig,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    onMessagesUpdate: (List<ChatMessage>) -> Unit,
    onLoadingUpdate: (Boolean) -> Unit,
    onError: (String) -> Unit
) {
    val userMessage = ChatMessage(role = "user", content = text)
    val updatedMessages = currentMessages + userMessage
    onMessagesUpdate(updatedMessages)
    
    coroutineScope.launch {
        onLoadingUpdate(true)
        
        when (val result = aiEngine.generateText(updatedMessages.joinToString("\n") { "${it.role}: ${it.content}" })) {
            is AIResult.Success -> {
                val aiMessage = ChatMessage(role = "assistant", content = result.content)
                onMessagesUpdate(updatedMessages + aiMessage)
            }
            is AIResult.Error -> {
                onError(result.message)
            }
        }
        
        onLoadingUpdate(false)
    }
}

/**
 * Unify AI文本生成组件
 */
@Composable
fun UnifyAITextGenerator(
    aiEngine: UnifyAIEngine,
    modifier: Modifier = Modifier,
    config: AIConfig = AIConfig(),
    onError: (String) -> Unit = {}
) {
    var prompt by remember { mutableStateOf("") }
    var generatedText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("输入提示词") },
            placeholder = { Text("请输入您想要生成的内容...") },
            minLines = 3,
            maxLines = 5
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                if (prompt.isNotBlank() && !isLoading) {
                    coroutineScope.launch {
                        isLoading = true
                        val result = aiEngine.generateText(prompt)
                        when (result) {
                            is AIResult.Success -> {
                                generatedText = result.content
                            }
                            is AIResult.Error -> {
                                onError(result.message)
                            }
                        }
                        isLoading = false
                    }
                }
            },
            enabled = prompt.isNotBlank() && !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(if (isLoading) "生成中..." else "生成文本")
        }
        
        if (generatedText.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "生成结果",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = generatedText)
                }
            }
        }
    }
}

/**
 * Unify AI图像分析组件
 */
@Composable
fun UnifyAIImageAnalyzer(
    aiEngine: UnifyAIEngine,
    imageData: ByteArray?,
    modifier: Modifier = Modifier,
    onError: (String) -> Unit = {}
) {
    var analysisPrompt by remember { mutableStateOf("") }
    var analysisResult by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    Column(modifier = modifier.fillMaxWidth()) {
        if (imageData != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "图像已加载",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "大小: ${imageData.size} 字节",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = analysisPrompt,
                onValueChange = { analysisPrompt = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("分析提示（可选）") },
                placeholder = { Text("请描述您想了解图像的什么内容...") }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    if (!isLoading) {
                        coroutineScope.launch {
                            isLoading = true
                            when (val result = aiEngine.generateText("分析这张图片: $analysisPrompt")) {
                                is AIResult.Success -> {
                                    analysisResult = result.content
                                }
                                is AIResult.Error -> {
                                    onError(result.message)
                                }
                            }
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isLoading) "分析中..." else "开始分析")
            }
            
            if (analysisResult.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "分析结果",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = analysisResult)
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = "上传图像",
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "请先上传图像",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "支持 JPG、PNG 格式",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Unify AI语音识别组件
 */
@Composable
fun UnifyAISpeechToText(
    aiEngine: UnifyAIEngine,
    modifier: Modifier = Modifier,
    language: String = "zh-CN",
    onResult: (String) -> Unit = {},
    onError: (String) -> Unit = {}
) {
    var isRecording by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var recognizedText by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 录音按钮
        IconButton(
            onClick = {
                if (!isRecording && !isProcessing) {
                    isRecording = true
                    // 开始录音逻辑
                } else if (isRecording) {
                    isRecording = false
                    isProcessing = true
                    // 停止录音并处理
                    coroutineScope.launch {
                        // 模拟音频数据
                        val audioData = ByteArray(0) // 实际应用中应该是录音数据
                        when (val result = aiEngine.speechToText(audioData, language)) {
                            is AIResult.Success -> {
                                recognizedText = result.content
                                onResult(result.content)
                            }
                            is AIResult.Error -> {
                                onError(result.message)
                            }
                        }
                        isProcessing = false
                    }
                }
            },
            modifier = Modifier.size(80.dp)
        ) {
            when {
                isProcessing -> CircularProgressIndicator()
                isRecording -> Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "停止录音",
                    modifier = Modifier.size(32.dp),
                    tint = Color.Red
                )
                else -> Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "开始录音",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = when {
                isProcessing -> "正在处理语音..."
                isRecording -> "正在录音中，点击停止"
                recognizedText.isNotEmpty() -> "识别结果: $recognizedText"
                else -> "点击麦克风开始录音"
            },
            style = MaterialTheme.typography.bodyMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        if (recognizedText.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "识别结果",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = recognizedText)
                }
            }
        }
    }
}
