package com.sim.darna.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sim.darna.auth.SessionManager
import com.sim.darna.chat.ChatApi
import com.sim.darna.chat.ChatRepository
import com.sim.darna.chat.ChatViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking

class ChatVmFactory(
    private val baseUrl: String,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Récupérer le token de manière synchrone mais avec timeout pour éviter les blocages
        val token = try {
            kotlinx.coroutines.runBlocking(kotlinx.coroutines.Dispatchers.IO) {
                kotlinx.coroutines.withTimeout(2000) {
                    sessionManager.getToken()
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("ChatVmFactory", "Erreur lors de la récupération du token", e)
            null
        }
        
        val userId = try {
            kotlinx.coroutines.runBlocking(kotlinx.coroutines.Dispatchers.IO) {
                kotlinx.coroutines.withTimeout(2000) {
                    sessionManager.getUserId()
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("ChatVmFactory", "Erreur lors de la récupération du userId", e)
            null
        }
        
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Cache pour éviter les appels répétés au SessionManager dans l'interceptor
        var cachedToken: String? = token
        
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                try {
                    // Utiliser le token en cache, ou essayer de le récupérer avec timeout
                    val currentToken = cachedToken ?: try {
                        kotlinx.coroutines.runBlocking(kotlinx.coroutines.Dispatchers.IO) {
                            kotlinx.coroutines.withTimeout(1000) {
                                sessionManager.getToken()
                            }
                        }.also { cachedToken = it }
                    } catch (e: Exception) {
                        null
                    }
                    
                    val requestBuilder = chain.request().newBuilder()
                    
                    if (!currentToken.isNullOrBlank()) {
                        requestBuilder.addHeader("Authorization", "Bearer $currentToken")
                    }
                    requestBuilder.addHeader("Accept", "application/json")
                    
                    val request = requestBuilder.build()
                    val response = chain.proceed(request)
                    
                    // Si on reçoit une erreur 401, la session a expiré
                    if (response.code == 401) {
                        cachedToken = null
                        try {
                            kotlinx.coroutines.runBlocking(kotlinx.coroutines.Dispatchers.IO) {
                                kotlinx.coroutines.withTimeout(1000) {
                                    sessionManager.clearSession()
                                }
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("ChatVmFactory", "Erreur lors du clear session", e)
                        }
                    }
                    
                    response
                } catch (e: Exception) {
                    android.util.Log.e("ChatVmFactory", "Erreur dans l'interceptor", e)
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

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val chatApi = retrofit.create(ChatApi::class.java)
        val chatRepo = ChatRepository(chatApi)
        
        return ChatViewModel(chatRepo, baseUrl, token, userId) as T
    }
}

