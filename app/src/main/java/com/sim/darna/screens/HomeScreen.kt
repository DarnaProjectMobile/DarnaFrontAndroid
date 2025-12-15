package com.sim.darna.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sim.darna.components.EmptyStateLottie
import com.sim.darna.components.PropertyCardView
import com.sim.darna.model.Property
import com.sim.darna.navigation.Routes
import com.sim.darna.ui.theme.AppTheme
import com.sim.darna.viewmodel.PropertyViewModel
import com.sim.darna.screens.AddPubliciteScreen
import com.sim.darna.screens.ProfileScreen
import com.sim.darna.screens.PublicitesListScreen

// Modern Color Palette
object ModernColors {
    val Primary = Color(0xFFFF4B6E)
    val Secondary = Color(0xFF4C6FFF)
    val Accent = Color(0xFFFFC857)
    val Background = Color(0xFFF7F7F7)
    val CardBackground = Color.White
    val TextPrimary = Color(0xFF1A1A2E)
    val TextSecondary = Color(0xFF6B7280)
    val Border = Color(0xFFE5E7EB)
}

// ---------------------- Bottom navigation items ----------------------
sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val iconSelected: ImageVector,
    val label: String
) {
    object Home : BottomNavItem("home", Icons.Outlined.Home, Icons.Filled.Home, "Accueil")
    object Publicite : BottomNavItem("publicite", Icons.Outlined.Campaign, Icons.Filled.Campaign, "Publicités")
    object Reserve : BottomNavItem("reserve", Icons.Outlined.Star, Icons.Filled.Star, "Réserver")
    object Profile : BottomNavItem("profile", Icons.Outlined.Person, Icons.Filled.Person, "Profil")
    object Calendar : BottomNavItem("calendar", Icons.Outlined.DateRange, Icons.Filled.DateRange, "Calendrier")
}

// ---------------------- MainScreen ----------------------
@Composable
fun MainScreen(parentNavController: NavHostController) {
    val bottomNavController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavBar(bottomNavController) },
        containerColor = ModernColors.Background
    ) { padding ->
        NavHost(
            navController = bottomNavController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(BottomNavItem.Home.route) {
                HomeScreen(parentNavController)
            }

            composable(BottomNavItem.Publicite.route) {
                PublicitesListScreen(
                    onAddClick = { parentNavController.navigate(Routes.AddPublicite) },
                    onEdit = { id -> parentNavController.navigate(Routes.EditPublicite.replace("{publiciteId}", id)) },
                    onDetailClick = { id -> parentNavController.navigate(Routes.PubliciteDetail.replace("{publiciteId}", id)) }
                )
            }

            composable(BottomNavItem.Profile.route) {
                ProfileScreen(parentNavController)
            }
        }
    }
}

// ---------------------- BottomNavBar ----------------------
@Composable
fun BottomNavBar(navController: NavController) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Publicite,
        BottomNavItem.Calendar,
        BottomNavItem.Reserve,
        BottomNavItem.Profile
    )

    val navBackStack by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStack?.destination

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp),
        color = ModernColors.CardBackground
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            contentColor = ModernColors.TextSecondary,
            tonalElevation = 0.dp
        ) {
            items.forEach { item ->
                val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true

                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = if (selected) item.iconSelected else item.icon,
                            contentDescription = item.label,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            item.label,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ModernColors.Primary,
                        unselectedIconColor = ModernColors.TextSecondary,
                        selectedTextColor = ModernColors.Primary,
                        unselectedTextColor = ModernColors.TextSecondary,
                        indicatorColor = ModernColors.Primary.copy(alpha = 0.1f)
                    )
                )
            }
        }
    }
}

// ---------------------- HomeScreen ----------------------
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
    val currentUserId = prefs.getString("user_id", null)
    val currentUserRole = prefs.getString("role", "guest") ?: "guest"
    val notificationCount = com.sim.darna.notifications.NotificationStore
        .getNotifications(context)
        .size

    val viewModel: PropertyViewModel = remember {
        PropertyViewModel(context).apply {
            init(currentUserId)
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }
    var showAddPropertyForm by remember { mutableStateOf(false) }
    var editingProperty by remember { mutableStateOf<Property?>(null) }
    var propertyPendingDeletion by remember { mutableStateOf<Property?>(null) }
    var isGridView by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadProperties()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ModernColors.Background)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            item {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    // Top bar with greeting and notifications
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Bonjour 👋",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = ModernColors.TextPrimary
                            )
                            Text(
                                text = "Trouvez votre colocation idéale",
                                fontSize = 15.sp,
                                color = ModernColors.TextSecondary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        // Notification Button
                        Box {
                            Surface(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clickable { navController.navigate(Routes.Notifications) },
                                shape = CircleShape,
                                color = ModernColors.CardBackground,
                                shadowElevation = 2.dp
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Notifications,
                                    contentDescription = "Notifications",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp),
                                    tint = ModernColors.TextPrimary
                                )
                            }
                            if (notificationCount > 0) {
                                Surface(
                                    shape = CircleShape,
                                    color = ModernColors.Accent,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .align(Alignment.TopEnd)
                                        .offset(x = 2.dp, y = (-2).dp)
                                ) {
                                    Text(
                                        text = if (notificationCount > 9) "9+" else notificationCount.toString(),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.wrapContentSize(Alignment.Center),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    // Search Bar
                    SearchBar(
                        searchText = uiState.searchText,
                        onSearchTextChange = { viewModel.setSearchText(it) },
                        onFilterClick = { showFilterSheet = true }
                    )

                    // Quick Filters
                    QuickFilterButtons(
                        ownershipFilter = uiState.ownershipFilter,
                        onFilterClick = { filter -> viewModel.toggleOwnershipFilter(filter) }
                    )

                    // View Toggle & Results Count
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${uiState.filteredProperties.size} annonces",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = ModernColors.TextSecondary
                        )
                        ViewToggle(
                            isGridView = isGridView,
                            onViewChange = { isGridView = it }
                        )
                    }
                }
            }

            // Property list content
            when {
                uiState.isLoading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = ModernColors.Primary)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Chargement des annonces...",
                                    color = ModernColors.TextSecondary
                                )
                            }
                        }
                    }
                }

                uiState.error != null -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = uiState.error ?: "Erreur inconnue",
                                color = ModernColors.Primary,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }

                uiState.filteredProperties.isEmpty() -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyStateLottie(
                                title = "Aucune annonce trouvée",
                                subtitle = "Essayez de modifier votre recherche",
                                modifier = Modifier.padding(40.dp)
                            )
                        }
                    }
                }

                else -> {
                    if (isGridView) {
                        item {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                contentPadding = PaddingValues(0.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height((uiState.filteredProperties.size / 2 + 1) * 280.dp)
                            ) {
                                items(uiState.filteredProperties.size) { index ->
                                    val property = uiState.filteredProperties[index]
                                    val canManage = currentUserRole == "collocator" && property.user == currentUserId

                                    PropertyCardView(
                                        property = property,
                                        canManage = canManage,
                                        onEdit = { editingProperty = property },
                                        onDelete = { propertyPendingDeletion = property },
                                        onClick = {
                                            navController.navigate("${Routes.PropertyDetail}/${property.id}")
                                        },
                                        isGridMode = true
                                    )
                                }
                            }
                        }
                    } else {
                        items(uiState.filteredProperties.size) { index ->
                            val property = uiState.filteredProperties[index]
                            val canManage = currentUserRole == "collocator" && property.user == currentUserId

                            PropertyCardView(
                                property = property,
                                canManage = canManage,
                                onEdit = { editingProperty = property },
                                onDelete = { propertyPendingDeletion = property },
                                onClick = {
                                    navController.navigate("${Routes.PropertyDetail}/${property.id}")
                                },
                                isGridMode = false
                            )
                        }
                    }
                }
            }

            // Bottom spacing for FAB
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Floating Add Button (only for collocators)
        if (currentUserRole == "collocator") {
            FloatingActionButton(
                onClick = { showAddPropertyForm = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 80.dp),
                containerColor = ModernColors.Primary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Property",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Map Button
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
                .clickable { navController.navigate(Routes.Map) },
            shape = RoundedCornerShape(30.dp),
            color = ModernColors.TextPrimary,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = "Carte",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Voir la carte",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    // Dialogs
    if (showFilterSheet) {
        FilterSheet(
            minPrice = uiState.minPrice,
            maxPrice = uiState.maxPrice,
            onDismiss = { showFilterSheet = false },
            onApply = { min, max ->
                viewModel.setPriceFilter(min, max)
                showFilterSheet = false
            }
        )
    }

    if (showAddPropertyForm || editingProperty != null) {
        AddPropertyFormView(
            propertyToEdit = editingProperty,
            onDismiss = {
                showAddPropertyForm = false
                editingProperty = null
            },
            onPropertySaved = {
                viewModel.refreshProperties()
                showAddPropertyForm = false
                editingProperty = null
            }
        )
    }

    propertyPendingDeletion?.let { property ->
        AlertDialog(
            onDismissRequest = { propertyPendingDeletion = null },
            title = {
                Text(
                    "Supprimer l'annonce ?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Cette action supprimera définitivement « ${property.title} ».")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteProperty(
                            property,
                            onSuccess = { propertyPendingDeletion = null },
                            onError = { propertyPendingDeletion = null }
                        )
                    }
                ) {
                    Text("Supprimer", color = ModernColors.Primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { propertyPendingDeletion = null }) {
                    Text("Annuler", color = ModernColors.TextSecondary)
                }
            }
        )
    }
}

// ---------------------- SearchBar ----------------------
@Composable
fun SearchBar(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onFilterClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = searchText,
            onValueChange = onSearchTextChange,
            modifier = Modifier.weight(1f),
            placeholder = {
                Text(
                    "Rechercher une colocation...",
                    color = ModernColors.TextSecondary
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = ModernColors.TextSecondary
                )
            },
            trailingIcon = {
                if (searchText.isNotEmpty()) {
                    IconButton(onClick = { onSearchTextChange("") }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = ModernColors.TextSecondary
                        )
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = ModernColors.CardBackground,
                unfocusedContainerColor = ModernColors.CardBackground,
                focusedBorderColor = ModernColors.Primary.copy(alpha = 0.3f),
                unfocusedBorderColor = ModernColors.Border,
                cursorColor = ModernColors.Primary
            ),
            singleLine = true
        )

        Surface(
            modifier = Modifier
                .size(56.dp)
                .clickable { onFilterClick() },
            shape = RoundedCornerShape(16.dp),
            color = ModernColors.Primary,
            shadowElevation = 2.dp
        ) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = "Filter",
                tint = Color.White,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
        }
    }
}

// ---------------------- QuickFilterButtons ----------------------
@Composable
fun QuickFilterButtons(
    ownershipFilter: com.sim.darna.viewmodel.OwnershipFilter,
    onFilterClick: (com.sim.darna.viewmodel.OwnershipFilter) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ModernFilterChip(
            selected = ownershipFilter == com.sim.darna.viewmodel.OwnershipFilter.ALL,
            onClick = { onFilterClick(com.sim.darna.viewmodel.OwnershipFilter.ALL) },
            label = "Tous"
        )
        ModernFilterChip(
            selected = ownershipFilter == com.sim.darna.viewmodel.OwnershipFilter.MINE,
            onClick = { onFilterClick(com.sim.darna.viewmodel.OwnershipFilter.MINE) },
            label = "Mes annonces"
        )
        ModernFilterChip(
            selected = ownershipFilter == com.sim.darna.viewmodel.OwnershipFilter.NOT_MINE,
            onClick = { onFilterClick(com.sim.darna.viewmodel.OwnershipFilter.NOT_MINE) },
            label = "Autres"
        )
    }
}

@Composable
fun ModernFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String
) {
    Surface(
        modifier = Modifier
            .height(40.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (selected) ModernColors.Primary else ModernColors.CardBackground,
        border = if (!selected) BorderStroke(1.dp, ModernColors.Border) else null,
        shadowElevation = if (selected) 2.dp else 0.dp
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (selected) Color.White else ModernColors.TextSecondary,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

// ---------------------- ViewToggle ----------------------
@Composable
fun ViewToggle(
    isGridView: Boolean,
    onViewChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .background(ModernColors.CardBackground, RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Surface(
            modifier = Modifier
                .size(36.dp)
                .clickable { onViewChange(false) },
            shape = RoundedCornerShape(8.dp),
            color = if (!isGridView) ModernColors.Primary.copy(alpha = 0.1f) else Color.Transparent
        ) {
            Icon(
                imageVector = Icons.Default.ViewAgenda,
                contentDescription = "List View",
                tint = if (!isGridView) ModernColors.Primary else ModernColors.TextSecondary,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            )
        }
        Surface(
            modifier = Modifier
                .size(36.dp)
                .clickable { onViewChange(true) },
            shape = RoundedCornerShape(8.dp),
            color = if (isGridView) ModernColors.Primary.copy(alpha = 0.1f) else Color.Transparent
        ) {
            Icon(
                imageVector = Icons.Default.GridView,
                contentDescription = "Grid View",
                tint = if (isGridView) ModernColors.Primary else ModernColors.TextSecondary,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            )
        }
    }
}

// ---------------------- FilterSheet ----------------------
@Composable
fun FilterSheet(
    minPrice: Double?,
    maxPrice: Double?,
    onDismiss: () -> Unit,
    onApply: (Double?, Double?) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Filtres de prix",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Prix minimum: ${minPrice?.toInt() ?: "Non défini"}€",
                    color = ModernColors.TextSecondary
                )
                Text(
                    "Prix maximum: ${maxPrice?.toInt() ?: "Non défini"}€",
                    color = ModernColors.TextSecondary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onApply(minPrice, maxPrice) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ModernColors.Primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Appliquer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = ModernColors.TextSecondary)
            }
        }
    )
}

// ---------------------- PropertyCard (keeping for backward compatibility) ----------------------
@Composable
fun PropertyCard(
    title: String,
    location: String,
    price: Int,
    roommates: Int,
    area: Int,
    imageColor: Color,
    navController: NavController
) {
    var isFavorite by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ModernColors.CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                imageColor.copy(alpha = 0.7f),
                                imageColor
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Property",
                    modifier = Modifier.size(80.dp),
                    tint = Color.White.copy(alpha = 0.3f)
                )

                IconButton(
                    onClick = { isFavorite = !isFavorite },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.9f)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            modifier = Modifier.padding(8.dp),
                            tint = if (isFavorite) ModernColors.Primary else ModernColors.TextSecondary
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = ModernColors.Secondary
                ) {
                    Text(
                        text = "NOUVEAU",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}