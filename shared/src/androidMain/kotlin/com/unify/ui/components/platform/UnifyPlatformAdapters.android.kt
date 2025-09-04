package com.unify.ui.components.platform

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Android平台特定的UI适配器组件
 */
object AndroidPlatformAdapters {
    
    /**
     * Android Material Design 按钮适配器
     */
    @Composable
    fun MaterialButton(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        colors: ButtonColors = ButtonDefaults.buttonColors()
    ) {
        Button(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            colors = colors,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = text,
                fontSize = 16.sp
            )
        }
    }
    
    /**
     * Android Material Design 卡片适配器
     */
    @Composable
    fun MaterialCard(
        modifier: Modifier = Modifier,
        colors: CardColors = CardDefaults.cardColors(),
        elevation: CardElevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        content: @Composable ColumnScope.() -> Unit
    ) {
        Card(
            modifier = modifier,
            colors = colors,
            elevation = elevation,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                content = content
            )
        }
    }
    
    /**
     * Android Material Design 输入框适配器
     */
    @Composable
    fun MaterialTextField(
        value: String,
        onValueChange: (String) -> Unit,
        label: String,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        isError: Boolean = false,
        supportingText: String? = null
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = modifier.fillMaxWidth(),
            enabled = enabled,
            isError = isError,
            supportingText = supportingText?.let { { Text(it) } },
            shape = RoundedCornerShape(8.dp)
        )
    }
    
    /**
     * Android Material Design 开关适配器
     */
    @Composable
    fun MaterialSwitch(
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        colors: SwitchColors = SwitchDefaults.colors()
    ) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier,
            enabled = enabled,
            colors = colors
        )
    }
    
    /**
     * Android Material Design 进度条适配器
     */
    @Composable
    fun MaterialProgressIndicator(
        progress: Float,
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.primary,
        trackColor: Color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        LinearProgressIndicator(
            progress = progress,
            modifier = modifier,
            color = color,
            trackColor = trackColor
        )
    }
    
    /**
     * Android Material Design 圆形进度条适配器
     */
    @Composable
    fun MaterialCircularProgressIndicator(
        modifier: Modifier = Modifier,
        color: Color = MaterialTheme.colorScheme.primary,
        strokeWidth: androidx.compose.ui.unit.Dp = 4.dp
    ) {
        CircularProgressIndicator(
            modifier = modifier,
            color = color,
            strokeWidth = strokeWidth
        )
    }
    
    /**
     * Android Material Design 芯片适配器
     */
    @Composable
    fun MaterialChip(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        selected: Boolean = false,
        enabled: Boolean = true
    ) {
        FilterChip(
            onClick = onClick,
            label = { Text(text) },
            selected = selected,
            modifier = modifier,
            enabled = enabled
        )
    }
    
    /**
     * Android Material Design 浮动操作按钮适配器
     */
    @Composable
    fun MaterialFloatingActionButton(
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
        contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
        content: @Composable () -> Unit
    ) {
        FloatingActionButton(
            onClick = onClick,
            modifier = modifier,
            containerColor = containerColor,
            contentColor = contentColor,
            content = content
        )
    }
    
    /**
     * Android Material Design 底部导航栏适配器
     */
    @Composable
    fun MaterialBottomNavigation(
        selectedIndex: Int,
        onItemSelected: (Int) -> Unit,
        items: List<BottomNavItem>,
        modifier: Modifier = Modifier
    ) {
        NavigationBar(
            modifier = modifier
        ) {
            items.forEachIndexed { index, item ->
                NavigationBarItem(
                    icon = { Icon(item.icon, contentDescription = item.label) },
                    label = { Text(item.label) },
                    selected = selectedIndex == index,
                    onClick = { onItemSelected(index) }
                )
            }
        }
    }
    
    /**
     * Android Material Design 顶部应用栏适配器
     */
    @Composable
    fun MaterialTopAppBar(
        title: String,
        modifier: Modifier = Modifier,
        navigationIcon: (@Composable () -> Unit)? = null,
        actions: @Composable RowScope.() -> Unit = {},
        colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors()
    ) {
        TopAppBar(
            title = { Text(title) },
            modifier = modifier,
            navigationIcon = navigationIcon ?: {},
            actions = actions,
            colors = colors
        )
    }
    
    /**
     * Android Material Design 对话框适配器
     */
    @Composable
    fun MaterialDialog(
        onDismissRequest: () -> Unit,
        title: String,
        content: @Composable () -> Unit,
        confirmButton: @Composable () -> Unit,
        dismissButton: @Composable (() -> Unit)? = null,
        modifier: Modifier = Modifier
    ) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = { Text(title) },
            text = content,
            confirmButton = confirmButton,
            dismissButton = dismissButton,
            modifier = modifier
        )
    }
    
    /**
     * Android Material Design 底部表单适配器
     */
    @Composable
    fun MaterialBottomSheet(
        onDismissRequest: () -> Unit,
        modifier: Modifier = Modifier,
        dragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
        content: @Composable ColumnScope.() -> Unit
    ) {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            dragHandle = dragHandle,
            content = content
        )
    }
    
    /**
     * Android 系统状态栏颜色适配器
     */
    @Composable
    fun SystemBarsColorAdapter(
        statusBarColor: Color = MaterialTheme.colorScheme.surface,
        navigationBarColor: Color = MaterialTheme.colorScheme.surface,
        darkIcons: Boolean = !isSystemInDarkTheme()
    ) {
        val context = LocalContext.current
        
        LaunchedEffect(statusBarColor, navigationBarColor, darkIcons) {
            // 在实际应用中，这里会设置系统状态栏颜色
            // 需要使用 WindowCompat.setDecorFitsSystemWindows() 等API
        }
    }
    
    /**
     * Android 权限请求适配器
     */
    @Composable
    fun PermissionRequestAdapter(
        permission: String,
        onPermissionResult: (Boolean) -> Unit,
        content: @Composable (requestPermission: () -> Unit) -> Unit
    ) {
        val context = LocalContext.current
        
        content { 
            // 在实际应用中，这里会使用 ActivityResultContracts.RequestPermission()
            // 进行权限请求
            onPermissionResult(true) // 简化实现
        }
    }
}

/**
 * 底部导航项数据类
 */
data class BottomNavItem(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String
)

/**
 * Android平台特定的主题适配器
 */
object AndroidThemeAdapter {
    
    /**
     * Material Design 3 主题适配器
     */
    @Composable
    fun MaterialTheme(
        darkTheme: Boolean = isSystemInDarkTheme(),
        dynamicColor: Boolean = true,
        content: @Composable () -> Unit
    ) {
        val colorScheme = when {
            dynamicColor && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            darkTheme -> darkColorScheme()
            else -> lightColorScheme()
        }
        
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography(),
            content = content
        )
    }
}

/**
 * Android平台特定的工具函数
 */
object AndroidPlatformUtils {
    
    /**
     * 获取屏幕密度
     */
    @Composable
    fun getScreenDensity(): Float {
        val context = LocalContext.current
        return context.resources.displayMetrics.density
    }
    
    /**
     * 获取屏幕尺寸
     */
    @Composable
    fun getScreenSize(): Pair<Int, Int> {
        val context = LocalContext.current
        val displayMetrics = context.resources.displayMetrics
        return Pair(displayMetrics.widthPixels, displayMetrics.heightPixels)
    }
    
    /**
     * 检查是否为平板设备
     */
    @Composable
    fun isTablet(): Boolean {
        val context = LocalContext.current
        val configuration = context.resources.configuration
        val screenLayout = configuration.screenLayout and android.content.res.Configuration.SCREENLAYOUT_SIZE_MASK
        return screenLayout >= android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE
    }
    
    /**
     * 获取系统语言
     */
    @Composable
    fun getSystemLanguage(): String {
        val context = LocalContext.current
        return context.resources.configuration.locales[0].language
    }
}
