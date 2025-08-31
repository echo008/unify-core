package com.unify.core.dynamic

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*

/**
 * 回滚管理器
 * 负责创建回滚点、管理版本历史和执行回滚操作
 */
class RollbackManager {
    private var isInitialized = false
    private val storageManager = DynamicStorageManager()
    private val maxRollbackPoints = 5 // 最多保留5个回滚点
    
    private val _rollbackPoints = MutableStateFlow<List<RollbackPoint>>(emptyList())
    val rollbackPoints: StateFlow<List<RollbackPoint>> = _rollbackPoints.asStateFlow()
    
    private val _currentVersion = MutableStateFlow<String?>(null)
    val currentVersion: StateFlow<String?> = _currentVersion.asStateFlow()
    
    /**
     * 初始化回滚管理器
     */
    suspend fun initialize() {
        try {
            // 加载现有回滚点
            loadRollbackPoints()
            
            // 加载当前版本信息
            _currentVersion.value = storageManager.getCurrentVersion()
            
            isInitialized = true
            
            UnifyPerformanceMonitor.recordMetric("rollback_manager_init", 1.0, "count")
        } catch (e: Exception) {
            throw RollbackException("回滚管理器初始化失败: ${e.message}", e)
        }
    }
    
    /**
     * 创建回滚点
     */
    suspend fun createRollbackPoint(): RollbackPoint {
        if (!isInitialized) {
            throw IllegalStateException("回滚管理器未初始化")
        }
        
        return try {
            val startTime = System.currentTimeMillis()
            
            val rollbackPoint = RollbackPoint(
                id = generateRollbackId(),
                version = _currentVersion.value ?: "unknown",
                timestamp = System.currentTimeMillis(),
                description = "自动创建的回滚点",
                componentSnapshot = captureComponentSnapshot(),
                configSnapshot = captureConfigSnapshot(),
                resourceSnapshot = captureResourceSnapshot()
            )
            
            // 保存回滚点
            storageManager.saveRollbackPoint(rollbackPoint)
            
            // 更新回滚点列表
            addRollbackPoint(rollbackPoint)
            
            // 清理旧的回滚点
            cleanupOldRollbackPoints()
            
            val creationTime = System.currentTimeMillis() - startTime
            UnifyPerformanceMonitor.recordMetric("rollback_point_creation_time", 
                creationTime.toDouble(), "ms")
            
            rollbackPoint
        } catch (e: Exception) {
            UnifyPerformanceMonitor.recordMetric("rollback_point_creation_error", 1.0, "count",
                mapOf("error" to e.message.orEmpty()))
            throw RollbackException("创建回滚点失败: ${e.message}", e)
        }
    }
    
    /**
     * 执行回滚
     */
    suspend fun rollback(rollbackPointId: String? = null): Boolean {
        if (!isInitialized) {
            throw IllegalStateException("回滚管理器未初始化")
        }
        
        return try {
            val startTime = System.currentTimeMillis()
            
            val rollbackPoint = if (rollbackPointId != null) {
                findRollbackPoint(rollbackPointId)
            } else {
                getLatestRollbackPoint()
            }
            
            if (rollbackPoint == null) {
                UnifyPerformanceMonitor.recordMetric("rollback_no_point_found", 1.0, "count")
                return false
            }
            
            // 执行回滚操作
            val success = executeRollback(rollbackPoint)
            
            if (success) {
                // 更新当前版本
                _currentVersion.value = rollbackPoint.version
                storageManager.setCurrentVersion(rollbackPoint.version)
                
                val rollbackTime = System.currentTimeMillis() - startTime
                UnifyPerformanceMonitor.recordMetric("rollback_execution_time", 
                    rollbackTime.toDouble(), "ms")
                UnifyPerformanceMonitor.recordMetric("rollback_success", 1.0, "count",
                    mapOf("version" to rollbackPoint.version))
            } else {
                UnifyPerformanceMonitor.recordMetric("rollback_failed", 1.0, "count",
                    mapOf("version" to rollbackPoint.version))
            }
            
            success
        } catch (e: Exception) {
            UnifyPerformanceMonitor.recordMetric("rollback_error", 1.0, "count",
                mapOf("error" to e.message.orEmpty()))
            false
        }
    }
    
    /**
     * 清除回滚点
     */
    suspend fun clearRollbackPoint() {
        val latestPoint = getLatestRollbackPoint()
        if (latestPoint != null) {
            removeRollbackPoint(latestPoint.id)
            storageManager.deleteRollbackPoint(latestPoint.id)
            
            UnifyPerformanceMonitor.recordMetric("rollback_point_cleared", 1.0, "count")
        }
    }
    
    /**
     * 获取回滚历史
     */
    fun getRollbackHistory(): List<RollbackPoint> {
        return _rollbackPoints.value.sortedByDescending { it.timestamp }
    }
    
    /**
     * 验证回滚点完整性
     */
    suspend fun validateRollbackPoint(rollbackPointId: String): Boolean {
        return try {
            val rollbackPoint = findRollbackPoint(rollbackPointId)
            if (rollbackPoint == null) {
                return false
            }
            
            // 验证快照完整性
            val componentValid = validateComponentSnapshot(rollbackPoint.componentSnapshot)
            val configValid = validateConfigSnapshot(rollbackPoint.configSnapshot)
            val resourceValid = validateResourceSnapshot(rollbackPoint.resourceSnapshot)
            
            componentValid && configValid && resourceValid
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 加载回滚点
     */
    private suspend fun loadRollbackPoints() {
        val points = storageManager.getAllRollbackPoints()
        _rollbackPoints.value = points.sortedByDescending { it.timestamp }
    }
    
    /**
     * 捕获组件快照
     */
    private suspend fun captureComponentSnapshot(): Map<String, ComponentSnapshot> {
        val snapshots = mutableMapOf<String, ComponentSnapshot>()
        
        // 获取当前所有动态组件
        val components = UnifyDynamicEngine.dynamicComponents.value
        
        components.forEach { (id, component) ->
            snapshots[id] = ComponentSnapshot(
                id = id,
                metadata = component.metadata,
                code = storageManager.getComponentCode(id) ?: "",
                resources = storageManager.getComponentResources(id) ?: emptyMap()
            )
        }
        
        return snapshots
    }
    
    /**
     * 捕获配置快照
     */
    private suspend fun captureConfigSnapshot(): Map<String, String> {
        return storageManager.getAllConfigs()
    }
    
    /**
     * 捕获资源快照
     */
    private suspend fun captureResourceSnapshot(): Map<String, ByteArray> {
        return storageManager.getAllResources()
    }
    
    /**
     * 执行回滚操作
     */
    private suspend fun executeRollback(rollbackPoint: RollbackPoint): Boolean {
        return try {
            // 1. 回滚组件
            val componentRollbackSuccess = rollbackComponents(rollbackPoint.componentSnapshot)
            if (!componentRollbackSuccess) {
                return false
            }
            
            // 2. 回滚配置
            val configRollbackSuccess = rollbackConfigurations(rollbackPoint.configSnapshot)
            if (!configRollbackSuccess) {
                return false
            }
            
            // 3. 回滚资源
            val resourceRollbackSuccess = rollbackResources(rollbackPoint.resourceSnapshot)
            if (!resourceRollbackSuccess) {
                return false
            }
            
            // 4. 重新加载动态引擎
            reloadDynamicEngine()
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 回滚组件
     */
    private suspend fun rollbackComponents(componentSnapshots: Map<String, ComponentSnapshot>): Boolean {
        return try {
            // 清除当前组件
            storageManager.clearAllComponents()
            
            // 恢复快照中的组件
            componentSnapshots.forEach { (id, snapshot) ->
                storageManager.saveComponentCode(id, snapshot.code)
                storageManager.saveComponentResources(id, snapshot.resources)
                storageManager.saveComponentMetadata(id, snapshot.metadata)
            }
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 回滚配置
     */
    private suspend fun rollbackConfigurations(configSnapshots: Map<String, String>): Boolean {
        return try {
            // 清除当前配置
            storageManager.clearAllConfigs()
            
            // 恢复快照中的配置
            configSnapshots.forEach { (key, value) ->
                storageManager.saveConfig(key, value)
            }
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 回滚资源
     */
    private suspend fun rollbackResources(resourceSnapshots: Map<String, ByteArray>): Boolean {
        return try {
            // 清除当前资源
            storageManager.clearAllResources()
            
            // 恢复快照中的资源
            resourceSnapshots.forEach { (path, data) ->
                storageManager.saveResource(path, data)
            }
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 重新加载动态引擎
     */
    private suspend fun reloadDynamicEngine() {
        // 通知动态引擎重新加载组件
        // 这里需要与UnifyDynamicEngine协调
    }
    
    /**
     * 验证组件快照
     */
    private fun validateComponentSnapshot(snapshots: Map<String, ComponentSnapshot>): Boolean {
        return snapshots.all { (_, snapshot) ->
            snapshot.code.isNotEmpty() && snapshot.metadata.name.isNotEmpty()
        }
    }
    
    /**
     * 验证配置快照
     */
    private fun validateConfigSnapshot(snapshots: Map<String, String>): Boolean {
        return snapshots.all { (key, value) ->
            key.isNotEmpty() && value.isNotEmpty()
        }
    }
    
    /**
     * 验证资源快照
     */
    private fun validateResourceSnapshot(snapshots: Map<String, ByteArray>): Boolean {
        return snapshots.all { (path, data) ->
            path.isNotEmpty() && data.isNotEmpty()
        }
    }
    
    /**
     * 添加回滚点
     */
    private fun addRollbackPoint(rollbackPoint: RollbackPoint) {
        val currentPoints = _rollbackPoints.value.toMutableList()
        currentPoints.add(0, rollbackPoint) // 添加到列表开头
        _rollbackPoints.value = currentPoints
    }
    
    /**
     * 移除回滚点
     */
    private fun removeRollbackPoint(rollbackPointId: String) {
        val currentPoints = _rollbackPoints.value.toMutableList()
        currentPoints.removeAll { it.id == rollbackPointId }
        _rollbackPoints.value = currentPoints
    }
    
    /**
     * 查找回滚点
     */
    private fun findRollbackPoint(rollbackPointId: String): RollbackPoint? {
        return _rollbackPoints.value.find { it.id == rollbackPointId }
    }
    
    /**
     * 获取最新回滚点
     */
    private fun getLatestRollbackPoint(): RollbackPoint? {
        return _rollbackPoints.value.maxByOrNull { it.timestamp }
    }
    
    /**
     * 清理旧的回滚点
     */
    private suspend fun cleanupOldRollbackPoints() {
        val currentPoints = _rollbackPoints.value
        if (currentPoints.size > maxRollbackPoints) {
            val pointsToRemove = currentPoints.drop(maxRollbackPoints)
            
            pointsToRemove.forEach { point ->
                storageManager.deleteRollbackPoint(point.id)
            }
            
            _rollbackPoints.value = currentPoints.take(maxRollbackPoints)
            
            UnifyPerformanceMonitor.recordMetric("rollback_points_cleaned", 
                pointsToRemove.size.toDouble(), "count")
        }
    }
    
    /**
     * 生成回滚点ID
     */
    private fun generateRollbackId(): String {
        return "rollback_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
}

/**
 * 回滚点
 */
@Serializable
data class RollbackPoint(
    val id: String,
    val version: String,
    val timestamp: Long,
    val description: String,
    val componentSnapshot: Map<String, ComponentSnapshot>,
    val configSnapshot: Map<String, String>,
    val resourceSnapshot: Map<String, ByteArray>
)

/**
 * 组件快照
 */
@Serializable
data class ComponentSnapshot(
    val id: String,
    val metadata: ComponentMetadata,
    val code: String,
    val resources: Map<String, String>
)

/**
 * 回滚异常
 */
class RollbackException(message: String, cause: Throwable? = null) : Exception(message, cause)
