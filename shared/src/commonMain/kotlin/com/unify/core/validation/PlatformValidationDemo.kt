package com.unify.core.validation

import com.unify.core.utils.UnifyPlatformUtils

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/**
 * 平台验证演示应用
 */
@Composable
fun PlatformValidationDemo() {
    var validationReport by remember { mutableStateOf<PlatformValidationReport?>(null) }
    var isValidating by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    Column(modifier = Modifier.fillMaxSize()) {
        // 顶部标题
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🔍 8大平台完整性验证",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Android • iOS • Desktop • Web • HarmonyOS • 小程序 • TV • Watch",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        // 验证控制
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "验证控制",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = {
                        scope.launch {
                            isValidating = true
                            val validator = PlatformValidator()
                            validationReport = validator.validateAllPlatforms()
                            isValidating = false
                        }
                    },
                    enabled = !isValidating,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("开始验证8大平台")
                }
                
                if (isValidating) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Text("正在验证平台完整性...")
                }
            }
        }
        
        // 验证结果
        validationReport?.let { report ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    ValidationSummaryCard(report.summary, report.overallStatus)
                }
                
                items(report.validatedPlatforms) { platform ->
                    PlatformValidationCard(platform)
                }
                
                item {
                    Text("Platform Status")
                }
                
                if (report.recommendations.isNotEmpty()) {
                    item {
                        RecommendationsCard(report.recommendations)
                    }
                }
            }
        }
    }
}

@Composable
private fun ValidationSummaryCard(summary: ValidationSummary, overallStatus: ValidationStatus) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "验证摘要",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = overallStatus.name,
                    color = when (overallStatus) {
                        ValidationStatus.PASSED -> MaterialTheme.colorScheme.primary
                        ValidationStatus.WARNING -> MaterialTheme.colorScheme.tertiary
                        ValidationStatus.FAILED -> MaterialTheme.colorScheme.error
                        ValidationStatus.NOT_TESTED -> MaterialTheme.colorScheme.outline
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("通过: ${summary.passedPlatforms}", color = MaterialTheme.colorScheme.primary)
                Text("警告: ${summary.warningPlatforms}", color = MaterialTheme.colorScheme.tertiary)
                Text("失败: ${summary.failedPlatforms}", color = MaterialTheme.colorScheme.error)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text("组件: ${summary.validComponents}/${summary.totalComponents}")
            Text("CPU使用率: 0.0%")
            Text("内存使用: 0 MB")
            Text("问题: ${summary.totalIssues} (严重: ${summary.criticalIssues})")
            
            LinearProgressIndicator(
                progress = if (summary.totalComponents > 0) (summary.validComponents.toFloat() / summary.totalComponents.toFloat()) else 0f,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PlatformValidationCard(platform: PlatformValidationResult) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Platform Info",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = platform.platformType.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = platform.status.name,
                        color = when (platform.status) {
                            ValidationStatus.PASSED -> MaterialTheme.colorScheme.primary
                            ValidationStatus.WARNING -> MaterialTheme.colorScheme.tertiary
                            ValidationStatus.FAILED -> MaterialTheme.colorScheme.error
                            ValidationStatus.NOT_TESTED -> MaterialTheme.colorScheme.outline
                        }
                    )
                    Text(
                        text = platform.buildStatus.name,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 组件状态
            Text("组件状态:", style = MaterialTheme.typography.bodyMedium)
            platform.components.forEach { component ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = component.componentName,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = if (component.fileExists && component.compilable) "✓" else "✗",
                        color = if (component.fileExists && component.compilable) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 特性支持
            Text("支持特性: ${platform.features.size}")
            Text("覆盖率: ${UnifyPlatformUtils.formatFloat(platform.coverage.toFloat(), 1)}%")
            
            if (platform.issues.isNotEmpty()) {
                Text("问题: ${platform.issues.size}", color = MaterialTheme.colorScheme.error)
            }
            
            LinearProgressIndicator(
                progress = 0.5f, // Placeholder progress value
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun RecommendationsCard(recommendations: List<String>) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "改进建议",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            recommendations.forEach { recommendation ->
                Text(
                    text = "• $recommendation",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text("Recommendations")
        }
    }
}
