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
    val baseUrl: String, // Rendre public pour l'utiliser dans ChatScreen
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
                try {
                    if (args.isNotEmpty() && args[0] is JSONObject) {
                        val messageJson = args[0] as JSONObject
                        val message = gson.fromJson(messageJson.toString(), MessageResponse::class.java)
                        
                        // Si je suis le destinataire, je marque comme livré et potentiellement lu
                        if (message.receiverId == userId) {
                            message.id?.let { id ->
                                updateMessageStatus(id, "delivered")
                                if (currentVisiteId == message.visiteId) {
                                    updateMessageStatus(id, "read")
                                }
                            }
                        }

                        // Ajouter le message envoyé à la liste immédiatement
                        viewModelScope.launch {
                            _state.update { 
                                it.copy(
                                    messages = it.messages + message,
                                    isSending = false
                                )
                            }
                        }
                        Log.d("ChatViewModel", "Message envoyé confirmé et ajouté à la liste")
                    }
                } catch (e: Exception) {
                    Log.e("ChatViewModel", "Erreur lors de la réception de message_sent", e)
                }
            }

            socket?.on("reaction_updated") { args ->
                try {
                    if (args.isNotEmpty() && args[0] is JSONObject) {
                        val data = args[0] as JSONObject
                        val messageId = data.getString("messageId")
                        val reactions = data.optJSONObject("reactions")
                        
                        val reactionsMap = mutableMapOf<String, List<String>>()
                        if (reactions != null) {
                            val keysIterator = reactions.keys()
                            while (keysIterator.hasNext()) {
                                val emoji = keysIterator.next() as String
                                val userIds = reactions.getJSONArray(emoji)
                                val userIdsList = mutableListOf<String>()
                                for (i in 0 until userIds.length()) {
                                    userIdsList.add(userIds.getString(i))
                                }
                                reactionsMap[emoji] = userIdsList
                            }
                        }
                        
                        viewModelScope.launch {
                            _state.update { currentState ->
                                val newMessages = currentState.messages.toMutableList()
                                val index = newMessages.indexOfFirst { it.id == messageId }
                                if (index != -1) {
                                    // Important : créer une NOUVELLE instance du message avec une NOUVELLE map pour déclencher la recomposition
                                    newMessages[index] = newMessages[index].copy(reactions = HashMap(reactionsMap))
                                }
                                currentState.copy(messages = newMessages)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ChatViewModel", "Erreur reaction_updated", e)
                }
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

            // Nouveaux événements WebSocket pour suppression, modification et statuts
            
            // Événement : message supprimé
            socket?.on("message_deleted") { args ->
                try {
                    if (args.isNotEmpty() && args[0] is JSONObject) {
                        val data = args[0] as JSONObject
                        val messageId = data.getString("messageId")
                        
                        viewModelScope.launch {
                            _state.update { 
                                it.copy(
                                    messages = it.messages.map { msg ->
                                        if (msg.id == messageId) {
                                            msg.copy(
                                                isDeleted = true,
                                                content = "Message supprimé"
                                            )
                                        } else msg
                                    }
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ChatViewModel", "Erreur lors de la réception de message_deleted", e)
                }
            }

            // Événement : message modifié
            socket?.on("message_updated") { args ->
                try {
                    if (args.isNotEmpty() && args[0] is JSONObject) {
                        val messageJson = args[0] as JSONObject
                        val updatedMessage = gson.fromJson(messageJson.toString(), MessageResponse::class.java)
                        
                        viewModelScope.launch {
                            _state.update { 
                                it.copy(
                                    messages = it.messages.map { msg ->
                                        if (msg.id == updatedMessage.id) updatedMessage else msg
                                    }
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ChatViewModel", "Erreur lors de la réception de message_updated", e)
                }
            }

            // Événement : statut du message changé
            socket?.on("message_status_changed") { args ->
                try {
                    if (args.isNotEmpty() && args[0] is JSONObject) {
                        val data = args[0] as JSONObject
                        val messageId = data.getString("messageId")
                        val status = data.getString("status")
                        val deliveredAt = if (data.has("deliveredAt")) data.getString("deliveredAt") else null
                        val readAt = if (data.has("readAt")) data.getString("readAt") else null
                        
                        viewModelScope.launch {
                            _state.update { 
                                it.copy(
                                    messages = it.messages.map { msg ->
                                        if (msg.id == messageId) {
                                            msg.copy(
                                                status = status,
                                                deliveredAt = deliveredAt,
                                                readAt = readAt,
                                                read = status == "read"
                                            )
                                        } else msg
                                    }
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ChatViewModel", "Erreur lors de la réception de message_status_changed", e)
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
                
                // Log pour vérifier les images dans les messages
                Log.d("ChatViewModel", "📥 Messages chargés: ${messages.size}")
                messages.forEachIndexed { index, message ->
                    Log.d("ChatViewModel", "Message $index: id=${message.id}, type=${message.type}, images=${message.images?.size ?: 0}")
                    message.images?.forEachIndexed { imgIndex, imgUrl ->
                        Log.d("ChatViewModel", "  Image $imgIndex: $imgUrl")
                    }
                }
                
                _state.update { 
                    it.copy(
                        messages = messages,
                        isLoading = false
                    )
                }

                // Marquer les messages reçus comme lus
                // Marquer tous les messages de la visite comme lus si nécessaire
                if (messages.any { it.receiverId == userId && it.status != "read" }) {
                    try {
                        repository.markAllAsRead(visiteId)
                    } catch (e: Exception) {
                        Log.e("ChatViewModel", "Erreur lors du marquage des messages comme lus", e)
                    }
                }

                // Rejoindre la room Socket.IO pour cette visite
                joinVisite(visiteId)
            } catch (error: Exception) {
                Log.e("ChatViewModel", "❌ Erreur lors du chargement des messages", error)
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

    // Nouvelles fonctions pour suppression, modification et statuts
    
    /**
     * Supprimer un message (soft delete)
     * Le message sera marqué comme supprimé dans la base de données
     * et affiché comme "Message supprimé" dans l'UI
     */
    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            try {
                // Appeler l'API pour supprimer le message
                val deletedMessage = repository.deleteMessage(messageId)
                
                // Mettre à jour l'état local
                _state.update { 
                    it.copy(
                        messages = it.messages.map { msg ->
                            if (msg.id == messageId) {
                                deletedMessage
                            } else msg
                        }
                    )
                }
                
                // Émettre l'événement via Socket.IO pour notifier les autres utilisateurs
                socket?.emit("delete_message", JSONObject().apply {
                    put("messageId", messageId)
                })
                
            } catch (error: Exception) {
                Log.e("ChatViewModel", "Erreur lors de la suppression du message", error)
                _state.update { 
                    it.copy(error = error.message ?: "Erreur lors de la suppression du message")
                }
            }
        }
    }

    /**
     * Modifier le contenu d'un message
     * Seuls les messages texte peuvent être modifiés
     */
    fun editMessage(messageId: String, newContent: String) {
        if (newContent.isBlank()) return
        
        viewModelScope.launch {
            try {
                // Appeler l'API pour modifier le message
                val updatedMessage = repository.updateMessage(messageId, newContent)
                
                // Mettre à jour l'état local
                _state.update { 
                    it.copy(
                        messages = it.messages.map { msg ->
                            if (msg.id == messageId) {
                                updatedMessage
                            } else msg
                        }
                    )
                }
                
                // Émettre l'événement via Socket.IO pour notifier les autres utilisateurs
                socket?.emit("update_message", JSONObject().apply {
                    put("messageId", messageId)
                    put("content", newContent)
                })
                
            } catch (error: Exception) {
                Log.e("ChatViewModel", "Erreur lors de la modification du message", error)
                _state.update { 
                    it.copy(error = error.message ?: "Erreur lors de la modification du message")
                }
            }
        }
    }

    /**
     * Mettre à jour le statut d'un message
     * Statuts possibles : "sent", "delivered", "read"
     */
    fun updateMessageStatus(messageId: String, status: String) {
        viewModelScope.launch {
            try {
                // Appeler l'API pour mettre à jour le statut
                val updatedMessage = repository.updateMessageStatus(messageId, status)
                
                // Mettre à jour l'état local
                _state.update { 
                    it.copy(
                        messages = it.messages.map { msg ->
                            if (msg.id == messageId) {
                                updatedMessage
                            } else msg
                        }
                    )
                }
                
                // Émettre l'événement via Socket.IO pour notifier l'expéditeur
                socket?.emit("update_message_status", JSONObject().apply {
                    put("messageId", messageId)
                    put("status", status)
                })
                
            } catch (error: Exception) {
                // Ignorer les erreurs silencieusement pour ne pas perturber l'UX
                Log.e("ChatViewModel", "Erreur lors de la mise à jour du statut", error)
            }
        }
    }

    /**
     * Ajouter ou retirer une réaction à un message
     */
    /**
     * Ajouter ou retirer une réaction à un message
     * Implémente une mise à jour optimiste (immédiate) pour l'UX
     */
    fun toggleReaction(messageId: String, emoji: String) {
        val currentUserId = userId ?: return // Si pas connecté, on ne fait rien

        // 1. Mise à jour Optimiste (Immédiate)
        _state.update { currentState ->
            val newMessages = currentState.messages.toMutableList()
            val index = newMessages.indexOfFirst { it.id == messageId }
            
            if (index != -1) {
                val msg = newMessages[index]
                // Copie mutable des réactions existantes
                val currentReactions = msg.reactions?.toMutableMap() ?: mutableMapOf()
                // Liste des users pour cet emoji
                val users = currentReactions[emoji]?.toMutableList() ?: mutableListOf()
                
                // Logique de bascule (Toggle)
                if (users.contains(currentUserId)) {
                    users.remove(currentUserId)
                    if (users.isEmpty()) {
                        currentReactions.remove(emoji)
                    } else {
                        currentReactions[emoji] = users
                    }
                } else {
                    users.add(currentUserId)
                    currentReactions[emoji] = users
                }
                
                // Mettre à jour le message localement tout de suite
                newMessages[index] = msg.copy(reactions = HashMap(currentReactions))
            }
            
            currentState.copy(messages = newMessages)
        }

        // 2. Appel serveur via WebSocket UNIQUEMENT (pour éviter double toggle)
        viewModelScope.launch {
            try {
                // On utilise le socket pour l'action ET la propagation
                // L'appel REST est supprimé car le Gateway gère déjà la logique métier + notification
                socket?.emit("toggle_reaction", JSONObject().apply {
                    put("messageId", messageId)
                    put("emoji", emoji)
                })
                
                Log.d("ChatViewModel", "Action réaction envoyée via Socket")
                
            } catch (error: Exception) {
                Log.e("ChatViewModel", "Erreur lors de l'envoi de la réaction socket", error)
            }
        }
    }
}

