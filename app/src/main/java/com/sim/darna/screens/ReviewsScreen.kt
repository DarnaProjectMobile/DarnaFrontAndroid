package com.sim.darna.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import com.sim.darna.model.Review
import com.sim.darna.viewmodel.ReviewViewModel
import kotlinx.coroutines.delay

// Simple Color Palette
private val PrimaryColor = Color(0xFF6366F1)
private val BackgroundColor = Color(0xFFF9FAFB)
private val CardColor = Color(0xFFFFFFFF)
private val TextPrimary = Color(0xFF1F2937)
private val TextSecondary = Color(0xFF6B7280)
private val BorderColor = Color(0xFFE5E7EB)
private val AccentOrange = Color(0xFFFBBF24)
private val ErrorRed = Color(0xFFEF4444)

@Composable
fun ReviewsScreen(
    propertyId: String? = null,
    propertyName: String? = null,
    userName: String? = null,
    userId: String? = null,
    onNavigateBack: () -> Unit = { /* Default: do nothing */ }
) {
    val context = LocalContext.current
    BackHandler { onNavigateBack() }

    val vm: ReviewViewModel = viewModel()
    val reviews by vm.reviews.collectAsState()
    
    // Get current user ID for ownership checking
    val currentUserId = com.sim.darna.auth.TokenStorage.getUserId(context)

    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedReview by remember { mutableStateOf<com.sim.darna.model.Review?>(null) }

    var rating by remember { mutableStateOf(5) }
    var comment by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        vm.init(context)
        if (propertyId != null) {
            // Load reviews for specific property
            vm.loadReviewsForProperty(propertyId)
        } else if (userId != null) {
            // Load ALL reviews as requested ("afficher les autres avis d'autres clients aussi")
            // The userId param acts as a context that we are in "Mes Evaluations" mode (for UI tweaks),
            // but we fetch everything. Ownership highlighting uses TokenStorage.getUserId separately.
            vm.loadReviews()
        } else {
            // Load all reviews
            vm.loadReviews()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            SimpleHeader(
                reviewCount = reviews.size,
                onNavigateBack = onNavigateBack
            )

            // Reviews List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 200.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(reviews) { review ->
                    SimpleReviewCard(
                        review = review,
                        currentUserId = currentUserId,
                        onEdit = {
                            selectedReview = review
                            showEditDialog = true
                        },
                        onDelete = {
                            selectedReview = review
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }

        // Bottom Input Panel
        // Bottom Input Panel - Only show if we are NOT viewing a specific user's reviews (My Evaluations mode)
        // If userId is null, it means we are either viewing all reviews or property reviews, where adding a review might be relevant.
        // Actually, if we are viewing ALL reviews (userId=null, propertyId=null), it might be weird to add a review without context, 
        // but let's stick to the request: remove "rate your experience" for the "My evaluations" case (userId != null).
        if (userId == null) {
            SimpleInputPanel(
                rating = rating,
                onRatingChange = { rating = it },
                comment = comment,
                onCommentChange = { comment = it },
                onSubmit = {
                    if (comment.isNotBlank()) {
                        // Pass property and user information when creating review
                        vm.addReview(
                            rating = rating,
                            comment = comment,
                            propertyId = propertyId,
                            userName = userName,
                            propertyName = propertyName
                        )
                        comment = ""
                        rating = 5
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    // Dialogs
    if (showEditDialog && selectedReview != null) {
        SimpleEditDialog(
            review = selectedReview!!,
            onDismiss = { showEditDialog = false },
            onSave = { edited -> 
                vm.updateReview(
                    id = edited.id,
                    rating = edited.rating,
                    comment = edited.comment,
                    userName = userName,
                    propertyName = propertyName
                )
                showEditDialog = false
            }
        )
    }

    if (showDeleteDialog && selectedReview != null) {
        SimpleDeleteDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                vm.deleteReview(selectedReview!!.id)
                showDeleteDialog = false
            }
        )
    }
}

@Composable
fun SimpleHeader(reviewCount: Int, onNavigateBack: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(16.dp),
        color = PrimaryColor,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onNavigateBack() },
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "←",
                        fontSize = 20.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Reviews",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "$reviewCount reviews",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
fun SimpleRatingBar(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        (1..5).forEach { star ->
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onRatingChange(star) },
                shape = CircleShape,
                color = if (star <= rating) AccentOrange.copy(0.15f) else BorderColor,
                border = BorderStroke(
                    1.dp,
                    if (star <= rating) AccentOrange else BorderColor
                )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (star <= rating) "★" else "☆",
                        fontSize = 18.sp,
                        color = if (star <= rating) AccentOrange else TextSecondary
                    )
                }
            }
        }
    }
}

private fun isReviewOwner(review: com.sim.darna.model.Review, currentUserId: String?): Boolean {
    return currentUserId != null && review.userId == currentUserId
}

@Composable
fun SimpleReviewCard(
    review: com.sim.darna.model.Review,
    currentUserId: String?,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val username = review.userName
    
    // Check if the current user owns this review
    val isOwner = isReviewOwner(review, currentUserId)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Determine display content based on ownership (context)
                val displayTitle = if (isOwner && review.propertyName.isNotBlank()) review.propertyName else username
                val avatarChar = displayTitle.take(1).uppercase()
                val avatarColor = if (isOwner) Color(0xFFEF5350) else PrimaryColor // Red for property, Blue for user

                // Avatar
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = avatarColor
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = avatarChar,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayTitle,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    
                    // If it's the owner's review (Mes Evaluations) and showing property name, show "My Review" subtitle
                    if (isOwner && review.propertyName.isNotBlank()) {
                         Text(
                            text = "Mon évaluation",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    Spacer(Modifier.height(6.dp))

                    // Rating display
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        repeat(5) { idx ->
                            Text(
                                text = if (idx < review.rating) "★" else "☆",
                                fontSize = 14.sp,
                                color = if (idx < review.rating) AccentOrange else BorderColor
                            )
                        }
                    }
                }

                // Expand indicator
                Text(
                    text = if (expanded) "▲" else "▼",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            Divider(color = BorderColor, thickness = 1.dp)

            Spacer(Modifier.height(12.dp))

            Text(
                text = review.comment,
                fontSize = 14.sp,
                color = TextSecondary,
                lineHeight = 20.sp
            )

            // Display details (sub-ratings) if available, regardless of ownership
            // This responds to "donne evalution par detet" (give evaluation by detail)
            if (expanded && (review.cleanlinessRating != null || review.locationRating != null || review.conformityRating != null)) {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = BorderColor.copy(alpha = 0.5f), thickness = 0.5.dp)
                Spacer(Modifier.height(12.dp))
                
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    review.cleanlinessRating?.let { SubRatingRow("Propreté", it) }
                    review.locationRating?.let { SubRatingRow("Emplacement", it) }
                    review.conformityRating?.let { SubRatingRow("Conformité", it) }
                    review.collectorRating?.let { SubRatingRow("Accueil", it) }
                }
            }

            // Action buttons - only show for owners
            if (isOwner) {
                AnimatedVisibility(visible = expanded) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        SimpleActionButton(
                            text = "Edit",
                            color = PrimaryColor,
                            onClick = onEdit
                        )
                        Spacer(Modifier.width(8.dp))
                        SimpleActionButton(
                            text = "Delete",
                            color = ErrorRed,
                            onClick = onDelete
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SimpleActionButton(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            color = color,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun SimpleInputPanel(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    comment: String,
    onCommentChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = CardColor,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Rate your experience",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            Spacer(Modifier.height(12.dp))

            SimpleRatingBar(
                rating = rating,
                onRatingChange = onRatingChange,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = comment,
                    onValueChange = onCommentChange,
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text("Write your review...", color = TextSecondary)
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryColor,
                        unfocusedBorderColor = BorderColor
                    ),
                    maxLines = 3
                )

                Surface(
                    modifier = Modifier
                        .size(56.dp)
                        .clickable(enabled = comment.isNotBlank()) { onSubmit() },
                    shape = RoundedCornerShape(16.dp),
                    color = if (comment.isNotBlank()) PrimaryColor else BorderColor
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "↑",
                            fontSize = 24.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SimpleEditDialog(
    review: Review,
    onDismiss: () -> Unit,
    onSave: (Review) -> Unit
) {
    var rating by remember { mutableStateOf(review.rating) }
    var comment by remember { mutableStateOf(review.comment) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = CardColor,
        title = {
            Text(
                "Edit Review",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = TextPrimary
            )
        },
        text = {
            Column {
                Text(
                    "Rating",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(8.dp))

                SimpleRatingBar(
                    rating = rating,
                    onRatingChange = { rating = it }
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    "Review",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryColor,
                        unfocusedBorderColor = BorderColor
                    ),
                    minLines = 3,
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(review.copy(rating = rating, comment = comment)) }
            ) {
                Text(
                    "Save",
                    color = PrimaryColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
fun SimpleDeleteDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = CardColor,
        title = {
            Text(
                "Delete Review",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = TextPrimary
            )
        },
        text = {
            Text(
                "Are you sure you want to delete this review? This action cannot be undone.",
                color = TextSecondary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    "Delete",
                    color = ErrorRed,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
fun SubRatingRow(label: String, rating: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = TextSecondary)
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            repeat(5) { idx ->
                val size = 6.dp
                val color = if (idx < rating) AccentOrange else BorderColor
                Box(
                    modifier = Modifier
                        .size(size)
                        .background(color, CircleShape)
                )
            }
        }
    }
}