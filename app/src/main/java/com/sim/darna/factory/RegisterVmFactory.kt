package com.sim.darna.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sim.darna.auth.AuthApi
import com.sim.darna.auth.AuthRepository
import com.sim.darna.auth.RegisterViewModel

class RegisterVmFactory(private val baseUrl: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val api = AuthApi.create(baseUrl)
        val repo = AuthRepository(api)
        return RegisterViewModel(repo) as T
    }
}
