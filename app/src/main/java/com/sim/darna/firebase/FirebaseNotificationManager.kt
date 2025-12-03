package com.sim.darna.firebase

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.sim.darna.auth.SessionManager
import kotlinx.coroutines.tasks.await

object FirebaseNotificationManager {
    private const val TAG = "FirebaseNotificationManager"

    /**
     * Récupère le token FCM et l'enregistre dans le backend
     */
    suspend fun registerToken(context: Context, sessionManager: SessionManager, api: FirebaseNotificationApi) {
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            Log.d(TAG, "Token FCM récupéré: $token")
            
            if (token.isNullOrBlank()) {
                Log.w(TAG, "Token FCM est vide ou null")
                return
            }
            
            val userId = sessionManager.getUserId()
            if (userId != null) {
                // Enregistrer le token dans le backend
                val request = RegisterTokenRequest(
                    fcmToken = token,
                    platform = "ANDROID"
                )
                
                try {
                    val response = api.registerToken(request)
                    if (response.success) {
                        Log.d(TAG, "Token FCM enregistré avec succès pour l'utilisateur $userId")
                    } else {
                        Log.w(TAG, "L'enregistrement du token FCM a échoué (success=false)")
                    }
                } catch (e: retrofit2.HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    Log.e(TAG, "Erreur HTTP lors de l'enregistrement du token FCM: ${e.code()} - $errorBody", e)
                } catch (e: Exception) {
                    Log.e(TAG, "Erreur lors de l'enregistrement du token FCM dans le backend", e)
                    e.printStackTrace()
                }
            } else {
                Log.w(TAG, "Aucune session active, le token ne sera pas enregistré")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la récupération ou l'enregistrement du token FCM", e)
            e.printStackTrace()
        }
    }

    /**
     * Récupère le token FCM de manière synchrone (pour les callbacks)
     */
    fun getToken(callback: (String?) -> Unit) {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Échec de la récupération du token FCM", task.exception)
                    callback(null)
                    return@OnCompleteListener
                }

                val token = task.result
                Log.d(TAG, "Token FCM: $token")
                callback(token)
            })
    }
}















