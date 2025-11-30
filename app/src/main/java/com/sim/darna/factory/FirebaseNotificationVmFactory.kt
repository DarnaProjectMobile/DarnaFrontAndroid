package com.sim.darna.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sim.darna.auth.SessionManager
import com.sim.darna.firebase.FirebaseNotificationApi
import com.sim.darna.firebase.FirebaseNotificationRepository
import com.sim.darna.firebase.FirebaseNotificationViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking

class FirebaseNotificationVmFactory(
    private val baseUrl: String,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                try {
                    val token = runBlocking { sessionManager.getToken() }
                    val requestBuilder = chain.request().newBuilder()
                    
                    if (!token.isNullOrBlank()) {
                        requestBuilder.addHeader("Authorization", "Bearer $token")
                    }
                    requestBuilder.addHeader("Accept", "application/json")
                    
                    val request = requestBuilder.build()
                    val response = chain.proceed(request)
                    
                    // Si on reçoit une erreur 401, la session a expiré
                    if (response.code == 401) {
                        runBlocking { sessionManager.clearSession() }
                    }
                    
                    response
                } catch (e: Exception) {
                    // En cas d'erreur lors de la récupération du token, continuer sans token
                    val request = chain.request().newBuilder()
                        .addHeader("Accept", "application/json")
                        .build()
                    chain.proceed(request)
                }
            }
            .addInterceptor(logging)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        // S'assurer que l'URL de base se termine par un slash
        val normalizedBaseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        
        val retrofit = Retrofit.Builder()
            .baseUrl(normalizedBaseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(FirebaseNotificationApi::class.java)
        val repo = FirebaseNotificationRepository(api)
        return FirebaseNotificationViewModel(repo) as T
    }
}








