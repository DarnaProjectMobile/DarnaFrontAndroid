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
        Log.d("ChatViewModel", "🔄 [VERSION V2] connectSocket appelé - Nouvelle version avec retry auto")
        Log.d("ChatViewModel", "🔌 Tentative de connexion Socket...")
        Log.d("ChatViewModel", "   - token: ${if (token != null) "✅ Présent" else "❌ NULL"}")
        Log.d("ChatViewModel", "   - userId: ${if (userId != null) "✅ $userId" else "❌ NULL"}")
        Log.d("ChatViewModel", "   - baseUrl: $baseUrl")
        
        if (token == null || userId == null) {
            Log.e("ChatViewModel", "❌ Token ou userId manquant, Socket.IO non connecté")
            return
        }
        
        if (socket != null && socket!!.connected()) {
            Log.d("ChatViewModel", "✅ Socket déjà connecté")
            return
        }

        try {
            // Construire l'URL Socket.IO (enlever le trailing slash si présent)
            val socketUrl = baseUrl.removeSuffix("/")
            val fullSocketUrl = "$socketUrl/chat"
            
            Log.d("ChatViewModel", "🔌 Configuration Socket.IO:")
            Log.d("ChatViewModel", "   - URL complète: $fullSocketUrl")
            Log.d("ChatViewModel", "   - Token présent: ${token != null}")
            Log.d("ChatViewModel", "   - Token longueur: ${token?.length ?: 0}")
            
            val options = IO.Options().apply {
                auth = mapOf("token" to token)
                reconnection = true
                reconnectionAttempts = 5
                reconnectionDelay = 1000
                reconnectionDelayMax = 5000
                timeout = 10000 // 10 secondes timeout
                transports = arrayOf("websocket", "polling")
                forceNew = false
            }

            socket = IO.socket(fullSocketUrl, options)
            
            Log.d("ChatViewModel", "✅ Socket créé, tentative de connexion...")

            socket?.on(Socket.EVENT_CONNECT) {
                Log.d("ChatViewModel", "✅✅✅ Socket CONNECTÉ avec succès!")
                viewModelScope.launch {
                    _state.update { it.copy(isConnected = true) }
                }
            }

            socket?.on(Socket.EVENT_DISCONNECT) { args ->
                val reason = if (args.isNotEmpty()) args[0].toString() else "raison inconnue"
                Log.w("ChatViewModel", "⚠️ Socket déconnecté - Raison: $reason")
                viewModelScope.launch {
                    _state.update { it.copy(isConnected = false) }
                }
            }

            socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                val error = if (args.isNotEmpty()) args[0].toString() else "erreur inconnue"
                Log.e("ChatViewModel", "❌ Erreur de connexion Socket: $error")
                Log.e("ChatViewModel", "   - Vérifiez que le serveur est accessible à: $fullSocketUrl")
                Log.e("ChatViewModel", "   - Vérifiez que le token JWT est valide")
                viewModelScope.launch {
                    _state.update { 
                        it.copy(
                            isConnected = false,
                            error = "Erreur de connexion au chat en temps réel: $error"
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
                    Log.d("ChatViewModel", "📥 Événement reaction_updated reçu!")
                    Log.d("ChatViewModel", "   - Args: ${args.contentToString()}")
                    
                    if (args.isNotEmpty() && args[0] is JSONObject) {
                        val data = args[0] as JSONObject
                        val messageId = data.getString("messageId")
                        val reactions = data.optJSONObject("reactions")
                        
                        Log.d("ChatViewModel", "   - messageId: $messageId")
                        Log.d("ChatViewModel", "   - reactions JSON: $reactions")
                        
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
                                Log.d("ChatViewModel", "   - Emoji $emoji: ${userIdsList.size} utilisateur(s)")
                            }
                        }
                        
                        Log.d("ChatViewModel", "   - reactionsMap final: $reactionsMap")
                        
                        viewModelScope.launch {
                            _state.update { currentState ->
                                val newMessages = currentState.messages.toMutableList()
                                val index = newMessages.indexOfFirst { it.id == messageId }
                                if (index != -1) {
                                    Log.d("ChatViewModel", "   ✅ Message trouvé à l'index $index, mise à jour des réactions")
                                    // Important : créer une NOUVELLE instance du message avec une NOUVELLE map pour déclencher la recomposition
                                    newMessages[index] = newMessages[index].copy(reactions = HashMap(reactionsMap))
                                } else {
                                    Log.w("ChatViewModel", "   ⚠️ Message non trouvé avec l'ID: $messageId")
                                }
                                currentState.copy(messages = newMessages)
                            }
                            Log.d("ChatViewModel", "✅ État mis à jour avec les nouvelles réactions")
                        }
                    } else {
                        Log.w("ChatViewModel", "   ⚠️ Format de données invalide")
                    }
                } catch (e: Exception) {
                    Log.e("ChatViewModel", "❌ Erreur reaction_updated", e)
                    e.printStackTrace()
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

            Log.d("ChatViewModel", "🚀 Appel de socket.connect()...")
            socket?.connect()
            Log.d("ChatViewModel", "⏳ Connexion en cours... (asynchrone)")
        } catch (e: Exception) {
            Log.e("ChatViewModel", "❌❌❌ Erreur CRITIQUE lors de la connexion Socket.IO", e)
            Log.e("ChatViewModel", "   - Message: ${e.message}")
            Log.e("ChatViewModel", "   - Type: ${e.javaClass.simpleName}")
            e.printStackTrace()
            viewModelScope.launch {
                _state.update { 
                    it.copy(
                        isConnected = false,
                        error = "Impossible de se connecter au chat en temps réel: ${e.message}"
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
        val currentUserId = userId ?: run {
            Log.e("ChatViewModel", "❌ toggleReaction: userId est null")
            return
        }
        
        Log.d("ChatViewModel", "🎯 toggleReaction appelé - messageId: $messageId, emoji: $emoji, userId: $currentUserId")

        // 1. Mise à jour Optimiste (Immédiate)
        _state.update { currentState ->
            val newMessages = currentState.messages.toMutableList()
            val index = newMessages.indexOfFirst { it.id == messageId }
            
            if (index != -1) {
                val msg = newMessages[index]
                Log.d("ChatViewModel", "📝 Message trouvé à l'index $index")
                Log.d("ChatViewModel", "📝 Réactions actuelles: ${msg.reactions}")
                
                // Copie mutable des réactions existantes
                val currentReactions = msg.reactions?.toMutableMap() ?: mutableMapOf()
                // Liste des users pour cet emoji
                val users = currentReactions[emoji]?.toMutableList() ?: mutableListOf()
                
                Log.d("ChatViewModel", "📝 Users ayant réagi avec $emoji: $users")
                
                // Logique de bascule (Toggle)
                if (users.contains(currentUserId)) {
                    users.remove(currentUserId)
                    Log.d("ChatViewModel", "➖ Retrait de la réaction $emoji pour l'utilisateur $currentUserId")
                    if (users.isEmpty()) {
                        currentReactions.remove(emoji)
                        Log.d("ChatViewModel", "🗑️ Suppression de l'emoji $emoji (plus aucun utilisateur)")
                    } else {
                        currentReactions[emoji] = users
                    }
                } else {
                    users.add(currentUserId)
                    currentReactions[emoji] = users
                    Log.d("ChatViewModel", "➕ Ajout de la réaction $emoji pour l'utilisateur $currentUserId")
                }
                
                Log.d("ChatViewModel", "📝 Nouvelles réactions: $currentReactions")
                
                // Mettre à jour le message localement tout de suite
                newMessages[index] = msg.copy(reactions = HashMap(currentReactions))
            } else {
                Log.e("ChatViewModel", "❌ Message non trouvé avec l'ID: $messageId")
            }
            
            currentState.copy(messages = newMessages)
        }

        // 2. Appel serveur via WebSocket avec retry et reconnexion automatique
        viewModelScope.launch {
            try {
                // Vérifier si le socket existe
                if (socket == null) {
                    Log.w("ChatViewModel", "⚠️ Socket est null, tentative de reconnexion...")
                    connectSocket()
                    kotlinx.coroutines.delay(500) // Attendre 500ms pour la connexion
                }
                
                // Vérifier si le socket est connecté, sinon tenter de le connecter
                if (socket?.connected() != true) {
                    Log.w("ChatViewModel", "⚠️ Socket non connecté, tentative de connexion...")
                    socket?.connect()
                    
                    // Attendre jusqu'à 2 secondes pour que la connexion s'établisse
                    var retries = 0
                    while (socket?.connected() != true && retries < 4) {
                        kotlinx.coroutines.delay(500)
                        retries++
                        Log.d("ChatViewModel", "⏳ Attente de connexion... tentative $retries/4")
                    }
                }
                
                // Vérifier à nouveau si le socket est connecté
                if (socket?.connected() != true) {
                    Log.e("ChatViewModel", "❌ Socket toujours non connecté après tentatives de reconnexion")
                    _state.update { it.copy(error = "Impossible de se connecter au serveur. Vérifiez votre connexion.") }
                    return@launch
                }
                
                val payload = JSONObject().apply {
                    put("messageId", messageId)
                    put("emoji", emoji)
                }
                
                Log.d("ChatViewModel", "📤 Envoi de toggle_reaction via Socket: $payload")
                
                socket?.emit("toggle_reaction", payload)
                
                Log.d("ChatViewModel", "✅ Action réaction envoyée via Socket")
                
            } catch (error: Exception) {
                Log.e("ChatViewModel", "❌ Erreur lors de l'envoi de la réaction socket", error)
                _state.update { it.copy(error = "Erreur lors de l'envoi de la réaction: ${error.message}") }
            }
        }
    }
}

