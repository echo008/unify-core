package com.unify.core.utils

import kotlin.reflect.KClass

/**
 * 跨平台反射工具类
 */
expect object UnifyReflectionUtils {
    /**
     * 获取类的简单名称
     */
    fun <T : Any> getClassName(obj: T): String

    /**
     * 获取KClass的简单名称
     */
    fun <T : Any> getClassName(kClass: KClass<T>): String
}
