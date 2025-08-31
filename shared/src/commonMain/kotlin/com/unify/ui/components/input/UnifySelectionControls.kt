package com.unify.ui.components.input

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.unify.ui.LocalUnifyTheme
import com.unify.ui.LocalUnifyPlatformTheme
import com.unify.ui.components.foundation.UnifyText
import com.unify.ui.components.foundation.UnifyTextVariant

/**
 * Unify Selection Controls 组件
 * 支持多平台适配的统一选择控件组件，参考 KuiklyUI 设计规范
 */

/**
 * 选择控件尺寸枚举
 */
enum class UnifySelectionSize {
    SMALL,          // 小尺寸
    MEDIUM,         // 中等尺寸
    LARGE           // 大尺寸
}

/**
 * 选择控件状态枚举
 */
enum class UnifySelectionState {
    NORMAL,         // 正常
    SUCCESS,        // 成功
    WARNING,        // 警告
    ERROR           // 错误
}

/**
 * Checkbox 组件
 */
@Composable
fun UnifyCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: UnifySelectionSize = UnifySelectionSize.MEDIUM,
    state: UnifySelectionState = UnifySelectionState.NORMAL,
    colors: CheckboxColors? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    val actualColors = colors ?: getCheckboxColors(state, theme)
    
    Checkbox(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier
            .size(getSelectionControlSize(size))
            .semantics {
                contentDescription?.let { 
                    this.contentDescription = it 
                }
            },
        enabled = enabled,
        colors = actualColors,
        interactionSource = interactionSource
    )
}

/**
 * 带标签的 Checkbox 组件
 */
@Composable
fun UnifyCheckboxWithLabel(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: UnifySelectionSize = UnifySelectionSize.MEDIUM,
    state: UnifySelectionState = UnifySelectionState.NORMAL,
    colors: CheckboxColors? = null,
    labelVariant: UnifyTextVariant = UnifyTextVariant.BODY_MEDIUM,
    spacing: Dp = 8.dp,
    contentDescription: String? = null
) {
    Row(
        modifier = modifier
            .selectable(
                selected = checked,
                onClick = { onCheckedChange?.invoke(!checked) },
                enabled = enabled,
                role = Role.Checkbox
            )
            .semantics {
                contentDescription?.let { 
                    this.contentDescription = it 
                }
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        UnifyCheckbox(
            checked = checked,
            onCheckedChange = null, // 由外层处理
            enabled = enabled,
            size = size,
            state = state,
            colors = colors
        )
        
        Spacer(modifier = Modifier.width(spacing))
        
        UnifyText(
            text = label,
            variant = labelVariant,
            color = if (enabled) Color.Unspecified else LocalUnifyTheme.current.colors.disabled
        )
    }
}

/**
 * Radio Button 组件
 */
@Composable
fun UnifyRadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: UnifySelectionSize = UnifySelectionSize.MEDIUM,
    state: UnifySelectionState = UnifySelectionState.NORMAL,
    colors: RadioButtonColors? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    val actualColors = colors ?: getRadioButtonColors(state, theme)
    
    RadioButton(
        selected = selected,
        onClick = onClick,
        modifier = modifier
            .size(getSelectionControlSize(size))
            .semantics {
                contentDescription?.let { 
                    this.contentDescription = it 
                }
            },
        enabled = enabled,
        colors = actualColors,
        interactionSource = interactionSource
    )
}

/**
 * 带标签的 Radio Button 组件
 */
@Composable
fun UnifyRadioButtonWithLabel(
    selected: Boolean,
    onClick: (() -> Unit)?,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: UnifySelectionSize = UnifySelectionSize.MEDIUM,
    state: UnifySelectionState = UnifySelectionState.NORMAL,
    colors: RadioButtonColors? = null,
    labelVariant: UnifyTextVariant = UnifyTextVariant.BODY_MEDIUM,
    spacing: Dp = 8.dp,
    contentDescription: String? = null
) {
    Row(
        modifier = modifier
            .selectable(
                selected = selected,
                onClick = { onClick?.invoke() },
                enabled = enabled,
                role = Role.RadioButton
            )
            .semantics {
                contentDescription?.let { 
                    this.contentDescription = it 
                }
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        UnifyRadioButton(
            selected = selected,
            onClick = null, // 由外层处理
            enabled = enabled,
            size = size,
            state = state,
            colors = colors
        )
        
        Spacer(modifier = Modifier.width(spacing))
        
        UnifyText(
            text = label,
            variant = labelVariant,
            color = if (enabled) Color.Unspecified else LocalUnifyTheme.current.colors.disabled
        )
    }
}

/**
 * Radio Group 组件
 */
@Composable
fun <T> UnifyRadioGroup(
    options: List<T>,
    selectedOption: T?,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: UnifySelectionSize = UnifySelectionSize.MEDIUM,
    state: UnifySelectionState = UnifySelectionState.NORMAL,
    labelProvider: (T) -> String,
    labelVariant: UnifyTextVariant = UnifyTextVariant.BODY_MEDIUM,
    spacing: Dp = 8.dp,
    arrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp),
    contentDescription: String? = null
) {
    Column(
        modifier = modifier
            .selectableGroup()
            .semantics {
                contentDescription?.let { 
                    this.contentDescription = it 
                }
            },
        verticalArrangement = arrangement
    ) {
        options.forEach { option ->
            UnifyRadioButtonWithLabel(
                selected = selectedOption == option,
                onClick = { onOptionSelected(option) },
                label = labelProvider(option),
                enabled = enabled,
                size = size,
                state = state,
                labelVariant = labelVariant,
                spacing = spacing
            )
        }
    }
}

/**
 * Switch 组件
 */
@Composable
fun UnifySwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    thumbContent: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    size: UnifySelectionSize = UnifySelectionSize.MEDIUM,
    state: UnifySelectionState = UnifySelectionState.NORMAL,
    colors: SwitchColors? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    val actualColors = colors ?: getSwitchColors(state, theme)
    
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier
            .semantics {
                contentDescription?.let { 
                    this.contentDescription = it 
                }
            },
        thumbContent = thumbContent,
        enabled = enabled,
        colors = actualColors,
        interactionSource = interactionSource
    )
}

/**
 * 带标签的 Switch 组件
 */
@Composable
fun UnifySwitchWithLabel(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    label: String,
    modifier: Modifier = Modifier,
    thumbContent: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    size: UnifySelectionSize = UnifySelectionSize.MEDIUM,
    state: UnifySelectionState = UnifySelectionState.NORMAL,
    colors: SwitchColors? = null,
    labelVariant: UnifyTextVariant = UnifyTextVariant.BODY_MEDIUM,
    spacing: Dp = 8.dp,
    labelPosition: UnifyLabelPosition = UnifyLabelPosition.START,
    contentDescription: String? = null
) {
    Row(
        modifier = modifier.semantics {
            contentDescription?.let { 
                this.contentDescription = it 
            }
        },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (labelPosition == UnifyLabelPosition.START) {
            Arrangement.spacedBy(spacing)
        } else {
            Arrangement.spacedBy(spacing, Alignment.End)
        }
    ) {
        if (labelPosition == UnifyLabelPosition.START) {
            UnifyText(
                text = label,
                variant = labelVariant,
                color = if (enabled) Color.Unspecified else LocalUnifyTheme.current.colors.disabled,
                modifier = Modifier.weight(1f)
            )
            
            UnifySwitch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                thumbContent = thumbContent,
                enabled = enabled,
                size = size,
                state = state,
                colors = colors
            )
        } else {
            UnifySwitch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                thumbContent = thumbContent,
                enabled = enabled,
                size = size,
                state = state,
                colors = colors
            )
            
            UnifyText(
                text = label,
                variant = labelVariant,
                color = if (enabled) Color.Unspecified else LocalUnifyTheme.current.colors.disabled,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Slider 组件
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
    size: UnifySelectionSize = UnifySelectionSize.MEDIUM,
    state: UnifySelectionState = UnifySelectionState.NORMAL,
    colors: SliderColors? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    showValue: Boolean = false,
    valueFormatter: ((Float) -> String)? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    val actualColors = colors ?: getSliderColors(state, theme)
    
    Column(
        modifier = modifier.semantics {
            contentDescription?.let { 
                this.contentDescription = it 
            }
        }
    ) {
        if (showValue) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                UnifyText(
                    text = valueFormatter?.invoke(valueRange.start) ?: valueRange.start.toString(),
                    variant = UnifyTextVariant.CAPTION
                )
                UnifyText(
                    text = valueFormatter?.invoke(value) ?: value.toString(),
                    variant = UnifyTextVariant.BODY_MEDIUM
                )
                UnifyText(
                    text = valueFormatter?.invoke(valueRange.endInclusive) ?: valueRange.endInclusive.toString(),
                    variant = UnifyTextVariant.CAPTION
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            valueRange = valueRange,
            steps = steps,
            onValueChangeFinished = onValueChangeFinished,
            colors = actualColors,
            interactionSource = interactionSource
        )
    }
}

/**
 * Range Slider 组件
 */
@Composable
fun UnifyRangeSlider(
    value: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    size: UnifySelectionSize = UnifySelectionSize.MEDIUM,
    state: UnifySelectionState = UnifySelectionState.NORMAL,
    colors: SliderColors? = null,
    showValues: Boolean = false,
    valueFormatter: ((Float) -> String)? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    val actualColors = colors ?: getSliderColors(state, theme)
    
    Column(
        modifier = modifier.semantics {
            contentDescription?.let { 
                this.contentDescription = it 
            }
        }
    ) {
        if (showValues) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                UnifyText(
                    text = valueFormatter?.invoke(value.start) ?: value.start.toString(),
                    variant = UnifyTextVariant.BODY_MEDIUM
                )
                UnifyText(
                    text = valueFormatter?.invoke(value.endInclusive) ?: value.endInclusive.toString(),
                    variant = UnifyTextVariant.BODY_MEDIUM
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
        
        RangeSlider(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            valueRange = valueRange,
            steps = steps,
            onValueChangeFinished = onValueChangeFinished,
            colors = actualColors
        )
    }
}

/**
 * 标签位置枚举
 */
enum class UnifyLabelPosition {
    START,
    END
}

/**
 * 获取选择控件尺寸
 */
private fun getSelectionControlSize(size: UnifySelectionSize): Dp {
    return when (size) {
        UnifySelectionSize.SMALL -> 16.dp
        UnifySelectionSize.MEDIUM -> 20.dp
        UnifySelectionSize.LARGE -> 24.dp
    }
}

/**
 * 获取 Checkbox 颜色
 */
@Composable
private fun getCheckboxColors(
    state: UnifySelectionState,
    theme: com.unify.ui.theme.UnifyTheme
): CheckboxColors {
    return when (state) {
        UnifySelectionState.SUCCESS -> CheckboxDefaults.colors(
            checkedColor = theme.colors.success,
            uncheckedColor = theme.colors.outline
        )
        UnifySelectionState.WARNING -> CheckboxDefaults.colors(
            checkedColor = theme.colors.warning,
            uncheckedColor = theme.colors.outline
        )
        UnifySelectionState.ERROR -> CheckboxDefaults.colors(
            checkedColor = theme.colors.error,
            uncheckedColor = theme.colors.outline
        )
        else -> CheckboxDefaults.colors()
    }
}

/**
 * 获取 RadioButton 颜色
 */
@Composable
private fun getRadioButtonColors(
    state: UnifySelectionState,
    theme: com.unify.ui.theme.UnifyTheme
): RadioButtonColors {
    return when (state) {
        UnifySelectionState.SUCCESS -> RadioButtonDefaults.colors(
            selectedColor = theme.colors.success,
            unselectedColor = theme.colors.outline
        )
        UnifySelectionState.WARNING -> RadioButtonDefaults.colors(
            selectedColor = theme.colors.warning,
            unselectedColor = theme.colors.outline
        )
        UnifySelectionState.ERROR -> RadioButtonDefaults.colors(
            selectedColor = theme.colors.error,
            unselectedColor = theme.colors.outline
        )
        else -> RadioButtonDefaults.colors()
    }
}

/**
 * 获取 Switch 颜色
 */
@Composable
private fun getSwitchColors(
    state: UnifySelectionState,
    theme: com.unify.ui.theme.UnifyTheme
): SwitchColors {
    return when (state) {
        UnifySelectionState.SUCCESS -> SwitchDefaults.colors(
            checkedThumbColor = theme.colors.success,
            checkedTrackColor = theme.colors.success.copy(alpha = 0.5f)
        )
        UnifySelectionState.WARNING -> SwitchDefaults.colors(
            checkedThumbColor = theme.colors.warning,
            checkedTrackColor = theme.colors.warning.copy(alpha = 0.5f)
        )
        UnifySelectionState.ERROR -> SwitchDefaults.colors(
            checkedThumbColor = theme.colors.error,
            checkedTrackColor = theme.colors.error.copy(alpha = 0.5f)
        )
        else -> SwitchDefaults.colors()
    }
}

/**
 * 获取 Slider 颜色
 */
@Composable
private fun getSliderColors(
    state: UnifySelectionState,
    theme: com.unify.ui.theme.UnifyTheme
): SliderColors {
    return when (state) {
        UnifySelectionState.SUCCESS -> SliderDefaults.colors(
            thumbColor = theme.colors.success,
            activeTrackColor = theme.colors.success
        )
        UnifySelectionState.WARNING -> SliderDefaults.colors(
            thumbColor = theme.colors.warning,
            activeTrackColor = theme.colors.warning
        )
        UnifySelectionState.ERROR -> SliderDefaults.colors(
            thumbColor = theme.colors.error,
            activeTrackColor = theme.colors.error
        )
        else -> SliderDefaults.colors()
    }
}
