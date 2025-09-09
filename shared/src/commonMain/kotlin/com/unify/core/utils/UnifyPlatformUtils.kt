package com.unify.core.utils

/**
 * 跨平台工具类 - 替代平台特定API
 */
expect object UnifyPlatformUtils {
    /**
     * 获取当前时间戳（毫秒）
     */
    fun currentTimeMillis(): Long

    /**
     * 触发垃圾回收
     */
    fun gc()

    /**
     * 获取已使用内存（字节）
     */
    fun getUsedMemory(): Long

    /**
     * 获取总内存（字节）
     */
    fun getTotalMemory(): Long

    /**
     * 获取最大内存（字节）
     */
    fun getMaxMemory(): Long

    /**
     * 格式化浮点数
     */
    fun formatFloat(
        value: Float,
        decimals: Int = 1,
    ): String

    /**
     * 格式化双精度浮点数
     */
    fun formatDouble(
        value: Double,
        decimals: Int = 1,
    ): String
}
