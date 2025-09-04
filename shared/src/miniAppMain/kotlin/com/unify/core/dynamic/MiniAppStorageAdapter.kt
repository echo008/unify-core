package com.unify.core.dynamic

import com.unify.core.storage.StorageAdapter

/**
 * 小程序平台存储适配器实现
 * 基于小程序存储API
 */
actual class MiniAppStorageAdapter : StorageAdapter {
    
    actual override suspend fun save(key: String, data: ByteArray): Boolean {
        return try {
            // 小程序存储实现
            val base64Data = data.encodeBase64()
            setStorageSync(key, base64Data)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    actual override suspend fun load(key: String): ByteArray? {
        return try {
            val base64Data = getStorageSync(key) ?: return null
            base64Data.decodeBase64()
        } catch (e: Exception) {
            null
        }
    }
    
    actual override suspend fun delete(key: String): Boolean {
        return try {
            removeStorageSync(key)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    actual override suspend fun exists(key: String): Boolean {
        return try {
            getStorageSync(key) != null
        } catch (e: Exception) {
            false
        }
    }
    
    actual override suspend fun clear(): Boolean {
        return try {
            clearStorageSync()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    actual override suspend fun getAllKeys(): List<String> {
        return try {
            getStorageInfoSync().keys
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    actual override suspend fun saveToFile(fileName: String, data: ByteArray): Boolean {
        return try {
            val base64Data = data.encodeBase64()
            writeFileSync(fileName, base64Data)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    actual override suspend fun loadFromFile(fileName: String): ByteArray? {
        return try {
            val base64Data = readFileSync(fileName) ?: return null
            base64Data.decodeBase64()
        } catch (e: Exception) {
            null
        }
    }
    
    actual override suspend fun deleteFile(fileName: String): Boolean {
        return try {
            unlinkSync(fileName)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    // 小程序API封装
    private external fun setStorageSync(key: String, data: String)
    private external fun getStorageSync(key: String): String?
    private external fun removeStorageSync(key: String)
    private external fun clearStorageSync()
    private external fun getStorageInfoSync(): StorageInfo
    private external fun writeFileSync(fileName: String, data: String)
    private external fun readFileSync(fileName: String): String?
    private external fun unlinkSync(fileName: String)
    
    private external interface StorageInfo {
        val keys: List<String>
        val currentSize: Int
        val limitSize: Int
    }
    
    private fun ByteArray.encodeBase64(): String {
        // 简化的Base64编码实现
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
        var result = ""
        var i = 0
        while (i < size) {
            val a = this[i].toInt() and 0xFF
            val b = if (i + 1 < size) this[i + 1].toInt() and 0xFF else 0
            val c = if (i + 2 < size) this[i + 2].toInt() and 0xFF else 0
            
            val bitmap = (a shl 16) or (b shl 8) or c
            
            result += chars[(bitmap shr 18) and 0x3F]
            result += chars[(bitmap shr 12) and 0x3F]
            result += if (i + 1 < size) chars[(bitmap shr 6) and 0x3F] else '='
            result += if (i + 2 < size) chars[bitmap and 0x3F] else '='
            
            i += 3
        }
        return result
    }
    
    private fun String.decodeBase64(): ByteArray {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
        val result = mutableListOf<Byte>()
        var i = 0
        
        while (i < length) {
            val a = chars.indexOf(this[i])
            val b = chars.indexOf(this[i + 1])
            val c = if (this[i + 2] != '=') chars.indexOf(this[i + 2]) else 0
            val d = if (this[i + 3] != '=') chars.indexOf(this[i + 3]) else 0
            
            val bitmap = (a shl 18) or (b shl 12) or (c shl 6) or d
            
            result.add(((bitmap shr 16) and 0xFF).toByte())
            if (this[i + 2] != '=') result.add(((bitmap shr 8) and 0xFF).toByte())
            if (this[i + 3] != '=') result.add((bitmap and 0xFF).toByte())
            
            i += 4
        }
        
        return result.toByteArray()
    }
}
