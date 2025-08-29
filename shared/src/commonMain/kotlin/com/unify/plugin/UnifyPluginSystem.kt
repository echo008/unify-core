package com.unify.plugin

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.*
import kotlin.reflect.KClass

/**
 * Unify 插件扩展系统
 * 提供灵活的插件架构，支持动态加载和管理插件
 */

/**
 * 插件接口
 */
interface UnifyPlugin {
    val id: String
    val name: String
    val version: String
    val description: String
    val author: String
    val dependencies: List<String>
    
    suspend fun initialize(context: PluginContext)
    suspend fun start()
    suspend fun stop()
    suspend fun cleanup()
    fun isCompatible(frameworkVersion: String): Boolean
}

/**
 * 插件上下文
 */
interface PluginContext {
    val pluginManager: PluginManager
    val serviceRegistry: ServiceRegistry
    val eventBus: PluginEventBus
    val configManager: PluginConfigManager
    val logger: PluginLogger
    
    fun <T : Any> getService(serviceClass: KClass<T>): T?
    fun <T : Any> registerService(serviceClass: KClass<T>, service: T)
    fun getConfig(key: String): String?
    fun setConfig(key: String, value: String)
}

/**
 * 插件管理器
 */
class PluginManager {
    private val plugins = mutableMapOf<String, UnifyPlugin>()
    private val pluginStates = mutableMapOf<String, PluginState>()
    private val pluginDependencies = mutableMapOf<String, Set<String>>()
    
    private val _pluginEvents = MutableSharedFlow<PluginEvent>()
    val pluginEvents: SharedFlow<PluginEvent> = _pluginEvents.asSharedFlow()
    
    private val serviceRegistry = ServiceRegistryImpl()
    private val eventBus = PluginEventBusImpl()
    private val configManager = PluginConfigManagerImpl()
    private val logger = PluginLoggerImpl()
    
    /**
     * 注册插件
     */
    suspend fun registerPlugin(plugin: UnifyPlugin): Result<Unit> {
        return try {
            if (plugins.containsKey(plugin.id)) {
                return Result.failure(IllegalArgumentException("Plugin ${plugin.id} already registered"))
            }
            
            // 检查依赖
            val missingDependencies = checkDependencies(plugin)
            if (missingDependencies.isNotEmpty()) {
                return Result.failure(IllegalStateException("Missing dependencies: $missingDependencies"))
            }
            
            plugins[plugin.id] = plugin
            pluginStates[plugin.id] = PluginState.REGISTERED
            pluginDependencies[plugin.id] = plugin.dependencies.toSet()
            
            val context = createPluginContext(plugin)
            plugin.initialize(context)
            pluginStates[plugin.id] = PluginState.INITIALIZED
            
            _pluginEvents.emit(PluginEvent.PluginRegistered(plugin.id, plugin.name))
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 启动插件
     */
    suspend fun startPlugin(pluginId: String): Result<Unit> {
        return try {
            val plugin = plugins[pluginId] 
                ?: return Result.failure(IllegalArgumentException("Plugin $pluginId not found"))
            
            val currentState = pluginStates[pluginId]
            if (currentState != PluginState.INITIALIZED && currentState != PluginState.STOPPED) {
                return Result.failure(IllegalStateException("Plugin $pluginId cannot be started from state $currentState"))
            }
            
            // 启动依赖插件
            val dependencies = pluginDependencies[pluginId] ?: emptySet()
            for (depId in dependencies) {
                if (pluginStates[depId] != PluginState.RUNNING) {
                    startPlugin(depId).getOrThrow()
                }
            }
            
            plugin.start()
            pluginStates[pluginId] = PluginState.RUNNING
            
            _pluginEvents.emit(PluginEvent.PluginStarted(pluginId, plugin.name))
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 停止插件
     */
    suspend fun stopPlugin(pluginId: String): Result<Unit> {
        return try {
            val plugin = plugins[pluginId] 
                ?: return Result.failure(IllegalArgumentException("Plugin $pluginId not found"))
            
            if (pluginStates[pluginId] != PluginState.RUNNING) {
                return Result.failure(IllegalStateException("Plugin $pluginId is not running"))
            }
            
            // 检查是否有其他插件依赖此插件
            val dependentPlugins = findDependentPlugins(pluginId)
            if (dependentPlugins.isNotEmpty()) {
                return Result.failure(IllegalStateException("Cannot stop plugin $pluginId, it has dependent plugins: $dependentPlugins"))
            }
            
            plugin.stop()
            pluginStates[pluginId] = PluginState.STOPPED
            
            _pluginEvents.emit(PluginEvent.PluginStopped(pluginId, plugin.name))
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 卸载插件
     */
    suspend fun unregisterPlugin(pluginId: String): Result<Unit> {
        return try {
            val plugin = plugins[pluginId] 
                ?: return Result.failure(IllegalArgumentException("Plugin $pluginId not found"))
            
            // 先停止插件
            if (pluginStates[pluginId] == PluginState.RUNNING) {
                stopPlugin(pluginId).getOrThrow()
            }
            
            plugin.cleanup()
            
            plugins.remove(pluginId)
            pluginStates.remove(pluginId)
            pluginDependencies.remove(pluginId)
            
            _pluginEvents.emit(PluginEvent.PluginUnregistered(pluginId, plugin.name))
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 获取插件信息
     */
    fun getPlugin(pluginId: String): UnifyPlugin? = plugins[pluginId]
    
    /**
     * 获取所有插件
     */
    fun getAllPlugins(): Map<String, UnifyPlugin> = plugins.toMap()
    
    /**
     * 获取插件状态
     */
    fun getPluginState(pluginId: String): PluginState? = pluginStates[pluginId]
    
    /**
     * 获取运行中的插件
     */
    fun getRunningPlugins(): List<UnifyPlugin> {
        return plugins.values.filter { pluginStates[it.id] == PluginState.RUNNING }
    }
    
    private fun checkDependencies(plugin: UnifyPlugin): List<String> {
        return plugin.dependencies.filter { depId ->
            !plugins.containsKey(depId)
        }
    }
    
    private fun findDependentPlugins(pluginId: String): List<String> {
        return pluginDependencies.entries
            .filter { (_, deps) -> pluginId in deps }
            .map { (id, _) -> id }
            .filter { pluginStates[it] == PluginState.RUNNING }
    }
    
    private fun createPluginContext(plugin: UnifyPlugin): PluginContext {
        return PluginContextImpl(
            pluginManager = this,
            serviceRegistry = serviceRegistry,
            eventBus = eventBus,
            configManager = configManager,
            logger = logger
        )
    }
}

/**
 * 插件状态
 */
enum class PluginState {
    REGISTERED,
    INITIALIZED,
    RUNNING,
    STOPPED,
    ERROR
}

/**
 * 插件事件
 */
sealed class PluginEvent {
    data class PluginRegistered(val pluginId: String, val pluginName: String) : PluginEvent()
    data class PluginStarted(val pluginId: String, val pluginName: String) : PluginEvent()
    data class PluginStopped(val pluginId: String, val pluginName: String) : PluginEvent()
    data class PluginUnregistered(val pluginId: String, val pluginName: String) : PluginEvent()
    data class PluginError(val pluginId: String, val error: String) : PluginEvent()
}

/**
 * 服务注册表
 */
interface ServiceRegistry {
    fun <T : Any> registerService(serviceClass: KClass<T>, service: T)
    fun <T : Any> getService(serviceClass: KClass<T>): T?
    fun <T : Any> unregisterService(serviceClass: KClass<T>)
    fun getAllServices(): Map<KClass<*>, Any>
}

/**
 * 插件事件总线
 */
interface PluginEventBus {
    suspend fun publish(event: Any)
    fun <T : Any> subscribe(eventClass: KClass<T>): Flow<T>
    fun unsubscribe(eventClass: KClass<*>)
}

/**
 * 插件配置管理器
 */
interface PluginConfigManager {
    fun getString(key: String): String?
    fun getInt(key: String): Int?
    fun getBoolean(key: String): Boolean?
    fun setString(key: String, value: String)
    fun setInt(key: String, value: Int)
    fun setBoolean(key: String, value: Boolean)
    fun remove(key: String)
    fun getAllConfig(): Map<String, String>
}

/**
 * 插件日志记录器
 */
interface PluginLogger {
    fun debug(message: String)
    fun info(message: String)
    fun warn(message: String)
    fun error(message: String, throwable: Throwable? = null)
}

/**
 * 插件上下文实现
 */
private class PluginContextImpl(
    override val pluginManager: PluginManager,
    override val serviceRegistry: ServiceRegistry,
    override val eventBus: PluginEventBus,
    override val configManager: PluginConfigManager,
    override val logger: PluginLogger
) : PluginContext {
    
    override fun <T : Any> getService(serviceClass: KClass<T>): T? {
        return serviceRegistry.getService(serviceClass)
    }
    
    override fun <T : Any> registerService(serviceClass: KClass<T>, service: T) {
        serviceRegistry.registerService(serviceClass, service)
    }
    
    override fun getConfig(key: String): String? {
        return configManager.getString(key)
    }
    
    override fun setConfig(key: String, value: String) {
        configManager.setString(key, value)
    }
}

/**
 * 服务注册表实现
 */
private class ServiceRegistryImpl : ServiceRegistry {
    private val services = mutableMapOf<KClass<*>, Any>()
    
    override fun <T : Any> registerService(serviceClass: KClass<T>, service: T) {
        services[serviceClass] = service
    }
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> getService(serviceClass: KClass<T>): T? {
        return services[serviceClass] as? T
    }
    
    override fun <T : Any> unregisterService(serviceClass: KClass<T>) {
        services.remove(serviceClass)
    }
    
    override fun getAllServices(): Map<KClass<*>, Any> = services.toMap()
}

/**
 * 插件事件总线实现
 */
private class PluginEventBusImpl : PluginEventBus {
    private val eventFlows = mutableMapOf<KClass<*>, MutableSharedFlow<Any>>()
    
    override suspend fun publish(event: Any) {
        val eventClass = event::class
        val flow = eventFlows[eventClass]
        flow?.emit(event)
    }
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> subscribe(eventClass: KClass<T>): Flow<T> {
        val flow = eventFlows.getOrPut(eventClass) {
            MutableSharedFlow<Any>()
        }
        return flow as Flow<T>
    }
    
    override fun unsubscribe(eventClass: KClass<*>) {
        eventFlows.remove(eventClass)
    }
}

/**
 * 插件配置管理器实现
 */
private class PluginConfigManagerImpl : PluginConfigManager {
    private val config = mutableMapOf<String, String>()
    
    override fun getString(key: String): String? = config[key]
    
    override fun getInt(key: String): Int? = config[key]?.toIntOrNull()
    
    override fun getBoolean(key: String): Boolean? = config[key]?.toBooleanStrictOrNull()
    
    override fun setString(key: String, value: String) {
        config[key] = value
    }
    
    override fun setInt(key: String, value: Int) {
        config[key] = value.toString()
    }
    
    override fun setBoolean(key: String, value: Boolean) {
        config[key] = value.toString()
    }
    
    override fun remove(key: String) {
        config.remove(key)
    }
    
    override fun getAllConfig(): Map<String, String> = config.toMap()
}

/**
 * 插件日志记录器实现
 */
private class PluginLoggerImpl : PluginLogger {
    override fun debug(message: String) {
        println("[DEBUG] $message")
    }
    
    override fun info(message: String) {
        println("[INFO] $message")
    }
    
    override fun warn(message: String) {
        println("[WARN] $message")
    }
    
    override fun error(message: String, throwable: Throwable?) {
        println("[ERROR] $message")
        throwable?.printStackTrace()
    }
}

/**
 * 抽象插件基类
 */
abstract class AbstractUnifyPlugin : UnifyPlugin {
    protected lateinit var context: PluginContext
    
    override suspend fun initialize(context: PluginContext) {
        this.context = context
        onInitialize()
    }
    
    override suspend fun start() {
        onStart()
    }
    
    override suspend fun stop() {
        onStop()
    }
    
    override suspend fun cleanup() {
        onCleanup()
    }
    
    override fun isCompatible(frameworkVersion: String): Boolean {
        return true // 默认兼容所有版本
    }
    
    protected open suspend fun onInitialize() {}
    protected open suspend fun onStart() {}
    protected open suspend fun onStop() {}
    protected open suspend fun onCleanup() {}
}

/**
 * 插件注解
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Plugin(
    val id: String,
    val name: String,
    val version: String,
    val description: String = "",
    val author: String = "",
    val dependencies: Array<String> = []
)

/**
 * 服务注解
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class PluginService(
    val name: String = ""
)

/**
 * 事件处理器注解
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class EventHandler

/**
 * 插件工厂
 */
object PluginFactory {
    
    /**
     * 创建插件实例
     */
    inline fun <reified T : UnifyPlugin> createPlugin(): T {
        return T::class.java.getDeclaredConstructor().newInstance()
    }
    
    /**
     * 从注解创建插件信息
     */
    fun createPluginFromAnnotation(pluginClass: KClass<out UnifyPlugin>): UnifyPlugin? {
        val annotation = pluginClass.annotations.find { it is Plugin } as? Plugin
            ?: return null
        
        return object : AbstractUnifyPlugin() {
            override val id = annotation.id
            override val name = annotation.name
            override val version = annotation.version
            override val description = annotation.description
            override val author = annotation.author
            override val dependencies = annotation.dependencies.toList()
        }
    }
}

/**
 * 插件扩展函数
 */
inline fun <reified T : Any> PluginContext.getService(): T? {
    return getService(T::class)
}

inline fun <reified T : Any> PluginContext.registerService(service: T) {
    registerService(T::class, service)
}

inline fun <reified T : Any> PluginEventBus.subscribe(): Flow<T> {
    return subscribe(T::class)
}

/**
 * 全局插件管理器
 */
object GlobalPluginManager {
    private val pluginManager = PluginManager()
    
    fun getInstance(): PluginManager = pluginManager
    
    suspend fun initializePluginSystem() {
        // 初始化插件系统
    }
    
    suspend fun shutdownPluginSystem() {
        // 关闭所有插件
        val runningPlugins = pluginManager.getRunningPlugins()
        runningPlugins.forEach { plugin ->
            pluginManager.stopPlugin(plugin.id)
        }
    }
}
