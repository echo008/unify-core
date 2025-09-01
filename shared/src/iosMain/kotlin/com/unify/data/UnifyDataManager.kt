package com.unify.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.Foundation.*
import platform.darwin.NSObject
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

/**
 * iOS平台数据管理器实现
 */
class IOSUnifyDataManager(
    private val config: UnifyDataManagerConfig
) : UnifyDataManager {
    
    override val storage: UnifyStorage = IOSUnifyStorage(config)
    override val state: UnifyStateManager = IOSUnifyStateManager()
    override val cache: UnifyCacheManager = IOSUnifyCacheManager(config.cachePolicy)
    override val sync: UnifyDataSync = IOSUnifyDataSync(config.syncPolicy)
    
    override suspend fun initialize() {
        (storage as IOSUnifyStorage).initialize()
        (cache as IOSUnifyCacheManager).initialize()
        (sync as IOSUnifyDataSync).initialize()
    }
    
    override suspend fun cleanup() {
        (cache as IOSUnifyCacheManager).cleanup()
        (sync as IOSUnifyDataSync).cleanup()
    }
}

/**
 * iOS存储实现 - 基于NSUserDefaults和Keychain
 */
class IOSUnifyStorage(
    private val config: UnifyDataManagerConfig
) : UnifyStorage {
    
    private val userDefaults = NSUserDefaults.standardUserDefaults
    private val json = Json { 
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    suspend fun initialize() {
        // 检查存储配额
        checkStorageQuota()
    }
    
    override suspend fun <T> put(key: String, value: T) {
        try {
            val jsonString = json.encodeToString(value)
            val finalData = if (config.storageEncryption) {
                storeInKeychain(key, jsonString)
                "keychain:$key"
            } else {
                jsonString
            }
            
            userDefaults.setObject(finalData, key)
            userDefaults.synchronize()
        } catch (e: Exception) {
            throw RuntimeException("Failed to store data for key: $key", e)
        }
    }
    
    override suspend fun <T> get(key: String, type: KClass<T>): T? {
        return try {
            val storedData = userDefaults.objectForKey(key) as? String ?: return null
            
            val jsonString = if (storedData.startsWith("keychain:")) {
                val keychainKey = storedData.removePrefix("keychain:")
                getFromKeychain(keychainKey) ?: return null
            } else {
                storedData
            }
            
            json.decodeFromString(type.java, jsonString) as T
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun remove(key: String): Boolean {
        return try {
            val storedData = userDefaults.objectForKey(key) as? String
            if (storedData?.startsWith("keychain:") == true) {
                val keychainKey = storedData.removePrefix("keychain:")
                removeFromKeychain(keychainKey)
            }
            
            userDefaults.removeObjectForKey(key)
            userDefaults.synchronize()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun clear() {
        try {
            val domain = NSBundle.mainBundle.bundleIdentifier
            if (domain != null) {
                userDefaults.removePersistentDomainForName(domain)
                userDefaults.synchronize()
            }
            
            // 清理Keychain中的数据
            clearKeychain()
        } catch (e: Exception) {
            // 忽略清理错误
        }
    }
    
    override suspend fun contains(key: String): Boolean {
        return userDefaults.objectForKey(key) != null
    }
    
    override suspend fun keys(): Set<String> {
        return try {
            val domain = NSBundle.mainBundle.bundleIdentifier
            if (domain != null) {
                val dict = userDefaults.persistentDomainForName(domain)
                dict?.allKeys?.mapNotNull { it as? String }?.toSet() ?: emptySet()
            } else {
                emptySet()
            }
        } catch (e: Exception) {
            emptySet()
        }
    }
    
    override suspend fun size(): Long {
        return try {
            // 估算UserDefaults大小
            val domain = NSBundle.mainBundle.bundleIdentifier
            if (domain != null) {
                val dict = userDefaults.persistentDomainForName(domain)
                // 简单估算：每个键值对约100字节
                (dict?.count ?: 0) * 100L
            } else {
                0L
            }
        } catch (e: Exception) {
            0L
        }
    }
    
    private fun checkStorageQuota() {
        // iOS存储配额检查
        // 可以通过NSFileManager检查可用空间
    }
    
    private fun storeInKeychain(key: String, value: String): Boolean {
        // iOS Keychain存储实现
        // 这里是占位符实现，实际需要使用Security framework
        return true
    }
    
    private fun getFromKeychain(key: String): String? {
        // iOS Keychain读取实现
        // 这里是占位符实现
        return null
    }
    
    private fun removeFromKeychain(key: String): Boolean {
        // iOS Keychain删除实现
        return true
    }
    
    private fun clearKeychain() {
        // 清理Keychain数据
    }
}

/**
 * iOS状态管理实现
 */
class IOSUnifyStateManager : UnifyStateManager {
    
    private val states = ConcurrentHashMap<String, Any?>()
    private val stateFlows = ConcurrentHashMap<String, MutableStateFlow<Any?>>()
    
    override fun <T> setState(key: String, value: T) {
        states[key] = value
        
        val flow = stateFlows.getOrPut(key) { MutableStateFlow(null) }
        flow.value = value
    }
    
    override fun <T> getState(key: String, type: KClass<T>): T? {
        return states[key] as? T
    }
    
    override fun <T> observeState(key: String, type: KClass<T>): Flow<T?> {
        val flow = stateFlows.getOrPut(key) { MutableStateFlow(states[key]) }
        return flow.map { it as? T }
    }
    
    override fun removeState(key: String) {
        states.remove(key)
        stateFlows[key]?.value = null
    }
    
    override fun clearStates() {
        states.clear()
        stateFlows.values.forEach { it.value = null }
        stateFlows.clear()
    }
    
    override fun getStateKeys(): Set<String> {
        return states.keys.toSet()
    }
}

/**
 * iOS缓存管理实现 - 基于NSCache
 */
class IOSUnifyCacheManager(
    private var policy: UnifyCachePolicy
) : UnifyCacheManager {
    
    private val cache = NSCache()
    private val cacheMetadata = ConcurrentHashMap<String, IOSCacheMetadata>()
    private val stats = IOSCacheStats()
    
    init {
        // 配置NSCache
        cache.countLimit = (policy.maxSize / 1024).toInt() // 转换为条目数限制
        cache.totalCostLimit = policy.maxSize.toInt()
    }
    
    suspend fun initialize() {
        cleanExpiredCache()
    }
    
    suspend fun cleanup() {
        cache.removeAllObjects()
        cacheMetadata.clear()
    }
    
    override suspend fun <T> cache(key: String, value: T, ttl: Long) {
        val actualTtl = if (ttl > 0) ttl else policy.defaultTtl
        val expireTime = if (actualTtl > 0) {
            NSDate.timeIntervalSinceReferenceDate() + (actualTtl / 1000.0)
        } else {
            0.0 // 永不过期
        }
        
        val metadata = IOSCacheMetadata(
            expireTime = expireTime,
            accessTime = NSDate.timeIntervalSinceReferenceDate(),
            accessCount = 1
        )
        
        cacheMetadata[key] = metadata
        cache.setObject(value as NSObject, key, 1024) // 估算每个对象1KB
    }
    
    override suspend fun <T> getCache(key: String, type: KClass<T>): T? {
        val metadata = cacheMetadata[key] ?: run {
            stats.missCount++
            return null
        }
        
        // 检查是否过期
        val currentTime = NSDate.timeIntervalSinceReferenceDate()
        if (metadata.expireTime > 0 && currentTime > metadata.expireTime) {
            cache.removeObjectForKey(key)
            cacheMetadata.remove(key)
            stats.missCount++
            return null
        }
        
        val cachedObject = cache.objectForKey(key)
        if (cachedObject == null) {
            cacheMetadata.remove(key)
            stats.missCount++
            return null
        }
        
        // 更新访问信息
        metadata.accessTime = currentTime
        metadata.accessCount++
        
        stats.hitCount++
        return cachedObject as? T
    }
    
    override suspend fun removeCache(key: String): Boolean {
        cache.removeObjectForKey(key)
        return cacheMetadata.remove(key) != null
    }
    
    override suspend fun clearCache() {
        cache.removeAllObjects()
        cacheMetadata.clear()
        stats.reset()
    }
    
    override suspend fun isCacheValid(key: String): Boolean {
        val metadata = cacheMetadata[key] ?: return false
        val currentTime = NSDate.timeIntervalSinceReferenceDate()
        return metadata.expireTime == 0.0 || currentTime <= metadata.expireTime
    }
    
    override suspend fun getCacheStats(): UnifyCacheStats {
        return UnifyCacheStats(
            hitCount = stats.hitCount,
            missCount = stats.missCount,
            evictionCount = stats.evictionCount,
            totalSize = cacheMetadata.size.toLong() * 1024, // 估算
            maxSize = policy.maxSize,
            hitRate = if (stats.hitCount + stats.missCount > 0) {
                stats.hitCount.toDouble() / (stats.hitCount + stats.missCount)
            } else 0.0
        )
    }
    
    override fun setCachePolicy(policy: UnifyCachePolicy) {
        this.policy = policy
        cache.countLimit = (policy.maxSize / 1024).toInt()
        cache.totalCostLimit = policy.maxSize.toInt()
    }
    
    private suspend fun cleanExpiredCache() {
        val currentTime = NSDate.timeIntervalSinceReferenceDate()
        val expiredKeys = cacheMetadata.entries
            .filter { it.value.expireTime > 0 && currentTime > it.value.expireTime }
            .map { it.key }
        
        expiredKeys.forEach { 
            cache.removeObjectForKey(it)
            cacheMetadata.remove(it)
        }
    }
}

/**
 * iOS数据同步实现
 */
class IOSUnifyDataSync(
    private var policy: UnifySyncPolicy
) : UnifyDataSync {
    
    private val syncStatus = MutableStateFlow(
        UnifySyncStatus(
            isOnline = checkNetworkStatus(),
            isSyncing = false,
            lastSyncTime = 0L,
            pendingSyncCount = 0,
            failedSyncCount = 0
        )
    )
    
    suspend fun initialize() {
        // 初始化网络状态监听
        setupNetworkMonitoring()
    }
    
    suspend fun cleanup() {
        // 清理资源
    }
    
    override suspend fun syncToRemote(key: String): UnifySyncResult {
        // iOS平台同步到远程实现
        return UnifySyncResult(
            key = key,
            success = true,
            timestamp = System.currentTimeMillis()
        )
    }
    
    override suspend fun syncFromRemote(key: String): UnifySyncResult {
        // iOS平台从远程同步实现
        return UnifySyncResult(
            key = key,
            success = true,
            timestamp = System.currentTimeMillis()
        )
    }
    
    override suspend fun bidirectionalSync(key: String): UnifySyncResult {
        // iOS平台双向同步实现
        return UnifySyncResult(
            key = key,
            success = true,
            timestamp = System.currentTimeMillis()
        )
    }
    
    override suspend fun batchSync(keys: List<String>): List<UnifySyncResult> {
        return keys.map { bidirectionalSync(it) }
    }
    
    override fun setSyncPolicy(policy: UnifySyncPolicy) {
        this.policy = policy
    }
    
    override fun observeSyncStatus(): Flow<UnifySyncStatus> {
        return syncStatus
    }
    
    private fun checkNetworkStatus(): Boolean {
        // 检查iOS网络状态
        // 这里需要使用SystemConfiguration framework
        return true // 占位符
    }
    
    private fun setupNetworkMonitoring() {
        // 设置iOS网络状态监听
        // 使用Network framework或SystemConfiguration
    }
}

/**
 * iOS缓存元数据
 */
private data class IOSCacheMetadata(
    var expireTime: Double,
    var accessTime: Double,
    var accessCount: Long
)

/**
 * iOS缓存统计
 */
private class IOSCacheStats {
    var hitCount: Long = 0
    var missCount: Long = 0
    var evictionCount: Long = 0
    
    fun reset() {
        hitCount = 0
        missCount = 0
        evictionCount = 0
    }
}

/**
 * iOS数据管理器工厂实现
 */
actual object UnifyDataManagerFactory {
    actual fun create(config: UnifyDataManagerConfig): UnifyDataManager {
        return IOSUnifyDataManager(config)
    }
}
