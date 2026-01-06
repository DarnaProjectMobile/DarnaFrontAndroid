package com.sim.darna.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Note
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.sim.darna.components.CustomDatePickerDialog
import com.sim.darna.factory.VisiteVmFactory
import com.sim.darna.ui.theme.AppTheme
import com.sim.darna.utils.ApiConfig
import com.sim.darna.visite.VisiteViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookVisitScreen(navController: NavController, propertyId: String) {
    val context = LocalContext.current
    val viewModel: VisiteViewModel = viewModel(factory = VisiteVmFactory(ApiConfig.BASE_URL, context))
    val uiState by viewModel.state.collectAsState()

    var selectedDate by remember { mutableStateOf<Date?>(null) }
    var selectedHour by remember { mutableStateOf(10) }
    var selectedMinute by remember { mutableStateOf(0) }
    var notes by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH)
    val timeFormat = SimpleDateFormat("HH:mm", Locale.FRENCH)

    LaunchedEffect(uiState.message) {
        if (uiState.message != null && uiState.message!!.contains("succès", ignoreCase = true)) {
            showSuccessDialog = true
            viewModel.clearFeedback()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearFeedback()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Réserver une visite", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1382B3),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header Information
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, null, tint = Color(0xFF1382B3))
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "Détails de la demande",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1382B3)
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Le propriétaire recevra votre demande et pourra l'accepter ou la refuser. Vous serez notifié de sa réponse.",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                // Date Selection
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Date de la visite", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = selectedDate?.let { dateFormat.format(it) } ?: "",
                            onValueChange = {},
                            readOnly = true,
                            placeholder = { Text("Sélectionnez une date") },
                            leadingIcon = { Icon(Icons.Outlined.Event, null, tint = Color(0xFF1382B3)) },
                            trailingIcon = {
                                IconButton(onClick = { showDatePicker = true }) {
                                    Icon(Icons.Default.EditCalendar, null, tint = Color(0xFF1382B3))
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF1382B3),
                                unfocusedBorderColor = Color.LightGray
                            )
                        )
                    }
                }

                // Time Selection
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Heure de la visite", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Spacer(Modifier.height(12.dp))
                        
                        // Simple Time Picker Button
                        OutlinedButton(
                            onClick = { showTimePicker = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Outlined.Schedule, null, tint = Color(0xFF1382B3))
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = String.format("%02d:%02d", selectedHour, selectedMinute),
                                    fontSize = 16.sp
                                )
                            }
                        }
                        
                        if (showTimePicker) {
                            TimePickerDialog(
                                onDismissRequest = { showTimePicker = false },
                                confirmButton = {
                                    TextButton(onClick = { showTimePicker = false }) {
                                        Text("OK")
                                    }
                                }
                            ) {
                                val state = rememberTimePickerState(
                                    initialHour = selectedHour,
                                    initialMinute = selectedMinute
                                )
                                TimePicker(state = state)
                                LaunchedEffect(state.hour, state.minute) {
                                    selectedHour = state.hour
                                    selectedMinute = state.minute
                                }
                            }
                        }
                    }
                }

                // Contact & Notes
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Informations complémentaires", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Spacer(Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = contactPhone,
                            onValueChange = { contactPhone = it },
                            label = { Text("Numéro de contact (Optionnel)") },
                            leadingIcon = { Icon(Icons.Outlined.Phone, null, tint = Color(0xFF1382B3)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                             colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF1382B3),
                                unfocusedBorderColor = Color.LightGray
                            ),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                            )
                        )
                        
                        Spacer(Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Notes (Optionnel)") },
                            leadingIcon = { Icon(Icons.Outlined.Note, null, tint = Color(0xFF1382B3)) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            shape = RoundedCornerShape(12.dp),
                             colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF1382B3),
                                unfocusedBorderColor = Color.LightGray
                            )
                        )
                    }
                }

                Spacer(Modifier.height(80.dp)) // Space for bottom button
            }

            // Bottom Sticky Button
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                color = Color.White,
                shadowElevation = 16.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Box(modifier = Modifier.padding(20.dp)) {
                    Button(
                        onClick = {
                            if (selectedDate == null) {
                                Toast.makeText(context, "Veuillez sélectionner une date", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            viewModel.createVisite(
                                logementId = propertyId,
                                dateMillis = selectedDate!!.time,
                                hour = selectedHour,
                                minute = selectedMinute,
                                notes = notes,
                                contactPhone = contactPhone
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1382B3)
                        ),
                        enabled = !uiState.isSubmitting
                    ) {
                        if (uiState.isSubmitting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Confirmer la demande", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (showDatePicker) {
            val calendar = Calendar.getInstance()
            selectedDate?.let { calendar.time = it }
            
            CustomDatePickerDialog(
                initialYear = calendar.get(Calendar.YEAR),
                initialMonth = calendar.get(Calendar.MONTH),
                initialDay = calendar.get(Calendar.DAY_OF_MONTH),
                onDateSelected = { year, month, day ->
                    val cal = Calendar.getInstance()
                    cal.set(year, month, day)
                    selectedDate = cal.time
                    showDatePicker = false
                },
                onDismiss = { showDatePicker = false },
                minDate = System.currentTimeMillis() - 86400000 // Allow today
            )
        }

        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showSuccessDialog = false
                    navController.popBackStack()
                },
                title = { Text("Demande Envoyée 🎉") },
                text = { Text("Votre demande de visite a été envoyée au propriétaire. Vous pouvez suivre son statut dans la section 'Mes Visites'.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showSuccessDialog = false
                            navController.popBackStack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1382B3))
                    ) {
                        Text("OK")
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}
