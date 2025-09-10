package com.unify.core.security

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

/**
 * 统一权限管理器
 * 提供基于角色的访问控制(RBAC)和细粒度权限管理功能
 */
class UnifyPermissionManager(
    private val config: PermissionConfig = PermissionConfig()
) {
    private val _permissionState = MutableStateFlow(PermissionState.INITIALIZING)
    val permissionState: StateFlow<PermissionState> = _permissionState.asStateFlow()
    
    // 用户和角色管理
    private val users = mutableMapOf<String, User>()
    private val roles = mutableMapOf<String, Role>()
    private val permissions = mutableMapOf<String, Permission>()
    
    // 权限缓存和会话管理
    private val permissionCache = PermissionCache()
    private val sessionManager = PermissionSessionManager()
    private val auditLogger = PermissionAuditLogger()
    
    // 权限策略引擎
    private val policyEngine = PermissionPolicyEngine()
    
    // 统计信息
    private val _permissionStats = MutableStateFlow(PermissionStats())
    val permissionStats: StateFlow<PermissionStats> = _permissionStats.asStateFlow()
    
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    init {
        initializePermissionSystem()
    }
    
    /**
     * 检查用户权限
     */
    suspend fun checkPermission(
        userId: String,
        resource: String,
        action: String,
        context: Map<String, Any> = emptyMap()
    ): PermissionCheckResult {
        return try {
            // 记录权限检查请求
            auditLogger.logPermissionCheck(userId, resource, action, context)
            
            // 从缓存获取权限
            val cachedResult = permissionCache.getPermission(userId, resource, action)
            if (cachedResult != null && !cachedResult.isExpired()) {
                updateStats { it.copy(cacheHits = it.cacheHits + 1) }
                return PermissionCheckResult.Success(cachedResult.granted, "缓存权限")
            }
            
            // 获取用户信息
            val user = users[userId] 
                ?: return PermissionCheckResult.Error("用户不存在: $userId")
            
            // 检查用户是否被禁用
            if (!user.isActive) {
                return PermissionCheckResult.Denied("用户已被禁用")
            }
            
            // 检查会话有效性
            if (!sessionManager.isSessionValid(userId)) {
                return PermissionCheckResult.Denied("会话已过期")
            }
            
            // 执行权限检查
            val hasPermission = performPermissionCheck(user, resource, action, context)
            
            // 缓存结果
            val cacheEntry = PermissionCacheEntry(
                userId = userId,
                resource = resource,
                action = action,
                granted = hasPermission,
                timestamp = com.unify.core.platform.getCurrentTimeMillis(),
                ttl = config.cacheTimeout
            )
            permissionCache.setPermission(cacheEntry)
            
            // 更新统计
            updateStats { stats ->
                stats.copy(
                    totalChecks = stats.totalChecks + 1,
                    grantedChecks = if (hasPermission) stats.grantedChecks + 1 else stats.grantedChecks,
                    deniedChecks = if (!hasPermission) stats.deniedChecks + 1 else stats.deniedChecks
                )
            }
            
            if (hasPermission) {
                PermissionCheckResult.Success(true, "权限检查通过")
            } else {
                PermissionCheckResult.Denied("权限不足")
            }
        } catch (e: Exception) {
            auditLogger.logError("权限检查失败", userId, resource, action, e)
            PermissionCheckResult.Error("权限检查失败: ${e.message}")
        }
    }
    
    /**
     * 创建用户
     */
    suspend fun createUser(userRequest: CreateUserRequest): UserCreationResult {
        return try {
            if (users.containsKey(userRequest.userId)) {
                return UserCreationResult.Error("用户已存在: ${userRequest.userId}")
            }
            
            val user = User(
                id = userRequest.userId,
                username = userRequest.username,
                email = userRequest.email,
                roles = userRequest.roles.toMutableSet(),
                directPermissions = mutableSetOf(),
                isActive = true,
                createdAt = com.unify.core.platform.getCurrentTimeMillis(),
                lastLoginAt = null,
                metadata = userRequest.metadata.toMutableMap()
            )
            
            users[user.id] = user
            auditLogger.logUserCreation(user.id, userRequest.createdBy)
            
            updateStats { it.copy(totalUsers = it.totalUsers + 1) }
            
            UserCreationResult.Success(user)
        } catch (e: Exception) {
            UserCreationResult.Error("用户创建失败: ${e.message}")
        }
    }
    
    /**
     * 分配角色给用户
     */
    suspend fun assignRole(
        userId: String,
        roleId: String,
        assignedBy: String
    ): RoleAssignmentResult {
        return try {
            val user = users[userId] 
                ?: return RoleAssignmentResult.Error("用户不存在: $userId")
            
            val role = roles[roleId] 
                ?: return RoleAssignmentResult.Error("角色不存在: $roleId")
            
            if (user.roles.contains(roleId)) {
                return RoleAssignmentResult.Error("用户已拥有该角色")
            }
            
            user.roles.add(roleId)
            permissionCache.invalidateUser(userId)
            
            auditLogger.logRoleAssignment(userId, roleId, assignedBy)
            
            updateStats { it.copy(roleAssignments = it.roleAssignments + 1) }
            
            RoleAssignmentResult.Success("角色分配成功")
        } catch (e: Exception) {
            RoleAssignmentResult.Error("角色分配失败: ${e.message}")
        }
    }
    
    /**
     * 撤销用户角色
     */
    suspend fun revokeRole(
        userId: String,
        roleId: String,
        revokedBy: String
    ): RoleRevocationResult {
        return try {
            val user = users[userId] 
                ?: return RoleRevocationResult.Error("用户不存在: $userId")
            
            if (!user.roles.contains(roleId)) {
                return RoleRevocationResult.Error("用户未拥有该角色")
            }
            
            user.roles.remove(roleId)
            permissionCache.invalidateUser(userId)
            
            auditLogger.logRoleRevocation(userId, roleId, revokedBy)
            
            updateStats { it.copy(roleRevocations = it.roleRevocations + 1) }
            
            RoleRevocationResult.Success("角色撤销成功")
        } catch (e: Exception) {
            RoleRevocationResult.Error("角色撤销失败: ${e.message}")
        }
    }
    
    /**
     * 创建角色
     */
    suspend fun createRole(roleRequest: CreateRoleRequest): RoleCreationResult {
        return try {
            if (roles.containsKey(roleRequest.roleId)) {
                return RoleCreationResult.Error("角色已存在: ${roleRequest.roleId}")
            }
            
            val role = Role(
                id = roleRequest.roleId,
                name = roleRequest.name,
                description = roleRequest.description,
                permissions = roleRequest.permissions.toMutableSet(),
                isActive = true,
                createdAt = com.unify.core.platform.getCurrentTimeMillis(),
                metadata = roleRequest.metadata.toMutableMap()
            )
            
            roles[role.id] = role
            auditLogger.logRoleCreation(role.id, roleRequest.createdBy)
            
            updateStats { it.copy(totalRoles = it.totalRoles + 1) }
            
            RoleCreationResult.Success(role)
        } catch (e: Exception) {
            RoleCreationResult.Error("角色创建失败: ${e.message}")
        }
    }
    
    /**
     * 创建权限
     */
    suspend fun createPermission(permissionRequest: CreatePermissionRequest): PermissionCreationResult {
        return try {
            if (permissions.containsKey(permissionRequest.permissionId)) {
                return PermissionCreationResult.Error("权限已存在: ${permissionRequest.permissionId}")
            }
            
            val permission = Permission(
                id = permissionRequest.permissionId,
                name = permissionRequest.name,
                description = permissionRequest.description,
                resource = permissionRequest.resource,
                actions = permissionRequest.actions.toMutableSet(),
                conditions = permissionRequest.conditions.toMutableList(),
                isActive = true,
                createdAt = com.unify.core.platform.getCurrentTimeMillis()
            )
            
            permissions[permission.id] = permission
            auditLogger.logPermissionCreation(permission.id, permissionRequest.createdBy)
            
            updateStats { it.copy(totalPermissions = it.totalPermissions + 1) }
            
            PermissionCreationResult.Success(permission)
        } catch (e: Exception) {
            PermissionCreationResult.Error("权限创建失败: ${e.message}")
        }
    }
    
    /**
     * 批量权限检查
     */
    suspend fun batchCheckPermissions(
        userId: String,
        requests: List<PermissionRequest>
    ): BatchPermissionResult {
        return try {
            val results = requests.map { request ->
                val result = checkPermission(userId, request.resource, request.action, request.context)
                BatchPermissionItem(
                    resource = request.resource,
                    action = request.action,
                    granted = result is PermissionCheckResult.Success && result.granted,
                    reason = when (result) {
                        is PermissionCheckResult.Success -> result.message
                        is PermissionCheckResult.Denied -> result.reason
                        is PermissionCheckResult.Error -> result.message
                    }
                )
            }
            
            BatchPermissionResult.Success(results)
        } catch (e: Exception) {
            BatchPermissionResult.Error("批量权限检查失败: ${e.message}")
        }
    }
    
    /**
     * 获取用户权限列表
     */
    suspend fun getUserPermissions(userId: String): UserPermissionsResult {
        return try {
            val user = users[userId] 
                ?: return UserPermissionsResult.Error("用户不存在: $userId")
            
            val allPermissions = mutableSetOf<String>()
            
            // 添加直接权限
            allPermissions.addAll(user.directPermissions)
            
            // 添加角色权限
            user.roles.forEach { roleId ->
                roles[roleId]?.let { role ->
                    allPermissions.addAll(role.permissions)
                }
            }
            
            // 解析权限详情
            val permissionDetails = allPermissions.mapNotNull { permId ->
                permissions[permId]
            }
            
            UserPermissionsResult.Success(permissionDetails)
        } catch (e: Exception) {
            UserPermissionsResult.Error("获取用户权限失败: ${e.message}")
        }
    }
    
    /**
     * 权限策略评估
     */
    suspend fun evaluatePolicy(
        userId: String,
        resource: String,
        action: String,
        context: Map<String, Any>
    ): PolicyEvaluationResult {
        return try {
            val user = users[userId] 
                ?: return PolicyEvaluationResult.Error("用户不存在: $userId")
            
            val evaluation = policyEngine.evaluate(user, resource, action, context, permissions.values)
            
            PolicyEvaluationResult.Success(evaluation)
        } catch (e: Exception) {
            PolicyEvaluationResult.Error("策略评估失败: ${e.message}")
        }
    }
    
    /**
     * 获取权限审计日志
     */
    fun getAuditLogs(
        userId: String? = null,
        resource: String? = null,
        startTime: Long? = null,
        endTime: Long? = null,
        limit: Int = 100
    ): List<AuditLogEntry> {
        return auditLogger.getLogs(userId, resource, startTime, endTime, limit)
    }
    
    /**
     * 清理过期会话和缓存
     */
    suspend fun cleanupExpiredData() {
        try {
            sessionManager.cleanupExpiredSessions()
            permissionCache.cleanupExpired()
            auditLogger.cleanupOldLogs(config.auditLogRetention)
            
            updateStats { it.copy(cleanupOperations = it.cleanupOperations + 1) }
        } catch (e: Exception) {
            println("清理过期数据失败: ${e.message}")
        }
    }
    
    /**
     * 获取权限统计信息
     */
    fun getPermissionStatistics(): PermissionStatistics {
        val stats = _permissionStats.value
        return PermissionStatistics(
            totalUsers = users.size,
            activeUsers = users.values.count { it.isActive },
            totalRoles = roles.size,
            activeRoles = roles.values.count { it.isActive },
            totalPermissions = permissions.size,
            activePermissions = permissions.values.count { it.isActive },
            totalChecks = stats.totalChecks,
            grantedChecks = stats.grantedChecks,
            deniedChecks = stats.deniedChecks,
            cacheHitRate = if (stats.totalChecks > 0) stats.cacheHits.toDouble() / stats.totalChecks else 0.0,
            averageCheckTime = stats.averageCheckTime,
            permissionState = _permissionState.value
        )
    }
    
    /**
     * 执行权限检查逻辑
     */
    private suspend fun performPermissionCheck(
        user: User,
        resource: String,
        action: String,
        context: Map<String, Any>
    ): Boolean {
        // 检查直接权限
        if (hasDirectPermission(user, resource, action, context)) {
            return true
        }
        
        // 检查角色权限
        if (hasRolePermission(user, resource, action, context)) {
            return true
        }
        
        // 检查动态策略
        if (policyEngine.evaluateDynamicPolicy(user, resource, action, context)) {
            return true
        }
        
        return false
    }
    
    /**
     * 检查直接权限
     */
    private fun hasDirectPermission(
        user: User,
        resource: String,
        action: String,
        context: Map<String, Any>
    ): Boolean {
        return user.directPermissions.any { permId ->
            val permission = permissions[permId]
            permission != null && 
            permission.isActive && 
            permission.resource == resource &&
            permission.actions.contains(action) &&
            evaluateConditions(permission.conditions, context)
        }
    }
    
    /**
     * 检查角色权限
     */
    private fun hasRolePermission(
        user: User,
        resource: String,
        action: String,
        context: Map<String, Any>
    ): Boolean {
        return user.roles.any { roleId ->
            val role = roles[roleId]
            role != null && role.isActive && role.permissions.any { permId ->
                val permission = permissions[permId]
                permission != null && 
                permission.isActive && 
                permission.resource == resource &&
                permission.actions.contains(action) &&
                evaluateConditions(permission.conditions, context)
            }
        }
    }
    
    /**
     * 评估权限条件
     */
    private fun evaluateConditions(
        conditions: List<PermissionCondition>,
        context: Map<String, Any>
    ): Boolean {
        return conditions.all { condition ->
            when (condition.type) {
                ConditionType.TIME_RANGE -> evaluateTimeCondition(condition, context)
                ConditionType.IP_RANGE -> evaluateIPCondition(condition, context)
                ConditionType.ATTRIBUTE_MATCH -> evaluateAttributeCondition(condition, context)
                ConditionType.CUSTOM -> evaluateCustomCondition(condition, context)
            }
        }
    }
    
    /**
     * 评估时间条件
     */
    private fun evaluateTimeCondition(condition: PermissionCondition, context: Map<String, Any>): Boolean {
        val currentTime = com.unify.core.platform.getCurrentTimeMillis()
        val startTime = condition.value["startTime"] as? Long ?: 0L
        val endTime = condition.value["endTime"] as? Long ?: Long.MAX_VALUE
        return currentTime in startTime..endTime
    }
    
    /**
     * 评估IP条件
     */
    private fun evaluateIPCondition(condition: PermissionCondition, context: Map<String, Any>): Boolean {
        val clientIP = context["clientIP"] as? String ?: return false
        val allowedIPs = condition.value["allowedIPs"] as? List<String> ?: return false
        return allowedIPs.contains(clientIP)
    }
    
    /**
     * 评估属性条件
     */
    private fun evaluateAttributeCondition(condition: PermissionCondition, context: Map<String, Any>): Boolean {
        val attributeName = condition.value["attributeName"] as? String ?: return false
        val expectedValue = condition.value["expectedValue"] ?: return false
        val actualValue = context[attributeName] ?: return false
        return actualValue == expectedValue
    }
    
    /**
     * 评估自定义条件
     */
    private fun evaluateCustomCondition(condition: PermissionCondition, context: Map<String, Any>): Boolean {
        // 自定义条件评估逻辑
        return true
    }
    
    /**
     * 初始化权限系统
     */
    private fun initializePermissionSystem() {
        coroutineScope.launch {
            try {
                _permissionState.value = PermissionState.INITIALIZING
                
                // 初始化默认权限和角色
                initializeDefaultPermissions()
                initializeDefaultRoles()
                
                // 启动清理任务
                startCleanupTask()
                
                _permissionState.value = PermissionState.READY
            } catch (e: Exception) {
                _permissionState.value = PermissionState.ERROR
                println("权限系统初始化失败: ${e.message}")
            }
        }
    }
    
    /**
     * 初始化默认权限
     */
    private suspend fun initializeDefaultPermissions() {
        val defaultPermissions = listOf(
            CreatePermissionRequest(
                permissionId = "read_data",
                name = "读取数据",
                description = "读取系统数据的权限",
                resource = "data",
                actions = setOf("read", "view"),
                conditions = emptyList(),
                createdBy = "system"
            ),
            CreatePermissionRequest(
                permissionId = "write_data",
                name = "写入数据",
                description = "写入系统数据的权限",
                resource = "data",
                actions = setOf("write", "create", "update"),
                conditions = emptyList(),
                createdBy = "system"
            ),
            CreatePermissionRequest(
                permissionId = "delete_data",
                name = "删除数据",
                description = "删除系统数据的权限",
                resource = "data",
                actions = setOf("delete"),
                conditions = emptyList(),
                createdBy = "system"
            )
        )
        
        defaultPermissions.forEach { request ->
            createPermission(request)
        }
    }
    
    /**
     * 初始化默认角色
     */
    private suspend fun initializeDefaultRoles() {
        val defaultRoles = listOf(
            CreateRoleRequest(
                roleId = "admin",
                name = "管理员",
                description = "系统管理员角色",
                permissions = setOf("read_data", "write_data", "delete_data"),
                createdBy = "system"
            ),
            CreateRoleRequest(
                roleId = "user",
                name = "普通用户",
                description = "普通用户角色",
                permissions = setOf("read_data"),
                createdBy = "system"
            ),
            CreateRoleRequest(
                roleId = "editor",
                name = "编辑者",
                description = "编辑者角色",
                permissions = setOf("read_data", "write_data"),
                createdBy = "system"
            )
        )
        
        defaultRoles.forEach { request ->
            createRole(request)
        }
    }
    
    /**
     * 启动清理任务
     */
    private fun startCleanupTask() {
        coroutineScope.launch {
            while (isActive) {
                delay(config.cleanupInterval)
                cleanupExpiredData()
            }
        }
    }
    
    /**
     * 更新统计信息
     */
    private fun updateStats(update: (PermissionStats) -> PermissionStats) {
        _permissionStats.value = update(_permissionStats.value)
    }
    
    /**
     * 清理资源
     */
    fun cleanup() {
        coroutineScope.cancel()
        permissionCache.clear()
        sessionManager.cleanup()
        auditLogger.cleanup()
    }
}

/**
 * 权限配置
 */
@Serializable
data class PermissionConfig(
    val cacheTimeout: Long = 5 * 60 * 1000L, // 5分钟
    val sessionTimeout: Long = 30 * 60 * 1000L, // 30分钟
    val auditLogRetention: Long = 30 * 24 * 60 * 60 * 1000L, // 30天
    val cleanupInterval: Long = 60 * 60 * 1000L, // 1小时
    val maxCacheSize: Int = 10000,
    val enableAuditLog: Boolean = true,
    val enablePermissionCache: Boolean = true
)

/**
 * 权限状态
 */
enum class PermissionState {
    INITIALIZING,   // 初始化中
    READY,          // 就绪
    ERROR           // 错误
}
