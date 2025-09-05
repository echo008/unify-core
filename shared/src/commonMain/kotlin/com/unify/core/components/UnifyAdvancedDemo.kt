package com.unify.core.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Unify高级组件演示应用
 * 展示图表、日历、选择器等高级组件
 */
@Composable
fun UnifyAdvancedDemo() {
    var selectedDate by remember { mutableStateOf<UnifyCalendarDate?>(null) }
    var selectedDropdownItem by remember { mutableStateOf<UnifyPickerItem<String>?>(null) }
    var selectedRadioItem by remember { mutableStateOf<UnifyPickerItem<String>?>(null) }
    var selectedCheckboxItems by remember { mutableStateOf<List<UnifyPickerItem<String>>>(emptyList()) }
    var sliderValue by remember { mutableStateOf(0.5f) }
    var switchValue by remember { mutableStateOf(false) }
    var wheelPickerIndex by remember { mutableStateOf(0) }
    
    val chartData = listOf(
        UnifyChartData("销售", 120f, Color.Blue),
        UnifyChartData("营销", 80f, Color.Green),
        UnifyChartData("开发", 150f, Color.Red),
        UnifyChartData("支持", 60f, Color(0xFFFFA500))
    )
    
    val dropdownItems = listOf(
        UnifyPickerItem("option1", "选项 1"),
        UnifyPickerItem("option2", "选项 2"),
        UnifyPickerItem("option3", "选项 3"),
        UnifyPickerItem("option4", "选项 4 (禁用)", enabled = false)
    )
    
    val radioItems = listOf(
        UnifyPickerItem("small", "小"),
        UnifyPickerItem("medium", "中"),
        UnifyPickerItem("large", "大")
    )
    
    val checkboxItems = listOf(
        UnifyPickerItem("feature1", "功能 1"),
        UnifyPickerItem("feature2", "功能 2"),
        UnifyPickerItem("feature3", "功能 3"),
        UnifyPickerItem("feature4", "功能 4")
    )
    
    val wheelItems = listOf("北京", "上海", "广州", "深圳", "杭州", "南京", "成都", "武汉")
    
    UnifyColumn(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        spacing = UnifySpacing.LARGE
    ) {
        // 标题
        Text(
            text = "📊 Unify 高级组件演示",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        // 图表演示
        UnifySection(title = "图表组件") {
            UnifyChart(
                data = chartData,
                type = UnifyChartType.BAR,
                title = "部门数据统计",
                modifier = Modifier.fillMaxWidth()
            )
            
            UnifyChart(
                data = chartData,
                type = UnifyChartType.PIE,
                title = "数据分布",
                modifier = Modifier.fillMaxWidth()
            )
            
            UnifyChart(
                data = chartData,
                type = UnifyChartType.LINE,
                title = "趋势分析",
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // 日历演示
        UnifySection(title = "日历组件") {
            UnifyDatePicker(
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it },
                label = "选择日期",
                modifier = Modifier.fillMaxWidth()
            )
            
            if (selectedDate != null) {
                Text(
                    text = "已选择: ${selectedDate!!.toDisplayString()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        // 选择器演示
        UnifySection(title = "下拉选择器") {
            UnifyDropdownPicker(
                selectedItem = selectedDropdownItem,
                items = dropdownItems,
                onItemSelected = { selectedDropdownItem = it },
                label = "选择选项",
                modifier = Modifier.fillMaxWidth()
            )
            
            if (selectedDropdownItem != null) {
                Text(
                    text = "已选择: ${selectedDropdownItem!!.label}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        // 单选组演示
        UnifySection(title = "单选按钮组") {
            UnifyRadioGroup(
                selectedItem = selectedRadioItem,
                items = radioItems,
                onItemSelected = { selectedRadioItem = it },
                title = "选择尺寸"
            )
        }
        
        // 多选组演示
        UnifySection(title = "复选框组") {
            UnifyCheckboxGroup(
                selectedItems = selectedCheckboxItems,
                items = checkboxItems,
                onItemsChanged = { selectedCheckboxItems = it },
                title = "选择功能"
            )
            
            if (selectedCheckboxItems.isNotEmpty()) {
                Text(
                    text = "已选择: ${selectedCheckboxItems.joinToString(", ") { it.label }}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        // 滑块演示
        UnifySection(title = "滑块组件") {
            UnifySlider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                label = "音量",
                valueRange = 0f..1f,
                showValue = true,
                valueFormatter = { "${(it * 100).toInt()}%" },
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // 开关演示
        UnifySection(title = "开关组件") {
            UnifySwitch(
                checked = switchValue,
                onCheckedChange = { switchValue = it },
                label = "启用通知",
                description = "接收应用推送通知"
            )
        }
        
        // 滚轮选择器演示
        UnifySection(title = "滚轮选择器") {
            UnifyCard {
                UnifyColumn(
                    modifier = Modifier.padding(16.dp),
                    spacing = UnifySpacing.MEDIUM
                ) {
                    Text(
                        text = "选择城市",
                        style = MaterialTheme.typography.titleSmall
                    )
                    
                    UnifyWheelPicker(
                        items = wheelItems,
                        selectedIndex = wheelPickerIndex,
                        onSelectionChanged = { wheelPickerIndex = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Text(
                        text = "已选择: ${wheelItems[wheelPickerIndex]}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        // 综合示例
        UnifySection(title = "综合示例") {
            UnifyCard {
                UnifyColumn(
                    modifier = Modifier.padding(16.dp),
                    spacing = UnifySpacing.MEDIUM
                ) {
                    Text(
                        text = "设置面板",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    UnifySwitch(
                        checked = true,
                        onCheckedChange = { },
                        label = "自动保存",
                        description = "自动保存您的工作进度"
                    )
                    
                    UnifyDivider()
                    
                    UnifySlider(
                        value = 0.7f,
                        onValueChange = { },
                        label = "界面缩放",
                        valueRange = 0.5f..2.0f,
                        valueFormatter = { "${(it * 100).toInt()}%" },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    UnifyDivider()
                    
                    UnifyDropdownPicker(
                        selectedItem = UnifyPickerItem("zh", "中文"),
                        items = listOf(
                            UnifyPickerItem("zh", "中文"),
                            UnifyPickerItem("en", "English"),
                            UnifyPickerItem("ja", "日本語")
                        ),
                        onItemSelected = { },
                        label = "语言设置",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
