package com.sim.darna.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sim.darna.Repository.AuthRepository
import com.sim.darna.auth.AuthApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

data class ResetPasswordUiState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null,
    val message: String? = null
)

class ResetPasswordViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _state = MutableStateFlow(ResetPasswordUiState())
    val state: StateFlow<ResetPasswordUiState> = _state

    fun resetPassword(code: String, newPassword: String, confirmPassword: String) {
        _state.value = ResetPasswordUiState(isLoading = true)

        viewModelScope.launch {
            repository.resetPassword(code, newPassword, confirmPassword)
                .enqueue(object : Callback<AuthApi.ResetPasswordResponse> {
                    override fun onResponse(
                        call: Call<AuthApi.ResetPasswordResponse>,
                        response: Response<AuthApi.ResetPasswordResponse>
                    ) {
                        if (response.isSuccessful) {
                            _state.value = ResetPasswordUiState(
                                success = true,
                                message = response.body()?.message ?: "Mot de passe réinitialisé ✅"
                            )
                        } else {
                            _state.value = ResetPasswordUiState(
                                error = "Erreur : ${response.code()}"
                            )
                        }
                    }

                    override fun onFailure(call: Call<AuthApi.ResetPasswordResponse>, t: Throwable) {
                        _state.value = ResetPasswordUiState(error = "Échec de connexion au serveur 😕")
                    }
                })
        }
    }
}

