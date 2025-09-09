package com.unify.ui.components.ai

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

/**
 * Desktop平台AI组件actual实现
 */

@Composable
actual fun UnifyAIChat(
    messages: List<ChatMessage>,
    onSendMessage: (String) -> Unit,
    modifier: Modifier,
    config: AIConfig,
    isLoading: Boolean,
    placeholder: String,
    enableVoiceInput: Boolean,
    enableImageUpload: Boolean,
) {
    var inputText by remember { mutableStateOf("") }

    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "AI聊天 - Desktop模拟",
                style = MaterialTheme.typography.headlineSmall,
            )

            LazyColumn(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(messages) { message ->
                    ChatMessageItem(message = message)
                }

                if (isLoading) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI思考中...")
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text(placeholder) },
                    modifier = Modifier.weight(1f),
                )

                Spacer(modifier = Modifier.width(8.dp))

                if (enableVoiceInput) {
                    IconButton(onClick = { /* 语音输入模拟 */ }) {
                        Icon(Icons.Default.Mic, contentDescription = "语音输入")
                    }
                }

                if (enableImageUpload) {
                    IconButton(onClick = { /* 图片上传模拟 */ }) {
                        Icon(Icons.Default.Image, contentDescription = "图片上传")
                    }
                }

                Button(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            onSendMessage(inputText)
                            inputText = ""
                        }
                    },
                ) {
                    Icon(Icons.Default.Send, contentDescription = "发送")
                }
            }
        }
    }
}

@Composable
private fun ChatMessageItem(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start,
    ) {
        Card(
            colors =
                CardDefaults.cardColors(
                    containerColor =
                        if (message.isUser) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                ),
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(12.dp),
                color =
                    if (message.isUser) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
            )
        }
    }
}

@Composable
actual fun UnifyAIImageGenerator(
    onImageGenerated: (String) -> Unit,
    modifier: Modifier,
    config: AIConfig,
    prompt: String,
    onPromptChange: (String) -> Unit,
    isGenerating: Boolean,
    generatedImages: List<String>,
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "AI图像生成 - Desktop模拟",
                style = MaterialTheme.typography.headlineSmall,
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = prompt,
                onValueChange = onPromptChange,
                label = { Text("输入描述") },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    onImageGenerated("mock_image_url_${System.currentTimeMillis()}")
                },
                enabled = !isGenerating,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("生成中...")
                } else {
                    Text("生成图像")
                }
            }

            if (generatedImages.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("生成的图像: ${generatedImages.size}张")
            }
        }
    }
}

@Composable
actual fun UnifyVoiceRecognition(
    onTextRecognized: (String) -> Unit,
    modifier: Modifier,
    isListening: Boolean,
    onListeningChange: (Boolean) -> Unit,
    language: String,
    continuous: Boolean,
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "语音识别 - Desktop模拟",
                style = MaterialTheme.typography.headlineSmall,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "麦克风",
                modifier = Modifier.size(64.dp),
                tint = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isListening) "正在监听..." else "点击开始语音识别",
                style = MaterialTheme.typography.bodyMedium,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    onListeningChange(!isListening)
                    if (!isListening) {
                        // 模拟识别结果
                        onTextRecognized("这是模拟的语音识别结果")
                    }
                },
                colors =
                    if (isListening) {
                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    } else {
                        ButtonDefaults.buttonColors()
                    },
            ) {
                Text(if (isListening) "停止识别" else "开始识别")
            }
        }
    }
}

@Composable
actual fun UnifyTextToSpeech(
    text: String,
    onSpeechComplete: () -> Unit,
    modifier: Modifier,
    language: String,
    rate: Float,
    pitch: Float,
    autoPlay: Boolean,
) {
    var isPlaying by remember { mutableStateOf(false) }

    LaunchedEffect(autoPlay, text) {
        if (autoPlay && text.isNotBlank()) {
            isPlaying = true
            delay(2000) // 模拟播放时间
            isPlaying = false
            onSpeechComplete()
        }
    }

    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "文本转语音 - Desktop模拟",
                style = MaterialTheme.typography.headlineSmall,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "文本内容: $text",
                style = MaterialTheme.typography.bodyMedium,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (!isPlaying) {
                        isPlaying = true
                        // 模拟播放
                    }
                },
                enabled = !isPlaying && text.isNotBlank(),
            ) {
                if (isPlaying) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("播放中...")
                } else {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "播放")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("播放语音")
                }
            }
        }
    }
}

@Composable
actual fun UnifyAIAssistant(
    onQuery: (String) -> Flow<String>,
    modifier: Modifier,
    config: AIConfig,
    suggestions: List<String>,
    enableContextMemory: Boolean,
    maxHistorySize: Int,
) {
    var query by remember { mutableStateOf("") }
    var response by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "AI助手 - Desktop模拟",
                style = MaterialTheme.typography.headlineSmall,
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("输入问题") },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 显示建议
            if (suggestions.isNotEmpty()) {
                Text("建议问题:", style = MaterialTheme.typography.bodySmall)
                suggestions.take(3).forEach { suggestion ->
                    TextButton(onClick = { query = suggestion }) {
                        Text(suggestion, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Button(
                onClick = {
                    if (query.isNotBlank()) {
                        isLoading = true
                        response = "AI助手正在思考您的问题: \"$query\""
                        isLoading = false
                    }
                },
                enabled = !isLoading && query.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("询问AI")
            }

            if (response.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                ) {
                    Text(
                        text = response,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }
        }
    }
}

@Composable
actual fun UnifySmartRecommendation(
    items: List<RecommendationItem>,
    onItemSelected: (RecommendationItem) -> Unit,
    modifier: Modifier,
    maxRecommendations: Int,
    refreshInterval: Long,
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "智能推荐 - Desktop模拟",
                style = MaterialTheme.typography.headlineSmall,
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(items.take(maxRecommendations)) { item ->
                    Card(
                        onClick = { onItemSelected(item) },
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            ),
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = item.description,
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Text(
                                text = "评分: ${item.score}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
actual fun UnifyAITranslator(
    sourceText: String,
    onTranslated: (String) -> Unit,
    modifier: Modifier,
    sourceLanguage: String,
    targetLanguage: String,
    enableAutoDetect: Boolean,
) {
    var translatedText by remember { mutableStateOf("") }
    var isTranslating by remember { mutableStateOf(false) }

    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "AI翻译 - Desktop模拟",
                style = MaterialTheme.typography.headlineSmall,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("原文: $sourceText")

            Spacer(modifier = Modifier.height(8.dp))

            Text("$sourceLanguage → $targetLanguage")

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    isTranslating = true
                    translatedText = "翻译结果: $sourceText (模拟翻译)"
                    onTranslated(translatedText)
                    isTranslating = false
                },
                enabled = !isTranslating && sourceText.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isTranslating) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("翻译")
            }

            if (translatedText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                ) {
                    Text(
                        text = translatedText,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }
        }
    }
}

@Composable
actual fun UnifySmartForm(
    fields: List<FormField>,
    onFieldValueChange: (String, Any) -> Unit,
    modifier: Modifier,
    enableAutoComplete: Boolean,
    enableValidation: Boolean,
    onSubmit: (Map<String, Any>) -> Unit,
) {
    var formData by remember { mutableStateOf(fields.associate { it.id to (it.value ?: "") }) }

    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "智能表单 - Desktop模拟",
                style = MaterialTheme.typography.headlineSmall,
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(fields) { field ->
                    SmartFormField(
                        field = field,
                        value = formData[field.id] ?: "",
                        onValueChange = { value ->
                            formData = formData + (field.id to value)
                            onFieldValueChange(field.id, value)
                        },
                        enableValidation = enableValidation,
                    )
                }

                item {
                    Button(
                        onClick = { onSubmit(formData) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("提交表单")
                    }
                }
            }
        }
    }
}

@Composable
private fun SmartFormField(
    field: FormField,
    value: Any,
    onValueChange: (Any) -> Unit,
    enableValidation: Boolean,
) {
    Column {
        when (field.type) {
            FieldType.TEXT, FieldType.EMAIL, FieldType.PASSWORD -> {
                OutlinedTextField(
                    value = value.toString(),
                    onValueChange = { onValueChange(it) },
                    label = { Text(field.label) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = enableValidation && field.validation?.invoke(value) != null,
                )
            }
            FieldType.NUMBER -> {
                OutlinedTextField(
                    value = value.toString(),
                    onValueChange = {
                        it.toDoubleOrNull()?.let(onValueChange) ?: onValueChange(it)
                    },
                    label = { Text(field.label) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            FieldType.TEXTAREA -> {
                OutlinedTextField(
                    value = value.toString(),
                    onValueChange = { onValueChange(it) },
                    label = { Text(field.label) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                )
            }
            FieldType.DATE -> {
                OutlinedTextField(
                    value = value.toString(),
                    onValueChange = { onValueChange(it) },
                    label = { Text("${field.label} (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            FieldType.SELECT, FieldType.MULTISELECT -> {
                Text("${field.label}: 选择器 (模拟)")
            }
        }

        // 显示验证错误
        if (enableValidation) {
            field.validation?.invoke(value)?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
