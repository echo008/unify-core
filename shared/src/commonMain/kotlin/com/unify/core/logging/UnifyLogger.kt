package com.unify.core.logging

import com.unify.core.types.UnifyLogLevel
import com.unify.core.types.UnifyResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Unify统一日志系统
 * 100% Kotlin Multiplatform实现
 */
data class UnifyLogEntry(
    val level: UnifyLogLevel,
    val tag: String,
    val message: String,
    val throwable: Throwable? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val threadName: String = "main",
    val metadata: Map<String, Any> = emptyMap()
) {
    fun toFormattedString(): String {
        val levelStr = level.name.padEnd(5)
        val timeStr = formatTimestamp(timestamp)
        val throwableStr = throwable?.let { "\n${it.stackTraceToString()}" } ?: ""
        return "[$timeStr] $levelStr/$tag: $message$throwableStr"
    }
    
    private fun formatTimestamp(timestamp: Long): String {
        // 简化时间格式化，实际项目中应使用kotlinx-datetime
        return timestamp.toString()
    }
}

interface UnifyLogAppender {
    suspend fun append(entry: UnifyLogEntry): UnifyResult<Unit>
    suspend fun flush(): UnifyResult<Unit>
    suspend fun close(): UnifyResult<Unit>
}

class UnifyConsoleAppender : UnifyLogAppender {
    override suspend fun append(entry: UnifyLogEntry): UnifyResult<Unit> {
        return try {
            println(entry.toFormattedString())
            UnifyResult.Success(Unit)
        } catch (e: Exception) {
            UnifyResult.failure(com.unify.core.exceptions.UnifyUnknownException("控制台输出失败", e.message))
        }
    }
    
    override suspend fun flush(): UnifyResult<Unit> = UnifyResult.Success(Unit)
    override suspend fun close(): UnifyResult<Unit> = UnifyResult.Success(Unit)
}

class UnifyMemoryAppender(
    private val maxEntries: Int = 1000
) : UnifyLogAppender {
    private val _entries = MutableStateFlow<List<UnifyLogEntry>>(emptyList())
    val entries: StateFlow<List<UnifyLogEntry>> = _entries.asStateFlow()
    
    override suspend fun append(entry: UnifyLogEntry): UnifyResult<Unit> {
        return try {
            val currentEntries = _entries.value.toMutableList()
            currentEntries.add(entry)
            
            // 保持最大条目数限制
            if (currentEntries.size > maxEntries) {
                currentEntries.removeAt(0)
            }
            
            _entries.value = currentEntries
            UnifyResult.Success(Unit)
        } catch (e: Exception) {
            UnifyResult.failure(com.unify.core.exceptions.UnifyUnknownException("内存日志添加失败", e.message))
        }
    }
    
    override suspend fun flush(): UnifyResult<Unit> = UnifyResult.Success(Unit)
    
    override suspend fun close(): UnifyResult<Unit> {
        return try {
            _entries.value = emptyList()
            UnifyResult.Success(Unit)
        } catch (e: Exception) {
            UnifyResult.failure(com.unify.core.exceptions.UnifyUnknownException("内存日志清理失败", e.message))
        }
    }
    
    suspend fun clear(): UnifyResult<Unit> = close()
    
    suspend fun getEntries(
        level: UnifyLogLevel? = null,
        tag: String? = null,
        limit: Int? = null
    ): UnifyResult<List<UnifyLogEntry>> {
        return try {
            var filteredEntries = _entries.value
            
            if (level != null) {
                filteredEntries = filteredEntries.filter { it.level == level }
            }
            
            if (tag != null) {
                filteredEntries = filteredEntries.filter { it.tag == tag }
            }
            
            if (limit != null && limit > 0) {
                filteredEntries = filteredEntries.takeLast(limit)
            }
            
            UnifyResult.Success(filteredEntries)
        } catch (e: Exception) {
            UnifyResult.failure(com.unify.core.exceptions.UnifyUnknownException("获取日志条目失败", e.message))
        }
    }
}

interface UnifyLogger {
    var level: UnifyLogLevel
    val appenders: List<UnifyLogAppender>
    
    suspend fun log(level: UnifyLogLevel, tag: String, message: String, throwable: Throwable? = null, metadata: Map<String, Any> = emptyMap())
    suspend fun verbose(tag: String, message: String, throwable: Throwable? = null, metadata: Map<String, Any> = emptyMap())
    suspend fun debug(tag: String, message: String, throwable: Throwable? = null, metadata: Map<String, Any> = emptyMap())
    suspend fun info(tag: String, message: String, throwable: Throwable? = null, metadata: Map<String, Any> = emptyMap())
    suspend fun warn(tag: String, message: String, throwable: Throwable? = null, metadata: Map<String, Any> = emptyMap())
    suspend fun error(tag: String, message: String, throwable: Throwable? = null, metadata: Map<String, Any> = emptyMap())
    
    suspend fun addAppender(appender: UnifyLogAppender): UnifyResult<Unit>
    suspend fun removeAppender(appender: UnifyLogAppender): UnifyResult<Unit>
    suspend fun flush(): UnifyResult<Unit>
    suspend fun close(): UnifyResult<Unit>
}

class UnifyLoggerImpl(
    override var level: UnifyLogLevel = UnifyLogLevel.DEBUG
) : UnifyLogger {
    private val _appenders = mutableListOf<UnifyLogAppender>()
    override val appenders: List<UnifyLogAppender> get() = _appenders.toList()
    
    init {
        // 默认添加控制台输出
        _appenders.add(UnifyConsoleAppender())
    }
    
    override suspend fun log(
        level: UnifyLogLevel,
        tag: String,
        message: String,
        throwable: Throwable?,
        metadata: Map<String, Any>
    ) {
        if (level.ordinal < this.level.ordinal) return
        
        val entry = UnifyLogEntry(
            level = level,
            tag = tag,
            message = message,
            throwable = throwable,
            metadata = metadata
        )
        
        _appenders.forEach { appender ->
            appender.append(entry)
        }
    }
    
    override suspend fun verbose(tag: String, message: String, throwable: Throwable?, metadata: Map<String, Any>) {
        log(UnifyLogLevel.VERBOSE, tag, message, throwable, metadata)
    }
    
    override suspend fun debug(tag: String, message: String, throwable: Throwable?, metadata: Map<String, Any>) {
        log(UnifyLogLevel.DEBUG, tag, message, throwable, metadata)
    }
    
    override suspend fun info(tag: String, message: String, throwable: Throwable?, metadata: Map<String, Any>) {
        log(UnifyLogLevel.INFO, tag, message, throwable, metadata)
    }
    
    override suspend fun warn(tag: String, message: String, throwable: Throwable?, metadata: Map<String, Any>) {
        log(UnifyLogLevel.WARN, tag, message, throwable, metadata)
    }
    
    override suspend fun error(tag: String, message: String, throwable: Throwable?, metadata: Map<String, Any>) {
        log(UnifyLogLevel.ERROR, tag, message, throwable, metadata)
    }
    
    override suspend fun addAppender(appender: UnifyLogAppender): UnifyResult<Unit> {
        return try {
            if (!_appenders.contains(appender)) {
                _appenders.add(appender)
            }
            UnifyResult.Success(Unit)
        } catch (e: Exception) {
            UnifyResult.failure(com.unify.core.exceptions.UnifyUnknownException("添加日志输出器失败", e.message))
        }
    }
    
    override suspend fun removeAppender(appender: UnifyLogAppender): UnifyResult<Unit> {
        return try {
            _appenders.remove(appender)
            UnifyResult.Success(Unit)
        } catch (e: Exception) {
            UnifyResult.failure(com.unify.core.exceptions.UnifyUnknownException("移除日志输出器失败", e.message))
        }
    }
    
    override suspend fun flush(): UnifyResult<Unit> {
        return try {
            _appenders.forEach { it.flush() }
            UnifyResult.Success(Unit)
        } catch (e: Exception) {
            UnifyResult.failure(com.unify.core.exceptions.UnifyUnknownException("刷新日志失败", e.message))
        }
    }
    
    override suspend fun close(): UnifyResult<Unit> {
        return try {
            _appenders.forEach { it.close() }
            _appenders.clear()
            UnifyResult.Success(Unit)
        } catch (e: Exception) {
            UnifyResult.failure(com.unify.core.exceptions.UnifyUnknownException("关闭日志系统失败", e.message))
        }
    }
}

// 全局日志实例
object UnifyLog {
    private var _logger: UnifyLogger = UnifyLoggerImpl()
    
    val logger: UnifyLogger get() = _logger
    
    fun setLogger(logger: UnifyLogger) {
        _logger = logger
    }
    
    suspend fun v(tag: String, message: String, throwable: Throwable? = null) = 
        _logger.verbose(tag, message, throwable)
    
    suspend fun d(tag: String, message: String, throwable: Throwable? = null) = 
        _logger.debug(tag, message, throwable)
    
    suspend fun i(tag: String, message: String, throwable: Throwable? = null) = 
        _logger.info(tag, message, throwable)
    
    suspend fun w(tag: String, message: String, throwable: Throwable? = null) = 
        _logger.warn(tag, message, throwable)
    
    suspend fun e(tag: String, message: String, throwable: Throwable? = null) = 
        _logger.error(tag, message, throwable)
}

// 便捷扩展函数
suspend inline fun <T> T.logDebug(tag: String, message: String) {
    UnifyLog.d(tag, message)
}

suspend inline fun <T> T.logInfo(tag: String, message: String) {
    UnifyLog.i(tag, message)
}

suspend inline fun <T> T.logWarn(tag: String, message: String, throwable: Throwable? = null) {
    UnifyLog.w(tag, message, throwable)
}

suspend inline fun <T> T.logError(tag: String, message: String, throwable: Throwable? = null) {
    UnifyLog.e(tag, message, throwable)
}
