package com.unify.core

import com.unify.data.UnifyDataManager
import com.unify.ui.components.platform.UnifyPlatformAdapters
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * TV平台特定测试
 */
class UnifyTVTest {
    
    private val dataManager = UnifyDataManager()
    private val platformAdapters = UnifyPlatformAdapters()
    
    @Test
    fun testTVDataManager() = runTest {
        // 测试TV存储API
        dataManager.putString("tv_test_key", "tv_test_value")
        val value = dataManager.getString("tv_test_key")
        assertEquals("tv_test_value", value)
        
        // 测试数据观察
        val flow = dataManager.observeString("tv_test_key")
        assertNotNull(flow)
    }
    
    @Test
    fun testTVPlatformAdapters() = runTest {
        val deviceInfo = platformAdapters.getDeviceInfo()
        
        assertNotNull(deviceInfo)
        assertNotNull(deviceInfo.deviceId)
        assertNotNull(deviceInfo.model)
        assertTrue(deviceInfo.screenWidth > 0)
        assertTrue(deviceInfo.screenHeight > 0)
        // TV屏幕通常较大
        assertTrue(deviceInfo.screenWidth >= 1280)
        assertTrue(deviceInfo.screenHeight >= 720)
    }
    
    @Test
    fun testTVSystemInfo() = runTest {
        val systemInfo = platformAdapters.getSystemInfo()
        
        assertNotNull(systemInfo)
        assertNotNull(systemInfo.osName)
        assertNotNull(systemInfo.osVersion)
        assertTrue(
            systemInfo.osName.contains("Android TV") || 
            systemInfo.osName.contains("tvOS") ||
            systemInfo.osName.contains("webOS") ||
            systemInfo.osName.contains("Tizen") ||
            systemInfo.osName.contains("HarmonyOS")
        )
    }
    
    @Test
    fun testTVInputFeatures() = runTest {
        // 测试TV输入功能
        val hasRemoteControl = platformAdapters.hasFeature("remote_control")
        val hasVoiceControl = platformAdapters.hasFeature("voice_control")
        val hasGamepad = platformAdapters.hasFeature("gamepad")
        val hasKeyboard = platformAdapters.hasFeature("keyboard")
        
        assertNotNull(hasRemoteControl)
        assertNotNull(hasVoiceControl)
        assertNotNull(hasGamepad)
        assertNotNull(hasKeyboard)
    }
    
    @Test
    fun testTVDisplayFeatures() = runTest {
        // 测试TV显示功能
        val hasHDR = platformAdapters.hasFeature("hdr")
        val has4K = platformAdapters.hasFeature("4k")
        val has8K = platformAdapters.hasFeature("8k")
        val hasDolbyVision = platformAdapters.hasFeature("dolby_vision")
        
        assertNotNull(hasHDR)
        assertNotNull(has4K)
        assertNotNull(has8K)
        assertNotNull(hasDolbyVision)
    }
    
    @Test
    fun testTVAudioFeatures() = runTest {
        // 测试TV音频功能
        val hasDolbyAtmos = platformAdapters.hasFeature("dolby_atmos")
        val hasDTS = platformAdapters.hasFeature("dts")
        val hasSurroundSound = platformAdapters.hasFeature("surround_sound")
        val hasAudioReturn = platformAdapters.hasFeature("audio_return_channel")
        
        assertNotNull(hasDolbyAtmos)
        assertNotNull(hasDTS)
        assertNotNull(hasSurroundSound)
        assertNotNull(hasAudioReturn)
    }
    
    @Test
    fun testTVConnectivity() = runTest {
        // 测试TV连接性
        val hasHDMI = platformAdapters.hasFeature("hdmi")
        val hasUSB = platformAdapters.hasFeature("usb")
        val hasEthernet = platformAdapters.hasFeature("ethernet")
        val hasWifi = platformAdapters.hasFeature("wifi")
        val hasBluetooth = platformAdapters.hasFeature("bluetooth")
        
        assertNotNull(hasHDMI)
        assertNotNull(hasUSB)
        assertNotNull(hasEthernet)
        assertNotNull(hasWifi)
        assertNotNull(hasBluetooth)
    }
    
    @Test
    fun testTVNetworkInfo() = runTest {
        val networkInfo = platformAdapters.getNetworkInfo()
        
        assertNotNull(networkInfo)
        assertNotNull(networkInfo.type)
        assertTrue(
            networkInfo.type == "wifi" || 
            networkInfo.type == "ethernet" ||
            networkInfo.type == "unknown"
        )
    }
    
    @Test
    fun testTVStorageInfo() = runTest {
        val storageInfo = platformAdapters.getStorageInfo()
        
        assertNotNull(storageInfo)
        assertTrue(storageInfo.totalSpace > 0)
        assertTrue(storageInfo.freeSpace >= 0)
        assertTrue(storageInfo.freeSpace <= storageInfo.totalSpace)
    }
    
    @Test
    fun testTVDisplayInfo() = runTest {
        val displayInfo = platformAdapters.getDisplayInfo()
        
        assertNotNull(displayInfo)
        assertTrue(displayInfo.width > 0)
        assertTrue(displayInfo.height > 0)
        assertTrue(displayInfo.density > 0)
        // TV通常支持多种分辨率
        assertTrue(displayInfo.width >= 1280)
        assertTrue(displayInfo.height >= 720)
    }
    
    @Test
    fun testTVDataTypes() = runTest {
        // 测试各种数据类型存储
        dataManager.putInt("tv_int", 654)
        assertEquals(654, dataManager.getInt("tv_int"))
        
        dataManager.putBoolean("tv_bool", true)
        assertEquals(true, dataManager.getBoolean("tv_bool"))
        
        dataManager.putFloat("tv_float", 5.43f)
        assertEquals(5.43f, dataManager.getFloat("tv_float"), 0.001f)
    }
    
    @Test
    fun testTVSpecificFeatures() = runTest {
        // 测试TV特有功能
        val hasSmartTV = platformAdapters.hasFeature("smart_tv")
        val hasAppStore = platformAdapters.hasFeature("app_store")
        val hasScreenMirroring = platformAdapters.hasFeature("screen_mirroring")
        val hasChromecast = platformAdapters.hasFeature("chromecast")
        val hasAirPlay = platformAdapters.hasFeature("airplay")
        
        assertNotNull(hasSmartTV)
        assertNotNull(hasAppStore)
        assertNotNull(hasScreenMirroring)
        assertNotNull(hasChromecast)
        assertNotNull(hasAirPlay)
    }
}
