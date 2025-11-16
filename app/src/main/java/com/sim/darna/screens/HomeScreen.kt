package com.sim.darna.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sim.darna.ViewModel.AnnonceViewModel
import com.sim.darna.navigation.Routes
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun HomeScreen(
    navController: NavController,
    username: String,
    userId: String,
    role: String,
    viewModel: AnnonceViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val annonces by viewModel.annonces.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAnnonces()
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF4F6FA),
        floatingActionButton = {
            if (role == "collocator") {
                FloatingActionButton(
                    onClick = {
                        navController.navigate(Routes.AddAnnonce)
                    },
                    containerColor = Color(0xFF4461F2)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Ajouter une annonce",
                        tint = Color.White
                    )
                }
            }
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            item {
                GreetingHome(username)
            }

            items(annonces) { annonce ->
                AnnonceCard(
                    annonce = annonce,
                    userId = userId,
                    role = role,
                    onCardClick = {
                        val encodedId = URLEncoder.encode(annonce.id, StandardCharsets.UTF_8.toString())
                        val encodedUserId = URLEncoder.encode(userId, StandardCharsets.UTF_8.toString())
                        val encodedRole = URLEncoder.encode(role, StandardCharsets.UTF_8.toString())
                        navController.navigate("${Routes.PropertyDetail}?id=$encodedId&userId=$encodedUserId&role=$encodedRole")
                    },
                    onEditClick = {
                        // Navigate to edit screen or show dialog
                        val encodedId = URLEncoder.encode(annonce.id, StandardCharsets.UTF_8.toString())
                        val encodedUserId = URLEncoder.encode(userId, StandardCharsets.UTF_8.toString())
                        val encodedRole = URLEncoder.encode(role, StandardCharsets.UTF_8.toString())
                        navController.navigate("${Routes.PropertyDetail}?id=$encodedId&userId=$encodedUserId&role=$encodedRole&edit=true")
                    },
                    onDeleteClick = {
                        viewModel.deleteAnnonce(annonce.id)
                    }
                )
            }
        }
    }
}

@Composable
fun GreetingHome(username: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Bonjour 👋",
                color = Color(0xFF8A8E9F),
                fontSize = 14.sp
            )
            Text(
                text = username,
                color = Color(0xFF1B1D28),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            tonalElevation = 2.dp,
            modifier = Modifier.size(46.dp)
        ) {
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = Color.Black
                )
            }
        }
    }
}

@Composable
fun AnnonceCard(
    annonce: com.sim.darna.model.Annonce,
    userId: String,
    role: String,
    onCardClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    // Check if user owns this annonce
    // user.id can be the user ID string or the full user object's _id
    val isOwner = annonce.user.id == userId
    val showActions = role == "collocator" && isOwner && userId.isNotEmpty()

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onCardClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // TITLE
            Text(
                text = annonce.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B1D28),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // DESCRIPTION
            Text(
                text = annonce.description,
                fontSize = 14.sp,
                color = Color(0xFF8A8E9F),
                modifier = Modifier.padding(bottom = 12.dp),
                maxLines = 3,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // PRICE
                Text(
                    text = "${annonce.price} DT",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4461F2)
                )

                // USER USERNAME or ACTIONS
                if (showActions) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onEditClick() },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Modifier",
                                tint = Color(0xFF4461F2),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = { onDeleteClick() },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Supprimer",
                                tint = Color(0xFFE53935),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                } else {
                    // USER USERNAME
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Par: ",
                            fontSize = 12.sp,
                            color = Color(0xFF8A8E9F)
                        )
                        Text(
                            text = annonce.user.username ?: "Utilisateur",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1B1D28)
                        )
                    }
                }
            }
        }
    }
}