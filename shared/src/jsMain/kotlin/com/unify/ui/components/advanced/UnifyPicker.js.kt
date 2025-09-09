package com.unify.ui.components.advanced

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

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
    Column(modifier = modifier) {
        Text(title)
        var localExpanded by remember { mutableStateOf(false) }

        Button(
            onClick = { localExpanded = true },
        ) {
            Text(if (selectedItems.isEmpty()) placeholder else selectedItems.joinToString(", "))
        }

        DropdownMenu(
            expanded = localExpanded,
            onDismissRequest = { localExpanded = false },
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.label) },
                    onClick = {
                        when (pickerType) {
                            PickerType.SINGLE -> {
                                onSelectionChanged(listOf(item.id))
                                localExpanded = false
                            }
                            PickerType.MULTI -> {
                                val newSelection =
                                    if (selectedItems.contains(item.id)) {
                                        selectedItems - item.id
                                    } else {
                                        if (selectedItems.size < maxSelections) selectedItems + item.id else selectedItems
                                    }
                                onSelectionChanged(newSelection)
                            }
                            else -> {
                                onSelectionChanged(listOf(item.id))
                                localExpanded = false
                            }
                        }
                    },
                )
            }
        }

        if (showClearButton && selectedItems.isNotEmpty()) {
            TextButton(onClick = { onSelectionChanged(emptyList()) }) {
                Text("Clear")
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
    Column(modifier = modifier) {
        Text("Wheel Picker (JS Implementation)")
        LazyColumn(modifier = Modifier.height(200.dp)) {
            items(items) { item ->
                Button(
                    onClick = {
                        val index = items.indexOf(item)
                        onSelectionChanged(index, item)
                    },
                ) {
                    Text(item, color = textColor)
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
    Row(modifier = modifier) {
        wheels.forEachIndexed { wheelIndex, wheelItems ->
            Column(modifier = Modifier.weight(1f)) {
                Text("Wheel ${wheelIndex + 1}")
                LazyColumn(modifier = Modifier.height(150.dp)) {
                    items(wheelItems) { item ->
                        Button(
                            onClick = {
                                val newIndices = selectedIndices.toMutableList()
                                if (wheelIndex < newIndices.size) {
                                    newIndices[wheelIndex] = wheelItems.indexOf(item)
                                } else {
                                    while (newIndices.size <= wheelIndex) newIndices.add(0)
                                    newIndices[wheelIndex] = wheelItems.indexOf(item)
                                }
                                val selectedValues =
                                    wheels.mapIndexed { index, wheel ->
                                        if (index < newIndices.size && newIndices[index] < wheel.size) {
                                            wheel[newIndices[index]]
                                        } else {
                                            wheel.firstOrNull() ?: ""
                                        }
                                    }
                                onSelectionChanged(newIndices, selectedValues)
                            },
                        ) {
                            Text(item)
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
    Column(modifier = modifier) {
        Text("Cascade Picker (JS Implementation)")
        if (showFullPath) {
            Text("Path: ${selectedPath.joinToString(" > ")}")
        } else {
            Text("Selected: ${selectedPath.lastOrNull() ?: "None"}")
        }

        // Simple cascade implementation
        var currentLevel by remember { mutableStateOf(0) }
        var currentData by remember { mutableStateOf(cascadeData) }

        LazyColumn(modifier = Modifier.height(200.dp)) {
            items(currentData.take(maxLevels)) { item ->
                Button(
                    onClick = {
                        val newPath = selectedPath.take(currentLevel) + item.id
                        onSelectionChanged(newPath)
                        if (item.children.isNotEmpty() && currentLevel < maxLevels - 1) {
                            currentData = item.children
                            currentLevel++
                        }
                    },
                ) {
                    Text(item.label)
                }
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
    var searchText by remember { mutableStateOf("") }

    Column(modifier = modifier) {
        Button(
            onClick = { onExpandedChange(!expanded) },
        ) {
            Text(selectedItem?.let { id -> items.find { it.id == id }?.label } ?: placeholder)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
        ) {
            if (searchEnabled) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = { Text("Search...") },
                    modifier = Modifier.padding(8.dp),
                )
            }

            val filteredItems =
                if (searchEnabled && searchText.isNotEmpty()) {
                    items.filter { it.label.contains(searchText, ignoreCase = true) }
                } else {
                    items
                }

            filteredItems.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.label) },
                    onClick = {
                        onSelectionChanged(item.id)
                        onExpandedChange(false)
                        searchText = ""
                    },
                    enabled = item.enabled,
                )
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
    Column(modifier = modifier) {
        Text("Color Picker (JS Implementation)")

        // Use preset colors if provided, otherwise default colors
        val colors =
            if (presetColors.isNotEmpty()) {
                presetColors
            } else {
                listOf(
                    Color.Red,
                    Color.Green,
                    Color.Blue,
                    Color.Yellow,
                    Color.Cyan,
                    Color.Magenta,
                    Color.Black,
                    Color.White,
                )
            }

        LazyColumn {
            items(colors.chunked(4)) { colorRow ->
                Row {
                    colorRow.forEach { color ->
                        Button(
                            onClick = { onColorSelected(color) },
                            modifier = Modifier.weight(1f).padding(2.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = color),
                        ) {
                            Text(" ")
                        }
                    }
                }
            }
        }

        if (showAlphaSlider) {
            Text("Alpha: ${selectedColor.alpha}")
        }

        if (showHexInput) {
            Text("Selected: #${selectedColor.value.toString(16).uppercase()}")
        }
    }
}
