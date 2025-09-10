@file:OptIn(ExperimentalMaterial3Api::class)

package com.unify.ui.components.ai

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unify.core.ai.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * 智能翻译组件 - 基于AI引擎的多语言翻译界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifySmartTranslator(
    aiEngine: UnifyAIEngine,
    modifier: Modifier = Modifier,
    enableAutoDetect: Boolean = true
) {
    var sourceText by remember { mutableStateOf("") }
    var translatedText by remember { mutableStateOf("") }
    var sourceLanguage by remember { mutableStateOf("auto") }
    var targetLanguage by remember { mutableStateOf("en") }
    var isTranslating by remember { mutableStateOf(false) }
    var translationHistory by remember { mutableStateOf<List<TranslationRecord>>(emptyList()) }
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
                text = "AI智能翻译",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            AIEngineStatusChip(engineState = engineState)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 语言选择器
        LanguageSelectorRow(
            sourceLanguage = sourceLanguage,
            targetLanguage = targetLanguage,
            onSourceLanguageChange = { sourceLanguage = it },
            onTargetLanguageChange = { targetLanguage = it },
            onSwapLanguages = {
                if (sourceLanguage != "auto") {
                    val temp = sourceLanguage
                    sourceLanguage = targetLanguage
                    targetLanguage = temp
                    // 交换文本
                    val tempText = sourceText
                    sourceText = translatedText
                    translatedText = tempText
                }
            },
            enabled = !isTranslating
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 翻译区域
        TranslationArea(
            sourceText = sourceText,
            translatedText = translatedText,
            onSourceTextChange = { sourceText = it },
            isTranslating = isTranslating,
            onTranslate = {
                if (sourceText.isNotBlank()) {
                    isTranslating = true
                    coroutineScope.launch {
                        try {
                            val aiRequest = AIRequest(
                                type = AICapabilityType.TRANSLATION,
                                input = sourceText,
                                parameters = mapOf(
                                    "sourceLanguage" to sourceLanguage,
                                    "targetLanguage" to targetLanguage,
                                    "enableAutoDetect" to enableAutoDetect.toString()
                                )
                            )
                            
                            val result = aiEngine.processRequest(aiRequest)
                            
                            when (result) {
                                is AIResult.Success -> {
                                    translatedText = result.content
                                    
                                    // 添加到历史记录
                                    val record = TranslationRecord(
                                        id = com.unify.core.platform.getCurrentTimeMillis().toString(),
                                        sourceText = sourceText,
                                        translatedText = result.content,
                                        sourceLanguage = sourceLanguage,
                                        targetLanguage = targetLanguage,
                                        timestamp = com.unify.core.platform.getCurrentTimeMillis()
                                    )
                                    translationHistory = (translationHistory + record).takeLast(20)
                                }
                                is AIResult.Error -> {
                                    translatedText = "翻译失败: ${result.message}"
                                }
                            }
                        } catch (e: Exception) {
                            translatedText = "翻译错误: ${e.message}"
                        } finally {
                            isTranslating = false
                        }
                    }
                }
            },
            enabled = engineState == AIEngineState.READY && sourceText.isNotBlank()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 翻译历史
        if (translationHistory.isNotEmpty()) {
            TranslationHistory(
                history = translationHistory,
                onRecordSelected = { record ->
                    sourceText = record.sourceText
                    translatedText = record.translatedText
                    sourceLanguage = record.sourceLanguage
                    targetLanguage = record.targetLanguage
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * 语言选择行
 */
@Composable
private fun LanguageSelectorRow(
    sourceLanguage: String,
    targetLanguage: String,
    onSourceLanguageChange: (String) -> Unit,
    onTargetLanguageChange: (String) -> Unit,
    onSwapLanguages: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val languages = mapOf(
        "auto" to "自动检测",
        "zh" to "中文",
        "en" to "English",
        "ja" to "日本語",
        "ko" to "한국어",
        "fr" to "Français",
        "de" to "Deutsch",
        "es" to "Español",
        "ru" to "Русский"
    )
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 源语言选择
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "从",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                var sourceExpanded by remember { mutableStateOf(false) }
                
                ExposedDropdownMenuBox(
                    expanded = sourceExpanded,
                    onExpandedChange = { if (enabled) sourceExpanded = it }
                ) {
                    OutlinedTextField(
                        value = languages[sourceLanguage] ?: sourceLanguage,
                        onValueChange = { },
                        readOnly = true,
                        enabled = enabled,
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        trailingIcon = { 
                            @OptIn(ExperimentalMaterial3Api::class)
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = sourceExpanded) 
                        }
                    )
                    
                    ExposedDropdownMenu(
                        expanded = sourceExpanded,
                        onDismissRequest = { sourceExpanded = false }
                    ) {
                        languages.forEach { (code, name) ->
                            DropdownMenuItem(
                                text = { Text(name) },
                                onClick = {
                                    onSourceLanguageChange(code)
                                    sourceExpanded = false
                                }
                            )
                        }
                    }
                }
            }
            
            // 交换按钮
            IconButton(
                onClick = onSwapLanguages,
                enabled = enabled && sourceLanguage != "auto"
            ) {
                Text("⇄", fontSize = 20.sp)
            }
            
            // 目标语言选择
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "到",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                var targetExpanded by remember { mutableStateOf(false) }
                
                ExposedDropdownMenuBox(
                    expanded = targetExpanded,
                    onExpandedChange = { if (enabled) targetExpanded = it }
                ) {
                    OutlinedTextField(
                        value = languages[targetLanguage] ?: targetLanguage,
                        onValueChange = { },
                        readOnly = true,
                        enabled = enabled,
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = targetExpanded) }
                    )
                    
                    ExposedDropdownMenu(
                        expanded = targetExpanded,
                        onDismissRequest = { targetExpanded = false }
                    ) {
                        languages.filter { it.key != "auto" }.forEach { (code, name) ->
                            DropdownMenuItem(
                                text = { Text(name) },
                                onClick = {
                                    onTargetLanguageChange(code)
                                    targetExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 翻译区域
 */
@Composable
private fun TranslationArea(
    sourceText: String,
    translatedText: String,
    onSourceTextChange: (String) -> Unit,
    isTranslating: Boolean,
    onTranslate: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // 源文本输入
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "输入文本",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = sourceText,
                    onValueChange = onSourceTextChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("输入要翻译的文本...") },
                    enabled = !isTranslating,
                    minLines = 3,
                    maxLines = 6,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onTranslate() })
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = onTranslate,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled && !isTranslating
                ) {
                    if (isTranslating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("翻译中...")
                    } else {
                        Text("翻译")
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 翻译结果
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "翻译结果",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp)
                        .padding(12.dp),
                    contentAlignment = if (translatedText.isEmpty() && !isTranslating) Alignment.Center else Alignment.TopStart
                ) {
                    when {
                        isTranslating -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "正在翻译...",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        translatedText.isEmpty() -> {
                            Text(
                                text = "翻译结果将显示在这里",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center
                            )
                        }
                        else -> {
                            Text(
                                text = translatedText,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 翻译历史
 */
@Composable
private fun TranslationHistory(
    history: List<TranslationRecord>,
    onRecordSelected: (TranslationRecord) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "翻译历史",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(history.reversed()) { record ->
                TranslationHistoryCard(
                    record = record,
                    onClick = { onRecordSelected(record) }
                )
            }
        }
    }
}

/**
 * 翻译历史卡片
 */
@Composable
private fun TranslationHistoryCard(
    record: TranslationRecord,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${getLanguageName(record.sourceLanguage)} → ${getLanguageName(record.targetLanguage)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = formatTranslationTimestamp(record.timestamp),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = record.sourceText.take(50) + if (record.sourceText.length > 50) "..." else "",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = record.translatedText.take(50) + if (record.translatedText.length > 50) "..." else "",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 翻译记录数据类
 */
data class TranslationRecord(
    val id: String,
    val sourceText: String,
    val translatedText: String,
    val sourceLanguage: String,
    val targetLanguage: String,
    val timestamp: Long
)

/**
 * 获取语言名称
 */
internal fun getLanguageName(code: String): String {
    return when (code) {
        "auto" -> "自动"
        "zh" -> "中文"
        "en" -> "英语"
        "ja" -> "日语"
        "ko" -> "韩语"
        "fr" -> "法语"
        "de" -> "德语"
        "es" -> "西班牙语"
        "ru" -> "俄语"
        else -> code
    }
}

/**
 * 格式化时间戳
 */
internal fun formatTranslationTimestamp(timestamp: Long): String {
    val now = com.unify.core.platform.getCurrentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60000 -> "刚刚"
        diff < 3600000 -> "${diff.toDouble() / 60000}分钟前"
        diff < 86400000 -> "${diff.toDouble() / 3600000}小时前"
        else -> "${diff.toDouble() / 86400000}天前"
    }
}
