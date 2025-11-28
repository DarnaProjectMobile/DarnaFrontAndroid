package com.sim.darna.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sim.darna.auth.SessionManager
import com.sim.darna.ui.components.AppColors
import com.sim.darna.ui.components.AppRadius
import com.sim.darna.ui.components.AppSpacing
import com.sim.darna.ui.components.EmptyStateCard
import com.sim.darna.ui.components.FeedbackBanner
import com.sim.darna.visite.VisiteViewModel
import com.sim.darna.visite.ReviewResponse
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllReviewsScreen(
    viewModel: VisiteViewModel,
    navController: NavController? = null
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context.applicationContext) }
    val userSession = sessionManager.sessionFlow.collectAsState(initial = null).value
    val isCollocator = userSession?.role?.lowercase() == "collocator"
    
    val uiState = viewModel.state.collectAsState().value
    var allReviews by remember { mutableStateOf<List<ReviewResponse>>(emptyList()) }
    var isLoadingReviews by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (isCollocator) {
            viewModel.loadLogementsVisites()
        } else {
            viewModel.loadVisites()
        }
    }

    // Charger les reviews de toutes les visites
    LaunchedEffect(uiState.visites) {
        if (uiState.visites.isNotEmpty()) {
            isLoadingReviews = true
            scope.launch {
                val reviewsList = mutableListOf<ReviewResponse>()
                uiState.visites.forEach { visite ->
                    visite.id?.let { visiteId ->
                        try {
                            val reviews = viewModel.getVisiteReviews(visiteId)
                            reviewsList.addAll(reviews)
                        } catch (e: Exception) {
                            // Ignorer silencieusement les erreurs pour les visites sans reviews
                            // (les erreurs 403 sont déjà gérées par le ViewModel)
                        }
                    }
                }
                allReviews = reviewsList
                isLoadingReviews = false
            }
        }
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearFeedback()
        }
    }

    // Afficher les erreurs (les erreurs 403 ne sont jamais stockées dans le state)
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            viewModel.clearFeedback()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Évaluations",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.textPrimary
                        )
                        Text(
                            text = "${allReviews.size} évaluation${if (allReviews.size > 1) "s" else ""}",
                            fontSize = 12.sp,
                            color = AppColors.textSecondary
                        )
                    }
                },
                navigationIcon = {
                    if (navController != null) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Retour",
                                tint = AppColors.textPrimary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (isCollocator) {
                                viewModel.loadLogementsVisites(force = true)
                            } else {
                                viewModel.loadVisites(force = true)
                            }
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(AppColors.primary.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Actualiser",
                            tint = AppColors.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = AppColors.textPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.background)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = AppSpacing.md)
            ) {
                // Error Banner (les erreurs 403 ne sont jamais stockées dans le state)
                AnimatedVisibility(
                    visible = uiState.error != null,
                    enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
                ) {
                    uiState.error?.let { error ->
                        FeedbackBanner(
                            message = error,
                            isError = true,
                            modifier = Modifier.fillMaxWidth(),
                            onDismiss = { viewModel.clearFeedback() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.md))

                // Content
                when {
                    isLoadingReviews || uiState.isLoadingList -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = AppColors.primary)
                        }
                    }
                    allReviews.isEmpty() -> {
                        EmptyStateCard(
                            title = "Aucune évaluation",
                            description = if (isCollocator) {
                                "Les évaluations de vos logements apparaîtront ici."
                            } else {
                                "Aucune évaluation disponible pour le moment."
                            },
                            actionLabel = "Actualiser",
                            onAction = {
                                if (isCollocator) {
                                    viewModel.loadLogementsVisites(force = true)
                                } else {
                                    viewModel.loadVisites(force = true)
                                }
                            },
                            icon = Icons.Default.Star
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
                            contentPadding = PaddingValues(bottom = AppSpacing.xl)
                        ) {
                            items(
                                items = allReviews,
                                key = { it.id ?: "" }
                            ) { review ->
                                ReviewCard(review = review, visite = uiState.visites.find { it.id == review.visiteId })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewCard(
    review: ReviewResponse,
    visite: com.sim.darna.visite.VisiteResponse?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadius.md),
        colors = CardDefaults.cardColors(containerColor = AppColors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md)
        ) {
            // Header avec logement et note globale
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = visite?.logementTitle ?: visite?.logementId ?: "Logement inconnu",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.textPrimary
                    )
                    if (visite?.clientUsername != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Par ${visite.clientUsername}",
                            fontSize = 12.sp,
                            color = AppColors.textSecondary
                        )
                    }
                }
                // Note globale
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val rating = (review.rating ?: 0f).toInt()
                    repeat(5) { index ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (index < rating) Color(0xFFFFC107) else Color(0xFFBDBDBD),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format("%.1f", review.rating ?: 0f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.textPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.sm))

            // Détails des notes
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (review.collectorRating != null) {
                    RatingDetailRow("Colocateur", review.collectorRating)
                }
                if (review.cleanlinessRating != null) {
                    RatingDetailRow("Propreté", review.cleanlinessRating)
                }
                if (review.locationRating != null) {
                    RatingDetailRow("Localisation", review.locationRating)
                }
                if (review.conformityRating != null) {
                    RatingDetailRow("Conformité", review.conformityRating)
                }
            }

            // Commentaire
            if (!review.comment.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(AppSpacing.sm))
                Divider(color = AppColors.divider, thickness = 1.dp)
                Spacer(modifier = Modifier.height(AppSpacing.sm))
                Text(
                    text = review.comment ?: "",
                    fontSize = 14.sp,
                    color = AppColors.textSecondary,
                    lineHeight = 20.sp
                )
            }

            // Date
            if (review.createdAt != null) {
                Spacer(modifier = Modifier.height(AppSpacing.xs))
                Text(
                    text = "Le ${formatReviewDate(review.createdAt)}",
                    fontSize = 11.sp,
                    color = AppColors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun RatingDetailRow(label: String, value: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = AppColors.textSecondary
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            val rating = value.toInt()
            repeat(5) { index ->
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = if (index < rating) Color(0xFFFFC107) else Color(0xFFBDBDBD),
                    modifier = Modifier.size(12.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = String.format("%.1f", value),
                fontSize = 12.sp,
                color = AppColors.textPrimary
            )
        }
    }
}

private fun formatReviewDate(dateString: String): String {
    return try {
        val parser = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
        parser.timeZone = java.util.TimeZone.getTimeZone("UTC")
        val date = parser.parse(dateString)
        val formatter = java.text.SimpleDateFormat("dd MMMM yyyy", java.util.Locale.FRENCH)
        formatter.format(date ?: return dateString)
    } catch (e: Exception) {
        dateString
    }
}

