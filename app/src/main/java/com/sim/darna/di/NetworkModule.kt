package com.sim.darna.di

import com.sim.darna.auth.UserSessionManager
import com.sim.darna.data.adapter.DateAdapter
import com.sim.darna.data.remote.PubliciteApi
import com.sim.darna.data.remote.PubliciteUploadService
import com.sim.darna.data.remote.StripeService
import com.sim.darna.utils.ApiConfig
import com.sim.darna.utils.ApiConfig.BASE_URL
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
            .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
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
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }


    @Provides
    @Singleton
    fun providePubliciteApi(retrofit: Retrofit): PubliciteApi =
        retrofit.create(PubliciteApi::class.java)

    @Provides
    @Singleton
    fun provideStripeService(retrofit: Retrofit): StripeService =
        retrofit.create(StripeService::class.java)

    @Provides
    @Singleton
    fun providePubliciteUploadService(retrofit: Retrofit): PubliciteUploadService =
        retrofit.create(PubliciteUploadService::class.java)

    @Provides
    @Singleton
    fun provideCloudinaryApi(retrofit: Retrofit): com.sim.darna.data.remote.CloudinaryApi =
        retrofit.create(com.sim.darna.data.remote.CloudinaryApi::class.java)
}
