package com.unify.core.dynamic

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Card
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Unify动态化系统演示应用
 * 展示AI增强的动态组件加载、智能推荐和性能优化功能
 */
@Composable
fun UnifyDynamicDemo() {
    var selectedTab by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Unify Dynamic Engine Demo",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("AI推荐") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("代码生成") }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("性能分析") }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("缓存管理") }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        when (selectedTab) {
            0 -> AIRecommendationDemo()
            1 -> CodeGenerationDemo()
            2 -> PerformanceAnalysisDemo()
            3 -> CacheManagementDemo()
        }
    }
}

/**
 * AI智能推荐演示
 */
@Composable
fun AIRecommendationDemo() {
    var recommendations by remember { mutableStateOf<List<ComponentRecommendation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    Column {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "AI智能组件推荐",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "基于使用模式、平台特性和性能需求的智能推荐",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            try {
                                val context = mapOf(
                                    "platform" to "android",
                                    "userPreferences" to mapOf("theme" to "material"),
                                    "usageHistory" to listOf("UnifyButton", "UnifyTextField", "UnifyCard"),
                                    "performance" to mapOf("minScore" to 0.8)
                                )
                                recommendations = UnifyDynamicEngine.getRecommendedComponents(context)
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("获取AI推荐")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (recommendations.isNotEmpty()) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(recommendations) { recommendation ->
                    RecommendationCard(recommendation = recommendation)
                }
            }
        }
    }
}

/**
 * 推荐卡片组件
 */
@Composable
fun RecommendationCard(recommendation: ComponentRecommendation) {
    Card(
        modifier = Modifier.fillMaxWidth()
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
                    text = recommendation.componentId,
                    style = MaterialTheme.typography.titleSmall
                )
                
                Surface(
                    color = when {
                        recommendation.score >= 0.8 -> MaterialTheme.colorScheme.primary
                        recommendation.score >= 0.6 -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.outline
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "${(recommendation.score * 100).toInt()}%",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = recommendation.reason,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "类别: ${recommendation.category}",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = "置信度: ${(recommendation.confidence * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

/**
 * AI代码生成演示
 */
@Composable
fun CodeGenerationDemo() {
    var componentType by remember { mutableStateOf("Button") }
    var generatedCode by remember { mutableStateOf<GeneratedComponentCode?>(null) }
    var isGenerating by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    Column {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "AI智能代码生成",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "基于需求自动生成高质量的Compose组件代码",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("组件类型:")
                    
                    FilterChip(
                        selected = componentType == "Button",
                        onClick = { componentType = "Button" },
                        label = { Text("Button") }
                    )
                    FilterChip(
                        selected = componentType == "TextField",
                        onClick = { componentType = "TextField" },
                        label = { Text("TextField") }
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = {
                        scope.launch {
                            isGenerating = true
                            try {
                                val requirements = mapOf(
                                    "customizations" to mapOf(
                                        "ComponentName" to "Custom${componentType}"
                                    )
                                )
                                generatedCode = UnifyDynamicEngine.generateComponentCode(componentType, requirements)
                            } finally {
                                isGenerating = false
                            }
                        }
                    },
                    enabled = !isGenerating
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("生成代码")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        generatedCode?.let { code ->
            GeneratedCodeCard(generatedCode = code)
        }
    }
}

/**
 * 生成代码卡片
 */
@Composable
fun GeneratedCodeCard(generatedCode: GeneratedComponentCode) {
    Card(
        modifier = Modifier.fillMaxWidth()
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
                    text = generatedCode.componentName,
                    style = MaterialTheme.typography.titleMedium
                )
                
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "质量: ${(generatedCode.quality * 100).toInt()}%",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = generatedCode.code,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (generatedCode.dependencies.isNotEmpty()) {
                Text(
                    text = "依赖: ${generatedCode.dependencies.joinToString(", ")}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * 性能分析演示
 */
@Composable
fun PerformanceAnalysisDemo() {
    var optimizationResult by remember { mutableStateOf<OptimizationResult?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    Column {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "智能性能分析",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "自动分析组件性能并提供优化建议",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = {
                        scope.launch {
                            isAnalyzing = true
                            try {
                                optimizationResult = UnifyDynamicEngine.optimizeComponents()
                            } finally {
                                isAnalyzing = false
                            }
                        }
                    },
                    enabled = !isAnalyzing
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("开始分析")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        optimizationResult?.let { result ->
            OptimizationResultCard(result = result)
        }
    }
}

/**
 * 优化结果卡片
 */
@Composable
fun OptimizationResultCard(result: OptimizationResult) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "性能优化分析结果",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PerformanceMetric(
                    label = "优化组件",
                    value = "${result.optimizedComponents}个"
                )
                PerformanceMetric(
                    label = "性能提升",
                    value = "${result.performanceGain.toInt()}ms"
                )
                PerformanceMetric(
                    label = "内存节省",
                    value = "${result.memoryReduction / 1024}KB"
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (result.recommendations.isNotEmpty()) {
                Text(
                    text = "优化建议:",
                    style = MaterialTheme.typography.titleSmall
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                result.recommendations.forEach { recommendation ->
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Text("• ", color = MaterialTheme.colorScheme.primary)
                        Text(
                            text = recommendation,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

/**
 * 性能指标组件
 */
@Composable
fun PerformanceMetric(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

/**
 * 缓存管理演示
 */
@Composable
fun CacheManagementDemo() {
    var cacheStats by remember { mutableStateOf<Map<String, Any>>(emptyMap()) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        cacheStats = UnifyDynamicEngine.getCacheStatistics()
    }
    
    Column {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "智能缓存管理",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "组件缓存统计和管理功能",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                cacheStats = UnifyDynamicEngine.getCacheStatistics()
                            }
                        }
                    ) {
                        Text("刷新统计")
                    }
                    
                    OutlinedButton(
                        onClick = {
                            UnifyDynamicEngine.cleanupExpiredCache()
                            scope.launch {
                                cacheStats = UnifyDynamicEngine.getCacheStatistics()
                            }
                        }
                    ) {
                        Text("清理过期")
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (cacheStats.isNotEmpty()) {
            CacheStatisticsCard(stats = cacheStats)
        }
    }
}

/**
 * 缓存统计卡片
 */
@Composable
fun CacheStatisticsCard(stats: Map<String, Any>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "缓存统计信息",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CacheMetric(
                    label = "总缓存",
                    value = "${stats["totalCached"]}个"
                )
                CacheMetric(
                    label = "有效缓存",
                    value = "${stats["validCached"]}个"
                )
                CacheMetric(
                    label = "命中率",
                    value = "${((stats["hitRate"] as? Double ?: 0.0) * 100).toInt()}%"
                )
                CacheMetric(
                    label = "缓存大小",
                    value = "${stats["cacheSize"]}B"
                )
            }
        }
    }
}

/**
 * 缓存指标组件
 */
@Composable
fun CacheMetric(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}
