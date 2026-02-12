package com.sim.darna.utils

import android.content.Context
import android.content.SharedPreferences

object FingerprintManager {
    private const val PREFS_NAME = "APP_PREFS"
    private const val KEY_FINGERPRINT_ENABLED = "fingerprint_enabled"
    private const val KEY_FINGERPRINT_REGISTERED = "fingerprint_registered"

    /**
     * Check if fingerprint authentication is enabled
     */
    fun isFingerprintEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_FINGERPRINT_ENABLED, false)
    }

    /**
     * Enable or disable fingerprint authentication
     */
    fun setFingerprintEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_FINGERPRINT_ENABLED, enabled).apply()
    }

    /**
     * Check if fingerprint is registered
     */
    fun isFingerprintRegistered(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_FINGERPRINT_REGISTERED, false)
    }

    /**
     * Mark fingerprint as registered
     */
    fun setFingerprintRegistered(context: Context, registered: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_FINGERPRINT_REGISTERED, registered).apply()
        
        // If registering, also enable fingerprint
        if (registered) {
            setFingerprintEnabled(context, true)
        }
    }

    /**
     * Get saved credentials for fingerprint login
     */
    fun getSavedCredentials(context: Context): Pair<String?, String?> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // First try fingerprint-specific credentials, then fallback to remember me credentials
        val email = prefs.getString("fingerprint_email", null) 
            ?: prefs.getString("saved_email", null)
        val password = prefs.getString("fingerprint_password", null) 
            ?: prefs.getString("saved_password", null)
        return Pair(email, password)
    }

    /**
     * Save credentials specifically for fingerprint authentication
     */
    fun saveFingerprintCredentials(context: Context, email: String, password: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString("fingerprint_email", email)
            .putString("fingerprint_password", password)
            .apply()
    }

    /**
     * Clear fingerprint credentials
     */
    fun clearFingerprintCredentials(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .remove("fingerprint_email")
            .remove("fingerprint_password")
            .apply()
    }
}

