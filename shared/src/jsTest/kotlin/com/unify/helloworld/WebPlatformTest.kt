package com.unify.helloworld

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Web平台特定测试
 */
class WebPlatformTest {
    
    @Test
    fun testWebPlatformName() {
        val platformName = SimplePlatformInfo.getPlatformName()
        assertEquals("Web", platformName, "Web平台名称应为'Web'")
    }
    
    @Test
    fun testWebDeviceInfo() {
        val deviceInfo = SimplePlatformInfo.getDeviceInfo()
        
        // Web设备信息应包含用户代理信息
        assertTrue(deviceInfo.isNotEmpty(), "Web设备信息不应为空")
        
        // 用户代理字符串通常包含浏览器信息
        assertTrue(deviceInfo.length > 10, "Web设备信息应包含详细的用户代理信息")
    }
}
