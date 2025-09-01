package com.unify.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.unify.core.data.UnifyDataManager
import com.unify.core.network.UnifyNetworkManager
import com.unify.core.platform.PlatformManager
import com.unify.core.theme.UnifyTheme
import com.unify.core.ui.UnifyUIManager

/**
 * Unify Core - 跨平台核心框架
 * 提供统一的初始化和配置入口
 * 实现"一套代码，多端复用"的核心目标
 */
interface UnifyCore {
    // UI管理
    val uiManager: UnifyUIManager
    
    // 数据管理
    val dataManager: UnifyDataManager
    
    // 网络管理
    val networkManager: UnifyNetworkManager
    
    // 平台适配
    val platformManager: PlatformManager
    
    /**
     * 初始化Unify框架
     */
    fun initialize()
    
    /**
     * 获取框架版本
     */
    fun getVersion(): String
    
    /**
     * 获取支持的平台列表
     */
    fun getSupportedPlatforms(): List<String>
    
    /**
     * 检查平台兼容性
     */
    fun isPlatformSupported(platform: String): Boolean
    
    /**
     * 获取当前平台配置
     */
    fun getCurrentPlatformConfig(): Map<String, Any>
}

/**
 * Unify Core 实现类
 * 使用expect/actual机制实现跨平台功能
 */
expect class UnifyCoreImpl : UnifyCore

/**
 * 全局Unify Core实例
 */
object UnifyCoreInstance {
    private var _instance: UnifyCore? = null
    
    val instance: UnifyCore
        get() = _instance ?: throw IllegalStateException("UnifyCore未初始化，请先调用initialize()")
    
    fun initialize(core: UnifyCore) {
        _instance = core
        core.initialize()
    }
    
    /**
     * 框架版本
     */
    const val VERSION = "1.0.0"
    
    /**
     * 支持的平台列表
     */
    val SUPPORTED_PLATFORMS = listOf(
        "Android", "iOS", "Web", "Desktop", "HarmonyOS", "MiniApp", "Watch", "TV"
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
        LocalUnifyCore provides UnifyCoreInstance.instance
    ) {
        UnifyTheme {
            content()
        }
    }
}

/**
 * UnifyCore的CompositionLocal
 */
val LocalUnifyCore = staticCompositionLocalOf<UnifyCore> {
    error("UnifyCore not provided")
}

/**
 * 平台管理器的CompositionLocal (保持向后兼容)
 */
val LocalPlatformManager = staticCompositionLocalOf<PlatformManager> {
    error("PlatformManager not provided")
}
