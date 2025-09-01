package com.unify.miniapp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.unify.helloworld.HelloWorldApp

/**
 * 小程序页面入口
 * 支持微信、支付宝、字节跳动等主流小程序平台
 */
class IndexPage {
    fun onLoad() {
        // 小程序生命周期
        renderCompose()
    }
    
    private fun renderCompose() {
        // 小程序Compose集成
        setComposeContent {
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
    
    private fun setComposeContent(content: @Composable () -> Unit) {
        // 小程序平台Compose渲染适配
        // 实际实现需要小程序SDK支持
        content()
    }
}
