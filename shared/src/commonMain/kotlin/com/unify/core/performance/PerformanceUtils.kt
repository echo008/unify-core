package com.unify.core.performance

import com.unify.core.utils.UnifyPlatformUtils

/**
 * 性能工具类
 * 提供性能相关的实用方法
 */
object PerformanceUtils {
    /**
     * 格式化内存大小
     */
    fun formatMemorySize(bytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var size = bytes.toDouble()
        var unitIndex = 0

        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }

        return "${size.toFloat()} ${units[unitIndex]}"
    }

    /**
     * 获取已使用内存
     */
    val usedMemory: Long
        get() = UnifyPlatformUtils.getUsedMemory()

    /**
     * 获取总内存
     */
    val totalMemory: Long
        get() = UnifyPlatformUtils.getTotalMemory()

    /**
     * 获取最大内存
     */
    val maxMemory: Long
        get() = UnifyPlatformUtils.getMaxMemory()

    /**
     * 计算内存使用百分比
     */
    fun getMemoryUsagePercent(): Float {
        val used = usedMemory
        val total = totalMemory
        return if (total > 0) (used.toFloat() / total) * 100f else 0f
    }

    /**
     * 格式化时间间隔
     */
    fun formatDuration(milliseconds: Long): String {
        return when {
            milliseconds < 1000 -> "${milliseconds}ms"
            milliseconds < 60000 -> "${UnifyPlatformUtils.formatDouble(milliseconds / 1000.0, 1)}s"
            else -> "${UnifyPlatformUtils.formatDouble(milliseconds / 60000.0, 1)}m"
        }
    }

    /**
     * 计算平均值
     */
    fun calculateAverage(values: List<Number>): Double {
        return if (values.isEmpty()) 0.0 else values.sumOf { it.toDouble() } / values.size
    }

    /**
     * 计算百分位数
     */
    fun calculatePercentile(
        values: List<Number>,
        percentile: Double,
    ): Double {
        if (values.isEmpty()) return 0.0

        val sorted = values.map { it.toDouble() }.sorted()
        val index = (percentile / 100.0) * (sorted.size - 1)
        val lower = index.toInt()
        val upper = (lower + 1).coerceAtMost(sorted.size - 1)
        val weight = index - lower

        return sorted[lower] * (1 - weight) + sorted[upper] * weight
    }
}
