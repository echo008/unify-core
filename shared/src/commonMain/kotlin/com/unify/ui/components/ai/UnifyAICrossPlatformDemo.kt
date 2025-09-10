package com.unify.ui.components.ai

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unify.core.ai.*
import kotlinx.coroutines.launch

/**
 * 跨平台AI功能统一演示界面
 */
@Composable
fun UnifyAICrossPlatformDemo(
    crossPlatformManager: UnifyAICrossPlatformManager,
    modifier: Modifier = Modifier
) {
    var selectedDemo by remember { mutableStateOf(DemoType.OVERVIEW) }
    val coroutineScope = rememberCoroutineScope()
    
    // 监听AI引擎状态
    val engineState by crossPlatformManager.engineState.collectAsState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 标题和状态
        DemoHeader(
            engineState = engineState,
            crossPlatformManager = crossPlatformManager
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 演示类型选择
        DemoSelector(
            selectedDemo = selectedDemo,
            onDemoSelected = { selectedDemo = it },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 演示内容
        Card(
            modifier = Modifier.weight(1f),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            when (selectedDemo) {
                DemoType.OVERVIEW -> {
                    OverviewDemo(
                        crossPlatformManager = crossPlatformManager,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                DemoType.TEXT_GENERATION -> {
                    TextGenerationDemo(
                        crossPlatformManager = crossPlatformManager,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                DemoType.IMAGE_GENERATION -> {
                    ImageGenerationDemo(
                        crossPlatformManager = crossPlatformManager,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                DemoType.SPEECH_PROCESSING -> {
                    SpeechProcessingDemo(
                        crossPlatformManager = crossPlatformManager,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                DemoType.TRANSLATION -> {
                    TranslationDemo(
                        crossPlatformManager = crossPlatformManager,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                DemoType.ANALYSIS -> {
                    AnalysisDemo(
                        crossPlatformManager = crossPlatformManager,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

/**
 * 演示头部
 */
@Composable
private fun DemoHeader(
    engineState: AIEngineState,
    crossPlatformManager: UnifyAICrossPlatformManager,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Unify AI 跨平台功能演示",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 引擎状态
            AIEngineStatusChip(engineState = engineState)
            
            // 活跃提供商
            val activeProvider = crossPlatformManager.getActiveProvider()
            Text(
                text = "提供商: ${activeProvider?.displayName ?: "未配置"}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 支持的能力
        Text(
            text = "支持的AI能力: ${crossPlatformManager.supportedCapabilities.size}项",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 演示选择器
 */
@Composable
private fun DemoSelector(
    selectedDemo: DemoType,
    onDemoSelected: (DemoType) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(DemoType.values().toList().chunked(3)) { rowDemos ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowDemos.forEach { demo ->
                    FilterChip(
                        onClick = { onDemoSelected(demo) },
                        label = { 
                            Text(
                                text = "${demo.icon} ${demo.displayName}",
                                fontSize = 12.sp
                            )
                        },
                        selected = selectedDemo == demo,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // 填充空白
                repeat(3 - rowDemos.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * 概览演示
 */
@Composable
private fun OverviewDemo(
    crossPlatformManager: UnifyAICrossPlatformManager,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "跨平台AI功能概览",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            // 使用统计
            val stats = crossPlatformManager.getUsageStatistics()
            UsageStatisticsCard(stats = stats)
        }
        
        item {
            // 支持的能力列表
            SupportedCapabilitiesCard(
                capabilities = crossPlatformManager.supportedCapabilities
            )
        }
        
        item {
            // 平台特性
            PlatformFeaturesCard()
        }
    }
}

/**
 * 文本生成演示
 */
@Composable
private fun TextGenerationDemo(
    crossPlatformManager: UnifyAICrossPlatformManager,
    modifier: Modifier = Modifier
) {
    var prompt by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    Column(
        modifier = modifier.padding(16.dp)
    ) {
        Text(
            text = "文本生成演示",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            label = { Text("输入提示词") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                isGenerating = true
                coroutineScope.launch {
                    val aiResult = crossPlatformManager.generateText(
                        prompt = prompt,
                        maxTokens = 1000,
                        temperature = 0.7f
                    )
                    
                    when (aiResult) {
                        is AIResult.Success -> result = aiResult.content
                        is AIResult.Error -> result = "错误: ${aiResult.message}"
                    }
                    isGenerating = false
                }
            },
            enabled = !isGenerating && prompt.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isGenerating) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(if (isGenerating) "生成中..." else "生成文本")
        }
        
        if (result.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Card {
                Text(
                    text = result,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 14.sp
                )
            }
        }
    }
}

/**
 * 图像生成演示
 */
@Composable
private fun ImageGenerationDemo(
    crossPlatformManager: UnifyAICrossPlatformManager,
    modifier: Modifier = Modifier
) {
    var prompt by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    Column(
        modifier = modifier.padding(16.dp)
    ) {
        Text(
            text = "图像生成演示",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            label = { Text("描述要生成的图像") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                isGenerating = true
                coroutineScope.launch {
                    val aiResult = crossPlatformManager.generateImage(
                        prompt = prompt,
                        style = "realistic",
                        size = "1024x1024"
                    )
                    
                    when (aiResult) {
                        is AIResult.Success -> result = "图像生成成功: ${aiResult.content}"
                        is AIResult.Error -> result = "错误: ${aiResult.message}"
                    }
                    isGenerating = false
                }
            },
            enabled = !isGenerating && prompt.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isGenerating) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(if (isGenerating) "生成中..." else "生成图像")
        }
        
        if (result.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Card {
                Text(
                    text = result,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 14.sp
                )
            }
        }
    }
}

/**
 * 语音处理演示
 */
@Composable
private fun SpeechProcessingDemo(
    crossPlatformManager: UnifyAICrossPlatformManager,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "语音处理演示",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "🎤",
            fontSize = 64.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "语音识别和文本转语音功能",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = { /* 实现语音识别 */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("开始语音识别")
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Button(
            onClick = { /* 实现文本转语音 */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("文本转语音")
        }
    }
}

/**
 * 翻译演示
 */
@Composable
private fun TranslationDemo(
    crossPlatformManager: UnifyAICrossPlatformManager,
    modifier: Modifier = Modifier
) {
    var sourceText by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var isTranslating by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    Column(
        modifier = modifier.padding(16.dp)
    ) {
        Text(
            text = "翻译演示",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = sourceText,
            onValueChange = { sourceText = it },
            label = { Text("输入要翻译的文本") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                isTranslating = true
                coroutineScope.launch {
                    val aiResult = crossPlatformManager.translateText(
                        text = sourceText,
                        sourceLanguage = "auto",
                        targetLanguage = "en"
                    )
                    
                    when (aiResult) {
                        is AIResult.Success -> result = aiResult.content
                        is AIResult.Error -> result = "错误: ${aiResult.message}"
                    }
                    isTranslating = false
                }
            },
            enabled = !isTranslating && sourceText.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isTranslating) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(if (isTranslating) "翻译中..." else "翻译到英文")
        }
        
        if (result.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Card {
                Text(
                    text = result,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 14.sp
                )
            }
        }
    }
}

/**
 * 分析演示
 */
@Composable
private fun AnalysisDemo(
    crossPlatformManager: UnifyAICrossPlatformManager,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }
    var sentimentResult by remember { mutableStateOf("") }
    var summaryResult by remember { mutableStateOf("") }
    var isAnalyzing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    Column(
        modifier = modifier.padding(16.dp)
    ) {
        Text(
            text = "文本分析演示",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("输入要分析的文本") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    isAnalyzing = true
                    coroutineScope.launch {
                        val result = crossPlatformManager.analyzeSentiment(text)
                        when (result) {
                            is AIResult.Success -> sentimentResult = result.content
                            is AIResult.Error -> sentimentResult = "错误: ${result.message}"
                        }
                        isAnalyzing = false
                    }
                },
                enabled = !isAnalyzing && text.isNotBlank(),
                modifier = Modifier.weight(1f)
            ) {
                Text("情感分析")
            }
            
            Button(
                onClick = {
                    isAnalyzing = true
                    coroutineScope.launch {
                        val result = crossPlatformManager.summarizeText(text, accuracy = 80)
                        when (result) {
                            is AIResult.Success -> summaryResult = result.content
                            is AIResult.Error -> summaryResult = "错误: ${result.message}"
                        }
                        isAnalyzing = false
                    }
                },
                enabled = !isAnalyzing && text.isNotBlank(),
                modifier = Modifier.weight(1f)
            ) {
                Text("文本摘要")
            }
        }
        
        if (sentimentResult.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("情感分析结果:", fontWeight = FontWeight.Medium)
                    Text(sentimentResult, fontSize = 14.sp)
                }
            }
        }
        
        if (summaryResult.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("摘要结果:", fontWeight = FontWeight.Medium)
                    Text(summaryResult, fontSize = 14.sp)
                }
            }
        }
    }
}

/**
 * 使用统计卡片
 */
@Composable
private fun UsageStatisticsCard(
    stats: AIUsageStatistics,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "使用统计",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text("总请求数: ${stats.totalRequests}")
            Text("成功请求: ${stats.successfulRequests}")
            Text("失败请求: ${stats.failedRequests}")
            Text("平均响应时间: ${stats.averageResponseTime}ms")
            Text("活跃提供商: ${stats.activeProvider}")
        }
    }
}

/**
 * 支持的能力卡片
 */
@Composable
private fun SupportedCapabilitiesCard(
    capabilities: List<AICapabilityType>,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "支持的AI能力",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            capabilities.forEach { capability ->
                Text(
                    text = "• ${capability.name.replace('_', ' ')}",
                    fontSize = 12.sp
                )
            }
        }
    }
}

/**
 * 平台特性卡片
 */
@Composable
private fun PlatformFeaturesCard(
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "跨平台特性",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            listOf(
                "统一的AI接口",
                "平台自适应优化",
                "原生性能保证",
                "多厂商API支持",
                "智能降级策略",
                "实时状态监控"
            ).forEach { feature ->
                Text(
                    text = "✓ $feature",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * 演示类型枚举
 */
enum class DemoType(val displayName: String, val icon: String) {
    OVERVIEW("概览", "📊"),
    TEXT_GENERATION("文本生成", "✍️"),
    IMAGE_GENERATION("图像生成", "🎨"),
    SPEECH_PROCESSING("语音处理", "🎤"),
    TRANSLATION("翻译", "🌐"),
    ANALYSIS("文本分析", "🔍")
}
