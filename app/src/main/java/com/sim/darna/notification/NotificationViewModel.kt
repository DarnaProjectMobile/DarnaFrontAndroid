package com.sim.darna.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import retrofit2.HttpException

data class NotificationUiState(
    val notifications: List<NotificationResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class NotificationViewModel(
    private val repository: NotificationRepository
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationUiState())
    val state: StateFlow<NotificationUiState> = _state

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount

    private val _selectedNotification = MutableStateFlow<NotificationResponse?>(null)
    val selectedNotification: StateFlow<NotificationResponse?> = _selectedNotification

    fun loadNotificationById(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val notification = repository.getNotificationById(id)
                _selectedNotification.value = notification
                _state.update { it.copy(isLoading = false, error = null) }
            } catch (e: IOException) {
                android.util.Log.w("NotificationViewModel", "Erreur de connexion, tentative de chargement depuis la liste", e)
                // Solution de secours : charger toutes les notifications et trouver celle recherchée
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
                                error = "Erreur de connexion. Vérifiez votre connexion internet."
                            )
                        }
                        _selectedNotification.value = null
                    }
                } catch (e2: Exception) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "Erreur de connexion. Vérifiez votre connexion internet."
                        )
                    }
                    _selectedNotification.value = null
                }
            } catch (e: HttpException) {
                android.util.Log.w("NotificationViewModel", "Erreur HTTP ${e.code()}, tentative de chargement depuis la liste", e)
                // Solution de secours : charger toutes les notifications et trouver celle recherchée
                if (e.code() == 404) {
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
                    } catch (e2: Exception) {
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
                    }
                } else {
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
                }
            } catch (e: Exception) {
                android.util.Log.e("NotificationViewModel", "Erreur lors du chargement de la notification", e)
                // Dernière tentative : charger toutes les notifications
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
                                error = "Erreur inattendue: ${e.message ?: "Erreur de connexion"}"
                            )
                        }
                        _selectedNotification.value = null
                    }
                } catch (e2: Exception) {
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
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                android.util.Log.d("NotificationViewModel", "Chargement des notifications...")
                val notifications = repository.getMyNotifications()
                android.util.Log.d("NotificationViewModel", "Notifications reçues: ${notifications.size}")
                
                // Filtrer les notifications cachées
                val visibleNotifications = notifications.filter { it.hidden != true }
                android.util.Log.d("NotificationViewModel", "Notifications visibles (non cachées): ${visibleNotifications.size}")
                
                notifications.forEach { notification ->
                    android.util.Log.d("NotificationViewModel", "Notification: id=${notification.id}, type=${notification.type}, title=${notification.title}, userId=${notification.userId}, hidden=${notification.hidden}, read=${notification.read}")
                }
                
                // Afficher un avertissement si aucune notification n'est reçue
                if (notifications.isEmpty()) {
                    android.util.Log.w("NotificationViewModel", "AUCUNE notification reçue du backend. Vérifiez que:")
                    android.util.Log.w("NotificationViewModel", "1. Le token d'authentification est valide")
                    android.util.Log.w("NotificationViewModel", "2. Le backend retourne bien les notifications pour cet utilisateur")
                    android.util.Log.w("NotificationViewModel", "3. Les notifications existent bien en base de données")
                } else if (visibleNotifications.isEmpty()) {
                    android.util.Log.w("NotificationViewModel", "Toutes les notifications sont cachées (hidden=true)")
                }
                _state.update {
                    it.copy(
                        notifications = notifications,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: IOException) {
                android.util.Log.e("NotificationViewModel", "Erreur IO lors du chargement des notifications", e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Erreur de connexion. Vérifiez votre connexion internet."
                    )
                }
            } catch (e: HttpException) {
                android.util.Log.e("NotificationViewModel", "Erreur HTTP ${e.code()} lors du chargement des notifications", e)
                val errorMessage = when (e.code()) {
                    401 -> "Session expirée. Veuillez vous reconnecter."
                    403 -> "Accès refusé. Vérifiez vos permissions."
                    404 -> {
                        // Pour 404, essayer de donner plus d'informations
                        val errorBody = try {
                            e.response()?.errorBody()?.string() ?: ""
                        } catch (_: Exception) {
                            ""
                        }
                        if (errorBody.contains("not found", ignoreCase = true) || 
                            errorBody.contains("404", ignoreCase = true)) {
                            "Service non disponible. Vérifiez que le serveur est démarré et que l'URL est correcte."
                        } else {
                            "Endpoint non trouvé. Vérifiez la configuration du serveur."
                        }
                    }
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
                android.util.Log.e("NotificationViewModel", "Erreur inattendue lors du chargement des notifications", e)
                e.printStackTrace()
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
                val count = repository.getUnreadCount()
                _unreadCount.value = count
            } catch (e: HttpException) {
                // Pour 404, on peut ignorer silencieusement (pas de notifications)
                if (e.code() != 404) {
                    android.util.Log.w("NotificationViewModel", "Erreur lors du chargement du compteur: ${e.code()}")
                }
                // Pour les autres erreurs, on met 0 par défaut
                if (e.code() == 404) {
                    _unreadCount.value = 0
                }
            } catch (e: Exception) {
                // Ignorer les autres erreurs pour le compteur (non critique)
                android.util.Log.w("NotificationViewModel", "Erreur lors du chargement du compteur", e)
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
                    // Notification déjà supprimée ou inexistante, recharger quand même
                    loadNotifications()
                    loadUnreadCount()
                } else {
                    android.util.Log.e("NotificationViewModel", "Erreur lors du marquage comme lu: ${e.code()}")
                }
            } catch (e: Exception) {
                android.util.Log.e("NotificationViewModel", "Erreur lors du marquage comme lu", e)
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                repository.markAllAsRead()
                loadNotifications()
                loadUnreadCount()
            } catch (e: HttpException) {
                if (e.code() == 404) {
                    // Endpoint non trouvé, mais on peut continuer
                    loadNotifications()
                    loadUnreadCount()
                } else {
                    android.util.Log.e("NotificationViewModel", "Erreur lors du marquage de toutes comme lues: ${e.code()}")
                }
            } catch (e: Exception) {
                android.util.Log.e("NotificationViewModel", "Erreur lors du marquage de toutes comme lues", e)
            }
        }
    }

    fun hideNotification(id: String) {
        viewModelScope.launch {
            try {
                repository.hideNotification(id)
                loadNotifications()
                loadUnreadCount()
            } catch (e: HttpException) {
                if (e.code() == 404) {
                    // Notification déjà supprimée, recharger quand même
                    loadNotifications()
                    loadUnreadCount()
                } else {
                    android.util.Log.e("NotificationViewModel", "Erreur lors du masquage: ${e.code()}")
                }
            } catch (e: Exception) {
                android.util.Log.e("NotificationViewModel", "Erreur lors du masquage", e)
            }
        }
    }

    fun deleteNotification(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteNotification(id)
                loadNotifications()
                loadUnreadCount()
            } catch (e: HttpException) {
                if (e.code() == 404) {
                    // Notification déjà supprimée, recharger quand même
                    loadNotifications()
                    loadUnreadCount()
                } else {
                    android.util.Log.e("NotificationViewModel", "Erreur lors de la suppression: ${e.code()}")
                }
            } catch (e: Exception) {
                android.util.Log.e("NotificationViewModel", "Erreur lors de la suppression", e)
            }
        }
    }
}





