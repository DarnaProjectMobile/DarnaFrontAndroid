package com.sim.darna.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sim.darna.ui.theme.AppTheme
import java.util.*

@Composable
fun CustomDatePickerDialog(
    initialYear: Int,
    initialMonth: Int,
    initialDay: Int,
    onDateSelected: (Int, Int, Int) -> Unit,
    onDismiss: () -> Unit,
    minDate: Long? = null,
    maxDate: Long? = null
) {
    var selectedYear by remember { mutableStateOf(initialYear) }
    var selectedMonth by remember { mutableStateOf(initialMonth) }
    var selectedDay by remember { mutableStateOf(initialDay) }
    
    val calendar = remember { Calendar.getInstance() }
    calendar.set(selectedYear, selectedMonth, selectedDay)
    
    val minCalendar = minDate?.let {
        Calendar.getInstance().apply { timeInMillis = it }
    }
    val maxCalendar = maxDate?.let {
        Calendar.getInstance().apply { timeInMillis = it }
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header with selected date
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppTheme.primary)
                        .padding(vertical = 20.dp, horizontal = 16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = selectedYear.toString(),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatSelectedDate(selectedYear, selectedMonth, selectedDay),
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Month navigation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (selectedMonth == 0) {
                                selectedMonth = 11
                                selectedYear--
                            } else {
                                selectedMonth--
                            }
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Previous month",
                            tint = AppTheme.primary
                        )
                    }
                    
                    Text(
                        text = getMonthYearText(selectedMonth, selectedYear),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.textPrimary
                    )
                    
                    IconButton(
                        onClick = {
                            if (selectedMonth == 11) {
                                selectedMonth = 0
                                selectedYear++
                            } else {
                                selectedMonth++
                            }
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = "Next month",
                            tint = AppTheme.primary
                        )
                    }
                }
                
                // Calendar grid
                CalendarGrid(
                    year = selectedYear,
                    month = selectedMonth,
                    selectedDay = selectedDay,
                    onDaySelected = { day ->
                        selectedDay = day
                    },
                    minCalendar = minCalendar,
                    maxCalendar = maxCalendar
                )
                
                // Action buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "CANCEL",
                            color = AppTheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    TextButton(
                        onClick = {
                            onDateSelected(selectedYear, selectedMonth, selectedDay)
                        }
                    ) {
                        Text(
                            text = "OK",
                            color = AppTheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarGrid(
    year: Int,
    month: Int,
    selectedDay: Int,
    onDaySelected: (Int) -> Unit,
    minCalendar: Calendar?,
    maxCalendar: Calendar?
) {
    val calendar = Calendar.getInstance()
    calendar.set(year, month, 1)
    
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    
    // Adjust first day of week (Calendar.SUNDAY = 1, we want Monday = 0)
    val startOffset = (firstDayOfWeek - Calendar.MONDAY + 7) % 7
    
    val dayLabels = listOf("S", "M", "T", "W", "T", "F", "S")
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Day labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            dayLabels.forEach { label ->
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppTheme.textSecondary,
                    modifier = Modifier
                        .width(40.dp)
                        .wrapContentHeight(),
                    textAlign = TextAlign.Center
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Calendar days
        var currentDay = 1
        repeat(6) { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(7) { dayOfWeek ->
                    if (week == 0 && dayOfWeek < startOffset) {
                        // Empty cell before first day
                        Spacer(modifier = Modifier.size(40.dp))
                    } else if (currentDay <= daysInMonth) {
                        val day = currentDay
                        val isSelected = day == selectedDay
                        val isEnabled = isDateEnabled(year, month, day, minCalendar, maxCalendar)
                        
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) AppTheme.primary else Color.Transparent
                                )
                                .clickable(enabled = isEnabled) {
                                    onDaySelected(day)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.toString(),
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = when {
                                    isSelected -> Color.White
                                    !isEnabled -> AppTheme.textSecondary.copy(alpha = 0.3f)
                                    else -> AppTheme.textPrimary
                                },
                                textAlign = TextAlign.Center
                            )
                        }
                        currentDay++
                    } else {
                        // Empty cell after last day
                        Spacer(modifier = Modifier.size(40.dp))
                    }
                }
            }
        }
    }
}

private fun formatSelectedDate(year: Int, month: Int, day: Int): String {
    val calendar = Calendar.getInstance()
    calendar.set(year, month, day)
    
    val dayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> "Lun"
        Calendar.TUESDAY -> "Mar"
        Calendar.WEDNESDAY -> "Mer"
        Calendar.THURSDAY -> "Jeu"
        Calendar.FRIDAY -> "Ven"
        Calendar.SATURDAY -> "Sam"
        Calendar.SUNDAY -> "Dim"
        else -> ""
    }
    
    val monthName = when (month) {
        0 -> "Jan"
        1 -> "Fév"
        2 -> "Mar"
        3 -> "Avr"
        4 -> "Mai"
        5 -> "Juin"
        6 -> "Juil"
        7 -> "Août"
        8 -> "Sep"
        9 -> "Oct"
        10 -> "Nov"
        11 -> "Déc"
        else -> ""
    }
    
    return "$dayOfWeek, $monthName $day"
}

private fun getMonthYearText(month: Int, year: Int): String {
    val monthName = when (month) {
        0 -> "Janvier"
        1 -> "Février"
        2 -> "Mars"
        3 -> "Avril"
        4 -> "Mai"
        5 -> "Juin"
        6 -> "Juillet"
        7 -> "Août"
        8 -> "Septembre"
        9 -> "Octobre"
        10 -> "Novembre"
        11 -> "Décembre"
        else -> ""
    }
    return "$monthName $year"
}

private fun isDateEnabled(year: Int, month: Int, day: Int, minCalendar: Calendar?, maxCalendar: Calendar?): Boolean {
    val calendar = Calendar.getInstance()
    calendar.set(year, month, day)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    
    val dateMillis = calendar.timeInMillis
    
    if (minCalendar != null && dateMillis < minCalendar.timeInMillis) {
        return false
    }
    if (maxCalendar != null && dateMillis > maxCalendar.timeInMillis) {
        return false
    }
    return true
}

