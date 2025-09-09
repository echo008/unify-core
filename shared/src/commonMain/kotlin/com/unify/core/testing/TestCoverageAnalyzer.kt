package com.unify.core.testing

import com.unify.core.platform.getCurrentTimeMillis
import com.unify.core.utils.UnifyStringUtils
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * 代码覆盖率数据
 */
@Serializable
data class CoverageData(
    val moduleId: String,
    val moduleName: String,
    val totalLines: Int,
    val coveredLines: Int,
    val coveragePercentage: Double,
    val uncoveredLines: List<Int> = emptyList(),
    val branchCoverage: Double = 0.0,
    val functionCoverage: Double = 0.0,
    val timestamp: Long = getCurrentTimeMillis(),
)

/**
 * 覆盖率报告
 */
@Serializable
data class CoverageReport(
    val id: String,
    val projectName: String,
    val timestamp: Long,
    val overallCoverage: Double,
    val modulesCoverage: List<CoverageData>,
    val summary: CoverageSummary,
    val thresholds: CoverageThresholds,
    val recommendations: List<String> = emptyList(),
)

@Serializable
data class CoverageSummary(
    val totalLines: Int,
    val coveredLines: Int,
    val totalBranches: Int,
    val coveredBranches: Int,
    val totalFunctions: Int,
    val coveredFunctions: Int,
    val lineCoverage: Double,
    val branchCoverage: Double,
    val functionCoverage: Double,
)

@Serializable
data class CoverageThresholds(
    val minLineCoverage: Double = 80.0,
    val minBranchCoverage: Double = 70.0,
    val minFunctionCoverage: Double = 85.0,
    val criticalModules: List<String> = emptyList(),
)

/**
 * 测试覆盖率分析器接口
 */
interface TestCoverageAnalyzer {
    // 覆盖率收集
    suspend fun collectCoverage(moduleId: String): CoverageData?

    suspend fun collectAllCoverage(): List<CoverageData>

    // 覆盖率分析
    suspend fun analyzeCoverage(): CoverageReport

    suspend fun compareWithThresholds(report: CoverageReport): List<String>

    // 报告生成
    suspend fun generateReport(): CoverageReport

    suspend fun exportReport(format: ReportFormat): String

    // 配置管理
    fun updateThresholds(thresholds: CoverageThresholds)

    fun getThresholds(): CoverageThresholds

    // 历史数据
    suspend fun saveCoverageHistory(report: CoverageReport): Boolean

    suspend fun getCoverageHistory(): List<CoverageReport>

    suspend fun getCoverageTrend(moduleId: String): List<Double>
}

/**
 * 测试覆盖率分析器实现
 */
class TestCoverageAnalyzerImpl : TestCoverageAnalyzer {
    private var thresholds = CoverageThresholds()
    private val coverageHistory = mutableListOf<CoverageReport>()
    private val moduleData = mutableMapOf<String, CoverageData>()

    companion object {
        // 模块覆盖率常量
        private const val CORE_MODULE_COVERAGE = 92.5
        private const val UI_MODULE_COVERAGE = 88.3
        private const val PLATFORM_MODULE_COVERAGE = 85.7
        private const val DYNAMIC_MODULE_COVERAGE = 90.1
        private const val TESTING_MODULE_COVERAGE = 95.2
        private const val PERFORMANCE_MODULE_COVERAGE = 87.4
        private const val SECURITY_MODULE_COVERAGE = 91.8
        private const val NETWORK_MODULE_COVERAGE = 89.6

        // 分支覆盖率常量
        private const val DEFAULT_BRANCH_COVERAGE = 75.0
        private const val HIGH_BRANCH_COVERAGE = 85.0

        // 函数覆盖率常量
        private const val DEFAULT_FUNCTION_COVERAGE = 90.0
        private const val HIGH_FUNCTION_COVERAGE = 95.0
    }

    init {
        // 初始化模拟数据
        initializeMockData()
    }

    override suspend fun collectCoverage(moduleId: String): CoverageData? {
        return moduleData[moduleId]
    }

    override suspend fun collectAllCoverage(): List<CoverageData> {
        return moduleData.values.toList()
    }

    override suspend fun analyzeCoverage(): CoverageReport {
        val allCoverage = collectAllCoverage()

        val totalLines = allCoverage.sumOf { it.totalLines }
        val coveredLines = allCoverage.sumOf { it.coveredLines }
        val overallCoverage = if (totalLines > 0) (coveredLines.toDouble() / totalLines) * 100 else 0.0

        // 计算分支和函数覆盖率
        val totalBranches = allCoverage.size * 100 // 模拟数据
        val coveredBranches = (totalBranches * DEFAULT_BRANCH_COVERAGE / 100).toInt()
        val totalFunctions = allCoverage.size * 50 // 模拟数据
        val coveredFunctions = (totalFunctions * DEFAULT_FUNCTION_COVERAGE / 100).toInt()

        val summary =
            CoverageSummary(
                totalLines = totalLines,
                coveredLines = coveredLines,
                totalBranches = totalBranches,
                coveredBranches = coveredBranches,
                totalFunctions = totalFunctions,
                coveredFunctions = coveredFunctions,
                lineCoverage = overallCoverage,
                branchCoverage = DEFAULT_BRANCH_COVERAGE,
                functionCoverage = DEFAULT_FUNCTION_COVERAGE,
            )

        val recommendations = generateRecommendations(allCoverage)

        return CoverageReport(
            id = "coverage_${getCurrentTimeMillis()}",
            projectName = "Unify-Core",
            timestamp = getCurrentTimeMillis(),
            overallCoverage = overallCoverage,
            modulesCoverage = allCoverage,
            summary = summary,
            thresholds = thresholds,
            recommendations = recommendations,
        )
    }

    override suspend fun compareWithThresholds(report: CoverageReport): List<String> {
        val violations = mutableListOf<String>()

        // 检查整体覆盖率
        if (report.summary.lineCoverage < thresholds.minLineCoverage) {
            violations.add("整体行覆盖率 ${UnifyStringUtils.format("%.1f", report.summary.lineCoverage)}% 低于阈值 ${thresholds.minLineCoverage}%")
        }

        if (report.summary.branchCoverage < thresholds.minBranchCoverage) {
            violations.add(
                "整体分支覆盖率 ${UnifyStringUtils.format("%.1f", report.summary.branchCoverage)}% 低于阈值 ${thresholds.minBranchCoverage}%",
            )
        }

        if (report.summary.functionCoverage < thresholds.minFunctionCoverage) {
            violations.add(
                "整体函数覆盖率 ${UnifyStringUtils.format("%.1f", report.summary.functionCoverage)}% 低于阈值 ${thresholds.minFunctionCoverage}%",
            )
        }

        // 检查关键模块
        thresholds.criticalModules.forEach { moduleId ->
            val moduleData = report.modulesCoverage.find { it.moduleId == moduleId }
            if (moduleData != null && moduleData.coveragePercentage < thresholds.minLineCoverage) {
                violations.add("关键模块 $moduleId 覆盖率 ${UnifyStringUtils.format("%.1f", moduleData.coveragePercentage)}% 低于阈值")
            }
        }

        return violations
    }

    override suspend fun generateReport(): CoverageReport {
        return analyzeCoverage()
    }

    override suspend fun exportReport(format: ReportFormat): String {
        val report = generateReport()

        return when (format) {
            ReportFormat.JSON -> Json.encodeToString(report)
            ReportFormat.XML -> exportToXML(report)
            ReportFormat.HTML -> exportToHTML(report)
            ReportFormat.TEXT -> exportToText(report)
        }
    }

    override fun updateThresholds(thresholds: CoverageThresholds) {
        this.thresholds = thresholds
    }

    override fun getThresholds(): CoverageThresholds = thresholds

    override suspend fun saveCoverageHistory(report: CoverageReport): Boolean {
        return try {
            coverageHistory.add(report)

            // 保持历史记录在合理范围内
            if (coverageHistory.size > 50) {
                coverageHistory.removeAt(0)
            }

            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getCoverageHistory(): List<CoverageReport> {
        return coverageHistory.toList()
    }

    override suspend fun getCoverageTrend(moduleId: String): List<Double> {
        return coverageHistory.mapNotNull { report ->
            report.modulesCoverage.find { it.moduleId == moduleId }?.coveragePercentage
        }
    }

    // 私有辅助方法
    private fun initializeMockData() {
        moduleData["core"] =
            CoverageData(
                moduleId = "core",
                moduleName = "核心模块",
                totalLines = 2500,
                coveredLines = (2500 * CORE_MODULE_COVERAGE / 100).toInt(),
                coveragePercentage = CORE_MODULE_COVERAGE,
                branchCoverage = HIGH_BRANCH_COVERAGE,
                functionCoverage = HIGH_FUNCTION_COVERAGE,
            )

        moduleData["ui"] =
            CoverageData(
                moduleId = "ui",
                moduleName = "UI组件模块",
                totalLines = 3200,
                coveredLines = (3200 * UI_MODULE_COVERAGE / 100).toInt(),
                coveragePercentage = UI_MODULE_COVERAGE,
                branchCoverage = DEFAULT_BRANCH_COVERAGE,
                functionCoverage = DEFAULT_FUNCTION_COVERAGE,
            )

        moduleData["platform"] =
            CoverageData(
                moduleId = "platform",
                moduleName = "平台适配模块",
                totalLines = 1800,
                coveredLines = (1800 * PLATFORM_MODULE_COVERAGE / 100).toInt(),
                coveragePercentage = PLATFORM_MODULE_COVERAGE,
                branchCoverage = DEFAULT_BRANCH_COVERAGE,
                functionCoverage = DEFAULT_FUNCTION_COVERAGE,
            )

        moduleData["dynamic"] =
            CoverageData(
                moduleId = "dynamic",
                moduleName = "动态组件模块",
                totalLines = 2100,
                coveredLines = (2100 * DYNAMIC_MODULE_COVERAGE / 100).toInt(),
                coveragePercentage = DYNAMIC_MODULE_COVERAGE,
                branchCoverage = HIGH_BRANCH_COVERAGE,
                functionCoverage = HIGH_FUNCTION_COVERAGE,
            )

        moduleData["testing"] =
            CoverageData(
                moduleId = "testing",
                moduleName = "测试框架模块",
                totalLines = 1500,
                coveredLines = (1500 * TESTING_MODULE_COVERAGE / 100).toInt(),
                coveragePercentage = TESTING_MODULE_COVERAGE,
                branchCoverage = HIGH_BRANCH_COVERAGE,
                functionCoverage = HIGH_FUNCTION_COVERAGE,
            )

        moduleData["performance"] =
            CoverageData(
                moduleId = "performance",
                moduleName = "性能监控模块",
                totalLines = 1200,
                coveredLines = (1200 * PERFORMANCE_MODULE_COVERAGE / 100).toInt(),
                coveragePercentage = PERFORMANCE_MODULE_COVERAGE,
                branchCoverage = DEFAULT_BRANCH_COVERAGE,
                functionCoverage = DEFAULT_FUNCTION_COVERAGE,
            )

        moduleData["security"] =
            CoverageData(
                moduleId = "security",
                moduleName = "安全模块",
                totalLines = 900,
                coveredLines = (900 * SECURITY_MODULE_COVERAGE / 100).toInt(),
                coveragePercentage = SECURITY_MODULE_COVERAGE,
                branchCoverage = HIGH_BRANCH_COVERAGE,
                functionCoverage = HIGH_FUNCTION_COVERAGE,
            )

        moduleData["network"] =
            CoverageData(
                moduleId = "network",
                moduleName = "网络模块",
                totalLines = 800,
                coveredLines = (800 * NETWORK_MODULE_COVERAGE / 100).toInt(),
                coveragePercentage = NETWORK_MODULE_COVERAGE,
                branchCoverage = DEFAULT_BRANCH_COVERAGE,
                functionCoverage = DEFAULT_FUNCTION_COVERAGE,
            )
    }

    private fun generateRecommendations(coverageData: List<CoverageData>): List<String> {
        val recommendations = mutableListOf<String>()

        coverageData.forEach { data ->
            when {
                data.coveragePercentage < 70.0 -> {
                    recommendations.add("${data.moduleName} 覆盖率过低 (${UnifyStringUtils.format("%.1f", data.coveragePercentage)}%)，建议增加单元测试")
                }
                data.coveragePercentage < 80.0 -> {
                    recommendations.add("${data.moduleName} 覆盖率偏低 (${UnifyStringUtils.format("%.1f", data.coveragePercentage)}%)，建议补充边界测试")
                }
                data.branchCoverage < 70.0 -> {
                    recommendations.add("${data.moduleName} 分支覆盖率不足，建议增加条件分支测试")
                }
                data.functionCoverage < 85.0 -> {
                    recommendations.add("${data.moduleName} 函数覆盖率不足，建议测试所有公共方法")
                }
            }
        }

        if (recommendations.isEmpty()) {
            recommendations.add("所有模块覆盖率良好，建议继续保持测试质量")
        }

        return recommendations
    }

    private fun exportToXML(report: CoverageReport): String {
        val modulesXml =
            report.modulesCoverage.joinToString("\n") { module ->
                "<module id=\"${module.moduleId}\" name=\"${module.moduleName}\" coverage=\"${module.coveragePercentage}\" />"
            }

        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <coverageReport>
                <summary>
                    <overallCoverage>${report.overallCoverage}</overallCoverage>
                    <lineCoverage>${report.summary.lineCoverage}</lineCoverage>
                    <branchCoverage>${report.summary.branchCoverage}</branchCoverage>
                    <functionCoverage>${report.summary.functionCoverage}</functionCoverage>
                </summary>
                <modules>
                    $modulesXml
                </modules>
            </coverageReport>
            """.trimIndent()
    }

    private fun exportToHTML(report: CoverageReport): String {
        val overallClass = getCoverageClass(report.overallCoverage)
        val overallCoverage = UnifyStringUtils.format("%.1f", report.overallCoverage)
        val lineCoverage = UnifyStringUtils.format("%.1f", report.summary.lineCoverage)
        val branchCoverage = UnifyStringUtils.format("%.1f", report.summary.branchCoverage)
        val functionCoverage = UnifyStringUtils.format("%.1f", report.summary.functionCoverage)

        val moduleRows =
            report.modulesCoverage.joinToString("\n") { module ->
                val moduleClass = getCoverageClass(module.coveragePercentage)
                val modulePercent = UnifyStringUtils.format("%.1f", module.coveragePercentage)
                val moduleBranch = UnifyStringUtils.format("%.1f", module.branchCoverage)
                "<tr><td>${module.moduleName}</td><td class=\"$moduleClass\">$modulePercent%</td>" +
                    "<td>${module.totalLines}</td><td>${module.coveredLines}</td><td>$moduleBranch%</td></tr>"
            }

        val recommendationsList = report.recommendations.joinToString("\n") { "<li>$it</li>" }

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <title>代码覆盖率报告</title>
                <style>
                    body { font-family: Arial, sans-serif; }
                    .summary { background: #f5f5f5; padding: 15px; margin: 10px 0; border-radius: 5px; }
                    .high { color: green; font-weight: bold; }
                    .medium { color: orange; font-weight: bold; }
                    .low { color: red; font-weight: bold; }
                    table { width: 100%; border-collapse: collapse; margin: 10px 0; }
                    th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
                    th { background-color: #f2f2f2; }
                </style>
            </head>
            <body>
                <h1>代码覆盖率报告</h1>
                <div class="summary">
                    <h2>总体覆盖率</h2>
                    <p class="$overallClass">整体覆盖率: $overallCoverage%</p>
                    <p>行覆盖率: $lineCoverage%</p>
                    <p>分支覆盖率: $branchCoverage%</p>
                    <p>函数覆盖率: $functionCoverage%</p>
                </div>
                
                <h2>模块覆盖率详情</h2>
                <table>
                    <tr><th>模块名称</th><th>覆盖率</th><th>总行数</th><th>覆盖行数</th><th>分支覆盖率</th></tr>
                    $moduleRows
                </table>
                
                <h2>改进建议</h2>
                <ul>
                    $recommendationsList
                </ul>
            </body>
            </html>
            """.trimIndent()
    }

    private fun exportToText(report: CoverageReport): String {
        val overallCoverage = UnifyStringUtils.format("%.1f", report.overallCoverage)
        val lineCoverage = UnifyStringUtils.format("%.1f", report.summary.lineCoverage)
        val branchCoverage = UnifyStringUtils.format("%.1f", report.summary.branchCoverage)
        val functionCoverage = UnifyStringUtils.format("%.1f", report.summary.functionCoverage)

        val moduleDetails =
            report.modulesCoverage.joinToString("\n") { module ->
                val modulePercent = UnifyStringUtils.format("%.1f", module.coveragePercentage)
                "- ${module.moduleName}: $modulePercent% (${module.coveredLines}/${module.totalLines})"
            }

        val recommendations = report.recommendations.joinToString("\n") { "- $it" }

        return """
            代码覆盖率报告
            ==============
            
            总体覆盖率: $overallCoverage%
            行覆盖率: $lineCoverage%
            分支覆盖率: $branchCoverage%
            函数覆盖率: $functionCoverage%
            
            模块详情:
            $moduleDetails
            
            改进建议:
            $recommendations
            """.trimIndent()
    }

    private fun getCoverageClass(coverage: Double): String {
        return when {
            coverage >= 90.0 -> "high"
            coverage >= 70.0 -> "medium"
            else -> "low"
        }
    }
}
