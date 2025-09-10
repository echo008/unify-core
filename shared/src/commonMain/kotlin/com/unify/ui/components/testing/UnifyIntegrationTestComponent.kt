package com.unify.ui.components.testing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unify.core.integration.*
import kotlinx.coroutines.launch

enum class TestCategory {
    INITIALIZATION,
    INTEGRATION,
    DATA_FLOW,
    PERFORMANCE,
    ERROR_HANDLING,
    CROSS_PLATFORM,
    BENCHMARK,
    ALL
}

/**
 * 统一集成测试UI组件
 * 基于Compose实现的跨平台测试界面，保证原生性能
 */
@Composable
fun UnifyIntegrationTestComponent(
    testSuite: UnifyIntegrationTestSuite,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val testResults by testSuite.testResults.collectAsState()
    val isRunning by testSuite.isRunning.collectAsState()
    val currentTest by testSuite.currentTest.collectAsState()
    
    var selectedTab by remember { mutableStateOf(0) }
    var testReport by remember { mutableStateOf<IntegrationTestReport?>(null) }
    var benchmarkReport by remember { mutableStateOf<BenchmarkReport?>(null) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 标题栏
        TestHeaderSection(
            isRunning = isRunning,
            currentTest = currentTest,
            onRunTests = {
                scope.launch {
                    val report = testSuite.runFullTestSuite()
                    testReport = report
                }
            },
            onRunBenchmarks = {
                scope.launch {
                    // 模拟基准测试套件
                    val benchmarkSuite = com.unify.core.integration.PerformanceBenchmarkSuite()
                    val report = benchmarkSuite.runBenchmarks()
                    benchmarkReport = report
                }
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 标签页
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("集成测试") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("性能基准") }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("测试报告") }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 内容区域
        when (selectedTab) {
            0 -> TestResultsSection(
                testResults = testReport?.results ?: emptyList(),
                isRunning = isRunning
            )
            1 -> BenchmarkTestTab(
                benchmarkReport = benchmarkReport,
                isRunning = isRunning
            )
            2 -> TestReportTab(
                testReport = testReport,
                benchmarkReport = benchmarkReport
            )
        }
    }
}

/**
 * 测试头部区域
 */
@Composable
private fun TestHeaderSection(
    isRunning: Boolean,
    currentTest: String?,
    onRunTests: () -> Unit,
    onRunBenchmarks: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Unify-Core 集成测试套件",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (isRunning) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = currentTest ?: "正在执行测试...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onRunTests,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Run Integration Tests")
                    }
                    
                    OutlinedButton(
                        onClick = onRunBenchmarks,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Run Performance Benchmarks")
                    }
                }
            }
        }
    }
}

/**
 * 集成测试标签页
 */
@Composable
private fun TestResultsSection(
    testResults: List<com.unify.core.integration.TestResult>,
    isRunning: Boolean,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (testResults.isNotEmpty()) {
            // 测试统计
            item {
                TestStatisticsCard(testResults.map { result ->
                    TestResult(
                        testName = result.testName,
                        success = result.success,
                        message = result.message,
                        duration = result.duration
                    )
                })
            }
            
            // 显示测试结果
            items(testResults) { result ->
                TestResultItem(result = TestResult(
                    testName = result.testName,
                    success = result.success,
                    message = result.message,
                    duration = result.duration
                ))
            }
        } else if (!isRunning) {
            item {
                EmptyTestStateCard()
            }
        }
    }
}

/**
 * 性能基准标签页
 */
@Composable
private fun BenchmarkTestTab(
    benchmarkReport: BenchmarkReport?,
    isRunning: Boolean,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        benchmarkReport?.let { report: BenchmarkReport ->
            // 总体性能评分
            item {
                OverallScoreCard(report.overallScore)
            }
            
            // 各项基准测试结果
            items(report.results) { result: BenchmarkResult ->
                BenchmarkResultCard(result)
            }
        } ?: run {
            if (!isRunning) {
                item {
                    EmptyBenchmarkStateCard()
                }
            }
        }
    }
}

/**
 * 测试报告标签页
 */
@Composable
private fun TestReportTab(
    testReport: IntegrationTestReport? = null,
    benchmarkReport: BenchmarkReport? = null,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        testReport?.let { report ->
            item {
                TestReportSummaryCard(report)
            }
        }
        
        benchmarkReport?.let { report ->
            item {
                BenchmarkReportSummaryCard(report)
            }
        }
        
        if (testReport == null && benchmarkReport == null) {
            item {
                EmptyReportStateCard()
            }
        }
    }
}

/**
 * 测试统计卡片
 */
@Composable
private fun TestStatisticsCard(
    testResults: List<TestResult>,
    modifier: Modifier = Modifier
) {
    val totalTests = testResults.size
    val passedTests = testResults.count { it.success }
    val failedTests = totalTests - passedTests
    val passRate = if (totalTests > 0) (passedTests * 100.0 / totalTests) else 0.0
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (passRate >= 90.0) {
                Color(0xFF4CAF50).copy(alpha = 0.1f)
            } else if (passRate >= 70.0) {
                Color(0xFFFF9800).copy(alpha = 0.1f)
            } else {
                Color(0xFFF44336).copy(alpha = 0.1f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "测试统计",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatisticItem("总数", totalTests.toString(), Color.Gray)
                StatisticItem("通过", passedTests.toString(), Color(0xFF4CAF50))
                StatisticItem("失败", failedTests.toString(), Color(0xFFF44336))
                StatisticItem("通过率", com.unify.core.utils.UnifyStringUtils.format("%.1f%%", passRate), 
                    if (passRate >= 90.0) Color(0xFF4CAF50) else Color(0xFFFF9800))
            }
        }
    }
}

/**
 * 统计项目
 */
@Composable
private fun StatisticItem(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 测试类别区域
 */
@Composable
private fun TestCategorySection(
    category: TestCategory,
    results: List<TestResult>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = getCategoryDisplayName(category),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            results.forEach { result ->
                TestResultItem(result = result)
                if (result != results.last()) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

/**
 * 测试结果项目
 */
@Composable
private fun TestResultItem(
    result: TestResult,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = if (result.success) {
                    Color(0xFF4CAF50).copy(alpha = 0.1f)
                } else {
                    Color(0xFFF44336).copy(alpha = 0.1f)
                },
                shape = RoundedCornerShape(4.dp)
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 状态指示器
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = if (result.success) Color(0xFF4CAF50) else Color(0xFFF44336),
                    shape = RoundedCornerShape(4.dp)
                )
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // 测试信息
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = result.testName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = result.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // 执行时间
        Text(
            text = "${result.duration}ms",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 总体评分卡片
 */
@Composable
private fun OverallScoreCard(
    score: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                score >= 90.0 -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                score >= 70.0 -> Color(0xFFFF9800).copy(alpha = 0.1f)
                else -> Color(0xFFF44336).copy(alpha = 0.1f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "总体性能评分",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = com.unify.core.utils.UnifyStringUtils.format("%.1f/100", score),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = when {
                    score >= 90.0 -> Color(0xFF4CAF50)
                    score >= 70.0 -> Color(0xFFFF9800)
                    else -> Color(0xFFF44336)
                }
            )
            
            Text(
                text = when {
                    score >= 90.0 -> "优秀"
                    score >= 70.0 -> "良好"
                    score >= 50.0 -> "一般"
                    else -> "需要优化"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 基准测试结果卡片
 */
@Composable
private fun BenchmarkResultCard(
    result: BenchmarkResult,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
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
                    text = result.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = com.unify.core.utils.UnifyStringUtils.format("%.1f/100", result.score),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        result.score >= 90.0 -> Color(0xFF4CAF50)
                        result.score >= 70.0 -> Color(0xFFFF9800)
                        else -> Color(0xFFF44336)
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BenchmarkMetric(
                    label = "平均延迟",
                    value = com.unify.core.utils.UnifyStringUtils.format("测试结果: %.2fms", result.score)
                )
                BenchmarkMetric(
                    label = "吞吐量",
                    value = com.unify.core.utils.UnifyStringUtils.format("网络延迟: %.1fms", result.score)
                )
                BenchmarkMetric(
                    label = "内存使用",
                    value = "${result.memoryUsage / 1024}KB"
                )
                BenchmarkMetric(
                    label = "数据库连接",
                    value = com.unify.core.utils.UnifyStringUtils.format("数据库连接: %.0fms", result.score)
                )
            }
        }
    }
}

/**
 * 基准测试指标
 */
@Composable
private fun BenchmarkMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 测试报告摘要卡片
 */
@Composable
private fun TestReportSummaryCard(
    report: IntegrationTestReport,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "集成测试报告",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = report.summary,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp
            )
        }
    }
}

/**
 * 基准测试报告摘要卡片
 */
@Composable
private fun BenchmarkReportSummaryCard(
    report: BenchmarkReport,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "性能基准报告",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = com.unify.core.utils.UnifyStringUtils.format("总体性能评分: %.1f/100", report.overallScore),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "共完成 ${report.results.size} 项基准测试",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 空状态卡片
 */
@Composable
private fun EmptyTestStateCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "尚未运行测试",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "点击运行集成测试开始测试",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyBenchmarkStateCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "尚未运行基准测试",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "点击运行性能基准开始测试",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyReportStateCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "暂无测试报告",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "运行测试后查看详细报告",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 获取测试类别显示名称
 */
private fun getCategoryDisplayName(category: TestCategory): String {
    return when (category) {
        TestCategory.INITIALIZATION -> "初始化测试"
        TestCategory.INTEGRATION -> "集成测试"
        TestCategory.DATA_FLOW -> "数据流测试"
        TestCategory.PERFORMANCE -> "性能测试"
        TestCategory.ERROR_HANDLING -> "错误处理测试"
        TestCategory.CROSS_PLATFORM -> "跨平台测试"
        TestCategory.BENCHMARK -> "基准测试"
        TestCategory.ALL -> "全部测试"
    }
}
