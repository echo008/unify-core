package com.unify.desktop

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.unify.helloworld.HelloWorldApp

/**
 * 桌面端应用入口
 * 使用shared模块的HelloWorldApp
 */
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Unify KMP Desktop",
        state = rememberWindowState(width = 800.dp, height = 600.dp)
    ) {
        MaterialTheme {
            HelloWorldApp()
        }
    }
}
