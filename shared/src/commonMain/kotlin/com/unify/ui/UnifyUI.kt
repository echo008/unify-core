package com.unify.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.unify.ui.theme.UnifyTheme
import com.unify.ui.theme.UnifyColors
import com.unify.ui.theme.UnifyTypography
import com.unify.ui.theme.UnifyShapes
import com.unify.ui.theme.UnifyDimensions
import com.unify.ui.platform.UnifyPlatformTheme
import com.unify.core.platform.PlatformManager
import com.unify.core.platform.PlatformType

/**
 * Unify UI 组件库核心入口
 * 基于 KuiklyUI 设计理念，实现跨平台统一 UI 组件库
 * 
 * 核心特性：
 * - 跨平台统一设计语言
 * - 平台原生体验适配
 * - 响应式布局支持
 * - 无障碍访问支持
 * - 主题系统完整支持
 */

/**
 * 全局 Composition Local 定义
 */
val LocalUnifyTheme = staticCompositionLocalOf<UnifyTheme> {
    error("UnifyTheme not provided")
}

val LocalUnifyPlatformTheme = staticCompositionLocalOf<UnifyPlatformTheme> {
    error("UnifyPlatformTheme not provided")
}

/**
 * Unify UI 主题提供者
 * 根据当前平台自动适配相应的设计规范
 */
@Composable
fun UnifyThemeProvider(
    theme: UnifyTheme = UnifyTheme.defaultTheme(),
    platformAdaptation: Boolean = true,
    content: @Composable () -> Unit
) {
    val platformTheme = if (platformAdaptation) {
        createPlatformTheme(theme)
    } else {
        UnifyPlatformTheme.default()
    }
    
    CompositionLocalProvider(
        LocalUnifyTheme provides theme,
        LocalUnifyPlatformTheme provides platformTheme
    ) {
        content()
    }
}

/**
 * 根据当前平台创建适配的主题
 */
private fun createPlatformTheme(baseTheme: UnifyTheme): UnifyPlatformTheme {
    return when (PlatformManager.getPlatformType()) {
        PlatformType.ANDROID -> UnifyPlatformTheme.material(baseTheme)
        PlatformType.IOS -> UnifyPlatformTheme.cupertino(baseTheme)
        PlatformType.HARMONY -> UnifyPlatformTheme.harmony(baseTheme)
        PlatformType.WEB -> UnifyPlatformTheme.web(baseTheme)
        PlatformType.DESKTOP -> UnifyPlatformTheme.desktop(baseTheme)
        else -> UnifyPlatformTheme.default()
    }
}

/**
 * Unify UI 组件库版本信息
 */
object UnifyUI {
    const val VERSION = "1.0.0"
    const val BUILD_DATE = "2024-08-30"
    
    /**
     * 初始化 Unify UI 组件库
     */
    fun initialize() {
        // 初始化平台管理器
        PlatformManager.initialize()
        
        // 初始化主题系统
        initializeThemeSystem()
        
        // 初始化无障碍支持
        initializeAccessibility()
        
        // 初始化平台适配
        initializePlatformAdaptation()
    }
    
    private fun initializeThemeSystem() {
        // 主题系统初始化逻辑
    }
    
    private fun initializeAccessibility() {
        // 无障碍支持初始化
    }
    
    private fun initializePlatformAdaptation() {
        // 平台适配初始化
    }
    
    /**
     * 获取当前平台信息
     */
    fun getPlatformInfo(): PlatformInfo {
        val platformType = PlatformManager.getPlatformType()
        val deviceInfo = PlatformManager.getDeviceInfo()
        val screenInfo = PlatformManager.getScreenInfo()
        
        return PlatformInfo(
            type = platformType,
            deviceModel = deviceInfo.model,
            systemVersion = deviceInfo.systemVersion,
            screenWidth = screenInfo.width,
            screenHeight = screenInfo.height,
            density = screenInfo.density
        )
    }
}

/**
 * 平台信息数据类
 */
data class PlatformInfo(
    val type: PlatformType,
    val deviceModel: String,
    val systemVersion: String,
    val screenWidth: Int,
    val screenHeight: Int,
    val density: Float
)

/**
 * Unify UI 设计原则
 */
object UnifyDesignPrinciples {
    
    /**
     * 设计原则枚举
     */
    enum class Principle {
        CONSISTENCY,      // 一致性
        ACCESSIBILITY,    // 可访问性
        RESPONSIVENESS,   // 响应性
        PERFORMANCE,      // 性能
        PLATFORM_NATIVE,  // 平台原生
        USER_CENTERED     // 用户中心
    }
    
    /**
     * 获取设计原则描述
     */
    fun getPrincipleDescription(principle: Principle): String {
        return when (principle) {
            Principle.CONSISTENCY -> "在所有平台保持一致的用户体验"
            Principle.ACCESSIBILITY -> "支持所有用户的无障碍访问需求"
            Principle.RESPONSIVENESS -> "适配不同屏幕尺寸和设备类型"
            Principle.PERFORMANCE -> "优化性能，提供流畅的用户体验"
            Principle.PLATFORM_NATIVE -> "遵循各平台的设计规范和交互习惯"
            Principle.USER_CENTERED -> "以用户需求为中心的设计决策"
        }
    }
}

/**
 * Unify UI 组件分类
 */
object UnifyComponentCategories {
    
    /**
     * 基础组件
     */
    object Foundation {
        const val BUTTON = "Button"
        const val TEXT = "Text"
        const val IMAGE = "Image"
        const val ICON = "Icon"
        const val SURFACE = "Surface"
        const val DIVIDER = "Divider"
    }
    
    /**
     * 布局组件
     */
    object Layout {
        const val ROW = "Row"
        const val COLUMN = "Column"
        const val BOX = "Box"
        const val STACK = "Stack"
        const val GRID = "Grid"
        const val SPACER = "Spacer"
    }
    
    /**
     * 输入组件
     */
    object Input {
        const val TEXT_FIELD = "TextField"
        const val CHECKBOX = "Checkbox"
        const val RADIO = "Radio"
        const val SWITCH = "Switch"
        const val SLIDER = "Slider"
        const val DATE_PICKER = "DatePicker"
    }
    
    /**
     * 导航组件
     */
    object Navigation {
        const val NAVIGATION_BAR = "NavigationBar"
        const val TAB_BAR = "TabBar"
        const val DRAWER = "Drawer"
        const val APP_BAR = "AppBar"
        const val BREADCRUMB = "Breadcrumb"
    }
    
    /**
     * 反馈组件
     */
    object Feedback {
        const val DIALOG = "Dialog"
        const val TOAST = "Toast"
        const val LOADING = "Loading"
        const val PROGRESS = "Progress"
        const val ALERT = "Alert"
        const val SNACKBAR = "Snackbar"
    }
    
    /**
     * 列表组件
     */
    object List {
        const val LAZY_COLUMN = "LazyColumn"
        const val LAZY_ROW = "LazyRow"
        const val LAZY_GRID = "LazyGrid"
        const val RECYCLER_VIEW = "RecyclerView"
        const val PAGER = "Pager"
    }
    
    /**
     * 高级组件
     */
    object Advanced {
        const val CHART = "Chart"
        const val CALENDAR = "Calendar"
        const val PICKER = "Picker"
        const val CAMERA = "Camera"
        const val MAP = "Map"
        const val VIDEO = "Video"
    }
}
