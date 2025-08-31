package com.unify.ui.components.system

import androidx.compose.animation.*
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.unify.ui.LocalUnifyTheme
import com.unify.ui.components.foundation.*

/**
 * Unify 系统组件
 * 对应微信小程序的 navigation-bar、page-meta 等系统组件
 */

/**
 * 导航栏样式
 */
enum class UnifyNavigationBarStyle {
    DEFAULT,    // 默认样式
    CUSTOM      // 自定义样式
}

/**
 * 导航栏配置
 */
data class UnifyNavigationBarConfig(
    val title: String = "",
    val loading: Boolean = false,
    val frontColor: Color = Color.Black,
    val backgroundColor: Color = Color.White,
    val colorAnimationDuration: Long = 0L,
    val colorAnimationTimingFunc: String = "linear",
    val animation: UnifyNavigationBarAnimation = UnifyNavigationBarAnimation(),
    val style: UnifyNavigationBarStyle = UnifyNavigationBarStyle.DEFAULT,
    val homePage: String = "",
    val showBackButton: Boolean = true,
    val showHomeButton: Boolean = false,
    val showMenuButton: Boolean = false,
    val showShareButton: Boolean = false,
    val showFavoriteButton: Boolean = false
)

/**
 * 导航栏动画配置
 */
data class UnifyNavigationBarAnimation(
    val duration: Long = 300L,
    val timingFunc: String = "linear" // linear, easeIn, easeOut, easeInOut
)

/**
 * 系统导航栏组件
 */
@Composable
fun UnifySystemNavigationBar(
    config: UnifyNavigationBarConfig,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    onHomeClick: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    onShareClick: (() -> Unit)? = null,
    onFavoriteClick: (() -> Unit)? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    val density = LocalDensity.current
    
    // 动画颜色
    val animatedBackgroundColor by animateColorAsState(
        targetValue = config.backgroundColor,
        animationSpec = tween(
            durationMillis = config.colorAnimationDuration.toInt(),
            easing = when (config.colorAnimationTimingFunc) {
                "easeIn" -> FastOutSlowInEasing
                "easeOut" -> LinearOutSlowInEasing
                "easeInOut" -> FastOutLinearInEasing
                else -> LinearEasing
            }
        ),
        label = "nav_background_color"
    )
    
    val animatedFrontColor by animateColorAsState(
        targetValue = config.frontColor,
        animationSpec = tween(
            durationMillis = config.colorAnimationDuration.toInt(),
            easing = when (config.colorAnimationTimingFunc) {
                "easeIn" -> FastOutSlowInEasing
                "easeOut" -> LinearOutSlowInEasing
                "easeInOut" -> FastOutLinearInEasing
                else -> LinearEasing
            }
        ),
        label = "nav_front_color"
    )
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .semantics {
                contentDescription?.let { 
                    this.contentDescription = it 
                }
            },
        color = animatedBackgroundColor,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧按钮组
            Row {
                if (config.showBackButton) {
                    IconButton(
                        onClick = { onBackClick?.invoke() }
                    ) {
                        UnifyIcon(
                            icon = Icons.Default.ArrowBack,
                            size = UnifyIconSize.MEDIUM,
                            tint = animatedFrontColor
                        )
                    }
                }
                
                if (config.showHomeButton) {
                    IconButton(
                        onClick = { onHomeClick?.invoke() }
                    ) {
                        UnifyIcon(
                            icon = Icons.Default.Home,
                            size = UnifyIconSize.MEDIUM,
                            tint = animatedFrontColor
                        )
                    }
                }
            }
            
            // 标题区域
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UnifyText(
                        text = config.title,
                        variant = UnifyTextVariant.TITLE_MEDIUM,
                        color = animatedFrontColor,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                    
                    if (config.loading) {
                        Spacer(modifier = Modifier.width(8.dp))
                        UnifyLoading(
                            variant = UnifyLoadingVariant.CIRCULAR,
                            size = UnifyLoadingSize.SMALL
                        )
                    }
                }
            }
            
            // 右侧按钮组
            Row {
                if (config.showShareButton) {
                    IconButton(
                        onClick = { onShareClick?.invoke() }
                    ) {
                        UnifyIcon(
                            icon = Icons.Default.Share,
                            size = UnifyIconSize.MEDIUM,
                            tint = animatedFrontColor
                        )
                    }
                }
                
                if (config.showFavoriteButton) {
                    IconButton(
                        onClick = { onFavoriteClick?.invoke() }
                    ) {
                        UnifyIcon(
                            icon = Icons.Default.Favorite,
                            size = UnifyIconSize.MEDIUM,
                            tint = animatedFrontColor
                        )
                    }
                }
                
                if (config.showMenuButton) {
                    IconButton(
                        onClick = { onMenuClick?.invoke() }
                    ) {
                        UnifyIcon(
                            icon = Icons.Default.MoreVert,
                            size = UnifyIconSize.MEDIUM,
                            tint = animatedFrontColor
                        )
                    }
                }
            }
        }
    }
}

/**
 * 页面元数据配置
 */
data class UnifyPageMetaConfig(
    val backgroundColor: Color = Color.White,
    val textSizeAdjust: String = "auto", // auto, none, percentage
    val backgroundColorTop: Color = Color.White,
    val backgroundColorBottom: Color = Color.White,
    val scrollTop: Dp = 0.dp,
    val scrollDuration: Long = 300L,
    val pageStyle: String = "",
    val rootFontSize: String = "16px",
    val rootBackgroundColor: Color = Color.White,
    val pageOrientation: String = "auto", // auto, portrait, landscape
    val enablePullDownRefresh: Boolean = false,
    val onReachBottomDistance: Dp = 50.dp,
    val backgroundTextStyle: String = "dark" // dark, light
)

/**
 * 页面元数据组件
 */
@Composable
fun UnifyPageMeta(
    config: UnifyPageMetaConfig,
    modifier: Modifier = Modifier,
    onResize: ((width: Dp, height: Dp) -> Unit)? = null,
    onScroll: ((scrollTop: Dp) -> Unit)? = null,
    onReachBottom: (() -> Unit)? = null,
    onPullDownRefresh: (() -> Unit)? = null,
    contentDescription: String? = null
) {
    val density = LocalDensity.current
    
    // 页面元数据设置
    LaunchedEffect(config) {
        // 在实际实现中，这里会设置页面的全局样式
        // 包括背景色、字体大小、页面方向等
    }
    
    // 滚动到指定位置
    LaunchedEffect(config.scrollTop) {
        if (config.scrollTop > 0.dp) {
            onScroll?.invoke(config.scrollTop)
        }
    }
    
    // 页面元数据不渲染可见内容，但会影响页面行为
    Box(
        modifier = modifier.semantics {
            contentDescription?.let { 
                this.contentDescription = it 
            }
        }
    ) {
        // 空内容，仅用于设置页面元数据
    }
}

/**
 * 自定义导航栏组件
 */
@Composable
fun UnifyCustomNavigationBar(
    modifier: Modifier = Modifier,
    height: Dp = 56.dp,
    backgroundColor: Color = LocalUnifyTheme.current.colors.surface,
    contentColor: Color = LocalUnifyTheme.current.colors.onSurface,
    elevation: Dp = 4.dp,
    contentDescription: String? = null,
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .semantics {
                contentDescription?.let { 
                    this.contentDescription = it 
                }
            },
        color = backgroundColor,
        contentColor = contentColor,
        shadowElevation = elevation
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

/**
 * 状态栏配置
 */
data class UnifyStatusBarConfig(
    val style: String = "default", // default, light-content, dark-content
    val backgroundColor: Color = Color.Transparent,
    val hidden: Boolean = false,
    val animation: String = "none" // none, fade, slide
)

/**
 * 状态栏组件
 */
@Composable
fun UnifyStatusBar(
    config: UnifyStatusBarConfig,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    // 状态栏样式设置
    LaunchedEffect(config) {
        // 在实际实现中，这里会调用平台特定的API来设置状态栏样式
        // Android: WindowInsetsController
        // iOS: UIStatusBarStyle
    }
    
    if (!config.hidden) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(config.backgroundColor)
                .semantics {
                    contentDescription?.let { 
                        this.contentDescription = it 
                    }
                }
        )
    }
}

/**
 * 系统弹窗组件
 */
@Composable
fun UnifySystemDialog(
    title: String,
    content: String,
    showCancel: Boolean = true,
    cancelText: String = "取消",
    confirmText: String = "确定",
    onCancel: (() -> Unit)? = null,
    onConfirm: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    AlertDialog(
        onDismissRequest = { onDismiss?.invoke() },
        title = {
            UnifyText(
                text = title,
                variant = UnifyTextVariant.TITLE_LARGE,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            UnifyText(
                text = content,
                variant = UnifyTextVariant.BODY_MEDIUM,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            UnifyButton(
                text = confirmText,
                onClick = { onConfirm?.invoke() }
            )
        },
        dismissButton = if (showCancel) {
            {
                UnifyButton(
                    text = cancelText,
                    variant = UnifyButtonVariant.TEXT,
                    onClick = { onCancel?.invoke() }
                )
            }
        } else null,
        modifier = modifier.semantics {
            contentDescription?.let { 
                this.contentDescription = it 
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    )
}

/**
 * 系统加载弹窗组件
 */
@Composable
fun UnifySystemLoading(
    title: String = "加载中",
    mask: Boolean = true,
    onCancel: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    if (mask) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .semantics {
                    contentDescription?.let { 
                        this.contentDescription = it 
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            UnifySystemLoadingContent(title = title)
        }
    } else {
        Box(
            modifier = modifier
                .semantics {
                    contentDescription?.let { 
                        this.contentDescription = it 
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            UnifySystemLoadingContent(title = title)
        }
    }
}

/**
 * 系统加载内容
 */
@Composable
private fun UnifySystemLoadingContent(title: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = LocalUnifyTheme.current.colors.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            UnifyLoading(
                variant = UnifyLoadingVariant.CIRCULAR,
                size = UnifyLoadingSize.LARGE
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            UnifyText(
                text = title,
                variant = UnifyTextVariant.BODY_MEDIUM,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 系统提示组件
 */
@Composable
fun UnifySystemToast(
    title: String,
    icon: ImageVector? = null,
    image: String? = null,
    duration: Long = 1500L,
    mask: Boolean = false,
    position: String = "center", // top, center, bottom
    onComplete: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    var visible by remember { mutableStateOf(true) }
    
    LaunchedEffect(duration) {
        kotlinx.coroutines.delay(duration)
        visible = false
        onComplete?.invoke()
    }
    
    if (visible) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .then(
                    if (mask) {
                        Modifier.background(Color.Black.copy(alpha = 0.3f))
                    } else {
                        Modifier
                    }
                )
                .semantics {
                    contentDescription?.let { 
                        this.contentDescription = it 
                    }
                },
            contentAlignment = when (position) {
                "top" -> Alignment.TopCenter
                "bottom" -> Alignment.BottomCenter
                else -> Alignment.Center
            }
        ) {
            Card(
                modifier = Modifier.padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.8f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    icon?.let { iconVector ->
                        UnifyIcon(
                            icon = iconVector,
                            size = UnifyIconSize.LARGE,
                            tint = Color.White
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    UnifyText(
                        text = title,
                        variant = UnifyTextVariant.BODY_MEDIUM,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * 系统操作菜单组件
 */
@Composable
fun UnifySystemActionSheet(
    itemList: List<String>,
    onItemClick: (index: Int, item: String) -> Unit,
    onCancel: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    var showSheet by remember { mutableStateOf(true) }
    
    if (showSheet) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable {
                    showSheet = false
                    onCancel?.invoke()
                }
                .semantics {
                    contentDescription?.let { 
                        this.contentDescription = it 
                    }
                },
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable(enabled = false) { },
                colors = CardDefaults.cardColors(
                    containerColor = LocalUnifyTheme.current.colors.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column {
                    itemList.forEachIndexed { index, item ->
                        ListItem(
                            headlineContent = {
                                UnifyText(
                                    text = item,
                                    variant = UnifyTextVariant.BODY_LARGE,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            },
                            modifier = Modifier.clickable {
                                showSheet = false
                                onItemClick(index, item)
                            }
                        )
                        
                        if (index < itemList.lastIndex) {
                            HorizontalDivider()
                        }
                    }
                    
                    HorizontalDivider(thickness = 8.dp, color = LocalUnifyTheme.current.colors.surfaceVariant)
                    
                    ListItem(
                        headlineContent = {
                            UnifyText(
                                text = "取消",
                                variant = UnifyTextVariant.BODY_LARGE,
                                textAlign = TextAlign.Center,
                                color = LocalUnifyTheme.current.colors.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        modifier = Modifier.clickable {
                            showSheet = false
                            onCancel?.invoke()
                        }
                    )
                }
            }
        }
    }
}
