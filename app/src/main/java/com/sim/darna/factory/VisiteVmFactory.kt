package com.sim.darna.factory

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sim.darna.visite.VisiteApi
import com.sim.darna.visite.VisiteRepository
import com.sim.darna.visite.VisiteViewModel

@Suppress("UNCHECKED_CAST")
class VisiteVmFactory(
    private val baseUrl: String,
    private val context: android.content.Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VisiteViewModel::class.java)) {
            val api = VisiteApi.create(baseUrl, context)
            val repo = VisiteRepository(api)
            val propertyRepo = com.sim.darna.repository.PropertyRepository(context)
            return VisiteViewModel(repo, propertyRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
