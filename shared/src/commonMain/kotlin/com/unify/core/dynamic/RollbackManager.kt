package com.unify.core.dynamic

import kotlinx.coroutines.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableListOf

/**
 * 回滚点数据结构
 */
@Serializable
data class RollbackPoint(
    val id: String,
    val componentId: String,
    val version: String,
    val timestamp: Long,
    val description: String,
    val componentData: String,
    val configData: Map<String, String> = emptyMap(),
    val dependencies: List<String> = emptyList(),
    val metadata: Map<String, String> = emptyMap(),
    val checksum: String = ""
)

@Serializable
data class RollbackOperation(
    val id: String,
    val componentId: String,
    val fromVersion: String,
    val toVersion: String,
    val rollbackPointId: String,
    val timestamp: Long,
    val status: RollbackStatus,
    val reason: String = "",
    val errorMessage: String? = null
)

@Serializable
enum class RollbackStatus {
    PENDING,     // 待执行
    IN_PROGRESS, // 执行中
    COMPLETED,   // 已完成
    FAILED,      // 失败
    CANCELLED    // 已取消
}

@Serializable
data class RollbackPolicy(
    val maxRollbackPoints: Int = 10,
    val retentionDays: Int = 30,
    val autoCleanup: Boolean = true,
    val requireConfirmation: Boolean = true,
    val enableBatchRollback: Boolean = true,
    val maxRollbackDepth: Int = 5
)

/**
 * 回滚管理器接口
 */
interface RollbackManager {
    // 备份点管理
    suspend fun createBackup(componentId: String, description: String = ""): String?
    suspend fun deleteBackup(rollbackPointId: String): Boolean
    suspend fun getBackupPoints(componentId: String): List<RollbackPoint>
    suspend fun getAllBackupPoints(): List<RollbackPoint>
    
    // 回滚操作
    suspend fun rollback(componentId: String, rollbackPointId: String? = null): Boolean
    suspend fun rollbackToVersion(componentId: String, version: String): Boolean
    suspend fun rollbackBatch(componentIds: List<String>): Map<String, Boolean>
    
    // 回滚历史
    suspend fun getRollbackHistory(componentId: String): List<RollbackOperation>
    suspend fun getAllRollbackHistory(): List<RollbackOperation>
    suspend fun cancelRollback(operationId: String): Boolean
    
    // 策略管理
    fun updateRollbackPolicy(policy: RollbackPolicy)
    fun getRollbackPolicy(): RollbackPolicy
    
    // 清理和维护
    suspend fun cleanupExpiredBackups(): Int
    suspend fun validateBackupIntegrity(): Map<String, Boolean>
    suspend fun exportBackups(): String
    suspend fun importBackups(data: String): Boolean
    
    // 生命周期
    suspend fun initialize()
    suspend fun shutdown()
}

/**
 * 回滚管理器实现
 */
class RollbackManagerImpl(
    private val storageManager: DynamicStorageManager,
    private val dynamicEngine: UnifyDynamicEngine
) : RollbackManager {
    
    private val rollbackPoints = mutableMapOf<String, MutableList<RollbackPoint>>()
    private val rollbackOperations = mutableListOf<RollbackOperation>()
    private var rollbackPolicy = RollbackPolicy()
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    companion object {
        private const val ROLLBACK_POINTS_KEY = "rollback_points"
        private const val ROLLBACK_OPERATIONS_KEY = "rollback_operations"
        private const val ROLLBACK_POLICY_KEY = "rollback_policy"
        private const val BACKUP_PREFIX = "backup_"
    }
    
    override suspend fun initialize() {
        loadRollbackData()
        
        // 启动自动清理任务
        if (rollbackPolicy.autoCleanup) {
            startAutoCleanupTask()
        }
    }
    
    override suspend fun shutdown() {
        saveRollbackData()
        scope.cancel()
    }
    
    override suspend fun createBackup(componentId: String, description: String): String? {
        return try {
            // 获取当前组件信息
            val componentInfo = dynamicEngine.getComponent(componentId)
            if (componentInfo == null) {
                return null
            }
            
            val component = componentInfo.component
            val rollbackPointId = generateRollbackPointId(componentId)
            
            // 创建回滚点
            val rollbackPoint = RollbackPoint(
                id = rollbackPointId,
                componentId = componentId,
                version = component.version,
                timestamp = System.currentTimeMillis(),
                description = description.ifEmpty { "自动备份 - ${component.version}" },
                componentData = Json.encodeToString(component),
                configData = component.config,
                dependencies = component.dependencies,
                metadata = component.metadata,
                checksum = calculateChecksum(component)
            )
            
            // 保存备份数据
            val backupKey = "$BACKUP_PREFIX$rollbackPointId"
            val backupData = Json.encodeToString(rollbackPoint)
            
            if (storageManager.saveConfig(backupKey, backupData)) {
                // 添加到内存缓存
                val componentBackups = rollbackPoints.getOrPut(componentId) { mutableListOf() }
                componentBackups.add(rollbackPoint)
                
                // 管理备份点数量
                manageBackupPoints(componentId)
                
                // 保存索引
                saveRollbackData()
                
                rollbackPointId
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun deleteBackup(rollbackPointId: String): Boolean {
        return try {
            // 从存储删除
            val backupKey = "$BACKUP_PREFIX$rollbackPointId"
            val success = storageManager.removeConfig(backupKey)
            
            if (success) {
                // 从内存缓存删除
                rollbackPoints.values.forEach { backupList ->
                    backupList.removeAll { it.id == rollbackPointId }
                }
                
                // 保存索引
                saveRollbackData()
            }
            
            success
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun getBackupPoints(componentId: String): List<RollbackPoint> {
        return rollbackPoints[componentId]?.sortedByDescending { it.timestamp } ?: emptyList()
    }
    
    override suspend fun getAllBackupPoints(): List<RollbackPoint> {
        return rollbackPoints.values.flatten().sortedByDescending { it.timestamp }
    }
    
    override suspend fun rollback(componentId: String, rollbackPointId: String?): Boolean {
        return try {
            val targetRollbackPoint = if (rollbackPointId != null) {
                // 回滚到指定备份点
                findRollbackPoint(rollbackPointId)
            } else {
                // 回滚到最近的备份点
                getBackupPoints(componentId).firstOrNull()
            }
            
            if (targetRollbackPoint == null) {
                return false
            }
            
            // 创建回滚操作记录
            val operationId = generateOperationId()
            val currentComponent = dynamicEngine.getComponent(componentId)
            val rollbackOperation = RollbackOperation(
                id = operationId,
                componentId = componentId,
                fromVersion = currentComponent?.component?.version ?: "unknown",
                toVersion = targetRollbackPoint.version,
                rollbackPointId = targetRollbackPoint.id,
                timestamp = System.currentTimeMillis(),
                status = RollbackStatus.PENDING,
                reason = "手动回滚操作"
            )
            
            rollbackOperations.add(rollbackOperation)
            updateRollbackOperationStatus(operationId, RollbackStatus.IN_PROGRESS)
            
            // 执行回滚
            val success = performRollback(targetRollbackPoint)
            
            // 更新操作状态
            val finalStatus = if (success) RollbackStatus.COMPLETED else RollbackStatus.FAILED
            updateRollbackOperationStatus(operationId, finalStatus)
            
            // 保存操作历史
            saveRollbackData()
            
            success
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun rollbackToVersion(componentId: String, version: String): Boolean {
        val backupPoints = getBackupPoints(componentId)
        val targetBackup = backupPoints.find { it.version == version }
        
        return if (targetBackup != null) {
            rollback(componentId, targetBackup.id)
        } else {
            false
        }
    }
    
    override suspend fun rollbackBatch(componentIds: List<String>): Map<String, Boolean> {
        val results = mutableMapOf<String, Boolean>()
        
        if (!rollbackPolicy.enableBatchRollback) {
            componentIds.forEach { results[it] = false }
            return results
        }
        
        // 并行执行回滚
        val jobs = componentIds.map { componentId ->
            scope.async {
                componentId to rollback(componentId)
            }
        }
        
        jobs.awaitAll().forEach { (componentId, success) ->
            results[componentId] = success
        }
        
        return results
    }
    
    override suspend fun getRollbackHistory(componentId: String): List<RollbackOperation> {
        return rollbackOperations.filter { it.componentId == componentId }
            .sortedByDescending { it.timestamp }
    }
    
    override suspend fun getAllRollbackHistory(): List<RollbackOperation> {
        return rollbackOperations.sortedByDescending { it.timestamp }
    }
    
    override suspend fun cancelRollback(operationId: String): Boolean {
        val operation = rollbackOperations.find { it.id == operationId }
        return if (operation != null && operation.status == RollbackStatus.PENDING) {
            updateRollbackOperationStatus(operationId, RollbackStatus.CANCELLED)
            saveRollbackData()
            true
        } else {
            false
        }
    }
    
    override fun updateRollbackPolicy(policy: RollbackPolicy) {
        rollbackPolicy = policy
        saveRollbackPolicy()
        
        // 重启自动清理任务
        if (policy.autoCleanup) {
            startAutoCleanupTask()
        }
    }
    
    override fun getRollbackPolicy(): RollbackPolicy = rollbackPolicy
    
    override suspend fun cleanupExpiredBackups(): Int {
        val cutoffTime = System.currentTimeMillis() - (rollbackPolicy.retentionDays * 24 * 60 * 60 * 1000L)
        var cleanedCount = 0
        
        rollbackPoints.values.forEach { backupList ->
            val expiredBackups = backupList.filter { it.timestamp < cutoffTime }
            expiredBackups.forEach { backup ->
                if (deleteBackup(backup.id)) {
                    cleanedCount++
                }
            }
        }
        
        return cleanedCount
    }
    
    override suspend fun validateBackupIntegrity(): Map<String, Boolean> {
        val results = mutableMapOf<String, Boolean>()
        
        getAllBackupPoints().forEach { rollbackPoint ->
            try {
                // 验证备份数据完整性
                val backupKey = "$BACKUP_PREFIX${rollbackPoint.id}"
                val backupData = storageManager.getConfig(backupKey)
                
                if (backupData != null) {
                    val restoredPoint = Json.decodeFromString<RollbackPoint>(backupData)
                    val isValid = restoredPoint.checksum == rollbackPoint.checksum
                    results[rollbackPoint.id] = isValid
                } else {
                    results[rollbackPoint.id] = false
                }
            } catch (e: Exception) {
                results[rollbackPoint.id] = false
            }
        }
        
        return results
    }
    
    override suspend fun exportBackups(): String {
        val exportData = mapOf(
            "version" to "1.0",
            "timestamp" to System.currentTimeMillis(),
            "rollbackPoints" to getAllBackupPoints(),
            "rollbackOperations" to rollbackOperations,
            "policy" to rollbackPolicy
        )
        
        return Json.encodeToString(exportData)
    }
    
    override suspend fun importBackups(data: String): Boolean {
        return try {
            val importData = Json.parseToJsonElement(data).jsonObject
            
            // 导入回滚点
            val rollbackPointsJson = importData["rollbackPoints"]
            if (rollbackPointsJson != null) {
                val importedPoints = Json.decodeFromJsonElement<List<RollbackPoint>>(rollbackPointsJson)
                
                importedPoints.forEach { point ->
                    val backupKey = "$BACKUP_PREFIX${point.id}"
                    val backupData = Json.encodeToString(point)
                    storageManager.saveConfig(backupKey, backupData)
                    
                    val componentBackups = rollbackPoints.getOrPut(point.componentId) { mutableListOf() }
                    componentBackups.add(point)
                }
            }
            
            // 导入操作历史
            val operationsJson = importData["rollbackOperations"]
            if (operationsJson != null) {
                val importedOperations = Json.decodeFromJsonElement<List<RollbackOperation>>(operationsJson)
                rollbackOperations.addAll(importedOperations)
            }
            
            // 导入策略
            val policyJson = importData["policy"]
            if (policyJson != null) {
                rollbackPolicy = Json.decodeFromJsonElement(policyJson)
            }
            
            saveRollbackData()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    // 私有辅助方法
    private fun generateRollbackPointId(componentId: String): String {
        return "rollback_${componentId}_${System.currentTimeMillis()}"
    }
    
    private fun generateOperationId(): String {
        return "operation_${System.currentTimeMillis()}_${(0..999).random()}"
    }
    
    private fun calculateChecksum(component: DynamicComponent): String {
        val content = "${component.id}${component.version}${component.content}"
        return content.hashCode().toString()
    }
    
    private fun findRollbackPoint(rollbackPointId: String): RollbackPoint? {
        return rollbackPoints.values.flatten().find { it.id == rollbackPointId }
    }
    
    private suspend fun performRollback(rollbackPoint: RollbackPoint): Boolean {
        return try {
            // 解析备份的组件数据
            val component = Json.decodeFromString<DynamicComponent>(rollbackPoint.componentData)
            
            // 卸载当前组件
            dynamicEngine.unloadComponent(rollbackPoint.componentId)
            
            // 加载备份的组件
            val loadResult = dynamicEngine.loadComponent(component)
            
            loadResult.success
        } catch (e: Exception) {
            false
        }
    }
    
    private fun manageBackupPoints(componentId: String) {
        val backupList = rollbackPoints[componentId] ?: return
        
        // 按时间排序，保留最新的备份点
        backupList.sortByDescending { it.timestamp }
        
        // 删除超出限制的备份点
        while (backupList.size > rollbackPolicy.maxRollbackPoints) {
            val oldestBackup = backupList.removeLastOrNull()
            if (oldestBackup != null) {
                scope.launch {
                    deleteBackup(oldestBackup.id)
                }
            }
        }
    }
    
    private fun updateRollbackOperationStatus(operationId: String, status: RollbackStatus) {
        val operationIndex = rollbackOperations.indexOfFirst { it.id == operationId }
        if (operationIndex >= 0) {
            rollbackOperations[operationIndex] = rollbackOperations[operationIndex].copy(status = status)
        }
    }
    
    private fun startAutoCleanupTask() {
        scope.launch {
            while (isActive) {
                try {
                    cleanupExpiredBackups()
                    delay(24 * 60 * 60 * 1000L) // 每24小时执行一次
                } catch (e: Exception) {
                    // 忽略清理错误
                }
            }
        }
    }
    
    private suspend fun loadRollbackData() {
        try {
            // 加载回滚点索引
            val rollbackPointsJson = storageManager.getConfig(ROLLBACK_POINTS_KEY)
            if (rollbackPointsJson != null) {
                val pointsMap: Map<String, List<RollbackPoint>> = Json.decodeFromString(rollbackPointsJson)
                pointsMap.forEach { (componentId, points) ->
                    rollbackPoints[componentId] = points.toMutableList()
                }
            }
            
            // 加载操作历史
            val operationsJson = storageManager.getConfig(ROLLBACK_OPERATIONS_KEY)
            if (operationsJson != null) {
                val operations: List<RollbackOperation> = Json.decodeFromString(operationsJson)
                rollbackOperations.addAll(operations)
            }
            
            // 加载策略
            val policyJson = storageManager.getConfig(ROLLBACK_POLICY_KEY)
            if (policyJson != null) {
                rollbackPolicy = Json.decodeFromString(policyJson)
            }
        } catch (e: Exception) {
            // 使用默认配置
        }
    }
    
    private fun saveRollbackData() {
        scope.launch {
            try {
                // 保存回滚点索引
                val pointsMap = rollbackPoints.mapValues { it.value.toList() }
                val rollbackPointsJson = Json.encodeToString(pointsMap)
                storageManager.saveConfig(ROLLBACK_POINTS_KEY, rollbackPointsJson)
                
                // 保存操作历史
                val operationsJson = Json.encodeToString(rollbackOperations)
                storageManager.saveConfig(ROLLBACK_OPERATIONS_KEY, operationsJson)
            } catch (e: Exception) {
                // 忽略保存错误
            }
        }
    }
    
    private fun saveRollbackPolicy() {
        scope.launch {
            try {
                val policyJson = Json.encodeToString(rollbackPolicy)
                storageManager.saveConfig(ROLLBACK_POLICY_KEY, policyJson)
            } catch (e: Exception) {
                // 忽略保存错误
            }
        }
    }
}
