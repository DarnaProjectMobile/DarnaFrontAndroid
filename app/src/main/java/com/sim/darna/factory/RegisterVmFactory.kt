package com.sim.darna.factory

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sim.darna.auth.AuthApi
import com.sim.darna.repository.AuthRepository
import com.sim.darna.viewmodel.RegisterViewModel

@Suppress("UNCHECKED_CAST")
class RegisterVmFactory(
    private val baseUrl: String,
    private val sharedPreferences: SharedPreferences,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val api = AuthApi.create(baseUrl)
        val repo = AuthRepository(api, sharedPreferences)
        // Create auth_prefs SharedPreferences for token storage
        val authPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return RegisterViewModel(repo, authPrefs) as T
    }
}
