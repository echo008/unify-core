package com.unify.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp

/**
 * Android平台的图标实现
 */
actual class UnifyPlatformIcon {
    companion object {
        fun getSystemIconResource(icon: UnifyIconType): Int? {
            return when (icon) {
                UnifyIconType.Add -> android.R.drawable.ic_input_add
                UnifyIconType.Delete -> android.R.drawable.ic_menu_delete
                UnifyIconType.Search -> android.R.drawable.ic_menu_search
                UnifyIconType.Settings -> android.R.drawable.ic_menu_preferences
                UnifyIconType.Info -> android.R.drawable.ic_menu_info_details
                UnifyIconType.Share -> android.R.drawable.ic_menu_share
                UnifyIconType.Camera -> android.R.drawable.ic_menu_camera
                UnifyIconType.Gallery -> android.R.drawable.ic_menu_gallery
                else -> null
            }
        }
        
        fun isVectorDrawable(resourceId: Int): Boolean {
            // 检查是否为矢量图标
            return true // 实际实现需要检查资源类型
        }
        
        fun getIconDensity(): String {
            val context = android.app.ActivityThread.currentApplication()
            val density = context.resources.displayMetrics.density
            return when {
                density >= 4.0f -> "xxxhdpi"
                density >= 3.0f -> "xxhdpi"
                density >= 2.0f -> "xhdpi"
                density >= 1.5f -> "hdpi"
                density >= 1.0f -> "mdpi"
                else -> "ldpi"
            }
        }
        
        fun supportsAdaptiveIcons(): Boolean {
            return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O
        }
        
        fun getThemeIconTint(context: android.content.Context): Int {
            val typedArray = context.theme.obtainStyledAttributes(intArrayOf(android.R.attr.textColorPrimary))
            val color = typedArray.getColor(0, 0)
            typedArray.recycle()
            return color
        }
    }
}

/**
 * Android平台的原生图标组件适配器
 */
@Composable
actual fun UnifyNativeIcon(
    icon: UnifyIconType,
    contentDescription: String?,
    modifier: Modifier,
    tint: Color,
    size: UnifyIconSize
) {
    val context = LocalContext.current
    val resourceId = UnifyPlatformIcon.getSystemIconResource(icon)
    
    if (resourceId != null) {
        try {
            val imageVector = ImageVector.vectorResource(id = resourceId)
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                modifier = modifier.size(size.dp),
                tint = tint
            )
        } catch (e: Exception) {
            // 回退到Material图标
            UnifyIcon(
                icon = icon,
                contentDescription = contentDescription,
                modifier = modifier,
                tint = tint,
                size = size
            )
        }
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
