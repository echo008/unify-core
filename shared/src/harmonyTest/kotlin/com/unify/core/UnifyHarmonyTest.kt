package com.unify.core

import com.unify.data.UnifyDataManager
import com.unify.ui.components.platform.UnifyPlatformAdapters
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * HarmonyOS平台特定测试
 */
class UnifyHarmonyTest {
    
    private val dataManager = UnifyDataManager()
    private val platformAdapters = UnifyPlatformAdapters()
    
    @Test
    fun testHarmonyDataManager() = runTest {
        // 测试HarmonyOS分布式数据存储
        dataManager.putString("harmony_test_key", "harmony_test_value")
        val value = dataManager.getString("harmony_test_key")
        assertEquals("harmony_test_value", value)
        
        // 测试数据观察
        val flow = dataManager.observeString("harmony_test_key")
        assertNotNull(flow)
    }
    
    @Test
    fun testHarmonyPlatformAdapters() = runTest {
        val deviceInfo = platformAdapters.getDeviceInfo()
        
        assertNotNull(deviceInfo)
        assertNotNull(deviceInfo.deviceId)
        assertNotNull(deviceInfo.model)
        assertNotNull(deviceInfo.manufacturer)
        assertTrue(deviceInfo.screenWidth > 0)
        assertTrue(deviceInfo.screenHeight > 0)
    }
    
    @Test
    fun testHarmonySystemInfo() = runTest {
        val systemInfo = platformAdapters.getSystemInfo()
        
        assertNotNull(systemInfo)
        assertNotNull(systemInfo.osName)
        assertNotNull(systemInfo.osVersion)
        assertTrue(systemInfo.osName.contains("HarmonyOS") || systemInfo.osName.contains("OpenHarmony"))
    }
    
    @Test
    fun testHarmonyDistributedFeatures() = runTest {
        // 测试HarmonyOS分布式特性
        val hasDistributedData = platformAdapters.hasFeature("distributed_data")
        val hasDistributedScheduler = platformAdapters.hasFeature("distributed_scheduler")
        val hasDistributedHardware = platformAdapters.hasFeature("distributed_hardware")
        
        assertNotNull(hasDistributedData)
        assertNotNull(hasDistributedScheduler)
        assertNotNull(hasDistributedHardware)
    }
    
    @Test
    fun testHarmonyDeviceTypes() = runTest {
        // 测试HarmonyOS设备类型识别
        val deviceType = platformAdapters.getDeviceType()
        
        assertNotNull(deviceType)
        assertTrue(
            deviceType == "phone" || 
            deviceType == "tablet" || 
            deviceType == "tv" || 
            deviceType == "watch" || 
            deviceType == "car" ||
            deviceType == "smart_speaker"
        )
    }
    
    @Test
    fun testHarmonyPermissions() = runTest {
        // 测试HarmonyOS权限系统
        val hasLocationPermission = platformAdapters.hasPermission("ohos.permission.LOCATION")
        val hasCameraPermission = platformAdapters.hasPermission("ohos.permission.CAMERA")
        val hasMicrophonePermission = platformAdapters.hasPermission("ohos.permission.MICROPHONE")
        
        // 权限状态可能变化，只测试方法调用不崩溃
        assertNotNull(hasLocationPermission)
        assertNotNull(hasCameraPermission)
        assertNotNull(hasMicrophonePermission)
    }
    
    @Test
    fun testHarmonyNetworkInfo() = runTest {
        val networkInfo = platformAdapters.getNetworkInfo()
        
        assertNotNull(networkInfo)
        assertNotNull(networkInfo.type)
    }
    
    @Test
    fun testHarmonyBatteryInfo() = runTest {
        val batteryInfo = platformAdapters.getBatteryInfo()
        
        assertNotNull(batteryInfo)
        assertTrue(batteryInfo.level >= 0)
        assertTrue(batteryInfo.level <= 100)
    }
    
    @Test
    fun testHarmonyStorageInfo() = runTest {
        val storageInfo = platformAdapters.getStorageInfo()
        
        assertNotNull(storageInfo)
        assertTrue(storageInfo.totalSpace > 0)
        assertTrue(storageInfo.freeSpace >= 0)
        assertTrue(storageInfo.freeSpace <= storageInfo.totalSpace)
    }
    
    @Test
    fun testHarmonyArkUIFeatures() = runTest {
        // 测试ArkUI相关特性
        val hasArkUI = platformAdapters.hasFeature("arkui")
        val hasArkTS = platformAdapters.hasFeature("arkts")
        
        assertNotNull(hasArkUI)
        assertNotNull(hasArkTS)
    }
    
    @Test
    fun testHarmonyDataTypes() = runTest {
        // 测试各种数据类型存储
        dataManager.putInt("harmony_int", 456)
        assertEquals(456, dataManager.getInt("harmony_int"))
        
        dataManager.putBoolean("harmony_bool", false)
        assertEquals(false, dataManager.getBoolean("harmony_bool"))
        
        dataManager.putFloat("harmony_float", 2.71f)
        assertEquals(2.71f, dataManager.getFloat("harmony_float"), 0.001f)
    }
}
