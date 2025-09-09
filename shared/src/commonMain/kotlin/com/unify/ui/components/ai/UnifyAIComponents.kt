package com.unify.ui.components.ai

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.Flow

/**
 * Unify跨平台AI组件
 * 支持智能聊天、图像生成、语音识别等AI功能
 */

data class ChatMessage(
    val id: String,
    val content: String,
    val isUser: Boolean,
    val timestamp: Long,
    val type: MessageType = MessageType.TEXT,
    val metadata: Map<String, Any> = emptyMap(),
)

enum class MessageType {
    TEXT,
    IMAGE,
    AUDIO,
    VIDEO,
    FILE,
}

enum class AIModelType {
    CHAT_GPT,
    CLAUDE,
    GEMINI,
    LLAMA,
    CUSTOM,
}

data class AIConfig(
    val modelType: AIModelType = AIModelType.CHAT_GPT,
    val apiKey: String = "",
    val maxTokens: Int = 2048,
    val temperature: Float = 0.7f,
    val systemPrompt: String = "",
    val enableStreaming: Boolean = true,
)

@Composable
expect fun UnifyAIChat(
    messages: List<ChatMessage>,
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
    config: AIConfig = AIConfig(),
    isLoading: Boolean = false,
    placeholder: String = "输入消息...",
    enableVoiceInput: Boolean = true,
    enableImageUpload: Boolean = true,
)

@Composable
expect fun UnifyAIImageGenerator(
    onImageGenerated: (String) -> Unit, // Base64 or URL
    modifier: Modifier = Modifier,
    config: AIConfig = AIConfig(),
    prompt: String = "",
    onPromptChange: (String) -> Unit = {},
    isGenerating: Boolean = false,
    generatedImages: List<String> = emptyList(),
)

@Composable
expect fun UnifyVoiceRecognition(
    onTextRecognized: (String) -> Unit,
    modifier: Modifier = Modifier,
    isListening: Boolean = false,
    onListeningChange: (Boolean) -> Unit = {},
    language: String = "zh-CN",
    continuous: Boolean = false,
)

@Composable
expect fun UnifyTextToSpeech(
    text: String,
    onSpeechComplete: () -> Unit = {},
    modifier: Modifier = Modifier,
    language: String = "zh-CN",
    rate: Float = 1.0f,
    pitch: Float = 1.0f,
    autoPlay: Boolean = false,
)

@Composable
expect fun UnifyAIAssistant(
    onQuery: (String) -> Flow<String>,
    modifier: Modifier = Modifier,
    config: AIConfig = AIConfig(),
    suggestions: List<String> = emptyList(),
    enableContextMemory: Boolean = true,
    maxHistorySize: Int = 100,
)

@Composable
expect fun UnifySmartRecommendation(
    items: List<RecommendationItem>,
    onItemSelected: (RecommendationItem) -> Unit,
    modifier: Modifier = Modifier,
    maxRecommendations: Int = 10,
    refreshInterval: Long = 300000L, // 5 minutes
)

data class RecommendationItem(
    val id: String,
    val title: String,
    val description: String,
    val score: Float,
    val category: String,
    val metadata: Map<String, Any> = emptyMap(),
)

@Composable
expect fun UnifyAITranslator(
    sourceText: String,
    onTranslated: (String) -> Unit,
    modifier: Modifier = Modifier,
    sourceLanguage: String = "auto",
    targetLanguage: String = "en",
    enableAutoDetect: Boolean = true,
)

@Composable
expect fun UnifySmartForm(
    fields: List<FormField>,
    onFieldValueChange: (String, Any) -> Unit,
    modifier: Modifier = Modifier,
    enableAutoComplete: Boolean = true,
    enableValidation: Boolean = true,
    onSubmit: (Map<String, Any>) -> Unit = {},
)

data class FormField(
    val id: String,
    val label: String,
    val type: FieldType,
    val value: Any? = null,
    val required: Boolean = false,
    val validation: ((Any?) -> String?)? = null,
    val suggestions: List<String> = emptyList(),
)

enum class FieldType {
    TEXT,
    EMAIL,
    PASSWORD,
    NUMBER,
    DATE,
    SELECT,
    MULTISELECT,
    TEXTAREA,
}
