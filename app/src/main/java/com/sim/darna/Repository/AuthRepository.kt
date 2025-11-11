package com.sim.darna.Repository

import com.sim.darna.auth.AuthApi
import com.sim.darna.model.LoginRequest
import com.sim.darna.model.LoginResponse
import com.sim.darna.model.RegisterResponse
import retrofit2.Call

class AuthRepository(private val api: AuthApi) {
    fun login(request: LoginRequest): Call<LoginResponse> {
        return api.login(request)
    }

    fun registerMultipart(
        username: okhttp3.RequestBody,
        email: okhttp3.RequestBody,
        password: okhttp3.RequestBody,
        role: okhttp3.RequestBody,
        dateDeNaissance: okhttp3.RequestBody,
        numTel: okhttp3.RequestBody,
        gender: okhttp3.RequestBody,
        image: okhttp3.MultipartBody.Part?
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
