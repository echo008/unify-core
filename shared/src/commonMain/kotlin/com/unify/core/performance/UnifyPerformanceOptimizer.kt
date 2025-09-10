package com.unify.core.performance

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

/**
 * 统一性能优化器
 * 提供自动化性能优化和智能调优功能
 */
class UnifyPerformanceOptimizer(
    private val config: OptimizerConfig = OptimizerConfig(),
    private val collector: UnifyPerformanceCollector
) {
    private val _optimizerState = MutableStateFlow(OptimizerState.IDLE)
    val optimizerState: StateFlow<OptimizerState> = _optimizerState.asStateFlow()
    
    // 优化策略引擎
    private val strategyEngine = OptimizationStrategyEngine()
    private val ruleEngine = OptimizationRuleEngine()
    private val actionExecutor = OptimizationActionExecutor()
    
    // 优化历史和统计
    private val optimizationHistory = mutableListOf<OptimizationRecord>()
    private val _optimizationStats = MutableStateFlow(OptimizationStats())
    val optimizationStats: StateFlow<OptimizationStats> = _optimizationStats.asStateFlow()
    
    // 学习和适应系统
    private val learningEngine = PerformanceLearningEngine()
    private val adaptiveThresholds = AdaptiveThresholds()
    
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    init {
        initializeOptimizer()
    }
    
    /**
     * 启动自动优化
     */
    suspend fun startAutoOptimization(): OptimizationResult {
        return try {
            _optimizerState.value = OptimizerState.STARTING
            
            // 启动性能监控
            collector.startMonitoring()
            
            // 启动优化任务
            startOptimizationTasks()
            
            _optimizerState.value = OptimizerState.RUNNING
            
            updateStats { it.copy(optimizationSessions = it.optimizationSessions + 1) }
            
            OptimizationResult.Success("自动优化已启动")
        } catch (e: Exception) {
            _optimizerState.value = OptimizerState.ERROR
            OptimizationResult.Error("启动自动优化失败: ${e.message}")
        }
    }
    
    /**
     * 停止自动优化
     */
    suspend fun stopAutoOptimization(): OptimizationResult {
        return try {
            _optimizerState.value = OptimizerState.STOPPING
            
            // 停止优化任务
            coroutineScope.coroutineContext.cancelChildren()
            
            _optimizerState.value = OptimizerState.IDLE
            
            OptimizationResult.Success("自动优化已停止")
        } catch (e: Exception) {
            _optimizerState.value = OptimizerState.ERROR
            OptimizationResult.Error("停止自动优化失败: ${e.message}")
        }
    }
    
    /**
     * 执行单次优化
     */
    suspend fun performOptimization(
        targetMetrics: List<OptimizationTarget> = emptyList()
    ): OptimizationResult {
        return try {
            _optimizerState.value = OptimizerState.OPTIMIZING
            
            // 获取当前性能快照
            val currentSnapshot = collector.getCurrentSnapshot()
            if (currentSnapshot is SnapshotResult.Error) {
                return OptimizationResult.Error("获取性能数据失败: ${currentSnapshot.message}")
            }
            
            val snapshot = (currentSnapshot as SnapshotResult.Success).snapshot
            
            // 分析性能问题
            val issues = analyzePerformanceIssues(snapshot)
            
            // 生成优化策略
            val strategies = strategyEngine.generateStrategies(issues, targetMetrics)
            
            // 执行优化操作
            val results = executeOptimizations(strategies, snapshot)
            
            // 记录优化历史
            recordOptimization(snapshot, strategies, results)
            
            // 学习和适应
            learningEngine.learn(snapshot, strategies, results)
            
            _optimizerState.value = OptimizerState.RUNNING
            
            updateStats { stats ->
                stats.copy(
                    totalOptimizations = stats.totalOptimizations + 1,
                    successfulOptimizations = stats.successfulOptimizations + results.count { it.success },
                    averageImprovementPercent = calculateAverageImprovement(results)
                )
            }
            
            OptimizationResult.Success("优化完成，执行了 ${results.size} 项优化操作")
        } catch (e: Exception) {
            _optimizerState.value = OptimizerState.ERROR
            OptimizationResult.Error("性能优化失败: ${e.message}")
        }
    }
    
    /**
     * 配置优化策略
     */
    suspend fun configureOptimizationStrategy(
        strategy: OptimizationStrategy
    ): ConfigurationResult {
        return try {
            strategyEngine.addStrategy(strategy)
            ConfigurationResult.Success("优化策略配置成功")
        } catch (e: Exception) {
            ConfigurationResult.Error("配置优化策略失败: ${e.message}")
        }
    }
    
    /**
     * 设置优化规则
     */
    suspend fun setOptimizationRules(
        rules: List<OptimizationRule>
    ): ConfigurationResult {
        return try {
            ruleEngine.setRules(rules)
            ConfigurationResult.Success("优化规则设置成功")
        } catch (e: Exception) {
            ConfigurationResult.Error("设置优化规则失败: ${e.message}")
        }
    }
    
    /**
     * 获取优化建议
     */
    suspend fun getOptimizationRecommendations(
        snapshot: PerformanceSnapshot? = null
    ): RecommendationResult {
        return try {
            val currentSnapshot = snapshot ?: run {
                val result = collector.getCurrentSnapshot()
                if (result is SnapshotResult.Error) {
                    return RecommendationResult.Error("获取性能数据失败: ${result.message}")
                }
                (result as SnapshotResult.Success).snapshot
            }
            
            val issues = analyzePerformanceIssues(currentSnapshot)
            val recommendations = strategyEngine.generateRecommendations(issues)
            
            RecommendationResult.Success(recommendations)
        } catch (e: Exception) {
            RecommendationResult.Error("生成优化建议失败: ${e.message}")
        }
    }
    
    /**
     * 预测性能趋势
     */
    suspend fun predictPerformanceTrend(
        duration: Long = 24 * 60 * 60 * 1000L // 24小时
    ): PredictionResult {
        return try {
            val endTime = com.unify.core.platform.getCurrentTimeMillis()
            val startTime = endTime - duration
            
            val historicalData = collector.getHistoricalData(startTime, endTime)
            if (historicalData is HistoricalDataResult.Error) {
                return PredictionResult.Error("获取历史数据失败: ${historicalData.message}")
            }
            
            val data = (historicalData as HistoricalDataResult.Success).data
            val prediction = learningEngine.predictTrend(data)
            
            PredictionResult.Success(prediction)
        } catch (e: Exception) {
            PredictionResult.Error("性能趋势预测失败: ${e.message}")
        }
    }
    
    /**
     * 获取优化历史
     */
    fun getOptimizationHistory(
        limit: Int = 50
    ): List<OptimizationRecord> {
        return optimizationHistory.takeLast(limit).reversed()
    }
    
    /**
     * 生成优化报告
     */
    suspend fun generateOptimizationReport(
        startTime: Long,
        endTime: Long
    ): ReportResult {
        return try {
            val relevantRecords = optimizationHistory.filter { 
                it.timestamp in startTime..endTime 
            }
            
            val report = OptimizationReport(
                reportId = "opt_report_${com.unify.core.platform.getCurrentTimeMillis()}",
                generatedAt = com.unify.core.platform.getCurrentTimeMillis(),
                period = ReportPeriod(startTime, endTime),
                totalOptimizations = relevantRecords.size,
                successfulOptimizations = relevantRecords.count { record ->
                    record.results.any { it.success }
                },
                averageImprovement = relevantRecords.flatMap { it.results }
                    .filter { it.success }
                    .map { it.improvementPercent }
                    .average(),
                topStrategies = getTopStrategies(relevantRecords),
                recommendations = generateFutureRecommendations(relevantRecords)
            )
            
            ReportResult.Success(report)
        } catch (e: Exception) {
            ReportResult.Error("生成优化报告失败: ${e.message}")
        }
    }
    
    /**
     * 分析性能问题
     */
    private fun analyzePerformanceIssues(
        snapshot: PerformanceSnapshot
    ): List<PerformanceIssue> {
        val issues = mutableListOf<PerformanceIssue>()
        
        // CPU问题分析
        if (snapshot.cpuMetrics.usage > config.cpuThreshold) {
            issues.add(
                PerformanceIssue(
                    type = IssueType.HIGH_CPU_USAGE,
                    severity = when {
                        snapshot.cpuMetrics.usage > 90 -> IssueSeverity.CRITICAL
                        snapshot.cpuMetrics.usage > 80 -> IssueSeverity.HIGH
                        else -> IssueSeverity.MEDIUM
                    },
                    description = com.unify.core.utils.UnifyStringUtils.format("CPU使用率过高: %.1f%%", snapshot.cpuMetrics.usage),
                    currentValue = snapshot.cpuMetrics.usage,
                    threshold = config.cpuThreshold,
                    affectedMetric = "cpu_usage"
                )
            )
        }
        
        // 内存问题分析
        val memoryUsage = if (snapshot.memoryMetrics.total > 0) {
            (snapshot.memoryMetrics.used.toDouble() / snapshot.memoryMetrics.total) * 100
        } else 0.0
        
        if (memoryUsage > config.memoryThreshold) {
            issues.add(
                PerformanceIssue(
                    type = IssueType.HIGH_MEMORY_USAGE,
                    severity = when {
                        memoryUsage > 95 -> IssueSeverity.CRITICAL
                        memoryUsage > 85 -> IssueSeverity.HIGH
                        else -> IssueSeverity.MEDIUM
                    },
                    description = com.unify.core.utils.UnifyStringUtils.format("内存使用率过高: %.1f%%", memoryUsage),
                    currentValue = memoryUsage,
                    threshold = config.memoryThreshold,
                    affectedMetric = "memory_usage"
                )
            )
        }
        
        // 磁盘问题分析
        if (snapshot.diskMetrics.usage > config.diskThreshold) {
            issues.add(
                PerformanceIssue(
                    type = IssueType.HIGH_DISK_USAGE,
                    severity = when {
                        snapshot.diskMetrics.usage > 95 -> IssueSeverity.CRITICAL
                        snapshot.diskMetrics.usage > 90 -> IssueSeverity.HIGH
                        else -> IssueSeverity.MEDIUM
                    },
                    description = com.unify.core.utils.UnifyStringUtils.format("磁盘使用率过高: %.1f%%", snapshot.diskMetrics.usage),
                    currentValue = snapshot.diskMetrics.usage,
                    threshold = config.diskThreshold,
                    affectedMetric = "disk_usage"
                )
            )
        }
        
        // 网络问题分析
        if (snapshot.networkMetrics.errors > config.networkErrorThreshold) {
            issues.add(
                PerformanceIssue(
                    type = IssueType.NETWORK_ERRORS,
                    severity = IssueSeverity.MEDIUM,
                    description = "网络错误过多: ${snapshot.networkMetrics.errors}",
                    currentValue = snapshot.networkMetrics.errors.toDouble(),
                    threshold = config.networkErrorThreshold.toDouble(),
                    affectedMetric = "network_errors"
                )
            )
        }
        
        // 电池问题分析
        snapshot.batteryMetrics?.let { battery ->
            if (battery.level < config.batteryLowThreshold) {
                issues.add(
                    PerformanceIssue(
                        type = IssueType.LOW_BATTERY,
                        severity = when {
                            battery.level < 10 -> IssueSeverity.CRITICAL
                            battery.level < 20 -> IssueSeverity.HIGH
                            else -> IssueSeverity.MEDIUM
                        },
                        description = com.unify.core.utils.UnifyStringUtils.format("电池电量过低: %.1f%%", battery.level),
                        currentValue = battery.level,
                        threshold = config.batteryLowThreshold,
                        affectedMetric = "battery_level"
                    )
                )
            }
        }
        
        return issues
    }
    
    /**
     * 执行优化操作
     */
    private suspend fun executeOptimizations(
        strategies: List<OptimizationStrategy>,
        snapshot: PerformanceSnapshot
    ): List<OptimizationActionResult> {
        val results = mutableListOf<OptimizationActionResult>()
        
        strategies.forEach { strategy ->
            try {
                val result = actionExecutor.executeStrategy(strategy, snapshot)
                results.add(result)
            } catch (e: Exception) {
                results.add(
                    OptimizationActionResult(
                        strategyId = strategy.id,
                        actionType = strategy.actionType,
                        success = false,
                        message = "执行失败: ${e.message}",
                        improvementPercent = 0.0,
                        executionTime = 0L
                    )
                )
            }
        }
        
        return results
    }
    
    /**
     * 记录优化历史
     */
    private fun recordOptimization(
        snapshot: PerformanceSnapshot,
        strategies: List<OptimizationStrategy>,
        results: List<OptimizationActionResult>
    ) {
        val record = OptimizationRecord(
            id = "opt_${com.unify.core.platform.getCurrentTimeMillis()}",
            timestamp = com.unify.core.platform.getCurrentTimeMillis(),
            beforeSnapshot = snapshot,
            appliedStrategies = strategies,
            results = results,
            overallImprovement = results.filter { it.success }.map { it.improvementPercent }.average()
        )
        
        optimizationHistory.add(record)
        
        // 限制历史记录数量
        if (optimizationHistory.size > config.maxHistorySize) {
            optimizationHistory.removeAt(0)
        }
    }
    
    /**
     * 启动优化任务
     */
    private fun startOptimizationTasks() {
        // 定期优化任务
        coroutineScope.launch {
            while (isActive && _optimizerState.value == OptimizerState.RUNNING) {
                try {
                    performOptimization()
                    delay(config.optimizationInterval)
                } catch (e: Exception) {
                    println("自动优化任务失败: ${e.message}")
                    delay(config.optimizationInterval)
                }
            }
        }
        
        // 自适应阈值调整任务
        coroutineScope.launch {
            while (isActive && _optimizerState.value == OptimizerState.RUNNING) {
                try {
                    adaptiveThresholds.adjustThresholds(optimizationHistory)
                    delay(config.thresholdAdjustmentInterval)
                } catch (e: Exception) {
                    println("阈值调整任务失败: ${e.message}")
                    delay(config.thresholdAdjustmentInterval)
                }
            }
        }
    }
    
    /**
     * 计算平均改善百分比
     */
    private fun calculateAverageImprovement(results: List<OptimizationActionResult>): Double {
        val successfulResults = results.filter { it.success }
        return if (successfulResults.isNotEmpty()) {
            successfulResults.map { it.improvementPercent }.average()
        } else 0.0
    }
    
    /**
     * 获取最佳策略
     */
    private fun getTopStrategies(records: List<OptimizationRecord>): List<String> {
        return records.flatMap { it.appliedStrategies }
            .groupBy { it.id }
            .mapValues { (_, strategies) ->
                strategies.size to records.flatMap { it.results }
                    .filter { result -> result.strategyId == strategies.first().id && result.success }
                    .map { it.improvementPercent }
                    .average()
            }
            .toList()
            .sortedByDescending { it.second.second }
            .take(5)
            .map { it.first }
    }
    
    /**
     * 生成未来建议
     */
    private fun generateFutureRecommendations(records: List<OptimizationRecord>): List<String> {
        val recommendations = mutableListOf<String>()
        
        // 基于历史数据生成建议
        val commonIssues = records.flatMap { record ->
            analyzePerformanceIssues(record.beforeSnapshot)
        }.groupBy { it.type }
        
        commonIssues.forEach { (issueType, issues) ->
            if (issues.size > records.size * 0.3) { // 如果问题出现频率超过30%
                when (issueType) {
                    IssueType.HIGH_CPU_USAGE -> recommendations.add("建议优化CPU密集型任务的执行策略")
                    IssueType.HIGH_MEMORY_USAGE -> recommendations.add("建议实施内存管理优化策略")
                    IssueType.HIGH_DISK_USAGE -> recommendations.add("建议清理磁盘空间或优化存储策略")
                    IssueType.NETWORK_ERRORS -> recommendations.add("建议检查网络配置和连接稳定性")
                    IssueType.LOW_BATTERY -> recommendations.add("建议启用节能模式和电池优化")
                    IssueType.THERMAL_THROTTLING -> recommendations.add("建议降低系统负载以减少发热")
                    IssueType.MEMORY_LEAK -> recommendations.add("建议检查并修复内存泄漏问题")
                    IssueType.SLOW_IO -> recommendations.add("建议优化I/O操作以提升性能")
                }
            }
        }
        
        return recommendations
    }
    
    /**
     * 初始化优化器
     */
    private fun initializeOptimizer() {
        coroutineScope.launch {
            try {
                _optimizerState.value = OptimizerState.INITIALIZING
                
                // 初始化各个组件
                strategyEngine.initialize()
                ruleEngine.initialize()
                actionExecutor.initialize()
                learningEngine.initialize()
                adaptiveThresholds.initialize()
                
                _optimizerState.value = OptimizerState.IDLE
            } catch (e: Exception) {
                _optimizerState.value = OptimizerState.ERROR
                println("性能优化器初始化失败: ${e.message}")
            }
        }
    }
    
    /**
     * 更新统计信息
     */
    private fun updateStats(update: (OptimizationStats) -> OptimizationStats) {
        _optimizationStats.value = update(_optimizationStats.value)
    }
    
    /**
     * 清理资源
     */
    fun cleanup() {
        coroutineScope.cancel()
        optimizationHistory.clear()
    }
}

/**
 * 优化器配置
 */
@Serializable
data class OptimizerConfig(
    val optimizationInterval: Long = 5 * 60 * 1000L, // 5分钟
    val thresholdAdjustmentInterval: Long = 30 * 60 * 1000L, // 30分钟
    val maxHistorySize: Int = 1000,
    val cpuThreshold: Double = 80.0,
    val memoryThreshold: Double = 85.0,
    val diskThreshold: Double = 90.0,
    val networkErrorThreshold: Long = 100L,
    val batteryLowThreshold: Double = 20.0,
    val enableAutoOptimization: Boolean = true,
    val enableLearning: Boolean = true
)

/**
 * 优化器状态
 */
enum class OptimizerState {
    INITIALIZING,   // 初始化中
    IDLE,           // 空闲
    STARTING,       // 启动中
    RUNNING,        // 运行中
    OPTIMIZING,     // 优化中
    STOPPING,       // 停止中
    ERROR           // 错误
}
