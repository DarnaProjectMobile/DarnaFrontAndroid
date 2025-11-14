package com.sim.darna.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sim.darna.model.Annonce
import com.sim.darna.repository.AnnonceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AnnonceViewModel : ViewModel() {

    private val repo = AnnonceRepository()

    private val _annonces = MutableStateFlow<List<Annonce>>(emptyList())
    val annonces: StateFlow<List<Annonce>> = _annonces

    fun loadAnnonces() {
        viewModelScope.launch {
            try {
                _annonces.value = repo.getAnnonces()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}