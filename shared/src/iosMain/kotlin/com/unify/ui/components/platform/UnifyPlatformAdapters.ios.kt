package com.unify.ui.components.platform

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import platform.UIKit.*
import platform.Foundation.*

/**
 * iOS平台特定组件适配器
 * 集成iOS原生UI元素和功能
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
                PlatformButtonStyle.PRIMARY -> Color(0xFF007AFF) // iOS Blue
                PlatformButtonStyle.SECONDARY -> Color(0xFF8E8E93) // iOS Gray
                PlatformButtonStyle.DESTRUCTIVE -> Color(0xFFFF3B30) // iOS Red
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
            focusedBorderColor = Color(0xFF007AFF),
            unfocusedBorderColor = Color(0xFFE5E5EA)
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
            checkedTrackColor = Color(0xFF34C759), // iOS Green
            uncheckedThumbColor = Color.White,
            uncheckedTrackColor = Color(0xFFE5E5EA)
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
            thumbColor = Color.White,
            activeTrackColor = Color(0xFF007AFF),
            inactiveTrackColor = Color(0xFFE5E5EA)
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
            color = Color(0xFF007AFF),
            trackColor = Color(0xFFE5E5EA)
        )
        if (showPercentage) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF8E8E93)
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
        containerColor = Color(0xFFF2F2F7), // iOS Background
        titleContentColor = Color.Black,
        textContentColor = Color(0xFF8E8E93)
    )
}

@Composable
actual fun UnifyPlatformActionSheet(
    items: List<ActionSheetItem>,
    onItemSelected: (ActionSheetItem) -> Unit,
    onDismiss: () -> Unit,
    title: String?
) {
    // iOS风格的ActionSheet实现
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF8E8E93),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        items.forEach { item ->
            TextButton(
                onClick = { onItemSelected(item) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = when (item.style) {
                        ActionSheetItemStyle.DEFAULT -> Color(0xFF007AFF)
                        ActionSheetItemStyle.DESTRUCTIVE -> Color(0xFFFF3B30)
                        ActionSheetItemStyle.CANCEL -> Color(0xFF8E8E93)
                    }
                )
            ) {
                Text(item.title)
            }
        }
        
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("取消", color = Color(0xFF8E8E93))
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
    // iOS风格的分段控制器
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = index == selectedIndex
            TextButton(
                onClick = { onSelectionChange(index) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.textButtonColors(
                    containerColor = if (isSelected) Color(0xFF007AFF) else Color.Transparent,
                    contentColor = if (isSelected) Color.White else Color(0xFF007AFF)
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
    // iOS风格的日期选择器
    Column(modifier = modifier) {
        Text(
            text = "选择日期",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )
        
        // 这里应该集成iOS原生的UIDatePicker
        // 暂时使用Material Design的实现
        Button(
            onClick = {
                // 触发iOS原生日期选择器
                val currentDate = NSDate()
                onDateSelected(currentDate.timeIntervalSince1970.toLong() * 1000)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF007AFF)
            )
        ) {
            Text("选择日期")
        }
    }
}
