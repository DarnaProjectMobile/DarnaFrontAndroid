package com.sim.darna.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.sim.darna.data.model.*
import com.sim.darna.ui.components.QRCodeDisplay
import com.sim.darna.ui.components.RouletteGame
import com.sim.darna.ui.theme.*
import com.sim.darna.viewmodel.PubliciteDetailViewModel
import com.sim.darna.viewmodel.PubliciteDetailUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PubliciteDetailScreen(
    navController: NavController,
    publiciteId: String,
    viewModel: PubliciteDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(publiciteId) {
        viewModel.loadPublicite(publiciteId)
    }

    when (val state = uiState) {
        is PubliciteDetailUiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(GreyLight),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        color = BluePrimary,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Chargement...",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        }
        is PubliciteDetailUiState.Error -> {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Erreur", fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = { navController.navigateUp() }) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "Retour",
                                    tint = TextPrimary
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = White,
                            titleContentColor = TextPrimary
                        )
                    )
                },
                containerColor = GreyLight
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFEBEE)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Oups !",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD32F2F)
                            )
                            Text(
                                text = state.message,
                                fontSize = 14.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = { viewModel.loadPublicite(publiciteId) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BluePrimary
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Réessayer")
                            }
                        }
                    }
                }
            }
        }
        is PubliciteDetailUiState.Success -> {
            PubliciteDetailContent(
                navController = navController,
                publicite = state.publicite
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PubliciteDetailContent(
    navController: NavController,
    publicite: Publicite
) {
    var showShareDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(GreyLight)
                .verticalScroll(rememberScrollState())
        ) {
            // Image principale avec boutons overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
            ) {
                // Image de fond
                if (publicite.imageUrl != null) {
                    Image(
                        painter = rememberAsyncImagePainter(publicite.imageUrl),
                        contentDescription = publicite.titre,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        when (publicite.type) {
                                            PubliciteType.REDUCTION -> Color(0xFF00C853)
                                            PubliciteType.PROMOTION -> BluePrimary
                                            PubliciteType.JEU -> Color(0xFFFF6D00)
                                        }.copy(alpha = 0.7f),
                                        when (publicite.type) {
                                            PubliciteType.REDUCTION -> Color(0xFF00C853)
                                            PubliciteType.PROMOTION -> BluePrimary
                                            PubliciteType.JEU -> Color(0xFFFF6D00)
                                        }
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (publicite.type) {
                                PubliciteType.REDUCTION -> Icons.Default.Favorite
                                PubliciteType.PROMOTION -> Icons.Default.CheckCircle
                                PubliciteType.JEU -> Icons.Default.Star
                            },
                            contentDescription = null,
                            tint = White.copy(alpha = 0.3f),
                            modifier = Modifier.size(120.dp)
                        )
                    }
                }

                // Gradient overlay en bas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    GreyLight.copy(alpha = 0.8f),
                                    GreyLight
                                )
                            )
                        )
                )

                // Boutons en haut
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Bouton retour
                    IconButton(
                        onClick = { navController.navigateUp() },
                        modifier = Modifier
                            .size(48.dp)
                            .background(White.copy(alpha = 0.95f), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Fermer",
                            tint = TextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Bouton partager
                    IconButton(
                        onClick = { showShareDialog = true },
                        modifier = Modifier
                            .size(48.dp)
                            .background(White.copy(alpha = 0.95f), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Partager",
                            tint = TextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Contenu principal
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Badge de réduction/promotion
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when (publicite.type) {
                        PubliciteType.REDUCTION -> Color(0xFF00C853)
                        PubliciteType.PROMOTION -> BluePrimary
                        PubliciteType.JEU -> Color(0xFFFF6D00)
                    },
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (publicite.type) {
                                PubliciteType.REDUCTION -> Icons.Default.Favorite
                                PubliciteType.PROMOTION -> Icons.Default.CheckCircle
                                PubliciteType.JEU -> Icons.Default.Star
                            },
                            contentDescription = null,
                            tint = White,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = when (publicite.type) {
                                PubliciteType.REDUCTION -> if (publicite.detailReduction != null)
                                    "-${publicite.detailReduction.pourcentage}% pour les étudiants"
                                else "Réduction étudiante"
                                PubliciteType.PROMOTION -> "Offre spéciale"
                                PubliciteType.JEU -> "Jouez et gagnez !"
                            },
                            color = White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Titre
                Text(
                    text = publicite.titre,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    lineHeight = 36.sp
                )

                // Description
                Text(
                    text = publicite.description,
                    fontSize = 16.sp,
                    color = TextSecondary,
                    lineHeight = 24.sp
                )

                // Info sponsor
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = BlueLight,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = publicite.sponsorName?.take(1)?.uppercase() ?: "?",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BluePrimary
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Offre proposée par",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = publicite.sponsorName ?: "Sponsor",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                }

                // Date d'expiration
                if (publicite.dateExpiration != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFFF6D00).copy(alpha = 0.1f),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null,
                                        tint = Color(0xFFFF6D00),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Date d'expiration",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "Valable jusqu'au ${publicite.dateExpiration}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }

                // Contenu spécifique selon le type
                when (publicite.type) {
                    PubliciteType.REDUCTION -> {
                        // Afficher le QR Code
                        QRCodeSection(
                            qrCodeData = publicite.qrCode ?: publicite.id ?: "NO_ID",
                            detailReduction = publicite.detailReduction
                        )
                    }
                    PubliciteType.PROMOTION -> {
                        // Afficher les détails de la promotion
                        if (publicite.detailPromotion != null) {
                            PromotionDetailsSection(publicite.detailPromotion)
                        }
                    }
                    PubliciteType.JEU -> {
                        // Afficher la roulette
                        RouletteSection(publicite.detailJeu)
                    }
                }

                // Bouton d'action principal
                Button(
                    onClick = {
                        // TODO: Logique selon le type
                        when (publicite.type) {
                            PubliciteType.REDUCTION -> {
                                // Afficher le QR code en plein écran ou copier le code
                            }
                            PubliciteType.PROMOTION -> {
                                // Afficher les conditions
                            }
                            PubliciteType.JEU -> {
                                // Pas d'action, la roulette est déjà affichée
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when (publicite.type) {
                            PubliciteType.REDUCTION -> Color(0xFF00C853)
                            PubliciteType.PROMOTION -> BluePrimary
                            PubliciteType.JEU -> Color(0xFFFF6D00)
                        }
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = when (publicite.type) {
                            PubliciteType.REDUCTION -> Icons.Default.Check
                            PubliciteType.PROMOTION -> Icons.Default.Info
                            PubliciteType.JEU -> Icons.Default.Star
                        },
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (publicite.type) {
                            PubliciteType.REDUCTION -> "J'en Profite !"
                            PubliciteType.PROMOTION -> "Voir les conditions"
                            PubliciteType.JEU -> "Bonne chance !"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Dialog de partage
    if (showShareDialog) {
        AlertDialog(
            onDismissRequest = { showShareDialog = false },
            icon = {
                Icon(
                    Icons.Default.Share,
                    contentDescription = null,
                    tint = BluePrimary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    "Partager cette offre",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Cette fonctionnalité sera bientôt disponible !")
            },
            confirmButton = {
                TextButton(onClick = { showShareDialog = false }) {
                    Text("OK", color = BluePrimary)
                }
            }
        )
    }
}

@Composable
fun QRCodeSection(
    qrCodeData: String,
    detailReduction: DetailReduction?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // En-tête avec icône
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF00C853).copy(alpha = 0.1f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF00C853),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = "Votre QR Code",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Prêt à être scanné",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Text(
                text = "Présentez ce code en caisse pour bénéficier de votre réduction",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            // QR Code Display
            QRCodeDisplay(
                data = qrCodeData,
                size = 220.dp
            )

            if (detailReduction != null) {
                Divider(color = GreyLight, thickness = 1.dp)

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = BluePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Conditions d'utilisation",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Text(
                        text = detailReduction.conditionsUtilisation,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
fun PromotionDetailsSection(detailPromotion: DetailPromotion) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // En-tête
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = BluePrimary.copy(alpha = 0.1f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = BluePrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Text(
                    text = "Détails de l'offre",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Divider(color = GreyLight, thickness = 1.dp)

            // Détails
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                InfoRow(
                    label = "Offre",
                    value = detailPromotion.offre,
                    icon = Icons.Default.Star,
                    iconColor = Color(0xFFFFC107)
                )

                InfoRow(
                    label = "Conditions",
                    value = detailPromotion.conditions,
                    icon = Icons.Default.Info,
                    iconColor = BluePrimary
                )
            }
        }
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color = BluePrimary
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = iconColor.copy(alpha = 0.05f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = iconColor.copy(alpha = 0.1f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    fontSize = 15.sp,
                    color = TextPrimary,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun RouletteSection(detailJeu: DetailJeu?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFF6D00).copy(alpha = 0.05f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // En-tête
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🎰",
                    fontSize = 48.sp
                )
                Text(
                    text = "Tournez la roue !",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF6D00)
                )

                if (detailJeu != null) {
                    Text(
                        text = detailJeu.description,
                        fontSize = 14.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                } else {
                    Text(
                        text = "Tentez votre chance et gagnez des récompenses exclusives !",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }

            Divider(color = Color(0xFFFF6D00).copy(alpha = 0.2f), thickness = 1.dp)

            // Roulette Game Component
            RouletteGame(
                gains = detailJeu?.gains ?: listOf(
                    "10% de réduction",
                    "Boisson offerte",
                    "Dessert gratuit",
                    "20% de réduction",
                    "Café offert",
                    "5€ offerts"
                )
            )
        }
    }
}