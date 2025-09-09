package com.unify.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
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
 * Unify选择控件组件
 * 提供多种选择和控制组件
 */

/**
 * 增强型复选框
 */
@Composable
fun UnifyEnhancedCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    description: String? = null,
    colors: CheckboxColors = CheckboxDefaults.colors(),
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .selectable(
                    selected = checked,
                    onClick = { onCheckedChange(!checked) },
                    enabled = enabled,
                    role = Role.Checkbox,
                )
                .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = colors,
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color =
                    if (enabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    },
            )

            description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color =
                        if (enabled) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                        },
                )
            }
        }
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
    title: String? = null,
    maxSelections: Int? = null,
) {
    Column(modifier = modifier) {
        title?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        Card(
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                options.forEach { option ->
                    val isSelected = option.value in selectedOptions
                    val canSelect =
                        maxSelections == null ||
                            selectedOptions.size < maxSelections ||
                            isSelected

                    UnifyEnhancedCheckbox(
                        checked = isSelected,
                        onCheckedChange = { checked ->
                            val newSelection =
                                if (checked) {
                                    selectedOptions + option.value
                                } else {
                                    selectedOptions - option.value
                                }
                            onSelectionChange(newSelection)
                        },
                        label = option.label,
                        description = option.description,
                        enabled = canSelect && option.enabled,
                    )
                }

                maxSelections?.let { max ->
                    Text(
                        text = "已选择 ${selectedOptions.size}/$max",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp),
                    )
                }
            }
        }
    }
}

/**
 * 增强型单选按钮
 */
@Composable
fun UnifyEnhancedRadioButton(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    description: String? = null,
    colors: RadioButtonColors = RadioButtonDefaults.colors(),
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .selectable(
                    selected = selected,
                    onClick = onClick,
                    enabled = enabled,
                    role = Role.RadioButton,
                )
                .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            enabled = enabled,
            colors = colors,
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color =
                    if (enabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    },
            )

            description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color =
                        if (enabled) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                        },
                )
            }
        }
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
    title: String? = null,
) {
    Column(
        modifier = modifier.selectableGroup(),
    ) {
        title?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        Card(
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                options.forEach { option ->
                    UnifyEnhancedRadioButton(
                        selected = option.value == selectedOption,
                        onClick = { onSelectionChange(option.value) },
                        label = option.label,
                        description = option.description,
                        enabled = option.enabled,
                    )
                }
            }
        }
    }
}

/**
 * 切换开关
 */
@Composable
fun UnifyToggleSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    description: String? = null,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color =
                    if (enabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    },
            )

            description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color =
                        if (enabled) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                        },
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
        )
    }
}

/**
 * 分段选择器
 */
@Composable
fun UnifySegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelectionChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline,
                    RoundedCornerShape(8.dp),
                ),
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = index == selectedIndex

            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .background(
                            if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                Color.Transparent
                            },
                        )
                        .selectable(
                            selected = isSelected,
                            onClick = { onSelectionChange(index) },
                            enabled = enabled,
                        )
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = option,
                    style = MaterialTheme.typography.bodyMedium,
                    color =
                        if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                )
            }
        }
    }
}

/**
 * 滑块控件
 */
@Composable
fun UnifySliderControl(
    value: Float,
    onValueChange: (Float) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    showValue: Boolean = true,
    valueFormatter: (Float) -> String = { "${(it * 100).toInt()}%" },
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )

            if (showValue) {
                Text(
                    text = valueFormatter(value),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Slider(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * 范围滑块
 */
@Composable
fun UnifyRangeSlider(
    values: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    showValues: Boolean = true,
    valueFormatter: (Float) -> String = { "${(it * 100).toInt()}%" },
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )

            if (showValues) {
                Text(
                    text = "${valueFormatter(values.start)} - ${valueFormatter(values.endInclusive)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        RangeSlider(
            value = values,
            onValueChange = onValueChange,
            enabled = enabled,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * 多选列表
 */
@Composable
fun UnifyMultiSelectList(
    items: List<SelectableItem>,
    selectedItems: Set<String>,
    onSelectionChange: (Set<String>) -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    searchable: Boolean = false,
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredItems =
        remember(items, searchQuery) {
            if (searchQuery.isEmpty()) {
                items
            } else {
                items.filter { it.label.contains(searchQuery, ignoreCase = true) }
            }
        }

    Column(modifier = modifier) {
        title?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        if (searchable) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("搜索") },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
            )
        }

        Card(
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
        ) {
            LazyColumn(
                modifier = Modifier.heightIn(max = 300.dp),
            ) {
                items(filteredItems) { item ->
                    val isSelected = item.id in selectedItems

                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = isSelected,
                                    onClick = {
                                        val newSelection =
                                            if (isSelected) {
                                                selectedItems - item.id
                                            } else {
                                                selectedItems + item.id
                                            }
                                        onSelectionChange(newSelection)
                                    },
                                )
                                .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = null,
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.bodyMedium,
                            )

                            item.description?.let { desc ->
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }

        Text(
            text = "已选择 ${selectedItems.size} 项",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

// 数据类
data class CheckboxOption(
    val value: String,
    val label: String,
    val description: String? = null,
    val enabled: Boolean = true,
)

data class RadioOption(
    val value: String,
    val label: String,
    val description: String? = null,
    val enabled: Boolean = true,
)

data class SelectableItem(
    val id: String,
    val label: String,
    val description: String? = null,
    val enabled: Boolean = true,
)
