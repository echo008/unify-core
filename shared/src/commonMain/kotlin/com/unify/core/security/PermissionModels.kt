package com.unify.core.security

import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual

/**
 * 用户模型
 */
@Serializable
data class User(
    val id: String,
    val username: String,
    val email: String,
    val roles: MutableSet<String>,
    val directPermissions: MutableSet<String>,
    val isActive: Boolean,
    val createdAt: Long,
    val lastLoginAt: Long?,
    val metadata: MutableMap<String, String>
)

/**
 * 角色模型
 */
@Serializable
data class Role(
    val id: String,
    val name: String,
    val description: String,
    val permissions: MutableSet<String>,
    val isActive: Boolean,
    val createdAt: Long,
    val metadata: MutableMap<String, String>
)

/**
 * 权限模型
 */
@Serializable
data class Permission(
    val id: String,
    val name: String,
    val description: String,
    val resource: String,
    val actions: MutableSet<String>,
    val conditions: MutableList<PermissionCondition>,
    val isActive: Boolean,
    val createdAt: Long
)

/**
 * 权限条件
 */
@Serializable
data class PermissionCondition(
    val type: ConditionType,
    val value: Map<String, @Contextual Any>
)

/**
 * 条件类型
 */
enum class ConditionType {
    TIME_RANGE,      // 时间范围
    IP_RANGE,        // IP范围
    ATTRIBUTE_MATCH, // 属性匹配
    CUSTOM          // 自定义条件
}

/**
 * 权限请求
 */
@Serializable
data class PermissionRequest(
    val resource: String,
    val action: String,
    val context: Map<String, @Contextual Any> = emptyMap()
)

/**
 * 创建用户请求
 */
@Serializable
data class CreateUserRequest(
    val userId: String,
    val username: String,
    val email: String,
    val roles: Set<String> = emptySet(),
    val metadata: Map<String, String> = emptyMap(),
    val createdBy: String
)

/**
 * 创建角色请求
 */
@Serializable
data class CreateRoleRequest(
    val roleId: String,
    val name: String,
    val description: String,
    val permissions: Set<String> = emptySet(),
    val metadata: Map<String, String> = emptyMap(),
    val createdBy: String
)

/**
 * 创建权限请求
 */
@Serializable
data class CreatePermissionRequest(
    val permissionId: String,
    val name: String,
    val description: String,
    val resource: String,
    val actions: Set<String>,
    val conditions: List<PermissionCondition> = emptyList(),
    val createdBy: String
)

/**
 * 权限统计
 */
@Serializable
data class PermissionStats(
    val totalChecks: Int = 0,
    val grantedChecks: Int = 0,
    val deniedChecks: Int = 0,
    val cacheHits: Int = 0,
    val roleAssignments: Int = 0,
    val roleRevocations: Int = 0,
    val cleanupOperations: Int = 0,
    val averageCheckTime: Double = 0.0,
    val totalUsers: Int = 0,
    val totalRoles: Int = 0,
    val totalPermissions: Int = 0
)

/**
 * 权限统计信息
 */
data class PermissionStatistics(
    val totalUsers: Int,
    val activeUsers: Int,
    val totalRoles: Int,
    val activeRoles: Int,
    val totalPermissions: Int,
    val activePermissions: Int,
    val totalChecks: Int,
    val grantedChecks: Int,
    val deniedChecks: Int,
    val cacheHitRate: Double,
    val averageCheckTime: Double,
    val permissionState: PermissionState
)

/**
 * 批量权限项
 */
@Serializable
data class BatchPermissionItem(
    val resource: String,
    val action: String,
    val granted: Boolean,
    val reason: String
)

/**
 * 权限缓存条目
 */
@Serializable
data class PermissionCacheEntry(
    val userId: String,
    val resource: String,
    val action: String,
    val granted: Boolean,
    val timestamp: Long,
    val ttl: Long
) {
    fun isExpired(): Boolean {
        return com.unify.core.platform.getCurrentTimeMillis() - timestamp > ttl
    }
}

/**
 * 审计日志条目
 */
@Serializable
data class AuditLogEntry(
    val id: String,
    val userId: String?,
    val action: String,
    val resource: String?,
    val result: String,
    val timestamp: Long,
    val clientIP: String?,
    val userAgent: String?,
    val details: Map<String, String>
)

/**
 * 策略评估结果
 */
@Serializable
data class PolicyEvaluation(
    val granted: Boolean,
    val reason: String,
    val appliedPolicies: List<String>,
    val evaluationTime: Long
)

// 结果类型定义

/**
 * 权限检查结果
 */
sealed class PermissionCheckResult {
    data class Success(val granted: Boolean, val message: String) : PermissionCheckResult()
    data class Denied(val reason: String) : PermissionCheckResult()
    data class Error(val message: String) : PermissionCheckResult()
}

/**
 * 用户创建结果
 */
sealed class UserCreationResult {
    data class Success(val user: User) : UserCreationResult()
    data class Error(val message: String) : UserCreationResult()
}

/**
 * 角色分配结果
 */
sealed class RoleAssignmentResult {
    data class Success(val message: String) : RoleAssignmentResult()
    data class Error(val message: String) : RoleAssignmentResult()
}

/**
 * 角色撤销结果
 */
sealed class RoleRevocationResult {
    data class Success(val message: String) : RoleRevocationResult()
    data class Error(val message: String) : RoleRevocationResult()
}

/**
 * 角色创建结果
 */
sealed class RoleCreationResult {
    data class Success(val role: Role) : RoleCreationResult()
    data class Error(val message: String) : RoleCreationResult()
}

/**
 * 权限创建结果
 */
sealed class PermissionCreationResult {
    data class Success(val permission: Permission) : PermissionCreationResult()
    data class Error(val message: String) : PermissionCreationResult()
}

/**
 * 批量权限结果
 */
sealed class BatchPermissionResult {
    data class Success(val results: List<BatchPermissionItem>) : BatchPermissionResult()
    data class Error(val message: String) : BatchPermissionResult()
}

/**
 * 用户权限结果
 */
sealed class UserPermissionsResult {
    data class Success(val permissions: List<Permission>) : UserPermissionsResult()
    data class Error(val message: String) : UserPermissionsResult()
}

/**
 * 策略评估结果
 */
sealed class PolicyEvaluationResult {
    data class Success(val evaluation: PolicyEvaluation) : PolicyEvaluationResult()
    data class Error(val message: String) : PolicyEvaluationResult()
}
