package com.sim.darna.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class ForgotPasswordUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val message: String? = null,
    val step: ForgotPasswordStep = ForgotPasswordStep.REQUEST_CODE
)

enum class ForgotPasswordStep {
    REQUEST_CODE,
    RESET_PASSWORD
}

class ForgotPasswordViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ForgotPasswordUiState())
    val state: StateFlow<ForgotPasswordUiState> = _state

    fun requestResetCode(email: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            try {
                val response = repository.forgotPassword(email)
                _state.value = _state.value.copy(
                    isLoading = false,
                    success = true,
                    message = response.message,
                    step = ForgotPasswordStep.RESET_PASSWORD
                )
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is HttpException -> {
                        when (e.code()) {
                            400 -> "Email invalide ou utilisateur non trouvé"
                            404 -> "Endpoint non trouvé"
                            500 -> "Erreur serveur. Veuillez réessayer plus tard"
                            else -> "Erreur serveur (${e.code()})"
                        }
                    }
                    else -> "Erreur de connexion: ${e.message}"
                }
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = errorMessage
                )
            }
        }
    }

    fun resetPassword(code: String, newPassword: String, confirmPassword: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            try {
                val response = repository.resetPassword(code, newPassword, confirmPassword)
                _state.value = _state.value.copy(
                    isLoading = false,
                    success = true,
                    message = response.message
                )
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is HttpException -> {
                        when (e.code()) {
                            400 -> "Code invalide ou expiré, ou les mots de passe ne correspondent pas"
                            404 -> "Endpoint non trouvé"
                            500 -> "Erreur serveur. Veuillez réessayer plus tard"
                            else -> "Erreur serveur (${e.code()})"
                        }
                    }
                    else -> "Erreur de connexion: ${e.message}"
                }
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = errorMessage
                )
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun reset() {
        _state.value = ForgotPasswordUiState()
    }
}

