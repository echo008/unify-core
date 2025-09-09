package com.unify.core.dynamic

import com.unify.core.architecture.ComponentType
import com.unify.core.platform.getCurrentTimeMillis
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

/**
 * 动态管理组件 - 提供动态组件的管理功能
 */
class DynamicManagementComponents {
    companion object {
        const val MAX_COMPONENTS = 1000
        const val COMPONENT_TIMEOUT_MS = 30000L
        const val HEALTH_CHECK_INTERVAL_MS = 10000L
        const val CLEANUP_INTERVAL_MS = 60000L
        const val MAX_COMPONENT_SIZE_MB = 50L
        const val DEFAULT_COMPONENT_VERSION = "1.0.0"
        const val COMPONENT_REGISTRY_VERSION = "1.0.0"
        const val MAX_DEPENDENCY_DEPTH = 10
        const val COMPONENT_LOAD_TIMEOUT_MS = 15000L
    }

    private val _managementState = MutableStateFlow(ManagementState.IDLE)
    val managementState: StateFlow<ManagementState> = _managementState.asStateFlow()

    private val _registeredComponents = MutableStateFlow<Map<String, ComponentRegistration>>(emptyMap())
    val registeredComponents: StateFlow<Map<String, ComponentRegistration>> = _registeredComponents.asStateFlow()

    private val _loadedComponents = MutableStateFlow<Map<String, LoadedComponent>>(emptyMap())
    val loadedComponents: StateFlow<Map<String, LoadedComponent>> = _loadedComponents.asStateFlow()

    private val _componentDependencies = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val componentDependencies: StateFlow<Map<String, List<String>>> = _componentDependencies.asStateFlow()

    private val _componentMetrics = MutableStateFlow<Map<String, ComponentMetrics>>(emptyMap())
    val componentMetrics: StateFlow<Map<String, ComponentMetrics>> = _componentMetrics.asStateFlow()

    /**
     * 初始化动态管理组件
     */
    suspend fun initialize(): Boolean {
        return try {
            _managementState.value = ManagementState.INITIALIZING

            // 注册核心组件
            registerCoreComponents()

            // 启动监控服务
            startMonitoringServices()

            _managementState.value = ManagementState.RUNNING
            true
        } catch (e: Exception) {
            _managementState.value = ManagementState.ERROR
            false
        }
    }

    /**
     * 注册核心组件
     */
    private fun registerCoreComponents() {
        val coreComponents =
            listOf(
                ComponentRegistration(
                    id = "unify-ui-core",
                    name = "Unify UI Core",
                    version = DEFAULT_COMPONENT_VERSION,
                    type = ComponentType.UI,
                    description = "核心UI组件库",
                    author = "Unify Team",
                    size = 2048L,
                    checksum = "abc123",
                    dependencies = emptyList(),
                    permissions = listOf("UI_ACCESS"),
                    metadata = mapOf("platform" to "multiplatform"),
                ),
                ComponentRegistration(
                    id = "unify-data-core",
                    name = "Unify Data Core",
                    version = DEFAULT_COMPONENT_VERSION,
                    type = ComponentType.DATA,
                    description = "核心数据管理组件",
                    author = "Unify Team",
                    size = 1536L,
                    checksum = "def456",
                    dependencies = emptyList(),
                    permissions = listOf("DATA_ACCESS", "STORAGE_ACCESS"),
                    metadata = mapOf("platform" to "multiplatform"),
                ),
                ComponentRegistration(
                    id = "unify-network-core",
                    name = "Unify Network Core",
                    version = DEFAULT_COMPONENT_VERSION,
                    type = ComponentType.NETWORK,
                    description = "核心网络组件",
                    author = "Unify Team",
                    size = 1024L,
                    checksum = "ghi789",
                    dependencies = emptyList(),
                    permissions = listOf("NETWORK_ACCESS"),
                    metadata = mapOf("platform" to "multiplatform"),
                ),
                ComponentRegistration(
                    id = "unify-ai-engine",
                    name = "Unify AI Engine",
                    version = DEFAULT_COMPONENT_VERSION,
                    type = ComponentType.AI,
                    description = "AI引擎组件",
                    author = "Unify Team",
                    size = 5120L,
                    checksum = "jkl012",
                    dependencies = listOf("unify-network-core"),
                    permissions = listOf("AI_ACCESS", "NETWORK_ACCESS"),
                    metadata = mapOf("platform" to "multiplatform", "gpu_required" to "false"),
                ),
            )

        val componentsMap = coreComponents.associateBy { it.id }
        _registeredComponents.value = componentsMap

        // 设置依赖关系
        val dependencies =
            mapOf(
                "unify-ai-engine" to listOf("unify-network-core"),
            )
        _componentDependencies.value = dependencies
    }

    /**
     * 启动监控服务
     */
    private fun startMonitoringServices() {
        kotlinx.coroutines.GlobalScope.launch {
            while (_managementState.value == ManagementState.RUNNING) {
                performHealthCheck()
                delay(HEALTH_CHECK_INTERVAL_MS)
            }
        }

        kotlinx.coroutines.GlobalScope.launch {
            while (_managementState.value == ManagementState.RUNNING) {
                performCleanup()
                delay(CLEANUP_INTERVAL_MS)
            }
        }
    }

    /**
     * 注册组件
     */
    fun registerComponent(registration: ComponentRegistration): ComponentRegistrationResult {
        if (_registeredComponents.value.size >= MAX_COMPONENTS) {
            return ComponentRegistrationResult.Error("组件注册表已满")
        }

        if (_registeredComponents.value.containsKey(registration.id)) {
            return ComponentRegistrationResult.Error("组件ID已存在: ${registration.id}")
        }

        if (registration.size > MAX_COMPONENT_SIZE_MB * 1024 * 1024) {
            return ComponentRegistrationResult.Error("组件大小超过限制: ${registration.size} bytes")
        }

        // 验证依赖关系
        val dependencyValidation = validateDependencies(registration.dependencies)
        if (!dependencyValidation.isValid) {
            return ComponentRegistrationResult.Error("依赖验证失败: ${dependencyValidation.message}")
        }

        val currentComponents = _registeredComponents.value.toMutableMap()
        currentComponents[registration.id] = registration
        _registeredComponents.value = currentComponents

        // 更新依赖关系
        if (registration.dependencies.isNotEmpty()) {
            val currentDeps = _componentDependencies.value.toMutableMap()
            currentDeps[registration.id] = registration.dependencies
            _componentDependencies.value = currentDeps
        }

        return ComponentRegistrationResult.Success("组件注册成功: ${registration.id}")
    }

    /**
     * 注销组件
     */
    suspend fun unregisterComponent(componentId: String): ComponentRegistrationResult {
        val component =
            _registeredComponents.value[componentId]
                ?: return ComponentRegistrationResult.Error("组件不存在: $componentId")

        // 检查是否有其他组件依赖此组件
        val dependents = findDependents(componentId)
        if (dependents.isNotEmpty()) {
            return ComponentRegistrationResult.Error("无法注销组件，存在依赖组件: ${dependents.joinToString()}")
        }

        // 如果组件已加载，先卸载
        if (_loadedComponents.value.containsKey(componentId)) {
            val unloadResult = unloadComponent(componentId)
            if (unloadResult !is ComponentOperationResult.Success) {
                return ComponentRegistrationResult.Error("卸载组件失败: $componentId")
            }
        }

        val currentComponents = _registeredComponents.value.toMutableMap()
        currentComponents.remove(componentId)
        _registeredComponents.value = currentComponents

        // 清理依赖关系
        val currentDeps = _componentDependencies.value.toMutableMap()
        currentDeps.remove(componentId)
        _componentDependencies.value = currentDeps

        // 清理指标
        val currentMetrics = _componentMetrics.value.toMutableMap()
        currentMetrics.remove(componentId)
        _componentMetrics.value = currentMetrics

        return ComponentRegistrationResult.Success("组件注销成功: $componentId")
    }

    /**
     * 加载组件
     */
    suspend fun loadComponent(componentId: String): ComponentOperationResult {
        val registration =
            _registeredComponents.value[componentId]
                ?: return ComponentOperationResult.Error("组件未注册: $componentId")

        if (_loadedComponents.value.containsKey(componentId)) {
            return ComponentOperationResult.Error("组件已加载: $componentId")
        }

        return try {
            // 加载依赖组件
            val dependencyResult = loadDependencies(registration.dependencies)
            if (dependencyResult !is ComponentOperationResult.Success) {
                return dependencyResult
            }

            // 模拟组件加载过程
            delay(kotlin.random.Random.nextLong(1000, 3000))

            val loadedComponent =
                LoadedComponent(
                    id = componentId,
                    registration = registration,
                    loadTime = getCurrentTimeMillis(),
                    status = ComponentStatus.RUNNING,
                    instanceId = kotlin.random.Random.nextInt().toString(),
                    memoryUsage = kotlin.random.Random.nextLong(10, 100) * 1024 * 1024, // MB转换为bytes
                    cpuUsage = kotlin.random.Random.nextDouble(1.0, 20.0),
                )

            val currentLoaded = _loadedComponents.value.toMutableMap()
            currentLoaded[componentId] = loadedComponent
            _loadedComponents.value = currentLoaded

            // 初始化组件指标
            val metrics =
                ComponentMetrics(
                    componentId = componentId,
                    loadCount = 1,
                    errorCount = 0,
                    lastAccess = getCurrentTimeMillis(),
                    totalExecutionTime = 0L,
                    averageResponseTime = 0L,
                )

            val currentMetrics = _componentMetrics.value.toMutableMap()
            currentMetrics[componentId] = metrics
            _componentMetrics.value = currentMetrics

            ComponentOperationResult.Success("组件加载成功: $componentId")
        } catch (e: Exception) {
            ComponentOperationResult.Error("组件加载失败: ${e.message}")
        }
    }

    /**
     * 卸载组件
     */
    suspend fun unloadComponent(componentId: String): ComponentOperationResult {
        val loadedComponent =
            _loadedComponents.value[componentId]
                ?: return ComponentOperationResult.Error("组件未加载: $componentId")

        // 检查是否有其他已加载的组件依赖此组件
        val loadedDependents = findLoadedDependents(componentId)
        if (loadedDependents.isNotEmpty()) {
            return ComponentOperationResult.Error("无法卸载组件，存在已加载的依赖组件: ${loadedDependents.joinToString()}")
        }

        return try {
            // 模拟组件卸载过程
            delay(kotlin.random.Random.nextLong(500, 1500))

            val currentLoaded = _loadedComponents.value.toMutableMap()
            currentLoaded.remove(componentId)
            _loadedComponents.value = currentLoaded

            ComponentOperationResult.Success("组件卸载成功: $componentId")
        } catch (e: Exception) {
            ComponentOperationResult.Error("组件卸载失败: ${e.message}")
        }
    }

    /**
     * 重新加载组件
     */
    suspend fun reloadComponent(componentId: String): ComponentOperationResult {
        val unloadResult = unloadComponent(componentId)
        if (unloadResult !is ComponentOperationResult.Success) {
            return unloadResult
        }

        delay(1000) // 等待卸载完成

        return loadComponent(componentId)
    }

    /**
     * 更新组件
     */
    suspend fun updateComponent(
        componentId: String,
        newRegistration: ComponentRegistration,
    ): ComponentOperationResult {
        val currentRegistration =
            _registeredComponents.value[componentId]
                ?: return ComponentOperationResult.Error("组件不存在: $componentId")

        val isLoaded = _loadedComponents.value.containsKey(componentId)

        return try {
            // 如果组件已加载，先卸载
            if (isLoaded) {
                val unloadResult = unloadComponent(componentId)
                if (unloadResult !is ComponentOperationResult.Success) {
                    return unloadResult
                }
            }

            // 更新注册信息
            val currentComponents = _registeredComponents.value.toMutableMap()
            currentComponents[componentId] = newRegistration
            _registeredComponents.value = currentComponents

            // 如果之前已加载，重新加载
            if (isLoaded) {
                val loadResult = loadComponent(componentId)
                if (loadResult !is ComponentOperationResult.Success) {
                    return loadResult
                }
            }

            ComponentOperationResult.Success("组件更新成功: $componentId")
        } catch (e: Exception) {
            ComponentOperationResult.Error("组件更新失败: ${e.message}")
        }
    }

    /**
     * 验证依赖关系
     */
    private fun validateDependencies(dependencies: List<String>): DependencyValidationResult {
        dependencies.forEach { depId ->
            if (!_registeredComponents.value.containsKey(depId)) {
                return DependencyValidationResult(false, "依赖组件不存在: $depId")
            }
        }

        // 检查循环依赖
        dependencies.forEach { depId ->
            if (hasCircularDependency(depId, dependencies)) {
                return DependencyValidationResult(false, "检测到循环依赖: $depId")
            }
        }

        return DependencyValidationResult(true, "依赖验证通过")
    }

    /**
     * 检查循环依赖
     */
    private fun hasCircularDependency(
        componentId: String,
        targetDeps: List<String>,
    ): Boolean {
        val visited = mutableSetOf<String>()

        fun checkCircular(currentId: String): Boolean {
            if (visited.contains(currentId)) {
                return true
            }

            if (targetDeps.contains(currentId)) {
                return true
            }

            visited.add(currentId)

            val deps = _componentDependencies.value[currentId] ?: emptyList()
            return deps.any { checkCircular(it) }
        }

        return checkCircular(componentId)
    }

    /**
     * 加载依赖组件
     */
    private suspend fun loadDependencies(dependencies: List<String>): ComponentOperationResult {
        dependencies.forEach { depId ->
            if (!_loadedComponents.value.containsKey(depId)) {
                val loadResult = loadComponent(depId)
                if (loadResult !is ComponentOperationResult.Success) {
                    return ComponentOperationResult.Error("加载依赖组件失败: $depId")
                }
            }
        }

        return ComponentOperationResult.Success("依赖组件加载完成")
    }

    /**
     * 查找依赖此组件的组件
     */
    private fun findDependents(componentId: String): List<String> {
        return _componentDependencies.value.entries
            .filter { it.value.contains(componentId) }
            .map { it.key }
    }

    /**
     * 查找已加载的依赖组件
     */
    private fun findLoadedDependents(componentId: String): List<String> {
        val dependents = findDependents(componentId)
        return dependents.filter { _loadedComponents.value.containsKey(it) }
    }

    /**
     * 执行健康检查
     */
    private suspend fun performHealthCheck() {
        val currentLoaded = _loadedComponents.value.toMutableMap()
        val currentMetrics = _componentMetrics.value.toMutableMap()

        currentLoaded.forEach { (componentId, component) ->
            try {
                // 模拟健康检查
                delay(50)

                val isHealthy = kotlin.random.Random.nextDouble() > 0.05 // 95%健康率
                val newStatus = if (isHealthy) ComponentStatus.RUNNING else ComponentStatus.ERROR

                if (component.status != newStatus) {
                    currentLoaded[componentId] = component.copy(status = newStatus)
                }

                // 更新指标
                val metrics = currentMetrics[componentId]
                if (metrics != null) {
                    currentMetrics[componentId] =
                        metrics.copy(
                            lastAccess = getCurrentTimeMillis(),
                            errorCount = if (isHealthy) metrics.errorCount else metrics.errorCount + 1,
                        )
                }
            } catch (e: Exception) {
                currentLoaded[componentId] = component.copy(status = ComponentStatus.ERROR)
            }
        }

        _loadedComponents.value = currentLoaded
        _componentMetrics.value = currentMetrics
    }

    /**
     * 执行清理任务
     */
    private fun performCleanup() {
        // 清理长时间未使用的组件指标
        val currentTime = getCurrentTimeMillis()
        val currentMetrics = _componentMetrics.value.toMutableMap()

        val expiredMetrics =
            currentMetrics.entries.filter { (_, metrics) ->
                currentTime - metrics.lastAccess > 3600000L // 1小时未访问
            }

        expiredMetrics.forEach { (componentId, _) ->
            if (!_loadedComponents.value.containsKey(componentId)) {
                currentMetrics.remove(componentId)
            }
        }

        if (expiredMetrics.isNotEmpty()) {
            _componentMetrics.value = currentMetrics
        }
    }

    /**
     * 获取组件信息
     */
    fun getComponentInfo(componentId: String): ComponentInfo? {
        val registration = _registeredComponents.value[componentId] ?: return null
        val loaded = _loadedComponents.value[componentId]
        val metrics = _componentMetrics.value[componentId]

        return ComponentInfo(
            component =
                DynamicComponent(
                    id = componentId,
                    name = registration.name,
                    version = registration.version,
                    type =
                        when (registration.type) {
                            ComponentType.CORE -> DynamicComponentType.BUSINESS_LOGIC
                            ComponentType.UI -> DynamicComponentType.COMPOSE_UI
                            ComponentType.DATA -> DynamicComponentType.BUSINESS_LOGIC
                            ComponentType.NETWORK -> DynamicComponentType.BUSINESS_LOGIC
                            ComponentType.DEVICE -> DynamicComponentType.BUSINESS_LOGIC
                            ComponentType.AI -> DynamicComponentType.BUSINESS_LOGIC
                            ComponentType.SECURITY -> DynamicComponentType.BUSINESS_LOGIC
                            ComponentType.PERFORMANCE -> DynamicComponentType.BUSINESS_LOGIC
                            ComponentType.PLATFORM -> DynamicComponentType.BUSINESS_LOGIC
                            ComponentType.CUSTOM -> DynamicComponentType.BUSINESS_LOGIC
                        },
                    metadata = registration.metadata,
                    dependencies = registration.dependencies,
                    config = emptyMap(),
                    content = "",
                    checksum = registration.checksum,
                    signature = "",
                ),
            state = ComponentState.LOADED,
            registration = registration,
            loaded = loaded,
            metrics = metrics,
            dependencies = _componentDependencies.value[componentId] ?: emptyList(),
            dependents = findDependents(componentId),
        )
    }

    /**
     * 获取所有组件状态
     */
    fun getAllComponentsStatus(): Map<String, ComponentStatusInfo> {
        return _registeredComponents.value.mapValues { (componentId, registration) ->
            val loaded = _loadedComponents.value[componentId]
            val metrics = _componentMetrics.value[componentId]

            ComponentStatusInfo(
                id = componentId,
                name = registration.name,
                version = registration.version,
                type = registration.type,
                isLoaded = loaded != null,
                status = loaded?.status ?: ComponentStatus.UNLOADED,
                loadTime = loaded?.loadTime,
                memoryUsage = loaded?.memoryUsage ?: 0L,
                cpuUsage = loaded?.cpuUsage ?: 0.0,
                errorCount = metrics?.errorCount ?: 0,
                lastAccess = metrics?.lastAccess,
            )
        }
    }

    /**
     * 获取管理统计信息
     */
    fun getManagementStats(): ManagementStats {
        val registered = _registeredComponents.value
        val loaded = _loadedComponents.value
        val metrics = _componentMetrics.value

        return ManagementStats(
            totalRegistered = registered.size,
            totalLoaded = loaded.size,
            runningComponents = loaded.values.count { it.status == ComponentStatus.RUNNING },
            errorComponents = loaded.values.count { it.status == ComponentStatus.ERROR },
            totalMemoryUsage = loaded.values.sumOf { it.memoryUsage },
            averageCpuUsage = loaded.values.takeIf { it.isNotEmpty() }?.map { it.cpuUsage }?.average() ?: 0.0,
            totalErrors = metrics.values.sumOf { it.errorCount },
            componentsByType = registered.values.groupBy { it.type }.mapValues { it.value.size },
        )
    }

    /**
     * 关闭管理组件
     */
    suspend fun shutdown() {
        _managementState.value = ManagementState.SHUTTING_DOWN

        // 卸载所有组件
        val loadedComponentIds = _loadedComponents.value.keys.toList()
        loadedComponentIds.forEach { componentId ->
            unloadComponent(componentId)
        }

        _managementState.value = ManagementState.STOPPED
    }
}

/**
 * 管理状态枚举
 */
enum class ManagementState {
    IDLE,
    INITIALIZING,
    RUNNING,
    SHUTTING_DOWN,
    STOPPED,
    ERROR,
}

// ComponentType已在UnifyArchitecture.kt中定义，此处移除重复声明

/**
 * 组件状态枚举
 */
enum class ComponentStatus {
    UNLOADED,
    LOADING,
    RUNNING,
    ERROR,
    STOPPING,
}

/**
 * 组件注册信息
 */
@Serializable
data class ComponentRegistration(
    val id: String,
    val name: String,
    val version: String,
    val type: ComponentType,
    val description: String,
    val author: String,
    val size: Long,
    val checksum: String,
    val dependencies: List<String>,
    val permissions: List<String>,
    val metadata: Map<String, String>,
)

/**
 * 已加载组件信息
 */
@Serializable
data class LoadedComponent(
    val id: String,
    val registration: ComponentRegistration,
    val loadTime: Long,
    val status: ComponentStatus,
    val instanceId: String,
    val memoryUsage: Long,
    val cpuUsage: Double,
)

/**
 * 组件指标信息
 */
@Serializable
data class ComponentMetrics(
    val componentId: String,
    val loadCount: Int,
    val errorCount: Int,
    val lastAccess: Long,
    val totalExecutionTime: Long,
    val averageResponseTime: Long,
)

// ComponentInfo已移至UnifyDynamicEngine.kt中统一定义

/**
 * 组件状态信息
 */
@Serializable
data class ComponentStatusInfo(
    val id: String,
    val name: String,
    val version: String,
    val type: ComponentType,
    val isLoaded: Boolean,
    val status: ComponentStatus,
    val loadTime: Long?,
    val memoryUsage: Long,
    val cpuUsage: Double,
    val errorCount: Int,
    val lastAccess: Long?,
)

/**
 * 管理统计信息
 */
@Serializable
data class ManagementStats(
    val totalRegistered: Int,
    val totalLoaded: Int,
    val runningComponents: Int,
    val errorComponents: Int,
    val totalMemoryUsage: Long,
    val averageCpuUsage: Double,
    val totalErrors: Int,
    val componentsByType: Map<ComponentType, Int>,
)

/**
 * 依赖验证结果
 */
data class DependencyValidationResult(
    val isValid: Boolean,
    val message: String,
)

/**
 * 组件注册结果密封类
 */
sealed class ComponentRegistrationResult {
    data class Success(val message: String) : ComponentRegistrationResult()

    data class Error(val message: String) : ComponentRegistrationResult()
}

/**
 * 组件操作结果密封类
 */
sealed class ComponentOperationResult {
    data class Success(val message: String) : ComponentOperationResult()

    data class Error(val message: String) : ComponentOperationResult()
}
