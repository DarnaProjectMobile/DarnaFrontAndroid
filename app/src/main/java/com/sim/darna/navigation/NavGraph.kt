package com.sim.darna.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sim.darna.screens.*

object Routes {
    const val Login = "login"
    const val SignUp = "signup"
    const val ForgotPassword = "forgot_password"
    const val Verification = "verification"
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
    const val Map = "map"
    const val ReviewSummary = "reviewSummary/{propertyId}/{propertyName}"
    const val PubliciteDetail = "publicite_detail/{publiciteId}"
    const val AddPublicite = "add_publicite"
    const val EditPublicite = "edit_publicite/{publiciteId}"
    const val Dashboard = "dashboard"
    const val VisitRequests = "visit_requests"
    const val MyVisits = "my_visits"
    const val Chat = "chat/{visiteId}/{title}"
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
                onSignUp = { navController.navigate(Routes.SignUp) },
                onForgotPassword = { navController.navigate(Routes.ForgotPassword) }
            )
        }

        composable(Routes.SignUp) {
            SignUpScreen(
                onVerificationNavigate = {
                    navController.navigate(Routes.Verification) {
                        popUpTo(Routes.SignUp) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Verification) {
            val previousRoute = navController.previousBackStackEntry?.destination?.route
            VerificationScreen(
                onVerificationSuccess = {
                    // If coming from SignUp, go to IdScan; otherwise go back
                    if (previousRoute == Routes.Login || previousRoute == Routes.SignUp) {
                        navController.navigate(Routes.IdScan) {
                            popUpTo(Routes.Verification) { inclusive = true }
                        }
                    } else {
                        navController.popBackStack()
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
            FingerprintScreen(
                onNext = {
                    navController.navigate(Routes.Main) {
                        popUpTo(Routes.Fingerprint) { inclusive = true }
                    }
                }
            )
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
        composable(Routes.Map) {
            MapScreen(navController)
        }
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

        // Review Summary (AI-Powered)
        composable(
            route = Routes.ReviewSummary,
            arguments = listOf(
                navArgument("propertyId") { type = NavType.StringType },
                navArgument("propertyName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val propertyId = backStackEntry.arguments?.getString("propertyId") ?: ""
            val propertyName = backStackEntry.arguments?.getString("propertyName") ?: ""
            ReviewSummaryScreen(
                propertyId = propertyId,
                propertyName = propertyName,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Dashboard
        composable(Routes.Dashboard) {
            DashboardScreen(navController)
        }
        
        // Visit Requests
        composable(Routes.VisitRequests) {
            VisitRequestsScreen(navController)
        }
        
        // My Visits
        composable(Routes.MyVisits) {
            MyVisitsScreen(navController)
        }

        // Chat
        composable(
            route = Routes.Chat,
            arguments = listOf(
                navArgument("visiteId") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val visiteId = backStackEntry.arguments?.getString("visiteId") ?: ""
            val title = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("title") ?: "Chat",
                "UTF-8"
            )
            ChatScreen(navController, visiteId, title)
        }
    }
}
