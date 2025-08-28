package com.unify.di

import org.koin.core.module.Module
import org.koin.dsl.module
import com.unify.network.*
import com.unify.storage.*
import com.unify.data.*
import com.unify.platform.*
import com.unify.database.*

/**
 * Unify KMP 依赖注入模块配置
 * 基于Koin框架的模块化依赖管理
 */

/**
 * 网络模块
 */
val networkModule = module {
    // 网络服务
    single<UnifyNetworkService> { 
        createPlatformNetworkService()
    }
    
    // 网络拦截器
    factory<NetworkInterceptor> { AuthInterceptor() }
    factory<NetworkInterceptor> { LoggingInterceptor() }
    factory<NetworkInterceptor> { CacheInterceptor() }
    
    // 网络配置
    single<NetworkConfig> {
        NetworkConfig(
            baseUrl = "https://api.example.com",
            timeout = 30000L,
            enableLogging = true,
            enableCache = true
        )
    }
}

/**
 * 存储模块
 */
val storageModule = module {
    // 基础存储服务
    single<UnifyStorage>(qualifier = org.koin.core.qualifier.named("preferences")) {
        createPlatformPreferencesStorage()
    }
    
    single<UnifyStorage>(qualifier = org.koin.core.qualifier.named("database")) {
        createPlatformDatabaseStorage()
    }
    
    single<UnifyStorage>(qualifier = org.koin.core.qualifier.named("file")) {
        createPlatformFileStorage()
    }
    
    single<UnifyStorage>(qualifier = org.koin.core.qualifier.named("secure")) {
        createPlatformSecureStorage()
    }
    
    // 加密存储装饰器
    factory<EncryptedStorageDecorator> {
        EncryptedStorageDecorator(
            storage = get(qualifier = org.koin.core.qualifier.named("preferences")),
            encryptor = get()
        )
    }
    
    // 加密器
    single<StorageEncryptor> { AESStorageEncryptor() }
}

/**
 * 数据库模块
 */
val databaseModule = module {
    // 数据库驱动工厂
    single<UnifyDatabaseDriverFactory> {
        createPlatformDatabaseDriverFactory()
    }
    
    // 数据库实例
    single<UnifyDatabase> {
        UnifyDatabaseFactory.createDatabase(get())
    }
}

/**
 * 数据仓库模块
 */
val repositoryModule = module {
    // 用户数据仓库
    single<UserRepository> {
        UserRepository(
            networkService = get(),
            localStorage = get(qualifier = org.koin.core.qualifier.named("preferences")),
            database = get()
        )
    }
    
    // 配置数据仓库
    single<ConfigRepository> {
        ConfigRepository(
            networkService = get(),
            localStorage = get(qualifier = org.koin.core.qualifier.named("preferences")),
            database = get()
        )
    }
    
    // 缓存数据仓库
    single<CacheRepository> {
        CacheRepository(
            localStorage = get(qualifier = org.koin.core.qualifier.named("preferences")),
            database = get()
        )
    }
}

/**
 * 平台模块
 */
val platformModule = module {
    // 平台信息
    single<PlatformInfo> {
        createPlatformInfo()
    }
    
    // 平台能力
    single<PlatformCapabilities> {
        createPlatformCapabilities()
    }
    
    // 平台管理器
    single<UnifyPlatformManager> {
        UnifyPlatformManager(
            platformInfo = get(),
            platformCapabilities = get()
        )
    }
}

/**
 * UI模块
 */
val uiModule = module {
    // 主题管理器
    single<UnifyThemeManager> {
        UnifyThemeManager()
    }
    
    // 导航管理器
    single<UnifyNavigator> {
        UnifyNavigatorImpl()
    }
    
    // 性能监控
    single<ComponentPerformanceMonitor> {
        ComponentPerformanceMonitor()
    }
}

/**
 * 业务模块
 */
val businessModule = module {
    // 用户服务
    single<UserService> {
        UserService(
            userRepository = get(),
            platformManager = get()
        )
    }
    
    // 配置服务
    single<ConfigService> {
        ConfigService(
            configRepository = get()
        )
    }
    
    // 分析服务
    single<AnalyticsService> {
        AnalyticsService(
            platformManager = get()
        )
    }
}

/**
 * 所有模块集合
 */
val allModules = listOf(
    networkModule,
    storageModule,
    databaseModule,
    repositoryModule,
    platformModule,
    uiModule,
    businessModule
)

/**
 * 平台特定的依赖创建函数
 */
expect fun createPlatformNetworkService(): UnifyNetworkService
expect fun createPlatformPreferencesStorage(): UnifyStorage
expect fun createPlatformDatabaseStorage(): UnifyStorage
expect fun createPlatformFileStorage(): UnifyStorage
expect fun createPlatformSecureStorage(): UnifyStorage
expect fun createPlatformDatabaseDriverFactory(): UnifyDatabaseDriverFactory
expect fun createPlatformInfo(): PlatformInfo
expect fun createPlatformCapabilities(): PlatformCapabilities

/**
 * 网络配置数据类
 */
data class NetworkConfig(
    val baseUrl: String,
    val timeout: Long,
    val enableLogging: Boolean,
    val enableCache: Boolean,
    val maxRetries: Int = 3,
    val retryDelayMs: Long = 1000L
)

/**
 * 主题管理器
 */
class UnifyThemeManager {
    private var currentTheme = UnifyTheme.LIGHT
    
    fun setTheme(theme: UnifyTheme) {
        currentTheme = theme
    }
    
    fun getCurrentTheme(): UnifyTheme = currentTheme
}

enum class UnifyTheme {
    LIGHT, DARK, AUTO
}

/**
 * 用户服务
 */
class UserService(
    private val userRepository: UserRepository,
    private val platformManager: UnifyPlatformManager
) {
    suspend fun getCurrentUser(): UserEntity? {
        return userRepository.getCurrentUser()
    }
    
    suspend fun login(username: String, password: String): Boolean {
        return userRepository.login(username, password)
    }
    
    suspend fun logout(): Boolean {
        return userRepository.logout()
    }
    
    suspend fun updateProfile(user: UserEntity): Boolean {
        return userRepository.updateUser(user)
    }
}

/**
 * 配置服务
 */
class ConfigService(
    private val configRepository: ConfigRepository
) {
    suspend fun getConfig(key: String): String? {
        return configRepository.getConfig(key)
    }
    
    suspend fun setConfig(key: String, value: String) {
        configRepository.setConfig(key, value)
    }
    
    suspend fun getAllConfigs(): Map<String, String> {
        return configRepository.getAllConfigs()
    }
}

/**
 * 分析服务
 */
class AnalyticsService(
    private val platformManager: UnifyPlatformManager
) {
    fun trackEvent(eventName: String, parameters: Map<String, Any>) {
        // 基于平台的事件追踪实现
        when (platformManager.getCurrentPlatform()) {
            PlatformType.ANDROID -> trackAndroidEvent(eventName, parameters)
            PlatformType.IOS -> trackIOSEvent(eventName, parameters)
            PlatformType.WEB -> trackWebEvent(eventName, parameters)
            else -> trackGenericEvent(eventName, parameters)
        }
    }
    
    private fun trackAndroidEvent(eventName: String, parameters: Map<String, Any>) {
        // Android特定的事件追踪
    }
    
    private fun trackIOSEvent(eventName: String, parameters: Map<String, Any>) {
        // iOS特定的事件追踪
    }
    
    private fun trackWebEvent(eventName: String, parameters: Map<String, Any>) {
        // Web特定的事件追踪
    }
    
    private fun trackGenericEvent(eventName: String, parameters: Map<String, Any>) {
        // 通用事件追踪
    }
}

/**
 * 用户数据仓库扩展
 */
class UserRepository(
    private val networkService: UnifyNetworkService,
    private val localStorage: UnifyStorage,
    private val database: UnifyDatabase
) {
    suspend fun getCurrentUser(): UserEntity? {
        // 先从本地存储获取
        val userId = localStorage.getLong("current_user_id", -1L)
        if (userId != -1L) {
            return database.getUserEntity(userId)
        }
        return null
    }
    
    suspend fun login(username: String, password: String): Boolean {
        return try {
            val loginRequest = mapOf(
                "username" to username,
                "password" to password
            )
            
            val result: NetworkResult<Map<String, Any>> = networkService.post(
                url = "/auth/login",
                body = loginRequest
            )
            
            when (result) {
                is NetworkResult.Success -> {
                    val userData = result.data
                    val userId = (userData["id"] as Number).toLong()
                    localStorage.putLong("current_user_id", userId)
                    
                    // 保存用户信息到数据库
                    val user = UserEntity(
                        id = userId,
                        username = userData["username"] as String,
                        email = userData["email"] as String,
                        displayName = userData["displayName"] as String,
                        avatarUrl = userData["avatarUrl"] as String?,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis(),
                        isActive = true
                    )
                    database.insertUserEntity(user)
                    true
                }
                else -> false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun logout(): Boolean {
        return try {
            localStorage.remove("current_user_id")
            true
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun updateUser(user: UserEntity): Boolean {
        return try {
            val updateRequest = mapOf(
                "email" to user.email,
                "displayName" to user.displayName,
                "avatarUrl" to user.avatarUrl
            )
            
            val result: NetworkResult<Map<String, Any>> = networkService.put(
                url = "/users/${user.id}",
                body = updateRequest
            )
            
            when (result) {
                is NetworkResult.Success -> {
                    database.userQueries.updateUser(
                        email = user.email,
                        displayName = user.displayName,
                        avatarUrl = user.avatarUrl,
                        updatedAt = System.currentTimeMillis(),
                        id = user.id
                    )
                    true
                }
                else -> false
            }
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * 配置数据仓库扩展
 */
class ConfigRepository(
    private val networkService: UnifyNetworkService,
    private val localStorage: UnifyStorage,
    private val database: UnifyDatabase
) {
    suspend fun getConfig(key: String): String? {
        // 先从本地数据库获取
        val config = database.getConfigEntity(key)
        if (config != null) {
            return config.value
        }
        
        // 从网络获取
        return try {
            val result: NetworkResult<Map<String, Any>> = networkService.get(
                url = "/config/$key"
            )
            
            when (result) {
                is NetworkResult.Success -> {
                    val value = result.data["value"] as String
                    
                    // 保存到本地
                    val configEntity = AppConfigEntity(
                        key = key,
                        value = value,
                        type = "STRING",
                        updatedAt = System.currentTimeMillis()
                    )
                    database.insertConfigEntity(configEntity)
                    
                    value
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun setConfig(key: String, value: String) {
        val configEntity = AppConfigEntity(
            key = key,
            value = value,
            type = "STRING",
            updatedAt = System.currentTimeMillis()
        )
        database.insertConfigEntity(configEntity)
        
        // 同步到服务器
        try {
            networkService.put<Map<String, Any>>(
                url = "/config/$key",
                body = mapOf("value" to value)
            )
        } catch (e: Exception) {
            // 忽略网络错误，优先保证本地存储
        }
    }
    
    suspend fun getAllConfigs(): Map<String, String> {
        return database.appConfigQueries.selectAllConfigs()
            .executeAsList()
            .associate { it.key to it.value }
    }
}

/**
 * 缓存数据仓库
 */
class CacheRepository(
    private val localStorage: UnifyStorage,
    private val database: UnifyDatabase
) {
    suspend fun getCache(key: String): String? {
        return database.getCacheEntity(key)?.value
    }
    
    suspend fun setCache(key: String, value: String, expirationMs: Long? = null) {
        val expiresAt = expirationMs?.let { System.currentTimeMillis() + it }
        val cacheEntity = CacheDataEntity(
            key = key,
            value = value,
            expiresAt = expiresAt,
            createdAt = System.currentTimeMillis()
        )
        database.insertCacheEntity(cacheEntity)
    }
    
    suspend fun removeCache(key: String) {
        database.cacheDataQueries.deleteCacheByKey(key)
    }
    
    suspend fun clearExpiredCache() {
        database.cacheDataQueries.deleteExpiredCache(System.currentTimeMillis())
    }
    
    suspend fun clearAllCache() {
        database.cacheDataQueries.clearAllCache()
    }
}
