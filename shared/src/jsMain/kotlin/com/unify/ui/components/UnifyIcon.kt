package com.unify.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Web平台的图标实现
 */
actual class UnifyPlatformIcon {
    companion object {
        fun getWebIconClass(icon: UnifyIconType): String? {
            // Font Awesome图标类名
            return when (icon) {
                UnifyIconType.Add -> "fas fa-plus"
                UnifyIconType.Remove -> "fas fa-minus"
                UnifyIconType.Edit -> "fas fa-edit"
                UnifyIconType.Delete -> "fas fa-trash"
                UnifyIconType.Save -> "fas fa-save"
                UnifyIconType.Cancel -> "fas fa-times"
                UnifyIconType.Check -> "fas fa-check"
                UnifyIconType.Close -> "fas fa-times"
                UnifyIconType.Menu -> "fas fa-bars"
                UnifyIconType.More -> "fas fa-ellipsis-v"
                UnifyIconType.Search -> "fas fa-search"
                UnifyIconType.Filter -> "fas fa-filter"
                UnifyIconType.Sort -> "fas fa-sort"
                UnifyIconType.Refresh -> "fas fa-sync"
                UnifyIconType.Settings -> "fas fa-cog"
                UnifyIconType.Info -> "fas fa-info-circle"
                UnifyIconType.Warning -> "fas fa-exclamation-triangle"
                UnifyIconType.Error -> "fas fa-times-circle"
                UnifyIconType.Success -> "fas fa-check-circle"
                UnifyIconType.Home -> "fas fa-home"
                UnifyIconType.Back -> "fas fa-arrow-left"
                UnifyIconType.Forward -> "fas fa-arrow-right"
                UnifyIconType.Up -> "fas fa-arrow-up"
                UnifyIconType.Down -> "fas fa-arrow-down"
                UnifyIconType.Left -> "fas fa-arrow-left"
                UnifyIconType.Right -> "fas fa-arrow-right"
                UnifyIconType.Favorite -> "fas fa-heart"
                UnifyIconType.FavoriteBorder -> "far fa-heart"
                UnifyIconType.Share -> "fas fa-share"
                UnifyIconType.Download -> "fas fa-download"
                UnifyIconType.Upload -> "fas fa-upload"
                UnifyIconType.Copy -> "fas fa-copy"
                UnifyIconType.Paste -> "fas fa-paste"
                UnifyIconType.Cut -> "fas fa-cut"
                UnifyIconType.Undo -> "fas fa-undo"
                UnifyIconType.Redo -> "fas fa-redo"
                UnifyIconType.Visibility -> "fas fa-eye"
                UnifyIconType.VisibilityOff -> "fas fa-eye-slash"
                UnifyIconType.Lock -> "fas fa-lock"
                UnifyIconType.LockOpen -> "fas fa-lock-open"
                UnifyIconType.Person -> "fas fa-user"
                UnifyIconType.Group -> "fas fa-users"
                UnifyIconType.Email -> "fas fa-envelope"
                UnifyIconType.Phone -> "fas fa-phone"
                UnifyIconType.Location -> "fas fa-map-marker-alt"
                UnifyIconType.Calendar -> "fas fa-calendar"
                UnifyIconType.Time -> "fas fa-clock"
                UnifyIconType.Camera -> "fas fa-camera"
                UnifyIconType.Image -> "fas fa-image"
                UnifyIconType.Video -> "fas fa-video"
                UnifyIconType.Audio -> "fas fa-volume-up"
                UnifyIconType.File -> "fas fa-file"
                UnifyIconType.Folder -> "fas fa-folder"
                UnifyIconType.Cloud -> "fas fa-cloud"
                UnifyIconType.Wifi -> "fas fa-wifi"
                UnifyIconType.Bluetooth -> "fab fa-bluetooth"
                UnifyIconType.Battery -> "fas fa-battery-full"
                UnifyIconType.Signal -> "fas fa-signal"
                UnifyIconType.Volume -> "fas fa-volume-up"
                UnifyIconType.VolumeOff -> "fas fa-volume-mute"
                UnifyIconType.Brightness -> "fas fa-sun"
                UnifyIconType.DarkMode -> "fas fa-moon"
                UnifyIconType.LightMode -> "fas fa-sun"
            }
        }
        
        fun supportsFontAwesome(): Boolean {
            // 检查是否加载了Font Awesome
            return true // 实际实现需要检查DOM
        }
        
        fun supportsGoogleIcons(): Boolean {
            // 检查是否加载了Google Material Icons
            return true // 实际实现需要检查DOM
        }
        
        fun getPreferredIconLibrary(): String {
            return when {
                supportsFontAwesome() -> "fontawesome"
                supportsGoogleIcons() -> "material"
                else -> "material"
            }
        }
        
        fun isHighContrastMode(): Boolean {
            // 检查是否为高对比度模式
            return false // 实际实现需要检查CSS媒体查询
        }
        
        fun getPrefersColorScheme(): String {
            // 获取用户偏好的颜色方案
            return "light" // 实际实现需要检查CSS媒体查询
        }
    }
}

/**
 * Web平台的原生图标组件适配器
 */
@Composable
actual fun UnifyNativeIcon(
    icon: UnifyIconType,
    contentDescription: String?,
    modifier: Modifier,
    tint: Color,
    size: UnifyIconSize
) {
    val webIconClass = UnifyPlatformIcon.getWebIconClass(icon)
    
    if (webIconClass != null && UnifyPlatformIcon.supportsFontAwesome()) {
        // 使用Font Awesome图标（实际实现需要调用Web API）
        // 这里回退到Material图标
        UnifyIcon(
            icon = icon,
            contentDescription = contentDescription,
            modifier = modifier,
            tint = tint,
            size = size
        )
    } else {
        // 使用Material图标
        UnifyIcon(
            icon = icon,
            contentDescription = contentDescription,
            modifier = modifier,
            tint = tint,
            size = size
        )
    }
}
