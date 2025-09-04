package com.unify.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 设置界面
 * 提供应用配置和用户偏好设置
 */
@Composable
fun SettingsScreen() {
    var settings by remember { mutableStateOf(AppSettings()) }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "设置",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        item {
            SettingsSection(title = "通用设置")
        }
        
        item {
            SwitchSettingItem(
                icon = Icons.Default.Notifications,
                title = "推送通知",
                subtitle = "接收应用通知和提醒",
                checked = settings.notificationsEnabled,
                onCheckedChange = { 
                    settings = settings.copy(notificationsEnabled = it)
                }
            )
        }
        
        item {
            SwitchSettingItem(
                icon = Icons.Default.DarkMode,
                title = "深色模式",
                subtitle = "使用深色主题",
                checked = settings.darkModeEnabled,
                onCheckedChange = { 
                    settings = settings.copy(darkModeEnabled = it)
                }
            )
        }
        
        item {
            DropdownSettingItem(
                icon = Icons.Default.Language,
                title = "语言",
                subtitle = "选择应用语言",
                currentValue = settings.language,
                options = listOf("中文", "English", "日本語"),
                onValueChange = { 
                    settings = settings.copy(language = it)
                }
            )
        }
        
        item {
            SliderSettingItem(
                icon = Icons.Default.VolumeUp,
                title = "音量",
                subtitle = "调整应用音量",
                value = settings.volume,
                onValueChange = { 
                    settings = settings.copy(volume = it)
                }
            )
        }
        
        item {
            SettingsSection(title = "隐私与安全")
        }
        
        item {
            SwitchSettingItem(
                icon = Icons.Default.Fingerprint,
                title = "生物识别",
                subtitle = "使用指纹或面部识别",
                checked = settings.biometricEnabled,
                onCheckedChange = { 
                    settings = settings.copy(biometricEnabled = it)
                }
            )
        }
        
        item {
            SwitchSettingItem(
                icon = Icons.Default.LocationOn,
                title = "位置服务",
                subtitle = "允许应用访问位置信息",
                checked = settings.locationEnabled,
                onCheckedChange = { 
                    settings = settings.copy(locationEnabled = it)
                }
            )
        }
        
        item {
            SwitchSettingItem(
                icon = Icons.Default.Analytics,
                title = "数据分析",
                subtitle = "帮助改善应用体验",
                checked = settings.analyticsEnabled,
                onCheckedChange = { 
                    settings = settings.copy(analyticsEnabled = it)
                }
            )
        }
        
        item {
            SettingsSection(title = "存储与同步")
        }
        
        item {
            SwitchSettingItem(
                icon = Icons.Default.CloudSync,
                title = "自动同步",
                subtitle = "自动同步数据到云端",
                checked = settings.autoSyncEnabled,
                onCheckedChange = { 
                    settings = settings.copy(autoSyncEnabled = it)
                }
            )
        }
        
        item {
            DropdownSettingItem(
                icon = Icons.Default.Storage,
                title = "缓存大小",
                subtitle = "设置本地缓存限制",
                currentValue = settings.cacheSize,
                options = listOf("100MB", "500MB", "1GB", "2GB"),
                onValueChange = { 
                    settings = settings.copy(cacheSize = it)
                }
            )
        }
        
        item {
            ActionSettingItem(
                icon = Icons.Default.Delete,
                title = "清除缓存",
                subtitle = "删除临时文件和缓存",
                onClick = { /* 清除缓存逻辑 */ }
            )
        }
        
        item {
            SettingsSection(title = "关于")
        }
        
        item {
            ActionSettingItem(
                icon = Icons.Default.Info,
                title = "版本信息",
                subtitle = "Unify Core v1.0.0",
                onClick = { /* 显示版本详情 */ }
            )
        }
        
        item {
            ActionSettingItem(
                icon = Icons.Default.Help,
                title = "帮助与支持",
                subtitle = "获取帮助和技术支持",
                onClick = { /* 打开帮助页面 */ }
            )
        }
        
        item {
            ActionSettingItem(
                icon = Icons.Default.Feedback,
                title = "反馈建议",
                subtitle = "向我们发送反馈",
                onClick = { /* 打开反馈页面 */ }
            )
        }
    }
}

@Composable
private fun SettingsSection(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun SwitchSettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
private fun DropdownSettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    currentValue: String,
    options: List<String>,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { expanded = true }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = currentValue,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "展开",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun SliderSettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Text(
                    text = "${(value * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Slider(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ActionSettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "前往",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

data class AppSettings(
    val notificationsEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false,
    val language: String = "中文",
    val volume: Float = 0.8f,
    val biometricEnabled: Boolean = false,
    val locationEnabled: Boolean = true,
    val analyticsEnabled: Boolean = true,
    val autoSyncEnabled: Boolean = true,
    val cacheSize: String = "500MB"
)
