package com.sim.darna.screens

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
                        "Toutes les Publicités",
                        color = Color(0xFF2196F3),
                        fontWeight = FontWeight.Medium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        floatingActionButton = {
            if (isSponsor) {
                FloatingActionButton(
                    onClick = onAddClick,
                    containerColor = Color(0xFF2196F3),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Ajouter")
                }
            }
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Barre de recherche
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { 
                            Text("Rechercher une publicité...", fontSize = 14.sp, color = Color.Gray) 
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFE0E0E0),
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            unfocusedContainerColor = Color(0xFFFAFAFA),
                            focusedContainerColor = Color(0xFFFAFAFA)
                        ),
                        singleLine = true
                    )
                    
                    // Filtres de catégories
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.ChevronLeft,
                            contentDescription = null,
                            modifier = Modifier
                                .size(32.dp)
                                .align(Alignment.CenterVertically),
                            tint = Color.Gray
                        )
                        
                        categories.forEach { categorie ->
                            FilterChip(
                                selected = selectedCategorie == categorie,
                                onClick = { selectedCategorie = categorie },
                                label = { Text(categorie, fontSize = 13.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF2196F3),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.White,
                                    labelColor = Color(0xFF666666)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selectedCategorie == categorie,
                                    borderColor = Color(0xFFE0E0E0),
                                    selectedBorderColor = Color(0xFF2196F3),
                                    borderWidth = 1.dp
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                        }
                        
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier
                                .size(32.dp)
                                .align(Alignment.CenterVertically),
                            tint = Color.Gray
                        )
                    }
                }
            }
            
            // Section User Sponsors (extrait des publicités)
            if (sponsors.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "User Sponsors",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2196F3)
                        )
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Default.ChevronLeft,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(32.dp)
                                    .align(Alignment.CenterVertically),
                                tint = Color.Gray
                            )
                            
                            sponsors.forEach { sponsor ->
                                SponsorItem(sponsor)
                            }
                            
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(32.dp)
                                    .align(Alignment.CenterVertically),
                                tint = Color.Gray
                            )
                        }
                    }
                }
            }
            
            // Section Promotions
            item {
                Text(
                    "Toutes les Promotions",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2196F3),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            // Liste des publicités
            when (val state = listState) {
                is UiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF2196F3))
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
                        matchesSearch && matchesCategorie
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
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Aucune publicité trouvée",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
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
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    state.message ?: "Erreur de chargement",
                                    color = Color.Red,
                                    fontSize = 14.sp
                                )
                                TextButton(onClick = { viewModel.loadPublicites() }) {
                                    Text("Réessayer")
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
fun SponsorItem(sponsor: com.sim.darna.data.model.Sponsor) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color(0xFF2196F3)),
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
            fontSize = 11.sp,
            color = Color(0xFF666666),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 80.dp)
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
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
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
                
            }
            
            // Contenu
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    publicite.titre,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
                Text(
                    publicite.description,
                    fontSize = 13.sp,
                    color = Color(0xFF666666),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Date d'expiration
                publicite.dateExpiration?.let { date ->
                    Text(
                        "Expire le $date",
                        fontSize = 11.sp,
                        color = Color(0xFF999999)
                    )
                }
                
                // Boutons Modifier et Supprimer (seulement si canManage)
                if (canManage) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Bouton Modifier (bleu)
                        OutlinedButton(
                            onClick = onEdit,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF2196F3)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Color(0xFF2196F3)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Color(0xFF2196F3)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Modifier",
                                color = Color(0xFF2196F3),
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                        
                        // Bouton Supprimer (rouge)
                        OutlinedButton(
                            onClick = onDelete,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFE53935)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Color(0xFFE53935)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Color(0xFFE53935)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Supprimer",
                                color = Color(0xFFE53935),
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

