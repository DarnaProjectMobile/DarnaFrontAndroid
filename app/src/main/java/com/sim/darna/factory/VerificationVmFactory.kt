package com.sim.darna.factory

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sim.darna.auth.AuthApi
import com.sim.darna.repository.AuthRepository
import com.sim.darna.viewmodel.VerificationViewModel

class VerificationVmFactory(
    private val baseUrl: String,
    private val sharedPreferences: SharedPreferences,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VerificationViewModel::class.java)) {
            val api = AuthApi.create(baseUrl)
            val repository = AuthRepository(api, sharedPreferences)
            @Suppress("UNCHECKED_CAST")
            return VerificationViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
