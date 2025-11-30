package com.sim.darna.logement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class LogementUiState(
    val logements: List<LogementResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class LogementViewModel(private val repository: LogementRepository) : ViewModel() {

    private val _state = MutableStateFlow(LogementUiState())
    val state: StateFlow<LogementUiState> = _state

    fun loadLogements(force: Boolean = false) {
        if (_state.value.isLoading && !force) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val logements = repository.getAllLogements()
                _state.update {
                    it.copy(
                        logements = logements,
                        isLoading = false
                    )
                }
            } catch (error: Exception) {
                val errorMessage = resolveError(error)
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = errorMessage // null si erreur 403, sera ignoré
                    )
                }
            }
        }
    }

    fun loadMyLogements(force: Boolean = false) {
        if (_state.value.isLoading && !force) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val logements = repository.getMyLogements()
                _state.update {
                    it.copy(
                        logements = logements,
                        isLoading = false
                    )
                }
            } catch (error: Exception) {
                val errorMessage = resolveError(error)
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = errorMessage
                    )
                }
            }
        }
    }
    
    fun getLogementById(id: String, onSuccess: (LogementResponse) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val logement = repository.getLogementById(id)
                _state.update { it.copy(isLoading = false) }
                onSuccess(logement)
            } catch (error: Exception) {
                val errorMessage = resolveError(error) ?: "Erreur lors du chargement"
                _state.update { it.copy(isLoading = false, error = errorMessage) }
                onError(errorMessage)
            }
        }
    }
    
    fun createLogement(request: CreateLogementRequest, onSuccess: (LogementResponse) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val logement = repository.createLogement(request)
                _state.update { it.copy(isLoading = false) }
                // Recharger la liste après création
                loadLogements(force = true)
                onSuccess(logement)
            } catch (error: Exception) {
                val errorMessage = resolveError(error) ?: "Erreur lors de la création"
                _state.update { it.copy(isLoading = false, error = errorMessage) }
                onError(errorMessage)
            }
        }
    }
    
    fun updateLogement(id: String, request: UpdateLogementRequest, onSuccess: (LogementResponse) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val logement = repository.updateLogement(id, request)
                _state.update { it.copy(isLoading = false) }
                // Recharger la liste après mise à jour
                loadLogements(force = true)
                onSuccess(logement)
            } catch (error: Exception) {
                val errorMessage = resolveError(error) ?: "Erreur lors de la mise à jour"
                _state.update { it.copy(isLoading = false, error = errorMessage) }
                onError(errorMessage)
            }
        }
    }
    
    fun deleteLogement(id: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                repository.deleteLogement(id)
                _state.update { it.copy(isLoading = false) }
                // Recharger la liste après suppression
                loadLogements(force = true)
                onSuccess()
            } catch (error: Exception) {
                val errorMessage = resolveError(error) ?: "Erreur lors de la suppression"
                _state.update { it.copy(isLoading = false, error = errorMessage) }
                onError(errorMessage)
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    private fun resolveError(error: Exception): String? {
        return when (error) {
            is HttpException -> {
                val errorBody = try {
                    error.response()?.errorBody()?.string()
                } catch (e: Exception) {
                    null
                }
                when (error.code()) {
                    400 -> "Requête invalide. Veuillez vérifier les données."
                    401 -> "Session expirée - veuillez vous reconnecter"
                    403 -> null // Ne jamais afficher les erreurs 403
                    404 -> "Aucun logement trouvé"
                    500 -> {
                        val message = errorBody?.takeIf { it.isNotBlank() }
                            ?: "Erreur serveur interne"
                        "Erreur serveur: $message"
                    }
                    else -> {
                        val message = errorBody?.takeIf { it.isNotBlank() }
                            ?: "Erreur serveur (${error.code()})"
                        "Erreur ${error.code()}: $message"
                    }
                }
            }
            is IOException -> "Connexion réseau indisponible"
            else -> error.localizedMessage ?: "Une erreur inattendue est survenue"
        }
    }
}





