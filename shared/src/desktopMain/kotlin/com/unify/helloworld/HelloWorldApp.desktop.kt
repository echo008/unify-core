package com.unify.helloworld

/**
 * Desktop平台Hello World实现
 */
actual fun getPlatformName(): String {
    val osName = System.getProperty("os.name")
    val osVersion = System.getProperty("os.version")
    val osArch = System.getProperty("os.arch")
    return "Desktop ($osName $osVersion, $osArch)"
}
