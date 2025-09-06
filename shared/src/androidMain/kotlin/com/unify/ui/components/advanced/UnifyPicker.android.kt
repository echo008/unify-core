@file:OptIn(ExperimentalMaterial3Api::class)

package com.unify.ui.components.advanced

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

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
    var showDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    // Display selected items
    OutlinedTextField(
        value = if (selectedItems.isEmpty()) placeholder else selectedItems.joinToString(", "),
        onValueChange = { },
        modifier = modifier.clickable { showDialog = true },
        enabled = false,
        label = { Text(title) },
        trailingIcon = {
            Row {
                if (showClearButton && selectedItems.isNotEmpty()) {
                    IconButton(onClick = { onSelectionChanged(emptyList()) }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Open")
            }
        }
    )
    
    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (searchEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("搜索...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val filteredItems = if (searchQuery.isEmpty()) {
                        items
                    } else {
                        items.filter { it.label.contains(searchQuery, ignoreCase = true) }
                    }
                    
                    LazyColumn {
                        items(filteredItems) { item ->
                            val isSelected = selectedItems.contains(item.id)
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = item.enabled) {
                                        when (pickerType) {
                                            PickerType.SINGLE -> {
                                                onSelectionChanged(listOf(item.id))
                                                showDialog = false
                                            }
                                            PickerType.MULTI -> {
                                                val newSelection = if (isSelected) {
                                                    selectedItems - item.id
                                                } else {
                                                    if (selectedItems.size < maxSelections) {
                                                        selectedItems + item.id
                                                    } else selectedItems
                                                }
                                                onSelectionChanged(newSelection)
                                            }
                                            else -> {
                                                onSelectionChanged(listOf(item.id))
                                                showDialog = false
                                            }
                                        }
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                when (pickerType) {
                                    PickerType.MULTI -> {
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = null,
                                            enabled = item.enabled
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    PickerType.SINGLE -> {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = null,
                                            enabled = item.enabled
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    else -> {}
                                }
                                
                                Text(
                                    text = item.label,
                                    color = if (item.enabled) 
                                        MaterialTheme.colorScheme.onSurface 
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showDialog = false }) {
                            Text("取消")
                        }
                        if (pickerType == PickerType.MULTI) {
                            TextButton(onClick = { showDialog = false }) {
                                Text("确定")
                            }
                        }
                    }
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
    textColor: Color
) {
    LazyColumn(
        modifier = modifier.height((visibleItemCount * 48).dp),
        verticalArrangement = Arrangement.Center
    ) {
        itemsIndexed(items) { index, item ->
            val isSelected = index == selectedIndex
            
            Text(
                text = item,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectionChanged(index, item) }
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else textColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                style = if (isSelected) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium
            )
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
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(wheelSpacing.dp)
    ) {
        wheels.forEachIndexed { wheelIndex, wheelItems ->
            val selectedIndex = selectedIndices.getOrElse(wheelIndex) { 0 }
            
            UnifyWheelPicker(
                items = wheelItems,
                selectedIndex = selectedIndex,
                onSelectionChanged = { index, item ->
                    val newIndices = selectedIndices.toMutableList()
                    val newItems = mutableListOf<String>()
                    
                    // Update indices
                    while (newIndices.size <= wheelIndex) {
                        newIndices.add(0)
                    }
                    newIndices[wheelIndex] = index
                    
                    // Get selected items
                    wheels.forEachIndexed { i, wheel ->
                        val idx = newIndices.getOrElse(i) { 0 }
                        if (idx < wheel.size) {
                            newItems.add(wheel[idx])
                        }
                    }
                    
                    onSelectionChanged(newIndices, newItems)
                },
                modifier = Modifier.weight(1f),
                visibleItemCount = visibleItemCount
            )
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
    var showDialog by remember { mutableStateOf(false) }
    var currentLevel by remember { mutableStateOf(0) }
    var levelData by remember { mutableStateOf(listOf(cascadeData)) }
    
    OutlinedTextField(
        value = if (selectedPath.isEmpty()) "请选择" else {
            if (showFullPath) selectedPath.joinToString(" > ") else selectedPath.lastOrNull() ?: ""
        },
        onValueChange = { },
        modifier = modifier.clickable { showDialog = true },
        enabled = false,
        label = { Text("级联选择") },
        trailingIcon = {
            Icon(Icons.Default.ArrowDropDown, contentDescription = "Open")
        }
    )
    
    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "级联选择",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Level tabs
                    if (levelData.size > 1) {
                        ScrollableTabRow(
                            selectedTabIndex = currentLevel,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            levelData.forEachIndexed { index, _ ->
                                Tab(
                                    selected = currentLevel == index,
                                    onClick = { currentLevel = index },
                                    text = { Text("级别 ${index + 1}") }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    LazyColumn {
                        items(levelData.getOrElse(currentLevel) { emptyList() }) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val newPath = selectedPath.take(currentLevel) + item.id
                                        onSelectionChanged(newPath)
                                        
                                        if (item.children.isNotEmpty() && currentLevel < maxLevels - 1) {
                                            val newLevelData = levelData.toMutableList()
                                            while (newLevelData.size <= currentLevel + 1) {
                                                newLevelData.add(emptyList())
                                            }
                                            newLevelData[currentLevel + 1] = item.children
                                            levelData = newLevelData
                                            currentLevel = currentLevel + 1
                                        } else {
                                            showDialog = false
                                        }
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.label,
                                    modifier = Modifier.weight(1f)
                                )
                                if (item.children.isNotEmpty()) {
                                    Icon(
                                        Icons.Default.ChevronRight,
                                        contentDescription = "Has children"
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showDialog = false }) {
                            Text("取消")
                        }
                    }
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
    searchEnabled: Boolean
) {
    var isExpanded by remember { mutableStateOf(expanded) }
    var searchQuery by remember { mutableStateOf("") }
    
    LaunchedEffect(expanded) {
        isExpanded = expanded
    }
    
    LaunchedEffect(isExpanded) {
        onExpandedChange(isExpanded)
    }
    
    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { isExpanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedItem?.let { id ->
                items.find { it.id == id }?.label
            } ?: placeholder,
            onValueChange = { },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
            }
        )
        
        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false }
        ) {
            if (searchEnabled) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("搜索...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
            }
            
            val filteredItems = if (searchQuery.isEmpty()) {
                items
            } else {
                items.filter { it.label.contains(searchQuery, ignoreCase = true) }
            }
            
            filteredItems.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.label) },
                    onClick = {
                        onSelectionChanged(item.id)
                        isExpanded = false
                    },
                    enabled = item.enabled
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
    presetColors: List<Color>
) {
    var showDialog by remember { mutableStateOf(false) }
    var currentColor by remember { mutableStateOf(selectedColor) }
    var hexInput by remember { mutableStateOf(colorToHex(selectedColor)) }
    
    // Color preview button
    OutlinedButton(
        onClick = { showDialog = true },
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(selectedColor)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("选择颜色")
    }
    
    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "颜色选择器",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Color preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(currentColor)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // RGB sliders
                    val red = (currentColor.red * 255).toInt()
                    val green = (currentColor.green * 255).toInt()
                    val blue = (currentColor.blue * 255).toInt()
                    val alpha = (currentColor.alpha * 255).toInt()
                    
                    ColorSlider(
                        label = "Red",
                        value = red,
                        onValueChange = { newRed ->
                            currentColor = Color(newRed, green, blue, alpha)
                            hexInput = colorToHex(currentColor)
                        }
                    )
                    
                    ColorSlider(
                        label = "Green",
                        value = green,
                        onValueChange = { newGreen ->
                            currentColor = Color(red, newGreen, blue, alpha)
                            hexInput = colorToHex(currentColor)
                        }
                    )
                    
                    ColorSlider(
                        label = "Blue",
                        value = blue,
                        onValueChange = { newBlue ->
                            currentColor = Color(red, green, newBlue, alpha)
                            hexInput = colorToHex(currentColor)
                        }
                    )
                    
                    if (showAlphaSlider) {
                        ColorSlider(
                            label = "Alpha",
                            value = alpha,
                            onValueChange = { newAlpha ->
                                currentColor = Color(red, green, blue, newAlpha)
                                hexInput = colorToHex(currentColor)
                            }
                        )
                    }
                    
                    if (showHexInput) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = hexInput,
                            onValueChange = { hex ->
                                hexInput = hex
                                hexToColor(hex)?.let { color ->
                                    currentColor = color
                                }
                            },
                            label = { Text("Hex Color") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    // Preset colors
                    if (presetColors.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("预设颜色", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        LazyColumn {
                            items(presetColors.chunked(6)) { colorRow ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    colorRow.forEach { color ->
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                                .clickable {
                                                    currentColor = color
                                                    hexInput = colorToHex(color)
                                                }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showDialog = false }) {
                            Text("取消")
                        }
                        TextButton(
                            onClick = {
                                onColorSelected(currentColor)
                                showDialog = false
                            }
                        ) {
                            Text("确定")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorSlider(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.width(60.dp),
            style = MaterialTheme.typography.bodySmall
        )
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 0f..255f,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value.toString(),
            modifier = Modifier.width(40.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

private fun colorToHex(color: Color): String {
    val argb = color.toArgb()
    return String.format("#%08X", argb)
}

private fun hexToColor(hex: String): Color? {
    return try {
        val cleanHex = hex.removePrefix("#")
        when (cleanHex.length) {
            6 -> Color(android.graphics.Color.parseColor("#FF$cleanHex"))
            8 -> Color(android.graphics.Color.parseColor("#$cleanHex"))
            else -> null
        }
    } catch (e: Exception) {
        null
    }
}
