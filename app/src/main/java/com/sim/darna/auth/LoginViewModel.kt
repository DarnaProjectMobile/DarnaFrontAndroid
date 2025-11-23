package com.sim.darna.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val message: String? = null
)

class LoginViewModel(
    private val repository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state

    fun login(email: String, password: String) {
        _state.value = LoginUiState(isLoading = true)

        val request = LoginRequest(email, password)

        viewModelScope.launch {
            repository.login(request).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let { body ->
                            viewModelScope.launch {
                                try {
                                    // S'assurer que la sauvegarde est complètement terminée
                                    sessionManager.saveSession(body)
                                    _state.value = LoginUiState(
                                        success = true,
                                        message = "Connexion réussie ✅"
                                    )
                                } catch (e: Exception) {
                                    _state.value = LoginUiState(
                                        error = "Erreur lors de la sauvegarde de la session: ${e.message}"
                                    )
                                }
                            }
                        }
                    } else if (response.code() == 401 || response.code() == 400) {
                        _state.value = LoginUiState(
                            error = "Email ou mot de passe incorrect ❌"
                        )
                    } else {
                        _state.value = LoginUiState(
                            error = "Erreur du serveur (${response.code()})"
                        )
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    val fallbackMessage = t.localizedMessage ?: "Impossible de contacter le serveur"
                    _state.value = LoginUiState(
                        error = "Erreur réseau : $fallbackMessage"
                    )
                }
            })
        }
    }
}
