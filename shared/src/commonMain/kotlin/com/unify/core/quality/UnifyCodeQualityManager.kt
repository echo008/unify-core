package com.unify.core.quality

import com.unify.core.platform.getCurrentTimeMillis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

/**
 * Unify代码质量管理器
 * 提供跨平台的代码质量检查、分析和改进建议
 */
class UnifyCodeQualityManager {
    private val _qualityMetrics = MutableStateFlow(CodeQualityMetrics())
    val qualityMetrics: StateFlow<CodeQualityMetrics> = _qualityMetrics

    private val testCoverageAnalyzer = TestCoverageAnalyzer()

    // 质量标准常量
    companion object {
        private const val EXCELLENT_QUALITY_SCORE = 90.0
        private const val GOOD_QUALITY_SCORE = 80.0
        private const val FAIR_QUALITY_SCORE = 70.0
        private const val POOR_QUALITY_SCORE = 60.0

        // 复杂度阈值
        private const val MAX_CYCLOMATIC_COMPLEXITY = 10
        private const val MAX_METHOD_LENGTH = 50
        private const val MAX_CLASS_LENGTH = 500
        private const val MAX_PARAMETER_COUNT = 5

        // 代码重复阈值
        private const val MAX_DUPLICATION_PERCENTAGE = 5.0
        private const val MIN_DUPLICATION_LINES = 6

        // 测试覆盖率目标
        private const val TARGET_LINE_COVERAGE = 85.0
        private const val TARGET_BRANCH_COVERAGE = 80.0
        private const val TARGET_METHOD_COVERAGE = 90.0

        // 技术债务阈值
        private const val MAX_TECHNICAL_DEBT_RATIO = 5.0
        private const val MAX_MAINTAINABILITY_INDEX = 20.0
    }

    /**
     * 执行全面的代码质量分析
     */
    suspend fun analyzeCodeQuality(codebase: CodebaseInfo): CodeQualityReport {
        val complexityAnalysis = analyzeComplexity(codebase)
        val duplicationAnalysis = analyzeDuplication(codebase)
        val coverageAnalysis = analyzeCoverage(codebase)
        val maintainabilityAnalysis = analyzeMaintainability(codebase)
        val securityAnalysis = analyzeSecurityIssues(codebase)
        val performanceAnalysis = analyzePerformanceIssues(codebase)

        val overallScore =
            calculateOverallQualityScore(
                complexityAnalysis,
                duplicationAnalysis,
                coverageAnalysis,
                maintainabilityAnalysis,
                securityAnalysis,
                performanceAnalysis,
            )

        val report =
            CodeQualityReport(
                overallScore = overallScore,
                complexityAnalysis = complexityAnalysis,
                duplicationAnalysis = duplicationAnalysis,
                coverageAnalysis = coverageAnalysis,
                maintainabilityAnalysis = maintainabilityAnalysis,
                securityAnalysis = securityAnalysis,
                performanceAnalysis = performanceAnalysis,
                recommendations =
                    generateQualityRecommendations(
                        complexityAnalysis,
                        duplicationAnalysis,
                        coverageAnalysis,
                        maintainabilityAnalysis,
                        securityAnalysis,
                        performanceAnalysis,
                    ),
                timestamp = getCurrentTimeMillis(),
            )

        _qualityMetrics.value =
            _qualityMetrics.value.copy(
                latestReport = report,
                totalFiles = codebase.files.size,
                totalLines = codebase.files.sumOf { it.lineCount },
                lastAnalysisTime = getCurrentTimeMillis(),
            )

        return report
    }

    /**
     * 分析代码复杂度
     */
    private fun analyzeComplexity(codebase: CodebaseInfo): ComplexityAnalysis {
        val fileComplexities =
            codebase.files.map { file ->
                FileComplexity(
                    fileName = file.fileName,
                    cyclomaticComplexity = calculateCyclomaticComplexity(file),
                    cognitiveComplexity = calculateCognitiveComplexity(file),
                    methodCount = file.methods.size,
                    averageMethodLength =
                        if (file.methods.isNotEmpty()) {
                            file.methods.sumOf { it.lineCount } / file.methods.size
                        } else {
                            0
                        },
                    maxMethodLength = file.methods.maxOfOrNull { it.lineCount } ?: 0,
                    classCount = file.classes.size,
                    averageClassLength =
                        if (file.classes.isNotEmpty()) {
                            file.classes.sumOf { it.lineCount } / file.classes.size
                        } else {
                            0
                        },
                )
            }

        val totalComplexity = fileComplexities.sumOf { it.cyclomaticComplexity }
        val averageComplexity =
            if (fileComplexities.isNotEmpty()) {
                totalComplexity / fileComplexities.size
            } else {
                0
            }

        val complexFiles =
            fileComplexities.filter {
                it.cyclomaticComplexity > MAX_CYCLOMATIC_COMPLEXITY ||
                    it.maxMethodLength > MAX_METHOD_LENGTH ||
                    it.averageClassLength > MAX_CLASS_LENGTH
            }

        return ComplexityAnalysis(
            totalComplexity = totalComplexity,
            averageComplexity = averageComplexity,
            maxComplexity = fileComplexities.maxOfOrNull { it.cyclomaticComplexity } ?: 0,
            fileComplexities = fileComplexities,
            complexFiles = complexFiles,
            complexityScore = calculateComplexityScore(averageComplexity, complexFiles.size, fileComplexities.size),
        )
    }

    /**
     * 分析代码重复
     */
    private fun analyzeDuplication(codebase: CodebaseInfo): DuplicationAnalysis {
        val duplicatedBlocks = findDuplicatedCodeBlocks(codebase)
        val totalLines = codebase.files.sumOf { it.lineCount }
        val duplicatedLines = duplicatedBlocks.sumOf { it.lineCount }
        val duplicationPercentage =
            if (totalLines > 0) {
                (duplicatedLines.toDouble() / totalLines) * 100
            } else {
                0.0
            }

        return DuplicationAnalysis(
            duplicationPercentage = duplicationPercentage,
            duplicatedLines = duplicatedLines,
            totalLines = totalLines,
            duplicatedBlocks = duplicatedBlocks,
            duplicationScore = calculateDuplicationScore(duplicationPercentage),
        )
    }

    /**
     * 分析测试覆盖率
     */
    private suspend fun analyzeCoverage(codebase: CodebaseInfo): CoverageAnalysis {
        val testResults = extractTestResults(codebase)
        val coverageReport = testCoverageAnalyzer.analyzeCoverage(testResults)

        return CoverageAnalysis(
            lineCoverage = coverageReport.overallCoverage,
            branchCoverage = calculateBranchCoverage(testResults),
            methodCoverage = calculateMethodCoverage(testResults),
            classCoverage = calculateClassCoverage(testResults),
            coverageScore = calculateCoverageScore(coverageReport.overallCoverage),
        )
    }

    /**
     * 分析可维护性
     */
    private fun analyzeMaintainability(codebase: CodebaseInfo): MaintainabilityAnalysis {
        val maintainabilityIndex = calculateMaintainabilityIndex(codebase)
        val technicalDebt = calculateTechnicalDebt(codebase)
        val codeSmells = detectCodeSmells(codebase)

        return MaintainabilityAnalysis(
            maintainabilityIndex = maintainabilityIndex,
            technicalDebtRatio = technicalDebt,
            codeSmells = codeSmells,
            maintainabilityScore = calculateMaintainabilityScore(maintainabilityIndex, technicalDebt, codeSmells.size),
        )
    }

    /**
     * 分析安全问题
     */
    private fun analyzeSecurityIssues(codebase: CodebaseInfo): SecurityAnalysis {
        val vulnerabilities = detectSecurityVulnerabilities(codebase)
        val securityHotspots = detectSecurityHotspots(codebase)

        return SecurityAnalysis(
            vulnerabilities = vulnerabilities,
            securityHotspots = securityHotspots,
            securityScore = calculateSecurityScore(vulnerabilities, securityHotspots),
        )
    }

    /**
     * 分析性能问题
     */
    private fun analyzePerformanceIssues(codebase: CodebaseInfo): PerformanceAnalysis {
        val performanceIssues = detectPerformanceIssues(codebase)
        val memoryLeaks = detectPotentialMemoryLeaks(codebase)

        return PerformanceAnalysis(
            performanceIssues = performanceIssues,
            memoryLeaks = memoryLeaks,
            performanceScore = calculatePerformanceScore(performanceIssues, memoryLeaks),
        )
    }

    /**
     * 计算总体质量分数
     */
    private fun calculateOverallQualityScore(
        complexity: ComplexityAnalysis,
        duplication: DuplicationAnalysis,
        coverage: CoverageAnalysis,
        maintainability: MaintainabilityAnalysis,
        security: SecurityAnalysis,
        performance: PerformanceAnalysis,
    ): Double {
        val weights =
            mapOf(
                "complexity" to 0.20,
                "duplication" to 0.15,
                "coverage" to 0.25,
                "maintainability" to 0.20,
                "security" to 0.15,
                "performance" to 0.05,
            )

        return complexity.complexityScore * weights["complexity"]!! +
            duplication.duplicationScore * weights["duplication"]!! +
            coverage.coverageScore * weights["coverage"]!! +
            maintainability.maintainabilityScore * weights["maintainability"]!! +
            security.securityScore * weights["security"]!! +
            performance.performanceScore * weights["performance"]!!
    }

    /**
     * 生成质量改进建议
     */
    private fun generateQualityRecommendations(
        complexity: ComplexityAnalysis,
        duplication: DuplicationAnalysis,
        coverage: CoverageAnalysis,
        maintainability: MaintainabilityAnalysis,
        security: SecurityAnalysis,
        performance: PerformanceAnalysis,
    ): List<QualityRecommendation> {
        val recommendations = mutableListOf<QualityRecommendation>()

        // 复杂度建议
        if (complexity.averageComplexity > MAX_CYCLOMATIC_COMPLEXITY) {
            recommendations.add(
                QualityRecommendation(
                    type = QualityIssueType.COMPLEXITY,
                    severity = IssueSeverity.HIGH,
                    message = "平均圈复杂度过高 (${complexity.averageComplexity})，建议重构复杂方法",
                    affectedFiles = complexity.complexFiles.map { it.fileName },
                ),
            )
        }

        // 重复代码建议
        if (duplication.duplicationPercentage > MAX_DUPLICATION_PERCENTAGE) {
            recommendations.add(
                QualityRecommendation(
                    type = QualityIssueType.DUPLICATION,
                    severity = IssueSeverity.MEDIUM,
                    message = "代码重复率过高 (${duplication.duplicationPercentage}%)，建议提取公共方法",
                    affectedFiles = duplication.duplicatedBlocks.map { it.fileName },
                ),
            )
        }

        // 测试覆盖率建议
        if (coverage.lineCoverage < TARGET_LINE_COVERAGE) {
            recommendations.add(
                QualityRecommendation(
                    type = QualityIssueType.COVERAGE,
                    severity = IssueSeverity.HIGH,
                    message = "测试覆盖率不足 (${coverage.lineCoverage}%)，需要增加测试用例",
                    affectedFiles = emptyList(),
                ),
            )
        }

        // 安全问题建议
        security.vulnerabilities.forEach { vulnerability ->
            recommendations.add(
                QualityRecommendation(
                    type = QualityIssueType.SECURITY,
                    severity = vulnerability.severity,
                    message = "发现安全漏洞: ${vulnerability.description}",
                    affectedFiles = listOf(vulnerability.fileName),
                ),
            )
        }

        // 性能问题建议
        performance.performanceIssues.forEach { issue ->
            recommendations.add(
                QualityRecommendation(
                    type = QualityIssueType.PERFORMANCE,
                    severity = issue.severity,
                    message = "性能问题: ${issue.description}",
                    affectedFiles = listOf(issue.fileName),
                ),
            )
        }

        return recommendations
    }

    // 辅助计算方法
    private fun calculateCyclomaticComplexity(file: FileInfo): Int {
        return file.methods.sumOf { method ->
            // 简化的圈复杂度计算
            1 + method.conditionalStatements + method.loops + method.switches
        }
    }

    private fun calculateCognitiveComplexity(file: FileInfo): Int {
        return file.methods.sumOf { method ->
            // 简化的认知复杂度计算
            method.conditionalStatements + method.loops * 2 + method.nestedBlocks
        }
    }

    private fun calculateComplexityScore(
        averageComplexity: Int,
        complexFiles: Int,
        totalFiles: Int,
    ): Double {
        val complexityRatio = averageComplexity.toDouble() / MAX_CYCLOMATIC_COMPLEXITY
        val complexFileRatio = if (totalFiles > 0) complexFiles.toDouble() / totalFiles else 0.0
        return maxOf(0.0, 100.0 - (complexityRatio * 50 + complexFileRatio * 50))
    }

    private fun calculateDuplicationScore(duplicationPercentage: Double): Double {
        return maxOf(0.0, 100.0 - (duplicationPercentage * 10))
    }

    private fun calculateCoverageScore(coverage: Double): Double {
        return coverage
    }

    private fun calculateMaintainabilityScore(
        maintainabilityIndex: Double,
        technicalDebt: Double,
        codeSmellsCount: Int,
    ): Double {
        val indexScore = minOf(100.0, maintainabilityIndex * 5)
        val debtPenalty = technicalDebt * 10
        val smellPenalty = codeSmellsCount * 2
        return maxOf(0.0, indexScore - debtPenalty - smellPenalty)
    }

    private fun calculateSecurityScore(
        vulnerabilities: List<SecurityVulnerability>,
        hotspots: List<SecurityHotspot>,
    ): Double {
        val criticalVulns = vulnerabilities.count { it.severity == IssueSeverity.CRITICAL }
        val highVulns = vulnerabilities.count { it.severity == IssueSeverity.HIGH }
        val mediumVulns = vulnerabilities.count { it.severity == IssueSeverity.MEDIUM }

        val penalty = criticalVulns * 30 + highVulns * 20 + mediumVulns * 10 + hotspots.size * 5
        return maxOf(0.0, 100.0 - penalty)
    }

    private fun calculatePerformanceScore(
        issues: List<PerformanceIssue>,
        memoryLeaks: List<MemoryLeak>,
    ): Double {
        val issuesPenalty =
            issues.sumOf { issue ->
                when (issue.severity) {
                    IssueSeverity.CRITICAL -> 25
                    IssueSeverity.HIGH -> 15
                    IssueSeverity.MEDIUM -> 10
                    IssueSeverity.LOW -> 5
                }.toLong()
            }.toInt()
        val memoryPenalty = memoryLeaks.size * 20
        val totalPenalty = issuesPenalty.plus(memoryPenalty)
        return maxOf(0.0, 100.0 - totalPenalty.toDouble())
    }

    // 占位符实现 - 实际项目中需要具体实现
    private fun findDuplicatedCodeBlocks(codebase: CodebaseInfo): List<DuplicatedBlock> = emptyList()

    private fun extractTestResults(codebase: CodebaseInfo): List<TestResult> = emptyList()

    private fun calculateBranchCoverage(testResults: List<TestResult>): Double = 85.0

    private fun calculateMethodCoverage(testResults: List<TestResult>): Double = 90.0

    private fun calculateClassCoverage(testResults: List<TestResult>): Double = 88.0

    private fun calculateMaintainabilityIndex(codebase: CodebaseInfo): Double = 15.0

    private fun calculateTechnicalDebt(codebase: CodebaseInfo): Double = 3.5

    private fun detectCodeSmells(codebase: CodebaseInfo): List<CodeSmell> = emptyList()

    private fun detectSecurityVulnerabilities(codebase: CodebaseInfo): List<SecurityVulnerability> = emptyList()

    private fun detectSecurityHotspots(codebase: CodebaseInfo): List<SecurityHotspot> = emptyList()

    private fun detectPerformanceIssues(codebase: CodebaseInfo): List<PerformanceIssue> = emptyList()

    private fun detectPotentialMemoryLeaks(codebase: CodebaseInfo): List<MemoryLeak> = emptyList()
}

// 数据类定义
@Serializable
data class CodeQualityMetrics(
    val latestReport: CodeQualityReport? = null,
    val totalFiles: Int = 0,
    val totalLines: Int = 0,
    val lastAnalysisTime: Long = 0,
)

@Serializable
data class CodeQualityReport(
    val overallScore: Double,
    val complexityAnalysis: ComplexityAnalysis,
    val duplicationAnalysis: DuplicationAnalysis,
    val coverageAnalysis: CoverageAnalysis,
    val maintainabilityAnalysis: MaintainabilityAnalysis,
    val securityAnalysis: SecurityAnalysis,
    val performanceAnalysis: PerformanceAnalysis,
    val recommendations: List<QualityRecommendation>,
    val timestamp: Long,
)

@Serializable
data class CodebaseInfo(
    val files: List<FileInfo>,
)

@Serializable
data class FileInfo(
    val fileName: String,
    val lineCount: Int,
    val methods: List<MethodInfo>,
    val classes: List<ClassInfo>,
)

@Serializable
data class MethodInfo(
    val name: String,
    val lineCount: Int,
    val conditionalStatements: Int,
    val loops: Int,
    val switches: Int,
    val nestedBlocks: Int,
    val parameters: Int,
)

@Serializable
data class ClassInfo(
    val name: String,
    val lineCount: Int,
    val methodCount: Int,
)

@Serializable
data class ComplexityAnalysis(
    val totalComplexity: Int,
    val averageComplexity: Int,
    val maxComplexity: Int,
    val fileComplexities: List<FileComplexity>,
    val complexFiles: List<FileComplexity>,
    val complexityScore: Double,
)

@Serializable
data class FileComplexity(
    val fileName: String,
    val cyclomaticComplexity: Int,
    val cognitiveComplexity: Int,
    val methodCount: Int,
    val averageMethodLength: Int,
    val maxMethodLength: Int,
    val classCount: Int,
    val averageClassLength: Int,
)

@Serializable
data class DuplicationAnalysis(
    val duplicationPercentage: Double,
    val duplicatedLines: Int,
    val totalLines: Int,
    val duplicatedBlocks: List<DuplicatedBlock>,
    val duplicationScore: Double,
)

@Serializable
data class DuplicatedBlock(
    val fileName: String,
    val startLine: Int,
    val endLine: Int,
    val lineCount: Int,
    val duplicateFiles: List<String>,
)

@Serializable
data class CoverageAnalysis(
    val lineCoverage: Double,
    val branchCoverage: Double,
    val methodCoverage: Double,
    val classCoverage: Double,
    val coverageScore: Double,
)

@Serializable
data class MaintainabilityAnalysis(
    val maintainabilityIndex: Double,
    val technicalDebtRatio: Double,
    val codeSmells: List<CodeSmell>,
    val maintainabilityScore: Double,
)

@Serializable
data class SecurityAnalysis(
    val vulnerabilities: List<SecurityVulnerability>,
    val securityHotspots: List<SecurityHotspot>,
    val securityScore: Double,
)

@Serializable
data class PerformanceAnalysis(
    val performanceIssues: List<PerformanceIssue>,
    val memoryLeaks: List<MemoryLeak>,
    val performanceScore: Double,
)

@Serializable
data class QualityRecommendation(
    val type: QualityIssueType,
    val severity: IssueSeverity,
    val message: String,
    val affectedFiles: List<String>,
)

@Serializable
data class CodeSmell(
    val type: String,
    val fileName: String,
    val lineNumber: Int,
    val description: String,
)

@Serializable
data class SecurityVulnerability(
    val type: String,
    val fileName: String,
    val lineNumber: Int,
    val description: String,
    val severity: IssueSeverity,
)

@Serializable
data class SecurityHotspot(
    val fileName: String,
    val lineNumber: Int,
    val description: String,
)

@Serializable
data class PerformanceIssue(
    val type: String,
    val fileName: String,
    val lineNumber: Int,
    val description: String,
    val severity: IssueSeverity,
)

@Serializable
data class MemoryLeak(
    val fileName: String,
    val lineNumber: Int,
    val description: String,
)

enum class QualityIssueType {
    COMPLEXITY,
    DUPLICATION,
    COVERAGE,
    MAINTAINABILITY,
    SECURITY,
    PERFORMANCE,
}

enum class IssueSeverity {
    CRITICAL,
    HIGH,
    MEDIUM,
    LOW,
}
