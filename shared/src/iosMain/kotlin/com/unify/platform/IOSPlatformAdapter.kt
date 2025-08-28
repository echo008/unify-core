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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import platform.Foundation.*
import platform.UIKit.*
import platform.Security.*

/**
 * iOS平台适配实现
 * 基于文档第10章iOS平台适配要求
 */

/**
 * iOS网络服务实现
 */
actual class UnifyNetworkServiceImpl actual constructor(
    private val config: NetworkConfig,
    private val interceptors: List<NetworkInterceptor>,
    private val cache: NetworkCache?
) : UnifyNetworkService {
    
    private val session: NSURLSession by lazy {
        val configuration = NSURLSessionConfiguration.defaultSessionConfiguration()
        configuration.timeoutIntervalForRequest = config.timeout / 1000.0
        configuration.timeoutIntervalForResource = config.timeout / 1000.0
        NSURLSession.sessionWithConfiguration(configuration)
    }
    
    override suspend fun <T> get(
        url: String,
        headers: Map<String, String>,
        queryParams: Map<String, String>
    ): com.unify.network.NetworkResult<T> = withContext(Dispatchers.Default) {
        try {
            val urlComponents = NSURLComponents(url)
            
            // 添加查询参数
            if (queryParams.isNotEmpty()) {
                val queryItems = queryParams.map { (key, value) ->
                    NSURLQueryItem(key, value)
                }
                urlComponents?.queryItems = queryItems
            }
            
            val request = NSMutableURLRequest(urlComponents?.URL ?: NSURL(string = url)!!)
            request.HTTPMethod = "GET"
            
            // 添加请求头
            headers.forEach { (key, value) ->
                request.setValue(value, key)
            }
            
            val result = performRequest<T>(request)
            result
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
    ): com.unify.network.NetworkResult<T> = withContext(Dispatchers.Default) {
        try {
            val request = NSMutableURLRequest(NSURL(string = url)!!)
            request.HTTPMethod = "POST"
            
            // 设置请求体
            body?.let {
                val bodyString = when (it) {
                    is String -> it
                    else -> it.toString()
                }
                request.HTTPBody = bodyString.encodeToByteArray().toNSData()
                request.setValue("application/json", "Content-Type")
            }
            
            // 添加请求头
            headers.forEach { (key, value) ->
                request.setValue(value, key)
            }
            
            performRequest<T>(request)
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
    ): com.unify.network.NetworkResult<T> = withContext(Dispatchers.Default) {
        try {
            val request = NSMutableURLRequest(NSURL(string = url)!!)
            request.HTTPMethod = "PUT"
            
            // 设置请求体
            body?.let {
                val bodyString = when (it) {
                    is String -> it
                    else -> it.toString()
                }
                request.HTTPBody = bodyString.encodeToByteArray().toNSData()
                request.setValue("application/json", "Content-Type")
            }
            
            // 添加请求头
            headers.forEach { (key, value) ->
                request.setValue(value, key)
            }
            
            performRequest<T>(request)
        } catch (e: Exception) {
            com.unify.network.NetworkResult.Error(
                com.unify.network.NetworkException.UnknownError(e.message ?: "Unknown error", e)
            )
        }
    }
    
    override suspend fun <T> delete(
        url: String,
        headers: Map<String, String>
    ): com.unify.network.NetworkResult<T> = withContext(Dispatchers.Default) {
        try {
            val request = NSMutableURLRequest(NSURL(string = url)!!)
            request.HTTPMethod = "DELETE"
            
            // 添加请求头
            headers.forEach { (key, value) ->
                request.setValue(value, key)
            }
            
            performRequest<T>(request)
        } catch (e: Exception) {
            com.unify.network.NetworkResult.Error(
                com.unify.network.NetworkException.UnknownError(e.message ?: "Unknown error", e)
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
    ): com.unify.network.NetworkResult<String> = withContext(Dispatchers.Default) {
        try {
            val request = NSMutableURLRequest(NSURL(string = url)!!)
            request.HTTPMethod = "POST"
            
            val fileData = NSData.dataWithContentsOfFile(filePath)
            if (fileData != null) {
                request.HTTPBody = fileData
                request.setValue("application/octet-stream", "Content-Type")
                
                // 添加请求头
                headers.forEach { (key, value) ->
                    request.setValue(value, key)
                }
                
                performRequest<String>(request)
            } else {
                com.unify.network.NetworkResult.Error(
                    com.unify.network.NetworkException.UnknownError("File not found: $filePath")
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
    ): com.unify.network.NetworkResult<String> = withContext(Dispatchers.Default) {
        try {
            val request = NSMutableURLRequest(NSURL(string = url)!!)
            request.HTTPMethod = "GET"
            
            // 添加请求头
            headers.forEach { (key, value) ->
                request.setValue(value, key)
            }
            
            // 执行下载请求
            val result = performDownload(request, destinationPath)
            result
        } catch (e: Exception) {
            com.unify.network.NetworkResult.Error(
                com.unify.network.NetworkException.UnknownError(e.message ?: "Download error", e)
            )
        }
    }
    
    private suspend fun <T> performRequest(request: NSURLRequest): com.unify.network.NetworkResult<T> {
        return kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
            val task = session.dataTaskWithRequest(request) { data, response, error ->
                when {
                    error != null -> {
                        continuation.resumeWith(
                            Result.success(
                                com.unify.network.NetworkResult.Error(
                                    com.unify.network.NetworkException.ConnectionError(
                                        error.localizedDescription ?: "Network error"
                                    )
                                )
                            )
                        )
                    }
                    response is NSHTTPURLResponse -> {
                        val statusCode = response.statusCode.toInt()
                        if (statusCode in 200..299) {
                            val responseString = data?.let { 
                                NSString.create(it, NSUTF8StringEncoding)?.toString() 
                            } ?: ""
                            @Suppress("UNCHECKED_CAST")
                            continuation.resumeWith(
                                Result.success(
                                    com.unify.network.NetworkResult.Success(responseString as T)
                                )
                            )
                        } else {
                            continuation.resumeWith(
                                Result.success(
                                    com.unify.network.NetworkResult.Error(
                                        com.unify.network.NetworkException.ServerError(
                                            statusCode,
                                            "HTTP $statusCode"
                                        )
                                    )
                                )
                            )
                        }
                    }
                    else -> {
                        continuation.resumeWith(
                            Result.success(
                                com.unify.network.NetworkResult.Error(
                                    com.unify.network.NetworkException.UnknownError("Unknown response type")
                                )
                            )
                        )
                    }
                }
            }
            task.resume()
            
            continuation.invokeOnCancellation {
                task.cancel()
            }
        }
    }
    
    private suspend fun performDownload(request: NSURLRequest, destinationPath: String): com.unify.network.NetworkResult<String> {
        return kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
            val task = session.downloadTaskWithRequest(request) { location, response, error ->
                when {
                    error != null -> {
                        continuation.resumeWith(
                            Result.success(
                                com.unify.network.NetworkResult.Error(
                                    com.unify.network.NetworkException.ConnectionError(
                                        error.localizedDescription ?: "Download error"
                                    )
                                )
                            )
                        )
                    }
                    location != null && response is NSHTTPURLResponse -> {
                        val statusCode = response.statusCode.toInt()
                        if (statusCode in 200..299) {
                            // 移动文件到目标位置
                            val fileManager = NSFileManager.defaultManager()
                            val destinationURL = NSURL.fileURLWithPath(destinationPath)
                            
                            try {
                                fileManager.moveItemAtURL(location, destinationURL, null)
                                continuation.resumeWith(
                                    Result.success(
                                        com.unify.network.NetworkResult.Success(destinationPath)
                                    )
                                )
                            } catch (e: Exception) {
                                continuation.resumeWith(
                                    Result.success(
                                        com.unify.network.NetworkResult.Error(
                                            com.unify.network.NetworkException.UnknownError("File move error")
                                        )
                                    )
                                )
                            }
                        } else {
                            continuation.resumeWith(
                                Result.success(
                                    com.unify.network.NetworkResult.Error(
                                        com.unify.network.NetworkException.ServerError(
                                            statusCode,
                                            "HTTP $statusCode"
                                        )
                                    )
                                )
                            )
                        }
                    }
                    else -> {
                        continuation.resumeWith(
                            Result.success(
                                com.unify.network.NetworkResult.Error(
                                    com.unify.network.NetworkException.UnknownError("Download failed")
                                )
                            )
                        )
                    }
                }
            }
            task.resume()
            
            continuation.invokeOnCancellation {
                task.cancel()
            }
        }
    }
}

/**
 * iOS UserDefaults存储实现
 */
actual class PreferencesStorageImpl : UnifyStorage {
    private val userDefaults = NSUserDefaults.standardUserDefaults()
    
    override suspend fun getString(key: String, defaultValue: String?): String? {
        return userDefaults.stringForKey(key) ?: defaultValue
    }
    
    override suspend fun putString(key: String, value: String) {
        userDefaults.setObject(value, key)
        userDefaults.synchronize()
    }
    
    override suspend fun getInt(key: String, defaultValue: Int): Int {
        val value = userDefaults.integerForKey(key)
        return if (userDefaults.objectForKey(key) != null) value.toInt() else defaultValue
    }
    
    override suspend fun putInt(key: String, value: Int) {
        userDefaults.setInteger(value.toLong(), key)
        userDefaults.synchronize()
    }
    
    override suspend fun getLong(key: String, defaultValue: Long): Long {
        val value = userDefaults.integerForKey(key)
        return if (userDefaults.objectForKey(key) != null) value else defaultValue
    }
    
    override suspend fun putLong(key: String, value: Long) {
        userDefaults.setInteger(value, key)
        userDefaults.synchronize()
    }
    
    override suspend fun getFloat(key: String, defaultValue: Float): Float {
        val value = userDefaults.floatForKey(key)
        return if (userDefaults.objectForKey(key) != null) value else defaultValue
    }
    
    override suspend fun putFloat(key: String, value: Float) {
        userDefaults.setFloat(value, key)
        userDefaults.synchronize()
    }
    
    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        val value = userDefaults.boolForKey(key)
        return if (userDefaults.objectForKey(key) != null) value else defaultValue
    }
    
    override suspend fun putBoolean(key: String, value: Boolean) {
        userDefaults.setBool(value, key)
        userDefaults.synchronize()
    }
    
    override suspend fun remove(key: String) {
        userDefaults.removeObjectForKey(key)
        userDefaults.synchronize()
    }
    
    override suspend fun clear() {
        val domain = NSBundle.mainBundle().bundleIdentifier()
        if (domain != null) {
            userDefaults.removePersistentDomainForName(domain)
            userDefaults.synchronize()
        }
    }
    
    override suspend fun contains(key: String): Boolean {
        return userDefaults.objectForKey(key) != null
    }
    
    override suspend fun getAllKeys(): Set<String> {
        return userDefaults.dictionaryRepresentation().keys.mapNotNull { 
            it as? String 
        }.toSet()
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
 * iOS Core Data存储实现
 */
actual class DatabaseStorageImpl : UnifyStorage {
    // 这里应该集成Core Data或SQLDelight
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
 * iOS文件系统存储实现
 */
actual class FileSystemStorageImpl : UnifyStorage {
    private val documentsDirectory: String by lazy {
        val paths = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory,
            NSUserDomainMask,
            true
        )
        (paths.firstOrNull() as? String) ?: ""
    }
    
    private fun getFilePath(key: String): String {
        return "$documentsDirectory/unify_$key.txt"
    }
    
    override suspend fun getString(key: String, defaultValue: String?): String? = withContext(Dispatchers.Default) {
        try {
            val filePath = getFilePath(key)
            val data = NSData.dataWithContentsOfFile(filePath)
            if (data != null) {
                NSString.create(data, NSUTF8StringEncoding)?.toString()
            } else {
                defaultValue
            }
        } catch (e: Exception) {
            defaultValue
        }
    }
    
    override suspend fun putString(key: String, value: String) = withContext(Dispatchers.Default) {
        try {
            val filePath = getFilePath(key)
            val data = value.encodeToByteArray().toNSData()
            data.writeToFile(filePath, true)
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
    
    override suspend fun remove(key: String) = withContext(Dispatchers.Default) {
        try {
            val filePath = getFilePath(key)
            NSFileManager.defaultManager().removeItemAtPath(filePath, null)
        } catch (e: Exception) {
            // 处理删除错误
        }
    }
    
    override suspend fun clear() = withContext(Dispatchers.Default) {
        try {
            val fileManager = NSFileManager.defaultManager()
            val files = fileManager.contentsOfDirectoryAtPath(documentsDirectory, null) as? List<String>
            files?.filter { 
                it.startsWith("unify_") && it.endsWith(".txt")
            }?.forEach { fileName ->
                fileManager.removeItemAtPath("$documentsDirectory/$fileName", null)
            }
        } catch (e: Exception) {
            // 处理清理错误
        }
    }
    
    override suspend fun contains(key: String): Boolean {
        val filePath = getFilePath(key)
        return NSFileManager.defaultManager().fileExistsAtPath(filePath)
    }
    
    override suspend fun getAllKeys(): Set<String> = withContext(Dispatchers.Default) {
        try {
            val fileManager = NSFileManager.defaultManager()
            val files = fileManager.contentsOfDirectoryAtPath(documentsDirectory, null) as? List<String>
            files?.filter { 
                it.startsWith("unify_") && it.endsWith(".txt")
            }?.map { 
                it.removePrefix("unify_").removeSuffix(".txt")
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
    
    override fun <T> observeKey(key: String): Flow<T?> {
        return flow {
            emit(getObject<T>(key))
        }
    }
}

/**
 * iOS Keychain安全存储实现
 */
actual class SecureStorageImpl : UnifyStorage {
    private val service = "com.unify.secure.storage"
    
    override suspend fun getString(key: String, defaultValue: String?): String? {
        return getKeychainValue(key) ?: defaultValue
    }
    
    override suspend fun putString(key: String, value: String) {
        setKeychainValue(key, value)
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
    
    override suspend fun remove(key: String) {
        deleteKeychainValue(key)
    }
    
    override suspend fun clear() {
        // 清理所有Keychain项目（需要谨慎实现）
    }
    
    override suspend fun contains(key: String): Boolean {
        return getKeychainValue(key) != null
    }
    
    override suspend fun getAllKeys(): Set<String> {
        // Keychain不支持枚举所有键，返回空集合
        return emptySet()
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
    
    private fun getKeychainValue(key: String): String? {
        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to service,
            kSecAttrAccount to key,
            kSecReturnData to kCFBooleanTrue,
            kSecMatchLimit to kSecMatchLimitOne
        )
        
        val result = SecItemCopyMatching(query as CFDictionaryRef, null)
        return if (result == errSecSuccess) {
            // 这里需要从结果中提取数据并转换为字符串
            // 实际实现需要处理CFData到String的转换
            null
        } else {
            null
        }
    }
    
    private fun setKeychainValue(key: String, value: String) {
        val data = value.encodeToByteArray().toNSData()
        
        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to service,
            kSecAttrAccount to key,
            kSecValueData to data
        )
        
        // 先尝试删除现有项目
        SecItemDelete(query as CFDictionaryRef)
        
        // 添加新项目
        SecItemAdd(query as CFDictionaryRef, null)
    }
    
    private fun deleteKeychainValue(key: String) {
        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to service,
            kSecAttrAccount to key
        )
        
        SecItemDelete(query as CFDictionaryRef)
    }
}

/**
 * iOS平台信息实现
 */
actual class IOSPlatformInfoImpl : PlatformInfo {
    override val platformType: PlatformType = PlatformType.IOS
    override val platformVersion: String = UIDevice.currentDevice().systemVersion()
    override val deviceModel: String = UIDevice.currentDevice().model()
    override val isDebug: Boolean = Platform.isDebugBinary
    
    override fun getScreenSize(): Pair<Int, Int> {
        val bounds = UIScreen.mainScreen().bounds()
        return Pair(bounds.size.width.toInt(), bounds.size.height.toInt())
    }
    
    override fun getDeviceId(): String? = UIDevice.currentDevice().identifierForVendor()?.UUIDString()
    
    override fun isMobile(): Boolean = true
    
    override fun isTablet(): Boolean = UIDevice.currentDevice().userInterfaceIdiom() == UIUserInterfaceIdiomPad
}

/**
 * iOS平台能力实现
 */
actual class IOSPlatformCapabilitiesImpl : PlatformCapabilities {
    override val supportsFileSystem: Boolean = true
    override val supportsCamera: Boolean = true
    override val supportsLocation: Boolean = true
    override val supportsPushNotifications: Boolean = true
    override val supportsBackgroundTasks: Boolean = true
    override val supportsBiometrics: Boolean = true
    override val supportsNFC: Boolean = false // iOS NFC支持有限
    override val supportsVibration: Boolean = true
}

/**
 * 获取iOS平台类型
 */
actual fun getPlatformType(): PlatformType = PlatformType.IOS

/**
 * iOS平台信息工具
 */
object IOSPlatformInfo {
    val deviceModel: String get() = UIDevice.currentDevice().model()
    val osVersion: String get() = UIDevice.currentDevice().systemVersion()
    val deviceName: String get() = UIDevice.currentDevice().name()
    val systemName: String get() = UIDevice.currentDevice().systemName()
    val identifierForVendor: String? get() = UIDevice.currentDevice().identifierForVendor()?.UUIDString()
    
    val isIPad: Boolean get() = UIDevice.currentDevice().userInterfaceIdiom() == UIUserInterfaceIdiomPad
    val isIPhone: Boolean get() = UIDevice.currentDevice().userInterfaceIdiom() == UIUserInterfaceIdiomPhone
    
    fun getScreenScale(): Double {
        return UIScreen.mainScreen().scale()
    }
    
    fun getScreenSize(): Pair<Double, Double> {
        val bounds = UIScreen.mainScreen().bounds()
        return Pair(bounds.size.width, bounds.size.height)
    }
}

/**
 * 扩展函数：ByteArray转NSData
 */
private fun ByteArray.toNSData(): NSData {
    return this.usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = this.size.toULong())
    }
}
