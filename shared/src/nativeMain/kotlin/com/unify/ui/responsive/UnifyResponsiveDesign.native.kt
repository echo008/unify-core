package com.unify.ui.responsive

/**
 * Native平台响应式设计实现 - 原生实现
 */

actual fun getScreenSize(): ScreenSize {
    // Native平台屏幕尺寸检测
    return ScreenSize.DESKTOP
}

actual fun getScreenOrientation(): ScreenOrientation {
    // Native平台屏幕方向检测
    return ScreenOrientation.LANDSCAPE
}

actual fun isTablet(): Boolean {
    // Native平台平板检测
    return false
}

actual fun isMobile(): Boolean {
    // Native平台手机检测
    return false
}

actual fun isDesktop(): Boolean {
    // Native平台桌面检测
    return true
}

actual fun getDeviceType(): DeviceType {
    // Native平台设备类型检测
    return DeviceType.DESKTOP
}

actual fun adaptiveLayout(
    content: (ScreenSize, DeviceType) -> Unit
) {
    content(getScreenSize(), getDeviceType())
}
