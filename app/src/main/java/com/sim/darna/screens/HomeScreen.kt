package com.sim.darna.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sim.darna.navigation.Routes
import com.sim.darna.factory.VisiteVmFactory
import com.sim.darna.visite.VisiteViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.sim.darna.auth.SessionManager
import com.sim.darna.network.NetworkConfig
import java.util.Locale

// Navigation destinations
sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object Home : BottomNavItem("home", Icons.Default.Home, "Accueil")
    object Calendar : BottomNavItem("calendar", Icons.Default.EventNote, "Mes visites")
    object Reserve : BottomNavItem("reserve", Icons.Default.Star, "Réserver")
    object Profile : BottomNavItem("profile", Icons.Default.Person, "Profil")
    
    // Items spécifiques pour les colocataires
    object VisitsRequests : BottomNavItem("visits_requests", Icons.Default.EventNote, "Demandes")
    object ManageProperties : BottomNavItem("manage_properties", Icons.Default.Home, "Mes logements")
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context.applicationContext) }
    val userSession = sessionManager.sessionFlow.collectAsState(initial = null).value
    val isCollocator = userSession?.role?.lowercase() == "collocator"
    
    val visiteViewModel: VisiteViewModel = viewModel(
        factory = VisiteVmFactory(NetworkConfig.BASE_URL, sessionManager)
    )

    Scaffold(
        bottomBar = { 
            if (isCollocator) {
                CollocatorBottomNavBar(navController)
            } else {
                BottomNavBar(navController)
            }
        },
        containerColor = Color(0xFFF5F5F5)
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (isCollocator) BottomNavItem.VisitsRequests.route else BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Écrans pour les clients
            composable(BottomNavItem.Home.route) { HomeScreen(navController) }
            composable(BottomNavItem.Calendar.route) { MyVisitsScreen(viewModel = visiteViewModel) }
            composable(BottomNavItem.Reserve.route) { ReserveScreen(navController, viewModel = visiteViewModel) }
            composable(BottomNavItem.Profile.route) { ProfileScreen(navController, sessionManager) }
            
            // Écrans pour les colocataires
            composable(BottomNavItem.VisitsRequests.route) { 
                CollocatorVisitsScreen(viewModel = visiteViewModel) 
            }
            composable(BottomNavItem.ManageProperties.route) { 
                ManagePropertiesScreen() 
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReserveScreen(navController: NavController, viewModel: VisiteViewModel) {
    val context = LocalContext.current
    val uiState = viewModel.state.collectAsState().value

    // Sélection de la date
    val datePickerState = androidx.compose.material3.rememberDatePickerState()

    // Liste de logements (maquette pour l'instant)
    val properties = listOf(
        "Appartement 3 pièces - Centre Ville",
        "Chambre dans T4 - Marseille 8e",
        "Studio meublé - Lyon"
    )
    var selectedProperty by remember { mutableStateOf(properties.first()) }
    var propertyMenuExpanded by remember { mutableStateOf(false) }

    // Créneaux horaires possibles
    val timeSlots = remember {
        listOf(
            9 to 0, 9 to 30, 10 to 0, 10 to 30,
            11 to 0, 11 to 30, 14 to 0, 14 to 30,
            15 to 0, 15 to 30, 16 to 0, 16 to 30
        )
    }
    var selectedHour by remember { mutableStateOf(14) }
    var selectedMinute by remember { mutableStateOf(0) }
    var timeMenuExpanded by remember { mutableStateOf(false) }

    var notes by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            if (it.contains("succès", ignoreCase = true)) {
                navController.navigate(BottomNavItem.Calendar.route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
            viewModel.clearFeedback()
            notes = ""
            contactPhone = ""
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearFeedback()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Réserver une visite",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )

        // Carte : sélection de la date
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFEFF6FF))
                        .padding(12.dp)
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFF6B00F5))
                    Spacer(Modifier.width(8.dp))
                    Text("Sélectionner une date", fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                }
                Spacer(Modifier.height(12.dp))
                androidx.compose.material3.DatePicker(state = datePickerState)
            }
        }

        // Carte : détails de la visite (logement + horaire + contact + notes)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFEFF6FF))
                        .padding(12.dp)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF6B00F5))
                    Spacer(Modifier.width(8.dp))
                    Text("Détails de la visite", fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                }

                Spacer(Modifier.height(12.dp))

                // Logement (dropdown)
                Text("Logement *", style = MaterialTheme.typography.labelLarge, color = Color(0xFF334155))
                Spacer(Modifier.height(6.dp))
                Box {
                    OutlinedButton(onClick = { propertyMenuExpanded = true }) {
                        Text(selectedProperty)
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = propertyMenuExpanded,
                        onDismissRequest = { propertyMenuExpanded = false }
                    ) {
                        properties.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    selectedProperty = option
                                    propertyMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Horaire (dropdown)
                Text("Horaire *", style = MaterialTheme.typography.labelLarge, color = Color(0xFF334155))
                Spacer(Modifier.height(6.dp))
                Box {
                    val currentLabel = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)
                    OutlinedButton(onClick = { timeMenuExpanded = true }) {
                        Icon(Icons.Default.AccessTime, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(currentLabel)
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = timeMenuExpanded,
                        onDismissRequest = { timeMenuExpanded = false }
                    ) {
                        timeSlots.forEach { (h, m) ->
                            val label = String.format(Locale.getDefault(), "%02d:%02d", h, m)
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    selectedHour = h
                                    selectedMinute = m
                                    timeMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = contactPhone,
                    onValueChange = { contactPhone = it },
                    label = { Text("Téléphone de contact (optionnel)") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optionnel)") },
                    leadingIcon = { Icon(Icons.Default.Note, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                )
            }
        }

        if (uiState.isSubmitting) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        // Barre violette de confirmation
        val enabled = datePickerState.selectedDateMillis != null && !uiState.isSubmitting
        val gradient = androidx.compose.ui.graphics.Brush.horizontalGradient(
            colors = listOf(Color(0xFF6B00F5), Color(0xFF9333EA))
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (enabled) gradient else androidx.compose.ui.graphics.Brush.horizontalGradient(
                        listOf(Color(0xFFCBD5E1), Color(0xFFE2E8F0))
                    )
                )
                .let { base ->
                    if (enabled) base.clickable {
                        val selectedMillis = datePickerState.selectedDateMillis
                        if (selectedMillis == null) {
                            Toast.makeText(context, "Veuillez choisir une date", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.createVisite(
                                logementId = selectedProperty,
                                dateMillis = selectedMillis,
                                hour = selectedHour,
                                minute = selectedMinute,
                                notes = notes,
                                contactPhone = contactPhone
                            )
                        }
                    } else base
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (uiState.isSubmitting) "Envoi en cours..." else "Confirmer la réservation",
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun CollocatorBottomNavBar(navController: NavController) {
    val items = listOf(
        BottomNavItem.VisitsRequests,
        BottomNavItem.ManageProperties,
        BottomNavItem.Profile
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
        shadowElevation = 4.dp,
        color = Color.White
    ) {
        NavigationBar(
            modifier = Modifier.height(80.dp),
            containerColor = Color.White,
            tonalElevation = 0.dp
        ) {
            items.forEach { item ->
                val isSelected = currentDestination?.hierarchy?.any {
                    it.route == item.route
                } == true

                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.1f else 1f,
                    animationSpec = tween(durationMillis = 200),
                    label = "scale"
                )

                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            modifier = Modifier
                                .scale(scale)
                                .size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    selected = isSelected,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0066FF),
                        selectedTextColor = Color(0xFF0066FF),
                        unselectedIconColor = Color(0xFF9E9E9E),
                        unselectedTextColor = Color(0xFF9E9E9E),
                        indicatorColor = Color(0xFF0066FF).copy(alpha = 0.1f)
                    )
                )
            }
        }
    }
}

@Composable
fun BottomNavBar(navController: NavController) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Calendar,
        BottomNavItem.Reserve,
        BottomNavItem.Profile
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
        shadowElevation = 4.dp,
        color = Color.White
    ) {
        NavigationBar(
            modifier = Modifier.height(80.dp),
            containerColor = Color.White,
            tonalElevation = 0.dp
        ) {
            items.forEach { item ->
                val isSelected = currentDestination?.hierarchy?.any {
                    it.route == item.route
                } == true

                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.1f else 1f,
                    animationSpec = tween(durationMillis = 200),
                    label = "scale"
                )

                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            modifier = Modifier
                                .scale(scale)
                                .size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    selected = isSelected,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0066FF),
                        selectedTextColor = Color(0xFF0066FF),
                        unselectedIconColor = Color(0xFF9E9E9E),
                        unselectedTextColor = Color(0xFF9E9E9E),
                        indicatorColor = Color(0xFF0066FF).copy(alpha = 0.1f)
                    )
                )
            }
        }
    }
}

// Home Screen
@Composable
fun HomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Header Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Découvrir",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Trouvez votre colocation idéale",
                        fontSize = 14.sp,
                        color = Color(0xFF757575)
                    )
                }

                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF0066FF).copy(alpha = 0.1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        modifier = Modifier.padding(12.dp),
                        tint = Color(0xFF0066FF)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Search Bar
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF9E9E9E)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Rechercher une colocation...",
                        color = Color(0xFF9E9E9E),
                        fontSize = 16.sp
                    )
                }
            }
        }

        // Content Section
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Categories Section
            item {
                Text(
                    text = "Catégories",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CategoryCard(
                        icon = Icons.Default.Home,
                        label = "Studios",
                        color = Color(0xFF0066FF)
                    )
                    CategoryCard(
                        icon = Icons.Default.Build,
                        label = "Maisons",
                        color = Color(0xFFFF6D00)
                    )
                }
            }

            // Featured Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "À la une",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    TextButton(onClick = { }) {
                        Text(
                            text = "Voir tout",
                            color = Color(0xFF0066FF),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Property Cards
            items(3) { index ->
                PropertyCard(
                    title = when (index) {
                        0 -> "Colocation moderne à Paris"
                        1 -> "Studio lumineux Bastille"
                        else -> "Appartement spacieux Marais"
                    },
                    location = when (index) {
                        0 -> "75011 - Bastille"
                        1 -> "75012 - Nation"
                        else -> "75003 - Le Marais"
                    },
                    price = when (index) {
                        0 -> 650
                        1 -> 850
                        else -> 1200
                    },
                    roommates = when (index) {
                        0 -> 3
                        1 -> 1
                        else -> 4
                    },
                    area = when (index) {
                        0 -> 85
                        1 -> 45
                        else -> 120
                    },
                    imageColor = when (index) {
                        0 -> Color(0xFF4A90E2)
                        1 -> Color(0xFF50C878)
                        else -> Color(0xFFFF6B6B)
                    },
                    navController = navController // ✅ added
                )
            }
        }
    }
}

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

            // 🖼️ Image placeholder with gradient
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

                // ❤️ Favorite Button
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

                // 🟢 New Badge
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

            // 🏡 Content Section
            Column(modifier = Modifier.padding(16.dp)) {

                // Title + Location
                Column(modifier = Modifier.weight(1f)) {
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
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Info Chips (Roommates, Area, Rating)
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

                // 💶 Price and Details Button
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
                        onClick = { navController.navigate(Routes.PropertyDetail) },
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

@Composable
fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
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

// Écran pour les colocataires - Gestion des demandes de visite
@Composable
fun CollocatorVisitsScreen(viewModel: VisiteViewModel) {
    val context = LocalContext.current
    val uiState = viewModel.state.collectAsState().value

    LaunchedEffect(Unit) {
        viewModel.loadLogementsVisites()
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearFeedback()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearFeedback()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Demandes de visite",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = "Gérez les demandes de visite",
                    color = Color(0xFF64748B),
                    fontSize = 14.sp
                )
            }
            IconButton(
                onClick = { viewModel.loadLogementsVisites(force = true) },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Rafraîchir",
                    tint = Color(0xFF0066FF)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (uiState.isSubmitting) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF0066FF),
                trackColor = Color(0xFFE2E8F0)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        when {
            uiState.isLoadingList && uiState.visites.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF0066FF))
                }
            }

            uiState.visites.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.EventBusy,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Aucune demande de visite",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(uiState.visites, key = { it.id ?: it.dateVisite ?: "" }) { visite ->
                        CollocatorVisitCard(
                            visite = visite,
                            onAccept = { id -> viewModel.acceptVisite(id) },
                            onReject = { id -> viewModel.rejectVisite(id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CollocatorVisitCard(
    visite: com.sim.darna.visite.VisiteResponse,
    onAccept: (String) -> Unit,
    onReject: (String) -> Unit
) {
    val statusChip = mapStatusForCollocator(visite.status)
    val isPending = visite.status?.lowercase() == "pending"
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Logement: ${visite.logementId ?: "Inconnu"}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatDateForCollocator(visite.dateVisite),
                        color = Color(0xFF64748B),
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                SurfaceChipForCollocator(
                    icon = statusChip.icon,
                    label = statusChip.label,
                    color = statusChip.color
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                InfoRowForCollocator(label = "Heure", value = formatTimeForCollocator(visite.dateVisite))
                if (!visite.contactPhone.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRowForCollocator(label = "Contact", value = visite.contactPhone)
                }
                if (!visite.notes.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRowForCollocator(label = "Notes", value = visite.notes)
                }
            }

            if (isPending) {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { visite.id?.let(onAccept) },
                        enabled = visite.id != null,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Accepter", fontSize = 14.sp)
                    }

                    Button(
                        onClick = { visite.id?.let(onReject) },
                        enabled = visite.id != null,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF4444)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Refuser", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

// Écran de gestion des logements (placeholder)
@Composable
fun ManagePropertiesScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Home,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color(0xFF94A3B8)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Gestion des logements",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Fonctionnalité à venir",
            fontSize = 14.sp,
            color = Color(0xFF64748B)
        )
    }
}

// Fonctions utilitaires pour les colocataires
private fun formatDateForCollocator(dateString: String?): String {
    val date = parseIsoToMillisForCollocator(dateString) ?: return "-"
    val formatter = java.text.SimpleDateFormat("EEEE d MMMM yyyy", java.util.Locale.getDefault())
    return formatter.format(date).replaceFirstChar { it.uppercase() }
}

private fun formatTimeForCollocator(dateString: String?): String {
    val calendar = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
    val millis = parseIsoToMillisForCollocator(dateString)
    if (millis != null) {
        calendar.timeInMillis = millis
    }
    val formatter = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
    return formatter.format(calendar.time)
}

private fun parseIsoToMillisForCollocator(dateString: String?): Long? {
    if (dateString.isNullOrBlank()) return null
    return try {
        val parser = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault()).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }
        parser.parse(dateString)?.time
    } catch (_: Exception) {
        null
    }
}

private fun mapStatusForCollocator(status: String?): StatusChipDataForCollocator {
    return when (status?.lowercase()) {
        "confirmed" -> StatusChipDataForCollocator("Acceptée", Color(0xFF10B981), Icons.Default.CheckCircle)
        "cancelled", "refused" -> StatusChipDataForCollocator("Refusée", Color(0xFFEF4444), Icons.Default.Cancel)
        "completed" -> StatusChipDataForCollocator("Terminée", Color(0xFF3B82F6), Icons.Default.TaskAlt)
        else -> StatusChipDataForCollocator("En attente", Color(0xFFF59E0B), Icons.Default.Schedule)
    }
}

private data class StatusChipDataForCollocator(
    val label: String,
    val color: Color,
    val icon: ImageVector
)

@Composable
private fun SurfaceChipForCollocator(icon: ImageVector, label: String, color: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, color = color, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun InfoRowForCollocator(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color(0xFF64748B), fontSize = 14.sp)
        Text(text = value, fontWeight = FontWeight.Medium, color = Color(0xFF334155))
    }
}



