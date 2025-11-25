package com.sim.darna.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sim.darna.components.PropertyCardView
import com.sim.darna.model.Property
import com.sim.darna.navigation.Routes
import com.sim.darna.repository.PropertyRepository
import com.sim.darna.ui.theme.AppTheme
import com.sim.darna.utils.FavoritesManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(navController: androidx.navigation.NavController) {
    val context = LocalContext.current
    val repository = PropertyRepository(context)
    
    var properties by remember { mutableStateOf<List<Property>>(emptyList()) }
    var favoriteIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        FavoritesManager.init(context)
        favoriteIds = FavoritesManager.favoritePropertyIds
        
        repository.getAllProperties().enqueue(object : retrofit2.Callback<List<Property>> {
            override fun onResponse(call: retrofit2.Call<List<Property>>, response: retrofit2.Response<List<Property>>) {
                if (response.isSuccessful && response.body() != null) {
                    val allProperties = response.body()!!
                    properties = allProperties.filter { favoriteIds.contains(it.id) }
                }
                isLoading = false
            }
            
            override fun onFailure(call: retrofit2.Call<List<Property>>, t: Throwable) {
                isLoading = false
            }
        })
    }
    
    val favoritesCount = properties.size

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

                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Mes Favoris",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                            Text(
                                text = if (favoritesCount > 0) "$favoritesCount annonces sauvegardées" else "Aucune annonce sauvegardée",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 13.sp
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .size(26.dp)
                        )
                    }
                }
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (properties.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.FavoriteBorder, null, modifier = Modifier.size(64.dp), tint = AppTheme.textSecondary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Aucun favori", fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Text("Vous n'avez pas encore de favoris", fontSize = 14.sp, color = AppTheme.textSecondary)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(properties) { property ->
                    PropertyCardView(
                        property = property,
                        onClick = {
                            navController.navigate("${Routes.PropertyDetail}/${property.id}")
                        }
                    )
                }
            }
        }
    }
}

