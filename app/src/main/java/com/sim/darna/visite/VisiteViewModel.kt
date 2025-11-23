package com.sim.darna.visite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class VisiteUiState(
    val visites: List<VisiteResponse> = emptyList(),
    val isLoadingList: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val message: String? = null
)

class VisiteViewModel(private val repository: VisiteRepository) : ViewModel() {

    private val _state = MutableStateFlow(VisiteUiState())
    val state: StateFlow<VisiteUiState> = _state

    fun loadVisites(force: Boolean = false) {
        if (_state.value.isLoadingList && !force) return
        viewModelScope.launch {
            _state.update { it.copy(isLoadingList = true, error = null) }
            try {
                val visites = repository.getMyVisites()
                _state.update {
                    it.copy(
                        visites = visites,
                        isLoadingList = false
                    )
                }
            } catch (error: Exception) {
                _state.update {
                    it.copy(
                        isLoadingList = false,
                        error = resolveError(error)
                    )
                }
            }
        }
    }

    fun loadLogementsVisites(force: Boolean = false) {
        if (_state.value.isLoadingList && !force) return
        viewModelScope.launch {
            _state.update { it.copy(isLoadingList = true, error = null) }
            try {
                val visites = repository.getMyLogementsVisites()
                _state.update {
                    it.copy(
                        visites = visites,
                        isLoadingList = false
                    )
                }
            } catch (error: Exception) {
                _state.update {
                    it.copy(
                        isLoadingList = false,
                        error = resolveError(error)
                    )
                }
            }
        }
    }

    fun createVisite(
        logementId: String,
        dateMillis: Long,
        hour: Int,
        minute: Int,
        notes: String?,
        contactPhone: String?
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, error = null, message = null) }
            val request = CreateVisiteRequest(
                logementId = logementId,
                dateVisite = buildIsoDateTime(dateMillis, hour, minute),
                notes = notes?.takeIf { it.isNotBlank() },
                contactPhone = contactPhone?.takeIf { it.isNotBlank() }
            )
            try {
                repository.createVisite(request)
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        message = "Visite réservée avec succès"
                    )
                }
                loadVisites(force = true)
            } catch (error: Exception) {
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        error = resolveError(error)
                    )
                }
            }
        }
    }

    fun updateVisite(
        visiteId: String,
        dateMillis: Long?,
        hour: Int?,
        minute: Int?,
        notes: String?,
        contactPhone: String?
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, error = null, message = null) }
            val body = UpdateVisiteRequest(
                dateVisite = if (dateMillis != null && hour != null && minute != null) {
                    buildIsoDateTime(dateMillis, hour, minute)
                } else {
                    null
                },
                notes = notes?.takeIf { it.isNotBlank() },
                contactPhone = contactPhone?.takeIf { it.isNotBlank() }
            )
            try {
                repository.updateVisite(visiteId, body)
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        message = "Visite mise à jour"
                    )
                }
                loadVisites(force = true)
            } catch (error: Exception) {
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        error = resolveError(error)
                    )
                }
            }
        }
    }

    fun cancelVisite(visiteId: String) {
        changeStatus(
            visiteId = visiteId,
            status = "cancelled",
            successMessage = "Visite annulée",
            fallbackDeleteOnForbidden = true
        )
    }

    fun acceptVisite(visiteId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, error = null, message = null) }
            try {
                repository.acceptVisite(visiteId)
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        message = "Visite acceptée avec succès"
                    )
                }
                loadLogementsVisites(force = true)
            } catch (error: Exception) {
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        error = resolveError(error)
                    )
                }
            }
        }
    }

    fun rejectVisite(visiteId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, error = null, message = null) }
            try {
                repository.rejectVisite(visiteId)
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        message = "Visite refusée"
                    )
                }
                loadLogementsVisites(force = true)
            } catch (error: Exception) {
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        error = resolveError(error)
                    )
                }
            }
        }
    }

    fun deleteVisite(visiteId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, error = null, message = null) }
            try {
                repository.deleteVisite(visiteId)
                _state.update { it.copy(isSubmitting = false, message = "Visite supprimée") }
                loadVisites(force = true)
            } catch (error: Exception) {
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        error = resolveError(error)
                    )
                }
            }
        }
    }

    fun clearFeedback() {
        _state.update { it.copy(message = null, error = null) }
    }

    private fun changeStatus(
        visiteId: String,
        status: String,
        successMessage: String,
        fallbackDeleteOnForbidden: Boolean = false
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, error = null, message = null) }
            try {
                repository.updateStatus(visiteId, status)
                _state.update { it.copy(isSubmitting = false, message = successMessage) }
                loadVisites(force = true)
            } catch (error: Exception) {
                if (error is HttpException && error.code() == 403 && fallbackDeleteOnForbidden) {
                    attemptDeleteFallback(visiteId, successMessage)
                    return@launch
                }
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        error = resolveError(error)
                    )
                }
            }
        }
    }

    private suspend fun attemptDeleteFallback(visiteId: String, successMessage: String) {
        try {
            repository.deleteVisite(visiteId)
            _state.update { it.copy(isSubmitting = false, message = successMessage) }
            loadVisites(force = true)
        } catch (deleteError: Exception) {
            _state.update {
                it.copy(
                    isSubmitting = false,
                    error = resolveError(deleteError)
                )
            }
        }
    }

    private fun resolveError(error: Exception): String {
        return when (error) {
            is HttpException -> {
                when (error.code()) {
                    401 -> "Session expirée - veuillez vous reconnecter"
                    403 -> "Accès refusé à cette action"
                    404 -> "Cette visite n'existe plus"
                    else -> "Erreur serveur (${error.code()})"
                }
            }
            is IOException -> "Connexion réseau indisponible"
            else -> error.localizedMessage ?: "Une erreur inattendue est survenue"
        }
    }

    private fun buildIsoDateTime(dateMillis: Long, hour: Int, minute: Int): String {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = dateMillis
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return formatter.format(calendar.time)
    }
}
