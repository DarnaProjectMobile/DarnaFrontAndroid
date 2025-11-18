package com.sim.darna.auth

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://192.168.1.195:3000/"

    /**
     * Creates a Retrofit instance with JWT token automatically added.
     */
    private fun getInstance(context: Context): Retrofit {

        val authInterceptor = Interceptor { chain ->
            val token = TokenStorage.getToken(context)
            val requestBuilder = chain.request().newBuilder()

            if (!token.isNullOrEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }

            chain.proceed(requestBuilder.build())
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    /**
     * API for reviews
     */
    fun reviewApi(context: Context): ReviewApi {
        return getInstance(context).create(ReviewApi::class.java)
    }
    fun reportApi(context: Context): ReportApi {
        return getInstance(context).create(ReportApi::class.java)
    }

}
