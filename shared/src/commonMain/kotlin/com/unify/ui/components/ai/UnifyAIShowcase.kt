package com.unify.ui.components.ai

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unify.core.ai.UnifyAIEngine

/**
 * AI组件展示界面 - 集成所有智能UI组件的演示界面
 */
@Composable
fun UnifyAIShowcase(
    aiEngine: UnifyAIEngine,
    modifier: Modifier = Modifier
) {
    var selectedComponent by remember { mutableStateOf(AIComponentType.CHAT) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 标题
        Text(
            text = "Unify AI 智能组件展示",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // 组件选择器
        ComponentSelector(
            selectedComponent = selectedComponent,
            onComponentSelected = { selectedComponent = it },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 组件展示区域
        Card(
            modifier = Modifier.weight(1f),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            when (selectedComponent) {
                AIComponentType.CHAT -> {
                    UnifySmartChatComponent(
                        aiEngine = aiEngine,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AIComponentType.IMAGE_GENERATOR -> {
                    UnifySmartImageGenerator(
                        aiEngine = aiEngine,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AIComponentType.VOICE -> {
                    UnifySmartVoiceComponent(
                        aiEngine = aiEngine,
                        onTextRecognized = { /* 处理识别结果 */ },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AIComponentType.ASSISTANT -> {
                    UnifySmartAssistant(
                        aiEngine = aiEngine,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AIComponentType.TRANSLATOR -> {
                    UnifySmartTranslator(
                        aiEngine = aiEngine,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 组件信息
        ComponentInfo(
            componentType = selectedComponent,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * 组件选择器
 */
@Composable
private fun ComponentSelector(
    selectedComponent: AIComponentType,
    onComponentSelected: (AIComponentType) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(AIComponentType.values()) { component ->
            FilterChip(
                onClick = { onComponentSelected(component) },
                label = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(component.icon, fontSize = 16.sp)
                        Text(component.displayName, fontSize = 14.sp)
                    }
                },
                selected = selectedComponent == component
            )
        }
    }
}

/**
 * 组件信息
 */
@Composable
private fun ComponentInfo(
    componentType: AIComponentType,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = componentType.displayName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = componentType.description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 功能特性
            Text(
                text = "主要功能:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
            
            componentType.features.forEach { feature ->
                Text(
                    text = "• $feature",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                )
            }
        }
    }
}

/**
 * AI组件类型枚举
 */
enum class AIComponentType(
    val displayName: String,
    val icon: String,
    val description: String,
    val features: List<String>
) {
    CHAT(
        displayName = "智能聊天",
        icon = "💬",
        description = "基于大语言模型的智能对话系统，支持多轮对话和上下文理解",
        features = listOf(
            "实时AI对话",
            "上下文记忆",
            "多种AI模型支持",
            "消息历史管理",
            "状态指示器"
        )
    ),
    IMAGE_GENERATOR(
        displayName = "图像生成",
        icon = "🎨",
        description = "AI驱动的图像生成工具，支持多种艺术风格和尺寸选择",
        features = listOf(
            "文本到图像生成",
            "多种艺术风格",
            "可调节图像尺寸",
            "生成历史记录",
            "批量生成支持"
        )
    ),
    VOICE(
        displayName = "语音识别",
        icon = "🎤",
        description = "智能语音转文本系统，支持多语言识别和实时音频可视化",
        features = listOf(
            "多语言语音识别",
            "实时音频可视化",
            "连续语音识别",
            "语音质量检测",
            "识别结果优化"
        )
    ),
    ASSISTANT(
        displayName = "AI助手",
        icon = "🤖",
        description = "多功能AI助手，提供聊天、任务管理、数据分析等综合服务",
        features = listOf(
            "多模式切换",
            "智能建议系统",
            "任务管理集成",
            "数据分析能力",
            "创意生成支持"
        )
    ),
    TRANSLATOR(
        displayName = "智能翻译",
        icon = "🌐",
        description = "基于AI的多语言翻译系统，支持自动语言检测和翻译历史",
        features = listOf(
            "多语言互译",
            "自动语言检测",
            "翻译历史记录",
            "语言快速切换",
            "批量翻译支持"
        )
    )
}
