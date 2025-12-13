package com.sim.darna.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import com.sim.darna.components.EmptyStateLottie
import com.sim.darna.components.PropertyCardView
import com.sim.darna.model.Property
import com.sim.darna.navigation.Routes
import com.sim.darna.ui.theme.AppTheme
import com.sim.darna.viewmodel.OwnershipFilter
import com.sim.darna.viewmodel.PropertyViewModel

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SearchAndFilterBar(
                searchText = uiState.searchText,
                onSearchTextChange = { viewModel.setSearchText(it) },
                onFilterClick = { showFilterSheet = true },
                onNotificationsClick = { navController.navigate(Routes.Notifications) },
                notificationCount = notificationCount,
            )

            QuickFilterButtons(
                ownershipFilter = uiState.ownershipFilter,
                onFilterClick = { filter ->
                    viewModel.toggleOwnershipFilter(filter)
                }
            )

            ViewToggle(
                isGridView = isGridView,
                onViewChange = { isGridView = it }
            )

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Chargement des annonces...")
                        }
                    }
                }

                uiState.error != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.error ?: "Erreur inconnue",
                            color = Color.Red,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                uiState.filteredProperties.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyStateLottie(
                            title = "Aucune annonce trouvée",
                            subtitle = "Essayez de modifier votre recherche ou vos filtres.",
                            modifier = Modifier.padding(40.dp)
                        )
                    }
                }

                else -> {
                    if (isGridView) {
                        LazyVerticalGrid(
                            modifier = Modifier
                                .fillMaxWidth()
                            .weight(1f),
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentPadding = PaddingValues(vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                                    isGridMode = false
                                )
                            }
                        }
                    }
                }
            }
        }

        // Map pill button
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(999.dp),
            color = AppTheme.primary,
            tonalElevation = 6.dp,
            shadowElevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .clickable { navController.navigate(Routes.Map) }
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Explore,
                    contentDescription = "Carte",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Carte",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        // Floating Add Button (only for collocators)
        if (currentUserRole == "collocator") {
            FloatingActionButton(
                onClick = { showAddPropertyForm = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 82.dp),
                containerColor = AppTheme.primary,
                shape = RoundedCornerShape(18.dp)
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
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val fieldShape = RoundedCornerShape(16.dp)

        OutlinedTextField(
            value = searchText,
            onValueChange = onSearchTextChange,
            modifier = Modifier
                .weight(1f)
                .height(54.dp),
            placeholder = { Text("Rechercher") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchText.isNotEmpty()) {
                    IconButton(onClick = { onSearchTextChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = fieldShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                cursorColor = AppTheme.primary,
                focusedTextColor = AppTheme.textPrimary,
                unfocusedTextColor = AppTheme.textPrimary
            )
        )

        IconButton(
            onClick = onFilterClick,
            modifier = Modifier
                .size(52.dp)
                .background(AppTheme.primary, RoundedCornerShape(14.dp))
        ) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = "Filter",
                tint = Color.White
            )
        }

        IconButton(
            onClick = onNotificationsClick,
            modifier = Modifier
                .size(52.dp)
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
    ownershipFilter: OwnershipFilter,
    onFilterClick: (OwnershipFilter) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        OwnershipFilterButton(
            title = "Mes annonces",
            isSelected = ownershipFilter == OwnershipFilter.MINE,
            icon = Icons.Default.Person,
            onClick = { onFilterClick(OwnershipFilter.MINE) },
            isFirst = true,
            modifier = Modifier.weight(1f)
        )

        OwnershipFilterButton(
            title = "Autre annonces",
            isSelected = ownershipFilter == OwnershipFilter.NOT_MINE,
            icon = Icons.Default.People,
            onClick = { onFilterClick(OwnershipFilter.NOT_MINE) },
            isFirst = false,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun OwnershipFilterButton(
    title: String,
    isSelected: Boolean,
    icon: ImageVector,
    onClick: () -> Unit,
    isFirst: Boolean = true,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        color = if (isSelected) AppTheme.primary else Color.White,
        shape = RoundedCornerShape(
            topStart = if (isFirst) 12.dp else 0.dp,
            bottomStart = if (isFirst) 12.dp else 0.dp,
            topEnd = if (isFirst) 0.dp else 12.dp,
            bottomEnd = if (isFirst) 0.dp else 12.dp
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isSelected) Color.Transparent else Color(0xFFE0E0E0)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = if (isSelected) Color.White else AppTheme.textPrimary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) Color.White else AppTheme.textPrimary
            )
        }
    }
}

@Composable
fun ViewToggle(
    isGridView: Boolean,
    onViewChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        ViewToggleButton(
            isSelected = !isGridView,
            icon = Icons.Default.ViewList,
            onClick = { onViewChange(false) },
            isFirst = true,
            modifier = Modifier.weight(1f)
        )

        ViewToggleButton(
            isSelected = isGridView,
            icon = Icons.Default.GridView,
            onClick = { onViewChange(true) },
            isFirst = false,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ViewToggleButton(
    isSelected: Boolean,
    icon: ImageVector,
    onClick: () -> Unit,
    isFirst: Boolean = true,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        color = if (isSelected) AppTheme.primary else Color.White,
        shape = RoundedCornerShape(
            topStart = if (isFirst) 12.dp else 0.dp,
            bottomStart = if (isFirst) 12.dp else 0.dp,
            topEnd = if (isFirst) 0.dp else 12.dp,
            bottomEnd = if (isFirst) 0.dp else 12.dp
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = Color(0xFFE0E0E0)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (isSelected) Color.White else AppTheme.textPrimary
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
    var minPriceText by remember { mutableStateOf(minPrice?.toString() ?: "") }
    var maxPriceText by remember { mutableStateOf(maxPrice?.toString() ?: "") }
    var minPriceError by remember { mutableStateOf<String?>(null) }
    var maxPriceError by remember { mutableStateOf<String?>(null) }

    fun filterNumericInput(text: String): String {
        return text.filter { it.isDigit() || it == '.' }
    }

    fun validatePrices(): Boolean {
        minPriceError = null
        maxPriceError = null

        val min = minPriceText.toDoubleOrNull()
        val max = maxPriceText.toDoubleOrNull()

        if (minPriceText.isEmpty() && maxPriceText.isEmpty()) return true

        if (minPriceText.isNotEmpty() && min == null) {
            minPriceError = "Veuillez entrer un nombre valide"
            return false
        }

        if (maxPriceText.isNotEmpty() && max == null) {
            maxPriceError = "Veuillez entrer un nombre valide"
            return false
        }

        if (min != null && max != null && min >= max) {
            minPriceError = "Le prix minimum doit être inférieur au prix maximum"
            maxPriceError = "Le prix maximum doit être supérieur au prix minimum"
            return false
        }

        return true
    }

    val isValid = validatePrices()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppTheme.card,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = AppTheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Filtrer par prix",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.textPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                OutlinedTextField(
                    value = minPriceText,
                    onValueChange = {
                        val filtered = filterNumericInput(it)
                        minPriceText = filtered
                        validatePrices()
                    },
                    label = {
                        Text(
                            "Prix minimum",
                            color = AppTheme.textSecondary
                        )
                    },
                    placeholder = { Text("Ex: 100", color = AppTheme.textSecondary.copy(alpha = 0.6f)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    isError = minPriceError != null,
                    supportingText = minPriceError?.let {
                        {
                            Text(
                                it,
                                color = Color(0xFFD32F2F),
                                fontSize = 12.sp
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = AppTheme.card,
                        unfocusedContainerColor = AppTheme.card,
                        focusedBorderColor = AppTheme.primary,
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        errorBorderColor = Color(0xFFD32F2F),
                        focusedTextColor = AppTheme.textPrimary,
                        unfocusedTextColor = AppTheme.textPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AttachMoney,
                            contentDescription = null,
                            tint = AppTheme.textSecondary
                        )
                    }
                )
                OutlinedTextField(
                    value = maxPriceText,
                    onValueChange = {
                        val filtered = filterNumericInput(it)
                        maxPriceText = filtered
                        validatePrices()
                    },
                    label = {
                        Text(
                            "Prix maximum",
                            color = AppTheme.textSecondary
                        )
                    },
                    placeholder = { Text("Ex: 1000", color = AppTheme.textSecondary.copy(alpha = 0.6f)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    isError = maxPriceError != null,
                    supportingText = maxPriceError?.let {
                        {
                            Text(
                                it,
                                color = Color(0xFFD32F2F),
                                fontSize = 12.sp
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = AppTheme.card,
                        unfocusedContainerColor = AppTheme.card,
                        focusedBorderColor = AppTheme.primary,
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        errorBorderColor = Color(0xFFD32F2F),
                        focusedTextColor = AppTheme.textPrimary,
                        unfocusedTextColor = AppTheme.textPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AttachMoney,
                            contentDescription = null,
                            tint = AppTheme.textSecondary
                        )
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isValid) {
                        val min = minPriceText.toDoubleOrNull()
                        val max = maxPriceText.toDoubleOrNull()
                        onApply(min, max)
                    }
                },
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.primary,
                    contentColor = Color.White,
                    disabledContainerColor = AppTheme.primary.copy(alpha = 0.5f),
                    disabledContentColor = Color.White.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(
                    "Appliquer",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    minPriceText = ""
                    maxPriceText = ""
                    minPriceError = null
                    maxPriceError = null
                    onApply(null, null)
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AppTheme.textPrimary
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Color(0xFFE0E0E0)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    "Réinitialiser",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }
    )
}
