// app/src/main/java/com/sim/darna/auth/UserApi.kt
package com.sim.darna.auth

import com.sim.darna.model.UserDto
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.HTTP
import retrofit2.http.PATCH
import retrofit2.http.POST
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

data class DeviceTokenRequest(
    @SerializedName("deviceToken") val deviceToken: String,
)

data class MessageResponse(
    val message: String,
)

interface UserApi {
    // PATCH /users/me - JSON body (matches NestJS @Body)
    @PATCH("users/me")
    fun updateMe(
        @Body request: UpdateUserRequest
    ): Call<UserDto>

    @POST("users/me/device-token")
    fun registerDeviceToken(
        @Body request: DeviceTokenRequest,
    ): Call<MessageResponse>

    @HTTP(method = "DELETE", path = "users/me/device-token", hasBody = true)
    fun removeDeviceToken(
        @Body request: DeviceTokenRequest,
    ): Call<MessageResponse>

    companion object {
        fun create(baseUrl: String): UserApi {
            val interceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
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