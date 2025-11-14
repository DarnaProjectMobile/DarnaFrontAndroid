package com.sim.darna.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sim.darna.Repository.AuthRepository
import com.sim.darna.model.LoginRequest
import com.sim.darna.model.LoginResponse
import com.sim.darna.model.UserDto
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
    val message: String? = null,
    val user: UserDto? = null
)

class LoginViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state

    fun login(email: String, password: String) {
        _state.value = LoginUiState(isLoading = true)

        val request = LoginRequest(email, password)

        viewModelScope.launch {
            repository.login(request).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        _state.value = LoginUiState(
                            success = true,
                            message = "Connexion réussie ✅",
                            user = body?.user
                        )
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
                    _state.value = LoginUiState(
                        error = "Échec de connexion au serveur 😕"
                    )
                }
            })
        }
    }
}
