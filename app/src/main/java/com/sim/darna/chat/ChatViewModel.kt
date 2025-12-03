package com.sim.darna.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import okhttp3.MultipartBody

data class ChatUiState(
    val messages: List<MessageResponse> = emptyList(),
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val unreadCount: Int = 0,
    val isConnected: Boolean = false
)

class ChatViewModel(
    private val repository: ChatRepository,
    private val baseUrl: String,
    private val token: String?,
    private val userId: String?
) : ViewModel() {

    private val _state = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = _state

    private var socket: Socket? = null
    private var currentVisiteId: String? = null
    private val gson = Gson()

    init {
        // Connecter Socket.IO de manière asynchrone pour éviter les crashes
        viewModelScope.launch {
            try {
                connectSocket()
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Erreur lors de l'initialisation Socket.IO", e)
            }
        }
    }

    private fun connectSocket() {
        if (token == null || userId == null) {
            Log.w("ChatViewModel", "Token ou userId manquant, Socket.IO non connecté")
            return
        }
        
        if (socket != null && socket!!.connected()) {
            Log.d("ChatViewModel", "Socket déjà connecté")
            return
        }

        try {
            // Construire l'URL Socket.IO (enlever le trailing slash si présent)
            val socketUrl = baseUrl.removeSuffix("/")
            val options = IO.Options().apply {
                auth = mapOf("token" to token)
                reconnection = true
                reconnectionAttempts = 5
                reconnectionDelay = 1000
                transports = arrayOf("websocket", "polling")
            }

            socket = IO.socket("$socketUrl/chat", options)

            socket?.on(Socket.EVENT_CONNECT) {
                Log.d("ChatViewModel", "Socket connecté")
                viewModelScope.launch {
                    _state.update { it.copy(isConnected = true) }
                }
            }

            socket?.on(Socket.EVENT_DISCONNECT) {
                Log.d("ChatViewModel", "Socket déconnecté")
                viewModelScope.launch {
                    _state.update { it.copy(isConnected = false) }
                }
            }

            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                Log.e("ChatViewModel", "Erreur de connexion Socket: ${args.contentToString()}")
                viewModelScope.launch {
                    _state.update { 
                        it.copy(
                            isConnected = false,
                            error = "Erreur de connexion au chat en temps réel"
                        )
                    }
                }
            }

            socket?.on("new_message") { args ->
                try {
                    if (args.isNotEmpty() && args[0] is JSONObject) {
                        val messageJson = args[0] as JSONObject
                        val message = gson.fromJson(messageJson.toString(), MessageResponse::class.java)
                        
                        // Ajouter le message seulement s'il appartient à la visite actuelle
                        if (message.visiteId == currentVisiteId) {
                            viewModelScope.launch {
                                _state.update { 
                                    it.copy(
                                        messages = it.messages + message
                                    )
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ChatViewModel", "Erreur lors de la réception du message", e)
                }
            }

            socket?.on("message_sent") { args ->
                Log.d("ChatViewModel", "Message envoyé confirmé")
            }

            socket?.on("error") { args ->
                val errorMsg = if (args.isNotEmpty()) args[0].toString() else "Erreur inconnue"
                Log.e("ChatViewModel", "Erreur Socket: $errorMsg")
                viewModelScope.launch {
                    _state.update { 
                        it.copy(error = errorMsg)
                    }
                }
            }

            socket?.connect()
        } catch (e: Exception) {
            Log.e("ChatViewModel", "Erreur lors de la connexion Socket.IO", e)
            viewModelScope.launch {
                _state.update { 
                    it.copy(
                        isConnected = false,
                        error = "Impossible de se connecter au chat en temps réel"
                    )
                }
            }
        }
    }

    fun joinVisite(visiteId: String) {
        currentVisiteId = visiteId
        socket?.emit("join_visite", JSONObject().apply {
            put("visiteId", visiteId)
        })
    }

    fun leaveVisite(visiteId: String) {
        socket?.emit("leave_visite", JSONObject().apply {
            put("visiteId", visiteId)
        })
        if (currentVisiteId == visiteId) {
            currentVisiteId = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        socket?.disconnect()
        socket = null
    }

    fun loadMessages(visiteId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val messages = repository.getMessages(visiteId)
                _state.update { 
                    it.copy(
                        messages = messages,
                        isLoading = false
                    )
                }
                // Rejoindre la room Socket.IO pour cette visite
                joinVisite(visiteId)
            } catch (error: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Erreur lors du chargement des messages"
                    )
                }
            }
        }
    }

    fun sendMessage(visiteId: String, content: String?, images: List<String>? = null) {
        if (content.isNullOrBlank() && (images.isNullOrEmpty())) return

        viewModelScope.launch {
            _state.update { it.copy(isSending = true, error = null) }
            
            // Essayer d'envoyer via Socket.IO d'abord (temps réel) - seulement pour texte
            if (socket?.connected() == true && userId != null && images.isNullOrEmpty()) {
                try {
                    val messageData = JSONObject().apply {
                        put("visiteId", visiteId)
                        if (!content.isNullOrBlank()) {
                            put("content", content)
                        }
                        put("senderId", userId)
                    }
                    socket?.emit("send_message", messageData)
                    _state.update { it.copy(isSending = false) }
                    // Le message sera ajouté automatiquement via l'événement "new_message" ou "message_sent"
                    return@launch
                } catch (e: Exception) {
                    Log.w("ChatViewModel", "Erreur Socket.IO, fallback vers REST", e)
                }
            }
            
            // Fallback vers REST API si Socket.IO n'est pas disponible ou si images
            try {
                val message = repository.sendMessage(visiteId, content, images)
                _state.update { 
                    it.copy(
                        messages = it.messages + message,
                        isSending = false,
                        message = "Message envoyé"
                    )
                }
            } catch (error: Exception) {
                _state.update { 
                    it.copy(
                        isSending = false,
                        error = error.message ?: "Erreur lors de l'envoi du message"
                    )
                }
            }
        }
    }

    fun sendMessageWithImages(visiteId: String, content: String?, imageParts: List<MultipartBody.Part>) {
        if (content.isNullOrBlank() && imageParts.isEmpty()) return

        viewModelScope.launch {
            _state.update { it.copy(isSending = true, error = null) }
            try {
                val message = repository.sendMessageWithImages(visiteId, content, imageParts)
                _state.update { 
                    it.copy(
                        messages = it.messages + message,
                        isSending = false,
                        message = "Message envoyé"
                    )
                }
            } catch (error: Exception) {
                _state.update { 
                    it.copy(
                        isSending = false,
                        error = error.message ?: "Erreur lors de l'envoi du message"
                    )
                }
            }
        }
    }

    fun uploadImages(images: List<MultipartBody.Part>) {
        viewModelScope.launch {
            _state.update { it.copy(isSending = true, error = null) }
            try {
                val imageUrls = repository.uploadImages(images)
                _state.update { 
                    it.copy(
                        isSending = false,
                        message = "${imageUrls.size} image(s) uploadée(s)"
                    )
                }
            } catch (error: Exception) {
                _state.update { 
                    it.copy(
                        isSending = false,
                        error = error.message ?: "Erreur lors de l'upload des images"
                    )
                }
            }
        }
    }

    fun addMessage(message: MessageResponse) {
        _state.update { 
            it.copy(
                messages = it.messages + message
            )
        }
    }

    fun markAsRead(messageId: String) {
        viewModelScope.launch {
            try {
                repository.markAsRead(messageId)
                _state.update { 
                    it.copy(
                        messages = it.messages.map { msg ->
                            if (msg.id == messageId) {
                                msg.copy(read = true)
                            } else {
                                msg
                            }
                        }
                    )
                }
            } catch (error: Exception) {
                // Ignorer les erreurs silencieusement
            }
        }
    }

    fun markAllAsRead(visiteId: String) {
        viewModelScope.launch {
            try {
                repository.markAllAsRead(visiteId)
                _state.update { 
                    it.copy(
                        messages = it.messages.map { it.copy(read = true) }
                    )
                }
            } catch (error: Exception) {
                // Ignorer les erreurs silencieusement
            }
        }
    }

    fun loadUnreadCount() {
        viewModelScope.launch {
            try {
                val count = repository.getUnreadCount()
                _state.update { it.copy(unreadCount = count) }
            } catch (error: Exception) {
                // Ignorer les erreurs silencieusement
            }
        }
    }

    fun clearFeedback() {
        _state.update { it.copy(error = null, message = null) }
    }
}

