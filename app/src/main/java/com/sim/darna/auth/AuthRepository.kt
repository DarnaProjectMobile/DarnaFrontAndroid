package com.sim.darna.auth

import retrofit2.Call

class AuthRepository(private val api: AuthApi) {
    suspend fun login(request: LoginRequest): LoginResponse {
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

    suspend fun forgotPassword(email: String): ForgotPasswordResponse {
        return api.forgotPassword(ForgotPasswordRequest(email))
    }

    suspend fun resetPassword(code: String, newPassword: String, confirmPassword: String): ResetPasswordResponse {
        return api.resetPassword(ResetPasswordRequest(code, newPassword, confirmPassword))
    }
}
