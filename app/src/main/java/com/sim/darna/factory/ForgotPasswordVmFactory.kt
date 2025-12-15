package com.sim.darna.factory

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sim.darna.repository.AuthRepository
import com.sim.darna.viewmodel.ForgotPasswordViewModel
import com.sim.darna.auth.*

@Suppress("UNCHECKED_CAST")
class ForgotPasswordVmFactory(
    private val baseUrl: String,
    private val sharedPreferences: SharedPreferences
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ForgotPasswordViewModel::class.java)) {
            val api = AuthApi.create(baseUrl)
            val repo = AuthRepository(api, sharedPreferences)
            return ForgotPasswordViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
