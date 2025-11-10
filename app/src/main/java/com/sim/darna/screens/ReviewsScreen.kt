package com.sim.darna.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

data class Review(
    val username: String,
    val rating: Int,
    val comment: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewsScreen(propertyId: String, navController: NavController) {
    val reviews = listOf(
        Review("Alice Dupont", 5, "Super expérience ! Appartement propre et bien situé."),
        Review("Mehdi K.", 4, "Très bon rapport qualité-prix. Je recommande."),
        Review("Léa M.", 3, "Pas mal, mais un peu de bruit le soir."),
        Review("Karim A.", 5, "Propriétaire très sympa et logement impeccable."),
        Review("Sofia R.", 2, "Manque de chauffage, sinon ok.")
    )

    val averageRating = reviews.map { it.rating }.average()
    val totalReviews = reviews.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Avis des locataires", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(padding)
                .padding(16.dp)
        ) {
            // ⭐ Average Rating Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = String.format("%.1f", averageRating),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0066FF)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(5) { index ->
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (index < averageRating.toInt()) Color(0xFFFFC107)
                                else Color(0xFFBDBDBD),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$totalReviews avis au total",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Rating Bar Breakdown
                    (5 downTo 1).forEach { stars ->
                        val count = reviews.count { it.rating == stars }
                        val percentage = (count.toFloat() / totalReviews) * 100
                        RatingBarRow(stars, percentage, count)
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }

            // 💬 Reviews List
            Text(
                text = "Commentaires",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(reviews) { review ->
                    ReviewCard(review)
                }
            }
        }
    }
}

@Composable
fun RatingBarRow(stars: Int, percentage: Float, count: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("$stars★", fontWeight = FontWeight.Medium, fontSize = 14.sp, modifier = Modifier.width(40.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .background(Color(0xFFE0E0E0), RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(percentage / 100)
                    .background(Color(0xFFFFC107), RoundedCornerShape(4.dp))
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text("$count", fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun ReviewCard(review: Review) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(review.username, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                Row {
                    repeat(5) { index ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (index < review.rating) Color(0xFFFFC107) else Color(0xFFBDBDBD),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = review.comment,
                fontSize = 14.sp,
                color = Color(0xFF616161),
                lineHeight = 20.sp,
                textAlign = TextAlign.Start
            )
        }
    }
}
