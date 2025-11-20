package com.sim.darna.utils

import android.content.Context
import com.sim.darna.auth.UserDto

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveAuth(token: String?, user: UserDto) {
        prefs.edit().apply {
            if (!token.isNullOrBlank()) {
                putString(KEY_TOKEN, token)
            }
            putString(KEY_USER_ID, user.id)
            putString(KEY_USER_NAME, user.username)
            putString(KEY_USER_EMAIL, user.email)
            putString(KEY_USER_ROLE, user.role)
        }.apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun getUser(): UserDto? {
        val id = prefs.getString(KEY_USER_ID, null)
        val name = prefs.getString(KEY_USER_NAME, null) ?: return null
        val email = prefs.getString(KEY_USER_EMAIL, null) ?: return null
        val role = prefs.getString(KEY_USER_ROLE, null) ?: return null

        // Log pour vérifier le rôle
        android.util.Log.d("ROLE", "current role = $role")
        return UserDto(
            id = id,
            username = name,
            email = email,
            role = role
        )
    }

    fun isLoggedIn(): Boolean = getToken() != null

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "darna_prefs"
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_ROLE = "user_role"
    }
}

