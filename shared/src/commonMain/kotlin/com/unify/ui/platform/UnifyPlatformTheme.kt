package com.unify.ui.platform

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.Serializable

/**
 * Unify跨平台主题系统
 * 提供统一的主题管理和平台适配
 */

/**
 * 统一主题配置
 */
@Serializable
data class UnifyThemeConfig(
    val name: String,
    val lightColorScheme: UnifyColorScheme,
    val darkColorScheme: UnifyColorScheme,
    val typography: UnifyTypography,
    val shapes: UnifyShapes,
    val spacing: UnifySpacing
)

/**
 * 统一颜色方案
 */
@Serializable
data class UnifyColorScheme(
    val primary: Long,
    val onPrimary: Long,
    val primaryContainer: Long,
    val onPrimaryContainer: Long,
    val secondary: Long,
    val onSecondary: Long,
    val secondaryContainer: Long,
    val onSecondaryContainer: Long,
    val tertiary: Long,
    val onTertiary: Long,
    val tertiaryContainer: Long,
    val onTertiaryContainer: Long,
    val error: Long,
    val onError: Long,
    val errorContainer: Long,
    val onErrorContainer: Long,
    val background: Long,
    val onBackground: Long,
    val surface: Long,
    val onSurface: Long,
    val surfaceVariant: Long,
    val onSurfaceVariant: Long,
    val outline: Long,
    val outlineVariant: Long,
    val scrim: Long,
    val inverseSurface: Long,
    val inverseOnSurface: Long,
    val inversePrimary: Long
)

/**
 * 统一字体排版
 */
@Serializable
data class UnifyTypography(
    val displayLarge: UnifyTextStyle,
    val displayMedium: UnifyTextStyle,
    val displaySmall: UnifyTextStyle,
    val headlineLarge: UnifyTextStyle,
    val headlineMedium: UnifyTextStyle,
    val headlineSmall: UnifyTextStyle,
    val titleLarge: UnifyTextStyle,
    val titleMedium: UnifyTextStyle,
    val titleSmall: UnifyTextStyle,
    val bodyLarge: UnifyTextStyle,
    val bodyMedium: UnifyTextStyle,
    val bodySmall: UnifyTextStyle,
    val labelLarge: UnifyTextStyle,
    val labelMedium: UnifyTextStyle,
    val labelSmall: UnifyTextStyle
)

/**
 * 统一文本样式
 */
@Serializable
data class UnifyTextStyle(
    val fontSize: Float,
    val lineHeight: Float,
    val fontWeight: Int,
    val letterSpacing: Float
)

/**
 * 统一形状配置
 */
@Serializable
data class UnifyShapes(
    val extraSmall: Float,
    val small: Float,
    val medium: Float,
    val large: Float,
    val extraLarge: Float
)

/**
 * 统一间距配置
 */
@Serializable
data class UnifySpacing(
    val extraSmall: Float,
    val small: Float,
    val medium: Float,
    val large: Float,
    val extraLarge: Float
)

/**
 * 平台主题适配器
 */
expect class UnifyPlatformThemeAdapter() {
    fun getSystemColorScheme(isDark: Boolean): UnifyColorScheme
    fun getSystemTypography(): UnifyTypography
    fun getSystemShapes(): UnifyShapes
    fun applyPlatformSpecificTheme(theme: UnifyThemeConfig): UnifyThemeConfig
}

/**
 * 主题管理器
 */
class UnifyThemeManager {
    
    private val _currentTheme = mutableStateOf(getDefaultTheme())
    val currentTheme: State<UnifyThemeConfig> = _currentTheme
    
    private val _isDarkMode = mutableStateOf(false)
    val isDarkMode: State<Boolean> = _isDarkMode
    
    private val platformAdapter = UnifyPlatformThemeAdapter()
    
    /**
     * 设置主题
     */
    fun setTheme(theme: UnifyThemeConfig) {
        _currentTheme.value = platformAdapter.applyPlatformSpecificTheme(theme)
    }
    
    /**
     * 切换深色模式
     */
    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }
    
    /**
     * 设置深色模式
     */
    fun setDarkMode(isDark: Boolean) {
        _isDarkMode.value = isDark
    }
    
    /**
     * 获取当前颜色方案
     */
    fun getCurrentColorScheme(): UnifyColorScheme {
        return if (_isDarkMode.value) {
            _currentTheme.value.darkColorScheme
        } else {
            _currentTheme.value.lightColorScheme
        }
    }
    
    /**
     * 创建自定义主题
     */
    fun createCustomTheme(
        name: String,
        primaryColor: Color,
        secondaryColor: Color? = null,
        backgroundColor: Color? = null
    ): UnifyThemeConfig {
        val baseTheme = getDefaultTheme()
        val lightScheme = createColorSchemeFromPrimary(primaryColor, false)
        val darkScheme = createColorSchemeFromPrimary(primaryColor, true)
        
        return baseTheme.copy(
            name = name,
            lightColorScheme = lightScheme,
            darkColorScheme = darkScheme
        )
    }
    
    private fun createColorSchemeFromPrimary(primary: Color, isDark: Boolean): UnifyColorScheme {
        // 基于主色生成完整的颜色方案
        return if (isDark) {
            getDarkColorScheme().copy(primary = primary.value.toLong())
        } else {
            getLightColorScheme().copy(primary = primary.value.toLong())
        }
    }
}

/**
 * 统一主题提供者组件
 */
@Composable
fun UnifyTheme(
    themeConfig: UnifyThemeConfig = getDefaultTheme(),
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        themeConfig.darkColorScheme.toMaterial3ColorScheme()
    } else {
        themeConfig.lightColorScheme.toMaterial3ColorScheme()
    }
    
    val typography = themeConfig.typography.toMaterial3Typography()
    val shapes = themeConfig.shapes.toMaterial3Shapes()
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = shapes,
        content = content
    )
}

/**
 * 主题预览组件
 */
@Composable
fun UnifyThemePreview(
    theme: UnifyThemeConfig,
    isDark: Boolean = false
) {
    UnifyTheme(
        themeConfig = theme,
        darkTheme = isDark
    ) {
        Surface {
            // 主题预览内容
        }
    }
}

// 扩展函数：转换为Material3类型
private fun UnifyColorScheme.toMaterial3ColorScheme(): ColorScheme {
    return if (background == Color.Black.value.toLong()) {
        darkColorScheme(
            primary = Color(primary.toULong()),
            onPrimary = Color(onPrimary.toULong()),
            primaryContainer = Color(primaryContainer.toULong()),
            onPrimaryContainer = Color(onPrimaryContainer.toULong()),
            secondary = Color(secondary.toULong()),
            onSecondary = Color(onSecondary.toULong()),
            secondaryContainer = Color(secondaryContainer.toULong()),
            onSecondaryContainer = Color(onSecondaryContainer.toULong()),
            tertiary = Color(tertiary.toULong()),
            onTertiary = Color(onTertiary.toULong()),
            tertiaryContainer = Color(tertiaryContainer.toULong()),
            onTertiaryContainer = Color(onTertiaryContainer.toULong()),
            error = Color(error.toULong()),
            onError = Color(onError.toULong()),
            errorContainer = Color(errorContainer.toULong()),
            onErrorContainer = Color(onErrorContainer.toULong()),
            background = Color(background.toULong()),
            onBackground = Color(onBackground.toULong()),
            surface = Color(surface.toULong()),
            onSurface = Color(onSurface.toULong()),
            surfaceVariant = Color(surfaceVariant.toULong()),
            onSurfaceVariant = Color(onSurfaceVariant.toULong()),
            outline = Color(outline.toULong()),
            outlineVariant = Color(outlineVariant.toULong()),
            scrim = Color(scrim.toULong()),
            inverseSurface = Color(inverseSurface.toULong()),
            inverseOnSurface = Color(inverseOnSurface.toULong()),
            inversePrimary = Color(inversePrimary.toULong())
        )
    } else {
        lightColorScheme(
            primary = Color(primary.toULong()),
            onPrimary = Color(onPrimary.toULong()),
            primaryContainer = Color(primaryContainer.toULong()),
            onPrimaryContainer = Color(onPrimaryContainer.toULong()),
            secondary = Color(secondary.toULong()),
            onSecondary = Color(onSecondary.toULong()),
            secondaryContainer = Color(secondaryContainer.toULong()),
            onSecondaryContainer = Color(onSecondaryContainer.toULong()),
            tertiary = Color(tertiary.toULong()),
            onTertiary = Color(onTertiary.toULong()),
            tertiaryContainer = Color(tertiaryContainer.toULong()),
            onTertiaryContainer = Color(onTertiaryContainer.toULong()),
            error = Color(error.toULong()),
            onError = Color(onError.toULong()),
            errorContainer = Color(errorContainer.toULong()),
            onErrorContainer = Color(onErrorContainer.toULong()),
            background = Color(background.toULong()),
            onBackground = Color(onBackground.toULong()),
            surface = Color(surface.toULong()),
            onSurface = Color(onSurface.toULong()),
            surfaceVariant = Color(surfaceVariant.toULong()),
            onSurfaceVariant = Color(onSurfaceVariant.toULong()),
            outline = Color(outline.toULong()),
            outlineVariant = Color(outlineVariant.toULong()),
            scrim = Color(scrim.toULong()),
            inverseSurface = Color(inverseSurface.toULong()),
            inverseOnSurface = Color(inverseOnSurface.toULong()),
            inversePrimary = Color(inversePrimary.toULong())
        )
    }
}

private fun UnifyTypography.toMaterial3Typography(): Typography {
    return Typography(
        displayLarge = displayLarge.toTextStyle(),
        displayMedium = displayMedium.toTextStyle(),
        displaySmall = displaySmall.toTextStyle(),
        headlineLarge = headlineLarge.toTextStyle(),
        headlineMedium = headlineMedium.toTextStyle(),
        headlineSmall = headlineSmall.toTextStyle(),
        titleLarge = titleLarge.toTextStyle(),
        titleMedium = titleMedium.toTextStyle(),
        titleSmall = titleSmall.toTextStyle(),
        bodyLarge = bodyLarge.toTextStyle(),
        bodyMedium = bodyMedium.toTextStyle(),
        bodySmall = bodySmall.toTextStyle(),
        labelLarge = labelLarge.toTextStyle(),
        labelMedium = labelMedium.toTextStyle(),
        labelSmall = labelSmall.toTextStyle()
    )
}

private fun UnifyTextStyle.toTextStyle(): TextStyle {
    return TextStyle(
        fontSize = fontSize.sp,
        lineHeight = lineHeight.sp,
        fontWeight = FontWeight(fontWeight),
        letterSpacing = letterSpacing.sp,
        fontFamily = FontFamily.Default
    )
}

private fun UnifyShapes.toMaterial3Shapes(): Shapes {
    return Shapes(
        extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(extraSmall.dp),
        small = androidx.compose.foundation.shape.RoundedCornerShape(small.dp),
        medium = androidx.compose.foundation.shape.RoundedCornerShape(medium.dp),
        large = androidx.compose.foundation.shape.RoundedCornerShape(large.dp),
        extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(extraLarge.dp)
    )
}

// 默认主题配置
fun getDefaultTheme(): UnifyThemeConfig {
    return UnifyThemeConfig(
        name = "Unify Default",
        lightColorScheme = getLightColorScheme(),
        darkColorScheme = getDarkColorScheme(),
        typography = getDefaultTypography(),
        shapes = getDefaultShapes(),
        spacing = getDefaultSpacing()
    )
}

private fun getLightColorScheme(): UnifyColorScheme {
    return UnifyColorScheme(
        primary = 0xFF6750A4,
        onPrimary = 0xFFFFFFFF,
        primaryContainer = 0xFFEADDFF,
        onPrimaryContainer = 0xFF21005D,
        secondary = 0xFF625B71,
        onSecondary = 0xFFFFFFFF,
        secondaryContainer = 0xFFE8DEF8,
        onSecondaryContainer = 0xFF1D192B,
        tertiary = 0xFF7D5260,
        onTertiary = 0xFFFFFFFF,
        tertiaryContainer = 0xFFFFD8E4,
        onTertiaryContainer = 0xFF31111D,
        error = 0xFFBA1A1A,
        onError = 0xFFFFFFFF,
        errorContainer = 0xFFFFDAD6,
        onErrorContainer = 0xFF410002,
        background = 0xFFFFFBFE,
        onBackground = 0xFF1C1B1F,
        surface = 0xFFFFFBFE,
        onSurface = 0xFF1C1B1F,
        surfaceVariant = 0xFFE7E0EC,
        onSurfaceVariant = 0xFF49454F,
        outline = 0xFF79747E,
        outlineVariant = 0xFFCAC4D0,
        scrim = 0xFF000000,
        inverseSurface = 0xFF313033,
        inverseOnSurface = 0xFFF4EFF4,
        inversePrimary = 0xFFD0BCFF
    )
}

private fun getDarkColorScheme(): UnifyColorScheme {
    return UnifyColorScheme(
        primary = 0xFFD0BCFF,
        onPrimary = 0xFF381E72,
        primaryContainer = 0xFF4F378B,
        onPrimaryContainer = 0xFFEADDFF,
        secondary = 0xFFCCC2DC,
        onSecondary = 0xFF332D41,
        secondaryContainer = 0xFF4A4458,
        onSecondaryContainer = 0xFFE8DEF8,
        tertiary = 0xFFEFB8C8,
        onTertiary = 0xFF492532,
        tertiaryContainer = 0xFF633B48,
        onTertiaryContainer = 0xFFFFD8E4,
        error = 0xFFFFB4AB,
        onError = 0xFF690005,
        errorContainer = 0xFF93000A,
        onErrorContainer = 0xFFFFDAD6,
        background = 0xFF1C1B1F,
        onBackground = 0xFFE6E1E5,
        surface = 0xFF1C1B1F,
        onSurface = 0xFFE6E1E5,
        surfaceVariant = 0xFF49454F,
        onSurfaceVariant = 0xFFCAC4D0,
        outline = 0xFF938F99,
        outlineVariant = 0xFF49454F,
        scrim = 0xFF000000,
        inverseSurface = 0xFFE6E1E5,
        inverseOnSurface = 0xFF313033,
        inversePrimary = 0xFF6750A4
    )
}

private fun getDefaultTypography(): UnifyTypography {
    return UnifyTypography(
        displayLarge = UnifyTextStyle(57f, 64f, 400, -0.25f),
        displayMedium = UnifyTextStyle(45f, 52f, 400, 0f),
        displaySmall = UnifyTextStyle(36f, 44f, 400, 0f),
        headlineLarge = UnifyTextStyle(32f, 40f, 400, 0f),
        headlineMedium = UnifyTextStyle(28f, 36f, 400, 0f),
        headlineSmall = UnifyTextStyle(24f, 32f, 400, 0f),
        titleLarge = UnifyTextStyle(22f, 28f, 400, 0f),
        titleMedium = UnifyTextStyle(16f, 24f, 500, 0.15f),
        titleSmall = UnifyTextStyle(14f, 20f, 500, 0.1f),
        bodyLarge = UnifyTextStyle(16f, 24f, 400, 0.5f),
        bodyMedium = UnifyTextStyle(14f, 20f, 400, 0.25f),
        bodySmall = UnifyTextStyle(12f, 16f, 400, 0.4f),
        labelLarge = UnifyTextStyle(14f, 20f, 500, 0.1f),
        labelMedium = UnifyTextStyle(12f, 16f, 500, 0.5f),
        labelSmall = UnifyTextStyle(11f, 16f, 500, 0.5f)
    )
}

private fun getDefaultShapes(): UnifyShapes {
    return UnifyShapes(
        extraSmall = 4f,
        small = 8f,
        medium = 12f,
        large = 16f,
        extraLarge = 28f
    )
}

private fun getDefaultSpacing(): UnifySpacing {
    return UnifySpacing(
        extraSmall = 4f,
        small = 8f,
        medium = 16f,
        large = 24f,
        extraLarge = 32f
    )
}
