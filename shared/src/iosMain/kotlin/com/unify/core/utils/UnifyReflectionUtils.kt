package com.unify.core.utils

import kotlin.reflect.KClass

/**
 * iOS平台反射工具实现
 */
actual object UnifyReflectionUtils {
    actual fun <T : Any> getClassName(obj: T): String {
        return obj::class.simpleName ?: "Unknown"
    }

    actual fun <T : Any> getClassName(kClass: KClass<T>): String {
        return kClass.simpleName ?: "Unknown"
    }
}
