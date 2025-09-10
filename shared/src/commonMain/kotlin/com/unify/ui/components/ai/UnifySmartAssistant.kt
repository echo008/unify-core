package com.unify.ui.components.ai

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unify.core.ai.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

/**
 * 智能助手组件 - 多功能AI助手界面
 */
@Composable
fun UnifySmartAssistant(
    aiEngine: UnifyAIEngine,
    modifier: Modifier = Modifier,
    enableContextMemory: Boolean = true,
    maxHistorySize: Int = 100
) {
    var selectedMode by remember { mutableStateOf(AssistantMode.CHAT) }
    var conversationHistory by remember { mutableStateOf<List<ConversationItem>>(emptyList()) }
    var currentQuery by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    // 监听AI引擎状态
    val engineState by aiEngine.engineState.collectAsState()
    
    // 预设建议
    val suggestions = remember {
        listOf(
            "帮我写一份会议纪要",
            "翻译这段文字",
            "总结这篇文章的要点",
            "生成一个创意方案",
            "分析数据趋势",
            "写一封邮件"
        )
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 标题和模式选择
        AssistantHeader(
            selectedMode = selectedMode,
            onModeSelected = { selectedMode = it },
            engineState = engineState
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 主要内容区域
        when (selectedMode) {
            AssistantMode.CHAT -> {
                ChatInterface(
                    aiEngine = aiEngine,
                    conversationHistory = conversationHistory,
                    onHistoryUpdate = { conversationHistory = it },
                    suggestions = suggestions,
                    isProcessing = isProcessing,
                    onProcessingChange = { isProcessing = it },
                    modifier = Modifier.weight(1f)
                )
            }
            AssistantMode.TASKS -> {
                TaskInterface(
                    aiEngine = aiEngine,
                    modifier = Modifier.weight(1f)
                )
            }
            AssistantMode.ANALYSIS -> {
                AnalysisInterface(
                    aiEngine = aiEngine,
                    modifier = Modifier.weight(1f)
                )
            }
            AssistantMode.CREATIVE -> {
                CreativeInterface(
                    aiEngine = aiEngine,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * 助手头部
 */
@Composable
private fun AssistantHeader(
    selectedMode: AssistantMode,
    onModeSelected: (AssistantMode) -> Unit,
    engineState: AIEngineState,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AI智能助手",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            AIEngineStatusChip(engineState = engineState)
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 模式选择
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(AssistantMode.values()) { mode ->
                FilterChip(
                    onClick = { onModeSelected(mode) },
                    label = { 
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(mode.icon, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(mode.displayName, fontSize = 12.sp)
                        }
                    },
                    selected = selectedMode == mode
                )
            }
        }
    }
}

/**
 * 聊天界面
 */
@Composable
private fun ChatInterface(
    aiEngine: UnifyAIEngine,
    conversationHistory: List<ConversationItem>,
    onHistoryUpdate: (List<ConversationItem>) -> Unit,
    suggestions: List<String>,
    isProcessing: Boolean,
    onProcessingChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    
    Column(modifier = modifier) {
        // 对话历史
        if (conversationHistory.isEmpty()) {
            // 空状态 - 显示建议
            SuggestionsGrid(
                suggestions = suggestions,
                onSuggestionSelected = { suggestion ->
                    inputText = suggestion
                },
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(conversationHistory) { item ->
                    ConversationItemCard(item = item)
                }
                
                if (isProcessing) {
                    item {
                        ProcessingIndicator()
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 输入区域
        ChatInputField(
            text = inputText,
            onTextChange = { inputText = it },
            onSend = {
                if (inputText.isNotBlank() && !isProcessing) {
                    val userItem = ConversationItem(
                        id = com.unify.core.platform.getCurrentTimeMillis().toString(),
                        content = inputText,
                        isUser = true,
                        timestamp = com.unify.core.platform.getCurrentTimeMillis(),
                        type = ConversationItemType.TEXT
                    )
                    
                    onHistoryUpdate(conversationHistory + userItem)
                    val currentInput = inputText
                    inputText = ""
                    onProcessingChange(true)
                    
                    coroutineScope.launch {
                        try {
                            val aiRequest = AIRequest(
                                type = AICapabilityType.TEXT_GENERATION,
                                input = currentInput
                            )
                            
                            val result = aiEngine.processRequest(aiRequest)
                            
                            when (result) {
                                is AIResult.Success -> {
                                    val aiItem = ConversationItem(
                                        id = com.unify.core.platform.getCurrentTimeMillis().toString(),
                                        content = result.content,
                                        isUser = false,
                                        timestamp = com.unify.core.platform.getCurrentTimeMillis(),
                                        type = ConversationItemType.TEXT
                                    )
                                    onHistoryUpdate(conversationHistory + userItem + aiItem)
                                }
                                is AIResult.Error -> {
                                    val errorItem = ConversationItem(
                                        id = com.unify.core.platform.getCurrentTimeMillis().toString(),
                                        content = "处理请求时出现错误：${result.message}",
                                        isUser = false,
                                        timestamp = com.unify.core.platform.getCurrentTimeMillis(),
                                        type = ConversationItemType.ERROR
                                    )
                                    onHistoryUpdate(conversationHistory + userItem + errorItem)
                                }
                            }
                        } catch (e: Exception) {
                            val errorItem = ConversationItem(
                                id = com.unify.core.platform.getCurrentTimeMillis().toString(),
                                content = "发生未知错误：${e.message}",
                                isUser = false,
                                timestamp = com.unify.core.platform.getCurrentTimeMillis(),
                                type = ConversationItemType.ERROR
                            )
                            onHistoryUpdate(conversationHistory + userItem + errorItem)
                        } finally {
                            onProcessingChange(false)
                        }
                    }
                }
            },
            placeholder = "输入您的问题或请求...",
            enabled = !isProcessing
        )
    }
}

/**
 * 建议网格
 */
@Composable
private fun SuggestionsGrid(
    suggestions: List<String>,
    onSuggestionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🤖",
            fontSize = 48.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "我是您的AI助手",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
        
        Text(
            text = "选择一个建议开始对话，或直接输入您的问题",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 建议卡片
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(suggestions.chunked(2)) { rowSuggestions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowSuggestions.forEach { suggestion ->
                        SuggestionCard(
                            text = suggestion,
                            onClick = { onSuggestionSelected(suggestion) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    // 如果是奇数个建议，添加空白占位
                    if (rowSuggestions.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/**
 * 建议卡片
 */
@Composable
private fun SuggestionCard(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 对话项卡片
 */
@Composable
private fun ConversationItemCard(
    item: ConversationItem,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (item.isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 300.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (item.isUser) 16.dp else 4.dp,
                bottomEnd = if (item.isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    item.type == ConversationItemType.ERROR -> MaterialTheme.colorScheme.errorContainer
                    item.isUser -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = item.content,
                    color = when {
                        item.type == ConversationItemType.ERROR -> MaterialTheme.colorScheme.onErrorContainer
                        item.isUser -> MaterialTheme.colorScheme.onPrimary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontSize = 14.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = formatTimestamp(item.timestamp),
                    fontSize = 10.sp,
                    color = when {
                        item.type == ConversationItemType.ERROR -> MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                        item.isUser -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    }
                )
            }
        }
    }
}

/**
 * 任务界面（简化实现）
 */
@Composable
private fun TaskInterface(
    aiEngine: UnifyAIEngine,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "任务管理功能开发中...",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 分析界面（简化实现）
 */
@Composable
private fun AnalysisInterface(
    aiEngine: UnifyAIEngine,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "数据分析功能开发中...",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 创意界面（简化实现）
 */
@Composable
private fun CreativeInterface(
    aiEngine: UnifyAIEngine,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "创意生成功能开发中...",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 处理指示器
 */
@Composable
private fun ProcessingIndicator(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "AI正在思考...",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 助手模式枚举
 */
enum class AssistantMode(val displayName: String, val icon: String) {
    CHAT("聊天", "💬"),
    TASKS("任务", "📋"),
    ANALYSIS("分析", "📊"),
    CREATIVE("创意", "🎨")
}

/**
 * 对话项数据类
 */
data class ConversationItem(
    val id: String,
    val content: String,
    val isUser: Boolean,
    val timestamp: Long,
    val type: ConversationItemType = ConversationItemType.TEXT
)

/**
 * 对话项类型枚举
 */
enum class ConversationItemType {
    TEXT,
    ERROR,
    SYSTEM
}

/**
 * 格式化时间戳
 */
private fun formatTimestamp(timestamp: Long): String {
    val now = com.unify.core.platform.getCurrentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60000 -> "刚刚"
        diff < 3600000 -> "${diff.toDouble() / 60000}分钟前"
        diff < 86400000 -> "${diff.toDouble() / 3600000}小时前"
        else -> "${diff.toDouble() / 86400000}天前"
    }
}
