package com.unify.helloworld

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Android平台特定测试
 */
class AndroidPlatformTest {

    @Test
    fun testAndroidPlatformName() {
        val platformName = SimplePlatformInfo.getPlatformName()
        assertEquals("Android", platformName, "Android平台名称应为'Android'")
    }

    @Test
    fun testAndroidDeviceInfo() {
        val deviceInfo = SimplePlatformInfo.getDeviceInfo()

        // Android设备信息应包含API级别
        assertTrue(deviceInfo.contains("API"), "Android设备信息应包含API级别")

        // 设备信息应包含制造商和型号信息
        assertTrue(deviceInfo.isNotEmpty(), "Android设备信息不应为空")
    }
}
