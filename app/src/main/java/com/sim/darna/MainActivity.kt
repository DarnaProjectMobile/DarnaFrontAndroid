package com.sim.darna

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        var contentSet = false
        
        try {
            // Appeler super.onCreate() EN PREMIER pour initialiser correctement l'activité
            super.onCreate(savedInstanceState)
            
            // Android 12+ Splash Screen - après super.onCreate()
            try {
                installSplashScreen()
            } catch (e: Exception) {
                Log.e("MainActivity", "Erreur lors de l'installation du splash screen", e)
                // Continuer même si le splash screen échoue
            }

            // Créer le canal de notification au démarrage de l'app
            try {
                createNotificationChannel()
            } catch (e: Exception) {
                Log.e("MainActivity", "Erreur lors de la création du canal de notification", e)
            }

            // Demander la permission POST_NOTIFICATIONS pour Android 13+ (API 33+)
            try {
                requestNotificationPermission()
            } catch (e: Exception) {
                Log.e("MainActivity", "Erreur lors de la demande de permission", e)
            }

            // Allow content to draw edge-to-edge (behind system bars)
            try {
                if (window != null) {
                    WindowCompat.setDecorFitsSystemWindows(window, false)
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Erreur lors de la configuration de la fenêtre", e)
            }

            // Hide system bars for immersive fullscreen experience
            try {
                if (window != null) {
                    hideSystemBars()
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Erreur lors du masquage des barres système", e)
            }

            // Launch the full app navigation
            setContent {
                contentSet = true
                DarnaTheme {
                    AppNavGraph() // ⬅️ Handles Splash → Login/Signup → MainScreen
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Erreur critique dans onCreate", e)
            // Afficher une interface d'erreur simple si le contenu n'a pas encore été défini
            if (!contentSet) {
                try {
                    setContent {
                        DarnaTheme {
                            androidx.compose.material3.Surface {
                                androidx.compose.material3.Text(
                                    text = "Erreur lors du démarrage de l'application",
                                    modifier = androidx.compose.ui.Modifier
                                        .fillMaxSize()
                                        .wrapContentSize(androidx.compose.ui.Alignment.Center)
                                )
                            }
                        }
                    }
                } catch (e2: Exception) {
                    Log.e("MainActivity", "Impossible d'afficher l'interface d'erreur", e2)
                    // Ne pas relancer l'exception pour éviter un crash en cascade
                }
            }
        }
    }
    
    /**
     * Crée le canal de notification au démarrage de l'app
     * Cela évite de le créer à chaque notification
     */
    private fun createNotificationChannel() {
        try {
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
        } catch (e: Exception) {
            Log.e("MainActivity", "Erreur lors de la création du canal de notification", e)
            throw e
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
        try {
            hideSystemBars()
        } catch (e: Exception) {
            Log.e("MainActivity", "Erreur lors du masquage des barres système dans onResume", e)
        }
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
