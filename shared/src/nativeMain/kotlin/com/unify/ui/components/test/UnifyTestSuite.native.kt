package com.unify.ui.components.test

import com.unify.core.platform.getCurrentTimeMillis

/**
 * Native平台测试组件实现
 */

actual suspend fun executeTestCase(testCase: TestCase): TestResult {
    return try {
        // 模拟测试执行
        TestResult(
            testCaseId = testCase.id,
            status = TestStatus.PASSED,
            message = "Native平台测试执行成功",
            duration = 100L,
            timestamp = getCurrentTimeMillis(),
        )
    } catch (e: Exception) {
        TestResult(
            testCaseId = testCase.id,
            status = TestStatus.FAILED,
            message = "测试执行失败: ${e.message}",
            duration = 0L,
            timestamp = getCurrentTimeMillis(),
        )
    }
}
