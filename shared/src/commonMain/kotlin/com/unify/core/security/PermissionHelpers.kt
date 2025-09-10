package com.unify.core.security

import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual
import kotlin.random.Random

/**
 * 权限缓存管理器
 */
class PermissionCache(private val maxSize: Int = 10000) {
    
    private val cache = mutableMapOf<String, PermissionCacheEntry>()
    
    /**
     * 获取权限缓存
     */
    fun getPermission(userId: String, resource: String, action: String): PermissionCacheEntry? {
        val key = generateCacheKey(userId, resource, action)
        val entry = cache[key]
        return if (entry?.isExpired() == false) entry else null
    }
    
    /**
     * 设置权限缓存
     */
    fun setPermission(entry: PermissionCacheEntry) {
        val key = generateCacheKey(entry.userId, entry.resource, entry.action)
        
        // 检查缓存大小限制
        if (cache.size >= maxSize) {
            evictOldestEntry()
        }
        
        cache[key] = entry
    }
    
    /**
     * 使用户权限缓存失效
     */
    fun invalidateUser(userId: String) {
        val keysToRemove = cache.keys.filter { it.startsWith("$userId:") }
        keysToRemove.forEach { cache.remove(it) }
    }
    
    /**
     * 清理过期缓存
     */
    fun cleanupExpired() {
        val currentTime = com.unify.core.platform.getCurrentTimeMillis()
        val expiredKeys = cache.filterValues { it.isExpired() }.keys
        expiredKeys.forEach { cache.remove(it) }
    }
    
    /**
     * 清空缓存
     */
    fun clear() {
        cache.clear()
    }
    
    /**
     * 获取缓存统计
     */
    fun getStats(): CacheStats {
        val currentTime = com.unify.core.platform.getCurrentTimeMillis()
        val expiredCount = cache.values.count { it.isExpired() }
        return CacheStats(
            totalEntries = cache.size,
            expiredEntries = expiredCount,
            activeEntries = cache.size - expiredCount
        )
    }
    
    /**
     * 生成缓存键
     */
    private fun generateCacheKey(userId: String, resource: String, action: String): String {
        return "$userId:$resource:$action"
    }
    
    /**
     * 淘汰最旧的缓存条目
     */
    private fun evictOldestEntry() {
        val oldestEntry = cache.values.minByOrNull { it.timestamp }
        if (oldestEntry != null) {
            val keyToRemove = generateCacheKey(oldestEntry.userId, oldestEntry.resource, oldestEntry.action)
            cache.remove(keyToRemove)
        }
    }
}

/**
 * 权限会话管理器
 */
class PermissionSessionManager {
    
    private val sessions = mutableMapOf<String, UserSession>()
    private val sessionTimeout = 30 * 60 * 1000L // 30分钟
    
    /**
     * 创建用户会话
     */
    fun createSession(userId: String, clientIP: String? = null, userAgent: String? = null): String {
        val sessionId = generateSessionId()
        val session = UserSession(
            sessionId = sessionId,
            userId = userId,
            createdAt = com.unify.core.platform.getCurrentTimeMillis(),
            lastAccessAt = com.unify.core.platform.getCurrentTimeMillis(),
            clientIP = clientIP,
            userAgent = userAgent,
            isActive = true
        )
        sessions[sessionId] = session
        return sessionId
    }
    
    /**
     * 验证会话有效性
     */
    fun isSessionValid(userId: String): Boolean {
        val userSessions = sessions.values.filter { it.userId == userId && it.isActive }
        return userSessions.any { !it.isExpired(sessionTimeout) }
    }
    
    /**
     * 更新会话访问时间
     */
    fun updateSessionAccess(sessionId: String) {
        sessions[sessionId]?.let { session ->
            sessions[sessionId] = session.copy(lastAccessAt = com.unify.core.platform.getCurrentTimeMillis())
        }
    }
    
    /**
     * 终止会话
     */
    fun terminateSession(sessionId: String) {
        sessions[sessionId]?.let { session ->
            sessions[sessionId] = session.copy(isActive = false)
        }
    }
    
    /**
     * 终止用户所有会话
     */
    fun terminateUserSessions(userId: String) {
        sessions.values.filter { it.userId == userId }.forEach { session ->
            sessions[session.sessionId] = session.copy(isActive = false)
        }
    }
    
    /**
     * 清理过期会话
     */
    fun cleanupExpiredSessions() {
        val expiredSessions = sessions.values.filter { it.isExpired(sessionTimeout) }
        expiredSessions.forEach { session ->
            sessions.remove(session.sessionId)
        }
    }
    
    /**
     * 获取会话信息
     */
    fun getSession(sessionId: String): UserSession? {
        return sessions[sessionId]
    }
    
    /**
     * 获取用户活跃会话
     */
    fun getUserActiveSessions(userId: String): List<UserSession> {
        return sessions.values.filter { 
            it.userId == userId && it.isActive && !it.isExpired(sessionTimeout) 
        }
    }
    
    /**
     * 生成会话ID
     */
    private fun generateSessionId(): String {
        return "session_${com.unify.core.platform.getCurrentTimeMillis()}_${Random.nextInt(10000)}"
    }
    
    /**
     * 清理资源
     */
    fun cleanup() {
        sessions.clear()
    }
}

/**
 * 权限审计日志管理器
 */
class PermissionAuditLogger {
    
    private val auditLogs = mutableListOf<AuditLogEntry>()
    private val maxLogSize = 100000
    
    /**
     * 记录权限检查
     */
    fun logPermissionCheck(
        userId: String,
        resource: String,
        action: String,
        context: Map<String, Any>,
        result: String = "PENDING"
    ) {
        val logEntry = AuditLogEntry(
            id = generateLogId(),
            userId = userId,
            action = "PERMISSION_CHECK",
            resource = resource,
            result = result,
            timestamp = com.unify.core.platform.getCurrentTimeMillis(),
            clientIP = context["clientIP"] as? String,
            userAgent = context["userAgent"] as? String,
            details = mapOf(
                "requestedAction" to action,
                "resource" to resource
            )
        )
        addLogEntry(logEntry)
    }
    
    /**
     * 记录用户创建
     */
    fun logUserCreation(userId: String, createdBy: String) {
        val logEntry = AuditLogEntry(
            id = generateLogId(),
            userId = createdBy,
            action = "USER_CREATED",
            resource = "user:$userId",
            result = "SUCCESS",
            timestamp = com.unify.core.platform.getCurrentTimeMillis(),
            clientIP = null,
            userAgent = null,
            details = mapOf("targetUserId" to userId)
        )
        addLogEntry(logEntry)
    }
    
    /**
     * 记录角色分配
     */
    fun logRoleAssignment(userId: String, roleId: String, assignedBy: String) {
        val logEntry = AuditLogEntry(
            id = generateLogId(),
            userId = assignedBy,
            action = "ROLE_ASSIGNED",
            resource = "user:$userId",
            result = "SUCCESS",
            timestamp = com.unify.core.platform.getCurrentTimeMillis(),
            clientIP = null,
            userAgent = null,
            details = mapOf(
                "targetUserId" to userId,
                "roleId" to roleId
            )
        )
        addLogEntry(logEntry)
    }
    
    /**
     * 记录角色撤销
     */
    fun logRoleRevocation(userId: String, roleId: String, revokedBy: String) {
        val logEntry = AuditLogEntry(
            id = generateLogId(),
            userId = revokedBy,
            action = "ROLE_REVOKED",
            resource = "user:$userId",
            result = "SUCCESS",
            timestamp = com.unify.core.platform.getCurrentTimeMillis(),
            clientIP = null,
            userAgent = null,
            details = mapOf(
                "targetUserId" to userId,
                "roleId" to roleId
            )
        )
        addLogEntry(logEntry)
    }
    
    /**
     * 记录角色创建
     */
    fun logRoleCreation(roleId: String, createdBy: String) {
        val logEntry = AuditLogEntry(
            id = generateLogId(),
            userId = createdBy,
            action = "ROLE_CREATED",
            resource = "role:$roleId",
            result = "SUCCESS",
            timestamp = com.unify.core.platform.getCurrentTimeMillis(),
            clientIP = null,
            userAgent = null,
            details = mapOf("roleId" to roleId)
        )
        addLogEntry(logEntry)
    }
    
    /**
     * 记录权限创建
     */
    fun logPermissionCreation(permissionId: String, createdBy: String) {
        val logEntry = AuditLogEntry(
            id = generateLogId(),
            userId = createdBy,
            action = "PERMISSION_CREATED",
            resource = "permission:$permissionId",
            result = "SUCCESS",
            timestamp = com.unify.core.platform.getCurrentTimeMillis(),
            clientIP = null,
            userAgent = null,
            details = mapOf("permissionId" to permissionId)
        )
        addLogEntry(logEntry)
    }
    
    /**
     * 记录错误
     */
    fun logError(
        message: String,
        userId: String?,
        resource: String?,
        action: String?,
        exception: Exception
    ) {
        val logEntry = AuditLogEntry(
            id = generateLogId(),
            userId = userId,
            action = action ?: "ERROR",
            resource = resource,
            result = "ERROR",
            timestamp = com.unify.core.platform.getCurrentTimeMillis(),
            clientIP = null,
            userAgent = null,
            details = mapOf(
                "errorMessage" to message,
                "exceptionType" to exception::class.simpleName.orEmpty(),
                "exceptionMessage" to (exception.message ?: "")
            )
        )
        addLogEntry(logEntry)
    }
    
    /**
     * 获取日志
     */
    fun getLogs(
        userId: String? = null,
        resource: String? = null,
        startTime: Long? = null,
        endTime: Long? = null,
        limit: Int = 100
    ): List<AuditLogEntry> {
        return auditLogs.filter { log ->
            (userId == null || log.userId == userId) &&
            (resource == null || log.resource == resource) &&
            (startTime == null || log.timestamp >= startTime) &&
            (endTime == null || log.timestamp <= endTime)
        }.takeLast(limit)
    }
    
    /**
     * 清理旧日志
     */
    fun cleanupOldLogs(retentionPeriod: Long) {
        val cutoffTime = com.unify.core.platform.getCurrentTimeMillis() - retentionPeriod
        auditLogs.removeAll { it.timestamp < cutoffTime }
    }
    
    /**
     * 添加日志条目
     */
    private fun addLogEntry(entry: AuditLogEntry) {
        auditLogs.add(entry)
        
        // 检查日志大小限制
        if (auditLogs.size > maxLogSize) {
            auditLogs.removeAt(0) // 移除最旧的日志
        }
    }
    
    /**
     * 生成日志ID
     */
    private fun generateLogId(): String {
        return "log_${com.unify.core.platform.getCurrentTimeMillis()}_${Random.nextInt(10000)}"
    }
    
    /**
     * 清理资源
     */
    fun cleanup() {
        auditLogs.clear()
    }
}

/**
 * 权限策略引擎
 */
class PermissionPolicyEngine {
    
    private val dynamicPolicies = mutableListOf<DynamicPolicy>()
    
    /**
     * 评估策略
     */
    fun evaluate(
        user: User,
        resource: String,
        action: String,
        context: Map<String, Any>,
        permissions: Collection<Permission>
    ): PolicyEvaluation {
        val startTime = com.unify.core.platform.getCurrentTimeMillis()
        val appliedPolicies = mutableListOf<String>()
        
        // 评估静态权限
        val hasStaticPermission = permissions.any { permission ->
            permission.resource == resource && 
            permission.actions.contains(action) &&
            evaluatePermissionConditions(permission, context)
        }
        
        if (hasStaticPermission) {
            appliedPolicies.add("static_permission")
        }
        
        // 评估动态策略
        val hasDynamicPermission = dynamicPolicies.any { policy ->
            val matches = evaluateDynamicPolicyInternal(policy, user, resource, action, context)
            if (matches) {
                appliedPolicies.add(policy.id)
            }
            matches
        }
        
        val granted = hasStaticPermission || hasDynamicPermission
        val evaluationTime = com.unify.core.platform.getCurrentTimeMillis() - startTime
        
        return PolicyEvaluation(
            granted = granted,
            reason = if (granted) "权限检查通过" else "权限不足",
            appliedPolicies = appliedPolicies,
            evaluationTime = evaluationTime
        )
    }
    
    /**
     * 评估动态策略
     */
    fun evaluateDynamicPolicy(
        user: User,
        resource: String,
        action: String,
        context: Map<String, Any>
    ): Boolean {
        return dynamicPolicies.any { policy ->
            evaluateDynamicPolicyInternal(policy, user, resource, action, context)
        }
    }
    
    /**
     * 添加动态策略
     */
    fun addDynamicPolicy(policy: DynamicPolicy) {
        dynamicPolicies.add(policy)
    }
    
    /**
     * 移除动态策略
     */
    fun removeDynamicPolicy(policyId: String) {
        dynamicPolicies.removeAll { it.id == policyId }
    }
    
    /**
     * 评估权限条件
     */
    private fun evaluatePermissionConditions(
        permission: Permission,
        context: Map<String, Any>
    ): Boolean {
        return permission.conditions.all { condition ->
            when (condition.type) {
                ConditionType.TIME_RANGE -> {
                    val currentTime = com.unify.core.platform.getCurrentTimeMillis()
                    val startTime = condition.value["startTime"] as? Long ?: 0L
                    val endTime = condition.value["endTime"] as? Long ?: Long.MAX_VALUE
                    currentTime in startTime..endTime
                }
                ConditionType.IP_RANGE -> {
                    val clientIP = context["clientIP"] as? String ?: return@all false
                    val allowedIPs = condition.value["allowedIPs"] as? List<String> ?: return@all false
                    allowedIPs.contains(clientIP)
                }
                ConditionType.ATTRIBUTE_MATCH -> {
                    val attributeName = condition.value["attributeName"] as? String ?: return@all false
                    val expectedValue = condition.value["expectedValue"] ?: return@all false
                    val actualValue = context[attributeName] ?: return@all false
                    actualValue == expectedValue
                }
                ConditionType.CUSTOM -> {
                    // 自定义条件评估
                    true
                }
            }
        }
    }
    
    /**
     * 评估动态策略内部实现
     */
    private fun evaluateDynamicPolicyInternal(
        policy: DynamicPolicy,
        user: User,
        resource: String,
        action: String,
        context: Map<String, Any>
    ): Boolean {
        // 检查资源匹配
        if (!policy.resourcePattern.toRegex().matches(resource)) {
            return false
        }
        
        // 检查动作匹配
        if (!policy.actions.contains(action)) {
            return false
        }
        
        // 检查用户条件
        return policy.conditions.all { condition ->
            when (condition.type) {
                "user_attribute" -> {
                    val attributeName = condition.parameters["attribute"] as? String ?: return@all false
                    val expectedValue = condition.parameters["value"] ?: return@all false
                    val actualValue = user.metadata[attributeName] ?: return@all false
                    actualValue == expectedValue
                }
                "time_based" -> {
                    val currentTime = com.unify.core.platform.getCurrentTimeMillis()
                    val startTime = condition.parameters["startTime"] as? Long ?: 0L
                    val endTime = condition.parameters["endTime"] as? Long ?: Long.MAX_VALUE
                    currentTime in startTime..endTime
                }
                "context_based" -> {
                    val contextKey = condition.parameters["contextKey"] as? String ?: return@all false
                    val expectedValue = condition.parameters["expectedValue"] ?: return@all false
                    val actualValue = context[contextKey] ?: return@all false
                    actualValue == expectedValue
                }
                else -> true
            }
        }
    }
}

/**
 * 用户会话
 */
@Serializable
data class UserSession(
    val sessionId: String,
    val userId: String,
    val createdAt: Long,
    val lastAccessAt: Long,
    val clientIP: String?,
    val userAgent: String?,
    val isActive: Boolean
) {
    fun isExpired(timeout: Long): Boolean {
        return (com.unify.core.platform.getCurrentTimeMillis() - lastAccessAt) > timeout
    }
}

/**
 * 缓存统计
 */
@Serializable
data class CacheStats(
    val totalEntries: Int,
    val expiredEntries: Int,
    val activeEntries: Int
)

/**
 * 动态策略
 */
@Serializable
data class DynamicPolicy(
    val id: String,
    val name: String,
    val description: String,
    val resourcePattern: String,
    val actions: Set<String>,
    val conditions: List<PolicyCondition>,
    val isActive: Boolean,
    val priority: Int = 0
)

/**
 * 策略条件
 */
@Serializable
data class PolicyCondition(
    val type: String,
    val parameters: Map<String, @Contextual Any>
)
