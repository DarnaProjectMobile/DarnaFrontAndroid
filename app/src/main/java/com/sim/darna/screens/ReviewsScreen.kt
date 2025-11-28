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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.sim.darna.auth.SessionManager
import com.sim.darna.factory.ReviewsVmFactory
import com.sim.darna.network.NetworkConfig
import com.sim.darna.reviews.CollectorReviewResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewsScreen(propertyId: String, navController: NavController) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context.applicationContext) }
    val baseUrl = remember { NetworkConfig.getBaseUrl(context.applicationContext) }
    val viewModel: com.sim.darna.reviews.ReviewsViewModel = viewModel(
        factory = ReviewsVmFactory(
            baseUrl = baseUrl,
            sessionManager = sessionManager
        )
    )
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadMyReviewsAndReputation()
    }

    val reviews = state.feedbacks
    val reputation = state.reputation

    val averageRating = reputation?.averageRating?.toDouble() ?: 0.0
    val totalReviews = reputation?.reviewsCount ?: 0

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
            // Score global
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
                        text = if (totalReviews > 0) "$totalReviews avis au total" else "Aucun avis pour le moment",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (reputation != null && totalReviews > 0) {
                        ReputationDetailRow("Collector", reputation.averageCollectorRating)
                        Spacer(Modifier.height(4.dp))
                        ReputationDetailRow("Propreté", reputation.averageCleanlinessRating)
                        Spacer(Modifier.height(4.dp))
                        ReputationDetailRow("Localisation", reputation.averageLocationRating)
                        Spacer(Modifier.height(4.dp))
                        ReputationDetailRow("Conformité", reputation.averageConformityRating)
                    }
                }
            }

            Text(
                text = "Commentaires",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            when {
                state.isLoading && reviews.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                reviews.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aucun avis pour le moment",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(reviews) { review ->
                            ReviewCardFromApi(review)
                        }
                    }
                }
            }

            state.error?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    color = Color.Red,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun ReputationDetailRow(label: String, value: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = Color.Gray)
        Text(
            String.format("%.1f / 5", value),
            fontSize = 13.sp,
            color = Color(0xFF0066FF),
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ReviewCardFromApi(review: CollectorReviewResponse) {
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
                Text(
                    review.userId ?: "Client",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Row {
                    val rating = (review.rating ?: 0f).toInt()
                    repeat(5) { index ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (index < rating) Color(0xFFFFC107) else Color(0xFFBDBDBD),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = review.comment ?: "",
                fontSize = 14.sp,
                color = Color(0xFF616161),
                lineHeight = 20.sp,
                textAlign = TextAlign.Start
            )
        }
    }
}