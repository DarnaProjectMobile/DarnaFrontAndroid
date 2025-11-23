package com.sim.darna.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "session_preferences")

data class UserSession(
    val token: String,
    val userId: String?,
    val username: String,
    val email: String,
    val role: String
)

class SessionManager(private val context: Context) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USERNAME_KEY = stringPreferencesKey("username")
        private val EMAIL_KEY = stringPreferencesKey("email")
        private val ROLE_KEY = stringPreferencesKey("role")
    }

    val sessionFlow: Flow<UserSession?> = context.sessionDataStore.data.map { preferences ->
        val token = preferences[TOKEN_KEY] ?: return@map null
        UserSession(
            token = token,
            userId = preferences[USER_ID_KEY],
            username = preferences[USERNAME_KEY].orEmpty(),
            email = preferences[EMAIL_KEY].orEmpty(),
            role = preferences[ROLE_KEY].orEmpty()
        )
    }

    suspend fun saveSession(response: LoginResponse) {
        context.sessionDataStore.edit { preferences ->
            preferences[TOKEN_KEY] = response.token
            preferences[USER_ID_KEY] = response.user.id.orEmpty()
            preferences[USERNAME_KEY] = response.user.username
            preferences[EMAIL_KEY] = response.user.email
            preferences[ROLE_KEY] = response.user.role
        }
    }

    suspend fun clearSession() {
        context.sessionDataStore.edit { it.clear() }
    }

    suspend fun getToken(): String? {
        return try {
            val preferences = context.sessionDataStore.data.firstOrNull()
            preferences?.get(TOKEN_KEY)
        } catch (e: Exception) {
            null
        }
    }
}

