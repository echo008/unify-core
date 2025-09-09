package com.unify.core

import com.unify.data.UnifyDataManagerImpl
import com.unify.ui.components.platform.UnifyPlatformUtils
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Desktop平台特定测试
 */
class UnifyDesktopTest {
    private val dataManager = UnifyDataManagerImpl()

    @Test
    fun testDesktopDataManager() =
        runTest {
            // 测试文件存储
            dataManager.saveString("desktop_test_key", "desktop_test_value")
            val value = dataManager.getString("desktop_test_key")
            assertEquals("desktop_test_value", value)

            // 测试数据观察
            val observedValues = mutableListOf<String>()
            val job = launch {
                dataManager.observeString("desktop_test_key").collect { value ->
                    observedValues.add(value)
                }
            }

            dataManager.saveString("desktop_test_key", "new_value")
            delay(100)
            job.cancel()

            assertTrue(observedValues.contains("new_value"))

            // 测试数据类型
            dataManager.saveInt("int_key", 42)
            assertEquals(42, dataManager.getInt("int_key"))

            dataManager.saveBoolean("bool_key", true)
            assertEquals(true, dataManager.getBoolean("bool_key"))

            dataManager.saveFloat("float_key", 3.14f)
            assertEquals(3.14f, dataManager.getFloat("float_key"))

            dataManager.saveLong("long_key", 123456789L)
            assertEquals(123456789L, dataManager.getLong("long_key"))

            // 测试文件操作
            val testData = "test file content".toByteArray()
            dataManager.saveFile("test.txt", testData)
            assertTrue(dataManager.fileExists("test.txt"))

            val loadedData = dataManager.getFile("test.txt")
            assertNotNull(loadedData)
            assertEquals("test file content", String(loadedData))

            // 清理
            dataManager.deleteFile("test.txt")
            dataManager.remove("desktop_test_key")
            dataManager.remove("int_key")
            dataManager.remove("bool_key")
            dataManager.remove("float_key")
            dataManager.remove("long_key")
        }

    @Test
    fun testDesktopPlatformAdapters() =
        runTest {
            val deviceInfo = UnifyPlatformUtils.getDeviceInfo()

            assertNotNull(deviceInfo)
            assertNotNull(deviceInfo.deviceId)
            assertNotNull(deviceInfo.model)
            assertTrue(deviceInfo.screenWidth > 0)
            assertTrue(deviceInfo.screenHeight > 0)
        }

    @Test
    fun testDesktopSystemInfo() =
        runTest {
            val systemInfo = UnifyPlatformUtils.getSystemInfo()

            assertNotNull(systemInfo)
            assertNotNull(systemInfo.platformType)
            assertNotNull(systemInfo.architecture)
            assertTrue(
                systemInfo.platformType.name.contains("DESKTOP") ||
                    systemInfo.architecture.isNotEmpty()
            )
        }

    @Test
    fun testJavaSystemProperties() {
        val osName = System.getProperty("os.name")
        val osVersion = System.getProperty("os.version")
        val javaVersion = System.getProperty("java.version")

        assertNotNull(osName)
        assertNotNull(osVersion)
        assertNotNull(javaVersion)
    }

    @Test
    fun testDesktopFileSystem() =
        runTest {
            val deviceInfo = UnifyPlatformUtils.getDeviceInfo()

            assertNotNull(deviceInfo)
            assertTrue(deviceInfo.totalStorage > 0)
            assertTrue(deviceInfo.availableStorage >= 0)
            assertTrue(deviceInfo.availableStorage <= deviceInfo.totalStorage)
        }

    @Test
    fun testDesktopNetworkInfo() =
        runTest {
            val systemInfo = UnifyPlatformUtils.getSystemInfo()

            assertNotNull(systemInfo)
            assertNotNull(systemInfo.networkType)
        }

    @Test
    fun testDesktopMemoryInfo() =
        runTest {
            val runtime = Runtime.getRuntime()
            val totalMemory = runtime.totalMemory()
            val freeMemory = runtime.freeMemory()
            val maxMemory = runtime.maxMemory()

            assertTrue(totalMemory > 0)
            assertTrue(freeMemory >= 0)
            assertTrue(maxMemory > 0)
            assertTrue(freeMemory <= totalMemory)
        }

    @Test
    fun testDesktopDisplayInfo() =
        runTest {
            val deviceInfo = UnifyPlatformUtils.getDeviceInfo()

            assertNotNull(deviceInfo)
            assertTrue(deviceInfo.screenWidth > 0)
            assertTrue(deviceInfo.screenHeight > 0)
            assertTrue(deviceInfo.density > 0)
        }

    @Test
    fun testDesktopFileOperations() =
        runTest {
            val tempDir = System.getProperty("java.io.tmpdir")
            val testFile = File(tempDir, "unify_test.txt")

            // 创建测试文件
            testFile.writeText("test content")
            assertTrue(testFile.exists())

            // 读取文件内容
            val content = testFile.readText()
            assertEquals("test content", content)

            // 清理测试文件
            testFile.delete()
        }

    @Test
    fun testDesktopEnvironmentVariables() {
        val userHome = System.getProperty("user.home")
        val userName = System.getProperty("user.name")

        assertNotNull(userHome)
        assertNotNull(userName)
        assertTrue(userHome.isNotEmpty())
        assertTrue(userName.isNotEmpty())
    }

    @Test
    fun testDesktopDataTypes() =
        runTest {
            // 测试各种数据类型存储
            dataManager.saveInt("test_int", 123)
            assertEquals(123, dataManager.getInt("test_int"))

            dataManager.saveLong("test_long", 123456789L)
            assertEquals(123456789L, dataManager.getLong("test_long"))

            dataManager.saveFloat("test_float", 3.14f)
            assertEquals(3.14f, dataManager.getFloat("test_float"))

            dataManager.saveBoolean("test_boolean", true)
            assertEquals(true, dataManager.getBoolean("test_boolean"))

            // 清理测试数据
            dataManager.remove("test_int")
            dataManager.remove("test_long")
            dataManager.remove("test_float")
            dataManager.remove("test_boolean")
        }
}
