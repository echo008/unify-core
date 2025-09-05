package com.unify.core.error

import com.unify.core.error.UnifyException
import com.unify.core.error.UnifyUnknownException
import com.unify.core.types.UnifyResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 简化的错误处理工具类
 * 注意：主要的UnifyErrorHandling在UnifyErrorHandler.kt中定义
 */
object UnifyErrorUtils {
    
    private val errorHandlers = mutableListOf<ErrorHandler>()
    
    /**
     * 错误处理器接口
     */
    interface ErrorHandler {
        suspend fun handleError(exception: Throwable, context: String, metadata: Map<String, Any>): UnifyResult<Unit>
    }
    
    /**
     * 注册错误处理器
     */
    fun registerErrorHandler(handler: ErrorHandler) {
        errorHandlers.add(handler)
    }
    
    /**
     * 处理错误
     */
    suspend fun handleError(
        exception: Throwable,
        context: String = "Unknown",
        metadata: Map<String, Any> = emptyMap()
    ): UnifyResult<Unit> {
        return try {
            val unifyException = when (exception) {
                is UnifyException -> exception
                else -> UnifyUnknownException(
                    message = "Unhandled exception: ${exception.message}",
                    cause = exception
                )
            }
            
            // 记录错误日志
            println("ERROR [$context]: ${unifyException.message}")
            if (unifyException.cause != null) {
                println("CAUSE: ${unifyException.cause?.message}")
            }
            
            // 调用所有注册的错误处理器
            errorHandlers.forEach { handler ->
                try {
                    handler.handleError(unifyException, context, metadata)
                } catch (e: Exception) {
                    println("Error in error handler: ${e.message}")
                }
            }
            
            UnifyResult.Success(Unit)
        } catch (e: Exception) {
            UnifyResult.Failure(UnifyUnknownException(
                message = "Failed to handle error: ${e.message}",
                cause = e
            ))
        }
    }
    
    /**
     * 处理错误（非挂起版本）
     */
    fun handleErrorSync(
        exception: Throwable,
        context: String = "Unknown",
        metadata: Map<String, Any> = emptyMap()
    ) {
        CoroutineScope(Dispatchers.Default).launch {
            handleError(exception, context, metadata)
        }
    }
    
    /**
     * 创建错误结果
     */
    fun <T> createErrorResult(
        message: String,
        cause: String? = null
    ): UnifyResult<T> {
        return UnifyResult.Failure(UnifyUnknownException(message, RuntimeException(cause)))
    }
    
    /**
     * 安全执行代码块
     */
    suspend inline fun <T> safeExecute(
        context: String = "SafeExecute",
        crossinline block: suspend () -> T
    ): UnifyResult<T> {
        return try {
            val result = block()
            UnifyResult.Success(result)
        } catch (e: Exception) {
            handleError(e, context)
            UnifyResult.Failure(UnifyUnknownException(
                message = "Safe execution failed: ${e.message}",
                cause = e
            ))
        }
    }
}
