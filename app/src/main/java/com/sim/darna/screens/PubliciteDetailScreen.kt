package com.sim.darna.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sim.darna.auth.TokenStorage
import com.sim.darna.auth.UserSessionManager
import com.sim.darna.components.QRCodeDisplay
import com.sim.darna.components.RouletteWheel
import com.sim.darna.data.model.Publicite
import com.sim.darna.viewmodel.PubliciteViewModel
import com.sim.darna.viewmodel.UiState
import android.content.Context
import android.content.SharedPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PubliciteDetailScreen(
    publiciteId: String,
    onNavigateBack: () -> Unit,
    onEdit: (String) -> Unit = {},
    viewModel: PubliciteViewModel = hiltViewModel()
) {
    val detailState by viewModel.detailState.collectAsState()
    val context = LocalContext.current
    
    // Récupérer le rôle et l'ID utilisateur depuis SharedPreferences
    val prefs = remember { context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE) }
    val userRole = remember(prefs) { prefs.getString("role", "user") ?: "user" }
    val currentUserId = remember(prefs) { 
        prefs.getString("user_id", null) ?: TokenStorage.getUserId(context)
    }
    val isSponsor = remember(userRole) { 
        userRole.lowercase() == "sponsor" || UserSessionManager.isSponsor()
    }
    
    LaunchedEffect(publiciteId) {
        viewModel.loadPubliciteDetail(publiciteId)
    }
    
    val publicite = when (val state = detailState) {
        is UiState.Success -> state.data as? Publicite
        else -> null
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détails de la publicité") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    if (isSponsor && !currentUserId.isNullOrEmpty() && 
                        (publicite?.sponsorId == currentUserId || publicite?.sponsor?._id == currentUserId)) {
                        IconButton(onClick = { publicite?._id?.let(onEdit) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Modifier")
                        }
                    }
                }
            )
        }
    ) { padding ->
        when {
            detailState is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            publicite == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Publicité non trouvée")
                }
            }
            
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Image
                    if (!publicite.image.isNullOrEmpty() || !publicite.imageUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(publicite.image ?: publicite.imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = publicite.titre,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                    
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Titre
                        Text(
                            text = publicite.titre,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        // Sponsor
                        if (publicite.sponsorName != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (!publicite.sponsorLogo.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(publicite.sponsorLogo)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Logo sponsor",
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Text(
                                    text = "Par ${publicite.sponsorName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        // Description
                        Text(
                            text = publicite.description,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        
                        // Type spécifique
                        when (publicite.type?.uppercase()) {
                            "REDUCTION" -> {
                                publicite.detailReduction?.let { detail ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Text(
                                                text = "Réduction de ${detail.pourcentage}%",
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold
                                            )
                                            detail.conditionsUtilisation?.let {
                                                Text(
                                                    text = "Conditions: $it",
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                            if (!publicite.qrCode.isNullOrEmpty()) {
                                                QRCodeDisplay(
                                                    qrCodeBase64 = publicite.qrCode,
                                                    couponCode = publicite.coupon
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            
                            "PROMOTION" -> {
                                publicite.detailPromotion?.let { detail ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Text(
                                                text = "Offre spéciale",
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold
                                            )
                                            detail.offre?.let {
                                                Text(
                                                    text = it,
                                                    style = MaterialTheme.typography.bodyLarge
                                                )
                                            }
                                            detail.conditions?.let {
                                                Text(
                                                    text = "Conditions: $it",
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            
                            "JEU" -> {
                                publicite.detailJeu?.let { detail ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            Text(
                                                text = "Jeu de la roulette",
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold
                                            )
                                            detail.description?.let {
                                                Text(
                                                    text = it,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                            if (!detail.gains.isNullOrEmpty()) {
                                                RouletteWheel(
                                                    items = detail.gains,
                                                    onSpinComplete = { gain ->
                                                        // Afficher le résultat
                                                    },
                                                    enabled = true // TODO: Vérifier si l'utilisateur a déjà joué
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Informations supplémentaires
                        if (!publicite.dateExpiration.isNullOrEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Expire le ${publicite.dateExpiration}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        if (!publicite.categorie.isNullOrEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Category,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Catégorie: ${publicite.categorie}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

