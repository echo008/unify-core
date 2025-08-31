package com.unify.ui.components.miniapp

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.unify.ui.LocalUnifyTheme
import com.unify.ui.components.foundation.*

/**
 * 小程序平台类型
 */
enum class UnifyMiniAppPlatform {
    WECHAT,         // 微信小程序
    ALIPAY,         // 支付宝小程序
    BAIDU,          // 百度小程序
    TOUTIAO,        // 字节跳动小程序
    QQ,             // QQ小程序
    KUAISHOU,       // 快手小程序
    XIAOMI,         // 小米小程序
    HUAWEI          // 华为快应用
}

/**
 * 小程序API类型
 */
enum class UnifyMiniAppAPI {
    USER_INFO,      // 用户信息
    PAYMENT,        // 支付
    LOCATION,       // 位置
    CAMERA,         // 相机
    ALBUM,          // 相册
    CONTACTS,       // 通讯录
    CALENDAR,       // 日历
    BLUETOOTH,      // 蓝牙
    WIFI,           // WiFi
    NFC,            // NFC
    BIOMETRIC,      // 生物识别
    DEVICE_INFO,    // 设备信息
    NETWORK,        // 网络状态
    STORAGE,        // 本地存储
    CLIPBOARD,      // 剪贴板
    SHARE,          // 分享
    DOWNLOAD,       // 下载
    UPLOAD,         // 上传
    WEBSOCKET,      // WebSocket
    REQUEST         // 网络请求
}

/**
 * 小程序API调用配置
 */
data class UnifyMiniAppAPIConfig(
    val platform: UnifyMiniAppPlatform,
    val api: UnifyMiniAppAPI,
    val params: Map<String, Any> = emptyMap(),
    val timeout: Long = 10000L
)

/**
 * 小程序API调用结果
 */
data class UnifyMiniAppAPIResult(
    val success: Boolean,
    val data: Map<String, Any>? = null,
    val errorCode: String? = null,
    val errorMessage: String? = null
)

/**
 * 小程序API调用组件
 */
@Composable
fun UnifyMiniAppAPI(
    config: UnifyMiniAppAPIConfig,
    modifier: Modifier = Modifier,
    onResult: ((UnifyMiniAppAPIResult) -> Unit)? = null,
    onError: ((String) -> Unit)? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    var isLoading by remember { mutableStateOf(false) }
    var lastResult by remember { mutableStateOf<UnifyMiniAppAPIResult?>(null) }
    
    LaunchedEffect(config) {
        isLoading = true
        try {
            // 模拟API调用
            kotlinx.coroutines.delay(1000)
            
            val result = when (config.api) {
                UnifyMiniAppAPI.USER_INFO -> {
                    UnifyMiniAppAPIResult(
                        success = true,
                        data = mapOf(
                            "nickName" to "小程序用户",
                            "avatarUrl" to "https://example.com/avatar.jpg",
                            "gender" to 1,
                            "city" to "北京",
                            "province" to "北京",
                            "country" to "中国"
                        )
                    )
                }
                
                UnifyMiniAppAPI.PAYMENT -> {
                    UnifyMiniAppAPIResult(
                        success = true,
                        data = mapOf(
                            "transactionId" to "tx_${System.currentTimeMillis()}",
                            "amount" to config.params["amount"] ?: 0,
                            "status" to "success"
                        )
                    )
                }
                
                UnifyMiniAppAPI.LOCATION -> {
                    UnifyMiniAppAPIResult(
                        success = true,
                        data = mapOf(
                            "latitude" to 39.9042,
                            "longitude" to 116.4074,
                            "accuracy" to 20.0,
                            "address" to "北京市朝阳区"
                        )
                    )
                }
                
                UnifyMiniAppAPI.DEVICE_INFO -> {
                    UnifyMiniAppAPIResult(
                        success = true,
                        data = mapOf(
                            "model" to "iPhone 15 Pro",
                            "system" to "iOS 17.0",
                            "platform" to getPlatformName(config.platform),
                            "version" to "8.0.0",
                            "screenWidth" to 393,
                            "screenHeight" to 852
                        )
                    )
                }
                
                else -> {
                    UnifyMiniAppAPIResult(
                        success = false,
                        errorCode = "API_NOT_IMPLEMENTED",
                        errorMessage = "API ${config.api} not implemented"
                    )
                }
            }
            
            lastResult = result
            onResult?.invoke(result)
        } catch (e: Exception) {
            val errorResult = UnifyMiniAppAPIResult(
                success = false,
                errorCode = "CALL_FAILED",
                errorMessage = e.message
            )
            lastResult = errorResult
            onError?.invoke(e.message ?: "Unknown error")
        } finally {
            isLoading = false
        }
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription?.let { this.contentDescription = it }
            }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = getPlatformIcon(config.platform),
                    contentDescription = null,
                    tint = theme.colors.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                UnifyText(
                    text = "${getPlatformName(config.platform)} API",
                    variant = UnifyTextVariant.H6,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // API信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                UnifyText(
                    text = "API类型:",
                    variant = UnifyTextVariant.BODY_MEDIUM,
                    fontWeight = FontWeight.Medium
                )
                UnifyText(
                    text = getAPIName(config.api),
                    variant = UnifyTextVariant.BODY_MEDIUM
                )
            }
            
            if (config.params.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                UnifyText(
                    text = "参数:",
                    variant = UnifyTextVariant.BODY_MEDIUM,
                    fontWeight = FontWeight.Medium
                )
                config.params.forEach { (key, value) ->
                    Row(
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    ) {
                        UnifyText(
                            text = "$key: ",
                            variant = UnifyTextVariant.BODY_SMALL,
                            color = theme.colors.onSurfaceVariant
                        )
                        UnifyText(
                            text = value.toString(),
                            variant = UnifyTextVariant.BODY_SMALL
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 调用状态
            if (isLoading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    UnifyText(
                        text = "调用中...",
                        variant = UnifyTextVariant.BODY_MEDIUM
                    )
                }
            } else {
                lastResult?.let { result ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (result.success) 
                                Color.Green.copy(alpha = 0.1f) 
                            else 
                                Color.Red.copy(alpha = 0.1f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (result.success) Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = null,
                                    tint = if (result.success) Color.Green else Color.Red,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                UnifyText(
                                    text = if (result.success) "调用成功" else "调用失败",
                                    variant = UnifyTextVariant.BODY_SMALL,
                                    fontWeight = FontWeight.Medium,
                                    color = if (result.success) Color.Green else Color.Red
                                )
                            }
                            
                            if (result.success && result.data != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                result.data.forEach { (key, value) ->
                                    UnifyText(
                                        text = "$key: $value",
                                        variant = UnifyTextVariant.CAPTION,
                                        color = theme.colors.onSurfaceVariant
                                    )
                                }
                            }
                            
                            if (!result.success) {
                                Spacer(modifier = Modifier.height(8.dp))
                                UnifyText(
                                    text = "错误码: ${result.errorCode}",
                                    variant = UnifyTextVariant.CAPTION,
                                    color = Color.Red
                                )
                                result.errorMessage?.let { message ->
                                    UnifyText(
                                        text = "错误信息: $message",
                                        variant = UnifyTextVariant.CAPTION,
                                        color = Color.Red
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 小程序分享组件
 */
@Composable
fun UnifyMiniAppShare(
    title: String,
    description: String,
    imageUrl: String? = null,
    path: String? = null,
    modifier: Modifier = Modifier,
    onShare: ((UnifyMiniAppPlatform) -> Unit)? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    var showShareDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { showShareDialog = true }
            .semantics {
                contentDescription?.let { this.contentDescription = it }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null,
                tint = theme.colors.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                UnifyText(
                    text = title,
                    variant = UnifyTextVariant.BODY_MEDIUM,
                    fontWeight = FontWeight.Medium
                )
                UnifyText(
                    text = description,
                    variant = UnifyTextVariant.BODY_SMALL,
                    color = theme.colors.onSurfaceVariant,
                    maxLines = 2
                )
            }
            
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = theme.colors.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
    
    // 分享对话框
    if (showShareDialog) {
        AlertDialog(
            onDismissRequest = { showShareDialog = false },
            title = {
                UnifyText(
                    text = "选择分享平台",
                    variant = UnifyTextVariant.H6,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height(200.dp)
                ) {
                    UnifyMiniAppPlatform.values().forEach { platform ->
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp)
                                    .clickable {
                                        onShare?.invoke(platform)
                                        showShareDialog = false
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = getPlatformIcon(platform),
                                        contentDescription = null,
                                        tint = getPlatformColor(platform),
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    UnifyText(
                                        text = getPlatformName(platform),
                                        variant = UnifyTextVariant.CAPTION,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showShareDialog = false }) {
                    UnifyText(text = "取消")
                }
            }
        )
    }
}

/**
 * 小程序登录组件
 */
@Composable
fun UnifyMiniAppLogin(
    platform: UnifyMiniAppPlatform,
    modifier: Modifier = Modifier,
    onLoginSuccess: ((Map<String, Any>) -> Unit)? = null,
    onLoginFailed: ((String) -> Unit)? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    var isLoggingIn by remember { mutableStateOf(false) }
    var isLoggedIn by remember { mutableStateOf(false) }
    var userInfo by remember { mutableStateOf<Map<String, Any>?>(null) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription?.let { this.contentDescription = it }
            }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = getPlatformIcon(platform),
                contentDescription = null,
                tint = getPlatformColor(platform),
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            UnifyText(
                text = "${getPlatformName(platform)}登录",
                variant = UnifyTextVariant.H6,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (!isLoggedIn) {
                Button(
                    onClick = {
                        isLoggingIn = true
                        // 模拟登录过程
                    },
                    enabled = !isLoggingIn,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLoggingIn) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = theme.colors.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    UnifyText(
                        text = if (isLoggingIn) "登录中..." else "授权登录",
                        color = theme.colors.onPrimary
                    )
                }
            } else {
                // 显示用户信息
                userInfo?.let { info ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Green.copy(alpha = 0.1f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.Green,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                UnifyText(
                                    text = "登录成功",
                                    variant = UnifyTextVariant.BODY_MEDIUM,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Green
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            info.forEach { (key, value) ->
                                UnifyText(
                                    text = "$key: $value",
                                    variant = UnifyTextVariant.CAPTION,
                                    color = theme.colors.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedButton(
                        onClick = {
                            isLoggedIn = false
                            userInfo = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        UnifyText(text = "退出登录")
                    }
                }
            }
        }
    }
    
    LaunchedEffect(isLoggingIn) {
        if (isLoggingIn) {
            kotlinx.coroutines.delay(2000)
            
            val mockUserInfo = mapOf(
                "nickName" to "小程序用户",
                "avatarUrl" to "https://example.com/avatar.jpg",
                "openid" to "openid_${System.currentTimeMillis()}"
            )
            
            userInfo = mockUserInfo
            isLoggedIn = true
            isLoggingIn = false
            onLoginSuccess?.invoke(mockUserInfo)
        }
    }
}

/**
 * 小程序支付组件
 */
@Composable
fun UnifyMiniAppPayment(
    amount: Double,
    orderInfo: String,
    platform: UnifyMiniAppPlatform,
    modifier: Modifier = Modifier,
    onPaymentSuccess: ((String) -> Unit)? = null,
    onPaymentFailed: ((String) -> Unit)? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    var isProcessing by remember { mutableStateOf(false) }
    var paymentResult by remember { mutableStateOf<String?>(null) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription?.let { this.contentDescription = it }
            }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Payment,
                    contentDescription = null,
                    tint = theme.colors.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                UnifyText(
                    text = "${getPlatformName(platform)}支付",
                    variant = UnifyTextVariant.H6,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 订单信息
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = theme.colors.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        UnifyText(
                            text = "订单信息:",
                            variant = UnifyTextVariant.BODY_MEDIUM,
                            fontWeight = FontWeight.Medium
                        )
                        UnifyText(
                            text = orderInfo,
                            variant = UnifyTextVariant.BODY_MEDIUM
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        UnifyText(
                            text = "支付金额:",
                            variant = UnifyTextVariant.BODY_MEDIUM,
                            fontWeight = FontWeight.Medium
                        )
                        UnifyText(
                            text = "¥${String.format("%.2f", amount)}",
                            variant = UnifyTextVariant.H6,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 支付按钮
            Button(
                onClick = {
                    isProcessing = true
                    // 模拟支付过程
                },
                enabled = !isProcessing,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = getPlatformColor(platform)
                )
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                UnifyText(
                    text = if (isProcessing) "支付中..." else "立即支付",
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // 支付结果
            paymentResult?.let { result ->
                Spacer(modifier = Modifier.height(12.dp))
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (result.startsWith("success")) 
                            Color.Green.copy(alpha = 0.1f) 
                        else 
                            Color.Red.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (result.startsWith("success")) 
                                Icons.Default.CheckCircle 
                            else 
                                Icons.Default.Error,
                            contentDescription = null,
                            tint = if (result.startsWith("success")) Color.Green else Color.Red,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        UnifyText(
                            text = result,
                            variant = UnifyTextVariant.BODY_MEDIUM,
                            color = if (result.startsWith("success")) Color.Green else Color.Red
                        )
                    }
                }
            }
        }
    }
    
    LaunchedEffect(isProcessing) {
        if (isProcessing) {
            kotlinx.coroutines.delay(3000)
            
            val success = (0..1).random() == 1
            if (success) {
                val transactionId = "tx_${System.currentTimeMillis()}"
                paymentResult = "success: 支付成功，交易号: $transactionId"
                onPaymentSuccess?.invoke(transactionId)
            } else {
                paymentResult = "failed: 支付失败，请重试"
                onPaymentFailed?.invoke("Payment failed")
            }
            
            isProcessing = false
        }
    }
}

// 辅助函数
private fun getPlatformIcon(platform: UnifyMiniAppPlatform): ImageVector {
    return when (platform) {
        UnifyMiniAppPlatform.WECHAT -> Icons.Default.Chat
        UnifyMiniAppPlatform.ALIPAY -> Icons.Default.Payment
        UnifyMiniAppPlatform.BAIDU -> Icons.Default.Search
        UnifyMiniAppPlatform.TOUTIAO -> Icons.Default.Article
        UnifyMiniAppPlatform.QQ -> Icons.Default.Forum
        UnifyMiniAppPlatform.KUAISHOU -> Icons.Default.VideoLibrary
        UnifyMiniAppPlatform.XIAOMI -> Icons.Default.PhoneAndroid
        UnifyMiniAppPlatform.HUAWEI -> Icons.Default.Smartphone
    }
}

private fun getPlatformName(platform: UnifyMiniAppPlatform): String {
    return when (platform) {
        UnifyMiniAppPlatform.WECHAT -> "微信"
        UnifyMiniAppPlatform.ALIPAY -> "支付宝"
        UnifyMiniAppPlatform.BAIDU -> "百度"
        UnifyMiniAppPlatform.TOUTIAO -> "字节跳动"
        UnifyMiniAppPlatform.QQ -> "QQ"
        UnifyMiniAppPlatform.KUAISHOU -> "快手"
        UnifyMiniAppPlatform.XIAOMI -> "小米"
        UnifyMiniAppPlatform.HUAWEI -> "华为"
    }
}

private fun getPlatformColor(platform: UnifyMiniAppPlatform): Color {
    return when (platform) {
        UnifyMiniAppPlatform.WECHAT -> Color(0xFF07C160)
        UnifyMiniAppPlatform.ALIPAY -> Color(0xFF1677FF)
        UnifyMiniAppPlatform.BAIDU -> Color(0xFF2932E1)
        UnifyMiniAppPlatform.TOUTIAO -> Color(0xFFFF6600)
        UnifyMiniAppPlatform.QQ -> Color(0xFF12B7F5)
        UnifyMiniAppPlatform.KUAISHOU -> Color(0xFFFF6B35)
        UnifyMiniAppPlatform.XIAOMI -> Color(0xFFFF6900)
        UnifyMiniAppPlatform.HUAWEI -> Color(0xFFFF0000)
    }
}

private fun getAPIName(api: UnifyMiniAppAPI): String {
    return when (api) {
        UnifyMiniAppAPI.USER_INFO -> "用户信息"
        UnifyMiniAppAPI.PAYMENT -> "支付"
        UnifyMiniAppAPI.LOCATION -> "位置信息"
        UnifyMiniAppAPI.CAMERA -> "相机"
        UnifyMiniAppAPI.ALBUM -> "相册"
        UnifyMiniAppAPI.CONTACTS -> "通讯录"
        UnifyMiniAppAPI.CALENDAR -> "日历"
        UnifyMiniAppAPI.BLUETOOTH -> "蓝牙"
        UnifyMiniAppAPI.WIFI -> "WiFi"
        UnifyMiniAppAPI.NFC -> "NFC"
        UnifyMiniAppAPI.BIOMETRIC -> "生物识别"
        UnifyMiniAppAPI.DEVICE_INFO -> "设备信息"
        UnifyMiniAppAPI.NETWORK -> "网络状态"
        UnifyMiniAppAPI.STORAGE -> "本地存储"
        UnifyMiniAppAPI.CLIPBOARD -> "剪贴板"
        UnifyMiniAppAPI.SHARE -> "分享"
        UnifyMiniAppAPI.DOWNLOAD -> "下载"
        UnifyMiniAppAPI.UPLOAD -> "上传"
        UnifyMiniAppAPI.WEBSOCKET -> "WebSocket"
        UnifyMiniAppAPI.REQUEST -> "网络请求"
    }
}
