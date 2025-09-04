package com.unify.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Unify跨平台图标组件
 * 支持8大平台的统一图标显示
 */
@Composable
expect fun UnifyIcon(
    iconName: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    size: Dp = 24.dp
)

/**
 * Unify矢量图标组件
 */
@Composable
expect fun UnifyVectorIcon(
    vectorPath: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    size: Dp = 24.dp
)

/**
 * Unify系统图标组件
 */
@Composable
expect fun UnifySystemIcon(
    systemIconType: SystemIconType,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    size: Dp = 24.dp
)

/**
 * 系统图标类型枚举
 */
enum class SystemIconType {
    HOME,
    BACK,
    MENU,
    SEARCH,
    SETTINGS,
    PROFILE,
    FAVORITE,
    SHARE,
    DELETE,
    EDIT,
    ADD,
    CLOSE,
    CHECK,
    ARROW_UP,
    ARROW_DOWN,
    ARROW_LEFT,
    ARROW_RIGHT,
    REFRESH,
    DOWNLOAD,
    UPLOAD,
    CAMERA,
    GALLERY,
    PHONE,
    EMAIL,
    LOCATION,
    CALENDAR,
    CLOCK,
    NOTIFICATION,
    VOLUME_UP,
    VOLUME_DOWN,
    VOLUME_OFF
}
