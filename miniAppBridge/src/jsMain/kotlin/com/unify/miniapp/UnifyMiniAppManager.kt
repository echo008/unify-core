package com.unify.miniapp

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.unify.miniapp.wechat.*
import com.unify.miniapp.alipay.*
import com.unify.core.platform.MiniAppBridgeJS

/**
 * 统一小程序管理器
 * 基于Compose实现跨小程序平台的统一开发体验
 * 确保原生性能和平台一致性
 */
object UnifyMiniAppManager {
    
    private var currentPlatform: MiniAppPlatform by mutableStateOf(MiniAppPlatform.Unknown)
    
    /**
     * 初始化小程序环境
     * 自动检测当前平台并初始化相应的适配器
     */
    fun initialize() {
        currentPlatform = detectCurrentPlatform()
        MiniAppBridgeJS.initialize()
    }
    
    /**
     * 获取当前小程序平台
     */
    fun getCurrentPlatform(): MiniAppPlatform = currentPlatform
    
    /**
     * 检测当前小程序平台
     */
    private fun detectCurrentPlatform(): MiniAppPlatform {
        return when {
            js("typeof wx !== 'undefined'") as? Boolean == true -> MiniAppPlatform.WeChat
            js("typeof my !== 'undefined'") as? Boolean == true -> MiniAppPlatform.Alipay
            js("typeof tt !== 'undefined'") as? Boolean == true -> MiniAppPlatform.ByteDance
            js("typeof swan !== 'undefined'") as? Boolean == true -> MiniAppPlatform.Baidu
            js("typeof qq !== 'undefined'") as? Boolean == true -> MiniAppPlatform.QQ
            js("typeof ks !== 'undefined'") as? Boolean == true -> MiniAppPlatform.Kuaishou
            else -> MiniAppPlatform.Unknown
        }
    }
}

// MiniAppPlatform enum is defined in the main source set

/**
 * 统一小程序应用根组件
 * 根据平台自动选择相应的适配器
 */
@Composable
fun UnifyMiniAppRoot(
    content: @Composable () -> Unit
) {
    LaunchedEffect(Unit) {
        UnifyMiniAppManager.initialize()
    }
    
    when (UnifyMiniAppManager.getCurrentPlatform()) {
        MiniAppPlatform.WeChat -> {
            WeChatMiniAppRoot {
                content()
            }
        }
        MiniAppPlatform.Alipay -> {
            AlipayMiniAppRoot {
                content()
            }
        }
        else -> {
            // 默认使用微信小程序适配器
            WeChatMiniAppRoot {
                content()
            }
        }
    }
}

/**
 * 统一导航栏组件
 * 根据平台自动选择相应的样式
 */
@Composable
fun UnifyNavigationBar(
    title: String,
    showBackButton: Boolean = true,
    onBackClick: () -> Unit = {}
) {
    when (UnifyMiniAppManager.getCurrentPlatform()) {
        MiniAppPlatform.WeChat -> {
            WeChatNavigationBar(
                title = title,
                showBackButton = showBackButton,
                onBackClick = onBackClick
            )
        }
        MiniAppPlatform.Alipay -> {
            AlipayNavigationBar(
                title = title,
                showBackButton = showBackButton,
                onBackClick = onBackClick
            )
        }
        else -> {
            WeChatNavigationBar(
                title = title,
                showBackButton = showBackButton,
                onBackClick = onBackClick
            )
        }
    }
}

/**
 * 统一按钮组件
 * 根据平台自动选择相应的样式
 */
@Composable
fun UnifyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: UnifyButtonType = UnifyButtonType.Primary,
    size: UnifyButtonSize = UnifyButtonSize.Default,
    enabled: Boolean = true
) {
    when (UnifyMiniAppManager.getCurrentPlatform()) {
        MiniAppPlatform.WeChat -> {
            val wechatType = when (type) {
                UnifyButtonType.Primary -> WeChatButtonType.Primary
                UnifyButtonType.Default -> WeChatButtonType.Default
                UnifyButtonType.Warn -> WeChatButtonType.Warn
            }
            val wechatSize = when (size) {
                UnifyButtonSize.Default -> WeChatButtonSize.Default
                UnifyButtonSize.Mini -> WeChatButtonSize.Mini
            }
            WeChatButton(
                text = text,
                onClick = onClick,
                modifier = modifier,
                type = wechatType,
                size = wechatSize,
                enabled = enabled
            )
        }
        MiniAppPlatform.Alipay -> {
            val alipayType = when (type) {
                UnifyButtonType.Primary -> AlipayButtonType.Primary
                UnifyButtonType.Default -> AlipayButtonType.Default
                UnifyButtonType.Warn -> AlipayButtonType.Warn
            }
            val alipaySize = when (size) {
                UnifyButtonSize.Default -> AlipayButtonSize.Default
                UnifyButtonSize.Mini -> AlipayButtonSize.Mini
            }
            AlipayButton(
                text = text,
                onClick = onClick,
                modifier = modifier,
                type = alipayType,
                size = alipaySize,
                enabled = enabled
            )
        }
        else -> {
            val wechatType = when (type) {
                UnifyButtonType.Primary -> WeChatButtonType.Primary
                UnifyButtonType.Default -> WeChatButtonType.Default
                UnifyButtonType.Warn -> WeChatButtonType.Warn
            }
            val wechatSize = when (size) {
                UnifyButtonSize.Default -> WeChatButtonSize.Default
                UnifyButtonSize.Mini -> WeChatButtonSize.Mini
            }
            WeChatButton(
                text = text,
                onClick = onClick,
                modifier = modifier,
                type = wechatType,
                size = wechatSize,
                enabled = enabled
            )
        }
    }
}

/**
 * 统一输入框组件
 * 根据平台自动选择相应的样式
 */
@Composable
fun UnifyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    enabled: Boolean = true,
    singleLine: Boolean = true
) {
    when (UnifyMiniAppManager.getCurrentPlatform()) {
        MiniAppPlatform.WeChat -> {
            WeChatTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = modifier,
                placeholder = placeholder,
                enabled = enabled,
                singleLine = singleLine
            )
        }
        MiniAppPlatform.Alipay -> {
            AlipayTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = modifier,
                placeholder = placeholder,
                enabled = enabled,
                singleLine = singleLine
            )
        }
        else -> {
            WeChatTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = modifier,
                placeholder = placeholder,
                enabled = enabled,
                singleLine = singleLine
            )
        }
    }
}

/**
 * 统一列表组件
 * 根据平台自动选择相应的样式
 */
@Composable
fun <T> UnifyList(
    items: List<T>,
    modifier: Modifier = Modifier,
    itemContent: @Composable (T) -> Unit
) {
    when (UnifyMiniAppManager.getCurrentPlatform()) {
        MiniAppPlatform.WeChat -> {
            WeChatList(
                items = items,
                modifier = modifier,
                itemContent = itemContent
            )
        }
        MiniAppPlatform.Alipay -> {
            AlipayList(
                items = items,
                modifier = modifier,
                itemContent = itemContent
            )
        }
        else -> {
            WeChatList(
                items = items,
                modifier = modifier,
                itemContent = itemContent
            )
        }
    }
}

/**
 * 统一加载组件
 * 根据平台自动选择相应的样式
 */
@Composable
fun UnifyLoading(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    text: String = "加载中..."
) {
    when (UnifyMiniAppManager.getCurrentPlatform()) {
        MiniAppPlatform.WeChat -> {
            WeChatLoading(
                isLoading = isLoading,
                modifier = modifier
            )
        }
        MiniAppPlatform.Alipay -> {
            AlipayLoading(
                isLoading = isLoading,
                modifier = modifier,
                text = text
            )
        }
        else -> {
            WeChatLoading(
                isLoading = isLoading,
                modifier = modifier
            )
        }
    }
}

// === 统一枚举定义 ===

enum class UnifyButtonType {
    Primary, Default, Warn
}

enum class UnifyButtonSize {
    Default, Mini
}

/**
 * 统一Toast显示函数
 * 根据平台调用相应的API
 */
fun showUnifyToast(message: String) {
    when (UnifyMiniAppManager.getCurrentPlatform()) {
        MiniAppPlatform.WeChat -> {
            js("""
                if (typeof wx !== 'undefined') {
                    wx.showToast({
                        title: arguments[0],
                        icon: 'none',
                        duration: 2000
                    });
                }
            """)
        }
        MiniAppPlatform.Alipay -> {
            js("""
                if (typeof my !== 'undefined') {
                    my.showToast({
                        content: arguments[0],
                        type: 'none',
                        duration: 2000
                    });
                }
            """)
        }
        else -> {
            js("console.log('Toast: ' + arguments[0]);")
        }
    }
}

/**
 * 统一页面导航函数
 * 根据平台调用相应的API
 */
fun navigateToPage(url: String) {
    when (UnifyMiniAppManager.getCurrentPlatform()) {
        MiniAppPlatform.WeChat -> {
            js("""
                if (typeof wx !== 'undefined') {
                    wx.navigateTo({
                        url: arguments[0]
                    });
                }
            """)
        }
        MiniAppPlatform.Alipay -> {
            js("""
                if (typeof my !== 'undefined') {
                    my.navigateTo({
                        url: arguments[0]
                    });
                }
            """)
        }
        else -> {
            js("console.log('Navigate to: ' + arguments[0]);")
        }
    }
}

/**
 * 统一页面返回函数
 * 根据平台调用相应的API
 */
fun navigateBack() {
    when (UnifyMiniAppManager.getCurrentPlatform()) {
        MiniAppPlatform.WeChat -> {
            js("""
                if (typeof wx !== 'undefined') {
                    wx.navigateBack();
                }
            """)
        }
        MiniAppPlatform.Alipay -> {
            js("""
                if (typeof my !== 'undefined') {
                    my.navigateBack();
                }
            """)
        }
        else -> {
            js("console.log('Navigate back');")
        }
    }
}
