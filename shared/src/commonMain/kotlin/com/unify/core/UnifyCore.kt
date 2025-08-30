package com.unify.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.unify.core.platform.PlatformManager
import com.unify.core.theme.UnifyTheme

/**
 * Unify Core - 跨平台核心框架
 * 提供统一的初始化和配置入口
 */
object UnifyCore {
    
    /**
     * 初始化Unify框架
     */
    fun initialize() {
        PlatformManager.initialize()
    }
    
    /**
     * 获取框架版本
     */
    const val VERSION = "1.0.0"
    
    /**
     * 获取支持的平台列表
     */
    val SUPPORTED_PLATFORMS = listOf(
        "Android", "iOS", "Web", "Desktop", "HarmonyOS", "MiniApp"
    )
}

/**
 * Unify应用根组件
 * 提供统一的主题和平台上下文
 */
@Composable
fun UnifyApp(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalPlatformManager provides PlatformManager
    ) {
        UnifyTheme {
            content()
        }
    }
}

/**
 * 平台管理器的CompositionLocal
 */
val LocalPlatformManager = staticCompositionLocalOf<PlatformManager> {
    error("PlatformManager not provided")
}
