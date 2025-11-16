package com.sim.darna.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sim.darna.model.Annonce
import com.sim.darna.model.CreateAnnonceRequest
import com.sim.darna.model.UpdateAnnonceRequest
import com.sim.darna.repository.AnnonceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class AnnonceUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val message: String? = null
)

class AnnonceViewModel : ViewModel() {

    private val repo = AnnonceRepository()

    private val _annonces = MutableStateFlow<List<Annonce>>(emptyList())
    val annonces: StateFlow<List<Annonce>> = _annonces

    private val _selectedAnnonce = MutableStateFlow<Annonce?>(null)
    val selectedAnnonce: StateFlow<Annonce?> = _selectedAnnonce

    private val _uiState = MutableStateFlow(AnnonceUiState())
    val uiState: StateFlow<AnnonceUiState> = _uiState

    fun loadAnnonces() {
        viewModelScope.launch {
            try {
                _uiState.value = AnnonceUiState(isLoading = true)
                _annonces.value = repo.getAnnonces()
                _uiState.value = AnnonceUiState(success = true)
            } catch (e: Exception) {
                _uiState.value = AnnonceUiState(error = e.message ?: "Erreur lors du chargement")
                e.printStackTrace()
            }
        }
    }

    fun getAnnonceById(id: String) {
        viewModelScope.launch {
            try {
                _uiState.value = AnnonceUiState(isLoading = true)
                _selectedAnnonce.value = repo.getAnnonceById(id)
                _uiState.value = AnnonceUiState(success = true)
            } catch (e: Exception) {
                _uiState.value = AnnonceUiState(error = e.message ?: "Erreur lors du chargement")
                e.printStackTrace()
            }
        }
    }

    fun createAnnonce(
        title: String,
        description: String,
        price: Double,
        image: String,
        type: String,
        location: String,
        startDate: String,
        endDate: String
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = AnnonceUiState(isLoading = true)
                val request = CreateAnnonceRequest(title, description, price, image, type, location, startDate, endDate)
                repo.createAnnonce(request)
                loadAnnonces()
                _uiState.value = AnnonceUiState(success = true, message = "Annonce créée avec succès")
            } catch (e: Exception) {
                _uiState.value = AnnonceUiState(error = e.message ?: "Erreur lors de la création")
                e.printStackTrace()
            }
        }
    }

    fun updateAnnonce(
        id: String,
        title: String,
        description: String,
        price: Double,
        image: String,
        type: String,
        location: String,
        startDate: String,
        endDate: String
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = AnnonceUiState(isLoading = true)
                val request = UpdateAnnonceRequest(title, description, price, image, type, location, startDate, endDate)
                repo.updateAnnonce(id, request)
                loadAnnonces()
                _uiState.value = AnnonceUiState(success = true, message = "Annonce modifiée avec succès")
            } catch (e: Exception) {
                _uiState.value = AnnonceUiState(error = e.message ?: "Erreur lors de la modification")
                e.printStackTrace()
            }
        }
    }

    fun deleteAnnonce(id: String) {
        viewModelScope.launch {
            try {
                _uiState.value = AnnonceUiState(isLoading = true)
                repo.deleteAnnonce(id)
                loadAnnonces()
                _uiState.value = AnnonceUiState(success = true, message = "Annonce supprimée avec succès")
            } catch (e: Exception) {
                _uiState.value = AnnonceUiState(error = e.message ?: "Erreur lors de la suppression")
                e.printStackTrace()
            }
        }
    }

    fun clearError() {
        _uiState.value = AnnonceUiState()
    }
}
