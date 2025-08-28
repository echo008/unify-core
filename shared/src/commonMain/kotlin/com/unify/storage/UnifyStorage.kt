package com.unify.storage

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

/**
 * 统一存储系统
 * 基于文档第8章要求实现的网络与存储系统
 */

/**
 * 统一存储接口
 */
interface UnifyStorage {
    suspend fun getString(key: String, defaultValue: String? = null): String?
    suspend fun putString(key: String, value: String)
    suspend fun getInt(key: String, defaultValue: Int = 0): Int
    suspend fun putInt(key: String, value: Int)
    suspend fun getLong(key: String, defaultValue: Long = 0L): Long
    suspend fun putLong(key: String, value: Long)
    suspend fun getFloat(key: String, defaultValue: Float = 0f): Float
    suspend fun putFloat(key: String, value: Float)
    suspend fun getBoolean(key: String, defaultValue: Boolean = false): Boolean
    suspend fun putBoolean(key: String, value: Boolean)
    suspend fun remove(key: String)
    suspend fun clear()
    suspend fun contains(key: String): Boolean
    suspend fun getAllKeys(): Set<String>
    
    // 对象存储
    suspend inline fun <reified T> getObject(key: String): T?
    suspend inline fun <reified T> putObject(key: String, value: T)
    
    // 流式数据
    fun <T> observeKey(key: String): Flow<T?>
}

/**
 * 数据库存储接口
 */
interface UnifyDatabase {
    suspend fun <T> insert(entity: T): Long
    suspend fun <T> update(entity: T): Int
    suspend fun <T> delete(entity: T): Int
    suspend fun <T> query(query: DatabaseQuery<T>): List<T>
    suspend fun <T> queryFirst(query: DatabaseQuery<T>): T?
    suspend fun <T> observeQuery(query: DatabaseQuery<T>): Flow<List<T>>
    suspend fun executeRaw(sql: String, args: List<Any> = emptyList()): Int
    suspend fun <T> queryRaw(sql: String, args: List<Any> = emptyList(), mapper: (Map<String, Any?>) -> T): List<T>
}

/**
 * 数据库查询构建器
 */
data class DatabaseQuery<T>(
    val table: String,
    val columns: List<String> = listOf("*"),
    val where: String? = null,
    val whereArgs: List<Any> = emptyList(),
    val orderBy: String? = null,
    val limit: Int? = null,
    val offset: Int? = null
)

/**
 * 存储配置
 */
@Serializable
data class StorageConfig(
    val enableEncryption: Boolean = false,
    val encryptionKey: String? = null,
    val enableCompression: Boolean = false,
    val enableCache: Boolean = true,
    val cacheSize: Int = 100,
    val enableBackup: Boolean = false,
    val backupInterval: Long = 86400000L // 24小时
)

/**
 * 存储类型枚举
 */
enum class StorageType {
    MEMORY,
    PREFERENCES,
    DATABASE,
    FILE_SYSTEM,
    SECURE_STORAGE
}

/**
 * 存储异常
 */
sealed class StorageException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class ReadError(message: String, cause: Throwable? = null) : StorageException(message, cause)
    class WriteError(message: String, cause: Throwable? = null) : StorageException(message, cause)
    class EncryptionError(message: String, cause: Throwable? = null) : StorageException(message, cause)
    class DatabaseError(message: String, cause: Throwable? = null) : StorageException(message, cause)
    class FileSystemError(message: String, cause: Throwable? = null) : StorageException(message, cause)
}

/**
 * 内存存储实现
 */
class MemoryStorage : UnifyStorage {
    private val storage = mutableMapOf<String, Any>()
    private val observers = mutableMapOf<String, MutableStateFlow<Any?>>()
    
    override suspend fun getString(key: String, defaultValue: String?): String? {
        return storage[key] as? String ?: defaultValue
    }
    
    override suspend fun putString(key: String, value: String) {
        storage[key] = value
        notifyObservers(key, value)
    }
    
    override suspend fun getInt(key: String, defaultValue: Int): Int {
        return storage[key] as? Int ?: defaultValue
    }
    
    override suspend fun putInt(key: String, value: Int) {
        storage[key] = value
        notifyObservers(key, value)
    }
    
    override suspend fun getLong(key: String, defaultValue: Long): Long {
        return storage[key] as? Long ?: defaultValue
    }
    
    override suspend fun putLong(key: String, value: Long) {
        storage[key] = value
        notifyObservers(key, value)
    }
    
    override suspend fun getFloat(key: String, defaultValue: Float): Float {
        return storage[key] as? Float ?: defaultValue
    }
    
    override suspend fun putFloat(key: String, value: Float) {
        storage[key] = value
        notifyObservers(key, value)
    }
    
    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return storage[key] as? Boolean ?: defaultValue
    }
    
    override suspend fun putBoolean(key: String, value: Boolean) {
        storage[key] = value
        notifyObservers(key, value)
    }
    
    override suspend fun remove(key: String) {
        storage.remove(key)
        notifyObservers(key, null)
    }
    
    override suspend fun clear() {
        val keys = storage.keys.toList()
        storage.clear()
        keys.forEach { key ->
            notifyObservers(key, null)
        }
    }
    
    override suspend fun contains(key: String): Boolean {
        return storage.containsKey(key)
    }
    
    override suspend fun getAllKeys(): Set<String> {
        return storage.keys.toSet()
    }
    
    override suspend inline fun <reified T> getObject(key: String): T? {
        val json = getString(key) ?: return null
        return try {
            Json.decodeFromString<T>(json)
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend inline fun <reified T> putObject(key: String, value: T) {
        val json = Json.encodeToString(value)
        putString(key, json)
    }
    
    override fun <T> observeKey(key: String): Flow<T?> {
        val flow = observers.getOrPut(key) { MutableStateFlow(storage[key]) }
        return flow.asStateFlow() as Flow<T?>
    }
    
    private fun notifyObservers(key: String, value: Any?) {
        observers[key]?.value = value
    }
}

/**
 * 加密存储装饰器
 */
class EncryptedStorage(
    private val delegate: UnifyStorage,
    private val encryptor: StorageEncryptor
) : UnifyStorage {
    
    override suspend fun getString(key: String, defaultValue: String?): String? {
        val encrypted = delegate.getString(key) ?: return defaultValue
        return try {
            encryptor.decrypt(encrypted)
        } catch (e: Exception) {
            defaultValue
        }
    }
    
    override suspend fun putString(key: String, value: String) {
        val encrypted = encryptor.encrypt(value)
        delegate.putString(key, encrypted)
    }
    
    override suspend fun getInt(key: String, defaultValue: Int): Int {
        return delegate.getInt(key, defaultValue)
    }
    
    override suspend fun putInt(key: String, value: Int) {
        delegate.putInt(key, value)
    }
    
    override suspend fun getLong(key: String, defaultValue: Long): Long {
        return delegate.getLong(key, defaultValue)
    }
    
    override suspend fun putLong(key: String, value: Long) {
        delegate.putLong(key, value)
    }
    
    override suspend fun getFloat(key: String, defaultValue: Float): Float {
        return delegate.getFloat(key, defaultValue)
    }
    
    override suspend fun putFloat(key: String, value: Float) {
        delegate.putFloat(key, value)
    }
    
    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return delegate.getBoolean(key, defaultValue)
    }
    
    override suspend fun putBoolean(key: String, value: Boolean) {
        delegate.putBoolean(key, value)
    }
    
    override suspend fun remove(key: String) {
        delegate.remove(key)
    }
    
    override suspend fun clear() {
        delegate.clear()
    }
    
    override suspend fun contains(key: String): Boolean {
        return delegate.contains(key)
    }
    
    override suspend fun getAllKeys(): Set<String> {
        return delegate.getAllKeys()
    }
    
    override suspend inline fun <reified T> getObject(key: String): T? {
        val json = getString(key) ?: return null
        return try {
            Json.decodeFromString<T>(json)
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend inline fun <reified T> putObject(key: String, value: T) {
        val json = Json.encodeToString(value)
        putString(key, json)
    }
    
    override fun <T> observeKey(key: String): Flow<T?> {
        return delegate.observeKey(key)
    }
}

/**
 * 存储加密器接口
 */
interface StorageEncryptor {
    fun encrypt(data: String): String
    fun decrypt(encryptedData: String): String
}

/**
 * 简单存储加密器实现（生产环境应使用更安全的实现）
 */
class SimpleStorageEncryptor(private val key: String) : StorageEncryptor {
    override fun encrypt(data: String): String {
        // 简单的XOR加密（仅用于演示）
        return data.mapIndexed { index, char ->
            (char.code xor key[index % key.length].code).toChar()
        }.joinToString("")
    }
    
    override fun decrypt(encryptedData: String): String {
        // XOR解密
        return encryptedData.mapIndexed { index, char ->
            (char.code xor key[index % key.length].code).toChar()
        }.joinToString("")
    }
}

/**
 * 存储仓库模式实现
 */
abstract class StorageRepository<T>(
    private val storage: UnifyStorage,
    private val keyPrefix: String = ""
) {
    protected abstract suspend fun serialize(item: T): String
    protected abstract suspend fun deserialize(data: String): T?
    
    protected fun getKey(id: String): String = if (keyPrefix.isNotEmpty()) "${keyPrefix}_$id" else id
    
    suspend fun save(id: String, item: T) {
        val data = serialize(item)
        storage.putString(getKey(id), data)
    }
    
    suspend fun load(id: String): T? {
        val data = storage.getString(getKey(id)) ?: return null
        return deserialize(data)
    }
    
    suspend fun delete(id: String) {
        storage.remove(getKey(id))
    }
    
    suspend fun exists(id: String): Boolean {
        return storage.contains(getKey(id))
    }
    
    suspend fun getAllIds(): List<String> {
        val prefix = if (keyPrefix.isNotEmpty()) "${keyPrefix}_" else ""
        return storage.getAllKeys()
            .filter { it.startsWith(prefix) }
            .map { it.removePrefix(prefix) }
    }
    
    fun observe(id: String): Flow<T?> {
        return storage.observeKey<String>(getKey(id))
    }
}

/**
 * 存储管理器
 */
class StorageManager(
    private val config: StorageConfig
) {
    private val storages = mutableMapOf<StorageType, UnifyStorage>()
    
    fun getStorage(type: StorageType): UnifyStorage {
        return storages.getOrPut(type) {
            createStorage(type)
        }
    }
    
    private fun createStorage(type: StorageType): UnifyStorage {
        val baseStorage = when (type) {
            StorageType.MEMORY -> MemoryStorage()
            StorageType.PREFERENCES -> createPreferencesStorage()
            StorageType.DATABASE -> createDatabaseStorage()
            StorageType.FILE_SYSTEM -> createFileSystemStorage()
            StorageType.SECURE_STORAGE -> createSecureStorage()
        }
        
        return if (config.enableEncryption && config.encryptionKey != null) {
            EncryptedStorage(baseStorage, SimpleStorageEncryptor(config.encryptionKey))
        } else {
            baseStorage
        }
    }
    
    private fun createPreferencesStorage(): UnifyStorage {
        return PreferencesStorageImpl()
    }
    
    private fun createDatabaseStorage(): UnifyStorage {
        return DatabaseStorageImpl()
    }
    
    private fun createFileSystemStorage(): UnifyStorage {
        return FileSystemStorageImpl()
    }
    
    private fun createSecureStorage(): UnifyStorage {
        return SecureStorageImpl()
    }
}

/**
 * 平台特定存储实现（将在各平台模块中实现）
 */
expect class PreferencesStorageImpl() : UnifyStorage
expect class DatabaseStorageImpl() : UnifyStorage
expect class FileSystemStorageImpl() : UnifyStorage
expect class SecureStorageImpl() : UnifyStorage

/**
 * 存储工厂
 */
object StorageFactory {
    fun createStorage(
        type: StorageType,
        config: StorageConfig = StorageConfig()
    ): UnifyStorage {
        val manager = StorageManager(config)
        return manager.getStorage(type)
    }
    
    fun createRepository(
        storage: UnifyStorage,
        keyPrefix: String = ""
    ): StorageRepository<String> {
        return object : StorageRepository<String>(storage, keyPrefix) {
            override suspend fun serialize(item: String): String = item
            override suspend fun deserialize(data: String): String = data
        }
    }
}

/**
 * 存储工具类
 */
object StorageUtils {
    fun generateKey(vararg parts: String): String {
        return parts.joinToString(":")
    }
    
    fun isValidKey(key: String): Boolean {
        return key.isNotEmpty() && key.all { it.isLetterOrDigit() || it in "_-:" }
    }
    
    suspend fun migrateData(
        source: UnifyStorage,
        target: UnifyStorage,
        keys: Set<String>? = null
    ) {
        val keysToMigrate = keys ?: source.getAllKeys()
        keysToMigrate.forEach { key ->
            val value = source.getString(key)
            if (value != null) {
                target.putString(key, value)
            }
        }
    }
}
