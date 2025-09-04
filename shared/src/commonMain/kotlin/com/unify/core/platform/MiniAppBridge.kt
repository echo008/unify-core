package com.unify.core.platform

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * 小程序桥接器 - 统一小程序平台API访问
 * 支持微信、支付宝、字节跳动、百度等主流小程序平台
 */
expect class MiniAppBridge {
    /**
     * 获取小程序平台类型
     */
    fun getPlatformType(): MiniAppPlatformType
    
    /**
     * 获取小程序基础信息
     */
    suspend fun getAppInfo(): MiniAppInfo
    
    /**
     * 获取用户信息
     */
    suspend fun getUserInfo(): MiniAppUserInfo?
    
    /**
     * 调用小程序API
     */
    suspend fun callAPI(apiName: String, params: Map<String, Any>): MiniAppAPIResult
    
    /**
     * 监听小程序生命周期
     */
    fun observeLifecycle(): Flow<MiniAppLifecycleEvent>
    
    /**
     * 获取小程序存储管理器
     */
    fun getStorageManager(): MiniAppStorageManager
    
    /**
     * 获取小程序网络管理器
     */
    fun getNetworkManager(): MiniAppNetworkManager
    
    /**
     * 获取小程序UI管理器
     */
    fun getUIManager(): MiniAppUIManager
}

/**
 * 小程序平台类型
 */
enum class MiniAppPlatformType {
    WECHAT,      // 微信小程序
    ALIPAY,      // 支付宝小程序
    BYTEDANCE,   // 字节跳动小程序
    BAIDU,       // 百度智能小程序
    QQ,          // QQ小程序
    KUAISHOU,    // 快手小程序
    UNKNOWN      // 未知平台
}

/**
 * 小程序基础信息
 */
@Serializable
data class MiniAppInfo(
    val appId: String,
    val version: String,
    val platform: MiniAppPlatformType,
    val scene: Int,
    val path: String,
    val query: Map<String, String> = emptyMap(),
    val referrerInfo: MiniAppReferrerInfo? = null
)

/**
 * 小程序来源信息
 */
@Serializable
data class MiniAppReferrerInfo(
    val appId: String,
    val extraData: Map<String, String> = emptyMap()
)

/**
 * 小程序用户信息
 */
@Serializable
data class MiniAppUserInfo(
    val nickName: String,
    val avatarUrl: String,
    val gender: Int,
    val country: String,
    val province: String,
    val city: String,
    val language: String
)

/**
 * 小程序API调用结果
 */
@Serializable
data class MiniAppAPIResult(
    val success: Boolean,
    val data: Map<String, Any>? = null,
    val errorMsg: String? = null,
    val errorCode: Int? = null
)

/**
 * 小程序生命周期事件
 */
sealed class MiniAppLifecycleEvent {
    object OnLaunch : MiniAppLifecycleEvent()
    object OnShow : MiniAppLifecycleEvent()
    object OnHide : MiniAppLifecycleEvent()
    data class OnError(val error: String) : MiniAppLifecycleEvent()
    data class OnPageNotFound(val path: String) : MiniAppLifecycleEvent()
}

/**
 * 小程序存储管理器
 */
expect class MiniAppStorageManager {
    suspend fun setItem(key: String, value: String): Boolean
    suspend fun getItem(key: String): String?
    suspend fun removeItem(key: String): Boolean
    suspend fun clear(): Boolean
    suspend fun getKeys(): List<String>
}

/**
 * 小程序网络管理器
 */
expect class MiniAppNetworkManager {
    suspend fun request(
        url: String,
        method: String = "GET",
        data: Map<String, Any>? = null,
        headers: Map<String, String>? = null
    ): MiniAppNetworkResult
    
    suspend fun uploadFile(
        url: String,
        filePath: String,
        name: String,
        formData: Map<String, String>? = null
    ): MiniAppUploadResult
    
    suspend fun downloadFile(url: String, filePath: String? = null): MiniAppDownloadResult
}

/**
 * 小程序网络请求结果
 */
@Serializable
data class MiniAppNetworkResult(
    val statusCode: Int,
    val data: String,
    val headers: Map<String, String> = emptyMap()
)

/**
 * 小程序文件上传结果
 */
@Serializable
data class MiniAppUploadResult(
    val statusCode: Int,
    val data: String,
    val tempFilePath: String? = null
)

/**
 * 小程序文件下载结果
 */
@Serializable
data class MiniAppDownloadResult(
    val statusCode: Int,
    val tempFilePath: String,
    val filePath: String? = null
)

/**
 * 小程序UI管理器
 */
expect class MiniAppUIManager {
    suspend fun showToast(title: String, icon: String = "success", duration: Int = 1500)
    suspend fun showModal(
        title: String,
        content: String,
        showCancel: Boolean = true,
        cancelText: String = "取消",
        confirmText: String = "确定"
    ): Boolean
    
    suspend fun showActionSheet(itemList: List<String>): Int?
    suspend fun showLoading(title: String = "加载中...")
    suspend fun hideLoading()
    suspend fun navigateTo(url: String)
    suspend fun redirectTo(url: String)
    suspend fun navigateBack(delta: Int = 1)
}

/**
 * 小程序桥接器工厂
 */
object MiniAppBridgeFactory {
    private var instance: MiniAppBridge? = null
    
    fun getInstance(): MiniAppBridge {
        return instance ?: createBridge().also { instance = it }
    }
    
    private fun createBridge(): MiniAppBridge {
        return MiniAppBridge()
    }
}
