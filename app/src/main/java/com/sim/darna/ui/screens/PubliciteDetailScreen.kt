package com.sim.darna.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.sim.darna.data.model.PubliciteType
import com.sim.darna.ui.components.QRCodeView
import com.sim.darna.ui.theme.*
import com.sim.darna.utils.SessionManager
import com.sim.darna.viewmodel.PubliciteDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PubliciteDetailScreen(
    publiciteId: String,
    onNavigateBack: () -> Unit,
    onDelete: () -> Unit,
    onEdit: ((String) -> Unit)? = null,
    viewModel: PubliciteDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val currentUser = remember { sessionManager.getUser() }
    val isSponsor = currentUser?.role == "sponsor" || currentUser?.role == "admin"
    
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(publiciteId) {
        viewModel.loadPublicite(publiciteId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Offres Étudiantes",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Notifications */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White,
                    titleContentColor = TextPrimary
                )
            )
        }
    ) { paddingValues ->
        // Dialogue de confirmation de suppression
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Supprimer l'annonce") },
                text = { Text("Êtes-vous sûr de vouloir supprimer cette annonce ? Cette action est irréversible.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            when (val state = uiState) {
                                is com.sim.darna.viewmodel.PubliciteDetailUiState.Success -> {
                                    viewModel.deletePublicite(state.publicite.id ?: "", onDelete)
                                    showDeleteDialog = false
                                }
                                else -> {}
                            }
                        }
                    ) {
                        Text("Supprimer", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Annuler")
                    }
                }
            )
        }
        
        val currentState = uiState
        when (currentState) {
            is com.sim.darna.viewmodel.PubliciteDetailUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BluePrimary)
                }
            }
            is com.sim.darna.viewmodel.PubliciteDetailUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Erreur: ${currentState.message}",
                            color = Color.Red
                        )
                        Button(onClick = { viewModel.loadPublicite(publiciteId) }) {
                            Text("Réessayer")
                        }
                    }
                }
            }
            is com.sim.darna.viewmodel.PubliciteDetailUiState.Success -> {
                PubliciteDetailContent(
                    publicite = currentState.publicite,
                    currentUser = currentUser,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(GreyLight)
                        .verticalScroll(rememberScrollState())
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PubliciteDetailContent(
    publicite: com.sim.darna.data.model.Publicite,
    currentUser: com.sim.darna.auth.UserDto?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showCopiedToast by remember { mutableStateOf(false) }
    
    // Utiliser le nom de l'utilisateur connecté comme nom de marque par défaut
    val brandName = publicite.sponsorName ?: currentUser?.username ?: "Marque"
    
    // Générer le code promo à partir de l'ID ou utiliser un code existant
    val promoCode = remember(publicite.id) {
        publicite.qrCode ?: "STUDENT2025${brandName.uppercase().take(6)}"
    }
    
    Column(modifier = modifier) {
        // Image avec badge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
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
                        .background(GreyMedium)
                )
            }
            
            // Badge -33% ou pourcentage de réduction
            val reductionPercent = publicite.detailReduction?.pourcentage ?: 33
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                shape = RoundedCornerShape(8.dp),
                color = BluePrimary
            ) {
                Text(
                    text = "-$reductionPercent%",
                    color = White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
        
        // Card principale
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Logo et nom de la marque avec tag catégorie
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Logo carré avec initiale
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(BlueLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = brandName.take(1).uppercase(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary
                        )
                    }
                    
                    // Nom de la marque en bleu
                    Text(
                        text = brandName,
                        color = BluePrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Tag catégorie
                    val categorieName = when (publicite.categorie) {
                        com.sim.darna.data.model.Categorie.NOURRITURE -> "Nourriture"
                        com.sim.darna.data.model.Categorie.TECH -> "Tech"
                        com.sim.darna.data.model.Categorie.LOISIRS -> "Loisirs"
                        com.sim.darna.data.model.Categorie.VOYAGE -> "Voyage"
                        com.sim.darna.data.model.Categorie.MODE -> "Mode"
                        else -> "Autre"
                    }
                    
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BlueLight
                    ) {
                        Text(
                            text = categorieName,
                            fontSize = 12.sp,
                            color = BluePrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Titre
                Text(
                    text = publicite.titre,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Description
                Text(
                    text = publicite.description,
                    fontSize = 15.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 24.dp),
                    lineHeight = 22.sp
                )
                
                // Section QR Code et Code Promo
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = GreyLight)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Code Promo",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        // QR Code
                        QRCodeView(
                            qrCodeData = promoCode,
                            modifier = Modifier
                                .size(200.dp)
                                .padding(bottom = 16.dp)
                        )
                        
                        // Code à utiliser
                        Text(
                            text = "Code à utiliser :",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        // Champ code promo avec bouton copier
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = promoCode,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = White,
                                    unfocusedContainerColor = White
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                            
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Code Promo", promoCode)
                                    clipboard.setPrimaryClip(clip)
                                    showCopiedToast = true
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(BluePrimary, RoundedCornerShape(8.dp))
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "Copier",
                                    tint = White
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Bouton Télécharger QR Code
                        Button(
                            onClick = { /* Télécharger QR code */ },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Télécharger le QR Code", color = White)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Section Comment utiliser
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Comment utiliser ?",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        val steps = listOf(
                            "Scannez le QR code en magasin",
                            "Ou copiez le code promo",
                            "Présentez votre carte étudiante",
                            "Profitez de votre réduction !"
                        )
                        
                        steps.forEachIndexed { index, step ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${index + 1}.",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BluePrimary,
                                    modifier = Modifier.width(24.dp)
                                )
                                Text(
                                    text = step,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
    
    // Toast pour confirmation de copie
    if (showCopiedToast) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(2000)
            showCopiedToast = false
        }
        AlertDialog(
            onDismissRequest = { showCopiedToast = false },
            title = { Text("Code copié !") },
            text = { Text("Le code promo a été copié dans le presse-papiers.") },
            confirmButton = {
                TextButton(onClick = { showCopiedToast = false }) {
                    Text("OK")
                }
            }
        )
    }
}

// Preview pour PubliciteDetailContent
@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun PubliciteDetailContentPreview() {
    val samplePublicite = com.sim.darna.data.model.Publicite(
        id = "1",
        titre = "2 Pizzas Achetées = 1 Offerte",
        description = "Profitez de notre offre spéciale étudiants: achetez 2 pizzas et recevez la 3ème gratuitement!",
        imageUrl = null,
        type = com.sim.darna.data.model.PubliciteType.REDUCTION,
        sponsorId = "sponsor1",
        sponsorName = "Pizza Express",
        categorie = com.sim.darna.data.model.Categorie.NOURRITURE,
        dateExpiration = "31 décembre 2025",
        detailReduction = com.sim.darna.data.model.DetailReduction(
            pourcentage = 33,
            conditionsUtilisation = "Valable uniquement sur présentation de la carte étudiante"
        ),
        qrCode = "STUDENT2025PIZZA"
    )
    
    val sampleUser = com.sim.darna.auth.UserDto(
        id = "user1",
        username = "manel",
        email = "manel@example.com",
        role = "sponsor"
    )
    
    PubliciteDetailContent(
        publicite = samplePublicite,
        currentUser = sampleUser,
        modifier = Modifier.background(GreyLight)
    )
}
