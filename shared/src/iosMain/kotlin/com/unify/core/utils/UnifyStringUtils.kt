package com.unify.core.utils

import platform.Foundation.NSString
import platform.Foundation.stringWithFormat

/**
 * iOS平台字符串工具实现
 */
actual object UnifyStringUtils {
    actual fun format(
        format: String,
        vararg args: Any?,
    ): String {
        return when (args.size) {
            0 -> format
            1 -> NSString.stringWithFormat(format, args[0])
            2 -> NSString.stringWithFormat(format, args[0], args[1])
            3 -> NSString.stringWithFormat(format, args[0], args[1], args[2])
            else -> {
                // 对于更多参数，使用简单的字符串替换
                var result = format
                args.forEachIndexed { index, arg ->
                    result = result.replace("%${index + 1}", arg.toString())
                }
                result
            }
        }
    }
}
