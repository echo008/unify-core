package com.unify.core.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Android平台工具类实现
 */
actual object UnifyPlatformUtils {
    actual fun currentTimeMillis(): Long {
        return System.currentTimeMillis()
    }
    
    actual fun gc() {
        System.gc()
    }
    
    actual fun getUsedMemory(): Long {
        val runtime = Runtime.getRuntime()
        return runtime.totalMemory() - runtime.freeMemory()
    }
    
    actual fun getTotalMemory(): Long {
        return Runtime.getRuntime().totalMemory()
    }
    
    actual fun getMaxMemory(): Long {
        return Runtime.getRuntime().maxMemory()
    }
    
    actual fun formatFloat(value: Float, decimals: Int): String {
        val pattern = "0." + "0".repeat(decimals)
        val format = DecimalFormat(pattern, DecimalFormatSymbols(Locale.US))
        return format.format(value)
    }
    
    actual fun formatDouble(value: Double, decimals: Int): String {
        val pattern = "0." + "0".repeat(decimals)
        val format = DecimalFormat(pattern, DecimalFormatSymbols(Locale.US))
        return format.format(value)
    }
}
