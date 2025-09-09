package com.unify.helloworld

import kotlin.native.Platform

/**
 * Native平台信息实现
 */
@OptIn(kotlin.experimental.ExperimentalNativeApi::class)
actual fun getPlatformName(): String {
    return "Native (${Platform.osFamily})"
}

@OptIn(kotlin.experimental.ExperimentalNativeApi::class)
actual fun getDeviceInfo(): String {
    return buildString {
        append("Platform: Native\n")
        append("OS Family: ${Platform.osFamily}\n")
        append("CPU Architecture: ${Platform.cpuArchitecture}\n")
        append("Memory Model: ${Platform.memoryModel}\n")
        append("Is Debug Binary: ${Platform.isDebugBinary}\n")
    }
}
