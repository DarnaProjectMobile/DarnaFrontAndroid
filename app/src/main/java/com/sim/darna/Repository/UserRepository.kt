package com.sim.darna.repository

import android.content.SharedPreferences
import com.sim.darna.auth.UserApi
import com.sim.darna.model.UserDto
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import java.io.File

class UserRepository(
    private val api: UserApi,
    private val sharedPreferences: SharedPreferences
) {

    /**
     * PATCH /users/me
     */
    fun updateUser(
        username: String,
        email: String,
        password: String? = null,
        imageFile: File? = null
    ): Call<UserDto> {

        val usernameBody = username.toRequestBody("text/plain".toMediaTypeOrNull())
        val emailBody = email.toRequestBody("text/plain".toMediaTypeOrNull())
        val passwordBody: RequestBody? =
            password?.toRequestBody("text/plain".toMediaTypeOrNull())

        val imagePart = imageFile?.let { file ->
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("image", file.name, requestFile)
        }

        return api.updateMe(
            username = usernameBody,
            email = emailBody,
            password = passwordBody,
            image = imagePart
        )
    }

    /**
     * Save user in SharedPreferences
     */
    fun saveUser(user: UserDto) {
        sharedPreferences.edit()
            .putString("user_id", user.id)
            .putString("username", user.username)
            .putString("email", user.email)
            .putString("role", user.role)
            .putString("image", user.image)   // ✅ FIX: save image!
            .apply()
    }

    /**
     * Return user stored locally
     */
    fun getCurrentUser(): UserDto? {
        val id = sharedPreferences.getString("user_id", null) ?: return null
        val username = sharedPreferences.getString("username", "") ?: ""
        val email = sharedPreferences.getString("email", "") ?: ""
        val role = sharedPreferences.getString("role", "user") ?: "user"
        val image = sharedPreferences.getString("image", null) // ✅ FIX: load image

        return UserDto(
            id = id,
            username = username,
            email = email,
            role = role,
            image = image      // ✅ FIX: return image
        )
    }
}
