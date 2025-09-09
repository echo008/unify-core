package com.unify.ui.rendering

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.unify.core.platform.getCurrentTimeMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable

/**
 * Unify渲染优化器
 * 提供跨平台渲染性能优化和智能渲染策略
 */
class UnifyRenderingOptimizer {
    private val _renderingMetrics = MutableStateFlow<RenderingMetrics?>(null)
    val renderingMetrics: Flow<RenderingMetrics?> = _renderingMetrics.asStateFlow()

    private val _optimizationSettings = MutableStateFlow(RenderingOptimizationSettings())
    val optimizationSettings: Flow<RenderingOptimizationSettings> = _optimizationSettings.asStateFlow()

    private val renderingCache = mutableMapOf<String, CachedRenderData>()
    private val drawCallHistory = mutableListOf<DrawCallInfo>()

    /**
     * 启用渲染优化
     */
    fun enableOptimization(settings: RenderingOptimizationSettings = RenderingOptimizationSettings()) {
        _optimizationSettings.value = settings
        println("渲染优化已启用")
    }

    /**
     * 禁用渲染优化
     */
    fun disableOptimization() {
        _optimizationSettings.value = RenderingOptimizationSettings(enabled = false)
        clearCache()
        println("渲染优化已禁用")
    }

    /**
     * 优化绘制调用
     */
    fun optimizeDrawCalls(
        drawScope: DrawScope,
        drawOperations: List<DrawOperation>,
    ): List<DrawOperation> {
        val settings = _optimizationSettings.value
        if (!settings.enabled) return drawOperations

        var optimizedOperations = drawOperations

        // 批处理相同类型的绘制操作
        if (settings.enableBatching) {
            optimizedOperations = batchDrawOperations(optimizedOperations)
        }

        // 剔除不可见的绘制操作
        if (settings.enableCulling) {
            optimizedOperations = cullInvisibleOperations(drawScope, optimizedOperations)
        }

        // 合并重叠的绘制操作
        if (settings.enableMerging) {
            optimizedOperations = mergeOverlappingOperations(optimizedOperations)
        }

        // 记录绘制调用信息
        recordDrawCalls(optimizedOperations)

        return optimizedOperations
    }

    /**
     * 智能缓存渲染结果
     */
    fun cacheRenderResult(
        key: String,
        renderData: CachedRenderData,
    ) {
        val settings = _optimizationSettings.value
        if (!settings.enableCaching) return

        // 限制缓存大小
        if (renderingCache.size >= settings.maxCacheSize) {
            // 移除最旧的缓存项
            val oldestKey = renderingCache.keys.first()
            renderingCache.remove(oldestKey)
        }

        renderingCache[key] = renderData.copy(timestamp = getCurrentTimeMillis())
    }

    /**
     * 获取缓存的渲染结果
     */
    fun getCachedRenderResult(key: String): CachedRenderData? {
        val settings = _optimizationSettings.value
        if (!settings.enableCaching) return null

        val cached = renderingCache[key] ?: return null

        // 检查缓存是否过期
        val currentTime = getCurrentTimeMillis()
        if (currentTime - cached.timestamp > settings.cacheExpirationMs) {
            renderingCache.remove(key)
            return null
        }

        return cached
    }

    /**
     * 分析渲染性能
     */
    fun analyzeRenderingPerformance(): RenderingAnalysisResult {
        val recentDrawCalls = drawCallHistory.takeLast(100)

        val totalDrawCalls = recentDrawCalls.size
        val averageDrawTime =
            if (recentDrawCalls.isNotEmpty()) {
                recentDrawCalls.map { it.duration }.average()
            } else {
                0.0
            }

        val drawCallsByType = recentDrawCalls.groupBy { it.type }
        val bottlenecks = identifyRenderingBottlenecks(drawCallsByType)

        return RenderingAnalysisResult(
            totalDrawCalls = totalDrawCalls,
            averageDrawTime = averageDrawTime,
            drawCallsByType = drawCallsByType.mapValues { it.value.size },
            bottlenecks = bottlenecks,
            cacheHitRate = calculateCacheHitRate(),
            optimizationSavings = calculateOptimizationSavings(),
            timestamp = getCurrentTimeMillis(),
        )
    }

    /**
     * 生成渲染优化建议
     */
    fun generateOptimizationRecommendations(): List<RenderingOptimizationRecommendation> {
        val analysis = analyzeRenderingPerformance()
        val recommendations = mutableListOf<RenderingOptimizationRecommendation>()

        // 检查绘制调用数量
        if (analysis.totalDrawCalls > MAX_DRAW_CALLS_PER_FRAME) {
            recommendations.add(
                RenderingOptimizationRecommendation(
                    type = OptimizationType.REDUCE_DRAW_CALLS,
                    priority = RecommendationPriority.HIGH,
                    title = "减少绘制调用",
                    description = "当前绘制调用数量过多: ${analysis.totalDrawCalls} > $MAX_DRAW_CALLS_PER_FRAME",
                    actionItems =
                        listOf(
                            "启用绘制批处理",
                            "合并相似的绘制操作",
                            "使用纹理图集减少纹理切换",
                        ),
                    estimatedImprovement = 0.3,
                ),
            )
        }

        // 检查缓存命中率
        if (analysis.cacheHitRate < MIN_CACHE_HIT_RATE) {
            recommendations.add(
                RenderingOptimizationRecommendation(
                    type = OptimizationType.IMPROVE_CACHING,
                    priority = RecommendationPriority.MEDIUM,
                    title = "提高缓存效率",
                    description = "缓存命中率较低: ${(analysis.cacheHitRate * 100).toInt()}% < ${(MIN_CACHE_HIT_RATE * 100).toInt()}%",
                    actionItems =
                        listOf(
                            "增加缓存大小",
                            "优化缓存键策略",
                            "延长缓存过期时间",
                        ),
                    estimatedImprovement = 0.2,
                ),
            )
        }

        // 检查平均绘制时间
        if (analysis.averageDrawTime > MAX_AVERAGE_DRAW_TIME_MS) {
            recommendations.add(
                RenderingOptimizationRecommendation(
                    type = OptimizationType.OPTIMIZE_DRAW_TIME,
                    priority = RecommendationPriority.HIGH,
                    title = "优化绘制时间",
                    description = "平均绘制时间过长: ${analysis.averageDrawTime.toInt()}ms > ${MAX_AVERAGE_DRAW_TIME_MS}ms",
                    actionItems =
                        listOf(
                            "简化复杂的绘制操作",
                            "使用硬件加速",
                            "预计算复杂的几何形状",
                        ),
                    estimatedImprovement = 0.4,
                ),
            )
        }

        return recommendations
    }

    /**
     * 应用自动优化
     */
    fun applyAutoOptimization() {
        val recommendations = generateOptimizationRecommendations()
        val currentSettings = _optimizationSettings.value

        var newSettings = currentSettings

        recommendations.forEach { recommendation ->
            when (recommendation.type) {
                OptimizationType.REDUCE_DRAW_CALLS -> {
                    newSettings =
                        newSettings.copy(
                            enableBatching = true,
                            enableMerging = true,
                        )
                }
                OptimizationType.IMPROVE_CACHING -> {
                    newSettings =
                        newSettings.copy(
                            enableCaching = true,
                            maxCacheSize = minOf(currentSettings.maxCacheSize * 2, 1000),
                            cacheExpirationMs = currentSettings.cacheExpirationMs * 2,
                        )
                }
                OptimizationType.OPTIMIZE_DRAW_TIME -> {
                    newSettings =
                        newSettings.copy(
                            enableCulling = true,
                            enableLevelOfDetail = true,
                        )
                }
            }
        }

        _optimizationSettings.value = newSettings
        println("自动优化已应用")
    }

    // 私有辅助方法
    private fun batchDrawOperations(operations: List<DrawOperation>): List<DrawOperation> {
        val batched = mutableListOf<DrawOperation>()
        val batchableOperations = operations.groupBy { it.type }

        batchableOperations.forEach { (type, ops) ->
            if (ops.size > 1 && canBatch(type)) {
                // 创建批处理操作
                batched.add(createBatchedOperation(ops))
            } else {
                batched.addAll(ops)
            }
        }

        return batched
    }

    private fun cullInvisibleOperations(
        drawScope: DrawScope,
        operations: List<DrawOperation>,
    ): List<DrawOperation> {
        val visibleArea = drawScope.size
        return operations.filter { operation ->
            // 检查操作是否在可见区域内
            isOperationVisible(operation, visibleArea)
        }
    }

    private fun mergeOverlappingOperations(operations: List<DrawOperation>): List<DrawOperation> {
        val merged = mutableListOf<DrawOperation>()
        val processed = mutableSetOf<Int>()

        operations.forEachIndexed { index, operation ->
            if (index in processed) return@forEachIndexed

            val overlapping = findOverlappingOperations(operation, operations, index)
            if (overlapping.isNotEmpty()) {
                // 合并重叠操作
                merged.add(mergeOperations(listOf(operation) + overlapping))
                processed.addAll(overlapping.map { operations.indexOf(it) })
            } else {
                merged.add(operation)
            }
            processed.add(index)
        }

        return merged
    }

    private fun recordDrawCalls(operations: List<DrawOperation>) {
        val timestamp = getCurrentTimeMillis()
        operations.forEach { operation ->
            drawCallHistory.add(
                DrawCallInfo(
                    type = operation.type,
                    duration = operation.estimatedDuration,
                    timestamp = timestamp,
                ),
            )
        }

        // 限制历史记录大小
        if (drawCallHistory.size > MAX_DRAW_CALL_HISTORY) {
            drawCallHistory.removeAt(0)
        }
    }

    private fun identifyRenderingBottlenecks(drawCallsByType: Map<DrawOperationType, List<DrawCallInfo>>): List<RenderingBottleneck> {
        val bottlenecks = mutableListOf<RenderingBottleneck>()

        drawCallsByType.forEach { (type, calls) ->
            val totalTime = calls.sumOf { it.duration }
            val averageTime = calls.map { it.duration }.average()

            if (calls.size > MAX_DRAW_CALLS_PER_TYPE) {
                bottlenecks.add(
                    RenderingBottleneck(
                        type = type,
                        issue = "绘制调用数量过多",
                        impact = BottleneckImpact.HIGH,
                        suggestion = "考虑批处理或合并操作",
                    ),
                )
            }

            if (averageTime > MAX_AVERAGE_DRAW_TIME_MS) {
                bottlenecks.add(
                    RenderingBottleneck(
                        type = type,
                        issue = "单次绘制时间过长",
                        impact = BottleneckImpact.MEDIUM,
                        suggestion = "优化绘制算法或使用LOD",
                    ),
                )
            }
        }

        return bottlenecks
    }

    private fun calculateCacheHitRate(): Double {
        // 模拟缓存命中率计算
        return 0.75 // 75%
    }

    private fun calculateOptimizationSavings(): Double {
        // 模拟优化节省计算
        return 0.25 // 25%性能提升
    }

    private fun canBatch(type: DrawOperationType): Boolean {
        return when (type) {
            DrawOperationType.RECTANGLE, DrawOperationType.CIRCLE, DrawOperationType.LINE -> true
            else -> false
        }
    }

    private fun createBatchedOperation(operations: List<DrawOperation>): DrawOperation {
        return DrawOperation(
            type = operations.first().type,
            estimatedDuration = operations.sumOf { it.estimatedDuration } * 0.7, // 批处理节省30%时间
        )
    }

    private fun isOperationVisible(
        operation: DrawOperation,
        visibleArea: androidx.compose.ui.geometry.Size,
    ): Boolean {
        // 简化的可见性检查
        return true // 实际实现会检查操作边界与可见区域的交集
    }

    private fun findOverlappingOperations(
        operation: DrawOperation,
        allOperations: List<DrawOperation>,
        currentIndex: Int,
    ): List<DrawOperation> {
        // 简化的重叠检查
        return emptyList() // 实际实现会检查几何重叠
    }

    private fun mergeOperations(operations: List<DrawOperation>): DrawOperation {
        return DrawOperation(
            type = operations.first().type,
            estimatedDuration = operations.sumOf { it.estimatedDuration } * 0.8, // 合并节省20%时间
        )
    }

    private fun clearCache() {
        renderingCache.clear()
        drawCallHistory.clear()
    }

    companion object {
        private const val MAX_DRAW_CALLS_PER_FRAME = 100
        private const val MAX_DRAW_CALLS_PER_TYPE = 20
        private const val MAX_AVERAGE_DRAW_TIME_MS = 2.0
        private const val MIN_CACHE_HIT_RATE = 0.6
        private const val MAX_DRAW_CALL_HISTORY = 1000
    }
}

// 数据类定义
@Serializable
data class RenderingOptimizationSettings(
    val enabled: Boolean = true,
    val enableBatching: Boolean = true,
    val enableCulling: Boolean = true,
    val enableMerging: Boolean = true,
    val enableCaching: Boolean = true,
    val enableLevelOfDetail: Boolean = false,
    val maxCacheSize: Int = 100,
    val cacheExpirationMs: Long = 300000L, // 5分钟
)

@Serializable
data class CachedRenderData(
    val data: ByteArray,
    val width: Int,
    val height: Int,
    val timestamp: Long,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as CachedRenderData
        return data.contentEquals(other.data) && width == other.width && height == other.height
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + width
        result = 31 * result + height
        return result
    }
}

@Serializable
data class DrawOperation(
    val type: DrawOperationType,
    val estimatedDuration: Double,
)

@Serializable
data class DrawCallInfo(
    val type: DrawOperationType,
    val duration: Double,
    val timestamp: Long,
)

@Serializable
data class RenderingMetrics(
    val frameRate: Double,
    val drawCallCount: Int,
    val averageDrawTime: Double,
    val cacheHitRate: Double,
    val memoryUsage: Long,
    val timestamp: Long,
)

@Serializable
data class RenderingAnalysisResult(
    val totalDrawCalls: Int,
    val averageDrawTime: Double,
    val drawCallsByType: Map<DrawOperationType, Int>,
    val bottlenecks: List<RenderingBottleneck>,
    val cacheHitRate: Double,
    val optimizationSavings: Double,
    val timestamp: Long,
)

@Serializable
data class RenderingBottleneck(
    val type: DrawOperationType,
    val issue: String,
    val impact: BottleneckImpact,
    val suggestion: String,
)

@Serializable
data class RenderingOptimizationRecommendation(
    val type: OptimizationType,
    val priority: RecommendationPriority,
    val title: String,
    val description: String,
    val actionItems: List<String>,
    val estimatedImprovement: Double,
)

enum class DrawOperationType {
    RECTANGLE,
    CIRCLE,
    LINE,
    PATH,
    TEXT,
    IMAGE,
    GRADIENT,
}

enum class BottleneckImpact {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL,
}

enum class OptimizationType {
    REDUCE_DRAW_CALLS,
    IMPROVE_CACHING,
    OPTIMIZE_DRAW_TIME,
}

enum class RecommendationPriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT,
}
