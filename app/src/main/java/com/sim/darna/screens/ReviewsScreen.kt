package com.sim.darna.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sim.darna.model.Review
import com.sim.darna.viewmodel.ReviewViewModel

@Composable
fun ReviewsScreen() {

    val context = LocalContext.current
    val vm: ReviewViewModel = viewModel()
    val reviews by vm.reviews.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedReview by remember { mutableStateOf<Review?>(null) }

    // Initialize ViewModel only once
    LaunchedEffect(Unit) {
        vm.init(context)
        vm.loadReviews()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Text("+")
            }
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            items(reviews) { review ->
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
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }

    // CREATE REVIEW
    if (showAddDialog) {
        AddReviewDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { rating, comment ->
                vm.addReview(rating, comment)
                showAddDialog = false
            }
        )
    }

    // EDIT REVIEW
    if (showEditDialog && selectedReview != null) {
        EditReviewDialog(
            review = selectedReview!!,
            onDismiss = { showEditDialog = false },
            onSave = { edited ->
                vm.updateReview(edited._id!!, edited.rating, edited.comment)
                showEditDialog = false
            }
        )
    }

    // DELETE REVIEW
    if (showDeleteDialog && selectedReview != null) {
        DeleteConfirmDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                vm.deleteReview(selectedReview!!._id!!)
                showDeleteDialog = false
            }
        )
    }
}

/* ----------------------------------------------------------
   REVIEW ITEM
---------------------------------------------------------- */

@Composable
fun ReviewItem(
    review: Review,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {

            // 🔥 FIX: Show username correctly from backend
            Text(
                text = review.user?._id ?: "Unknown User",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(4.dp))

            Row {
                repeat(review.rating) { Text("⭐") }
            }

            Spacer(Modifier.height(4.dp))

            Text(review.comment)

            Spacer(Modifier.height(8.dp))

            Row {
                TextButton(onClick = onEdit) { Text("Edit") }
                TextButton(onClick = onDelete) { Text("Delete") }
            }
        }
    }
}

/* ----------------------------------------------------------
   ADD REVIEW DIALOG
---------------------------------------------------------- */

@Composable
fun AddReviewDialog(onDismiss: () -> Unit, onAdd: (Int, String) -> Unit) {
    var rating by remember { mutableStateOf(5) }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Review") },
        text = {
            Column {

                Row {
                    (1..5).forEach { star ->
                        Text(
                            text = if (star <= rating) "⭐" else "☆",
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .clickable { rating = star }
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Comment") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(rating, comment) },
                enabled = comment.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
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
        title = { Text("Edit Review") },
        text = {
            Column {

                Row {
                    (1..5).forEach { star ->
                        Text(
                            text = if (star <= rating) "⭐" else "☆",
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .clickable { rating = star }
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(review.copy(rating = rating, comment = comment)) }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
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
        title = { Text("Delete Review") },
        text = { Text("Are you sure? This action cannot be undone.") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Delete") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
