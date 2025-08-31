package com.unify.ui.components.open

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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unify.ui.LocalUnifyTheme
import com.unify.ui.components.foundation.*

/**
 * Unify 开放能力组件
 * 对应微信小程序的 web-view、ad、official-account 等开放能力组件
 */

/**
 * WebView 组件
 */
@Composable
fun UnifyWebView(
    src: String,
    modifier: Modifier = Modifier,
    onMessage: ((data: Map<String, Any>) -> Unit)? = null,
    onLoad: ((url: String) -> Unit)? = null,
    onError: ((error: String) -> Unit)? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    val uriHandler = LocalUriHandler.current
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    
    LaunchedEffect(src) {
        if (src.isNotEmpty()) {
            isLoading = true
            hasError = false
            try {
                // 模拟加载过程
                kotlinx.coroutines.delay(1000)
                isLoading = false
                onLoad?.invoke(src)
            } catch (e: Exception) {
                isLoading = false
                hasError = true
                onError?.invoke(e.message ?: "加载失败")
            }
        }
    }
    
    Card(
        modifier = modifier
            .fillMaxSize()
            .semantics {
                contentDescription?.let { 
                    this.contentDescription = it 
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = theme.colors.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        UnifyLoading(
                            variant = UnifyLoadingVariant.CIRCULAR,
                            size = UnifyLoadingSize.LARGE
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        UnifyText(
                            text = "正在加载网页...",
                            variant = UnifyTextVariant.BODY_MEDIUM,
                            color = theme.colors.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                
                hasError -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        UnifyIcon(
                            icon = Icons.Default.Error,
                            size = UnifyIconSize.EXTRA_LARGE,
                            tint = theme.colors.error
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        UnifyText(
                            text = "网页加载失败",
                            variant = UnifyTextVariant.BODY_LARGE,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        UnifyText(
                            text = "请检查网络连接或稍后重试",
                            variant = UnifyTextVariant.BODY_SMALL,
                            color = theme.colors.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        UnifyButton(
                            text = "重新加载",
                            onClick = {
                                isLoading = true
                                hasError = false
                            }
                        )
                    }
                }
                
                else -> {
                    // 模拟 WebView 内容
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        UnifyIcon(
                            icon = Icons.Default.Language,
                            size = UnifyIconSize.EXTRA_LARGE,
                            tint = theme.colors.primary
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        UnifyText(
                            text = "WebView 内容",
                            variant = UnifyTextVariant.TITLE_LARGE,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        UnifyText(
                            text = src,
                            variant = UnifyTextVariant.BODY_SMALL,
                            color = theme.colors.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.clickable {
                                uriHandler.openUri(src)
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        UnifyText(
                            text = "在实际应用中，这里会显示网页内容",
                            variant = UnifyTextVariant.BODY_MEDIUM,
                            color = theme.colors.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

/**
 * 广告组件类型
 */
enum class UnifyAdType {
    BANNER,         // 横幅广告
    INTERSTITIAL,   // 插屏广告
    REWARDED_VIDEO, // 激励视频广告
    NATIVE,         // 原生广告
    GRID            // 格子广告
}

/**
 * 广告组件配置
 */
data class UnifyAdConfig(
    val adUnitId: String,
    val type: UnifyAdType = UnifyAdType.BANNER,
    val adIntervals: Long = 30000L, // 广告刷新间隔
    val adTheme: String = "white", // white, black
    val adLeft: Dp = 0.dp,
    val adTop: Dp = 0.dp,
    val adWidth: Dp = 300.dp,
    val adHeight: Dp = 250.dp,
    val gridOpacity: Float = 0.8f,
    val gridCount: Int = 5
)

/**
 * 广告组件
 */
@Composable
fun UnifyAd(
    config: UnifyAdConfig,
    modifier: Modifier = Modifier,
    onLoad: (() -> Unit)? = null,
    onError: ((error: String) -> Unit)? = null,
    onClose: (() -> Unit)? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(true) }
    
    LaunchedEffect(config.adUnitId) {
        if (config.adUnitId.isNotEmpty()) {
            isLoading = true
            hasError = false
            try {
                // 模拟广告加载
                kotlinx.coroutines.delay(1500)
                isLoading = false
                onLoad?.invoke()
            } catch (e: Exception) {
                isLoading = false
                hasError = true
                onError?.invoke(e.message ?: "广告加载失败")
            }
        }
    }
    
    if (isVisible) {
        Card(
            modifier = modifier
                .size(config.adWidth, config.adHeight)
                .semantics {
                    contentDescription?.let { 
                        this.contentDescription = it 
                    }
                },
            colors = CardDefaults.cardColors(
                containerColor = if (config.adTheme == "black") Color.Black else Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                UnifyLoading(
                                    variant = UnifyLoadingVariant.CIRCULAR,
                                    size = UnifyLoadingSize.MEDIUM
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                UnifyText(
                                    text = "广告加载中...",
                                    variant = UnifyTextVariant.CAPTION,
                                    color = if (config.adTheme == "black") Color.White else Color.Black
                                )
                            }
                        }
                    }
                    
                    hasError -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                UnifyIcon(
                                    icon = Icons.Default.Error,
                                    size = UnifyIconSize.MEDIUM,
                                    tint = theme.colors.error
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                UnifyText(
                                    text = "广告加载失败",
                                    variant = UnifyTextVariant.CAPTION,
                                    color = if (config.adTheme == "black") Color.White else Color.Black
                                )
                            }
                        }
                    }
                    
                    else -> {
                        // 模拟广告内容
                        when (config.type) {
                            UnifyAdType.BANNER -> {
                                UnifyBannerAd(config)
                            }
                            UnifyAdType.NATIVE -> {
                                UnifyNativeAd(config)
                            }
                            UnifyAdType.GRID -> {
                                UnifyGridAd(config)
                            }
                            else -> {
                                UnifyBannerAd(config)
                            }
                        }
                    }
                }
                
                // 关闭按钮
                if (!isLoading && !hasError) {
                    IconButton(
                        onClick = {
                            isVisible = false
                            onClose?.invoke()
                        },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        UnifyIcon(
                            icon = Icons.Default.Close,
                            size = UnifyIconSize.SMALL,
                            tint = if (config.adTheme == "black") Color.White else Color.Black
                        )
                    }
                }
            }
        }
    }
}

/**
 * 横幅广告
 */
@Composable
private fun UnifyBannerAd(config: UnifyAdConfig) {
    val textColor = if (config.adTheme == "black") Color.White else Color.Black
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            UnifyIcon(
                icon = Icons.Default.Campaign,
                size = UnifyIconSize.LARGE,
                tint = LocalUnifyTheme.current.colors.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            UnifyText(
                text = "广告内容",
                variant = UnifyTextVariant.BODY_LARGE,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
            
            UnifyText(
                text = "这里显示广告详情",
                variant = UnifyTextVariant.BODY_SMALL,
                color = textColor.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 原生广告
 */
@Composable
private fun UnifyNativeAd(config: UnifyAdConfig) {
    val textColor = if (config.adTheme == "black") Color.White else Color.Black
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(LocalUnifyTheme.current.colors.primary),
                contentAlignment = Alignment.Center
            ) {
                UnifyIcon(
                    icon = Icons.Default.Apps,
                    size = UnifyIconSize.MEDIUM,
                    tint = Color.White
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                UnifyText(
                    text = "原生广告标题",
                    variant = UnifyTextVariant.BODY_MEDIUM,
                    fontWeight = FontWeight.Medium,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                UnifyText(
                    text = "广告描述信息",
                    variant = UnifyTextVariant.BODY_SMALL,
                    color = textColor.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(LocalUnifyTheme.current.colors.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            UnifyText(
                text = "广告图片",
                variant = UnifyTextVariant.BODY_MEDIUM,
                color = LocalUnifyTheme.current.colors.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        UnifyButton(
            text = "了解更多",
            modifier = Modifier.fillMaxWidth(),
            onClick = { /* 广告点击 */ }
        )
    }
}

/**
 * 格子广告
 */
@Composable
private fun UnifyGridAd(config: UnifyAdConfig) {
    val textColor = if (config.adTheme == "black") Color.White else Color.Black
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(if (config.gridCount <= 3) config.gridCount else 3),
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        contentPadding = PaddingValues(4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(config.gridCount) { index ->
            Card(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clickable { /* 广告点击 */ },
                colors = CardDefaults.cardColors(
                    containerColor = LocalUnifyTheme.current.colors.surfaceVariant.copy(alpha = config.gridOpacity)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    UnifyIcon(
                        icon = Icons.Default.ShoppingCart,
                        size = UnifyIconSize.MEDIUM,
                        tint = LocalUnifyTheme.current.colors.primary
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    UnifyText(
                        text = "广告${index + 1}",
                        variant = UnifyTextVariant.CAPTION,
                        color = textColor,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * 公众号关注组件
 */
@Composable
fun UnifyOfficialAccount(
    modifier: Modifier = Modifier,
    onLoad: (() -> Unit)? = null,
    onError: ((error: String) -> Unit)? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var isFollowed by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        try {
            // 模拟加载公众号信息
            kotlinx.coroutines.delay(1000)
            isLoading = false
            onLoad?.invoke()
        } catch (e: Exception) {
            isLoading = false
            hasError = true
            onError?.invoke(e.message ?: "加载失败")
        }
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription?.let { 
                    this.contentDescription = it 
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = theme.colors.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    UnifyLoading(
                        variant = UnifyLoadingVariant.CIRCULAR,
                        size = UnifyLoadingSize.MEDIUM
                    )
                }
            }
            
            hasError -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        UnifyIcon(
                            icon = Icons.Default.Error,
                            size = UnifyIconSize.MEDIUM,
                            tint = theme.colors.error
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        UnifyText(
                            text = "加载失败",
                            variant = UnifyTextVariant.BODY_MEDIUM,
                            color = theme.colors.error
                        )
                    }
                }
            }
            
            else -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 公众号头像
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(theme.colors.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        UnifyIcon(
                            icon = Icons.Default.AccountBox,
                            size = UnifyIconSize.LARGE,
                            tint = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // 公众号信息
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        UnifyText(
                            text = "示例公众号",
                            variant = UnifyTextVariant.BODY_LARGE,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        UnifyText(
                            text = "提供优质内容和服务",
                            variant = UnifyTextVariant.BODY_SMALL,
                            color = theme.colors.onSurface.copy(alpha = 0.7f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // 关注按钮
                    UnifyButton(
                        text = if (isFollowed) "已关注" else "关注",
                        variant = if (isFollowed) UnifyButtonVariant.OUTLINED else UnifyButtonVariant.FILLED,
                        onClick = {
                            isFollowed = !isFollowed
                        }
                    )
                }
            }
        }
    }
}

/**
 * 开放数据组件
 */
@Composable
fun UnifyOpenData(
    type: String, // groupName, userNickName, userAvatarUrl, userGender, userCity, userProvince, userCountry, userLanguage
    modifier: Modifier = Modifier,
    openGid: String = "",
    lang: String = "zh_CN",
    defaultText: String = "",
    defaultAvatar: String = "",
    onError: ((error: String) -> Unit)? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    var isLoading by remember { mutableStateOf(true) }
    var data by remember { mutableStateOf("") }
    
    LaunchedEffect(type, openGid) {
        try {
            // 模拟获取开放数据
            kotlinx.coroutines.delay(500)
            data = when (type) {
                "groupName" -> "示例群聊"
                "userNickName" -> "用户昵称"
                "userCity" -> "北京"
                "userProvince" -> "北京"
                "userCountry" -> "中国"
                "userLanguage" -> "zh_CN"
                "userGender" -> "1"
                else -> defaultText
            }
            isLoading = false
        } catch (e: Exception) {
            isLoading = false
            onError?.invoke(e.message ?: "获取数据失败")
        }
    }
    
    Box(
        modifier = modifier.semantics {
            contentDescription?.let { 
                this.contentDescription = it 
            }
        }
    ) {
        when {
            isLoading -> {
                UnifyLoading(
                    variant = UnifyLoadingVariant.CIRCULAR,
                    size = UnifyLoadingSize.SMALL
                )
            }
            
            type == "userAvatarUrl" -> {
                // 用户头像
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(theme.colors.primary),
                    contentAlignment = Alignment.Center
                ) {
                    UnifyIcon(
                        icon = Icons.Default.Person,
                        size = UnifyIconSize.MEDIUM,
                        tint = Color.White
                    )
                }
            }
            
            else -> {
                UnifyText(
                    text = data.ifEmpty { defaultText },
                    variant = UnifyTextVariant.BODY_MEDIUM
                )
            }
        }
    }
}
