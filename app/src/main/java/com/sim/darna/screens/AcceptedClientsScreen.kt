package com.sim.darna.screens

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.sim.darna.components.EmptyStateLottie
import com.sim.darna.model.Booking
import com.sim.darna.model.Property
import com.sim.darna.repository.PropertyRepository
import com.sim.darna.ui.theme.AppTheme
import java.text.SimpleDateFormat
import java.util.*
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcceptedClientsScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
    val currentUserId = prefs.getString("user_id", null)
    val repository = PropertyRepository(context)
    
    var properties by remember { mutableStateOf<List<Property>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    fun loadProperties() {
        isLoading = true
        errorMessage = null
        repository.getAllProperties().enqueue(object : retrofit2.Callback<List<Property>> {
            override fun onResponse(
                call: retrofit2.Call<List<Property>>,
                response: retrofit2.Response<List<Property>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val allProperties = response.body()!!
                    properties = allProperties.filter { it.user == currentUserId }
                } else {
                    errorMessage = "Impossible de charger vos annonces."
                }
                isLoading = false
            }
            
            override fun onFailure(call: retrofit2.Call<List<Property>>, t: Throwable) {
                errorMessage = "Erreur: ${t.message}"
                isLoading = false
            }
        })
    }
    
    LaunchedEffect(Unit) { loadProperties() }
    
    Scaffold(
        topBar = {
            Column {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(shadowElevation = 6.dp) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(AppTheme.primary, AppTheme.primary.copy(alpha = 0.9f))
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
                                text = "Clients acceptés",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (properties.isEmpty()) "Aucune annonce publiée" else "Sélectionnez une annonce",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 13.sp
                            )
                        }
                        
                        Icon(
                            Icons.Default.VerifiedUser,
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
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Chargement de vos annonces…")
                    }
                }
                errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(48.dp))
                        Text(
                            text = errorMessage ?: "Erreur",
                            color = Color.Red,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                        Button(onClick = { loadProperties() }) {
                            Text("Réessayer")
                        }
                    }
                }
                properties.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyStateLottie(
                            title = "Aucune annonce",
                            subtitle = "Vous n'avez pas encore créé d'annonces.",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(properties) { property ->
                            PropertySelectionCard(
                                property = property,
                                onClick = {
                                    navController.navigate("confirmed_clients/${property.id}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PropertySelectionCard(property: Property, onClick: () -> Unit) {
    val imageUrl = property.getFirstImage()
    val baseUrl = "http://192.168.100.3:3000/"
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = Color.Black.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Square thumbnail image
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF5F5F5))
            ) {
                if (imageUrl != null && imageUrl.isNotEmpty()) {
                    if (imageUrl.startsWith("data:image")) {
                        // Base64 image
                        val base64String = imageUrl.substringAfter(",")
                        val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Property image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Property",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp),
                                tint = AppTheme.textSecondary
                            )
                        }
                    } else {
                        // URL image - using Coil
                        val fullUrl = if (imageUrl.startsWith("http")) imageUrl else "$baseUrl$imageUrl"
                        AsyncImage(
                            model = fullUrl,
                            contentDescription = "Property image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Property",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        tint = AppTheme.textSecondary
                    )
                }
            }
            
            // Middle: Title, Location, Capacity
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Title
                Text(
                    text = property.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.textPrimary,
                    maxLines = 1
                )
                
                // Location with Send icon
                if (!property.location.isNullOrEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = AppTheme.textSecondary
                        )
                        Text(
                            text = property.location ?: "",
                            fontSize = 12.sp,
                            color = AppTheme.textSecondary,
                            maxLines = 1
                        )
                    }
                }
                
                // Capacity with People icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.People,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = AppTheme.textSecondary
                    )
                    Text(
                        text = "${property.nbrCollocateurActuel ?: 0}/${property.nbrCollocateurMax ?: 0}",
                        fontSize = 12.sp,
                        color = AppTheme.textSecondary
                    )
                }
            }
            
            // Right: Price and Arrow
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${property.price.toInt()} DT",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.primary
                )
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = AppTheme.textSecondary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmedClientsScreen(navController: NavController, propertyId: String) {
    val context = LocalContext.current
    val repository = PropertyRepository(context)
    
    var property by remember { mutableStateOf<Property?>(null) }
    var isLoading by remember { mutableStateOf(true) }
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
                    errorMessage = "Impossible de charger cette annonce."
                }
                isLoading = false
            }
            
            override fun onFailure(call: retrofit2.Call<Property>, t: Throwable) {
                errorMessage = "Erreur: ${t.message}"
                isLoading = false
            }
        })
    }
    
    LaunchedEffect(propertyId) { loadProperty() }
    
    Scaffold(
        topBar = {
            Column {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(shadowElevation = 6.dp) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(AppTheme.primary, AppTheme.primary.copy(alpha = 0.9f))
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
                                text = property?.title ?: "Clients confirmés",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            val confirmedCount = property?.bookings?.size ?: 0
                            Text(
                                text = if (confirmedCount == 0) "Aucun client confirmé" else "$confirmedCount client${if (confirmedCount > 1) "s" else ""} confirmés",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 13.sp
                            )
                        }
                        
                        Icon(
                            Icons.Default.People,
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
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Chargement…")
                    }
                }
                errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(48.dp))
                        Text(
                            text = errorMessage ?: "Erreur",
                            color = Color.Red,
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
                    val confirmedBookings = property?.bookings ?: emptyList()
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            ConfirmedPropertyHeader(property!!)
                        }
                        
                        if (confirmedBookings.isEmpty()) {
                            item {
                                EmptyConfirmedState()
                            }
                        } else {
                            items(confirmedBookings) { booking ->
                                ConfirmedBookingCard(booking = booking)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfirmedPropertyHeader(property: Property) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(property.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            property.location?.takeIf { it.isNotBlank() }?.let {
                Text(it, fontSize = 13.sp, color = AppTheme.textSecondary)
            }
            
            Divider()
            
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("Colocataires", fontSize = 12.sp, color = AppTheme.textSecondary)
                    Text(
                        "${property.nbrCollocateurActuel ?: 0}/${property.nbrCollocateurMax ?: 0}",
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.primary
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text("Loyer", fontSize = 12.sp, color = AppTheme.textSecondary)
                    Text("${property.price.toInt()} DT/mois", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun EmptyConfirmedState() {
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
            Text("Aucun client confirmé", fontWeight = FontWeight.Bold)
            Text(
                text = "Dès que vous acceptez une demande, le client apparaîtra ici.",
                color = AppTheme.textSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ConfirmedBookingCard(booking: Booking) {
    val context = LocalContext.current
    val user = booking.user
    val moveInDate = formatDateShort(booking.bookingStartDate)
    
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            text = "Confirmé",
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32))
                    },
                    colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFFC8E6C9)),
                    border = null,
                    enabled = false
                )
                
                Text(
                    text = "Depuis le $moveInDate",
                    fontSize = 12.sp,
                    color = AppTheme.textSecondary
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(AppTheme.primaryLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = AppTheme.primary)
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user?.username ?: "Utilisateur",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    user?.email?.let {
                        InfoLine(icon = Icons.Default.Email, value = it)
                    }
                    user?.phone?.let {
                        InfoLine(icon = Icons.Default.Phone, value = it)
                    }
                }
            }
            
            user?.gender?.let {
                InfoLine(icon = Icons.Default.Wc, value = it.replaceFirstChar { c -> c.uppercase() })
            }
            user?.dateDeNaissance?.let {
                InfoLine(icon = Icons.Default.Cake, value = formatDateShort(it))
            } ?: user?.dateOfBirth?.let {
                InfoLine(icon = Icons.Default.Cake, value = formatDateShort(it))
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        user?.phone?.let { openDialer(context, it) } ?: showMissingContactToast(context)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AppTheme.primaryLight)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = AppTheme.primary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Appeler", color = AppTheme.primary)
                }
                
                Button(
                    onClick = {
                        user?.email?.let { openEmail(context, it) } ?: showMissingContactToast(context)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AppTheme.primary)
                ) {
                    Icon(Icons.Default.Email, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Email", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun InfoLine(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icon, contentDescription = null, tint = AppTheme.textSecondary, modifier = Modifier.size(16.dp))
        Text(value, fontSize = 13.sp, color = AppTheme.textPrimary)
    }
}

private fun formatDateShort(value: String?): String {
    if (value.isNullOrBlank()) return "—"
    return try {
        val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        input.timeZone = TimeZone.getTimeZone("UTC")
        val date = input.parse(value)
        val output = SimpleDateFormat("dd MMM yyyy", Locale("fr", "FR"))
        date?.let { output.format(it) } ?: value
    } catch (_: Exception) {
        value
    }
}

private fun openDialer(context: Context, phone: String) {
    val trimmed = phone.trim()
    if (trimmed.isEmpty()) {
        showMissingContactToast(context)
        return
    }
    
    val sanitized = buildString {
        trimmed.forEachIndexed { index, c ->
            when {
                c.isDigit() -> append(c)
                c == '+' && index == 0 -> append(c)
            }
        }
    }.ifEmpty { trimmed }
    
    try {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$sanitized"))
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "Impossible d'ouvrir le téléphone", Toast.LENGTH_SHORT).show()
    }
}

private fun openEmail(context: Context, email: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email")))
    } catch (_: Exception) {
        Toast.makeText(context, "Impossible d'ouvrir l'email", Toast.LENGTH_SHORT).show()
    }
}

private fun showMissingContactToast(context: Context) {
    Toast.makeText(context, "Contact indisponible", Toast.LENGTH_SHORT).show()
}

