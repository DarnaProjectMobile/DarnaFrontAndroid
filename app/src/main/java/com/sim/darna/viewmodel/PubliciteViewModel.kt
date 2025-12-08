package com.sim.darna.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import android.net.Uri
import com.sim.darna.data.model.Publicite
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
    fun createPublicite(publicite: Map<String, Any>, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val res = repository.create(publicite)
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

    // UPDATE
    fun updatePublicite(id: String, payload: Map<String, Any>, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val res = repository.update(id, payload)
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

    // DELETE
    fun deletePublicite(id: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val res = repository.delete(id)
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
                    _detailState.value = UiState.Success(res.body())
                } else {
                    _detailState.value = UiState.Error("Erreur: ${res.code()}")
                }
            } catch (e: Exception) {
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
}
