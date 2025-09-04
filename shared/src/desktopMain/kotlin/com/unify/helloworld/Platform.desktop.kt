package com.unify.helloworld

actual fun getPlatformName(): String = "Desktop"

actual fun getDeviceInfo(): String {
    return buildString {
        append("OS: ${System.getProperty("os.name")}\n")
        append("Version: ${System.getProperty("os.version")}\n")
        append("Architecture: ${System.getProperty("os.arch")}\n")
        append("Java Version: ${System.getProperty("java.version")}\n")
        append("Available Processors: ${Runtime.getRuntime().availableProcessors()}\n")
        append("Max Memory: ${Runtime.getRuntime().maxMemory() / 1024 / 1024} MB")
    }
}
