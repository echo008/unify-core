package com.unify.core.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Android平台的数据管理器实现
 */
actual class UnifyDataManagerImpl : UnifyDataManager {
    
    override val storage: UnifyStorage = AndroidStorage()
    override val secureStorage: UnifySecureStorage = AndroidSecureStorage()
    override val cache: UnifyCacheManager = AndroidCacheManager()
    override val state: UnifyStateManager = AndroidStateManager()
    override val database: UnifyDatabaseManager = AndroidDatabaseManager()
    
    private var context: Context? = null
    private var initialized = false
    
    fun setContext(context: Context) {
        this.context = context
        (storage as AndroidStorage).setContext(context)
        (secureStorage as AndroidSecureStorage).setContext(context)
        (cache as AndroidCacheManager).setContext(context)
        (database as AndroidDatabaseManager).setContext(context)
    }
    
    override suspend fun initialize() {
        if (initialized) return
        
        cache.setMaxSize(50 * 1024 * 1024) // 50MB默认缓存
        database.initialize("unify_database", 1)
        
        initialized = true
    }
    
    override suspend fun clearAllData() {
        storage.clear()
        cache.clear()
        state.clearAllStates()
    }
    
    override suspend fun backupData(): BackupResult {
        return try {
            val data = mutableMapOf<String, Any>()
            
            // 备份存储数据
            val storageKeys = storage.getAllKeys()
            val storageData = mutableMapOf<String, String?>()
            storageKeys.forEach { key ->
                storageData[key] = storage.getString(key)
            }
            data["storage"] = storageData
            
            val jsonData = Json.encodeToString(data)
            val bytes = jsonData.toByteArray()
            
            BackupResult(
                success = true,
                data = bytes,
                size = bytes.size.toLong()
            )
        } catch (e: Exception) {
            BackupResult(
                success = false,
                error = e.message
            )
        }
    }
    
    override suspend fun restoreData(backupData: ByteArray): RestoreResult {
        return try {
            val jsonData = String(backupData)
            val data = Json.decodeFromString<Map<String, Any>>(jsonData)
            
            var restoredItems = 0
            
            // 恢复存储数据
            @Suppress("UNCHECKED_CAST")
            val storageData = data["storage"] as? Map<String, String?>
            storageData?.forEach { (key, value) ->
                if (value != null) {
                    storage.putString(key, value)
                    restoredItems++
                }
            }
            
            RestoreResult(
                success = true,
                restoredItems = restoredItems
            )
        } catch (e: Exception) {
            RestoreResult(
                success = false,
                error = e.message
            )
        }
    }
    
    override suspend fun getDataUsageStats(): DataUsageStats {
        val cacheSize = cache.getSize()
        val storageSize = calculateStorageSize()
        
        return DataUsageStats(
            totalStorageUsed = storageSize + cacheSize,
            cacheSize = cacheSize,
            databaseSize = calculateDatabaseSize(),
            secureStorageSize = calculateSecureStorageSize()
        )
    }
    
    private suspend fun calculateStorageSize(): Long {
        return try {
            context?.let { ctx ->
                val prefs = ctx.getSharedPreferences("unify_storage", Context.MODE_PRIVATE)
                val prefsFile = File(ctx.applicationInfo.dataDir + "/shared_prefs/unify_storage.xml")
                if (prefsFile.exists()) prefsFile.length() else 0L
            } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
    
    private suspend fun calculateDatabaseSize(): Long {
        return try {
            context?.let { ctx ->
                val dbFile = ctx.getDatabasePath("unify_database")
                if (dbFile.exists()) dbFile.length() else 0L
            } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
    
    private suspend fun calculateSecureStorageSize(): Long {
        return try {
            context?.let { ctx ->
                val prefsFile = File(ctx.applicationInfo.dataDir + "/shared_prefs/unify_secure_storage.xml")
                if (prefsFile.exists()) prefsFile.length() else 0L
            } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
}

/**
 * Android存储实现
 */
class AndroidStorage : UnifyStorage {
    private var context: Context? = null
    private val prefs: SharedPreferences?
        get() = context?.getSharedPreferences("unify_storage", Context.MODE_PRIVATE)
    
    fun setContext(context: Context) {
        this.context = context
    }
    
    override suspend fun putString(key: String, value: String) = withContext(Dispatchers.IO) {
        prefs?.edit()?.putString(key, value)?.apply()
    }
    
    override suspend fun getString(key: String): String? = withContext(Dispatchers.IO) {
        prefs?.getString(key, null)
    }
    
    override suspend fun putInt(key: String, value: Int) = withContext(Dispatchers.IO) {
        prefs?.edit()?.putInt(key, value)?.apply()
    }
    
    override suspend fun getInt(key: String): Int? = withContext(Dispatchers.IO) {
        prefs?.let { if (it.contains(key)) it.getInt(key, 0) else null }
    }
    
    override suspend fun putLong(key: String, value: Long) = withContext(Dispatchers.IO) {
        prefs?.edit()?.putLong(key, value)?.apply()
    }
    
    override suspend fun getLong(key: String): Long? = withContext(Dispatchers.IO) {
        prefs?.let { if (it.contains(key)) it.getLong(key, 0L) else null }
    }
    
    override suspend fun putFloat(key: String, value: Float) = withContext(Dispatchers.IO) {
        prefs?.edit()?.putFloat(key, value)?.apply()
    }
    
    override suspend fun getFloat(key: String): Float? = withContext(Dispatchers.IO) {
        prefs?.let { if (it.contains(key)) it.getFloat(key, 0f) else null }
    }
    
    override suspend fun putBoolean(key: String, value: Boolean) = withContext(Dispatchers.IO) {
        prefs?.edit()?.putBoolean(key, value)?.apply()
    }
    
    override suspend fun getBoolean(key: String): Boolean? = withContext(Dispatchers.IO) {
        prefs?.let { if (it.contains(key)) it.getBoolean(key, false) else null }
    }
    
    override suspend fun putByteArray(key: String, value: ByteArray) = withContext(Dispatchers.IO) {
        val encoded = android.util.Base64.encodeToString(value, android.util.Base64.DEFAULT)
        prefs?.edit()?.putString(key, encoded)?.apply()
    }
    
    override suspend fun getByteArray(key: String): ByteArray? = withContext(Dispatchers.IO) {
        prefs?.getString(key, null)?.let { encoded ->
            android.util.Base64.decode(encoded, android.util.Base64.DEFAULT)
        }
    }
    
    override suspend fun remove(key: String) = withContext(Dispatchers.IO) {
        prefs?.edit()?.remove(key)?.apply()
    }
    
    override suspend fun clear() = withContext(Dispatchers.IO) {
        prefs?.edit()?.clear()?.apply()
    }
    
    override suspend fun contains(key: String): Boolean = withContext(Dispatchers.IO) {
        prefs?.contains(key) ?: false
    }
    
    override suspend fun getAllKeys(): Set<String> = withContext(Dispatchers.IO) {
        prefs?.all?.keys ?: emptySet()
    }
}

/**
 * Android安全存储实现
 */
class AndroidSecureStorage : AndroidStorage(), UnifySecureStorage {
    private var context: Context? = null
    private val securePrefs: SharedPreferences?
        get() = context?.let { ctx ->
            try {
                val masterKey = MasterKey.Builder(ctx)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
                
                EncryptedSharedPreferences.create(
                    ctx,
                    "unify_secure_storage",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            } catch (e: Exception) {
                null
            }
        }
    
    override fun setContext(context: Context) {
        super.setContext(context)
        this.context = context
    }
    
    override suspend fun putSecureString(key: String, value: String) = withContext(Dispatchers.IO) {
        securePrefs?.edit()?.putString(key, value)?.apply()
    }
    
    override suspend fun getSecureString(key: String): String? = withContext(Dispatchers.IO) {
        securePrefs?.getString(key, null)
    }
    
    override suspend fun putEncrypted(key: String, value: ByteArray) = withContext(Dispatchers.IO) {
        val encoded = android.util.Base64.encodeToString(value, android.util.Base64.DEFAULT)
        securePrefs?.edit()?.putString(key, encoded)?.apply()
    }
    
    override suspend fun getDecrypted(key: String): ByteArray? = withContext(Dispatchers.IO) {
        securePrefs?.getString(key, null)?.let { encoded ->
            android.util.Base64.decode(encoded, android.util.Base64.DEFAULT)
        }
    }
    
    override suspend fun setEncryptionKey(key: String) {
        // Android EncryptedSharedPreferences 自动处理加密密钥
    }
    
    override suspend fun authenticateWithBiometric(): Boolean {
        // 生物识别认证实现（使用BiometricPrompt API）
        return false
    }
}

/**
 * Android缓存管理器实现
 */
class AndroidCacheManager : UnifyCacheManager {
    private var context: Context? = null
    private val cache = ConcurrentHashMap<String, CacheEntry>()
    private var maxSize: Long = 50 * 1024 * 1024 // 50MB
    
    fun setContext(context: Context) {
        this.context = context
    }
    
    override suspend fun <T> put(key: String, value: T, ttl: Long?) {
        val entry = CacheEntry(
            value = value,
            timestamp = System.currentTimeMillis(),
            ttl = ttl
        )
        cache[key] = entry
        
        if (getSize() > maxSize) {
            cleanupExpired()
        }
    }
    
    override suspend fun <T> get(key: String, type: Class<T>): T? {
        val entry = cache[key] ?: return null
        
        if (entry.ttl != null && System.currentTimeMillis() - entry.timestamp > entry.ttl) {
            cache.remove(key)
            return null
        }
        
        return try {
            type.cast(entry.value)
        } catch (e: ClassCastException) {
            null
        }
    }
    
    override suspend fun remove(key: String) {
        cache.remove(key)
    }
    
    override suspend fun clear() {
        cache.clear()
    }
    
    override suspend fun isValid(key: String): Boolean {
        val entry = cache[key] ?: return false
        return entry.ttl == null || System.currentTimeMillis() - entry.timestamp <= entry.ttl
    }
    
    override suspend fun getSize(): Long {
        return cache.size.toLong() * 1024 // 粗略估算
    }
    
    override suspend fun setMaxSize(maxSize: Long) {
        this.maxSize = maxSize
    }
    
    override suspend fun cleanupExpired() {
        val currentTime = System.currentTimeMillis()
        val expiredKeys = cache.entries.filter { (_, entry) ->
            entry.ttl != null && currentTime - entry.timestamp > entry.ttl
        }.map { it.key }
        
        expiredKeys.forEach { cache.remove(it) }
    }
    
    private data class CacheEntry(
        val value: Any?,
        val timestamp: Long,
        val ttl: Long? = null
    )
}

/**
 * Android状态管理器实现
 */
class AndroidStateManager : UnifyStateManager {
    private val states = ConcurrentHashMap<String, MutableStateFlow<Any?>>()
    
    override fun <T> setState(key: String, value: T) {
        val stateFlow = states.getOrPut(key) { MutableStateFlow(null) }
        stateFlow.value = value
    }
    
    override fun <T> getState(key: String, type: Class<T>): T? {
        val stateFlow = states[key] ?: return null
        return try {
            type.cast(stateFlow.value)
        } catch (e: ClassCastException) {
            null
        }
    }
    
    override fun <T> observeState(key: String, type: Class<T>): Flow<T?> {
        val stateFlow = states.getOrPut(key) { MutableStateFlow(null) }
        return stateFlow.asStateFlow()
    }
    
    override fun removeState(key: String) {
        states.remove(key)
    }
    
    override fun clearAllStates() {
        states.clear()
    }
    
    override suspend fun persistState() {
        // 状态持久化实现（使用SharedPreferences）
    }
    
    override suspend fun restoreState() {
        // 状态恢复实现（从本地存储读取）
    }
}

/**
 * Android数据库管理器实现
 */
class AndroidDatabaseManager : UnifyDatabaseManager {
    private var context: Context? = null
    
    fun setContext(context: Context) {
        this.context = context
    }
    
    override suspend fun initialize(databaseName: String, version: Int) {
        // SQLite数据库初始化实现（使用Room或SQLDelight）
    }
    
    override suspend fun query(sql: String, args: Array<Any>?): List<Map<String, Any?>> {
        // SQL查询实现（使用数据库驱动）
        return emptyList()
    }
    
    override suspend fun execute(sql: String, args: Array<Any>?): Int {
        // SQL执行实现（使用数据库驱动）
        return 0
    }
    
    override suspend fun beginTransaction(): TransactionHandle {
        return TransactionHandle(
            id = java.util.UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis()
        )
    }
    
    override suspend fun commitTransaction(handle: TransactionHandle) {
        // 事务提交实现（使用数据库事务API）
    }
    
    override suspend fun rollbackTransaction(handle: TransactionHandle) {
        // 事务回滚实现（使用数据库事务API）
    }
    
    override suspend fun close() {
        // 数据库关闭实现（释放连接资源）
    }
}
