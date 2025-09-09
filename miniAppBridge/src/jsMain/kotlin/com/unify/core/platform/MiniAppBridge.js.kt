package com.unify.core.platform

/**
 * JavaScript平台的小程序桥接实现
 * 这是一个简化的实现，仅包含基本的平台检测功能
 */
object MiniAppBridgeJS {
    
    private var isInitialized = false
    
    fun initialize() {
        if (!isInitialized) {
            detectPlatform()
            isInitialized = true
        }
    }
    
    private fun detectPlatform(): String {
        return when {
            js("typeof wx !== 'undefined'") as Boolean -> "WECHAT"
            js("typeof my !== 'undefined'") as Boolean -> "ALIPAY"
            js("typeof tt !== 'undefined'") as Boolean -> "BYTEDANCE"
            js("typeof swan !== 'undefined'") as Boolean -> "BAIDU"
            js("typeof qq !== 'undefined'") as Boolean -> "QQ"
            js("typeof ks !== 'undefined'") as Boolean -> "KUAISHOU"
            else -> "UNKNOWN"
        }
    }
    
    fun getPlatformAPI(): dynamic {
        return when (detectPlatform()) {
            "WECHAT" -> js("wx")
            "ALIPAY" -> js("my")
            "BYTEDANCE" -> js("tt")
            "BAIDU" -> js("swan")
            "QQ" -> js("qq")
            "KUAISHOU" -> js("ks")
            else -> js("{}")
        }
    }
}
