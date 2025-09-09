package com.unify.ui.components.advanced

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Desktop平台Picker组件actual实现
 */

@Composable
actual fun UnifyPicker(
    items: List<PickerItem>,
    selectedItems: List<String>,
    onSelectionChanged: (List<String>) -> Unit,
    modifier: Modifier,
    pickerType: PickerType,
    title: String,
    placeholder: String,
    maxSelections: Int,
    searchEnabled: Boolean,
    showClearButton: Boolean,
) {
    var searchText by remember { mutableStateOf("") }
    val filteredItems =
        if (searchEnabled && searchText.isNotEmpty()) {
            items.filter { it.label.contains(searchText, ignoreCase = true) }
        } else {
            items
        }

    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
            )

            if (searchEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = { Text("搜索...") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "搜索")
                    },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(filteredItems) { item ->
                    val isSelected = selectedItems.contains(item.id)

                    when (pickerType) {
                        PickerType.SINGLE -> {
                            FilterChip(
                                onClick = {
                                    onSelectionChanged(listOf(item.id))
                                },
                                label = { Text(item.label) },
                                selected = isSelected,
                                enabled = item.enabled,
                            )
                        }
                        PickerType.MULTI -> {
                            FilterChip(
                                onClick = {
                                    val newSelection =
                                        if (isSelected) {
                                            selectedItems - item.id
                                        } else {
                                            if (selectedItems.size < maxSelections) {
                                                selectedItems + item.id
                                            } else {
                                                selectedItems
                                            }
                                        }
                                    onSelectionChanged(newSelection)
                                },
                                label = { Text(item.label) },
                                selected = isSelected,
                                enabled = item.enabled,
                            )
                        }
                        else -> {
                            Card(
                                onClick = { onSelectionChanged(listOf(item.id)) },
                            ) {
                                Text(
                                    text = item.label,
                                    modifier = Modifier.padding(12.dp),
                                )
                            }
                        }
                    }
                }
            }

            if (showClearButton && selectedItems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = { onSelectionChanged(emptyList()) },
                ) {
                    Text("清除选择")
                }
            }
        }
    }
}

@Composable
actual fun UnifyWheelPicker(
    items: List<String>,
    selectedIndex: Int,
    onSelectionChanged: (Int, String) -> Unit,
    modifier: Modifier,
    visibleItemCount: Int,
    infiniteLoop: Boolean,
    textColor: Color,
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "滚轮选择器 - Desktop模拟",
                style = MaterialTheme.typography.titleMedium,
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.height(200.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(items.size) { index ->
                    val isSelected = index == selectedIndex
                    Card(
                        onClick = { onSelectionChanged(index, items[index]) },
                        colors =
                            CardDefaults.cardColors(
                                containerColor =
                                    if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    },
                            ),
                    ) {
                        Text(
                            text = items[index],
                            modifier = Modifier.padding(12.dp),
                            color =
                                if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else if (textColor != Color.Unspecified) {
                                    textColor
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                        )
                    }
                }
            }
        }
    }
}

@Composable
actual fun UnifyMultiWheelPicker(
    wheels: List<List<String>>,
    selectedIndices: List<Int>,
    onSelectionChanged: (List<Int>, List<String>) -> Unit,
    modifier: Modifier,
    visibleItemCount: Int,
    wheelSpacing: Int,
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "多轮选择器 - Desktop模拟",
                style = MaterialTheme.typography.titleMedium,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(wheelSpacing.dp),
            ) {
                wheels.forEachIndexed { wheelIndex, wheelItems ->
                    Column(
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = "轮 ${wheelIndex + 1}",
                            style = MaterialTheme.typography.bodySmall,
                        )

                        LazyColumn(
                            modifier = Modifier.height(150.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            items(wheelItems.size) { itemIndex ->
                                val isSelected = selectedIndices.getOrNull(wheelIndex) == itemIndex
                                Card(
                                    onClick = {
                                        val newIndices = selectedIndices.toMutableList()
                                        while (newIndices.size <= wheelIndex) {
                                            newIndices.add(0)
                                        }
                                        newIndices[wheelIndex] = itemIndex
                                        val selectedValues =
                                            newIndices.mapIndexed { idx, selectedIdx ->
                                                wheels.getOrNull(idx)?.getOrNull(selectedIdx) ?: ""
                                            }
                                        onSelectionChanged(newIndices, selectedValues)
                                    },
                                    colors =
                                        CardDefaults.cardColors(
                                            containerColor =
                                                if (isSelected) {
                                                    MaterialTheme.colorScheme.primary
                                                } else {
                                                    MaterialTheme.colorScheme.surfaceVariant
                                                },
                                        ),
                                ) {
                                    Text(
                                        text = wheelItems[itemIndex],
                                        modifier = Modifier.padding(8.dp),
                                        style = MaterialTheme.typography.bodySmall,
                                        color =
                                            if (isSelected) {
                                                MaterialTheme.colorScheme.onPrimary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
actual fun UnifyCascadePicker(
    cascadeData: List<PickerItem>,
    selectedPath: List<String>,
    onSelectionChanged: (List<String>) -> Unit,
    modifier: Modifier,
    maxLevels: Int,
    showFullPath: Boolean,
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "级联选择器 - Desktop模拟",
                style = MaterialTheme.typography.titleMedium,
            )

            if (showFullPath && selectedPath.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "已选择: ${selectedPath.joinToString(" > ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(cascadeData) { item ->
                    CascadePickerItem(
                        item = item,
                        selectedPath = selectedPath,
                        onSelectionChanged = onSelectionChanged,
                        level = 0,
                        maxLevels = maxLevels,
                    )
                }
            }
        }
    }
}

@Composable
private fun CascadePickerItem(
    item: PickerItem,
    selectedPath: List<String>,
    onSelectionChanged: (List<String>) -> Unit,
    level: Int,
    maxLevels: Int,
) {
    val isSelected = selectedPath.getOrNull(level) == item.id

    Column {
        Card(
            onClick = {
                val newPath = selectedPath.take(level) + item.id
                onSelectionChanged(newPath)
            },
            colors =
                CardDefaults.cardColors(
                    containerColor =
                        if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                ),
            modifier = Modifier.padding(start = (level * 16).dp),
        ) {
            Text(
                text = item.label,
                modifier = Modifier.padding(12.dp),
                color =
                    if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
            )
        }

        if (isSelected && item.children.isNotEmpty() && level < maxLevels - 1) {
            item.children.forEach { child ->
                CascadePickerItem(
                    item = child,
                    selectedPath = selectedPath,
                    onSelectionChanged = onSelectionChanged,
                    level = level + 1,
                    maxLevels = maxLevels,
                )
            }
        }
    }
}

@Composable
actual fun UnifyDropdownPicker(
    items: List<PickerItem>,
    selectedItem: String?,
    onSelectionChanged: (String?) -> Unit,
    modifier: Modifier,
    placeholder: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    searchEnabled: Boolean,
) {
    var isExpanded by remember { mutableStateOf(expanded) }
    var searchText by remember { mutableStateOf("") }

    val filteredItems =
        if (searchEnabled && searchText.isNotEmpty()) {
            items.filter { it.label.contains(searchText, ignoreCase = true) }
        } else {
            items
        }

    Column(modifier = modifier) {
        OutlinedTextField(
            value =
                selectedItem?.let { selectedId ->
                    items.find { it.id == selectedId }?.label
                } ?: "",
            onValueChange = { },
            readOnly = true,
            placeholder = { Text(placeholder) },
            trailingIcon = {
                IconButton(onClick = {
                    isExpanded = !isExpanded
                    onExpandedChange(isExpanded)
                }) {
                    Icon(
                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "展开/收起",
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )

        if (isExpanded) {
            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    if (searchEnabled) {
                        OutlinedTextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            placeholder = { Text("搜索...") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "搜索")
                            },
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp),
                    ) {
                        items(filteredItems) { item ->
                            TextButton(
                                onClick = {
                                    onSelectionChanged(item.id)
                                    isExpanded = false
                                    onExpandedChange(false)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = item.enabled,
                            ) {
                                Text(
                                    text = item.label,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
actual fun UnifyColorPicker(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier,
    showAlphaSlider: Boolean,
    showHexInput: Boolean,
    presetColors: List<Color>,
) {
    var hexInput by remember { mutableStateOf("") }

    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "颜色选择器 - Desktop模拟",
                style = MaterialTheme.typography.titleMedium,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 当前选中颜色显示
            Surface(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                color = selectedColor,
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "当前颜色",
                        color = Color.White,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 预设颜色
            if (presetColors.isNotEmpty()) {
                Text("预设颜色:", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(100.dp),
                ) {
                    items(presetColors) { color ->
                        Surface(
                            modifier =
                                Modifier
                                    .size(40.dp),
                            color = color,
                            onClick = { onColorSelected(color) },
                        ) {}
                    }
                }
            } else {
                // 默认颜色调色板
                val defaultColors =
                    listOf(
                        Color.Red, Color.Green, Color.Blue, Color.Yellow,
                        Color.Cyan, Color.Magenta, Color.Black, Color.White,
                        Color.Gray, Color.DarkGray, Color.LightGray, Color.Transparent,
                    )
                Text("颜色调色板:", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(100.dp),
                ) {
                    items(defaultColors) { color ->
                        Surface(
                            modifier = Modifier.size(40.dp),
                            color = color,
                            onClick = { onColorSelected(color) },
                        ) {}
                    }
                }
            }

            if (showHexInput) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = hexInput,
                    onValueChange = { hexInput = it },
                    label = { Text("十六进制颜色") },
                    placeholder = { Text("#FF0000") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
