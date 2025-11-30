package com.sim.darna.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.sim.darna.MainActivity
import com.sim.darna.R
import com.sim.darna.auth.SessionManager
import com.sim.darna.network.NetworkConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class DarnaFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Nouveau token FCM reçu: $token")
        
        // Enregistrer automatiquement le nouveau token dans le backend
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val sessionManager = SessionManager(applicationContext)
                val userId = sessionManager.getUserId()
                
                if (userId != null) {
                    // Créer l'API pour enregistrer le token
                    val retrofit = createRetrofitInstance()
                    val api = retrofit.create(FirebaseNotificationApi::class.java)
                    
                    val request = RegisterTokenRequest(
                        fcmToken = token,
                        platform = "ANDROID"
                    )
                    
                    try {
                        val response = api.registerToken(request)
                        if (response.success) {
                            Log.d(TAG, "✅ Nouveau token FCM enregistré avec succès pour l'utilisateur $userId")
                        } else {
                            Log.w(TAG, "⚠️ L'enregistrement du token FCM a retourné success=false pour l'utilisateur $userId")
                        }
                    } catch (e: retrofit2.HttpException) {
                        val errorBody = e.response()?.errorBody()?.string()
                        Log.e(TAG, "❌ Erreur HTTP ${e.code()} lors de l'enregistrement du token FCM: $errorBody", e)
                        // Ne pas faire échouer, on réessayera plus tard
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Erreur lors de l'enregistrement du nouveau token FCM", e)
                        e.printStackTrace()
                        // Ne pas faire échouer, on réessayera plus tard
                    }
                } else {
                    Log.w(TAG, "⚠️ Aucune session active, le nouveau token ne sera pas enregistré immédiatement")
                    // Le token sera enregistré lors de la prochaine connexion
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erreur critique lors de l'enregistrement du nouveau token FCM", e)
                e.printStackTrace()
            }
        }
    }
    
    private fun createRetrofitInstance(): Retrofit {
        // Utiliser NetworkConfig pour obtenir l'URL de base
        val baseUrl = NetworkConfig.getBaseUrl(applicationContext)
        val normalizedBaseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                try {
                    val sessionManager = SessionManager(applicationContext)
                    val token = runBlocking { sessionManager.getToken() }
                    val requestBuilder = chain.request().newBuilder()
                    if (!token.isNullOrBlank()) {
                        requestBuilder.addHeader("Authorization", "Bearer $token")
                    }
                    requestBuilder.addHeader("Accept", "application/json")
                    chain.proceed(requestBuilder.build())
                } catch (e: Exception) {
                    chain.proceed(chain.request().newBuilder()
                        .addHeader("Accept", "application/json")
                        .build())
                }
            }
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
        
        return Retrofit.Builder()
            .baseUrl(normalizedBaseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "🔔 Notification reçue: ${remoteMessage.messageId}")
        Log.d(TAG, "📨 Message depuis: ${remoteMessage.from}")
        Log.d(TAG, "📦 Données: ${remoteMessage.data}")
        Log.d(TAG, "📋 Notification payload: ${remoteMessage.notification?.title} - ${remoteMessage.notification?.body}")

        // Vérifier si le message contient une notification (priorité)
        remoteMessage.notification?.let { notification ->
            val title = notification.title ?: "Notification Darna"
            val body = notification.body ?: "Nouvelle notification"
            Log.d(TAG, "✅ Affichage notification avec titre: $title, corps: $body")
            showNotification(title, body, remoteMessage.data)
            return
        }

        // Si pas de notification payload, vérifier les données
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "📊 Traitement d'un message data-only")
            handleDataMessage(remoteMessage)
        } else {
            Log.w(TAG, "⚠️ Message reçu sans notification ni données")
        }
    }

    private fun handleDataMessage(remoteMessage: RemoteMessage) {
        val notificationId = remoteMessage.data["notificationId"]
        val type = remoteMessage.data["type"]
        val visitId = remoteMessage.data["visitId"]
        val housingId = remoteMessage.data["housingId"]
        
        // Essayer de récupérer le titre et le corps depuis les données
        val title = remoteMessage.data["title"] 
            ?: when (type) {
                "VISIT_ACCEPTED" -> "Visite acceptée"
                "VISIT_REFUSED" -> "Visite refusée"
                "VISIT_REMINDER_J2" -> "Rappel de visite (J-2)"
                "VISIT_REMINDER_J1" -> "Rappel de visite (J-1)"
                "VISIT_REMINDER_H2" -> "Rappel de visite (H-2)"
                "VISIT_REMINDER_H1" -> "Rappel de visite (H-1)"
                "VISIT_REMINDER_H30" -> "Rappel de visite (30 min)"
                else -> "Notification Darna"
            }
        
        val body = remoteMessage.data["body"] 
            ?: remoteMessage.data["message"] 
            ?: "Nouvelle notification"

        Log.d(TAG, "Traitement du message data-only - ID: $notificationId, Type: $type, VisitId: $visitId")
        
        // Toujours afficher une notification pour les messages data-only
        // Cela garantit que les notifications sont toujours visibles même si l'app est en arrière-plan
        showNotification(title, body, remoteMessage.data)
        
        // Mettre à jour les notifications dans l'app si nécessaire
        // Cela sera géré par le ViewModel qui écoute les changements
    }

    private fun showNotification(title: String, body: String, data: Map<String, String>) {
        val notificationId = data["notificationId"]?.toIntOrNull() ?: System.currentTimeMillis().toInt()
        
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Ajouter les données de la notification pour navigation
            data["notificationId"]?.let { putExtra("notificationId", it) }
            data["visitId"]?.let { putExtra("visitId", it) }
            data["housingId"]?.let { putExtra("housingId", it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = getString(R.string.default_notification_channel_id)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Le canal de notification devrait déjà être créé dans MainActivity.onCreate()
        // Mais on le crée quand même ici pour être sûr (au cas où MainActivity n'a pas été lancée)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val existingChannel = notificationManager.getNotificationChannel(channelId)
            if (existingChannel == null) {
                val channel = NotificationChannel(
                    channelId,
                    "Notifications Darna",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications pour les visites et logements"
                    enableVibration(true)
                    enableLights(true)
                }
                notificationManager.createNotificationChannel(channel)
                Log.d(TAG, "Canal de notification créé dans DarnaFirebaseMessagingService")
            }
        }

        notificationManager.notify(notificationId, notificationBuilder.build())
        Log.d(TAG, "✅ Notification affichée avec succès - ID: $notificationId, Titre: $title")
    }

    companion object {
        private const val TAG = "DarnaFCMService"
    }
}



