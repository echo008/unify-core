package com.unify.core.error

/**
 * iOS平台错误实现
 */
actual class PlatformError actual constructor(message: String, cause: Throwable?) : Exception(message, cause)
