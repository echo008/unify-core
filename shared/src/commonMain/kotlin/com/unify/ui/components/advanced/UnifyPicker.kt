package com.unify.ui.components.advanced

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Unify跨平台选择器组件
 * 支持单选、多选、级联选择等功能
 */

data class PickerItem(
    val id: String,
    val label: String,
    val value: Any,
    val children: List<PickerItem> = emptyList(),
    val enabled: Boolean = true
)

enum class PickerType {
    SINGLE, MULTI, CASCADE, WHEEL, DROPDOWN
}

@Composable
expect fun UnifyPicker(
    items: List<PickerItem>,
    selectedItems: List<String> = emptyList(),
    onSelectionChanged: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
    pickerType: PickerType = PickerType.SINGLE,
    title: String = "请选择",
    placeholder: String = "请选择选项",
    maxSelections: Int = Int.MAX_VALUE,
    searchEnabled: Boolean = false,
    showClearButton: Boolean = true
)

@Composable
expect fun UnifyWheelPicker(
    items: List<String>,
    selectedIndex: Int = 0,
    onSelectionChanged: (Int, String) -> Unit,
    modifier: Modifier = Modifier,
    visibleItemCount: Int = 5,
    infiniteLoop: Boolean = false,
    textColor: Color = Color.Unspecified
)

@Composable
expect fun UnifyMultiWheelPicker(
    wheels: List<List<String>>,
    selectedIndices: List<Int> = emptyList(),
    onSelectionChanged: (List<Int>, List<String>) -> Unit,
    modifier: Modifier = Modifier,
    visibleItemCount: Int = 5,
    wheelSpacing: Int = 16
)

@Composable
expect fun UnifyCascadePicker(
    cascadeData: List<PickerItem>,
    selectedPath: List<String> = emptyList(),
    onSelectionChanged: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
    maxLevels: Int = 3,
    showFullPath: Boolean = true
)

@Composable
expect fun UnifyDropdownPicker(
    items: List<PickerItem>,
    selectedItem: String? = null,
    onSelectionChanged: (String?) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "请选择",
    expanded: Boolean = false,
    onExpandedChange: (Boolean) -> Unit = {},
    searchEnabled: Boolean = false
)

@Composable
expect fun UnifyColorPicker(
    selectedColor: Color = Color.Blue,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier,
    showAlphaSlider: Boolean = true,
    showHexInput: Boolean = true,
    presetColors: List<Color> = emptyList()
)
