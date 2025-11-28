package com.sim.darna

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.sim.darna.navigation.AppNavGraph
import com.sim.darna.ui.theme.DarnaTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Android 12+ Splash Screen
        installSplashScreen()
        super.onCreate(savedInstanceState)

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ (API 30+)
            val controller = WindowInsetsControllerCompat(window, window.decorView)
            controller.hide(android.view.WindowInsets.Type.statusBars() or android.view.WindowInsets.Type.navigationBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            // Android 10 and below (API 24-29)
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            )
        }
    }
}
