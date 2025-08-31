package com.unify.helloworld

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * HelloWorldApp组件测试
 */
class HelloWorldAppTest {
    
    @Test
    fun testPlatformInfoInterface() {
        // 测试SimplePlatformInfo接口的基本功能
        val platformName = SimplePlatformInfo.getPlatformName()
        val deviceInfo = SimplePlatformInfo.getDeviceInfo()
        
        assertNotNull(platformName, "平台名称不应为空")
        assertNotNull(deviceInfo, "设备信息不应为空")
        
        // 验证平台名称不为空字符串
        assertTrue(platformName.isNotEmpty(), "平台名称不应为空字符串")
        assertTrue(deviceInfo.isNotEmpty(), "设备信息不应为空字符串")
    }
    
    @Test
    fun testPlatformNameValidation() {
        val platformName = SimplePlatformInfo.getPlatformName()
        
        // 验证平台名称是预期的值之一
        val validPlatforms = listOf("Android", "iOS", "Web", "Desktop", "HarmonyOS", "MiniApp", "Watch", "TV")
        assertTrue(validPlatforms.contains(platformName), 
            "平台名称 '$platformName' 不在有效列表中: $validPlatforms")
    }
    
    @Test
    fun testDeviceInfoFormat() {
        val deviceInfo = SimplePlatformInfo.getDeviceInfo()
        
        // 验证设备信息包含有用信息（长度大于3）
        assertTrue(deviceInfo.length > 3, "设备信息过短: '$deviceInfo'")
    }
}
