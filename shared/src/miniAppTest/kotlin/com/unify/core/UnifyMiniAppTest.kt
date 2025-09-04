package com.unify.core

import com.unify.data.UnifyDataManager
import com.unify.ui.components.platform.UnifyPlatformAdapters
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * 小程序平台特定测试
 */
class UnifyMiniAppTest {
    
    private val dataManager = UnifyDataManager()
    private val platformAdapters = UnifyPlatformAdapters()
    
    @Test
    fun testMiniAppDataManager() = runTest {
        // 测试小程序存储API
        dataManager.putString("miniapp_test_key", "miniapp_test_value")
        val value = dataManager.getString("miniapp_test_key")
        assertEquals("miniapp_test_value", value)
        
        // 测试数据观察
        val flow = dataManager.observeString("miniapp_test_key")
        assertNotNull(flow)
    }
    
    @Test
    fun testMiniAppPlatformAdapters() = runTest {
        val deviceInfo = platformAdapters.getDeviceInfo()
        
        assertNotNull(deviceInfo)
        assertNotNull(deviceInfo.deviceId)
        assertNotNull(deviceInfo.model)
        assertTrue(deviceInfo.screenWidth > 0)
        assertTrue(deviceInfo.screenHeight > 0)
    }
    
    @Test
    fun testMiniAppSystemInfo() = runTest {
        val systemInfo = platformAdapters.getSystemInfo()
        
        assertNotNull(systemInfo)
        assertNotNull(systemInfo.osName)
        assertNotNull(systemInfo.osVersion)
        assertNotNull(systemInfo.platform)
        assertTrue(
            systemInfo.platform == "wechat" || 
            systemInfo.platform == "alipay" || 
            systemInfo.platform == "bytedance" ||
            systemInfo.platform == "baidu"
        )
    }
    
    @Test
    fun testMiniAppPermissions() = runTest {
        // 测试小程序权限系统
        val hasLocationPermission = platformAdapters.hasPermission("scope.userLocation")
        val hasCameraPermission = platformAdapters.hasPermission("scope.camera")
        val hasRecordPermission = platformAdapters.hasPermission("scope.record")
        
        // 权限状态可能变化，只测试方法调用不崩溃
        assertNotNull(hasLocationPermission)
        assertNotNull(hasCameraPermission)
        assertNotNull(hasRecordPermission)
    }
    
    @Test
    fun testMiniAppNetworkInfo() = runTest {
        val networkInfo = platformAdapters.getNetworkInfo()
        
        assertNotNull(networkInfo)
        assertNotNull(networkInfo.type)
        assertTrue(
            networkInfo.type == "wifi" || 
            networkInfo.type == "2g" || 
            networkInfo.type == "3g" || 
            networkInfo.type == "4g" || 
            networkInfo.type == "5g" ||
            networkInfo.type == "unknown"
        )
    }
    
    @Test
    fun testMiniAppSpecificFeatures() = runTest {
        // 测试小程序特有功能
        val hasPayment = platformAdapters.hasFeature("payment")
        val hasShare = platformAdapters.hasFeature("share")
        val hasLogin = platformAdapters.hasFeature("login")
        val hasSubscribe = platformAdapters.hasFeature("subscribe_message")
        
        assertNotNull(hasPayment)
        assertNotNull(hasShare)
        assertNotNull(hasLogin)
        assertNotNull(hasSubscribe)
    }
    
    @Test
    fun testMiniAppStorageInfo() = runTest {
        val storageInfo = platformAdapters.getStorageInfo()
        
        assertNotNull(storageInfo)
        // 小程序存储限制通常较小
        assertTrue(storageInfo.totalSpace > 0)
        assertTrue(storageInfo.freeSpace >= 0)
    }
    
    @Test
    fun testMiniAppDisplayInfo() = runTest {
        val displayInfo = platformAdapters.getDisplayInfo()
        
        assertNotNull(displayInfo)
        assertTrue(displayInfo.width > 0)
        assertTrue(displayInfo.height > 0)
        assertTrue(displayInfo.density > 0)
        assertTrue(displayInfo.pixelRatio > 0)
    }
    
    @Test
    fun testMiniAppDataTypes() = runTest {
        // 测试各种数据类型存储
        dataManager.putInt("miniapp_int", 789)
        assertEquals(789, dataManager.getInt("miniapp_int"))
        
        dataManager.putBoolean("miniapp_bool", true)
        assertEquals(true, dataManager.getBoolean("miniapp_bool"))
        
        dataManager.putFloat("miniapp_float", 1.23f)
        assertEquals(1.23f, dataManager.getFloat("miniapp_float"), 0.001f)
    }
    
    @Test
    fun testMiniAppPlatformSpecific() = runTest {
        // 测试平台特定功能
        val platformType = platformAdapters.getPlatformType()
        assertTrue(
            platformType == "wechat_miniapp" || 
            platformType == "alipay_miniapp" || 
            platformType == "bytedance_miniapp" ||
            platformType == "baidu_miniapp"
        )
        
        val appId = platformAdapters.getAppId()
        assertNotNull(appId)
        assertTrue(appId.isNotEmpty())
    }
}
