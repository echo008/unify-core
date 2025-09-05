package com.unify.performance.analysis

import kotlinx.coroutines.flow.Flow
import com.unify.core.platform.getCurrentTimeMillis
import com.unify.core.platform.getNanoTime
import kotlinx.coroutines.flow.flow
import com.unify.core.platform.getCurrentTimeMillis
import com.unify.core.platform.getNanoTime
import kotlinx.coroutines.delay
import com.unify.core.platform.getCurrentTimeMillis
import com.unify.core.platform.getNanoTime

/**
 * Unify性能分析实现
 * 跨平台性能监控和分析功能
 */

data class PerformanceMetrics(
    val cpuUsage: Float,
    val memoryUsage: Float,
    val frameRate: Float,
    val networkLatency: Long,
    val storageIO: Float,
    val batteryLevel: Int,
    val timestamp: Long = getCurrentTimeMillis()
)

data class PerformanceReport(
    val startTime: Long,
    val endTime: Long,
    val averageMetrics: PerformanceMetrics,
    val peakMetrics: PerformanceMetrics,
    val recommendations: List<String>,
    val issues: List<PerformanceIssue>
)

data class PerformanceIssue(
    val type: IssueType,
    val severity: IssueSeverity,
    val description: String,
    val suggestion: String,
    val timestamp: Long
)

enum class IssueType {
    HIGH_CPU_USAGE,
    HIGH_MEMORY_USAGE,
    LOW_FRAME_RATE,
    HIGH_NETWORK_LATENCY,
    SLOW_STORAGE_IO,
    LOW_BATTERY
}

enum class IssueSeverity {
    LOW, MEDIUM, HIGH, CRITICAL
}

interface UnifyPerformanceAnalyzer {
    fun startMonitoring()
    fun stopMonitoring()
    fun getMetricsFlow(): Flow<PerformanceMetrics>
    fun generateReport(durationMs: Long): PerformanceReport
    fun getRecommendations(): List<String>
    fun detectIssues(metrics: PerformanceMetrics): List<PerformanceIssue>
}

class UnifyPerformanceAnalysisImpl : UnifyPerformanceAnalyzer {
    
    private var isMonitoring = false
    private val metricsHistory = mutableListOf<PerformanceMetrics>()
    
    // 性能阈值常量
    companion object {
        private const val HIGH_CPU_THRESHOLD = 80f
        private const val HIGH_MEMORY_THRESHOLD = 85f
        private const val LOW_FRAME_RATE_THRESHOLD = 30f
        private const val HIGH_LATENCY_THRESHOLD = 500L
        private const val SLOW_IO_THRESHOLD = 10f
        private const val LOW_BATTERY_THRESHOLD = 20
        private const val METRICS_COLLECTION_INTERVAL = 1000L
        private const val MAX_HISTORY_SIZE = 1000
    }
    
    override fun startMonitoring() {
        isMonitoring = true
    }
    
    override fun stopMonitoring() {
        isMonitoring = false
    }
    
    override fun getMetricsFlow(): Flow<PerformanceMetrics> = flow {
        while (isMonitoring) {
            val metrics = collectCurrentMetrics()
            addToHistory(metrics)
            emit(metrics)
            delay(METRICS_COLLECTION_INTERVAL)
        }
    }
    
    override fun generateReport(durationMs: Long): PerformanceReport {
        val endTime = getCurrentTimeMillis()
        val startTime = endTime - durationMs
        
        val relevantMetrics = metricsHistory.filter { 
            it.timestamp >= startTime && it.timestamp <= endTime 
        }
        
        if (relevantMetrics.isEmpty()) {
            return createEmptyReport(startTime, endTime)
        }
        
        val averageMetrics = calculateAverageMetrics(relevantMetrics)
        val peakMetrics = calculatePeakMetrics(relevantMetrics)
        val issues = relevantMetrics.flatMap { detectIssues(it) }.distinctBy { it.type }
        val recommendations = generateRecommendations(averageMetrics, issues)
        
        return PerformanceReport(
            startTime = startTime,
            endTime = endTime,
            averageMetrics = averageMetrics,
            peakMetrics = peakMetrics,
            recommendations = recommendations,
            issues = issues
        )
    }
    
    override fun getRecommendations(): List<String> {
        if (metricsHistory.isEmpty()) {
            return listOf("开始监控以获取性能建议")
        }
        
        val recentMetrics = metricsHistory.takeLast(10)
        val averageMetrics = calculateAverageMetrics(recentMetrics)
        val issues = recentMetrics.flatMap { detectIssues(it) }.distinctBy { it.type }
        
        return generateRecommendations(averageMetrics, issues)
    }
    
    override fun detectIssues(metrics: PerformanceMetrics): List<PerformanceIssue> {
        val issues = mutableListOf<PerformanceIssue>()
        
        // CPU使用率检查
        if (metrics.cpuUsage > HIGH_CPU_THRESHOLD) {
            issues.add(
                PerformanceIssue(
                    type = IssueType.HIGH_CPU_USAGE,
                    severity = if (metrics.cpuUsage > 95f) IssueSeverity.CRITICAL else IssueSeverity.HIGH,
                    description = "CPU使用率过高: ${metrics.cpuUsage.toInt()}%",
                    suggestion = "关闭不必要的后台任务，优化算法复杂度",
                    timestamp = metrics.timestamp
                )
            )
        }
        
        // 内存使用率检查
        if (metrics.memoryUsage > HIGH_MEMORY_THRESHOLD) {
            issues.add(
                PerformanceIssue(
                    type = IssueType.HIGH_MEMORY_USAGE,
                    severity = if (metrics.memoryUsage > 95f) IssueSeverity.CRITICAL else IssueSeverity.HIGH,
                    description = "内存使用率过高: ${metrics.memoryUsage.toInt()}%",
                    suggestion = "释放未使用的对象，优化内存分配",
                    timestamp = metrics.timestamp
                )
            )
        }
        
        // 帧率检查
        if (metrics.frameRate < LOW_FRAME_RATE_THRESHOLD) {
            issues.add(
                PerformanceIssue(
                    type = IssueType.LOW_FRAME_RATE,
                    severity = if (metrics.frameRate < 15f) IssueSeverity.CRITICAL else IssueSeverity.MEDIUM,
                    description = "帧率过低: ${metrics.frameRate.toInt()} FPS",
                    suggestion = "减少UI复杂度，优化渲染性能",
                    timestamp = metrics.timestamp
                )
            )
        }
        
        // 网络延迟检查
        if (metrics.networkLatency > HIGH_LATENCY_THRESHOLD) {
            issues.add(
                PerformanceIssue(
                    type = IssueType.HIGH_NETWORK_LATENCY,
                    severity = if (metrics.networkLatency > 1000L) IssueSeverity.HIGH else IssueSeverity.MEDIUM,
                    description = "网络延迟过高: ${metrics.networkLatency}ms",
                    suggestion = "检查网络连接，优化请求策略",
                    timestamp = metrics.timestamp
                )
            )
        }
        
        // 存储I/O检查
        if (metrics.storageIO < SLOW_IO_THRESHOLD) {
            issues.add(
                PerformanceIssue(
                    type = IssueType.SLOW_STORAGE_IO,
                    severity = IssueSeverity.MEDIUM,
                    description = "存储I/O速度慢: ${metrics.storageIO} MB/s",
                    suggestion = "清理存储空间，优化文件操作",
                    timestamp = metrics.timestamp
                )
            )
        }
        
        // 电池电量检查
        if (metrics.batteryLevel < LOW_BATTERY_THRESHOLD) {
            issues.add(
                PerformanceIssue(
                    type = IssueType.LOW_BATTERY,
                    severity = if (metrics.batteryLevel < 10) IssueSeverity.HIGH else IssueSeverity.LOW,
                    description = "电池电量低: ${metrics.batteryLevel}%",
                    suggestion = "启用省电模式，减少后台活动",
                    timestamp = metrics.timestamp
                )
            )
        }
        
        return issues
    }
    
    private fun collectCurrentMetrics(): PerformanceMetrics {
        // 模拟性能数据收集
        return PerformanceMetrics(
            cpuUsage = (20..90).random().toFloat(),
            memoryUsage = (30..85).random().toFloat(),
            frameRate = (25..60).random().toFloat(),
            networkLatency = (10..300).random().toLong(),
            storageIO = (5..100).random().toFloat(),
            batteryLevel = (15..100).random()
        )
    }
    
    private fun addToHistory(metrics: PerformanceMetrics) {
        metricsHistory.add(metrics)
        if (metricsHistory.size > MAX_HISTORY_SIZE) {
            metricsHistory.removeAt(0)
        }
    }
    
    private fun calculateAverageMetrics(metrics: List<PerformanceMetrics>): PerformanceMetrics {
        if (metrics.isEmpty()) {
            return PerformanceMetrics(0f, 0f, 0f, 0L, 0f, 0)
        }
        
        return PerformanceMetrics(
            cpuUsage = metrics.map { it.cpuUsage }.average().toFloat(),
            memoryUsage = metrics.map { it.memoryUsage }.average().toFloat(),
            frameRate = metrics.map { it.frameRate }.average().toFloat(),
            networkLatency = metrics.map { it.networkLatency }.average().toLong(),
            storageIO = metrics.map { it.storageIO }.average().toFloat(),
            batteryLevel = metrics.map { it.batteryLevel }.average().toInt()
        )
    }
    
    private fun calculatePeakMetrics(metrics: List<PerformanceMetrics>): PerformanceMetrics {
        if (metrics.isEmpty()) {
            return PerformanceMetrics(0f, 0f, 0f, 0L, 0f, 0)
        }
        
        return PerformanceMetrics(
            cpuUsage = metrics.maxOf { it.cpuUsage },
            memoryUsage = metrics.maxOf { it.memoryUsage },
            frameRate = metrics.maxOf { it.frameRate },
            networkLatency = metrics.maxOf { it.networkLatency },
            storageIO = metrics.maxOf { it.storageIO },
            batteryLevel = metrics.maxOf { it.batteryLevel }
        )
    }
    
    private fun generateRecommendations(
        averageMetrics: PerformanceMetrics,
        issues: List<PerformanceIssue>
    ): List<String> {
        val recommendations = mutableListOf<String>()
        
        // 基于问题生成建议
        issues.forEach { issue ->
            when (issue.type) {
                IssueType.HIGH_CPU_USAGE -> {
                    recommendations.add("优化算法效率，减少计算密集型操作")
                    recommendations.add("使用异步处理避免阻塞主线程")
                }
                IssueType.HIGH_MEMORY_USAGE -> {
                    recommendations.add("及时释放不再使用的对象")
                    recommendations.add("使用内存池技术减少GC压力")
                }
                IssueType.LOW_FRAME_RATE -> {
                    recommendations.add("减少UI层级复杂度")
                    recommendations.add("使用硬件加速提升渲染性能")
                }
                IssueType.HIGH_NETWORK_LATENCY -> {
                    recommendations.add("实现请求缓存机制")
                    recommendations.add("使用CDN加速网络请求")
                }
                IssueType.SLOW_STORAGE_IO -> {
                    recommendations.add("清理临时文件和缓存")
                    recommendations.add("使用异步I/O操作")
                }
                IssueType.LOW_BATTERY -> {
                    recommendations.add("启用省电模式")
                    recommendations.add("减少后台任务执行频率")
                }
            }
        }
        
        // 基于平均指标生成通用建议
        if (averageMetrics.cpuUsage > 50f) {
            recommendations.add("考虑使用更高效的数据结构")
        }
        
        if (averageMetrics.memoryUsage > 60f) {
            recommendations.add("定期进行内存清理")
        }
        
        if (averageMetrics.frameRate < 45f) {
            recommendations.add("优化UI渲染流程")
        }
        
        return recommendations.distinct()
    }
    
    private fun createEmptyReport(startTime: Long, endTime: Long): PerformanceReport {
        return PerformanceReport(
            startTime = startTime,
            endTime = endTime,
            averageMetrics = PerformanceMetrics(0f, 0f, 0f, 0L, 0f, 0),
            peakMetrics = PerformanceMetrics(0f, 0f, 0f, 0L, 0f, 0),
            recommendations = listOf("暂无数据，请开始性能监控"),
            issues = emptyList()
        )
    }
}
