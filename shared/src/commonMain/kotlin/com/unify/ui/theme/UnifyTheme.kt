package com.unify.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * 统一多平台主题系统
 */
@Immutable
data class UnifyTheme(
    val colors: UnifyColors,
    val typography: UnifyTypography,
    val shapes: UnifyShapes
)

/**
 * 统一颜色系统
 */
@Immutable
data class UnifyColors(
    val primary: Color,
    val primaryVariant: Color,
    val secondary: Color,
    val secondaryVariant: Color,
    val background: Color,
    val surface: Color,
    val error: Color,
    val onPrimary: Color,
    val onSecondary: Color,
    val onBackground: Color,
    val onSurface: Color,
    val onError: Color
)

/**
 * 统一字体系统
 */
@Immutable
data class UnifyTypography(
    val h1: TextStyle,
    val h2: TextStyle,
    val h3: TextStyle,
    val h4: TextStyle,
    val h5: TextStyle,
    val h6: TextStyle,
    val subtitle1: TextStyle,
    val subtitle2: TextStyle,
    val body1: TextStyle,
    val body2: TextStyle,
    val button: TextStyle,
    val caption: TextStyle,
    val overline: TextStyle
)

/**
 * 统一形状系统
 */
@Immutable
data class UnifyShapes(
    val small: Float,
    val medium: Float,
    val large: Float
)

/**
 * 本地主题提供者
 */
val LocalUnifyTheme = staticCompositionLocalOf<UnifyTheme> {
    error("No UnifyTheme provided")
}

/**
 * 默认浅色主题
 */
val LightUnifyTheme = UnifyTheme(
    colors = UnifyColors(
        primary = Color(0xFF0066CC),
        primaryVariant = Color(0xFF004D99),
        secondary = Color(0xFF66BB6A),
        secondaryVariant = Color(0xFF4CAF50),
        background = Color(0xFFFFFFFF),
        surface = Color(0xFFFFFFFF),
        error = Color(0xFFD32F2F),
        onPrimary = Color.White,
        onSecondary = Color.White,
        onBackground = Color.Black,
        onSurface = Color.Black,
        onError = Color.White
    ),
    typography = UnifyTypography(
        h1 = TextStyle(fontSize = 96.sp, fontWeight = FontWeight.Light, lineHeight = 117.sp),
        h2 = TextStyle(fontSize = 60.sp, fontWeight = FontWeight.Light, lineHeight = 73.sp),
        h3 = TextStyle(fontSize = 48.sp, fontWeight = FontWeight.Normal, lineHeight = 59.sp),
        h4 = TextStyle(fontSize = 34.sp, fontWeight = FontWeight.Normal, lineHeight = 41.sp),
        h5 = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Normal, lineHeight = 29.sp),
        h6 = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium, lineHeight = 24.sp),
        subtitle1 = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal, lineHeight = 24.sp),
        subtitle2 = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 21.sp),
        body1 = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal, lineHeight = 24.sp),
        body2 = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, lineHeight = 20.sp),
        button = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 16.sp),
        caption = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal, lineHeight = 16.sp),
        overline = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Normal, lineHeight = 16.sp)
    ),
    shapes = UnifyShapes(
        small = 4f,
        medium = 8f,
        large = 16f
    )
)

/**
 * 默认深色主题
 */
val DarkUnifyTheme = UnifyTheme(
    colors = UnifyColors(
        primary = Color(0xFF3399FF),
        primaryVariant = Color(0xFF0066CC),
        secondary = Color(0xFF81C784),
        secondaryVariant = Color(0xFF66BB6A),
        background = Color(0xFF121212),
        surface = Color(0xFF1E1E1E),
        error = Color(0xFFCF6679),
        onPrimary = Color.Black,
        onSecondary = Color.Black,
        onBackground = Color.White,
        onSurface = Color.White,
        onError = Color.Black
    ),
    typography = LightUnifyTheme.typography,
    shapes = LightUnifyTheme.shapes
)

/**
 * 统一主题Composable函数
 */
@Composable
fun UnifyTheme(
    isDarkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val theme = if (isDarkTheme) DarkUnifyTheme else LightUnifyTheme
    
    androidx.compose.runtime.CompositionLocalProvider(
        LocalUnifyTheme provides theme
    ) {
        MaterialTheme(
            colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme(),
            typography = Typography(
                displayLarge = theme.typography.h1,
                displayMedium = theme.typography.h2,
                displaySmall = theme.typography.h3,
                headlineLarge = theme.typography.h4,
                headlineMedium = theme.typography.h5,
                headlineSmall = theme.typography.h6,
                titleLarge = theme.typography.subtitle1,
                titleMedium = theme.typography.subtitle2,
                titleSmall = theme.typography.body1,
                bodyLarge = theme.typography.body2,
                bodyMedium = theme.typography.button,
                bodySmall = theme.typography.caption,
                labelLarge = theme.typography.overline
            )
        ) {
            content()
        }
    }
}

/**
 * 主题访问器对象
 */
object UnifyThemeAccessor {
    val colors: UnifyColors
        @Composable get() = LocalUnifyTheme.current.colors

    val typography: UnifyTypography  
        @Composable get() = LocalUnifyTheme.current.typography

    val shapes: UnifyShapes
        @Composable get() = LocalUnifyTheme.current.shapes
}