package com.sim.darna.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sim.darna.repository.AuthRepository
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
    val loginResponse: LoginResponse? = null
)


class LoginViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state

    private val _currentUser = MutableStateFlow<UserDto?>(null)
    val currentUser: StateFlow<UserDto?> = _currentUser

    fun login(email: String, password: String) {

        _state.value = LoginUiState(isLoading = true)

        val request = LoginRequest(email, password)

        repository.login(request).enqueue(object : Callback<LoginResponse> {

            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {

                if (!response.isSuccessful || response.body() == null) {
                    _state.value = LoginUiState(error = "Email ou mot de passe incorrect ❌")
                    return
                }

                val data = response.body()!!

                // Save token and user data
                repository.saveToken(data.token)
                data.user?.let { user ->
                    repository.saveUser(user)
                    _currentUser.value = user
                }

                // Update UI state with response
                _state.value = LoginUiState(
                    loginResponse = data
                )
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                _state.value = LoginUiState(error = "Erreur réseau")
            }
        })
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            val token = repository.getSavedToken() ?: return@launch
            try {
                val user = repository.getCurrentUser(token)
                _currentUser.value = user
            } catch (e: Exception) {
                _currentUser.value = null
            }
        }
    }
}
