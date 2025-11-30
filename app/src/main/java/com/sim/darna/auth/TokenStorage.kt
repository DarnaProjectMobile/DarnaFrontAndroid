package com.sim.darna.auth

import android.content.Context
import android.content.SharedPreferences

object TokenStorage {

    private const val PREFS_NAME = "auth_prefs"
    private const val TOKEN_KEY = "jwt_token"
    private const val USER_ID_KEY = "user_id"

    // -----------------------------
    // 🔥 Save token + user ID
    // -----------------------------
    fun saveAuthData(context: Context, token: String, userId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(TOKEN_KEY, token)
            .putString(USER_ID_KEY, userId)
            .apply()
    }

    // -----------------------------
    // 🔥 Save token only (fallback)
    // -----------------------------
    fun saveToken(context: Context, token: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(TOKEN_KEY, token)
            .apply()
    }

    // -----------------------------
    // 🔥 Save user ID only (fallback)
    // -----------------------------
    fun saveUserId(context: Context, userId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(USER_ID_KEY, userId)
            .apply()
    }

    // -----------------------------
    // 🔥 Get token
    // -----------------------------
    fun getToken(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(TOKEN_KEY, null)
    }

    // -----------------------------
    // 🔥 Get user ID (needed for POST review)
    // -----------------------------
    fun getUserId(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(USER_ID_KEY, null)
    }

    // -----------------------------
    // 🔥 Clear all auth data
    // -----------------------------
    fun clearAuth(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .remove(TOKEN_KEY)
            .remove(USER_ID_KEY)
            .apply()
    }
}
