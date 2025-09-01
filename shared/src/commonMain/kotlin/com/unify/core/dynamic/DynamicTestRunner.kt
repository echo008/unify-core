package com.unify.core.dynamic

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope

/**
 * 动态化测试运行器
 * 提供测试执行、结果展示和报告生成的完整界面
 */
@Composable
fun DynamicTestRunner(
    testFramework: DynamicTestFramework,
    modifier: Modifier = Modifier
) {
    var isRunning by remember { mutableStateOf(false) }
    var currentTestReport by remember { mutableStateOf<TestReport?>(null) }
    var selectedSuite by remember { mutableStateOf<String?>(null) }
    
    val testSuites = remember {
        listOf(
            testFramework.createComponentTestSuite(),
            testFramework.createHotUpdateTestSuite(),
            testFramework.createConfigurationTestSuite(),
            testFramework.createStorageTestSuite(),
            testFramework.createPerformanceTestSuite(),
            testFramework.createSecurityTestSuite()
        )
    }
    
    Column(modifier = modifier.fillMaxSize()) {
        // 顶部控制栏
        TestControlBar(
            isRunning = isRunning,
            onRunAllTests = {
                isRunning = true
                GlobalScope.launch {
                    try {
                        // 注册所有测试套件
                        testSuites.forEach { suite ->
                            testFramework.registerTestSuite(suite)
                        }
                        
                        // 运行所有测试
                        val report = testFramework.runAllTests()
                        currentTestReport = report
                    } finally {
                        isRunning = false
                    }
                }
            },
            onRunSuite = { suiteName ->
                isRunning = true
                selectedSuite = suiteName
                GlobalScope.launch {
                    try {
                        val suite = testSuites.find { it.name == suiteName }
                        if (suite != null) {
                            testFramework.registerTestSuite(suite)
                            val results = testFramework.runTestSuite(suite)
                            currentTestReport = TestReport(
                                totalTests = results.size,
                                passedTests = results.count { it.status == TestStatus.PASSED },
                                failedTests = results.count { it.status == TestStatus.FAILED },
                                skippedTests = results.count { it.status == TestStatus.SKIPPED },
                                executionTime = results.sumOf { it.executionTime },
                                results = results,
                                timestamp = System.currentTimeMillis()
                            )
                        }
                    } finally {
                        isRunning = false
                        selectedSuite = null
                    }
                }
            }
        )
        
        Divider()
        
        Row(modifier = Modifier.fillMaxSize()) {
            // 左侧测试套件列表
            TestSuiteList(
                testSuites = testSuites,
                selectedSuite = selectedSuite,
                onSuiteSelected = { selectedSuite = it },
                modifier = Modifier.weight(0.3f)
            )
            
            VerticalDivider()
            
            // 右侧测试结果
            TestResultsPanel(
                testReport = currentTestReport,
                isRunning = isRunning,
                modifier = Modifier.weight(0.7f)
            )
        }
    }
}

/**
 * 测试控制栏
 */
@Composable
private fun TestControlBar(
    isRunning: Boolean,
    onRunAllTests: () -> Unit,
    onRunSuite: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "动态化测试运行器",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onRunAllTests,
                    enabled = !isRunning
                ) {
                    if (isRunning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("运行所有测试")
                }
                
                OutlinedButton(
                    onClick = { /* 清理测试结果 */ },
                    enabled = !isRunning
                ) {
                    Icon(Icons.Default.Clear, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("清理结果")
                }
            }
        }
    }
}

/**
 * 测试套件列表
 */
@Composable
private fun TestSuiteList(
    testSuites: List<TestSuite>,
    selectedSuite: String?,
    onSuiteSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxHeight()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = "测试套件",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(testSuites) { suite ->
                TestSuiteCard(
                    suite = suite,
                    isSelected = selectedSuite == suite.name,
                    onSelected = { onSuiteSelected(suite.name) }
                )
            }
        }
    }
}

/**
 * 测试套件卡片
 */
@Composable
private fun TestSuiteCard(
    suite: TestSuite,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        onClick = onSelected
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = suite.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = suite.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${suite.tests.size} 个测试",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 测试结果面板
 */
@Composable
private fun TestResultsPanel(
    testReport: TestReport?,
    isRunning: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxHeight()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = "测试结果",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
        }
        
        if (isRunning) {
            // 运行中状态
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "正在运行测试...",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        } else if (testReport != null) {
            // 显示测试结果
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    TestSummaryCard(testReport)
                }
                
                items(testReport.results) { result ->
                    TestResultCard(result)
                }
            }
        } else {
            // 空状态
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Science,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "选择测试套件开始测试",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * 测试摘要卡片
 */
@Composable
private fun TestSummaryCard(report: TestReport) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "测试摘要",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(
                    label = "总计",
                    value = report.totalTests.toString(),
                    color = MaterialTheme.colorScheme.primary
                )
                SummaryItem(
                    label = "通过",
                    value = report.passedTests.toString(),
                    color = Color.Green
                )
                SummaryItem(
                    label = "失败",
                    value = report.failedTests.toString(),
                    color = Color.Red
                )
                SummaryItem(
                    label = "跳过",
                    value = report.skippedTests.toString(),
                    color = Color.Orange
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "成功率: ${String.format("%.1f", report.successRate)}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (report.successRate >= 90) Color.Green else if (report.successRate >= 70) Color.Orange else Color.Red
                )
                Text(
                    text = "执行时间: ${report.executionTime}ms",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = (report.passedTests.toFloat() / report.totalTests),
                modifier = Modifier.fillMaxWidth(),
                color = if (report.successRate >= 90) Color.Green else if (report.successRate >= 70) Color.Orange else Color.Red
            )
        }
    }
}

/**
 * 摘要项
 */
@Composable
private fun SummaryItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 测试结果卡片
 */
@Composable
private fun TestResultCard(result: TestResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (result.status) {
                TestStatus.PASSED -> MaterialTheme.colorScheme.surfaceVariant
                TestStatus.FAILED -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                TestStatus.SKIPPED -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.1f)
            }
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (result.status) {
                            TestStatus.PASSED -> Icons.Default.CheckCircle
                            TestStatus.FAILED -> Icons.Default.Error
                            TestStatus.SKIPPED -> Icons.Default.Warning
                        },
                        contentDescription = null,
                        tint = when (result.status) {
                            TestStatus.PASSED -> Color.Green
                            TestStatus.FAILED -> Color.Red
                            TestStatus.SKIPPED -> Color.Orange
                        },
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = result.testName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Text(
                    text = "${result.executionTime}ms",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = result.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (result.error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Text(
                        text = result.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(8.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

/**
 * 垂直分割线
 */
@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(1.dp)
    ) {
        Divider(
            modifier = Modifier.fillMaxHeight(),
            color = MaterialTheme.colorScheme.outline
        )
    }
}
