package com.unify.ui.components.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.unify.ui.LocalUnifyTheme
import com.unify.ui.LocalUnifyPlatformTheme
import com.unify.ui.components.foundation.*
import kotlinx.coroutines.launch

/**
 * Unify Drawer 组件
 * 支持多平台适配的统一抽屉导航组件，参考 KuiklyUI 设计规范
 */

/**
 * 抽屉变体枚举
 */
enum class UnifyDrawerVariant {
    MODAL,          // 模态抽屉
    DISMISSIBLE,    // 可关闭抽屉
    PERMANENT       // 永久抽屉
}

/**
 * 抽屉尺寸枚举
 */
enum class UnifyDrawerSize {
    COMPACT,        // 紧凑尺寸 - 256dp
    STANDARD,       // 标准尺寸 - 320dp
    WIDE            // 宽尺寸 - 400dp
}

/**
 * 抽屉项数据类
 */
data class UnifyDrawerItem(
    val icon: ImageVector? = null,
    val text: String,
    val badge: String? = null,
    val enabled: Boolean = true,
    val selected: Boolean = false,
    val onClick: (() -> Unit)? = null,
    val contentDescription: String? = null
)

/**
 * 抽屉分组数据类
 */
data class UnifyDrawerGroup(
    val title: String? = null,
    val items: List<UnifyDrawerItem>
)

/**
 * 主要 Unify Drawer 组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifyDrawer(
    drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
    variant: UnifyDrawerVariant = UnifyDrawerVariant.MODAL,
    size: UnifyDrawerSize = UnifyDrawerSize.STANDARD,
    backgroundColor: Color? = null,
    contentColor: Color? = null,
    scrimColor: Color? = null,
    shape: Shape? = null,
    gesturesEnabled: Boolean = true,
    header: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
    drawerContent: @Composable () -> Unit
) {
    val theme = LocalUnifyTheme.current
    val drawerConfig = getDrawerConfig(variant, size, theme)
    val actualBackgroundColor = backgroundColor ?: drawerConfig.backgroundColor
    val actualContentColor = contentColor ?: drawerConfig.contentColor
    val actualScrimColor = scrimColor ?: Color.Black.copy(alpha = 0.32f)
    val actualShape = shape ?: drawerConfig.shape
    
    when (variant) {
        UnifyDrawerVariant.MODAL -> {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        modifier = Modifier.width(drawerConfig.width),
                        drawerShape = actualShape,
                        drawerContainerColor = actualBackgroundColor,
                        drawerContentColor = actualContentColor
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            header?.invoke()
                            drawerContent()
                        }
                    }
                },
                modifier = Modifier.fillMaxSize(),
                gesturesEnabled = gesturesEnabled,
                scrimColor = actualScrimColor,
                content = content
            )
        }
        
        UnifyDrawerVariant.DISMISSIBLE -> {
            DismissibleNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    DismissibleDrawerSheet(
                        modifier = Modifier.width(drawerConfig.width),
                        drawerShape = actualShape,
                        drawerContainerColor = actualBackgroundColor,
                        drawerContentColor = actualContentColor
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            header?.invoke()
                            drawerContent()
                        }
                    }
                },
                modifier = Modifier.fillMaxSize(),
                gesturesEnabled = gesturesEnabled,
                content = content
            )
        }
        
        UnifyDrawerVariant.PERMANENT -> {
            PermanentNavigationDrawer(
                drawerContent = {
                    PermanentDrawerSheet(
                        modifier = Modifier.width(drawerConfig.width),
                        drawerShape = actualShape,
                        drawerContainerColor = actualBackgroundColor,
                        drawerContentColor = actualContentColor
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            header?.invoke()
                            drawerContent()
                        }
                    }
                },
                modifier = Modifier.fillMaxSize(),
                content = content
            )
        }
    }
}

/**
 * 标准抽屉内容组件
 */
@Composable
fun UnifyDrawerContent(
    groups: List<UnifyDrawerGroup>,
    modifier: Modifier = Modifier,
    selectedItem: String? = null,
    onItemClick: ((UnifyDrawerItem) -> Unit)? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .semantics {
                contentDescription?.let { 
                    this.contentDescription = it 
                }
            },
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        groups.forEach { group ->
            // 分组标题
            group.title?.let { title ->
                item {
                    UnifyDrawerGroupTitle(
                        title = title,
                        modifier = Modifier.padding(
                            horizontal = 16.dp,
                            vertical = 8.dp
                        )
                    )
                }
            }
            
            // 分组项目
            items(group.items) { item ->
                UnifyDrawerItem(
                    item = item,
                    selected = item.selected || item.text == selectedItem,
                    onClick = {
                        onItemClick?.invoke(item)
                        item.onClick?.invoke()
                    }
                )
            }
            
            // 分组分隔符
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

/**
 * 抽屉项组件
 */
@Composable
fun UnifyDrawerItem(
    item: UnifyDrawerItem,
    selected: Boolean = item.selected,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalUnifyTheme.current
    val interactionSource = remember { MutableInteractionSource() }
    
    val backgroundColor = if (selected) {
        theme.colors.secondaryContainer
    } else {
        Color.Transparent
    }
    
    val contentColor = if (selected) {
        theme.colors.onSecondaryContainer
    } else {
        theme.colors.onSurface
    }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(backgroundColor)
            .clickable(
                enabled = item.enabled,
                onClick = onClick,
                indication = rememberRipple(),
                interactionSource = interactionSource,
                role = Role.Button
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 图标
        item.icon?.let { iconVector ->
            Box {
                UnifyIcon(
                    imageVector = iconVector,
                    contentDescription = null,
                    size = UnifyIconSize.SMALL,
                    tint = if (item.enabled) contentColor else theme.colors.disabled
                )
                
                // 徽章
                item.badge?.let { badgeText ->
                    UnifyBadge(
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        if (badgeText.isNotEmpty()) {
                            UnifyText(
                                text = badgeText,
                                variant = UnifyTextVariant.CAPTION,
                                color = theme.colors.onError
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
        }
        
        // 文本
        UnifyText(
            text = item.text,
            variant = UnifyTextVariant.BODY_MEDIUM,
            color = if (item.enabled) contentColor else theme.colors.disabled,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * 抽屉分组标题组件
 */
@Composable
fun UnifyDrawerGroupTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    val theme = LocalUnifyTheme.current
    
    UnifyText(
        text = title,
        variant = UnifyTextVariant.TITLE_SMALL,
        color = theme.colors.onSurfaceVariant,
        modifier = modifier
    )
}

/**
 * 抽屉头部组件
 */
@Composable
fun UnifyDrawerHeader(
    title: String,
    subtitle: String? = null,
    avatar: (@Composable () -> Unit)? = null,
    background: Color? = null,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    val actualBackground = background ?: theme.colors.primaryContainer
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(164.dp)
            .background(actualBackground)
            .semantics {
                contentDescription?.let { 
                    this.contentDescription = it 
                }
            }
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            avatar?.let { avatarContent ->
                avatarContent()
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            UnifyText(
                text = title,
                variant = UnifyTextVariant.TITLE_MEDIUM,
                color = theme.colors.onPrimaryContainer
            )
            
            subtitle?.let { subtitleText ->
                UnifyText(
                    text = subtitleText,
                    variant = UnifyTextVariant.BODY_MEDIUM,
                    color = theme.colors.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * 迷你抽屉组件
 */
@Composable
fun UnifyMiniDrawer(
    items: List<UnifyDrawerItem>,
    selectedItem: String? = null,
    onItemClick: ((UnifyDrawerItem) -> Unit)? = null,
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null,
    contentColor: Color? = null,
    header: (@Composable () -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    val actualBackgroundColor = backgroundColor ?: theme.colors.surface
    val actualContentColor = contentColor ?: theme.colors.onSurface
    
    Column(
        modifier = modifier
            .width(72.dp)
            .fillMaxHeight()
            .background(actualBackgroundColor)
            .semantics {
                contentDescription?.let { 
                    this.contentDescription = it 
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        header?.invoke()
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(items) { item ->
                UnifyMiniDrawerItem(
                    item = item,
                    selected = item.selected || item.text == selectedItem,
                    onClick = {
                        onItemClick?.invoke(item)
                        item.onClick?.invoke()
                    }
                )
            }
        }
        
        footer?.invoke()
    }
}

/**
 * 迷你抽屉项组件
 */
@Composable
private fun UnifyMiniDrawerItem(
    item: UnifyDrawerItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalUnifyTheme.current
    val interactionSource = remember { MutableInteractionSource() }
    
    val backgroundColor = if (selected) {
        theme.colors.secondaryContainer
    } else {
        Color.Transparent
    }
    
    val contentColor = if (selected) {
        theme.colors.onSecondaryContainer
    } else {
        theme.colors.onSurface
    }
    
    Column(
        modifier = modifier
            .size(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(
                enabled = item.enabled,
                onClick = onClick,
                indication = rememberRipple(),
                interactionSource = interactionSource,
                role = Role.Button
            )
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        item.icon?.let { iconVector ->
            Box {
                UnifyIcon(
                    imageVector = iconVector,
                    contentDescription = item.contentDescription,
                    size = UnifyIconSize.SMALL,
                    tint = if (item.enabled) contentColor else theme.colors.disabled
                )
                
                // 徽章
                item.badge?.let { badgeText ->
                    UnifyBadge(
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        if (badgeText.isNotEmpty()) {
                            UnifyText(
                                text = badgeText,
                                variant = UnifyTextVariant.CAPTION,
                                color = theme.colors.onError
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 自适应抽屉组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifyAdaptiveDrawer(
    groups: List<UnifyDrawerGroup>,
    selectedItem: String? = null,
    onItemClick: ((UnifyDrawerItem) -> Unit)? = null,
    modifier: Modifier = Modifier,
    drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
    header: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    BoxWithConstraints(modifier = modifier) {
        when {
            maxWidth < 600.dp -> {
                // 小屏幕使用模态抽屉
                UnifyDrawer(
                    drawerState = drawerState,
                    variant = UnifyDrawerVariant.MODAL,
                    header = header,
                    content = content,
                    drawerContent = {
                        UnifyDrawerContent(
                            groups = groups,
                            selectedItem = selectedItem,
                            onItemClick = onItemClick
                        )
                    }
                )
            }
            maxWidth < 840.dp -> {
                // 中等屏幕使用可关闭抽屉
                UnifyDrawer(
                    drawerState = drawerState,
                    variant = UnifyDrawerVariant.DISMISSIBLE,
                    header = header,
                    content = content,
                    drawerContent = {
                        UnifyDrawerContent(
                            groups = groups,
                            selectedItem = selectedItem,
                            onItemClick = onItemClick
                        )
                    }
                )
            }
            else -> {
                // 大屏幕使用永久抽屉
                UnifyDrawer(
                    variant = UnifyDrawerVariant.PERMANENT,
                    header = header,
                    content = content,
                    drawerContent = {
                        UnifyDrawerContent(
                            groups = groups,
                            selectedItem = selectedItem,
                            onItemClick = onItemClick
                        )
                    }
                )
            }
        }
    }
}

/**
 * 获取抽屉配置
 */
@Composable
private fun getDrawerConfig(
    variant: UnifyDrawerVariant,
    size: UnifyDrawerSize,
    theme: com.unify.ui.theme.UnifyTheme
): DrawerConfig {
    val width = when (size) {
        UnifyDrawerSize.COMPACT -> 256.dp
        UnifyDrawerSize.STANDARD -> 320.dp
        UnifyDrawerSize.WIDE -> 400.dp
    }
    
    val shape = when (variant) {
        UnifyDrawerVariant.MODAL -> RoundedCornerShape(
            topEnd = 16.dp,
            bottomEnd = 16.dp
        )
        UnifyDrawerVariant.DISMISSIBLE -> RoundedCornerShape(
            topEnd = 16.dp,
            bottomEnd = 16.dp
        )
        UnifyDrawerVariant.PERMANENT -> RoundedCornerShape(0.dp)
    }
    
    return DrawerConfig(
        width = width,
        backgroundColor = theme.colors.surface,
        contentColor = theme.colors.onSurface,
        shape = shape
    )
}

/**
 * 抽屉配置数据类
 */
private data class DrawerConfig(
    val width: Dp,
    val backgroundColor: Color,
    val contentColor: Color,
    val shape: Shape
)
