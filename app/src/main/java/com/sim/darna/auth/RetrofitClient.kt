package com.sim.darna.auth

import android.content.Context
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:3000/"

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
        
        // Gson that excludes null values from JSON (default behavior)
        val gson = GsonBuilder()
            .setLenient() // Allow lenient parsing
            .registerTypeAdapter(
                com.sim.darna.model.Property::class.java,
                com.sim.darna.model.PropertyTypeAdapter()
            )
            .registerTypeAdapter(String::class.java, com.sim.darna.model.UserDeserializer()) // For Property.user
            .registerTypeAdapter(com.sim.darna.model.BookingUser::class.java, com.sim.darna.model.BookingUserDeserializer()) // For Booking.user
            .create()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
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
    
    fun userApi(context: Context): UserApi {
        return getInstance(context).create(UserApi::class.java)
    }
    
    fun propertyApi(context: Context): PropertyApi {
        return getInstance(context).create(PropertyApi::class.java)
    }

}
