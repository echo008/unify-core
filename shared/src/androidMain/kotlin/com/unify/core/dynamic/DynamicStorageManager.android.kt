package com.unify.core.dynamic

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Android平台存储适配器实现
 */
actual class PlatformStorageAdapter : StorageAdapter {
    
    private lateinit var context: Context
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var filesDir: File
    
    companion object {
        private const val PREFS_NAME = "unify_dynamic_storage"
        private const val FILES_DIR = "dynamic_components"
    }
    
    fun initialize(context: Context) {
        this.context = context
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        this.filesDir = File(context.filesDir, FILES_DIR)
        if (!filesDir.exists()) {
            filesDir.mkdirs()
        }
    }
    
    override suspend fun save(key: String, data: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // 对于大数据使用文件存储，小数据使用SharedPreferences
            if (data.length > 8192) { // 8KB阈值
                val file = File(filesDir, "${key.hashCode()}.dat")
                file.writeText(data)
                
                // 在SharedPreferences中记录文件路径
                sharedPreferences.edit()
                    .putString(key, "FILE:${file.absolutePath}")
                    .apply()
            } else {
                sharedPreferences.edit()
                    .putString(key, data)
                    .apply()
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun load(key: String): String? = withContext(Dispatchers.IO) {
        try {
            val value = sharedPreferences.getString(key, null) ?: return@withContext null
            
            if (value.startsWith("FILE:")) {
                val filePath = value.removePrefix("FILE:")
                val file = File(filePath)
                if (file.exists()) {
                    file.readText()
                } else {
                    null
                }
            } else {
                value
            }
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun delete(key: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val value = sharedPreferences.getString(key, null)
            
            if (value?.startsWith("FILE:") == true) {
                val filePath = value.removePrefix("FILE:")
                val file = File(filePath)
                if (file.exists()) {
                    file.delete()
                }
            }
            
            sharedPreferences.edit().remove(key).apply()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun exists(key: String): Boolean = withContext(Dispatchers.IO) {
        sharedPreferences.contains(key)
    }
    
    override suspend fun listKeys(prefix: String): List<String> = withContext(Dispatchers.IO) {
        try {
            sharedPreferences.all.keys.filter { it.startsWith(prefix) }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun clear(): Boolean = withContext(Dispatchers.IO) {
        try {
            // 清理所有文件
            filesDir.listFiles()?.forEach { file ->
                file.delete()
            }
            
            // 清理SharedPreferences
            sharedPreferences.edit().clear().apply()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun getSize(key: String): Long = withContext(Dispatchers.IO) {
        try {
            val value = sharedPreferences.getString(key, null) ?: return@withContext 0L
            
            if (value.startsWith("FILE:")) {
                val filePath = value.removePrefix("FILE:")
                val file = File(filePath)
                if (file.exists()) file.length() else 0L
            } else {
                value.toByteArray().size.toLong()
            }
        } catch (e: Exception) {
            0L
        }
    }
    
    override suspend fun getTotalSize(): Long = withContext(Dispatchers.IO) {
        try {
            var totalSize = 0L
            
            // 计算文件大小
            filesDir.listFiles()?.forEach { file ->
                totalSize += file.length()
            }
            
            // 计算SharedPreferences大小（估算）
            sharedPreferences.all.values.forEach { value ->
                if (value is String && !value.startsWith("FILE:")) {
                    totalSize += value.toByteArray().size
                }
            }
            
            totalSize
        } catch (e: Exception) {
            0L
        }
    }
}
