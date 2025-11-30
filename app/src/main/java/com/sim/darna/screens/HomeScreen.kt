package com.sim.darna.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.hilt.navigation.compose.hiltViewModel
import com.sim.darna.navigation.Routes

// Navigation destinations
sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object Home : BottomNavItem("home", Icons.Default.Home, "Accueil")
    object Publicite : BottomNavItem("publicite", Icons.Default.Campaign, "Publicité")
    object Reserve : BottomNavItem("reserve", Icons.Default.Star, "Réserver")
    object Profile : BottomNavItem("profile", Icons.Default.Person, "Profil")
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavBar(navController) },
        containerColor = Color(0xFFF5F5F5)
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Home.route) { HomeScreen(navController) }

            composable(BottomNavItem.Publicite.route) {
                com.sim.darna.ui.screens.PubliciteListScreen(
                    navController = navController,
                    onPubliciteClick = { publiciteId ->
                        navController.navigate("publicite_detail/$publiciteId")
                    },
                    onAddPubliciteClick = {
                        navController.navigate(Routes.AddPublicite)
                    },
                    onEditPublicite = { id ->
                        navController.navigate("edit_publicite/$id")
                    },
                    onDeletePublicite = { id ->
                        navController.currentBackStackEntry?.savedStateHandle?.set("refreshOffers", true)
                    }
                )
            }

            composable(
                route = Routes.PubliciteDetail,
                arguments = listOf(navArgument("publiciteId") { type = NavType.StringType })
            ) { backStackEntry ->
                val publiciteId = backStackEntry.arguments?.getString("publiciteId") ?: ""
                com.sim.darna.ui.screens.PubliciteDetailScreen(
                    navController = navController,
                    publiciteId = publiciteId
                )
            }

            composable(Routes.AddPublicite) {
                com.sim.darna.ui.screens.AddPubliciteScreen(
                    navController = navController,
                    publiciteId = null
                )
            }

            composable(
                route = Routes.EditPublicite,
                arguments = listOf(navArgument("publiciteId") { type = NavType.StringType })
            ) { backStackEntry ->
                val publiciteId = backStackEntry.arguments?.getString("publiciteId") ?: ""
                com.sim.darna.ui.screens.AddPubliciteScreen(
                    navController = navController,
                    publiciteId = publiciteId
                )
            }

            composable(Routes.Payment) {
                val viewModel: com.sim.darna.viewmodel.PaymentViewModel = hiltViewModel()
                com.sim.darna.ui.screens.PaymentScreen(
                    navController = navController,
                    viewModel = viewModel
                )
            }

            composable(BottomNavItem.Reserve.route) { ReserveScreen() }
            composable(BottomNavItem.Profile.route) { ProfileScreen() }
        }
    }
}

@Composable
fun ProfileScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Écran Profil - En cours de développement")
    }
}

@Composable
fun ReserveScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Écran Réservation - En cours de développement")
    }
}

@Composable
fun BottomNavBar(navController: NavController) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Publicite,
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
                    navController = navController
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

// Preview pour HomeScreen
@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun HomeScreenPreview() {
    val navController = rememberNavController()
    HomeScreen(navController = navController)
}

// Preview pour BottomNavBar
@Preview(showBackground = true)
@Composable
fun BottomNavBarPreview() {
    val navController = rememberNavController()
    BottomNavBar(navController = navController)
}

// Preview pour PropertyCard
@Preview(showBackground = true, widthDp = 360)
@Composable
fun PropertyCardPreview() {
    val navController = rememberNavController()
    PropertyCard(
        title = "Studio moderne",
        location = "Paris 15ème",
        price = 650,
        roommates = 2,
        area = 35,
        imageColor = Color(0xFF0066FF),
        navController = navController
    )
}