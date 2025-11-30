package com.sim.darna.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }).build()

    val api: PaymentApi by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:3000/") // localhost Android
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(PaymentApi::class.java)
    }
}
