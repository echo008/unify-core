package com.unify.core.error

/**
 * 跨平台异常类型定义
 */
open class UnifyException(message: String, cause: Throwable? = null) : Exception(message, cause)

class UnifyUnknownException(message: String = "Unknown error", cause: Throwable? = null) : UnifyException(message, cause)

class UnifyOutOfMemoryException(message: String = "Out of memory") : UnifyException(message)

class UnifyStackOverflowException(message: String = "Stack overflow") : UnifyException(message)

class UnifyNetworkException(message: String, cause: Throwable? = null) : UnifyException(message, cause)

class UnifyStorageException(message: String, cause: Throwable? = null) : UnifyException(message, cause)

class UnifyPermissionException(message: String) : UnifyException(message)

class UnifyPlatformException(message: String, cause: Throwable? = null) : UnifyException(message, cause)

class UnifyPerformanceException(message: String, cause: Throwable? = null) : UnifyException(message, cause)

class UnifyTransformException(message: String, cause: Throwable? = null) : UnifyException(message, cause)

/**
 * 跨平台错误类型映射
 */
expect class PlatformError(message: String, cause: Throwable? = null) : Throwable

/**
 * 错误类型转换函数
 */
fun Throwable.toUnifyException(): UnifyException {
    return when (this) {
        is UnifyException -> this
        else -> UnifyException(this.message ?: "Unknown error", this)
    }
}
