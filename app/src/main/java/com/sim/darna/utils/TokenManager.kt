package com.sim.darna.utils

import android.content.Context
import android.content.SharedPreferences

class TokenManager private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    companion object {
        @Volatile private var instance: TokenManager? = null

        fun getInstance(context: Context): TokenManager {
            return instance ?: synchronized(this) {
                instance ?: TokenManager(context.applicationContext).also { instance = it }
            }
        }
    }

    fun saveToken(token: String) {
        prefs.edit().putString("jwt_token", token).commit() // commit = sync
    }

    fun getToken(): String? = prefs.getString("jwt_token", null)

    fun hasToken(): Boolean = getToken() != null

    fun clear() {
        prefs.edit().clear().commit()
    }
}
