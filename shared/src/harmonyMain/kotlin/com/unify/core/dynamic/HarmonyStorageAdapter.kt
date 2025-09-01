package com.unify.core.dynamic

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * HarmonyOS平台存储适配器
 * 使用HarmonyOS的Preferences和分布式数据管理进行数据持久化
 */
class HarmonyStorageAdapter : StorageAdapter {
    private val preferencesName = "unify_dynamic_prefs"
    private val filesDir = "/data/storage/el2/base/haps/entry/files/unify_dynamic"
    
    init {
        // 初始化存储目录
        initializeStorage()
    }
    
    override suspend fun save(key: String, value: String) {
        withContext(Dispatchers.IO) {
            try {
                if (value.length > MAX_PREFERENCES_SIZE) {
                    // 大数据存储到文件
                    saveToFile(key, value)
                    
                    // 在Preferences中记录文件标记
                    saveToPreferences(key, "file:$key")
                    saveToPreferences("${key}_timestamp", System.currentTimeMillis().toString())
                } else {
                    // 小数据直接存储到Preferences
                    saveToPreferences(key, value)
                    saveToPreferences("${key}_timestamp", System.currentTimeMillis().toString())
                }
                
                UnifyPerformanceMonitor.recordMetric("harmony_storage_save", 1.0, "count",
                    mapOf("key" to key, "size" to value.length.toString()))
            } catch (e: Exception) {
                UnifyPerformanceMonitor.recordMetric("harmony_storage_save_error", 1.0, "count",
                    mapOf("key" to key, "error" to e.message.orEmpty()))
                throw StorageException("HarmonyOS存储保存失败: $key", e)
            }
        }
    }
    
    override suspend fun load(key: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val value = loadFromPreferences(key)
                
                if (value?.startsWith("file:") == true) {
                    // 从文件读取
                    val fileName = value.removePrefix("file:")
                    loadFromFile(fileName)
                } else {
                    value
                }
            } catch (e: Exception) {
                UnifyPerformanceMonitor.recordMetric("harmony_storage_load_error", 1.0, "count",
                    mapOf("key" to key, "error" to e.message.orEmpty()))
                null
            }
        }
    }
    
    override suspend fun delete(key: String) {
        withContext(Dispatchers.IO) {
            try {
                val value = loadFromPreferences(key)
                
                if (value?.startsWith("file:") == true) {
                    // 删除文件
                    val fileName = value.removePrefix("file:")
                    deleteFile(fileName)
                }
                
                // 删除Preferences中的记录
                deleteFromPreferences(key)
                deleteFromPreferences("${key}_timestamp")
                
                UnifyPerformanceMonitor.recordMetric("harmony_storage_delete", 1.0, "count",
                    mapOf("key" to key))
            } catch (e: Exception) {
                UnifyPerformanceMonitor.recordMetric("harmony_storage_delete_error", 1.0, "count",
                    mapOf("key" to key, "error" to e.message.orEmpty()))
            }
        }
    }
    
    override suspend fun getAllKeys(): List<String> {
        return withContext(Dispatchers.IO) {
            try {
                getAllPreferencesKeys()
                    .filter { !it.endsWith("_timestamp") }
            } catch (e: Exception) {
                UnifyPerformanceMonitor.recordMetric("harmony_storage_get_keys_error", 1.0, "count",
                    mapOf("error" to e.message.orEmpty()))
                emptyList()
            }
        }
    }
    
    override suspend fun getLastModified(key: String): Long? {
        return withContext(Dispatchers.IO) {
            try {
                val timestamp = loadFromPreferences("${key}_timestamp")
                timestamp?.toLongOrNull()
            } catch (e: Exception) {
                null
            }
        }
    }
    
    override suspend fun getTotalSize(): Long {
        return withContext(Dispatchers.IO) {
            try {
                var totalSize = 0L
                
                // 计算Preferences大小（估算）
                getAllPreferencesKeys().forEach { key ->
                    val value = loadFromPreferences(key) ?: ""
                    totalSize += (key.length + value.length) * 2 // UTF-16编码
                }
                
                // 计算文件大小
                totalSize += getFilesSize()
                
                totalSize
            } catch (e: Exception) {
                0L
            }
        }
    }
    
    /**
     * 初始化存储
     */
    private fun initializeStorage() {
        // HarmonyOS特定的存储初始化
        js("""
            // 创建存储目录
            try {
                const fs = requireNapi('file.fs');
                fs.mkdirSync('$filesDir', { recursive: true });
            } catch (e) {
                console.log('创建存储目录失败:', e);
            }
        """)
    }
    
    /**
     * 保存到Preferences
     */
    private fun saveToPreferences(key: String, value: String) {
        js("""
            try {
                const preferences = requireNapi('data.preferences');
                const context = globalThis.getContext();
                const prefs = preferences.getPreferences(context, '$preferencesName');
                prefs.put(key, value);
                prefs.flush();
            } catch (e) {
                throw new Error('HarmonyOS Preferences保存失败: ' + e.message);
            }
        """)
    }
    
    /**
     * 从Preferences加载
     */
    private fun loadFromPreferences(key: String): String? {
        return try {
            js("""
                try {
                    const preferences = requireNapi('data.preferences');
                    const context = globalThis.getContext();
                    const prefs = preferences.getPreferences(context, '$preferencesName');
                    return prefs.get(key, null);
                } catch (e) {
                    return null;
                }
            """) as? String
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 从Preferences删除
     */
    private fun deleteFromPreferences(key: String) {
        js("""
            try {
                const preferences = requireNapi('data.preferences');
                const context = globalThis.getContext();
                const prefs = preferences.getPreferences(context, '$preferencesName');
                prefs.delete(key);
                prefs.flush();
            } catch (e) {
                console.log('HarmonyOS Preferences删除失败:', e);
            }
        """)
    }
    
    /**
     * 获取所有Preferences键
     */
    private fun getAllPreferencesKeys(): List<String> {
        return try {
            val keysArray = js("""
                try {
                    const preferences = requireNapi('data.preferences');
                    const context = globalThis.getContext();
                    const prefs = preferences.getPreferences(context, '$preferencesName');
                    return prefs.getAll().keys();
                } catch (e) {
                    return [];
                }
            """) as Array<String>
            keysArray.toList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * 保存到文件
     */
    private fun saveToFile(fileName: String, content: String) {
        js("""
            try {
                const fs = requireNapi('file.fs');
                const filePath = '$filesDir/' + fileName;
                fs.writeFileSync(filePath, content, { encoding: 'utf8' });
            } catch (e) {
                throw new Error('HarmonyOS文件保存失败: ' + e.message);
            }
        """)
    }
    
    /**
     * 从文件加载
     */
    private fun loadFromFile(fileName: String): String? {
        return try {
            js("""
                try {
                    const fs = requireNapi('file.fs');
                    const filePath = '$filesDir/' + fileName;
                    if (fs.accessSync(filePath)) {
                        return fs.readFileSync(filePath, { encoding: 'utf8' });
                    }
                    return null;
                } catch (e) {
                    return null;
                }
            """) as? String
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 删除文件
     */
    private fun deleteFile(fileName: String) {
        js("""
            try {
                const fs = requireNapi('file.fs');
                const filePath = '$filesDir/' + fileName;
                if (fs.accessSync(filePath)) {
                    fs.unlinkSync(filePath);
                }
            } catch (e) {
                console.log('HarmonyOS文件删除失败:', e);
            }
        """)
    }
    
    /**
     * 获取文件总大小
     */
    private fun getFilesSize(): Long {
        return try {
            val size = js("""
                try {
                    const fs = requireNapi('file.fs');
                    let totalSize = 0;
                    const files = fs.readdirSync('$filesDir');
                    files.forEach(file => {
                        const filePath = '$filesDir/' + file;
                        const stats = fs.statSync(filePath);
                        if (stats.isFile()) {
                            totalSize += stats.size;
                        }
                    });
                    return totalSize;
                } catch (e) {
                    return 0;
                }
            """) as? Number
            size?.toLong() ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
    
    /**
     * 清理过期文件
     */
    suspend fun cleanupExpiredFiles(maxAge: Long = 7 * 24 * 60 * 60 * 1000) {
        withContext(Dispatchers.IO) {
            try {
                val currentTime = System.currentTimeMillis()
                var cleanedCount = 0
                
                val cleanedFiles = js("""
                    try {
                        const fs = requireNapi('file.fs');
                        let cleaned = 0;
                        const files = fs.readdirSync('$filesDir');
                        files.forEach(file => {
                            const filePath = '$filesDir/' + file;
                            const stats = fs.statSync(filePath);
                            if (stats.isFile()) {
                                const fileAge = $currentTime - stats.mtime.getTime();
                                if (fileAge > $maxAge) {
                                    fs.unlinkSync(filePath);
                                    cleaned++;
                                }
                            }
                        });
                        return cleaned;
                    } catch (e) {
                        return 0;
                    }
                """) as? Number
                
                cleanedCount = cleanedFiles?.toInt() ?: 0
                
                UnifyPerformanceMonitor.recordMetric("harmony_storage_cleanup", 1.0, "count",
                    mapOf("cleaned_files" to cleanedCount.toString()))
            } catch (e: Exception) {
                UnifyPerformanceMonitor.recordMetric("harmony_storage_cleanup_error", 1.0, "count",
                    mapOf("error" to e.message.orEmpty()))
            }
        }
    }
    
    /**
     * 同步到分布式数据管理
     */
    suspend fun syncToDistributedData(key: String, value: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val success = js("""
                    try {
                        const distributedData = requireNapi('data.distributedData');
                        const kvManager = distributedData.createKVManager({
                            context: globalThis.getContext(),
                            bundleName: 'com.unify.app'
                        });
                        const kvStore = kvManager.getKVStore('unify_dynamic_store');
                        kvStore.put(key, value);
                        return true;
                    } catch (e) {
                        console.log('HarmonyOS分布式数据同步失败:', e);
                        return false;
                    }
                """) as? Boolean
                
                success ?: false
            } catch (e: Exception) {
                UnifyPerformanceMonitor.recordMetric("harmony_distributed_sync_error", 1.0, "count",
                    mapOf("key" to key, "error" to e.message.orEmpty()))
                false
            }
        }
    }
    
    companion object {
        private const val MAX_PREFERENCES_SIZE = 1024 * 1024 // 1MB
    }
}

/**
 * 创建HarmonyOS平台存储适配器
 */
actual fun createPlatformStorageAdapter(): StorageAdapter {
    return HarmonyStorageAdapter()
}
