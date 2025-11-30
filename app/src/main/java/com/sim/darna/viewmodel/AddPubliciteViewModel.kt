package com.sim.darna.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sim.darna.data.model.*
import com.sim.darna.data.repository.PubliciteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddPubliciteViewModel @Inject constructor(
    private val repository: PubliciteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddPubliciteUiState>(AddPubliciteUiState.Idle)
    val uiState: StateFlow<AddPubliciteUiState> = _uiState.asStateFlow()

    private val _selectedType = MutableStateFlow<PubliciteType?>(null)
    val selectedType: StateFlow<PubliciteType?> = _selectedType.asStateFlow()

    fun setType(type: PubliciteType) {
        _selectedType.value = type
    }

    private fun resolveImage(imageUrl: String?): String {
        return if (imageUrl.isNullOrBlank()) {
            "https://picsum.photos/600/400?random=${System.currentTimeMillis()}"
        } else imageUrl
    }

    fun createPublicite(
        titre: String,
        description: String,
        imageUri: Uri?,
        type: PubliciteType,
        categorie: Categorie,
        dateExpiration: String?,
        detailReduction: DetailReduction?,
        detailPromotion: DetailPromotion?,
        detailJeu: DetailJeu?,
        onSuccess: () -> Unit
    ) {
        if (titre.isBlank() || description.isBlank()) {
            _uiState.value = AddPubliciteUiState.Error("Veuillez remplir tous les champs obligatoires")
            return
        }

        viewModelScope.launch {
            _uiState.value = AddPubliciteUiState.Loading
            // Convertir Uri en String pour le backend
            // Note: Vous devrez peut-être uploader l'image vers votre backend
            // et obtenir une URL. Pour l'instant, on utilise le toString() du Uri
            val imageUrl = imageUri?.toString() ?: ""
            val request = CreatePubliciteRequest(
                titre = titre,
                description = description,
                image = resolveImage(imageUrl),
                type = type,
                categorie = categorie,
                dateExpiration = dateExpiration,
                detailReduction = detailReduction,
                detailPromotion = detailPromotion,
                detailJeu = detailJeu
            )

            val result = repository.createPublicite(request)
            result.fold(
                onSuccess = {
                    _uiState.value = AddPubliciteUiState.Success
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.value = AddPubliciteUiState.Error(error.message ?: "Erreur lors de la création")
                }
            )
        }
    }

    fun updatePublicite(
        id: String,
        titre: String,
        description: String,
        imageUri: Uri?,
        type: PubliciteType,
        categorie: Categorie,
        dateExpiration: String?,
        detailReduction: DetailReduction?,
        detailPromotion: DetailPromotion?,
        detailJeu: DetailJeu?,
        onSuccess: () -> Unit
    ) {
        if (titre.isBlank() || description.isBlank()) {
            _uiState.value = AddPubliciteUiState.Error("Veuillez remplir tous les champs obligatoires")
            return
        }

        viewModelScope.launch {
            _uiState.value = AddPubliciteUiState.Loading
            // Convertir Uri en String pour le backend
            // Note: Vous devrez peut-être uploader l'image vers votre backend
            // et obtenir une URL. Pour l'instant, on utilise le toString() du Uri
            val imageUrl = imageUri?.toString() ?: ""
            val request = UpdatePubliciteRequest(
                titre = titre,
                description = description,
                image = resolveImage(imageUrl),
                type = type,
                categorie = categorie,
                dateExpiration = dateExpiration,
                detailReduction = detailReduction,
                detailPromotion = detailPromotion,
                detailJeu = detailJeu
            )

            val result = repository.updatePublicite(id, request)
            result.fold(
                onSuccess = {
                    _uiState.value = AddPubliciteUiState.Success
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.value = AddPubliciteUiState.Error(error.message ?: "Erreur lors de la mise à jour")
                }
            )
        }
    }

    fun resetState() {
        _uiState.value = AddPubliciteUiState.Idle
        _selectedType.value = null
    }
}

sealed class AddPubliciteUiState {
    object Idle : AddPubliciteUiState()
    object Loading : AddPubliciteUiState()
    object Success : AddPubliciteUiState()
    data class Error(val message: String) : AddPubliciteUiState()
}

