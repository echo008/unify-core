package com.unify.web

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.unify.examples.HelloWorldScreen
import com.unify.examples.initializeHelloWorldTranslations

/**
 * Web应用入口点
 * 使用Compose Multiplatform for Web实现
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    // 初始化Hello World翻译
    initializeHelloWorldTranslations()
    
    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
        HelloWorldScreen()
    }
}
