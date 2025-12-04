package com.sim.darna.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
import com.sim.darna.ui.components.AppColors
import com.sim.darna.ui.components.AppRadius
import com.sim.darna.ui.components.AppSpacing

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
                title = { 
                    Text(
                        "Avis des locataires", 
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = AppColors.textPrimary
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            AppColors.background,
                            AppColors.surfaceVariant
                        )
                    )
                )
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = AppSpacing.md)
            ) {
                Spacer(modifier = Modifier.height(AppSpacing.md))
                
                // Score global avec design moderne
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(AppRadius.xl),
                            spotColor = AppColors.primary.copy(alpha = 0.2f)
                        ),
                    shape = RoundedCornerShape(AppRadius.xl),
                    colors = CardDefaults.cardColors(containerColor = AppColors.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = BorderStroke(1.dp, AppColors.divider)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        AppColors.primary.copy(alpha = 0.05f),
                                        AppColors.surface
                                    )
                                )
                            )
                            .padding(AppSpacing.xl),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Note principale avec animation
                            Surface(
                                shape = CircleShape,
                                color = AppColors.primary.copy(alpha = 0.1f),
                                modifier = Modifier.size(120.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = String.format("%.1f", averageRating),
                                            fontSize = 48.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AppColors.primary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            repeat(5) { index ->
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = if (index < averageRating.toInt()) Color(0xFFFFC107) else Color(0xFFE2E8F0),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(AppSpacing.md))
                            
                            Text(
                                text = if (totalReviews > 0) "$totalReviews avis au total" else "Aucun avis pour le moment",
                                fontSize = 15.sp,
                                color = AppColors.textSecondary,
                                fontWeight = FontWeight.Medium
                            )

                            if (reputation != null && totalReviews > 0) {
                                Spacer(modifier = Modifier.height(AppSpacing.lg))
                                
                                // Détails avec design moderne
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            AppColors.surfaceVariant.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(AppRadius.md)
                                        )
                                        .padding(AppSpacing.md),
                                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                                ) {
                                    ModernReputationRow("Colocateur", reputation.averageCollectorRating, Icons.Default.Person)
                                    ModernReputationRow("Propreté", reputation.averageCleanlinessRating, Icons.Default.Star)
                                    ModernReputationRow("Localisation", reputation.averageLocationRating, Icons.Default.Home)
                                    ModernReputationRow("Conformité", reputation.averageConformityRating, Icons.Default.CheckCircle)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.lg))

                // Titre section commentaires
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Commentaires",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.textPrimary
                    )
                    Surface(
                        shape = RoundedCornerShape(AppRadius.md),
                        color = AppColors.primary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "${reviews.size}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.primary,
                            modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.md))

                // Liste des commentaires
                when {
                    state.isLoading && reviews.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = AppColors.primary)
                        }
                    }
                    reviews.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = AppSpacing.xxl),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = AppColors.textTertiary.copy(alpha = 0.1f),
                                    modifier = Modifier.size(64.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = AppColors.textTertiary,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Aucun avis pour le moment",
                                    fontSize = 16.sp,
                                    color = AppColors.textSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
                            contentPadding = PaddingValues(bottom = AppSpacing.xl)
                        ) {
                            items(reviews) { review ->
                                ModernReviewCard(review)
                            }
                        }
                    }
                }

                state.error?.let { error ->
                    Spacer(modifier = Modifier.height(AppSpacing.sm))
                    Surface(
                        shape = RoundedCornerShape(AppRadius.md),
                        color = AppColors.danger.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, AppColors.danger.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = error,
                            color = AppColors.danger,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(AppSpacing.sm)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernReputationRow(label: String, value: Float, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppColors.primary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                label, 
                fontSize = 15.sp, 
                color = AppColors.textPrimary,
                fontWeight = FontWeight.Medium
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val rating = value.toInt()
            repeat(5) { index ->
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = if (index < rating) Color(0xFFFFC107) else Color(0xFFE2E8F0),
                    modifier = Modifier.size(14.dp)
                )
            }
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = AppColors.primary.copy(alpha = 0.1f)
            ) {
                Text(
                    String.format("%.1f", value),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ModernReviewCard(review: CollectorReviewResponse) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(AppRadius.lg),
                spotColor = Color.Black.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(AppRadius.lg),
        colors = CardDefaults.cardColors(containerColor = AppColors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, AppColors.divider)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            AppColors.surface,
                            AppColors.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                )
                .padding(AppSpacing.lg)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = AppColors.primary.copy(alpha = 0.1f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = AppColors.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                review.userId ?: "Client",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = AppColors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val rating = (review.rating ?: 0f).toInt()
                                repeat(5) { index ->
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (index < rating) Color(0xFFFFC107) else Color(0xFFE2E8F0),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = String.format("%.1f", review.rating ?: 0f),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.textSecondary
                                )
                            }
                        }
                    }
                }

                if (!review.comment.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(AppSpacing.md))
                    Divider(color = AppColors.divider, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(AppSpacing.md))
                    Text(
                        text = review.comment ?: "",
                        fontSize = 15.sp,
                        color = AppColors.textPrimary,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Start
                    )
                }
            }
        }
    }
}
