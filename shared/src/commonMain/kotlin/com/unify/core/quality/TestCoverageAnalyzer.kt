package com.unify.core.quality

import com.unify.core.platform.getCurrentTimeMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

/**
 * 测试覆盖率分析器
 * 提供跨平台的测试覆盖率统计和分析功能
 */
class TestCoverageAnalyzer {
    private val _coverageData = MutableStateFlow(TestCoverageData())
    val coverageData: StateFlow<TestCoverageData> = _coverageData

    // 覆盖率常量定义
    companion object {
        private const val EXCELLENT_COVERAGE_THRESHOLD = 90.0
        private const val GOOD_COVERAGE_THRESHOLD = 80.0
        private const val FAIR_COVERAGE_THRESHOLD = 70.0
        private const val POOR_COVERAGE_THRESHOLD = 60.0

        // 模块覆盖率目标
        private const val CORE_MODULE_TARGET = 95.0
        private const val UI_MODULE_TARGET = 85.0
        private const val DATA_MODULE_TARGET = 90.0
        private const val NETWORK_MODULE_TARGET = 88.0
        private const val DEVICE_MODULE_TARGET = 82.0
        private const val PLATFORM_MODULE_TARGET = 75.0
        private const val PERFORMANCE_MODULE_TARGET = 85.0
        private const val SECURITY_MODULE_TARGET = 95.0

        // 平台覆盖率权重
        private const val ANDROID_WEIGHT = 0.25
        private const val IOS_WEIGHT = 0.25
        private const val WEB_WEIGHT = 0.20
        private const val DESKTOP_WEIGHT = 0.15
        private const val HARMONY_WEIGHT = 0.10
        private const val MINIAPP_WEIGHT = 0.03
        private const val WATCH_WEIGHT = 0.01
        private const val TV_WEIGHT = 0.01
    }

    /**
     * 分析测试覆盖率
     */
    suspend fun analyzeCoverage(testResults: List<TestResult>): TestCoverageReport {
        val modulesCoverage = analyzeModulesCoverage(testResults)
        val platformsCoverage = analyzePlatformsCoverage(testResults)
        val overallCoverage = calculateOverallCoverage(modulesCoverage, platformsCoverage)

        val report =
            TestCoverageReport(
                overallCoverage = overallCoverage,
                modulesCoverage = modulesCoverage,
                platformsCoverage = platformsCoverage,
                recommendations = generateRecommendations(modulesCoverage, platformsCoverage),
                timestamp = getCurrentTimeMillis(),
            )

        _coverageData.value =
            _coverageData.value.copy(
                latestReport = report,
                totalTests = testResults.size,
                passedTests = testResults.count { it.passed },
                failedTests = testResults.count { !it.passed },
            )

        return report
    }

    /**
     * 分析模块覆盖率
     */
    private fun analyzeModulesCoverage(testResults: List<TestResult>): Map<String, ModuleCoverageInfo> {
        val modules =
            mapOf(
                "core" to CORE_MODULE_TARGET,
                "ui" to UI_MODULE_TARGET,
                "data" to DATA_MODULE_TARGET,
                "network" to NETWORK_MODULE_TARGET,
                "device" to DEVICE_MODULE_TARGET,
                "platform" to PLATFORM_MODULE_TARGET,
                "performance" to PERFORMANCE_MODULE_TARGET,
                "security" to SECURITY_MODULE_TARGET,
            )

        return modules.mapValues { (moduleName, target) ->
            val moduleTests = testResults.filter { it.module == moduleName }
            val totalLines = moduleTests.sumOf { it.totalLines }
            val coveredLines = moduleTests.sumOf { it.coveredLines }
            val coverage = if (totalLines > 0) (coveredLines.toDouble() / totalLines) * 100 else 0.0

            ModuleCoverageInfo(
                moduleName = moduleName,
                coverage = coverage,
                target = target,
                totalLines = totalLines,
                coveredLines = coveredLines,
                uncoveredLines = totalLines - coveredLines,
                testCount = moduleTests.size,
                passedTests = moduleTests.count { it.passed },
                failedTests = moduleTests.count { !it.passed },
            )
        }
    }

    /**
     * 分析平台覆盖率
     */
    private fun analyzePlatformsCoverage(testResults: List<TestResult>): Map<String, PlatformCoverageInfo> {
        val platforms = listOf("android", "ios", "web", "desktop", "harmony", "miniapp", "watch", "tv")

        return platforms.associateWith { platformName ->
            val platformTests = testResults.filter { it.platform == platformName }
            val totalLines = platformTests.sumOf { it.totalLines }
            val coveredLines = platformTests.sumOf { it.coveredLines }
            val coverage = if (totalLines > 0) (coveredLines.toDouble() / totalLines) * 100 else 0.0

            PlatformCoverageInfo(
                platformName = platformName,
                coverage = coverage,
                totalLines = totalLines,
                coveredLines = coveredLines,
                testCount = platformTests.size,
                passedTests = platformTests.count { it.passed },
                failedTests = platformTests.count { !it.passed },
            )
        }
    }

    /**
     * 计算总体覆盖率
     */
    private fun calculateOverallCoverage(
        modulesCoverage: Map<String, ModuleCoverageInfo>,
        platformsCoverage: Map<String, PlatformCoverageInfo>,
    ): Double {
        val moduleWeightedCoverage = modulesCoverage.values.sumOf { it.coverage } / modulesCoverage.size

        val platformWeights =
            mapOf(
                "android" to ANDROID_WEIGHT,
                "ios" to IOS_WEIGHT,
                "web" to WEB_WEIGHT,
                "desktop" to DESKTOP_WEIGHT,
                "harmony" to HARMONY_WEIGHT,
                "miniapp" to MINIAPP_WEIGHT,
                "watch" to WATCH_WEIGHT,
                "tv" to TV_WEIGHT,
            )

        val platformWeightedCoverage =
            platformsCoverage.entries.sumOf { (platform, info) ->
                info.coverage * (platformWeights[platform] ?: 0.0)
            }

        return (moduleWeightedCoverage + platformWeightedCoverage) / 2.0
    }

    /**
     * 生成改进建议
     */
    private fun generateRecommendations(
        modulesCoverage: Map<String, ModuleCoverageInfo>,
        platformsCoverage: Map<String, PlatformCoverageInfo>,
    ): List<CoverageRecommendation> {
        val recommendations = mutableListOf<CoverageRecommendation>()

        // 模块覆盖率建议
        modulesCoverage.values.forEach { module ->
            when {
                module.coverage < POOR_COVERAGE_THRESHOLD -> {
                    recommendations.add(
                        CoverageRecommendation(
                            type = RecommendationType.CRITICAL,
                            module = module.moduleName,
                            message = "模块 ${module.moduleName} 覆盖率过低 (${module.coverage}%)，需要紧急增加测试用例",
                            priority = RecommendationPriority.HIGH,
                        ),
                    )
                }
                module.coverage < module.target -> {
                    recommendations.add(
                        CoverageRecommendation(
                            type = RecommendationType.IMPROVEMENT,
                            module = module.moduleName,
                            message = "模块 ${module.moduleName} 未达到目标覆盖率 (当前: ${module.coverage}%, 目标: ${module.target}%)",
                            priority = RecommendationPriority.MEDIUM,
                        ),
                    )
                }
            }
        }

        // 平台覆盖率建议
        platformsCoverage.values.forEach { platform ->
            if (platform.coverage < FAIR_COVERAGE_THRESHOLD) {
                recommendations.add(
                    CoverageRecommendation(
                        type = RecommendationType.PLATFORM_SPECIFIC,
                        platform = platform.platformName,
                        message = "平台 ${platform.platformName} 覆盖率较低 (${platform.coverage}%)，建议增加平台特定测试",
                        priority = RecommendationPriority.MEDIUM,
                    ),
                )
            }
        }

        return recommendations
    }

    /**
     * 获取覆盖率趋势
     */
    fun getCoverageTrend(): Flow<List<CoverageTrendPoint>> {
        // 实现覆盖率趋势分析
        return MutableStateFlow(emptyList())
    }

    /**
     * 导出覆盖率报告
     */
    suspend fun exportReport(format: ReportFormat): String {
        val report = _coverageData.value.latestReport ?: return ""

        return when (format) {
            ReportFormat.JSON -> "mock_coverage_report_${getCurrentTimeMillis()}"
            ReportFormat.HTML -> generateHtmlReport(report)
            ReportFormat.CSV -> generateCsvReport(report)
        }
    }

    private fun generateHtmlReport(report: TestCoverageReport): String {
        val coverageClass = getCoverageClass(report.overallCoverage)
        val modulesRows =
            report.modulesCoverage.values.joinToString("") { module ->
                val status = if (module.coverage >= module.target) "✅" else "❌"
                "<tr><td>${module.moduleName}</td><td>${module.coverage}%</td>" +
                    "<td>${module.target}%</td><td>${module.testCount}</td><td>$status</td></tr>"
            }
        val platformsRows =
            report.platformsCoverage.values.joinToString("") { platform ->
                "<tr><td>${platform.platformName}</td><td>${platform.coverage}%</td>" +
                    "<td>${platform.testCount}</td><td>${platform.passedTests}/${platform.failedTests}</td></tr>"
            }

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Unify-Core 测试覆盖率报告</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 20px; }
                    .header { background: #f5f5f5; padding: 20px; border-radius: 8px; }
                    .coverage-high { color: #4CAF50; }
                    .coverage-medium { color: #FF9800; }
                    .coverage-low { color: #F44336; }
                    table { width: 100%; border-collapse: collapse; margin: 20px 0; }
                    th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }
                    th { background-color: #f2f2f2; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>Unify-Core 测试覆盖率报告</h1>
                    <p>总体覆盖率: <span class="$coverageClass">${report.overallCoverage}%</span></p>
                    <p>生成时间: ${report.timestamp}</p>
                </div>
                
                <h2>模块覆盖率</h2>
                <table>
                    <tr><th>模块</th><th>覆盖率</th><th>目标</th><th>测试数量</th><th>状态</th></tr>
                    $modulesRows
                </table>
                
                <h2>平台覆盖率</h2>
                <table>
                    <tr><th>平台</th><th>覆盖率</th><th>测试数量</th><th>通过/失败</th></tr>
                    $platformsRows
                </table>
            </body>
            </html>
            """.trimIndent()
    }

    private fun generateCsvReport(report: TestCoverageReport): String {
        val csv = StringBuilder()
        csv.appendLine("模块,覆盖率,目标,测试数量,通过,失败")
        report.modulesCoverage.values.forEach { module ->
            csv.appendLine(
                "${module.moduleName},${module.coverage},${module.target},${module.testCount},${module.passedTests},${module.failedTests}",
            )
        }
        return csv.toString()
    }

    private fun getCoverageClass(coverage: Double): String {
        return when {
            coverage >= EXCELLENT_COVERAGE_THRESHOLD -> "coverage-high"
            coverage >= GOOD_COVERAGE_THRESHOLD -> "coverage-medium"
            else -> "coverage-low"
        }
    }
}

/**
 * 测试结果数据
 */
@Serializable
data class TestResult(
    val testName: String,
    val module: String,
    val platform: String,
    val passed: Boolean,
    val totalLines: Int,
    val coveredLines: Int,
    val executionTime: Long,
    val errorMessage: String? = null,
)

/**
 * 测试覆盖率数据
 */
@Serializable
data class TestCoverageData(
    val latestReport: TestCoverageReport? = null,
    val totalTests: Int = 0,
    val passedTests: Int = 0,
    val failedTests: Int = 0,
    val lastAnalysisTime: Long = 0,
)

/**
 * 测试覆盖率报告
 */
@Serializable
data class TestCoverageReport(
    val overallCoverage: Double,
    val modulesCoverage: Map<String, ModuleCoverageInfo>,
    val platformsCoverage: Map<String, PlatformCoverageInfo>,
    val recommendations: List<CoverageRecommendation>,
    val timestamp: Long,
)

/**
 * 模块覆盖率信息
 */
@Serializable
data class ModuleCoverageInfo(
    val moduleName: String,
    val coverage: Double,
    val target: Double,
    val totalLines: Int,
    val coveredLines: Int,
    val uncoveredLines: Int,
    val testCount: Int,
    val passedTests: Int,
    val failedTests: Int,
)

/**
 * 平台覆盖率信息
 */
@Serializable
data class PlatformCoverageInfo(
    val platformName: String,
    val coverage: Double,
    val totalLines: Int,
    val coveredLines: Int,
    val testCount: Int,
    val passedTests: Int,
    val failedTests: Int,
)

/**
 * 覆盖率建议
 */
@Serializable
data class CoverageRecommendation(
    val type: RecommendationType,
    val module: String? = null,
    val platform: String? = null,
    val message: String,
    val priority: RecommendationPriority,
)

/**
 * 建议类型
 */
enum class RecommendationType {
    CRITICAL,
    IMPROVEMENT,
    PLATFORM_SPECIFIC,
    OPTIMIZATION,
}

/**
 * 建议优先级
 */
enum class RecommendationPriority {
    HIGH,
    MEDIUM,
    LOW,
}

/**
 * 覆盖率趋势点
 */
@Serializable
data class CoverageTrendPoint(
    val timestamp: Long,
    val overallCoverage: Double,
    val modulesCoverage: Map<String, Double>,
)

/**
 * 报告格式
 */
enum class ReportFormat {
    JSON,
    HTML,
    CSV,
}
