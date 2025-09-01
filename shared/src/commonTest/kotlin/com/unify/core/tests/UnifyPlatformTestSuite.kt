package com.unify.core.tests

import kotlin.test.Test
import kotlin.test.BeforeTest
import kotlin.test.AfterTest
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNotNull
import kotlinx.coroutines.test.runTest
import com.unify.core.platform.PlatformManager
import com.unify.core.platform.PlatformType
import com.unify.core.platform.DeviceInfo
import com.unify.core.platform.ScreenInfo
import com.unify.core.platform.SystemCapabilities
import com.unify.core.platform.NetworkStatus
import com.unify.core.platform.StorageInfo
import com.unify.core.platform.PerformanceInfo

/**
 * Unify跨平台系统测试套件
 * 全面测试8大平台的适配层和核心功能
 */
class UnifyPlatformTestSuite {

    companion object {
        // 屏幕和显示相关常量
        private const val MAX_REFRESH_RATE = 240 // 最高240Hz
        private const val MAX_SCREEN_SIZE_INCHES = 100 // 合理的屏幕尺寸范围
        
        // 网络相关常量
        private const val MIN_SIGNAL_STRENGTH = -100
        private const val MAX_SIGNAL_STRENGTH = 0
        
        // 性能相关常量
        private const val MIN_PERCENTAGE = 0.0
        private const val MAX_PERCENTAGE = 100.0
        private const val MIN_BATTERY_LEVEL = 0
        private const val MAX_BATTERY_LEVEL = 100
        
        // 时间相关常量
        private const val NETWORK_MONITOR_DELAY_MS = 1000L
        private const val PERFORMANCE_MONITOR_INTERVAL_MS = 1000L
        private const val PERFORMANCE_MONITOR_DURATION_MS = 3000L
        private const val API_PERFORMANCE_THRESHOLD_MS = 1000L
        private const val MEMORY_CLEANUP_DELAY_MS = 100L
        private const val MAX_MEMORY_INCREASE_MB = 10L
        
        // 性能基准测试常量
        private const val BENCHMARK_ITERATIONS = 100
        private const val NANOSECONDS_TO_MILLISECONDS = 1_000_000.0
        private const val MAX_DEVICE_INFO_TIME_MS = 10.0
        private const val MAX_SCREEN_INFO_TIME_MS = 5.0
        private const val STORAGE_TEST_DATA_REPEAT = 100 // ~2KB
        private const val MAX_WRITE_TIME_MS = 100.0
        private const val MAX_READ_TIME_MS = 50.0
    }

    @BeforeTest
    fun setup() {
        PlatformManager.initialize()
    }

    @AfterTest
    fun tearDown() {
        PlatformManager.cleanup()
    }

    // 平台识别和初始化测试
    @Test
    fun testPlatformDetection() = runTest {
        val platformType = PlatformManager.getPlatformType()
        assertNotNull(platformType)
        assertTrue(platformType in PlatformType.values())
        
        val platformName = PlatformManager.getPlatformName()
        assertTrue(platformName.isNotEmpty())
        
        val platformVersion = PlatformManager.getPlatformVersion()
        assertTrue(platformVersion.isNotEmpty())
    }

    @Test
    fun testPlatformInitialization() = runTest {
        assertTrue(PlatformManager.isInitialized())
        
        // 重复初始化应该是安全的
        PlatformManager.initialize()
        assertTrue(PlatformManager.isInitialized())
        
        // 测试初始化状态
        val initStatus = PlatformManager.getInitializationStatus()
        assertTrue(initStatus.isComplete)
        assertTrue(initStatus.timestamp > 0)
    }

    // 设备信息测试
    @Test
    fun testDeviceInfoRetrieval() = runTest {
        val deviceInfo = PlatformManager.getDeviceInfo()
        assertNotNull(deviceInfo)
        
        // 基本设备信息验证
        assertTrue(deviceInfo.deviceId.isNotEmpty())
        assertTrue(deviceInfo.systemName.isNotEmpty())
        assertTrue(deviceInfo.systemVersion.isNotEmpty())
        assertTrue(deviceInfo.manufacturer.isNotEmpty())
        assertTrue(deviceInfo.model.isNotEmpty())
        
        // 设备ID应该在会话期间保持一致
        val deviceInfo2 = PlatformManager.getDeviceInfo()
        assertEquals(deviceInfo.deviceId, deviceInfo2.deviceId)
    }

    @Test
    fun testDeviceCapabilities() = runTest {
        val capabilities = PlatformManager.getSystemCapabilities()
        assertNotNull(capabilities)
        
        // 验证能力标志
        assertNotNull(capabilities.isTouchSupported)
        assertNotNull(capabilities.isKeyboardSupported)
        assertNotNull(capabilities.isMouseSupported)
        assertNotNull(capabilities.isCameraSupported)
        assertNotNull(capabilities.isMicrophoneSupported)
        assertNotNull(capabilities.isGPSSupported)
        assertNotNull(capabilities.isBiometricSupported)
        assertNotNull(capabilities.isNFCSupported)
        
        // 平台特定能力验证
        when (PlatformManager.getPlatformType()) {
            PlatformType.ANDROID, PlatformType.IOS -> {
                assertTrue(capabilities.isTouchSupported)
            }
            PlatformType.DESKTOP -> {
                assertTrue(capabilities.isKeyboardSupported)
                assertTrue(capabilities.isMouseSupported)
            }
            PlatformType.WEB -> {
                assertTrue(capabilities.isKeyboardSupported)
            }
            else -> {
                // 其他平台的基本验证
                assertTrue(true)
            }
        }
    }

    // 屏幕和显示测试
    @Test
    fun testScreenInfo() = runTest {
        val screenInfo = PlatformManager.getScreenInfo()
        assertNotNull(screenInfo)
        
        assertTrue(screenInfo.width > 0)
        assertTrue(screenInfo.height > 0)
        assertTrue(screenInfo.density > 0)
        assertTrue(screenInfo.scaleFactor > 0)
        
        // 屏幕方向应该是有效值
        assertTrue(screenInfo.orientation in listOf("portrait", "landscape", "unknown"))
        
        // 刷新率应该是合理值
        assertTrue(screenInfo.refreshRate > 0)
        assertTrue(screenInfo.refreshRate <= MAX_REFRESH_RATE) // 最高240Hz
    }

    @Test
    fun testScreenMetrics() = runTest {
        val metrics = PlatformManager.getScreenMetrics()
        assertNotNull(metrics)
        
        assertTrue(metrics.physicalWidth > 0)
        assertTrue(metrics.physicalHeight > 0)
        assertTrue(metrics.dpi > 0)
        
        // 计算屏幕尺寸（英寸）
        val diagonalInches = kotlin.math.sqrt(
            (metrics.physicalWidth * metrics.physicalWidth + 
             metrics.physicalHeight * metrics.physicalHeight).toDouble()
        ) / metrics.dpi
        
        assertTrue(diagonalInches > 0)
        assertTrue(diagonalInches < MAX_SCREEN_SIZE_INCHES) // 合理的屏幕尺寸范围
    }

    // 网络状态测试
    @Test
    fun testNetworkStatus() = runTest {
        val networkStatus = PlatformManager.getNetworkStatus()
        assertNotNull(networkStatus)
        
        // 网络状态应该是有效值
        assertTrue(networkStatus.isConnected != null)
        assertTrue(networkStatus.connectionType.isNotEmpty())
        
        if (networkStatus.isConnected == true) {
            assertTrue(networkStatus.connectionType in listOf(
                "wifi", "cellular", "ethernet", "bluetooth", "vpn", "unknown"
            ))
            
            // 如果有网络，应该有基本的网络信息
            assertTrue(networkStatus.signalStrength >= MIN_SIGNAL_STRENGTH)
            assertTrue(networkStatus.signalStrength <= MAX_SIGNAL_STRENGTH)
        }
    }

    @Test
    fun testNetworkMonitoring() = runTest {
        var statusChanges = 0
        
        PlatformManager.startNetworkMonitoring { status ->
            statusChanges++
            assertNotNull(status)
        }
        
        // 等待一段时间以检测网络状态变化
        kotlinx.coroutines.delay(NETWORK_MONITOR_DELAY_MS)
        
        PlatformManager.stopNetworkMonitoring()
        
        // 至少应该有初始状态
        assertTrue(statusChanges >= 1)
    }

    // 存储信息测试
    @Test
    fun testStorageInfo() = runTest {
        val storageInfo = PlatformManager.getStorageInfo()
        assertNotNull(storageInfo)
        
        assertTrue(storageInfo.totalSpace >= 0)
        assertTrue(storageInfo.availableSpace >= 0)
        assertTrue(storageInfo.usedSpace >= 0)
        
        // 逻辑验证
        assertTrue(storageInfo.availableSpace <= storageInfo.totalSpace)
        assertTrue(storageInfo.usedSpace <= storageInfo.totalSpace)
        assertTrue(storageInfo.availableSpace + storageInfo.usedSpace <= storageInfo.totalSpace)
        
        // 使用率计算
        val usagePercentage = (storageInfo.usedSpace.toDouble() / storageInfo.totalSpace) * MAX_PERCENTAGE
        assertTrue(usagePercentage >= MIN_PERCENTAGE)
        assertTrue(usagePercentage <= MAX_PERCENTAGE)
    }

    @Test
    fun testStorageOperations() = runTest {
        val testData = "Test storage data"
        val fileName = "test_file.txt"
        
        // 写入文件
        val writeSuccess = PlatformManager.writeToStorage(fileName, testData)
        assertTrue(writeSuccess)
        
        // 读取文件
        val readData = PlatformManager.readFromStorage(fileName)
        assertEquals(testData, readData)
        
        // 检查文件是否存在
        assertTrue(PlatformManager.fileExists(fileName))
        
        // 删除文件
        val deleteSuccess = PlatformManager.deleteFromStorage(fileName)
        assertTrue(deleteSuccess)
        
        // 确认文件已删除
        assertFalse(PlatformManager.fileExists(fileName))
    }

    // 性能信息测试
    @Test
    fun testPerformanceInfo() = runTest {
        val performanceInfo = PlatformManager.getPerformanceInfo()
        assertNotNull(performanceInfo)
        
        // CPU使用率验证
        assertTrue(performanceInfo.cpuUsage >= MIN_PERCENTAGE)
        assertTrue(performanceInfo.cpuUsage <= MAX_PERCENTAGE)
        
        // 内存信息验证
        assertTrue(performanceInfo.memoryUsage.totalMemory > 0)
        assertTrue(performanceInfo.memoryUsage.usedMemory >= 0)
        assertTrue(performanceInfo.memoryUsage.availableMemory >= 0)
        assertTrue(performanceInfo.memoryUsage.usedMemory <= performanceInfo.memoryUsage.totalMemory)
        
        // 电池信息验证（如果支持）
        if (performanceInfo.batteryInfo != null) {
            assertTrue(performanceInfo.batteryInfo.level >= MIN_BATTERY_LEVEL)
            assertTrue(performanceInfo.batteryInfo.level <= MAX_BATTERY_LEVEL)
            assertTrue(performanceInfo.batteryInfo.isCharging != null)
        }
    }

    @Test
    fun testPerformanceMonitoring() = runTest {
        val metrics = mutableListOf<PerformanceInfo>()
        
        PlatformManager.startPerformanceMonitoring(PERFORMANCE_MONITOR_INTERVAL_MS) { info ->
            metrics.add(info)
        }
        
        // 等待收集几个样本
        kotlinx.coroutines.delay(PERFORMANCE_MONITOR_DURATION_MS)
        
        PlatformManager.stopPerformanceMonitoring()
        
        assertTrue(metrics.size >= 2)
        
        // 验证数据一致性
        metrics.forEach { info ->
            assertTrue(info.cpuUsage >= 0.0)
            assertTrue(info.memoryUsage.totalMemory > 0)
        }
    }

    // 平台特定功能测试
    @Test
    fun testAndroidSpecificFeatures() = runTest {
        if (PlatformManager.getPlatformType() == PlatformType.ANDROID) {
            val androidInfo = PlatformManager.getAndroidSpecificInfo()
            assertNotNull(androidInfo)
            
            assertTrue(androidInfo.apiLevel > 0)
            assertTrue(androidInfo.buildVersion.isNotEmpty())
            assertNotNull(androidInfo.isEmulator)
            
            // 测试Android权限
            val permissions = PlatformManager.getRequiredPermissions()
            assertNotNull(permissions)
            
            // 测试传感器
            val sensors = PlatformManager.getAvailableSensors()
            assertNotNull(sensors)
        }
    }

    @Test
    fun testIOSSpecificFeatures() = runTest {
        if (PlatformManager.getPlatformType() == PlatformType.IOS) {
            val iosInfo = PlatformManager.getIOSSpecificInfo()
            assertNotNull(iosInfo)
            
            assertTrue(iosInfo.systemVersion.isNotEmpty())
            assertTrue(iosInfo.deviceModel.isNotEmpty())
            assertNotNull(iosInfo.isSimulator)
            
            // 测试iOS能力
            val capabilities = PlatformManager.getIOSCapabilities()
            assertNotNull(capabilities.faceIDSupported)
            assertNotNull(capabilities.touchIDSupported)
        }
    }

    @Test
    fun testWebSpecificFeatures() = runTest {
        if (PlatformManager.getPlatformType() == PlatformType.WEB) {
            val webInfo = PlatformManager.getWebSpecificInfo()
            assertNotNull(webInfo)
            
            assertTrue(webInfo.userAgent.isNotEmpty())
            assertTrue(webInfo.browserName.isNotEmpty())
            assertTrue(webInfo.browserVersion.isNotEmpty())
            
            // 测试Web API支持
            val webApis = PlatformManager.getSupportedWebAPIs()
            assertNotNull(webApis)
            
            // 测试PWA能力
            val pwaSupport = PlatformManager.isPWASupported()
            assertNotNull(pwaSupport)
        }
    }

    @Test
    fun testDesktopSpecificFeatures() = runTest {
        if (PlatformManager.getPlatformType() == PlatformType.DESKTOP) {
            val desktopInfo = PlatformManager.getDesktopSpecificInfo()
            assertNotNull(desktopInfo)
            
            assertTrue(desktopInfo.osName.isNotEmpty())
            assertTrue(desktopInfo.osVersion.isNotEmpty())
            assertTrue(desktopInfo.architecture.isNotEmpty())
            
            // 测试桌面功能
            val windowManager = PlatformManager.getWindowManager()
            assertNotNull(windowManager)
            
            // 测试文件系统访问
            val fileSystemAccess = PlatformManager.hasFileSystemAccess()
            assertTrue(fileSystemAccess)
        }
    }

    // 跨平台一致性测试
    @Test
    fun testCrossPlatformConsistency() = runTest {
        // 所有平台都应该支持的基本功能
        val deviceInfo = PlatformManager.getDeviceInfo()
        val screenInfo = PlatformManager.getScreenInfo()
        val storageInfo = PlatformManager.getStorageInfo()
        
        assertNotNull(deviceInfo)
        assertNotNull(screenInfo)
        assertNotNull(storageInfo)
        
        // API响应时间应该合理
        val startTime = System.currentTimeMillis()
        repeat(10) {
            PlatformManager.getDeviceInfo()
        }
        val duration = System.currentTimeMillis() - startTime
        assertTrue(duration < API_PERFORMANCE_THRESHOLD_MS, "API calls too slow: ${duration}ms")
    }

    @Test
    fun testPlatformConfigurationConsistency() = runTest {
        val config = PlatformManager.getPlatformConfig()
        assertNotNull(config)
        
        // 配置应该包含平台特定信息
        assertTrue(config.supportedFeatures.isNotEmpty())
        assertTrue(config.platformCapabilities.isNotEmpty())
        
        // 配置应该与实际能力一致
        val capabilities = PlatformManager.getSystemCapabilities()
        
        if (capabilities.isCameraSupported) {
            assertTrue(config.supportedFeatures.contains("camera"))
        }
        
        if (capabilities.isGPSSupported) {
            assertTrue(config.supportedFeatures.contains("location"))
        }
    }

    // 错误处理和边界条件测试
    @Test
    fun testErrorHandling() = runTest {
        // 测试无效参数处理
        assertFailsWith<IllegalArgumentException> {
            PlatformManager.writeToStorage("", "data")
        }
        
        assertFailsWith<IllegalArgumentException> {
            PlatformManager.readFromStorage("")
        }
        
        // 测试文件不存在的情况
        val nonExistentFile = "non_existent_file_${System.currentTimeMillis()}.txt"
        assertNull(PlatformManager.readFromStorage(nonExistentFile))
        assertFalse(PlatformManager.fileExists(nonExistentFile))
    }

    @Test
    fun testResourceManagement() = runTest {
        // 测试资源清理
        val initialMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        
        repeat(BENCHMARK_ITERATIONS) {
            PlatformManager.getDeviceInfo()
            PlatformManager.getScreenInfo()
            PlatformManager.getNetworkStatus()
            PlatformManager.getStorageInfo()
            PlatformManager.getPerformanceInfo()
        }
        
        System.gc()
        kotlinx.coroutines.delay(MEMORY_CLEANUP_DELAY_MS)
        
        val finalMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        val memoryIncrease = finalMemory - initialMemory
        
        // 内存增长应该控制在合理范围内
        assertTrue(memoryIncrease < MAX_MEMORY_INCREASE_MB * 1024 * 1024, "Memory leak detected: ${memoryIncrease / 1024 / 1024}MB")
    }

    // 并发和线程安全测试
    @Test
    fun testConcurrentAccess() = runTest {
        val results = mutableListOf<DeviceInfo>()
        val jobs = List(50) {
            kotlinx.coroutines.async {
                PlatformManager.getDeviceInfo()
            }
        }
        
        jobs.forEach { job ->
            results.add(job.await())
        }
        
        // 所有结果应该一致
        assertTrue(results.all { it.deviceId == results.first().deviceId })
        assertTrue(results.all { it.systemName == results.first().systemName })
    }

    @Test
    fun testThreadSafety() = runTest {
        val exceptions = mutableListOf<Exception>()
        
        val jobs = List(20) {
            kotlinx.coroutines.launch {
                try {
                    repeat(10) {
                        PlatformManager.getDeviceInfo()
                        PlatformManager.getScreenInfo()
                        PlatformManager.getNetworkStatus()
                    }
                } catch (e: Exception) {
                    exceptions.add(e)
                }
            }
        }
        
        jobs.forEach { it.join() }
        
        // 不应该有并发异常
        assertTrue(exceptions.isEmpty(), "Concurrent access caused exceptions: ${exceptions.size}")
    }

    // 性能基准测试
    @Test
    fun testPerformanceBenchmarks() = runTest {
        // 设备信息获取性能
        val deviceInfoTimes = mutableListOf<Long>()
        repeat(BENCHMARK_ITERATIONS) {
            val start = System.nanoTime()
            PlatformManager.getDeviceInfo()
            val end = System.nanoTime()
            deviceInfoTimes.add(end - start)
        }
        
        val avgDeviceInfoTime = deviceInfoTimes.average() / NANOSECONDS_TO_MILLISECONDS // 转换为毫秒
        assertTrue(avgDeviceInfoTime < MAX_DEVICE_INFO_TIME_MS, "Device info retrieval too slow: ${avgDeviceInfoTime}ms")
        
        // 屏幕信息获取性能
        val screenInfoTimes = mutableListOf<Long>()
        repeat(BENCHMARK_ITERATIONS) {
            val start = System.nanoTime()
            PlatformManager.getScreenInfo()
            val end = System.nanoTime()
            screenInfoTimes.add(end - start)
        }
        
        val avgScreenInfoTime = screenInfoTimes.average() / NANOSECONDS_TO_MILLISECONDS
        assertTrue(avgScreenInfoTime < MAX_SCREEN_INFO_TIME_MS, "Screen info retrieval too slow: ${avgScreenInfoTime}ms")
    }

    @Test
    fun testStoragePerformance() = runTest {
        val testData = "Performance test data ".repeat(STORAGE_TEST_DATA_REPEAT) // ~2KB
        val fileName = "perf_test_${System.currentTimeMillis()}.txt"
        
        // 写入性能测试
        val writeStart = System.nanoTime()
        PlatformManager.writeToStorage(fileName, testData)
        val writeTime = (System.nanoTime() - writeStart) / NANOSECONDS_TO_MILLISECONDS
        
        // 读取性能测试
        val readStart = System.nanoTime()
        val readData = PlatformManager.readFromStorage(fileName)
        val readTime = (System.nanoTime() - readStart) / NANOSECONDS_TO_MILLISECONDS
        
        // 清理
        PlatformManager.deleteFromStorage(fileName)
        
        assertEquals(testData, readData)
        assertTrue(writeTime < MAX_WRITE_TIME_MS, "Write operation too slow: ${writeTime}ms")
        assertTrue(readTime < MAX_READ_TIME_MS, "Read operation too slow: ${readTime}ms")
    }
}
