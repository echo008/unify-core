package com.unify.ui.components.platform

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLInputElement

/**
 * Web平台特定组件适配器
 * 集成Web原生元素和功能
 */

@Composable
actual fun UnifyPlatformButton(
    onClick: () -> Unit,
    modifier: Modifier,
    text: String,
    style: PlatformButtonStyle
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = when (style) {
                PlatformButtonStyle.PRIMARY -> Color(0xFF0066CC) // Web Blue
                PlatformButtonStyle.SECONDARY -> Color(0xFF6C757D) // Web Gray
                PlatformButtonStyle.DESTRUCTIVE -> Color(0xFFDC3545) // Web Red
            }
        )
    ) {
        Text(text)
    }
}

@Composable
actual fun UnifyPlatformTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier,
    placeholder: String,
    style: PlatformTextFieldStyle
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = { Text(placeholder) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF0066CC),
            unfocusedBorderColor = Color(0xFFCED4DA)
        )
    )
}

@Composable
actual fun UnifyPlatformSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = Color(0xFF28A745), // Web Green
            uncheckedThumbColor = Color.White,
            uncheckedTrackColor = Color(0xFFCED4DA)
        )
    )
}

@Composable
actual fun UnifyPlatformSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier,
    valueRange: ClosedFloatingPointRange<Float>
) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        valueRange = valueRange,
        colors = SliderDefaults.colors(
            thumbColor = Color(0xFF0066CC),
            activeTrackColor = Color(0xFF0066CC),
            inactiveTrackColor = Color(0xFFCED4DA)
        )
    )
}

@Composable
actual fun UnifyPlatformProgressBar(
    progress: Float,
    modifier: Modifier,
    showPercentage: Boolean
) {
    Column(modifier = modifier) {
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF0066CC),
            trackColor = Color(0xFFE9ECEF)
        )
        if (showPercentage) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6C757D)
            )
        }
    }
}

@Composable
actual fun UnifyPlatformAlert(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: (@Composable () -> Unit)?
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        containerColor = Color.White,
        titleContentColor = Color.Black,
        textContentColor = Color(0xFF6C757D)
    )
}

@Composable
actual fun UnifyPlatformActionSheet(
    items: List<ActionSheetItem>,
    onItemSelected: (ActionSheetItem) -> Unit,
    onDismiss: () -> Unit,
    title: String?
) {
    // Web风格的ActionSheet实现
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            items.forEach { item ->
                TextButton(
                    onClick = { onItemSelected(item) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = when (item.style) {
                            ActionSheetItemStyle.DEFAULT -> Color(0xFF0066CC)
                            ActionSheetItemStyle.DESTRUCTIVE -> Color(0xFFDC3545)
                            ActionSheetItemStyle.CANCEL -> Color(0xFF6C757D)
                        }
                    )
                ) {
                    Text(item.title)
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("取消", color = Color(0xFF6C757D))
            }
        }
    }
}

@Composable
actual fun UnifyPlatformSegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onSelectionChange: (Int) -> Unit,
    modifier: Modifier
) {
    // Web风格的分段控制器
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = index == selectedIndex
            OutlinedButton(
                onClick = { onSelectionChange(index) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isSelected) Color(0xFF0066CC) else Color.Transparent,
                    contentColor = if (isSelected) Color.White else Color(0xFF0066CC)
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0066CC)).brush
                )
            ) {
                Text(item)
            }
        }
    }
}

@Composable
actual fun UnifyPlatformDatePicker(
    selectedDate: Long,
    onDateSelected: (Long) -> Unit,
    modifier: Modifier
) {
    // Web风格的日期选择器
    Column(modifier = modifier) {
        Text(
            text = "选择日期",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )
        
        Button(
            onClick = {
                // 使用Web原生日期选择器
                val input = document.createElement("input") as HTMLInputElement
                input.type = "date"
                input.onchange = { event ->
                    val dateValue = (event.target as HTMLInputElement).value
                    val timestamp = js("new Date(dateValue).getTime()") as Long
                    onDateSelected(timestamp)
                }
                input.click()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0066CC)
            )
        ) {
            Text("选择日期")
        }
    }
}
