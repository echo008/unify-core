package com.unify.ui.input

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Unify复选框组件
 */
@Composable
fun UnifyCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: CheckboxColors = CheckboxDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    Checkbox(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource
    )
}

/**
 * 带标签的复选框
 */
@Composable
fun UnifyCheckboxWithLabel(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: CheckboxColors = CheckboxDefaults.colors()
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                enabled = enabled,
                onClick = { onCheckedChange(!checked) }
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        UnifyCheckbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = colors
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (enabled) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            },
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * 复选框组
 */
@Composable
fun UnifyCheckboxGroup(
    options: List<CheckboxOption>,
    selectedOptions: Set<String>,
    onSelectionChange: (Set<String>) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    title: String? = null
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        title?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        options.forEach { option ->
            UnifyCheckboxWithLabel(
                checked = selectedOptions.contains(option.value),
                onCheckedChange = { isChecked ->
                    val newSelection = if (isChecked) {
                        selectedOptions + option.value
                    } else {
                        selectedOptions - option.value
                    }
                    onSelectionChange(newSelection)
                },
                label = option.label,
                enabled = enabled && option.enabled
            )
        }
    }
}

/**
 * Unify单选按钮组件
 */
@Composable
fun UnifyRadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: RadioButtonColors = RadioButtonDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    RadioButton(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource
    )
}

/**
 * 带标签的单选按钮
 */
@Composable
fun UnifyRadioButtonWithLabel(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: RadioButtonColors = RadioButtonDefaults.colors()
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                enabled = enabled,
                role = Role.RadioButton,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple()
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        UnifyRadioButton(
            selected = selected,
            onClick = onClick,
            enabled = enabled,
            colors = colors
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (enabled) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            },
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * 单选按钮组
 */
@Composable
fun UnifyRadioButtonGroup(
    options: List<RadioOption>,
    selectedOption: String?,
    onSelectionChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    title: String? = null
) {
    Column(
        modifier = modifier.selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        title?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        options.forEach { option ->
            UnifyRadioButtonWithLabel(
                selected = selectedOption == option.value,
                onClick = { onSelectionChange(option.value) },
                label = option.label,
                enabled = enabled && option.enabled
            )
        }
    }
}

/**
 * Unify开关组件
 */
@Composable
fun UnifySwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    thumbContent: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    colors: SwitchColors = SwitchDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        thumbContent = thumbContent,
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource
    )
}

/**
 * 带标签的开关
 */
@Composable
fun UnifySwitchWithLabel(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    enabled: Boolean = true,
    colors: SwitchColors = SwitchDefaults.colors()
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                enabled = enabled,
                onClick = { onCheckedChange(!checked) }
            )
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                }
            )
            
            description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        UnifySwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = colors
        )
    }
}

/**
 * 开关组
 */
@Composable
fun UnifySwitchGroup(
    options: List<SwitchOption>,
    switchStates: Map<String, Boolean>,
    onSwitchChange: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    title: String? = null
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        title?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        options.forEach { option ->
            UnifySwitchWithLabel(
                checked = switchStates[option.key] ?: false,
                onCheckedChange = { isChecked ->
                    onSwitchChange(option.key, isChecked)
                },
                label = option.label,
                description = option.description,
                enabled = enabled && option.enabled
            )
        }
    }
}

/**
 * 自定义选择器
 */
@Composable
fun UnifyCustomSelector(
    options: List<String>,
    selectedIndex: Int,
    onSelectionChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: SelectorStyle = SelectorStyle.SEGMENTED
) {
    when (style) {
        SelectorStyle.SEGMENTED -> {
            UnifySegmentedControl(
                options = options,
                selectedIndex = selectedIndex,
                onSelectionChange = onSelectionChange,
                modifier = modifier,
                enabled = enabled
            )
        }
        SelectorStyle.CHIPS -> {
            UnifyChipSelector(
                options = options,
                selectedIndex = selectedIndex,
                onSelectionChange = onSelectionChange,
                modifier = modifier,
                enabled = enabled
            )
        }
    }
}

/**
 * 分段控制器
 */
@Composable
private fun UnifySegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelectionChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(8.dp)
            )
            .padding(4.dp)
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = index == selectedIndex
            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) {
                    MaterialTheme.colorScheme.surface
                } else {
                    Color.Transparent
                },
                animationSpec = tween(200),
                label = "segment_background"
            )
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(backgroundColor)
                    .clickable(
                        enabled = enabled,
                        onClick = { onSelectionChange(index) }
                    )
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                    color = if (enabled) {
                        if (isSelected) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    }
                )
            }
        }
    }
}

/**
 * 芯片选择器
 */
@Composable
private fun UnifyChipSelector(
    options: List<String>,
    selectedIndex: Int,
    onSelectionChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = index == selectedIndex
            
            FilterChip(
                selected = isSelected,
                onClick = { onSelectionChange(index) },
                label = { Text(option) },
                enabled = enabled,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * 滑块组件
 */
@Composable
fun UnifySlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    colors: SliderColors = SliderDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        valueRange = valueRange,
        steps = steps,
        onValueChangeFinished = onValueChangeFinished,
        colors = colors,
        interactionSource = interactionSource
    )
}

/**
 * 带标签的滑块
 */
@Composable
fun UnifySliderWithLabel(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    enabled: Boolean = true,
    showValue: Boolean = true,
    valueFormatter: (Float) -> String = { "%.1f".format(it) }
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 标签和当前值
        if (label != null || showValue) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                label?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                if (showValue) {
                    Text(
                        text = valueFormatter(value),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        // 滑块
        UnifySlider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            enabled = enabled
        )
        
        // 范围标签
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = valueFormatter(valueRange.start),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = valueFormatter(valueRange.endInclusive),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// 数据类和枚举

data class CheckboxOption(
    val value: String,
    val label: String,
    val enabled: Boolean = true
)

data class RadioOption(
    val value: String,
    val label: String,
    val enabled: Boolean = true
)

data class SwitchOption(
    val key: String,
    val label: String,
    val description: String? = null,
    val enabled: Boolean = true
)

enum class SelectorStyle {
    SEGMENTED,
    CHIPS
}
