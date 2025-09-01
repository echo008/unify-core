package com.unify.ui.components.platform

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.unify.core.platform.PlatformManager
import java.awt.Desktop
import java.awt.SystemTray
import java.awt.TrayIcon
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

/**
 * Desktop平台特定组件适配器
 * 提供桌面原生功能的Compose封装
 */

@Composable
actual fun PlatformSpecificButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF1976D2), // Desktop蓝色主题
            contentColor = Color.White
        )
    ) {
        Text(text)
    }
}

@Composable
actual fun PlatformSpecificCard(
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

/**
 * Desktop文件选择器组件
 */
@Composable
fun DesktopFileChooser(
    onFileSelected: (String) -> Unit,
    fileExtensions: List<String> = emptyList(),
    modifier: Modifier = Modifier
) {
    Button(
        onClick = {
            val fileChooser = JFileChooser()
            if (fileExtensions.isNotEmpty()) {
                val filter = FileNameExtensionFilter(
                    "支持的文件 (${fileExtensions.joinToString(", ")})",
                    *fileExtensions.toTypedArray()
                )
                fileChooser.fileFilter = filter
            }
            
            val result = fileChooser.showOpenDialog(null)
            if (result == JFileChooser.APPROVE_OPTION) {
                onFileSelected(fileChooser.selectedFile.absolutePath)
            }
        },
        modifier = modifier
    ) {
        Text("选择文件")
    }
}

/**
 * Desktop系统托盘通知组件
 */
@Composable
fun DesktopSystemTrayNotification(
    title: String,
    message: String,
    onShow: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = {
            if (SystemTray.isSupported()) {
                // 显示系统托盘通知
                onShow()
            }
        },
        modifier = modifier
    ) {
        Text("显示通知")
    }
}

/**
 * Desktop窗口管理组件
 */
@Composable
fun DesktopWindowControls(
    onMinimize: () -> Unit = {},
    onMaximize: () -> Unit = {},
    onClose: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onMinimize,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow)
        ) {
            Text("−", color = Color.Black)
        }
        
        Button(
            onClick = onMaximize,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
        ) {
            Text("□", color = Color.Black)
        }
        
        Button(
            onClick = onClose,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text("×", color = Color.White)
        }
    }
}

/**
 * Desktop拖拽文件组件
 */
@Composable
fun DesktopDropZone(
    onFilesDropped: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    // 简化实现，实际需要集成AWT的拖拽功能
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        content = content
    )
}

/**
 * Desktop菜单栏组件
 */
@Composable
fun DesktopMenuBar(
    menuItems: List<DesktopMenuItem>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        menuItems.forEach { item ->
            TextButton(onClick = item.onClick) {
                Text(item.title)
            }
        }
    }
}

data class DesktopMenuItem(
    val title: String,
    val onClick: () -> Unit,
    val subItems: List<DesktopMenuItem> = emptyList()
)

/**
 * Desktop多显示器支持组件
 */
@Composable
fun DesktopMultiDisplayInfo(
    modifier: Modifier = Modifier
) {
    val screenInfo = remember { PlatformManager.getScreenInfo() }
    
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "显示器信息",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("分辨率: ${screenInfo.width} x ${screenInfo.height}")
            Text("刷新率: ${screenInfo.refreshRate} Hz")
            Text("密度: ${screenInfo.density}")
        }
    }
}
