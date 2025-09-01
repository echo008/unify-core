package com.unify.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * iOS平台的图标实现
 */
actual class UnifyPlatformIcon {
    companion object {
        fun getSystemIconName(icon: UnifyIconType): String? {
            return when (icon) {
                UnifyIconType.Add -> "plus"
                UnifyIconType.Remove -> "minus"
                UnifyIconType.Edit -> "pencil"
                UnifyIconType.Delete -> "trash"
                UnifyIconType.Save -> "square.and.arrow.down"
                UnifyIconType.Cancel -> "xmark"
                UnifyIconType.Check -> "checkmark"
                UnifyIconType.Close -> "xmark"
                UnifyIconType.Menu -> "line.horizontal.3"
                UnifyIconType.More -> "ellipsis"
                UnifyIconType.Search -> "magnifyingglass"
                UnifyIconType.Filter -> "line.3.horizontal.decrease"
                UnifyIconType.Sort -> "arrow.up.arrow.down"
                UnifyIconType.Refresh -> "arrow.clockwise"
                UnifyIconType.Settings -> "gearshape"
                UnifyIconType.Info -> "info.circle"
                UnifyIconType.Warning -> "exclamationmark.triangle"
                UnifyIconType.Error -> "xmark.circle"
                UnifyIconType.Success -> "checkmark.circle"
                UnifyIconType.Home -> "house"
                UnifyIconType.Back -> "chevron.left"
                UnifyIconType.Forward -> "chevron.right"
                UnifyIconType.Up -> "chevron.up"
                UnifyIconType.Down -> "chevron.down"
                UnifyIconType.Left -> "chevron.left"
                UnifyIconType.Right -> "chevron.right"
                UnifyIconType.Favorite -> "heart.fill"
                UnifyIconType.FavoriteBorder -> "heart"
                UnifyIconType.Share -> "square.and.arrow.up"
                UnifyIconType.Download -> "square.and.arrow.down"
                UnifyIconType.Upload -> "square.and.arrow.up"
                UnifyIconType.Copy -> "doc.on.doc"
                UnifyIconType.Paste -> "doc.on.clipboard"
                UnifyIconType.Cut -> "scissors"
                UnifyIconType.Undo -> "arrow.uturn.backward"
                UnifyIconType.Redo -> "arrow.uturn.forward"
                UnifyIconType.Visibility -> "eye"
                UnifyIconType.VisibilityOff -> "eye.slash"
                UnifyIconType.Lock -> "lock"
                UnifyIconType.LockOpen -> "lock.open"
                UnifyIconType.Person -> "person"
                UnifyIconType.Group -> "person.2"
                UnifyIconType.Email -> "envelope"
                UnifyIconType.Phone -> "phone"
                UnifyIconType.Location -> "location"
                UnifyIconType.Calendar -> "calendar"
                UnifyIconType.Time -> "clock"
                UnifyIconType.Camera -> "camera"
                UnifyIconType.Image -> "photo"
                UnifyIconType.Video -> "video"
                UnifyIconType.Audio -> "waveform"
                UnifyIconType.File -> "doc"
                UnifyIconType.Folder -> "folder"
                UnifyIconType.Cloud -> "icloud"
                UnifyIconType.Wifi -> "wifi"
                UnifyIconType.Bluetooth -> "bluetooth"
                UnifyIconType.Battery -> "battery.100"
                UnifyIconType.Signal -> "antenna.radiowaves.left.and.right"
                UnifyIconType.Volume -> "speaker.2"
                UnifyIconType.VolumeOff -> "speaker.slash"
                UnifyIconType.Brightness -> "sun.max"
                UnifyIconType.DarkMode -> "moon"
                UnifyIconType.LightMode -> "sun.max"
            }
        }
        
        fun supportsSFSymbols(): Boolean {
            // 检查是否支持SF Symbols
            return true // iOS 13+支持SF Symbols
        }
        
        fun getIconWeight(): String {
            return "regular" // ultraLight, thin, light, regular, medium, semibold, bold, heavy, black
        }
        
        fun getIconScale(): String {
            return "medium" // small, medium, large
        }
        
        fun supportsMulticolor(): Boolean {
            // 检查是否支持多色图标
            return true // iOS 15+支持多色SF Symbols
        }
        
        fun isAccessibilityBoldTextEnabled(): Boolean {
            // 检查是否启用粗体文本辅助功能
            return false // 实际实现需要调用iOS API
        }
    }
}

/**
 * iOS平台的原生图标组件适配器
 */
@Composable
actual fun UnifyNativeIcon(
    icon: UnifyIconType,
    contentDescription: String?,
    modifier: Modifier,
    tint: Color,
    size: UnifyIconSize
) {
    val systemIconName = UnifyPlatformIcon.getSystemIconName(icon)
    
    if (systemIconName != null && UnifyPlatformIcon.supportsSFSymbols()) {
        // 使用SF Symbols（实际实现需要调用iOS原生API）
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
