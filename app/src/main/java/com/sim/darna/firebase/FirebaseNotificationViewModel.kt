package com.sim.darna.firebase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import retrofit2.HttpException

data class FirebaseNotificationUiState(
    val notifications: List<FirebaseNotificationResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class FirebaseNotificationViewModel(
    private val repository: FirebaseNotificationRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FirebaseNotificationUiState())
    val state: StateFlow<FirebaseNotificationUiState> = _state

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount

    private val _selectedNotification = MutableStateFlow<FirebaseNotificationResponse?>(null)
    val selectedNotification: StateFlow<FirebaseNotificationResponse?> = _selectedNotification

    fun loadNotificationById(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val notifications = repository.getMyNotifications()
                val found = notifications.find { it.id == id }
                if (found != null) {
                    _selectedNotification.value = found
                    _state.update { it.copy(isLoading = false, error = null) }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "Notification non trouvée."
                        )
                    }
                    _selectedNotification.value = null
                }
            } catch (e: IOException) {
                android.util.Log.e("FirebaseNotificationViewModel", "Erreur de connexion", e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Erreur de connexion. Vérifiez votre connexion internet."
                    )
                }
                _selectedNotification.value = null
            } catch (e: HttpException) {
                android.util.Log.e("FirebaseNotificationViewModel", "Erreur HTTP ${e.code()}", e)
                val errorMessage = when (e.code()) {
                    401 -> "Session expirée. Veuillez vous reconnecter."
                    403 -> "Accès refusé. Vérifiez vos permissions."
                    404 -> "Notification non trouvée."
                    500 -> "Erreur serveur interne. Veuillez réessayer plus tard."
                    else -> "Erreur serveur: ${e.code()}. Veuillez réessayer."
                }
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = errorMessage
                    )
                }
                _selectedNotification.value = null
            } catch (e: Exception) {
                android.util.Log.e("FirebaseNotificationViewModel", "Erreur inattendue", e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Erreur inattendue: ${e.message ?: "Erreur de connexion"}"
                    )
                }
                _selectedNotification.value = null
            }
        }
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                android.util.Log.d("FirebaseNotificationViewModel", "Chargement des notifications Firebase...")
                val notifications = repository.getMyNotifications()
                android.util.Log.d("FirebaseNotificationViewModel", "Notifications reçues: ${notifications.size}")
                
                // Calculer le nombre de notifications non lues
                val unread = notifications.count { it.isRead != true }
                _unreadCount.value = unread
                
                _state.update {
                    it.copy(
                        notifications = notifications,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: IOException) {
                android.util.Log.e("FirebaseNotificationViewModel", "Erreur IO", e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Erreur de connexion. Vérifiez votre connexion internet."
                    )
                }
            } catch (e: HttpException) {
                android.util.Log.e("FirebaseNotificationViewModel", "Erreur HTTP ${e.code()}", e)
                val errorMessage = when (e.code()) {
                    401 -> "Session expirée. Veuillez vous reconnecter."
                    403 -> "Accès refusé. Vérifiez vos permissions."
                    404 -> "Aucune notification trouvée."
                    500 -> "Erreur serveur interne. Veuillez réessayer plus tard."
                    else -> "Erreur serveur: ${e.code()}. Veuillez réessayer."
                }
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = errorMessage
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("FirebaseNotificationViewModel", "Erreur inattendue", e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Erreur inattendue: ${e.message ?: "Erreur de connexion"}"
                    )
                }
            }
        }
    }

    fun loadUnreadCount() {
        viewModelScope.launch {
            try {
                val notifications = repository.getMyNotifications()
                val unread = notifications.count { it.isRead != true }
                _unreadCount.value = unread
            } catch (e: HttpException) {
                if (e.code() != 404) {
                    android.util.Log.w("FirebaseNotificationViewModel", "Erreur lors du chargement du compteur: ${e.code()}")
                }
                if (e.code() == 404) {
                    _unreadCount.value = 0
                }
            } catch (e: Exception) {
                android.util.Log.w("FirebaseNotificationViewModel", "Erreur lors du chargement du compteur", e)
            }
        }
    }

    fun markAsRead(id: String) {
        viewModelScope.launch {
            try {
                repository.markAsRead(id)
                // Recharger les notifications et le compteur
                loadNotifications()
                loadUnreadCount()
            } catch (e: HttpException) {
                if (e.code() == 404) {
                    loadNotifications()
                    loadUnreadCount()
                } else {
                    android.util.Log.e("FirebaseNotificationViewModel", "Erreur lors du marquage comme lu: ${e.code()}")
                }
            } catch (e: Exception) {
                android.util.Log.e("FirebaseNotificationViewModel", "Erreur lors du marquage comme lu", e)
            }
        }
    }

    fun deleteNotification(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteNotification(id)
                // Recharger les notifications et le compteur
                loadNotifications()
                loadUnreadCount()
            } catch (e: HttpException) {
                if (e.code() == 404) {
                    loadNotifications()
                    loadUnreadCount()
                } else {
                    android.util.Log.e("FirebaseNotificationViewModel", "Erreur lors de la suppression: ${e.code()}")
                }
            } catch (e: Exception) {
                android.util.Log.e("FirebaseNotificationViewModel", "Erreur lors de la suppression", e)
            }
        }
    }

    fun markAsUnread(id: String) {
        // Pour marquer comme non lu, on pourrait ajouter un endpoint backend
        // Pour l'instant, on peut juste recharger les notifications
        viewModelScope.launch {
            loadNotifications()
            loadUnreadCount()
        }
    }
}


















