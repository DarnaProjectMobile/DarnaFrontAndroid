package com.sim.darna.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.ui.graphics.Color
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
import android.content.ClipData
import android.content.ClipboardManager
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PubliciteDetailScreen(
    publiciteId: String,
    onNavigateBack: () -> Unit,
    onEdit: (String) -> Unit = {},
    onScanQRCode: () -> Unit = {},
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
                actions = {}
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
                    // Image bannière avec badge de réduction pour type REDUCTION
                    if (!publicite.image.isNullOrEmpty() || !publicite.imageUrl.isNullOrEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth()) {
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
                            // Badge de réduction en haut à droite pour REDUCTION
                            if (publicite.type?.uppercase()?.trim() == "REDUCTION") {
                                publicite.detailReduction?.pourcentage?.let { pourcentage ->
                                    Card(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(0xFF2196F3)
                                        ),
                                        shape = RoundedCornerShape(20.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.LocalOffer,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "-$pourcentage%",
                                                color = Color.White,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                            // Badge "Jeu" en haut à droite pour type JEU
                            if (publicite.type?.uppercase()?.trim() == "JEU") {
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
                                        text = "Jeu",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Sponsor Card
                        if (publicite.sponsorName != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Icône circulaire du sponsor
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF2196F3)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (!publicite.sponsorLogo.isNullOrEmpty()) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(context)
                                                    .data(publicite.sponsorLogo)
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = "Logo sponsor",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Text(
                                                text = publicite.sponsorName?.take(1)?.uppercase() ?: "S",
                                                color = Color.White,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Text(
                                        text = publicite.sponsorName ?: "Sponsor",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF333333)
                                    )
                                }
                            }
                        }
                        
                        // Type spécifique
                        val publiciteType = publicite.type?.uppercase()?.trim()
                        android.util.Log.d("PubliciteDetailScreen", "Type de publicité: $publiciteType")
                        android.util.Log.d("PubliciteDetailScreen", "detailJeu: ${publicite.detailJeu}")
                        android.util.Log.d("PubliciteDetailScreen", "gains: ${publicite.detailJeu?.gains}")
                        android.util.Log.d("PubliciteDetailScreen", "gains size: ${publicite.detailJeu?.gains?.size}")
                        android.util.Log.d("PubliciteDetailScreen", "gains content: ${publicite.detailJeu?.gains?.joinToString(", ")}")
                        
                        when (publiciteType) {
                            "REDUCTION" -> {
                                publicite.detailReduction?.let { detail ->
                                    // Carte sponsor et description
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.White
                                        ),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(20.dp),
                                            verticalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            // Titre de la réduction
                                            Text(
                                                text = "Profitez de ${detail.pourcentage}% de réduction sur tous nos produits",
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF2196F3),
                                                fontSize = 20.sp
                                            )
                                            
                                            // Description
                                            Text(
                                                text = publicite.description.ifEmpty { 
                                                    "Offre exceptionnelle valable sur l'ensemble de notre catalogue. Profitez-en pour découvrir nos nouveautés et faire le plein de vos produits préférés à prix réduit."
                                                },
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color(0xFF666666),
                                                lineHeight = 20.sp
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    // Section QR Code et Code promo
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.White
                                        ),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(24.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(20.dp)
                                        ) {
                                            // Titre
                                            Text(
                                                text = "Scannez le QR Code ou copiez votre code promo",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = Color(0xFF333333),
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )
                                            
                                            // QR Code
                                            if (!publicite.qrCode.isNullOrEmpty()) {
                                                val bitmap = remember(publicite.qrCode) {
                                                    try {
                                                        val base64Data = if (publicite.qrCode.startsWith("data:image")) {
                                                            publicite.qrCode.substring(publicite.qrCode.indexOf(",") + 1)
                                                        } else {
                                                            publicite.qrCode
                                                        }
                                                        val imageBytes = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT)
                                                        android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                                    } catch (e: Exception) {
                                                        android.util.Log.e("PubliciteDetailScreen", "Erreur décodage QR: ${e.message}", e)
                                                        null
                                                    }
                                                }
                                                
                                                bitmap?.let {
                                                    Card(
                                                        modifier = Modifier.size(280.dp),
                                                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                                        shape = RoundedCornerShape(12.dp)
                                                    ) {
                                                        androidx.compose.foundation.Image(
                                                            bitmap = it.asImageBitmap(),
                                                            contentDescription = "QR Code",
                                                            modifier = Modifier.fillMaxSize()
                                                        )
                                                    }
                                                }
                                            } else {
                                                // Message si QR code non disponible
                                                Card(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = Color(0xFFFFF3E0)
                                                    ),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(16.dp),
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Text(
                                                            text = "QR Code en cours de génération",
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            color = Color(0xFFE65100),
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    }
                                                }
                                            }
                                            
                                            // Séparateur "ou"
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Divider(
                                                    modifier = Modifier.weight(1f),
                                                    color = Color(0xFFE0E0E0),
                                                    thickness = 1.dp
                                                )
                                                Text(
                                                    text = "ou",
                                                    modifier = Modifier.padding(horizontal = 16.dp),
                                                    color = Color(0xFF666666),
                                                    fontSize = 14.sp
                                                )
                                                Divider(
                                                    modifier = Modifier.weight(1f),
                                                    color = Color(0xFFE0E0E0),
                                                    thickness = 1.dp
                                                )
                                            }
                                            
                                            // Code promo avec bouton copier
                                            if (!publicite.coupon.isNullOrEmpty()) {
                                                Column(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Text(
                                                        text = "Code promo",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = Color(0xFF333333),
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                    
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(Color(0xFFE3F2FD))
                                                            .padding(16.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Text(
                                                            text = publicite.coupon,
                                                            style = MaterialTheme.typography.titleMedium,
                                                            color = Color(0xFF2196F3),
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                        
                                                        IconButton(
                                                            onClick = {
                                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                                val clip = ClipData.newPlainText("Code promo", publicite.coupon)
                                                                clipboard.setPrimaryClip(clip)
                                                                Toast.makeText(context, "Code promo copié !", Toast.LENGTH_SHORT).show()
                                                            },
                                                            modifier = Modifier.size(40.dp)
                                                        ) {
                                                            Icon(
                                                                Icons.Default.ContentCopy,
                                                                contentDescription = "Copier",
                                                                tint = Color(0xFF2196F3),
                                                                modifier = Modifier.size(24.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                            
                                            // Message d'instruction
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = Color(0xFF2196F3)
                                                ),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    text = "Présentez ce code à la caisse pour bénéficier de votre réduction de ${detail.pourcentage}%",
                                                    modifier = Modifier.padding(16.dp),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = Color.White,
                                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                                    lineHeight = 20.sp
                                                )
                                            }
                                            
                                            // Bouton "Utiliser cette offre"
                                            Button(
                                                onClick = onScanQRCode,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(56.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color(0xFF2196F3)
                                                ),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Text(
                                                    text = "Utiliser cette offre",
                                                    color = Color.White,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            
                            "PROMOTION" -> {
                                publicite.detailPromotion?.let { detail ->
                                    // Carte principale (style similaire à la maquette Pizza)
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(20.dp),
                                            verticalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            // Titre de l'offre
                                            Text(
                                                text = detail.offre?.ifBlank { publicite.titre } ?: publicite.titre,
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF333333)
                                            )

                                            Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                                            // Ligne catégorie
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.LocalOffer,
                                                    contentDescription = null,
                                                    tint = Color(0xFF2196F3)
                                                )
                                                Column {
                                                    Text(
                                                        text = "Catégorie",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = Color(0xFF666666)
                                                    )
                                                    Text(
                                                        text = publicite.categorie ?: "Catégorie",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = Color(0xFF333333)
                                                    )
                                                }
                                            }

                                            // Ligne expiration
                                            if (!publicite.dateExpiration.isNullOrEmpty()) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Schedule,
                                                        contentDescription = null,
                                                        tint = Color(0xFFFF9800)
                                                    )
                                                    Column {
                                                        Text(
                                                            text = "Date d'expiration",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = Color(0xFF666666)
                                                        )
                                                        Text(
                                                            text = publicite.dateExpiration,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            color = Color(0xFF333333)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Bloc "L'offre" (texte détaillé)
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(20.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Text(
                                                text = "L'offre",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF333333)
                                            )
                                            Text(
                                                text = publicite.description.ifBlank { detail.conditions ?: "" },
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color(0xFF666666),
                                                lineHeight = 20.sp
                                            )
                                        }
                                    }
                                }
                            }
                            
                            "JEU" -> {
                                var gameResult by remember { mutableStateOf<String?>(null) }
                                var hasPlayed by remember { mutableStateOf(false) }
                                var wonGain by remember { mutableStateOf<String?>(null) }
                                
                                // Vérifier si l'utilisateur a déjà joué (stockage local)
                                LaunchedEffect(publiciteId) {
                                    if (publiciteId.isNotBlank()) {
                                        val prefs = context.getSharedPreferences("GAME_PLAYS", Context.MODE_PRIVATE)
                                        val hasPlayedKey = "has_played_$publiciteId"
                                        hasPlayed = prefs.getBoolean(hasPlayedKey, false)
                                    }
                                }
                                
                                // Section Description du jeu
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text(
                                            text = "Tentez votre chance !",
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2196F3),
                                            fontSize = 22.sp
                                        )
                                        
                                        if (publicite.detailJeu?.description != null) {
                                            Text(
                                                text = publicite.detailJeu!!.description!!,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color(0xFF666666),
                                                lineHeight = 20.sp
                                            )
                                        } else {
                                            Text(
                                                text = "Tournez la roue et gagnez des prix incroyables. Un tour gratuit par jour pour tous nos clients fidèles.",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color(0xFF666666),
                                                lineHeight = 20.sp
                                            )
                                        }
                                    }
                                }
                                
                                // Section Roulette
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Tournez la roue !",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2196F3),
                                            fontSize = 20.sp
                                        )
                                        
                                        Text(
                                            text = "Cliquez sur le bouton pour tenter votre chance",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFF666666)
                                        )
                                        
                                        // Afficher la roulette - utiliser les gains de detailJeu ou des gains par défaut
                                        val gainsFromApi = publicite.detailJeu?.gains
                                        android.util.Log.d("PubliciteDetailScreen", "Gains bruts de l'API: $gainsFromApi")
                                        
                                        val gains = if (!gainsFromApi.isNullOrEmpty()) {
                                            val filtered = gainsFromApi.filter { it.isNotBlank() }
                                            if (filtered.isNotEmpty()) {
                                                filtered
                                            } else {
                                                listOf("Gain 1", "Gain 2", "Gain 3", "Gain 4", "Gain 5", "Gain 6")
                                            }
                                        } else {
                                            listOf("Gain 1", "Gain 2", "Gain 3", "Gain 4", "Gain 5", "Gain 6")
                                        }
                                        
                                        android.util.Log.d("PubliciteDetailScreen", "Gains finaux pour la roulette: $gains")
                                        
                                        RouletteWheel(
                                            items = gains,
                                            onSpinComplete = { gain ->
                                                android.util.Log.d("PubliciteDetailScreen", "Gain sélectionné: $gain")
                                                // Vérifier si le gain est "rien" (insensible à la casse)
                                                val isRien = gain.isNotBlank() && 
                                                    gain.trim().lowercase() == "rien"
                                                
                                                // Si c'est "rien", c'est une perte, sinon c'est un gain
                                                val isWin = gain.isNotBlank() && !isRien
                                                gameResult = if (isWin) "win" else "lose"
                                                wonGain = if (isWin) gain.trim() else null
                                                
                                                // Enregistrer dans SharedPreferences que l'utilisateur a joué
                                                val prefs = context.getSharedPreferences("GAME_PLAYS", Context.MODE_PRIVATE)
                                                val hasPlayedKey = "has_played_$publiciteId"
                                                prefs.edit().putBoolean(hasPlayedKey, true).apply()
                                                hasPlayed = true
                                                android.util.Log.d("PubliciteDetailScreen", "Partie enregistrée localement")
                                            },
                                            enabled = !hasPlayed
                                        )
                                        
                                        // Afficher un message si l'utilisateur a déjà joué
                                        if (hasPlayed) {
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = Color(0xFFFFF3E0)
                                                ),
                                                shape = RoundedCornerShape(12.dp),
                                                border = androidx.compose.foundation.BorderStroke(
                                                    1.dp,
                                                    Color(0xFFFF9800)
                                                )
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(16.dp),
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Info,
                                                        contentDescription = null,
                                                        tint = Color(0xFFFF9800),
                                                        modifier = Modifier.size(32.dp)
                                                    )
                                                    Text(
                                                        text = "Vous avez déjà joué à ce jeu. Une seule partie autorisée par utilisateur.",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Medium,
                                                        color = Color(0xFFE65100)
                                                    )
                                                }
                                            }
                                        }
                                        
                                        // Afficher le résultat
                                        gameResult?.let { result ->
                                            Spacer(modifier = Modifier.height(16.dp))
                                            if (result == "win") {
                                                Card(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = Color(0xFFE8F5E9)
                                                    ),
                                                    shape = RoundedCornerShape(12.dp),
                                                    border = androidx.compose.foundation.BorderStroke(
                                                        1.dp,
                                                        Color(0xFF4CAF50)
                                                    )
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(16.dp),
                                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Star,
                                                            contentDescription = null,
                                                            tint = Color(0xFF4CAF50),
                                                            modifier = Modifier.size(32.dp)
                                                        )
                                                        Text(
                                                            text = if (wonGain != null) {
                                                                "Félicitations ! Vous avez gagné une réduction de $wonGain !"
                                                            } else {
                                                                "Félicitations ! Vous avez gagné !"
                                                            },
                                                            style = MaterialTheme.typography.titleMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFF2E7D32)
                                                        )
                                                    }
                                                }
                                            } else {
                                                Card(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = Color(0xFFFFEBEE)
                                                    ),
                                                    shape = RoundedCornerShape(12.dp),
                                                    border = androidx.compose.foundation.BorderStroke(
                                                        1.dp,
                                                        Color(0xFFF44336)
                                                    )
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(16.dp),
                                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Cancel,
                                                            contentDescription = null,
                                                            tint = Color(0xFFF44336),
                                                            modifier = Modifier.size(32.dp)
                                                        )
                                                        Text(
                                                            text = "Malheureusement, vous n'avez pas gagné.",
                                                            style = MaterialTheme.typography.titleMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFFC62828)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Informations supplémentaires (style tag + calendrier)
                        if (!publicite.categorie.isNullOrEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.LocalOffer,
                                    contentDescription = null,
                                    tint = Color(0xFF2196F3)
                                )
                                Column {
                                    Text(
                                        text = "Catégorie",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF666666)
                                    )
                                    Text(
                                        text = publicite.categorie ?: "",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF333333)
                                    )
                                }
                            }
                        }

                        if (!publicite.dateExpiration.isNullOrEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = Color(0xFFFF9800)
                                )
                                Column {
                                    Text(
                                        text = "Date d'expiration",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF666666)
                                    )
                                    Text(
                                        text = publicite.dateExpiration,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF333333)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

