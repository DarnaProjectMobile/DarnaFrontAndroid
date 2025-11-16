package com.sim.darna.ViewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sim.darna.api.RetrofitClient
import com.sim.darna.model.LoginRequest
import com.sim.darna.model.LoginResponse
import com.sim.darna.model.UserDto
import com.sim.darna.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// UI State for login screen
data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val message: String? = null,
    val user: UserDto? = null,
    val token: String? = null
)

class LoginViewModel(
    private val repository: AuthRepository,
    private val context: Context? = null
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state

    fun login(email: String, password: String) {
        _state.value = LoginUiState(isLoading = true)
        val request = LoginRequest(email, password)

        repository.login(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    val token = body?.token  // Correct: use 'token' here

                    if (!token.isNullOrEmpty()) {
                        RetrofitClient.saveToken(token) // Save token globally
                    }

                    _state.value = LoginUiState(
                        success = true,
                        message = "Connexion réussie ✅",
                        user = body?.user,
                        token = token
                    )
                } else {
                    _state.value = LoginUiState(
                        error = if (response.code() in listOf(400, 401))
                            "Email ou mot de passe incorrect ❌"
                        else
                            "Erreur serveur (${response.code()})"
                    )
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                _state.value = LoginUiState(
                    error = "Impossible de contacter le serveur 😕"
                )
            }
        })
    }
}
