package com.sim.darna.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sim.darna.screens.*

/**
 * ✅ Centralized route definitions for clarity and reuse.
 */
object Routes {
    const val Splash = "splash"
    const val Login = "login"
    const val SignUp = "signup"
    const val ForgotPassword = "forgot_password"
    const val IdScan = "idscan"
    const val Selfie = "selfie"
    const val Fingerprint = "fingerprint"
    const val Main = "main"
    const val PropertyDetail = "property_detail"
    const val ResetPassword = "reset_password"
    const val Home = "home"
    const val AddAnnonce = "add_annonce"
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

        // ✅ LOGIN → HOME or SIGNUP
        composable(Routes.Login) {
            LoginScreen(
                navController = navController
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

        // ✅ FINGERPRINT → HOME
        composable(Routes.Fingerprint) {
            FingerprintScreen {
                navController.navigate("${Routes.Home}?username=") {
                    popUpTo(Routes.Fingerprint) { inclusive = true }
                }
            }
        }

        // ✅ HOME SCREEN (Main content)
        composable(
            route = "${Routes.Home}?username={username}&userId={userId}&role={role}",
            arguments = listOf(
                navArgument("username") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("userId") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("role") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username").orEmpty()
            val userId = backStackEntry.arguments?.getString("userId").orEmpty()
            val role = backStackEntry.arguments?.getString("role").orEmpty()
            HomeScreen(navController = navController, username = username, userId = userId, role = role)
        }

        composable(
            route = "${Routes.PropertyDetail}?id={id}&userId={userId}&role={role}",
            arguments = listOf(
                navArgument("id") {
                    type = NavType.StringType
                },
                navArgument("userId") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("role") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            val userId = backStackEntry.arguments?.getString("userId").orEmpty()
            val role = backStackEntry.arguments?.getString("role").orEmpty()
            PropertyDetailScreen(navController = navController, annonceId = id, userId = userId, role = role)
        }

        composable(Routes.AddAnnonce) {
            AddAnnonceScreen(navController = navController)
        }

        composable(Routes.ForgotPassword) {
            ForgotPasswordScreen(navController = navController)
        }

        composable(Routes.ResetPassword) {
            ResetPasswordScreen(navController = navController)
        }
    }
}