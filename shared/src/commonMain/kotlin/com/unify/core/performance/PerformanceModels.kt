package com.unify.core.performance

import kotlinx.serialization.Serializable

/**
 * 性能快照
 */
@Serializable
data class PerformanceSnapshot(
    val timestamp: Long = com.unify.core.platform.getCurrentTimeMillis(),
    val cpuMetrics: CPUMetrics = CPUMetrics(),
    val memoryMetrics: MemoryMetrics = MemoryMetrics(),
    val networkMetrics: NetworkMetrics = NetworkMetrics(),
    val diskMetrics: DiskMetrics = DiskMetrics(),
    val batteryMetrics: BatteryMetrics? = null,
    val systemLoad: SystemLoad = SystemLoad()
)

/**
 * CPU指标
 */
@Serializable
data class CPUMetrics(
    val usage: Double = 0.0,        // CPU使用率百分比
    val cores: Int = 1,             // CPU核心数
    val frequency: Long = 0L,       // CPU频率(Hz)
    val temperature: Double? = null, // CPU温度(摄氏度)
    val loadAverage: Double = 0.0   // 负载平均值
)

/**
 * 内存指标
 */
@Serializable
data class MemoryMetrics(
    val total: Long = 0L,           // 总内存(字节)
    val used: Long = 0L,            // 已用内存(字节)
    val available: Long = 0L,       // 可用内存(字节)
    val cached: Long = 0L,          // 缓存内存(字节)
    val swapTotal: Long = 0L,       // 交换空间总量(字节)
    val swapUsed: Long = 0L         // 交换空间使用量(字节)
)

/**
 * 网络指标
 */
@Serializable
data class NetworkMetrics(
    val bytesReceived: Long = 0L,   // 接收字节数
    val bytesSent: Long = 0L,       // 发送字节数
    val packetsReceived: Long = 0L, // 接收包数
    val packetsSent: Long = 0L,     // 发送包数
    val errors: Long = 0L,          // 错误数
    val drops: Long = 0L,           // 丢包数
    val bandwidth: Long = 0L        // 带宽(bps)
)

/**
 * 磁盘指标
 */
@Serializable
data class DiskMetrics(
    val usage: Double = 0.0,        // 磁盘使用率百分比
    val totalSpace: Long = 0L,      // 总空间(字节)
    val usedSpace: Long = 0L,       // 已用空间(字节)
    val freeSpace: Long = 0L,       // 可用空间(字节)
    val readBytes: Long = 0L,       // 读取字节数
    val writeBytes: Long = 0L,      // 写入字节数
    val readOps: Long = 0L,         // 读操作数
    val writeOps: Long = 0L         // 写操作数
)

/**
 * 电池指标
 */
@Serializable
data class BatteryMetrics(
    val level: Double = 100.0,      // 电量百分比
    val isCharging: Boolean = false, // 是否充电中
    val voltage: Double = 0.0,      // 电压(伏特)
    val temperature: Double = 0.0,  // 温度(摄氏度)
    val health: BatteryHealth = BatteryHealth.GOOD, // 电池健康状态
    val estimatedTime: Long = 0L    // 预估剩余时间(毫秒)
)

/**
 * 电池健康状态
 */
enum class BatteryHealth {
    UNKNOWN,    // 未知
    GOOD,       // 良好
    OVERHEAT,   // 过热
    DEAD,       // 损坏
    COLD        // 过冷
}

/**
 * 系统负载
 */
@Serializable
data class SystemLoad(
    val cpu: Double = 0.0,          // CPU负载
    val memory: Double = 0.0,       // 内存负载
    val overall: Double = 0.0,      // 整体负载
    val status: LoadStatus = LoadStatus.LOW // 负载状态
)

/**
 * 负载状态
 */
enum class LoadStatus {
    LOW,        // 低负载
    MEDIUM,     // 中等负载
    HIGH,       // 高负载
    CRITICAL    // 临界负载
}

/**
 * 聚合指标
 */
@Serializable
data class AggregatedMetrics(
    val timestamp: Long,
    val avgCpuUsage: Double,
    val avgMemoryUsage: Double,
    val avgNetworkThroughput: Double,
    val avgDiskUsage: Double,
    val sampleCount: Int
)

/**
 * 趋势分析
 */
@Serializable
data class TrendAnalysis(
    val cpuTrend: Double,           // CPU使用趋势
    val memoryTrend: Double,        // 内存使用趋势
    val networkTrend: Double,       // 网络流量趋势
    val diskTrend: Double,          // 磁盘使用趋势
    val overallTrend: Double,       // 整体趋势
    val analysisWindow: Long,       // 分析时间窗口
    val confidence: Double          // 置信度
)

/**
 * 性能异常
 */
@Serializable
data class PerformanceAnomaly(
    val id: String,
    val type: AnomalyType,
    val severity: AlertSeverity,
    val timestamp: Long,
    val value: Double,
    val threshold: Double,
    val description: String,
    val affectedMetric: String
)

/**
 * 异常类型
 */
enum class AnomalyType {
    CPU_SPIKE,          // CPU峰值
    MEMORY_LEAK,        // 内存泄漏
    NETWORK_CONGESTION, // 网络拥塞
    DISK_IO_HIGH,       // 磁盘I/O过高
    BATTERY_DRAIN,      // 电池耗电异常
    SYSTEM_OVERLOAD     // 系统过载
}

/**
 * 告警严重程度
 */
enum class AlertSeverity {
    LOW,        // 低
    MEDIUM,     // 中
    HIGH,       // 高
    CRITICAL    // 严重
}

/**
 * 性能告警
 */
@Serializable
data class PerformanceAlert(
    val id: String,
    val severity: AlertSeverity,
    val type: AnomalyType,
    val message: String,
    val timestamp: Long,
    val acknowledged: Boolean = false,
    val resolvedAt: Long? = null,
    val metadata: Map<String, String> = emptyMap()
)

/**
 * 性能阈值
 */
@Serializable
data class PerformanceThresholds(
    val cpuUsageThreshold: Double = 80.0,      // CPU使用率阈值
    val memoryUsageThreshold: Double = 85.0,   // 内存使用率阈值
    val diskUsageThreshold: Double = 90.0,     // 磁盘使用率阈值
    val networkErrorThreshold: Long = 100L,    // 网络错误阈值
    val batteryLowThreshold: Double = 20.0,    // 电池低电量阈值
    val temperatureThreshold: Double = 70.0    // 温度阈值
)

/**
 * 性能报告
 */
@Serializable
data class PerformanceReport(
    val reportId: String,
    val reportType: ReportType,
    val generatedAt: Long,
    val period: ReportPeriod,
    val summary: ReportSummary?,
    val trendAnalysis: TrendAnalysis?,
    val anomalies: List<PerformanceAnomaly>,
    val recommendations: List<String>
)

/**
 * 报告类型
 */
enum class ReportType {
    COMPREHENSIVE,  // 综合报告
    SUMMARY,        // 摘要报告
    ALERT_FOCUSED   // 告警重点报告
}

/**
 * 报告周期
 */
@Serializable
data class ReportPeriod(
    val startTime: Long,
    val endTime: Long
)

/**
 * 报告摘要
 */
@Serializable
data class ReportSummary(
    val avgCpuUsage: Double,
    val avgMemoryUsage: Double,
    val avgNetworkThroughput: Double,
    val avgDiskUsage: Double,
    val avgBatteryLevel: Double,
    val totalSamples: Int
)

/**
 * 导出格式
 */
enum class ExportFormat {
    JSON,   // JSON格式
    CSV     // CSV格式
}

/**
 * 性能统计
 */
@Serializable
data class PerformanceStats(
    val monitoringSessions: Int = 0,
    val snapshotsTaken: Int = 0,
    val anomaliesDetected: Int = 0,
    val reportsGenerated: Int = 0,
    val dataExports: Int = 0,
    val cleanupOperations: Int = 0,
    val averageCollectionTime: Double = 0.0
)

/**
 * 性能统计信息
 */
data class PerformanceStatistics(
    val collectorState: CollectorState,
    val currentSnapshot: PerformanceSnapshot,
    val totalSnapshots: Int,
    val monitoringSessions: Int,
    val anomaliesDetected: Int,
    val reportsGenerated: Int,
    val dataExports: Int,
    val cleanupOperations: Int,
    val averageCollectionTime: Double,
    val storageUsage: StorageUsage
)

/**
 * 存储使用情况
 */
@Serializable
data class StorageUsage(
    val totalEntries: Int = 0,
    val usedSpace: Long = 0L,
    val maxSpace: Long = 0L,
    val utilizationPercent: Double = 0.0
)

// 结果类型定义

/**
 * 监控结果
 */
sealed class MonitoringResult {
    data class Success(val message: String) : MonitoringResult()
    data class Error(val message: String) : MonitoringResult()
}

/**
 * 快照结果
 */
sealed class SnapshotResult {
    data class Success(val snapshot: PerformanceSnapshot) : SnapshotResult()
    data class Error(val message: String) : SnapshotResult()
}

/**
 * 历史数据结果
 */
sealed class HistoricalDataResult {
    data class Success(val data: List<AggregatedMetrics>) : HistoricalDataResult()
    data class Error(val message: String) : HistoricalDataResult()
}

/**
 * 趋势分析结果
 */
sealed class TrendAnalysisResult {
    data class Success(val analysis: TrendAnalysis) : TrendAnalysisResult()
    data class Error(val message: String) : TrendAnalysisResult()
}

/**
 * 异常检测结果
 */
sealed class AnomalyDetectionResult {
    data class Success(val anomalies: List<PerformanceAnomaly>) : AnomalyDetectionResult()
    data class Error(val message: String) : AnomalyDetectionResult()
}

/**
 * 报告生成结果
 */
sealed class ReportGenerationResult {
    data class Success(val report: PerformanceReport) : ReportGenerationResult()
    data class Error(val message: String) : ReportGenerationResult()
}

/**
 * 阈值设置结果
 */
sealed class ThresholdResult {
    data class Success(val message: String) : ThresholdResult()
    data class Error(val message: String) : ThresholdResult()
}

/**
 * 清理结果
 */
sealed class CleanupResult {
    data class Success(val message: String) : CleanupResult()
    data class Error(val message: String) : CleanupResult()
}

/**
 * 导出结果
 */
sealed class ExportResult {
    data class Success(val data: String) : ExportResult()
    data class Error(val message: String) : ExportResult()
}
