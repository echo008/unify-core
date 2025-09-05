package com.unify.core.error

/**
 * Desktop/JVM平台错误实现
 */
actual class PlatformError actual constructor(message: String, cause: Throwable?) : Exception(message, cause)
