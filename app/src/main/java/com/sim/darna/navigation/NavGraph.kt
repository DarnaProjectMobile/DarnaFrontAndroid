package com.sim.darna.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
            MainScreen()
        }

        composable(
            route = Routes.PropertyDetail, // This now correctly uses "property_detail/{propertyName}"
            arguments = listOf(navArgument("propertyName") { type = NavType.StringType })
        ) { backStackEntry ->
            // Extract the argument
            val propertyName = backStackEntry.arguments?.getString("propertyName")?.let {
                URLDecoder.decode(it, StandardCharsets.UTF_8.name())
            }


        }

    }
}

