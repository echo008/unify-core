package com.unify.miniapp.wechat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unify.core.platform.MiniAppBridgeJS

/**
 * 微信小程序Compose适配器
 * 基于Compose实现原生性能的微信小程序UI组件
 */
@Composable
fun WeChatMiniAppRoot(
    content: @Composable () -> Unit
) {
    // 初始化微信小程序环境
    LaunchedEffect(Unit) {
        MiniAppBridgeJS.initialize()
        initWeChatAPI()
    }
    
    MaterialTheme(
        colorScheme = WeChatColorScheme
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            content()
        }
    }
}

/**
 * 微信小程序专用颜色主题
 * 遵循微信设计规范，确保视觉一致性
 */
private val WeChatColorScheme = lightColorScheme(
    primary = Color(0xFF07C160),        // 微信绿
    secondary = Color(0xFF576B95),      // 微信蓝
    background = Color(0xFFF7F7F7),     // 微信背景灰
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF191919),
    onSurface = Color(0xFF191919)
)

/**
 * 微信小程序导航栏组件
 * 高性能实现，支持自定义标题和返回按钮
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeChatNavigationBar(
    title: String,
    showBackButton: Boolean = true,
    onBackClick: () -> Unit = { navigateBack() }
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF191919)
            )
        },
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "返回",
                        tint = Color(0xFF191919)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White,
            titleContentColor = Color(0xFF191919)
        )
    )
}

/**
 * 微信小程序按钮组件
 * 原生性能实现，支持微信小程序特有的按钮样式
 */
@Composable
fun WeChatButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: WeChatButtonType = WeChatButtonType.Primary,
    size: WeChatButtonSize = WeChatButtonSize.Default,
    enabled: Boolean = true
) {
    val buttonColors = when (type) {
        WeChatButtonType.Primary -> ButtonDefaults.buttonColors(
            containerColor = Color(0xFF07C160),
            contentColor = Color.White
        )
        WeChatButtonType.Default -> ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color(0xFF191919)
        )
        WeChatButtonType.Warn -> ButtonDefaults.buttonColors(
            containerColor = Color(0xFFFA5151),
            contentColor = Color.White
        )
    }
    
    val buttonHeight = when (size) {
        WeChatButtonSize.Mini -> 30.dp
        WeChatButtonSize.Default -> 44.dp
    }
    
    Button(
        onClick = {
            // 调用微信小程序API进行用户反馈
            triggerWeChatFeedback()
            onClick()
        },
        modifier = modifier
            .fillMaxWidth()
            .height(buttonHeight),
        colors = buttonColors,
        enabled = enabled,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            fontSize = if (size == WeChatButtonSize.Mini) 13.sp else 16.sp
        )
    }
}

enum class WeChatButtonType {
    Primary, Default, Warn
}

enum class WeChatButtonSize {
    Default, Mini
}

/**
 * 微信小程序列表组件
 * 高性能LazyColumn实现，支持大数据量渲染
 */
@Composable
fun <T> WeChatList(
    items: List<T>,
    modifier: Modifier = Modifier,
    itemContent: @Composable (T) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                Box(
                    modifier = Modifier.padding(16.dp)
                ) {
                    itemContent(item)
                }
            }
        }
    }
}

/**
 * 微信小程序输入框组件
 * 原生性能实现，支持微信小程序输入特性
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeChatTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    enabled: Boolean = true,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            // 调用微信小程序输入监听API
            notifyWeChatInput(newValue)
            onValueChange(newValue)
        },
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = placeholder,
                color = Color(0xFF999999)
            )
        },
        enabled = enabled,
        singleLine = singleLine,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF07C160),
            unfocusedBorderColor = Color(0xFFE5E5E5),
            focusedTextColor = Color(0xFF191919),
            unfocusedTextColor = Color(0xFF191919)
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
    )
}

/**
 * 微信小程序加载组件
 * 高性能动画实现
 */
@Composable
fun WeChatLoading(
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    if (isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = Color(0xFF07C160),
                strokeWidth = 3.dp
            )
        }
    }
}

/**
 * 微信小程序Toast组件
 * 基于微信原生Toast API实现
 */
@Composable
fun WeChatToast(
    message: String,
    show: Boolean,
    onDismiss: () -> Unit
) {
    LaunchedEffect(show) {
        if (show) {
            showWeChatToast(message)
            kotlinx.coroutines.delay(2000)
            onDismiss()
        }
    }
}

// === 微信小程序API桥接函数 ===

/**
 * 初始化微信小程序API
 */
private fun initWeChatAPI() {
    js("""
        if (typeof wx !== 'undefined') {
            console.log('微信小程序环境已初始化');
        }
    """)
}

/**
 * 微信小程序页面导航
 */
private fun navigateBack() {
    js("""
        if (typeof wx !== 'undefined') {
            wx.navigateBack();
        }
    """)
}

/**
 * 微信小程序按钮反馈
 */
private fun triggerWeChatFeedback() {
    js("""
        if (typeof wx !== 'undefined') {
            wx.vibrateShort({
                type: 'light'
            });
        }
    """)
}

/**
 * 微信小程序输入监听
 */
private fun notifyWeChatInput(value: String) {
    js("""
        if (typeof wx !== 'undefined') {
            console.log('输入内容:', arguments[0]);
        }
    """)
}

/**
 * 微信小程序Toast显示
 */
private fun showWeChatToast(message: String) {
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
