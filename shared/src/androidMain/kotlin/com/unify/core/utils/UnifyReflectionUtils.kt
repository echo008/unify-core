package com.unify.core.utils

import kotlin.reflect.KClass

/**
 * Android平台反射工具实现
 */
actual object UnifyReflectionUtils {
    actual fun <T : Any> getClassName(obj: T): String {
        return obj.javaClass.simpleName
    }

    actual fun <T : Any> getClassName(kClass: KClass<T>): String {
        return kClass.java.simpleName
    }
}
