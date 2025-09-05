package com.unify.core.error

/**
 * Web/JS平台错误实现
 */
actual class PlatformError actual constructor(message: String, cause: Throwable?) : Throwable(message, cause)
