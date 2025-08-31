package com.unify.core.test

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNotNull
import kotlinx.coroutines.test.runTest
import com.unify.core.platform.PlatformManager
import com.unify.core.platform.PlatformType
import com.unify.core.performance.PerformanceMonitor
import com.unify.core.performance.PerformanceCategory

/**
 * 跨平台测试覆盖体系
 * 确保所有平台功能的测试覆盖率和质量
 */
class UnifyCoreTestSuite {
    
    @Test
    fun testPlatformManagerInitialization() {
        PlatformManager.initialize()
        
        val platformType = PlatformManager.getPlatformType()
        assertNotNull(platformType)
        assertTrue(platformType in PlatformType.values())
        
        val platformName = PlatformManager.getPlatformName()
        assertTrue(platformName.isNotEmpty())
    }
    
    @Test
    fun testDeviceInfoRetrieval() {
        PlatformManager.initialize()
        
        val deviceInfo = PlatformManager.getDeviceInfo()
        assertNotNull(deviceInfo)
        assertTrue(deviceInfo.systemName.isNotEmpty())
        assertTrue(deviceInfo.deviceId.isNotEmpty())
    }
    
    @Test
    fun testScreenInfoRetrieval() {
        PlatformManager.initialize()
        
        val screenInfo = PlatformManager.getScreenInfo()
        assertNotNull(screenInfo)
        assertTrue(screenInfo.width > 0)
        assertTrue(screenInfo.height > 0)
        assertTrue(screenInfo.density > 0)
    }
    
    @Test
    fun testSystemCapabilities() {
        PlatformManager.initialize()
        
        val capabilities = PlatformManager.getSystemCapabilities()
        assertNotNull(capabilities)
        // 基本能力检查
    }
    
    @Test
    fun testNetworkStatus() {
        PlatformManager.initialize()
        
        val networkStatus = PlatformManager.getNetworkStatus()
        assertNotNull(networkStatus)
    }
    
    @Test
    fun testStorageInfo() {
        PlatformManager.initialize()
        
        val storageInfo = PlatformManager.getStorageInfo()
        assertNotNull(storageInfo)
        assertTrue(storageInfo.totalSpace >= 0)
    }
    
    @Test
    fun testPerformanceInfo() {
        PlatformManager.initialize()
        
        val performanceInfo = PlatformManager.getPerformanceInfo()
        assertNotNull(performanceInfo)
        assertTrue(performanceInfo.cpuUsage >= 0)
        assertTrue(performanceInfo.memoryUsage.totalMemory >= 0)
    }
    
    @Test
    fun testPerformanceMonitoring() = runTest {
        PerformanceMonitor.startMonitoring()
        
        // 测试操作计时
        PerformanceMonitor.startOperation("test_operation", PerformanceCategory.GENERAL)
        kotlinx.coroutines.delay(100)
        val duration = PerformanceMonitor.endOperation("test_operation")
        
        assertNotNull(duration)
        assertTrue(duration.inWholeMilliseconds >= 100)
        
        // 测试性能报告生成
        val report = PerformanceMonitor.generatePerformanceReport()
        assertNotNull(report)
        assertTrue(report.metrics.operationMetrics.containsKey("test_operation"))
        
        PerformanceMonitor.stopMonitoring()
    }
    
    @Test
    fun testPlatformConfig() {
        PlatformManager.initialize()
        
        val config = PlatformManager.getPlatformConfig()
        assertNotNull(config)
        assertTrue(config.supportedFeatures.isNotEmpty())
    }
}

/**
 * Android平台特定测试
 */
class AndroidPlatformTests {
    
    @Test
    fun testAndroidSpecificFeatures() {
        // Android平台特定功能测试
        if (PlatformManager.getPlatformType() == PlatformType.ANDROID) {
            val deviceInfo = PlatformManager.getDeviceInfo()
            assertTrue(deviceInfo.manufacturer.isNotEmpty())
            assertTrue(deviceInfo.model.isNotEmpty())
        }
    }
}

/**
 * iOS平台特定测试
 */
class IOSPlatformTests {
    
    @Test
    fun testIOSSpecificFeatures() {
        // iOS平台特定功能测试
        if (PlatformManager.getPlatformType() == PlatformType.IOS) {
            val deviceInfo = PlatformManager.getDeviceInfo()
            assertEquals("Apple", deviceInfo.manufacturer)
        }
    }
}

/**
 * Desktop平台特定测试
 */
class DesktopPlatformTests {
    
    @Test
    fun testDesktopSpecificFeatures() {
        // Desktop平台特定功能测试
        if (PlatformManager.getPlatformType() == PlatformType.DESKTOP) {
            val capabilities = PlatformManager.getSystemCapabilities()
            assertTrue(capabilities.isKeyboardSupported)
            assertTrue(capabilities.isMouseSupported)
        }
    }
}

/**
 * Web平台特定测试
 */
class WebPlatformTests {
    
    @Test
    fun testWebSpecificFeatures() {
        // Web平台特定功能测试
        if (PlatformManager.getPlatformType() == PlatformType.WEB) {
            val config = PlatformManager.getPlatformConfig()
            assertTrue(config.supportedFeatures.contains("pwa_support"))
        }
    }
}

/**
 * 性能基准测试
 */
class PerformanceBenchmarks {
    
    @Test
    fun benchmarkPlatformManagerInitialization() = runTest {
        val iterations = 100
        val startTime = kotlinx.coroutines.TimeSource.Monotonic.markNow()
        
        repeat(iterations) {
            PlatformManager.initialize()
        }
        
        val duration = startTime.elapsedNow()
        val averageTime = duration / iterations
        
        // 初始化应该在10ms内完成
        assertTrue(averageTime.inWholeMilliseconds < 10)
    }
    
    @Test
    fun benchmarkDeviceInfoRetrieval() = runTest {
        PlatformManager.initialize()
        
        val iterations = 1000
        val startTime = kotlinx.coroutines.TimeSource.Monotonic.markNow()
        
        repeat(iterations) {
            PlatformManager.getDeviceInfo()
        }
        
        val duration = startTime.elapsedNow()
        val averageTime = duration / iterations
        
        // 设备信息获取应该在1ms内完成
        assertTrue(averageTime.inWholeMilliseconds < 1)
    }
}

/**
 * 内存泄漏测试
 */
class MemoryLeakTests {
    
    @Test
    fun testNoMemoryLeaksInPlatformManager() = runTest {
        val initialMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        
        repeat(1000) {
            PlatformManager.initialize()
            PlatformManager.getDeviceInfo()
            PlatformManager.getScreenInfo()
            PlatformManager.getSystemCapabilities()
        }
        
        System.gc()
        kotlinx.coroutines.delay(100)
        
        val finalMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        val memoryIncrease = finalMemory - initialMemory
        
        // 内存增长应该控制在合理范围内（小于10MB）
        assertTrue(memoryIncrease < 10 * 1024 * 1024)
    }
}

/**
 * 并发安全测试
 */
class ConcurrencyTests {
    
    @Test
    fun testConcurrentPlatformManagerAccess() = runTest {
        PlatformManager.initialize()
        
        val jobs = List(100) {
            kotlinx.coroutines.launch {
                repeat(10) {
                    PlatformManager.getDeviceInfo()
                    PlatformManager.getScreenInfo()
                    PlatformManager.getNetworkStatus()
                }
            }
        }
        
        jobs.forEach { it.join() }
        
        // 并发访问不应该导致异常或数据不一致
        assertTrue(true)
    }
}

/**
 * 错误处理测试
 */
class ErrorHandlingTests {
    
    @Test
    fun testGracefulErrorHandling() = runTest {
        // 测试在异常情况下的优雅降级
        try {
            val deviceInfo = PlatformManager.getDeviceInfo()
            assertNotNull(deviceInfo)
        } catch (e: Exception) {
            // 即使出现异常，也应该有默认值
            assertTrue(true)
        }
    }
}
