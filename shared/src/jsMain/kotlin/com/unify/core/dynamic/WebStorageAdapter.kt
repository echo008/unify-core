package com.unify.core.dynamic

import com.unify.core.storage.StorageAdapter
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.w3c.dom.get
import org.w3c.dom.set

/**
 * Web平台存储适配器实现
 * 基于localStorage和IndexedDB
 */
actual class WebStorageAdapter : StorageAdapter {
    
    actual override suspend fun save(key: String, data: ByteArray): Boolean {
        return try {
            val base64Data = data.encodeBase64()
            localStorage[key] = base64Data
            true
        } catch (e: Exception) {
            false
        }
    }
    
    actual override suspend fun load(key: String): ByteArray? {
        return try {
            val base64Data = localStorage[key] ?: return null
            base64Data.decodeBase64()
        } catch (e: Exception) {
            null
        }
    }
    
    actual override suspend fun delete(key: String): Boolean {
        return try {
            localStorage.removeItem(key)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    actual override suspend fun exists(key: String): Boolean {
        return localStorage[key] != null
    }
    
    actual override suspend fun clear(): Boolean {
        return try {
            localStorage.clear()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    actual override suspend fun getAllKeys(): List<String> {
        return try {
            (0 until localStorage.length).mapNotNull { index ->
                localStorage.key(index)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    actual override suspend fun saveToFile(fileName: String, data: ByteArray): Boolean {
        return try {
            // 使用IndexedDB或者Blob API保存文件
            val blob = createBlob(data)
            saveBlob(fileName, blob)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    actual override suspend fun loadFromFile(fileName: String): ByteArray? {
        return try {
            val blob = loadBlob(fileName) ?: return null
            blobToByteArray(blob)
        } catch (e: Exception) {
            null
        }
    }
    
    actual override suspend fun deleteFile(fileName: String): Boolean {
        return try {
            deleteBlob(fileName)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private fun ByteArray.encodeBase64(): String {
        return window.btoa(this.decodeToString())
    }
    
    private fun String.decodeBase64(): ByteArray {
        return window.atob(this).encodeToByteArray()
    }
    
    private fun createBlob(data: ByteArray): dynamic {
        val arrayBuffer = ArrayBuffer(data.size)
        val view = Int8Array(arrayBuffer)
        data.forEachIndexed { index, byte ->
            view[index] = byte
        }
        return js("new Blob([arrayBuffer])")
    }
    
    private fun saveBlob(fileName: String, blob: dynamic) {
        // 实现Blob保存逻辑
        localStorage["file_$fileName"] = js("URL.createObjectURL(blob)")
    }
    
    private fun loadBlob(fileName: String): dynamic? {
        val url = localStorage["file_$fileName"] ?: return null
        return js("fetch(url).then(response => response.blob())")
    }
    
    private suspend fun blobToByteArray(blob: dynamic): ByteArray {
        val arrayBuffer = blob.arrayBuffer().await()
        val view = Int8Array(arrayBuffer)
        return ByteArray(view.length) { index -> view[index] }
    }
    
    private fun deleteBlob(fileName: String) {
        val url = localStorage["file_$fileName"]
        if (url != null) {
            js("URL.revokeObjectURL(url)")
            localStorage.removeItem("file_$fileName")
        }
    }
}
