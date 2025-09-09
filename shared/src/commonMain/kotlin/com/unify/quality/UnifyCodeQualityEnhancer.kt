package com.unify.quality

import com.unify.core.platform.getCurrentTimeMillis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

/**
 * 代码质量增强器
 * 提供代码质量分析、检查和改进建议
 */
class UnifyCodeQualityEnhancer {
    private val _qualityState = MutableStateFlow(QualityState())
    val qualityState: StateFlow<QualityState> = _qualityState

    private val codeAnalyzer = CodeAnalyzer()
    private val qualityChecker = QualityChecker()
    private val metricsCalculator = MetricsCalculator()

    companion object {
        private const val MIN_QUALITY_SCORE = 70
        private const val GOOD_QUALITY_SCORE = 85
        private const val EXCELLENT_QUALITY_SCORE = 95
        private const val MAX_COMPLEXITY_THRESHOLD = 10
        private const val MIN_COVERAGE_THRESHOLD = 80.0
    }

    suspend fun initialize(config: QualityConfig = QualityConfig()): QualityResult {
        return try {
            _qualityState.value =
                _qualityState.value.copy(
                    isInitializing = true,
                    config = config,
                )

            codeAnalyzer.initialize(config.analyzerConfig)
            qualityChecker.initialize(config.checkerConfig)
            metricsCalculator.initialize(config.metricsConfig)

            _qualityState.value =
                _qualityState.value.copy(
                    isInitializing = false,
                    isInitialized = true,
                    initTime = getCurrentTimeMillis(),
                )

            QualityResult.Success("代码质量增强器初始化成功")
        } catch (e: Exception) {
            _qualityState.value =
                _qualityState.value.copy(
                    isInitializing = false,
                    initError = "初始化失败: ${e.message}",
                )
            QualityResult.Error("初始化失败: ${e.message}")
        }
    }

    suspend fun analyzeCode(codeSource: CodeSource): CodeAnalysisResult {
        return try {
            if (!_qualityState.value.isInitialized) {
                return CodeAnalysisResult(
                    source = codeSource,
                    qualityScore = 0,
                    issues =
                        listOf(
                            QualityIssue(
                                type = IssueType.SYSTEM_ERROR,
                                severity = IssueSeverity.HIGH,
                                message = "质量增强器未初始化",
                                location = CodeLocation("", 0, 0),
                            ),
                        ),
                    metrics = CodeMetrics(),
                    suggestions = emptyList(),
                )
            }

            val issues = qualityChecker.checkCode(codeSource)
            val metrics = metricsCalculator.calculateMetrics(codeSource)
            val qualityScore = calculateQualityScore(issues, metrics)
            val suggestions = generateSuggestions(issues, metrics)

            CodeAnalysisResult(
                source = codeSource,
                qualityScore = qualityScore,
                issues = issues,
                metrics = metrics,
                suggestions = suggestions,
            )
        } catch (e: Exception) {
            CodeAnalysisResult(
                source = codeSource,
                qualityScore = 0,
                issues =
                    listOf(
                        QualityIssue(
                            type = IssueType.SYSTEM_ERROR,
                            severity = IssueSeverity.HIGH,
                            message = "分析失败: ${e.message}",
                            location = CodeLocation("", 0, 0),
                        ),
                    ),
                metrics = CodeMetrics(),
                suggestions = emptyList(),
            )
        }
    }

    suspend fun analyzeProject(projectPath: String): ProjectAnalysisResult {
        return try {
            val sourceFiles = codeAnalyzer.scanProject(projectPath)
            val analysisResults = sourceFiles.map { analyzeCode(it) }

            val overallScore = analysisResults.map { it.qualityScore }.average().toInt()
            val allIssues = analysisResults.flatMap { it.issues }
            val overallMetrics = aggregateMetrics(analysisResults.map { it.metrics })
            val projectSuggestions = generateProjectSuggestions(analysisResults)

            ProjectAnalysisResult(
                projectPath = projectPath,
                overallScore = overallScore,
                fileCount = sourceFiles.size,
                totalIssues = allIssues.size,
                criticalIssues = allIssues.count { it.severity == IssueSeverity.HIGH },
                overallMetrics = overallMetrics,
                fileResults = analysisResults,
                suggestions = projectSuggestions,
            )
        } catch (e: Exception) {
            ProjectAnalysisResult(
                projectPath = projectPath,
                overallScore = 0,
                fileCount = 0,
                totalIssues = 1,
                criticalIssues = 1,
                overallMetrics = CodeMetrics(),
                fileResults = emptyList(),
                suggestions = listOf("项目分析失败: ${e.message}"),
            )
        }
    }

    fun getQualityReport(): QualityReport {
        val state = _qualityState.value
        return QualityReport(
            timestamp = getCurrentTimeMillis(),
            isInitialized = state.isInitialized,
            config = state.config,
            systemStatus = if (state.isInitialized) "运行正常" else "未初始化",
            recommendations = getSystemRecommendations(),
        )
    }

    private fun calculateQualityScore(
        issues: List<QualityIssue>,
        metrics: CodeMetrics,
    ): Int {
        var score = 100

        // 根据问题严重程度扣分
        issues.forEach { issue ->
            when (issue.severity) {
                IssueSeverity.HIGH -> score -= 10
                IssueSeverity.MEDIUM -> score -= 5
                IssueSeverity.LOW -> score -= 2
            }
        }

        // 根据代码复杂度扣分
        if (metrics.cyclomaticComplexity > MAX_COMPLEXITY_THRESHOLD) {
            score -= (metrics.cyclomaticComplexity - MAX_COMPLEXITY_THRESHOLD) * 2
        }

        // 根据测试覆盖率调整分数
        if (metrics.testCoverage < MIN_COVERAGE_THRESHOLD) {
            score -= ((MIN_COVERAGE_THRESHOLD - metrics.testCoverage) * 0.5).toInt()
        }

        return maxOf(0, minOf(100, score))
    }

    private fun generateSuggestions(
        issues: List<QualityIssue>,
        metrics: CodeMetrics,
    ): List<String> {
        val suggestions = mutableListOf<String>()

        if (issues.any { it.type == IssueType.CODE_SMELL }) {
            suggestions.add("发现代码异味，建议重构相关代码")
        }

        if (metrics.cyclomaticComplexity > MAX_COMPLEXITY_THRESHOLD) {
            suggestions.add("代码复杂度过高，建议拆分复杂函数")
        }

        if (metrics.testCoverage < MIN_COVERAGE_THRESHOLD) {
            suggestions.add("测试覆盖率不足，建议增加单元测试")
        }

        if (metrics.duplicatedLines > 50) {
            suggestions.add("发现重复代码，建议提取公共方法")
        }

        return suggestions
    }

    private fun generateProjectSuggestions(results: List<CodeAnalysisResult>): List<String> {
        val suggestions = mutableListOf<String>()

        val avgScore = results.map { it.qualityScore }.average()
        when {
            avgScore < MIN_QUALITY_SCORE -> suggestions.add("项目整体质量需要改进")
            avgScore < GOOD_QUALITY_SCORE -> suggestions.add("项目质量良好，可进一步优化")
            avgScore >= EXCELLENT_QUALITY_SCORE -> suggestions.add("项目质量优秀")
        }

        val highIssueFiles = results.filter { it.issues.any { issue -> issue.severity == IssueSeverity.HIGH } }
        if (highIssueFiles.isNotEmpty()) {
            suggestions.add("${highIssueFiles.size}个文件存在严重问题，需要优先处理")
        }

        return suggestions
    }

    private fun aggregateMetrics(metricsList: List<CodeMetrics>): CodeMetrics {
        if (metricsList.isEmpty()) return CodeMetrics()

        return CodeMetrics(
            linesOfCode = metricsList.sumOf { it.linesOfCode },
            cyclomaticComplexity = metricsList.map { it.cyclomaticComplexity }.average().toInt(),
            testCoverage = metricsList.map { it.testCoverage }.average(),
            duplicatedLines = metricsList.sumOf { it.duplicatedLines },
            maintainabilityIndex = metricsList.map { it.maintainabilityIndex }.average(),
        )
    }

    private fun getSystemRecommendations(): List<String> {
        val recommendations = mutableListOf<String>()
        val state = _qualityState.value

        if (!state.isInitialized) {
            recommendations.add("请先初始化代码质量增强器")
        } else {
            recommendations.add("系统运行正常，可以进行代码质量分析")
        }

        return recommendations
    }
}

// 组件类
class CodeAnalyzer {
    suspend fun initialize(config: AnalyzerConfig) {}

    fun scanProject(projectPath: String): List<CodeSource> {
        // 模拟扫描项目文件
        return listOf(
            CodeSource("MainActivity.kt", "kotlin", "class MainActivity { ... }"),
            CodeSource("Utils.kt", "kotlin", "object Utils { ... }"),
            CodeSource("Repository.kt", "kotlin", "class Repository { ... }"),
        )
    }
}

class QualityChecker {
    suspend fun initialize(config: CheckerConfig) {}

    fun checkCode(codeSource: CodeSource): List<QualityIssue> {
        val issues = mutableListOf<QualityIssue>()

        // 模拟代码检查
        if (codeSource.content.contains("TODO")) {
            issues.add(
                QualityIssue(
                    type = IssueType.CODE_SMELL,
                    severity = IssueSeverity.LOW,
                    message = "发现TODO注释",
                    location = CodeLocation(codeSource.fileName, 1, 1),
                ),
            )
        }

        return issues
    }
}

class MetricsCalculator {
    suspend fun initialize(config: MetricsConfig) {}

    fun calculateMetrics(codeSource: CodeSource): CodeMetrics {
        return CodeMetrics(
            linesOfCode = codeSource.content.lines().size,
            cyclomaticComplexity = (1..15).random(),
            testCoverage = (60..95).random().toDouble(),
            duplicatedLines = (0..100).random(),
            maintainabilityIndex = (50..100).random().toDouble(),
        )
    }
}

// 数据类
@Serializable
data class QualityState(
    val isInitializing: Boolean = false,
    val isInitialized: Boolean = false,
    val config: QualityConfig = QualityConfig(),
    val initTime: Long = 0,
    val initError: String? = null,
)

@Serializable
data class QualityConfig(
    val analyzerConfig: AnalyzerConfig = AnalyzerConfig(),
    val checkerConfig: CheckerConfig = CheckerConfig(),
    val metricsConfig: MetricsConfig = MetricsConfig(),
)

@Serializable
data class AnalyzerConfig(
    val includeTests: Boolean = true,
    val excludePatterns: List<String> = emptyList(),
)

@Serializable
data class CheckerConfig(
    val enableStyleCheck: Boolean = true,
    val enableComplexityCheck: Boolean = true,
    val enableDuplicationCheck: Boolean = true,
)

@Serializable
data class MetricsConfig(
    val calculateComplexity: Boolean = true,
    val calculateCoverage: Boolean = true,
    val calculateMaintainability: Boolean = true,
)

@Serializable
data class CodeSource(
    val fileName: String,
    val language: String,
    val content: String,
)

@Serializable
data class CodeAnalysisResult(
    val source: CodeSource,
    val qualityScore: Int,
    val issues: List<QualityIssue>,
    val metrics: CodeMetrics,
    val suggestions: List<String>,
)

@Serializable
data class ProjectAnalysisResult(
    val projectPath: String,
    val overallScore: Int,
    val fileCount: Int,
    val totalIssues: Int,
    val criticalIssues: Int,
    val overallMetrics: CodeMetrics,
    val fileResults: List<CodeAnalysisResult>,
    val suggestions: List<String>,
)

@Serializable
data class QualityIssue(
    val type: IssueType,
    val severity: IssueSeverity,
    val message: String,
    val location: CodeLocation,
)

@Serializable
data class CodeLocation(
    val fileName: String,
    val line: Int,
    val column: Int,
)

@Serializable
data class CodeMetrics(
    val linesOfCode: Int = 0,
    val cyclomaticComplexity: Int = 0,
    val testCoverage: Double = 0.0,
    val duplicatedLines: Int = 0,
    val maintainabilityIndex: Double = 0.0,
)

@Serializable
data class QualityReport(
    val timestamp: Long,
    val isInitialized: Boolean,
    val config: QualityConfig,
    val systemStatus: String,
    val recommendations: List<String>,
)

enum class IssueType {
    CODE_SMELL,
    BUG,
    VULNERABILITY,
    DUPLICATION,
    COMPLEXITY,
    STYLE,
    SYSTEM_ERROR,
}

enum class IssueSeverity {
    LOW,
    MEDIUM,
    HIGH,
}

sealed class QualityResult {
    data class Success(val message: String) : QualityResult()

    data class Error(val message: String) : QualityResult()
}
