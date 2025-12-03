package com.sim.darna.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sim.darna.auth.AuthApi
import com.sim.darna.auth.AuthRepository
import com.sim.darna.auth.ForgotPasswordViewModel

@Suppress("UNCHECKED_CAST")
class ForgotPasswordVmFactory(
    private val baseUrl: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ForgotPasswordViewModel::class.java)) {
            val api = AuthApi.create(baseUrl)
            val repo = AuthRepository(api)
            return ForgotPasswordViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}



