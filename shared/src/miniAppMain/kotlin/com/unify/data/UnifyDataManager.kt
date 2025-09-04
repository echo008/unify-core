package com.unify.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 小程序平台数据管理器实现
 * 基于小程序存储API实现数据持久化
 */
class MiniAppUnifyDataManager : UnifyDataManager {
    
    private val localStorage = mutableMapOf<String, String>()
    private val sessionStorage = mutableMapOf<String, String>()
    private val _connectionState = MutableStateFlow(DataConnectionState.CONNECTED)
    
    companion object {
        private const val MAX_STORAGE_SIZE = 10 * 1024 * 1024 // 10MB 小程序存储限制
        private const val SESSION_PREFIX = "session_"
    }
    
    override suspend fun <T> save(key: String, value: T, serializer: kotlinx.serialization.KSerializer<T>) {
        try {
            val jsonString = Json.encodeToString(serializer, value)
            
            // 检查存储大小限制
            if (getCurrentStorageSize() + jsonString.length > MAX_STORAGE_SIZE) {
                throw DataException("Storage quota exceeded for key: $key")
            }
            
            when {
                key.startsWith(SESSION_PREFIX) -> {
                    // 会话存储
                    sessionStorage[key] = jsonString
                }
                else -> {
                    // 持久化存储
                    localStorage[key] = jsonString
                    // 模拟小程序存储API
                    saveToMiniAppStorage(key, jsonString)
                }
            }
        } catch (e: Exception) {
            throw DataException("Failed to save data for key: $key", e)
        }
    }
    
    override suspend fun <T> load(key: String, serializer: kotlinx.serialization.KSerializer<T>): T? {
        return try {
            val jsonString = when {
                key.startsWith(SESSION_PREFIX) -> sessionStorage[key]
                else -> {
                    // 从小程序存储加载
                    loadFromMiniAppStorage(key) ?: localStorage[key]
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
                key.startsWith(SESSION_PREFIX) -> {
                    sessionStorage.remove(key)
                }
                else -> {
                    localStorage.remove(key)
                    removeFromMiniAppStorage(key)
                }
            }
        } catch (e: Exception) {
            throw DataException("Failed to delete data for key: $key", e)
        }
    }
    
    override suspend fun clear() {
        try {
            localStorage.clear()
            sessionStorage.clear()
            clearMiniAppStorage()
        } catch (e: Exception) {
            throw DataException("Failed to clear all data", e)
        }
    }
    
    override suspend fun exists(key: String): Boolean {
        return when {
            key.startsWith(SESSION_PREFIX) -> sessionStorage.containsKey(key)
            else -> localStorage.containsKey(key) || existsInMiniAppStorage(key)
        }
    }
    
    override suspend fun getAllKeys(): Set<String> {
        val localKeys = localStorage.keys
        val sessionKeys = sessionStorage.keys
        val miniAppKeys = getMiniAppStorageKeys()
        return localKeys + sessionKeys + miniAppKeys
    }
    
    override fun observeConnectionState(): Flow<DataConnectionState> {
        return _connectionState.asStateFlow()
    }
    
    override suspend fun sync() {
        try {
            _connectionState.value = DataConnectionState.SYNCING
            
            // 同步本地存储到小程序存储
            localStorage.forEach { (key, value) ->
                saveToMiniAppStorage(key, value)
            }
            
            // 检查网络状态
            if (isNetworkAvailable()) {
                // 同步到云端
                syncToCloud()
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
                "session" to sessionStorage,
                "timestamp" to System.currentTimeMillis().toString(),
                "platform" to "MiniApp",
                "miniAppInfo" to getMiniAppInfo()
            )
            
            val backupJson = Json.encodeToString(backupData)
            val backupId = "miniapp_backup_${System.currentTimeMillis()}"
            
            // 存储备份到小程序云开发
            storeToMiniAppCloud(backupId, backupJson)
            
            backupId
        } catch (e: Exception) {
            throw DataException("Failed to create backup", e)
        }
    }
    
    override suspend fun restore(backupPath: String) {
        try {
            val backupJson = loadFromMiniAppCloud(backupPath)
            val backupData = Json.decodeFromString<Map<String, Map<String, String>>>(backupJson)
            
            // 恢复本地数据
            backupData["local"]?.let { localData ->
                localStorage.clear()
                localStorage.putAll(localData)
                // 同步到小程序存储
                localData.forEach { (key, value) ->
                    saveToMiniAppStorage(key, value)
                }
            }
            
            // 恢复会话数据
            backupData["session"]?.let { sessionData ->
                sessionStorage.clear()
                sessionStorage.putAll(sessionData)
            }
        } catch (e: Exception) {
            throw DataException("Failed to restore from backup: $backupPath", e)
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
    
    // 小程序特有功能
    
    /**
     * 获取小程序信息
     */
    suspend fun getMiniAppInfo(): Map<String, String> {
        return mapOf(
            "appId" to "wx1234567890abcdef",
            "version" to "1.0.0",
            "platform" to detectMiniAppPlatform(),
            "scene" to "1001", // 启动场景
            "path" to "/pages/index/index"
        )
    }
    
    /**
     * 检测小程序平台
     */
    private fun detectMiniAppPlatform(): String {
        // 模拟检测逻辑
        return when {
            // 检测微信小程序
            checkWeChatMiniApp() -> "WeChat"
            // 检测支付宝小程序
            checkAlipayMiniApp() -> "Alipay"
            // 检测字节跳动小程序
            checkByteDanceMiniApp() -> "ByteDance"
            // 检测百度小程序
            checkBaiduMiniApp() -> "Baidu"
            else -> "Unknown"
        }
    }
    
    /**
     * 用户授权数据存储
     */
    suspend fun saveUserAuthData(userData: Map<String, Any>) {
        try {
            val userDataJson = Json.encodeToString(userData)
            save("user_auth_data", userDataJson, kotlinx.serialization.serializers.StringSerializer())
        } catch (e: Exception) {
            throw DataException("Failed to save user auth data", e)
        }
    }
    
    /**
     * 获取用户授权数据
     */
    suspend fun getUserAuthData(): Map<String, Any>? {
        return try {
            val userDataJson = load("user_auth_data", kotlinx.serialization.serializers.StringSerializer())
            userDataJson?.let { Json.decodeFromString<Map<String, Any>>(it) }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 清理过期的会话数据
     */
    suspend fun cleanExpiredSessionData() {
        val currentTime = System.currentTimeMillis()
        val expiredKeys = sessionStorage.keys.filter { key ->
            // 检查是否过期（示例：1小时过期）
            val saveTime = extractTimestampFromKey(key)
            currentTime - saveTime > 3600000 // 1小时
        }
        
        expiredKeys.forEach { key ->
            sessionStorage.remove(key)
        }
    }
    
    // 私有辅助方法
    
    private suspend fun saveToMiniAppStorage(key: String, value: String) {
        // 模拟小程序存储API调用
        // wx.setStorageSync(key, value) 或类似API
    }
    
    private suspend fun loadFromMiniAppStorage(key: String): String? {
        // 模拟小程序存储API调用
        // return wx.getStorageSync(key) 或类似API
        return localStorage[key]
    }
    
    private suspend fun removeFromMiniAppStorage(key: String) {
        // 模拟小程序存储API调用
        // wx.removeStorageSync(key) 或类似API
    }
    
    private suspend fun clearMiniAppStorage() {
        // 模拟小程序存储API调用
        // wx.clearStorageSync() 或类似API
    }
    
    private suspend fun existsInMiniAppStorage(key: String): Boolean {
        // 模拟检查小程序存储
        return localStorage.containsKey(key)
    }
    
    private suspend fun getMiniAppStorageKeys(): Set<String> {
        // 模拟获取小程序存储键列表
        return localStorage.keys
    }
    
    private fun getCurrentStorageSize(): Int {
        return localStorage.values.sumOf { it.toByteArray().size } +
               sessionStorage.values.sumOf { it.toByteArray().size }
    }
    
    private fun calculateCacheSize(): Long {
        return getCurrentStorageSize().toLong()
    }
    
    private suspend fun isNetworkAvailable(): Boolean {
        // 模拟网络检查
        return true
    }
    
    private suspend fun syncToCloud() {
        // 模拟云端同步
    }
    
    private suspend fun storeToMiniAppCloud(backupId: String, data: String) {
        // 模拟存储到小程序云开发
    }
    
    private suspend fun loadFromMiniAppCloud(backupId: String): String {
        // 模拟从小程序云开发加载
        return "{\"local\":{},\"session\":{},\"timestamp\":\"${System.currentTimeMillis()}\",\"platform\":\"MiniApp\"}"
    }
    
    private fun checkWeChatMiniApp(): Boolean = false
    private fun checkAlipayMiniApp(): Boolean = false
    private fun checkByteDanceMiniApp(): Boolean = false
    private fun checkBaiduMiniApp(): Boolean = false
    
    private fun extractTimestampFromKey(key: String): Long {
        // 从key中提取时间戳
        return try {
            key.substringAfterLast("_").toLong()
        } catch (e: Exception) {
            0L
        }
    }
}

/**
 * 小程序平台数据管理器工厂
 */
actual fun createUnifyDataManager(): UnifyDataManager {
    return MiniAppUnifyDataManager()
}
