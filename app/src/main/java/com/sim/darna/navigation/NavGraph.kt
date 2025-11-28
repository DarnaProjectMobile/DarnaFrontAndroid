package com.sim.darna.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sim.darna.auth.SessionManager
import com.sim.darna.factory.NotificationVmFactory
import com.sim.darna.network.NetworkConfig
import com.sim.darna.notification.NotificationViewModel
import com.sim.darna.screens.*
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * ✅ Centralized route definitions for clarity and reuse.
 */
object Routes {
    const val Splash = "splash"
    const val Login = "login"
    const val SignUp = "signup"
    const val IdScan = "idscan"
    const val Selfie = "selfie"
    const val Fingerprint = "fingerprint"
    const val Main = "main"
    // Change this line
    const val PropertyDetail = "property_detail/{propertyName}" // Add the argument placeholder
    const val Reviews = "reviews/{propertyId}"
    const val Notifications = "notifications"
    const val NotificationDetail = "notification_detail/{notificationId}"
}

@Composable
fun AppNavGraph(navController: NavHostController = rememberNavController()) {

    NavHost(
        navController = navController,
        startDestination = Routes.Splash
    ) {

        // ✅ SPLASH → LOGIN
        composable(Routes.Splash) {
            SplashScreen {
                navController.navigate(Routes.Login) {
                    popUpTo(Routes.Splash) { inclusive = true }
                }
            }
        }

        // ✅ LOGIN → MAIN or SIGNUP
        composable(Routes.Login) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.Main) {
                        popUpTo(Routes.Login) { inclusive = true }
                    }
                },
                onSignUp = {
                    navController.navigate(Routes.SignUp)
                }
            )
        }

        // ✅ SIGNUP → ID SCAN
        composable(Routes.SignUp) {
            SignUpScreen(
                onScanIdClick = {
                    navController.navigate(Routes.IdScan) {
                        popUpTo(Routes.SignUp) { inclusive = true }
                    }
                }
            )
        }

        // ✅ ID SCAN → SELFIE
        composable(Routes.IdScan) {
            IdScanScreen {
                navController.navigate(Routes.Selfie) {
                    popUpTo(Routes.IdScan) { inclusive = true }
                }
            }
        }

        // ✅ SELFIE → FINGERPRINT
        composable(Routes.Selfie) {
            SelfieScreen {
                navController.navigate(Routes.Fingerprint) {
                    popUpTo(Routes.Selfie) { inclusive = true }
                }
            }
        }

        // ✅ FINGERPRINT → MAIN
        composable(Routes.Fingerprint) {
            FingerprintScreen {
                navController.navigate(Routes.Main) {
                    popUpTo(Routes.Fingerprint) { inclusive = true }
                }
            }
        }

        // ✅ MAIN APP (Bottom Navigation / Dashboard)
        composable(Routes.Main) {
            MainScreen(parentNavController = navController)
        }

        composable(
            route = Routes.PropertyDetail, // This now correctly uses "property_detail/{propertyName}"
            arguments = listOf(navArgument("propertyName") { type = NavType.StringType })
        ) { backStackEntry ->
            // Extract the argument
            val propertyName = backStackEntry.arguments?.getString("propertyName")?.let {
                URLDecoder.decode(it, StandardCharsets.UTF_8.name())
            } ?: "Détails du bien"
            
            PropertyDetailScreen(
                navController = navController,
                title = propertyName
            )
        }

        // ✅ NOTIFICATIONS
        composable(Routes.Notifications) {
            val context = LocalContext.current
            val sessionManager = remember { SessionManager(context.applicationContext) }
            val baseUrl = remember { NetworkConfig.getBaseUrl(context.applicationContext) }
            val notificationViewModel: NotificationViewModel = viewModel(
                factory = NotificationVmFactory(baseUrl, sessionManager)
            )
            NotificationsScreen(navController = navController, viewModel = notificationViewModel)
        }

        // ✅ NOTIFICATION DETAIL
        composable(
            route = Routes.NotificationDetail,
            arguments = listOf(navArgument("notificationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val notificationId = backStackEntry.arguments?.getString("notificationId") ?: ""
            
            if (notificationId.isEmpty()) {
                // Si pas d'ID, retourner en arrière
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
                return@composable
            }
            
            val context = LocalContext.current
            val sessionManager = remember { SessionManager(context.applicationContext) }
            val baseUrl = remember { NetworkConfig.getBaseUrl(context.applicationContext) }
            val notificationViewModel: NotificationViewModel = viewModel(
                factory = NotificationVmFactory(baseUrl, sessionManager)
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
                    android.util.Log.e("NavGraph", "Erreur lors du chargement de la notification", e)
                }
            }
            
            // Afficher le contenu selon l'état
            when {
                uiState.isLoading -> {
                    // Afficher un loader pendant le chargement
                    CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize(Alignment.Center)
                    )
                }
                selectedNotification != null && selectedNotification!!.id != null -> {
                    // Afficher les détails de la notification
                    NotificationDetailScreen(
                        navController = navController,
                        notification = selectedNotification!!
                    )
                }
                uiState.error != null -> {
                    // Afficher un message d'erreur
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
                                    onClick = { 
                                        navController.popBackStack()
                                    }
                                ) {
                                    Text("Retour")
                                }
                            }
                        }
                    }
                }
                else -> {
                    // Si notification non trouvée après un délai, retourner à la liste
                    var hasShownError by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        delay(2000) // Attendre 2 secondes
                        if (selectedNotification == null && !uiState.isLoading) {
                            hasShownError = true
                            delay(500)
                            navController.popBackStack()
                        }
                    }
                    
                    if (hasShownError) {
                        // Retourner silencieusement
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

