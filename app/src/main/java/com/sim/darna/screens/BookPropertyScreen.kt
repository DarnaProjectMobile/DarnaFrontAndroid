package com.sim.darna.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sim.darna.model.Property
import com.sim.darna.model.PropertyWithBookings
import com.sim.darna.model.UserDto
import com.sim.darna.repository.PropertyRepository
import com.sim.darna.ui.theme.AppTheme
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookPropertyScreen(navController: androidx.navigation.NavController, propertyId: String) {
    val context = LocalContext.current
    val repository = PropertyRepository(context)
    
    var property by remember { mutableStateOf<Property?>(null) }
    var propertyWithBookings by remember { mutableStateOf<PropertyWithBookings?>(null) }
    var ownerInfo by remember { mutableStateOf<UserDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedDate by remember { mutableStateOf<Date?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    dateFormat.timeZone = TimeZone.getTimeZone("UTC")
    
    LaunchedEffect(propertyId) {
        // Load property details
        repository.getPropertyById(propertyId).enqueue(object : retrofit2.Callback<Property> {
            override fun onResponse(call: retrofit2.Call<Property>, response: retrofit2.Response<Property>) {
                if (response.isSuccessful && response.body() != null) {
                    property = response.body()
                    
                    // Load property with bookings to get user details
                    repository.getPropertyWithBookings(propertyId).enqueue(object : retrofit2.Callback<PropertyWithBookings> {
                        override fun onResponse(call: retrofit2.Call<PropertyWithBookings>, response: retrofit2.Response<PropertyWithBookings>) {
                            if (response.isSuccessful && response.body() != null) {
                                propertyWithBookings = response.body()
                            }
                            isLoading = false
                        }
                        
                        override fun onFailure(call: retrofit2.Call<PropertyWithBookings>, t: Throwable) {
                            isLoading = false
                        }
                    })
                } else {
                    errorMessage = "Impossible de charger l'annonce"
                    isLoading = false
                }
            }
            
            override fun onFailure(call: retrofit2.Call<Property>, t: Throwable) {
                errorMessage = "Erreur: ${t.message}"
                isLoading = false
            }
        })
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contacter les Colocataires") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (errorMessage != null || property == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(text = errorMessage ?: "Annonce non trouvée", color = Color.Red)
            }
        } else {
            val prop = property!!
            val bookings = propertyWithBookings?.bookings ?: prop.bookings ?: emptyList()
            
            // Parse dates for validation
            val startDate = prop.startDate?.let {
                try { dateFormat.parse(it) } catch (e: Exception) { null }
            }
            val endDate = prop.endDate?.let {
                try { dateFormat.parse(it) } catch (e: Exception) { null }
            }
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Property Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AppTheme.card)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Informations sur l'annonce",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = prop.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (prop.location != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(16.dp), tint = AppTheme.textSecondary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = prop.location, fontSize = 14.sp, color = AppTheme.textSecondary)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AttachMoney, null, modifier = Modifier.size(16.dp), tint = AppTheme.primary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${prop.price.toInt()} DT/mois",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppTheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.People, null, modifier = Modifier.size(16.dp), tint = AppTheme.textSecondary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${prop.nbrCollocateurActuel ?: 0}/${prop.nbrCollocateurMax ?: 0} colocataires",
                                fontSize = 14.sp,
                                color = AppTheme.textSecondary
                            )
                        }
                        if (startDate != null && endDate != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(16.dp), tint = AppTheme.textSecondary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Disponible du ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(startDate)} au ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(endDate)}",
                                    fontSize = 14.sp,
                                    color = AppTheme.textSecondary
                                )
                            }
                        }
                    }
                }
                
                // Owner Info Card
                if (prop.user != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = AppTheme.card)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Propriétaire",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, null, modifier = Modifier.size(20.dp), tint = AppTheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = prop.ownerName ?: prop.ownerUsername ?: "ID: ${prop.user}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
                
                // Bookings Info Card
                if (bookings.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = AppTheme.card)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Colocataires ayant réservé (${bookings.size})",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            bookings.forEachIndexed { index, booking ->
                                if (index > 0) {
                                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                                }
                                Column {
                                    if (booking.user != null) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Person, null, modifier = Modifier.size(16.dp), tint = AppTheme.textSecondary)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = booking.user.username ?: "Utilisateur",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        if (booking.user.email != null) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Email, null, modifier = Modifier.size(14.dp), tint = AppTheme.textSecondary)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = booking.user.email,
                                                    fontSize = 12.sp,
                                                    color = AppTheme.textSecondary
                                                )
                                            }
                                        }
                                    }
                                    if (booking.bookingStartDate != null) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(14.dp), tint = AppTheme.textSecondary)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Réservé le: ${formatBookingDate(booking.bookingStartDate)}",
                                                fontSize = 12.sp,
                                                color = AppTheme.textSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Date Picker for Booking
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AppTheme.card)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Réserver cette annonce",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = selectedDate?.let { 
                                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it) 
                            } ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Date de réservation") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = { showDatePicker = true }) {
                                    Icon(Icons.Default.CalendarToday, null)
                                }
                            }
                        )
                        
                        errorMessage?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = it, color = Color.Red, fontSize = 12.sp)
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Book Button
                        Button(
                            onClick = {
                                if (selectedDate == null) {
                                    errorMessage = "Veuillez sélectionner une date"
                                    return@Button
                                }
                                
                                // Validate date range
                                if (startDate != null && selectedDate!!.before(startDate)) {
                                    errorMessage = "La date doit être >= ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(startDate)}"
                                    return@Button
                                }
                                
                                if (endDate != null && selectedDate!!.after(endDate)) {
                                    errorMessage = "La date doit être <= ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(endDate)}"
                                    return@Button
                                }
                                
                                errorMessage = null
                                val dateStr = dateFormat.format(selectedDate!!)
                                
                                repository.bookProperty(propertyId, dateStr).enqueue(object : retrofit2.Callback<Property> {
                                    override fun onResponse(call: retrofit2.Call<Property>, response: retrofit2.Response<Property>) {
                                        if (response.isSuccessful) {
                                            showSuccessDialog = true
                                        } else {
                                            errorMessage = "Erreur lors de la réservation"
                                        }
                                    }
                                    
                                    override fun onFailure(call: retrofit2.Call<Property>, t: Throwable) {
                                        errorMessage = "Erreur: ${t.message}"
                                    }
                                })
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AppTheme.primary)
                        ) {
                            Text("Réserver", modifier = Modifier.padding(vertical = 12.dp), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
    
    if (showDatePicker && property != null) {
        val prop = property!!
        val startDate = prop.startDate?.let {
            try { dateFormat.parse(it) } catch (e: Exception) { null }
        }
        val endDate = prop.endDate?.let {
            try { dateFormat.parse(it) } catch (e: Exception) { null }
        }
        
        val calendar = Calendar.getInstance()
        calendar.time = selectedDate ?: startDate ?: Date()
        
        // Calculate min and max dates
        val minDateMillis = startDate?.time
        val maxDateMillis = endDate?.time
        
        DatePickerDialog(
            initialYear = calendar.get(Calendar.YEAR),
            initialMonth = calendar.get(Calendar.MONTH),
            initialDay = calendar.get(Calendar.DAY_OF_MONTH),
            onDateSelected = { year, month, day ->
                calendar.set(year, month, day)
                val selected = calendar.time
                
                // Validate date range
                if (startDate != null && selected.before(startDate)) {
                    errorMessage = "La date doit être >= ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(startDate)}"
                } else if (endDate != null && selected.after(endDate)) {
                    errorMessage = "La date doit être <= ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(endDate)}"
                } else {
                    selectedDate = selected
                    errorMessage = null
                }
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
            minDate = minDateMillis,
            maxDate = maxDateMillis
        )
    }
    
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { 
                showSuccessDialog = false
                navController.popBackStack()
            },
            title = { Text("Réservation réussie") },
            text = { Text("Votre réservation a été enregistrée avec succès.") },
            confirmButton = {
                TextButton(onClick = {
                    showSuccessDialog = false
                    navController.popBackStack()
                }) {
                    Text("OK")
                }
            }
        )
    }
}

private fun formatBookingDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}
