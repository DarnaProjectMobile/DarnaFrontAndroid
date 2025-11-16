package com.sim.darna.repository

import com.sim.darna.api.RetrofitClient
import com.sim.darna.auth.AuthApi
import com.sim.darna.model.LoginRequest
import com.sim.darna.model.LoginResponse
import com.sim.darna.model.RegisterResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call

class AuthRepository(private val api: AuthApi = RetrofitClient.authApi) {

    fun login(request: LoginRequest): Call<LoginResponse> {
        return api.login(request)
    }

    fun registerMultipart(
        username: RequestBody,
        email: RequestBody,
        password: RequestBody,
        role: RequestBody,
        dateDeNaissance: RequestBody,
        numTel: RequestBody,
        gender: RequestBody,
        image: MultipartBody.Part?
    ): Call<RegisterResponse> =
        api.register(username, email, password, role, dateDeNaissance, numTel, gender, image)

    fun forgotPassword(email: String): Call<AuthApi.ForgotPasswordResponse> {
        val request = AuthApi.ForgotPasswordRequest(email)
        return api.forgotPassword(request)
    }

    fun resetPassword(code: String, newPass: String, confirmPass: String): Call<AuthApi.ResetPasswordResponse> {
        val request = AuthApi.ResetPasswordRequest(code, newPass, confirmPass)
        return api.resetPassword(request)
    }
}
