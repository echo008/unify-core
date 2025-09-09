package com.unify.ui.memory

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * 跨平台统一UI内存管理器
 */
object UnifyUIMemoryManager {
    private val _memoryState = MutableStateFlow(MemoryState())
    val memoryState: StateFlow<MemoryState> = _memoryState.asStateFlow()

    private val componentCache = mutableMapOf<String, Any>()
    private val imageCache = mutableMapOf<String, Any>()
    private val dataCache = mutableMapOf<String, Any>()

    private var monitoringJob: Job? = null
    private var isMonitoring = false

    /**
     * 内存状态数据类
     */
    data class MemoryState(
        val totalMemory: Long = 0L,
        val usedMemory: Long = 0L,
        val availableMemory: Long = 0L,
        val cacheSize: Long = 0L,
        val gcCount: Int = 0,
        val memoryPressure: MemoryPressure = MemoryPressure.LOW,
    )

    enum class MemoryPressure {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL,
    }

    /**
     * 启动内存监控
     */
    fun startMonitoring(scope: CoroutineScope) {
        if (isMonitoring) return

        isMonitoring = true
        monitoringJob =
            scope.launch {
                while (isMonitoring) {
                    updateMemoryState()
                    delay(2000) // 每2秒更新一次
                }
            }
    }

    /**
     * 停止内存监控
     */
    fun stopMonitoring() {
        isMonitoring = false
        monitoringJob?.cancel()
        monitoringJob = null
    }

    /**
     * 更新内存状态
     */
    private suspend fun updateMemoryState() {
        val memoryInfo = getPlatformMemoryInfo()
        val cacheSize = calculateCacheSize()

        val pressure = calculateMemoryPressure(memoryInfo.usedMemory, memoryInfo.totalMemory)

        _memoryState.value =
            MemoryState(
                totalMemory = memoryInfo.totalMemory,
                usedMemory = memoryInfo.usedMemory,
                availableMemory = memoryInfo.availableMemory,
                cacheSize = cacheSize,
                gcCount = memoryInfo.gcCount,
                memoryPressure = pressure,
            )

        // 根据内存压力自动清理
        when (pressure) {
            MemoryPressure.HIGH -> performLightCleanup()
            MemoryPressure.CRITICAL -> performAggressiveCleanup()
            else -> {}
        }
    }

    /**
     * 计算内存压力
     */
    private fun calculateMemoryPressure(
        used: Long,
        total: Long,
    ): MemoryPressure {
        val usage = used.toDouble() / total.toDouble()
        return when {
            usage > 0.9 -> MemoryPressure.CRITICAL
            usage > 0.75 -> MemoryPressure.HIGH
            usage > 0.6 -> MemoryPressure.MEDIUM
            else -> MemoryPressure.LOW
        }
    }

    /**
     * 计算缓存大小
     */
    private fun calculateCacheSize(): Long {
        return (componentCache.size + imageCache.size + dataCache.size) * 1024L // 估算
    }

    /**
     * 轻度清理
     */
    private fun performLightCleanup() {
        // 清理最近最少使用的缓存项
        if (imageCache.size > 50) {
            val keysToRemove = imageCache.keys.take(imageCache.size / 4)
            keysToRemove.forEach { imageCache.remove(it) }
        }

        if (dataCache.size > 100) {
            val keysToRemove = dataCache.keys.take(dataCache.size / 4)
            keysToRemove.forEach { dataCache.remove(it) }
        }

        // 触发垃圾回收
        requestGarbageCollection()
    }

    /**
     * 激进清理
     */
    private fun performAggressiveCleanup() {
        // 清理大部分缓存
        imageCache.clear()
        dataCache.clear()

        // 保留最重要的组件缓存
        if (componentCache.size > 20) {
            val keysToRemove = componentCache.keys.take(componentCache.size / 2)
            keysToRemove.forEach { componentCache.remove(it) }
        }

        // 强制垃圾回收
        requestGarbageCollection()
    }

    /**
     * 手动清理缓存
     */
    fun clearCache(type: CacheType = CacheType.ALL) {
        when (type) {
            CacheType.COMPONENT -> componentCache.clear()
            CacheType.IMAGE -> imageCache.clear()
            CacheType.DATA -> dataCache.clear()
            CacheType.ALL -> {
                componentCache.clear()
                imageCache.clear()
                dataCache.clear()
            }
        }
        requestGarbageCollection()
    }

    enum class CacheType {
        COMPONENT,
        IMAGE,
        DATA,
        ALL,
    }

    /**
     * 缓存组件
     */
    fun cacheComponent(
        key: String,
        component: Any,
    ) {
        if (componentCache.size < 100) { // 限制缓存大小
            componentCache[key] = component
        }
    }

    /**
     * 获取缓存的组件
     */
    fun getCachedComponent(key: String): Any? {
        return componentCache[key]
    }

    /**
     * 缓存图片
     */
    fun cacheImage(
        key: String,
        image: Any,
    ) {
        if (imageCache.size < 200) { // 限制缓存大小
            imageCache[key] = image
        }
    }

    /**
     * 获取缓存的图片
     */
    fun getCachedImage(key: String): Any? {
        return imageCache[key]
    }

    /**
     * 缓存数据
     */
    fun cacheData(
        key: String,
        data: Any,
    ) {
        if (dataCache.size < 500) { // 限制缓存大小
            dataCache[key] = data
        }
    }

    /**
     * 获取缓存的数据
     */
    fun getCachedData(key: String): Any? {
        return dataCache[key]
    }

    /**
     * 获取缓存统计信息
     */
    fun getCacheStats(): CacheStats {
        return CacheStats(
            componentCacheSize = componentCache.size,
            imageCacheSize = imageCache.size,
            dataCacheSize = dataCache.size,
            totalCacheSize = componentCache.size + imageCache.size + dataCache.size,
        )
    }

    data class CacheStats(
        val componentCacheSize: Int,
        val imageCacheSize: Int,
        val dataCacheSize: Int,
        val totalCacheSize: Int,
    )

    /**
     * 内存优化建议
     */
    fun getMemoryOptimizationSuggestions(): List<OptimizationSuggestion> {
        val suggestions = mutableListOf<OptimizationSuggestion>()
        val state = _memoryState.value

        when (state.memoryPressure) {
            MemoryPressure.HIGH, MemoryPressure.CRITICAL -> {
                suggestions.add(
                    OptimizationSuggestion(
                        type = SuggestionType.CLEAR_CACHE,
                        message = "内存使用率过高，建议清理缓存",
                        priority = Priority.HIGH,
                    ),
                )
            }
            else -> {}
        }

        if (imageCache.size > 150) {
            suggestions.add(
                OptimizationSuggestion(
                    type = SuggestionType.OPTIMIZE_IMAGES,
                    message = "图片缓存过多，建议优化图片加载策略",
                    priority = Priority.MEDIUM,
                ),
            )
        }

        if (state.cacheSize > 10 * 1024 * 1024) { // 10MB
            suggestions.add(
                OptimizationSuggestion(
                    type = SuggestionType.REDUCE_CACHE_SIZE,
                    message = "缓存占用过多内存，建议减少缓存大小",
                    priority = Priority.MEDIUM,
                ),
            )
        }

        return suggestions
    }

    data class OptimizationSuggestion(
        val type: SuggestionType,
        val message: String,
        val priority: Priority,
    )

    enum class SuggestionType {
        CLEAR_CACHE,
        OPTIMIZE_IMAGES,
        REDUCE_CACHE_SIZE,
        GARBAGE_COLLECT,
    }

    enum class Priority {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL,
    }
}

/**
 * 平台特定内存信息
 */
data class PlatformMemoryInfo(
    val totalMemory: Long,
    val usedMemory: Long,
    val availableMemory: Long,
    val gcCount: Int = 0,
)

/**
 * 获取平台特定的内存信息
 */
expect fun getPlatformMemoryInfo(): PlatformMemoryInfo

/**
 * 请求垃圾回收
 */
expect fun requestGarbageCollection()

/**
 * 内存监控Composable
 */
@Composable
fun rememberMemoryManager(): UnifyUIMemoryManager {
    val scope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        UnifyUIMemoryManager.startMonitoring(scope)
        onDispose {
            UnifyUIMemoryManager.stopMonitoring()
        }
    }

    return UnifyUIMemoryManager
}

/**
 * 内存状态Composable
 */
@Composable
fun collectMemoryState(): State<UnifyUIMemoryManager.MemoryState> {
    return UnifyUIMemoryManager.memoryState.collectAsState()
}
