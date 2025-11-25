package com.sim.darna.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.sim.darna.navigation.Routes
import com.sim.darna.viewmodel.ReviewViewModel
import com.sim.darna.model.Review as DatabaseReview

//------------------------------------------------------
// HELPER FUNCTIONS
//------------------------------------------------------

// Format date helper
fun formatDate(createdAt: String?): String {
    // Simple date formatting - you can enhance this
    return createdAt ?: "Date inconnue"
}

//------------------------------------------------------
// MAIN SCREEN
//------------------------------------------------------
@Composable
fun PropertyDetailScreen(navController: NavController, propertyId: String? = null) {

    val context = LocalContext.current
    val repository = com.sim.darna.repository.PropertyRepository(context)
    
    var property by remember { mutableStateOf<com.sim.darna.model.Property?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // ViewModel for reviews
    val reviewViewModel: ReviewViewModel = viewModel()
    val allReviews by reviewViewModel.reviews.collectAsState()
    
    // Load property from API
    LaunchedEffect(propertyId) {
        if (propertyId != null) {
            repository.getPropertyById(propertyId).enqueue(object : retrofit2.Callback<com.sim.darna.model.Property> {
                override fun onResponse(call: retrofit2.Call<com.sim.darna.model.Property>, response: retrofit2.Response<com.sim.darna.model.Property>) {
                    if (response.isSuccessful && response.body() != null) {
                        property = response.body()
                        isLoading = false
                    } else {
                        error = "Impossible de charger l'annonce"
                        isLoading = false
                    }
                }
                
                override fun onFailure(call: retrofit2.Call<com.sim.darna.model.Property>, t: Throwable) {
                    error = "Erreur: ${t.message}"
                    isLoading = false
                }
            })
        } else {
            isLoading = false
        }
    }
    
    // Initialize ViewModel and load reviews
    LaunchedEffect(Unit) {
        reviewViewModel.init(context)
        reviewViewModel.loadReviews()
    }
    
    // Take only first 3 reviews
    val recentReviews = allReviews.take(3)
    
    // Calculate average rating and total reviews
    val averageRating = if (allReviews.isNotEmpty()) {
        allReviews.map { it.rating }.average().toFloat()
    } else {
        0f
    }
    val totalReviews = allReviews.size
    
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    
    if (error != null || property == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = error ?: "Annonce non trouvée", color = Color.Red)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navController.popBackStack() }) {
                    Text("Retour")
                }
            }
        }
        return
    }
    
    val prop = property!!

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {

        //------------------------------------------------------
        // HEADER - Swipeable Images
        //------------------------------------------------------
        item {
            val images = prop.images ?: listOfNotNull(prop.image)
            val pagerState = rememberPagerState(initialPage = 0) { images.size }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                if (images.isNotEmpty()) {
                    // Use HorizontalPager for swipeable images
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        com.sim.darna.components.PropertyImageView(
                            imageString = images[page],
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    
                    // Page indicator
                    if (images.size > 1) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            repeat(images.size) { index ->
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (pagerState.currentPage == index) Color.White else Color.White.copy(alpha = 0.5f)
                                        )
                                )
                            }
                        }
                    }
                } else {
                    com.sim.darna.components.PropertyImageView(
                        imageString = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.TopStart)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.9f)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.padding(8.dp),
                            tint = Color(0xFF1A1A1A)
                        )
                    }
                }
            }
        }

        //------------------------------------------------------
        // TITLE + LOCATION
        //------------------------------------------------------
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(20.dp)
            ) {
                Text(
                    text = prop.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color(0xFF757575)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = prop.location ?: "Localisation non spécifiée",
                        fontSize = 16.sp,
                        color = Color(0xFF757575)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = prop.type ?: "Type non spécifié",
                    fontSize = 15.sp,
                    color = Color(0xFF757575)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.People,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF757575)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${prop.nbrCollocateurActuel ?: 0}/${prop.nbrCollocateurMax ?: 0} colocataires",
                        fontSize = 14.sp,
                        color = Color(0xFF757575)
                    )
                }
            }
        }

        //------------------------------------------------------
        // INFO CARDS
        //------------------------------------------------------
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PropertyInfoCard(
                        icon = Icons.Default.Person,
                        label = "Colocataires",
                        value = "${prop.nbrCollocateurActuel ?: 0}/${prop.nbrCollocateurMax ?: 0}",
                        modifier = Modifier.weight(1f)
                    )
                    PropertyInfoCard(
                        icon = Icons.Default.AttachMoney,
                        label = "Loyer",
                        value = "${prop.price.toInt()} DT",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        //------------------------------------------------------
        // DESCRIPTION
        //------------------------------------------------------
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Description",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = prop.description ?: "Aucune description disponible.",
                    fontSize = 15.sp,
                    color = Color(0xFF757575),
                    lineHeight = 22.sp
                )
            }
        }

        //------------------------------------------------------
        // AMENITIES
        //------------------------------------------------------
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = "Équipements",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AmenityChip("WiFi", Icons.Default.Wifi)
                    AmenityChip("Cuisine", Icons.Default.Restaurant)
                    AmenityChip("Parking", Icons.Default.LocalParking)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AmenityChip("Balcon", Icons.Default.Balcony)
                    AmenityChip("Machine à laver", Icons.Default.LocalLaundryService)
                }
            }
        }

        //------------------------------------------------------
        // REVIEWS HEADER
        //------------------------------------------------------
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Avis",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Color(0xFFFFC107)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$averageRating",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1A1A)
                            )
                            Text(
                                text = " ($totalReviews avis)",
                                fontSize = 14.sp,
                                color = Color(0xFF757575)
                            )
                        }
                    }

                    Button(
                        onClick = { navController.navigate(Routes.Reviews) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0066FF)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Voir tous les avis",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                }

                Spacer(modifier = Modifier.height(16.dp))

                RatingBreakdown(allReviews)
            }
        }

        //------------------------------------------------------
        // RECENT REVIEWS
        //------------------------------------------------------
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = "Avis récents",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A1A)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        items(recentReviews) { review ->
            ReviewCard(review)
        }

        item { 
            Spacer(modifier = Modifier.height(16.dp))
            // Contact Button
            Button(
                onClick = {
                    if (propertyId != null) {
                        navController.navigate(Routes.BookProperty.replace("{propertyId}", propertyId))
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9C27B0)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Contacter les Colocataires",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

//------------------------------------------------------
// SMALL COMPOSABLES
//------------------------------------------------------
@Composable
fun PropertyInfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = Color(0xFF0066FF), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(label, fontSize = 12.sp, color = Color(0xFF757575))
        }
    }
}

@Composable
fun AmenityChip(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFF5F5F5)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = Color(0xFF0066FF), modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text, fontSize = 13.sp)
        }
    }
}

@Composable
fun RatingBreakdown(reviews: List<DatabaseReview>) {
    // Calculate rating distribution (how many reviews for each star level)
    val ratingCounts = (1..5).map { star ->
        reviews.count { it.rating == star }
    }.reversed() // Reverse to show 5 stars first
    
    val totalReviews = reviews.size
    
    Column {
        for ((index, star) in (5 downTo 1).withIndex()) {
            val count = ratingCounts[index]
            val percentage = if (totalReviews > 0) count.toFloat() / totalReviews else 0f
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("$star", modifier = Modifier.width(20.dp), color = Color(0xFF757575))
                Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(8.dp))

                LinearProgressIndicator(
                    progress = percentage,
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFFFFC107),
                    trackColor = Color(0xFFE0E0E0)
                )

                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${(percentage * 100).toInt()}%",
                    fontSize = 12.sp,
                    color = Color(0xFF757575),
                    modifier = Modifier.width(35.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun ReviewCard(review: DatabaseReview) {
    val username = review.user?.username ?: "Anonymous"
    val userInitial = username.firstOrNull()?.uppercase() ?: "?"
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = Color(0xFF0066FF).copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                userInitial,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0066FF)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(username, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Text("Récemment", fontSize = 12.sp, color = Color(0xFF9E9E9E))
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(5) { index ->
                        Icon(
                            if (index < review.rating) Icons.Default.Star
                            else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = review.comment,
                fontSize = 14.sp,
                color = Color(0xFF757575),
                lineHeight = 20.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
