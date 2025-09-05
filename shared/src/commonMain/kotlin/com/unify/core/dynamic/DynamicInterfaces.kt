package com.unify.core.dynamic

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

/**
 * 动态存储适配器接口
 */
interface DynamicStorageAdapter {
    suspend fun loadModule(moduleId: String, moduleData: ByteArray): ModuleLoadResult
    suspend fun unloadModule(moduleId: String): Boolean
    suspend fun getModule(moduleId: String): DynamicModule?
    suspend fun getAllModules(): List<DynamicModule>
    suspend fun installPlugin(pluginId: String, pluginData: ByteArray): PluginInstallResult
    suspend fun uninstallPlugin(pluginId: String): Boolean
    suspend fun getPlugin(pluginId: String): DynamicPlugin?
    suspend fun getAllPlugins(): List<DynamicPlugin>
    suspend fun updateModule(moduleId: String, newModuleData: ByteArray): ModuleUpdateResult
    suspend fun reloadModule(moduleId: String): Boolean
    fun observeModuleStates(): Flow<Map<String, ModuleState>>
    fun observePluginStates(): Flow<Map<String, PluginState>>
}

/**
 * 动态模块接口
 */
interface DynamicModule {
    val id: String
    val info: ModuleInfo
    val file: Any // Platform-specific file type
    
    fun initialize(): Boolean
    fun stop()
    fun cleanup()
    fun reload()
}

/**
 * 动态插件接口
 */
interface DynamicPlugin {
    val id: String
    val info: PluginInfo
    val file: Any // Platform-specific file type
    
    fun install(): Boolean
    fun uninstall()
    fun stop()
    fun cleanup()
}

/**
 * 模块状态枚举
 */
enum class ModuleState {
    LOADING,
    LOADED,
    UNLOADED,
    ERROR,
    RELOADED
}

/**
 * 插件状态枚举
 */
enum class PluginState {
    INSTALLING,
    INSTALLED,
    UNINSTALLED,
    ERROR
}

/**
 * 模块加载结果
 */
sealed class ModuleLoadResult {
    data class Success(val module: DynamicModule) : ModuleLoadResult()
    data class Error(val error: String) : ModuleLoadResult()
}

/**
 * 模块更新结果
 */
sealed class ModuleUpdateResult {
    data class Success(val module: DynamicModule) : ModuleUpdateResult()
    data class Error(val error: String) : ModuleUpdateResult()
}

/**
 * 插件安装结果
 */
sealed class PluginInstallResult {
    data class Success(val plugin: DynamicPlugin) : PluginInstallResult()
    data class Error(val error: String) : PluginInstallResult()
}

/**
 * 模块信息
 */
@Serializable
data class ModuleInfo(
    val id: String,
    val name: String,
    val version: String,
    val description: String,
    val author: String,
    val minPlatformVersion: String,
    val permissions: List<String> = emptyList(),
    val dependencies: List<String> = emptyList()
)

/**
 * 插件信息
 */
@Serializable
data class PluginInfo(
    val id: String,
    val name: String,
    val version: String,
    val description: String,
    val author: String,
    val minPlatformVersion: String,
    val permissions: List<String> = emptyList(),
    val dependencies: List<String> = emptyList()
)
