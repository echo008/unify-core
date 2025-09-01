package com.unify.ui.components.platform

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * 平台特定UI组件的通用接口
 * 使用expect/actual机制为不同平台提供定制化实现
 */

/**
 * 平台特定按钮组件
 * 每个平台可以有自己的样式和交互方式
 */
@Composable
expect fun PlatformSpecificButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)

/**
 * 平台特定卡片组件
 * 适配不同平台的设计语言
 */
@Composable
expect fun PlatformSpecificCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
)

/**
 * 平台特定输入框组件
 * 处理不同平台的输入法和键盘行为
 */
@Composable
expect fun PlatformSpecificTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "",
    placeholder: String = "",
    modifier: Modifier = Modifier
)

/**
 * 平台特定列表组件
 * 优化不同平台的滚动和性能表现
 */
@Composable
expect fun PlatformSpecificList(
    items: List<String>,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
)

/**
 * 平台特定对话框组件
 * 遵循各平台的对话框设计规范
 */
@Composable
expect fun PlatformSpecificDialog(
    title: String,
    content: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
)

/**
 * 平台特定导航栏组件
 * 适配不同平台的导航模式
 */
@Composable
expect fun PlatformSpecificNavigationBar(
    items: List<NavigationItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
)

/**
 * 导航项数据类
 */
data class NavigationItem(
    val title: String,
    val icon: String? = null,
    val badge: String? = null
)

/**
 * 平台特定加载指示器
 * 使用各平台原生的加载动画
 */
@Composable
expect fun PlatformSpecificLoadingIndicator(
    isLoading: Boolean,
    modifier: Modifier = Modifier
)

/**
 * 平台特定开关组件
 * 遵循各平台的开关设计
 */
@Composable
expect fun PlatformSpecificSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
)

/**
 * 平台特定滑块组件
 * 适配不同平台的滑块交互
 */
@Composable
expect fun PlatformSpecificSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    modifier: Modifier = Modifier
)

/**
 * 平台特定图片组件
 * 优化不同平台的图片加载和缓存
 */
@Composable
expect fun PlatformSpecificImage(
    url: String,
    contentDescription: String? = null,
    modifier: Modifier = Modifier
)
