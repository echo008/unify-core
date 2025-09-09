package com.unify.core.dynamic

import com.unify.core.platform.getCurrentTimeMillis
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlin.collections.mutableMapOf

/**
 * 动态配置数据结构
 */
@Serializable
data class DynamicConfiguration(
    val id: String,
    val name: String,
    val version: String,
    val category: ConfigCategory,
    val priority: ConfigPriority,
    val scope: ConfigScope,
    val values: Map<String, ConfigValue>,
    val metadata: Map<String, String> = emptyMap(),
    val dependencies: List<String> = emptyList(),
    val validationRules: List<ValidationRule> = emptyList(),
    val lastModified: Long = getCurrentTimeMillis(),
    val checksum: String = "",
)

@Serializable
enum class ConfigCategory {
    UI_THEME, // UI主题配置
    PERFORMANCE, // 性能配置
    SECURITY, // 安全配置
    NETWORK, // 网络配置
    STORAGE, // 存储配置
    FEATURE_FLAGS, // 功能开关
    BUSINESS_RULES, // 业务规则
    PLATFORM_SPECIFIC, // 平台特定配置
    EXPERIMENTAL, // 实验性配置
}

@Serializable
enum class ConfigPriority {
    LOW, // 低优先级
    NORMAL, // 普通优先级
    HIGH, // 高优先级
    CRITICAL, // 关键优先级
}

@Serializable
enum class ConfigScope {
    GLOBAL, // 全局配置
    USER, // 用户配置
    SESSION, // 会话配置
    COMPONENT, // 组件配置
    PLATFORM, // 平台配置
}

@Serializable
sealed class ConfigValue {
    @Serializable
    data class StringValue(val value: String) : ConfigValue()

    @Serializable
    data class IntValue(val value: Int) : ConfigValue()

    @Serializable
    data class LongValue(val value: Long) : ConfigValue()

    @Serializable
    data class DoubleValue(val value: Double) : ConfigValue()

    @Serializable
    data class BooleanValue(val value: Boolean) : ConfigValue()

    @Serializable
    data class ListValue(val value: List<String>) : ConfigValue()

    @Serializable
    data class MapValue(val value: Map<String, String>) : ConfigValue()
}

@Serializable
data class ValidationRule(
    val field: String,
    val type: ValidationType,
    val constraint: String,
    val errorMessage: String,
)

@Serializable
enum class ValidationType {
    REQUIRED, // 必填
    MIN_LENGTH, // 最小长度
    MAX_LENGTH, // 最大长度
    RANGE, // 数值范围
    REGEX, // 正则表达式
    ENUM, // 枚举值
    CUSTOM, // 自定义验证
}

@Serializable
data class ConfigValidationResult(
    val isValid: Boolean,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList(),
)

@Serializable
data class ConfigChangeEvent(
    val configId: String,
    val changeType: ConfigChangeType,
    val oldValue: Map<String, ConfigValue>? = null,
    val newValue: Map<String, ConfigValue>? = null,
    val timestamp: Long = getCurrentTimeMillis(),
    val source: String = "",
)

@Serializable
enum class ConfigChangeType {
    CREATED, // 创建
    UPDATED, // 更新
    DELETED, // 删除
    MERGED, // 合并
    RESTORED, // 恢复
}

/**
 * 动态配置管理器接口
 */
interface DynamicConfigurationManager {
    // 配置管理
    suspend fun saveConfiguration(config: DynamicConfiguration): Boolean

    suspend fun loadConfiguration(configId: String): DynamicConfiguration?

    suspend fun deleteConfiguration(configId: String): Boolean

    suspend fun getAllConfigurations(): List<DynamicConfiguration>

    // 配置查询
    suspend fun getConfigurationsByCategory(category: ConfigCategory): List<DynamicConfiguration>

    suspend fun getConfigurationsByScope(scope: ConfigScope): List<DynamicConfiguration>

    suspend fun searchConfigurations(query: String): List<DynamicConfiguration>

    // 配置值操作
    suspend fun getConfigValue(
        configId: String,
        key: String,
    ): ConfigValue?

    suspend fun setConfigValue(
        configId: String,
        key: String,
        value: ConfigValue,
    ): Boolean

    suspend fun removeConfigValue(
        configId: String,
        key: String,
    ): Boolean

    // 批量操作
    suspend fun batchUpdateConfigurations(updates: Map<String, Map<String, ConfigValue>>): Boolean

    suspend fun mergeConfigurations(
        baseConfigId: String,
        overrideConfigId: String,
    ): DynamicConfiguration?

    // 配置验证
    suspend fun validateConfiguration(config: DynamicConfiguration): ConfigValidationResult

    suspend fun validateConfigValue(
        configId: String,
        key: String,
        value: ConfigValue,
    ): Boolean

    // 配置监听
    fun observeConfiguration(configId: String): Flow<DynamicConfiguration?>

    fun observeConfigurationChanges(): Flow<ConfigChangeEvent>

    fun observeConfigValue(
        configId: String,
        key: String,
    ): Flow<ConfigValue?>

    // 配置备份和恢复
    suspend fun backupConfiguration(configId: String): String?

    suspend fun restoreConfiguration(
        configId: String,
        backupData: String,
    ): Boolean

    suspend fun exportConfigurations(): String

    suspend fun importConfigurations(data: String): Boolean

    // 配置缓存
    fun enableCache(maxSize: Int = 100)

    fun disableCache()

    fun clearCache()

    // 生命周期
    suspend fun initialize()

    suspend fun shutdown()
}

/**
 * 动态配置管理器实现
 */
class DynamicConfigurationManagerImpl(
    private val storageManager: DynamicStorageManager,
) : DynamicConfigurationManager {
    private val configurations = mutableMapOf<String, DynamicConfiguration>()
    private val configChangeFlow = MutableSharedFlow<ConfigChangeEvent>()
    private val configObservers = mutableMapOf<String, MutableSharedFlow<DynamicConfiguration?>>()
    private val valueObservers = mutableMapOf<String, MutableSharedFlow<ConfigValue?>>()
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var cacheEnabled = false
    private var maxCacheSize = 100

    companion object {
        private const val CONFIG_STORAGE_PREFIX = "dynamic_config_"
        private const val BACKUP_STORAGE_PREFIX = "config_backup_"
        private const val EXPORT_VERSION = "1.0"
    }

    override suspend fun initialize() {
        // 加载所有配置
        loadAllConfigurations()
    }

    override suspend fun shutdown() {
        scope.cancel()
        configurations.clear()
        configObservers.clear()
        valueObservers.clear()
    }

    override suspend fun saveConfiguration(config: DynamicConfiguration): Boolean {
        return try {
            // 验证配置
            val validationResult = validateConfiguration(config)
            if (!validationResult.isValid) {
                return false
            }

            val oldConfig = configurations[config.id]

            // 保存到存储
            val configJson = Json.encodeToString(config)
            val success = storageManager.saveConfig("${CONFIG_STORAGE_PREFIX}${config.id}", configJson)

            if (success) {
                // 更新内存缓存
                if (cacheEnabled) {
                    manageCacheSize()
                    configurations[config.id] = config
                }

                // 通知变更
                val changeType = if (oldConfig == null) ConfigChangeType.CREATED else ConfigChangeType.UPDATED
                val changeEvent =
                    ConfigChangeEvent(
                        configId = config.id,
                        changeType = changeType,
                        oldValue = oldConfig?.values,
                        newValue = config.values,
                        source = "DynamicConfigurationManager",
                    )

                configChangeFlow.emit(changeEvent)

                // 通知特定配置观察者
                configObservers[config.id]?.emit(config)

                // 通知值观察者
                config.values.forEach { (key, value) ->
                    val observerKey = "${config.id}:$key"
                    valueObservers[observerKey]?.emit(value)
                }
            }

            success
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun loadConfiguration(configId: String): DynamicConfiguration? {
        // 先检查缓存
        if (cacheEnabled && configurations.containsKey(configId)) {
            return configurations[configId]
        }

        return try {
            val configJson = storageManager.getConfig("${CONFIG_STORAGE_PREFIX}$configId")
            if (configJson != null) {
                val config = Json.decodeFromString<DynamicConfiguration>(configJson)

                // 更新缓存
                if (cacheEnabled) {
                    manageCacheSize()
                    configurations[configId] = config
                }

                config
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun deleteConfiguration(configId: String): Boolean {
        return try {
            val oldConfig = configurations[configId]

            // 从存储删除
            val success = storageManager.removeConfig("${CONFIG_STORAGE_PREFIX}$configId")

            if (success) {
                // 从缓存删除
                configurations.remove(configId)

                // 通知变更
                if (oldConfig != null) {
                    val changeEvent =
                        ConfigChangeEvent(
                            configId = configId,
                            changeType = ConfigChangeType.DELETED,
                            oldValue = oldConfig.values,
                            source = "DynamicConfigurationManager",
                        )
                    configChangeFlow.emit(changeEvent)
                }

                // 通知观察者
                configObservers[configId]?.emit(null)

                // 清理观察者
                configObservers.remove(configId)
                valueObservers.keys.removeAll { it.startsWith("$configId:") }
            }

            success
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getAllConfigurations(): List<DynamicConfiguration> {
        return if (cacheEnabled && configurations.isNotEmpty()) {
            configurations.values.toList()
        } else {
            loadAllConfigurations()
            configurations.values.toList()
        }
    }

    override suspend fun getConfigurationsByCategory(category: ConfigCategory): List<DynamicConfiguration> {
        val allConfigs = getAllConfigurations()
        return allConfigs.filter { it.category == category }
    }

    override suspend fun getConfigurationsByScope(scope: ConfigScope): List<DynamicConfiguration> {
        val allConfigs = getAllConfigurations()
        return allConfigs.filter { it.scope == scope }
    }

    override suspend fun searchConfigurations(query: String): List<DynamicConfiguration> {
        val allConfigs = getAllConfigurations()
        return allConfigs.filter { config ->
            config.name.contains(query, ignoreCase = true) ||
                config.id.contains(query, ignoreCase = true) ||
                config.values.keys.any { it.contains(query, ignoreCase = true) }
        }
    }

    override suspend fun getConfigValue(
        configId: String,
        key: String,
    ): ConfigValue? {
        val config = loadConfiguration(configId)
        return config?.values?.get(key)
    }

    override suspend fun setConfigValue(
        configId: String,
        key: String,
        value: ConfigValue,
    ): Boolean {
        val config = loadConfiguration(configId) ?: return false

        val updatedValues = config.values.toMutableMap()
        updatedValues[key] = value

        val updatedConfig =
            config.copy(
                values = updatedValues,
                lastModified = getCurrentTimeMillis(),
            )

        return saveConfiguration(updatedConfig)
    }

    override suspend fun removeConfigValue(
        configId: String,
        key: String,
    ): Boolean {
        val config = loadConfiguration(configId) ?: return false

        val updatedValues = config.values.toMutableMap()
        updatedValues.remove(key)

        val updatedConfig =
            config.copy(
                values = updatedValues,
                lastModified = getCurrentTimeMillis(),
            )

        return saveConfiguration(updatedConfig)
    }

    override suspend fun batchUpdateConfigurations(updates: Map<String, Map<String, ConfigValue>>): Boolean {
        return try {
            var allSuccess = true

            updates.forEach { (configId, valueUpdates) ->
                val config = loadConfiguration(configId)
                if (config != null) {
                    val updatedValues = config.values.toMutableMap()
                    updatedValues.putAll(valueUpdates)

                    val updatedConfig =
                        config.copy(
                            values = updatedValues,
                            lastModified = getCurrentTimeMillis(),
                        )

                    if (!saveConfiguration(updatedConfig)) {
                        allSuccess = false
                    }
                } else {
                    allSuccess = false
                }
            }

            allSuccess
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun mergeConfigurations(
        baseConfigId: String,
        overrideConfigId: String,
    ): DynamicConfiguration? {
        val baseConfig = loadConfiguration(baseConfigId) ?: return null
        val overrideConfig = loadConfiguration(overrideConfigId) ?: return null

        val mergedValues = baseConfig.values.toMutableMap()
        mergedValues.putAll(overrideConfig.values)

        return baseConfig.copy(
            id = "${baseConfigId}_merged_$overrideConfigId",
            name = "${baseConfig.name} (合并 ${overrideConfig.name})",
            values = mergedValues,
            lastModified = getCurrentTimeMillis(),
        )
    }

    override suspend fun validateConfiguration(config: DynamicConfiguration): ConfigValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        // 基本验证
        if (config.id.isBlank()) {
            errors.add("配置ID不能为空")
        }

        if (config.name.isBlank()) {
            errors.add("配置名称不能为空")
        }

        // 验证规则检查
        config.validationRules.forEach { rule ->
            val value = config.values[rule.field]
            val validationError = validateRule(rule, value)
            if (validationError != null) {
                errors.add(validationError)
            }
        }

        // 依赖检查
        config.dependencies.forEach { depId ->
            if (loadConfiguration(depId) == null) {
                warnings.add("依赖配置 $depId 不存在")
            }
        }

        return ConfigValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings,
        )
    }

    override suspend fun validateConfigValue(
        configId: String,
        key: String,
        value: ConfigValue,
    ): Boolean {
        val config = loadConfiguration(configId) ?: return false

        val rule = config.validationRules.find { it.field == key }
        return if (rule != null) {
            validateRule(rule, value) == null
        } else {
            true
        }
    }

    override fun observeConfiguration(configId: String): Flow<DynamicConfiguration?> {
        return configObservers.getOrPut(configId) {
            MutableSharedFlow(replay = 1)
        }.asSharedFlow()
    }

    override fun observeConfigurationChanges(): Flow<ConfigChangeEvent> {
        return configChangeFlow.asSharedFlow()
    }

    override fun observeConfigValue(
        configId: String,
        key: String,
    ): Flow<ConfigValue?> {
        val observerKey = "$configId:$key"
        return valueObservers.getOrPut(observerKey) {
            MutableSharedFlow(replay = 1)
        }.asSharedFlow()
    }

    override suspend fun backupConfiguration(configId: String): String? {
        val config = loadConfiguration(configId) ?: return null

        return try {
            val backupData = Json.encodeToString(config)
            val backupKey = "${BACKUP_STORAGE_PREFIX}${configId}_${getCurrentTimeMillis()}"

            if (storageManager.saveConfig(backupKey, backupData)) {
                backupKey
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun restoreConfiguration(
        configId: String,
        backupData: String,
    ): Boolean {
        return try {
            val config = Json.decodeFromString<DynamicConfiguration>(backupData)
            saveConfiguration(config.copy(id = configId))
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun exportConfigurations(): String {
        val allConfigs = getAllConfigurations()
        val exportData =
            mapOf(
                "version" to EXPORT_VERSION,
                "timestamp" to getCurrentTimeMillis(),
                "configurations" to allConfigs,
            )

        return Json.encodeToString(exportData)
    }

    override suspend fun importConfigurations(data: String): Boolean {
        return try {
            val importData = Json.parseToJsonElement(data).jsonObject
            val configurations =
                Json.decodeFromJsonElement<List<DynamicConfiguration>>(
                    importData["configurations"] ?: return false,
                )

            var allSuccess = true
            configurations.forEach { config ->
                if (!saveConfiguration(config)) {
                    allSuccess = false
                }
            }

            allSuccess
        } catch (e: Exception) {
            false
        }
    }

    override fun enableCache(maxSize: Int) {
        cacheEnabled = true
        maxCacheSize = maxSize
    }

    override fun disableCache() {
        cacheEnabled = false
        configurations.clear()
    }

    override fun clearCache() {
        configurations.clear()
    }

    // 私有辅助方法
    private suspend fun loadAllConfigurations() {
        // 这里需要实现从存储加载所有配置的逻辑
        // 由于存储接口限制，这里使用简化实现
    }

    private fun manageCacheSize() {
        if (configurations.size >= maxCacheSize) {
            // 移除最旧的配置（简化实现）
            val oldestKey = configurations.keys.first()
            configurations.remove(oldestKey)
        }
    }

    private fun validateRule(
        rule: ValidationRule,
        value: ConfigValue?,
    ): String? {
        return when (rule.type) {
            ValidationType.REQUIRED -> {
                if (value == null) rule.errorMessage else null
            }
            ValidationType.MIN_LENGTH -> {
                val minLength = rule.constraint.toIntOrNull() ?: 0
                when (value) {
                    is ConfigValue.StringValue -> {
                        if (value.value.length < minLength) rule.errorMessage else null
                    }
                    else -> null
                }
            }
            ValidationType.MAX_LENGTH -> {
                val maxLength = rule.constraint.toIntOrNull() ?: Int.MAX_VALUE
                when (value) {
                    is ConfigValue.StringValue -> {
                        if (value.value.length > maxLength) rule.errorMessage else null
                    }
                    else -> null
                }
            }
            ValidationType.RANGE -> {
                val range = rule.constraint.split(",")
                if (range.size == 2) {
                    val min = range[0].toDoubleOrNull() ?: Double.MIN_VALUE
                    val max = range[1].toDoubleOrNull() ?: Double.MAX_VALUE

                    when (value) {
                        is ConfigValue.IntValue -> {
                            if (value.value < min || value.value > max) rule.errorMessage else null
                        }
                        is ConfigValue.LongValue -> {
                            if (value.value < min || value.value > max) rule.errorMessage else null
                        }
                        is ConfigValue.DoubleValue -> {
                            if (value.value < min || value.value > max) rule.errorMessage else null
                        }
                        else -> null
                    }
                } else {
                    null
                }
            }
            ValidationType.REGEX -> {
                when (value) {
                    is ConfigValue.StringValue -> {
                        try {
                            val regex = Regex(rule.constraint)
                            if (!regex.matches(value.value)) rule.errorMessage else null
                        } catch (e: Exception) {
                            "正则表达式无效"
                        }
                    }
                    else -> null
                }
            }
            ValidationType.ENUM -> {
                val allowedValues = rule.constraint.split(",")
                when (value) {
                    is ConfigValue.StringValue -> {
                        if (!allowedValues.contains(value.value)) rule.errorMessage else null
                    }
                    else -> null
                }
            }
            ValidationType.CUSTOM -> {
                // 自定义验证逻辑
                null
            }
        }
    }
}
