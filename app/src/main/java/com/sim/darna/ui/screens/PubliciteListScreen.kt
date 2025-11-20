package com.sim.darna.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.sim.darna.data.model.Categorie
import com.sim.darna.data.model.Publicite
import com.sim.darna.ui.components.PubliciteCard
import com.sim.darna.ui.components.PubliciteCardWithActions
import com.sim.darna.ui.theme.*
import com.sim.darna.utils.SessionManager
import com.sim.darna.viewmodel.PubliciteListViewModel
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.sim.darna.data.model.PubliciteType


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PubliciteListScreen(
    navController: NavController,
    onPubliciteClick: (String) -> Unit,
    onAddPubliciteClick: () -> Unit,
    onEditPublicite: ((String) -> Unit)? = null,
    onDeletePublicite: ((String) -> Unit)? = null,
    viewModel: PubliciteListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val publicites by viewModel.publicites.collectAsStateWithLifecycle()
    val selectedCategorie by viewModel.selectedCategorie.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val currentUser = remember { sessionManager.getUser() }
    val isSponsor = currentUser?.role == "sponsor" || currentUser?.role == "admin"

    var searchText by remember { mutableStateOf("") }

    val currentBackStackEntry by navController.currentBackStackEntryAsState()

    LaunchedEffect(currentBackStackEntry) {
        val entry = currentBackStackEntry
        entry?.savedStateHandle
            ?.getStateFlow("refreshOffers", false)
            ?.collectLatest { shouldRefresh ->
                if (shouldRefresh) {
                    viewModel.refresh()
                    entry?.savedStateHandle?.set("refreshOffers", false)
                }
            }
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
                    IconButton(onClick = { /* Menu */ }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddPubliciteClick,
                containerColor = BluePrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter", tint = White)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(GreyLight)
        ) {
            // Barre de recherche
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = {
                        searchText = it
                        viewModel.search(it)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp)),
                    placeholder = { Text("Rechercher une marque...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Rechercher")
                    },
                    trailingIcon = {
                        IconButton(onClick = { /* Filter */ }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filtres")
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = White,
                        unfocusedContainerColor = White
                    )
                )
            }

            // Filtres par catégorie
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(Categorie.values()) { categorie ->
                    FilterChip(
                        selected = selectedCategorie == categorie,
                        onClick = {
                            viewModel.filterByCategorie(
                                if (selectedCategorie == categorie) null else categorie
                            )
                        },
                        label = {
                            Text(
                                text = when (categorie) {
                                    Categorie.TOUT -> "Tout"
                                    Categorie.NOURRITURE -> "Nourriture"
                                    Categorie.TECH -> "Tech"
                                    Categorie.LOISIRS -> "Loisirs"
                                    Categorie.VOYAGE -> "Voyage"
                                    Categorie.MODE -> "Mode"
                                    Categorie.AUTRE -> "Autre"
                                },
                                fontSize = 14.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BluePrimary,
                            selectedLabelColor = White,
                            containerColor = White,
                            labelColor = TextPrimary
                        )
                    )
                }
            }

            // Section "Nos Marques Partenaires"
            val uniqueBrands = remember(publicites, currentUser?.username) {
                publicites.mapNotNull { it.sponsorName ?: currentUser?.username }
                    .distinct()
                    .take(10)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Text(
                    text = "Nos Marques Partenaires",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(uniqueBrands) { brand ->
                        PartnerBrandItem(brandName = brand)
                    }
                }
            }

            // Liste des publicités
            Text(
                text = "Toutes les Promotions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            val currentState = uiState
            when (currentState) {
                is com.sim.darna.viewmodel.PubliciteListUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = BluePrimary)
                    }
                }
                is com.sim.darna.viewmodel.PubliciteListUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
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
                            Button(onClick = { viewModel.refresh() }) {
                                Text("Réessayer")
                            }
                        }
                    }
                }
                is com.sim.darna.viewmodel.PubliciteListUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(publicites) { publicite ->
                            // Vérifier si l'utilisateur est le propriétaire de la publicité
                            val isOwner = if (isSponsor && currentUser != null) {
                                if (currentUser.role == "admin") {
                                    true // Les admins peuvent modifier toutes les publicités
                                } else {
                                    // Pour les sponsors, vérifier si c'est leur publicité
                                    val userIdentifier = currentUser.id ?: currentUser.username

                                    if (publicite.sponsorId.isEmpty()) {
                                        true // Si pas de sponsorId, permettre à tous les sponsors
                                    } else {
                                        publicite.sponsorId == currentUser.id ||
                                                publicite.sponsorId == currentUser.username ||
                                                publicite.sponsorId == userIdentifier ||
                                                publicite.sponsorName?.equals(currentUser.username, ignoreCase = true) == true ||
                                                publicite.sponsorName == currentUser.id
                                    }
                                }
                            } else {
                                false
                            }

                            // AJOUTEZ CES LOGS POUR DÉBOGUER
                            android.util.Log.d("PubliciteDebug", "=== Publicité: ${publicite.titre} ===")
                            android.util.Log.d("PubliciteDebug", "publicite.sponsorId: ${publicite.sponsorId}")
                            android.util.Log.d("PubliciteDebug", "publicite.sponsorName: ${publicite.sponsorName}")
                            android.util.Log.d("PubliciteDebug", "currentUser.id: ${currentUser?.id}")
                            android.util.Log.d("PubliciteDebug", "currentUser.username: ${currentUser?.username}")
                            android.util.Log.d("PubliciteDebug", "currentUser.role: ${currentUser?.role}")
                            android.util.Log.d("PubliciteDebug", "isSponsor: $isSponsor")
                            android.util.Log.d("PubliciteDebug", "isOwner: $isOwner")


                            // Utiliser isOwner pour la sécurité (ou isSponsor pour tous les sponsors)
                            val showButtons = isOwner

                            android.util.Log.d("PubliciteDebug", "showButtons: $showButtons")
                            android.util.Log.d("PubliciteDebug", "===============================")
                            android.util.Log.d("PubliciteDebug", "publicite.id: ${publicite.id}")
                            android.util.Log.d("PubliciteDebug", "isOwner: $isOwner")
                            android.util.Log.d("PubliciteDebug", "publicite.id != null: ${publicite.id != null}")
                            android.util.Log.d("PubliciteDebug", "showButtons: $showButtons")

                            PubliciteCardWithActions(
                                publicite = publicite,
                                onClick = { onPubliciteClick(publicite.id ?: "") },
                                showEditDelete = showButtons,
                                onEdit = if (showButtons) {
                                    {
                                        publicite.id?.let { id ->
                                            onEditPublicite?.invoke(id)
                                        }
                                    }
                                } else null,
                                onDelete = if (showButtons) {
                                    { /* Sera géré par le dialog dans PubliciteCardWithActions */ }
                                } else null,
                                onDeleteConfirm = { id ->
                                    viewModel.deletePublicite(id) {
                                        // Suppression réussie, la liste sera automatiquement mise à jour
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
fun PartnerBrandItem(brandName: String) {
    Column(
        modifier = Modifier
            .width(80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(BlueLight),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = brandName.take(1),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = BluePrimary
            )
        }
        Text(
            text = brandName,
            fontSize = 12.sp,
            color = TextPrimary,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PartnerBrandItemPreview() {
    PartnerBrandItem(brandName = "Pizza Express")
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun PubliciteListScreenPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GreyLight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp)),
                placeholder = { Text("Rechercher une marque...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Rechercher")
                },
                trailingIcon = {
                    IconButton(onClick = { /* Filter */ }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filtres")
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = White,
                    unfocusedContainerColor = White
                )
            )
        }

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(listOf("Tout", "Nourriture", "Tech", "Loisirs", "Voyage", "Mode")) { categorie ->
                FilterChip(
                    selected = categorie == "Tout",
                    onClick = {},
                    label = { Text(categorie, fontSize = 14.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BluePrimary,
                        selectedLabelColor = White,
                        containerColor = White,
                        labelColor = TextPrimary
                    )
                )
            }
        }

        val uniqueBrands = listOf("Pizza Express", "Baristas", "TechCorp", "Foodie", "StyleUp")

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text(
                text = "Nos Marques Partenaires",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(uniqueBrands) { brand ->
                    PartnerBrandItem(brandName = brand)
                }
            }
        }

        Text(
            text = "Toutes les Promotions",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        val samplePublicite = Publicite(
            id = "1",
            titre = "2 Pizzas Achetées = 1 Offerte",
            description = "Profitez de notre offre spéciale étudiants",
            imageUrl = null,
            type = PubliciteType.PROMOTION,
            sponsorId = "sponsor1",
            sponsorName = "Pizza Express",
            categorie = Categorie.NOURRITURE,
            dateExpiration = "31 décembre 2025"
        )

        PubliciteCard(
            publicite = samplePublicite,
            onClick = {},
            showEditDelete = true,
            onEdit = { },
            onDelete = { }
        )
    }
}