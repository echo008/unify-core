package com.unify.core.providers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.unify.core.UnifyCore
import com.unify.core.data.UnifyDataManager
import com.unify.core.ui.UnifyUIManager
import com.unify.network.UnifyNetworkManager
import com.unify.device.UnifyDeviceManager
import com.unify.core.performance.UnifyPerformanceMonitor
import com.unify.core.security.UnifySecurityManager
import com.unify.core.platform.PlatformManager

/**
 * Unify框架的Composition Local提供器
 * 用于在Compose组件树中提供全局访问的核心服务
 */

/**
 * 核心管理器CompositionLocal
 */
val LocalUnifyCore = staticCompositionLocalOf<UnifyCore> {
    error("UnifyCore not provided")
}

val LocalUnifyDataManager = staticCompositionLocalOf<UnifyDataManager> {
    error("UnifyDataManager not provided")
}

val LocalUnifyUIManager = staticCompositionLocalOf<UnifyUIManager> {
    error("UnifyUIManager not provided")
}

val LocalUnifyNetworkManager = staticCompositionLocalOf<UnifyNetworkManager> {
    error("UnifyNetworkManager not provided")
}

val LocalUnifyDeviceManager = staticCompositionLocalOf<UnifyDeviceManager> {
    error("UnifyDeviceManager not provided")
}

val LocalUnifyPerformanceMonitor = staticCompositionLocalOf<UnifyPerformanceMonitor> {
    error("UnifyPerformanceMonitor not provided")
}

val LocalUnifySecurityManager = staticCompositionLocalOf<UnifySecurityManager> {
    error("UnifySecurityManager not provided")
}

val LocalPlatformManager = staticCompositionLocalOf<PlatformManager> {
    error("PlatformManager not provided")
}

/**
 * 主题和配置CompositionLocal
 */
val LocalUnifyTheme = staticCompositionLocalOf<UnifyTheme> {
    UnifyTheme.Default
}

val LocalUnifyConfiguration = staticCompositionLocalOf<UnifyConfiguration> {
    UnifyConfiguration.Default
}

/**
 * 平台特定CompositionLocal
 */
val LocalPlatformInfo = staticCompositionLocalOf<PlatformInfo> {
    error("PlatformInfo not provided")
}

val LocalPlatformCapabilities = staticCompositionLocalOf<PlatformCapabilities> {
    PlatformCapabilities.Default
}

/**
 * Unify主题配置
 */
data class UnifyTheme(
    val colorScheme: UnifyColorScheme = UnifyColorScheme.Default,
    val typography: UnifyTypography = UnifyTypography.Default,
    val shapes: UnifyShapes = UnifyShapes.Default,
    val spacing: UnifySpacing = UnifySpacing.Default,
    val elevation: UnifyElevation = UnifyElevation.Default
) {
    companion object {
        val Default = UnifyTheme()
        
        val Light = UnifyTheme(
            colorScheme = UnifyColorScheme.Light
        )
        
        val Dark = UnifyTheme(
            colorScheme = UnifyColorScheme.Dark
        )
    }
}

/**
 * Unify颜色方案
 */
data class UnifyColorScheme(
    val primary: Long = 0xFF6200EE,
    val primaryVariant: Long = 0xFF3700B3,
    val secondary: Long = 0xFF03DAC6,
    val secondaryVariant: Long = 0xFF018786,
    val background: Long = 0xFFFFFFFF,
    val surface: Long = 0xFFFFFFFF,
    val error: Long = 0xFFB00020,
    val onPrimary: Long = 0xFFFFFFFF,
    val onSecondary: Long = 0xFF000000,
    val onBackground: Long = 0xFF000000,
    val onSurface: Long = 0xFF000000,
    val onError: Long = 0xFFFFFFFF
) {
    companion object {
        val Default = UnifyColorScheme()
        
        val Light = UnifyColorScheme(
            primary = 0xFF6200EE,
            background = 0xFFFFFFFF,
            surface = 0xFFFFFFFF,
            onBackground = 0xFF000000,
            onSurface = 0xFF000000
        )
        
        val Dark = UnifyColorScheme(
            primary = 0xFFBB86FC,
            background = 0xFF121212,
            surface = 0xFF121212,
            onBackground = 0xFFFFFFFF,
            onSurface = 0xFFFFFFFF
        )
    }
}

/**
 * Unify字体排版
 */
data class UnifyTypography(
    val h1: UnifyTextStyle = UnifyTextStyle.H1,
    val h2: UnifyTextStyle = UnifyTextStyle.H2,
    val h3: UnifyTextStyle = UnifyTextStyle.H3,
    val h4: UnifyTextStyle = UnifyTextStyle.H4,
    val h5: UnifyTextStyle = UnifyTextStyle.H5,
    val h6: UnifyTextStyle = UnifyTextStyle.H6,
    val subtitle1: UnifyTextStyle = UnifyTextStyle.Subtitle1,
    val subtitle2: UnifyTextStyle = UnifyTextStyle.Subtitle2,
    val body1: UnifyTextStyle = UnifyTextStyle.Body1,
    val body2: UnifyTextStyle = UnifyTextStyle.Body2,
    val button: UnifyTextStyle = UnifyTextStyle.Button,
    val caption: UnifyTextStyle = UnifyTextStyle.Caption,
    val overline: UnifyTextStyle = UnifyTextStyle.Overline
) {
    companion object {
        val Default = UnifyTypography()
    }
}

/**
 * Unify文本样式
 */
data class UnifyTextStyle(
    val fontSize: Float,
    val lineHeight: Float,
    val fontWeight: Int = 400,
    val letterSpacing: Float = 0f
) {
    companion object {
        val H1 = UnifyTextStyle(fontSize = 96f, lineHeight = 112f, fontWeight = 300, letterSpacing = -1.5f)
        val H2 = UnifyTextStyle(fontSize = 60f, lineHeight = 72f, fontWeight = 300, letterSpacing = -0.5f)
        val H3 = UnifyTextStyle(fontSize = 48f, lineHeight = 56f, fontWeight = 400)
        val H4 = UnifyTextStyle(fontSize = 34f, lineHeight = 42f, fontWeight = 400, letterSpacing = 0.25f)
        val H5 = UnifyTextStyle(fontSize = 24f, lineHeight = 32f, fontWeight = 400)
        val H6 = UnifyTextStyle(fontSize = 20f, lineHeight = 32f, fontWeight = 500, letterSpacing = 0.15f)
        val Subtitle1 = UnifyTextStyle(fontSize = 16f, lineHeight = 24f, fontWeight = 400, letterSpacing = 0.15f)
        val Subtitle2 = UnifyTextStyle(fontSize = 14f, lineHeight = 24f, fontWeight = 500, letterSpacing = 0.1f)
        val Body1 = UnifyTextStyle(fontSize = 16f, lineHeight = 24f, fontWeight = 400, letterSpacing = 0.5f)
        val Body2 = UnifyTextStyle(fontSize = 14f, lineHeight = 20f, fontWeight = 400, letterSpacing = 0.25f)
        val Button = UnifyTextStyle(fontSize = 14f, lineHeight = 16f, fontWeight = 500, letterSpacing = 1.25f)
        val Caption = UnifyTextStyle(fontSize = 12f, lineHeight = 16f, fontWeight = 400, letterSpacing = 0.4f)
        val Overline = UnifyTextStyle(fontSize = 10f, lineHeight = 16f, fontWeight = 400, letterSpacing = 1.5f)
    }
}

/**
 * Unify形状配置
 */
data class UnifyShapes(
    val small: Float = 4f,
    val medium: Float = 8f,
    val large: Float = 16f
) {
    companion object {
        val Default = UnifyShapes()
    }
}

/**
 * Unify间距配置
 */
data class UnifySpacing(
    val xs: Float = 4f,
    val sm: Float = 8f,
    val md: Float = 16f,
    val lg: Float = 24f,
    val xl: Float = 32f,
    val xxl: Float = 48f
) {
    companion object {
        val Default = UnifySpacing()
    }
}

/**
 * Unify阴影配置
 */
data class UnifyElevation(
    val none: Float = 0f,
    val small: Float = 2f,
    val medium: Float = 4f,
    val large: Float = 8f,
    val xlarge: Float = 16f
) {
    companion object {
        val Default = UnifyElevation()
    }
}

/**
 * Unify配置
 */
data class UnifyConfiguration(
    val enableDebugMode: Boolean = false,
    val enablePerformanceMonitoring: Boolean = true,
    val enableErrorReporting: Boolean = true,
    val enableAnalytics: Boolean = false,
    val maxCacheSize: Long = 100 * 1024 * 1024, // 100MB
    val networkTimeout: Long = 30000, // 30秒
    val retryCount: Int = 3
) {
    companion object {
        val Default = UnifyConfiguration()
        
        val Debug = UnifyConfiguration(
            enableDebugMode = true,
            enablePerformanceMonitoring = true,
            enableErrorReporting = true
        )
        
        val Production = UnifyConfiguration(
            enableDebugMode = false,
            enablePerformanceMonitoring = true,
            enableErrorReporting = true,
            enableAnalytics = true
        )
    }
}

/**
 * 平台信息
 */
data class PlatformInfo(
    val name: String,
    val version: String,
    val type: PlatformType,
    val capabilities: PlatformCapabilities
)

/**
 * 平台类型
 */
enum class PlatformType {
    ANDROID,
    IOS,
    WEB,
    DESKTOP,
    HARMONY_OS,
    MINI_APP,
    WATCH,
    TV
}

/**
 * 平台能力
 */
data class PlatformCapabilities(
    val supportsTouchInput: Boolean = true,
    val supportsKeyboardInput: Boolean = true,
    val supportsMouseInput: Boolean = false,
    val supportsCamera: Boolean = true,
    val supportsMicrophone: Boolean = true,
    val supportsLocation: Boolean = true,
    val supportsBluetooth: Boolean = false,
    val supportsNFC: Boolean = false,
    val supportsBiometric: Boolean = false,
    val supportsNotifications: Boolean = true,
    val supportsVibration: Boolean = true,
    val supportsFileSystem: Boolean = true,
    val supportsNetworking: Boolean = true
) {
    companion object {
        val Default = PlatformCapabilities()
    }
}

/**
 * Unify提供器组件
 * 为整个应用提供Unify框架的核心服务
 */
@Composable
fun UnifyProvider(
    core: UnifyCore,
    theme: UnifyTheme = UnifyTheme.Default,
    configuration: UnifyConfiguration = UnifyConfiguration.Default,
    platformInfo: PlatformInfo,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalUnifyCore provides core,
        LocalUnifyDataManager provides core.dataManager,
        LocalUnifyUIManager provides core.uiManager,
        LocalUnifyNetworkManager provides core.networkManager,
        LocalUnifyDeviceManager provides core.deviceManager,
        LocalUnifyPerformanceMonitor provides core.performanceMonitor,
        LocalUnifySecurityManager provides core.securityManager,
        LocalPlatformManager provides core.platformManager,
        LocalUnifyTheme provides theme,
        LocalUnifyConfiguration provides configuration,
        LocalPlatformInfo provides platformInfo,
        LocalPlatformCapabilities provides platformInfo.capabilities
    ) {
        content()
    }
}

/**
 * 获取当前Unify核心实例
 */
@Composable
fun currentUnifyCore(): UnifyCore = LocalUnifyCore.current

/**
 * 获取当前数据管理器
 */
@Composable
fun currentDataManager(): UnifyDataManager = LocalUnifyDataManager.current

/**
 * 获取当前UI管理器
 */
@Composable
fun currentUIManager(): UnifyUIManager = LocalUnifyUIManager.current

/**
 * 获取当前网络管理器
 */
@Composable
fun currentNetworkManager(): UnifyNetworkManager = LocalUnifyNetworkManager.current

/**
 * 获取当前设备管理器
 */
@Composable
fun currentDeviceManager(): UnifyDeviceManager = LocalUnifyDeviceManager.current

/**
 * 获取当前性能监控器
 */
@Composable
fun currentPerformanceMonitor(): UnifyPerformanceMonitor = LocalUnifyPerformanceMonitor.current

/**
 * 获取当前安全管理器
 */
@Composable
fun currentSecurityManager(): UnifySecurityManager = LocalUnifySecurityManager.current

/**
 * 获取当前平台管理器
 */
@Composable
fun currentPlatformManager(): PlatformManager = LocalPlatformManager.current

/**
 * 获取当前主题
 */
@Composable
fun currentUnifyTheme(): UnifyTheme = LocalUnifyTheme.current

/**
 * 获取当前配置
 */
@Composable
fun currentUnifyConfiguration(): UnifyConfiguration = LocalUnifyConfiguration.current

/**
 * 获取当前平台信息
 */
@Composable
fun currentPlatformInfo(): PlatformInfo = LocalPlatformInfo.current

/**
 * 获取当前平台能力
 */
@Composable
fun currentPlatformCapabilities(): PlatformCapabilities = LocalPlatformCapabilities.current
