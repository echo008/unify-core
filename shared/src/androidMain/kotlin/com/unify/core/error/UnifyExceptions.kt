package com.unify.core.error

/**
 * Android平台错误实现
 */
actual class PlatformError actual constructor(message: String, cause: Throwable?) : Throwable(message, cause)
