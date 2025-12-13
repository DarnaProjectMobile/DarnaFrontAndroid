package com.sim.darna.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.sim.darna.factory.VisiteVmFactory
import com.sim.darna.ui.components.*
import com.sim.darna.visite.ReviewResponse
import com.sim.darna.visite.VisiteViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceivedReviewsScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("APP_PREFS", android.content.Context.MODE_PRIVATE)
    val baseUrl = "http://192.168.1.101:3009/"
    
    val viewModel: VisiteViewModel = viewModel(factory = VisiteVmFactory(baseUrl, context))
    val uiState = viewModel.state.collectAsState().value
    
    var allReviews by remember { mutableStateOf<List<ReviewResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        scope.launch {
            isLoading = true
            viewModel.loadLogementsVisites()
            // Wait for visites to load
            kotlinx.coroutines.delay(500)
            
            // Collect all reviews from all completed visites
            val reviews = mutableListOf<ReviewResponse>()
            uiState.visites.forEach { visite ->
                if (visite.reviewId != null && visite.id != null) {
                    try {
                        val visiteReviews = viewModel.getVisiteReviews(visite.id)
                        reviews.addAll(visiteReviews)
                    } catch (e: Exception) {
                        // Ignore errors for individual visites
                    }
                }
            }
            allReviews = reviews
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.StarRate,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Text(
                            "Évaluations reçues",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFFC107)
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            AppColors.background,
                            AppColors.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = AppSpacing.md)
            ) {
                Spacer(modifier = Modifier.height(AppSpacing.md))
                
                // Statistics Card
                if (allReviews.isNotEmpty()) {
                    val avgCollector = allReviews.mapNotNull { it.collectorRating }.average().toFloat()
                    val avgCleanliness = allReviews.mapNotNull { it.cleanlinessRating }.average().toFloat()
                    val avgLocation = allReviews.mapNotNull { it.locationRating }.average().toFloat()
                    val avgConformity = allReviews.mapNotNull { it.conformityRating }.average().toFloat()
                    val avgOverall = (avgCollector + avgCleanliness + avgLocation + avgConformity) / 4
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(AppRadius.lg),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFF9C4)
                        ),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(AppSpacing.lg),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Note moyenne",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF57C00)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFC107),
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    text = String.format("%.1f", avgOverall),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFF57C00)
                                )
                                Text(
                                    text = "/ 5.0",
                                    fontSize = 18.sp,
                                    color = Color(0xFFF57C00).copy(alpha = 0.7f)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${allReviews.size} évaluation${if (allReviews.size > 1) "s" else ""}",
                                fontSize = 14.sp,
                                color = Color(0xFFF57C00).copy(alpha = 0.8f)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(AppSpacing.md))
                }
                
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFFFFC107))
                        }
                    }
                    allReviews.isEmpty() -> {
                        EmptyStateCard(
                            title = "Aucune évaluation",
                            description = "Vous n'avez pas encore reçu d'évaluations de vos clients.",
                            actionLabel = "Actualiser",
                            onAction = {
                                scope.launch {
                                    isLoading = true
                                    viewModel.loadLogementsVisites(force = true)
                                    kotlinx.coroutines.delay(500)
                                    val reviews = mutableListOf<ReviewResponse>()
                                    uiState.visites.forEach { visite ->
                                        if (visite.reviewId != null && visite.id != null) {
                                            try {
                                                val visiteReviews = viewModel.getVisiteReviews(visite.id)
                                                reviews.addAll(visiteReviews)
                                            } catch (e: Exception) {
                                                // Ignore
                                            }
                                        }
                                    }
                                    allReviews = reviews
                                    isLoading = false
                                }
                            },
                            icon = Icons.Default.StarBorder
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
                                key = { it.id ?: UUID.randomUUID().toString() }
                            ) { review ->
                                ReviewCard(review)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewCard(review: ReviewResponse) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(500)) +
                slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) +
                scaleIn(initialScale = 0.8f, animationSpec = tween(500))
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(AppRadius.lg),
                    spotColor = Color(0xFFFFC107).copy(alpha = 0.3f)
                ),
            shape = RoundedCornerShape(AppRadius.lg),
            colors = CardDefaults.cardColors(containerColor = AppColors.surface),
            border = BorderStroke(
                width = 2.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFFFC107).copy(alpha = 0.4f),
                        Color(0xFFFFA000).copy(alpha = 0.2f)
                    )
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.md)
            ) {
                // Header
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
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = Color(0xFFFFC107).copy(alpha = 0.2f)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color(0xFFFFC107),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        
                        Column {
                            Text(
                                text = "Client",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.textPrimary
                            )
                            review.createdAt?.let {
                                Text(
                                    text = formatReviewDate(it),
                                    fontSize = 12.sp,
                                    color = AppColors.textSecondary
                                )
                            }
                        }
                    }
                    
                    // Overall rating
                    val avgRating = listOfNotNull(
                        review.collectorRating,
                        review.cleanlinessRating,
                        review.locationRating,
                        review.conformityRating
                    ).average().toFloat()
                    
                    Surface(
                        shape = RoundedCornerShape(AppRadius.round),
                        color = Color(0xFFFFC107).copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = String.format("%.1f", avgRating),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF57C00)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(AppSpacing.md))
                
                // Rating Details
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    review.collectorRating?.let {
                        RatingRow("Service", it)
                    }
                    review.cleanlinessRating?.let {
                        RatingRow("Propreté", it)
                    }
                    review.locationRating?.let {
                        RatingRow("Localisation", it)
                    }
                    review.conformityRating?.let {
                        RatingRow("Conformité", it)
                    }
                }
                
                // Comment
                review.comment?.let { comment ->
                    if (comment.isNotBlank()) {
                        Spacer(modifier = Modifier.height(AppSpacing.md))
                        HorizontalDivider(color = AppColors.divider)
                        Spacer(modifier = Modifier.height(AppSpacing.md))
                        
                        Surface(
                            shape = RoundedCornerShape(AppRadius.md),
                            color = AppColors.surfaceVariant.copy(alpha = 0.3f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(AppSpacing.sm),
                                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                            ) {
                                Icon(
                                    Icons.Default.Comment,
                                    contentDescription = null,
                                    tint = AppColors.textSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = comment,
                                    fontSize = 14.sp,
                                    color = AppColors.textSecondary,
                                    modifier = Modifier.weight(1f)
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
private fun RatingRow(label: String, rating: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = AppColors.textSecondary,
            modifier = Modifier.width(100.dp)
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(5) { index ->
                Icon(
                    imageVector = if (index < rating.toInt()) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = null,
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = String.format("%.1f", rating),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.textPrimary
            )
        }
    }
}

private fun formatReviewDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(dateString)
        
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.FRENCH)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}
