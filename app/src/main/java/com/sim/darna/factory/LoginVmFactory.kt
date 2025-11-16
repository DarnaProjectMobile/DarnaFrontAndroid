package com.sim.darna.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sim.darna.auth.AuthApi
import com.sim.darna.repository.AuthRepository
import com.sim.darna.ViewModel.LoginViewModel

@Suppress("UNCHECKED_CAST")
class LoginVmFactory(
    private val baseUrl: String,
    private val context: Context? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            val api = AuthApi.create(baseUrl)
            val repo = AuthRepository(api)
            return LoginViewModel(repo, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
