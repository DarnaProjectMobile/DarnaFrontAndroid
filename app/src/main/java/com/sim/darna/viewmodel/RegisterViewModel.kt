package com.sim.darna.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sim.darna.repository.AuthRepository
import com.sim.darna.model.RegisterRequest
import com.sim.darna.model.RegisterResponse
import com.sim.darna.model.LoginRequest
import com.sim.darna.model.LoginResponse
import com.sim.darna.auth.TokenStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

data class RegisterUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

class RegisterViewModel(
    private val repository: AuthRepository,
    private val authPrefs: SharedPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state

    fun register(user: RegisterRequest, imageFile: java.io.File? = null) {
        _state.value = RegisterUiState(isLoading = true)

        viewModelScope.launch {
            try {
                // ✅ Normalisation des valeurs avant conversion
                val normalizedUser = user.copy(
                    role = user.role.lowercase().trim(), // ⚠️ Très important
                    gender = user.gender.replaceFirstChar { it.uppercase() }.trim()
                )

                // ✅ Conversion des champs texte en RequestBody
                fun textBody(value: String): RequestBody =
                    value.toRequestBody("text/plain".toMediaTypeOrNull())

                val username = textBody(normalizedUser.username)
                val email = textBody(normalizedUser.email)
                val password = textBody(normalizedUser.password)
                val role = textBody(normalizedUser.role)
                val dateDeNaissance = textBody(normalizedUser.dateDeNaissance)
                val numTel = textBody(normalizedUser.numTel)
                val gender = textBody(normalizedUser.gender)

                // ✅ Image optionnelle (multipart)
                val imagePart = if (imageFile != null && imageFile.exists()) {
                    val requestBody = okhttp3.RequestBody.create("image/*".toMediaTypeOrNull(), imageFile)
                    MultipartBody.Part.createFormData("image", imageFile.name, requestBody)
                } else {
                    null
                }

                // ✅ Envoi de la requête
                repository.registerMultipart(
                    username,
                    email,
                    password,
                    role,
                    dateDeNaissance,
                    numTel,
                    gender,
                    imagePart
                ).enqueue(object : Callback<RegisterResponse> {
                    override fun onResponse(
                        call: Call<RegisterResponse>,
                        response: Response<RegisterResponse>
                    ) {
                        if (response.isSuccessful) {
                            // Auto-login to get token for verification
                            val loginRequest = LoginRequest(
                                email = normalizedUser.email,
                                password = normalizedUser.password
                            )
                            repository.login(loginRequest).enqueue(object : Callback<LoginResponse> {
                                override fun onResponse(
                                    call: Call<LoginResponse>,
                                    response: Response<LoginResponse>
                                ) {
                                    if (response.isSuccessful && response.body() != null) {
                                        val loginResponse = response.body()!!
                                        // Save token using TokenStorage
                                        TokenStorage.saveAuthDataFromPrefs(
                                            authPrefs,
                                            loginResponse.token,
                                            loginResponse.user.id
                                        )
                                        _state.value = RegisterUiState(success = true)
                                    } else {
                                        _state.value = RegisterUiState(
                                            error = "Inscription réussie mais connexion automatique échouée"
                                        )
                                    }
                                }

                                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                                    _state.value = RegisterUiState(
                                        error = "Inscription réussie mais connexion automatique échouée: ${t.localizedMessage}"
                                    )
                                }
                            })
                        } else {
                            val errorBody = response.errorBody()?.string()
                            _state.value = RegisterUiState(
                                error = "Inscription échouée (${response.code()}): ${errorBody ?: "Erreur inconnue"}"
                            )
                        }
                    }

                    override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                        _state.value = RegisterUiState(
                            error = "Erreur réseau : ${t.localizedMessage ?: "Impossible de contacter le serveur"}"
                        )
                    }
                })
            } catch (e: Exception) {
                _state.value = RegisterUiState(error = "Erreur interne : ${e.localizedMessage}")
            }
        }
    }
}
