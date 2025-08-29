package com.unify.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.unit.dp
import com.unify.examples.HelloWorldScreen
import com.unify.examples.initializeHelloWorldTranslations
import com.unify.ui.theme.UnifyTheme

/**
 * 桌面端应用入口
 * 使用Compose Multiplatform for Desktop实现
 */
fun main() = application {
    // 初始化Hello World翻译
    initializeHelloWorldTranslations()
    
    Window(
        onCloseRequest = ::exitApplication,
        title = "Unify KMP Desktop",
        state = rememberWindowState(width = 800.dp, height = 600.dp)
    ) {
        UnifyTheme {
            HelloWorldScreen()
        }
    }
}
