package com.unify.core.miniapp

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import java.awt.Desktop
import java.io.File
import java.net.URI
import java.util.prefs.Preferences

/**
 * Desktop平台小程序运行时实现
 * 支持通过系统浏览器或内嵌WebView运行各种小程序平台
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
    private val preferences = Preferences.userNodeForPackage(MiniAppRuntime::class.java)

    actual suspend fun initialize(config: MiniAppConfig) {
        this.config = config
        
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
        
        println("MiniAppRuntime initialized for platform: ${platform.name}")
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
        
        // 在系统浏览器中打开小程序
        openMiniAppInBrowser(appId, params)
        
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
        println("Registered API: $apiName")
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

    private fun openMiniAppInBrowser(appId: String, params: Map<String, Any>) {
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
        
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI(fullUrl))
                lifecycleFlow.value = MiniAppLifecycleState.SHOWN
            } else {
                println("Desktop browsing not supported. URL: $fullUrl")
            }
        } catch (e: Exception) {
            println("Failed to open browser: ${e.message}")
        }
    }

    private fun initializeWechatMiniApp(config: MiniAppConfig) {
        println("Initializing WeChat MiniApp on Desktop")
    }

    private fun initializeAlipayMiniApp(config: MiniAppConfig) {
        println("Initializing Alipay MiniApp on Desktop")
    }

    private fun initializeBytedanceMiniApp(config: MiniAppConfig) {
        println("Initializing ByteDance MiniApp on Desktop")
    }

    private fun initializeBaiduMiniApp(config: MiniAppConfig) {
        println("Initializing Baidu MiniApp on Desktop")
    }

    private fun initializeKuaishouMiniApp(config: MiniAppConfig) {
        println("Initializing Kuaishou MiniApp on Desktop")
    }

    private fun initializeXiaomiMiniApp(config: MiniAppConfig) {
        println("Initializing Xiaomi MiniApp on Desktop")
    }

    private fun initializeHuaweiMiniApp(config: MiniAppConfig) {
        println("Initializing Huawei MiniApp on Desktop")
    }

    private fun initializeQQMiniApp(config: MiniAppConfig) {
        println("Initializing QQ MiniApp on Desktop")
    }

    private fun registerDefaultApis() {
        // 注册默认API
        registerApi("getSystemInfo") { params ->
            MiniAppApiResult(
                success = true,
                data = mapOf(
                    "platform" to "desktop",
                    "miniAppPlatform" to platform.name,
                    "version" to (config?.version ?: "1.0.0"),
                    "os" to System.getProperty("os.name"),
                    "osVersion" to System.getProperty("os.version"),
                    "arch" to System.getProperty("os.arch"),
                    "javaVersion" to System.getProperty("java.version")
                )
            )
        }

        registerApi("showToast") { params ->
            val title = params["title"] as? String ?: "提示"
            
            // Desktop Toast实现（控制台输出）
            println("Toast: $title")
            
            MiniAppApiResult(success = true)
        }

        registerApi("showDialog") { params ->
            val title = params["title"] as? String ?: "提示"
            val content = params["content"] as? String ?: ""
            
            // Desktop Dialog实现
            println("Dialog - Title: $title, Content: $content")
            
            MiniAppApiResult(
                success = true,
                data = mapOf("confirm" to true)
            )
        }

        registerApi("setStorage") { params ->
            val key = params["key"] as? String
            val data = params["data"]
            
            if (key != null && data != null) {
                preferences.put("miniapp_$key", data.toString())
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
                val data = preferences.get("miniapp_$key", null)
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

        registerApi("removeStorage") { params ->
            val key = params["key"] as? String
            if (key != null) {
                preferences.remove("miniapp_$key")
                MiniAppApiResult(success = true)
            } else {
                MiniAppApiResult(
                    success = false,
                    error = "Key is required",
                    errorCode = 400
                )
            }
        }

        registerApi("openFile") { params ->
            val filePath = params["filePath"] as? String
            if (filePath != null) {
                try {
                    val file = File(filePath)
                    if (file.exists() && Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(file)
                        MiniAppApiResult(success = true)
                    } else {
                        MiniAppApiResult(
                            success = false,
                            error = "File not found or desktop not supported",
                            errorCode = 404
                        )
                    }
                } catch (e: Exception) {
                    MiniAppApiResult(
                        success = false,
                        error = e.message ?: "Failed to open file",
                        errorCode = 500
                    )
                }
            } else {
                MiniAppApiResult(
                    success = false,
                    error = "File path is required",
                    errorCode = 400
                )
            }
        }

        registerApi("getClipboard") { params ->
            try {
                val clipboard = java.awt.Toolkit.getDefaultToolkit().systemClipboard
                val data = clipboard.getData(java.awt.datatransfer.DataFlavor.stringFlavor) as? String
                MiniAppApiResult(
                    success = true,
                    data = mapOf("data" to (data ?: ""))
                )
            } catch (e: Exception) {
                MiniAppApiResult(
                    success = false,
                    error = e.message ?: "Failed to get clipboard",
                    errorCode = 500
                )
            }
        }

        registerApi("setClipboard") { params ->
            val text = params["text"] as? String
            if (text != null) {
                try {
                    val clipboard = java.awt.Toolkit.getDefaultToolkit().systemClipboard
                    val stringSelection = java.awt.datatransfer.StringSelection(text)
                    clipboard.setContents(stringSelection, null)
                    MiniAppApiResult(success = true)
                } catch (e: Exception) {
                    MiniAppApiResult(
                        success = false,
                        error = e.message ?: "Failed to set clipboard",
                        errorCode = 500
                    )
                }
            } else {
                MiniAppApiResult(
                    success = false,
                    error = "Text is required",
                    errorCode = 400
                )
            }
        }
    }

    private fun generateInstanceId(): String {
        return "miniapp_desktop_${platform.name.lowercase()}_${System.currentTimeMillis()}"
    }
}
