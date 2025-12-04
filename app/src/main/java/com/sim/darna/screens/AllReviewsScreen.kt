package com.sim.darna.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
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
import kotlinx.coroutines.delay

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
    var previousReviewsCount by remember { mutableStateOf(0) }
    var newReviewIds by remember { mutableStateOf<Set<String>>(emptySet()) }
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
                // Trier les reviews par date (plus récentes en premier)
                val sortedReviews = reviewsList.sortedByDescending { 
                    it.createdAt?.let { dateStr ->
                        try {
                            val parser = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
                            parser.timeZone = java.util.TimeZone.getTimeZone("UTC")
                            parser.parse(dateStr)?.time ?: 0L
                        } catch (e: Exception) {
                            0L
                        }
                    } ?: 0L
                }
                
                // Détecter les nouvelles évaluations
                val previousIds = allReviews.mapNotNull { it.id }.toSet()
                val currentIds = sortedReviews.mapNotNull { it.id }.toSet()
                val newIds = currentIds - previousIds
                
                allReviews = sortedReviews
                newReviewIds = newIds
                isLoadingReviews = false
                
                // Supprimer le badge après 5 secondes
                if (newIds.isNotEmpty()) {
                    delay(5000)
                    newReviewIds = emptySet()
                }
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
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.textPrimary
                        )
                        Text(
                            text = "${allReviews.size} évaluation${if (allReviews.size > 1) "s" else ""}",
                            fontSize = 13.sp,
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
                        }
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Actualiser",
                            tint = AppColors.textPrimary
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
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            AppColors.background,
                            AppColors.surfaceVariant
                        )
                    )
                )
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
                                val isNew = review.id != null && newReviewIds.contains(review.id)
                                AnimatedReviewCard(
                                    review = review, 
                                    visite = uiState.visites.find { it.id == review.visiteId },
                                    isNew = isNew
                                )
                            }
                        }
                    }
                }
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

@Composable
private fun ModernRatingDetailRow(label: String, value: Float, icon: androidx.compose.ui.graphics.vector.ImageVector) {
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
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                fontSize = 14.sp,
                color = AppColors.textPrimary,
                fontWeight = FontWeight.Medium
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
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
                color = AppColors.primary.copy(alpha = 0.1f),
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Text(
                    text = String.format("%.1f", value),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.primary,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
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

@Composable
private fun AnimatedReviewCard(
    review: ReviewResponse,
    visite: com.sim.darna.visite.VisiteResponse?,
    isNew: Boolean
) {
    // Animation pour l'entrée
    var showGreenBorder by remember { mutableStateOf(isNew) }
    
    LaunchedEffect(isNew) {
        if (isNew) {
            showGreenBorder = true
            delay(5000) // Disparaître après 5 secondes
            showGreenBorder = false
        }
    }
    
    val borderColor = if (showGreenBorder) AppColors.success else Color.Transparent
    
    val borderWidth by animateFloatAsState(
        targetValue = if (showGreenBorder) 3f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "border_width"
    )
    
    val cardElevation by animateFloatAsState(
        targetValue = if (showGreenBorder) 8f else 2f,
        animationSpec = tween(durationMillis = 300),
        label = "elevation"
    )
    
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = tween(400)) + slideInVertically(
            initialOffsetY = { -it / 2 },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ),
        exit = fadeOut(animationSpec = tween(300))
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = cardElevation.dp,
                    shape = RoundedCornerShape(AppRadius.lg),
                    spotColor = if (showGreenBorder) AppColors.success.copy(alpha = 0.3f) else Color.Black.copy(
                        alpha = 0.1f
                    )
                ),
            shape = RoundedCornerShape(AppRadius.lg),
            colors = CardDefaults.cardColors(containerColor = AppColors.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = if (showGreenBorder) BorderStroke(
                width = borderWidth.dp,
                color = borderColor
            ) else BorderStroke(1.dp, AppColors.divider)
        ) {
            Column(
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
                // Badge "Nouvelle évaluation" moderne avec gradient
                AnimatedVisibility(
                    visible = showGreenBorder,
                    enter = fadeIn(animationSpec = tween(300)) + slideInHorizontally(
                        initialOffsetX = { -it },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ) + scaleIn(initialScale = 0.8f),
                    exit = fadeOut(animationSpec = tween(300)) + slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(300)
                    ) + scaleOut(targetScale = 0.8f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = AppSpacing.md)
                            .clip(RoundedCornerShape(AppRadius.md))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        AppColors.success.copy(alpha = 0.15f),
                                        AppColors.success.copy(alpha = 0.08f)
                                    )
                                )
                            )
                            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = AppColors.success.copy(alpha = 0.2f),
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = AppColors.success,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Nouvelle évaluation",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.success
                            )
                        }
                    }
                }

                // Header avec logement et note globale - Design moderne
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(AppRadius.sm),
                                color = AppColors.primary.copy(alpha = 0.1f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Home,
                                        contentDescription = null,
                                        tint = AppColors.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = visite?.logementTitle ?: visite?.logementId
                                    ?: "Logement inconnu",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.textPrimary
                                )
                                if (visite?.clientUsername != null) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = AppColors.textSecondary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = visite.clientUsername,
                                            fontSize = 13.sp,
                                            color = AppColors.textSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                    // Note globale avec badge moderne
                    Surface(
                        shape = RoundedCornerShape(AppRadius.md),
                        color = AppColors.primary.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, AppColors.primary.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = AppSpacing.sm,
                                vertical = AppSpacing.xs
                            ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val rating = (review.rating ?: 0f).toInt()
                            repeat(5) { index ->
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (index < rating) Color(0xFFFFC107) else Color(
                                        0xFFE2E8F0
                                    ),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = String.format("%.1f", review.rating ?: 0f),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacing.md))
                Divider(color = AppColors.divider, thickness = 1.dp)
                Spacer(modifier = Modifier.height(AppSpacing.md))

                // Détails des notes avec design moderne
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
                    if (review.collectorRating != null) {
                        ModernRatingDetailRow(
                            "Colocateur",
                            review.collectorRating,
                            Icons.Default.Person
                        )
                    }
                    if (review.cleanlinessRating != null) {
                        ModernRatingDetailRow(
                            "Propreté",
                            review.cleanlinessRating,
                            Icons.Default.Star
                        )
                    }
                    if (review.locationRating != null) {
                        ModernRatingDetailRow(
                            "Localisation",
                            review.locationRating,
                            Icons.Default.Home
                        )
                    }
                    if (review.conformityRating != null) {
                        ModernRatingDetailRow(
                            "Conformité",
                            review.conformityRating,
                            Icons.Default.CheckCircle
                        )
                    }
                }

                // Commentaire avec design moderne
                if (!review.comment.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(AppSpacing.md))
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = AppColors.info.copy(alpha = 0.1f),
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = AppColors.info,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = review.comment ?: "",
                                fontSize = 14.sp,
                                color = AppColors.textPrimary,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }

                // Date avec icône
                if (review.createdAt != null) {
                    Spacer(modifier = Modifier.height(AppSpacing.md))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = AppColors.textTertiary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = formatReviewDate(review.createdAt),
                            fontSize = 12.sp,
                            color = AppColors.textTertiary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }}
