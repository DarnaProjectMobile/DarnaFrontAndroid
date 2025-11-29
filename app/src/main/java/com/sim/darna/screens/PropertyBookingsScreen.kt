package com.sim.darna.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sim.darna.model.Booking
import com.sim.darna.model.Property
import com.sim.darna.repository.PropertyRepository
import com.sim.darna.ui.theme.AppTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyBookingsScreen(
    navController: androidx.navigation.NavController,
    propertyId: String
) {
    val context = LocalContext.current
    val repository = PropertyRepository(context)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    var property by remember { mutableStateOf<Property?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    fun loadProperty() {
        isLoading = true
        errorMessage = null
        repository.getPropertyById(propertyId).enqueue(object : retrofit2.Callback<Property> {
            override fun onResponse(
                call: retrofit2.Call<Property>,
                response: retrofit2.Response<Property>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    property = response.body()
                } else {
                    errorMessage = "Impossible de charger les réservations."
                }
                isLoading = false
            }
            
            override fun onFailure(call: retrofit2.Call<Property>, t: Throwable) {
                errorMessage = "Erreur de connexion : ${t.message}"
                isLoading = false
            }
        })
    }
    
    LaunchedEffect(propertyId) {
        loadProperty()
    }
    
    fun rejectAllPendingBookings(bookings: List<Booking>) {
        if (bookings.isEmpty()) {
            isProcessing = false
            loadProperty() // Reload to refresh the UI
            return
        }
        
        // Reject bookings one by one
        var completedCount = 0
        val totalCount = bookings.size
        
        bookings.forEach { booking ->
            val bookingId = booking.id ?: return@forEach
            repository.respondToBooking(propertyId, bookingId, false)
                .enqueue(object : retrofit2.Callback<Property> {
                    override fun onResponse(
                        call: retrofit2.Call<Property>,
                        response: retrofit2.Response<Property>
                    ) {
                        completedCount++
                        if (response.isSuccessful && response.body() != null) {
                            property = response.body()
                        }
                        
                        // When all rejections are complete, reload the property
                        if (completedCount >= totalCount) {
                            isProcessing = false
                            loadProperty() // Reload to refresh the UI
                        }
                    }
                    
                    override fun onFailure(call: retrofit2.Call<Property>, t: Throwable) {
                        completedCount++
                        // Continue even if one fails
                        if (completedCount >= totalCount) {
                            isProcessing = false
                            loadProperty() // Reload to refresh the UI
                        }
                    }
                })
        }
    }
    
    fun respondToBooking(booking: Booking, accept: Boolean) {
        val bookingId = booking.id ?: return
        isProcessing = true
        repository.respondToBooking(propertyId, bookingId, accept)
            .enqueue(object : retrofit2.Callback<Property> {
                override fun onResponse(
                    call: retrofit2.Call<Property>,
                    response: retrofit2.Response<Property>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val updatedProperty = response.body()!!
                        property = updatedProperty
                        
                        // Check if property is now full after accepting
                        val isFull = (updatedProperty.nbrCollocateurActuel ?: 0) >= (updatedProperty.nbrCollocateurMax ?: 0)
                        
                        if (accept && isFull) {
                            // Automatically reject all remaining pending bookings
                            val remainingPendingBookings = updatedProperty.attendingListBookings ?: emptyList()
                            
                            if (remainingPendingBookings.isNotEmpty()) {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Demande acceptée. ${remainingPendingBookings.size} demande(s) en attente automatiquement refusée(s)."
                                    )
                                }
                                
                                // Reject all remaining pending bookings
                                rejectAllPendingBookings(remainingPendingBookings)
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Demande acceptée."
                                    )
                                }
                                isProcessing = false
                            }
                        } else {
                            isProcessing = false
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = if (accept) "Demande acceptée." else "Demande refusée."
                                )
                            }
                        }
                    } else {
                        isProcessing = false
                        scope.launch {
                            snackbarHostState.showSnackbar("Action impossible, réessayez.")
                        }
                    }
                }
                
                override fun onFailure(call: retrofit2.Call<Property>, t: Throwable) {
                    isProcessing = false
                    scope.launch {
                        snackbarHostState.showSnackbar("Erreur : ${t.message}")
                    }
                }
            })
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(shadowElevation = 6.dp) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            AppTheme.primary,
                                            AppTheme.primary.copy(alpha = 0.9f)
                                        )
                                    )
                                )
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            IconButton(
                                onClick = { navController.popBackStack() },
                                colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White),
                                modifier = Modifier.align(Alignment.CenterStart)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                            }
                            
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = property?.title ?: "Demandes de réservation",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                val pendingCount = property?.attendingListBookings?.size ?: 0
                                Text(
                                    text = if (pendingCount > 0) "$pendingCount demande${if (pendingCount > 1) "s" else ""} en attente" else "Aucune demande en attente",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 13.sp
                                )
                            }
                            
                            Icon(
                                imageVector = Icons.Default.Assignment,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .size(26.dp)
                            )
                        }
                    }
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppTheme.background)
                    .padding(padding)
            ) {
                when {
                    isLoading -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .wrapContentSize(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Chargement des demandes...")
                        }
                    }
                    errorMessage != null -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .wrapContentSize(Alignment.Center)
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color.Red,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = errorMessage ?: "Erreur",
                                color = Color.Red,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                            Button(onClick = { loadProperty() }) {
                                Text("Réessayer")
                            }
                        }
                    }
                    property == null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Annonce introuvable", color = Color.Red)
                        }
                    }
                    else -> {
                        val pendingBookings = property?.attendingListBookings ?: emptyList()
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                PropertySummaryCard(
                                    property = property!!,
                                    pendingCount = pendingBookings.size
                                )
                            }
                            
                            if (pendingBookings.isEmpty()) {
                                item {
                                    EmptyPendingState()
                                }
                            } else {
                                item {
                                    PendingHeader(pendingBookings.size)
                                }
                                
                                items(pendingBookings) { booking ->
                                    PendingBookingCard(
                                        booking = booking,
                                        onAccept = { respondToBooking(booking, true) },
                                        onReject = { respondToBooking(booking, false) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        if (isProcessing) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 32.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("Traitement en cours...")
                    }
                }
            }
        }
    }
}

@Composable
private fun PropertySummaryCard(property: Property, pendingCount: Int) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = property.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.textPrimary
            )
            
            property.location?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    fontSize = 14.sp,
                    color = AppTheme.textSecondary,
                    lineHeight = 18.sp
                )
            }
            
            Divider(color = Color(0xFFE0E0E0))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Colocataires", fontSize = 12.sp, color = AppTheme.textSecondary)
                    Text(
                        text = "${property.nbrCollocateurActuel ?: 0}/${property.nbrCollocateurMax ?: 0}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E88E5)
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text("Loyer", fontSize = 12.sp, color = AppTheme.textSecondary)
                    Text(
                        text = "${property.price.toInt()} DT/mois",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.textPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun PendingHeader(count: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            Icons.Default.Schedule,
            contentDescription = null,
            tint = Color(0xFFFF9800)
        )
        Text(
            text = "Demandes en attente ($count)",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = AppTheme.textPrimary
        )
    }
}

@Composable
private fun EmptyPendingState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val compositionResult = rememberLottieComposition(
                LottieCompositionSpec.Asset("empty.json")
            )
            LottieAnimation(
                composition = compositionResult.value,
                modifier = Modifier.size(120.dp),
                iterations = Int.MAX_VALUE
            )
            Text(
                text = "Aucune demande en attente",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Vous serez notifié dès qu'un colocataire enverra une nouvelle demande.",
                fontSize = 14.sp,
                color = AppTheme.textSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PendingBookingCard(
    booking: Booking,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    var showAcceptDialog by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }
    val user = booking.user
    val bookingDate = formatIsoDate(booking.bookingStartDate)
    val birthDate = user?.dateDeNaissance ?: user?.dateOfBirth
    val context = LocalContext.current
    
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.HourglassTop,
                        contentDescription = null,
                        tint = Color(0xFFFFA000),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Demande en attente",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFFA000)
                    )
                }
                
                Text(
                    text = bookingDate,
                    fontSize = 12.sp,
                    color = AppTheme.textSecondary
                )
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(AppTheme.primaryLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = AppTheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                if (user != null) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = user.username ?: "Utilisateur",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.textPrimary
                        )
                        user.email?.let {
                            InfoRow(icon = Icons.Default.Email, label = it)
                        }
                        user.phone?.let {
                            InfoRow(icon = Icons.Default.Phone, label = it)
                        }
                    }
                } else {
                    Text(
                        text = "Informations utilisateur indisponibles",
                        color = AppTheme.textSecondary,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Divider()
            
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = AppTheme.primary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text("Souhaite emménager le", fontSize = 12.sp, color = AppTheme.textSecondary)
                        Text(bookingDate, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = AppTheme.textSecondary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = buildString {
                            user?.gender?.let { append(it.replaceFirstChar { c -> c.uppercase() }) }
                            birthDate?.let {
                                if (isNotEmpty()) append(" • ")
                                append("Né(e) le ${formatIsoDate(it)}")
                            }
                        }.ifEmpty { "Informations complémentaires non fournies" },
                        fontSize = 13.sp,
                        color = AppTheme.textSecondary
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        user?.phone?.let { tryOpenDialer(context, it) } ?: showContactToast(context)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE3F2FD))
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF1E88E5))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Appeler", color = Color(0xFF1E88E5))
                }
                
                Button(
                    onClick = {
                        user?.email?.let { tryOpenEmail(context, it) } ?: showContactToast(context)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
                ) {
                    Icon(Icons.Default.Email, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Email", color = Color.White)
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { showRejectDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp, brush = SolidColor(Color(0xFFD32F2F)))
                ) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Refuser")
                }
                
                Button(
                    onClick = { showAcceptDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Accepter")
                }
            }
        }
    }
    
    if (showAcceptDialog) {
        AlertDialog(
            onDismissRequest = { showAcceptDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showAcceptDialog = false
                    onAccept()
                }) {
                    Text("Confirmer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAcceptDialog = false }) {
                    Text("Annuler")
                }
            },
            title = { Text("Accepter la demande ?") },
            text = { Text("Cette personne sera ajoutée à vos colocataires confirmés.") }
        )
    }
    
    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showRejectDialog = false
                    onReject()
                }) {
                    Text("Refuser", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("Annuler")
                }
            },
            title = { Text("Refuser la demande ?") },
            text = { Text("Cette demande sera supprimée définitivement.") }
        )
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icon, contentDescription = null, tint = AppTheme.textSecondary, modifier = Modifier.size(16.dp))
        Text(label, fontSize = 13.sp, color = AppTheme.textPrimary)
    }
}

private fun formatIsoDate(dateString: String?): String {
    if (dateString.isNullOrBlank()) return "—"
    return try {
        val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        input.timeZone = TimeZone.getTimeZone("UTC")
        val date = input.parse(dateString)
        val output = SimpleDateFormat("dd MMM yyyy", Locale("fr", "FR"))
        date?.let { output.format(it) } ?: dateString
    } catch (_: Exception) {
        dateString
    }
}

private fun tryOpenDialer(context: Context, phone: String) {
    val sanitized = phone.trim().filterIndexed { index, c ->
        c.isDigit() || (c == '+' && index == 0)
    }.ifEmpty { phone.trim() }
    
    if (sanitized.isEmpty()) {
        showContactToast(context)
        return
    }
    
    try {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$sanitized"))
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "Impossible d'ouvrir le téléphone", Toast.LENGTH_SHORT).show()
    }
}

private fun tryOpenEmail(context: Context, email: String) {
    val trimmed = email.trim()
    if (trimmed.isEmpty()) {
        showContactToast(context)
        return
    }
    try {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$trimmed"))
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "Impossible d'ouvrir l'email", Toast.LENGTH_SHORT).show()
    }
}

private fun showContactToast(context: Context) {
    Toast.makeText(context, "Contact indisponible", Toast.LENGTH_SHORT).show()
}

