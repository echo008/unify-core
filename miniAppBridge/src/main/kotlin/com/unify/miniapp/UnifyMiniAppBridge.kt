package com.unify.miniapp

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import com.unify.ui.state.*
import com.unify.network.*
import com.unify.storage.*
import com.unify.platform.*

/**
 * Unify 小程序桥接模块
 * 支持微信小程序、支付宝小程序、百度小程序、字节跳动小程序等
 */

/**
 * 小程序平台类型
 */
enum class MiniAppPlatform(val platformId: String, val displayName: String) {
    WECHAT("wechat", "微信小程序"),
    ALIPAY("alipay", "支付宝小程序"),
    BAIDU("baidu", "百度智能小程序"),
    TIKTOK("tiktok", "抖音小程序"),
    QQ("qq", "QQ小程序"),
    KUAISHOU("kuaishou", "快手小程序"),
    HARMONY_MINI("harmony", "HarmonyOS原子化服务")
}

/**
 * 小程序桥接管理器
 */
class UnifyMiniAppBridge {
    private val platformAdapters = mutableMapOf<MiniAppPlatform, MiniAppPlatformAdapter>()
    private val _currentPlatform = MutableStateFlow<MiniAppPlatform?>(null)
    val currentPlatform: StateFlow<MiniAppPlatform?> = _currentPlatform.asStateFlow()
    
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    /**
     * 初始化小程序桥接
     */
    suspend fun initialize() {
        detectCurrentPlatform()
        initializePlatformAdapters()
    }
    
    /**
     * 检测当前小程序平台
     */
    private suspend fun detectCurrentPlatform() {
        val platform = when {
            isWeChatMiniProgram() -> MiniAppPlatform.WECHAT
            isAlipayMiniProgram() -> MiniAppPlatform.ALIPAY
            isBaiduMiniProgram() -> MiniAppPlatform.BAIDU
            isTikTokMiniProgram() -> MiniAppPlatform.TIKTOK
            isQQMiniProgram() -> MiniAppPlatform.QQ
            isKuaishouMiniProgram() -> MiniAppPlatform.KUAISHOU
            isHarmonyMiniService() -> MiniAppPlatform.HARMONY_MINI
            else -> null
        }
        _currentPlatform.value = platform
    }
    
    /**
     * 初始化平台适配器
     */
    private suspend fun initializePlatformAdapters() {
        platformAdapters[MiniAppPlatform.WECHAT] = WeChatMiniAppAdapter()
        platformAdapters[MiniAppPlatform.ALIPAY] = AlipayMiniAppAdapter()
        platformAdapters[MiniAppPlatform.BAIDU] = BaiduMiniAppAdapter()
        platformAdapters[MiniAppPlatform.TIKTOK] = TikTokMiniAppAdapter()
        platformAdapters[MiniAppPlatform.QQ] = QQMiniAppAdapter()
        platformAdapters[MiniAppPlatform.KUAISHOU] = KuaishouMiniAppAdapter()
        platformAdapters[MiniAppPlatform.HARMONY_MINI] = HarmonyMiniServiceAdapter()
        
        // 初始化当前平台适配器
        _currentPlatform.value?.let { platform ->
            platformAdapters[platform]?.initialize()
        }
    }
    
    /**
     * 获取当前平台适配器
     */
    fun getCurrentAdapter(): MiniAppPlatformAdapter? {
        return _currentPlatform.value?.let { platformAdapters[it] }
    }
    
    /**
     * 获取指定平台适配器
     */
    fun getAdapter(platform: MiniAppPlatform): MiniAppPlatformAdapter? {
        return platformAdapters[platform]
    }
    
    /**
     * 调用小程序API
     */
    suspend fun <T> callMiniAppAPI(
        apiName: String,
        parameters: Map<String, Any> = emptyMap()
    ): Result<T> {
        val adapter = getCurrentAdapter()
        return if (adapter != null) {
            adapter.callAPI(apiName, parameters)
        } else {
            Result.failure(IllegalStateException("No adapter available for current platform"))
        }
    }
    
    /**
     * 获取小程序系统信息
     */
    suspend fun getSystemInfo(): Result<MiniAppSystemInfo> {
        return callMiniAppAPI("getSystemInfo")
    }
    
    /**
     * 显示小程序Toast
     */
    suspend fun showToast(title: String, icon: String = "success", duration: Int = 1500): Result<Unit> {
        return callMiniAppAPI("showToast", mapOf(
            "title" to title,
            "icon" to icon,
            "duration" to duration
        ))
    }
    
    /**
     * 显示小程序Loading
     */
    suspend fun showLoading(title: String = "加载中"): Result<Unit> {
        return callMiniAppAPI("showLoading", mapOf("title" to title))
    }
    
    /**
     * 隐藏小程序Loading
     */
    suspend fun hideLoading(): Result<Unit> {
        return callMiniAppAPI("hideLoading")
    }
    
    /**
     * 小程序页面导航
     */
    suspend fun navigateTo(url: String): Result<Unit> {
        return callMiniAppAPI("navigateTo", mapOf("url" to url))
    }
    
    /**
     * 小程序页面重定向
     */
    suspend fun redirectTo(url: String): Result<Unit> {
        return callMiniAppAPI("redirectTo", mapOf("url" to url))
    }
    
    /**
     * 小程序页面返回
     */
    suspend fun navigateBack(delta: Int = 1): Result<Unit> {
        return callMiniAppAPI("navigateBack", mapOf("delta" to delta))
    }
    
    /**
     * 获取小程序存储数据
     */
    suspend fun getStorageSync(key: String): Result<String?> {
        return callMiniAppAPI("getStorageSync", mapOf("key" to key))
    }
    
    /**
     * 设置小程序存储数据
     */
    suspend fun setStorageSync(key: String, data: String): Result<Unit> {
        return callMiniAppAPI("setStorageSync", mapOf("key" to key, "data" to data))
    }
    
    /**
     * 发起小程序网络请求
     */
    suspend fun request(
        url: String,
        method: String = "GET",
        data: Any? = null,
        header: Map<String, String> = emptyMap()
    ): Result<MiniAppNetworkResponse> {
        return callMiniAppAPI("request", mapOf(
            "url" to url,
            "method" to method,
            "data" to data,
            "header" to header
        ))
    }
    
    // 平台检测方法
    private fun isWeChatMiniProgram(): Boolean {
        return js("typeof wx !== 'undefined' && wx.getSystemInfoSync") as? Boolean ?: false
    }
    
    private fun isAlipayMiniProgram(): Boolean {
        return js("typeof my !== 'undefined' && my.getSystemInfoSync") as? Boolean ?: false
    }
    
    private fun isBaiduMiniProgram(): Boolean {
        return js("typeof swan !== 'undefined' && swan.getSystemInfoSync") as? Boolean ?: false
    }
    
    private fun isTikTokMiniProgram(): Boolean {
        return js("typeof tt !== 'undefined' && tt.getSystemInfoSync") as? Boolean ?: false
    }
    
    private fun isQQMiniProgram(): Boolean {
        return js("typeof qq !== 'undefined' && qq.getSystemInfoSync") as? Boolean ?: false
    }
    
    private fun isKuaishouMiniProgram(): Boolean {
        return js("typeof ks !== 'undefined' && ks.getSystemInfoSync") as? Boolean ?: false
    }
    
    private fun isHarmonyMiniService(): Boolean {
        // HarmonyOS原子化服务检测逻辑
        return false // 简化实现
    }
}

/**
 * 小程序平台适配器接口
 */
interface MiniAppPlatformAdapter {
    val platform: MiniAppPlatform
    
    suspend fun initialize()
    suspend fun <T> callAPI(apiName: String, parameters: Map<String, Any>): Result<T>
    suspend fun getSystemInfo(): Result<MiniAppSystemInfo>
    suspend fun onShow(callback: () -> Unit)
    suspend fun onHide(callback: () -> Unit)
    suspend fun onError(callback: (String) -> Unit)
}

/**
 * 小程序系统信息
 */
@Serializable
data class MiniAppSystemInfo(
    val brand: String,
    val model: String,
    val system: String,
    val platform: String,
    val version: String,
    val SDKVersion: String,
    val screenWidth: Int,
    val screenHeight: Int,
    val windowWidth: Int,
    val windowHeight: Int,
    val pixelRatio: Double,
    val statusBarHeight: Int,
    val language: String,
    val fontSizeSetting: Int
)

/**
 * 小程序网络响应
 */
@Serializable
data class MiniAppNetworkResponse(
    val data: String,
    val statusCode: Int,
    val header: Map<String, String>
)

/**
 * 微信小程序适配器
 */
class WeChatMiniAppAdapter : MiniAppPlatformAdapter {
    override val platform = MiniAppPlatform.WECHAT
    
    override suspend fun initialize() {
        // 微信小程序初始化逻辑
    }
    
    override suspend fun <T> callAPI(apiName: String, parameters: Map<String, Any>): Result<T> {
        return try {
            val result = callWeChatAPI(apiName, parameters)
            @Suppress("UNCHECKED_CAST")
            Result.success(result as T)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getSystemInfo(): Result<MiniAppSystemInfo> {
        return callAPI("getSystemInfoSync")
    }
    
    override suspend fun onShow(callback: () -> Unit) {
        js("wx.onAppShow(callback)")
    }
    
    override suspend fun onHide(callback: () -> Unit) {
        js("wx.onAppHide(callback)")
    }
    
    override suspend fun onError(callback: (String) -> Unit) {
        js("wx.onError(callback)")
    }
    
    private fun callWeChatAPI(apiName: String, parameters: Map<String, Any>): Any {
        return js("wx[apiName](parameters)")
    }
}

/**
 * 支付宝小程序适配器
 */
class AlipayMiniAppAdapter : MiniAppPlatformAdapter {
    override val platform = MiniAppPlatform.ALIPAY
    
    override suspend fun initialize() {
        // 支付宝小程序初始化逻辑
    }
    
    override suspend fun <T> callAPI(apiName: String, parameters: Map<String, Any>): Result<T> {
        return try {
            val result = callAlipayAPI(apiName, parameters)
            @Suppress("UNCHECKED_CAST")
            Result.success(result as T)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getSystemInfo(): Result<MiniAppSystemInfo> {
        return callAPI("getSystemInfoSync")
    }
    
    override suspend fun onShow(callback: () -> Unit) {
        js("my.onAppShow(callback)")
    }
    
    override suspend fun onHide(callback: () -> Unit) {
        js("my.onAppHide(callback)")
    }
    
    override suspend fun onError(callback: (String) -> Unit) {
        js("my.onError(callback)")
    }
    
    private fun callAlipayAPI(apiName: String, parameters: Map<String, Any>): Any {
        return js("my[apiName](parameters)")
    }
}

/**
 * 百度小程序适配器
 */
class BaiduMiniAppAdapter : MiniAppPlatformAdapter {
    override val platform = MiniAppPlatform.BAIDU
    
    override suspend fun initialize() {
        // 百度小程序初始化逻辑
    }
    
    override suspend fun <T> callAPI(apiName: String, parameters: Map<String, Any>): Result<T> {
        return try {
            val result = callBaiduAPI(apiName, parameters)
            @Suppress("UNCHECKED_CAST")
            Result.success(result as T)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getSystemInfo(): Result<MiniAppSystemInfo> {
        return callAPI("getSystemInfoSync")
    }
    
    override suspend fun onShow(callback: () -> Unit) {
        js("swan.onAppShow(callback)")
    }
    
    override suspend fun onHide(callback: () -> Unit) {
        js("swan.onAppHide(callback)")
    }
    
    override suspend fun onError(callback: (String) -> Unit) {
        js("swan.onError(callback)")
    }
    
    private fun callBaiduAPI(apiName: String, parameters: Map<String, Any>): Any {
        return js("swan[apiName](parameters)")
    }
}

/**
 * 字节跳动小程序适配器
 */
class TikTokMiniAppAdapter : MiniAppPlatformAdapter {
    override val platform = MiniAppPlatform.TIKTOK
    
    override suspend fun initialize() {
        // 字节跳动小程序初始化逻辑
    }
    
    override suspend fun <T> callAPI(apiName: String, parameters: Map<String, Any>): Result<T> {
        return try {
            val result = callTikTokAPI(apiName, parameters)
            @Suppress("UNCHECKED_CAST")
            Result.success(result as T)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getSystemInfo(): Result<MiniAppSystemInfo> {
        return callAPI("getSystemInfoSync")
    }
    
    override suspend fun onShow(callback: () -> Unit) {
        js("tt.onAppShow(callback)")
    }
    
    override suspend fun onHide(callback: () -> Unit) {
        js("tt.onAppHide(callback)")
    }
    
    override suspend fun onError(callback: (String) -> Unit) {
        js("tt.onError(callback)")
    }
    
    private fun callTikTokAPI(apiName: String, parameters: Map<String, Any>): Any {
        return js("tt[apiName](parameters)")
    }
}

/**
 * QQ小程序适配器
 */
class QQMiniAppAdapter : MiniAppPlatformAdapter {
    override val platform = MiniAppPlatform.QQ
    
    override suspend fun initialize() {
        // QQ小程序初始化逻辑
    }
    
    override suspend fun <T> callAPI(apiName: String, parameters: Map<String, Any>): Result<T> {
        return try {
            val result = callQQAPI(apiName, parameters)
            @Suppress("UNCHECKED_CAST")
            Result.success(result as T)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getSystemInfo(): Result<MiniAppSystemInfo> {
        return callAPI("getSystemInfoSync")
    }
    
    override suspend fun onShow(callback: () -> Unit) {
        js("qq.onAppShow(callback)")
    }
    
    override suspend fun onHide(callback: () -> Unit) {
        js("qq.onAppHide(callback)")
    }
    
    override suspend fun onError(callback: (String) -> Unit) {
        js("qq.onError(callback)")
    }
    
    private fun callQQAPI(apiName: String, parameters: Map<String, Any>): Any {
        return js("qq[apiName](parameters)")
    }
}

/**
 * 快手小程序适配器
 */
class KuaishouMiniAppAdapter : MiniAppPlatformAdapter {
    override val platform = MiniAppPlatform.KUAISHOU
    
    override suspend fun initialize() {
        // 快手小程序初始化逻辑
    }
    
    override suspend fun <T> callAPI(apiName: String, parameters: Map<String, Any>): Result<T> {
        return try {
            val result = callKuaishouAPI(apiName, parameters)
            @Suppress("UNCHECKED_CAST")
            Result.success(result as T)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getSystemInfo(): Result<MiniAppSystemInfo> {
        return callAPI("getSystemInfoSync")
    }
    
    override suspend fun onShow(callback: () -> Unit) {
        js("ks.onAppShow(callback)")
    }
    
    override suspend fun onHide(callback: () -> Unit) {
        js("ks.onAppHide(callback)")
    }
    
    override suspend fun onError(callback: (String) -> Unit) {
        js("ks.onError(callback)")
    }
    
    private fun callKuaishouAPI(apiName: String, parameters: Map<String, Any>): Any {
        return js("ks[apiName](parameters)")
    }
}

/**
 * HarmonyOS原子化服务适配器
 */
class HarmonyMiniServiceAdapter : MiniAppPlatformAdapter {
    override val platform = MiniAppPlatform.HARMONY_MINI
    
    override suspend fun initialize() {
        // HarmonyOS原子化服务初始化逻辑
    }
    
    override suspend fun <T> callAPI(apiName: String, parameters: Map<String, Any>): Result<T> {
        return try {
            val result = callHarmonyAPI(apiName, parameters)
            @Suppress("UNCHECKED_CAST")
            Result.success(result as T)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getSystemInfo(): Result<MiniAppSystemInfo> {
        return callAPI("getSystemInfoSync")
    }
    
    override suspend fun onShow(callback: () -> Unit) {
        // HarmonyOS生命周期回调
    }
    
    override suspend fun onHide(callback: () -> Unit) {
        // HarmonyOS生命周期回调
    }
    
    override suspend fun onError(callback: (String) -> Unit) {
        // HarmonyOS错误回调
    }
    
    private fun callHarmonyAPI(apiName: String, parameters: Map<String, Any>): Any {
        // HarmonyOS原子化服务API调用
        return Unit
    }
}

/**
 * 小程序工具类
 */
object MiniAppUtils {
    
    /**
     * 检测小程序环境
     */
    fun detectMiniAppEnvironment(): MiniAppPlatform? {
        return when {
            js("typeof wx !== 'undefined'") as Boolean -> MiniAppPlatform.WECHAT
            js("typeof my !== 'undefined'") as Boolean -> MiniAppPlatform.ALIPAY
            js("typeof swan !== 'undefined'") as Boolean -> MiniAppPlatform.BAIDU
            js("typeof tt !== 'undefined'") as Boolean -> MiniAppPlatform.TIKTOK
            js("typeof qq !== 'undefined'") as Boolean -> MiniAppPlatform.QQ
            js("typeof ks !== 'undefined'") as Boolean -> MiniAppPlatform.KUAISHOU
            else -> null
        }
    }
    
    /**
     * 格式化小程序URL
     */
    fun formatMiniAppUrl(path: String, query: Map<String, String> = emptyMap()): String {
        val queryString = if (query.isNotEmpty()) {
            "?" + query.map { "${it.key}=${it.value}" }.joinToString("&")
        } else {
            ""
        }
        return "$path$queryString"
    }
    
    /**
     * 解析小程序URL参数
     */
    fun parseUrlQuery(url: String): Map<String, String> {
        val queryStart = url.indexOf('?')
        if (queryStart == -1) return emptyMap()
        
        val queryString = url.substring(queryStart + 1)
        return queryString.split('&').associate { param ->
            val (key, value) = param.split('=', limit = 2)
            key to (value ?: "")
        }
    }
}
