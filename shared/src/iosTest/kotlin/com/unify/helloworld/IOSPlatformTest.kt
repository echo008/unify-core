package com.unify.helloworld

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * iOS平台特定测试
 */
class IOSPlatformTest {
    
    @Test
    fun testIOSPlatformName() {
        val platformName = SimplePlatformInfo.getPlatformName()
        assertEquals("iOS", platformName, "iOS平台名称应为'iOS'")
    }
    
    @Test
    fun testIOSDeviceInfo() {
        val deviceInfo = SimplePlatformInfo.getDeviceInfo()
        
        // iOS设备信息应包含系统版本
        assertTrue(deviceInfo.contains("iOS") || deviceInfo.contains("iPhone") || deviceInfo.contains("iPad"), 
                  "iOS设备信息应包含iOS相关信息")
        
        assertTrue(deviceInfo.isNotEmpty(), "iOS设备信息不应为空")
    }
}
