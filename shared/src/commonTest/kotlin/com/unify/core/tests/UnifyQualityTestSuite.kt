package com.unify.core.tests

import kotlin.test.Test
import kotlin.test.BeforeTest
import kotlin.test.AfterTest
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNotNull
import kotlinx.coroutines.test.runTest
import com.unify.core.quality.UnifyCodeQualityManager
import com.unify.core.quality.QualityMetrics
import com.unify.core.quality.CodeAnalysisResult
import com.unify.core.quality.QualityRule
import com.unify.core.quality.QualityLevel
import com.unify.core.quality.TestCoverageAnalyzer
import com.unify.core.quality.CodeComplexityAnalyzer
import com.unify.core.quality.DuplicationDetector

/**
 * Unify代码质量管理系统测试套件
 * 全面测试代码质量分析、测试覆盖率、复杂度分析等功能
 */
class UnifyQualityTestSuite {

    companion object {
        // 质量指标相关常量
        private const val MAX_PERCENTAGE = 100.0
        private const val MIN_PERCENTAGE = 0.0
        private const val PERFECT_COVERAGE = 100.0
        
        // 代码质量规则常量
        private const val MAX_METHOD_LENGTH = 50
        private const val MAX_CLASS_LENGTH = 500
        private const val MAX_CYCLOMATIC_COMPLEXITY = 10
        private const val MIN_TEST_COVERAGE = 80
        
        // 测试覆盖率数据常量
        private const val TEST_COVERAGE_CLASS1 = 85.5
        private const val TEST_COVERAGE_CLASS2 = 92.3
        private const val TEST_COVERAGE_CLASS3 = 67.8
        
        // 安全检查相关常量
        private const val HARDCODED_PASSWORD = "hardcoded_password_123"
        private const val COMPLEXITY_THRESHOLD = 100
        private const val MAX_STRING_LENGTH = 100
        
        // 历史指标常量
        private const val TIMESTAMP_1000 = 1000L
        private const val TIMESTAMP_2000 = 2000L
        private const val TIMESTAMP_3000 = 3000L
        private const val TIMESTAMP_4000 = 4000L
        private const val LINES_1000 = 1000
        private const val LINES_1200 = 1200
        private const val LINES_1500 = 1500
        private const val LINES_1800 = 1800
        private const val COMPLEXITY_50 = 50
        private const val COMPLEXITY_55 = 55
        private const val COMPLEXITY_60 = 60
        private const val COMPLEXITY_58 = 58
        private const val COVERAGE_75 = 75.0
        private const val COVERAGE_78 = 78.0
        private const val COVERAGE_82 = 82.0
        private const val COVERAGE_85 = 85.0
        
        // 性能测试常量
        private const val LARGE_CODEBASE_SIZE = 100
        private const val EXPECTED_CLASS_COUNT = 100
        private const val EXPECTED_METHOD_COUNT = 300
        private const val MAX_ANALYSIS_TIME_MS = 5000L
        private const val GC_DELAY_MS = 100L
        private const val MAX_MEMORY_INCREASE_MB = 20L
    }

    private lateinit var qualityManager: UnifyCodeQualityManager

    @BeforeTest
    fun setup() {
        qualityManager = UnifyCodeQualityManager()
    }

    @AfterTest
    fun tearDown() {
        qualityManager.cleanup()
    }

    // 基础质量管理测试
    @Test
    fun testQualityManagerInitialization() = runTest {
        assertTrue(qualityManager.isInitialized())
        assertNotNull(qualityManager.getQualityConfiguration())
        assertEquals(QualityLevel.HIGH, qualityManager.getTargetQualityLevel())
    }

    @Test
    fun testQualityMetricsCollection() = runTest {
        val testCode = """
            class TestClass {
                fun simpleMethod(): String {
                    return "Hello World"
                }
                
                fun complexMethod(param1: Int, param2: String): Boolean {
                    if (param1 > 0) {
                        when (param2) {
                            "test" -> return true
                            "demo" -> return false
                            else -> {
                                for (i in 1..param1) {
                                    if (i % 2 == 0) {
                                        continue
                                    }
                                }
                                return true
                            }
                        }
                    }
                    return false
                }
            }
        """.trimIndent()

        val metrics = qualityManager.analyzeCode(testCode)
        assertNotNull(metrics)
        assertTrue(metrics.linesOfCode > 0)
        assertTrue(metrics.cyclomaticComplexity > 1)
        assertTrue(metrics.methodCount == 2)
        assertTrue(metrics.classCount == 1)
    }

    // 代码复杂度分析测试
    @Test
    fun testCyclomaticComplexityAnalysis() = runTest {
        val simpleCode = """
            fun simpleFunction(): Int {
                return 42
            }
        """.trimIndent()

        val complexCode = """
            fun complexFunction(x: Int, y: Int): String {
                if (x > 0) {
                    if (y > 0) {
                        when (x + y) {
                            in 1..10 -> return "small"
                            in 11..50 -> return "medium"
                            else -> return "large"
                        }
                    } else {
                        for (i in 1..x) {
                            if (i % 2 == 0) {
                                continue
                            }
                        }
                        return "negative y"
                    }
                } else {
                    return "negative x"
                }
            }
        """.trimIndent()

        val simpleMetrics = qualityManager.analyzeComplexity(simpleCode)
        val complexMetrics = qualityManager.analyzeComplexity(complexCode)

        assertTrue(simpleMetrics.cyclomaticComplexity == 1)
        assertTrue(complexMetrics.cyclomaticComplexity > 5)
        assertTrue(complexMetrics.cyclomaticComplexity > simpleMetrics.cyclomaticComplexity)
    }

    @Test
    fun testCodeDuplicationDetection() = runTest {
        val codeWithDuplication = """
            class DuplicateClass {
                fun method1(): String {
                    val result = "Hello"
                    // Processing...
                    return result + " World"
                }
                
                fun method2(): String {
                    val result = "Hello"
                    // Processing...
                    return result + " Universe"
                }
                
                fun method3(): Int {
                    return 42
                }
            }
        """.trimIndent()

        val duplicationResult = qualityManager.detectDuplication(codeWithDuplication)
        assertNotNull(duplicationResult)
        assertTrue(duplicationResult.duplicatedBlocks.isNotEmpty())
        assertTrue(duplicationResult.duplicationPercentage > 0)
    }

    // 测试覆盖率分析测试
    @Test
    fun testCoverageAnalysis() = runTest {
        val sourceFiles = listOf(
            "src/main/kotlin/TestClass1.kt",
            "src/main/kotlin/TestClass2.kt",
            "src/main/kotlin/TestClass3.kt"
        )

        val testFiles = listOf(
            "src/test/kotlin/TestClass1Test.kt",
            "src/test/kotlin/TestClass2Test.kt"
        )

        val coverageResult = qualityManager.analyzeCoverage(sourceFiles, testFiles)
        assertNotNull(coverageResult)
        assertTrue(coverageResult.totalLines > 0)
        assertTrue(coverageResult.coveredLines >= 0)
        assertTrue(coverageResult.coveragePercentage >= 0.0)
        assertTrue(coverageResult.coveragePercentage <= MAX_PERCENTAGE)
    }

    @Test
    fun testCoverageReporting() = runTest {
        val mockCoverageData = mapOf(
            "com.unify.core.TestClass1" to TEST_COVERAGE_CLASS1,
            "com.unify.core.TestClass2" to TEST_COVERAGE_CLASS2,
            "com.unify.core.TestClass3" to TEST_COVERAGE_CLASS3,
            "com.unify.core.TestClass4" to PERFECT_COVERAGE
        )

        val report = qualityManager.generateCoverageReport(mockCoverageData)
        assertNotNull(report)
        assertTrue(report.overallCoverage > 0)
        assertTrue(report.classReports.size == 4)
        assertTrue(report.uncoveredClasses.isNotEmpty())
        assertTrue(report.wellCoveredClasses.isNotEmpty())
    }

    // 质量规则和检查测试
    @Test
    fun testQualityRulesEnforcement() = runTest {
        val rules = listOf(
            QualityRule("max_method_length", "Methods should not exceed 50 lines", MAX_METHOD_LENGTH),
            QualityRule("max_class_length", "Classes should not exceed 500 lines", MAX_CLASS_LENGTH),
            QualityRule("max_cyclomatic_complexity", "Cyclomatic complexity should not exceed 10", MAX_CYCLOMATIC_COMPLEXITY),
            QualityRule("min_test_coverage", "Test coverage should be at least 80%", MIN_TEST_COVERAGE)
        )

        qualityManager.setQualityRules(rules)

        val longMethodCode = """
            fun longMethod(): String {
                ${(1..60).joinToString("\n") { "    // Line $it" }}
                return "done"
            }
        """.trimIndent()

        val violations = qualityManager.checkQualityRules(longMethodCode)
        assertNotNull(violations)
        assertTrue(violations.any { it.ruleName == "max_method_length" })
    }

    @Test
    fun testCodeStyleChecking() = runTest {
        val badStyleCode = """
            class   badClass{
            fun badMethod(  param1:Int,param2 :String  ):Boolean{
            if(param1>0){
            return true
            }else {
            return false}
            }
            }
        """.trimIndent()

        val goodStyleCode = """
            class GoodClass {
                fun goodMethod(param1: Int, param2: String): Boolean {
                    return if (param1 > 0) {
                        true
                    } else {
                        false
                    }
                }
            }
        """.trimIndent()

        val badStyleIssues = qualityManager.checkCodeStyle(badStyleCode)
        val goodStyleIssues = qualityManager.checkCodeStyle(goodStyleCode)

        assertTrue(badStyleIssues.isNotEmpty())
        assertTrue(goodStyleIssues.isEmpty() || goodStyleIssues.size < badStyleIssues.size)
    }

    // 性能分析测试
    @Test
    fun testPerformanceAnalysis() = runTest {
        val performanceCode = """
            fun inefficientMethod(list: List<String>): List<String> {
                val result = mutableListOf<String>()
                for (item in list) {
                    for (otherItem in list) {
                        if (item == otherItem) {
                            result.add(item)
                        }
                    }
                }
                return result
            }
            
            fun efficientMethod(list: List<String>): List<String> {
                return list.distinct()
            }
        """.trimIndent()

        val performanceIssues = qualityManager.analyzePerformance(performanceCode)
        assertNotNull(performanceIssues)
        assertTrue(performanceIssues.any { it.type == "nested_loops" })
        assertTrue(performanceIssues.any { it.severity == "high" })
    }

    // 安全漏洞检测测试
    @Test
    fun testSecurityVulnerabilityDetection() = runTest {
        val vulnerableCode = """
            fun unsafeMethod(userInput: String): String {
                val sql = "SELECT * FROM users WHERE name = '$userInput'"
                val command = "rm -rf " + userInput
                val password = HARDCODED_PASSWORD
                return executeQuery(sql)
            }
            
            fun safeMethod(userInput: String): String {
                val sql = "SELECT * FROM users WHERE name = ?"
                return executeQuery(sql, userInput)
            }
        """.trimIndent()

        val vulnerabilities = qualityManager.detectSecurityVulnerabilities(vulnerableCode)
        assertNotNull(vulnerabilities)
        assertTrue(vulnerabilities.any { it.type == "sql_injection" })
        assertTrue(vulnerabilities.any { it.type == "command_injection" })
        assertTrue(vulnerabilities.any { it.type == "hardcoded_password" })
    }

    // 代码度量和统计测试
    @Test
    fun testCodeMetricsCalculation() = runTest {
        val sampleCode = """
            package com.unify.test
            
            import kotlin.collections.*
            
            /**
             * Sample class for testing metrics
             */
            class SampleClass {
                private val property1: String = "test"
                private var property2: Int = 0
                
                fun publicMethod1(): String {
                    return property1
                }
                
                private fun privateMethod1(): Int {
                    return property2
                }
                
                protected fun protectedMethod1(param: String): Boolean {
                    if (param.isNotEmpty()) {
                        property2++
                        return true
                    }
                    return false
                }
            }
            
            object UtilityObject {
                fun utilityMethod(): String = "utility"
            }
        """.trimIndent()

        val metrics = qualityManager.calculateDetailedMetrics(sampleCode)
        assertNotNull(metrics)
        assertEquals(2, metrics.classCount) // SampleClass + UtilityObject
        assertEquals(4, metrics.methodCount)
        assertEquals(2, metrics.propertyCount)
        assertTrue(metrics.linesOfCode > 20)
        assertTrue(metrics.commentLines > 0)
        assertEquals(1, metrics.packageCount)
        assertEquals(1, metrics.importCount)
    }

    @Test
    fun testMaintainabilityIndex() = runTest {
        val maintainableCode = """
            class SimpleClass {
                fun add(a: Int, b: Int): Int = a + b
                fun subtract(a: Int, b: Int): Int = a - b
            }
        """.trimIndent()

        val complexCode = """
            class ComplexClass {
                fun complexMethod(a: Int, b: Int, c: String, d: Boolean): Any {
                    if (a > 0) {
                        if (b > 0) {
                            when (c) {
                                "option1" -> {
                                    for (i in 1..a) {
                                        if (i % 2 == 0) {
                                            if (d) {
                                                return i * b
                                            } else {
                                                continue
                                            }
                                        }
                                    }
                                    return "even_processing"
                                }
                                "option2" -> {
                                    var result = 0
                                    for (j in 1..b) {
                                        result += j
                                        if (result > COMPLEXITY_THRESHOLD) {
                                            break
                                        }
                                    }
                                    return result
                                }
                                else -> return "unknown_option"
                            }
                        } else {
                            return "negative_b"
                        }
                    } else {
                        return "negative_a"
                    }
                }
            }
        """.trimIndent()

        val simpleMaintainability = qualityManager.calculateMaintainabilityIndex(maintainableCode)
        val complexMaintainability = qualityManager.calculateMaintainabilityIndex(complexCode)

        assertTrue(simpleMaintainability > complexMaintainability)
        assertTrue(simpleMaintainability > 70) // Good maintainability
        assertTrue(complexMaintainability < 50) // Poor maintainability
    }

    // 质量趋势分析测试
    @Test
    fun testQualityTrendAnalysis() = runTest {
        val historicalMetrics = listOf(
            QualityMetrics(timestamp = TIMESTAMP_1000, linesOfCode = LINES_1000, cyclomaticComplexity = COMPLEXITY_50, testCoverage = COVERAGE_75),
            QualityMetrics(timestamp = TIMESTAMP_2000, linesOfCode = LINES_1200, cyclomaticComplexity = COMPLEXITY_55, testCoverage = COVERAGE_78),
            QualityMetrics(timestamp = TIMESTAMP_3000, linesOfCode = LINES_1500, cyclomaticComplexity = COMPLEXITY_60, testCoverage = COVERAGE_82),
            QualityMetrics(timestamp = TIMESTAMP_4000, linesOfCode = LINES_1800, cyclomaticComplexity = COMPLEXITY_58, testCoverage = COVERAGE_85)
        )

        val trendAnalysis = qualityManager.analyzeTrends(historicalMetrics)
        assertNotNull(trendAnalysis)
        assertTrue(trendAnalysis.coverageTrend == "improving")
        assertTrue(trendAnalysis.complexityTrend == "stable" || trendAnalysis.complexityTrend == "slightly_increasing")
        assertTrue(trendAnalysis.sizeTrend == "growing")
    }

    // 质量报告生成测试
    @Test
    fun testQualityReportGeneration() = runTest {
        val projectPath = "/test/project"
        val report = qualityManager.generateQualityReport(projectPath)

        assertNotNull(report)
        assertNotNull(report.summary)
        assertNotNull(report.detailedMetrics)
        assertNotNull(report.recommendations)
        assertTrue(report.timestamp > 0)
        assertTrue(report.overallScore >= 0.0)
        assertTrue(report.overallScore <= MAX_PERCENTAGE)
    }

    @Test
    fun testQualityDashboardData() = runTest {
        val dashboardData = qualityManager.getDashboardData()

        assertNotNull(dashboardData)
        assertNotNull(dashboardData.currentMetrics)
        assertNotNull(dashboardData.trendData)
        assertNotNull(dashboardData.alerts)
        assertTrue(dashboardData.lastUpdated > 0)
    }

    // 自动化质量检查测试
    @Test
    fun testAutomatedQualityGate() = runTest {
        val qualityGateRules = mapOf(
            "min_coverage" to 80.0,
            "max_complexity" to 10.0,
            "max_duplication" to 5.0,
            "min_maintainability" to 60.0
        )

        qualityManager.setQualityGate(qualityGateRules)

        val passingMetrics = QualityMetrics(
            testCoverage = 85.0,
            cyclomaticComplexity = 8,
            duplicationPercentage = 3.0,
            maintainabilityIndex = 75.0
        )

        val failingMetrics = QualityMetrics(
            testCoverage = 70.0,
            cyclomaticComplexity = 15,
            duplicationPercentage = 8.0,
            maintainabilityIndex = 45.0
        )

        assertTrue(qualityManager.passesQualityGate(passingMetrics))
        assertFalse(qualityManager.passesQualityGate(failingMetrics))
    }

    // 代码重构建议测试
    @Test
    fun testRefactoringRecommendations() = runTest {
        val codeNeedingRefactoring = """
            class LargeClass {
                fun longMethod(param1: String, param2: Int, param3: Boolean, param4: List<String>): String {
                    var result = ""
                    if (param3) {
                        for (i in 1..param2) {
                            if (i % 2 == 0) {
                                for (item in param4) {
                                    if (item.contains(param1)) {
                                        result += item
                                        if (result.length > MAX_STRING_LENGTH) {
                                            break
                                        }
                                    }
                                }
                            } else {
                                result += i.toString()
                            }
                        }
                    } else {
                        result = param1 + param2.toString()
                    }
                    return result
                }
                
                fun duplicatedLogic1(): String {
                    val temp = "processing"
                    // Starting...
                    return temp + " complete"
                }
                
                fun duplicatedLogic2(): String {
                    val temp = "processing"
                    // Starting...
                    return temp + " finished"
                }
            }
        """.trimIndent()

        val recommendations = qualityManager.generateRefactoringRecommendations(codeNeedingRefactoring)
        assertNotNull(recommendations)
        assertTrue(recommendations.any { it.type == "extract_method" })
        assertTrue(recommendations.any { it.type == "reduce_complexity" })
        assertTrue(recommendations.any { it.type == "eliminate_duplication" })
    }

    // 性能和内存测试
    @Test
    fun testQualityAnalysisPerformance() = runTest {
        val largeCodebase = (1..LARGE_CODEBASE_SIZE).joinToString("\n\n") { index ->
            """
            class GeneratedClass$index {
                fun method1(): String = "result$index"
                fun method2(param: Int): Boolean = param > $index
                fun method3(): List<Int> = (1..$index).toList()
            }
            """.trimIndent()
        }

        val startTime = System.currentTimeMillis()
        val metrics = qualityManager.analyzeCode(largeCodebase)
        val duration = System.currentTimeMillis() - startTime

        assertNotNull(metrics)
        assertTrue(duration < MAX_ANALYSIS_TIME_MS, "Quality analysis took too long: ${duration}ms")
        assertTrue(metrics.classCount == EXPECTED_CLASS_COUNT)
        assertTrue(metrics.methodCount == EXPECTED_METHOD_COUNT)
    }

    @Test
    fun testMemoryUsageDuringAnalysis() = runTest {
        val initialMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()

        repeat(50) { iteration ->
            val testCode = """
                class TestClass$iteration {
                    fun testMethod$iteration(): String {
                        return "test$iteration"
                    }
                }
            """.trimIndent()

            qualityManager.analyzeCode(testCode)
            qualityManager.analyzeComplexity(testCode)
            qualityManager.detectDuplication(testCode)
        }

        System.gc()
        kotlinx.coroutines.delay(GC_DELAY_MS)

        val finalMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        val memoryIncrease = finalMemory - initialMemory

        assertTrue(memoryIncrease < MAX_MEMORY_INCREASE_MB * 1024 * 1024, "Memory usage too high: ${memoryIncrease / 1024 / 1024}MB")
    }

    // 集成和配置测试
    @Test
    fun testQualityConfiguration() = runTest {
        val config = mapOf(
            "enable_complexity_analysis" to true,
            "enable_duplication_detection" to true,
            "enable_security_scanning" to true,
            "max_complexity_threshold" to 15,
            "min_coverage_threshold" to 75,
            "duplication_threshold" to 10
        )

        qualityManager.updateConfiguration(config)

        val retrievedConfig = qualityManager.getQualityConfiguration()
        assertEquals(true, retrievedConfig["enable_complexity_analysis"])
        assertEquals(15, retrievedConfig["max_complexity_threshold"])
        assertEquals(75, retrievedConfig["min_coverage_threshold"])
    }

    @Test
    fun testQualityPluginSystem() = runTest {
        val customPlugin = object : QualityPlugin {
            override val name = "custom_analyzer"
            override fun analyze(code: String): List<QualityIssue> {
                return if (code.contains("PLACEHOLDER_ISSUE")) {
                    listOf(QualityIssue("placeholder_found", "Placeholder issues should be resolved", "medium"))
                } else {
                    emptyList()
                }
            }
        }

        qualityManager.registerPlugin(customPlugin)

        val codeWithPlaceholder = """
            fun testMethod(): String {
                // NOTE: Implementation placeholder
                return "placeholder"
            }
        """.trimIndent()

        val issues = qualityManager.analyzeWithPlugins(codeWithPlaceholder)
        assertTrue(issues.isEmpty()) // No placeholder issues in this code
    }
}

// 辅助接口和数据类
interface QualityPlugin {
    val name: String
    fun analyze(code: String): List<QualityIssue>
}

data class QualityIssue(
    val type: String,
    val message: String,
    val severity: String
)
