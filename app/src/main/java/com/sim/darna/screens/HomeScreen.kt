package com.sim.darna.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

// ---------------------- Bottom navigation items ----------------------
sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object Home : BottomNavItem("home", Icons.Default.Home, "Accueil")
    object Publicite : BottomNavItem("publicite", Icons.Default.Campaign, "Publicités")
    object Reserve : BottomNavItem("reserve", Icons.Default.Star, "Réserver")
    object Profile : BottomNavItem("profile", Icons.Default.Person, "Profil")
    object Calendar : BottomNavItem("calendar", Icons.Default.DateRange, "Calendrier")
}

// ---------------------- MainScreen ----------------------
@Composable
fun MainScreen(parentNavController: NavHostController) {

    val bottomNavController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavBar(bottomNavController) },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->

        NavHost(
            navController = bottomNavController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(BottomNavItem.Home.route) {
                HomeScreen(parentNavController)   // forward parent nav
            }

            // Liste des publicités
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

    NavigationBar(
        containerColor = AppTheme.primary,
        contentColor = Color.White
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
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    unselectedIconColor = Color.White.copy(alpha = 0.7f),
                    selectedTextColor = Color.White,
                    unselectedTextColor = Color.White.copy(alpha = 0.7f),
                    indicatorColor = Color.White.copy(alpha = 0.2f)
                )
            )
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
            .background(AppTheme.background)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header / title


            // Search and filter + list
            item {
                SearchAndFilterBar(
                    searchText = uiState.searchText,
                    onSearchTextChange = { viewModel.setSearchText(it) },
                    onFilterClick = { showFilterSheet = true },
                    onNotificationsClick = { navController.navigate(Routes.Notifications) },
                    notificationCount = notificationCount,
                )

                Spacer(modifier = Modifier.height(16.dp))

                QuickFilterButtons(
                    ownershipFilter = uiState.ownershipFilter,
                    onFilterClick = { filter ->
                        viewModel.toggleOwnershipFilter(filter)
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                ViewToggle(
                    isGridView = isGridView,
                    onViewChange = { isGridView = it }
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Property list content
            when {
                uiState.isLoading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Chargement des annonces...")
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
                                color = Color.Red,
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
                                .height(220.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyStateLottie(
                                title = "Aucune annonce trouvée",
                                subtitle = "Essayez de modifier votre recherche ou vos filtres.",
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
                                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(uiState.filteredProperties.size) { index ->
                                    val property = uiState.filteredProperties[index]
                                    val canManage =
                                        currentUserRole == "collocator" && property.user == currentUserId

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
                            val canManage =
                                currentUserRole == "collocator" && property.user == currentUserId

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
        }

        // Floating Add Button (only for collocators)
        if (currentUserRole == "collocator") {
            FloatingActionButton(
                onClick = { showAddPropertyForm = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                containerColor = AppTheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Property",
                    tint = Color.White
                )
            }
        }
    }

    // Filter Sheet
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

    // Add/Edit Property Form
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

    // Delete Confirmation Dialog
    propertyPendingDeletion?.let { property ->
        AlertDialog(
            onDismissRequest = { propertyPendingDeletion = null },
            title = { Text("Supprimer l'annonce ?") },
            text = { Text("Cette action supprimera définitivement « ${property.title} ».") },
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
                    Text("Supprimer", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { propertyPendingDeletion = null }) {
                    Text("Annuler")
                }
            }
        )
    }
}

// ---------------------- Search & Filter bar ----------------------
@Composable
fun SearchAndFilterBar(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    notificationCount: Int,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search field
        OutlinedTextField(
            value = searchText,
            onValueChange = onSearchTextChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Rechercher") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            trailingIcon = {
                if (searchText.isNotEmpty()) {
                    IconButton(onClick = { onSearchTextChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = AppTheme.card,
                unfocusedContainerColor = AppTheme.card
            )
        )

        // Filter button
        IconButton(
            onClick = onFilterClick,
            modifier = Modifier
                .size(48.dp)
                .background(AppTheme.primary, RoundedCornerShape(14.dp))
        ) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = "Filter",
                tint = Color.White
            )
        }

        // Notifications button
        IconButton(
            onClick = onNotificationsClick,
            modifier = Modifier
                .size(48.dp)
                .background(Color(0xFF00BFA5), RoundedCornerShape(14.dp))
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
                if (notificationCount > 0) {
                    Surface(
                        shape = CircleShape,
                        color = Color.Red,
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 2.dp, y = (-6).dp)
                    ) {
                        Text(
                            text = if (notificationCount > 9) "9+" else notificationCount.toString(),
                            color = Color.White,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(2.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// ---------------------- PropertyCard ----------------------
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // Image placeholder
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

                // Favorite button
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
                            tint = if (isFavorite) Color(0xFFFF6B6B) else Color(0xFF757575)
                        )
                    }
                }

                // New Badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF00C853)
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

            // Content
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF757575)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = location,
                        fontSize = 14.sp,
                        color = Color(0xFF757575)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    InfoChip(icon = Icons.Default.Person, text = "$roommates pers.")
                    InfoChip(icon = Icons.Default.Home, text = "${area}m²")
                    Surface(
                        color = Color(0xFF0066FF).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color(0xFFFFC107)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "4.8",
                                fontSize = 12.sp,
                                color = Color(0xFF1A1A1A),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "À partir de",
                            fontSize = 12.sp,
                            color = Color(0xFF757575)
                        )
                        Text(
                            text = "${price}€/mois",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0066FF)
                        )
                    }

                    Button(
                        onClick = { /* Navigate to details if needed */ },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0066FF)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Voir détails", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------- InfoChip ----------------------
@Composable
fun InfoChip(icon: ImageVector, text: String) {
    Surface(
        color = Color(0xFFF5F5F5),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Color(0xFF757575)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                fontSize = 12.sp,
                color = Color(0xFF757575)
            )
        }
    }
}

// ---------------------- CategoryCard ----------------------
@Composable
fun CategoryCard(icon: ImageVector, label: String, color: Color) {
    Card(
        modifier = Modifier
            .width(110.dp)
            .height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(32.dp),
                tint = color
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = color,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ---------------------- Placeholder Screens ----------------------
// Note: These screens are implemented in their respective files
// ProfileScreen - implemented in ProfileScreen.kt
// PublicitesListScreen - implemented in PublicitesListScreen.kt
// AddPubliciteScreen - implemented in AddPubliciteScreen.kt

// ---------------------- QuickFilterButtons ----------------------
@Composable
fun QuickFilterButtons(
    ownershipFilter: com.sim.darna.viewmodel.OwnershipFilter,
    onFilterClick: (com.sim.darna.viewmodel.OwnershipFilter) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = ownershipFilter == com.sim.darna.viewmodel.OwnershipFilter.ALL,
            onClick = { onFilterClick(com.sim.darna.viewmodel.OwnershipFilter.ALL) },
            label = { Text("Tous") }
        )
        FilterChip(
            selected = ownershipFilter == com.sim.darna.viewmodel.OwnershipFilter.MINE,
            onClick = { onFilterClick(com.sim.darna.viewmodel.OwnershipFilter.MINE) },
            label = { Text("Mes annonces") }
        )
        FilterChip(
            selected = ownershipFilter == com.sim.darna.viewmodel.OwnershipFilter.NOT_MINE,
            onClick = { onFilterClick(com.sim.darna.viewmodel.OwnershipFilter.NOT_MINE) },
            label = { Text("Annonces des autres") }
        )
    }
}

// ---------------------- ViewToggle ----------------------
@Composable
fun ViewToggle(
    isGridView: Boolean,
    onViewChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        IconButton(
            onClick = { onViewChange(false) }
        ) {
            Icon(
                imageVector = if (!isGridView) Icons.Default.List else Icons.Default.List,
                contentDescription = "List View"
            )
        }
        IconButton(
            onClick = { onViewChange(true) }
        ) {
            Icon(
                imageVector = if (isGridView) Icons.Default.GridView else Icons.Default.GridView,
                contentDescription = "Grid View"
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
    // Simple implementation - you may want to enhance this with a proper sheet
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filtres") },
        text = {
            Column {
                Text("Prix minimum: $minPrice")
                Text("Prix maximum: $maxPrice")
            }
        },
        confirmButton = {
            TextButton(onClick = { onApply(minPrice, maxPrice) }) {
                Text("Appliquer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

// ---------------------- FilterChip ----------------------
@Composable
fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.height(32.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) AppTheme.primary else Color.Transparent,
        border = if (!selected) BorderStroke(1.dp, AppTheme.primary) else null
    ) {
        TextButton(
            onClick = onClick,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
        ) {
            CompositionLocalProvider(
                LocalContentColor provides if (selected) Color.White else AppTheme.primary
            ) {
                label()
            }
        }
    }
}
