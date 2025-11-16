package com.sim.darna.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sim.darna.ViewModel.AnnonceViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    username: String,
    viewModel: AnnonceViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {

    val annonces by viewModel.annonces.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAnnonces() // Fixed: changed getAllAnnonces() to loadAnnonces()
    }

    Scaffold(
        containerColor = Color(0xFFF4F6FA)
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
                AnnonceCard(annonce = annonce)
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
fun AnnonceCard(annonce: com.sim.darna.model.Annonce) {

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
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
                        text = annonce.user.username,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1B1D28)
                    )
                }
            }
        }
    }
}