package com.unify.ui.components.advanced

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.unify.ui.LocalUnifyTheme
import com.unify.ui.components.foundation.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlin.math.*

/**
 * Unify Picker 组件
 * 支持多平台适配的统一选择器组件，参考 KuiklyUI 设计规范
 */

/**
 * 选择器类型枚举
 */
enum class UnifyPickerType {
    DROPDOWN,       // 下拉选择器
    WHEEL,          // 滚轮选择器
    DATE,           // 日期选择器
    TIME,           // 时间选择器
    DATETIME,       // 日期时间选择器
    MULTI_COLUMN    // 多列选择器
}

/**
 * 选择器项数据
 */
data class UnifyPickerItem<T>(
    val value: T,
    val label: String,
    val enabled: Boolean = true
)

/**
 * 多列选择器数据
 */
data class UnifyMultiColumnPickerData<T>(
    val columns: List<List<UnifyPickerItem<T>>>,
    val selectedIndices: List<Int> = emptyList()
)

/**
 * 主要 Unify Picker 组件
 */
@Composable
fun <T> UnifyPicker(
    items: List<UnifyPickerItem<T>>,
    selectedItem: UnifyPickerItem<T>?,
    onItemSelected: (UnifyPickerItem<T>) -> Unit,
    modifier: Modifier = Modifier,
    type: UnifyPickerType = UnifyPickerType.DROPDOWN,
    label: String? = null,
    placeholder: String? = null,
    enabled: Boolean = true,
    expanded: Boolean = false,
    onExpandedChange: ((Boolean) -> Unit)? = null,
    contentDescription: String? = null
) {
    when (type) {
        UnifyPickerType.DROPDOWN -> {
            UnifyDropdownPicker(
                items = items,
                selectedItem = selectedItem,
                onItemSelected = onItemSelected,
                modifier = modifier,
                label = label,
                placeholder = placeholder,
                enabled = enabled,
                expanded = expanded,
                onExpandedChange = onExpandedChange,
                contentDescription = contentDescription
            )
        }
        UnifyPickerType.WHEEL -> {
            UnifyWheelPicker(
                items = items,
                selectedItem = selectedItem,
                onItemSelected = onItemSelected,
                modifier = modifier,
                enabled = enabled,
                contentDescription = contentDescription
            )
        }
        else -> {
            // 其他类型的选择器将在专门的组合函数中实现
            UnifyDropdownPicker(
                items = items,
                selectedItem = selectedItem,
                onItemSelected = onItemSelected,
                modifier = modifier,
                label = label,
                placeholder = placeholder,
                enabled = enabled,
                expanded = expanded,
                onExpandedChange = onExpandedChange,
                contentDescription = contentDescription
            )
        }
    }
}

/**
 * 下拉选择器组件
 */
@Composable
private fun <T> UnifyDropdownPicker(
    items: List<UnifyPickerItem<T>>,
    selectedItem: UnifyPickerItem<T>?,
    onItemSelected: (UnifyPickerItem<T>) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    enabled: Boolean = true,
    expanded: Boolean = false,
    onExpandedChange: ((Boolean) -> Unit)? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    var internalExpanded by remember { mutableStateOf(expanded) }
    
    LaunchedEffect(expanded) {
        internalExpanded = expanded
    }
    
    Column(
        modifier = modifier.semantics {
            contentDescription?.let { 
                this.contentDescription = it 
            }
        }
    ) {
        // 标签
        label?.let { labelText ->
            UnifyText(
                text = labelText,
                variant = UnifyTextVariant.CAPTION,
                color = theme.colors.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        
        // 选择器触发器
        Box {
            UnifySurface(
                variant = UnifySurfaceVariant.OUTLINED,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = enabled) {
                        val newExpanded = !internalExpanded
                        internalExpanded = newExpanded
                        onExpandedChange?.invoke(newExpanded)
                    }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UnifyText(
                        text = selectedItem?.label ?: placeholder ?: "请选择",
                        variant = UnifyTextVariant.BODY_MEDIUM,
                        color = if (selectedItem != null) {
                            theme.colors.onSurface
                        } else {
                            theme.colors.onSurfaceVariant
                        },
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    UnifyIcon(
                        icon = Icons.Default.KeyboardArrowDown,
                        size = UnifyIconSize.SMALL,
                        modifier = Modifier.graphicsLayer {
                            rotationZ = if (internalExpanded) 180f else 0f
                        }
                    )
                }
            }
            
            // 下拉菜单
            DropdownMenu(
                expanded = internalExpanded,
                onDismissRequest = {
                    internalExpanded = false
                    onExpandedChange?.invoke(false)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                items.forEach { item ->
                    DropdownMenuItem(
                        text = {
                            UnifyText(
                                text = item.label,
                                variant = UnifyTextVariant.BODY_MEDIUM
                            )
                        },
                        onClick = {
                            onItemSelected(item)
                            internalExpanded = false
                            onExpandedChange?.invoke(false)
                        },
                        enabled = item.enabled
                    )
                }
            }
        }
    }
}

/**
 * 滚轮选择器组件
 */
@Composable
private fun <T> UnifyWheelPicker(
    items: List<UnifyPickerItem<T>>,
    selectedItem: UnifyPickerItem<T>?,
    onItemSelected: (UnifyPickerItem<T>) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    visibleItemsCount: Int = 5,
    itemHeight: Dp = 48.dp,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    
    val selectedIndex = items.indexOfFirst { it == selectedItem }.takeIf { it >= 0 } ?: 0
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)
    
    val itemHeightPx = with(density) { itemHeight.toPx() }
    val halfVisibleItems = visibleItemsCount / 2
    
    LaunchedEffect(selectedIndex) {
        listState.animateScrollToItem(selectedIndex)
    }
    
    Box(
        modifier = modifier
            .height(itemHeight * visibleItemsCount)
            .semantics {
                contentDescription?.let { 
                    this.contentDescription = it 
                }
            }
    ) {
        // 选中项背景
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight)
                .align(Alignment.Center)
                .background(
                    theme.colors.primaryContainer.copy(alpha = 0.3f),
                    RoundedCornerShape(8.dp)
                )
        )
        
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            // 滚动结束后，自动对齐到最近的项
                            coroutineScope.launch {
                                val firstVisibleIndex = listState.firstVisibleItemIndex
                                val firstVisibleOffset = listState.firstVisibleItemScrollOffset
                                
                                val targetIndex = if (firstVisibleOffset > itemHeightPx / 2) {
                                    firstVisibleIndex + 1
                                } else {
                                    firstVisibleIndex
                                }
                                
                                val clampedIndex = targetIndex.coerceIn(0, items.size - 1)
                                listState.animateScrollToItem(clampedIndex)
                                
                                if (clampedIndex != selectedIndex) {
                                    onItemSelected(items[clampedIndex])
                                }
                            }
                        }
                    ) { _, _ -> }
                },
            contentPadding = PaddingValues(vertical = itemHeight * halfVisibleItems),
            userScrollEnabled = enabled
        ) {
            itemsIndexed(items) { index, item ->
                val isSelected = index == selectedIndex
                val distanceFromCenter = abs(index - selectedIndex)
                val alpha = when {
                    distanceFromCenter == 0 -> 1f
                    distanceFromCenter == 1 -> 0.7f
                    distanceFromCenter == 2 -> 0.4f
                    else -> 0.2f
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .alpha(alpha)
                        .clickable(enabled = enabled && item.enabled) {
                            coroutineScope.launch {
                                listState.animateScrollToItem(index)
                                onItemSelected(item)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    UnifyText(
                        text = item.label,
                        variant = UnifyTextVariant.BODY_MEDIUM,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (item.enabled) {
                            theme.colors.onSurface
                        } else {
                            theme.colors.onSurface.copy(alpha = 0.38f)
                        },
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * 日期选择器组件
 */
@Composable
fun UnifyDatePicker(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    minDate: LocalDate? = null,
    maxDate: LocalDate? = null,
    label: String? = null,
    enabled: Boolean = true,
    contentDescription: String? = null
) {
    var showDialog by remember { mutableStateOf(false) }
    val theme = LocalUnifyTheme.current
    
    Column(
        modifier = modifier.semantics {
            contentDescription?.let { 
                this.contentDescription = it 
            }
        }
    ) {
        // 标签
        label?.let { labelText ->
            UnifyText(
                text = labelText,
                variant = UnifyTextVariant.CAPTION,
                color = theme.colors.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        
        // 日期显示触发器
        UnifySurface(
            variant = UnifySurfaceVariant.OUTLINED,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) { showDialog = true }
                .padding(16.dp)
        ) {
            UnifyText(
                text = selectedDate?.let { formatDate(it) } ?: "请选择日期",
                variant = UnifyTextVariant.BODY_MEDIUM,
                color = if (selectedDate != null) {
                    theme.colors.onSurface
                } else {
                    theme.colors.onSurfaceVariant
                }
            )
        }
        
        // 日期选择对话框
        if (showDialog) {
            UnifyDialog(
                onDismissRequest = { showDialog = false },
                title = "选择日期",
                content = {
                    UnifyCalendar(
                        selectedDate = selectedDate ?: Clock.System.todayIn(TimeZone.currentSystemDefault()),
                        onDateSelected = { date ->
                            onDateSelected(date)
                            showDialog = false
                        },
                        minDate = minDate,
                        maxDate = maxDate
                    )
                },
                confirmButton = {
                    UnifyButton(
                        text = "确定",
                        onClick = { showDialog = false }
                    )
                },
                dismissButton = {
                    UnifyButton(
                        text = "取消",
                        variant = UnifyButtonVariant.TEXT,
                        onClick = { showDialog = false }
                    )
                }
            )
        }
    }
}

/**
 * 时间选择器组件
 */
@Composable
fun UnifyTimePicker(
    selectedTime: LocalTime?,
    onTimeSelected: (LocalTime) -> Unit,
    modifier: Modifier = Modifier,
    is24Hour: Boolean = true,
    label: String? = null,
    enabled: Boolean = true,
    contentDescription: String? = null
) {
    var showDialog by remember { mutableStateOf(false) }
    val theme = LocalUnifyTheme.current
    
    Column(
        modifier = modifier.semantics {
            contentDescription?.let { 
                this.contentDescription = it 
            }
        }
    ) {
        // 标签
        label?.let { labelText ->
            UnifyText(
                text = labelText,
                variant = UnifyTextVariant.CAPTION,
                color = theme.colors.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        
        // 时间显示触发器
        UnifySurface(
            variant = UnifySurfaceVariant.OUTLINED,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) { showDialog = true }
                .padding(16.dp)
        ) {
            UnifyText(
                text = selectedTime?.let { formatTime(it, is24Hour) } ?: "请选择时间",
                variant = UnifyTextVariant.BODY_MEDIUM,
                color = if (selectedTime != null) {
                    theme.colors.onSurface
                } else {
                    theme.colors.onSurfaceVariant
                }
            )
        }
        
        // 时间选择对话框
        if (showDialog) {
            UnifyTimePickerDialog(
                selectedTime = selectedTime ?: LocalTime(12, 0),
                onTimeSelected = { time ->
                    onTimeSelected(time)
                    showDialog = false
                },
                onDismiss = { showDialog = false },
                is24Hour = is24Hour
            )
        }
    }
}

/**
 * 时间选择对话框
 */
@Composable
private fun UnifyTimePickerDialog(
    selectedTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
    is24Hour: Boolean
) {
    var hour by remember { mutableStateOf(selectedTime.hour) }
    var minute by remember { mutableStateOf(selectedTime.minute) }
    var isAM by remember { mutableStateOf(selectedTime.hour < 12) }
    
    UnifyDialog(
        onDismissRequest = onDismiss,
        title = "选择时间",
        content = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 小时选择器
                val hourItems = if (is24Hour) {
                    (0..23).map { UnifyPickerItem(it, it.toString().padStart(2, '0')) }
                } else {
                    (1..12).map { UnifyPickerItem(it, it.toString().padStart(2, '0')) }
                }
                
                UnifyWheelPicker(
                    items = hourItems,
                    selectedItem = hourItems.find { 
                        it.value == if (is24Hour) hour else {
                            if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
                        }
                    },
                    onItemSelected = { item ->
                        hour = if (is24Hour) {
                            item.value
                        } else {
                            when {
                                item.value == 12 && isAM -> 0
                                item.value == 12 && !isAM -> 12
                                !isAM -> item.value + 12
                                else -> item.value
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                
                UnifyText(text = ":", variant = UnifyTextVariant.TITLE_MEDIUM)
                
                // 分钟选择器
                val minuteItems = (0..59).map { 
                    UnifyPickerItem(it, it.toString().padStart(2, '0')) 
                }
                
                UnifyWheelPicker(
                    items = minuteItems,
                    selectedItem = minuteItems.find { it.value == minute },
                    onItemSelected = { item -> minute = item.value },
                    modifier = Modifier.weight(1f)
                )
                
                // AM/PM 选择器（12小时制）
                if (!is24Hour) {
                    val amPmItems = listOf(
                        UnifyPickerItem(true, "AM"),
                        UnifyPickerItem(false, "PM")
                    )
                    
                    UnifyWheelPicker(
                        items = amPmItems,
                        selectedItem = amPmItems.find { it.value == isAM },
                        onItemSelected = { item -> 
                            isAM = item.value
                            hour = if (isAM && hour >= 12) {
                                hour - 12
                            } else if (!isAM && hour < 12) {
                                hour + 12
                            } else {
                                hour
                            }
                        },
                        modifier = Modifier.weight(0.8f)
                    )
                }
            }
        },
        confirmButton = {
            UnifyButton(
                text = "确定",
                onClick = {
                    onTimeSelected(LocalTime(hour, minute))
                }
            )
        },
        dismissButton = {
            UnifyButton(
                text = "取消",
                variant = UnifyButtonVariant.TEXT,
                onClick = onDismiss
            )
        }
    )
}

/**
 * 多列选择器组件
 */
@Composable
fun <T> UnifyMultiColumnPicker(
    data: UnifyMultiColumnPickerData<T>,
    onSelectionChanged: (List<Int>) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription?.let { 
                    this.contentDescription = it 
                }
            },
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        data.columns.forEachIndexed { columnIndex, columnItems ->
            val selectedIndex = data.selectedIndices.getOrNull(columnIndex) ?: 0
            val selectedItem = columnItems.getOrNull(selectedIndex)
            
            UnifyWheelPicker(
                items = columnItems,
                selectedItem = selectedItem,
                onItemSelected = { item ->
                    val newSelectedIndices = data.selectedIndices.toMutableList()
                    val newIndex = columnItems.indexOf(item)
                    
                    // 确保列表足够长
                    while (newSelectedIndices.size <= columnIndex) {
                        newSelectedIndices.add(0)
                    }
                    
                    newSelectedIndices[columnIndex] = newIndex
                    onSelectionChanged(newSelectedIndices)
                },
                modifier = Modifier.weight(1f),
                enabled = enabled
            )
        }
    }
}

/**
 * 辅助函数
 */
private fun formatDate(date: LocalDate): String {
    return "${date.year}年${date.monthNumber}月${date.dayOfMonth}日"
}

private fun formatTime(time: LocalTime, is24Hour: Boolean): String {
    return if (is24Hour) {
        "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}"
    } else {
        val hour = if (time.hour == 0) 12 else if (time.hour > 12) time.hour - 12 else time.hour
        val amPm = if (time.hour < 12) "AM" else "PM"
        "${hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')} $amPm"
    }
}
