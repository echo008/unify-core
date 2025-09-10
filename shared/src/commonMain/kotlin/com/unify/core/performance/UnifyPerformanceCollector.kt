package com.unify.core.performance

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

/**
 * 统一性能指标收集器
 * 提供跨平台的性能监控、指标收集和分析功能
 */
class UnifyPerformanceCollector(
    private val config: PerformanceConfig = PerformanceConfig()
) {
    private val _collectorState = MutableStateFlow(CollectorState.INITIALIZING)
    val collectorState: StateFlow<CollectorState> = _collectorState.asStateFlow()
    
    // 性能指标收集器
    private val cpuCollector = CPUMetricsCollector()
    private val memoryCollector = MemoryMetricsCollector()
    private val networkCollector = NetworkMetricsCollector()
    private val diskCollector = DiskMetricsCollector()
    private val batteryCollector = BatteryMetricsCollector()
    
    // 性能数据存储和分析
    private val metricsStorage = PerformanceMetricsStorage(config.maxStorageSize)
    private val metricsAnalyzer = PerformanceAnalyzer()
    private val alertManager = PerformanceAlertManager()
    
    // 实时性能数据流
    private val _performanceMetrics = MutableStateFlow(PerformanceSnapshot())
    val performanceMetrics: StateFlow<PerformanceSnapshot> = _performanceMetrics.asStateFlow()
    
    // 性能统计
    private val _performanceStats = MutableStateFlow(PerformanceStats())
    val performanceStats: StateFlow<PerformanceStats> = _performanceStats.asStateFlow()
    
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    init {
        initializePerformanceCollection()
    }
    
    /**
     * 开始性能监控
     */
    suspend fun startMonitoring(): MonitoringResult {
        return try {
            _collectorState.value = CollectorState.STARTING
            
            // 启动各个指标收集器
            cpuCollector.start()
            memoryCollector.start()
            networkCollector.start()
            diskCollector.start()
            batteryCollector.start()
            
            // 启动数据收集任务
            startCollectionTasks()
            
            // 启动分析任务
            startAnalysisTasks()
            
            _collectorState.value = CollectorState.RUNNING
            
            updateStats { it.copy(monitoringSessions = it.monitoringSessions + 1) }
            
            MonitoringResult.Success("性能监控已启动")
        } catch (e: Exception) {
            _collectorState.value = CollectorState.ERROR
            MonitoringResult.Error("启动性能监控失败: ${e.message}")
        }
    }
    
    /**
     * 停止性能监控
     */
    suspend fun stopMonitoring(): MonitoringResult {
        return try {
            _collectorState.value = CollectorState.STOPPING
            
            // 停止各个指标收集器
            cpuCollector.stop()
            memoryCollector.stop()
            networkCollector.stop()
            diskCollector.stop()
            batteryCollector.stop()
            
            _collectorState.value = CollectorState.IDLE
            
            MonitoringResult.Success("性能监控已停止")
        } catch (e: Exception) {
            _collectorState.value = CollectorState.ERROR
            MonitoringResult.Error("停止性能监控失败: ${e.message}")
        }
    }
    
    /**
     * 获取当前性能快照
     */
    suspend fun getCurrentSnapshot(): SnapshotResult {
        return try {
            val snapshot = PerformanceSnapshot(
                timestamp = com.unify.core.platform.getCurrentTimeMillis(),
                cpuMetrics = cpuCollector.getCurrentMetrics(),
                memoryMetrics = memoryCollector.getCurrentMetrics(),
                networkMetrics = networkCollector.getCurrentMetrics(),
                diskMetrics = diskCollector.getCurrentMetrics(),
                batteryMetrics = batteryCollector.getCurrentMetrics(),
                systemLoad = calculateSystemLoad()
            )
            
            // 存储快照
            metricsStorage.storeSnapshot(snapshot)
            _performanceMetrics.value = snapshot
            
            updateStats { it.copy(snapshotsTaken = it.snapshotsTaken + 1) }
            
            SnapshotResult.Success(snapshot)
        } catch (e: Exception) {
            SnapshotResult.Error("获取性能快照失败: ${e.message}")
        }
    }
    
    /**
     * 获取历史性能数据
     */
    suspend fun getHistoricalData(
        startTime: Long,
        endTime: Long,
        interval: Long = 60000L // 1分钟间隔
    ): HistoricalDataResult {
        return try {
            val snapshots = metricsStorage.getSnapshots(startTime, endTime)
            val aggregatedData = aggregateSnapshots(snapshots, interval)
            
            HistoricalDataResult.Success(aggregatedData)
        } catch (e: Exception) {
            HistoricalDataResult.Error("获取历史数据失败: ${e.message}")
        }
    }
    
    /**
     * 分析性能趋势
     */
    suspend fun analyzePerformanceTrends(
        duration: Long = 24 * 60 * 60 * 1000L // 24小时
    ): TrendAnalysisResult {
        return try {
            val endTime = com.unify.core.platform.getCurrentTimeMillis()
            val startTime = endTime - duration
            val snapshots = metricsStorage.getSnapshots(startTime, endTime)
            
            val analysis = metricsAnalyzer.analyzeTrends(snapshots)
            
            TrendAnalysisResult.Success(analysis)
        } catch (e: Exception) {
            TrendAnalysisResult.Error("性能趋势分析失败: ${e.message}")
        }
    }
    
    /**
     * 检测性能异常
     */
    suspend fun detectAnomalies(
        threshold: Double = 2.0 // 标准差阈值
    ): AnomalyDetectionResult {
        return try {
            val recentSnapshots = metricsStorage.getRecentSnapshots(100)
            val anomalies = metricsAnalyzer.detectAnomalies(recentSnapshots, threshold)
            
            // 触发告警
            anomalies.forEach { anomaly ->
                alertManager.triggerAlert(anomaly)
            }
            
            updateStats { it.copy(anomaliesDetected = it.anomaliesDetected + anomalies.size) }
            
            AnomalyDetectionResult.Success(anomalies)
        } catch (e: Exception) {
            AnomalyDetectionResult.Error("异常检测失败: ${e.message}")
        }
    }
    
    /**
     * 生成性能报告
     */
    suspend fun generatePerformanceReport(
        startTime: Long,
        endTime: Long,
        reportType: ReportType = ReportType.COMPREHENSIVE
    ): ReportGenerationResult {
        return try {
            val snapshots = metricsStorage.getSnapshots(startTime, endTime)
            val report = when (reportType) {
                ReportType.COMPREHENSIVE -> generateComprehensiveReport(snapshots)
                ReportType.SUMMARY -> generateSummaryReport(snapshots)
                ReportType.ALERT_FOCUSED -> generateAlertReport(snapshots)
            }
            
            updateStats { it.copy(reportsGenerated = it.reportsGenerated + 1) }
            
            ReportGenerationResult.Success(report)
        } catch (e: Exception) {
            ReportGenerationResult.Error("生成性能报告失败: ${e.message}")
        }
    }
    
    /**
     * 设置性能阈值
     */
    suspend fun setPerformanceThresholds(thresholds: PerformanceThresholds): ThresholdResult {
        return try {
            alertManager.updateThresholds(thresholds)
            ThresholdResult.Success("性能阈值已更新")
        } catch (e: Exception) {
            ThresholdResult.Error("设置性能阈值失败: ${e.message}")
        }
    }
    
    /**
     * 获取性能告警
     */
    fun getPerformanceAlerts(
        severity: AlertSeverity? = null,
        limit: Int = 50
    ): List<PerformanceAlert> {
        return alertManager.getAlerts(severity, limit)
    }
    
    /**
     * 清理历史数据
     */
    suspend fun cleanupHistoricalData(
        retentionPeriod: Long = 7 * 24 * 60 * 60 * 1000L // 7天
    ): CleanupResult {
        return try {
            val cutoffTime = com.unify.core.platform.getCurrentTimeMillis() - retentionPeriod
            val removedCount = metricsStorage.cleanupOldData(cutoffTime)
            
            updateStats { it.copy(cleanupOperations = it.cleanupOperations + 1) }
            
            CleanupResult.Success("已清理 $removedCount 条历史记录")
        } catch (e: Exception) {
            CleanupResult.Error("清理历史数据失败: ${e.message}")
        }
    }
    
    /**
     * 导出性能数据
     */
    suspend fun exportPerformanceData(
        startTime: Long,
        endTime: Long,
        format: ExportFormat = ExportFormat.JSON
    ): ExportResult {
        return try {
            val snapshots = metricsStorage.getSnapshots(startTime, endTime)
            val exportData = when (format) {
                ExportFormat.JSON -> json.encodeToString(snapshots)
                ExportFormat.CSV -> convertToCSV(snapshots)
            }
            
            updateStats { it.copy(dataExports = it.dataExports + 1) }
            
            ExportResult.Success(exportData)
        } catch (e: Exception) {
            ExportResult.Error("导出性能数据失败: ${e.message}")
        }
    }
    
    /**
     * 获取性能统计信息
     */
    fun getPerformanceStatistics(): PerformanceStatistics {
        val stats = _performanceStats.value
        val currentSnapshot = _performanceMetrics.value
        
        return PerformanceStatistics(
            collectorState = _collectorState.value,
            currentSnapshot = currentSnapshot,
            totalSnapshots = metricsStorage.getTotalCount(),
            monitoringSessions = stats.monitoringSessions,
            anomaliesDetected = stats.anomaliesDetected,
            reportsGenerated = stats.reportsGenerated,
            dataExports = stats.dataExports,
            cleanupOperations = stats.cleanupOperations,
            averageCollectionTime = stats.averageCollectionTime,
            storageUsage = metricsStorage.getStorageUsage()
        )
    }
    
    /**
     * 启动数据收集任务
     */
    private fun startCollectionTasks() {
        // 定期收集性能数据
        coroutineScope.launch {
            while (isActive && _collectorState.value == CollectorState.RUNNING) {
                try {
                    getCurrentSnapshot()
                    delay(config.collectionInterval)
                } catch (e: Exception) {
                    println("性能数据收集失败: ${e.message}")
                    delay(config.collectionInterval)
                }
            }
        }
        
        // 定期清理过期数据
        coroutineScope.launch {
            while (isActive) {
                try {
                    cleanupHistoricalData(config.dataRetentionPeriod)
                    delay(config.cleanupInterval)
                } catch (e: Exception) {
                    println("数据清理失败: ${e.message}")
                    delay(config.cleanupInterval)
                }
            }
        }
    }
    
    /**
     * 启动分析任务
     */
    private fun startAnalysisTasks() {
        // 定期异常检测
        coroutineScope.launch {
            while (isActive && _collectorState.value == CollectorState.RUNNING) {
                try {
                    detectAnomalies(config.anomalyThreshold)
                    delay(config.analysisInterval)
                } catch (e: Exception) {
                    println("异常检测失败: ${e.message}")
                    delay(config.analysisInterval)
                }
            }
        }
    }
    
    /**
     * 计算系统负载
     */
    private suspend fun calculateSystemLoad(): SystemLoad {
        val cpuMetrics = cpuCollector.getCurrentMetrics()
        val memoryMetrics = memoryCollector.getCurrentMetrics()
        
        val cpuLoad = cpuMetrics.usage
        val memoryLoad = if (memoryMetrics.total > 0) {
            (memoryMetrics.used.toDouble() / memoryMetrics.total) * 100
        } else 0.0
        
        val overallLoad = (cpuLoad + memoryLoad) / 2
        
        return SystemLoad(
            cpu = cpuLoad,
            memory = memoryLoad,
            overall = overallLoad,
            status = when {
                overallLoad < 30 -> LoadStatus.LOW
                overallLoad < 70 -> LoadStatus.MEDIUM
                overallLoad < 90 -> LoadStatus.HIGH
                else -> LoadStatus.CRITICAL
            }
        )
    }
    
    /**
     * 聚合快照数据
     */
    private fun aggregateSnapshots(
        snapshots: List<PerformanceSnapshot>,
        interval: Long
    ): List<AggregatedMetrics> {
        return snapshots.groupBy { it.timestamp / interval * interval }
            .map { (timestamp, group) ->
                AggregatedMetrics(
                    timestamp = timestamp,
                    avgCpuUsage = group.map { it.cpuMetrics.usage }.average(),
                    avgMemoryUsage = group.map { 
                        if (it.memoryMetrics.total > 0) {
                            (it.memoryMetrics.used.toDouble() / it.memoryMetrics.total) * 100
                        } else 0.0
                    }.average(),
                    avgNetworkThroughput = group.map { 
                        it.networkMetrics.bytesReceived + it.networkMetrics.bytesSent 
                    }.average(),
                    avgDiskUsage = group.map { it.diskMetrics.usage }.average(),
                    sampleCount = group.size
                )
            }
            .sortedBy { it.timestamp }
    }
    
    /**
     * 生成综合报告
     */
    private suspend fun generateComprehensiveReport(
        snapshots: List<PerformanceSnapshot>
    ): PerformanceReport {
        val analysis = metricsAnalyzer.analyzeTrends(snapshots)
        val anomalies = metricsAnalyzer.detectAnomalies(snapshots, config.anomalyThreshold)
        
        return PerformanceReport(
            reportId = "report_${com.unify.core.platform.getCurrentTimeMillis()}",
            reportType = ReportType.COMPREHENSIVE,
            generatedAt = com.unify.core.platform.getCurrentTimeMillis(),
            period = ReportPeriod(
                startTime = snapshots.firstOrNull()?.timestamp ?: 0L,
                endTime = snapshots.lastOrNull()?.timestamp ?: 0L
            ),
            summary = generateReportSummary(snapshots),
            trendAnalysis = analysis,
            anomalies = anomalies,
            recommendations = generateRecommendations(analysis, anomalies)
        )
    }
    
    /**
     * 生成摘要报告
     */
    private fun generateSummaryReport(snapshots: List<PerformanceSnapshot>): PerformanceReport {
        return PerformanceReport(
            reportId = "summary_${com.unify.core.platform.getCurrentTimeMillis()}",
            reportType = ReportType.SUMMARY,
            generatedAt = com.unify.core.platform.getCurrentTimeMillis(),
            period = ReportPeriod(
                startTime = snapshots.firstOrNull()?.timestamp ?: 0L,
                endTime = snapshots.lastOrNull()?.timestamp ?: 0L
            ),
            summary = generateReportSummary(snapshots),
            trendAnalysis = null,
            anomalies = emptyList(),
            recommendations = emptyList()
        )
    }
    
    /**
     * 生成告警报告
     */
    private suspend fun generateAlertReport(snapshots: List<PerformanceSnapshot>): PerformanceReport {
        val anomalies = metricsAnalyzer.detectAnomalies(snapshots, config.anomalyThreshold)
        
        return PerformanceReport(
            reportId = "alert_${com.unify.core.platform.getCurrentTimeMillis()}",
            reportType = ReportType.ALERT_FOCUSED,
            generatedAt = com.unify.core.platform.getCurrentTimeMillis(),
            period = ReportPeriod(
                startTime = snapshots.firstOrNull()?.timestamp ?: 0L,
                endTime = snapshots.lastOrNull()?.timestamp ?: 0L
            ),
            summary = null,
            trendAnalysis = null,
            anomalies = anomalies,
            recommendations = generateRecommendations(null, anomalies)
        )
    }
    
    /**
     * 生成报告摘要
     */
    private fun generateReportSummary(snapshots: List<PerformanceSnapshot>): ReportSummary {
        if (snapshots.isEmpty()) {
            return ReportSummary(0.0, 0.0, 0.0, 0.0, 0.0, 0)
        }
        
        val avgCpu = snapshots.map { it.cpuMetrics.usage }.average()
        val avgMemory = snapshots.map { 
            if (it.memoryMetrics.total > 0) {
                (it.memoryMetrics.used.toDouble() / it.memoryMetrics.total) * 100
            } else 0.0
        }.average()
        val avgNetwork = snapshots.map { 
            it.networkMetrics.bytesReceived + it.networkMetrics.bytesSent 
        }.average()
        val avgDisk = snapshots.map { it.diskMetrics.usage }.average()
        val avgBattery = snapshots.mapNotNull { it.batteryMetrics?.level }.average()
        
        return ReportSummary(avgCpu, avgMemory, avgNetwork, avgDisk, avgBattery, snapshots.size)
    }
    
    /**
     * 生成优化建议
     */
    private fun generateRecommendations(
        analysis: TrendAnalysis?,
        anomalies: List<PerformanceAnomaly>
    ): List<String> {
        val recommendations = mutableListOf<String>()
        
        analysis?.let { trend ->
            if (trend.cpuTrend > 0.1) {
                recommendations.add("CPU使用率呈上升趋势，建议检查高CPU消耗的进程")
            }
            if (trend.memoryTrend > 0.1) {
                recommendations.add("内存使用率持续增长，可能存在内存泄漏")
            }
        }
        
        anomalies.forEach { anomaly ->
            when (anomaly.type) {
                AnomalyType.CPU_SPIKE -> recommendations.add("检测到CPU使用率异常峰值，建议优化计算密集型任务")
                AnomalyType.MEMORY_LEAK -> recommendations.add("检测到内存使用异常，建议检查内存泄漏")
                AnomalyType.NETWORK_CONGESTION -> recommendations.add("网络流量异常，建议检查网络连接")
                AnomalyType.DISK_IO_HIGH -> recommendations.add("磁盘I/O异常，建议优化磁盘访问")
                AnomalyType.BATTERY_DRAIN -> recommendations.add("检测到电池消耗异常，建议优化电源管理")
                AnomalyType.SYSTEM_OVERLOAD -> recommendations.add("系统负载过高，建议减少并发任务")
            }
        }
        
        return recommendations
    }
    
    /**
     * 转换为CSV格式
     */
    private fun convertToCSV(snapshots: List<PerformanceSnapshot>): String {
        val header = "Timestamp,CPU_Usage,Memory_Used,Memory_Total,Network_Received,Network_Sent,Disk_Usage,Battery_Level"
        val rows = snapshots.map { snapshot ->
            "${snapshot.timestamp},${snapshot.cpuMetrics.usage},${snapshot.memoryMetrics.used}," +
            "${snapshot.memoryMetrics.total},${snapshot.networkMetrics.bytesReceived}," +
            "${snapshot.networkMetrics.bytesSent},${snapshot.diskMetrics.usage}," +
            "${snapshot.batteryMetrics?.level ?: ""}"
        }
        
        return listOf(header).plus(rows).joinToString("\n")
    }
    
    /**
     * 初始化性能收集系统
     */
    private fun initializePerformanceCollection() {
        coroutineScope.launch {
            try {
                _collectorState.value = CollectorState.INITIALIZING
                
                // 初始化各个收集器
                cpuCollector.initialize()
                memoryCollector.initialize()
                networkCollector.initialize()
                diskCollector.initialize()
                batteryCollector.initialize()
                
                // 初始化存储和分析器
                metricsStorage.initialize()
                metricsAnalyzer.initialize()
                alertManager.initialize()
                
                _collectorState.value = CollectorState.IDLE
            } catch (e: Exception) {
                _collectorState.value = CollectorState.ERROR
                println("性能收集系统初始化失败: ${e.message}")
            }
        }
    }
    
    /**
     * 更新统计信息
     */
    private fun updateStats(update: (PerformanceStats) -> PerformanceStats) {
        _performanceStats.value = update(_performanceStats.value)
    }
    
    /**
     * 清理资源
     */
    fun cleanup() {
        coroutineScope.cancel()
        metricsStorage.cleanup()
        alertManager.cleanup()
    }
}

/**
 * 性能配置
 */
@Serializable
data class PerformanceConfig(
    val collectionInterval: Long = 5000L, // 5秒
    val analysisInterval: Long = 60000L, // 1分钟
    val cleanupInterval: Long = 60 * 60 * 1000L, // 1小时
    val dataRetentionPeriod: Long = 7 * 24 * 60 * 60 * 1000L, // 7天
    val maxStorageSize: Int = 100000,
    val anomalyThreshold: Double = 2.0,
    val enableRealTimeAnalysis: Boolean = true,
    val enableAlerting: Boolean = true
)

/**
 * 收集器状态
 */
enum class CollectorState {
    INITIALIZING,   // 初始化中
    IDLE,           // 空闲
    STARTING,       // 启动中
    RUNNING,        // 运行中
    STOPPING,       // 停止中
    ERROR           // 错误
}
