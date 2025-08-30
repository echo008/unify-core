# 工具类 API

## 📝 日志工具

### Logger - 统一日志系统

跨平台的日志记录工具，支持不同级别的日志输出。

```kotlin
object Logger {
    fun d(tag: String, message: String, throwable: Throwable? = null)
    fun i(tag: String, message: String, throwable: Throwable? = null)
    fun w(tag: String, message: String, throwable: Throwable? = null)
    fun e(tag: String, message: String, throwable: Throwable? = null)
    fun v(tag: String, message: String, throwable: Throwable? = null)
    
    fun setLogLevel(level: LogLevel)
    fun enableFileLogging(enabled: Boolean)
    fun setLogFileMaxSize(sizeInMB: Int)
    fun clearLogFiles()
}

enum class LogLevel {
    VERBOSE,
    DEBUG,
    INFO,
    WARN,
    ERROR,
    NONE
}
```

#### 使用示例
```kotlin
class UserService {
    companion object {
        private const val TAG = "UserService"
    }
    
    suspend fun loginUser(email: String, password: String): Result<User> {
        Logger.i(TAG, "开始用户登录: $email")
        
        return try {
            val user = authenticateUser(email, password)
            Logger.i(TAG, "用户登录成功: ${user.id}")
            Result.success(user)
        } catch (e: Exception) {
            Logger.e(TAG, "用户登录失败: $email", e)
            Result.failure(e)
        }
    }
    
    private suspend fun authenticateUser(email: String, password: String): User {
        Logger.d(TAG, "验证用户凭据")
        // 实际认证逻辑
        return User(id = "123", email = email, name = "用户")
    }
}
```

## 💾 缓存工具

### CacheManager - 内存和磁盘缓存

高效的缓存管理系统，支持内存缓存和磁盘持久化。

```kotlin
class CacheManager {
    suspend fun put(key: String, value: Any, ttl: Long = 300000L) // 默认5分钟TTL
    suspend fun get(key: String): Any?
    suspend fun remove(key: String)
    suspend fun clear()
    suspend fun size(): Int
    suspend fun keys(): Set<String>
    suspend fun contains(key: String): Boolean
    
    // 批量操作
    suspend fun putAll(items: Map<String, Any>, ttl: Long = 300000L)
    suspend fun getAll(keys: Set<String>): Map<String, Any>
    suspend fun removeAll(keys: Set<String>)
    
    // 缓存统计
    suspend fun getHitRate(): Double
    suspend fun getCacheStats(): CacheStats
}

data class CacheStats(
    val hitCount: Long,
    val missCount: Long,
    val loadCount: Long,
    val evictionCount: Long,
    val totalLoadTime: Long
)
```

#### 使用示例
```kotlin
class ApiService {
    private val cache = CacheManager()
    private val httpClient = HttpClient()
    
    suspend fun getUser(userId: String): User? {
        val cacheKey = "user_$userId"
        
        // 先尝试从缓存获取
        cache.get(cacheKey)?.let { cachedUser ->
            Logger.d("ApiService", "从缓存获取用户: $userId")
            return cachedUser as User
        }
        
        // 缓存未命中，从网络获取
        return try {
            val response = httpClient.get("/api/users/$userId")
            val user = Json.decodeFromString<User>(response.body)
            
            // 缓存结果，TTL为10分钟
            cache.put(cacheKey, user, ttl = 600000L)
            Logger.d("ApiService", "从网络获取并缓存用户: $userId")
            
            user
        } catch (e: Exception) {
            Logger.e("ApiService", "获取用户失败: $userId", e)
            null
        }
    }
    
    suspend fun clearUserCache() {
        val userKeys = cache.keys().filter { it.startsWith("user_") }
        cache.removeAll(userKeys.toSet())
        Logger.i("ApiService", "清除用户缓存，共${userKeys.size}个条目")
    }
}
```

## 🔐 加密工具

### CryptoUtils - 加密解密工具

提供常用的加密算法和安全功能。

```kotlin
object CryptoUtils {
    // AES 加密
    fun encryptAES(data: String, key: String): String
    fun decryptAES(encryptedData: String, key: String): String
    
    // RSA 加密
    fun encryptRSA(data: String, publicKey: String): String
    fun decryptRSA(encryptedData: String, privateKey: String): String
    
    // 哈希算法
    fun generateHash(input: String, algorithm: HashAlgorithm = HashAlgorithm.SHA256): String
    fun verifyHash(input: String, hash: String, algorithm: HashAlgorithm = HashAlgorithm.SHA256): Boolean
    
    // 密钥生成
    fun generateAESKey(): String
    fun generateRSAKeyPair(): KeyPair
    fun generateRandomString(length: Int): String
    
    // Base64 编码
    fun encodeBase64(data: ByteArray): String
    fun decodeBase64(encodedData: String): ByteArray
    
    // HMAC
    fun generateHMAC(data: String, key: String, algorithm: HMACAlgorithm = HMACAlgorithm.SHA256): String
}

enum class HashAlgorithm {
    MD5, SHA1, SHA256, SHA512
}

enum class HMACAlgorithm {
    SHA1, SHA256, SHA512
}

data class KeyPair(
    val publicKey: String,
    val privateKey: String
)
```

#### 使用示例
```kotlin
class SecurityService {
    private val aesKey = CryptoUtils.generateAESKey()
    
    fun encryptSensitiveData(data: String): String {
        Logger.d("SecurityService", "加密敏感数据")
        return CryptoUtils.encryptAES(data, aesKey)
    }
    
    fun decryptSensitiveData(encryptedData: String): String {
        Logger.d("SecurityService", "解密敏感数据")
        return CryptoUtils.decryptAES(encryptedData, aesKey)
    }
    
    fun generateSecureToken(): String {
        val randomData = CryptoUtils.generateRandomString(32)
        val timestamp = System.currentTimeMillis().toString()
        val combined = "$randomData:$timestamp"
        return CryptoUtils.generateHash(combined, HashAlgorithm.SHA256)
    }
    
    fun verifyDataIntegrity(data: String, expectedHash: String): Boolean {
        return CryptoUtils.verifyHash(data, expectedHash, HashAlgorithm.SHA256)
    }
}
```

## 🌐 网络工具

### HttpClient - HTTP 客户端

功能完整的HTTP客户端，支持各种请求类型和配置。

```kotlin
class HttpClient(
    private val baseUrl: String = "",
    private val timeout: Long = 30000L,
    private val retryCount: Int = 3
) {
    suspend fun get(
        url: String,
        headers: Map<String, String> = emptyMap(),
        queryParams: Map<String, String> = emptyMap()
    ): HttpResponse
    
    suspend fun post(
        url: String,
        body: Any,
        headers: Map<String, String> = emptyMap()
    ): HttpResponse
    
    suspend fun put(
        url: String,
        body: Any,
        headers: Map<String, String> = emptyMap()
    ): HttpResponse
    
    suspend fun delete(
        url: String,
        headers: Map<String, String> = emptyMap()
    ): HttpResponse
    
    suspend fun patch(
        url: String,
        body: Any,
        headers: Map<String, String> = emptyMap()
    ): HttpResponse
    
    // 文件上传
    suspend fun uploadFile(
        url: String,
        file: ByteArray,
        fileName: String,
        fieldName: String = "file",
        headers: Map<String, String> = emptyMap()
    ): HttpResponse
    
    // 文件下载
    suspend fun downloadFile(
        url: String,
        headers: Map<String, String> = emptyMap(),
        progressCallback: ((Long, Long) -> Unit)? = null
    ): ByteArray
}

data class HttpResponse(
    val status: Int,
    val headers: Map<String, String>,
    val body: String,
    val isSuccessful: Boolean = status in 200..299
)
```

#### 使用示例
```kotlin
class ApiClient {
    private val httpClient = HttpClient(
        baseUrl = "https://api.example.com",
        timeout = 15000L,
        retryCount = 2
    )
    
    suspend fun fetchUsers(): List<User> {
        val response = httpClient.get(
            url = "/users",
            headers = mapOf(
                "Authorization" to "Bearer $token",
                "Content-Type" to "application/json"
            )
        )
        
        return if (response.isSuccessful) {
            Json.decodeFromString<List<User>>(response.body)
        } else {
            throw ApiException("获取用户列表失败: ${response.status}")
        }
    }
    
    suspend fun createUser(user: CreateUserRequest): User {
        val response = httpClient.post(
            url = "/users",
            body = user,
            headers = mapOf(
                "Authorization" to "Bearer $token",
                "Content-Type" to "application/json"
            )
        )
        
        return if (response.isSuccessful) {
            Json.decodeFromString<User>(response.body)
        } else {
            throw ApiException("创建用户失败: ${response.status}")
        }
    }
}
```

## 📅 日期时间工具

### DateTimeUtils - 日期时间处理

跨平台的日期时间处理工具。

```kotlin
object DateTimeUtils {
    // 格式化
    fun formatDate(timestamp: Long, pattern: String = "yyyy-MM-dd"): String
    fun formatTime(timestamp: Long, pattern: String = "HH:mm:ss"): String
    fun formatDateTime(timestamp: Long, pattern: String = "yyyy-MM-dd HH:mm:ss"): String
    
    // 解析
    fun parseDate(dateString: String, pattern: String = "yyyy-MM-dd"): Long?
    fun parseDateTime(dateTimeString: String, pattern: String = "yyyy-MM-dd HH:mm:ss"): Long?
    
    // 计算
    fun addDays(timestamp: Long, days: Int): Long
    fun addHours(timestamp: Long, hours: Int): Long
    fun addMinutes(timestamp: Long, minutes: Int): Long
    
    // 比较
    fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean
    fun isToday(timestamp: Long): Boolean
    fun isYesterday(timestamp: Long): Boolean
    fun isTomorrow(timestamp: Long): Boolean
    
    // 相对时间
    fun getRelativeTime(timestamp: Long): String
    fun getTimeAgo(timestamp: Long): String
    
    // 时区
    fun convertToTimeZone(timestamp: Long, timeZone: String): Long
    fun getCurrentTimeZone(): String
    
    // 工具方法
    fun getCurrentTimestamp(): Long
    fun getStartOfDay(timestamp: Long): Long
    fun getEndOfDay(timestamp: Long): Long
    fun getDayOfWeek(timestamp: Long): Int
    fun getDaysInMonth(year: Int, month: Int): Int
}
```

#### 使用示例
```kotlin
class MessageService {
    fun formatMessageTime(timestamp: Long): String {
        return when {
            DateTimeUtils.isToday(timestamp) -> {
                DateTimeUtils.formatTime(timestamp, "HH:mm")
            }
            DateTimeUtils.isYesterday(timestamp) -> {
                "昨天 ${DateTimeUtils.formatTime(timestamp, "HH:mm")}"
            }
            else -> {
                DateTimeUtils.formatDateTime(timestamp, "MM-dd HH:mm")
            }
        }
    }
    
    fun getRelativeMessageTime(timestamp: Long): String {
        val now = DateTimeUtils.getCurrentTimestamp()
        val diffMinutes = (now - timestamp) / (1000 * 60)
        
        return when {
            diffMinutes < 1 -> "刚刚"
            diffMinutes < 60 -> "${diffMinutes}分钟前"
            DateTimeUtils.isToday(timestamp) -> DateTimeUtils.formatTime(timestamp, "HH:mm")
            DateTimeUtils.isYesterday(timestamp) -> "昨天"
            else -> DateTimeUtils.formatDate(timestamp, "MM-dd")
        }
    }
}
```

## 🔧 验证工具

### ValidationUtils - 数据验证

常用的数据验证工具集合。

```kotlin
object ValidationUtils {
    // 邮箱验证
    fun isValidEmail(email: String): Boolean
    
    // 手机号验证
    fun isValidPhoneNumber(phoneNumber: String, countryCode: String = "CN"): Boolean
    
    // 密码强度验证
    fun validatePassword(password: String): PasswordValidationResult
    
    // URL验证
    fun isValidUrl(url: String): Boolean
    
    // 身份证验证
    fun isValidIdCard(idCard: String): Boolean
    
    // 银行卡验证
    fun isValidBankCard(cardNumber: String): Boolean
    
    // 自定义正则验证
    fun matchesPattern(input: String, pattern: String): Boolean
    
    // 长度验证
    fun isValidLength(input: String, minLength: Int, maxLength: Int): Boolean
    
    // 数字验证
    fun isNumeric(input: String): Boolean
    fun isInteger(input: String): Boolean
    fun isDecimal(input: String): Boolean
    
    // 特殊字符验证
    fun containsSpecialCharacters(input: String): Boolean
    fun isAlphanumeric(input: String): Boolean
}

data class PasswordValidationResult(
    val isValid: Boolean,
    val strength: PasswordStrength,
    val issues: List<String>
)

enum class PasswordStrength {
    WEAK, MEDIUM, STRONG, VERY_STRONG
}
```

#### 使用示例
```kotlin
class UserRegistrationValidator {
    fun validateRegistrationForm(
        email: String,
        password: String,
        confirmPassword: String,
        phoneNumber: String
    ): ValidationResult {
        val errors = mutableListOf<String>()
        
        // 邮箱验证
        if (!ValidationUtils.isValidEmail(email)) {
            errors.add("邮箱格式不正确")
        }
        
        // 密码验证
        val passwordResult = ValidationUtils.validatePassword(password)
        if (!passwordResult.isValid) {
            errors.addAll(passwordResult.issues)
        }
        
        // 确认密码验证
        if (password != confirmPassword) {
            errors.add("两次输入的密码不一致")
        }
        
        // 手机号验证
        if (!ValidationUtils.isValidPhoneNumber(phoneNumber)) {
            errors.add("手机号格式不正确")
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
}

data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String>
)
```

## 🎨 图片工具

### ImageUtils - 图片处理

图片处理和操作工具。

```kotlin
object ImageUtils {
    // 图片压缩
    suspend fun compressImage(
        imageData: ByteArray,
        quality: Int = 80,
        maxWidth: Int = 1920,
        maxHeight: Int = 1080
    ): ByteArray
    
    // 图片缩放
    suspend fun resizeImage(
        imageData: ByteArray,
        targetWidth: Int,
        targetHeight: Int,
        keepAspectRatio: Boolean = true
    ): ByteArray
    
    // 图片旋转
    suspend fun rotateImage(imageData: ByteArray, degrees: Float): ByteArray
    
    // 图片裁剪
    suspend fun cropImage(
        imageData: ByteArray,
        x: Int,
        y: Int,
        width: Int,
        height: Int
    ): ByteArray
    
    // 图片格式转换
    suspend fun convertImageFormat(
        imageData: ByteArray,
        fromFormat: ImageFormat,
        toFormat: ImageFormat
    ): ByteArray
    
    // 获取图片信息
    suspend fun getImageInfo(imageData: ByteArray): ImageInfo
    
    // Base64转换
    fun imageToBase64(imageData: ByteArray): String
    fun base64ToImage(base64String: String): ByteArray
}

enum class ImageFormat {
    JPEG, PNG, WEBP, BMP
}

data class ImageInfo(
    val width: Int,
    val height: Int,
    val format: ImageFormat,
    val sizeInBytes: Long
)
```

## 📊 统计工具

### StatisticsUtils - 统计计算

数学统计相关的工具方法。

```kotlin
object StatisticsUtils {
    // 基础统计
    fun mean(values: List<Double>): Double
    fun median(values: List<Double>): Double
    fun mode(values: List<Double>): List<Double>
    fun standardDeviation(values: List<Double>): Double
    fun variance(values: List<Double>): Double
    
    // 极值
    fun min(values: List<Double>): Double
    fun max(values: List<Double>): Double
    fun range(values: List<Double>): Double
    
    // 百分位数
    fun percentile(values: List<Double>, percentile: Double): Double
    fun quartiles(values: List<Double>): Quartiles
    
    // 相关性
    fun correlation(x: List<Double>, y: List<Double>): Double
    
    // 频率统计
    fun frequency(values: List<Any>): Map<Any, Int>
    fun relativeFrequency(values: List<Any>): Map<Any, Double>
}

data class Quartiles(
    val q1: Double,
    val q2: Double, // median
    val q3: Double
)
```

## 🔄 重试工具

### RetryUtils - 重试机制

实现各种重试策略的工具。

```kotlin
object RetryUtils {
    suspend fun <T> retry(
        maxAttempts: Int = 3,
        delay: Long = 1000L,
        backoffMultiplier: Double = 2.0,
        maxDelay: Long = 30000L,
        retryCondition: (Throwable) -> Boolean = { true },
        action: suspend () -> T
    ): T
    
    suspend fun <T> retryWithExponentialBackoff(
        maxAttempts: Int = 3,
        initialDelay: Long = 1000L,
        maxDelay: Long = 30000L,
        action: suspend () -> T
    ): T
    
    suspend fun <T> retryWithFixedDelay(
        maxAttempts: Int = 3,
        delay: Long = 1000L,
        action: suspend () -> T
    ): T
}
```

#### 使用示例
```kotlin
class NetworkService {
    suspend fun fetchDataWithRetry(): ApiResponse {
        return RetryUtils.retry(
            maxAttempts = 3,
            delay = 2000L,
            retryCondition = { exception ->
                // 只对网络错误进行重试
                exception is NetworkException || exception is TimeoutException
            }
        ) {
            httpClient.get("/api/data")
        }
    }
}
```

---

这些工具类提供了开发跨平台应用时常用的功能，包括日志记录、缓存管理、加密解密、网络请求、数据验证等。所有工具都经过优化，确保在各个目标平台上都能高效运行。
