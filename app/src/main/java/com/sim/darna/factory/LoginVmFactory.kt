package com.sim.darna.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sim.darna.auth.AuthApi
import com.sim.darna.auth.AuthRepository
import com.sim.darna.auth.LoginViewModel
import com.sim.darna.utils.SessionManager

@Suppress("UNCHECKED_CAST")
class LoginVmFactory(
    private val baseUrl: String,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            val api = AuthApi.create(baseUrl)
            val repo = AuthRepository(api)
            return LoginViewModel(repo, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
