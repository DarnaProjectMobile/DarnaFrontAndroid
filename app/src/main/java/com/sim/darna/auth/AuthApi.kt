package com.sim.darna.auth

import com.sim.darna.model.LoginRequest
import com.sim.darna.model.LoginResponse
import com.sim.darna.model.RegisterResponse
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


    // 🔹 Login (en JSON)
    @POST("auth/login")
    fun login(@Body body: LoginRequest): Call<LoginResponse>

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
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build()

            // 🧱 Retrofit instance
            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(AuthApi::class.java)
        }
    }
    @POST("users/forgot-password")
    fun forgotPassword(@Body body: ForgotPasswordRequest): Call<ForgotPasswordResponse>


    @POST("users/reset-password")
    fun resetPassword(@Body body: ResetPasswordRequest): Call<ResetPasswordResponse>


    data class ForgotPasswordRequest(val email: String)
    data class ForgotPasswordResponse(val message: String)

    data class ResetPasswordRequest(
        val code: String,
        val newPassword: String,
        val confirmPassword: String
    )
    data class ResetPasswordResponse(val message: String)

}
