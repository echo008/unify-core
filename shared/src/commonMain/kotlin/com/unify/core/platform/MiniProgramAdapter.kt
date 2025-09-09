package com.unify.core.platform

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

/**
 * 小程序适配器 - 统一小程序平台差异处理
 * 提供跨平台的小程序功能适配和API标准化
 */
class MiniProgramAdapter {
    private val bridge = MiniAppBridgeFactory.getInstance()
    private val _platformState = MutableStateFlow(MiniProgramPlatformState())

    val platformState: StateFlow<MiniProgramPlatformState> = _platformState

    /**
     * 初始化小程序适配器
     */
    suspend fun initialize(): MiniProgramInitResult {
        return try {
            val appInfo = bridge.getAppInfo()
            val platformType = bridge.getPlatformType()

            _platformState.value =
                _platformState.value.copy(
                    isInitialized = true,
                    platformType = platformType,
                    appInfo = appInfo,
                )

            MiniProgramInitResult.Success(appInfo)
        } catch (e: Exception) {
            _platformState.value =
                _platformState.value.copy(
                    isInitialized = false,
                    error = e.message,
                )
            MiniProgramInitResult.Error(e.message ?: "初始化失败")
        }
    }

    /**
     * 获取平台能力支持情况
     */
    fun getPlatformCapabilities(): MiniProgramCapabilities {
        val platformType = _platformState.value.platformType
        return when (platformType) {
            MiniAppPlatformType.WECHAT ->
                MiniProgramCapabilities(
                    supportLogin = true,
                    supportPayment = true,
                    supportShare = true,
                    supportLocation = true,
                    supportCamera = true,
                    supportBluetooth = true,
                    supportNFC = false,
                    supportBiometric = false,
                    supportPush = true,
                    supportLivePlayer = true,
                    supportCanvas = true,
                    supportWebGL = false,
                )
            MiniAppPlatformType.ALIPAY ->
                MiniProgramCapabilities(
                    supportLogin = true,
                    supportPayment = true,
                    supportShare = true,
                    supportLocation = true,
                    supportCamera = true,
                    supportBluetooth = false,
                    supportNFC = true,
                    supportBiometric = true,
                    supportPush = true,
                    supportLivePlayer = false,
                    supportCanvas = true,
                    supportWebGL = false,
                )
            MiniAppPlatformType.BYTEDANCE ->
                MiniProgramCapabilities(
                    supportLogin = true,
                    supportPayment = false,
                    supportShare = true,
                    supportLocation = true,
                    supportCamera = true,
                    supportBluetooth = false,
                    supportNFC = false,
                    supportBiometric = false,
                    supportPush = true,
                    supportLivePlayer = true,
                    supportCanvas = true,
                    supportWebGL = true,
                )
            MiniAppPlatformType.BAIDU ->
                MiniProgramCapabilities(
                    supportLogin = true,
                    supportPayment = true,
                    supportShare = true,
                    supportLocation = true,
                    supportCamera = true,
                    supportBluetooth = false,
                    supportNFC = false,
                    supportBiometric = false,
                    supportPush = true,
                    supportLivePlayer = false,
                    supportCanvas = true,
                    supportWebGL = false,
                )
            else -> MiniProgramCapabilities()
        }
    }

    /**
     * 统一登录接口
     */
    suspend fun login(): MiniProgramLoginResult {
        val capabilities = getPlatformCapabilities()
        if (!capabilities.supportLogin) {
            return MiniProgramLoginResult.NotSupported
        }

        return try {
            val result = bridge.callAPI("login", emptyMap())
            if (result.success) {
                val code = result.data?.get("code") as? String
                if (code != null) {
                    MiniProgramLoginResult.Success(code)
                } else {
                    MiniProgramLoginResult.Error("登录失败：未获取到授权码")
                }
            } else {
                MiniProgramLoginResult.Error(result.errorMsg ?: "登录失败")
            }
        } catch (e: Exception) {
            MiniProgramLoginResult.Error(e.message ?: "登录异常")
        }
    }

    /**
     * 统一支付接口
     */
    suspend fun requestPayment(paymentInfo: MiniProgramPaymentInfo): MiniProgramPaymentResult {
        val capabilities = getPlatformCapabilities()
        if (!capabilities.supportPayment) {
            return MiniProgramPaymentResult.NotSupported
        }

        return try {
            val params =
                mapOf(
                    "timeStamp" to paymentInfo.timeStamp,
                    "nonceStr" to paymentInfo.nonceStr,
                    "package" to paymentInfo.packageValue,
                    "signType" to paymentInfo.signType,
                    "paySign" to paymentInfo.paySign,
                )

            val result = bridge.callAPI("requestPayment", params)
            if (result.success) {
                MiniProgramPaymentResult.Success
            } else {
                MiniProgramPaymentResult.Error(result.errorMsg ?: "支付失败")
            }
        } catch (e: Exception) {
            MiniProgramPaymentResult.Error(e.message ?: "支付异常")
        }
    }

    /**
     * 统一分享接口
     */
    suspend fun share(shareInfo: MiniProgramShareInfo): MiniProgramShareResult {
        val capabilities = getPlatformCapabilities()
        if (!capabilities.supportShare) {
            return MiniProgramShareResult.NotSupported
        }

        return try {
            val params =
                mapOf(
                    "title" to shareInfo.title,
                    "desc" to shareInfo.desc,
                    "path" to shareInfo.path,
                    "imageUrl" to shareInfo.imageUrl,
                )

            val result = bridge.callAPI("share", params)
            if (result.success) {
                MiniProgramShareResult.Success
            } else {
                MiniProgramShareResult.Error(result.errorMsg ?: "分享失败")
            }
        } catch (e: Exception) {
            MiniProgramShareResult.Error(e.message ?: "分享异常")
        }
    }

    /**
     * 获取位置信息
     */
    suspend fun getLocation(type: String = "wgs84"): MiniProgramLocationResult {
        val capabilities = getPlatformCapabilities()
        if (!capabilities.supportLocation) {
            return MiniProgramLocationResult.NotSupported
        }

        return try {
            val params = mapOf("type" to type)
            val result = bridge.callAPI("getLocation", params)

            if (result.success && result.data != null) {
                val latitude = result.data["latitude"] as? Double ?: 0.0
                val longitude = result.data["longitude"] as? Double ?: 0.0
                val speed = result.data["speed"] as? Double ?: 0.0
                val accuracy = result.data["accuracy"] as? Double ?: 0.0

                val location =
                    MiniProgramLocation(
                        latitude = latitude,
                        longitude = longitude,
                        speed = speed,
                        accuracy = accuracy,
                    )
                MiniProgramLocationResult.Success(location)
            } else {
                MiniProgramLocationResult.Error(result.errorMsg ?: "获取位置失败")
            }
        } catch (e: Exception) {
            MiniProgramLocationResult.Error(e.message ?: "获取位置异常")
        }
    }

    /**
     * 监听小程序生命周期
     */
    fun observeLifecycle(): Flow<MiniAppLifecycleEvent> {
        return bridge.observeLifecycle()
    }
}

/**
 * 小程序平台状态
 */
@Serializable
data class MiniProgramPlatformState(
    val isInitialized: Boolean = false,
    val platformType: MiniAppPlatformType = MiniAppPlatformType.UNKNOWN,
    val appInfo: MiniAppInfo? = null,
    val error: String? = null,
)

/**
 * 小程序初始化结果
 */
sealed class MiniProgramInitResult {
    data class Success(val appInfo: MiniAppInfo) : MiniProgramInitResult()

    data class Error(val message: String) : MiniProgramInitResult()
}

/**
 * 小程序平台能力
 */
@Serializable
data class MiniProgramCapabilities(
    val supportLogin: Boolean = false,
    val supportPayment: Boolean = false,
    val supportShare: Boolean = false,
    val supportLocation: Boolean = false,
    val supportCamera: Boolean = false,
    val supportBluetooth: Boolean = false,
    val supportNFC: Boolean = false,
    val supportBiometric: Boolean = false,
    val supportPush: Boolean = false,
    val supportLivePlayer: Boolean = false,
    val supportCanvas: Boolean = false,
    val supportWebGL: Boolean = false,
)

/**
 * 小程序登录结果
 */
sealed class MiniProgramLoginResult {
    data class Success(val code: String) : MiniProgramLoginResult()

    data class Error(val message: String) : MiniProgramLoginResult()

    object NotSupported : MiniProgramLoginResult()
}

/**
 * 小程序支付信息
 */
@Serializable
data class MiniProgramPaymentInfo(
    val timeStamp: String,
    val nonceStr: String,
    val packageValue: String,
    val signType: String,
    val paySign: String,
)

/**
 * 小程序支付结果
 */
sealed class MiniProgramPaymentResult {
    object Success : MiniProgramPaymentResult()

    data class Error(val message: String) : MiniProgramPaymentResult()

    object NotSupported : MiniProgramPaymentResult()
}

/**
 * 小程序分享信息
 */
@Serializable
data class MiniProgramShareInfo(
    val title: String,
    val desc: String,
    val path: String,
    val imageUrl: String,
)

/**
 * 小程序分享结果
 */
sealed class MiniProgramShareResult {
    object Success : MiniProgramShareResult()

    data class Error(val message: String) : MiniProgramShareResult()

    object NotSupported : MiniProgramShareResult()
}

/**
 * 小程序位置信息
 */
@Serializable
data class MiniProgramLocation(
    val latitude: Double,
    val longitude: Double,
    val speed: Double,
    val accuracy: Double,
)

/**
 * 小程序位置结果
 */
sealed class MiniProgramLocationResult {
    data class Success(val location: MiniProgramLocation) : MiniProgramLocationResult()

    data class Error(val message: String) : MiniProgramLocationResult()

    object NotSupported : MiniProgramLocationResult()
}
