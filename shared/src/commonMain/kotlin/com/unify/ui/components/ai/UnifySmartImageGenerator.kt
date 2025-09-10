package com.unify.ui.components.ai

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unify.core.ai.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope

/**
 * 智能图像生成组件 - 基于AI引擎的图像生成界面
 */
@Composable
fun UnifySmartImageGenerator(
    aiEngine: UnifyAIEngine,
    modifier: Modifier = Modifier,
    placeholder: String = "描述您想要生成的图像...",
    maxImages: Int = 20
) {
    var prompt by remember { mutableStateOf("") }
    var generatedImages by remember { mutableStateOf<List<GeneratedImage>>(emptyList()) }
    var isGenerating by remember { mutableStateOf(false) }
    var selectedStyle by remember { mutableStateOf(ImageStyle.REALISTIC) }
    var selectedSize by remember { mutableStateOf(ImageSize.MEDIUM) }
    val coroutineScope = rememberCoroutineScope()
    
    // 监听AI引擎状态
    val engineState by aiEngine.engineState.collectAsState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 标题和状态
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AI图像生成",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            AIEngineStatusChip(engineState = engineState)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 输入区域
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // 提示词输入
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(placeholder) },
                    enabled = !isGenerating && engineState == AIEngineState.READY,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { 
                        if (prompt.isNotBlank()) generateImage(aiEngine, prompt, selectedStyle, selectedSize, coroutineScope) { result ->
                            when (result) {
                                is ImageGenerationResult.Success -> {
                                    generatedImages = (generatedImages + result.image).takeLast(maxImages)
                                }
                                is ImageGenerationResult.Error -> {
                                    // 处理错误
                                }
                            }
                            isGenerating = false
                        }
                    }),
                    minLines = 2,
                    maxLines = 4
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 样式选择
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "样式:",
                        modifier = Modifier.align(Alignment.CenterVertically),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    ImageStyle.values().forEach { style ->
                        FilterChip(
                            onClick = { selectedStyle = style },
                            label = { Text(style.displayName, fontSize = 12.sp) },
                            selected = selectedStyle == style
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 尺寸选择
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "尺寸:",
                        modifier = Modifier.align(Alignment.CenterVertically),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    ImageSize.values().forEach { size ->
                        FilterChip(
                            onClick = { selectedSize = size },
                            label = { Text(size.displayName, fontSize = 12.sp) },
                            selected = selectedSize == size
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 生成按钮
                Button(
                    onClick = {
                        if (prompt.isNotBlank()) {
                            isGenerating = true
                            generateImage(aiEngine, prompt, selectedStyle, selectedSize, coroutineScope) { result ->
                                when (result) {
                                    is ImageGenerationResult.Success -> {
                                        generatedImages = (generatedImages + result.image).takeLast(maxImages)
                                    }
                                    is ImageGenerationResult.Error -> {
                                        // 可以添加错误提示
                                    }
                                }
                                isGenerating = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isGenerating && engineState == AIEngineState.READY && prompt.isNotBlank()
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("生成中...")
                    } else {
                        Text("生成图像")
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 生成的图像网格
        if (generatedImages.isNotEmpty()) {
            Text(
                text = "生成的图像 (${generatedImages.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(generatedImages.reversed()) { image ->
                    GeneratedImageCard(
                        image = image,
                        modifier = Modifier.aspectRatio(1f)
                    )
                }
            }
        } else {
            // 空状态
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🎨",
                        fontSize = 48.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "输入描述开始生成图像",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * AI引擎状态芯片
 */
@Composable
internal fun AIEngineStatusChip(
    engineState: AIEngineState,
    modifier: Modifier = Modifier
) {
    val (statusText, statusColor) = when (engineState) {
        AIEngineState.READY -> "就绪" to Color.Green
        AIEngineState.PROCESSING -> "处理中" to Color(0xFFFFA500)
        AIEngineState.ERROR -> "错误" to Color.Red
        else -> "初始化" to Color.Blue
    }
    
    AssistChip(
        onClick = { },
        label = { Text(statusText, fontSize = 12.sp) },
        modifier = modifier,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = statusColor.copy(alpha = 0.1f),
            labelColor = statusColor
        )
    )
}

/**
 * 生成图像卡片
 */
@Composable
private fun GeneratedImageCard(
    image: GeneratedImage,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // 图像占位符（实际应用中应显示真实图像）
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                contentAlignment = Alignment.Center
            ) {
                // 这里应该显示实际的图像
                // 由于是演示，显示占位符
                Text(
                    text = "🖼️",
                    fontSize = 32.sp
                )
            }
            
            // 图像信息
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = image.prompt.take(50) + if (image.prompt.length > 50) "..." else "",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = image.style.displayName,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Text(
                        text = image.size.displayName,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

/**
 * 生成图像
 */
internal fun generateImage(
    aiEngine: UnifyAIEngine,
    prompt: String,
    style: ImageStyle,
    size: ImageSize,
    coroutineScope: CoroutineScope,
    onResult: (ImageGenerationResult) -> Unit
) {
    coroutineScope.launch {
        try {
            val enhancedPrompt = buildString {
                append(prompt)
                append(", ")
                append(style.promptModifier)
                append(", high quality, detailed")
            }
            
            val aiRequest = AIRequest(
                type = AICapabilityType.IMAGE_GENERATION,
                input = enhancedPrompt,
                parameters = mapOf(
                    "size" to size.dimensions,
                    "style" to style.name.lowercase()
                )
            )
            
            val result = aiEngine.processRequest(aiRequest)
            
            when (result) {
                is AIResult.Success -> {
                    val generatedImage = GeneratedImage(
                        id = com.unify.core.platform.getCurrentTimeMillis().toString(),
                        prompt = prompt,
                        imageUrl = result.content, // 实际应用中这里是图像URL
                        style = style,
                        size = size,
                        timestamp = com.unify.core.platform.getCurrentTimeMillis()
                    )
                    onResult(ImageGenerationResult.Success(generatedImage))
                }
                is AIResult.Error -> {
                    onResult(ImageGenerationResult.Error(result.message))
                }
            }
        } catch (e: Exception) {
            onResult(ImageGenerationResult.Error("生成图像时发生错误: ${e.message}"))
        }
    }
}

/**
 * 图像样式枚举
 */
enum class ImageStyle(val displayName: String, val promptModifier: String) {
    REALISTIC("写实", "photorealistic, realistic"),
    ARTISTIC("艺术", "artistic, painting style"),
    CARTOON("卡通", "cartoon style, animated"),
    ABSTRACT("抽象", "abstract art, modern"),
    VINTAGE("复古", "vintage style, retro")
}

/**
 * 图像尺寸枚举
 */
enum class ImageSize(val displayName: String, val dimensions: String) {
    SMALL("小", "512x512"),
    MEDIUM("中", "1024x1024"),
    LARGE("大", "1536x1536")
}

/**
 * 生成的图像数据类
 */
data class GeneratedImage(
    val id: String,
    val prompt: String,
    val imageUrl: String,
    val style: ImageStyle,
    val size: ImageSize,
    val timestamp: Long
)

/**
 * 图像生成结果密封类
 */
sealed class ImageGenerationResult {
    data class Success(val image: GeneratedImage) : ImageGenerationResult()
    data class Error(val message: String) : ImageGenerationResult()
}
