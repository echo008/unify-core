package com.unify.error

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import com.unify.database.*
import com.unify.platform.*
import com.unify.monitoring.*

/**
 * 统一错误处理系统
 * 基于文档要求的错误处理和异常管理机制
 */

/**
 * 统一错误类型
 */
sealed class UnifyError(
    open val message: String,
    open val cause: Throwable? = null,
    open val timestamp: Long = System.currentTimeMillis()
) {
    data class NetworkError(
        override val message: String,
        override val cause: Throwable? = null,
        val url: String? = null,
        val statusCode: Int? = null
    ) : UnifyError(message, cause)
    
    data class DatabaseError(
        override val message: String,
        override val cause: Throwable? = null,
        val query: String? = null
    ) : UnifyError(message, cause)
    
    data class StorageError(
        override val message: String,
        override val cause: Throwable? = null,
        val key: String? = null
    ) : UnifyError(message, cause)
    
    data class ComponentError(
        override val message: String,
        override val cause: Throwable? = null,
        val componentId: String,
        val componentType: String? = null
    ) : UnifyError(message, cause)
    
    data class NavigationError(
        override val message: String,
        override val cause: Throwable? = null,
        val route: String? = null
    ) : UnifyError(message, cause)
    
    data class PerformanceError(
        override val message: String,
        val metric: PerformanceMetric? = null
    ) : UnifyError(message)
    
    data class PlatformError(
        override val message: String,
        override val cause: Throwable? = null,
        val platform: String,
        val feature: String? = null
    ) : UnifyError(message, cause)
    
    data class ValidationError(
        override val message: String,
        val field: String? = null,
        val value: Any? = null
    ) : UnifyError(message)
    
    data class AuthenticationError(
        override val message: String,
        override val cause: Throwable? = null,
        val userId: String? = null
    ) : UnifyError(message, cause)
    
    data class UnknownError(
        override val message: String,
        override val cause: Throwable? = null
    ) : UnifyError(message, cause)
}

/**
 * 错误严重级别
 */
enum class ErrorSeverity {
    LOW,        // 低级别，不影响功能
    MEDIUM,     // 中级别，部分功能受影响
    HIGH,       // 高级别，核心功能受影响
    CRITICAL    // 严重级别，应用崩溃或无法使用
}

/**
 * 错误处理策略
 */
enum class ErrorHandlingStrategy {
    IGNORE,         // 忽略错误
    LOG_ONLY,       // 仅记录日志
    RETRY,          // 重试操作
    FALLBACK,       // 使用备用方案
    USER_NOTIFY,    // 通知用户
    CRASH           // 让应用崩溃
}

/**
 * 错误处理配置
 */
data class ErrorHandlingConfig(
    val enableErrorLogging: Boolean = true,
    val enableErrorReporting: Boolean = true,
    val enableCrashReporting: Boolean = true,
    val maxErrorsInMemory: Int = 500,
    val errorRetentionDays: Int = 7,
    val autoRetryMaxAttempts: Int = 3,
    val autoRetryDelayMs: Long = 1000L,
    val strategies: Map<String, ErrorHandlingStrategy> = defaultStrategies()
)

/**
 * 错误处理器
 */
object UnifyErrorHandler {
    private var config = ErrorHandlingConfig()
    private var database: UnifyDatabase? = null
    private var platformManager: UnifyPlatformManager? = null
    
    private val errorFlow = MutableSharedFlow<UnifyError>()
    private val errorBuffer = mutableListOf<UnifyError>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    private val errorHandlers = mutableMapOf<String, suspend (UnifyError) -> Unit>()
    
    fun initialize(
        config: ErrorHandlingConfig = ErrorHandlingConfig(),
        database: UnifyDatabase? = null,
        platformManager: UnifyPlatformManager? = null
    ) {
        this.config = config
        this.database = database
        this.platformManager = platformManager
        
        if (config.enableErrorReporting) {
            startErrorReporting()
        }
        
        setupDefaultHandlers()
    }
    
    /**
     * 处理错误
     */
    suspend fun handleError(error: UnifyError) {
        // 添加到缓冲区
        synchronized(errorBuffer) {
            errorBuffer.add(error)
            if (errorBuffer.size > config.maxErrorsInMemory) {
                errorBuffer.removeAt(0)
            }
        }
        
        // 发送到流
        errorFlow.emit(error)
        
        // 记录日志
        if (config.enableErrorLogging) {
            logError(error)
        }
        
        // 持久化到数据库
        if (database != null && config.enableErrorReporting) {
            try {
                persistError(error)
            } catch (e: Exception) {
                // 避免递归错误处理
                logError(UnifyError.DatabaseError("Failed to persist error", e))
            }
        }
        
        // 执行错误处理策略
        executeErrorStrategy(error)
        
        // 调用自定义处理器
        val errorType = error::class.simpleName ?: "UnknownError"
        errorHandlers[errorType]?.invoke(error)
    }
    
    /**
     * 注册自定义错误处理器
     */
    fun registerErrorHandler(errorType: String, handler: suspend (UnifyError) -> Unit) {
        errorHandlers[errorType] = handler
    }
    
    /**
     * 获取错误流
     */
    fun getErrorFlow(): SharedFlow<UnifyError> = errorFlow.asSharedFlow()
    
    /**
     * 获取缓存的错误
     */
    fun getCachedErrors(): List<UnifyError> {
        return synchronized(errorBuffer) {
            errorBuffer.toList()
        }
    }
    
    /**
     * 获取错误统计
     */
    fun getErrorStats(): ErrorStats {
        val errors = getCachedErrors()
        val now = System.currentTimeMillis()
        val last24Hours = now - (24 * 60 * 60 * 1000L)
        
        val recentErrors = errors.filter { it.timestamp >= last24Hours }
        
        return ErrorStats(
            totalErrors = errors.size,
            recentErrors = recentErrors.size,
            errorsByType = errors.groupBy { it::class.simpleName ?: "Unknown" }
                .mapValues { it.value.size },
            errorsBySeverity = errors.groupBy { getErrorSeverity(it) }
                .mapValues { it.value.size },
            mostFrequentError = errors.groupBy { it.message }
                .maxByOrNull { it.value.size }?.key
        )
    }
    
    /**
     * 清理旧错误
     */
    suspend fun cleanupOldErrors() {
        val cutoffTime = System.currentTimeMillis() - (config.errorRetentionDays * 24 * 60 * 60 * 1000L)
        
        // 清理内存缓冲区
        synchronized(errorBuffer) {
            errorBuffer.removeAll { it.timestamp < cutoffTime }
        }
        
        // 清理数据库
        database?.errorLogQueries?.deleteOldErrorLogs(cutoffTime)
    }
    
    /**
     * 重试操作
     */
    suspend fun <T> retryOperation(
        maxAttempts: Int = config.autoRetryMaxAttempts,
        delayMs: Long = config.autoRetryDelayMs,
        operation: suspend () -> T
    ): T {
        var lastException: Exception? = null
        
        repeat(maxAttempts) { attempt ->
            try {
                return operation()
            } catch (e: Exception) {
                lastException = e
                
                if (attempt < maxAttempts - 1) {
                    delay(delayMs * (attempt + 1)) // 指数退避
                }
                
                handleError(
                    UnifyError.UnknownError(
                        "Retry attempt ${attempt + 1} failed: ${e.message}",
                        e
                    )
                )
            }
        }
        
        throw lastException ?: Exception("All retry attempts failed")
    }
    
    private fun logError(error: UnifyError) {
        val platform = platformManager?.getCurrentPlatform()?.name ?: "UNKNOWN"
        val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date(error.timestamp))
        
        println("[$timestamp][$platform] ERROR: ${error::class.simpleName} - ${error.message}")
        error.cause?.let { cause ->
            println("  Caused by: ${cause::class.simpleName} - ${cause.message}")
            cause.printStackTrace()
        }
    }
    
    private suspend fun persistError(error: UnifyError) {
        val platform = platformManager?.getCurrentPlatform()?.name ?: "UNKNOWN"
        val errorType = error::class.simpleName ?: "UnknownError"
        
        database?.insertErrorLogEntity(
            ErrorLogEntity(
                id = 0, // 自动生成
                errorType = errorType,
                message = error.message,
                stackTrace = error.cause?.stackTraceToString(),
                timestamp = error.timestamp,
                platform = platform,
                userId = getCurrentUserId()
            )
        )
    }
    
    private suspend fun executeErrorStrategy(error: UnifyError) {
        val errorType = error::class.simpleName ?: "UnknownError"
        val strategy = config.strategies[errorType] ?: ErrorHandlingStrategy.LOG_ONLY
        
        when (strategy) {
            ErrorHandlingStrategy.IGNORE -> {
                // 什么都不做
            }
            ErrorHandlingStrategy.LOG_ONLY -> {
                // 已经在上面记录了日志
            }
            ErrorHandlingStrategy.RETRY -> {
                // 重试逻辑需要在调用方实现
                logError(UnifyError.UnknownError("Retry strategy requires caller implementation"))
            }
            ErrorHandlingStrategy.FALLBACK -> {
                // 备用方案需要在调用方实现
                logError(UnifyError.UnknownError("Fallback strategy requires caller implementation"))
            }
            ErrorHandlingStrategy.USER_NOTIFY -> {
                // 通知用户（可以通过平台特定的方式）
                notifyUser(error)
            }
            ErrorHandlingStrategy.CRASH -> {
                // 让应用崩溃
                throw RuntimeException("Critical error: ${error.message}", error.cause)
            }
        }
    }
    
    private fun setupDefaultHandlers() {
        // 网络错误处理器
        registerErrorHandler("NetworkError") { error ->
            if (error is UnifyError.NetworkError) {
                when (error.statusCode) {
                    401 -> {
                        // 未授权，可能需要重新登录
                        logError(UnifyError.AuthenticationError("Authentication required"))
                    }
                    404 -> {
                        // 资源不存在
                        logError(UnifyError.UnknownError("Resource not found: ${error.url}"))
                    }
                    500 -> {
                        // 服务器错误，可能需要重试
                        logError(UnifyError.UnknownError("Server error, consider retry"))
                    }
                }
            }
        }
        
        // 组件错误处理器
        registerErrorHandler("ComponentError") { error ->
            if (error is UnifyError.ComponentError) {
                // 组件错误可能需要重新渲染或重置状态
                logError(UnifyError.UnknownError("Component ${error.componentId} error, consider reset"))
            }
        }
        
        // 性能错误处理器
        registerErrorHandler("PerformanceError") { error ->
            if (error is UnifyError.PerformanceError) {
                // 性能问题可能需要优化或降级处理
                logError(UnifyError.UnknownError("Performance issue detected, consider optimization"))
            }
        }
    }
    
    private fun startErrorReporting() {
        scope.launch {
            errorFlow.collect { error ->
                // 可以在这里实现错误上报到分析服务
                if (config.enableCrashReporting && getErrorSeverity(error) == ErrorSeverity.CRITICAL) {
                    reportCriticalError(error)
                }
            }
        }
    }
    
    private fun getErrorSeverity(error: UnifyError): ErrorSeverity {
        return when (error) {
            is UnifyError.NetworkError -> when (error.statusCode) {
                in 500..599 -> ErrorSeverity.HIGH
                in 400..499 -> ErrorSeverity.MEDIUM
                else -> ErrorSeverity.LOW
            }
            is UnifyError.DatabaseError -> ErrorSeverity.HIGH
            is UnifyError.ComponentError -> ErrorSeverity.MEDIUM
            is UnifyError.PerformanceError -> ErrorSeverity.MEDIUM
            is UnifyError.AuthenticationError -> ErrorSeverity.HIGH
            is UnifyError.ValidationError -> ErrorSeverity.LOW
            else -> ErrorSeverity.MEDIUM
        }
    }
    
    private suspend fun notifyUser(error: UnifyError) {
        // 平台特定的用户通知实现
        val userMessage = when (error) {
            is UnifyError.NetworkError -> "网络连接出现问题，请检查网络设置"
            is UnifyError.AuthenticationError -> "登录已过期，请重新登录"
            is UnifyError.ValidationError -> "输入数据有误：${error.message}"
            else -> "应用出现问题，我们正在处理中"
        }
        
        // 这里可以调用平台特定的通知API
        logError(UnifyError.UnknownError("User notification: $userMessage"))
    }
    
    private suspend fun reportCriticalError(error: UnifyError) {
        // 上报严重错误到分析服务
        logError(UnifyError.UnknownError("Critical error reported: ${error.message}"))
    }
    
    private suspend fun getCurrentUserId(): Long? {
        // 从存储中获取当前用户ID
        return try {
            // 这里需要依赖注入获取存储服务
            null
        } catch (e: Exception) {
            null
        }
    }
    
    fun cleanup() {
        scope.cancel()
        errorHandlers.clear()
        synchronized(errorBuffer) {
            errorBuffer.clear()
        }
    }
}

/**
 * 错误统计
 */
data class ErrorStats(
    val totalErrors: Int,
    val recentErrors: Int,
    val errorsByType: Map<String, Int>,
    val errorsBySeverity: Map<ErrorSeverity, Int>,
    val mostFrequentError: String?
)

/**
 * 默认错误处理策略
 */
private fun defaultStrategies(): Map<String, ErrorHandlingStrategy> = mapOf(
    "NetworkError" to ErrorHandlingStrategy.RETRY,
    "DatabaseError" to ErrorHandlingStrategy.LOG_ONLY,
    "StorageError" to ErrorHandlingStrategy.FALLBACK,
    "ComponentError" to ErrorHandlingStrategy.USER_NOTIFY,
    "NavigationError" to ErrorHandlingStrategy.FALLBACK,
    "PerformanceError" to ErrorHandlingStrategy.LOG_ONLY,
    "PlatformError" to ErrorHandlingStrategy.FALLBACK,
    "ValidationError" to ErrorHandlingStrategy.USER_NOTIFY,
    "AuthenticationError" to ErrorHandlingStrategy.USER_NOTIFY,
    "UnknownError" to ErrorHandlingStrategy.LOG_ONLY
)

/**
 * 错误处理扩展函数
 */
suspend inline fun <T> safeCall(
    crossinline operation: suspend () -> T,
    crossinline onError: suspend (UnifyError) -> T
): T {
    return try {
        operation()
    } catch (e: Exception) {
        val error = when (e) {
            is java.net.SocketTimeoutException -> UnifyError.NetworkError("Network timeout", e)
            is java.net.UnknownHostException -> UnifyError.NetworkError("Unknown host", e)
            is IllegalArgumentException -> UnifyError.ValidationError(e.message ?: "Validation error")
            else -> UnifyError.UnknownError(e.message ?: "Unknown error", e)
        }
        
        UnifyErrorHandler.handleError(error)
        onError(error)
    }
}

/**
 * 带重试的安全调用
 */
suspend inline fun <T> safeCallWithRetry(
    maxAttempts: Int = 3,
    delayMs: Long = 1000L,
    crossinline operation: suspend () -> T,
    crossinline onError: suspend (UnifyError) -> T
): T {
    return try {
        UnifyErrorHandler.retryOperation(maxAttempts, delayMs) {
            operation()
        }
    } catch (e: Exception) {
        val error = UnifyError.UnknownError("All retry attempts failed: ${e.message}", e)
        UnifyErrorHandler.handleError(error)
        onError(error)
    }
}
