package com.unify.core.utils

/**
 * JS平台字符串工具实现
 */
actual object UnifyStringUtils {
    actual fun format(format: String, vararg args: Any?): String {
        var result = format
        args.forEachIndexed { index, arg ->
            result = result.replace("%s", arg.toString())
            result = result.replace("%d", arg.toString())
            result = result.replace("%f", arg.toString())
            result = result.replace("%${index + 1}", arg.toString())
        }
        return result
    }
}
