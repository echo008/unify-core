package com.unify.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * 小程序平台的图标实现
 */
actual class UnifyPlatformIcon {
    companion object {
        fun getMiniAppIconName(icon: UnifyIconType): String? {
            return when (icon) {
                UnifyIconType.Add -> "add"
                UnifyIconType.Remove -> "minus"
                UnifyIconType.Edit -> "edit"
                UnifyIconType.Delete -> "delete"
                UnifyIconType.Save -> "download"
                UnifyIconType.Cancel -> "clear"
                UnifyIconType.Check -> "success"
                UnifyIconType.Close -> "clear"
                UnifyIconType.Menu -> "list"
                UnifyIconType.More -> "more"
                UnifyIconType.Search -> "search"
                UnifyIconType.Filter -> "filter"
                UnifyIconType.Sort -> "sort"
                UnifyIconType.Refresh -> "refresh"
                UnifyIconType.Settings -> "setting"
                UnifyIconType.Info -> "info"
                UnifyIconType.Warning -> "warn"
                UnifyIconType.Error -> "cancel"
                UnifyIconType.Success -> "success"
                UnifyIconType.Home -> "home"
                UnifyIconType.Back -> "back"
                UnifyIconType.Forward -> "forward"
                UnifyIconType.Up -> "up"
                UnifyIconType.Down -> "down"
                UnifyIconType.Left -> "left"
                UnifyIconType.Right -> "right"
                UnifyIconType.Favorite -> "like"
                UnifyIconType.FavoriteBorder -> "like"
                UnifyIconType.Share -> "share"
                UnifyIconType.Download -> "download"
                UnifyIconType.Upload -> "upload"
                UnifyIconType.Copy -> "copy"
                UnifyIconType.Visibility -> "view"
                UnifyIconType.VisibilityOff -> "hide"
                UnifyIconType.Lock -> "lock"
                UnifyIconType.LockOpen -> "unlock"
                UnifyIconType.Person -> "contact"
                UnifyIconType.Group -> "group"
                UnifyIconType.Email -> "email"
                UnifyIconType.Phone -> "phone"
                UnifyIconType.Location -> "location"
                UnifyIconType.Calendar -> "calendar"
                UnifyIconType.Time -> "time"
                UnifyIconType.Camera -> "camera"
                UnifyIconType.Image -> "image"
                UnifyIconType.Video -> "video"
                UnifyIconType.Audio -> "voice"
                UnifyIconType.File -> "document"
                UnifyIconType.Folder -> "folder"
                UnifyIconType.Cloud -> "cloud"
                UnifyIconType.Wifi -> "wifi"
                UnifyIconType.Bluetooth -> "bluetooth"
                UnifyIconType.Battery -> "battery"
                UnifyIconType.Signal -> "signal"
                UnifyIconType.Volume -> "volume"
                UnifyIconType.VolumeOff -> "mute"
                UnifyIconType.Brightness -> "light"
                UnifyIconType.DarkMode -> "night"
                UnifyIconType.LightMode -> "light"
                else -> null
            }
        }
        
        fun getMiniAppPlatform(): String {
            // 获取小程序平台类型
            return "wechat" // 实际实现需要检测具体平台
        }
        
        fun supportsPlatformIcons(): Boolean {
            return when (getMiniAppPlatform()) {
                "wechat" -> true
                "alipay" -> true
                "baidu" -> true
                "toutiao" -> true
                else -> false
            }
        }
        
        fun getIconPrefix(): String {
            return when (getMiniAppPlatform()) {
                "wechat" -> "weui-icon_"
                "alipay" -> "am-icon-"
                "baidu" -> "swan-icon-"
                "toutiao" -> "tt-icon-"
                else -> "icon-"
            }
        }
        
        fun getMaxIconSize(): Int {
            return when (getMiniAppPlatform()) {
                "wechat" -> 64
                "alipay" -> 48
                "baidu" -> 56
                "toutiao" -> 48
                else -> 48
            }
        }
        
        fun supportsCustomIcons(): Boolean {
            return getMiniAppPlatform() != "wechat" // 微信限制较严
        }
    }
}

/**
 * 小程序平台的原生图标组件适配器
 */
@Composable
actual fun UnifyNativeIcon(
    icon: UnifyIconType,
    contentDescription: String?,
    modifier: Modifier,
    tint: Color,
    size: UnifyIconSize
) {
    val miniAppIconName = UnifyPlatformIcon.getMiniAppIconName(icon)
    
    if (miniAppIconName != null && UnifyPlatformIcon.supportsPlatformIcons()) {
        // 使用小程序平台图标（实际实现需要调用小程序API）
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
