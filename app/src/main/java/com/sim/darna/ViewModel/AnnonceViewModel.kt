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

    fun createAnnonce(title: String, description: String, price: Double) {
        viewModelScope.launch {
            try {
                // Verify token exists before making request
                val hasToken = com.sim.darna.api.RetrofitClient.hasToken()
                android.util.Log.d("AnnonceViewModel", "Creating annonce: $title, Has token: $hasToken")
                if (!hasToken) {
                    _uiState.value = AnnonceUiState(error = "Token d'authentification manquant. Veuillez vous reconnecter.")
                    return@launch
                }
                
                _uiState.value = AnnonceUiState(isLoading = true)
                val request = CreateAnnonceRequest(title, description, price)
                val response = repo.createAnnonce(request)
                android.util.Log.d("AnnonceViewModel", "Annonce created successfully")
                loadAnnonces() // Reload list
                _uiState.value = AnnonceUiState(success = true, message = "Annonce créée avec succès")
            } catch (e: HttpException) {
                val errorMessage = when (e.code()) {
                    401 -> "Non autorisé. Veuillez vous reconnecter."
                    404 -> "Endpoint non trouvé"
                    500 -> "Erreur serveur"
                    else -> "Erreur HTTP ${e.code()}: ${e.message()}"
                }
                android.util.Log.e("AnnonceViewModel", "HTTP Error creating annonce: ${e.code()} - ${e.message()}")
                _uiState.value = AnnonceUiState(error = errorMessage)
            } catch (e: Exception) {
                android.util.Log.e("AnnonceViewModel", "Error creating annonce: ${e.message}", e)
                _uiState.value = AnnonceUiState(error = e.message ?: "Erreur lors de la création")
                e.printStackTrace()
            }
        }
    }

    fun updateAnnonce(id: String, title: String, description: String, price: Double) {
        viewModelScope.launch {
            try {
                // Verify token exists before making request
                val hasToken = com.sim.darna.api.RetrofitClient.hasToken()
                android.util.Log.d("AnnonceViewModel", "Updating annonce with ID: $id, Has token: $hasToken")
                if (!hasToken) {
                    _uiState.value = AnnonceUiState(error = "Token d'authentification manquant. Veuillez vous reconnecter.")
                    return@launch
                }
                
                _uiState.value = AnnonceUiState(isLoading = true)
                val request = UpdateAnnonceRequest(title, description, price)
                val response = repo.updateAnnonce(id, request)
                android.util.Log.d("AnnonceViewModel", "Annonce updated successfully")
                loadAnnonces() // Reload list
                _uiState.value = AnnonceUiState(success = true, message = "Annonce modifiée avec succès")
            } catch (e: HttpException) {
                val errorMessage = when (e.code()) {
                    401 -> "Non autorisé. Veuillez vous reconnecter."
                    404 -> "Annonce non trouvée (ID: $id)"
                    500 -> "Erreur serveur"
                    else -> "Erreur HTTP ${e.code()}: ${e.message()}"
                }
                android.util.Log.e("AnnonceViewModel", "HTTP Error updating annonce: ${e.code()} - ${e.message()}, ID: $id")
                _uiState.value = AnnonceUiState(error = errorMessage)
            } catch (e: Exception) {
                android.util.Log.e("AnnonceViewModel", "Error updating annonce: ${e.message}", e)
                _uiState.value = AnnonceUiState(error = e.message ?: "Erreur lors de la modification")
                e.printStackTrace()
            }
        }
    }

    fun deleteAnnonce(id: String) {
        viewModelScope.launch {
            try {
                // Verify token exists before making request
                val hasToken = com.sim.darna.api.RetrofitClient.hasToken()
                android.util.Log.d("AnnonceViewModel", "Deleting annonce with ID: $id, Has token: $hasToken")
                if (!hasToken) {
                    _uiState.value = AnnonceUiState(error = "Token d'authentification manquant. Veuillez vous reconnecter.")
                    return@launch
                }
                
                _uiState.value = AnnonceUiState(isLoading = true)
                val response = repo.deleteAnnonce(id)
                android.util.Log.d("AnnonceViewModel", "Annonce deleted successfully")
                loadAnnonces() // Reload list
                _uiState.value = AnnonceUiState(success = true, message = "Annonce supprimée avec succès")
            } catch (e: HttpException) {
                val errorMessage = when (e.code()) {
                    401 -> "Non autorisé. Veuillez vous reconnecter."
                    404 -> "Annonce non trouvée (ID: $id)"
                    500 -> "Erreur serveur"
                    else -> "Erreur HTTP ${e.code()}: ${e.message()}"
                }
                android.util.Log.e("AnnonceViewModel", "HTTP Error deleting annonce: ${e.code()} - ${e.message()}, ID: $id")
                _uiState.value = AnnonceUiState(error = errorMessage)
            } catch (e: Exception) {
                android.util.Log.e("AnnonceViewModel", "Error deleting annonce: ${e.message}", e)
                _uiState.value = AnnonceUiState(error = e.message ?: "Erreur lors de la suppression")
                e.printStackTrace()
            }
        }
    }

    fun clearError() {
        _uiState.value = AnnonceUiState()
    }
}