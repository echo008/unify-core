package com.unify.quality.data

import com.unify.core.platform.getCurrentTimeMillis
import kotlinx.serialization.Serializable

/**
 * Unify质量管理数据类型定义
 * 支持代码质量、性能质量、测试质量等多维度质量指标
 */

@Serializable
data class UnifyQualityMetrics(
    val codeQuality: CodeQualityMetrics,
    val performanceQuality: PerformanceQualityMetrics,
    val testQuality: TestQualityMetrics,
    val securityQuality: SecurityQualityMetrics,
    val timestamp: Long = getCurrentTimeMillis(),
)

@Serializable
data class CodeQualityMetrics(
    val codeReusabilityRate: Double, // 代码复用率
    val platformSpecificCodeRate: Double, // 平台特定代码率
    val cyclomaticComplexity: Int, // 圈复杂度
    val codeSmellCount: Int, // 代码异味数量
    val duplicatedLinesRate: Double, // 重复代码率
    val maintainabilityIndex: Double, // 可维护性指数
    val technicalDebtRatio: Double, // 技术债务比率
)

@Serializable
data class PerformanceQualityMetrics(
    val startupTime: Long, // 启动时间(ms)
    val memoryUsage: Long, // 内存使用(bytes)
    val cpuUsage: Double, // CPU使用率
    val renderingTime: Long, // 渲染时间(ms)
    val networkLatency: Long, // 网络延迟(ms)
    val batteryConsumption: Double, // 电池消耗率
    val frameRate: Double, // 帧率
)

@Serializable
data class TestQualityMetrics(
    val testCoverage: Double, // 测试覆盖率
    val unitTestCount: Int, // 单元测试数量
    val integrationTestCount: Int, // 集成测试数量
    val uiTestCount: Int, // UI测试数量
    val performanceTestCount: Int, // 性能测试数量
    val passRate: Double, // 测试通过率
    val testExecutionTime: Long, // 测试执行时间(ms)
)

@Serializable
data class SecurityQualityMetrics(
    val vulnerabilityCount: Int, // 漏洞数量
    val securityScore: Double, // 安全评分(0-10)
    val encryptionCompliance: Boolean, // 加密合规性
    val permissionCompliance: Boolean, // 权限合规性
    val dataProtectionCompliance: Boolean, // 数据保护合规性
    val auditTrailCompliance: Boolean, // 审计跟踪合规性
)

@Serializable
data class QualityThreshold(
    val metricName: String,
    val minValue: Double,
    val maxValue: Double,
    val targetValue: Double,
    val criticalValue: Double,
)

@Serializable
data class QualityReport(
    val projectName: String,
    val version: String,
    val platform: String,
    val metrics: UnifyQualityMetrics,
    val thresholds: List<QualityThreshold>,
    val violations: List<QualityViolation>,
    val recommendations: List<QualityRecommendation>,
    val overallScore: Double,
    val generatedAt: Long = getCurrentTimeMillis(),
)

@Serializable
data class QualityViolation(
    val metricName: String,
    val actualValue: Double,
    val expectedValue: Double,
    val severity: ViolationSeverity,
    val description: String,
    val filePath: String? = null,
    val lineNumber: Int? = null,
)

@Serializable
data class QualityRecommendation(
    val category: RecommendationCategory,
    val priority: RecommendationPriority,
    val title: String,
    val description: String,
    val actionItems: List<String>,
    val estimatedImpact: Double,
)

enum class ViolationSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL,
}

enum class RecommendationCategory {
    CODE_QUALITY,
    PERFORMANCE,
    TESTING,
    SECURITY,
    ARCHITECTURE,
}

enum class RecommendationPriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT,
}

@Serializable
data class QualityTrend(
    val metricName: String,
    val dataPoints: List<QualityDataPoint>,
    val trend: TrendDirection,
    val changeRate: Double,
)

@Serializable
data class QualityDataPoint(
    val timestamp: Long,
    val value: Double,
    val version: String,
)

enum class TrendDirection {
    IMPROVING,
    STABLE,
    DECLINING,
}

// 质量目标常量
object QualityTargets {
    const val MIN_CODE_REUSABILITY_RATE = 0.85 // 85%
    const val MAX_PLATFORM_SPECIFIC_CODE_RATE = 0.15 // 15%
    const val MAX_STARTUP_TIME_MS = 500L
    const val MIN_TEST_COVERAGE = 0.95 // 95%
    const val MIN_SECURITY_SCORE = 9.0 // 9/10
    const val MAX_TECHNICAL_DEBT_RATIO = 0.05 // 5%
    const val MIN_FRAME_RATE = 60.0
    const val MAX_MEMORY_USAGE_MB = 100L
}
