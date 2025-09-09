package com.unify.core.architecture

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable

/**
 * Unify架构核心 - 定义统一的架构模式和组件
 */
object UnifyArchitecture {
    const val ARCHITECTURE_VERSION = "1.0.0"
    const val MIN_COMPONENT_VERSION = "1.0.0"
    const val MAX_DEPENDENCY_DEPTH = 10
    const val DEFAULT_TIMEOUT_MS = 5000L
    const val MAX_RETRY_COUNT = 3
    const val COMPONENT_REGISTRY_SIZE = 1000

    private val _components = MutableStateFlow<Map<String, ArchitectureComponent>>(emptyMap())
    val components: StateFlow<Map<String, ArchitectureComponent>> = _components.asStateFlow()

    private val _dependencies = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val dependencies: StateFlow<Map<String, List<String>>> = _dependencies.asStateFlow()

    private val _lifecycle = MutableStateFlow(ArchitectureLifecycle.UNINITIALIZED)
    val lifecycle: StateFlow<ArchitectureLifecycle> = _lifecycle.asStateFlow()

    /**
     * 初始化架构
     */
    suspend fun initialize(): Boolean {
        return try {
            _lifecycle.value = ArchitectureLifecycle.INITIALIZING

            // 注册核心组件
            registerCoreComponents()

            // 验证依赖关系
            validateDependencies()

            // 启动组件
            startComponents()

            _lifecycle.value = ArchitectureLifecycle.RUNNING
            true
        } catch (e: Exception) {
            _lifecycle.value = ArchitectureLifecycle.ERROR
            false
        }
    }

    /**
     * 注册组件
     */
    fun registerComponent(component: ArchitectureComponent): Boolean {
        val currentComponents = _components.value.toMutableMap()

        if (currentComponents.containsKey(component.id)) {
            return false // 组件已存在
        }

        if (currentComponents.size >= COMPONENT_REGISTRY_SIZE) {
            return false // 注册表已满
        }

        currentComponents[component.id] = component
        _components.value = currentComponents

        return true
    }

    /**
     * 注销组件
     */
    fun unregisterComponent(componentId: String): Boolean {
        val currentComponents = _components.value.toMutableMap()

        if (!currentComponents.containsKey(componentId)) {
            return false // 组件不存在
        }

        // 检查是否有其他组件依赖此组件
        val hasDependents =
            _dependencies.value.values.any { deps ->
                deps.contains(componentId)
            }

        if (hasDependents) {
            return false // 有其他组件依赖此组件
        }

        currentComponents.remove(componentId)
        _components.value = currentComponents

        // 清理依赖关系
        val currentDeps = _dependencies.value.toMutableMap()
        currentDeps.remove(componentId)
        _dependencies.value = currentDeps

        return true
    }

    /**
     * 获取组件
     */
    fun getComponent(componentId: String): ArchitectureComponent? {
        return _components.value[componentId]
    }

    /**
     * 添加依赖关系
     */
    fun addDependency(
        componentId: String,
        dependencyId: String,
    ): Boolean {
        // 检查组件是否存在
        if (!_components.value.containsKey(componentId) ||
            !_components.value.containsKey(dependencyId)
        ) {
            return false
        }

        // 检查循环依赖
        if (hasCircularDependency(componentId, dependencyId)) {
            return false
        }

        val currentDeps = _dependencies.value.toMutableMap()
        val componentDeps = currentDeps[componentId]?.toMutableList() ?: mutableListOf()

        if (!componentDeps.contains(dependencyId)) {
            componentDeps.add(dependencyId)
            currentDeps[componentId] = componentDeps
            _dependencies.value = currentDeps
        }

        return true
    }

    /**
     * 移除依赖关系
     */
    fun removeDependency(
        componentId: String,
        dependencyId: String,
    ): Boolean {
        val currentDeps = _dependencies.value.toMutableMap()
        val componentDeps = currentDeps[componentId]?.toMutableList() ?: return false

        val removed = componentDeps.remove(dependencyId)
        if (removed) {
            currentDeps[componentId] = componentDeps
            _dependencies.value = currentDeps
        }

        return removed
    }

    /**
     * 获取组件依赖
     */
    fun getComponentDependencies(componentId: String): List<String> {
        return _dependencies.value[componentId] ?: emptyList()
    }

    /**
     * 获取依赖此组件的组件列表
     */
    fun getDependents(componentId: String): List<String> {
        return _dependencies.value.entries
            .filter { it.value.contains(componentId) }
            .map { it.key }
    }

    /**
     * 检查循环依赖
     */
    private fun hasCircularDependency(
        componentId: String,
        dependencyId: String,
    ): Boolean {
        return hasCircularDependencyRecursive(dependencyId, componentId, mutableSetOf())
    }

    private fun hasCircularDependencyRecursive(
        currentId: String,
        targetId: String,
        visited: MutableSet<String>,
    ): Boolean {
        if (currentId == targetId) {
            return true
        }

        if (visited.contains(currentId) || visited.size > MAX_DEPENDENCY_DEPTH) {
            return true
        }

        visited.add(currentId)

        val deps = _dependencies.value[currentId] ?: return false
        return deps.any { depId ->
            hasCircularDependencyRecursive(depId, targetId, visited)
        }
    }

    /**
     * 验证依赖关系
     */
    private fun validateDependencies(): Boolean {
        val components = _components.value
        val dependencies = _dependencies.value

        // 检查所有依赖的组件是否存在
        dependencies.forEach { (componentId, deps) ->
            if (!components.containsKey(componentId)) {
                throw IllegalStateException("组件不存在: $componentId")
            }

            deps.forEach { depId ->
                if (!components.containsKey(depId)) {
                    throw IllegalStateException("依赖的组件不存在: $depId")
                }
            }
        }

        return true
    }

    /**
     * 注册核心组件
     */
    private fun registerCoreComponents() {
        val coreComponents =
            listOf(
                ArchitectureComponent(
                    id = "unify-core",
                    name = "Unify Core",
                    type = ComponentType.CORE,
                    version = ARCHITECTURE_VERSION,
                    state = ComponentState.REGISTERED,
                ),
                ArchitectureComponent(
                    id = "unify-ui",
                    name = "Unify UI",
                    type = ComponentType.UI,
                    version = ARCHITECTURE_VERSION,
                    state = ComponentState.REGISTERED,
                ),
                ArchitectureComponent(
                    id = "unify-data",
                    name = "Unify Data",
                    type = ComponentType.DATA,
                    version = ARCHITECTURE_VERSION,
                    state = ComponentState.REGISTERED,
                ),
                ArchitectureComponent(
                    id = "unify-network",
                    name = "Unify Network",
                    type = ComponentType.NETWORK,
                    version = ARCHITECTURE_VERSION,
                    state = ComponentState.REGISTERED,
                ),
                ArchitectureComponent(
                    id = "unify-device",
                    name = "Unify Device",
                    type = ComponentType.DEVICE,
                    version = ARCHITECTURE_VERSION,
                    state = ComponentState.REGISTERED,
                ),
                ArchitectureComponent(
                    id = "unify-ai",
                    name = "Unify AI",
                    type = ComponentType.AI,
                    version = ARCHITECTURE_VERSION,
                    state = ComponentState.REGISTERED,
                ),
                ArchitectureComponent(
                    id = "unify-security",
                    name = "Unify Security",
                    type = ComponentType.SECURITY,
                    version = ARCHITECTURE_VERSION,
                    state = ComponentState.REGISTERED,
                ),
                ArchitectureComponent(
                    id = "unify-performance",
                    name = "Unify Performance",
                    type = ComponentType.PERFORMANCE,
                    version = ARCHITECTURE_VERSION,
                    state = ComponentState.REGISTERED,
                ),
            )

        val componentsMap = coreComponents.associateBy { it.id }
        _components.value = componentsMap

        // 设置核心依赖关系
        val coreDependencies =
            mapOf(
                "unify-ui" to listOf("unify-core"),
                "unify-data" to listOf("unify-core"),
                "unify-network" to listOf("unify-core"),
                "unify-device" to listOf("unify-core"),
                "unify-ai" to listOf("unify-core", "unify-network"),
                "unify-security" to listOf("unify-core"),
                "unify-performance" to listOf("unify-core"),
            )

        _dependencies.value = coreDependencies
    }

    /**
     * 启动组件
     */
    private suspend fun startComponents() {
        val sortedComponents = topologicalSort()

        sortedComponents.forEach { componentId ->
            val component = _components.value[componentId]
            if (component != null) {
                updateComponentState(componentId, ComponentState.STARTING)
                // 模拟组件启动
                kotlinx.coroutines.delay(100)
                updateComponentState(componentId, ComponentState.RUNNING)
            }
        }
    }

    /**
     * 拓扑排序 - 确定组件启动顺序
     */
    private fun topologicalSort(): List<String> {
        val result = mutableListOf<String>()
        val visited = mutableSetOf<String>()
        val visiting = mutableSetOf<String>()

        fun visit(componentId: String) {
            if (visiting.contains(componentId)) {
                throw IllegalStateException("检测到循环依赖: $componentId")
            }

            if (!visited.contains(componentId)) {
                visiting.add(componentId)

                val deps = _dependencies.value[componentId] ?: emptyList()
                deps.forEach { depId ->
                    visit(depId)
                }

                visiting.remove(componentId)
                visited.add(componentId)
                result.add(0, componentId) // 添加到开头
            }
        }

        _components.value.keys.forEach { componentId ->
            if (!visited.contains(componentId)) {
                visit(componentId)
            }
        }

        return result.reversed()
    }

    /**
     * 更新组件状态
     */
    private fun updateComponentState(
        componentId: String,
        newState: ComponentState,
    ) {
        val currentComponents = _components.value.toMutableMap()
        val component = currentComponents[componentId]

        if (component != null) {
            currentComponents[componentId] = component.copy(state = newState)
            _components.value = currentComponents
        }
    }

    /**
     * 获取架构统计信息
     */
    fun getArchitectureStats(): ArchitectureStats {
        val components = _components.value
        val dependencies = _dependencies.value

        return ArchitectureStats(
            totalComponents = components.size,
            runningComponents = components.values.count { it.state == ComponentState.RUNNING },
            totalDependencies = dependencies.values.sumOf { it.size },
            architectureState = _lifecycle.value,
            componentsByType = components.values.groupBy { it.type }.mapValues { it.value.size },
        )
    }

    /**
     * 关闭架构
     */
    suspend fun shutdown() {
        _lifecycle.value = ArchitectureLifecycle.SHUTTING_DOWN

        // 按相反顺序关闭组件
        val sortedComponents = topologicalSort().reversed()

        sortedComponents.forEach { componentId ->
            updateComponentState(componentId, ComponentState.STOPPING)
            kotlinx.coroutines.delay(50)
            updateComponentState(componentId, ComponentState.STOPPED)
        }

        _lifecycle.value = ArchitectureLifecycle.STOPPED
    }
}

/**
 * 架构组件数据类
 */
@Serializable
data class ArchitectureComponent(
    val id: String,
    val name: String,
    val type: ComponentType,
    val version: String,
    val state: ComponentState = ComponentState.REGISTERED,
    val metadata: Map<String, String> = emptyMap(),
)

/**
 * 组件类型枚举
 */
enum class ComponentType {
    CORE,
    UI,
    DATA,
    NETWORK,
    DEVICE,
    AI,
    SECURITY,
    PERFORMANCE,
    PLATFORM,
    CUSTOM,
}

/**
 * 组件状态枚举
 */
enum class ComponentState {
    REGISTERED,
    STARTING,
    RUNNING,
    STOPPING,
    STOPPED,
    ERROR,
}

/**
 * 架构生命周期枚举
 */
enum class ArchitectureLifecycle {
    UNINITIALIZED,
    INITIALIZING,
    RUNNING,
    SHUTTING_DOWN,
    STOPPED,
    ERROR,
}

/**
 * 架构统计信息
 */
@Serializable
data class ArchitectureStats(
    val totalComponents: Int,
    val runningComponents: Int,
    val totalDependencies: Int,
    val architectureState: ArchitectureLifecycle,
    val componentsByType: Map<ComponentType, Int>,
)
