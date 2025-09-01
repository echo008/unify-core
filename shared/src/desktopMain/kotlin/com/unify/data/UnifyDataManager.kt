package com.unify.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.concurrent.ConcurrentHashMap
import java.util.prefs.Preferences
import kotlin.reflect.KClass

/**
 * Desktop平台数据管理器实现
 */
class DesktopUnifyDataManager(
    private val config: UnifyDataManagerConfig
) : UnifyDataManager {
    
    override val storage: UnifyStorage = DesktopUnifyStorage(config)
    override val state: UnifyStateManager = DesktopUnifyStateManager()
    override val cache: UnifyCacheManager = DesktopUnifyCacheManager(config.cachePolicy)
    override val sync: UnifyDataSync = DesktopUnifyDataSync(config.syncPolicy)
    
    override suspend fun initialize() {
        (storage as DesktopUnifyStorage).initialize()
        (cache as DesktopUnifyCacheManager).initialize()
        (sync as DesktopUnifyDataSync).initialize()
    }
    
    override suspend fun cleanup() {
        (cache as DesktopUnifyCacheManager).cleanup()
        (sync as DesktopUnifyDataSync).cleanup()
    }
}

/**
 * Desktop存储实现 - 基于Java Preferences和文件系统
 */
class DesktopUnifyStorage(
    private val config: UnifyDataManagerConfig
) : UnifyStorage {
    
    private val preferences = Preferences.userNodeForPackage(this::class.java)
    private val dataDir: Path
    private val json = Json { 
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    init {
        // 获取用户数据目录
        val userHome = System.getProperty("user.home")
        val appDataDir = when {
            System.getProperty("os.name").lowercase().contains("windows") -> 
                Paths.get(userHome, "AppData", "Local", "UnifyCore")
            System.getProperty("os.name").lowercase().contains("mac") -> 
                Paths.get(userHome, "Library", "Application Support", "UnifyCore")
            else -> 
                Paths.get(userHome, ".unifycore")
        }
        
        dataDir = appDataDir.resolve("data")
    }
    
    suspend fun initialize() {
        // 创建数据目录
        Files.createDirectories(dataDir)
        
        // 检查存储配额
        checkStorageQuota()
        
        // 清理过期文件
        cleanExpiredFiles()
    }
    
    override suspend fun <T> put(key: String, value: T) {
        try {
            val jsonString = json.encodeToString(value)
            
            if (config.storageEncryption) {
                // 加密存储到文件
                val encryptedData = encryptData(jsonString)
                val filePath = dataDir.resolve("$key.enc")
                Files.write(filePath, encryptedData.toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
                
                // 在Preferences中记录文件路径
                preferences.put(key, "file:${filePath.toAbsolutePath()}")
            } else {
                // 小数据存储到Preferences
                if (jsonString.length < 8192) { // 8KB以下
                    preferences.put(key, jsonString)
                } else {
                    // 大数据存储到文件
                    val filePath = dataDir.resolve("$key.json")
                    Files.write(filePath, jsonString.toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
                    preferences.put(key, "file:${filePath.toAbsolutePath()}")
                }
            }
            
            // 存储元数据
            val metadata = DesktopStorageMetadata(
                timestamp = System.currentTimeMillis(),
                size = jsonString.length.toLong(),
                encrypted = config.storageEncryption,
                isFile = jsonString.length >= 8192 || config.storageEncryption
            )
            preferences.put("${key}_meta", json.encodeToString(metadata))
            
        } catch (e: Exception) {
            throw RuntimeException("Failed to store data for key: $key", e)
        }
    }
    
    override suspend fun <T> get(key: String, type: KClass<T>): T? {
        return try {
            val storedValue = preferences.get(key, null) ?: return null
            
            val jsonString = if (storedValue.startsWith("file:")) {
                // 从文件读取
                val filePath = Paths.get(storedValue.removePrefix("file:"))
                if (Files.exists(filePath)) {
                    val content = Files.readAllBytes(filePath).toString(Charsets.UTF_8)
                    if (config.storageEncryption) {
                        decryptData(content)
                    } else {
                        content
                    }
                } else {
                    return null
                }
            } else {
                storedValue
            }
            
            json.decodeFromString(type.java, jsonString) as T
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun remove(key: String): Boolean {
        return try {
            val storedValue = preferences.get(key, null)
            if (storedValue?.startsWith("file:") == true) {
                // 删除文件
                val filePath = Paths.get(storedValue.removePrefix("file:"))
                Files.deleteIfExists(filePath)
            }
            
            preferences.remove(key)
            preferences.remove("${key}_meta")
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun clear() {
        try {
            // 清理所有文件
            if (Files.exists(dataDir)) {
                Files.walk(dataDir).use { paths ->
                    paths.filter { Files.isRegularFile(it) }
                        .forEach { Files.deleteIfExists(it) }
                }
            }
            
            // 清理Preferences
            preferences.keys().forEach { key ->
                preferences.remove(key)
            }
        } catch (e: Exception) {
            // 忽略清理错误
        }
    }
    
    override suspend fun contains(key: String): Boolean {
        return preferences.get(key, null) != null
    }
    
    override suspend fun keys(): Set<String> {
        return try {
            preferences.keys()
                .filter { !it.endsWith("_meta") }
                .toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }
    
    override suspend fun size(): Long {
        return try {
            var totalSize = 0L
            
            // 计算Preferences大小
            preferences.keys().forEach { key ->
                val value = preferences.get(key, "")
                totalSize += (key.length + value.length) * 2 // UTF-16编码
            }
            
            // 计算文件大小
            if (Files.exists(dataDir)) {
                Files.walk(dataDir).use { paths ->
                    totalSize += paths.filter { Files.isRegularFile(it) }
                        .mapToLong { Files.size(it) }
                        .sum()
                }
            }
            
            totalSize
        } catch (e: Exception) {
            0L
        }
    }
    
    private fun checkStorageQuota() {
        // 检查磁盘空间
        val freeSpace = dataDir.toFile().freeSpace
        if (freeSpace < config.maxStorageSize) {
            // 清理旧数据
            cleanOldData()
        }
    }
    
    private suspend fun cleanExpiredFiles() {
        // 清理7天前的文件
        val cutoffTime = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
        
        if (Files.exists(dataDir)) {
            Files.walk(dataDir).use { paths ->
                paths.filter { Files.isRegularFile(it) }
                    .filter { Files.getLastModifiedTime(it).toMillis() < cutoffTime }
                    .forEach { 
                        Files.deleteIfExists(it)
                        // 同时清理对应的Preferences条目
                        val fileName = it.fileName.toString()
                        val key = fileName.removeSuffix(".json").removeSuffix(".enc")
                        preferences.remove(key)
                        preferences.remove("${key}_meta")
                    }
            }
        }
    }
    
    private fun cleanOldData() {
        // 清理最旧的数据以释放空间
        val dataWithTimestamp = mutableListOf<Pair<String, Long>>()
        
        preferences.keys()
            .filter { it.endsWith("_meta") }
            .forEach { metaKey ->
                try {
                    val metaJson = preferences.get(metaKey, null)
                    if (metaJson != null) {
                        val metadata = json.decodeFromString<DesktopStorageMetadata>(metaJson)
                        val dataKey = metaKey.removeSuffix("_meta")
                        dataWithTimestamp.add(dataKey to metadata.timestamp)
                    }
                } catch (e: Exception) {
                    // 忽略解析错误
                }
            }
        
        // 删除最旧的20%数据
        val sortedData = dataWithTimestamp.sortedBy { it.second }
        val deleteCount = (sortedData.size * 0.2).toInt()
        
        sortedData.take(deleteCount).forEach { (key, _) ->
            runCatching { remove(key) }
        }
    }
    
    private fun encryptData(data: String): String {
        // Desktop平台加密实现 - 可以使用Java Crypto API
        // 这里返回原数据作为占位符
        return data
    }
    
    private fun decryptData(data: String): String {
        // Desktop平台解密实现
        return data
    }
}

/**
 * Desktop状态管理实现
 */
class DesktopUnifyStateManager : UnifyStateManager {
    
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
 * Desktop缓存管理实现 - 基于内存和临时文件
 */
class DesktopUnifyCacheManager(
    private var policy: UnifyCachePolicy
) : UnifyCacheManager {
    
    private val cache = ConcurrentHashMap<String, DesktopCacheEntry>()
    private val stats = DesktopCacheStats()
    private val tempDir: Path
    
    init {
        tempDir = Paths.get(System.getProperty("java.io.tmpdir"), "unifycore_cache")
    }
    
    suspend fun initialize() {
        // 创建临时缓存目录
        Files.createDirectories(tempDir)
        cleanExpiredCache()
    }
    
    suspend fun cleanup() {
        // 清理临时文件
        if (Files.exists(tempDir)) {
            Files.walk(tempDir).use { paths ->
                paths.filter { Files.isRegularFile(it) }
                    .forEach { Files.deleteIfExists(it) }
            }
        }
        cache.clear()
    }
    
    override suspend fun <T> cache(key: String, value: T, ttl: Long) {
        val actualTtl = if (ttl > 0) ttl else policy.defaultTtl
        val expireTime = if (actualTtl > 0) {
            System.currentTimeMillis() + actualTtl
        } else {
            0L // 永不过期
        }
        
        val entry = DesktopCacheEntry(
            value = value,
            expireTime = expireTime,
            accessTime = System.currentTimeMillis(),
            accessCount = 1
        )
        
        cache[key] = entry
        
        // 检查缓存大小限制
        if (cache.size * 1024 > policy.maxSize) {
            evictCache()
        }
    }
    
    override suspend fun <T> getCache(key: String, type: KClass<T>): T? {
        val entry = cache[key] ?: run {
            stats.missCount++
            return null
        }
        
        // 检查是否过期
        if (entry.expireTime > 0 && System.currentTimeMillis() > entry.expireTime) {
            cache.remove(key)
            stats.missCount++
            return null
        }
        
        // 更新访问信息
        entry.accessTime = System.currentTimeMillis()
        entry.accessCount++
        
        stats.hitCount++
        return entry.value as? T
    }
    
    override suspend fun removeCache(key: String): Boolean {
        return cache.remove(key) != null
    }
    
    override suspend fun clearCache() {
        cache.clear()
        stats.reset()
    }
    
    override suspend fun isCacheValid(key: String): Boolean {
        val entry = cache[key] ?: return false
        return entry.expireTime == 0L || System.currentTimeMillis() <= entry.expireTime
    }
    
    override suspend fun getCacheStats(): UnifyCacheStats {
        return UnifyCacheStats(
            hitCount = stats.hitCount,
            missCount = stats.missCount,
            evictionCount = stats.evictionCount,
            totalSize = cache.size.toLong() * 1024, // 估算
            maxSize = policy.maxSize,
            hitRate = if (stats.hitCount + stats.missCount > 0) {
                stats.hitCount.toDouble() / (stats.hitCount + stats.missCount)
            } else 0.0
        )
    }
    
    override fun setCachePolicy(policy: UnifyCachePolicy) {
        this.policy = policy
    }
    
    private suspend fun cleanExpiredCache() {
        val currentTime = System.currentTimeMillis()
        val expiredKeys = cache.entries
            .filter { it.value.expireTime > 0 && currentTime > it.value.expireTime }
            .map { it.key }
        
        expiredKeys.forEach { cache.remove(it) }
    }
    
    private fun evictCache() {
        when (policy.evictionPolicy) {
            UnifyCacheEvictionPolicy.LRU -> evictLRU()
            UnifyCacheEvictionPolicy.LFU -> evictLFU()
            UnifyCacheEvictionPolicy.FIFO -> evictFIFO()
            UnifyCacheEvictionPolicy.RANDOM -> evictRandom()
        }
    }
    
    private fun evictLRU() {
        val lruEntry = cache.entries.minByOrNull { it.value.accessTime }
        lruEntry?.let { 
            cache.remove(it.key)
            stats.evictionCount++
        }
    }
    
    private fun evictLFU() {
        val lfuEntry = cache.entries.minByOrNull { it.value.accessCount }
        lfuEntry?.let { 
            cache.remove(it.key)
            stats.evictionCount++
        }
    }
    
    private fun evictFIFO() {
        val firstEntry = cache.entries.firstOrNull()
        firstEntry?.let { 
            cache.remove(it.key)
            stats.evictionCount++
        }
    }
    
    private fun evictRandom() {
        val randomEntry = cache.entries.randomOrNull()
        randomEntry?.let { 
            cache.remove(it.key)
            stats.evictionCount++
        }
    }
}

/**
 * Desktop数据同步实现
 */
class DesktopUnifyDataSync(
    private var policy: UnifySyncPolicy
) : UnifyDataSync {
    
    private val syncStatus = MutableStateFlow(
        UnifySyncStatus(
            isOnline = checkNetworkConnection(),
            isSyncing = false,
            lastSyncTime = 0L,
            pendingSyncCount = 0,
            failedSyncCount = 0
        )
    )
    
    suspend fun initialize() {
        // 初始化网络状态监听
        startNetworkMonitoring()
    }
    
    suspend fun cleanup() {
        // 清理资源
    }
    
    override suspend fun syncToRemote(key: String): UnifySyncResult {
        // Desktop平台同步到远程实现
        return try {
            // 这里可以使用Java HTTP客户端进行网络请求
            UnifySyncResult(
                key = key,
                success = true,
                timestamp = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            UnifySyncResult(
                key = key,
                success = false,
                timestamp = System.currentTimeMillis(),
                error = e.message
            )
        }
    }
    
    override suspend fun syncFromRemote(key: String): UnifySyncResult {
        return UnifySyncResult(
            key = key,
            success = true,
            timestamp = System.currentTimeMillis()
        )
    }
    
    override suspend fun bidirectionalSync(key: String): UnifySyncResult {
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
    
    private fun checkNetworkConnection(): Boolean {
        return try {
            // 简单的网络连接检查
            val process = ProcessBuilder("ping", "-c", "1", "8.8.8.8")
                .start()
            process.waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }
    
    private fun startNetworkMonitoring() {
        // 启动网络状态监听
        // 可以使用定时器定期检查网络状态
    }
}

/**
 * Desktop存储元数据
 */
@kotlinx.serialization.Serializable
private data class DesktopStorageMetadata(
    val timestamp: Long,
    val size: Long,
    val encrypted: Boolean,
    val isFile: Boolean
)

/**
 * Desktop缓存条目
 */
private data class DesktopCacheEntry(
    val value: Any?,
    var expireTime: Long,
    var accessTime: Long,
    var accessCount: Long
)

/**
 * Desktop缓存统计
 */
private class DesktopCacheStats {
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
 * Desktop数据管理器工厂实现
 */
actual object UnifyDataManagerFactory {
    actual fun create(config: UnifyDataManagerConfig): UnifyDataManager {
        return DesktopUnifyDataManager(config)
    }
}
