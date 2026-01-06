package com.sim.darna.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sim.darna.model.UserDto
import com.sim.darna.repository.UserRepository
import com.sim.darna.utils.ImageUtils
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
        numTel: String? = null,
        dateDeNaissance: String? = null,
        gender: String? = null
        // Image updates are now handled separately
    ) {
        _uiState.value = _uiState.value.copy(isUpdating = true, error = null)

        userRepository.updateUser(username, email, numTel, dateDeNaissance, gender, null) // No image file passed here
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

    fun updateProfileImage(imageFile: File) {
        _uiState.value = _uiState.value.copy(isUpdating = true, error = null)

        userRepository.uploadImage(imageFile)
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
                            error = "Image update failed: ${response.message()}"
                        )
                    }
                }

                override fun onFailure(call: Call<UserDto>, t: Throwable) {
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        error = "Image network error: ${t.message}"
                    )
                }
            })
    }

    fun updateProfileImageFromUri(context: android.content.Context, imageUri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true, error = null)

            try {
                // Convert URI to multipart part using our utility
                val imagePart = ImageUtils.imageUriToMultipartPart(context, imageUri, "image")
                
                if (imagePart != null) {
                    // Create a temporary file from the URI to pass to the repository
                    val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
                    val inputStream = context.contentResolver.openInputStream(imageUri)
                    inputStream?.use { input ->
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    
                    if (file.exists()) {
                        userRepository.uploadImage(file)
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
                                            error = "Image update failed: ${response.message()}"
                                        )
                                    }
                                }

                                override fun onFailure(call: Call<UserDto>, t: Throwable) {
                                    _uiState.value = _uiState.value.copy(
                                        isUpdating = false,
                                        error = "Image network error: ${t.message}"
                                    )
                                }
                            })
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isUpdating = false,
                            error = "Could not create image file from URI"
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        error = "Could not process image file"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isUpdating = false,
                    error = "Error processing image: ${e.message}"
                )
            }
        }
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