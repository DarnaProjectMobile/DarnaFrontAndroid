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
import com.sim.darna.components.CustomDatePickerDialog
import com.sim.darna.model.Property
import com.sim.darna.model.PropertyWithBookings
import com.sim.darna.model.UserDto
import com.sim.darna.repository.PropertyRepository
import com.sim.darna.ui.theme.AppTheme
import java.text.SimpleDateFormat
import java.util.*
import org.json.JSONObject
import org.json.JSONArray
import android.util.Log

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
    var showErrorDialog by remember { mutableStateOf(false) }

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
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            "Contacter les Colocataires",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            null,
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        color = AppTheme.primary,
                        modifier = Modifier.size(48.dp),
                        strokeWidth = 4.dp
                    )
                    Text(
                        "Chargement...",
                        color = AppTheme.textSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        } else if (errorMessage != null || property == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AppTheme.card),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = errorMessage ?: "Annonce non trouvée",
                            color = Color(0xFFD32F2F),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
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
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = AppTheme.card),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = AppTheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Informations sur l'annonce",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.primary
                            )
                        }
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
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = AppTheme.card),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = AppTheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "Propriétaire",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = AppTheme.primaryLight.copy(alpha = 0.3f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        null,
                                        modifier = Modifier.size(20.dp),
                                        tint = AppTheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = prop.ownerName ?: prop.ownerUsername ?: "ID: ${prop.user}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AppTheme.textPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                // Bookings Info Card
                if (bookings.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = AppTheme.card),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.People,
                                    contentDescription = null,
                                    tint = AppTheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "Colocataires ayant réservé (${bookings.size})",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            bookings.forEachIndexed { index, booking ->
                                if (index > 0) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = AppTheme.primaryLight.copy(alpha = 0.2f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        if (booking.user != null) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.Person,
                                                    null,
                                                    modifier = Modifier.size(18.dp),
                                                    tint = AppTheme.primary
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = booking.user.username ?: "Utilisateur",
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = AppTheme.textPrimary
                                                )
                                            }
                                            if (booking.user.email != null) {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        Icons.Default.Email,
                                                        null,
                                                        modifier = Modifier.size(16.dp),
                                                        tint = AppTheme.textSecondary
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = booking.user.email,
                                                        fontSize = 14.sp,
                                                        color = AppTheme.textSecondary
                                                    )
                                                }
                                            }
                                        }
                                        if (booking.bookingStartDate != null) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.CalendarToday,
                                                    null,
                                                    modifier = Modifier.size(16.dp),
                                                    tint = AppTheme.textSecondary
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Réservé le: ${formatBookingDate(booking.bookingStartDate)}",
                                                    fontSize = 14.sp,
                                                    color = AppTheme.textSecondary
                                                )
                                            }
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
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = AppTheme.card),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = null,
                                tint = AppTheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Réserver cette annonce",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = selectedDate?.let {
                                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
                            } ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = {
                                Text(
                                    "Date de réservation",
                                    color = AppTheme.textSecondary
                                )
                            },
                            placeholder = {
                                Text(
                                    "Sélectionnez une date",
                                    color = AppTheme.textSecondary.copy(alpha = 0.6f)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = AppTheme.card,
                                unfocusedContainerColor = AppTheme.card,
                                focusedBorderColor = AppTheme.primary,
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                focusedTextColor = AppTheme.textPrimary,
                                unfocusedTextColor = AppTheme.textPrimary
                            ),
                            shape = RoundedCornerShape(14.dp),
                            leadingIcon = {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    null,
                                    tint = AppTheme.textSecondary
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { showDatePicker = true }) {
                                    Icon(
                                        Icons.Default.CalendarToday,
                                        null,
                                        tint = AppTheme.primary
                                    )
                                }
                            }
                        )


                        Spacer(modifier = Modifier.height(16.dp))

                        // Book Button
                        Button(
                            onClick = {
                                if (selectedDate == null) {
                                    errorMessage = "Veuillez sélectionner une date"
                                    showErrorDialog = true
                                    return@Button
                                }

                                // Validate date range - must match backend logic exactly:
                                // bookingDate >= startDate AND bookingDate < endDate (strictly less than endDate)
                                // We need to compare dates at UTC midnight to match backend
                                val localCal = Calendar.getInstance()
                                localCal.time = selectedDate!!
                                val selYear = localCal.get(Calendar.YEAR)
                                val selMonth = localCal.get(Calendar.MONTH)
                                val selDay = localCal.get(Calendar.DAY_OF_MONTH)

                                val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                                utcCal.set(selYear, selMonth, selDay, 0, 0, 0)
                                utcCal.set(Calendar.MILLISECOND, 0)
                                val selectedUtc = utcCal.time

                                val startDateUtc = startDate?.let {
                                    val startCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                                    val localStartCal = Calendar.getInstance()
                                    localStartCal.time = it
                                    startCal.set(
                                        localStartCal.get(Calendar.YEAR),
                                        localStartCal.get(Calendar.MONTH),
                                        localStartCal.get(Calendar.DAY_OF_MONTH),
                                        0, 0, 0
                                    )
                                    startCal.set(Calendar.MILLISECOND, 0)
                                    startCal.time
                                }

                                val endDateUtc = endDate?.let {
                                    val endCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                                    val localEndCal = Calendar.getInstance()
                                    localEndCal.time = it
                                    endCal.set(
                                        localEndCal.get(Calendar.YEAR),
                                        localEndCal.get(Calendar.MONTH),
                                        localEndCal.get(Calendar.DAY_OF_MONTH),
                                        0, 0, 0
                                    )
                                    endCal.set(Calendar.MILLISECOND, 0)
                                    endCal.time
                                }

                                if (startDateUtc != null && selectedUtc.before(startDateUtc)) {
                                    errorMessage = "La date doit être >= ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(startDate)}"
                                    showErrorDialog = true
                                    return@Button
                                }

                                // Backend uses: bookingDate >= endDate (reject), so we reject if selectedUtc >= endDateUtc
                                if (endDateUtc != null && !selectedUtc.before(endDateUtc)) {
                                    errorMessage = "La date doit être < ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(endDate)}"
                                    showErrorDialog = true
                                    return@Button
                                }

                                errorMessage = null

                                // Format date: Extract year/month/day from selected date and set to UTC midnight
                                // This ensures the date doesn't shift due to timezone conversion
                                val localCalendar = Calendar.getInstance()
                                localCalendar.time = selectedDate!!
                                val year = localCalendar.get(Calendar.YEAR)
                                val month = localCalendar.get(Calendar.MONTH)
                                val day = localCalendar.get(Calendar.DAY_OF_MONTH)

                                // Create a new date at UTC midnight with the same year/month/day
                                val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                                utcCalendar.set(year, month, day, 0, 0, 0)
                                utcCalendar.set(Calendar.MILLISECOND, 0)

                                val dateStr = dateFormat.format(utcCalendar.time)

                                // Debug logging
                                Log.d("BookPropertyScreen", "Selected date (local): ${selectedDate}")
                                Log.d("BookPropertyScreen", "Year: $year, Month: $month, Day: $day")
                                Log.d("BookPropertyScreen", "UTC date: ${utcCalendar.time}")
                                Log.d("BookPropertyScreen", "Formatted date string: $dateStr")
                                Log.d("BookPropertyScreen", "Property startDate: ${prop.startDate}")
                                Log.d("BookPropertyScreen", "Property endDate: ${prop.endDate}")

                                repository.bookProperty(propertyId, dateStr).enqueue(object : retrofit2.Callback<Property> {
                                    override fun onResponse(call: retrofit2.Call<Property>, response: retrofit2.Response<Property>) {
                                        if (response.isSuccessful) {
                                            showSuccessDialog = true
                                            errorMessage = null
                                        } else {
                                            val errorBody = response.errorBody()?.string()
                                            val extractedError = extractServerError(errorBody, response.code())
                                            errorMessage = extractedError
                                            showErrorDialog = true
                                        }
                                    }

                                    override fun onFailure(call: retrofit2.Call<Property>, t: Throwable) {
                                        errorMessage = when (t) {
                                            is java.net.UnknownHostException -> "Impossible de contacter le serveur. Vérifiez votre connexion."
                                            else -> "Erreur: ${t.message ?: "inconnue"}"
                                        }
                                        showErrorDialog = true
                                    }
                                })
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppTheme.primary,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Réserver",
                                modifier = Modifier.padding(vertical = 4.dp),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
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

        CustomDatePickerDialog(
            initialYear = calendar.get(Calendar.YEAR),
            initialMonth = calendar.get(Calendar.MONTH),
            initialDay = calendar.get(Calendar.DAY_OF_MONTH),
            onDateSelected = { year, month, day ->
                calendar.set(year, month, day)
                val selected = calendar.time

                // Validate date range (same as clone project)
                if (startDate != null && selected.before(startDate)) {
                    errorMessage = "La date doit être >= ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(startDate)}"
                    showErrorDialog = true
                } else if (endDate != null && selected.after(endDate)) {
                    errorMessage = "La date doit être <= ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(endDate)}"
                    showErrorDialog = true
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

    // Error Dialog
    if (showErrorDialog && errorMessage != null) {
        AlertDialog(
            onDismissRequest = {
                showErrorDialog = false
                errorMessage = null
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = Color(0xFFFFEBEE),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Text(
                        text = "Erreur lors de la réservation",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.textPrimary
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = errorMessage ?: "",
                        fontSize = 15.sp,
                        color = AppTheme.textSecondary,
                        lineHeight = 22.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showErrorDialog = false
                        errorMessage = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppTheme.primary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Compris",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            },
            containerColor = AppTheme.card,
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 8.dp
        )
    }

    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                navController.popBackStack()
            },
            containerColor = AppTheme.card,
            shape = RoundedCornerShape(24.dp),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        "Réservation réussie",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.textPrimary
                    )
                }
            },
            text = {
                Text(
                    "Votre réservation a été enregistrée avec succès.",
                    fontSize = 16.sp,
                    color = AppTheme.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppTheme.primary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "OK",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
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

private fun extractServerError(rawBody: String?, code: Int): String {
    if (rawBody.isNullOrBlank()) {
        return when (code) {
            400 -> "Requête invalide. Veuillez vérifier les données saisies."
            401 -> "Session expirée. Veuillez vous reconnecter."
            403 -> "Vous n'avez pas la permission d'effectuer cette action."
            404 -> "L'annonce n'a pas été trouvée."
            500 -> "Erreur serveur. Veuillez réessayer plus tard."
            else -> "Erreur de réservation (code $code)"
        }
    }

    return try {
        val json = JSONObject(rawBody)
        val message = when {
            json.has("message") -> {
                val messageNode = json.get("message")
                when (messageNode) {
                    is JSONArray -> (0 until messageNode.length())
                        .joinToString("\n") { messageNode.getString(it) }
                    is String -> messageNode
                    else -> messageNode.toString()
                }
            }
            json.has("error") -> json.getString("error")
            else -> rawBody
        }

        // Translate common backend error messages to French
        when {
            message.contains("bookingStartDate must be >= annonce.startDate and < annonce.endDate", ignoreCase = true) ->
                "La date de réservation doit être dans la période de disponibilité de l'annonce."
            message.contains("bookingStartDate must be", ignoreCase = true) ->
                "La date de réservation doit être dans la période de disponibilité de l'annonce."
            message.contains("Annonce #", ignoreCase = true) && message.contains("not found", ignoreCase = true) ->
                "L'annonce n'a pas été trouvée."
            message.contains("not found", ignoreCase = true) ->
                "L'annonce n'a pas été trouvée."
            else -> message
        }
    } catch (e: Exception) {
        // If parsing fails, try to extract a simple message from raw body
        when {
            rawBody.contains("bookingStartDate must be", ignoreCase = true) ->
                "La date de réservation doit être dans la période de disponibilité de l'annonce."
            rawBody.contains("not found", ignoreCase = true) ->
                "L'annonce n'a pas été trouvée."
            rawBody.contains("Annonce", ignoreCase = true) && rawBody.contains("#", ignoreCase = false) ->
                "L'annonce n'existe plus."
            else -> "Erreur lors de la réservation. Veuillez réessayer."
        }
    }
}