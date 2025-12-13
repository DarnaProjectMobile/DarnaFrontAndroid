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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class VisiteUiState(
    val visites: List<VisiteResponse> = emptyList(),
    val isLoadingList: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val currentVisiteReviews: List<ReviewResponse> = emptyList()
)

class VisiteViewModel(
    private val repository: VisiteRepository,
    private val propertyRepository: com.sim.darna.repository.PropertyRepository? = null
) : ViewModel() {

    private val _state = MutableStateFlow(VisiteUiState())
    val state: StateFlow<VisiteUiState> = _state

    fun loadVisites(force: Boolean = false) {
        if (_state.value.isLoadingList && !force) return
        viewModelScope.launch {
            _state.update { it.copy(isLoadingList = true, error = null) }
            try {
                var visites = repository.getMyVisites()
                
                // Enroll fields if propertyRepository is available
                if (propertyRepository != null) {
                    visites = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        visites.map { visite ->
                            if (visite.logementTitle.isNullOrBlank() && !visite.logementId.isNullOrBlank()) {
                                try {
                                    val response = propertyRepository.getPropertyById(visite.logementId).execute()
                                    if (response.isSuccessful) {
                                        val title = response.body()?.title
                                        if (!title.isNullOrBlank()) {
                                            return@map visite.copy(logementTitle = title)
                                        }
                                    }
                                } catch (e: Exception) {
                                    // Ignore errors to avoid crashing list load
                                }
                            }
                            visite
                        }
                    }
                }

                val finalVisites = visites
                _state.update {
                    it.copy(
                        visites = finalVisites,
                        isLoadingList = false
                    )
                }
            } catch (error: Exception) {
                val errorMessage = resolveError(error)
                _state.update {
                    it.copy(
                        isLoadingList = false,
                        error = errorMessage
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
                var visites = repository.getMyLogementsVisites()
                
                // Enroll fields if propertyRepository is available
                if (propertyRepository != null) {
                    visites = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        visites.map { visite ->
                            if (visite.logementTitle.isNullOrBlank() && !visite.logementId.isNullOrBlank()) {
                                try {
                                    val response = propertyRepository.getPropertyById(visite.logementId).execute()
                                    if (response.isSuccessful) {
                                        val title = response.body()?.title
                                        if (!title.isNullOrBlank()) {
                                            return@map visite.copy(logementTitle = title)
                                        }
                                    }
                                } catch (e: Exception) {
                                    // Ignore
                                }
                            }
                            visite
                        }
                    }
                }

                val finalVisites = visites
                _state.update {
                    it.copy(
                        visites = finalVisites,
                        isLoadingList = false
                    )
                }
            } catch (error: Exception) {
                val errorMessage = resolveError(error)
                _state.update {
                    it.copy(
                        isLoadingList = false,
                        error = errorMessage
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
                val response = repository.createVisite(request)
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        message = "Visite réservée avec succès"
                    )
                }
                kotlinx.coroutines.delay(500)
                loadVisites(force = true)
            } catch (error: Exception) {
                val errorMessage = resolveError(error)
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        error = errorMessage
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
                val response = repository.updateVisite(visiteId, body)
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        message = "✏️ Visite modifiée"
                    )
                }
                loadVisites(force = true)
            } catch (error: Exception) {
                val errorMessage = resolveError(error)
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        error = errorMessage
                    )
                }
            }
        }
    }

    fun cancelVisite(visiteId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, error = null, message = null) }
            try {
                val currentVisite = _state.value.visites.find { it.id == visiteId }
                val response = repository.cancelVisite(visiteId)
                
                val logementTitle = response.logementTitle?.takeIf { it.isNotBlank() } 
                    ?: currentVisite?.logementTitle?.takeIf { it.isNotBlank() }
                    ?: "le logement"
                _state.update { it.copy(isSubmitting = false, message = "✅ Visite annulée pour $logementTitle") }
                loadVisites(force = true)
            } catch (error: Exception) {
                if (error is HttpException && error.code() == 403) {
                    attemptDeleteFallback(visiteId, "Visite annulée")
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

    fun acceptVisite(visiteId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, error = null, message = null) }
            try {
                val response = repository.acceptVisite(visiteId)
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        message = "✅ Visite acceptée"
                    )
                }
                loadLogementsVisites(force = true)
            } catch (error: Exception) {
                val errorMessage = resolveError(error)
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        error = errorMessage
                    )
                }
            }
        }
    }

    fun rejectVisite(visiteId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, error = null, message = null) }
            try {
                val response = repository.rejectVisite(visiteId)
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        message = "✅ Visite refusée"
                    )
                }
                loadLogementsVisites(force = true)
            } catch (error: Exception) {
                val errorMessage = resolveError(error)
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        error = errorMessage
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
                _state.update { it.copy(isSubmitting = false, message = "🚫 Visite annulée") }
                loadVisites(force = true)
            } catch (error: Exception) {
                val errorMessage = resolveError(error)
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        error = errorMessage
                    )
                }
            }
        }
    }

    fun validateVisite(visiteId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, error = null, message = null) }
            try {
                val response = repository.validateVisite(visiteId)
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        message = "✅ Visite effectuée"
                    )
                }
                kotlinx.coroutines.delay(500)
                loadVisites(force = true)
            } catch (error: Exception) {
                val errorMessage = resolveError(error)
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        error = errorMessage
                    )
                }
            }
        }
    }

    fun submitReview(
        visiteId: String,
        collectorRating: Int,
        cleanlinessRating: Int,
        locationRating: Int,
        conformityRating: Int,
        comment: String?
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, error = null, message = null) }
            
            if (visiteId.isBlank()) {
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        error = "Erreur: L'identifiant de la visite est invalide."
                    )
                }
                return@launch
            }
            
            val validCollectorRating = collectorRating.coerceIn(1, 5)
            val validCleanlinessRating = cleanlinessRating.coerceIn(1, 5)
            val validLocationRating = locationRating.coerceIn(1, 5)
            val validConformityRating = conformityRating.coerceIn(1, 5)
            
            val request = CreateReviewRequest(
                visiteId = visiteId,
                collectorRating = validCollectorRating,
                cleanlinessRating = validCleanlinessRating,
                locationRating = validLocationRating,
                conformityRating = validConformityRating,
                comment = comment?.takeIf { it.isNotBlank() }
            )
            try {
                val reviewResponse = repository.createReview(visiteId, request)
                
                if (reviewResponse.id != null) {
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            message = "⭐ Évaluation faite"
                        )
                    }
                    kotlinx.coroutines.delay(800)
                    loadVisites(force = true)
                } else {
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            error = "Erreur: L'évaluation n'a pas été enregistrée correctement."
                        )
                    }
                }
            } catch (error: Exception) {
                val errorMessage = when {
                    error is HttpException && error.code() == 404 -> {
                        viewModelScope.launch {
                            kotlinx.coroutines.delay(500)
                            loadVisites(force = true)
                        }
                        "Cette visite n'existe plus. Actualisation de la liste..."
                    }
                    error is HttpException && error.code() == 400 -> {
                        val errorBody = try {
                            error.response()?.errorBody()?.string() ?: ""
                        } catch (e: Exception) {
                            ""
                        }
                        when {
                            errorBody.contains("validée", ignoreCase = true) || 
                            errorBody.contains("validate", ignoreCase = true) -> 
                                "Vous devez d'abord valider la visite (cliquez sur 'Visite effectuée') avant de l'évaluer."
                            errorBody.contains("déjà évalué", ignoreCase = true) || 
                            errorBody.contains("already", ignoreCase = true) || 
                            errorBody.contains("review", ignoreCase = true) -> 
                                "Cette visite a déjà été évaluée."
                            errorBody.contains("completed", ignoreCase = true) ->
                                "La visite doit être terminée (status: completed) avant d'être évaluée."
                            errorBody.contains("visiteId", ignoreCase = true) ->
                                "Erreur: L'ID de la visite est manquant ou invalide."
                            else -> {
                                val msg = if (errorBody.isNotBlank()) {
                                    "Erreur: ${errorBody.take(150)}"
                                } else {
                                    "Erreur lors de l'évaluation. Vérifiez que la visite est validée."
                                }
                                msg
                            }
                        }
                    }
                    error is HttpException && error.code() == 500 -> {
                        "Erreur serveur. Veuillez réessayer plus tard."
                    }
                    else -> {
                        val msg = error.localizedMessage ?: error.message ?: "Erreur inconnue"
                        "Erreur: $msg"
                    }
                }
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        error = errorMessage
                    )
                }
            }
        }
    }

    fun loadVisiteReviews(visiteId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingList = true, error = null) }
            try {
                val reviews = repository.getVisiteReviews(visiteId)
                _state.update {
                    it.copy(
                        isLoadingList = false,
                        currentVisiteReviews = reviews
                    )
                }
            } catch (error: Exception) {
                val errorMessage = resolveError(error)
                _state.update {
                    it.copy(
                        isLoadingList = false,
                        error = errorMessage
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
                val response = repository.updateStatus(visiteId, status)
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

    suspend fun getVisiteReviews(visiteId: String): List<ReviewResponse> {
        return try {
            repository.getVisiteReviews(visiteId)
        } catch (error: Exception) {
            emptyList()
        }
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
                    400 -> {
                        val message = errorBody?.takeIf { it.isNotBlank() } 
                            ?: "Requête invalide. Veuillez vérifier les données saisies."
                        when {
                            message.contains("validée", ignoreCase = true) -> 
                                "Vous devez d'abord valider la visite (cliquez sur 'Visite effectuée') avant de l'évaluer."
                            message.contains("déjà évalué", ignoreCase = true) -> 
                                "Cette visite a déjà été évaluée."
                            message.contains("visiteId", ignoreCase = true) -> 
                                "Erreur: La visite n'est pas valide. Veuillez réessayer."
                            else -> {
                                val shortMessage = extractShortMessage(message)
                                "Erreur 400: $shortMessage"
                            }
                        }
                    }
                    401 -> "Session expirée - veuillez vous reconnecter"
                    403 -> null // Ne jamais afficher les erreurs 403
                    404 -> "Cette visite n'existe plus"
                    500 -> {
                        val message = errorBody?.takeIf { it.isNotBlank() }
                            ?: "Erreur serveur interne"
                        val shortMessage = extractShortMessage(message)
                        "Erreur serveur: $shortMessage. Veuillez réessayer plus tard."
                    }
                    else -> {
                        val message = errorBody?.takeIf { it.isNotBlank() }
                            ?: "Erreur serveur (${error.code()})"
                        val shortMessage = extractShortMessage(message)
                        "Erreur ${error.code()}: $shortMessage"
                    }
                }
            }
            is IOException -> "Connexion réseau indisponible"
            else -> error.localizedMessage ?: "Une erreur inattendue est survenue"
        }
    }

    private fun extractShortMessage(errorBody: String): String {
        if (errorBody.length <= 100) return errorBody
        
        try {
            val messageMatch = Regex(""""message"\s*:\s*"([^"]+)"""").find(errorBody)
            if (messageMatch != null) {
                val extracted = messageMatch.groupValues[1]
                return if (extracted.length <= 100) extracted else extracted.take(97) + "..."
            }
            
            val errorMatch = Regex(""""error"\s*:\s*"([^"]+)"""").find(errorBody)
            if (errorMatch != null) {
                val extracted = errorMatch.groupValues[1]
                return if (extracted.length <= 100) extracted else extracted.take(97) + "..."
            }
        } catch (e: Exception) {
            // Si le parsing échoue, continuer avec la méthode par défaut
        }
        
        return errorBody.take(100) + if (errorBody.length > 100) "..." else ""
    }

    private fun buildIsoDateTime(dateMillis: Long, hour: Int, minute: Int): String {
        val localCalendar = Calendar.getInstance().apply {
            timeInMillis = dateMillis
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = localCalendar.timeInMillis
        }
        
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return formatter.format(utcCalendar.time)
    }
}
