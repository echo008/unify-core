package com.unify.core.storage

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

actual class PlatformStorageAdapter : StorageAdapter {
    
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var filesDir: File
    
    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences("unify_storage", Context.MODE_PRIVATE)
        filesDir = context.filesDir
    }
    
    override suspend fun save(key: String, data: ByteArray): Boolean = withContext(Dispatchers.IO) {
        try {
            val encoded = android.util.Base64.encodeToString(data, android.util.Base64.DEFAULT)
            sharedPreferences.edit().putString(key, encoded).apply()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun load(key: String): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val encoded = sharedPreferences.getString(key, null) ?: return@withContext null
            android.util.Base64.decode(encoded, android.util.Base64.DEFAULT)
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun delete(key: String): Boolean = withContext(Dispatchers.IO) {
        try {
            sharedPreferences.edit().remove(key).apply()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun exists(key: String): Boolean = withContext(Dispatchers.IO) {
        sharedPreferences.contains(key)
    }
    
    override suspend fun clear(): Boolean = withContext(Dispatchers.IO) {
        try {
            sharedPreferences.edit().clear().apply()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun getAllKeys(): List<String> = withContext(Dispatchers.IO) {
        try {
            sharedPreferences.all.keys.toList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun saveToFile(fileName: String, data: ByteArray): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(filesDir, fileName)
            FileOutputStream(file).use { it.write(data) }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun loadFromFile(fileName: String): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val file = File(filesDir, fileName)
            if (!file.exists()) return@withContext null
            FileInputStream(file).use { it.readBytes() }
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun deleteFile(fileName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(filesDir, fileName)
            file.delete()
        } catch (e: Exception) {
            false
        }
    }
}
