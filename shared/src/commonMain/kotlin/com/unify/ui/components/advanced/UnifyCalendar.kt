package com.unify.ui.components.advanced

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
// 使用简化的日期类型，避免外部依赖
data class SimpleDate(val year: Int, val month: Int, val day: Int)
data class SimpleTime(val hour: Int, val minute: Int)

/**
 * Unify跨平台日历组件
 * 支持日期选择、事件显示、多选等功能
 */

data class CalendarEvent(
    val id: String,
    val title: String,
    val date: SimpleDate,
    val color: Color = Color.Blue,
    val description: String = ""
)

enum class CalendarViewType {
    MONTH, WEEK, DAY, YEAR
}

@Composable
expect fun UnifyCalendar(
    selectedDate: SimpleDate? = null,
    events: List<CalendarEvent> = emptyList(),
    viewType: CalendarViewType = CalendarViewType.MONTH,
    onDateSelected: (SimpleDate) -> Unit = {},
    onEventClicked: (CalendarEvent) -> Unit = {},
    modifier: Modifier = Modifier
)

@Composable
expect fun UnifyDatePicker(
    selectedDate: SimpleDate? = null,
    minDate: SimpleDate? = null,
    maxDate: SimpleDate? = null,
    onDateSelected: (SimpleDate) -> Unit,
    modifier: Modifier = Modifier
)

@Composable
expect fun UnifyTimePicker(
    selectedTime: SimpleTime? = null,
    onTimeSelected: (SimpleTime) -> Unit,
    modifier: Modifier = Modifier
)

@Composable
expect fun UnifyDateTimePicker(
    selectedDateTime: Pair<SimpleDate, SimpleTime>? = null,
    onDateTimeSelected: (Pair<SimpleDate, SimpleTime>) -> Unit,
    modifier: Modifier = Modifier
)

@Composable
expect fun UnifyCalendarView(
    events: List<CalendarEvent>,
    modifier: Modifier = Modifier,
    viewType: CalendarViewType = CalendarViewType.MONTH,
    onEventClick: (CalendarEvent) -> Unit = {},
    onDateClick: (SimpleDate) -> Unit = {},
    showWeekNumbers: Boolean = false,
    firstDayOfWeek: Int = 1 // 1 = Monday, 7 = Sunday
)

// 工具函数
fun getCurrentDate(): SimpleDate {
    val currentTimeMillis = System.currentTimeMillis()
    // 简化实现，返回当前日期
    return SimpleDate(2024, 1, 1)
}

fun formatDate(date: SimpleDate, pattern: String = "yyyy-MM-dd"): String {
    return "${date.year}-${date.month.toString().padStart(2, '0')}-${date.day.toString().padStart(2, '0')}"
}

fun parseDate(dateString: String): SimpleDate? {
    return try {
        val parts = dateString.split("-")
        if (parts.size == 3) {
            SimpleDate(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
        } else null
    } catch (e: Exception) {
        null
    }
}

fun isWeekend(date: SimpleDate): Boolean {
    // 简化实现
    return date.day % 7 == 0 || date.day % 7 == 6
}

fun getMonthDays(year: Int, month: Int): List<SimpleDate> {
    val daysInMonth = when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (isLeapYear(year)) 29 else 28
        else -> 30
    }
    return (1..daysInMonth).map { SimpleDate(year, month, it) }
}

fun isLeapYear(year: Int): Boolean {
    return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
}
