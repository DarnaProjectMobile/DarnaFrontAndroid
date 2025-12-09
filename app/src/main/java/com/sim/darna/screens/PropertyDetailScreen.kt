package com.sim.darna.screens

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.sim.darna.navigation.Routes
import com.sim.darna.viewmodel.ReviewViewModel
import com.sim.darna.model.Review as DatabaseReview

//------------------------------------------------------
// COLOR SCHEME
//------------------------------------------------------
private val PrimaryColor = Color(0xFFFF4B6E)
private val SecondaryColor = Color(0xFF4C6FFF)
private val AccentColor = Color(0xFFFFC857)
private val BackgroundColor = Color(0xFFF7F7F7)
private val SurfaceColor = Color.White
private val TextPrimary = Color(0xFF1A1A1A)
private val TextSecondary = Color(0xFF6B6B6B)
private val TextTertiary = Color(0xFF9E9E9E)

//------------------------------------------------------
// HELPER FUNCTIONS
//------------------------------------------------------
fun formatDate(createdAt: String?): String {
    return createdAt ?: "Date inconnue"
}

//------------------------------------------------------
// MAIN SCREEN
//------------------------------------------------------
@Composable
fun PropertyDetailScreen(navController: NavController, propertyId: String? = null) {

    val context = LocalContext.current
    val repository = com.sim.darna.repository.PropertyRepository(context)
    val prefs = context.getSharedPreferences("APP_PREFS", android.content.Context.MODE_PRIVATE)
    val currentUserId = prefs.getString("user_id", null)

    var property by remember { mutableStateOf<com.sim.darna.model.Property?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val reviewViewModel: ReviewViewModel = viewModel()
    val allReviews by reviewViewModel.reviews.collectAsState()

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

    LaunchedEffect(propertyId) {
        if (propertyId != null) {
            reviewViewModel.init(context)
            reviewViewModel.loadReviewsForProperty(propertyId)
        }
    }

    // Direct usage of reviews from VM, assumed to be for this property only
    val propertyReviews = allReviews
    val recentReviews = propertyReviews.take(3)

    val averageRating = if (propertyReviews.isNotEmpty()) {
        propertyReviews.map { it.rating }.average().toFloat()
    } else {
        0f
    }
    val totalReviews = propertyReviews.size

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryColor)
        }
        return
    }

    if (error != null || property == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = PrimaryColor
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = error ?: "Annonce non trouvée",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { navController.popBackStack() },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Retour", modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                }
            }
        }
        return
    }

    val prop = property!!

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundColor)
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
                        .height(320.dp)
                ) {
                    if (images.isNotEmpty()) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            com.sim.darna.components.PropertyImageView(
                                imageString = images[page],
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        if (images.size > 1) {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 20.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.Black.copy(alpha = 0.4f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                repeat(images.size) { index ->
                                    Box(
                                        modifier = Modifier
                                            .size(if (pagerState.currentPage == index) 24.dp else 6.dp, 6.dp)
                                            .clip(RoundedCornerShape(3.dp))
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
                            color = Color.White,
                            shadowElevation = 4.dp
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                modifier = Modifier.padding(10.dp),
                                tint = TextPrimary
                            )
                        }
                    }
                }
            }

            //------------------------------------------------------
            // TITLE + LOCATION
            //------------------------------------------------------
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = SurfaceColor,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    shadowElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = prop.title,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            lineHeight = 32.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(BackgroundColor)
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = PrimaryColor
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = prop.location ?: "Localisation non spécifiée",
                                fontSize = 15.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SecondaryColor.copy(alpha = 0.1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Home,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = SecondaryColor
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = prop.type ?: "Type",
                                        fontSize = 14.sp,
                                        color = SecondaryColor,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = PrimaryColor.copy(alpha = 0.1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.People,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = PrimaryColor
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${prop.nbrCollocateurActuel ?: 0}/${prop.nbrCollocateurMax ?: 0}",
                                        fontSize = 14.sp,
                                        color = PrimaryColor,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            //------------------------------------------------------
            // PRICE CARD
            //------------------------------------------------------
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = PrimaryColor,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Loyer mensuel",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.9f),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${prop.price.toInt()}",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "DT",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }

                        Icon(
                            Icons.Default.AttachMoney,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = Color.White.copy(alpha = 0.3f)
                        )
                    }
                }
            }

            //------------------------------------------------------
            // DESCRIPTION
            //------------------------------------------------------
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 24.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = SurfaceColor,
                    shadowElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "Description",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = prop.description ?: "Aucune description disponible.",
                            fontSize = 15.sp,
                            color = TextSecondary,
                            lineHeight = 24.sp
                        )
                    }
                }
            }

            //------------------------------------------------------
            // AMENITIES
            //------------------------------------------------------
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = SurfaceColor,
                    shadowElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "Équipements",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AmenityChip("WiFi", Icons.Default.Wifi, Modifier.weight(1f))
                            AmenityChip("Cuisine", Icons.Default.Restaurant, Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AmenityChip("Parking", Icons.Default.LocalParking, Modifier.weight(1f))
                            AmenityChip("Balcon", Icons.Default.Balcony, Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        AmenityChip("Machine à laver", Icons.Default.LocalLaundryService, Modifier.fillMaxWidth())
                    }
                }
            }

            //------------------------------------------------------
            // REVIEWS SECTION
            //------------------------------------------------------
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 24.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = SurfaceColor,
                    shadowElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
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
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        modifier = Modifier.size(22.dp),
                                        tint = AccentColor
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = String.format("%.1f", averageRating),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = " ($totalReviews)",
                                        fontSize = 15.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            TextButton(
                                onClick = {
                                    val currentUser = context.getSharedPreferences("APP_PREFS", android.content.Context.MODE_PRIVATE)
                                    val currentUsername = currentUser.getString("username", "Anonymous")
                                    val encodedTitle = android.net.Uri.encode(prop.title)
                                    val encodedUsername = android.net.Uri.encode(currentUsername)
                                    navController.navigate(Routes.ReviewsWithParams.replace("{propertyId}", prop.id).replace("{propertyName}", encodedTitle).replace("{userName}", encodedUsername))
                                },
                                colors = ButtonDefaults.textButtonColors(contentColor = SecondaryColor)
                            ) {
                                Text(
                                    text = "Voir tout",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        RatingBreakdown(propertyReviews)
                    }
                }
            }

            //------------------------------------------------------
            // AI SUMMARY BUTTON
            //------------------------------------------------------
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = SecondaryColor,
                    shadowElevation = 4.dp,
                    onClick = {
                        val encodedTitle = android.net.Uri.encode(prop.title)
                        navController.navigate("reviewSummary/${prop.id}/$encodedTitle")
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "View AI Summary",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        //------------------------------------------------------
        // FLOATING ACTION BUTTON
        //------------------------------------------------------
        val isFull = (prop.nbrCollocateurActuel ?: 0) >= (prop.nbrCollocateurMax ?: 0)
        val isOwner = prop.user == currentUserId

        if (!isOwner) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(24.dp),
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 8.dp
            ) {
                if (isFull) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = BackgroundColor,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(2.dp, TextTertiary.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Block,
                                contentDescription = null,
                                tint = TextTertiary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Maison complète",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            if (propertyId != null) {
                                navController.navigate(Routes.BookProperty.replace("{propertyId}", propertyId))
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(vertical = 18.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Chat,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Contacter les Colocataires",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = SecondaryColor, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(label, fontSize = 13.sp, color = TextSecondary)
        }
    }
}

@Composable
fun AmenityChip(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = BackgroundColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE8E8E8))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = SecondaryColor, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }
    }
}

@Composable
fun RatingBreakdown(reviews: List<DatabaseReview>) {
    val ratingCounts = (1..5).map { star ->
        reviews.count { it.rating == star }
    }.reversed()

    val totalReviews = reviews.size

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        for ((index, star) in (5 downTo 1).withIndex()) {
            val count = ratingCounts[index]
            val percentage = if (totalReviews > 0) count.toFloat() / totalReviews else 0f

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$star",
                    modifier = Modifier.width(16.dp),
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    Icons.Default.Star,
                    null,
                    tint = AccentColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFE8E8E8))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(percentage)
                            .background(AccentColor)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "${(percentage * 100).toInt()}%",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.width(35.dp),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ReviewCard(review: DatabaseReview) {
    val username = review.userName
    val userInitial = username.firstOrNull()?.uppercase() ?: "?"

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 12.dp),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceColor,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = CircleShape,
                        color = SecondaryColor.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                userInitial,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = SecondaryColor
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            username,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "Récemment",
                            fontSize = 12.sp,
                            color = TextTertiary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AccentColor.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = AccentColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = review.rating.toString(),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = review.comment,
                fontSize = 14.sp,
                color = TextSecondary,
                lineHeight = 22.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}