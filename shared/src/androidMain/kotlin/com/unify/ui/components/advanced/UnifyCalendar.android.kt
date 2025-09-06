@file:OptIn(ExperimentalMaterial3Api::class)

package com.unify.ui.components.advanced

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.util.*

@Composable
actual fun UnifyCalendar(
    selectedDate: SimpleDate?,
    events: List<CalendarEvent>,
    viewType: CalendarViewType,
    onDateSelected: (SimpleDate) -> Unit,
    onEventClicked: (CalendarEvent) -> Unit,
    modifier: Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Calendar",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            when (viewType) {
                CalendarViewType.MONTH -> {
                    MonthCalendarView(
                        selectedDate = selectedDate,
                        events = events,
                        onDateSelected = onDateSelected,
                        onEventClicked = onEventClicked
                    )
                }
                CalendarViewType.WEEK -> {
                    WeekCalendarView(
                        selectedDate = selectedDate,
                        events = events,
                        onDateSelected = onDateSelected
                    )
                }
                CalendarViewType.DAY -> {
                    DayCalendarView(
                        selectedDate = selectedDate ?: getCurrentDate(),
                        events = events,
                        onEventClicked = onEventClicked
                    )
                }
                CalendarViewType.YEAR -> {
                    YearCalendarView(
                        selectedDate = selectedDate,
                        onDateSelected = onDateSelected
                    )
                }
            }
        }
    }
}

@Composable
actual fun UnifyDatePicker(
    selectedDate: SimpleDate?,
    minDate: SimpleDate?,
    maxDate: SimpleDate?,
    onDateSelected: (SimpleDate) -> Unit,
    modifier: Modifier
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate?.let { dateToMillis(it) }
    )
    
    LaunchedEffect(datePickerState.selectedDateMillis) {
        datePickerState.selectedDateMillis?.let { millis ->
            val date = millisToDate(millis)
            onDateSelected(date)
        }
    }
    
    DatePicker(
        state = datePickerState,
        modifier = modifier
    )
}

@Composable
actual fun UnifyTimePicker(
    selectedTime: SimpleTime?,
    onTimeSelected: (SimpleTime) -> Unit,
    modifier: Modifier
) {
    val timePickerState = rememberTimePickerState(
        initialHour = selectedTime?.hour ?: 12,
        initialMinute = selectedTime?.minute ?: 0
    )
    
    LaunchedEffect(timePickerState.hour, timePickerState.minute) {
        onTimeSelected(SimpleTime(timePickerState.hour, timePickerState.minute))
    }
    
    TimePicker(
        state = timePickerState,
        modifier = modifier
    )
}

@Composable
actual fun UnifyDateTimePicker(
    selectedDateTime: Pair<SimpleDate, SimpleTime>?,
    onDateTimeSelected: (Pair<SimpleDate, SimpleTime>) -> Unit,
    modifier: Modifier
) {
    Column(modifier = modifier) {
        UnifyDatePicker(
            selectedDate = selectedDateTime?.first,
            onDateSelected = { date ->
                val time = selectedDateTime?.second ?: SimpleTime(12, 0)
                onDateTimeSelected(Pair(date, time))
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        UnifyTimePicker(
            selectedTime = selectedDateTime?.second,
            onTimeSelected = { time ->
                val date = selectedDateTime?.first ?: getCurrentDate()
                onDateTimeSelected(Pair(date, time))
            }
        )
    }
}

@Composable
actual fun UnifyCalendarView(
    events: List<CalendarEvent>,
    modifier: Modifier,
    viewType: CalendarViewType,
    onEventClick: (CalendarEvent) -> Unit,
    onDateClick: (SimpleDate) -> Unit,
    showWeekNumbers: Boolean,
    firstDayOfWeek: Int
) {
    UnifyCalendar(
        selectedDate = null,
        events = events,
        viewType = viewType,
        onDateSelected = onDateClick,
        onEventClicked = onEventClick,
        modifier = modifier
    )
}

@Composable
private fun MonthCalendarView(
    selectedDate: SimpleDate?,
    events: List<CalendarEvent>,
    onDateSelected: (SimpleDate) -> Unit,
    onEventClicked: (CalendarEvent) -> Unit
) {
    val currentDate = getCurrentDate()
    val monthDays = getMonthDays(currentDate.year, currentDate.month)
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        contentPadding = PaddingValues(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(monthDays) { date ->
            val isSelected = selectedDate == date
            val dayEvents = events.filter { it.date == date }
            
            Card(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clickable { onDateSelected(date) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) 
                        MaterialTheme.colorScheme.primary 
                    else MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = date.day.toString(),
                        color = if (isSelected) 
                            MaterialTheme.colorScheme.onPrimary 
                        else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                    
                    if (dayEvents.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    color = dayEvents.first().color,
                                    shape = androidx.compose.foundation.shape.CircleShape
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekCalendarView(
    selectedDate: SimpleDate?,
    events: List<CalendarEvent>,
    onDateSelected: (SimpleDate) -> Unit
) {
    val currentDate = getCurrentDate()
    val weekDays = (0..6).map { 
        SimpleDate(currentDate.year, currentDate.month, currentDate.day + it) 
    }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        weekDays.forEach { date ->
            val isSelected = selectedDate == date
            Card(
                modifier = Modifier
                    .weight(1f)
                    .padding(2.dp)
                    .clickable { onDateSelected(date) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) 
                        MaterialTheme.colorScheme.primary 
                    else MaterialTheme.colorScheme.surface
                )
            ) {
                Text(
                    text = date.day.toString(),
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center,
                    color = if (isSelected) 
                        MaterialTheme.colorScheme.onPrimary 
                    else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun DayCalendarView(
    selectedDate: SimpleDate,
    events: List<CalendarEvent>,
    onEventClicked: (CalendarEvent) -> Unit
) {
    val dayEvents = events.filter { it.date == selectedDate }
    
    Column {
        Text(
            text = formatDate(selectedDate),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        if (dayEvents.isEmpty()) {
            Text(
                text = "No events for this day",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            dayEvents.forEach { event ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onEventClicked(event) }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    color = event.color,
                                    shape = androidx.compose.foundation.shape.CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = event.title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            if (event.description.isNotEmpty()) {
                                Text(
                                    text = event.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
private fun YearCalendarView(
    selectedDate: SimpleDate?,
    onDateSelected: (SimpleDate) -> Unit
) {
    val currentYear = getCurrentDate().year
    val months = (1..12).map { month ->
        Pair(month, getMonthName(month))
    }
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(months) { (month, monthName) ->
            Card(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clickable { 
                        onDateSelected(SimpleDate(currentYear, month, 1))
                    }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = monthName,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// Helper functions
private fun dateToMillis(date: SimpleDate): Long {
    val calendar = Calendar.getInstance()
    calendar.set(date.year, date.month - 1, date.day)
    return calendar.timeInMillis
}

private fun millisToDate(millis: Long): SimpleDate {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = millis
    return SimpleDate(
        year = calendar.get(Calendar.YEAR),
        month = calendar.get(Calendar.MONTH) + 1,
        day = calendar.get(Calendar.DAY_OF_MONTH)
    )
}

private fun getMonthName(month: Int): String {
    return when (month) {
        1 -> "Jan"
        2 -> "Feb"
        3 -> "Mar"
        4 -> "Apr"
        5 -> "May"
        6 -> "Jun"
        7 -> "Jul"
        8 -> "Aug"
        9 -> "Sep"
        10 -> "Oct"
        11 -> "Nov"
        12 -> "Dec"
        else -> "Unknown"
    }
}
