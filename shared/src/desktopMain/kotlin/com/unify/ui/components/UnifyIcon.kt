package com.unify.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Desktop平台的图标实现
 */
actual class UnifyPlatformIcon {
    companion object {
        fun getSystemIconPath(icon: UnifyIconType): String? {
            val osName = System.getProperty("os.name").lowercase()
            return when {
                osName.contains("windows") -> getWindowsIconPath(icon)
                osName.contains("mac") -> getMacIconPath(icon)
                osName.contains("linux") -> getLinuxIconPath(icon)
                else -> null
            }
        }
        
        private fun getWindowsIconPath(icon: UnifyIconType): String? {
            return when (icon) {
                UnifyIconType.Add -> "shell32.dll,0"
                UnifyIconType.Delete -> "shell32.dll,31"
                UnifyIconType.Search -> "shell32.dll,23"
                UnifyIconType.Settings -> "shell32.dll,21"
                UnifyIconType.Folder -> "shell32.dll,4"
                UnifyIconType.File -> "shell32.dll,1"
                else -> null
            }
        }
        
        private fun getMacIconPath(icon: UnifyIconType): String? {
            return when (icon) {
                UnifyIconType.Add -> "NSAddTemplate"
                UnifyIconType.Remove -> "NSRemoveTemplate"
                UnifyIconType.Refresh -> "NSRefreshTemplate"
                UnifyIconType.Search -> "NSSearchFieldTemplate"
                UnifyIconType.Settings -> "NSPreferencesGeneral"
                UnifyIconType.Folder -> "NSFolder"
                else -> null
            }
        }
        
        private fun getLinuxIconPath(icon: UnifyIconType): String? {
            return when (icon) {
                UnifyIconType.Add -> "list-add"
                UnifyIconType.Remove -> "list-remove"
                UnifyIconType.Edit -> "document-edit"
                UnifyIconType.Delete -> "edit-delete"
                UnifyIconType.Search -> "system-search"
                UnifyIconType.Settings -> "preferences-system"
                UnifyIconType.Folder -> "folder"
                else -> null
            }
        }
        
        fun supportsSystemIcons(): Boolean {
            return true
        }
        
        fun getIconTheme(): String {
            val osName = System.getProperty("os.name").lowercase()
            return when {
                osName.contains("windows") -> "fluent"
                osName.contains("mac") -> "sf-symbols"
                osName.contains("linux") -> "adwaita"
                else -> "material"
            }
        }
        
        fun isHighDpiDisplay(): Boolean {
            return System.getProperty("sun.java2d.uiScale")?.toFloatOrNull()?.let { it > 1.0f } ?: false
        }
        
        fun getSystemDarkMode(): Boolean {
            // 检查系统深色模式
            return false // 实际实现需要调用系统API
        }
    }
}

/**
 * Desktop平台的原生图标组件适配器
 */
@Composable
actual fun UnifyNativeIcon(
    icon: UnifyIconType,
    contentDescription: String?,
    modifier: Modifier,
    tint: Color,
    size: UnifyIconSize
) {
    val systemIconPath = UnifyPlatformIcon.getSystemIconPath(icon)
    
    if (systemIconPath != null && UnifyPlatformIcon.supportsSystemIcons()) {
        // 使用系统图标（实际实现需要调用系统API）
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
