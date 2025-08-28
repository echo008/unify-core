package com.unify.storage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.*
import platform.Security.*

actual class PreferencesStorageImpl : UnifyStorage {
    private val userDefaults = NSUserDefaults.standardUserDefaults
    
    override suspend fun getString(key: String, defaultValue: String?): String? = withContext(Dispatchers.Main) {
        userDefaults.stringForKey(key) ?: defaultValue
    }
    
    override suspend fun putString(key: String, value: String) = withContext(Dispatchers.Main) {
        userDefaults.setObject(value, key)
        userDefaults.synchronize()
    }
    
    override suspend fun getInt(key: String, defaultValue: Int): Int = withContext(Dispatchers.Main) {
        val result = userDefaults.integerForKey(key)
        if (userDefaults.objectForKey(key) != null) result.toInt() else defaultValue
    }
    
    override suspend fun putInt(key: String, value: Int) = withContext(Dispatchers.Main) {
        userDefaults.setInteger(value.toLong(), key)
        userDefaults.synchronize()
    }
    
    override suspend fun getLong(key: String, defaultValue: Long): Long = withContext(Dispatchers.Main) {
        val result = userDefaults.integerForKey(key)
        if (userDefaults.objectForKey(key) != null) result else defaultValue
    }
    
    override suspend fun putLong(key: String, value: Long) = withContext(Dispatchers.Main) {
        userDefaults.setInteger(value, key)
        userDefaults.synchronize()
    }
    
    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean = withContext(Dispatchers.Main) {
        val result = userDefaults.boolForKey(key)
        if (userDefaults.objectForKey(key) != null) result else defaultValue
    }
    
    override suspend fun putBoolean(key: String, value: Boolean) = withContext(Dispatchers.Main) {
        userDefaults.setBool(value, key)
        userDefaults.synchronize()
    }
    
    override suspend fun remove(key: String) = withContext(Dispatchers.Main) {
        userDefaults.removeObjectForKey(key)
        userDefaults.synchronize()
    }
    
    override suspend fun clear() = withContext(Dispatchers.Main) {
        val domain = NSBundle.mainBundle.bundleIdentifier ?: "com.unify.app"
        userDefaults.removePersistentDomainForName(domain)
        userDefaults.synchronize()
    }
    
    override suspend fun contains(key: String): Boolean = withContext(Dispatchers.Main) {
        userDefaults.objectForKey(key) != null
    }
    
    override suspend fun getAllKeys(): Set<String> = withContext(Dispatchers.Main) {
        val domain = NSBundle.mainBundle.bundleIdentifier ?: "com.unify.app"
        val dict = userDefaults.persistentDomainForName(domain)
        dict?.allKeys?.mapNotNull { it as? String }?.toSet() ?: emptySet()
    }
}

actual class DatabaseStorageImpl : UnifyStorage {
    // Core Data implementation would go here
    // For now, delegating to preferences as fallback
    private val prefsImpl = PreferencesStorageImpl()
    
    override suspend fun getString(key: String, defaultValue: String?): String? = 
        prefsImpl.getString(key, defaultValue)
    
    override suspend fun putString(key: String, value: String) = 
        prefsImpl.putString(key, value)
    
    override suspend fun getInt(key: String, defaultValue: Int): Int = 
        prefsImpl.getInt(key, defaultValue)
    
    override suspend fun putInt(key: String, value: Int) = 
        prefsImpl.putInt(key, value)
    
    override suspend fun getLong(key: String, defaultValue: Long): Long = 
        prefsImpl.getLong(key, defaultValue)
    
    override suspend fun putLong(key: String, value: Long) = 
        prefsImpl.putLong(key, value)
    
    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean = 
        prefsImpl.getBoolean(key, defaultValue)
    
    override suspend fun putBoolean(key: String, value: Boolean) = 
        prefsImpl.putBoolean(key, value)
    
    override suspend fun remove(key: String) = prefsImpl.remove(key)
    override suspend fun clear() = prefsImpl.clear()
    override suspend fun contains(key: String): Boolean = prefsImpl.contains(key)
    override suspend fun getAllKeys(): Set<String> = prefsImpl.getAllKeys()
}

actual class FileSystemStorageImpl : UnifyStorage {
    private val documentsPath: String by lazy {
        val paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true)
        paths.firstOrNull() as? String ?: ""
    }
    
    override suspend fun getString(key: String, defaultValue: String?): String? = withContext(Dispatchers.Main) {
        try {
            val filePath = "$documentsPath/$key"
            val fileManager = NSFileManager.defaultManager
            if (fileManager.fileExistsAtPath(filePath)) {
                NSString.stringWithContentsOfFile(filePath, NSUTF8StringEncoding, null) as? String
            } else {
                defaultValue
            }
        } catch (e: Exception) {
            defaultValue
        }
    }
    
    override suspend fun putString(key: String, value: String) = withContext(Dispatchers.Main) {
        try {
            val filePath = "$documentsPath/$key"
            (value as NSString).writeToFile(filePath, true, NSUTF8StringEncoding, null)
        } catch (e: Exception) {
            // Handle error
        }
    }
    
    override suspend fun getInt(key: String, defaultValue: Int): Int = 
        getString(key)?.toIntOrNull() ?: defaultValue
    
    override suspend fun putInt(key: String, value: Int) = 
        putString(key, value.toString())
    
    override suspend fun getLong(key: String, defaultValue: Long): Long = 
        getString(key)?.toLongOrNull() ?: defaultValue
    
    override suspend fun putLong(key: String, value: Long) = 
        putString(key, value.toString())
    
    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean = 
        getString(key)?.toBooleanStrictOrNull() ?: defaultValue
    
    override suspend fun putBoolean(key: String, value: Boolean) = 
        putString(key, value.toString())
    
    override suspend fun remove(key: String) = withContext(Dispatchers.Main) {
        try {
            val filePath = "$documentsPath/$key"
            val fileManager = NSFileManager.defaultManager
            fileManager.removeItemAtPath(filePath, null)
        } catch (e: Exception) {
            // Handle error
        }
    }
    
    override suspend fun clear() = withContext(Dispatchers.Main) {
        try {
            val fileManager = NSFileManager.defaultManager
            val files = fileManager.contentsOfDirectoryAtPath(documentsPath, null)
            files?.forEach { fileName ->
                val filePath = "$documentsPath/$fileName"
                fileManager.removeItemAtPath(filePath, null)
            }
        } catch (e: Exception) {
            // Handle error
        }
    }
    
    override suspend fun contains(key: String): Boolean = withContext(Dispatchers.Main) {
        val filePath = "$documentsPath/$key"
        NSFileManager.defaultManager.fileExistsAtPath(filePath)
    }
    
    override suspend fun getAllKeys(): Set<String> = withContext(Dispatchers.Main) {
        try {
            val fileManager = NSFileManager.defaultManager
            val files = fileManager.contentsOfDirectoryAtPath(documentsPath, null)
            files?.mapNotNull { it as? String }?.toSet() ?: emptySet()
        } catch (e: Exception) {
            emptySet()
        }
    }
}

actual class SecureStorageImpl : UnifyStorage {
    private val service = "com.unify.keychain"
    
    private fun keychainQuery(key: String): NSMutableDictionary {
        return NSMutableDictionary().apply {
            setObject(kSecClassGenericPassword, kSecClass)
            setObject(service, kSecAttrService)
            setObject(key, kSecAttrAccount)
        }
    }
    
    override suspend fun getString(key: String, defaultValue: String?): String? = withContext(Dispatchers.Main) {
        try {
            val query = keychainQuery(key).apply {
                setObject(kSecMatchLimitOne, kSecMatchLimit)
                setObject(true, kSecReturnData)
            }
            
            val result = memScoped {
                val resultPtr = alloc<CFTypeRefVar>()
                val status = SecItemCopyMatching(query, resultPtr.ptr)
                if (status == errSecSuccess) {
                    val data = resultPtr.value as? NSData
                    data?.let { NSString.create(it, NSUTF8StringEncoding) as? String }
                } else {
                    null
                }
            }
            result ?: defaultValue
        } catch (e: Exception) {
            defaultValue
        }
    }
    
    override suspend fun putString(key: String, value: String) = withContext(Dispatchers.Main) {
        try {
            val data = (value as NSString).dataUsingEncoding(NSUTF8StringEncoding)
            val query = keychainQuery(key)
            
            // Try to update first
            val updateQuery = NSMutableDictionary().apply {
                setObject(data!!, kSecValueData)
            }
            
            val updateStatus = SecItemUpdate(query, updateQuery)
            if (updateStatus == errSecItemNotFound) {
                // Item doesn't exist, add it
                query.setObject(data!!, kSecValueData)
                SecItemAdd(query, null)
            }
        } catch (e: Exception) {
            // Handle error
        }
    }
    
    override suspend fun getInt(key: String, defaultValue: Int): Int = 
        getString(key)?.toIntOrNull() ?: defaultValue
    
    override suspend fun putInt(key: String, value: Int) = 
        putString(key, value.toString())
    
    override suspend fun getLong(key: String, defaultValue: Long): Long = 
        getString(key)?.toLongOrNull() ?: defaultValue
    
    override suspend fun putLong(key: String, value: Long) = 
        putString(key, value.toString())
    
    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean = 
        getString(key)?.toBooleanStrictOrNull() ?: defaultValue
    
    override suspend fun putBoolean(key: String, value: Boolean) = 
        putString(key, value.toString())
    
    override suspend fun remove(key: String) = withContext(Dispatchers.Main) {
        try {
            val query = keychainQuery(key)
            SecItemDelete(query)
        } catch (e: Exception) {
            // Handle error
        }
    }
    
    override suspend fun clear() = withContext(Dispatchers.Main) {
        try {
            val query = NSMutableDictionary().apply {
                setObject(kSecClassGenericPassword, kSecClass)
                setObject(service, kSecAttrService)
            }
            SecItemDelete(query)
        } catch (e: Exception) {
            // Handle error
        }
    }
    
    override suspend fun contains(key: String): Boolean = withContext(Dispatchers.Main) {
        try {
            val query = keychainQuery(key).apply {
                setObject(kSecMatchLimitOne, kSecMatchLimit)
            }
            val status = SecItemCopyMatching(query, null)
            status == errSecSuccess
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun getAllKeys(): Set<String> = withContext(Dispatchers.Main) {
        try {
            val query = NSMutableDictionary().apply {
                setObject(kSecClassGenericPassword, kSecClass)
                setObject(service, kSecAttrService)
                setObject(kSecMatchLimitAll, kSecMatchLimit)
                setObject(true, kSecReturnAttributes)
            }
            
            memScoped {
                val resultPtr = alloc<CFTypeRefVar>()
                val status = SecItemCopyMatching(query, resultPtr.ptr)
                if (status == errSecSuccess) {
                    val results = resultPtr.value as? NSArray
                    results?.mapNotNull { item ->
                        (item as? NSDictionary)?.objectForKey(kSecAttrAccount) as? String
                    }?.toSet() ?: emptySet()
                } else {
                    emptySet()
                }
            }
        } catch (e: Exception) {
            emptySet()
        }
    }
}
