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
import androidx.compose.ui.unit.dp

/**
 * Desktop平台Calendar组件actual实现
 */

@Composable
actual fun UnifyCalendar(
    selectedDate: SimpleDate?,
    events: List<CalendarEvent>,
    viewType: CalendarViewType,
    onDateSelected: (SimpleDate) -> Unit,
    onEventClicked: (CalendarEvent) -> Unit,
    modifier: Modifier,
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "日历组件 - Desktop模拟",
                style = MaterialTheme.typography.headlineSmall,
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (viewType) {
                CalendarViewType.MONTH -> {
                    MonthView(
                        selectedDate = selectedDate,
                        events = events,
                        onDateSelected = onDateSelected,
                        onEventClicked = onEventClicked,
                    )
                }
                CalendarViewType.WEEK -> {
                    WeekView(
                        selectedDate = selectedDate,
                        events = events,
                        onDateSelected = onDateSelected,
                        onEventClicked = onEventClicked,
                    )
                }
                CalendarViewType.DAY -> {
                    DayView(
                        selectedDate = selectedDate ?: getCurrentDate(),
                        events = events,
                        onEventClicked = onEventClicked,
                    )
                }
                CalendarViewType.YEAR -> {
                    YearView(
                        selectedDate = selectedDate,
                        onDateSelected = onDateSelected,
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
    modifier: Modifier,
) {
    var currentMonth by remember { mutableStateOf(selectedDate?.month ?: 1) }
    var currentYear by remember { mutableStateOf(selectedDate?.year ?: 2024) }

    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "日期选择器 - Desktop模拟",
                style = MaterialTheme.typography.titleMedium,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 月份年份选择
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = {
                    if (currentMonth > 1) {
                        currentMonth--
                    } else {
                        currentMonth = 12
                        currentYear--
                    }
                }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "上个月")
                }

                Text(
                    text = "${currentYear}年${currentMonth}月",
                    style = MaterialTheme.typography.titleMedium,
                )

                IconButton(onClick = {
                    if (currentMonth < 12) {
                        currentMonth++
                    } else {
                        currentMonth = 1
                        currentYear++
                    }
                }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "下个月")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 日期网格
            val daysInMonth = getMonthDays(currentYear, currentMonth)
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(daysInMonth) { date ->
                    val isSelected = selectedDate == date
                    FilterChip(
                        onClick = { onDateSelected(date) },
                        label = { Text(date.day.toString()) },
                        selected = isSelected,
                    )
                }
            }
        }
    }
}

@Composable
actual fun UnifyTimePicker(
    selectedTime: SimpleTime?,
    onTimeSelected: (SimpleTime) -> Unit,
    modifier: Modifier,
) {
    var hour by remember { mutableStateOf(selectedTime?.hour ?: 12) }
    var minute by remember { mutableStateOf(selectedTime?.minute ?: 0) }

    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "时间选择器 - Desktop模拟",
                style = MaterialTheme.typography.titleMedium,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 小时选择
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("小时", style = MaterialTheme.typography.bodySmall)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            hour = if (hour > 0) hour - 1 else 23
                            onTimeSelected(SimpleTime(hour, minute))
                        }) {
                            Icon(Icons.Default.Remove, contentDescription = "减少小时")
                        }
                        Text(
                            text = hour.toString().padStart(2, '0'),
                            style = MaterialTheme.typography.headlineMedium,
                        )
                        IconButton(onClick = {
                            hour = if (hour < 23) hour + 1 else 0
                            onTimeSelected(SimpleTime(hour, minute))
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "增加小时")
                        }
                    }
                }

                Text(":", style = MaterialTheme.typography.headlineMedium)

                // 分钟选择
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("分钟", style = MaterialTheme.typography.bodySmall)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            minute = if (minute > 0) minute - 1 else 59
                            onTimeSelected(SimpleTime(hour, minute))
                        }) {
                            Icon(Icons.Default.Remove, contentDescription = "减少分钟")
                        }
                        Text(
                            text = minute.toString().padStart(2, '0'),
                            style = MaterialTheme.typography.headlineMedium,
                        )
                        IconButton(onClick = {
                            minute = if (minute < 59) minute + 1 else 0
                            onTimeSelected(SimpleTime(hour, minute))
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "增加分钟")
                        }
                    }
                }
            }
        }
    }
}

@Composable
actual fun UnifyDateTimePicker(
    selectedDateTime: Pair<SimpleDate, SimpleTime>?,
    onDateTimeSelected: (Pair<SimpleDate, SimpleTime>) -> Unit,
    modifier: Modifier,
) {
    val currentDate = selectedDateTime?.first ?: getCurrentDate()
    val currentTime = selectedDateTime?.second ?: SimpleTime(12, 0)

    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "日期时间选择器 - Desktop模拟",
                style = MaterialTheme.typography.titleMedium,
            )

            Spacer(modifier = Modifier.height(16.dp))

            UnifyDatePicker(
                selectedDate = currentDate,
                onDateSelected = { date ->
                    onDateTimeSelected(Pair(date, currentTime))
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            UnifyTimePicker(
                selectedTime = currentTime,
                onTimeSelected = { time ->
                    onDateTimeSelected(Pair(currentDate, time))
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
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
    firstDayOfWeek: Int,
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "日历视图 - Desktop模拟",
                style = MaterialTheme.typography.titleMedium,
            )

            if (showWeekNumbers) {
                Text(
                    text = "显示周数: 是",
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Text(
                text = "一周开始: ${if (firstDayOfWeek == 1) "周一" else "周日"}",
                style = MaterialTheme.typography.bodySmall,
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (events.isNotEmpty()) {
                Text("事件列表:", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(events) { event ->
                        Card(
                            onClick = { onEventClick(event) },
                            colors =
                                CardDefaults.cardColors(
                                    containerColor = event.color.copy(alpha = 0.1f),
                                ),
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = event.title,
                                    style = MaterialTheme.typography.titleSmall,
                                )
                                Text(
                                    text = formatDate(event.date),
                                    style = MaterialTheme.typography.bodySmall,
                                )
                                if (event.description.isNotEmpty()) {
                                    Text(
                                        text = event.description,
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Text("暂无事件")
            }
        }
    }
}

@Composable
private fun MonthView(
    selectedDate: SimpleDate?,
    events: List<CalendarEvent>,
    onDateSelected: (SimpleDate) -> Unit,
    onEventClicked: (CalendarEvent) -> Unit,
) {
    Text("月视图 - ${selectedDate?.let { formatDate(it) } ?: "未选择"}")
}

@Composable
private fun WeekView(
    selectedDate: SimpleDate?,
    events: List<CalendarEvent>,
    onDateSelected: (SimpleDate) -> Unit,
    onEventClicked: (CalendarEvent) -> Unit,
) {
    Text("周视图 - ${selectedDate?.let { formatDate(it) } ?: "未选择"}")
}

@Composable
private fun DayView(
    selectedDate: SimpleDate,
    events: List<CalendarEvent>,
    onEventClicked: (CalendarEvent) -> Unit,
) {
    Text("日视图 - ${formatDate(selectedDate)}")
}

@Composable
private fun YearView(
    selectedDate: SimpleDate?,
    onDateSelected: (SimpleDate) -> Unit,
) {
    Text("年视图 - ${selectedDate?.year ?: "未选择"}")
}
