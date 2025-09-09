package com.unify.core

import com.unify.data.UnifyDataManager
import com.unify.ui.components.platform.UnifyPlatformAdapters
import kotlinx.coroutines.test.runTest
import platform.Foundation.NSUserDefaults
import platform.UIKit.UIDevice
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * iOS平台特定测试
 */
class UnifyIOSTest {
    private val dataManager = UnifyDataManager()
    private val platformAdapters = UnifyPlatformAdapters()

    @Test
    fun testIOSDataManager() =
        runTest {
            // 测试NSUserDefaults存储
            dataManager.putString("ios_test_key", "ios_test_value")
            val value = dataManager.getString("ios_test_key")
            assertEquals("ios_test_value", value)

            // 测试数据观察
            val flow = dataManager.observeString("ios_test_key")
            assertNotNull(flow)
        }

    @Test
    fun testIOSPlatformAdapters() =
        runTest {
            val deviceInfo = platformAdapters.getDeviceInfo()

            assertNotNull(deviceInfo)
            assertNotNull(deviceInfo.deviceId)
            assertNotNull(deviceInfo.model)
            assertNotNull(deviceInfo.manufacturer)
            assertEquals("Apple", deviceInfo.manufacturer)
            assertTrue(deviceInfo.screenWidth > 0)
            assertTrue(deviceInfo.screenHeight > 0)
        }

    @Test
    fun testIOSSystemInfo() =
        runTest {
            val systemInfo = platformAdapters.getSystemInfo()

            assertNotNull(systemInfo)
            assertNotNull(systemInfo.osName)
            assertNotNull(systemInfo.osVersion)
            assertTrue(systemInfo.osName.contains("iOS") || systemInfo.osName.contains("iPadOS"))
        }

    @Test
    fun testIOSDeviceModel() =
        runTest {
            val device = UIDevice.currentDevice
            val model = device.model
            val systemName = device.systemName
            val systemVersion = device.systemVersion

            assertNotNull(model)
            assertNotNull(systemName)
            assertNotNull(systemVersion)
            assertTrue(systemName == "iOS" || systemName == "iPadOS")
        }

    @Test
    fun testIOSUserDefaults() {
        val userDefaults = NSUserDefaults.standardUserDefaults

        // 测试存储和读取
        userDefaults.setObject("test_value", "test_key")
        val value = userDefaults.stringForKey("test_key")
        assertEquals("test_value", value)

        // 清理测试数据
        userDefaults.removeObjectForKey("test_key")
    }

    @Test
    fun testIOSBatteryInfo() =
        runTest {
            val batteryInfo = platformAdapters.getBatteryInfo()

            assertNotNull(batteryInfo)
            assertTrue(batteryInfo.level >= 0)
            assertTrue(batteryInfo.level <= 100)
        }

    @Test
    fun testIOSNetworkInfo() =
        runTest {
            val networkInfo = platformAdapters.getNetworkInfo()

            assertNotNull(networkInfo)
            assertNotNull(networkInfo.type)
        }

    @Test
    fun testIOSStorageInfo() =
        runTest {
            val storageInfo = platformAdapters.getStorageInfo()

            assertNotNull(storageInfo)
            assertTrue(storageInfo.totalSpace > 0)
            assertTrue(storageInfo.freeSpace >= 0)
            assertTrue(storageInfo.freeSpace <= storageInfo.totalSpace)
        }

    @Test
    fun testIOSPermissions() =
        runTest {
            // iOS权限测试需要特殊处理，这里测试方法调用
            val hasLocationPermission = platformAdapters.hasPermission("location")
            val hasCameraPermission = platformAdapters.hasPermission("camera")

            // 权限状态可能变化，只测试方法调用不崩溃
            assertNotNull(hasLocationPermission)
            assertNotNull(hasCameraPermission)
        }

    @Test
    fun testIOSSpecificFeatures() =
        runTest {
            // 测试iOS特有功能
            val hasFaceID = platformAdapters.hasFeature("face_id")
            val hasTouchID = platformAdapters.hasFeature("touch_id")

            // 这些功能在模拟器中可能不存在，只测试方法调用不崩溃
            assertNotNull(hasFaceID)
            assertNotNull(hasTouchID)
        }

    @Test
    fun testIOSDisplayInfo() =
        runTest {
            val displayInfo = platformAdapters.getDisplayInfo()

            assertNotNull(displayInfo)
            assertTrue(displayInfo.width > 0)
            assertTrue(displayInfo.height > 0)
            assertTrue(displayInfo.density > 0)
        }
}
