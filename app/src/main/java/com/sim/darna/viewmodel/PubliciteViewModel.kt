package com.sim.darna.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import android.net.Uri
import com.sim.darna.data.model.Publicite
import com.sim.darna.data.model.QRCodeVerificationResponse
import com.sim.darna.data.repository.PubliciteRepository
import com.sim.darna.data.repository.PubliciteUploadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

@HiltViewModel
class PubliciteViewModel @Inject constructor(
    private val repository: PubliciteRepository,
    private val uploadRepository: PubliciteUploadRepository
) : ViewModel() {

    private val _listState = MutableStateFlow<UiState<List<Publicite>>>(UiState.Loading)
    val listState: StateFlow<UiState<List<Publicite>>> = _listState

    private val _formState = MutableStateFlow<UiState<Publicite?>>(UiState.Success(null))
    val formState: StateFlow<UiState<Publicite?>> = _formState

    private val _detailState = MutableStateFlow<UiState<Publicite?>>(UiState.Success(null))
    val detailState: StateFlow<UiState<Publicite?>> = _detailState

    // Load all publicités
    fun loadPublicites() {
        viewModelScope.launch {
            _listState.value = UiState.Loading
            try {
                val res = repository.getAll()
                Log.d("PubliciteViewModel", "Réponse brute: $res")
                if (res.isSuccessful) {
                    _listState.value = UiState.Success(res.body() ?: emptyList())
                } else {
                    _listState.value = UiState.Error("Erreur: ${res.code()}")
                }
            } catch (e: Exception) {
                _listState.value = UiState.Error(e.localizedMessage ?: "Erreur réseau")
            }
        }
    }

    // Load one publicite
    fun loadPublicite(id: String) {
        viewModelScope.launch {
            _formState.value = UiState.Loading
            try {
                val res = repository.getOne(id)
                if (res.isSuccessful) {
                    _formState.value = UiState.Success(res.body())
                } else {
                    _formState.value = UiState.Error("Erreur: ${res.code()}")
                }
            } catch (e: Exception) {
                _formState.value = UiState.Error(e.localizedMessage ?: "Erreur réseau")
            }
        }
    }

    // CREATE
    fun createPublicite(context: Context, publicite: Map<String, Any>, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                Log.d("PubliciteViewModel", "Creating publicite with payload: $publicite")
                val res = repository.create(context, publicite)
                if (res.isSuccessful) {
                    val createdPublicite = res.body()
                    Log.d("PubliciteViewModel", "Publicité créée avec succès")
                    Log.d("PubliciteViewModel", "Type: ${createdPublicite?.type}")
                    Log.d("PubliciteViewModel", "detailJeu: ${createdPublicite?.detailJeu}")
                    Log.d("PubliciteViewModel", "gains dans la réponse: ${createdPublicite?.detailJeu?.gains}")
                    onResult(true, null)
                    loadPublicites()
                } else {
                    val errorBody = try {
                        res.errorBody()?.string() ?: "Erreur ${res.code()}"
                    } catch (e: Exception) {
                        "Erreur ${res.code()}"
                    }
                    Log.e("PubliciteViewModel", "Error creating publicite: $errorBody")
                    onResult(false, errorBody)
                }
            } catch (e: Exception) {
                Log.e("PubliciteViewModel", "Exception creating publicite: ${e.message}", e)
                onResult(false, e.localizedMessage ?: e.message)
            }
        }
    }

    // UPDATE
    fun updatePublicite(context: Context, id: String, payload: Map<String, Any>, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                Log.d("PubliciteViewModel", "Updating publicite with payload: $payload")
                val res = repository.update(context, id, payload)
                if (res.isSuccessful) {
                    onResult(true, null)
                    loadPublicites()
                } else {
                    val errorBody = res.errorBody()?.string()
                    val errorMessage = if (errorBody != null) {
                        try {
                            // Attempt to parse the error message from the JSON body
                            val json = org.json.JSONObject(errorBody)
                            json.optString("message", "Erreur: ${res.code()}")
                        } catch (e: Exception) {
                            "Erreur: ${res.code()} - $errorBody"
                        }
                    } else {
                        "Erreur: ${res.code()}"
                    }
                    android.util.Log.e("PubliciteViewModel", "Error updating publicite: $errorMessage")
                    onResult(false, errorMessage)
                }
            } catch (e: Exception) {
                android.util.Log.e("PubliciteViewModel", "Exception updating publicite: ${e.localizedMessage}")
                onResult(false, e.localizedMessage)
            }
        }
    }

    // DELETE
    fun deletePublicite(context: Context, id: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val res = repository.delete(context, id)
                if (res.isSuccessful) {
                    onResult(true, null)
                    loadPublicites()
                } else {
                    onResult(false, "Erreur: ${res.code()}")
                }
            } catch (e: Exception) {
                onResult(false, e.localizedMessage)
            }
        }
    }

    // Load publicite detail
    fun loadPubliciteDetail(id: String) {
        viewModelScope.launch {
            _detailState.value = UiState.Loading
            try {
                val res = repository.getOne(id)
                if (res.isSuccessful) {
                    val publicite = res.body()
                    Log.d("PubliciteViewModel", "Publicité chargée: ${publicite?.titre}")
                    Log.d("PubliciteViewModel", "Type: ${publicite?.type}")
                    Log.d("PubliciteViewModel", "qrCode présent: ${!publicite?.qrCode.isNullOrEmpty()}")
                    Log.d("PubliciteViewModel", "qrCode length: ${publicite?.qrCode?.length}")
                    Log.d("PubliciteViewModel", "qrCode (premiers 50 chars): ${publicite?.qrCode?.take(50)}")
                    Log.d("PubliciteViewModel", "coupon: ${publicite?.coupon}")
                    Log.d("PubliciteViewModel", "detailJeu: ${publicite?.detailJeu}")
                    Log.d("PubliciteViewModel", "gains: ${publicite?.detailJeu?.gains}")
                    Log.d("PubliciteViewModel", "gains type: ${publicite?.detailJeu?.gains?.javaClass}")
                    _detailState.value = UiState.Success(publicite)
                } else {
                    val errorBody = res.errorBody()?.string()
                    Log.e("PubliciteViewModel", "Erreur ${res.code()}: $errorBody")
                    _detailState.value = UiState.Error("Erreur: ${res.code()}")
                }
            } catch (e: Exception) {
                Log.e("PubliciteViewModel", "Erreur lors du chargement: ${e.message}", e)
                _detailState.value = UiState.Error(e.localizedMessage ?: "Erreur réseau")
            }
        }
    }
    
    // Upload image
    fun uploadImage(context: Context, imageUri: Uri, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val res = uploadRepository.uploadImage(context, imageUri)
                if (res.isSuccessful && res.body()?.imageUrl != null) {
                    onResult(true, res.body()?.imageUrl)
                } else {
                    onResult(false, res.body()?.error ?: "Erreur lors de l'upload")
                }
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Erreur réseau")
            }
        }
    }
    
    // Verify QR Code
    fun verifyQRCode(context: Context, qrData: String, onResult: (Boolean, QRCodeVerificationResponse?) -> Unit) {
        viewModelScope.launch {
            try {
                val res = repository.verifyQRCode(context, qrData)
                if (res.isSuccessful) {
                    val response = res.body()
                    if (response != null && response.valid) {
                        onResult(true, response)
                    } else {
                        onResult(false, response)
                    }
                } else {
                    val errorBody = res.errorBody()?.string()
                    Log.e("PubliciteViewModel", "Error verifying QR code: $errorBody")
                    onResult(false, null)
                }
            } catch (e: Exception) {
                Log.e("PubliciteViewModel", "Exception verifying QR code: ${e.message}", e)
                onResult(false, null)
            }
        }
    }
}
