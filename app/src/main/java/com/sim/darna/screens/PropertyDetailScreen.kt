package com.sim.darna.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sim.darna.navigation.Routes

//------------------------------------------------------
// DATA CLASS (CLEAN VERSION)
//------------------------------------------------------

data class Review(
    val id: String,
    val userName: String,
    val rating: Int,
    val comment: String,
    val date: String,
    val userInitial: String = userName.first().toString()
)

//------------------------------------------------------
// MAIN SCREEN
//------------------------------------------------------
@Composable
fun PropertyDetailScreen(navController: NavController) {

    val reviews = remember {
        listOf(
            Review("1", "Marie Dubois", 5, "Excellent appartement, très bien situé! Le quartier est calme et les transports sont à proximité.", "Il y a 2 jours"),
            Review("2", "Marie Dubois", 5, "Excellent appartement, très bien situé! Le quartier est calme et les transports sont à proximité.", "Il y a 2 jours"),
            Review("3", "Marie Dubois", 5, "Excellent appartement, très bien situé! Le quartier est calme et les transports sont à proximité.", "Il y a 2 jours"),
        )
    }

    val averageRating = 4.8f
    val totalReviews = 127

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {

        //------------------------------------------------------
        // HEADER
        //------------------------------------------------------
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(Color(0xFF4A90E2), Color(0xFF2C5AA0))
                        )
                    )
            ) {
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

                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Property",
                    modifier = Modifier
                        .size(120.dp)
                        .align(Alignment.Center),
                    tint = Color.White.copy(alpha = 0.3f)
                )
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
                    text = "Colocation moderne à Paris",
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
                        text = "75011 - Bastille, Paris",
                        fontSize = 16.sp,
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
                        value = "3 pers.",
                        modifier = Modifier.weight(1f)
                    )
                    PropertyInfoCard(
                        icon = Icons.Default.Home,
                        label = "Surface",
                        value = "85m²",
                        modifier = Modifier.weight(1f)
                    )
                    PropertyInfoCard(
                        icon = Icons.Default.AttachMoney,
                        label = "Loyer",
                        value = "650€",
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
                    text = "Magnifique colocation dans le quartier animé de Bastille. L'appartement dispose de 4 chambres, 2 salles de bain, une cuisine équipée et un salon spacieux. Proche de tous commerces et transports.",
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

                RatingBreakdown(averageRating)
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

        items(reviews.take(3)) { review ->
            ReviewCard(review)
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
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
fun RatingBreakdown(averageRating: Float) {

    // FIXED & CORRECTED VALUE
    val progress = (averageRating / 5f)

    Column {
        for (star in 5 downTo 1) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("$star", modifier = Modifier.width(20.dp), color = Color(0xFF757575))
                Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(8.dp))

                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFFFFC107),
                    trackColor = Color(0xFFE0E0E0)
                )

                Spacer(modifier = Modifier.width(8.dp))
                Text("${(progress * 100).toInt()}%", fontSize = 12.sp, color = Color(0xFF757575))
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun ReviewCard(review: Review) {
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
                                review.userName.first().toString(),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0066FF)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(review.userName, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Text(review.date, fontSize = 12.sp, color = Color(0xFF9E9E9E))
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
