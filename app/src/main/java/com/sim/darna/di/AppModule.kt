package com.sim.darna.di

import android.content.Context
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.sim.darna.data.remote.ArrayToObjectInterceptor
import com.sim.darna.data.remote.AuthInterceptor
import com.sim.darna.data.remote.PubliciteApi
import com.sim.darna.utils.Constants
import com.sim.darna.utils.SessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSessionManager(@ApplicationContext context: Context): SessionManager {
        return SessionManager(context)
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(sessionManager: SessionManager): AuthInterceptor {
        return AuthInterceptor(sessionManager)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val arrayToObjectInterceptor = ArrayToObjectInterceptor()

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(arrayToObjectInterceptor) // Transforme les tableaux en objets pour getPubliciteById
            .addInterceptor(loggingInterceptor)
            .connectTimeout(Constants.TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(Constants.TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(Constants.TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = false
        }

        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun providePubliciteApi(retrofit: Retrofit): PubliciteApi {
        return retrofit.create(PubliciteApi::class.java)
    }
}

