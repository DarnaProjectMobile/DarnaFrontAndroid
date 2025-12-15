package com.sim.darna.components

import android.app.TimePickerDialog
import android.widget.TimePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateVisiteDialog(
    onDismiss: () -> Unit,
    onSubmit: (Long, Int, Int, String, String) -> Unit,
    isLoading: Boolean
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    
    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var selectedHour by remember { mutableStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableStateOf(calendar.get(Calendar.MINUTE)) }
    
    var notes by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    
    var showDatePicker by remember { mutableStateOf(false) }
    
    // Helper to format date
    val dateString = remember(selectedDateMillis) {
        val date = Date(selectedDateMillis)
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
    }
    
    // Helper to format time
    val timeString = remember(selectedHour, selectedMinute) {
        String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)
    }

    if (showDatePicker) {
        CustomDatePickerDialog(
            initialYear = calendar.get(Calendar.YEAR),
            initialMonth = calendar.get(Calendar.MONTH),
            initialDay = calendar.get(Calendar.DAY_OF_MONTH),
            onDateSelected = { year, month, day ->
                val cal = Calendar.getInstance()
                cal.set(year, month, day)
                selectedDateMillis = cal.timeInMillis
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Réserver une visite",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                // Date Picker
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = dateString,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Date") },
                        leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false, // Disable typing, rely on click
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = Color.Black,
                            disabledLabelColor = Color.Gray,
                            disabledBorderColor = Color.Gray,
                            disabledLeadingIconColor = Color.Gray
                        )
                    )
                    // Overlay for click since enabled=false blocks click
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showDatePicker = true }
                    )
                }

                // Time Picker
                val timePickerDialog = TimePickerDialog(
                    context,
                    { _: TimePicker, hour: Int, minute: Int ->
                        selectedHour = hour
                        selectedMinute = minute
                    },
                    selectedHour,
                    selectedMinute,
                    true
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = timeString,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Heure") },
                        leadingIcon = { Icon(Icons.Default.Schedule, null) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = Color.Black,
                            disabledLabelColor = Color.Gray,
                            disabledBorderColor = Color.Gray,
                            disabledLeadingIconColor = Color.Gray
                        )
                    )
                     // Overlay for click
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { timePickerDialog.show() }
                    )
                }

                // Phone
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Téléphone de contact") },
                    leadingIcon = { Icon(Icons.Default.Phone, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optionnel)") },
                    leadingIcon = { Icon(Icons.Default.Edit, null) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Annuler")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (phone.isNotBlank()) {
                                onSubmit(selectedDateMillis, selectedHour, selectedMinute, notes, phone)
                            }
                        },
                        enabled = !isLoading && phone.isNotBlank()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Confirmer")
                    }
                }
            }
        }
    }
}
