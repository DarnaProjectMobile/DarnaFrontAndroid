package com.sim.darna.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sim.darna.data.model.Publicite
import com.sim.darna.data.repository.PubliciteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val repository: PubliciteRepository
) : ViewModel() {

    private val _listState = MutableStateFlow<UiState<List<Publicite>>>(UiState.Loading)
    val listState: StateFlow<UiState<List<Publicite>>> = _listState

    private val _formState = MutableStateFlow<UiState<Publicite?>>(UiState.Success(null))
    val formState: StateFlow<UiState<Publicite?>> = _formState

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
}
