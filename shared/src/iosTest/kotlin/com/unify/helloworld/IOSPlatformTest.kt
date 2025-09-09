package com.unify.helloworld

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * iOS平台测试
 * 测试iOS平台特定功能和组件
 */
class IOSPlatformTest {
    @Test
    fun testIOSPlatformName() {
        val platform = getPlatform()
        assertEquals("iOS", platform.name)
    }

    @Test
    fun testIOSPlatformInfo() {
        val platformInfo = getPlatformInfo()
        assertNotNull(platformInfo.deviceName)
        assertNotNull(platformInfo.osVersion)
        assertEquals("iOS", platformInfo.platformName)
        assertFalse(platformInfo.isAndroid)
        assertTrue(platformInfo.isIOS)
        assertFalse(platformInfo.isWeb)
        assertFalse(platformInfo.isDesktop)
    }

    @Test
    fun testIOSSpecificFeatures() {
        val platformInfo = getPlatformInfo()

        // iOS特有功能测试
        assertTrue("iOS应该支持触摸", platformInfo.supportedFeatures.contains("touch"))
        assertTrue("iOS应该支持相机", platformInfo.supportedFeatures.contains("camera"))
        assertTrue(
            "iOS应该支持Face ID/Touch ID",
            platformInfo.supportedFeatures.contains("biometric"),
        )
        assertTrue(
            "iOS应该支持Core Motion",
            platformInfo.supportedFeatures.contains("motion"),
        )
    }

    @Test
    fun testIOSMemoryInfo() {
        val platformInfo = getPlatformInfo()

        // 内存信息应该大于0
        assertTrue("可用内存应该大于0", platformInfo.availableMemoryMB > 0)
        assertTrue("总内存应该大于0", platformInfo.totalMemoryMB > 0)
        assertTrue("总内存应该大于可用内存", platformInfo.totalMemoryMB >= platformInfo.availableMemoryMB)
    }

    @Test
    fun testIOSStorageInfo() {
        val platformInfo = getPlatformInfo()

        // 存储信息测试
        assertTrue("可用存储应该大于0", platformInfo.availableStorageGB > 0)
        assertTrue("总存储应该大于0", platformInfo.totalStorageGB > 0)
        assertTrue("总存储应该大于可用存储", platformInfo.totalStorageGB >= platformInfo.availableStorageGB)
    }

    @Test
    fun testIOSDeviceInfo() {
        val platformInfo = getPlatformInfo()

        // iOS设备信息测试
        assertNotNull("设备型号不应为空", platformInfo.deviceModel)
        assertFalse("设备型号不应为空字符串", platformInfo.deviceModel.isBlank())
        assertEquals("iOS设备制造商应该是Apple", "Apple", platformInfo.manufacturer)

        // iOS设备型号应该包含iPhone、iPad或iPod
        val validDeviceTypes = listOf("iPhone", "iPad", "iPod")
        val isValidDevice =
            validDeviceTypes.any { type ->
                platformInfo.deviceModel.contains(type, ignoreCase = true)
            }
        assertTrue("应该是有效的iOS设备", isValidDevice)
    }

    @Test
    fun testIOSDisplayInfo() {
        val platformInfo = getPlatformInfo()

        // 显示信息测试
        assertTrue("屏幕宽度应该大于0", platformInfo.screenWidth > 0)
        assertTrue("屏幕高度应该大于0", platformInfo.screenHeight > 0)
        assertTrue("屏幕密度应该大于0", platformInfo.screenDensity > 0f)

        // iOS特有的显示特性
        assertTrue("iOS屏幕密度通常较高", platformInfo.screenDensity >= 2.0f)
    }

    @Test
    fun testIOSBatteryInfo() {
        val platformInfo = getPlatformInfo()

        // 电池信息测试
        assertTrue(
            "电池电量应该在0-100之间",
            platformInfo.batteryLevel in 0..100,
        )
        assertNotNull("充电状态不应为空", platformInfo.chargingStatus)

        // iOS充电状态应该是有效值
        val validChargingStates = listOf("charging", "not_charging", "full", "unknown")
        assertTrue(
            "充电状态应该是有效值",
            platformInfo.chargingStatus in validChargingStates,
        )
    }

    @Test
    fun testIOSNetworkInfo() {
        val platformInfo = getPlatformInfo()

        // 网络信息测试
        assertNotNull("网络类型不应为空", platformInfo.networkType)
        assertTrue(
            "应该有网络连接信息",
            platformInfo.networkType in listOf("WiFi", "Cellular", "None"),
        )
    }

    @Test
    fun testIOSSensorInfo() {
        val platformInfo = getPlatformInfo()

        // 传感器信息测试
        assertNotNull("传感器列表不应为空", platformInfo.availableSensors)
        assertTrue("iOS设备应该有基本传感器", platformInfo.availableSensors.isNotEmpty())

        // iOS常见传感器检查
        val iosSensors = listOf("accelerometer", "gyroscope", "magnetometer", "proximity")
        val hasSomeSensors =
            iosSensors.any { sensor ->
                platformInfo.availableSensors.contains(sensor)
            }
        assertTrue("应该至少有一个iOS传感器", hasSomeSensors)
    }

    @Test
    fun testIOSSecurityFeatures() {
        val platformInfo = getPlatformInfo()

        // iOS安全特性测试
        assertTrue(
            "iOS应该支持生物识别",
            platformInfo.supportedFeatures.contains("biometric"),
        )
        assertTrue(
            "iOS应该支持Keychain",
            platformInfo.supportedFeatures.contains("keychain"),
        )
        assertTrue(
            "iOS应该支持App Transport Security",
            platformInfo.supportedFeatures.contains("ats"),
        )
    }

    @Test
    fun testIOSPerformanceMetrics() {
        val platformInfo = getPlatformInfo()

        // 性能指标测试
        assertTrue("CPU核心数应该大于0", platformInfo.cpuCores > 0)
        assertTrue("CPU频率应该大于0", platformInfo.cpuFrequencyMHz > 0)

        // iOS特有的性能指标
        assertNotNull("GPU信息不应为空", platformInfo.gpuRenderer)
        assertFalse("GPU信息不应为空字符串", platformInfo.gpuRenderer.isBlank())

        // iOS设备通常有较好的性能
        assertTrue("iOS设备CPU核心数通常>=2", platformInfo.cpuCores >= 2)
    }

    @Test
    fun testIOSAppInfo() {
        val platformInfo = getPlatformInfo()

        // iOS应用信息测试
        assertNotNull("应用版本不应为空", platformInfo.appVersion)
        assertNotNull("应用构建号不应为空", platformInfo.buildNumber)
        assertFalse("应用版本不应为空字符串", platformInfo.appVersion.isBlank())
        assertFalse("应用构建号不应为空字符串", platformInfo.buildNumber.isBlank())
    }

    @Test
    fun testIOSSystemInfo() {
        val platformInfo = getPlatformInfo()

        // iOS系统信息测试
        assertNotNull("iOS版本不应为空", platformInfo.osVersion)
        assertFalse("iOS版本不应为空字符串", platformInfo.osVersion.isBlank())

        // iOS版本格式检查（例如：15.0, 16.1等）
        val versionPattern = Regex("""^\d+\.\d+(\.\d+)?$""")
        assertTrue(
            "iOS版本格式应该正确",
            versionPattern.matches(platformInfo.osVersion),
        )
    }
}
