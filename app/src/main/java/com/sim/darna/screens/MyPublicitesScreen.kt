package com.sim.darna.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.sim.darna.auth.TokenStorage
import com.sim.darna.data.model.Publicite
import com.sim.darna.ui.theme.AppTheme
import com.sim.darna.viewmodel.PubliciteViewModel
import com.sim.darna.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPublicitesScreen(
    navController: NavHostController,
    viewModel: PubliciteViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val listState by viewModel.listState.collectAsState()
    
    // Récupérer l'ID utilisateur depuis SharedPreferences
    val prefs = remember { context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE) }
    val currentUserId = remember(prefs) { 
        prefs.getString("user_id", null) ?: TokenStorage.getUserId(context)
    }
    
    LaunchedEffect(Unit) {
        viewModel.loadPublicites()
    }
    
    // Extraire les publicités depuis l'état
    val allPublicites = remember(listState) {
        when (val state = listState) {
            is UiState.Success<*> -> {
                when (val data = state.data) {
                    is List<*> -> data.filterIsInstance<Publicite>()
                    is Publicite -> listOf(data)
                    else -> emptyList()
                }
            }
            else -> emptyList()
        }
    }
    
    // Filtrer uniquement les publicités du sponsor connecté
    val myPublicites = remember(allPublicites, currentUserId) {
        allPublicites.filter { pub ->
            pub.sponsorId == currentUserId || pub.sponsor?._id == currentUserId
        }
    }
    
    Scaffold(
        topBar = {
            Column {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(shadowElevation = 6.dp) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        AppTheme.primary,
                                        AppTheme.primary.copy(alpha = 0.9f)
                                    )
                                )
                            )
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        IconButton(
                            onClick = { navController.popBackStack() },
                            colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White),
                            modifier = Modifier.align(Alignment.CenterStart)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                        }
                        
                        Text(
                            "Mes Publicités",
                            modifier = Modifier.align(Alignment.Center),
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        when (listState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF2196F3))
                }
            }
            is UiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Erreur de chargement",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            }
            is UiState.Success<*> -> {
                if (myPublicites.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Default.Campaign,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color(0xFFB0BEC5)
                            )
                            Text(
                                text = "Aucune publicité créée",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(myPublicites) { publicite ->
                            PubliciteCard(
                                publicite = publicite,
                                onClick = {
                                    publicite._id?.let { id ->
                                        navController.navigate("publicite_detail/$id")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PubliciteCard(
    publicite: Publicite,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(Color.White)
    ) {
        Column {
            // Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                AsyncImage(
                    model = publicite.image ?: publicite.imageUrl,
                    contentDescription = publicite.titre,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Tag de type de publicité en overlay (coin supérieur droit)
                if (!publicite.type.isNullOrEmpty()) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF2196F3)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = publicite.type.uppercase(),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            // Contenu
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = publicite.titre,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = publicite.description,
                    fontSize = 14.sp,
                    color = Color(0xFF757575),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (!publicite.categorie.isNullOrEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE3F2FD)
                    ) {
                        Text(
                            text = publicite.categorie,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 12.sp,
                            color = Color(0xFF2196F3),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

