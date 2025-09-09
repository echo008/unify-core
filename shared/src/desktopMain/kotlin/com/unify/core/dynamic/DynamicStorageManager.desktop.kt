package com.unify.core.dynamic

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.prefs.Preferences

/**
 * Desktop平台存储适配器实现
 */
actual class PlatformStorageAdapter : StorageAdapter {
    private val preferences = Preferences.userNodeForPackage(PlatformStorageAdapter::class.java)
    private val dataDirectory: File

    companion object {
        private const val DATA_DIR_NAME = "unify_dynamic_storage"
        private const val FILE_PREFIX = "FILE:"
        private const val SIZE_THRESHOLD = 8192 // 8KB
    }

    init {
        val userHome = System.getProperty("user.home")
        val appDataDir =
            when {
                System.getProperty("os.name").lowercase().contains("windows") -> {
                    File(System.getenv("APPDATA") ?: "$userHome\\AppData\\Roaming", "UnifyCore")
                }
                System.getProperty("os.name").lowercase().contains("mac") -> {
                    File("$userHome/Library/Application Support/UnifyCore")
                }
                else -> { // Linux and others
                    File("$userHome/.config/UnifyCore")
                }
            }

        dataDirectory = File(appDataDir, DATA_DIR_NAME)
        if (!dataDirectory.exists()) {
            dataDirectory.mkdirs()
        }
    }

    override suspend fun save(
        key: String,
        data: String,
    ): Boolean =
        withContext(Dispatchers.IO) {
            try {
                // 对于大数据使用文件存储，小数据使用Preferences
                if (data.length > SIZE_THRESHOLD) {
                    val fileName = "${key.hashCode()}.dat"
                    val file = File(dataDirectory, fileName)

                    file.writeText(data, Charsets.UTF_8)
                    preferences.put(key, "$FILE_PREFIX$fileName")
                    preferences.flush()
                } else {
                    preferences.put(key, data)
                    preferences.flush()
                }
                true
            } catch (e: Exception) {
                false
            }
        }

    override suspend fun load(key: String): String? =
        withContext(Dispatchers.IO) {
            try {
                val value = preferences.get(key, null) ?: return@withContext null

                if (value.startsWith(FILE_PREFIX)) {
                    val fileName = value.removePrefix(FILE_PREFIX)
                    val file = File(dataDirectory, fileName)

                    if (file.exists()) {
                        file.readText(Charsets.UTF_8)
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

    override suspend fun delete(key: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val value = preferences.get(key, null)

                if (value?.startsWith(FILE_PREFIX) == true) {
                    val fileName = value.removePrefix(FILE_PREFIX)
                    val file = File(dataDirectory, fileName)

                    if (file.exists()) {
                        file.delete()
                    }
                }

                preferences.remove(key)
                preferences.flush()
                true
            } catch (e: Exception) {
                false
            }
        }

    override suspend fun exists(key: String): Boolean =
        withContext(Dispatchers.IO) {
            preferences.get(key, null) != null
        }

    override suspend fun listKeys(prefix: String): List<String> =
        withContext(Dispatchers.IO) {
            try {
                preferences.keys().filter { it.startsWith(prefix) }.toList()
            } catch (e: Exception) {
                emptyList()
            }
        }

    override suspend fun clear(): Boolean =
        withContext(Dispatchers.IO) {
            try {
                // 清理所有文件
                dataDirectory.listFiles()?.forEach { file ->
                    if (file.name.endsWith(".dat")) {
                        file.delete()
                    }
                }

                // 清理Preferences
                val keys = preferences.keys()
                keys.forEach { key ->
                    preferences.remove(key)
                }
                preferences.flush()

                true
            } catch (e: Exception) {
                false
            }
        }

    override suspend fun getSize(key: String): Long =
        withContext(Dispatchers.IO) {
            try {
                val value = preferences.get(key, null) ?: return@withContext 0L

                if (value.startsWith(FILE_PREFIX)) {
                    val fileName = value.removePrefix(FILE_PREFIX)
                    val file = File(dataDirectory, fileName)

                    if (file.exists()) file.length() else 0L
                } else {
                    value.toByteArray(Charsets.UTF_8).size.toLong()
                }
            } catch (e: Exception) {
                0L
            }
        }

    override suspend fun getTotalSize(): Long =
        withContext(Dispatchers.IO) {
            try {
                var totalSize = 0L

                // 计算文件大小
                dataDirectory.listFiles()?.forEach { file ->
                    if (file.name.endsWith(".dat")) {
                        totalSize += file.length()
                    }
                }

                // 计算Preferences大小（估算）
                preferences.keys().forEach { key ->
                    val value = preferences.get(key, "")
                    if (!value.startsWith(FILE_PREFIX)) {
                        totalSize += value.toByteArray(Charsets.UTF_8).size
                    }
                }

                totalSize
            } catch (e: Exception) {
                0L
            }
        }
}
