package com.unify.ui.components.advanced

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
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
    showClearButton: Boolean
) {
    Column(modifier = modifier) {
        Text(title)
        Text("iOS Picker - ${items.size} items")
        items.forEach { item ->
            Button(
                onClick = { onSelectionChanged(listOf(item.id)) }
            ) {
                Text(item.label)
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
    textColor: Color
) {
    Column(modifier = modifier) {
        Text("iOS Wheel Picker")
        LazyColumn {
            items(items) { item ->
                val index = items.indexOf(item)
                Card(
                    modifier = Modifier.fillMaxWidth().padding(2.dp),
                    onClick = { onSelectionChanged(index, item) }
                ) {
                    Text(
                        text = item,
                        modifier = Modifier.padding(8.dp),
                        color = textColor
                    )
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
    wheelSpacing: Int
) {
    Row(modifier = modifier) {
        wheels.forEachIndexed { wheelIndex, items ->
            Column(modifier = Modifier.weight(1f)) {
                Text("Wheel ${wheelIndex + 1}")
                items.forEachIndexed { itemIndex, item ->
                    Button(
                        onClick = { 
                            val newIndices = selectedIndices.toMutableList()
                            if (wheelIndex < newIndices.size) {
                                newIndices[wheelIndex] = itemIndex
                            }
                            val selectedItems = newIndices.mapIndexed { index, selectedIndex ->
                                if (index < wheels.size && selectedIndex < wheels[index].size) {
                                    wheels[index][selectedIndex]
                                } else ""
                            }
                            onSelectionChanged(newIndices, selectedItems)
                        }
                    ) {
                        Text(item)
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
    showFullPath: Boolean
) {
    Column(modifier = modifier) {
        Text("iOS Cascade Picker")
        cascadeData.forEach { item ->
            Button(
                onClick = { onSelectionChanged(listOf(item.id)) }
            ) {
                Text(item.label)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun UnifyDropdownPicker(
    items: List<PickerItem>,
    selectedItem: String?,
    onSelectionChanged: (String?) -> Unit,
    modifier: Modifier,
    placeholder: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    searchEnabled: Boolean
) {
    var internalExpanded by remember { mutableStateOf(expanded) }
    
    Column(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = internalExpanded,
            onExpandedChange = { 
                internalExpanded = !internalExpanded
                onExpandedChange(internalExpanded)
            }
        ) {
            OutlinedTextField(
                value = selectedItem ?: placeholder,
                onValueChange = { },
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = internalExpanded) },
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = internalExpanded,
                onDismissRequest = { 
                    internalExpanded = false
                    onExpandedChange(false)
                }
            ) {
                items.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item.label) },
                        onClick = {
                            onSelectionChanged(item.id)
                            internalExpanded = false
                            onExpandedChange(false)
                        }
                    )
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
    presetColors: List<Color>
) {
    Column(modifier = modifier) {
        Text("iOS Color Picker")
        Row {
            presetColors.forEach { color ->
                Button(
                    onClick = { onColorSelected(color) },
                    colors = ButtonDefaults.buttonColors(containerColor = color)
                ) {
                    Text(" ")
                }
            }
        }
        if (showHexInput) {
            Text("Selected: ${selectedColor.toString()}")
        }
    }
}
