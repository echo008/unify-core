package com.unify.core

import com.unify.data.UnifyDataManager
import com.unify.ui.components.platform.UnifyPlatformAdapters
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Web平台特定测试
 */
class UnifyWebTest {
    
    private val dataManager = UnifyDataManager()
    private val platformAdapters = UnifyPlatformAdapters()
    
    @Test
    fun testWebDataManager() = runTest {
        // 测试localStorage存储
        dataManager.putString("web_test_key", "web_test_value")
        val value = dataManager.getString("web_test_key")
        assertEquals("web_test_value", value)
        
        // 测试数据观察
        val flow = dataManager.observeString("web_test_key")
        assertNotNull(flow)
    }
    
    @Test
    fun testWebPlatformAdapters() = runTest {
        val deviceInfo = platformAdapters.getDeviceInfo()
        
        assertNotNull(deviceInfo)
        assertNotNull(deviceInfo.deviceId)
        assertNotNull(deviceInfo.model)
        assertTrue(deviceInfo.screenWidth > 0)
        assertTrue(deviceInfo.screenHeight > 0)
    }
    
    @Test
    fun testWebSystemInfo() = runTest {
        val systemInfo = platformAdapters.getSystemInfo()
        
        assertNotNull(systemInfo)
        assertNotNull(systemInfo.osName)
        assertNotNull(systemInfo.osVersion)
        assertNotNull(systemInfo.browserName)
        assertNotNull(systemInfo.browserVersion)
    }
    
    @Test
    fun testWebNetworkInfo() = runTest {
        val networkInfo = platformAdapters.getNetworkInfo()
        
        assertNotNull(networkInfo)
        assertNotNull(networkInfo.type)
        // Web环境下网络信息可能有限
    }
    
    @Test
    fun testWebStorageInfo() = runTest {
        val storageInfo = platformAdapters.getStorageInfo()
        
        assertNotNull(storageInfo)
        // Web环境下存储信息可能有限，只测试不崩溃
    }
    
    @Test
    fun testWebPermissions() = runTest {
        // Web权限测试
        val hasLocationPermission = platformAdapters.hasPermission("geolocation")
        val hasCameraPermission = platformAdapters.hasPermission("camera")
        val hasMicrophonePermission = platformAdapters.hasPermission("microphone")
        
        // 权限状态可能变化，只测试方法调用不崩溃
        assertNotNull(hasLocationPermission)
        assertNotNull(hasCameraPermission)
        assertNotNull(hasMicrophonePermission)
    }
    
    @Test
    fun testWebSpecificFeatures() = runTest {
        // 测试Web特有功能
        val hasWebGL = platformAdapters.hasFeature("webgl")
        val hasWebRTC = platformAdapters.hasFeature("webrtc")
        val hasServiceWorker = platformAdapters.hasFeature("service_worker")
        
        assertNotNull(hasWebGL)
        assertNotNull(hasWebRTC)
        assertNotNull(hasServiceWorker)
    }
    
    @Test
    fun testWebDisplayInfo() = runTest {
        val displayInfo = platformAdapters.getDisplayInfo()
        
        assertNotNull(displayInfo)
        assertTrue(displayInfo.width > 0)
        assertTrue(displayInfo.height > 0)
        assertTrue(displayInfo.density > 0)
    }
    
    @Test
    fun testWebUserAgent() = runTest {
        val userAgent = platformAdapters.getUserAgent()
        
        assertNotNull(userAgent)
        assertTrue(userAgent.isNotEmpty())
    }
    
    @Test
    fun testWebLocalStorage() = runTest {
        // 直接测试localStorage功能
        dataManager.putInt("test_int", 42)
        val intValue = dataManager.getInt("test_int")
        assertEquals(42, intValue)
        
        dataManager.putBoolean("test_bool", true)
        val boolValue = dataManager.getBoolean("test_bool")
        assertEquals(true, boolValue)
        
        dataManager.putFloat("test_float", 3.14f)
        val floatValue = dataManager.getFloat("test_float")
        assertEquals(3.14f, floatValue, 0.001f)
    }
}
