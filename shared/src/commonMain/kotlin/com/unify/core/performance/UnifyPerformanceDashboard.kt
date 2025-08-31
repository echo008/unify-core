package com.unify.core.performance

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Unify性能监控仪表板
 * 实时展示系统性能指标、健康状态和优化建议
 */
@Composable
fun UnifyPerformanceDashboard() {
    var selectedTab by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    
    // 实时数据收集
    val realTimeMetrics by UnifyPerformanceMonitor.realTimeMetrics.collectAsState()
    val performanceScore by UnifyPerformanceMonitor.performanceScore.collectAsState()
    var healthCheck by remember { mutableStateOf<PerformanceHealthCheck?>(null) }
    var detailedReport by remember { mutableStateOf<DetailedPerformanceReport?>(null) }
    
    // 定期更新数据
    LaunchedEffect(Unit) {
        while (true) {
            UnifyPerformanceMonitor.updateRealTimeMetrics()
            healthCheck = UnifyPerformanceMonitor.performHealthCheck()
            delay(1000) // 每秒更新
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 标题和总体评分
        DashboardHeader(
            performanceScore = performanceScore,
            healthStatus = healthCheck?.status ?: HealthStatus.GOOD
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 标签页
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("实时监控") },
                icon = { Icon(Icons.Default.Speed, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("健康检查") },
                icon = { Icon(Icons.Default.Health, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("组件分析") },
                icon = { Icon(Icons.Default.Analytics, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("详细报告") },
                icon = { Icon(Icons.Default.Assessment, contentDescription = null) }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 内容区域
        when (selectedTab) {
            0 -> RealTimeMonitoringTab(realTimeMetrics)
            1 -> HealthCheckTab(healthCheck, scope)
            2 -> ComponentAnalysisTab(scope)
            3 -> DetailedReportTab(detailedReport, scope) { detailedReport = it }
        }
    }
}

/**
 * 仪表板头部
 */
@Composable
fun DashboardHeader(
    performanceScore: Float,
    healthStatus: HealthStatus
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (healthStatus) {
                HealthStatus.GOOD -> MaterialTheme.colorScheme.primaryContainer
                HealthStatus.FAIR -> MaterialTheme.colorScheme.secondaryContainer
                HealthStatus.WARNING -> MaterialTheme.colorScheme.tertiaryContainer
                HealthStatus.CRITICAL -> MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Unify性能监控中心",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "实时监控系统性能和健康状态",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${performanceScore.toInt()}",
                    style = MaterialTheme.typography.displayMedium,
                    color = when (healthStatus) {
                        HealthStatus.GOOD -> Color(0xFF4CAF50)
                        HealthStatus.FAIR -> Color(0xFFFF9800)
                        HealthStatus.WARNING -> Color(0xFFFF5722)
                        HealthStatus.CRITICAL -> Color(0xFFF44336)
                    }
                )
                Text(
                    text = "性能评分",
                    style = MaterialTheme.typography.labelMedium
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Surface(
                    color = when (healthStatus) {
                        HealthStatus.GOOD -> Color(0xFF4CAF50)
                        HealthStatus.FAIR -> Color(0xFFFF9800)
                        HealthStatus.WARNING -> Color(0xFFFF5722)
                        HealthStatus.CRITICAL -> Color(0xFFF44336)
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = healthStatus.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * 实时监控标签页
 */
@Composable
fun RealTimeMonitoringTab(realTimeMetrics: RealTimeMetrics) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            // 核心指标卡片
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "帧率",
                    value = "${realTimeMetrics.frameRate.toInt()}",
                    unit = "FPS",
                    icon = Icons.Default.Speed,
                    color = if (realTimeMetrics.frameRate >= 30) Color(0xFF4CAF50) else Color(0xFFFF5722)
                )
                
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "内存",
                    value = "${realTimeMetrics.memoryUsage.toInt()}",
                    unit = "%",
                    icon = Icons.Default.Memory,
                    color = if (realTimeMetrics.memoryUsage < 80) Color(0xFF4CAF50) else Color(0xFFFF5722)
                )
            }
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "CPU",
                    value = "${realTimeMetrics.cpuUsage.toInt()}",
                    unit = "%",
                    icon = Icons.Default.Cpu,
                    color = if (realTimeMetrics.cpuUsage < 70) Color(0xFF4CAF50) else Color(0xFFFF9800)
                )
                
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "网络延迟",
                    value = "${realTimeMetrics.networkLatency.toInt()}",
                    unit = "ms",
                    icon = Icons.Default.NetworkCheck,
                    color = if (realTimeMetrics.networkLatency < 100) Color(0xFF4CAF50) else Color(0xFFFF9800)
                )
            }
        }
        
        item {
            // 活跃组件信息
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Widgets,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "活跃组件",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "当前活跃组件数量: ${realTimeMetrics.activeComponents}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Text(
                        text = "最后更新: ${formatTimestamp(realTimeMetrics.timestamp)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * 健康检查标签页
 */
@Composable
fun HealthCheckTab(
    healthCheck: PerformanceHealthCheck?,
    scope: CoroutineScope
) {
    var isRefreshing by remember { mutableStateOf(false) }
    
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            // 刷新按钮
            Button(
                onClick = {
                    scope.launch {
                        isRefreshing = true
                        delay(1000)
                        isRefreshing = false
                    }
                },
                enabled = !isRefreshing,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isRefreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("执行健康检查")
            }
        }
        
        healthCheck?.let { check ->
            item {
                // 健康状态总览
                HealthStatusCard(healthCheck = check)
            }
            
            if (check.issues.isNotEmpty()) {
                item {
                    Text(
                        text = "发现的问题 (${check.issues.size})",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                items(check.issues) { issue ->
                    PerformanceIssueCard(issue = issue)
                }
            } else {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "系统运行良好，未发现性能问题",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 组件分析标签页
 */
@Composable
fun ComponentAnalysisTab(scope: CoroutineScope) {
    var componentReport by remember { mutableStateOf<ComponentPerformanceReport?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        componentReport = UnifyPerformanceMonitor.getComponentPerformanceReport()
    }
    
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        componentReport = UnifyPerformanceMonitor.getComponentPerformanceReport()
                        isLoading = false
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("刷新组件分析")
            }
        }
        
        componentReport?.let { report ->
            item {
                // 组件统计概览
                ComponentStatsCard(report = report)
            }
            
            if (report.topPerformers.isNotEmpty()) {
                item {
                    Text(
                        text = "性能最佳组件",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(report.topPerformers) { component ->
                            ComponentMetricCard(
                                component = component,
                                isTopPerformer = true
                            )
                        }
                    }
                }
            }
            
            if (report.bottomPerformers.isNotEmpty()) {
                item {
                    Text(
                        text = "需要优化的组件",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(report.bottomPerformers) { component ->
                            ComponentMetricCard(
                                component = component,
                                isTopPerformer = false
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 详细报告标签页
 */
@Composable
fun DetailedReportTab(
    detailedReport: DetailedPerformanceReport?,
    scope: CoroutineScope,
    onReportGenerated: (DetailedPerformanceReport) -> Unit
) {
    var isGenerating by remember { mutableStateOf(false) }
    
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Button(
                onClick = {
                    scope.launch {
                        isGenerating = true
                        val report = UnifyPerformanceMonitor.getDetailedPerformanceReport()
                        onReportGenerated(report)
                        isGenerating = false
                    }
                },
                enabled = !isGenerating,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("生成详细报告")
            }
        }
        
        detailedReport?.let { report ->
            item {
                DetailedReportCard(report = report)
            }
        }
    }
}

/**
 * 指标卡片
 */
@Composable
fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    color = color
                )
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
            }
        }
    }
}

/**
 * 健康状态卡片
 */
@Composable
fun HealthStatusCard(healthCheck: PerformanceHealthCheck) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (healthCheck.status) {
                HealthStatus.GOOD -> MaterialTheme.colorScheme.primaryContainer
                HealthStatus.FAIR -> MaterialTheme.colorScheme.secondaryContainer
                HealthStatus.WARNING -> MaterialTheme.colorScheme.tertiaryContainer
                HealthStatus.CRITICAL -> MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        when (healthCheck.status) {
                            HealthStatus.GOOD -> Icons.Default.CheckCircle
                            HealthStatus.FAIR -> Icons.Default.Info
                            HealthStatus.WARNING -> Icons.Default.Warning
                            HealthStatus.CRITICAL -> Icons.Default.Error
                        },
                        contentDescription = null,
                        tint = when (healthCheck.status) {
                            HealthStatus.GOOD -> Color(0xFF4CAF50)
                            HealthStatus.FAIR -> Color(0xFF2196F3)
                            HealthStatus.WARNING -> Color(0xFFFF9800)
                            HealthStatus.CRITICAL -> Color(0xFFF44336)
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "系统健康状态",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                
                Text(
                    text = "${healthCheck.score.toInt()}/100",
                    style = MaterialTheme.typography.headlineSmall,
                    color = when (healthCheck.status) {
                        HealthStatus.GOOD -> Color(0xFF4CAF50)
                        HealthStatus.FAIR -> Color(0xFF2196F3)
                        HealthStatus.WARNING -> Color(0xFFFF9800)
                        HealthStatus.CRITICAL -> Color(0xFFF44336)
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "状态: ${healthCheck.status.name}",
                style = MaterialTheme.typography.bodyLarge
            )
            
            Text(
                text = "检查时间: ${formatTimestamp(healthCheck.timestamp)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * 性能问题卡片
 */
@Composable
fun PerformanceIssueCard(issue: PerformanceIssue) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (issue.severity) {
                IssueSeverity.LOW -> MaterialTheme.colorScheme.surfaceVariant
                IssueSeverity.MEDIUM -> MaterialTheme.colorScheme.secondaryContainer
                IssueSeverity.HIGH -> MaterialTheme.colorScheme.tertiaryContainer
                IssueSeverity.CRITICAL -> MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    when (issue.severity) {
                        IssueSeverity.LOW -> Icons.Default.Info
                        IssueSeverity.MEDIUM -> Icons.Default.Warning
                        IssueSeverity.HIGH -> Icons.Default.PriorityHigh
                        IssueSeverity.CRITICAL -> Icons.Default.Error
                    },
                    contentDescription = null,
                    tint = when (issue.severity) {
                        IssueSeverity.LOW -> Color(0xFF2196F3)
                        IssueSeverity.MEDIUM -> Color(0xFFFF9800)
                        IssueSeverity.HIGH -> Color(0xFFFF5722)
                        IssueSeverity.CRITICAL -> Color(0xFFF44336)
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = issue.description,
                    style = MaterialTheme.typography.titleSmall
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "建议: ${issue.recommendation}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Surface(
                color = when (issue.severity) {
                    IssueSeverity.LOW -> Color(0xFF2196F3)
                    IssueSeverity.MEDIUM -> Color(0xFFFF9800)
                    IssueSeverity.HIGH -> Color(0xFFFF5722)
                    IssueSeverity.CRITICAL -> Color(0xFFF44336)
                },
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = issue.severity.name,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * 组件统计卡片
 */
@Composable
fun ComponentStatsCard(report: ComponentPerformanceReport) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "组件性能统计",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "总组件数",
                    value = "${report.totalComponents}"
                )
                StatItem(
                    label = "平均渲染时间",
                    value = "${report.averageRenderTime.toInt()}ms"
                )
                StatItem(
                    label = "最佳组件",
                    value = "${report.topPerformers.size}"
                )
            }
        }
    }
}

/**
 * 组件指标卡片
 */
@Composable
fun ComponentMetricCard(
    component: ComponentMetrics,
    isTopPerformer: Boolean
) {
    Card(
        modifier = Modifier.width(200.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isTopPerformer) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = component.componentName,
                style = MaterialTheme.typography.titleSmall
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "渲染: ${component.renderTime.toInt()}ms",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "内存: ${component.memoryUsage.toInt()}MB",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "重组: ${component.recompositionCount}次",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

/**
 * 详细报告卡片
 */
@Composable
fun DetailedReportCard(report: DetailedPerformanceReport) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "详细性能报告",
                style = MaterialTheme.typography.titleLarge
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 性能摘要
            Text(
                text = "性能摘要",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "平均帧率: ${report.summary.averageFrameRate.toInt()}fps",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "内存使用: ${report.summary.memoryUsage.toInt()}MB",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "网络请求: ${report.summary.networkRequestCount}次",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 趋势分析
            Text(
                text = "性能趋势",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "帧率趋势: ${report.trends.frameRateTrend.name}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "内存趋势: ${report.trends.memoryTrend.name}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "CPU趋势: ${report.trends.cpuTrend.name}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "报告生成时间: ${formatTimestamp(report.timestamp)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * 统计项组件
 */
@Composable
fun StatItem(label: String, value: String) {
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
 * 格式化时间戳
 */
private fun formatTimestamp(timestamp: Long): String {
    val currentTime = System.currentTimeMillis()
    val diff = currentTime - timestamp
    
    return when {
        diff < 60000 -> "刚刚"
        diff < 3600000 -> "${diff / 60000}分钟前"
        diff < 86400000 -> "${diff / 3600000}小时前"
        else -> "${diff / 86400000}天前"
    }
}
