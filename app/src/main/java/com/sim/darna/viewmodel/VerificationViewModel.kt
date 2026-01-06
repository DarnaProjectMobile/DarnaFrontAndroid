package com.sim.darna.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sim.darna.auth.AuthApi
import com.sim.darna.auth.TokenStorage
import com.sim.darna.repository.AuthRepository
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

data class VerificationUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

class VerificationViewModel(
    private val repository: AuthRepository,
    private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(VerificationUiState())
    val state: StateFlow<VerificationUiState> = _state

    fun verify(code: String) {
        if (code.isBlank()) {
            _state.value = VerificationUiState(error = "Veuillez entrer le code")
            return
        }

        // Get JWT token
        val token = TokenStorage.getToken(context)
        if (token == null) {
            _state.value = VerificationUiState(error = "Session expirée, veuillez vous reconnecter")
            return
        }
        
        _state.value = VerificationUiState(isLoading = true)

        viewModelScope.launch {
            repository.verifyEmail(token, code).enqueue(object : Callback<AuthApi.VerifyEmailResponse> {
                override fun onResponse(
                    call: Call<AuthApi.VerifyEmailResponse>,
                    response: Response<AuthApi.VerifyEmailResponse>
                ) {
                    if (response.isSuccessful) {
                        // Save isVerified to SharedPreferences
                        val prefs = context.getSharedPreferences("APP_PREFS", android.content.Context.MODE_PRIVATE)
                        prefs.edit().putBoolean("isVerified", true).apply()
                        
                        _state.value = VerificationUiState(success = true)
                    } else {
                        val errorBody = response.errorBody()?.string()
                        _state.value = VerificationUiState(
                            error = "Vérification échouée: $errorBody"
                        )
                    }
                }

                override fun onFailure(call: Call<AuthApi.VerifyEmailResponse>, t: Throwable) {
                    _state.value = VerificationUiState(
                        error = "Erreur réseau : ${t.localizedMessage}"
                    )
                }
            })
        }
    }
}
