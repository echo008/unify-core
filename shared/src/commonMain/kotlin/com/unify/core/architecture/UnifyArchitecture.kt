package com.unify.core.architecture

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier

/**
 * Unify-Core 核心架构设计
 * 严格遵循 100% Compose 语法，85%+ 代码复用率
 */

/**
 * 1. UI 层 - 纯 Compose 实现
 */
@Composable
fun UnifyApp(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    UnifyTheme {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            content()
        }
    }
}

/**
 * 2. 核心主题系统
 */
@Composable
fun UnifyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme()
    } else {
        lightColorScheme()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = UnifyTypography,
        shapes = UnifyShapes,
        content = content
    )
}

/**
 * 3. 响应式布局系统
 */
@Composable
fun UnifyResponsiveLayout(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        content()
    }
}

/**
 * 4. 导航系统抽象
 */
interface UnifyNavigator {
    fun navigate(route: String)
    fun navigateBack()
    fun navigateUp(): Boolean
}

/**
 * 5. 屏幕抽象
 */
abstract class UnifyScreen {
    abstract val route: String
    
    @Composable
    abstract fun Content()
}

/**
 * 6. 平台适配器接口
 */
interface UnifyPlatformAdapter {
    val platformName: String
    val platformVersion: String
    fun getPlatformSpecificModifier(): Modifier
    fun supportsDynamicTheming(): Boolean
}

/**
 * 7. 架构层次定义
 */
object ArchitectureLayers {
    const val UI_LAYER = "UI层 - 纯Compose实现"
    const val VIEWMODEL_LAYER = "ViewModel层 - 跨平台业务逻辑" 
    const val REPOSITORY_LAYER = "Repository层 - 数据抽象"
    const val PLATFORM_LAYER = "Platform层 - 平台特定实现"
}

/**
 * 8. 代码复用率计算
 */
object CodeReuseMetrics {
    const val TARGET_REUSE_RATE = 85 // 85%+
    const val MAX_PLATFORM_SPECIFIC = 15 // <15%
    
    fun calculateReuseRate(
        sharedCodeLines: Int,
        totalCodeLines: Int
    ): Double {
        return (sharedCodeLines.toDouble() / totalCodeLines) * 100
    }
}
