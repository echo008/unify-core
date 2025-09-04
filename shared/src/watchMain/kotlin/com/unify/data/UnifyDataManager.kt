package com.unify.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Watch平台数据管理器实现
 * 基于可穿戴设备存储API和健康数据管理实现
 */
class WatchUnifyDataManager : UnifyDataManager {
    
    private val localStorage = mutableMapOf<String, String>()
    private val healthStorage = mutableMapOf<String, String>()
    private val sensorStorage = mutableMapOf<String, String>()
    private val _connectionState = MutableStateFlow(DataConnectionState.CONNECTED)
    
    companion object {
        private const val HEALTH_PREFIX = "health_"
        private const val SENSOR_PREFIX = "sensor_"
        private const val WORKOUT_PREFIX = "workout_"
        private const val MAX_STORAGE_SIZE = 4 * 1024 * 1024 // 4MB Watch存储限制
    }
    
    override suspend fun <T> save(key: String, value: T, serializer: kotlinx.serialization.KSerializer<T>) {
        try {
            val jsonString = Json.encodeToString(serializer, value)
            
            // 检查存储大小限制
            if (getCurrentStorageSize() + jsonString.length > MAX_STORAGE_SIZE) {
                // 清理旧数据为新数据腾出空间
                cleanOldData()
            }
            
            when {
                key.startsWith(HEALTH_PREFIX) -> {
                    // 健康数据存储
                    healthStorage[key] = jsonString
                    saveToWatchHealthStorage(key, jsonString)
                }
                key.startsWith(SENSOR_PREFIX) -> {
                    // 传感器数据存储
                    sensorStorage[key] = jsonString
                    saveToWatchSensorStorage(key, jsonString)
                }
                key.startsWith(WORKOUT_PREFIX) -> {
                    // 运动数据存储
                    healthStorage[key] = jsonString
                    saveToWatchWorkoutStorage(key, jsonString)
                }
                else -> {
                    // 通用数据存储
                    localStorage[key] = jsonString
                    saveToWatchGeneralStorage(key, jsonString)
                }
            }
        } catch (e: Exception) {
            throw DataException("Failed to save data for key: $key", e)
        }
    }
    
    override suspend fun <T> load(key: String, serializer: kotlinx.serialization.KSerializer<T>): T? {
        return try {
            val jsonString = when {
                key.startsWith(HEALTH_PREFIX) -> {
                    loadFromWatchHealthStorage(key) ?: healthStorage[key]
                }
                key.startsWith(SENSOR_PREFIX) -> {
                    loadFromWatchSensorStorage(key) ?: sensorStorage[key]
                }
                else -> {
                    loadFromWatchStorage(key) ?: localStorage[key]
                }
            }
            
            jsonString?.let { Json.decodeFromString(serializer, it) }
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun delete(key: String) {
        try {
            when {
                key.startsWith(HEALTH_PREFIX) -> {
                    healthStorage.remove(key)
                    removeFromWatchHealthStorage(key)
                }
                key.startsWith(SENSOR_PREFIX) -> {
                    sensorStorage.remove(key)
                    removeFromWatchSensorStorage(key)
                }
                else -> {
                    localStorage.remove(key)
                    removeFromWatchStorage(key)
                }
            }
        } catch (e: Exception) {
            throw DataException("Failed to delete data for key: $key", e)
        }
    }
    
    override suspend fun clear() {
        try {
            localStorage.clear()
            healthStorage.clear()
            sensorStorage.clear()
            clearWatchStorage()
        } catch (e: Exception) {
            throw DataException("Failed to clear all data", e)
        }
    }
    
    override suspend fun exists(key: String): Boolean {
        return when {
            key.startsWith(HEALTH_PREFIX) -> healthStorage.containsKey(key)
            key.startsWith(SENSOR_PREFIX) -> sensorStorage.containsKey(key)
            else -> localStorage.containsKey(key)
        }
    }
    
    override suspend fun getAllKeys(): Set<String> {
        return localStorage.keys + healthStorage.keys + sensorStorage.keys
    }
    
    override fun observeConnectionState(): Flow<DataConnectionState> {
        return _connectionState.asStateFlow()
    }
    
    override suspend fun sync() {
        try {
            _connectionState.value = DataConnectionState.SYNCING
            
            // 同步健康数据到手机
            syncHealthDataToPhone()
            
            // 同步传感器数据
            syncSensorData()
            
            // 检查与手机的连接状态
            if (isConnectedToPhone()) {
                syncToPhoneApp()
                _connectionState.value = DataConnectionState.CONNECTED
            } else {
                _connectionState.value = DataConnectionState.OFFLINE
            }
        } catch (e: Exception) {
            _connectionState.value = DataConnectionState.ERROR
            throw DataException("Failed to sync watch data", e)
        }
    }
    
    override suspend fun backup(): String {
        return try {
            val backupData = mapOf(
                "local" to localStorage,
                "health" to healthStorage,
                "sensor" to sensorStorage,
                "timestamp" to System.currentTimeMillis().toString(),
                "platform" to "Watch",
                "watchInfo" to getWatchInfo()
            )
            
            val backupJson = Json.encodeToString(backupData)
            val backupId = "watch_backup_${System.currentTimeMillis()}"
            
            // 存储备份到手机应用
            storeToPhoneApp(backupId, backupJson)
            
            backupId
        } catch (e: Exception) {
            throw DataException("Failed to create watch backup", e)
        }
    }
    
    override suspend fun restore(backupPath: String) {
        try {
            val backupJson = loadFromPhoneApp(backupPath)
            val backupData = Json.decodeFromString<Map<String, Map<String, String>>>(backupJson)
            
            // 恢复本地数据
            backupData["local"]?.let { localData ->
                localStorage.clear()
                localStorage.putAll(localData)
            }
            
            // 恢复健康数据
            backupData["health"]?.let { healthData ->
                healthStorage.clear()
                healthStorage.putAll(healthData)
            }
            
            // 恢复传感器数据
            backupData["sensor"]?.let { sensorData ->
                sensorStorage.clear()
                sensorStorage.putAll(sensorData)
            }
            
            // 同步到Watch存储
            sync()
        } catch (e: Exception) {
            throw DataException("Failed to restore watch backup: $backupPath", e)
        }
    }
    
    override suspend fun getStorageInfo(): StorageInfo {
        return try {
            val usedSpace = getCurrentStorageSize()
            val freeSpace = MAX_STORAGE_SIZE - usedSpace
            
            StorageInfo(
                totalSpace = MAX_STORAGE_SIZE.toLong(),
                usedSpace = usedSpace.toLong(),
                freeSpace = freeSpace.toLong(),
                cacheSize = calculateCacheSize()
            )
        } catch (e: Exception) {
            StorageInfo(0, 0, 0, 0)
        }
    }
    
    // Watch特有功能
    
    /**
     * 获取Watch设备信息
     */
    suspend fun getWatchInfo(): Map<String, String> {
        return mapOf(
            "model" to "Smart Watch Pro",
            "display" to "AMOLED 1.4\"",
            "battery" to "300mAh",
            "sensors" to "Heart Rate, SpO2, GPS, Accelerometer, Gyroscope",
            "connectivity" to "Bluetooth 5.0, WiFi",
            "waterResistance" to "5ATM",
            "storage" to "4GB",
            "os" to "WearOS 4.0"
        )
    }
    
    /**
     * 保存心率数据
     */
    suspend fun saveHeartRateData(heartRate: Int, timestamp: Long = System.currentTimeMillis()) {
        val heartRateData = mapOf(
            "heartRate" to heartRate.toString(),
            "timestamp" to timestamp.toString(),
            "type" to "resting"
        )
        save("${HEALTH_PREFIX}heartrate_$timestamp", heartRateData, kotlinx.serialization.serializers.MapSerializer(
            kotlinx.serialization.serializers.StringSerializer(),
            kotlinx.serialization.serializers.StringSerializer()
        ))
    }
    
    /**
     * 获取心率历史数据
     */
    suspend fun getHeartRateHistory(startTime: Long, endTime: Long): List<Map<String, String>> {
        val heartRateKeys = healthStorage.keys.filter { 
            it.startsWith("${HEALTH_PREFIX}heartrate_") 
        }
        
        return heartRateKeys.mapNotNull { key ->
            val timestamp = key.substringAfterLast("_").toLongOrNull()
            if (timestamp != null && timestamp in startTime..endTime) {
                load(key, kotlinx.serialization.serializers.MapSerializer(
                    kotlinx.serialization.serializers.StringSerializer(),
                    kotlinx.serialization.serializers.StringSerializer()
                ))
            } else null
        }
    }
    
    /**
     * 保存步数数据
     */
    suspend fun saveStepData(steps: Int, timestamp: Long = System.currentTimeMillis()) {
        val stepData = mapOf(
            "steps" to steps.toString(),
            "timestamp" to timestamp.toString(),
            "calories" to (steps * 0.04).toString() // 估算卡路里
        )
        save("${HEALTH_PREFIX}steps_$timestamp", stepData, kotlinx.serialization.serializers.MapSerializer(
            kotlinx.serialization.serializers.StringSerializer(),
            kotlinx.serialization.serializers.StringSerializer()
        ))
    }
    
    /**
     * 保存运动数据
     */
    suspend fun saveWorkoutData(workoutType: String, duration: Long, calories: Int) {
        val workoutData = mapOf(
            "type" to workoutType,
            "duration" to duration.toString(),
            "calories" to calories.toString(),
            "timestamp" to System.currentTimeMillis().toString()
        )
        save("${WORKOUT_PREFIX}${workoutType}_${System.currentTimeMillis()}", workoutData, kotlinx.serialization.serializers.MapSerializer(
            kotlinx.serialization.serializers.StringSerializer(),
            kotlinx.serialization.serializers.StringSerializer()
        ))
    }
    
    /**
     * 保存传感器数据
     */
    suspend fun saveSensorData(sensorType: String, values: FloatArray, timestamp: Long = System.currentTimeMillis()) {
        val sensorData = mapOf(
            "type" to sensorType,
            "values" to values.joinToString(","),
            "timestamp" to timestamp.toString()
        )
        save("${SENSOR_PREFIX}${sensorType}_$timestamp", sensorData, kotlinx.serialization.serializers.MapSerializer(
            kotlinx.serialization.serializers.StringSerializer(),
            kotlinx.serialization.serializers.StringSerializer()
        ))
    }
    
    /**
     * 清理旧的健康数据
     */
    suspend fun cleanOldHealthData(olderThanDays: Int = 30) {
        val cutoffTime = System.currentTimeMillis() - (olderThanDays * 24 * 60 * 60 * 1000L)
        val oldKeys = healthStorage.keys.filter { key ->
            val timestamp = extractTimestampFromKey(key)
            timestamp < cutoffTime
        }
        
        oldKeys.forEach { key ->
            delete(key)
        }
    }
    
    // 私有辅助方法
    
    private suspend fun saveToWatchHealthStorage(key: String, value: String) {
        // 模拟Watch健康存储API
    }
    
    private suspend fun saveToWatchSensorStorage(key: String, value: String) {
        // 模拟Watch传感器存储API
    }
    
    private suspend fun saveToWatchWorkoutStorage(key: String, value: String) {
        // 模拟Watch运动存储API
    }
    
    private suspend fun saveToWatchGeneralStorage(key: String, value: String) {
        // 模拟Watch通用存储API
    }
    
    private suspend fun loadFromWatchHealthStorage(key: String): String? {
        return healthStorage[key]
    }
    
    private suspend fun loadFromWatchSensorStorage(key: String): String? {
        return sensorStorage[key]
    }
    
    private suspend fun loadFromWatchStorage(key: String): String? {
        return localStorage[key]
    }
    
    private suspend fun removeFromWatchHealthStorage(key: String) {
        // 模拟Watch健康存储删除API
    }
    
    private suspend fun removeFromWatchSensorStorage(key: String) {
        // 模拟Watch传感器存储删除API
    }
    
    private suspend fun removeFromWatchStorage(key: String) {
        // 模拟Watch存储删除API
    }
    
    private suspend fun clearWatchStorage() {
        // 模拟Watch存储清空API
    }
    
    private fun getCurrentStorageSize(): Int {
        return localStorage.values.sumOf { it.toByteArray().size } +
               healthStorage.values.sumOf { it.toByteArray().size } +
               sensorStorage.values.sumOf { it.toByteArray().size }
    }
    
    private suspend fun cleanOldData() {
        // 清理7天前的传感器数据
        val cutoffTime = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        val oldSensorKeys = sensorStorage.keys.filter { key ->
            val timestamp = extractTimestampFromKey(key)
            timestamp < cutoffTime
        }
        
        oldSensorKeys.forEach { key ->
            sensorStorage.remove(key)
        }
    }
    
    private suspend fun syncHealthDataToPhone() {
        // 同步健康数据到手机应用
    }
    
    private suspend fun syncSensorData() {
        // 同步传感器数据
    }
    
    private suspend fun isConnectedToPhone(): Boolean {
        return true // 模拟连接检查
    }
    
    private suspend fun syncToPhoneApp() {
        // 同步到手机应用
    }
    
    private suspend fun storeToPhoneApp(backupId: String, data: String) {
        // 存储到手机应用
    }
    
    private suspend fun loadFromPhoneApp(backupId: String): String {
        return "{\"local\":{},\"health\":{},\"sensor\":{},\"timestamp\":\"${System.currentTimeMillis()}\",\"platform\":\"Watch\"}"
    }
    
    private fun calculateCacheSize(): Long {
        return getCurrentStorageSize().toLong()
    }
    
    private fun extractTimestampFromKey(key: String): Long {
        return try {
            key.substringAfterLast("_").toLong()
        } catch (e: Exception) {
            0L
        }
    }
}

/**
 * Watch平台数据管理器工厂
 */
actual fun createUnifyDataManager(): UnifyDataManager {
    return WatchUnifyDataManager()
}
