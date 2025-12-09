package com.sim.darna.di

import com.sim.darna.auth.UserSessionManager
import com.sim.darna.data.adapter.DateAdapter
import com.sim.darna.data.remote.PubliciteApi
import com.sim.darna.utils.ApiConfig
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.Date
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttp(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        return OkHttpClient.Builder()
            .addInterceptor { chain: Interceptor.Chain ->
                val original = chain.request()
                val builder = original.newBuilder()
                UserSessionManager.currentToken?.let { token ->
                    builder.addHeader("Authorization", "Bearer $token")
                }
                chain.proceed(builder.build())
            }
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        val moshi = Moshi.Builder()
            .add(Date::class.java, DateAdapter())
            .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
            .build()

        return Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }


    @Provides
    @Singleton
    fun providePubliciteApi(retrofit: Retrofit): PubliciteApi =
        retrofit.create(PubliciteApi::class.java)
}
