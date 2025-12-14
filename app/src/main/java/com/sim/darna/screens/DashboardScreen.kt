package com.sim.darna.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.sim.darna.factory.VisiteVmFactory
import com.sim.darna.visite.VisiteViewModel
import com.sim.darna.visite.VisiteResponse

@Composable
fun DashboardScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
    val baseUrl = "http://192.168.1.101:3009/" // Same as LoginScreen
    
    val viewModel: VisiteViewModel = viewModel(
        factory = VisiteVmFactory(baseUrl, context)
    )
    
    val uiState by viewModel.state.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadLogementsVisites(force = true)
    }
    
    // Calculate statistics
    val pendingCount = uiState.visites.count { 
        it.status?.equals("pending", ignoreCase = true) == true 
    }
    val acceptedCount = uiState.visites.count { 
        it.status?.equals("confirmed", ignoreCase = true) == true ||
        it.status?.equals("acceptée", ignoreCase = true) == true
    }
    val waitingCount = uiState.visites.count { 
        it.status?.equals("en attente", ignoreCase = true) == true ||
        (it.status == null)
    }
    val reviewedCount = uiState.visites.count { 
        it.reviewId != null
    }
    
    val totalVisites = uiState.visites.size
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFF5F9FF), Color.White)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))
            
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Tableau de bord",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1A1A1A)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Gérez vos logements et visites",
                        fontSize = 14.sp,
                        color = Color(0xFF757575)
                    )
                }
                
                IconButton(
                    onClick = { navController.navigate(com.sim.darna.navigation.Routes.Notifications) },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE3F2FD))
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color(0xFF2196F3)
                    )
                }
            }
            
            Spacer(Modifier.height(32.dp))
            
            // Statistics Cards Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    count = pendingCount,
                    label = "Demandes reçues",
                    icon = Icons.Default.Mail,
                    iconColor = Color(0xFF2196F3),
                    backgroundColor = Color(0xFFE3F2FD)
                )
                
                StatCard(
                    modifier = Modifier.weight(1f),
                    count = acceptedCount,
                    label = "Acceptées",
                    icon = Icons.Default.CheckCircle,
                    iconColor = Color(0xFF4CAF50),
                    backgroundColor = Color(0xFFE8F5E9)
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    count = waitingCount,
                    label = "En attente",
                    icon = Icons.Default.Schedule,
                    iconColor = Color(0xFFFFA726),
                    backgroundColor = Color(0xFFFFF3E0)
                )
                
                StatCard(
                    modifier = Modifier.weight(1f),
                    count = reviewedCount,
                    label = "Avis reçus",
                    icon = Icons.Default.Star,
                    iconColor = Color(0xFF2196F3),
                    backgroundColor = Color(0xFFE3F2FD)
                )
            }
            
            Spacer(Modifier.height(32.dp))
            
            // Visit Distribution Chart
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        "Répartition des visites",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    
                    Spacer(Modifier.height(24.dp))
                    
                    VisitDistributionRow(
                        label = "En attente",
                        count = waitingCount,
                        total = totalVisites,
                        color = Color(0xFFFFA726)
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    VisitDistributionRow(
                        label = "Acceptées",
                        count = acceptedCount,
                        total = totalVisites,
                        color = Color(0xFF4CAF50)
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    VisitDistributionRow(
                        label = "Terminées",
                        count = reviewedCount,
                        total = totalVisites,
                        color = Color(0xFF2196F3)
                    )
                }
            }
            
            Spacer(Modifier.height(32.dp))
            
            // Recent Reviews Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Avis récents",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                        
                        TextButton(onClick = { /* View all */ }) {
                            Text(
                                "Voir tout",
                                color = Color(0xFF2196F3),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    if (reviewedCount == 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.RateReview,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = Color(0xFFBDBDBD)
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    "Aucun avis pour le moment",
                                    color = Color(0xFF757575),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        Text(
                            "Vous avez reçu $reviewedCount avis",
                            color = Color(0xFF757575),
                            fontSize = 14.sp
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    count: Int,
    label: String,
    icon: ImageVector,
    iconColor: Color,
    backgroundColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                text = count.toString(),
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1A1A1A)
            )
            
            Spacer(Modifier.height(4.dp))
            
            Text(
                text = label,
                fontSize = 14.sp,
                color = Color(0xFF757575)
            )
        }
    }
}

@Composable
private fun VisitDistributionRow(
    label: String,
    count: Int,
    total: Int,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                fontSize = 14.sp,
                color = Color(0xFF1A1A1A),
                fontWeight = FontWeight.Medium
            )
            
            Text(
                "$count ($total)",
                fontSize = 14.sp,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(Modifier.height(8.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFF5F5F5))
        ) {
            val progress = if (total > 0) count.toFloat() / total.toFloat() else 0f
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}
