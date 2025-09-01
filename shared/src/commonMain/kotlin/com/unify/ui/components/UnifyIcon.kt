package com.unify.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.unify.ui.LocalUnifyTheme

/**
 * 统一图标组件
 * 跨平台一致的图标显示组件，支持多种图标类型和尺寸
 */
@Composable
fun UnifyIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    size: UnifyIconSize = UnifyIconSize.Medium
) {
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier.size(size.dp),
        tint = tint
    )
}

/**
 * 使用预定义图标的便捷组件
 */
@Composable
fun UnifyIcon(
    icon: UnifyIconType,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    size: UnifyIconSize = UnifyIconSize.Medium
) {
    val imageVector = when (icon) {
        UnifyIconType.Add -> Icons.Default.Add
        UnifyIconType.Remove -> Icons.Default.Remove
        UnifyIconType.Edit -> Icons.Default.Edit
        UnifyIconType.Delete -> Icons.Default.Delete
        UnifyIconType.Save -> Icons.Default.Save
        UnifyIconType.Cancel -> Icons.Default.Cancel
        UnifyIconType.Check -> Icons.Default.Check
        UnifyIconType.Close -> Icons.Default.Close
        UnifyIconType.Menu -> Icons.Default.Menu
        UnifyIconType.More -> Icons.Default.MoreVert
        UnifyIconType.Search -> Icons.Default.Search
        UnifyIconType.Filter -> Icons.Default.FilterList
        UnifyIconType.Sort -> Icons.Default.Sort
        UnifyIconType.Refresh -> Icons.Default.Refresh
        UnifyIconType.Settings -> Icons.Default.Settings
        UnifyIconType.Info -> Icons.Default.Info
        UnifyIconType.Warning -> Icons.Default.Warning
        UnifyIconType.Error -> Icons.Default.Error
        UnifyIconType.Success -> Icons.Default.CheckCircle
        UnifyIconType.Home -> Icons.Default.Home
        UnifyIconType.Back -> Icons.Default.ArrowBack
        UnifyIconType.Forward -> Icons.Default.ArrowForward
        UnifyIconType.Up -> Icons.Default.KeyboardArrowUp
        UnifyIconType.Down -> Icons.Default.KeyboardArrowDown
        UnifyIconType.Left -> Icons.Default.KeyboardArrowLeft
        UnifyIconType.Right -> Icons.Default.KeyboardArrowRight
        UnifyIconType.Favorite -> Icons.Default.Favorite
        UnifyIconType.FavoriteBorder -> Icons.Default.FavoriteBorder
        UnifyIconType.Share -> Icons.Default.Share
        UnifyIconType.Download -> Icons.Default.Download
        UnifyIconType.Upload -> Icons.Default.Upload
        UnifyIconType.Copy -> Icons.Default.ContentCopy
        UnifyIconType.Paste -> Icons.Default.ContentPaste
        UnifyIconType.Cut -> Icons.Default.ContentCut
        UnifyIconType.Undo -> Icons.Default.Undo
        UnifyIconType.Redo -> Icons.Default.Redo
        UnifyIconType.Visibility -> Icons.Default.Visibility
        UnifyIconType.VisibilityOff -> Icons.Default.VisibilityOff
        UnifyIconType.Lock -> Icons.Default.Lock
        UnifyIconType.LockOpen -> Icons.Default.LockOpen
        UnifyIconType.Person -> Icons.Default.Person
        UnifyIconType.Group -> Icons.Default.Group
        UnifyIconType.Email -> Icons.Default.Email
        UnifyIconType.Phone -> Icons.Default.Phone
        UnifyIconType.Location -> Icons.Default.LocationOn
        UnifyIconType.Calendar -> Icons.Default.DateRange
        UnifyIconType.Time -> Icons.Default.AccessTime
        UnifyIconType.Camera -> Icons.Default.CameraAlt
        UnifyIconType.Image -> Icons.Default.Image
        UnifyIconType.Video -> Icons.Default.Videocam
        UnifyIconType.Audio -> Icons.Default.AudioFile
        UnifyIconType.File -> Icons.Default.InsertDriveFile
        UnifyIconType.Folder -> Icons.Default.Folder
        UnifyIconType.Cloud -> Icons.Default.Cloud
        UnifyIconType.Wifi -> Icons.Default.Wifi
        UnifyIconType.Bluetooth -> Icons.Default.Bluetooth
        UnifyIconType.Battery -> Icons.Default.Battery90
        UnifyIconType.Signal -> Icons.Default.SignalCellular4Bar
        UnifyIconType.Volume -> Icons.Default.VolumeUp
        UnifyIconType.VolumeOff -> Icons.Default.VolumeOff
        UnifyIconType.Brightness -> Icons.Default.Brightness6
        UnifyIconType.DarkMode -> Icons.Default.DarkMode
        UnifyIconType.LightMode -> Icons.Default.LightMode
    }
    
    UnifyIcon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint,
        size = size
    )
}

/**
 * 图标尺寸枚举
 */
enum class UnifyIconSize(val dp: Dp) {
    ExtraSmall(12.dp),
    Small(16.dp),
    Medium(24.dp),
    Large(32.dp),
    ExtraLarge(48.dp)
}

/**
 * 预定义图标类型
 */
enum class UnifyIconType {
    // 基础操作
    Add, Remove, Edit, Delete, Save, Cancel, Check, Close,
    
    // 导航
    Menu, More, Search, Filter, Sort, Refresh, Settings,
    Home, Back, Forward, Up, Down, Left, Right,
    
    // 状态
    Info, Warning, Error, Success,
    
    // 交互
    Favorite, FavoriteBorder, Share, Download, Upload,
    Copy, Paste, Cut, Undo, Redo,
    
    // 可见性和安全
    Visibility, VisibilityOff, Lock, LockOpen,
    
    // 用户和联系
    Person, Group, Email, Phone, Location,
    
    // 时间和日期
    Calendar, Time,
    
    // 媒体
    Camera, Image, Video, Audio, File, Folder,
    
    // 连接和系统
    Cloud, Wifi, Bluetooth, Battery, Signal,
    Volume, VolumeOff, Brightness, DarkMode, LightMode
}

/**
 * 带背景的图标按钮
 */
@Composable
fun UnifyIconButton(
    icon: UnifyIconType,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = LocalContentColor.current,
    size: UnifyIconSize = UnifyIconSize.Medium,
    backgroundColor: Color = Color.Transparent
) {
    val theme = LocalUnifyTheme.current
    
    UnifyButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        variant = UnifyButtonVariant.Text,
        size = UnifyButtonSize.Medium,
        colors = UnifyButtonColors(
            background = backgroundColor,
            content = tint,
            backgroundDisabled = theme.colors.surfaceVariant,
            contentDisabled = theme.colors.onSurfaceVariant
        )
    ) {
        UnifyIcon(
            icon = icon,
            contentDescription = contentDescription,
            tint = if (enabled) tint else theme.colors.onSurfaceVariant,
            size = size
        )
    }
}

/**
 * 带徽章的图标
 */
@Composable
fun UnifyBadgedIcon(
    icon: UnifyIconType,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    size: UnifyIconSize = UnifyIconSize.Medium,
    badgeContent: (@Composable () -> Unit)? = null
) {
    androidx.compose.material3.BadgedBox(
        badge = {
            if (badgeContent != null) {
                androidx.compose.material3.Badge {
                    badgeContent()
                }
            }
        },
        modifier = modifier
    ) {
        UnifyIcon(
            icon = icon,
            contentDescription = contentDescription,
            tint = tint,
            size = size
        )
    }
}

/**
 * 平台特定的图标实现
 */
expect class UnifyPlatformIcon

/**
 * 图标组件的平台适配器
 */
@Composable
expect fun UnifyNativeIcon(
    icon: UnifyIconType,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    size: UnifyIconSize = UnifyIconSize.Medium
)
