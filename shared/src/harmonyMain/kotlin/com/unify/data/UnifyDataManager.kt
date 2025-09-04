package com.unify.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * HarmonyOS平台数据管理器实现
 * 基于HarmonyOS分布式数据管理和本地存储实现
 */
class HarmonyUnifyDataManager : UnifyDataManager {
    
    private val localStorage = mutableMapOf<String, String>()
    private val distributedStorage = mutableMapOf<String, String>()
    private val _connectionState = MutableStateFlow(DataConnectionState.CONNECTED)
    
    companion object {
        private const val DISTRIBUTED_PREFIX = "distributed_"
        private const val LOCAL_PREFIX = "local_"
    }
    
    override suspend fun <T> save(key: String, value: T, serializer: kotlinx.serialization.KSerializer<T>) {
        try {
            val jsonString = Json.encodeToString(serializer, value)
            
            // 根据key前缀决定存储位置
            when {
                key.startsWith(DISTRIBUTED_PREFIX) -> {
                    // 存储到分布式数据库
                    distributedStorage[key] = jsonString
                    // 模拟分布式同步
                    syncToDistributedDevices(key, jsonString)
                }
                else -> {
                    // 存储到本地
                    localStorage[key] = jsonString
                }
            }
        } catch (e: Exception) {
            throw DataException("Failed to save data for key: $key", e)
        }
    }
    
    override suspend fun <T> load(key: String, serializer: kotlinx.serialization.KSerializer<T>): T? {
        return try {
            val jsonString = when {
                key.startsWith(DISTRIBUTED_PREFIX) -> distributedStorage[key]
                else -> localStorage[key]
            }
            
            jsonString?.let { Json.decodeFromString(serializer, it) }
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun delete(key: String) {
        try {
            when {
                key.startsWith(DISTRIBUTED_PREFIX) -> {
                    distributedStorage.remove(key)
                    // 同步删除到分布式设备
                    syncDeleteToDistributedDevices(key)
                }
                else -> {
                    localStorage.remove(key)
                }
            }
        } catch (e: Exception) {
            throw DataException("Failed to delete data for key: $key", e)
        }
    }
    
    override suspend fun clear() {
        try {
            localStorage.clear()
            distributedStorage.clear()
            // 清空分布式数据
            clearDistributedData()
        } catch (e: Exception) {
            throw DataException("Failed to clear all data", e)
        }
    }
    
    override suspend fun exists(key: String): Boolean {
        return when {
            key.startsWith(DISTRIBUTED_PREFIX) -> distributedStorage.containsKey(key)
            else -> localStorage.containsKey(key)
        }
    }
    
    override suspend fun getAllKeys(): Set<String> {
        return localStorage.keys + distributedStorage.keys
    }
    
    override fun observeConnectionState(): Flow<DataConnectionState> {
        return _connectionState.asStateFlow()
    }
    
    override suspend fun sync() {
        try {
            _connectionState.value = DataConnectionState.SYNCING
            
            // 同步分布式数据
            syncDistributedData()
            
            // 检查设备连接状态
            val connectedDevices = getConnectedHarmonyDevices()
            if (connectedDevices.isNotEmpty()) {
                _connectionState.value = DataConnectionState.CONNECTED
            } else {
                _connectionState.value = DataConnectionState.OFFLINE
            }
        } catch (e: Exception) {
            _connectionState.value = DataConnectionState.ERROR
            throw DataException("Failed to sync data", e)
        }
    }
    
    override suspend fun backup(): String {
        return try {
            val backupData = mapOf(
                "local" to localStorage,
                "distributed" to distributedStorage,
                "timestamp" to System.currentTimeMillis().toString(),
                "platform" to "HarmonyOS"
            )
            
            val backupJson = Json.encodeToString(backupData)
            val backupId = "harmony_backup_${System.currentTimeMillis()}"
            
            // 存储备份到HarmonyOS云服务
            storeToHarmonyCloud(backupId, backupJson)
            
            backupId
        } catch (e: Exception) {
            throw DataException("Failed to create backup", e)
        }
    }
    
    override suspend fun restore(backupPath: String) {
        try {
            // 从HarmonyOS云服务恢复
            val backupJson = loadFromHarmonyCloud(backupPath)
            val backupData = Json.decodeFromString<Map<String, Map<String, String>>>(backupJson)
            
            // 恢复本地数据
            backupData["local"]?.let { localData ->
                localStorage.clear()
                localStorage.putAll(localData)
            }
            
            // 恢复分布式数据
            backupData["distributed"]?.let { distributedData ->
                distributedStorage.clear()
                distributedStorage.putAll(distributedData)
            }
            
            // 同步到其他设备
            sync()
        } catch (e: Exception) {
            throw DataException("Failed to restore from backup: $backupPath", e)
        }
    }
    
    override suspend fun getStorageInfo(): StorageInfo {
        return try {
            val localSize = calculateDataSize(localStorage)
            val distributedSize = calculateDataSize(distributedStorage)
            val totalSize = localSize + distributedSize
            
            StorageInfo(
                totalSpace = 1024 * 1024 * 1024, // 1GB 模拟总空间
                usedSpace = totalSize,
                freeSpace = 1024 * 1024 * 1024 - totalSize,
                cacheSize = calculateCacheSize()
            )
        } catch (e: Exception) {
            StorageInfo(0, 0, 0, 0)
        }
    }
    
    // HarmonyOS特有功能
    
    /**
     * 获取连接的HarmonyOS设备列表
     */
    suspend fun getConnectedHarmonyDevices(): List<HarmonyDevice> {
        return try {
            // 模拟获取连接的设备
            listOf(
                HarmonyDevice("phone", "Mate 60 Pro", true),
                HarmonyDevice("tablet", "MatePad Pro", true),
                HarmonyDevice("watch", "Watch GT 4", false),
                HarmonyDevice("tv", "Vision Smart TV", true)
            )
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * 跨设备数据同步
     */
    suspend fun syncAcrossDevices(deviceIds: List<String>) {
        try {
            deviceIds.forEach { deviceId ->
                // 同步分布式数据到指定设备
                syncToDevice(deviceId, distributedStorage)
            }
        } catch (e: Exception) {
            throw DataException("Failed to sync across devices", e)
        }
    }
    
    /**
     * 启用分布式数据管理
     */
    suspend fun enableDistributedData(key: String) {
        val value = localStorage[key]
        if (value != null) {
            localStorage.remove(key)
            distributedStorage["${DISTRIBUTED_PREFIX}$key"] = value
            sync()
        }
    }
    
    // 私有辅助方法
    
    private suspend fun syncToDistributedDevices(key: String, value: String) {
        // 模拟分布式同步逻辑
        val connectedDevices = getConnectedHarmonyDevices()
        connectedDevices.filter { it.isConnected }.forEach { device ->
            // 同步到设备
            syncToDevice(device.id, mapOf(key to value))
        }
    }
    
    private suspend fun syncDeleteToDistributedDevices(key: String) {
        val connectedDevices = getConnectedHarmonyDevices()
        connectedDevices.filter { it.isConnected }.forEach { device ->
            deleteFromDevice(device.id, key)
        }
    }
    
    private suspend fun syncDistributedData() {
        // 同步分布式数据逻辑
        val connectedDevices = getConnectedHarmonyDevices()
        connectedDevices.forEach { device ->
            if (device.isConnected) {
                val deviceData = getDataFromDevice(device.id)
                deviceData.forEach { (key, value) ->
                    if (key.startsWith(DISTRIBUTED_PREFIX)) {
                        distributedStorage[key] = value
                    }
                }
            }
        }
    }
    
    private suspend fun clearDistributedData() {
        val connectedDevices = getConnectedHarmonyDevices()
        connectedDevices.forEach { device ->
            clearDeviceData(device.id)
        }
    }
    
    private suspend fun storeToHarmonyCloud(backupId: String, data: String) {
        // 模拟存储到HarmonyOS云服务
    }
    
    private suspend fun loadFromHarmonyCloud(backupId: String): String {
        // 模拟从HarmonyOS云服务加载
        return "{\"local\":{},\"distributed\":{},\"timestamp\":\"${System.currentTimeMillis()}\",\"platform\":\"HarmonyOS\"}"
    }
    
    private suspend fun syncToDevice(deviceId: String, data: Map<String, String>) {
        // 模拟同步到设备
    }
    
    private suspend fun deleteFromDevice(deviceId: String, key: String) {
        // 模拟从设备删除
    }
    
    private suspend fun getDataFromDevice(deviceId: String): Map<String, String> {
        // 模拟从设备获取数据
        return emptyMap()
    }
    
    private suspend fun clearDeviceData(deviceId: String) {
        // 模拟清空设备数据
    }
    
    private fun calculateDataSize(storage: Map<String, String>): Long {
        return storage.values.sumOf { it.toByteArray().size.toLong() }
    }
    
    private fun calculateCacheSize(): Long {
        return calculateDataSize(localStorage) + calculateDataSize(distributedStorage)
    }
}

/**
 * HarmonyOS设备信息
 */
data class HarmonyDevice(
    val id: String,
    val name: String,
    val isConnected: Boolean
)

/**
 * HarmonyOS平台数据管理器工厂
 */
actual fun createUnifyDataManager(): UnifyDataManager {
    return HarmonyUnifyDataManager()
}
