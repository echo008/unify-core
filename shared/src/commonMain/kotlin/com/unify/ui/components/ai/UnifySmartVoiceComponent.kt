package com.unify.ui.components.ai

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unify.core.ai.*
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

/**
 * 智能语音识别组件 - 基于AI引擎的语音转文本界面
 */
@Composable
fun UnifySmartVoiceComponent(
    aiEngine: UnifyAIEngine,
    onTextRecognized: (String) -> Unit,
    modifier: Modifier = Modifier,
    language: String = "zh-CN",
    continuous: Boolean = false
) {
    var isListening by remember { mutableStateOf(false) }
    var recognizedText by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf(language) }
    var audioLevels by remember { mutableStateOf(List(20) { 0f }) }
    val coroutineScope = rememberCoroutineScope()
    
    // 监听AI引擎状态
    val engineState by aiEngine.engineState.collectAsState()
    
    // 模拟音频级别动画
    LaunchedEffect(isListening) {
        if (isListening) {
            while (isListening) {
                audioLevels = List(20) { Random.nextFloat() * if (isListening) 1f else 0.1f }
                kotlinx.coroutines.delay(100)
            }
        } else {
            audioLevels = List(20) { 0f }
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 标题和状态
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "智能语音识别",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            AIEngineStatusChip(engineState = engineState)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 语言选择
        LanguageSelector(
            selectedLanguage = selectedLanguage,
            onLanguageSelected = { selectedLanguage = it },
            enabled = !isListening && !isProcessing
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 音频可视化
        AudioVisualizer(
            audioLevels = audioLevels,
            isListening = isListening,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 录音按钮
        VoiceRecordButton(
            isListening = isListening,
            isProcessing = isProcessing,
            enabled = engineState == AIEngineState.READY,
            onClick = {
                if (isListening) {
                    // 停止录音并处理
                    isListening = false
                    isProcessing = true
                    
                    coroutineScope.launch {
                        try {
                            // 模拟语音识别处理
                            val aiRequest = AIRequest(
                                type = AICapabilityType.SPEECH_RECOGNITION,
                                input = "audio_data_placeholder", // 实际应用中这里是音频数据
                                parameters = mapOf(
                                    "language" to selectedLanguage,
                                    "continuous" to continuous.toString()
                                )
                            )
                            
                            val result = aiEngine.processRequest(aiRequest)
                            
                            when (result) {
                                is AIResult.Success -> {
                                    recognizedText = result.content
                                    onTextRecognized(result.content)
                                }
                                is AIResult.Error -> {
                                    recognizedText = "识别失败: ${result.message}"
                                }
                            }
                        } catch (e: Exception) {
                            recognizedText = "处理错误: ${e.message}"
                        } finally {
                            isProcessing = false
                        }
                    }
                } else {
                    // 开始录音
                    isListening = true
                    recognizedText = ""
                }
            }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 识别结果显示
        RecognitionResultCard(
            text = recognizedText,
            isProcessing = isProcessing,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 操作提示
        Text(
            text = when {
                isListening -> "正在录音，点击停止"
                isProcessing -> "正在处理语音..."
                engineState != AIEngineState.READY -> "AI引擎未就绪"
                else -> "点击开始语音识别"
            },
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 语言选择器
 */
@Composable
private fun LanguageSelector(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val languages = listOf(
        "zh-CN" to "中文",
        "en-US" to "English",
        "ja-JP" to "日本語",
        "ko-KR" to "한국어"
    )
    
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "语言:",
                modifier = Modifier.align(Alignment.CenterVertically),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            
            languages.forEach { (code, name) ->
                FilterChip(
                    onClick = { if (enabled) onLanguageSelected(code) },
                    label = { Text(name, fontSize = 12.sp) },
                    selected = selectedLanguage == code,
                    enabled = enabled
                )
            }
        }
    }
}

/**
 * 音频可视化器
 */
@Composable
private fun AudioVisualizer(
    audioLevels: List<Float>,
    isListening: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedLevels = audioLevels.map { level ->
        val animatedValue = remember { Animatable(0f) }
        LaunchedEffect(level) {
            animatedValue.animateTo(
                targetValue = level,
                animationSpec = tween(100)
            )
        }
        animatedValue.value
    }
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isListening) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            val barWidth = size.width / audioLevels.size
            val maxHeight = size.height
            
            animatedLevels.forEachIndexed { index, level ->
                val barHeight = maxHeight * level
                val x = index * barWidth + barWidth / 2
                
                drawLine(
                    color = if (isListening) Color(0xFF4CAF50) else Color.Gray,
                    start = Offset(x, maxHeight),
                    end = Offset(x, maxHeight - barHeight),
                    strokeWidth = barWidth * 0.6f
                )
            }
        }
    }
}

/**
 * 语音录制按钮
 */
@Composable
private fun VoiceRecordButton(
    isListening: Boolean,
    isProcessing: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isListening) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    val buttonColor = when {
        isProcessing -> MaterialTheme.colorScheme.secondary
        isListening -> Color(0xFFFF5722)
        else -> MaterialTheme.colorScheme.primary
    }
    
    Button(
        onClick = onClick,
        modifier = modifier
            .size(80.dp)
            .clip(CircleShape),
        enabled = enabled && !isProcessing,
        colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
        contentPadding = PaddingValues(0.dp)
    ) {
        if (isProcessing) {
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                strokeWidth = 3.dp,
                color = MaterialTheme.colorScheme.onSecondary
            )
        } else {
            Text(
                text = if (isListening) "🛑" else "🎤",
                fontSize = 32.sp
            )
        }
    }
}

/**
 * 识别结果卡片
 */
@Composable
private fun RecognitionResultCard(
    text: String,
    isProcessing: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "识别结果",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp),
                contentAlignment = if (text.isEmpty() && !isProcessing) Alignment.Center else Alignment.TopStart
            ) {
                if (text.isEmpty() && !isProcessing) {
                    Text(
                        text = "识别结果将显示在这里",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = if (isProcessing) "正在处理..." else text,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
