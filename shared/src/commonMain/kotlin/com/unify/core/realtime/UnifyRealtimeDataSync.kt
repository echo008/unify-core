package com.unify.core.realtime

import kotlinx.coroutines.*
import kotlinx.serialization.Contextual
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * 实时数据同步管理器
 * 提供跨平台的实时数据同步能力，支持冲突解决、版本控制和离线缓存
 */
class UnifyRealtimeDataSync(
    private val webSocketManager: UnifyWebSocketManager,
    private val config: RealtimeSyncConfig = RealtimeSyncConfig()
) {
    private val _syncState = MutableStateFlow(SyncState.IDLE)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()
    
    private val _connectedClients = MutableStateFlow(0)
    val connectedClients: StateFlow<Int> = _connectedClients.asStateFlow()
    
    private val dataStore = mutableMapOf<String, SyncDataEntry>()
    private val conflictResolver = ConflictResolver()
    private val versionManager = VersionManager()
    private val changeBuffer = mutableListOf<DataChange>()
    
    private var syncJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    private val _dataUpdates = MutableSharedFlow<DataUpdate>(replay = 0, extraBufferCapacity = 100)
    val dataUpdates: SharedFlow<DataUpdate> = _dataUpdates.asSharedFlow()
    
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    init {
        // 监听WebSocket连接状态
        coroutineScope.launch {
            webSocketManager.connectionState.collect { state ->
                when (state) {
                    WebSocketState.CONNECTED -> {
                        _syncState.value = SyncState.SYNCING
                        startSyncProcess()
                    }
                    WebSocketState.DISCONNECTED -> {
                        _syncState.value = SyncState.OFFLINE
                        stopSyncProcess()
                    }
                    WebSocketState.ERROR -> {
                        _syncState.value = SyncState.ERROR
                        handleSyncError()
                    }
                    else -> { /* 其他状态暂不处理 */ }
                }
            }
        }
        
        // 监听WebSocket消息
        coroutineScope.launch {
            webSocketManager.messageFlow.collect { message ->
                message?.let { handleIncomingMessage(it) }
            }
        }
    }
    
    /**
     * 设置数据
     */
    suspend fun setData(key: String, value: Any, metadata: Map<String, String> = emptyMap()): SyncResult {
        return try {
            val serializedValue = json.encodeToString(value)
            val timestamp = com.unify.core.platform.getCurrentTimeMillis()
            val version = versionManager.getNextVersion(key)
            
            val entry = SyncDataEntry(
                key = key,
                value = serializedValue,
                version = version,
                timestamp = timestamp,
                metadata = metadata,
                clientId = config.clientId,
                changeType = ChangeType.UPDATE
            )
            
            // 本地存储
            dataStore[key] = entry
            
            // 记录变更
            val change = DataChange(
                key = key,
                oldValue = dataStore[key]?.value,
                newValue = serializedValue,
                timestamp = timestamp,
                version = version,
                clientId = config.clientId
            )
            changeBuffer.add(change)
            
            // 发送到服务器
            if (_syncState.value == SyncState.SYNCING) {
                sendDataUpdate(entry)
            }
            
            // 通知本地订阅者
            _dataUpdates.emit(DataUpdate(key, serializedValue, UpdateType.LOCAL_UPDATE))
            
            SyncResult.Success("数据设置成功")
        } catch (e: Exception) {
            SyncResult.Error("数据设置失败: ${e.message}")
        }
    }
    
    /**
     * 获取数据
     */
    fun getData(key: String): String? {
        return try {
            val entry = dataStore[key]
            entry?.value
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 删除数据
     */
    suspend fun deleteData(key: String): SyncResult {
        return try {
            val existingEntry = dataStore[key]
            if (existingEntry == null) {
                return SyncResult.Error("数据不存在: $key")
            }
            
            val timestamp = com.unify.core.platform.getCurrentTimeMillis()
            val version = versionManager.getNextVersion(key)
            
            val deleteEntry = SyncDataEntry(
                key = key,
                value = "",
                version = version,
                timestamp = timestamp,
                metadata = emptyMap(),
                clientId = config.clientId,
                changeType = ChangeType.DELETE
            )
            
            // 本地删除
            dataStore.remove(key)
            
            // 记录变更
            val change = DataChange(
                key = key,
                oldValue = existingEntry.value,
                newValue = null,
                timestamp = timestamp,
                version = version,
                clientId = config.clientId
            )
            changeBuffer.add(change)
            
            // 发送删除通知
            if (_syncState.value == SyncState.SYNCING) {
                sendDataUpdate(deleteEntry)
            }
            
            // 通知本地订阅者
            _dataUpdates.emit(DataUpdate(key, null, UpdateType.LOCAL_DELETE))
            
            SyncResult.Success("数据删除成功")
        } catch (e: Exception) {
            SyncResult.Error("数据删除失败: ${e.message}")
        }
    }
    
    /**
     * 订阅数据变更
     */
    fun subscribeToKey(key: String): Flow<String?> {
        return dataUpdates
            .filter { it.key == key }
            .map { it.value }
            .onStart { 
                // 发送当前值
                emit(dataStore[key]?.value)
            }
    }
    
    /**
     * 批量获取数据
     */
    fun getBatchData(keys: List<String>): Map<String, String?> {
        return keys.associateWith { key ->
            dataStore[key]?.value
        }
    }
    
    /**
     * 强制同步
     */
    suspend fun forceSync(): SyncResult {
        return try {
            if (_syncState.value != SyncState.SYNCING) {
                return SyncResult.Error("未连接到服务器")
            }
            
            // 发送所有本地数据
            dataStore.values.forEach { entry ->
                sendDataUpdate(entry)
            }
            
            // 请求服务器数据
            requestFullSync()
            
            SyncResult.Success("强制同步完成")
        } catch (e: Exception) {
            SyncResult.Error("强制同步失败: ${e.message}")
        }
    }
    
    /**
     * 获取同步统计信息
     */
    fun getSyncStats(): SyncStats {
        return SyncStats(
            totalEntries = dataStore.size,
            pendingChanges = changeBuffer.size,
            lastSyncTime = versionManager.getLastSyncTime(),
            syncState = _syncState.value,
            connectedClients = _connectedClients.value,
            conflictCount = conflictResolver.getConflictCount()
        )
    }
    
    /**
     * 清理过期数据
     */
    suspend fun cleanupExpiredData(): Int {
        val currentTime = com.unify.core.platform.getCurrentTimeMillis()
        val expiredKeys = mutableListOf<String>()
        
        dataStore.entries.forEach { (key, entry) ->
            val ttl = entry.metadata["ttl"]?.toLongOrNull()
            if (ttl != null && currentTime - entry.timestamp > ttl) {
                expiredKeys.add(key)
            }
        }
        
        expiredKeys.forEach { key ->
            dataStore.remove(key)
            _dataUpdates.emit(DataUpdate(key, null, UpdateType.EXPIRED))
        }
        
        return expiredKeys.size
    }
    
    /**
     * 启动同步进程
     */
    private fun startSyncProcess() {
        syncJob?.cancel()
        syncJob = coroutineScope.launch {
            while (isActive && _syncState.value == SyncState.SYNCING) {
                try {
                    // 发送待同步的变更
                    sendPendingChanges()
                    
                    // 清理过期数据
                    if (config.autoCleanup) {
                        cleanupExpiredData()
                    }
                    
                    delay(config.syncInterval.inWholeMilliseconds)
                } catch (e: Exception) {
                    println("同步进程错误: ${e.message}")
                }
            }
        }
    }
    
    /**
     * 停止同步进程
     */
    private fun stopSyncProcess() {
        syncJob?.cancel()
        syncJob = null
    }
    
    /**
     * 处理同步错误
     */
    private fun handleSyncError() {
        // 保存未同步的变更到本地缓存
        // 等待重新连接后恢复同步
    }
    
    /**
     * 发送数据更新
     */
    private suspend fun sendDataUpdate(entry: SyncDataEntry) {
        val message = SyncMessage(
            type = MessageType.DATA_UPDATE,
            data = entry,
            clientId = config.clientId,
            timestamp = com.unify.core.platform.getCurrentTimeMillis()
        )
        
        val serialized = json.encodeToString(message)
        webSocketManager.sendMessage(serialized)
    }
    
    /**
     * 发送待同步变更
     */
    private suspend fun sendPendingChanges() {
        if (changeBuffer.isEmpty()) return
        
        val changes = changeBuffer.toList()
        changeBuffer.clear()
        
        val message = SyncMessage(
            type = MessageType.BATCH_UPDATE,
            data = changes,
            clientId = config.clientId,
            timestamp = com.unify.core.platform.getCurrentTimeMillis()
        )
        
        val serialized = json.encodeToString(message)
        webSocketManager.sendMessage(serialized)
    }
    
    /**
     * 请求完整同步
     */
    private suspend fun requestFullSync() {
        val message = SyncMessage(
            type = MessageType.SYNC_REQUEST,
            data = versionManager.getAllVersions(),
            clientId = config.clientId,
            timestamp = com.unify.core.platform.getCurrentTimeMillis()
        )
        
        val serialized = json.encodeToString(message)
        webSocketManager.sendMessage(serialized)
    }
    
    /**
     * 处理接收到的消息
     */
    private suspend fun handleIncomingMessage(message: WebSocketMessage) {
        try {
            when (message.type) {
                MessageType.TEXT -> {
                    val syncMessage = json.decodeFromString<SyncMessage>(message.data)
                    handleSyncMessage(syncMessage)
                }
                else -> { /* 忽略其他类型消息 */ }
            }
        } catch (e: Exception) {
            println("处理消息失败: ${e.message}")
        }
    }
    
    /**
     * 处理同步消息
     */
    private suspend fun handleSyncMessage(message: SyncMessage) {
        when (message.type) {
            MessageType.DATA_UPDATE -> {
                val entry = message.data as? SyncDataEntry ?: return
                handleRemoteDataUpdate(entry)
            }
            MessageType.BATCH_UPDATE -> {
                val changes = message.data as? List<DataChange> ?: return
                handleBatchUpdate(changes)
            }
            MessageType.SYNC_RESPONSE -> {
                val serverData = message.data as? Map<String, SyncDataEntry> ?: return
                handleSyncResponse(serverData)
            }
            MessageType.CLIENT_COUNT -> {
                val count = message.data as? Int ?: return
                _connectedClients.value = count
            }
            MessageType.CONFLICT -> {
                val conflict = message.data as? DataConflict ?: return
                handleConflict(conflict)
            }
            else -> { /* 忽略未知消息类型 */ }
        }
    }
    
    /**
     * 处理远程数据更新
     */
    private suspend fun handleRemoteDataUpdate(entry: SyncDataEntry) {
        val existingEntry = dataStore[entry.key]
        
        // 检查版本冲突
        if (existingEntry != null && existingEntry.version >= entry.version) {
            // 可能的冲突，使用冲突解决策略
            val resolution = conflictResolver.resolve(existingEntry, entry)
            when (resolution.strategy) {
                ConflictStrategy.USE_REMOTE -> {
                    dataStore[entry.key] = entry
                    _dataUpdates.emit(DataUpdate(entry.key, entry.value, UpdateType.REMOTE_UPDATE))
                }
                ConflictStrategy.USE_LOCAL -> {
                    // 保持本地版本，发送本地数据到服务器
                    sendDataUpdate(existingEntry)
                }
                ConflictStrategy.MERGE -> {
                    val mergedEntry = resolution.mergedData ?: entry
                    dataStore[entry.key] = mergedEntry
                    _dataUpdates.emit(DataUpdate(entry.key, mergedEntry.value, UpdateType.MERGED_UPDATE))
                }
            }
        } else {
            // 无冲突，直接更新
            if (entry.changeType == ChangeType.DELETE) {
                dataStore.remove(entry.key)
                _dataUpdates.emit(DataUpdate(entry.key, null, UpdateType.REMOTE_DELETE))
            } else {
                dataStore[entry.key] = entry
                _dataUpdates.emit(DataUpdate(entry.key, entry.value, UpdateType.REMOTE_UPDATE))
            }
        }
        
        // 更新版本信息
        versionManager.updateVersion(entry.key, entry.version)
    }
    
    /**
     * 处理批量更新
     */
    private suspend fun handleBatchUpdate(changes: List<DataChange>) {
        changes.forEach { change ->
            val entry = SyncDataEntry(
                key = change.key,
                value = change.newValue ?: "",
                version = change.version,
                timestamp = change.timestamp,
                metadata = emptyMap(),
                clientId = change.clientId,
                changeType = if (change.newValue == null) ChangeType.DELETE else ChangeType.UPDATE
            )
            handleRemoteDataUpdate(entry)
        }
    }
    
    /**
     * 处理同步响应
     */
    private suspend fun handleSyncResponse(serverData: Map<String, SyncDataEntry>) {
        serverData.values.forEach { entry ->
            handleRemoteDataUpdate(entry)
        }
    }
    
    /**
     * 处理冲突
     */
    private suspend fun handleConflict(conflict: DataConflict) {
        val resolution = conflictResolver.resolve(conflict.localEntry, conflict.remoteEntry)
        
        when (resolution.strategy) {
            ConflictStrategy.USE_REMOTE -> {
                dataStore[conflict.key] = conflict.remoteEntry
                _dataUpdates.emit(DataUpdate(conflict.key, conflict.remoteEntry.value, UpdateType.CONFLICT_RESOLVED))
            }
            ConflictStrategy.USE_LOCAL -> {
                sendDataUpdate(conflict.localEntry)
            }
            ConflictStrategy.MERGE -> {
                val mergedEntry = resolution.mergedData ?: conflict.remoteEntry
                dataStore[conflict.key] = mergedEntry
                sendDataUpdate(mergedEntry)
                _dataUpdates.emit(DataUpdate(conflict.key, mergedEntry.value, UpdateType.CONFLICT_RESOLVED))
            }
        }
    }
    
    /**
     * 清理资源
     */
    fun cleanup() {
        syncJob?.cancel()
        coroutineScope.cancel()
        dataStore.clear()
        changeBuffer.clear()
    }
}

/**
 * 实时同步配置
 */
@Serializable
data class RealtimeSyncConfig(
    val clientId: String = generateClientId(),
    val syncInterval: Duration = 5.seconds,
    val conflictStrategy: ConflictStrategy = ConflictStrategy.USE_REMOTE,
    val autoCleanup: Boolean = true,
    val maxCacheSize: Int = 1000,
    val compressionEnabled: Boolean = true
)

/**
 * 同步数据条目
 */
@Serializable
data class SyncDataEntry(
    val key: String,
    val value: String,
    val version: Long,
    val timestamp: Long,
    val metadata: Map<String, String>,
    val clientId: String,
    val changeType: ChangeType
)

/**
 * 数据变更记录
 */
@Serializable
data class DataChange(
    val key: String,
    val oldValue: String?,
    val newValue: String?,
    val timestamp: Long,
    val version: Long,
    val clientId: String
)

/**
 * 同步消息
 */
@Serializable
data class SyncMessage(
    val type: MessageType,
    @Contextual val data: Any,
    val clientId: String,
    val timestamp: Long
)

/**
 * 数据更新通知
 */
data class DataUpdate(
    val key: String,
    val value: String?,
    val type: UpdateType
)

/**
 * 数据冲突
 */
data class DataConflict(
    val key: String,
    val localEntry: SyncDataEntry,
    val remoteEntry: SyncDataEntry
)

/**
 * 冲突解决结果
 */
data class ConflictResolution(
    val strategy: ConflictStrategy,
    val mergedData: SyncDataEntry?
)

/**
 * 同步统计信息
 */
data class SyncStats(
    val totalEntries: Int,
    val pendingChanges: Int,
    val lastSyncTime: Long,
    val syncState: SyncState,
    val connectedClients: Int,
    val conflictCount: Int
)

/**
 * 同步状态
 */
enum class SyncState {
    IDLE,       // 空闲
    SYNCING,    // 同步中
    OFFLINE,    // 离线
    ERROR       // 错误
}

/**
 * 变更类型
 */
enum class ChangeType {
    CREATE,     // 创建
    UPDATE,     // 更新
    DELETE      // 删除
}

/**
 * 更新类型
 */
enum class UpdateType {
    LOCAL_UPDATE,       // 本地更新
    LOCAL_DELETE,       // 本地删除
    REMOTE_UPDATE,      // 远程更新
    REMOTE_DELETE,      // 远程删除
    MERGED_UPDATE,      // 合并更新
    CONFLICT_RESOLVED,  // 冲突解决
    EXPIRED            // 过期删除
}

/**
 * 冲突策略
 */
enum class ConflictStrategy {
    USE_LOCAL,  // 使用本地版本
    USE_REMOTE, // 使用远程版本
    MERGE       // 合并版本
}

/**
 * 消息类型
 */
enum class MessageType {
    DATA_UPDATE,    // 数据更新
    BATCH_UPDATE,   // 批量更新
    SYNC_REQUEST,   // 同步请求
    SYNC_RESPONSE,  // 同步响应
    CLIENT_COUNT,   // 客户端数量
    CONFLICT,       // 冲突通知
    TEXT,          // 文本消息
    BINARY,        // 二进制消息
    HEARTBEAT,     // 心跳
    ERROR          // 错误
}

/**
 * 同步结果
 */
sealed class SyncResult {
    data class Success(val message: String) : SyncResult()
    data class Error(val message: String) : SyncResult()
}

/**
 * 冲突解决器
 */
class ConflictResolver(
    private val defaultStrategy: ConflictStrategy = ConflictStrategy.USE_REMOTE
) {
    private var conflictCount = 0
    
    fun resolve(localEntry: SyncDataEntry, remoteEntry: SyncDataEntry): ConflictResolution {
        conflictCount++
        
        return when (defaultStrategy) {
            ConflictStrategy.USE_LOCAL -> ConflictResolution(ConflictStrategy.USE_LOCAL, null)
            ConflictStrategy.USE_REMOTE -> ConflictResolution(ConflictStrategy.USE_REMOTE, null)
            ConflictStrategy.MERGE -> {
                // 简单的合并策略：使用时间戳较新的版本
                val mergedEntry = if (localEntry.timestamp > remoteEntry.timestamp) {
                    localEntry
                } else {
                    remoteEntry
                }
                ConflictResolution(ConflictStrategy.MERGE, mergedEntry)
            }
        }
    }
    
    fun getConflictCount(): Int = conflictCount
}

/**
 * 版本管理器
 */
class VersionManager {
    private val versions = mutableMapOf<String, Long>()
    private var lastSyncTime = 0L
    
    fun getNextVersion(key: String): Long {
        val currentVersion = versions[key] ?: 0L
        val nextVersion = currentVersion + 1
        versions[key] = nextVersion
        return nextVersion
    }
    
    fun updateVersion(key: String, version: Long) {
        val currentVersion = versions[key] ?: 0L
        if (version > currentVersion) {
            versions[key] = version
        }
        lastSyncTime = com.unify.core.platform.getCurrentTimeMillis()
    }
    
    fun getAllVersions(): Map<String, Long> = versions.toMap()
    
    fun getLastSyncTime(): Long = lastSyncTime
}

/**
 * 生成客户端ID
 */
private fun generateClientId(): String {
    return "client_${com.unify.core.platform.getCurrentTimeMillis()}_${(0..999).random()}"
}
