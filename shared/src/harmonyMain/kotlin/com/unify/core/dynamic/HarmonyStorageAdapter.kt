package com.unify.core.dynamic

import com.unify.core.storage.StorageAdapter

/**
 * HarmonyOS平台存储适配器实现
 * 基于HarmonyOS分布式数据管理
 */
actual class HarmonyStorageAdapter : StorageAdapter {
    actual override suspend fun save(
        key: String,
        data: ByteArray,
    ): Boolean {
        return try {
            // HarmonyOS分布式存储实现
            val preferences = getPreferences()
            val base64Data = data.encodeBase64()
            preferences.putString(key, base64Data)
            preferences.flush()
            true
        } catch (e: Exception) {
            false
        }
    }

    actual override suspend fun load(key: String): ByteArray? {
        return try {
            val preferences = getPreferences()
            val base64Data = preferences.getString(key, null) ?: return null
            base64Data.decodeBase64()
        } catch (e: Exception) {
            null
        }
    }

    actual override suspend fun delete(key: String): Boolean {
        return try {
            val preferences = getPreferences()
            preferences.delete(key)
            preferences.flush()
            true
        } catch (e: Exception) {
            false
        }
    }

    actual override suspend fun exists(key: String): Boolean {
        return try {
            val preferences = getPreferences()
            preferences.has(key)
        } catch (e: Exception) {
            false
        }
    }

    actual override suspend fun clear(): Boolean {
        return try {
            val preferences = getPreferences()
            preferences.clear()
            preferences.flush()
            true
        } catch (e: Exception) {
            false
        }
    }

    actual override suspend fun getAllKeys(): List<String> {
        return try {
            val preferences = getPreferences()
            preferences.getAll().keys.toList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    actual override suspend fun saveToFile(
        fileName: String,
        data: ByteArray,
    ): Boolean {
        return try {
            val context = getContext()
            val fileDir = context.getFilesDir()
            val file = createFile(fileDir, fileName)
            writeFileBytes(file, data)
            true
        } catch (e: Exception) {
            false
        }
    }

    actual override suspend fun loadFromFile(fileName: String): ByteArray? {
        return try {
            val context = getContext()
            val fileDir = context.getFilesDir()
            val file = getFile(fileDir, fileName)
            if (fileExists(file)) {
                readFileBytes(file)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    actual override suspend fun deleteFile(fileName: String): Boolean {
        return try {
            val context = getContext()
            val fileDir = context.getFilesDir()
            val file = getFile(fileDir, fileName)
            deleteFile(file)
        } catch (e: Exception) {
            false
        }
    }

    // HarmonyOS API封装
    private external fun getPreferences(): Preferences

    private external fun getContext(): Context

    private external interface Preferences {
        fun putString(
            key: String,
            value: String,
        )

        fun getString(
            key: String,
            defaultValue: String?,
        ): String?

        fun delete(key: String)

        fun has(key: String): Boolean

        fun clear()

        fun flush(): Boolean

        fun getAll(): Map<String, Any>
    }

    private external interface Context {
        fun getFilesDir(): FileDir
    }

    private external interface FileDir

    private external fun createFile(
        dir: FileDir,
        fileName: String,
    ): File

    private external fun getFile(
        dir: FileDir,
        fileName: String,
    ): File

    private external fun fileExists(file: File): Boolean

    private external fun writeFileBytes(
        file: File,
        data: ByteArray,
    )

    private external fun readFileBytes(file: File): ByteArray

    private external fun deleteFile(file: File): Boolean

    private external interface File

    private fun ByteArray.encodeBase64(): String {
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
