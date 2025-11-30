package com.sim.darna

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.sim.darna.navigation.AppNavGraph
import com.sim.darna.ui.theme.DarnaTheme

class MainActivity : ComponentActivity() {

    // Launcher pour demander la permission POST_NOTIFICATIONS (Android 13+)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission accordée, le canal de notification est déjà créé
        } else {
            // Permission refusée - l'utilisateur ne recevra pas de notifications
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Android 12+ Splash Screen
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Créer le canal de notification au démarrage de l'app
        createNotificationChannel()

        // Demander la permission POST_NOTIFICATIONS pour Android 13+ (API 33+)
        requestNotificationPermission()

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
    
    /**
     * Crée le canal de notification au démarrage de l'app
     * Cela évite de le créer à chaque notification
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = getString(R.string.default_notification_channel_id)
            val channelName = "Notifications Darna"
            val channelDescription = "Notifications pour les visites et logements"
            val importance = NotificationManager.IMPORTANCE_HIGH
            
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
                enableVibration(true)
                enableLights(true)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Demande la permission POST_NOTIFICATIONS pour Android 13+ (API 33+)
     * Cette permission est requise pour afficher des notifications
     */
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33+) nécessite la permission explicite
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission déjà accordée
                }
                else -> {
                    // Demander la permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
        // Pour Android 12 et inférieur, la permission n'est pas nécessaire
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
