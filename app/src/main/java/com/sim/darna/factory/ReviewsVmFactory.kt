package com.sim.darna.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sim.darna.auth.SessionManager
import com.sim.darna.reviews.ReviewsApi
import com.sim.darna.reviews.ReviewsRepository
import com.sim.darna.reviews.ReviewsViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking

class ReviewsVmFactory(
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

                    chain.proceed(requestBuilder.build())
                } catch (e: Exception) {
                    val request = chain.request().newBuilder()
                        .addHeader("Accept", "application/json")
                        .build()
                    chain.proceed(request)
                }
            }
            .addInterceptor(logging)
            .connectTimeout(60, TimeUnit.SECONDS) // Augmenté à 60 secondes pour connexions lentes
            .readTimeout(60, TimeUnit.SECONDS) // Augmenté à 60 secondes pour réponses lentes
            .writeTimeout(60, TimeUnit.SECONDS) // Augmenté à 60 secondes pour envoi de données
            .retryOnConnectionFailure(true) // Réessayer en cas d'échec de connexion
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(ReviewsApi::class.java)
        val repo = ReviewsRepository(api)
        return ReviewsViewModel(repo) as T
    }
}