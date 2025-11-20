package com.sim.darna.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sim.darna.data.model.Publicite
import com.sim.darna.data.repository.PubliciteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PubliciteDetailViewModel @Inject constructor(
    private val repository: PubliciteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PubliciteDetailUiState>(PubliciteDetailUiState.Loading)
    val uiState: StateFlow<PubliciteDetailUiState> = _uiState.asStateFlow()

    fun loadPublicite(id: String) {
        viewModelScope.launch {
            _uiState.value = PubliciteDetailUiState.Loading
            val result = repository.getPubliciteById(id)
            result.fold(
                onSuccess = { publicite ->
                    _uiState.value = PubliciteDetailUiState.Success(publicite)
                },
                onFailure = { error ->
                    _uiState.value = PubliciteDetailUiState.Error(error.message ?: "Erreur inconnue")
                }
            )
        }
    }

    fun deletePublicite(id: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = repository.deletePublicite(id)
            result.fold(
                onSuccess = { onSuccess() },
                onFailure = { error ->
                    _uiState.value = PubliciteDetailUiState.Error(error.message ?: "Erreur lors de la suppression")
                }
            )
        }
    }
}

sealed class PubliciteDetailUiState {
    object Loading : PubliciteDetailUiState()
    data class Success(val publicite: Publicite) : PubliciteDetailUiState()
    data class Error(val message: String) : PubliciteDetailUiState()
}

