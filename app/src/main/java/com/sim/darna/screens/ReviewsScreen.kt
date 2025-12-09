package com.sim.darna.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.sim.darna.model.Review
import com.sim.darna.model.Review as DatabaseReview
import com.sim.darna.viewmodel.ReviewViewModel

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewsScreen(
    propertyId: String = "",
    propertyName: String = "",
    userName: String = "",
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val reviewViewModel: ReviewViewModel = viewModel()
    val allReviews by reviewViewModel.reviews.collectAsState()
    
    var showAddReviewDialog by remember { mutableStateOf(false) }
    var selectedRating by remember { mutableStateOf(0) }
    var reviewComment by remember { mutableStateOf("") }
    
    // Initialize ViewModel
    LaunchedEffect(Unit) {
        reviewViewModel.init(context)
        if (propertyId.isNotEmpty()) {
            reviewViewModel.loadReviewsForProperty(propertyId)
        } else {
            reviewViewModel.loadReviews()
        }
    }
    
    // Backend already filters by propertyId, use reviews directly
    val reviews = allReviews
    
    val averageRating = if (reviews.isNotEmpty()) {
        reviews.map { it.rating }.average().toFloat()
    } else {
        0f
    }
    val totalReviews = reviews.size
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (propertyName.isNotEmpty()) propertyName else "Tous les avis",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        if (totalReviews > 0) {
                            Text(
                                text = "$totalReviews avis • ${String.format("%.1f", averageRating)} ⭐",
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceColor
                )
            )
        },
        floatingActionButton = {
            if (propertyId.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { showAddReviewDialog = true },
                    containerColor = PrimaryColor
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Ajouter un avis",
                        tint = Color.White
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundColor)
                .padding(paddingValues)
        ) {
            if (reviews.isEmpty()) {
                EmptyReviewsState(
                    propertyName = propertyName,
                    onAddReview = { showAddReviewDialog = true }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Rating Summary Card
                    item {
                        RatingSummaryCard(
                            reviews = reviews,
                            averageRating = averageRating,
                            totalReviews = totalReviews
                        )
                    }
                    
                    // Rating Breakdown
                    item {
                        RatingBreakdownCard(reviews = reviews)
                    }
                    
                    // Reviews List Header
                    item {
                        Text(
                            text = "Avis ($totalReviews)",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    // Reviews
                    items(reviews) { review ->
                        FullReviewCard(review = review)
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
    
    // Add Review Dialog
    if (showAddReviewDialog && propertyId.isNotEmpty()) {
        AddReviewDialog(
            propertyName = propertyName,
            userName = userName,
            rating = selectedRating,
            comment = reviewComment,
            onRatingChange = { selectedRating = it },
            onCommentChange = { reviewComment = it },
            onDismiss = {
                showAddReviewDialog = false
                selectedRating = 0
                reviewComment = ""
            },
            onSubmit = {
                reviewViewModel.addReview(
                    rating = selectedRating,
                    comment = reviewComment,
                    propertyId = propertyId,
                    userName = userName.ifEmpty { null },
                    propertyName = propertyName.ifEmpty { null }
                )
                showAddReviewDialog = false
                selectedRating = 0
                reviewComment = ""
            }
        )
    }
}

@Composable
fun EmptyReviewsState(
    propertyName: String,
    onAddReview: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.StarOutline,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = TextTertiary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Aucun avis",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (propertyName.isNotEmpty()) {
                "Soyez le premier à laisser un avis pour $propertyName"
            } else {
                "Aucun avis disponible pour le moment"
            },
            fontSize = 16.sp,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        if (propertyName.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onAddReview,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ajouter un avis", fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun RatingSummaryCard(
    reviews: List<Review>,
    averageRating: Float,
    totalReviews: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = SurfaceColor,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = String.format("%.1f", averageRating),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            repeat(5) { index ->
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = if (index < averageRating.toInt()) AccentColor else Color(0xFFE0E0E0)
                                )
                            }
                        }
                        Text(
                            text = "$totalReviews avis",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RatingBreakdownCard(reviews: List<Review>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = SurfaceColor,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Répartition des notes",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            // Use the RatingBreakdown function from PropertyDetailScreen.kt
            // DatabaseReview is a type alias for Review
            RatingBreakdown(reviews = reviews as List<DatabaseReview>)
        }
    }
}

@Composable
fun FullReviewCard(review: Review) {
    val username = review.userName
    val userInitial = username.firstOrNull()?.uppercase() ?: "?"

    Surface(
        modifier = Modifier.fillMaxWidth(),
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
                            review.date ?: review.createdAt ?: "Récemment",
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
                lineHeight = 22.sp
            )
            
            if (review.propertyName.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Pour: ${review.propertyName}",
                    fontSize = 12.sp,
                    color = TextTertiary,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

@Composable
fun AddReviewDialog(
    propertyName: String,
    userName: String,
    rating: Int,
    comment: String,
    onRatingChange: (Int) -> Unit,
    onCommentChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Ajouter un avis",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (propertyName.isNotEmpty()) {
                    Text(
                        text = "Propriété: $propertyName",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
                
                Text(
                    text = "Note",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    repeat(5) { index ->
                        IconButton(
                            onClick = { onRatingChange(index + 1) }
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "${index + 1} étoiles",
                                modifier = Modifier.size(40.dp),
                                tint = if (index < rating) AccentColor else Color(0xFFE0E0E0)
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = comment,
                    onValueChange = onCommentChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Commentaire") },
                    placeholder = { Text("Partagez votre expérience...") },
                    minLines = 4,
                    maxLines = 6
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSubmit,
                enabled = rating > 0 && comment.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
            ) {
                Text("Publier")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

