// app/src/main/java/com/sim/darna/auth/UserApi.kt
package com.sim.darna.auth

import com.sim.darna.model.UserDto
import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

// DTO matching your NestJS UpdateUserDto
// Only username, email, and bio are truly required based on your DTO
data class UpdateUserRequest(
    val username: String,
    val email: String,
    val bio: String = "User bio",  // Required by DTO
    val password: String = "KeepCurrentPassword123!",  // Required by DTO - send dummy to avoid re-hashing
    val numTel: String? = null,
    val dateDeNaissance: String? = null,
    val gender: String? = null
)

interface UserApi {
    // PATCH /users/me - JSON body (matches NestJS @Body)
    @PATCH("users/me")
    fun updateMe(
        @Body request: UpdateUserRequest
    ): Call<UserDto>

    companion object {
        fun create(baseUrl: String): UserApi {
            val interceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(UserApi::class.java)
        }
    }
}