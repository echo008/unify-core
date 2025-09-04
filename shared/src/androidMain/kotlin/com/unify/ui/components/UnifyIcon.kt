package com.unify.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp

@Composable
actual fun UnifyIcon(
    iconName: String,
    contentDescription: String?,
    modifier: Modifier,
    tint: Color,
    size: Dp
) {
    val context = LocalContext.current
    val resourceId = context.resources.getIdentifier(
        iconName,
        "drawable",
        context.packageName
    )
    
    if (resourceId != 0) {
        Icon(
            painter = painterResource(id = resourceId),
            contentDescription = contentDescription,
            modifier = modifier,
            tint = tint
        )
    } else {
        // 使用默认图标
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = contentDescription,
            modifier = modifier,
            tint = tint
        )
    }
}

@Composable
actual fun UnifyVectorIcon(
    vectorPath: String,
    contentDescription: String?,
    modifier: Modifier,
    tint: Color,
    size: Dp
) {
    // Android矢量图标实现 - 简化版本
    Icon(
        imageVector = Icons.Default.Star,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint
    )
}

@Composable
actual fun UnifySystemIcon(
    systemIconType: SystemIconType,
    contentDescription: String?,
    modifier: Modifier,
    tint: Color,
    size: Dp
) {
    val imageVector = when (systemIconType) {
        SystemIconType.HOME -> Icons.Default.Home
        SystemIconType.BACK -> Icons.Default.ArrowBack
        SystemIconType.MENU -> Icons.Default.Menu
        SystemIconType.SEARCH -> Icons.Default.Search
        SystemIconType.SETTINGS -> Icons.Default.Settings
        SystemIconType.PROFILE -> Icons.Default.Person
        SystemIconType.FAVORITE -> Icons.Default.Favorite
        SystemIconType.SHARE -> Icons.Default.Share
        SystemIconType.DELETE -> Icons.Default.Delete
        SystemIconType.EDIT -> Icons.Default.Edit
        SystemIconType.ADD -> Icons.Default.Add
        SystemIconType.CLOSE -> Icons.Default.Close
        SystemIconType.CHECK -> Icons.Default.Check
        SystemIconType.ARROW_UP -> Icons.Default.KeyboardArrowUp
        SystemIconType.ARROW_DOWN -> Icons.Default.KeyboardArrowDown
        SystemIconType.ARROW_LEFT -> Icons.Default.KeyboardArrowLeft
        SystemIconType.ARROW_RIGHT -> Icons.Default.KeyboardArrowRight
        SystemIconType.REFRESH -> Icons.Default.Refresh
        SystemIconType.DOWNLOAD -> Icons.Default.Download
        SystemIconType.UPLOAD -> Icons.Default.Upload
        SystemIconType.CAMERA -> Icons.Default.CameraAlt
        SystemIconType.GALLERY -> Icons.Default.PhotoLibrary
        SystemIconType.PHONE -> Icons.Default.Phone
        SystemIconType.EMAIL -> Icons.Default.Email
        SystemIconType.LOCATION -> Icons.Default.LocationOn
        SystemIconType.CALENDAR -> Icons.Default.CalendarToday
        SystemIconType.CLOCK -> Icons.Default.Schedule
        SystemIconType.NOTIFICATION -> Icons.Default.Notifications
        SystemIconType.VOLUME_UP -> Icons.Default.VolumeUp
        SystemIconType.VOLUME_DOWN -> Icons.Default.VolumeDown
        SystemIconType.VOLUME_OFF -> Icons.Default.VolumeOff
    }
    
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint
    )
}
