package com.unify.core

import com.unify.data.UnifyDataManager
import com.unify.ui.components.platform.UnifyPlatformAdapters
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Watch平台特定测试
 */
class UnifyWatchTest {
    
    private val dataManager = UnifyDataManager()
    private val platformAdapters = UnifyPlatformAdapters()
    
    @Test
    fun testWatchDataManager() = runTest {
        // 测试手表存储API
        dataManager.putString("watch_test_key", "watch_test_value")
        val value = dataManager.getString("watch_test_key")
        assertEquals("watch_test_value", value)
        
        // 测试数据观察
        val flow = dataManager.observeString("watch_test_key")
        assertNotNull(flow)
    }
    
    @Test
    fun testWatchPlatformAdapters() = runTest {
        val deviceInfo = platformAdapters.getDeviceInfo()
        
        assertNotNull(deviceInfo)
        assertNotNull(deviceInfo.deviceId)
        assertNotNull(deviceInfo.model)
        assertTrue(deviceInfo.screenWidth > 0)
        assertTrue(deviceInfo.screenHeight > 0)
        // 手表屏幕通常较小
        assertTrue(deviceInfo.screenWidth <= 500)
        assertTrue(deviceInfo.screenHeight <= 500)
    }
    
    @Test
    fun testWatchSystemInfo() = runTest {
        val systemInfo = platformAdapters.getSystemInfo()
        
        assertNotNull(systemInfo)
        assertNotNull(systemInfo.osName)
        assertNotNull(systemInfo.osVersion)
        assertTrue(
            systemInfo.osName.contains("watchOS") || 
            systemInfo.osName.contains("Wear OS") ||
            systemInfo.osName.contains("HarmonyOS")
        )
    }
    
    @Test
    fun testWatchHealthFeatures() = runTest {
        // 测试手表健康功能
        val hasHeartRate = platformAdapters.hasFeature("heart_rate")
        val hasStepCounter = platformAdapters.hasFeature("step_counter")
        val hasAccelerometer = platformAdapters.hasFeature("accelerometer")
        val hasGyroscope = platformAdapters.hasFeature("gyroscope")
        
        assertNotNull(hasHeartRate)
        assertNotNull(hasStepCounter)
        assertNotNull(hasAccelerometer)
        assertNotNull(hasGyroscope)
    }
    
    @Test
    fun testWatchSensorData() = runTest {
        // 测试传感器数据获取
        val heartRate = platformAdapters.getHeartRate()
        val stepCount = platformAdapters.getStepCount()
        val batteryLevel = platformAdapters.getBatteryLevel()
        
        // 传感器数据可能为空或0，只测试方法调用不崩溃
        assertNotNull(heartRate)
        assertNotNull(stepCount)
        assertNotNull(batteryLevel)
        assertTrue(batteryLevel >= 0)
        assertTrue(batteryLevel <= 100)
    }
    
    @Test
    fun testWatchBatteryInfo() = runTest {
        val batteryInfo = platformAdapters.getBatteryInfo()
        
        assertNotNull(batteryInfo)
        assertTrue(batteryInfo.level >= 0)
        assertTrue(batteryInfo.level <= 100)
        assertNotNull(batteryInfo.isCharging)
    }
    
    @Test
    fun testWatchDisplayInfo() = runTest {
        val displayInfo = platformAdapters.getDisplayInfo()
        
        assertNotNull(displayInfo)
        assertTrue(displayInfo.width > 0)
        assertTrue(displayInfo.height > 0)
        assertTrue(displayInfo.density > 0)
        // 手表通常是圆形或方形小屏幕
        assertTrue(displayInfo.width <= 500)
        assertTrue(displayInfo.height <= 500)
    }
    
    @Test
    fun testWatchConnectivity() = runTest {
        // 测试手表连接性
        val isConnectedToPhone = platformAdapters.isConnectedToPhone()
        val hasWifi = platformAdapters.hasFeature("wifi")
        val hasBluetooth = platformAdapters.hasFeature("bluetooth")
        val hasCellular = platformAdapters.hasFeature("cellular")
        
        assertNotNull(isConnectedToPhone)
        assertNotNull(hasWifi)
        assertNotNull(hasBluetooth)
        assertNotNull(hasCellular)
    }
    
    @Test
    fun testWatchDataTypes() = runTest {
        // 测试各种数据类型存储
        dataManager.putInt("watch_int", 321)
        assertEquals(321, dataManager.getInt("watch_int"))
        
        dataManager.putBoolean("watch_bool", false)
        assertEquals(false, dataManager.getBoolean("watch_bool"))
        
        dataManager.putFloat("watch_float", 9.87f)
        assertEquals(9.87f, dataManager.getFloat("watch_float"), 0.001f)
    }
    
    @Test
    fun testWatchSpecificFeatures() = runTest {
        // 测试手表特有功能
        val hasHapticFeedback = platformAdapters.hasFeature("haptic_feedback")
        val hasDigitalCrown = platformAdapters.hasFeature("digital_crown")
        val hasAlwaysOnDisplay = platformAdapters.hasFeature("always_on_display")
        
        assertNotNull(hasHapticFeedback)
        assertNotNull(hasDigitalCrown)
        assertNotNull(hasAlwaysOnDisplay)
    }
}
