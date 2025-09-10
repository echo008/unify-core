package com.unify.core.miniapp

import kotlinx.coroutines.*
import kotlinx.serialization.Contextual
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable

/**
 * 小程序运行时核心接口
 * 支持微信、支付宝、字节跳动、百度、快手、小米、华为、QQ等小程序平台
 */
expect class MiniAppRuntime {
    companion object {
        fun create(platform: MiniAppPlatform): MiniAppRuntime
    }

    /**
     * 初始化小程序运行时
     */
    suspend fun initialize(config: MiniAppConfig)

    /**
     * 启动小程序
     */
    suspend fun launch(appId: String, params: Map<String, Any> = emptyMap()): MiniAppInstance

    /**
     * 获取当前运行的小程序实例
     */
    fun getCurrentInstance(): MiniAppInstance?

    /**
     * 销毁小程序实例
     */
    suspend fun destroyInstance(instanceId: String)

    /**
     * 获取小程序生命周期状态流
     */
    fun getLifecycleFlow(): Flow<MiniAppLifecycleState>

    /**
     * 注册小程序API
     */
    fun registerApi(apiName: String, handler: MiniAppApiHandler)

    /**
     * 调用小程序API
     */
    suspend fun callApi(apiName: String, params: Map<String, Any>): MiniAppApiResult
}

/**
 * 小程序平台枚举
 */
enum class MiniAppPlatform {
    WECHAT,      // 微信小程序
    ALIPAY,      // 支付宝小程序
    BYTEDANCE,   // 字节跳动小程序
    BAIDU,       // 百度智能小程序
    KUAISHOU,    // 快手小程序
    XIAOMI,      // 小米小程序
    HUAWEI,      // 华为快应用
    QQ           // QQ小程序
}

/**
 * 小程序配置
 */
@Serializable
data class MiniAppConfig(
    val platform: MiniAppPlatform,
    val appId: String,
    val version: String,
    val enableDebug: Boolean = false,
    val enableCompose: Boolean = true,
    val composeConfig: ComposeConfig? = null,
    val apiConfig: ApiConfig = ApiConfig(),
    val storageConfig: StorageConfig = StorageConfig()
)

/**
 * Compose配置
 */
@Serializable
data class ComposeConfig(
    val enableHotReload: Boolean = false,
    val enablePreview: Boolean = false,
    val theme: String = "default"
)

/**
 * API配置
 */
@Serializable
data class ApiConfig(
    val baseUrl: String = "",
    val timeout: Long = 30000L,
    val enableCache: Boolean = true,
    val enableMock: Boolean = false
)

/**
 * 存储配置
 */
@Serializable
data class StorageConfig(
    val enableLocalStorage: Boolean = true,
    val enableCloudStorage: Boolean = false,
    val maxStorageSize: Long = 10 * 1024 * 1024L // 10MB
)

/**
 * 小程序实例
 */
data class MiniAppInstance(
    val instanceId: String,
    val appId: String,
    val platform: MiniAppPlatform,
    val state: MiniAppLifecycleState,
    val createdAt: Long,
    val params: Map<String, Any>
)

/**
 * 小程序生命周期状态
 */
enum class MiniAppLifecycleState {
    CREATED,     // 已创建
    LAUNCHED,    // 已启动
    SHOWN,       // 已显示
    HIDDEN,      // 已隐藏
    DESTROYED    // 已销毁
}

/**
 * 小程序API处理器
 */
fun interface MiniAppApiHandler {
    suspend fun handle(params: Map<String, Any>): MiniAppApiResult
}

/**
 * 小程序API调用结果
 */
@Serializable
data class MiniAppApiResult(
    val success: Boolean,
    val data: Map<String, @Contextual Any>? = null,
    val error: String? = null,
    val errorCode: Int? = null
)

/**
 * 小程序Compose桥接器
 * 将Compose UI组件转换为小程序原生组件
 */
abstract class MiniAppComposeBridge {
    /**
     * 渲染Compose组件到小程序视图
     */
    abstract suspend fun renderCompose(
        composableId: String,
        props: Map<String, Any>
    ): MiniAppViewResult

    /**
     * 更新Compose组件属性
     */
    abstract suspend fun updateProps(
        viewId: String,
        props: Map<String, Any>
    ): Boolean

    /**
     * 销毁Compose视图
     */
    abstract suspend fun destroyView(viewId: String): Boolean
}

/**
 * 小程序视图结果
 */
@Serializable
data class MiniAppViewResult(
    val viewId: String,
    val viewType: String,
    val properties: Map<String, @Contextual Any>,
    val children: List<MiniAppViewResult> = emptyList()
)

/**
 * 小程序事件管理器
 */
class MiniAppEventManager {
    private val eventListeners = mutableMapOf<String, MutableList<MiniAppEventListener>>()

    /**
     * 注册事件监听器
     */
    fun addEventListener(eventType: String, listener: MiniAppEventListener) {
        eventListeners.getOrPut(eventType) { mutableListOf() }.add(listener)
    }

    /**
     * 移除事件监听器
     */
    fun removeEventListener(eventType: String, listener: MiniAppEventListener) {
        eventListeners[eventType]?.remove(listener)
    }

    /**
     * 触发事件
     */
    suspend fun emitEvent(event: MiniAppEvent) {
        eventListeners[event.type]?.forEach { listener ->
            listener.onEvent(event)
        }
    }
}

/**
 * 小程序事件监听器
 */
fun interface MiniAppEventListener {
    suspend fun onEvent(event: MiniAppEvent)
}

/**
 * 小程序事件
 */
@Serializable
data class MiniAppEvent(
    val type: String,
    val data: Map<String, @Contextual Any>,
    val timestamp: Long = com.unify.core.platform.getCurrentTimeMillis(),
    val source: String? = null
)

/**
 * 小程序状态管理器
 */
class MiniAppStateManager {
    private val _state = MutableStateFlow<Map<String, @Contextual Any>>(emptyMap())
    val state: StateFlow<Map<String, @Contextual Any>> = _state.asStateFlow()

    /**
     * 更新状态
     */
    fun updateState(key: String, value: Any) {
        _state.value = _state.value.toMutableMap().apply {
            put(key, value)
        }
    }

    /**
     * 获取状态值
     */
    fun getState(key: String): Any? {
        return _state.value[key]
    }

    /**
     * 清空状态
     */
    fun clearState() {
        _state.value = emptyMap()
    }
}
