package com.unify.core.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.unify.core.utils.UnifyPlatformUtils
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Unify统一选择器组件
 * 100% Kotlin Compose语法实现
 */
data class UnifyPickerItem<T>(
    val value: T,
    val label: String,
    val enabled: Boolean = true,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> UnifyDropdownPicker(
    selectedItem: UnifyPickerItem<T>?,
    items: List<UnifyPickerItem<T>>,
    onItemSelected: (UnifyPickerItem<T>) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "请选择",
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it && enabled },
        modifier = modifier,
    ) {
        UnifyTextField(
            value = selectedItem?.label ?: "",
            onValueChange = { },
            label = label,
            placeholder = placeholder,
            readOnly = true,
            enabled = enabled,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = item.label,
                            color =
                                if (item.enabled) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                },
                        )
                    },
                    onClick = {
                        if (item.enabled) {
                            onItemSelected(item)
                            expanded = false
                        }
                    },
                    enabled = item.enabled,
                    trailingIcon =
                        if (selectedItem == item) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        } else {
                            null
                        },
                )
            }
        }
    }
}

@Composable
fun <T> UnifyRadioGroup(
    selectedItem: UnifyPickerItem<T>?,
    items: List<UnifyPickerItem<T>>,
    onItemSelected: (UnifyPickerItem<T>) -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    arrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp),
) {
    Column(
        modifier = modifier,
        verticalArrangement = arrangement,
    ) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        items.forEach { item ->
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable(enabled = item.enabled) {
                            onItemSelected(item)
                        }
                        .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                RadioButton(
                    selected = selectedItem == item,
                    onClick = {
                        if (item.enabled) {
                            onItemSelected(item)
                        }
                    },
                    enabled = item.enabled,
                )

                Text(
                    text = item.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color =
                        if (item.enabled) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
fun <T> UnifyCheckboxGroup(
    selectedItems: List<UnifyPickerItem<T>>,
    items: List<UnifyPickerItem<T>>,
    onItemsChanged: (List<UnifyPickerItem<T>>) -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    arrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp),
) {
    Column(
        modifier = modifier,
        verticalArrangement = arrangement,
    ) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        items.forEach { item ->
            val isSelected = selectedItems.contains(item)

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable(enabled = item.enabled) {
                            val newSelection =
                                if (isSelected) {
                                    selectedItems - item
                                } else {
                                    selectedItems + item
                                }
                            onItemsChanged(newSelection)
                        }
                        .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { checked ->
                        if (item.enabled) {
                            val newSelection =
                                if (checked) {
                                    selectedItems + item
                                } else {
                                    selectedItems - item
                                }
                            onItemsChanged(newSelection)
                        }
                    },
                    enabled = item.enabled,
                )

                Text(
                    text = item.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color =
                        if (item.enabled) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
fun UnifySlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    showValue: Boolean = true,
    valueFormatter: (Float) -> String = { UnifyPlatformUtils.formatFloat(it, 1) },
    label: String? = null,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (label != null || showValue) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (label != null) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                if (showValue) {
                    Text(
                        text = valueFormatter(value),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            valueRange = valueRange,
            steps = steps,
        )
    }
}

@Composable
fun UnifySwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: String? = null,
    description: String? = null,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) {
                    onCheckedChange(!checked)
                }
                .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        if (label != null || description != null) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (label != null) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color =
                            if (enabled) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            },
                    )
                }

                if (description != null) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color =
                            if (enabled) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            },
                    )
                }
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
        )
    }
}

@Composable
fun UnifyWheelPicker(
    items: List<String>,
    selectedIndex: Int,
    onSelectionChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    visibleItemsCount: Int = 5,
) {
    val listState =
        rememberLazyListState(
            initialFirstVisibleItemIndex = (selectedIndex - visibleItemsCount / 2).coerceAtLeast(0),
        )

    LaunchedEffect(selectedIndex) {
        listState.animateScrollToItem(
            (selectedIndex - visibleItemsCount / 2).coerceAtLeast(0),
        )
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { firstVisibleIndex ->
                val centerIndex = firstVisibleIndex + visibleItemsCount / 2
                if (centerIndex in items.indices && centerIndex != selectedIndex) {
                    onSelectionChanged(centerIndex)
                }
            }
    }

    Box(
        modifier = modifier.height((visibleItemsCount * 48).dp),
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
        ) {
            // 添加顶部填充项
            items(visibleItemsCount / 2) {
                Box(modifier = Modifier.height(48.dp))
            }

            items(items.size) { index ->
                val isSelected = index == selectedIndex

                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clickable { onSelectionChanged(index) }
                            .alpha(if (isSelected) 1f else 0.6f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = items[index],
                        style =
                            if (isSelected) {
                                MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                )
                            } else {
                                MaterialTheme.typography.bodyMedium
                            },
                        color =
                            if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        textAlign = TextAlign.Center,
                    )
                }
            }

            // 添加底部填充项
            items(visibleItemsCount / 2) {
                Box(modifier = Modifier.height(48.dp))
            }
        }

        // 选择指示器
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .align(Alignment.Center),
        ) {
            UnifyDivider(
                modifier = Modifier.align(Alignment.TopCenter),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            )
            UnifyDivider(
                modifier = Modifier.align(Alignment.BottomCenter),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            )
        }
    }
}
