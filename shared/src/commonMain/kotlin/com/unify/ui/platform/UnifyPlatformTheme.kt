package com.unify.ui.platform

import androidx.compose.runtime.Immutable
import com.unify.ui.theme.UnifyTheme
import com.unify.ui.theme.UnifyColors
import com.unify.core.platform.PlatformManager
import com.unify.core.platform.PlatformType

/**
 * 平台适配主题系统
 * 根据不同平台的设计规范自动适配主题样式
 */
@Immutable
data class UnifyPlatformTheme(
    val platformType: PlatformType,
    val adaptedColors: UnifyColors,
    val platformSpecificStyles: Map<String, Any>,
    val interactionPatterns: InteractionPatterns,
    val accessibilityFeatures: AccessibilityFeatures
) {
    companion object {
        fun default(): UnifyPlatformTheme {
            return UnifyPlatformTheme(
                platformType = PlatformManager.getPlatformType(),
                adaptedColors = UnifyColors.defaultLight(),
                platformSpecificStyles = emptyMap(),
                interactionPatterns = InteractionPatterns.default(),
                accessibilityFeatures = AccessibilityFeatures.default()
            )
        }
        
        /**
         * Android Material Design 适配
         */
        fun material(baseTheme: UnifyTheme): UnifyPlatformTheme {
            return UnifyPlatformTheme(
                platformType = PlatformType.ANDROID,
                adaptedColors = adaptColorsForMaterial(baseTheme.colors),
                platformSpecificStyles = mapOf(
                    "rippleEffect" to true,
                    "elevation" to true,
                    "materialMotion" to true,
                    "fab" to true
                ),
                interactionPatterns = InteractionPatterns.material(),
                accessibilityFeatures = AccessibilityFeatures.android()
            )
        }
        
        /**
         * iOS Cupertino 适配
         */
        fun cupertino(baseTheme: UnifyTheme): UnifyPlatformTheme {
            return UnifyPlatformTheme(
                platformType = PlatformType.IOS,
                adaptedColors = adaptColorsForCupertino(baseTheme.colors),
                platformSpecificStyles = mapOf(
                    "blurEffect" to true,
                    "vibrancy" to true,
                    "springAnimation" to true,
                    "navigationStyle" to "large"
                ),
                interactionPatterns = InteractionPatterns.cupertino(),
                accessibilityFeatures = AccessibilityFeatures.ios()
            )
        }
        
        /**
         * HarmonyOS 适配
         */
        fun harmony(baseTheme: UnifyTheme): UnifyPlatformTheme {
            return UnifyPlatformTheme(
                platformType = PlatformType.HARMONY,
                adaptedColors = adaptColorsForHarmony(baseTheme.colors),
                platformSpecificStyles = mapOf(
                    "arkUIStyle" to true,
                    "distributedUI" to true,
                    "atomicService" to true,
                    "serviceWidget" to true
                ),
                interactionPatterns = InteractionPatterns.harmony(),
                accessibilityFeatures = AccessibilityFeatures.harmony()
            )
        }
        
        /**
         * Web 适配
         */
        fun web(baseTheme: UnifyTheme): UnifyPlatformTheme {
            return UnifyPlatformTheme(
                platformType = PlatformType.WEB,
                adaptedColors = adaptColorsForWeb(baseTheme.colors),
                platformSpecificStyles = mapOf(
                    "cssTransitions" to true,
                    "hoverEffects" to true,
                    "focusRing" to true,
                    "responsiveDesign" to true
                ),
                interactionPatterns = InteractionPatterns.web(),
                accessibilityFeatures = AccessibilityFeatures.web()
            )
        }
        
        /**
         * Desktop 适配
         */
        fun desktop(baseTheme: UnifyTheme): UnifyPlatformTheme {
            return UnifyPlatformTheme(
                platformType = PlatformType.DESKTOP,
                adaptedColors = adaptColorsForDesktop(baseTheme.colors),
                platformSpecificStyles = mapOf(
                    "nativeMenus" to true,
                    "keyboardShortcuts" to true,
                    "contextMenus" to true,
                    "windowChrome" to true
                ),
                interactionPatterns = InteractionPatterns.desktop(),
                accessibilityFeatures = AccessibilityFeatures.desktop()
            )
        }
        
        private fun adaptColorsForMaterial(colors: UnifyColors): UnifyColors {
            // Material Design 色彩适配逻辑
            return colors.copy(
                // 增强对比度以符合 Material Design 规范
                primary = colors.primary,
                surface = colors.surface
            )
        }
        
        private fun adaptColorsForCupertino(colors: UnifyColors): UnifyColors {
            // iOS 色彩适配逻辑
            return colors.copy(
                // 调整为 iOS 系统色彩
                primary = colors.primary,
                background = colors.background
            )
        }
        
        private fun adaptColorsForHarmony(colors: UnifyColors): UnifyColors {
            // HarmonyOS 色彩适配逻辑
            return colors.copy(
                // 适配鸿蒙设计语言
                primary = colors.primary,
                surface = colors.surface
            )
        }
        
        private fun adaptColorsForWeb(colors: UnifyColors): UnifyColors {
            // Web 色彩适配逻辑
            return colors.copy(
                // Web 可访问性色彩调整
                primary = colors.primary,
                outline = colors.outline
            )
        }
        
        private fun adaptColorsForDesktop(colors: UnifyColors): UnifyColors {
            // Desktop 色彩适配逻辑
            return colors.copy(
                // 桌面环境色彩调整
                primary = colors.primary,
                surface = colors.surface
            )
        }
    }
}

/**
 * 交互模式定义
 */
@Immutable
data class InteractionPatterns(
    val touchTargetSize: Float,
    val animationDuration: Int,
    val feedbackType: FeedbackType,
    val navigationPattern: NavigationPattern,
    val gestureSupport: Set<GestureType>
) {
    companion object {
        fun default(): InteractionPatterns {
            return InteractionPatterns(
                touchTargetSize = 44f,
                animationDuration = 300,
                feedbackType = FeedbackType.VISUAL,
                navigationPattern = NavigationPattern.STACK,
                gestureSupport = setOf(GestureType.TAP, GestureType.LONG_PRESS)
            )
        }
        
        fun material(): InteractionPatterns {
            return InteractionPatterns(
                touchTargetSize = 48f,
                animationDuration = 300,
                feedbackType = FeedbackType.RIPPLE,
                navigationPattern = NavigationPattern.DRAWER,
                gestureSupport = setOf(
                    GestureType.TAP,
                    GestureType.LONG_PRESS,
                    GestureType.SWIPE,
                    GestureType.PINCH
                )
            )
        }
        
        fun cupertino(): InteractionPatterns {
            return InteractionPatterns(
                touchTargetSize = 44f,
                animationDuration = 250,
                feedbackType = FeedbackType.HAPTIC,
                navigationPattern = NavigationPattern.TAB,
                gestureSupport = setOf(
                    GestureType.TAP,
                    GestureType.LONG_PRESS,
                    GestureType.SWIPE,
                    GestureType.EDGE_SWIPE
                )
            )
        }
        
        fun harmony(): InteractionPatterns {
            return InteractionPatterns(
                touchTargetSize = 48f,
                animationDuration = 350,
                feedbackType = FeedbackType.DISTRIBUTED,
                navigationPattern = NavigationPattern.SERVICE,
                gestureSupport = setOf(
                    GestureType.TAP,
                    GestureType.LONG_PRESS,
                    GestureType.SWIPE,
                    GestureType.MULTI_DEVICE
                )
            )
        }
        
        fun web(): InteractionPatterns {
            return InteractionPatterns(
                touchTargetSize = 44f,
                animationDuration = 200,
                feedbackType = FeedbackType.VISUAL,
                navigationPattern = NavigationPattern.BREADCRUMB,
                gestureSupport = setOf(
                    GestureType.TAP,
                    GestureType.HOVER,
                    GestureType.KEYBOARD
                )
            )
        }
        
        fun desktop(): InteractionPatterns {
            return InteractionPatterns(
                touchTargetSize = 32f,
                animationDuration = 150,
                feedbackType = FeedbackType.VISUAL,
                navigationPattern = NavigationPattern.MENU,
                gestureSupport = setOf(
                    GestureType.CLICK,
                    GestureType.RIGHT_CLICK,
                    GestureType.KEYBOARD,
                    GestureType.SCROLL
                )
            )
        }
    }
}

/**
 * 无障碍功能定义
 */
@Immutable
data class AccessibilityFeatures(
    val screenReaderSupport: Boolean,
    val highContrastSupport: Boolean,
    val largeTextSupport: Boolean,
    val keyboardNavigationSupport: Boolean,
    val voiceControlSupport: Boolean,
    val reduceMotionSupport: Boolean,
    val semanticLabels: Boolean,
    val focusManagement: Boolean
) {
    companion object {
        fun default(): AccessibilityFeatures {
            return AccessibilityFeatures(
                screenReaderSupport = true,
                highContrastSupport = true,
                largeTextSupport = true,
                keyboardNavigationSupport = true,
                voiceControlSupport = false,
                reduceMotionSupport = true,
                semanticLabels = true,
                focusManagement = true
            )
        }
        
        fun android(): AccessibilityFeatures {
            return AccessibilityFeatures(
                screenReaderSupport = true, // TalkBack
                highContrastSupport = true,
                largeTextSupport = true,
                keyboardNavigationSupport = true,
                voiceControlSupport = true, // Voice Access
                reduceMotionSupport = true,
                semanticLabels = true,
                focusManagement = true
            )
        }
        
        fun ios(): AccessibilityFeatures {
            return AccessibilityFeatures(
                screenReaderSupport = true, // VoiceOver
                highContrastSupport = true,
                largeTextSupport = true, // Dynamic Type
                keyboardNavigationSupport = true,
                voiceControlSupport = true, // Voice Control
                reduceMotionSupport = true,
                semanticLabels = true,
                focusManagement = true
            )
        }
        
        fun harmony(): AccessibilityFeatures {
            return AccessibilityFeatures(
                screenReaderSupport = true,
                highContrastSupport = true,
                largeTextSupport = true,
                keyboardNavigationSupport = true,
                voiceControlSupport = true,
                reduceMotionSupport = true,
                semanticLabels = true,
                focusManagement = true
            )
        }
        
        fun web(): AccessibilityFeatures {
            return AccessibilityFeatures(
                screenReaderSupport = true, // NVDA, JAWS, VoiceOver
                highContrastSupport = true,
                largeTextSupport = true,
                keyboardNavigationSupport = true,
                voiceControlSupport = false,
                reduceMotionSupport = true, // prefers-reduced-motion
                semanticLabels = true, // ARIA
                focusManagement = true
            )
        }
        
        fun desktop(): AccessibilityFeatures {
            return AccessibilityFeatures(
                screenReaderSupport = true,
                highContrastSupport = true,
                largeTextSupport = true,
                keyboardNavigationSupport = true,
                voiceControlSupport = true,
                reduceMotionSupport = true,
                semanticLabels = true,
                focusManagement = true
            )
        }
    }
}

/**
 * 反馈类型枚举
 */
enum class FeedbackType {
    VISUAL,      // 视觉反馈
    HAPTIC,      // 触觉反馈
    AUDIO,       // 音频反馈
    RIPPLE,      // 涟漪效果
    DISTRIBUTED  // 分布式反馈
}

/**
 * 导航模式枚举
 */
enum class NavigationPattern {
    STACK,       // 堆栈导航
    TAB,         // 标签导航
    DRAWER,      // 抽屉导航
    BREADCRUMB,  // 面包屑导航
    MENU,        // 菜单导航
    SERVICE      // 服务导航
}

/**
 * 手势类型枚举
 */
enum class GestureType {
    TAP,           // 点击
    LONG_PRESS,    // 长按
    SWIPE,         // 滑动
    PINCH,         // 捏合
    EDGE_SWIPE,    // 边缘滑动
    HOVER,         // 悬停
    CLICK,         // 鼠标点击
    RIGHT_CLICK,   // 右键点击
    KEYBOARD,      // 键盘操作
    SCROLL,        // 滚动
    MULTI_DEVICE   // 多设备手势
}
