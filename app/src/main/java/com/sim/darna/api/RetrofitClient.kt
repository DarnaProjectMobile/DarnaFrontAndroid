package com.sim.darna.api

import android.content.Context
import android.util.Log
import com.sim.darna.auth.AuthApi
import com.sim.darna.utils.TokenManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "http://10.61.177.155:3000/"

    @Volatile
    private var retrofit: Retrofit? = null

    @Volatile
    private var tokenManager: TokenManager? = null

    fun initialize(context: Context) {
        tokenManager = TokenManager.getInstance(context)
        retrofit = null
        Log.d("RetrofitClient", "Initialized with TokenManager")
    }

    fun saveToken(token: String) {
        tokenManager?.saveToken(token)
        retrofit = null
        Log.d("RetrofitClient", "Token saved: ${token.take(15)}...")
    }

    fun hasToken(): Boolean = tokenManager?.hasToken() ?: false

    fun getTokenManager(): TokenManager? = tokenManager

    private fun buildRetrofit(): Retrofit {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

        val authInterceptor = Interceptor { chain ->
            val request = chain.request()
            val token = tokenManager?.getToken()
            val newRequest = if (!token.isNullOrEmpty()) {
                request.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
            } else request
            chain.proceed(newRequest)
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun getRetrofit(): Retrofit {
        return retrofit ?: synchronized(this) {
            retrofit ?: buildRetrofit().also { retrofit = it }
        }
    }

    val authApi: AuthApi
        get() = getRetrofit().create(AuthApi::class.java)

    val api: ApiService
        get() = getRetrofit().create(ApiService::class.java)
}
