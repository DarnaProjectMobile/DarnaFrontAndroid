package com.sim.darna.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sim.darna.model.UserDto
import com.sim.darna.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: UserDto? = null,
    val isUpdating: Boolean = false,
    val isUpdated: Boolean = false,
    val error: String? = null,
    val imageUri: Uri? = null
)

class ProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val user = userRepository.getCurrentUser()
                _uiState.value = _uiState.value.copy(
                    user = user,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load profile: ${e.message}"
                )
            }
        }
    }

    fun updateProfile(
        username: String,
        email: String,
        password: String? = null,
        numTel: String? = null,
        dateDeNaissance: String? = null,
        gender: String? = null,
        imageFile: File? = null
    ) {
        _uiState.value = _uiState.value.copy(isUpdating = true, error = null)

        userRepository.updateUser(username, email, password, numTel, dateDeNaissance, gender, imageFile)
            .enqueue(object : Callback<UserDto> {
                override fun onResponse(call: Call<UserDto>, response: Response<UserDto>) {
                    if (response.isSuccessful) {
                        val updatedUser = response.body()

                        if (updatedUser != null) {
                            userRepository.saveUser(updatedUser)

                            _uiState.value = _uiState.value.copy(
                                user = updatedUser,
                                isUpdating = false,
                                isUpdated = true
                            )
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isUpdating = false,
                            error = "Update failed: ${response.message()}"
                        )
                    }
                }

                override fun onFailure(call: Call<UserDto>, t: Throwable) {
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        error = "Network error: ${t.message}"
                    )
                }
            })
    }

    fun updateImageUri(uri: Uri?) {
        _uiState.value = _uiState.value.copy(imageUri = uri)
    }

    fun resetUpdateState() {
        _uiState.value = _uiState.value.copy(isUpdated = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
