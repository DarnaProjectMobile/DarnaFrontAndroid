package com.sim.darna.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sim.darna.screens.*
import com.sim.darna.ui.screens.PaymentScreen
import com.sim.darna.viewmodel.PaymentViewModel
import java.net.URLDecoder
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
    const val PropertyDetail = "property_detail/{propertyName}"
    const val Reviews = "reviews/{propertyId}"

    // Publicités routes
    const val PubliciteList = "publicite_list"
    const val PubliciteDetail = "publicite_detail/{publiciteId}"
    const val AddPublicite = "add_publicite"
    const val EditPublicite = "edit_publicite/{publiciteId}"

    const val Payment = "payment"
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
            route = Routes.PropertyDetail,
            arguments = listOf(navArgument("propertyName") { type = NavType.StringType })
        ) { backStackEntry ->
            val propertyName = backStackEntry.arguments?.getString("propertyName")?.let {
                URLDecoder.decode(it, StandardCharsets.UTF_8.name())
            } ?: "Détails du bien"

            PropertyDetailScreen(
                navController = navController,
                title = propertyName
            )
        }

        // ✅ PUBLICITÉS ROUTES
        composable(Routes.PubliciteList) {
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
                    // La suppression sera gérée dans le ViewModel
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
            val viewModel: PaymentViewModel = hiltViewModel()
            PaymentScreen(navController = navController, viewModel = viewModel)
        }

    }
}