package com.unify.core.dynamic

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.w3c.dom.Storage
import kotlinx.browser.localStorage
import kotlinx.browser.window

/**
 * Web平台存储适配器实现
 */
actual class PlatformStorageAdapter : StorageAdapter {
    
    private val storage: Storage = localStorage
    private val indexedDBSupported = js("typeof indexedDB !== 'undefined'") as Boolean
    
    companion object {
        private const val KEY_PREFIX = "unify_dynamic_"
        private const val FILE_PREFIX = "FILE:"
        private const val SIZE_THRESHOLD = 8192 // 8KB
    }
    
    override suspend fun save(key: String, data: String): Boolean = withContext(Dispatchers.Default) {
        try {
            val storageKey = "$KEY_PREFIX$key"
            
            // 对于大数据尝试使用IndexedDB，否则使用localStorage
            if (data.length > SIZE_THRESHOLD && indexedDBSupported) {
                val success = saveToIndexedDB(key, data)
                if (success) {
                    storage.setItem(storageKey, "$FILE_PREFIX$key")
                    return@withContext true
                }
            }
            
            // 使用localStorage
            storage.setItem(storageKey, data)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun load(key: String): String? = withContext(Dispatchers.Default) {
        try {
            val storageKey = "$KEY_PREFIX$key"
            val value = storage.getItem(storageKey) ?: return@withContext null
            
            if (value.startsWith(FILE_PREFIX)) {
                val fileKey = value.removePrefix(FILE_PREFIX)
                loadFromIndexedDB(fileKey)
            } else {
                value
            }
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun delete(key: String): Boolean = withContext(Dispatchers.Default) {
        try {
            val storageKey = "$KEY_PREFIX$key"
            val value = storage.getItem(storageKey)
            
            if (value?.startsWith(FILE_PREFIX) == true) {
                val fileKey = value.removePrefix(FILE_PREFIX)
                deleteFromIndexedDB(fileKey)
            }
            
            storage.removeItem(storageKey)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun exists(key: String): Boolean = withContext(Dispatchers.Default) {
        val storageKey = "$KEY_PREFIX$key"
        storage.getItem(storageKey) != null
    }
    
    override suspend fun listKeys(prefix: String): List<String> = withContext(Dispatchers.Default) {
        try {
            val keys = mutableListOf<String>()
            val fullPrefix = "$KEY_PREFIX$prefix"
            
            for (i in 0 until storage.length) {
                val key = storage.key(i)
                if (key != null && key.startsWith(fullPrefix)) {
                    keys.add(key.removePrefix(KEY_PREFIX))
                }
            }
            
            keys
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun clear(): Boolean = withContext(Dispatchers.Default) {
        try {
            val keysToRemove = mutableListOf<String>()
            
            // 收集所有需要删除的键
            for (i in 0 until storage.length) {
                val key = storage.key(i)
                if (key != null && key.startsWith(KEY_PREFIX)) {
                    keysToRemove.add(key)
                }
            }
            
            // 删除所有键
            keysToRemove.forEach { key ->
                val value = storage.getItem(key)
                if (value?.startsWith(FILE_PREFIX) == true) {
                    val fileKey = value.removePrefix(FILE_PREFIX)
                    deleteFromIndexedDB(fileKey)
                }
                storage.removeItem(key)
            }
            
            // 清理IndexedDB
            if (indexedDBSupported) {
                clearIndexedDB()
            }
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun getSize(key: String): Long = withContext(Dispatchers.Default) {
        try {
            val storageKey = "$KEY_PREFIX$key"
            val value = storage.getItem(storageKey) ?: return@withContext 0L
            
            if (value.startsWith(FILE_PREFIX)) {
                val fileKey = value.removePrefix(FILE_PREFIX)
                getIndexedDBSize(fileKey)
            } else {
                value.encodeToByteArray().size.toLong()
            }
        } catch (e: Exception) {
            0L
        }
    }
    
    override suspend fun getTotalSize(): Long = withContext(Dispatchers.Default) {
        try {
            var totalSize = 0L
            
            for (i in 0 until storage.length) {
                val key = storage.key(i)
                if (key != null && key.startsWith(KEY_PREFIX)) {
                    val originalKey = key.removePrefix(KEY_PREFIX)
                    totalSize += getSize(originalKey)
                }
            }
            
            totalSize
        } catch (e: Exception) {
            0L
        }
    }
    
    // IndexedDB操作的JavaScript包装
    private suspend fun saveToIndexedDB(key: String, data: String): Boolean {
        return try {
            js("""
                return new Promise((resolve) => {
                    const request = indexedDB.open('UnifyDynamicStorage', 1);
                    
                    request.onupgradeneeded = function(event) {
                        const db = event.target.result;
                        if (!db.objectStoreNames.contains('components')) {
                            db.createObjectStore('components', { keyPath: 'key' });
                        }
                    };
                    
                    request.onsuccess = function(event) {
                        const db = event.target.result;
                        const transaction = db.transaction(['components'], 'readwrite');
                        const store = transaction.objectStore('components');
                        
                        store.put({ key: key, data: data, timestamp: Date.now() });
                        
                        transaction.oncomplete = function() {
                            resolve(true);
                        };
                        
                        transaction.onerror = function() {
                            resolve(false);
                        };
                    };
                    
                    request.onerror = function() {
                        resolve(false);
                    };
                });
            """) as Boolean
        } catch (e: Exception) {
            false
        }
    }
    
    private suspend fun loadFromIndexedDB(key: String): String? {
        return try {
            js("""
                return new Promise((resolve) => {
                    const request = indexedDB.open('UnifyDynamicStorage', 1);
                    
                    request.onsuccess = function(event) {
                        const db = event.target.result;
                        const transaction = db.transaction(['components'], 'readonly');
                        const store = transaction.objectStore('components');
                        const getRequest = store.get(key);
                        
                        getRequest.onsuccess = function() {
                            const result = getRequest.result;
                            resolve(result ? result.data : null);
                        };
                        
                        getRequest.onerror = function() {
                            resolve(null);
                        };
                    };
                    
                    request.onerror = function() {
                        resolve(null);
                    };
                });
            """) as String?
        } catch (e: Exception) {
            null
        }
    }
    
    private suspend fun deleteFromIndexedDB(key: String): Boolean {
        return try {
            js("""
                return new Promise((resolve) => {
                    const request = indexedDB.open('UnifyDynamicStorage', 1);
                    
                    request.onsuccess = function(event) {
                        const db = event.target.result;
                        const transaction = db.transaction(['components'], 'readwrite');
                        const store = transaction.objectStore('components');
                        
                        store.delete(key);
                        
                        transaction.oncomplete = function() {
                            resolve(true);
                        };
                        
                        transaction.onerror = function() {
                            resolve(false);
                        };
                    };
                    
                    request.onerror = function() {
                        resolve(false);
                    };
                });
            """) as Boolean
        } catch (e: Exception) {
            false
        }
    }
    
    private suspend fun getIndexedDBSize(key: String): Long {
        return try {
            js("""
                return new Promise((resolve) => {
                    const request = indexedDB.open('UnifyDynamicStorage', 1);
                    
                    request.onsuccess = function(event) {
                        const db = event.target.result;
                        const transaction = db.transaction(['components'], 'readonly');
                        const store = transaction.objectStore('components');
                        const getRequest = store.get(key);
                        
                        getRequest.onsuccess = function() {
                            const result = getRequest.result;
                            if (result && result.data) {
                                resolve(new Blob([result.data]).size);
                            } else {
                                resolve(0);
                            }
                        };
                        
                        getRequest.onerror = function() {
                            resolve(0);
                        };
                    };
                    
                    request.onerror = function() {
                        resolve(0);
                    };
                });
            """) as Long
        } catch (e: Exception) {
            0L
        }
    }
    
    private suspend fun clearIndexedDB(): Boolean {
        return try {
            js("""
                return new Promise((resolve) => {
                    const request = indexedDB.open('UnifyDynamicStorage', 1);
                    
                    request.onsuccess = function(event) {
                        const db = event.target.result;
                        const transaction = db.transaction(['components'], 'readwrite');
                        const store = transaction.objectStore('components');
                        
                        store.clear();
                        
                        transaction.oncomplete = function() {
                            resolve(true);
                        };
                        
                        transaction.onerror = function() {
                            resolve(false);
                        };
                    };
                    
                    request.onerror = function() {
                        resolve(false);
                    };
                });
            """) as Boolean
        } catch (e: Exception) {
            false
        }
    }
}
