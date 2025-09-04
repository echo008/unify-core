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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.unify.ai.*
import com.unify.ui.components.foundation.*
import kotlinx.coroutines.launch

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
                        UnifyText("AI正在思考...")
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
                modifier = Modifier.weight(1f),
                placeholder = { UnifyText("输入消息...") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
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
                    }
                )
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            UnifyIconButton(
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
                UnifyIcon(
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
            UnifyIcon(
                imageVector = Icons.Default.SmartToy,
                contentDescription = "AI",
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        UnifyCard(
            modifier = Modifier.widthIn(max = 280.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            UnifyText(
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
            UnifyIcon(
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
        
        when (val result = aiEngine.chatCompletion(updatedMessages, config)) {
            is AIResult.Success -> {
                val aiMessage = ChatMessage(role = "assistant", content = result.data)
                onMessagesUpdate(updatedMessages + aiMessage)
            }
            is AIResult.Error -> {
                onError(result.exception.message)
            }
            is AIResult.Loading -> {
                // 处理加载状态
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
            label = { UnifyText("输入提示词") },
            placeholder = { UnifyText("请输入您想要生成的内容...") },
            minLines = 3,
            maxLines = 5
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        UnifyButton(
            onClick = {
                if (prompt.isNotBlank() && !isLoading) {
                    coroutineScope.launch {
                        isLoading = true
                        when (val result = aiEngine.generateText(prompt, config)) {
                            is AIResult.Success -> {
                                generatedText = result.data
                            }
                            is AIResult.Error -> {
                                onError(result.exception.message)
                            }
                            is AIResult.Loading -> {
                                // 处理加载状态
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
            UnifyText(if (isLoading) "生成中..." else "生成文本")
        }
        
        if (generatedText.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            
            UnifyCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    UnifyText(
                        text = "生成结果",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    UnifyText(text = generatedText)
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
            UnifyCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    UnifyText(
                        text = "图像已加载",
                        style = MaterialTheme.typography.titleMedium
                    )
                    UnifyText(
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
                label = { UnifyText("分析提示（可选）") },
                placeholder = { UnifyText("请描述您想了解图像的什么内容...") }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            UnifyButton(
                onClick = {
                    if (!isLoading) {
                        coroutineScope.launch {
                            isLoading = true
                            when (val result = aiEngine.analyzeImage(imageData, analysisPrompt)) {
                                is AIResult.Success -> {
                                    analysisResult = result.data
                                }
                                is AIResult.Error -> {
                                    onError(result.exception.message)
                                }
                                is AIResult.Loading -> {
                                    // 处理加载状态
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
                UnifyText(if (isLoading) "分析中..." else "分析图像")
            }
            
            if (analysisResult.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                
                UnifyCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        UnifyText(
                            text = "分析结果",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        UnifyText(text = analysisResult)
                    }
                }
            }
        } else {
            UnifyCard(
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
                    UnifyIcon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = "上传图像",
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    UnifyText(
                        text = "请先上传图像",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    UnifyText(
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
        UnifyIconButton(
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
                                recognizedText = result.data
                                onResult(result.data)
                            }
                            is AIResult.Error -> {
                                onError(result.exception.message)
                            }
                            is AIResult.Loading -> {
                                // 处理加载状态
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
                isRecording -> UnifyIcon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "停止录音",
                    modifier = Modifier.size(32.dp),
                    tint = Color.Red
                )
                else -> UnifyIcon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "开始录音",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        UnifyText(
            text = when {
                isProcessing -> "正在识别..."
                isRecording -> "录音中，点击停止"
                else -> "点击开始录音"
            },
            style = MaterialTheme.typography.bodyMedium
        )
        
        if (recognizedText.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            
            UnifyCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    UnifyText(
                        text = "识别结果",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    UnifyText(text = recognizedText)
                }
            }
        }
    }
}
