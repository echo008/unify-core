package com.unify.watch

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.unify.helloworld.HelloWorldApp

/**
 * Watch应用主Activity
 * 支持Wear OS、watchOS、HarmonyOS穿戴设备
 */
class MainActivity {
    fun onCreate() {
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HelloWorldApp()
                }
            }
        }
    }
    
    private fun setContent(content: @Composable () -> Unit) {
        // Watch平台Compose集成点
        // 针对小屏幕优化的渲染
        content()
    }
}
