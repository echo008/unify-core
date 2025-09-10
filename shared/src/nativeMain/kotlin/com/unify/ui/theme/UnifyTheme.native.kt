package com.unify.ui.theme

/**
 * Native平台主题实现 - 原生实现
 */
actual fun UnifyTheme(
    darkTheme: Boolean,
    content: () -> Unit
) {
    // Native平台主题配置 - 原生实现
    content()
}

actual fun supportsDynamicColor(): Boolean {
    // Native平台动态颜色支持
    return false
}

actual fun dynamicLightColorScheme(): Any {
    // Native平台动态浅色主题 - 返回原生主题对象
    return Any()
}

actual fun dynamicDarkColorScheme(): Any {
    // Native平台动态深色主题 - 返回原生主题对象
    return Any()
}
