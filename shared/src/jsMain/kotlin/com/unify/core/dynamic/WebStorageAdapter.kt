package com.unify.core.dynamic

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.browser.localStorage
import kotlinx.browser.window
import org.w3c.dom.get
import org.w3c.dom.set

/**
 * Web平台存储适配器
 * 使用localStorage和IndexedDB进行数据持久化
 */
class WebStorageAdapter : StorageAdapter {
    private val keyPrefix = "unify_dynamic_"
    private val timestampSuffix = "_timestamp"
    
    override suspend fun save(key: String, value: String) {
        withContext(Dispatchers.Main) {
            try {
                val prefixedKey = keyPrefix + key
                
                if (value.length > MAX_LOCALSTORAGE_SIZE) {
                    // 大数据使用IndexedDB存储
                    saveToIndexedDB(prefixedKey, value)
                    
                    // 在localStorage中记录IndexedDB标记
                    localStorage[prefixedKey] = "indexeddb:$prefixedKey"
                    localStorage["$prefixedKey$timestampSuffix"] = System.currentTimeMillis().toString()
                } else {
                    // 小数据直接存储到localStorage
                    localStorage[prefixedKey] = value
                    localStorage["$prefixedKey$timestampSuffix"] = System.currentTimeMillis().toString()
                }
                
                UnifyPerformanceMonitor.recordMetric("web_storage_save", 1.0, "count",
                    mapOf("key" to key, "size" to value.length.toString()))
            } catch (e: Exception) {
                UnifyPerformanceMonitor.recordMetric("web_storage_save_error", 1.0, "count",
                    mapOf("key" to key, "error" to e.message.orEmpty()))
                throw StorageException("Web存储保存失败: $key", e)
            }
        }
    }
    
    override suspend fun load(key: String): String? {
        return withContext(Dispatchers.Main) {
            try {
                val prefixedKey = keyPrefix + key
                val value = localStorage[prefixedKey]
                
                if (value?.startsWith("indexeddb:") == true) {
                    // 从IndexedDB读取
                    val dbKey = value.removePrefix("indexeddb:")
                    loadFromIndexedDB(dbKey)
                } else {
                    value
                }
            } catch (e: Exception) {
                UnifyPerformanceMonitor.recordMetric("web_storage_load_error", 1.0, "count",
                    mapOf("key" to key, "error" to e.message.orEmpty()))
                null
            }
        }
    }
    
    override suspend fun delete(key: String) {
        withContext(Dispatchers.Main) {
            try {
                val prefixedKey = keyPrefix + key
                val value = localStorage[prefixedKey]
                
                if (value?.startsWith("indexeddb:") == true) {
                    // 从IndexedDB删除
                    val dbKey = value.removePrefix("indexeddb:")
                    deleteFromIndexedDB(dbKey)
                }
                
                // 从localStorage删除
                localStorage.removeItem(prefixedKey)
                localStorage.removeItem("$prefixedKey$timestampSuffix")
                
                UnifyPerformanceMonitor.recordMetric("web_storage_delete", 1.0, "count",
                    mapOf("key" to key))
            } catch (e: Exception) {
                UnifyPerformanceMonitor.recordMetric("web_storage_delete_error", 1.0, "count",
                    mapOf("key" to key, "error" to e.message.orEmpty()))
            }
        }
    }
    
    override suspend fun getAllKeys(): List<String> {
        return withContext(Dispatchers.Main) {
            try {
                val keys = mutableListOf<String>()
                
                for (i in 0 until localStorage.length) {
                    val key = localStorage.key(i)
                    if (key?.startsWith(keyPrefix) == true && !key.endsWith(timestampSuffix)) {
                        keys.add(key.removePrefix(keyPrefix))
                    }
                }
                
                keys
            } catch (e: Exception) {
                UnifyPerformanceMonitor.recordMetric("web_storage_get_keys_error", 1.0, "count",
                    mapOf("error" to e.message.orEmpty()))
                emptyList()
            }
        }
    }
    
    override suspend fun getLastModified(key: String): Long? {
        return withContext(Dispatchers.Main) {
            try {
                val prefixedKey = keyPrefix + key
                val timestamp = localStorage["$prefixedKey$timestampSuffix"]
                timestamp?.toLongOrNull()
            } catch (e: Exception) {
                null
            }
        }
    }
    
    override suspend fun getTotalSize(): Long {
        return withContext(Dispatchers.Main) {
            try {
                var totalSize = 0L
                
                // 计算localStorage大小
                for (i in 0 until localStorage.length) {
                    val key = localStorage.key(i)
                    if (key?.startsWith(keyPrefix) == true) {
                        val value = localStorage[key] ?: ""
                        totalSize += (key.length + value.length) * 2 // UTF-16编码
                    }
                }
                
                // IndexedDB大小需要异步计算，这里返回localStorage大小
                totalSize
            } catch (e: Exception) {
                0L
            }
        }
    }
    
    /**
     * 保存到IndexedDB
     */
    private suspend fun saveToIndexedDB(key: String, value: String) {
        return suspendCancellableCoroutine { continuation ->
            try {
                js("""
                    const request = indexedDB.open('UnifyDynamicDB', 1);
                    
                    request.onupgradeneeded = function(event) {
                        const db = event.target.result;
                        if (!db.objectStoreNames.contains('data')) {
                            db.createObjectStore('data');
                        }
                    };
                    
                    request.onsuccess = function(event) {
                        const db = event.target.result;
                        const transaction = db.transaction(['data'], 'readwrite');
                        const store = transaction.objectStore('data');
                        const putRequest = store.put(value, key);
                        
                        putRequest.onsuccess = function() {
                            continuation.resumeWith({value: kotlin.Unit});
                        };
                        
                        putRequest.onerror = function() {
                            continuation.resumeWith({exception: new Error('IndexedDB保存失败')});
                        };
                    };
                    
                    request.onerror = function() {
                        continuation.resumeWith({exception: new Error('IndexedDB打开失败')});
                    };
                """)
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }
    }
    
    /**
     * 从IndexedDB加载
     */
    private suspend fun loadFromIndexedDB(key: String): String? {
        return suspendCancellableCoroutine { continuation ->
            try {
                js("""
                    const request = indexedDB.open('UnifyDynamicDB', 1);
                    
                    request.onsuccess = function(event) {
                        const db = event.target.result;
                        const transaction = db.transaction(['data'], 'readonly');
                        const store = transaction.objectStore('data');
                        const getRequest = store.get(key);
                        
                        getRequest.onsuccess = function() {
                            const result = getRequest.result;
                            continuation.resumeWith({value: result || null});
                        };
                        
                        getRequest.onerror = function() {
                            continuation.resumeWith({value: null});
                        };
                    };
                    
                    request.onerror = function() {
                        continuation.resumeWith({value: null});
                    };
                """)
            } catch (e: Exception) {
                continuation.resumeWith(Result.success(null))
            }
        }
    }
    
    /**
     * 从IndexedDB删除
     */
    private suspend fun deleteFromIndexedDB(key: String) {
        return suspendCancellableCoroutine { continuation ->
            try {
                js("""
                    const request = indexedDB.open('UnifyDynamicDB', 1);
                    
                    request.onsuccess = function(event) {
                        const db = event.target.result;
                        const transaction = db.transaction(['data'], 'readwrite');
                        const store = transaction.objectStore('data');
                        const deleteRequest = store.delete(key);
                        
                        deleteRequest.onsuccess = function() {
                            continuation.resumeWith({value: kotlin.Unit});
                        };
                        
                        deleteRequest.onerror = function() {
                            continuation.resumeWith({value: kotlin.Unit});
                        };
                    };
                    
                    request.onerror = function() {
                        continuation.resumeWith({value: kotlin.Unit});
                    };
                """)
            } catch (e: Exception) {
                continuation.resumeWith(Result.success(Unit))
            }
        }
    }
    
    /**
     * 清理过期数据
     */
    suspend fun cleanupExpiredData(maxAge: Long = 7 * 24 * 60 * 60 * 1000) {
        withContext(Dispatchers.Main) {
            try {
                val currentTime = System.currentTimeMillis()
                var cleanedCount = 0
                
                val keysToDelete = mutableListOf<String>()
                
                for (i in 0 until localStorage.length) {
                    val key = localStorage.key(i)
                    if (key?.startsWith(keyPrefix) == true && key.endsWith(timestampSuffix)) {
                        val timestamp = localStorage[key]?.toLongOrNull() ?: 0L
                        if ((currentTime - timestamp) > maxAge) {
                            val dataKey = key.removeSuffix(timestampSuffix)
                            keysToDelete.add(dataKey.removePrefix(keyPrefix))
                        }
                    }
                }
                
                keysToDelete.forEach { key ->
                    delete(key)
                    cleanedCount++
                }
                
                UnifyPerformanceMonitor.recordMetric("web_storage_cleanup", 1.0, "count",
                    mapOf("cleaned_items" to cleanedCount.toString()))
            } catch (e: Exception) {
                UnifyPerformanceMonitor.recordMetric("web_storage_cleanup_error", 1.0, "count",
                    mapOf("error" to e.message.orEmpty()))
            }
        }
    }
    
    companion object {
        private const val MAX_LOCALSTORAGE_SIZE = 5 * 1024 * 1024 // 5MB
    }
}

/**
 * 创建Web平台存储适配器
 */
actual fun createPlatformStorageAdapter(): StorageAdapter {
    return WebStorageAdapter()
}
