package com.unify.ui.components.advanced

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.unify.ui.LocalUnifyTheme
import com.unify.ui.components.foundation.*
import kotlinx.datetime.*

/**
 * Unify Calendar 组件
 * 支持多平台适配的统一日历组件，参考 KuiklyUI 设计规范
 */

/**
 * 日历视图类型
 */
enum class UnifyCalendarViewType {
    MONTH,      // 月视图
    WEEK,       // 周视图
    YEAR,       // 年视图
    AGENDA      // 议程视图
}

/**
 * 日历事件数据
 */
data class UnifyCalendarEvent(
    val id: String,
    val title: String,
    val description: String? = null,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val color: Color? = null,
    val isAllDay: Boolean = true
)

/**
 * 日历日期状态
 */
data class UnifyCalendarDateState(
    val date: LocalDate,
    val isSelected: Boolean = false,
    val isToday: Boolean = false,
    val isInCurrentMonth: Boolean = true,
    val events: List<UnifyCalendarEvent> = emptyList(),
    val isEnabled: Boolean = true
)

/**
 * 主要 Unify Calendar 组件
 */
@Composable
fun UnifyCalendar(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    viewType: UnifyCalendarViewType = UnifyCalendarViewType.MONTH,
    events: List<UnifyCalendarEvent> = emptyList(),
    minDate: LocalDate? = null,
    maxDate: LocalDate? = null,
    showWeekNumbers: Boolean = false,
    showEvents: Boolean = true,
    firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    var currentMonth by remember { mutableStateOf(YearMonth(selectedDate.year, selectedDate.month)) }
    
    Column(
        modifier = modifier.semantics {
            contentDescription?.let { 
                this.contentDescription = it 
            }
        }
    ) {
        when (viewType) {
            UnifyCalendarViewType.MONTH -> {
                UnifyMonthCalendar(
                    selectedDate = selectedDate,
                    onDateSelected = onDateSelected,
                    currentMonth = currentMonth,
                    onMonthChanged = { currentMonth = it },
                    events = events,
                    minDate = minDate,
                    maxDate = maxDate,
                    showWeekNumbers = showWeekNumbers,
                    showEvents = showEvents,
                    firstDayOfWeek = firstDayOfWeek
                )
            }
            UnifyCalendarViewType.WEEK -> {
                UnifyWeekCalendar(
                    selectedDate = selectedDate,
                    onDateSelected = onDateSelected,
                    events = events,
                    firstDayOfWeek = firstDayOfWeek
                )
            }
            UnifyCalendarViewType.YEAR -> {
                UnifyYearCalendar(
                    selectedDate = selectedDate,
                    onDateSelected = onDateSelected,
                    currentYear = selectedDate.year,
                    events = events
                )
            }
            UnifyCalendarViewType.AGENDA -> {
                UnifyAgendaCalendar(
                    selectedDate = selectedDate,
                    events = events
                )
            }
        }
    }
}

/**
 * 月视图日历
 */
@Composable
private fun UnifyMonthCalendar(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    currentMonth: YearMonth,
    onMonthChanged: (YearMonth) -> Unit,
    events: List<UnifyCalendarEvent>,
    minDate: LocalDate?,
    maxDate: LocalDate?,
    showWeekNumbers: Boolean,
    showEvents: Boolean,
    firstDayOfWeek: DayOfWeek
) {
    val theme = LocalUnifyTheme.current
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    
    Column {
        // 月份导航栏
        UnifyCalendarHeader(
            currentMonth = currentMonth,
            onMonthChanged = onMonthChanged
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 星期标题
        UnifyCalendarWeekHeader(firstDayOfWeek = firstDayOfWeek)
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 日期网格
        val daysInMonth = getDaysInMonth(currentMonth, firstDayOfWeek)
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(if (showWeekNumbers) 8 else 7),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(daysInMonth) { dateState ->
                UnifyCalendarDateCell(
                    dateState = dateState.copy(
                        isSelected = dateState.date == selectedDate,
                        isToday = dateState.date == today,
                        events = if (showEvents) {
                            events.filter { event ->
                                dateState.date >= event.startDate && 
                                dateState.date <= (event.endDate ?: event.startDate)
                            }
                        } else emptyList()
                    ),
                    onDateSelected = onDateSelected,
                    isEnabled = isDateEnabled(dateState.date, minDate, maxDate)
                )
            }
        }
        
        // 事件列表
        if (showEvents && events.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            UnifyCalendarEventList(
                events = events.filter { event ->
                    selectedDate >= event.startDate && 
                    selectedDate <= (event.endDate ?: event.startDate)
                }
            )
        }
    }
}

/**
 * 周视图日历
 */
@Composable
private fun UnifyWeekCalendar(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    events: List<UnifyCalendarEvent>,
    firstDayOfWeek: DayOfWeek
) {
    val weekDates = getWeekDates(selectedDate, firstDayOfWeek)
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    
    Column {
        // 周标题
        UnifyText(
            text = "第 ${selectedDate.weekOfYear()} 周",
            variant = UnifyTextVariant.TITLE_MEDIUM,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // 周日期
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            weekDates.forEach { date ->
                UnifyCalendarDateCell(
                    dateState = UnifyCalendarDateState(
                        date = date,
                        isSelected = date == selectedDate,
                        isToday = date == today,
                        events = events.filter { event ->
                            date >= event.startDate && 
                            date <= (event.endDate ?: event.startDate)
                        }
                    ),
                    onDateSelected = onDateSelected,
                    isEnabled = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // 周事件
        Spacer(modifier = Modifier.height(16.dp))
        UnifyCalendarEventList(
            events = events.filter { event ->
                weekDates.any { date ->
                    date >= event.startDate && 
                    date <= (event.endDate ?: event.startDate)
                }
            }
        )
    }
}

/**
 * 年视图日历
 */
@Composable
private fun UnifyYearCalendar(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    currentYear: Int,
    events: List<UnifyCalendarEvent>
) {
    Column {
        // 年份标题
        UnifyText(
            text = currentYear.toString(),
            variant = UnifyTextVariant.TITLE_LARGE,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // 月份网格
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(12) { monthIndex ->
                val month = Month.values()[monthIndex]
                val yearMonth = YearMonth(currentYear, month)
                
                UnifyMiniMonthCalendar(
                    yearMonth = yearMonth,
                    selectedDate = selectedDate,
                    onDateSelected = onDateSelected,
                    events = events
                )
            }
        }
    }
}

/**
 * 议程视图日历
 */
@Composable
private fun UnifyAgendaCalendar(
    selectedDate: LocalDate,
    events: List<UnifyCalendarEvent>
) {
    Column {
        // 选中日期标题
        UnifyText(
            text = formatDate(selectedDate),
            variant = UnifyTextVariant.TITLE_MEDIUM,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // 当日事件
        val dayEvents = events.filter { event ->
            selectedDate >= event.startDate && 
            selectedDate <= (event.endDate ?: event.startDate)
        }
        
        if (dayEvents.isEmpty()) {
            UnifyText(
                text = "今日无事件",
                variant = UnifyTextVariant.BODY_MEDIUM,
                color = LocalUnifyTheme.current.colors.onSurfaceVariant
            )
        } else {
            UnifyCalendarEventList(events = dayEvents)
        }
    }
}

/**
 * 日历头部组件
 */
@Composable
private fun UnifyCalendarHeader(
    currentMonth: YearMonth,
    onMonthChanged: (YearMonth) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        UnifyIconButton(
            icon = Icons.Default.ChevronLeft,
            onClick = { onMonthChanged(currentMonth.minusMonths(1)) },
            contentDescription = "上个月"
        )
        
        UnifyText(
            text = "${currentMonth.year}年${currentMonth.monthNumber}月",
            variant = UnifyTextVariant.TITLE_MEDIUM
        )
        
        UnifyIconButton(
            icon = Icons.Default.ChevronRight,
            onClick = { onMonthChanged(currentMonth.plusMonths(1)) },
            contentDescription = "下个月"
        )
    }
}

/**
 * 星期标题组件
 */
@Composable
private fun UnifyCalendarWeekHeader(
    firstDayOfWeek: DayOfWeek
) {
    val weekDays = getWeekDayNames(firstDayOfWeek)
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        weekDays.forEach { dayName ->
            UnifyText(
                text = dayName,
                variant = UnifyTextVariant.CAPTION,
                color = LocalUnifyTheme.current.colors.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 日期单元格组件
 */
@Composable
private fun UnifyCalendarDateCell(
    dateState: UnifyCalendarDateState,
    onDateSelected: (LocalDate) -> Unit,
    isEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val theme = LocalUnifyTheme.current
    
    val backgroundColor = when {
        dateState.isSelected -> theme.colors.primary
        dateState.isToday -> theme.colors.primaryContainer
        else -> Color.Transparent
    }
    
    val textColor = when {
        !isEnabled -> theme.colors.onSurface.copy(alpha = 0.38f)
        dateState.isSelected -> theme.colors.onPrimary
        dateState.isToday -> theme.colors.onPrimaryContainer
        !dateState.isInCurrentMonth -> theme.colors.onSurface.copy(alpha = 0.6f)
        else -> theme.colors.onSurface
    }
    
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(enabled = isEnabled) {
                onDateSelected(dateState.date)
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            UnifyText(
                text = dateState.date.dayOfMonth.toString(),
                variant = UnifyTextVariant.BODY_MEDIUM,
                color = textColor,
                fontWeight = if (dateState.isToday || dateState.isSelected) {
                    FontWeight.Bold
                } else FontWeight.Normal
            )
            
            // 事件指示器
            if (dateState.events.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    repeat(minOf(dateState.events.size, 3)) { index ->
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .background(
                                    dateState.events.getOrNull(index)?.color 
                                        ?: theme.colors.primary,
                                    CircleShape
                                )
                        )
                    }
                }
            }
        }
    }
}

/**
 * 迷你月份日历组件
 */
@Composable
private fun UnifyMiniMonthCalendar(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    events: List<UnifyCalendarEvent>
) {
    val theme = LocalUnifyTheme.current
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                theme.colors.surfaceVariant.copy(alpha = 0.5f),
                RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
    ) {
        // 月份标题
        UnifyText(
            text = "${yearMonth.monthNumber}月",
            variant = UnifyTextVariant.CAPTION,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // 简化的日期网格
        val daysInMonth = getDaysInMonth(yearMonth, DayOfWeek.MONDAY)
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.height(120.dp)
        ) {
            items(daysInMonth.take(28)) { dateState -> // 只显示前4周
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .background(
                            if (dateState.date == selectedDate) {
                                theme.colors.primary
                            } else Color.Transparent,
                            CircleShape
                        )
                        .clickable { onDateSelected(dateState.date) },
                    contentAlignment = Alignment.Center
                ) {
                    UnifyText(
                        text = dateState.date.dayOfMonth.toString(),
                        variant = UnifyTextVariant.CAPTION,
                        color = if (dateState.date == selectedDate) {
                            theme.colors.onPrimary
                        } else if (dateState.isInCurrentMonth) {
                            theme.colors.onSurface
                        } else {
                            theme.colors.onSurface.copy(alpha = 0.6f)
                        }
                    )
                }
            }
        }
    }
}

/**
 * 事件列表组件
 */
@Composable
private fun UnifyCalendarEventList(
    events: List<UnifyCalendarEvent>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        events.forEach { event ->
            UnifyCalendarEventItem(event = event)
        }
    }
}

/**
 * 事件项组件
 */
@Composable
private fun UnifyCalendarEventItem(
    event: UnifyCalendarEvent
) {
    val theme = LocalUnifyTheme.current
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                theme.colors.surfaceVariant.copy(alpha = 0.5f),
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 事件颜色指示器
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(
                    event.color ?: theme.colors.primary,
                    CircleShape
                )
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            UnifyText(
                text = event.title,
                variant = UnifyTextVariant.BODY_MEDIUM,
                fontWeight = FontWeight.Medium
            )
            
            event.description?.let { description ->
                UnifyText(
                    text = description,
                    variant = UnifyTextVariant.CAPTION,
                    color = theme.colors.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 辅助函数
 */
private fun getDaysInMonth(
    yearMonth: YearMonth,
    firstDayOfWeek: DayOfWeek
): List<UnifyCalendarDateState> {
    val firstDayOfMonth = yearMonth.atDay(1)
    val lastDayOfMonth = yearMonth.atEndOfMonth()
    
    val startDate = firstDayOfMonth.let { date ->
        val dayOfWeek = date.dayOfWeek
        val daysToSubtract = (dayOfWeek.ordinal - firstDayOfWeek.ordinal + 7) % 7
        date.minusDays(daysToSubtract)
    }
    
    val endDate = lastDayOfMonth.let { date ->
        val dayOfWeek = date.dayOfWeek
        val daysToAdd = (firstDayOfWeek.ordinal - dayOfWeek.ordinal + 6) % 7
        date.plusDays(daysToAdd)
    }
    
    val dates = mutableListOf<UnifyCalendarDateState>()
    var currentDate = startDate
    
    while (currentDate <= endDate) {
        dates.add(
            UnifyCalendarDateState(
                date = currentDate,
                isInCurrentMonth = currentDate.month == yearMonth.month
            )
        )
        currentDate = currentDate.plusDays(1)
    }
    
    return dates
}

private fun getWeekDates(date: LocalDate, firstDayOfWeek: DayOfWeek): List<LocalDate> {
    val dayOfWeek = date.dayOfWeek
    val daysToSubtract = (dayOfWeek.ordinal - firstDayOfWeek.ordinal + 7) % 7
    val startOfWeek = date.minusDays(daysToSubtract)
    
    return (0..6).map { startOfWeek.plusDays(it) }
}

private fun getWeekDayNames(firstDayOfWeek: DayOfWeek): List<String> {
    val dayNames = listOf("一", "二", "三", "四", "五", "六", "日")
    val startIndex = (firstDayOfWeek.ordinal + 6) % 7 // 转换为中文星期索引
    return (0..6).map { dayNames[(startIndex + it) % 7] }
}

private fun isDateEnabled(
    date: LocalDate,
    minDate: LocalDate?,
    maxDate: LocalDate?
): Boolean {
    return (minDate == null || date >= minDate) && 
           (maxDate == null || date <= maxDate)
}

private fun formatDate(date: LocalDate): String {
    return "${date.year}年${date.monthNumber}月${date.dayOfMonth}日"
}

private fun LocalDate.weekOfYear(): Int {
    // 简化的周数计算
    val firstDayOfYear = LocalDate(year, 1, 1)
    val dayOfYear = dayOfYear
    return (dayOfYear + firstDayOfYear.dayOfWeek.ordinal) / 7 + 1
}
