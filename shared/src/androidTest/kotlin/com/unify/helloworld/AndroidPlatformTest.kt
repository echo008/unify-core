package com.unify.helloworld

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Android平台测试
 * 测试Android平台特定功能和组件
 */
@RunWith(AndroidJUnit4::class)
class AndroidPlatformTest {
    @Test
    fun testAndroidPlatformName() {
        val platform = getPlatform()
        assertEquals("Android", platform.name)
    }

    @Test
    fun testAndroidContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.unify.androidApp", appContext.packageName)
    }

    @Test
    fun testAndroidPlatformInfo() {
        val platformInfo = getPlatformInfo()
        assertNotNull(platformInfo.deviceName)
        assertNotNull(platformInfo.osVersion)
        assertEquals("Android", platformInfo.platformName)
        assertTrue(platformInfo.isAndroid)
        assertFalse(platformInfo.isIOS)
        assertFalse(platformInfo.isWeb)
        assertFalse(platformInfo.isDesktop)
    }

    @Test
    fun testAndroidSpecificFeatures() {
        val platformInfo = getPlatformInfo()

        // Android特有功能测试
        assertTrue("Android应该支持触摸", platformInfo.supportedFeatures.contains("touch"))
        assertTrue("Android应该支持相机", platformInfo.supportedFeatures.contains("camera"))
        assertTrue("Android应该支持GPS", platformInfo.supportedFeatures.contains("gps"))
        assertTrue("Android应该支持传感器", platformInfo.supportedFeatures.contains("sensors"))
    }

    @Test
    fun testAndroidMemoryInfo() {
        val platformInfo = getPlatformInfo()

        // 内存信息应该大于0
        assertTrue("可用内存应该大于0", platformInfo.availableMemoryMB > 0)
        assertTrue("总内存应该大于0", platformInfo.totalMemoryMB > 0)
        assertTrue("总内存应该大于可用内存", platformInfo.totalMemoryMB >= platformInfo.availableMemoryMB)
    }

    @Test
    fun testAndroidStorageInfo() {
        val platformInfo = getPlatformInfo()

        // 存储信息测试
        assertTrue("可用存储应该大于0", platformInfo.availableStorageGB > 0)
        assertTrue("总存储应该大于0", platformInfo.totalStorageGB > 0)
        assertTrue("总存储应该大于可用存储", platformInfo.totalStorageGB >= platformInfo.availableStorageGB)
    }

    @Test
    fun testAndroidNetworkInfo() {
        val platformInfo = getPlatformInfo()

        // 网络信息测试
        assertNotNull("网络类型不应为空", platformInfo.networkType)
        assertTrue(
            "应该有网络连接信息",
            platformInfo.networkType in listOf("WiFi", "Mobile", "Ethernet", "None"),
        )
    }

    @Test
    fun testAndroidBatteryInfo() {
        val platformInfo = getPlatformInfo()

        // 电池信息测试（Android特有）
        assertTrue(
            "电池电量应该在0-100之间",
            platformInfo.batteryLevel in 0..100,
        )
        assertNotNull("充电状态不应为空", platformInfo.chargingStatus)
    }

    @Test
    fun testAndroidDisplayInfo() {
        val platformInfo = getPlatformInfo()

        // 显示信息测试
        assertTrue("屏幕宽度应该大于0", platformInfo.screenWidth > 0)
        assertTrue("屏幕高度应该大于0", platformInfo.screenHeight > 0)
        assertTrue("屏幕密度应该大于0", platformInfo.screenDensity > 0f)
    }

    @Test
    fun testAndroidDeviceInfo() {
        val platformInfo = getPlatformInfo()

        // 设备信息测试
        assertNotNull("设备制造商不应为空", platformInfo.manufacturer)
        assertNotNull("设备型号不应为空", platformInfo.deviceModel)
        assertFalse("设备制造商不应为空字符串", platformInfo.manufacturer.isBlank())
        assertFalse("设备型号不应为空字符串", platformInfo.deviceModel.isBlank())
    }

    @Test
    fun testAndroidPermissions() {
        val platformInfo = getPlatformInfo()

        // 权限相关测试
        assertNotNull("权限列表不应为空", platformInfo.grantedPermissions)
        assertTrue("应该有基本权限", platformInfo.grantedPermissions.isNotEmpty())
    }

    @Test
    fun testAndroidSensorInfo() {
        val platformInfo = getPlatformInfo()

        // 传感器信息测试
        assertNotNull("传感器列表不应为空", platformInfo.availableSensors)
        assertTrue("Android设备应该有基本传感器", platformInfo.availableSensors.isNotEmpty())

        // 常见传感器检查
        val commonSensors = listOf("accelerometer", "gyroscope", "magnetometer")
        val hasSomeSensors =
            commonSensors.any { sensor ->
                platformInfo.availableSensors.contains(sensor)
            }
        assertTrue("应该至少有一个常见传感器", hasSomeSensors)
    }

    @Test
    fun testAndroidPerformanceMetrics() {
        val platformInfo = getPlatformInfo()

        // 性能指标测试
        assertTrue("CPU核心数应该大于0", platformInfo.cpuCores > 0)
        assertTrue("CPU频率应该大于0", platformInfo.cpuFrequencyMHz > 0)

        // Android特有的性能指标
        assertNotNull("GPU信息不应为空", platformInfo.gpuRenderer)
        assertFalse("GPU信息不应为空字符串", platformInfo.gpuRenderer.isBlank())
    }
}
