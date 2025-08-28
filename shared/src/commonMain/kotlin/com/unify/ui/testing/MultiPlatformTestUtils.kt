package com.unify.ui.testing

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * 多平台测试工具类
 */
object MultiPlatformTestUtils {
    
    /**
     * 跨平台组件测试函数
     */
    @Composable
    fun <T> testComponentAcrossPlatforms(
        componentName: String,
        testBlock: suspend (PlatformTestContext) -> TestResult<T>
    ): Map<PlatformType, TestResult<T>> {
        val results = mutableMapOf<PlatformType, TestResult<T>>()
        
        // 模拟在不同平台上运行测试
        PlatformType.values().forEach { platform ->
            val context = PlatformTestContext(platform, componentName)
            val result = runBlocking {
                withTimeout(30.seconds) {
                    testBlock(context)
                }
            }
            results[platform] = result
        }
        
        return results
    }
    
    /**
     * 验证组件在不同平台的一致性
     */
    fun <T> verifyConsistency(
        testResults: Map<PlatformType, TestResult<T>>,
        consistencyCheck: (List<T>) -> Boolean
    ): ConsistencyReport {
        val values = testResults.values.map { it.value }
        val isConsistent = consistencyCheck(values)
        val platformDifferences = mutableMapOf<PlatformType, String>()
        
        testResults.forEach { (platform, result) ->
            if (result.error != null) {
                platformDifferences[platform] = "Error: ${result.error.message}"
            }
        }
        
        return ConsistencyReport(
            isConsistent = isConsistent,
            platformResults = testResults,
            differences = platformDifferences,
            timestamp = System.currentTimeMillis()
        )
    }
    
    /**
     * 性能基准测试
     */
    fun performanceBenchmark(
        componentName: String,
        iterations: Int = 100,
        testBlock: suspend (PlatformType) -> PerformanceMetrics
    ): Map<PlatformType, PerformanceMetrics> {
        val results = mutableMapOf<PlatformType, PerformanceMetrics>()
        
        PlatformType.values().forEach { platform ->
            val metrics = runBlocking {
                val allMetrics = mutableListOf<PerformanceMetrics>()
                repeat(iterations) {
                    val metric = testBlock(platform)
                    allMetrics.add(metric)
                }
                aggregateMetrics(allMetrics)
            }
            results[platform] = metrics
        }
        
        return results
    }
    
    /**
     * 生成测试报告
     */
    fun generateTestReport(
        componentName: String,
        functionalResults: Map<PlatformType, TestResult<*>>,
        performanceResults: Map<PlatformType, PerformanceMetrics>,
        consistencyReport: ConsistencyReport
    ): TestReport {
        val totalTests = functionalResults.size + performanceResults.size
        val passedTests = functionalResults.values.count { it.success } + 
                         performanceResults.values.count { it.isWithinThreshold }
        
        return TestReport(
            componentName = componentName,
            totalTests = totalTests,
            passedTests = passedTests,
            passRate = passedTests.toDouble() / totalTests.toDouble(),
            functionalResults = functionalResults,
            performanceResults = performanceResults,
            consistencyReport = consistencyReport,
            timestamp = System.currentTimeMillis()
        )
    }
    
    private fun aggregateMetrics(metrics: List<PerformanceMetrics>): PerformanceMetrics {
        return PerformanceMetrics(
            averageTime = metrics.map { it.averageTime }.average(),
            minTime = metrics.map { it.minTime }.minOrNull() ?: 0.0,
            maxTime = metrics.map { it.maxTime }.maxOrNull() ?: 0.0,
            memoryUsage = metrics.map { it.memoryUsage }.average(),
            cpuUsage = metrics.map { it.cpuUsage }.average(),
            fps = metrics.map { it.fps }.average(),
            isWithinThreshold = metrics.all { it.isWithinThreshold }
        )
    }
}

/**
 * 平台测试上下文
 */
data class PlatformTestContext(
    val platform: PlatformType,
    val componentName: String,
    val testConfig: TestConfig = TestConfig()
)

/**
 * 测试结果
 */
data class TestResult<T>(
    val value: T,
    val success: Boolean,
    val error: Throwable? = null,
    val duration: Duration = Duration.ZERO
)

/**
 * 性能指标
 */
data class PerformanceMetrics(
    val averageTime: Double, // 毫秒
    val minTime: Double,
    val maxTime: Double,
    val memoryUsage: Double, // MB
    val cpuUsage: Double, // 百分比
    val fps: Double,
    val isWithinThreshold: Boolean = true
)

/**
 * 一致性报告
 */
data class ConsistencyReport(
    val isConsistent: Boolean,
    val platformResults: Map<PlatformType, TestResult<*>>,
    val differences: Map<PlatformType, String>,
    val timestamp: Long
)

/**
 * 测试报告
 */
data class TestReport(
    val componentName: String,
    val totalTests: Int,
    val passedTests: Int,
    val passRate: Double,
    val functionalResults: Map<PlatformType, TestResult<*>>,
    val performanceResults: Map<PlatformType, PerformanceMetrics>,
    val consistencyReport: ConsistencyReport,
    val timestamp: Long
)

/**
 * 测试配置
 */
data class TestConfig(
    val timeout: Duration = 30.seconds,
    val retryCount: Int = 3,
    val performanceThreshold: PerformanceThreshold = PerformanceThreshold(),
    val enableScreenshots: Boolean = true,
    val enableVideoRecording: Boolean = false
)

/**
 * 性能阈值
 */
data class PerformanceThreshold(
    val maxTime: Double = 100.0, // 毫秒
    val maxMemory: Double = 50.0, // MB
    val minFPS: Double = 30.0,
    val maxCPU: Double = 80.0 // 百分比
)

/**
 * 平台类型枚举
 */
enum class PlatformType {
    ANDROID, IOS, WEB, DESKTOP
}

/**
 * Compose测试扩展函数
 */
fun ComposeContentTestRule.testButtonClick(
    buttonText: String,
    expectedResult: () -> Boolean
) {
    onNodeWithText(buttonText).performClick()
    assert(expectedResult())
}

fun ComposeContentTestRule.testTextInput(
    fieldTag: String,
    inputText: String,
    expectedResult: (String) -> Boolean
) {
    onNodeWithTag(fieldTag).performTextInput(inputText)
    assert(expectedResult(inputText))
}

fun ComposeContentTestRule.testComponentPerformance(
    componentTag: String,
    iterations: Int = 10,
    performanceCheck: (Long) -> Boolean
) {
    val times = mutableListOf<Long>()
    
    repeat(iterations) {
        val startTime = System.currentTimeMillis()
        onNodeWithTag(componentTag).performClick()
        val endTime = System.currentTimeMillis()
        times.add(endTime - startTime)
    }
    
    val averageTime = times.average()
    assert(performanceCheck(averageTime.toLong()))
}

/**
 * 异步测试支持
 */
suspend fun <T> withRetry(
    retries: Int = 3,
    delay: Duration = 1.seconds,
    block: suspend () -> T
): T {
    var lastError: Throwable? = null
    
    repeat(retries) { attempt ->
        try {
            return block()
        } catch (e: Throwable) {
            lastError = e
            if (attempt < retries - 1) {
                kotlinx.coroutines.delay(delay.inWholeMilliseconds)
            }
        }
    }
    
    throw lastError ?: IllegalStateException("No error recorded after $retries retries")
}

/**
 * 测试断言扩展
 */
fun <T> T.shouldBe(expected: T, message: String = "Expected: $expected, but got: $this"): TestResult<T> {
    return if (this == expected) {
        TestResult(this, true)
    } else {
        TestResult(this, false, AssertionError(message))
    }
}

fun Double.shouldBeLessThan(maxValue: Double, message: String = "Value $this should be less than $maxValue"): TestResult<Double> {
    return if (this < maxValue) {
        TestResult(this, true)
    } else {
        TestResult(this, false, AssertionError(message))
    }
}

fun Double.shouldBeGreaterThan(minValue: Double, message: String = "Value $this should be greater than $minValue"): TestResult<Double> {
    return if (this > minValue) {
        TestResult(this, true)
    } else {
        TestResult(this, false, AssertionError(message))
    }
}