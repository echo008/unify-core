package com.unify.helloworld

actual class SimplePlatformInfo {
    actual fun getPlatformName(): String = "Desktop"
    
    actual fun getDeviceInfo(): String {
        return buildString {
            append("OS: ${System.getProperty("os.name")}\n")
            append("Version: ${System.getProperty("os.version")}\n")
            append("Architecture: ${System.getProperty("os.arch")}\n")
            append("Java Version: ${System.getProperty("java.version")}\n")
            append("User: ${System.getProperty("user.name")}\n")
            append("Home: ${System.getProperty("user.home")}\n")
            append("Available Processors: ${Runtime.getRuntime().availableProcessors()}\n")
            append("Max Memory: ${Runtime.getRuntime().maxMemory() / 1024 / 1024} MB\n")
            append("Free Memory: ${Runtime.getRuntime().freeMemory() / 1024 / 1024} MB")
        }
    }
}
