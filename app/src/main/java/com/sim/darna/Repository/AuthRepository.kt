package com.sim.darna.repository

import android.content.SharedPreferences
import com.sim.darna.auth.AuthApi
import com.sim.darna.model.LoginRequest
import com.sim.darna.model.LoginResponse
import com.sim.darna.model.RegisterResponse
import com.sim.darna.model.UserDto
import retrofit2.Call

class AuthRepository(
    private val api: AuthApi,
    private val sharedPreferences: SharedPreferences
) {
    
    companion object {
        private const val PREF_NAME = "DarnaPrefs"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER = "current_user"
    }
    
    fun saveToken(token: String) {
        sharedPreferences.edit()
            .putString(KEY_AUTH_TOKEN, token)
            .apply()
    }
    
    fun getSavedToken(): String? {
        return sharedPreferences.getString(KEY_AUTH_TOKEN, null)
    }
    
    fun saveUser(user: UserDto) {
        // You might want to use Gson or similar to convert UserDto to JSON string
        // For now, we'll just store the email as a simple example
        sharedPreferences.edit()
            .putString(KEY_USER, user.email)
            .apply()
    }
    
    fun getCurrentUser(token: String): UserDto? {
        // In a real app, you would make an API call to get the current user
        // For now, we'll just return null or a default user
        val email = sharedPreferences.getString(KEY_USER, null) ?: return null
        return UserDto(
            id = "0", // You should get this from your API
            username = "",
            email = email,
            role = ""
        )
    }
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
