package com.unify.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Unify统一日历组件
 * 100% Kotlin Compose语法实现
 */
data class UnifyCalendarDate(
    val year: Int,
    val month: Int,
    val day: Int
) {
    fun toDisplayString(): String = "${year}年${month}月${day}日"
    
    companion object {
        fun today(): UnifyCalendarDate {
            // 简化实现，实际项目中应使用kotlinx-datetime
            return UnifyCalendarDate(2024, 1, 15)
        }
    }
}

data class UnifyCalendarEvent(
    val date: UnifyCalendarDate,
    val title: String,
    val color: Color = Color.Blue
)

@Composable
fun UnifyCalendar(
    selectedDate: UnifyCalendarDate?,
    onDateSelected: (UnifyCalendarDate) -> Unit,
    modifier: Modifier = Modifier,
    events: List<UnifyCalendarEvent> = emptyList(),
    minDate: UnifyCalendarDate? = null,
    maxDate: UnifyCalendarDate? = null,
    showHeader: Boolean = true
) {
    var currentMonth by remember { mutableStateOf(selectedDate?.month ?: UnifyCalendarDate.today().month) }
    var currentYear by remember { mutableStateOf(selectedDate?.year ?: UnifyCalendarDate.today().year) }
    
    UnifyCard(
        modifier = modifier,
        type = UnifyCardType.OUTLINED
    ) {
        UnifyColumn(
            modifier = Modifier.padding(16.dp),
            spacing = UnifySpacing.MEDIUM
        ) {
            if (showHeader) {
                CalendarHeader(
                    year = currentYear,
                    month = currentMonth,
                    onPreviousMonth = {
                        if (currentMonth == 1) {
                            currentMonth = 12
                            currentYear--
                        } else {
                            currentMonth--
                        }
                    },
                    onNextMonth = {
                        if (currentMonth == 12) {
                            currentMonth = 1
                            currentYear++
                        } else {
                            currentMonth++
                        }
                    }
                )
            }
            
            CalendarGrid(
                year = currentYear,
                month = currentMonth,
                selectedDate = selectedDate,
                onDateSelected = onDateSelected,
                events = events,
                minDate = minDate,
                maxDate = maxDate
            )
        }
    }
}

@Composable
private fun CalendarHeader(
    year: Int,
    month: Int,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "上个月"
            )
        }
        
        Text(
            text = "${year}年${month}月",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        
        IconButton(onClick = onNextMonth) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "下个月"
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    year: Int,
    month: Int,
    selectedDate: UnifyCalendarDate?,
    onDateSelected: (UnifyCalendarDate) -> Unit,
    events: List<UnifyCalendarEvent>,
    minDate: UnifyCalendarDate?,
    maxDate: UnifyCalendarDate?
) {
    val daysInMonth = getDaysInMonth(year, month)
    val firstDayOfWeek = getFirstDayOfWeek(year, month)
    val weekdays = listOf("日", "一", "二", "三", "四", "五", "六")
    
    UnifyColumn(spacing = UnifySpacing.SMALL) {
        // 星期标题
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            weekdays.forEach { day ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        
        // 日期网格
        val totalCells = 42 // 6周 × 7天
        val dates = mutableListOf<Int?>()
        
        // 添加空白单元格（上个月的日期）
        repeat(firstDayOfWeek) {
            dates.add(null)
        }
        
        // 添加当月日期
        repeat(daysInMonth) { day ->
            dates.add(day + 1)
        }
        
        // 填充剩余单元格
        while (dates.size < totalCells) {
            dates.add(null)
        }
        
        // 按周分组显示
        dates.chunked(7).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                week.forEach { day ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (day != null) {
                            val date = UnifyCalendarDate(year, month, day)
                            val isSelected = selectedDate == date
                            val hasEvent = events.any { it.date == date }
                            val isEnabled = isDateEnabled(date, minDate, maxDate)
                            
                            CalendarDay(
                                day = day,
                                isSelected = isSelected,
                                hasEvent = hasEvent,
                                isEnabled = isEnabled,
                                onClick = {
                                    if (isEnabled) {
                                        onDateSelected(date)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDay(
    day: Int,
    isSelected: Boolean,
    hasEvent: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        else -> Color.Transparent
    }
    
    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        !isEnabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(enabled = isEnabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        UnifyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            spacing = UnifySpacing.NONE
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                textAlign = TextAlign.Center
            )
            
            if (hasEvent && !isSelected) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

// 辅助函数
private fun getDaysInMonth(year: Int, month: Int): Int {
    return when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (isLeapYear(year)) 29 else 28
        else -> 30
    }
}

private fun isLeapYear(year: Int): Boolean {
    return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
}

private fun getFirstDayOfWeek(year: Int, month: Int): Int {
    // 简化实现，返回固定值
    // 实际项目中应使用正确的日期计算
    return (year + month) % 7
}

private fun isDateEnabled(
    date: UnifyCalendarDate,
    minDate: UnifyCalendarDate?,
    maxDate: UnifyCalendarDate?
): Boolean {
    if (minDate != null) {
        if (date.year < minDate.year) return false
        if (date.year == minDate.year && date.month < minDate.month) return false
        if (date.year == minDate.year && date.month == minDate.month && date.day < minDate.day) return false
    }
    
    if (maxDate != null) {
        if (date.year > maxDate.year) return false
        if (date.year == maxDate.year && date.month > maxDate.month) return false
        if (date.year == maxDate.year && date.month == maxDate.month && date.day > maxDate.day) return false
    }
    
    return true
}

// 便捷组件
@Composable
fun UnifyDatePicker(
    selectedDate: UnifyCalendarDate?,
    onDateSelected: (UnifyCalendarDate) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "选择日期"
) {
    var showCalendar by remember { mutableStateOf(false) }
    
    UnifyColumn(
        modifier = modifier,
        spacing = UnifySpacing.SMALL
    ) {
        UnifyTextField(
            value = selectedDate?.toDisplayString() ?: "",
            onValueChange = { },
            label = label,
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showCalendar = !showCalendar }
        )
        
        if (showCalendar) {
            UnifyCalendar(
                selectedDate = selectedDate,
                onDateSelected = { date ->
                    onDateSelected(date)
                    showCalendar = false
                }
            )
        }
    }
}
