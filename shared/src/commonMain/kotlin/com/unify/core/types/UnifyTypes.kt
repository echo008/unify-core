package com.unify.core.types

import com.unify.core.platform.getCurrentTimeMillis
import kotlinx.serialization.Serializable

/**
 * 企业级结果封装类
 * 提供类型安全的成功/失败结果处理
 */
sealed class UnifyResult<out T> {
    /**
     * 成功结果
     */
    data class Success<T>(val data: T) : UnifyResult<T>()

    /**
     * 失败结果
     */
    data class Failure(val exception: com.unify.core.error.UnifyException) : UnifyResult<Nothing>()

    /**
     * 是否成功
     */
    val isSuccess: Boolean get() = this is Success

    /**
     * 是否失败
     */
    val isFailure: Boolean get() = this is Failure

    /**
     * 获取数据（成功时）
     */
    fun getOrNull(): T? =
        when (this) {
            is Success -> data
            is Failure -> null
        }

    /**
     * 获取异常（失败时）
     */
    fun exceptionOrNull(): com.unify.core.error.UnifyException? =
        when (this) {
            is Success -> null
            is Failure -> exception
        }

    /**
     * 获取数据或抛出异常
     */
    fun getOrThrow(): T =
        when (this) {
            is Success -> data
            is Failure -> throw exception
        }

    /**
     * 映射成功结果
     */
    inline fun <R> map(transform: (T) -> R): UnifyResult<R> =
        when (this) {
            is Success ->
                try {
                    Success(transform(data))
                } catch (e: Exception) {
                    Failure(com.unify.core.error.UnifyTransformException("数据转换失败", e))
                }
            is Failure -> this
        }

    /**
     * 平面映射
     */
    inline fun <R> flatMap(transform: (T) -> UnifyResult<R>): UnifyResult<R> =
        when (this) {
            is Success ->
                try {
                    transform(data)
                } catch (e: Exception) {
                    Failure(com.unify.core.error.UnifyTransformException("数据转换失败", e))
                }
            is Failure -> this
        }

    companion object {
        /**
         * 创建成功结果
         */
        fun <T> success(data: T): UnifyResult<T> = Success(data)

        /**
         * 创建失败结果
         */
        fun <T> failure(exception: com.unify.core.error.UnifyException): UnifyResult<T> = Failure(exception)

        /**
         * 从可能抛出异常的代码块创建结果
         */
        inline fun <T> runCatching(block: () -> T): UnifyResult<T> =
            try {
                Success(block())
            } catch (e: Exception) {
                Failure(
                    when (e) {
                        is com.unify.core.error.UnifyException -> e
                        else -> com.unify.core.error.UnifyUnknownException("未知错误", e)
                    },
                )
            }
    }
}

/**
 * 平台类型枚举
 */
@Serializable
enum class PlatformType(val displayName: String, val identifier: String) {
    ANDROID("Android", "android"),
    IOS("iOS", "ios"),
    WEB("Web", "web"),
    DESKTOP("Desktop", "desktop"),
    NATIVE("Native", "native"),
    HARMONY_OS("HarmonyOS", "harmonyos"),
    MINI_PROGRAM("MiniProgram", "miniprogram"),
    WATCH("Watch", "watch"),
    TV("TV", "tv"),
    ;

    companion object {
        /**
         * 从标识符获取平台
         */
        fun fromIdentifier(identifier: String): PlatformType? {
            return values().find { it.identifier == identifier }
        }
    }
}

/**
 * 平台枚举（兼容性别名）
 */
@Serializable
enum class UnifyPlatform(val displayName: String, val identifier: String) {
    ANDROID("Android", "android"),
    IOS("iOS", "ios"),
    WEB("Web", "web"),
    DESKTOP("Desktop", "desktop"),
    HARMONY_OS("HarmonyOS", "harmonyos"),
    MINI_APP("MiniApp", "miniapp"),
    WATCH("Watch", "watch"),
    TV("TV", "tv"),
    ;

    companion object {
        /**
         * 从标识符获取平台
         */
        fun fromIdentifier(identifier: String): UnifyPlatform? {
            return values().find { it.identifier == identifier }
        }
    }
}

/**
 * 初始化状态
 */
@Serializable
enum class UnifyInitializationState {
    /** 未初始化 */
    NOT_INITIALIZED,

    /** 初始化中 */
    INITIALIZING,

    /** 初始化成功 */
    INITIALIZED,

    /** 初始化失败 */
    FAILED,

    /** 清理中 */
    CLEANING_UP,

    /** 已清理 */
    CLEANED_UP,
}

/**
 * 健康状态
 */
@Serializable
enum class UnifyHealthState {
    /** 健康 */
    HEALTHY,

    /** 警告 */
    WARNING,

    /** 错误 */
    ERROR,

    /** 严重错误 */
    CRITICAL,

    /** 未知 */
    UNKNOWN,
}

/**
 * 问题严重性
 */
@Serializable
enum class IssueSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL,
}

/**
 * 测试状态
 */
@Serializable
enum class TestStatus {
    PASSED,
    FAILED,
    SKIPPED,
}

/**
 * 活动类型枚举
 */
@Serializable
enum class UnifyActivityType {
    CYCLING,
    STOPPED,
    WALKING,
    RUNNING,
    DRIVING,
    UNKNOWN,
}

/**
 * 版本信息
 */
@Serializable
data class UnifyVersionInfo(
    val version: String,
    val buildNumber: Int,
    val releaseDate: String,
    val gitCommit: String,
    val kotlinVersion: String,
    val composeVersion: String,
)

/**
 * 平台配置
 */
@Serializable
data class UnifyPlatformConfig(
    val platform: UnifyPlatform,
    val version: String,
    val capabilities: List<String>,
    val features: Map<String, String>,
    val limitations: List<String> = emptyList(),
    val metadata: Map<String, String> = emptyMap(),
)

/**
 * 性能指标
 */
@Serializable
data class UnifyPerformanceMetrics(
    val startupTime: Long,
    val memoryUsage: Long,
    val cpuUsage: Double,
    val renderTime: Long,
    val networkLatency: Long,
    val frameRate: Double = 60.0,
    val timestamp: Long = getCurrentTimeMillis(),
)

/**
 * 健康检查结果
 */
@Serializable
data class UnifyHealthCheckResult(
    val overallState: UnifyHealthState,
    val checks: Map<String, UnifyHealthCheck>,
    val timestamp: Long = getCurrentTimeMillis(),
    val duration: Long,
)

/**
 * 单个健康检查
 */
@Serializable
data class UnifyHealthCheck(
    val name: String,
    val state: UnifyHealthState,
    val status: HealthStatus,
    val message: String,
    val details: Map<String, String> = emptyMap(),
)

/**
 * 框架配置
 */
@Serializable
data class UnifyConfiguration(
    val enableDebugMode: Boolean = false,
    val enablePerformanceMonitoring: Boolean = true,
    val enableHealthChecks: Boolean = true,
    val logLevel: UnifyLogLevel = UnifyLogLevel.INFO,
    val customProperties: Map<String, String> = emptyMap(),
) {
    companion object {
        fun default() = UnifyConfiguration()
    }
}

/**
 * 应用配置
 */
@Serializable
data class UnifyAppConfiguration(
    val themeConfig: UnifyThemeConfig = UnifyThemeConfig.default(),
    val errorConfig: UnifyErrorConfig = UnifyErrorConfig.default(),
    val performanceConfig: UnifyPerformanceConfig = UnifyPerformanceConfig.default(),
) {
    companion object {
        fun default() = UnifyAppConfiguration()
    }
}

/**
 * 主题配置
 */
@Serializable
data class UnifyThemeConfig(
    val useDarkTheme: Boolean = false,
    val useSystemTheme: Boolean = true,
    val primaryColor: String = "#6200EE",
    val customColors: Map<String, String> = emptyMap(),
) {
    companion object {
        fun default() = UnifyThemeConfig()
    }
}

/**
 * 错误配置
 */
@Serializable
data class UnifyErrorConfig(
    val enableErrorBoundary: Boolean = true,
    val enableCrashReporting: Boolean = false,
    val maxErrorRetries: Int = 3,
    val errorDisplayMode: UnifyErrorDisplayMode = UnifyErrorDisplayMode.USER_FRIENDLY,
) {
    companion object {
        fun default() = UnifyErrorConfig()
    }
}

/**
 * 性能配置
 */
@Serializable
data class UnifyPerformanceConfig(
    val enableMonitoring: Boolean = true,
    val samplingRate: Double = 0.1,
    val maxMetricsHistory: Int = 1000,
) {
    companion object {
        fun default() = UnifyPerformanceConfig()
    }
}

/**
 * 日志级别
 */
@Serializable
enum class UnifyLogLevel {
    VERBOSE,
    DEBUG,
    INFO,
    WARN,
    ERROR,
}

/**
 * 错误显示模式
 */
@Serializable
enum class UnifyErrorDisplayMode {
    USER_FRIENDLY,
    TECHNICAL,
    HIDDEN,
}

/**
 * 设备信息
 */
@Serializable
data class DeviceInfo(
    val manufacturer: String,
    val model: String,
    val systemName: String,
    val systemVersion: String,
    val deviceId: String,
    val isEmulator: Boolean,
)
