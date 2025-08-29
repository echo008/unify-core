package com.unify.web

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.unify.helloworld.HelloWorldApp
import com.unify.helloworld.PlatformInfo

/**
 * Web应用入口点
 * 使用共享的HelloWorldApp组件
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
        HelloWorldApp(platformName = PlatformInfo.getPlatformName())
    }
}
