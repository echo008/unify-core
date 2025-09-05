package com.unify.core.dynamic

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Android平台动态存储适配器
 * 提供动态模块加载、热更新和插件管理功能
 */
class AndroidStorageAdapter(private val context: Context) : DynamicStorageAdapter {
    
    private val moduleStorage = ConcurrentHashMap<String, DynamicModule>()
    private val pluginStorage = ConcurrentHashMap<String, DynamicPlugin>()
    private val _moduleState = MutableStateFlow<Map<String, ModuleState>>(emptyMap())
    private val _pluginState = MutableStateFlow<Map<String, PluginState>>(emptyMap())
    
    private val dynamicDir = File(context.filesDir, "dynamic")
    private val moduleDir = File(dynamicDir, "modules")
    private val pluginDir = File(dynamicDir, "plugins")
    private val configDir = File(dynamicDir, "config")
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    init {
        initializeDirectories()
        loadPersistedModules()
        loadPersistedPlugins()
    }
    
    override suspend fun loadModule(moduleId: String, moduleData: ByteArray): ModuleLoadResult {
        return try {
            // 验证模块数据
            val moduleInfo = validateModuleData(moduleData)
            if (moduleInfo == null) {
                return ModuleLoadResult.Error("Invalid module data")
            }
            
            // 检查兼容性
            if (!isModuleCompatible(moduleInfo)) {
                return ModuleLoadResult.Error("Module not compatible with current platform")
            }
            
            // 保存模块文件
            val moduleFile = File(moduleDir, "$moduleId.dex")
            moduleFile.writeBytes(moduleData)
            
            // 创建动态模块实例
            val module = createDynamicModule(moduleId, moduleInfo, moduleFile)
            
            // 初始化模块
            val initResult = initializeModule(module)
            if (!initResult) {
                moduleFile.delete()
                return ModuleLoadResult.Error("Module initialization failed")
            }
            
            // 存储模块
            moduleStorage[moduleId] = module
            updateModuleState(moduleId, ModuleState.LOADED)
            
            // 持久化模块信息
            persistModuleInfo(moduleId, moduleInfo)
            
            ModuleLoadResult.Success(module)
        } catch (e: Exception) {
            ModuleLoadResult.Error("Failed to load module: ${e.message}")
        }
    }
    
    override suspend fun unloadModule(moduleId: String): Boolean {
        return try {
            val module = moduleStorage[moduleId]
            if (module != null) {
                // 停止模块
                stopModule(module)
                
                // 清理资源
                cleanupModuleResources(module)
                
                // 删除模块文件
                val moduleFile = File(moduleDir, "$moduleId.dex")
                moduleFile.delete()
                
                // 移除模块
                moduleStorage.remove(moduleId)
                updateModuleState(moduleId, ModuleState.UNLOADED)
                
                // 删除持久化信息
                deleteModuleInfo(moduleId)
                
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun getModule(moduleId: String): DynamicModule? {
        return moduleStorage[moduleId]
    }
    
    override suspend fun getAllModules(): List<DynamicModule> {
        return moduleStorage.values.toList()
    }
    
    override suspend fun installPlugin(pluginId: String, pluginData: ByteArray): PluginInstallResult {
        return try {
            // 验证插件数据
            val pluginInfo = validatePluginData(pluginData)
            if (pluginInfo == null) {
                return PluginInstallResult.Error("Invalid plugin data")
            }
            
            // 检查权限
            if (!hasRequiredPermissions(pluginInfo.permissions)) {
                return PluginInstallResult.Error("Missing required permissions")
            }
            
            // 保存插件文件
            val pluginFile = File(pluginDir, "$pluginId.apk")
            pluginFile.writeBytes(pluginData)
            
            // 创建动态插件实例
            val plugin = createDynamicPlugin(pluginId, pluginInfo, pluginFile)
            
            // 安装插件
            val installResult = installPluginInternal(plugin)
            if (!installResult) {
                pluginFile.delete()
                return PluginInstallResult.Error("Plugin installation failed")
            }
            
            // 存储插件
            pluginStorage[pluginId] = plugin
            updatePluginState(pluginId, PluginState.INSTALLED)
            
            // 持久化插件信息
            persistPluginInfo(pluginId, pluginInfo)
            
            PluginInstallResult.Success(plugin)
        } catch (e: Exception) {
            PluginInstallResult.Error("Failed to install plugin: ${e.message}")
        }
    }
    
    override suspend fun uninstallPlugin(pluginId: String): Boolean {
        return try {
            val plugin = pluginStorage[pluginId]
            if (plugin != null) {
                // 停止插件
                stopPlugin(plugin)
                
                // 卸载插件
                uninstallPluginInternal(plugin)
                
                // 清理资源
                cleanupPluginResources(plugin)
                
                // 删除插件文件
                val pluginFile = File(pluginDir, "$pluginId.apk")
                pluginFile.delete()
                
                // 移除插件
                pluginStorage.remove(pluginId)
                updatePluginState(pluginId, PluginState.UNINSTALLED)
                
                // 删除持久化信息
                deletePluginInfo(pluginId)
                
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun getPlugin(pluginId: String): DynamicPlugin? {
        return pluginStorage[pluginId]
    }
    
    override suspend fun getAllPlugins(): List<DynamicPlugin> {
        return pluginStorage.values.toList()
    }
    
    override suspend fun updateModule(moduleId: String, newModuleData: ByteArray): ModuleUpdateResult {
        return try {
            // 备份当前模块
            val currentModule = moduleStorage[moduleId]
            val backupData = currentModule?.let { backupModule(it) }
            
            // 卸载当前模块
            if (currentModule != null) {
                unloadModule(moduleId)
            }
            
            // 加载新模块
            val loadResult = loadModule(moduleId, newModuleData)
            
            when (loadResult) {
                is ModuleLoadResult.Success -> {
                    // 清理备份
                    backupData?.let { cleanupBackup(it) }
                    ModuleUpdateResult.Success(loadResult.module)
                }
                is ModuleLoadResult.Error -> {
                    // 恢复备份
                    if (backupData != null && currentModule != null) {
                        restoreModule(currentModule, backupData)
                        moduleStorage[moduleId] = currentModule
                        updateModuleState(moduleId, ModuleState.LOADED)
                    }
                    ModuleUpdateResult.Error("Update failed: ${loadResult.error}")
                }
            }
        } catch (e: Exception) {
            ModuleUpdateResult.Error("Update failed: ${e.message}")
        }
    }
    
    override suspend fun reloadModule(moduleId: String): Boolean {
        return try {
            val module = moduleStorage[moduleId]
            if (module != null) {
                // 重新加载模块
                reloadModuleInternal(module)
                updateModuleState(moduleId, ModuleState.RELOADED)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    override fun observeModuleStates(): Flow<Map<String, ModuleState>> {
        return _moduleState.asStateFlow()
    }
    
    override fun observePluginStates(): Flow<Map<String, PluginState>> {
        return _pluginState.asStateFlow()
    }
    
    private suspend fun cleanup(): Boolean {
        return try {
            // 清理过期模块
            cleanupExpiredModules()
            
            // 清理过期插件
            cleanupExpiredPlugins()
            
            // 清理临时文件
            cleanupTempFiles()
            
            // 压缩存储
            compactStorage()
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    // 私有辅助方法
    
    private fun initializeDirectories() {
        dynamicDir.mkdirs()
        moduleDir.mkdirs()
        pluginDir.mkdirs()
        configDir.mkdirs()
    }
    
    private fun validateModuleData(moduleData: ByteArray): ModuleInfo? {
        return try {
            // 验证DEX文件格式
            if (moduleData.size < 100) return null
            
            // 检查DEX魔数
            val dexMagic = "dex\n"
            val headerMagic = String(moduleData.copyOfRange(0, 4))
            if (headerMagic != dexMagic) return null
            
            // 提取模块信息
            ModuleInfo(
                id = "temp_module",
                name = "Dynamic Module",
                version = "1.0.0",
                description = "Android dynamic module",
                author = "Unify System",
                minPlatformVersion = Build.VERSION_CODES.LOLLIPOP.toString(),
                permissions = emptyList(),
                dependencies = emptyList()
            )
        } catch (e: Exception) {
            null
        }
    }
    
    private fun validatePluginData(pluginData: ByteArray): PluginInfo? {
        return try {
            // 验证APK文件格式
            if (pluginData.size < 1000) return null
            
            // 检查ZIP/APK签名
            val zipSignature = byteArrayOf(0x50, 0x4B, 0x03, 0x04)
            val headerSignature = pluginData.copyOfRange(0, 4)
            if (!zipSignature.contentEquals(headerSignature)) return null
            
            // 提取插件信息
            PluginInfo(
                id = "temp_plugin",
                name = "Dynamic Plugin",
                version = "1.0.0",
                description = "Android dynamic plugin",
                author = "Unify System",
                minPlatformVersion = Build.VERSION_CODES.LOLLIPOP.toString(),
                permissions = emptyList(),
                dependencies = emptyList()
            )
        } catch (e: Exception) {
            null
        }
    }
    
    private fun isModuleCompatible(moduleInfo: ModuleInfo): Boolean {
        return Build.VERSION.SDK_INT >= moduleInfo.minPlatformVersion.toIntOrNull() ?: 0
    }
    
    private fun hasRequiredPermissions(permissions: List<String>): Boolean {
        return permissions.all { permission ->
            context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    private fun createDynamicModule(moduleId: String, moduleInfo: ModuleInfo, moduleFile: File): DynamicModule {
        return AndroidDynamicModule(
            id = moduleId,
            info = moduleInfo,
            file = moduleFile,
            classLoader = createModuleClassLoader(moduleFile)
        )
    }
    
    private fun createDynamicPlugin(pluginId: String, pluginInfo: PluginInfo, pluginFile: File): DynamicPlugin {
        return AndroidDynamicPlugin(
            id = pluginId,
            info = pluginInfo,
            file = pluginFile,
            packageInfo = extractPackageInfo(pluginFile)
        )
    }
    
    private fun createModuleClassLoader(moduleFile: File): ClassLoader {
        return dalvik.system.DexClassLoader(
            moduleFile.absolutePath,
            context.cacheDir.absolutePath,
            null,
            context.classLoader
        )
    }
    
    private fun extractPackageInfo(pluginFile: File): android.content.pm.PackageInfo? {
        return try {
            context.packageManager.getPackageArchiveInfo(
                pluginFile.absolutePath,
                PackageManager.GET_ACTIVITIES or PackageManager.GET_SERVICES
            )
        } catch (e: Exception) {
            null
        }
    }
    
    private fun initializeModule(module: DynamicModule): Boolean {
        return try {
            // 初始化模块
            module.initialize()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private fun installPluginInternal(plugin: DynamicPlugin): Boolean {
        return try {
            // 安装插件逻辑
            plugin.install()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private fun stopModule(module: DynamicModule) {
        try {
            module.stop()
        } catch (e: Exception) {
            // 忽略停止错误
        }
    }
    
    private fun stopPlugin(plugin: DynamicPlugin) {
        try {
            plugin.stop()
        } catch (e: Exception) {
            // 忽略停止错误
        }
    }
    
    private fun cleanupModuleResources(module: DynamicModule) {
        try {
            module.cleanup()
        } catch (e: Exception) {
            // 忽略清理错误
        }
    }
    
    private fun cleanupPluginResources(plugin: DynamicPlugin) {
        try {
            plugin.cleanup()
        } catch (e: Exception) {
            // 忽略清理错误
        }
    }
    
    private fun uninstallPluginInternal(plugin: DynamicPlugin) {
        try {
            plugin.uninstall()
        } catch (e: Exception) {
            // 忽略卸载错误
        }
    }
    
    private fun reloadModuleInternal(module: DynamicModule) {
        try {
            module.reload()
        } catch (e: Exception) {
            // 忽略重载错误
        }
    }
    
    private fun backupModule(module: DynamicModule): ByteArray? {
        return try {
            (module.file as File).readBytes()
        } catch (e: Exception) {
            null
        }
    }
    
    private fun restoreModule(module: DynamicModule, backupData: ByteArray) {
        try {
            (module.file as File).writeBytes(backupData)
        } catch (e: Exception) {
            // 忽略恢复错误
        }
    }
    
    private fun cleanupBackup(backupData: ByteArray) {
        // 清理备份数据
    }
    
    private fun updateModuleState(moduleId: String, state: ModuleState) {
        val currentStates = _moduleState.value.toMutableMap()
        currentStates[moduleId] = state
        _moduleState.value = currentStates
    }
    
    private fun updatePluginState(pluginId: String, state: PluginState) {
        val currentStates = _pluginState.value.toMutableMap()
        currentStates[pluginId] = state
        _pluginState.value = currentStates
    }
    
    private fun persistModuleInfo(moduleId: String, moduleInfo: ModuleInfo) {
        try {
            val configFile = File(configDir, "$moduleId.json")
            val jsonString = json.encodeToString(ModuleInfo.serializer(), moduleInfo)
            configFile.writeText(jsonString)
        } catch (e: Exception) {
            // 忽略持久化错误
        }
    }
    
    private fun persistPluginInfo(pluginId: String, pluginInfo: PluginInfo) {
        try {
            val configFile = File(configDir, "$pluginId.json")
            val jsonString = json.encodeToString(PluginInfo.serializer(), pluginInfo)
            configFile.writeText(jsonString)
        } catch (e: Exception) {
            // 忽略持久化错误
        }
    }
    
    private fun deleteModuleInfo(moduleId: String) {
        try {
            val configFile = File(configDir, "$moduleId.json")
            configFile.delete()
        } catch (e: Exception) {
            // 忽略删除错误
        }
    }
    
    private fun deletePluginInfo(pluginId: String) {
        try {
            val configFile = File(configDir, "$pluginId.json")
            configFile.delete()
        } catch (e: Exception) {
            // 忽略删除错误
        }
    }
    
    private fun loadPersistedModules() {
        try {
            configDir.listFiles { file -> file.name.endsWith(".json") }?.forEach { configFile ->
                val moduleId = configFile.nameWithoutExtension
                val moduleFile = File(moduleDir, "$moduleId.dex")
                
                if (moduleFile.exists()) {
                    val jsonString = configFile.readText()
                    val moduleInfo = json.decodeFromString(ModuleInfo.serializer(), jsonString)
                    val module = createDynamicModule(moduleId, moduleInfo, moduleFile)
                    
                    if (initializeModule(module)) {
                        moduleStorage[moduleId] = module
                        updateModuleState(moduleId, ModuleState.LOADED)
                    }
                }
            }
        } catch (e: Exception) {
            // 忽略加载错误
        }
    }
    
    private fun loadPersistedPlugins() {
        try {
            configDir.listFiles { file -> file.name.endsWith(".json") }?.forEach { configFile ->
                val pluginId = configFile.nameWithoutExtension
                val pluginFile = File(pluginDir, "$pluginId.apk")
                
                if (pluginFile.exists()) {
                    val jsonString = configFile.readText()
                    val pluginInfo = json.decodeFromString(PluginInfo.serializer(), jsonString)
                    val plugin = createDynamicPlugin(pluginId, pluginInfo, pluginFile)
                    
                    if (installPluginInternal(plugin)) {
                        pluginStorage[pluginId] = plugin
                        updatePluginState(pluginId, PluginState.INSTALLED)
                    }
                }
            }
        } catch (e: Exception) {
            // 忽略加载错误
        }
    }
    
    private fun calculateTotalSize(): Long {
        return try {
            dynamicDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
        } catch (e: Exception) {
            0L
        }
    }
    
    private fun getAvailableSpace(): Long {
        return try {
            dynamicDir.freeSpace
        } catch (e: Exception) {
            0L
        }
    }
    
    private fun cleanupExpiredModules() {
        // 清理过期模块逻辑
    }
    
    private fun cleanupExpiredPlugins() {
        // 清理过期插件逻辑
    }
    
    private fun cleanupTempFiles() {
        // 清理临时文件逻辑
    }
    
    private fun compactStorage() {
        // 压缩存储逻辑
    }
}

/**
 * Android动态模块实现
 */
class AndroidDynamicModule(
    override val id: String,
    override val info: ModuleInfo,
    override val file: File,
    val classLoader: ClassLoader
) : DynamicModule {
    
    private var isInitialized = false
    private var isRunning = false
    
    override fun initialize(): Boolean {
        return try {
            // 模块初始化逻辑
            isInitialized = true
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private fun start(): Boolean {
        return try {
            if (isInitialized) {
                // 模块启动逻辑
                isRunning = true
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    override fun stop() {
        try {
            // 模块停止逻辑
            isRunning = false
        } catch (e: Exception) {
            // 忽略停止错误
        }
    }
    
    override fun reload() {
        try {
            // 模块重载逻辑
            stop()
            initialize()
            start()
        } catch (e: Exception) {
            // 忽略重载错误
        }
    }
    
    override fun cleanup() {
        try {
            // 模块清理逻辑
            stop()
            isInitialized = false
        } catch (e: Exception) {
            // 忽略清理错误
        }
    }
    
    private fun getState(): ModuleState {
        return when {
            !isInitialized -> ModuleState.UNLOADED
            !isRunning -> ModuleState.LOADED
            else -> ModuleState.LOADED
        }
    }
}

/**
 * Android动态插件实现
 */
class AndroidDynamicPlugin(
    override val id: String,
    override val info: PluginInfo,
    override val file: File,
    val packageInfo: android.content.pm.PackageInfo?
) : DynamicPlugin {
    
    private var isInstalled = false
    private var isRunning = false
    
    override fun install(): Boolean {
        return try {
            // 插件安装逻辑
            isInstalled = true
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override fun uninstall() {
        try {
            // 插件卸载逻辑
            stop()
            isInstalled = false
        } catch (e: Exception) {
            // 忽略卸载错误
        }
    }
    
    private fun start(): Boolean {
        return try {
            if (isInstalled) {
                // 插件启动逻辑
                isRunning = true
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    override fun stop() {
        try {
            // 插件停止逻辑
            isRunning = false
        } catch (e: Exception) {
            // 忽略停止错误
        }
    }
    
    override fun cleanup() {
        try {
            // 插件清理逻辑
            uninstall()
        } catch (e: Exception) {
            // 忽略清理错误
        }
    }
    
    private fun getState(): PluginState {
        return when {
            !isInstalled -> PluginState.UNINSTALLED
            !isRunning -> PluginState.INSTALLED
            else -> PluginState.INSTALLED
        }
    }
}
