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
    private val sessionManager: com.sim.darna.auth.SessionManager? = null,
    private val logementRepository: com.sim.darna.logement.LogementRepository? = null
) : ViewModel() {

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
                val errorMessage = resolveError(error)
                _state.update {
                    it.copy(
                        isLoadingList = false,
                        error = errorMessage // null si erreur 403, sera ignoré
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
                val errorMessage = resolveError(error)
                _state.update {
                    it.copy(
                        isLoadingList = false,
                        error = errorMessage // null si erreur 403, sera ignoré
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
                // Appel au backend pour créer la visite
                val response = repository.createVisite(request)
                // Attendre que la réponse soit reçue avant de mettre à jour l'UI
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        message = "Visite réservée avec succès"
                    )
                }
                // Attendre un peu pour s'assurer que le serveur a bien enregistré la visite
                kotlinx.coroutines.delay(500)
                // Recharger les visites depuis le backend pour avoir les données à jour
                loadVisites(force = true)
            } catch (error: Exception) {
                val errorMessage = resolveError(error)
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        error = errorMessage // null si erreur 403, sera ignoré
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
                // Récupérer la visite actuelle pour obtenir les infos
                val currentVisite = _state.value.visites.find { it.id == visiteId }
                
                // Appel au backend pour mettre à jour la visite
                val response = repository.updateVisite(visiteId, body)
                
                // Créer notification "Visite modifiée" pour le colocataire (enregistrée dans MongoDB)
                if (currentVisite != null && sessionManager != null) {
                    val userSession = sessionManager.sessionFlow.firstOrNull()
                    if (userSession?.role?.lowercase() != "collocator") {
                        // C'est le client qui modifie, notifier le colocataire
                        val collectorId = getCollectorIdFromLogement(currentVisite.logementId)
                        if (collectorId != null) {
                            createNotificationForUser(
                                targetUserId = collectorId,
                                type = "visite_modified",
                                title = "Visite modifiée",
                                message = "${currentVisite.clientUsername ?: "Le client"} a modifié la visite pour ${currentVisite.logementTitle ?: "le logement"}",
                                visite = response
                            )
                        }
                    }
                }
                
                // Attendre que la réponse soit reçue avant de mettre à jour l'UI
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        message = "✏️ Visite modifiée"
                    )
                }
                // Recharger les visites depuis le backend pour avoir les données à jour
                loadVisites(force = true)
            } catch (error: Exception) {
                val errorMessage = resolveError(error)
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        error = errorMessage // null si erreur 403, sera ignoré
                    )
                }
            }
        }
    }

    fun cancelVisite(visiteId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, error = null, message = null) }
            try {
                // Récupérer la visite avant annulation pour obtenir les infos
                val currentVisite = _state.value.visites.find { it.id == visiteId }
                
                // Appel au backend pour annuler la visite (enregistre dans MongoDB)
                // Le backend crée automatiquement la notification pour le collecteur
                val response = repository.cancelVisite(visiteId)
                
                android.util.Log.d("VisiteViewModel", "Visite annulée avec succès. Le backend devrait créer la notification pour le collecteur.")
                
                // Le backend gère la création de la notification pour le collecteur
                // Pas besoin de créer une notification côté client
                
                // Attendre que la réponse soit reçue (données enregistrées dans MongoDB)
                val logementTitle = response.logementTitle?.takeIf { it.isNotBlank() } 
                    ?: currentVisite?.logementTitle?.takeIf { it.isNotBlank() }
                    ?: "le logement"
                _state.update { it.copy(isSubmitting = false, message = "✅ Visite annulée pour $logementTitle") }
                // Recharger les visites depuis le backend/MongoDB pour avoir les données à jour
                loadVisites(force = true)
            } catch (error: Exception) {
                if (error is HttpException && error.code() == 403) {
                    // Si 403, essayer de supprimer la visite
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
                // Appel au backend pour accepter la visite (enregistre dans MongoDB)
                val response = repository.acceptVisite(visiteId)
                
                // Créer notification "Visite acceptée" pour le client (enregistrée dans MongoDB)
                if (response.userId != null) {
                    createNotificationForUser(
                        targetUserId = response.userId,
                        type = "visite_accepted",
                        title = "Visite acceptée",
                        message = "Votre demande de visite pour ${response.logementTitle ?: "le logement"} a été acceptée",
                        visite = response
                    )
                }
                
                // Créer automatiquement les rappels pour la visite
                createRemindersForVisite(response)
                
                // Attendre que la réponse soit reçue (données enregistrées dans MongoDB)
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        message = "✅ Visite acceptée"
                    )
                }
                // Recharger les visites depuis le backend/MongoDB pour avoir les données à jour
                loadLogementsVisites(force = true)
            } catch (error: Exception) {
                val errorMessage = resolveError(error)
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        error = errorMessage // null si erreur 403, sera ignoré
                    )
                }
            }
        }
    }
    
    /**
     * Récupère l'ID du colocataire (owner) depuis le logement
     */
    private suspend fun getCollectorIdFromLogement(logementId: String?): String? {
        if (logementId == null) {
            android.util.Log.w("VisiteViewModel", "getCollectorIdFromLogement: logementId est null")
            return null
        }
        if (logementRepository == null) {
            android.util.Log.w("VisiteViewModel", "getCollectorIdFromLogement: logementRepository est null")
            return null
        }
        
        return try {
            android.util.Log.d("VisiteViewModel", "Recherche du collecteur pour logementId: $logementId")
            val logements = logementRepository.getAllLogements()
            android.util.Log.d("VisiteViewModel", "Nombre de logements récupérés: ${logements.size}")
            val logement = logements.find { it.id == logementId }
            if (logement == null) {
                android.util.Log.w("VisiteViewModel", "Logement non trouvé pour logementId: $logementId")
                return null
            }
            val ownerId = logement.ownerId
            android.util.Log.d("VisiteViewModel", "OwnerId trouvé: $ownerId pour logementId: $logementId")
            ownerId
        } catch (e: Exception) {
            android.util.Log.e("VisiteViewModel", "Erreur récupération ownerId pour logementId: $logementId", e)
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Crée une notification pour un utilisateur
     */
    // Note: Les notifications sont maintenant gérées par le backend Firebase (NestJS)
    // Cette méthode n'est plus utilisée car les notifications sont créées automatiquement
    // par le backend lors des changements d'état des visites
    private suspend fun createNotificationForUser(
        targetUserId: String,
        type: String,
        title: String,
        message: String,
        visite: VisiteResponse
    ) {
        // Les notifications Firebase sont gérées par le backend NestJS
        android.util.Log.d("VisiteViewModel", "Notifications gérées par le backend Firebase pour type=$type, userId=$targetUserId")
    }
    
    /**
     * Les rappels sont maintenant gérés automatiquement par le backend Firebase (NestJS)
     * Cette méthode n'est plus utilisée car les notifications Firebase sont créées par le backend
     */
    private suspend fun createRemindersForVisite(visite: VisiteResponse) {
        // Les rappels Firebase sont gérés par le backend NestJS
        android.util.Log.d("VisiteViewModel", "Rappels gérés par le backend Firebase pour visite ${visite.id}")
    }

    fun rejectVisite(visiteId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, error = null, message = null) }
            try {
                android.util.Log.d("VisiteViewModel", "Tentative de refus de visite: $visiteId")
                
                // Appel au backend pour refuser la visite (enregistre dans MongoDB)
                val response = repository.rejectVisite(visiteId)
                android.util.Log.d("VisiteViewModel", "Visite refusée avec succès, userId: ${response.userId}")
                
                // Créer notification "Visite refusée" pour le client (enregistrée dans MongoDB)
                if (response.userId != null) {
                    android.util.Log.d("VisiteViewModel", "Création notification de refus pour userId: ${response.userId}")
                    createNotificationForUser(
                        targetUserId = response.userId,
                        type = "visite_rejected",
                        title = "Visite refusée",
                        message = "Votre demande de visite pour ${response.logementTitle ?: "le logement"} a été refusée",
                        visite = response
                    )
                } else {
                    android.util.Log.w("VisiteViewModel", "userId est null dans la réponse, impossible de créer la notification")
                }
                
                // Attendre que la réponse soit reçue (données enregistrées dans MongoDB)
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        message = "✅ Visite refusée"
                    )
                }
                // Recharger les visites depuis le backend/MongoDB pour avoir les données à jour
                loadLogementsVisites(force = true)
            } catch (error: Exception) {
                android.util.Log.e("VisiteViewModel", "Erreur lors du refus de visite: $visiteId", error)
                error.printStackTrace()
                val errorMessage = resolveError(error)
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        error = errorMessage // null si erreur 403, sera ignoré
                    )
                }
            }
        }
    }

    fun deleteVisite(visiteId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, error = null, message = null) }
            try {
                // Récupérer la visite avant suppression pour obtenir les infos
                val currentVisite = _state.value.visites.find { it.id == visiteId }
                
                // Appel au backend pour supprimer la visite
                repository.deleteVisite(visiteId)
                
                // Créer notification "Visite annulée" pour le colocataire (enregistrée dans MongoDB)
                if (currentVisite != null && sessionManager != null) {
                    val userSession = sessionManager.sessionFlow.firstOrNull()
                    if (userSession?.role?.lowercase() != "collocator") {
                        // C'est le client qui annule, notifier le colocataire
                        val collectorId = getCollectorIdFromLogement(currentVisite.logementId)
                        if (collectorId != null) {
                            createNotificationForUser(
                                targetUserId = collectorId,
                                type = "visite_cancelled",
                                title = "Visite annulée",
                                message = "${currentVisite.clientUsername ?: "Le client"} a annulé la visite pour ${currentVisite.logementTitle ?: "le logement"}",
                                visite = currentVisite
                            )
                        }
                    }
                }
                
                // Attendre que la réponse soit reçue avant de mettre à jour l'UI
                _state.update { it.copy(isSubmitting = false, message = "🚫 Visite annulée") }
                // Recharger les visites depuis le backend pour avoir les données à jour
                loadVisites(force = true)
            } catch (error: Exception) {
                val errorMessage = resolveError(error)
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        error = errorMessage // null si erreur 403, sera ignoré
                    )
                }
            }
        }
    }

    fun validateVisite(visiteId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, error = null, message = null) }
            try {
                // Récupérer la visite avant validation pour obtenir les infos
                val currentVisite = _state.value.visites.find { it.id == visiteId }
                
                // Appel au backend pour valider la visite
                val response = repository.validateVisite(visiteId)
                
                // Créer notification "Visite effectuée" pour le colocataire (enregistrée dans MongoDB)
                if (currentVisite != null && sessionManager != null) {
                    val userSession = sessionManager.sessionFlow.firstOrNull()
                    if (userSession?.role?.lowercase() != "collocator") {
                        // C'est le client qui valide, notifier le colocataire
                        val collectorId = getCollectorIdFromLogement(currentVisite.logementId)
                        if (collectorId != null) {
                            createNotificationForUser(
                                targetUserId = collectorId,
                                type = "visite_completed",
                                title = "Visite effectuée",
                                message = "${currentVisite.clientUsername ?: "Le client"} a effectué la visite pour ${currentVisite.logementTitle ?: "le logement"}",
                                visite = response
                            )
                        }
                    }
                }
                
                // Attendre que la réponse soit reçue avant de mettre à jour l'UI
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        message = "✅ Visite effectuée"
                    )
                }
                // Recharger les visites depuis le backend pour avoir les données à jour
                // Attendre un peu pour s'assurer que la base de données est à jour
                kotlinx.coroutines.delay(500)
                loadVisites(force = true)
            } catch (error: Exception) {
                val errorMessage = resolveError(error)
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        error = errorMessage // null si erreur 403, sera ignoré
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
            
            // Vérifier que l'ID de la visite est valide
            if (visiteId.isBlank()) {
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        error = "Erreur: L'identifiant de la visite est invalide."
                    )
                }
                return@launch
            }
            
            // S'assurer que les ratings sont dans la plage valide (1-5)
            val validCollectorRating = collectorRating.coerceIn(1, 5)
            val validCleanlinessRating = cleanlinessRating.coerceIn(1, 5)
            val validLocationRating = locationRating.coerceIn(1, 5)
            val validConformityRating = conformityRating.coerceIn(1, 5)
            
            // Créer la requête - certains backends peuvent attendre visiteId dans le body aussi
            val request = CreateReviewRequest(
                visiteId = visiteId, // Inclure l'ID pour compatibilité avec certains backends
                collectorRating = validCollectorRating,
                cleanlinessRating = validCleanlinessRating,
                locationRating = validLocationRating,
                conformityRating = validConformityRating,
                comment = comment?.takeIf { it.isNotBlank() }
            )
            try {
                // Récupérer la visite actuelle pour obtenir les infos
                val currentVisite = _state.value.visites.find { it.id == visiteId }
                
                // Appel au backend pour enregistrer l'évaluation dans MongoDB
                val reviewResponse = repository.createReview(visiteId, request)
                
                // Vérifier que la réponse est valide (l'évaluation a été créée)
                if (reviewResponse.id != null) {
                    // Créer notification "Évaluation faite" pour le colocataire (enregistrée dans MongoDB)
                    if (currentVisite != null && reviewResponse.collectorId != null) {
                        createNotificationForUser(
                            targetUserId = reviewResponse.collectorId,
                            type = "review_submitted",
                            title = "Évaluation faite",
                            message = "${currentVisite.clientUsername ?: "Le client"} a évalué la visite pour ${currentVisite.logementTitle ?: "le logement"}",
                            visite = currentVisite
                        )
                    }
                    
                    // Attendre que la réponse soit reçue avant de mettre à jour l'UI
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            message = "⭐ Évaluation faite"
                        )
                    }
                    // Attendre un peu pour s'assurer que MongoDB a bien enregistré l'évaluation
                    kotlinx.coroutines.delay(800)
                    // Recharger les visites depuis le backend pour avoir les données à jour
                    loadVisites(force = true)
                } else {
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            error = "Erreur: L'évaluation n'a pas été enregistrée correctement dans MongoDB."
                        )
                    }
                }
            } catch (error: Exception) {
                // Log de l'erreur pour débogage
                android.util.Log.e("VisiteViewModel", "Erreur lors de l'évaluation: ${error.message}", error)
                
                val errorMessage = when {
                    error is HttpException && error.code() == 404 -> {
                        // Recharger automatiquement la liste après une erreur 404
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
                        android.util.Log.e("VisiteViewModel", "Erreur 400: $errorBody")
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
                        error = errorMessage // null si erreur 403, sera ignoré
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
                // Appel au backend pour mettre à jour le statut (enregistre dans MongoDB)
                val response = repository.updateStatus(visiteId, status)
                // Attendre que la réponse soit reçue (données enregistrées dans MongoDB)
                _state.update { it.copy(isSubmitting = false, message = successMessage) }
                // Recharger les visites depuis le backend/MongoDB pour avoir les données à jour
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
            // Appel au backend pour supprimer la visite (supprime dans MongoDB)
            repository.deleteVisite(visiteId)
            // Attendre que la réponse soit reçue (données supprimées dans MongoDB)
            _state.update { it.copy(isSubmitting = false, message = successMessage) }
            // Recharger les visites depuis le backend/MongoDB pour avoir les données à jour
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
                        // Messages d'erreur plus clairs pour les cas spécifiques
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
                        // Message convivial pour les erreurs serveur
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
        
        // Essayer d'extraire le message depuis un JSON
        try {
            // Chercher un champ "message" dans le JSON
            val messageMatch = Regex(""""message"\s*:\s*"([^"]+)"""").find(errorBody)
            if (messageMatch != null) {
                val extracted = messageMatch.groupValues[1]
                return if (extracted.length <= 100) extracted else extracted.take(97) + "..."
            }
            
            // Chercher un champ "error" dans le JSON
            val errorMatch = Regex(""""error"\s*:\s*"([^"]+)"""").find(errorBody)
            if (errorMatch != null) {
                val extracted = errorMatch.groupValues[1]
                return if (extracted.length <= 100) extracted else extracted.take(97) + "..."
            }
        } catch (e: Exception) {
            // Si le parsing échoue, continuer avec la méthode par défaut
        }
        
        // Si pas de JSON valide, prendre les 100 premiers caractères
        return errorBody.take(100) + if (errorBody.length > 100) "..." else ""
    }

    private fun buildIsoDateTime(dateMillis: Long, hour: Int, minute: Int): String {
        // First, create a calendar in LOCAL timezone with the selected date and time
        val localCalendar = Calendar.getInstance().apply {
            timeInMillis = dateMillis
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        // Then convert to UTC for storage
        val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = localCalendar.timeInMillis
        }
        
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return formatter.format(utcCalendar.time)
    }
}