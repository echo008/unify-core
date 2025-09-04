package com.unify.core.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Android平台存储工厂实现
 */
actual class PlatformStorageFactory : StorageFactory {
    
    companion object {
        private var context: Context? = null
        
        fun initialize(applicationContext: Context) {
            context = applicationContext.applicationContext
        }
    }
    
    actual override fun createStorage(config: StorageConfig): UnifyStorage {
        val appContext = context ?: throw IllegalStateException("StorageFactory not initialized")
        return if (config.encrypted) {
            AndroidEncryptedStorage(appContext, config)
        } else {
            AndroidSharedPreferencesStorage(appContext, config)
        }
    }
    
    actual override fun createEncryptedStorage(config: StorageConfig, encryptionKey: String): UnifyStorage {
        val appContext = context ?: throw IllegalStateException("StorageFactory not initialized")
        return AndroidEncryptedStorage(appContext, config, encryptionKey)
    }
    
    actual override fun createMemoryStorage(): UnifyStorage {
        return AndroidMemoryStorage()
    }
    
    actual override fun createFileStorage(path: String): UnifyStorage {
        val appContext = context ?: throw IllegalStateException("StorageFactory not initialized")
        return AndroidFileStorage(appContext, path)
    }
}

/**
 * Android SharedPreferences存储实现
 */
class AndroidSharedPreferencesStorage(
    private val context: Context,
    private val config: StorageConfig
) : UnifyStorage {
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        config.name,
        Context.MODE_PRIVATE
    )
    
    private val _events = MutableSharedFlow<StorageEvent>()
    private val json = Json { ignoreUnknownKeys = true }
    
    override suspend fun <T> save(key: String, value: T, serializer: KSerializer<T>) {
        try {
            val jsonString = json.encodeToString(serializer, value)
            val editor = sharedPreferences.edit()
            
            val wasExisting = sharedPreferences.contains(key)
            editor.putString(key, jsonString)
            editor.apply()
            
            _events.tryEmit(if (wasExisting) StorageEvent.KeyUpdated(key) else StorageEvent.KeyAdded(key))
        } catch (e: Exception) {
            throw StorageException("Failed to save data for key: $key", e)
        }
    }
    
    override suspend fun <T> load(key: String, serializer: KSerializer<T>): T? {
        return try {
            val jsonString = sharedPreferences.getString(key, null)
            jsonString?.let { json.decodeFromString(serializer, it) }
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun delete(key: String) {
        try {
            if (sharedPreferences.contains(key)) {
                val editor = sharedPreferences.edit()
                editor.remove(key)
                editor.apply()
                _events.tryEmit(StorageEvent.KeyDeleted(key))
            }
        } catch (e: Exception) {
            throw StorageException("Failed to delete key: $key", e)
        }
    }
    
    override suspend fun clear() {
        try {
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()
            _events.tryEmit(StorageEvent.Cleared)
        } catch (e: Exception) {
            throw StorageException("Failed to clear storage", e)
        }
    }
    
    override suspend fun exists(key: String): Boolean {
        return sharedPreferences.contains(key)
    }
    
    override suspend fun getAllKeys(): Set<String> {
        return sharedPreferences.all.keys
    }
    
    override suspend fun getSize(): Long {
        return try {
            sharedPreferences.all.values.sumOf { value ->
                when (value) {
                    is String -> value.toByteArray().size.toLong()
                    else -> value.toString().toByteArray().size.toLong()
                }
            }
        } catch (e: Exception) {
            0L
        }
    }
    
    override fun observeChanges(): Flow<StorageEvent> {
        return _events.asSharedFlow()
    }
    
    override suspend fun batch(operations: List<StorageOperation>) {
        try {
            val editor = sharedPreferences.edit()
            
            operations.forEach { operation ->
                when (operation) {
                    is StorageOperation.Save<*> -> {
                        val jsonString = json.encodeToString(operation.serializer as KSerializer<Any?>, operation.value)
                        editor.putString(operation.key, jsonString)
                    }
                    is StorageOperation.Delete -> {
                        editor.remove(operation.key)
                    }
                    is StorageOperation.Clear -> {
                        editor.clear()
                    }
                }
            }
            
            editor.apply()
        } catch (e: Exception) {
            throw StorageException("Batch operation failed", e)
        }
    }
    
    override suspend fun backup(): String {
        return try {
            val allData = sharedPreferences.all
            json.encodeToString(allData)
        } catch (e: Exception) {
            throw StorageException("Backup failed", e)
        }
    }
    
    override suspend fun restore(backupData: String) {
        try {
            val data = json.decodeFromString<Map<String, Any>>(backupData)
            val editor = sharedPreferences.edit()
            editor.clear()
            
            data.forEach { (key, value) ->
                when (value) {
                    is String -> editor.putString(key, value)
                    is Boolean -> editor.putBoolean(key, value)
                    is Int -> editor.putInt(key, value)
                    is Long -> editor.putLong(key, value)
                    is Float -> editor.putFloat(key, value)
                    else -> editor.putString(key, value.toString())
                }
            }
            
            editor.apply()
        } catch (e: Exception) {
            throw StorageException("Restore failed", e)
        }
    }
    
    override suspend fun compact() {
        // SharedPreferences不需要压缩
    }
}

/**
 * Android加密存储实现
 */
class AndroidEncryptedStorage(
    private val context: Context,
    private val config: StorageConfig,
    private val customEncryptionKey: String? = null
) : UnifyStorage {
    
    private val encryptedSharedPreferences: SharedPreferences
    private val _events = MutableSharedFlow<StorageEvent>()
    private val json = Json { ignoreUnknownKeys = true }
    
    init {
        encryptedSharedPreferences = if (customEncryptionKey != null) {
            // 使用自定义加密密钥
            createCustomEncryptedPreferences()
        } else {
            // 使用Android Jetpack Security
            createJetpackEncryptedPreferences()
        }
    }
    
    private fun createJetpackEncryptedPreferences(): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        
        return EncryptedSharedPreferences.create(
            context,
            config.name,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    private fun createCustomEncryptedPreferences(): SharedPreferences {
        // 使用自定义加密实现
        return context.getSharedPreferences("${config.name}_encrypted", Context.MODE_PRIVATE)
    }
    
    override suspend fun <T> save(key: String, value: T, serializer: KSerializer<T>) {
        try {
            val jsonString = json.encodeToString(serializer, value)
            val encryptedValue = if (customEncryptionKey != null) {
                encrypt(jsonString, customEncryptionKey)
            } else {
                jsonString
            }
            
            val editor = encryptedSharedPreferences.edit()
            val wasExisting = encryptedSharedPreferences.contains(key)
            editor.putString(key, encryptedValue)
            editor.apply()
            
            _events.tryEmit(if (wasExisting) StorageEvent.KeyUpdated(key) else StorageEvent.KeyAdded(key))
        } catch (e: Exception) {
            throw StorageException("Failed to save encrypted data for key: $key", e)
        }
    }
    
    override suspend fun <T> load(key: String, serializer: KSerializer<T>): T? {
        return try {
            val encryptedValue = encryptedSharedPreferences.getString(key, null)
            if (encryptedValue != null) {
                val jsonString = if (customEncryptionKey != null) {
                    decrypt(encryptedValue, customEncryptionKey)
                } else {
                    encryptedValue
                }
                json.decodeFromString(serializer, jsonString)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun delete(key: String) {
        try {
            if (encryptedSharedPreferences.contains(key)) {
                val editor = encryptedSharedPreferences.edit()
                editor.remove(key)
                editor.apply()
                _events.tryEmit(StorageEvent.KeyDeleted(key))
            }
        } catch (e: Exception) {
            throw StorageException("Failed to delete encrypted key: $key", e)
        }
    }
    
    override suspend fun clear() {
        try {
            val editor = encryptedSharedPreferences.edit()
            editor.clear()
            editor.apply()
            _events.tryEmit(StorageEvent.Cleared)
        } catch (e: Exception) {
            throw StorageException("Failed to clear encrypted storage", e)
        }
    }
    
    override suspend fun exists(key: String): Boolean {
        return encryptedSharedPreferences.contains(key)
    }
    
    override suspend fun getAllKeys(): Set<String> {
        return encryptedSharedPreferences.all.keys
    }
    
    override suspend fun getSize(): Long {
        return try {
            encryptedSharedPreferences.all.values.sumOf { value ->
                when (value) {
                    is String -> value.toByteArray().size.toLong()
                    else -> value.toString().toByteArray().size.toLong()
                }
            }
        } catch (e: Exception) {
            0L
        }
    }
    
    override fun observeChanges(): Flow<StorageEvent> {
        return _events.asSharedFlow()
    }
    
    override suspend fun batch(operations: List<StorageOperation>) {
        try {
            val editor = encryptedSharedPreferences.edit()
            
            operations.forEach { operation ->
                when (operation) {
                    is StorageOperation.Save<*> -> {
                        val jsonString = json.encodeToString(operation.serializer as KSerializer<Any?>, operation.value)
                        val encryptedValue = if (customEncryptionKey != null) {
                            encrypt(jsonString, customEncryptionKey)
                        } else {
                            jsonString
                        }
                        editor.putString(operation.key, encryptedValue)
                    }
                    is StorageOperation.Delete -> {
                        editor.remove(operation.key)
                    }
                    is StorageOperation.Clear -> {
                        editor.clear()
                    }
                }
            }
            
            editor.apply()
        } catch (e: Exception) {
            throw StorageException("Encrypted batch operation failed", e)
        }
    }
    
    override suspend fun backup(): String {
        return try {
            val allData = encryptedSharedPreferences.all
            json.encodeToString(allData)
        } catch (e: Exception) {
            throw StorageException("Encrypted backup failed", e)
        }
    }
    
    override suspend fun restore(backupData: String) {
        try {
            val data = json.decodeFromString<Map<String, String>>(backupData)
            val editor = encryptedSharedPreferences.edit()
            editor.clear()
            
            data.forEach { (key, value) ->
                editor.putString(key, value)
            }
            
            editor.apply()
        } catch (e: Exception) {
            throw StorageException("Encrypted restore failed", e)
        }
    }
    
    override suspend fun compact() {
        // 加密存储不需要压缩
    }
    
    // 自定义加密方法
    private fun encrypt(data: String, key: String): String {
        return try {
            val secretKey = SecretKeySpec(key.toByteArray().copyOf(32), "AES")
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val iv = ByteArray(16)
            SecureRandom().nextBytes(iv)
            val ivSpec = IvParameterSpec(iv)
            
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
            val encryptedData = cipher.doFinal(data.toByteArray())
            
            android.util.Base64.encodeToString(iv + encryptedData, android.util.Base64.DEFAULT)
        } catch (e: Exception) {
            throw StorageException("Encryption failed", e)
        }
    }
    
    private fun decrypt(encryptedData: String, key: String): String {
        return try {
            val secretKey = SecretKeySpec(key.toByteArray().copyOf(32), "AES")
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            
            val decodedData = android.util.Base64.decode(encryptedData, android.util.Base64.DEFAULT)
            val iv = decodedData.copyOfRange(0, 16)
            val encrypted = decodedData.copyOfRange(16, decodedData.size)
            
            val ivSpec = IvParameterSpec(iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
            val decryptedData = cipher.doFinal(encrypted)
            
            String(decryptedData)
        } catch (e: Exception) {
            throw StorageException("Decryption failed", e)
        }
    }
}

/**
 * Android内存存储实现
 */
class AndroidMemoryStorage : UnifyStorage {
    
    private val storage = mutableMapOf<String, String>()
    private val _events = MutableSharedFlow<StorageEvent>()
    private val json = Json { ignoreUnknownKeys = true }
    
    override suspend fun <T> save(key: String, value: T, serializer: KSerializer<T>) {
        try {
            val jsonString = json.encodeToString(serializer, value)
            val wasExisting = storage.containsKey(key)
            storage[key] = jsonString
            
            _events.tryEmit(if (wasExisting) StorageEvent.KeyUpdated(key) else StorageEvent.KeyAdded(key))
        } catch (e: Exception) {
            throw StorageException("Failed to save data to memory for key: $key", e)
        }
    }
    
    override suspend fun <T> load(key: String, serializer: KSerializer<T>): T? {
        return try {
            val jsonString = storage[key]
            jsonString?.let { json.decodeFromString(serializer, it) }
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun delete(key: String) {
        if (storage.remove(key) != null) {
            _events.tryEmit(StorageEvent.KeyDeleted(key))
        }
    }
    
    override suspend fun clear() {
        storage.clear()
        _events.tryEmit(StorageEvent.Cleared)
    }
    
    override suspend fun exists(key: String): Boolean {
        return storage.containsKey(key)
    }
    
    override suspend fun getAllKeys(): Set<String> {
        return storage.keys.toSet()
    }
    
    override suspend fun getSize(): Long {
        return storage.values.sumOf { it.toByteArray().size.toLong() }
    }
    
    override fun observeChanges(): Flow<StorageEvent> {
        return _events.asSharedFlow()
    }
    
    override suspend fun batch(operations: List<StorageOperation>) {
        operations.forEach { operation ->
            when (operation) {
                is StorageOperation.Save<*> -> {
                    val jsonString = json.encodeToString(operation.serializer as KSerializer<Any?>, operation.value)
                    storage[operation.key] = jsonString
                }
                is StorageOperation.Delete -> {
                    storage.remove(operation.key)
                }
                is StorageOperation.Clear -> {
                    storage.clear()
                }
            }
        }
    }
    
    override suspend fun backup(): String {
        return json.encodeToString(storage)
    }
    
    override suspend fun restore(backupData: String) {
        try {
            val data = json.decodeFromString<Map<String, String>>(backupData)
            storage.clear()
            storage.putAll(data)
        } catch (e: Exception) {
            throw StorageException("Memory restore failed", e)
        }
    }
    
    override suspend fun compact() {
        // 内存存储不需要压缩
    }
}

/**
 * Android文件存储实现
 */
class AndroidFileStorage(
    private val context: Context,
    private val path: String
) : UnifyStorage {
    
    private val storageDir = File(context.filesDir, path)
    private val _events = MutableSharedFlow<StorageEvent>()
    private val json = Json { ignoreUnknownKeys = true }
    
    init {
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
    }
    
    override suspend fun <T> save(key: String, value: T, serializer: KSerializer<T>) {
        try {
            val jsonString = json.encodeToString(serializer, value)
            val file = File(storageDir, key)
            val wasExisting = file.exists()
            
            file.writeText(jsonString)
            
            _events.tryEmit(if (wasExisting) StorageEvent.KeyUpdated(key) else StorageEvent.KeyAdded(key))
        } catch (e: Exception) {
            throw StorageException("Failed to save file for key: $key", e)
        }
    }
    
    override suspend fun <T> load(key: String, serializer: KSerializer<T>): T? {
        return try {
            val file = File(storageDir, key)
            if (file.exists()) {
                val jsonString = file.readText()
                json.decodeFromString(serializer, jsonString)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun delete(key: String) {
        try {
            val file = File(storageDir, key)
            if (file.exists() && file.delete()) {
                _events.tryEmit(StorageEvent.KeyDeleted(key))
            }
        } catch (e: Exception) {
            throw StorageException("Failed to delete file for key: $key", e)
        }
    }
    
    override suspend fun clear() {
        try {
            storageDir.listFiles()?.forEach { file ->
                file.delete()
            }
            _events.tryEmit(StorageEvent.Cleared)
        } catch (e: Exception) {
            throw StorageException("Failed to clear file storage", e)
        }
    }
    
    override suspend fun exists(key: String): Boolean {
        return File(storageDir, key).exists()
    }
    
    override suspend fun getAllKeys(): Set<String> {
        return try {
            storageDir.listFiles()?.map { it.name }?.toSet() ?: emptySet()
        } catch (e: Exception) {
            emptySet()
        }
    }
    
    override suspend fun getSize(): Long {
        return try {
            storageDir.listFiles()?.sumOf { it.length() } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
    
    override fun observeChanges(): Flow<StorageEvent> {
        return _events.asSharedFlow()
    }
    
    override suspend fun batch(operations: List<StorageOperation>) {
        try {
            operations.forEach { operation ->
                when (operation) {
                    is StorageOperation.Save<*> -> {
                        val jsonString = json.encodeToString(operation.serializer as KSerializer<Any?>, operation.value)
                        val file = File(storageDir, operation.key)
                        file.writeText(jsonString)
                    }
                    is StorageOperation.Delete -> {
                        val file = File(storageDir, operation.key)
                        file.delete()
                    }
                    is StorageOperation.Clear -> {
                        storageDir.listFiles()?.forEach { it.delete() }
                    }
                }
            }
        } catch (e: Exception) {
            throw StorageException("File batch operation failed", e)
        }
    }
    
    override suspend fun backup(): String {
        return try {
            val allData = mutableMapOf<String, String>()
            storageDir.listFiles()?.forEach { file ->
                allData[file.name] = file.readText()
            }
            json.encodeToString(allData)
        } catch (e: Exception) {
            throw StorageException("File backup failed", e)
        }
    }
    
    override suspend fun restore(backupData: String) {
        try {
            val data = json.decodeFromString<Map<String, String>>(backupData)
            
            // 清空现有文件
            storageDir.listFiles()?.forEach { it.delete() }
            
            // 恢复数据
            data.forEach { (key, value) ->
                val file = File(storageDir, key)
                file.writeText(value)
            }
        } catch (e: Exception) {
            throw StorageException("File restore failed", e)
        }
    }
    
    override suspend fun compact() {
        // 文件存储可以通过重新组织文件来压缩
        try {
            val tempDir = File(storageDir.parent, "${storageDir.name}_temp")
            tempDir.mkdirs()
            
            storageDir.listFiles()?.forEach { file ->
                val tempFile = File(tempDir, file.name)
                file.copyTo(tempFile, overwrite = true)
            }
            
            storageDir.deleteRecursively()
            tempDir.renameTo(storageDir)
        } catch (e: Exception) {
            throw StorageException("File compaction failed", e)
        }
    }
}
