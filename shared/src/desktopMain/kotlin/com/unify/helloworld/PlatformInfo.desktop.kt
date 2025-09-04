package com.unify.helloworld

/**
 * Desktop平台实现
 */
actual fun getPlatformName(): String = "Desktop"

/**
 * 获取Desktop设备详细信息
 */
actual fun getDeviceInfo(): String {
    return buildString {
        append("OS: ${System.getProperty("os.name")}\n")
        append("Version: ${System.getProperty("os.version")}\n")
        append("Architecture: ${System.getProperty("os.arch")}\n")
        append("Java Version: ${System.getProperty("java.version")}\n")
        append("Java Vendor: ${System.getProperty("java.vendor")}\n")
        append("User: ${System.getProperty("user.name")}\n")
        append("Home: ${System.getProperty("user.home")}\n")
        append("Working Directory: ${System.getProperty("user.dir")}")
    }
}
