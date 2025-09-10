package com.unify.core.miniapp

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import org.w3c.dom.Window
import kotlin.js.json

/**
 * Web/JS平台小程序运行时实现
 * 主要支持微信小程序Web版和其他Web小程序
 */
actual class MiniAppRuntime private constructor(private val platform: MiniAppPlatform) {
    actual companion object {
        actual fun create(platform: MiniAppPlatform): MiniAppRuntime {
            return MiniAppRuntime(platform)
        }
    }

    private var config: MiniAppConfig? = null
    private var currentInstance: MiniAppInstance? = null
    private val lifecycleFlow = MutableStateFlow(MiniAppLifecycleState.CREATED)
    private val apiHandlers = mutableMapOf<String, MiniAppApiHandler>()
    private val eventManager = MiniAppEventManager()
    private val stateManager = MiniAppStateManager()

    actual suspend fun initialize(config: MiniAppConfig) {
        this.config = config
        
        // 初始化小程序环境
        when (platform) {
            MiniAppPlatform.WECHAT -> initializeWechatMiniApp(config)
            MiniAppPlatform.ALIPAY -> initializeAlipayMiniApp(config)
            MiniAppPlatform.BYTEDANCE -> initializeBytedanceMiniApp(config)
            else -> initializeGenericMiniApp(config)
        }
        
        // 注册默认API
        registerDefaultApis()
        
        console.log("MiniApp Runtime initialized for platform: ${platform.name}")
    }

    actual suspend fun launch(appId: String, params: Map<String, Any>): MiniAppInstance {
        val instanceId = generateInstanceId()
        
        currentInstance = MiniAppInstance(
            instanceId = instanceId,
            appId = appId,
            platform = platform,
            state = MiniAppLifecycleState.LAUNCHED,
            createdAt = System.currentTimeMillis(),
            params = params
        )
        
        lifecycleFlow.value = MiniAppLifecycleState.LAUNCHED
        
        // 触发启动事件
        eventManager.emitEvent(MiniAppEvent(
            type = "launch",
            data = mapOf(
                "appId" to appId,
                "params" to params
            )
        ))
        
        return currentInstance!!
    }

    actual fun getCurrentInstance(): MiniAppInstance? {
        return currentInstance
    }

    actual suspend fun destroyInstance(instanceId: String) {
        if (currentInstance?.instanceId == instanceId) {
            lifecycleFlow.value = MiniAppLifecycleState.DESTROYED
            
            // 触发销毁事件
            eventManager.emitEvent(MiniAppEvent(
                type = "destroy",
                data = mapOf("instanceId" to instanceId)
            ))
            
            currentInstance = null
            stateManager.clearState()
        }
    }

    actual fun getLifecycleFlow(): Flow<MiniAppLifecycleState> {
        return lifecycleFlow.asStateFlow()
    }

    actual fun registerApi(apiName: String, handler: MiniAppApiHandler) {
        apiHandlers[apiName] = handler
        console.log("Registered API: $apiName")
    }

    actual suspend fun callApi(apiName: String, params: Map<String, Any>): MiniAppApiResult {
        val handler = apiHandlers[apiName]
        return if (handler != null) {
            try {
                handler.handle(params)
            } catch (e: Exception) {
                MiniAppApiResult(
                    success = false,
                    error = e.message ?: "Unknown error",
                    errorCode = -1
                )
            }
        } else {
            MiniAppApiResult(
                success = false,
                error = "API not found: $apiName",
                errorCode = 404
            )
        }
    }

    private fun initializeWechatMiniApp(config: MiniAppConfig) {
        // 微信小程序Web版初始化
        if (js("typeof wx !== 'undefined'")) {
            // 微信小程序环境
            js("""
                wx.miniProgram.postMessage({
                    data: {
                        type: 'init',
                        config: JSON.stringify(config)
                    }
                });
            """)
        } else {
            // Web环境模拟
            console.log("Initializing WeChat MiniApp in web environment")
        }
    }

    private fun initializeAlipayMiniApp(config: MiniAppConfig) {
        // 支付宝小程序初始化
        if (js("typeof my !== 'undefined'")) {
            // 支付宝小程序环境
            js("""
                my.postMessage({
                    type: 'init',
                    data: JSON.stringify(config)
                });
            """)
        } else {
            console.log("Initializing Alipay MiniApp in web environment")
        }
    }

    private fun initializeBytedanceMiniApp(config: MiniAppConfig) {
        // 字节跳动小程序初始化
        if (js("typeof tt !== 'undefined'")) {
            // 字节跳动小程序环境
            js("""
                tt.postMessage({
                    data: {
                        type: 'init',
                        config: JSON.stringify(config)
                    }
                });
            """)
        } else {
            console.log("Initializing ByteDance MiniApp in web environment")
        }
    }

    private fun initializeGenericMiniApp(config: MiniAppConfig) {
        // 通用小程序初始化
        console.log("Initializing generic MiniApp")
    }

    private fun registerDefaultApis() {
        // 注册默认API
        registerApi("getSystemInfo") { params ->
            MiniAppApiResult(
                success = true,
                data = mapOf(
                    "platform" to platform.name,
                    "version" to (config?.version ?: "1.0.0"),
                    "userAgent" to js("navigator.userAgent").toString()
                )
            )
        }

        registerApi("showToast") { params ->
            val title = params["title"] as? String ?: "提示"
            val duration = params["duration"] as? Int ?: 2000
            
            // 显示Toast（Web实现）
            showWebToast(title, duration)
            
            MiniAppApiResult(success = true)
        }

        registerApi("navigateTo") { params ->
            val url = params["url"] as? String
            if (url != null) {
                // 页面导航（Web实现）
                js("window.location.hash = url")
                MiniAppApiResult(success = true)
            } else {
                MiniAppApiResult(
                    success = false,
                    error = "URL is required",
                    errorCode = 400
                )
            }
        }

        registerApi("setStorage") { params ->
            val key = params["key"] as? String
            val data = params["data"]
            
            if (key != null && data != null) {
                js("localStorage.setItem(key, JSON.stringify(data))")
                MiniAppApiResult(success = true)
            } else {
                MiniAppApiResult(
                    success = false,
                    error = "Key and data are required",
                    errorCode = 400
                )
            }
        }

        registerApi("getStorage") { params ->
            val key = params["key"] as? String
            if (key != null) {
                val data = js("localStorage.getItem(key)")
                if (data != null) {
                    MiniAppApiResult(
                        success = true,
                        data = mapOf("data" to JSON.parse(data.toString()))
                    )
                } else {
                    MiniAppApiResult(
                        success = false,
                        error = "Data not found",
                        errorCode = 404
                    )
                }
            } else {
                MiniAppApiResult(
                    success = false,
                    error = "Key is required",
                    errorCode = 400
                )
            }
        }
    }

    private fun showWebToast(title: String, duration: Int) {
        // Web Toast实现
        js("""
            const toast = document.createElement('div');
            toast.textContent = title;
            toast.style.cssText = `
                position: fixed;
                top: 50%;
                left: 50%;
                transform: translate(-50%, -50%);
                background: rgba(0, 0, 0, 0.8);
                color: white;
                padding: 10px 20px;
                border-radius: 4px;
                z-index: 10000;
                font-size: 14px;
            `;
            document.body.appendChild(toast);
            
            setTimeout(() => {
                document.body.removeChild(toast);
            }, duration);
        """)
    }

    private fun generateInstanceId(): String {
        return "miniapp_${platform.name.lowercase()}_${System.currentTimeMillis()}"
    }
}
