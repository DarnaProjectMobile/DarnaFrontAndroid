package com.sim.darna

import android.app.Application
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import okhttp3.OkHttpClient
import java.io.PrintWriter
import java.io.StringWriter
import java.util.concurrent.TimeUnit

class DarnaApplication : Application(), ImageLoaderFactory {
    
    override fun onCreate() {
        super.onCreate()
        setupGlobalExceptionHandler()
    }
    
    /**
     * Configure un gestionnaire global d'exceptions non capturées
     * pour logger les erreurs avant qu'elles ne causent un crash
     * et éviter les problèmes de visibilité avec le dialogue d'erreur système
     */
    private fun setupGlobalExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            try {
                // Logger l'erreur de manière détaillée AVANT que le système ne la gère
                logException(thread, exception)
                
                // Appeler le handler par défaut pour permettre au système de gérer le crash
                // Ne pas tuer le processus manuellement - laisser Android le gérer
                defaultHandler?.uncaughtException(thread, exception)
            } catch (e: Exception) {
                // Si le handler par défaut échoue, on log juste l'erreur
                Log.e("DarnaApplication", "Erreur dans le handler d'exception par défaut", e)
                Log.e("DarnaApplication", "Exception originale", exception)
                // Ne pas tuer le processus - laisser le système le gérer naturellement
            }
        }
    }
    
    /**
     * Log une exception avec toutes les informations nécessaires
     */
    private fun logException(thread: Thread, exception: Throwable) {
        try {
            val stackTrace = StringWriter()
            exception.printStackTrace(PrintWriter(stackTrace))
            
            val errorReport = buildString {
                appendLine("═══════════════════════════════════════")
                appendLine("CRASH REPORT - ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}")
                appendLine("═══════════════════════════════════════")
                appendLine("Thread: ${thread.name}")
                appendLine("Exception: ${exception.javaClass.simpleName}")
                appendLine("Message: ${exception.message ?: "No message"}")
                appendLine("")
                appendLine("Stack Trace:")
                appendLine(stackTrace.toString())
                appendLine("═══════════════════════════════════════")
            }
            
            Log.e("DarnaApplication", "UNCAUGHT EXCEPTION:\n$errorReport")
            
            // Si vous avez un service de crash reporting (Firebase Crashlytics, etc.)
            // vous pouvez l'ajouter ici
            // FirebaseCrashlytics.getInstance().recordException(exception)
            
        } catch (e: Exception) {
            Log.e("DarnaApplication", "Erreur lors du logging de l'exception", e)
        }
    }

    override fun newImageLoader(): ImageLoader {
        // Créer un OkHttpClient qui respecte la configuration de sécurité réseau
        // Cet OkHttpClient utilisera automatiquement la network_security_config.xml
        // qui permet le trafic HTTP clair vers les adresses IP locales
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        return ImageLoader.Builder(this)
            .okHttpClient(okHttpClient)
            .build()
    }
}

