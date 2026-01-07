package com.sim.darna.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sim.darna.auth.TokenStorage
import com.sim.darna.auth.UserSessionManager
import com.sim.darna.components.RouletteWheel
import com.sim.darna.components.WinAnimationLottie
import com.sim.darna.data.model.Publicite
import com.sim.darna.utils.SoundManager
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
                title = { 
                    Text(
                        "Détails de la publicité",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack, 
                            contentDescription = "Retour",
                            tint = Color(0xFF2196F3)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1A1A1A)
                )
            )
        },
        containerColor = Color(0xFFF5F7FA)
    ) { padding ->
        when {
            detailState is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(Color(0xFFF5F7FA)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = Color(0xFF2196F3),
                            strokeWidth = 4.dp
                        )
                        Text(
                            "Chargement...",
                            fontSize = 16.sp,
                            color = Color(0xFF666666),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            publicite == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(Color(0xFFF5F7FA)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = Color(0xFFF44336),
                                modifier = Modifier.size(64.dp)
                            )
                            Text(
                                "Publicité non trouvée",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1A1A)
                            )
                            Text(
                                "La publicité demandée n'existe pas ou a été supprimée",
                                fontSize = 14.sp,
                                color = Color(0xFF666666),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
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
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    elevation = 8.dp,
                                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                                    spotColor = Color.Black.copy(alpha = 0.2f)
                                )
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(publicite.image ?: publicite.imageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = publicite.titre,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(280.dp)
                                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
                                contentScale = ContentScale.Crop
                            )
                            
                            // Overlay gradient en bas pour meilleure lisibilité
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(280.dp)
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.3f)
                                            ),
                                            startY = 200f
                                        )
                                    )
                            )
                            
                            // Badge de réduction en haut à droite pour REDUCTION
                            if (publicite.type?.uppercase()?.trim() == "REDUCTION") {
                                publicite.detailReduction?.pourcentage?.let { pourcentage ->
                                    Card(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(16.dp)
                                            .shadow(
                                                elevation = 8.dp,
                                                shape = RoundedCornerShape(24.dp),
                                                spotColor = Color(0xFF2196F3).copy(alpha = 0.4f)
                                            ),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(0xFF2196F3)
                                        ),
                                        shape = RoundedCornerShape(24.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.LocalOffer,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = "-$pourcentage%",
                                                color = Color.White,
                                                fontSize = 16.sp,
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
                                        .padding(16.dp)
                                        .shadow(
                                            elevation = 8.dp,
                                            shape = RoundedCornerShape(20.dp),
                                            spotColor = Color(0xFF2196F3).copy(alpha = 0.4f)
                                        ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF2196F3)
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Text(
                                        text = "🎮 Jeu",
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Sponsor Card avec design amélioré
                        if (publicite.sponsorName != null) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(
                                        elevation = 6.dp,
                                        shape = RoundedCornerShape(20.dp),
                                        spotColor = Color.Black.copy(alpha = 0.1f)
                                    ),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White
                                ),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // Icône circulaire du sponsor avec ombre
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .shadow(
                                                elevation = 4.dp,
                                                shape = CircleShape,
                                                spotColor = Color(0xFF2196F3).copy(alpha = 0.3f)
                                            )
                                            .clip(CircleShape)
                                            .background(
                                                brush = Brush.radialGradient(
                                                    colors = listOf(
                                                        Color(0xFF2196F3),
                                                        Color(0xFF1976D2)
                                                    )
                                                )
                                            ),
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
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "Par",
                                            fontSize = 12.sp,
                                            color = Color(0xFF999999),
                                            fontWeight = FontWeight.Normal
                                        )
                                        Text(
                                            text = publicite.sponsorName ?: "Sponsor",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1A1A1A),
                                            fontSize = 18.sp
                                        )
                                    }
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
                                    // Carte sponsor et description avec design amélioré
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .shadow(
                                                elevation = 6.dp,
                                                shape = RoundedCornerShape(20.dp),
                                                spotColor = Color.Black.copy(alpha = 0.1f)
                                            ),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(20.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(24.dp),
                                            verticalArrangement = Arrangement.spacedBy(20.dp)
                                        ) {
                                            // Titre de la réduction avec icône
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(48.dp)
                                                        .background(
                                                            brush = Brush.radialGradient(
                                                                colors = listOf(
                                                                    Color(0xFF2196F3).copy(alpha = 0.2f),
                                                                    Color(0xFF2196F3).copy(alpha = 0.1f)
                                                                )
                                                            ),
                                                            shape = CircleShape
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        Icons.Default.LocalOffer,
                                                        contentDescription = null,
                                                        tint = Color(0xFF2196F3),
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                                Text(
                                                    text = "Profitez de ${detail.pourcentage}% de réduction",
                                                    style = MaterialTheme.typography.headlineSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF1A1A1A),
                                                    fontSize = 22.sp
                                                )
                                            }
                                            
                                            // Date d'expiration (si disponible)
                                            if (!publicite.dateExpiration.isNullOrEmpty()) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Schedule,
                                                        contentDescription = null,
                                                        tint = Color(0xFFFF9800),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Text(
                                                        text = "Expire le ${publicite.dateExpiration}",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = Color(0xFF666666),
                                                        fontSize = 14.sp
                                                    )
                                                }
                                            }
                                            
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
                                            
                                            // Bouton "Utiliser cette offre" (visible seulement pour les sponsors)
                                            if (isSponsor) {
                                                Button(
                                                    onClick = onScanQRCode,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(56.dp)
                                                        .shadow(
                                                            elevation = 8.dp,
                                                            shape = RoundedCornerShape(16.dp),
                                                            spotColor = Color(0xFF2196F3).copy(alpha = 0.4f)
                                                        ),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = Color(0xFF2196F3)
                                                    ),
                                                    shape = RoundedCornerShape(16.dp),
                                                    elevation = ButtonDefaults.buttonElevation(
                                                        defaultElevation = 0.dp,
                                                        pressedElevation = 4.dp
                                                    )
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                    ) {
                                                        Icon(
                                                            Icons.Outlined.QrCodeScanner,
                                                            contentDescription = null,
                                                            tint = Color.White,
                                                            modifier = Modifier.size(22.dp)
                                                        )
                                                        Text(
                                                            text = "Utiliser cette offre",
                                                            color = Color.White,
                                                            fontSize = 17.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
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

                                            // Date d'expiration (si disponible)
                                            if (!publicite.dateExpiration.isNullOrEmpty()) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Schedule,
                                                        contentDescription = null,
                                                        tint = Color(0xFFFF9800),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Text(
                                                        text = "Expire le ${publicite.dateExpiration}",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = Color(0xFF666666),
                                                        fontSize = 14.sp
                                                    )
                                                }
                                            }

                                            Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Bloc "L'offre" (texte détaillé)
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                                val soundManager = remember { SoundManager.getInstance(context) }
                                
                                var gameResult by remember { mutableStateOf<String?>(null) }
                                var hasPlayed by remember { mutableStateOf(false) }
                                var wonGain by remember { mutableStateOf<String?>(null) }
                                var showWinAnimation by remember { mutableStateOf(false) }
                                var showResultDialog by remember { mutableStateOf(false) }
                                
                                // Réinitialiser le résultat quand on change de publicité (pour éviter les anciennes valeurs)
                                LaunchedEffect(publiciteId) {
                                    gameResult = null
                                    wonGain = null
                                    showResultDialog = false
                                    showWinAnimation = false
                                    android.util.Log.d("PubliciteDetailScreen", "Réinitialisation gameResult et wonGain pour publiciteId: $publiciteId")
                                }
                                
                                // Jouer le son approprié et afficher l'animation quand le résultat change
                                LaunchedEffect(gameResult) {
                                    gameResult?.let { result ->
                                        when (result) {
                                            "win" -> {
                                                soundManager.playSound(SoundManager.SoundType.WIN)
                                                showWinAnimation = true
                                                android.util.Log.d("PubliciteDetailScreen", "Son de victoire joué, animation affichée")
                                                // Afficher le dialog après l'animation
                                                kotlinx.coroutines.delay(2000) // Attendre la fin de l'animation
                                                showResultDialog = true
                                            }
                                            "lose" -> {
                                                soundManager.playSound(SoundManager.SoundType.LOSE)
                                                android.util.Log.d("PubliciteDetailScreen", "Son de défaite joué")
                                                // Afficher le dialog immédiatement pour la perte
                                                showResultDialog = true
                                            }
                                        }
                                    }
                                }
                                
                                // Dialog avec animation de victoire
                                if (showWinAnimation) {
                                    Dialog(
                                        onDismissRequest = { showWinAnimation = false },
                                        properties = DialogProperties(
                                            dismissOnBackPress = false,
                                            dismissOnClickOutside = false
                                        )
                                    ) {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = Color.Transparent
                                            ),
                                        ) {
                                            WinAnimationLottie(
                                                assetFileName = "win_animation.json",
                                                onAnimationEnd = { 
                                                    showWinAnimation = false 
                                                },
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                                
                                // Dialog avec message de résultat (gain ou perte)
                                if (showResultDialog && gameResult != null) {
                                    Dialog(
                                        onDismissRequest = { showResultDialog = false },
                                        properties = DialogProperties(
                                            dismissOnBackPress = true,
                                            dismissOnClickOutside = true
                                        )
                                    ) {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = Color.White
                                            ),
                                            shape = RoundedCornerShape(20.dp),
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(24.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(16.dp)
                                            ) {
                                                // Icône
                                                if (gameResult == "win") {
                                                    Icon(
                                                        imageVector = Icons.Default.Star,
                                                        contentDescription = null,
                                                        tint = Color(0xFF4CAF50),
                                                        modifier = Modifier.size(64.dp)
                                                    )
                                                } else {
                                                    Icon(
                                                        imageVector = Icons.Default.Cancel,
                                                        contentDescription = null,
                                                        tint = Color(0xFFF44336),
                                                        modifier = Modifier.size(64.dp)
                                                    )
                                                }
                                                
                                                // Message
                                                val message = if (gameResult == "win") {
                                                    wonGain?.let { gain ->
                                                        val gainTrimmed = gain.trim()
                                                        val gainLower = gainTrimmed.lowercase()
                                                        when {
                                                            gainTrimmed.contains("%", ignoreCase = true) -> {
                                                                "Félicitations ! Vous avez gagné une réduction de $gainTrimmed !"
                                                            }
                                                            gainLower == "smartphone" || gainLower.startsWith("smartphone") -> {
                                                                "Félicitations ! Vous avez gagné un smartphone !"
                                                            }
                                                            gainLower == "tablette" || gainLower.startsWith("tablette") -> {
                                                                "Félicitations ! Vous avez gagné une tablette !"
                                                            }
                                                            gainLower == "laptop" || gainLower == "ordinateur" || 
                                                            gainLower.startsWith("laptop") || gainLower.startsWith("ordinateur") -> {
                                                                "Félicitations ! Vous avez gagné un ordinateur !"
                                                            }
                                                            else -> {
                                                                "Félicitations ! Vous avez gagné $gainTrimmed !"
                                                            }
                                                        }
                                                    } ?: "Félicitations ! Vous avez gagné !"
                                                } else {
                                                    "Malheureusement, vous n'avez pas gagné."
                                                }
                                                
                                                Text(
                                                    text = message,
                                                    style = MaterialTheme.typography.titleLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (gameResult == "win") Color(0xFF2E7D32) else Color(0xFFC62828),
                                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                                )
                                                
                                                // Bouton OK
                                                Button(
                                                    onClick = { showResultDialog = false },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(48.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = if (gameResult == "win") Color(0xFF4CAF50) else Color(0xFFF44336)
                                                    ),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Text(
                                                        text = "OK",
                                                        color = Color.White,
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                
                                // Vérifier si l'utilisateur a déjà joué (stockage local)
                                // Seulement pour les clients et collocators, pas pour les sponsors
                                // La clé inclut l'ID utilisateur pour que chaque compte ait son propre enregistrement
                                LaunchedEffect(publiciteId, isSponsor, currentUserId) {
                                    if (publiciteId.isNotBlank() && !isSponsor && !currentUserId.isNullOrBlank()) {
                                        val prefs = context.getSharedPreferences("GAME_PLAYS", Context.MODE_PRIVATE)
                                        val hasPlayedKey = "has_played_${publiciteId}_${currentUserId}"
                                        hasPlayed = prefs.getBoolean(hasPlayedKey, false)
                                        android.util.Log.d("PubliciteDetailScreen", "Vérification jeu pour userId: $currentUserId, clé: $hasPlayedKey, a joué: $hasPlayed")
                                    } else {
                                        // Les sponsors peuvent toujours jouer
                                        hasPlayed = false
                                    }
                                }
                                
                                // Section Description du jeu
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White
                                    ),
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
                                        
                                        // Date d'expiration (si disponible)
                                        if (!publicite.dateExpiration.isNullOrEmpty()) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Schedule,
                                                    contentDescription = null,
                                                    tint = Color(0xFFFF9800),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = "Expire le ${publicite.dateExpiration}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = Color(0xFF666666),
                                                    fontSize = 14.sp
                                                )
                                            }
                                        }
                                        
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
                                            // Nettoyer les gains : trim et filtrer les vides
                                            val filtered = gainsFromApi
                                                .map { it.trim() }
                                                .filter { it.isNotBlank() }
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
                                                android.util.Log.d("PubliciteDetailScreen", "=== DÉBUT onSpinComplete ===")
                                                android.util.Log.d("PubliciteDetailScreen", "Gain reçu de la roue: '$gain'")
                                                
                                                // Stocker le gain réel (même si c'est "rien") pour personnaliser le message
                                                val gainTrimmed = gain.trim()
                                                android.util.Log.d("PubliciteDetailScreen", "Gain après trim: '$gainTrimmed'")
                                                
                                                // Vérifier si le gain est "rien" (insensible à la casse)
                                                val isRien = gainTrimmed.isNotBlank() && 
                                                    gainTrimmed.lowercase() == "rien"
                                                
                                                // Si c'est "rien", c'est une perte, sinon c'est un gain
                                                val isWin = gainTrimmed.isNotBlank() && !isRien
                                                
                                                // IMPORTANT: Mettre à jour les états avec le nouveau gain
                                                gameResult = if (isWin) "win" else "lose"
                                                wonGain = gainTrimmed // Toujours stocker le gain pour personnaliser le message
                                                
                                                android.util.Log.d("PubliciteDetailScreen", "gameResult mis à jour: $gameResult")
                                                android.util.Log.d("PubliciteDetailScreen", "wonGain mis à jour: '$wonGain'")
                                                android.util.Log.d("PubliciteDetailScreen", "isWin: $isWin")
                                                
                                                // Enregistrer dans SharedPreferences que l'utilisateur a joué
                                                // Seulement pour les clients et collocators, pas pour les sponsors
                                                // La clé inclut l'ID utilisateur pour que chaque compte ait son propre enregistrement
                                                if (!isSponsor && !currentUserId.isNullOrBlank()) {
                                                    val prefs = context.getSharedPreferences("GAME_PLAYS", Context.MODE_PRIVATE)
                                                    val hasPlayedKey = "has_played_${publiciteId}_${currentUserId}"
                                                    prefs.edit().putBoolean(hasPlayedKey, true).apply()
                                                    hasPlayed = true
                                                    android.util.Log.d("PubliciteDetailScreen", "Partie enregistrée localement (client/collocator) pour userId: $currentUserId, clé: $hasPlayedKey")
                                                } else {
                                                    android.util.Log.d("PubliciteDetailScreen", "Partie jouée par sponsor (non enregistrée)")
                                                }
                                                
                                                android.util.Log.d("PubliciteDetailScreen", "=== FIN onSpinComplete ===")
                                            },
                                            enabled = !hasPlayed || isSponsor
                                        )
                                        
                                        // Afficher un message si l'utilisateur a déjà joué (seulement pour clients et collocators)
                                        // Le message s'affiche dès que l'utilisateur arrive sur la page s'il a déjà joué
                                        if (hasPlayed && !isSponsor) {
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .shadow(
                                                        elevation = 4.dp,
                                                        shape = RoundedCornerShape(16.dp),
                                                        spotColor = Color(0xFFFF9800).copy(alpha = 0.2f)
                                                    ),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = Color(0xFFFFF3E0)
                                                ),
                                                shape = RoundedCornerShape(16.dp),
                                                border = androidx.compose.foundation.BorderStroke(
                                                    2.dp,
                                                    Color(0xFFFF9800)
                                                )
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(20.dp),
                                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(48.dp)
                                                            .background(
                                                                brush = Brush.radialGradient(
                                                                    colors = listOf(
                                                                        Color(0xFFFF9800).copy(alpha = 0.3f),
                                                                        Color(0xFFFF9800).copy(alpha = 0.1f)
                                                                    )
                                                                ),
                                                                shape = CircleShape
                                                            ),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Info,
                                                            contentDescription = null,
                                                            tint = Color(0xFFFF9800),
                                                            modifier = Modifier.size(28.dp)
                                                        )
                                                    }
                                                    Column(
                                                        modifier = Modifier.weight(1f),
                                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Text(
                                                            text = "Vous avez déjà joué à ce jeu",
                                                            style = MaterialTheme.typography.titleMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFFE65100),
                                                            fontSize = 16.sp
                                                        )
                                                        Text(
                                                            text = "Une seule partie est autorisée par utilisateur.",
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = FontWeight.Normal,
                                                            color = Color(0xFFE65100),
                                                            fontSize = 13.sp,
                                                            lineHeight = 18.sp
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        
                                        // Le message sera affiché dans un Dialog (voir plus bas)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
