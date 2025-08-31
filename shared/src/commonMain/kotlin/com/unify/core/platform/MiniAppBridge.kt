package com.unify.core.platform

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

/**
 * 小程序平台桥接接口定义
 * 支持微信小程序、支付宝小程序、字节跳动小程序、百度小程序等
 */
interface MiniAppBridge {
    
    /**
     * 初始化小程序桥接
     */
    fun initialize()
    
    /**
     * 获取小程序平台类型
     */
    fun getMiniAppPlatform(): MiniAppPlatform
    
    /**
     * 获取小程序环境信息
     */
    suspend fun getMiniAppInfo(): MiniAppInfo
    
    /**
     * 调用小程序原生API
     */
    suspend fun invokeNativeAPI(apiName: String, params: Map<String, Any>): MiniAppResult
    
    /**
     * 监听小程序生命周期事件
     */
    fun observeLifecycleEvents(): Flow<MiniAppLifecycleEvent>
    
    /**
     * 获取小程序页面信息
     */
    suspend fun getPageInfo(): MiniAppPageInfo
    
    /**
     * 导航到小程序页面
     */
    suspend fun navigateToPage(path: String, params: Map<String, Any> = emptyMap()): Boolean
    
    /**
     * 返回上一页
     */
    suspend fun navigateBack(): Boolean
    
    /**
     * 显示小程序原生组件
     */
    suspend fun showNativeComponent(component: MiniAppComponent): Boolean
    
    /**
     * 隐藏小程序原生组件
     */
    suspend fun hideNativeComponent(componentId: String): Boolean
    
    /**
     * 获取用户信息
     */
    suspend fun getUserInfo(): MiniAppUserInfo?
    
    /**
     * 获取位置信息
     */
    suspend fun getLocation(): MiniAppLocation?
    
    /**
     * 选择图片
     */
    suspend fun chooseImage(config: ImageChooseConfig): List<MiniAppImage>
    
    /**
     * 预览图片
     */
    suspend fun previewImage(urls: List<String>, current: Int = 0): Boolean
    
    /**
     * 保存图片到相册
     */
    suspend fun saveImageToPhotosAlbum(imagePath: String): Boolean
    
    /**
     * 扫码
     */
    suspend fun scanCode(): MiniAppScanResult?
    
    /**
     * 分享内容
     */
    suspend fun shareContent(content: MiniAppShareContent): Boolean
    
    /**
     * 支付
     */
    suspend fun requestPayment(paymentInfo: MiniAppPaymentInfo): MiniAppPaymentResult
    
    /**
     * 获取网络状态
     */
    suspend fun getNetworkType(): MiniAppNetworkType
    
    /**
     * 监听网络状态变化
     */
    fun observeNetworkStatusChange(): Flow<MiniAppNetworkType>
    
    /**
     * 设置存储数据
     */
    suspend fun setStorage(key: String, data: String): Boolean
    
    /**
     * 获取存储数据
     */
    suspend fun getStorage(key: String): String?
    
    /**
     * 删除存储数据
     */
    suspend fun removeStorage(key: String): Boolean
    
    /**
     * 清空存储数据
     */
    suspend fun clearStorage(): Boolean
    
    /**
     * 获取系统信息
     */
    suspend fun getSystemInfo(): MiniAppSystemInfo
    
    /**
     * 显示Toast
     */
    suspend fun showToast(title: String, icon: MiniAppToastIcon = MiniAppToastIcon.SUCCESS, duration: Int = 1500)
    
    /**
     * 显示Loading
     */
    suspend fun showLoading(title: String = "加载中...")
    
    /**
     * 隐藏Loading
     */
    suspend fun hideLoading()
    
    /**
     * 显示Modal
     */
    suspend fun showModal(config: MiniAppModalConfig): MiniAppModalResult
    
    /**
     * 显示ActionSheet
     */
    suspend fun showActionSheet(itemList: List<String>): Int
    
    /**
     * 设置导航栏标题
     */
    suspend fun setNavigationBarTitle(title: String): Boolean
    
    /**
     * 设置导航栏颜色
     */
    suspend fun setNavigationBarColor(frontColor: String, backgroundColor: String): Boolean
}

/**
 * 小程序平台类型
 */
enum class MiniAppPlatform {
    WECHAT,      // 微信小程序
    ALIPAY,      // 支付宝小程序
    BYTEDANCE,   // 字节跳动小程序
    BAIDU,       // 百度小程序
    QQ,          // QQ小程序
    KUAISHOU,    // 快手小程序
    UNKNOWN
}

/**
 * 小程序信息
 */
@Serializable
data class MiniAppInfo(
    val platform: MiniAppPlatform,
    val version: String,
    val appId: String,
    val scene: Int,
    val path: String,
    val query: Map<String, String> = emptyMap(),
    val shareTicket: String? = null,
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
 * 小程序API调用结果
 */
@Serializable
data class MiniAppResult(
    val success: Boolean,
    val data: Map<String, Any> = emptyMap(),
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
    object OnError : MiniAppLifecycleEvent()
    data class OnPageNotFound(val path: String) : MiniAppLifecycleEvent()
    data class OnUnhandledRejection(val reason: String) : MiniAppLifecycleEvent()
}

/**
 * 小程序页面信息
 */
@Serializable
data class MiniAppPageInfo(
    val route: String,
    val options: Map<String, String> = emptyMap()
)

/**
 * 小程序原生组件
 */
sealed class MiniAppComponent {
    data class Map(
        val id: String,
        val longitude: Double,
        val latitude: Double,
        val scale: Int = 16,
        val markers: List<MiniAppMapMarker> = emptyList()
    ) : MiniAppComponent()
    
    data class Video(
        val id: String,
        val src: String,
        val autoplay: Boolean = false,
        val loop: Boolean = false,
        val muted: Boolean = false
    ) : MiniAppComponent()
    
    data class Camera(
        val id: String,
        val mode: String = "normal", // normal, scanCode
        val resolution: String = "medium" // low, medium, high
    ) : MiniAppComponent()
    
    data class LivePlayer(
        val id: String,
        val src: String,
        val mode: String = "live", // live, RTC
        val autoplay: Boolean = false,
        val muted: Boolean = false
    ) : MiniAppComponent()
}

/**
 * 地图标记
 */
@Serializable
data class MiniAppMapMarker(
    val id: String,
    val longitude: Double,
    val latitude: Double,
    val title: String? = null,
    val iconPath: String? = null,
    val width: Int = 30,
    val height: Int = 30
)

/**
 * 小程序用户信息
 */
@Serializable
data class MiniAppUserInfo(
    val nickName: String,
    val avatarUrl: String,
    val gender: Int, // 0: 未知, 1: 男, 2: 女
    val city: String,
    val province: String,
    val country: String,
    val language: String
)

/**
 * 小程序位置信息
 */
@Serializable
data class MiniAppLocation(
    val latitude: Double,
    val longitude: Double,
    val speed: Double,
    val accuracy: Double,
    val altitude: Double,
    val verticalAccuracy: Double,
    val horizontalAccuracy: Double
)

/**
 * 图片选择配置
 */
@Serializable
data class ImageChooseConfig(
    val count: Int = 9,
    val sizeType: List<String> = listOf("original", "compressed"),
    val sourceType: List<String> = listOf("album", "camera")
)

/**
 * 小程序图片
 */
@Serializable
data class MiniAppImage(
    val path: String,
    val size: Long
)

/**
 * 扫码结果
 */
@Serializable
data class MiniAppScanResult(
    val result: String,
    val scanType: String,
    val charSet: String,
    val path: String
)

/**
 * 分享内容
 */
@Serializable
data class MiniAppShareContent(
    val title: String,
    val desc: String? = null,
    val path: String? = null,
    val imageUrl: String? = null
)

/**
 * 支付信息
 */
@Serializable
data class MiniAppPaymentInfo(
    val timeStamp: String,
    val nonceStr: String,
    val package_: String,
    val signType: String,
    val paySign: String
)

/**
 * 支付结果
 */
@Serializable
data class MiniAppPaymentResult(
    val success: Boolean,
    val errMsg: String? = null
)

/**
 * 网络类型
 */
enum class MiniAppNetworkType {
    WIFI,
    CELLULAR_2G,
    CELLULAR_3G,
    CELLULAR_4G,
    CELLULAR_5G,
    UNKNOWN,
    NONE
}

/**
 * 系统信息
 */
@Serializable
data class MiniAppSystemInfo(
    val brand: String,
    val model: String,
    val pixelRatio: Float,
    val screenWidth: Int,
    val screenHeight: Int,
    val windowWidth: Int,
    val windowHeight: Int,
    val statusBarHeight: Int,
    val language: String,
    val version: String,
    val system: String,
    val platform: String,
    val fontSizeSetting: Int,
    val SDKVersion: String,
    val benchmarkLevel: Int,
    val albumAuthorized: Boolean,
    val cameraAuthorized: Boolean,
    val locationAuthorized: Boolean,
    val microphoneAuthorized: Boolean,
    val notificationAuthorized: Boolean,
    val bluetoothEnabled: Boolean,
    val locationEnabled: Boolean,
    val wifiEnabled: Boolean,
    val safeArea: MiniAppSafeArea
)

/**
 * 安全区域
 */
@Serializable
data class MiniAppSafeArea(
    val left: Int,
    val right: Int,
    val top: Int,
    val bottom: Int,
    val width: Int,
    val height: Int
)

/**
 * Toast图标类型
 */
enum class MiniAppToastIcon {
    SUCCESS,
    LOADING,
    NONE
}

/**
 * Modal配置
 */
@Serializable
data class MiniAppModalConfig(
    val title: String,
    val content: String,
    val showCancel: Boolean = true,
    val cancelText: String = "取消",
    val cancelColor: String = "#000000",
    val confirmText: String = "确定",
    val confirmColor: String = "#576B95"
)

/**
 * Modal结果
 */
@Serializable
data class MiniAppModalResult(
    val confirm: Boolean,
    val cancel: Boolean
)
