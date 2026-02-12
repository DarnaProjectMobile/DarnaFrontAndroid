package com.sim.darna.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sim.darna.auth.TokenStorage
import com.sim.darna.auth.UserSessionManager
import com.sim.darna.data.model.Publicite
import com.sim.darna.viewmodel.PubliciteViewModel
import com.sim.darna.viewmodel.UiState
import android.content.Context
import android.content.SharedPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicitesListScreen(
    viewModel: PubliciteViewModel = hiltViewModel(),
    onAddClick: () -> Unit = {},
    onEdit: (String) -> Unit = {},
    onDetailClick: (String) -> Unit = {}
) {
    val listState by viewModel.listState.collectAsState()
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
    
    // Debug: Log pour vérifier les valeurs
    LaunchedEffect(userRole, currentUserId, isSponsor) {
        android.util.Log.d("PublicitesListScreen", "Role: $userRole, UserId: $currentUserId, IsSponsor: $isSponsor")
    }
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Tout") }
    
    LaunchedEffect(Unit) {
        viewModel.loadPublicites()
    }

    val publicites = when (val state = listState) {
        is UiState.Success -> state.data
        else -> emptyList()
    }
    val isLoading = listState is UiState.Loading
    val errorMessage = (listState as? UiState.Error)?.message

    // Extraire les sponsors uniques pour la section marques partenaires
    val sponsors = remember(publicites) {
        publicites.mapNotNull { it.sponsorName }
            .distinct()
            .take(10)
    }
    
    // Filtrer les publicités selon la recherche et la catégorie
    val filteredPublicites = remember(publicites, searchQuery, selectedCategory) {
        publicites.filter { pub ->
            val matchesSearch = searchQuery.isEmpty() || 
                pub.titre.contains(searchQuery, ignoreCase = true) ||
                pub.sponsorName?.contains(searchQuery, ignoreCase = true) == true
            val matchesCategory = selectedCategory == "Tout" || 
                pub.categorie?.equals(selectedCategory, ignoreCase = true) == true
            matchesSearch && matchesCategory
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Publicités",
                        fontWeight = FontWeight.Bold
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
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        Icons.Default.Add, 
                        contentDescription = "Ajouter une publicité",
                        tint = Color.White
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {
            // Barre de recherche
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            // Filtres de catégories
            CategoryFilters(
                categories = listOf("Tout", "Nourriture", "Tech", "Loisirs", "Mode", "Sport"),
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it },
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                // Section Marques Partenaires
                item {
                    PartnerBrandsSection(
                        sponsors = sponsors,
                        onAddClick = null, // Supprimé - seul le FAB est utilisé
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // Section Toutes les Promotions
                item {
                    PromotionsHeader(
                        onAddClick = null // Supprimé - seul le FAB est utilisé
                    )
                }
                
                // Liste des publicités
            when {
                isLoading -> {
                        item {
                    Box(
                                modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                            }
                    }
                }

                errorMessage != null -> {
                        item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(errorMessage, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(12.dp))
                                Button(onClick = { viewModel.loadPublicites() }) {
                            Text("Réessayer")
                                }
                            }
                        }
                    }
                    
                    filteredPublicites.isEmpty() -> {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(200.dp),
                                contentAlignment = Alignment.Center
                    ) {
                        Text("Aucune publicité trouvée")
                            }
                        }
                    }
                    
                    else -> {
                        items(filteredPublicites) { pub ->
                            val canManageThis = isSponsor && !currentUserId.isNullOrEmpty() && 
                                (pub.sponsorId == currentUserId || pub.sponsor?._id == currentUserId)
                            
                            // Debug log
                            LaunchedEffect(pub._id, canManageThis) {
                                android.util.Log.d("PublicitesListScreen", 
                                    "Pub: ${pub.titre}, SponsorId: ${pub.sponsorId}, CurrentUserId: $currentUserId, CanManage: $canManageThis")
                            }
                            
                            PromotionCard(
                                publicite = pub,
                                canManage = canManageThis,
                                onEdit = { pub._id?.let(onEdit) },
                                onDelete = { 
                                    pub._id?.let { id ->
                                        viewModel.deletePublicite(id) { success, _ ->
                                            if (success) {
                                                viewModel.loadPublicites()
                                            }
                                        }
                                    }
                                },
                                onClick = { pub._id?.let(onDetailClick) }
                            )
                        }
                    }
                }
                
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Rechercher une marque...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Rechercher")
        },
        trailingIcon = {
            IconButton(onClick = { /* TODO: Filter */ }) {
                Icon(Icons.Default.FilterList, contentDescription = "Filtres")
            }
        },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
        ),
        singleLine = true
    )
}

@Composable
fun CategoryFilters(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(categories) { category ->
            FilterChip(
                selected = category == selectedCategory,
                onClick = { onCategorySelected(category) },
                label = { Text(category) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = Color.White,
                    containerColor = Color.Gray.copy(alpha = 0.1f)
                )
            )
        }
    }
}

@Composable
fun PartnerBrandsSection(
    sponsors: List<String>,
    onAddClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Nos Marques Partenaires",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            if (onAddClick != null) {
                IconButton(
                    onClick = onAddClick,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Ajouter marque",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sponsors) { sponsor ->
                PartnerBrandItem(sponsor = sponsor)
            }
        }
    }
}

@Composable
fun PartnerBrandItem(sponsor: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(
                    MaterialTheme.colorScheme.primary,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = sponsor.take(1).uppercase(),
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = sponsor,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun PromotionsHeader(
    onAddClick: (() -> Unit)?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Toutes les Promotions",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        if (onAddClick != null) {
            IconButton(
                onClick = onAddClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Ajouter promotion",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun PromotionCard(
    publicite: Publicite,
    canManage: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val imageUrl = publicite.image ?: publicite.imageUrl

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
        Column {
                // Image
                if (!imageUrl.isNullOrEmpty()) {
                    Box {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                                .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = publicite.titre,
                    modifier = Modifier
                        .fillMaxWidth()
                                .height(200.dp),
                    contentScale = ContentScale.Crop
                )
                        
                        // Boutons edit/delete en haut à droite de l'image
                        if (canManage) {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .zIndex(1f),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(
                                    onClick = onEdit,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primary,
                                            CircleShape
                                        )
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Modifier",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                IconButton(
                                    onClick = onDelete,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            MaterialTheme.colorScheme.error,
                                            CircleShape
                                        )
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Supprimer",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
            } else {
                    Box {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                                .height(200.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(publicite.type ?: "Publicité")
                }
                        
                        // Boutons edit/delete même sans image
                        if (canManage) {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .zIndex(1f),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(
                                    onClick = onEdit,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primary,
                                            CircleShape
                                        )
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Modifier",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                IconButton(
                                    onClick = onDelete,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            MaterialTheme.colorScheme.error,
                                            CircleShape
                                        )
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Supprimer",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Contenu de la carte
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Nom du sponsor
                    if (!publicite.sponsorName.isNullOrEmpty()) {
                        Text(
                            text = publicite.sponsorName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Titre/Offre
                    Text(
                        text = publicite.titre,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Description
                    Text(
                        text = publicite.description,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    // Date d'expiration
                    if (!publicite.dateExpiration.isNullOrEmpty()) {
                        Text(
                            text = "Expire le ${publicite.dateExpiration}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
