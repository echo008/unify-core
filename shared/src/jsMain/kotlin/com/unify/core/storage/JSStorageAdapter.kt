package com.unify.core.storage

import org.khronos.webgl.get
import org.khronos.webgl.set

actual class PlatformStorageAdapter : StorageAdapter {
    override suspend fun save(
        key: String,
        data: ByteArray,
    ): Boolean {
        return try {
            val base64 = btoa(data.decodeToString())
            localStorage.setItem(key, base64)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun load(key: String): ByteArray? {
        return try {
            val base64 = localStorage.getItem(key) ?: return null
            atob(base64).encodeToByteArray()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun delete(key: String): Boolean {
        return try {
            localStorage.removeItem(key)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun exists(key: String): Boolean {
        return localStorage.getItem(key) != null
    }

    override suspend fun clear(): Boolean {
        return try {
            localStorage.clear()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getAllKeys(): List<String> {
        return try {
            val keys = mutableListOf<String>()
            for (i in 0 until localStorage.length) {
                localStorage.key(i)?.let { keys.add(it) }
            }
            keys
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun saveToFile(
        fileName: String,
        data: ByteArray,
    ): Boolean {
        return try {
            // 在浏览器环境中，文件存储使用 localStorage 模拟
            val fileKey = "file_$fileName"
            val base64 = btoa(data.decodeToString())
            localStorage.setItem(fileKey, base64)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun loadFromFile(fileName: String): ByteArray? {
        return try {
            val fileKey = "file_$fileName"
            val base64 = localStorage.getItem(fileKey) ?: return null
            atob(base64).encodeToByteArray()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun deleteFile(fileName: String): Boolean {
        return try {
            val fileKey = "file_$fileName"
            localStorage.removeItem(fileKey)
            true
        } catch (e: Exception) {
            false
        }
    }
}

// JavaScript 全局函数声明
external fun btoa(str: String): String

external fun atob(str: String): String

external object localStorage {
    fun setItem(
        key: String,
        value: String,
    )

    fun getItem(key: String): String?

    fun removeItem(key: String)

    fun clear()

    fun key(index: Int): String?

    val length: Int
}
