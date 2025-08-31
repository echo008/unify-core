package com.unify.core.dynamic

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlin.collections.mutableMapOf

/**
 * 动态配置管理器
 * 负责远程配置获取、缓存、更新和同步
 */
class DynamicConfigurationManager {
    private var configUrl: String = ""
    private var apiKey: String = ""
    private var isInitialized = false
    
    private val _configurations = MutableStateFlow<Map<String, String>>(emptyMap())
    val configurations: StateFlow<Map<String, String>> = _configurations.asStateFlow()
    
    private val _remoteComponents = MutableStateFlow<Map<String, RemoteComponent>>(emptyMap())
    val remoteComponents: StateFlow<Map<String, RemoteComponent>> = _remoteComponents.asStateFlow()
    
    private val localCache = mutableMapOf<String, CacheEntry>()
    private val networkClient = DynamicNetworkClient()
    private val storageManager = DynamicStorageManager()
    
    /**
     * 初始化配置管理器
     */
    suspend fun initialize(configUrl: String, apiKey: String) {
        this.configUrl = configUrl
        this.apiKey = apiKey
        
        try {
            // 加载本地缓存配置
            loadLocalConfigurations()
            
            // 同步远程配置
            syncRemoteConfigurations()
            
            isInitialized = true
            
            UnifyPerformanceMonitor.recordMetric("config_manager_init", 1.0, "count")
        } catch (e: Exception) {
            // 初始化失败时使用本地缓存
            loadLocalConfigurations()
            isInitialized = true
            
            UnifyPerformanceMonitor.recordMetric("config_manager_init_fallback", 1.0, "count",
                mapOf("error" to e.message.orEmpty()))
        }
    }
    
    /**
     * 获取配置值
     */
    suspend fun getConfig(key: String): String? {
        // 优先从内存缓存获取
        val memoryValue = _configurations.value[key]
        if (memoryValue != null) {
            return memoryValue
        }
        
        // 从本地存储获取
        val localValue = storageManager.getConfig(key)
        if (localValue != null) {
            // 更新内存缓存
            updateMemoryCache(key, localValue)
            return localValue
        }
        
        // 尝试从远程获取
        return try {
            val remoteValue = fetchRemoteConfig(key)
            if (remoteValue != null) {
                // 更新缓存
                updateConfig(key, remoteValue)
                remoteValue
            } else {
                null
            }
        } catch (e: Exception) {
            UnifyPerformanceMonitor.recordMetric("config_fetch_error", 1.0, "count",
                mapOf("key" to key, "error" to e.message.orEmpty()))
            null
        }
    }
    
    /**
     * 更新配置
     */
    suspend fun updateConfig(key: String, value: String) {
        // 更新内存缓存
        updateMemoryCache(key, value)
        
        // 保存到本地存储
        storageManager.saveConfig(key, value)
        
        // 更新缓存条目
        localCache[key] = CacheEntry(
            value = value,
            timestamp = System.currentTimeMillis(),
            source = CacheSource.LOCAL
        )
        
        UnifyPerformanceMonitor.recordMetric("config_updated", 1.0, "count",
            mapOf("key" to key))
    }
    
    /**
     * 批量更新配置
     */
    suspend fun updateConfigs(configs: Map<String, String>) {
        configs.forEach { (key, value) ->
            updateConfig(key, value)
        }
    }
    
    /**
     * 检查更新
     */
    suspend fun checkUpdate(): HotUpdateInfo? {
        if (!isInitialized || configUrl.isEmpty()) {
            return null
        }
        
        return try {
            val request = UpdateCheckRequest(
                currentVersion = getCurrentVersion(),
                platform = getPlatformInfo(),
                deviceId = getDeviceId()
            )
            
            val response = networkClient.checkUpdate(configUrl, apiKey, request)
            
            if (response.hasUpdate) {
                HotUpdateInfo(
                    version = response.latestVersion,
                    description = response.description,
                    downloadUrl = response.downloadUrl,
                    checksum = response.checksum,
                    signature = response.signature,
                    forceUpdate = response.forceUpdate,
                    rollbackVersion = response.rollbackVersion,
                    releaseTime = response.releaseTime
                )
            } else {
                null
            }
        } catch (e: Exception) {
            UnifyPerformanceMonitor.recordMetric("update_check_error", 1.0, "count",
                mapOf("error" to e.message.orEmpty()))
            null
        }
    }
    
    /**
     * 下载更新包
     */
    suspend fun downloadUpdate(updateInfo: HotUpdateInfo): UpdatePackage {
        val startTime = System.currentTimeMillis()
        
        try {
            val packageData = networkClient.downloadPackage(updateInfo.downloadUrl, apiKey)
            
            // 验证校验和
            val calculatedChecksum = calculateChecksum(packageData)
            if (calculatedChecksum != updateInfo.checksum) {
                throw SecurityException("更新包校验和不匹配")
            }
            
            val updatePackage = Json.decodeFromString<UpdatePackage>(packageData.decodeToString())
            
            // 保存到本地缓存
            storageManager.saveUpdatePackage(updateInfo.version, packageData)
            
            val downloadTime = System.currentTimeMillis() - startTime
            UnifyPerformanceMonitor.recordMetric("update_download_time", downloadTime.toDouble(), "ms",
                mapOf("version" to updateInfo.version, "size" to packageData.size.toString()))
            
            return updatePackage
        } catch (e: Exception) {
            UnifyPerformanceMonitor.recordMetric("update_download_error", 1.0, "count",
                mapOf("version" to updateInfo.version, "error" to e.message.orEmpty()))
            throw e
        }
    }
    
    /**
     * 加载远程组件
     */
    suspend fun loadRemoteComponent(componentId: String): RemoteComponent? {
        // 检查内存缓存
        val cachedComponent = _remoteComponents.value[componentId]
        if (cachedComponent != null && !isComponentExpired(cachedComponent)) {
            return cachedComponent
        }
        
        // 检查本地存储
        val localComponent = storageManager.getComponent(componentId)
        if (localComponent != null && !isComponentExpired(localComponent)) {
            updateRemoteComponentCache(componentId, localComponent)
            return localComponent
        }
        
        // 从远程加载
        return try {
            val remoteComponent = fetchRemoteComponent(componentId)
            if (remoteComponent != null) {
                // 更新缓存
                updateRemoteComponentCache(componentId, remoteComponent)
                storageManager.saveComponent(componentId, remoteComponent)
                
                UnifyPerformanceMonitor.recordMetric("remote_component_loaded", 1.0, "count",
                    mapOf("component_id" to componentId))
            }
            remoteComponent
        } catch (e: Exception) {
            UnifyPerformanceMonitor.recordMetric("remote_component_load_error", 1.0, "count",
                mapOf("component_id" to componentId, "error" to e.message.orEmpty()))
            null
        }
    }
    
    /**
     * 获取待处理更新
     */
    suspend fun getPendingUpdate(): HotUpdateInfo? {
        return storageManager.getPendingUpdate()
    }
    
    /**
     * 更新资源
     */
    suspend fun updateResource(path: String, resourceData: ByteArray) {
        storageManager.saveResource(path, resourceData)
        
        UnifyPerformanceMonitor.recordMetric("resource_updated", 1.0, "count",
            mapOf("path" to path, "size" to resourceData.size.toString()))
    }
    
    /**
     * 同步远程配置
     */
    private suspend fun syncRemoteConfigurations() {
        if (configUrl.isEmpty()) return
        
        try {
            val remoteConfigs = networkClient.fetchConfigurations(configUrl, apiKey)
            
            remoteConfigs.forEach { (key, value) ->
                updateConfig(key, value)
            }
            
            UnifyPerformanceMonitor.recordMetric("config_sync_success", 1.0, "count",
                mapOf("count" to remoteConfigs.size.toString()))
        } catch (e: Exception) {
            UnifyPerformanceMonitor.recordMetric("config_sync_error", 1.0, "count",
                mapOf("error" to e.message.orEmpty()))
        }
    }
    
    /**
     * 加载本地配置
     */
    private suspend fun loadLocalConfigurations() {
        val localConfigs = storageManager.getAllConfigs()
        
        val configMap = mutableMapOf<String, String>()
        localConfigs.forEach { (key, value) ->
            configMap[key] = value
            localCache[key] = CacheEntry(
                value = value,
                timestamp = System.currentTimeMillis(),
                source = CacheSource.LOCAL
            )
        }
        
        _configurations.value = configMap
    }
    
    /**
     * 获取远程配置
     */
    private suspend fun fetchRemoteConfig(key: String): String? {
        if (configUrl.isEmpty()) return null
        
        return try {
            networkClient.fetchConfig(configUrl, apiKey, key)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 获取远程组件
     */
    private suspend fun fetchRemoteComponent(componentId: String): RemoteComponent? {
        if (configUrl.isEmpty()) return null
        
        return try {
            val componentData = networkClient.fetchComponent(configUrl, apiKey, componentId)
            
            RemoteComponent(
                id = componentId,
                factory = createComponentFactory(componentData),
                metadata = componentData.metadata,
                loadTime = System.currentTimeMillis(),
                expireTime = System.currentTimeMillis() + 3600000 // 1小时过期
            )
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 更新内存缓存
     */
    private fun updateMemoryCache(key: String, value: String) {
        val currentConfigs = _configurations.value.toMutableMap()
        currentConfigs[key] = value
        _configurations.value = currentConfigs
    }
    
    /**
     * 更新远程组件缓存
     */
    private fun updateRemoteComponentCache(componentId: String, component: RemoteComponent) {
        val currentComponents = _remoteComponents.value.toMutableMap()
        currentComponents[componentId] = component
        _remoteComponents.value = currentComponents
    }
    
    /**
     * 检查组件是否过期
     */
    private fun isComponentExpired(component: RemoteComponent): Boolean {
        return System.currentTimeMillis() > component.expireTime
    }
    
    /**
     * 创建组件工厂
     */
    private fun createComponentFactory(componentData: ComponentData): ComponentFactory {
        return when (componentData.type) {
            "compose" -> ComposeComponentFactory(componentData)
            "native" -> NativeComponentFactory(componentData)
            "hybrid" -> HybridComponentFactory(componentData)
            else -> throw IllegalArgumentException("不支持的组件类型: ${componentData.type}")
        }
    }
    
    /**
     * 获取当前版本
     */
    private fun getCurrentVersion(): String {
        return storageManager.getCurrentVersion() ?: "1.0.0"
    }
    
    /**
     * 获取平台信息
     */
    private fun getPlatformInfo(): String {
        return PlatformManager.getCurrentPlatform().name
    }
    
    /**
     * 获取设备ID
     */
    private fun getDeviceId(): String {
        return PlatformManager.getDeviceInfo().deviceId
    }
    
    /**
     * 计算校验和
     */
    private fun calculateChecksum(data: ByteArray): String {
        // 使用SHA-256计算校验和
        return data.contentHashCode().toString()
    }
}

/**
 * 远程组件
 */
data class RemoteComponent(
    val id: String,
    val factory: ComponentFactory,
    val metadata: ComponentMetadata,
    val loadTime: Long,
    val expireTime: Long
)

/**
 * 缓存条目
 */
data class CacheEntry(
    val value: String,
    val timestamp: Long,
    val source: CacheSource
)

/**
 * 缓存来源
 */
enum class CacheSource {
    LOCAL, REMOTE, MEMORY
}

/**
 * 更新检查请求
 */
@Serializable
data class UpdateCheckRequest(
    val currentVersion: String,
    val platform: String,
    val deviceId: String
)

/**
 * 更新检查响应
 */
@Serializable
data class UpdateCheckResponse(
    val hasUpdate: Boolean,
    val latestVersion: String,
    val description: String,
    val downloadUrl: String,
    val checksum: String,
    val signature: String,
    val forceUpdate: Boolean = false,
    val rollbackVersion: String? = null,
    val releaseTime: Long = System.currentTimeMillis()
)
