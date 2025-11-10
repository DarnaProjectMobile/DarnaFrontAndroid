package com.sim.darna.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sim.darna.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyDetailScreen(
    navController: NavController,
    title: String = "Détails du bien" // ✅ default fallback title
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // ✅ Header Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo), // Replace with actual image
                    contentDescription = "Image du bien",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // ✅ Property Info
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Paris, France",
                    fontSize = 16.sp,
                    color = Color(0xFF757575)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("4.8 (35 avis)", color = Color(0xFF1A1A1A), fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Description",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Appartement moderne et lumineux situé au cœur de Paris. " +
                            "Idéal pour étudiants ou jeunes actifs, avec toutes les commodités à proximité : métro, commerces et cafés.",
                    fontSize = 15.sp,
                    color = Color(0xFF616161),
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ✅ Amenities section
                Text(
                    text = "Équipements",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• Wi-Fi haut débit", color = Color(0xFF616161))
                    Text("• Cuisine équipée", color = Color(0xFF616161))
                    Text("• Lave-linge", color = Color(0xFF616161))
                    Text("• Chauffage central", color = Color(0xFF616161))
                }

                Spacer(modifier = Modifier.height(32.dp))

                // ✅ Price and button
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 3.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Prix par mois", color = Color(0xFF757575), fontSize = 14.sp)
                            Text(
                                "850€",
                                color = Color(0xFF0066FF),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Button(
                            onClick = {
                                // ✅ You can later navigate to a reservation or reviews screen
                                // navController.navigate(Routes.Reviews)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0066FF)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Réserver", fontSize = 16.sp, color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
