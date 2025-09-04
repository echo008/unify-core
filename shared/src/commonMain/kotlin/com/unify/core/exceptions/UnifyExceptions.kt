package com.unify.core.exceptions

import kotlinx.serialization.Serializable

/**
 * Unify框架基础异常类
 */
sealed class UnifyException(
    override val message: String,
    val errorCode: String,
    val errorType: UnifyExceptionType,
    val causeMessage: String? = null
) : Exception(message)

/**
 * 初始化异常
 */
class UnifyInitializationException(
    message: String,
    cause: String? = null
) : UnifyException(
    message = message,
    errorCode = "INIT_ERROR",
    errorType = UnifyExceptionType.INITIALIZATION,
    causeMessage = cause
)

/**
 * 配置异常
 */
class UnifyConfigurationException(
    message: String,
    cause: String? = null
) : UnifyException(
    message = message,
    errorCode = "CONFIG_ERROR",
    errorType = UnifyExceptionType.CONFIGURATION,
    causeMessage = cause
)

/**
 * 网络异常
 */
class UnifyNetworkException(
    message: String,
    cause: String? = null
) : UnifyException(
    message = message,
    errorCode = "NETWORK_ERROR",
    errorType = UnifyExceptionType.NETWORK,
    causeMessage = cause
)

/**
 * 数据异常
 */
class UnifyDataException(
    message: String,
    cause: String? = null
) : UnifyException(
    message = message,
    errorCode = "DATA_ERROR",
    errorType = UnifyExceptionType.DATA,
    causeMessage = cause
)

/**
 * UI异常
 */
class UnifyUIException(
    message: String,
    cause: String? = null
) : UnifyException(
    message = message,
    errorCode = "UI_ERROR",
    errorType = UnifyExceptionType.UI,
    causeMessage = cause
)

/**
 * 平台异常
 */
class UnifyPlatformException(
    message: String,
    cause: String? = null
) : UnifyException(
    message = message,
    errorCode = "PLATFORM_ERROR",
    errorType = UnifyExceptionType.PLATFORM,
    causeMessage = cause
)

/**
 * 权限异常
 */
class UnifyPermissionException(
    message: String,
    cause: String? = null
) : UnifyException(
    message = message,
    errorCode = "PERMISSION_ERROR",
    errorType = UnifyExceptionType.PERMISSION,
    causeMessage = cause
)

/**
 * 安全异常
 */
class UnifySecurityException(
    message: String,
    cause: String? = null
) : UnifyException(
    message = message,
    errorCode = "SECURITY_ERROR",
    errorType = UnifyExceptionType.SECURITY,
    causeMessage = cause
)

/**
 * 性能异常
 */
class UnifyPerformanceException(
    message: String,
    cause: String? = null
) : UnifyException(
    message = message,
    errorCode = "PERFORMANCE_ERROR",
    errorType = UnifyExceptionType.PERFORMANCE,
    causeMessage = cause
)

/**
 * 存储异常
 */
class UnifyStorageException(
    message: String,
    cause: String? = null
) : UnifyException(
    message = message,
    errorCode = "STORAGE_ERROR",
    errorType = UnifyExceptionType.STORAGE,
    causeMessage = cause
)

/**
 * 转换异常
 */
class UnifyTransformException(
    message: String,
    cause: String? = null
) : UnifyException(
    message = message,
    errorCode = "TRANSFORM_ERROR",
    errorType = UnifyExceptionType.TRANSFORM,
    causeMessage = cause
)

/**
 * 未知异常
 */
class UnifyUnknownException(
    message: String,
    cause: String? = null
) : UnifyException(
    message = message,
    errorCode = "UNKNOWN_ERROR",
    errorType = UnifyExceptionType.UNKNOWN,
    causeMessage = cause
)

/**
 * 异常类型枚举
 */
@Serializable
enum class UnifyExceptionType {
    INITIALIZATION,
    CONFIGURATION,
    NETWORK,
    DATA,
    UI,
    PLATFORM,
    PERMISSION,
    SECURITY,
    PERFORMANCE,
    STORAGE,
    TRANSFORM,
    UNKNOWN
}

/**
 * 错误响应数据类
 */
@Serializable
data class UnifyErrorResponse(
    val errorCode: String,
    val message: String,
    val type: UnifyExceptionType,
    val timestamp: Long,
    val cause: String? = null,
    val stackTrace: List<String> = emptyList()
)
