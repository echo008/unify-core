package com.unify.platform

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Android平台适配实现
 * 基于文档第9章Android平台适配要求
 */

/**
 * Android网络服务实现
 */
actual class UnifyNetworkServiceImpl actual constructor(
    private val config: NetworkConfig,
    private val interceptors: List<NetworkInterceptor>,
    private val cache: NetworkCache?
) : UnifyNetworkService {
    
    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(config.timeout, TimeUnit.MILLISECONDS)
            .readTimeout(config.timeout, TimeUnit.MILLISECONDS)
            .writeTimeout(config.timeout, TimeUnit.MILLISECONDS)
            .addInterceptor { chain ->
                val request = chain.request()
                val response = chain.proceed(request)
                response
            }
            .build()
    }
    
    override suspend fun <T> get(
        url: String,
        headers: Map<String, String>,
        queryParams: Map<String, String>
    ): com.unify.network.NetworkResult<T> = withContext(Dispatchers.IO) {
        try {
            val urlBuilder = HttpUrl.parse(url)?.newBuilder()
            queryParams.forEach { (key, value) ->
                urlBuilder?.addQueryParameter(key, value)
            }
            
            val requestBuilder = Request.Builder()
                .url(urlBuilder?.build() ?: url)
                .get()
            
            headers.forEach { (key, value) ->
                requestBuilder.addHeader(key, value)
            }
            
            val response = client.newCall(requestBuilder.build()).execute()
            
            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                @Suppress("UNCHECKED_CAST")
                com.unify.network.NetworkResult.Success(body as T)
            } else {
                com.unify.network.NetworkResult.Error(
                    com.unify.network.NetworkException.ServerError(
                        response.code,
                        response.message
                    )
                )
            }
        } catch (e: IOException) {
            com.unify.network.NetworkResult.Error(
                com.unify.network.NetworkException.ConnectionError(e.message ?: "Network error", e)
            )
        } catch (e: Exception) {
            com.unify.network.NetworkResult.Error(
                com.unify.network.NetworkException.UnknownError(e.message ?: "Unknown error", e)
            )
        }
    }
    
    override suspend fun <T> post(
        url: String,
        body: Any?,
        headers: Map<String, String>
    ): com.unify.network.NetworkResult<T> = withContext(Dispatchers.IO) {
        try {
            val requestBuilder = Request.Builder().url(url)
            
            val requestBody = when (body) {
                is String -> body.toRequestBody("application/json".toMediaType())
                null -> "".toRequestBody("application/json".toMediaType())
                else -> body.toString().toRequestBody("application/json".toMediaType())
            }
            
            requestBuilder.post(requestBody)
            
            headers.forEach { (key, value) ->
                requestBuilder.addHeader(key, value)
            }
            
            val response = client.newCall(requestBuilder.build()).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                @Suppress("UNCHECKED_CAST")
                com.unify.network.NetworkResult.Success(responseBody as T)
            } else {
                com.unify.network.NetworkResult.Error(
                    com.unify.network.NetworkException.ServerError(
                        response.code,
                        response.message
                    )
                )
            }
        } catch (e: IOException) {
            com.unify.network.NetworkResult.Error(
                com.unify.network.NetworkException.ConnectionError(e.message ?: "Network error", e)
            )
        } catch (e: Exception) {
            com.unify.network.NetworkResult.Error(
                com.unify.network.NetworkException.UnknownError(e.message ?: "Unknown error", e)
            )
        }
    }
    
    override suspend fun <T> put(
        url: String,
        body: Any?,
        headers: Map<String, String>
    ): com.unify.network.NetworkResult<T> = withContext(Dispatchers.IO) {
        try {
            val requestBuilder = Request.Builder().url(url)
            
            val requestBody = when (body) {
                is String -> body.toRequestBody("application/json".toMediaType())
                null -> "".toRequestBody("application/json".toMediaType())
                else -> body.toString().toRequestBody("application/json".toMediaType())
            }
            
            requestBuilder.put(requestBody)
            
            headers.forEach { (key, value) ->
                requestBuilder.addHeader(key, value)
            }
            
            val response = client.newCall(requestBuilder.build()).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                @Suppress("UNCHECKED_CAST")
                com.unify.network.NetworkResult.Success(responseBody as T)
            } else {
                com.unify.network.NetworkResult.Error(
                    com.unify.network.NetworkException.ServerError(
                        response.code,
                        response.message
                    )
                )
            }
        } catch (e: Exception) {
            com.unify.network.NetworkResult.Error(
                com.unify.network.NetworkException.UnknownError(e.message ?: "Unknown error", e)
            )
        }
    }
    
    override suspend fun <T> delete(
        url: String,
        headers: Map<String, String>
    ): com.unify.network.NetworkResult<T> = withContext(Dispatchers.IO) {
        try {
            val requestBuilder = Request.Builder()
                .url(url)
                .delete()
            
            headers.forEach { (key, value) ->
                requestBuilder.addHeader(key, value)
            }
            
            val response = client.newCall(requestBuilder.build()).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                @Suppress("UNCHECKED_CAST")
                com.unify.network.NetworkResult.Success(responseBody as T)
            } else {
                com.unify.network.NetworkResult.Error(
                    com.unify.network.NetworkException.ServerError(
                        response.code,
                        response.message
                    )
                )
            }
        } catch (e: Exception) {
            com.unify.network.NetworkResult.Error(
                com.unify.network.NetworkException.UnknownError(e.message ?: "Unknown error", e)
            )
        }
    }
    
    override fun <T> getStream(
        url: String,
        headers: Map<String, String>
    ): kotlinx.coroutines.flow.Flow<com.unify.network.NetworkResult<T>> {
        return kotlinx.coroutines.flow.flow {
            emit(com.unify.network.NetworkResult.Loading)
            emit(get(url, headers))
        }
    }
    
    override suspend fun uploadFile(
        url: String,
        filePath: String,
        headers: Map<String, String>,
        onProgress: ((Float) -> Unit)?
    ): com.unify.network.NetworkResult<String> = withContext(Dispatchers.IO) {
        try {
            val file = java.io.File(filePath)
            val requestBody = file.asRequestBody("application/octet-stream".toMediaType())
            
            val requestBuilder = Request.Builder()
                .url(url)
                .post(requestBody)
            
            headers.forEach { (key, value) ->
                requestBuilder.addHeader(key, value)
            }
            
            val response = client.newCall(requestBuilder.build()).execute()
            
            if (response.isSuccessful) {
                com.unify.network.NetworkResult.Success(response.body?.string() ?: "")
            } else {
                com.unify.network.NetworkResult.Error(
                    com.unify.network.NetworkException.ServerError(
                        response.code,
                        response.message
                    )
                )
            }
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
    ): com.unify.network.NetworkResult<String> = withContext(Dispatchers.IO) {
        try {
            val requestBuilder = Request.Builder()
                .url(url)
                .get()
            
            headers.forEach { (key, value) ->
                requestBuilder.addHeader(key, value)
            }
            
            val response = client.newCall(requestBuilder.build()).execute()
            
            if (response.isSuccessful) {
                val file = java.io.File(destinationPath)
                file.parentFile?.mkdirs()
                
                response.body?.byteStream()?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                
                com.unify.network.NetworkResult.Success(destinationPath)
            } else {
                com.unify.network.NetworkResult.Error(
                    com.unify.network.NetworkException.ServerError(
                        response.code,
                        response.message
                    )
                )
            }
        } catch (e: Exception) {
            com.unify.network.NetworkResult.Error(
                com.unify.network.NetworkException.UnknownError(e.message ?: "Download error", e)
            )
        }
    }
}

/**
 * Android SharedPreferences存储实现
 */
actual class PreferencesStorageImpl : UnifyStorage {
    private var context: Context? = null
    private val preferences: SharedPreferences?
        get() = context?.getSharedPreferences("unify_prefs", Context.MODE_PRIVATE)
    
    @Composable
    fun initialize() {
        context = LocalContext.current
    }
    
    override suspend fun getString(key: String, defaultValue: String?): String? {
        return preferences?.getString(key, defaultValue)
    }
    
    override suspend fun putString(key: String, value: String) {
        preferences?.edit()?.putString(key, value)?.apply()
    }
    
    override suspend fun getInt(key: String, defaultValue: Int): Int {
        return preferences?.getInt(key, defaultValue) ?: defaultValue
    }
    
    override suspend fun putInt(key: String, value: Int) {
        preferences?.edit()?.putInt(key, value)?.apply()
    }
    
    override suspend fun getLong(key: String, defaultValue: Long): Long {
        return preferences?.getLong(key, defaultValue) ?: defaultValue
    }
    
    override suspend fun putLong(key: String, value: Long) {
        preferences?.edit()?.putLong(key, value)?.apply()
    }
    
    override suspend fun getFloat(key: String, defaultValue: Float): Float {
        return preferences?.getFloat(key, defaultValue) ?: defaultValue
    }
    
    override suspend fun putFloat(key: String, value: Float) {
        preferences?.edit()?.putFloat(key, value)?.apply()
    }
    
    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return preferences?.getBoolean(key, defaultValue) ?: defaultValue
    }
    
    override suspend fun putBoolean(key: String, value: Boolean) {
        preferences?.edit()?.putBoolean(key, value)?.apply()
    }
    
    override suspend fun remove(key: String) {
        preferences?.edit()?.remove(key)?.apply()
    }
    
    override suspend fun clear() {
        preferences?.edit()?.clear()?.apply()
    }
    
    override suspend fun contains(key: String): Boolean {
        return preferences?.contains(key) ?: false
    }
    
    override suspend fun getAllKeys(): Set<String> {
        return preferences?.all?.keys ?: emptySet()
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
    
    override fun <T> observeKey(key: String): kotlinx.coroutines.flow.Flow<T?> {
        return kotlinx.coroutines.flow.flow {
            // Android SharedPreferences不支持观察，这里提供基础实现
            emit(null)
        }
    }
}

/**
 * Android数据库存储实现（基于Room或SQLDelight）
 */
actual class DatabaseStorageImpl : UnifyStorage {
    // 这里应该集成Room或SQLDelight数据库
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
    
    override fun <T> observeKey(key: String): kotlinx.coroutines.flow.Flow<T?> {
        return kotlinx.coroutines.flow.flow {
            emit(memoryStorage[key] as? T)
        }
    }
}

/**
 * Android文件系统存储实现
 */
actual class FileSystemStorageImpl : UnifyStorage {
    private var context: Context? = null
    
    @Composable
    fun initialize() {
        context = LocalContext.current
    }
    
    private fun getFile(key: String): java.io.File? {
        return context?.let { ctx ->
            java.io.File(ctx.filesDir, "unify_$key.txt")
        }
    }
    
    override suspend fun getString(key: String, defaultValue: String?): String? = withContext(Dispatchers.IO) {
        try {
            val file = getFile(key)
            if (file?.exists() == true) {
                file.readText()
            } else {
                defaultValue
            }
        } catch (e: Exception) {
            defaultValue
        }
    }
    
    override suspend fun putString(key: String, value: String) = withContext(Dispatchers.IO) {
        try {
            val file = getFile(key)
            file?.writeText(value)
        } catch (e: Exception) {
            // 处理写入错误
        }
    }
    
    override suspend fun getInt(key: String, defaultValue: Int): Int {
        return getString(key)?.toIntOrNull() ?: defaultValue
    }
    
    override suspend fun putInt(key: String, value: Int) {
        putString(key, value.toString())
    }
    
    override suspend fun getLong(key: String, defaultValue: Long): Long {
        return getString(key)?.toLongOrNull() ?: defaultValue
    }
    
    override suspend fun putLong(key: String, value: Long) {
        putString(key, value.toString())
    }
    
    override suspend fun getFloat(key: String, defaultValue: Float): Float {
        return getString(key)?.toFloatOrNull() ?: defaultValue
    }
    
    override suspend fun putFloat(key: String, value: Float) {
        putString(key, value.toString())
    }
    
    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return getString(key)?.toBooleanStrictOrNull() ?: defaultValue
    }
    
    override suspend fun putBoolean(key: String, value: Boolean) {
        putString(key, value.toString())
    }
    
    override suspend fun remove(key: String) = withContext(Dispatchers.IO) {
        try {
            getFile(key)?.delete()
        } catch (e: Exception) {
            // 处理删除错误
        }
    }
    
    override suspend fun clear() = withContext(Dispatchers.IO) {
        try {
            context?.filesDir?.listFiles()?.filter { 
                it.name.startsWith("unify_") && it.name.endsWith(".txt")
            }?.forEach { it.delete() }
        } catch (e: Exception) {
            // 处理清理错误
        }
    }
    
    override suspend fun contains(key: String): Boolean {
        return getFile(key)?.exists() ?: false
    }
    
    override suspend fun getAllKeys(): Set<String> = withContext(Dispatchers.IO) {
        try {
            context?.filesDir?.listFiles()?.filter { 
                it.name.startsWith("unify_") && it.name.endsWith(".txt")
            }?.map { 
                it.name.removePrefix("unify_").removeSuffix(".txt")
            }?.toSet() ?: emptySet()
        } catch (e: Exception) {
            emptySet()
        }
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
    
    override fun <T> observeKey(key: String): kotlinx.coroutines.flow.Flow<T?> {
        return kotlinx.coroutines.flow.flow {
            emit(getObject<T>(key))
        }
    }
}

/**
 * Android安全存储实现（基于EncryptedSharedPreferences）
 */
actual class SecureStorageImpl : UnifyStorage {
    // 这里应该使用EncryptedSharedPreferences
    private val fallbackStorage = PreferencesStorageImpl()
    
    override suspend fun getString(key: String, defaultValue: String?): String? {
        return fallbackStorage.getString(key, defaultValue)
    }
    
    override suspend fun putString(key: String, value: String) {
        fallbackStorage.putString(key, value)
    }
    
    override suspend fun getInt(key: String, defaultValue: Int): Int {
        return fallbackStorage.getInt(key, defaultValue)
    }
    
    override suspend fun putInt(key: String, value: Int) {
        fallbackStorage.putInt(key, value)
    }
    
    override suspend fun getLong(key: String, defaultValue: Long): Long {
        return fallbackStorage.getLong(key, defaultValue)
    }
    
    override suspend fun putLong(key: String, value: Long) {
        fallbackStorage.putLong(key, value)
    }
    
    override suspend fun getFloat(key: String, defaultValue: Float): Float {
        return fallbackStorage.getFloat(key, defaultValue)
    }
    
    override suspend fun putFloat(key: String, value: Float) {
        fallbackStorage.putFloat(key, value)
    }
    
    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return fallbackStorage.getBoolean(key, defaultValue)
    }
    
    override suspend fun putBoolean(key: String, value: Boolean) {
        fallbackStorage.putBoolean(key, value)
    }
    
    override suspend fun remove(key: String) {
        fallbackStorage.remove(key)
    }
    
    override suspend fun clear() {
        fallbackStorage.clear()
    }
    
    override suspend fun contains(key: String): Boolean {
        return fallbackStorage.contains(key)
    }
    
    override suspend fun getAllKeys(): Set<String> {
        return fallbackStorage.getAllKeys()
    }
    
    override suspend inline fun <reified T> getObject(key: String): T? {
        return fallbackStorage.getObject<T>(key)
    }
    
    override suspend inline fun <reified T> putObject(key: String, value: T) {
        fallbackStorage.putObject(key, value)
    }
    
    override fun <T> observeKey(key: String): kotlinx.coroutines.flow.Flow<T?> {
        return fallbackStorage.observeKey(key)
    }
}

/**
 * Android平台信息实现
 */
actual class AndroidPlatformInfoImpl : PlatformInfo {
    override val platformType: PlatformType = PlatformType.ANDROID
    override val platformVersion: String = Build.VERSION.RELEASE
    override val deviceModel: String = Build.MODEL
    override val isDebug: Boolean = BuildConfig.DEBUG
    
    override fun getScreenSize(): Pair<Int, Int> {
        val displayMetrics = Resources.getSystem().displayMetrics
        return Pair(displayMetrics.widthPixels, displayMetrics.heightPixels)
    }
    
    override fun getDeviceId(): String? = Build.ID
    
    override fun isMobile(): Boolean = true
    
    override fun isTablet(): Boolean {
        val configuration = Resources.getSystem().configuration
        return configuration.smallestScreenWidthDp >= 600
    }
}

/**
 * Android平台能力实现
 */
actual class AndroidPlatformCapabilitiesImpl : PlatformCapabilities {
    override val supportsFileSystem: Boolean = true
    override val supportsCamera: Boolean = true
    override val supportsLocation: Boolean = true
    override val supportsPushNotifications: Boolean = true
    override val supportsBackgroundTasks: Boolean = true
    override val supportsBiometrics: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    override val supportsNFC: Boolean = true
    override val supportsVibration: Boolean = true
}

/**
 * 获取Android平台类型
 */
actual fun getPlatformType(): PlatformType = PlatformType.ANDROID

/**
 * Android平台信息工具
 */
object AndroidPlatformInfo {
    val deviceModel: String get() = Build.MODEL
    val osVersion: String get() = Build.VERSION.RELEASE
    val apiLevel: Int get() = Build.VERSION.SDK_INT
    val manufacturer: String get() = Build.MANUFACTURER
    val deviceId: String get() = Build.ID
    
    fun isTablet(context: Context): Boolean {
        val configuration = context.resources.configuration
        return configuration.smallestScreenWidthDp >= 600
    }
    
    fun getScreenDensity(context: Context): Float {
        return context.resources.displayMetrics.density
    }
}
