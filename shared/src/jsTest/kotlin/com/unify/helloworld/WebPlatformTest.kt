package com.unify.helloworld

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Web平台测试
 * 测试Web平台特定功能和组件
 */
class WebPlatformTest {
    
    @Test
    fun testWebPlatformName() {
        val platform = getPlatform()
        assertEquals("Web", platform.name)
    }
    
    @Test
    fun testWebPlatformInfo() {
        val platformInfo = getPlatformInfo()
        assertNotNull(platformInfo.deviceName)
        assertNotNull(platformInfo.osVersion)
        assertEquals("Web", platformInfo.platformName)
        assertFalse(platformInfo.isAndroid)
        assertFalse(platformInfo.isIOS)
        assertTrue(platformInfo.isWeb)
        assertFalse(platformInfo.isDesktop)
    }
    
    @Test
    fun testWebSpecificFeatures() {
        val platformInfo = getPlatformInfo()
        
        // Web特有功能测试
        assertTrue("Web应该支持DOM", platformInfo.supportedFeatures.contains("dom"))
        assertTrue("Web应该支持LocalStorage", platformInfo.supportedFeatures.contains("localStorage"))
        assertTrue("Web应该支持WebGL", platformInfo.supportedFeatures.contains("webgl"))
        assertTrue("Web应该支持WebRTC", platformInfo.supportedFeatures.contains("webrtc"))
        assertTrue("Web应该支持Service Worker", platformInfo.supportedFeatures.contains("serviceWorker"))
    }
    
    @Test
    fun testWebBrowserInfo() {
        val platformInfo = getPlatformInfo()
        
        // 浏览器信息测试
        assertNotNull("浏览器名称不应为空", platformInfo.browserName)
        assertNotNull("浏览器版本不应为空", platformInfo.browserVersion)
        assertFalse("浏览器名称不应为空字符串", platformInfo.browserName.isBlank())
        assertFalse("浏览器版本不应为空字符串", platformInfo.browserVersion.isBlank())
        
        // 常见浏览器检查
        val commonBrowsers = listOf("Chrome", "Firefox", "Safari", "Edge", "Opera")
        val isKnownBrowser = commonBrowsers.any { browser ->
            platformInfo.browserName.contains(browser, ignoreCase = true)
        }
        assertTrue("应该是已知的浏览器", isKnownBrowser)
    }
    
    @Test
    fun testWebDisplayInfo() {
        val platformInfo = getPlatformInfo()
        
        // 显示信息测试
        assertTrue("屏幕宽度应该大于0", platformInfo.screenWidth > 0)
        assertTrue("屏幕高度应该大于0", platformInfo.screenHeight > 0)
        assertTrue("视口宽度应该大于0", platformInfo.viewportWidth > 0)
        assertTrue("视口高度应该大于0", platformInfo.viewportHeight > 0)
        
        // Web特有的显示特性
        assertTrue("视口宽度应该小于等于屏幕宽度", 
            platformInfo.viewportWidth <= platformInfo.screenWidth)
        assertTrue("视口高度应该小于等于屏幕高度", 
            platformInfo.viewportHeight <= platformInfo.screenHeight)
    }
    
    @Test
    fun testWebNetworkInfo() {
        val platformInfo = getPlatformInfo()
        
        // 网络信息测试
        assertNotNull("网络类型不应为空", platformInfo.networkType)
        assertTrue("应该有网络连接信息", 
            platformInfo.networkType in listOf("ethernet", "wifi", "cellular", "bluetooth", "other", "none"))
        
        // Web特有的网络信息
        assertNotNull("连接类型不应为空", platformInfo.connectionType)
        assertTrue("下行速度应该>=0", platformInfo.downlinkSpeed >= 0.0)
    }
    
    @Test
    fun testWebStorageInfo() {
        val platformInfo = getPlatformInfo()
        
        // Web存储信息测试
        assertTrue("LocalStorage配额应该>0", platformInfo.localStorageQuota > 0)
        assertTrue("SessionStorage配额应该>0", platformInfo.sessionStorageQuota > 0)
        assertTrue("IndexedDB配额应该>0", platformInfo.indexedDBQuota > 0)
        
        // 存储使用情况
        assertTrue("LocalStorage使用量应该>=0", platformInfo.localStorageUsed >= 0)
        assertTrue("SessionStorage使用量应该>=0", platformInfo.sessionStorageUsed >= 0)
        assertTrue("IndexedDB使用量应该>=0", platformInfo.indexedDBUsed >= 0)
    }
    
    @Test
    fun testWebAPISupport() {
        val platformInfo = getPlatformInfo()
        
        // Web API支持测试
        val webAPIs = listOf(
            "geolocation", "notification", "camera", "microphone",
            "clipboard", "fullscreen", "vibration", "battery"
        )
        
        webAPIs.forEach { api ->
            val isSupported = platformInfo.supportedAPIs.contains(api)
            // 不强制要求所有API都支持，但应该有API支持列表
            assertNotNull("API支持列表不应为空", platformInfo.supportedAPIs)
        }
        
        assertTrue("应该至少支持一些Web API", platformInfo.supportedAPIs.isNotEmpty())
    }
    
    @Test
    fun testWebPerformanceMetrics() {
        val platformInfo = getPlatformInfo()
        
        // 性能指标测试
        assertTrue("CPU核心数应该大于0", platformInfo.cpuCores > 0)
        assertTrue("可用内存应该大于0", platformInfo.availableMemoryMB > 0)
        
        // Web特有的性能指标
        assertTrue("页面加载时间应该>=0", platformInfo.pageLoadTime >= 0)
        assertTrue("DOM内容加载时间应该>=0", platformInfo.domContentLoadedTime >= 0)
        assertTrue("首次绘制时间应该>=0", platformInfo.firstPaintTime >= 0)
    }
    
    @Test
    fun testWebSecurityFeatures() {
        val platformInfo = getPlatformInfo()
        
        // Web安全特性测试
        assertNotNull("协议不应为空", platformInfo.protocol)
        assertTrue("应该使用安全协议", 
            platformInfo.protocol in listOf("https:", "http:", "file:"))
        
        // 安全特性检查
        assertTrue("应该支持HTTPS", platformInfo.supportedFeatures.contains("https"))
        assertTrue("应该支持CSP", platformInfo.supportedFeatures.contains("csp"))
        assertTrue("应该支持CORS", platformInfo.supportedFeatures.contains("cors"))
    }
    
    @Test
    fun testWebDeviceOrientation() {
        val platformInfo = getPlatformInfo()
        
        // 设备方向测试（移动Web）
        assertNotNull("设备方向不应为空", platformInfo.deviceOrientation)
        assertTrue("设备方向应该是有效值", 
            platformInfo.deviceOrientation in listOf("portrait", "landscape", "unknown"))
        
        // 方向变化支持
        assertTrue("应该支持方向变化检测", 
            platformInfo.supportedFeatures.contains("orientationchange"))
    }
    
    @Test
    fun testWebUserAgent() {
        val platformInfo = getPlatformInfo()
        
        // User Agent测试
        assertNotNull("User Agent不应为空", platformInfo.userAgent)
        assertFalse("User Agent不应为空字符串", platformInfo.userAgent.isBlank())
        
        // User Agent应该包含浏览器信息
        val userAgent = platformInfo.userAgent.lowercase()
        val browserKeywords = listOf("chrome", "firefox", "safari", "edge", "opera")
        val containsBrowserInfo = browserKeywords.any { keyword ->
            userAgent.contains(keyword)
        }
        assertTrue("User Agent应该包含浏览器信息", containsBrowserInfo)
    }
    
    @Test
    fun testWebLanguageInfo() {
        val platformInfo = getPlatformInfo()
        
        // 语言信息测试
        assertNotNull("语言不应为空", platformInfo.language)
        assertNotNull("语言列表不应为空", platformInfo.languages)
        assertFalse("语言不应为空字符串", platformInfo.language.isBlank())
        assertTrue("语言列表应该包含主语言", 
            platformInfo.languages.contains(platformInfo.language))
    }
    
    @Test
    fun testWebTimezone() {
        val platformInfo = getPlatformInfo()
        
        // 时区信息测试
        assertNotNull("时区不应为空", platformInfo.timezone)
        assertFalse("时区不应为空字符串", platformInfo.timezone.isBlank())
        assertTrue("时区偏移应该在合理范围内", 
            platformInfo.timezoneOffset in -720..840) // -12到+14小时
    }
}
