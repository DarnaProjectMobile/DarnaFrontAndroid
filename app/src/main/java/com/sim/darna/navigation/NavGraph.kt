package com.sim.darna.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sim.darna.screens.*

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
    const val Reviews = "reviews"
}

@Composable
fun AppNavGraph(navController: NavHostController = rememberNavController()) {

    NavHost(
        navController = navController,
        startDestination = Routes.Splash
    ) {

        composable(Routes.Splash) {
            SplashScreen {
                navController.navigate(Routes.Login) {
                    popUpTo(Routes.Splash) { inclusive = true }
                }
            }
        }

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

        // ⭐ MAIN APP (BOTTOM NAVIGATION)
        composable(Routes.Main) { MainScreen(navController) }

        // ⭐ FULL SCREEN PAGES
        composable(Routes.PropertyDetail) { PropertyDetailScreen(navController) }
        composable(Routes.Reviews) { ReviewsScreen() }
        composable(Routes.ForgotPassword) { ForgotPasswordScreen(navController) }
        composable(Routes.ResetPassword) { ResetPasswordScreen(navController) }
    }
}
