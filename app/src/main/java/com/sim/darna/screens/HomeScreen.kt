package com.sim.darna.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Refresh
// Import wildcard pour avoir accès à ExposedDropdownMenu (fonction d'extension)
import androidx.compose.material3.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sim.darna.navigation.Routes
import com.sim.darna.factory.VisiteVmFactory
import com.sim.darna.visite.VisiteViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.sim.darna.auth.SessionManager
import com.sim.darna.network.NetworkConfig
import com.sim.darna.ui.components.AppColors
import com.sim.darna.ui.components.AppRadius
import com.sim.darna.ui.components.AppSpacing
import com.sim.darna.ui.components.ElevatedCard
import com.sim.darna.ui.components.EmptyStateCard
import com.sim.darna.ui.components.FeedbackBanner
import com.sim.darna.ui.components.KeyValueRow
import com.sim.darna.ui.components.PrimaryActionButton
import com.sim.darna.ui.components.SecondaryActionButton
import com.sim.darna.ui.components.SkeletonBox
import com.sim.darna.ui.components.StatusPill
import com.sim.darna.ui.components.TimeSlotRow
import com.sim.darna.ui.components.AnimatedPageTransition
import com.sim.darna.ui.components.ConfirmationDialog
import com.sim.darna.ui.components.defaultAnimationSpec
import com.sim.darna.ui.components.defaultSlideAnimationSpec
import com.sim.darna.ui.components.AppElevation
import com.sim.darna.ui.components.fastAnimationSpec
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.text.SimpleDateFormat
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
    object CollocatorHome : BottomNavItem("collocator_home", Icons.Default.Home, "Accueil")
    object VisitsRequests : BottomNavItem("visits_requests", Icons.Default.EventNote, "Demandes")
    object Reviews : BottomNavItem("reviews", Icons.Default.Star, "Évaluations")
}

data class LogementOption(
    val id: String,
    val label: String
)

@Composable
fun MainScreen(parentNavController: NavHostController? = null) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context.applicationContext) }
    val userSession = sessionManager.sessionFlow.collectAsState(initial = null).value
    val isCollocator = userSession?.role?.lowercase() == "collocator"
    val baseUrl = remember { NetworkConfig.getBaseUrl(context.applicationContext) }
    
    val visiteViewModel: VisiteViewModel = viewModel(
        factory = VisiteVmFactory(baseUrl, sessionManager)
    )
    
    val logementViewModel: com.sim.darna.logement.LogementViewModel = viewModel(
        factory = com.sim.darna.factory.LogementVmFactory(baseUrl, sessionManager)
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
            startDestination = if (isCollocator) BottomNavItem.CollocatorHome.route else BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Écrans pour les clients
            composable(BottomNavItem.Home.route) { HomeScreen(navController) }
            composable(BottomNavItem.Calendar.route) { MyVisitsScreen(viewModel = visiteViewModel) }
            composable(BottomNavItem.Reserve.route) { 
                ReserveScreen(
                    navController, 
                    visiteViewModel = visiteViewModel,
                    logementViewModel = logementViewModel
                ) 
            }
            composable(BottomNavItem.Profile.route) { 
                ProfileScreen(
                    navController = navController, 
                    sessionManager = sessionManager,
                    parentNavController = parentNavController
                ) 
            }
            
            // Écrans pour les colocataires
            composable(BottomNavItem.CollocatorHome.route) { 
                CollocatorHomeScreen(navController = navController) 
            }
            composable(BottomNavItem.VisitsRequests.route) { 
                CollocatorVisitsScreen(viewModel = visiteViewModel) 
            }
            composable(BottomNavItem.Reviews.route) { 
                AllReviewsScreen(viewModel = visiteViewModel, navController = navController) 
            }
            
            // Écran de notifications (accessible depuis HomeScreen)
            composable("notifications") {
                val notificationViewModel: com.sim.darna.firebase.FirebaseNotificationViewModel = viewModel(
                    factory = com.sim.darna.factory.FirebaseNotificationVmFactory(baseUrl, sessionManager)
                )
                NotificationsScreen(navController = navController, viewModel = notificationViewModel)
            }
            
            // Écran de détails de notification (accessible depuis les notifications)
            composable(
                route = "notification_detail/{notificationId}",
                arguments = listOf(navArgument("notificationId") { 
                    type = NavType.StringType 
                })
            ) { backStackEntry ->
                val notificationId = backStackEntry.arguments?.getString("notificationId") ?: ""
                
                if (notificationId.isEmpty()) {
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                    return@composable
                }
                
                val notificationViewModel: com.sim.darna.firebase.FirebaseNotificationViewModel = viewModel(
                    factory = com.sim.darna.factory.FirebaseNotificationVmFactory(baseUrl, sessionManager)
                )
                
                val uiState by notificationViewModel.state.collectAsState()
                val selectedNotification by notificationViewModel.selectedNotification.collectAsState()
                
                // Charger la notification par ID
                LaunchedEffect(notificationId) {
                    try {
                        if (notificationId.isNotEmpty()) {
                            notificationViewModel.loadNotificationById(notificationId)
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("HomeScreen", "Erreur lors du chargement de la notification", e)
                    }
                }
                
                // Afficher le contenu selon l'état
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .fillMaxSize()
                                .wrapContentSize(Alignment.Center)
                        )
                    }
                    selectedNotification != null && selectedNotification!!.id != null -> {
                        NotificationDetailScreen(
                            navController = navController,
                            notification = selectedNotification!!
                        )
                    }
                    uiState.error != null -> {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(
                                        text = "Erreur",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        text = uiState.error ?: "Notification introuvable",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Button(
                                        onClick = { navController.popBackStack() }
                                    ) {
                                        Text("Retour")
                                    }
                                }
                            }
                        }
                    }
                    else -> {
                        var hasShownError by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(2000)
                            if (selectedNotification == null && !uiState.isLoading) {
                                hasShownError = true
                                kotlinx.coroutines.delay(500)
                                navController.popBackStack()
                            }
                        }
                        
                        if (hasShownError) {
                            Box(modifier = Modifier.fillMaxSize())
                        } else {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .wrapContentSize(Alignment.Center)
                            )
                        }
                    }
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReserveScreen(
    navController: NavController, 
    visiteViewModel: VisiteViewModel,
    logementViewModel: com.sim.darna.logement.LogementViewModel
) {
    val context = LocalContext.current
    val uiState = visiteViewModel.state.collectAsState().value
    val logementUiState = logementViewModel.state.collectAsState().value
    val datePickerState = rememberDatePickerState()
    val scrollState = rememberScrollState()

    // Charger les logements au démarrage et recharger périodiquement
    LaunchedEffect(Unit) {
        // Charger immédiatement
        logementViewModel.loadLogements(force = true)
        // Recharger automatiquement toutes les 20 secondes pour avoir les nouveaux logements
        kotlinx.coroutines.delay(20000) // Attendre 20 secondes avant le premier rechargement
        while (true) {
            logementViewModel.loadLogements(force = true)
            kotlinx.coroutines.delay(20000) // Recharger toutes les 20 secondes
        }
    }
    
    // Recharger les logements si erreur
    LaunchedEffect(logementUiState.error) {
        logementUiState.error?.let {
            // Réessayer après 2 secondes en cas d'erreur
            kotlinx.coroutines.delay(2000)
            logementViewModel.loadLogements(force = true)
        }
    }

    // Logements mock pour la sélection
    val mockLogements = remember {
        listOf(
            com.sim.darna.logement.LogementResponse(
                id = "mock-1",
                annonceId = "appartement-3-pieces-centre-ville",
                ownerId = "mock-owner",
                title = "Appartement 3 pièces - Centre Ville",
                description = "Bel appartement de 3 pièces situé en plein centre-ville",
                address = "Centre Ville",
                price = 450.0,
                rooms = 3,
                surface = 75.0,
                available = true,
                images = null,
                location = com.sim.darna.logement.Location(36.8065, 10.1815),
                createdAt = null,
                updatedAt = null
            ),
            com.sim.darna.logement.LogementResponse(
                id = "mock-2",
                annonceId = "studio-meuble-lyon",
                ownerId = "mock-owner",
                title = "Studio meublé - Lyon",
                description = "Studio meublé et équipé, idéal pour étudiant",
                address = "Lyon",
                price = 380.0,
                rooms = 1,
                surface = 25.0,
                available = true,
                images = null,
                location = com.sim.darna.logement.Location(45.7640, 4.8357),
                createdAt = null,
                updatedAt = null
            ),
            com.sim.darna.logement.LogementResponse(
                id = "mock-3",
                annonceId = "chambre-t4-marseille-8e",
                ownerId = "mock-owner",
                title = "Chambre dans T4 - Marseille 8e",
                description = "Chambre disponible dans un appartement T4 partagé",
                address = "Marseille 8e",
                price = 320.0,
                rooms = 1,
                surface = 15.0,
                available = true,
                images = null,
                location = com.sim.darna.logement.Location(43.2503, 5.3845),
                createdAt = null,
                updatedAt = null
            ),
            com.sim.darna.logement.LogementResponse(
                id = "mock-4",
                annonceId = "studio-meuble-lyon-2",
                ownerId = "mock-owner",
                title = "Studio meublé - Lyon",
                description = "Studio moderne et fonctionnel",
                address = "Lyon",
                price = 400.0,
                rooms = 1,
                surface = 28.0,
                available = true,
                images = null,
                location = com.sim.darna.logement.Location(45.7500, 4.8500),
                createdAt = null,
                updatedAt = null
            )
        )
    }

    // Convertir les logements en LogementOption avec format amélioré
    // Combiner les logements de l'API avec les logements mock
    val properties = remember(logementUiState.logements) {
        val apiLogements = logementUiState.logements.map { logement ->
            LogementOption(
                id = logement.id ?: "",
                label = formatLogementLabel(logement)
            )
        }
        val mockLogementsOptions = mockLogements.map { logement ->
            LogementOption(
                id = logement.id ?: "",
                label = formatLogementLabel(logement)
            )
        }
        // Combiner les deux listes (API en premier, puis mock)
        apiLogements + mockLogementsOptions
    }
    
    var selectedProperty by remember { mutableStateOf<LogementOption?>(null) }
    var selectedHour by remember { mutableStateOf(14) }
    var selectedMinute by remember { mutableStateOf(0) }
    var notes by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf<String?>(null) }
    var showSuccessFeedback by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var showConfirmationSuccessDialog by remember { mutableStateOf(false) }

    data class PendingReservation(
        val logementId: String,
        val dateMillis: Long,
        val hour: Int,
        val minute: Int,
        val notes: String?,
        val contactPhone: String?
    )
    var pendingReservation by remember { mutableStateOf<PendingReservation?>(null) }

    val selectedDateLabel by remember(datePickerState.selectedDateMillis) {
        mutableStateOf(formatReservationDate(datePickerState.selectedDateMillis))
    }
    val selectedTimeLabel by remember(selectedHour, selectedMinute) {
        mutableStateOf(String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute))
    }
    val timeSlots = remember {
        listOf(
            9 to 0, 9 to 30, 10 to 0, 10 to 30,
            11 to 0, 11 to 30, 14 to 0, 14 to 30,
            15 to 0, 15 to 30, 16 to 0, 16 to 30
        )
    }
    val isFormValid by remember {
        derivedStateOf { 
            selectedProperty != null &&
            datePickerState.selectedDateMillis != null && 
            !uiState.isSubmitting &&
            datePickerState.selectedDateMillis != null &&
            datePickerState.selectedDateMillis!! >= System.currentTimeMillis() - 86400000 // Pas dans le passé
        }
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            if (it.contains("succès", ignoreCase = true)) {
                showSuccessFeedback = true
                delay(2000)
                navController.navigate(BottomNavItem.Calendar.route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            } else {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
            visiteViewModel.clearFeedback()
            notes = ""
            contactPhone = ""
            selectedProperty = null
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            formError = it
            visiteViewModel.clearFeedback()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Header minimaliste
            AnimatedPageTransition(visible = true) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.md, vertical = AppSpacing.md),
                    color = AppColors.primary.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, AppColors.primary.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(AppSpacing.md),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(24.dp)
                                    .background(AppColors.primary)
                            )
                            Text(
                                text = "Nouvelle réservation",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.textPrimary
                            )
                        }
                        Text(
                            text = "Remplissez le formulaire ci-dessous pour réserver une visite",
                            fontSize = 13.sp,
                            color = AppColors.textSecondary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Progress Indicator avec animation
            AnimatedVisibility(
                visible = uiState.isSubmitting,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.lg)
                        .padding(top = AppSpacing.md),
                    color = AppColors.primary,
                    trackColor = AppColors.divider
                )
            }

            // Error Banner avec animation
            AnimatedVisibility(
                visible = formError != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
            ) {
                formError?.let {
                    FeedbackBanner(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppSpacing.lg)
                            .padding(top = AppSpacing.md),
                        message = it,
                        isError = true,
                        onDismiss = { formError = null }
                    )
                }
            }

            // Success Feedback
            AnimatedVisibility(
                visible = showSuccessFeedback,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                if (showSuccessFeedback) {
                    FeedbackBanner(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppSpacing.lg)
                            .padding(top = AppSpacing.md),
                        message = "Réservation créée avec succès !",
                        isError = false
                    )
                }
            }

            // Form Content - Design minimaliste plat
            Column(
                modifier = Modifier
                    .padding(horizontal = AppSpacing.md)
                    .padding(top = AppSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)
            ) {
                // Section 1: Logement - Style minimaliste avec bordure
                AnimatedPageTransition(visible = true) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = AppColors.surface,
                        border = BorderStroke(1.dp, AppColors.divider)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(AppSpacing.md),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                        ) {
                            // Titre avec ligne de séparation
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(20.dp)
                                        .background(AppColors.primary)
                                )
                                Text(
                                    text = "Logement",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.textPrimary
                                )
                            }
                            Divider(color = AppColors.divider, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(AppSpacing.xs))
                            
                            // Afficher un indicateur de chargement
                            if (logementUiState.isLoading) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(AppSpacing.md),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = AppColors.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            } else if (logementUiState.error != null) {
                                // Afficher l'erreur
                                Text(
                                    text = "Erreur: ${logementUiState.error}",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(AppSpacing.sm)
                                )
                            } else if (properties.isEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(AppSpacing.sm),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Aucun logement disponible",
                                        color = AppColors.textSecondary,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                                    Text(
                                        text = "Chargement en cours...",
                                        color = AppColors.textSecondary,
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                                    TextButton(
                                        onClick = { logementViewModel.loadLogements(force = true) }
                                    ) {
                                        Icon(
                                            Icons.Default.Refresh,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Actualiser", fontSize = 12.sp)
                                    }
                                }
                            } else {
                                // Afficher le nombre de logements disponibles
                                Text(
                                    text = "${properties.size} logement${if (properties.size > 1) "s" else ""} disponible${if (properties.size > 1) "s" else ""}",
                                    fontSize = 11.sp,
                                    color = AppColors.textSecondary,
                                    modifier = Modifier.padding(bottom = AppSpacing.xs)
                                )
                                PropertyDropdownMenu(
                                    items = properties,
                                    selectedItem = selectedProperty,
                                    onSelect = { selectedProperty = it }
                                )
                            }
                        }
                    }
                }

                // Section 2: Date & Heure - Style minimaliste
                AnimatedPageTransition(visible = true) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = AppColors.surface,
                        border = BorderStroke(1.dp, AppColors.divider)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(AppSpacing.md),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(20.dp)
                                        .background(AppColors.primary)
                                )
                            Text(
                                    text = "Date & Heure",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.textPrimary
                                )
                            }
                            Divider(color = AppColors.divider, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(AppSpacing.xs))
                            
                            // Date Picker minimaliste
                            androidx.compose.material3.DatePicker(
                                state = datePickerState,
                                showModeToggle = false,
                                colors = androidx.compose.material3.DatePickerDefaults.colors(
                                    containerColor = Color.Transparent
                                )
                            )
                        
                            Divider(color = AppColors.divider, thickness = 1.dp, modifier = Modifier.padding(vertical = AppSpacing.sm))
                        
                            // Créneaux horaires
                        Text(
                            text = "Créneau horaire",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = AppColors.textSecondary,
                                modifier = Modifier.padding(bottom = AppSpacing.xs)
                            )
                        TimeSlotRow(
                            slots = timeSlots,
                            selectedHour = selectedHour,
                            selectedMinute = selectedMinute,
                            onSelect = { h, m ->
                                selectedHour = h
                                selectedMinute = m
                            }
                        )
                        }
                    }
                }

                // Section 3: Contact - Style minimaliste
                AnimatedPageTransition(visible = true) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = AppColors.surface,
                        border = BorderStroke(1.dp, AppColors.divider)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(AppSpacing.md),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(20.dp)
                                        .background(AppColors.primary)
                                )
                                Text(
                                    text = "Contact (optionnel)",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.textPrimary
                                )
                            }
                            Divider(color = AppColors.divider, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(AppSpacing.xs))
                        
                        OutlinedTextField(
                            value = contactPhone,
                            onValueChange = { 
                                contactPhone = it
                                formError = null
                            },
                                label = { Text("Téléphone") },
                                placeholder = { Text("+33 6 12 34 56 78") },
                            leadingIcon = { 
                                Icon(
                                    Icons.Default.Phone, 
                                    contentDescription = null,
                                        tint = AppColors.textSecondary,
                                        modifier = Modifier.size(20.dp)
                                ) 
                            },
                            modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(0.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AppColors.primary,
                                unfocusedBorderColor = AppColors.divider,
                                focusedLabelColor = AppColors.primary
                            )
                        )
                            
                            Spacer(modifier = Modifier.height(AppSpacing.sm))
                            
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { 
                                notes = it
                                formError = null
                            },
                                label = { Text("Notes") },
                                placeholder = { Text("Message optionnel...") },
                            leadingIcon = { 
                                Icon(
                                    Icons.Default.Note, 
                                    contentDescription = null,
                                        tint = AppColors.textSecondary,
                                        modifier = Modifier.size(20.dp)
                                ) 
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                    .height(100.dp),
                                shape = RoundedCornerShape(0.dp),
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AppColors.primary,
                                unfocusedBorderColor = AppColors.divider,
                                focusedLabelColor = AppColors.primary
                            )
                        )
                        }
                    }
                }

                // Section 4: Récapitulatif - Style minimaliste
                AnimatedPageTransition(visible = true) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = AppColors.surfaceVariant,
                        border = BorderStroke(2.dp, AppColors.primary.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(AppSpacing.md),
                            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(20.dp)
                                        .background(AppColors.primary)
                                )
                                Text(
                                    text = "Récapitulatif",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.textPrimary
                                )
                            }
                            Divider(color = AppColors.divider, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(AppSpacing.xs))
                            
                            // Liste simple des infos
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Logement", fontSize = 13.sp, color = AppColors.textSecondary)
                                Text(
                                    selectedProperty?.label ?: "Non sélectionné",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppColors.textPrimary
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Date", fontSize = 13.sp, color = AppColors.textSecondary)
                                Text(
                                    selectedDateLabel ?: "À sélectionner",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppColors.textPrimary
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Heure", fontSize = 13.sp, color = AppColors.textSecondary)
                                Text(
                                    selectedTimeLabel,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppColors.textPrimary
                                )
                            }
                        }
                    }
                }

                // Submit Button
                Spacer(Modifier.height(AppSpacing.sm))
                PrimaryActionButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = if (uiState.isSubmitting) "Envoi en cours..." else "Confirmer la réservation",
                    enabled = isFormValid,
                    isLoading = uiState.isSubmitting,
                    onClick = {
                        if (selectedProperty == null) {
                            formError = "Veuillez sélectionner un logement."
                            return@PrimaryActionButton
                        }
                        val selectedMillis = datePickerState.selectedDateMillis
                        if (selectedMillis == null) {
                            formError = "Veuillez sélectionner une date pour votre visite."
                            return@PrimaryActionButton
                        }
                        if (selectedMillis < System.currentTimeMillis() - 86400000) {
                            formError = "La date sélectionnée ne peut pas être dans le passé."
                            return@PrimaryActionButton
                        }
                        formError = null
                        pendingReservation = PendingReservation(
                            logementId = selectedProperty!!.id,
                            dateMillis = selectedMillis,
                            hour = selectedHour,
                            minute = selectedMinute,
                            notes = notes.takeIf { it.isNotBlank() },
                            contactPhone = contactPhone.takeIf { it.isNotBlank() }
                        )
                        showConfirmationDialog = true
                    },
                    icon = {
                        if (!uiState.isSubmitting) {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                )

                Spacer(Modifier.height(AppSpacing.xl))
            }
        }
    }

    if (showConfirmationDialog && pendingReservation != null) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = {
                Text(
                    text = "Confirmer la visite",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Voulez-vous confirmer cette demande de visite ?")
            },
                    confirmButton = {
                TextButton(
                    onClick = {
                        pendingReservation?.let { reservation ->
                            // Utiliser l'ID original pour le backend, mais stocker aussi le titre pour l'affichage
                            // Le backend a besoin de l'ID ou annonceId, pas du titre
                            val logementIdToUse = if (reservation.logementId.startsWith("mock-")) {
                                // Pour les logements mock, utiliser l'annonceId au lieu de l'ID mock
                                val mockLogement = mockLogements.find { it.id == reservation.logementId }
                                mockLogement?.annonceId ?: reservation.logementId
                            } else {
                                // Pour les logements de l'API, utiliser l'ID directement
                                reservation.logementId
                            }
                            visiteViewModel.createVisite(
                                logementId = logementIdToUse,
                                dateMillis = reservation.dateMillis,
                                hour = reservation.hour,
                                minute = reservation.minute,
                                notes = reservation.notes,
                                contactPhone = reservation.contactPhone
                            )
                        }
                        showConfirmationDialog = false
                        showConfirmationSuccessDialog = true
                        pendingReservation = null
                    }
                ) {
                    Text("Oui")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showConfirmationDialog = false
                    pendingReservation = null
                }) {
                    Text("Non")
                }
            }
        )
    }

    if (showConfirmationSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationSuccessDialog = false },
            title = { Text("Visite confirmée") },
            text = { Text("Votre demande de visite a été envoyée.") },
            confirmButton = {
                TextButton(onClick = { showConfirmationSuccessDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun CollocatorBottomNavBar(navController: NavController) {
    val items = listOf(
        BottomNavItem.CollocatorHome,
        BottomNavItem.VisitsRequests,
        BottomNavItem.Reviews,
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
                        try {
                            val currentRoute = navController.currentBackStackEntry?.destination?.route
                            
                            // Si on clique sur la même route, ne rien faire
                            if (currentRoute != item.route) {
                                // Si on est sur une route qui n'est pas dans la bottom nav (comme notifications), revenir d'abord
                                val bottomNavRoutes = items.map { it.route }.toSet()
                                if (currentRoute != null && !bottomNavRoutes.contains(currentRoute) && 
                                    currentRoute != "notification_detail/{notificationId}" &&
                                    !currentRoute.startsWith("notification_detail/")) {
                                    // Revenir en arrière pour sortir de l'écran actuel (notifications, etc.)
                                    navController.popBackStack()
                                    // Naviguer après le pop avec un petit délai
                                    CoroutineScope(Dispatchers.Main).launch {
                                        delay(150)
                                        navController.navigate(item.route) {
                                            popUpTo(BottomNavItem.CollocatorHome.route) {
                                                inclusive = false
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                } else {
                                    // Navigation normale
                                    navController.navigate(item.route) {
                                        popUpTo(BottomNavItem.CollocatorHome.route) {
                                            inclusive = false
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("CollocatorBottomNavBar", "Erreur navigation vers ${item.route}", e)
                            // Fallback: navigation simple
                            try {
                                navController.navigate(item.route) {
                                    launchSingleTop = true
                                }
                            } catch (e2: Exception) {
                                android.util.Log.e("CollocatorBottomNavBar", "Erreur navigation fallback", e2)
                            }
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
                        try {
                            val currentRoute = navController.currentBackStackEntry?.destination?.route
                            
                            // Si on clique sur la même route, ne rien faire
                            if (currentRoute != item.route) {
                                // Si on est sur une route qui n'est pas dans la bottom nav (comme notifications), revenir d'abord
                                val bottomNavRoutes = items.map { it.route }.toSet()
                                if (currentRoute != null && !bottomNavRoutes.contains(currentRoute) && 
                                    currentRoute != "notification_detail/{notificationId}" &&
                                    !currentRoute.startsWith("notification_detail/")) {
                                    // Revenir en arrière pour sortir de l'écran actuel (notifications, etc.)
                                    navController.popBackStack()
                                    // Naviguer après le pop avec un petit délai
                                    CoroutineScope(Dispatchers.Main).launch {
                                        delay(150)
                                        navController.navigate(item.route) {
                                            popUpTo(BottomNavItem.Home.route) {
                                                inclusive = item.route == BottomNavItem.Home.route
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                } else {
                                    // Navigation normale
                                    navController.navigate(item.route) {
                                        // Pour la route Home, pop jusqu'à la route de base
                                        if (item.route == BottomNavItem.Home.route) {
                                            popUpTo(BottomNavItem.Home.route) {
                                                inclusive = true
                                                saveState = true
                                            }
                                        } else {
                                            popUpTo(BottomNavItem.Home.route) {
                                                inclusive = false
                                                saveState = true
                                            }
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("BottomNavBar", "Erreur navigation vers ${item.route}", e)
                            // Fallback: navigation simple
                            try {
                                navController.navigate(item.route) {
                                    launchSingleTop = true
                                }
                            } catch (e2: Exception) {
                                android.util.Log.e("BottomNavBar", "Erreur navigation fallback", e2)
                            }
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
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context.applicationContext) }
    val baseUrl = remember { NetworkConfig.getBaseUrl(context.applicationContext) }
    val notificationViewModel: com.sim.darna.firebase.FirebaseNotificationViewModel = viewModel(
        factory = com.sim.darna.factory.FirebaseNotificationVmFactory(baseUrl, sessionManager)
    )
    val unreadCount by notificationViewModel.unreadCount.collectAsState()

    // Charger les notifications et le compteur au démarrage
    LaunchedEffect(Unit) {
        android.util.Log.d("HomeScreen", "Chargement initial des notifications...")
        notificationViewModel.loadUnreadCount()
        notificationViewModel.loadNotifications()
    }
    
    // Rafraîchir les notifications périodiquement
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(5000) // Attendre 5 secondes avant le premier rafraîchissement
        while (true) {
            android.util.Log.d("HomeScreen", "Rafraîchissement périodique des notifications...")
            notificationViewModel.loadUnreadCount()
            notificationViewModel.loadNotifications()
            kotlinx.coroutines.delay(30000) // Attendre 30 secondes entre chaque rafraîchissement
        }
    }

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

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icône étoile pour les évaluations
                    Surface(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { navController.navigate(BottomNavItem.Reviews.route) },
                        shape = RoundedCornerShape(24.dp),
                        color = Color(0xFFFFC107).copy(alpha = 0.1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Évaluations",
                            modifier = Modifier.padding(12.dp),
                            tint = Color(0xFFFFC107)
                        )
                    }
                    // Icône notifications avec badge
                    Box {
                        Surface(
                            modifier = Modifier
                                .size(48.dp)
                                .clickable { navController.navigate("notifications") },
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
                        if (unreadCount > 0) {
                            Badge(
                                containerColor = Color(0xFFFF3B30),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 4.dp, y = (-4).dp)
                            ) {
                                Text(
                                    text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
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

// Écran Home pour les colocataires
@Composable
fun CollocatorHomeScreen(navController: NavController) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context.applicationContext) }
    val baseUrl = remember { NetworkConfig.getBaseUrl(context.applicationContext) }
    val notificationViewModel: com.sim.darna.firebase.FirebaseNotificationViewModel = viewModel(
        factory = com.sim.darna.factory.FirebaseNotificationVmFactory(baseUrl, sessionManager)
    )
    val unreadCount by notificationViewModel.unreadCount.collectAsState()

    // Charger les notifications et le compteur au démarrage
    LaunchedEffect(Unit) {
        android.util.Log.d("CollocatorHomeScreen", "Chargement initial des notifications...")
        notificationViewModel.loadUnreadCount()
        notificationViewModel.loadNotifications()
    }
    
    // Rafraîchir les notifications périodiquement pour capturer les nouvelles notifications d'annulation
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(5000) // Attendre 5 secondes avant le premier rafraîchissement
        while (true) {
            android.util.Log.d("CollocatorHomeScreen", "Rafraîchissement périodique des notifications...")
            notificationViewModel.loadUnreadCount()
            notificationViewModel.loadNotifications()
            kotlinx.coroutines.delay(30000) // Attendre 30 secondes entre chaque rafraîchissement
        }
    }

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
                        text = "Tableau de bord",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Gérez vos logements et visites",
                        fontSize = 14.sp,
                        color = Color(0xFF757575)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icône notifications avec badge
                    Box {
                        Surface(
                            modifier = Modifier
                                .size(48.dp)
                                .clickable { navController.navigate("notifications") },
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
                        if (unreadCount > 0) {
                            Badge(
                                containerColor = Color(0xFFFF3B30),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 4.dp, y = (-4).dp)
                            ) {
                                Text(
                                    text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card pour les demandes de visite
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { navController.navigate(BottomNavItem.VisitsRequests.route) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = Color(0xFF0066FF).copy(alpha = 0.1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventNote,
                            contentDescription = null,
                            modifier = Modifier.padding(12.dp),
                            tint = Color(0xFF0066FF)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Demandes",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1A1A1A)
                    )
                }
            }

            // Card pour les évaluations
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { navController.navigate(BottomNavItem.Reviews.route) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = Color(0xFFFFC107).copy(alpha = 0.1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.padding(12.dp),
                            tint = Color(0xFFFFC107)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Évaluations",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1A1A1A)
                    )
                }
            }
        }
    }
}

// Écran pour les colocataires - Gestion des demandes de visite
@Composable
fun CollocatorVisitsScreen(viewModel: VisiteViewModel) {
    val context = LocalContext.current
    val uiState = viewModel.state.collectAsState().value
    var showRejectConfirmation by remember { mutableStateOf<String?>(null) }
    var selectedStatusFilter by remember { mutableStateOf<String?>(null) }

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = AppSpacing.lg)
                .padding(top = AppSpacing.lg)
        ) {
            // Progress Indicator avec animation
            AnimatedVisibility(
                visible = uiState.isSubmitting,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = AppSpacing.md)
                        .clip(RoundedCornerShape(AppRadius.md)),
                    color = AppColors.primary,
                    trackColor = AppColors.divider
                )
            }

            Spacer(Modifier.height(AppSpacing.md))

            // Error Banner avec animation
            AnimatedVisibility(
                visible = uiState.error != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
            ) {
                uiState.error?.let {
                    FeedbackBanner(
                        message = it,
                        isError = true,
                        modifier = Modifier.fillMaxWidth(),
                        onDismiss = { viewModel.clearFeedback() }
                    )
                }
            }

            Spacer(Modifier.height(AppSpacing.lg))

            // Barre de navigation avec les statuts
            CollocatorStatusFiltersSection(
                visites = uiState.visites,
                selectedFilter = selectedStatusFilter,
                onFilterSelected = { selectedStatusFilter = it }
            )

            Spacer(Modifier.height(AppSpacing.md))

            // Filtrer les visites selon le statut sélectionné
            // Exclure les visites "cancelled" (annulées par le client) de la liste - elles apparaissent seulement dans les notifications
            val filteredVisites = remember(uiState.visites, selectedStatusFilter) {
                // D'abord exclure les visites annulées par le client (cancelled)
                // Mais garder les visites refusées par le collecteur (refused)
                val visitesWithoutCancelled = uiState.visites.filter { visite ->
                    val status = visite.status?.lowercase()
                    // Exclure seulement "cancelled" (annulé par client), pas "refused" (refusé par collecteur)
                    status != "cancelled"
                }
                
                if (selectedStatusFilter == null) {
                    visitesWithoutCancelled
                } else {
                    visitesWithoutCancelled.filter { visite ->
                        when (selectedStatusFilter) {
                            "pending" -> visite.status?.equals("pending", ignoreCase = true) == true || 
                                        (visite.status == null || visite.status.equals("en attente", ignoreCase = true))
                            "confirmed" -> visite.status?.equals("confirmed", ignoreCase = true) == true ||
                                           visite.status?.equals("acceptée", ignoreCase = true) == true
                            "refused" -> visite.status?.equals("refused", ignoreCase = true) == true ||
                                         visite.status?.equals("refusée", ignoreCase = true) == true
                            else -> true
                        }
                    }
                }
            }

            // Content avec animations
            when {
                uiState.isLoadingList && filteredVisites.isEmpty() -> {
                    AnimatedPageTransition(visible = true) {
                        CollocatorSkeletonList()
                    }
                }
                filteredVisites.isEmpty() -> {
                    AnimatedPageTransition(visible = true) {
                        EmptyStateCard(
                            title = "Aucune demande en cours",
                            description = "Les réservations apparaîtront ici dès qu'un client sollicitera une visite.",
                            actionLabel = "Actualiser",
                            onAction = { viewModel.loadLogementsVisites(force = true) },
                            icon = Icons.Default.EventNote
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
                        contentPadding = PaddingValues(bottom = AppSpacing.xl)
                    ) {
                        items(
                            items = filteredVisites,
                            key = { it.id ?: it.dateVisite ?: "" }
                        ) { visite ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn() + slideInVertically(
                                    initialOffsetY = { it / 2 },
                                    animationSpec = defaultSlideAnimationSpec
                                ),
                                exit = fadeOut() + slideOutVertically(
                                    targetOffsetY = { it / 2 },
                                    animationSpec = defaultSlideAnimationSpec
                                )
                            ) {
                                CollocatorVisitCard(
                                    visite = visite,
                                    onAccept = { id -> viewModel.acceptVisite(id) },
                                    onReject = { id -> showRejectConfirmation = id }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Reject Confirmation
    showRejectConfirmation?.let { id ->
        ConfirmationDialog(
            title = "Refuser la demande",
            message = "Êtes-vous sûr de vouloir refuser cette demande de visite ?",
            confirmText = "Refuser",
            cancelText = "Annuler",
            onConfirm = {
                viewModel.rejectVisite(id)
                showRejectConfirmation = null
            },
            onDismiss = { showRejectConfirmation = null },
            isDestructive = true
        )
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
    
    // Design minimaliste avec bordure colorée
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(0.dp)),
        color = AppColors.surface,
        border = BorderStroke(
            width = 4.dp,
            color = statusChip.color.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp)
        ) {
            // Header minimaliste
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(statusChip.color.copy(alpha = 0.08f))
                    .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    modifier = Modifier.weight(1f)
                ) {
                    // Ligne verticale de statut
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(40.dp)
                            .background(statusChip.color)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = getLogementTitleForCollocator(visite),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.textPrimary
                        )
                    }
                }
                // Badge de statut avec icône
                SurfaceChipForCollocator(
                    icon = statusChip.icon,
                    label = statusChip.label,
                    color = statusChip.color
                )
            }

            // Contenu
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.md),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                // Client
                if (!visite.clientUsername.isNullOrBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                            Icons.Default.Person,
                                contentDescription = null,
                                tint = AppColors.textSecondary,
                            modifier = Modifier.size(18.dp)
                            )
                        Column {
                            Text("Demandeur", fontSize = 11.sp, color = AppColors.textSecondary)
                            Text(
                                visite.clientUsername,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = AppColors.textPrimary
                            )
                        }
                    }
                }

                // Date et heure
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = AppColors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                        Text(
                                text = formatDateForCollocator(visite.dateVisite),
                                fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                                color = AppColors.textPrimary
                            )
                            Text(
                                text = formatTimeForCollocator(visite.dateVisite),
                                fontSize = 12.sp,
                                color = AppColors.textSecondary
                            )
                        }
                    }
                }

                // Séparateur
                if (!visite.contactPhone.isNullOrBlank() || !visite.notes.isNullOrBlank()) {
                    Divider(
                        color = AppColors.divider,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = AppSpacing.xs)
                    )
                }

                // Contact téléphone
                if (!visite.contactPhone.isNullOrBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                            Icon(
                            Icons.Default.Person,
                                contentDescription = null,
                            tint = AppColors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = visite.contactPhone,
                            fontSize = 14.sp,
                            color = AppColors.textPrimary
                        )
                    }
                }

                // Notes
                if (!visite.notes.isNullOrBlank()) {
                    if (!visite.contactPhone.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(AppSpacing.xs))
                    }
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Note,
                            contentDescription = null,
                            tint = AppColors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = visite.notes ?: "",
                            fontSize = 13.sp,
                            color = AppColors.textSecondary,
                            lineHeight = 18.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Actions minimalistes
            if (isPending) {
                Divider(color = AppColors.divider, thickness = 1.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppSpacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                ) {
                    // Bouton accepter - style texte avec fond
                    Button(
                        onClick = { visite.id?.let(onAccept) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.success,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(0.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Accepter", fontSize = 13.sp)
                    }
                    // Bouton refuser - style texte
                    TextButton(
                        onClick = { visite.id?.let(onReject) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.Cancel,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = AppColors.danger
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Refuser", fontSize = 13.sp, color = AppColors.danger)
                    }
                }
            }
        }
    }
}

// Écran de gestion des logements (placeholder)
@Composable
fun ManagePropertiesScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)
        ) {
            // Header avec animation
            AnimatedPageTransition(visible = true) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = AppSpacing.lg)
                        .clip(RoundedCornerShape(AppRadius.xl))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF0066FF),
                                    Color(0xFF0052CC),
                                    Color(0xFF003D99)
                                )
                            )
                        )
                        .padding(AppSpacing.lg)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                "Espace collecteur",
                                color = Color.White,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(AppSpacing.sm))
                        Text(
                            "Publiez vos logements, suivez les demandes et optimisez vos visites.",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 15.sp,
                            lineHeight = 22.sp
                        )
                        Spacer(Modifier.height(AppSpacing.md))
                        PrimaryActionButton(
                            text = "Ajouter un logement",
                            onClick = {},
                            modifier = Modifier.fillMaxWidth(),
                            icon = {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        )
                    }
                }
            }

            // Features Card
            AnimatedPageTransition(visible = true) {
                ElevatedCard {
                    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = AppColors.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                "Fonctionnalités à venir",
                                fontWeight = FontWeight.Bold,
                                color = AppColors.textPrimary,
                                fontSize = 18.sp
                            )
                        }
                        Spacer(Modifier.height(AppSpacing.xs))
                        Text(
                            "• Création d'annonces complètes\n• Gestion des créneaux\n• Statistiques de performance",
                            color = AppColors.textSecondary,
                            fontSize = 14.sp,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            // Skeleton placeholders
            AnimatedPageTransition(visible = true) {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                    repeat(2) {
                        ElevatedCard {
                            SkeletonBox(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Fonctions utilitaires pour les colocataires
private fun formatDateForCollocator(dateString: String?): String {
    val date = parseIsoToMillisForCollocator(dateString) ?: return "-"
    val formatter = java.text.SimpleDateFormat("EEEE d MMMM yyyy", java.util.Locale.getDefault())
    return formatter.format(date).replaceFirstChar { it.uppercase() }
}

private fun formatTimeForCollocator(dateString: String?): String {
    if (dateString.isNullOrBlank()) return "-"
    
    // Parse the UTC date string
    val millis = parseIsoToMillisForCollocator(dateString) ?: return "-"
    
    // Create a calendar in LOCAL timezone to display the time correctly
    val calendar = java.util.Calendar.getInstance() // Uses local timezone by default
    calendar.timeInMillis = millis
    
    // Format in local time
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

// Fonction pour obtenir le titre du logement pour le colocateur
private fun getLogementTitleForCollocator(visite: com.sim.darna.visite.VisiteResponse): String {
    // 1. Utiliser logementTitle si disponible
    if (!visite.logementTitle.isNullOrBlank()) {
        return visite.logementTitle
    }
    
    // 2. Chercher dans les logements mock par logementId
    visite.logementId?.let { logementId ->
        mockLogementsMap[logementId]?.let { return it }
        
        // Si le logementId contient "LOGEMENT_ID", essayer de trouver un titre par défaut
        if (logementId.contains("LOGEMENT_ID", ignoreCase = true)) {
            val number = logementId.replace(Regex("[^0-9]"), "")
            if (number.isNotBlank()) {
                val index = number.toIntOrNull()?.let { 
                    if (it <= mockLogementsMap.size) it - 1 else null
                }
                if (index != null && index >= 0) {
                    val titles = mockLogementsMap.values.toList()
                    if (index < titles.size) {
                        return titles[index]
                    }
                }
            }
        }
    }
    
    // 3. Fallback par défaut
    return visite.logementId ?: "Logement inconnu"
}

private fun mapStatusForCollocator(status: String?): StatusChipDataForCollocator {
    return when (status?.lowercase()) {
        "confirmed" -> StatusChipDataForCollocator("Acceptée", Color(0xFF10B981), Icons.Default.CheckCircle)
        "refused" -> StatusChipDataForCollocator("Refusée", Color(0xFFEF4444), Icons.Default.Cancel)
        "cancelled" -> StatusChipDataForCollocator("Annulée", Color(0xFF9E9E9E), Icons.Default.EventBusy)
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

@Composable
private fun CollocatorStatusFiltersSection(
    visites: List<com.sim.darna.visite.VisiteResponse>,
    selectedFilter: String?,
    onFilterSelected: (String?) -> Unit
) {
    // Compter les visites par statut
    // Exclure les visites "cancelled" (annulées par le client) du comptage
    // Mais garder les visites "refused" (refusées par le collecteur)
    val visitesWithoutCancelled = visites.filter { visite ->
        val status = visite.status?.lowercase()
        // Exclure seulement "cancelled" (annulé par client), pas "refused" (refusé par collecteur)
        status != "cancelled"
    }
    
    val pendingCount = visitesWithoutCancelled.count { 
        it.status?.equals("pending", ignoreCase = true) == true || 
        (it.status == null || it.status.equals("en attente", ignoreCase = true))
    }
    val confirmedCount = visitesWithoutCancelled.count { 
        it.status?.equals("confirmed", ignoreCase = true) == true ||
        it.status?.equals("acceptée", ignoreCase = true) == true
    }
    // Compter "refused" (refusé par le collecteur) - ce sont les visites que le collecteur a refusées
    val refusedCount = visitesWithoutCancelled.count { 
        it.status?.equals("refused", ignoreCase = true) == true ||
        it.status?.equals("refusée", ignoreCase = true) == true
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        // Toutes les demandes
        CollocatorStatusFilterCard(
            label = "Toutes",
            count = visitesWithoutCancelled.size,
            color = AppColors.primary,
            icon = Icons.Default.Apps,
            isSelected = selectedFilter == null,
            onClick = { onFilterSelected(null) }
        )
        
        // En attente
        CollocatorStatusFilterCard(
            label = "En attente",
            count = pendingCount,
            color = AppColors.warning,
            icon = Icons.Default.Schedule,
            isSelected = selectedFilter == "pending",
            onClick = { onFilterSelected(if (selectedFilter == "pending") null else "pending") }
        )
        
        // Acceptée
        CollocatorStatusFilterCard(
            label = "Acceptée",
            count = confirmedCount,
            color = AppColors.success,
            icon = Icons.Default.CheckCircle,
            isSelected = selectedFilter == "confirmed",
            onClick = { onFilterSelected(if (selectedFilter == "confirmed") null else "confirmed") }
        )
        
        // Refusée (seulement les visites refusées par le collecteur, pas celles annulées par le client)
        CollocatorStatusFilterCard(
            label = "Refusée",
            count = refusedCount,
            color = AppColors.danger,
            icon = Icons.Default.Cancel,
            isSelected = selectedFilter == "refused",
            onClick = { onFilterSelected(if (selectedFilter == "refused") null else "refused") }
        )
    }
}

@Composable
private fun CollocatorStatusFilterCard(
    label: String,
    count: Int,
    color: Color,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .clickable { onClick() }
            .width(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) color.copy(alpha = 0.2f) else color.copy(alpha = 0.1f)
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) color else color.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(AppRadius.md)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = color
                )
                Text(
                    text = "$count",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}

@Composable
private fun CollocatorHeader(onRefresh: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = AppSpacing.lg)
            .clip(RoundedCornerShape(AppRadius.xl))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF0066FF),
                        Color(0xFF0052CC),
                        Color(0xFF003D99)
                    )
                )
            )
            .padding(AppSpacing.lg)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                Icon(
                    imageVector = Icons.Default.EventNote,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "Demandes de visite",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "Acceptez, refusez et suivez les visites de vos logements en temps réel.",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 15.sp,
                lineHeight = 22.sp
            )
            Spacer(Modifier.height(AppSpacing.md))
            SecondaryActionButton(
                text = "Actualiser",
                onClick = onRefresh,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = AppColors.primary)
            }
        }
    }
}

@Composable
private fun CollocatorSkeletonList() {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)) {
        repeat(3) {
            ElevatedCard {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    SkeletonBox(modifier = Modifier.fillMaxWidth().height(18.dp))
                    SkeletonBox(modifier = Modifier.fillMaxWidth().height(16.dp))
                    SkeletonBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        shape = RoundedCornerShape(AppRadius.md)
                    )
                }
            }
        }
    }
}

@Composable
private fun CollocatorClientBadge(username: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppRadius.md))
            .background(AppColors.surface)
            .padding(AppSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = AppColors.primary
        )
        Column {
            Text("Demandeur", color = AppColors.textSecondary, fontSize = 12.sp)
            Text(username, fontWeight = FontWeight.Bold, color = AppColors.textPrimary)
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column {
        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.textPrimary)
        Spacer(Modifier.height(4.dp))
        Text(text = subtitle, color = AppColors.textSecondary, fontSize = 14.sp)
    }
}

@Composable
private fun ModernSectionHeader(
    title: String,
    subtitle: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppColors.primary,
            modifier = Modifier.size(24.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = AppColors.textPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = AppColors.textSecondary,
                fontSize = 14.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PropertyDropdownMenu(
    items: List<LogementOption>,
    selectedItem: LogementOption?,
    onSelect: (LogementOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedItem?.label ?: "",
            onValueChange = {},
            readOnly = true,
        modifier = Modifier
            .fillMaxWidth()
                .menuAnchor(),
            label = { Text("Sélectionner un logement") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Home,
                    contentDescription = null,
                    tint = AppColors.primary,
                    modifier = Modifier.size(20.dp)
                )
            },
            shape = RoundedCornerShape(0.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.primary,
                unfocusedBorderColor = AppColors.divider,
                focusedLabelColor = AppColors.primary
            )
        )
        // ExposedDropdownMenu est disponible via import androidx.compose.material3.*
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Row(
                    verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                            if (option.id == selectedItem?.id) {
                        Icon(
                                    Icons.Default.CheckCircle,
                            contentDescription = null,
                                    tint = AppColors.primary,
                                    modifier = Modifier.size(18.dp)
                        )
                            } else {
                                Spacer(modifier = Modifier.size(18.dp))
                    }
                    Text(
                                text = option.label,
                                color = if (option.id == selectedItem?.id) AppColors.primary else AppColors.textPrimary,
                                fontWeight = if (option.id == selectedItem?.id) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ReservableHighlight(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(AppRadius.md))
            .background(AppColors.background)
            .padding(12.dp)
    ) {
        Text(text = title, fontWeight = FontWeight.Medium, color = AppColors.textSecondary)
        Spacer(Modifier.height(AppSpacing.sm))
        content()
    }
}

private fun formatReservationDate(millis: Long?): String? {
    if (millis == null) return null
    val formatter = SimpleDateFormat("EEEE dd MMMM yyyy", Locale.getDefault())
    return formatter.format(millis).replaceFirstChar { it.uppercase() }
}

// Fonction pour formater le label d'un logement de manière attractive
// Format: "Type - Ville" ou "Titre - Adresse"
private fun formatLogementLabel(logement: com.sim.darna.logement.LogementResponse): String {
    val title = logement.title?.trim() ?: ""
    val address = logement.address?.trim() ?: ""
    val rooms = logement.rooms
    val surface = logement.surface
    
    // Partie 1: Type/Titre du logement
    val typePart = when {
        title.isNotBlank() -> {
            // Si le titre contient déjà un format intéressant, l'utiliser
            if (title.contains("-") || title.contains("dans") || title.contains("meublé")) {
                title
            } else {
                // Sinon, enrichir avec le nombre de pièces si disponible
                when {
                    rooms != null && rooms > 1 -> {
                        when (rooms) {
                            2 -> "Appartement 2 pièces"
                            3 -> "Appartement 3 pièces"
                            4 -> "Appartement 4 pièces (T4)"
                            5 -> "Appartement 5 pièces"
                            else -> "Appartement $rooms pièces"
                        }
                    }
                    rooms == 1 -> "Studio"
                    title.lowercase().contains("chambre") -> title
                    title.lowercase().contains("studio") -> "Studio meublé"
                    title.lowercase().contains("appartement") -> title
                    else -> title
                }
            }
        }
        rooms != null -> {
            when (rooms) {
                1 -> "Studio meublé"
                2 -> "Appartement 2 pièces"
                3 -> "Appartement 3 pièces"
                4 -> "Chambre dans T4"
                5 -> "Appartement 5 pièces"
                else -> "Appartement $rooms pièces"
            }
        }
        else -> "Logement"
    }
    
    // Partie 2: Localisation (Ville/Adresse)
    val locationPart = when {
        address.isNotBlank() -> {
            // Extraire la ville de l'adresse
            // Formats possibles: "Marseille 8e", "Lyon", "Centre Ville", "75011 - Bastille"
            val patterns = listOf(
                Regex("""([A-Za-zÀ-ÿ\s]+?)\s+\d+e\s*(?:arrondissement|arr\.)?""", RegexOption.IGNORE_CASE), // "Marseille 8e"
                Regex("""([A-Za-zÀ-ÿ\s]+?)(?:\s*-\s*|\s*,\s*)([A-Za-zÀ-ÿ\s]+)"""), // "75011 - Bastille" ou "Paris, Centre"
                Regex("""(?:^|\s)([A-Za-zÀ-ÿ]+(?:\s+[A-Za-zÀ-ÿ]+)*)(?:\s+\d+|$)""") // Ville simple
            )
            
            var city = address
            for (pattern in patterns) {
                val match = pattern.find(address)
                if (match != null) {
                    city = match.groupValues.lastOrNull()?.trim() ?: address
                    if (city.isNotBlank() && city.length > 2) {
                        break
                    }
                }
            }
            
            // Nettoyer la ville
            city = city.replace(Regex("""\d+"""), "").trim()
            if (city.isBlank()) city = address
            
            city
        }
        else -> ""
    }
    
    // Construire le label final
    return when {
        locationPart.isNotBlank() -> "$typePart - $locationPart"
        typePart.isNotBlank() -> typePart
        else -> "Logement ${logement.id?.take(8) ?: "Inconnu"}"
    }
}



