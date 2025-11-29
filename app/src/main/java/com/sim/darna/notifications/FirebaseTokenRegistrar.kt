package com.sim.darna.notifications

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.sim.darna.auth.DeviceTokenRequest
import com.sim.darna.auth.MessageResponse
import com.sim.darna.auth.RetrofitClient
import com.sim.darna.auth.TokenStorage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Handles registering/unregistering the current device token with the backend.
 */
object FirebaseTokenRegistrar {

    private const val PREFS_NAME = "APP_PREFS"
    private const val KEY_LAST_TOKEN = "last_fcm_token"
    private const val TAG = "FirebaseTokenRegistrar"

    /**
     * Pull the latest token from FCM and try to register it server-side.
     */
    fun syncCurrentToken(context: Context) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Failed to fetch FCM token", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result ?: return@addOnCompleteListener
            registerToken(context, token)
        }
    }

    /**
     * Register the provided token with the backend API (if user authenticated).
     */
    fun registerToken(context: Context, token: String) {
        cacheToken(context, token)

        if (TokenStorage.getToken(context).isNullOrEmpty()) {
            Log.d(TAG, "JWT missing, postponing device token sync")
            return
        }

        RetrofitClient.userApi(context)
            .registerDeviceToken(DeviceTokenRequest(token))
            .enqueue(object : Callback<MessageResponse> {
                override fun onResponse(
                    call: Call<MessageResponse>,
                    response: Response<MessageResponse>,
                ) {
                    if (response.isSuccessful) {
                        Log.d(TAG, "Device token registered")
                    } else {
                        Log.w(TAG, "Device token register failed: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                    Log.e(TAG, "Device token register error", t)
                }
            })
    }

    /**
     * Remove the currently cached FCM token from the backend.
     */
    fun unregisterCurrentToken(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val token = prefs.getString(KEY_LAST_TOKEN, null)

        if (token.isNullOrBlank()) {
            return
        }

        if (TokenStorage.getToken(context).isNullOrEmpty()) {
            prefs.edit().remove(KEY_LAST_TOKEN).apply()
            return
        }

        RetrofitClient.userApi(context)
            .removeDeviceToken(DeviceTokenRequest(token))
            .enqueue(object : Callback<MessageResponse> {
                override fun onResponse(
                    call: Call<MessageResponse>,
                    response: Response<MessageResponse>,
                ) {
                    prefs.edit().remove(KEY_LAST_TOKEN).apply()
                    if (response.isSuccessful) {
                        Log.d(TAG, "Device token removed")
                    } else {
                        Log.w(TAG, "Device token remove failed: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                    Log.e(TAG, "Device token remove error", t)
                }
            })
    }

    private fun cacheToken(context: Context, token: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LAST_TOKEN, token)
            .apply()
    }
}

