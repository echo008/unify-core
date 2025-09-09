package com.unify.core.utils

import kotlin.reflect.KClass

/**
 * Desktop平台反射工具类actual实现
 */

actual object UnifyReflectionUtils {
    actual fun <T : Any> getClassName(obj: T): String {
        return obj::class.simpleName ?: "Unknown"
    }

    actual fun <T : Any> getClassName(kClass: KClass<T>): String {
        return kClass.simpleName ?: "Unknown"
    }
}
