package com.sim.darna.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.foundation.Image
import com.sim.darna.auth.RetrofitClient
import com.sim.darna.components.EmptyStateLottie
import com.sim.darna.model.Property
import com.sim.darna.navigation.Routes
import com.sim.darna.repository.PropertyRepository
import com.sim.darna.ui.theme.AppTheme
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationsScreen(navController: androidx.navigation.NavController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("APP_PREFS", android.content.Context.MODE_PRIVATE)
    val currentUserId = prefs.getString("user_id", null)
    
    val repository = PropertyRepository(context)
    
    var properties by remember { mutableStateOf<List<Property>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    fun loadProperties() {
        isLoading = true
        errorMessage = null
        
        repository.getAllProperties().enqueue(object : Callback<List<Property>> {
            override fun onResponse(
                call: retrofit2.Call<List<Property>>,
                response: Response<List<Property>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val allProperties = response.body()!!
                    // Filter properties owned by the current user (like iOS fetchUserProperties)
                    properties = allProperties.filter { property ->
                        property.user == currentUserId
                    }
                } else {
                    errorMessage = "Impossible de charger vos annonces."
                }
                isLoading = false
            }
            
            override fun onFailure(call: retrofit2.Call<List<Property>>, t: Throwable) {
                errorMessage = "Impossible de charger vos annonces."
                isLoading = false
            }
        })
    }
    
    LaunchedEffect(Unit) {
        loadProperties()
    }
    
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
                                text = "Demandes en attente",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (properties.isNotEmpty()) "Gérer les demandes." else "Aucune annonce publiée",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 13.sp
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
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
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Chargement...")
                        }
                    }
                }
                errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = Color.Red
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = errorMessage ?: "Erreur",
                                color = Color.Red,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { loadProperties() }) {
                                Text("Réessayer")
                            }
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
                            PropertyReservationCard(
                                property = property,
                                onClick = {
                                    // Navigate to property bookings view to show all users who booked
                                    navController.navigate("property_bookings/${property.id}")
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
fun PropertyReservationCard(
    property: Property,
    onClick: () -> Unit
) {
    val pendingCount = property.attendingListBookings?.size ?: 0
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
                        val imageBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
                        val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        
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
            
            // Right: Price, Badge, and Arrow
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "${property.price.toInt()} DT/mois",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.primary
                )
                
                // Badge showing pending requests
                if (pendingCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFFA726), // Orange color
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = "$pendingCount en attente",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                
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
