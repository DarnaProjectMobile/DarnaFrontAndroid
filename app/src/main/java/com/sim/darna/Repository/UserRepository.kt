package com.sim.darna.repository

import android.content.SharedPreferences
import com.sim.darna.auth.UpdateUserRequest
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
     * PATCH /users/me - Sends JSON body to match NestJS UpdateUserDto
     */
    fun updateUser(
        username: String,
        email: String,
        password: String? = null,
        numTel: String? = null,
        dateDeNaissance: String? = null,
        gender: String? = null,
        imageFile: File? = null
    ): Call<UserDto> {
        val request = UpdateUserRequest(
            username = username,
            email = email,
            bio = "User bio",  // Required by your UpdateUserDto
            password = password ?: "KeepCurrentPassword123!",  // Use provided password or dummy
            numTel = numTel,
            dateDeNaissance = dateDeNaissance,
            gender = gender
        )
        
        return api.updateMe(request)
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
            .putString("image", user.image)
            .putString("numTel", user.numTel)
            .putString("dateDeNaissance", user.dateDeNaissance)
            .putString("gender", user.gender)
            .putString("createdAt", user.createdAt)
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
