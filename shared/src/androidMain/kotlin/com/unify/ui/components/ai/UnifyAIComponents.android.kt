@file:OptIn(ExperimentalMaterial3Api::class)

package com.unify.ui.components.ai

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow

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

    Column(modifier = modifier.fillMaxSize()) {
        // Chat messages
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(messages) { message ->
                ChatMessageItem(message = message)
            }

            if (isLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                    ) {
                        Card(
                            colors =
                                CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                ),
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("AI正在思考...")
                            }
                        }
                    }
                }
            }
        }

        // Input area
        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (enableImageUpload) {
                    IconButton(onClick = { /* Handle image upload */ }) {
                        Icon(Icons.Default.Image, contentDescription = "Upload Image")
                    }
                }

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text(placeholder) },
                    modifier = Modifier.weight(1f),
                    maxLines = 3,
                )

                if (enableVoiceInput) {
                    IconButton(onClick = { /* Handle voice input */ }) {
                        Icon(Icons.Default.Mic, contentDescription = "Voice Input")
                    }
                }

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            onSendMessage(inputText)
                            inputText = ""
                        }
                    },
                    enabled = inputText.isNotBlank() && !isLoading,
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send")
                }
            }
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
    Column(modifier = modifier.fillMaxSize()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                Text(
                    text = "AI图像生成器",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = prompt,
                    onValueChange = onPromptChange,
                    label = { Text("描述你想要生成的图像") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onImageGenerated("mock_generated_image_${System.currentTimeMillis()}") },
                    enabled = !isGenerating && prompt.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("生成中...")
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("生成图像")
                    }
                }
            }
        }

        if (generatedImages.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "生成的图像",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(generatedImages) { imageUrl ->
                    Card(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("生成的图像: $imageUrl")
                        }
                    }
                }
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
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "语音识别",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            FloatingActionButton(
                onClick = {
                    onListeningChange(!isListening)
                    if (!isListening) {
                        // Simulate voice recognition
                        onTextRecognized("这是模拟的语音识别结果")
                    }
                },
                modifier = Modifier.size(64.dp),
                containerColor =
                    if (isListening) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                shape = CircleShape,
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = if (isListening) "停止录音" else "开始录音",
                    modifier = Modifier.size(32.dp),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isListening) "正在录音..." else "点击开始语音识别",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (isListening) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
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

    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "文本转语音",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Button(
                    onClick = {
                        isPlaying = !isPlaying
                        if (!isPlaying) {
                            onSpeechComplete()
                        }
                    },
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "停止" else "播放",
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isPlaying) "停止" else "播放")
                }
            }

            if (isPlaying) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
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
    var isProcessing by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                Text(
                    text = "AI助手",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("向AI助手提问") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        isProcessing = true
                        // Simulate AI response
                        response = "这是AI助手的模拟回复：$query"
                        isProcessing = false
                    },
                    enabled = !isProcessing && query.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("处理中...")
                    } else {
                        Text("提问")
                    }
                }
            }
        }

        if (suggestions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "建议问题",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(suggestions) { suggestion ->
                    Card(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable { query = suggestion },
                    ) {
                        Text(
                            text = suggestion,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
            }
        }

        if (response.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                ) {
                    Text(
                        text = "AI回复",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = response,
                        style = MaterialTheme.typography.bodyMedium,
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
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Text(
                text = "智能推荐",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(items.take(maxRecommendations)) { item ->
            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable { onItemSelected(item) },
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = "${(item.score * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = item.score,
                        modifier = Modifier.fillMaxWidth(),
                    )
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
    var inputText by remember { mutableStateOf(sourceText) }
    var translatedText by remember { mutableStateOf("") }
    var isTranslating by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "AI翻译器",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                label = { Text("输入要翻译的文本") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 5,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("$sourceLanguage → $targetLanguage")

                Button(
                    onClick = {
                        isTranslating = true
                        // Simulate translation
                        translatedText = "翻译结果: $inputText"
                        onTranslated(translatedText)
                        isTranslating = false
                    },
                    enabled = !isTranslating && inputText.isNotBlank(),
                ) {
                    if (isTranslating) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Text("翻译")
                    }
                }
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
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
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
    val fieldValues = remember { mutableStateMapOf<String, Any>() }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                text = "智能表单",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        }

        items(fields) { field ->
            SmartFormField(
                field = field,
                value = fieldValues[field.id],
                onValueChange = { value ->
                    fieldValues[field.id] = value
                    onFieldValueChange(field.id, value)
                },
                enableAutoComplete = enableAutoComplete,
                enableValidation = enableValidation,
            )
        }

        item {
            Button(
                onClick = { onSubmit(fieldValues.toMap()) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("提交")
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
            modifier = Modifier.widthIn(max = 280.dp),
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
private fun SmartFormField(
    field: FormField,
    value: Any?,
    onValueChange: (Any) -> Unit,
    enableAutoComplete: Boolean,
    enableValidation: Boolean,
) {
    when (field.type) {
        FieldType.TEXT -> {
            OutlinedTextField(
                value = value?.toString() ?: "",
                onValueChange = { onValueChange(it) },
                label = { Text(field.label) },
                modifier = Modifier.fillMaxWidth(),
                isError = enableValidation && field.validation?.invoke(value) != null,
            )
        }
        FieldType.EMAIL -> {
            OutlinedTextField(
                value = value?.toString() ?: "",
                onValueChange = { onValueChange(it) },
                label = { Text(field.label) },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            )
        }
        FieldType.PASSWORD -> {
            OutlinedTextField(
                value = value?.toString() ?: "",
                onValueChange = { onValueChange(it) },
                label = { Text(field.label) },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            )
        }
        FieldType.TEXTAREA -> {
            OutlinedTextField(
                value = value?.toString() ?: "",
                onValueChange = { onValueChange(it) },
                label = { Text(field.label) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 5,
            )
        }
        else -> {
            OutlinedTextField(
                value = value?.toString() ?: "",
                onValueChange = { onValueChange(it) },
                label = { Text(field.label) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
