package com.unify.helloworld

/**
 * 简单平台信息工具
 * 提供基础的平台名称和设备信息
 */
object SimplePlatformInfo {
    fun getPlatformName(): String = getPlatformNameImpl()
    fun getDeviceInfo(): String = getDeviceInfoImpl()
}

expect fun getPlatformNameImpl(): String
expect fun getDeviceInfoImpl(): String
