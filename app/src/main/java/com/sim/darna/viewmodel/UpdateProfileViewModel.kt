package com.sim.darna.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sim.darna.auth.RetrofitClient
import com.sim.darna.model.UserDto
import com.sim.darna.repository.UserRepository
import com.sim.darna.utils.ImageUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class UpdateProfileViewModel : ViewModel() {

    private var repo: UserRepository? = null

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess

    private val _imageUploadSuccess = MutableStateFlow(false)
    val imageUploadSuccess: StateFlow<Boolean> = _imageUploadSuccess

    fun init(context: Context) {
        if (repo == null) {
            val api = RetrofitClient.userApi(context)
            val prefs = context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE)
            repo = UserRepository(api, prefs)
        }
    }

    fun updateProfile(
        username: String,
        email: String,
        numTel: String?,
        dateDeNaissance: String?,
        gender: String?
    ) {
        val repository = repo ?: return

        _isLoading.value = true
        _errorMessage.value = null
        _isSuccess.value = false

        viewModelScope.launch {
            repository.updateUser(
                username = username,
                email = email,
                numTel = numTel,
                dateDeNaissance = dateDeNaissance,
                gender = gender,
                imageFile = null
            ).enqueue(object : Callback<UserDto> {
                override fun onResponse(call: Call<UserDto>, response: Response<UserDto>) {
                    _isLoading.value = false
                    if (response.isSuccessful) {
                        val updatedUser = response.body()
                        if (updatedUser != null) {
                            // Save updated user to SharedPreferences
                            repository.saveUser(updatedUser)
                            _isSuccess.value = true
                        } else {
                            _errorMessage.value = "Update failed: No data returned"
                        }
                    } else {
                        _errorMessage.value = "Update failed: ${response.message()}"
                    }
                }

                override fun onFailure(call: Call<UserDto>, t: Throwable) {
                    _isLoading.value = false
                    _errorMessage.value = "Network error: ${t.message}"
                }
            })
        }
    }

    // Add image upload functionality
    fun updateProfileImage(context: Context, imageUri: Uri) {
        val repository = repo ?: return

        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
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
                        repository.uploadImage(file)
                            .enqueue(object : Callback<UserDto> {
                                override fun onResponse(call: Call<UserDto>, response: Response<UserDto>) {
                                    _isLoading.value = false
                                    if (response.isSuccessful) {
                                        val updatedUser = response.body()
                                        if (updatedUser != null) {
                                            repository.saveUser(updatedUser)
                                            _imageUploadSuccess.value = true
                                        } else {
                                            _errorMessage.value = "Image upload failed: No data returned"
                                        }
                                    } else {
                                        _errorMessage.value = "Image upload failed: ${response.message()}"
                                    }
                                }

                                override fun onFailure(call: Call<UserDto>, t: Throwable) {
                                    _isLoading.value = false
                                    _errorMessage.value = "Image upload network error: ${t.message}"
                                }
                            })
                    } else {
                        _isLoading.value = false
                        _errorMessage.value = "Could not create temporary file"
                    }
                } else {
                    _isLoading.value = false
                    _errorMessage.value = "Could not process image file"
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "Error processing image: ${e.message}"
            }
        }
    }

    fun resetState() {
        _isSuccess.value = false
        _errorMessage.value = null
        _imageUploadSuccess.value = false
    }
}