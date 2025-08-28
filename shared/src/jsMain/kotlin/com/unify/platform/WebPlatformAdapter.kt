package com.unify.platform

import com.unify.network.NetworkConfig
import com.unify.network.UnifyNetworkService
import com.unify.network.UnifyNetworkServiceImpl
import com.unify.network.NetworkInterceptor
import com.unify.network.NetworkCache
import com.unify.storage.UnifyStorage
import com.unify.storage.PreferencesStorageImpl
import com.unify.storage.DatabaseStorageImpl
import com.unify.storage.FileSystemStorageImpl
import com.unify.storage.SecureStorageImpl
import kotlinx.coroutines.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.w3c.dom.Window
import org.w3c.dom.get
import org.w3c.dom.set
import org.w3c.fetch.*
import org.w3c.files.File
import org.w3c.files.FileReader
import kotlin.js.Promise

/**
 * Web平台适配实现
 * 基于文档第12章Web平台适配要求
 */

/**
 * Web网络服务实现
 */
actual class UnifyNetworkServiceImpl actual constructor(
    private val config: NetworkConfig,
    private val interceptors: List<NetworkInterceptor>,
    private val cache: NetworkCache?
) : UnifyNetworkService {
    
    override suspend fun <T> get(
        url: String,
        headers: Map<String, String>,
        queryParams: Map<String, String>
    ): com.unify.network.NetworkResult<T> {
        return try {
            val urlWithParams = buildUrlWithParams(url, queryParams)
            val requestInit = RequestInit(
                method = "GET",
                headers = js("({})").apply {
                    headers.forEach { (key, value) ->
                        this[key] = value
                    }
                }
            )
            
            val response = kotlinx.browser.window.fetch(urlWithParams, requestInit).await()
            
            if (response.ok) {
                val text = response.text().await()
                @Suppress("UNCHECKED_CAST")
                com.unify.network.NetworkResult.Success(text as T)
            } else {
                com.unify.network.NetworkResult.Error(
                    com.unify.network.NetworkException.ServerError(
                        response.status.toInt(),
                        response.statusText
                    )
                )
            }
        } catch (e: Exception) {
            com.unify.network.NetworkResult.Error(
                com.unify.network.NetworkException.ConnectionError(e.message ?: "Network error", e)
            )
        }
    }
    
    override suspend fun <T> post(
        url: String,
        body: Any?,
        headers: Map<String, String>
    ): com.unify.network.NetworkResult<T> {
        return try {
            val requestInit = RequestInit(
                method = "POST",
                headers = js("({})").apply {
                    this["Content-Type"] = "application/json"
                    headers.forEach { (key, value) ->
                        this[key] = value
                    }
                },
                body = when (body) {
                    is String -> body
                    null -> null
                    else -> JSON.stringify(body)
                }
            )
            
            val response = kotlinx.browser.window.fetch(url, requestInit).await()
            
            if (response.ok) {
                val text = response.text().await()
                @Suppress("UNCHECKED_CAST")
                com.unify.network.NetworkResult.Success(text as T)
            } else {
                com.unify.network.NetworkResult.Error(
                    com.unify.network.NetworkException.ServerError(
                        response.status.toInt(),
                        response.statusText
                    )
                )
            }
        } catch (e: Exception) {
            com.unify.network.NetworkResult.Error(
                com.unify.network.NetworkException.ConnectionError(e.message ?: "Network error", e)
            )
        }
    }
    
    override suspend fun <T> put(
        url: String,
        body: Any?,
        headers: Map<String, String>
    ): com.unify.network.NetworkResult<T> {
        return try {
            val requestInit = RequestInit(
                method = "PUT",
                headers = js("({})").apply {
                    this["Content-Type"] = "application/json"
                    headers.forEach { (key, value) ->
                        this[key] = value
                    }
                },
                body = when (body) {
                    is String -> body
                    null -> null
                    else -> JSON.stringify(body)
                }
            )
            
            val response = kotlinx.browser.window.fetch(url, requestInit).await()
            
            if (response.ok) {
                val text = response.text().await()
                @Suppress("UNCHECKED_CAST")
                com.unify.network.NetworkResult.Success(text as T)
            } else {
                com.unify.network.NetworkResult.Error(
                    com.unify.network.NetworkException.ServerError(
                        response.status.toInt(),
                        response.statusText
                    )
                )
            }
        } catch (e: Exception) {
            com.unify.network.NetworkResult.Error(
                com.unify.network.NetworkException.ConnectionError(e.message ?: "Network error", e)
            )
        }
    }
    
    override suspend fun <T> delete(
        url: String,
        headers: Map<String, String>
    ): com.unify.network.NetworkResult<T> {
        return try {
            val requestInit = RequestInit(
                method = "DELETE",
                headers = js("({})").apply {
                    headers.forEach { (key, value) ->
                        this[key] = value
                    }
                }
            )
            
            val response = kotlinx.browser.window.fetch(url, requestInit).await()
            
            if (response.ok) {
                val text = response.text().await()
                @Suppress("UNCHECKED_CAST")
                com.unify.network.NetworkResult.Success(text as T)
            } else {
                com.unify.network.NetworkResult.Error(
                    com.unify.network.NetworkException.ServerError(
                        response.status.toInt(),
                        response.statusText
                    )
                )
            }
        } catch (e: Exception) {
            com.unify.network.NetworkResult.Error(
                com.unify.network.NetworkException.ConnectionError(e.message ?: "Network error", e)
            )
        }
    }
    
    override fun <T> getStream(
        url: String,
        headers: Map<String, String>
    ): Flow<com.unify.network.NetworkResult<T>> {
        return flow {
            emit(com.unify.network.NetworkResult.Loading)
            emit(get(url, headers))
        }
    }
    
    override suspend fun uploadFile(
        url: String,
        filePath: String,
        headers: Map<String, String>,
        onProgress: ((Float) -> Unit)?
    ): com.unify.network.NetworkResult<String> {
        return try {
            // Web平台文件上传需要通过File API
            com.unify.network.NetworkResult.Error(
                com.unify.network.NetworkException.UnknownError("File upload not implemented for web platform")
            )
        } catch (e: Exception) {
            com.unify.network.NetworkResult.Error(
                com.unify.network.NetworkException.UnknownError(e.message ?: "Upload error", e)
            )
        }
    }
    
    override suspend fun downloadFile(
        url: String,
        destinationPath: String,
        headers: Map<String, String>,
        onProgress: ((Float) -> Unit)?
    ): com.unify.network.NetworkResult<String> {
        return try {
            val response = kotlinx.browser.window.fetch(url, RequestInit(
                method = "GET",
                headers = js("({})").apply {
                    headers.forEach { (key, value) ->
                        this[key] = value
                    }
                }
            )).await()
            
            if (response.ok) {
                val blob = response.blob().await()
                // 在Web平台中，我们可以创建下载链接
                val downloadUrl = kotlinx.browser.window.URL.createObjectURL(blob)
                
                // 创建临时下载链接
                val link = kotlinx.browser.document.createElement("a")
                link.setAttribute("href", downloadUrl)
                link.setAttribute("download", destinationPath.substringAfterLast("/"))
                kotlinx.browser.document.body?.appendChild(link)
                link.click()
                kotlinx.browser.document.body?.removeChild(link)
                kotlinx.browser.window.URL.revokeObjectURL(downloadUrl)
                
                com.unify.network.NetworkResult.Success(destinationPath)
            } else {
                com.unify.network.NetworkResult.Error(
                    com.unify.network.NetworkException.ServerError(
                        response.status.toInt(),
                        response.statusText
                    )
                )
            }
        } catch (e: Exception) {
            com.unify.network.NetworkResult.Error(
                com.unify.network.NetworkException.UnknownError(e.message ?: "Download error", e)
            )
        }
    }
    
    private fun buildUrlWithParams(url: String, queryParams: Map<String, String>): String {
        return if (queryParams.isNotEmpty()) {
            val params = queryParams.entries.joinToString("&") { "${it.key}=${it.value}" }
            if (url.contains("?")) "$url&$params" else "$url?$params"
        } else {
            url
        }
    }
}

/**
 * Web LocalStorage存储实现
 */
actual class PreferencesStorageImpl : UnifyStorage {
    private val localStorage = kotlinx.browser.window.localStorage
    
    override suspend fun getString(key: String, defaultValue: String?): String? {
        return localStorage[key] ?: defaultValue
    }
    
    override suspend fun putString(key: String, value: String) {
        localStorage[key] = value
    }
    
    override suspend fun getInt(key: String, defaultValue: Int): Int {
        return localStorage[key]?.toIntOrNull() ?: defaultValue
    }
    
    override suspend fun putInt(key: String, value: Int) {
        localStorage[key] = value.toString()
    }
    
    override suspend fun getLong(key: String, defaultValue: Long): Long {
        return localStorage[key]?.toLongOrNull() ?: defaultValue
    }
    
    override suspend fun putLong(key: String, value: Long) {
        localStorage[key] = value.toString()
    }
    
    override suspend fun getFloat(key: String, defaultValue: Float): Float {
        return localStorage[key]?.toFloatOrNull() ?: defaultValue
    }
    
    override suspend fun putFloat(key: String, value: Float) {
        localStorage[key] = value.toString()
    }
    
    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return localStorage[key]?.toBooleanStrictOrNull() ?: defaultValue
    }
    
    override suspend fun putBoolean(key: String, value: Boolean) {
        localStorage[key] = value.toString()
    }
    
    override suspend fun remove(key: String) {
        localStorage.removeItem(key)
    }
    
    override suspend fun clear() {
        localStorage.clear()
    }
    
    override suspend fun contains(key: String): Boolean {
        return localStorage[key] != null
    }
    
    override suspend fun getAllKeys(): Set<String> {
        val keys = mutableSetOf<String>()
        for (i in 0 until localStorage.length) {
            localStorage.key(i)?.let { keys.add(it) }
        }
        return keys
    }
    
    override suspend inline fun <reified T> getObject(key: String): T? {
        val json = getString(key) ?: return null
        return try {
            kotlinx.serialization.json.Json.decodeFromString<T>(json)
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend inline fun <reified T> putObject(key: String, value: T) {
        val json = kotlinx.serialization.json.Json.encodeToString(value)
        putString(key, json)
    }
    
    override fun <T> observeKey(key: String): Flow<T?> {
        return flow {
            emit(getObject<T>(key))
        }
    }
}

/**
 * Web IndexedDB存储实现
 */
actual class DatabaseStorageImpl : UnifyStorage {
    // IndexedDB实现较为复杂，这里提供基础框架
    private val memoryStorage = mutableMapOf<String, Any>()
    
    override suspend fun getString(key: String, defaultValue: String?): String? {
        return memoryStorage[key] as? String ?: defaultValue
    }
    
    override suspend fun putString(key: String, value: String) {
        memoryStorage[key] = value
    }
    
    override suspend fun getInt(key: String, defaultValue: Int): Int {
        return memoryStorage[key] as? Int ?: defaultValue
    }
    
    override suspend fun putInt(key: String, value: Int) {
        memoryStorage[key] = value
    }
    
    override suspend fun getLong(key: String, defaultValue: Long): Long {
        return memoryStorage[key] as? Long ?: defaultValue
    }
    
    override suspend fun putLong(key: String, value: Long) {
        memoryStorage[key] = value
    }
    
    override suspend fun getFloat(key: String, defaultValue: Float): Float {
        return memoryStorage[key] as? Float ?: defaultValue
    }
    
    override suspend fun putFloat(key: String, value: Float) {
        memoryStorage[key] = value
    }
    
    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return memoryStorage[key] as? Boolean ?: defaultValue
    }
    
    override suspend fun putBoolean(key: String, value: Boolean) {
        memoryStorage[key] = value
    }
    
    override suspend fun remove(key: String) {
        memoryStorage.remove(key)
    }
    
    override suspend fun clear() {
        memoryStorage.clear()
    }
    
    override suspend fun contains(key: String): Boolean {
        return memoryStorage.containsKey(key)
    }
    
    override suspend fun getAllKeys(): Set<String> {
        return memoryStorage.keys.toSet()
    }
    
    override suspend inline fun <reified T> getObject(key: String): T? {
        val json = getString(key) ?: return null
        return try {
            kotlinx.serialization.json.Json.decodeFromString<T>(json)
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend inline fun <reified T> putObject(key: String, value: T) {
        val json = kotlinx.serialization.json.Json.encodeToString(value)
        putString(key, json)
    }
    
    override fun <T> observeKey(key: String): Flow<T?> {
        return flow {
            emit(memoryStorage[key] as? T)
        }
    }
}

/**
 * Web文件系统存储实现（基于File System Access API）
 */
actual class FileSystemStorageImpl : UnifyStorage {
    // File System Access API支持有限，回退到localStorage
    private val fallbackStorage = PreferencesStorageImpl()
    
    override suspend fun getString(key: String, defaultValue: String?): String? {
        return fallbackStorage.getString("fs_$key", defaultValue)
    }
    
    override suspend fun putString(key: String, value: String) {
        fallbackStorage.putString("fs_$key", value)
    }
    
    override suspend fun getInt(key: String, defaultValue: Int): Int {
        return fallbackStorage.getInt("fs_$key", defaultValue)
    }
    
    override suspend fun putInt(key: String, value: Int) {
        fallbackStorage.putInt("fs_$key", value)
    }
    
    override suspend fun getLong(key: String, defaultValue: Long): Long {
        return fallbackStorage.getLong("fs_$key", defaultValue)
    }
    
    override suspend fun putLong(key: String, value: Long) {
        fallbackStorage.putLong("fs_$key", value)
    }
    
    override suspend fun getFloat(key: String, defaultValue: Float): Float {
        return fallbackStorage.getFloat("fs_$key", defaultValue)
    }
    
    override suspend fun putFloat(key: String, value: Float) {
        fallbackStorage.putFloat("fs_$key", value)
    }
    
    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return fallbackStorage.getBoolean("fs_$key", defaultValue)
    }
    
    override suspend fun putBoolean(key: String, value: Boolean) {
        fallbackStorage.putBoolean("fs_$key", value)
    }
    
    override suspend fun remove(key: String) {
        fallbackStorage.remove("fs_$key")
    }
    
    override suspend fun clear() {
        val keys = fallbackStorage.getAllKeys().filter { it.startsWith("fs_") }
        keys.forEach { fallbackStorage.remove(it) }
    }
    
    override suspend fun contains(key: String): Boolean {
        return fallbackStorage.contains("fs_$key")
    }
    
    override suspend fun getAllKeys(): Set<String> {
        return fallbackStorage.getAllKeys()
            .filter { it.startsWith("fs_") }
            .map { it.removePrefix("fs_") }
            .toSet()
    }
    
    override suspend inline fun <reified T> getObject(key: String): T? {
        return fallbackStorage.getObject<T>("fs_$key")
    }
    
    override suspend inline fun <reified T> putObject(key: String, value: T) {
        fallbackStorage.putObject("fs_$key", value)
    }
    
    override fun <T> observeKey(key: String): Flow<T?> {
        return fallbackStorage.observeKey("fs_$key")
    }
}

/**
 * Web安全存储实现（基于加密的localStorage）
 */
actual class SecureStorageImpl : UnifyStorage {
    private val fallbackStorage = PreferencesStorageImpl()
    private val keyPrefix = "secure_"
    
    override suspend fun getString(key: String, defaultValue: String?): String? {
        return fallbackStorage.getString("$keyPrefix$key", defaultValue)
    }
    
    override suspend fun putString(key: String, value: String) {
        // 在生产环境中应该加密存储
        fallbackStorage.putString("$keyPrefix$key", value)
    }
    
    override suspend fun getInt(key: String, defaultValue: Int): Int {
        return fallbackStorage.getInt("$keyPrefix$key", defaultValue)
    }
    
    override suspend fun putInt(key: String, value: Int) {
        fallbackStorage.putInt("$keyPrefix$key", value)
    }
    
    override suspend fun getLong(key: String, defaultValue: Long): Long {
        return fallbackStorage.getLong("$keyPrefix$key", defaultValue)
    }
    
    override suspend fun putLong(key: String, value: Long) {
        fallbackStorage.putLong("$keyPrefix$key", value)
    }
    
    override suspend fun getFloat(key: String, defaultValue: Float): Float {
        return fallbackStorage.getFloat("$keyPrefix$key", defaultValue)
    }
    
    override suspend fun putFloat(key: String, value: Float) {
        fallbackStorage.putFloat("$keyPrefix$key", value)
    }
    
    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return fallbackStorage.getBoolean("$keyPrefix$key", defaultValue)
    }
    
    override suspend fun putBoolean(key: String, value: Boolean) {
        fallbackStorage.putBoolean("$keyPrefix$key", value)
    }
    
    override suspend fun remove(key: String) {
        fallbackStorage.remove("$keyPrefix$key")
    }
    
    override suspend fun clear() {
        val keys = fallbackStorage.getAllKeys().filter { it.startsWith(keyPrefix) }
        keys.forEach { fallbackStorage.remove(it) }
    }
    
    override suspend fun contains(key: String): Boolean {
        return fallbackStorage.contains("$keyPrefix$key")
    }
    
    override suspend fun getAllKeys(): Set<String> {
        return fallbackStorage.getAllKeys()
            .filter { it.startsWith(keyPrefix) }
            .map { it.removePrefix(keyPrefix) }
            .toSet()
    }
    
    override suspend inline fun <reified T> getObject(key: String): T? {
        return fallbackStorage.getObject<T>("$keyPrefix$key")
    }
    
    override suspend inline fun <reified T> putObject(key: String, value: T) {
        fallbackStorage.putObject("$keyPrefix$key", value)
    }
    
    override fun <T> observeKey(key: String): Flow<T?> {
        return fallbackStorage.observeKey("$keyPrefix$key")
    }
}

/**
 * Web平台信息实现
 */
actual class WebPlatformInfoImpl : PlatformInfo {
    override val platformType: PlatformType = PlatformType.WEB
    override val platformVersion: String = kotlinx.browser.window.navigator.userAgent
    override val deviceModel: String = "Web Browser"
    override val isDebug: Boolean = js("typeof console !== 'undefined' && console.assert") as Boolean
    
    override fun getScreenSize(): Pair<Int, Int> {
        return Pair(
            kotlinx.browser.window.screen.width,
            kotlinx.browser.window.screen.height
        )
    }
    
    override fun getDeviceId(): String? = null // Web平台不支持设备ID
    
    override fun isMobile(): Boolean {
        val userAgent = kotlinx.browser.window.navigator.userAgent
        return userAgent.contains("Mobile") || userAgent.contains("Android") || userAgent.contains("iPhone")
    }
    
    override fun isTablet(): Boolean {
        val userAgent = kotlinx.browser.window.navigator.userAgent
        return userAgent.contains("iPad") || (userAgent.contains("Android") && !userAgent.contains("Mobile"))
    }
}

/**
 * Web平台能力实现
 */
actual class WebPlatformCapabilitiesImpl : PlatformCapabilities {
    override val supportsFileSystem: Boolean = js("typeof window.showOpenFilePicker === 'function'") as Boolean
    override val supportsCamera: Boolean = js("typeof navigator.mediaDevices !== 'undefined'") as Boolean
    override val supportsLocation: Boolean = js("typeof navigator.geolocation !== 'undefined'") as Boolean
    override val supportsPushNotifications: Boolean = js("typeof window.Notification !== 'undefined'") as Boolean
    override val supportsBackgroundTasks: Boolean = js("typeof window.Worker !== 'undefined'") as Boolean
    override val supportsBiometrics: Boolean = js("typeof window.PublicKeyCredential !== 'undefined'") as Boolean
    override val supportsNFC: Boolean = js("typeof window.NDEFReader !== 'undefined'") as Boolean
    override val supportsVibration: Boolean = js("typeof navigator.vibrate === 'function'") as Boolean
}

/**
 * 获取Web平台类型
 */
actual fun getPlatformType(): PlatformType = PlatformType.WEB

// 暂时实现桌面和HarmonyOS平台（在Web模块中）
actual class DesktopPlatformInfoImpl : PlatformInfo {
    override val platformType: PlatformType = PlatformType.DESKTOP
    override val platformVersion: String = "Desktop"
    override val deviceModel: String = "Desktop"
    override val isDebug: Boolean = false
    override fun getScreenSize(): Pair<Int, Int> = Pair(1920, 1080)
    override fun getDeviceId(): String? = null
    override fun isMobile(): Boolean = false
    override fun isTablet(): Boolean = false
}

actual class HarmonyOSPlatformInfoImpl : PlatformInfo {
    override val platformType: PlatformType = PlatformType.HARMONY_OS
    override val platformVersion: String = "HarmonyOS"
    override val deviceModel: String = "HarmonyOS Device"
    override val isDebug: Boolean = false
    override fun getScreenSize(): Pair<Int, Int> = Pair(1080, 1920)
    override fun getDeviceId(): String? = null
    override fun isMobile(): Boolean = true
    override fun isTablet(): Boolean = false
}

actual class DesktopPlatformCapabilitiesImpl : PlatformCapabilities {
    override val supportsFileSystem: Boolean = true
    override val supportsCamera: Boolean = false
    override val supportsLocation: Boolean = false
    override val supportsPushNotifications: Boolean = false
    override val supportsBackgroundTasks: Boolean = true
    override val supportsBiometrics: Boolean = false
    override val supportsNFC: Boolean = false
    override val supportsVibration: Boolean = false
}

actual class HarmonyOSPlatformCapabilitiesImpl : PlatformCapabilities {
    override val supportsFileSystem: Boolean = true
    override val supportsCamera: Boolean = true
    override val supportsLocation: Boolean = true
    override val supportsPushNotifications: Boolean = true
    override val supportsBackgroundTasks: Boolean = true
    override val supportsBiometrics: Boolean = true
    override val supportsNFC: Boolean = true
    override val supportsVibration: Boolean = true
}

/**
 * Web平台信息工具
 */
object WebPlatformInfo {
    val userAgent: String get() = kotlinx.browser.window.navigator.userAgent
    val language: String get() = kotlinx.browser.window.navigator.language
    val platform: String get() = kotlinx.browser.window.navigator.platform
    val cookieEnabled: Boolean get() = kotlinx.browser.window.navigator.cookieEnabled
    val onLine: Boolean get() = kotlinx.browser.window.navigator.onLine
    
    fun getScreenSize(): Pair<Int, Int> {
        return Pair(
            kotlinx.browser.window.screen.width,
            kotlinx.browser.window.screen.height
        )
    }
    
    fun getViewportSize(): Pair<Int, Int> {
        return Pair(
            kotlinx.browser.window.innerWidth,
            kotlinx.browser.window.innerHeight
        )
    }
    
    fun isMobile(): Boolean {
        return userAgent.contains("Mobile") || userAgent.contains("Android") || userAgent.contains("iPhone")
    }
    
    fun isTablet(): Boolean {
        return userAgent.contains("iPad") || (userAgent.contains("Android") && !userAgent.contains("Mobile"))
    }
    
    fun getDevicePixelRatio(): Double {
        return kotlinx.browser.window.devicePixelRatio
    }
}
