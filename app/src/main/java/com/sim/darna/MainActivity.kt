package com.sim.darna

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.sim.darna.components.PaymentSheetManager
import com.sim.darna.navigation.AppNavGraph
import com.sim.darna.notifications.FirebaseTokenRegistrar
import com.sim.darna.ui.theme.DarnaTheme
import com.stripe.android.paymentsheet.PaymentSheetResult
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {

        // Android 12+ splash screen
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge content
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Enable fullscreen (status + navigation bars hidden)
        hideSystemBars()
        requestNotificationPermissionIfNeeded()
        // Move non-UI initialization to background to avoid ANR
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                FirebaseTokenRegistrar.syncCurrentToken(this@MainActivity)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Initialiser PaymentSheetManager (doit être fait dans onCreate, avant STARTED)
        try {
            PaymentSheetManager.initialize(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        setContent {
            DarnaTheme {
                val navController = androidx.navigation.compose.rememberNavController()
                
                // Handle notification intent
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    intent?.let {
                        val type = it.getStringExtra("type")
                        val visitId = it.getStringExtra("visitId")
                        val housingId = it.getStringExtra("housingId")
                        
                        if (type != null) {
                            when (type) {
                                "VISIT_REQUEST" -> {
                                    navController.navigate(com.sim.darna.navigation.Routes.VisitRequests)
                                }
                                "NEW_MESSAGE" -> {
                                    if (visitId != null) {
                                        // We might not have the title, default to "Discussion"
                                        val title = it.getStringExtra("housingTitle") ?: "Discussion"
                                        val encodedTitle = java.net.URLEncoder.encode(title, "UTF-8")
                                        navController.navigate("chat/$visitId/$encodedTitle")
                                    } else {
                                        navController.navigate(com.sim.darna.navigation.Routes.Notifications)
                                    }
                                }
                                "VISIT_ACCEPTED", "VISIT_REFUSED" -> {
                                    // For client, go to MyVisits
                                    navController.navigate(com.sim.darna.navigation.Routes.MyVisits)
                                }
                                else -> {
                                    navController.navigate(com.sim.darna.navigation.Routes.Notifications)
                                }
                            }
                        }
                    }
                }

                AppNavGraph(navController = navController)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        PaymentSheetManager.clear()
    }

    override fun onResume() {
        super.onResume()
        hideSystemBars()
    }

    /** Hide system bars safely on all Android versions */
    private fun hideSystemBars() {
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(
            WindowInsetsCompat.Type.statusBars() or
                    WindowInsetsCompat.Type.navigationBars()
        )
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        val alreadyGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED

        if (!alreadyGranted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
