package com.unify.core

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.unify.data.UnifyDataManager
import com.unify.ui.components.platform.UnifyPlatformAdapters
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Android平台特定测试
 */
@RunWith(AndroidJUnit4::class)
class UnifyAndroidTest {
    private lateinit var context: Context
    private lateinit var dataManager: UnifyDataManager
    private lateinit var platformAdapters: UnifyPlatformAdapters

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        dataManager = UnifyDataManager()
        platformAdapters = UnifyPlatformAdapters()
    }

    @Test
    fun testAndroidContext() {
        assertNotNull(context)
        assertEquals("com.unify.core.test", context.packageName)
    }

    @Test
    fun testDataManagerAndroidImplementation() =
        runTest {
            // 测试SharedPreferences存储
            dataManager.putString("test_key", "test_value")
            val value = dataManager.getString("test_key")
            assertEquals("test_value", value)

            // 测试数据观察
            val flow = dataManager.observeString("test_key")
            assertNotNull(flow)
        }

    @Test
    fun testPlatformAdaptersAndroid() =
        runTest {
            val deviceInfo = platformAdapters.getDeviceInfo()

            assertNotNull(deviceInfo)
            assertNotNull(deviceInfo.deviceId)
            assertNotNull(deviceInfo.model)
            assertNotNull(deviceInfo.manufacturer)
            assertTrue(deviceInfo.screenWidth > 0)
            assertTrue(deviceInfo.screenHeight > 0)
        }

    @Test
    fun testAndroidPermissions() =
        runTest {
            val hasPermission = platformAdapters.hasPermission("android.permission.INTERNET")
            // Internet权限通常是默认授予的
            assertTrue(hasPermission)
        }

    @Test
    fun testAndroidSystemInfo() =
        runTest {
            val systemInfo = platformAdapters.getSystemInfo()

            assertNotNull(systemInfo)
            assertNotNull(systemInfo.osName)
            assertNotNull(systemInfo.osVersion)
            assertTrue(systemInfo.osName.contains("Android"))
        }

    @Test
    fun testAndroidNetworkInfo() =
        runTest {
            val networkInfo = platformAdapters.getNetworkInfo()

            assertNotNull(networkInfo)
            assertNotNull(networkInfo.type)
            // 网络状态可能变化，只测试返回值不为空
        }

    @Test
    fun testAndroidBatteryInfo() =
        runTest {
            val batteryInfo = platformAdapters.getBatteryInfo()

            assertNotNull(batteryInfo)
            assertTrue(batteryInfo.level >= 0)
            assertTrue(batteryInfo.level <= 100)
        }

    @Test
    fun testAndroidStorageInfo() =
        runTest {
            val storageInfo = platformAdapters.getStorageInfo()

            assertNotNull(storageInfo)
            assertTrue(storageInfo.totalSpace > 0)
            assertTrue(storageInfo.freeSpace >= 0)
            assertTrue(storageInfo.freeSpace <= storageInfo.totalSpace)
        }

    @Test
    fun testInstrumentationContext() {
        val instrumentationContext = InstrumentationRegistry.getInstrumentation().context
        assertNotNull(instrumentationContext)
    }

    @Test
    fun testAndroidSpecificFeatures() =
        runTest {
            // 测试Android特有功能
            val hasCamera = platformAdapters.hasFeature("android.hardware.camera")
            val hasTouchscreen = platformAdapters.hasFeature("android.hardware.touchscreen")

            // 这些功能在模拟器中可能不存在，只测试方法调用不崩溃
            assertNotNull(hasCamera)
            assertNotNull(hasTouchscreen)
        }
}
