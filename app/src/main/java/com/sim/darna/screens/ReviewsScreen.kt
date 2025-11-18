package com.sim.darna.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sim.darna.model.Review
import com.sim.darna.viewmodel.ReviewViewModel

@Composable
fun ReviewsScreen() {

    val context = LocalContext.current
    val vm: ReviewViewModel = viewModel()
    val reviews by vm.reviews.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedReview by remember { mutableStateOf<Review?>(null) }

    // Bottom input states
    var rating by remember { mutableStateOf(5) }
    var comment by remember { mutableStateOf("") }
    var isInputFocused by remember { mutableStateOf(false) }

    // Initialize ViewModel only once
    LaunchedEffect(Unit) {
        vm.init(context)
        vm.loadReviews()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Header with gradient
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 4.dp,
                color = Color.White
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF1A73E8),
                                    Color(0xFF0D47A1)
                                )
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    Column {
                        Text(
                            text = "Reviews & Ratings",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "${reviews.size} reviews",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            // Reviews List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(reviews) { review ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically()
                    ) {
                        ReviewItem(
                            review = review,
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

                item {
                    Spacer(Modifier.height(100.dp))
                }
            }
        }

        // Bottom Input Section (Google Play style)
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            shadowElevation = 16.dp,
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {

                // Star Rating Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Rate this:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF5F6368),
                        modifier = Modifier.padding(end = 12.dp)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        (1..5).forEach { star ->
                            var scale by remember { mutableStateOf(1f) }
                            val starScale by animateFloatAsState(
                                targetValue = scale,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                            )

                            Text(
                                text = if (star <= rating) "⭐" else "☆",
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier
                                    .scale(starScale)
                                    .clickable {
                                        rating = star
                                        scale = 1.2f
                                    }
                            )

                            LaunchedEffect(scale) {
                                if (scale > 1f) {
                                    kotlinx.coroutines.delay(150)
                                    scale = 1f
                                }
                            }
                        }
                    }
                }

                // Input Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    // Input Field
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(
                                "Share your experience...",
                                color = Color(0xFFBDBDBD)
                            )
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1A73E8),
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedContainerColor = Color(0xFFF8F9FA),
                            unfocusedContainerColor = Color(0xFFF8F9FA)
                        ),
                        maxLines = 3
                    )

                    // Send Button
                    val buttonScale by animateFloatAsState(
                        targetValue = if (comment.isNotBlank()) 1f else 0.8f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                    )

                    IconButton(
                        onClick = {
                            if (comment.isNotBlank()) {
                                vm.addReview(rating, comment)
                                comment = ""
                                rating = 5
                            }
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .scale(buttonScale)
                            .shadow(4.dp, CircleShape)
                            .clip(CircleShape)
                            .background(
                                if (comment.isNotBlank()) {
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF1A73E8),
                                            Color(0xFF0D47A1)
                                        )
                                    )
                                } else {
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFFE0E0E0),
                                            Color(0xFFBDBDBD)
                                        )
                                    )
                                }
                            ),
                        enabled = comment.isNotBlank()
                    ) {
                        Text(
                            text = "➤",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // EDIT REVIEW
    if (showEditDialog && selectedReview != null) {
        EditReviewDialog(
            review = selectedReview!!,
            onDismiss = { showEditDialog = false },
            onSave = { edited ->
                vm.updateReview(edited._id, edited.rating, edited.comment)
                showEditDialog = false
            }
        )
    }

    // DELETE REVIEW
    if (showDeleteDialog && selectedReview != null) {
        DeleteConfirmDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                vm.deleteReview(selectedReview!!._id)
                showDeleteDialog = false
            }
        )
    }
}

/* ----------------------------------------------------------
   REVIEW ITEM (Google Play Style)
---------------------------------------------------------- */

@Composable
fun ReviewItem(
    review: Review,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp, vertical = 0.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Avatar
                // Extract username safely
                val username = review.user?.username ?: "Unknown"

// Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF1A73E8),
                                    Color(0xFF0D47A1)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = username.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {

                    Text(
                        text = username,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF202124)
                    )

                    Spacer(Modifier.height(4.dp))

                    // Star Rating
                    Row {
                        repeat(review.rating) {
                            Text("⭐", style = MaterialTheme.typography.bodySmall)
                        }
                        repeat(5 - review.rating) {
                            Text("☆", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

            }

            Spacer(Modifier.height(12.dp))

            // Comment
            Text(
                text = review.comment,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF5F6368),
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )

            // Action Buttons (shown when expanded)
            AnimatedVisibility(visible = expanded) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onEdit,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFF1A73E8)
                        )
                    ) {
                        Text("Edit", fontWeight = FontWeight.Medium)
                    }

                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFFD93025)
                        )
                    ) {
                        Text("Delete", fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        Divider(
            color = Color(0xFFE8EAED),
            thickness = 1.dp,
            modifier = Modifier.padding(start = 72.dp)
        )
    }
}

/* ----------------------------------------------------------
   EDIT REVIEW DIALOG
---------------------------------------------------------- */

@Composable
fun EditReviewDialog(
    review: Review,
    onDismiss: () -> Unit,
    onSave: (Review) -> Unit
) {
    var rating by remember { mutableStateOf(review.rating) }
    var comment by remember { mutableStateOf(review.comment) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Edit Review",
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF202124)
            )
        },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (1..5).forEach { star ->
                        Text(
                            text = if (star <= rating) "⭐" else "☆",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.clickable { rating = star }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1A73E8),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(review.copy(rating = rating, comment = comment)) },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFF1A73E8)
                )
            ) {
                Text("SAVE", fontWeight = FontWeight.Medium)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFF5F6368)
                )
            ) {
                Text("CANCEL", fontWeight = FontWeight.Medium)
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

/* ----------------------------------------------------------
   DELETE CONFIRM DIALOG
---------------------------------------------------------- */

@Composable
fun DeleteConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Delete Review",
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF202124)
            )
        },
        text = {
            Text(
                "This review will be permanently deleted.",
                color = Color(0xFF5F6368)
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFFD93025)
                )
            ) {
                Text("DELETE", fontWeight = FontWeight.Medium)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFF5F6368)
                )
            ) {
                Text("CANCEL", fontWeight = FontWeight.Medium)
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}