package com.unify.ui.components.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import kotlinx.coroutines.delay

/**
 * Unify 导航组件
 * 对应微信小程序的 navigator、functional-page-navigator 等导航组件
 */

/**
 * 导航类型
 */
enum class UnifyNavigatorOpenType {
    NAVIGATE,           // 保留当前页面，跳转到应用内的某个页面
    REDIRECT,           // 关闭当前页面，跳转到应用内的某个页面
    SWITCH_TAB,         // 跳转到 tabBar 页面，并关闭其他所有非 tabBar 页面
    RE_LAUNCH,          // 关闭所有页面，打开到应用内的某个页面
    NAVIGATE_BACK,      // 关闭当前页面，返回上一页面或多级页面
    EXIT                // 退出小程序，target="miniProgram"时生效
}

/**
 * 导航动画类型
 */
enum class UnifyNavigatorAnimation {
    SLIDE_IN_RIGHT,     // 从右侧滑入
    SLIDE_IN_LEFT,      // 从左侧滑入
    SLIDE_IN_UP,        // 从下方滑入
    SLIDE_IN_DOWN,      // 从上方滑入
    FADE_IN,            // 淡入
    ZOOM_IN,            // 缩放进入
    NONE                // 无动画
}

/**
 * 导航器组件
 */
@Composable
fun UnifyNavigator(
    url: String,
    modifier: Modifier = Modifier,
    openType: UnifyNavigatorOpenType = UnifyNavigatorOpenType.NAVIGATE,
    delta: Int = 1,
    appId: String = "",
    path: String = "",
    extraData: Map<String, Any> = emptyMap(),
    version: String = "release", // release, trial, develop
    shortLink: String = "",
    animation: UnifyNavigatorAnimation = UnifyNavigatorAnimation.SLIDE_IN_RIGHT,
    hoverClass: String = "navigator-hover",
    hoverStopPropagation: Boolean = false,
    hoverStartTime: Long = 50L,
    hoverStayTime: Long = 600L,
    onSuccess: ((result: Map<String, Any>) -> Unit)? = null,
    onFail: ((error: String) -> Unit)? = null,
    onComplete: (() -> Unit)? = null,
    contentDescription: String? = null,
    content: @Composable () -> Unit
) {
    val theme = LocalUnifyTheme.current
    val uriHandler = LocalUriHandler.current
    var isHovered by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }
    
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isPressed -> theme.colors.primary.copy(alpha = 0.12f)
            isHovered -> theme.colors.primary.copy(alpha = 0.08f)
            else -> Color.Transparent
        },
        animationSpec = tween(
            durationMillis = if (isPressed) hoverStartTime.toInt() else hoverStayTime.toInt()
        ),
        label = "navigator_background"
    )
    
    Box(
        modifier = modifier
            .background(backgroundColor)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                handleNavigation(
                    url = url,
                    openType = openType,
                    delta = delta,
                    appId = appId,
                    path = path,
                    extraData = extraData,
                    version = version,
                    shortLink = shortLink,
                    onSuccess = onSuccess,
                    onFail = onFail,
                    onComplete = onComplete,
                    uriHandler = uriHandler
                )
            }
            .semantics {
                contentDescription?.let { 
                    this.contentDescription = it 
                }
                role = Role.Button
            }
    ) {
        content()
    }
}

/**
 * 处理导航逻辑
 */
private fun handleNavigation(
    url: String,
    openType: UnifyNavigatorOpenType,
    delta: Int,
    appId: String,
    path: String,
    extraData: Map<String, Any>,
    version: String,
    shortLink: String,
    onSuccess: ((result: Map<String, Any>) -> Unit)?,
    onFail: ((error: String) -> Unit)?,
    onComplete: (() -> Unit)?,
    uriHandler: androidx.compose.ui.platform.UriHandler
) {
    try {
        when (openType) {
            UnifyNavigatorOpenType.NAVIGATE -> {
                // 导航到新页面
                if (url.startsWith("http")) {
                    uriHandler.openUri(url)
                } else {
                    // 处理应用内导航
                    onSuccess?.invoke(mapOf("url" to url))
                }
            }
            UnifyNavigatorOpenType.REDIRECT -> {
                // 重定向到新页面
                if (url.startsWith("http")) {
                    uriHandler.openUri(url)
                } else {
                    onSuccess?.invoke(mapOf("url" to url, "redirect" to true))
                }
            }
            UnifyNavigatorOpenType.SWITCH_TAB -> {
                // 切换到标签页
                onSuccess?.invoke(mapOf("url" to url, "switchTab" to true))
            }
            UnifyNavigatorOpenType.RE_LAUNCH -> {
                // 重新启动应用
                onSuccess?.invoke(mapOf("url" to url, "reLaunch" to true))
            }
            UnifyNavigatorOpenType.NAVIGATE_BACK -> {
                // 返回上一页
                onSuccess?.invoke(mapOf("delta" to delta, "navigateBack" to true))
            }
            UnifyNavigatorOpenType.EXIT -> {
                // 退出应用
                onSuccess?.invoke(mapOf("exit" to true))
            }
        }
        onComplete?.invoke()
    } catch (e: Exception) {
        onFail?.invoke(e.message ?: "导航失败")
        onComplete?.invoke()
    }
}

/**
 * 功能页面导航器组件
 */
@Composable
fun UnifyFunctionalPageNavigator(
    name: String,
    modifier: Modifier = Modifier,
    args: Map<String, Any> = emptyMap(),
    version: String = "release",
    shortLink: String = "",
    onSuccess: ((result: Map<String, Any>) -> Unit)? = null,
    onFail: ((error: String) -> Unit)? = null,
    onCancel: (() -> Unit)? = null,
    contentDescription: String? = null,
    content: @Composable () -> Unit
) {
    val theme = LocalUnifyTheme.current
    var isPressed by remember { mutableStateOf(false) }
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isPressed) {
            theme.colors.primary.copy(alpha = 0.12f)
        } else {
            Color.Transparent
        },
        animationSpec = tween(200),
        label = "functional_navigator_background"
    )
    
    Box(
        modifier = modifier
            .background(backgroundColor)
            .clickable {
                isPressed = true
                handleFunctionalPageNavigation(
                    name = name,
                    args = args,
                    version = version,
                    shortLink = shortLink,
                    onSuccess = onSuccess,
                    onFail = onFail,
                    onCancel = onCancel
                )
            }
            .semantics {
                contentDescription?.let { 
                    this.contentDescription = it 
                }
                role = Role.Button
            }
    ) {
        content()
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(200)
            isPressed = false
        }
    }
}

/**
 * 处理功能页面导航
 */
private fun handleFunctionalPageNavigation(
    name: String,
    args: Map<String, Any>,
    version: String,
    shortLink: String,
    onSuccess: ((result: Map<String, Any>) -> Unit)?,
    onFail: ((error: String) -> Unit)?,
    onCancel: (() -> Unit)?
) {
    try {
        when (name) {
            "loginPage" -> {
                // 登录页面
                onSuccess?.invoke(mapOf("type" to "login", "args" to args))
            }
            "chooseAddress" -> {
                // 选择地址页面
                onSuccess?.invoke(mapOf("type" to "chooseAddress", "args" to args))
            }
            "chooseInvoiceTitle" -> {
                // 选择发票抬头页面
                onSuccess?.invoke(mapOf("type" to "chooseInvoiceTitle", "args" to args))
            }
            "requestPayment" -> {
                // 支付页面
                onSuccess?.invoke(mapOf("type" to "requestPayment", "args" to args))
            }
            "chooseContact" -> {
                // 选择联系人页面
                onSuccess?.invoke(mapOf("type" to "chooseContact", "args" to args))
            }
            else -> {
                onFail?.invoke("不支持的功能页面: $name")
            }
        }
    } catch (e: Exception) {
        onFail?.invoke(e.message ?: "功能页面导航失败")
    }
}

/**
 * 导航栏组件
 */
@Composable
fun UnifyNavigationBar(
    title: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = LocalUnifyTheme.current.colors.surface,
    frontColor: Color = LocalUnifyTheme.current.colors.onSurface,
    animation: UnifyNavigatorAnimation = UnifyNavigatorAnimation.SLIDE_IN_RIGHT,
    loading: Boolean = false,
    showBackButton: Boolean = true,
    showHomeButton: Boolean = false,
    showMenuButton: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    onHomeClick: (() -> Unit)? = null,
    onMenuClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .semantics {
                contentDescription?.let { 
                    this.contentDescription = it 
                }
            },
        color = backgroundColor,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧按钮组
            Row {
                if (showBackButton) {
                    IconButton(
                        onClick = { onBackClick?.invoke() }
                    ) {
                        UnifyIcon(
                            icon = Icons.Default.ArrowBack,
                            size = UnifyIconSize.MEDIUM,
                            tint = frontColor
                        )
                    }
                }
                
                if (showHomeButton) {
                    IconButton(
                        onClick = { onHomeClick?.invoke() }
                    ) {
                        UnifyIcon(
                            icon = Icons.Default.Home,
                            size = UnifyIconSize.MEDIUM,
                            tint = frontColor
                        )
                    }
                }
            }
            
            // 标题
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UnifyText(
                        text = title,
                        variant = UnifyTextVariant.TITLE_MEDIUM,
                        color = frontColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (loading) {
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
                actions()
                
                if (showMenuButton) {
                    IconButton(
                        onClick = { onMenuClick?.invoke() }
                    ) {
                        UnifyIcon(
                            icon = Icons.Default.MoreVert,
                            size = UnifyIconSize.MEDIUM,
                            tint = frontColor
                        )
                    }
                }
            }
        }
    }
}

/**
 * 页面元数据组件
 */
@Composable
fun UnifyPageMeta(
    backgroundColor: Color = LocalUnifyTheme.current.colors.background,
    textSizeAdjust: String = "auto",
    backgroundColorTop: Color = LocalUnifyTheme.current.colors.background,
    backgroundColorBottom: Color = LocalUnifyTheme.current.colors.background,
    scrollTop: Dp = 0.dp,
    scrollDuration: Long = 300L,
    pageStyle: String = "",
    rootFontSize: String = "",
    rootBackgroundColor: Color = LocalUnifyTheme.current.colors.background,
    pageOrientation: String = "auto", // auto, portrait, landscape
    onResize: ((size: androidx.compose.ui.geometry.Size) -> Unit)? = null,
    onScroll: ((scrollTop: Dp) -> Unit)? = null,
    onReachBottom: (() -> Unit)? = null,
    onPullDownRefresh: (() -> Unit)? = null
) {
    // 页面元数据设置，影响整个页面的样式和行为
    // 在实际实现中，这些设置会影响页面的全局样式
    
    LaunchedEffect(backgroundColor, textSizeAdjust, backgroundColorTop, backgroundColorBottom) {
        // 应用页面样式设置
        // 这里可以通过平台特定的实现来设置页面样式
    }
    
    LaunchedEffect(scrollTop) {
        if (scrollTop > 0.dp) {
            // 滚动到指定位置
            onScroll?.invoke(scrollTop)
        }
    }
}

/**
 * 标签栏组件
 */
@Composable
fun UnifyTabBar(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = LocalUnifyTheme.current.colors.surface,
    borderStyle: String = "solid",
    color: Color = LocalUnifyTheme.current.colors.onSurface.copy(alpha = 0.6f),
    selectedColor: Color = LocalUnifyTheme.current.colors.primary,
    position: String = "bottom", // bottom, top
    custom: Boolean = false,
    tabs: List<UnifyTabBarItem>,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .semantics {
                contentDescription?.let { 
                    this.contentDescription = it 
                }
            },
        color = backgroundColor,
        shadowElevation = if (position == "bottom") 8.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            tabs.forEachIndexed { index, tab ->
                val isSelected = index == selectedIndex
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onTabSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        UnifyIcon(
                            icon = if (isSelected) tab.selectedIconPath else tab.iconPath,
                            size = UnifyIconSize.MEDIUM,
                            tint = if (isSelected) selectedColor else color
                        )
                        
                        Spacer(modifier = Modifier.height(2.dp))
                        
                        UnifyText(
                            text = tab.text,
                            variant = UnifyTextVariant.CAPTION,
                            color = if (isSelected) selectedColor else color,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        if (tab.dot) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(
                                        color = theme.colors.error,
                                        shape = RoundedCornerShape(3.dp)
                                    )
                            )
                        }
                        
                        if (tab.badge.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = theme.colors.error,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                UnifyText(
                                    text = tab.badge,
                                    variant = UnifyTextVariant.CAPTION,
                                    color = Color.White,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 标签栏项目
 */
data class UnifyTabBarItem(
    val pagePath: String,
    val text: String,
    val iconPath: ImageVector,
    val selectedIconPath: ImageVector,
    val dot: Boolean = false,
    val badge: String = ""
)

/**
 * 面包屑导航组件
 */
@Composable
fun UnifyBreadcrumb(
    items: List<UnifyBreadcrumbItem>,
    modifier: Modifier = Modifier,
    separator: String = "/",
    separatorColor: Color = LocalUnifyTheme.current.colors.onSurface.copy(alpha = 0.6f),
    activeColor: Color = LocalUnifyTheme.current.colors.primary,
    inactiveColor: Color = LocalUnifyTheme.current.colors.onSurface.copy(alpha = 0.6f),
    onItemClick: ((item: UnifyBreadcrumbItem) -> Unit)? = null,
    contentDescription: String? = null
) {
    Row(
        modifier = modifier
            .semantics {
                contentDescription?.let { 
                    this.contentDescription = it 
                }
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, item ->
            if (index > 0) {
                UnifyText(
                    text = separator,
                    color = separatorColor,
                    variant = UnifyTextVariant.BODY_MEDIUM,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
            
            val isLast = index == items.lastIndex
            val textColor = if (isLast) activeColor else inactiveColor
            
            if (item.clickable && !isLast) {
                UnifyText(
                    text = item.text,
                    color = textColor,
                    variant = UnifyTextVariant.BODY_MEDIUM,
                    modifier = Modifier.clickable {
                        onItemClick?.invoke(item)
                    }
                )
            } else {
                UnifyText(
                    text = item.text,
                    color = textColor,
                    variant = UnifyTextVariant.BODY_MEDIUM,
                    fontWeight = if (isLast) FontWeight.Medium else FontWeight.Normal
                )
            }
        }
    }
}

/**
 * 面包屑项目
 */
data class UnifyBreadcrumbItem(
    val text: String,
    val path: String = "",
    val clickable: Boolean = true
)

/**
 * 侧边导航组件
 */
@Composable
fun UnifySideNavigation(
    items: List<UnifySideNavigationItem>,
    selectedItem: String,
    onItemSelected: (UnifySideNavigationItem) -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = LocalUnifyTheme.current.colors.surface,
    selectedBackgroundColor: Color = LocalUnifyTheme.current.colors.primary.copy(alpha = 0.12f),
    textColor: Color = LocalUnifyTheme.current.colors.onSurface,
    selectedTextColor: Color = LocalUnifyTheme.current.colors.primary,
    collapsible: Boolean = false,
    collapsed: Boolean = false,
    onCollapseToggle: (() -> Unit)? = null,
    contentDescription: String? = null
) {
    Surface(
        modifier = modifier
            .fillMaxHeight()
            .width(if (collapsed) 72.dp else 240.dp)
            .semantics {
                contentDescription?.let { 
                    this.contentDescription = it 
                }
            },
        color = backgroundColor,
        shadowElevation = 2.dp
    ) {
        Column {
            // 折叠按钮
            if (collapsible) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clickable { onCollapseToggle?.invoke() },
                    contentAlignment = Alignment.Center
                ) {
                    UnifyIcon(
                        icon = if (collapsed) Icons.Default.MenuOpen else Icons.Default.Menu,
                        size = UnifyIconSize.MEDIUM,
                        tint = textColor
                    )
                }
                
                HorizontalDivider()
            }
            
            // 导航项目
            LazyColumn {
                items(items) { item ->
                    val isSelected = item.id == selectedItem
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isSelected) selectedBackgroundColor else Color.Transparent
                            )
                            .clickable { onItemSelected(item) }
                            .padding(16.dp)
                    ) {
                        if (collapsed) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                UnifyIcon(
                                    icon = item.icon,
                                    size = UnifyIconSize.MEDIUM,
                                    tint = if (isSelected) selectedTextColor else textColor
                                )
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                UnifyIcon(
                                    icon = item.icon,
                                    size = UnifyIconSize.MEDIUM,
                                    tint = if (isSelected) selectedTextColor else textColor
                                )
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                UnifyText(
                                    text = item.text,
                                    color = if (isSelected) selectedTextColor else textColor,
                                    variant = UnifyTextVariant.BODY_MEDIUM,
                                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                                )
                                
                                if (item.badge.isNotEmpty()) {
                                    Spacer(modifier = Modifier.weight(1f))
                                    
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = LocalUnifyTheme.current.colors.error,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        UnifyText(
                                            text = item.badge,
                                            variant = UnifyTextVariant.CAPTION,
                                            color = Color.White,
                                            fontSize = 10.sp
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
}

/**
 * 侧边导航项目
 */
data class UnifySideNavigationItem(
    val id: String,
    val text: String,
    val icon: ImageVector,
    val path: String = "",
    val badge: String = ""
)
