package com.sim.darna.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sim.darna.screens.*
import com.sim.darna.screens.AddPubliciteScreen
import com.sim.darna.screens.MapScreen

object Routes {
    const val Login = "login"
    const val SignUp = "signup"
    const val ForgotPassword = "forgot_password"
    const val IdScan = "idscan"
    const val Selfie = "selfie"
    const val Fingerprint = "fingerprint"
    const val Main = "main"
    const val PropertyDetail = "property_detail"
    const val PropertyDetailWithId = "property_detail/{propertyId}"
    const val ResetPassword = "reset_password"
    const val Reviews = "reviews"
    const val ReviewsWithParams = "reviews/{propertyId}/{propertyName}/{userName}"
    const val UpdateProfile = "update_profile"
    const val Favorites = "favorites"
    const val Reservations = "reservations"
    const val AcceptedClients = "accepted_clients"
    const val ConfirmedClients = "confirmed_clients/{propertyId}"
    const val BookProperty = "book_property/{propertyId}"
    const val PropertyBookings = "property_books/{propertyId}"
    const val Notifications = "notifications"
    const val PubliciteDetail = "publicite_detail/{publiciteId}"
    const val AddPublicite = "add_publicite"
    const val EditPublicite = "edit_publicite/{publiciteId}"
    const val QRCodeScanner = "qr_code_scanner"
    const val Map = "map"
    const val Dashboard = "dashboard"
    const val MyVisits = "my_visits"
    const val Chat = "chat/{visiteId}/{visiteTitle}"
    const val AllReviews = "all_reviews/{visiteId}"
    const val VisitRequests = "visit_requests"
    const val ReceivedReviews = "received_reviews"
}

@Composable
fun AppNavGraph(navController: NavHostController = rememberNavController()) {

    NavHost(
        navController = navController,
        startDestination = Routes.Login
    ) {


        composable(Routes.Login) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.Main) {
                        popUpTo(Routes.Login) { inclusive = true }
                    }
                },
                onSignUp = { navController.navigate(Routes.SignUp) }
            )
        }

        composable(Routes.SignUp) {
            SignUpScreen(
                onScanIdClick = {
                    navController.navigate(Routes.IdScan) {
                        popUpTo(Routes.SignUp) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.IdScan) {
            IdScanScreen {
                navController.navigate(Routes.Selfie) {
                    popUpTo(Routes.IdScan) { inclusive = true }
                }
            }
        }

        composable(Routes.Selfie) {
            SelfieScreen {
                navController.navigate(Routes.Fingerprint) {
                    popUpTo(Routes.Selfie) { inclusive = true }
                }
            }
        }

        composable(Routes.Fingerprint) {
            FingerprintScreen {
                navController.navigate(Routes.Main) {
                    popUpTo(Routes.Fingerprint) { inclusive = true }
                }
            }
        }
        composable("feedback") {
            FeedbackScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Routes.UpdateProfile) {
            UpdateProfileScreen(onNavigateBack = { navController.popBackStack() })
        }



        // ⭐ MAIN APP (BOTTOM NAVIGATION)
        composable(Routes.Main) { MainScreen(navController) }

        // ⭐ FULL SCREEN PAGES
        composable(Routes.PropertyDetail) { PropertyDetailScreen(navController) }
        composable(
            route = Routes.PropertyDetailWithId,
            arguments = listOf(navArgument("propertyId") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val propertyId = backStackEntry.arguments?.getString("propertyId") ?: ""
            PropertyDetailScreen(navController, propertyId)
        }
        composable(Routes.Reviews) { 
            ReviewsScreen(
                onNavigateBack = { navController.popBackStack() }
            ) 
        }
        
        composable(
            route = Routes.ReviewsWithParams,
            arguments = listOf(
                navArgument("propertyId") { type = androidx.navigation.NavType.StringType },
                navArgument("propertyName") { type = androidx.navigation.NavType.StringType },
                navArgument("userName") { type = androidx.navigation.NavType.StringType }
            )
        ) { backStackEntry ->
            val propertyId = backStackEntry.arguments?.getString("propertyId") ?: ""
            val propertyName = backStackEntry.arguments?.getString("propertyName") ?: ""
            val userName = backStackEntry.arguments?.getString("userName") ?: ""
            ReviewsScreen(
                propertyId = propertyId,
                propertyName = propertyName,
                userName = userName,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Routes.ForgotPassword) { ForgotPasswordScreen(navController) }
        composable(Routes.ResetPassword) { ResetPasswordScreen(navController) }
        composable(Routes.Favorites) { FavoritesScreen(navController) }
        composable(Routes.Reservations) { ReservationsScreen(navController) }
        composable(Routes.AcceptedClients) { AcceptedClientsScreen(navController) }
        composable(Routes.Notifications) {
            NotificationsScreen(
                navController = navController,
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.Map) { MapScreen(navController) }
        composable(
            route = Routes.BookProperty,
            arguments = listOf(navArgument("propertyId") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val propertyId = backStackEntry.arguments?.getString("propertyId") ?: ""
            BookPropertyScreen(navController, propertyId)
        }
        composable(
            route = Routes.PropertyBookings,
            arguments = listOf(navArgument("propertyId") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val propertyId = backStackEntry.arguments?.getString("propertyId") ?: ""
            PropertyBookingsScreen(navController, propertyId)
        }
        composable(
            route = Routes.ConfirmedClients,
            arguments = listOf(navArgument("propertyId") { type = NavType.StringType })
        ) { backStackEntry ->
            val propertyId = backStackEntry.arguments?.getString("propertyId") ?: ""
            ConfirmedClientsScreen(navController, propertyId)
        }
        
        // Routes pour les publicités
        composable(
            route = Routes.PubliciteDetail,
            arguments = listOf(navArgument("publiciteId") { type = NavType.StringType })
        ) { backStackEntry ->
            val publiciteId = backStackEntry.arguments?.getString("publiciteId") ?: ""
            PubliciteDetailScreen(
                publiciteId = publiciteId,
                onNavigateBack = { navController.popBackStack() },
                onEdit = { id ->
                    navController.navigate(Routes.EditPublicite.replace("{publiciteId}", id))
                },
                onScanQRCode = {
                    navController.navigate(Routes.QRCodeScanner)
                }
            )
        }
        
        composable(Routes.AddPublicite) {
            AddPubliciteScreen(
                onFinish = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Routes.EditPublicite,
            arguments = listOf(navArgument("publiciteId") { type = NavType.StringType })
        ) { backStackEntry ->
            val publiciteId = backStackEntry.arguments?.getString("publiciteId") ?: ""
            AddPubliciteScreen(
                publiciteId = publiciteId,
                onFinish = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
        
        composable(Routes.QRCodeScanner) {
            QRCodeScanResultScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Routes.Dashboard) {
            DashboardScreen(navController)
        }
        
        // Routes pour les Visites
        composable(Routes.MyVisits) {
            val context = androidx.compose.ui.platform.LocalContext.current
            val prefs = context.getSharedPreferences("APP_PREFS", android.content.Context.MODE_PRIVATE)
            val baseUrl = "http://192.168.1.101:3009/"
            val viewModel: com.sim.darna.visite.VisiteViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = com.sim.darna.factory.VisiteVmFactory(baseUrl, context)
            )
            MyVisitsScreen(viewModel, navController, navController)
        }
        
        composable(
            route = Routes.Chat,
            arguments = listOf(
                navArgument("visiteId") { type = NavType.StringType },
                navArgument("visiteTitle") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val visiteId = backStackEntry.arguments?.getString("visiteId") ?: ""
            val visiteTitle = backStackEntry.arguments?.getString("visiteTitle") ?: ""
            val context = androidx.compose.ui.platform.LocalContext.current
            val sessionManager = com.sim.darna.auth.SessionManager(context)
            val currentUserId = context.getSharedPreferences("APP_PREFS", android.content.Context.MODE_PRIVATE)
                .getString("user_id", "") ?: ""
            val baseUrl = "http://192.168.1.101:3009/"
            val viewModel: com.sim.darna.chat.ChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = com.sim.darna.factory.ChatVmFactory(baseUrl, context)
            )
            ChatScreen(
                visiteId = visiteId,
                visiteTitle = visiteTitle,
                currentUserId = currentUserId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Routes.AllReviews,
            arguments = listOf(navArgument("visiteId") { type = NavType.StringType })
        ) { backStackEntry ->
            val context = androidx.compose.ui.platform.LocalContext.current
            val prefs = context.getSharedPreferences("APP_PREFS", android.content.Context.MODE_PRIVATE)
            val baseUrl = "http://192.168.1.101:3009/"
            val viewModel: com.sim.darna.visite.VisiteViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = com.sim.darna.factory.VisiteVmFactory(baseUrl, context)
            )
            AllReviewsScreen(viewModel, navController)
        }
        
        // Routes pour les COLLOCATORS
        composable(Routes.VisitRequests) {
            VisitRequestsScreen(navController)
        }
        
        composable(Routes.ReceivedReviews) {
            ReceivedReviewsScreen(navController)
        }
    }
}
