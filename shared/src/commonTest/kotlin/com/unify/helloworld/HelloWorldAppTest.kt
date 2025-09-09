package com.unify.helloworld

import com.unify.testing.TestCase
import com.unify.testing.TestCategory
import com.unify.testing.TestSuite
import com.unify.testing.UnifyAssert
import com.unify.testing.UnifyTestFramework
import com.unify.testing.UnifyTestFrameworkImpl
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Hello World应用测试
 */
class HelloWorldAppTest {
    private val testFramework: UnifyTestFramework = UnifyTestFrameworkImpl()

    @Test
    fun testHelloWorldMessage() =
        runTest {
            val expectedMessage = "Hello, Unify World!"
            val actualMessage = getHelloWorldMessage()

            assertEquals(expectedMessage, actualMessage)
        }

    @Test
    fun testPlatformInfo() =
        runTest {
            val platformInfo = getPlatformInfo()

            assertNotNull(platformInfo)
            assertTrue(platformInfo.isNotEmpty())
        }

    @Test
    fun testUnifyTestFramework() =
        runTest {
            val testCase =
                TestCase(
                    id = "test_hello_world",
                    name = "Hello World Test",
                    description = "测试Hello World功能",
                    category = TestCategory.UNIT,
                )

            val result = testFramework.runTest(testCase)

            assertNotNull(result)
            assertEquals("test_hello_world", result.testCaseId)
        }

    @Test
    fun testUnifyAssertions() {
        // 测试assertEquals
        UnifyAssert.assertEquals("expected", "expected", "字符串相等测试")

        // 测试assertTrue
        UnifyAssert.assertTrue(true, "布尔值真测试")

        // 测试assertFalse
        UnifyAssert.assertFalse(false, "布尔值假测试")

        // 测试assertNull
        UnifyAssert.assertNull(null, "空值测试")

        // 测试assertNotNull
        UnifyAssert.assertNotNull("not null", "非空值测试")
    }

    @Test
    fun testExceptionHandling() {
        UnifyAssert.assertThrows(IllegalArgumentException::class) {
            throw IllegalArgumentException("测试异常")
        }
    }

    @Test
    fun testTestSuiteExecution() =
        runTest {
            val testSuite =
                TestSuite(
                    id = "hello_world_suite",
                    name = "Hello World Test Suite",
                    description = "Hello World应用测试套件",
                    testCases =
                        listOf(
                            TestCase(
                                id = "test_1",
                                name = "Test 1",
                                category = TestCategory.UNIT,
                            ),
                            TestCase(
                                id = "test_2",
                                name = "Test 2",
                                category = TestCategory.INTEGRATION,
                            ),
                        ),
                )

            (testFramework as UnifyTestFrameworkImpl).addTestSuite(testSuite)
            val result = testFramework.runTestSuite(testSuite)

            assertNotNull(result)
            assertEquals("hello_world_suite", result.testSuiteId)
            assertEquals(2, result.totalTests)
        }

    @Test
    fun testReportGeneration() =
        runTest {
            val report = testFramework.generateReport()

            assertNotNull(report)
            assertNotNull(report.summary)
            assertNotNull(report.coverage)
            assertTrue(report.timestamp > 0)
        }

    private fun getHelloWorldMessage(): String {
        return "Hello, Unify World!"
    }

    private fun getPlatformInfo(): String {
        return "Test Platform Info"
    }
}
