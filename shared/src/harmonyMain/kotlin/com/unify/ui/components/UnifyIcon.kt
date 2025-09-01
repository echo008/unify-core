package com.unify.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * HarmonyOS平台的图标实现
 */
actual class UnifyPlatformIcon {
    companion object {
        fun getHarmonyIconResource(icon: UnifyIconType): String? {
            return when (icon) {
                UnifyIconType.Add -> "ic_public_add"
                UnifyIconType.Remove -> "ic_public_remove"
                UnifyIconType.Edit -> "ic_public_edit"
                UnifyIconType.Delete -> "ic_public_delete"
                UnifyIconType.Save -> "ic_public_save"
                UnifyIconType.Cancel -> "ic_public_cancel"
                UnifyIconType.Check -> "ic_public_ok"
                UnifyIconType.Close -> "ic_public_cancel"
                UnifyIconType.Menu -> "ic_public_more"
                UnifyIconType.Search -> "ic_public_search"
                UnifyIconType.Settings -> "ic_settings_filled"
                UnifyIconType.Info -> "ic_public_help"
                UnifyIconType.Warning -> "ic_public_warn"
                UnifyIconType.Error -> "ic_public_fail"
                UnifyIconType.Success -> "ic_public_ok"
                UnifyIconType.Home -> "ic_public_home"
                UnifyIconType.Back -> "ic_public_arrow_left"
                UnifyIconType.Forward -> "ic_public_arrow_right"
                UnifyIconType.Up -> "ic_public_arrow_up"
                UnifyIconType.Down -> "ic_public_arrow_down"
                UnifyIconType.Favorite -> "ic_public_favor_filled"
                UnifyIconType.FavoriteBorder -> "ic_public_favor"
                UnifyIconType.Share -> "ic_public_share"
                UnifyIconType.Download -> "ic_public_download"
                UnifyIconType.Upload -> "ic_public_upload"
                UnifyIconType.Visibility -> "ic_public_view"
                UnifyIconType.VisibilityOff -> "ic_public_view_off"
                UnifyIconType.Lock -> "ic_public_lock"
                UnifyIconType.LockOpen -> "ic_public_unlock"
                UnifyIconType.Person -> "ic_public_contacts"
                UnifyIconType.Group -> "ic_public_contacts_group"
                UnifyIconType.Email -> "ic_public_message"
                UnifyIconType.Phone -> "ic_public_phone"
                UnifyIconType.Location -> "ic_public_location"
                UnifyIconType.Calendar -> "ic_public_calendar"
                UnifyIconType.Time -> "ic_public_clock"
                UnifyIconType.Camera -> "ic_public_camera"
                UnifyIconType.Image -> "ic_public_photo"
                UnifyIconType.Video -> "ic_public_video"
                UnifyIconType.Audio -> "ic_public_sound"
                UnifyIconType.File -> "ic_public_file"
                UnifyIconType.Folder -> "ic_public_folder"
                UnifyIconType.Cloud -> "ic_public_cloud"
                UnifyIconType.Wifi -> "ic_public_wifi"
                UnifyIconType.Bluetooth -> "ic_public_bluetooth"
                UnifyIconType.Battery -> "ic_public_battery"
                UnifyIconType.Volume -> "ic_public_sound_filled"
                UnifyIconType.VolumeOff -> "ic_public_sound_off"
                UnifyIconType.Brightness -> "ic_public_light"
                UnifyIconType.DarkMode -> "ic_public_night"
                UnifyIconType.LightMode -> "ic_public_light"
                else -> null
            }
        }
        
        fun supportsHarmonyIcons(): Boolean {
            return true // HarmonyOS支持系统图标
        }
        
        fun getHarmonyIconTheme(): String {
            return "harmony-design" // HarmonyOS设计语言
        }
        
        fun isDistributedCapable(): Boolean {
            // 检查是否支持分布式能力
            return true // HarmonyOS特有功能
        }
        
        fun getDeviceFormFactor(): String {
            // 获取设备形态
            return "phone" // 实际实现需要调用HarmonyOS API
        }
        
        fun adaptIconForDevice(): Boolean {
            // 根据设备形态自适应图标
            return getDeviceFormFactor() in listOf("watch", "tv", "car")
        }
        
        fun supportsAtomicService(): Boolean {
            // 检查是否支持原子化服务
            return true // HarmonyOS特有功能
        }
    }
}

/**
 * HarmonyOS平台的原生图标组件适配器
 */
@Composable
actual fun UnifyNativeIcon(
    icon: UnifyIconType,
    contentDescription: String?,
    modifier: Modifier,
    tint: Color,
    size: UnifyIconSize
) {
    val harmonyIconResource = UnifyPlatformIcon.getHarmonyIconResource(icon)
    
    if (harmonyIconResource != null && UnifyPlatformIcon.supportsHarmonyIcons()) {
        // 使用HarmonyOS系统图标（实际实现需要调用HarmonyOS API）
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
