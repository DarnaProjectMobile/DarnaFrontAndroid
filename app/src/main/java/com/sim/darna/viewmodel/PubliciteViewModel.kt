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
                Log.d("PubliciteViewModel", "=== DÉBUT VÉRIFICATION QR CODE ===")
                Log.d("PubliciteViewModel", "QR Data reçu: ${qrData.take(100)}...")
                
                // Essayer d'abord l'API
                val res = repository.verifyQRCode(context, qrData)
                Log.d("PubliciteViewModel", "Réponse API - Code: ${res.code()}, Success: ${res.isSuccessful}")
                
                // Si l'API retourne 404, vérifier localement en cherchant la publicité par coupon ou ID
                if (!res.isSuccessful && res.code() == 404) {
                    Log.d("PubliciteViewModel", "Endpoint API non disponible (404), vérification locale...")
                    verifyQRCodeLocally(qrData, onResult)
                    return@launch
                }
                
                if (res.isSuccessful) {
                    val response = res.body()
                    Log.d("PubliciteViewModel", "Réponse API - valid: ${response?.valid}, message: ${response?.message}, publiciteId: ${response?.publiciteId}")
                    
                    // Même si l'API dit que le QR code est invalide, on vérifie quand même la date
                    // car l'API pourrait ne pas vérifier la date d'expiration
                    if (response != null) {
                        // Vérifier la date d'expiration de la publicité
                        // La validité du QR code est liée à la date d'expiration de la publicité
                        if (response.publiciteId != null) {
                            try {
                                Log.d("PubliciteViewModel", "Récupération de la publicité avec ID: ${response.publiciteId}")
                                val publiciteRes = repository.getOne(response.publiciteId)
                                
                                if (publiciteRes.isSuccessful) {
                                    val publicite = publiciteRes.body()
                                    Log.d("PubliciteViewModel", "Publicité récupérée - Titre: ${publicite?.titre}, Date expiration: ${publicite?.dateExpiration}")
                                    
                                    if (publicite != null && !publicite.dateExpiration.isNullOrEmpty()) {
                                        // Parser la date d'expiration (format ISO: yyyy-MM-dd)
                                        val expirationDate = try {
                                            // Essayer d'abord le format ISO standard (yyyy-MM-dd)
                                            val isoFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                                            isoFormat.isLenient = false
                                            val parsed = isoFormat.parse(publicite.dateExpiration)
                                            Log.d("PubliciteViewModel", "Date parsée avec format ISO: $parsed")
                                            parsed
                                        } catch (e: Exception) {
                                            Log.d("PubliciteViewModel", "Échec parsing ISO, essai format français. Erreur: ${e.message}")
                                            try {
                                                // Essayer le format français (dd/MM/yyyy)
                                                val frenchFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                                                frenchFormat.isLenient = false
                                                val parsed = frenchFormat.parse(publicite.dateExpiration)
                                                Log.d("PubliciteViewModel", "Date parsée avec format français: $parsed")
                                                parsed
                                            } catch (e2: Exception) {
                                                Log.d("PubliciteViewModel", "Échec parsing français, essai format complet. Erreur: ${e2.message}")
                                                try {
                                                    // Essayer le format avec timestamp complet
                                                    val fullFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
                                                    fullFormat.isLenient = false
                                                    val parsed = fullFormat.parse(publicite.dateExpiration)
                                                    Log.d("PubliciteViewModel", "Date parsée avec format complet: $parsed")
                                                    parsed
                                                } catch (e3: Exception) {
                                                    Log.e("PubliciteViewModel", "Impossible de parser la date dans aucun format. Erreur: ${e3.message}")
                                                    null
                                                }
                                            }
                                        }
                                        
                                        if (expirationDate != null) {
                                            // Obtenir la date actuelle (sans l'heure, seulement la date)
                                            val calendarNow = java.util.Calendar.getInstance()
                                            calendarNow.set(java.util.Calendar.HOUR_OF_DAY, 0)
                                            calendarNow.set(java.util.Calendar.MINUTE, 0)
                                            calendarNow.set(java.util.Calendar.SECOND, 0)
                                            calendarNow.set(java.util.Calendar.MILLISECOND, 0)
                                            val today = calendarNow.time
                                            
                                            // Obtenir la date d'expiration (sans l'heure, seulement la date)
                                            val calendarExp = java.util.Calendar.getInstance()
                                            calendarExp.time = expirationDate
                                            calendarExp.set(java.util.Calendar.HOUR_OF_DAY, 0)
                                            calendarExp.set(java.util.Calendar.MINUTE, 0)
                                            calendarExp.set(java.util.Calendar.SECOND, 0)
                                            calendarExp.set(java.util.Calendar.MILLISECOND, 0)
                                            val expirationDateOnly = calendarExp.time
                                            
                                            Log.d("PubliciteViewModel", "Date aujourd'hui: $today")
                                            Log.d("PubliciteViewModel", "Date expiration: $expirationDateOnly")
                                            Log.d("PubliciteViewModel", "Expiration avant aujourd'hui? ${expirationDateOnly.before(today)}")
                                            
                                            // Comparer les dates : le QR code est valide si la date d'expiration est >= aujourd'hui
                                            // (c'est-à-dire que si on est le jour d'expiration, c'est encore valide)
                                            if (expirationDateOnly.before(today)) {
                                                // La publicité a expiré, le QR code n'est plus valide
                                                Log.d("PubliciteViewModel", "QR Code rejeté: Publicité expirée le ${publicite.dateExpiration}")
                                                onResult(false, QRCodeVerificationResponse(
                                                    valid = false,
                                                    message = "Ce QR Code n'est plus valide. La publicité a expiré le ${publicite.dateExpiration}.",
                                                    reduction = response.reduction,
                                                    publiciteId = response.publiciteId
                                                ))
                                                return@launch
                                            } else {
                                                Log.d("PubliciteViewModel", "QR Code valide: Publicité valide jusqu'au ${publicite.dateExpiration}")
                                                // Le QR code est valide selon la date, on accepte même si l'API a dit invalide
                                                onResult(true, QRCodeVerificationResponse(
                                                    valid = true,
                                                    message = response.message ?: "QR Code valide",
                                                    reduction = response.reduction,
                                                    publiciteId = response.publiciteId
                                                ))
                                                return@launch
                                            }
                                        } else {
                                            Log.w("PubliciteViewModel", "Impossible de parser la date d'expiration: ${publicite.dateExpiration}")
                                            // Si on ne peut pas parser la date, on utilise la réponse de l'API
                                        }
                                    } else {
                                        // Pas de date d'expiration définie, on utilise la réponse de l'API
                                        Log.d("PubliciteViewModel", "QR Code: Pas de date d'expiration définie, utilisation de la réponse API")
                                    }
                                } else {
                                    Log.w("PubliciteViewModel", "Impossible de récupérer la publicité. Code: ${publiciteRes.code()}")
                                }
                            } catch (e: Exception) {
                                Log.e("PubliciteViewModel", "Erreur lors de la récupération de la publicité: ${e.message}", e)
                                e.printStackTrace()
                                // En cas d'erreur, on utilise la réponse de l'API
                            }
                        } else {
                            Log.w("PubliciteViewModel", "QR Code: Pas d'ID de publicité associé")
                        }
                        
                        // Utiliser la réponse de l'API si on n'a pas pu vérifier la date
                        if (response.valid) {
                            Log.d("PubliciteViewModel", "QR Code accepté selon l'API")
                            onResult(true, response)
                        } else {
                            Log.d("PubliciteViewModel", "QR Code rejeté selon l'API: ${response.message}")
                            onResult(false, response)
                        }
                    } else {
                        Log.e("PubliciteViewModel", "Réponse API est null")
                        onResult(false, null)
                    }
                } else {
                    val errorBody = res.errorBody()?.string()
                    Log.e("PubliciteViewModel", "Erreur API - Code: ${res.code()}, Body: $errorBody")
                    onResult(false, null)
                }
            } catch (e: Exception) {
                Log.e("PubliciteViewModel", "Exception lors de la vérification: ${e.message}", e)
                e.printStackTrace()
                onResult(false, null)
            }
            Log.d("PubliciteViewModel", "=== FIN VÉRIFICATION QR CODE ===")
        }
    }
    
    // Vérification locale du QR code (quand l'API n'est pas disponible)
    private suspend fun verifyQRCodeLocally(qrData: String, onResult: (Boolean, QRCodeVerificationResponse?) -> Unit) {
        try {
            Log.d("PubliciteViewModel", "=== VÉRIFICATION LOCALE QR CODE ===")
            
            // Récupérer toutes les publicités
            val allPublicitesRes = repository.getAll()
            if (!allPublicitesRes.isSuccessful) {
                Log.e("PubliciteViewModel", "Impossible de récupérer les publicités")
                onResult(false, QRCodeVerificationResponse(
                    valid = false,
                    message = "Impossible de vérifier le QR Code. Veuillez réessayer.",
                    reduction = null,
                    publiciteId = null
                ))
                return
            }
            
            val allPublicites = allPublicitesRes.body() ?: emptyList()
            Log.d("PubliciteViewModel", "Nombre de publicités trouvées: ${allPublicites.size}")
            
            // Chercher la publicité correspondante au QR code (par coupon ou ID)
            val publicite = allPublicites.find { 
                it.coupon == qrData || 
                it._id == qrData ||
                (it.qrCode != null && it.qrCode.contains(qrData))
            }
            
            if (publicite == null) {
                Log.d("PubliciteViewModel", "Aucune publicité trouvée pour le QR code: $qrData")
                onResult(false, QRCodeVerificationResponse(
                    valid = false,
                    message = "QR Code invalide. Aucune publicité trouvée.",
                    reduction = null,
                    publiciteId = null
                ))
                return
            }
            
            Log.d("PubliciteViewModel", "Publicité trouvée: ${publicite.titre}, ID: ${publicite._id}")
            Log.d("PubliciteViewModel", "Date expiration: ${publicite.dateExpiration}")
            
            // Vérifier la date d'expiration
            if (!publicite.dateExpiration.isNullOrEmpty()) {
                val expirationDate = try {
                    val isoFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                    isoFormat.isLenient = false
                    isoFormat.parse(publicite.dateExpiration)
                } catch (e: Exception) {
                    try {
                        val frenchFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                        frenchFormat.isLenient = false
                        frenchFormat.parse(publicite.dateExpiration)
                    } catch (e2: Exception) {
                        try {
                            val fullFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
                            fullFormat.isLenient = false
                            fullFormat.parse(publicite.dateExpiration)
                        } catch (e3: Exception) {
                            null
                        }
                    }
                }
                
                if (expirationDate != null) {
                    val calendarNow = java.util.Calendar.getInstance()
                    calendarNow.set(java.util.Calendar.HOUR_OF_DAY, 0)
                    calendarNow.set(java.util.Calendar.MINUTE, 0)
                    calendarNow.set(java.util.Calendar.SECOND, 0)
                    calendarNow.set(java.util.Calendar.MILLISECOND, 0)
                    val today = calendarNow.time
                    
                    val calendarExp = java.util.Calendar.getInstance()
                    calendarExp.time = expirationDate
                    calendarExp.set(java.util.Calendar.HOUR_OF_DAY, 0)
                    calendarExp.set(java.util.Calendar.MINUTE, 0)
                    calendarExp.set(java.util.Calendar.SECOND, 0)
                    calendarExp.set(java.util.Calendar.MILLISECOND, 0)
                    val expirationDateOnly = calendarExp.time
                    
                    Log.d("PubliciteViewModel", "Date aujourd'hui: $today")
                    Log.d("PubliciteViewModel", "Date expiration: $expirationDateOnly")
                    Log.d("PubliciteViewModel", "Expiration avant aujourd'hui? ${expirationDateOnly.before(today)}")
                    
                    if (expirationDateOnly.before(today)) {
                        Log.d("PubliciteViewModel", "QR Code rejeté: Publicité expirée le ${publicite.dateExpiration}")
                        onResult(false, QRCodeVerificationResponse(
                            valid = false,
                            message = "Ce QR Code n'est plus valide. La publicité a expiré le ${publicite.dateExpiration}.",
                            reduction = publicite.detailReduction?.pourcentage,
                            publiciteId = publicite._id
                        ))
                        return
                    }
                } else {
                    Log.w("PubliciteViewModel", "Impossible de parser la date: ${publicite.dateExpiration}")
                }
            }
            
            // Le QR code est valide
            Log.d("PubliciteViewModel", "QR Code valide: Publicité '${publicite.titre}' valide jusqu'au ${publicite.dateExpiration ?: "jamais"}")
            onResult(true, QRCodeVerificationResponse(
                valid = true,
                message = "QR Code valide",
                reduction = publicite.detailReduction?.pourcentage,
                publiciteId = publicite._id
            ))
            
        } catch (e: Exception) {
            Log.e("PubliciteViewModel", "Erreur lors de la vérification locale: ${e.message}", e)
            e.printStackTrace()
            onResult(false, QRCodeVerificationResponse(
                valid = false,
                message = "Erreur lors de la vérification du QR Code. Veuillez réessayer.",
                reduction = null,
                publiciteId = null
            ))
        }
    }
}
