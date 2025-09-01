package com.unify.harmony

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.unify.helloworld.HelloWorldApp

/**
 * HarmonyOS应用主Activity
 * 使用ArkUI + Compose实现跨平台UI
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
        // HarmonyOS ArkUI集成点
        // 实际实现需要HarmonyOS SDK支持
        content()
    }
}
