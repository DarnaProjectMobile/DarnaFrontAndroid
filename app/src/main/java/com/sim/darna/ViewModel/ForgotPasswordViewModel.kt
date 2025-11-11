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

data class ForgotPasswordUiState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null,
    val message: String? = null
)

class ForgotPasswordViewModel(private val repo: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(ForgotPasswordUiState())
    val state: StateFlow<ForgotPasswordUiState> = _state

    fun sendResetCode(email: String) {
        _state.value = ForgotPasswordUiState(isLoading = true)

        viewModelScope.launch {
            repo.forgotPassword(email).enqueue(object : Callback<AuthApi.ForgotPasswordResponse> {
                override fun onResponse(
                    call: Call<AuthApi.ForgotPasswordResponse>,
                    response: Response<AuthApi.ForgotPasswordResponse>
                ) {
                    if (response.isSuccessful) {
                        _state.value = ForgotPasswordUiState(
                            success = true,
                            message = response.body()?.message ?: "Code envoyé ✅"
                        )
                    } else {
                        _state.value = ForgotPasswordUiState(
                            error = "Erreur : ${response.code()}"
                        )
                    }
                }

                override fun onFailure(call: Call<AuthApi.ForgotPasswordResponse>, t: Throwable) {
                    _state.value = ForgotPasswordUiState(
                        error = "Échec de connexion au serveur 😕"
                    )
                }
            })
        }
    }
}
