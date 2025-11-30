package com.sim.darna.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sim.darna.auth.RetrofitClient
import com.sim.darna.model.UserDto
import com.sim.darna.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UpdateProfileViewModel : ViewModel() {

    private var repo: UserRepository? = null

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess

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
        password: String? = null,  // Optional password
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
                password = password,  // Pass password (null if not changing)
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

    fun resetState() {
        _isSuccess.value = false
        _errorMessage.value = null
    }
}
