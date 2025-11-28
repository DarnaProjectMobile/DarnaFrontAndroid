package com.sim.darna.reviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class ReviewsUiState(
    val feedbacks: List<CollectorReviewResponse> = emptyList(),
    val reputation: CollectorReputationResponse? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class ReviewsViewModel(
    private val repository: ReviewsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ReviewsUiState())
    val state: StateFlow<ReviewsUiState> = _state

    fun loadMyReviewsAndReputation() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val reputation = repository.getMyReputation()
                val feedbacks = repository.getMyFeedbacks()
                _state.update {
                    it.copy(
                        isLoading = false,
                        reputation = reputation,
                        feedbacks = feedbacks
                    )
                }
            } catch (e: Exception) {
                val errorMessage = resolveError(e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = errorMessage // null si erreur 403, sera ignoré
                    )
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    private fun resolveError(error: Exception): String? {
        return when (error) {
            is HttpException -> {
                when (error.code()) {
                    403 -> null // Ne jamais afficher les erreurs 403
                    else -> "Erreur serveur (${error.code()})"
                }
            }
            is IOException -> "Connexion réseau indisponible"
            else -> error.localizedMessage ?: "Une erreur inattendue est survenue"
        }
    }
}