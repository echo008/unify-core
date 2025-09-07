@file:OptIn(ExperimentalForeignApi::class)

package com.unify.core.utils

import kotlinx.cinterop.*
import platform.Foundation.*
import platform.darwin.*

/**
 * iOS平台工具类实现
 */
actual object UnifyPlatformUtils {
    actual fun currentTimeMillis(): Long {
        return (NSDate().timeIntervalSince1970 * 1000).toLong()
    }
    
    actual fun gc() {
        // iOS自动内存管理，无需手动触发GC
        // 可以调用autoreleasepool来清理自动释放池
        autoreleasepool {
            // 清理自动释放池
        }
    }
    
    actual fun getUsedMemory(): Long {
        // Simplified iOS memory usage - return default value
        return NSProcessInfo.processInfo.physicalMemory.toLong() / 4
    }
    
    actual fun getTotalMemory(): Long {
        // Return physical memory size
        return NSProcessInfo.processInfo.physicalMemory.toLong()
    }
    
    actual fun getMaxMemory(): Long {
        // iOS没有直接的最大内存限制概念，返回物理内存大小
        return NSProcessInfo.processInfo.physicalMemory.toLong()
    }
    
    actual fun formatFloat(value: Float, decimals: Int): String {
        val formatter = NSNumberFormatter()
        formatter.numberStyle = NSNumberFormatterDecimalStyle
        formatter.minimumFractionDigits = decimals.toULong()
        formatter.maximumFractionDigits = decimals.toULong()
        return formatter.stringFromNumber(NSNumber.numberWithFloat(value)) ?: value.toString()
    }
    
    actual fun formatDouble(value: Double, decimals: Int): String {
        val formatter = NSNumberFormatter()
        formatter.numberStyle = NSNumberFormatterDecimalStyle
        formatter.minimumFractionDigits = decimals.toULong()
        formatter.maximumFractionDigits = decimals.toULong()
        return formatter.stringFromNumber(NSNumber.numberWithDouble(value)) ?: value.toString()
    }
}
