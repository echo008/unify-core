package com.unify.demo

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.unify.core.ui.components.*
import com.unify.core.performance.UnifyPerformanceMonitor
import com.unify.core.performance.UnifyComposeOptimizer.PerformanceTracker
import kotlinx.coroutines.delay

/**
 * 性能监控屏幕 - 展示实时性能指标
 */
@Composable
fun PerformanceScreen(
    onNavigateBack: () -> Unit
) {
    var performanceSummary by remember { mutableStateOf(UnifyPerformanceMonitor.getPerformanceSummary()) }
    val metrics by UnifyPerformanceMonitor.metrics.collectAsState()
    
    // 定期更新性能数据
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000) // 每秒更新一次
            performanceSummary = UnifyPerformanceMonitor.getPerformanceSummary()
            
            // 模拟一些性能数据
            UnifyPerformanceMonitor.recordFrameTime(16) // 60 FPS
            UnifyPerformanceMonitor.recordMemoryUsage(50 * 1024 * 1024) // 50MB
        }
    }
    
    PerformanceTracker("PerformanceScreen") {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 顶部栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                UnifyButton(
                    onClick = onNavigateBack,
                    text = "← 返回"
                )
                Text(
                    text = "性能监控",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.width(80.dp))
            }
            
            // 性能概览
            UnifyCard {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "📊 性能概览",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        PerformanceMetricCard(
                            title = "帧率",
                            value = "${performanceSummary.averageFrameRate.toInt()}",
                            unit = "FPS",
                            icon = "🎯"
                        )
                        
                        PerformanceMetricCard(
                            title = "内存",
                            value = "${performanceSummary.memoryUsage.toInt()}",
                            unit = "MB",
                            icon = "💾"
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        PerformanceMetricCard(
                            title = "网络请求",
                            value = "${performanceSummary.networkRequestCount}",
                            unit = "次",
                            icon = "🌐"
                        )
                        
                        PerformanceMetricCard(
                            title = "重组次数",
                            value = "${performanceSummary.recompositionCount}",
                            unit = "次",
                            icon = "🔄"
                        )
                    }
                }
            }
            
            // 启动性能
            UnifyCard {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🚀 启动性能",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    val startupTime = System.currentTimeMillis() - performanceSummary.appStartTime
                    InfoRow("启动时间", "${startupTime}ms")
                    InfoRow("冷启动", "< 2000ms")
                    InfoRow("热启动", "< 500ms")
                }
            }
            
            // 详细指标
            UnifyCard {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "📈 详细指标",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    metrics.forEach { (name, metric) ->
                        InfoRow(
                            label = name,
                            value = "${metric.value.toInt()} ${metric.unit}"
                        )
                    }
                }
            }
            
            // 性能建议
            UnifyCard {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "💡 性能建议",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    val suggestions = getPerformanceSuggestions(performanceSummary)
                    suggestions.forEach { suggestion ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "• ",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = suggestion,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
            
            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UnifyButton(
                    onClick = {
                        // 清理内存
                        System.gc()
                    },
                    text = "清理内存",
                    modifier = Modifier.weight(1f)
                )
                
                UnifyButton(
                    onClick = {
                        // 重置指标
                        UnifyPerformanceMonitor.initialize()
                    },
                    text = "重置指标",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PerformanceMetricCard(
    title: String,
    value: String,
    unit: String,
    icon: String
) {
    Card(
        modifier = Modifier.width(120.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun getPerformanceSuggestions(summary: com.unify.core.performance.PerformanceSummary): List<String> {
    val suggestions = mutableListOf<String>()
    
    if (summary.averageFrameRate < 30) {
        suggestions.add("帧率较低，建议优化 UI 重组逻辑")
    }
    
    if (summary.memoryUsage > 100) {
        suggestions.add("内存使用较高，建议清理无用对象")
    }
    
    if (summary.recompositionCount > 50) {
        suggestions.add("重组次数较多，建议使用 remember 优化")
    }
    
    if (suggestions.isEmpty()) {
        suggestions.add("性能表现良好，继续保持！")
    }
    
    return suggestions
}
