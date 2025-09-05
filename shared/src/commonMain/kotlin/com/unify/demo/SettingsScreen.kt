@file:OptIn(ExperimentalMaterial3Api::class)

package com.unify.demo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.unify.core.components.UnifySection
import com.unify.ui.components.input.UnifySwitchWithLabel
import com.unify.ui.components.input.UnifySliderWithLabel
import com.unify.ui.components.feedback.UnifyDialog

/**
 * Unify设置界面演示
 * 展示跨平台设置管理功能
 */

data class SettingCategory(
    val title: String,
    val icon: String,
    val settings: List<Setting>
)

sealed class Setting {
    abstract val id: String
    abstract val title: String
    abstract val description: String
    
    data class SwitchSetting(
        override val id: String,
        override val title: String,
        override val description: String,
        val value: Boolean
    ) : Setting()
    
    data class SliderSetting(
        override val id: String,
        override val title: String,
        override val description: String,
        val value: Float,
        val min: Float = 0f,
        val max: Float = 100f,
        val unit: String = ""
    ) : Setting()
    
    data class SelectionSetting(
        override val id: String,
        override val title: String,
        override val description: String,
        val options: List<String>,
        val selectedIndex: Int
    ) : Setting()
    
    data class ActionSetting(
        override val id: String,
        override val title: String,
        override val description: String,
        val actionText: String = "执行"
    ) : Setting()
}

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    var settingCategories by remember { mutableStateOf(getDefaultSettingCategories()) }
    var showResetDialog by remember { mutableStateOf(false) }
    
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SettingsHeader()
        }
        
        items(settingCategories) { category ->
            SettingCategoryCard(
                category = category,
                onSettingChanged = { settingId, newValue ->
                    settingCategories = updateSetting(settingCategories, settingId, newValue)
                }
            )
        }
        
        item {
            SettingsFooter(
                onResetSettings = { showResetDialog = true },
                onExportSettings = { /* 导出设置 */ },
                onImportSettings = { /* 导入设置 */ }
            )
        }
    }
    
    if (showResetDialog) {
        UnifyDialog(
            onDismissRequest = { showResetDialog = false }
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "重置设置",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "确定要重置所有设置到默认值吗？此操作无法撤销。",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = { showResetDialog = false },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("取消")
                    }
                    
                    Button(
                        onClick = {
                            settingCategories = getDefaultSettingCategories()
                            showResetDialog = false
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("重置")
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsHeader(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "⚙️",
                style = MaterialTheme.typography.displayMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "应用设置",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Text(
                text = "自定义您的应用体验",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun SettingCategoryCard(
    category: SettingCategory,
    onSettingChanged: (String, Any) -> Unit,
    modifier: Modifier = Modifier
) {
    UnifySection(
        title = "${category.icon} ${category.title}",
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            category.settings.forEach { setting ->
                SettingItem(
                    setting = setting,
                    onChanged = { newValue ->
                        onSettingChanged(setting.id, newValue)
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingItem(
    setting: Setting,
    onChanged: (Any) -> Unit,
    modifier: Modifier = Modifier
) {
    when (setting) {
        is Setting.SwitchSetting -> {
            UnifySwitchWithLabel(
                checked = setting.value,
                onCheckedChange = onChanged,
                label = setting.title,
                description = setting.description,
                modifier = modifier
            )
        }
        
        is Setting.SliderSetting -> {
            UnifySliderWithLabel(
                value = setting.value,
                onValueChange = onChanged,
                label = setting.title,
                valueRange = setting.min..setting.max,
                valueFormatter = { "${it.toInt()}${setting.unit}" },
                modifier = modifier
            )
        }
        
        is Setting.SelectionSetting -> {
            var expanded by remember { mutableStateOf(false) }
            
            Column(modifier = modifier) {
                Text(
                    text = setting.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = setting.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = setting.options[setting.selectedIndex],
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        setting.options.forEachIndexed { index, option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    onChanged(index)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
        
        is Setting.ActionSetting -> {
            Column(modifier = modifier) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = setting.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = setting.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    OutlinedButton(
                        onClick = { onChanged(Unit) }
                    ) {
                        Text(setting.actionText)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsFooter(
    onResetSettings: () -> Unit,
    onExportSettings: () -> Unit,
    onImportSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "设置管理",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onExportSettings,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("导出")
                    }
                    
                    OutlinedButton(
                        onClick = onImportSettings,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("导入")
                    }
                }
                
                Button(
                    onClick = onResetSettings,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("重置所有设置")
                }
            }
        }
    }
}

private fun getDefaultSettingCategories(): List<SettingCategory> {
    return listOf(
        SettingCategory(
            title = "通用",
            icon = "🔧",
            settings = listOf(
                Setting.SwitchSetting(
                    id = "notifications",
                    title = "推送通知",
                    description = "接收应用推送消息",
                    value = true
                ),
                Setting.SwitchSetting(
                    id = "dark_mode",
                    title = "深色模式",
                    description = "使用深色主题界面",
                    value = false
                ),
                Setting.SelectionSetting(
                    id = "language",
                    title = "语言",
                    description = "选择应用显示语言",
                    options = listOf("简体中文", "English", "日本語", "한국어"),
                    selectedIndex = 0
                )
            )
        ),
        SettingCategory(
            title = "性能",
            icon = "⚡",
            settings = listOf(
                Setting.SliderSetting(
                    id = "animation_speed",
                    title = "动画速度",
                    description = "调整界面动画播放速度",
                    value = 100f,
                    min = 50f,
                    max = 200f,
                    unit = "%"
                ),
                Setting.SwitchSetting(
                    id = "hardware_acceleration",
                    title = "硬件加速",
                    description = "启用GPU硬件加速",
                    value = true
                ),
                Setting.SwitchSetting(
                    id = "reduce_animations",
                    title = "减少动画",
                    description = "减少界面动画以提升性能",
                    value = false
                )
            )
        ),
        SettingCategory(
            title = "隐私",
            icon = "🔒",
            settings = listOf(
                Setting.SwitchSetting(
                    id = "analytics",
                    title = "数据分析",
                    description = "允许收集匿名使用数据",
                    value = true
                ),
                Setting.SwitchSetting(
                    id = "crash_reports",
                    title = "崩溃报告",
                    description = "自动发送崩溃报告",
                    value = true
                ),
                Setting.ActionSetting(
                    id = "clear_data",
                    title = "清除数据",
                    description = "删除所有本地存储的数据",
                    actionText = "清除"
                )
            )
        ),
        SettingCategory(
            title = "关于",
            icon = "ℹ️",
            settings = listOf(
                Setting.ActionSetting(
                    id = "version_info",
                    title = "版本信息",
                    description = "查看应用版本和构建信息",
                    actionText = "查看"
                ),
                Setting.ActionSetting(
                    id = "licenses",
                    title = "开源许可",
                    description = "查看第三方库许可信息",
                    actionText = "查看"
                ),
                Setting.ActionSetting(
                    id = "feedback",
                    title = "反馈建议",
                    description = "向开发团队发送反馈",
                    actionText = "发送"
                )
            )
        )
    )
}

private fun updateSetting(
    categories: List<SettingCategory>,
    settingId: String,
    newValue: Any
): List<SettingCategory> {
    return categories.map { category ->
        category.copy(
            settings = category.settings.map { setting ->
                if (setting.id == settingId) {
                    when (setting) {
                        is Setting.SwitchSetting -> setting.copy(value = newValue as Boolean)
                        is Setting.SliderSetting -> setting.copy(value = newValue as Float)
                        is Setting.SelectionSetting -> setting.copy(selectedIndex = newValue as Int)
                        is Setting.ActionSetting -> {
                            // 处理动作设置的点击
                            setting
                        }
                    }
                } else {
                    setting
                }
            }
        )
    }
}
