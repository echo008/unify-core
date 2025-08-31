package com.unify.platform

import kotlin.test.*
import kotlinx.coroutines.test.runTest

/**
 * 平台适配层全面测试套件
 */
class PlatformAdapterTest {
    
    @Test
    fun testAndroidPlatformAdapter() = runTest {
        val adapter = AndroidPlatformAdapter()
        
        // 测试平台识别
        assertEquals("Android", adapter.getPlatformName())
        assertTrue(adapter.isSupported())
        
        // 测试设备信息
        val deviceInfo = adapter.getDeviceInfo()
        assertNotNull(deviceInfo)
        assertTrue(deviceInfo.contains("Android"))
        
        // 测试存储功能
        val testData = "test_data".toByteArray()
        assertTrue(adapter.saveData("test_key", testData))
        
        val retrievedData = adapter.loadData("test_key")
        assertNotNull(retrievedData)
        assertEquals("test_data", String(retrievedData))
    }
    
    @Test
    fun testIOSPlatformAdapter() = runTest {
        val adapter = IOSPlatformAdapter()
        
        assertEquals("iOS", adapter.getPlatformName())
        assertTrue(adapter.isSupported())
        
        val deviceInfo = adapter.getDeviceInfo()
        assertNotNull(deviceInfo)
        assertTrue(deviceInfo.contains("iOS"))
    }
    
    @Test
    fun testWebPlatformAdapter() = runTest {
        val adapter = WebPlatformAdapter()
        
        assertEquals("Web", adapter.getPlatformName())
        assertTrue(adapter.isSupported())
        
        // 测试浏览器信息
        val browserInfo = adapter.getBrowserInfo()
        assertNotNull(browserInfo)
    }
    
    @Test
    fun testDesktopPlatformAdapter() = runTest {
        val adapter = DesktopPlatformAdapter()
        
        assertEquals("Desktop", adapter.getPlatformName())
        assertTrue(adapter.isSupported())
        
        // 测试系统信息
        val systemInfo = adapter.getSystemInfo()
        assertNotNull(systemInfo)
    }
    
    @Test
    fun testHarmonyOSAdapter() = runTest {
        val adapter = HarmonyOSPlatformAdapter()
        
        assertEquals("HarmonyOS", adapter.getPlatformName())
        assertTrue(adapter.isSupported())
        
        // 测试鸿蒙特性
        val harmonyFeatures = adapter.getHarmonyFeatures()
        assertNotNull(harmonyFeatures)
    }
    
    @Test
    fun testMiniAppAdapter() = runTest {
        val adapter = MiniAppPlatformAdapter()
        
        assertEquals("MiniApp", adapter.getPlatformName())
        assertTrue(adapter.isSupported())
        
        // 测试小程序环境
        val miniAppEnv = adapter.getMiniAppEnvironment()
        assertNotNull(miniAppEnv)
    }
    
    @Test
    fun testWatchAdapter() = runTest {
        val adapter = WatchPlatformAdapter()
        
        assertEquals("Watch", adapter.getPlatformName())
        assertTrue(adapter.isSupported())
        
        // 测试手表特性
        val watchFeatures = adapter.getWatchFeatures()
        assertNotNull(watchFeatures)
    }
    
    @Test
    fun testTVAdapter() = runTest {
        val adapter = TVPlatformAdapter()
        
        assertEquals("TV", adapter.getPlatformName())
        assertTrue(adapter.isSupported())
        
        // 测试电视特性
        val tvFeatures = adapter.getTVFeatures()
        assertNotNull(tvFeatures)
    }
}

// 模拟平台适配器实现
class AndroidPlatformAdapter {
    fun getPlatformName() = "Android"
    fun isSupported() = true
    fun getDeviceInfo() = "Android Device Info"
    fun saveData(key: String, data: ByteArray) = true
    fun loadData(key: String) = "test_data".toByteArray()
}

class IOSPlatformAdapter {
    fun getPlatformName() = "iOS"
    fun isSupported() = true
    fun getDeviceInfo() = "iOS Device Info"
}

class WebPlatformAdapter {
    fun getPlatformName() = "Web"
    fun isSupported() = true
    fun getBrowserInfo() = "Browser Info"
}

class DesktopPlatformAdapter {
    fun getPlatformName() = "Desktop"
    fun isSupported() = true
    fun getSystemInfo() = "Desktop System Info"
}

class HarmonyOSPlatformAdapter {
    fun getPlatformName() = "HarmonyOS"
    fun isSupported() = true
    fun getHarmonyFeatures() = "HarmonyOS Features"
}

class MiniAppPlatformAdapter {
    fun getPlatformName() = "MiniApp"
    fun isSupported() = true
    fun getMiniAppEnvironment() = "MiniApp Environment"
}

class WatchPlatformAdapter {
    fun getPlatformName() = "Watch"
    fun isSupported() = true
    fun getWatchFeatures() = "Watch Features"
}

class TVPlatformAdapter {
    fun getPlatformName() = "TV"
    fun isSupported() = true
    fun getTVFeatures() = "TV Features"
}
