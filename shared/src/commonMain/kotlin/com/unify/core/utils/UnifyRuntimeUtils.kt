package com.unify.core.utils

/**
 * 跨平台运行时工具类
 */
expect object UnifyRuntimeUtils {
    /**
     * 获取可用内存
     */
    fun getAvailableMemory(): Long

    /**
     * 获取总内存
     */
    fun getTotalMemory(): Long

    /**
     * 触发垃圾回收
     */
    fun gc()

    /**
     * 获取最大内存
     */
    fun getMaxMemory(): Long

    /**
     * 获取线程数量
     */
    fun getThreadCount(): Int

    /**
     * 获取系统负载平均值
     */
    fun getSystemLoadAverage(): Double

    /**
     * 获取可用处理器数量
     */
    fun availableProcessors(): Int
}
