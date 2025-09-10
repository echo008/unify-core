package com.unify.core.miniapp

import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json

/**
 * Android平台小程序运行时实现
 * 支持通过WebView运行各种小程序平台
 */
actual class MiniAppRuntime private constructor(
    private val platform: MiniAppPlatform,
    private val context: Context
) {
    actual companion object {
        private lateinit var applicationContext: Context
        
        fun initialize(context: Context) {
            applicationContext = context.applicationContext
        }
        
        actual fun create(platform: MiniAppPlatform): MiniAppRuntime {
            return MiniAppRuntime(platform, applicationContext)
        }
    }

    private var config: MiniAppConfig? = null
    private var currentInstance: MiniAppInstance? = null
    private val lifecycleFlow = MutableStateFlow(MiniAppLifecycleState.CREATED)
    private val apiHandlers = mutableMapOf<String, MiniAppApiHandler>()
    private val eventManager = MiniAppEventManager()
    private val stateManager = MiniAppStateManager()
    private var webView: WebView? = null

    actual suspend fun initialize(config: MiniAppConfig) {
        this.config = config
        
        // 初始化WebView
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
        
        android.util.Log.d("MiniAppRuntime", "Initialized for platform: ${platform.name}")
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
        
        // 在WebView中加载小程序
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
            
            // 清理WebView
            webView?.destroy()
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
        
        // 在WebView中注册JavaScript接口
        webView?.addJavascriptInterface(
            AndroidMiniAppJSInterface(apiName, handler),
            "UnifyMiniApp_$apiName"
        )
        
        android.util.Log.d("MiniAppRuntime", "Registered API: $apiName")
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
        webView = WebView(context).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = true
                allowContentAccess = true
                allowFileAccessFromFileURLs = true
                allowUniversalAccessFromFileURLs = true
            }
            
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    lifecycleFlow.value = MiniAppLifecycleState.SHOWN
                }
            }
        }
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
        
        webView?.loadUrl(fullUrl)
    }

    private fun initializeWechatMiniApp(config: MiniAppConfig) {
        // 微信小程序特定初始化
        android.util.Log.d("MiniAppRuntime", "Initializing WeChat MiniApp")
    }

    private fun initializeAlipayMiniApp(config: MiniAppConfig) {
        // 支付宝小程序特定初始化
        android.util.Log.d("MiniAppRuntime", "Initializing Alipay MiniApp")
    }

    private fun initializeBytedanceMiniApp(config: MiniAppConfig) {
        // 字节跳动小程序特定初始化
        android.util.Log.d("MiniAppRuntime", "Initializing ByteDance MiniApp")
    }

    private fun initializeBaiduMiniApp(config: MiniAppConfig) {
        // 百度智能小程序特定初始化
        android.util.Log.d("MiniAppRuntime", "Initializing Baidu MiniApp")
    }

    private fun initializeKuaishouMiniApp(config: MiniAppConfig) {
        // 快手小程序特定初始化
        android.util.Log.d("MiniAppRuntime", "Initializing Kuaishou MiniApp")
    }

    private fun initializeXiaomiMiniApp(config: MiniAppConfig) {
        // 小米小程序特定初始化
        android.util.Log.d("MiniAppRuntime", "Initializing Xiaomi MiniApp")
    }

    private fun initializeHuaweiMiniApp(config: MiniAppConfig) {
        // 华为快应用特定初始化
        android.util.Log.d("MiniAppRuntime", "Initializing Huawei MiniApp")
    }

    private fun initializeQQMiniApp(config: MiniAppConfig) {
        // QQ小程序特定初始化
        android.util.Log.d("MiniAppRuntime", "Initializing QQ MiniApp")
    }

    private fun registerDefaultApis() {
        // 注册默认API
        registerApi("getSystemInfo") { params ->
            MiniAppApiResult(
                success = true,
                data = mapOf(
                    "platform" to "android",
                    "miniAppPlatform" to platform.name,
                    "version" to (config?.version ?: "1.0.0"),
                    "brand" to android.os.Build.BRAND,
                    "model" to android.os.Build.MODEL,
                    "system" to "Android ${android.os.Build.VERSION.RELEASE}"
                )
            )
        }

        registerApi("showToast") { params ->
            val title = params["title"] as? String ?: "提示"
            val duration = when (params["duration"] as? String) {
                "long" -> Toast.LENGTH_LONG
                else -> Toast.LENGTH_SHORT
            }
            
            Toast.makeText(context, title, duration).show()
            
            MiniAppApiResult(success = true)
        }

        registerApi("setStorage") { params ->
            val key = params["key"] as? String
            val data = params["data"]
            
            if (key != null && data != null) {
                val prefs = context.getSharedPreferences("miniapp_storage", Context.MODE_PRIVATE)
                prefs.edit().putString(key, Json.encodeToString(kotlinx.serialization.json.JsonElement.serializer(), kotlinx.serialization.json.JsonPrimitive(data.toString()))).apply()
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
                val prefs = context.getSharedPreferences("miniapp_storage", Context.MODE_PRIVATE)
                val data = prefs.getString(key, null)
                if (data != null) {
                    MiniAppApiResult(
                        success = true,
                        data = mapOf("data" to data)
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
        return "miniapp_android_${platform.name.lowercase()}_${System.currentTimeMillis()}"
    }

    /**
     * Android WebView JavaScript接口
     */
    private inner class AndroidMiniAppJSInterface(
        private val apiName: String,
        private val handler: MiniAppApiHandler
    ) {
        @android.webkit.JavascriptInterface
        fun call(paramsJson: String): String {
            return try {
                val params = Json.decodeFromString<Map<String, kotlinx.serialization.json.JsonElement>>(paramsJson)
                val result = kotlinx.coroutines.runBlocking {
                    handler.handle(params.mapValues { it.value.toString() })
                }
                Json.encodeToString(MiniAppApiResult.serializer(), result)
            } catch (e: Exception) {
                Json.encodeToString(MiniAppApiResult.serializer(), MiniAppApiResult(
                    success = false,
                    error = e.message ?: "Unknown error",
                    errorCode = -1
                ))
            }
        }
    }
}
