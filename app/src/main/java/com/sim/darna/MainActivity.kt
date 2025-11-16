package com.sim.darna

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.sim.darna.api.RetrofitClient
import com.sim.darna.navigation.AppNavGraph
import com.sim.darna.ui.theme.DarnaTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Android 12+ Splash Screen
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Initialize RetrofitClient with context for token management
        RetrofitClient.initialize(this)

        // Allow content to draw edge-to-edge (behind system bars)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Hide system bars for immersive fullscreen experience
        hideSystemBars()

        // Launch the full app navigation
        setContent {
            DarnaTheme {
                AppNavGraph() // ⬅️ Handles Splash → Login/Signup → MainScreen
            }
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemBars()
    }

    @SuppressLint("WrongConstant", "NewApi")
    private fun hideSystemBars() {
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(android.view.WindowInsets.Type.statusBars() or android.view.WindowInsets.Type.navigationBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}
