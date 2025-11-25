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
    const val IdScan = "idscan"
    const val Selfie = "selfie"
    const val Fingerprint = "fingerprint"
    const val Main = "main"
    const val PropertyDetail = "property_detail"
    const val PropertyDetailWithId = "property_detail/{propertyId}"
    const val ResetPassword = "reset_password"
    const val Reviews = "reviews"
    const val UpdateProfile = "update_profile"
    const val Favorites = "favorites"
    const val Reservations = "reservations"
    const val BookProperty = "book_property/{propertyId}"
    const val PropertyBookings = "property_bookings/{propertyId}"
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
        composable(Routes.Reviews) { ReviewsScreen() }
        composable(Routes.ForgotPassword) { ForgotPasswordScreen(navController) }
        composable(Routes.ResetPassword) { ResetPasswordScreen(navController) }
        composable(Routes.Favorites) { FavoritesScreen(navController) }
        composable(Routes.Reservations) { ReservationsScreen(navController) }
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
    }
}
