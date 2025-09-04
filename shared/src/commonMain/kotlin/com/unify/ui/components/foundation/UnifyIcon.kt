package com.unify.ui.components.foundation

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Unify跨平台图标组件
 * 支持8大平台的统一图标显示
 */
@Composable
fun UnifyIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) {
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint
    )
}

/**
 * Unify图标组件（Painter版本）
 */
@Composable
fun UnifyIcon(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) {
    Icon(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint
    )
}

/**
 * Unify小图标
 */
@Composable
fun UnifySmallIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) {
    UnifyIcon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier.size(16.dp),
        tint = tint
    )
}

/**
 * Unify中等图标
 */
@Composable
fun UnifyMediumIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) {
    UnifyIcon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier.size(24.dp),
        tint = tint
    )
}

/**
 * Unify大图标
 */
@Composable
fun UnifyLargeIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) {
    UnifyIcon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier.size(32.dp),
        tint = tint
    )
}

/**
 * Unify自定义尺寸图标
 */
@Composable
fun UnifyIconWithSize(
    imageVector: ImageVector,
    contentDescription: String?,
    size: Dp,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) {
    UnifyIcon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        tint = tint
    )
}

/**
 * 常用图标预定义
 */
object UnifyIcons {
    val Home = Icons.Default.Home
    val Search = Icons.Default.Search
    val Settings = Icons.Default.Settings
    val Person = Icons.Default.Person
    val Add = Icons.Default.Add
    val Remove = Icons.Default.Remove
    val Edit = Icons.Default.Edit
    val Delete = Icons.Default.Delete
    val Save = Icons.Default.Save
    val Share = Icons.Default.Share
    val Favorite = Icons.Default.Favorite
    val FavoriteBorder = Icons.Default.FavoriteBorder
    val Star = Icons.Default.Star
    val StarBorder = Icons.Default.StarBorder
    val ThumbUp = Icons.Default.ThumbUp
    val ThumbDown = Icons.Default.ThumbDown
    val Visibility = Icons.Default.Visibility
    val VisibilityOff = Icons.Default.VisibilityOff
    val Lock = Icons.Default.Lock
    val LockOpen = Icons.Default.LockOpen
    val Menu = Icons.Default.Menu
    val MoreVert = Icons.Default.MoreVert
    val MoreHoriz = Icons.Default.MoreHoriz
    val ArrowBack = Icons.Default.ArrowBack
    val ArrowForward = Icons.Default.ArrowForward
    val ArrowUpward = Icons.Default.ArrowUpward
    val ArrowDownward = Icons.Default.ArrowDownward
    val Close = Icons.Default.Close
    val Check = Icons.Default.Check
    val Clear = Icons.Default.Clear
    val Refresh = Icons.Default.Refresh
    val Download = Icons.Default.Download
    val Upload = Icons.Default.Upload
    val Camera = Icons.Default.Camera
    val Photo = Icons.Default.Photo
    val Mic = Icons.Default.Mic
    val MicOff = Icons.Default.MicOff
    val VolumeUp = Icons.Default.VolumeUp
    val VolumeDown = Icons.Default.VolumeDown
    val VolumeOff = Icons.Default.VolumeOff
    val PlayArrow = Icons.Default.PlayArrow
    val Pause = Icons.Default.Pause
    val Stop = Icons.Default.Stop
    val SkipNext = Icons.Default.SkipNext
    val SkipPrevious = Icons.Default.SkipPrevious
    val Notifications = Icons.Default.Notifications
    val NotificationsOff = Icons.Default.NotificationsOff
    val Email = Icons.Default.Email
    val Phone = Icons.Default.Phone
    val LocationOn = Icons.Default.LocationOn
    val DateRange = Icons.Default.DateRange
    val Schedule = Icons.Default.Schedule
    val Info = Icons.Default.Info
    val Warning = Icons.Default.Warning
    val Error = Icons.Default.Error
    val CheckCircle = Icons.Default.CheckCircle
    val Cancel = Icons.Default.Cancel
}
