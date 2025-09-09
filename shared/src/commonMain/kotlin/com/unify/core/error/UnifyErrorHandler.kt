package com.unify.core.error

import com.unify.core.logging.UnifyLog
import com.unify.core.platform.getCurrentTimeMillis
import com.unify.core.types.UnifyResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Unify统一错误处理系统
 * 100% Kotlin Multiplatform实现
 */
data class UnifyErrorReport(
    val id: String = generateErrorId(),
    val exception: Throwable,
    val context: String,
    val timestamp: Long = getCurrentTimeMillis(),
    val stackTrace: String = exception.stackTraceToString(),
    val metadata: Map<String, Any> = emptyMap(),
    val severity: UnifyErrorSeverity = UnifyErrorSeverity.MEDIUM,
    val handled: Boolean = false,
) {
    companion object {
        private var errorCounter = 0

        private fun generateErrorId(): String = "ERR_${getCurrentTimeMillis()}_${++errorCounter}"
    }
}

enum class UnifyErrorSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL,
}

interface UnifyErrorReporter {
    suspend fun reportError(report: UnifyErrorReport): UnifyResult<Unit>

    suspend fun reportException(
        exception: Throwable,
        context: String,
        metadata: Map<String, Any> = emptyMap(),
        severity: UnifyErrorSeverity = UnifyErrorSeverity.MEDIUM,
    ): UnifyResult<String>
}

class UnifyLocalErrorReporter : UnifyErrorReporter {
    private val _errorReports = MutableStateFlow<List<UnifyErrorReport>>(emptyList())
    val errorReports: StateFlow<List<UnifyErrorReport>> = _errorReports.asStateFlow()

    override suspend fun reportError(report: UnifyErrorReport): UnifyResult<Unit> {
        return try {
            // 记录到日志
            UnifyLog.e(
                tag = "ErrorHandler",
                message = "错误报告: ${report.context} - ${report.exception.message}",
                throwable = report.exception,
            )

            // 添加到错误列表
            val currentReports = _errorReports.value.toMutableList()
            currentReports.add(report)

            // 保持最大1000条记录
            if (currentReports.size > 1000) {
                currentReports.removeAt(0)
            }

            _errorReports.value = currentReports

            UnifyResult.Success(Unit)
        } catch (e: Exception) {
            UnifyResult.Failure(
                UnifyUnknownException(
                    message = "错误报告失败: ${e.message}",
                    cause = e,
                ),
            )
        }
    }

    override suspend fun reportException(
        exception: Throwable,
        context: String,
        metadata: Map<String, Any>,
        severity: UnifyErrorSeverity,
    ): UnifyResult<String> {
        return try {
            val report =
                UnifyErrorReport(
                    exception = exception,
                    context = context,
                    metadata = metadata,
                    severity = severity,
                )

            reportError(report)
            UnifyResult.Success(report.id)
        } catch (e: Exception) {
            UnifyResult.Failure(
                UnifyUnknownException(
                    message = "异常报告失败: ${e.message}",
                    cause = e,
                ),
            )
        }
    }

    suspend fun getErrorReports(
        severity: UnifyErrorSeverity? = null,
        limit: Int? = null,
    ): UnifyResult<List<UnifyErrorReport>> {
        return try {
            var reports = _errorReports.value

            if (severity != null) {
                reports = reports.filter { it.severity == severity }
            }

            if (limit != null && limit > 0) {
                reports = reports.takeLast(limit)
            }

            UnifyResult.Success(reports)
        } catch (e: Exception) {
            UnifyResult.Failure(
                UnifyUnknownException(
                    message = "获取错误报告失败: ${e.message}",
                    cause = e,
                ),
            )
        }
    }

    suspend fun clearErrorReports(): UnifyResult<Unit> {
        return try {
            _errorReports.value = emptyList()
            UnifyResult.Success(Unit)
        } catch (e: Exception) {
            UnifyResult.Failure(
                UnifyUnknownException(
                    message = "清除错误报告失败: ${e.message}",
                    cause = e,
                ),
            )
        }
    }
}

interface UnifyErrorHandler {
    suspend fun handleError(
        exception: Throwable,
        context: String = "Unknown",
        metadata: Map<String, Any> = emptyMap(),
    ): UnifyResult<Unit>

    suspend fun handleUnifyException(
        exception: UnifyException,
        context: String = "Unknown",
    ): UnifyResult<Unit>

    suspend fun <T> safeExecute(
        context: String = "SafeExecution",
        block: suspend () -> T,
    ): UnifyResult<T>

    suspend fun <T> safeExecuteWithFallback(
        context: String = "SafeExecutionWithFallback",
        fallback: T,
        block: suspend () -> T,
    ): T

    suspend fun setGlobalErrorHandler(handler: (Throwable, String) -> Unit)
}

class UnifyErrorHandlerImpl(
    private val reporter: UnifyErrorReporter = UnifyLocalErrorReporter(),
) : UnifyErrorHandler {
    private var globalErrorHandler: ((Throwable, String) -> Unit)? = null

    override suspend fun handleError(
        exception: Throwable,
        context: String,
        metadata: Map<String, Any>,
    ): UnifyResult<Unit> {
        return try {
            val severity = determineSeverity(exception)

            // 报告错误
            reporter.reportException(exception, context, metadata, severity)

            // 调用全局错误处理器
            globalErrorHandler?.invoke(exception, context)

            // 根据严重程度采取不同行动
            when (severity) {
                UnifyErrorSeverity.CRITICAL -> {
                    UnifyLog.e("ErrorHandler", "严重错误: $context", exception)
                    // 可以在这里添加崩溃报告或重启逻辑
                }
                UnifyErrorSeverity.HIGH -> {
                    UnifyLog.e("ErrorHandler", "高级错误: $context", exception)
                }
                UnifyErrorSeverity.MEDIUM -> {
                    UnifyLog.w("ErrorHandler", "中级错误: $context", exception)
                }
                UnifyErrorSeverity.LOW -> {
                    UnifyLog.d("ErrorHandler", "低级错误: $context", exception)
                }
            }

            UnifyResult.Success(Unit)
        } catch (e: Exception) {
            UnifyResult.Failure(
                UnifyUnknownException(
                    message = "错误恢复失败: ${e.message}",
                    cause = e,
                ),
            )
        }
    }

    override suspend fun handleUnifyException(
        exception: UnifyException,
        context: String,
    ): UnifyResult<Unit> {
        return handleError(
            exception = exception,
            context = context,
            metadata =
                mapOf(
                    "error_message" to exception.message.orEmpty(),
                    "error_class" to exception::class.simpleName.orEmpty(),
                ),
        )
    }

    override suspend fun <T> safeExecute(
        context: String,
        block: suspend () -> T,
    ): UnifyResult<T> {
        return try {
            val result = block()
            UnifyResult.Success(result)
        } catch (e: Exception) {
            handleError(e, context)
            UnifyResult.Failure(
                UnifyUnknownException(
                    message = "执行失败: ${e.message}",
                    cause = e,
                ),
            )
        }
    }

    override suspend fun <T> safeExecuteWithFallback(
        context: String,
        fallback: T,
        block: suspend () -> T,
    ): T {
        return try {
            block()
        } catch (e: Exception) {
            handleError(e, context)
            fallback
        }
    }

    override suspend fun setGlobalErrorHandler(handler: (Throwable, String) -> Unit) {
        globalErrorHandler = handler
    }

    private fun determineSeverity(exception: Throwable): UnifyErrorSeverity {
        return when (exception) {
            is UnifyOutOfMemoryException -> UnifyErrorSeverity.CRITICAL
            is UnifyStackOverflowException -> UnifyErrorSeverity.CRITICAL
            is UnifyNetworkException -> UnifyErrorSeverity.HIGH
            is UnifyStorageException -> UnifyErrorSeverity.MEDIUM
            is UnifyPermissionException -> UnifyErrorSeverity.HIGH
            is UnifyPlatformException -> UnifyErrorSeverity.CRITICAL
            is UnifyException -> UnifyErrorSeverity.MEDIUM
            is IllegalArgumentException -> UnifyErrorSeverity.LOW
            is IllegalStateException -> UnifyErrorSeverity.MEDIUM
            is RuntimeException -> UnifyErrorSeverity.MEDIUM
            else -> UnifyErrorSeverity.MEDIUM
        }
    }
}

// 全局错误处理实例
object UnifyErrorHandling {
    private var _handler: UnifyErrorHandler = UnifyErrorHandlerImpl()

    val handler: UnifyErrorHandler get() = _handler

    fun setHandler(handler: UnifyErrorHandler) {
        _handler = handler
    }

    suspend fun handleError(
        exception: Throwable,
        context: String = "Global",
        metadata: Map<String, Any> = emptyMap(),
    ): UnifyResult<Unit> = _handler.handleError(exception, context, metadata)

    suspend fun <T> safe(
        context: String = "SafeExecution",
        block: suspend () -> T,
    ): UnifyResult<T> = _handler.safeExecute(context, block)

    suspend fun <T> safeWithFallback(
        fallback: T,
        context: String = "SafeExecutionWithFallback",
        block: suspend () -> T,
    ): T = _handler.safeExecuteWithFallback(context, fallback, block)
}

// 便捷扩展函数
suspend fun <T> UnifyResult<T>.onError(
    context: String = "ResultError",
    action: suspend (String) -> Unit = {},
): UnifyResult<T> {
    if (this is UnifyResult.Failure) {
        action(this.exception.message ?: "Unknown error")
        UnifyErrorHandling.handleError(
            RuntimeException(this.exception.message),
            context,
        )
    }
    return this
}

suspend fun <T> T.tryCatch(
    context: String = "TryCatch",
    block: suspend T.() -> Unit,
) {
    try {
        block()
    } catch (e: Exception) {
        UnifyErrorHandling.handleError(e, context)
    }
}
