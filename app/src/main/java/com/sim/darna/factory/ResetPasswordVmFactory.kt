package com.sim.darna.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sim.darna.auth.AuthApi
import com.sim.darna.Repository.AuthRepository
import com.sim.darna.ViewModel.ResetPasswordViewModel

@Suppress("UNCHECKED_CAST")
class ResetPasswordVmFactory(private val baseUrl: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ResetPasswordViewModel::class.java)) {
            val api = AuthApi.create(baseUrl)
            val repo = AuthRepository(api)
            return ResetPasswordViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
