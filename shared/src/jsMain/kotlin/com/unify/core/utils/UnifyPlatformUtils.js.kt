package com.unify.core.utils

import kotlin.js.Date

/**
 * JS平台工具类实现
 */
actual object UnifyPlatformUtils {
    actual fun currentTimeMillis(): Long {
        return Date.now().toLong()
    }
    
    actual fun gc() {
        // JS引擎自动垃圾回收，无需手动触发
        // 可以尝试清理一些引用来帮助GC
        js("if (typeof window !== 'undefined' && window.gc) { window.gc(); }")
    }
    
    actual fun getUsedMemory(): Long {
        // JS环境中获取内存使用情况
        return js("""
            if (typeof performance !== 'undefined' && performance.memory) {
                return performance.memory.usedJSHeapSize || 0;
            }
            return 0;
        """) as Long
    }
    
    actual fun getTotalMemory(): Long {
        // JS环境中获取总内存
        return js("""
            if (typeof performance !== 'undefined' && performance.memory) {
                return performance.memory.totalJSHeapSize || 0;
            }
            return 0;
        """) as Long
    }
    
    actual fun getMaxMemory(): Long {
        // JS环境中获取最大内存限制
        return js("""
            if (typeof performance !== 'undefined' && performance.memory) {
                return performance.memory.jsHeapSizeLimit || 0;
            }
            return 0;
        """) as Long
    }
    
    actual fun formatFloat(value: Float, decimals: Int): String {
        return value.asDynamic().toFixed(decimals) as String
    }
    
    actual fun formatDouble(value: Double, decimals: Int): String {
        return value.asDynamic().toFixed(decimals) as String
    }
}
