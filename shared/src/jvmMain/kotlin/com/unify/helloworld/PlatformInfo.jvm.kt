package com.unify.helloworld

actual object PlatformInfo {
    actual fun getPlatformName(): String = "Desktop (JVM)"
    
    actual fun getDeviceInfo(): String {
        val osName = System.getProperty("os.name")
        val osVersion = System.getProperty("os.version")
        val javaVersion = System.getProperty("java.version")
        return "$osName $osVersion (Java $javaVersion)"
    }
}
