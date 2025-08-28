package com.unify.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

actual class PreferencesStorageImpl : UnifyStorage {
    private lateinit var sharedPreferences: SharedPreferences
    
    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences("unify_prefs", Context.MODE_PRIVATE)
    }
    
    override suspend fun getString(key: String, defaultValue: String?): String? = withContext(Dispatchers.IO) {
        sharedPreferences.getString(key, defaultValue)
    }
    
    override suspend fun putString(key: String, value: String) = withContext(Dispatchers.IO) {
        sharedPreferences.edit().putString(key, value).apply()
    }
    
    override suspend fun getInt(key: String, defaultValue: Int): Int = withContext(Dispatchers.IO) {
        sharedPreferences.getInt(key, defaultValue)
    }
    
    override suspend fun putInt(key: String, value: Int) = withContext(Dispatchers.IO) {
        sharedPreferences.edit().putInt(key, value).apply()
    }
    
    override suspend fun getLong(key: String, defaultValue: Long): Long = withContext(Dispatchers.IO) {
        sharedPreferences.getLong(key, defaultValue)
    }
    
    override suspend fun putLong(key: String, value: Long) = withContext(Dispatchers.IO) {
        sharedPreferences.edit().putLong(key, value).apply()
    }
    
    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean = withContext(Dispatchers.IO) {
        sharedPreferences.getBoolean(key, defaultValue)
    }
    
    override suspend fun putBoolean(key: String, value: Boolean) = withContext(Dispatchers.IO) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }
    
    override suspend fun remove(key: String) = withContext(Dispatchers.IO) {
        sharedPreferences.edit().remove(key).apply()
    }
    
    override suspend fun clear() = withContext(Dispatchers.IO) {
        sharedPreferences.edit().clear().apply()
    }
    
    override suspend fun contains(key: String): Boolean = withContext(Dispatchers.IO) {
        sharedPreferences.contains(key)
    }
    
    override suspend fun getAllKeys(): Set<String> = withContext(Dispatchers.IO) {
        sharedPreferences.all.keys
    }
}

actual class DatabaseStorageImpl : UnifyStorage {
    // Room database implementation would go here
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
    private lateinit var filesDir: File
    
    fun initialize(context: Context) {
        filesDir = context.filesDir
    }
    
    override suspend fun getString(key: String, defaultValue: String?): String? = withContext(Dispatchers.IO) {
        try {
            val file = File(filesDir, key)
            if (file.exists()) file.readText() else defaultValue
        } catch (e: Exception) {
            defaultValue
        }
    }
    
    override suspend fun putString(key: String, value: String) = withContext(Dispatchers.IO) {
        try {
            val file = File(filesDir, key)
            file.writeText(value)
        } catch (e: Exception) {
            // Handle error
        }
    }
    
    override suspend fun getInt(key: String, defaultValue: Int): Int = withContext(Dispatchers.IO) {
        getString(key)?.toIntOrNull() ?: defaultValue
    }
    
    override suspend fun putInt(key: String, value: Int) = withContext(Dispatchers.IO) {
        putString(key, value.toString())
    }
    
    override suspend fun getLong(key: String, defaultValue: Long): Long = withContext(Dispatchers.IO) {
        getString(key)?.toLongOrNull() ?: defaultValue
    }
    
    override suspend fun putLong(key: String, value: Long) = withContext(Dispatchers.IO) {
        putString(key, value.toString())
    }
    
    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean = withContext(Dispatchers.IO) {
        getString(key)?.toBooleanStrictOrNull() ?: defaultValue
    }
    
    override suspend fun putBoolean(key: String, value: Boolean) = withContext(Dispatchers.IO) {
        putString(key, value.toString())
    }
    
    override suspend fun remove(key: String) = withContext(Dispatchers.IO) {
        try {
            val file = File(filesDir, key)
            file.delete()
        } catch (e: Exception) {
            // Handle error
        }
    }
    
    override suspend fun clear() = withContext(Dispatchers.IO) {
        try {
            filesDir.listFiles()?.forEach { it.delete() }
        } catch (e: Exception) {
            // Handle error
        }
    }
    
    override suspend fun contains(key: String): Boolean = withContext(Dispatchers.IO) {
        File(filesDir, key).exists()
    }
    
    override suspend fun getAllKeys(): Set<String> = withContext(Dispatchers.IO) {
        try {
            filesDir.listFiles()?.map { it.name }?.toSet() ?: emptySet()
        } catch (e: Exception) {
            emptySet()
        }
    }
}

actual class SecureStorageImpl : UnifyStorage {
    private lateinit var encryptedSharedPreferences: SharedPreferences
    
    fun initialize(context: Context) {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        encryptedSharedPreferences = EncryptedSharedPreferences.create(
            "unify_secure_prefs",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    override suspend fun getString(key: String, defaultValue: String?): String? = withContext(Dispatchers.IO) {
        encryptedSharedPreferences.getString(key, defaultValue)
    }
    
    override suspend fun putString(key: String, value: String) = withContext(Dispatchers.IO) {
        encryptedSharedPreferences.edit().putString(key, value).apply()
    }
    
    override suspend fun getInt(key: String, defaultValue: Int): Int = withContext(Dispatchers.IO) {
        encryptedSharedPreferences.getInt(key, defaultValue)
    }
    
    override suspend fun putInt(key: String, value: Int) = withContext(Dispatchers.IO) {
        encryptedSharedPreferences.edit().putInt(key, value).apply()
    }
    
    override suspend fun getLong(key: String, defaultValue: Long): Long = withContext(Dispatchers.IO) {
        encryptedSharedPreferences.getLong(key, defaultValue)
    }
    
    override suspend fun putLong(key: String, value: Long) = withContext(Dispatchers.IO) {
        encryptedSharedPreferences.edit().putLong(key, value).apply()
    }
    
    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean = withContext(Dispatchers.IO) {
        encryptedSharedPreferences.getBoolean(key, defaultValue)
    }
    
    override suspend fun putBoolean(key: String, value: Boolean) = withContext(Dispatchers.IO) {
        encryptedSharedPreferences.edit().putBoolean(key, value).apply()
    }
    
    override suspend fun remove(key: String) = withContext(Dispatchers.IO) {
        encryptedSharedPreferences.edit().remove(key).apply()
    }
    
    override suspend fun clear() = withContext(Dispatchers.IO) {
        encryptedSharedPreferences.edit().clear().apply()
    }
    
    override suspend fun contains(key: String): Boolean = withContext(Dispatchers.IO) {
        encryptedSharedPreferences.contains(key)
    }
    
    override suspend fun getAllKeys(): Set<String> = withContext(Dispatchers.IO) {
        encryptedSharedPreferences.all.keys
    }
}
