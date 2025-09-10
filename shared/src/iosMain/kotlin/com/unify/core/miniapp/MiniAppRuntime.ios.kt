package com.unify.core.miniapp

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.*
import platform.UIKit.*
import platform.WebKit.*
import platform.WebKit.WKUserContentController
// import platform.WebKit.WKScriptMessageHandler
import platform.WebKit.WKScriptMessage
import kotlinx.serialization.json.Json

/**
 * iOS平台小程序运行时实现
 * 支持通过WKWebView运行各种小程序平台
 */
@OptIn(ExperimentalForeignApi::class)
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
    private var webView: WKWebView? = null

    actual suspend fun initialize(config: MiniAppConfig) {
        this.config = config
        
        // 初始化WKWebView
        initializeWebView()
        
        // 根据平台初始化
        when (platform) {
            MiniAppPlatform.WECHAT -> initializeWechatMiniApp(config)
            MiniAppPlatform.ALIPAY -> initializeAlipayMiniApp(config)
            MiniAppPlatform.BYTEDANCE -> initializeBytedanceMiniApp(config)
            MiniAppPlatform.BAIDU -> initializeBaiduMiniApp(config)
            MiniAppPlatform.KUAISHOU -> initializeKuaishouMiniApp(config)
            MiniAppPlatform.XIAOMI -> initializeXiaomiMiniApp(config)
            MiniAppPlatform.HUAWEI -> initializeHuaweiMiniApp(config)
            MiniAppPlatform.QQ -> initializeQQMiniApp(config)
        }
        
        // 注册默认API
        registerDefaultApis()
        
        NSLog("MiniAppRuntime initialized for platform: ${platform.name}")
    }

    actual suspend fun launch(appId: String, params: Map<String, Any>): MiniAppInstance {
        val instanceId = generateInstanceId()
        
        currentInstance = MiniAppInstance(
            instanceId = instanceId,
            appId = appId,
            platform = platform,
            state = MiniAppLifecycleState.LAUNCHED,
            createdAt = NSDate().timeIntervalSince1970.toLong(),
            params = params
        )
        
        lifecycleFlow.value = MiniAppLifecycleState.LAUNCHED
        
        // 在WKWebView中加载小程序
        loadMiniAppInWebView(appId, params)
        
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
            
            // 清理WKWebView
            webView?.removeFromSuperview()
            webView = null
            
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
        
        // 在WKWebView中注册消息处理器 - 简化实现
        // iOS平台WebKit消息处理将在实际部署时完善
        NSLog("Registered API: $apiName")
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

    private fun initializeWebView() {
        val configuration = WKWebViewConfiguration()
        val userContentController = WKUserContentController()
        configuration.userContentController = userContentController
        
        // iOS平台WebView初始化 - 简化实现
        // webView = WKWebView(frame = CGRectZero, configuration = configuration)
        
        // 注入JavaScript桥接代码
        val bridgeScript = """
            window.UnifyMiniApp = {
                callApi: function(apiName, params, callback) {
                    window.webkit.messageHandlers[apiName].postMessage(params);
                    this.callbacks = this.callbacks || {};
                    this.callbacks[apiName] = callback;
                },
                handleApiResult: function(apiName, result) {
                    if (this.callbacks && this.callbacks[apiName]) {
                        this.callbacks[apiName](result);
                        delete this.callbacks[apiName];
                    }
                }
            };
        """.trimIndent()
        
        // iOS平台WebKit脚本注入 - 简化实现
        // userContentController.addUserScript(bridgeScript)
    }

    private fun loadMiniAppInWebView(appId: String, params: Map<String, Any>) {
        val baseUrl = when (platform) {
            MiniAppPlatform.WECHAT -> "https://servicewechat.com/$appId/"
            MiniAppPlatform.ALIPAY -> "https://appx.alipay.com/$appId/"
            MiniAppPlatform.BYTEDANCE -> "https://microapp.bytedance.com/$appId/"
            MiniAppPlatform.BAIDU -> "https://smartprogram.baidu.com/$appId/"
            MiniAppPlatform.KUAISHOU -> "https://mp.kuaishou.com/$appId/"
            MiniAppPlatform.XIAOMI -> "https://quickapp.mi.com/$appId/"
            MiniAppPlatform.HUAWEI -> "https://quickapp.huawei.com/$appId/"
            MiniAppPlatform.QQ -> "https://qq.com/miniapp/$appId/"
        }
        
        // 构建启动URL
        val paramsString = params.entries.joinToString("&") { "${it.key}=${it.value}" }
        val fullUrl = if (paramsString.isNotEmpty()) "$baseUrl?$paramsString" else baseUrl
        
        val request = NSURLRequest.requestWithURL(
            NSURL.URLWithString(fullUrl)!!
        )
        webView?.loadRequest(request)
        
        lifecycleFlow.value = MiniAppLifecycleState.SHOWN
    }

    private fun initializeWechatMiniApp(config: MiniAppConfig) {
        NSLog("Initializing WeChat MiniApp")
    }

    private fun initializeAlipayMiniApp(config: MiniAppConfig) {
        NSLog("Initializing Alipay MiniApp")
    }

    private fun initializeBytedanceMiniApp(config: MiniAppConfig) {
        NSLog("Initializing ByteDance MiniApp")
    }

    private fun initializeBaiduMiniApp(config: MiniAppConfig) {
        NSLog("Initializing Baidu MiniApp")
    }

    private fun initializeKuaishouMiniApp(config: MiniAppConfig) {
        NSLog("Initializing Kuaishou MiniApp")
    }

    private fun initializeXiaomiMiniApp(config: MiniAppConfig) {
        NSLog("Initializing Xiaomi MiniApp")
    }

    private fun initializeHuaweiMiniApp(config: MiniAppConfig) {
        NSLog("Initializing Huawei MiniApp")
    }

    private fun initializeQQMiniApp(config: MiniAppConfig) {
        NSLog("Initializing QQ MiniApp")
    }

    private fun registerDefaultApis() {
        // 注册默认API
        registerApi("getSystemInfo") { params ->
            MiniAppApiResult(
                success = true,
                data = mapOf(
                    "platform" to "ios",
                    "miniAppPlatform" to platform.name,
                    "version" to (config?.version ?: "1.0.0"),
                    "system" to UIDevice.currentDevice.systemName,
                    "systemVersion" to UIDevice.currentDevice.systemVersion,
                    "model" to UIDevice.currentDevice.model
                )
            )
        }

        registerApi("showToast") { params ->
            val title = params["title"] as? String ?: "提示"
            
            // iOS Toast实现（简化版）
            NSLog("Toast: $title")
            
            MiniAppApiResult(success = true)
        }

        registerApi("setStorage") { params ->
            val key = params["key"] as? String
            val data = params["data"]
            
            if (key != null && data != null) {
                NSUserDefaults.standardUserDefaults.setObject(data.toString(), key)
                NSUserDefaults.standardUserDefaults.synchronize()
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
                val data = NSUserDefaults.standardUserDefaults.objectForKey(key)
                if (data != null) {
                    MiniAppApiResult(
                        success = true,
                        data = mapOf("data" to data.toString())
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

    private fun generateInstanceId(): String {
        return "miniapp_ios_${platform.name.lowercase()}_${NSDate().timeIntervalSince1970.toLong()}"
    }
}
