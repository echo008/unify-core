package com.unify.core

import com.unify.data.UnifyDataManager
import com.unify.ui.components.platform.UnifyPlatformAdapters
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Desktop平台特定测试
 */
class UnifyDesktopTest {
    private val dataManager = UnifyDataManager()
    private val platformAdapters = UnifyPlatformAdapters()

    @Test
    fun testDesktopDataManager() =
        runTest {
            // 测试文件存储
            dataManager.putString("desktop_test_key", "desktop_test_value")
            val value = dataManager.getString("desktop_test_key")
            assertEquals("desktop_test_value", value)

            // 测试数据观察
            val flow = dataManager.observeString("desktop_test_key")
            assertNotNull(flow)
        }

    @Test
    fun testDesktopPlatformAdapters() =
        runTest {
            val deviceInfo = platformAdapters.getDeviceInfo()

            assertNotNull(deviceInfo)
            assertNotNull(deviceInfo.deviceId)
            assertNotNull(deviceInfo.model)
            assertTrue(deviceInfo.screenWidth > 0)
            assertTrue(deviceInfo.screenHeight > 0)
        }

    @Test
    fun testDesktopSystemInfo() =
        runTest {
            val systemInfo = platformAdapters.getSystemInfo()

            assertNotNull(systemInfo)
            assertNotNull(systemInfo.osName)
            assertNotNull(systemInfo.osVersion)
            assertTrue(
                systemInfo.osName.contains("Windows") ||
                    systemInfo.osName.contains("Mac") ||
                    systemInfo.osName.contains("Linux"),
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
            val storageInfo = platformAdapters.getStorageInfo()

            assertNotNull(storageInfo)
            assertTrue(storageInfo.totalSpace > 0)
            assertTrue(storageInfo.freeSpace >= 0)
            assertTrue(storageInfo.freeSpace <= storageInfo.totalSpace)
        }

    @Test
    fun testDesktopNetworkInfo() =
        runTest {
            val networkInfo = platformAdapters.getNetworkInfo()

            assertNotNull(networkInfo)
            assertNotNull(networkInfo.type)
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
            val displayInfo = platformAdapters.getDisplayInfo()

            assertNotNull(displayInfo)
            assertTrue(displayInfo.width > 0)
            assertTrue(displayInfo.height > 0)
            assertTrue(displayInfo.density > 0)
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
            dataManager.putInt("test_int", 123)
            assertEquals(123, dataManager.getInt("test_int"))

            dataManager.putLong("test_long", 123456789L)
            assertEquals(123456789L, dataManager.getLong("test_long"))

            dataManager.putFloat("test_float", 3.14f)
            assertEquals(3.14f, dataManager.getFloat("test_float"), 0.001f)

            dataManager.putDouble("test_double", 3.14159)
            assertEquals(3.14159, dataManager.getDouble("test_double"), 0.00001)

            dataManager.putBoolean("test_boolean", true)
            assertEquals(true, dataManager.getBoolean("test_boolean"))
        }
}
