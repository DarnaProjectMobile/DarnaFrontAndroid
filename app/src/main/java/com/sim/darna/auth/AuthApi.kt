package com.sim.darna.auth

import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface AuthApi {

    // 🔹 Login (en JSON) - Utilise suspend pour mieux gérer les timeouts
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    // 🔹 Register (multipart/form-data)
    @Multipart
    @POST("auth/register")
    fun register(
        @Part("username") username: RequestBody,
        @Part("email") email: RequestBody,
        @Part("password") password: RequestBody,
        @Part("role") role: RequestBody,
        @Part("dateDeNaissance") dateDeNaissance: RequestBody,
        @Part("numTel") numTel: RequestBody,
        @Part("gender") gender: RequestBody,
        @Part image: MultipartBody.Part? = null
    ): Call<RegisterResponse>

    companion object {
        fun create(baseUrl: String): AuthApi {
            // S'assurer que l'URL de base se termine par un slash (requis par Retrofit)
            val normalizedBaseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
            
            // 🔍 Logger pour déboguer les requêtes dans Logcat
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            // 🕒 Configuration du client HTTP avec timeouts optimisés pour connexions réseau
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS) // 30 secondes pour la connexion initiale
                .readTimeout(30, TimeUnit.SECONDS) // 30 secondes pour lire la réponse
                .writeTimeout(30, TimeUnit.SECONDS) // 30 secondes pour envoyer les données
                .retryOnConnectionFailure(true) // Réessayer automatiquement en cas d'échec
                .build()

            // 🧱 Retrofit instance
            return Retrofit.Builder()
                .baseUrl(normalizedBaseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(AuthApi::class.java)
        }
    }
}
