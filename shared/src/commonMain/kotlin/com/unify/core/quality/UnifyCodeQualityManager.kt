package com.unify.core.quality

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

/**
 * Unify代码质量管理器
 * 提供代码质量检查、评分和改进建议
 */
class UnifyCodeQualityManager {
    private val _qualityMetrics = MutableStateFlow<Map<String, QualityMetric>>(emptyMap())
    val qualityMetrics: StateFlow<Map<String, QualityMetric>> = _qualityMetrics
    
    private val _overallScore = MutableStateFlow(0.0f)
    val overallScore: StateFlow<Float> = _overallScore
    
    private val qualityRules = mutableListOf<QualityRule>()
    private val codeAnalyzers = mutableMapOf<String, CodeAnalyzer>()
    
    /**
     * 初始化质量管理器
     */
    fun initialize() {
        setupDefaultRules()
        initializeAnalyzers()
        calculateInitialScore()
    }
    
    /**
     * 分析代码质量
     */
    fun analyzeCodeQuality(codeFiles: List<CodeFile>): QualityReport {
        val metrics = mutableMapOf<String, QualityMetric>()
        val violations = mutableListOf<QualityViolation>()
        
        codeFiles.forEach { file ->
            val fileMetrics = analyzeFile(file)
            metrics.putAll(fileMetrics)
            
            val fileViolations = checkRuleViolations(file)
            violations.addAll(fileViolations)
        }
        
        val overallScore = calculateOverallScore(metrics.values.toList())
        _qualityMetrics.value = metrics
        _overallScore.value = overallScore
        
        return QualityReport(
            overallScore = overallScore,
            metrics = metrics,
            violations = violations,
            suggestions = generateImprovementSuggestions(violations),
            timestamp = System.currentTimeMillis()
        )
    }
    
    /**
     * 分析单个文件
     */
    private fun analyzeFile(file: CodeFile): Map<String, QualityMetric> {
        val metrics = mutableMapOf<String, QualityMetric>()
        
        // 代码复杂度分析
        val complexity = calculateComplexity(file.content)
        metrics["complexity_${file.name}"] = QualityMetric(
            name = "complexity",
            value = complexity.toDouble(),
            threshold = 10.0,
            category = MetricCategory.COMPLEXITY
        )
        
        // 代码重复分析
        val duplication = calculateDuplication(file.content)
        metrics["duplication_${file.name}"] = QualityMetric(
            name = "duplication",
            value = duplication,
            threshold = 5.0,
            category = MetricCategory.DUPLICATION
        )
        
        // 测试覆盖率分析
        val coverage = calculateTestCoverage(file)
        metrics["coverage_${file.name}"] = QualityMetric(
            name = "coverage",
            value = coverage,
            threshold = 80.0,
            category = MetricCategory.COVERAGE
        )
        
        return metrics
    }
    
    /**
     * 检查规则违规
     */
    private fun checkRuleViolations(file: CodeFile): List<QualityViolation> {
        val violations = mutableListOf<QualityViolation>()
        
        qualityRules.forEach { rule ->
            val ruleViolations = rule.check(file)
            violations.addAll(ruleViolations)
        }
        
        return violations
    }
    
    /**
     * 计算整体评分
     */
    private fun calculateOverallScore(metrics: List<QualityMetric>): Float {
        if (metrics.isEmpty()) return 0.0f
        
        val categoryScores = mutableMapOf<MetricCategory, Float>()
        val categoryCounts = mutableMapOf<MetricCategory, Int>()
        
        metrics.forEach { metric ->
            val score = if (metric.value <= metric.threshold) {
                1.0f - (metric.value / metric.threshold).toFloat()
            } else {
                0.0f
            }
            
            categoryScores[metric.category] = (categoryScores[metric.category] ?: 0.0f) + score
            categoryCounts[metric.category] = (categoryCounts[metric.category] ?: 0) + 1
        }
        
        val averageScores = categoryScores.map { (category, totalScore) ->
            val count = categoryCounts[category] ?: 1
            totalScore / count
        }
        
        return (averageScores.sum() / averageScores.size * 10).coerceIn(0.0f, 10.0f)
    }
    
    /**
     * 生成改进建议
     */
    private fun generateImprovementSuggestions(violations: List<QualityViolation>): List<ImprovementSuggestion> {
        val suggestions = mutableListOf<ImprovementSuggestion>()
        
        val violationGroups = violations.groupBy { it.ruleId }
        
        violationGroups.forEach { (ruleId, ruleViolations) ->
            when (ruleId) {
                "high_complexity" -> suggestions.add(
                    ImprovementSuggestion(
                        type = "refactoring",
                        description = "将复杂方法拆分为更小的函数",
                        priority = SuggestionPriority.HIGH,
                        estimatedEffort = "2-4小时",
                        affectedFiles = ruleViolations.map { it.fileName }.distinct()
                    )
                )
                "code_duplication" -> suggestions.add(
                    ImprovementSuggestion(
                        type = "refactoring",
                        description = "提取重复代码到公共函数",
                        priority = SuggestionPriority.MEDIUM,
                        estimatedEffort = "1-2小时",
                        affectedFiles = ruleViolations.map { it.fileName }.distinct()
                    )
                )
                "low_test_coverage" -> suggestions.add(
                    ImprovementSuggestion(
                        type = "testing",
                        description = "增加单元测试覆盖率",
                        priority = SuggestionPriority.HIGH,
                        estimatedEffort = "4-8小时",
                        affectedFiles = ruleViolations.map { it.fileName }.distinct()
                    )
                )
            }
        }
        
        return suggestions
    }
    
    private fun setupDefaultRules() {
        qualityRules.addAll(listOf(
            ComplexityRule(),
            DuplicationRule(),
            NamingConventionRule(),
            TestCoverageRule(),
            DocumentationRule()
        ))
    }
    
    private fun initializeAnalyzers() {
        codeAnalyzers["kotlin"] = KotlinAnalyzer()
        codeAnalyzers["compose"] = ComposeAnalyzer()
    }
    
    private fun calculateInitialScore() {
        _overallScore.value = 8.5f // 初始评分
    }
    
    private fun calculateComplexity(content: String): Int {
        // 简化的复杂度计算
        val cyclomaticKeywords = listOf("if", "else", "when", "while", "for", "try", "catch")
        return cyclomaticKeywords.sumOf { keyword ->
            content.split(keyword).size - 1
        } + 1
    }
    
    private fun calculateDuplication(content: String): Double {
        // 简化的重复度计算
        val lines = content.lines().filter { it.trim().isNotEmpty() }
        val uniqueLines = lines.distinct()
        return if (lines.isEmpty()) 0.0 else ((lines.size - uniqueLines.size).toDouble() / lines.size * 100)
    }
    
    private fun calculateTestCoverage(file: CodeFile): Double {
        // 模拟测试覆盖率计算
        return when {
            file.name.contains("Test") -> 95.0
            file.content.contains("@Test") -> 85.0
            else -> 70.0
        }
    }
}

@Serializable
data class QualityMetric(
    val name: String,
    val value: Double,
    val threshold: Double,
    val category: MetricCategory
)

@Serializable
data class QualityReport(
    val overallScore: Float,
    val metrics: Map<String, QualityMetric>,
    val violations: List<QualityViolation>,
    val suggestions: List<ImprovementSuggestion>,
    val timestamp: Long
)

@Serializable
data class QualityViolation(
    val ruleId: String,
    val severity: ViolationSeverity,
    val message: String,
    val fileName: String,
    val lineNumber: Int,
    val column: Int = 0
)

@Serializable
data class ImprovementSuggestion(
    val type: String,
    val description: String,
    val priority: SuggestionPriority,
    val estimatedEffort: String,
    val affectedFiles: List<String>
)

@Serializable
data class CodeFile(
    val name: String,
    val path: String,
    val content: String,
    val language: String = "kotlin"
)

enum class MetricCategory {
    COMPLEXITY, DUPLICATION, COVERAGE, MAINTAINABILITY, PERFORMANCE
}

enum class ViolationSeverity {
    INFO, WARNING, ERROR, CRITICAL
}

enum class SuggestionPriority {
    LOW, MEDIUM, HIGH, CRITICAL
}

// 质量规则接口和实现
interface QualityRule {
    val id: String
    fun check(file: CodeFile): List<QualityViolation>
}

class ComplexityRule : QualityRule {
    override val id = "high_complexity"
    
    override fun check(file: CodeFile): List<QualityViolation> {
        val violations = mutableListOf<QualityViolation>()
        val lines = file.content.lines()
        
        lines.forEachIndexed { index, line ->
            val complexity = calculateLineComplexity(line)
            if (complexity > 5) {
                violations.add(QualityViolation(
                    ruleId = id,
                    severity = ViolationSeverity.WARNING,
                    message = "方法复杂度过高: $complexity",
                    fileName = file.name,
                    lineNumber = index + 1
                ))
            }
        }
        
        return violations
    }
    
    private fun calculateLineComplexity(line: String): Int {
        val complexityKeywords = listOf("if", "else", "when", "while", "for")
        return complexityKeywords.count { line.contains(it) }
    }
}

class DuplicationRule : QualityRule {
    override val id = "code_duplication"
    
    override fun check(file: CodeFile): List<QualityViolation> {
        val violations = mutableListOf<QualityViolation>()
        val lines = file.content.lines()
        val lineGroups = lines.groupBy { it.trim() }
        
        lineGroups.forEach { (line, occurrences) ->
            if (occurrences.size > 3 && line.length > 20) {
                violations.add(QualityViolation(
                    ruleId = id,
                    severity = ViolationSeverity.WARNING,
                    message = "检测到重复代码: ${occurrences.size}次重复",
                    fileName = file.name,
                    lineNumber = lines.indexOf(line) + 1
                ))
            }
        }
        
        return violations
    }
}

class NamingConventionRule : QualityRule {
    override val id = "naming_convention"
    
    override fun check(file: CodeFile): List<QualityViolation> {
        val violations = mutableListOf<QualityViolation>()
        val lines = file.content.lines()
        
        lines.forEachIndexed { index, line ->
            // 检查函数命名
            val functionRegex = Regex("fun\\s+([a-zA-Z_][a-zA-Z0-9_]*)")
            functionRegex.findAll(line).forEach { match ->
                val functionName = match.groupValues[1]
                if (!functionName.matches(Regex("^[a-z][a-zA-Z0-9]*$"))) {
                    violations.add(QualityViolation(
                        ruleId = id,
                        severity = ViolationSeverity.INFO,
                        message = "函数名不符合驼峰命名规范: $functionName",
                        fileName = file.name,
                        lineNumber = index + 1
                    ))
                }
            }
        }
        
        return violations
    }
}

class TestCoverageRule : QualityRule {
    override val id = "low_test_coverage"
    
    override fun check(file: CodeFile): List<QualityViolation> {
        val violations = mutableListOf<QualityViolation>()
        
        if (!file.name.contains("Test") && !file.content.contains("@Test")) {
            val functionCount = file.content.split("fun ").size - 1
            if (functionCount > 5) {
                violations.add(QualityViolation(
                    ruleId = id,
                    severity = ViolationSeverity.WARNING,
                    message = "文件缺少测试覆盖，包含${functionCount}个函数",
                    fileName = file.name,
                    lineNumber = 1
                ))
            }
        }
        
        return violations
    }
}

class DocumentationRule : QualityRule {
    override val id = "missing_documentation"
    
    override fun check(file: CodeFile): List<QualityViolation> {
        val violations = mutableListOf<QualityViolation>()
        val lines = file.content.lines()
        
        lines.forEachIndexed { index, line ->
            if (line.trim().startsWith("class ") || line.trim().startsWith("fun ")) {
                val previousLine = if (index > 0) lines[index - 1].trim() else ""
                if (!previousLine.startsWith("/**") && !previousLine.startsWith("//")) {
                    violations.add(QualityViolation(
                        ruleId = id,
                        severity = ViolationSeverity.INFO,
                        message = "缺少文档注释",
                        fileName = file.name,
                        lineNumber = index + 1
                    ))
                }
            }
        }
        
        return violations
    }
}

// 代码分析器
interface CodeAnalyzer {
    fun analyze(file: CodeFile): AnalysisResult
}

class KotlinAnalyzer : CodeAnalyzer {
    override fun analyze(file: CodeFile): AnalysisResult {
        return AnalysisResult(
            language = "Kotlin",
            linesOfCode = file.content.lines().size,
            functions = countFunctions(file.content),
            classes = countClasses(file.content),
            complexity = calculateComplexity(file.content)
        )
    }
    
    private fun countFunctions(content: String): Int = content.split("fun ").size - 1
    private fun countClasses(content: String): Int = content.split("class ").size - 1
    private fun calculateComplexity(content: String): Int = content.split("if ").size - 1
}

class ComposeAnalyzer : CodeAnalyzer {
    override fun analyze(file: CodeFile): AnalysisResult {
        return AnalysisResult(
            language = "Compose",
            linesOfCode = file.content.lines().size,
            functions = countComposables(file.content),
            classes = 0,
            complexity = calculateComposeComplexity(file.content)
        )
    }
    
    private fun countComposables(content: String): Int = content.split("@Composable").size - 1
    private fun calculateComposeComplexity(content: String): Int {
        return listOf("LazyColumn", "LazyRow", "remember", "derivedStateOf").sumOf { keyword ->
            content.split(keyword).size - 1
        }
    }
}

@Serializable
data class AnalysisResult(
    val language: String,
    val linesOfCode: Int,
    val functions: Int,
    val classes: Int,
    val complexity: Int
)
