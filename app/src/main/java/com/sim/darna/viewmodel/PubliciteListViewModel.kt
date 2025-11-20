package com.sim.darna.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sim.darna.data.model.Categorie
import com.sim.darna.data.model.Publicite
import com.sim.darna.data.repository.PubliciteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PubliciteListViewModel @Inject constructor(
    private val repository: PubliciteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PubliciteListUiState>(PubliciteListUiState.Loading)
    val uiState: StateFlow<PubliciteListUiState> = _uiState.asStateFlow()

    private val _publicites = MutableStateFlow<List<Publicite>>(emptyList())
    val publicites: StateFlow<List<Publicite>> = _publicites.asStateFlow()

    private val _selectedCategorie = MutableStateFlow<Categorie?>(null)
    val selectedCategorie: StateFlow<Categorie?> = _selectedCategorie.asStateFlow()

    private val _searchQuery = MutableStateFlow<String>("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadPublicites()
    }

    fun loadPublicites() {
        viewModelScope.launch {
            _uiState.value = PubliciteListUiState.Loading
            val result = repository.getAllPublicites(
                categorie = _selectedCategorie.value,
                searchQuery = _searchQuery.value.takeIf { it.isNotBlank() }
            )
            result.fold(
                onSuccess = { publicites ->
                    _publicites.value = publicites
                    _uiState.value = PubliciteListUiState.Success(publicites)
                },
                onFailure = { error ->
                    _uiState.value = PubliciteListUiState.Error(error.message ?: "Erreur inconnue")
                }
            )
        }
    }

    fun filterByCategorie(categorie: Categorie?) {
        _selectedCategorie.value = categorie
        loadPublicites()
    }

    fun search(query: String) {
        _searchQuery.value = query
        loadPublicites()
    }

    fun refresh() {
        loadPublicites()
    }
    
    fun deletePublicite(id: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = repository.deletePublicite(id)
            result.fold(
                onSuccess = {
                    // Retirer la publicité de la liste
                    _publicites.value = _publicites.value.filter { it.id != id }
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.value = PubliciteListUiState.Error(error.message ?: "Erreur lors de la suppression")
                }
            )
        }
    }
}

sealed class PubliciteListUiState {
    object Loading : PubliciteListUiState()
    data class Success(val publicites: List<Publicite>) : PubliciteListUiState()
    data class Error(val message: String) : PubliciteListUiState()
}

