package com.sim.darna.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.sim.darna.data.model.Publicite
import com.sim.darna.viewmodel.PubliciteViewModel
import com.sim.darna.viewmodel.UiState
import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import com.sim.darna.auth.TokenStorage
import com.sim.darna.auth.UserSessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicitesListScreen(
    viewModel: PubliciteViewModel = hiltViewModel(),
    onAddClick: () -> Unit = {},
    onEdit: (String) -> Unit = {},
    onDetailClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val listState by viewModel.listState.collectAsState()
    
    // Récupérer le rôle et l'ID utilisateur depuis SharedPreferences
    val prefs = remember { context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE) }
    val userRole = remember(prefs) { prefs.getString("role", "user") ?: "user" }
    val currentUserId = remember(prefs) { 
        prefs.getString("user_id", null) ?: TokenStorage.getUserId(context)
    }
    val isSponsor = remember(userRole) {
        userRole.lowercase() == "sponsor" || UserSessionManager.isSponsor()
    }
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategorie by remember { mutableStateOf("Tout") }
    var selectedSponsorId by remember { mutableStateOf<String?>(null) }
    var publiciteToDelete by remember { mutableStateOf<Publicite?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.loadPublicites()
    }
    
    val categories = listOf("Tout", "Nourriture", "Tech", "Loisirs", "Mode", "Santé", "Autre")
    
    // Extraire les publicités depuis l'état
    val publicites = remember(listState) {
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
    
    // Extraire les sponsors uniques depuis les publicités
    val sponsors = remember(publicites) {
        publicites
            .mapNotNull { it.sponsor }
            .distinctBy { it._id }
            .filter { it.role?.uppercase() == "SPONSOR" }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (selectedSponsorId != null) {
                            val selectedSponsor = sponsors.find { it._id == selectedSponsorId }
                            "Publicités de ${selectedSponsor?.username ?: "Sponsor"}"
                        } else {
                            "Toutes les Publicités"
                        },
                        color = Color(0xFF1A1A1A),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1A1A1A)
                )
            )
        },
        floatingActionButton = {
            if (isSponsor) {
                FloatingActionButton(
                    onClick = onAddClick,
                    modifier = Modifier
                        .shadow(
                            elevation = 8.dp,
                            shape = CircleShape,
                            spotColor = Color(0xFF2196F3).copy(alpha = 0.4f)
                        ),
                    containerColor = Color(0xFF2196F3),
                    contentColor = Color.White
                ) {
                    Icon(
                        Icons.Default.Add, 
                        contentDescription = "Ajouter",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
        containerColor = Color(0xFFF5F7FA)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(20.dp),
                            spotColor = Color.Black.copy(alpha = 0.08f)
                        ),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Barre de recherche
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { 
                                Text(
                                    "Rechercher une publicité...", 
                                    fontSize = 14.sp, 
                                    color = Color(0xFF999999)
                                ) 
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = Color(0xFF2196F3)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2196F3),
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                unfocusedContainerColor = Color(0xFFFAFAFA),
                                focusedContainerColor = Color.White,
                                focusedTextColor = Color(0xFF1A1A1A),
                                unfocusedTextColor = Color(0xFF1A1A1A)
                            ),
                            singleLine = true
                        )
                    
                        // Filtres de catégories
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            categories.forEach { categorie ->
                                var scale by remember { mutableStateOf(1f) }
                                val animatedScale by animateFloatAsState(
                                    targetValue = scale,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    ),
                                    label = "scale"
                                )
                                
                                FilterChip(
                                    selected = selectedCategorie == categorie,
                                    onClick = {
                                        scale = 0.95f
                                        selectedCategorie = categorie
                                        scale = 1f
                                    },
                                    modifier = Modifier.scale(animatedScale),
                                    label = { 
                                        Text(
                                            categorie, 
                                            fontSize = 14.sp,
                                            fontWeight = if (selectedCategorie == categorie) FontWeight.SemiBold else FontWeight.Medium
                                        ) 
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF2196F3),
                                        selectedLabelColor = Color.White,
                                        containerColor = Color(0xFFFAFAFA),
                                        labelColor = Color(0xFF666666)
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selectedCategorie == categorie,
                                        borderColor = Color(0xFFE0E0E0),
                                        selectedBorderColor = Color(0xFF2196F3),
                                        borderWidth = if (selectedCategorie == categorie) 2.dp else 1.dp
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            // Section User Sponsors (extrait des publicités)
            if (sponsors.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Person,
                                contentDescription = null,
                                tint = Color(0xFF2196F3),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                "User Sponsors",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1A1A)
                            )
                        }
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            sponsors.forEach { sponsor ->
                                SponsorItem(
                                    sponsor = sponsor,
                                    isSelected = selectedSponsorId == sponsor._id,
                                    onClick = {
                                        // Toggle selection: if already selected, deselect; otherwise select
                                        selectedSponsorId = if (selectedSponsorId == sponsor._id) null else sponsor._id
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            // Section Promotions
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Outlined.LocalOffer,
                        contentDescription = null,
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "Toutes les Promotions",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                }
            }
            
            // Liste des publicités
            when (val state = listState) {
                is UiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = Color(0xFF2196F3),
                                    modifier = Modifier.size(48.dp),
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
                }
                is UiState.Success<*> -> {
                    val filteredPublicites = publicites.filter { pub ->
                        val matchesSearch = searchQuery.isEmpty() || 
                            pub.titre.contains(searchQuery, ignoreCase = true) ||
                            pub.description.contains(searchQuery, ignoreCase = true)
                        val matchesCategorie = selectedCategorie == "Tout" || 
                            pub.categorie?.equals(selectedCategorie, ignoreCase = true) == true
                        val matchesSponsor = selectedSponsorId == null || 
                            pub.sponsor?._id == selectedSponsorId || 
                            pub.sponsorId == selectedSponsorId
                        matchesSearch && matchesCategorie && matchesSponsor
                    }
                    
                    // Trier les publicités :
                    // 1. D'abord les publicités du sponsor connecté
                    // 2. Puis par date de publication (ordre décroissant - plus récent en premier)
                    val sortedPublicites = filteredPublicites.sortedWith(
                        compareByDescending<Publicite> { pub ->
                            // Prioriser les publicités du sponsor connecté
                            val isCurrentSponsorPub = !currentUserId.isNullOrEmpty() &&
                                (pub.sponsorId == currentUserId || pub.sponsor?._id == currentUserId)
                            if (isCurrentSponsorPub) 1 else 0
                        }.thenByDescending { pub ->
                            // Puis trier par date de publication (createdAt) - plus récent en premier
                            pub.createdAt?.time ?: 0L
                        }
                    )
                    
                    if (sortedPublicites.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp)
                                    .shadow(
                                        elevation = 4.dp,
                                        shape = RoundedCornerShape(20.dp),
                                        spotColor = Color.Black.copy(alpha = 0.08f)
                                    ),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.SearchOff,
                                        contentDescription = null,
                                        tint = Color(0xFF999999),
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Text(
                                        if (selectedSponsorId != null) {
                                            val selectedSponsor = sponsors.find { it._id == selectedSponsorId }
                                            "Aucune publicité trouvée pour ${selectedSponsor?.username ?: "ce sponsor"}"
                                        } else {
                                            "Aucune publicité trouvée"
                                        },
                                        color = Color(0xFF666666),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        items(sortedPublicites) { publicite ->
                            val canManage = isSponsor && !currentUserId.isNullOrEmpty() &&
                                (publicite.sponsorId == currentUserId || publicite.sponsor?._id == currentUserId)
                            
                            PubliciteCard(
                                publicite = publicite,
                                onClick = { 
                                    publicite._id?.let { onDetailClick(it) }
                                },
                                onEdit = {
                                    if (canManage) {
                                        publicite._id?.let { onEdit(it) }
                                    }
                                },
                                onDelete = {
                                    if (canManage) {
                                        publiciteToDelete = publicite
                                        showDeleteDialog = true
                                    }
                                },
                                canManage = canManage
                            )
                        }
                    }
                }
                is UiState.Error -> {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp)
                                .shadow(
                                    elevation = 4.dp,
                                    shape = RoundedCornerShape(20.dp),
                                    spotColor = Color(0xFFF44336).copy(alpha = 0.2f)
                                ),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
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
                                    state.message ?: "Erreur de chargement",
                                    color = Color(0xFFC62828),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Button(
                                    onClick = { viewModel.loadPublicites() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF2196F3)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Réessayer", color = Color.White)
                                }
                            }
                        }
                    }
                }
                else -> {}
            }
            
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
        
        // Dialog de confirmation de suppression
        if (showDeleteDialog && publiciteToDelete != null) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteDialog = false
                    publiciteToDelete = null
                },
                title = {
                    Text("Supprimer la publicité ?")
                },
                text = {
                    Text("Cette action supprimera définitivement « ${publiciteToDelete!!.titre} ». Cette action est irréversible.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            publiciteToDelete?._id?.let { id ->
                                viewModel.deletePublicite(context, id) { success, message ->
                                    if (success) {
                                        Toast.makeText(context, "Publicité supprimée avec succès", Toast.LENGTH_SHORT).show()
                                        viewModel.loadPublicites() // Recharger la liste
                                    } else {
                                        val errorMsg = message ?: "Erreur lors de la suppression"
                                        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                            showDeleteDialog = false
                            publiciteToDelete = null
                        }
                    ) {
                        Text("Supprimer", color = Color(0xFFE53935))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            publiciteToDelete = null
                        }
                    ) {
                        Text("Annuler")
                    }
                }
            )
        }
    }
}

@Composable
fun SponsorItem(
    sponsor: com.sim.darna.data.model.Sponsor,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    var scale by remember { mutableStateOf(1f) }
    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .clickable {
                scale = 0.9f
                onClick()
                scale = 1f
            }
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .scale(animatedScale)
                .shadow(
                    elevation = if (isSelected) 8.dp else 4.dp,
                    shape = CircleShape,
                    spotColor = if (isSelected) Color(0xFF2196F3).copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.2f)
                )
                .clip(CircleShape)
                .background(
                    brush = if (isSelected) {
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF1976D2),
                                Color(0xFF2196F3)
                            )
                        )
                    } else {
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF2196F3),
                                Color(0xFF42A5F5)
                            )
                        )
                    }
                )
                .then(
                    if (isSelected) {
                        Modifier.border(3.dp, Color(0xFF0D47A1), CircleShape)
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (sponsor.image != null && sponsor.image.isNotBlank()) {
                AsyncImage(
                    model = sponsor.image,
                    contentDescription = sponsor.username ?: "Sponsor",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Afficher l'initiale si pas d'image
                Text(
                    sponsor.username?.take(1)?.uppercase() ?: "S",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        Text(
            sponsor.username ?: "Sponsor",
            fontSize = 12.sp,
            color = if (isSelected) Color(0xFF1976D2) else Color(0xFF666666),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 90.dp)
        )
    }
}

@Composable
fun PubliciteCard(
    publicite: Publicite,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    canManage: Boolean = false
) {
    var scale by remember { mutableStateOf(1f) }
    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    Card(
        onClick = {
            scale = 0.98f
            onClick()
            scale = 1f
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .scale(animatedScale)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = Color.Black.copy(alpha = 0.1f)
            ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column {
            // Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                AsyncImage(
                    model = publicite.image ?: publicite.imageUrl,
                    contentDescription = publicite.titre,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                    contentScale = ContentScale.Crop
                )
                
                // Overlay gradient en bas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.2f)
                                ),
                                startY = 150f
                            )
                        )
                )
                
                // Tag de type de publicité en overlay (coin supérieur droit)
                if (!publicite.type.isNullOrEmpty()) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .shadow(
                                elevation = 6.dp,
                                shape = RoundedCornerShape(20.dp),
                                spotColor = Color(0xFF2196F3).copy(alpha = 0.4f)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF2196F3)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = publicite.type.uppercase(),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            // Contenu
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    publicite.titre,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Text(
                    publicite.description,
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )
                
                // Date d'expiration
                publicite.dateExpiration?.let { date ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = Color(0xFF999999),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "Expire le $date",
                            fontSize = 12.sp,
                            color = Color(0xFF999999),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                // Boutons Modifier et Supprimer (seulement si canManage)
                if (canManage) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Bouton Modifier (bleu)
                        OutlinedButton(
                            onClick = onEdit,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .shadow(
                                    elevation = 2.dp,
                                    shape = RoundedCornerShape(12.dp),
                                    spotColor = Color(0xFF2196F3).copy(alpha = 0.2f)
                                ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF2196F3)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                2.dp,
                                Color(0xFF2196F3)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Color(0xFF2196F3)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Modifier",
                                color = Color(0xFF2196F3),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                        
                        // Bouton Supprimer (rouge)
                        OutlinedButton(
                            onClick = onDelete,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .shadow(
                                    elevation = 2.dp,
                                    shape = RoundedCornerShape(12.dp),
                                    spotColor = Color(0xFFE53935).copy(alpha = 0.2f)
                                ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFE53935)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                2.dp,
                                Color(0xFFE53935)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Color(0xFFE53935)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Supprimer",
                                color = Color(0xFFE53935),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

