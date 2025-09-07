package com.unify.core.dynamic
import com.unify.core.platform.getCurrentTimeMillis
import com.unify.core.platform.getNanoTime

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable

/**
 * 动态测试运行器 - 负责执行和管理动态组件测试
 */
class DynamicTestRunner(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    
    companion object {
        const val MAX_PARALLEL_TESTS = 5
        const val TEST_QUEUE_SIZE = 100
        const val RESULT_CACHE_SIZE = 500
        const val CLEANUP_INTERVAL_MS = 60000L
        const val HEARTBEAT_INTERVAL_MS = 5000L
        const val MAX_EXECUTION_TIME_MS = 600000L // 10分钟
        const val RETRY_DELAY_MS = 1000L
        const val MAX_RETRY_ATTEMPTS = 3
    }
    
    private val _runnerState = MutableStateFlow(TestRunnerState.IDLE)
    val runnerState: StateFlow<TestRunnerState> = _runnerState.asStateFlow()
    
    private val _testQueue = MutableStateFlow<List<QueuedTest>>(emptyList())
    val testQueue: StateFlow<List<QueuedTest>> = _testQueue.asStateFlow()
    
    private val _runningTests = MutableStateFlow<Map<String, RunningTest>>(emptyMap())
    val runningTests: StateFlow<Map<String, RunningTest>> = _runningTests.asStateFlow()
    
    private val _completedTests = MutableStateFlow<List<CompletedTest>>(emptyList())
    val completedTests: StateFlow<List<CompletedTest>> = _completedTests.asStateFlow()
    
    private val _runnerMetrics = MutableStateFlow(TestRunnerMetrics())
    val runnerMetrics: StateFlow<TestRunnerMetrics> = _runnerMetrics.asStateFlow()
    
    private val testFramework = DynamicTestFramework()
    
    /**
     * 初始化测试运行器
     */
    suspend fun initialize(): Boolean {
        return try {
            _runnerState.value = TestRunnerState.INITIALIZING
            
            // 初始化测试框架
            testFramework.initialize()
            
            // 启动后台任务
            startBackgroundTasks()
            
            _runnerState.value = TestRunnerState.READY
            true
        } catch (e: Exception) {
            _runnerState.value = TestRunnerState.ERROR
            false
        }
    }
    
    /**
     * 启动后台任务
     */
    private fun startBackgroundTasks() {
        // 测试队列处理器
        scope.launch {
            while (_runnerState.value != TestRunnerState.STOPPED) {
                processTestQueue()
                delay(1000)
            }
        }
        
        // 清理任务
        scope.launch {
            while (_runnerState.value != TestRunnerState.STOPPED) {
                performCleanup()
                delay(CLEANUP_INTERVAL_MS)
            }
        }
        
        // 心跳监控
        scope.launch {
            while (_runnerState.value != TestRunnerState.STOPPED) {
                updateHeartbeat()
                delay(HEARTBEAT_INTERVAL_MS)
            }
        }
    }
    
    /**
     * 提交测试任务
     */
    fun submitTest(testRequest: TestRequest): String {
        val testId = kotlin.random.Random.nextInt().toString()
        
        val queuedTest = QueuedTest(
            id = testId,
            request = testRequest,
            priority = testRequest.priority,
            submittedAt = getCurrentTimeMillis(),
            retryCount = 0
        )
        
        val currentQueue = _testQueue.value.toMutableList()
        
        if (currentQueue.size >= TEST_QUEUE_SIZE) {
            throw IllegalStateException("测试队列已满")
        }
        
        currentQueue.add(queuedTest)
        // 按优先级排序
        currentQueue.sortByDescending { it.priority.ordinal }
        _testQueue.value = currentQueue
        
        updateMetrics { it.copy(totalSubmitted = it.totalSubmitted + 1) }
        
        return testId
    }
    
    /**
     * 批量提交测试任务
     */
    fun submitTests(testRequests: List<TestRequest>): List<String> {
        return testRequests.map { request ->
            submitTest(request)
        }
    }
    
    /**
     * 处理测试队列
     */
    private suspend fun processTestQueue() {
        val queue = _testQueue.value
        val running = _runningTests.value
        
        if (queue.isEmpty() || running.size >= MAX_PARALLEL_TESTS) {
            return
        }
        
        val availableSlots = MAX_PARALLEL_TESTS - running.size
        val testsToRun = queue.take(availableSlots)
        
        testsToRun.forEach { queuedTest ->
            executeTest(queuedTest)
        }
        
        // 从队列中移除已开始执行的测试
        val remainingQueue = queue.drop(testsToRun.size)
        _testQueue.value = remainingQueue
    }
    
    /**
     * 执行测试
     */
    private fun executeTest(queuedTest: QueuedTest) {
        scope.launch {
            val runningTest = RunningTest(
                id = queuedTest.id,
                request = queuedTest.request,
                startTime = getCurrentTimeMillis(),
                status = TestExecutionStatus.RUNNING,
                progress = 0.0
            )
            
            val currentRunning = _runningTests.value.toMutableMap()
            currentRunning[queuedTest.id] = runningTest
            _runningTests.value = currentRunning
            
            updateMetrics { it.copy(totalStarted = it.totalStarted + 1) }
            
            try {
                val result = when (queuedTest.request.type) {
                    TestRequestType.COMPONENT_LOAD -> executeComponentLoadTest(queuedTest.request)
                    TestRequestType.PERFORMANCE -> executePerformanceTest(queuedTest.request)
                    TestRequestType.SECURITY -> executeSecurityTest(queuedTest.request)
                    TestRequestType.INTEGRATION -> executeIntegrationTest(queuedTest.request)
                    TestRequestType.STRESS -> executeStressTest(queuedTest.request)
                    TestRequestType.COMPATIBILITY -> executeCompatibilityTest(queuedTest.request)
                }
                
                completeTest(queuedTest.id, result)
                
            } catch (e: Exception) {
                val errorResult = TestExecutionResult(
                    success = false,
                    message = "测试执行异常: ${e.message}",
                    executionTime = getCurrentTimeMillis() - runningTest.startTime,
                    details = mapOf("error" to e.toString())
                )
                
                handleTestFailure(queuedTest, errorResult)
            }
        }
    }
    
    /**
     * 执行组件加载测试
     */
    private suspend fun executeComponentLoadTest(request: TestRequest): TestExecutionResult {
        val startTime = getCurrentTimeMillis()
        
        updateTestProgress(request.id, 0.1)
        
        // 模拟组件加载
        delay(kotlin.random.Random.nextLong(1000, 3000))
        updateTestProgress(request.id, 0.5)
        
        val componentId = request.parameters["componentId"] ?: "unknown"
        val loadSuccess = kotlin.random.Random.nextDouble() > 0.1 // 90%成功率
        
        updateTestProgress(request.id, 0.8)
        delay(500)
        updateTestProgress(request.id, 1.0)
        
        return TestExecutionResult(
            success = loadSuccess,
            message = if (loadSuccess) "组件加载成功" else "组件加载失败",
            executionTime = getCurrentTimeMillis() - startTime,
            details = mapOf(
                "componentId" to componentId,
                "loadTime" to (getCurrentTimeMillis() - startTime).toString()
            )
        )
    }
    
    /**
     * 执行性能测试
     */
    private suspend fun executePerformanceTest(request: TestRequest): TestExecutionResult {
        val startTime = getCurrentTimeMillis()
        
        updateTestProgress(request.id, 0.1)
        
        // 模拟性能测试
        delay(kotlin.random.Random.nextLong(2000, 5000))
        updateTestProgress(request.id, 0.4)
        
        val cpuUsage = kotlin.random.Random.nextDouble(10.0, 80.0)
        val memoryUsage = kotlin.random.Random.nextLong(50, 200)
        val responseTime = kotlin.random.Random.nextLong(100, 1000)
        
        updateTestProgress(request.id, 0.7)
        delay(1000)
        updateTestProgress(request.id, 1.0)
        
        val performanceGood = cpuUsage < 70.0 && memoryUsage < 150 && responseTime < 800
        
        return TestExecutionResult(
            success = performanceGood,
            message = if (performanceGood) "性能测试通过" else "性能测试未达标",
            executionTime = getCurrentTimeMillis() - startTime,
            details = mapOf(
                "cpuUsage" to cpuUsage.toString(),
                "memoryUsage" to memoryUsage.toString(),
                "responseTime" to responseTime.toString()
            )
        )
    }
    
    /**
     * 执行安全测试
     */
    private suspend fun executeSecurityTest(request: TestRequest): TestExecutionResult {
        val startTime = getCurrentTimeMillis()
        
        updateTestProgress(request.id, 0.2)
        
        // 模拟安全测试
        delay(kotlin.random.Random.nextLong(1500, 4000))
        updateTestProgress(request.id, 0.6)
        
        val vulnerabilities = kotlin.random.Random.nextInt(0, 3)
        val signatureValid = kotlin.random.Random.nextDouble() > 0.05
        val permissionsCorrect = kotlin.random.Random.nextDouble() > 0.1
        
        updateTestProgress(request.id, 0.9)
        delay(500)
        updateTestProgress(request.id, 1.0)
        
        val securityPassed = vulnerabilities == 0 && signatureValid && permissionsCorrect
        
        return TestExecutionResult(
            success = securityPassed,
            message = if (securityPassed) "安全测试通过" else "发现安全问题",
            executionTime = getCurrentTimeMillis() - startTime,
            details = mapOf(
                "vulnerabilities" to vulnerabilities.toString(),
                "signatureValid" to signatureValid.toString(),
                "permissionsCorrect" to permissionsCorrect.toString()
            )
        )
    }
    
    /**
     * 执行集成测试
     */
    private suspend fun executeIntegrationTest(request: TestRequest): TestExecutionResult {
        val startTime = getCurrentTimeMillis()
        
        updateTestProgress(request.id, 0.1)
        
        // 模拟集成测试
        delay(kotlin.random.Random.nextLong(3000, 6000))
        updateTestProgress(request.id, 0.5)
        
        val componentsIntegrated = kotlin.random.Random.nextInt(2, 6)
        val dataFlowCorrect = kotlin.random.Random.nextDouble() > 0.15
        val eventsHandled = kotlin.random.Random.nextDouble() > 0.1
        
        updateTestProgress(request.id, 0.8)
        delay(1000)
        updateTestProgress(request.id, 1.0)
        
        val integrationSuccess = dataFlowCorrect && eventsHandled
        
        return TestExecutionResult(
            success = integrationSuccess,
            message = if (integrationSuccess) "集成测试通过" else "集成测试失败",
            executionTime = getCurrentTimeMillis() - startTime,
            details = mapOf(
                "componentsIntegrated" to componentsIntegrated.toString(),
                "dataFlowCorrect" to dataFlowCorrect.toString(),
                "eventsHandled" to eventsHandled.toString()
            )
        )
    }
    
    /**
     * 执行压力测试
     */
    private suspend fun executeStressTest(request: TestRequest): TestExecutionResult {
        val startTime = getCurrentTimeMillis()
        
        updateTestProgress(request.id, 0.1)
        
        // 模拟压力测试
        val testDuration = kotlin.random.Random.nextLong(5000, 10000)
        val progressSteps = 10
        val stepDuration = testDuration / progressSteps
        
        for (i in 1..progressSteps) {
            delay(stepDuration)
            updateTestProgress(request.id, i.toDouble() / progressSteps)
        }
        
        val maxConcurrentUsers = kotlin.random.Random.nextInt(100, 1000)
        val errorRate = kotlin.random.Random.nextDouble(0.0, 10.0)
        val avgResponseTime = kotlin.random.Random.nextLong(200, 2000)
        
        val stressPassed = errorRate < 5.0 && avgResponseTime < 1500
        
        return TestExecutionResult(
            success = stressPassed,
            message = if (stressPassed) "压力测试通过" else "压力测试未通过",
            executionTime = getCurrentTimeMillis() - startTime,
            details = mapOf(
                "maxConcurrentUsers" to maxConcurrentUsers.toString(),
                "responseTime" to avgResponseTime.toString(),
                "avgResponseTime" to avgResponseTime.toString()
            )
        )
    }
    
    /**
     * 执行兼容性测试
     */
    private suspend fun executeCompatibilityTest(request: TestRequest): TestExecutionResult {
        val startTime = getCurrentTimeMillis()
        
        updateTestProgress(request.id, 0.1)
        
        // 模拟兼容性测试
        delay(kotlin.random.Random.nextLong(2000, 4000))
        updateTestProgress(request.id, 0.5)
        
        val platforms = listOf("Android", "iOS", "Web", "Desktop")
        val compatiblePlatforms = platforms.filter { kotlin.random.Random.nextDouble() > 0.1 }
        
        updateTestProgress(request.id, 0.8)
        delay(1000)
        updateTestProgress(request.id, 1.0)
        
        val compatibilityGood = compatiblePlatforms.size >= platforms.size * 0.8
        
        return TestExecutionResult(
            success = compatibilityGood,
            message = if (compatibilityGood) "兼容性测试通过" else "兼容性问题",
            executionTime = getCurrentTimeMillis() - startTime,
            details = mapOf(
                "totalPlatforms" to platforms.size.toString(),
                "compatiblePlatforms" to compatiblePlatforms.size.toString(),
                "compatibilityRate" to (compatiblePlatforms.size.toDouble() / platforms.size * 100).toString()
            )
        )
    }
    
    /**
     * 更新测试进度
     */
    private fun updateTestProgress(testId: String, progress: Double) {
        val currentRunning = _runningTests.value.toMutableMap()
        val runningTest = currentRunning[testId]
        
        if (runningTest != null) {
            currentRunning[testId] = runningTest.copy(progress = progress)
            _runningTests.value = currentRunning
        }
    }
    
    /**
     * 完成测试
     */
    private fun completeTest(testId: String, result: TestExecutionResult) {
        val runningTest = _runningTests.value[testId] ?: return
        
        val completedTest = CompletedTest(
            id = testId,
            request = runningTest.request,
            result = result,
            startTime = runningTest.startTime,
            endTime = getCurrentTimeMillis()
        )
        
        // 移除运行中的测试
        val currentRunning = _runningTests.value.toMutableMap()
        currentRunning.remove(testId)
        _runningTests.value = currentRunning
        
        // 添加到完成列表
        val currentCompleted = _completedTests.value.toMutableList()
        currentCompleted.add(0, completedTest) // 添加到开头
        
        // 保持缓存大小限制
        if (currentCompleted.size > RESULT_CACHE_SIZE) {
            currentCompleted.removeAt(currentCompleted.size - 1)
        }
        
        _completedTests.value = currentCompleted
        
        // 更新指标
        updateMetrics { metrics ->
            if (result.success) {
                metrics.copy(totalCompleted = metrics.totalCompleted + 1)
            } else {
                metrics.copy(
                    totalCompleted = metrics.totalCompleted + 1,
                    totalFailed = metrics.totalFailed + 1
                )
            }
        }
    }
    
    /**
     * 处理测试失败
     */
    private suspend fun handleTestFailure(queuedTest: QueuedTest, result: TestExecutionResult) {
        if (queuedTest.retryCount < MAX_RETRY_ATTEMPTS) {
            // 重试测试
            delay(RETRY_DELAY_MS * (queuedTest.retryCount + 1))
            
            val retryTest = queuedTest.copy(retryCount = queuedTest.retryCount + 1)
            val currentQueue = _testQueue.value.toMutableList()
            currentQueue.add(0, retryTest) // 添加到队列开头
            _testQueue.value = currentQueue
            
            // 移除运行中的测试
            val currentRunning = _runningTests.value.toMutableMap()
            currentRunning.remove(queuedTest.id)
            _runningTests.value = currentRunning
            
        } else {
            // 达到最大重试次数，标记为失败
            completeTest(queuedTest.id, result)
        }
    }
    
    /**
     * 取消测试
     */
    fun cancelTest(testId: String): Boolean {
        // 从队列中移除
        val currentQueue = _testQueue.value.toMutableList()
        val queuedTest = currentQueue.find { it.id == testId }
        if (queuedTest != null) {
            currentQueue.remove(queuedTest)
            _testQueue.value = currentQueue
            return true
        }
        
        // 从运行中移除
        val currentRunning = _runningTests.value.toMutableMap()
        val runningTest = currentRunning[testId]
        if (runningTest != null) {
            currentRunning.remove(testId)
            _runningTests.value = currentRunning
            
            // 添加到完成列表，标记为取消
            val cancelledResult = TestExecutionResult(
                success = false,
                message = "测试已取消",
                executionTime = getCurrentTimeMillis() - runningTest.startTime,
                details = mapOf("cancelled" to "true")
            )
            
            completeTest(testId, cancelledResult)
            return true
        }
        
        return false
    }
    
    /**
     * 获取测试状态
     */
    fun getTestStatus(testId: String): TestStatus? {
        // 检查队列
        _testQueue.value.find { it.id == testId }?.let {
            return TestStatus.PENDING
        }
        
        // 检查运行中
        _runningTests.value[testId]?.let {
            return TestStatus.RUNNING
        }
        
        // 检查完成
        _completedTests.value.find { it.id == testId }?.let {
            return if (it.result.success) TestStatus.PASSED else TestStatus.FAILED
        }
        
        return null
    }
    
    /**
     * 执行清理任务
     */
    private fun performCleanup() {
        val currentTime = getCurrentTimeMillis()
        
        // 清理超时的运行测试
        val currentRunning = _runningTests.value.toMutableMap()
        val timedOutTests = currentRunning.values.filter { 
            currentTime - it.startTime > MAX_EXECUTION_TIME_MS 
        }
        
        timedOutTests.forEach { test ->
            currentRunning.remove(test.id)
            
            val timeoutResult = TestExecutionResult(
                success = false,
                message = "测试超时",
                executionTime = currentTime - test.startTime,
                details = mapOf("timeout" to "true")
            )
            
            completeTest(test.id, timeoutResult)
        }
        
        if (timedOutTests.isNotEmpty()) {
            _runningTests.value = currentRunning
        }
    }
    
    /**
     * 更新心跳
     */
    private fun updateHeartbeat() {
        updateMetrics { it.copy(lastHeartbeat = getCurrentTimeMillis()) }
    }
    
    /**
     * 更新指标
     */
    private fun updateMetrics(updater: (TestRunnerMetrics) -> TestRunnerMetrics) {
        _runnerMetrics.value = updater(_runnerMetrics.value)
    }
    
    /**
     * 获取运行器统计信息
     */
    fun getRunnerStats(): TestRunnerStats {
        val metrics = _runnerMetrics.value
        val queue = _testQueue.value
        val running = _runningTests.value
        val completed = _completedTests.value
        
        return TestRunnerStats(
            queuedTests = queue.size,
            runningTests = running.size,
            completedTests = completed.size,
            totalSubmitted = metrics.totalSubmitted,
            totalStarted = metrics.totalStarted,
            totalCompleted = metrics.totalCompleted,
            totalFailed = metrics.totalFailed,
            successRate = if (metrics.totalCompleted > 0) {
                ((metrics.totalCompleted - metrics.totalFailed).toDouble() / metrics.totalCompleted) * 100
            } else 0.0,
            averageExecutionTime = completed.takeIf { it.isNotEmpty() }?.map { 
                it.endTime - it.startTime 
            }?.average()?.toLong() ?: 0L,
            lastHeartbeat = metrics.lastHeartbeat
        )
    }
    
    /**
     * 停止运行器
     */
    suspend fun stop() {
        _runnerState.value = TestRunnerState.STOPPING
        
        // 取消所有排队的测试
        _testQueue.value = emptyList()
        
        // 等待运行中的测试完成或超时
        val maxWaitTime = 30000L // 30秒
        val startTime = getCurrentTimeMillis()
        
        while (_runningTests.value.isNotEmpty() && 
               getCurrentTimeMillis() - startTime < maxWaitTime) {
            delay(1000)
        }
        
        // 强制停止剩余的测试
        _runningTests.value = emptyMap()
        
        _runnerState.value = TestRunnerState.STOPPED
    }
}

/**
 * 测试运行器状态枚举
 */
enum class TestRunnerState {
    IDLE,
    INITIALIZING,
    READY,
    RUNNING,
    STOPPING,
    STOPPED,
    ERROR
}

/**
 * 测试请求类型枚举
 */
enum class TestRequestType {
    COMPONENT_LOAD,
    PERFORMANCE,
    SECURITY,
    INTEGRATION,
    STRESS,
    COMPATIBILITY
}

/**
 * 测试优先级枚举
 */
enum class TestPriority {
    LOW,
    NORMAL,
    HIGH,
    CRITICAL
}

/**
 * 测试执行状态枚举
 */
enum class TestExecutionStatus {
    QUEUED,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELLED
}

// TestStatus已在UnifyTypes.kt中定义，此处移除重复声明

/**
 * 测试请求数据类
 */
@Serializable
data class TestRequest(
    val id: String = kotlin.random.Random.nextInt().toString(),
    val type: TestRequestType,
    val name: String,
    val description: String = "",
    val priority: TestPriority = TestPriority.NORMAL,
    val timeout: Long = 30000L,
    val parameters: Map<String, String> = emptyMap()
)

/**
 * 排队测试数据类
 */
@Serializable
data class QueuedTest(
    val id: String,
    val request: TestRequest,
    val priority: TestPriority,
    val submittedAt: Long,
    val retryCount: Int
)

/**
 * 运行中测试数据类
 */
@Serializable
data class RunningTest(
    val id: String,
    val request: TestRequest,
    val startTime: Long,
    val status: TestExecutionStatus,
    val progress: Double
)

/**
 * 完成测试数据类
 */
@Serializable
data class CompletedTest(
    val id: String,
    val request: TestRequest,
    val result: TestExecutionResult,
    val startTime: Long,
    val endTime: Long
)

/**
 * 测试执行结果数据类
 */
@Serializable
data class TestExecutionResult(
    val success: Boolean,
    val message: String,
    val executionTime: Long,
    val details: Map<String, String> = emptyMap()
)

/**
 * 测试运行器指标数据类
 */
@Serializable
data class TestRunnerMetrics(
    val totalSubmitted: Int = 0,
    val totalStarted: Int = 0,
    val totalCompleted: Int = 0,
    val totalFailed: Int = 0,
    val lastHeartbeat: Long = getCurrentTimeMillis()
)

/**
 * 测试运行器统计数据类
 */
@Serializable
data class TestRunnerStats(
    val queuedTests: Int,
    val runningTests: Int,
    val completedTests: Int,
    val totalSubmitted: Int,
    val totalStarted: Int,
    val totalCompleted: Int,
    val totalFailed: Int,
    val successRate: Double,
    val averageExecutionTime: Long,
    val lastHeartbeat: Long
)
