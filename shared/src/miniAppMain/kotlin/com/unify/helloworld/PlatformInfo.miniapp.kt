package com.unify.helloworld

/**
 * 小程序平台实现
 */
actual fun getPlatformName(): String = "MiniApp"

/**
 * 获取小程序设备详细信息
 */
actual fun getDeviceInfo(): String {
    return buildString {
        append("Platform: MiniApp\n")
        append("Environment: WeChat/Alipay/ByteDance\n")
        append("Runtime: JavaScript V8\n")
        append("API Level: 3.0\n")
        append("Canvas Support: Yes\n")
        append("WebGL Support: Limited\n")
        append("Native Bridge: Available")
    }
}
