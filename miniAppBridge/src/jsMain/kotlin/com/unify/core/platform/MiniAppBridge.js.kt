package com.unify.core.platform

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.js.Json
import kotlin.js.json

/**
 * 小程序桥接实现 - 支持微信、支付宝、字节跳动等主流小程序平台
 */
actual object MiniAppBridgeImpl : MiniAppBridge {
    
    private var currentPlatform: MiniAppPlatform = MiniAppPlatform.UNKNOWN
    private var isInitialized = false
    
    actual override fun initialize() {
        if (!isInitialized) {
            detectMiniAppPlatform()
            setupPlatformSpecificFeatures()
            isInitialized = true
        }
    }
    
    private fun detectMiniAppPlatform() {
        currentPlatform = when {
            js("typeof wx !== 'undefined'") as Boolean -> MiniAppPlatform.WECHAT
            js("typeof my !== 'undefined'") as Boolean -> MiniAppPlatform.ALIPAY
            js("typeof tt !== 'undefined'") as Boolean -> MiniAppPlatform.BYTEDANCE
            js("typeof swan !== 'undefined'") as Boolean -> MiniAppPlatform.BAIDU
            js("typeof qq !== 'undefined'") as Boolean -> MiniAppPlatform.QQ
            js("typeof ks !== 'undefined'") as Boolean -> MiniAppPlatform.KUAISHOU
            else -> MiniAppPlatform.UNKNOWN
        }
    }
    
    private fun setupPlatformSpecificFeatures() {
        when (currentPlatform) {
            MiniAppPlatform.WECHAT -> setupWeChatFeatures()
            MiniAppPlatform.ALIPAY -> setupAlipayFeatures()
            MiniAppPlatform.BYTEDANCE -> setupByteDanceFeatures()
            MiniAppPlatform.BAIDU -> setupBaiduFeatures()
            else -> {}
        }
    }
    
    private fun setupWeChatFeatures() {
        // 微信小程序特定初始化
    }
    
    private fun setupAlipayFeatures() {
        // 支付宝小程序特定初始化
    }
    
    private fun setupByteDanceFeatures() {
        // 字节跳动小程序特定初始化
    }
    
    private fun setupBaiduFeatures() {
        // 百度小程序特定初始化
    }
    
    actual override fun getMiniAppPlatform(): MiniAppPlatform = currentPlatform
    
    actual override suspend fun getMiniAppInfo(): MiniAppInfo = suspendCancellableCoroutine { continuation ->
        val api = getPlatformAPI()
        api.getLaunchOptionsSync?.let { getLaunchOptions ->
            val options = getLaunchOptions()
            val info = MiniAppInfo(
                platform = currentPlatform,
                version = getAppVersion(),
                appId = getAppId(),
                scene = options.scene ?: 0,
                path = options.path ?: "",
                query = options.query?.let { convertJsObjectToMap(it) } ?: emptyMap()
            )
            continuation.resume(info)
        } ?: continuation.resume(
            MiniAppInfo(
                platform = currentPlatform,
                version = "unknown",
                appId = "unknown",
                scene = 0,
                path = ""
            )
        )
    }
    
    actual override suspend fun invokeNativeAPI(apiName: String, params: Map<String, Any>): MiniAppResult = 
        suspendCancellableCoroutine { continuation ->
            val api = getPlatformAPI()
            val jsParams = convertMapToJsObject(params)
            
            val successCallback: (dynamic) -> Unit = { result ->
                continuation.resume(MiniAppResult(
                    success = true,
                    data = convertJsObjectToMap(result)
                ))
            }
            
            val failCallback: (dynamic) -> Unit = { error ->
                continuation.resume(MiniAppResult(
                    success = false,
                    errorMsg = error.errMsg?.toString(),
                    errorCode = error.errCode?.toString()?.toIntOrNull()
                ))
            }
            
            jsParams.success = successCallback
            jsParams.fail = failCallback
            
            try {
                api.asDynamic()[apiName](jsParams)
            } catch (e: Exception) {
                continuation.resume(MiniAppResult(
                    success = false,
                    errorMsg = e.message
                ))
            }
        }
    
    actual override fun observeLifecycleEvents(): Flow<MiniAppLifecycleEvent> = callbackFlow {
        // 小程序生命周期事件监听实现
        val app = js("getApp()")
        
        val originalOnLaunch = app.onLaunch
        app.onLaunch = { options: dynamic ->
            trySend(MiniAppLifecycleEvent.OnLaunch)
            originalOnLaunch?.call(app, options)
        }
        
        val originalOnShow = app.onShow
        app.onShow = { options: dynamic ->
            trySend(MiniAppLifecycleEvent.OnShow)
            originalOnShow?.call(app, options)
        }
        
        val originalOnHide = app.onHide
        app.onHide = {
            trySend(MiniAppLifecycleEvent.OnHide)
            originalOnHide?.call(app)
        }
        
        awaitClose {
            app.onLaunch = originalOnLaunch
            app.onShow = originalOnShow
            app.onHide = originalOnHide
        }
    }
    
    actual override suspend fun showToast(title: String, icon: MiniAppToastIcon, duration: Int) {
        val api = getPlatformAPI()
        val iconStr = when (icon) {
            MiniAppToastIcon.SUCCESS -> "success"
            MiniAppToastIcon.LOADING -> "loading"
            MiniAppToastIcon.NONE -> "none"
        }
        
        api.showToast(json(
            "title" to title,
            "icon" to iconStr,
            "duration" to duration
        ))
    }
    
    actual override suspend fun getUserInfo(): MiniAppUserInfo? = suspendCancellableCoroutine { continuation ->
        val api = getPlatformAPI()
        
        api.getUserInfo(json(
            "success" to { result: dynamic ->
                val userInfo = result.userInfo
                continuation.resume(MiniAppUserInfo(
                    nickName = userInfo.nickName ?: "",
                    avatarUrl = userInfo.avatarUrl ?: "",
                    gender = userInfo.gender ?: 0,
                    city = userInfo.city ?: "",
                    province = userInfo.province ?: "",
                    country = userInfo.country ?: "",
                    language = userInfo.language ?: ""
                ))
            },
            "fail" to { _: dynamic ->
                continuation.resume(null)
            }
        ))
    }
    
    actual override suspend fun getNetworkType(): MiniAppNetworkType = suspendCancellableCoroutine { continuation ->
        val api = getPlatformAPI()
        
        api.getNetworkType(json(
            "success" to { result: dynamic ->
                val networkType = when (result.networkType?.toString()) {
                    "wifi" -> MiniAppNetworkType.WIFI
                    "2g" -> MiniAppNetworkType.CELLULAR_2G
                    "3g" -> MiniAppNetworkType.CELLULAR_3G
                    "4g" -> MiniAppNetworkType.CELLULAR_4G
                    "5g" -> MiniAppNetworkType.CELLULAR_5G
                    "none" -> MiniAppNetworkType.NONE
                    else -> MiniAppNetworkType.UNKNOWN
                }
                continuation.resume(networkType)
            },
            "fail" to { _: dynamic ->
                continuation.resume(MiniAppNetworkType.UNKNOWN)
            }
        ))
    }
    
    private fun getPlatformAPI(): dynamic {
        return when (currentPlatform) {
            MiniAppPlatform.WECHAT -> js("wx")
            MiniAppPlatform.ALIPAY -> js("my")
            MiniAppPlatform.BYTEDANCE -> js("tt")
            MiniAppPlatform.BAIDU -> js("swan")
            MiniAppPlatform.QQ -> js("qq")
            MiniAppPlatform.KUAISHOU -> js("ks")
            else -> js("{}")
        }
    }
    
    private fun getAppVersion(): String {
        return try {
            val api = getPlatformAPI()
            api.getSystemInfoSync().version ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }
    }
    
    private fun getAppId(): String {
        return try {
            val api = getPlatformAPI()
            api.getAccountInfoSync().miniProgram.appId ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }
    }
    
    private fun convertJsObjectToMap(obj: dynamic): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val keys = js("Object.keys(obj)") as Array<String>
        for (key in keys) {
            map[key] = obj[key]?.toString() ?: ""
        }
        return map
    }
    
    private fun convertMapToJsObject(map: Map<String, Any>): dynamic {
        val obj = js("{}")
        for ((key, value) in map) {
            obj[key] = value
        }
        return obj
    }
    
    actual override suspend fun getPageInfo(): MiniAppPageInfo = suspendCancellableCoroutine { continuation ->
        try {
            val pages = js("getCurrentPages()") as Array<dynamic>
            val currentPage = pages.lastOrNull()
            if (currentPage != null) {
                continuation.resume(MiniAppPageInfo(
                    route = currentPage.route ?: "",
                    options = convertJsObjectToMap(currentPage.options ?: js("{}"))
                ))
            } else {
                continuation.resume(MiniAppPageInfo(""))
            }
        } catch (e: Exception) {
            continuation.resume(MiniAppPageInfo(""))
        }
    }
    
    actual override suspend fun navigateToPage(path: String, params: Map<String, Any>): Boolean = suspendCancellableCoroutine { continuation ->
        val api = getPlatformAPI()
        api.navigateTo(json(
            "url" to buildUrlWithParams(path, params),
            "success" to { _: dynamic -> continuation.resume(true) },
            "fail" to { _: dynamic -> continuation.resume(false) }
        ))
    }
    
    actual override suspend fun navigateBack(): Boolean = suspendCancellableCoroutine { continuation ->
        val api = getPlatformAPI()
        api.navigateBack(json(
            "success" to { _: dynamic -> continuation.resume(true) },
            "fail" to { _: dynamic -> continuation.resume(false) }
        ))
    }
    
    actual override suspend fun chooseImage(config: ImageChooseConfig): List<MiniAppImage> = suspendCancellableCoroutine { continuation ->
        val api = getPlatformAPI()
        api.chooseImage(json(
            "count" to config.count,
            "sizeType" to config.sizeType.toTypedArray(),
            "sourceType" to config.sourceType.toTypedArray(),
            "success" to { result: dynamic ->
                val images = (result.tempFiles as? Array<dynamic>)?.map { file ->
                    MiniAppImage(
                        path = file.path ?: file.tempFilePath ?: "",
                        size = (file.size as? Number)?.toLong() ?: 0L
                    )
                } ?: emptyList()
                continuation.resume(images)
            },
            "fail" to { _: dynamic -> continuation.resume(emptyList()) }
        ))
    }
    
    actual override suspend fun setStorage(key: String, data: String): Boolean = suspendCancellableCoroutine { continuation ->
        val api = getPlatformAPI()
        api.setStorage(json(
            "key" to key,
            "data" to data,
            "success" to { _: dynamic -> continuation.resume(true) },
            "fail" to { _: dynamic -> continuation.resume(false) }
        ))
    }
    
    actual override suspend fun getStorage(key: String): String? = suspendCancellableCoroutine { continuation ->
        val api = getPlatformAPI()
        api.getStorage(json(
            "key" to key,
            "success" to { result: dynamic -> continuation.resume(result.data?.toString()) },
            "fail" to { _: dynamic -> continuation.resume(null) }
        ))
    }
    
    actual override suspend fun removeStorage(key: String): Boolean = suspendCancellableCoroutine { continuation ->
        val api = getPlatformAPI()
        api.removeStorage(json(
            "key" to key,
            "success" to { _: dynamic -> continuation.resume(true) },
            "fail" to { _: dynamic -> continuation.resume(false) }
        ))
    }
    
    actual override suspend fun clearStorage(): Boolean = suspendCancellableCoroutine { continuation ->
        val api = getPlatformAPI()
        api.clearStorage(json(
            "success" to { _: dynamic -> continuation.resume(true) },
            "fail" to { _: dynamic -> continuation.resume(false) }
        ))
    }
    
    actual override suspend fun getSystemInfo(): MiniAppSystemInfo = suspendCancellableCoroutine { continuation ->
        val api = getPlatformAPI()
        try {
            val systemInfo = api.getSystemInfoSync()
            continuation.resume(MiniAppSystemInfo(
                brand = systemInfo.brand ?: "",
                model = systemInfo.model ?: "",
                pixelRatio = (systemInfo.pixelRatio as? Number)?.toFloat() ?: 1f,
                screenWidth = (systemInfo.screenWidth as? Number)?.toInt() ?: 0,
                screenHeight = (systemInfo.screenHeight as? Number)?.toInt() ?: 0,
                windowWidth = (systemInfo.windowWidth as? Number)?.toInt() ?: 0,
                windowHeight = (systemInfo.windowHeight as? Number)?.toInt() ?: 0,
                statusBarHeight = (systemInfo.statusBarHeight as? Number)?.toInt() ?: 0,
                language = systemInfo.language ?: "",
                version = systemInfo.version ?: "",
                system = systemInfo.system ?: "",
                platform = systemInfo.platform ?: "",
                fontSizeSetting = (systemInfo.fontSizeSetting as? Number)?.toInt() ?: 16,
                SDKVersion = systemInfo.SDKVersion ?: "",
                benchmarkLevel = (systemInfo.benchmarkLevel as? Number)?.toInt() ?: 0,
                albumAuthorized = systemInfo.albumAuthorized == true,
                cameraAuthorized = systemInfo.cameraAuthorized == true,
                locationAuthorized = systemInfo.locationAuthorized == true,
                microphoneAuthorized = systemInfo.microphoneAuthorized == true,
                notificationAuthorized = systemInfo.notificationAuthorized == true,
                bluetoothEnabled = systemInfo.bluetoothEnabled == true,
                locationEnabled = systemInfo.locationEnabled == true,
                wifiEnabled = systemInfo.wifiEnabled == true,
                safeArea = MiniAppSafeArea(
                    left = (systemInfo.safeArea?.left as? Number)?.toInt() ?: 0,
                    right = (systemInfo.safeArea?.right as? Number)?.toInt() ?: 0,
                    top = (systemInfo.safeArea?.top as? Number)?.toInt() ?: 0,
                    bottom = (systemInfo.safeArea?.bottom as? Number)?.toInt() ?: 0,
                    width = (systemInfo.safeArea?.width as? Number)?.toInt() ?: 0,
                    height = (systemInfo.safeArea?.height as? Number)?.toInt() ?: 0
                )
            ))
        } catch (e: Exception) {
            continuation.resume(MiniAppSystemInfo(
                brand = "", model = "", pixelRatio = 1f, screenWidth = 0, screenHeight = 0,
                windowWidth = 0, windowHeight = 0, statusBarHeight = 0, language = "",
                version = "", system = "", platform = "", fontSizeSetting = 16,
                SDKVersion = "", benchmarkLevel = 0, albumAuthorized = false,
                cameraAuthorized = false, locationAuthorized = false, microphoneAuthorized = false,
                notificationAuthorized = false, bluetoothEnabled = false, locationEnabled = false,
                wifiEnabled = false, safeArea = MiniAppSafeArea(0, 0, 0, 0, 0, 0)
            ))
        }
    }
    
    private fun buildUrlWithParams(path: String, params: Map<String, Any>): String {
        if (params.isEmpty()) return path
        val queryString = params.entries.joinToString("&") { "${it.key}=${it.value}" }
        return if (path.contains("?")) "$path&$queryString" else "$path?$queryString"
    }
    
    // 其他未实现的方法使用默认实现
    actual override suspend fun showNativeComponent(component: MiniAppComponent): Boolean = false
    actual override suspend fun hideNativeComponent(componentId: String): Boolean = false
    actual override suspend fun getLocation(): MiniAppLocation? = null
    actual override suspend fun previewImage(urls: List<String>, current: Int): Boolean = false
    actual override suspend fun saveImageToPhotosAlbum(imagePath: String): Boolean = false
    actual override suspend fun scanCode(): MiniAppScanResult? = null
    actual override suspend fun shareContent(content: MiniAppShareContent): Boolean = false
    actual override suspend fun requestPayment(paymentInfo: MiniAppPaymentInfo): MiniAppPaymentResult = MiniAppPaymentResult(false)
    actual override fun observeNetworkStatusChange(): Flow<MiniAppNetworkType> = callbackFlow { close() }
    actual override suspend fun showLoading(title: String) {}
    actual override suspend fun hideLoading() {}
    actual override suspend fun showModal(config: MiniAppModalConfig): MiniAppModalResult = MiniAppModalResult(false, true)
    actual override suspend fun showActionSheet(itemList: List<String>): Int = -1
    actual override suspend fun setNavigationBarTitle(title: String): Boolean = false
    actual override suspend fun setNavigationBarColor(frontColor: String, backgroundColor: String): Boolean = false
}
